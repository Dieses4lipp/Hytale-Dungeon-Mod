package com.example.plugin.Ui.PlayPage;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.AttachedToType;
import com.hypixel.hytale.protocol.ClientCameraView;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.MouseInputTargetType;
import com.hypixel.hytale.protocol.MouseInputType;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.PositionDistanceOffsetType;
import com.hypixel.hytale.protocol.RotationType;
import com.hypixel.hytale.protocol.ServerCameraSettings;
import com.hypixel.hytale.protocol.Vector2f;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.CameraManager;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class InventoryPage extends InteractiveCustomUIPage<InventoryPage.Data> {

    public World world;
    public PlayerRef playerRef;
    private static final Map<String, ItemContainer> player_inventorys = new HashMap<>();
    private static final Map<String, String> selectedSlotAction = new HashMap<>();

    private static class SlotData {
        ItemContainer container;
        short index;
        boolean isArmorSlot;

        SlotData(ItemContainer container, short index, boolean isArmorSlot) {
            this.container = container;
            this.index = index;
            this.isArmorSlot = isArmorSlot;
        }
    }

    private SlotData resolveSlot(Player player, String action) {
        if (action.startsWith("slot_clicked_")) {
            short index = Short.parseShort(action.replace("slot_clicked_", ""));
            return new SlotData(getOrCreateEmptyStash(player.getUuid().toString()), index, false);
        }

        Inventory inv = player.getInventory();
        if (inv == null)
            return null;

        ItemContainer armor = inv.getArmor();
        return switch (action) {
            case "equip_head" -> new SlotData(armor, (short) 0, true);
            case "equip_chest" -> new SlotData(armor, (short) 1, true);
            case "equip_gloves" -> new SlotData(armor, (short) 2, true);
            case "equip_pants" -> new SlotData(armor, (short) 3, true);
            case "equip_weapon" -> new SlotData(inv.getHotbar(), (short) 0, true);
            case "equip_shield" -> new SlotData(inv.getUtility(), (short) 1, true);
            case "equip_potion" -> new SlotData(inv.getHotbar(), (short) 8, true);
            default -> null;
        };
    }

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .append(new KeyedCodec<>("ButtonClicked", Codec.STRING),
                        (data, value) -> data.clickedButton = value,
                        data -> data.clickedButton)
                .add()
                .build();

        private String clickedButton;
    }

    public InventoryPage(PlayerRef playerRef, World world) {
        super(playerRef, CustomPageLifetime.CantClose, Data.CODEC);
        this.world = world;
        this.playerRef = playerRef;
    }

    public static ItemContainer getOrCreateEmptyStash(String playerId) {
        return player_inventorys.computeIfAbsent(playerId, id -> new SimpleItemContainer((short) 90));
    }

    private boolean isItemValidForSlot(ItemStack item, String action) {
        if (item == null || ItemStack.isEmpty(item))
            return true;
        String id = item.getItem().getId();

        return switch (action) {
            case "equip_head" -> id.endsWith("_Head");
            case "equip_chest" -> id.endsWith("_Chest");
            case "equip_gloves" -> id.endsWith("_Hands");
            case "equip_pants" -> id.endsWith("_Legs");

            case "equip_weapon" -> id.startsWith("Weapon_") && !id.contains("_Shield_");

            case "equip_shield" -> id.contains("_Shield_");

            case "equip_potion" -> id.startsWith("Potion_");

            default -> true;
        };
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder,
            @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/InventoryPage.ui");
        var stats = com.example.plugin.Stats.PlayerLevelComponent.getStats(store, ref);
        int currentLevel = (stats != null) ? stats.level : 1;
        int currentXp = (stats != null) ? stats.xp : 0;
        int xpNeeded = currentLevel * 100;
        uiCommandBuilder.set("#LevelLabel.Text", "Lv. " + currentLevel);
        uiCommandBuilder.set("#XpLabel.Text", currentXp + " / " + xpNeeded + " XP");
        uiEventBuilder.addEventBinding(
                com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType.Activating, "#PlayBtn",
                EventData.of("ButtonClicked", "nav_play"), false);
        uiEventBuilder.addEventBinding(
                com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType.Activating, "#StashBtn",
                EventData.of("ButtonClicked", "nav_inventory"), false);
        uiEventBuilder.addEventBinding(
                com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType.Activating, "#CharacterBtn",
                EventData.of("ButtonClicked", "nav_character"), false);
        uiEventBuilder.addEventBinding(
                com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType.Activating, "#MarketBtn",
                EventData.of("ButtonClicked", "nav_market"), false);
        uiEventBuilder.addEventBinding(
                com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType.Activating, "#LeaderboardBtn",
                EventData.of("ButtonClicked", "nav_leaderboard"), false);
        uiEventBuilder.addEventBinding(
                com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType.Activating, "#CloseBtn",
                EventData.of("ButtonClicked", "nav_close"), false);
        // Dynamische Equipment Bindings
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#EquipHeadBtn",
                EventData.of("ButtonClicked", "equip_head"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#EquipChestBtn",
                EventData.of("ButtonClicked", "equip_chest"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#EquipGlovesBtn",
                EventData.of("ButtonClicked", "equip_gloves"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#EquipPantsBtn",
                EventData.of("ButtonClicked", "equip_pants"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#EquipWeaponBtn",
                EventData.of("ButtonClicked", "equip_weapon"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#EquipShieldBtn",
                EventData.of("ButtonClicked", "equip_shield"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#EquipHealBtn",
                EventData.of("ButtonClicked", "equip_potion"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#EquipTrashBtn",
                EventData.of("ButtonClicked", "trash_item"), false);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            String playerId = player.getUuid().toString();
            String currentSelection = selectedSlotAction.get(playerId);
            Inventory inventory = player.getInventory();

            if (inventory != null) {
                // Rüstung
                ItemContainer armor = inventory.getArmor();
                appendArmorSlot(uiCommandBuilder, armor, (short) 0, "#EquipHead", "EquipHeadBtn", "Head",
                        currentSelection != null && currentSelection.equals("equip_head"));
                appendArmorSlot(uiCommandBuilder, armor, (short) 1, "#EquipChest", "EquipChestBtn", "Chest",
                        currentSelection != null && currentSelection.equals("equip_chest"));
                appendArmorSlot(uiCommandBuilder, armor, (short) 2, "#EquipGloves", "EquipGlovesBtn", "Gloves",
                        currentSelection != null && currentSelection.equals("equip_gloves"));
                appendArmorSlot(uiCommandBuilder, armor, (short) 3, "#EquipPants", "EquipPantsBtn", "Pants",
                        currentSelection != null && currentSelection.equals("equip_pants"));

                // Waffen & Schild (100% gleiche Logik wie Rüstung)
                appendArmorSlot(uiCommandBuilder, inventory.getHotbar(), (short) 0, "#EquipWeapon", "EquipWeaponBtn",
                        "Weapon", currentSelection != null && currentSelection.equals("equip_weapon"));
                appendArmorSlot(uiCommandBuilder, inventory.getUtility(), (short) 1, "#EquipShield", "EquipShieldBtn",
                        "Shield", currentSelection != null && currentSelection.equals("equip_shield"));
                appendArmorSlot(uiCommandBuilder, inventory.getHotbar(), (short) 8, "#EquipHeal", "EquipHealBtn",
                        "Potion", currentSelection != null && currentSelection.equals("equip_potion"));

            }
            String trashTooltip = "TRASH ITEM (Permanently Deletes Item)";
            uiCommandBuilder.appendInline("#EquipTrash",
                    "TextButton #EquipTrashBtn { Anchor: (Full: 0); Text: \"TRASH\"; Background: #ff0000(0.1); TooltipText: \""
                            + trashTooltip
                            + "\"; Style: (Hovered: (Background: #ff0000(0.4)), Default: (LabelStyle: (TextColor: #ff5555, RenderBold: true, HorizontalAlignment: Center, VerticalAlignment: Center))); }");
            if (currentSelection != null && currentSelection.equals("trash_item")) {
                        uiCommandBuilder.appendInline("#EquipTrash",
                                "Group { Anchor: (Top: 0, Left: 0, Right: 0, Height: 2); Background: #f5c518; }");
                        uiCommandBuilder.appendInline("#EquipTrash",
                                "Group { Anchor: (Bottom: 0, Left: 0, Right: 0, Height: 2); Background: #f5c518; }");
                        uiCommandBuilder.appendInline("#EquipTrash",
                                "Group { Anchor: (Left: 0, Top: 0, Bottom: 0, Width: 2); Background: #f5c518; }");
                        uiCommandBuilder.appendInline("#EquipTrash",
                                "Group { Anchor: (Right: 0, Top: 0, Bottom: 0, Width: 2); Background: #f5c518; }");
                    }
            // Stash Slots
            ItemContainer stash = getOrCreateEmptyStash(playerId);
            for (short i = 0; i < 90; i++) {
                String slotGroupId = "#Slot" + (i + 1);
                String btnId = "Slot" + (i + 1) + "Btn";
                var item = stash.getItemStack(i);

                if (item != null && !ItemStack.isEmpty(item)) {
                    uiCommandBuilder.appendInline(slotGroupId, "ItemSlot { ItemId: \"" + item.getItem().getId()
                            + "\"; Anchor: (Full: 0); ShowQuantity: true; }");
                    uiCommandBuilder.appendInline(slotGroupId,
                            "TextButton #" + btnId
                                    + " { Anchor: (Full: 0); Text: \"\"; Background: #30435f(0.0); TooltipText: \""
                                    + item.getItem().getId() + "\"; Style: (Hovered: (Background: #254a7588)); }");
                } else {
                    uiCommandBuilder.appendInline(slotGroupId, "TextButton #" + btnId
                            + " { Anchor: (Full: 0); Text: \"\"; Background: #30435f(0.0); Style: (Hovered: (Background: #254a7588)); }");
                }

                if (currentSelection != null && currentSelection.equals("slot_clicked_" + i)) {
                    // Top Edge
                    uiCommandBuilder.appendInline(slotGroupId,
                            "Group { Anchor: (Top: 0, Left: 0, Right: 0, Height: 2); Background: #f5c518; }");
                    // Bottom Edge
                    uiCommandBuilder.appendInline(slotGroupId,
                            "Group { Anchor: (Bottom: 0, Left: 0, Right: 0, Height: 2); Background: #f5c518; }");
                    // Left Edge
                    uiCommandBuilder.appendInline(slotGroupId,
                            "Group { Anchor: (Left: 0, Top: 0, Bottom: 0, Width: 2); Background: #f5c518; }");
                    // Right Edge
                    uiCommandBuilder.appendInline(slotGroupId,
                            "Group { Anchor: (Right: 0, Top: 0, Bottom: 0, Width: 2); Background: #f5c518; }");
                }
                uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#" + btnId,
                        EventData.of("ButtonClicked", "slot_clicked_" + i), false);
            }
        }
    }

    private void appendArmorSlot(UICommandBuilder cmd, ItemContainer container, short slot, String groupId,
            String btnId, String fallback, boolean isSelected) {
        var item = container.getItemStack(slot);
        if (item != null && !ItemStack.isEmpty(item)) {
            cmd.appendInline(groupId,
                    "ItemSlot { ItemId: \"" + item.getItem().getId() + "\"; Anchor: (Full: 0); ShowQuantity: false; }");
            cmd.appendInline(groupId,
                    "TextButton #" + btnId
                            + " { Anchor: (Full: 0); Text: \"\"; Background: #141c28(0.0); TooltipText: \""
                            + item.getItem().getId() + "\"; Style: (Hovered: (Background: #254a7588)); }");
        } else {
            cmd.appendInline(groupId, "TextButton #" + btnId + " { Anchor: (Full: 0); Text: \"" + fallback
                    + "\"; Background: #141c28(0.0); Style: (Hovered: (Background: #254a7588), Default: (LabelStyle: (HorizontalAlignment: Center, VerticalAlignment: Center))); }");
        }
        if (isSelected) {
            // Obere Linie
            cmd.appendInline(groupId, "Group { Anchor: (Top: 0, Left: 0, Right: 0, Height: 2); Background: #f5c518; }");
            // Untere Linie
            cmd.appendInline(groupId,
                    "Group { Anchor: (Bottom: 0, Left: 0, Right: 0, Height: 2); Background: #f5c518; }");
            // Linke Linie
            cmd.appendInline(groupId, "Group { Anchor: (Left: 0, Top: 0, Bottom: 0, Width: 2); Background: #f5c518; }");
            // Rechte Linie
            cmd.appendInline(groupId,
                    "Group { Anchor: (Right: 0, Top: 0, Bottom: 0, Width: 2); Background: #f5c518; }");
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, Data data) {
        super.handleDataEvent(ref, store, data);
        if (data.clickedButton == null)
            return;
        String action = data.clickedButton;
        data.clickedButton = null;

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null)
            return;
        String playerId = player.getUuid().toString();

        if (action.equals("nav_play")) {
            player.getPageManager().openCustomPage(ref, store, new PlayPage(playerRef, world));
            PlayPage.LineUpCameraForCamModel(store, ref, playerRef);
            return;
        }
        if (action.equals("nav_inventory")) {
            player.getPageManager().openCustomPage(ref, store, new InventoryPage(playerRef, world));
            return;
        }
        if (action.equals("nav_character")) {
            player.getPageManager().openCustomPage(ref, store, new CharacterPage(playerRef, world));
            return;
        }
        if (action.equals("nav_market")) {
            player.getPageManager().openCustomPage(ref, store, new MarketPage(playerRef, world));
            return;
        }
        if (action.equals("nav_leaderboard")) {
            player.getPageManager().openCustomPage(ref, store, new LeaderboardPage(playerRef, world));
            return;
        }
        if (action.equals("nav_close")) {
            player.getPageManager().setPage(ref, store, Page.None);
            resetCamera(ref, store);
            return;
        }

        if (action.startsWith("slot_clicked_") || action.startsWith("equip_") || action.equals("trash_item")) {
            String currentSelection = selectedSlotAction.get(playerId);
            if (action.equals("trash_item")) {
                if (currentSelection != null && !currentSelection.equals("trash_item")) {
                    SlotData a = resolveSlot(player, currentSelection);
                    if (a != null) {
                        a.container.setItemStackForSlot(a.index, null);

                        java.util.concurrent.CompletableFuture.runAsync(() -> {
                            com.example.plugin.Stats.SellConfig.saveStashToDatabase(playerId,
                                    getOrCreateEmptyStash(playerId));
                        });
                    }
                    selectedSlotAction.remove(playerId);
                } else {
                    selectedSlotAction.put(playerId, action);
                }
                refreshPage(player, ref, store);
                return;
            }
            if (currentSelection != null && currentSelection.equals("trash_item")) {
                SlotData b = resolveSlot(player, action);
                if (b != null) {
                    b.container.setItemStackForSlot(b.index, null);

                    java.util.concurrent.CompletableFuture.runAsync(() -> {
                        com.example.plugin.Stats.SellConfig.saveStashToDatabase(playerId,
                                getOrCreateEmptyStash(playerId));
                    });
                }
                selectedSlotAction.remove(playerId);
                refreshPage(player, ref, store);
                return;
            }
            if (currentSelection == null) {
                SlotData slot = resolveSlot(player, action);
                if (slot != null && slot.container.getItemStack(slot.index) != null)
                    selectedSlotAction.put(playerId, action);
            } else if (currentSelection.equals(action)) {
                selectedSlotAction.remove(playerId);
            } else {
                SlotData a = resolveSlot(player, currentSelection);
                SlotData b = resolveSlot(player, action);
                if (a != null && b != null) {
                    var itemA = a.container.getItemStack(a.index);
                    var itemB = b.container.getItemStack(b.index);

                    if (a.isArmorSlot && !isItemValidForSlot(itemB, currentSelection)) {
                        selectedSlotAction.remove(playerId);
                        refreshPage(player, ref, store);
                        return;
                    }
                    if (b.isArmorSlot && !isItemValidForSlot(itemA, action)) {
                        selectedSlotAction.remove(playerId);
                        refreshPage(player, ref, store);
                        return;
                    }

                    ItemContainer temp = new SimpleItemContainer((short) 1);
                    if (itemA != null)
                        a.container.moveItemStackFromSlotToSlot(a.index, itemA.getQuantity(), temp, (short) 0);
                    if (itemB != null)
                        b.container.moveItemStackFromSlotToSlot(b.index, itemB.getQuantity(), a.container, a.index);
                    var tItem = temp.getItemStack((short) 0);
                    if (tItem != null)
                        temp.moveItemStackFromSlotToSlot((short) 0, tItem.getQuantity(), b.container, b.index);

                    java.util.concurrent.CompletableFuture.runAsync(() -> {
                        com.example.plugin.Stats.SellConfig.saveStashToDatabase(playerId,
                                getOrCreateEmptyStash(playerId));
                    });
                }
                selectedSlotAction.remove(playerId);
            }
            refreshPage(player, ref, store);
        }

    }

    private void refreshPage(Player player, Ref<EntityStore> ref, Store<EntityStore> store) {
        player.getPageManager().openCustomPage(ref, store, new InventoryPage(this.playerRef, this.world));
        LineUpCameraForCamModel(store, ref, playerRef);
    }

    private void resetCamera(Ref<EntityStore> ref, Store<EntityStore> store) {
        CameraManager camManager = store.getComponent(ref, CameraManager.getComponentType());
        if (camManager != null) {
            camManager.resetCamera(this.playerRef);
        }
    }

    public static void LineUpCameraForCamModel(Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef) {
        float f3Pitch = (float) Math.toRadians(-11.9);
        float f3Yaw = (float) Math.toRadians(118.2);
        float f3Roll = 0.0f;

        TransformComponent transformComp = store.getComponent(ref, TransformComponent.getComponentType());
        Vector3d currentPosition = transformComp.getPosition();

        Vector3f newLookDirection = new Vector3f(f3Pitch, f3Yaw, f3Roll);
        Teleport teleportComponent = Teleport.createForPlayer(currentPosition, newLookDirection);
        store.addComponent(ref, Teleport.getComponentType(), teleportComponent);

        ServerCameraSettings camSettings = new ServerCameraSettings();
        camSettings.isFirstPerson = false;
        camSettings.distance = 1.4f;
        camSettings.positionOffset = new Position(-0.8, -0.7, 2);
        camSettings.positionLerpSpeed = 0.15f;
        camSettings.rotationLerpSpeed = 0.15f;
        camSettings.attachedToType = AttachedToType.LocalPlayer;
        camSettings.rotationType = RotationType.AttachedToPlusOffset;
        camSettings.rotationOffset = new Direction((float) Math.PI - 0.5f, 0.3f, 0f);
        camSettings.allowPitchControls = false;
        camSettings.sendMouseMotion = false;
        camSettings.mouseInputType = MouseInputType.LookAtTargetEntity;
        camSettings.displayCursor = false;
        camSettings.displayReticle = false;
        camSettings.lookMultiplier = new Vector2f(0.0f, 0.0f);
        camSettings.mouseInputTargetType = MouseInputTargetType.None;
        camSettings.skipCharacterPhysics = false;
        camSettings.eyeOffset = true;
        camSettings.positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffsetRaycast;

        playerRef.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.Custom, false, camSettings));
    }
}
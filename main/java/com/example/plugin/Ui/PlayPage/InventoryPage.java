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

    private ItemContainer getPlayerInventory(Player player) {
        String playerId = player.getUuid().toString();

        if (!player_inventorys.containsKey(playerId)) {
            ItemContainer newStash = new SimpleItemContainer((short) 90);

            Inventory vanillaInv = player.getInventory();
            if (vanillaInv != null) {
                ItemContainer vanillaStorage = vanillaInv.getStorage();
                vanillaStorage.moveItemStackFromSlotToSlot(
                        (short) 0,
                        1,
                        newStash,
                        (short) 5);
                System.out.println("[Stash] Test-Item erfolgreich in den neuen Stash transferiert!");
            }

            player_inventorys.put(playerId, newStash);
        }

        return player_inventorys.get(playerId);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
            @Nonnull UICommandBuilder uiCommandBuilder,
            @Nonnull UIEventBuilder uiEventBuilder,
            @Nonnull Store<EntityStore> store) {

        uiCommandBuilder.append("Pages/InventoryPage.ui");

        // Navigation bindings
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#PlayBtn",
                EventData.of("ButtonClicked", "play"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#StashBtn",
                EventData.of("ButtonClicked", "inventory"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CharacterBtn",
                EventData.of("ButtonClicked", "character"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#MarketBtn",
                EventData.of("ButtonClicked", "market"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#LeaderboardBtn",
                EventData.of("ButtonClicked", "leaderboard"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#EnterBtn",
                EventData.of("ButtonClicked", "equip_mode"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CloseBtn",
                EventData.of("ButtonClicked", "close"), false);

        // Armor slot bindings
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
                EventData.of("ButtonClicked", "equip_heal"), false);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {

            // Sync armor slots on open
            Inventory inventory = player.getInventory();
            if (inventory != null) {
                ItemContainer armor = inventory.getArmor();
                appendArmorSlot(uiCommandBuilder, armor, (short) 0, "#EquipHeadItem", "Head");
                appendArmorSlot(uiCommandBuilder, armor, (short) 1, "#EquipChestItem", "Chest");
                appendArmorSlot(uiCommandBuilder, armor, (short) 2, "#EquipGlovesItem", "Gloves");
                appendArmorSlot(uiCommandBuilder, armor, (short) 3, "#EquipPantsItem", "Pants");
            }

            // Inventory stash slots
            ItemContainer stash = getPlayerInventory(player);
            for (short i = 0; i < 90; i++) {
                String slotId = "#Slot" + (i + 1);
                String btnId = "#Slot" + (i + 1) + "Btn";

                var item = stash.getItemStack(i);
                if (item != null && !ItemStack.isEmpty(item)) {
                    String itemName = item.getItem().getId();

                    // ItemSlot with TooltipText
                    String slotUI = "ItemSlot { ItemId: \"" + itemName
                            + "\"; Anchor: (Full: 0); ShowQuantity: true; TooltipText: \"" + itemName + "\"; }";

                    uiCommandBuilder.appendInline(slotId, slotUI);
                }

                uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, btnId,
                        EventData.of("ButtonClicked", "slot_clicked_" + i), false);
            }
        }
    }

    private void appendArmorSlot(UICommandBuilder cmd, ItemContainer armor, short slot, String groupId,
            String fallback) {
        var item = armor.getItemStack(slot);

        if (item != null && !ItemStack.isEmpty(item)) {
            String itemName = item.getItem().getId();

            // ItemSlot with TooltipText
            String slotUI = "ItemSlot { ItemId: \"" + itemName
                    + "\"; Anchor: (Full: 0); ShowQuantity: false; TooltipText: \"" + itemName + "\"; }";

            cmd.appendInline(groupId, slotUI);
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            Data data) {
        super.handleDataEvent(ref, store, data);

        if (data.clickedButton == null)
            return;
        String action = data.clickedButton;
        data.clickedButton = null;

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null)
            return;

        Inventory inventory = player.getInventory();
        if (inventory == null)
            return;

        // --- Handle Stash Slot Clicks ---
        if (action.startsWith("slot_clicked_")) {
            String slotIndexString = action.replace("slot_clicked_", "");
            try {
                short clickedSlotIndex = Short.parseShort(slotIndexString);
                System.out.println("[Stash] Clicked slot: " + clickedSlotIndex);
            } catch (NumberFormatException e) {
                System.out.println("[Stash] Error parsing slot index.");
            }
            return;
        }

        // --- Navigation ---
        switch (action) {
            case "play" -> {
                PlayPage playPage = new PlayPage(playerRef, world);
                playPage.LineUpCameraForCamModel(store, ref, playerRef);
                player.getPageManager().openCustomPage(ref, store, playPage);
                return;
            }
            case "close" -> {
                player.getPageManager().setPage(ref, store, Page.None);
                resetCamera(ref, store);
                return;
            }
        }

        // --- Armor Slot Removal ---
        short armorSlotToDelete = switch (action) {
            case "equip_head" -> (short) 0;
            case "equip_chest" -> (short) 1;
            case "equip_gloves" -> (short) 2;
            case "equip_pants" -> (short) 3;
            default -> (short) -1;
        };

        if (armorSlotToDelete == -1)
            return;

        ItemContainer armor = inventory.getArmor();
        var item = armor.getItemStack(armorSlotToDelete);

        if (item != null && !ItemStack.isEmpty(item)) {
            System.out.println("[Armor] Removing item from slot " + armorSlotToDelete + ": " + item.getItem().getId());
            armor.setItemStackForSlot(armorSlotToDelete, null);
        } else {
            System.out.println("[Armor] Slot " + armorSlotToDelete + " is already empty.");
        }

        // Refresh the inventory page
        InventoryPage freshPage = new InventoryPage(this.playerRef, this.world);
player.getPageManager().openCustomPage(ref, store, freshPage);
LineUpCameraForCamModel(store, ref, playerRef);
    }

    private void resetCamera(Ref<EntityStore> ref, Store<EntityStore> store) {
        CameraManager camManager = store.getComponent(ref, CameraManager.getComponentType());
        if (camManager != null) {
            camManager.resetCamera(this.playerRef);
        }
    }

    void LineUpCameraForCamModel(Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef) {
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
        camSettings.displayCursor = true;
        camSettings.displayReticle = false;
        camSettings.lookMultiplier = new Vector2f(0.0f, 0.0f);
        camSettings.mouseInputTargetType = MouseInputTargetType.None;
        camSettings.skipCharacterPhysics = false;
        camSettings.eyeOffset = true;
        camSettings.positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffsetRaycast;

        playerRef.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.Custom, false, camSettings));
    }
}
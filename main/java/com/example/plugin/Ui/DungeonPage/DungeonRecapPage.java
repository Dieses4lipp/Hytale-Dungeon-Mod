package com.example.plugin.Ui.DungeonPage;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

import com.example.plugin.DungeonGeneration.DungeonInstance;
import com.example.plugin.DungeonGeneration.DungeonManager;
import com.example.plugin.Stats.SellConfig;
import com.example.plugin.Ui.PlayPage.InventoryPage;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class DungeonRecapPage extends InteractiveCustomUIPage<DungeonRecapPage.Data> {

    private final String statsText;
    private final int dungeonSlot;
    private final PlayerRef playerRef;

    private static final Map<String, String> selectedSlot = new HashMap<>();

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .append(new KeyedCodec<>("ButtonClicked", Codec.STRING),
                        (data, value) -> data.clickedButton = value,
                        data -> data.clickedButton)
                .add()
                .build();
        private String clickedButton;
    }

    public DungeonRecapPage(PlayerRef playerRef, String statsText, int dungeonSlot) {
        super(playerRef, CustomPageLifetime.CantClose, Data.CODEC);
        this.playerRef = playerRef;
        this.statsText = statsText;
        this.dungeonSlot = dungeonSlot;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder cmd,
            @Nonnull UIEventBuilder evt, @Nonnull Store<EntityStore> store) {

        cmd.append("Pages/DungeonRecap.ui");

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null)
            return;

        String playerId = player.getUuid().toString();
        String currentSelection = selectedSlot.get(playerId);

        ItemContainer inventory = player.getInventory().getStorage();
        ItemContainer hotbar = player.getInventory().getHotbar();

        cmd.set("#StatsLabel.Text", this.statsText);
        int goldValue = SellConfig.calculateStashSellValue(playerId, player);
        cmd.set("#GoldValueLabel.Text", "Current earned Gold: " + goldValue + "g");

        var stats = com.example.plugin.Stats.PlayerLevelComponent.getStats(store, ref);
        int currentLevel = (stats != null) ? stats.level : 1;
        cmd.set("#LevelLabel.Text", "LEVEL: " + currentLevel);

        for (short i = 0; i < 36; i++) {
            String groupId = "#Slot" + (i + 1);
            String btnId = "Slot" + (i + 1) + "Btn";
            ItemStack item = null;
            try {
                item = inventory.getItemStack(i);
            } catch (Exception ignored) {
            }

            buildSlot(cmd, evt, groupId, btnId, item, "inv_" + i, currentSelection);
        }

        for (short i = 0; i < 9; i++) {
            String groupId = "#Slot" + (i + 37);
            String btnId = "Slot" + (i + 37) + "Btn";
            ItemStack item = null;
            try {
                item = hotbar.getItemStack(i);
            } catch (Exception ignored) {
            }

            buildSlot(cmd, evt, groupId, btnId, item, "hotbar_" + i, currentSelection);
        }

        if (currentSelection != null) {
            try {
                ItemStack selected = null;
                if (currentSelection.startsWith("inv_")) {
                    selected = inventory.getItemStack(Short.parseShort(currentSelection.replace("inv_", "")));
                } else if (currentSelection.startsWith("hotbar_")) {
                    selected = hotbar.getItemStack(Short.parseShort(currentSelection.replace("hotbar_", "")));
                }

                if (selected != null && !ItemStack.isEmpty(selected)) {
                    cmd.appendInline("#StashTargetSlot", "ItemSlot { ItemId: \"" + selected.getItemId()
                            + "\"; Anchor: (Full: 0); ShowQuantity: true; }");
                }
            } catch (Exception ignored) {
            }
        }

        // Bind the new side-by-side buttons
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#QuickDepositBtn",
                EventData.of("ButtonClicked", "deposit_selected"), false);
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#QuickDepositAllBtn",
                EventData.of("ButtonClicked", "transfer_all"), false);
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#CloseBtn",
                EventData.of("ButtonClicked", "close_recap"), false);
    }

    private void buildSlot(UICommandBuilder cmd, UIEventBuilder evt, String groupId, String btnId, ItemStack item,
            String selectionId, String currentSelection) {
        if (item != null && !ItemStack.isEmpty(item)) {
            cmd.appendInline(groupId,
                    "ItemSlot { ItemId: \"" + item.getItemId() + "\"; Anchor: (Full: 0); ShowQuantity: true; }");

            int itemValue = SellConfig.getItemSellValue(item);
                String tooltip = "Will be sold automatically for: ";
            if (itemValue > 0) {
                int totalStackValue = itemValue * item.getQuantity();
                tooltip += totalStackValue + " Gold";

                cmd.appendInline(groupId,
                        "TextButton #" + btnId
                                + " { Anchor: (Full: 0); Text: \"\"; Background: #ff0000(0.15); TooltipText: \""
                                + tooltip + "\"; }");
            } else {
                cmd.appendInline(groupId,
                        "TextButton #" + btnId
                                + " { Anchor: (Full: 0); Text: \"\"; Background: #000000(0.0); TooltipText: \""
                                + tooltip + "\"; Style: (Hovered: (Background: #254a7588)); }");
                evt.addEventBinding(CustomUIEventBindingType.Activating, "#" + btnId,
                        EventData.of("ButtonClicked", "select_" + selectionId), false);
            }

        } else {
            cmd.appendInline(groupId,
                    "TextButton #" + btnId + " { Anchor: (Full: 0); Text: \"\"; Background: #000000(0.0); }");
        }

        if (currentSelection != null && currentSelection.equals(selectionId)) {
            cmd.appendInline(groupId, "Group { Anchor: (Full: 0); Background: #f5c518(0.1); }");
            cmd.appendInline(groupId, "Group { Anchor: (Top: 0, Left: 0, Right: 0, Height: 2); Background: #f5c518; }");
            cmd.appendInline(groupId,
                    "Group { Anchor: (Bottom: 0, Left: 0, Right: 0, Height: 2); Background: #f5c518; }");
            cmd.appendInline(groupId, "Group { Anchor: (Left: 0, Top: 0, Bottom: 0, Width: 2); Background: #f5c518; }");
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

        if (action.startsWith("select_")) {
            String targetId = action.replace("select_", "");
            selectedSlot.put(playerId, targetId);
            refresh(player, ref, store);
            return;
        }

        if (action.equals("deposit_selected")) {
            String sel = selectedSlot.get(playerId);
            if (sel == null)
                return;

            ItemContainer inventory = player.getInventory().getStorage();
            ItemContainer hotbar = player.getInventory().getHotbar();

            ItemContainer targetSource = sel.startsWith("inv_") ? inventory : hotbar;
            short index = Short.parseShort(sel.replace("inv_", "").replace("hotbar_", ""));

            ItemStack item = null;
            try {
                item = targetSource.getItemStack(index);
            } catch (Exception ignored) {
            }

            if (item == null || ItemStack.isEmpty(item)) {
                selectedSlot.remove(playerId);
                refresh(player, ref, store);
                return;
            }

            ItemContainer stash = InventoryPage.getOrCreateEmptyStash(playerId);
            short freeSlot = findFreeStashSlot(stash);

            if (freeSlot != -1) {
                targetSource.moveItemStackFromSlotToSlot(index, item.getQuantity(), stash, freeSlot);
                selectedSlot.remove(playerId);
            }
            refresh(player, ref, store);
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                com.example.plugin.Stats.SellConfig.saveStashToDatabase(playerId, stash);
            });
            return;
        }

        if (action.equals("transfer_all")) {
            ItemContainer inventory = player.getInventory().getStorage();
            ItemContainer hotbar = player.getInventory().getHotbar();
            ItemContainer stash = InventoryPage.getOrCreateEmptyStash(playerId);

            // Transfer Main Inventory
            for (short i = 0; i < 36; i++) {
                transferToStash(inventory, i, stash);
            }

            // Transfer Hotbar
            for (short i = 0; i < 9; i++) {
                transferToStash(hotbar, i, stash);
            }

            selectedSlot.remove(playerId);
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                com.example.plugin.Stats.SellConfig.saveStashToDatabase(playerId, stash);
            });
            refresh(player, ref, store);
            return;
        }

        if (action.equals("close_recap")) {
            int gold = SellConfig.calculateStashSellValue(playerId, player);
            SellConfig.addGoldToPlayer(player, ref, store, gold);
            SellConfig.removeSellableItemsFromVanillaInventory(player);

            player.getPageManager().setPage(ref, store, Page.None);
            selectedSlot.remove(playerId);

            World activeWorld = DungeonManager.get().activeWorld;
            if (activeWorld != null) {
                Teleport teleport = Teleport.createForPlayer(activeWorld, new Transform(110, 133, 110));
                store.addComponent(ref, Teleport.getComponentType(), teleport);
            }

            DungeonInstance inst = DungeonManager.get().getBySlot(this.dungeonSlot);
            if (inst != null) {
                DungeonManager.get().destroyDungeon(store, null, activeWorld, inst);
            }
        }
    }

    private void transferToStash(ItemContainer source, short sourceIndex, ItemContainer stash) {
        try {
            ItemStack item = source.getItemStack(sourceIndex);
            if (item == null || ItemStack.isEmpty(item))
                return;
            if (SellConfig.getItemSellValue(item) > 0)
                return;
            short freeSlot = findFreeStashSlot(stash);
            if (freeSlot != -1) {
                source.moveItemStackFromSlotToSlot(sourceIndex, item.getQuantity(), stash, freeSlot);
            }
        } catch (Exception ignored) {
        }
    }

    private short findFreeStashSlot(ItemContainer stash) {
        for (short s = 0; s < 90; s++) {
            try {
                ItemStack existing = stash.getItemStack(s);
                if (existing == null || ItemStack.isEmpty(existing)) {
                    return s;
                }
            } catch (Exception ignored) {
            }
        }
        return -1;
    }

    private void refresh(Player player, Ref<EntityStore> ref, Store<EntityStore> store) {
        player.getPageManager().openCustomPage(ref, store,
                new DungeonRecapPage(this.playerRef, this.statsText, this.dungeonSlot));
    }
}
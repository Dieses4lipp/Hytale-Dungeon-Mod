package com.example.plugin.Ui.PlayPage;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

import com.example.plugin.Ui.PlayPage.InventoryPage;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class StashPage extends InteractiveCustomUIPage<StashPage.Data> {

    private static final Map<String, String> selectedSlot = new HashMap<>();

    private final PlayerRef playerRef;
    private final World world;

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .append(new KeyedCodec<>("ButtonClicked", Codec.STRING),
                        (data, value) -> data.clickedButton = value,
                        data -> data.clickedButton)
                .add()
                .build();
        private String clickedButton;
    }

    public StashPage(PlayerRef playerRef, World world) {
        super(playerRef, CustomPageLifetime.CantClose, Data.CODEC);
        this.playerRef = playerRef;
        this.world = world;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder cmd,
            @Nonnull UIEventBuilder evt, @Nonnull Store<EntityStore> store) {

        cmd.append("Pages/StashPage.ui");

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        String playerId = player.getUuid().toString();
        String currentSelection = selectedSlot.get(playerId);

        ItemContainer inventory = player.getInventory().getStorage();
        ItemContainer hotbar = player.getInventory().getHotbar();
        ItemContainer stash = InventoryPage.getOrCreateEmptyStash(playerId);

        // Update hint label if something is selected
        if (currentSelection != null) {
            cmd.set("#SelectionLabel.Text", "Item selected — click a slot to deposit/swap");
        }

        // --- Left: Player inventory (36 main + 9 hotbar) ---
        for (short i = 0; i < 36; i++) {
            buildInvSlot(cmd, evt, "#InvSlot" + (i + 1), "InvSlot" + (i + 1) + "Btn",
                    getItemSafe(inventory, i), "inv_" + i, currentSelection);
        }
        for (short i = 0; i < 9; i++) {
            buildInvSlot(cmd, evt, "#InvSlot" + (i + 37), "InvSlot" + (i + 37) + "Btn",
                    getItemSafe(hotbar, i), "hotbar_" + i, currentSelection);
        }

        // --- Right: Stash ---
        for (short i = 0; i < 90; i++) {
            buildStashSlot(cmd, evt, "#StashSlot" + (i + 1), "StashSlot" + (i + 1) + "Btn",
                    getItemSafe(stash, i), i, currentSelection);
        }

        // Buttons
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#DepositAllBtn",
                EventData.of("ButtonClicked", "deposit_all"), false);
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#CloseBtn",
                EventData.of("ButtonClicked", "close"), false);
    }

    private ItemStack getItemSafe(ItemContainer container, short index) {
        try {
            return container.getItemStack(index);
        } catch (Exception e) {
            return null;
        }
    }

    private void buildInvSlot(UICommandBuilder cmd, UIEventBuilder evt,
            String groupId, String btnId, ItemStack item, String selectionId, String currentSelection) {

        if (item != null && !ItemStack.isEmpty(item)) {
            cmd.appendInline(groupId, "ItemSlot { ItemId: \"" + item.getItem().getId()
                    + "\"; Anchor: (Full: 0); ShowQuantity: true; }");
            cmd.appendInline(groupId, "TextButton #" + btnId
                    + " { Anchor: (Full: 0); Text: \"\"; Background: #000000(0.0); TooltipText: \""
                    + item.getItem().getId() + "\"; Style: (Hovered: (Background: #254a7588)); }");
        } else {
            cmd.appendInline(groupId, "TextButton #" + btnId
                    + " { Anchor: (Full: 0); Text: \"\"; Background: #000000(0.0); Style: (Hovered: (Background: #254a7588)); }");
        }

        if (currentSelection != null && currentSelection.equals(selectionId)) {
            addSelectionHighlight(cmd, groupId);
        }

        // Always add event binding so empty slots can be clicked to deposit stash items
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#" + btnId,
                EventData.of("ButtonClicked", "click_" + selectionId), false);
    }

    private void buildStashSlot(UICommandBuilder cmd, UIEventBuilder evt,
            String groupId, String btnId, ItemStack item, short stashIndex, String currentSelection) {

        if (item != null && !ItemStack.isEmpty(item)) {
            cmd.appendInline(groupId, "ItemSlot { ItemId: \"" + item.getItem().getId()
                    + "\"; Anchor: (Full: 0); ShowQuantity: true; }");
            cmd.appendInline(groupId, "TextButton #" + btnId
                    + " { Anchor: (Full: 0); Text: \"\"; Background: #000000(0.0); TooltipText: \""
                    + item.getItem().getId() + "\"; Style: (Hovered: (Background: #254a7588)); }");
        } else {
            cmd.appendInline(groupId, "TextButton #" + btnId
                    + " { Anchor: (Full: 0); Text: \"\"; Background: #000000(0.0); Style: (Hovered: (Background: #1a3a5c88)); }");
        }

        if (currentSelection != null && currentSelection.equals("stash_" + stashIndex)) {
            addSelectionHighlight(cmd, groupId);
        }

        evt.addEventBinding(CustomUIEventBindingType.Activating, "#" + btnId,
                EventData.of("ButtonClicked", "click_stash_" + stashIndex), false);
    }

    private void addSelectionHighlight(UICommandBuilder cmd, String groupId) {
        cmd.appendInline(groupId, "Group { Anchor: (Top: 0, Left: 0, Right: 0, Height: 2); Background: #f5c518; }");
        cmd.appendInline(groupId, "Group { Anchor: (Bottom: 0, Left: 0, Right: 0, Height: 2); Background: #f5c518; }");
        cmd.appendInline(groupId, "Group { Anchor: (Left: 0, Top: 0, Bottom: 0, Width: 2); Background: #f5c518; }");
        cmd.appendInline(groupId, "Group { Anchor: (Right: 0, Top: 0, Bottom: 0, Width: 2); Background: #f5c518; }");
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, Data data) {
        super.handleDataEvent(ref, store, data);
        if (data.clickedButton == null) return;
        String action = data.clickedButton;
        data.clickedButton = null;

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;
        String playerId = player.getUuid().toString();

        if (action.equals("close")) {
            selectedSlot.remove(playerId);
            player.getPageManager().setPage(ref, store, Page.None);
            return;
        }

        if (action.equals("deposit_all")) {
            ItemContainer inventory = player.getInventory().getStorage();
            ItemContainer hotbar = player.getInventory().getHotbar();
            ItemContainer stash = InventoryPage.getOrCreateEmptyStash(playerId);

            for (short i = 0; i < 36; i++) transferToStash(inventory, i, stash);
            for (short i = 0; i < 9; i++) transferToStash(hotbar, i, stash);

            selectedSlot.remove(playerId);
            saveAndRefresh(player, playerId, stash, ref, store);
            return;
        }

        // Unified click handling for any slot
        if (action.startsWith("click_")) {
            String clickedSlotId = action.replace("click_", ""); // e.g. "inv_5", "stash_10", "hotbar_2"
            String currentSelection = selectedSlot.get(playerId);
            ItemContainer stash = InventoryPage.getOrCreateEmptyStash(playerId);

            // 1. Nothing is selected: Select the clicked item (if it exists)
            if (currentSelection == null) {
                ItemContainer clickedContainer = getContainerForSlot(player, clickedSlotId, stash);
                short clickedIndex = getIndexForSlot(clickedSlotId);
                ItemStack clickedItem = getItemSafe(clickedContainer, clickedIndex);

                if (clickedItem != null && !ItemStack.isEmpty(clickedItem)) {
                    selectedSlot.put(playerId, clickedSlotId);
                }
                refresh(player, ref, store);
                return;
            }

            // 2. Clicked the same slot twice: Deselect
            if (currentSelection.equals(clickedSlotId)) {
                selectedSlot.remove(playerId);
                refresh(player, ref, store);
                return;
            }

            // 3. Different slot clicked: Perform a swap
            ItemContainer sourceContainer = getContainerForSlot(player, currentSelection, stash);
            short sourceIndex = getIndexForSlot(currentSelection);
            
            ItemContainer destContainer = getContainerForSlot(player, clickedSlotId, stash);
            short destIndex = getIndexForSlot(clickedSlotId);

            ItemStack sourceItem = getItemSafe(sourceContainer, sourceIndex);
            ItemStack destItem = getItemSafe(destContainer, destIndex);

            // Swap them using the temporary container method
            swapItems(sourceContainer, sourceIndex, destContainer, destIndex, sourceItem, destItem);
            selectedSlot.remove(playerId); // Clear selection after swap

            // Save if stash was involved in the transaction, otherwise just refresh
            if (currentSelection.startsWith("stash_") || clickedSlotId.startsWith("stash_")) {
                saveAndRefresh(player, playerId, stash, ref, store);
            } else {
                refresh(player, ref, store);
            }
        }
    }

    // --- Helper Methods to parse unified slot IDs ---

    private ItemContainer getContainerForSlot(Player player, String slotId, ItemContainer stash) {
        if (slotId.startsWith("inv_")) return player.getInventory().getStorage();
        if (slotId.startsWith("hotbar_")) return player.getInventory().getHotbar();
        if (slotId.startsWith("stash_")) return stash;
        return null;
    }

    private short getIndexForSlot(String slotId) {
        if (slotId.startsWith("inv_")) return Short.parseShort(slotId.replace("inv_", ""));
        if (slotId.startsWith("hotbar_")) return Short.parseShort(slotId.replace("hotbar_", ""));
        if (slotId.startsWith("stash_")) return Short.parseShort(slotId.replace("stash_", ""));
        return -1;
    }


    private void swapItems(ItemContainer containerA, short indexA,
                           ItemContainer containerB, short indexB,
                           ItemStack itemA, ItemStack itemB) {
        SimpleItemContainer temp = new SimpleItemContainer((short) 1);
        if (itemA != null && !ItemStack.isEmpty(itemA))
            containerA.moveItemStackFromSlotToSlot(indexA, itemA.getQuantity(), temp, (short) 0);
        if (itemB != null && !ItemStack.isEmpty(itemB))
            containerB.moveItemStackFromSlotToSlot(indexB, itemB.getQuantity(), containerA, indexA);
        ItemStack tempItem = getItemSafe(temp, (short) 0);
        if (tempItem != null && !ItemStack.isEmpty(tempItem))
            temp.moveItemStackFromSlotToSlot((short) 0, tempItem.getQuantity(), containerB, indexB);
    }

    private void transferToStash(ItemContainer source, short sourceIndex, ItemContainer stash) {
        try {
            ItemStack item = source.getItemStack(sourceIndex);
            if (item == null || ItemStack.isEmpty(item)) return;
            short freeSlot = findFreeStashSlot(stash);
            if (freeSlot != -1)
                source.moveItemStackFromSlotToSlot(sourceIndex, item.getQuantity(), stash, freeSlot);
        } catch (Exception ignored) {}
    }

    private short findFreeStashSlot(ItemContainer stash) {
        for (short s = 0; s < 90; s++) {
            ItemStack existing = getItemSafe(stash, s);
            if (existing == null || ItemStack.isEmpty(existing)) return s;
        }
        return -1;
    }

    private void saveAndRefresh(Player player, String playerId, ItemContainer stash,
                                 Ref<EntityStore> ref, Store<EntityStore> store) {
        java.util.concurrent.CompletableFuture.runAsync(() ->
            com.example.plugin.Stats.SellConfig.saveStashToDatabase(playerId, stash));
        refresh(player, ref, store);
    }

    private void refresh(Player player, Ref<EntityStore> ref, Store<EntityStore> store) {
        player.getPageManager().openCustomPage(ref, store, new StashPage(this.playerRef, this.world));
    }
}
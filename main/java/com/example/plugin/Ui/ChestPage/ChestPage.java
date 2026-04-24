package com.example.plugin.Ui.ChestPage;

import javax.annotation.Nonnull;
import com.example.plugin.Ui.PlayPage.InventoryPage;
import com.example.plugin.doorsystem.ChestRegistry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
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
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.HashMap;
import java.util.Map;

public class ChestPage extends InteractiveCustomUIPage<ChestPage.Data> {

    private final Vector3i chestPos;
    private final PlayerRef playerRef;

    private static final Map<String, Short> selectedSlot = new HashMap<>();

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .append(new KeyedCodec<>("ButtonClicked", Codec.STRING),
                        (d, v) -> d.clickedButton = v, d -> d.clickedButton)
                .add().build();
        private String clickedButton;
    }

    public ChestPage(PlayerRef playerRef, Vector3i chestPos) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.playerRef = playerRef;
        this.chestPos = chestPos;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
            @Nonnull UICommandBuilder cmd,
            @Nonnull UIEventBuilder evt,
            @Nonnull Store<EntityStore> store) {
        cmd.append("Pages/ChestPage.ui");

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null)
            return;

        String playerId = player.getUuid().toString();
        Short currentSelection = selectedSlot.get(playerId);

        ItemContainer inventory = player.getInventory().getStorage();

        for (short i = 0; i < 36; i++) {
            String groupId = "#Slot" + (i + 1);
            String btnId = "Slot" + (i + 1) + "Btn";
            ItemStack item = null;
            try {
                item = inventory.getItemStack(i);
            } catch (Exception ignored) {
            }

            if (item != null && !ItemStack.isEmpty(item)) {
                cmd.appendInline(groupId, "ItemSlot { ItemId: \"" + item.getItem().getId()
                        + "\"; Anchor: (Full: 0); ShowQuantity: true; }");
                cmd.appendInline(groupId,
                        "TextButton #" + btnId
                                + " { Anchor: (Full: 0); Text: \"\"; Background: #30435f(0.0); TooltipText: \""
                                + item.getItem().getId() + "\"; Style: (Hovered: (Background: #254a7588)); }");
            } else {
                cmd.appendInline(groupId, "TextButton #" + btnId
                        + " { Anchor: (Full: 0); Text: \"\"; Background: #30435f(0.0); Style: (Hovered: (Background: #254a7588)); }");
            }

            if (currentSelection != null && currentSelection == i) {
            cmd.appendInline(groupId, "Group { Anchor: (Top: 0, Left: 0, Right: 0, Height: 2); Background: #f5c518; }");
            cmd.appendInline(groupId, "Group { Anchor: (Bottom: 0, Left: 0, Right: 0, Height: 2); Background: #f5c518; }");
            cmd.appendInline(groupId, "Group { Anchor: (Left: 0, Top: 0, Bottom: 0, Width: 2); Background: #f5c518; }");
            cmd.appendInline(groupId, "Group { Anchor: (Right: 0, Top: 0, Bottom: 0, Width: 2); Background: #f5c518; }");
            }

            evt.addEventBinding(CustomUIEventBindingType.Activating, "#" + btnId,
                    EventData.of("ButtonClicked", "slot_" + i), false);
        }

        if (currentSelection != null) {
            try {
                ItemStack selected = inventory.getItemStack(currentSelection);
                if (selected != null && !ItemStack.isEmpty(selected)) {
                    cmd.appendInline("#SelectedPreview", "ItemSlot { ItemId: \"" + selected.getItem().getId()
                            + "\"; Anchor: (Full: 0); ShowQuantity: true; }");
                }
            } catch (Exception ignored) {
            }
        }

        evt.addEventBinding(CustomUIEventBindingType.Activating, "#DepositBtn",
                EventData.of("ButtonClicked", "deposit"), false);
        evt.addEventBinding(CustomUIEventBindingType.Activating, "#CancelBtn",
                EventData.of("ButtonClicked", "cancel"), false);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store, Data data) {
        super.handleDataEvent(ref, store, data);
        if (data.clickedButton == null)
            return;
        String action = data.clickedButton;
        data.clickedButton = null;

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null)
            return;
        String playerId = player.getUuid().toString();

        if (action.startsWith("slot_")) {
            short index = Short.parseShort(action.replace("slot_", ""));
            ItemContainer inventory = player.getInventory().getStorage();
            ItemStack item = null;
            try {
                item = inventory.getItemStack(index);
            } catch (Exception ignored) {
            }
            if (item != null && !ItemStack.isEmpty(item)) {
                selectedSlot.put(playerId, index);
            }
            refresh(player, ref, store);
            return;
        }

        if (action.equals("deposit")) {
            Short index = selectedSlot.get(playerId);
            if (index == null)
                return;

            ItemContainer vanillaInventory = player.getInventory().getStorage();
            ItemStack item = null;
            try {
                item = vanillaInventory.getItemStack(index);
            } catch (Exception ignored) {
            }
            if (item == null || ItemStack.isEmpty(item)) {
                selectedSlot.remove(playerId);
                return;
            }

            ItemContainer stash = InventoryPage.getOrCreateEmptyStash(playerId);
            short freeSlot = -1;
            for (short s = 0; s < 90; s++) {
                try {
                    ItemStack existing = stash.getItemStack(s);
                    if (existing == null || ItemStack.isEmpty(existing)) {
                        freeSlot = s;
                        break;
                    }
                } catch (Exception ignored) {
                }
            }

            if (freeSlot == -1) {
                selectedSlot.remove(playerId);
                player.getPageManager().setPage(ref, store, Page.None);
                return;
            }

            vanillaInventory.moveItemStackFromSlotToSlot(index, 1, stash, freeSlot);


            ChestRegistry.lock(chestPos);
            selectedSlot.remove(playerId);
            player.getPageManager().setPage(ref, store, Page.None);
            return;
        }

        if (action.equals("cancel")) {
            selectedSlot.remove(playerId);
            player.getPageManager().setPage(ref, store, Page.None);
        }
    }

    private void refresh(Player player, Ref<EntityStore> ref, Store<EntityStore> store) {
        player.getPageManager().openCustomPage(ref, store, new ChestPage(playerRef, chestPos));
    }

    private static final Map<String, ItemContainer> extractionStashes = new HashMap<>();

    public static ItemContainer getOrCreateExtractionStash(String playerId) {
        return extractionStashes.computeIfAbsent(playerId, id -> new SimpleItemContainer((short) 1));
    }
}
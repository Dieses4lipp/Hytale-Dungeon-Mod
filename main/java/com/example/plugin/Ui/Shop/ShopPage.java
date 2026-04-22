package com.example.plugin.Ui.Shop;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

import com.example.plugin.Stats.PlayerLevelComponent;
import com.example.plugin.Stats.SellConfig;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;

public class ShopPage extends InteractiveCustomUIPage<ShopPage.Data> {

    public static class ShopItem {
        public String id;
        public int price;

        public ShopItem(String id, int price) {
            this.id = id;
            this.price = price;
        }
    }

    public static final ShopItem[] SHOP_INVENTORY = {
            // first row
            new ShopItem("Potion_Health_Small", 25),
            new ShopItem("Potion_Health_Greater", 50),
            new ShopItem("Potion_Health_Large", 100),
            new ShopItem("Weapong_Arrow_Crude", 10),
            new ShopItem("Weapong_Arrow_Iron", 10),

            // second row
            new ShopItem("Weapon_Battleaxe_Mithril", 5000),
            new ShopItem("Armor_Mithril_Head", 10000),
            new ShopItem("Armor_Mithril_Chest", 15000),
            new ShopItem("Armor_Mithril_Hands", 12000),
            new ShopItem("Armor_Mithril_Legs", 10000)

    };

    private static final Map<String, String> selectedItem = new HashMap<>();

    private static final Map<String, String> typedQuantity = new HashMap<>();

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .append(new KeyedCodec<>("@QuantityInput", Codec.STRING),
                        (data, value) -> data.quantity = value,
                        data -> data.quantity)
                .add()
                .append(new KeyedCodec<>("ButtonClicked", Codec.STRING),
                        (data, value) -> data.clickedButton = value,
                        data -> data.clickedButton)
                .add()
                .build();

        private String quantity;
        private String clickedButton;
    }

    private final PlayerRef playerRef;

    public ShopPage(PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.playerRef = playerRef;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder cmd,
            @Nonnull UIEventBuilder evt, @Nonnull Store<EntityStore> store) {

        cmd.append("Pages/ShopPage.ui");

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null)
            return;
        String playerId = player.getUuid().toString();
        String currentSelection = selectedItem.get(playerId);

        var stats = PlayerLevelComponent.getStats(store, ref);
        int currentGold = (stats != null) ? stats.gold : 0;

        cmd.set("#ShopTitle.Text", "MERCHANT SHOP");
        cmd.set("#GoldLabel.Text", "Gold: " + currentGold);

        String rememberedQuantity = typedQuantity.getOrDefault(playerId, "1");
        cmd.appendInline("#InputWrapper",
                "TextField #QuantityInput { Anchor: (Full: 0); Padding: (Full: 10); Value: \"" + rememberedQuantity
                        + "\"; }");

        evt.addEventBinding(CustomUIEventBindingType.ValueChanged, "#QuantityInput",
                EventData.of("@QuantityInput", "#QuantityInput.Value"), false);

        evt.addEventBinding(CustomUIEventBindingType.Activating, "#BuyBtn",
                EventData.of("ButtonClicked", "buy"), false);

        evt.addEventBinding(CustomUIEventBindingType.Activating, "#CloseBtn",
                EventData.of("ButtonClicked", "close"), false);

        for (int i = 0; i < SHOP_INVENTORY.length && i < 10; i++) {
            String groupId = "#ShopSlot" + (i + 1);
            String btnId = "ShopBtn" + i;
            ShopItem item = SHOP_INVENTORY[i];

            cmd.appendInline(groupId,
                    "ItemSlot { ItemId: \"" + item.id + "\"; Anchor: (Full: 0); ShowQuantity: true; }");
            cmd.appendInline(groupId,
                    "TextButton #" + btnId
                            + " { Anchor: (Full: 0); Text: \"\"; Background: #000000(0.0); TooltipText: \"Price: "
                            + item.price + " Gold\"; Style: (Hovered: (Background: #254a7588)); }");

            evt.addEventBinding(CustomUIEventBindingType.Activating, "#" + btnId,
                    EventData.of("ButtonClicked", "select_" + item.id), false);

            if (currentSelection != null && currentSelection.equals(item.id)) {
                cmd.appendInline(groupId, "Group { Anchor: (Full: 0); Background: #f5c518(0.1); }");
                cmd.appendInline(groupId,
                        "Group { Anchor: (Top: 0, Left: 0, Right: 0, Height: 2); Background: #f5c518; }");
                cmd.appendInline(groupId,
                        "Group { Anchor: (Bottom: 0, Left: 0, Right: 0, Height: 2); Background: #f5c518; }");
                cmd.appendInline(groupId,
                        "Group { Anchor: (Left: 0, Top: 0, Bottom: 0, Width: 2); Background: #f5c518; }");
                cmd.appendInline(groupId,
                        "Group { Anchor: (Right: 0, Top: 0, Bottom: 0, Width: 2); Background: #f5c518; }");
            }
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, Data data) {
        super.handleDataEvent(ref, store, data);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null)
            return;
        String playerId = player.getUuid().toString();

        if (data.quantity != null) {
            typedQuantity.put(playerId, data.quantity);
        }

        if (data.clickedButton == null)
            return;

        String action = data.clickedButton;
        data.clickedButton = null;

        if (action.startsWith("select_")) {
            String targetId = action.replace("select_", "");
            selectedItem.put(playerId, targetId);
            refresh(player, ref, store);
            return;
        }

        String finalQuantityStr = typedQuantity.getOrDefault(playerId, "1");
        int amount = 1;
        try {
            amount = Integer.parseInt(finalQuantityStr);
            if (amount <= 0)
                amount = 1;
        } catch (NumberFormatException e) {
            amount = 1;
        }

        if (action.equals("buy")) {
            String selectedId = selectedItem.get(playerId);
            if (selectedId == null) {
                int soundIndex = SoundEvent.getAssetMap().getIndex("sfx_creative_play_error");
                    SoundUtil.playSoundEvent2d(ref, soundIndex, SoundCategory.SFX, store);
                return;
            }

            int price = 0;
            for (ShopItem si : SHOP_INVENTORY) {
                if (si.id.equals(selectedId)) {
                    price = si.price;
                    break;
                }
            }

            int totalCost = price * amount;
            var stats = PlayerLevelComponent.getStats(store, ref);

            if (stats == null || stats.gold < totalCost) {
                Message rawMessage = Message.raw("You don't have enough gold! You need " + totalCost + "g to buy "
                        + amount + "x " + selectedId + ".");
                player.getPlayerRef().sendMessage(rawMessage);
                int soundIndex = SoundEvent.getAssetMap().getIndex("sfx_creative_play_error");
                    SoundUtil.playSoundEvent2d(ref, soundIndex, SoundCategory.SFX, store);
                return;
            }

            SellConfig.addGoldToPlayer(player, ref, store, -totalCost);

            ItemContainer inventory = player.getInventory().getStorage();
            short freeSlot = findFreeSlot(inventory);

            if (freeSlot != -1) {
                try {
                    ItemStack purchasedItem = new ItemStack(selectedId, amount);
                    inventory.setItemStackForSlot(freeSlot, purchasedItem);

                    int soundIndex = SoundEvent.getAssetMap().getIndex("sfx_coins_land");
                    SoundUtil.playSoundEvent2d(ref, soundIndex, SoundCategory.SFX, store);

                } catch (Exception e) {
                    System.err.println("[Shop] Error giving item: " + e.getMessage());
                }
            } else {
                Message rawMessage = Message.raw("Your inventory is full! Please free up some space and try again.");
                player.getPlayerRef().sendMessage(rawMessage);
                SellConfig.addGoldToPlayer(player, ref, store, totalCost);
                int soundIndex = SoundEvent.getAssetMap().getIndex("sfx_creative_play_error");
                SoundUtil.playSoundEvent2d(ref, soundIndex, SoundCategory.SFX, store);
            }

            refresh(player, ref, store);
            return;
        }

        if (action.equals("close")) {
            selectedItem.remove(playerId);
            typedQuantity.remove(playerId);
            player.getPageManager().setPage(ref, store, Page.None);
        }
    }

    private short findFreeSlot(ItemContainer container) {
        for (short s = 0; s < container.getCapacity(); s++) {
            try {
                ItemStack existing = container.getItemStack(s);
                if (existing == null || ItemStack.isEmpty(existing)) {
                    return s;
                }
            } catch (Exception ignored) {
            }
        }
        return -1;
    }

    private void refresh(Player player, Ref<EntityStore> ref, Store<EntityStore> store) {
        player.getPageManager().openCustomPage(ref, store, new ShopPage(this.playerRef));
    }
}
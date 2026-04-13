package com.example.plugin.Stats;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.example.plugin.DatabaseManager;
import com.example.plugin.DungeonGeneration.DungeonTables;
import com.example.plugin.Ui.PlayPage.InventoryPage;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class SellConfig {

    public static int calculateStashSellValue(String playerId, Player player) {
        int total = 0;

        System.out.println("[SellConfig] Scanning vanilla inventory for player: " + playerId);

        ItemContainer[] containers = {
                player.getInventory().getHotbar(),
                player.getInventory().getStorage()
        };

        for (ItemContainer container : containers) {
            for (short i = 0; i < container.getCapacity(); i++) {
                ItemStack item = container.getItemStack(i);
                if (item == null || ItemStack.isEmpty(item))
                    continue;

                String itemId = item.getItem().getId();
                int quantity = item.getQuantity();
                int price = DungeonTables.get().getSellValue(itemId);

                if (price > 0) {
                    int lineTotal = price * quantity;
                    total += lineTotal;
                    System.out.println("[SellConfig] SELLABLE  slot=" + i
                            + "  item=" + itemId
                            + "  qty=" + quantity
                            + "  price=" + price + "g each"
                            + "  subtotal=" + lineTotal + "g");
                } else {
                    System.out.println("[SellConfig] NOT SELLABLE  slot=" + i
                            + "  item=" + itemId);
                }
            }
        }

        System.out.println("[SellConfig] Grand total sell value: " + total + "g");
        return total;
    }

    public static void removeSellableItemsFromVanillaInventory(Player player) {
        ItemContainer[] containers = {
                player.getInventory().getHotbar(),
                player.getInventory().getStorage()
        };

        for (ItemContainer container : containers) {
            for (short i = 0; i < container.getCapacity(); i++) {
                ItemStack item = container.getItemStack(i);
                if (item == null || ItemStack.isEmpty(item))
                    continue;

                if (DungeonTables.get().getSellValue(item.getItem().getId()) > 0) {
                    container.setItemStackForSlot(i, null);
                }
            }
        }
    }
    public static int getItemSellValue(ItemStack item) {
        if (item == null || ItemStack.isEmpty(item)) {
            return 0;
        }
        String itemId = item.getItem().getId();
        return DungeonTables.get().getSellValue(itemId);
    }
    public static void addGoldToPlayer(Player player, Ref<EntityStore> ref,
            Store<EntityStore> store, int amount) {
        if (amount <= 0)
            return;

        PlayerLevelComponent stats = store.getComponent(ref, PlayerLevelComponent.getComponentType());
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

        if (stats == null || playerRef == null)
            return;

        stats.gold += amount;

        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(
                "UPDATE player_levels SET gold = ? WHERE uuid = ?")) {
            pstmt.setInt(1, stats.gold);
            pstmt.setString(2, playerRef.getUuid().toString());
            pstmt.executeUpdate();
            System.out.println("[SellConfig] Saved " + amount + "g to player "
                    + playerRef.getUuid() + " (total: " + stats.gold + "g)");
        } catch (SQLException e) {
            System.err.println("[SellConfig] Failed to save gold: " + e.getMessage());
        }
    }
}
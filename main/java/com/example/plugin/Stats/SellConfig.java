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

    public static void saveStashToDatabase(String playerId, ItemContainer stash) {
        if (stash == null)
            return;

        try (java.sql.PreparedStatement deleteStmt = DatabaseManager.getConnection().prepareStatement(
                "DELETE FROM player_stash WHERE uuid = ?")) {
            deleteStmt.setString(1, playerId);
            deleteStmt.executeUpdate();
        } catch (java.sql.SQLException e) {
            System.err.println("[Database] Failed to clear old stash: " + e.getMessage());
        }

        try (java.sql.PreparedStatement insertStmt = DatabaseManager.getConnection().prepareStatement(
                "INSERT OR REPLACE INTO player_stash (uuid, slot, item_id, quantity) VALUES (?, ?, ?, ?)")) {

            for (short i = 0; i < stash.getCapacity(); i++) {
                ItemStack item = stash.getItemStack(i);
                if (item != null && !ItemStack.isEmpty(item)) {
                    insertStmt.setString(1, playerId);
                    insertStmt.setInt(2, i);
                    insertStmt.setString(3, item.getItem().getId());
                    insertStmt.setInt(4, item.getQuantity());
                    insertStmt.addBatch();
                }
            }
            insertStmt.executeBatch();
        } catch (java.sql.SQLException e) {
            System.err.println("[Database] Failed to save stash: " + e.getMessage());
        }
    }

    public static void loadStashFromDatabase(String playerId, ItemContainer emptyStash) {
        try (java.sql.PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(
                "SELECT slot, item_id, quantity FROM player_stash WHERE uuid = ?")) {

            pstmt.setString(1, playerId);
            java.sql.ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                short slot = rs.getShort("slot");
                String itemId = rs.getString("item_id");
                int quantity = rs.getInt("quantity");

                try {
                    ItemStack loadedItem = new ItemStack(itemId, quantity);
                    emptyStash.setItemStackForSlot(slot, loadedItem);
                } catch (Exception ex) {
                    System.err.println("[Database] Could not create item: " + itemId);
                }
            }
        } catch (Exception e) {
            System.err.println("[Database] Failed to load stash: " + e.getMessage());
        }
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
                "UPDATE player_info SET gold = ? WHERE uuid = ?")) {
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
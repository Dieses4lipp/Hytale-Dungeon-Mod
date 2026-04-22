package com.example.plugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static Connection connection;

    public static void initialize(File pluginFolder) {
        try {
            if (!pluginFolder.exists()) {
                pluginFolder.mkdirs();
            }

            Class.forName("org.sqlite.JDBC");

            String url = "jdbc:sqlite:" + pluginFolder.getAbsolutePath() + "/plugin_data.db";
            connection = DriverManager.getConnection(url);

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS player_info (" +
                        "uuid TEXT PRIMARY KEY, " +
                        "level INTEGER DEFAULT 1, " +
                        "xp INTEGER DEFAULT 0, " +
                        "gold INTEGER DEFAULT 0, " +
                        "can_build INTEGER DEFAULT 0)");
            }
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS player_stash (" +
                        "uuid TEXT, " +
                        "slot INTEGER, " +
                        "item_id TEXT, " +
                        "quantity INTEGER, " +
                        "PRIMARY KEY (uuid, slot))");
            }
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS active_dungeons (slot INTEGER PRIMARY KEY)");
            }
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS hub_npcs (uuid TEXT PRIMARY KEY)");
            }

            System.out.println("[Database] SQLite Tables verified.");

        } catch (Exception e) {
            System.err.println("[Database] Critical Error during init: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        if (connection == null) {
            initialize(new File("plugins/HytaleDungeonMod"));
        }
        return connection;
    }

    public static void setBuildPermission(String uuid, boolean canBuild) {
        try (PreparedStatement pstmt = getConnection().prepareStatement(
                "UPDATE player_info SET can_build = ? WHERE uuid = ?")) {
            pstmt.setInt(1, canBuild ? 1 : 0);
            pstmt.setString(2, uuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[Database] Fehler beim Updaten der Baurechte: " + e.getMessage());
        }
    }

    // =========================================================================
    // DUNGEON TRACKING
    // =========================================================================

    public static void addActiveDungeon(int slot) {
        try (PreparedStatement pstmt = getConnection().prepareStatement(
                "INSERT OR IGNORE INTO active_dungeons (slot) VALUES (?)")) {
            pstmt.setInt(1, slot);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[Database] Error adding active dungeon: " + e.getMessage());
        }
    }

    public static void removeActiveDungeon(int slot) {
        try (PreparedStatement pstmt = getConnection().prepareStatement(
                "DELETE FROM active_dungeons WHERE slot = ?")) {
            pstmt.setInt(1, slot);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[Database] Error removing active dungeon: " + e.getMessage());
        }
    }

    public static List<Integer> getActiveDungeons() {
        List<Integer> slots = new ArrayList<>();
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT slot FROM active_dungeons")) {
            while (rs.next()) {
                slots.add(rs.getInt("slot"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return slots;
    }

    public static void clearAllActiveDungeons() {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute("DELETE FROM active_dungeons");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =========================================================================
    // HUB NPC TRACKING
    // =========================================================================

    public static void saveHubNpcUuid(String uuid) {
        try (PreparedStatement pstmt = getConnection().prepareStatement(
                "INSERT OR IGNORE INTO hub_npcs (uuid) VALUES (?)")) {
            pstmt.setString(1, uuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[Database] Error saving hub NPC UUID: " + e.getMessage());
        }
    }

    public static List<String> getHubNpcUuids() {
        List<String> uuids = new ArrayList<>();
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT uuid FROM hub_npcs")) {
            while (rs.next()) {
                uuids.add(rs.getString("uuid"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return uuids;
    }

    public static void clearHubNpcUuids() {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute("DELETE FROM hub_npcs");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
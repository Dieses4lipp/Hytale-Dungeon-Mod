package com.example.plugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;

public class DatabaseManager {
    private static Connection connection;

    public static void initialize(File pluginFolder) {
        try {
            if (!pluginFolder.exists()) {
                pluginFolder.mkdirs();
            }

            // Load the SQLite Driver explicitly to be safe
            Class.forName("org.sqlite.JDBC");

            String url = "jdbc:sqlite:" + pluginFolder.getAbsolutePath() + "/plugin_data.db";

            // ASSIGN TO STATIC VARIABLE
            connection = DriverManager.getConnection(url);

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS player_levels (" +
                        "uuid TEXT PRIMARY KEY, " +
                        "level INTEGER DEFAULT 1, " +
                        "xp INTEGER DEFAULT 0, " +
                        "gold INTEGER DEFAULT 0)");
                System.out.println("[Database] SQLite Tables verified.");
            }
            try (java.sql.Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS player_stash (" +
                        "uuid TEXT, " +
                        "slot INTEGER, " +
                        "item_id TEXT, " +
                        "quantity INTEGER, " +
                        "PRIMARY KEY (uuid, slot))");
            }
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
}
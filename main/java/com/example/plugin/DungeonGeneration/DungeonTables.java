package com.example.plugin.DungeonGeneration;

import com.example.plugin.DungeonGeneration.DungeonTables.LootEntry;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DungeonTables {
    private static DungeonTables instance;

    public static DungeonTables get() {
        return instance;
    }

    public static class MobStats {
        public int weight;
        public int xp;
    }

    public static class LootEntry {
        public int weight;
        public int Quantity;
    }
    public static class BossRoomData {
        public Map<String, MobStats> bosses = new HashMap<>();
        public Map<String, MobStats> minions = new HashMap<>();
    }
    public static class MobsData {
        public Map<String, MobStats> default_room = new HashMap<>();
        public BossRoomData boss_room = new BossRoomData(); 
    }
    public MobsData mobs = new MobsData();
    public Map<String, Map<String, LootEntry>> loot = new HashMap<>();

    @SerializedName("sell_values")
    public Map<String, Integer> sellValues = new HashMap<>();

    public static DungeonTables load(InputStream stream) {
        instance = new Gson().fromJson(new InputStreamReader(stream), DungeonTables.class);
        return instance;
    }

    public int getSellValue(String itemId) {
        return this.sellValues.getOrDefault(itemId, 0);
    }
    public Map.Entry<String, LootEntry> getRandomLootEntry(Map<String, LootEntry> table) {
        if (table == null || table.isEmpty())
            return null;
        int totalWeight = 0;
        for (LootEntry entry : table.values())
            totalWeight += entry.weight;
        int randomVal = new Random().nextInt(totalWeight);
        for (Map.Entry<String, LootEntry> entry : table.entrySet()) {
            randomVal -= entry.getValue().weight;
            if (randomVal < 0)
                return entry;
        }
        return null;
    }

    public String getRandomMob(Map<String, MobStats> table) {
        if (table == null || table.isEmpty())
            return null;

        int totalWeight = 0;
        for (MobStats stats : table.values()) {
            totalWeight += stats.weight;
        }

        int randomVal = new Random().nextInt(totalWeight);
        for (Map.Entry<String, MobStats> entry : table.entrySet()) {
            randomVal -= entry.getValue().weight;
            if (randomVal < 0) {
                return entry.getKey();
            }
        }
        return null;
    }

    public String getRandomFromTable(Map<String, Integer> table) {
        if (table == null || table.isEmpty())
            return null;

        int totalWeight = 0;
        for (int weight : table.values()) {
            totalWeight += weight;
        }

        int randomVal = new Random().nextInt(totalWeight);
        for (Map.Entry<String, Integer> entry : table.entrySet()) {
            randomVal -= entry.getValue();
            if (randomVal < 0) {
                return entry.getKey();
            }
        }
        return null;
    }
}
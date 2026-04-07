package com.example.plugin.DungeonGeneration;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DungeonTables {
private static DungeonTables instance;
    public static DungeonTables get() { return instance; }

    public static class MobStats {
        public int weight;
        public int xp;
    }

    public Map<String, Map<String, MobStats>> mobs = new HashMap<>();
    
    public Map<String, Map<String, Integer>> loot = new HashMap<>();

    public static DungeonTables load(InputStream stream) {
        instance = new Gson().fromJson(new InputStreamReader(stream), DungeonTables.class);
        return instance;
    }

    public String getRandomMob(Map<String, MobStats> table) {
        if (table == null || table.isEmpty()) return null;

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
        if (table == null || table.isEmpty()) return null;

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
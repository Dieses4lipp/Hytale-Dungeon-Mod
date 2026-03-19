
package com.example.plugin.DungeonGeneration;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.io.InputStream;

public class DungeonConfig {

    private static DungeonConfig instance;
    public static DungeonConfig get() { return instance; }

    public Layout    layout    = new Layout();
    public Manager   manager   = new Manager();
    public Generator generator = new Generator();

    public static class Layout {
        public int    gridsize       = 20;
        public double treasureChance = 0.10;
        public double hallwayChance  = 0.20;
        public double shopChance     = 0.50; 
        public double stashChance    = 0.10;
    }

    public static class Manager {
        public int spacing    = 11;
        public int slotsPerRow = 10;
    }

    public static class Generator {
        public int baseY      = 90;
        public int bossYOffset = -2;
        public int clearYMin  = 85;
        public int clearYMax  = 115;
        public int doorY      = 91;
    }

    public static DungeonConfig load(InputStream stream) {
        instance = new Gson().fromJson(new InputStreamReader(stream), DungeonConfig.class);
        return instance;
    }
}

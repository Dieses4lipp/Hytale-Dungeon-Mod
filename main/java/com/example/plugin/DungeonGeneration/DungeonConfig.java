
package com.example.plugin.DungeonGeneration;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.io.InputStream;

public class DungeonConfig {

    private static DungeonConfig instance;
    public static DungeonConfig get() { return instance; }

    // Layout
    public int gridsize          = 20;
    public int spacing           = 11;
    public int slotsPerRow       = 10;

    // Room type chances
    public double treasureChance = 0.10;
    public double hallwayChance  = 0.20;

    // Heights
    public int baseY             = 90;
    public int clearYMin         = 85;
    public int clearYMax         = 115;
    public int doorY             = 91;

    // Boss room
    public int bossRoomSize      = 3;    // 3x3

    public static DungeonConfig load(InputStream stream) {
        instance = new Gson().fromJson(new InputStreamReader(stream), DungeonConfig.class);
        return instance;
    }
}

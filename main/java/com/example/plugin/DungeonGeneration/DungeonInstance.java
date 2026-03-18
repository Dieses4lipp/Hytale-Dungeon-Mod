package com.example.plugin.DungeonGeneration;

public class DungeonInstance {
    public final int slot;
    public final Room[][] grid;
    public final int worldOriginX;
    public final int worldOriginZ;
    public final int abstand;
    public final int startX;
    public final int startY;

    public DungeonInstance(int slot, Room[][] grid, int worldOriginX, int worldOriginZ,
                           int abstand, int startX, int startY) {
        this.slot = slot;
        this.grid = grid;
        this.worldOriginX = worldOriginX;
        this.worldOriginZ = worldOriginZ;
        this.abstand = abstand;
        this.startX = startX;
        this.startY = startY;
    }
}
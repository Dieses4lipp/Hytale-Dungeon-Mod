package com.example.plugin.DungeonGeneration;

public class DungeonInstance {
    public final int slot;
    public final Room[][] grid;
    public final int worldOriginX;
    public final int worldOriginZ;
    public final int spacing;
    public final int startX;
    public final int startY;

    public DungeonInstance(int slot, Room[][] grid, int worldOriginX, int worldOriginZ,
                           int spacing, int startX, int startY) {
        this.slot = slot;
        this.grid = grid;
        this.worldOriginX = worldOriginX;
        this.worldOriginZ = worldOriginZ;
        this.spacing = spacing;
        this.startX = startX;
        this.startY = startY;
    }
}
package com.example.plugin.model;

import java.util.List;

public class Room {
    private String templateId;
    private int minX;
    private int minY;
    private int minZ;
    private int maxX;
    private int maxY;
    private int maxZ;
    private List<Exit> exits;
    private List<BlockData> blocks;

    public Room() {}

    public Room(String templateId, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, List<Exit> exits, List<BlockData> blocks) {
        this.templateId = templateId;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.exits = exits;
        this.blocks = blocks;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public int getMinX() {
        return minX;
    }

    public void setMinX(int minX) {
        this.minX = minX;
    }

    public int getMinY() {
        return minY;
    }

    public void setMinY(int minY) {
        this.minY = minY;
    }

    public int getMinZ() {
        return minZ;
    }

    public void setMinZ(int minZ) {
        this.minZ = minZ;
    }

    public int getMaxX() {
        return maxX;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public void setMaxZ(int maxZ) {
        this.maxZ = maxZ;
    }

    public int getWidth() {
        return maxX - minX;
    }

    public int getHeight() {
        return maxY - minY;
    }

    public int getDepth() {
        return maxZ - minZ;
    }

    public List<Exit> getExits() {
        return exits;
    }

    public void setExits(List<Exit> exits) {
        this.exits = exits;
    }

    public List<BlockData> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<BlockData> blocks) {
        this.blocks = blocks;
    }

    public static class Exit {
        private int x;
        private int y;
        private int z;
        private String direction;

        public Exit() {}

        public Exit(int x, int y, int z, String direction) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.direction = direction;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getZ() {
            return z;
        }

        public void setZ(int z) {
            this.z = z;
        }

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }
    }
}

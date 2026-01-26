package com.example.plugin.generator;

import com.example.plugin.model.BlockData;
import com.example.plugin.model.Room;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.server.core.universe.world.World;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BlockCapture {
    private final World world;

    public BlockCapture(World world) {
        this.world = world;
    }

    /**
     * Captures all blocks in a cuboid region defined by two corners
     */
    public List<BlockData> captureRegion(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        List<BlockData> blocks = new ArrayList<>();

        // Ensure proper ordering
        int x1 = Math.min(minX, maxX);
        int x2 = Math.max(minX, maxX);
        int y1 = Math.min(minY, maxY);
        int y2 = Math.max(minY, maxY);
        int z1 = Math.min(minZ, maxZ);
        int z2 = Math.max(minZ, maxZ);

        System.out.println("Capturing blocks from (" + x1 + ", " + y1 + ", " + z1 + ") to (" + x2 + ", " + y2 + ", " + z2 + ")");

        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    try {
                        int blockId = world.getBlock(x, y, z);
                        
                        // Skip air blocks (blockId 0) to reduce file size
                        if (blockId != 0) {
                            String blockType = null;
                            
                            // Try to get block name using getBlockType
                            try {
                                Object blockState = world.getBlockType(x, y, z);
                                if (blockState != null) {
                                    String blockStateStr = blockState.toString();
                                    
                                    // Extract id= value from BlockType{id=..., ...}
                                    if (blockStateStr.contains("id=")) {
                                        int idStart = blockStateStr.indexOf("id=") + 3;
                                        int idEnd = blockStateStr.indexOf(",", idStart);
                                        if (idEnd == -1) {
                                            idEnd = blockStateStr.indexOf("}", idStart);
                                        }
                                        if (idEnd > idStart) {
                                            blockType = blockStateStr.substring(idStart, idEnd).trim();
                                            System.out.println("[DEBUG] Block at (" + x + "," + y + "," + z + "): " + blockType);
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                System.out.println("[DEBUG] getBlockType failed: " + ex.getMessage());
                            }
                            
                            if (blockType == null || blockType.isEmpty()) {
                                blockType = "block_" + blockId;
                                System.out.println("[DEBUG] Using fallback name: " + blockType);
                            }
                            
                            // Store relative coordinates from minX, minY, minZ
                            BlockData block = new BlockData(x - x1, y - y1, z - z1, blockType);
                            blocks.add(block);
                        }
                    } catch (Exception e) {
                        System.err.println("Error reading block at (" + x + ", " + y + ", " + z + "): " + e.getMessage());
                    }
                }
            }
        }

        System.out.println("Captured " + blocks.size() + " non-air blocks");
        return blocks;
    }

    /**
     * Creates a Room object from captured blocks
     */
    public Room createRoomFromCapture(String templateId, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, List<Room.Exit> exits) {
        List<BlockData> blocks = captureRegion(minX, minY, minZ, maxX, maxY, maxZ);
        return new Room(templateId, minX, minY, minZ, maxX, maxY, maxZ, exits, blocks);
    }

    /**
     * Saves a room configuration to JSON file
     */
    public void saveRoomToJson(Room room, Path outputPath) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(outputPath.toFile())) {
            gson.toJson(room, writer);
        }
        System.out.println("Room saved to: " + outputPath);
    }

    /**
     * Saves multiple rooms to JSON file
     */
    public void saveRoomsToJson(List<Room> rooms, Path outputPath) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(outputPath.toFile())) {
            gson.toJson(rooms, writer);
        }
        System.out.println("Rooms saved to: " + outputPath);
    }
}

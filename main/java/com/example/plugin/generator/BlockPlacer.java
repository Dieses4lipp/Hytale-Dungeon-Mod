package com.example.plugin.generator;

import com.example.plugin.model.BlockData;
import com.hypixel.hytale.server.core.universe.world.World;

import java.util.List;

public class BlockPlacer {
    private final World world;

    public BlockPlacer(World world) {
        this.world = world;
    }

    /**
     * Places a room using pre-captured block data
     */
    public void placeRoomFromData(DungeonGenerator.PlacedRoom room, List<BlockData> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            System.out.println("No block data for room: " + room.getTemplateId());
            return;
        }

        int offsetX = room.getPosX();
        int offsetY = room.getPosY();
        int offsetZ = room.getPosZ();

        System.out.println("Placing " + blocks.size() + " blocks for " + room.getTemplateId() + 
                         " at offset (" + offsetX + ", " + offsetY + ", " + offsetZ + ")");

        int successCount = 0;
        int failCount = 0;
        
        for (BlockData block : blocks) {
            try {
                int worldX = offsetX + block.getX();
                int worldY = offsetY + block.getY();
                int worldZ = offsetZ + block.getZ();
                
                String blockType = block.getBlockType();
                System.out.println("[PLACE] Setting block " + blockType + " at (" + worldX + ", " + worldY + ", " + worldZ + ")");
                
                // Place the block using the stored block name directly
                world.setBlock(worldX, worldY, worldZ, blockType);
                successCount++;
                
            } catch (Exception e) {
                failCount++;
                System.err.println("[FAILED] Error placing block: " + e.getMessage() + " | Type: " + block.getBlockType());
            }
        }
        
        System.out.println("Placement result: " + successCount + " successful, " + failCount + " failed");
    }


}

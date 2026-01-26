package com.example.plugin.generator;

import com.example.plugin.model.BlockData;
import com.example.plugin.model.Room;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hypixel.hytale.server.core.universe.world.World;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DungeonGenerator {
    private final List<Room> rooms;
    private int currentX = 0;
    private int currentY = 0;
    private int currentZ = 0;

    public DungeonGenerator(Path configPath) throws IOException {
        this.rooms = loadRoomsFromJson(configPath);
    }

    /**
     * Loads room definitions from a JSON file
     */
    private List<Room> loadRoomsFromJson(Path configPath) throws IOException {
        try (FileReader reader = new FileReader(configPath.toFile())) {
            Gson gson = new Gson();
            Type roomListType = new TypeToken<List<Room>>(){}.getType();
            return gson.fromJson(reader, roomListType);
        }
    }

    /**
     * Generates a dungeon layout from the loaded room templates
     */
    public DungeonLayout generateDungeon() {
        DungeonLayout layout = new DungeonLayout();

        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            int width = room.getWidth();
            int height = room.getHeight();
            int depth = room.getDepth();
            
            PlacedRoom placedRoom = new PlacedRoom(
                    room.getTemplateId(),
                    currentX,
                    currentY,
                    currentZ,
                    width,
                    height,
                    depth,
                    room.getExits(),
                    room.getBlocks()
            );
            layout.addRoom(placedRoom);

            // Move to the next room position based on the first exit
            if (!room.getExits().isEmpty()) {
                Room.Exit exit = room.getExits().get(0);
                currentX += exit.getX() + width;
                currentY = exit.getY();
                currentZ += exit.getZ();
            }
        }

        return layout;
    }

    /**
     * Builds the dungeon in the world using the generated layout and captured block data
     */
    public void buildDungeon(DungeonLayout layout, World world) {
        BlockPlacer placer = new BlockPlacer(world);

        for (PlacedRoom room : layout.getPlacedRooms()) {
            // Place the room's captured blocks
            if (room.getBlocks() != null && !room.getBlocks().isEmpty()) {
                placer.placeRoomFromData(room, room.getBlocks());
            }

            // Create doorways at exits
            if (room.getExits() != null && !room.getExits().isEmpty()) {
                for (Room.Exit exit : room.getExits()) {
                    placer.createDoorway(room, exit.getDirection());
                }
            }
        }
    }

    /**
     * Represents a room placed in the dungeon at specific coordinates
     */
    public static class PlacedRoom {
        private final String templateId;
        private final int posX;
        private final int posY;
        private final int posZ;
        private final int width;
        private final int height;
        private final int depth;
        private final List<Room.Exit> exits;
        private final List<BlockData> blocks;

        public PlacedRoom(String templateId, int posX, int posY, int posZ, int width, int height, int depth, List<Room.Exit> exits, List<BlockData> blocks) {
            this.templateId = templateId;
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            this.width = width;
            this.height = height;
            this.depth = depth;
            this.exits = exits;
            this.blocks = blocks;
        }

        public String getTemplateId() {
            return templateId;
        }

        public int getPosX() {
            return posX;
        }

        public int getPosY() {
            return posY;
        }

        public int getPosZ() {
            return posZ;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int getDepth() {
            return depth;
        }

        public List<Room.Exit> getExits() {
            return exits;
        }

        public List<BlockData> getBlocks() {
            return blocks;
        }
    }

    /**
     * Represents the complete dungeon layout
     */
    public static class DungeonLayout {
        private final List<PlacedRoom> placedRooms = new ArrayList<>();

        public void addRoom(PlacedRoom room) {
            placedRooms.add(room);
        }

        public List<PlacedRoom> getPlacedRooms() {
            return placedRooms;
        }

        public String getSummary() {
            StringBuilder sb = new StringBuilder();
            sb.append("Dungeon Layout:\n");
            for (PlacedRoom room : placedRooms) {
                sb.append(String.format("  - %s at (%d, %d, %d) [%dx%dx%d]\n",
                        room.getTemplateId(),
                        room.getPosX(),
                        room.getPosY(),
                        room.getPosZ(),
                        room.getWidth(),
                        room.getHeight(),
                        room.getDepth()
                ));
            }
            return sb.toString();
        }
    }
}

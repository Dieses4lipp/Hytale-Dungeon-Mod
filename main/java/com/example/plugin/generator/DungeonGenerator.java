package com.example.plugin.generator;

import com.example.plugin.model.BlockData;
import com.example.plugin.model.Room;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hypixel.hytale.server.core.universe.world.World;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class DungeonGenerator {
    private final List<Room> rooms;
    private int currentX = 31;
    private int currentY = 136;
    private int currentZ = 4;

    public DungeonGenerator(Path configPath) throws IOException {
        this.rooms = loadRoomsFromJson(configPath);
    }

    /**
     * Loads room definitions from individual JSON files in a directory
     */
    private List<Room> loadRoomsFromJson(Path configPath) throws IOException {
        List<Room> allRooms = new ArrayList<>();
        
        // If configPath is a file, try the parent directory as rooms directory
        Path roomsDir = configPath;
        if (Files.isRegularFile(configPath)) {
            roomsDir = configPath.getParent();
        }
        
        // Look for rooms directory
        if (!Files.exists(roomsDir)) {
            throw new IOException("Rooms directory not found: " + roomsDir);
        }
        
        // Load all JSON files from the rooms directory
        try (Stream<Path> paths = Files.list(roomsDir)) {
            paths.filter(p -> p.toString().endsWith(".json"))
                 .sorted()
                 .forEach(roomFile -> {
                     try (FileReader reader = new FileReader(roomFile.toFile())) {
                         Gson gson = new Gson();
                         Room room = gson.fromJson(reader, Room.class);
                         if (room != null) {
                             allRooms.add(room);
                         }
                     } catch (IOException e) {
                         System.err.println("Error loading room file: " + roomFile);
                         e.printStackTrace();
                     }
                 });
        }
        
        return allRooms;
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
                String direction = exit.getDirection();
                
                // Calculate next room position based on direction
                switch (direction != null ? direction : "NORTH") {
                    case "NORTH":
                        // NORTH: decrease Z (move away from positive Z)
                        currentZ -= depth + exit.getZ() + 1;
                        break;
                    case "SOUTH":
                        // SOUTH: increase Z (move toward positive Z)
                        currentZ += depth + exit.getZ() + 1;
                        break;
                    case "EAST":
                        // EAST: increase X (move toward positive X)
                        currentX += width + exit.getX() + 1;
                        break;
                    case "WEST":
                        // WEST: decrease X (move away from positive X)
                        currentX -= width + exit.getX() + 1;
                        break;
                }
                
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

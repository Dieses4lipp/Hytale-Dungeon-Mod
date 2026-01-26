package com.example.plugin;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import com.example.plugin.generator.DungeonGenerator;
import com.example.plugin.generator.BlockCapture;
import com.example.plugin.model.Room;
import com.example.plugin.model.BlockData;
import com.google.gson.Gson;

import javax.annotation.Nonnull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class HelloCommand extends AbstractPlayerCommand {

    public HelloCommand(@Nonnull String name, @Nonnull String description, boolean requiresConfirmation) {
        super(name, description, requiresConfirmation);
    }

    @Override
    protected void execute(
            @Nonnull CommandContext commandContext,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        try {
            // Build the dungeon with captured blocks
            buildDungeonFromJson(world, playerRef);
            
            // First time setup: capture blocks from world coordinates
            captureAndSaveBlocks(world, playerRef);

        } catch (Exception e) {
            EventTitleUtil.showEventTitleToPlayer(
                    playerRef,
                    Message.raw("Error"),
                    Message.raw("Error: " + e.getMessage()),
                    true
            );
            e.printStackTrace();
        }
    }

    private void buildDungeonFromJson(@Nonnull World world, @Nonnull PlayerRef playerRef) throws Exception {
        // Try multiple paths to find rooms directory
        String[] possiblePaths = {
            "Server/config/rooms",
            "config/rooms",
            "../Server/config/rooms",
            "/home/philipp/hytale-server/Server/config/rooms"
        };
        
        java.nio.file.Path roomsDir = null;
        for (String path : possiblePaths) {
            java.nio.file.Path p = java.nio.file.Paths.get(path);
            System.out.println("[DEBUG] Checking rooms directory: " + p.toAbsolutePath());
            if (java.nio.file.Files.exists(p) && java.nio.file.Files.isDirectory(p)) {
                roomsDir = p;
                System.out.println("[DEBUG] ✓ Found rooms directory at: " + roomsDir.toAbsolutePath());
                break;
            }
        }
        
        if (roomsDir == null) {
            System.out.println("[ERROR] Working directory: " + Paths.get("").toAbsolutePath());
            throw new Exception("rooms directory not found in any expected location");
        }
        
        // Load and generate dungeon from individual room files
        DungeonGenerator generator = new DungeonGenerator(roomsDir);
        DungeonGenerator.DungeonLayout layout = generator.generateDungeon();
        
        // Build the dungeon in the world
        generator.buildDungeon(layout, world);

        // Show success message to player
        EventTitleUtil.showEventTitleToPlayer(
                playerRef,
                Message.raw("Dungeon Built!"),
                Message.raw("Rooms: " + layout.getPlacedRooms().size()),
                true
        );

        System.out.println(layout.getSummary());
    }

    private void captureAndSaveBlocks(@Nonnull World world, @Nonnull PlayerRef playerRef) throws Exception {
        // Try multiple possible paths
        String[] possiblePaths = {
            "Server/config/rooms",
            "config/rooms",
            "../Server/config/rooms",
            "/home/philipp/hytale-server/Server/config/rooms"
        };
        
        Path roomsDir = null;
        for (String pathStr : possiblePaths) {
            Path p = Paths.get(pathStr);
            System.out.println("[DEBUG] Checking rooms directory: " + p.toAbsolutePath());
            if (Files.exists(p) && Files.isDirectory(p)) {
                roomsDir = p;
                System.out.println("[DEBUG] ✓ Found rooms directory at: " + roomsDir.toAbsolutePath());
                break;
            }
        }
        
        if (roomsDir == null) {
            System.out.println("[ERROR] Rooms directory not found in any location. Working directory: " + Paths.get("").toAbsolutePath());
            EventTitleUtil.showEventTitleToPlayer(
                    playerRef,
                    Message.raw("Error"),
                    Message.raw("rooms directory not found"),
                    true
            );
            return;
        }

        // List all JSON files in the directory and load them
        BlockCapture capture = new BlockCapture(world);
        
        try (java.util.stream.Stream<Path> paths = Files.list(roomsDir)) {
            paths.filter(p -> p.toString().endsWith(".json"))
                 .sorted()
                 .forEach(roomFile -> {
                     try {
                         String jsonContent = new String(Files.readAllBytes(roomFile));
                         Gson gson = new Gson();
                         Room room = gson.fromJson(jsonContent, Room.class);
                         
                         if (room != null) {
                             System.out.println("Capturing blocks for: " + room.getTemplateId());
                             
                             List<BlockData> blocks = capture.captureRegion(
                                     room.getMinX(), room.getMinY(), room.getMinZ(),
                                     room.getMaxX(), room.getMaxY(), room.getMaxZ()
                             );
                             
                             room.setBlocks(blocks);
                             
                             // Save updated room back to its own file
                             capture.saveRoomToJson(room, roomFile);
                         }
                     } catch (Exception e) {
                         System.err.println("Error processing room file: " + roomFile);
                         e.printStackTrace();
                     }
                 });
        }
        
        System.out.println("Blocks captured and saved to individual room files");
        
        EventTitleUtil.showEventTitleToPlayer(
                playerRef,
                Message.raw("Blocks Captured!"),
                Message.raw("Saved to room files"),
                true
        );
    }
}

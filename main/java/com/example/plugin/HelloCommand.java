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
import com.google.gson.reflect.TypeToken;

import javax.annotation.Nonnull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.reflect.Type;
import java.util.ArrayList;
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
        // Try multiple paths to find rooms.json
        String[] possiblePaths = {
            "Server/config/rooms.json",
            "config/rooms.json",
            "../Server/config/rooms.json",
            "/home/philipp/hytale-server/Server/config/rooms.json"
        };
        
        java.nio.file.Path roomsPath = null;
        for (String path : possiblePaths) {
            java.nio.file.Path p = java.nio.file.Paths.get(path);
            System.out.println("[DEBUG] Checking path: " + p.toAbsolutePath());
            if (java.nio.file.Files.exists(p)) {
                roomsPath = p;
                System.out.println("[DEBUG] ✓ Found rooms.json at: " + roomsPath.toAbsolutePath());
                break;
            }
        }
        
        if (roomsPath == null) {
            System.out.println("[ERROR] Working directory: " + Paths.get("").toAbsolutePath());
            throw new Exception("rooms.json not found in any expected location");
        }
        
        // Load and generate dungeon from JSON configuration
        DungeonGenerator generator = new DungeonGenerator(roomsPath);
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
            "Server/config/rooms.json",
            "config/rooms.json",
            "../Server/config/rooms.json",
            "/home/philipp/hytale-server/Server/config/rooms.json"
        };
        
        Path configPath = null;
        for (String pathStr : possiblePaths) {
            Path p = Paths.get(pathStr);
            System.out.println("[DEBUG] Checking path: " + p.toAbsolutePath());
            if (Files.exists(p)) {
                configPath = p;
                System.out.println("[DEBUG] ✓ Found rooms.json at: " + configPath.toAbsolutePath());
                break;
            }
        }
        
        if (configPath == null) {
            System.out.println("[ERROR] Config file not found in any location. Working directory: " + Paths.get("").toAbsolutePath());
            EventTitleUtil.showEventTitleToPlayer(
                    playerRef,
                    Message.raw("Error"),
                    Message.raw("rooms.json not found"),
                    true
            );
            return;
        }

        // Parse rooms from JSON
        String jsonContent = new String(Files.readAllBytes(configPath));
        Gson gson = new Gson();
        Type roomListType = new TypeToken<List<Room>>(){}.getType();
        List<Room> rooms = gson.fromJson(jsonContent, roomListType);

        if (rooms == null || rooms.isEmpty()) {
            System.out.println("No rooms found in config");
            return;
        }

        BlockCapture capture = new BlockCapture(world);
        List<Room> updatedRooms = new ArrayList<>();

        // Capture blocks for each room
        for (Room room : rooms) {
            System.out.println("Capturing blocks for: " + room.getTemplateId());
            
            List<BlockData> blocks = capture.captureRegion(
                    room.getMinX(), room.getMinY(), room.getMinZ(),
                    room.getMaxX(), room.getMaxY(), room.getMaxZ()
            );
            
            room.setBlocks(blocks);
            updatedRooms.add(room);
        }

        // Save updated rooms back to JSON
        String updatedJson = new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(updatedRooms);
        Files.write(configPath, updatedJson.getBytes());

        System.out.println("Blocks captured and saved to: " + configPath.toAbsolutePath());
        
        EventTitleUtil.showEventTitleToPlayer(
                playerRef,
                Message.raw("Blocks Captured!"),
                Message.raw("Saved to rooms.json"),
                true
        );
    }
}

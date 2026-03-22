package com.example.plugin.DungeonGeneration;

import java.nio.file.Path;

import com.hypixel.hytale.server.core.modules.entity.component.Invulnerable;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import it.unimi.dsi.fastutil.Pair;

import com.example.plugin.Npc.Testinteractionnpc.NPCSetupPending;
import com.example.plugin.doorsystem.DoorNPCComponent;
import com.example.plugin.doorsystem.DoorRegistry;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;

public class DungeonGenerator {

    private final int spacing = DungeonConfig.get().manager.spacing;
    private final int baseY = DungeonConfig.get().generator.baseY;
    private final int doorY = DungeonConfig.get().generator.doorY;
    private final int clearYMin = DungeonConfig.get().generator.clearYMin;
    private final int clearYMax = DungeonConfig.get().generator.clearYMax;

    private static final Path doorWEPath_closed = Path.of("prefabs/Prefabs/Door/door_we_closed.prefab.json");
    private static final Path doorSNPath_closed = Path.of("prefabs/Prefabs/Door/door_sn_closed.prefab.json");

    public void generate(World world, Room[][] grid, int originX, int originZ, Store<EntityStore> store) {
        DoorRegistry.clear();
        DoorRegistry.setWorld(world);
        int gridsize = grid.length;
        PrefabStore prefabStore = PrefabStore.get();

        for (int x = 0; x < gridsize; x++) {
            for (int y = 0; y < grid[x].length; y++) {
                Room room = grid[x][y];
                if (room == null)
                    continue;

                int worldX = originX + x * spacing;
                int worldZ = originZ + y * spacing;

                if (room.isSatellite()) {
                    placeDoors(world, prefabStore, store, room, worldX, worldZ);
                    continue;
                }
                placeRoom(world, prefabStore, room, worldX, worldZ);
                placeDoors(world, prefabStore, store, room, worldX, worldZ);
            }
        }
    }

    private void placeRoom(World world, PrefabStore prefabStore, Room room, int worldX, int worldZ) {
        Path prefabPath;

        if (room.getType() == RoomType.SHOP) {
            int dir = room.getSingleDoorDirection();
            prefabPath = room.getType().getPrefabPathForDirection(dir);
        } else {
            prefabPath = room.getType().getRandomPrefabPath();
        }

        BlockSelection prefab = prefabStore.getPrefab(prefabPath);
        world.setBlock(worldX, baseY - 1, worldZ, markerBlockFor(room.getType()));
        prefab.placeNoReturn(world, new Vector3i(worldX, baseY, worldZ), null);
    }

    private String markerBlockFor(RoomType type) {
        return switch (type) {
            case BOSS -> "Cloth_Block_Wool_Purple";
            case TREASURE -> "Cloth_Block_Wool_Yellow";
            case HALLWAY -> "Cloth_Block_Wool_Gray";
            case SHOP -> "Cloth_Block_Wool_Blue";
            case STASH -> "Cloth_Block_Wool_Orange";
            default -> "Cloth_Block_Wool_Green";
        };
    }

    private void placeDoors(World world, PrefabStore prefabStore, Store<EntityStore> store,
            Room room, int worldX, int worldZ) {
        boolean[] doors = room.getDoors();
        boolean isBossOrigin = room.getType() == RoomType.BOSS && !room.isSatellite();
        int offset = isBossOrigin ? (spacing + spacing / 2) : (spacing / 2);

        BlockSelection doorSN = prefabStore.getPrefab(doorSNPath_closed);
        BlockSelection doorWE = prefabStore.getPrefab(doorWEPath_closed);

        if (doors[0]) {
            Vector3i pos = new Vector3i(worldX, doorY, worldZ - offset);
            doorSN.placeNoReturn(world, pos, null);
            spawnDoorNPC(store, pos, DoorRegistry.Orientation.SN);
        }
        if (doors[1]) {
            Vector3i pos = new Vector3i(worldX + offset, doorY, worldZ);
            doorWE.placeNoReturn(world, pos, null);
            spawnDoorNPC(store, pos, DoorRegistry.Orientation.WE);
        }
        if (doors[2]) {
            Vector3i pos = new Vector3i(worldX, doorY, worldZ + offset);
            doorSN.placeNoReturn(world, pos, null);
            spawnDoorNPC(store, pos, DoorRegistry.Orientation.SN);
        }
        if (doors[3]) {
            Vector3i pos = new Vector3i(worldX - offset, doorY, worldZ);
            doorWE.placeNoReturn(world, pos, null);
            spawnDoorNPC(store, pos, DoorRegistry.Orientation.WE);
        }
    }

    private void spawnDoorNPC(Store<EntityStore> store, Vector3i pos,
            DoorRegistry.Orientation orientation) {
    // Spawn position centered on the door, at a walkable Y
    Vector3d spawnPos = new Vector3d(pos.x +0.5, pos.y, pos.z+ 0.5);
        Vector3f rotation = new Vector3f(0, 0, 0);

        Pair<Ref<EntityStore>, INonPlayerCharacter> result = NPCPlugin.get().spawnNPC(
                store,
                "Invis",
                null,
                spawnPos,
                rotation);
        if (result == null) {
            System.out.println("[DoorSystem] ERROR: Failed to spawn door NPC at " + pos.x + "," + pos.y + "," + pos.z);
            return;
        }

        Ref<EntityStore> npcRef = result.first();

        // Attach door data to the NPC
        store.addComponent(npcRef, DoorNPCComponent.getComponentType(),
                new DoorNPCComponent(pos, orientation));

        // Reuse NPCSetupPending to wire up the interaction
        store.addComponent(npcRef, NPCSetupPending.getComponentType(),
                new NPCSetupPending("Root_OpenDoor", "Open"));

        store.addComponent(npcRef, Invulnerable.getComponentType());

        DoorRegistry.register(pos, orientation);
        System.out.println("[DoorSystem] Door NPC spawned at " + pos.x + "," + pos.y + "," + pos.z);
    }

    public void clearDungeon(World world, DungeonInstance inst) {
        int gridsize = inst.grid.length;
        for (int x = 0; x < gridsize; x++) {
            for (int y = 0; y < gridsize; y++) {
                if (inst.grid[x][y] == null)
                    continue;
                int worldX = inst.worldOriginX + x * inst.spacing;
                int worldZ = inst.worldOriginZ + y * inst.spacing;
                for (int dx = -inst.spacing / 2; dx <= inst.spacing / 2; dx++) {
                    for (int dz = -inst.spacing / 2; dz <= inst.spacing / 2; dz++) {
                        for (int dy = clearYMin; dy <= clearYMax; dy++) {
                            world.setBlock(worldX + dx, dy, worldZ + dz, "Empty");
                        }
                    }
                }
            }
        }
    }
}
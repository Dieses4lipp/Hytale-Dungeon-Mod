package com.example.plugin.DungeonGeneration;

import java.nio.file.Path;

import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.math.vector.Vector3i;

public class DungeonGenerator {

    private final int spacing   = DungeonConfig.get().manager.spacing;
    private final int baseY     = DungeonConfig.get().generator.baseY;
    private final int doorY     = DungeonConfig.get().generator.doorY;
    private final int clearYMin = DungeonConfig.get().generator.clearYMin;
    private final int clearYMax = DungeonConfig.get().generator.clearYMax;

    // Adjust to match actual block dimensions of your closed-door prefabs
    private static final int DOOR_WIDTH  = 3;
    private static final int DOOR_HEIGHT = 3;

    private final Path doorSNClosedPath = Path.of("prefabs/Prefabs/Door/door_sn_closed.prefab.json");
    private final Path doorWEClosedPath = Path.of("prefabs/Prefabs/Door/door_we_closed.prefab.json");

    /** Signature changed: takes DungeonInstance so it can register doors into its registry. */
    public void generate(World world, DungeonInstance inst) {
        Room[][] grid   = inst.grid;
        int originX     = inst.worldOriginX;
        int originZ     = inst.worldOriginZ;
        int gridsize    = grid.length;
        PrefabStore prefabStore = PrefabStore.get();

        // The start room is unlocked from the beginning
        inst.doorRegistry.unlockRoom(inst.startX, inst.startY);

        for (int x = 0; x < gridsize; x++) {
            for (int y = 0; y < grid[x].length; y++) {
                Room room = grid[x][y];
                if (room == null) continue;

                int worldX = originX + x * spacing;
                int worldZ = originZ + y * spacing;

                if (room.isSatellite()) {
                    placeDoors(world, prefabStore, inst, room, x, y, worldX, worldZ);
                    continue;
                }
                placeRoom(world, prefabStore, room, worldX, worldZ);
                placeDoors(world, prefabStore, inst, room, x, y, worldX, worldZ);
            }
        }
    }

    private void placeRoom(World world, PrefabStore prefabStore, Room room,
                           int worldX, int worldZ) {
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

    private void placeDoors(World world, PrefabStore prefabStore,
                            DungeonInstance inst,
                            Room room, int gridX, int gridY,
                            int worldX, int worldZ) {
        boolean[] doors = room.getDoors();
        boolean isBossOrigin = room.getType() == RoomType.BOSS && !room.isSatellite();
        int offset = isBossOrigin ? (spacing + spacing / 2) : (spacing / 2);

        BlockSelection doorSN = prefabStore.getPrefab(doorSNClosedPath);
        BlockSelection doorWE = prefabStore.getPrefab(doorWEClosedPath);

        // North (side 0) → neighbour grid cell is (gridX, gridY - 1)
        if (doors[0]) {
            Vector3i origin = new Vector3i(worldX, doorY, worldZ - offset);
            doorSN.placeNoReturn(world, origin, null);
            inst.doorRegistry.addDoor(new Door(
                gridX, gridY, gridX, gridY - 1,
                0, origin, true, DOOR_WIDTH, DOOR_HEIGHT
            ));
        }
        // East (side 1) → neighbour grid cell is (gridX + 1, gridY)
        if (doors[1]) {
            Vector3i origin = new Vector3i(worldX + offset, doorY, worldZ);
            doorWE.placeNoReturn(world, origin, null);
            inst.doorRegistry.addDoor(new Door(
                gridX, gridY, gridX + 1, gridY,
                1, origin, false, DOOR_WIDTH, DOOR_HEIGHT
            ));
        }
        // South (side 2) → neighbour grid cell is (gridX, gridY + 1)
        if (doors[2]) {
            Vector3i origin = new Vector3i(worldX, doorY, worldZ + offset);
            doorSN.placeNoReturn(world, origin, null);
            inst.doorRegistry.addDoor(new Door(
                gridX, gridY, gridX, gridY + 1,
                2, origin, true, DOOR_WIDTH, DOOR_HEIGHT
            ));
        }
        // West (side 3) → neighbour grid cell is (gridX - 1, gridY)
        if (doors[3]) {
            Vector3i origin = new Vector3i(worldX - offset, doorY, worldZ);
            doorWE.placeNoReturn(world, origin, null);
            inst.doorRegistry.addDoor(new Door(
                gridX, gridY, gridX - 1, gridY,
                3, origin, false, DOOR_WIDTH, DOOR_HEIGHT
            ));
        }
    }

    public void clearDungeon(World world, DungeonInstance inst) {
        int gridsize = inst.grid.length;
        for (int x = 0; x < gridsize; x++) {
            for (int y = 0; y < gridsize; y++) {
                if (inst.grid[x][y] == null) continue;
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
        inst.doorRegistry.clear();
    }

    private String markerBlockFor(RoomType type) {
        return switch (type) {
            case BOSS     -> "Cloth_Block_Wool_Purple";
            case TREASURE -> "Cloth_Block_Wool_Yellow";
            case HALLWAY  -> "Cloth_Block_Wool_Gray";
            case SHOP     -> "Cloth_Block_Wool_Blue";
            case STASH    -> "Cloth_Block_Wool_Orange";
            default       -> "Cloth_Block_Wool_Green";
        };
    }
}
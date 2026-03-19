package com.example.plugin.DungeonGeneration;

import java.nio.file.Path;

import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.math.vector.Vector3i;

public class DungeonGenerator {

    private final int spacing  = DungeonConfig.get().manager.spacing;
    private final int baseY    = DungeonConfig.get().generator.baseY;
    private final int doorY    = DungeonConfig.get().generator.doorY;
    private final int clearYMin = DungeonConfig.get().generator.clearYMin;
    private final int clearYMax = DungeonConfig.get().generator.clearYMax;

    private final Path doorWEPath = Path.of("prefabs/Prefabs/door2.prefab.json");
    private final Path doorSNPath = Path.of("prefabs/Prefabs/door.prefab.json");

    public void generate(World world, Room[][] grid, int originX, int originZ) {
        int gridsize = grid.length;
        PrefabStore prefabStore = PrefabStore.get();

        for (int x = 0; x < gridsize; x++) {
            for (int y = 0; y < grid[x].length; y++) {
                Room room = grid[x][y];
                if (room == null) continue;

                int worldX = originX + x * spacing;
                int worldZ = originZ + y * spacing;

                if (room.isSatellite()) {
                    placeDoors(world, prefabStore, room, worldX, worldZ);
                    continue;
                }
                placeRoom(world, prefabStore, room, worldX, worldZ);
                placeDoors(world, prefabStore, room, worldX, worldZ);
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
    int placeY = room.getType() == RoomType.BOSS
        ? baseY + DungeonConfig.get().generator.bossYOffset
        : baseY;
    world.setBlock(worldX, baseY - 1, worldZ, markerBlockFor(room.getType()));
    prefab.placeNoReturn(world, new Vector3i(worldX, placeY, worldZ), null);
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

    private void placeDoors(World world, PrefabStore prefabStore, Room room, int worldX, int worldZ) {
        boolean[] doors = room.getDoors();
        boolean isBossOrigin = room.getType() == RoomType.BOSS && !room.isSatellite();
        int offset = isBossOrigin ? (spacing + spacing / 2) : (spacing / 2);

        BlockSelection doorSN = prefabStore.getPrefab(doorSNPath);
        BlockSelection doorWE = prefabStore.getPrefab(doorWEPath);

        if (doors[0]) doorSN.placeNoReturn(world, new Vector3i(worldX,          doorY, worldZ - offset), null);
        if (doors[1]) doorWE.placeNoReturn(world, new Vector3i(worldX + offset,  doorY, worldZ),          null);
        if (doors[2]) doorSN.placeNoReturn(world, new Vector3i(worldX,          doorY, worldZ + offset), null);
        if (doors[3]) doorWE.placeNoReturn(world, new Vector3i(worldX - offset,  doorY, worldZ),          null);
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
    }
}
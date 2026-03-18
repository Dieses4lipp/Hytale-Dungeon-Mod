package com.example.plugin.DungeonGeneration;

import java.nio.file.Path;
import java.util.Random;

import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;

public class DungeonGenerator {

    public int abstand = 11;

    private final Path doorWEPath = Path.of("prefabs/Prefabs/door2.prefab.json");
    private final Path doorSNPath = Path.of("prefabs/Prefabs/door.prefab.json");

    public void generate(World world, Room[][] grid, int originX, int originZ) {
        int gridsize = grid.length;
        PrefabStore prefabStore = PrefabStore.get();

        for (int x = 0; x < gridsize; x++) {
            for (int y = 0; y < grid[x].length; y++) {
                Room room = grid[x][y];
                if (room == null)
                    continue;

                int worldX = originX + x * abstand;
                int worldZ = originZ + y * abstand;

                if (room.isSatellite()) {
                    placeDoors(world, prefabStore, room, worldX, worldZ);
                    continue;
                }
                placeRoom(world, prefabStore, room, worldX, worldZ);
                placeDoors(world, prefabStore, room, worldX, worldZ);
            }
        }
    }

    int bossroomcounter = 0;

    private void placeRoom(World world, PrefabStore prefabStore, Room room, int worldX, int worldZ) {
        Path prefabPath = room.getType().getRandomPrefabPath();
        BlockSelection prefab = prefabStore.getPrefab(prefabPath);
        world.setBlock(worldX, 90, worldZ, markerBlockFor(room.getType()));
        prefab.placeNoReturn(world, new Vector3i(worldX, 90, worldZ), null);
    }

    private String markerBlockFor(RoomType type) {
        return switch (type) {
            case BOSS -> "Cloth_Block_Wool_Purple";
            case TREASURE -> "Cloth_Block_Wool_Yellow";
            case HALLWAY -> "Cloth_Block_Wool_Gray";
            default -> "Cloth_Block_Wool_Green";
        };
    }

    private void placeDoors(World world, PrefabStore prefabStore, Room room, int worldX, int worldZ) {
        boolean[] doors = room.getDoors();
        boolean isBossOrigin = room.getType() == RoomType.BOSS && !room.isSatellite();
        int offset = isBossOrigin ? (abstand + abstand / 2) : (abstand / 2);

        BlockSelection doorSN = prefabStore.getPrefab(doorSNPath);
        BlockSelection doorWE = prefabStore.getPrefab(doorWEPath);

        if (doors[0])
            doorSN.placeNoReturn(world, new Vector3i(worldX, 91, worldZ - offset), null);
        if (doors[1])
            doorWE.placeNoReturn(world, new Vector3i(worldX + offset, 91, worldZ), null);
        if (doors[2])
            doorSN.placeNoReturn(world, new Vector3i(worldX, 91, worldZ + offset), null);
        if (doors[3])
            doorWE.placeNoReturn(world, new Vector3i(worldX - offset, 91, worldZ), null);
    }

    public void clearDungeon(World world, DungeonInstance inst) {
        int gridsize = inst.grid.length;
        for (int x = 0; x < gridsize; x++) {
            for (int y = 0; y < gridsize; y++) {
                if (inst.grid[x][y] == null)
                    continue;
                int worldX = inst.worldOriginX + x * inst.abstand;
                int worldZ = inst.worldOriginZ + y * inst.abstand;
                for (int dx = -inst.abstand / 2; dx <= inst.abstand / 2; dx++) {
                    for (int dz = -inst.abstand / 2; dz <= inst.abstand / 2; dz++) {
                        for (int dy = 90; dy <= 100; dy++) {
                            world.setBlock(worldX + dx, dy, worldZ + dz, "Empty");
                        }
                    }
                }
            }
        }
    }

}
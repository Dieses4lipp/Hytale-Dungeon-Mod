package com.example.plugin.DungeonGeneration;

import java.nio.file.Path;
import java.util.Random;

import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.World;
import com.example.plugin.RoomType;
import com.hypixel.hytale.math.vector.Vector3i;

public class DungeonGenerator {

    public int abstand = 11;

    private final Path doorWEPath = Path.of("prefabs/Prefabs/door2.prefab.json");
    private final Path doorSNPath = Path.of("prefabs/Prefabs/door.prefab.json");

    public void generate(World world, Room[][] grid) {
        int gridsize = grid.length;
        PrefabStore prefabStore = PrefabStore.get();

        for (int x = 0; x < gridsize; x++) {
            for (int y = 0; y < grid[x].length; y++) {
                Room room = grid[x][y];
                if (room == null)
                    continue;
                if (room.isSatellite()) {
                    placeDoors(world, prefabStore, room, x, y); // don't place room, but DO place doors
                    continue;
                }
                placeRoom(world, prefabStore, room, x, y);
                placeDoors(world, prefabStore, room, x, y);
            }
        }
    }

    int bossroomcounter = 0;

    private void placeRoom(World world, PrefabStore prefabStore, Room room, int x, int y) {
        RoomType type = room.getType();
        Path prefabPath = type.getRandomPrefabPath();
        System.out.println("[Debug] Loading prefab: " + prefabPath);
        BlockSelection prefab = prefabStore.getPrefab(prefabPath);

        int worldX = x * abstand;
        int worldZ = y * abstand;

        world.setBlock(worldX, 90, worldZ, markerBlockFor(type));
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

    private void placeDoors(World world, PrefabStore prefabStore, Room room, int x, int y) {
        boolean[] doors = room.getDoors();

        int worldX = x * abstand;
        int worldZ = y * abstand;


        boolean isBossOrigin = room.getType() == RoomType.BOSS && !room.isSatellite();
        int offset = isBossOrigin ? (abstand + abstand / 2) : (abstand / 2);

        BlockSelection doorSN = prefabStore.getPrefab(doorSNPath);
        BlockSelection doorWE = prefabStore.getPrefab(doorWEPath);

        // North (0)
        if (doors[0]) {
            world.setBlock(worldX, 89, worldZ - 1, "Cloth_Block_Wool_Red");
            doorSN.placeNoReturn(world, new Vector3i(worldX, 91, worldZ - offset), null);
        }
        // East (1)
        if (doors[1]) {
            world.setBlock(worldX + 1, 89, worldZ, "Cloth_Block_Wool_Red");
            doorWE.placeNoReturn(world, new Vector3i(worldX + offset, 91, worldZ), null);
        }
        // South (2)
        if (doors[2]) {
            world.setBlock(worldX, 89, worldZ + 1, "Cloth_Block_Wool_Red");
            doorSN.placeNoReturn(world, new Vector3i(worldX, 91, worldZ + offset), null);
        }
        // West (3)
        if (doors[3]) {
            world.setBlock(worldX - 1, 89, worldZ, "Cloth_Block_Wool_Red");
            doorWE.placeNoReturn(world, new Vector3i(worldX - offset, 91, worldZ), null);
        }
    }

}
package com.example.plugin.DungeonGeneration;

import java.nio.file.Path;
import java.util.Random;

import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.math.vector.Vector3i;

public class DungeonGenerator {
        public int abstand = 10;
    public void generate(World world, Room[][] grid) {
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[x].length; y++) {
                if (grid[x][y] != null) {
                    
                    PrefabStore prefabStore = PrefabStore.get();
                    Path roomPrefabPath = getRandomRoomPreFabPath();
                    Path doorWEPath = Path.of("prefabs/Prefabs/door2.prefab.json");
                    Path doorSNPath = Path.of("prefabs/Prefabs/door.prefab.json");
                    BlockSelection roomSNPrefab = prefabStore.getPrefab(doorSNPath);
                    BlockSelection roomWEPrefab = prefabStore.getPrefab(doorWEPath);
                    BlockSelection roomPrefab = prefabStore.getPrefab(roomPrefabPath);
                    
                    if (roomPrefab != null) {
                        world.setBlock(x * abstand, 90, y * abstand, "Cloth_Block_Wool_Green");
                        Vector3i place = new Vector3i(x * abstand, 90, y * abstand);
                        roomPrefab.placeNoReturn(world, place, null);
                    }
                    Room currentRoom = grid[x][y];
                    if (currentRoom.getDoors()[0]) {
                        world.setBlock(x * abstand, 90, y * abstand - 1, "Cloth_Block_Wool_Red");
                        roomSNPrefab.placeNoReturn(world, new Vector3i(x * abstand, 91, y * abstand - (abstand / 2)), null);
                    }
                    if (currentRoom.getDoors()[1]) {
                        world.setBlock(x * abstand + 1, 90, y * abstand, "Cloth_Block_Wool_Red");
                        roomWEPrefab.placeNoReturn(world, new Vector3i((x * abstand) + (abstand / 2), 91, y * abstand), null);
                        
                    }
                    if (currentRoom.getDoors()[2]) {
                        world.setBlock(x * abstand, 90, y * abstand + 1, "Cloth_Block_Wool_Red");
                        roomSNPrefab.placeNoReturn(world, new Vector3i(x * abstand, 91, y * abstand + (abstand / 2)), null);
                    }
                    if (currentRoom.getDoors()[3]) {
                        world.setBlock(x * abstand - 1, 90, y * abstand, "Cloth_Block_Wool_Red");
                        roomWEPrefab.placeNoReturn(world, new Vector3i(x * abstand - (abstand / 2), 91, y * abstand), null);
                        
                    }
                }
            }
        }
    }
    public Path getRandomRoomPreFabPath(){
        Path[] allroomPrefabs = new Path[]{
            Path.of("prefabs/Prefabs/testroom1.prefab.json"),
            Path.of("prefabs/Prefabs/testroom2.prefab.json"),
            Path.of("prefabs/Prefabs/testroom3.prefab.json")
        };
        return allroomPrefabs[new Random().nextInt(allroomPrefabs.length)];
    }
}


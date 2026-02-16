package com.example.plugin;

import java.util.Random;
import java.util.HashSet;
import java.util.Iterator;
import java.awt.Point;
import java.nio.file.Path;
import java.util.Set;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.World;


public class DungeonLayoutGenerator {
    private int NUMBERROOMS = 100;
    private int gridsize = 20;
    private Room[][] grid = new Room[gridsize][gridsize];
    private Set<Point> nextRoomToProcess = new HashSet<>();
    int counter = 0;
    public void generateLayout() {
        int startY = grid.length / 2;
        int startX = grid[0].length / 2;
        Room startRoom = new Room();
        startRoom.getDoors()[0] = true;
        grid[startY][startX] = startRoom;
        nextRoomToProcess.add(new Point(startX, startY - 1));

        while (!nextRoomToProcess.isEmpty()) {
            Point currentPoint = getAndRemoveRandom(nextRoomToProcess);
            Room currentRoom = grid[currentPoint.x][currentPoint.y];
            if (currentRoom != null)
                continue;
            currentRoom = grid[currentPoint.x][currentPoint.y] = new Room();
            
            //#region Room generation 
            // norden check ob raum da mit tür zu mir
            if (grid[currentPoint.x][currentPoint.y - 1] != null
                    && grid[currentPoint.x][currentPoint.y - 1].getDoors()[2]) {
                currentRoom.getDoors()[0] = true;
            }
            // osten check ob raum da mit tür zu mir
            if (grid[currentPoint.x + 1][currentPoint.y] != null
                    && grid[currentPoint.x + 1][currentPoint.y].getDoors()[3]) {
                currentRoom.getDoors()[1] = true;
            }
            // süden check ob raum da mit tür zu mir
            if (grid[currentPoint.x][currentPoint.y + 1] != null
                    && grid[currentPoint.x][currentPoint.y + 1].getDoors()[0]) {
                currentRoom.getDoors()[2] = true;
            }
            // westen check ob raum da mit tür zu mir
            if (grid[currentPoint.x - 1][currentPoint.y] != null
                    && grid[currentPoint.x - 1][currentPoint.y].getDoors()[1]) {
                currentRoom.getDoors()[3] = true;
            }
            //#endregion
            //#region Door generation
            boolean[] doors = currentRoom.getDoors();
            double probalitiy = 0.9;
            if(counter + nextRoomToProcess.size() > NUMBERROOMS){
                probalitiy = 0;
            }
            if (grid[currentPoint.x][currentPoint.y - 1] == null && Math.random() < probalitiy && currentPoint.y > 1) {
                doors[0] = true;
            } // norden
            if (grid[currentPoint.x + 1][currentPoint.y] == null && Math.random() < probalitiy
                    && currentPoint.x < gridsize - 2) {
                doors[1] = true;
            } // osten
            if (grid[currentPoint.x][currentPoint.y + 1] == null && Math.random() < probalitiy
                    && currentPoint.y < gridsize - 2) {
                doors[2] = true;
            } // süden
            if (grid[currentPoint.x - 1][currentPoint.y] == null && Math.random() < probalitiy && currentPoint.x > 1) {
                doors[3] = true;
            } // westen

            for (int i = 0; i < doors.length; i++) {
                if (doors[i]) {
                    switch (i) {
                        case 0:
                            nextRoomToProcess.add(new Point(currentPoint.x, currentPoint.y - 1));
                            break;
                        case 1:
                            nextRoomToProcess.add(new Point(currentPoint.x + 1, currentPoint.y));
                            break;
                        case 2:
                            nextRoomToProcess.add(new Point(currentPoint.x, currentPoint.y + 1));
                            break;
                        case 3:
                            nextRoomToProcess.add(new Point(currentPoint.x - 1, currentPoint.y));
                            break;
                        default:
                            break;
                    }
                }
            }
            counter++;

        }
        //#endregion

        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                System.out.print(grid[y][x] == null ? " ." : " R");
            }
            System.out.println(" ");
        }
    }

    public static <T> T getAndRemoveRandom(Set<T> set) {
        if (set.isEmpty())
            return null;

        int index = new Random().nextInt(set.size());
        Iterator<T> iter = set.iterator();

        for (int i = 0; i < index; i++) {
            iter.next();
        }

        T item = iter.next();
        iter.remove();
        return item;
    }

    public int abstand = 10;
    public void generate(World world) {
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
                    world.setBlock(x * abstand, 90, y * abstand, "Cloth_Block_Wool_Blue");
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

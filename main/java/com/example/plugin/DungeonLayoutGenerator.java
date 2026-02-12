package com.example.plugin;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.awt.Point;
import java.util.Set;

import com.hypixel.hytale.server.core.universe.world.World;

public class DungeonLayoutGenerator {
    private int gridsize = 20;
    private Room[][] grid = new Room[gridsize][gridsize];
    private Set<Point> nextRoomToProcess = new HashSet<>();

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

            //norden check ob raum da mit tür zu mir
            if(grid[currentPoint.x][currentPoint.y-1]!=null && grid[currentPoint.x][currentPoint.y-1].getDoors()[2]){currentRoom.getDoors()[0]=true;}
            //osten check ob raum da mit tür zu mir
            if(grid[currentPoint.x+1][currentPoint.y]!=null && grid[currentPoint.x + 1][currentPoint.y].getDoors()[3]){currentRoom.getDoors()[1]=true;}
            //süden check ob raum da mit tür zu mir
            if(grid[currentPoint.x][currentPoint.y+1]!=null && grid[currentPoint.x][currentPoint.y+1].getDoors()[0]){currentRoom.getDoors()[2]=true;}
            //westen check ob raum da mit tür zu mir
            if(grid[currentPoint.x-1][currentPoint.y]!=null && grid[currentPoint.x - 1][currentPoint.y].getDoors()[1]){currentRoom.getDoors()[3]=true;}

            boolean[] doors = currentRoom.getDoors();

            if(grid[currentPoint.x][currentPoint.y-1]==null && Math.random()<0.5 && currentPoint.y > 1){doors[0]=true;} //norden
            if(grid[currentPoint.x+1][currentPoint.y]==null && Math.random()<0.5 && currentPoint.x < gridsize - 2){doors[1]=true;} //osten
            if(grid[currentPoint.x][currentPoint.y+1]==null && Math.random()<0.5 && currentPoint.y < gridsize - 2){doors[2]=true;} //süden
            if(grid[currentPoint.x-1][currentPoint.y]==null && Math.random()<0.5 && currentPoint.x > 1){doors[3]=true;} //westen

            
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

        }
        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                System.out.print(grid[y][x] == null ? " ." : " R");
            }
            System.out.println(" ");
        }
    }

    public static <T> T getAndRemoveRandom(Set<T> set) {
    if (set.isEmpty()) return null;

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
public void generate(World world){
    for (int x = 0; x < grid.length; x++) {
        for (int y = 0; y < grid[x].length; y++) {
            if(grid[x][y] != null){
                world.setBlock(x * abstand, 90, y * abstand, "Cloth_Block_Wool_Green");
                Room currentRoom = grid[x][y];
                if(currentRoom.getDoors()[0]){world.setBlock(x * abstand, 90, y * abstand -1, "Cloth_Block_Wool_Red");}
                if(currentRoom.getDoors()[1]){world.setBlock(x * abstand +1, 90, y * abstand, "Cloth_Block_Wool_Red");}
                if(currentRoom.getDoors()[2]){world.setBlock(x * abstand, 90, y * abstand +1, "Cloth_Block_Wool_Red");}
                if(currentRoom.getDoors()[3]){world.setBlock(x * abstand -1, 90, y * abstand, "Cloth_Block_Wool_Red");}
            }
        }
    }
}

}

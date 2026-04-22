package com.example.plugin.DungeonGeneration;

import java.awt.Point;
import java.util.*;

public class LayoutGenerator {

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    private int NUMBERROOMS = 20;
    private final int gridsize = DungeonConfig.get().layout.gridsize;
    private final double TREASURE_CHANCE = DungeonConfig.get().layout.treasureChance;
    private final double HALLWAY_CHANCE = DungeonConfig.get().layout.hallwayChance;
    private Room[][] grid = new Room[gridsize][gridsize];
    private Set<Point> nextRoomToProcess = new HashSet<>();
    int counter = 0;
    private int startX;
    private int startY;

    public void generateLayout(int roomCount) {
        NUMBERROOMS = roomCount;
        startY = grid.length / 2;
        startX = grid[0].length / 2;
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

            // take over doors from neighbours
            if (grid[currentPoint.x][currentPoint.y - 1] != null
                    && grid[currentPoint.x][currentPoint.y - 1].getDoors()[2])
                currentRoom.getDoors()[0] = true;

            if (grid[currentPoint.x + 1][currentPoint.y] != null
                    && grid[currentPoint.x + 1][currentPoint.y].getDoors()[3])
                currentRoom.getDoors()[1] = true;

            if (grid[currentPoint.x][currentPoint.y + 1] != null
                    && grid[currentPoint.x][currentPoint.y + 1].getDoors()[0])
                currentRoom.getDoors()[2] = true;

            if (grid[currentPoint.x - 1][currentPoint.y] != null
                    && grid[currentPoint.x - 1][currentPoint.y].getDoors()[1])
                currentRoom.getDoors()[3] = true;

            // generate doors
            boolean[] doors = currentRoom.getDoors();
            double probability = (counter + nextRoomToProcess.size() > NUMBERROOMS) ? 0.0 : 0.9;

            if (grid[currentPoint.x][currentPoint.y - 1] == null && Math.random() < probability && currentPoint.y > 1)
                doors[0] = true;
            if (grid[currentPoint.x + 1][currentPoint.y] == null && Math.random() < probability
                    && currentPoint.x < gridsize - 2)
                doors[1] = true;
            if (grid[currentPoint.x][currentPoint.y + 1] == null && Math.random() < probability
                    && currentPoint.y < gridsize - 2)
                doors[2] = true;
            if (grid[currentPoint.x - 1][currentPoint.y] == null && Math.random() < probability && currentPoint.x > 1)
                doors[3] = true;
            for (int i = 0; i < doors.length; i++) {
                if (!doors[i])
                    continue;
                switch (i) {
                    case 0 -> nextRoomToProcess.add(new Point(currentPoint.x, currentPoint.y - 1));
                    case 1 -> nextRoomToProcess.add(new Point(currentPoint.x + 1, currentPoint.y));
                    case 2 -> nextRoomToProcess.add(new Point(currentPoint.x, currentPoint.y + 1));
                    case 3 -> nextRoomToProcess.add(new Point(currentPoint.x - 1, currentPoint.y));
                }
            }
            counter++;
        }
        // clean up rest doors still pointing outwards
        for (int x = 0; x < gridsize; x++) {
            for (int y = 0; y < gridsize; y++) {
                Room room = grid[x][y];
                if (room == null)
                    continue;
                boolean[] doors = room.getDoors();
                if (doors[0] && (y == 0 || grid[x][y - 1] == null))
                    doors[0] = false;
                if (doors[1] && (x == gridsize - 1 || grid[x + 1][y] == null))
                    doors[1] = false;
                if (doors[2] && (y == gridsize - 1 || grid[x][y + 1] == null))
                    doors[2] = false;
                if (doors[3] && (x == 0 || grid[x - 1][y] == null))
                    doors[3] = false;
            }
        }

        assignRoomTypes();
    }

    private void assignRoomTypes() {
        List<Point> allRooms = new ArrayList<>();
        for (int x = 0; x < gridsize; x++)
            for (int y = 0; y < gridsize; y++)
                if (grid[x][y] != null && !grid[x][y].isSatellite())
                    allRooms.add(new Point(x, y));

        Point centre = new Point(gridsize / 2, gridsize / 2);

        // Try farthest rooms first, find one where a 3×3 fits adjacent to it
        boolean bossPlaced = allRooms.stream()
                .sorted(Comparator.comparingDouble((Point p) -> p.distance(centre)).reversed())
                .anyMatch(p -> tryPlaceBossAdjacentTo(p.x, p.y));

        if (!bossPlaced) {
            allRooms.stream()
                    .max(Comparator.comparingDouble(p -> p.distance(centre)))
                    .ifPresent(p -> {
                        grid[p.x][p.y].setType(RoomType.BOSS);
                    });
        }

        for (Point p : allRooms) {
            Room room = grid[p.x][p.y];
            if (room == null || room.isSatellite() || room.getType() == RoomType.BOSS)
                continue;
            if (p.x == startX && p.y == startY)
                continue;
            double roll = Math.random();
            if (roll < TREASURE_CHANCE)
                room.setType(RoomType.TREASURE);
            else if (roll < TREASURE_CHANCE + HALLWAY_CHANCE)
                room.setType(RoomType.HALLWAY);
        }
        Collections.shuffle(allRooms);
        if (Math.random() < DungeonConfig.get().layout.shopChance) {
            allRooms.stream()
                    .filter(p -> grid[p.x][p.y].getType() == RoomType.NORMAL)
                    .filter(p -> !(p.x == startX && p.y == startY))
                    .filter(p -> grid[p.x][p.y].countDoors() == 1)
                    .findAny()
                    .ifPresent(p -> grid[p.x][p.y].setType(RoomType.SHOP));
        }
        if (Math.random() < DungeonConfig.get().layout.stashChance) {
            allRooms.stream()
                    .filter(p -> grid[p.x][p.y].getType() == RoomType.NORMAL)
                    .filter(p -> !(p.x == startX && p.y == startY))
                    .findAny()
                    .ifPresent(p -> grid[p.x][p.y].setType(RoomType.STASH));
        }
    }

    /**
     * Tries to place a 3×3 boss room adjacent to the room at (rx, ry).
     * The entry is always through the middle cell of one side of the boss.
     * Checks all 4 directions and picks the first that fits.
     */
    private boolean tryPlaceBossAdjacentTo(int rx, int ry) {
        int[][] directions = {
                { 0, -2 }, { 2, 0 }, { 0, 2 }, { -2, 0 }
        };
        int[] doorIndex = { 2, 3, 0, 1 };

        for (int d = 0; d < 4; d++) {
            int bx = rx + directions[d][0];
            int by = ry + directions[d][1];

            if (!isFree3x3(bx, by))
                continue;

            Room origin = new Room();
            origin.setType(RoomType.BOSS);
            origin.getDoors()[doorIndex[d]] = true;
            grid[bx][by] = origin;

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0)
                        continue;
                    Room satellite = new Room();
                    satellite.setOriginRoom(origin);
                    grid[bx + dx][by + dy] = satellite;
                }
            }

            int edgeX = rx + directions[d][0] / 2;
            int edgeY = ry + directions[d][1] / 2;
            grid[edgeX][edgeY].getDoors()[doorIndex[d]] = true;

            int[][] offsets = { { 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 } };
            int[] opposite = { 2, 3, 0, 1 };
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0)
                        continue;
                    int sx = bx + dx;
                    int sy = by + dy;
                    for (int n = 0; n < 4; n++) {
                        int nx = sx + offsets[n][0];
                        int ny = sy + offsets[n][1];
                        if (nx < 0 || ny < 0 || nx >= gridsize || ny >= gridsize)
                            continue;
                        Room neighbour = grid[nx][ny];
                        if (neighbour != null && !neighbour.isSatellite() && neighbour.getType() != RoomType.BOSS) {
                            neighbour.getDoors()[opposite[n]] = false;
                        }
                    }
                }
            }
            int connectDoor = (doorIndex[d] + 2) % 4;
            grid[rx][ry].getDoors()[connectDoor] = true;

            return true;
        }
        return false;
    }

    private boolean isFree3x3(int cx, int cy) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int nx = cx + dx;
                int ny = cy + dy;
                if (nx < 0 || ny < 0 || nx >= gridsize || ny >= gridsize)
                    return false;
                if (grid[nx][ny] != null)
                    return false;
            }
        }
        return true;
    }

    public static <T> T getAndRemoveRandom(Set<T> set) {
        if (set.isEmpty())
            return null;
        int index = new Random().nextInt(set.size());
        Iterator<T> iter = set.iterator();
        for (int i = 0; i < index; i++)
            iter.next();
        T item = iter.next();
        iter.remove();
        return item;
    }

    public Room[][] getGrid() {
        return grid;
    }
}
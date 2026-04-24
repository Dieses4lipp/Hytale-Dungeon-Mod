package com.example.plugin.DungeonGeneration;

public class Room {
    // north:0 east:1 south:2 west:3
    public boolean[] doors = new boolean[4];

    private RoomType type = RoomType.NORMAL;
    private Room originRoom = null;

    public boolean[] getDoors() {
        return doors;
    }

    public RoomType getType() {
        return type;
    }

    public void setType(RoomType type) {
        this.type = type;
    }

    public boolean isSatellite() {
        return originRoom != null;
    }

    public Room getOriginRoom() {
        return originRoom;
    }

    public void setOriginRoom(Room originRoom) {
        this.originRoom = originRoom;
    }

    public int countDoors() {
        int count = 0;
        for (boolean door : doors)
            if (door)
                count++;
        return count;
    }

    public int getSingleDoorDirection() {
        for (int i = 0; i < doors.length; i++)
            if (doors[i])
                return i;
        return -1;
    }
}

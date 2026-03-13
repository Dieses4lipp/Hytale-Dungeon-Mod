package com.example.plugin.DungeonGeneration;

import com.example.plugin.RoomType;

public class Room {
    // norden:0 osten:1 süden:2 westen:3
    public boolean[] doors = new boolean[4];

    private RoomType type = RoomType.NORMAL;
    private Room originRoom = null;

    public boolean[] getDoors() { return doors; }

    public RoomType getType() { return type; }
    public void setType(RoomType type) { this.type = type; }

    /** Returns true if this is a satellite cell of a larger room. */
    public boolean isSatellite() { return originRoom != null; }
    public Room getOriginRoom() { return originRoom; }
    public void setOriginRoom(Room originRoom) { this.originRoom = originRoom; }
}

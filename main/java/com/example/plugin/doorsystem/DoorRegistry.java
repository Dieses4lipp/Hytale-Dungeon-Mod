package com.example.plugin.doorsystem;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;

public class DoorRegistry {

    public enum Orientation {
        SN, WE
    }

    private static final Map<String, Orientation> doors = new ConcurrentHashMap<>();
    private static World world;

    public static void register(Vector3i pos, Orientation orientation) {
        doors.put(key(pos), orientation);
    }

    public static Orientation get(Vector3i pos) {
        return doors.get(key(pos));
    }

    public static void remove(Vector3i pos) {
        doors.remove(key(pos));
    }

    public static void setWorld(World w) {
        world = w;
    }

    public static World getWorld() {
        return world;
    }

    public static void clear() {
        doors.clear();
        world = null;
    }

    private static String key(Vector3i pos) {
        return pos.x + "," + pos.y + "," + pos.z;
    }

    public static int size() {
        return doors.size();
    }
    
    public static List<Vector3i> getNearby(Vector3i pos, int radius) {
    List<Vector3i> result = new java.util.ArrayList<>();
    for (String key : doors.keySet()) {
        String[] parts = key.split(",");
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
        int z = Integer.parseInt(parts[2]);
        if (Math.abs(x - pos.x) <= radius &&
            Math.abs(y - pos.y) <= radius &&
            Math.abs(z - pos.z) <= radius) {
            result.add(new Vector3i(x, y, z));
        }
    }
    return result;
}
}
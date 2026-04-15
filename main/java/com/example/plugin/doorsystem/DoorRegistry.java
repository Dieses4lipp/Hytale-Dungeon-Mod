package com.example.plugin.doorsystem;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DoorRegistry {

    public enum Orientation {
        SN, WE
    }
    
    public static class DoorEntry {
        public final Orientation orientation;
        public final Ref<EntityStore> entityRef;

        public DoorEntry(Orientation orientation, Ref<EntityStore> entityRef) {
            this.orientation = orientation;
            this.entityRef = entityRef;
        }
    }
    
    private static final Map<String, DoorEntry> doors = new ConcurrentHashMap<>();

    public static void register(Vector3i pos, Orientation orientation, Ref<EntityStore> entityRef) {
        doors.put(key(pos), new DoorEntry(orientation, entityRef));
    }

    public static DoorEntry get(Vector3i pos) {
        return doors.get(key(pos));
    }

    public static void remove(Vector3i pos) {
        doors.remove(key(pos));
    }

    public static void clearAll() {
        doors.clear();
    }

    public static int size() {
        return doors.size();
    }

    public static List<Vector3i> getNearby(Vector3i pos, int radius) {
        List<Vector3i> result = new ArrayList<>();
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

    private static String key(Vector3i pos) {
        return pos.x + "," + pos.y + "," + pos.z;
    }
}
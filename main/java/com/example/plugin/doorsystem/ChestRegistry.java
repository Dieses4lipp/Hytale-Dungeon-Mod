package com.example.plugin.doorsystem;


import com.hypixel.hytale.math.vector.Vector3i;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChestRegistry {
    private static final Set<String> lockedChests = ConcurrentHashMap.newKeySet();
    private static final Set<String> openedChests = ConcurrentHashMap.newKeySet();
    public static void lock(Vector3i pos) { lockedChests.add(key(pos)); }
    public static boolean isLocked(Vector3i pos) { return lockedChests.contains(key(pos)); }
    private static String key(Vector3i pos) { return pos.x + "," + pos.y + "," + pos.z; }

    public static void markOpened(Vector3i pos) { openedChests.add(key(pos)); }
    public static boolean isOpened(Vector3i pos) { return openedChests.contains(key(pos)); }
}

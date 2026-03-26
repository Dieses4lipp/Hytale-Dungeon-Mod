package com.example.plugin.DungeonGeneration;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;

import it.unimi.dsi.fastutil.Pair;
import java.util.Random;

public class EnemySpawner {

    private static final Random random = new Random();

    public static void populateDungeon(World world, DungeonInstance instance, Store<EntityStore> store) {
        int baseY = DungeonConfig.get().generator.baseY + 1;
        int roomsPopulated = 0;

        System.out.println("[EnemySpawner] Starting dungeon population...");

        for (int x = 0; x < instance.grid.length; x++) {
            for (int y = 0; y < instance.grid[x].length; y++) {

                Room room = instance.grid[x][y];
                if (room == null){
                    continue;
                }
                RoomType type = room.getType();
                if (type == RoomType.HALLWAY || type == RoomType.STASH || type == RoomType.TREASURE
                        || type == RoomType.SHOP || room.isSatellite()) {
                    continue;
                }
                
                if (x == instance.startX && y == instance.startY)
                    continue;

                int roomStartX = instance.worldOriginX + (x * instance.spacing);
                int roomStartZ = instance.worldOriginZ + (y * instance.spacing);

                int actualRoomSize = 5;

                int minX = roomStartX + 1;
                int maxX = roomStartX + actualRoomSize;
                int minZ = roomStartZ + 1;
                int maxZ = roomStartZ + actualRoomSize;

                if (room.getType() == RoomType.BOSS) {
                    spawnBoss(store, instance, minX, maxX, baseY, minZ, maxZ);
                } else if ((room.getType() == RoomType.SHOP)) {
                    System.out.println("SHop NPC Spawn here");
                } else {
                    spawnNormalEnemies(store, instance, minX, maxX, baseY, minZ, maxZ);
                }
                roomsPopulated++;
            }
        }
        System.out.println("[EnemySpawner] Finished! Populated " + roomsPopulated + " rooms.");
    }

    private static void spawnNormalEnemies(Store<EntityStore> store, DungeonInstance instance, int minX, int maxX,
            int y, int minZ, int maxZ) {
        int enemyCount = 3 + random.nextInt(3);

        for (int i = 0; i < enemyCount; i++) {
            int spawnX = minX + random.nextInt(maxX - minX);
            int spawnZ = minZ + random.nextInt(maxZ - minZ);

            Vector3d spawnPos = new Vector3d(spawnX, y, spawnZ);
            Vector3f rotation = new Vector3f(0, random.nextFloat() * 360f, 0);

            // Make sure this is the EXACT string your command uses
            String prefabToSpawn = "Skeleton_Sand_Guard";

            Pair<Ref<EntityStore>, INonPlayerCharacter> result = NPCPlugin.get().spawnNPC(
                    store,
                    prefabToSpawn,
                    null,
                    spawnPos,
                    rotation);

            if (result != null) {
                instance.spawnedEnemies.add(result.first());
                System.out.println("[EnemySpawner] SUCCESS: Spawned " + prefabToSpawn + " at " + spawnX + ", " + y
                        + ", " + spawnZ);
            } else {
                System.out.println("[EnemySpawner] FAILED: spawnNPC returned null for " + prefabToSpawn + " at "
                        + spawnX + ", " + y + ", " + spawnZ + ". (Is the prefab ID correct? Are chunks loaded?)");
            }
        }
    }

    private static void spawnBoss(Store<EntityStore> store, DungeonInstance instance, int minX, int maxX, int y,
            int minZ, int maxZ) {
        int centerX = minX + ((maxX - minX) / 2);
        int centerZ = minZ + ((maxZ - minZ) / 2);

        Vector3d spawnPos = new Vector3d(centerX, y, centerZ);
        Vector3f rotation = new Vector3f(0, 0, 0);

        String prefabToSpawn = "Klops_Merchant";

        Pair<Ref<EntityStore>, INonPlayerCharacter> result = NPCPlugin.get().spawnNPC(
                store,
                prefabToSpawn,
                null,
                spawnPos,
                rotation);

        if (result != null) {
            instance.spawnedEnemies.add(result.first());
            System.out.println("[EnemySpawner] SUCCESS: Spawned BOSS " + prefabToSpawn + " at " + centerX + ", " + y
                    + ", " + centerZ);
        } else {
            System.out.println("[EnemySpawner] FAILED: BOSS spawnNPC returned null for " + prefabToSpawn
                    + ". (Prefab ID wrong or chunks unloaded?)");
        }
    }
}
package com.example.plugin.DungeonGeneration;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.component.Invulnerable;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;

import it.unimi.dsi.fastutil.Pair;

import java.util.Map;
import java.util.Random;

public class EnemySpawner {

    private static final Random random = new Random();

    public static void populateDungeon(World world, DungeonInstance instance, Store<EntityStore> store) {
        int baseY = DungeonConfig.get().generator.baseY + 1;

        for (int x = 0; x < instance.grid.length; x++) {
            for (int y = 0; y < instance.grid[x].length; y++) {

                Room room = instance.grid[x][y];
                if (room == null) {
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
                } else {
                    spawnNormalEnemies(store, instance, minX, maxX, baseY, minZ, maxZ);
                }
            }
        }
    }

    private static void spawnNormalEnemies(Store<EntityStore> store, DungeonInstance instance, int minX, int maxX,
            int y, int minZ, int maxZ) {
        int enemyCount = 3 + random.nextInt(3);

        Map<String, DungeonTables.MobStats> mobPool = DungeonTables.get().mobs.default_room;

        for (int i = 0; i < enemyCount; i++) {
            int spawnX = minX + random.nextInt(maxX - minX);
            int spawnZ = minZ + random.nextInt(maxZ - minZ);

            Vector3d spawnPos = new Vector3d(spawnX, y, spawnZ);
            Vector3f rotation = new Vector3f(0, random.nextFloat() * 360f, 0);

            String prefabToSpawn = DungeonTables.get().getRandomMob(mobPool);
            if (prefabToSpawn == null) {
                prefabToSpawn = "Skeleton_Sand_Guard";
            }

            int xpBounty = 25;
            if (mobPool.containsKey(prefabToSpawn)) {
                xpBounty = mobPool.get(prefabToSpawn).xp;
            }
            Pair<Ref<EntityStore>, INonPlayerCharacter> result = NPCPlugin.get().spawnNPC(
                    store,
                    prefabToSpawn,
                    null,
                    spawnPos,
                    rotation);

            if (result != null) {
                instance.spawnedEnemies.add(result.first());
                DungeonManager.get().mobXpRewards.put(result.first(), xpBounty);
            }
        }
    }
   public static void spawnBossMinion(Store<EntityStore> store, DungeonInstance instance) {
        java.util.Map<String, DungeonTables.MobStats> minions = DungeonTables.get().mobs.boss_room.minions;
        if (minions == null || minions.isEmpty()) return;

        String selectedMinion = DungeonTables.get().getRandomMob(minions);

        if (selectedMinion != null) {
            int bossRoomX = -1;
            int bossRoomZ = -1;

            for (int x = 0; x < instance.grid.length; x++) {
                for (int y = 0; y < instance.grid[x].length; y++) {
                    Room room = instance.grid[x][y];
                    if (room != null && room.getType() == RoomType.BOSS) {
                        bossRoomX = x;
                        bossRoomZ = y;
                        break;
                    }
                }
                if (bossRoomX != -1) break;
            }

            if (bossRoomX == -1) {
                System.err.println("[EnemySpawner] Could not find boss room to spawn minion.");
                return;
            }

            int roomStartX = instance.worldOriginX + (bossRoomX * instance.spacing);
            int roomStartZ = instance.worldOriginZ + (bossRoomZ * instance.spacing);
            
            int actualRoomSize = 5;
            int minX = roomStartX + 1;
            int maxX = roomStartX + actualRoomSize;
            int minZ = roomStartZ + 1;
            int maxZ = roomStartZ + actualRoomSize;

            int exactCenterX = minX + ((maxX - minX) / 2);
            int exactCenterZ = minZ + ((maxZ - minZ) / 2);
            int spawnY = DungeonConfig.get().generator.baseY + 1;

            com.hypixel.hytale.math.vector.Vector3d spawnPos = new com.hypixel.hytale.math.vector.Vector3d(
                    exactCenterX + (new java.util.Random()).nextInt(5) - 2, 
                    spawnY, 
                    exactCenterZ + (new java.util.Random()).nextInt(5) - 2);
                    
            com.hypixel.hytale.math.vector.Vector3f rotation = new com.hypixel.hytale.math.vector.Vector3f(0, (new java.util.Random()).nextFloat() * 360f, 0);

            it.unimi.dsi.fastutil.Pair<Ref<EntityStore>, com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter> result = 
                com.hypixel.hytale.server.npc.NPCPlugin.get().spawnNPC(
                    store,
                    selectedMinion,
                    null,
                    spawnPos,
                    rotation);

            if (result != null) {
                instance.spawnedEnemies.add(result.first());
                int minionXp = minions.get(selectedMinion).xp;
                DungeonManager.get().mobXpRewards.put(result.first(), minionXp);
            }
        }
    }

    private static void spawnBoss(Store<EntityStore> store, DungeonInstance instance, int minX, int maxX, int y,
            int minZ, int maxZ) {
        int centerX = minX + ((maxX - minX) / 2);
        int centerZ = minZ + ((maxZ - minZ) / 2);
        Map<String, DungeonTables.MobStats> possibleBosses = DungeonTables.get().mobs.boss_room.bosses;

        Vector3d spawnPos = new Vector3d(centerX, y + 10, centerZ);
        Vector3f rotation = new Vector3f(0, 0, 0);

        String prefabToSpawn = DungeonTables.get().getRandomMob(possibleBosses);
        if (prefabToSpawn == null) {
            prefabToSpawn = "Sekeleton";
        }

        DungeonTables.MobStats stats = possibleBosses.get(prefabToSpawn);
        int xpBounty = (stats != null) ? stats.xp : 150;
        Pair<Ref<EntityStore>, INonPlayerCharacter> result = NPCPlugin.get().spawnNPC(
                store,
                prefabToSpawn,
                null,
                spawnPos,
                rotation);
        if (result != null) {
            instance.spawnedEnemies.add(result.first());
            DungeonManager.get().mobXpRewards.put(result.first(), xpBounty);
            instance.bossRef = result.first();
        } else {
            System.err.println("[EnemySpawner] Boss spawnNPC returned null for " + prefabToSpawn
                    + ". (Prefab ID wrong or chunks unloaded?)");
        }
    }
}
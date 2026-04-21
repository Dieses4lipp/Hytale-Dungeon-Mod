package com.example.plugin.DungeonGeneration;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BossMinionSpawnerSystem extends EntityTickingSystem<EntityStore> {

    private final ComponentType<EntityStore, BossMinionSpawnerComponent> spawnerType;

    public BossMinionSpawnerSystem() {
        this.spawnerType = BossMinionSpawnerComponent.getComponentType();
    }

    @Override
    public void tick(float dt, int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        BossMinionSpawnerComponent spawner = store.getComponent(ref, spawnerType);

        if (spawner != null && spawner.active) {
            spawner.timer += dt;
            
            if (spawner.timer >= 5.0f) {
                spawner.timer = 0.0f; // Reset timer for the next 5 seconds
                
                DungeonInstance instance = DungeonManager.get().getBySlot(spawner.dungeonSlot);
                
                if (instance != null && DungeonManager.get().activeWorld != null) {
                    // Tell the World Thread to execute this safely AFTER the tick finishes!
                    DungeonManager.get().activeWorld.execute(() -> {
                        try {
                            EnemySpawner.spawnBossMinion(store, instance);
                        } catch (Exception e) {
                            System.out.println("[MinionSpawner] Error during scheduled spawn:");
                            e.printStackTrace();
                        }
                    });
                } else {
                    commandBuffer.removeComponent(ref, spawnerType);
                }
            }
        }
    }

    @Nullable
    @Override
    public SystemGroup<EntityStore> getGroup() {
        return null;
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(spawnerType);
    }
}
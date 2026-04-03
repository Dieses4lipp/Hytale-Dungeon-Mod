package com.example.plugin.Stats; // Adjust if your package is different!

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class PlayerLevelSetupSystem extends EntityTickingSystem<EntityStore> {

    private final ComponentType<EntityStore, PlayerRef> playerRefType;
    private final ComponentType<EntityStore, PlayerLevelComponent> playerLevelType;

    public PlayerLevelSetupSystem() {
        this.playerRefType = PlayerRef.getComponentType();
        this.playerLevelType = PlayerLevelComponent.getComponentType();
    }

    @Override
    public void tick(float dt, int index, 
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store, 
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        // Get the entity reference
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);

        if (!store.getArchetype(ref).contains(playerLevelType)) {
            PlayerLevelComponent newStats = new PlayerLevelComponent();
            commandBuffer.addComponent(ref, playerLevelType, newStats);
            System.out.println("[DungeonMod] ECS automatically attached PlayerLevelComponent to new Player Ref: " + ref);
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
        // Query: Grab all entities that are Players
        return Query.and(playerRefType);
    }
}
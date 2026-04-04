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
import com.example.plugin.Ui.Hud.LevelHud;

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

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);

        if (!store.getArchetype(ref).contains(playerLevelType)) {
            
            PlayerLevelComponent newStats = new PlayerLevelComponent();
            commandBuffer.addComponent(ref, playerLevelType, newStats);
            System.out.println("[DungeonMod] ECS automatically attached PlayerLevelComponent to new Player Ref: " + ref);

            try {
                com.hypixel.hytale.server.core.entity.entities.Player player = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
                PlayerRef playerRef = store.getComponent(ref, playerRefType);

                if (player != null && playerRef != null) {
                    LevelHud hudPage = new LevelHud(playerRef, store, ref);
player.getHudManager().setCustomHud(playerRef, hudPage);
                    System.out.println("[DungeonMod] Persistent Level HUD opened for player.");
                }
            } catch (Exception e) {
                System.out.println("[DungeonMod] Error opening HUD during setup: " + e.getMessage());
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
        return Query.and(playerRefType);
    }
}
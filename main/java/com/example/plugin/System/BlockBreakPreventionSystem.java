package com.example.plugin.System;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;

public class BlockBreakPreventionSystem extends EntityEventSystem<EntityStore, BreakBlockEvent> {
    public BlockBreakPreventionSystem() {
        super(BreakBlockEvent.class);
    }
    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull BreakBlockEvent event) {

        Ref<EntityStore> playerRef = chunk.getReferenceTo(index);

        if (store.getComponent(playerRef, BuildPermissionComponent.getComponentType()) == null) {

            event.setCancelled(true);

        }
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }

}
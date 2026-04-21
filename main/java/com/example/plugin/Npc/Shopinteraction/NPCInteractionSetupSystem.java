package com.example.plugin.Npc.Shopinteraction;
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
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class NPCInteractionSetupSystem extends EntityTickingSystem<EntityStore> {

    private final ComponentType<EntityStore, NPCSetupPending> setupPendingType;
    private final ComponentType<EntityStore, Interactions> interactionsType;

    public NPCInteractionSetupSystem(
            ComponentType<EntityStore, NPCSetupPending> setupPendingType) {
        this.setupPendingType = setupPendingType;
        this.interactionsType = Interactions.getComponentType();
    }

    @Override
    public void tick(float dt, int index, 
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store, 
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);

        NPCSetupPending setupPending = archetypeChunk.getComponent(index, setupPendingType);

        Interactions interactions = new Interactions();
        interactions.setInteractionId(InteractionType.Use, setupPending.getInteractionId());
        interactions.setInteractionHint(setupPending.getInteractionHint());
        
        commandBuffer.putComponent(ref, interactionsType, interactions);
}

    @Nullable
    @Override
    public SystemGroup<EntityStore> getGroup() {
        return null;
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(setupPendingType);
    }
}

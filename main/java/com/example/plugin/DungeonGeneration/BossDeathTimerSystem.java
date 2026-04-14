package com.example.plugin.DungeonGeneration;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.example.plugin.Ui.DungeonPage.DungeonRecapPage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BossDeathTimerSystem extends EntityTickingSystem<EntityStore> {

    private final ComponentType<EntityStore, PlayerRef> playerRefType;
    private final ComponentType<EntityStore, BossDeathTimerComponent> timerType;

    public BossDeathTimerSystem() {
        this.playerRefType = PlayerRef.getComponentType();
        this.timerType = BossDeathTimerComponent.getComponentType();
    }

    @Override
    public void tick(float dt, int index,
                     @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                     @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        BossDeathTimerComponent timer = store.getComponent(ref, timerType);

        if (timer != null) {
            timer.timeRemaining -= dt;

            if (timer.timeRemaining <= 0) {
                PlayerRef pRef = store.getComponent(ref, playerRefType);
                Player player = store.getComponent(ref, Player.getComponentType());

                if (pRef != null && player != null) {
                    String stats = "Deepest Layer: 1";
                    
                    try {
                        player.getPageManager().openCustomPage(ref, store, new DungeonRecapPage(pRef, stats, timer.dungeonSlot));
                        System.out.println("[DungeonMod] Timer finished! Showing Recap Page to " + pRef.getUuid());
                    } catch (Exception e) {
                        System.err.println("[DungeonMod] Error showing Recap Page: " + e.getMessage());
                    }
                }

                commandBuffer.removeComponent(ref, timerType);
            }
        }
    }

    @Nullable
    @Override
    public SystemGroup<EntityStore> getGroup() { return null; }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() { 
        return Query.and(playerRefType, timerType); 
    }
}
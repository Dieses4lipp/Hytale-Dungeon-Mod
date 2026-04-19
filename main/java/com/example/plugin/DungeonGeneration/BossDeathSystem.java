package com.example.plugin.DungeonGeneration;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;

import javax.annotation.Nonnull;

public class BossDeathSystem extends DeathSystems.OnDeathSystem {

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and();
    }

    @Override
    public void onComponentAdded(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull DeathComponent deathComponent,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        DungeonManager manager = DungeonManager.get();
        if (manager == null)
            return;

        for (DungeonInstance instance : manager.getAllActiveDungeons()) {
            if (instance.bossRef != null && instance.bossRef.equals(ref)) {

                System.out
                        .println("[DungeonMod] DEBUG: Der Boss im Dungeon (Slot " + instance.slot + ") wurde getötet!");

                Damage damageInfo = deathComponent.getDeathInfo();
                if (damageInfo != null) {
                    Damage.Source source = damageInfo.getSource();
                    Ref<EntityStore> killerRef = null;

                    if (source instanceof Damage.EntitySource) {
                        killerRef = ((Damage.EntitySource) source).getRef();
                    }

                    if (killerRef != null) {
                        PlayerRef playerRef = store.getComponent(killerRef, PlayerRef.getComponentType());

                        if (playerRef != null) {
                            int soundIndex = SoundEvent.getAssetMap().getIndex("sfx_memories_unlock_local");
                            SoundUtil.playSoundEvent2d(ref, soundIndex, SoundCategory.SFX, store);
                            
                            System.out.println(
                                    "[DungeonMod] Boss killed by player: " + playerRef.getUuid() + ". Scheduling Recap UI.");

                            BossDeathTimerComponent timerComp = new BossDeathTimerComponent();
                            timerComp.timeRemaining = 60.0f;
                            timerComp.dungeonSlot = instance.slot;
                            commandBuffer.addComponent(killerRef, BossDeathTimerComponent.getComponentType(),
                                    timerComp);
                        }
                    }
                }

                break;
            }
        }
    }
}
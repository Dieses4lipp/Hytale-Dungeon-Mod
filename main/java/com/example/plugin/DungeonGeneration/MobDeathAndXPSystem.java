package com.example.plugin.DungeonGeneration;

import com.example.plugin.Stats.PlayerLevelComponent;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class MobDeathAndXPSystem extends DeathSystems.OnDeathSystem {

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(); 
    }

    @Override
    public void onComponentAdded(
            @Nonnull Ref ref,
            @Nonnull DeathComponent deathComponent,
            @Nonnull Store store,
            @Nonnull CommandBuffer commandBuffer) {

        System.out.println("\n[DungeonMod] === MOB DEATH EVENT TRIGGERED ===");
        System.out.println("[DungeonMod] Dead Entity Ref: " + ref);

        // --- 1. IDENTIFY THE KILLER ---
        Damage damageInfo = deathComponent.getDeathInfo();
        if (damageInfo == null) {
            System.out.println("[DungeonMod] Result: No Damage info found (Entity died from something else).");
            return;
        }

        Damage.Source source = damageInfo.getSource();
        Ref killerRef = null;

        // Check if the source of the damage was an entity (melee or projectile)
        if (source instanceof Damage.EntitySource) {
            killerRef = ((Damage.EntitySource) source).getRef();
            System.out.println("[DungeonMod] Killer identified! Killer Ref: " + killerRef);
        } else {
            System.out.println("[DungeonMod] Entity died, but not by another entity. Source: " + source.getClass().getSimpleName());
        }

        // --- 2. APPLY XP TO THE PLAYER ---
        if (killerRef != null) {
            // Check if the killer actually has the level component
            if (store.getArchetype(killerRef).contains(PlayerLevelComponent.getComponentType())) {
                
                PlayerLevelComponent stats = (PlayerLevelComponent) store.getComponent(killerRef, PlayerLevelComponent.getComponentType());
                
                if (stats != null) {
                    System.out.println("[DungeonMod] Killer Stats BEFORE: Level " + stats.level + " | XP: " + stats.xp);
                    
                    stats.xp += 25; // Grant XP
                    int xpNeeded = stats.level * 100; // Next level requires (CurrentLevel * 100) XP
                    
                    System.out.println("[DungeonMod] Added 25 XP. Killer Stats AFTER: Level " + stats.level + " | XP: " + stats.xp + " / " + xpNeeded);
                    
                    if (stats.xp >= xpNeeded) {
                        stats.xp -= xpNeeded;
                        stats.level++;
                        System.out.println("[DungeonMod] *** LEVEL UP! ***");
                        System.out.println("[RPG] Player " + killerRef + " leveled up to Level " + stats.level + "! (Rollover XP: " + stats.xp + ")");
                    }
                }
            } else {
                System.out.println("[DungeonMod] WARNING: The killer (" + killerRef + ") does NOT have a PlayerLevelComponent attached! No XP given.");
            }
        }
        
        System.out.println("[DungeonMod] === END OF DEATH EVENT ===\n");
    }
}
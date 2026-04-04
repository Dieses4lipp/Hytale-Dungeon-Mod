package com.example.plugin.DungeonGeneration;

import com.example.plugin.Stats.PlayerLevelComponent;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.example.plugin.Ui.Hud.LevelHud;

import javax.annotation.Nonnull;

public class MobDeathAndXPSystem extends DeathSystems.OnDeathSystem {

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
     return Query.not(PlayerLevelComponent.getComponentType());
    }

    @Override
    public void onComponentAdded(
            @Nonnull Ref ref,
            @Nonnull DeathComponent deathComponent,
            @Nonnull Store store,
            @Nonnull CommandBuffer commandBuffer) {

        System.out.println("\n[DungeonMod] === MOB DEATH EVENT TRIGGERED ===");
        System.out.println("[DungeonMod] Dead Entity Ref: " + ref);

        Damage damageInfo = deathComponent.getDeathInfo();
        if (damageInfo == null) {
            System.out.println("[DungeonMod] Result: No Damage info found (Entity died from something else).");
            return;
        }

        Damage.Source source = damageInfo.getSource();
        Ref killerRef = null;

        if (source instanceof Damage.EntitySource) {
            killerRef = ((Damage.EntitySource) source).getRef();
            System.out.println("[DungeonMod] Killer identified! Killer Ref: " + killerRef);
        } else {
            System.out.println("[DungeonMod] Entity died, but not by another entity. Source: " + source.getClass().getSimpleName());
        }

        if (killerRef != null) {
            if (store.getArchetype(killerRef).contains(PlayerLevelComponent.getComponentType())) {
                
                PlayerLevelComponent stats = (PlayerLevelComponent) store.getComponent(killerRef, PlayerLevelComponent.getComponentType());
                
                if (stats != null) {
                    System.out.println("[DungeonMod] Killer Stats BEFORE: Level " + stats.level + " | XP: " + stats.xp);
                    
                    stats.xp += 25; 
                    int xpNeeded = stats.level * 100; 
                    
                    System.out.println("[DungeonMod] Added 25 XP. Killer Stats AFTER: Level " + stats.level + " | XP: " + stats.xp + " / " + xpNeeded);
                    
                    if (stats.xp >= xpNeeded) {
                        stats.xp -= xpNeeded;
                        stats.level++;
                        System.out.println("[DungeonMod] *** LEVEL UP! ***");
                        System.out.println("[RPG] Player " + killerRef + " leveled up to Level " + stats.level + "! (Rollover XP: " + stats.xp + ")");
                    }
                    try {
                        Player player = (Player) store.getComponent(killerRef, Player.getComponentType());
                        PlayerRef playerRef = (PlayerRef) store.getComponent(killerRef, PlayerRef.getComponentType());

                        if (player != null && playerRef != null) {
                            LevelHud refreshedHud = new LevelHud(playerRef, store, killerRef);
                            player.getHudManager().setCustomHud(playerRef, refreshedHud);
                        }
                    } catch (Exception e) {
                        System.out.println("[DungeonMod] Error refreshing HUD: " + e.getMessage());
                    }
                }
            } else {
                System.out.println("[DungeonMod] WARNING: The killer (" + killerRef + ") does NOT have a PlayerLevelComponent attached! No XP given.");
            }
        }
        
        System.out.println("[DungeonMod] === END OF DEATH EVENT ===\n");
    }
}
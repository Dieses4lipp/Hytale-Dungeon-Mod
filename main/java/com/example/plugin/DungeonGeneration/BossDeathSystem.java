package com.example.plugin.DungeonGeneration;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class BossDeathSystem extends DeathSystems.OnDeathSystem {

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        // Filtere alle Events, sodass dieses System nur bei NPCs (Tieren/Monstern) auslöst
        return Query.and();
    }

    @Override
    public void onComponentAdded(
            @Nonnull Ref ref,
            @Nonnull DeathComponent component,
            @Nonnull Store store,
            @Nonnull CommandBuffer commandBuffer) {

        DungeonManager manager = DungeonManager.get();
        if (manager == null) return;
        
        // Gehe alle aktiven Dungeons durch
        for (DungeonInstance instance : manager.getAllActiveDungeons()) {
            if (instance.bossRef != null && instance.bossRef.equals(ref)) {
                
                System.out.println("[DungeonMod] DEBUG: Der Boss im Dungeon (Slot " + instance.slot + ") wurde getötet!");
                
                // HIER kannst du später deinen Code für "Tür öffnet sich" oder "Loot droppt" einbauen.
                
                // Sobald der Boss gefunden und behandelt wurde, können wir die Schleife abbrechen
                break;
            }
        }
    }
}
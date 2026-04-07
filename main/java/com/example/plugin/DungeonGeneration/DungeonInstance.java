package com.example.plugin.DungeonGeneration;

import java.util.ArrayList;
import java.util.List;

import com.example.plugin.doorsystem.DoorNPCComponent;
import com.example.plugin.doorsystem.DoorRegistry;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.Invulnerable;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.component.RemoveReason;

public class DungeonInstance {
    public final int slot;
    public final Room[][] grid;
    public final int worldOriginX;
    public final int worldOriginZ;
    public final int spacing;
    public final int startX;
    public final int startY;
    private final List<Ref<EntityStore>> npcDoorRefs = new ArrayList<>();
    public Ref<EntityStore> bossRef = null;

    public DungeonInstance(int slot, Room[][] grid, int worldOriginX, int worldOriginZ,
            int spacing, int startX, int startY) {
        this.slot = slot;
        this.grid = grid;
        this.worldOriginX = worldOriginX;
        this.worldOriginZ = worldOriginZ;
        this.spacing = spacing;
        this.startX = startX;
        this.startY = startY;
    }

    public void registerNPC(Ref<EntityStore> ref) {
        npcDoorRefs.add(ref);
    }

    public List<Ref<EntityStore>> spawnedEnemies = new ArrayList<>();

    public void cleanup(Store<EntityStore> store, CommandBuffer commandBuffer) {
        
        java.util.Set<Ref<EntityStore>> entitiesToDelete = new java.util.HashSet<>();

        for (Ref<EntityStore> ref : npcDoorRefs) {
            if (ref != null && ref.isValid()) {
                try {
                    DoorNPCComponent doorData = store.getComponent(ref, DoorNPCComponent.getComponentType());
                    if (doorData != null) {
                        DoorRegistry.remove(doorData.getDoorPos());
                    }
                    entitiesToDelete.add(ref); 

                } catch (Exception e) {
                    System.out.println("[DungeonInstance] Error removing Door NPC: " + e.getMessage());
                }
            }
        }
        npcDoorRefs.clear();

        // 2. Clean up Enemies
        for (Ref<EntityStore> enemyRef : spawnedEnemies) {
            if (enemyRef != null && enemyRef.isValid()) {
                entitiesToDelete.add(enemyRef);
            }
        }
        spawnedEnemies.clear();

        // 3. Clean up Boss
        if (bossRef != null && bossRef.isValid()) {
            entitiesToDelete.add(bossRef); 
            bossRef = null;
        }

        for (Ref<EntityStore> entity : entitiesToDelete) {
            if (commandBuffer != null) {
                commandBuffer.removeEntity(entity, RemoveReason.REMOVE); 
            } else {
                store.removeEntity(entity, RemoveReason.REMOVE); 
            }
        }

        System.out.println("[DungeonInstance] Cleanup finished. Entities silently despawned: " + entitiesToDelete.size());
    }

    public int getId() {
        return slot;
    }

    public Room[][] getGrid() {
        return grid;
    }
}
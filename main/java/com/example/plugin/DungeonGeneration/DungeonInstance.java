package com.example.plugin.DungeonGeneration;

import java.util.ArrayList;
import java.util.List;

import com.example.plugin.doorsystem.DoorNPCComponent;
import com.example.plugin.doorsystem.DoorRegistry;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
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

    public void cleanup(Store<EntityStore> store) {
        System.out.println("[DungeonInstance] Starting cleanup for slot " + slot);

        for (Ref<EntityStore> ref : npcDoorRefs) {
            if (ref.isValid()) {
                try {
                    DoorNPCComponent doorData = store.getComponent(ref, DoorNPCComponent.getComponentType());
                    if (doorData != null) {
                        DoorRegistry.remove(doorData.getDoorPos());
                    }
                    store.removeEntity(ref, RemoveReason.REMOVE);
                } catch (Exception e) {
                    System.out.println("[DungeonInstance] Error removing Door NPC: " + e.getMessage());
                }
            }
        }
        npcDoorRefs.clear();

        int mobsCleared = 0;
        for (Ref<EntityStore> enemyRef : spawnedEnemies) {
            if (enemyRef.isValid()) {
                store.removeEntity(enemyRef, RemoveReason.REMOVE);
                mobsCleared++;
            }
        }
        spawnedEnemies.clear();
        
        System.out.println("[DungeonInstance] Cleanup finished. Mobs removed: " + mobsCleared);
    }

    public int getId() {
        return slot;
    }

    public Room[][] getGrid() {
        return grid;
    }
}
package com.example.plugin.DungeonGeneration;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.worldmap.MapImage;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarkerComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MapMarkerBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DungeonManager {
    private static DungeonManager instance;

    public static DungeonManager get() {
        return instance;
    }

    public Map<Ref<EntityStore>, Integer> mobXpRewards = new ConcurrentHashMap<>();
    private final int gridsize = DungeonConfig.get().layout.gridsize;
    private final int spacing = DungeonConfig.get().manager.spacing;
    private final int slotSize = gridsize * spacing;
    private final int slotsPerRow = DungeonConfig.get().manager.slotsPerRow;
    public World activeWorld;
    private int nextSlot = 1; // so the first slot is empty for the lobby
    private final Queue<Integer> freeSlots = new LinkedList<>();
    private final Map<Integer, DungeonInstance> activeBySlot = new HashMap<>();

    public DungeonManager() {
        instance = this;
    }

    public DungeonInstance createDungeon(World world, int roomCount, Store<EntityStore> store) {
        this.activeWorld = world;

        int slot = freeSlots.isEmpty() ? nextSlot++ : freeSlots.poll();
        if (slot == -1)
            slot = nextSlot++;
        if (slot == 0)
            slot = nextSlot++;
        int slotCol = slot % slotsPerRow;
        int slotRow = slot / slotsPerRow;
        int originX = slotCol * slotSize;
        int originZ = slotRow * slotSize;

        System.out.println("[DungeonManager] Spawning dungeon in slot ("
                + slotCol + "," + slotRow + ") at world (" + originX + ", " + originZ + ")");

        LayoutGenerator layout = new LayoutGenerator();
        layout.generateLayout(roomCount);

        DungeonInstance inst = new DungeonInstance(
                slot, layout.getGrid(), originX, originZ,
                spacing, layout.getStartX(), layout.getStartY());
        activeBySlot.put(slot, inst);

        DungeonGenerator generator = new DungeonGenerator();
        generator.generate(world, inst, store);
        EnemySpawner.populateDungeon(world, inst, store);
        return inst;
    }

    public void destroyDungeon(Store<EntityStore> store, CommandBuffer commandBuffer, World world,
            DungeonInstance inst) {
        inst.cleanup(store, commandBuffer);
        DungeonGenerator generator = new DungeonGenerator();
        generator.clearDungeon(world, inst);
        activeBySlot.remove(inst.slot);
        freeSlots.add(inst.slot);
        System.out.println("[DungeonManager] Destroyed dungeon in slot " + inst.slot);
    }

    public DungeonInstance getBySlot(int slot) {
        return activeBySlot.get(slot);
    }

    public Collection<DungeonInstance> getAllActiveDungeons() {
        return activeBySlot.values();
    }
}
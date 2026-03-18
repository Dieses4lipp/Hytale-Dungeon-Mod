package com.example.plugin.DungeonGeneration;

import com.hypixel.hytale.server.core.universe.world.World;
import java.util.*;

public class DungeonManager {
    private static DungeonManager instance;

    public static DungeonManager get() {
        return instance;
    }

    private final int gridsize = 20;
    private final int abstand = 11;
    private final int slotSize = gridsize * abstand; // 220 blocks
    private final int slotsPerRow = 10;

    private int nextSlot = 0;
    private final Queue<Integer> freeSlots = new LinkedList<>();
    private final Map<Integer, DungeonInstance> activeBySlot = new HashMap<>();

    public DungeonManager() {
        instance = this;
    }

    public DungeonInstance createDungeon(World world, int roomCount) {
        int slot = freeSlots.isEmpty() ? nextSlot++ : freeSlots.poll();

        int slotCol = slot % slotsPerRow;
        int slotRow = slot / slotsPerRow;
        int originX = slotCol * slotSize;
        int originZ = slotRow * slotSize;

        System.out.println("[DungeonManager] Spawning dungeon in slot ("
                + slotCol + "," + slotRow + ") at world (" + originX + ", " + originZ + ")");

        LayoutGenerator layout = new LayoutGenerator();
        layout.generateLayout(roomCount);

        DungeonGenerator generator = new DungeonGenerator();
        generator.generate(world, layout.getGrid(), originX, originZ);

        DungeonInstance inst = new DungeonInstance(
                slot, layout.getGrid(), originX, originZ,
                abstand, layout.getStartX(), layout.getStartY());
        activeBySlot.put(slot, inst);
        return inst;
    }

    public void destroyDungeon(World world, DungeonInstance inst) {
        DungeonGenerator generator = new DungeonGenerator();
        generator.clearDungeon(world, inst);
        activeBySlot.remove(inst.slot);
        freeSlots.add(inst.slot);
        System.out.println("[DungeonManager] Destroyed dungeon in slot " + inst.slot);
    }

    public DungeonInstance getBySlot(int slot) {
        return activeBySlot.get(slot);
    }
}
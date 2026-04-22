package com.example.plugin;

import com.example.plugin.DatabaseManager;
import com.example.plugin.DungeonGeneration.DungeonConfig;
import com.example.plugin.Npc.Shopinteraction.NPCSetupPending;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import it.unimi.dsi.fastutil.Pair;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class DungeonStartupSystem {
    private static boolean hasRun = false;

    public static void handleStartup(HelloPlugin plugin, PlayerReadyEvent event) {
        if (hasRun)
            return;
        hasRun = true;

        World world = event.getPlayer().getWorld();
        Store<EntityStore> store = event.getPlayerRef().getStore();


        // 1. Clear orphaned dungeon slots
        List<Integer> activeSlots = DatabaseManager.getActiveDungeons();
        if (!activeSlots.isEmpty()) {
            int spacing = DungeonConfig.get().manager.spacing;
            int slotsPerRow = DungeonConfig.get().manager.slotsPerRow;
            int gridsize = DungeonConfig.get().layout.gridsize;
            int slotSize = gridsize * spacing;
            int clearYMin = DungeonConfig.get().generator.clearYMin;
            int clearYMax = DungeonConfig.get().generator.clearYMax;

            for (int slot : activeSlots) {
                int originX = (slot % slotsPerRow) * slotSize;
                int originZ = (slot / slotsPerRow) * slotSize;

                for (int dx = 0; dx < slotSize; dx++) {
                    for (int dz = 0; dz < slotSize; dz++) {
                        for (int dy = clearYMin; dy <= clearYMax; dy++) {
                            world.setBlock(originX + dx, dy, originZ + dz, "Empty");
                        }
                    }
                }
            }
            DatabaseManager.clearAllActiveDungeons();
        }

        // 2. Remove old hub NPCs by UUID
        AtomicInteger removedNpcCount = new AtomicInteger(0);
        List<String> hubNpcUuids = DatabaseManager.getHubNpcUuids();

        if (!hubNpcUuids.isEmpty()) {
            store.forEachChunk(
                    (BiConsumer<ArchetypeChunk<EntityStore>, CommandBuffer<EntityStore>>) (chunk, cmd) -> {
                        for (int i = 0; i < chunk.size(); i++) {
                            Ref<EntityStore> ref = chunk.getReferenceTo(i);
                            if (ref.equals(event.getPlayerRef()))
                                continue;

                            UUIDComponent uuidComp = chunk.getComponent(i, UUIDComponent.getComponentType());
                            if (uuidComp != null && hubNpcUuids.contains(uuidComp.getUuid().toString())) {
                                cmd.removeEntity(ref, RemoveReason.REMOVE);
                                removedNpcCount.incrementAndGet();
                            }
                        }
                    });
        }

        DatabaseManager.clearHubNpcUuids();

        if (!activeSlots.isEmpty()) {
            AtomicInteger removedDungeonEntityCount = new AtomicInteger(0);

            int spacing = DungeonConfig.get().manager.spacing;
            int slotsPerRow = DungeonConfig.get().manager.slotsPerRow;
            int gridsize = DungeonConfig.get().layout.gridsize;
            int slotSize = gridsize * spacing;

            store.forEachChunk(
                    (BiConsumer<ArchetypeChunk<EntityStore>, CommandBuffer<EntityStore>>) (chunk, cmd) -> {
                        for (int i = 0; i < chunk.size(); i++) {
                            Ref<EntityStore> ref = chunk.getReferenceTo(i);
                            if (ref.equals(event.getPlayerRef()))
                                continue;

                            TransformComponent transformComp = chunk.getComponent(i,
                                    TransformComponent.getComponentType());
                            if (transformComp == null)
                                continue;

                            Vector3d pos = transformComp.getPosition();

                            for (int slot : activeSlots) {
                                int originX = (slot % slotsPerRow) * slotSize;
                                int originZ = (slot / slotsPerRow) * slotSize;

                                if (pos.x >= originX && pos.x <= (originX + slotSize) &&
                                        pos.z >= originZ && pos.z <= (originZ + slotSize)) {
                                    cmd.removeEntity(ref, RemoveReason.REMOVE);
                                    removedDungeonEntityCount.incrementAndGet();
                                    break;
                                }
                            }
                        }
                    });

        }

        spawnSetupNPCs(world, store);

    }

    private static void spawnSetupNPCs(World world, Store<EntityStore> store) {
        Vector3f npcRot = new Vector3f(0, 0, 0);
        Vector3d playPos = new Vector3d(109.5, 135.0, 112.5);
        Pair<Ref<EntityStore>, INonPlayerCharacter> playResult = NPCPlugin.get().spawnNPC(
                store, "Temple_Mithril_Guard", null, playPos, npcRot);
        if (playResult != null) {
            Ref<EntityStore> playRef = playResult.first();
            store.removeComponent(playRef, DisplayNameComponent.getComponentType());
            store.putComponent(playRef, Nameplate.getComponentType(), new Nameplate("Enter Dungeon"));
            store.addComponent(playRef, NPCSetupPending.getComponentType(),
                    new NPCSetupPending("Root_OpenPlay", "Enter"));
            saveNpcUuid(store, playRef, "Play-NPC");
        }

        // Stash NPC
        Vector3d stashPos = new Vector3d(111.5, 135.0, 112.5);
        Pair<Ref<EntityStore>, INonPlayerCharacter> stashResult = NPCPlugin.get().spawnNPC(
                store, "Temple_Kweebec_Rootling_Static", null, stashPos, npcRot);
        if (stashResult != null) {
            Ref<EntityStore> stashRef = stashResult.first();
            store.removeComponent(stashRef, DisplayNameComponent.getComponentType());
            store.putComponent(stashRef, Nameplate.getComponentType(), new Nameplate("Deposit Items"));
            store.addComponent(stashRef, NPCSetupPending.getComponentType(),
                    new NPCSetupPending("Root_DepositToStash", "Deposit"));
            saveNpcUuid(store, stashRef, "Stash-NPC");
        }
    }

    private static void saveNpcUuid(Store<EntityStore> store, Ref<EntityStore> ref, String label) {
        UUIDComponent uuidComp = store.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComp != null) {
            String uuid = uuidComp.getUuid().toString();
            DatabaseManager.saveHubNpcUuid(uuid);
        } else {
            System.err.println("[Startup] Could not read UUID for " + label + "!");
        }
    }
}
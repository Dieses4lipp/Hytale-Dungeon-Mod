package com.example.plugin.Commands;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import com.example.plugin.DungeonGeneration.DungeonInstance;
import com.example.plugin.DungeonGeneration.DungeonManager;
import com.example.plugin.Npc.Shopinteraction.NPCSetupPending;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;

import it.unimi.dsi.fastutil.Pair;

public class SetupHubCommand extends AbstractPlayerCommand {

    public SetupHubCommand() {
        super("setuphub", "Clears old dungeons and spawns the main Hub NPCs.", false);
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                           @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref,
                           @Nonnull PlayerRef playerRef,
                           @Nonnull World world) {

        playerRef.sendMessage(Message.raw("Starting Hub Setup..."));
        System.out.println("[HubSetup] Cleaning up old data...");

        DungeonManager manager = DungeonManager.get();
        if (manager != null) {
            List<DungeonInstance> activeDungeons = new ArrayList<>(manager.getAllActiveDungeons());
            for (DungeonInstance inst : activeDungeons) {
                System.out.println("[HubSetup] Destroying leftover dungeon slot " + inst.slot);
                manager.destroyDungeon(store, null, world, inst); 
            }
            playerRef.sendMessage(Message.raw("Cleared " + activeDungeons.size() + " leftover dungeons."));
        }

        Vector3f npcRot = new Vector3f(0, 0, 0);

        Vector3d playPos = new Vector3d(109.5, 135.0, 112.5);
        Pair<Ref<EntityStore>, INonPlayerCharacter> playResult = 
            NPCPlugin.get().spawnNPC(store, "Temple_Mithril_Guard", null, playPos, npcRot);

        if (playResult != null) {
            Ref<EntityStore> playRef = playResult.first();
            store.removeComponent(playRef, DisplayNameComponent.getComponentType());
            store.putComponent(playRef, Nameplate.getComponentType(), new Nameplate("Enter Dungeon"));
            store.addComponent(playRef, NPCSetupPending.getComponentType(), new NPCSetupPending("Root_OpenPlay", "Enter"));
        }

        Vector3d stashPos = new Vector3d(111.5, 135.0, 112.5);
        Pair<Ref<EntityStore>, INonPlayerCharacter> stashResult = 
            NPCPlugin.get().spawnNPC(store, "Temple_Kweebec_Rootling_Static", null, stashPos, npcRot);

        if (stashResult != null) {
            Ref<EntityStore> stashRef = stashResult.first();
            store.removeComponent(stashRef, DisplayNameComponent.getComponentType());
            store.putComponent(stashRef, Nameplate.getComponentType(), new Nameplate("Deposit Items"));
            store.addComponent(stashRef, NPCSetupPending.getComponentType(), new NPCSetupPending("Root_DepositToStash", "Deposit"));
        }

        playerRef.sendMessage(Message.raw("Hub Setup Complete! NPCs spawned at coordinates."));
        System.out.println("[HubSetup] Setup successfully finished.");
    }
}
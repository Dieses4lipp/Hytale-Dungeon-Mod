package com.example.plugin.Commands;
import javax.annotation.Nonnull;

import com.hypixel.hytale.builtin.path.PrefabPathSystems.NameplateHolderSystem;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.entity.component.Invulnerable;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;

import it.unimi.dsi.fastutil.Pair;

public class SpawnNPCCommand extends AbstractPlayerCommand {

    public SpawnNPCCommand() {
        super("spawnnpc", "Spawns an interactable NPC at your location");
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                          @Nonnull Store<EntityStore> store,
                          @Nonnull Ref<EntityStore> ref,
                          @Nonnull PlayerRef playerRef,
                          @Nonnull World world) {

        Vector3d playerPos = playerRef.getTransform().getPosition();
        Vector3f playerRot = playerRef.getTransform().getRotation();

        // Spawn 2 blocks in front of player
        double offsetX = -Math.sin(Math.toRadians(playerRot.y)) * 2.0;
        double offsetZ = Math.cos(Math.toRadians(playerRot.y)) * 2.0;
        Vector3d spawnPos = new Vector3d(
            playerPos.x + offsetX,
            playerPos.y,
            playerPos.z + offsetZ
        );

        Vector3f npcRotation = new Vector3f(0, playerRot.y + 180, 0);

        // Spawn the NPC
        Pair<Ref<EntityStore>, INonPlayerCharacter> result = NPCPlugin.get().spawnNPC(
            store,
            "Goblin_Ogre",
            null,
            spawnPos,
            npcRotation
        );

        if (result == null) {
            playerRef.sendMessage(Message.raw("Failed to spawn NPC!"));
            return;
        }

        Ref<EntityStore> npcRef = result.first();
        store.removeComponent(npcRef, DisplayNameComponent.getComponentType());
        store.addComponent(npcRef, Nameplate.getComponentType(), new Nameplate("ColinsFetteMutter"));
        
        NPCSetupPending setupPending = new NPCSetupPending("Root_TalkToNPC", "Talk");
        
        store.addComponent(npcRef, NPCSetupPending.getComponentType(), setupPending);
        store.addComponent(npcRef, Invulnerable.getComponentType());
        
        
        
        playerRef.sendMessage(Message.raw("Spawned interactable NPC!"));
        playerRef.sendMessage(Message.raw("Right-click or press E to interact."));
    }
}
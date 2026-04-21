package com.example.plugin.Commands;

import javax.annotation.Nonnull;

import com.example.plugin.Npc.Playinteraction.OpenPlayPageInteraction;
import com.example.plugin.Npc.Shopinteraction.NPCSetupPending;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.entity.component.Invulnerable;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;

import it.unimi.dsi.fastutil.Pair;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;

public class SpawnPlayNPCCommand extends AbstractPlayerCommand {

    public SpawnPlayNPCCommand() {
        super("spawnplaynpc", "Spawns the Play Page NPC at your location.", false);
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext,
                           @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref,
                           @Nonnull PlayerRef playerRef,
                           @Nonnull World world) {

        System.out.println("Spawning Play NPC...");

        Vector3d pos = playerRef.getTransform().getPosition();
        Vector3f rot = playerRef.getTransform().getRotation();

        double offsetX = -Math.sin(Math.toRadians(rot.y)) * 2.0;
        double offsetZ =  Math.cos(Math.toRadians(rot.y)) * 2.0;
        Vector3d spawnPos = new Vector3d(pos.x + offsetX, pos.y, pos.z + offsetZ);
        Vector3f npcRot = new Vector3f(0, rot.y + 180, 0);

        Pair<Ref<EntityStore>, INonPlayerCharacter> result =
            NPCPlugin.get().spawnNPC(store, "Temple_Mithril_Guard", null, spawnPos, npcRot);

        if (result == null) {
            System.out.println("Failed to spawn Play NPC!");
            return;
        }

        Ref<EntityStore> npcRef = result.first();
        store.removeComponent(npcRef, DisplayNameComponent.getComponentType());
        store.putComponent(npcRef, Nameplate.getComponentType(), new Nameplate("Enter Dungeon"));

        NPCSetupPending setupPending = new NPCSetupPending("Root_OpenPlay", "Enter");
        store.addComponent(npcRef, NPCSetupPending.getComponentType(), setupPending);

        System.out.println("Play NPC spawned successfully.");
    }
}
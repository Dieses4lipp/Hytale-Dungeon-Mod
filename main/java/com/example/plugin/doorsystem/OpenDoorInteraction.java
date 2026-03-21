package com.example.plugin.doorsystem;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.nio.file.Path;

public class OpenDoorInteraction extends SimpleInstantInteraction {

    public static final BuilderCodec<OpenDoorInteraction> CODEC = BuilderCodec.builder(
        OpenDoorInteraction.class,
        OpenDoorInteraction::new,
        SimpleInstantInteraction.CODEC
    ).build();

    private static final Path DOOR_WE_OPEN = Path.of("prefabs/Prefabs/Door/door_we_open.prefab.json");
    private static final Path DOOR_SN_OPEN = Path.of("prefabs/Prefabs/Door/door_sn_open.prefab.json");

    @Override
    protected void firstRun(@Nonnull InteractionType interactionType,
                            @Nonnull InteractionContext interactionContext,
                            @Nonnull CooldownHandler cooldownHandler) {

        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();
        if (commandBuffer == null) {
            interactionContext.getState().state = InteractionState.Failed;
            return;
        }

        
        Ref<EntityStore> npcRef = interactionContext.getTargetEntity();

        DoorNPCComponent doorData = commandBuffer.getComponent(npcRef, DoorNPCComponent.getComponentType());
        if (doorData == null) {
            System.out.println("[DoorSystem] ERROR: NPC has no DoorNPCComponent");
            interactionContext.getState().state = InteractionState.Failed;
            return;
        }

        World world = DoorRegistry.getWorld();
        if (world == null) {
            System.out.println("[DoorSystem] ERROR: World is null in registry");
            interactionContext.getState().state = InteractionState.Failed;
            return;
        }

        Vector3i pos = doorData.getDoorPos();
        Path openPath = doorData.getOrientation() == DoorRegistry.Orientation.WE ? DOOR_WE_OPEN : DOOR_SN_OPEN;
        BlockSelection openPrefab = PrefabStore.get().getPrefab(openPath);


        openPrefab.placeNoReturn(world, pos, null);
        System.out.println("[DoorSystem] Door opened at " + pos.x + "," + pos.y + "," + pos.z);

        // Despawn the NPC now the door is open
        commandBuffer.removeEntity(npcRef, RemoveReason.REMOVE); 
        DoorRegistry.remove(pos);

        interactionContext.getState().state = InteractionState.Finished;
    }
}
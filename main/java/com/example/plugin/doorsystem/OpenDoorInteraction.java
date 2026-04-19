package com.example.plugin.doorsystem;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
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
        if (doorData.isOpening) {
            interactionContext.getState().state = InteractionState.Failed;
            return;
        }
        doorData.isOpening = true;
        World world = com.example.plugin.DungeonGeneration.DungeonManager.get().activeWorld;
        if (world == null) {
            System.out.println("[DoorSystem] ERROR: World is null in DoorNPCComponent");
            interactionContext.getState().state = InteractionState.Failed;
            return;
        }
        Ref<EntityStore> playerRef = interactionContext.getEntity();
        Store<EntityStore> store = world.getEntityStore().getStore();
        int soundIndex = SoundEvent.getAssetMap().getIndex("sfx_door_crude_open");
        SoundUtil.playSoundEvent2d(playerRef, soundIndex, SoundCategory.SFX, store);
        
        Vector3i clickedPos = doorData.getDoorPos();

        java.util.List<Vector3i> nearbyDoors = DoorRegistry.getNearby(clickedPos, 2);
        boolean clickedNpcDeleted = false;

        for (Vector3i doorPos : nearbyDoors) {
            DoorRegistry.DoorEntry entry = DoorRegistry.get(doorPos);
            if (entry == null) continue;

            Path openPath = entry.orientation == DoorRegistry.Orientation.WE ? DOOR_WE_OPEN : DOOR_SN_OPEN;
            BlockSelection openPrefab = PrefabStore.get().getPrefab(openPath);
            openPrefab.placeNoReturn(world, doorPos, null);
            System.out.println("[DoorSystem] Double-Door part opened at " + doorPos.x + "," + doorPos.y + "," + doorPos.z);

            if (entry.entityRef != null && entry.entityRef.isValid()) {
                commandBuffer.removeEntity(entry.entityRef, RemoveReason.REMOVE);
                if (entry.entityRef.equals(npcRef)) clickedNpcDeleted = true;
            }

            DoorRegistry.remove(doorPos);
        }

        if (!clickedNpcDeleted) {
            commandBuffer.removeEntity(npcRef, RemoveReason.REMOVE);
        }

        interactionContext.getState().state = InteractionState.Finished;
    }
}
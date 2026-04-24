package com.example.plugin.Npc.Playinteraction;

import javax.annotation.Nonnull;

import com.example.plugin.Ui.PlayPage.PlayPage;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.AttachedToType;
import com.hypixel.hytale.protocol.ClientCameraView;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.MouseInputTargetType;
import com.hypixel.hytale.protocol.MouseInputType;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.PositionDistanceOffsetType;
import com.hypixel.hytale.protocol.RotationType;
import com.hypixel.hytale.protocol.ServerCameraSettings;
import com.hypixel.hytale.protocol.Vector2f;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class OpenPlayPageInteraction extends SimpleInstantInteraction {

    public static final BuilderCodec<OpenPlayPageInteraction> CODEC = BuilderCodec.builder(
        OpenPlayPageInteraction.class,
        OpenPlayPageInteraction::new,
        SimpleInstantInteraction.CODEC
    ).build();

    @Override
    protected void firstRun(@Nonnull InteractionType interactionType,
                           @Nonnull InteractionContext interactionContext,
                           @Nonnull CooldownHandler cooldownHandler) {

        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();
        if (commandBuffer == null) {
            interactionContext.getState().state = InteractionState.Failed;
            return;
        }

        Ref<EntityStore> playerEntityRef = interactionContext.getEntity();
        Player player = commandBuffer.getComponent(playerEntityRef, Player.getComponentType());

        if (player == null) {
            interactionContext.getState().state = InteractionState.Failed;
            return;
        }

        PlayerRef playerRef = player.getPlayerRef();
        World world = player.getWorld();
        Store<EntityStore> store = world.getEntityStore().getStore();

        LineUpCameraForCamModel(commandBuffer, playerEntityRef, playerRef);

        PlayPage page = new PlayPage(playerRef, world);
        player.getPageManager().openCustomPage(playerEntityRef, store, page);
        
        interactionContext.getState().state = InteractionState.Finished;
    }

    private void LineUpCameraForCamModel(CommandBuffer<EntityStore> commandBuffer, Ref<EntityStore> ref, PlayerRef playerRef) {
        float f3Pitch = (float) Math.toRadians(-11.9);
        float f3Yaw = (float) Math.toRadians(118.2);
        float f3Roll = 0.0f;
        
        TransformComponent transformComp = commandBuffer.getComponent(ref, TransformComponent.getComponentType());
        if (transformComp == null) return;
        
        Vector3d currentPosition = transformComp.getPosition();
        Vector3f newLookDirection = new Vector3f(f3Pitch, f3Yaw, f3Roll);

        Teleport teleportComponent = Teleport.createForPlayer(currentPosition, newLookDirection);
        commandBuffer.addComponent(ref, Teleport.getComponentType(), teleportComponent);

        ServerCameraSettings camSettings = new ServerCameraSettings();
        camSettings.isFirstPerson = false;
        camSettings.distance = 1.4f;
        camSettings.positionOffset = new Position(-0.8, -0.7, 0);
        camSettings.positionLerpSpeed = 0.15f;
        camSettings.rotationLerpSpeed = 0.15f;
        camSettings.attachedToType = AttachedToType.LocalPlayer;
        camSettings.rotationType = RotationType.AttachedToPlusOffset;
        camSettings.rotationOffset = new Direction((float) Math.PI, 0.7f, 0.3f);
        camSettings.allowPitchControls = false;
        camSettings.sendMouseMotion = false;
        camSettings.mouseInputType = MouseInputType.LookAtTargetEntity;
        camSettings.displayCursor = true;
        camSettings.displayReticle = false;
        camSettings.lookMultiplier = new Vector2f(0.0f, 0.0f);
        camSettings.mouseInputTargetType = MouseInputTargetType.None;
        camSettings.skipCharacterPhysics = false;
        camSettings.eyeOffset = true;
        camSettings.positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffsetRaycast;

        playerRef.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.Custom, false, camSettings));
    }
}
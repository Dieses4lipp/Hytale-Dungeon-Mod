package com.example.plugin.Commands;

import javax.annotation.Nonnull;

import com.example.plugin.Ui.PlayPage.PlayPage;
import com.hypixel.hytale.builtin.adventure.camera.CameraPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.ApplyLookType;
import com.hypixel.hytale.protocol.ClientCameraView;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.MouseInputType;
import com.hypixel.hytale.protocol.PositionDistanceOffsetType;
import com.hypixel.hytale.protocol.RotationType;
import com.hypixel.hytale.protocol.ServerCameraSettings;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.server.core.asset.type.model.config.camera.CameraSettings;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class OpenPlayPageCommand extends AbstractPlayerCommand {

    public OpenPlayPageCommand() {
        super("playpage", "Opens the fullscreen play UI", false);
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world) {
        Player player = store.getComponent(ref, Player.getComponentType());
        PlayPage page = new PlayPage(playerRef, world);

        ServerCameraSettings camSettings = new ServerCameraSettings();
        // Third-person, fixed front view
camSettings.isFirstPerson = false;
camSettings.distance = 4.0f;
camSettings.positionLerpSpeed = 0.15f;
camSettings.rotationLerpSpeed = 0.15f;

// Lock rotation to a custom fixed angle
camSettings.rotationType = RotationType.Custom;
camSettings.rotationOffset = new Direction(-0.2f, (float) Math.PI, 0.0f);

camSettings.allowPitchControls = false;
camSettings.sendMouseMotion = false;
camSettings.mouseInputType = MouseInputType.LookAtTarget;

// Cursor for UI, no combat reticle
camSettings.displayCursor = true;
camSettings.displayReticle = false;

// Don't skip physics — that was causing the float
camSettings.skipCharacterPhysics = false;

// Eye-level orbit point so the character is properly centered
camSettings.eyeOffset = true;
camSettings.positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffsetRaycast;

playerRef.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.Custom, false, camSettings));
player.getPageManager().openCustomPage(ref, store, page);
    }
}

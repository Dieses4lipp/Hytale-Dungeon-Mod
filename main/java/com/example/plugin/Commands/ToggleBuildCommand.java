package com.example.plugin.Commands;

import javax.annotation.Nonnull;
import java.util.UUID;

import com.example.plugin.DatabaseManager;
import com.example.plugin.System.BuildPermissionComponent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class ToggleBuildCommand extends AbstractPlayerCommand {

    public ToggleBuildCommand() {
        super("build", "Schaltet deinen Baumodus an oder aus", true);
    }

    @Override
    protected void execute(
            @Nonnull CommandContext ctx,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world) {

        String uuid = playerRef.getUuid().toString();

        boolean canCurrentlyBuild = store.getComponent(ref, BuildPermissionComponent.getComponentType()) != null;

        if (canCurrentlyBuild) {
            store.removeComponent(ref, BuildPermissionComponent.getComponentType());
            
            DatabaseManager.setBuildPermission(uuid, false);
            
            playerRef.sendMessage(Message.raw("Baumodus deaktiviert. Du kannst keine Blöcke mehr abbauen."));
        } else {
            store.addComponent(ref, BuildPermissionComponent.getComponentType(), new BuildPermissionComponent());
            
            DatabaseManager.setBuildPermission(uuid, true);
            
            playerRef.sendMessage(Message.raw("Baumodus aktiviert. Du kannst jetzt Blöcke abbauen."));
        }
    }
}
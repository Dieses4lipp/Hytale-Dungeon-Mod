package com.example.plugin.Commands;

import com.example.plugin.DungeonLayoutGenerator;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;

import javax.annotation.Nonnull;

public class GenerateDungeonCommand extends AbstractPlayerCommand {

    public GenerateDungeonCommand(@Nonnull String name, @Nonnull String description, boolean requiresConfirmation) {
        super(name, description, requiresConfirmation);
    }

    @Override
    protected void execute(
            @Nonnull CommandContext commandContext,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        
        // Show success message to player
        EventTitleUtil.showEventTitleToPlayer(
                playerRef,
                Message.raw("Dungeon Built!"),
                Message.raw("Dungeon Built!"),
                true
        );
        DungeonLayoutGenerator dungeonGenerator = new DungeonLayoutGenerator();
        dungeonGenerator.generateLayout();
        dungeonGenerator.generate(world);
    }
}

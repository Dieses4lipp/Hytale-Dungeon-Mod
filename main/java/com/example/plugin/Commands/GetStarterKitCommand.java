package com.example.plugin.Commands;

import com.example.plugin.DungeonGeneration.DungeonConfig;
import com.example.plugin.DungeonGeneration.DungeonGenerator;
import com.example.plugin.DungeonGeneration.DungeonInstance;
import com.example.plugin.DungeonGeneration.DungeonManager;
import com.example.plugin.DungeonGeneration.LayoutGenerator;
import com.example.plugin.System.StarterKit;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import javax.annotation.Nonnull;

public class GetStarterKitCommand extends AbstractPlayerCommand {

    public GetStarterKitCommand(@Nonnull String name, @Nonnull String description, boolean requiresConfirmation) {
        super(name, description, requiresConfirmation);
    }

    @Override
    protected void execute(
            @Nonnull CommandContext commandContext,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world) {
                
         com.hypixel.hytale.server.core.entity.entities.Player player = (com.hypixel.hytale.server.core.entity.entities.Player) store
                .getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
                
       StarterKit.giveStarterKit(player);

    }
}

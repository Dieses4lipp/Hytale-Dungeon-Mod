package com.example.plugin.Commands;

import javax.annotation.Nonnull;

import com.example.plugin.DungeonGeneration.DungeonInstance;
import com.example.plugin.DungeonGeneration.DungeonManager;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class DestroyDungeonCommand extends AbstractPlayerCommand {

    public DestroyDungeonCommand() {
        super("destroydungeon", "Destroys a dungeon by slot number", false);
    }

    private final RequiredArg<Integer> slotArg =
        withRequiredArg("slot", "Slot", ArgTypes.INTEGER);

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                           @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref,
                           @Nonnull PlayerRef playerRef,
                           @Nonnull World world) {

        int slot = slotArg.get(ctx);
        DungeonInstance inst = DungeonManager.get().getBySlot(slot);

        if (inst == null) {
            playerRef.sendMessage(Message.raw("No dungeon found in slot " + slot));
            return;
        }

        DungeonManager.get().destroyDungeon(store, null ,world, inst);
        playerRef.sendMessage(Message.raw("Dungeon slot " + slot + " destroyed."));
    }


}

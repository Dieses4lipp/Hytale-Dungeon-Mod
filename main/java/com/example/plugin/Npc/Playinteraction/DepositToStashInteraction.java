package com.example.plugin.Npc.Playinteraction;

import javax.annotation.Nonnull;

import com.example.plugin.Ui.PlayPage.StashPage;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;

public class DepositToStashInteraction extends SimpleInstantInteraction {

    public static final BuilderCodec<DepositToStashInteraction> CODEC = BuilderCodec.builder(
            DepositToStashInteraction.class,
            DepositToStashInteraction::new,
            SimpleInstantInteraction.CODEC).build();

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

        StashPage page = new StashPage(playerRef, world);
        player.getPageManager().openCustomPage(playerEntityRef, store, page);
        
        interactionContext.getState().state = InteractionState.Finished;

        interactionContext.getState().state = InteractionState.Finished;
    }
}

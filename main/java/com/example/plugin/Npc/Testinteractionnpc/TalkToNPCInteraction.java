package com.example.plugin.Npc.Testinteractionnpc;

import java.util.Random;

import javax.annotation.Nonnull;

import com.example.plugin.Ui.Shop.ShopPage;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class TalkToNPCInteraction extends SimpleInstantInteraction {

    public static final BuilderCodec<TalkToNPCInteraction> CODEC = BuilderCodec.builder(
        TalkToNPCInteraction.class,
        TalkToNPCInteraction::new,
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

        Ref<EntityStore> ref = interactionContext.getEntity();
        Player player = commandBuffer.getComponent(ref, Player.getComponentType());

        if (player == null) {
            interactionContext.getState().state = InteractionState.Failed;
            return;
        }

        PlayerRef playerRef = player.getPlayerRef();

        Store<EntityStore> store = player.getWorld().getEntityStore().getStore();

        ShopPage page = new ShopPage(playerRef);
        player.getPageManager().openCustomPage(ref, store, page);
        interactionContext.getState().state = InteractionState.Finished;
    }
}
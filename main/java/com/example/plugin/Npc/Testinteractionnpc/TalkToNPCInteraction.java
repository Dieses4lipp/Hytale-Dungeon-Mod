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

    private static final String[] DIALOG_LINES = {
        "Hello, traveler! Welcome to our village.",
        "Have you seen any Trorks around lately?",
        "The weather in Zone 1 is lovely this time of year.",
        "I heard there's treasure hidden in the caves to the north!",
        "Would you like to trade? ...Just kidding, I have nothing to sell."
    };

    private final Random random = new Random();

    @Override
    protected void firstRun(@Nonnull InteractionType interactionType,
                           @Nonnull InteractionContext interactionContext,
                           @Nonnull CooldownHandler cooldownHandler) {

        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();
        if (commandBuffer == null) {
            interactionContext.getState().state = InteractionState.Failed;
            return;
        }

        // ref is the entity ref of the player who triggered the interaction
        Ref<EntityStore> ref = interactionContext.getEntity();
        Player player = commandBuffer.getComponent(ref, Player.getComponentType());

        if (player == null) {
            interactionContext.getState().state = InteractionState.Failed;
            return;
        }

        // docs: player.getPlayerRef() gives the typed PlayerRef needed by the page constructor
        PlayerRef playerRef = player.getPlayerRef();

        // docs: player.getWorld().getEntityStore().getStore() gives the Store<EntityStore>
        Store<EntityStore> store = player.getWorld().getEntityStore().getStore();

        // docs: player.getPageManager().openCustomPage(ref, store, page)
        ShopPage page = new ShopPage(playerRef);
        player.getPageManager().openCustomPage(ref, store, page);

        String dialogLine = DIALOG_LINES[random.nextInt(DIALOG_LINES.length)];
        player.sendMessage(Message.raw("§6[Kweebec]§r " + dialogLine));

        interactionContext.getState().state = InteractionState.Finished;
    }
}
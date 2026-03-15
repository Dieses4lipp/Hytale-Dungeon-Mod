package com.example.plugin.Ui.PlayPage;

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class PlayPage extends InteractiveCustomUIPage<PlayPage.Data> {

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
            .append(new KeyedCodec<>("ButtonClicked", Codec.STRING),
                (data, value) -> data.clickedButton = value,
                data -> data.clickedButton)
            .add()
            .build();

        private String clickedButton;
    }

    public PlayPage(PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder uiCommandBuilder,
                      @Nonnull UIEventBuilder uiEventBuilder,
                      @Nonnull Store<EntityStore> store) {

        uiCommandBuilder.append("Pages/PlayPage.ui");

        // Top nav
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#PlayBtn",
                EventData.of("ButtonClicked", "play"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#StashBtn",
                EventData.of("ButtonClicked", "stash"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CharacterBtn",
                EventData.of("ButtonClicked", "character"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#MarketBtn",
                EventData.of("ButtonClicked", "market"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#LeaderboardBtn",
                EventData.of("ButtonClicked", "leaderboard"), false);

        // Dungeon size selection
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#SmallBtn",
                EventData.of("ButtonClicked", "small"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#MediumBtn",
                EventData.of("ButtonClicked", "medium"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#LargeBtn",
                EventData.of("ButtonClicked", "large"), false);

        // Bottom bar
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#EnterBtn",
                EventData.of("ButtonClicked", "enter"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CloseBtn",
                EventData.of("ButtonClicked", "close"), false);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                Data data) {
        super.handleDataEvent(ref, store, data);

        if (data.clickedButton == null) return;

        switch (data.clickedButton) {
            case "play"        -> System.out.println("[PlayPage] Play tab clicked");
            case "stash"       -> System.out.println("[PlayPage] Stash tab clicked");
            case "character"   -> System.out.println("[PlayPage] Character tab clicked");
            case "market"      -> System.out.println("[PlayPage] Market tab clicked");
            case "leaderboard" -> System.out.println("[PlayPage] Leaderboards tab clicked");

            case "small"       -> System.out.println("[PlayPage] Small dungeon selected (20 rooms)");
            case "medium"      -> System.out.println("[PlayPage] Medium dungeon selected (100 rooms)");
            case "large"       -> System.out.println("[PlayPage] Large dungeon selected (150 rooms)");

            case "enter"       -> System.out.println("[PlayPage] Enter Dungeon clicked");
            case "close"       -> System.out.println("[PlayPage] Close clicked");
        }

        data.clickedButton = null;
        sendUpdate();
    }
}

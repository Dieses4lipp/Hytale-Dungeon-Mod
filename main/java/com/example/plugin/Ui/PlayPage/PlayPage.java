package com.example.plugin.Ui.PlayPage;

import javax.annotation.Nonnull;

import com.example.plugin.DungeonGeneration.DungeonConfig;
import com.example.plugin.DungeonGeneration.DungeonInstance;
import com.example.plugin.DungeonGeneration.DungeonManager;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.packets.interface_.CustomPage;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;

public class PlayPage extends InteractiveCustomUIPage<PlayPage.Data> {
    public World world;
    public PlayerRef playerRef;
    private int selectedRoomCount;

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
            .append(new KeyedCodec<>("ButtonClicked", Codec.STRING),
                (data, value) -> data.clickedButton = value,
                data -> data.clickedButton)
            .add()
            .build();

        private String clickedButton;
    }

    // This constructor allows your OpenPlayPageCommand to work without any changes!
    public PlayPage(PlayerRef playerRef, World world) {
        this(playerRef, world, 20); // Defaults to small
    }

    // This constructor is used internally to load the different sizes
    public PlayPage(PlayerRef playerRef, World world, int selectedRoomCount) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.world = world;
        this.playerRef = playerRef;
        this.selectedRoomCount = selectedRoomCount;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder uiCommandBuilder,
                      @Nonnull UIEventBuilder uiEventBuilder,
                      @Nonnull Store<EntityStore> store) {

        // Load the specific UI file based on the selected size
        if (this.selectedRoomCount == 100) {
            uiCommandBuilder.append("Pages/PlayPage_Medium.ui");
        } else if (this.selectedRoomCount == 150) {
            uiCommandBuilder.append("Pages/PlayPage_Large.ui");
        } else {
            uiCommandBuilder.append("Pages/PlayPage_Small.ui");
        }

        // Top nav
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#PlayBtn", EventData.of("ButtonClicked", "play"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#StashBtn", EventData.of("ButtonClicked", "stash"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CharacterBtn", EventData.of("ButtonClicked", "character"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#MarketBtn", EventData.of("ButtonClicked", "market"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#LeaderboardBtn", EventData.of("ButtonClicked", "leaderboard"), false);

        // Dungeon size selection
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#SmallBtn", EventData.of("ButtonClicked", "small"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#MediumBtn", EventData.of("ButtonClicked", "medium"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#LargeBtn", EventData.of("ButtonClicked", "large"), false);

        // Bottom bar
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#EnterBtn", EventData.of("ButtonClicked", "enter"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CloseBtn", EventData.of("ButtonClicked", "close"), false);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                Data data) {
        super.handleDataEvent(ref, store, data);

        if (data.clickedButton == null) return;
        String action = data.clickedButton;
        data.clickedButton = null;

        Player player = store.getComponent(ref, Player.getComponentType());

        switch (action) {
            case "play"        -> System.out.println("[PlayPage] Play tab clicked");
            case "stash"       -> System.out.println("[PlayPage] Stash tab clicked");
            case "character"   -> System.out.println("[PlayPage] Character tab clicked");
            case "market"      -> System.out.println("[PlayPage] Market tab clicked");
            case "leaderboard" -> System.out.println("[PlayPage] Leaderboards tab clicked");

            // Re-open the CustomPage using the exact method from your Command
            case "small"       -> player.getPageManager().openCustomPage(ref, store, new PlayPage(playerRef, world, 20));
            case "medium"      -> player.getPageManager().openCustomPage(ref, store, new PlayPage(playerRef, world, 100));
            case "large"       -> player.getPageManager().openCustomPage(ref, store, new PlayPage(playerRef, world, 150));

            case "enter"       -> generateAndTeleport(ref, store);
            case "close"       -> player.getPageManager().setPage(ref, store, Page.None);
        }
    }

    private void generateAndTeleport(Ref<EntityStore> ref, Store<EntityStore> store) {
        DungeonInstance instance = DungeonManager.get().createDungeon(world, selectedRoomCount, store);

        int spawnX = instance.worldOriginX + (instance.startX * instance.spacing);
        int spawnY = DungeonConfig.get().generator.baseY + 2;
        int spawnZ = instance.worldOriginZ + (instance.startY * instance.spacing);

        Transform transform = new Transform(spawnX, spawnY, spawnZ);
        Teleport teleport = Teleport.createForPlayer(world, transform);
        store.addComponent(ref, Teleport.getComponentType(), teleport);

        Player player = store.getComponent(ref, Player.getComponentType());
        player.getPageManager().setPage(ref, store, Page.None);
    }
}
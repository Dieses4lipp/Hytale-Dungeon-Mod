package com.example.plugin.Ui.PlayPage;

import javax.annotation.Nonnull;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.CameraManager;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class MarketPage extends InteractiveCustomUIPage<MarketPage.Data> {
    public World world;
    public PlayerRef playerRef;

    public MarketPage(PlayerRef playerRef, World world) {
        super(playerRef, com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime.CantClose, Data.CODEC);
        this.world = world;
        this.playerRef = playerRef;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/MarketPage.ui");
var stats = com.example.plugin.Stats.PlayerLevelComponent.getStats(store, ref);
    int currentLevel = (stats != null) ? stats.level : 1;
    int currentXp = (stats != null) ? stats.xp : 0;
    int xpNeeded = currentLevel * 100;
    uiCommandBuilder.set("#LevelLabel.Text", "Lv. " + currentLevel);
    uiCommandBuilder.set("#XpLabel.Text", currentXp + " / " + xpNeeded + " XP");
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#PlayBtn", EventData.of("ButtonClicked", "nav_play"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#StashBtn", EventData.of("ButtonClicked", "nav_inventory"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CharacterBtn", EventData.of("ButtonClicked", "nav_character"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#MarketBtn", EventData.of("ButtonClicked", "nav_market"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#LeaderboardBtn", EventData.of("ButtonClicked", "nav_leaderboard"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CloseBtn", EventData.of("ButtonClicked", "nav_close"), false);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, Data data) {
        if (data.clickedButton == null) return;
        String action = data.clickedButton;
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;
        if (action.equals("nav_play")) { player.getPageManager().openCustomPage(ref, store, new PlayPage(playerRef, world)); PlayPage.LineUpCameraForCamModel(store, ref, playerRef); return; }
        if (action.equals("nav_inventory")) { player.getPageManager().openCustomPage(ref, store, new InventoryPage(playerRef, world)); InventoryPage.LineUpCameraForCamModel(store, ref, playerRef); return; }
        if (action.equals("nav_character")) { player.getPageManager().openCustomPage(ref, store, new CharacterPage(playerRef, world)); return; }
        if (action.equals("nav_market")) { player.getPageManager().openCustomPage(ref, store, new MarketPage(playerRef, world)); return; }
        if (action.equals("nav_leaderboard")) { player.getPageManager().openCustomPage(ref, store, new LeaderboardPage(playerRef, world)); return; }
        if (action.equals("nav_close")) { 
            player.getPageManager().setPage(ref, store, Page.None); 
            CameraManager camManager = store.getComponent(ref, CameraManager.getComponentType());
            if (camManager != null) camManager.resetCamera(this.playerRef);
            return; 
        }
    }

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
            .append(new KeyedCodec<>("ButtonClicked", Codec.STRING), (d, v) -> d.clickedButton = v, d -> d.clickedButton)
            .add().build();
        private String clickedButton;
    }
}
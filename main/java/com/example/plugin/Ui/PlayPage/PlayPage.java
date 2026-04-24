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
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.AttachedToType;
import com.hypixel.hytale.protocol.CameraInteraction;
import com.hypixel.hytale.protocol.ClientCameraView;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.MouseInputTargetType;
import com.hypixel.hytale.protocol.MouseInputType;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.PositionDistanceOffsetType;
import com.hypixel.hytale.protocol.RotationType;
import com.hypixel.hytale.protocol.ServerCameraSettings;
import com.hypixel.hytale.protocol.Vector2f;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.protocol.packets.interface_.CustomPage;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import com.hypixel.hytale.server.core.entity.entities.player.CameraManager;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;

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

    public PlayPage(PlayerRef playerRef, World world) {
        this(playerRef, world, 20);
    }

    public PlayPage(PlayerRef playerRef, World world, int selectedRoomCount) {
        super(playerRef, CustomPageLifetime.CantClose, Data.CODEC);
        this.world = world;
        this.playerRef = playerRef;
        this.selectedRoomCount = selectedRoomCount;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
            @Nonnull UICommandBuilder uiCommandBuilder,
            @Nonnull UIEventBuilder uiEventBuilder,
            @Nonnull Store<EntityStore> store) {

        if (this.selectedRoomCount == 100) {
            uiCommandBuilder.append("Pages/PlayPage_Medium.ui");
        } else if (this.selectedRoomCount == 150) {
            uiCommandBuilder.append("Pages/PlayPage_Large.ui");
        } else {
            uiCommandBuilder.append("Pages/PlayPage_Small.ui");
        } 


    var stats = com.example.plugin.Stats.PlayerLevelComponent.getStats(store, ref);
    int currentLevel = (stats != null) ? stats.level : 1;
    int currentXp = (stats != null) ? stats.xp : 0;
    int xpNeeded = currentLevel * 100;
    int currentGold = (stats != null) ? stats.gold : 0;
    uiCommandBuilder.set("#LevelLabel.Text", "Lv. " + currentLevel);
    uiCommandBuilder.set("#GoldLabel.Text","" + currentGold);
    uiCommandBuilder.set("#XpLabel.Text", currentXp + " / " + xpNeeded + " XP");
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#PlayBtn",
                EventData.of("ButtonClicked", "nav_play"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#StashBtn",
                EventData.of("ButtonClicked", "nav_inventory"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CharacterBtn",
                EventData.of("ButtonClicked", "nav_character"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#MarketBtn",
                EventData.of("ButtonClicked", "nav_market"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#LeaderboardBtn",
                EventData.of("ButtonClicked", "nav_leaderboard"), false);

        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CloseBtn",
                EventData.of("ButtonClicked", "nav_close"), false);

        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#SmallBtn",
                EventData.of("ButtonClicked", "small"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#MediumBtn",
                EventData.of("ButtonClicked", "medium"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#LargeBtn",
                EventData.of("ButtonClicked", "large"), false);

        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#EnterBtn",
                EventData.of("ButtonClicked", "enter"), false);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            Data data) {
        super.handleDataEvent(ref, store, data);

        if (data.clickedButton == null)
            return;
        String action = data.clickedButton;
        data.clickedButton = null;

        Player player = store.getComponent(ref, Player.getComponentType());

        switch (action) {
            case "nav_play" -> {
                player.getPageManager().openCustomPage(ref, store, new PlayPage(playerRef, world, selectedRoomCount));
            }
            case "nav_inventory" -> {
                player.getPageManager().openCustomPage(ref, store, new InventoryPage(playerRef, world));
                InventoryPage.LineUpCameraForCamModel(store, ref, playerRef);
            }
            case "nav_character" -> {
                player.getPageManager().openCustomPage(ref, store, new CharacterPage(playerRef, world));
            }
            case "nav_market" -> {
                player.getPageManager().openCustomPage(ref, store, new MarketPage(playerRef, world));
            }
            case "nav_leaderboard" -> {
                player.getPageManager().openCustomPage(ref, store, new LeaderboardPage(playerRef, world));
            }
            case "nav_close" -> {
                player.getPageManager().setPage(ref, store, Page.None);
                resetCamera(ref, store);
            }

            case "small" -> player.getPageManager().openCustomPage(ref, store, new PlayPage(playerRef, world, 20));
            case "medium" -> player.getPageManager().openCustomPage(ref, store, new PlayPage(playerRef, world, 100));
            case "large" -> player.getPageManager().openCustomPage(ref, store, new PlayPage(playerRef, world, 150));

            case "enter" -> {
                generateAndTeleport(ref, store);
                resetCamera(ref, store);
            }
            case "close" -> {
                player.getPageManager().setPage(ref, store, Page.None);
                resetCamera(ref, store);
            }
        }
    }

    private void resetCamera(Ref<EntityStore> ref, Store<EntityStore> store) {
        CameraManager camManager = store.getComponent(ref, CameraManager.getComponentType());

        if (camManager != null) {
            camManager.resetCamera(this.playerRef);
        }
    }

    private void generateAndTeleport(Ref<EntityStore> ref, Store<EntityStore> store) {
        DungeonInstance instance = DungeonManager.get().createDungeon(world, selectedRoomCount, store);
        int soundIndex = SoundEvent.getAssetMap().getIndex("sfx_discovery_z1_medium");
        SoundUtil.playSoundEvent2d(ref, soundIndex, SoundCategory.SFX, store);

        int spawnX = instance.worldOriginX + (instance.startX * instance.spacing);
        int spawnY = DungeonConfig.get().generator.baseY + 2;
        int spawnZ = instance.worldOriginZ + (instance.startY * instance.spacing);

        Transform transform = new Transform(spawnX, spawnY, spawnZ);
        Teleport teleport = Teleport.createForPlayer(world, transform);
        store.addComponent(ref, Teleport.getComponentType(), teleport);

        Player player = store.getComponent(ref, Player.getComponentType());
        player.getPageManager().setPage(ref, store, Page.None);
    }

    public static void LineUpCameraForCamModel(Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef) {
        float f3Pitch = (float) Math.toRadians(-11.9);
        float f3Yaw = (float) Math.toRadians(118.2);
        float f3Roll = 0.0f;
        TransformComponent transformComp = store.getComponent(ref, TransformComponent.getComponentType());
        Vector3d currentPosition = transformComp.getPosition();

        Vector3f newLookDirection = new Vector3f(f3Pitch, f3Yaw, f3Roll);

        Teleport teleportComponent = Teleport.createForPlayer(currentPosition, newLookDirection);
        Direction lockedDirection = new Direction(0f, (float) Math.PI / 2, 0f);
        store.addComponent(ref, Teleport.getComponentType(), teleportComponent);
        ServerCameraSettings camSettings = new ServerCameraSettings();
        camSettings.mouseInputTargetType = MouseInputTargetType.None;
        camSettings.isFirstPerson = false;
        camSettings.distance = 1.4f;
        camSettings.positionOffset = new Position(-0.8, -0.7, 0);
        camSettings.positionLerpSpeed = 0.15f;
        camSettings.rotationLerpSpeed = 0.15f;
        camSettings.rotationType = RotationType.AttachedToPlusOffset;
        camSettings.rotationOffset = new Direction((float) Math.PI, 0.7f, 0f);
        
        camSettings.allowPitchControls = false;
        camSettings.sendMouseMotion = false;
        camSettings.displayCursor = false;
        camSettings.displayReticle = false;
        camSettings.lookMultiplier = new Vector2f(0.0f, 0.0f);
        camSettings.skipCharacterPhysics = false;
        camSettings.eyeOffset = true;
        
        camSettings.positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffset;

        playerRef.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.Custom, true, camSettings));
    }
}
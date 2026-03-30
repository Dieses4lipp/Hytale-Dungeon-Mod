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

        // Load the specific UI file based on the selected size
        if (this.selectedRoomCount == 100) {
            uiCommandBuilder.append("Pages/PlayPage_Medium.ui");
        } else if (this.selectedRoomCount == 150) {
            uiCommandBuilder.append("Pages/PlayPage_Large.ui");
        } else {
            uiCommandBuilder.append("Pages/PlayPage_Small.ui");
        }

        // Top nav
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#PlayBtn",
                EventData.of("ButtonClicked", "play"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#InventoryBtn",
                EventData.of("ButtonClicked", "inventory"), false);
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

        if (data.clickedButton == null)
            return;
        String action = data.clickedButton;
        data.clickedButton = null;

        Player player = store.getComponent(ref, Player.getComponentType());

        switch (action) {
            case "play" -> System.out.println("[PlayPage] Play tab clicked");
            case "inventory" -> {System.out.println("[PlayPage] Stash tab clicked");
                InventoryPage inventoryPage = new InventoryPage(playerRef, world);
                inventoryPage.LineUpCameraForCamModel(store, ref, playerRef);
               player.getPageManager().openCustomPage(ref, store, inventoryPage);
            }
            case "character" -> System.out.println("[PlayPage] Character tab clicked");
            case "market" -> System.out.println("[PlayPage] Market tab clicked");
            case "leaderboard" -> System.out.println("[PlayPage] Leaderboards tab clicked");

            case "small" -> player.getPageManager().openCustomPage(ref, store, new PlayPage(playerRef, world, 20));
            case "medium" -> player.getPageManager().openCustomPage(ref, store, new PlayPage(playerRef, world, 100));
            case "large" -> player.getPageManager().openCustomPage(ref, store, new PlayPage(playerRef, world, 150));

            case "enter" -> {
                generateAndTeleport(ref, store);
                resetCamera(ref, store);
            }
            case "close" -> {player.getPageManager().setPage(ref, store, Page.None);
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

        int spawnX = instance.worldOriginX + (instance.startX * instance.spacing);
        int spawnY = DungeonConfig.get().generator.baseY + 2;
        int spawnZ = instance.worldOriginZ + (instance.startY * instance.spacing);

        Transform transform = new Transform(spawnX, spawnY, spawnZ);
        Teleport teleport = Teleport.createForPlayer(world, transform);
        store.addComponent(ref, Teleport.getComponentType(), teleport);

        Player player = store.getComponent(ref, Player.getComponentType());
        player.getPageManager().setPage(ref, store, Page.None);
    }
    void LineUpCameraForCamModel(Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef){
        float f3Pitch = (float) Math.toRadians(-11.9);
        float f3Yaw = (float) Math.toRadians(118.2);
        float f3Roll = 0.0f;
        TransformComponent transformComp = store.getComponent(ref, TransformComponent.getComponentType());
        Vector3d currentPosition = transformComp.getPosition();

        Vector3f newLookDirection = new Vector3f(f3Pitch, f3Yaw, f3Roll);

        Teleport teleportComponent = Teleport.createForPlayer(currentPosition, newLookDirection);

        store.addComponent(ref, Teleport.getComponentType(), teleportComponent);

        ServerCameraSettings camSettings = new ServerCameraSettings();
        camSettings.isFirstPerson = false;
        camSettings.distance = 1.4f;
        camSettings.positionOffset = new Position(-0.8, -0.7, 0);
        camSettings.positionLerpSpeed = 0.15f;
        camSettings.rotationLerpSpeed = 0.15f;
        camSettings.attachedToType = AttachedToType.LocalPlayer;
        camSettings.rotationType = RotationType.AttachedToPlusOffset;
        camSettings.rotationOffset = new Direction((float) Math.PI, 0.7f, 0.3f);
        camSettings.allowPitchControls = false;
        camSettings.sendMouseMotion = false;
        camSettings.mouseInputType = MouseInputType.LookAtTargetEntity;
        camSettings.displayCursor = true;
        camSettings.displayReticle = false;
        camSettings.lookMultiplier = new Vector2f(0.0f, 0.0f);
        camSettings.mouseInputTargetType = MouseInputTargetType.None;
        camSettings.skipCharacterPhysics = false;

        camSettings.eyeOffset = true;
        camSettings.positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffsetRaycast;

        playerRef.getPacketHandler().writeNoCache(new SetServerCamera(ClientCameraView.Custom, false, camSettings));
    }
}
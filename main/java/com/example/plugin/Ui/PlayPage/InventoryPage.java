package com.example.plugin.Ui.PlayPage; // Passe das an dein genaues Package an

import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
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
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.CameraManager;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class InventoryPage extends InteractiveCustomUIPage<InventoryPage.Data> {

    public World world;
    public PlayerRef playerRef;

    // 1. Data-Klasse für die Button-Klicks
    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .append(new KeyedCodec<>("ButtonClicked", Codec.STRING),
                        (data, value) -> data.clickedButton = value,
                        data -> data.clickedButton)
                .add()
                .build();

        private String clickedButton;
    }

    public InventoryPage(PlayerRef playerRef, World world) {
        super(playerRef, CustomPageLifetime.CantClose, Data.CODEC);
        this.world = world;
        this.playerRef = playerRef;
    }

    // 3. UI Bauen und Buttons binden
    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder uiCommandBuilder,
                      @Nonnull UIEventBuilder uiEventBuilder,
                      @Nonnull Store<EntityStore> store) {

        uiCommandBuilder.append("Pages/InventoryPage.ui");

        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#PlayBtn",
                EventData.of("ButtonClicked", "play"), false);
                
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#StashBtn",
                EventData.of("ButtonClicked", "inventory"), false);
                
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CharacterBtn",
                EventData.of("ButtonClicked", "character"), false);
                
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#MarketBtn",
                EventData.of("ButtonClicked", "market"), false);
                
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#LeaderboardBtn",
                EventData.of("ButtonClicked", "leaderboard"), false);

        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#EnterBtn",
                EventData.of("ButtonClicked", "equip_mode"), false);
                
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CloseBtn",
                EventData.of("ButtonClicked", "close"), false);

        // Optional: Hier kannst du später auch deine ganzen Rüstungs-Slots binden!
        // uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#EquipHead",
        //         EventData.of("ButtonClicked", "equip_head"), false);
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
            case "play" -> {
                System.out.println("[InventoryPage] Switching to Play tab");
                PlayPage playPage = new PlayPage(playerRef, world);
                playPage.LineUpCameraForCamModel(store, ref, playerRef);
                player.getPageManager().openCustomPage(ref, store, new PlayPage(playerRef, world));
            }
            case "inventory" -> {
                System.out.println("[InventoryPage] Already on Inventory tab");
            }
            case "character" -> System.out.println("[InventoryPage] Character tab clicked");
            case "market" -> System.out.println("[InventoryPage] Market tab clicked");
            case "leaderboard" -> System.out.println("[InventoryPage] Leaderboards tab clicked");

            case "equip_mode" -> System.out.println("[InventoryPage] Equip mode activated");
            
            case "close" -> {
                player.getPageManager().setPage(ref, store, Page.None);
                resetCamera(ref, store);
            }
        }
    }

    // 5. Hilfsmethode zum Zurücksetzen der Kamera beim Schließen
    private void resetCamera(Ref<EntityStore> ref, Store<EntityStore> store) {
        CameraManager camManager = store.getComponent(ref, CameraManager.getComponentType());
        if (camManager != null) {
            camManager.resetCamera(this.playerRef);
        }
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
        camSettings.positionOffset = new Position(-0.8, -0.7, 2.5);
        camSettings.positionLerpSpeed = 0.15f;
        camSettings.rotationLerpSpeed = 0.15f;
        camSettings.attachedToType = AttachedToType.LocalPlayer;
        camSettings.rotationType = RotationType.AttachedToPlusOffset;
        camSettings.rotationOffset = new Direction((float) Math.PI, 0f, 0f);
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
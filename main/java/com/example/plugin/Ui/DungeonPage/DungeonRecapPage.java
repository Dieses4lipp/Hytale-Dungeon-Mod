package com.example.plugin.Ui.DungeonPage;

import javax.annotation.Nonnull;

import com.example.plugin.DungeonGeneration.DungeonInstance;
import com.example.plugin.DungeonGeneration.DungeonManager;
import com.example.plugin.Stats.SellConfig;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

// 1. We extend InteractiveCustomUIPage instead of CustomUIPage
public class DungeonRecapPage extends InteractiveCustomUIPage<DungeonRecapPage.Data> {

    private final String statsText;
    private final int dungeonSlot;

    // 2. Data Class and CODEC to catch button clicks (just like in PlayPage)
    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .append(new KeyedCodec<>("ButtonClicked", Codec.STRING),
                        (data, value) -> data.clickedButton = value,
                        data -> data.clickedButton)
                .add()
                .build();

        private String clickedButton;
    }

    public DungeonRecapPage(PlayerRef playerRef, String statsText, int dungeonSlot) {
        super(playerRef, CustomPageLifetime.CantClose, Data.CODEC);
        this.statsText = statsText;
        this.dungeonSlot = dungeonSlot;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
            @Nonnull UICommandBuilder uiCommandBuilder,
            @Nonnull UIEventBuilder uiEventBuilder,
            @Nonnull Store<EntityStore> store) {

        uiCommandBuilder.append("Pages/DungeonRecap.ui");

        uiCommandBuilder.set("#StatsLabel.Text", this.statsText);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            String playerId = player.getUuid().toString();
            int goldValue = SellConfig.calculateStashSellValue(playerId, player);
            uiCommandBuilder.set("#GoldValueLabel.Text", goldValue + " Gold");
            SellConfig.addGoldToPlayer(player, ref, store, goldValue);
            SellConfig.removeSellableItemsFromVanillaInventory(player);
        }
        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#CloseBtn",
                EventData.of("ButtonClicked", "close_recap"),
                false);
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

        if ("close_recap".equals(action) && player != null) {

            player.getPageManager().setPage(ref, store, Page.None);

            World activeWorld = DungeonManager.get().activeWorld;
            if (activeWorld != null) {
                Transform transform = new Transform(110, 133, 110);
                Teleport teleport = Teleport.createForPlayer(activeWorld, transform);
                store.addComponent(ref, Teleport.getComponentType(), teleport);
            }

            DungeonInstance inst = DungeonManager.get().getBySlot(this.dungeonSlot);
            if (inst != null) {
                try {
                    DungeonManager.get().destroyDungeon(store, null, activeWorld, inst);
                    System.out.println("[DungeonMod] Player left. Destroyed dungeon in slot " + this.dungeonSlot);
                } catch (Exception e) {
                    System.err.println("[DungeonMod] Error destroying dungeon: " + e.getMessage());
                }
            }
        }
    }
}
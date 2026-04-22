package com.example.plugin.Stats;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.*;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.example.plugin.Ui.Hud.LevelHud;
import com.example.plugin.DatabaseManager;

public class PlayerDatabaseSetupSystem extends EntityTickingSystem<EntityStore> {

    private final ComponentType<EntityStore, PlayerRef> playerRefType;
    private final ComponentType<EntityStore, PlayerLevelComponent> playerLevelType;

    public PlayerDatabaseSetupSystem() {
        this.playerRefType = PlayerRef.getComponentType();
        this.playerLevelType = PlayerLevelComponent.getComponentType();
    }

    @Override
    public void tick(float dt, int index, 
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store, 
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);

        if (!store.getArchetype(ref).contains(playerLevelType)) {
            
            PlayerLevelComponent stats = new PlayerLevelComponent();
            PlayerRef playerRef = store.getComponent(ref, playerRefType);
            
            if (playerRef != null) {
                String uuid = playerRef.getUuid().toString();

                try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(
                        "SELECT level, xp, gold, can_build FROM player_info WHERE uuid = ?")) {
                    pstmt.setString(1, uuid);
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        stats.level = rs.getInt("level");
                        stats.xp = rs.getInt("xp");
                        stats.gold = rs.getInt("gold");
                        
                        boolean canBuild = rs.getInt("can_build") == 1;
                        if (canBuild) {
                            commandBuffer.addComponent(ref, com.example.plugin.System.BuildPermissionComponent.getComponentType(), new com.example.plugin.System.BuildPermissionComponent());
                        }

                        com.example.plugin.Ui.PlayPage.InventoryPage.getOrCreateEmptyStash(uuid); 
                        com.example.plugin.Stats.SellConfig.loadStashFromDatabase(uuid, com.example.plugin.Ui.PlayPage.InventoryPage.getOrCreateEmptyStash(uuid));
                    } else {
                        try (PreparedStatement insert = DatabaseManager.getConnection().prepareStatement(
                                "INSERT INTO player_info (uuid, level, xp, gold, can_build) VALUES (?, 1, 0, 0, 0)")) {
                            insert.setString(1, uuid);
                            insert.executeUpdate();
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("[DungeonMod] Error while loading player data: " + e.getMessage());
                }
            }

            commandBuffer.addComponent(ref, playerLevelType, stats);

            // HUD initialization
            try {
                com.hypixel.hytale.server.core.entity.entities.Player player = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
                if (player != null && playerRef != null) {
                   LevelHud hudPage = new LevelHud(playerRef, stats.level, stats.xp);
                   player.getHudManager().setCustomHud(playerRef, hudPage);
                }
            } catch (Exception e) {
                System.err.println("[DungeonMod] Error during HUD setup: " + e.getMessage());
            }
        }
    }

    @Nullable
    @Override
    public SystemGroup<EntityStore> getGroup() { return null; }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() { return Query.and(playerRefType); }
}
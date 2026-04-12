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
                        "SELECT level, xp, gold FROM player_levels WHERE uuid = ?")) {
                    pstmt.setString(1, uuid);
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        stats.level = rs.getInt("level");
                        stats.xp = rs.getInt("xp");
                        stats.gold = rs.getInt("gold");
                        
                        System.out.println("[DungeonMod] Daten geladen für " + uuid + ": Level " + stats.level + " | Gold " + stats.gold);
                    } else {
                        try (PreparedStatement insert = DatabaseManager.getConnection().prepareStatement(
                                "INSERT INTO player_levels (uuid, level, xp, gold) VALUES (?, 1, 0, 0)")) {
                            insert.setString(1, uuid);
                            insert.executeUpdate();
                            System.out.println("[DungeonMod] Neuer Spieler in Datenbank registriert: " + uuid);
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("[DungeonMod] Fehler beim Laden der Spielerdaten: " + e.getMessage());
                }
            }

            commandBuffer.addComponent(ref, playerLevelType, stats);

            // HUD Initialisierung
            try {
                com.hypixel.hytale.server.core.entity.entities.Player player = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
                if (player != null && playerRef != null) {
                   LevelHud hudPage = new LevelHud(playerRef, stats.level, stats.xp);
                   player.getHudManager().setCustomHud(playerRef, hudPage);
                }
            } catch (Exception e) {
                System.out.println("[DungeonMod] Fehler beim HUD-Setup: " + e.getMessage());
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
package com.example.plugin.DungeonGeneration;

import com.example.plugin.Stats.PlayerLevelComponent;
import com.example.plugin.DatabaseManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.example.plugin.Ui.Hud.LevelHud;

import javax.annotation.Nonnull;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MobDeathAndXPSystem extends DeathSystems.OnDeathSystem {

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.not(PlayerLevelComponent.getComponentType());
    }

    @Override
    public void onComponentAdded(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull DeathComponent deathComponent,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        Damage damageInfo = deathComponent.getDeathInfo();
        if (damageInfo == null)
            return;

        Damage.Source source = damageInfo.getSource();
        Ref<EntityStore> killerRef = null;

        if (source instanceof Damage.EntitySource) {
            killerRef = ((Damage.EntitySource) source).getRef();
        }

        if (killerRef != null) {
            if (store.getArchetype(killerRef).contains(PlayerLevelComponent.getComponentType())) {

                PlayerLevelComponent stats = store.getComponent(killerRef, PlayerLevelComponent.getComponentType());
                PlayerRef pRef = store.getComponent(killerRef, PlayerRef.getComponentType());

                if (stats != null && pRef != null) {

                    int xpGained = 25;
                    if (DungeonManager.get().mobXpRewards.containsKey(ref)) {
                        xpGained = DungeonManager.get().mobXpRewards.remove(ref);
                    }

                    stats.xp += xpGained;
                    int xpNeeded = stats.level * 100;

                    if (stats.xp >= xpNeeded) {
                        stats.xp -= xpNeeded;
                        stats.level++;
                        System.out.println("[DungeonMod] Level Up gespeichert!");
                    }

                    try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(
                            "UPDATE player_levels SET level = ?, xp = ?, gold = ? WHERE uuid = ?")) {
                        pstmt.setInt(1, stats.level);
                        pstmt.setInt(2, stats.xp);
                        pstmt.setInt(3, stats.gold);
                        pstmt.setString(4, pRef.getUuid().toString());
                        pstmt.executeUpdate();
                    } catch (SQLException e) {
                        System.err.println("[DungeonMod] Fehler beim Speichern: " + e.getMessage());
                    }

                    // HUD Refresh
                    try {
                        Player player = store.getComponent(killerRef, Player.getComponentType());
                        if (player != null) {

                            player.getHudManager().setCustomHud(pRef, null);

                            LevelHud refreshedHud = new LevelHud(pRef, stats.level, stats.xp);

                            player.getHudManager().setCustomHud(pRef, refreshedHud);

                        }
                    } catch (Exception e) {
                        System.out.println("[DungeonMod] HUD-Update fehlgeschlagen.");
                    }
                }
            }
        }
    }
}
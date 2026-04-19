package com.example.plugin.DungeonGeneration;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.type.gameplay.DeathConfig.ItemsLossMode;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.example.plugin.Stats.PlayerLevelComponent;
import com.example.plugin.System.StarterKit;
import com.example.plugin.doorsystem.OpenDoorInteraction;

import javax.annotation.Nonnull;

public class PlayerDeathDungeonSystem extends DeathSystems.OnDeathSystem {

    public PlayerDeathDungeonSystem() {

    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(PlayerLevelComponent.getComponentType(), TransformComponent.getComponentType());
    }

    @Override
    public void onComponentAdded(
            @Nonnull Ref ref,
            @Nonnull DeathComponent deathComponent,
            @Nonnull Store store,
            @Nonnull CommandBuffer commandBuffer) {
        com.hypixel.hytale.server.core.entity.entities.Player player = (com.hypixel.hytale.server.core.entity.entities.Player) store
                .getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());

        if (player != null) {
            player.getInventory().clear();

            deathComponent.setItemsLossMode(ItemsLossMode.ALL);
            StarterKit.giveStarterKit(player);

        }
        TransformComponent transform = (TransformComponent) store.getComponent(ref,
                TransformComponent.getComponentType());
        if (transform == null || transform.getPosition() == null)
            return;

        Vector3d pos = transform.getPosition();
        double playerX = pos.x;
        double playerZ = pos.z;

        DungeonManager manager = DungeonManager.get();
        if (manager == null)
            return;

        int gridsize = DungeonConfig.get().layout.gridsize;
        int spacing = DungeonConfig.get().manager.spacing;
        int slotSize = gridsize * spacing;

        for (DungeonInstance instance : manager.getAllActiveDungeons()) {

            double minX = instance.worldOriginX;
            double maxX = instance.worldOriginX + slotSize;

            double minZ = instance.worldOriginZ;
            double maxZ = instance.worldOriginZ + slotSize;

            if (playerX >= minX && playerX <= maxX && playerZ >= minZ && playerZ <= maxZ) {
                System.out.println("[DungeonMod] Player died in Dungeon Slot " + instance.slot + ". Destroying...");
                World world = manager.activeWorld;
                manager.destroyDungeon(store, commandBuffer, world, instance);
                break;
            }
        }
        
    }
}
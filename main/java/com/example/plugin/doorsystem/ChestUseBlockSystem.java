package com.example.plugin.doorsystem;

import com.example.plugin.Ui.ChestPage.*;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.component.Ref;

public class ChestUseBlockSystem extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {

    public ChestUseBlockSystem() {
        super(UseBlockEvent.Pre.class);
    }

    @Override
    public void handle(int index, ArchetypeChunk<EntityStore> chunk,
            Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer,
            UseBlockEvent.Pre event) {

        String blockId = event.getBlockType().getId();
        System.out.println("[ChestSystem] Block used: " + blockId);

        if (!blockId.equals("Furniture_Temple_Emerald_Chest_Small")) { 
            return;
        }

        event.setCancelled(true);

        Ref<EntityStore> ref = chunk.getReferenceTo(index);
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        PlayerRef playerRef = player.getPlayerRef();
        ChestPage page = new ChestPage(playerRef);
        player.getPageManager().openCustomPage(ref, store, page);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
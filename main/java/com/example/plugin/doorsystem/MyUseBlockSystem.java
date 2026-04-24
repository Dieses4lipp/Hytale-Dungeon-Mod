package com.example.plugin.doorsystem;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.nio.file.Path;

public class MyUseBlockSystem extends EntityEventSystem<EntityStore, UseBlockEvent.Pre> {

    private static final Path doorWE_open = Path.of("prefabs/Prefabs/Door/door_we_open.prefab.json");
    private static final Path doorSN_open = Path.of("prefabs/Prefabs/Door/door_sn_open.prefab.json");
     private final World world;

    public MyUseBlockSystem(World world) {
        super(UseBlockEvent.Pre.class);
        this.world = world;
    }

    @Override
    public void handle(int index, ArchetypeChunk<EntityStore> chunk,
            Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer,
            UseBlockEvent.Pre event) {

        Vector3i pos = event.getTargetBlock();

        DoorRegistry.DoorEntry entry = DoorRegistry.get(pos);

        if (entry == null) {
            return;
        }

        event.setCancelled(true);

        if (world == null) {
            System.err.println("[DoorSystem] ERROR: World is null in registry!");
            return;
        }

        Path openPath = entry.orientation == DoorRegistry.Orientation.WE ? doorWE_open : doorSN_open;

        BlockSelection openPrefab = PrefabStore.get().getPrefab(openPath);

        openPrefab.placeNoReturn(world, pos, null);
        DoorRegistry.remove(pos);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
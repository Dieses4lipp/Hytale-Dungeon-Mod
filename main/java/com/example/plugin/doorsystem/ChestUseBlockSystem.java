package com.example.plugin.doorsystem;

import java.util.Map;

import com.example.plugin.DungeonGeneration.DungeonTables;
import com.example.plugin.Ui.ChestPage.*;
import com.hypixel.hytale.builtin.adventure.objectives.config.task.TreasureMapObjectiveTaskAsset.ChestConfig;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.block.BlockEntity;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
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

        Vector3i pos = event.getTargetBlock();
        String blockId = event.getBlockType().getId();

        boolean isEpicDungeonChest = blockId.equals("Furniture_Dungeon_Chest_Epic");
        boolean isEmeraldChest = blockId.equals("Furniture_Temple_Emerald_Chest_Small")
                || blockId.equals("Furniture_Temple_Emerald_Chest_Large");

        if (!isEpicDungeonChest && !isEmeraldChest)
            return;
        if (isEmeraldChest) {
            event.setCancelled(true);

            if (ChestRegistry.isLocked(pos)) {
                System.out.println("[ChestSystem] Chest is locked at " + pos.x + "," + pos.y + "," + pos.z);
                return;
            }

            Ref<EntityStore> ref = chunk.getReferenceTo(index);
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null)
                return;
            PlayerRef playerRef = player.getPlayerRef();
            player.getPageManager().openCustomPage(ref, store, new ChestPage(playerRef, pos));
        } else if (isEpicDungeonChest) {
            if (!ChestRegistry.isOpened(pos)) {
                System.out.println("[ChestSystem] First time opening chest at "
                        + pos.x + "," + pos.y + "," + pos.z + " - GENERATING LOOT");

                try {
                    World world = store.getExternalData().getWorld();
                    long chunkIndex = ChunkUtil.indexChunkFromBlock(pos.x, pos.z);
                    WorldChunk worldChunk = world.getChunkIfLoaded(chunkIndex);

                    if (worldChunk != null) {
                        Ref<ChunkStore> blockRef = worldChunk.getBlockComponentEntity(pos.x, pos.y, pos.z);

                        if (blockRef != null && blockRef.isValid()) {
                            Store<ChunkStore> chunkComponentStore = blockRef.getStore();
                            ItemContainerBlock containerBlock = chunkComponentStore.getComponent(
                                    blockRef, ItemContainerBlock.getComponentType());

                            if (containerBlock != null) {
                                Map<String, DungeonTables.LootEntry> lootTable = DungeonTables.get().loot
                                        .get("wooden_chest");
                                Map.Entry<String, DungeonTables.LootEntry> rolled = DungeonTables.get()
                                        .getRandomLootEntry(lootTable);

                                if (rolled != null) {
                                    ItemStack lootItem = new ItemStack(rolled.getKey(), rolled.getValue().Quantity);
                                    containerBlock.getItemContainer().addItemStack(lootItem);
                                    System.out.println("[ChestSystem] Placed " + rolled.getKey()
                                            + " x" + rolled.getValue().Quantity + " into chest!");
                                } else {
                                    System.out.println("[ChestSystem] Loot table empty or missing.");
                                }
                            }
                        } else {
                            System.out.println("[ChestSystem] No block component entity at pos.");
                        }
                    } else {
                        System.out.println("[ChestSystem] Chunk not loaded.");
                    }

                } catch (Exception e) {
                    System.out.println("[ChestSystem] Error: " + e.getMessage());
                    e.printStackTrace();
                }

                ChestRegistry.markOpened(pos);
            }
        }
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
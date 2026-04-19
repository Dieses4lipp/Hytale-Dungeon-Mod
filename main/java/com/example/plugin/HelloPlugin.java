package com.example.plugin;

import com.example.plugin.Commands.DestroyDungeonCommand;
import com.example.plugin.Commands.GetStarterKitCommand;
import com.example.plugin.Commands.OpenPlayPageCommand;
import com.example.plugin.Commands.SpawnNPCCommand;
import com.example.plugin.Commands.ToggleBuildCommand;
import com.example.plugin.Npc.Testinteractionnpc.NPCInteractionSetupSystem;
import com.example.plugin.Npc.Testinteractionnpc.NPCSetupPending;
import com.example.plugin.Npc.Testinteractionnpc.TalkToNPCInteraction;
import com.example.plugin.doorsystem.DoorNPCComponent;
import com.example.plugin.doorsystem.MyUseBlockSystem;
import com.example.plugin.doorsystem.OpenDoorInteraction;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.protocol.packets.player.JoinWorld;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.example.plugin.Stats.PlayerLevelComponent;
import com.example.plugin.DungeonGeneration.*;
import com.example.plugin.System.*;

import java.io.File;
import java.io.InputStream;

import javax.annotation.Nonnull;

public class HelloPlugin extends JavaPlugin {

    public HelloPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        super.setup();
        DatabaseManager.initialize(new File("plugins/HytaleDungeonMod"));
        ComponentType<EntityStore, BuildPermissionComponent> buildPermissionType = this.getEntityStoreRegistry()
        .registerComponent(
                BuildPermissionComponent.class,
                BuildPermissionComponent::new);
BuildPermissionComponent.setComponentType(buildPermissionType);
        ComponentType<EntityStore, PlayerLevelComponent> playerLevelType = this.getEntityStoreRegistry()
                .registerComponent(
                        PlayerLevelComponent.class,
                        PlayerLevelComponent::new);
        PlayerLevelComponent.setComponentType(playerLevelType);

        ComponentType<EntityStore, BossDeathTimerComponent> bossDeathTimerType = this.getEntityStoreRegistry()
                .registerComponent(
                        BossDeathTimerComponent.class,
                        BossDeathTimerComponent::new);
        BossDeathTimerComponent.setComponentType(bossDeathTimerType);

        InputStream configStream = getClass().getResourceAsStream("/dungeon_config.json");
        if (configStream == null) {
            System.out.println("Failed to load dungeon_config.json");
        }
        DungeonConfig.load(configStream);
        
        InputStream tablesStream = getClass().getResourceAsStream("/dungeon_tables.json");
        if(tablesStream == null) {
            System.out.println("Failed to load dungeon_tables.json");
        }
        DungeonTables.load(tablesStream);

        new DungeonManager();

        ComponentType<EntityStore, NPCSetupPending> setupPendingType = this.getEntityStoreRegistry().registerComponent(
                NPCSetupPending.class,
                "dungeon_mod:npc_setup_pending",
                NPCSetupPending.CODEC);
        NPCSetupPending.setComponentType(setupPendingType);

        this.getEntityStoreRegistry().registerSystem(
                new NPCInteractionSetupSystem(NPCSetupPending.getComponentType()));
        ComponentType<EntityStore, DoorNPCComponent> doorNPCType = this.getEntityStoreRegistry().registerComponent(
                DoorNPCComponent.class,
                "dungeon_mod:door_npc",
                DoorNPCComponent.CODEC);
        DoorNPCComponent.setComponentType(doorNPCType);

        this.getCodecRegistry(Interaction.CODEC).register(
                "open_door_type",
                OpenDoorInteraction.class,
                OpenDoorInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register(
                "talk_to_npc_type",
                TalkToNPCInteraction.class,
                TalkToNPCInteraction.CODEC);

        this.getEntityStoreRegistry().registerSystem(new com.example.plugin.Stats.PlayerDatabaseSetupSystem());
        this.getEntityStoreRegistry().registerSystem(new MobDeathAndXPSystem());
        this.getEntityStoreRegistry().registerSystem(new ChestUseBlockSystem());
        this.getEntityStoreRegistry().registerSystem(new BossDeathSystem());
        this.getEntityStoreRegistry().registerSystem(new PlayerDeathDungeonSystem());
        this.getEntityStoreRegistry().registerSystem(new BossDeathTimerSystem());
        this.getEntityStoreRegistry().registerSystem(new BlockBreakPreventionSystem());
        this.getCommandRegistry().registerCommand(new GetStarterKitCommand("starterkit", "Get a starter kit to begin your dungeon adventure!", false));
        this.getCommandRegistry().registerCommand(new SpawnNPCCommand());
        this.getCommandRegistry().registerCommand(new OpenPlayPageCommand());
        this.getCommandRegistry().registerCommand(new DestroyDungeonCommand());
        this.getCommandRegistry().registerCommand(new ToggleBuildCommand());
        
        System.out.println("Plugin loaded");
    }

    @Override
    protected void shutdown() {
        System.out.println("Plugin shutdown");
    }
}
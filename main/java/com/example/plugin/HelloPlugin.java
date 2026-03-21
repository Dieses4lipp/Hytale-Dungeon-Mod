package com.example.plugin;

import com.example.plugin.Commands.DestroyDungeonCommand;
import com.example.plugin.Commands.GenerateDungeonCommand;
import com.example.plugin.Commands.OpenPlayPageCommand;
import com.example.plugin.Commands.SpawnNPCCommand;
import com.example.plugin.DungeonGeneration.DungeonConfig;
import com.example.plugin.DungeonGeneration.DungeonManager;
import com.example.plugin.Npc.Testinteractionnpc.NPCInteractionSetupSystem;
import com.example.plugin.Npc.Testinteractionnpc.NPCSetupPending;
import com.example.plugin.Npc.Testinteractionnpc.TalkToNPCInteraction;
import com.example.plugin.doorsystem.DoorNPCComponent;
import com.example.plugin.doorsystem.MyUseBlockSystem;
import com.example.plugin.doorsystem.OpenDoorInteraction;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.io.InputStream;

import javax.annotation.Nonnull;

public class HelloPlugin extends JavaPlugin {

    public HelloPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        super.setup();
        InputStream configStream = getClass().getResourceAsStream("/dungeon_config.json");
        if (configStream == null) {
            System.out.println("Failed to load dungeon_config.json");
        }
        DungeonConfig.load(configStream);

        new DungeonManager();
        ComponentType<EntityStore, NPCSetupPending> setupPendingType = this.getEntityStoreRegistry().registerComponent(
                NPCSetupPending.class,
                NPCSetupPending::new);
        NPCSetupPending.setComponentType(setupPendingType);

        this.getEntityStoreRegistry().registerSystem(
                new NPCInteractionSetupSystem(NPCSetupPending.getComponentType()));// Register DoorNPCComponent
        ComponentType<EntityStore, DoorNPCComponent> doorNPCType = this.getEntityStoreRegistry().registerComponent(
                DoorNPCComponent.class,
                DoorNPCComponent::new);
        DoorNPCComponent.setComponentType(doorNPCType);

        // Register OpenDoorInteraction codec
        this.getCodecRegistry(Interaction.CODEC).register(
                "open_door_type",
                OpenDoorInteraction.class,
                OpenDoorInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register(
                "talk_to_npc_type",
                TalkToNPCInteraction.class,
                TalkToNPCInteraction.CODEC);

        this.getCommandRegistry().registerCommand(new GenerateDungeonCommand("test", "An example command", false));
        this.getCommandRegistry().registerCommand(new SpawnNPCCommand());
        this.getCommandRegistry().registerCommand(new OpenPlayPageCommand());
        this.getCommandRegistry().registerCommand(new DestroyDungeonCommand());
        System.out.println("Plugin loaded");

    }

    @Override
    protected void shutdown() {
        System.out.println("Plugin shutdown");
    }
}

package com.example.plugin;

import com.example.plugin.Commands.GenerateDungeonCommand;
import com.example.plugin.Commands.OpenPlayPageCommand;
import com.example.plugin.Commands.SpawnNPCCommand; 
import com.example.plugin.Npc.Testinteractionnpc.NPCInteractionSetupSystem;
import com.example.plugin.Npc.Testinteractionnpc.NPCSetupPending;
import com.example.plugin.Npc.Testinteractionnpc.TalkToNPCInteraction;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class HelloPlugin extends JavaPlugin {

    public HelloPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        super.setup();
        System.out.println("Hello from the plugin!");
        
        // 1. Register the marker component
        ComponentType<EntityStore, NPCSetupPending> setupPendingType = 
            this.getEntityStoreRegistry().registerComponent(
                NPCSetupPending.class, 
                NPCSetupPending::new
            );
        NPCSetupPending.setComponentType(setupPendingType);

        // 2. Register the EntityTickingSystem that uses CommandBuffer
        this.getEntityStoreRegistry().registerSystem(
            new NPCInteractionSetupSystem(NPCSetupPending.getComponentType())
        );

        // 3. Register the custom interaction handler
        this.getCodecRegistry(Interaction.CODEC).register(
            "talk_to_npc_type",
            TalkToNPCInteraction.class,
            TalkToNPCInteraction.CODEC
        );

        this.getCommandRegistry().registerCommand(new GenerateDungeonCommand("test", "An example command", false));
        this.getCommandRegistry().registerCommand(new SpawnNPCCommand());
        this.getCommandRegistry().registerCommand(new OpenPlayPageCommand());
                System.out.println("Plugin loaded");

        
    }
    @Override
    protected void shutdown() {
        System.out.println("Plugin shutdown");
    }
}

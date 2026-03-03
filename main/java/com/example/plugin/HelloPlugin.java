package com.example.plugin;

import com.example.plugin.Commands.GenerateDungeonCommand;
import com.example.plugin.Commands.TestUi;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;

public class HelloPlugin extends JavaPlugin {

    public HelloPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        super.setup();
        this.getCommandRegistry().registerCommand(new GenerateDungeonCommand("test", "An example command", false));
        this.getCommandRegistry().registerCommand(new TestUi("testui", "An example command", false));
        
    }

}

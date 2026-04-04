package com.example.plugin.Ui.Hud; 

import javax.annotation.Nonnull;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class LevelHud extends CustomUIHud {

    private final Store<EntityStore> store;
    private final Ref<EntityStore> ref;

    public LevelHud(PlayerRef playerRef, Store<EntityStore> store, Ref<EntityStore> ref) {
        super(playerRef); 
        this.store = store;
        this.ref = ref;
    }

    @Override
    protected void build(@Nonnull UICommandBuilder uiCommandBuilder) {
         uiCommandBuilder.append("Pages/LevelHud.ui"); 
        
        // 3. Now this works perfectly because store and ref are saved in the class!
        var stats = com.example.plugin.Stats.PlayerLevelComponent.getStats(this.store, this.ref);
        int currentLevel = (stats != null) ? stats.level : 1;
        int currentXp = (stats != null) ? stats.xp : 0;
        int xpNeeded = currentLevel * 100;

        // --- Generate Text Progress Bar ---
        int totalBars = 20; 
        int filledBars = (int) Math.round(((double) currentXp / xpNeeded) * totalBars);
        if (filledBars > totalBars) filledBars = totalBars;

        StringBuilder barBuilder = new StringBuilder("[");
        for (int i = 0; i < totalBars; i++) {
            if (i < filledBars) {
                barBuilder.append("■");
            } else {
                barBuilder.append("-");
            }
        }
        barBuilder.append("]");
        // ----------------------------------

        // Update the UI
        uiCommandBuilder.set("#HudLevelLabel.Text", "Lv. " + currentLevel);
        uiCommandBuilder.set("#HudXpLabel.Text", currentXp + " / " + xpNeeded + " XP");
        uiCommandBuilder.set("#HudVisualBar.Text", barBuilder.toString()); 
    }
}
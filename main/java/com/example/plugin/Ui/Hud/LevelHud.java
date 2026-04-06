package com.example.plugin.Ui.Hud; 

import javax.annotation.Nonnull;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class LevelHud extends CustomUIHud {

    // 1. Store the raw stats directly
    private final int level;
    private final int xp;

    // 2. Ask for the stats in the constructor instead of the store!
    public LevelHud(PlayerRef playerRef, int level, int xp) {
        super(playerRef); 
        this.level = level;
        this.xp = xp;
    }

    @Override
    protected void build(@Nonnull UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append("Pages/LevelHud.ui"); 
        
        // 3. We don't need to look in the store anymore! Just use the variables.
        int xpNeeded = this.level * 100;

        // --- Generate Text Progress Bar ---
        int totalBars = 20; 
        int filledBars = (int) Math.round(((double) this.xp / xpNeeded) * totalBars);
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

        // Update the UI using our direct variables
        uiCommandBuilder.set("#HudLevelLabel.Text", "Lv. " + this.level);
        uiCommandBuilder.set("#HudXpLabel.Text", this.xp + " / " + xpNeeded + " XP");
        uiCommandBuilder.set("#HudVisualBar.Text", barBuilder.toString()); 
    }
}
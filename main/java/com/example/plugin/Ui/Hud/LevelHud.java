package com.example.plugin.Ui.Hud; 

import javax.annotation.Nonnull;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class LevelHud extends CustomUIHud {

    private final int level;
    private final int xp;
    private final int extractionTimeSeconds;

    public LevelHud(PlayerRef playerRef, int level, int xp) {
        this(playerRef, level, xp, -1); 
    }

    public LevelHud(PlayerRef playerRef, int level, int xp, int extractionTimeSeconds) {
        super(playerRef); 
        this.level = level;
        this.xp = xp;
        this.extractionTimeSeconds = extractionTimeSeconds;
    }

    @Override
    protected void build(@Nonnull UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append("Pages/LevelHud.ui"); 
        
        int xpNeeded = this.level * 100;

        int totalBars = 20; 
        int filledBars = (int) Math.round(((double) this.xp / xpNeeded) * totalBars);
        if (filledBars > totalBars) filledBars = totalBars;

        StringBuilder filledBuilder = new StringBuilder();
        StringBuilder emptyBuilder = new StringBuilder();

        for (int i = 0; i < totalBars; i++) {
            if (i < filledBars) {
                filledBuilder.append("=");
            } else {
                emptyBuilder.append("-");
            }
        }

        uiCommandBuilder.set("#HudVisualBarFilled.Text", filledBuilder.toString()); 
        uiCommandBuilder.set("#HudVisualBarEmpty.Text", emptyBuilder.toString());

        uiCommandBuilder.set("#HudLevelLabel.Text", "Lv. " + this.level);
        uiCommandBuilder.set("#HudXpLabel.Text", this.xp + " / " + xpNeeded + " XP");

        if (this.extractionTimeSeconds >= 0) {
            int minutes = this.extractionTimeSeconds / 60;
            int seconds = this.extractionTimeSeconds % 60;
            String formattedTime = String.format("%02d:%02d", minutes, seconds);
            
            uiCommandBuilder.set("#HudExtractionTimer.Text", "Extraction in: " + formattedTime);
        } else {
            uiCommandBuilder.set("#HudExtractionTimer.Text", ""); 
        }
    }
}
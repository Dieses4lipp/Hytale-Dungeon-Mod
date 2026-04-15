package com.example.plugin.DungeonGeneration;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class BossDeathTimerComponent implements Component<EntityStore> {
    
    private static ComponentType<EntityStore, BossDeathTimerComponent> TYPE;

    public float timeRemaining = 60.0f; 
    public int dungeonSlot;
    public int lastSentSecond = -1;

    public static ComponentType<EntityStore, BossDeathTimerComponent> getComponentType() {
        return TYPE;
    }

    public static void setComponentType(ComponentType<EntityStore, BossDeathTimerComponent> type) {
        TYPE = type;
    }

    @Nonnull
    @Override
    public BossDeathTimerComponent clone() {
        BossDeathTimerComponent cloned = new BossDeathTimerComponent();
        cloned.timeRemaining = this.timeRemaining;
        cloned.dungeonSlot = this.dungeonSlot; 
        return cloned;
    }
}
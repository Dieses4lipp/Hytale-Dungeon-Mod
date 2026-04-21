package com.example.plugin.DungeonGeneration;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class BossMinionSpawnerComponent implements Component<EntityStore> {
    
    private static ComponentType<EntityStore, BossMinionSpawnerComponent> TYPE;

    public float timer = 0.0f;
    public boolean active = false;
    public int dungeonSlot;

    public static ComponentType<EntityStore, BossMinionSpawnerComponent> getComponentType() {
        return TYPE;
    }

    public static void setComponentType(ComponentType<EntityStore, BossMinionSpawnerComponent> type) {
        TYPE = type;
    }

    @Nonnull
    @Override
    public BossMinionSpawnerComponent clone() {
        BossMinionSpawnerComponent cloned = new BossMinionSpawnerComponent();
        cloned.timer = this.timer;
        cloned.active = this.active;
        cloned.dungeonSlot = this.dungeonSlot;
        return cloned;
    }
}
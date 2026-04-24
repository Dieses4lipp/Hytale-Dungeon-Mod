package com.example.plugin.Stats;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerLevelComponent implements Component<EntityStore> {
    public int level = 1;
    public int xp = 0;
    public int gold = 0; 

    private static ComponentType<EntityStore, PlayerLevelComponent> TYPE;
    
    public static ComponentType<EntityStore, PlayerLevelComponent> getComponentType() { 
        return TYPE; 
    }
    
    public static void setComponentType(ComponentType<EntityStore, PlayerLevelComponent> type) { 
        TYPE = type; 
    }

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        PlayerLevelComponent cloned = new PlayerLevelComponent();
        cloned.level = this.level;
        cloned.xp = this.xp;
        cloned.gold = this.gold;
        return cloned;
    }

    @Nullable
    public static PlayerLevelComponent getStats(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> playerRef) {
        if (TYPE != null && store.getArchetype(playerRef).contains(TYPE)) {
            return (PlayerLevelComponent) store.getComponent(playerRef, TYPE);
        }
        return null;
    }
}
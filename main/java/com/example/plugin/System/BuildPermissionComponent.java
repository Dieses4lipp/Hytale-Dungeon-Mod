package com.example.plugin.System;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class BuildPermissionComponent implements Component<EntityStore> {
    
    private static ComponentType<EntityStore, BuildPermissionComponent> TYPE;

    public static ComponentType<EntityStore, BuildPermissionComponent> getComponentType() {
        return TYPE;
    }

    public static void setComponentType(ComponentType<EntityStore, BuildPermissionComponent> type) {
        TYPE = type;
    }

    @Nonnull
    @Override
    public BuildPermissionComponent clone() {
        return new BuildPermissionComponent();
    }
}
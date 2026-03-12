package com.example.plugin.Npc.Testinteractionnpc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class NPCSetupPending implements Component<EntityStore> {

    public static final BuilderCodec<NPCSetupPending> CODEC = BuilderCodec.builder(
        NPCSetupPending.class,
        NPCSetupPending::new
    ).build();

    private static ComponentType<EntityStore, NPCSetupPending> componentType;

    private final String interactionId;
    private final String interactionHint;

    public NPCSetupPending() {
        this("talk_to_npc", "Talk");
    }

    public NPCSetupPending(String interactionId, String interactionHint) {
        this.interactionId = interactionId;
        this.interactionHint = interactionHint;
    }

    public String getInteractionId() {
        return interactionId;
    }

    public String getInteractionHint() {
        return interactionHint;
    }

    @Nonnull
    @Override
    public NPCSetupPending clone() {
        return new NPCSetupPending(this.interactionId, this.interactionHint);
    }

    public static void setComponentType(ComponentType<EntityStore, NPCSetupPending> type) {
        componentType = type;
    }

    @Nullable
    public static ComponentType<EntityStore, NPCSetupPending> getComponentType() {
        return componentType;
    }
}
package com.example.plugin.Npc.Shopinteraction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class NPCSetupPending implements Component<EntityStore> {

    

    private static ComponentType<EntityStore, NPCSetupPending> componentType;

    private String interactionId;
    private String interactionHint;
    public static final BuilderCodec<NPCSetupPending> CODEC = BuilderCodec.builder(
        NPCSetupPending.class,
        NPCSetupPending::new
    )
    .addField(
        new KeyedCodec<String>("InteractionId", Codec.STRING),
        (NPCSetupPending component, String value) -> component.setInteractionId(value), 
        (NPCSetupPending component) -> component.getInteractionId()
    )
    .addField(
        new KeyedCodec<String>("InteractionHint", Codec.STRING),
        (NPCSetupPending component, String value) -> component.setInteractionHint(value), 
        (NPCSetupPending component) -> component.getInteractionHint()
    )
    .build();

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
    public void setInteractionId(String interactionId) {
        this.interactionId = interactionId;
    }

    public void setInteractionHint(String interactionHint) {
        this.interactionHint = interactionHint;
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
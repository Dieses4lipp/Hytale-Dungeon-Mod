package com.example.plugin.doorsystem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class DoorNPCComponent implements Component<EntityStore> {

    public static final BuilderCodec<DoorNPCComponent> CODEC = BuilderCodec.builder(
            DoorNPCComponent.class,
            DoorNPCComponent::new)
            .addField(
                    new KeyedCodec<Vector3i>("DoorPos", Vector3i.CODEC), 
                    (DoorNPCComponent component, Vector3i value) -> component.setDoorPos(value),
                    (DoorNPCComponent component) -> component.getDoorPos())
            .<String>addField(
                    new KeyedCodec<String>("Orientation", Codec.STRING), 
                    (DoorNPCComponent component, String value) -> {
                        component.setOrientation(DoorRegistry.Orientation.valueOf(value));
                    },
                    (DoorNPCComponent component) -> component.getOrientation().name())
            .build();

    private static ComponentType<EntityStore, DoorNPCComponent> componentType;

    private Vector3i doorPos;
    private DoorRegistry.Orientation orientation;

    public DoorNPCComponent() {
        this(new Vector3i(0, 0, 0), DoorRegistry.Orientation.SN);
    }

    public DoorNPCComponent(Vector3i doorPos, DoorRegistry.Orientation orientation) {
        this.doorPos = doorPos;
        this.orientation = orientation;
    }

    public Vector3i getDoorPos() {
        return doorPos;
    }

    public DoorRegistry.Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(DoorRegistry.Orientation orientation) {
        this.orientation = orientation;
    }

    public void setDoorPos(Vector3i doorPos) {
        this.doorPos = doorPos;
    }

    public static void setComponentType(ComponentType<EntityStore, DoorNPCComponent> type) {
        componentType = type;
    }

    @Nullable
    public static ComponentType<EntityStore, DoorNPCComponent> getComponentType() {
        return componentType;
    }

    @Nonnull
    @Override
    public DoorNPCComponent clone() {
        return new DoorNPCComponent(doorPos, orientation);
    }
}
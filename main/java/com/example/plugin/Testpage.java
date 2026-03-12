package com.example.plugin;


import javax.annotation.Nonnull;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;


public class Testpage extends InteractiveCustomUIPage<Testpage.Data> {

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
            // Field 1: Input Value
            .append(new KeyedCodec<>("@MyInput", Codec.STRING), 
                (data, value) -> data.value = value, 
                data -> data.value)
            .add() 

            // Field 2: Button Click State
            .append(new KeyedCodec<>("ButtonClicked", Codec.STRING), 
                (data, value) -> data.clickedButton = value, 
                data -> data.clickedButton)
            .add() 

            .build();
        private String value;
        private String clickedButton;
    }

    public Testpage(PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder,
            @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/TestPage.ui");
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#MyInput",
                EventData.of("@MyInput", "#MyInput.Value"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#SaveBtn",
                EventData.of("ButtonClicked", "save"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ResetBtn",
                EventData.of("ButtonClicked", "reset"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#CloseBtn",
                EventData.of("ButtonClicked", "close"), false);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, Data data) {
        super.handleDataEvent(ref, store, data);

        System.out.println("EVENT: " + data.value);
        if (data.clickedButton != null) {
        switch (data.clickedButton) {
            case "save"  -> System.out.println("Save button was clicked!");
            case "reset" -> System.out.println("Reset button was clicked!");
            case "close" -> System.out.println("Close button was clicked!");
        }
        data.clickedButton = null; // reset so it doesn't fire again on next event
    }

        sendUpdate();
    }
}

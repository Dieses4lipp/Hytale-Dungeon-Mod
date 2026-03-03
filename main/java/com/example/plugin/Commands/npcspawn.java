package com.example.plugin.Commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;

import it.unimi.dsi.fastutil.Pair;
public class npcspawn {
    public void spawnnpc(Store<EntityStore> store) {
        

Vector3d position = new Vector3d(0, 100, 0); // Example spawn position
Vector3f rotation = new Vector3f(0, 0, 0);
// Spawn the NPC
Pair<Ref<EntityStore>, INonPlayerCharacter> result = NPCPlugin.get().spawnNPC(
    store,              // The entity store
    "Kweebec_Sapling",  // Entity model/type key
    null,               // Optional configuration
    position,           // Vector3d spawn position
    rotation            // Vector3f orientation
);

if (result != null) {
    Ref<EntityStore> npcRef = result.first();   // Entity reference for ECS operations
    INonPlayerCharacter npc = result.second();  // NPC-specific interface
}
    }
}

package com.example.plugin.System;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;

public class StarterKit {
    public static void giveStarterKit(Player player){

        if (player == null)
            return;

        String helmetId = "Armor_Leather_Light_Head";
        String chestId = "Armor_Leather_Light_Chest";
        String legsId = "Armor_Leather_Light_Legs";
        String handId = "Armor_Leather_Light_Hands";

        String swordId = "Weapon_Sword_Copper";
        String shieldId = "Weapon_Shield_Wood";
        String potionId = "Potion_Health_Small";

        ItemStack head = new ItemStack(helmetId, 1);
        player.getInventory().getArmor().addItemStackToSlot((short) 0, head);

        ItemStack chest = new ItemStack(chestId, 1);
        player.getInventory().getArmor().addItemStackToSlot((short) 1, chest);

        ItemStack feet = new ItemStack(handId, 1);
        player.getInventory().getArmor().addItemStackToSlot((short) 2, feet);

        ItemStack legs = new ItemStack(legsId, 1);
        player.getInventory().getArmor().addItemStackToSlot((short) 3, legs);

        ItemStack sword = new ItemStack(swordId, 1);
        player.getInventory().getHotbar().addItemStackToSlot((short) 0, sword);

        ItemStack shield = new ItemStack(shieldId, 1);
        player.getInventory().getUtility().addItemStackToSlot((short) 1, shield);

        ItemStack potion = new ItemStack(potionId, 5);
        player.getInventory().getHotbar().addItemStackToSlot((short) 8, potion);
    }
}

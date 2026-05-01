# VaultsAndVillains
# TABLE OF CONTENTS

* [INSTALLATION](#installation)
* [HOW TO PLAY](#how-to-play)


# INSTALLATION

## Package Content
Upon downloading, your folder should look like this
* **Dungeongenerator.invisnpc/** (Folder containing custom models and UI)
* **dungeon_generator_v1.jar** (The core server plugin)
* **default/** (The pre-configured worlds folder)
* **prefabs/** (The prefabs used for creating the dungeon)


## Automated Installation (Recommended)

To make the installation easier, you can use the provided scripts.

### For Windows (InstallWindows.bat)
Put all of your files including the `InstallWindows.bat` inside the Root Folder of your Server the one where the `mods` and `worlds`are located. Then just run it.

### For Linux (InstallLinux.sh)
Put all of your files including the `InstallLinux.sh` inside the Root Folder of your Server (the one where the `mods` and `worlds`are located) and make it executable (`chmod +x install.sh`). Then just run it.


## Manual Installation

If you prefer to install the components yourself, follow these steps to match the required server structure:

### 1. Plugin and Assets
* Locate your server's **mods** folder.
* Copy the **DungeonMinigame.jar** file into the **mods** folder.
* Copy the entire **Dungeongenerator.invisnpc** folder into the **mods** folder.

### 2. World Setup
* Navigate to your server's **worlds** directory.
* Locate the folder named **default** (this is usually the standard worlds).
* **Delete or rename** the existing **default** folder to create a backup.
* Move the **default** folder from this download into the server's **worlds** directory.

### 3. Prefab Setup
* Move the **prefabs** folder from this download into the server's **root** directory.

### 3. Verification
After installation, your server structure should look like this:
* `ServerRoot/mods/DungeonMinigame.jar`
* `ServerRoot/mods/Dungeongenerator.invisnpc/...`
* `ServerRoot/worlds/default/...`
* `ServerRoot/prefabs/...`

## Final Steps
1. Ensure all files have been moved correctly.
2. Restart your Hytale server.
3. Use the admin commands (`/npc clean --confirm` and`/setuphub`) if the Npcs arent reacting to your Interaction

**Important:** Do not modify the folder names within the Assetpack or the Worlds folder, as the plugin relies on these specific paths to load the dungeon segments and UI definitions.

# HOW TO PLAY

Welcome to the dungeon! The core gameplay loop revolves around preparing your gear, surviving the depths, and extracting with your hard-earned loot before you lose it.

### Getting Started in the Hub
When you first load into the hub, you will find two main NPCs that serve as your gateway to the minigame:
* **Enter Dungeon:** Interact with this NPC when you are ready to start your run and enter the dungeon.
* **Deposit Items:** Interact with this NPC to access your Stash, manage your saved inventory, and safely equip your character.

If you are just starting out, use the command `**/starterkit**` to receive a basic set of equipment.

### Inventory and The Risk System
Preparation is key. Before speaking to the Dungeon Guide to enter, ensure you have equipped yourself properly using your Stash.
* **High Risk, High Reward:** Any items placed in your normal active inventory and taken into the dungeon are at risk. If you die during your run, everything in your active inventory will be lost.
* **Safe Storage:** Leave your most prized possessions and backup gear in your Stash to ensure they are safe from death.

### Surviving the Dungeon
Once inside, you will need to fight your way through hordes of challenging mobs. You can open the 3x3 Doors with pressing your interact Button. As you progress, keep an eye out for these points of interest:
* **Treasure Rooms:** Discover high-tier loot and valuable gear to take back to the hub.
* **Shops:** Spend the gold your earn to purchase helpful items mid-run.
* **Safe Chests:** Find these rare chests to securely stash specific items, ensuring they are saved even if you don't survive the whole dungeon.

### The Extraction
The ultimate goal of every run is to make it out alive. You must fight completely through the dungeon's segments until you locate an Bossroom. Successfully beating the Boss is the only way to officially save the new loot you have gathered in your active inventory and bring it back to your permanent Stash!

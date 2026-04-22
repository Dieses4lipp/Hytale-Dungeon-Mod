package com.example.plugin.DungeonGeneration;

import java.nio.file.Path;

public enum RoomType {

    NORMAL(1, 1, new String[] {
            "prefabs/Prefabs/Normal/room1.prefab.json",
            "prefabs/Prefabs/Normal/room2.prefab.json",
            "prefabs/Prefabs/Normal/room3.prefab.json"
    }),
    HALLWAY(1, 1, new String[] {
            "prefabs/Prefabs/Hallway/hallway1.prefab.json",
            "prefabs/Prefabs/Hallway/hallway2.prefab.json"
    }),
    TREASURE(1, 1, new String[] {
            "prefabs/Prefabs/Treasure/treasure1.prefab.json",
            "prefabs/Prefabs/Treasure/treasure2.prefab.json"
    }),
    BOSS(3, 3, new String[] {
            "prefabs/Prefabs/Boss/boss1.prefab.json"
    }),
    SHOP(1, 1, new String[] {
            "prefabs/Prefabs/Shop/shop_north.prefab.json", // door facing North (0)
            "prefabs/Prefabs/Shop/shop_east.prefab.json", // door facing East  (1)
            "prefabs/Prefabs/Shop/shop_south.prefab.json", // door facing South (2)
            "prefabs/Prefabs/Shop/shop_west.prefab.json"// door facing West  (3)
    }),
    STASH(1, 1, new String[] {
            "prefabs/Prefabs/Stash/stash1.prefab.json"
    });

    public final int gridWidth;
    public final int gridHeight;
    private final String[] prefabPaths;

    RoomType(int gridWidth, int gridHeight, String[] prefabPaths) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.prefabPaths = prefabPaths;
    }

    public Path getRandomPrefabPath() {
        int idx = (int) (Math.random() * prefabPaths.length);
        return Path.of(prefabPaths[idx]);
    }
    public Path getPrefabPathForDirection(int doorDirection) {
    int idx = Math.max(0, Math.min(doorDirection, prefabPaths.length - 1));
    return Path.of(prefabPaths[idx]);
}
}
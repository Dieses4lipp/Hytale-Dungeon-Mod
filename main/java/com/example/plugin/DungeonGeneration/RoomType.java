package com.example.plugin.DungeonGeneration;

import java.nio.file.Path;

public enum RoomType {

    NORMAL(1, 1, new String[] {
            "prefabs/Prefabs/testroom1.prefab.json",
            "prefabs/Prefabs/testroom2.prefab.json",
            "prefabs/Prefabs/testroom3.prefab.json"
    }),
    HALLWAY(1, 1, new String[] {
            "prefabs/Prefabs/hallway1.prefab.json",
            "prefabs/Prefabs/hallway2.prefab.json"
    }),
    TREASURE(1, 1, new String[] {
            "prefabs/Prefabs/treasure1.prefab.json",
            "prefabs/Prefabs/treasure2.prefab.json"
    }),
    BOSS(3, 3, new String[] {
            "prefabs/Prefabs/boss1.prefab.json",
            "prefabs/Prefabs/boss2.prefab.json",
            "prefabs/Prefabs/boss3.prefab.json"
    }),
    SHOP(1, 1, new String[] {
            "prefabs/Prefabs/shop_north.prefab.json", // door facing North (0)
            "prefabs/Prefabs/shop_east.prefab.json", // door facing East  (1)
            "prefabs/Prefabs/shop_south.prefab.json", // door facing South (2)
            "prefabs/Prefabs/shop_west.prefab.json"// door facing West  (3)
    }),
    STASH(1, 1, new String[] {
            "prefabs/Prefabs/stash1.prefab.json"
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
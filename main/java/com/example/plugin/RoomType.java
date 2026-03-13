package com.example.plugin;

import java.nio.file.Path;

public enum RoomType {

    NORMAL(1, 1, new String[]{
        "prefabs/Prefabs/testroom1.prefab.json",
        "prefabs/Prefabs/testroom2.prefab.json",
        "prefabs/Prefabs/testroom3.prefab.json"
    }),
    HALLWAY(1, 1, new String[]{
        "prefabs/Prefabs/hallway1.prefab.json",
        "prefabs/Prefabs/hallway2.prefab.json"
    }),
    TREASURE(1, 1, new String[]{
        "prefabs/Prefabs/treasure1.prefab.json",
        "prefabs/Prefabs/treasure2.prefab.json"
    }),
    BOSS(3, 3, new String[]{
        "prefabs/Prefabs/boss1.prefab.json",
        "prefabs/Prefabs/boss2.prefab.json",
        "prefabs/Prefabs/boss3.prefab.json"
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
}
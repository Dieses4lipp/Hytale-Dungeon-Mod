package com.example.plugin.DungeonGeneration;

public class Room {
    // norden:0 osten:1 süden:2 westen:3
        public boolean[] doors =new boolean[4];

        public boolean[] getDoors() {
            return doors;
        }
}

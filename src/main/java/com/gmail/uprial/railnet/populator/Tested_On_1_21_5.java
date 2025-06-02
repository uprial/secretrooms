package com.gmail.uprial.railnet.populator;

/*
    Technical interface that emphasizes manual test quality.

    ==== Test server configuration ====

    Server: Paper
    Version: 1.21.5
    Seed: -1565193744182814265 (Belongings 2025-01-12)

    TerraformGenerator-19.1.0
    $ grep oceanic- plugins/TerraformGenerator/config.yml
    oceanic-frequency: 0.11
    oceanic-threshold: 8.0
    deep-oceanic-threshold: 27.0

    WorldBorder
    world:          4050 x 4050
    world_nether:   300  x 300
    world_the_end:  4050 x 4050

    Collected test data will be marked by a "#" prefix.

    ==== ConsistencyReference#1 ====

    Titles better have a consistent number of spaces to keep commands working:
    | cut -d' ' -f12

    Furnaces:
    [DEBUG] FURNACE[world:-139:-9:-143] result item IRON_INGOT set to 8
    Blocks:
    [DEBUG] CHEST[world:609:46:-246] item #4 ZOMBIE_HEAD set to 1
    Dungeons:
    [DEBUG] CHEST[world:203:21:989] item #0 SPLASH_POTION set to 1

 */
public interface Tested_On_1_21_5 {
}

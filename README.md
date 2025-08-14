![SecretRooms Logo](images/secretrooms-logo.png)

## Compatibility

Tested on Spigot-1.21.5, 1.21.6.

## Introduction

Motivate players to explore the map by secret buildings with rare loot underground, underwater, and in the End.

## Features

* Softly depend on [TakeAim](https://github.com/uprial/takeaim)
* Uncommon, rare, and epic loot in chests with higher probability in pyramids, ancient cities, mansions and bastions
* Fuel and ingots in furnaces
* Whirlpools in water with loot chests under magma blocks
* 0.01% of appropriate blocks are infested
* End Ships have Illusioners pre-generated
* Underground dungeons with spawners, building blocks, and items enchanted impossibly for survival
* Loot density increases according to distance from the map center
* The End has hardened mansions with epic loot
* Enchantment limit of loot: pickaxes - Fortune-5 and Efficiency-10, tridents - Loyalty-5, bows - Power-10, cloths - Protection-5, swords - Sharpness-10, fishing rods - Luck-of-the-sea-5, Lure-5, Unbreaking-5.

That plugin combines all the fun I had with my friends that I didn't find in other plugins and didn't manage to split into other my plugins. If you like any changes - please request them specifically, and I'll create a separate configurable plugin.

#### Use cases

Find a whirlpool, then a chest, and loot it

![Find a whirlpool](https://raw.githubusercontent.com/uprial/secretrooms/master/images/find-a-whirlpool.png)

Find furnaces in mineshaft, check your luck

![Find furnaces](https://raw.githubusercontent.com/uprial/secretrooms/master/images/find-furnaces.png)

Find a soul torch, go down, go up, loot

![Find a dungeon](https://raw.githubusercontent.com/uprial/secretrooms/master/images/find-a-dungeon.png)

Detect an End Mansion by being attacked, find a chest inside, loot it

![Find a mansion](https://raw.githubusercontent.com/uprial/secretrooms/master/images/find-a-mansion.png)

Build a turret from an End Crystal and a Heavy Core or a Dragon Head above it

![Build a turret](https://raw.githubusercontent.com/uprial/secretrooms/master/images/build-a-turret.png)

## Commands

`secretrooms repopulate-loaded <radius>` - repopulate loaded terrain around player

`secretrooms repopulate-loaded <world> <x> <z> <radius>` - repopulate loaded terrain

`secretrooms claim <density>` - generate player inventory like it's a chest

`secretrooms break <radius>` - break terrain around player

`secretrooms break <world> <x> <y> <z> <radius>` - break terrain

`secretrooms loaded-stats <material>` - show material stats in loaded terrain

`secretrooms loaded-locations <material>` - show material locations in loaded terrain

## Permissions

* Access to 'repopulate-loaded' command:
`secretrooms.repopulate-loaded` (default: op)
* Access to 'claim' command:
`secretrooms.claim` (default: op)
* Access to 'break' command:
`secretrooms.break` (default: op)
* Access to 'loaded-stats' command:
`secretrooms.loaded-stats` (default: op)
* Access to 'loaded-locations' command:
`secretrooms.loaded-locations` (default: op)

## Configuration
[Default configuration file](src/main/resources/config.yml)

## Author
I will be happy to add some features or fix bugs. My mail: uprial@gmail.com.

## Useful links
* [Project on GitHub](https://github.com/uprial/secretrooms)
* [Project on Bukkit Dev](https://legacy.curseforge.com/minecraft/bukkit-plugins/secret-rooms/)
* [Project on Spigot](https://www.spigotmc.org/resources/secret-rooms.121505/)

## Related projects
* CustomBazookas: [Bukkit Dev](https://legacy.curseforge.com/minecraft/bukkit-plugins/custombazookas/) [GitHub](https://github.com/uprial/custombazookas), [Spigot](https://www.spigotmc.org/resources/custombazookas.124997/)
* CustomCreatures: [Bukkit Dev](http://dev.bukkit.org/bukkit-plugins/customcreatures/), [GitHub](https://github.com/uprial/customcreatures), [Spigot](https://www.spigotmc.org/resources/customcreatures.68711/)
* CustomNukes: [Bukkit Dev](http://dev.bukkit.org/bukkit-plugins/customnukes/), [GitHub](https://github.com/uprial/customnukes), [Spigot](https://www.spigotmc.org/resources/customnukes.68710/)
* CustomRecipes: [Bukkit Dev](https://dev.bukkit.org/projects/custom-recipes), [GitHub](https://github.com/uprial/customrecipes/), [Spigot](https://www.spigotmc.org/resources/customrecipes.89435/)
* CustomVillage: [Bukkit Dev](http://dev.bukkit.org/bukkit-plugins/customvillage/), [GitHub](https://github.com/uprial/customvillage/), [Spigot](https://www.spigotmc.org/resources/customvillage.69170/)
* MasochisticSurvival: [Bukkit Dev](https://legacy.curseforge.com/minecraft/bukkit-plugins/masochisticsurvival/), [GitHub](https://github.com/uprial/masochisticsurvival/), [Spigot](https://www.spigotmc.org/resources/masochisticsurvival.124943/)
* TakeAim: [Bukkit Dev](https://dev.bukkit.org/projects/takeaim), [GitHub](https://github.com/uprial/takeaim), [Spigot](https://www.spigotmc.org/resources/takeaim.68713/)

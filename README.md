![RailNet Logo](images/railnet-logo.png)

## Compatibility

Tested on Spigot-1.21.5.

## Introduction

Underground railways, rare loot.

## Features

* Underground railways from a spawn location to a woodland mansion and an ocean monument with ladders from above at stations (disabled by default in config.yml)
* Uncommon, rare, and epic loot in chests with higher probability in pyramids, mansions and bastions
* Fuel and ingots in furnaces
* Whirlpools in water with loot chests under magma blocks
* Lime-colored stained glass and panels are explosive-resistant
* 0.01% of appropriate blocks are infested
* End Ships have Illusioners pre-generated
* Underground dungeons with spawners and rare building blocks
* Loot density increases according to distance from the map center

That plugin combines all the fun I had with my friends that I didn't find in other plugins and didn't manage to split into other my plugins. If you like any changes - please request them specifically, and I'll create a separate configurable plugin.

#### Use cases

Find an entrance, take a minecart, enjoy your ride, discover a struct (disabled by default in config.yml)

![Find a monument](https://raw.githubusercontent.com/uprial/railnet/master/images/find-a-monument.png)

Find a whirlpool, then a chest, and loot it

![Find a whirlpool](https://raw.githubusercontent.com/uprial/railnet/master/images/find-a-whirlpool.png)

Find furnaces in mineshaft, check your luck

![Find furnaces](https://raw.githubusercontent.com/uprial/railnet/master/images/find-furnaces.png)

Find a soul torch, go down, go up, loot

![Find a dungeon](https://raw.githubusercontent.com/uprial/railnet/master/images/find-a-dungeon.png)


## Commands

`railnet repopulate-loaded <radius>` - repopulate loaded terrain around player

`railnet repopulate-loaded <world> <x> <z> <radius>` - repopulate loaded terrain

`railnet claim <density>` - generate player inventory like it's a chest

`railnet break <radius>` - break terrain around player

`railnet break <world> <x> <y> <z> <radius>` - break terrain

`railnet loaded-stats <material>` - show material stats in loaded terrain

## Permissions

* Access to 'repopulate-loaded' command:
`railnet.repopulate-loaded` (default: op)
* Access to 'claim' command:
`railnet.claim` (default: op)
* Access to 'break' command:
`railnet.break` (default: op)
* Access to 'loaded-stats' command:
`railnet.loaded-stats` (default: op)

## Configuration
[Default configuration file](src/main/resources/config.yml)

## Author
I will be happy to add some features or fix bugs. My mail: uprial@gmail.com.

## Useful links
* [Project on GitHub](https://github.com/uprial/railnet)
* [Project on Bukkit Dev](https://legacy.curseforge.com/minecraft/bukkit-plugins/rails-chests/)
* [Project on Spigot](https://www.spigotmc.org/resources/rails-chests.121505/)

## Related projects
* CustomBazookas: [Bukkit Dev](https://legacy.curseforge.com/minecraft/bukkit-plugins/custombazookas/) [GitHub](https://github.com/uprial/custombazookas), [Spigot](https://www.spigotmc.org/resources/custombazookas.124997/)
* CustomCreatures: [Bukkit Dev](http://dev.bukkit.org/bukkit-plugins/customcreatures/), [GitHub](https://github.com/uprial/customcreatures), [Spigot](https://www.spigotmc.org/resources/customcreatures.68711/)
* CustomNukes: [Bukkit Dev](http://dev.bukkit.org/bukkit-plugins/customnukes/), [GitHub](https://github.com/uprial/customnukes), [Spigot](https://www.spigotmc.org/resources/customnukes.68710/)
* CustomRecipes: [Bukkit Dev](https://dev.bukkit.org/projects/custom-recipes), [GitHub](https://github.com/uprial/customrecipes/), [Spigot](https://www.spigotmc.org/resources/customrecipes.89435/)
* CustomVillage: [Bukkit Dev](http://dev.bukkit.org/bukkit-plugins/customvillage/), [GitHub](https://github.com/uprial/customvillage/), [Spigot](https://www.spigotmc.org/resources/customvillage.69170/)
* MasochisticSurvival: [Bukkit Dev](https://legacy.curseforge.com/minecraft/bukkit-plugins/masochisticsurvival/), [GitHub](https://github.com/uprial/masochisticsurvival/), [Spigot](https://www.spigotmc.org/resources/masochisticsurvival.124943/)
* TakeAim: [Bukkit Dev](https://dev.bukkit.org/projects/takeaim), [GitHub](https://github.com/uprial/takeaim), [Spigot](https://www.spigotmc.org/resources/takeaim.68713/)

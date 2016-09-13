![Blockbuster](./logo.png) 

# Blockbuster

Blockbuster (**pun intended**) is a Minecraft mod which lets you create simple 
Minecraft machinimas in single player (without having to recruit and organize a 
crowd of actors and cameras) and simple cinematics in adventure maps.

Blockbuster mod is built on top of Forge 12.17.0.1976 for Minecraft 1.9.4, and 
the recording code is based on the code from the 
[Mocap mod](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/1445402-minecraft-motion-capture-mod-mocap-16-000) 
(the author of the mod gave me permission to use his code). 

Original [minecraft forum thread](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2700216-blockbuster-create-simple-machinimas-and-adventure) 
and [planet minecraft post](http://www.planetminecraft.com/mod/blockbuster-machinima-mod/).

## Install

Install [Minecraft Forge](http://files.minecraftforge.net/), then go to 
[releases](https://github.com/mchorse/blockbuster/releases) and download the 
latest version jar file. Put it in minecraft's `mods` folder, and launch the game. 

After that, Blockbuster mod should be installed and will appear in Minecraft's 
mods menu. If Blockbuster didn't appear in the mods menu, then something went 
wrong.

## Videos

### Tutorial video

Tutorial for version 1.2. This tutorial covers all items, blocks, and commands 
that this mod adds into the game. It's also covers the concepts it introduces, 
and shows how to use this mod. Machinima intro included.

<a href="https://youtu.be/mDCYX1oRKYk?list=PL6UPd2Tj65nHvEH-_F_brz6LQDdlsCIXJ">
    <img src="https://img.youtube.com/vi/mDCYX1oRKYk/0.jpg">
</a>

### Machinima Examples

This playlist consists out of videos that I've created during Blockbuster mod testings. Those videos are my lab experiments. Don't judge the quality of these machinimas yet, I'm still learning.

<a href="https://youtu.be/PyAO7DOdL00?list=PL6UPd2Tj65nFdhjzY-z6yCJuPaEanB2BF">
    <img src="https://img.youtube.com/vi/PyAO7DOdL00/0.jpg">
</a>

## Features

This mod provides following features:

#### Player's recording

* All stuff in Mocap, but more
* Text formatting in chat using the `[` character instead of `§`
* Interacting with blocks (opening doors, toggling levers, pushing buttons, etc.)
* Breaking blocks
* Mounting entities like pigs (tested with AnimalBikes, works well, but keep 
  animals in fences)
* Flying the elytra

#### Director blocks

* Has two variations: for machinimas and for adventure maps
* Ties actors into an organizable scene (with lots of benefits)
* Can be playbacked by using the playback button or the `/director play` command
* Both of the block have their own GUIs for managing the cast (view, add, edit, 
  remove, reset)

#### Actors

* Can playback player's actions
* Customizable skins (simply drop 64x32 skins into the minecraft/config/blockbsuter/skins folder)
* Mostly look like players
* When tied to director block and player starts recording this actor, player 
  will be able to react to previously recorded actors
* When recording, HUD overlay would be displayed with caption to which file it 
  records actions

#### Cameras

* Flexible and complex customizations of cameras
* Camera profiles – saveable and loadable list of camera fixtures
* Camera fixtures – constructing blocks of camera profiles, they define how the camera 
  moves. Following fixtures are provided by the mod:
    * Idle fixture – static looking at camera
    * Path fixture – smooth path which camera follows
    * Look fixture – look fixture which keeps focus on a given entity
    * Follow fixture – GoPro fixture
    * Circular fixture – fixture that rotates around the given point with 
      specified offset and distance
* Keyboard bindings that allows almost full control of camera profile

#### Commands

* Action command (`/action <play|record|stop>`) – allows players to record their 
  actions to a file and playback recorded actions
* Director command (`/director <play|stop>`) – allows players to trigger or stop 
  playback in director block specified at XYZ position
* Camera command (`/camera`) – allows players to customize camera profiles

## Manual

Manual is located in repository's [wiki](https://github.com/mchorse/blockbuster/wiki).

## License

See the file `LICENSE.md`. Most of the code is licensed under MIT license, but 
recording code from Mocap mod is actually licensed under GPL license 
(`mchorse.blockbuster.recording` package).

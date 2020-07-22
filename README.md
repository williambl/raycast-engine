# Raycast Engine

Raycast Engine is a multipurpose game engine. It is designed for Pseudo-3D games (Wolfenstein-3D
style).

It has:

 - Multiplayer
 - Pseudo-3D rendering from a 2D map
 - Fast, quadtree-based collision checking
 - Multithreading, with rendering and game ticks running on separate threads

The engine loads levels (known as *worlds*) from JSON files. JSON worlds are then deserialized
and loaded into the game. A world file can specify a custom deserializer to be used, allowing
for custom logic when loading in the world.

The default world serializer is powerful, and can create game objects from a class name and
arguments for the constructor. The engine contains a large amount of \[de-\]serialization
utilities, so custom world serializers can be made with ease.

The engine has first-class support for add-ons/mods: an installation will contain a `mods`
directory, and jars placed in there will be loaded into the classpath (there will, at some point,
be a more sophisticated system for loading mods.)

The engine supports single- and multi-player games: multiplayer support is easy to add to your
game objects, by adding the correct serialization methods. Member variables of game objects can
be set to automatically sync from server to client simply using the `synced` delegate.

The engine is still very WIP - planned features include:

 - A main menu
 - Leaving/rejoining worlds
 - HUD/in-game GUI support
 - An inventory for players
 - A debug console in-game, quake-style
 
## Creating add-ons/mods

Firstly, add the raycast-engine jar to your libraries. Then, just write some classes, such as
`GameObject`s or `World`s, and build the jar. No need to register your mod or anything. Then
add your jar to the `mods` folder and run!

# Cat Race

Cat race is a 3D cross-platform (desktop and Android) multiplayer racing game. Players start off a small distance from the vehicles, and must run to the car they want to drive. The terrain and roads are generated as the players go farther away from the starting point, so the race could potentially go on forever. 

- [Current Features](#current-features)
- [Link to source files](https://github.com/rabbort/Cat-Race/tree/master/core/src/com/mygdx/game)

##Current Features

- Procedural Generation

Currently, Cat World uses noise to generate terrain in real-time. This allows the game to have "infinite" terrain. 
Terrain is generated in a 128x128 square, using a 2D noise array as a heightmap. As the player approaches the edge of the current terrain square, new ones are generated and rendered. Roads are generated using a seeded random number generator which tells the road to go left, right, or straight. The seed ensures all players will be seeing the same thing.

In order to help with mobile performance, each terrain square is scaled up a large amount so that not as much needs to be rendered and new squares don't need to be genereated nearly as often.

- Skybox

A simple skydome is implemented. It stays centered on the player to keep the player from reaching the end of it.

- Multiplayer

Multiplayer is implemented using Kryonet. Frequent updates (player/vehicle movments) are sent over UDP. TCP is used for logging in, disconnects, and other important information.

Terrain is generated client-side.

- Vehicles
- 
There are a couple vehicles to choose from, which spawn at the beginning of the game. Only one player can be in a vehicle at a time. They are implemented using Bullet's btRaycastVehicle.

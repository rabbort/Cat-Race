# Cat World

(This is currently my active project, so will be going through many changes over time)

- [Current Features](#current-features)
- [Planned Features](#planned-features)

##Current Features

- Procedural Generation

Currently, Cat World uses noise to generate terrain in real-time. This allows the game to have "infinite" terrain. 
Terrain is generated in a 128x128 square, using the noise array as a heightmap. As the player approaches the edge of the current 
terrain square, new ones are generated and rendered, while far away squares are disposed of. 

In order to help with mobile performance, each terrain square is scaled up a large amount so that not as much needs to be rendered
and new squares don't need to be genereated nearly as often.

- Skybox

A simple skydome is implemented. It stays centered on the player to keep the player from reaching the end of it.

##Planned Features

- Multiplayer

- Vehicles

- Buildings

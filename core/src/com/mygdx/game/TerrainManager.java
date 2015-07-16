package com.mygdx.game;

import com.badlogic.gdx.math.Vector3;

public class TerrainManager extends BaseBulletTest
{	
	private static final String[] chunkLocation = 
		{ 	
			"NW2", "NL", "NM", "NR", "NE2",
			"WT", "NW", "N", "NE", "ET",
			"WM", "W", "C", "E", "EM",
			"WB", "SW", "S", "SE", "EB",
			"SW2", "SL", "SM", "SR", "SE2" 
		};
	
	private int xOffsetNegative;
	private int xOffsetPositive;
	private int zOffsetNegative;
	private int zOffsetPositive;
	private Terrain[] terrainChunks;
	private BaseBulletTest base;
	private Vector3 center = new Vector3();
	
	public TerrainManager()
	{
		
	}
	
	public TerrainManager(Vector3 position, BaseBulletTest base)
	{
		this.base = base;
		this.center = position;
		this.center.x = (int)this.center.x;
		this.center.z = (int)this.center.z;
		
		terrainChunks = new Terrain[25];
		
		// Create each terrain chunk based on the starting position
		/*for(int i = 0; i < 25; i++)
		{
			terrainChunks[i] = new Terrain(this.base, chunkLocation[i], position);
		}*/
		terrainChunks[0] = new Terrain(this.base, chunkLocation[12], new Vector3(-256,0,0));
		
		xOffsetNegative = (int)position.x - 64;
		xOffsetPositive = (int)position.x + 64;
		zOffsetNegative = (int)position.z - 64;
		zOffsetPositive = (int)position.z + 64;
	}
	
	public void update(Vector3 playerPosition)
	{
		//for(int i = 0; i < 25; i++)
			//terrainChunks[i].render();
		terrainChunks[0].render();
		
		// Shift terrain chunks over one space, create new ones in the empty spots
		if(playerPosition.x > xOffsetPositive)
		{
			// update the center
			this.center.x += 128;

			new Terrain(this.base, chunkLocation[4], this.center);
			new Terrain(this.base, chunkLocation[9], this.center);
			new Terrain(this.base, chunkLocation[14], this.center);
			new Terrain(this.base, chunkLocation[19], this.center);
			new Terrain(this.base, chunkLocation[24], this.center);
			
			xOffsetPositive += 128;
			xOffsetNegative += 128;
			System.out.println("positive x "+this.center);
		}
		else if(playerPosition.x < xOffsetNegative)
		{
			//update the center
			this.center.x -= 128;
			
			new Terrain(this.base, chunkLocation[0], this.center);
			new Terrain(this.base, chunkLocation[5], this.center);
			new Terrain(this.base, chunkLocation[10], this.center);
			new Terrain(this.base, chunkLocation[15], this.center);
			new Terrain(this.base, chunkLocation[20], this.center);
			
			xOffsetPositive -= 128;
			xOffsetNegative -= 128;
			System.out.println("neg x "+this.center);
		}
		
		if(playerPosition.z > zOffsetPositive)
		{
			//update the center
			this.center.z += 128;

			new Terrain(this.base, chunkLocation[0], this.center);
			new Terrain(this.base, chunkLocation[1], this.center);
			new Terrain(this.base, chunkLocation[2], this.center);
			new Terrain(this.base, chunkLocation[3], this.center);
			new Terrain(this.base, chunkLocation[4], this.center);
			
			zOffsetPositive += 128;
			zOffsetNegative += 128;
			System.out.println("pos z "+this.center);
		}
		else if(playerPosition.z < zOffsetNegative)
		{
			//update the center
			this.center.z -= 128;
			
			for(int i = 0; i < terrainChunks.length - 5; i++)
			{
				terrainChunks[i] = terrainChunks[i + 5];
			}

			terrainChunks[20] = new Terrain(this.base, chunkLocation[20], this.center);
			terrainChunks[21] = new Terrain(this.base, chunkLocation[21], this.center);
			terrainChunks[22] = new Terrain(this.base, chunkLocation[22], this.center);
			terrainChunks[23] = new Terrain(this.base, chunkLocation[23], this.center);
			terrainChunks[24] = new Terrain(this.base, chunkLocation[24], this.center);
			
			zOffsetPositive -= 128;
			zOffsetNegative -= 128;
			System.out.println("neg z "+this.center);
		}
	}
}

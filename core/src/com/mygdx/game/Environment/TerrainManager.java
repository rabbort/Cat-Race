package com.mygdx.game.Environment;

import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.World.GameManager;

public class TerrainManager extends GameManager
{	
	private static final String[] chunkLocation = 
		{ 	
			"NW", "N", "NE",
			"W", "C", "E",
			"SW", "S", "SE"
		};
	
	private int xOffsetNegative;
	private int xOffsetPositive;
	private int zOffsetNegative;
	private int zOffsetPositive;
	private Terrain[] terrainChunks;
	private GameManager base;
	private Vector3 center = new Vector3();
	private Vector3 playerPosition = new Vector3();
	private int terrainScale = 4;
	
	public TerrainManager()
	{
		
	}
	
	public TerrainManager(Vector3 position, GameManager base)
	{
		this.base = base;
		this.center = position.cpy();
		this.center.x = (int)this.center.x;
		this.center.z = (int)this.center.z;
		
		terrainChunks = new Terrain[9];
		
		// Create each terrain chunk based on the starting position
		for(int i = 0; i < terrainChunks.length; i++)
		{
			terrainChunks[i] = new Terrain(this.base, chunkLocation[i], this.center);
		}
		
		xOffsetNegative = (int)this.center.x - 64 * terrainScale;
		xOffsetPositive = (int)this.center.x + 64 * terrainScale;
		zOffsetNegative = (int)this.center.z - 64 * terrainScale;
		zOffsetPositive = (int)this.center.z + 64 * terrainScale;
	}

	
	public void setPosition(Vector3 position)
	{
		this.playerPosition = position;
	}
	
	public void update()
	{	
		// Shift terrain chunks over one space, create new ones in the empty spots
		if(this.playerPosition.x > xOffsetPositive)
		{
			// update the center
			this.center.x += 128 * terrainScale;

			terrainChunks[2] = new Terrain(this.base, chunkLocation[2], this.center);
			terrainChunks[5] = new Terrain(this.base, chunkLocation[5], this.center);
			terrainChunks[8] = new Terrain(this.base, chunkLocation[8], this.center);

			xOffsetPositive += 128 * terrainScale;
			xOffsetNegative += 128 * terrainScale;
			System.out.println("positive x "+this.center);
		}
		else if(this.playerPosition.x < xOffsetNegative)
		{
			//update the center
			this.center.x -= 128 * terrainScale;
			
			terrainChunks[0] = new Terrain(this.base, chunkLocation[0], this.center);
			terrainChunks[3] = new Terrain(this.base, chunkLocation[3], this.center);
			terrainChunks[6] = new Terrain(this.base, chunkLocation[6], this.center);

			xOffsetPositive -= 128 * terrainScale;
			xOffsetNegative -= 128 * terrainScale;
			System.out.println("neg x "+this.center);
		}
		
		if(this.playerPosition.z > zOffsetPositive)
		{
			//update the center
			this.center.z += 128 * terrainScale;

			terrainChunks[0] = new Terrain(this.base, chunkLocation[0], this.center);
			terrainChunks[1] = new Terrain(this.base, chunkLocation[1], this.center);
			terrainChunks[2] = new Terrain(this.base, chunkLocation[2], this.center);
			
			zOffsetPositive += 128 * terrainScale;
			zOffsetNegative += 128 * terrainScale;
			System.out.println("pos z "+this.center);
		}
		else if(this.playerPosition.z < zOffsetNegative)
		{
			//update the center
			this.center.z -= 128 * terrainScale;

			terrainChunks[6] = new Terrain(this.base, chunkLocation[6], this.center);
			terrainChunks[7] = new Terrain(this.base, chunkLocation[7], this.center);
			terrainChunks[8] = new Terrain(this.base, chunkLocation[8], this.center);
			
			zOffsetPositive -= 128 * terrainScale;
			zOffsetNegative -= 128 * terrainScale;
			System.out.println("neg z "+this.center);
		}
	}
}

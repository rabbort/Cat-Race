package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector3;

public class TerrainManager extends BaseBulletTest
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
		
		terrainChunks = new Terrain[9];
		
		// Create each terrain chunk based on the starting position
		for(int i = 0; i < terrainChunks.length; i++)
		{
			terrainChunks[i] = new Terrain(this.base, chunkLocation[i], position);
		}
		
		xOffsetNegative = (int)position.x - 1280;
		xOffsetPositive = (int)position.x + 1280;
		zOffsetNegative = (int)position.z - 1280;
		zOffsetPositive = (int)position.z + 1280;
	}
	
	public void update(Vector3 playerPosition)
	{
		System.out.println(playerPosition);
		//for(int i = 0; i < 25; i++)
			//terrainChunks[i].render();
		//terrainChunks[0].render();
		if(Gdx.input.isKeyJustPressed(Keys.G))
			new Terrain(this.base,chunkLocation[5], this.center);
		
		// Shift terrain chunks over one space, create new ones in the empty spots
		if(playerPosition.x > xOffsetPositive)
		{
			// update the center
			this.center.x += 2560;

			new Terrain(this.base, chunkLocation[2], this.center);
			new Terrain(this.base, chunkLocation[5], this.center);
			new Terrain(this.base, chunkLocation[8], this.center);
			
			xOffsetPositive += 2560;
			xOffsetNegative += 2560;
			System.out.println("positive x "+this.center);
		}
		else if(playerPosition.x < xOffsetNegative)
		{
			//update the center
			this.center.x -= 1280;
			
			new Terrain(this.base, chunkLocation[0], this.center);
			new Terrain(this.base, chunkLocation[3], this.center);
			new Terrain(this.base, chunkLocation[6], this.center);

			xOffsetPositive -= 1280;
			xOffsetNegative -= 1280;
			System.out.println("neg x "+this.center);
		}
		
		if(playerPosition.z > zOffsetPositive)
		{
			//update the center
			this.center.z += 2560;

			new Terrain(this.base, chunkLocation[0], this.center);
			new Terrain(this.base, chunkLocation[1], this.center);
			new Terrain(this.base, chunkLocation[2], this.center);
			
			zOffsetPositive += 2560;
			zOffsetNegative += 2560;
			System.out.println("pos z "+this.center);
		}
		else if(playerPosition.z < zOffsetNegative)
		{
			//update the center
			this.center.z -= 1280;

			new Terrain(this.base, chunkLocation[6], this.center);
			new Terrain(this.base, chunkLocation[7], this.center);
			new Terrain(this.base, chunkLocation[8], this.center);
			
			zOffsetPositive -= 1280;
			zOffsetNegative -= 1280;
			System.out.println("neg z "+this.center);
		}
	}
}

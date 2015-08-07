package com.mygdx.game.Environment;

import java.util.Random;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.World.GameManager;

public class TerrainManager extends GameManager
{	
	private Random roads;
	private int offset;
	private GameManager base;
	private Vector3 center = new Vector3();
	private Vector3 playerPosition = new Vector3();
	private int terrainScale = 5;
	private Array<Terrain> terrain;
	private int[] startPoints = new int[3];
	private int chunksToStart = 4;
	private int terrainLength = 128;
	private int terrainWidth = 128;
	
	public TerrainManager()
	{
		
	}
	
	public TerrainManager(Vector3 position, GameManager base)
	{
		terrain = new Array<>();
		this.base = base;
		this.center = position.cpy();
		this.center.x = (int)this.center.x;
		this.center.z = (int)this.center.z;
		roads = new Random((long)(position.x + position.y + position.z));
		
		startPoints = getRoads();
		startPoints[1] = 64;
		
		// Add the initial chunk. It will have walls on three sides
		terrain.add(new Terrain(this.base, this.center, false, true, startPoints));
		
		offset = (int)this.center.x + terrainLength * terrainScale;
		
		// Load in several chunks on start so the player wont actually see the new chunks being created
		for(int i = 0; i < chunksToStart; i++)
		{
			this.center.x += terrainWidth * terrainScale;
			startPoints = terrain.peek().getStart();
			terrain.add(new Terrain(this.base, this.center, false, false, startPoints));
		}
	}
	
	// Generates the starting/ending points for the roads
	private int[] getRoads()
	{
		int[] roadPoints = new int[3];
		
		roadPoints[0] = roads.nextInt(128);
		roadPoints[1] = roads.nextInt(128);
		roadPoints[2] = roads.nextInt(128);
		
		return roadPoints;
	}

	
	public void setPosition(Vector3 position)
	{
		this.playerPosition = position;
	}
	
	public void update()
	{	
		if(this.playerPosition.x > offset)
		{
			//update the center
			this.center.x += terrainWidth * terrainScale;
			
			// Feed where the road left off into the new terrain
			startPoints = terrain.peek().getStart();
			terrain.add(new Terrain(this.base, this.center, true, false, startPoints));
			
			offset += terrainWidth * terrainScale;
		}
	}
}

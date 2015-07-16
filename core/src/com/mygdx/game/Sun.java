package com.mygdx.game;

import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;

public class Sun extends BaseBulletTest
{
	MyGdxGame game;
	float rate = 0.05f;
	int cycle = 1;
	DirectionalLight light = super.light;
	
	public Sun(MyGdxGame myGame, float revolveRate)
	{
		game = myGame;
		rate = revolveRate;
		
		cycle = 1;
		light = new DirectionalLight().set(0.8f, 0.8f, 0.8f, 0.0f, -1.0f, 0.0f);
		//game.getEnvironment().add(light);
	}
	
	// This just changes the direction the light vector is pointing over time to simulate a day/night cycle
	public void updateSun()
	{
		// Sunset
		if(cycle == 1) // starting here y should be -1, x 0
		{
			light.direction.x += rate; // x approaches 1
			light.direction.y += rate; // y approaches 0
			
			if(light.direction.x >= 1.0f)
				cycle = 2;
		}
		// Night 
		else if(cycle == 2) // starting here y should be 0, x 1
		{
			light.direction.x -= rate; // x approaches 0
			light.direction.y += rate; // y approaches 1
			
			if(light.direction.x <= 0.0f)
				cycle = 3;
		}
		// Night part two
		else if(cycle == 3) // starting here y should be 1, x 0
		{
			light.direction.x -= rate; // x approaches -1
			light.direction.y -= rate; // y approaches 0
			
			if(light.direction.x <= -1.0f)
				cycle = 4;
		}
		// Sunrise
		else if(cycle == 4) // starting here y should be 0, x -1
		{
			light.direction.x += rate; // x approaches 0
			light.direction.y -= rate; // y approaches -1
			
			if(light.direction.x >= 0)
				cycle = 1;
		}
	}
}

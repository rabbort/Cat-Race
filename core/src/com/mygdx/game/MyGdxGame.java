package com.mygdx.game;

import com.badlogic.gdx.Game;

public class MyGdxGame extends Game 
{
	@Override
	public void create() 
	{
		this.setScreen(new MainMenu(this));
	}
	
	public void render()
	{
		super.render();
	}
	
	public void dispose()
	{
		
	}
}

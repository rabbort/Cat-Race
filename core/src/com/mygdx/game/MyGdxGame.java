package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.mygdx.game.Menus.MainMenu;
import com.mygdx.game.kryonet.ServerScreen;

public class MyGdxGame extends Game 
{
	private String type;
	
	public MyGdxGame(String type)
	{
		this.type = type;
	}
	
	@Override
	public void create() 
	{
		if(this.type == "server")
			this.setScreen(new ServerScreen(this));
		else
			this.setScreen(new MainMenu(this));
	}
	
	@Override
	public void render()
	{
		super.render();
	}
}

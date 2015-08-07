package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.World.GameManager;

public class Sounds 
{
	private Array<Sound> sounds = new Array<>();

	public Sounds()
	{
		// Load the sounds in
		sounds.add(Gdx.audio.newSound(Gdx.files.internal("data/vehicles/sounds/ignition.mp3")));
		sounds.add(Gdx.audio.newSound(Gdx.files.internal("data/vehicles/sounds/idle.mp3")));
		sounds.add(Gdx.audio.newSound(Gdx.files.internal("data/vehicles/sounds/driving.mp3")));
		
		// Add them to disposables
		for(int i = 0; i < sounds.size; i++)
			GameManager.inst.disposables.add(sounds.get(i));
	}
	
	public void playSound(int idx, boolean loop, float volume)
	{
		if(loop)
		{
			sounds.get(idx).loop(volume);
		}
		else
			sounds.get(idx).play(volume);
	}
	
	public void stopSound(int idx)
	{
		sounds.get(idx).stop();
	}
}

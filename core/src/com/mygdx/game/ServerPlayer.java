package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.mygdx.game.World.GameManager;

public class ServerPlayer extends AbstractPlayer
{
	private String name;
	
	public ServerPlayer(Vector3 startPosition)
	{
		super.setStart(startPosition);
		create();
	}
	
	public void create()
	{
		super.create();
	}
	
	public void update()
	{
		super.getAnimation().update(Gdx.graphics.getDeltaTime());

		if(super.getStatus() == idle)
			super.getAnimation().animate("idle", -1, 0.8f, null, 0.2f);
		else if(super.getStatus() == forward)
			super.getAnimation().animate("run", -1, 0.8f, null, 0.2f);
		else if(super.getStatus() == backward)
			super.getAnimation().animate("run", -1, -0.8f, null, 0.2f);
		else if(super.getStatus() == jumping)
			super.getAnimation().animate("jump", 1, 2.5f, null, 0.2f);
		else if(super.getStatus() == driving)
		{
			super.getAnimation().animate("carsit", -1, 0.8f, null, 0.2f);
		}
			
		super.getGhostObject().setWorldTransform(super.getCharacter().transform);
		GameManager.inst.update();
		super.getGhostObject().getWorldTransform(super.getCharacter().transform);
	}
	
	public void setCharacterTransform(Matrix4 characterTransform) 
	{
		//super.getCharacter().transform.lerp(characterTransform, 0.1f);
		super.getCharacter().transform.set(characterTransform);
	}
	
	public void setStatus(byte status)
	{
		super.setStatus(status);
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public Vector3 getPosition()
	{
		Vector3 position = new Vector3();
		
		super.getCharacter().transform.getTranslation(position);
		return position;
	}
}

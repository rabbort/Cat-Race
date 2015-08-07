package com.mygdx.game.Environment;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.World.BulletEntity;
import com.mygdx.game.World.GameManager;

public class SkyDome extends EnvironmentEntity 
{
	private BulletEntity sky;
	private String name;
	
	public SkyDome(GameManager baseBulletTest)
	{
		name = "skyDome";
		create(baseBulletTest);
	}

	public void create(GameManager base)
	{
		super.create(Gdx.files.internal("data/skybox/skyball.g3dj"), base, false, name);

		sky = base.world.add(name, 0f, 0f, 0f);
	}
	
	// Used to move the sky to stay centered on the player
	public void moveSky(Vector3 newPosition)
	{
		newPosition.y -= 300f;
		sky.modelInstance.transform.setToTranslation(newPosition);
		sky.modelInstance.transform.scale(3.5f, 3.5f, 3.5f);
	}
}

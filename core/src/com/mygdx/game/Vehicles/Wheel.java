package com.mygdx.game.Vehicles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.utils.TextureProvider;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.JsonReader;
import com.mygdx.game.World.BulletConstructor;
import com.mygdx.game.World.BulletEntity;
import com.mygdx.game.World.GameManager;

public class Wheel 
{
	private GameManager base;
	private BulletEntity wheel;
	private ModelLoader modelLoader;
	private Model wheelModel;
	private ModelData wheelData;
	private Vector3 wheelHalfExtents;
	
	public Wheel(GameManager base, Vector3 location)
	{
		this.base = base;
		
		// If there is no wheel constructor, set it up
		if(this.base.world.getConstructor("wheel") == null)
		{
			modelLoader = new G3dModelLoader(new JsonReader());
			wheelData = modelLoader.loadModelData(Gdx.files.internal("data/vehicles/wheels/policewheel.g3dj"));
			wheelModel = new Model(wheelData, new TextureProvider.FileTextureProvider());
			wheelHalfExtents = wheelModel.calculateBoundingBox(new BoundingBox()).getDimensions(new Vector3()).scl(0.5f);
			
			this.base.world.addConstructor("wheel", new BulletConstructor(wheelModel, 0, null));
			this.base.disposables.add(wheelModel);
			
			wheel = this.base.world.add("wheel", location.x, location.y, location.z);
		}
		else
		{
			wheel = this.base.world.add("wheel", location.x, location.y, location.z);
			wheelHalfExtents = wheel.modelInstance.model.calculateBoundingBox(new BoundingBox()).getDimensions(new Vector3()).scl(0.5f);
		}
	}
	
	public BulletEntity getWheel()
	{
		return wheel;
	}
	
	public Vector3 getWheelHalfExtents()
	{
		return wheelHalfExtents;
	}
}

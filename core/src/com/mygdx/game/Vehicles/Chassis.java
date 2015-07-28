package com.mygdx.game.Vehicles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.utils.TextureProvider;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.utils.JsonReader;
import com.mygdx.game.World.BulletConstructor;
import com.mygdx.game.World.BulletEntity;
import com.mygdx.game.World.GameManager;

public class Chassis 
{
	private GameManager base;
	private BulletEntity chassis;
	private Model chassisModel;
	private ModelData chassisData;
	private ModelLoader modelLoader;
	private Vector3 chassisHalfExtents;
	
	public Chassis(GameManager base, Vector3 location)
	{
		this.base = base;
		
		// If there is no wheel constructor, set it up
		if(this.base.world.getConstructor("chassis") == null)
		{
			modelLoader = new G3dModelLoader(new JsonReader());
			chassisData = modelLoader.loadModelData(Gdx.files.internal("data/vehicles/chassis/chassis.g3dj"));
			chassisModel = new Model(chassisData, new TextureProvider.FileTextureProvider());
			chassisHalfExtents = chassisModel.calculateBoundingBox(new BoundingBox()).getDimensions(new Vector3()).scl(0.5f);
			
			this.base.world.addConstructor("chassis", new BulletConstructor(chassisModel, 1000f, new btBoxShape(chassisHalfExtents)));
			this.base.disposables.add(chassisModel);
			
			chassis = this.base.world.add("chassis", location.x, location.y, location.z);
		}
		else
		{
			chassis = this.base.world.add("chassis", location.x, location.y, location.z);
			chassisHalfExtents = chassis.modelInstance.model.calculateBoundingBox(new BoundingBox()).getDimensions(new Vector3()).scl(0.5f);
		}
	}
	
	public BulletEntity getChassis()
	{
		return chassis;
	}
	
	public Vector3 getChassisHalfExtents()
	{
		return chassisHalfExtents;
	}
}

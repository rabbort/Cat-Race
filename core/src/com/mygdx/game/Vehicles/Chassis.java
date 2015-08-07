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

public abstract class Chassis 
{
	private BulletEntity chassis;
	private Model chassisModel;
	private ModelData chassisData;
	private ModelLoader modelLoader;
	private Vector3 chassisHalfExtents;
	private float mass;
	
	public Chassis(Vector3 location, String type)
	{
		mass = setMass(type);
		
		// If there is no chassis constructor, set it up
		if(GameManager.inst.world.getConstructor("chassis"+type) == null)
		{
			modelLoader = new G3dModelLoader(new JsonReader());
			chassisData = modelLoader.loadModelData(Gdx.files.internal("data/vehicles/chassis/"+type+".g3dj"));
			chassisModel = new Model(chassisData, new TextureProvider.FileTextureProvider());
			chassisHalfExtents = chassisModel.calculateBoundingBox(new BoundingBox()).getDimensions(new Vector3()).scl(0.5f);
			if(type == "bigtruck")
			{
				chassisHalfExtents.y = 1.0f;
				chassisHalfExtents.z = 9.0f;
			}
			else
			{
				chassisHalfExtents.y = 1.0f;
				chassisHalfExtents.z -= 0.6f;
			}
			GameManager.inst.world.addConstructor("chassis"+type, new BulletConstructor(chassisModel, mass, new btBoxShape(chassisHalfExtents)));
			GameManager.inst.disposables.add(chassisModel);
			
			chassis = GameManager.inst.world.add("chassis"+type, location.x, location.y, location.z);
		}
		else
		{
			chassis = GameManager.inst.world.add("chassis"+type, location.x, location.y, location.z);
			chassisHalfExtents = chassis.modelInstance.model.calculateBoundingBox(new BoundingBox()).getDimensions(new Vector3()).scl(0.5f);
			chassisHalfExtents.y = 1;
			chassisHalfExtents.z -= 0.6f;
		}
	}
	
	private float setMass(String type)
	{
		if(type == "catherham")
			return 300f;
		else if(type == "interceptor")
			return 800f;
		else if(type == "tropfenwagen")
			return 300f;
		else if(type == "truck")
			return 1500f;
		else if(type == "bigtruck")
			return 2200f;
		
		return 0f;
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

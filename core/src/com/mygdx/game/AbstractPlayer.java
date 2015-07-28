package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.TextureProvider;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShapeZ;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btConvexShape;
import com.badlogic.gdx.physics.bullet.collision.btPairCachingGhostObject;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.utils.JsonReader;
import com.mygdx.game.World.BulletConstructor;
import com.mygdx.game.World.BulletEntity;
import com.mygdx.game.World.GameManager;

public abstract class AbstractPlayer 
{
	final static byte idle = 1;
	final static byte forward = 2;
	final static byte backward = 3;
	final static byte left = 4;
	final static byte right = 5;
	final static byte jumping = 6;
	final static byte driving = 7;
	
	private byte ID;
	private BulletEntity character;
	private Matrix4 characterTransform = new Matrix4();
	private Vector3 start = new Vector3();
	private btPairCachingGhostObject ghostObject;
	private btConvexShape ghostShape;
	private AnimationController animation;
	private byte status;
	
	public void create()
	{
		// Build the player constructor if it hasn't been done yet
		if(GameManager.inst.world.getConstructor("player") == null)
		{
			ModelLoader loader = new G3dModelLoader(new JsonReader());
			ModelData data = loader.loadModelData(Gdx.files.internal("data/cat.g3dj"));
			Model cat = new Model(data, new TextureProvider.FileTextureProvider());
		
			// Create a visual representation of the character (note that we don't use the physics part of BulletEntity, we'll do that manually)
			GameManager.inst.world.addConstructor("player", new BulletConstructor(cat, new btCapsuleShape(0.05f, 0.1f)));
		}
		
		character = GameManager.inst.world.add("player", start.x, start.y, start.z);
		character.modelInstance.transform.scale(0.1f, 0.1f, 0.1f);
		characterTransform = (character.transform); // Set by reference
		
		// Create the physics representation of the character
		ghostObject = new btPairCachingGhostObject();
		ghostObject.setWorldTransform(character.transform);
		ghostShape = new btBoxShape(new Vector3(0.5f,0.5f,0.5f));
		ghostShape = new btCapsuleShapeZ(0.5f, 4f);
		ghostShape = new btSphereShape(0.5f);
		ghostObject.setCollisionShape(ghostShape);
		ghostObject.setCollisionFlags(btCollisionObject.CollisionFlags.CF_CHARACTER_OBJECT);
		
		animation = new AnimationController(character.modelInstance);
		status = 0;
	}
	
	public void setStart(Vector3 startPosition)
	{
		this.start = startPosition;
	}
	
	public BulletEntity getCharacter()
	{
		return character;
	}
	
	public AnimationController getAnimation()
	{
		return animation;
	}
	
	public btPairCachingGhostObject getGhostObject()
	{
		return ghostObject;
	}
	
	public btConvexShape getGhostShape()
	{
		return ghostShape;
	}
	
	public void dispose()
	{
		GameManager.inst.world.collisionWorld.removeCollisionObject(ghostObject);
		character.dispose(); 
		ghostObject.dispose();
		ghostShape.dispose();
	}
	
	public byte getStatus()
	{
		return status;
	}
	
	public void setStatus(byte newStatus)
	{
		status = newStatus;
	}
	
	
	public void setID(byte id)
	{
		ID = id;
	}
	
	public byte getID()
	{
		return ID;
	}
}

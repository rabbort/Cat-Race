package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.TextureProvider;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseProxy;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShapeZ;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btConvexShape;
import com.badlogic.gdx.physics.bullet.collision.btPairCachingGhostObject;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btKinematicCharacterController;
import com.badlogic.gdx.utils.JsonReader;

public class Player extends BaseBulletTest
{
	private int status;
	final static int idle = 1;
	final static int forward = 2;
	final static int backward = 3;
	final static int left = 4;
	final static int right = 5;
	final static int jumping = 6;
	
	BulletEntity ground;
	BulletEntity character;
	BulletEntity terrain;
	BulletEntity skybox;

	btPairCachingGhostObject ghostObject;
	btConvexShape ghostShape;
	btKinematicCharacterController characterController;
	Matrix4 characterTransform;
	Matrix4 ghostTransform = new Matrix4();
	Vector3 characterDirection = new Vector3();
	Vector3 characterPosition = new Vector3();
	Quaternion characterRotation = new Quaternion();
	Vector3 walkDirection = new Vector3();
	int mouseX, mouseY;
	float rotation;
	
	Quaternion q = new Quaternion();
	Vector3 p = new Vector3();
	Vector3 desired = new Vector3();
	AnimationController animation;
	
	private boolean inVehicle;
	private BaseBulletTest base;
	
	public Player(BaseBulletTest base)
	{
		this.base = base;
		createWorld();
		create();
	}
	
	public void create () 
	{		
		inVehicle = false;
		
		ModelLoader loader = new G3dModelLoader(new JsonReader());
		ModelData data = loader.loadModelData(Gdx.files.internal("data/cat.g3dj"));
		Model cat = new Model(data, new TextureProvider.FileTextureProvider());

		// Create a visual representation of the character (note that we don't use the physics part of BulletEntity, we'll do that manually)
		this.base.world.addConstructor("player", new BulletConstructor(cat, new btCapsuleShape(0.05f, 0.1f)));
		character = this.base.world.add("player", 0f, 50, 0f);
		character.modelInstance.transform.scale(0.1f, 0.1f, 0.1f);
		characterTransform = character.transform; // Set by reference
		this.base.setCharacter(characterTransform);
		
		// Create the physics representation of the character
		ghostObject = new btPairCachingGhostObject();
		ghostObject.setWorldTransform(characterTransform);
		ghostShape = new btBoxShape(new Vector3(0.5f,0.5f,0.5f));
		ghostShape = new btCapsuleShapeZ(0.5f, 4f);
		ghostShape = new btSphereShape(0.5f);
		ghostObject.setCollisionShape(ghostShape);
		ghostObject.setCollisionFlags(btCollisionObject.CollisionFlags.CF_CHARACTER_OBJECT);
		characterController = new btKinematicCharacterController(ghostObject, ghostShape, .35f);
		characterController.setJumpSpeed(15f);
		animation = new AnimationController(character.modelInstance);
		status = 0;
		
		// And add it to the physics world
		this.base.world.collisionWorld.addCollisionObject(ghostObject, 
			(short)btBroadphaseProxy.CollisionFilterGroups.CharacterFilter,
			(short)(btBroadphaseProxy.CollisionFilterGroups.StaticFilter | btBroadphaseProxy.CollisionFilterGroups.DefaultFilter));
		((btDiscreteDynamicsWorld)(this.base.world.collisionWorld)).addAction(characterController);
	}
	
	public int getStatus()
	{
		return status;
	}
	
	public void setStatis(int newStatus)
	{
		status = newStatus;
	}
	
	@Override
	public void update () 
	{
		animation.update(Gdx.graphics.getDeltaTime());
		this.base.setCharacter(characterTransform);
		
		// Find the character's position and center he skydome on it
		characterTransform.getTranslation(characterPosition);
		this.base.sky.moveSky(characterPosition);
		
		if(this.base.isAndroid())
		{
			if(this.base.getController().getDriving().getClickListener().isOver())
				occupyVehicle();
		}
		else if(Gdx.input.isKeyJustPressed(Keys.E))
		{
			occupyVehicle();
		}
		
		// Move according to android controls if on android, otherwise just use desktop controls
		if(!inVehicle)
		{
			if(this.base.isAndroid())
				androidMove();
			else
				desktopMove();
			
			walkDirection.scl(15f * Gdx.graphics.getDeltaTime());
			// And update the character controller
			characterController.setWalkDirection(walkDirection);
			// Now we can update the world as normally
			this.base.update();
			// And fetch the new transformation of the character (this will make the model be rendered correctly)
			ghostObject.getWorldTransform(characterTransform);
		}
		else
			animation.animate("carsit", -1, 0.8f, null, 0.2f);
	}
	
	private void desktopMove()
	{
		characterTransform.getRotation(characterRotation);
		desired.set(characterPosition);
		
		// Fetch which direction the character is facing now
		characterDirection.set(0,0,1).rot(characterTransform).nor();
		// Set the walking direction accordingly (either forward or backward)
		walkDirection.set(0,0,0);
		
		if (Gdx.input.isKeyPressed(Keys.W))
		{
			if(!animation.inAction)
				walkDirection.add(characterDirection);
			animation.animate("run", -1, 0.8f, null, 0.2f);
			if(status != forward)
				status = forward;
		}
		else if (Gdx.input.isKeyPressed(Keys.S))
		{
			if(!animation.inAction)
				walkDirection.add(-characterDirection.x, -characterDirection.y, -characterDirection.z);
			animation.animate("run", -1, -0.8f, null, 0.2f);
			if(status != backward)
				status = backward;
		}

		else if(status != idle)
		{
			animation.queue("idle", -1, 0.8f, null, 0.2f);
			status = idle;
		}
		
		// Strafing
		if (Gdx.input.isKeyPressed(Keys.A))
		{
			characterTransform.translate(new Vector3(15 * Gdx.graphics.getDeltaTime(), 0, 0));
			ghostObject.setWorldTransform(characterTransform);
			
			if(!animation.inAction)
				//animation.animate("Hop Left", 1, -0.5f, null, 0.2f);
			status = right;
		}
		if (Gdx.input.isKeyPressed(Keys.D)) 
		{
			characterTransform.translate(new Vector3(-15 * Gdx.graphics.getDeltaTime(), 0, 0));
			ghostObject.setWorldTransform(characterTransform);
			
			if(!animation.inAction)
				//animation.animate("Hop Right", 1, 0.5f, null, 0.2f);
			status = right;
		}
		
		if(Gdx.input.isKeyPressed(Keys.SPACE))
		{
			if(!animation.inAction)
				characterController.jump();
			animation.animate("jump", 1, 2.5f, null, 0.2f);
			status = jumping;
		}

		// Rotate player with mouse movements
		characterTransform.rotate(Vector3.Y, -0.2f * Gdx.input.getDeltaX());
		//characterTransform.rotate(Vector3.X, -0.2f * Gdx.input.getDeltaY());

		ghostObject.setWorldTransform(characterTransform);
	}
	
	private void androidMove()
	{
		//TODO android jump button
		/*if(Gdx.input.isKeyPressed(Keys.SPACE))
		{
			characterController.jump();
		}*/
		
		// Rotate according to the turn knob
		characterTransform.rotate(0, 1, 0, -2f * this.base.getController().getKnobValue("turnX"));
			
		ghostObject.setWorldTransform(characterTransform);
		
		// Fetch which direction the character is facing now
		characterDirection.set(0,0,1).rot(characterTransform).nor();
		// Set the walking direction accordingly (either forward or backward)
		walkDirection.set(0,0,0);
		
		if (this.base.getController().getKnobValue("moveY") > 0)
		{
			if(!animation.inAction)
				walkDirection.add(characterDirection);

			animation.animate("run", -1, 0.8f, null, 0.2f);
				
			if(status != forward)
				status = forward;
		}
		else if (this.base.getController().getKnobValue("moveY") < 0)
		{
			if(!animation.inAction)
				walkDirection.add(-characterDirection.x, -characterDirection.y, -characterDirection.z);

			animation.animate("run", -1, -0.8f, null, 0.2f);
			
			if(status != backward)
				status = backward;
		}
		else if(status != idle)
		{
			animation.queue("idle", -1, 0.5f, null, 0.2f);
			status = idle;
		}
		
		// Strafing
		if (this.base.getController().getKnobValue("moveX") < -0.15f)
		{
			characterTransform.translate(new Vector3(2 * Gdx.graphics.getDeltaTime(), 0, 0));
			ghostObject.setWorldTransform(characterTransform);

			status = left;
		}
		if (this.base.getController().getKnobValue("moveX") > 0.15f) 
		{
			characterTransform.translate(new Vector3(-2 * Gdx.graphics.getDeltaTime(), 0, 0));
			ghostObject.setWorldTransform(characterTransform);

			status = right;
		}
	}
	
	private void occupyVehicle()
	{
		if(inVehicle)
		{
			this.base.camera.setScale(1);
			inVehicle = false;
			this.base.vehicle.occupyVehicle(character);
		}
		else if(!inVehicle)
		{
			this.base.camera.setScale(2);
			inVehicle = true;
			this.base.vehicle.occupyVehicle(character);
		}
	}
	
	@Override
	protected void renderWorld () 
	{
		this.base.renderWorld();
	}
	
	@Override
	public void dispose () 
	{
		((btDiscreteDynamicsWorld)(this.base.world.collisionWorld)).removeAction(characterController);
		this.base.world.collisionWorld.removeCollisionObject(ghostObject);
		this.base.dispose();
		characterController.dispose();
		ghostObject.dispose();
		ghostShape.dispose();
		ground = null;
	}
}
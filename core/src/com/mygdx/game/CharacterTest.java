package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.TextureProvider;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btAxisSweep3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseProxy;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btConvexShape;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btGhostPairCallback;
import com.badlogic.gdx.physics.bullet.collision.btPairCachingGhostObject;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btKinematicCharacterController;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.utils.JsonReader;

public class CharacterTest extends BaseBulletTest 
{
	private int status;
	final static int idle = 1;
	final static int forward = 2;
	final static int backward = 3;
	final static int left = 4;
	final static int right = 5;
	final static int flying = 6;
	final static int landing = 7;
	
	private boolean inFlight;
	
	BulletEntity ground;
	BulletEntity character;
	BulletEntity terrain;
	BulletEntity skybox;
	
	btGhostPairCallback ghostPairCallback;
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
	
	@Override
	public BulletWorld createWorld () 
	{
		// We create the world using an axis sweep broadphase for this test
		btDefaultCollisionConfiguration collisionConfiguration = new btDefaultCollisionConfiguration();
		btCollisionDispatcher dispatcher = new btCollisionDispatcher(collisionConfiguration);
		btAxisSweep3 sweep = new btAxisSweep3(new Vector3(-1000, -1000, -1000), new Vector3(1000, 1000, 1000));
		btSequentialImpulseConstraintSolver solver = new btSequentialImpulseConstraintSolver();
		btDiscreteDynamicsWorld collisionWorld = new btDiscreteDynamicsWorld(dispatcher, sweep, solver, collisionConfiguration);
		ghostPairCallback = new btGhostPairCallback();
		sweep.getOverlappingPairCache().setInternalGhostPairCallback(ghostPairCallback);
		return new BulletWorld(collisionConfiguration, dispatcher, sweep, solver, collisionWorld);
	}
	
	@Override
	public void create () 
	{
		super.create();
		
		ModelLoader loader = new G3dModelLoader(new JsonReader());
		ModelData data = loader.loadModelData(Gdx.files.internal("data/butterfly/butterfly.g3dj"));
		Model player = new Model(data, new TextureProvider.FileTextureProvider());
		disposables.add(player);
		
		data = loader.loadModelData(Gdx.files.internal("data/cat.g3dj"));
		Model cat = new Model(data, new TextureProvider.FileTextureProvider());
		world.addConstructor("cat", new BulletConstructor(cat, new btCapsuleShape(1.0f, 2.0f)));
		BulletEntity hero = world.add("cat", 0, 200f, 0);
		Matrix4 heroTransform = hero.transform;
		ghostObject = new btPairCachingGhostObject();
		ghostObject.setWorldTransform(heroTransform);
		ghostShape = new btBoxShape(new Vector3(0.5f,0.2f,0.5f));
		ghostObject.setCollisionShape(ghostShape);
		ghostObject.setCollisionFlags(btCollisionObject.CollisionFlags.CF_CHARACTER_OBJECT);
		characterController = new btKinematicCharacterController(ghostObject, ghostShape, .35f);
		
		// Create a visual representation of the character (note that we don't use the physics part of BulletEntity, we'll do that manually)
		world.addConstructor("player", new BulletConstructor(cat, new btCapsuleShape(0.05f, 0.1f)));
		character = world.add("player", 5f, 200, 5f);
		characterTransform = character.transform; // Set by reference
		super.setCharacter(characterTransform);
		
		// Create the physics representation of the character
		ghostObject = new btPairCachingGhostObject();
		ghostObject.setWorldTransform(characterTransform);
		ghostShape = new btBoxShape(new Vector3(0.5f,0.2f,0.5f));
		ghostObject.setCollisionShape(ghostShape);
		ghostObject.setCollisionFlags(btCollisionObject.CollisionFlags.CF_CHARACTER_OBJECT);
		characterController = new btKinematicCharacterController(ghostObject, ghostShape, .35f);
		animation = new AnimationController(character.modelInstance);
		status = 0;
		inFlight = false;
		
		// And add it to the physics world
		world.collisionWorld.addCollisionObject(ghostObject, 
			(short)btBroadphaseProxy.CollisionFilterGroups.CharacterFilter,
			(short)(btBroadphaseProxy.CollisionFilterGroups.StaticFilter | btBroadphaseProxy.CollisionFilterGroups.DefaultFilter));
		((btDiscreteDynamicsWorld)(world.collisionWorld)).addAction(characterController);
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
		super.setCharacter(characterTransform);
		
		// Find the character's position and center he skydome on it
		characterTransform.getTranslation(characterPosition);
		super.sky.moveSky(characterPosition);
		
		// Move according to android controls if on android, otherwise just use desktop controls
		if(super.isAndroid())
			androidMove();
		else
			desktopMove();
		
		walkDirection.scl(inFlight ? 10f * Gdx.graphics.getDeltaTime(): 5f * Gdx.graphics.getDeltaTime());
		// And update the character controller
		characterController.setWalkDirection(walkDirection);
		// Now we can update the world as normally
		super.update();
		// And fetch the new transformation of the character (this will make the model be rendered correctly)
		ghostObject.getWorldTransform(characterTransform);
	}
	
	private void desktopMove()
	{
		characterTransform.getRotation(characterRotation);
		desired.set(characterPosition);
		
		// Toggle flight mode
		if(Gdx.input.isKeyJustPressed(Keys.F))
		{
			if(status == flying)
			{
				status = landing;
				characterController.setGravity(9.8f);
				ghostObject.setWorldTransform(characterTransform);
			}
			else
				status = flying;
		}
		
		// Fetch which direction the character is facing now
		characterDirection.set(0,0,1).rot(characterTransform).nor();
		// Set the walking direction accordingly (either forward or backward)
		walkDirection.set(0,0,0);
		if(status == flying)
		{
			characterController.setGravity(0);
			if(!animation.inAction)
			{
				if(!inFlight)
				//	animation.animate("Flight Take Off", 1, 0.5f, null, 0.2f);
				//animation.queue("Flight Loop", -1, 0.5f, null, 0.2f);
				inFlight = true;
			}
		}
		if (Gdx.input.isKeyPressed(Keys.W))
		{
			if(!animation.inAction)
				walkDirection.add(characterDirection);
			if(status != flying)
			{
				//animation.animate("Hop Forward", -1, 0.3f, null, 0.2f);
			}
			if(status != forward && status != flying)
				status = forward;
		}
		else if (Gdx.input.isKeyPressed(Keys.S))
		{
			if(!animation.inAction)
				walkDirection.add(-characterDirection.x, -characterDirection.y, -characterDirection.z);
			if(status != flying)
				//animation.animate("Hop Backward", -1, 0.3f, null, 0.2f);
			if(status != backward && status != flying)
				status = backward;
		}

		else if(status != idle && status != flying)
		{
			if(inFlight)
			{
				if(!animation.inAction)
				//	animation.animate("Flight Land", 1, 0.5f, null, 0.2f);
				inFlight = false;
			}
			animation.queue("Idle", -1, 0.5f, null, 0.2f);
			status = idle;
		}
		
		// Strafing
		if (Gdx.input.isKeyPressed(Keys.A))
		{
			characterTransform.translate(new Vector3(2 * Gdx.graphics.getDeltaTime(), 0, 0));
			ghostObject.setWorldTransform(characterTransform);
			
			if(!animation.inAction && status != flying)
				//animation.animate("Hop Left", 1, -0.5f, null, 0.2f);
			status = right;
		}
		if (Gdx.input.isKeyPressed(Keys.D)) 
		{
			characterTransform.translate(new Vector3(-2 * Gdx.graphics.getDeltaTime(), 0, 0));
			ghostObject.setWorldTransform(characterTransform);
			
			if(!animation.inAction && status != flying)
				//animation.animate("Hop Right", 1, 0.5f, null, 0.2f);
			status = right;
		}
		
		if(Gdx.input.isKeyPressed(Keys.SPACE))
		{
			if(!animation.inAction)
				characterController.jump();
			//animation.animate("Hop", 1, 0.5f, null, 0.2f);
		}

		// Rotate player with mouse movements
		// Player can control roll/pitch while flying, can only turn while on ground
		if(inFlight)
		{
			characterTransform.rotate(0, 0, 1, 0.2f * Gdx.input.getDeltaX());
			characterTransform.rotate(1, 0, 0, -0.2f * Gdx.input.getDeltaY());
		}
		else
		{
			characterTransform.rotate(Vector3.Y, -0.2f * Gdx.input.getDeltaX());
			characterTransform.rotate(Vector3.X, -0.2f * Gdx.input.getDeltaY());
			//if(Math.abs(Gdx.input.getY()) < 450)
			{
				//characterTransform.rotate(Vector3.X, -0.2f * Gdx.input.getDeltaY()).lerp
					//(characterTransform.rotate(Vector3.Y, -0.2f * Gdx.input.getDeltaX()), 1.0f);
				//characterTransform.setToRotation(Vector3.Y, -0.2f * Gdx.input.getX()).mul(characterTransform).
					//lerp(characterTransform.rotate(Vector3.X, -0.2f * Gdx.input.getY()), 0.1f);
				//characterTransform.tra();
				//characterTransform.setFromEulerAngles(-0.2f * Gdx.input.getX(), -0.2f * Gdx.input.getY(), 0).trn(characterPosition.x, 0,0);
				//characerTransform.trn(-0.2f * Gdx.input.getDeltaY(), -0.2f * Gdx.input.getDeltaX(), 0);
				
	
			}
			//characterTransform.setToRotation(Vector3.X, 0.2f * Gdx.input.getY());
		}

		ghostObject.setWorldTransform(characterTransform);
	}
	
	private void androidMove()
	{
		//TODO android jump button
		/*if(Gdx.input.isKeyPressed(Keys.SPACE))
		{
			characterController.jump();
		}*/
		
		if(super.getController().getFlying())
			status = flying;
		else
		{
			status = landing;
			characterController.setGravity(9.8f);
			ghostObject.setWorldTransform(characterTransform);
		}
		
		if(status == flying)
		{
			characterController.setGravity(0);
			if(!animation.inAction)
			{
				if(!inFlight)
					animation.animate("Flight Take Off", 1, 0.5f, null, 0.2f);
				animation.queue("Flight Loop", -1, 0.5f, null, 0.2f);
				inFlight = true;
			}
		}
		
		// Rotate according to the turn knob
		// Player can control roll/pitch while flying, can only turn while on ground
		if(inFlight)
		{
			characterTransform.rotate(0, 0, 1, 2f * super.getController().getKnobValue("turnX"));
			characterTransform.rotate(1, 0, 0, 2f * super.getController().getKnobValue("turnY"));
		}
		else
			characterTransform.rotate(0, 1, 0, -2f * super.getController().getKnobValue("turnX"));
			
		ghostObject.setWorldTransform(characterTransform);
		
		// Fetch which direction the character is facing now
		characterDirection.set(0,0,1).rot(characterTransform).nor();
		// Set the walking direction accordingly (either forward or backward)
		walkDirection.set(0,0,0);
		
		if (super.getController().getKnobValue("moveY") > 0)
		{
			if(!animation.inAction)
				walkDirection.add(characterDirection);
			if(status != flying)
			{
				animation.animate("Hop Forward", -1, 0.3f, null, 0.2f);
			}
			if(status != forward && status != flying)
				status = forward;
		}
		else if (super.getController().getKnobValue("moveY") < 0)
		{
			if(!animation.inAction)
				walkDirection.add(-characterDirection.x, -characterDirection.y, -characterDirection.z);
			if(status != flying)
				animation.animate("Hop Backward", -1, 0.3f, null, 0.2f);
			if(status != backward && status != flying)
				status = backward;
		}
		else if(status != idle && status != flying)
		{
			if(inFlight)
			{
				if(!animation.inAction)
					animation.animate("Flight Land", 1, 0.5f, null, 0.2f);
				inFlight = false;
			}
			animation.queue("Idle Feed", -1, 0.5f, null, 0.2f);
			status = idle;
		}
		
		// Strafing
		if (super.getController().getKnobValue("moveX") < -0.15f)
		{
			characterTransform.translate(new Vector3(2 * Gdx.graphics.getDeltaTime(), 0, 0));
			ghostObject.setWorldTransform(characterTransform);
			
			if(!animation.inAction && status != flying)
				animation.animate("Hop Left", 1, -0.5f, null, 0.2f);
			status = right;
		}
		if (super.getController().getKnobValue("moveX") > 0.15f) 
		{
			characterTransform.translate(new Vector3(-2 * Gdx.graphics.getDeltaTime(), 0, 0));
			ghostObject.setWorldTransform(characterTransform);
			
			if(!animation.inAction && status != flying)
				animation.animate("Hop Right", 1, 0.5f, null, 0.2f);
			status = right;
		}
	}
	
	@Override
	protected void renderWorld () 
	{
		super.renderWorld();
	}
	
	@Override
	public void dispose () 
	{
		((btDiscreteDynamicsWorld)(world.collisionWorld)).removeAction(characterController);
		world.collisionWorld.removeCollisionObject(ghostObject);
		super.dispose();
		characterController.dispose();
		ghostObject.dispose();
		ghostShape.dispose();
		ghostPairCallback.dispose();
		ground = null;
	}
}
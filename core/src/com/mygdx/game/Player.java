package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseProxy;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btKinematicCharacterController;
import com.mygdx.game.World.GameManager;

public class Player extends AbstractPlayer
{
	private btKinematicCharacterController characterController;
	private Vector3 characterDirection = new Vector3();
	private Vector3 characterPosition = new Vector3();
	private Quaternion characterRotation = new Quaternion();
	private Vector3 walkDirection = new Vector3();
	private Vector3 desired = new Vector3();
	
	public Player(Vector3 startPosition)
	{
		super.setStart(startPosition);
		create();
	}

	public void create () 
	{		
		super.create();

		GameManager.inst.setCharacter(getCharacterTransform());
		characterController = new btKinematicCharacterController(super.getGhostObject(), super.getGhostShape(), .35f);
		characterController.setJumpSpeed(15f);
		
		// Add to the physics world
		GameManager.inst.world.collisionWorld.addCollisionObject(super.getGhostObject(), 
			(short)btBroadphaseProxy.CollisionFilterGroups.CharacterFilter,
			(short)(btBroadphaseProxy.CollisionFilterGroups.StaticFilter | btBroadphaseProxy.CollisionFilterGroups.DefaultFilter));
		((btDiscreteDynamicsWorld)(GameManager.inst.world.collisionWorld)).addAction(characterController);
	}
	
	private void sendTransform()
	{
		// Send the transform to the server
		GameManager.inst.updatePlayerTransform(super.getCharacter().transform, super.getID(), super.getStatus());
	}
	
	public void update () 
	{
		super.getAnimation().update(Gdx.graphics.getDeltaTime());

		GameManager.inst.setCharacter(getCharacterTransform());
	
		// Find the character's position and center he skydome on it
		getCharacterTransform().getTranslation(characterPosition);
		GameManager.inst.sky.moveSky(characterPosition);

		// Handles entering/exiting vehicle
		if(GameManager.inst.isAndroid())
		{
			if(GameManager.inst.getController().getDriving().getClickListener().isOver())
				occupyVehicle();
		}
		else if(Gdx.input.isKeyJustPressed(Keys.E))
		{
			occupyVehicle();
		}
	
		// Move according to android controls if on android, otherwise just use desktop controls
		if(super.getStatus() != AbstractPlayer.driving)
		{
			if(GameManager.inst.isAndroid())
				androidMove();
			else
				desktopMove();
			
			walkDirection.scl(15f * Gdx.graphics.getDeltaTime());
			// And update the character controller
			characterController.setWalkDirection(walkDirection);
			// Now we can update the world as normally
			GameManager.inst.update();
			// And fetch the new transformation of the character (this will make the model be rendered correctly)
			super.getGhostObject().getWorldTransform(getCharacterTransform());
		}
		else
			super.getAnimation().animate("carsit", -1, 0.8f, null, 0.2f);
		
		sendTransform();
	}
	
	private void desktopMove()
	{
		getCharacterTransform().getRotation(characterRotation);
		desired.set(characterPosition);
		
		// Fetch which direction the character is facing now
		characterDirection.set(0,0,1).rot(getCharacterTransform()).nor();
		// Set the walking direction accordingly (either forward or backward)
		walkDirection.set(0,0,0);
		
		if (Gdx.input.isKeyPressed(Keys.W))
		{
			if(!super.getAnimation().inAction)
				walkDirection.add(characterDirection);
			super.getAnimation().animate("run", -1, 0.8f, null, 0.2f);
			if(super.getStatus() != AbstractPlayer.forward)
				super.setStatus(AbstractPlayer.forward);
		}
		else if (Gdx.input.isKeyPressed(Keys.S))
		{
			if(!super.getAnimation().inAction)
				walkDirection.add(-characterDirection.x, -characterDirection.y, -characterDirection.z);
			super.getAnimation().animate("run", -1, -0.8f, null, 0.2f);
			if(super.getStatus() != AbstractPlayer.backward)
				super.setStatus(AbstractPlayer.backward);
		}

		else if(super.getStatus() != AbstractPlayer.idle)
		{
			super.getAnimation().queue("idle", -1, 0.8f, null, 0.2f);
			super.setStatus(AbstractPlayer.idle);
		}
		
		// Strafing
		if (Gdx.input.isKeyPressed(Keys.A))
		{
			getCharacterTransform().translate(new Vector3(15 * Gdx.graphics.getDeltaTime(), 0, 0));
			super.getGhostObject().setWorldTransform(getCharacterTransform());
			
			if(!super.getAnimation().inAction)
				//super.getAnimation().animate("Hop Left", 1, -0.5f, null, 0.2f);
			super.setStatus(AbstractPlayer.left);
		}
		if (Gdx.input.isKeyPressed(Keys.D)) 
		{
			getCharacterTransform().translate(new Vector3(-15 * Gdx.graphics.getDeltaTime(), 0, 0));
			super.getGhostObject().setWorldTransform(getCharacterTransform());
			
			if(!super.getAnimation().inAction)
				//super.getAnimation().animate("Hop Right", 1, 0.5f, null, 0.2f);
			super.setStatus(AbstractPlayer.right);
		}
		
		if(Gdx.input.isKeyPressed(Keys.SPACE))
		{
			if(!super.getAnimation().inAction)
				characterController.jump();
			super.getAnimation().animate("jump", 1, 2.5f, null, 0.2f);
			super.setStatus(AbstractPlayer.jumping);
		}

		// Rotate player with mouse movements
		getCharacterTransform().rotate(Vector3.Y, -0.2f * Gdx.input.getDeltaX());
		//characterTransform.rotate(Vector3.X, -0.2f * Gdx.input.getDeltaY());

		super.getGhostObject().setWorldTransform(getCharacterTransform());
	}
	
	private void androidMove()
	{
		//TODO android jump button
		/*if(Gdx.input.isKeyPressed(Keys.SPACE))
		{
			characterController.jump();
		}*/
		
		// Rotate according to the turn knob
		getCharacterTransform().rotate(0, 1, 0, -2f * GameManager.inst.getController().getKnobValue("turnX"));
			
		super.getGhostObject().setWorldTransform(getCharacterTransform());
		
		// Fetch which direction the character is facing now
		characterDirection.set(0,0,1).rot(getCharacterTransform()).nor();
		// Set the walking direction accordingly (either forward or backward)
		walkDirection.set(0,0,0);
		
		if (GameManager.inst.getController().getKnobValue("moveY") > 0)
		{
			if(!super.getAnimation().inAction)
				walkDirection.add(characterDirection);

			super.getAnimation().animate("run", -1, 0.8f, null, 0.2f);
				
			if(super.getStatus() != AbstractPlayer.forward)
				super.setStatus(AbstractPlayer.forward);
		}
		else if (GameManager.inst.getController().getKnobValue("moveY") < 0)
		{
			if(!super.getAnimation().inAction)
				walkDirection.add(-characterDirection.x, -characterDirection.y, -characterDirection.z);

			super.getAnimation().animate("run", -1, -0.8f, null, 0.2f);
			
			if(super.getStatus() != AbstractPlayer.backward)
				super.setStatus(AbstractPlayer.backward);
		}
		else if(super.getStatus() != AbstractPlayer.idle)
		{
			super.getAnimation().queue("idle", -1, 0.5f, null, 0.2f);
			super.setStatus(AbstractPlayer.idle);
		}
		
		// Strafing
		if (GameManager.inst.getController().getKnobValue("moveX") < -0.15f)
		{
			getCharacterTransform().translate(new Vector3(2 * Gdx.graphics.getDeltaTime(), 0, 0));
			super.getGhostObject().setWorldTransform(getCharacterTransform());

			super.setStatus(AbstractPlayer.left);
		}
		if (GameManager.inst.getController().getKnobValue("moveX") > 0.15f) 
		{
			getCharacterTransform().translate(new Vector3(-2 * Gdx.graphics.getDeltaTime(), 0, 0));
			super.getGhostObject().setWorldTransform(getCharacterTransform());

			super.setStatus(AbstractPlayer.right);
		}
	}
	
	private void occupyVehicle()
	{
		if(super.getStatus() == AbstractPlayer.driving)
		{
			GameManager.inst.camera.setScale(1);
			super.setStatus(AbstractPlayer.idle);
			GameManager.inst.vehicle.occupyVehicle(super.getCharacter());
		}
		else if(super.getStatus() != AbstractPlayer.driving)
		{
			GameManager.inst.camera.setScale(2);
			super.setStatus(AbstractPlayer.driving);
			GameManager.inst.vehicle.occupyVehicle(super.getCharacter());
		}
	}
	
	public void dispose () 
	{
		((btDiscreteDynamicsWorld)(GameManager.inst.world.collisionWorld)).removeAction(characterController);
		characterController.dispose();
		
		super.dispose();
	}

	public Matrix4 getCharacterTransform() 
	{
		return super.getCharacter().transform;
	}
}
package com.mygdx.game.Vehicles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.dynamics.btDefaultVehicleRaycaster;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRaycastVehicle;
import com.badlogic.gdx.physics.bullet.dynamics.btRaycastVehicle.btVehicleTuning;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btVehicleRaycaster;
import com.badlogic.gdx.physics.bullet.dynamics.btWheelInfo;
import com.mygdx.game.World.BulletEntity;
import com.mygdx.game.World.GameManager;

public class Vehicle 
{
	final static byte idle = 1;
	final static byte driving = 2;
	
	private btVehicleRaycaster raycaster;
	private btRaycastVehicle vehicle;
	private btVehicleTuning tuning;
	
	private Chassis chassis;
	private Wheel wheels[] = new Wheel[4];
	private btWheelInfo wheelInfo[] = new btWheelInfo[4];
	
	private Vector3 tmp = new Vector3();
	private Vector3 direction;
	private Vector3 axis;
	
	private float maxForce;
	private float currentForce;
	private float acceleration;
	private float maxAngle;
	private float currentAngle;
	private float steerSpeed;
	private float timer;
	private float netTimer;
	
	private int ID;
	private boolean hasDriver;
	
	private boolean occupied;
	
	private BulletEntity driver = null;
	private BulletEntity passenger = null;
	private Matrix4 driverPosition = new Matrix4();
	private Matrix4 passengerPosition = new Matrix4();
	private String type;
	private byte status;
	
	public Vehicle(Vector3 location, String type)
	{
		this.type = type;
		
		if(type == "interceptor")
			chassis = new Interceptor(location);
		else if(type == "truck")
			chassis = new Truck(location);
		else if(type == "tropfenwagen")
			chassis = new Tropfenwagen(location);
		else if(type == "catherham")
			chassis = new Catherham(location);
		else if(type == "bigtruck")
			chassis = new BigTruck(location);
		
		for(int i = 0; i < wheels.length; i++)
		{
			wheels[i] = new Wheel(GameManager.inst, location, type);
		}
		
		raycaster = new btDefaultVehicleRaycaster((btDynamicsWorld)GameManager.inst.world.collisionWorld);
		setTuning(type);
		vehicle = new btRaycastVehicle(tuning, (btRigidBody)chassis.getChassis().body, raycaster);
		
		chassis.getChassis().body.setActivationState(Collision.DISABLE_DEACTIVATION);
		((btDynamicsWorld)GameManager.inst.world.collisionWorld).addVehicle(vehicle);
		
		vehicle.setCoordinateSystem(0, 1, 2);
		
		direction = new Vector3(0, -1, 0);
		axis = new Vector3(-1, 0, 0);
		
		setWheelInfo(type);
		
		for(int i = 0; i < wheelInfo.length; i++)
		{
			// Set roll influence low so vehicle doesn't flip easily, set some brake value so vehicle stops when idle
			wheelInfo[i].setRollInfluence(0.0f);
			//wheelInfo[i].setBrake(10f);
		}
		
		// Add any disposables
		GameManager.inst.disposables.add(vehicle);
		GameManager.inst.disposables.add(raycaster);
		GameManager.inst.disposables.add(tuning);
		
		// Starts off empty
		hasDriver = false;
	}
	
	private void setWheelInfo(String type)
	{
		float scaleXL = 0;
		float scaleXR = 0;
		float scaleY = 0;
		float scaleZF = 0;
		float scaleZB = 0;
		
		if(type == "catherham")
		{
			scaleXL = 0.9f;
			scaleXR = 0.65f;
			scaleY = 0.2f;
			scaleZF = 0.81f;
			scaleZB = 0.77f;
		}
		else if(type == "interceptor")
		{
			scaleXL = 0.8f;
			scaleXR = 0.8f;
			scaleY = 0.8f;
			scaleZF = 0.7f;
			scaleZB = 0.6f;
		}
		else if(type == "tropfenwagen")
		{
			scaleXL = 1.0f;
			scaleXR = 1.0f;
			scaleY = 0.3f;
			scaleZF = 0.63f;
			scaleZB = 0.51f;
		}
		else if(type == "truck")
		{
			scaleXL = 0.5f;
			scaleXR = 0.9f;
			scaleY = 0.3f;
			scaleZF = 0.9f;
			scaleZB = 0.4f;
		}
		else if(type == "bigtruck")
		{
			scaleXL = 1.1f;
			scaleXR = 0.6f;
			scaleY = -1.5f;
			scaleZF = 0.6f;
			scaleZB = 0.51f;
		}
			
		wheelInfo[0] = vehicle.addWheel(tmp.set(chassis.getChassisHalfExtents()).scl(scaleXL, -scaleY, scaleZF), direction, 
				axis, wheels[0].getWheelHalfExtents().z * 0.3f, wheels[0].getWheelHalfExtents().z, tuning, true);
		wheelInfo[1] = vehicle.addWheel(tmp.set(chassis.getChassisHalfExtents()).scl(-scaleXR, -scaleY, scaleZF), direction, 
				axis, wheels[1].getWheelHalfExtents().z * 0.3f, wheels[1].getWheelHalfExtents().z, tuning, true);
		wheelInfo[2] = vehicle.addWheel(tmp.set(chassis.getChassisHalfExtents()).scl(scaleXL, -scaleY, -scaleZB), direction, 
				axis, wheels[2].getWheelHalfExtents().z * 0.3f, wheels[2].getWheelHalfExtents().z, tuning, false);
		wheelInfo[3] = vehicle.addWheel(tmp.set(chassis.getChassisHalfExtents()).scl(-scaleXR, -scaleY, -scaleZB), direction, 
				axis, wheels[3].getWheelHalfExtents().z * 0.3f, wheels[3].getWheelHalfExtents().z, tuning, false);
		
		for(int i = 0; i < wheelInfo.length; i++)
		{
			wheelInfo[i].setBrake(10f);
		}
	}
	
	private void updateDriver(String type)
	{
		float positionX = 0;
		float positionY = 0;
		float positionZ = 0;
		
		if(type == "catherham")
		{
			positionX = -0.6f;
			positionY = -0.5f;
			positionZ = -2f;
		}
		else if(type == "interceptor")
		{
			positionX = 1.2f;
			positionY = -1f;
			positionZ = 0.15f;
		}
		else if(type == "tropfenwagen")
		{
			positionX = 0f;
			positionY = 0f;
			positionZ = 1.0f;
		}
		else if(type == "truck")
		{
			positionX = 0f;
			positionY = 0f;
			positionZ = 0f;
		}
		else if(type == "bigtruck")
		{
			positionX = 3.2f;
			positionY = 4.8f;
			positionZ = 3f;
		}
		
		// Put driver in the driver seat
		driverPosition = vehicle.getChassisWorldTransform();
		driverPosition.translate(positionX, positionY, positionZ);
		driver.transform.set(driverPosition);
		driver.modelInstance.transform.scale(0.1f, 0.1f, 0.1f);
	}
	
	private void setTuning(String type)
	{
		tuning = new btVehicleTuning();
		tuning.setMaxSuspensionTravelCm(20f);
		tuning.setFrictionSlip(10f);
		tuning.setSuspensionCompression(40f);
		tuning.setSuspensionDamping(50f);
		tuning.setSuspensionStiffness(40f);
		tuning.setMaxSuspensionForce(6000f);
		
		if(type == "catherham")
		{
			maxForce = 3000f;
			acceleration = 1500f;
			maxAngle = 55f;
			steerSpeed = 25f;
		}
		else if(type == "interceptor")
		{
			maxForce = 3500f;
			acceleration = 2000f;
			maxAngle = 35f;
			steerSpeed = 15f;
		}
		else if(type == "truck")
		{
			maxForce = 5000f;
			acceleration = 1000f;
			maxAngle = 35f;
			steerSpeed = 20f;
		}
		else if(type == "bigtruck")
		{
			maxForce = 7000f;
			acceleration = 1500f;
			maxAngle = 25f;
			steerSpeed = 10f;
		}
		
		currentForce = 0f;
		currentAngle = 0f;
	}
	
	public void update()
	{
		netTimer -= Gdx.graphics.getDeltaTime();
		
		for (int i = 0; i < wheels.length; i++) 
		{
			vehicle.updateWheelTransform(i, false);
			vehicle.getWheelInfo(i).getWorldTransform().getOpenGLMatrix(wheels[i].getWheel().transform.val);
		}
		
		if(occupied && driver != null)
		{
			moveCar();
			if(netTimer < 0)
			{
				GameManager.inst.updateVehicleTransform(vehicle.getChassisWorldTransform(), ID, hasDriver);
				netTimer = 0.01f;
			}
		}
		else if(passenger != null)
			movePassenger();
	}
	
	public void updateVehicle(Matrix4 transform)
	{
		if(!occupied || passenger != null)
		{
			vehicle.getRigidBody().setWorldTransform(transform);
		}
	}
	
	private void movePassenger()
	{
		// Put passenger in the passenger seat
		passengerPosition = vehicle.getChassisWorldTransform();
		passengerPosition.translate(-1.2f, -1f, 0.15f);
		passenger.transform.set(passengerPosition);
		passenger.modelInstance.transform.scale(0.1f, 0.1f, 0.1f);
	}
	
	public void flip()
	{
		if(occupied && timer < 0)
		{
			Vector3 position = new Vector3();
			Matrix4 upright = new Matrix4();
			
			vehicle.getRigidBody().getWorldTransform().getTranslation(position);
			upright.set(position.x, position.y + 10, position.z, 0, 0, 0, 0);
			vehicle.getRigidBody().setWorldTransform(upright);
			
			timer = 15.0f;
		}
	}
	
	private void moveCar()
	{
		timer -= Gdx.graphics.getDeltaTime();
		netTimer -= Gdx.graphics.getDeltaTime();
		
		updateDriver(this.type);
		
		// Flips the vehicle back to the upright position (should be used when car is flipped over)
		// A timer is used so that this feature can't be spammed
		if(Gdx.input.isKeyJustPressed(Keys.R))
		{
			flip();
		}
		
		final float delta = Gdx.graphics.getDeltaTime();
		float angle = currentAngle;
		if((GameManager.inst.getController() != null ? GameManager.inst.getController().getKnobValue("turnX") > 0.05 :
			Gdx.input.isKeyPressed(Keys.D)) || Gdx.input.isKeyPressed(Keys.D))
		{
			if (angle > 0f) angle = 0f;
			angle = MathUtils.clamp(angle - steerSpeed * delta, -maxAngle, 0f);
		} 
		else if((GameManager.inst.getController() != null ? GameManager.inst.getController().getKnobValue("turnX") < -0.05 :
			Gdx.input.isKeyPressed(Keys.A)) || Gdx.input.isKeyPressed(Keys.A))
		{
			if (angle < 0f) angle = 0f;
			angle = MathUtils.clamp(angle + steerSpeed * delta, 0f, maxAngle);
		} 
		else
			angle = 0f;
		
		if (angle != currentAngle) 
		{
			currentAngle = angle;
			vehicle.setSteeringValue(angle * MathUtils.degreesToRadians, 0);
			vehicle.setSteeringValue(angle * MathUtils.degreesToRadians, 1);
		}

		float force = currentForce;
		
		if((GameManager.inst.getController() != null ? GameManager.inst.getController().getKnobValue("moveY") > 0 :
			Gdx.input.isKeyPressed(Keys.W)) || Gdx.input.isKeyPressed(Keys.W))
		{
			if (force < 0f) 
				force = 0f;
			force = MathUtils.clamp(force + acceleration * delta, 0f, maxForce);
			
			if(status != driving)
			{
				status = driving;
				GameManager.inst.getSounds().playSound(2, true, 2.5f);
				GameManager.inst.getSounds().stopSound(1);
			}
		} 
		else if((GameManager.inst.getController() != null ? GameManager.inst.getController().getKnobValue("moveY") < 0 :
			Gdx.input.isKeyPressed(Keys.S)) || Gdx.input.isKeyPressed(Keys.S))
		{
			if (force > 0f) 
				force = 0f;
			force = MathUtils.clamp(force - acceleration * delta, -maxForce, 0f);
			
			if(status != driving)
			{
				status = driving;
				GameManager.inst.getSounds().playSound(2, true, 2.5f);
				GameManager.inst.getSounds().stopSound(1);
			}
		} 
		else
		{
			force = 0f;
			if(status != idle)
			{
				status = idle;
				GameManager.inst.getSounds().playSound(1, true, 0.05f);
				GameManager.inst.getSounds().stopSound(2);
			}
		}
		if (force != currentForce) 
		{
			currentForce = force;
			vehicle.applyEngineForce(force, 0);
			vehicle.applyEngineForce(force, 1);
		}
	}
	
	// Used to enter or exit vehicle
	public void occupyVehicle(BulletEntity character)
	{
		if(occupied)
		{
			occupied = false;
			if(driver != null)
			{
				driver = null;
				hasDriver = false;
				// Send one update to show the car has no driver now
				GameManager.inst.updateVehicleTransform(vehicle.getChassisWorldTransform(), ID, hasDriver);
				
				// Make sure sounds stop when exiting
				GameManager.inst.getSounds().stopSound(1);
				GameManager.inst.getSounds().stopSound(2);
			}
			else if(passenger != null)
				passenger = null;
		}
		else if(!occupied)
		{
			occupied = true;
			if(!hasDriver)
			{
				driver = character;
				hasDriver = true;
				GameManager.inst.getSounds().playSound(0, false, 1.0f);
			}
			else
				passenger = character;
		}
	}
	
	public void setID(int id)
	{
		this.ID = id;
	}
	
	public int getID()
	{
		return this.ID;
	}
	
	// Returns the current position of the vehicle for purposes of comparing to the player position
	public Vector3 getPosition()
	{
		Vector3 position = new Vector3();
		vehicle.getChassisWorldTransform().getTranslation(position);
		
		return position;
	}
	
	public void setDriver(boolean hasDriver)
	{
		this.hasDriver = hasDriver;
	}
	
	public btRaycastVehicle getVehicle()
	{
		return vehicle;
	}
	
	public BulletEntity getDriver()
	{
		return driver;
	}
	
	public BulletEntity getChassis()
	{
		return chassis.getChassis();
	}
	
	public boolean hasDriver()
	{
		return hasDriver;
	}
}

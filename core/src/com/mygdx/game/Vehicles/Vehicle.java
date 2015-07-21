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
import com.mygdx.game.BaseBulletTest;
import com.mygdx.game.BulletEntity;

public class Vehicle 
{
	private BaseBulletTest base;
	
	private btVehicleRaycaster raycaster;
	private btRaycastVehicle vehicle;
	private btVehicleTuning tuning;
	
	private Chassis chassis;
	private Wheel wheels[] = new Wheel[4];
	
	private Vector3 tmp = new Vector3();
	private Vector3 direction;
	private Vector3 axis;
	
	private float maxForce;
	private float currentForce;
	private float acceleration;
	private float maxAngle;
	private float currentAngle;
	private float steerSpeed;
	
	private boolean occupied;
	
	private BulletEntity driver;
	private Matrix4 driverPosition = new Matrix4();
	
	public Vehicle(BaseBulletTest base, Vector3 location)
	{
		this.base = base;
		
		chassis = new Chassis(this.base, location);
		for(int i = 0; i < wheels.length; i++)
		{
			wheels[i] = new Wheel(this.base, location);
		}
		
		raycaster = new btDefaultVehicleRaycaster((btDynamicsWorld)this.base.world.collisionWorld);
		tuning = new btVehicleTuning();
		vehicle = new btRaycastVehicle(tuning, (btRigidBody)chassis.getChassis().body, raycaster);
		
		chassis.getChassis().body.setActivationState(Collision.DISABLE_DEACTIVATION);
		((btDynamicsWorld)this.base.world.collisionWorld).addVehicle(vehicle);
		
		vehicle.setCoordinateSystem(0, 1, 2);
		
		direction = new Vector3(0, -1, 0);
		axis = new Vector3(-1, 0, 0);
		
		vehicle.addWheel(tmp.set(chassis.getChassisHalfExtents()).scl(0.8f, -0.8f, 0.65f), direction, 
				axis, wheels[0].getWheelHalfExtents().z * 0.3f, wheels[0].getWheelHalfExtents().z, tuning, true);
		vehicle.addWheel(tmp.set(chassis.getChassisHalfExtents()).scl(-0.8f, -0.8f, 0.65f), direction, 
				axis, wheels[1].getWheelHalfExtents().z * 0.3f, wheels[1].getWheelHalfExtents().z, tuning, true);
		vehicle.addWheel(tmp.set(chassis.getChassisHalfExtents()).scl(0.8f, -0.8f, -0.55f), direction, 
				axis, wheels[2].getWheelHalfExtents().z * 0.3f, wheels[2].getWheelHalfExtents().z, tuning, false);
		vehicle.addWheel(tmp.set(chassis.getChassisHalfExtents()).scl(-0.8f, -0.8f, -0.55f), direction, 
				axis, wheels[3].getWheelHalfExtents().z * 0.3f, wheels[3].getWheelHalfExtents().z, tuning, false);
		
		maxForce = 300f;
		currentForce = 0f;
		acceleration = 50f;
		maxAngle = 25f;
		currentAngle = 0f;
		steerSpeed = 5f;
		
		this.base.disposables.add(vehicle);
		this.base.disposables.add(raycaster);
		this.base.disposables.add(tuning);
	}
	
	public void update()
	{
		for (int i = 0; i < wheels.length; i++) 
		{
			vehicle.updateWheelTransform(i, true);
			vehicle.getWheelInfo(i).getWorldTransform().getOpenGLMatrix(wheels[i].getWheel().transform.val);
		}
		
		if(occupied)
			moveCar();
	}
	
	private void moveCar()
	{
		// Put driver in the seat
		driverPosition = vehicle.getChassisWorldTransform();
		driverPosition.translate(1, -1, 0);
		driver.transform.set(driverPosition);
		driver.modelInstance.transform.scale(0.1f, 0.1f, 0.1f);
		
		final float delta = Gdx.graphics.getDeltaTime();
		float angle = currentAngle;
		if (this.base.getController() != null ? this.base.getController().getKnobValue("moveX") > 15 : Gdx.input.isKeyPressed(Keys.D))//(Gdx.input.isKeyPressed(Keys.D) || this.base.getController().getKnobValue("turnX") > 0) 
		{
			if (angle > 0f) angle = 0f;
			angle = MathUtils.clamp(angle - steerSpeed * delta, -maxAngle, 0f);
		} 
		else if(this.base.getController() != null ? this.base.getController().getKnobValue("moveX") < -15 : Gdx.input.isKeyPressed(Keys.A))//(Gdx.input.isKeyPressed(Keys.A) || this.base.getController().getKnobValue("turnX") < 0) 
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
		
		if(this.base.getController() != null ? this.base.getController().getKnobValue("moveY") > 0 : Gdx.input.isKeyPressed(Keys.W))//(Gdx.input.isKeyPressed(Keys.W) || this.base.getController().getKnobValue("moveY") > 0) 
		{
			if (force < 0f) 
				force = 0f;
			force = MathUtils.clamp(force + acceleration * delta, 0f, maxForce);
		} 
		else if(this.base.getController() != null ? this.base.getController().getKnobValue("moveY") < 0 : Gdx.input.isKeyPressed(Keys.S)) //(Gdx.input.isKeyPressed(Keys.S) || this.base.getController().getKnobValue("moveY") < 0) 
		{
			if (force > 0f) 
				force = 0f;
			force = MathUtils.clamp(force - acceleration * delta, -maxForce, 0f);
		} 
		else
			force = 0f;
		if (force != currentForce) 
		{
			currentForce = force;
			vehicle.applyEngineForce(force, 0);
			vehicle.applyEngineForce(force, 1);
		}
	}
	
	public void occupyVehicle(BulletEntity character)
	{
		if(occupied)
		{
			occupied = false;
			driver = null;
		}
		else if(!occupied)
		{
			occupied = true;
			driver = character;
		}
	}
}

package com.mygdx.game.Vehicles;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.World.BulletEntity;

public class VehicleManager 
{
	private Vehicle vehicles[];
	private Vector3 spawnPosition = new Vector3(30, 50, 100);
	private int clientidx;
	
	public VehicleManager(int numVehicles)
	{
		Matrix4 start = new Matrix4();
		
		vehicles = new Vehicle[numVehicles];
		
		for(int i = 0; i < numVehicles; i++)
		{
			if(i % 3 == 0)
				vehicles[i] = new Vehicle(spawnPosition, "truck");
			/*else if(i % 3 == 1)
				vehicles[i] = new Vehicle(spawnPosition, "bigtruck");*/
			else
				vehicles[i] = new Vehicle(spawnPosition, "interceptor");
			/*else if(i == 2)
				vehicles[i] = new Vehicle(spawnPosition, "tropfenwagen");*/
			/*else
				vehicles[i] = new Vehicle(spawnPosition, "catherham");*/
			vehicles[i].setID(i);
			
			start.set(spawnPosition.x, spawnPosition.y, spawnPosition.z, 0, 0.4f, 0, 1);
			vehicles[i].getVehicle().getRigidBody().setWorldTransform(start);
			
			spawnPosition.z += 25;
		}
	}
	
	public void updateVehicles()
	{
		for(int i = 0; i < vehicles.length; i++)
			vehicles[i].update();
	}
	
	public void updateTransform(Matrix4 transform, int id, boolean hasDriver)
	{
		vehicles[id].updateVehicle(transform);
		vehicles[id].setDriver(hasDriver);
	}
	
	public boolean occupy(BulletEntity character)
	{
		Vector3 position = new Vector3();
		character.transform.getTranslation(position);
		
		for(int i = 0; i < vehicles.length; i++)
		{
			if(position.dst(vehicles[i].getPosition()) < 10.0f && !vehicles[i].hasDriver() || vehicles[i].getDriver() != null && vehicles[i].getDriver().equals(character))
			{
				vehicles[i].occupyVehicle(character);
				clientidx = i;
				return true;
			}
		}
		
		return false;
	}
	
	public void leaveVehicle(BulletEntity character)
	{
		
	}
	
	public Vehicle getVehicle(int index)
	{
		return vehicles[index];
	}
	
	public Vehicle getClientVehicle()
	{
		return vehicles[clientidx];
	}
}

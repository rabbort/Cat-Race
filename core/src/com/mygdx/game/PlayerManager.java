package com.mygdx.game;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.World.GameManager;


// This class handles the players who are not the client ( for multiplayer )
public class PlayerManager 
{
	public static PlayerManager playerManager;
	
	private Array<ServerPlayer> players;
	
	public Array<ServerPlayer> getPlayers()
	{
		return players;
	}
	
	public PlayerManager()
	{
		players = new Array<ServerPlayer>();
		playerManager = this;
	}
	
	public void addPlayer(ServerPlayer player, int id)
	{
		players.add(player);
		players.get(players.size - 1).setID(id);
	}
	
	public void removePlayer(int id)
	{
		for(int i = 0; i < players.size; i++)
		{
			if(players.get(i).getID() == id)
			{
				GameManager.inst.world.remove(players.get(i).getCharacter());
				players.get(i).dispose();
				players.removeIndex(i);
			}
		}
	}
	
	public void updatePlayers(int id, String name, Matrix4 transform, byte status)
	{
		// Skip the update if the player is the client
		if(GameManager.inst.getClientPlayer().getID() == id)
			return;

		// Find the right player and set their transform and animation status
		for(int i = 0; i < players.size; i++)
		{
			if(players.get(i).getID() == id)
			{
				players.get(i).setCharacterTransform(transform);
				players.get(i).setStatus(status);
				players.get(i).setName(name);
				return;
			}
		}

		// If player isn't found, it needs to be added
		Vector3 position = new Vector3();
		transform.getTranslation(position);
		addPlayer(new ServerPlayer(position), id);
	}
	
	public void sortPlayers()
	{
		for(int i = 0; i < players.size; i++)
		{
			for(int j = 0; j < players.size - j - 1; j++)
			{
				if(players.get(j).getPosition().z > players.get(j + 1).getPosition().z)
					players.swap(j, j + 1);
			}
		}
	}
}

package com.mygdx.game.kryonet;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import com.mygdx.game.PlayerManager;
import com.mygdx.game.Menus.MainMenu;
import com.mygdx.game.World.GameManager;

public class GameClient 
{
	private Client client;
	private int timeout = 10000;
	String name;

	public GameClient()
	{
		client = new Client();
		client.addListener(makeListener());
		Network.register(client.getKryo());
		client.start();
		connect();
	}
	
	public void connect()
	{
		try
		{
			client.connect(timeout, Network.host, Network.tcp, Network.udp);
		}
		catch(IOException e)
		{
			throw new GdxRuntimeException(e);
		}
	}
	
	public void login(String username, String password)
	{
		Log.debug("Connected to server: "+Network.host);
		
		Network.Login login = new Network.Login();
		login.name = username;
		login.password = password;
		
		client.sendTCP(login);
	}
	
	public void vote()
	{
		Network.Vote vote = new Network.Vote();
		
		vote.voted = true;
		client.sendTCP(vote);
	}
	
	public void register(String username, String password)
	{
		Network.Register register = new Network.Register();
		
		register.name = username;
		register.password = password;
		
		client.sendTCP(register);
	}
	
	// Informs the server of any player movement
	public void updateCharacter(int id, String name, Matrix4 transform, byte status)
	{
		Network.UpdateCharacter update = new Network.UpdateCharacter();
		
		update.transform = transform;
		update.id = id;
		update.name = name;
		update.status = status;
		
		client.sendUDP(update);
	}
	
	public void updateVehicle(Matrix4 chassis, int id, boolean hasDriver)
	{
		Network.UpdateVehicle update = new Network.UpdateVehicle();
		
		update.transform = chassis;
		update.id = id;
		update.hasDriver = hasDriver;
		
		client.sendUDP(update);
	}
	
	private Listener makeListener()
	{
		return new Listener()
		{
			public void received(Connection connection, Object object)
			{
				if(object instanceof Network.StartPosition)
				{
					final Network.StartPosition start = (Network.StartPosition) object;

					Gdx.app.postRunnable(new Runnable() 
					{
						@Override
						public void run()
						{
							GameManager.inst.Spawn(start.start);
						}
					});
				}
				else if(object instanceof Network.UpdateCharacter)
				{
					final Network.UpdateCharacter update = (Network.UpdateCharacter)object;
					
					Gdx.app.postRunnable(new Runnable() 
					{
						@Override
						public void run()
						{
							if(PlayerManager.playerManager != null)
								PlayerManager.playerManager.updatePlayers(update.id, update.name, update.transform, update.status);
						}
					});
				}
				else if(object instanceof Network.Character)
				{
					final Network.Character player = (Network.Character)object;
					
					MainMenu.menu.loggedIn(player.valid, player);
				}
				else if(object instanceof Network.RemoveCharacter)
				{
					final Network.RemoveCharacter removeCharacter = (Network.RemoveCharacter)object;
					
					Gdx.app.postRunnable(new Runnable()
					{
						@Override
						public void run()
						{
							PlayerManager.playerManager.removePlayer(removeCharacter.id);
						}
					});
				}
				else if(object instanceof Network.ClientID)
				{
					final Network.ClientID ID = (Network.ClientID)object;
					
					MainMenu.menu.setID(ID.id);
				}
				else if(object instanceof Network.Registered)
				{
					MainMenu.menu.registered(((Network.Registered)object).registered);
				}
				else if(object instanceof Network.UpdateVehicle)
				{
					if(GameManager.inst != null && GameManager.inst.vehicleManager != null)
						GameManager.inst.vehicleManager.updateTransform(((Network.UpdateVehicle)object).transform, ((Network.UpdateVehicle)object).id,
								((Network.UpdateVehicle)object).hasDriver);
				}
				else if(object instanceof Network.Vote)
				{
					Gdx.app.postRunnable(new Runnable()
					{
						@Override
						public void run()
						{
							if(GameManager.inst != null)
								GameManager.inst.restart();
						}
					});
				}
				else if(object instanceof Network.VotesNeeded)
				{
					if(GameManager.inst != null)
						GameManager.inst.updateVotes(((Network.VotesNeeded)object).votes);
					System.out.println(((Network.VotesNeeded)object).votes);
				}
			}
		};
	}
}

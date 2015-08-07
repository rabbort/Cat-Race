package com.mygdx.game.kryonet;

import java.io.IOException;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Array.ArrayIterator;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import com.mygdx.game.Vehicles.Vehicle;
import com.mygdx.game.kryonet.Network.Vote;

public class GameServer 
{
	Server server;
	private Array<Connection> clients = new Array<>();
	private ObjectMap<Connection, Network.Character> clientMap;
	private ObjectMap<Connection, Network.Vote> voteMap;
	private Array<Integer> playerIDs = new Array<>();
	private Array<Vehicle> vehicles = new Array<>();
	
	Database db;
	
	
	public GameServer()
	{
		clientMap = new ObjectMap<>();
		voteMap = new ObjectMap<>();

		server = new Server(Network.writeBufferSize, Network.objectBufferSize);
		Network.register(server.getKryo());
		
		try
		{
			server.bind(Network.tcp, Network.udp);
			server.start();
			server.addListener(makeListener());
			Log.debug("Server is listening at: "+Network.host+" on port "+Network.tcp);
		}
		catch(IOException e)
		{
			Log.error(e.toString());
			throw new GdxRuntimeException(e);
		}

		db = new Database();
	}
	
	public Connection getClient(int index)
	{
		return clients.get(index);
	}
	
	public String getName(int index)
	{
		return clientMap.get(clients.get(index)).name;
	}
	
	public int getNumClients()
	{
		return clients.size;
	}
	
	private void clientConnect(Connection connection)
	{
		Log.debug("Connected to server: "+connection);
		
		Network.ClientID ID = new Network.ClientID();
		ID.id = connection.getID();
		
		connection.sendTCP(ID);
	}
	
	private void clientDisconnect(Connection connection)
	{
		Log.debug("Disconnected from server: "+connection);
		Network.RemoveCharacter removeCharacter = new Network.RemoveCharacter();
		if(clientMap.get(connection) != null)
		{
			removeCharacter.id = clientMap.get(connection).id;
			clients.removeValue(connection, true);
		
			server.sendToAllTCP(removeCharacter);
		}
		if(voteMap.get(connection) != null)
			voteMap.remove(connection);
	}
	
	private void handleReceived(Connection connection, Object object)
	{
		if(object instanceof Network.Login)
		{
			Network.Character player = new Network.Character();
			player.id = (byte)connection.getID();
			player.name = ((Network.Login)object).name;
			player.transform = db.getStartPosition(player.name);
			player.valid = db.validateUser(((Network.Login)object).name, ((Network.Login)object).password);

			connection.sendTCP(player);
			
			if(clientMap.get(connection) == null)
			{
				clientMap.put(connection, player);
				clients.add(connection);
			}
			if(voteMap.get(connection) == null)
				voteMap.put(connection, new Network.Vote());
		}
		else if(object instanceof Network.RemoveCharacter)
		{
			clientDisconnect(connection);
		}
		else if(object instanceof Network.UpdateCharacter)
		{
			server.sendToAllUDP((Network.UpdateCharacter)object);
		}
		else if(object instanceof Network.Register)
		{
			Network.Registered registered = new Network.Registered();
			registered.registered = db.createUser(((Network.Register)object).name, ((Network.Register)object).password);
			
			connection.sendTCP(registered);
		}
		else if(object instanceof Network.UpdateVehicle)
		{
			server.sendToAllUDP((Network.UpdateVehicle)object);
		}
		else if(object instanceof Network.Vote)
		{
			int numVotes = 0;
			
			voteMap.get(connection).voted = true;
			
			ObjectMap.Entries<Connection, Network.Vote> iterator = voteMap.iterator();
			
			while(iterator.hasNext)
			{
				if(iterator.next().value.voted == true)
					numVotes++;
			}
			
			if(numVotes == voteMap.size)
			{
				iterator.reset();
				while(iterator.hasNext)
				{
					iterator.next().value.voted = false;
				}
				
				server.sendToAllTCP((Network.Vote)object);
			}
			else
			{
				Network.VotesNeeded votes = new Network.VotesNeeded();
				votes.votes = numVotes;
				server.sendToAllTCP(votes);
			}
		}
	}
	
	private Listener makeListener()
	{
		return new Listener()
		{
			public void connected(Connection connection)
			{
				clientConnect(connection);
			}
			
			public void disconnected(Connection connection)
			{
				clientDisconnect(connection);
			}
			
			public void received(Connection connection, Object object)
			{
				handleReceived(connection, object);
			}
		};
	}
}

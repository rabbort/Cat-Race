package com.mygdx.game.kryonet;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;

// This class is a convenient place to keep things common to both the client and server.
public class Network 
{
	static public final int tcp = 53535;
	static public final int udp = 53536;
	public static String host = "72.238.13.232";
	public static int writeBufferSize = 256000;
	public static int objectBufferSize = 128000;

	// This registers objects that are going to be sent over the network.
	static public void register (Kryo kryo) 
	{
		kryo.register(Login.class);
		kryo.register(UpdateCharacter.class);
		kryo.register(RemoveCharacter.class);
		kryo.register(Character.class);
		kryo.register(ClientID.class);
		kryo.register(StartPosition.class);
		kryo.register(Register.class);
		kryo.register(Registered.class);
		kryo.register(com.badlogic.gdx.math.Vector3.class);
		kryo.register(com.badlogic.gdx.math.Matrix4.class);
		kryo.register(float[].class);
	}
	
	static public class StartPosition
	{
		public Vector3 start;
	}

	static public class Login
	{
		public String name;
		public String password;
	}
	
	static public class Register
	{
		public String name;
		public String password;
	}
	
	static public class Registered
	{
		public boolean registered;
	}
	
	static public class Character
	{
		public boolean valid;
		public String name;
		public byte id;
		public Vector3 transform;
	}
	
	static public class ClientID
	{
		public byte id;
	}

	static public class UpdateCharacter 
	{
		public byte id;
		public byte status;
		public Matrix4 transform;
	}

	static public class RemoveCharacter 
	{
		public byte id;
	}
}
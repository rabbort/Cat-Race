package com.mygdx.game.kryonet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.badlogic.gdx.math.Vector3;

public class Database 
{
	private String connectionUrl = "jdbc:sqlserver://localhost:1433;" +
			"databaseName=Catworld;integratedSecurity=true;";
	private Connection sqlConnection = null;
	private Statement stmt = null;
	private ResultSet rs = null;
	
	private int users = 1;
	private int passwords = 2;
	private int x = 3;
	private int y = 4;
	private int z = 5;
	
	public Database()
	{
		try{
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			sqlConnection = DriverManager.getConnection(connectionUrl);
			
			String SQL = "SELECT * FROM Users";
			stmt = sqlConnection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			rs = stmt.executeQuery(SQL);
			
			while(rs.next()){
				System.out.println(rs.getString(1) + " " + rs.getString(2));
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean createUser(String name, String password)
	{
		// Add a new user to the database
		// return false if user already exists, true otherwise
		try 
		{
			rs.absolute(0);
			while(rs.next())
			{
				if(rs.getString(users).equals(name))
					return false;
			}
			
			rs.moveToInsertRow();
			rs.updateString(users, name);
			rs.updateString(passwords, password);
			rs.updateString(x, "0");
			rs.updateString(y, "30");
			rs.updateString(z, "0");
			rs.insertRow();
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		return true;
	}
	
	public Vector3 getStartPosition(String name)
	{
		// Default to this if something goes wrong
		Vector3 start = new Vector3(0, 50, 0);
		
		try
		{
			rs.absolute(0);
			
			while(rs.next())
			{
				if(rs.getString(users).equals(name))
				{
					break;
				}
			}
			
			start.x = Integer.parseInt(rs.getString(x)); 
			start.y = Integer.parseInt(rs.getString(y));
			start.z = Integer.parseInt(rs.getString(z));
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		
		return start;
	}
	
	public boolean validateUser(String name, String password)
	{
		// Search for user in the database, return true if the password matches
		try 
		{
			rs.absolute(0);
			
			while(rs.next())
			{
				if(rs.getString(users).equals(name))
					if(rs.getString(passwords).equals(password))
						return true;
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}

		// If password doesn't match or user doesn't exist return false
		return false;
	}
}

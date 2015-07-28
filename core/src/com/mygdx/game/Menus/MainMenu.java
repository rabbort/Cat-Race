package com.mygdx.game.Menus;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.World.GameManager;
import com.mygdx.game.kryonet.GameClient;
import com.mygdx.game.kryonet.Network;
import com.mygdx.game.kryonet.Network.Login;

public class MainMenu implements Screen
{
	public static MainMenu menu;
	private final MyGdxGame game;
	
	private OrthographicCamera camera;
	
	private Button register;
	private Button login;
	private Button join;
	private Stage stage;
	private Skin skin;
	private TextField name;
	private TextField username;
	private TextField pass;
	private TextField password;
	private TextField error;
	
	private Network network;
	private GameClient client;
	private byte clientID;
	private Network.Character player;
	
	public MainMenu(final MyGdxGame game)
	{
		this.game = game;
		menu = this;
		
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		network = new Network();
		client = new GameClient();
	}
	
	@Override
	public void show()
	{
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);
		
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		
		username = new TextField("", skin);
		username.setPosition(Gdx.graphics.getWidth() / 2 - username.getWidth() / 2, Gdx.graphics.getHeight() / 2);
		name = new TextField("Name:", skin);
		name.setPosition(Gdx.graphics.getWidth() / 2 - name.getWidth() - 10, Gdx.graphics.getHeight() / 2);
		name.setDisabled(true);

		pass = new TextField("Password:", skin);
		pass.setPosition(Gdx.graphics.getWidth() / 2 - pass.getWidth() - 10, Gdx.graphics.getHeight() / 2 - pass.getHeight());
		pass.setDisabled(true);
		password = new TextField("", skin);
		password.setPosition(Gdx.graphics.getWidth() / 2 - password.getWidth() / 2, Gdx.graphics.getHeight() / 2 - password.getHeight());
		password.setPasswordMode(true);
		password.setPasswordCharacter('*');
		
		error = new TextField("", skin);
		error.setWidth(error.getWidth() + 15);
		error.setPosition(Gdx.graphics.getWidth() / 2 - error.getWidth() / 2, Gdx.graphics.getHeight() / 2 - 3 * error.getHeight());
		error.setDisabled(true);
		
		register = new TextButton("Register", skin);
		register.setPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2 - 2 * register.getHeight());
		register.addListener(new ChangeListener()
		{
			public void changed(ChangeEvent event, Actor actor)
			{
				client.register(username.getText(), password.getText());
			}
		});
		
		login = new TextButton("Log in", skin);
		login.setWidth(register.getWidth());
		login.setPosition(Gdx.graphics.getWidth() / 2 - login.getWidth(), Gdx.graphics.getHeight() / 2 - 2 * login.getHeight());
		login.addListener(new ChangeListener() 
		{
			public void changed(ChangeEvent event, Actor actor) 
			{
				client.login(username.getText(), password.getText());
			}
		});
		
		join = new TextButton("Join server", skin);
		join.setPosition(Gdx.graphics.getWidth() / 2 - join.getWidth(), Gdx.graphics.getHeight() / 2 - 2 * join.getHeight());
		join.addListener(new ChangeListener()
		{
			public void changed(ChangeEvent event, Actor actor)
			{
				Gdx.input.setInputProcessor(null);
				dispose();
				game.setScreen(new GameManager(game, player));
				login = null;
				camera = null;
			}
		});

		stage.addActor(name);
		stage.addActor(username);
		stage.addActor(pass);
		stage.addActor(password);
		stage.addActor(login);
		stage.addActor(register);
	}
	
	public void loggedIn(boolean success, Network.Character character)
	{
		if(success)
		{
			login.remove();
			stage.addActor(join);
			username.setDisabled(true);
			password.setDisabled(true);
			
			player = character;
		}
		else
		{
			stage.addActor(error);
			
			error.setText("Login failed, try again");
			error.setWidth(Gdx.graphics.getWidth() / 6);
		}
	}
	
	public void registered(boolean registered)
	{
		stage.addActor(error);
		
		if(registered)
			error.setText("Registration successful");
		else
			error.setText("Name taken, select another");
		
		error.setWidth(Gdx.graphics.getWidth() / 4.8f);
	}

	@Override
	public void render(float delta) 
	{
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
		
		camera.update();
		
		stage.act(delta);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) 
	{
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void pause() 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void dispose()
	{
		stage.dispose();
		game.dispose();
	}
	
	public void setID(byte id)
	{
		clientID = id;
	}
}

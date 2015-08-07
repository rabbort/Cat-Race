package com.mygdx.game.Menus;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
	private int clientID;
	private Network.Character player;
	
	private Texture logo;
	private Texture title;
	private SpriteBatch spriteBatch;
	
	private boolean android;
	
	public MainMenu(final MyGdxGame game)
	{
		if(Gdx.app.getType() == ApplicationType.Android)
			android = true;
		
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
		logo = new Texture(Gdx.files.internal("data/icon.png"));
		title = new Texture(Gdx.files.internal("data/title.png"));
		spriteBatch = new SpriteBatch();
		
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);
		
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		if(android)
			skin.getFont("default-font").getData().setScale(2);
		
		username = new TextField("", skin);
		username.setWidth(username.getWidth() + 50);
		username.setPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 3);
		name = new TextField("Name:", skin);
		name.setWidth(name.getWidth() + 50);
		name.setPosition(Gdx.graphics.getWidth() / 2 - name.getWidth(), Gdx.graphics.getHeight() / 3);
		name.setDisabled(true);

		pass = new TextField("Password:", skin);
		pass.setWidth(pass.getWidth() + 50);
		pass.setPosition(Gdx.graphics.getWidth() / 2 - pass.getWidth(), Gdx.graphics.getHeight() / 3 - pass.getHeight());
		pass.setDisabled(true);
		password = new TextField("", skin);
		password.setWidth(password.getWidth() + 50);
		password.setPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 3 - password.getHeight());
		password.setPasswordMode(true);
		password.setPasswordCharacter('*');
		
		error = new TextField("", skin);
		error.setWidth(password.getWidth() * 3);
		error.setPosition(Gdx.graphics.getWidth() / 2 - error.getWidth() / 2, Gdx.graphics.getHeight() / 3 - 3 * error.getHeight());
		error.setDisabled(true);
		
		register = new TextButton("Register", skin);
		register.setWidth(password.getWidth());
		register.setPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 3 - 2 * register.getHeight());
		register.addListener(new ChangeListener()
		{
			public void changed(ChangeEvent event, Actor actor)
			{
				client.register(username.getText(), password.getText());
			}
		});
		
		login = new TextButton("Log in", skin);
		login.setWidth(pass.getWidth());
		login.setWidth(register.getWidth());
		login.setPosition(Gdx.graphics.getWidth() / 2 - login.getWidth(), Gdx.graphics.getHeight() / 3 - 2 * login.getHeight());
		login.addListener(new ChangeListener() 
		{
			public void changed(ChangeEvent event, Actor actor) 
			{
				client.login(username.getText(), password.getText());
			}
		});
		
		join = new TextButton("Join server", skin);
		join.setWidth(pass.getWidth());
		join.setPosition(Gdx.graphics.getWidth() / 2 - join.getWidth(), Gdx.graphics.getHeight() / 3 - 2 * join.getHeight());
		join.addListener(new ChangeListener()
		{
			public void changed(ChangeEvent event, Actor actor)
			{
				Gdx.input.setInputProcessor(null);
				dispose();
				game.setScreen(new GameManager(game, player, clientID, client));
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
		}
	}
	
	public void registered(boolean registered)
	{
		stage.addActor(error);
		
		if(registered)
			error.setText("Registration successful");
		else
			error.setText("Name taken, select another");
	}

	@Override
	public void render(float delta) 
	{
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
		
		camera.update();
		
		stage.act(delta);
		stage.draw();
		
		spriteBatch.begin();
		spriteBatch.draw(logo, Gdx.graphics.getWidth() / 2 - logo.getWidth() / 2, Gdx.graphics.getHeight() / 1.65f);
		spriteBatch.draw(title, Gdx.graphics.getWidth() / 2 - title.getWidth() / 2, Gdx.graphics.getHeight() / 2 - title.getHeight() / 2);
		spriteBatch.end();
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
	
	public void setID(int id)
	{
		clientID = id;
	}
}

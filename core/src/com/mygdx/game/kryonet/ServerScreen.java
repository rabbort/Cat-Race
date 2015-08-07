package com.mygdx.game.kryonet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;
import com.mygdx.game.MyGdxGame;

public class ServerScreen implements Screen
{
	private MyGdxGame game;
	private OrthographicCamera camera;

	private Stage stage;
	private GameServer server;
	
	private TextArea textField;
	private TextField ip;
	private Skin skin;
	
	public ServerScreen(MyGdxGame game)
	{
		this.game = game;
		
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		server = new GameServer();
	}
	
	@Override
	public void show() 
	{
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);
		
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		textField = new TextArea("", skin);
		textField.setWidth(Gdx.graphics.getWidth() / 3);
		textField.setHeight(Gdx.graphics.getHeight());
		textField.setPosition(Gdx.graphics.getWidth(), Gdx.graphics.getHeight() / 2, Align.right);
		textField.setDisabled(true);
		
		ip = new TextField("", skin);
		ip.setPosition(0, Gdx.graphics.getHeight());
		ip.setDisabled(true);
		
		stage.addActor(textField);
	}

	@Override
	public void render(float delta) 
	{
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
		
		camera.update();
		
		//
		//textField.setMessageText("Number of players: "+server.clients.size);
		textField.setText("Number of players: "+server.getNumClients());
		if(server.getNumClients() > 0)
		{
			textField.appendText("\nID  Name");
			
			for(int i = 0; i < server.getNumClients(); i++)
				textField.appendText("\n"+server.getClient(i).getID()+"   "+server.getName(i));
		}

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
}

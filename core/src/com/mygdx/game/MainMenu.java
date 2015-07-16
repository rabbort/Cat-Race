package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class MainMenu extends MyGdxGame implements Screen
{
	private Button button;
	private TextButtonStyle buttonStyle;
	private Stage stage;
	private MyGdxGame game;
	
	public MainMenu(MyGdxGame myGame)
	{
		game = myGame;
	}
	
	@Override
	public void show()
	{
		stage = new Stage();
		Gdx.input.setInputProcessor(stage);
		
		buttonStyle = new TextButtonStyle();
		buttonStyle.font = new BitmapFont();

		button = new TextButton("Play", buttonStyle);
		button.setPosition(Gdx.graphics.getWidth() - button.getWidth(), Gdx.graphics.getHeight() / 2);
		button.addListener(new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				System.out.println("button pressed");
			}
		});
		
		stage.addActor(button);
	}

	@Override
	public void render(float delta) 
	{
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);
		
		super.render();
		
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
	}
}
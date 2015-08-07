package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad.TouchpadStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.mygdx.game.World.GameManager;

public class AndroidController //extends Player
{
	Stage stage;
	Touchpad move;
	Touchpad turn;
	float deadzone;
	Skin skin;
	Skin buttonSkin;
	Drawable background, knob;
	TouchpadStyle style;
	OrthographicCamera camera;
	int knobWidth;
	int backgroundWidth;
	int padding;
	
	TextButton enterVehicle;
	TextButtonStyle enterVehicleStyle;
	
	TextButton vote;
	TextButton flip;
	
	private boolean pressed;
	
	//@Override
	public void dispose()
	{
		stage.dispose();
	}

	//@Override
	//public void create() {
	public AndroidController()
	{	
		stage = new Stage();
		backgroundWidth = Gdx.graphics.getWidth() / 8;
		knobWidth = backgroundWidth / 3;
		padding = 15;
		
		deadzone = 5f;
		skin = new Skin();
		skin.add("background", new Texture(Gdx.files.internal("data/touchBackground.png")));
		skin.add("knob", new Texture(Gdx.files.internal("data/touchKnob.png")));
		
		buttonSkin = new Skin(Gdx.files.internal("data/uiskin.json"));
		
		background = skin.getDrawable("background");
		knob = skin.getDrawable("knob");
		
		style = new TouchpadStyle();
		style.background = background;
		style.knob = knob;
		style.knob.setMinHeight(knobWidth);
		style.knob.setMinWidth(knobWidth);
		
		move = new Touchpad(deadzone, style);
		turn = new Touchpad(deadzone, style);
		move.setBounds(padding, padding, backgroundWidth, backgroundWidth);
		turn.setBounds(Gdx.graphics.getWidth() - (padding + backgroundWidth),
				padding, backgroundWidth, backgroundWidth);	
		
		enterVehicleStyle = new TextButtonStyle();
		enterVehicleStyle.font = new BitmapFont();
		enterVehicleStyle.fontColor = Color.RED;
		enterVehicleStyle.checkedFontColor = Color.GREEN;

		buttonSkin.getFont("default-font").getData().setScale(2);
		enterVehicle = new TextButton("Enter Vehicle", buttonSkin);
		enterVehicle.setWidth(Gdx.graphics.getWidth() / 5);
		enterVehicle.setHeight(Gdx.graphics.getHeight() / 16);
		enterVehicle.setPosition(Gdx.graphics.getWidth() - enterVehicle.getWidth(), Gdx.graphics.getHeight() / 2);
		enterVehicle.addListener(makeChangeListener());
		
		vote = new TextButton("Vote to Restart", buttonSkin);
		vote.setWidth(Gdx.graphics.getWidth() / 5);
		vote.setHeight(Gdx.graphics.getHeight() / 16);
		vote.setPosition(Gdx.graphics.getWidth() - vote.getWidth(), Gdx.graphics.getHeight() / 2 + vote.getHeight());
		vote.addListener(makeVoteListener());
		
		flip = new TextButton("Flip Car", buttonSkin);
		flip.setWidth(Gdx.graphics.getWidth() / 5);
		flip.setHeight(Gdx.graphics.getHeight() / 16);
		flip.setPosition(Gdx.graphics.getWidth() - flip.getWidth(), Gdx.graphics.getHeight() / 2 - flip.getHeight());
		flip.addListener(makeFlipListener());

		stage.addActor(enterVehicle);
		stage.addActor(move);
		stage.addActor(turn);
		stage.addActor(vote);
		stage.addActor(flip);
		
		Gdx.input.setInputProcessor(stage);
	}
	
	public Button getDriving()
	{
		return enterVehicle;
	}
	
	public float getKnobValue(String knob)
	{
		if(knob == "moveX")
			return move.getKnobPercentX();
		if(knob == "moveY")
			return move.getKnobPercentY();
		if(knob == "turnX")
			return turn.getKnobPercentX();
		if(knob == "turnY")
			return turn.getKnobPercentY();
		
		return 0;
	}

	//@Override
	//public void render() 
	public void update()
	{
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}
	
	private ClickListener makeListener()
	{
		ClickListener listener = new ClickListener() 
		{
			public void clicked(InputEvent event, float x, float y)
			{
				pressed = !pressed;
			}
			
			/*public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
			{
				System.out.println(enterVehicle.getText());
				if(enterVehicle.getText().equals("Enter Vehicle"))
					System.out.println("match");
					//enterVehicle.setText("Exit Vehicle");
				else
					enterVehicle.setText("Enter Vehicle");
				
				pressed = true;
				
				return true;
			}*/
			
			public void touchUp(InputEvent event, float x, float y, int pointer, int button)
			{
				pressed = !pressed;
			}
		};
		
		listener.setTapCountInterval(1.0f);
		
		return listener;
	}
	
	private ChangeListener makeChangeListener()
	{
		ChangeListener listener = new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor) 
			{
				GameManager.inst.getClientPlayer().occupyVehicle();
			}
		};
		
		return listener;
	}
	
	private ChangeListener makeVoteListener()
	{
		ChangeListener listener = new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor) 
			{
				GameManager.inst.getClient().vote();
			}
		};
		
		return listener;
	}
	
	private ChangeListener makeFlipListener()
	{
		ChangeListener listener = new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor) 
			{
				GameManager.inst.vehicleManager.getClientVehicle().flip();
			}
		};
		
		return listener;
	}
	
	public boolean isPressed()
	{
		return pressed;
	}
}

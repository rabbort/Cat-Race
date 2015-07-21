package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad.TouchpadStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class AndroidController //extends Player
{
	Stage stage;
	Touchpad move;
	Touchpad turn;
	float deadzone;
	Skin skin;
	Drawable background, knob;
	TouchpadStyle style;
	OrthographicCamera camera;
	int knobWidth;
	int backgroundWidth;
	int padding;
	
	Button enterVehicle;
	TextButtonStyle enterVehicleStyle;
	
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

		enterVehicle = new TextButton("Enter Vehicle", enterVehicleStyle);
		enterVehicle.setWidth(Gdx.graphics.getWidth() / 8);
		enterVehicle.setHeight(Gdx.graphics.getHeight() / 8);
		enterVehicle.setPosition(Gdx.graphics.getWidth() - enterVehicle.getWidth(), Gdx.graphics.getHeight() / 2);
		enterVehicle.addListener(new ClickListener() 
		{
			public void clicked(InputEvent event, float x, float y)
			{
				
			}
		});

		stage.addActor(enterVehicle);
		stage.addActor(move);
		stage.addActor(turn);
		
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
}

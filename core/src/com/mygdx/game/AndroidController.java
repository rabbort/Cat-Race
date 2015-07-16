package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad.TouchpadStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

public class AndroidController extends CharacterTest
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
	
	Button flyToggle;
	TextButtonStyle flyToggleStyle;
	private boolean flying;
	
	@Override
	public void dispose()
	{
		stage.dispose();
	}

	//@Override
	//public void create() {
	public AndroidController()
	{
		flying = false;
		
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
		
		flyToggleStyle = new TextButtonStyle();
		flyToggleStyle.font = new BitmapFont();
		flyToggleStyle.fontColor = Color.RED;
		flyToggleStyle.checkedFontColor = Color.GREEN;

		flyToggle = new TextButton("Fly", flyToggleStyle);
		flyToggle.setWidth(Gdx.graphics.getWidth() / 8);
		flyToggle.setHeight(Gdx.graphics.getHeight() / 8);
		flyToggle.setPosition(Gdx.graphics.getWidth() - flyToggle.getWidth(), Gdx.graphics.getHeight() / 2);
		flyToggle.addListener(new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				if(flying == false)
					flying = true;
				else
					flying = false;
			}
		});

		stage.addActor(flyToggle);
		stage.addActor(move);
		stage.addActor(turn);
		
		Gdx.input.setInputProcessor(stage);
	}
	
	public boolean getFlying()
	{
		return flying;
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

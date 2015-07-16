package com.mygdx.game;

import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.utils.TextureProvider;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.utils.JsonReader;

public class EnvironmentEntity extends BaseBulletTest
{
	private Model model;
	private ModelData modelData;
	private ModelLoader modelLoader;
	
	public void create(FileHandle handle, BaseBulletTest base, boolean shouldCollide, String name)
	{
		modelLoader = new G3dModelLoader(new JsonReader());
		
		modelData = modelLoader.loadModelData(handle);
		model = new Model(modelData, new TextureProvider.FileTextureProvider());
		base.disposables.add(model);
		
		if(shouldCollide)
			base.world.addConstructor(name, new BulletConstructor(model, 0f, Bullet.obtainStaticNodeShape(model.nodes)));
		else
			base.world.addConstructor(name, new BulletConstructor(model, 0f, null));
	}
}

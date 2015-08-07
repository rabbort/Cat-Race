/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.mygdx.game.World;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.mygdx.game.ChaseCamera;
import com.mygdx.game.Environment.Terrain;
import com.badlogic.gdx.graphics.g3d.ModelInstance;

/** @author xoppa No physics, simple base class for rendering a bunch of entities. */
public class BaseWorld<T extends BaseEntity> implements Disposable 
{
	int terrain;
	Vector3 position = new Vector3();
	Vector3 center = new Vector3();
	Vector3 dimensions = new Vector3();
	BoundingBox bounds = new BoundingBox();
	
	public static abstract class Constructor<T extends BaseEntity> implements Disposable 
	{
		public Model model = null;

		public abstract T construct (final float x, final float y, final float z);

		public abstract T construct (final Matrix4 transform);
	}

	private final ObjectMap<String, Constructor<T>> constructors = new ObjectMap<String, Constructor<T>>();
	protected final Array<T> entities = new Array<T>();
	private final Array<Model> models = new Array<Model>();

	public void addConstructor (final String name, final Constructor<T> constructor) 
	{
		constructors.put(name, constructor);
		if (constructor.model != null && !models.contains(constructor.model, true)) models.add(constructor.model);
	}
	
	public void removeConstructor(final String name)
	{
		constructors.remove(name);
	}

	public Constructor<T> getConstructor (final String name) 
	{
		return constructors.get(name);
	}

	public void add (final T entity) 
	{
		entities.add(entity);
	}
	
	public void remove(final T entity)
	{
		entities.removeValue(entity, false);
	}

	public T add (final String type, float x, float y, float z) 
	{
		final T entity = constructors.get(type).construct(x, y, z);
		// Set frustum info for static object so they can be culled when not in view
		if(type.contains("terrain") || type.contains("floor") || type.contains("roof") || type.contains("block"))
		{
			((BulletEntity)entity).type = 1;
			((BulletEntity)entity).setFrustumInfo();
		}
		
		add(entity);
		return entity;
	}

	public T add (final String type, final Matrix4 transform) 
	{
		final T entity = constructors.get(type).construct(transform);
		add(entity);
		return entity;
	}

	public void render (final ModelBatch batch, final Environment lights) 
	{
		render(batch, lights, entities);
	}

	public void render (final ModelBatch batch, final Environment lights, final Iterable<T> entities) 
	{
		for (final T e : entities) 
		{
			if(((BulletEntity)e).type == 1)
			{
				if(isVisible(GameManager.inst.camera, (BulletEntity)e))
				{
					batch.render(e.modelInstance, lights);
				}
			}
			else
				batch.render(e.modelInstance, lights);
		}
	}

	public void render (final ModelBatch batch, final Environment lights, final T entity) 
	{
		//if(isVisible(GameManager.inst.camera, (BulletEntity)entity))
			batch.render(entity.modelInstance, lights);
	}
	
	private boolean isVisible(final ChaseCamera cam, final BulletEntity instance)
	{
		return cam.frustum.boundsInFrustum(instance.position, instance.dimensions);
	}

	public void update () 
	{
	}

	@Override
	public void dispose () 
	{
		for (int i = 0; i < entities.size; i++)
			entities.get(i).dispose();
		entities.clear();

		for (Constructor<T> constructor : constructors.values())
			constructor.dispose();
		constructors.clear();

		models.clear();
	}
}
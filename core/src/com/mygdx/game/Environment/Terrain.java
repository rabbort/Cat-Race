package com.mygdx.game.Environment;

import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBvhTriangleMeshShape;
import com.badlogic.gdx.physics.bullet.collision.btTriangleIndexVertexArray;
import com.mygdx.game.World.BulletConstructor;
import com.mygdx.game.World.BulletEntity;
import com.mygdx.game.World.GameManager;

import NoiseGeneration.NoiseGenerator;

public class Terrain implements Runnable
{
	private TerrainChunk chunk;
	private Mesh mesh;
	private Model terrainModel;
	private Material material;
	private String location;
	private Vector3 center = new Vector3();
	private GameManager base;
	private static double[][] simplex;
	private static int terrainScale = 4;
	private BulletEntity terrain;
	
	private Thread terrainThread;

	public Terrain(GameManager base, String location, Vector3 center)
	{
		this.base = base;
		this.location = location;
		this.center = center;
		
		terrainThread = new Thread(this, "terrain"+location);
		terrainThread.start();
	}
	
	@Override
	public void run() 
	{
		// position, normal, color, texture
		int vertexSize = 3 + 3 + 1 + 2; 
		// Create the new chunk on this thread
		chunk = new TerrainChunk(128, 128, vertexSize, this.location, this.center);
		// Pass the results to opengl
		Gdx.app.postRunnable(new Runnable() 
		{
			@Override
			public void run()
			{
				create();
			}
		});
	}
	
	public void create() 
	{  
	    mesh = new Mesh(true, chunk.vertices.length / 3, chunk.indices.length,
	            new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
	            new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
	            new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
	            new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE));
	    mesh.setVertices(chunk.vertices);
	    mesh.setIndices(chunk.indices);
	    
	    material = new Material(ColorAttribute.createSpecular(1,1,1,1), FloatAttribute.createShininess(8f));
	    
	    terrainModel = new Model();
	    
	    MeshPart meshPart = new MeshPart();
	    meshPart.id = "terrainChunk"+this.location;
	    meshPart.indexOffset = 0;
	    meshPart.numVertices = mesh.getNumIndices();
	    meshPart.primitiveType = GL30.GL_TRIANGLES;
	    meshPart.mesh = mesh;
	    
	    btTriangleIndexVertexArray vertArray = new btTriangleIndexVertexArray(meshPart);
	    btBvhTriangleMeshShape terrainShape = new btBvhTriangleMeshShape(vertArray, true);
	    
	    NodePart nodePart = new NodePart();
	    nodePart.material = material;
	    nodePart.meshPart = meshPart;
	    
	    Node node = new Node();
	    node.id = "terrainNode"+this.location;
	    node.parts.add(nodePart);
	    
	    terrainModel.meshes.add(mesh);
	    terrainModel.materials.add(material);
	    terrainModel.nodes.add(node);
	    terrainModel.meshParts.add(meshPart);
	    terrainModel.manageDisposable(mesh);
	    
	    this.center.x = (int)this.center.x;
	    this.center.z = (int)this.center.z;
	    
	    this.base.world.addConstructor("terrain"+this.location, new BulletConstructor(terrainModel, 0f, terrainShape));
	    this.base.disposables.add(terrainModel);
	    this.base.disposables.add(vertArray);
	    this.base.disposables.add(terrainShape);
	    
	    switch(this.location)
	    {
	    	case "NW": 	terrain = this.base.world.add("terrain"+this.location, this.center.x - 128 * terrainScale, 0, this.center.z + 128 * terrainScale);
						break;
	    	case "N": 	terrain = this.base.world.add("terrain"+this.location, this.center.x, 0, this.center.z + 128 * terrainScale);
						break;
	    	case "NE": 	terrain = this.base.world.add("terrain"+this.location, this.center.x + 128 * terrainScale, 0, this.center.z + 128 * terrainScale);
	    				break;
	    	case "W": 	terrain = this.base.world.add("terrain"+this.location, this.center.x - 128 * terrainScale, 0, this.center.z);
						break;
	    	case "C": 	terrain = this.base.world.add("terrain"+this.location, this.center.x, 0, this.center.z);
	    				break;
	    	case "E": 	terrain = this.base.world.add("terrain"+this.location, this.center.x + 128 * terrainScale, 0, this.center.z);
						break;
	    	case "SW": 	terrain = this.base.world.add("terrain"+this.location, this.center.x - 128 * terrainScale, 0, this.center.z - 128 * terrainScale);
						break;
	    	case "S": 	terrain = this.base.world.add("terrain"+this.location, this.center.x, 0, this.center.z - 128 * terrainScale);
						break;
	    	case "SE": 	terrain = this.base.world.add("terrain"+this.location, this.center.x + 128 * terrainScale, 0, this.center.z - 128 * terrainScale);
						break;
	    }

	    this.base.disposables.add(terrain);
	}
	
	final static class TerrainChunk 
	{
	    public final float[] heightMap;
	    public final short width;
	    public final short height;
	    public final float[] vertices;
	    public final short[] indices;
	
	    public final int vertexSize;
	    private final int positionSize = 3;
	    private final String location;
	    private final Vector3 center;
	
	    public TerrainChunk(int width, int height, int vertexSize, String location, Vector3 center) 
	    {
	
	        if ((width + 1) * (height + 1) > Short.MAX_VALUE) 
	        {
	            throw new IllegalArgumentException(            
	                    "Chunk size too big, (width + 1)*(height+1) must be <= 32767");
	        }
	
	        this.heightMap = new float[(width + 1) * (height + 1)];
	        this.width = (short) width;
	        this.height = (short) height;
	        this.vertices = new float[heightMap.length * vertexSize];
	        this.indices = new short[width * height * 6];
	        this.vertexSize = vertexSize;
	        this.location = location;
	        this.center = center;
	
	        buildHeightmap();
	
	        buildIndices();
	        buildVertices();
	
	        calcNormals(indices, vertices);
	    }

	    public void buildHeightmap() 
	    {
	        int idh = 0;
	        int xOffset = 0;
	        int yOffset = 0;
	        
	        simplex = new double[this.width + 1][this.height + 1];
	        
	        switch(this.location)
	        {
	        	case "NW": 	xOffset = ((int)this.center.x / terrainScale) - 64 - (128 * 1);
							yOffset = ((int)this.center.z / terrainScale) + 64 + (128 * 1);
							break;
	        	case "N": 	xOffset = ((int)this.center.x / terrainScale) - 64 - (128 * 0);
							yOffset = ((int)this.center.z / terrainScale) + 64 + (128 * 1);
							break;
	        	case "NE": 	xOffset = ((int)this.center.x / terrainScale) + 64 + (128 * 0);
							yOffset = ((int)this.center.z / terrainScale) + 64 + (128 * 1);
							break;
	        	case "W": 	xOffset = ((int)this.center.x / terrainScale) - 64 - (128 * 1);
							yOffset = ((int)this.center.z / terrainScale) + 64 + (128 * 0);
							break;
	        	case "C": 	xOffset = ((int)this.center.x / terrainScale) - 64 - (128 * 0);
							yOffset = ((int)this.center.z / terrainScale) + 64 + (128 * 0);
							break;
	        	case "E": 	xOffset = ((int)this.center.x / terrainScale) + 64 + (128 * 0);
							yOffset = ((int)this.center.z / terrainScale) + 64 + (128 * 0);
							break;
	        	case "SW": 	xOffset = ((int)this.center.x / terrainScale) - 64 - (128 * 1);
							yOffset = ((int)this.center.z / terrainScale) - 64 - (128 * 0);
							break;
	        	case "S": 	xOffset = ((int)this.center.x / terrainScale) - 64 - (128 * 0);
							yOffset = ((int)this.center.z / terrainScale) - 64 - (128 * 0);
							break;
	        	case "SE": 	xOffset = ((int)this.center.x / terrainScale) + 64 + (128 * 0);
							yOffset = ((int)this.center.z / terrainScale) - 64 - (128 * 0);
							break;
	        }
	        
	        simplex = NoiseGenerator.get2DNoise(xOffset, yOffset);
	
	        for (int x = 0; x < this.width + 1; x++) 
	        {
	            for (int y = 0; y < this.height + 1; y++) 
	            {
	                this.heightMap[idh++] = (float)simplex[x][y];
	            }
	        }
	    }

	    public void buildVertices() 
	    {
	        int heightPitch = height + 1;
	        int widthPitch = width + 1;
	
	        int idx = 0;
	        int hIdx = 0;
	        int strength = 300; // multiplier for height map
	
	        float scale = terrainScale;
	
	        for (int z = 0; z < heightPitch; z++) 
	        {
	            for (int x = 0; x < widthPitch; x++) 
	            {
	                // POSITION
	                vertices[idx++] = scale * z;
	                vertices[idx++] = heightMap[hIdx++] * strength - strength;
	                vertices[idx++] = scale * x;
	
	                // NORMAL
	                idx += 3;
	                
	                int mountainCapBorder = MathUtils.random(60, 100);
	                int mountainBorder = MathUtils.random(10, 30);
	                int dirtBorder = MathUtils.random(-5, mountainBorder);
	                int white = MathUtils.random(240, 255);
	                // COLOR
	                if(vertices[idx - 5] > mountainCapBorder)
	                	vertices[idx++] = Color.toFloatBits(white, white, white, 1);
	                else if(vertices[idx - 5] > mountainBorder)
	                {
	                	int grey = MathUtils.random(100, 200);
	                	vertices[idx++] = Color.toFloatBits(grey, grey, grey, 1);
	                }
	                else if(vertices[idx - 5] > dirtBorder)
	                	vertices[idx++] = Color.toFloatBits(MathUtils.random(100, 120), MathUtils.random(60, 80), 
	                			MathUtils.random(20, 30), 1);
	                else
	                	vertices[idx++] = Color.toFloatBits(0, MathUtils.random(50, 90), 0, 1);
	
	                // TEXTURE
	                vertices[idx++] = (x / (float) width * scale);
	                vertices[idx++] = (z / (float) height * scale);
	            }
	        }
	    }

	    private void buildIndices() 
	    {
	        int idx = 0;
	        short pitch = (short) (width + 1);
	        short i1 = 0;
	        short i2 = 1;
	        short i3 = (short) (1 + pitch);
	        short i4 = pitch;
	
	        short row = 0;
	
	        for (int z = 0; z < height; z++) 
	        {
	            for (int x = 0; x < width; x++) 
	            {
	                indices[idx++] = i1;
	                indices[idx++] = i2;
	                indices[idx++] = i3;
	
	                indices[idx++] = i3;
	                indices[idx++] = i4;
	                indices[idx++] = i1;
	
	                i1++;
	                i2++;
	                i3++;
	                i4++;
	            }
	
	            row += pitch;
	            i1 = row;
	            i2 = (short) (row + 1);
	            i3 = (short) (i2 + pitch);
	            i4 = (short) (row + pitch);
	        }
	    }
	
	    // Gets the index of the first float of a normal for a specific vertex
	    private int getNormalStart(int vertIndex) 
	    {
	        return vertIndex * vertexSize + positionSize;
	    }
	
	    // Gets the index of the first float of a specific vertex
	    private int getPositionStart(int vertIndex) 
	    {
	        return vertIndex * vertexSize;
	    }
	
	    // Adds the provided value to the normal
	    private void addNormal(int vertIndex, float[] verts, float x, float y, float z) 
	    {
	        int i = getNormalStart(vertIndex);
	
	        verts[i] += x;
	        verts[i + 1] += y;
	        verts[i + 2] += z;
	    }
	
	    /*
	     * Normalizes normals
	     */
	    private void normalizeNormal(int vertIndex, float[] verts) 
	    {
	        int i = getNormalStart(vertIndex);
	
	        float x = verts[i];
	        float y = verts[i + 1];
	        float z = verts[i + 2];
	
	        float num2 = ((x * x) + (y * y)) + (z * z);
	        float num = 1f / (float) Math.sqrt(num2);
	        x *= num;
	        y *= num;
	        z *= num;
	
	        verts[i] = x;
	        verts[i + 1] = y;
	        verts[i + 2] = z;
	    }
	
	    /*
	     * Calculates the normals
	     */
	    private void calcNormals(short[] indices, float[] verts) 
	    {
	        for (int i = 0; i < indices.length; i += 3) {
	            int i1 = getPositionStart(indices[i]);
	            int i2 = getPositionStart(indices[i + 1]);
	            int i3 = getPositionStart(indices[i + 2]);
	
	            // p1
	            float x1 = verts[i1];
	            float y1 = verts[i1 + 1];
	            float z1 = verts[i1 + 2];
	
	            // p2
	            float x2 = verts[i2];
	            float y2 = verts[i2 + 1];
	            float z2 = verts[i2 + 2];
	
	            // p3
	            float x3 = verts[i3];
	            float y3 = verts[i3 + 1];
	            float z3 = verts[i3 + 2];
	
	            // u = p3 - p1
	            float ux = x3 - x1;
	            float uy = y3 - y1;
	            float uz = z3 - z1;
	
	            // v = p2 - p1
	            float vx = x2 - x1;
	            float vy = y2 - y1;
	            float vz = z2 - z1;
	
	            // n = cross(v, u)
	            float nx = (vy * uz) - (vz * uy);
	            float ny = (vz * ux) - (vx * uz);
	            float nz = (vx * uy) - (vy * ux);
	
	            // normalize(n)
	            float num2 = ((nx * nx) + (ny * ny)) + (nz * nz);
	            float num = 1f / (float) Math.sqrt(num2);
	            nx *= num;
	            ny *= num;
	            nz *= num;
	
	            addNormal(indices[i], verts, nx, ny, nz);
	            addNormal(indices[i + 1], verts, nx, ny, nz);
	            addNormal(indices[i + 2], verts, nx, ny, nz);
	        }
	
	        for (int i = 0; i < (verts.length / vertexSize); i++) 
	        {
	            normalizeNormal(i, verts);
	        }
	    }
	}
}
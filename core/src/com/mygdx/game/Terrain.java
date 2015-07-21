package com.mygdx.game;

import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBvhTriangleMeshShape;
import com.badlogic.gdx.physics.bullet.collision.btTriangleIndexVertexArray;

import NoiseGeneration.NoiseGenerator;

public class Terrain
{
	private TerrainChunk chunk;
	private Mesh mesh;
	
	private ShaderProgram shader;
	private Texture terrainTexture;
	
	private final Matrix3 normalMatrix = new Matrix3();
	
	private static final float[] lightPosition = { 5, 35, 5 };
	private static final float[] ambientColor = { 0.2f, 0.2f, 0.2f, 1.0f };
	private static final float[] diffuseColor = { 0.5f, 0.5f, 0.5f, 1.0f };
	private static final float[] specularColor = { 0.7f, 0.7f, 0.7f, 1.0f };
	
	private Matrix4 modelView = new Matrix4();
	private Model terrainModel;
	private Material material;
	private String location;
	private Vector3 center = new Vector3();
	
	private BaseBulletTest base;
	private static double[][] simplex;
	private static int terrainScale = 20;
	
	private BulletEntity terrain;
	
	private final String vertexShader =
        "attribute vec4 a_position; \n" +
        "attribute vec3 a_normal; \n" +
        "attribute vec2 a_texCoord; \n" +
        "attribute vec4 a_color; \n" +

        "uniform mat4 u_MVPMatrix; \n" +
        "uniform mat3 u_normalMatrix; \n" +

        "uniform vec3 u_lightPosition; \n" +

        "varying float intensity; \n" +
        "varying vec2 texCoords; \n" +
        "varying vec4 v_color; \n" +

        "void main() { \n" +
        "    vec3 normal = normalize(u_normalMatrix * a_normal); \n" +
        "    vec3 light = normalize(u_lightPosition); \n" +
        "    intensity = max( dot(normal, light) , 0.0); \n" +

        "    v_color = a_color; \n" +
        "    texCoords = a_texCoord; \n" +

        "    gl_Position = u_MVPMatrix * a_position; \n" +
        "}";

	private final String fragmentShader =
        "#ifdef GL_ES \n" +
        "precision mediump float; \n" +
        "#endif \n" +

        "uniform vec4 u_ambientColor; \n" +
        "uniform vec4 u_diffuseColor; \n" +
        "uniform vec4 u_specularColor; \n" +

        "uniform sampler2D u_texture; \n" +
        "varying vec2 texCoords; \n" +
        "varying vec4 v_color; \n" +

        "varying float intensity; \n" +

        "void main() { \n" +
        "    gl_FragColor = v_color * intensity * texture2D(u_texture, texCoords); \n" +
        "}";

	public Terrain(BaseBulletTest base, String location, Vector3 center)
	{
		this.base = base;
		this.location = location;
		this.center = center;
		
		create(this.base);
	}
	
	public void update()
	{
		render();
	}
	
	public void create(BaseBulletTest base) 
	{
	    // Terrain texture size is 128x128
	    terrainTexture = new Texture(Gdx.files.internal("data/chara.jpg"));

	    // position, normal, color, texture
	    int vertexSize = 3 + 3 + 1 + 2;  
	
	    chunk = new TerrainChunk(128, 128, vertexSize, this.location, this.center);
	    
	    mesh = new Mesh(true, chunk.vertices.length / 3, chunk.indices.length,
	            new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
	            new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
	            new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
	            new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE));
	    mesh.setVertices(chunk.vertices);
	    mesh.setIndices(chunk.indices);
	
	    ShaderProgram.pedantic = false;
	
	    shader = new ShaderProgram(vertexShader, fragmentShader);
	    
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
	    this.base.disposables.add(terrainTexture);
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
	
	public ShaderProgram getShader()
	{
		return shader;
	}
	
	//@Override
	public void render() 
	
	{	
		terrainTexture.bind();
	    shader.begin();
	    Matrix4 model = new Matrix4();
	    modelView.set(this.base.camera.view).mul(model);
	
	    shader.setUniformMatrix("u_MVPMatrix", this.base.camera.combined);
	    shader.setUniformMatrix("u_normalMatrix", normalMatrix.set(modelView).inv().transpose());

	
	    shader.setUniform3fv("u_lightPosition", lightPosition, 0, 3);
	    shader.setUniform4fv("u_ambientColor", ambientColor, 0, 4);
	    shader.setUniform4fv("u_diffuseColor", diffuseColor, 0, 4);
	    shader.setUniform4fv("u_specularColor", specularColor, 0, 4);
	
	    //shader.setUniformi("u_texture", 0);
	    
	    mesh.render(shader, GL30.GL_TRIANGLES);
	
	    shader.end();
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
	        	case "NW": 	xOffset = (int)this.center.x - 64 - (128 * 1);
							yOffset = (int)this.center.z + 64 + (128 * 1);
							break;
	        	case "N": 	xOffset = (int)this.center.x - 64 - (128 * 0);
							yOffset = (int)this.center.z + 64 + (128 * 1);
							break;
	        	case "NE": 	xOffset = (int)this.center.x + 64 + (128 * 0);
							yOffset = (int)this.center.z + 64 + (128 * 1);
							break;
	        	case "W": 	xOffset = (int)this.center.x - 64 - (128 * 1);
							yOffset = (int)this.center.z + 64 + (128 * 0);
							break;
	        	case "C": 	xOffset = (int)this.center.x - 64 - (128 * 0);
							yOffset = (int)this.center.z + 64 + (128 * 0);
							break;
	        	case "E": 	xOffset = (int)this.center.x + 64 + (128 * 0);
							yOffset = (int)this.center.z + 64 + (128 * 0);
							break;
	        	case "SW": 	xOffset = (int)this.center.x - 64 - (128 * 1);
							yOffset = (int)this.center.z - 64 - (128 * 0);
							break;
	        	case "S": 	xOffset = (int)this.center.x - 64 - (128 * 0);
							yOffset = (int)this.center.z - 64 - (128 * 0);
							break;
	        	case "SE": 	xOffset = (int)this.center.x + 64 + (128 * 0);
							yOffset = (int)this.center.z - 64 - (128 * 0);
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
	        int strength = 10; // multiplier for height map
	
	        float scale = terrainScale;
	
	        for (int z = 0; z < heightPitch; z++) 
	        {
	            for (int x = 0; x < widthPitch; x++) 
	            {
	                // POSITION
	                vertices[idx++] = scale * z;
	                
	                // Exaggerate high values to create larger mountains
	                /*if(heightMap[hIdx] > 2.7)
	                	heightMap[hIdx] *= 5;
	                else if(heightMap[hIdx] > 2.4)
	                	heightMap[hIdx] *= 4.5;
	                else if(heightMap[hIdx] > 2.0)
	                	heightMap[hIdx] *= 3.3;
	                else*/
	                	heightMap[hIdx] = 0;//MathUtils.random(0f, 0.2f);

	                vertices[idx++] = heightMap[hIdx++] * strength;
	                vertices[idx++] = scale * x;
	
	                // NORMAL
	                idx += 3;
	
	                // COLOR
	                if(vertices[idx - 5] > 100)
	                	vertices[idx++] = Color.WHITE.toFloatBits();
	                else if(vertices[idx - 5] > 20)
	                {
	                	int grey = MathUtils.random(100, 200);
	                	vertices[idx++] = Color.toFloatBits(grey, grey, grey, 1);
	                }
	                else if(vertices[idx - 5] > 15)
	                	vertices[idx++] = Color.toFloatBits(MathUtils.random(100, 120), MathUtils.random(60, 80), 
	                			MathUtils.random(20, 30), 1);
	                else //if(vertices[idx - 2])
	                	vertices[idx++] = Color.toFloatBits(0, MathUtils.random(50, 90), 0, 1);//GREEN.toFloatBits();
	
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
package com.mygdx.game.World;

import java.io.IOException;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.btAxisSweep3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btGhostPairCallback;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.LinearMath;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw.DebugDrawModes;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.mygdx.game.AndroidController;
import com.mygdx.game.ChaseCamera;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.Player;
import com.mygdx.game.PlayerManager;
import com.mygdx.game.Environment.SkyDome;
import com.mygdx.game.Environment.TerrainManager;
import com.mygdx.game.Vehicles.Vehicle;
import com.mygdx.game.kryonet.GameClient;
import com.mygdx.game.kryonet.GameServer;
import com.mygdx.game.kryonet.Network;
import com.mygdx.game.kryonet.Network.UpdateCharacter;

public class GameManager extends BulletTest implements Screen
{
	public static GameManager inst;
	
	// Set this to the path of the lib to use it on desktop instead of default lib.
	private final static String customDesktopLib = null;

	private static boolean initialized = false;
	
	public static boolean shadows = true;
	
	public static void init () 
	{
		if (initialized) return;
		// Need to initialize bullet before using it.
		if (Gdx.app.getType() == ApplicationType.Desktop && customDesktopLib != null) 
		{
			System.load(customDesktopLib);
		} else
			Bullet.init();
		Gdx.app.log("Bullet", "Version = " + LinearMath.btGetVersion());
		initialized = true;
	}

	public Environment environment;
	public DirectionalLight light;
	public ModelBatch shadowBatch;
	public Matrix4 character = new Matrix4();
	public SkyDome sky;
	private TerrainManager terrainManager;
	
	public final Vector3 tmp = new Vector3();

	public BulletWorld world;
	public ObjLoader objLoader = new ObjLoader();
	public ModelBuilder modelBuilder = new ModelBuilder();
	public ModelBatch modelBatch;
	public Array<Disposable> disposables = new Array<Disposable>();
	private int debugMode = DebugDrawModes.DBG_NoDebug;
	private AndroidController controller;
	private boolean android;
	private Vector3 playerPosition = new Vector3();
	
	public Vehicle vehicle;
	private Player player;
	
	private MyGdxGame game;
	
	private Network network;
	private GameClient client;
	private GameServer server;
	private PlayerManager pm;
	private byte ID;
	private Vector3 startPosition;
	
	protected final static Vector3 tmpV1 = new Vector3(), tmpV2 = new Vector3();
	
	public GameManager()
	{
		
	}
	
	public GameManager(MyGdxGame game, Network.Character player)
	{
		this.game = game;
		ID = player.id;
		startPosition = player.transform;
		create();
	}

	public BulletWorld createWorld () 
	{
		// We create the world using an axis sweep broadphase for this test
		btDefaultCollisionConfiguration collisionConfiguration = new btDefaultCollisionConfiguration();
		btCollisionDispatcher dispatcher = new btCollisionDispatcher(collisionConfiguration);
		btAxisSweep3 sweep = new btAxisSweep3(new Vector3(-1000, -1000, -1000), new Vector3(1000, 1000, 1000));
		btSequentialImpulseConstraintSolver solver = new btSequentialImpulseConstraintSolver();
		btDiscreteDynamicsWorld collisionWorld = new btDiscreteDynamicsWorld(dispatcher, sweep, solver, collisionConfiguration);
		btGhostPairCallback ghostPairCallback = new btGhostPairCallback();
		disposables.add(ghostPairCallback);
		sweep.getOverlappingPairCache().setInternalGhostPairCallback(ghostPairCallback);
		return new BulletWorld(collisionConfiguration, dispatcher, sweep, solver, collisionWorld);
	}
	
	public boolean isAndroid(){
		return android;
	}
	
	public AndroidController getController()
	{
		return controller;
	}

	@Override
	public void create () 
	{
		init();
		inst = this;
		pm = new PlayerManager();
		
		// Find out if we are playing on desktop or android
		switch(Gdx.app.getType())
		{
			case Android:
				android = true;
				controller = new AndroidController();
			default:
				Gdx.input.setCursorCatched(true);
				break;
		}

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1.f));
		light = shadows ? new DirectionalShadowLight(1024, 1024, 20f, 20f, 1f, 300f) : new DirectionalLight();
		light.set(0.8f, 0.8f, 0.8f, -0.5f, -1f, -0.5f);
		environment.add(light);
		if (shadows)
			environment.shadowMap = (DirectionalShadowLight)light;
		shadowBatch = new ModelBatch(new DepthShaderProvider());
		
		modelBatch = new ModelBatch();

		world = createWorld();
		world.performanceCounter = performanceCounter;
		
		final float width = Gdx.graphics.getWidth();
		final float height = Gdx.graphics.getHeight();
		if (width > height)
			camera = new ChaseCamera(67f, 3f * width / height, 3f, android ? controller : null);
		else
			camera = new ChaseCamera(67f, 3f, 3f * height / width, android ? controller : null);
		camera.near = 0.01f;
		camera.far = 5000f;
		camera.update();

		terrainManager = new TerrainManager(playerPosition, this);
		sky = new SkyDome(this);
		
		startConnection();
		
		player = new Player(startPosition);
		player.setID(ID);
	}
	
	public void startConnection()
	{
		network = new Network();
		client = new GameClient();
	}
	
	public void updatePlayerTransform(Matrix4 transform, byte id, byte status)
	{
		client.updateCharacter(id, transform, status);
	}
	
	public void Spawn(Vector3 startPosition)
	{
		vehicle = new Vehicle(this, startPosition);
		player = new Player(startPosition);
		player.setID(ID);
	}
	
	public void setID(byte id)
	{
		ID = id;
	}
	
	public Player getClientPlayer()
	{
		return player;
	}

	@Override
	public void dispose () 
	{
		world.dispose();
		world = null;

		for (Disposable disposable : disposables)
			disposable.dispose();
		disposables.clear();

		modelBatch.dispose();
		modelBatch = null;

		shadowBatch.dispose();
		shadowBatch = null;

		if (shadows)
			((DirectionalShadowLight)light).dispose();
		light = null;

		super.dispose();
		controller.dispose();
		player.dispose();
	}

	@Override
	public void render (float delta) 
	{		
		render(true);
	}

	public void render (boolean update) 
	{
		if (update) update();

		beginRender(true);
		
		renderWorld();

		Gdx.gl.glDisable(GL30.GL_DEPTH_TEST);
		//if (debugMode != DebugDrawModes.DBG_NoDebug) 
			world.setDebugMode(debugMode);
		Gdx.gl.glEnable(GL30.GL_DEPTH_TEST);
		
		// Only need to update controller if on android
		if(controller != null)
			controller.update();
	}
	
	public void setCharacter(Matrix4 charMatrix)
	{
		character = charMatrix;
	}

	protected void beginRender (boolean lighting) 
	{
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

		character.getTranslation(playerPosition);
		terrainManager.setPosition(playerPosition);
		terrainManager.update();
		
		if(vehicle != null)
		{
			vehicle.update();
			
		}
		
		player.update();
		
		// Update the other players in the game
		for(int i = 0; i < PlayerManager.playerManager.getPlayers().size; i++)
		{
			PlayerManager.playerManager.getPlayers().get(i).update();
		}
		
		camera.transform.set(character);
		camera.update();
	}


	protected void renderWorld () 
	{
		if (shadows) 
		{
			((DirectionalShadowLight)light).begin(Vector3.Zero, camera.direction);
			shadowBatch.begin(((DirectionalShadowLight)light).getCamera());
			world.render(shadowBatch, null);
			shadowBatch.end();
			((DirectionalShadowLight)light).end();
		}

		modelBatch.begin(camera);
		world.render(modelBatch, environment);
		modelBatch.end();
	}

	public void update () 
	{
		world.update();
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}
}

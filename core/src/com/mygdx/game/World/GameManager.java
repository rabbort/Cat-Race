package com.mygdx.game.World;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
import com.mygdx.game.Sounds;
import com.mygdx.game.Environment.SkyDome;
import com.mygdx.game.Environment.TerrainManager;
import com.mygdx.game.Vehicles.VehicleManager;
import com.mygdx.game.kryonet.GameClient;
import com.mygdx.game.kryonet.GameServer;
import com.mygdx.game.kryonet.Network;

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
	private FPSLogger fps;
	
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
	
	public VehicleManager vehicleManager;
	private Player player;
	
	private MyGdxGame game;
	
	private Network network;
	private GameClient client;
	private GameServer server;
	private PlayerManager pm;
	private int ID;
	private Vector3 startPosition;
	
	private SpriteBatch spriteBatch;
	private BitmapFont font;
	private String clientName;
	private Network.Character player1;
	
	private Sounds sounds;
	private int numVotes;
	
	protected final static Vector3 tmpV1 = new Vector3(), tmpV2 = new Vector3();
	
	public GameManager()
	{
		
	}
	
	public GameManager(MyGdxGame game, Network.Character player, int id, GameClient client)
	{
		this.client = client;
		this.game = game;
		ID = id;
		player1 = player;
		startPosition = player.transform.cpy();
		clientName = player.name;
		if(clientName.length() > 7)
			clientName = clientName.substring(0, 6);
		
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
		fps = new FPSLogger();
		spriteBatch = new SpriteBatch();
		font = new BitmapFont();
		sounds = new Sounds();
		
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
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.6f, 0.6f, 0.6f, 1.0f));
		light = shadows ? new DirectionalShadowLight(1024, 1024, 20f, 20f, 1f, 300f) : new DirectionalLight();
		light.set(0.8f, 0.8f, 0.8f, -0.5f, -1f, -0.5f);
		environment.add(light);
		if (shadows)
			environment.shadowMap = (DirectionalShadowLight)light;
		shadowBatch = new ModelBatch(new DepthShaderProvider());
		
		modelBatch = new ModelBatch();

		world = createWorld();
		world.performanceCounter = performanceCounter;
		
		vehicleManager = new VehicleManager(4);
		
		final float width = Gdx.graphics.getWidth();
		final float height = Gdx.graphics.getHeight();
		if (width > height)
			camera = new ChaseCamera(67f, width, height);//3f * width / height, 3f);
		else
			camera = new ChaseCamera(67f, 3f, 3f * height / width);
		camera.near = 1f;
		camera.far = 2300f;
		camera.update();

		terrainManager = new TerrainManager(playerPosition, this);
		sky = new SkyDome(this);
		
		startPosition.x += 30f; 
		startPosition.z += 50f;
		player = new Player(startPosition);
		player.setID(ID);
		player.setName(clientName);
	}
	
	public void updatePlayerTransform(Matrix4 transform, String name, int id, byte status)
	{
		client.updateCharacter(id, name, transform, status);
	}
	
	public void updateVehicleTransform(Matrix4 chassis, int id, boolean hasDriver)
	{
		client.updateVehicle(chassis, id, hasDriver);
	}
	
	public void Spawn(Vector3 startPosition)
	{
		player = new Player(startPosition);
		player.setID(ID);
	}
	
	public void setID(int id)
	{
		ID = id;
	}
	
	public int getID()
	{
		return ID;
	}
	
	public Player getClientPlayer()
	{
		return player;
	}
	
	public Sounds getSounds()
	{
		return sounds;
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
		
		spriteBatch.dispose();
		spriteBatch = null;

		if (shadows)
			((DirectionalShadowLight)light).dispose();
		light = null;

		super.dispose();
		if(controller != null)
			controller.dispose();
		if(player != null)
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

		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
		//if (debugMode != DebugDrawModes.DBG_NoDebug) 
			world.setDebugMode(debugMode);
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		
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
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		character.getTranslation(playerPosition);
		terrainManager.setPosition(playerPosition);
		terrainManager.update();

		vehicleManager.updateVehicles();
		//fps.log();
		
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
		
		listPlayers();
		
		if(Gdx.input.isKeyJustPressed(Keys.N))
		{
			camera = null;
			camera = new ChaseCamera(67f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			camera.near = 1f;
			camera.far = 2300f;
			camera.transform.set(character);
		}
		
		if(Gdx.input.isKeyJustPressed(Keys.T))
		{
			client.vote();
		}
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
	
	public void restart()
	{
		// Stop all sounds before restarting
		sounds.stopSound(0);
		sounds.stopSound(1);
		sounds.stopSound(2);
		
		game.setScreen(new GameManager(game, player1, ID, client));
		this.dispose();
	}
	
	public GameClient getClient()
	{
		return client;
	}
	
	public void updateVotes(int numVotes)
	{
		this.numVotes = numVotes;
	}
	
	// Lists all current players, their place, and the distance they are at
	private void listPlayers()
	{
		int place = 1;
		boolean playerDrawn = false;
		
		spriteBatch.begin();
		// Text is small on android, so scale it up
		if(android)
			font.getData().setScale(2,2);
		font.setColor(Color.YELLOW);
		font.draw(spriteBatch, "Player", 5 * font.getScaleX(), Gdx.graphics.getHeight() - 5 * font.getScaleY());
		font.draw(spriteBatch, "Place", 60 * font.getScaleX(), Gdx.graphics.getHeight() - 5 * font.getScaleY());
		font.draw(spriteBatch, "Distance Travelled", 100 * font.getScaleX(), Gdx.graphics.getHeight() - 5 * font.getScaleY());
		font.setColor(Color.RED);
		
		pm.sortPlayers();
		
		// Print players to screen from highest distance to lowest
		if(pm.getPlayers().size > 0)
		{
			for(int i = pm.getPlayers().size - 1; i >= 0; i--)
			{
				if(playerPosition.x > pm.getPlayers().get(i).getPosition().x && !playerDrawn)
				{
					playerDrawn = true;
					font.setColor(Color.GREEN);
					font.draw(spriteBatch, clientName, 5 * font.getScaleX(), Gdx.graphics.getHeight() - 25 * place * font.getScaleY());
					font.draw(spriteBatch, ""+place, 60 * font.getScaleX(), Gdx.graphics.getHeight() - 25 * place * font.getScaleY());
					font.draw(spriteBatch, ""+(int)playerPosition.x, 100 * font.getScaleX(), Gdx.graphics.getHeight() - 25 * place * font.getScaleY());
					if(pm.getPlayers().get(i).getName() != null)
					{
						if(pm.getPlayers().get(i).getName().length() > 7)
							pm.getPlayers().get(i).setName(pm.getPlayers().get(i).getName().substring(0, 6));
						font.setColor(Color.RED);
						font.draw(spriteBatch, pm.getPlayers().get(i).getName(), 5 * font.getScaleX(), Gdx.graphics.getHeight() - 25 * (place + 1) * font.getScaleY());
						font.draw(spriteBatch, ""+(place + 1), 60 * font.getScaleX(), Gdx.graphics.getHeight() - 25 * (place + 1) * font.getScaleY());
						font.draw(spriteBatch, ""+(int)pm.getPlayers().get(i).getPosition().x, 100 * font.getScaleX(), Gdx.graphics.getHeight() - 25 * (place + 1) * font.getScaleY());
					}
					
					place++;
				}
				else
				{
					if(pm.getPlayers().get(i).getName() != null)
					{
						if(pm.getPlayers().get(i).getName().length() > 7)
							pm.getPlayers().get(i).setName(pm.getPlayers().get(i).getName().substring(0, 6));
						font.draw(spriteBatch, pm.getPlayers().get(i).getName(), 5 * font.getScaleX(), Gdx.graphics.getHeight() - 25 * place * font.getScaleY());
						font.draw(spriteBatch, ""+place, 60 * font.getScaleX(), Gdx.graphics.getHeight() - 25 * place * font.getScaleY());
						font.draw(spriteBatch, ""+(int)pm.getPlayers().get(i).getPosition().x, 100 * font.getScaleX(), Gdx.graphics.getHeight() - 25 * place * font.getScaleY());
					}
				}
				
				place++;
			}
		}
		
		if(!playerDrawn)
		{
			font.setColor(Color.GREEN);
			font.draw(spriteBatch, clientName, 5 * font.getScaleX(), Gdx.graphics.getHeight() - 25 * place * font.getScaleY());
			font.draw(spriteBatch, ""+place, 60 * font.getScaleX(), Gdx.graphics.getHeight() - 25 * place * font.getScaleY());
			font.draw(spriteBatch, ""+(int)playerPosition.x, 100 * font.getScaleX(), Gdx.graphics.getHeight() - 25 * place * font.getScaleY());
		}
		
		font.setColor(Color.YELLOW);
		font.draw(spriteBatch, "Votes needed to restart: "+(pm.getPlayers().size + 1 - numVotes), Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() - 5 * font.getScaleY());
		
		spriteBatch.end();
	}
}

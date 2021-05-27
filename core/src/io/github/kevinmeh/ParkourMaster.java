package io.github.kevinmeh;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.math.Rectangle;

public class ParkourMaster extends Game {

	// DECLARE VARIABLES HERE
	private TiledMap map;
	// INFO: tiled map dimensions start from top left.
	private MapObjects objects;
	private OrthogonalTiledMapRenderer renderer;
	private OrthographicCamera camera;
	private float mapHeight;
	private float mapWidth;
	
	private Animation<TextureRegion> agentPurpleIdle;
	private Animation<TextureRegion> agentPurpleWalk;
	private Animation<TextureRegion> agentPurpleShoot;
	// TODO: Add jumping animation
	
	private AgentPurple agentPurple;
	private Pool<Rectangle> rectanglePool = new Pool<Rectangle>() {
		@Override
		protected Rectangle newObject() {
			return new Rectangle();
		}
	};
	private Array<Rectangle> tiles = new Array<>();
	
	private static boolean debug = false;
	private ShapeRenderer debugRenderer;
	
	static float GRAVITY = -1.3f;
	static float BACKUP_GRAVITY = -1.3f;
	
	static final float TILE_SIZE = 4f;
	
	@Override
	public void create() {
		// NOTE: 1 Unit = 8 Pixels
		map = new TmxMapLoader().load("maps/stage1.tmx");
		renderer = new OrthogonalTiledMapRenderer(map, 1 / TILE_SIZE);
		mapWidth = map.getProperties().get("width", Integer.class);
		mapHeight = map.getProperties().get("height", Integer.class);
		MapLayer objectLayer = map.getLayers().get("entities");
		objects = objectLayer.getObjects();

		loadAgentPurple();
		
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 32, 18);
		camera.update();
		
		debugRenderer = new ShapeRenderer();
	}
	
	void loadAgentPurple() {
		Texture texture = new Texture("sprites/agentPurple.png");
		TextureRegion[] textureRegion = TextureRegion.split(texture, 25, 22)[0];
		agentPurpleIdle = new Animation<>(0.5f, textureRegion[0], textureRegion[1]);
		agentPurpleWalk = new Animation<>(0.15f, textureRegion[2], textureRegion[3], textureRegion[4], textureRegion[5]);
		agentPurpleShoot = new Animation<>(0.15f, textureRegion[6], textureRegion[7], textureRegion[8]);
		agentPurpleIdle.setPlayMode(Animation.PlayMode.LOOP);
		agentPurpleWalk.setPlayMode(Animation.PlayMode.LOOP);

		agentPurple = new AgentPurple();
		agentPurple.setPosition(new Vector2((Integer) objects.get("spawn").getProperties().get("x") / TILE_SIZE, mapHeight - (Integer) objects.get("spawn").getProperties().get("y") / TILE_SIZE));
	}

	@Override
	public void render() {
		ScreenUtils.clear(1.0f, 1.0f, 1.0f, 1);
		
		float deltaTime = Gdx.graphics.getDeltaTime();
		
		// Input, collision, position
		updateAgent(deltaTime);
		
		if(debug) renderDebug();
	}
	
	void updateCameraPosition() {
		camera.position.x = agentPurple.getPosition().x;
		camera.position.y = agentPurple.getPosition().y + 2;

		// Clamping camera to map:
		// There are 2 rectangles: The map and the camera. 
		// If the camera goes outside the map, set camera to be on the edge of map.
		float cameraLeft = camera.position.x - camera.viewportWidth / 2;
		float cameraRight = camera.position.x + camera.viewportWidth / 2;
		float cameraBottom = camera.position.y - camera.viewportHeight / 2;
		float cameraTop = camera.position.y + camera.viewportHeight / 2;

		if(cameraLeft <= 0)
			camera.position.x = camera.viewportWidth / 2;
		if(cameraRight >= mapWidth)
			camera.position.x = mapWidth - camera.viewportWidth / 2;
		if(cameraBottom <= 0)
			camera.position.y = camera.viewportHeight / 2;
		if(cameraTop >= mapHeight)
			camera.position.y = mapHeight - camera.viewportHeight / 2;

		camera.update();
	}

	// Prevents certain commands from executing multiple times with one keystroke
	float lastExecute = 0f;
	float executeThreshold = 0.25f;

	public void updateAgent(float deltaTime) {
		if(deltaTime == 0) return;

		if(deltaTime > 0.1f)
			deltaTime = 0.1f;

		agentPurple.setStateTime(deltaTime + agentPurple.getStateTime());
		
		lastExecute = lastExecute + deltaTime;

		// Checking input
		if(agentPurple.isGrounded() && (Gdx.input.isKeyPressed(Input.Keys.SPACE) || Gdx.input.isKeyPressed(Input.Keys.W))) {
			agentPurple.getVelocity().y += agentPurple.getJumpVelocity();
			agentPurple.setState(AgentPurple.State.JUMP);
			agentPurple.setGrounded(false);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
			agentPurple.getVelocity().x = agentPurple.getVelocity().x - agentPurple.getMaxVelocity();
			if(agentPurple.isGrounded()) agentPurple.setState(AgentPurple.State.WALK);
			agentPurple.setDirection(AgentPurple.Direction.LEFT);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
			agentPurple.getVelocity().x = agentPurple.getVelocity().x + agentPurple.getMaxVelocity();
			if(agentPurple.isGrounded()) agentPurple.setState(AgentPurple.State.WALK);
			agentPurple.setDirection(AgentPurple.Direction.RIGHT);
		}
		// TODO: Bind shoot to F
		if(Gdx.input.isKeyPressed(Input.Keys.B)) {
			if(lastExecute > executeThreshold) {
				debug = !debug;
				lastExecute = 0f;
			}
		}
		if(Gdx.input.isKeyPressed(Input.Keys.G)) {
			if(lastExecute > executeThreshold) {
				if (GRAVITY > -0.5f) GRAVITY = BACKUP_GRAVITY;
				else GRAVITY = 0f;
				lastExecute = 0f;
			}
		}
		
		agentPurple.getVelocity().add(0, GRAVITY);
		
		agentPurple.getVelocity().x = MathUtils.clamp(agentPurple.getVelocity().x, -agentPurple.getMaxVelocity(), agentPurple.getMaxVelocity());
		
		if(Math.abs(agentPurple.getVelocity().x) < 0.25f) {
			agentPurple.getVelocity().x = 0;
			if(agentPurple.isGrounded()) agentPurple.setState(AgentPurple.State.IDLE);
		}
		
		agentPurple.getVelocity().scl(deltaTime);
		
		// Collision detection and response
		
		// Checks the direction agent is moving and find collision boxes and compare it with agent's bounding box.
		Rectangle xTile = xCollides(agentPurple, getXTiles(agentPurple));
		if(xTile != null) agentPurple.getVelocity().x = 0;
		
		// Up down collision checking
		Rectangle yTile = yCollides(agentPurple, getYTiles(agentPurple));
		if(yTile != null) {
			if(agentPurple.getVelocity().y > 0)
				agentPurple.getPosition().y = yTile.y - AgentPurple.COLLISION_HEIGHT;
			else {
				agentPurple.getPosition().y = yTile.y + yTile.height;
				agentPurple.setGrounded(true);
			}
			agentPurple.getVelocity().y = 0;
		}
		
		// unscale velocity
		agentPurple.getPosition().add(agentPurple.getVelocity());
		agentPurple.getVelocity().scl(1 / deltaTime);
		
		agentPurple.getVelocity().x *= AgentPurple.DAMPING;
	}
	
	Array<Rectangle> getXTiles(Entity entity) {
		int startX, startY, endX, endY;
		
		if(entity.getVelocity().x > 0) {
			startX = endX = (int) (entity.getPosition().x + Entity.COLLISION_WIDTH + entity.getVelocity().x);
		} else {
			startX = endX = (int) (entity.getPosition().x + entity.getVelocity().x);
		}
		startY = (int) (entity.getPosition().y);
		endY = (int) (entity.getPosition().y + Entity.COLLISION_HEIGHT);

		getTiles(startX, startY, endX, endY, tiles);
		return tiles;
	}
	
	// Returns true if collides in the x direction
	Rectangle xCollides(Entity entity, Array<Rectangle> tiles) {
		Rectangle entityRect = rectanglePool.obtain();
		entityRect.set(entity.getPosition().x, entity.getPosition().y, Entity.COLLISION_WIDTH, Entity.COLLISION_HEIGHT);
		entityRect.x += entity.getVelocity().x;
		for(Rectangle tile : tiles) {
			if(entityRect.overlaps(tile)) {
				rectanglePool.free(entityRect);
				return tile;
			}
		}
		rectanglePool.free(entityRect);
		return null;
	}
	
	Array<Rectangle> getYTiles(Entity entity) {
		int startX, startY, endX, endY;
		
		if(entity.getVelocity().y > 0) {
			startY = endY = (int) (entity.getPosition().y + Entity.COLLISION_HEIGHT + entity.getVelocity().y);
		} else {
			startY = endY = (int) (entity.getPosition().y + entity.getVelocity().y);
		}
		startX = (int) (entity.getPosition().x);
		endX = (int) (entity.getPosition().x + Entity.COLLISION_WIDTH);
		getTiles(startX, startY, endX, endY, tiles);
		return tiles;
	}
	
	Rectangle yCollides(Entity entity, Array<Rectangle> tiles) {
		Rectangle entityRect = rectanglePool.obtain();
		entityRect.set(entity.getPosition().x, entity.getPosition().y, Entity.COLLISION_WIDTH, Entity.COLLISION_HEIGHT);
		entityRect.y += entity.getVelocity().y;
		for(Rectangle tile : tiles) {
			if(entityRect.overlaps(tile)) {
				rectanglePool.free(entityRect);
				return tile;
			}
		}
		rectanglePool.free(entityRect);
		return null;
	}
	
	private void getTiles(int startX, int startY, int endX, int endY, Array<Rectangle> tiles) {
		// Gets all tiles of layer "foreground" where all the collision boxes are supposed to be.
		TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("foreground");
		rectanglePool.freeAll(tiles);
		tiles.clear();
		for(int y = startY; y <= endY; y++) {
			for(int x = startX; x <= endX; x++) {
				Cell cell = layer.getCell(x, y);
				if(cell != null) {
					Rectangle rect = rectanglePool.obtain();
					rect.set(x, y, 1, 1);
					tiles.add(rect);
				}
			}
		}
	}

	public void render(float deltaTime) {
		TextureRegion animation = null;
		
		switch(agentPurple.getState()) {
			case IDLE:
				animation = agentPurpleIdle.getKeyFrame(agentPurple.getStateTime());
				break;
			case WALK:
				animation = agentPurpleWalk.getKeyFrame(agentPurple.getStateTime());
				break;
			case JUMP:
				animation = agentPurpleIdle.getKeyFrame(agentPurple.getStateTime());
				break;
			case DEAD:
				animation = agentPurpleIdle.getKeyFrame(agentPurple.getStateTime());
				break;
		}

		Batch batch = renderer.getBatch();
		batch.begin();
		if(agentPurple.getDirection() == AgentPurple.Direction.RIGHT) {
			batch.draw(animation, agentPurple.getDrawPosition().x, agentPurple.getDrawPosition().y, AgentPurple.DRAW_WIDTH, AgentPurple.DRAW_HEIGHT);
		} else {
			batch.draw(animation, agentPurple.getDrawPosition().x + AgentPurple.DRAW_WIDTH, agentPurple.getDrawPosition().y, -AgentPurple.DRAW_WIDTH, AgentPurple.DRAW_HEIGHT);
		}
		batch.end();
	}
	
	private void renderDebug() {
		debugRenderer.setProjectionMatrix(camera.combined);
		debugRenderer.begin(ShapeRenderer.ShapeType.Line);
		
		// Draw box
		debugRenderer.setColor(Color.RED);
		debugRenderer.rect(agentPurple.getDrawPosition().x, agentPurple.getDrawPosition().y, AgentPurple.DRAW_WIDTH, AgentPurple.DRAW_HEIGHT);
		
		// Collision box
		debugRenderer.setColor(Color.CORAL);
		debugRenderer.rect(agentPurple.getPosition().x, agentPurple.getPosition().y, AgentPurple.COLLISION_WIDTH, AgentPurple.COLLISION_HEIGHT);
		
		debugRenderer.setColor(Color.BLUE);
		TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("foreground");
		for(int y = 0; y <= layer.getHeight(); y++) {
			for(int x = 0; x <= layer.getWidth(); x++) {
				if(layer.getCell(x, y) != null) {
					if(camera.frustum.boundsInFrustum(x + 0.5f, y + 0.5f, 0, 1, 1, 0))
						debugRenderer.rect(x, y, 1, 1);
				}
			}
		}
		debugRenderer.end();
	}

	@Override
	public void dispose() {
		// TODO: Dispose first level
	}
}
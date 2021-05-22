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
	private OrthogonalTiledMapRenderer renderer;
	private OrthographicCamera camera;
	
	private Animation<TextureRegion> agentPurpleIdle;
	private Animation<TextureRegion> agentPurpleWalk;
	// TODO: Add jumping animation
	
	private AgentPurple agentPurple;
	private Pool<Rectangle> rectanglePool = new Pool<Rectangle>() {
		@Override
		protected Rectangle newObject() {
			return new Rectangle();
		}
	};
	private Array<Rectangle> tiles = new Array<>();
	
	// FIXME: Agent Purple passing through blocks. Temp set to 0.
	static final float GRAVITY = -2f;
	
	private static boolean debug = false;
	private ShapeRenderer debugRenderer;
	
	public static void debugMode(boolean bool) { debug = bool; }
	public static boolean isDebugMode() { return debug; }
	
	// YOU MUST INITIALIZE VARIABLES INSIDE CREATE()
	@Override
	public void create() {
		Texture texture = new Texture("sprites/agentPurple.png");
		TextureRegion[] textureRegion = TextureRegion.split(texture, 15, 22)[0];
		agentPurpleIdle = new Animation<>(0.5f, textureRegion[0], textureRegion[1]);
		agentPurpleWalk = new Animation<>(0.15f, textureRegion[2], textureRegion[3], textureRegion[4], textureRegion[5]);
		agentPurpleIdle.setPlayMode(Animation.PlayMode.LOOP);
		agentPurpleWalk.setPlayMode(Animation.PlayMode.LOOP);
		
		// NOTE: 1 Unit = 8 Pixels
		
		map = new TmxMapLoader().load("maps/level1.tmx");
		renderer = new OrthogonalTiledMapRenderer(map, 1 / 8f);

		agentPurple = new AgentPurple();
		agentPurple.setPosition(new Vector2(7, 7));
		
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 30, 18);
		camera.update();
		
		debugRenderer = new ShapeRenderer();
	}

	@Override
	public void render() {
		// Clears the screen
		ScreenUtils.clear(1.0f, 1.0f, 1.0f, 1);
		
		// Delta time = time between current frame and last frame in seconds.
		float deltaTime = Gdx.graphics.getDeltaTime();
		
		// Update agent purple. Process input, detect collision, update position.
		update(deltaTime);
		
		// Camera follows agent purple
		camera.position.x = agentPurple.getPosition().x;
		camera.position.y = agentPurple.getPosition().y;
		camera.update();
		
		// TiledMapRenderer based on what camera sees. Render map.
		renderer.setView(camera);
		renderer.render();
		
		// Render agent purple
		render(deltaTime);
		
		// If debug mode render rectangles.
		if(debug) renderDebug();
	}

	public void update(float deltaTime) {
		if(deltaTime == 0) return;

		if(deltaTime > 0.1f)
			deltaTime = 0.1f;

		agentPurple.setStateTime(deltaTime + agentPurple.getStateTime());

		// Checking input
		if(agentPurple.isGrounded() && (Gdx.input.isKeyPressed(Input.Keys.SPACE) || Gdx.input.isKeyPressed(Input.Keys.W))) {
			agentPurple.getVelocity().y = agentPurple.getVelocity().y + AgentPurple.JUMP_VELOCITY;
			agentPurple.setState(AgentPurple.State.JUMP);
			agentPurple.setGrounded(false);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
			agentPurple.getVelocity().x = agentPurple.getVelocity().x - AgentPurple.ACCELERATION;
			if(agentPurple.isGrounded()) agentPurple.setState(AgentPurple.State.WALK);
			agentPurple.setDirection(AgentPurple.Direction.LEFT);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
			agentPurple.getVelocity().x = agentPurple.getVelocity().x + AgentPurple.ACCELERATION;
			if(agentPurple.isGrounded()) agentPurple.setState(AgentPurple.State.WALK);
			agentPurple.setDirection(AgentPurple.Direction.RIGHT);
		}
		if(Gdx.input.isKeyPressed(Input.Keys.B)) {
			ParkourMaster.debugMode(!ParkourMaster.isDebugMode());
		}

		// Apply gravity
		agentPurple.getVelocity().add(0, ParkourMaster.GRAVITY);

		// If velocity greater than max, set to max
		agentPurple.getVelocity().x = MathUtils.clamp(agentPurple.getVelocity().x, -AgentPurple.MAX_VELOCITY, AgentPurple.MAX_VELOCITY);

		// If velocity less than 0.5, set to 0 and set state to IDLE.
		if(Math.abs(agentPurple.getVelocity().x) < 0.5f) {
			agentPurple.getVelocity().x = 0;
			if(agentPurple.isGrounded()) agentPurple.setState(AgentPurple.State.IDLE);
		}

		// Multiply by time to see how far we go.
		agentPurple.getVelocity().scl(deltaTime);
		
		// Collision detection and response
		// Checks the direction agent is moving and find collision boxes and compare it with agent's bounding box.
		// INFO: Sprite coordinate starts from bottom left, hence why we have to add the sprite's WIDTH if it is going right.
		// This is also why we're adding the sprite's height to endY.
		Rectangle agentPurpleRect = rectanglePool.obtain();
		agentPurpleRect.set(agentPurple.getPosition().x, agentPurple.getPosition().y, AgentPurple.WIDTH - 0.5f, AgentPurple.HEIGHT);
		// Bounds to check for collision boxes
		int startX, startY, endX, endY;
		if(agentPurple.getVelocity().x > 0) { // If moving right, add width, else don't
			startX = endX = (int)(agentPurple.getPosition().x + AgentPurple.WIDTH + agentPurple.getVelocity().x);
		} else {
			startX = endX = (int) (agentPurple.getPosition().x + agentPurple.getVelocity().x);
		}
		startY = (int) (agentPurple.getPosition().y);
		endY = (int) (agentPurple.getPosition().y + AgentPurple.HEIGHT);
		
		getTiles(startX, startY, endX, endY, tiles);
		agentPurpleRect.x += agentPurple.getVelocity().x;
		
		for(Rectangle tile : tiles) {
			if(agentPurpleRect.overlaps(tile)) {
				agentPurple.getVelocity().x = 0;
				break;
			}
		}
		agentPurpleRect.x = agentPurple.getPosition().x;
		
		// Up down collision checking
		if(agentPurple.getVelocity().y > 0) {
			startY = endY = (int) (agentPurple.getPosition().y + AgentPurple.HEIGHT + agentPurple.getVelocity().y);
		} else {
			startY = endY = (int) (agentPurple.getPosition().y + agentPurple.getVelocity().y);
		}
		// FIXME: What if agent purple is moving diagonally? Fix if it does not work.
		startX = (int) (agentPurple.getPosition().x);
		endX = (int) (agentPurple.getPosition().x + AgentPurple.WIDTH);
		getTiles(startX, startY, endX, endY, tiles);
		agentPurpleRect.y += agentPurple.getVelocity().y;
		for(Rectangle tile : tiles) {
			if(agentPurpleRect.overlaps(tile)) {
				// Resets position to under tile
				if(agentPurple.getVelocity().y > 0) 
					agentPurple.getPosition().y = tile.y - AgentPurple.HEIGHT;
				else {
					agentPurple.getPosition().y = tile.y + tile.height;
					agentPurple.setGrounded(true);
				}
				agentPurple.getVelocity().y = 0;
				break;
			}
		}
		rectanglePool.free(agentPurpleRect);
		
		// unscale velocity
		agentPurple.getPosition().add(agentPurple.getVelocity());
		agentPurple.getVelocity().scl(1 / deltaTime);
		
		agentPurple.getVelocity().x *= AgentPurple.DAMPING;
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
					rect.set(x, y, 1, 1); // FIXME: Adjust to 8 if its pixel size and not tile size.
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
		// FIXME: Try and flip animation...?
		if(agentPurple.getDirection() == AgentPurple.Direction.RIGHT) {
			batch.draw(animation, agentPurple.getPosition().x, agentPurple.getPosition().y, AgentPurple.WIDTH, AgentPurple.HEIGHT);
		} else {
			batch.draw(animation, agentPurple.getPosition().x + AgentPurple.WIDTH, agentPurple.getPosition().y, -AgentPurple.WIDTH, AgentPurple.HEIGHT);
		}
		batch.end();
	}
	
	private void renderDebug() {
		debugRenderer.setProjectionMatrix(camera.combined);
		debugRenderer.begin(ShapeRenderer.ShapeType.Line);
		
		debugRenderer.setColor(Color.RED);
		debugRenderer.rect(agentPurple.getPosition().x, agentPurple.getPosition().y, AgentPurple.WIDTH, AgentPurple.HEIGHT);
		
		debugRenderer.setColor(Color.BLUE);
		TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("foreground");
		for(int y = 0; y <= layer.getHeight(); y++) {
			for(int x = 0; x <= layer.getWidth(); x++) {
				if(layer.getCell(x, y) != null) {
					// FIXME...?
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
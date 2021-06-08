package io.github.kevinmeh;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
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

import java.util.ArrayList;

public class ParkourMaster extends Game {

	// DECLARE VARIABLES HERE
	private TiledMap map;
	// INFO: tiled map dimensions start from top left.
	private MapObjects objects;
	private OrthogonalTiledMapRenderer renderer;
	private OrthographicCamera camera;
	private float mapHeight;
	private float mapWidth;

	private AgentPurple agentPurple;
	private ArrayList<Slime> slimes = new ArrayList<>();
	private ArrayList<Bullet> bullets = new ArrayList<>();

	private Animation<TextureRegion> agentPurpleIdle;
	private Animation<TextureRegion> agentPurpleWalk;
	private Animation<TextureRegion> agentPurpleShoot;
	private TextureRegion agentPurpleDeadFrame;
	// TODO: Add jumping animation

	private Animation<TextureRegion> slimeWalk;
	private TextureRegion slimeDead;

	private TextureRegion bulletFrame;

	private Sound fireSound;

	private Pool<Rectangle> rectanglePool = new Pool<Rectangle>() {
		@Override
		protected Rectangle newObject() {
			return new Rectangle();
		}
	};
	private Array<Rectangle> tiles = new Array<>();

	private static boolean debug = false;
	private ShapeRenderer debugRenderer;
	
	static float GRAVITY = -78f;

	static final float TILE_SIZE = 4f;

	@Override
	public void create() {
		// NOTE: 1 Unit = 8 Pixels
		map = new TmxMapLoader().load("maps/stage1.tmx");
		renderer = new OrthogonalTiledMapRenderer(map, 1 / TILE_SIZE);
		mapWidth = (float) map.getProperties().get("width", Integer.class);
		mapHeight = (float) map.getProperties().get("height", Integer.class);
		MapLayer objectLayer = map.getLayers().get("entities");
		objects = objectLayer.getObjects();

		initialize();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, 32, 18);
		camera.update();

		debugRenderer = new ShapeRenderer();
	}

	void initialize() {
		Texture texture = new Texture("sprites/agentPurple.png");
		TextureRegion[] textureRegion = TextureRegion.split(texture, 25, 22)[0];
		agentPurpleIdle = new Animation<>(0.5f, textureRegion[0], textureRegion[1]);
		agentPurpleIdle.setPlayMode(Animation.PlayMode.LOOP);
		agentPurpleWalk = new Animation<>(0.15f, textureRegion[2], textureRegion[3], textureRegion[4], textureRegion[5]);
		agentPurpleWalk.setPlayMode(Animation.PlayMode.LOOP);
		agentPurpleShoot = new Animation<>(0.15f, textureRegion[6], textureRegion[7], textureRegion[8]);
		agentPurpleShoot.setPlayMode(Animation.PlayMode.NORMAL);
		agentPurpleDeadFrame = textureRegion[9];

		Texture slimeTexture = new Texture("sprites/slime.png");
		TextureRegion[] slimeTextureRegion = TextureRegion.split(slimeTexture, 22, 13)[0];
		slimeWalk = new Animation<>(0.4f, slimeTextureRegion[0], slimeTextureRegion[1]);
		slimeWalk.setPlayMode(Animation.PlayMode.LOOP);
		slimeDead = slimeTextureRegion[2];

		Texture bulletTexture = new Texture("sprites/bullet.png");
		bulletFrame = TextureRegion.split(bulletTexture, 4, 3)[0][0];

		fireSound = Gdx.audio.newSound(Gdx.files.internal("sounds/pistol.mp3"));

		agentPurple = new AgentPurple();
		agentPurple.setPosition(
				new Vector2(
						(Integer) objects.get("spawn").getProperties().get("x") / TILE_SIZE,
						mapHeight - (Integer) objects.get("spawn").getProperties().get("y") / TILE_SIZE
				)
		);

		// Initializes slime1 through slime4 and adds to slimes
		for(int i = 1; i < 4; i++) {
			Slime slime = new Slime();
			slime.setPosition(
					new Vector2(
							(Integer) objects.get("slime" + i).getProperties().get("x") / TILE_SIZE,
							mapHeight - (Integer) objects.get("slime" + i).getProperties().get("y") / TILE_SIZE
					)
			);
			slime.setMovementType((String) objects.get("slime" + i).getProperties().get("movementType"));
			slimes.add(slime);
		}
	}

	@Override
	public void render() {
		ScreenUtils.clear(1.0f, 1.0f, 1.0f, 1);

		float deltaTime = Gdx.graphics.getDeltaTime();

		// Input, collision, position
		updateAgent(deltaTime);
		updateSlime(deltaTime);
		updateBullet(deltaTime);
		calcDamages();

		updateCameraPosition();

		// TiledMapRenderer based on what camera sees. Render map.
		renderer.setView(camera);
		renderer.render();

		renderAgent(deltaTime);
		renderSlime(deltaTime);
		renderBullet(deltaTime);

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

		agentPurple.setTimeSinceLastDamage(deltaTime + agentPurple.getTimeSinceLastDamage());
		agentPurple.setStateTime(deltaTime + agentPurple.getStateTime());
		agentPurple.setTimeSinceLastShot(agentPurple.getTimeSinceLastShot() + deltaTime);

		lastExecute = lastExecute + deltaTime;

		if(!agentPurple.isDead()) {
			if (agentPurple.isGrounded() && (Gdx.input.isKeyPressed(Input.Keys.SPACE) || Gdx.input.isKeyPressed(Input.Keys.W))) {
				agentPurple.getVelocity().y += agentPurple.getJumpVelocity();
				agentPurple.setState(AgentPurple.State.JUMP);
				agentPurple.setGrounded(false);
			}
			if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
				agentPurple.getVelocity().x = agentPurple.getVelocity().x - agentPurple.getMaxVelocity();
				if (agentPurple.isGrounded()) agentPurple.setState(AgentPurple.State.WALK);
				agentPurple.setDirection(AgentPurple.Direction.LEFT);
			}
			if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
				agentPurple.getVelocity().x = agentPurple.getVelocity().x + agentPurple.getMaxVelocity();
				if (agentPurple.isGrounded()) agentPurple.setState(AgentPurple.State.WALK);
				agentPurple.setDirection(AgentPurple.Direction.RIGHT);
			}
			if (Gdx.input.isKeyPressed(Input.Keys.F)) {
				if (agentPurple.getTimeSinceLastShot() > agentPurple.getFireRate())
					fire();
			}
			if (Gdx.input.isKeyPressed(Input.Keys.B)) {
				if (lastExecute > executeThreshold) {
					debug = !debug;
					lastExecute = 0f;
				}
			}
		}

		agentPurple.getVelocity().add(0, GRAVITY * deltaTime);

		agentPurple.getVelocity().x = MathUtils.clamp(agentPurple.getVelocity().x, -agentPurple.getMaxVelocity(), agentPurple.getMaxVelocity());

		if(Math.abs(agentPurple.getVelocity().x) < 0.25f) {
			agentPurple.getVelocity().x = 0;
			if(agentPurple.isNotShooting() && agentPurple.isGrounded()) agentPurple.setState(AgentPurple.State.IDLE);
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
				agentPurple.getPosition().y = yTile.y - agentPurple.getCollisionHeight();
			else {
				agentPurple.getPosition().y = yTile.y + yTile.height;
				agentPurple.setGrounded(true);
			}
			agentPurple.getVelocity().y = 0;
		}

		// unscale velocity
		agentPurple.getPosition().add(agentPurple.getVelocity());
		agentPurple.getVelocity().scl(1 / deltaTime);

		agentPurple.getVelocity().x *= agentPurple.getDamping();
	}

	void fire() {
		agentPurple.setTimeSinceLastShot(0f);
		agentPurple.setState(AgentPurple.State.IDLE_SHOOT);
		agentPurple.setStateTime(0);
		Bullet bullet = new Bullet();
		if(agentPurple.getDirection() == Entity.Direction.RIGHT) {
			bullet.setDirection(Entity.Direction.RIGHT);
			bullet.setPosition(new Vector2(agentPurple.getPosition().x + 1f + agentPurple.getCollisionWidth(), agentPurple.getPosition().y + 1.37f));
			bullet.getVelocity().x = bullet.getMaxVelocity();
		} else {
			bullet.setDirection(Entity.Direction.LEFT);
			bullet.setPosition(new Vector2(agentPurple.getPosition().x - 1.5f, agentPurple.getPosition().y + 1.37f));
			bullet.getVelocity().x = -bullet.getMaxVelocity();
		}
		bullets.add(bullet);
		fireSound.play(0.2f);
	}

	void updateSlime(float deltaTime) {
		if(deltaTime == 0) return;

		if(deltaTime > 0.1f)
			deltaTime = 0.1f;

		for(Slime slime : slimes) {
			slime.setStateTime(deltaTime + slime.getStateTime());
			slime.setTimeSinceLastDamage(deltaTime + slime.getTimeSinceLastDamage());

			// Movement
			if(slime.getState() != Enemy.State.DEAD) {
				switch (slime.getMovementType()) {
					case STATIC:
						break;
					case PATROL:
					case FREE:
						// Adds / subtracts velocity based on which direction slime is going
						if (slime.getDirection() == Entity.Direction.RIGHT)
							slime.getVelocity().x += slime.getMaxVelocity();
						else
							slime.getVelocity().x -= slime.getMaxVelocity();
						if (slime.isGrounded()) slime.setState(Slime.State.WALK);
						break;
					case JUMPING:
						// Randomly jump
						if (Math.random() > 0.7 && slime.isGrounded()) {
							slime.getVelocity().y += slime.getJumpVelocity();
							slime.setState(Slime.State.JUMP);
							slime.setGrounded(false);
						}
						break;
				}
			}

			slime.getVelocity().add(0, GRAVITY * deltaTime);

			slime.getVelocity().x = MathUtils.clamp(slime.getVelocity().x, -slime.getMaxVelocity(), slime.getMaxVelocity());

			slime.getVelocity().scl(deltaTime);

			// Collision checking

			// X collision checking if PATROL or FREE
			if(slime.getMovementType() == Enemy.MovementType.PATROL || slime.getMovementType() == Enemy.MovementType.FREE) {
				Rectangle xTile = xCollides(slime, getXTiles(slime));
				if (xTile != null) {
					slime.getVelocity().x = 0;
					switch (slime.getMovementType()) {
						// If bumps into wall switch direction
						case PATROL:
							slime.switchDirection();
							break;
						case FREE:
							// If slime's x position is the same as the one half a second before, then switch direction
							if (slime.getTimer() > 0.5f && Math.abs(slime.getPosition().x - slime.getLastPosition()) < 0.001) {
								slime.switchDirection();
								slime.setTimer(0f);
							} else if (slime.isGrounded()) { // Else if it is grounded, jump
								slime.getVelocity().scl(1 / deltaTime);
								slime.getVelocity().y += slime.getJumpVelocity();
								slime.getVelocity().scl(deltaTime);
								slime.setGrounded(false);
							}
							if(slime.getTimer() < 0.01f) {
								slime.setLastPosition(slime.getPosition().x);
								slime.setTimer(slime.getTimer() + deltaTime);
							} else if(slime.getTimer() > 0.01f) {
								slime.setTimer(slime.getTimer() + deltaTime);
							}
					}
				}

				if(slime.getMovementType() == Enemy.MovementType.PATROL) {
					TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("foreground");
					Cell edgeTile;
					// Get the tile below and to the RIGHT / LEFT of slime and see if it is there
					if(slime.getDirection() == Entity.Direction.RIGHT) {
						edgeTile = layer.getCell((int) (slime.getPosition().x + slime.getCollisionWidth()), (int) slime.getPosition().y - 1);
					} else {
						edgeTile = layer.getCell((int) (slime.getPosition().x), (int) slime.getPosition().y - 1);
					}
					// If there is no tile, switch direction
					if(edgeTile == null) {
						slime.switchDirection();
					}
				}
			}

			// Y collision checking
			Rectangle yTile = yCollides(slime, getYTiles(slime));
			if(yTile != null) {
				if(slime.getVelocity().y > 0)
					slime.getPosition().y = yTile.y - slime.getCollisionHeight();
				else {
					slime.getPosition().y = yTile.y + yTile.height;
					slime.setGrounded(true);
				}
				slime.getVelocity().y = 0;
			}

			slime.getPosition().add(slime.getVelocity());
			slime.getVelocity().scl(1 / deltaTime);

			slime.getVelocity().x *= slime.getDamping();
		}
	}

	void updateBullet(float deltaTime) {
		if(deltaTime == 0) return;

		if(deltaTime > 0.1f)
			deltaTime = 0.1f;

		for(int i = 0; i < bullets.size(); i++) {
			Bullet bullet = bullets.get(i);
			bullet.setStateTime(deltaTime + bullet.getStateTime());

			bullet.getVelocity().scl(deltaTime);

			// Collision checking

			Rectangle xTile = xCollides(bullet, getXTiles(bullet));
			if(xTile != null) {
				bullet.getVelocity().x = 0;
				bullets.remove(bullet);
				i--;
			}

			bullet.getPosition().add(bullet.getVelocity());
			bullet.getVelocity().scl(1 / deltaTime);
		}
	}

	void calcDamages() {
		for(Bullet bullet : bullets) {
			for (Slime slime : slimes) {
				if (slime.getCollisionBox().overlaps(bullet.getCollisionBox()) && slime.getTimeSinceLastDamage() > 0.5f) {
					slime.setHealth(slime.getHealth() - bullet.getDamage());
				}
			}
		}
		for(Slime slime : slimes) {
			if(!slime.isDead() && slime.getCollisionBox().overlaps(agentPurple.getCollisionBox()) && agentPurple.getTimeSinceLastDamage() > 0.5f) {
				agentPurple.setHealth(agentPurple.getHealth() - slime.getDamage());
			}
		}
	}

	Array<Rectangle> getXTiles(Entity entity) {
		int startX, startY, endX, endY;

		if(entity.getVelocity().x > 0) {
			startX = endX = (int) (entity.getPosition().x + entity.getCollisionWidth() + entity.getVelocity().x);
		} else {
			startX = endX = (int) (entity.getPosition().x + entity.getVelocity().x);
		}
		startY = (int) (entity.getPosition().y);
		endY = (int) (entity.getPosition().y + entity.getCollisionHeight());

		getTiles(startX, startY, endX, endY, tiles);
		return tiles;
	}

	// Returns true if collides in the x direction
	Rectangle xCollides(Entity entity, Array<Rectangle> tiles) {
		Rectangle entityRect = rectanglePool.obtain();
		entityRect.set(entity.getPosition().x, entity.getPosition().y, entity.getCollisionWidth(), entity.getCollisionHeight());
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
			startY = endY = (int) (entity.getPosition().y + entity.getCollisionHeight() + entity.getVelocity().y);
		} else {
			startY = endY = (int) (entity.getPosition().y + entity.getVelocity().y);
		}
		startX = (int) (entity.getPosition().x);
		endX = (int) (entity.getPosition().x + entity.getCollisionWidth());
		getTiles(startX, startY, endX, endY, tiles);
		return tiles;
	}

	Rectangle yCollides(Entity entity, Array<Rectangle> tiles) {
		Rectangle entityRect = rectanglePool.obtain();
		entityRect.set(entity.getPosition().x, entity.getPosition().y, entity.getCollisionWidth(), entity.getCollisionHeight());
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

	void renderAgent(float deltaTime) {
		Batch batch = renderer.getBatch();
		TextureRegion animation = null;
		batch.begin();

		if(agentPurple.getState() == AgentPurple.State.IDLE_SHOOT) {
			if(agentPurple.getDirection() == AgentPurple.Direction.RIGHT) {
				batch.draw(agentPurpleShoot.getKeyFrame(agentPurple.getStateTime()), agentPurple.getDrawPosition().x, agentPurple.getDrawPosition().y, agentPurple.getDrawWidth(), agentPurple.getDrawHeight());
			} else {
				batch.draw(agentPurpleShoot.getKeyFrame(agentPurple.getStateTime()), agentPurple.getDrawPosition().x + agentPurple.getDrawWidth(), agentPurple.getDrawPosition().y, -agentPurple.getDrawWidth(), agentPurple.getDrawHeight());
			}
			batch.end();
			if(agentPurpleShoot.isAnimationFinished(deltaTime)) {
				agentPurple.setState(AgentPurple.State.IDLE);
			}
			return;
		}

		switch(agentPurple.getState()) {
			case IDLE:
				animation = agentPurpleIdle.getKeyFrame(agentPurple.getStateTime());
				break;
			case WALK:
				animation = agentPurpleWalk.getKeyFrame(agentPurple.getStateTime());
				break;
			case JUMP:
				animation = agentPurpleWalk.getKeyFrame(agentPurple.getStateTime());
				break;
			case DEAD:
				animation = agentPurpleDeadFrame;
				if(agentPurple.getDirection() == AgentPurple.Direction.RIGHT) {
					batch.draw(animation, agentPurple.getDrawPosition().x, agentPurple.getDrawPosition().y, agentPurple.getDrawWidth(), agentPurple.getDrawHeight());
				} else {
					batch.draw(animation, agentPurple.getDrawPosition().x, agentPurple.getDrawPosition().y, agentPurple.getDrawWidth(), agentPurple.getDrawHeight());
				}
				batch.end();
				return;
		}

		if(agentPurple.getDirection() == AgentPurple.Direction.RIGHT) {
			batch.draw(animation, agentPurple.getDrawPosition().x, agentPurple.getDrawPosition().y, agentPurple.getDrawWidth(), agentPurple.getDrawHeight());
		} else {
			batch.draw(animation, agentPurple.getDrawPosition().x + agentPurple.getDrawWidth(), agentPurple.getDrawPosition().y, -agentPurple.getDrawWidth(), agentPurple.getDrawHeight());
		}
		batch.end();
	}

	void renderSlime(float deltaTime) {
		Batch batch = renderer.getBatch();
		batch.begin();
		for(Slime slime : slimes) {
			TextureRegion animation;

			switch(slime.getState()) {
				case DEAD:
					animation = slimeDead;
					break;
				default:
					animation = slimeWalk.getKeyFrame(slime.getStateTime());
			}

			if(slime.getDirection() == Entity.Direction.RIGHT) {
				batch.draw(animation, slime.getDrawPosition().x, slime.getDrawPosition().y, slime.getDrawWidth(), slime.getDrawHeight());
			} else {
				batch.draw(animation, slime.getDrawPosition().x + slime.getDrawWidth(), slime.getDrawPosition().y, -slime.getDrawWidth(), slime.getDrawHeight());
			}
		}
		batch.end();
	}

	void renderBullet(float deltaTime) {
		Batch batch = renderer.getBatch();
		batch.begin();
		for(Bullet bullet : bullets) {
			if(bullet.getDirection() == Entity.Direction.RIGHT) {
				batch.draw(bulletFrame, bullet.getDrawPosition().x, bullet.getDrawPosition().y, bullet.getDrawWidth(), bullet.getDrawHeight());
			} else {
				batch.draw(bulletFrame, bullet.getDrawPosition().x + bullet.getDrawWidth(), bullet.getDrawPosition().y, -bullet.getDrawWidth(), bullet.getDrawHeight());
			}
		}
		batch.end();
	}

	private void renderDebug() {
		debugRenderer.setProjectionMatrix(camera.combined);
		debugRenderer.begin(ShapeRenderer.ShapeType.Line);

		// Draw box
		debugRenderer.setColor(Color.RED);
		debugRenderer.rect(agentPurple.getDrawPosition().x, agentPurple.getDrawPosition().y, agentPurple.getDrawWidth(), agentPurple.getDrawHeight());

		// Collision box
		debugRenderer.setColor(Color.CORAL);
		debugRenderer.rect(agentPurple.getPosition().x, agentPurple.getPosition().y, agentPurple.getCollisionWidth(), agentPurple.getCollisionHeight());

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
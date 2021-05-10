package io.github.kevinmeh;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.Array;

import java.awt.*;

public class ParkourMaster extends Game {
	
	// DECLARE VARIABLES HERE
	private TiledMap map;
	private OrthographicCamera camera;
	// FACING RIGHT
	private Texture agentPurpleWalk;
	private Texture agentPurpleIdle;
	// FACING RIGHT
	private Animation<Texture> idle;
	private Animation<Texture> walk;
	// TODO: Add jumping animation
	private AgentPurple agentPurple;
	private Array<Rectangle> tiles = new Array<Rectangle>();
	
	private static final float GRAVITY = -4f;
	
	// YOU MUST INITIALIZE VARIABLES INSIDE CREATE()
	@Override
	public void create() {
		agentPurpleIdle = new Texture("sprites/agentPurple-idle-right.png");
		agentPurpleWalk = new Texture("sprites/agentPurple-walk-right.png");
	}

	@Override
	public void render() {
	}
	
	// REMEMBER TO DISPOSE TO ALERT GARBAGE COLLECTOR
	@Override
	public void dispose() {

	}
}

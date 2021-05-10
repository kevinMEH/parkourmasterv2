package io.github.kevinmeh;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class AgentPurple {
    
    public enum State {
        IDLE, WALK, JUMP, DEAD
    }
    
    static final float VELOCITY = 3f; // Units per second
    static final float MAX_VELOCITY = 5f;
    static final float JUMP_VELOCITY = 1f;
    static final float HEIGHT = 2f; // Defines collision box
    static final float WIDTH = 1f;
    
    Vector2 position = new Vector2();
    Vector2 velocity = new Vector2();
    Vector2 acceleration = new Vector2();
    Rectangle bounds = new Rectangle();
    State state = State.IDLE;
    
    public AgentPurple(Vector2 position) {
        this.position = position;
        this.bounds.height = HEIGHT;
        this.bounds.width = WIDTH;
    }
    
}

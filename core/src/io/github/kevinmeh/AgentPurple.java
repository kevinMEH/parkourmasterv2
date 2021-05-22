package io.github.kevinmeh;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class AgentPurple {

    enum State { IDLE, WALK, JUMP, DEAD }
    enum Direction { RIGHT, LEFT }

    // 1 UNIT = 8 PIXELS
    final static float WIDTH = 15 / 8f;
    final static float HEIGHT = 22 / 8f; // Defines collision box
    final static float MAX_VELOCITY = 7.2f;
    final static float JUMP_VELOCITY = 30f;
    final static float DAMPING = 0.67f; // Dampening velocity

    private Vector2 position = new Vector2();
    public void setPosition(Vector2 position) { this.position = position; }
    public Vector2 getPosition() { return position; }
    
    private Vector2 velocity = new Vector2();
    public Vector2 getVelocity() { return velocity; }
    
    private State state = State.IDLE;
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }
    
    private float stateTime = 0;
    public float getStateTime() { return stateTime; }
    public void setStateTime(float stateTime) { this.stateTime = stateTime; }

    private boolean grounded = false;
    public boolean isGrounded() { return grounded; }
    public void setGrounded(boolean grounded) { this.grounded = grounded; }

    private Direction direction = Direction.RIGHT;
    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }
}

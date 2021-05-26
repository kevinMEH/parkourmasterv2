package io.github.kevinmeh;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Entity {
    
    enum Direction { RIGHT, LEFT }

    // 1 UNIT = 8 PIXELS
    // Collision box WIDTH and HEIGHT
    static float COLLISION_WIDTH;
    static float COLLISION_HEIGHT;
    // Half of difference between actual dimensions and collision dimensions.
    static float WIDTH_DIFF;
    static float HEIGHT_DIFF;
    // The height to draw at
    static float DRAW_WIDTH;
    static float DRAW_HEIGHT;
    
    static float JUMP_VELOCITY;
    static float MAX_VELOCITY;
    static float DAMPING = 0.77f;

    // Position describes the corner of the collision box.
    private Vector2 position = new Vector2();
    public void setPosition(Vector2 position) { this.position = position; }
    public Vector2 getPosition() { return position; }
    public Vector2 getDrawPosition() { return new Vector2(position.x - WIDTH_DIFF, position.y - HEIGHT_DIFF); }

    private Vector2 velocity = new Vector2();
    public Vector2 getVelocity() { return velocity; }

    private Direction direction = Direction.RIGHT;
    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }

    private boolean grounded = false;
    public boolean isGrounded() { return grounded; }
    public void setGrounded(boolean grounded) { this.grounded = grounded; }
    
    private float stateTime = 0;
    public float getStateTime() { return stateTime; }
    public void setStateTime(float stateTime) { this.stateTime = stateTime; }
}

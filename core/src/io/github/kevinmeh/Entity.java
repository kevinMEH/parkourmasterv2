package io.github.kevinmeh;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

abstract class Entity {

    abstract float getCollisionWidth();
    abstract float getCollisionHeight();
    abstract float getWidthDiff();
    abstract float getHeightDiff();
    abstract float getDrawWidth();
    abstract float getDrawHeight();
    abstract float getJumpVelocity();
    abstract float getMaxVelocity();
    abstract float getDamping();

    enum Direction { RIGHT, LEFT }
    
    public Rectangle getCollisionBox() { return new Rectangle(position.x, position.y, getCollisionWidth(), getCollisionHeight()); }

    // Position describes the corner of the collision box.
    private Vector2 position = new Vector2();
    public void setPosition(Vector2 position) { this.position = position; }
    public Vector2 getPosition() { return position; }
    public Vector2 getDrawPosition() { return new Vector2(position.x - getWidthDiff(), position.y); }

    private Vector2 velocity = new Vector2();
    public Vector2 getVelocity() { return velocity; }

    private Direction direction = Direction.RIGHT;
    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }
    public void switchDirection() {
        if(this.direction == Direction.RIGHT) this.direction = Direction.LEFT;
        else this.direction = Direction.RIGHT;
    }

    private boolean grounded = false;
    public boolean isGrounded() { return grounded; }
    public void setGrounded(boolean grounded) { this.grounded = grounded; }
    
    private float stateTime = 0;
    public float getStateTime() { return stateTime; }
    public void setStateTime(float stateTime) { this.stateTime = stateTime; }
}

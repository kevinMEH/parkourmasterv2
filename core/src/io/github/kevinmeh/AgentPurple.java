package io.github.kevinmeh;

public class AgentPurple extends Entity {
    
    public float getCollisionWidth() { return COLLISION_WIDTH; }
    public float getCollisionHeight() { return COLLISION_HEIGHT; }
    public float getWidthDiff() { return WIDTH_DIFF; }
    public float getHeightDiff() { return HEIGHT_DIFF; }
    public float getDrawWidth() { return DRAW_WIDTH; }
    public float getDrawHeight() { return DRAW_HEIGHT; }
    public float getJumpVelocity() { return JUMP_VELOCITY; }
    public float getMaxVelocity() { return MAX_VELOCITY; }
    public float getDamping() { return DAMPING; }

    // Collision box WIDTH and HEIGHT
    private static float COLLISION_WIDTH;
    private static float COLLISION_HEIGHT;
    // Half of difference between actual dimensions and collision dimensions.
    private static float WIDTH_DIFF;
    private static float HEIGHT_DIFF;
    // The height to draw at
    private static float DRAW_WIDTH;
    private static float DRAW_HEIGHT;

    private static float JUMP_VELOCITY;
    private static float MAX_VELOCITY;
    private static float DAMPING = 0.77f;
    
    static {
        COLLISION_WIDTH = 6.5f / (ParkourMaster.TILE_SIZE * 2);
        COLLISION_HEIGHT = 21f / (ParkourMaster.TILE_SIZE * 2);
        
        DRAW_WIDTH = 25f / (ParkourMaster.TILE_SIZE * 2);
        DRAW_HEIGHT = 22f / (ParkourMaster.TILE_SIZE * 2);
        
        WIDTH_DIFF = (DRAW_WIDTH - COLLISION_WIDTH) / 2;
        HEIGHT_DIFF = 0f;

        JUMP_VELOCITY = 27f;
        MAX_VELOCITY = 12f;
    }
    
    enum State { IDLE, WALK, JUMP, DEAD, IDLE_SHOOT, WALK_SHOOT, JUMP_SHOOT }

    private State state = State.IDLE;
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }
    
}

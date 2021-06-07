package io.github.kevinmeh;

public class Bullet extends Entity {

    float getCollisionWidth() { return COLLISION_WIDTH; }
    float getCollisionHeight() { return COLLISION_HEIGHT; }
    float getWidthDiff() { return WIDTH_DIFF; }
    float getHeightDiff() { return HEIGHT_DIFF; }
    float getDrawWidth() { return DRAW_WIDTH; }
    float getDrawHeight() { return DRAW_HEIGHT; }
    float getJumpVelocity() { return JUMP_VELOCITY; }
    float getMaxVelocity() { return MAX_VELOCITY; }
    float getDamping() { return DAMPING; }

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
        COLLISION_WIDTH = 4f / (ParkourMaster.TILE_SIZE * 2);
        COLLISION_HEIGHT = 3f / (ParkourMaster.TILE_SIZE * 2);

        DRAW_WIDTH = 4f / (ParkourMaster.TILE_SIZE * 2);
        DRAW_HEIGHT = 3f / (ParkourMaster.TILE_SIZE * 2);

        WIDTH_DIFF = (DRAW_WIDTH - COLLISION_WIDTH) / 2;
        HEIGHT_DIFF = 0f;

        JUMP_VELOCITY = 0f;
        MAX_VELOCITY = 72f;
    }
    
    {
        this.setDamage(120);
        this.setHealthBasic(1000);
    }

    @Override
    public void setHealth(int health) {
        // Bullet has no health
    }
}

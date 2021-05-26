package io.github.kevinmeh;

public class Slime extends Entity {
    
    static {
        COLLISION_WIDTH = 20 / (ParkourMaster.TILE_SIZE * 2);
        COLLISION_HEIGHT = 10 / (ParkourMaster.TILE_SIZE * 2);
        
        DRAW_WIDTH = 22 / (ParkourMaster.TILE_SIZE * 2);
        DRAW_HEIGHT = 13 / (ParkourMaster.TILE_SIZE * 2);
        
        WIDTH_DIFF = (DRAW_WIDTH - COLLISION_WIDTH) / 2;
        HEIGHT_DIFF = 0f;
        
        JUMP_VELOCITY = 18f;
        MAX_VELOCITY = 6f;
    }
    
    enum State { IDLE, WALK, JUMP, DEAD}
    
}
package io.github.kevinmeh;

public class AgentPurple extends Entity {
    
    static {
        COLLISION_WIDTH = 6.5f / (ParkourMaster.TILE_SIZE * 2);
        COLLISION_HEIGHT = 21 / (ParkourMaster.TILE_SIZE * 2);
        
        DRAW_WIDTH = 25 / (ParkourMaster.TILE_SIZE * 2);
        DRAW_HEIGHT = 22 / (ParkourMaster.TILE_SIZE * 2);
        
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

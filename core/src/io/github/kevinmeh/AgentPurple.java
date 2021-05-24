package io.github.kevinmeh;

public class AgentPurple extends Entity {
    
    static {
        COLLISION_WIDTH = 8 / 8f;
        COLLISION_HEIGHT = 22 / 8f;
        
        DRAW_WIDTH = 15 / 8f;
        DRAW_HEIGHT = 22 / 8f;
        
        WIDTH_DIFF = (DRAW_WIDTH - COLLISION_WIDTH) / 2;
        HEIGHT_DIFF = (DRAW_HEIGHT - COLLISION_HEIGHT) / 2;
        
        MAX_VELOCITY = 8f;
    }

    final static float JUMP_VELOCITY = 35f;
    final static float DAMPING = 0.77f; // Dampening velocity

    enum State { IDLE, WALK, JUMP, DEAD }

    private State state = State.IDLE;
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }

    private float stateTime = 0;
    public float getStateTime() { return stateTime; }
    public void setStateTime(float stateTime) { this.stateTime = stateTime; }

    private boolean grounded = false;
    public boolean isGrounded() { return grounded; }
    public void setGrounded(boolean grounded) { this.grounded = grounded; }
    
}

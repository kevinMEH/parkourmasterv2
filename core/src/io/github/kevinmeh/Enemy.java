package io.github.kevinmeh;

public class Enemy extends Entity {
    
    // Movement type of slime.
    // STATIC: does not move around
    // JUMPING: does not move in the x direction but jumps
    // PATROL: walks back and forth without changing elevation
    // FREE: walks and jumps freely around the map
    // CHASE: chases agent around the map
    enum MovementType { STATIC, JUMPING, PATROL, FREE, CHASE }
    private MovementType movementType;
    public MovementType getMovementType() { return movementType; }
    public void setMovementType(String movementType) {
        if(movementType.equalsIgnoreCase("static")) this.movementType = MovementType.STATIC;
        else if(movementType.equalsIgnoreCase("jumping")) this.movementType = MovementType.JUMPING;
        else if(movementType.equalsIgnoreCase("patrol")) this.movementType = MovementType.PATROL;
        else if(movementType.equalsIgnoreCase("free")) this.movementType = MovementType.FREE;
        else if(movementType.equalsIgnoreCase("chase")) this.movementType = MovementType.CHASE;
        else System.out.println("ERROR: Movement type " + movementType + " not found!");
    }
    
}

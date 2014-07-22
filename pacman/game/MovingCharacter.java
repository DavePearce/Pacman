// This file is part of the Multi-player Pacman Game.
//
// Pacman is free software; you can redistribute it and/or modify 
// it under the terms of the GNU General Public License as published 
// by the Free Software Foundation; either version 3 of the License, 
// or (at your option) any later version.
//
// Pacman is distributed in the hope that it will be useful, but 
// WITHOUT ANY WARRANTY; without even the implied warranty of 
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See 
// the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public 
// License along with Pacman. If not, see <http://www.gnu.org/licenses/>
//
// Copyright 2010, David James Pearce. 

package pacman.game;

/**
 * The moving character class represents characters on the pacman board which
 * move. Moving characters have a direction of movement, and a speed at which
 * they are moving.
 * 
 * @author djp
 * 
 */
public abstract class MovingCharacter extends Character {
	// Direction constants
	public static final int STOPPED = 0;
	public static final int UP = 1;
	public static final int DOWN = 2;
	public static final int RIGHT = 3;
	public static final int LEFT = 4;
	
	protected int direction;
	protected int queued; // queued direction change
	
	public MovingCharacter(int realX, int realY, int direction) {
		super(realX,realY);
		this.direction = direction;
	}

	/**
	 * Determine the direction in which this character is moving.
	 */
	public int direction() {
		return direction;
	}


	public void moveUp() {
		queued = UP;
	}
	
	public void moveDown() {
		queued = DOWN;
	}
	
	public void moveLeft() {
		queued = LEFT;
	}
	
	public void moveRight() {
		queued = RIGHT;
	}
	
	/**
	 * The tick method is provided to enable computer control characters to
	 * make decisions.
	 */
	public void tick(Board game) {
		// The following are used to determine the point at which a character
		// can change direction (if a change is queued).
		boolean xready = realX % 30 == 0;
		boolean yready = realY % 30 == 0;
		
		if(xready && yready) {
			// yes, can accept direction change
			direction = queued; 
		}
		
		// Attempt to update the character's position. This is done by
		// speculating at the new board position and then deciding if this
		// should be allowed or not.			
		int nRealX = realX;
		int nRealY = realY;
		int nx,ny;
		int speed = speed();
		int width = game.width();		
		
		if(direction == MovingCharacter.UP) {
			nRealY -= speed;
			ny = nRealY / 30;
			nx = (nRealX+15)/30;
		} else if(direction == MovingCharacter.DOWN) {
			nRealY += speed;
			ny = (nRealY / 30);
			if(nRealY % 30 != 0) { ny++; }
			nx = (nRealX+15)/30;
		} else if(direction == MovingCharacter.RIGHT) {
			nRealX = (nRealX + speed) % (width*30);
			nx = nRealX / 30;
			if(nRealX % 30 != 0) { nx++; }
			ny = (nRealY+15)/30;
		} else if(direction == MovingCharacter.LEFT) {
			nRealX = (nRealX - speed) % (width*30);				
			nx = nRealX / 30;
			ny = (nRealY+15)/30;				
		} else {				
			return;
		}
		
		if(nx < 0) {
			nx += width;
			nRealX += 30 * width;
		} else if(nx >= width) {
			nRealX -= 30 * width;
			nx -= width;
		}
		
		if(game.isWall(nx,ny)) {
			// we've bumped into a wall ... so we have to stop!!
			direction = MovingCharacter.STOPPED;
		} else {
			// we can update our position ...
			realX = nRealX;
			realY = nRealY;
		}
	}
	
	/**
	 * Determine the speed at which this character moves
	 */
	abstract public int speed();
}

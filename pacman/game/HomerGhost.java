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

import java.util.*;
import java.awt.Graphics;
import java.awt.Image;
import java.io.*;

import pacman.ui.Board;

import static pacman.ui.BoardCanvas.*;

/**
 * Homer Ghosts are controlled by the computer. They look to see where the
 * nearest pacman is, and move towards it. If their route is blocked, then they
 * make the best available option.
 * 
 * @author djp
 * 
 */
public final class HomerGhost extends MovingCharacter implements Ghost {
	protected static final Random random = new Random(System.currentTimeMillis());
	
	public HomerGhost(int realX, int realY) {
		super(realX,realY,MovingCharacter.STOPPED);
	}
	
	public void tick(Board game) {
		super.tick(game);
		
		// check whether we are at an intersection.
		if (direction == MovingCharacter.DOWN
				|| direction == MovingCharacter.UP) {
			// ok, moving in up/down direction
			if (!game.canMoveLeft(this) && !game.canMoveRight(this)) {
				return; // no horizontal movement possible
			}
		} else if (direction == MovingCharacter.RIGHT
				|| direction == MovingCharacter.LEFT) {
			// ok, moving in left/right direction
			if (!game.canMoveUp(this) && !game.canMoveDown(this)) {
				return; // no horizontal movement possible
			}
		}
		
		// yes, we're at an intersection. Now, flip a coin to see if we're
		// really homing or going to move randomly. This is kinda important, as
		// otherwise having multiple homing ghosts just means they all act in
		// exactly the same manner.		
		if(random.nextInt(10) > 7) {
			queued = random.nextInt(4)+1; // don't stop
			return;
		}
		
		double targetDistance = 10000;
		int targetDeltaX=-1;
		int targetDeltaY=-1;		
		
		// home in on target
		synchronized(game) {
			for(Character c : game.characters()) {
				if(c instanceof Pacman && !((Pacman)c).isDead()) {
					// potential target
					int deltaX = Math.abs(c.realX() - realX);
					int deltaY = Math.abs(c.realY() - realY);
					double distance = Math.sqrt((deltaX*deltaX) + (deltaY*deltaY));
					if(distance < targetDistance) {					
						targetDeltaX = c.realX() - realX;
						targetDeltaY = c.realY() - realY;					
						targetDistance = distance;
					}
				}
			}
		}
		
		if(targetDeltaX != -1) {			
			int deltaX = Math.abs(targetDeltaX);
			int deltaY = Math.abs(targetDeltaY);
			if(deltaX < deltaY) {
				// prefer to move north-south
				if(targetDeltaY < 0) {
					tryMoveUp(targetDeltaX < 0, game);
				} else {					
					tryMoveDown(targetDeltaX < 0, game);
				}
			} else {
				// prefer to move east-west
				if(targetDeltaX < 0) {
					tryMoveLeft(targetDeltaY < 0, game);
				} else {
					tryMoveRight(targetDeltaY < 0, game);
				}
			}
		}				
	}
	
	public void tryMoveUp(boolean preferLeft, Board game) {		
		if(game.canMoveUp(this)) {
			moveUp();
		} else if(preferLeft && game.canMoveLeft(this)) {			
			moveLeft();
		} else if(!preferLeft && game.canMoveRight(this)) {
			moveRight();
		} else if(game.canMoveRight(this)) {
			moveRight();
		} else if(game.canMoveLeft(this)) {
			moveLeft();
		} else {
			moveDown(); // last resort
		}
	}
	
	public void tryMoveDown(boolean preferLeft, Board game) {		
		if(game.canMoveDown(this)) {
			moveDown();
		} else if(preferLeft && game.canMoveLeft(this)) {			
			moveLeft();
		} else if(!preferLeft && game.canMoveRight(this)) {
			moveRight();
		} else if(preferLeft && game.canMoveRight(this)) {
			moveRight();
		} else if(!preferLeft && game.canMoveLeft(this)) {
			moveLeft();
		} else {
			moveUp(); // last resort
		}		
	}

	public void tryMoveLeft(boolean preferUp, Board game) {		
		if(game.canMoveLeft(this)) {
			moveLeft();
		} else if(preferUp && game.canMoveUp(this)) {			
			moveUp();
		} else if(!preferUp && game.canMoveDown(this)) {
			moveDown();
		} else if(game.canMoveUp(this)) {
			moveUp();
		} else if(game.canMoveDown(this)) {
			moveDown();
		} else {
			moveRight(); // last resort
		}	
	}

	public void tryMoveRight(boolean preferUp, Board game) {		
		if(game.canMoveRight(this)) {
			moveRight();
		} else if(preferUp && game.canMoveUp(this)) {			
			moveUp();
		} else if(!preferUp && game.canMoveDown(this)) {
			moveDown();
		} else if(game.canMoveUp(this)) {
			moveUp();
		} else if(game.canMoveDown(this)) {
			moveDown();
		} else {
			moveLeft(); // last resort
		}	
	}
	
	public int speed() {
		return 3;
	}
	
	public void toOutputStream(DataOutputStream dout) throws IOException {		
		dout.writeByte(Character.HOMERGHOST);
		dout.writeShort(realX);
		dout.writeShort(realY);	
		dout.writeByte(direction);
	}
	
	/**
	 * Read a homing ghost from the input stream
	 * 
	 * @param rx
	 * @param ry
	 * @param din
	 * @return
	 */
	public static HomerGhost fromInputStream(int rx, int ry, DataInputStream din)
			throws IOException {
		int dir = din.readByte();
		HomerGhost r = new HomerGhost(rx, ry);
		r.direction = dir;
		return r;
	}
	
	public void draw(Graphics g) {
		switch(direction) {
			case MovingCharacter.RIGHT:
				g.drawImage(HGHOST_RIGHT, realX,realY, null, null);
				break;
			case MovingCharacter.UP:
				g.drawImage(HGHOST_UP, realX,realY, null, null);
				break;
			case MovingCharacter.DOWN:
				g.drawImage(HGHOST_DOWN, realX,realY, null, null);
				break;
			case MovingCharacter.LEFT:
				g.drawImage(HGHOST_LEFT, realX,realY, null, null);
				break;
		}
	}
	
	private static final Image HGHOST_RIGHT = loadImage("hghostright.png");
	private static final Image HGHOST_LEFT = loadImage("hghostleft.png");
	private static final Image HGHOST_UP = loadImage("hghostup.png");
	private static final Image HGHOST_DOWN = loadImage("hghostdown.png");	
}

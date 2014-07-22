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

import java.awt.Graphics;
import java.awt.Image;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

import static pacman.game.BoardCanvas.*;

/**
 * Random Ghosts are controlled by the computer. They basically just walk in a
 * pretty arbitrary direction.
 * 
 * @author djp
 * 
 */
public final class RandomGhost extends MovingCharacter implements Ghost {
	protected static final Random random = new Random(System.currentTimeMillis());
	
	public RandomGhost(int realX, int realY) {
		super(realX,realY,MovingCharacter.STOPPED);		
	}
	
	public void tick(Board game) {
		super.tick(game);
		
		// check whether we are at an intersection.
		if (direction == MovingCharacter.DOWN || direction == MovingCharacter.UP) {
			// ok, moving in up/down direction
			if (!game.canMoveLeft(this) && !game.canMoveRight(this)) {
				return; // no horizontal movement possible
			}
		} else if (direction == MovingCharacter.RIGHT || direction == MovingCharacter.LEFT) {
			// ok, moving in left/right direction
			if (!game.canMoveUp(this) && !game.canMoveDown(this)) {
				return; // no horizontal movement possible
			}
		}
		
		queued = random.nextInt(4)+1; // don't stop
	}
		
	public int speed() {
		return 3;
	}
	
	public void toOutputStream(DataOutputStream dout) throws IOException {		
		dout.writeByte(Character.RANDOMGHOST);
		dout.writeShort(realX);
		dout.writeShort(realY);
		dout.writeByte(direction);
	}	
	
	public static RandomGhost fromInputStream(int rx, int ry,
			DataInputStream din) throws IOException {
		int dir = din.readByte();
		RandomGhost r = new RandomGhost(rx, ry);
		r.direction = dir;
		return r;		
	}
	
	public void draw(Graphics g) {
		switch(direction) {
			case MovingCharacter.RIGHT:
				g.drawImage(RGHOST_RIGHT, realX,realY, null, null);
				break;
			case MovingCharacter.UP:
				g.drawImage(RGHOST_UP, realX,realY, null, null);
				break;
			case MovingCharacter.DOWN:
				g.drawImage(RGHOST_DOWN, realX,realY, null, null);
				break;
			case MovingCharacter.LEFT:
				g.drawImage(RGHOST_LEFT, realX,realY, null, null);
				break;
		}
	}
	
	private static final Image RGHOST_RIGHT = loadImage("rghostright.png");
	private static final Image RGHOST_LEFT = loadImage("rghostleft.png");
	private static final Image RGHOST_UP = loadImage("rghostup.png");
	private static final Image RGHOST_DOWN = loadImage("rghostdown.png");	
}

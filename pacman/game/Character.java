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

import java.io.*;
import java.awt.*;

import pacman.ui.Board;

/**
 * A Character is a record of information about a particular character in the
 * game. There are essentially two kinds of characters: player controlled and
 * computer controlled.
 * 
 * @author djp
 * 
 */
public abstract class Character {
	protected int realX; // real x-position
	protected int realY; // real y-position	
	
	public Character(int realX, int realY) {
		this.realX = realX;
		this.realY = realY;		
	}
		
	public int realX() {
		return realX;
	}
	
	public int realY() {
		return realY;
	}

	/**
	 * The following method is provided to allow characters to take actions on
	 * every clock tick; for example, ghosts may choose new directions to move
	 * in.
	 * 
	 * @param game
	 */
	public abstract void tick(Board game);

	/**
	 * This method enables characters to draw themselves onto a given canvas.
	 * 
	 * @param g
	 */
	public abstract void draw(Graphics g);
	
	/**
	 * The following method is provided to simplify the process of writing a
	 * given character to the output stream.
	 * 
	 * @param dout
	 */
	public abstract void toOutputStream(DataOutputStream dout) throws IOException;
	
	// Character type constants
	public static final int ENTERING = 0;
	public static final int LEAVING = 1;
	public static final int HOMERGHOST = 2;
	public static final int RANDOMGHOST = 3;
	public static final int PACMAN = 4;
	public static final int DISAPPEAR = 5;		
	
	/**
	 * The following constructs a character given a byte array.
	 * 
	 * @param bytes
	 * @return
	 */
	public static Character fromInputStream(DataInputStream din) throws IOException {
		int type = din.readByte();
		int rx = din.readShort();
		int ry = din.readShort();
		
		if(type == Character.PACMAN) {
			return Pacman.fromInputStream(rx,ry,din);
		} else if(type == Character.HOMERGHOST) {
			return HomerGhost.fromInputStream(rx,ry,din);
		} else if(type == Character.RANDOMGHOST) {
			return RandomGhost.fromInputStream(rx,ry,din);
		} else if(type == Character.DISAPPEAR) {
			return Disappear.fromInputStream(rx,ry,din);
		} else {
			throw new IllegalArgumentException("Unrecognised character type: " + type);
		}
	}
	
}

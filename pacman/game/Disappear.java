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
import java.io.*;

import pacman.ui.Board;

import static pacman.ui.BoardCanvas.*;

public final class Disappear extends Character {
	private int state;
	
	public Disappear(int realX, int realY, int state) {
		super(realX, realY);		
	}
	
	public int state() {
		return state;
	}
	
	
	public void tick(Board game) {				
		if (state >= 7) {
			game.removeCharacter(this);
		} else {
			state = state + 1;
		}
	}
	
	public static Disappear fromInputStream(int rx, int ry, DataInputStream din)
			throws IOException {
		byte state = din.readByte();
		return new Disappear(rx,ry,state);
	}
	
	public void toOutputStream(DataOutputStream dout) throws IOException {
		dout.writeByte(Character.DISAPPEAR);
		dout.writeShort(realX);
		dout.writeShort(realY);
		dout.writeByte(state);		
	}
	
	public void draw(Graphics g) {
		g.drawImage(DISCONNECTS[state], realX, realY,
				null, null);
	}				

	private static final Image[] DISCONNECTS = {
		loadImage("disconnect1.png"),
		loadImage("disconnect1.png"),
		loadImage("disconnect2.png"),
		loadImage("disconnect2.png"),
		loadImage("disconnect3.png"),
		loadImage("disconnect3.png"),
		loadImage("disconnect4.png"),
		loadImage("disconnect4.png")
	};		
}

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
// UPDATED 03/06/2010, Terrence Miller

package pacman.game;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import pacman.ui.Board;

import static pacman.ui.BoardCanvas.*;

public final class Pacman extends MovingCharacter {	
	private final int uid;
	private int score;
	private int state; // 0 means alive, anything means dead or dying.
	private int lives;
	
	public Pacman(int realX, int realY, int dir, int uid, int lives, int score) {
		super(realX,realY,dir);
		this.score = score;
		this.uid = uid;
		this.lives = lives;
	}

	/**
	 * Get this players unique identifier.
	 * 
	 * @return
	 */
	public int uid() {
		return uid;
	}
	
	/**
	 * Get this players score.
	 * 
	 * @return
	 */
	public int score() {
		return score;
	}

	/**
	 * Get this players remaining lives
	 * @return
	 */
	public int lives() {
		return lives;
	}
	
	public int speed() {
		return 5;
	}
	
	/**
	 * Mark this pacman as dying.
	 */
	public void markAsDying() {
		state = 1;		
	}

	/**
	 * Check whether this pacman is dying.
	 * @return
	 */
	public boolean isDying() {
		return state != 0 && lives > 0;
	}
	
	/**
	 * Check if this pacman is dead.
	 * 
	 * @return
	 */
	public boolean isDead() {
		return lives == 0 && state != 0;
	}
	
	/**
	 * Add to this players score.
	 * 
	 * @param delta
	 */
	public void addScore(int delta) {
		score += delta;
	}
	
	public void tick(Board game) {	
		if(state == 0) {
			// only do stuff if we're not dead.
			super.tick(game);
			int x = (realX+15)/30;
			int y = (realY+15)/30;
			if(game.isPill(x,y)) {
				// eat the pill			
				score += 10;
				game.eatPill(x,y);			
			}
		} else if(state < 20) {
			state = state + 1;
		} else if(lives > 0) {		
			state = 0;				
			int[] portal = game.respawnPacman();
			realX = portal[0] * 30;
			realY = portal[1] * 30;			
			lives = lives - 1;
		}
	}
	
	public void toOutputStream(DataOutputStream dout) throws IOException {		
		dout.writeByte(Character.PACMAN);
		dout.writeShort(realX);
		dout.writeShort(realY);
		dout.writeByte(uid);
		dout.writeByte(direction);
		dout.writeByte(state);
		dout.writeByte(lives);
		dout.writeShort(score);
	}
		
	/**
	 * Construct a pacman player from an input stream.
	 * 
	 * @param rx
	 * @param ry
	 * @param din
	 * @return
	 * @throws IOException
	 */
	public static Pacman fromInputStream(int rx, int ry,
			DataInputStream din) throws IOException {
		int uid = din.readByte();
		int dir = din.readByte();
		int state = din.readByte();
		int lives = din.readByte();
		int score = din.readShort();
		Pacman p = new Pacman(rx, ry, dir, uid, lives, score);		
		p.state = state;		
		return p;
	}
	
	/**
	 * Draw the pacman to the screen
	 */
	public void draw(Graphics g) {
		if(state == 0) {
			switch(direction) {
			case MovingCharacter.UP:
				g.drawImage(PACMAN2_UP[Math.abs(realY % 30)/ 4], realX,realY, null, null);
				break;
			case MovingCharacter.DOWN:				
				g.drawImage(PACMAN2_DOWN[Math.abs(realY % 30)/ 4], realX,realY, null, null);
				break;
			case MovingCharacter.RIGHT:
				g.drawImage(PACMAN2_RIGHT[Math.abs(realX % 30)/ 4], realX,realY, null, null);
				break;
			case MovingCharacter.LEFT:					
				g.drawImage(PACMAN2_LEFT[Math.abs(realX % 30)/ 4], realX,realY, null, null);
				break;
			default:
				// stopped
				g.drawImage(PACMAN2_DYING[0], realX,realY, null, null);
			}
		} else if(state > 19) {
			// do nothing
		} else {
			g.drawImage(PACMAN2_DYING[(state-1)/3],realX,realY,null,null);
		}
	}
	
	/**
	 * Draw the pacman that is yours to the screen different so you know which one is you
	 */
	public void drawOwn(Graphics g) {
		if(state == 0) {
			switch(direction) {
			case MovingCharacter.UP:
				g.drawImage(PACMAN_UP[Math.abs(realY % 30)/ 4], realX,realY, null, null);
				break;
			case MovingCharacter.DOWN:				
				g.drawImage(PACMAN_DOWN[Math.abs(realY % 30)/ 4], realX,realY, null, null);
				break;
			case MovingCharacter.RIGHT:
				g.drawImage(PACMAN_RIGHT[Math.abs(realX % 30)/ 4], realX,realY, null, null);
				break;
			case MovingCharacter.LEFT:					
				g.drawImage(PACMAN_LEFT[Math.abs(realX % 30)/ 4], realX,realY, null, null);
				break;
			default:
				// stopped
				g.drawImage(PACMAN_DYING[0], realX,realY, null, null);
			}
		} else if(state > 19) {
			// do nothing
		} else {
			g.drawImage(PACMAN_DYING[(state-1)/3],realX,realY,null,null);
		}
	}
	
	// following is public so can be access from BoardCanvas
	public static final Image[] PACMAN_RIGHT = { loadImage("pacman2.png"),
			loadImage("pacman2.png"), loadImage("pacman3.png"),
			loadImage("pacman3.png"), loadImage("pacman4.png"),
			loadImage("pacman4.png"), loadImage("pacman3.png"),
			loadImage("pacman3.png") };

	private static final Image[] PACMAN_LEFT = {rotate(PACMAN_RIGHT[0], 180),
		rotate(PACMAN_RIGHT[1], 180), rotate(PACMAN_RIGHT[2], 180),
		rotate(PACMAN_RIGHT[3], 180), rotate(PACMAN_RIGHT[4], 180),
		rotate(PACMAN_RIGHT[5], 180), rotate(PACMAN_RIGHT[6], 180)};
	
	private static final Image[] PACMAN_UP = {rotate(PACMAN_RIGHT[0], -90),
		rotate(PACMAN_RIGHT[1], -90), rotate(PACMAN_RIGHT[2], -90),
		rotate(PACMAN_RIGHT[3], -90), rotate(PACMAN_RIGHT[4], -90),
		rotate(PACMAN_RIGHT[5], -90), rotate(PACMAN_RIGHT[6], -90)};
	
	private static final Image[] PACMAN_DOWN = {rotate(PACMAN_RIGHT[0], 90),
		rotate(PACMAN_RIGHT[1], 90), rotate(PACMAN_RIGHT[2], 90),
		rotate(PACMAN_RIGHT[3], 90), rotate(PACMAN_RIGHT[4], 90),
		rotate(PACMAN_RIGHT[5], 90), rotate(PACMAN_RIGHT[6], 90)};
	
	private static final Image[] PACMAN_DYING = {
		loadImage("pacman1.png"),
		loadImage("pacman2.png"),
		loadImage("pacman3.png"),
		loadImage("pacman4.png"),
		loadImage("pacman5.png"),
		loadImage("pacman6.png"),
		loadImage("pacman7.png"),
	};
	
	
public static final Image[] PACMAN2_RIGHT = { loadImage("pacman22.png"),
		loadImage("pacman22.png"), loadImage("pacman23.png"),
		loadImage("pacman23.png"), loadImage("pacman24.png"),
		loadImage("pacman24.png"), loadImage("pacman23.png"),
		loadImage("pacman23.png") };

private static final Image[] PACMAN2_LEFT = {rotate(PACMAN2_RIGHT[0], 180),
	rotate(PACMAN2_RIGHT[1], 180), rotate(PACMAN2_RIGHT[2], 180),
	rotate(PACMAN2_RIGHT[3], 180), rotate(PACMAN2_RIGHT[4], 180),
	rotate(PACMAN2_RIGHT[5], 180), rotate(PACMAN2_RIGHT[6], 180)};

private static final Image[] PACMAN2_UP = {rotate(PACMAN2_RIGHT[0], -90),
	rotate(PACMAN2_RIGHT[1], -90), rotate(PACMAN2_RIGHT[2], -90),
	rotate(PACMAN2_RIGHT[3], -90), rotate(PACMAN2_RIGHT[4], -90),
	rotate(PACMAN2_RIGHT[5], -90), rotate(PACMAN2_RIGHT[6], -90)};

private static final Image[] PACMAN2_DOWN = {rotate(PACMAN2_RIGHT[0], 90),
	rotate(PACMAN2_RIGHT[1], 90), rotate(PACMAN2_RIGHT[2], 90),
	rotate(PACMAN2_RIGHT[3], 90), rotate(PACMAN2_RIGHT[4], 90),
	rotate(PACMAN2_RIGHT[5], 90), rotate(PACMAN2_RIGHT[6], 90)};

private static final Image[] PACMAN2_DYING = {
	loadImage("pacman21.png"),
	loadImage("pacman22.png"),
	loadImage("pacman23.png"),
	loadImage("pacman24.png"),
	loadImage("pacman25.png"),
	loadImage("pacman26.png"),
	loadImage("pacman27.png"),
};
}

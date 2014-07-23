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

package pacman.ui;

import java.util.*;
import java.io.*;

import pacman.game.Character;
import pacman.game.Disappear;
import pacman.game.Ghost;
import pacman.game.HomerGhost;
import pacman.game.MovingCharacter;
import pacman.game.Pacman;
import pacman.game.RandomGhost;

/**
 * The board class represents the pacman game board. This class is used by the
 * Character threads to move, and generally interact with each other.
 * 
 * @author djp
 * 
 */
public class Board {
	private final int width;
	private final int height;	
	
	public static final int WAITING = 0;
	public static final int READY = 1;
	public static final int PLAYING = 2;
	public static final int GAMEOVER = 3;
	public static final int GAMEWON = 4;
	
	private int state; // this is used to tell us what state we're in. 
	private int nPillsRemaining; // this is used to count the number of remaining pills

	/**
	 * The following stores the locations in the grid of all walls. It is
	 * effectively implemented as a 2D grid of bits, where each bit represents a
	 * wall.
	 */	
	private BitSet walls;
	
	/**
	 * The following stores the locations in the grid of all pills. It is
	 * effectively implemented as a 2D grid of bits, where each bit represents a
	 * wall.
	 */	
	private BitSet pills;
		
	/**
	 * The following is a list of one dimension integer arrays, which are
	 * guaranteed to be of length 2. The first element gives the x-component of
	 * the portal, the second component gives the y-component.
	 */
	private final ArrayList<int[]> pacmanPortals = new ArrayList<int[]>();	
	private int nextPacPortal = 0; // identify the next portal to be used

	/**
	 * The following is a list of one dimension integer arrays, which are
	 * guaranteed to be of length 2. The first element gives the x-component of
	 * the portal, the second component gives the y-component.
	 */
	private final ArrayList<int[]> ghostPortals = new ArrayList<int[]>();	
	private int nextGhostPortal = 0; // identify the next portal to be used

	/**
	 * The following is a list of the characters in the game. This includes
	 * pacmen, ghosts and other misc things.
	 */
	private final ArrayList<Character> characters = new ArrayList<Character>();
	
	public Board(int width, int height) {		
		this.width = width;
		this.height = height;
		this.walls = new BitSet();
		this.pills = new BitSet();
	}

	/**
	 * Get the board width.
	 * @return
	 */
	public int width() {
		return width;
	}
	
	/**
	 * Get the board height.
	 * @return
	 */
	public int height() {
		return height;
	}

	/**
	 * The following constants determine the possible fixed-object types.
	 */
	public final static int NOUT = 0;
	public final static int WALL = 1;	
	
	public boolean isPill(int x, int y) {
		return pills.get(x + (y*width));
	}
	
	public void addPill(int x, int y) {
		nPillsRemaining++;
		pills.set(x + (y*width));
	}
	
	public void eatPill(int x, int y) {
		nPillsRemaining--;
		pills.clear(x + (y*width));	
	}
	
	public boolean isWall(int x, int y) {
		return walls.get(x + (y*width));	
	}
	
	public void addWall(int x, int y) {
		walls.set(x + (y*width));	
	}
		
	/**
	 * The UID is a unique identifier for all characters in the game. This is
	 * required in order to synchronise the movements of different players
	 * across boards.
	 */
	private static int uid = 0;

	/**
	 * Register a new pacman portal on the board. A pacman portal is a place
	 * where pacman will appear from.
	 * 
	 * @param x
	 * @param y
	 */
	public synchronized void registerPacPortal(int x, int y) {
		pacmanPortals.add(new int[]{x,y});
	}

	/**
	 * Register a new pacman into the game. The Pacman will be placed onto the
	 * next available portal.
	 * 
	 * @return
	 */
	public synchronized int registerPacman() {
		int[] portal = pacmanPortals.get(nextPacPortal);
		nextPacPortal = (nextPacPortal + 1) % pacmanPortals.size();
		Character r = new Pacman(portal[0] * 30, portal[1] * 30,
				MovingCharacter.STOPPED, ++uid, 3, 0);
		characters.add(r);
		return uid;
	}

	/**
	 * A pre-registered pacman has died, but wants to come back to life.
	 * Therefore, allocate it the next available pacman portal.
	 * 
	 * @return
	 */
	public int[] respawnPacman() {
		int[] portal = pacmanPortals.get(nextPacPortal);
		nextPacPortal = (nextPacPortal + 1) % pacmanPortals.size();
		return portal;
	}
	
	/**
	 * Register a new ghost portal on the board. A ghost portal is a place
	 * where ghosts will appear from.
	 * 
	 * @param x
	 * @param y
	 */
	public synchronized void registerGhostPortal(int x, int y) {
		ghostPortals.add(new int[]{x,y});
	}
	
	/**
	 * Register a new ghost into the game. The ghost will be placed into the
	 * next available ghost portal.
	 * 
	 * @param homer --- is this a homing ghost or not?
	 */
	public synchronized void registerGhost(boolean homer) {
		int[] portal = ghostPortals.get(nextGhostPortal);
		nextGhostPortal = (nextGhostPortal + 1) % ghostPortals.size();
		Character r;
		if(homer) {
			r = new HomerGhost(portal[0]*30,portal[1]*30);
		} else {
			r = new RandomGhost(portal[0]*30,portal[1]*30);
		}
		characters.add(r);
	}
	
	public synchronized void removeCharacter(Character character) {
		for(int i=0;i!=characters.size();++i) {
			Character p = characters.get(i);
			if(character == p) {
				// NOTE: we can't call remove here, since this results in a
				// concurrent modification exception, as this method will be
				// called indirectly from the clockTick() method.
				characters.set(i,null);
				return;
			}
		}
	}
	
	public synchronized void disconnectPlayer(int uid) {		
		for(int i=0;i!=characters.size();++i) {
			Character p = characters.get(i);
			if (p instanceof Pacman && ((Pacman) p).uid() == uid) {
				characters.set(i, new Disappear(p.realX(), p.realY(),0));
			}
		}		
	}
	
	public synchronized Pacman player(int uid) {
		for(Character p : characters) {
			if (p instanceof Pacman && ((Pacman) p).uid() == uid) {
				return (Pacman) p;
			}			
		}
		throw new IllegalArgumentException("Invalid Character UID");
	}
	
	/**
	 * Iterate the characters in the game
	 * @return
	 */
	public List<Character> characters() {
		return characters;
	}
	
	/**
	 * Get current board state.
	 * @return
	 */
	public int state() {
		return state;
	}
	
	/**
	 * Set the board state.
	 * @param state
	 */
	public void setState(int state) {
		this.state = state;
	}
	
	public boolean canMoveUp(MovingCharacter p) {
		int realX = p.realX();
		int realY = p.realY();
		realY -= p.speed();
		int ny = realY / 30;
		int nx = (realX+15)/30;
		return !isWall(nx,ny); 
	}

	public boolean canMoveDown(MovingCharacter p) {
		int realX = p.realX();
		int realY = p.realY();
		realY += 5;
		int ny = realY / 30;
		if(realY % 30 != 0) { ny++; }
		int nx = (realX+15)/30;
		return !isWall(nx,ny); 
	}
	
	public boolean canMoveLeft(MovingCharacter p) {
		int realX = p.realX();
		int realY = p.realY();
		realX -= 5;
		int nx = realX / 30;
		int ny = (realY+15)/30;
		return !isWall(nx,ny); 
	}
	
	public boolean canMoveRight(MovingCharacter p) {
		int realX = p.realX();
		int realY = p.realY();
		realX += 5;
		int nx = realX / 30;
		if(realX % 30 != 0) { nx++; }
		int ny = (realY+15)/30;
		return !isWall(nx,ny); 
	}
	/**
	 * The clock tick is essentially a clock trigger, which allows the board to
	 * update the current state. The frequency with which this is called
	 * determines the rate at which the game state is updated.
	 * 
	 * @return
	 */
	public synchronized void clockTick() {
		if (state != PLAYING && state != GAMEOVER) {
			return; // do nothing unless the game is active.
		}
		
		ArrayList<Character> ghosts = new ArrayList<Character>();
		
		int nplayers = 0;
		for(int i=0;i!=characters.size();++i) {
			Character p = characters.get(i);			
			p.tick(this);
			
			// reread p, since it might be gone now ...
			p = characters.get(i);
			if(p == null) {
				// dead character encountered, remove it.
				characters.remove(i--);
				continue;				
			} 
			if (p instanceof Ghost) {					
				ghosts.add(p);				
			}			
		}
		
		// Now, perform collision detection to see if any PACMEN have collided
		// with ghosts.
		for (int i = 0; i != characters.size(); ++i) {
			Character c = characters.get(i);
			int px = (c.realX() + 15) / 30;
			int py = (c.realY() + 15) / 30;
			if (c instanceof Pacman) {
				Pacman p = (Pacman) c;
				if(!p.isDead()) { nplayers++; }
				if(p.isDead() || p.isDying()) { continue; }				
				for (Character g : ghosts) {
					int gx = (g.realX() + 15) / 30;
					int gy = (g.realY() + 15) / 30;
					if (px == gx && py == gy) {
						// pacman and ghost have collided ...
						// So, replace pacman with disappearing character
						p.markAsDying();
					}
				}
			}
		}
		
		if (nplayers == 0) {
			state = GAMEOVER;
		} else if(nPillsRemaining == 0) {
			state = GAMEWON;
		}
	}
	
	/**
	 * The following method accepts a byte array representing the state of a
	 * pacman board; this state will be broadcast by a master connection, and is
	 * then used to overwrite the current state (since it should be more up to
	 * date).
	 * 
	 * @param bytes
	 */
	public synchronized void fromByteArray(byte[] bytes) throws IOException {		
		ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
		DataInputStream din = new DataInputStream(bin);

		state = din.readByte();
		// Second, update pills
		int bitwidth = width%8 == 0 ? width : width+8;
		int bitsize = (bitwidth/8)*height;
		byte[] pillBytes = new byte[bitsize];
		din.read(pillBytes);
		pills = bitsFromByteArray(pillBytes); 
		nPillsRemaining = pills.cardinality();
				
		// Third, update characters
		int ncharacters = din.readInt();
		characters.clear();
		for(int i=0;i!=ncharacters;++i) {						
			characters.add(Character.fromInputStream(din));
		}
	}

	/**
	 * The following method accepts a byte array representation of the board
	 * walls. This is broadcast by a master connection when the connection is
	 * established.
	 * 
	 * @param bytes
	 */
	public synchronized void wallsFromByteArray(byte[] bytes) {
		walls = bitsFromByteArray(bytes); 		
	}
	
	/**
	 * Read a bit set from a byte array.
	 */
	private static BitSet bitsFromByteArray(byte[] bytes) {
		BitSet bits = new BitSet();
		for (int i = 0; i < bytes.length * 8; i++) {
			int offset = i >> 3;
			if ((bytes[offset] & (1 << (i % 8))) > 0) {
				bits.set(i);
			}
		}
		return bits;
	}
		
	/**
	 * The following method converts the current state of the board into a byte
	 * array, such that it can be shipped across a connection to an awaiting
	 * client.
	 * 
	 * @return
	 */
	public synchronized byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(bout);
		
		dout.writeByte(state);
		
		// First, write output locations of remaining pills
		int bitwidth = width%8 == 0 ? width : width+8;
		int bitsize = (bitwidth/8)*height;
		byte[] pillBytes = new byte[bitsize];
		bitsToByteArray(pills,pillBytes);
		dout.write(pillBytes);
		

		dout.writeInt(characters.size());
		for(Character p : characters) {			
			p.toOutputStream(dout);			
		}
		
		dout.flush();
		
		// Finally, return!!
		return bout.toByteArray(); 
	}

	/**
	 * The following method generates a byte array representation of the walls
	 * in the board. This is broadcast by a master connection when that
	 * connection is established.
	 * 
	 * @return
	 */
	public synchronized byte[] wallsToByteArray() {
		// First, write output locations of remaining pills
		int bitwidth = width%8 == 0 ? width : width+8;
		int bitsize = (bitwidth/8)*height;
		byte[] wallBytes = new byte[bitsize];
		bitsToByteArray(walls,wallBytes);
		return wallBytes; 		
	}
	
	/**
	 * Create a byte array from a bit set
	 */
	private static byte[] bitsToByteArray(BitSet bits, byte[] bytes) {		
		for (int i=0; i<bits.length(); i++) {
			int offset = i >> 3;
			if (bits.get(i) && offset < bytes.length) {				
				bytes[offset] |= 1<<(i%8);
			}
		}
		return bytes;
	}	
}

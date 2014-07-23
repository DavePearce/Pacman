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

package pacman;

import java.util.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import pacman.control.*;
import pacman.game.*;
import pacman.ui.Board;
import pacman.ui.BoardFrame;

public class Main {
	private static final int DEFAULT_CLK_PERIOD = 20;
	private static final int DEFAULT_BROADCAST_CLK_PERIOD = 5;
	
	public static void main(String[] args) {
		// ======================================================
		// ======== First, parse command-line arguments ========
		// ======================================================		
		String filename = null;
		boolean server = false;
		int nclients = 0;		
		String url = null;		
		int gameClock = DEFAULT_CLK_PERIOD;
		int broadcastClock = DEFAULT_BROADCAST_CLK_PERIOD;
		int port = 32768; // default
		int nHomerGhosts = 2;
		int nRandomGhosts = 2;				
		
		for (int i = 0; i != args.length; ++i) {
			if (args[i].startsWith("-")) {
				String arg = args[i];
				if(arg.equals("-help")) {
					usage();
					System.exit(0);
				} else if(arg.equals("-server")) {
					server = true;
					nclients = Integer.parseInt(args[++i]);
				} else if(arg.equals("-connect")) {
					url = args[++i];
				} else if(arg.equals("-clock")) {
					gameClock = Integer.parseInt(args[++i]);
				} else if(arg.equals("-port")) {
					port = Integer.parseInt(args[++i]);
				} else if(arg.equals("-nhoming")) {
					nHomerGhosts = Integer.parseInt(args[++i]);
				} else if(arg.equals("-nrandom")) {
					nRandomGhosts = Integer.parseInt(args[++i]);
				}
			} else {
				filename = args[i];
			}
		}
		
		// Sanity checks
		if(url != null && server) {
			System.out.println("Cannot be a server and connect to another server!");
			System.exit(1);
		} else if(url != null && gameClock != DEFAULT_CLK_PERIOD) {
			System.out.println("Cannot overide clock period when connecting to server.");
			System.exit(1);
		} else if(url == null && filename == null) {
			System.out.println("Board file must be provided for single user, or server mode.");
			System.exit(1);
		}
		
		try {
			if(server) {
				// Run in Server mode
				Board board = createBoardFromFile(filename,nHomerGhosts,nRandomGhosts);
				runServer(port,nclients,gameClock,broadcastClock, board);			
			} else if(url != null) {
				// Run in client mode
				runClient(url,port);
			} else {			
				// single user game
				Board board = createBoardFromFile(filename,nHomerGhosts,nRandomGhosts);
				singleUserGame(gameClock, board);							
			}
		} catch(IOException ioe) {			
			System.out.println("I/O error: " + ioe.getMessage());
			ioe.printStackTrace();
			System.exit(1);
		}
		
		System.exit(0);
	}
	
	private static void usage() {		
		String[][] info = {		
				{"server <n>", "Run in server mode, awaiting n client connections"},
				{"connect <url>", "Connect to server at <url>"},
				{"clock", "Set clock period (default 20ms)"},
				{"bclock","Set broadcast clock period (default 5ms)"},
				{"port", "Set port for use for connection (default 32768)"},
				{"nhoming <n>","Set the number of \"homing\" ghosts"},
				{"nrandom <n>","Set the number of \"random walking\" ghosts"}				
		};
		System.out.println("Usage: java com.pacman.Main <options> ");
		System.out.println("Options:");

		// first, work out gap information
		int gap = 0;

		for (String[] p : info) {
			gap = Math.max(gap, p[0].length() + 5);
		}

		// now, print the information
		for (String[] p : info) {
			System.out.print("  -" + p[0]);
			int rest = gap - p[0].length();
			for (int i = 0; i != rest; ++i) {
				System.out.print(" ");
			}
			System.out.println(p[1]);
		}
	}
	
	private static void runClient(String addr, int port) throws IOException {		
		Socket s = new Socket(addr,port);
		System.out.println("PACMAN CLIENT CONNECTED TO " + addr + ":" + port);			
		new Slave(s).run();		
	}
	
	private static void runServer(int port, int nclients, int gameClock, int broadcastClock, Board game) {		
		ClockThread clk = new ClockThread(gameClock,game,null);	
		
		// Listen for connections
		System.out.println("PACMAN SERVER LISTENING ON PORT " + port);
		System.out.println("PACMAN SERVER AWAITING " + nclients + " CLIENTS");
		try {
			Master[] connections = new Master[nclients];
			// Now, we await connections.
			ServerSocket ss = new ServerSocket(port);			
			while (1 == 1) {
				// 	Wait for a socket
				Socket s = ss.accept();
				System.out.println("ACCEPTED CONNECTION FROM: " + s.getInetAddress());				
				int uid = game.registerPacman();
				connections[--nclients] = new Master(s,uid,broadcastClock,game);
				connections[nclients].start();				
				if(nclients == 0) {
					System.out.println("ALL CLIENTS ACCEPTED --- GAME BEGINS");
					multiUserGame(clk,game,connections);
					System.out.println("ALL CLIENTS DISCONNECTED --- GAME OVER");
					return; // done
				}
			}
		} catch(IOException e) {
			System.err.println("I/O error: " + e.getMessage());
		} 
	}

	/**
	 * The following method controls a multi-user game. When a given game is
	 * over, it will simply restart the game with whatever players are
	 * remaining. However, if all players have disconnected then it will stop.
	 * 
	 * @param clk
	 * @param game
	 * @param connections
	 * @throws IOException
	 */
	private static void multiUserGame(ClockThread clk, Board game,
			Master... connections) throws IOException {
		// save initial state of board, so we can reset it.
		byte[] state = game.toByteArray();		
	
		clk.start(); // start the clock ticking!!!				
		
		// loop forever
		while(atleastOneConnection(connections)) {
			game.setState(Board.READY);
			pause(3000);
			game.setState(Board.PLAYING);
			// now, wait for the game to finish
			while(game.state() == Board.PLAYING) {
				Thread.yield();
			}
			// If we get here, then we're in game over mode
			pause(3000);
			// Reset board state
			game.setState(Board.WAITING);
			game.fromByteArray(state);			
		}
	}

	/**
	 * Check whether or not there is at least one connection alive.
	 * 
	 * @param connections
	 * @return
	 */
	private static boolean atleastOneConnection(Master... connections) {
		for (Master m : connections) {
			if (m.isAlive()) {
				return true;
			}			
		}
		return false;
	}
	
	private static void singleUserGame(int gameClock, Board game) throws IOException {
		int playerID = game.registerPacman();
		BoardFrame display = new BoardFrame("Pacman (single-user)", game,
				playerID, new Player(playerID, game));
		ClockThread clk = new ClockThread(gameClock,game,display);		
		// save initial state of board, so we can reset it.
		byte[] state = game.toByteArray();
		
		clk.start(); // start the clock ticking!!!		
		
		while(display.isVisible()) {
			// keep going until the frame becomes invisible
			game.setState(Board.READY);
			pause(3000);
			game.setState(Board.PLAYING);
			// now, wait for the game to finish
			while(game.state() == Board.PLAYING) {
				Thread.yield();
			}
			// If we get here, then we're in game over mode
			pause(3000);
			// Reset board state
			game.fromByteArray(state);
		}
	}
	
	private static Board createBoardFromFile(String filename, int nHomerGhosts,
			int nRandomGhosts) throws IOException {		
		FileReader fr = new FileReader(filename);		
		BufferedReader br = new BufferedReader(fr);
		ArrayList<String> lines = new ArrayList<String>();
		int width = -1;
		String line;		
		while((line = br.readLine()) != null) {
			lines.add(line);
			
			// now sanity check
			
			if(width == -1) {				
				width = line.length();
			} else if(width != line.length()) {				
				throw new IllegalArgumentException("Input file \"" + filename + "\" is malformed; line " + lines.size() + " incorrect width.");
			}			
		}
		
		Board board = new Board(width,lines.size());
		for(int y=0;y!=lines.size();++y) {
			line = lines.get(y);
			for(int x=0;x!=width;++x) {
				char c = line.charAt(x);
				switch (c) {
					case 'W' :
						board.addWall(x, y);
						break;
					case 'P':
						board.addPill(x, y);
						break;
					case 'X':
						board.registerPacPortal(x, y);
						break;
					case 'G':
						board.registerGhostPortal(x,y);
						break;					
				}
			}
		}
		
		for(int i=0;i!=nHomerGhosts;++i) {
			board.registerGhost(true);
		}
		for(int i=0;i!=nRandomGhosts;++i) {		
			board.registerGhost(false);
		}
			
		return board;
	}
	
	private static void pause(int delay) {
		try {
			Thread.sleep(delay);
		} catch(InterruptedException e){			
		}
	}
	
	// The following two bits of code are a bit sneaky, but they help make the
	// problems more visible.
	static {
		System.setProperty("sun.awt.exception.handler", "pacman.Main");
	}
	
	public void handle(Throwable ex) {
		try {
			ex.printStackTrace();
			System.exit(1); } 
		catch(Throwable t) {}
	}
}

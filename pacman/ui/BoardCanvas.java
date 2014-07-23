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

package pacman.ui;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.awt.*;
import java.awt.image.*;

import javax.imageio.ImageIO;

import pacman.game.Character;
import pacman.game.Pacman;

/**
 * The board canvas is responsible for drawing the game. Currently, it uses a
 * relatively primitive form of double buffering to ensure there's no flicker
 * during frame updates. This class also generates a number of images using
 * Java's graphics capabilities, which saves having to have lots of very similar
 * images for the different directions.
 * 
 * @author djp
 * 
 */
public class BoardCanvas extends Canvas {
	private static final String IMAGE_PATH = "images/";
	
	private static final Image WALL_S = loadImage("walls.png"); 
	private static final Image WALL_E = loadImage("walle.png");
	private static final Image WALL_C = loadImage("wallc.png");
	private static final Image WALL_T = loadImage("wallt.png");
	private static final Image WALL_O = loadImage("wallo.png");
	private static final Image WALL_X = loadImage("wallx.png");
	private static final Image PILL = loadImage("pill.png");
	
	private static final Image[] WALL_IMGS = {
		rotate(WALL_O,90),
		rotate(WALL_E,180), // 1 -   |   |   | U
		WALL_E,             // 2 -   |   | D | 
		WALL_S,             // 3 -   |   | D | U
		rotate(WALL_E,90),  // 4 -   | L |   | 
		rotate(WALL_C,180), // 5 -   | L |   | U
		rotate(WALL_C,90),  // 6 -   | L | D |  
		rotate(WALL_T,180), // 7 -   | L | D | U
		rotate(WALL_E,-90), // 8 - R |   |   |  
		rotate(WALL_C,-90), // 9 - R |   |   | U
		WALL_C,             // 0 - R |   | D |  
		WALL_T,             // 1 - R |   | D | U
		rotate(WALL_S,90),  // 2 - R | L |   |  
		rotate(WALL_T,-90), // 3 - R | L |   | U
		rotate(WALL_T,90),  // 4 - R | L | D |
		WALL_X              // 5 - R | L | D | U 
	};
		
	private static final String[] preferredFonts = {"Courier New","Arial","Times New Roman"};
	private Font font;	
	private final int uid;
	private final Board gameBoard;			
	
	public BoardCanvas(int uid, Board gameBoard) {
		this.gameBoard = gameBoard;
		this.uid = uid;
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();		
		HashSet<String> availableNames = new HashSet();

		for(String name : env.getAvailableFontFamilyNames()) {
			availableNames.add(name);			
		}

		for(String pf : preferredFonts) {
			if(availableNames.contains(pf)) {				
				font = new Font(pf,Font.BOLD,24);
				break;
			}
		}		
		setSize(new Dimension(gameBoard.width()*30,(gameBoard.height()*30) + 30));		
	}
	
	public void paint(Graphics g) {
		int width = gameBoard.width();
		int height = gameBoard.height();		
		
		// First, draw the board
		
		for(int x=0;x!=width;++x) {
			for(int y=0;y!=height;++y) {				
				if(gameBoard.isWall(x, y)) {
					// do nothing
				} else if(gameBoard.isPill(x,y)) {
					g.drawImage(PILL, x*30,y*30, null, null);	
				} else {
					g.setColor(Color.BLACK);
					g.fillRect(x*30,y*30,30,30);
				}
			}	
		}
		
		// Second, draw the characters
		int score = 0;
		int nlives = 0;
		synchronized(gameBoard){
			for(Character p : gameBoard.characters()) {				
				
				if(p instanceof Pacman) {
					Pacman pm = (Pacman) p;
					if(pm.uid() == uid) {
						pm.drawOwn(g);
						score = pm.score();
						nlives = pm.lives();
					}else{p.draw(g);}
				}
				else{p.draw(g);}
			}
		}

		
		// finally, draw any messages
		switch(gameBoard.state()) {
		case Board.WAITING:		
			drawMessage("Waiting",g);
			break;
		case Board.READY:		
			drawMessage("Ready",g);
			break;
		case Board.GAMEOVER:
			drawMessage("Game Over",g);
			break;
		case Board.GAMEWON:
			drawGameWone(g);
		}
		
		drawScore("SCORE: " + score,g);
		drawLives(nlives,g);
	}
	
	private static final String[] trails = {"st","nd","rd","th","th","th","th","th","th","th"};
	
	public void drawGameWone(Graphics g) {
		int myScore = gameBoard.player(uid).score();
		int nAbove = 0;
		int nBelow = 0;
		
		synchronized(gameBoard){
			for(Character c : gameBoard.characters()) {
				if(c instanceof Pacman) {
					Pacman p = (Pacman) c;
					if(p.score() < myScore) {
						nBelow++;
					} else if(p.score() > myScore) {
						nAbove++;
					}
				}
			}
		}
			
		if(nAbove == 0 && nBelow == 0) {
			// single user game
			drawMessage("You Won!",g);
		} else {
			drawMessage("You came: " + (nAbove+1) + trails[(nAbove)%10],g);
		}
	}
	
	private void drawLives(int nlives, Graphics g) {
		for(int i=0;i!=nlives;++i) {
			int rx = (gameBoard.width() - i - 1) * 30;
			g.drawImage(Pacman.PACMAN_RIGHT[2],rx,gameBoard.height()*30,null,null);
		}
	}
	
	private void drawScore(String score, Graphics g) {
		g.setFont(font);
		FontMetrics metrics = g.getFontMetrics();
		int ascent = metrics.getAscent();
		int width = gameBoard.width();
		int height = gameBoard.height();		
		int y = (height*30);
		g.setColor(Color.BLACK);
		g.fillRect(0,y, width*30, 30);		
		char[] chars = score.toCharArray();
		g.setColor(Color.YELLOW);
		g.drawChars(chars,0,chars.length,5,y+ascent);
	}
	
	private void drawMessage(String msg, Graphics g) {
		g.setFont(font);
		int width = gameBoard.width();
		int height = gameBoard.height();
		FontMetrics metrics = g.getFontMetrics();
		int ascent = metrics.getAscent();
		char[] chars = msg.toCharArray();
		int msgWidth = metrics.charsWidth(msg.toCharArray(),0,msg.length());
		int msgHeight = metrics.getHeight();
		int boxWidth = msgWidth + 30;
		int boxHeight = msgHeight + 30;
		int x = ((width*30) - boxWidth) / 2;
		int y = ((height*30) - boxHeight) / 2;
		g.setColor(Color.DARK_GRAY);
		g.fillRect(x,y, boxWidth, boxHeight);
		g.setColor(Color.yellow);
		g.drawRect(x,y, boxWidth, boxHeight);
		g.drawRect(x+1,y+1, boxWidth-2, boxHeight-2);
		g.drawChars(chars,0,chars.length,x+15,y+15+ascent);
		offscreen = null; // reset offscreen, since we want to get rid of the
							// message
	}
	
	
	private Image offscreen = null;
	public void update(Graphics g) {	
		if(offscreen == null) {
			initialiseOffscreen();			
		} 
		Image localOffscreen = offscreen;
		Graphics offgc = offscreen.getGraphics();		
		// do normal redraw
		paint(offgc);
		// transfer offscreen to window
		g.drawImage(localOffscreen, 0, 0, this);
	}

	private void initialiseOffscreen() {
		Dimension d = size();
		offscreen = createImage(d.width, d.height);
		// clear the exposed area
		Graphics offgc = offscreen.getGraphics();
		offgc.setColor(getBackground());
		offgc.fillRect(0, 0, d.width, d.height);
		offgc.setColor(getForeground());
		
		int width = gameBoard.width();
		int height = gameBoard.height();		
		
		// First, draw the board
		
		for(int x=0;x!=width;++x) {
			for(int y=0;y!=height;++y) {
				if(gameBoard.isWall(x, y)) {
					drawWall(x,y,offgc);
				}
			}
		}
		
	}
	
	private void drawWall(int x, int y, Graphics g) {
		boolean above = (y-1) >= 0 && gameBoard.isWall(x, y-1);
		boolean below = (y+1) < gameBoard.height() && gameBoard.isWall(x, y+1);
		boolean left = (x-1) >= 0 && gameBoard.isWall(x-1, y);
		boolean right = (x+1) < gameBoard.width() && gameBoard.isWall(x+1, y);
		
		int mask = 0;
		
		if(above) { mask |= 1; }
		if(below) { mask |= 2; }
		if(left) { mask |= 4; }
		if(right) { mask |= 8; }
		
		g.drawImage(WALL_IMGS[mask], x*30,y*30, null, null);		
	}
	
	/**
	 * Rotate an image a given number of degrees.
	 * @param src
	 * @param angle
	 * @return
	 */
	public static Image rotate(Image src, double angle) {
		int width = src.getWidth(null);
		int height = src.getHeight(null);		
		BufferedImage img = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.rotate(Math.toRadians(angle), width/2, height/2);		
		g.drawImage(src,0,0,width,height,null);
		g.dispose();
		return img;
	}
	
	/**
	 * Load an image from the file system, using a given filename.
	 * 
	 * @param filename
	 * @return
	 */
	public static Image loadImage(String filename) {
		// using the URL means the image loads when stored
		// in a jar or expanded into individual files.
		java.net.URL imageURL = BoardCanvas.class.getResource(IMAGE_PATH
				+ filename);

		try {
			Image img = ImageIO.read(imageURL);
			return img;
		} catch (IOException e) {
			// we've encountered an error loading the image. There's not much we
			// can actually do at this point, except to abort the game.
			throw new RuntimeException("Unable to load image: " + filename);
		}
	}
}

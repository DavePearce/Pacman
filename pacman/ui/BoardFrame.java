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

import java.awt.*;
import java.awt.event.*;
import javax.swing.JFrame;

import pacman.game.*;

public class BoardFrame extends JFrame {
	private final BoardCanvas canvas;
	public BoardFrame(String title, Board game, int uid, KeyListener... keys) {
		super(title);		
				
		canvas = new BoardCanvas(uid,game);				
		setLayout(new BorderLayout());
		for(KeyListener k : keys) {
			canvas.addKeyListener(k);
		}		
		add(canvas, BorderLayout.CENTER);		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);						

		// Center window in screen
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension scrnsize = toolkit.getScreenSize();
		setBounds((scrnsize.width - getWidth()) / 2,
				(scrnsize.height - getHeight()) / 2, getWidth(), getHeight());
		pack();
		setResizable(false);						
	
		// Display window
		setVisible(true);		
		canvas.requestFocus();
	}
	
	public void repaint() {
		canvas.repaint();
	}		
}

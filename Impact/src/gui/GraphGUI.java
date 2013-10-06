/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOUSE. See the GNU
 * General Public License for more details.
 *
 * You should have recieved a copy of the GNU General Public License
 * along with this program; if not write to the Free Software
 * Foundation, inc., 59 Temple Place, Suite 330, Boston MA 02111-1307
 * USA
 */

package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy
 */

public class GraphGUI extends JFrame {
	Graph graph = new Graph();

	public GraphGUI() {
		try {
			setTitle(graph.ver);
			getContentPane().setLayout(new BorderLayout());
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			setBounds(50, 50, screenSize.width - 100, screenSize.height - 100);
			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
			});
			getContentPane().add(graph, BorderLayout.CENTER);
			show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new GraphGUI();
	}

}

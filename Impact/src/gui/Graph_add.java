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

import java.awt.*;
import javax.swing.*;

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy
 */
public class Graph_add extends JPanel {
	String[] st_result = { "DISPLACEMENTS (I)", "DISPLACEMENTS (X)",
			"DISPLACEMENTS (Y)", "DISPLACEMENTS (Z)", "STRAINS (I)",
			"STRAINS (X)", "STRAINS (Y)", "STRAINS (Z)", "STRAINS (XY)",
			"STRAINS (YZ)", "STRAINS (XZ)", "STRESSES (I)", "STRESSES (X)",
			"STRESSES (Y)", "STRESSES (Z)", "STRESSES (XY)", "STRESSES (YZ)",
			"STRESSES (XZ)" };
	BorderLayout borderLayout1 = new BorderLayout();
	JPanel jPanel1 = new JPanel();
	JPanel jPanel2 = new JPanel();
	GridLayout gridLayout1 = new GridLayout();
	GridLayout gridLayout2 = new GridLayout();
	JLabel jLabel1 = new JLabel();
	JLabel jLabel2 = new JLabel();
	JLabel jLabel3 = new JLabel();
	JLabel Time = new JLabel();
	JComboBox AxisY = new JComboBox(st_result);
	JTextField FromTo = new JTextField();

	public Graph_add() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		this.setLayout(borderLayout1);
		jPanel1.setLayout(gridLayout1);
		jPanel2.setLayout(gridLayout2);
		gridLayout1.setRows(3);
		gridLayout2.setRows(3);
		jLabel1.setText("Axis X");
		jLabel2.setText("Axis Y");
		jLabel3.setText("Nodes / Elements");
		Time.setText("Time");
		this.add(jPanel1, BorderLayout.WEST);
		jPanel1.add(jLabel1, null);
		jPanel1.add(jLabel2, null);
		jPanel1.add(jLabel3, null);
		this.add(jPanel2, BorderLayout.CENTER);
		jPanel2.add(Time, null);
		jPanel2.add(AxisY, null);
		jPanel2.add(FromTo, null);
	}
}
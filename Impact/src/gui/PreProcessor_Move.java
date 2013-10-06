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
import j3d.*;
import java.awt.event.*;

public class PreProcessor_Move extends JPanel {
	Canvas3D J3D;
	PreProcessor PreP;
	BorderLayout borderLayout1 = new BorderLayout();
	JPanel jPanel1 = new JPanel();
	GridLayout gridLayout1 = new GridLayout();
	JTextField x1 = new JTextField();
	JPanel jPanel2 = new JPanel();
	JButton jButton1 = new JButton();
	JTextField y1 = new JTextField();
	JTextField z1 = new JTextField();
	JLabel jLabel5 = new JLabel("", JLabel.CENTER);
	JLabel jLabel6 = new JLabel("", JLabel.CENTER);
	JLabel jLabel7 = new JLabel("", JLabel.CENTER);
	private JLabel jLabel1 = new JLabel();

	public PreProcessor_Move(Canvas3D j3d, PreProcessor p) {
		try {
			J3D = j3d;
			PreP = p;
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		this.setLayout(borderLayout1);
		jPanel1.setLayout(gridLayout1);
		gridLayout1.setRows(2);
		gridLayout1.setColumns(3);
		jButton1.setText("Move");
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton1_actionPerformed(e);
			}
		});
		jLabel5.setText("dX");
		jLabel6.setText("dY");
		jLabel7.setText("dZ");
		jLabel1.setForeground(Color.blue);
		jLabel1.setText("Move");
		x1.setText("0");
		x1.setColumns(5);
		y1.setText("0");
		y1.setColumns(5);
		z1.setText("0");
		z1.setColumns(5);
		this.add(jPanel1, BorderLayout.CENTER);
		jPanel1.add(jLabel5, null);
		jPanel1.add(jLabel6, null);
		jPanel1.add(jLabel7, null);
		jPanel1.add(x1, null);
		jPanel1.add(y1, null);
		jPanel1.add(z1, null);
		this.add(jPanel2, BorderLayout.SOUTH);
		jPanel2.add(jButton1, null);
		this.add(jLabel1, BorderLayout.NORTH);
	}

	void jButton1_actionPerformed(ActionEvent e) {
		try {
			Matrix3D m = new Matrix3D();
			m.translate(Float.parseFloat(x1.getText()),
					Float.parseFloat(y1.getText()),
					Float.parseFloat(z1.getText()));
			J3D.transform3D(m);
		} catch (Exception e1) {
			error(e1);
		}
	}

	public void error(Object st) {
		JOptionPane.showMessageDialog(this, "Error: " + st, "Error!",
				JOptionPane.ERROR_MESSAGE);
		if (st instanceof Exception)
			((Exception) st).printStackTrace();
		else
			System.out.println("Error: " + st);
	}

}

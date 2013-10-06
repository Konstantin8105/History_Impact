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

public class PreProcessor_Scale extends JPanel {
	Canvas3D J3D;
	PreProcessor PreP;
	BorderLayout borderLayout1 = new BorderLayout();
	JPanel jPanel2 = new JPanel();
	JButton jButton1 = new JButton();
	private JLabel jLabel1 = new JLabel();
	private JPanel jPanel1 = new JPanel();
	private BorderLayout borderLayout2 = new BorderLayout();
	private GridLayout gridLayout1 = new GridLayout();
	private JTextField x1 = new JTextField();
	private JPanel jPanel3 = new JPanel();
	private JLabel jLabel7 = new JLabel("", JLabel.CENTER);
	private JTextField y1 = new JTextField();
	private JLabel jLabel6 = new JLabel("", JLabel.CENTER);
	private JLabel jLabel5 = new JLabel("", JLabel.CENTER);
	private JTextField z1 = new JTextField();
	private JTextField tfa = new JTextField();
	private BorderLayout borderLayout4 = new BorderLayout();
	private JLabel jLabel2 = new JLabel();
	private JPanel jPanel6 = new JPanel();

	public PreProcessor_Scale(Canvas3D j3d, PreProcessor p) {
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
		jButton1.setText("Scale");
		jButton1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton1_actionPerformed(e);
			}
		});
		jLabel1.setForeground(Color.blue);
		jLabel1.setText("Scale from point");
		jPanel1.setLayout(borderLayout2);
		gridLayout1.setColumns(3);
		gridLayout1.setRows(2);
		x1.setText("0");
		jPanel3.setLayout(gridLayout1);
		jLabel7.setText("Z");
		y1.setText("0");
		jLabel6.setText("Y");
		jLabel5.setText("X");
		z1.setText("0");
		jLabel2.setText("Scale factor");
		jPanel6.setLayout(borderLayout4);
		tfa.setText("1");
		this.setPreferredSize(new Dimension(180, 117));
		this.add(jPanel2, BorderLayout.SOUTH);
		jPanel2.add(jButton1, null);
		this.add(jLabel1, BorderLayout.NORTH);
		this.add(jPanel1, BorderLayout.CENTER);
		jPanel1.add(jPanel3, BorderLayout.NORTH);
		jPanel3.add(jLabel5, null);
		jPanel3.add(jLabel6, null);
		jPanel3.add(jLabel7, null);
		jPanel3.add(x1, null);
		jPanel3.add(y1, null);
		jPanel3.add(z1, null);
		jPanel1.add(jPanel6, BorderLayout.SOUTH);
		jPanel6.add(jLabel2, BorderLayout.WEST);
		jPanel6.add(tfa, BorderLayout.CENTER);
		this.validate();
	}

	void jButton1_actionPerformed(ActionEvent e) {
		try {
			Matrix3D m = new Matrix3D();
			m.translate(-Float.parseFloat(x1.getText()),
					-Float.parseFloat(y1.getText()),
					-Float.parseFloat(z1.getText()));
			m.scale(Float.parseFloat(tfa.getText()));
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

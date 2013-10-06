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

public class PreProcessor_Transform extends JPanel {
	Canvas3D J3D;
	BorderLayout borderLayout1 = new BorderLayout();// ("",JLabel.CENTER);
	JPanel jPanel2 = new JPanel();
	JButton b_transform = new JButton();
	private JLabel jLabel1 = new JLabel();
	private JPanel jPanel1 = new JPanel();
	private BorderLayout borderLayout2 = new BorderLayout();
	private JTextField B = new JTextField();
	private JTextField jTextField13 = new JTextField();
	private JTextField jTextField12 = new JTextField();
	private JPanel jPanel3 = new JPanel();
	private JTextField jTextField11 = new JTextField();
	private JTextField jTextField10 = new JTextField();
	private JTextField C = new JTextField();
	private JTextField N = new JTextField();
	private JTextField M = new JTextField();
	private JTextField L = new JTextField();
	private JTextField K = new JTextField();
	private JTextField H = new JTextField();
	private JTextField G = new JTextField();
	private GridLayout gridLayout1 = new GridLayout();
	private JTextField F = new JTextField();
	private JTextField E = new JTextField();
	private JTextField D = new JTextField();
	private JTextField A = new JTextField();
	private JLabel jLabel2 = new JLabel("", JLabel.CENTER);
	ImageIcon img_transform4X4 = new ImageIcon(
			PreProcessor_Transform.class.getResource("transform4X4.gif"));

	public PreProcessor_Transform(Canvas3D j3d) {
		try {
			J3D = j3d;
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		this.setLayout(borderLayout1);
		b_transform.setText("Transform");
		b_transform.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_transform_actionPerformed(e);
			}
		});
		jLabel1.setForeground(Color.blue);
		jLabel1.setText("Transform");
		jPanel1.setLayout(borderLayout2);
		B.setColumns(5);
		B.setToolTipText("B");
		B.setText("0");
		jTextField13.setEditable(false);
		jTextField13.setText("1");
		jTextField12.setEditable(false);
		jTextField12.setText("0");
		jPanel3.setPreferredSize(new Dimension(170, 170));
		jPanel3.setLayout(gridLayout1);
		jTextField11.setEditable(false);
		jTextField11.setText("0");
		jTextField10.setEditable(false);
		jTextField10.setText("0");
		C.setToolTipText("C");
		C.setText("0");
		C.setColumns(5);
		N.setToolTipText("N");
		N.setText("0");
		M.setToolTipText("M");
		M.setText("1");
		L.setToolTipText("L");
		L.setText("0");
		K.setToolTipText("K");
		K.setText("0");
		H.setToolTipText("H");
		H.setText("0");
		G.setToolTipText("G");
		G.setText("0");
		gridLayout1.setRows(4);
		gridLayout1.setColumns(4);
		F.setToolTipText("F");
		F.setText("1");
		E.setToolTipText("E");
		E.setText("0");
		D.setToolTipText("D");
		D.setText("0");
		A.setToolTipText("A");
		A.setText("1");
		A.setColumns(5);
		jLabel2.setIcon(img_transform4X4);
		this.add(jPanel2, BorderLayout.SOUTH);
		jPanel2.add(b_transform, null);
		this.add(jLabel1, BorderLayout.NORTH);
		this.add(jPanel1, BorderLayout.CENTER);
		jPanel1.add(jPanel3, BorderLayout.NORTH);
		jPanel3.add(A, null);
		jPanel3.add(B, null);
		jPanel3.add(C, null);
		jPanel3.add(D, null);
		jPanel3.add(E, null);
		jPanel3.add(F, null);
		jPanel3.add(G, null);
		jPanel3.add(H, null);
		jPanel3.add(K, null);
		jPanel3.add(L, null);
		jPanel3.add(M, null);
		jPanel3.add(N, null);
		jPanel3.add(jTextField10, null);
		jPanel3.add(jTextField11, null);
		jPanel3.add(jTextField12, null);
		jPanel3.add(jTextField13, null);
		jPanel1.add(jLabel2, BorderLayout.CENTER);
	}

	void b_transform_actionPerformed(ActionEvent e) {
		try {
			float[][] t = new float[4][4];
			t[0][0] = Float.parseFloat(A.getText());
			t[0][1] = Float.parseFloat(B.getText());
			t[0][2] = Float.parseFloat(C.getText());
			t[0][3] = Float.parseFloat(D.getText());
			t[1][0] = Float.parseFloat(E.getText());
			t[1][1] = Float.parseFloat(F.getText());
			t[1][2] = Float.parseFloat(G.getText());
			t[1][3] = Float.parseFloat(H.getText());
			t[2][0] = Float.parseFloat(K.getText());
			t[2][1] = Float.parseFloat(L.getText());
			t[2][2] = Float.parseFloat(M.getText());
			t[2][3] = Float.parseFloat(N.getText());
			t[3][0] = 0;
			t[3][1] = 0;
			t[3][2] = 0;
			t[3][3] = 1;
			Matrix3D m = new Matrix3D(t);
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

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

public class PreProcessor_Rotate extends JPanel {
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
	private JPanel jPanel4 = new JPanel();
	private BorderLayout borderLayout3 = new BorderLayout();
	private JPanel jPanel5 = new JPanel();
	private JRadioButton rb_x = new JRadioButton();
	private JRadioButton rb_y = new JRadioButton();
	private JRadioButton rb_z = new JRadioButton();
	private ButtonGroup b_Group = new ButtonGroup();
	private JPanel jPanel6 = new JPanel();
	private JLabel jLabel2 = new JLabel();
	private BorderLayout borderLayout4 = new BorderLayout();
	private JTextField tfa = new JTextField();
	private GridLayout gridLayout2 = new GridLayout();

	public PreProcessor_Rotate(Canvas3D j3d, PreProcessor p) {
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
		jButton1.setText("Rotate");
		jButton1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton1_actionPerformed(e);
			}
		});
		jLabel1.setForeground(Color.blue);
		jLabel1.setText("Rotate around of point");
		jPanel1.setLayout(borderLayout2);
		gridLayout1.setColumns(3);
		gridLayout1.setRows(2);
		x1.setColumns(3);
		x1.setText("0");
		jPanel3.setLayout(gridLayout1);
		jLabel7.setText("Z");
		y1.setText("0");
		y1.setColumns(3);
		jLabel6.setText("Y");
		jLabel5.setText("X");
		z1.setText("0");
		z1.setColumns(3);
		jPanel4.setLayout(borderLayout3);
		rb_x.setSelected(true);
		rb_x.setText("X-axis");
		rb_y.setText("Y-axis");
		rb_z.setText("Z-axis");
		jLabel2.setText("Angle");
		jPanel6.setLayout(borderLayout4);
		jPanel5.setLayout(gridLayout2);
		gridLayout2.setColumns(3);
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
		jPanel1.add(jPanel4, BorderLayout.CENTER);
		jPanel4.add(jPanel5, BorderLayout.NORTH);
		jPanel5.add(rb_x, null);
		jPanel5.add(rb_y, null);
		jPanel5.add(rb_z, null);
		b_Group.add(rb_x);
		b_Group.add(rb_y);
		b_Group.add(rb_z);
		jPanel4.add(jPanel6, BorderLayout.CENTER);
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
			if (rb_x.isSelected())
				m.xrot(Float.parseFloat(tfa.getText()));
			else if (rb_y.isSelected())
				m.yrot(Float.parseFloat(tfa.getText()));
			else if (rb_z.isSelected())
				m.zrot(Float.parseFloat(tfa.getText()));
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

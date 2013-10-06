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

public class PreProcessor_Grid extends JPanel {
	BorderLayout borderLayout1 = new BorderLayout();
	String[] type = { "XY", "YZ", "XZ" };
	private JButton jButton1 = new JButton();
	private JPanel jPanel2 = new JPanel();
	private JLabel jLabel1 = new JLabel();
	private JPanel jPanel1 = new JPanel();
	private Canvas3D J3D;
	private BorderLayout borderLayout2 = new BorderLayout();
	private JPanel jPanel3 = new JPanel();
	private GridLayout gridLayout1 = new GridLayout();
	private JPanel jPanel4 = new JPanel();
	private JPanel jPanel5 = new JPanel();
	private GridLayout gridLayout2 = new GridLayout();
	private JComboBox cb_plane = new JComboBox(type);
	private JLabel jLabel2 = new JLabel();
	private JCheckBox cb_visible = new JCheckBox();
	private JLabel jLabel3 = new JLabel();
	private JTextField tf_size = new JTextField();
	private JLabel jLabel4 = new JLabel();
	private JLabel jLabel5 = new JLabel();
	private JTextField tf_level = new JTextField();
	private JLabel jLabel6 = new JLabel();
	private JButton b_color = new JButton();
	private BorderLayout borderLayout3 = new BorderLayout();
	private JLabel jLabel7 = new JLabel();
	private JPanel jPanel6 = new JPanel();
	private GridLayout gridLayout3 = new GridLayout();
	private JTextField ft_limits_x1 = new JTextField();
	private JTextField ft_limits_y1 = new JTextField();
	private JTextField ft_limits_x2 = new JTextField();
	private JTextField ft_limits_y2 = new JTextField();
	private JLabel jLabel8 = new JLabel();
	private JLabel jLabel9 = new JLabel();

	public PreProcessor_Grid(Canvas3D j3d) {
		try {
			J3D = j3d;
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		this.setLayout(borderLayout1);
		jButton1.setText("Set");
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton1_actionPerformed(e);
			}
		});
		jLabel1.setForeground(Color.blue);
		jLabel1.setText("Grid");
		jPanel1.setLayout(borderLayout2);
		jPanel3.setLayout(gridLayout1);
		gridLayout1.setColumns(2);
		jPanel5.setLayout(gridLayout2);
		gridLayout2.setColumns(2);
		gridLayout2.setRows(5);
		jLabel2.setText("Plane");
		cb_visible.setSelected(true);
		cb_visible.setText("Visible");
		jLabel3.setText("Size");
		jLabel5.setText("Level");
		jLabel6.setText("Color");
		jPanel4.setLayout(borderLayout3);
		jLabel7.setText("Limits");
		jPanel6.setLayout(gridLayout3);
		gridLayout3.setColumns(3);
		gridLayout3.setRows(2);
		ft_limits_x1.setColumns(5);
		ft_limits_y1.setColumns(5);
		ft_limits_x2.setColumns(5);
		ft_limits_y2.setColumns(5);
		tf_size.setColumns(5);
		tf_level.setColumns(5);
		jLabel8.setText("Point 1");
		jLabel9.setText("Point 2");
		b_color.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_color_actionPerformed(e);
			}
		});
		this.add(jPanel2, BorderLayout.SOUTH);
		jPanel2.add(jButton1, null);
		this.add(jLabel1, BorderLayout.NORTH);
		this.add(jPanel1, BorderLayout.CENTER);
		jPanel1.add(jPanel3, BorderLayout.NORTH);
		jPanel1.add(jPanel4, BorderLayout.SOUTH);
		jPanel4.add(jLabel7, BorderLayout.NORTH);
		jPanel4.add(jPanel6, BorderLayout.CENTER);
		jPanel6.add(jLabel8, null);
		jPanel6.add(ft_limits_x1, null);
		jPanel6.add(ft_limits_y1, null);
		jPanel6.add(jLabel9, null);
		jPanel6.add(ft_limits_x2, null);
		jPanel6.add(ft_limits_y2, null);
		jPanel1.add(jPanel5, BorderLayout.CENTER);
		jPanel5.add(cb_visible, null);
		jPanel5.add(jLabel4, null);
		jPanel5.add(jLabel2, null);
		jPanel5.add(cb_plane, null);
		jPanel5.add(jLabel3, null);
		jPanel5.add(tf_size, null);
		jPanel5.add(jLabel5, null);
		jPanel5.add(tf_level, null);
		jPanel5.add(jLabel6, null);
		jPanel5.add(b_color, null);
		cb_visible.setSelected(J3D.getGRIDMODE());
		cb_plane.setSelectedIndex(J3D.getGRIDPLANE());
		tf_size.setText(J3D.getGRIDSIZE() + "");
		tf_level.setText(J3D.getGRIDLEVEL() + "");
		b_color.setBackground(J3D.getGRIDCOLOR());
		ft_limits_x1.setText(J3D.getLIMITS()[0] + "");
		ft_limits_y1.setText(J3D.getLIMITS()[1] + "");
		ft_limits_x2.setText(J3D.getLIMITS()[2] + "");
		ft_limits_y2.setText(J3D.getLIMITS()[3] + "");
		this.validate();
	}

	public void error(Object st) {
		JOptionPane.showMessageDialog(this, "Error: " + st, "Error!",
				JOptionPane.ERROR_MESSAGE);
		if (st instanceof Exception)
			((Exception) st).printStackTrace();
	}

	void jButton1_actionPerformed(ActionEvent e) {
		try {
			float size = Float.parseFloat(tf_size.getText());
			float[] limits = new float[4];
			limits[0] = Float.parseFloat(ft_limits_x1.getText());
			limits[1] = Float.parseFloat(ft_limits_y1.getText());
			limits[2] = Float.parseFloat(ft_limits_x2.getText());
			limits[3] = Float.parseFloat(ft_limits_y2.getText());
			float level = Float.parseFloat(tf_level.getText());
			J3D.setGRIDSIZE(size);
			J3D.setGRIDLEVEL(level);
			J3D.setGRIDCOLOR(b_color.getBackground());
			J3D.setGRIDMODE(cb_visible.isSelected());
			J3D.setGRIDPLANE((byte) cb_plane.getSelectedIndex());
			J3D.setLIMITS(limits);
			J3D.view_reset();
			// J3D.view_repaint();

		} catch (Exception ioe) {
			error(ioe);
		}
	}

	void b_color_actionPerformed(ActionEvent e) {
		Color cl = JColorChooser.showDialog(null, "Pick a Color",
				b_color.getBackground());
		if (cl != null)
			b_color.setBackground(cl);
	}

}

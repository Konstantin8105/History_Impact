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
package j3d;

import java.awt.*;
import java.util.*;

import javax.swing.tree.*;
import java.io.*;
import javax.swing.*;

import gui.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy
 */

public class _Node extends _Object implements Serializable {
	public float x, y, z;
	public Color color;
	public boolean selected = false;
	public Constraints constraint;
	public Loads load;
	public Float M, Ixx, Iyy, Izz, Ixy, Iyz, Ixz;
	public JTextField tx, ty, tz;
	public JComboBox cb_constraints, cb_loads;
	public JTextField tmass, txi, tyi, tzi, txyi, tyzi, txzi;
	private _Node merged_reference;
	private Object[] arr;

	public _Node(boolean add) {
		this(0, 0, 0, Color.black, null, null, add);
	}

	public _Node() {
		this(0, 0, 0, Color.black);
	}

	public _Node(float xx, float yy, float zz) {
		this(xx, yy, zz, Color.black);
	}

	public _Node(float xx, float yy, float zz, Color cl) {
		this(xx, yy, zz, cl, null, null, false);
	}

	public _Node(float xx, float yy, float zz, Color cl, Constraints con,
			Loads ld, boolean add) {
		x = xx;
		y = yy;
		z = zz;
		color = cl;
		constraint = con;
		load = ld;
		this.add = add;
	}

	public String writeObject() {
		String st = new String();
		st = Id + "\tX = " + x + " \tY = " + y + " \tZ = " + z;
		if (constraint != null)
			st += " \tConstraint = " + constraint.name;
		if (load != null)
			st += " \tLoad = " + load.name;
		if (M != null)
			st += " \tM = " + M;
		if (Ixx != null)
			st += " \tIxx = " + Ixx;
		if (Iyy != null)
			st += " \tIyy = " + Iyy;
		if (Izz != null)
			st += " \tIzz = " + Izz;
		if (Ixy != null)
			st += " \tIxy = " + Ixy;
		if (Iyz != null)
			st += " \tIyz = " + Iyz;
		if (Ixz != null)
			st += " \tIxz = " + Ixz;

		return st;
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		x = in.readFloat();
		y = in.readFloat();
		z = in.readFloat();
		color = (Color) in.readObject();
		selected = false;
		constraint = (Constraints) in.readObject();
		load = (Loads) in.readObject();
		M = (Float) in.readObject();
		Ixx = (Float) in.readObject();
		Iyy = (Float) in.readObject();
		Izz = (Float) in.readObject();
		Ixy = (Float) in.readObject();
		Iyz = (Float) in.readObject();
		Ixz = (Float) in.readObject();
		Id = (String) in.readObject();
	}

	public void addSelectedObjects(Vector v) {
		if (selected)
			v.add(this);
	}

	public void deselectRequiredObjects() {
		// do nothing
	}

	public void reset(boolean do_mesh) {
	};

	public void mesh(int type, float size) {
	}

	public Object[] get_Array(Canvas3D j3d) {
		int k = 0;
		arr = new Object[1
				+ (j3d.getSHOW_ID_NODE() ? 1 : 0)
				+ ((j3d.getSHOW_ID_CONSTRAINTS() && constraint != null) ? 1 : 0)
				+ ((j3d.getSHOW_ID_LOADS() && load != null) ? 1 : 0)];
		arr[k++] = new shpNode(x, y, z, selected ? j3d.SELECTCOLOR : color);
		if (j3d.getSHOW_ID_NODE())
			arr[k++] = new shpOrientedText(get_Id(), x, y, z, Color.black);
		if (j3d.getSHOW_ID_CONSTRAINTS() && constraint != null)
			arr[k++] = new shpOrientedText(constraint.name, x, y, z,
					constraint.color);
		if (j3d.getSHOW_ID_LOADS() && load != null)
			arr[k++] = new shpOrientedText(load.name, x, y, z, load.color);

		for (int i = 0; i < arr.length; i++)
			((shp) arr[i]).setShow(show);

		return arr;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean sel) {
		selected = sel;
	}

	public String toString() {
		return "Node ID=" + Id;
	}

	public MutableTreeNode get_TreeNode() {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(this);
		return node;
	}

	public void transform3D(Matrix3D t) {
		if (!selected || processed)
			return;
		float xx = x * t.xx + y * t.xy + z * t.xz + t.xo;
		float yy = x * t.yx + y * t.yy + z * t.yz + t.yo;
		float zz = x * t.zx + y * t.zy + z * t.zz + t.zo;
		x = xx;
		y = yy;
		z = zz;
	}

	public JPanel getEditPanel(Canvas3D j3d, PreProcessor pp) {
		this.J3D = j3d;
		this.PreP = pp;
		JPanel p = new JPanel(new BorderLayout());
		JPanel p1 = new JPanel(new GridLayout(2, 4));
		JPanel p2 = new JPanel(new BorderLayout());
		JPanel p3 = new JPanel(new BorderLayout());
		JPanel p4 = new JPanel(new VerticalFlowLayout());
		JPanel p5 = new JPanel(new BorderLayout());
		JPanel p6 = new JPanel(new BorderLayout());
		JPanel p7 = new JPanel(new BorderLayout());
		JPanel p8 = new JPanel(new BorderLayout());
		JPanel p9 = new JPanel(new BorderLayout());
		JPanel p10 = new JPanel(new BorderLayout());
		JPanel p11 = new JPanel(new BorderLayout());
		JPanel p12 = new JPanel(new BorderLayout());
		JPanel p13 = new JPanel(new BorderLayout());
		JLabel lb = new JLabel("Edit - " + toString());
		JButton b_upd = new JButton(add == true ? "Add" : "Update");
		if (!add) {
			tx = new JTextField(x + "", 3);
			ty = new JTextField(y + "", 3);
			tz = new JTextField(z + "", 3);
		} else {
			tx = new JTextField("", 3);
			ty = new JTextField("", 3);
			tz = new JTextField("", 3);
		}
		cb_constraints = new JComboBox();
		cb_loads = new JComboBox();
		tmass = new JTextField(M == null ? "" : M + "");
		txi = new JTextField(Ixx == null ? "" : Ixx + "");
		tyi = new JTextField(Iyy == null ? "" : Iyy + "");
		tzi = new JTextField(Izz == null ? "" : Izz + "");
		txyi = new JTextField(Ixy == null ? "" : Ixy + "");
		tyzi = new JTextField(Iyz == null ? "" : Iyz + "");
		txzi = new JTextField(Ixz == null ? "" : Ixz + "");

		p.add(p1, BorderLayout.CENTER);
		p1.add(new JLabel());
		p1.add(new JLabel("X"));
		p1.add(new JLabel("Y"));
		p1.add(new JLabel("Z"));
		p1.add(new JLabel("Point:"));
		p1.add(tx, null);
		tx.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();

				if (c == '\n') {
					jButton1_actionPerformed(null);
					tx.requestFocus();
					tx.selectAll();
				} else if (c == ',') {
					ty.requestFocus();
					ty.selectAll();
					e.consume();
				}

			}

		});

		p1.add(ty, null);
		ty.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();

				if (c == '\n') {
					jButton1_actionPerformed(null);
					tx.requestFocus();
					;
				} else if (c == ',') {
					tz.requestFocus();
					tz.selectAll();
					e.consume();
				}
			}
		});

		p1.add(tz, null);
		tz.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();

				if (c == '\n') {
					jButton1_actionPerformed(null);
					tx.requestFocus();
					tx.selectAll();
				} else if (c == ',') {
					tx.requestFocus();
					tx.selectAll();
					e.consume();
				}
			}
		});

		p.add(lb, BorderLayout.NORTH);
		lb.setForeground(Color.blue);

		p.add(p2, BorderLayout.SOUTH);
		p2.add(p3, BorderLayout.SOUTH);
		p3.add(b_upd, null);
		b_upd.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton1_actionPerformed(e);
			}
		});

		p2.add(p4, BorderLayout.NORTH);
		p4.add(p5, null);
		p5.add(new JLabel("Constraints"), BorderLayout.WEST);
		p5.add(cb_constraints, BorderLayout.CENTER);
		cb_constraints.addItem("");
		for (Enumeration en = pp.ConstDB.keys(); en.hasMoreElements();)
			cb_constraints.addItem(en.nextElement());
		if (constraint != null)
			cb_constraints.setSelectedItem(constraint.name);
		cb_constraints.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					cb_loads.requestFocus();
				}
			}
		});

		p4.add(p6, null);
		p6.add(new JLabel("Loads"), BorderLayout.WEST);
		p6.add(cb_loads, BorderLayout.CENTER);
		cb_loads.addItem("");
		for (Enumeration en = pp.LoadDB.keys(); en.hasMoreElements();)
			cb_loads.addItem(en.nextElement());
		if (load != null)
			cb_loads.setSelectedItem(load.name);
		cb_loads.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					tmass.requestFocus();
					tmass.selectAll();
				}
			}
		});

		p4.add(p7, null);
		p7.add(new JLabel("Mass"), BorderLayout.WEST);
		p7.add(tmass, BorderLayout.CENTER);
		tmass.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					txi.requestFocus();
					txi.selectAll();
				}
			}
		});

		p4.add(p8, null);
		p8.add(new JLabel("X inertia"), BorderLayout.WEST);
		p8.add(txi, BorderLayout.CENTER);
		txi.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					tyi.requestFocus();
					tyi.selectAll();
				}
			}
		});

		p4.add(p9, null);
		p9.add(new JLabel("Y inertia"), BorderLayout.WEST);
		p9.add(tyi, BorderLayout.CENTER);
		tyi.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					tzi.requestFocus();
					tzi.selectAll();
				}
			}
		});

		p4.add(p10, null);
		p10.add(new JLabel("Z inertia"), BorderLayout.WEST);
		p10.add(tzi, BorderLayout.CENTER);
		tzi.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					txyi.requestFocus();
					txyi.selectAll();
				}
			}
		});

		p4.add(p11, null);
		p11.add(new JLabel("XY inertia"), BorderLayout.WEST);
		p11.add(txyi, BorderLayout.CENTER);
		txyi.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					tyzi.requestFocus();
					tyzi.selectAll();
				}
			}
		});

		p4.add(p12, null);
		p12.add(new JLabel("YZ inertia"), BorderLayout.WEST);
		p12.add(tyzi, BorderLayout.CENTER);
		tyzi.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					txzi.requestFocus();
					txzi.selectAll();
				}
			}
		});

		p4.add(p13, null);
		p13.add(new JLabel("XZ inertia"), BorderLayout.WEST);
		p13.add(txzi, BorderLayout.CENTER);
		txzi.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					jButton1_actionPerformed(null);
					tx.requestFocus();
					tx.selectAll();
				}
			}
		});

		return p;
	}

	void jButton1_actionPerformed(ActionEvent e) {

		try {
			_Node tmp = this;
			float xx1 = Float.parseFloat(tx.getText());
			float yy1 = Float.parseFloat(ty.getText());
			float zz1 = Float.parseFloat(tz.getText());
			x = xx1;
			y = yy1;
			z = zz1;
			constraint = (Constraints) PreP.ConstDB.get(cb_constraints
					.getSelectedItem() + "");
			load = (Loads) PreP.LoadDB.get(cb_loads.getSelectedItem() + "");
			if (tmass.getText().trim().length() != 0)
				M = new Float(tmass.getText());
			else
				M = null;
			if (txi.getText().trim().length() != 0)
				Ixx = new Float(txi.getText());
			else
				Ixx = null;
			if (tyi.getText().trim().length() != 0)
				Iyy = new Float(tyi.getText());
			else
				Iyy = null;
			if (tzi.getText().trim().length() != 0)
				Izz = new Float(tzi.getText());
			else
				Izz = null;
			if (txyi.getText().trim().length() != 0)
				Ixy = new Float(txyi.getText());
			else
				Ixy = null;
			if (tyzi.getText().trim().length() != 0)
				Iyz = new Float(tyzi.getText());
			else
				Iyz = null;
			if (txzi.getText().trim().length() != 0)
				Ixz = new Float(txzi.getText());
			else
				Ixz = null;

			if (add == true) {
				try {
					tmp = (_Node) this.clone();
					J3D.add3D(tmp);
				} catch (CloneNotSupportedException e1) {
					e1.printStackTrace();
				}
				tmp.add = false;
				tx.requestFocus();
				tx.selectAll();
			} else
				tmp.reset(true);

			J3D.tree_reset();
			J3D.view_reset();
		} catch (Exception e1) {
			error(e1);
		}
	}

	public void requestFocus() {

		tx.requestFocus();
		tx.selectAll();

	}

	public void error(Object st) {
		JOptionPane.showMessageDialog(null, "Error: " + st, "Error!",
				JOptionPane.ERROR_MESSAGE);
		System.out.println("Error: " + st);
		if (st instanceof Exception)
			((Exception) st).printStackTrace();
	}

	public _Node[] get_Nodes() {
		_Node[] arr = { this };
		return arr;
	}

	public _Object[] get_Elements() {
		return null;
	}

	public _Node getMergedReference() {
		return merged_reference;
	}

	public void setMergedReference(_Node merged_reference) {
		this.merged_reference = merged_reference;
	}

	public _Object duplicate(Canvas3D out, boolean add) {
		_Node o = null;
		try {
			o = (_Node) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		if (add)
			out.add3D(o);

		return o;
	}

	public Vector3D getCenter() {
		Vector3D s = new Vector3D(x, y, z);

		return s;
	}

	public boolean isPickPoint(int x, int y, boolean shw, boolean ogl) {
		boolean check = false;

		if (show == shw)
			for (int i = 0; i < arr.length; i++)
				check = (((shp) arr[i]).isPickPoint(x, y, ogl) == true ? true
						: check);

		return check;
	}

	public boolean isPickPoint(Rectangle2D r, boolean shw, boolean ogl) {
		boolean check = false;

		if (show == shw)
			for (int i = 0; i < arr.length; i++)
				check = (((shp) arr[i]).isPickPoint(r, ogl) == true ? true
						: check);

		return check;
	}

	public void replaceObjectWith(_Object o, _Object replacement) {
		// Do nothing
	}

}
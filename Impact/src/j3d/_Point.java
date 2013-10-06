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
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import javax.swing.tree.*;
import java.io.*;
import javax.swing.*;

import com.stevesoft.pat.Regex;

import gui.*;

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy
 */

public class _Point extends _Geometry implements Serializable {
	public float x, y, z, vx, vy, vz, x2, y2, z2;
	public Color color;
	public boolean selected = false;
	private JTextField tx, ty, tz, tvx, tvy, tvz;
	private Object[] arr = new Object[2];
	private static Regex r_index = new Regex("^ *([0-9]+) *");
	private static Regex r_x = new Regex(
			" X *= *([-+]?[0-9]+[.]?[0-9]*(?:[eE][-+]?[0-9]+)?) *");
	private static Regex r_y = new Regex(
			" Y *= *([-+]?[0-9]+[.]?[0-9]*(?:[eE][-+]?[0-9]+)?) *");
	private static Regex r_z = new Regex(
			" Z *= *([-+]?[0-9]+[.]?[0-9]*(?:[eE][-+]?[0-9]+)?) *");

	public String writeObject() {
		String st = new String(Id);
		st += " \tX = " + x + "\tY = " + y + "\tZ = " + z;

		return st;
	}

	public void readObject(String st) {
		if (r_index.search(st))
			Id = r_index.stringMatched(1);
		if (r_x.search(st))
			x = Float.parseFloat(r_x.stringMatched(1));
		if (r_y.search(st))
			y = Float.parseFloat(r_y.stringMatched(1));
		if (r_z.search(st))
			z = Float.parseFloat(r_z.stringMatched(1));
	}

	public void deselectRequiredObjects() {
		// Do nothing
	}

	public void reset(boolean do_mesh) {

		x2 = x + vx;
		y2 = y + vy;
		z2 = z + vz;

	};

	public void mesh(int type, float size) {
	}

	public boolean delete() {
		return selected;
	}

	public _Point(boolean add) {
		this(0, 0, 0, 0, 0, 0, Color.black, add);
	}

	public _Point(_Point p) {
		this(p.x, p.y, p.z, p.vx, p.vy, p.vz, p.color, false);
	}

	public _Point(float xx, float yy, float zz, float vx, float vy, float vz,
			Color cl, boolean add) {
		this.add = add;
		x = xx;
		y = yy;
		z = zz;
		this.vx = vx;
		this.vy = vy;
		this.vz = vz;
		color = cl;
	}

	public _Point(float xx, float yy, float zz, float vx, float vy, float vz,
			Color cl) {
		this(xx, yy, zz, vx, vy, vz, cl, false);
	}

	public _Point(float xx, float yy, float zz, float vx, float vy, float vz) {
		this(xx, yy, zz, vx, vy, vz, Color.black, false);
	}

	public _Point(float xx, float yy, float zz, Color cl) {
		this(xx, yy, zz, 0f, 0f, 0f, cl, false);
	}

	public _Point(float xx, float yy, float zz) {
		this(xx, yy, zz, 0f, 0f, 0f, Color.black, false);
	}

	public Object[] get_Array(Canvas3D j3d) {
		this.J3D = j3d;
		arr = new Object[2];
		arr[0] = new shpPoint(x, y, z, selected ? j3d.SELECTCOLOR : color);
		arr[1] = new shpLine(x, y, z, x2, y2, z2, selected ? j3d.SELECTCOLOR
				: color);
		for (int i = 0; i < arr.length; i++)
			((shp) arr[i]).setShow(show);
		return arr;
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

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean sel) {
		selected = sel;
	}

	public String toString() {
		return "Point ID=" + Id;
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
		xx = x2 * t.xx + y2 * t.xy + z2 * t.xz + t.xo;
		yy = x2 * t.yx + y2 * t.yy + z2 * t.yz + t.yo;
		zz = x2 * t.zx + y2 * t.zy + z2 * t.zz + t.zo;
		x2 = xx;
		y2 = yy;
		z2 = zz;
	}

	public JPanel getEditPanel(Canvas3D j3d, PreProcessor pp) {
		this.J3D = j3d;
		this.PreP = pp;
		JPanel p = new JPanel(new BorderLayout());
		JPanel p1 = new JPanel(new GridLayout(3, 4));
		JPanel p2 = new JPanel(new BorderLayout());
		JLabel lb = new JLabel("Edit - " + toString());
		// if (!add) {
		tx = new JTextField(x + "", 3);
		ty = new JTextField(y + "", 3);
		tz = new JTextField(z + "", 3);
		tvx = new JTextField(vx + "", 3);
		tvy = new JTextField(vy + "", 3);
		tvz = new JTextField(vz + "", 3);
		/*
		 * } else { tx = new JTextField("",3); ty = new JTextField("",3); tz =
		 * new JTextField("",3); tvx = new JTextField("",3); tvy = new
		 * JTextField("",3); tvz = new JTextField("",3); }
		 */
		lb.setForeground(Color.blue);
		JButton b_upd = new JButton(add == true ? "Add" : "Update");
		b_upd.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton1_actionPerformed(e);
			}
		});

		p.add(p1, BorderLayout.CENTER);
		p1.add(new JLabel());
		p1.add(new JLabel("X"));
		p1.add(new JLabel("Y"));
		p1.add(new JLabel("Z"));
		p1.add(new JLabel("Point:"));
		p1.add(tx, null);
		p1.add(ty, null);
		p1.add(tz, null);
		p1.add(new JLabel("Vector:"));
		p1.add(tvx, null);
		p1.add(tvy, null);
		p1.add(tvz, null);
		p.add(lb, BorderLayout.NORTH);
		p.add(p2, BorderLayout.SOUTH);
		p2.add(b_upd, null);

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

		ty.addKeyListener(new KeyAdapter() {

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
					tz.requestFocus();
					tz.selectAll();
					e.consume();
				}
			}
		});

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
					tvx.requestFocus();
					tvx.selectAll();
					e.consume();
				}
			}
		});

		tvx.addKeyListener(new KeyAdapter() {

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
					tvy.requestFocus();
					tvy.selectAll();
					e.consume();
				}

			}

		});

		tvy.addKeyListener(new KeyAdapter() {

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
					tvz.requestFocus();
					tvz.selectAll();
					e.consume();
				}
			}
		});

		tvz.addKeyListener(new KeyAdapter() {

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

		return p;
	}

	void jButton1_actionPerformed(ActionEvent e) {
		_Point tmp = this;
		float xx1 = Float.parseFloat(tx.getText());
		float yy1 = Float.parseFloat(ty.getText());
		float zz1 = Float.parseFloat(tz.getText());
		x = xx1;
		y = yy1;
		z = zz1;

		vx = Float.parseFloat(tvx.getText());
		vy = Float.parseFloat(tvy.getText());
		vz = Float.parseFloat(tvz.getText());
		float l = (float) Math.sqrt(vx * vx + vy * vy + vz * vz);
		if (l > 0) {
			vx = vx / l;
			vy = vy / l;
			vz = vz / l;
		}

		x2 = x + vx;
		y2 = y + vy;
		z2 = z + vz;

		if (add == true) {
			try {
				tmp = (_Point) this.clone();
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
	}

	public void requestFocus() {

		tx.requestFocus();
		tx.selectAll();

	}

	public _Node[] get_Nodes() {
		return null;
	}

	public _Object[] get_Elements() {
		return null;
	}

	public float distance(_Point p) {
		return (float) Math.sqrt((x - p.x) * (x - p.x) + (y - p.y) * (y - p.y)
				+ (z - p.z) * (z - p.z));
	}

	public Vector3D getVector() {
		Vector3D v = new Vector3D();

		v.x = this.x;
		v.y = this.y;
		v.z = this.z;

		return v;
	}

	public void toLocal(Vector3D translate, Matrix3D rotate) {

		Vector3D temp = new Vector3D(this.x, this.y, this.z);

		temp.add(temp, translate);
		temp.mult(rotate, temp);

		this.x = temp.x;
		this.y = temp.y;
		this.z = temp.z;

	}

	public void toGlobal(Vector3D translate, Matrix3D rotate) {

		Vector3D temp = new Vector3D(this.x, this.y, this.z);

		temp.sub(temp, translate);
		temp.transMult(rotate, temp);

		this.x = temp.x;
		this.y = temp.y;
		this.z = temp.z;

	}

	public _Object duplicate(Canvas3D out, boolean add) {
		_Point o = null;
		try {
			o = (_Point) this.clone();
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

	public void replaceObjectWith(_Object o, _Object replacement) {
		// Do nothing
	}

}

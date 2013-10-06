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
import java.awt.geom.*;
import javax.swing.tree.*;
import java.io.*;
import javax.swing.*;
import java.awt.event.*;
import gui.*;

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy
 */

public class _Element2Post extends _Element implements Serializable {
	public float x1, x2, y1, y2, z1, z2;
	public Color color;
	private Canvas3D J3D;
	private JTextField tx1, tx2, ty1, ty2, tz2, tz1;
	private Object[] arr;

	public void reset(boolean do_mesh) {
	};

	public void mesh(int type, float size) {
	}

	public boolean delete() {
		return selected;
	}

	public _Element2Post(_Point p1, _Point p2, Color c) {
		this(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, c);
	}

	public _Element2Post(_Point p1, _Point p2) {
		this(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, Color.black);
	}

	public _Element2Post(float xx1, float yy1, float zz1, float xx2, float yy2,
			float zz2) {
		this(xx1, yy1, zz1, xx2, yy2, zz2, Color.black);
	}

	public _Element2Post(float xx1, float yy1, float zz1, float xx2, float yy2,
			float zz2, Color c) {
		x1 = xx1;
		x2 = xx2;
		y1 = yy1;
		y2 = yy2;
		z1 = zz1;
		z2 = zz2;
		color = c;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeFloat(x1);
		out.writeFloat(x2);
		out.writeFloat(y1);
		out.writeFloat(y2);
		out.writeFloat(z1);
		out.writeFloat(z2);
		out.writeObject(color);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		x1 = in.readFloat();
		x2 = in.readFloat();
		y1 = in.readFloat();
		y2 = in.readFloat();
		z1 = in.readFloat();
		z2 = in.readFloat();
		color = (Color) in.readObject();
		selected = false;
	}

	public Object[] get_Array(Canvas3D j3d) {
		arr = new Object[1];
		arr[0] = new shpLine(x1, y1, z1, x2, y2, z2, selected ? j3d.SELECTCOLOR
				: color);
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
		return "Line ID=" + Id;
	}

	public MutableTreeNode get_TreeNode() {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(this);
		// node.add(new DefaultMutableTreeNode("Color"));
		node.add(new DefaultMutableTreeNode("X,Y,Z - (" + x1 + "," + y1 + ","
				+ z1 + ")"));
		node.add(new DefaultMutableTreeNode("X,Y,Z - (" + x2 + "," + y2 + ","
				+ z2 + ")"));
		return node;
	}

	public void transform3D(Matrix3D t) {
		if (!selected || processed)
			return;
		float xx1 = x1 * t.xx + y1 * t.xy + z1 * t.xz + t.xo;
		float xx2 = x2 * t.xx + y2 * t.xy + z2 * t.xz + t.xo;
		float yy1 = x1 * t.yx + y1 * t.yy + z1 * t.yz + t.yo;
		float yy2 = x2 * t.yx + y2 * t.yy + z2 * t.yz + t.yo;
		float zz1 = x1 * t.zx + y1 * t.zy + z1 * t.zz + t.zo;
		float zz2 = x2 * t.zx + y2 * t.zy + z2 * t.zz + t.zo;
		x1 = xx1;
		x2 = xx2;
		y1 = yy1;
		y2 = yy2;
		z1 = zz1;
		z2 = zz2;
	}

	public JPanel getEditPanel(Canvas3D j3d, PreProcessor pp) {
		this.J3D = j3d;
		this.PreP = pp;

		JPanel p = new JPanel(new BorderLayout());
		JPanel p1 = new JPanel(new GridLayout(3, 4));
		JPanel p2 = new JPanel();
		JLabel lb = new JLabel("Edit - " + toString());
		JButton b_up = new JButton("Update");
		tx1 = new JTextField();
		tx2 = new JTextField();
		ty1 = new JTextField();
		ty2 = new JTextField();
		tz2 = new JTextField();
		tz1 = new JTextField();
		tx1.setText(x1 + "");
		tx2.setText(x2 + "");
		ty1.setText(y1 + "");
		ty2.setText(y2 + "");
		tz2.setText(z2 + "");
		tz1.setText(z1 + "");
		p.add(p1, BorderLayout.CENTER);
		b_up.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				float xx1 = java.lang.Float.parseFloat(tx1.getText());
				float xx2 = java.lang.Float.parseFloat(tx2.getText());
				float yy1 = java.lang.Float.parseFloat(ty1.getText());
				float yy2 = java.lang.Float.parseFloat(ty2.getText());
				float zz1 = java.lang.Float.parseFloat(tz1.getText());
				float zz2 = java.lang.Float.parseFloat(tz2.getText());
				x1 = xx1;
				x2 = xx2;
				y1 = yy1;
				y2 = yy2;
				z1 = zz1;
				z2 = zz2;
				J3D.tree_reset();
				J3D.view_reset();
			}
		});
		lb.setForeground(Color.blue);
		p1.add(new JLabel());
		p1.add(new JLabel("X", JLabel.CENTER));
		p1.add(new JLabel("Y", JLabel.CENTER));
		p1.add(new JLabel("Z", JLabel.CENTER));
		p1.add(new JLabel("From: "));
		p1.add(tx1, null);
		p1.add(ty1, null);
		p1.add(tz1, null);
		p1.add(new JLabel("To: "));
		p1.add(tx2, null);
		p1.add(ty2, null);
		p1.add(tz2, null);
		p.add(p2, BorderLayout.SOUTH);
		p2.add(b_up, null);
		p.add(lb, BorderLayout.NORTH);
		return p;
	}

	public _Node[] get_Nodes() {
		return null;
	}

	public _Object[] get_Elements() {
		return null;
	}

	public _Object duplicate(Canvas3D out, boolean add) {
		_Element2Post o = null;
		try {
			o = (_Element2Post) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		// out.add3D(o);

		return o;
	}

	public Vector3D getCenter() {
		Vector3D s = new Vector3D(x1 + x2, y1 + y2, z1 + z2);

		s.scale(1.0f / 2.0f);

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

	public void deselectRequiredObjects() {
		// Do nothing
	}

	public void replaceObjectWith(_Object o, _Object replacement) {
		// Do nothing
	}

}
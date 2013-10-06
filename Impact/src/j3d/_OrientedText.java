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
import gui.*;

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy
 */

public class _OrientedText extends _Object implements Serializable {
	public float x, y, z;
	public String text;
	public Color color;
	public boolean selected = false;

	public void reset(boolean do_mesh) {
	};

	public void mesh(int type, float size) {
	}

	private Object[] arr;

	public _OrientedText(String st, float xx, float yy, float zz) {
		text = st;
		x = xx;
		y = yy;
		z = zz;
		color = Color.black;
	}

	public _OrientedText(String st, float xx, float yy, float zz, Color cl) {
		text = st;
		x = xx;
		y = yy;
		z = zz;
		color = cl;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeFloat(x);
		out.writeFloat(y);
		out.writeFloat(z);
		out.writeObject(color);
		out.writeObject(text);
		out.writeObject(Id);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		x = in.readFloat();
		y = in.readFloat();
		z = in.readFloat();
		color = (Color) in.readObject();
		selected = false;
		text = (String) in.readObject();
		Id = (String) in.readObject();
	}

	public Object[] get_Array(Canvas3D j3d) {
		arr = new Object[1];
		arr[0] = new shpOrientedText(text, x, y, z, selected ? j3d.SELECTCOLOR
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
		return "OrientedText ID=" + Id;
	}

	public MutableTreeNode get_TreeNode() {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(this);
		node.add(new DefaultMutableTreeNode("Color"));
		return node;
	}

	public JPanel getEditPanel(Canvas3D j3d, PreProcessor pp) {
		this.J3D = j3d;
		this.PreP = pp;
		return new JPanel();
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

	public _Node[] get_Nodes() {
		return null;
	}

	public _Object[] get_Elements() {
		return null;
	}

	public _Object duplicate(Canvas3D out, boolean add) {
		_OrientedText o = null;
		try {
			o = (_OrientedText) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		// out.add3D(o);

		return o;
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
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

public class _Element4Post extends _Element implements Serializable {
	public float x1, x2, x3, x4, y1, y2, y3, y4, z1, z2, z3, z4;
	public Color contur;
	public Paint fill;
	public Color[] gradient = new Color[5];
	public boolean selected = false;
	private Object[] arr;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeFloat(x1);
		out.writeFloat(x2);
		out.writeFloat(x3);
		out.writeFloat(x4);
		out.writeFloat(y1);
		out.writeFloat(y2);
		out.writeFloat(y3);
		out.writeFloat(y4);
		out.writeFloat(z1);
		out.writeFloat(z2);
		out.writeFloat(z3);
		out.writeFloat(z4);
		out.writeObject(contur);
		out.writeObject(fill);
		out.writeObject(gradient);
		out.writeObject(Id);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		x1 = in.readFloat();
		x2 = in.readFloat();
		x3 = in.readFloat();
		x4 = in.readFloat();
		y1 = in.readFloat();
		y2 = in.readFloat();
		y3 = in.readFloat();
		y4 = in.readFloat();
		z1 = in.readFloat();
		z2 = in.readFloat();
		z3 = in.readFloat();
		z4 = in.readFloat();
		contur = (Color) in.readObject();
		fill = (Paint) in.readObject();
		gradient = (Color[]) in.readObject();
		Id = (String) in.readObject();
		selected = false;
	}

	public void reset(boolean do_mesh) {
	};

	public void mesh(int type, float size) {
	}

	public boolean delete() {
		return selected;
	}

	public _Element4Post(_Point p1, _Point p2, _Point p3, _Point p4, Paint f,
			Color cont) {
		this(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, p3.x, p3.y, p3.z, p4.x, p4.y,
				p4.z, f, cont);
	}

	public _Element4Post(_Point p1, _Point p2, _Point p3, _Point p4, Color cont) {
		this(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, p3.x, p3.y, p3.z, p4.x, p4.y,
				p4.z, null, cont);
	}

	public _Element4Post(_Point p1, _Point p2, _Point p3, _Point p4) {
		this(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, p3.x, p3.y, p3.z, p4.x, p4.y,
				p4.z, null, Color.black);
	}

	public _Element4Post(float xx1, float yy1, float zz1, float xx2, float yy2,
			float zz2, float xx3, float yy3, float zz3, float xx4, float yy4,
			float zz4, Color cont) {
		this(xx1, yy1, zz1, xx2, yy2, zz2, xx3, yy3, zz3, xx4, yy4, zz4, null,
				cont);
	}

	public _Element4Post(float xx1, float yy1, float zz1, float xx2, float yy2,
			float zz2, float xx3, float yy3, float xx4, float yy4, float zz4,
			float zz3) {
		this(xx1, yy1, zz1, xx2, yy2, zz2, xx3, yy3, zz3, xx4, yy4, zz4, null,
				Color.black);
	}

	public _Element4Post(float xx1, float yy1, float zz1, float xx2, float yy2,
			float zz2, float xx3, float yy3, float zz3, float xx4, float yy4,
			float zz4, Paint f, Color cont) {
		x1 = xx1;
		x2 = xx2;
		x3 = xx3;
		x4 = xx4;
		y1 = yy1;
		y2 = yy2;
		y3 = yy3;
		y4 = yy4;
		z1 = zz1;
		z2 = zz2;
		z3 = zz3;
		z4 = zz4;
		fill = f;
		contur = cont;
	}

	public _Element4Post(float xx1, float yy1, float zz1, Color c1, float xx2,
			float yy2, float zz2, Color c2, float xx3, float yy3, float zz3,
			Color c3, float xx4, float yy4, float zz4, Color c4, Color cont) {
		this(xx1, yy1, zz1, c1, xx2, yy2, zz2, c2, xx3, yy3, zz3, c3, xx4, yy4,
				zz4, c4, null, cont);
	}

	public _Element4Post(float xx1, float yy1, float zz1, Color c1, float xx2,
			float yy2, float zz2, Color c2, float xx3, float yy3, float zz3,
			Color c3, float xx4, float yy4, float zz4, Color c4, Color c5,
			Color cont) {
		x1 = xx1;
		x2 = xx2;
		x3 = xx3;
		x4 = xx4;
		y1 = yy1;
		y2 = yy2;
		y3 = yy3;
		y4 = yy4;
		z1 = zz1;
		z2 = zz2;
		z3 = zz3;
		z4 = zz4;
		fill = null;
		gradient[0] = c1;
		gradient[1] = c2;
		gradient[2] = c3;
		gradient[3] = c4;
		gradient[4] = c5;
		contur = cont;
	}

	public Object[] get_Array(Canvas3D j3d) {
		arr = new Object[1];
		if (x1 == x2 && y1 == y2 && z1 == z2) {
			if (gradient[0] == null)
				arr[0] = new shpTriangle(x2, y2, z2, x3, y3, z3, x4, y4, z4,
						selected ? j3d.SELECTCOLOR : fill, contur);
			else if (gradient[4] != null)
				if (selected)
					arr[0] = new shpTriangle(x2, y2, z2, x3, y3, z3, x4, y4,
							z4, j3d.SELECTCOLOR, contur);
				else
					arr[0] = new shpTriangle(x2, y2, z2, gradient[1], x3, y3,
							z3, gradient[2], x4, y4, z4, gradient[3],
							gradient[4], contur);
			else if (selected)
				arr[0] = new shpTriangle(x2, y2, z2, x3, y3, z3, x4, y4, z4,
						j3d.SELECTCOLOR, contur);
			else
				arr[0] = new shpTriangle(x2, y2, z2, gradient[1], x3, y3, z3,
						gradient[2], x4, y4, z4, gradient[3], contur);
		} else if (x3 == x2 && y3 == y2 && z3 == z2) {
			if (gradient[0] == null)
				arr[0] = new shpTriangle(x1, y1, z1, x3, y3, z3, x4, y4, z4,
						selected ? j3d.SELECTCOLOR : fill, contur);
			else if (gradient[4] != null)
				if (selected)
					arr[0] = new shpTriangle(x1, y1, z1, x3, y3, z3, x4, y4,
							z4, j3d.SELECTCOLOR, contur);
				else
					arr[0] = new shpTriangle(x1, y1, z1, gradient[1], x3, y3,
							z3, gradient[2], x4, y4, z4, gradient[3],
							gradient[4], contur);
			else if (selected)
				arr[0] = new shpTriangle(x1, y1, z1, x3, y3, z3, x4, y4, z4,
						j3d.SELECTCOLOR, contur);
			else
				arr[0] = new shpTriangle(x1, y1, z1, gradient[1], x3, y3, z3,
						gradient[2], x4, y4, z4, gradient[3], contur);
		} else {
			if (gradient[0] == null)
				arr[0] = new shpQuad(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4,
						y4, z4, selected ? j3d.SELECTCOLOR : fill, contur);
			else if (gradient[4] != null)
				if (selected)
					arr[0] = new shpQuad(x1, y1, z1, x2, y2, z2, x3, y3, z3,
							x4, y4, z4, j3d.SELECTCOLOR, contur);
				else
					arr[0] = new shpQuad(x1, y1, z1, gradient[0], x2, y2, z2,
							gradient[1], x3, y3, z3, gradient[2], x4, y4, z4,
							gradient[3], gradient[4], contur);
			else if (selected)
				arr[0] = new shpQuad(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4,
						y4, z4, j3d.SELECTCOLOR, contur);
			else
				arr[0] = new shpQuad(x1, y1, z1, gradient[0], x2, y2, z2,
						gradient[1], x3, y3, z3, gradient[2], x4, y4, z4,
						gradient[3], contur);
		}
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
		return "Quad ID=" + Id;
	}

	public MutableTreeNode get_TreeNode() {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(this);
		return node;
	}

	public void transform3D(Matrix3D t) {
		if (!selected || processed)
			return;
		float xx1 = x1 * t.xx + y1 * t.xy + z1 * t.xz + t.xo;
		float xx2 = x2 * t.xx + y2 * t.xy + z2 * t.xz + t.xo;
		float xx3 = x3 * t.xx + y3 * t.xy + z3 * t.xz + t.xo;
		float xx4 = x4 * t.xx + y4 * t.xy + z4 * t.xz + t.xo;
		float yy1 = x1 * t.yx + y1 * t.yy + z1 * t.yz + t.yo;
		float yy2 = x2 * t.yx + y2 * t.yy + z2 * t.yz + t.yo;
		float yy3 = x3 * t.yx + y3 * t.yy + z3 * t.yz + t.yo;
		float yy4 = x4 * t.yx + y4 * t.yy + z4 * t.yz + t.yo;
		float zz1 = x1 * t.zx + y1 * t.zy + z1 * t.zz + t.zo;
		float zz2 = x2 * t.zx + y2 * t.zy + z2 * t.zz + t.zo;
		float zz3 = x3 * t.zx + y3 * t.zy + z3 * t.zz + t.zo;
		float zz4 = x4 * t.zx + y4 * t.zy + z4 * t.zz + t.zo;
		x1 = xx1;
		x2 = xx2;
		x3 = xx3;
		x4 = xx4;
		y1 = yy1;
		y2 = yy2;
		y3 = yy3;
		y4 = yy4;
		z1 = zz1;
		z2 = zz2;
		z3 = zz3;
		z4 = zz4;
	}

	public JPanel getEditPanel(Canvas3D j3d, PreProcessor pp) {
		this.J3D = j3d;
		this.PreP = pp;
		return new JPanel();
	}

	public _Node[] get_Nodes() {
		return null;
	}

	public _Object[] get_Elements() {
		return null;
	}

	public _Object duplicate(Canvas3D out, boolean add) {
		_Element4Post o = null;
		try {
			o = (_Element4Post) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		if (add)
			out.add3D(o);

		return o;
	}

	public Vector3D getCenter() {
		Vector3D s = new Vector3D(x1 + x2 + x3 + x4, y1 + y2 + y3 + y4, z1 + z2
				+ z3 + z4);

		s.scale(1.0f / 4.0f);

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
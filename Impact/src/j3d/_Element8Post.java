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

public class _Element8Post extends _Element implements Serializable {
	public float x1, x2, x3, x4, x5, x6, x7, x8, y1, y2, y3, y4, y5, y6, y7,
			y8, z1, z2, z3, z4, z5, z6, z7, z8;
	public Color contur;
	public Paint fill;
	public Color[] gradient = new Color[8];
	public boolean selected = false;
	private Object[] arr;

	public void reset(boolean do_mesh) {
	};

	public void mesh(int type, float size) {
	}

	public boolean delete() {
		return selected;
	}

	public _Element8Post(_Point p1, _Point p2, _Point p3, _Point p4, _Point p5,
			_Point p6, _Point p7, _Point p8, Paint f, Color cont) {
		this(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, p3.x, p3.y, p3.z, p4.x, p4.y,
				p4.z, p5.x, p5.y, p5.z, p6.x, p6.y, p6.z, p7.x, p7.y, p7.z,
				p8.x, p8.y, p8.z, f, cont);
	}

	public _Element8Post(float xx1, float yy1, float zz1, float xx2, float yy2,
			float zz2, float xx3, float yy3, float zz3, float xx4, float yy4,
			float zz4, float xx5, float yy5, float zz5, float xx6, float yy6,
			float zz6, float xx7, float yy7, float zz7, float xx8, float yy8,
			float zz8, Paint f, Color cont) {
		x1 = xx1;
		x2 = xx2;
		x3 = xx3;
		x4 = xx4;
		x5 = xx5;
		x6 = xx6;
		x7 = xx7;
		x8 = xx8;
		y1 = yy1;
		y2 = yy2;
		y3 = yy3;
		y4 = yy4;
		y5 = yy5;
		y6 = yy6;
		y7 = yy7;
		y8 = yy8;
		z1 = zz1;
		z2 = zz2;
		z3 = zz3;
		z4 = zz4;
		z5 = zz5;
		z6 = zz6;
		z7 = zz7;
		z8 = zz8;
		fill = f;
		contur = cont;
	}

	public _Element8Post(float xx1, float yy1, float zz1, Color c1, float xx2,
			float yy2, float zz2, Color c2, float xx3, float yy3, float zz3,
			Color c3, float xx4, float yy4, float zz4, Color c4, float xx5,
			float yy5, float zz5, Color c5, float xx6, float yy6, float zz6,
			Color c6, float xx7, float yy7, float zz7, Color c7, float xx8,
			float yy8, float zz8, Color c8, Color cont) {
		x1 = xx1;
		x2 = xx2;
		x3 = xx3;
		x4 = xx4;
		x5 = xx5;
		x6 = xx6;
		x7 = xx7;
		x8 = xx8;
		y1 = yy1;
		y2 = yy2;
		y3 = yy3;
		y4 = yy4;
		y5 = yy5;
		y6 = yy6;
		y7 = yy7;
		y8 = yy8;
		z1 = zz1;
		z2 = zz2;
		z3 = zz3;
		z4 = zz4;
		z5 = zz5;
		z6 = zz6;
		z7 = zz7;
		z8 = zz8;
		fill = null;
		gradient[0] = c1;
		gradient[1] = c2;
		gradient[2] = c3;
		gradient[3] = c4;
		gradient[4] = c5;
		gradient[5] = c1;
		gradient[6] = c2;
		gradient[7] = c3;
		contur = cont;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeFloat(x1);
		out.writeFloat(x2);
		out.writeFloat(x3);
		out.writeFloat(x4);
		out.writeFloat(x5);
		out.writeFloat(x6);
		out.writeFloat(x7);
		out.writeFloat(x8);
		out.writeFloat(y1);
		out.writeFloat(y2);
		out.writeFloat(y3);
		out.writeFloat(y4);
		out.writeFloat(y5);
		out.writeFloat(y6);
		out.writeFloat(y7);
		out.writeFloat(y8);
		out.writeFloat(z1);
		out.writeFloat(z2);
		out.writeFloat(z3);
		out.writeFloat(z4);
		out.writeFloat(z5);
		out.writeFloat(z6);
		out.writeFloat(z7);
		out.writeFloat(z8);
		out.writeObject(contur);
		out.writeObject(fill);
		out.writeObject(gradient);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		x1 = in.readFloat();
		x2 = in.readFloat();
		x3 = in.readFloat();
		x4 = in.readFloat();
		x5 = in.readFloat();
		x6 = in.readFloat();
		x7 = in.readFloat();
		x8 = in.readFloat();
		y1 = in.readFloat();
		y2 = in.readFloat();
		y3 = in.readFloat();
		y4 = in.readFloat();
		y5 = in.readFloat();
		y6 = in.readFloat();
		y7 = in.readFloat();
		y8 = in.readFloat();
		z1 = in.readFloat();
		z2 = in.readFloat();
		z3 = in.readFloat();
		z4 = in.readFloat();
		z5 = in.readFloat();
		z6 = in.readFloat();
		z7 = in.readFloat();
		z8 = in.readFloat();
		contur = (Color) in.readObject();
		fill = (Paint) in.readObject();
		gradient = (Color[]) in.readObject();
		selected = false;
	}

	public Object[] get_Array(Canvas3D j3d) {
		arr = new Object[6];
		if (gradient[0] == null || selected) {
			arr[0] = new shpQuad(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4,
					z4, selected ? j3d.SELECTCOLOR : fill, contur);
			arr[1] = new shpQuad(x5, y5, z5, x6, y6, z6, x7, y7, z7, x8, y8,
					z8, selected ? j3d.SELECTCOLOR : fill, contur);
			arr[2] = new shpQuad(x1, y1, z1, x2, y2, z2, x6, y6, z6, x5, y5,
					z5, selected ? j3d.SELECTCOLOR : fill, contur);
			arr[3] = new shpQuad(x2, y2, z2, x3, y3, z3, x7, y7, z7, x6, y6,
					z6, selected ? j3d.SELECTCOLOR : fill, contur);
			arr[4] = new shpQuad(x1, y1, z1, x4, y4, z4, x8, y8, z8, x5, y5,
					z5, selected ? j3d.SELECTCOLOR : fill, contur);
			arr[5] = new shpQuad(x3, y3, z3, x4, y4, z4, x8, y8, z8, x7, y7,
					z7, selected ? j3d.SELECTCOLOR : fill, contur);
		} else {
			arr[0] = new shpQuad(x1, y1, z1, gradient[0], x2, y2, z2,
					gradient[1], x3, y3, z3, gradient[2], x4, y4, z4,
					gradient[3], contur);
			arr[1] = new shpQuad(x5, y5, z5, gradient[4], x6, y6, z6,
					gradient[5], x7, y7, z7, gradient[6], x8, y8, z8,
					gradient[7], contur);
			arr[2] = new shpQuad(x1, y1, z1, gradient[0], x2, y2, z2,
					gradient[1], x6, y6, z6, gradient[5], x5, y5, z5,
					gradient[4], contur);
			arr[3] = new shpQuad(x2, y2, z2, gradient[1], x3, y3, z3,
					gradient[2], x7, y7, z7, gradient[6], x6, y6, z6,
					gradient[5], contur);
			arr[4] = new shpQuad(x1, y1, z1, gradient[0], x4, y4, z4,
					gradient[3], x8, y8, z8, gradient[7], x5, y5, z5,
					gradient[4], contur);
			arr[5] = new shpQuad(x3, y3, z3, gradient[2], x4, y4, z4,
					gradient[3], x8, y8, z8, gradient[7], x7, y7, z7,
					gradient[6], contur);
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
		return "Hexahedron ID=" + Id;
	}

	public MutableTreeNode get_TreeNode() {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(this);
		node.add(new DefaultMutableTreeNode("Color"));
		node.add(new DefaultMutableTreeNode("Mesh"));
		return node;
	}

	public void transform3D(Matrix3D t) {
		if (!selected || processed)
			return;
		float xx1 = x1 * t.xx + y1 * t.xy + z1 * t.xz + t.xo;
		float xx2 = x2 * t.xx + y2 * t.xy + z2 * t.xz + t.xo;
		float xx3 = x3 * t.xx + y3 * t.xy + z3 * t.xz + t.xo;
		float xx4 = x4 * t.xx + y4 * t.xy + z4 * t.xz + t.xo;
		float xx5 = x5 * t.xx + y5 * t.xy + z5 * t.xz + t.xo;
		float xx6 = x6 * t.xx + y6 * t.xy + z6 * t.xz + t.xo;
		float xx7 = x7 * t.xx + y7 * t.xy + z7 * t.xz + t.xo;
		float xx8 = x8 * t.xx + y8 * t.xy + z8 * t.xz + t.xo;
		float yy1 = x1 * t.yx + y1 * t.yy + z1 * t.yz + t.yo;
		float yy2 = x2 * t.yx + y2 * t.yy + z2 * t.yz + t.yo;
		float yy3 = x3 * t.yx + y3 * t.yy + z3 * t.yz + t.yo;
		float yy4 = x4 * t.yx + y4 * t.yy + z4 * t.yz + t.yo;
		float yy5 = x5 * t.yx + y5 * t.yy + z5 * t.yz + t.yo;
		float yy6 = x6 * t.yx + y6 * t.yy + z6 * t.yz + t.yo;
		float yy7 = x7 * t.yx + y7 * t.yy + z7 * t.yz + t.yo;
		float yy8 = x8 * t.yx + y8 * t.yy + z8 * t.yz + t.yo;
		float zz1 = x1 * t.zx + y1 * t.zy + z1 * t.zz + t.zo;
		float zz2 = x2 * t.zx + y2 * t.zy + z2 * t.zz + t.zo;
		float zz3 = x3 * t.zx + y3 * t.zy + z3 * t.zz + t.zo;
		float zz4 = x4 * t.zx + y4 * t.zy + z4 * t.zz + t.zo;
		float zz5 = x5 * t.zx + y5 * t.zy + z5 * t.zz + t.zo;
		float zz6 = x6 * t.zx + y6 * t.zy + z6 * t.zz + t.zo;
		float zz7 = x7 * t.zx + y7 * t.zy + z7 * t.zz + t.zo;
		float zz8 = x8 * t.zx + y8 * t.zy + z8 * t.zz + t.zo;
		x1 = xx1;
		x2 = xx2;
		x3 = xx3;
		x4 = xx4;
		x5 = xx5;
		x6 = xx6;
		x7 = xx7;
		x8 = xx8;
		y1 = yy1;
		y2 = yy2;
		y3 = yy3;
		y4 = yy4;
		y5 = yy5;
		y6 = yy6;
		y7 = yy7;
		y8 = yy8;
		z1 = zz1;
		z2 = zz2;
		z3 = zz3;
		z4 = zz4;
		z5 = zz5;
		z6 = zz6;
		z7 = zz7;
		z8 = zz8;
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
		_Element8Post o = null;
		try {
			o = (_Element8Post) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		if (add)
			out.add3D(o);

		return o;
	}

	public Vector3D getCenter() {
		Vector3D s = new Vector3D(x1 + x2 + x3 + x4 + x5 + x6 + x7 + x8, y1
				+ y2 + y3 + y4 + y5 + y6 + y7 + y8, z1 + z2 + z3 + z4 + z5 + z6
				+ z7 + z8);

		s.scale(1.0f / 8.0f);

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
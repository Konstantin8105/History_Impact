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

import java.awt.geom.*;
import javax.swing.tree.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import gui.*;

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy
 */

public class _BgImage extends _Object implements Serializable {
	public int x, y;
	public BufferedImage img;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeFloat(x);
		out.writeFloat(y);
		out.writeObject(img);
		out.writeObject(Id);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		x = in.readInt();
		y = in.readInt();
		img = (BufferedImage) in.readObject();
		Id = (String) in.readObject();
		selected = false;
	}

	public void reset(boolean do_mesh) {
	};

	public void mesh(int type, float size) {
	}

	public _BgImage(BufferedImage i, int xx, int yy) {
		img = i;
		x = xx;
		y = yy;
	}

	public _BgImage(BufferedImage i) {
		img = i;
		x = 0;
		y = 0;
	}

	public Object[] get_Array(Canvas3D j3d) {
		shp t = new shpBgImage(img, x, y);
		t.setShow(show);
		Object[] arr = { t };
		return arr;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean sel) {
		selected = sel;
	}

	public String toString() {
		return "Background Image ID=" + Id;
	}

	public MutableTreeNode get_TreeNode() {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(this);
		// node.add(new DefaultMutableTreeNode("Color"));
		return node;
	}

	public void transform3D(Matrix3D t) {
		if (!selected || processed)
			return;
		int xx = (int) (x * t.xx + y * t.xy + t.xo);
		int yy = (int) (x * t.yx + y * t.yy + t.yo);
		x = xx;
		y = yy;
	}

	public JPanel getEditPanel(Canvas3D j3d, PreProcessor pp) {
		J3D = j3d;
		PreP = pp;
		return new JPanel();
	}

	public _Node[] get_Nodes() {
		return null;
	}

	public _Object[] get_Elements() {
		return null;
	}

	public _Object duplicate(Canvas3D out, boolean add) {
		_BgImage o = null;
		try {
			o = (_BgImage) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		if (add)
			out.add3D(o);

		return o;
	}

	public boolean isPickPoint(int x, int y, boolean show, boolean ogl) {

		return false;
	}

	public boolean isPickPoint(Rectangle2D r, boolean show, boolean ogl) {

		return false;
	}

	public void deselectRequiredObjects() {
		// Do nothing
	}

	public void replaceObjectWith(_Object o, _Object replacement) {
		// Do nothing
	}

}
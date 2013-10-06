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
//import java.util.*;
import javax.swing.tree.*;

import java.io.*;
import java.util.Vector;

/**
 * This is a ControlPoint for NURBS.
 * 
 * All coordinates are represented in weighted control points i.e.
 * {w*x,w*y,w*z,w}
 * 
 * @author: Jonas Forssell
 */

public class _CtrlPoint extends _Point implements Serializable {
	public float w = 1.0f;

	public _CtrlPoint() {
		super(false);
	}

	public _CtrlPoint(float xx, float yy, float zz) {
		super(xx, yy, zz);
	}

	public _CtrlPoint(float xx, float yy, float zz, Color cl) {
		super(xx, yy, zz, cl);
	}

	public _CtrlPoint(float xx, float yy, float zz, float weight, Color cl) {
		super(xx, yy, zz, cl);
		w = weight;
	}

	public _CtrlPoint(_CtrlPoint p) {
		super(p.x, p.y, p.z, p.color);
		this.w = p.w;
	}

	public void setTo(_CtrlPoint p) {
		this.x = p.x;
		this.y = p.y;
		this.z = p.z;
		this.color = p.color;
		this.w = p.w;
	}

	public void applyWeight() {
		this.x *= this.w;
		this.y *= this.w;
		this.z *= this.w;
	}

	public void stripWeight() {
		this.x /= this.w;
		this.y /= this.w;
		this.z /= this.w;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeFloat(x);
		out.writeFloat(y);
		out.writeFloat(z);
		out.writeFloat(w);
		out.writeObject(color);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		x = in.readFloat();
		y = in.readFloat();
		z = in.readFloat();
		w = in.readFloat();
		color = (Color) in.readObject();
		selected = false;

	}

	public String toString() {
		return "CtrlPoint ID=" + Id;
	}

	public MutableTreeNode get_TreeNode() {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(this);
		node.add(new DefaultMutableTreeNode("Color - (" + color.getRed() + ","
				+ color.getGreen() + "," + color.getBlue() + ")"));
		node.add(new DefaultMutableTreeNode("X,Y,Z - (" + x + "," + y + "," + z
				+ ")"));
		node.add(new DefaultMutableTreeNode("Weight - (" + w + ")"));
		return node;
	}

	public _Node[] get_Nodes() {
		return null;
	}

	public _Object[] get_Elements() {
		return null;
	}

	public void addSelectedObjects(Vector v) {
		if (selected)
			v.add(this);
	}

	public void transform3D(Matrix3D t) {

		this.stripWeight();
		super.transform3D(t);
		this.applyWeight();
	}

}
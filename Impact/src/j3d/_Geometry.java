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

import gui.PreProcessor;

import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;
import javax.swing.tree.MutableTreeNode;

public class _Geometry extends _Object {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public String writeObject() {
		// This method should be overridden for each subclass
		return null;
	}

	public Object[] get_Array(Canvas3D j3d) {
		// TODO Auto-generated method stub
		return null;
	}

	public void reset(boolean do_mesh) {
		// TODO Auto-generated method stub

	}

	public _Node[] get_Nodes() {
		// TODO Auto-generated method stub
		return null;
	}

	public _Object[] get_Elements() {
		// TODO Auto-generated method stub
		return null;
	}

	public void transform3D(Matrix3D trans) {
		// TODO Auto-generated method stub

	}

	public void mesh(int type, float size) {
		// TODO Auto-generated method stub

	}

	public void deselectRequiredObjects() {
		// TODO Auto-generated method stub

	}

	public boolean isSelected() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isPickPoint(int x, int y, boolean show, boolean openGL) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isPickPoint(Rectangle2D r, boolean show, boolean openGL) {
		// TODO Auto-generated method stub
		return false;
	}

	public void setSelected(boolean sel) {
		// TODO Auto-generated method stub

	}

	public MutableTreeNode get_TreeNode() {
		// TODO Auto-generated method stub
		return null;
	}

	public JPanel getEditPanel(Canvas3D j3d, PreProcessor pp) {
		// TODO Auto-generated method stub
		return null;
	}

	public _Object duplicate(Canvas3D j3d, boolean add) {
		// TODO Auto-generated method stub
		return null;
	}

	public void replaceObjectWith(_Object o, _Object replacement) {
		// Do nothing. Implemented in subclasses
	}

}

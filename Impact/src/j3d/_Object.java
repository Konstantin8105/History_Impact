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

import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import javax.swing.tree.*;
import javax.swing.*;
import gui.*;

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy
 */

public abstract class _Object implements Cloneable {
	public int master_type = Canvas3D.NONE;
	public int geometry_type = Canvas3D.NONE;
	protected String Id;
	public boolean selected = false;
	public boolean processed = false;
	protected boolean add = false;
	protected int input_pointer = 0;
	protected boolean show = true;
	protected PreProcessor PreP;
	protected Canvas3D J3D;
	protected Circle circle;

	// *******************************************************

	public abstract Object[] get_Array(Canvas3D j3d);

	public abstract void reset(boolean do_mesh);

	public abstract _Node[] get_Nodes();

	public abstract _Object[] get_Elements();

	public abstract void replaceObjectWith(_Object o, _Object replacement);

	public abstract void transform3D(Matrix3D trans);

	public abstract void mesh(int type, float size);

	public abstract void deselectRequiredObjects();

	public abstract boolean isSelected();

	public abstract boolean isPickPoint(int x, int y, boolean show,
			boolean openGL);

	public abstract boolean isPickPoint(Rectangle2D r, boolean show,
			boolean openGL);

	public abstract void setSelected(boolean sel);

	public String get_Id() {
		return Id;
	}

	public void set_Id(String id) {
		Id = id;
	}

	public abstract MutableTreeNode get_TreeNode();

	public abstract JPanel getEditPanel(Canvas3D j3d, PreProcessor pp);

	public abstract _Object duplicate(Canvas3D j3d, boolean add);

	public int getMaster_type() {
		return master_type;
	}

	public void setMaster_type(int master_type) {
		this.master_type = master_type;
	}

	public int getGeometryType() {
		return geometry_type;
	}

	public void setGeometryType(int type) {
		this.geometry_type = type;
	}

	public Vector3D getCenter() {
		return new Vector3D(0.0f, 0.0f, 0.0f);
	}

	public boolean isProcessed() {
		return processed;
	}

	public void setProcessed(boolean processed) {
		this.processed = processed;
	}

	public void requestFocus() {

	}

	public void requestAction() {

	}

	public void setShow(boolean s) {
		show = s;
	}

	public boolean isShow() {
		return show;
	}

	public _Object[] getBorderObjects() {
		return null;
	}

	protected void checkDefaultKey(KeyEvent e) {

		if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			PreP.clearEditPanel();

	}

	public void setCircle(Circle c) {
		circle = c;
	}

	public Circle getCircle() {
		return circle;
	}

}
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
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;

import util.QSortZ;

import gui.PreProcessor;
import util.PngEncoderB;

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy, Jonas Forssell
 */

public class Canvas3DSW extends JPanel implements Canvas3D, Serializable {
	private DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode();
	public DefaultTreeModel tree3d = new DefaultTreeModel(treeNode);
	private BufferedImage offscreen;
	private Graphics2D offgraphics;
	boolean painted = false;
	private Vector obj3d = new Vector();
	private Vector shp3d = new Vector();
	private int id_obj3d = 0;
	public Matrix3D VMatrix3D = new Matrix3D();
	public Vector3D center_of_rotation = new Vector3D();
	public JLabel coodinates = new JLabel();
	private QSortZ qsort = new QSortZ();
	private int x, y, x0, y0;
	private PreProcessor pre;
	private boolean show = true;
	//
	public boolean DRAFTMODE = false;
	public Color BGCOLOR = Color.white;
	public byte GRAPHICSMODE = 2;
	public Color GRIDCOLOR = Color.black;
	public float GRIDLEVEL = 0;
	public byte GRIDPLANE = 0;
	public boolean GRIDMODE = true;
	public float GRIDSIZE = 10;
	public float[] LIMITS = { 0, 0, 210, 297 };
	public byte RENDERMODE = 2;
	public boolean SHOW_ID_NODE = false;
	public boolean SHOW_ID_ELEMENT = false;
	public boolean SHOW_ID_CONSTRAINTS = true;
	public boolean SHOW_ID_LOADS = true;
	public boolean SHOW_ID_TRACKERS = true;
	public boolean SHOW_ID_MATERIALS = true;
	public int POINTSIZE = 8;
	public int NODESIZE = 6;
	public Color STLCOLOR = new Color(0.7f, 0.7f, 0.7f);
	private boolean DRAG = false;
	// Codes for handling of geometry
	public float GeometricTolerance = 0.01f; // Tolerance for calculation of
												// geometry
	public float NODE_MERGE_TOLERANCE = 1e-3F;
	//
	private _Group nodes, elements, geometry;

	public Canvas3DSW(PreProcessor pre) {
		this.pre = pre;

		createGroups();

		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				offscreen = null;
			}
		});

		addMouseListener(new MouseAdapter() {

			public void mousePressed(MouseEvent e) {
				int mod = e.getModifiers();
				x = e.getX();
				y = e.getY();
				x0 = x;
				y0 = y;
			}

			public void mouseReleased(MouseEvent e) {
				int mod = e.getModifiers();
				if (((mod & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK)
						&& DRAG == true) {
					boxSelect(Math.min(x0, x), Math.min(y0, y),
							Math.max(x0, x), Math.max(y0, y));

					view_reset();
				}

				DRAG = false;
				DRAFTMODE = false;
			}

			public void mouseClicked(MouseEvent e) {
				int mod = e.getModifiers();
				if ((((mod & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) && ((mod & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK))
						|| ((mod & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK)) {
					if (!e.isControlDown()) {
						clearSelectOnAllObjects3D();
						clearTree();
					}

					setPickedObject(e.getX(), e.getY());

					view_reset();

					if (e.getClickCount() == 2)
						showEditPanel(e);

					requestAction();
				}

				if (((mod & InputEvent.BUTTON2_MASK) == InputEvent.BUTTON2_MASK)) {
					view_all();
				}
			}
		});

		addMouseMotionListener(new MouseMotionAdapter() {

			public void mouseMoved(MouseEvent e) {
				coodinates.setText(e.getX() + "," + e.getY());
			}

			public void mouseDragged(MouseEvent e) {
				if (e.getModifiers() == MouseEvent.BUTTON1_MASK)
					DRAG = true;
				else
					DRAFTMODE = true;
				this_mouseDragged(e);
			}
		});

		VMatrix3D.translate(400, -300, 0);
		VMatrix3D.scale(1, -1, 1);
		view_grid();
	}

	private void createGroups() {
		nodes = new _Group(false, true);
		nodes.setName("Node");
		elements = new _Group(false, true);
		elements.setName("Elem");
		geometry = new _Group(false, true);
		geometry.setName("Geo");

		this.add3D(nodes);
		this.add3D(elements);
		this.add3D(geometry);
	}

	private synchronized void setPainted() {
		painted = true;
		notifyAll();
	}

	public synchronized void paint(Graphics g) {
		shp tmp;

		if (offscreen == null) {
			if (offgraphics != null)
				offgraphics.dispose();
			if (offscreen != null)
				offscreen.flush();
			offscreen = (BufferedImage) createImage(getWidth(), getHeight());
			offgraphics = (Graphics2D) offscreen.getGraphics();
		}

		// Create background
		offgraphics.setColor(BGCOLOR);
		offgraphics.fillRect(0, 0, getWidth(), getHeight());

		// Draw each element
		for (int i = 0; i < shp3d.size(); i++) {
			tmp = (shp) shp3d.elementAt(i);
			if (tmp.isShow() == show)
				tmp.paint(offgraphics, this);
		}

		// Draw bounding box if used
		if (DRAG == true) {
			offgraphics.setColor(Color.RED);
			offgraphics.drawRect(Math.min(x0, x), Math.min(y0, y),
					Math.abs(x - x0), Math.abs(y - y0));
		}

		g.drawImage(offscreen, 0, 0, this);
		setPainted();
	}

	public void update(Graphics g) {
		paint(g);
	}

	private synchronized void view_transform() {
		shp tmp;
		int count = shp3d.size();
		for (int i = 0; i < count; i++) {
			tmp = (shp) shp3d.elementAt(i);
			if (tmp.isShow() == show)
				tmp.transform2D(VMatrix3D, center_of_rotation, this);
		}

		if (!DRAFTMODE)
			qsort.sort(shp3d);
	}

	public int add3D(Object obj) {
		id_obj3d++;
		((_Object) obj).set_Id(id_obj3d + "");

		try {
			((_Object) obj).reset(true);
		} catch (Exception e1) {
			error(e1);
			return -1;
		}

		obj3d.addElement(obj);

		_Object[] o = new _Object[1];
		o[0] = (_Object) obj;

		if (obj instanceof _Node)
			nodes.addToGroup(o);
		else if (obj instanceof _Element)
			elements.addToGroup(o);
		else if (obj instanceof _Geometry)
			geometry.addToGroup(o);
		else {
			treeNode.add(((_Object) obj).get_TreeNode());
		}

		Object[] arr = ((_Object) obj).get_Array(this);
		for (int i = 0; i < arr.length; i++) {
			((shp) arr[i]).transform2D(VMatrix3D, center_of_rotation, this);
			shp3d.addElement(arr[i]);
		}
		return id_obj3d;
	}

	private void clearTree() {
		if (pre != null)
			pre.clearTree();
	}

	private void showEditPanel(MouseEvent e) {
		if (pre != null)
			pre.showEditPanel(e);
	}

	private void setPickedObject(int x, int y) {
		int count = obj3d.size();
		_Object o;

		for (int i = 0; i < count; i++) {
			o = ((_Object) obj3d.elementAt(i));
			if (o.isPickPoint(x, y, show, false))
				o.setSelected(true);
		}

		if (pre != null)
			pre.rescanTree();
	}

	private void boxSelect(int sx, int sy, int ex, int ey) {
		int count = obj3d.size();
		_Object o;

		Rectangle2D r = new Rectangle(sx, sy, ex - sx, ey - sy);

		for (int i = 0; i < count; i++) {
			o = ((_Object) obj3d.elementAt(i));
			if (o.isPickPoint(r, show, false))
				o.setSelected(true);
		}

		if (pre != null)
			pre.rescanTree();
	}

	public _Object getSelectedObject3D() {
		_Object[] obj = getSelectedObjects3D();

		if (obj.length > 0)
			return obj[0];

		return null;
	}

	public _Object[] getSelectedObjects3D() {
		Vector v = new Vector();
		_Object o;
		_Object[] p;

		for (int i = 0; i < obj3d.size(); i++) {
			o = ((_Object) obj3d.elementAt(i));
			if (o.isSelected())
				v.add(o);
		}

		p = new _Object[v.size()];
		for (int i = 0; i < v.size(); i++)
			p[i] = (_Object) v.elementAt(i);

		return p;
	}

	public _Object[] getAllObjects3D() {
		_Object[] arr = new _Object[obj3d.size()];
		for (int i = 0; i < obj3d.size(); i++)
			arr[i] = ((_Object) obj3d.elementAt(i));
		return arr;
	}

	public _Node[] getAllNodes3D() {
		Vector arr = new Vector();
		_Object o;

		for (int i = 0; i < obj3d.size(); i++) {
			o = ((_Object) obj3d.elementAt(i));
			if (o instanceof _Node)
				arr.add(o);
		}

		// Generate array
		_Node[] out = new _Node[arr.size()];
		for (int i = 0; i < out.length; i++)
			out[i] = (_Node) arr.elementAt(i);

		return out;
	}

	public _Object[] getAllElements3D() {
		Vector arr = new Vector();
		_Object o;

		for (int i = 0; i < obj3d.size(); i++) {
			o = ((_Object) obj3d.elementAt(i));
			if (o instanceof _Element)
				arr.add(o);
		}

		// Generate array
		_Object[] out = new _Object[arr.size()];
		for (int i = 0; i < out.length; i++)
			out[i] = (_Element) arr.elementAt(i);

		return out;
	}

	public _Geometry[] getAllGeometry3D() {
		Vector arr = new Vector();
		_Object o;

		for (int i = 0; i < obj3d.size(); i++) {
			o = ((_Object) obj3d.elementAt(i));
			if (o instanceof _Geometry)
				arr.add(o);
		}

		// Generate array
		_Geometry[] out = new _Geometry[arr.size()];
		for (int i = 0; i < out.length; i++)
			out[i] = (_Geometry) arr.elementAt(i);

		return out;
	}

	public _Group[] getAllGroups3D() {
		Vector arr = new Vector();
		_Object o;

		for (int i = 0; i < obj3d.size(); i++) {
			o = ((_Object) obj3d.elementAt(i));
			if (o instanceof _Group)
				arr.add(o);
		}

		_Group[] out = new _Group[arr.size()];
		for (int i = 0; i < out.length; i++)
			out[i] = (_Group) arr.elementAt(i);

		return out;
	}

	public void remove_all() {
		id_obj3d = 0;
		obj3d.removeAllElements();
		shp3d.removeAllElements();
		treeNode.removeAllChildren();
		this.createGroups();
		tree3d.reload();
		view_grid();
		repaint();
		painted = true;
	}

	public void clearSelectOnAllObjects3D() {
		int count = obj3d.size();
		for (int i = 0; i < count; i++)
			((_Object) obj3d.elementAt(i)).setSelected(false);
	}

	public void removeSelectedObjects3D() {
		_Object o;
		_Object[] s;
		/*
		 * treeNode.removeAllChildren(); for(int i=0; i<obj3d.size(); i++) { o =
		 * (_Object)obj3d.elementAt(i); if(o.isSelected()){ for (int j=0;
		 * j<obj3d.size(); j++) // Remove from all groups if (obj3d.elementAt(j)
		 * instanceof _Group) ((_Group)obj3d.elementAt(j)).removeFromGroup(o);
		 * obj3d.removeElementAt(i); i--; } else
		 * treeNode.add(((_Object)obj3d.elementAt(i)).get_TreeNode()); }
		 * tree3d.reload(); view_reset();
		 */

		// Deselect required elements
		for (int i = 0; i < obj3d.size(); i++)
			((_Object) obj3d.elementAt(i)).deselectRequiredObjects();

		// Remove selected objects from all groups
		s = this.getSelectedObjects3D();

		for (int i = 0; i < obj3d.size(); i++) {
			o = (_Object) obj3d.elementAt(i);
			if (o instanceof _Group)
				((_Group) o).removeFromGroup(s);
		}

		// Remove selected objects from j3d
		for (int i = 0; i < obj3d.size(); i++) {
			o = (_Object) obj3d.elementAt(i);
			if (o.isSelected()) {
				obj3d.remove(i);
				i--;
			}
		}

		this.tree_reset();
		this.view_reset();

	}

	public void duplicate() {
		int count = obj3d.size();
		_Object o;
		Vector tmp = new Vector();

		for (int i = 0; i < count; i++) {
			o = (_Object) obj3d.elementAt(i);
			if (o.isSelected()) {
				tmp.add(o);
			}
		}

		for (int i = 0; i < tmp.size(); i++)
			((_Object) tmp.elementAt(i)).duplicate(this, true);

		tree3d.reload();
		view_reset();
	}

	public void transform3D(Matrix3D m) {
		int count = obj3d.size();

		_Object[] p = getSelectedObjects3D();

		for (int i = 0; i < p.length; i++)
			if (!p[i].isProcessed()) {
				p[i].transform3D(m);
				p[i].setProcessed(true);
			}

		for (int i = 0; i < count; i++)
			((_Object) obj3d.elementAt(i)).setProcessed(false);

		view_reset();
		view_repaint();
		tree_reset();

	}

	public void tree_reset() {
		_Object o;

		treeNode.removeAllChildren();
		int count = obj3d.size();
		for (int i = 0; i < count; i++) {
			o = (_Object) obj3d.elementAt(i);
			if (o instanceof _Group)
				if (((_Group) o).isTopgroup())
					treeNode.add(o.get_TreeNode());
		}

		tree3d.reload();
	}

	public synchronized void view_reset() {
		int count = obj3d.size();
		shp3d.removeAllElements();
		for (int i = 0; i < count; i++) {
			Object[] arr = ((_Object) obj3d.elementAt(i)).get_Array(this);
			for (int j = 0; j < arr.length; j++) {
				((shp) arr[j]).transform2D(VMatrix3D, center_of_rotation, this);
				shp3d.addElement(arr[j]);
			}
		}
		view_grid();
	}

	public synchronized void view_repaint() {
		if (painted) {
			painted = false;
			view_transform();
			repaint();
		}
	}

	public synchronized void view_translate(float dx, float dy, float dz) {
		VMatrix3D.translate(dx, -dy, dz);
		view_repaint();
	}

	public synchronized void view_xrot(double theta) {
		float dx = VMatrix3D.xo;
		float dy = VMatrix3D.yo;
		float dz = VMatrix3D.zo;
		VMatrix3D.translate(-dx, -dy, -dz);
		VMatrix3D.xrot(theta);
		VMatrix3D.translate(dx, dy, dz);
		view_repaint();
	}

	public synchronized void view_yrot(double theta) {
		float dx = VMatrix3D.xo;
		float dy = VMatrix3D.yo;
		float dz = VMatrix3D.zo;
		VMatrix3D.translate(-dx, -dy, -dz);
		VMatrix3D.yrot(theta);
		VMatrix3D.translate(dx, dy, dz);
		view_repaint();
	}

	public synchronized void view_zrot(double theta) {
		float dx = VMatrix3D.xo;
		float dy = VMatrix3D.yo;
		float dz = VMatrix3D.zo;
		VMatrix3D.translate(-dx, -dy, -dz);
		VMatrix3D.zrot(theta);
		VMatrix3D.translate(dx, dy, dz);
		view_repaint();
	}

	public synchronized void view_top() {
		VMatrix3D.view_top();
		view_repaint();
	}

	public synchronized void view_bottom() {
		VMatrix3D.view_bottom();
		view_repaint();
	}

	public synchronized void view_ne() {
		VMatrix3D.view_ne();
		view_repaint();
	}

	public synchronized void view_sw() {
		VMatrix3D.view_sw();
		view_repaint();
	}

	public synchronized void view_se() {
		VMatrix3D.view_se();
		view_repaint();
	}

	public synchronized void view_nw() {
		VMatrix3D.view_nw();
		view_repaint();
	}

	public synchronized void view_left() {
		VMatrix3D.view_left();
		view_repaint();
	}

	public synchronized void view_right() {
		VMatrix3D.view_right();
		view_repaint();
	}

	public synchronized void view_front() {
		VMatrix3D.view_front();
		view_repaint();
	}

	public synchronized void view_back() {
		VMatrix3D.view_back();
		view_repaint();
	}

	public synchronized void view_all() {

		int h = this.getHeight();
		int w = this.getWidth();
		shp shp;
		float[] bound;
		boolean zoomeable = false;

		float xmin = java.lang.Float.MAX_VALUE;
		float ymin = java.lang.Float.MAX_VALUE;
		float xmax = java.lang.Float.MIN_VALUE;
		float ymax = java.lang.Float.MIN_VALUE;

		for (int i = 0; i < shp3d.size(); i++) {
			try {
				shp = (shp) shp3d.elementAt(i);

				if (!(shp instanceof shpPointGrid)
						&& !(shp instanceof shpOrientedText)) {

					bound = shp.get2DBoundaries();
					xmax = Math.max(xmax, bound[0]);
					xmin = Math.min(xmin, bound[1]);
					ymax = Math.max(ymax, bound[2]);
					ymin = Math.min(ymin, bound[3]);

					zoomeable = true;
				}
			} catch (Exception e) {
			}
		}

		// Check if there is something to zoom around?
		if (zoomeable == false)
			return;

		double sw = w / (xmax - xmin);
		double sh = h / (ymax - ymin);
		double s = 0.8 * Math.min(sh, sw);

		view_scale((float) s);

		xmin = java.lang.Float.MAX_VALUE;
		ymin = java.lang.Float.MAX_VALUE;
		xmax = java.lang.Float.MIN_VALUE;
		ymax = java.lang.Float.MIN_VALUE;

		for (int i = 0; i < shp3d.size(); i++) {
			try {
				shp = (shp) shp3d.elementAt(i);

				if (!(shp instanceof shpPointGrid)
						&& !(shp instanceof shpOrientedText)) {
					bound = shp.get2DBoundaries();
					xmax = Math.max(xmax, bound[0]);
					xmin = Math.min(xmin, bound[1]);
					ymax = Math.max(ymax, bound[2]);
					ymin = Math.min(ymin, bound[3]);
				}
			} catch (Exception e) {
			}
		}

		int mx = (int) ((w / 2.0f) - (xmax + xmin) / 2.0f);
		int my = (int) ((h / 2.0f) - (ymax + ymin) / 2.0f);

		this.view_translate(mx, -my, 0);

		painted = true;
		view_repaint();

	}

	public synchronized boolean showhide() {

		_Object[] obj = this.getSelectedObjects3D();
		if (obj.length != 0)
			for (int i = 0; i < obj.length; i++)
				obj[i].setShow(!show);
		else
			show = !show;

		clearSelectOnAllObjects3D();
		if (pre != null)
			pre.clearTree();
		view_reset();
		view_repaint();

		return show;
	}

	public synchronized void view_grid() {
		for (int i = 0; i < shp3d.size(); i++) {
			if (shp3d.elementAt(i) instanceof shpPointGrid) {
				shp3d.removeElementAt(i);
				i--;
			}
		}
		if (GRIDMODE)
			for (float i = LIMITS[0]; i <= LIMITS[2]; i += GRIDSIZE) {
				for (float j = LIMITS[1]; j <= LIMITS[3]; j += GRIDSIZE) {
					float x, y, z;
					if (GRIDPLANE == GRIDPLANE_XY) {
						x = i;
						y = j;
						z = GRIDLEVEL;
					} else if (GRIDPLANE == GRIDPLANE_YZ) {
						x = GRIDLEVEL;
						y = j;
						z = i;
					} else {
						x = i;
						y = GRIDLEVEL;
						z = j;
					}
					shpPointGrid gp = new shpPointGrid(x, y, z, GRIDCOLOR);
					gp.setShow(show);
					gp.transform2D(VMatrix3D, center_of_rotation, this);
					shp3d.addElement(gp);
				}
			}
		shpXYZ xyz = new shpXYZ();
		xyz.transform2D(VMatrix3D, center_of_rotation, this);
		shp3d.addElement(xyz);
		view_repaint();
	}

	public synchronized void view_scale(float s) {

		float dx = VMatrix3D.xo;
		float dy = VMatrix3D.yo;
		float dz = VMatrix3D.zo;
		VMatrix3D.translate(-dx, -dy, -dz);
		VMatrix3D.scale(s);
		VMatrix3D.translate(dx, dy, dz);
		view_repaint();
	}

	void this_mouseDragged(MouseEvent e) {
		int mod = e.getModifiers();

		if (((mod & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)
				&& ((mod & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK)
				&& ((mod & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK)) {
			this.view_zrot(e.getY() - y);
		} else if (((mod & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)
				&& ((mod & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK)) {
			this.view_yrot(e.getX() - x);
			this.view_xrot(e.getY() - y);
		} else if (((mod & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)
				&& ((mod & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK)) {
			if (y - e.getY() > 0)
				this.view_scale(0.9f);
			if (y - e.getY() < 0)
				this.view_scale(1.1f);
		} else if (((mod & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)) {
			this.view_translate(e.getX() - x, y - e.getY(), 0);
		} else if (((mod & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK)
				&& DRAG == true)
			view_repaint();

		x = e.getX();
		y = e.getY();
	}

	public void save(String file) {
		try {
			FileOutputStream ostream = new FileOutputStream(file);
			ObjectOutputStream p = new ObjectOutputStream(ostream);
			p.writeObject(VMatrix3D);
			for (int i = 0; i < obj3d.size(); i++)
				p.writeObject(obj3d.elementAt(i));
			p.flush();
			ostream.close();
		} catch (FileNotFoundException fnfe) {
			System.err.println("File not found: " + file);
		} catch (IOException ioe) {
			System.err.println("Could not write file: " + file);
			ioe.printStackTrace();
		}
	}

	public void save(ObjectOutputStream oos) {
		try {
			oos.writeObject(VMatrix3D);
			for (int i = 0; i < obj3d.size(); i++)
				oos.writeObject(obj3d.elementAt(i));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void open(String file) {
		try {
			FileInputStream istream = new FileInputStream(file);
			ObjectInputStream p = new ObjectInputStream(istream);
			remove_all();
			VMatrix3D = (Matrix3D) p.readObject();
			Object obj;
			try {
				while ((obj = p.readObject()) != null)
					add3D(obj);
			} catch (EOFException eof) {
			}
			view_all();
			p.close();
			istream.close();
		} catch (Exception ioe) {
			System.err.println("Could not open file: " + file + "\n" + ioe);
		}
	}

	public void open(ObjectInputStream ois) {
		try {
			// remove_all();
			VMatrix3D = (Matrix3D) ois.readObject();
			Object obj;
			try {
				while ((obj = ois.readObject()) != null)
					add3D(obj);
			} catch (EOFException eof) {
			}
			view_all();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void save_Image(String file) {
		byte[] pngbytes;
		PngEncoderB png;

		try {
			if (file.toLowerCase().indexOf(".png") == -1)
				file += ".png";
			FileOutputStream out = new FileOutputStream(file);
			BufferedImage image = (BufferedImage) createImage(getWidth(),
					getHeight());
			Graphics g = image.getGraphics();
			paint(g);

			png = new PngEncoderB(image, false, 0, 5); // Encode at level 5
														// compression
			pngbytes = png.pngEncode();
			out.write(pngbytes);

			g.dispose();
			out.flush();
			out.close();
		} catch (FileNotFoundException fnfe) {
			System.err.println("File not found: " + file);
		} catch (IOException ioe) {
			System.err.println("Could not write file: " + file);
		}
	}

	public void open_stl(String file) {
		this.add3D(new _Stl(file));
		view_repaint();
	}

	public void error(Object st) {
		JOptionPane.showMessageDialog(null, "Error: " + st, "Error!",
				JOptionPane.ERROR_MESSAGE);
		if (st instanceof Exception)
			((Exception) st).printStackTrace();
		else
			System.out.println("Error: " + st);
	}

	public void rebuild() {
		int count = obj3d.size();
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < count; j++) {
				_Object obj = ((_Object) obj3d.elementAt(j));
				if (obj.getGeometryType() == i)
					obj.reset(true);
			}
		view_reset();
		view_repaint();
		tree_reset();
	}

	public void setCenterOfRotation() {
		_Object obj = this.getSelectedObject3D();
		if (obj != null) {

			center_of_rotation = obj.getCenter();
			center_of_rotation.sub(new Vector3D(0.0f, 0.0f, 0.0f),
					center_of_rotation);

		}
		view_reset();
		view_repaint();
	}

	public void addBorderObjects() {
		_Object[] obj = this.getSelectedObjects3D();
		_Object[] add = null;
		if (obj != null)
			for (int i = 0; i < obj.length; i++) {
				add = obj[i].getBorderObjects();
				if (add != null)
					for (int j = 0; j < add.length; j++)
						this.add3D(add[j]);
			}
		view_reset();
		view_repaint();
	}

	public void intersectObjects() throws IllegalStateException {
		_Object[] obj = this.getSelectedObjects3D();
		_NurbCurve[] o = new _NurbCurve[obj.length];
		_NurbSurface[] s = new _NurbSurface[obj.length];
		_Point[] p = null;
		_NurbCurve[] c = null;
		int j = 0, k = 0;

		if (obj != null)
			if (obj.length > 1)
				for (int i = 0; i < obj.length; i++) {
					if (obj[i] instanceof _NurbCurve
							&& !lib3D.contains(o, obj[i]))
						o[j++] = (_NurbCurve) obj[i];
					if (obj[i] instanceof _NurbSurface
							&& !lib3D.contains(s, obj[i]))
						s[k++] = (_NurbSurface) obj[i];
				}

		if (j == 2) {
			try {
				p = lib3D.intersectCurves(o[0], o[1], GeometricTolerance);
			} catch (IllegalStateException e) {
				throw new IllegalStateException("No intersection found");
			}
		} else if (j == 1 && k == 1) {
			try {
				p = lib3D.intersectCurveWithSurface(o[0], s[0],
						GeometricTolerance);
			} catch (IllegalStateException e) {
				throw new IllegalStateException("No intersection found");
			}
		} else if (k == 2) {
			try {
				c = lib3D.intersectSurfaces(s[0], s[1]);
			} catch (IllegalStateException e) {
				throw new IllegalStateException("No intersection found");
			}
		}

		if (p != null)
			if (p.length > 0)
				for (int i = 0; i < p.length; i++)
					this.add3D(p[i]);

		if (c != null)
			if (c.length > 0)
				for (int i = 0; i < c.length; i++)
					this.add3D(c[i]);

		view_reset();
		view_repaint();
	}

	public void projectObjects() throws IllegalStateException {
		_Object[] obj = this.getSelectedObjects3D();
		_Point[] ps = new _Point[obj.length];
		_NurbCurve[] o = new _NurbCurve[obj.length];
		_NurbSurface[] s = new _NurbSurface[obj.length];
		_Point[] p = null;
		_NurbCurve[] c = null;
		int j = 0, k = 0, l = 0;

		if (obj != null)
			if (obj.length > 1)
				for (int i = 0; i < obj.length; i++) {
					if (obj[i] instanceof _Point && !lib3D.contains(ps, obj[i]))
						ps[l++] = (_Point) obj[i];
					if (obj[i] instanceof _NurbCurve
							&& !lib3D.contains(o, obj[i]))
						o[j++] = (_NurbCurve) obj[i];
					if (obj[i] instanceof _NurbSurface
							&& !lib3D.contains(s, obj[i]))
						s[k++] = (_NurbSurface) obj[i];
				}

		if (l == 1 && j == 1) {
			try {
				p = lib3D
						.projectPointOntoCurve(ps[0], o[0], GeometricTolerance);
			} catch (IllegalStateException e) {
				throw new IllegalStateException("No projection found");
			}
		} else if (l == 1 && k == 1) {
			try {
				p = lib3D.projectPointOntoSurface(ps[0], s[0],
						GeometricTolerance);
			} catch (IllegalStateException e) {
				throw new IllegalStateException("No projection found");
			}
		} else if (j == 1 && k == 1) {
			try {
				// c = lib3D.projectCurveOntoSurface(o[0], s[0]);
			} catch (IllegalStateException e) {
				throw new IllegalStateException("No projection found");
			}
		}

		if (p != null)
			if (p.length > 0)
				for (int i = 0; i < p.length; i++)
					this.add3D(p[i]);

		if (c != null)
			if (c.length > 0)
				for (int i = 0; i < c.length; i++)
					this.add3D(c[i]);

		view_reset();
		view_repaint();
	}

	public void breakObjects() throws IllegalStateException {
		_Object[] obj = this.getSelectedObjects3D();
		_Point[] ps = new _Point[obj.length];
		_NurbCurve[] o = new _NurbCurve[obj.length];
		_NurbSurface[] s = new _NurbSurface[obj.length];
		_Point[] p = null;
		_NurbCurve[] c = null;
		int j = 0, k = 0, l = 0;

		if (obj != null)
			if (obj.length > 1)
				for (int i = 0; i < obj.length; i++) {
					if (obj[i] instanceof _Point && !lib3D.contains(ps, obj[i]))
						ps[l++] = (_Point) obj[i];
					if (obj[i] instanceof _NurbCurve
							&& !lib3D.contains(o, obj[i]))
						o[j++] = (_NurbCurve) obj[i];
					if (obj[i] instanceof _NurbSurface
							&& !lib3D.contains(s, obj[i]))
						s[k++] = (_NurbSurface) obj[i];
				}

		if (l == 1 && j == 1) {
			try {
				c = lib3D.breakCurve(o[0], ps[0], GeometricTolerance);
			} catch (IllegalStateException e) {
				throw new IllegalStateException("No break possible");
			}
		} else if (l == 1 && k == 1)
			throw new IllegalStateException("Surface break not supported yet");

		else if (j == 1 && k == 1)
			throw new IllegalStateException(
					"Surface break using a curve is not supported yet");

		if (p != null)
			if (p.length > 0)
				for (int i = 0; i < p.length; i++)
					this.add3D(p[i]);

		if (c != null)
			if (c.length > 0)
				for (int i = 0; i < c.length; i++)
					this.add3D(c[i]);

		view_reset();
		view_repaint();
	}

	private void requestAction() {

		if (pre != null)
			pre.requestAction();
	}

	public boolean getSHOW_ID_NODE() {
		return SHOW_ID_NODE;
	}

	public void setSHOW_ID_NODE(boolean show_id_node) {
		SHOW_ID_NODE = show_id_node;
	}

	public Color getBGCOLOR() {
		return BGCOLOR;
	}

	public void setBGCOLOR(Color bgcolor) {
		BGCOLOR = bgcolor;
	}

	public boolean getDRAFTMODE() {
		return DRAFTMODE;
	}

	public void setDRAFTMODE(boolean draftmode) {
		DRAFTMODE = draftmode;
	}

	public byte getGRAPHICSMODE() {
		return GRAPHICSMODE;
	}

	public void setGRAPHICSMODE(byte graphicsmode) {
		GRAPHICSMODE = graphicsmode;
	}

	public Color getGRIDCOLOR() {
		return GRIDCOLOR;
	}

	public void setGRIDCOLOR(Color gridcolor) {
		GRIDCOLOR = gridcolor;
	}

	public float getGRIDLEVEL() {
		return GRIDLEVEL;
	}

	public void setGRIDLEVEL(float gridlevel) {
		GRIDLEVEL = gridlevel;
	}

	public boolean getGRIDMODE() {
		return GRIDMODE;
	}

	public void setGRIDMODE(boolean gridmode) {
		GRIDMODE = gridmode;
	}

	public byte getGRIDPLANE() {
		return GRIDPLANE;
	}

	public void setGRIDPLANE(byte gridplane) {
		GRIDPLANE = gridplane;
	}

	public float getGRIDSIZE() {
		return GRIDSIZE;
	}

	public void setGRIDSIZE(float gridsize) {
		GRIDSIZE = gridsize;
	}

	public float[] getLIMITS() {
		return LIMITS;
	}

	public void setLIMITS(float[] limits) {
		LIMITS = limits;
	}

	public float getNODE_MERGE_TOLERANCE() {
		return NODE_MERGE_TOLERANCE;
	}

	public void setNODE_MERGE_TOLERANCE(float node_merge_tolerance) {
		NODE_MERGE_TOLERANCE = node_merge_tolerance;
	}

	public int getNODESIZE() {
		return NODESIZE;
	}

	public void setNODESIZE(int nodesize) {
		NODESIZE = nodesize;
	}

	public int getPOINTSIZE() {
		return POINTSIZE;
	}

	public void setPOINTSIZE(int pointsize) {
		POINTSIZE = pointsize;
	}

	public byte getRENDERMODE() {
		return RENDERMODE;
	}

	public void setRENDERMODE(byte rendermode) {
		RENDERMODE = rendermode;
	}

	public boolean getSHOW_ID_CONSTRAINTS() {
		return SHOW_ID_CONSTRAINTS;
	}

	public void setSHOW_ID_CONSTRAINTS(boolean show_id_constraints) {
		SHOW_ID_CONSTRAINTS = show_id_constraints;
	}

	public boolean getSHOW_ID_ELEMENT() {
		return SHOW_ID_ELEMENT;
	}

	public void setSHOW_ID_ELEMENT(boolean show_id_element) {
		SHOW_ID_ELEMENT = show_id_element;
	}

	public boolean getSHOW_ID_LOADS() {
		return SHOW_ID_LOADS;
	}

	public void setSHOW_ID_LOADS(boolean show_id_loads) {
		SHOW_ID_LOADS = show_id_loads;
	}

	public boolean getSHOW_ID_MATERIALS() {
		return SHOW_ID_MATERIALS;
	}

	public void setSHOW_ID_MATERIALS(boolean show_id_materials) {
		SHOW_ID_MATERIALS = show_id_materials;
	}

	public boolean getSHOW_ID_TRACKERS() {
		return SHOW_ID_TRACKERS;
	}

	public void setSHOW_ID_TRACKERS(boolean show_id_trackers) {
		SHOW_ID_TRACKERS = show_id_trackers;
	}

	public Color getSTLCOLOR() {
		return STLCOLOR;
	}

	public void setSTLCOLOR(Color stlcolor) {
		STLCOLOR = stlcolor;
	}

	public Matrix3D getVMatrix3D() {
		return VMatrix3D;
	}

	public void setVMatrix3D(Matrix3D matrix3D) {
		VMatrix3D = matrix3D;
	}

	public DefaultTreeModel getTree3d() {
		return tree3d;
	}

	public float getGeometricTolerance() {
		return GeometricTolerance;
	}

	public void setGeometricTolerance(float geometricTolerance) {
		GeometricTolerance = geometricTolerance;
	}

	public void replaceAllInstancesOf(_Object o, _Object replacement) {

		// Replace all instances of object o with replacement
		for (int i = 0; i < obj3d.size(); i++)
			((_Object) obj3d.elementAt(i)).replaceObjectWith(o, replacement);

		// Remove object o from database
		obj3d.remove(o);

	}

}
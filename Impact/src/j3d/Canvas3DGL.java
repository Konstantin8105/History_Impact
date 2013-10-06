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
import java.awt.image.*;
import java.io.*;
import java.nio.IntBuffer;
import java.util.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.swing.*;
import javax.swing.tree.*;

import com.sun.opengl.util.j2d.*;
import com.sun.opengl.util.BufferUtil;

import gui.PreProcessor;
import util.PngEncoderB;

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy, Jonas Forssell
 */
public class Canvas3DGL extends GLCanvas implements GLEventListener, Canvas3D,
		Serializable {
	private DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode();
	public DefaultTreeModel tree3d = new DefaultTreeModel(treeNode);
	boolean painted = false;
	private Vector obj3d = new Vector();
	private Vector shp3d = new Vector();
	private int id_obj3d = 0;
	public Matrix3D VMatrix3D = new Matrix3D();
	public Vector3D center_of_rotation = new Vector3D();
	public JLabel coodinates = new JLabel();
	private int x, y, x0, y0;
	private float screenwidth, screenheight, aspw, asph;
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

	// OpenGL specific stuff
	private static int NO_PICK = 0;
	private static int POINT = 1;
	private static int BOX = 2;

	private GLU glu = new GLU();
	private Overlay ovl;
	private Graphics2D g2D;

	private ArcBall arcBall = new ArcBall(640.0f, 480.0f);
	private Matrix4f LastRot = new Matrix4f();
	private Matrix4f ThisRot = new Matrix4f();
	private final Object matrixLock = new Object();
	private float[] matrix = new float[16];
	private GLUquadric quadratic;
	private float scale = 1.0f;
	private float transX, transY, transZ;
	private float left, right, bottom, top, Znear, Zfar;
	private float aspect = 1.0f;
	private boolean genlist;
	private int picking = NO_PICK;
	private IntBuffer selectBuffer;
	private int hits = 0;
	private shp axis = new shpXYZ();
	//
	private _Group nodes, elements, geometry;

	public static void main(String[] args) {
		JFrame frame = new JFrame("Canvas3DGL Demo");

		final GLCanvas canvas = new Canvas3DGL(null);

		frame.getContentPane().add(canvas);
		frame.setSize(300, 300);
		// kill the process when the JFrame is closed
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.show();

		// show what we've done
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				canvas.setVisible(true);
			}
		});
	}

	public Canvas3DGL(PreProcessor pre) {
		this.pre = pre;
		this.setFocusable(false);

		createGroups();

		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
			}
		});

		addGLEventListener(this);

		addMouseListener(new MouseAdapter() {

			public void mousePressed(MouseEvent e) {
				int mod = e.getModifiers();
				x = e.getX();
				y = e.getY();
				x0 = x;
				y0 = y;
				this_mousePressed(e);
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

				view_repaint();
			}

			public void mouseClicked(MouseEvent e) {
				int mod = e.getModifiers();
				if ((((mod & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) && ((mod & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK))
						|| ((mod & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK)) {
					if (!e.isControlDown()) {
						clearSelectOnAllObjects3D();
						clearTree();
					}

					setPickedObject();

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

		view_reset();
	}

	private void createGroups() {
		nodes = new _Group(false, true);
		nodes.setName("Node");
		this.add3D(nodes);

		elements = new _Group(false, true);
		elements.setName("Elem");
		this.add3D(elements);

		geometry = new _Group(false, true);
		geometry.setName("Geo");
		this.add3D(geometry);
	}

	// OpenGL initialization method.
	public void init(GLAutoDrawable drawable) {
		float[] ambient = { 1f, 1f, 1f, 0f };
		float[] diffuse = { 1f, 1f, 1f, 0f };

		GL gl = drawable.getGL();
		ovl = new Overlay(drawable);

		// Start Of User Initialization
		LastRot.setIdentity(); // Reset Rotation
		ThisRot.setIdentity(); // Reset Rotation
		ThisRot.get(matrix);

		gl.glClearColor(1.0f, 1.0f, 1.0f, 0.5f);
		gl.glClearDepth(1.0f);
		gl.glDepthFunc(GL.GL_LEQUAL);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glShadeModel(GL.GL_FLAT);

		gl.glEnable(GL.GL_LIGHTING);

		gl.glLightModeli(GL.GL_LIGHT_MODEL_TWO_SIDE, 1);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, ambient, 0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, diffuse, 0);
		gl.glEnable(GL.GL_LIGHT0);

		gl.glEnable(GL.GL_COLOR_MATERIAL);
		gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE);

		quadratic = glu.gluNewQuadric();
		glu.gluQuadricNormals(quadratic, GLU.GLU_SMOOTH);
		glu.gluQuadricTexture(quadratic, true);

		view_all();
	}

	// OpenGL method called when the canvas is reshaped
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		GL gl = drawable.getGL();
		float[] lightpos = { -1, 1, 1, 0 };
		screenwidth = width;
		screenheight = height;

		gl.glViewport(0, 0, width, height); // Reset The Current Viewport
		gl.glMatrixMode(GL.GL_PROJECTION); // Select The Projection Matrix
		gl.glLoadIdentity(); // Reset The Projection Matrix

		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, lightpos, 0); // Set light

		aspect = width / height;

		arcBall.setBounds((float) width, (float) height); // *NEW* Update mouse
															// bounds for
															// arcball

	}

	// OpenGL method called when the display is updated. Do all drawing here.
	public void display(GLAutoDrawable drawable) {
		boolean shpimage = false;
		shp tmp;
		int[] viewport = new int[4];
		float box_scale_x, box_scale_y, box_x0, box_y0;

		// Set always rectangular aspect of the view
		if (aspect < 1.0f) {
			asph = aspect;
			aspw = 1.0f;
		} else {
			asph = 1.0f;
			aspw = aspect;
		}

		synchronized (matrixLock) {
			ThisRot.get(matrix);
		}

		GL gl = drawable.getGL();

		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);

		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();

		if (picking != NO_PICK) {
			selectBuffer = BufferUtil.newIntBuffer(4 * shp3d.size());
			gl.glSelectBuffer(4 * shp3d.size(), selectBuffer);
			gl.glRenderMode(GL.GL_SELECT);
			gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
			gl.glInitNames();
			gl.glPushName(-1);
			if (picking == POINT)
				glu.gluPickMatrix((double) x, (double) (viewport[3] - y), 5.0,
						5.0, viewport, 0);
			else {
				glu.gluPickMatrix((x0 + x) * 0.5,
						(double) (viewport[3] - (y0 + y) * 0.5),
						Math.abs(x0 - x), Math.abs(y0 - y), viewport, 0);
			}
		}

		gl.glOrtho(left * scale * aspw, right * scale * aspw, bottom * scale
				/ asph, top * scale / asph, Znear, Zfar);

		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();

		gl.glPushMatrix();

		// translate the model
		gl.glTranslated(transX, transY, transZ);

		// rotate the model
		gl.glMultMatrixf(matrix, 0);

		// Draw each element
		// if (genlist == true) {

		// gl.glNewList(1,GL.GL_COMPILE);

		for (int i = 0; i < shp3d.size(); i++) {
			tmp = (shp) shp3d.elementAt(i);
			if (tmp instanceof shpBgImage && shpimage == false) { // For speed
																	// only
				shpimage = true;
				g2D = ovl.createGraphics();
			}
			if (tmp.isShow() == show) {
				gl.glLoadName(i); // Set current position in shp3d stack as name
									// for picking
				tmp.paintGL(gl, g2D, this, center_of_rotation.x,
						center_of_rotation.y, center_of_rotation.z, scale);
			}
		}

		// gl.glEndList();
		genlist = false;
		// }

		// gl.glCallList(1);

		if (picking != NO_PICK)
			hits = gl.glRenderMode(GL.GL_RENDER); // Set render mode & collect
													// if picking

		gl.glPopMatrix();

		// If box select, draw the box
		if (DRAG == true) {

			gl.glLoadIdentity();
			gl.glPushMatrix();

			box_scale_x = (right * scale * aspw - left * scale * aspw)
					/ screenwidth;
			box_scale_y = -(top * scale / asph - bottom * scale / asph)
					/ screenheight;
			box_x0 = left * scale * aspw;
			box_y0 = top * scale / asph;

			gl.glColor3f(1f, 0.1f, 0.1f);
			gl.glBegin(GL.GL_LINE_STRIP);
			gl.glVertex3f(box_x0 + x0 * box_scale_x, box_y0 + y0 * box_scale_y,
					Znear * 0.99f);
			gl.glVertex3f(box_x0 + x * box_scale_x, box_y0 + y0 * box_scale_y,
					Znear * 0.99f);
			gl.glVertex3f(box_x0 + x * box_scale_x, box_y0 + y * box_scale_y,
					Znear * 0.99f);
			gl.glVertex3f(box_x0 + x0 * box_scale_x, box_y0 + y * box_scale_y,
					Znear * 0.99f);
			gl.glVertex3f(box_x0 + x0 * box_scale_x, box_y0 + y0 * box_scale_y,
					Znear * 0.99f);
			gl.glEnd();

			gl.glPopMatrix();

		}

		gl.glFlush(); // Flush the GL Rendering Pipeline

		// generate the 2D overlay
		if (shpimage == true)
			ovl.drawAll();

	}

	// OpenGL method called when the display is changed.
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
			boolean deviceChanged) {

	}

	private synchronized void setPainted() {
		painted = true;
		notifyAll();
	}

	public synchronized void paint(Graphics g) {

		display();

		setPainted();
	}

	public void update(Graphics g) {
		paint(g);
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

	private void setPickedObject() {
		int count = obj3d.size();
		_Object o;

		// Generate selection
		picking = POINT;
		display();
		picking = NO_PICK;

		// Set related shapes to picked
		if (hits > 0)
			for (int i = 0; i < hits; i++)
				((shp) shp3d.elementAt(selectBuffer.get(4 * i + 3)))
						.setPicked(true);

		// Generate selected object tree
		for (int i = 0; i < count; i++) {
			o = ((_Object) obj3d.elementAt(i));
			if (o.isPickPoint(x, y, show, true))
				o.setSelected(true);
		}

		// Update object selection in preprocessor tree
		if (pre != null)
			pre.rescanTree();

	}

	private void boxSelect(int sx, int sy, int ex, int ey) {
		int count = obj3d.size();
		_Object o;

		// Generate selection
		picking = BOX;
		display();
		picking = NO_PICK;

		// Set related shapes to picked
		if (hits > 0)
			for (int i = 0; i < hits; i++)
				((shp) shp3d.elementAt(selectBuffer.get(4 * i + 3)))
						.setPicked(true);

		// Generate selected object tree
		for (int i = 0; i < count; i++) {
			o = ((_Object) obj3d.elementAt(i));
			if (o.isPickPoint(x, y, show, true))
				o.setSelected(true);
		}

		// Update object selection in preprocessor tree
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
			out[i] = (_Object) arr.elementAt(i);

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

	/**
	 * This method generates a new list of shapes to be used in the model
	 * rendering.
	 * 
	 */
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

		// Generate a new command list in the display method
		genlist = true;

		// Add the grid to the shp3d array
		view_grid();
	}

	public synchronized void view_repaint() {
		if (painted) {
			painted = false;
			repaint();
		}
	}

	public synchronized void view_translate(float dx, float dy, float dz) {
		transX += dx * (right - left) * scale * aspw / screenwidth;
		transY += dy * (top - bottom) * scale * asph / screenheight;
		transZ += dz;

		view_repaint();
	}

	public synchronized void view_top() {
		synchronized (matrixLock) {
			LastRot.setIdentity(); // Reset Rotation
			ThisRot.setIdentity(); // Reset Rotation
		}
		view_repaint();
	}

	public synchronized void view_bottom() {
		synchronized (matrixLock) {
			LastRot.set(-1, 0, 0, 0, 0, 1, 0, 0, 0, 0, -1, 0, 0, 0, 0, 1);
			ThisRot.set(-1, 0, 0, 0, 0, 1, 0, 0, 0, 0, -1, 0, 0, 0, 0, 1);
		}
		view_repaint();
	}

	public synchronized void view_ne() {
		synchronized (matrixLock) {
			LastRot.set(-0.67843485f, -0.47181374f, 0.5631324f, 0.0f,
					0.7337033f, -0.4742565f, 0.48658055f, 0.0f, 0.03749419f,
					0.7432855f, 0.6679232f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
			ThisRot.set(-0.67843485f, -0.47181374f, 0.5631324f, 0.0f,
					0.7337033f, -0.4742565f, 0.48658055f, 0.0f, 0.03749419f,
					0.7432855f, 0.6679232f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
		}
		view_repaint();
	}

	public synchronized void view_sw() {
		synchronized (matrixLock) {
			LastRot.set(0.847451f, 0.355411f, -0.394347f, 0f, -0.5291711f,
					0.5060926f, -0.6810645f, 0f, -0.04248167f, 0.7858457f,
					0.6169619f, 0f, 0f, 0f, 0f, 1f);
			ThisRot.set(0.847451f, 0.355411f, -0.394347f, 0f, -0.5291711f,
					0.5060926f, -0.6810645f, 0f, -0.04248167f, 0.7858457f,
					0.6169619f, 0f, 0f, 0f, 0f, 1f);
		}
		view_repaint();
	}

	public synchronized void view_se() {
		synchronized (matrixLock) {
			LastRot.set(0.7957134f, -0.36887878f, 0.48038355f, 0f, 0.6056719f,
					0.48640004f, -0.6297437f, 0f, -0.0013595134f, 0.7920504f,
					0.6104544f, 0f, 0f, 0f, 0f, 1f);
			ThisRot.set(0.7957134f, -0.36887878f, 0.48038355f, 0f, 0.6056719f,
					0.48640004f, -0.6297437f, 0f, -0.0013595134f, 0.7920504f,
					0.6104544f, 0f, 0f, 0f, 0f, 1f);
		}
		view_repaint();
	}

	public synchronized void view_nw() {
		synchronized (matrixLock) {
			LastRot.set(-0.7357225f, 0.48088235f, -0.4769326f, 0.0f,
					-0.6764666f, -0.48717147f, 0.55231994f, 0f, 0.03325309f,
					0.72898287f, 0.683724f, 0f, 0f, 0f, 0f, 1f);
			ThisRot.set(-0.7357225f, 0.48088235f, -0.4769326f, 0.0f,
					-0.6764666f, -0.48717147f, 0.55231994f, 0f, 0.03325309f,
					0.72898287f, 0.683724f, 0f, 0f, 0f, 0f, 1f);
		}
		view_repaint();
	}

	public synchronized void view_left() {
		synchronized (matrixLock) {
			LastRot.set(0, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1);
			ThisRot.set(0, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1);
		}
		view_repaint();
	}

	public synchronized void view_right() {
		synchronized (matrixLock) {
			LastRot.set(0, 0, -1, 0, -1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1);
			ThisRot.set(0, 0, -1, 0, -1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1);
		}
		view_repaint();
	}

	public synchronized void view_front() {
		synchronized (matrixLock) {
			LastRot.set(1, 0, 0, 0, 0, 0, -1, 0, 0, 1, 0, 0, 0, 0, 0, 1);
			ThisRot.set(1, 0, 0, 0, 0, 0, -1, 0, 0, 1, 0, 0, 0, 0, 0, 1);
		}
		view_repaint();
	}

	public synchronized void view_back() {
		synchronized (matrixLock) {
			LastRot.set(-1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1);
			ThisRot.set(-1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1);
		}
		view_repaint();
	}

	public synchronized void view_all() {
		float cx, cy, cz, maxx, minx, maxy, miny, maxz, minz, d;
		float[] b;
		Shape shp;

		minx = java.lang.Float.MAX_VALUE;
		miny = java.lang.Float.MAX_VALUE;
		minz = java.lang.Float.MAX_VALUE;
		maxx = java.lang.Float.MIN_VALUE;
		maxy = java.lang.Float.MIN_VALUE;
		maxz = java.lang.Float.MIN_VALUE;

		for (int i = 0; i < shp3d.size(); i++) {
			try {
				b = ((shp) shp3d.elementAt(i)).getBoundaries();

				if (b != null) {

					maxx = Math.max(maxx, b[0]);
					minx = Math.min(minx, b[1]);
					maxy = Math.max(maxy, b[2]);
					miny = Math.min(miny, b[3]);
					maxz = Math.max(maxz, b[4]);
					minz = Math.min(minz, b[5]);

				}
			} catch (Exception e) {
			}
		}

		// Ball centre
		cx = (maxx + minx) / 2;
		cy = (maxy + miny) / 2;
		cz = (maxz + minz) / 2;

		// Ball diameter
		d = maxx - minx;
		d = Math.max(d, maxy - miny);
		d = Math.max(d, maxz - minz);

		// Set viewing coordinates
		Znear = -d;
		Zfar = d;
		left = cx - d;
		right = cx + d;
		bottom = cy - d;
		top = cy + d;

		// Reset zoom
		scale = 1.0f;
		transX = 0.0f;
		transY = 0.0f;
		transZ = 0.0f;

		// Repaint
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

	// Creates a grid and a coordinate system and adds them to the shp base
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
					shp3d.addElement(gp);
				}
			}

		shpXYZ xyz = new shpXYZ();
		shp3d.addElement(xyz);
		view_repaint();

	}

	public synchronized void view_scale(float s) {
		scale *= s;
		view_repaint();
	}

	void this_mousePressed(MouseEvent e) {
		int mod = e.getModifiers();

		if (((mod & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)
				&& ((mod & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK)) {
			synchronized (matrixLock) {
				LastRot.set(ThisRot); // Set Last Static Rotation To Last
										// Dynamic One
			}
			arcBall.click(e.getPoint()); // Update Start Vector And Prepare For
											// Dragging

		}
	}

	void this_mouseDragged(MouseEvent e) {
		int mod = e.getModifiers();

		if (((mod & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)
				&& ((mod & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK)) {
			Quat4f ThisQuat = new Quat4f();

			arcBall.drag(e.getPoint(), ThisQuat); // Update End Vector And Get
													// Rotation As Quaternion
			synchronized (matrixLock) {
				ThisRot.setRotation(ThisQuat); // Convert Quaternion Into
												// Matrix3fT
				ThisRot.mul(ThisRot, LastRot); // Accumulate Last Rotation Into
												// This One
			}
		} else

		if (((mod & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)
				&& ((mod & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK)) {
			if (y - e.getY() > 0)
				this.view_scale(1.1f);
			if (y - e.getY() < 0)
				this.view_scale(0.9f);
		} else

		if (((mod & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)) {
			this.view_translate(e.getX() - x, y - e.getY(), 0);
		}

		x = e.getX();
		y = e.getY();

		view_repaint();
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
			view_repaint();
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
			view_repaint();
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
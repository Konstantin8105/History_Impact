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
 * USA */

/*
 * NurbsCurve.java
 *
 * author   Jonas Forssell, Yuriy Mikhaylovskiy
 */
package j3d;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.tree.*;
import javax.swing.*;

import gui.*;

/**
 * Implementing the NurbsCurve prototype.
 * 
 * @author Timothy F. Rohaly / Jonas Forssell, Yuriy Mikhaylovskiy
 */
public class _NurbCurve extends _Geometry implements Serializable {
	public Color color;
	protected int order;
	protected int div = 5;
	protected int numKnots;
	protected Vector geomDB = new Vector();
	protected Vector nodesDB = new Vector();
	protected Vector elementsDB = new Vector();
	protected Vector arr = new Vector();
	protected _Group g_m = new _Group(false, false);
	protected _Group g_e = new _Group(false, false);
	protected _Group g_n = new _Group(false, false);

	public _CtrlPoint[] controlPoints;
	protected float[] knots, knots_v;

	public String msh_name = "Dummy";
	public int msh_type = Canvas3D.MESH_Dummy_2;
	public float diameter, mesh_size;
	public int mesh_div;
	public Float factor, friction;
	public String contact;
	public Material material = null;

	protected JComboBox cComboBox1, cComboBox2;
	protected JTextField cTextField1, cTextField2;
	private JButton cButton1;
	protected String[] type = { "No_Mesh", "Rod_2", "Beam_2", "Contact_Line" };
	protected int mesh_type_index;
	protected Object material_index;
	protected JButton jButton1;

	private boolean by_constructor = false;

	public _NurbCurve(boolean add, Canvas3D J3D) {
		this.J3D = J3D;
		this.color = Color.BLUE;
		this.diameter = 1.0f;
		this.mesh_size = 1.0f;
		this.add = add;
		this.geometry_type = Canvas3D.CURVE;
	}

	public _NurbCurve(_CtrlPoint[] p, float[] knots, int order,
			float mesh_size, int msh_type, float diameter, Material material,
			Canvas3D J3D) {
		this.J3D = J3D;
		this.add = false;
		this.geometry_type = Canvas3D.CURVE;
		this.color = Color.BLUE;
		this.knots = (float[]) knots.clone();
		this.msh_type = msh_type;
		this.diameter = diameter;
		if (material != null)
			this.material = new Material(material);
		this.mesh_size = mesh_size;
		this.order = order;

		this.controlPoints = new _CtrlPoint[p.length];
		for (int i = 0; i < p.length; i++)
			controlPoints[i] = new _CtrlPoint(p[i]);

		by_constructor = true;

		reset(false);
	}

	public String writeObject() {
		String st = new String();
		st += " MESHTYPE = " + type[msh_type];
		st += " D = " + diameter;
		st += " MESHSIZE = " + mesh_size;
		if (material != null)
			st += " MATERIAL = " + material.name;
		return st;
	}

	protected void readObject(String st) {

		/*
		 * controlPoints = (_CtrlPoint[])in.readObject(); knots =
		 * (float[])in.readObject(); div = in.readInt(); color =
		 * (Color)in.readObject(); order = in.readInt(); geometry_type =
		 * in.readInt();
		 * 
		 * geomDB = (Vector)in.readObject(); nodesDB = (Vector)in.readObject();
		 * elementsDB = (Vector)in.readObject(); Id = (String)in.readObject();
		 * 
		 * msh_name = (String)in.readObject(); msh_type = in.readInt(); diameter
		 * = in.readFloat(); mesh_size = in.readFloat();
		 * 
		 * mesh_div = in.readInt(); factor = (Float)in.readObject(); friction =
		 * (Float)in.readObject(); contact = (String)in.readObject();
		 * 
		 * material = (Material)in.readObject(); type =
		 * (String[])in.readObject(); mesh_type_index = in.readInt();
		 * material_index = in.readObject();
		 */

		selected = false;
	}

	public void reset(boolean do_mesh) {
		// Generate geometry
		generate();
		// Generate mesh
		if (do_mesh)
			mesh(msh_type, mesh_size);
	}

	/**
	 * This method generates the points which makes up the curve. Note: before
	 * calling this method, make sure you have set the following knots - the
	 * knot vector controlPoints - The controlPoint vector order - Curve segment
	 * degree
	 * 
	 * @param div
	 *            The number of divisions for the curve (number of points)
	 */

	public void generate() {

		int numControlPoints = controlPoints.length;
		int numKnots = knots.length;
		geomDB = new Vector();
		Vector tmp = new Vector();
		float distance, us, ue;
		_Point s, e, p;

		// Create start vector of u-values along the curve. Use the knot vector
		// and avoid double knots
		tmp.add(new Float(knots[order]));
		for (int i = order + 1; i < numKnots - order; i++)
			if (((Float) tmp.lastElement()).floatValue() != knots[i])
				tmp.add(new Float(knots[i]));

		// Refine the vector to within tolerance
		for (int i = 0; i < tmp.size() - 1; i++) {
			us = ((Float) tmp.elementAt(i)).floatValue();
			ue = ((Float) tmp.elementAt(i + 1)).floatValue();

			s = lib3D.curvePoint(numControlPoints, order, knots, controlPoints,
					us);
			e = lib3D.curvePoint(numControlPoints, order, knots, controlPoints,
					ue);
			p = lib3D.curvePoint(numControlPoints, order, knots, controlPoints,
					(us + ue) / 2.0f);

			distance = lib3D.distancePoint2Line(p, s, e);

			if (distance > J3D.getGeometricTolerance()) {
				tmp.insertElementAt(new Float((us + ue) / 2.0f), i + 1);
				i--;
			}
		}

		// Add resulting points to the geometric database
		for (int i = 0; i < tmp.size(); i++) {
			geomDB.add(lib3D.curvePoint(numControlPoints, order, knots,
					controlPoints, ((Float) tmp.elementAt(i)).floatValue()));
		}
	}

	public Object[] get_Array(Canvas3D j3d) {
		_Point tmp1, tmp2;
		arr = new Vector();

		// Generate geometry
		for (int i = 0; i < geomDB.size() - 1; i++) {
			tmp1 = (_Point) geomDB.elementAt(i);
			tmp2 = (_Point) geomDB.elementAt(i + 1);
			arr.add(new shpLine(tmp1.x, tmp1.y, tmp1.z, tmp2.x, tmp2.y, tmp2.z,
					selected ? Canvas3D.SELECTCOLOR : color));
		}

		for (int i = 0; i < arr.size(); i++)
			((shp) arr.elementAt(i)).setShow(show);

		return arr.toArray();
	}

	public boolean isPickPoint(int x, int y, boolean shw, boolean ogl) {
		boolean check = false;

		// select this nurb curve if geometry is picked
		if (show == shw)
			for (int i = 0; i < geomDB.size() - 1; i++)
				check = (((shp) arr.elementAt(i)).isPickPoint(x, y, ogl) == true ? true
						: check);

		return check;
	}

	public boolean isPickPoint(Rectangle2D r, boolean shw, boolean ogl) {
		boolean check = true;

		if (show == shw)
			for (int i = 0; i < geomDB.size() - 1; i++)
				check = (((shp) arr.elementAt(i)).isPickPoint(r, ogl) == false ? false
						: check);

		return check;
	}

	protected void prepareGroups() {
		// Define group
		g_m.setName("Mesh");
		J3D.add3D(g_m);

		g_n.setName("Nodes");
		J3D.add3D(g_n);

		g_e.setName("Elements");
		J3D.add3D(g_e);

		g_m.addToGroup(g_n);
		g_m.addToGroup(g_e);

	}

	/**
	 * This method generates the nodes and elements which makes up the curve
	 * mesh. Note: before calling this method, make sure you have set the
	 * following knots - the knot vector controlPoints - The controlPoint vector
	 * order - Curve segment degree
	 * 
	 * @param type
	 *            The element type for the mesh
	 * @param size
	 *            The length of each element assuming uniform mesh
	 */

	public void mesh(int type, float size) {

		// Remove old mesh
		J3D.clearSelectOnAllObjects3D();
		for (int i = 0; i < nodesDB.size(); i++)
			((_Node) nodesDB.elementAt(i)).setSelected(true);
		for (int i = 0; i < elementsDB.size(); i++)
			((_Object) elementsDB.elementAt(i)).setSelected(true);
		J3D.removeSelectedObjects3D();

		// Generate new mesh
		mesh_size = size;
		nodesDB = new Vector();
		elementsDB = new Vector();

		int numControlPoints = controlPoints.length;
		float length;
		_Point op, np;
		_Node on, nn;
		_Element2 ne;

		// Determine length
		length = 0;
		op = lib3D.curvePoint(numControlPoints, order, knots, controlPoints,
				0.0f / (float) div);
		for (int i = 1; i <= div; i++) {
			np = lib3D.curvePoint(numControlPoints, order, knots,
					controlPoints, (float) i / (float) div);
			length += Math.sqrt((np.x - op.x) * (np.x - op.x) + (np.y - op.y)
					* (np.y - op.y) + (np.z - op.z) * (np.z - op.z));
			op = np;
		}

		mesh_div = (int) (length / mesh_size);

		// Generate mesh
		if (type != Canvas3D.MESH_Dummy_2) {
			op = lib3D.curvePoint(numControlPoints, order, knots,
					controlPoints, 0.0f / (float) mesh_div);
			on = new _Node(op.x, op.y, op.z);
			on.set_Id(Id + "." + 0);
			on.setMaster_type(Canvas3D.CURVE);
			on.setShow(show);
			nodesDB.add(on);

			for (int i = 1; i <= mesh_div; i++) {

				np = lib3D.curvePoint(numControlPoints, order, knots,
						controlPoints, (float) i / (float) mesh_div);
				nn = new _Node(np.x, np.y, np.z);
				nn.set_Id(Id + "." + i);
				nn.setMaster_type(Canvas3D.CURVE);
				nn.setShow(show);
				nodesDB.add(nn);

				ne = new _Element2(on, nn, type, material, diameter);
				ne.set_Id(Id + "." + (i - 1));
				ne.setShow(show);
				elementsDB.add(ne);

				on = nn;
				op = np;
			}
		}

		// Add mesh to canvas & groups
		for (int i = 0; i < nodesDB.size(); i++) {
			J3D.add3D((_Node) nodesDB.elementAt(i));
			g_n.addToGroup((_Node) nodesDB.elementAt(i));
		}

		for (int i = 0; i < elementsDB.size(); i++) {
			J3D.add3D((_Object) elementsDB.elementAt(i));
			g_e.addToGroup((_Object) elementsDB.elementAt(i));
		}

	}

	public void setSelected(boolean sel) {
		selected = sel;
	}

	public boolean isSelected() {
		return selected;
	}

	public void deselectRequiredObjects() {
		// Do nothing
	}

	public String toString() {
		return "Nurb Curve ID=" + Id;
	}

	public void transform3D(Matrix3D t) {
		if (!selected || processed)
			return;
		for (int i = 0; i < controlPoints.length; i++) {
			controlPoints[i].setSelected(true);
			controlPoints[i].transform3D(t);
			controlPoints[i].setSelected(true);
		}
		reset(true);
	}

	public MutableTreeNode get_TreeNode() {
		DefaultMutableTreeNode curve = new DefaultMutableTreeNode(this);

		if (by_constructor) {
			curve.add(g_m.get_TreeNode());
			return curve;
		} else
			return g_m.get_TreeNode();
	}

	public JPanel getEditPanel(Canvas3D j3d, PreProcessor pp) {
		this.J3D = j3d;
		this.PreP = pp;

		JPanel cPanel0 = new JPanel();
		JPanel cPanel1 = new JPanel();
		JPanel cPanel2 = new JPanel();
		JPanel cPanel3 = new JPanel();
		JPanel cPanel4 = new JPanel();
		cComboBox1 = new JComboBox(type);
		cComboBox2 = new JComboBox();
		cTextField1 = new JTextField("" + diameter);
		cTextField2 = new JTextField("" + mesh_size);
		cButton1 = new JButton("Update");

		cPanel0.setLayout(new BoxLayout(cPanel0, BoxLayout.Y_AXIS));
		cPanel0.add(cComboBox1);
		cComboBox1.setForeground(Color.blue);
		cComboBox1.setSelectedIndex(mesh_type_index);
		cComboBox1.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					cTextField1.requestFocus();
					cTextField1.selectAll();
				}
			}
		});

		cPanel0.add(cPanel2);
		cPanel2.setLayout(new GridLayout());
		cPanel2.add(new JLabel("Diameter"), null);
		cPanel2.add(cTextField1, null);
		cTextField1.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					cComboBox2.requestFocus();
				}
			}
		});

		cPanel0.add(cPanel1);
		cPanel1.setLayout(new BorderLayout());
		cPanel1.add(new JLabel("Material"), BorderLayout.WEST);
		cPanel1.add(cComboBox2, BorderLayout.EAST);
		cComboBox2.setActionCommand("");
		cComboBox2.setLightWeightPopupEnabled(false);
		for (Enumeration en = PreP.MatDB.keys(); en.hasMoreElements();)
			cComboBox2.addItem(en.nextElement());
		if (material_index == null)
			cComboBox2.setSelectedIndex(0);
		else
			cComboBox2.setSelectedItem(material_index);
		cComboBox2.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					cTextField2.requestFocus();
					cTextField2.selectAll();
				}
			}
		});

		cPanel0.add(cPanel3);
		cPanel3.setLayout(new GridLayout());
		cPanel3.add(new JLabel("Element Size"), null);
		cPanel3.add(cTextField2, null);
		cTextField2.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					jButton1.requestFocus();
				}
			}
		});

		if (by_constructor) {

			cPanel0.add(cPanel4);
			cPanel4.add(cButton1);
			cButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					cButton1_actionPerformed(e);
				}
			});
			cButton1.addKeyListener(new KeyAdapter() {

				public void keyPressed(KeyEvent e) {
					checkDefaultKey(e);
				}

				public void keyReleased(KeyEvent e) {
					int c = e.getKeyCode();

					if (c == KeyEvent.VK_ENTER) {
						cButton1_actionPerformed(null);
					}
				}
			});
		}

		return cPanel0;
	}

	void cButton1_actionPerformed(ActionEvent e) {
		_NurbCurve tmp = this;
		int i, n;

		tmp.update(e);

		tmp.reset(true);

		J3D.tree_reset();
		J3D.view_reset();
		J3D.repaint();
	}

	public void requestAction() {

	}

	public void update(ActionEvent e) {
		mesh_type_index = cComboBox1.getSelectedIndex();
		if (mesh_type_index == 0)
			msh_type = Canvas3D.MESH_Dummy_2;
		if (mesh_type_index == 1)
			msh_type = Canvas3D.MESH_Rod_2;
		if (mesh_type_index == 2)
			msh_type = Canvas3D.MESH_Beam_2;
		if (mesh_type_index == 3)
			msh_type = Canvas3D.MESH_Contact_Line;
		diameter = Float.parseFloat(cTextField1.getText());
		material_index = cComboBox2.getSelectedItem();
		material = (Material) PreP.MatDB.get(material_index + "");
		mesh_size = Float.parseFloat(cTextField2.getText());
	}

	public _Node[] get_Nodes() {
		_Node[] arr = new _Node[0];
		return arr;
	}

	public _Object[] get_Elements() {
		_Object[] arr = new _Object[0];
		return arr;
	}

	public void insertKnots(Vector ins) {

		if (ins.size() == 0)
			return;

		_CtrlPoint[] newcontrolPoints = new _CtrlPoint[controlPoints.length
				+ ins.size()];
		float[] newknot = new float[controlPoints.length + ins.size() + order
				+ 1];

		float[] insknts = new float[ins.size()];
		for (int i = 0; i < ins.size(); i++)
			insknts[i] = ((Float) ins.elementAt(i)).floatValue();

		lib3D.refineKnotVectCurve(order, knots, controlPoints, insknts,
				newknot, newcontrolPoints);

		knots = newknot;
		controlPoints = newcontrolPoints;
	}

	public _NurbCurve copy() {
		_NurbCurve c = new _NurbCurve(false, J3D);
		try {
			c = (_NurbCurve) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		return c;
	}

	public void elevate(int times) {

		// Dimension for worst case
		_CtrlPoint[] newcontrolPoints = new _CtrlPoint[controlPoints.length
				* (times + 1)];
		float[] newknots = new float[knots.length * (times + 1)];
		int[] cl;

		cl = lib3D.degreeElevateCurve(order, knots, controlPoints, times,
				newknots, newcontrolPoints);

		knots = new float[cl[0]];
		for (int i = 0; i < cl[0]; i++)
			knots[i] = newknots[i];

		controlPoints = new _CtrlPoint[cl[1]];
		for (int i = 0; i < cl[1]; i++)
			controlPoints[i] = new _CtrlPoint(newcontrolPoints[i]);

		order = cl[2];
	}

	public _Object duplicate(Canvas3D out, boolean add) {
		_NurbCurve o = null;
		try {
			o = (_NurbCurve) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		o.controlPoints = (_CtrlPoint[]) controlPoints.clone();
		for (int i = 0; i < controlPoints.length; i++)
			o.controlPoints[i] = new _CtrlPoint(controlPoints[i]);

		o.knots = (float[]) knots.clone();

		if (add)
			out.add3D(o);

		return o;
	}

	public Vector3D getCenter() {
		_Point s = lib3D.curvePoint(controlPoints.length, order, knots,
				controlPoints, 0.5f);

		return s.getVector();
	}

	protected _Point[] calculateDerivate(float u, int derivate_order) {
		_CtrlPoint[] dp = new _CtrlPoint[controlPoints.length];
		_Point[] p;

		if (derivate_order > order)
			return null;

		dp = lib3D.curveDerivsAlg1(controlPoints.length, order, knots,
				controlPoints, u, derivate_order);
		p = lib3D.ratCurveDerivs(dp, derivate_order);

		return p;
	}

	protected _Point[] getEndPoints() {
		_Point[] p = new _Point[2];

		p[0] = (_Point) controlPoints[0];
		p[1] = (_Point) controlPoints[controlPoints.length - 1];

		return p;
	}

	public void reverseDirection() {
		int n = knots.length;
		int m = controlPoints.length;
		int i;
		float[] newknot = new float[n];
		_CtrlPoint[] ncp = new _CtrlPoint[m];

		for (i = 0; i < n; i++)
			newknot[i] = knots[n - 1] - knots[n - 1 - i];

		for (i = 0; i < m; i++)
			ncp[i] = controlPoints[m - 1 - i];

		knots = newknot;
		controlPoints = ncp;
	}

	public _Object[] getBorderObjects() {
		_Point[] p = new _Point[2];

		p[0] = new _Point(controlPoints[0].x, controlPoints[0].y,
				controlPoints[0].z, 0f, 0f, 0f);
		p[1] = new _Point(controlPoints[controlPoints.length - 1].x,
				controlPoints[controlPoints.length - 1].y,
				controlPoints[controlPoints.length - 1].z, 0f, 0f, 0f);

		return p;
	}

	public _Point getPointAt(float u, boolean deriv) {
		_Point a;
		_Point[] da;
		float l;

		if (deriv == false)
			return lib3D.curvePoint(controlPoints.length, order, knots,
					controlPoints, u);
		else {
			a = lib3D.curvePoint(controlPoints.length, order, knots,
					controlPoints, u);
			da = this.calculateDerivate(u, 1);

			l = (float) Math.sqrt(da[1].x * da[1].x + da[1].y * da[1].y
					+ da[1].z * da[1].z);

			a.vx = da[1].x / l;
			a.vy = da[1].y / l;
			a.vz = da[1].z / l;

			a.reset(true);

			return a;
		}

	}

	public float getLength() {
		int numControlPoints = controlPoints.length;
		float length;
		_Point op, np;

		// Determine length
		length = 0;
		op = lib3D.curvePoint(numControlPoints, order, knots, controlPoints,
				0.0f / (float) div);
		for (int i = 1; i <= div; i++) {
			np = lib3D.curvePoint(numControlPoints, order, knots,
					controlPoints, (float) i / (float) div);
			length += Math.sqrt((np.x - op.x) * (np.x - op.x) + (np.y - op.y)
					* (np.y - op.y) + (np.z - op.z) * (np.z - op.z));
			op = np;
		}

		return length;
	}

	public void replaceObjectWith(_Object o, _Object replacement) {
		// Do nothing, points are in subclasses
	}

}

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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.Enumeration;
import java.util.Vector;

import util.XVector;

import javax.swing.tree.*;
import javax.swing.*;

import gui.*;

/**
 * Implementing the NurbsCurve prototype.
 * 
 * @author Timothy F. Rohaly / Jonas Forssell, Yuriy Mikhaylovskiy
 */
public class _NurbSurface extends _Geometry implements Serializable {
	public Color color;
	protected int uorder, vorder;
	protected int udiv = 5;
	protected int vdiv = 5;
	protected int numKnots;
	protected Vector geomUDB = new Vector();
	protected Vector geomVDB = new Vector();
	protected Vector geomBDB = new Vector();
	protected XVector nodesDB = new XVector();
	protected XVector elementsDB = new XVector();
	protected Vector arr = new Vector();
	protected _Group g_m = new _Group(false, false);
	protected _Group g_n = new _Group(false, false);
	protected _Group g_e = new _Group(false, false);

	public _CtrlPoint[][] controlPoints;
	protected float[] uknots, vknots, knots_v;

	public String msh_name = "Dummy";
	public int msh_type = Canvas3D.MESH_Dummy_4;
	public float thickness, mesh_size;
	public int[] mesh_div;
	public float[] edgelength;
	public Float factor, friction;
	public String contact;
	public Material material = null;

	protected JComboBox cComboBox1, cComboBox2;
	protected JTextField cTextField1, cTextField2;
	String[] type = { "No_mesh", "Shell_BT_4", "Shell_C0_3", "Contact_Triangle" };
	protected int mesh_type_index;
	protected Object material_index;
	protected int masterType = Canvas3D.SURFACE;
	protected JButton jButton1;

	private boolean by_constructor = false;

	public _NurbSurface(boolean add, Canvas3D J3D) {
		this.udiv = 5;
		this.vdiv = 5;
		this.color = Color.CYAN;
		this.thickness = 1.0f;
		this.mesh_div = new int[4];
		this.add = add;
		this.mesh_size = 1.0f;
		this.J3D = J3D;
		geometry_type = Canvas3D.SURFACE;
	}

	public String writeObject() {
		String st = new String();
		st += " MESHTYPE = " + type[mesh_type_index];
		st += " MESHSIZE = " + mesh_size;
		st += " T = " + thickness;

		if (contact != null)
			st += " CONTACT = " + contact;
		if (factor != null)
			st += " FACTOR = " + factor;
		if (friction != null)
			st += " FRICTION = " + friction;
		if (material != null)
			st += " MATERIAL = " + material.name;

		return st;
	}

	/*
	 * private void writeObject(ObjectOutputStream out) throws IOException{
	 * out.writeObject(controlPoints); out.writeObject(uknots);
	 * out.writeObject(vknots); out.writeInt(udiv); out.writeInt(vdiv);
	 * out.writeObject(color); out.writeInt(geometry_type);
	 * out.writeObject(msh_name); out.writeInt(msh_type); out.writeObject(type);
	 * out.writeFloat(thickness); out.writeObject(mesh_div);
	 * out.writeFloat(mesh_size); out.writeObject(factor);
	 * out.writeObject(friction); out.writeObject(contact);
	 * out.writeObject(material); out.writeObject(material_index);
	 * out.writeObject(geomUDB); out.writeObject(geomVDB);
	 * out.writeObject(geomBDB); out.writeObject(nodesDB);
	 * out.writeObject(elementsDB); }
	 */

	public void readObject(String st) {
		/*
		 * controlPoints = (_CtrlPoint[][])in.readObject(); uknots =
		 * (float[])in.readObject(); vknots = (float[])in.readObject(); udiv =
		 * in.readInt(); vdiv = in.readInt(); color = (Color)in.readObject();
		 * geometry_type = in.readInt(); msh_name = (String)in.readObject();
		 * msh_type = in.readInt(); type = (String[])in.readObject(); thickness
		 * = in.readFloat(); mesh_div = (int[]) in.readObject(); mesh_size =
		 * in.readFloat(); factor = (Float) in.readObject(); friction = (Float)
		 * in.readObject(); contact = (String) in.readObject(); material =
		 * (Material)in.readObject(); material_index = in.readObject(); geomUDB
		 * = (Vector)in.readObject(); geomVDB = (Vector)in.readObject(); geomBDB
		 * = (Vector)in.readObject(); nodesDB = (XVector)in.readObject();
		 * elementsDB = (XVector)in.readObject();
		 */

		selected = false;

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

	public void reset(boolean do_mesh) {
		// Generate geometry
		generate(udiv, vdiv);
		// Generate mesh
		if (do_mesh)
			mesh(msh_type, mesh_size);
	}

	/**
	 * This method generates the points which makes up the curve. It also
	 * calculates the length of the surface edges.
	 * 
	 * Note: before calling this method, make sure you have set the following
	 * knots - the knot vector controlPoints - The controlPoint vector order -
	 * Curve segment degree
	 * 
	 * @param div
	 *            The number of divisions for the curve (number of points)
	 */

	public void generate(int udiv, int vdiv) {

		int numUControlPoints = controlPoints.length;
		int numVControlPoints = controlPoints[0].length;
		geomUDB = new Vector();
		geomVDB = new Vector();
		geomBDB = new Vector();

		edgelength = new float[4];

		// Generate two middle curves and a boundary to repesent the surface
		// geometry

		this.generateRefinedCurve(geomUDB, numUControlPoints, uorder, uknots,
				numVControlPoints, vorder, vknots, controlPoints, -1f, 0.5f,
				true);
		this.generateRefinedCurve(geomVDB, numUControlPoints, uorder, uknots,
				numVControlPoints, vorder, vknots, controlPoints, 0.5f, -1f,
				true);

		edgelength[0] = this.generateRefinedCurve(geomBDB, numUControlPoints,
				uorder, uknots, numVControlPoints, vorder, vknots,
				controlPoints, -1f, 0.0f, true);
		edgelength[1] = this.generateRefinedCurve(geomBDB, numUControlPoints,
				uorder, uknots, numVControlPoints, vorder, vknots,
				controlPoints, 1.0f, -1f, true);
		edgelength[2] = this.generateRefinedCurve(geomBDB, numUControlPoints,
				uorder, uknots, numVControlPoints, vorder, vknots,
				controlPoints, -1f, 1.0f, false);
		edgelength[3] = this.generateRefinedCurve(geomBDB, numUControlPoints,
				uorder, uknots, numVControlPoints, vorder, vknots,
				controlPoints, 0.0f, -1f, false);
	}

	private float generateRefinedCurve(Vector db, int n, int p, float[] U,
			int m, int q, float[] V, _CtrlPoint[][] P, float u, float v,
			boolean positive) {

		int numKnots = (u < 0 ? U.length : V.length);
		int order = (u < 0 ? p : q);
		Vector tmp = new Vector();
		float distance, ts, te;
		_Point s, e, a;

		// Create start vector of u-values along the curve. Use the knot vector
		// and avoid double knots
		tmp.add(new Float(u < 0 ? U[order] : V[order]));
		for (int i = order + 1; i < numKnots - order; i++)
			if (((Float) tmp.lastElement()).floatValue() != (u < 0 ? U[i]
					: V[i]))
				tmp.add(new Float(u < 0 ? U[i] : V[i]));

		// Refine the vector to within tolerance
		for (int i = 0; i < tmp.size() - 1; i++) {
			ts = ((Float) tmp.elementAt(i)).floatValue();
			te = ((Float) tmp.elementAt(i + 1)).floatValue();

			s = lib3D.surfacePoint(n, p, U, m, q, V, P, u < 0 ? ts : u,
					v < 0 ? ts : v);
			e = lib3D.surfacePoint(n, p, U, m, q, V, P, u < 0 ? te : u,
					v < 0 ? te : v);
			a = lib3D.surfacePoint(n, p, U, m, q, V, P, u < 0 ? (ts + te) / 2f
					: u, v < 0 ? (ts + te) / 2f : v);

			distance = lib3D.distancePoint2Line(a, s, e);

			if (distance > J3D.getGeometricTolerance()) {
				tmp.insertElementAt(new Float((ts + te) / 2.0f), i + 1);
				i--;
			}
		}

		// Add resulting points to the geometric database
		distance = 0.0f;
		s = null;

		if (positive)
			for (int i = 0; i < tmp.size(); i++) {
				e = lib3D.surfacePoint(n, p, U, m, q, V, P,
						u < 0 ? ((Float) tmp.elementAt(i)).floatValue() : u,
						v < 0 ? ((Float) tmp.elementAt(i)).floatValue() : v);
				db.add(e);
				if (s != null)
					distance += e.distance(s);
				s = e;
			}
		else
			for (int i = tmp.size() - 1; i >= 0; i--) {
				e = lib3D.surfacePoint(n, p, U, m, q, V, P,
						u < 0 ? ((Float) tmp.elementAt(i)).floatValue() : u,
						v < 0 ? ((Float) tmp.elementAt(i)).floatValue() : v);
				db.add(e);
				if (s != null)
					distance += e.distance(s);
				s = e;
			}

		return distance;
	}

	public Object[] get_Array(Canvas3D j3d) {
		J3D = j3d;
		_Point tmp1, tmp2;
		arr = new Vector();

		// Generate geometry
		for (int i = 0; i < geomUDB.size() - 1; i++) {
			tmp1 = (_Point) geomUDB.elementAt(i);
			tmp2 = (_Point) geomUDB.elementAt(i + 1);
			arr.add(new shpLine(tmp1.x, tmp1.y, tmp1.z, tmp2.x, tmp2.y, tmp2.z,
					selected ? Canvas3D.SELECTCOLOR : color));
		}
		for (int i = 0; i < geomVDB.size() - 1; i++) {
			tmp1 = (_Point) geomVDB.elementAt(i);
			tmp2 = (_Point) geomVDB.elementAt(i + 1);
			arr.add(new shpLine(tmp1.x, tmp1.y, tmp1.z, tmp2.x, tmp2.y, tmp2.z,
					selected ? Canvas3D.SELECTCOLOR : color));
		}
		for (int i = 0; i < geomBDB.size() - 1; i++) {
			tmp1 = (_Point) geomBDB.elementAt(i);
			tmp2 = (_Point) geomBDB.elementAt(i + 1);
			arr.add(new shpLine(tmp1.x, tmp1.y, tmp1.z, tmp2.x, tmp2.y, tmp2.z,
					selected ? Canvas3D.SELECTCOLOR : color));
		}

		for (int i = 0; i < arr.size(); i++)
			((shp) arr.elementAt(i)).setShow(show);

		return arr.toArray();
	}

	/**
	 * This method generates the nodes and elements which makes up the surface
	 * mesh. Note: before calling this method, make sure you have set the
	 * following knots - the knot vectors controlPoints - The controlPoint array
	 * order - Curve segment degrees
	 * 
	 * Each edge has a number of divisions (div[0] -> div[3]) [2] They are
	 * defined in the following way +------------+ div[0] = divisions of curve
	 * u=0-1, v=0 | | div[1] = divisions of curve u=1, v=0-1 [3] | | [1] div[2]
	 * = divisions of curve u=0-1, v=1 | | div[3] = divisions of curve u=0,
	 * v=0-1 +------------+ [0]
	 * 
	 * An algorithm will be developed to provide a mesh with minimum number of
	 * triangle elements and good quality based on the constraints put on the
	 * edges of the surface.
	 * 
	 * For now, we settle for using only constant divisions defined by div[0]
	 * and div[1]
	 * 
	 * @param type
	 *            The element type for the mesh
	 * @param size
	 *            The length of each element assuming uniform mesh
	 */

	public void mesh(int type, float size) {

		// Remove old mesh
		J3D.clearSelectOnAllObjects3D();

		for (int i = 0; i < nodesDB.rowsize(); i++)
			for (int j = 0; j < nodesDB.colsize(); j++)
				((_Node) nodesDB.elementAt(i, j)).setSelected(true);

		for (int i = 0; i < elementsDB.rowsize(); i++)
			for (int j = 0; j < elementsDB.colsize(); j++)
				((_Element) elementsDB.elementAt(i, j)).setSelected(true);

		J3D.removeSelectedObjects3D();

		// Start new mesh
		mesh_size = size;
		nodesDB = new XVector();
		elementsDB = new XVector();

		int numUControlPoints = controlPoints.length;
		int numVControlPoints = controlPoints[0].length;

		for (int i = 0; i < 4; i++)
			if (mesh_div[i] < 0) {
				mesh_div[i] = (int) (edgelength[i] / mesh_size);
				if (mesh_div[i] == 0)
					mesh_div[i] = 1;
			}

		if (mesh_div[0] == mesh_div[2] && mesh_div[1] == mesh_div[3])
			mesh_structured(type, numUControlPoints, numVControlPoints);
		else
			mesh_unstructured(type, numUControlPoints, numVControlPoints);

		// Add mesh to canvas
		for (int i = 0; i < nodesDB.rowsize(); i++)
			for (int j = 0; j < nodesDB.colsize(); j++)
				J3D.add3D((_Node) nodesDB.elementAt(i, j));

		for (int i = 0; i < elementsDB.rowsize(); i++)
			for (int j = 0; j < elementsDB.colsize(); j++)
				J3D.add3D((_Element) elementsDB.elementAt(i, j));

	}

	/**
	 * Generates an unstructured mesh for the surface.
	 * 
	 * @param type
	 *            Type of mesh. Can be triangle or mixed quad and tria
	 * @param numUControlPoints
	 *            Number of controlpoints for the surface in U direction
	 * @param numVControlPoints
	 *            Number of controlpoints for the surface in V direction
	 * 
	 *            Notice that the number of boundary nodes are set with the
	 *            mesh_div[] variable in a counter clockwise direction. The
	 *            distribution of the element sizes are controlled inside the
	 *            Circle object by calculating a quota. An element is concidered
	 *            too large if the quota is greater than 1.
	 * 
	 */

	private void mesh_unstructured(int type, int numUControlPoints,
			int numVControlPoints) {
		_Point np;
		_Node nn;
		_Element3 nf;
		int i, j, k, l, n, nodid, elid;
		boolean free;
		Circle c = new Circle();
		Vector pnts = new Vector();
		float sa, sb, sc, sd;
		Vector badElements = new Vector();
		Vector tempNodes = new Vector();
		Vector tempPoints = new Vector();
		Vector tempElements = new Vector();
		Vector tnodesDB = new Vector();
		Vector telementsDB = new Vector();
		Vector encIndeces = new Vector();
		Point2D tp;

		// Initialize variables
		nodid = 0;
		sa = (mesh_div[3] != 0 ? 1.0f / mesh_div[3] : 0.5f);
		sb = (mesh_div[2] != 0 ? 1.0f / mesh_div[2] : 0.5f);
		sc = (mesh_div[1] != 0 ? 1.0f / mesh_div[1] : 0.5f);
		sd = (mesh_div[0] != 0 ? 1.0f / mesh_div[0] : 0.5f);

		// No not mesh if no mesh is selected
		if (msh_type == Canvas3D.MESH_Dummy_4)
			return;

		// Generate mesh nodes
		for (i = 1; i <= mesh_div[0]; i++) {
			np = lib3D.surfacePoint(numUControlPoints, uorder, uknots,
					numVControlPoints, vorder, vknots, controlPoints, (float) i
							/ (float) mesh_div[0], 0);
			nn = new _Node(np.x, np.y, np.z);
			nn.set_Id(Id + "." + nodid++);
			nn.setMaster_type(Canvas3D.SURFACE);
			nn.setShow(show);
			tnodesDB.add(nn);
			pnts.add(new Point2D.Double((double) i / mesh_div[0], 0.0 + Math
					.random() * 1E-9));
		}

		for (i = 1; i <= mesh_div[1]; i++) {
			np = lib3D.surfacePoint(numUControlPoints, uorder, uknots,
					numVControlPoints, vorder, vknots, controlPoints, 1,
					(float) i / (float) mesh_div[1]);
			nn = new _Node(np.x, np.y, np.z);
			nn.set_Id(Id + "." + nodid++);
			nn.setMaster_type(Canvas3D.SURFACE);
			nn.setShow(show);
			tnodesDB.add(nn);
			pnts.add(new Point2D.Double(1.0 - Math.random() * 1E-9, (double) i
					/ mesh_div[1]));
		}

		for (i = 1; i <= mesh_div[2]; i++) {
			np = lib3D.surfacePoint(numUControlPoints, uorder, uknots,
					numVControlPoints, vorder, vknots, controlPoints, 1.0f
							- (float) i / (float) mesh_div[2], 1);
			nn = new _Node(np.x, np.y, np.z);
			nn.set_Id(Id + "." + nodid++);
			nn.setMaster_type(Canvas3D.SURFACE);
			nn.setShow(show);
			tnodesDB.add(nn);
			pnts.add(new Point2D.Double(1.0 - (double) i / mesh_div[2],
					1.0 - Math.random() * 1E-9));
		}

		for (i = 1; i <= mesh_div[3]; i++) {
			np = lib3D.surfacePoint(numUControlPoints, uorder, uknots,
					numVControlPoints, vorder, vknots, controlPoints, 0, 1.0f
							- (float) i / (float) mesh_div[3]);
			nn = new _Node(np.x, np.y, np.z);
			nn.set_Id(Id + "." + nodid++);
			nn.setMaster_type(Canvas3D.SURFACE);
			nn.setShow(show);
			tnodesDB.add(nn);
			pnts.add(new Point2D.Double(0.0 + Math.random() * 1E-9, 1.0
					- (double) i / mesh_div[3]));
		}

		// Generate original elements
		n = pnts.size();
		elid = 0;
		for (i = 0; i < n - 2; i++)
			for (j = i + 1; j < n - 1; j++)
				if (j != i)
					for (k = j + 1; k < n; k++)
						if (k != i
								&& k != j
								&& Math.abs(lib2D.crossProduct(
										(Point2D.Double) pnts.elementAt(i),
										(Point2D.Double) pnts.elementAt(j),
										(Point2D.Double) pnts.elementAt(k))) > 1E-9) {
							c.circumCircle((Point2D.Double) pnts.elementAt(i),
									(Point2D.Double) pnts.elementAt(j),
									(Point2D.Double) pnts.elementAt(k));
							free = true;

							for (l = 0; l < n; l++)
								if (l != i && l != j && l != k)
									if (c.inside((Point2D.Double) pnts
											.elementAt(l))) {
										free = false;
										break;
									}

							if (free) {
								nf = new _Element3(
										(_Node) tnodesDB.elementAt(i),
										(_Node) tnodesDB.elementAt(j),
										(_Node) tnodesDB.elementAt(k),
										Canvas3D.MESH_Shell_C0_3, material,
										thickness);
								nf.set_Id(Id + "." + elid++);
								nf.setShow(show);

								// Add the element.
								c.calculateQuota(sa, sb, sc, sd);
								nf.setCircle(c);
								telementsDB.add(nf);

								// Add to bad list if bad angle etc....
								if (c.isWithinLimits() && c.q > 1.0)
									addSorted(badElements, c, nf);

								c = new Circle();
							}
						}

		// Refine bad elements by adding more nodes & points
		while (!badElements.isEmpty()) {

			// Clean temporary workspace
			tempNodes.removeAllElements();
			tempPoints.removeAllElements();
			encIndeces.removeAllElements();
			tempElements.removeAllElements();

			// Get the bad element
			nf = (_Element3) badElements.elementAt(badElements.size() - 1);
			badElements.removeElementAt(badElements.size() - 1);

			// Get the corresponding circumcircle
			i = telementsDB.indexOf(nf);

			if (i != -1) {

				c = nf.getCircle();

				// Calculate new point and node for the center
				np = lib3D.surfacePoint(numUControlPoints, uorder, uknots,
						numVControlPoints, vorder, vknots, controlPoints,
						(float) c.c.x, (float) c.c.y);
				nn = new _Node(np.x, np.y, np.z);
				nn.set_Id(Id + "." + nodid++);
				nn.setMaster_type(Canvas3D.SURFACE);
				nn.setShow(show);
				tp = new Point2D.Double(c.c.x, c.c.y);

				// Find all elements which has an encroaching circumcircle
				for (i = 0; i < telementsDB.size(); i++) {
					c = ((_Object) telementsDB.elementAt(i)).getCircle();
					if (c.inside(tp))
						encIndeces.add(new Integer(i));
				}

				// Add the nodes and points of these elements to a temporary
				// vector
				for (i = 0; i < encIndeces.size(); i++) {
					Point2D p1, p2, p3;

					j = ((Integer) encIndeces.elementAt(i)).intValue();
					nf = (_Element3) telementsDB.elementAt(j);

					p1 = (Point2D) pnts.elementAt(tnodesDB.indexOf(nf.node1));
					p2 = (Point2D) pnts.elementAt(tnodesDB.indexOf(nf.node2));
					p3 = (Point2D) pnts.elementAt(tnodesDB.indexOf(nf.node3));

					if (!tempNodes.contains(nf.node1)) {
						k = getSortedPosition(tp, p1, tempPoints);
						tempNodes.insertElementAt(nf.node1, k);
						tempPoints.insertElementAt(p1, k);
					}

					if (!tempNodes.contains(nf.node2)) {
						k = getSortedPosition(tp, p2, tempPoints);
						tempNodes.insertElementAt(nf.node2, k);
						tempPoints.insertElementAt(p2, k);
					}

					if (!tempNodes.contains(nf.node3)) {
						k = getSortedPosition(tp, p3, tempPoints);
						tempNodes.insertElementAt(nf.node3, k);
						tempPoints.insertElementAt(p3, k);
					}
				}

				tempNodes.add(tempNodes.elementAt(0));
				tempPoints.add(tempPoints.elementAt(0));

				// Generate new elements
				n = tempPoints.size();
				for (i = 0; i < n - 1; i++) {
					c = new Circle();
					c.circumCircle(tp,
							(Point2D.Double) tempPoints.elementAt(i),
							(Point2D.Double) tempPoints.elementAt(i + 1));
					nf = new _Element3(nn, (_Node) tempNodes.elementAt(i),
							(_Node) tempNodes.elementAt(i + 1),
							Canvas3D.MESH_Shell_C0_3, material, thickness);
					nf.set_Id(Id + "." + elid++);
					nf.setShow(show);

					c.calculateQuota(sa, sb, sc, sd);
					nf.setCircle(c);
					tempElements.add(nf);
				}

				// Delete the elements and circumcircles from the database
				for (i = encIndeces.size() - 1; i >= 0; i--) {
					int ti = ((Integer) encIndeces.elementAt(i)).intValue();
					telementsDB.removeElementAt(ti);
				}

				// Add the new node to the database
				tnodesDB.add(nn);
				pnts.add(tp);

				// Add the new elements to the database
				telementsDB.addAll(tempElements);

				// Add corresponding bad elements to the list
				for (i = 0; i < tempElements.size(); i++) {
					nf = (_Element3) tempElements.elementAt(i);
					c = nf.getCircle();

					if (c.isWithinLimits() && c.q > 1.0)
						addSorted(badElements, c, nf);
				}

			}
		}

		// Finished looping. Clean out bad elements
		for (i = telementsDB.size() - 1; i >= 0; i--) {
			nf = (_Element3) telementsDB.elementAt(i);
			if (nf.getMinLength() < 1e-5) // This is an element which can occur
											// at edges for triangular surfaces
				telementsDB.remove(i);
		}

		// Finished loop. Now construct the vectors
		elementsDB.setVectorAtRow(telementsDB, 0);
		nodesDB.setVectorAtRow(tnodesDB, 0);
	}

	private void addSorted(Vector bE, Circle ac, _Element3 anf) {
		int i = bE.size() - 1;

		while (i >= 0 && (((_Object) bE.elementAt(i)).getCircle()).r > ac.r)
			i--;

		if (i > 0)
			bE.insertElementAt(anf, i);
		else
			bE.insertElementAt(anf, 0);

	}

	private void mesh_structured(int type, int numUControlPoints,
			int numVControlPoints) {
		_Point np;
		_Node nn;
		_Element4 ne;
		_Element3 nf;
		if (msh_type != Canvas3D.MESH_Dummy_4) {

			// Generate mesh nodes
			for (int i = 0; i <= mesh_div[0]; i++) {
				for (int j = 0; j <= mesh_div[1]; j++) {
					np = lib3D.surfacePoint(numUControlPoints, uorder, uknots,
							numVControlPoints, vorder, vknots, controlPoints,
							(float) i / (float) mesh_div[0], (float) j
									/ (float) mesh_div[1]);
					nn = new _Node(np.x, np.y, np.z);
					nn.set_Id(Id + "." + i + "." + j);
					nn.setMaster_type(Canvas3D.SURFACE);
					nn.setShow(show);
					nodesDB.add(nn);
				}
				if (i < mesh_div[0])
					nodesDB.addRow();
			}

			// Generate mesh elements
			for (int i = 0; i < mesh_div[0]; i++) {
				for (int j = 0; j < mesh_div[1]; j++) {
					switch (type) {
					case Canvas3D.MESH_Shell_BT_4:
						if (this.areClose((_Node) nodesDB.elementAt(i, j),
								(_Node) nodesDB.elementAt(i + 1, j))) {
							nf = new _Element3((_Node) nodesDB.elementAt(i, j),
									(_Node) nodesDB.elementAt(i + 1, j + 1),
									(_Node) nodesDB.elementAt(i, j + 1),
									Canvas3D.MESH_Shell_C0_3, material,
									thickness);
							nf.set_Id(Id + "." + i + "." + j + ".1");
							nf.setShow(show);
							elementsDB.add(nf);
						} else if (this.areClose(
								(_Node) nodesDB.elementAt(i + 1, j),
								(_Node) nodesDB.elementAt(i + 1, j + 1))) {
							nf = new _Element3((_Node) nodesDB.elementAt(i, j),
									(_Node) nodesDB.elementAt(i + 1, j),
									(_Node) nodesDB.elementAt(i, j + 1),
									Canvas3D.MESH_Shell_C0_3, material,
									thickness);
							nf.set_Id(Id + "." + i + "." + j + ".1");
							nf.setShow(show);
							elementsDB.add(nf);
						} else if (this.areClose(
								(_Node) nodesDB.elementAt(i + 1, j + 1),
								(_Node) nodesDB.elementAt(i, j + 1))) {
							nf = new _Element3((_Node) nodesDB.elementAt(i, j),
									(_Node) nodesDB.elementAt(i + 1, j),
									(_Node) nodesDB.elementAt(i, j + 1),
									Canvas3D.MESH_Shell_C0_3, material,
									thickness);
							nf.set_Id(Id + "." + i + "." + j + ".1");
							nf.setShow(show);
							elementsDB.add(nf);
						} else if (this.areClose(
								(_Node) nodesDB.elementAt(i, j),
								(_Node) nodesDB.elementAt(i, j + 1))) {
							nf = new _Element3((_Node) nodesDB.elementAt(i, j),
									(_Node) nodesDB.elementAt(i + 1, j),
									(_Node) nodesDB.elementAt(i + 1, j + 1),
									Canvas3D.MESH_Shell_C0_3, material,
									thickness);
							nf.set_Id(Id + "." + i + "." + j + ".1");
							nf.setShow(show);
							elementsDB.add(nf);
						} else {
							ne = new _Element4((_Node) nodesDB.elementAt(i, j),
									(_Node) nodesDB.elementAt(i + 1, j),
									(_Node) nodesDB.elementAt(i + 1, j + 1),
									(_Node) nodesDB.elementAt(i, j + 1), type,
									material, thickness);
							ne.set_Id(Id + "." + i + "." + j);
							ne.setShow(show);
							elementsDB.add(ne);
						}
						break;
					case Canvas3D.MESH_Shell_C0_3:
						nf = new _Element3((_Node) nodesDB.elementAt(i, j),
								(_Node) nodesDB.elementAt(i + 1, j),
								(_Node) nodesDB.elementAt(i + 1, j + 1), type,
								material, thickness);
						nf.set_Id(Id + "." + i + "." + j + ".1");
						nf.setShow(show);
						elementsDB.add(nf);
						nf = new _Element3((_Node) nodesDB.elementAt(i + 1,
								j + 1), (_Node) nodesDB.elementAt(i, j + 1),
								(_Node) nodesDB.elementAt(i, j), type,
								material, thickness);
						nf.set_Id(Id + "." + i + "." + j + ".2");
						nf.setShow(show);
						elementsDB.add(nf);
						break;
					case Canvas3D.MESH_Contact_Triangle:
						nf = new _Element3((_Node) nodesDB.elementAt(i, j),
								(_Node) nodesDB.elementAt(i + 1, j),
								(_Node) nodesDB.elementAt(i + 1, j + 1), type,
								material, thickness);
						nf.set_Id(Id + "." + i + "." + j + ".1");
						nf.setShow(show);
						elementsDB.add(nf);
						nf = new _Element3((_Node) nodesDB.elementAt(i + 1,
								j + 1), (_Node) nodesDB.elementAt(i, j + 1),
								(_Node) nodesDB.elementAt(i, j), type,
								material, thickness);
						nf.set_Id(Id + "." + i + "." + j + ".2");
						nf.setShow(show);
						elementsDB.add(nf);
						break;
					case Canvas3D.MESH_Dummy_4:
						ne = new _Element4((_Node) nodesDB.elementAt(i, j),
								(_Node) nodesDB.elementAt(i + 1, j),
								(_Node) nodesDB.elementAt(i + 1, j + 1),
								(_Node) nodesDB.elementAt(i, j + 1), type,
								material, thickness);
						ne.set_Id(Id + "." + i + "." + j);
						ne.setShow(show);
						elementsDB.add(ne);
						break;
					}
				}
				if (i < mesh_div[0] - 1)
					elementsDB.addRow();
			}
		}
	}

	public void setSelected(boolean sel) {
		selected = sel;
	}

	public boolean isSelected() {
		return selected;
	}

	public void deselectRequiredObjects() {
		// Do nothing. Points and curves are in subclasses
	}

	public String toString() {
		return "Nurb Surface ID=" + Id;
	}

	public void transform3D(Matrix3D t) {
		if (!selected || processed)
			return;
		for (int i = 0; i < controlPoints.length; i++)
			for (int j = 0; j < controlPoints[0].length; j++) {
				controlPoints[i][j].setSelected(true);
				controlPoints[i][j].transform3D(t);
				controlPoints[i][j].setSelected(true);
			}
		reset(true);
	}

	public MutableTreeNode get_TreeNode() {
		DefaultMutableTreeNode surface = new DefaultMutableTreeNode(this);

		if (by_constructor) {
			surface.add(g_m.get_TreeNode());
			return surface;
		} else

			return g_m.get_TreeNode();
	}

	public JPanel getEditPanel(Canvas3D j3d, PreProcessor pp) {
		J3D = j3d;
		PreP = pp;
		cTextField1 = new JTextField("" + thickness);

		JPanel cPanel0 = new JPanel();
		cPanel0.setLayout(new BoxLayout(cPanel0, BoxLayout.Y_AXIS));
		cComboBox1 = new JComboBox(type);
		cComboBox2 = new JComboBox();
		JPanel cPanel1 = new JPanel();
		JPanel cPanel2 = new JPanel();

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

		cPanel0.add(cPanel1);
		cPanel1.setLayout(new GridLayout());
		cPanel1.add(new JLabel("Thickness"), null);
		cPanel1.add(cTextField1, null);
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

		cPanel0.add(cPanel2);
		cPanel2.setLayout(new BorderLayout());
		cPanel2.add(new JLabel("Material"), BorderLayout.WEST);
		cPanel2.add(cComboBox2, BorderLayout.EAST);
		cComboBox2.setActionCommand("");
		cComboBox2.setLightWeightPopupEnabled(false);
		cComboBox2.addKeyListener(new KeyAdapter() {

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

		for (Enumeration en = PreP.MatDB.keys(); en.hasMoreElements();)
			cComboBox2.addItem(en.nextElement());
		if (material_index == null)
			cComboBox2.setSelectedIndex(0);
		else
			cComboBox2.setSelectedItem(material_index);

		return cPanel0;
	}

	protected void update(ActionEvent e) {
		mesh_type_index = cComboBox1.getSelectedIndex();
		if (mesh_type_index == 1)
			msh_type = Canvas3D.MESH_Shell_BT_4;
		if (mesh_type_index == 2)
			msh_type = Canvas3D.MESH_Shell_C0_3;
		if (mesh_type_index == 3)
			msh_type = Canvas3D.MESH_Contact_Triangle;
		if (mesh_type_index == 0)
			msh_type = Canvas3D.MESH_Dummy_4;

		thickness = Float.parseFloat(cTextField1.getText());
		material_index = cComboBox2.getSelectedItem();
		material = (Material) PreP.MatDB.get(material_index + "");
	}

	public _Node[] get_Nodes() {
		_Node[] arr = new _Node[0];
		return arr;
	}

	public _Object[] get_Elements() {
		_Object[] arr = new _Object[0];
		return arr;
	}

	public _Object duplicate(Canvas3D out, boolean add) {
		_NurbSurface o = null;
		try {
			o = (_NurbSurface) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		if (add)
			out.add3D(o);

		return o;
	}

	public void insertKnots(float[] ins, boolean udir) {
		_CtrlPoint[][] newcontrolPoints;
		float[] newknot;

		if (ins.length == 0)
			return;

		if (udir) {
			newcontrolPoints = new _CtrlPoint[controlPoints.length + ins.length][controlPoints[0].length];
			newknot = new float[controlPoints.length + ins.length + uorder + 1];
		} else {
			controlPoints = lib3D.swapUV(controlPoints);
			newcontrolPoints = new _CtrlPoint[controlPoints.length + ins.length][controlPoints[0].length];
			newknot = new float[controlPoints.length + ins.length + vorder + 1];
		}

		lib3D.refineKnotVectSurface((udir ? uorder : vorder), (udir ? uknots
				: vknots), controlPoints, ins, newknot, newcontrolPoints);

		if (udir)
			uknots = newknot;
		else
			vknots = newknot;

		controlPoints = newcontrolPoints;

		if (!udir)
			controlPoints = lib3D.swapUV(controlPoints);

	}

	public void elevate(int times, boolean udir) {
		_CtrlPoint[] ctrlPoints, tmp;
		float[] knots;
		int nr_of_strips, order;
		Vector vtmp = new Vector();

		// Set up the right arrays depending on direction chosen
		if (udir) {

			ctrlPoints = new _CtrlPoint[controlPoints.length];
			knots = new float[uknots.length];
			nr_of_strips = controlPoints[0].length;
			order = uorder;

		} else {

			ctrlPoints = new _CtrlPoint[controlPoints[0].length];
			knots = new float[vknots.length];
			nr_of_strips = controlPoints.length;
			order = vorder;

		}

		// Dimension for worst case
		_CtrlPoint[] newcontrolPoints = new _CtrlPoint[ctrlPoints.length
				* (times + 1)];
		float[] newknots = new float[knots.length * (times + 1)];
		int[] cl = new int[3];

		// loop through all the slices and elevate them strip by strip

		for (int strip = 0; strip < nr_of_strips; strip++) {

			// Create the new strip
			if (udir) {

				for (int i = 0; i < ctrlPoints.length; i++)
					ctrlPoints[i] = new _CtrlPoint(controlPoints[i][strip]);
				for (int i = 0; i < knots.length; i++)
					knots[i] = uknots[i];

			} else {

				for (int i = 0; i < ctrlPoints.length; i++)
					ctrlPoints[i] = new _CtrlPoint(controlPoints[strip][i]);
				for (int i = 0; i < knots.length; i++)
					knots[i] = vknots[i];

			}

			// Elevate it
			cl = lib3D.degreeElevateCurve(order, knots, ctrlPoints, times,
					newknots, newcontrolPoints);

			// Assign the new control points to the temporary vector
			tmp = new _CtrlPoint[cl[1]];
			for (int i = 0; i < cl[1]; i++)
				tmp[i] = new _CtrlPoint(newcontrolPoints[i]);

			vtmp.add(tmp);

		}

		// Update the knot vector and controlpoints
		if (udir) {
			uorder = cl[2];
			uknots = new float[cl[0]];
			for (int i = 0; i < cl[0]; i++)
				uknots[i] = newknots[i];

			controlPoints = new _CtrlPoint[cl[1]][nr_of_strips];
			for (int i = 0; i < cl[1]; i++)
				controlPoints[i] = new _CtrlPoint[nr_of_strips];
			for (int i = 0; i < cl[1]; i++)
				for (int j = 0; j < nr_of_strips; j++)
					controlPoints[i][j] = ((_CtrlPoint[]) vtmp.elementAt(j))[i];

		} else {

			vorder = cl[2];
			vknots = new float[cl[0]];
			for (int i = 0; i < cl[0]; i++)
				vknots[i] = newknots[i];

			controlPoints = new _CtrlPoint[nr_of_strips][cl[1]];
			for (int i = 0; i < nr_of_strips; i++)
				controlPoints[i] = (_CtrlPoint[]) vtmp.elementAt(i);

		}

	}

	public Vector3D getCenter() {
		int numUControlPoints = controlPoints.length;
		int numVControlPoints = controlPoints[0].length;

		_Point s = lib3D.surfacePoint(numUControlPoints, uorder, uknots,
				numVControlPoints, vorder, vknots, controlPoints, 0.5f, 0.5f);

		return s.getVector();
	}

	public boolean isPickPoint(int x, int y, boolean shw, boolean ogl) {
		boolean check = false;
		int s = geomUDB.size() + geomVDB.size() + geomBDB.size() - 3;

		// Select this nurb surface if geometry is picked
		if (show == shw)
			for (int i = 0; i < s; i++)
				check = (((shp) arr.elementAt(i)).isPickPoint(x, y, ogl) == true ? true
						: check);

		return check;
	}

	public boolean isPickPoint(Rectangle2D r, boolean shw, boolean ogl) {
		boolean check = true;
		int s = geomUDB.size() + geomVDB.size() + geomBDB.size() - 3;

		if (show == shw)
			for (int i = 0; i < s; i++)
				check = (((shp) arr.elementAt(i)).isPickPoint(r, ogl) == false ? false
						: check);

		return check;
	}

	private boolean areClose(_Node n1, _Node n2) {
		float d, dx, dy, dz;

		dx = n2.x - n1.x;
		dy = n2.y - n1.y;
		dz = n2.z - n1.z;

		d = dx * dx + dy * dy + dz * dz;

		if (d > J3D.getNODE_MERGE_TOLERANCE() * J3D.getNODE_MERGE_TOLERANCE())
			return false;

		return true;
	}

	private static int getSortedPosition(Point2D p1, Point2D p2, Vector v) {
		double a1, a2;
		Point2D p;
		int i = 0;

		a2 = lib2D.angle(p1, p2);

		while (i < v.size()) {
			p = (Point2D) v.elementAt(i);

			a1 = lib2D.angle(p1, p);

			if (a1 > a2)
				break;

			i++;
		}

		return i;
	}

	public _Object[] getBorderObjects() {
		_NurbCurve[] c = new _NurbCurve[4];
		_CtrlPoint[] tmp = new _CtrlPoint[controlPoints.length];

		c[0] = new _NurbCurve(controlPoints[0], vknots, vorder, 5,
				Canvas3D.MESH_Dummy_2, 1.0f, null, J3D);

		for (int i = 0; i < controlPoints.length; i++)
			tmp[i] = controlPoints[i][0];
		c[1] = new _NurbCurve(tmp, uknots, uorder, 5, Canvas3D.MESH_Dummy_2,
				1.0f, null, J3D);

		c[2] = new _NurbCurve(controlPoints[controlPoints.length - 1], vknots,
				vorder, 5, Canvas3D.MESH_Dummy_2, 1.0f, null, J3D);

		for (int i = 0; i < controlPoints.length; i++)
			tmp[i] = controlPoints[i][controlPoints[0].length - 1];
		c[3] = new _NurbCurve(tmp, uknots, uorder, 5, Canvas3D.MESH_Dummy_2,
				1.0f, null, J3D);

		// Set mesh density
		/*
		 * c[0].mesh_size = c[0].getLength() / (float)mesh_div[0];
		 * c[1].mesh_size = c[1].getLength() / (float)mesh_div[1];
		 * c[2].mesh_size = c[2].getLength() / (float)mesh_div[2];
		 * c[3].mesh_size = c[3].getLength() / (float)mesh_div[3];
		 */

		c[0].mesh_size = mesh_size;
		c[1].mesh_size = mesh_size;
		c[2].mesh_size = mesh_size;
		c[3].mesh_size = mesh_size;

		return c;
	}

	public _Point getPointAt(float u, float v) {
		int numUControlPoints = controlPoints.length;
		int numVControlPoints = controlPoints[0].length;

		return lib3D.surfacePoint(numUControlPoints, uorder, uknots,
				numVControlPoints, vorder, vknots, controlPoints, u, v);
	}

	public void replaceObjectWith(_Object o, _Object replacement) {
		// Do nothing, points and curves are in subclasses
	}

}

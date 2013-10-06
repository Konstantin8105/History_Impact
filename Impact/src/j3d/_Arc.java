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
 * author  Jonas Forssell
 */
package j3d;

import java.awt.*;
import java.io.*;
import java.util.Hashtable;
import javax.swing.tree.*;
import javax.swing.*;

import java.awt.event.*;

import com.stevesoft.pat.Regex;

import gui.*;

/**
 * Creates a nurbcurve in the shape of an arc (or circle)
 * 
 * @author Jonas Forssell, Yuriy Mikhaylovskiy
 */
public class _Arc extends _NurbCurve implements Serializable {
	private _Point p_start, p_center, p_plane;
	private float angle;
	private Hashtable mat;
	private static Regex r_index = new Regex("^ *([0-9]+) *");
	private static Regex r_spoint = new Regex("(?i)SPOINT *= *([0-9]+)");
	private static Regex r_cpoint = new Regex("(?i)CPOINT *= *([0-9]+)");
	private static Regex r_ppoint = new Regex("(?i)PPOINT *= *([0-9]+)");
	private static Regex r_angle = new Regex(
			"(?i)ANGLE *= *([-+]?[0-9]+[.]?[0-9]*(?:[eE][-+]?[0-9]+)?)");

	JTextField jTextField1, jTextField2, jTextField3, jTextField4;
	JButton jButton2, jButton3, jButton4;

	/**
	 * Creates a new arc which starts at the start point, has a center at the
	 * given center point and has a given angle. The arc plane is indicated by a
	 * third point. Together with the start and center point, the plane can be
	 * determined.
	 * 
	 * @param p_start
	 *            Starting point (distance to center point sets radius)
	 * @param p_center
	 *            Center point
	 * @param p_plane
	 *            Any third point indicating the plane for the arc
	 * @param angle
	 *            Angle for the arc
	 * @param div
	 *            Number of line segments used to draw the arc
	 * @param color
	 *            Color of the arc
	 * @param element_type
	 *            element type to be used for the FE-mesh
	 * @param element_material
	 *            element material to be used for the FE-mesh
	 * @param element_diameter
	 *            diameter of the element in the mesh
	 */
	public _Arc(boolean add, Canvas3D J3D) {
		super(add, J3D);
		this.angle = 0.0f;
	}

	public String writeObject() {
		String st = new String(Id);
		st += "  SPOINT = " + p_start.get_Id() + "  CPOINT = "
				+ p_center.get_Id() + "  PPOINT = " + p_plane.get_Id();
		st += "  ANGLE = " + angle;
		st += super.writeObject();
		return st;
	}

	public void readObject(String st, Hashtable geo) {
		if (r_index.search(st))
			Id = r_index.stringMatched(1);
		if (r_spoint.search(st))
			p_start = (_Point) geo.get(r_spoint.stringMatched(1));
		if (r_cpoint.search(st))
			p_center = (_Point) geo.get(r_cpoint.stringMatched(1));
		if (r_ppoint.search(st))
			p_plane = (_Point) geo.get(r_ppoint.stringMatched(1));
		if (r_angle.search(st))
			angle = Float.parseFloat(r_angle.stringMatched(1));
		super.readObject(st);
	}

	public void reset(boolean do_mesh) {
		float a_start, a_end, ang, dang, w1, radius, a, to_rad, to_deg;
		int narcs, n, index, j;
		Vector3D P0 = new Vector3D();
		Vector3D T0 = new Vector3D();
		Vector3D P2 = new Vector3D();
		Vector3D T2 = new Vector3D();
		Vector3D P1 = new Vector3D();
		Vector3D X = new Vector3D();
		Vector3D Y = new Vector3D();
		Vector3D Z = new Vector3D();
		Vector3D O = new Vector3D();
		Vector3D[] Pw;
		float[] weight;
		Vector3D Temp1 = new Vector3D();

		// Inititalize
		Vector3D vp_start = p_start.getVector();
		Vector3D vp_center = p_center.getVector();
		Vector3D vp_plane = p_plane.getVector();

		O.copy(vp_center);
		to_rad = (float) Math.PI / 180f;
		to_deg = (float) 180f / (float) Math.PI;

		// Create a local coordinate system
		X.sub(vp_start, vp_center);
		Y.sub(vp_plane, vp_center);
		Z.cross(X, Y);

		if (angle < 0)
			Z.scale(-1.0f);

		Y.cross(Z, X);

		radius = X.getLength();

		X.toUnitLength();
		Y.toUnitLength();
		Z.toUnitLength();

		// Determine the arc angles
		a_start = 0;
		a_end = a_start + Math.abs(angle);
		if (a_end < a_start)
			a_end = (float) 360.0 + a_end;
		ang = a_end - a_start;

		if (ang <= 90)
			narcs = 1;
		else if (ang <= 180)
			narcs = 2;
		else if (ang <= 270)
			narcs = 3;
		else
			narcs = 4;

		dang = ang / narcs;
		n = 2 * narcs + 1;
		w1 = (float) Math.cos(dang * to_rad / 2.0f);

		// Generate the control points
		Pw = new Vector3D[n];
		controlPoints = new _CtrlPoint[n];
		weight = new float[n];
		for (int i = 0; i < n; i++)
			Pw[i] = new Vector3D();

		Temp1.copy(Y);
		Temp1.scale(radius * (float) Math.sin(a_start * to_rad));
		P0.copy(X);
		P0.scale(radius * (float) Math.cos(a_start * to_rad));
		P0.add(Temp1, P0);
		P0.add(O, P0);

		Temp1.copy(Y);
		Temp1.scale((float) Math.cos(a_start * to_rad));
		T0.copy(X);
		T0.scale((float) -Math.sin(a_start * to_rad));
		T0.add(Temp1, T0);

		Pw[0].copy(P0);
		index = 0;
		a = a_start;
		weight[0] = 1.0f;

		for (int i = 1; i <= narcs; i++) {
			a += dang;

			Temp1.copy(Y);
			Temp1.scale(radius * (float) Math.sin(a * to_rad));
			P2.copy(X);
			P2.scale(radius * (float) Math.cos(a * to_rad));
			P2.add(Temp1, P2);
			P2.add(O, P2);

			Pw[index + 2].copy(P2);
			weight[index + 2] = 1;

			Temp1.copy(Y);
			Temp1.scale((float) Math.cos(a * to_rad));
			T2.copy(X);
			T2.scale((float) -Math.sin(a * to_rad));
			T2.add(Temp1, T2);

			P1 = lib3D.intersection(P0, T0, P2, T2);

			Pw[index + 1].copy(P1);
			Pw[index + 1].scale(w1);
			weight[index + 1] = w1;

			index += 2;

			if (i < narcs) {
				P0.copy(P2);
				T0.copy(T2);
			}

		}

		for (int i = 0; i < n; i++)
			controlPoints[i] = new _CtrlPoint(Pw[i].x, Pw[i].y, Pw[i].z,
					weight[i], Color.GRAY);

		// Generate the knots
		j = 2 * narcs + 1;
		knots = new float[j + 3];
		order = 2;

		for (int i = 0; i < 3; i++) {
			knots[i] = 0.0f;
			knots[i + j] = 1.0f;
		}

		switch (narcs) {
		case 1:
			break;
		case 2:
			knots[3] = 0.5f;
			knots[4] = 0.5f;
			break;
		case 3:
			knots[3] = 1.0f / 3.0f;
			knots[4] = knots[3];
			knots[5] = 2.0f / 3.0f;
			knots[6] = knots[5];
			break;
		case 4:
			knots[3] = 0.25f;
			knots[4] = knots[3];
			knots[5] = 0.5f;
			knots[6] = knots[5];
			knots[7] = 0.75f;
			knots[8] = knots[7];
			break;
		}

		// Generate geometry & mesh
		super.reset(do_mesh);

	}

	public String toString() {
		return "Arc ID=" + Id;
	}

	public MutableTreeNode get_TreeNode() {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(this);

		// Add unique data for the _Arc
		node.add(p_start.get_TreeNode());
		node.add(p_center.get_TreeNode());
		node.add(p_plane.get_TreeNode());

		// Add mesh data from _NurbCurve
		node.add(super.get_TreeNode());

		return node;
	}

	public JPanel getEditPanel(Canvas3D j3d, PreProcessor pp) {
		this.J3D = j3d;
		this.PreP = pp;

		JPanel jPanel0 = new JPanel();
		JPanel jPanel1 = new JPanel();
		JPanel jPanel4 = new JPanel();
		JPanel jPanel5 = new JPanel();
		jButton1 = new JButton(add == true ? "Add" : "Update");
		jButton2 = new JButton("<<");
		jButton3 = new JButton("<<");
		jButton4 = new JButton("<<");
		jTextField1 = new JTextField(
				(p_start != null ? p_start.toString() : "") + "", 3);
		jTextField2 = new JTextField((p_center != null ? p_center.toString()
				: "") + "", 3);
		jTextField3 = new JTextField(
				(p_plane != null ? p_plane.toString() : "") + "", 3);
		jTextField4 = new JTextField("" + angle);

		// Add all geometry data
		jPanel0.setLayout(new BoxLayout(jPanel0, BoxLayout.Y_AXIS));
		jPanel0.add(jPanel1);
		jPanel1.setLayout(new GridLayout(3, 3));
		jPanel1.add(new JLabel("Strt Pnt"), null);
		jPanel1.add(jTextField1, null);
		jTextField1.setEditable(false);
		jPanel1.add(jButton2, null);
		jButton2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton2_actionPerformed(e);
			}
		});
		jButton2.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					jButton2_actionPerformed(null);
				}
			}
		});
		jPanel1.add(new JLabel("Ctr Pnt"), null);
		jPanel1.add(jTextField2, null);
		jTextField2.setEditable(false);
		jPanel1.add(jButton3, null);
		jButton3.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton3_actionPerformed(e);
			}
		});
		jButton3.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					jButton3_actionPerformed(null);
				}
			}
		});
		jPanel1.add(new JLabel("Pln Pnt"), null);
		jPanel1.add(jTextField3, null);
		jTextField3.setEditable(false);
		jPanel1.add(jButton4, null);
		jButton4.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton4_actionPerformed(e);
			}
		});
		jButton4.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					jButton4_actionPerformed(null);
				}
			}
		});

		jPanel0.add(jPanel4);
		jPanel4.setLayout(new GridLayout());
		jPanel4.add(new JLabel("Angle"), null);
		jPanel4.add(jTextField4, null);
		jTextField4.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					cComboBox1.requestFocus();
				}
			}
		});

		// Add all mesh data
		jPanel0.add(super.getEditPanel(j3d, pp));

		// Finally, the update button
		jPanel0.add(jPanel5);
		jPanel5.add(jButton1);
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton1_actionPerformed(e);
			}
		});
		jButton1.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					jButton1_actionPerformed(null);
				}
			}
		});

		jPanel0.validate();

		return jPanel0;
	}

	void jButton1_actionPerformed(ActionEvent e) {
		_Arc tmp = this;

		if (p_start == null || p_center == null || p_plane == null)
			return;
		angle = Float.parseFloat(jTextField4.getText());

		tmp.update(e);

		if (add == true) {
			try {
				tmp = (_Arc) this.clone();
				tmp.prepareGroups();
				J3D.add3D(tmp);
			} catch (CloneNotSupportedException e1) {
				e1.printStackTrace();
			}
			tmp.add = false;
			jButton2.requestFocus();
		} else
			tmp.reset(true);

		J3D.tree_reset();
		J3D.view_reset();
		J3D.repaint();
	}

	void jButton2_actionPerformed(ActionEvent e) {
		Object obj = J3D.getSelectedObject3D();
		if (obj != null && obj instanceof _Point) {
			p_start = (_Point) obj;
			jTextField1.setText(obj.toString());
			jButton3.requestFocus();
		}
	}

	void jButton3_actionPerformed(ActionEvent e) {
		Object obj = J3D.getSelectedObject3D();
		if (obj != null && obj instanceof _Point) {
			p_center = (_Point) obj;
			jTextField2.setText(obj.toString());
			jButton4.requestFocus();
		}
	}

	void jButton4_actionPerformed(ActionEvent e) {
		Object obj = J3D.getSelectedObject3D();
		if (obj != null && obj instanceof _Point) {
			p_plane = (_Point) obj;
			jTextField3.setText(obj.toString());
			jTextField4.requestFocus();
			jTextField4.selectAll();
		}
	}

	public _Object duplicate(Canvas3D out, boolean add) {
		_Arc o = null;
		try {
			o = (_Arc) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		o.p_center = (_Point) p_center.duplicate(out, add);
		o.p_plane = (_Point) p_plane.duplicate(out, add);
		o.p_start = (_Point) p_start.duplicate(out, add);

		if (add)
			out.add3D(o);

		return o;
	}

	public void transform3D(Matrix3D t) {
		if (!selected || processed)
			return;
		super.transform3D(t);
		p_center.setSelected(true);
		p_center.transform3D(t);
		p_center.setSelected(true);
		p_start.setSelected(true);
		p_start.transform3D(t);
		p_start.setSelected(true);
		p_plane.setSelected(true);
		p_plane.transform3D(t);
		p_plane.setSelected(true);
		reset(true);
	}

	public void requestFocus() {
		jButton2.requestFocus();
	}

	public void requestAction() {
		if (add == true)
			if (jButton2.hasFocus())
				jButton2_actionPerformed(null);
			else if (jButton3.hasFocus())
				jButton3_actionPerformed(null);
			else if (jButton4.hasFocus())
				jButton4_actionPerformed(null);
	}

	public void deselectRequiredObjects() {
		if (!selected) {
			p_center.setSelected(false);
			p_start.setSelected(false);
			p_plane.setSelected(false);
		}
	}

	public void replaceObjectWith(_Object o, _Object replacement) {
		if (!(o instanceof _Point) || !(replacement instanceof _Point))
			return;

		if (p_center == (_Point) o)
			p_center = (_Point) replacement;
		if (p_start == (_Point) o)
			p_start = (_Point) replacement;
		if (p_plane == (_Point) o)
			p_plane = (_Point) replacement;

	}

}

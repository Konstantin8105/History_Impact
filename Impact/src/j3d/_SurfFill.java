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
import javax.swing.tree.*;
import javax.swing.*;
import java.util.*;

import com.stevesoft.pat.Regex;

import java.awt.event.*;

import gui.*;

/**
 * Creates a nurb surface defined by four edge curves
 * 
 * @author Jonas Forssell, Yuriy Mikhaylovskiy
 */
public class _SurfFill extends _NurbSurface implements Serializable {
	private _NurbCurve[] curve = new _NurbCurve[4];

	JTextField jTextField1, jTextField2, jTextField3, jTextField4, jTextField5,
			jTextField6, jTextField7;
	JButton jButton2, jButton3, jButton4, jButton5;
	JComboBox jComboBox1, jComboBox2;

	private static Regex r_index = new Regex("^ *([0-9]+) *");
	private static Regex r_curve = new Regex(
			"(?i)CURVES *= *\\[ *([0-9, ]+) *\\]");
	private static Regex r_indexer = new Regex("\\G,? *([0-9]+) *,?");

	/**
	 * Constructs a NURB surface defined by four corner curves
	 * 
	 * 
	 */
	public _SurfFill(boolean add, Canvas3D J3D) {
		super(add, J3D);
	}

	public String writeObject() {
		String st = new String(Id);
		st += "  CURVES = [";
		for (int i = 0; i < curve.length; i++) {
			st += ((_NurbCurve) curve[i]).get_Id();
			if (i < curve.length - 1)
				st += ", ";
		}
		st += "] ";
		st += super.writeObject();
		return st;
	}

	public void readObject(String st, Hashtable geo) {
		int i = 0;
		if (r_index.search(st))
			Id = r_index.stringMatched(1);
		if (r_curve.search(st))
			while (r_indexer.search(r_curve.stringMatched(1)))
				curve[i++] = (_NurbCurve) geo.get(r_indexer.stringMatched(1));
		super.readObject(st);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		curve = (_NurbCurve[]) in.readObject();
	}

	public void reset(boolean do_mesh) {
		int i, j;
		_NurbCurve tcurve[];
		_SurfRul A, B;
		_SurfBil C;
		_Point[] tmp, pnt;
		int li, lj;
		float[] muknt, mvknt;

		// 0. Initialize
		// tcurve = new _NurbCurve[curve.length];
		// for (i=0; i<curve.length; i++)
		// tcurve[i] = (_NurbCurve)curve[i].copy();

		// Make sure the curves are in right order and direction
		pnt = new _Point[4];

		tmp = curve[0].getEndPoints();
		pnt[0] = tmp[0];
		pnt[1] = tmp[1];

		tmp = curve[1].getEndPoints();

		if (!lib3D.isClose(pnt[1], tmp[0], J3D.getGeometricTolerance())
				&& !lib3D.isClose(pnt[1], tmp[1], J3D.getGeometricTolerance())) {
			curve[0].reverseDirection();
			tmp = curve[0].getEndPoints();
			pnt[0] = tmp[0];
			pnt[1] = tmp[1];
		}

		for (i = 1; i < 4; i++) {
			tmp = curve[i].getEndPoints();

			if (!lib3D.isClose(pnt[1], tmp[0], J3D.getGeometricTolerance())
					&& !lib3D.isClose(pnt[1], tmp[1],
							J3D.getGeometricTolerance()))
				throw new IllegalArgumentException(
						"Illegal input of curves. Must be in a circle");

			if (lib3D.isClose(pnt[1], tmp[1], J3D.getGeometricTolerance())) {
				curve[i].reverseDirection();
				tmp = curve[i].getEndPoints();
			}

			pnt[0] = tmp[0];
			pnt[1] = tmp[1];
		}

		curve[3].reverseDirection();
		curve[2].reverseDirection();

		tmp = curve[0].getEndPoints();
		pnt[0] = tmp[0];
		pnt[1] = tmp[1];

		tmp = curve[2].getEndPoints();
		pnt[2] = tmp[0];
		pnt[3] = tmp[1];

		// 1. Generate a lofted surface A
		A = new _SurfRul(curve[0], curve[2], Canvas3D.MESH_Dummy_4, thickness,
				material, J3D);

		// 2. Generate a lofted surface B
		B = new _SurfRul(curve[1], curve[3], Canvas3D.MESH_Dummy_4, thickness,
				material, J3D);

		// 3. Generate a bilinear surface C
		C = new _SurfBil(pnt, Canvas3D.MESH_Dummy_4, thickness, material, J3D);

		// 4. Degree elevete surface A to surface B u-direction degree
		A.elevate(B.uorder - A.vorder, false);

		// 5. Degree elevate surface B to surface A u-direction degree
		B.elevate(A.uorder - B.vorder, false);

		// 6. Degree elevate surface C in both directions to A & B degrees
		C.elevate(A.uorder - C.uorder, true);
		C.elevate(B.uorder - C.vorder, false);

		// 7. Merge knot vectors to obtain a common vector in u and v direction
		muknt = lib3D.mergeKnots(A.uknots, B.vknots);
		muknt = lib3D.mergeKnots(muknt, C.uknots);

		mvknt = lib3D.mergeKnots(A.vknots, B.uknots);
		mvknt = lib3D.mergeKnots(mvknt, C.vknots);

		// 8. Insert missing knots into surface A
		A.insertKnots(lib3D.diffKnots(muknt, A.uknots), true);
		A.insertKnots(lib3D.diffKnots(mvknt, A.vknots), false);

		// 9. Insert missing knots into surface B - reversed due to orientation
		B.insertKnots(lib3D.diffKnots(muknt, B.vknots), false);
		B.insertKnots(lib3D.diffKnots(mvknt, B.uknots), true);

		// 10. Insert missing knots into surface C
		C.insertKnots(lib3D.diffKnots(muknt, C.uknots), true);
		C.insertKnots(lib3D.diffKnots(mvknt, C.vknots), false);

		// 11. S(u,v) = S1(u,v) + S2(u,v) - T(u,v) (do this on the control
		// points)
		// Note the reversed index on surface B since the orientation is
		// different
		li = A.controlPoints.length;
		lj = A.controlPoints[0].length;
		controlPoints = new _CtrlPoint[li][lj];
		for (i = 0; i < li; i++)
			for (j = 0; j < lj; j++)
				controlPoints[i][j] = new _CtrlPoint(A.controlPoints[i][j].x
						+ B.controlPoints[j][i].x - C.controlPoints[i][j].x,
						A.controlPoints[i][j].y + B.controlPoints[j][i].y
								- C.controlPoints[i][j].y,
						A.controlPoints[i][j].z + B.controlPoints[j][i].z
								- C.controlPoints[i][j].z,
						A.controlPoints[i][j].w * B.controlPoints[j][i].w
								* C.controlPoints[i][j].w, Color.GRAY);

		// 12. Generate the knots
		uorder = A.uorder;
		uknots = muknt;

		vorder = A.vorder;
		vknots = mvknt;

		// 13. Set mesh data
		mesh_div[0] = curve[0].mesh_div;
		mesh_div[1] = curve[3].mesh_div;
		mesh_div[2] = curve[2].mesh_div;
		mesh_div[3] = curve[1].mesh_div;

		// 14. Generate geometry & mesh
		super.reset(do_mesh);
	}

	public String toString() {
		return "FillSurface ID=" + Id;
	}

	public MutableTreeNode get_TreeNode() {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(this);

		// Add unique data for this class
		for (int i = 0; i < 4; i++)
			node.add(curve[i].get_TreeNode());

		// Add mesh data from _NurbCurve
		node.add(super.get_TreeNode());

		return node;
	}

	public JPanel getEditPanel(Canvas3D j3d, PreProcessor pp) {
		this.J3D = j3d;
		this.PreP = pp;

		JPanel jPanel0 = new JPanel();
		JPanel jPanel1 = new JPanel();
		JPanel jPanel7 = new JPanel();
		jButton1 = new JButton(add == true ? "Add" : "Update");
		jButton2 = new JButton("<<");
		jButton3 = new JButton("<<");
		jButton4 = new JButton("<<");
		jButton5 = new JButton("<<");
		jTextField1 = new JTextField((curve[0] != null ? curve[0].toString()
				: "") + "", 3);
		jTextField2 = new JTextField((curve[1] != null ? curve[1].toString()
				: "") + "", 3);
		jTextField3 = new JTextField((curve[2] != null ? curve[2].toString()
				: "") + "", 3);
		jTextField4 = new JTextField((curve[3] != null ? curve[3].toString()
				: "") + "", 3);

		jPanel0.setLayout(new BoxLayout(jPanel0, BoxLayout.Y_AXIS));
		jPanel0.add(jPanel1);
		jPanel1.setLayout(new GridLayout(4, 3));

		jPanel1.add(new JLabel("Curve 1"), null);
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
		jPanel1.add(new JLabel("Curve 2"), null);
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
		jPanel1.add(new JLabel("Curve 3"), null);
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
		jPanel1.add(new JLabel("Curve 4"), null);
		jPanel1.add(jTextField4, null);
		jTextField4.setEditable(false);
		jPanel1.add(jButton5, null);
		jButton5.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton5_actionPerformed(e);
			}
		});
		jButton5.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					jButton5_actionPerformed(null);
				}
			}
		});

		jPanel0.add(super.getEditPanel(j3d, pp));

		jPanel0.add(jPanel7);
		jPanel7.add(jButton1);
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
		_SurfFill tmp = this;

		if (curve[0] == null || curve[1] == null || curve[2] == null
				|| curve[3] == null)
			return;

		tmp.update(e);

		if (add == true) {
			try {
				tmp = (_SurfFill) this.clone();
				tmp.prepareGroups();
				J3D.add3D(tmp);
			} catch (CloneNotSupportedException e1) {
				e1.printStackTrace();
			}

			tmp.add = false;
			jButton2.requestFocus();

			curve = new _NurbCurve[4];

		} else
			tmp.reset(true);

		J3D.tree_reset();
		J3D.view_reset();
		J3D.repaint();
	}

	void jButton2_actionPerformed(ActionEvent e) {
		boolean set = false;
		Object[] obj = J3D.getSelectedObjects3D();
		for (int i = 0; i < obj.length; i++)
			if (obj[i] != null && obj[i] instanceof _NurbCurve) {
				curve[0] = (_NurbCurve) obj[i];
				jTextField1.setText(obj[i].toString());
				set = true;
				jButton3.requestFocus();
			}

		if (!set) {
			curve[0] = null;
			jTextField1.setText("");
		}
	}

	void jButton3_actionPerformed(ActionEvent e) {
		boolean set = false;
		Object[] obj = J3D.getSelectedObjects3D();
		for (int i = 0; i < obj.length; i++)
			if (obj[i] != null && obj[i] instanceof _NurbCurve) {
				curve[1] = (_NurbCurve) obj[i];
				jTextField2.setText(obj[i].toString());
				set = true;
				jButton4.requestFocus();
			}

		if (!set) {
			curve[1] = null;
			jTextField2.setText("");
		}
	}

	void jButton4_actionPerformed(ActionEvent e) {
		boolean set = false;
		Object[] obj = J3D.getSelectedObjects3D();
		for (int i = 0; i < obj.length; i++)
			if (obj[i] != null && obj[i] instanceof _NurbCurve) {
				curve[2] = (_NurbCurve) obj[i];
				jTextField3.setText(obj[i].toString());
				set = true;
				jButton5.requestFocus();
			}

		if (!set) {
			curve[2] = null;
			jTextField3.setText("");
		}
	}

	void jButton5_actionPerformed(ActionEvent e) {
		boolean set = false;
		Object[] obj = J3D.getSelectedObjects3D();
		for (int i = 0; i < obj.length; i++)
			if (obj[i] != null && obj[i] instanceof _NurbCurve) {
				curve[3] = (_NurbCurve) obj[i];
				jTextField4.setText(obj[i].toString());
				set = true;
				cComboBox1.requestFocus();
			}

		if (!set) {
			curve[3] = null;
			jTextField4.setText("");
		}
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
			else if (jButton5.hasFocus())
				jButton5_actionPerformed(null);
	}

	public _Object duplicate(Canvas3D out, boolean add) {
		_SurfFill o = null;
		try {
			o = (_SurfFill) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		o.curve = new _NurbCurve[4];
		for (int i = 0; i < 4; i++)
			o.curve[i] = (_NurbCurve) curve[i].duplicate(out, add);

		if (add)
			out.add3D(o);

		return o;
	}

	public void transform3D(Matrix3D t) {
		if (!selected || processed)
			return;
		super.transform3D(t);
		for (int i = 0; i < 4; i++) {
			curve[i].setSelected(true);
			curve[i].transform3D(t);
			curve[i].setSelected(true);
			curve[i].setProcessed(true);
		}
		reset(true);
	}

	public void deselectRequiredObjects() {
		if (!selected)
			for (int i = 0; i < curve.length; i++) {
				curve[i].setSelected(false);
				curve[i].deselectRequiredObjects();
			}
	}

	public void replaceObjectWith(_Object o, _Object replacement) {
		if (!(o instanceof _NurbCurve) || !(replacement instanceof _NurbCurve))
			return;

		for (int i = 0; i < curve.length; i++)
			if (curve[i] == o)
				curve[i] = (_NurbCurve) replacement;

	}

}

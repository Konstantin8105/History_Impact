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
import java.util.*;

import javax.swing.tree.*;
import javax.swing.*;

import com.stevesoft.pat.Regex;

import java.awt.event.*;

import gui.*;

/**
 * Creates a nurbcurve in the shape of a Pline
 * 
 * @author Jonas Forssell, Yuriy Mikhaylovskiy
 */
public class _Spline extends _NurbCurve implements Serializable {
	public Vector points = new Vector();

	private JList table;
	private DefaultListModel tableModel;
	private JButton jButton2, jButton3;
	private JComboBox jComboBox1;
	private JCheckBox jCheckBox1;
	static String[] hdr = { "Points" };
	private String[] curvetype = { "Line", "Curve 2 deg", "Curve 3 deg" };
	private boolean fit_curve_to_points;
	private static Regex r_index = new Regex("^ *([0-9]+) *");
	private static Regex r_points = new Regex(
			"(?i)POINTS *= *\\[ *([0-9, ]+) *\\]");
	private static Regex r_fit = new Regex("(?i)FIT *= *(TRUE|FALSE)");
	private static Regex r_order = new Regex("(?i)DEGREE *= *([0-9])");
	private static Regex r_indexer = new Regex("\\G,? *([0-9]+) *,?");

	/**
	 * Creates a new Pline. The parameters are:
	 * 
	 * @param points
	 *            [] Starting point (distance to center point sets radius)
	 * @param color
	 *            Color of the arc
	 * @param element_type
	 *            element type to be used for the FE-mesh
	 * @param element_material
	 *            element material to be used for the FE-mesh
	 * @param element_diameter
	 *            diameter of the element in the mesh
	 */
	public _Spline(boolean add, Canvas3D J3D) {
		super(add, J3D);
		order = 1;
		fit_curve_to_points = true;
	}

	public String writeObject() {
		String st = new String(Id);
		st += "  POINTS = [";
		for (int i = 0; i < points.size(); i++) {
			st += ((_Point) points.elementAt(i)).get_Id();
			if (i < points.size() - 1)
				st += ", ";
		}
		st += "] ";
		st += "DEGREE = " + order;
		st += " FIT = " + (fit_curve_to_points == true ? "TRUE " : "FALSE ");
		st += super.writeObject();
		return st;
	}

	public void readObject(String st, Hashtable geo) {
		if (r_index.search(st))
			Id = r_index.stringMatched(1);
		if (r_points.search(st))
			while (r_indexer.search(r_points.stringMatched(1)))
				points.add(geo.get(r_indexer.stringMatched(1)));
		if (r_order.search(st))
			order = Integer.parseInt(r_order.stringMatched(1));
		if (r_fit.search(st))
			fit_curve_to_points = (r_fit.stringMatched(1).equals("TRUE") ? true
					: false);
		super.readObject(st);
	}

	public void reset(boolean do_mesh) {
		_Point p;
		int kl;

		// Generate the control points
		controlPoints = new _CtrlPoint[points.size()];

		for (int i = 0; i < points.size(); i++) {
			p = (_Point) points.elementAt(i);
			controlPoints[i] = new _CtrlPoint(p.x, p.y, p.z, 1.0f, Color.GRAY);
		}

		// Generate the knots
		kl = controlPoints.length + order + 1;
		knots = new float[kl];

		for (int i = 0; i <= order; i++) {
			knots[i] = 0.0f;
			knots[kl - 1 - i] = 1.0f;
		}

		for (int i = 1; i < controlPoints.length - order; i++) {
			knots[order + i] = (float) (i)
					/ (float) (controlPoints.length - order);
		}

		// Optimize controlpoints to fit the points
		if (points.size() > 2 && fit_curve_to_points == true) {
			_Point[] inpoints = new _Point[points.size()];
			for (int i = 0; i < inpoints.length; i++)
				inpoints[i] = (_Point) points.elementAt(i);

			lib3D.globalCurveInterp(inpoints, order, knots, controlPoints);
		}

		// Generate geometry & mesh
		super.reset(do_mesh);

	}

	public String toString() {
		return "Spline ID=" + Id;
	}

	public MutableTreeNode get_TreeNode() {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(this);

		// Add unique data for the _Spline
		for (int i = 0; i < points.size(); i++)
			node.add(((_Point) points.elementAt(i)).get_TreeNode());

		// Add mesh data from _NurbCurve
		node.add(super.get_TreeNode());

		return node;
	}

	public JPanel getEditPanel(Canvas3D j3d, PreProcessor pp) {
		this.J3D = j3d;
		this.PreP = pp;

		JPanel jPanel0 = new JPanel();
		JPanel jPanel1 = new JPanel();
		JPanel jPanel2 = new JPanel();
		JPanel jPanel3 = new JPanel();
		JPanel jPanel4 = new JPanel();

		jComboBox1 = new JComboBox(curvetype);
		jButton1 = new JButton(add == true ? "Add" : "Update");
		jButton2 = new JButton("Add Point");
		jButton3 = new JButton("Delete");
		jCheckBox1 = new JCheckBox("Fit curve to points", fit_curve_to_points);

		tableModel = new DefaultListModel();
		table = new JList(tableModel);
		for (int i = 0; i < points.size(); i++) {
			tableModel.addElement(((_Point) points.elementAt(i)).toString());
		}
		table.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_DELETE) {
					jButton2.requestFocus();
					jButton3_actionPerformed(null);
				}
			}
		});

		JScrollPane sp = new JScrollPane();
		sp.setPreferredSize(new Dimension(180, 200));

		// Add all geometry data
		jPanel0.setLayout(new BoxLayout(jPanel0, BoxLayout.Y_AXIS));
		jPanel0.add(sp);
		sp.getViewport().add(table, null);
		jPanel0.add(jPanel1);
		jPanel1.setLayout(new GridLayout(1, 2));
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

		jPanel1.add(jButton3, null);
		jButton3.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton3_actionPerformed(e);
			}
		});

		jPanel0.add(jPanel4);
		jPanel4.add(jCheckBox1);

		jPanel0.add(jPanel3);
		jPanel3.setLayout(new GridLayout(1, 2));
		jPanel3.add(new JLabel("Curve Type"));
		jPanel3.add(jComboBox1);
		jComboBox1.setSelectedIndex(order - 1);
		jComboBox1.addKeyListener(new KeyAdapter() {
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
		jPanel0.add(jPanel2);
		jPanel2.add(jButton1);
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
		_Spline tmp = this;

		order = jComboBox1.getSelectedIndex() + 1;
		if (points.size() <= order)
			return;
		fit_curve_to_points = jCheckBox1.isSelected();

		tmp.update(e);

		if (add == true) {
			try {
				tmp = (_Spline) this.clone();
				tmp.prepareGroups();
				J3D.add3D(tmp);
			} catch (CloneNotSupportedException e1) {
				e1.printStackTrace();
			}

			tmp.add = false;

			jButton2.requestFocus();
			points = new Vector();

			tableModel.removeAllElements();

		} else
			tmp.reset(true);

		J3D.tree_reset();
		J3D.view_reset();
		J3D.repaint();
	}

	void jButton2_actionPerformed(ActionEvent e) {
		Object[] obj = J3D.getSelectedObjects3D();
		for (int i = 0; i < obj.length; i++)
			if (obj[i] != null && obj[i] instanceof _Point) {
				points.add((_Point) obj[i]);
				tableModel.addElement(obj[i].toString());
				jComboBox1.requestFocus();
			}
	}

	void jButton3_actionPerformed(ActionEvent e) {
		int[] index = table.getSelectedIndices();

		for (int i = index.length - 1; i >= 0; i--) {
			tableModel.removeElementAt(index[i]);
			points.removeElementAt(index[i]);
		}

	}

	public void requestFocus() {
		jButton2.requestFocus();
	}

	public void requestAction() {
		if (add == true)
			if (jButton2.hasFocus()) {
				jButton2_actionPerformed(null);
			} else if (jButton3.hasFocus())
				jButton3_actionPerformed(null);
			else if (jComboBox1.hasFocus())
				jButton2_actionPerformed(null);
			else
				super.requestAction();
	}

	public _Object duplicate(Canvas3D out, boolean add) {
		_Spline o = null;
		try {
			o = (_Spline) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		o.points = new Vector(points);
		for (int i = 0; i < points.size(); i++) {
			o.points.set(i,
					(_Point) ((_Point) points.elementAt(i)).duplicate(out, add));
		}

		if (add)
			out.add3D(o);
		return o;

	}

	public void transform3D(Matrix3D t) {
		if (!selected || processed)
			return;
		super.transform3D(t);
		for (int i = 0; i < points.size(); i++) {
			((_Point) points.elementAt(i)).setSelected(true);
			((_Point) points.elementAt(i)).transform3D(t);
			((_Point) points.elementAt(i)).setSelected(false);
			((_Point) points.elementAt(i)).setProcessed(true);
		}
		reset(true);
	}

	public void deselectRequiredObjects() {
		if (!selected)
			for (int i = 0; i < points.size(); i++)
				((_Point) points.elementAt(i)).setSelected(false);
	}

	public void replaceObjectWith(_Object o, _Object replacement) {
		if (!(o instanceof _Point) || !(replacement instanceof _Point))
			return;

		for (int i = 0; i < points.size(); i++)
			if (points.elementAt(i) == o)
				points.setElementAt(replacement, i);

	}

}

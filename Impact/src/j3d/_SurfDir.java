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
 * Creates a nurb surface defined by a profile and a spine
 * 
 * @author Jonas Forssell, Yuriy Mikhaylovskiy
 */
public class _SurfDir extends _NurbSurface implements Serializable {
	private _NurbCurve profile, spine;
	private boolean spine_reversed = false;

	JTextField jTextField1, jTextField2, jTextField3, jTextField4, jTextField5,
			jTextField6, jTextField7;
	JButton jButton2, jButton3, jButton4, jButton5;
	JComboBox jComboBox1, jComboBox2;
	JCheckBox jCheckBox1;

	private static Regex r_index = new Regex("^ *([0-9]+) *");
	private static Regex r_profile = new Regex(
			"(?i)PROFILE *= *\\[ *([0-9, ]+) *\\]");
	private static Regex r_spine = new Regex(
			"(?i)SPINE *= *\\[ *([0-9, ]+) *\\]");
	private static Regex r_spinerev = new Regex(
			"(?i)SPINE_REVERSED *= *(TRUE|FALSE)");

	/**
	 * Constructs a NURB surface defined by a profile and a spine
	 * 
	 * 
	 */
	public _SurfDir(boolean add, Canvas3D J3D) {
		super(add, J3D);
	}

	public String writeObject() {
		String st = new String(Id);
		st += "  PROFILE = " + profile.get_Id();
		st += "  SPINE = " + spine.get_Id();
		st += "  SPINE_REVERSED = " + (spine_reversed ? "TRUE" : "FALSE");
		st += super.writeObject();
		return st;
	}

	public void readObject(String st, Hashtable geo) {
		if (r_index.search(st))
			Id = r_index.stringMatched(1);
		if (r_profile.search(st))
			profile = (_NurbCurve) geo.get(r_profile.stringMatched(1));
		if (r_spine.search(st))
			spine = (_NurbCurve) geo.get(r_spine.stringMatched(1));
		if (r_spinerev.search(st))
			spine_reversed = (r_spinerev.stringMatched(1).toUpperCase()
					.equals("TRUE") ? true : false);
		super.readObject(st);
	}

	public void reset(boolean do_mesh) {
		Vector3D x, y, z, B0, B, T;
		Matrix3D A, A_inv, tmp;
		_CtrlPoint p;
		float u, du;
		_CtrlPoint[] cp, cpu, cpv;

		_NurbCurve tspine = spine.copy();

		if (spine_reversed)
			tspine.reverseDirection();

		// Initialize
		cp = tspine.controlPoints;
		cpu = new _CtrlPoint[cp.length];
		for (int i = 0; i < cp.length; i++) {
			cpu[i] = new _CtrlPoint(cp[i]);
			cpu[i].stripWeight();
		}

		cp = profile.controlPoints;
		cpv = new _CtrlPoint[cp.length];
		for (int i = 0; i < cp.length; i++) {
			cpv[i] = new _CtrlPoint(cp[i]);
			cpv[i].stripWeight();
		}

		controlPoints = new _CtrlPoint[cpu.length][cpv.length];

		y = new Vector3D();
		z = new Vector3D();
		B = new Vector3D();
		B0 = new Vector3D();
		A_inv = new Matrix3D();

		// Generate the knots
		uorder = tspine.order;
		uknots = tspine.knots;

		vorder = profile.order;
		vknots = profile.knots;

		// Generate control points
		du = 1f / (float) (cpu.length - 1);
		u = 0;

		// Move section to origo (center around spine end)
		for (int i = cpv.length - 1; i >= 0; i--) {
			cpv[i].x = cpv[i].x - cpu[0].x;
			cpv[i].y = cpv[i].y - cpu[0].y;
			cpv[i].z = cpv[i].z - cpu[0].z;
		}

		// Choose initial B0 vector (arbitraty orthogonal to derivate will do)
		x = tspine.calculateDerivate(u, 1)[1].getVector();
		B = new Vector3D(1, 0, 0);
		B0.cross(x, B);

		if (B0.getLength() < 1E-5) {
			B = new Vector3D(0, 1, 0);
			B0.cross(x, B);
		}

		B0.toUnitLength();

		for (int i = 0; i < cpu.length; i++) {

			// Determine A
			T = tspine.calculateDerivate(u, 1)[1].getVector();
			T.toUnitLength();

			// bi = B[i-1] - (B[i-1] dot T[i])*T[i]
			T.scale(B0.dot(B0, T));
			B.sub(B0, T);

			B.toUnitLength();

			x = tspine.calculateDerivate(u, 1)[1].getVector();
			x.toUnitLength();

			z.copy(B);

			y.cross(z, x);

			tmp = new Matrix3D(x, y, z);

			if (i == 0) {
				A_inv = new Matrix3D(tmp);
				A_inv.invert();
			}

			A = new Matrix3D(A_inv);

			A.mult(tmp);

			for (int j = 0; j < cpv.length; j++) {
				// Insert tranformation of section (cpv) here for each new spine
				// section
				p = new _CtrlPoint(cpv[j]);
				p.setSelected(true);
				p.transform3D(A); // Rotate according to derivate angle change
				controlPoints[i][j] = new _CtrlPoint(cpu[i].x + p.x / cpu[i].w,
						cpu[i].y + p.y / cpu[i].w, cpu[i].z + p.z / cpu[i].w,
						cpu[i].w * p.w, Color.GRAY);
				controlPoints[i][j].applyWeight();
			}

			u += du;
			B0.copy(B);
		}

		// Set mesh data
		mesh_div[0] = tspine.mesh_div;
		mesh_div[1] = profile.mesh_div;
		mesh_div[2] = mesh_div[0];
		mesh_div[3] = mesh_div[1];

		// Generate geometry & mesh
		super.reset(do_mesh);
	}

	public String toString() {
		return "DirSurface ID=" + Id;
	}

	public MutableTreeNode get_TreeNode() {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(this);

		// Add unique data for this class
		node.add(profile.get_TreeNode());
		node.add(spine.get_TreeNode());

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
		JPanel jPanel7 = new JPanel();
		jButton1 = new JButton(add == true ? "Add" : "Update");
		jButton2 = new JButton("<<");
		jButton3 = new JButton("<<");
		jTextField1 = new JTextField(
				(profile != null ? profile.toString() : "") + "", 3);
		jTextField2 = new JTextField((spine != null ? spine.toString() : "")
				+ "", 3);

		jCheckBox1 = new JCheckBox("Spine reverse direction", spine_reversed);

		jPanel0.setLayout(new BoxLayout(jPanel0, BoxLayout.Y_AXIS));
		jPanel0.add(jPanel1);
		jPanel1.setLayout(new GridLayout(2, 3));

		jPanel1.add(new JLabel("Profile"), null);
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
		jPanel1.add(new JLabel("Spine"), null);
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

		jPanel0.add(jPanel2);
		jPanel2.add(jCheckBox1);

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
		_SurfDir tmp = this;

		if (profile == null || spine == null)
			return;

		spine_reversed = jCheckBox1.isSelected();

		tmp.update(e);

		if (add == true) {
			try {
				tmp = (_SurfDir) this.clone();
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
		boolean set = false;
		Object[] obj = J3D.getSelectedObjects3D();
		for (int i = 0; i < obj.length; i++)
			if (obj[i] != null && obj[i] instanceof _NurbCurve) {
				profile = (_NurbCurve) obj[i];
				jTextField1.setText(obj[i].toString());
				set = true;
				jButton3.requestFocus();
			}

		if (!set) {
			profile = null;
			jTextField1.setText("");
		}
	}

	void jButton3_actionPerformed(ActionEvent e) {
		boolean set = false;
		Object[] obj = J3D.getSelectedObjects3D();
		for (int i = 0; i < obj.length; i++)
			if (obj[i] != null && obj[i] instanceof _NurbCurve) {
				spine = (_NurbCurve) obj[i];
				jTextField2.setText(obj[i].toString());
				set = true;
				cComboBox1.requestFocus();
			}

		if (!set) {
			spine = null;
			jTextField2.setText("");
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
	}

	public _Object duplicate(Canvas3D out, boolean add) {
		_SurfDir o = null;
		try {
			o = (_SurfDir) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		o.spine = (_NurbCurve) spine.duplicate(out, add);
		o.profile = (_NurbCurve) profile.duplicate(out, add);

		if (add)
			out.add3D(o);

		return o;
	}

	public void transform3D(Matrix3D t) {
		if (!selected || processed)
			return;
		super.transform3D(t);
		spine.setSelected(true);
		spine.transform3D(t);
		spine.setSelected(false);
		spine.setProcessed(true);
		profile.setSelected(true);
		profile.transform3D(t);
		profile.setSelected(false);
		profile.setProcessed(true);
		reset(true);
	}

	public void deselectRequiredObjects() {
		if (!selected) {
			spine.setSelected(false);
			profile.setSelected(false);
			spine.deselectRequiredObjects();
			profile.deselectRequiredObjects();
		}
	}

	public void replaceObjectWith(_Object o, _Object replacement) {
		if (!(o instanceof _NurbCurve) || !(replacement instanceof _NurbCurve))
			return;

		if (spine == o)
			spine = (_NurbCurve) replacement;
		if (profile == o)
			profile = (_NurbCurve) replacement;

	}

}

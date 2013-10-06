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

import com.stevesoft.pat.Regex;

import java.awt.event.*;

import gui.*;
import java.util.*;

/**
 * Creates a nurbsurface by interpolating between two edge curves
 * 
 * @author Jonas Forssell, Yuriy Mikhaylovskiy
 */
public class _SurfRul extends _NurbSurface implements Serializable {
	private _NurbCurve[] profile;
	private boolean curve2_reversed = false;

	JTextField jTextField1, jTextField2, jTextField3, jTextField4, jTextField5,
			jTextField6, jTextField7;
	JButton jButton2, jButton3, jButton4, jButton5;
	JComboBox jComboBox1, jComboBox2;
	JCheckBox jCheckBox1;

	private static Regex r_index = new Regex("^ *([0-9]+) *");
	private static Regex r_profile = new Regex(
			"(?i)PROFILES *= *\\[ *([0-9, ]+) *\\]");
	private static Regex r_indexer = new Regex("\\G,? *([0-9]+) *,?");
	private static Regex r_rev = new Regex(
			"(?i)PROFILE_REVERSED *= *(TRUE|FALSE)");

	/**
	 * Constructs a NURB surface defined by a profile and a profile2
	 * 
	 * 
	 */
	public _SurfRul(boolean add, Canvas3D J3D) {
		super(add, J3D);
		profile = new _NurbCurve[2];
	}

	public _SurfRul(_NurbCurve c1, _NurbCurve c2, int msh_type,
			float thickness, Material material, Canvas3D j3d) {
		super(false, j3d);
		this.msh_type = msh_type;
		this.thickness = thickness;
		this.material = material;
		profile = new _NurbCurve[2];
		profile[0] = c1;
		profile[1] = c2;
		curve2_reversed = false;
		J3D = j3d;
		reset(false);
	}

	public String writeObject() {
		String st = new String(Id);
		st += "  PROFILES = [";
		for (int i = 0; i < profile.length; i++) {
			st += ((_NurbCurve) profile[i]).get_Id();
			if (i < profile.length - 1)
				st += ", ";
		}
		st += "] ";
		st += " PROFILE_REVERSED = " + (curve2_reversed ? "TRUE" : "FALSE");
		st += super.writeObject();
		return st;
	}

	public void readObject(String st, Hashtable geo) {
		int i = 0;
		if (r_index.search(st))
			Id = r_index.stringMatched(1);
		if (r_profile.search(st))
			while (r_indexer.search(r_profile.stringMatched(1)))
				profile[i++] = (_NurbCurve) geo.get(r_indexer.stringMatched(1));
		if (r_rev.search(st))
			curve2_reversed = (r_rev.stringMatched(1).toUpperCase()
					.equals("TRUE") ? true : false);
		super.readObject(st);
	}

	public void reset(boolean do_mesh) {
		int cpl, i, j, max, e0, e1;
		_CtrlPoint[] cp;
		_NurbCurve tprofile[] = new _NurbCurve[profile.length];
		Vector elev0 = new Vector();
		Vector elev1 = new Vector();
		float knt0, knt1;

		// Initialize

		tprofile = new _NurbCurve[profile.length];
		for (i = 0; i < profile.length; i++)
			tprofile[i] = (_NurbCurve) profile[i].copy();

		if (curve2_reversed)
			tprofile[profile.length - 1].reverseDirection();

		// Degree elevate both curve to a common degree

		max = Math.max(tprofile[0].order, tprofile[1].order);
		e0 = max - tprofile[0].order;
		e1 = max - tprofile[1].order;

		if (e0 > 0)
			tprofile[0].elevate(e0);
		if (e1 > 0)
			tprofile[1].elevate(e1);

		// Create common knot vector

		j = 0;
		i = 0;
		while (i < tprofile[0].knots.length && j < tprofile[1].knots.length) {
			knt0 = tprofile[0].knots[i];
			knt1 = tprofile[1].knots[j];

			if (knt0 < knt1) {
				elev1.add(new Float(knt0));
				i++;
			} else if (knt0 == knt1) {
				i++;
				j++;
			} else {
				j++;
			}
		}

		j = 0;
		i = 0;
		while (i < tprofile[0].knots.length && j < tprofile[1].knots.length) {
			knt0 = tprofile[0].knots[i];
			knt1 = tprofile[1].knots[j];

			if (knt1 < knt0) {
				elev0.add(new Float(knt1));
				j++;
			} else if (knt0 == knt1) {
				i++;
				j++;
			} else {
				i++;
			}
		}

		if (elev0.size() != 0)
			tprofile[0].insertKnots(elev0);
		if (elev1.size() != 0)
			tprofile[1].insertKnots(elev1);

		// Generate control points
		cpl = tprofile[0].controlPoints.length;
		controlPoints = new _CtrlPoint[cpl][tprofile.length];
		for (i = 0; i < cpl; i++)
			controlPoints[i] = new _CtrlPoint[tprofile.length];

		for (i = 0; i < tprofile.length; i++) {
			cp = tprofile[i].controlPoints;
			for (j = 0; j < cpl; j++)
				controlPoints[j][i] = new _CtrlPoint(cp[j].x, cp[j].y, cp[j].z,
						cp[j].w, Color.GRAY);
		}

		// Generate the knots
		uorder = tprofile[0].order;
		uknots = tprofile[0].knots;

		vorder = 1; // Linear
		vknots = new float[4];
		vknots[0] = 0.0f;
		vknots[1] = 0.0f;
		vknots[2] = 1.0f;
		vknots[3] = 1.0f;

		// Set mesh data
		mesh_div[0] = tprofile[0].mesh_div;
		mesh_div[1] = -1; // Let mesh size decide
		mesh_div[2] = tprofile[1].mesh_div;
		mesh_div[3] = -1;

		// Generate geometry & mesh
		super.reset(do_mesh);
	}

	public String toString() {
		return "RulSurface ID=" + Id;
	}

	public MutableTreeNode get_TreeNode() {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(this);

		// Add unique data for this class
		for (int i = 0; i < profile.length; i++)
			node.add(profile[i].get_TreeNode());

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
		JPanel jPanel6 = new JPanel();
		jButton1 = new JButton(add == true ? "Add" : "Update");
		jButton2 = new JButton("<<");
		jButton3 = new JButton("<<");
		jTextField1 = new JTextField(
				(profile[0] != null ? profile[0].toString() : "") + "", 3);
		jTextField2 = new JTextField(
				(profile[1] != null ? profile[1].toString() : "") + "", 3);
		jTextField3 = new JTextField("" + mesh_size);

		jCheckBox1 = new JCheckBox("Curve2 reverse direction", curve2_reversed);

		jPanel0.setLayout(new BoxLayout(jPanel0, BoxLayout.Y_AXIS));
		jPanel0.add(jPanel1);
		jPanel1.setLayout(new GridLayout(2, 3));

		jPanel1.add(new JLabel("Curve1"), null);
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
		jPanel1.add(new JLabel("Curve2"), null);
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

		jPanel0.add(jPanel6);
		jPanel6.setLayout(new GridLayout());
		jPanel6.add(new JLabel("Mesh size"), null);
		jPanel6.add(jTextField3, null);
		jTextField3.addKeyListener(new KeyAdapter() {
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

		jPanel0.add(super.getEditPanel(j3d, pp));

		jPanel0.add(jButton1);
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
		_SurfRul tmp = this;

		if (profile[0] == null || profile[1] == null)
			return;

		mesh_size = Float.parseFloat(jTextField3.getText());

		curve2_reversed = jCheckBox1.isSelected();

		tmp.update(e);

		if (add == true) {
			try {
				tmp = (_SurfRul) this.clone();
				tmp.prepareGroups();
				J3D.add3D(tmp);
			} catch (CloneNotSupportedException e1) {
				e1.printStackTrace();
			}
			tmp.add = false;
			jButton2.requestFocus();

			profile = new _NurbCurve[2];

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
				profile[0] = (_NurbCurve) obj[i];
				jTextField1.setText(obj[i].toString());
				set = true;
				jButton3.requestFocus();
			}

		if (!set) {
			profile[0] = null;
			jTextField1.setText("");
		}
	}

	void jButton3_actionPerformed(ActionEvent e) {
		boolean set = false;
		Object[] obj = J3D.getSelectedObjects3D();
		for (int i = 0; i < obj.length; i++)
			if (obj[i] != null && obj[i] instanceof _NurbCurve) {
				profile[1] = (_NurbCurve) obj[i];
				jTextField2.setText(obj[i].toString());
				set = true;
				jTextField3.requestFocus();
				jTextField3.selectAll();
			}

		if (!set) {
			profile[1] = null;
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
		_SurfRul o = null;

		try {
			o = (_SurfRul) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		o.profile = new _NurbCurve[2];
		o.profile[0] = (_NurbCurve) profile[0].duplicate(out, add);
		o.profile[1] = (_NurbCurve) profile[1].duplicate(out, add);

		if (add)
			out.add3D(o);

		return o;
	}

	public void transform3D(Matrix3D t) {
		if (!selected || processed)
			return;
		super.transform3D(t);
		profile[0].setSelected(true);
		profile[0].transform3D(t);
		profile[0].setSelected(true);
		profile[0].setProcessed(true);
		profile[1].setSelected(true);
		profile[1].transform3D(t);
		profile[1].setSelected(true);
		profile[1].setProcessed(true);
		reset(true);
	}

	public void deselectRequiredObjects() {
		if (!selected) {
			for (int i = 0; i < profile.length; i++) {
				profile[i].setSelected(false);
				profile[i].deselectRequiredObjects();
			}
		}
	}

	public void replaceObjectWith(_Object o, _Object replacement) {
		if (!(o instanceof _NurbCurve) || !(replacement instanceof _NurbCurve))
			return;

		for (int i = 0; i < profile.length; i++)
			if (profile[i] == o)
				profile[i] = (_NurbCurve) replacement;
	}

}

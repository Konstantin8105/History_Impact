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

import com.stevesoft.pat.Regex;

import java.awt.event.*;

import gui.*;

/**
 * Creates a nurbsurface by rotation of a curve
 * 
 * @author Jonas Forssell, Yuriy Mikhaylovskiy
 */
public class _SurfRev extends _NurbSurface implements Serializable {
	private _NurbCurve profile;
	private _Point s_axis, e_axis;
	private float angle;

	JTextField jTextField1, jTextField2, jTextField3, jTextField4, jTextField5,
			jTextField6, jTextField7;
	JButton jButton2, jButton3, jButton4, jButton5;
	JComboBox jComboBox1, jComboBox2;

	private static Regex r_index = new Regex("^ *([0-9]+) *");
	private static Regex r_profile = new Regex("(?i)PROFILE *= *([0-9]+) *");
	private static Regex r_start = new Regex(
			"(?i)AXIS_STARTPOINT *= *([0-9]+) *");
	private static Regex r_end = new Regex("(?i)AXIS_ENDPOINT *= *([0-9]+) *");
	private static Regex r_angle = new Regex(
			"(?i)ANGLE *= *([-+]?[0-9]+[.]?[0-9]*(?:[eE][-+]?[0-9]+)?)");
	private static Regex r_meshsize = new Regex(
			"(?i)MESHSIZE *= *([-+]?[0-9]+[.]?[0-9]*(?:[eE][-+]?[0-9]+)?)");

	public _SurfRev(boolean add, Canvas3D J3D) {
		super(add, J3D);
	}

	public void readObject(String st, Hashtable geo) {
		if (r_index.search(st))
			Id = r_index.stringMatched(1);
		if (r_profile.search(st))
			profile = (_NurbCurve) geo.get(r_profile.stringMatched(1));
		if (r_start.search(st))
			s_axis = (_Point) geo.get(r_start.stringMatched(1));
		if (r_end.search(st))
			e_axis = (_Point) geo.get(r_end.stringMatched(1));
		if (r_angle.search(st))
			angle = Float.parseFloat(r_angle.stringMatched(1));
		if (r_meshsize.search(st))
			mesh_size = Float.parseFloat(r_meshsize.stringMatched(1));
		super.readObject(st);
	}

	public String writeObject() {
		String st = new String(Id);
		st += "  PROFILE = " + profile.get_Id();
		st += "  AXIS_STARTPOINT = " + s_axis.get_Id();
		st += "  AXIS_ENDPOINT = " + e_axis.get_Id();
		st += "  ANGLE = " + angle;
		st += "  MESHSIZE = " + mesh_size;
		st += super.writeObject();
		return st;
	}

	public void reset(boolean do_mesh) {
		int narcs, k, j, i, m, n, index;
		float dang, wm, ang, radius, to_rad;
		float[] cosines, sines;
		float[][] wij;
		Vector3D O, S, T, X, Y, Pj, P0, T0, P2, T2, Temp1;
		Vector3D[][] Pij;
		_CtrlPoint tmp;

		X = new Vector3D();
		Y = new Vector3D();
		P0 = new Vector3D();
		T0 = new Vector3D();
		P2 = new Vector3D();
		T2 = new Vector3D();
		Temp1 = new Vector3D();

		// Initialize
		to_rad = (float) Math.PI / 180f;
		_CtrlPoint[] cpv = profile.controlPoints;
		S = s_axis.getVector();
		T = e_axis.getVector();
		T.sub(T, S);
		T.toUnitLength();

		if (angle <= 90)
			narcs = 1;
		else if (angle <= 180)
			narcs = 2;
		else if (angle <= 270)
			narcs = 3;
		else
			narcs = 4;

		dang = angle / narcs;
		j = 3 + 2 * (narcs - 1);
		n = 2 * narcs + 1;
		m = cpv.length;
		wm = (float) Math.cos(dang * to_rad / 2.0f);

		controlPoints = new _CtrlPoint[n][m];
		Pij = new Vector3D[n][m];
		for (i = 0; i < n; i++) {
			Pij[i] = new Vector3D[m];
			for (k = 0; k < m; k++)
				Pij[i][k] = new Vector3D();
		}

		wij = new float[n][m];
		cosines = new float[narcs + 1];
		sines = new float[narcs + 1];

		ang = 0.0f;
		for (i = 1; i <= narcs; i++) {
			ang += dang;
			cosines[i] = (float) Math.cos(ang * to_rad);
			sines[i] = (float) Math.sin(ang * to_rad);
		}

		// Generate the knots
		vorder = profile.order;
		vknots = profile.knots;

		uorder = 2;
		uknots = new float[j + 3];

		for (i = 0; i < 3; i++) {
			uknots[i] = 0.0f;
			uknots[j++] = 1.0f;
		}

		switch (narcs) {
		case 2:
			uknots[3] = 0.5f;
			uknots[4] = 0.5f;
			break;
		case 3:
			uknots[3] = 1.0f / 3.0f;
			uknots[4] = 1.0f / 3.0f;
			uknots[5] = 2.0f / 3.0f;
			uknots[6] = 2.0f / 3.0f;
			break;
		case 4:
			uknots[3] = 0.25f;
			uknots[4] = 0.25f;
			uknots[5] = 0.5f;
			uknots[6] = 0.5f;
			uknots[7] = 0.75f;
			uknots[8] = 0.75f;
			break;
		}

		// Generate control points
		for (j = 0; j < m; j++) {
			tmp = cpv[j];
			tmp.stripWeight();
			Pj = tmp.getVector();
			O = lib3D.pointToLine(S, T, Pj);
			X.sub(Pj, O);
			radius = X.getLength();
			if (radius == 0) {
				X = new Vector3D(1, 0, 0);
				Y = new Vector3D(0, 1, 0);
			} else {
				X.toUnitLength();
				Y.cross(T, X);
			}
			Pij[0][j].copy(Pj);
			P0.copy(Pj);
			wij[0][j] = cpv[j].w;
			T0.copy(Y);
			index = 0;
			ang = 0.0f;

			for (i = 1; i <= narcs; i++) {
				Temp1.copy(Y);
				Temp1.scale(radius * sines[i]);
				P2.copy(X);
				P2.scale(radius * cosines[i]);
				P2.add(Temp1, P2);
				P2.add(O, P2);

				Pij[index + 2][j].copy(P2);
				wij[index + 2][j] = cpv[j].w;

				Temp1.copy(Y);
				Temp1.scale(cosines[i]);
				T2.copy(X);
				T2.scale(-sines[i]);
				T2.add(Temp1, T2);

				Pij[index + 1][j] = lib3D.intersection(P0, T0, P2, T2);
				wij[index + 1][j] = wm * cpv[j].w;

				index += 2;
				if (i < narcs) {
					P0.copy(P2);
					T0.copy(T2);
				}
			}
		}

		for (j = 0; j < m; j++)
			for (i = 0; i < n; i++) {
				controlPoints[i][j] = new _CtrlPoint(Pij[i][j].x, Pij[i][j].y,
						Pij[i][j].z, wij[i][j], Color.GRAY);
				controlPoints[i][j].applyWeight();
			}

		// Set mesh data
		mesh_div[0] = -1; // Let mesh size decide
		mesh_div[1] = profile.mesh_div;
		mesh_div[2] = -1;
		mesh_div[3] = profile.mesh_div;
		;

		// Generate geometry & mesh
		super.reset(do_mesh);
	}

	public String toString() {
		return "RevSurface ID=" + Id;
	}

	public MutableTreeNode get_TreeNode() {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(this);

		// Add unique data for this class
		node.add(profile.get_TreeNode());
		node.add(s_axis.get_TreeNode());
		node.add(e_axis.get_TreeNode());

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
		JPanel jPanel5 = new JPanel();
		JPanel jPanel6 = new JPanel();
		JPanel jPanel7 = new JPanel();
		jButton1 = new JButton(add == true ? "Add" : "Update");
		jButton2 = new JButton("<<");
		jButton3 = new JButton("<<");
		jButton4 = new JButton("<<");
		jTextField1 = new JTextField(
				(profile != null ? profile.toString() : "") + "", 3);
		jTextField2 = new JTextField((s_axis != null ? s_axis.toString() : "")
				+ "", 3);
		jTextField3 = new JTextField((e_axis != null ? e_axis.toString() : "")
				+ "", 3);
		jTextField4 = new JTextField("" + angle);
		jTextField5 = new JTextField("" + mesh_size);

		jPanel0.setLayout(new BoxLayout(jPanel0, BoxLayout.Y_AXIS));
		jPanel0.add(jPanel1);
		jPanel1.setLayout(new GridLayout(3, 3));

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
		jPanel1.add(new JLabel("Strt pnt axs"), null);
		jPanel1.add(jTextField2, null);
		jTextField2.setEditable(false);
		jPanel1.add(jButton3, null);
		jButton3.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton3_actionPerformed(e);
			}
		});
		jButton2.addKeyListener(new KeyAdapter() {
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
		jPanel1.add(new JLabel("End pnt axs"), null);
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
					jTextField5.requestFocus();
					jTextField5.selectAll();
				}
			}
		});
		jPanel0.add(jPanel5);
		jPanel5.setLayout(new GridLayout());
		jPanel5.add(new JLabel("Mesh size"), null);
		jPanel5.add(jTextField5, null);
		jTextField5.addKeyListener(new KeyAdapter() {
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
		_SurfRev tmp = this;

		if (profile == null || s_axis == null || e_axis == null)
			return;
		angle = Float.parseFloat(jTextField4.getText());
		mesh_size = Float.parseFloat(jTextField5.getText());

		tmp.update(e);

		if (add == true) {
			try {
				tmp = (_SurfRev) this.clone();
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
			if (obj[i] != null && obj[i] instanceof _Point) {
				s_axis = (_Point) obj[i];
				jTextField2.setText(obj[i].toString());
				set = true;
				jButton4.requestFocus();
			}

		if (!set) {
			s_axis = null;
			jTextField2.setText("");
		}
	}

	void jButton4_actionPerformed(ActionEvent e) {
		boolean set = false;
		Object[] obj = J3D.getSelectedObjects3D();
		for (int i = 0; i < obj.length; i++)
			if (obj[i] != null && obj[i] instanceof _Point) {
				e_axis = (_Point) obj[i];
				jTextField3.setText(obj[i].toString());
				set = true;
				jTextField4.requestFocus();
				jTextField4.selectAll();
			}

		if (!set) {
			e_axis = null;
			jTextField3.setText("");
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
	}

	public _Object duplicate(Canvas3D out, boolean add) {
		_SurfRev o = null;
		try {
			o = (_SurfRev) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		o.profile = (_NurbCurve) profile.duplicate(out, add);
		o.s_axis = (_Point) s_axis.duplicate(out, add);
		o.e_axis = (_Point) e_axis.duplicate(out, add);

		if (add)
			out.add3D(o);

		return o;
	}

	public void transform3D(Matrix3D t) {
		if (!selected || processed)
			return;
		super.transform3D(t);
		s_axis.setSelected(true);
		s_axis.transform3D(t);
		s_axis.setSelected(false);
		s_axis.setProcessed(true);
		e_axis.setSelected(true);
		e_axis.transform3D(t);
		e_axis.setSelected(false);
		e_axis.setProcessed(true);
		profile.setSelected(true);
		profile.transform3D(t);
		profile.setSelected(false);
		profile.setProcessed(true);
		reset(true);
	}

	public void deselectRequiredObjects() {
		if (!selected) {
			s_axis.setSelected(false);
			e_axis.setSelected(false);
			profile.setSelected(false);
			profile.deselectRequiredObjects();
		}
	}

	public void replaceObjectWith(_Object o, _Object replacement) {
		if (o instanceof _NurbCurve && replacement instanceof _NurbCurve)
			if (profile == o)
				profile = (_NurbCurve) replacement;

		if (o instanceof _Point && replacement instanceof _Point) {
			if (s_axis == o)
				s_axis = (_Point) replacement;
			if (e_axis == o)
				e_axis = (_Point) replacement;
		}
	}

}

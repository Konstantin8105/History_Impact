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
 * Creates a nurb surface based on four corner points
 * 
 * @author Jonas Forssell, Yuriy Mikhaylovskiy
 */
public class _SurfBil extends _NurbSurface implements Serializable {
	private _Point[] p_corner = new _Point[4];

	JTextField jTextField1, jTextField2, jTextField3, jTextField4, jTextField5,
			jTextField6;
	JButton jButton2, jButton3, jButton4, jButton5;
	JComboBox jComboBox1, jComboBox2;

	private static Regex r_index = new Regex("^ *([0-9]+) *");
	private static Regex r_points = new Regex(
			"(?i)POINTS *= *\\[ *([0-9, ]+) *\\]");
	private static Regex r_indexer = new Regex("\\G,? *([0-9]+) *,?");

	/**
	 * Constructs a NURB surface defined by 4 points to form a bilinear surface.
	 * Inputs:
	 * 
	 * The position of the corner points
	 * 
	 * ^ v direction | ---------------- |p[2] p[3]| | | | SRF | | | |p[0] p[1]|
	 * -------------------> u direction'''
	 * 
	 * @param p_corner
	 *            Corner points of the surface
	 * @param udiv
	 *            Number of line segments used to draw the arc in u-direction
	 * @param udiv
	 *            Number of line segments used to draw the arc in v-direction
	 * @param color
	 *            Color of the arc
	 * @param element_type
	 *            element type to be used for the FE-mesh
	 * @param element_material
	 *            element material to be used for the FE-mesh
	 * @param element_thickness
	 *            thickness of the element in the mesh
	 */
	public _SurfBil(boolean add, Canvas3D J3D) {
		super(add, J3D);
	}

	public _SurfBil(_Point[] p, int msh_type, float thickness,
			Material material, Canvas3D j3d) {
		super(false, j3d);
		this.msh_type = msh_type;
		this.thickness = thickness;
		this.material = material;
		p_corner = p;
		reset(false);
	}

	public String writeObject() {
		String st = new String(Id);
		st += "  POINTS = [";
		for (int i = 0; i < p_corner.length; i++) {
			st += ((_Point) p_corner[i]).get_Id();
			if (i < p_corner.length - 1)
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
		if (r_points.search(st))
			while (r_indexer.search(r_points.stringMatched(1)))
				p_corner[i++] = (_Point) geo.get(r_indexer.stringMatched(1));
		super.readObject(st);
	}

	public void reset(boolean do_mesh) {

		// Generate control points
		controlPoints = new _CtrlPoint[2][2];

		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 2; j++)
				controlPoints[i][j] = new _CtrlPoint(p_corner[i + 2 * j].x,
						p_corner[i + 2 * j].y, p_corner[i + 2 * j].z, 1.0f,
						Color.GRAY);

		// Generate the knots
		uknots = new float[4];
		vknots = new float[4];
		uorder = 1;
		vorder = 1;

		uknots[0] = 0.0f;
		uknots[1] = 0.0f;
		uknots[2] = 1.0f;
		uknots[3] = 1.0f;

		vknots[0] = 0.0f;
		vknots[1] = 0.0f;
		vknots[2] = 1.0f;
		vknots[3] = 1.0f;

		// No defined division of the surface edges
		for (int i = 0; i < 4; i++)
			mesh_div[i] = -1;

		// Generate geometry & mesh
		super.reset(do_mesh);

	}

	public String toString() {
		return "BLSurface ID=" + Id;
	}

	public MutableTreeNode get_TreeNode() {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(this);

		// Add unique data for this class
		for (int i = 0; i < p_corner.length; i++)
			node.add(p_corner[i].get_TreeNode());

		// Add mesh data from _NurbCurve
		node.add(super.get_TreeNode());

		return node;
	}

	public JPanel getEditPanel(Canvas3D j3d, PreProcessor pp) {
		this.J3D = j3d;
		this.PreP = pp;

		JPanel jPanel0 = new JPanel();
		JPanel jPanel1 = new JPanel();
		JPanel jPanel5 = new JPanel();
		JPanel jPanel7 = new JPanel();
		jButton1 = new JButton(add == true ? "Add" : "Update");
		jButton2 = new JButton("<<");
		jButton3 = new JButton("<<");
		jButton4 = new JButton("<<");
		jButton5 = new JButton("<<");
		jTextField1 = new JTextField(
				(p_corner[0] != null ? p_corner[0].toString() : "") + "", 3);
		jTextField2 = new JTextField(
				(p_corner[1] != null ? p_corner[1].toString() : "") + "", 3);
		jTextField4 = new JTextField(
				(p_corner[2] != null ? p_corner[2].toString() : "") + "", 3);
		jTextField3 = new JTextField(
				(p_corner[3] != null ? p_corner[3].toString() : "") + "", 3);
		jTextField6 = new JTextField("" + mesh_size);

		jPanel0.setLayout(new BoxLayout(jPanel0, BoxLayout.Y_AXIS));
		jPanel0.add(jPanel1);
		jPanel1.setLayout(new GridLayout(4, 3));

		jPanel1.add(new JLabel("LL"), null);
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
		jPanel1.add(new JLabel("LR"), null);
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
		jPanel1.add(new JLabel("UR"), null);
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
		jPanel1.add(new JLabel("UL"), null);
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

		jPanel0.add(jPanel5);
		jPanel5.setLayout(new GridLayout());
		jPanel5.add(new JLabel("Mesh size"), null);
		jPanel5.add(jTextField6, null);
		jTextField6.addKeyListener(new KeyAdapter() {
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

		if (add == true && pp != null)
			pp.headerMessage("new Bilinear surface: Select lower left corner point");

		return jPanel0;
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

	void jButton1_actionPerformed(ActionEvent e) {
		_SurfBil tmp = this;

		if (p_corner[0] == null || p_corner[1] == null || p_corner[2] == null
				|| p_corner[3] == null)
			return;
		mesh_size = Float.parseFloat(jTextField6.getText());

		tmp.update(e);

		if (add == true) {
			try {
				tmp = (_SurfBil) this.clone();
				tmp.prepareGroups();
				J3D.add3D(tmp);
			} catch (CloneNotSupportedException e1) {
				e1.printStackTrace();
			}
			tmp.add = false;
			jButton2.requestFocus();

			p_corner = new _Point[4];
		} else
			tmp.reset(true);

		J3D.tree_reset();
		J3D.view_reset();
		J3D.repaint();
	}

	void jButton2_actionPerformed(ActionEvent e) {
		Object obj = J3D.getSelectedObject3D();
		if (obj != null && obj instanceof _Point) {
			p_corner[0] = (_Point) obj;
			jTextField1.setText(obj.toString());
			jButton3.requestFocus();
			if (add == true && PreP != null)
				PreP.headerMessage("new Bilinear surface: Select lower right corner point");
		}
	}

	void jButton3_actionPerformed(ActionEvent e) {
		Object obj = J3D.getSelectedObject3D();
		if (obj != null && obj instanceof _Point) {
			p_corner[1] = (_Point) obj;
			jTextField2.setText(obj.toString());
			jButton4.requestFocus();
			if (add == true && PreP != null)
				PreP.headerMessage("new Bilinear surface: Select upper right corner point");
		}
	}

	void jButton4_actionPerformed(ActionEvent e) {
		Object obj = J3D.getSelectedObject3D();
		if (obj != null && obj instanceof _Point) {
			p_corner[3] = (_Point) obj;
			jTextField3.setText(obj.toString());
			jButton5.requestFocus();
			if (add == true && PreP != null)
				PreP.headerMessage("new Bilinear surface: Select upper left corner point");
		}
	}

	void jButton5_actionPerformed(ActionEvent e) {
		Object obj = J3D.getSelectedObject3D();
		if (obj != null && obj instanceof _Point) {
			p_corner[2] = (_Point) obj;
			jTextField4.setText(obj.toString());
			jTextField6.requestFocus();
			jTextField6.selectAll();
		}
	}

	public _Object duplicate(Canvas3D out) {
		_SurfBil o = null;
		try {
			o = (_SurfBil) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < 4; i++)
			o.p_corner[i] = (_Point) p_corner[i].duplicate(out, add);

		out.add3D(o);

		return o;
	}

	public void transform3D(Matrix3D t) {
		if (!selected)
			return;
		super.transform3D(t);
		for (int i = 0; i < 4; i++) {
			p_corner[i].setSelected(true);
			p_corner[i].transform3D(t);
			p_corner[i].setSelected(true);
			p_corner[i].setProcessed(true);
		}
		reset(true);
	}

	public void deselectRequiredObjects() {
		if (!selected)
			for (int i = 0; i < p_corner.length; i++)
				p_corner[i].setSelected(false);
	}

	public void replaceObjectWith(_Object o, _Object replacement) {
		if (!(o instanceof _Point) || !(replacement instanceof _Point))
			return;

		for (int i = 0; i < p_corner.length; i++)
			if (p_corner[i] == o)
				p_corner[i] = (_Point) replacement;

	}

}

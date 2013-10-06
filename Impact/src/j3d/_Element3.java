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
import java.awt.geom.*;
import java.util.*;

import javax.swing.tree.*;
import java.io.*;
import gui.*;

import javax.swing.*;
import java.awt.event.*;

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy
 */

public class _Element3 extends _Element implements Serializable {
	public _Node node1, node2, node3, node1_tmp, node2_tmp, node3_tmp;
	public Material material = null;
	public Loads load;
	public float thickness;
	public Integer NIP = new Integer(5), PIP;
	public java.lang.Float factor, friction;
	public String contact;
	public String thinning;
	public Color contur = Color.black;
	public boolean selected = false;
	private String msh_name = "Contact_Triangle";
	public int msh_type = Canvas3D.MESH_Contact_Triangle;
	private JComboBox cb_type, cb_ctype, cb_mat, cb_load, cb_thin;
	private JTextField tfn1, tfn2, tfn3, tft, tffact, tffrict, tfnip, tfpip;
	private Object[] arr;
	private JButton b_1, b_2, b_3, b_upd;
	private double circumcircle_radius;

	public void reset(boolean do_mesh) {
	};

	public void mesh(int type, float size) {
	}

	public boolean delete() {
		return selected;
	}

	public _Element3(boolean add) {
		this.add = add;
		msh_type = Canvas3D.MESH_Shell_C0_3;
		msh_name = "Shell_C0_3";
	}

	public _Element3(_Node n1, _Node n2, _Node n3, int t, Material m, float th) {
		msh_type = t;
		if (t == Canvas3D.MESH_Contact_Triangle)
			msh_name = "Contact_Triangle";
		else
			msh_name = "Shell_C0_3";
		node1 = n1;
		node2 = n2;
		node3 = n3;
		material = m;
		thickness = th;
	}

	public _Element3(_Node n1, _Node n2, _Node n3, int t) {
		this(n1, n2, n3, t, null, 1);
	}

	public _Element3(_Node n1, _Node n2, _Node n3, int t, float th) {
		this(n1, n2, n3, t, null, th);
	}

	public _Element3(_Node n1, _Node n2, _Node n3) {
		this(n1, n2, n3, Canvas3D.MESH_Shell_C0_3, null, 1);
	}

	public String writeObject() {
		String st = new String(Id);

		st += " \tnodes = [" + node1.get_Id() + "," + node2.get_Id() + ","
				+ node3.get_Id() + "] \tT = " + thickness;
		if (factor != null)
			st += " \tFactor = " + factor;
		if (friction != null)
			st += " \tFriction = " + friction;

		if (msh_type == Canvas3D.MESH_Contact_Triangle)
			return st;

		if (material != null)
			st += " \tMaterial = " + material.name;
		if (NIP != null && msh_type != Canvas3D.MESH_Contact_Triangle)
			st += " \tNIP = " + NIP;
		if (PIP != null)
			st += " \tPIP = " + PIP;
		if (load != null)
			st += " \tLoad = " + load.name;
		if (contact != null && contact.length() != 0)
			st += " \tContact = " + contact;
		if (thinning != null && thinning.compareToIgnoreCase("OFF") == 0)
			st += " \tThinning = OFF";

		return st;

	}

	public void readObject(String st_load, Hashtable LoadDB) {
		try {
			NIP = new Integer(getValue(st_load, "NIP"));
		} catch (Exception e1) {
		}
		try {
			PIP = new Integer(getValue(st_load, "PIP"));
		} catch (Exception e1) {
		}
		try {
			load = (Loads) LoadDB.get(getValue(st_load, "LOAD"));
		} catch (Exception e1) {
		}
		try {
			factor = new java.lang.Float(getValue(st_load, "FACTOR"));
		} catch (Exception e1) {
		}
		try {
			contact = getValue(st_load, "CONTACT");
		} catch (Exception e1) {
		}
		try {
			friction = new java.lang.Float(getValue(st_load, "FRICTION"));
		} catch (Exception e1) {
		}
		try {
			thinning = getValue(st_load, "THINNING");
		} catch (Exception e1) {
		}
	}

	public Object[] get_Array(Canvas3D j3d) {
		int k = 0;
		arr = new Object[1
				+ (j3d.getSHOW_ID_ELEMENT() ? 1 : 0)
				+ ((j3d.getSHOW_ID_LOADS()
						&& msh_type != Canvas3D.MESH_Contact_Triangle && load != null) ? 1
						: 0)];
		if (material == null)
			arr[k++] = new shpTriangle(node1.x, node1.y, node1.z, node2.x,
					node2.y, node2.z, node3.x, node3.y, node3.z,
					selected ? Canvas3D.SELECTCOLOR : null, contur);
		else
			arr[k++] = new shpTriangle(node1.x, node1.y, node1.z, node2.x,
					node2.y, node2.z, node3.x, node3.y, node3.z,
					selected ? Canvas3D.SELECTCOLOR : material.color,
					selected ? Canvas3D.SELECTCOLOR : material.color);

		if (j3d.getSHOW_ID_ELEMENT()) {
			arr[k++] = new shpOrientedText(get_Id(),
					(node1.x + node2.x + node3.x) / 3,
					(node1.y + node2.y + node3.y) / 3,
					(node1.z + node2.z + node3.z) / 3, contur);
		}

		if (j3d.getSHOW_ID_LOADS()
				&& msh_type != Canvas3D.MESH_Contact_Triangle && load != null)
			arr[k++] = new shpOrientedText(load.name,
					(node1.x + node2.x + node3.x) / 3,
					(node1.y + node2.y + node3.y) / 3,
					(node1.z + node2.z + node3.z) / 3, load.color);

		for (int i = 0; i < arr.length; i++)
			((shp) arr[i]).setShow(show);

		return arr;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean sel) {
		selected = sel;
	}

	public String toString() {
		return msh_name + " ID=" + Id;
	}

	public MutableTreeNode get_TreeNode() {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(this);
		node.add(node1.get_TreeNode());
		node.add(node2.get_TreeNode());
		node.add(node3.get_TreeNode());
		return node;
	}

	public void transform3D(Matrix3D t) {
		if (!selected || processed)
			return;
		node1.setSelected(true);
		node2.setSelected(true);
		node3.setSelected(true);
		node1.transform3D(t);
		node2.transform3D(t);
		node3.transform3D(t);
		node1.setSelected(true);
		node2.setSelected(true);
		node3.setSelected(true);
		node1.setProcessed(true);
		node2.setProcessed(true);
		node3.setProcessed(true);
	}

	public void error(Object st) {
		JOptionPane.showMessageDialog(null, "Error: " + st, "Error!",
				JOptionPane.ERROR_MESSAGE);
		System.out.println("Error: " + st);
		if (st instanceof Exception)
			((Exception) st).printStackTrace();
	}

	public JPanel getEditPanel(Canvas3D j3d, PreProcessor pp) {
		this.J3D = j3d;
		this.PreP = pp;
		JPanel p = new JPanel(new BorderLayout());
		JPanel p1 = new JPanel(new GridLayout(3, 3));
		JPanel p3 = new JPanel();
		JPanel p2 = new JPanel(new GridLayout(9, 1));
		JPanel p4 = new JPanel(new BorderLayout());
		JPanel p5 = new JPanel(new BorderLayout());
		JPanel p6 = new JPanel(new BorderLayout());
		JPanel p7 = new JPanel(new BorderLayout());
		JPanel p8 = new JPanel(new BorderLayout());
		JPanel p9 = new JPanel(new BorderLayout());
		JPanel p10 = new JPanel(new BorderLayout());
		JPanel p11 = new JPanel(new BorderLayout());
		JPanel p12 = new JPanel(new BorderLayout());
		JPanel p13 = new JPanel(new BorderLayout());
		JPanel p14 = new JPanel(new BorderLayout());
		node1_tmp = node1;
		node2_tmp = node2;
		node3_tmp = node3;
		String[] type = { "Contact_Triangle", "Shell_C0_3" };
		String[] ctype = { "", "EDGE", "OFF" };
		String[] ttype = { "", "ON", "OFF" };
		cb_mat = new JComboBox();
		cb_type = new JComboBox(type);
		cb_ctype = new JComboBox(ctype);
		cb_load = new JComboBox();
		cb_thin = new JComboBox(ttype);
		cb_load.addItem("");
		for (Enumeration en = PreP.LoadDB.keys(); en.hasMoreElements();)
			cb_load.addItem(en.nextElement());
		if (load != null)
			cb_load.setSelectedItem(load.name);
		if (contact != null)
			cb_ctype.setSelectedItem(contact);
		if (thinning != null)
			cb_thin.setSelectedItem(thinning);
		tfn1 = new JTextField(node1 != null ? node1.toString() : "", 3);
		tfn2 = new JTextField(node2 != null ? node2.toString() : "", 3);
		tfn3 = new JTextField(node3 != null ? node3.toString() : "", 3);
		tft = new JTextField(thickness + "");
		tffact = new JTextField(factor != null ? factor + "" : "");
		tffrict = new JTextField(friction != null ? friction + "" : "");
		tfnip = new JTextField(NIP != null ? NIP + "" : "");
		tfpip = new JTextField(PIP != null ? PIP + "" : "");
		tfn1.setEditable(false);
		tfn2.setEditable(false);
		tfn3.setEditable(false);
		b_1 = new JButton("<<");
		b_2 = new JButton("<<");
		b_3 = new JButton("<<");
		b_upd = new JButton(add == true ? "Add" : "Update");
		cb_type.setForeground(Color.blue);

		p.add(p1, BorderLayout.NORTH);
		p1.add(new JLabel("Node 1"));
		p1.add(tfn1);
		p1.add(b_1);
		b_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton2_actionPerformed(e);
			}
		});
		b_1.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					b_2.requestFocus();
				}
			}
		});

		p1.add(new JLabel("Node 2"));
		p1.add(tfn2);
		p1.add(b_2);
		b_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton3_actionPerformed(e);
			}
		});
		b_2.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					b_3.requestFocus();
				}
			}
		});

		p1.add(new JLabel("Node 3"));
		p1.add(tfn3);
		p1.add(b_3);
		b_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton4_actionPerformed(e);
			}
		});
		b_3.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					cb_type.requestFocus();
				}
			}
		});

		JLabel lb1 = new JLabel("Element Type ");
		lb1.setForeground(Color.blue);
		p.add(p6, BorderLayout.CENTER);
		p6.add(cb_type, BorderLayout.CENTER);
		p6.add(lb1, BorderLayout.WEST);
		cb_type.setSelectedItem(msh_name);
		cb_type.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if ((cb_type.getSelectedItem() + "")
						.indexOf("Contact_Triangle") != -1) {
					cb_mat.setEnabled(false);
					cb_ctype.setEnabled(false);
					cb_load.setEnabled(false);
					tfnip.setEnabled(false);
					tfpip.setEnabled(false);
					cb_thin.setEnabled(false);
				} else {
					cb_mat.setEnabled(true);
					cb_ctype.setEnabled(true);
					cb_load.setEnabled(true);
					tfnip.setEnabled(true);
					tfpip.setEnabled(true);
					cb_thin.setEnabled(true);
				}
			}
		});
		if ((cb_type.getSelectedItem() + "").indexOf("Contact_Triangle") != -1) {
			cb_mat.setEnabled(false);
			cb_ctype.setEnabled(false);
			cb_load.setEnabled(false);
			tfnip.setEnabled(false);
			tfpip.setEnabled(false);
			cb_thin.setEnabled(false);
		} else {
			cb_mat.setEnabled(true);
			cb_ctype.setEnabled(true);
			cb_load.setEnabled(true);
			tfnip.setEnabled(true);
			tfpip.setEnabled(true);
			cb_thin.setEnabled(true);
		}
		cb_type.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					cb_mat.requestFocus();
				}
			}
		});

		p.add(p10, BorderLayout.SOUTH);
		p10.add(p2, BorderLayout.CENTER);
		p2.add(p4);
		p4.add(new JLabel("Material"), BorderLayout.WEST);
		p4.add(cb_mat, BorderLayout.CENTER);
		for (Enumeration en = PreP.MatDB.keys(); en.hasMoreElements();)
			cb_mat.addItem(en.nextElement());
		if (material == null)
			cb_mat.setSelectedIndex(0);
		else
			cb_mat.setSelectedItem(material.name);
		cb_mat.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					tft.requestFocus();
					tft.selectAll();
				}
			}
		});

		p2.add(p5);
		p5.add(new JLabel("Thickness"), BorderLayout.WEST);
		p5.add(tft, BorderLayout.CENTER);
		tft.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					tffact.requestFocus();
					tffact.selectAll();
				}
			}
		});

		p2.add(p7);
		p7.add(new JLabel("Factor"), BorderLayout.WEST);
		p7.add(tffact, BorderLayout.CENTER);
		tffact.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					tffrict.requestFocus();
					tffrict.selectAll();
				}
			}
		});

		p2.add(p8);
		p8.add(new JLabel("Friction"), BorderLayout.WEST);
		p8.add(tffrict, BorderLayout.CENTER);
		tffrict.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					cb_ctype.requestFocus();
				}
			}
		});

		p2.add(p9);
		p9.add(new JLabel("Contact"), BorderLayout.WEST);
		p9.add(cb_ctype, BorderLayout.CENTER);
		p10.add(p3, BorderLayout.SOUTH);
		cb_ctype.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					cb_load.requestFocus();
				}
			}
		});

		p2.add(p11);
		p11.add(new JLabel("Load"), BorderLayout.WEST);
		p11.add(cb_load, BorderLayout.CENTER);
		cb_load.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					tfnip.requestFocus();
					tfnip.selectAll();
				}
			}
		});

		p2.add(p12);
		p12.add(new JLabel("NIP"), BorderLayout.WEST);
		p12.add(tfnip, BorderLayout.CENTER);
		tfnip.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					tfpip.requestFocus();
					tfpip.selectAll();
				}
			}
		});

		p2.add(p13);
		p13.add(new JLabel("PIP"), BorderLayout.WEST);
		p13.add(tfpip, BorderLayout.CENTER);
		tfpip.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					cb_thin.requestFocus();
				}
			}
		});

		p2.add(p14);
		p14.add(new JLabel("Thinning"), BorderLayout.WEST);
		p14.add(cb_thin, BorderLayout.CENTER);
		cb_thin.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					b_upd.requestFocus();
				}
			}
		});

		p3.add(b_upd);
		b_upd.addActionListener(new ActionListener() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void actionPerformed(ActionEvent e) {
				jButton1_actionPerformed(e);
			}
		});
		b_upd.addKeyListener(new KeyAdapter() {
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

		p.validate();
		return p;
	}

	void jButton1_actionPerformed(ActionEvent e) {
		try {
			_Element3 tmp = this;

			if (node1_tmp == null || node2_tmp == null || node3_tmp == null) {
				error("Node = null!");
				return;
			}
			float thickness_tmp = java.lang.Float.parseFloat(tft.getText());
			java.lang.Float factor_tmp = tffact.getText().trim().length() == 0 ? null
					: new java.lang.Float(tffact.getText());
			java.lang.Float friction_tmp = tffrict.getText().trim().length() == 0 ? null
					: new java.lang.Float(tffrict.getText());
			Integer NIP_tmp = tfnip.getText().trim().length() == 0 ? null
					: new Integer(tfnip.getText());
			Integer PIP_tmp = tfpip.getText().trim().length() == 0 ? null
					: new Integer(tfpip.getText());
			node1 = node1_tmp;
			node2 = node2_tmp;
			node3 = node3_tmp;
			msh_name = cb_type.getSelectedItem() + "";
			material = (Material) PreP.MatDB.get(cb_mat.getSelectedItem() + "");
			if (cb_type.getSelectedIndex() == 0)
				msh_type = Canvas3D.MESH_Contact_Triangle;
			else
				msh_type = Canvas3D.MESH_Shell_C0_3;
			contact = (cb_ctype.getSelectedItem() + "").length() == 0 ? null
					: cb_ctype.getSelectedItem() + "";
			thinning = (cb_thin.getSelectedItem() + "").length() == 0 ? null
					: cb_thin.getSelectedItem() + "";
			load = (cb_load.getSelectedItem() + "").length() == 0 ? null
					: (Loads) PreP.LoadDB.get(cb_load.getSelectedItem() + "");
			thickness = thickness_tmp;
			friction = friction_tmp;
			factor = factor_tmp;
			NIP = NIP_tmp;
			PIP = PIP_tmp;

			if (add == true) {
				try {
					tmp = (_Element3) this.clone();
					J3D.add3D(tmp);
				} catch (CloneNotSupportedException e1) {
					e1.printStackTrace();
				}
				tmp.add = false;
				b_1.requestFocus();
			}

			J3D.tree_reset();
			J3D.view_reset();
		} catch (Exception e1) {
			error(e1);
		}
	}

	void jButton2_actionPerformed(ActionEvent e) {
		try {
			Object obj = J3D.getSelectedObject3D();
			if (obj != null && obj instanceof _Node) {
				node1_tmp = (_Node) obj;
				tfn1.setText(obj.toString());
				b_2.requestFocus();
			}
		} catch (Exception e1) {
			error(e1);
		}
	}

	void jButton3_actionPerformed(ActionEvent e) {
		try {
			Object obj = J3D.getSelectedObject3D();
			if (obj != null && obj instanceof _Node) {
				node2_tmp = (_Node) obj;
				tfn2.setText(obj.toString());
				b_3.requestFocus();
			}
		} catch (Exception e1) {
			error(e1);
		}
	}

	void jButton4_actionPerformed(ActionEvent e) {
		try {
			Object obj = J3D.getSelectedObject3D();
			if (obj != null && obj instanceof _Node) {
				node3_tmp = (_Node) obj;
				tfn3.setText(obj.toString());
				cb_type.requestFocus();
			}
		} catch (Exception e1) {
			error(e1);
		}
	}

	public void requestFocus() {

		b_1.requestFocus();

	}

	public void requestAction() {
		if (add == true)
			if (b_1.hasFocus())
				jButton2_actionPerformed(null);
			else if (b_2.hasFocus())
				jButton3_actionPerformed(null);
			else if (b_3.hasFocus())
				jButton4_actionPerformed(null);

	}

	public _Node[] get_Nodes() {
		_Node[] arr = { node1, node2, node3 };
		return arr;
	}

	public _Object[] get_Elements() {
		_Object[] arr = { this };
		return arr;
	}

	public _Object duplicate(Canvas3D out, boolean add) {
		_Element3 o = null;
		try {
			o = (_Element3) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		o.node1 = (_Node) node1.duplicate(out, add);
		o.node2 = (_Node) node2.duplicate(out, add);
		o.node3 = (_Node) node3.duplicate(out, add);

		if (add)
			out.add3D(o);

		return o;
	}

	public Vector3D getCenter() {
		Vector3D s = new Vector3D();

		s.add(node1.getCenter(), node2.getCenter());
		s.add(s, node3.getCenter());

		s.scale(1.0f / 3.0f);

		return s;
	}

	public boolean isPickPoint(int x, int y, boolean shw, boolean ogl) {
		boolean check = false;

		if (show == shw)
			for (int i = 0; i < arr.length; i++)
				check = (((shp) arr[i]).isPickPoint(x, y, ogl) == true ? true
						: check);

		return check;
	}

	public boolean isPickPoint(Rectangle2D r, boolean shw, boolean ogl) {
		boolean check = false;

		if (show == shw)
			for (int i = 0; i < arr.length; i++)
				check = (((shp) arr[i]).isPickPoint(r, ogl) == true ? true
						: check);

		return check;
	}

	public void deselectRequiredObjects() {
		if (!selected) {
			node1.setSelected(false);
			node2.setSelected(false);
			node3.setSelected(false);
		}
	}

	/**
	 * Returns the smallest of the triangle angles in degrees
	 * 
	 * @return
	 */
	public double getMinAngle() {
		Vector3D v1, v2, v3;
		double angle1, angle2, angle3;

		v1 = new Vector3D(node2.x - node1.x, node2.y - node1.y, node2.z
				- node1.z);
		v2 = new Vector3D(node3.x - node1.x, node3.y - node1.y, node3.z
				- node1.z);
		v3 = new Vector3D(node3.x - node2.x, node3.y - node2.y, node3.z
				- node2.z);

		v1.toUnitLength();
		v2.toUnitLength();
		v3.toUnitLength();

		angle1 = Math.abs(Math.acos(Vector3D.dot(v2, v1)));
		angle2 = Math.abs(Math.acos(Vector3D.dot(v2, v3)));
		angle3 = Math.PI - angle2 - angle1;

		return 180 * Math.min(angle1, Math.min(angle2, angle3)) / Math.PI;
	}

	/**
	 * Returns the shortest length of any of the triangle sides.
	 * 
	 * @return
	 */
	public double getMinLength() {
		Vector3D v1, v2, v3;

		v1 = new Vector3D(node2.x - node1.x, node2.y - node1.y, node2.z
				- node1.z);
		v2 = new Vector3D(node3.x - node1.x, node3.y - node1.y, node3.z
				- node1.z);
		v3 = new Vector3D(node3.x - node2.x, node3.y - node2.y, node3.z
				- node2.z);

		return Math.min(v3.getLength(),
				Math.min(v1.getLength(), v2.getLength()));
	}

	/**
	 * Returns the longest length of any of the triangle sides.
	 * 
	 * @return
	 */
	public double getMaxLength() {
		Vector3D v1, v2, v3;

		v1 = new Vector3D(node2.x - node1.x, node2.y - node1.y, node2.z
				- node1.z);
		v2 = new Vector3D(node3.x - node1.x, node3.y - node1.y, node3.z
				- node1.z);
		v3 = new Vector3D(node3.x - node2.x, node3.y - node2.y, node3.z
				- node2.z);

		return Math.max(v3.getLength(),
				Math.max(v1.getLength(), v2.getLength()));
	}

	public double getB() {
		return circumcircle_radius / this.getMinLength();
	}

	public void setCircumcircle_radius(double circumcircle_radius) {
		this.circumcircle_radius = circumcircle_radius;
	}

	public void replaceObjectWith(_Object o, _Object replacement) {
		if (!(o instanceof _Node) || !(replacement instanceof _Node))
			return;

		if (node1 == (_Node) o)
			node1 = (_Node) replacement;
		if (node2 == (_Node) o)
			node2 = (_Node) replacement;
		if (node3 == (_Node) o)
			node3 = (_Node) replacement;

	}

}
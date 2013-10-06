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

public class _Element2 extends _Element implements Serializable {
	public _Node node1, node2, node1_tmp, node2_tmp;
	public Material material = null;
	public Loads load;
	public Color contur = Color.black;
	public float diameter;
	public java.lang.Float factor, friction;
	public String contact;
	public boolean selected = false;
	public String msh_name = "Contact_Line";
	public int msh_type = Canvas3D.MESH_Beam_2;
	private JComboBox cb_type, cb_ctype, cb_mat;
	private JTextField tfn1, tfn2, tfd, tffact, tffrict;
	private Object[] arr;
	private JButton b_1, b_2, b_upd;

	public _Element2(boolean add) {
		this.add = add;
		msh_type = Canvas3D.MESH_Rod_2;
		msh_name = "Rod_2";
	}

	public _Element2(_Node n1, _Node n2, int t, Material m, float d) {
		msh_type = t;
		if (t == Canvas3D.MESH_Dummy_2)
			msh_name = "Dummy_2";
		else if (t == Canvas3D.MESH_Beam_2)
			msh_name = "Beam_2";
		else if (t == Canvas3D.MESH_Rod_2)
			msh_name = "Rod_2";
		else if (t == Canvas3D.MESH_Contact_Line)
			msh_name = "Contact_Line";
		node1 = n1;
		node2 = n2;
		material = m;
		diameter = d;
	}

	public _Element2(_Node n1, _Node n2, int t, float d) {
		this(n1, n2, t, null, d);
	}

	public _Element2(_Node n1, _Node n2, int t) {
		this(n1, n2, t, null, 1);
	}

	public _Element2(_Node n1, _Node n2) {
		this(n1, n2, Canvas3D.MESH_Rod_2, null, 1);
	}

	public String writeObject() {
		String st = new String(Id);
		st += " \tnodes = [" + node1.get_Id() + "," + node2.get_Id()
				+ "] \tD = " + diameter;
		if (factor != null)
			st += " \tFactor = " + factor;
		if (friction != null)
			st += " \tFriction = " + friction;

		if (msh_type == Canvas3D.MESH_Contact_Line)
			return st;

		if (material != null)
			st += "\tMaterial = " + material.name;
		if (contact != null && contact.compareToIgnoreCase("OFF") == 0)
			st += " \tContact = OFF";

		return st;
	}

	public void readObject(String st_load) {
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
	}

	public void reset(boolean do_mesh) {
	};

	public void mesh(int type, float size) {
	}

	public boolean delete() {
		return selected;
	}

	public Object[] get_Array(Canvas3D j3d) {
		int k = 0;
		arr = new Object[1 + (j3d.getSHOW_ID_ELEMENT() ? 1 : 0)];
		if (material == null)
			arr[k++] = new shpLine(node1.x, node1.y, node1.z, node2.x, node2.y,
					node2.z, selected ? Canvas3D.SELECTCOLOR : contur);
		else
			arr[k++] = new shpLine(node1.x, node1.y, node1.z, node2.x, node2.y,
					node2.z, selected ? Canvas3D.SELECTCOLOR : material.color);

		if (j3d.getSHOW_ID_ELEMENT()) {
			arr[k++] = new shpOrientedText(get_Id(), (node1.x + node2.x) / 2,
					(node1.y + node2.y) / 2, (node1.z + node2.z) / 2, contur);
		}
		if (j3d.getSHOW_ID_LOADS() && load != null)
			arr[k++] = new shpOrientedText(load.name, (node1.x + node2.x) / 2,
					(node1.y + node2.y) / 2, (node1.z + node2.z) / 2,
					load.color);

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
		return node;
	}

	public void deselectRequiredObjects() {
		if (!selected) {
			node1.setSelected(false);
			node2.setSelected(false);
		}
	}

	public void transform3D(Matrix3D t) {
		if (!selected || processed)
			return;
		node1.setSelected(true);
		node2.setSelected(true);
		node1.transform3D(t);
		node2.transform3D(t);
		node1.setSelected(true);
		node2.setSelected(true);
		node1.setProcessed(true);
		node2.setProcessed(true);
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
		JPanel p1 = new JPanel(new GridLayout(2, 3));
		JPanel p3 = new JPanel();
		JPanel p2 = new JPanel(new GridLayout(5, 1));
		JPanel p4 = new JPanel(new BorderLayout());
		JPanel p5 = new JPanel(new BorderLayout());
		JPanel p6 = new JPanel(new BorderLayout());
		JPanel p7 = new JPanel(new BorderLayout());
		JPanel p8 = new JPanel(new BorderLayout());
		JPanel p9 = new JPanel(new BorderLayout());
		JPanel p10 = new JPanel(new BorderLayout());
		String[] type = { "Rod_2", "Beam_2", "Contact_Line" };
		cb_type = new JComboBox(type);
		String[] ctype = { "", "BASIC", "OFF" };
		cb_ctype = new JComboBox(ctype);
		cb_mat = new JComboBox();
		tfn1 = new JTextField((node1 != null ? node1.toString() : ""), 3);
		tfn2 = new JTextField((node2 != null ? node2.toString() : ""), 3);
		tfd = new JTextField(diameter + "");
		tffact = new JTextField(factor != null ? factor + "" : "");
		tffrict = new JTextField(friction != null ? friction + "" : "");
		tfn1.setEditable(false);
		tfn2.setEditable(false);
		b_1 = new JButton("<<");
		b_2 = new JButton("<<");
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
					jButton2_actionPerformed(null);
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
					jButton3_actionPerformed(null);
				}
			}
		});

		JLabel lb1 = new JLabel("Element Type ");
		lb1.setForeground(Color.blue);

		p.add(p6, BorderLayout.CENTER);
		p6.add(lb1, BorderLayout.WEST);
		p6.add(cb_type, BorderLayout.CENTER);
		cb_type.setSelectedItem(msh_name);
		cb_type.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if ((cb_type.getSelectedItem() + "").indexOf("Rod_2") != -1) {
					tfd.setEnabled(true);
					cb_mat.setEnabled(true);
					tffact.setEnabled(true);
					tffrict.setEnabled(true);
					cb_ctype.setEnabled(true);
				} else if ((cb_type.getSelectedItem() + "").indexOf("Beam_2") != -1) {
					tfd.setEnabled(true);
					cb_mat.setEnabled(true);
					tffact.setEnabled(false);
					tffrict.setEnabled(false);
					cb_ctype.setEnabled(false);
				} else if ((cb_type.getSelectedItem() + "")
						.indexOf("Contact_Line") != -1) {
					tfd.setEnabled(true);
					cb_mat.setEnabled(false);
					tffact.setEnabled(true);
					tffrict.setEnabled(true);
					cb_ctype.setEnabled(true);
				}
			}
		});
		cb_ctype.setSelectedItem(contact != null ? contact : "");
		if ((cb_type.getSelectedItem() + "").indexOf("Rod_2") != -1) {
			tfd.setEnabled(true);
			cb_mat.setEnabled(true);
			tffact.setEnabled(true);
			tffrict.setEnabled(true);
			cb_ctype.setEnabled(true);
		} else if ((cb_type.getSelectedItem() + "").indexOf("Beam_2") != -1) {
			tfd.setEnabled(true);
			cb_mat.setEnabled(true);
			tffact.setEnabled(false);
			tffrict.setEnabled(false);
			cb_ctype.setEnabled(false);
		} else if ((cb_type.getSelectedItem() + "").indexOf("Contact_Line") != -1) {
			tfd.setEnabled(true);
			cb_mat.setEnabled(false);
			tffact.setEnabled(true);
			tffrict.setEnabled(true);
			cb_ctype.setEnabled(true);
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
					tfd.requestFocus();
					tfd.selectAll();
				}
			}
		});

		p2.add(p5);
		p5.add(new JLabel("Diameter"), BorderLayout.WEST);
		p5.add(tfd, BorderLayout.CENTER);
		tfd.addKeyListener(new KeyAdapter() {
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
		cb_ctype.addKeyListener(new KeyAdapter() {
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

		p10.add(p3, BorderLayout.SOUTH);
		p3.add(b_upd);
		b_upd.addActionListener(new ActionListener() {
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
			_Element2 tmp = this;

			if ((node1_tmp == null || node2_tmp == null)
					&& (node1 == null || node2 == null))
				return;
			float d = java.lang.Float.parseFloat(tfd.getText());
			java.lang.Float fact, frict;
			fact = tffact.getText().trim().length() == 0 ? null
					: new java.lang.Float(tffact.getText());
			frict = tffrict.getText().trim().length() == 0 ? null
					: new java.lang.Float(tffrict.getText());
			if (node1_tmp != null)
				node1 = node1_tmp;
			if (node2_tmp != null)
				node2 = node2_tmp;
			diameter = d;
			factor = fact;
			friction = frict;
			contact = (cb_ctype.getSelectedItem() + "").length() == 0 ? null
					: cb_ctype.getSelectedItem() + "";
			msh_name = cb_type.getSelectedItem() + "";
			if (cb_type.getSelectedIndex() == 0)
				msh_type = Canvas3D.MESH_Rod_2;
			else if (cb_type.getSelectedIndex() == 1)
				msh_type = Canvas3D.MESH_Beam_2;
			else if (cb_type.getSelectedIndex() == 2)
				msh_type = Canvas3D.MESH_Contact_Line;
			material = (Material) PreP.MatDB.get(cb_mat.getSelectedItem() + "");

			if (add == true) {
				try {
					tmp = (_Element2) this.clone();
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
	}

	public _Node[] get_Nodes() {
		_Node[] arr = { node1, node2 };
		return arr;
	}

	public _Object[] get_Elements() {
		_Object[] arr = { this };
		return arr;
	}

	public _Object duplicate(Canvas3D out, boolean add) {
		_Element2 o = null;
		try {
			o = (_Element2) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		o.node1 = (_Node) node1.duplicate(out, add);
		o.node2 = (_Node) node2.duplicate(out, add);

		if (add)
			out.add3D(o);

		return o;
	}

	public Vector3D getCenter() {
		Vector3D s = new Vector3D();

		s.add(node1.getCenter(), node2.getCenter());
		s.scale(0.5f);

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

	public void replaceObjectWith(_Object o, _Object replacement) {
		if (!(o instanceof _Node) || !(replacement instanceof _Node))
			return;

		if (node1 == (_Node) o)
			node1 = (_Node) replacement;
		if (node2 == (_Node) o)
			node2 = (_Node) replacement;

	}

}
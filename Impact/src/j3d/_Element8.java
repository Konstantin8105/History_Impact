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

public class _Element8 extends _Element implements Serializable {
	public _Node node1, node2, node3, node4, node5, node6, node7, node8,
			node1_tmp, node2_tmp, node3_tmp, node4_tmp, node5_tmp, node6_tmp,
			node7_tmp, node8_tmp;
	public Material material = null;
	public Loads load;
	public Integer NIP = new Integer(8);
	public Color contur = Color.black;
	public boolean selected = false;
	private String msh_name = "Solid_Iso_6";
	private JComboBox cb_mat;
	private JTextField tfn1, tfn2, tfn3, tfn4, tfn5, tfn6, tfn7, tfn8, tfnip;
	private Object[] arr;
	private JButton b_1, b_2, b_3, b_4, b_5, b_6, b_7, b_8, b_upd;

	public void reset(boolean do_mesh) {
	}

	public void mesh(int type, float size) {
	}

	public boolean delete() {
		return selected;
	}

	public _Element8(boolean add) {
		this.add = add;
	}

	public _Element8(_Node n1, _Node n2, _Node n3, _Node n4, _Node n5,
			_Node n6, _Node n7, _Node n8, Material m) {
		node1 = n1;
		node2 = n2;
		node3 = n3;
		node4 = n4;
		node5 = n5;
		node6 = n6;
		node7 = n7;
		node8 = n8;
		material = m;
	}

	public _Element8(_Node n1, _Node n2, _Node n3, _Node n4, _Node n5,
			_Node n6, _Node n7, _Node n8) {
		this(n1, n2, n3, n4, n5, n6, n7, n8, null);
	}

	public String writeObject() {
		String st = new String(Id);

		st += " \tnodes = [" + node1.get_Id() + "," + node2.get_Id() + ","
				+ node3.get_Id() + "," + node4.get_Id() + "," + node5.get_Id()
				+ "," + node6.get_Id() + "," + node7.get_Id() + ","
				+ node8.get_Id() + "] \tNIP = 8 \tMaterial = "
				+ (material != null ? material.name : "");

		return st;
	}

	public void readObject(String st_load) {
		try {
			NIP = new Integer(getValue(st_load, "NIP"));
		} catch (Exception e1) {
		}
	}

	public Object[] get_Array(Canvas3D j3d) {
		int k = 0;
		arr = new Object[6 + (j3d.getSHOW_ID_ELEMENT() ? 1 : 0)];

		if (material == null) {
			arr[k++] = new shpQuad(node1.x, node1.y, node1.z, node2.x, node2.y,
					node2.z, node3.x, node3.y, node3.z, node4.x, node4.y,
					node4.z, selected ? Canvas3D.SELECTCOLOR : null, contur);
			arr[k++] = new shpQuad(node1.x, node1.y, node1.z, node2.x, node2.y,
					node2.z, node6.x, node6.y, node6.z, node5.x, node5.y,
					node5.z, selected ? Canvas3D.SELECTCOLOR : null, contur);
			arr[k++] = new shpQuad(node2.x, node2.y, node2.z, node3.x, node3.y,
					node3.z, node7.x, node7.y, node7.z, node6.x, node6.y,
					node6.z, selected ? Canvas3D.SELECTCOLOR : null, contur);
			arr[k++] = new shpQuad(node3.x, node3.y, node3.z, node4.x, node4.y,
					node4.z, node8.x, node8.y, node8.z, node7.x, node7.y,
					node7.z, selected ? Canvas3D.SELECTCOLOR : null, contur);
			arr[k++] = new shpQuad(node1.x, node1.y, node1.z, node4.x, node4.y,
					node4.z, node8.x, node8.y, node8.z, node5.x, node5.y,
					node5.z, selected ? Canvas3D.SELECTCOLOR : null, contur);
			arr[k++] = new shpQuad(node5.x, node5.y, node5.z, node6.x, node6.y,
					node6.z, node7.x, node7.y, node7.z, node8.x, node8.y,
					node8.z, selected ? Canvas3D.SELECTCOLOR : null, contur);
		} else {
			arr[k++] = new shpQuad(node1.x, node1.y, node1.z, node2.x, node2.y,
					node2.z, node3.x, node3.y, node3.z, node4.x, node4.y,
					node4.z, selected ? Canvas3D.SELECTCOLOR : material.color,
					selected ? Canvas3D.SELECTCOLOR : material.color);
			arr[k++] = new shpQuad(node1.x, node1.y, node1.z, node2.x, node2.y,
					node2.z, node6.x, node6.y, node6.z, node5.x, node5.y,
					node5.z, selected ? Canvas3D.SELECTCOLOR : material.color,
					selected ? Canvas3D.SELECTCOLOR : material.color);
			arr[k++] = new shpQuad(node2.x, node2.y, node2.z, node3.x, node3.y,
					node3.z, node7.x, node7.y, node7.z, node6.x, node6.y,
					node6.z, selected ? Canvas3D.SELECTCOLOR : material.color,
					selected ? Canvas3D.SELECTCOLOR : material.color);
			arr[k++] = new shpQuad(node3.x, node3.y, node3.z, node4.x, node4.y,
					node4.z, node8.x, node8.y, node8.z, node7.x, node7.y,
					node7.z, selected ? Canvas3D.SELECTCOLOR : material.color,
					selected ? Canvas3D.SELECTCOLOR : material.color);
			arr[k++] = new shpQuad(node1.x, node1.y, node1.z, node4.x, node4.y,
					node4.z, node8.x, node8.y, node8.z, node5.x, node5.y,
					node5.z, selected ? Canvas3D.SELECTCOLOR : material.color,
					selected ? Canvas3D.SELECTCOLOR : material.color);
			arr[k++] = new shpQuad(node5.x, node5.y, node5.z, node6.x, node6.y,
					node6.z, node7.x, node7.y, node7.z, node8.x, node8.y,
					node8.z, selected ? Canvas3D.SELECTCOLOR : material.color,
					selected ? Canvas3D.SELECTCOLOR : material.color);

		}

		if (j3d.getSHOW_ID_ELEMENT()) {
			arr[k++] = new shpOrientedText(get_Id(),
					(node1.x + node2.x + node3.x + node4.x + node5.x + node6.x
							+ node7.x + node8.x) / 8,
					(node1.y + node2.y + node3.y + node4.y + node5.y + node6.y
							+ node7.y + node8.y) / 8,
					(node1.z + node2.z + node3.z + node4.z + node5.z + node6.z
							+ node7.z + node8.z) / 8, contur);
		}

		if (j3d.getSHOW_ID_LOADS() && load != null)
			arr[k++] = new shpOrientedText(load.name,
					(node1.x + node2.x + node3.x + node4.x + node5.x + node6.x
							+ node7.x + node8.x) / 8,
					(node1.y + node2.y + node3.y + node4.y + node5.y + node6.y
							+ node7.y + node8.y) / 8,
					(node1.z + node2.z + node3.z + node4.z + node5.z + node6.z
							+ node7.z + node8.z) / 8, load.color);

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
		node.add(node4.get_TreeNode());
		node.add(node5.get_TreeNode());
		node.add(node6.get_TreeNode());
		node.add(node7.get_TreeNode());
		node.add(node8.get_TreeNode());
		return node;
	}

	public void transform3D(Matrix3D t) {
		if (!selected || processed)
			return;
		node1.setSelected(true);
		node2.setSelected(true);
		node3.setSelected(true);
		node4.setSelected(true);
		node5.setSelected(true);
		node6.setSelected(true);
		node7.setSelected(true);
		node8.setSelected(true);
		node1.transform3D(t);
		node2.transform3D(t);
		node3.transform3D(t);
		node4.transform3D(t);
		node5.transform3D(t);
		node6.transform3D(t);
		node7.transform3D(t);
		node8.transform3D(t);
		node1.setSelected(true);
		node2.setSelected(true);
		node3.setSelected(true);
		node4.setSelected(true);
		node5.setSelected(true);
		node6.setSelected(true);
		node7.setSelected(true);
		node8.setSelected(true);
		node1.setProcessed(true);
		node2.setProcessed(true);
		node3.setProcessed(true);
		node4.setProcessed(true);
		node5.setProcessed(true);
		node6.setProcessed(true);
		node7.setProcessed(true);
		node8.setProcessed(true);
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
		JPanel p1 = new JPanel(new GridLayout(8, 3));
		JPanel p3 = new JPanel();
		JPanel p2 = new JPanel(new GridLayout(2, 1));
		JPanel p4 = new JPanel(new BorderLayout());
		JPanel p10 = new JPanel(new BorderLayout());
		JPanel p12 = new JPanel(new BorderLayout());
		node1_tmp = node1;
		node2_tmp = node2;
		node3_tmp = node3;
		node4_tmp = node4;
		node5_tmp = node5;
		node6_tmp = node6;
		node7_tmp = node7;
		node8_tmp = node8;
		cb_mat = new JComboBox();
		tfn1 = new JTextField((node1 != null ? node1.toString() : ""), 3);
		tfn2 = new JTextField((node2 != null ? node2.toString() : ""), 3);
		tfn3 = new JTextField((node3 != null ? node3.toString() : ""), 3);
		tfn4 = new JTextField((node4 != null ? node4.toString() : ""), 3);
		tfn5 = new JTextField((node5 != null ? node5.toString() : ""), 3);
		tfn6 = new JTextField((node6 != null ? node6.toString() : ""), 3);
		tfn7 = new JTextField((node7 != null ? node7.toString() : ""), 3);
		tfn8 = new JTextField((node8 != null ? node8.toString() : ""), 3);
		tfnip = new JTextField(NIP + "");
		tfn1.setEditable(false);
		tfn2.setEditable(false);
		tfn3.setEditable(false);
		tfn4.setEditable(false);
		tfn5.setEditable(false);
		tfn6.setEditable(false);
		tfn7.setEditable(false);
		tfn8.setEditable(false);

		b_1 = new JButton("<<");
		b_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton1_actionPerformed(e);
			}
		});
		b_1.addKeyListener(new KeyAdapter() {
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

		b_2 = new JButton("<<");
		b_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton2_actionPerformed(e);
			}
		});
		b_2.addKeyListener(new KeyAdapter() {
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

		b_3 = new JButton("<<");
		b_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton3_actionPerformed(e);
			}
		});
		b_3.addKeyListener(new KeyAdapter() {
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

		b_4 = new JButton("<<");
		b_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton4_actionPerformed(e);
			}
		});
		b_4.addKeyListener(new KeyAdapter() {
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

		b_5 = new JButton("<<");
		b_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton5_actionPerformed(e);
			}
		});
		b_5.addKeyListener(new KeyAdapter() {
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

		b_6 = new JButton("<<");
		b_6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton6_actionPerformed(e);
			}
		});
		b_6.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					jButton6_actionPerformed(null);
				}
			}
		});

		b_7 = new JButton("<<");
		b_7.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton7_actionPerformed(e);
			}
		});
		b_7.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					jButton7_actionPerformed(null);
				}
			}
		});

		b_8 = new JButton("<<");
		b_8.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton8_actionPerformed(e);
			}
		});
		b_8.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					jButton8_actionPerformed(null);
				}
			}
		});

		b_upd = new JButton(add == true ? "Add" : "Update");
		b_upd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton0_actionPerformed(e);
			}
		});
		b_upd.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					jButton0_actionPerformed(null);
				}
			}
		});

		p.add(p1, BorderLayout.CENTER);
		p1.add(new JLabel("Node 1"));
		p1.add(tfn1);
		p1.add(b_1);
		p1.add(new JLabel("Node 2"));
		p1.add(tfn2);
		p1.add(b_2);
		p1.add(new JLabel("Node 3"));
		p1.add(tfn3);
		p1.add(b_3);
		p1.add(new JLabel("Node 4"));
		p1.add(tfn4);
		p1.add(b_4);
		p1.add(new JLabel("Node 5"));
		p1.add(tfn5);
		p1.add(b_5);
		p1.add(new JLabel("Node 6"));
		p1.add(tfn6);
		p1.add(b_6);
		p1.add(new JLabel("Node 7"));
		p1.add(tfn7);
		p1.add(b_7);
		p1.add(new JLabel("Node 8"));
		p1.add(tfn8);
		p1.add(b_8);
		JLabel lb = new JLabel("Edit - " + toString());
		lb.setForeground(Color.blue);
		p.add(lb, BorderLayout.NORTH);
		p.add(p10, BorderLayout.SOUTH);
		p10.add(p2, BorderLayout.CENTER);
		p2.add(p4);
		p4.add(new JLabel("Material"), BorderLayout.WEST);
		p4.add(cb_mat, BorderLayout.CENTER);
		cb_mat.addKeyListener(new KeyAdapter() {
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
					b_upd.requestFocus();
				}
			}
		});
		p10.add(p3, BorderLayout.SOUTH);
		p3.add(b_upd);
		for (Enumeration en = PreP.MatDB.keys(); en.hasMoreElements();)
			cb_mat.addItem(en.nextElement());
		if (material == null)
			cb_mat.setSelectedIndex(0);
		else
			cb_mat.setSelectedItem(material.name);
		p.validate();
		return p;
	}

	void jButton0_actionPerformed(ActionEvent e) {
		try {
			_Element8 tmp = this;

			if (node1_tmp == null || node2_tmp == null || node3_tmp == null
					|| node4_tmp == null || node5_tmp == null
					|| node6_tmp == null || node7_tmp == null
					|| node8_tmp == null) {
				error("Node = null!");
				return;
			}
			material = (Material) PreP.MatDB.get(cb_mat.getSelectedItem() + "");
			NIP = new Integer(tfnip.getText());
			node1 = node1_tmp;
			node2 = node2_tmp;
			node3 = node3_tmp;
			node4 = node4_tmp;
			node5 = node5_tmp;
			node6 = node6_tmp;
			node7 = node7_tmp;
			node8 = node8_tmp;

			if (add == true) {
				try {
					tmp = (_Element8) this.clone();
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

	void jButton1_actionPerformed(ActionEvent e) {
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

	void jButton2_actionPerformed(ActionEvent e) {
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

	void jButton3_actionPerformed(ActionEvent e) {
		try {
			Object obj = J3D.getSelectedObject3D();
			if (obj != null && obj instanceof _Node) {
				node3_tmp = (_Node) obj;
				tfn3.setText(obj.toString());
				b_4.requestFocus();
			}
		} catch (Exception e1) {
			error(e1);
		}
	}

	void jButton4_actionPerformed(ActionEvent e) {
		try {
			Object obj = J3D.getSelectedObject3D();
			if (obj != null && obj instanceof _Node) {
				node4_tmp = (_Node) obj;
				tfn4.setText(obj.toString());
				b_5.requestFocus();
			}
		} catch (Exception e1) {
			error(e1);
		}
	}

	void jButton5_actionPerformed(ActionEvent e) {
		try {
			Object obj = J3D.getSelectedObject3D();
			if (obj != null && obj instanceof _Node) {
				node5_tmp = (_Node) obj;
				tfn5.setText(obj.toString());
				b_6.requestFocus();
			}
		} catch (Exception e1) {
			error(e1);
		}
	}

	void jButton6_actionPerformed(ActionEvent e) {
		try {
			Object obj = J3D.getSelectedObject3D();
			if (obj != null && obj instanceof _Node) {
				node6_tmp = (_Node) obj;
				tfn6.setText(obj.toString());
				b_7.requestFocus();
			}
		} catch (Exception e1) {
			error(e1);
		}
	}

	void jButton7_actionPerformed(ActionEvent e) {
		try {
			Object obj = J3D.getSelectedObject3D();
			if (obj != null && obj instanceof _Node) {
				node7_tmp = (_Node) obj;
				tfn7.setText(obj.toString());
				b_8.requestFocus();
			}
		} catch (Exception e1) {
			error(e1);
		}
	}

	void jButton8_actionPerformed(ActionEvent e) {
		try {
			Object obj = J3D.getSelectedObject3D();
			if (obj != null && obj instanceof _Node) {
				node8_tmp = (_Node) obj;
				tfn8.setText(obj.toString());
				cb_mat.requestFocus();
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
				jButton1_actionPerformed(null);
			else if (b_2.hasFocus())
				jButton2_actionPerformed(null);
			else if (b_3.hasFocus())
				jButton3_actionPerformed(null);
			else if (b_4.hasFocus())
				jButton4_actionPerformed(null);
			else if (b_5.hasFocus())
				jButton5_actionPerformed(null);
			else if (b_6.hasFocus())
				jButton6_actionPerformed(null);
			else if (b_7.hasFocus())
				jButton7_actionPerformed(null);
			else if (b_8.hasFocus())
				jButton8_actionPerformed(null);
	}

	public _Node[] get_Nodes() {
		_Node[] arr = { node1, node2, node3, node4, node5, node6, node7, node8 };
		return arr;
	}

	public _Object[] get_Elements() {
		_Object[] arr = { this };
		return arr;
	}

	public _Object duplicate(Canvas3D out, boolean add) {
		_Element8 o = null;
		try {
			o = (_Element8) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		o.node1 = (_Node) node1.duplicate(out, add);
		o.node2 = (_Node) node2.duplicate(out, add);
		o.node3 = (_Node) node3.duplicate(out, add);
		o.node4 = (_Node) node4.duplicate(out, add);
		o.node5 = (_Node) node5.duplicate(out, add);
		o.node6 = (_Node) node6.duplicate(out, add);
		o.node7 = (_Node) node7.duplicate(out, add);
		o.node8 = (_Node) node8.duplicate(out, add);

		if (add)
			out.add3D(o);

		return o;
	}

	public Vector3D getCenter() {
		Vector3D s = new Vector3D();

		s.add(node1.getCenter(), node2.getCenter());
		s.add(s, node3.getCenter());
		s.add(s, node4.getCenter());
		s.add(s, node5.getCenter());
		s.add(s, node6.getCenter());
		s.add(s, node7.getCenter());
		s.add(s, node8.getCenter());

		s.scale(1.0f / 8.0f);

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

	public void deselectRequiredObjects(Vector v) {
		if (!selected) {
			node1.setSelected(false);
			node2.setSelected(false);
			node3.setSelected(false);
			node4.setSelected(false);
			node5.setSelected(false);
			node6.setSelected(false);
			node7.setSelected(false);
			node8.setSelected(false);
		}
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
		if (node4 == (_Node) o)
			node4 = (_Node) replacement;
		if (node5 == (_Node) o)
			node5 = (_Node) replacement;
		if (node6 == (_Node) o)
			node6 = (_Node) replacement;
		if (node7 == (_Node) o)
			node7 = (_Node) replacement;
		if (node8 == (_Node) o)
			node8 = (_Node) replacement;

	}

}
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

public class _Element4 extends _Element implements Serializable {
	public _Node node1, node2, node3, node4, node1_tmp, node2_tmp, node3_tmp,
			node4_tmp;
	public Material material = null;
	public Loads load;
	public float thickness;
	public Integer NIP = new Integer(5), PIP;
	public java.lang.Float factor/* = new Float(10) */, friction, shear_factor,
			MHC, OOPHC, RHC;
	public String hourglass;// = "ON";
	public String contact;// = "BASIC";
	public String thinning;// = "ON";
	public Color contur = Color.black;
	public boolean selected = false;
	public String msh_name = "Shell_BT_4";
	public int msh_type = Canvas3D.MESH_Shell_BT_4;
	private JComboBox cb_ctype, cb_mat, cb_load, cb_thin, cb_hourglass;
	private JTextField tfn1, tfn2, tfn3, tfn4, tft, tffact, tffrict, tfnip,
			tfpip, tfshear, tfmhc, tfoophc, tfrhc;
	private Object[] arr;
	private JButton b_1, b_2, b_3, b_4, b_upd;

	public void reset(boolean do_mesh) {
	};

	public void mesh(int type, float size) {
	}

	public boolean delete() {
		return selected;
	}

	public _Element4(boolean add, int t) {
		this.add = add;
		msh_type = t;
		msh_name = (msh_type == Canvas3D.MESH_Shell_BT_4 ? "Shell_BT_4"
				: "Solid_Iso_4");
		if (Canvas3D.MESH_Shell_BT_4 == t)
			NIP = new Integer(5);
		else
			NIP = new Integer(1);
	}

	public _Element4(_Node n1, _Node n2, _Node n3, _Node n4, int type,
			Material m, float th) {
		node1 = n1;
		node2 = n2;
		node3 = n3;
		node4 = n4;
		msh_type = type;
		material = m;
		thickness = th;
		msh_name = (msh_type == Canvas3D.MESH_Shell_BT_4 ? "Shell_BT_4"
				: "Solid_Iso_4");
		if (Canvas3D.MESH_Shell_BT_4 == type)
			NIP = new Integer(5);
		else
			NIP = new Integer(1);
	}

	public _Element4(_Node n1, _Node n2, _Node n3, _Node n4, int type, float th) {
		this(n1, n2, n3, n4, type, null, th);
	}

	public _Element4(_Node n1, _Node n2, _Node n3, _Node n4, int type) {
		this(n1, n2, n3, n4, type, null, 1);
	}

	public _Element4(_Node n1, _Node n2, _Node n3, _Node n4) {
		this(n1, n2, n3, n4, Canvas3D.MESH_Shell_BT_4, null, 1);
	}

	public String writeObject() {
		String st = new String(Id);

		st += " \tnodes = ["
				+ node1.get_Id()
				+ ","
				+ node2.get_Id()
				+ ","
				+ node3.get_Id()
				+ ","
				+ node4.get_Id()
				+ "] "
				+ (msh_type == Canvas3D.MESH_Shell_BT_4 ? "\tT = " + thickness
						: "") + " \tMaterial = "
				+ (material != null ? material.name : "");

		if (NIP != null)
			st += " \tNIP = " + NIP;
		if (msh_type == Canvas3D.MESH_Solid_Iso_4)
			return st;

		if (PIP != null)
			st += " \tPIP = " + PIP;
		if (load != null)
			st += " \tLoad = " + load.name;
		if (factor != null)
			st += " \tFactor = " + factor;
		if (contact != null && contact.length() != 0)
			st += " \tContact = " + contact;
		if (friction != null)
			st += " \tFriction = " + friction;
		if (thinning != null && thinning.compareToIgnoreCase("OFF") == 0)
			st += " \tThinning = OFF";
		if (shear_factor != null)
			st += " \tSHEAR_FACTOR = " + shear_factor;
		if (hourglass != null && hourglass.compareToIgnoreCase("OFF") == 0)
			st += " \tHOURGLASS = OFF";
		if (MHC != null)
			st += " \tMHC = " + MHC;
		if (OOPHC != null)
			st += " \tOOPHC = " + OOPHC;
		if (RHC != null)
			st += " \tRHC = " + RHC;

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
			shear_factor = new java.lang.Float(
					getValue(st_load, "SHEAR_FACTOR"));
		} catch (Exception e1) {
		}
		try {
			hourglass = getValue(st_load, "HOURGLASS");
		} catch (Exception e1) {
		}
		try {
			MHC = new java.lang.Float(getValue(st_load, "MHC"));
		} catch (Exception e1) {
		}
		try {
			OOPHC = new java.lang.Float(getValue(st_load, "OOPHC"));
		} catch (Exception e1) {
		}
		try {
			RHC = new java.lang.Float(getValue(st_load, "RHC"));
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
		if (this.msh_type == Canvas3D.MESH_Shell_BT_4) {
			arr = new Object[1 + (j3d.getSHOW_ID_ELEMENT() ? 1 : 0)
					+ ((j3d.getSHOW_ID_LOADS() && load != null) ? 1 : 0)];
			if (node1.x == node2.x && node1.y == node2.y && node1.z == node2.z) {
				if (material == null)
					arr[k++] = new shpTriangle(node2.x, node2.y, node2.z,
							node3.x, node3.y, node3.z, node4.x, node4.y,
							node4.z, selected ? j3d.SELECTCOLOR : null, contur);
				else
					arr[k++] = new shpTriangle(node2.x, node2.y, node2.z,
							node3.x, node3.y, node3.z, node4.x, node4.y,
							node4.z, selected ? j3d.SELECTCOLOR
									: material.color,
							selected ? j3d.SELECTCOLOR : material.color);
			} else if (node3.x == node2.x && node3.y == node2.y
					&& node3.z == node2.z) {
				if (material == null)
					arr[k++] = new shpTriangle(node1.x, node1.y, node1.z,
							node3.x, node3.y, node3.z, node4.x, node4.y,
							node4.z, selected ? j3d.SELECTCOLOR : null, contur);
				else
					arr[k++] = new shpTriangle(node1.x, node1.y, node1.z,
							node3.x, node3.y, node3.z, node4.x, node4.y,
							node4.z, selected ? j3d.SELECTCOLOR
									: material.color,
							selected ? j3d.SELECTCOLOR : material.color);
			} else {
				if (material == null)
					arr[k++] = new shpQuad(node1.x, node1.y, node1.z, node2.x,
							node2.y, node2.z, node3.x, node3.y, node3.z,
							node4.x, node4.y, node4.z,
							selected ? j3d.SELECTCOLOR : null, contur);
				else
					arr[k++] = new shpQuad(node1.x, node1.y, node1.z, node2.x,
							node2.y, node2.z, node3.x, node3.y, node3.z,
							node4.x, node4.y, node4.z,
							selected ? j3d.SELECTCOLOR : material.color,
							selected ? j3d.SELECTCOLOR : material.color);
			}
			if (j3d.getSHOW_ID_ELEMENT()) {
				arr[k++] = new shpOrientedText(node4.get_Id(), (node1.x
						+ node2.x + node3.x + node4.x) / 4, (node1.y + node2.y
						+ node3.y + node4.y) / 4,
						(node1.z + node2.z + node3.z + node4.z) / 4, contur);
			}
			if (j3d.getSHOW_ID_LOADS() && load != null)
				arr[k++] = new shpOrientedText(load.name, (node1.x + node2.x
						+ node3.x + node4.x) / 4,
						(node1.y + node2.y + node3.y + node4.y) / 4, (node1.z
								+ node2.z + node3.z + node4.z) / 4, load.color);
		} else if (this.msh_type == Canvas3D.MESH_Solid_Iso_4) {
			arr = new Object[4 + (j3d.getSHOW_ID_ELEMENT() ? 1 : 0)];
			if (material == null) {
				arr[k++] = new shpTriangle(node1.x, node1.y, node1.z, node2.x,
						node2.y, node2.z, node3.x, node3.y, node3.z,
						selected ? j3d.SELECTCOLOR : null, contur);
				arr[k++] = new shpTriangle(node4.x, node4.y, node4.z, node2.x,
						node2.y, node2.z, node3.x, node3.y, node3.z,
						selected ? j3d.SELECTCOLOR : null, contur);
				arr[k++] = new shpTriangle(node1.x, node1.y, node1.z, node4.x,
						node4.y, node4.z, node3.x, node3.y, node3.z,
						selected ? j3d.SELECTCOLOR : null, contur);
				arr[k++] = new shpTriangle(node1.x, node1.y, node1.z, node2.x,
						node2.y, node2.z, node4.x, node4.y, node4.z,
						selected ? j3d.SELECTCOLOR : null, contur);
			} else {
				arr[k++] = new shpTriangle(node1.x, node1.y, node1.z, node2.x,
						node2.y, node2.z, node3.x, node3.y, node3.z,
						selected ? j3d.SELECTCOLOR : material.color,
						selected ? j3d.SELECTCOLOR : material.color);
				arr[k++] = new shpTriangle(node4.x, node4.y, node4.z, node2.x,
						node2.y, node2.z, node3.x, node3.y, node3.z,
						selected ? j3d.SELECTCOLOR : material.color,
						selected ? j3d.SELECTCOLOR : material.color);
				arr[k++] = new shpTriangle(node1.x, node1.y, node1.z, node4.x,
						node4.y, node4.z, node3.x, node3.y, node3.z,
						selected ? j3d.SELECTCOLOR : material.color,
						selected ? j3d.SELECTCOLOR : material.color);
				arr[k++] = new shpTriangle(node1.x, node1.y, node1.z, node2.x,
						node2.y, node2.z, node4.x, node4.y, node4.z,
						selected ? j3d.SELECTCOLOR : material.color,
						selected ? j3d.SELECTCOLOR : material.color);
			}
			if (j3d.getSHOW_ID_ELEMENT()) {
				arr[k++] = new shpOrientedText(node4.get_Id(), (node1.x
						+ node2.x + node3.x + node4.x) / 4, (node1.y + node2.y
						+ node3.y + node4.y) / 4,
						(node1.z + node2.z + node3.z + node4.z) / 4, contur);
			}
		}
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
		return node;
	}

	public void transform3D(Matrix3D t) {
		if (!selected || processed)
			return;
		node1.setSelected(true);
		node2.setSelected(true);
		node3.setSelected(true);
		node4.setSelected(true);
		node1.transform3D(t);
		node2.transform3D(t);
		node3.transform3D(t);
		node4.transform3D(t);
		node1.setSelected(true);
		node2.setSelected(true);
		node3.setSelected(true);
		node4.setSelected(true);
		node1.setProcessed(true);
		node2.setProcessed(true);
		node3.setProcessed(true);
		node4.setProcessed(true);
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
		JPanel p1 = new JPanel(new GridLayout(4, 3));
		JPanel p2;
		if (Canvas3D.MESH_Shell_BT_4 == this.msh_type)
			p2 = new JPanel(new GridLayout(15, 1));
		else
			p2 = new JPanel(new GridLayout(3, 1));
		JPanel p4 = new JPanel(new BorderLayout());
		JPanel p5 = new JPanel(new GridLayout(1, 2));
		JPanel p6 = new JPanel(new BorderLayout());
		JPanel p7 = new JPanel(new GridLayout(1, 2));
		JPanel p8 = new JPanel(new GridLayout(1, 2));
		JPanel p9 = new JPanel(new BorderLayout());
		JPanel p11 = new JPanel(new BorderLayout());
		JPanel p12 = new JPanel(new GridLayout(1, 2));
		JPanel p13 = new JPanel(new GridLayout(1, 2));
		JPanel p14 = new JPanel(new GridLayout(1, 2));
		JPanel p15 = new JPanel(new BorderLayout());
		JPanel p16 = new JPanel(new GridLayout(1, 2));
		JPanel p17 = new JPanel(new GridLayout(1, 2));
		JPanel p18 = new JPanel(new GridLayout(1, 2));
		JPanel p19 = new JPanel(new GridLayout(1, 2));
		node1_tmp = node1;
		node2_tmp = node2;
		node3_tmp = node3;
		node4_tmp = node4;
		String[] ctype = { "", "EDGE", "ADVANCED", "ADVANCED_EDGE", "OFF" };
		String[] ttype = { "", "ON", "OFF" };
		cb_mat = new JComboBox();
		cb_ctype = new JComboBox(ctype);
		cb_load = new JComboBox();
		cb_thin = new JComboBox(ttype);
		cb_hourglass = new JComboBox(ttype);
		cb_load.addItem("");
		for (Enumeration en = PreP.LoadDB.keys(); en.hasMoreElements();)
			cb_load.addItem(en.nextElement());
		if (load != null)
			cb_load.setSelectedItem(load.name);
		if (contact != null)
			cb_ctype.setSelectedItem(contact);
		if (thinning != null)
			cb_thin.setSelectedItem(thinning);
		if (cb_hourglass != null)
			cb_hourglass.setSelectedItem(hourglass);
		tfn1 = new JTextField(node1 != null ? node1.toString() : "", 3);
		tfn2 = new JTextField(node2 != null ? node2.toString() : "", 3);
		tfn3 = new JTextField(node3 != null ? node3.toString() : "", 3);
		tfn4 = new JTextField(node4 != null ? node4.toString() : "", 3);
		tft = new JTextField(thickness + "");
		tffact = new JTextField(factor != null ? factor + "" : "");
		tffrict = new JTextField(friction != null ? friction + "" : "");
		tfnip = new JTextField(NIP != null ? NIP + "" : "");
		tfpip = new JTextField(PIP != null ? PIP + "" : "");
		tfshear = new JTextField(shear_factor != null ? shear_factor + "" : "");
		tfmhc = new JTextField(MHC != null ? MHC + "" : "");
		tfoophc = new JTextField(OOPHC != null ? OOPHC + "" : "");
		tfrhc = new JTextField(RHC != null ? RHC + "" : "");
		tfn1.setEditable(false);
		tfn2.setEditable(false);
		tfn3.setEditable(false);
		tfn4.setEditable(false);
		b_1 = new JButton("<<");
		b_2 = new JButton("<<");
		b_3 = new JButton("<<");
		b_4 = new JButton("<<");
		b_upd = new JButton(add == true ? "Add" : "Update");

		JLabel lb = new JLabel(this.toString());
		lb.setForeground(Color.blue);
		p.add(lb, BorderLayout.NORTH);

		p.add(p1, BorderLayout.CENTER);
		p1.add(new JLabel("Node 1"));
		p1.add(tfn1);
		p1.add(b_1);
		b_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton2_actionPerformed(e);
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

		p1.add(new JLabel("Node 3"));
		p1.add(tfn3);
		p1.add(b_3);
		b_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton4_actionPerformed(e);
			}
		});

		p1.add(new JLabel("Node 4"));
		p1.add(tfn4);
		p1.add(b_4);
		b_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton5_actionPerformed(e);
			}
		});

		p.add(p2, BorderLayout.SOUTH);

		p2.add(p4);
		p4.add(new JLabel("Material  "), BorderLayout.WEST);
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
		if (Canvas3D.MESH_Shell_BT_4 == this.msh_type) {
			p2.add(p5);
			p5.add(new JLabel("Thickness"));
			p5.add(tft);
			tft.addKeyListener(new KeyAdapter() {
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
			p9.add(new JLabel("Contact "), BorderLayout.WEST);
			p9.add(cb_ctype, BorderLayout.CENTER);
			cb_ctype.addKeyListener(new KeyAdapter() {
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
			p7.add(new JLabel("Factor"));
			p7.add(tffact);
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
			p8.add(new JLabel("Friction"));
			p8.add(tffrict);
			tffrict.addKeyListener(new KeyAdapter() {
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
			p11.add(new JLabel("Load "), BorderLayout.WEST);
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
		}
		p2.add(p12);
		p12.add(new JLabel("NIP"));
		p12.add(tfnip);
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
		if (Canvas3D.MESH_Shell_BT_4 == this.msh_type) {
			p2.add(p13);
			p13.add(new JLabel("PIP"));
			p13.add(tfpip);
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
			p14.add(new JLabel("Thinning"));
			p14.add(cb_thin);
			cb_thin.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					checkDefaultKey(e);
				}

				public void keyReleased(KeyEvent e) {
					int c = e.getKeyCode();

					if (c == KeyEvent.VK_ENTER) {
						tfshear.requestFocus();
						tfshear.selectAll();
					}
				}
			});

			p2.add(p19);
			p19.add(new JLabel("Shear factor"));
			p19.add(tfshear);
			tfshear.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					checkDefaultKey(e);
				}

				public void keyReleased(KeyEvent e) {
					int c = e.getKeyCode();

					if (c == KeyEvent.VK_ENTER) {
						cb_hourglass.requestFocus();
					}
				}
			});

			p2.add(p15);
			p15.add(new JLabel("Hourglass Control "), BorderLayout.WEST);
			p15.add(cb_hourglass, BorderLayout.CENTER);
			cb_hourglass.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					checkDefaultKey(e);
				}

				public void keyReleased(KeyEvent e) {
					int c = e.getKeyCode();

					if (c == KeyEvent.VK_ENTER) {
						tfmhc.requestFocus();
						tfmhc.selectAll();
					}
				}
			});

			p2.add(p16);
			p16.add(new JLabel("MHC"));
			p16.add(tfmhc);
			tfmhc.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					checkDefaultKey(e);
				}

				public void keyReleased(KeyEvent e) {
					int c = e.getKeyCode();

					if (c == KeyEvent.VK_ENTER) {
						tfoophc.requestFocus();
						tfoophc.selectAll();
					}
				}
			});

			p2.add(p17);
			p17.add(new JLabel("OOPHC"));
			p17.add(tfoophc);
			tfoophc.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					checkDefaultKey(e);
				}

				public void keyReleased(KeyEvent e) {
					int c = e.getKeyCode();

					if (c == KeyEvent.VK_ENTER) {
						tfrhc.requestFocus();
						tfrhc.selectAll();
					}
				}
			});

			p2.add(p18);
			p18.add(new JLabel("RHC"));
			p18.add(tfrhc);
			tfrhc.addKeyListener(new KeyAdapter() {
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
		}
		p2.add(p6);
		p6.add(b_upd, BorderLayout.CENTER);
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
			_Element4 tmp = this;
			float thickness_tmp = Float.NaN;
			Float factor_tmp = null, friction_tmp = null, shear_factor_tmp = null, MHC_tmp = null, OOPHC_tmp = null, RHC_tmp = null;
			Integer PIP_tmp = null;
			if (node1_tmp == null || node2_tmp == null || node3_tmp == null
					|| node4_tmp == null) {
				error("Node = null!");
				return;
			}
			if (Canvas3D.MESH_Shell_BT_4 == this.msh_type) {
				thickness_tmp = java.lang.Float.parseFloat(tft.getText());
				factor_tmp = tffact.getText().trim().length() == 0 ? null
						: new java.lang.Float(tffact.getText());
				friction_tmp = tffrict.getText().trim().length() == 0 ? null
						: new java.lang.Float(tffrict.getText());
			}
			Integer NIP_tmp = tfnip.getText().trim().length() == 0 ? null
					: new Integer(tfnip.getText());
			if (Canvas3D.MESH_Shell_BT_4 == this.msh_type) {
				PIP_tmp = tfpip.getText().trim().length() == 0 ? null
						: new Integer(tfpip.getText());
				shear_factor_tmp = tfshear.getText().trim().length() == 0 ? null
						: new java.lang.Float(tfshear.getText());
				MHC_tmp = tfmhc.getText().trim().length() == 0 ? null
						: new java.lang.Float(tfmhc.getText());
				OOPHC_tmp = tfoophc.getText().trim().length() == 0 ? null
						: new java.lang.Float(tfoophc.getText());
				RHC_tmp = tfrhc.getText().trim().length() == 0 ? null
						: new java.lang.Float(tfrhc.getText());
			}
			node1 = node1_tmp;
			node2 = node2_tmp;
			node3 = node3_tmp;
			node4 = node4_tmp;
			material = (Material) PreP.MatDB.get(cb_mat.getSelectedItem() + "");
			if (Canvas3D.MESH_Shell_BT_4 == this.msh_type) {
				contact = cb_ctype.getSelectedItem() + "";
				load = (cb_load.getSelectedItem() + "").length() == 0 ? null
						: (Loads) PreP.LoadDB.get(cb_load.getSelectedItem()
								+ "");
				thinning = (cb_thin.getSelectedItem() + "").length() == 0 ? null
						: cb_thin.getSelectedItem() + "";
				hourglass = (cb_hourglass.getSelectedItem() + "").length() == 0 ? null
						: cb_hourglass.getSelectedItem() + "";
				thickness = thickness_tmp;
				factor = factor_tmp;
				friction = friction_tmp;
				PIP = PIP_tmp;
				shear_factor = shear_factor_tmp;
				MHC = MHC_tmp;
				OOPHC = OOPHC_tmp;
				RHC = RHC_tmp;
			}
			NIP = NIP_tmp;
			if (add == true) {
				try {
					tmp = (_Element4) this.clone();
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
				b_4.requestFocus();
			}
		} catch (Exception e1) {
			error(e1);
		}
	}

	void jButton5_actionPerformed(ActionEvent e) {
		try {
			Object obj = J3D.getSelectedObject3D();
			if (obj != null && obj instanceof _Node) {
				node4_tmp = (_Node) obj;
				tfn4.setText(obj.toString());
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
				jButton2_actionPerformed(null);
			else if (b_2.hasFocus())
				jButton3_actionPerformed(null);
			else if (b_3.hasFocus())
				jButton4_actionPerformed(null);
			else if (b_4.hasFocus())
				jButton5_actionPerformed(null);

	}

	public _Node[] get_Nodes() {
		_Node[] arr = { node1, node2, node3, node4 };
		return arr;
	}

	public _Object[] get_Elements() {
		_Object[] arr = { this };
		return arr;
	}

	public _Object duplicate(Canvas3D out, boolean add) {
		_Element4 o = null;
		try {
			o = (_Element4) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		o.node1 = (_Node) node1.duplicate(out, add);
		o.node2 = (_Node) node2.duplicate(out, add);
		o.node3 = (_Node) node3.duplicate(out, add);
		o.node4 = (_Node) node4.duplicate(out, add);

		if (add)
			out.add3D(o);

		return o;
	}

	public Vector3D getCenter() {
		Vector3D s = new Vector3D();

		s.add(node1.getCenter(), node2.getCenter());
		s.add(s, node3.getCenter());
		s.add(s, node4.getCenter());

		s.scale(1.0f / 4.0f);

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
			node4.setSelected(false);
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

	}

}
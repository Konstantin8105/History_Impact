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

import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.tree.*;

import gui.*;

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy
 */

public class _Stl extends _Object implements Serializable {
	private Canvas3D J3D;
	private Vector nodesDB = new Vector();
	private Vector elementsDB = new Vector();
	float xmin, ymin, zmin, xmax, ymax, zmax;
	private Hashtable nodes;
	private Vector elements;
	private Hashtable nodes_tmp = new Hashtable();
	private Vector elements_tmp = new Vector();
	private String file_name;
	private String msh_name = "No mesh";
	private int msh_type = Canvas3D.MESH_Dummy_4;
	private boolean AutoMesh = true;
	public boolean selected = false;
	private Vector arr;
	protected String[] type = { "No mesh", "Solid_Iso_6", "Shell_BT_4",
			"Shell_C0_3", "Contact_Triangle" };
	protected JComboBox cb_msh_name = new JComboBox(type);
	private JTextField tf_mesh_size = new JTextField("");

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(nodesDB);
		out.writeObject(elementsDB);
		out.writeObject(file_name);
		out.writeObject(msh_name);
		out.writeInt(msh_type);
		out.writeBoolean(selected);
		out.writeObject(Id);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		nodesDB = (Vector) in.readObject();
		elementsDB = (Vector) in.readObject();
		file_name = (String) in.readObject();
		msh_name = (String) in.readObject();
		msh_type = in.readInt();
		selected = in.readBoolean();
		Id = (String) in.readObject();
		for (int i = 0; i < nodesDB.size(); i++)
			((_Object) nodesDB.elementAt(i)).set_Id(i + "");
		for (int i = 0; i < elementsDB.size(); i++)
			((_Object) elementsDB.elementAt(i)).set_Id(i + "");
	}

	public void reset(boolean do_mesh) {
	};

	private void mesh() {
		if (cb_msh_name.getSelectedItem().toString().equals("Contact_Triangle"))
			msh_type = Canvas3D.MESH_Contact_Triangle;
		else if (cb_msh_name.getSelectedItem().toString().equals("Shell_C0_3"))
			msh_type = Canvas3D.MESH_Shell_C0_3;
		else if (cb_msh_name.getSelectedItem().toString().equals("Shell_BT_4"))
			msh_type = Canvas3D.MESH_Shell_BT_4;
		else if (cb_msh_name.getSelectedItem().toString().equals("Solid_Iso_6"))
			msh_type = Canvas3D.MESH_Solid_Iso_6;
		else
			msh_type = -1;
		msh_name = cb_msh_name.getSelectedItem().toString();
		try {
			if (AutoMesh) {
				float msize = (xmax - xmin + ymax - ymin + zmax - zmin) / 20.0f;
				mesh(msh_type, msize);
				tf_mesh_size.setText("" + msize);
			} else
				mesh(msh_type, Float.parseFloat(tf_mesh_size.getText()));
		} catch (Exception e) {
			e.printStackTrace();
			message(e);
		}
	}

	public static Frame getFrame(Component component) {
		for (; component != null && !(component instanceof Frame); component = component
				.getParent())
			;
		return (Frame) component;
	}

	public void message(Object obj) {
		Frame f;
		if (J3D instanceof Canvas3DSW)
			f = getFrame((Canvas3DSW) J3D);
		else
			f = getFrame((Canvas3DGL) J3D);
		f.setTitle("" + obj);
	}

	public void mesh(int type, float size) {
		// if(!selected)return;
		msh_type = type;
		System.out.println("\nMesh type - " + msh_name + "\nMesh size - "
				+ size);
		System.out.println("\nBounding box:\n" + xmin + ",\t" + ymin + ",\t"
				+ zmin + "\n" + xmax + ",\t" + ymax + ",\t" + zmax);
		System.out.println("\nRemove old mesh:");
		System.out.println("Nodes - " + nodesDB.size());
		System.out.println("Elements - " + elementsDB.size());
		elementsDB.clear();
		nodesDB.clear();
		nodes_tmp.clear();
		elements_tmp.removeAllElements();
		Vector nodes_join = new Vector();
		if (type != -1) {
			for (float z = zmin; z <= zmax; z += size) {
				message("Scaning intersection in plane  XY=" + z);
				for (float x = xmin; x <= xmax; x += size) {
					for (int i = 0; i < elements.size(); i++) {
						String[] nds = (String[]) elements.elementAt(i);
						_Node nd0 = (_Node) nodes.get(nds[0]);
						_Node nd1 = (_Node) nodes.get(nds[1]);
						_Node nd2 = (_Node) nodes.get(nds[2]);
						float[] p = lib3D.IntersectLineAndTriangle(new _Point(
								nd0.x, nd0.y, nd0.z), new _Point(nd1.x, nd1.y,
								nd1.z), new _Point(nd2.x, nd2.y, nd2.z),
								new _Point(x, ymin, z), new _Point(x, ymax, z));
						if (p != null) {
							nodes_tmp.put((p[0] + "_" + p[1] + "_" + p[2])
									.replaceAll("-0.0_", "0.0_"), new _Node(
									p[0], p[1], p[2]) {
								public String get_Id() {
									return "n." + this_getId() + "."
											+ super.get_Id();
								}
							});
						}
					}
				}
				for (float y = ymin; y <= ymax; y += size) {
					for (int i = 0; i < elements.size(); i++) {
						String[] nds = (String[]) elements.elementAt(i);
						_Node nd0 = (_Node) nodes.get(nds[0]);
						_Node nd1 = (_Node) nodes.get(nds[1]);
						_Node nd2 = (_Node) nodes.get(nds[2]);
						float[] p = lib3D.IntersectLineAndTriangle(new _Point(
								nd0.x, nd0.y, nd0.z), new _Point(nd1.x, nd1.y,
								nd1.z), new _Point(nd2.x, nd2.y, nd2.z),
								new _Point(xmin, y, z), new _Point(xmax, y, z));
						if (p != null) {
							nodes_tmp.put((p[0] + "_" + p[1] + "_" + p[2])
									.replaceAll("-0.0_", "0.0_"), new _Node(
									p[0], p[1], p[2]) {
								public String get_Id() {
									return "n." + this_getId() + "."
											+ super.get_Id();
								}
							});
						}
					}
				}
			}
			for (float x = xmin; x <= xmax; x += size) {
				message("Scaning intersection in plane  XZ=" + x);
				for (float y = ymin; y <= ymax; y += size) {
					for (int i = 0; i < elements.size(); i++) {
						String[] nds = (String[]) elements.elementAt(i);
						_Node nd0 = (_Node) nodes.get(nds[0]);
						_Node nd1 = (_Node) nodes.get(nds[1]);
						_Node nd2 = (_Node) nodes.get(nds[2]);
						float[] p = lib3D.IntersectLineAndTriangle(new _Point(
								nd0.x, nd0.y, nd0.z), new _Point(nd1.x, nd1.y,
								nd1.z), new _Point(nd2.x, nd2.y, nd2.z),
								new _Point(x, y, zmin), new _Point(x, y, zmax));
						if (p != null) {
							nodes_tmp.put((p[0] + "_" + p[1] + "_" + p[2])
									.replaceAll("-0.0_", "0.0_"), new _Node(
									p[0], p[1], p[2]) {
								public String get_Id() {
									return "n." + this_getId() + "."
											+ super.get_Id();
								}
							});
						}
					}
				}
			}
			System.out.println("\nNodes intersection - " + nodes_tmp.size());
			// =======================================================================
			/*
			 * for (Enumeration e = nodes.keys(); e.hasMoreElements();){ Object
			 * key = e.nextElement(); nodes_tmp.put(key,nodes.get(key)); }
			 */
			// =======================================================================
		}
		if (type == Canvas3D.MESH_Solid_Iso_6) {
			for (float z = zmin; z < zmax; z += size) {
				for (float y = ymin; y < ymax; y += size) {
					for (float x = xmin; x < xmax; x += size) {
						_Node[] mnode = new _Node[8];
						mnode[0] = new _Node(x, y, z) {
							public String get_Id() {
								return "n." + this_getId() + "."
										+ super.get_Id();
							}
						};
						mnode[1] = new _Node(x + size, y, z) {
							public String get_Id() {
								return "n." + this_getId() + "."
										+ super.get_Id();
							}
						};
						mnode[2] = new _Node(x + size, y + size, z) {
							public String get_Id() {
								return "n." + this_getId() + "."
										+ super.get_Id();
							}
						};
						mnode[3] = new _Node(x, y + size, z) {
							public String get_Id() {
								return "n." + this_getId() + "."
										+ super.get_Id();
							}
						};
						mnode[4] = new _Node(x, y, z + size) {
							public String get_Id() {
								return "n." + this_getId() + "."
										+ super.get_Id();
							}
						};
						mnode[5] = new _Node(x + size, y, z + size) {
							public String get_Id() {
								return "n." + this_getId() + "."
										+ super.get_Id();
							}
						};
						mnode[6] = new _Node(x + size, y + size, z + size) {
							public String get_Id() {
								return "n." + this_getId() + "."
										+ super.get_Id();
							}
						};
						mnode[7] = new _Node(x, y + size, z + size) {
							public String get_Id() {
								return "n." + this_getId() + "."
										+ super.get_Id();
							}
						};
						String[] snd = new String[8];
						for (int i = 0; i < 8; i++)
							snd[i] = (mnode[i].x + "_" + mnode[i].y + "_" + mnode[i].z)
									.replaceAll("-0.0_", "0.0_");
						boolean[] p_exist = new boolean[8];
						for (int i = 0; i < 8; i++)
							p_exist[i] = contains(mnode[i].x, mnode[i].y,
									mnode[i].z);
						int p_count = 0;
						for (int i = 0; i < p_exist.length; i++)
							if (p_exist[i])
								p_count++;
						Vector nodes_removing = new Vector();
						for (Enumeration e = nodes_tmp.keys(); e
								.hasMoreElements();) {
							_Node n = (_Node) nodes_tmp.get(e.nextElement()
									+ "");
							if (n.x >= x && n.x <= x + size && n.y >= y
									&& n.y <= y + size && n.z >= z
									&& n.z <= z + size) {
								boolean isMaster = false;
								for (int i = 0; i < 8 && !isMaster; i++)
									isMaster = lib3D.compare_points(new _Point(
											n.x, n.y, n.z),
											new _Point(mnode[i].x, mnode[i].y,
													mnode[i].z), 0.001);
								if (!isMaster)
									nodes_removing.addElement(n);
							}
						}
						if (nodes_removing.size() == 0 && p_count != 8)
							continue;
						if (p_count == 8) {
							elements_tmp.addElement(snd);
							for (int i = 0; i < 8; i++)
								nodes_tmp.put(snd[i], mnode[i]);
							// message("Re mesh "+toString()+" ("+msh_name+"): Nodes-"+nodes_tmp.size()+"   Elements-"+elements_tmp.size());
						} else if (p_count == 11) {
							// message("Found 1 Master node and Slave - "+nodes_removing.size());
							// nodes_removing.insertElementAt(null,0);
							for (int i = 0; i < 8; i++) {
								if (p_exist[i]) {
									nodes_removing.insertElementAt(mnode[i], 0);
									break;
								}
							}
							nodes_join.addElement(nodes_removing);

						} else if (p_count == 22) {
							// message("Found 2 Master node and Slave - ");
							System.out
									.println("Found 2 Master node and Slave - "
											+ nodes_removing.size());

						} else if (p_count == 33) {
							// message("Found 3 Master node and Slave - ");
							System.out
									.println("Found 3 Master node and Slave - "
											+ nodes_removing.size());

						} else if (p_count == 44) {
							// message("Found 4 Master node and Slave - ");
							System.out
									.println("Found 4 Master node and Slave - "
											+ nodes_removing.size());

						} else if (p_count == 55) {
							// message("Found 5 Master node and Slave - ");
							System.out
									.println("Found 5 Master node and Slave - "
											+ nodes_removing.size());

						} else if (p_count == 22 || p_count == 33
								|| p_count == 4 || p_count == 5 || p_count == 6
								|| p_count == 7) {
							if (p_count == 7)
								System.out.println("Found " + p_count
										+ " Master node and Slave - "
										+ nodes_removing.size());
							_Node n1 = null, n2 = null;
							if (p_count == 2)
								for (int i = 0; i < 8; i++)
									if (p_exist[i])
										if (n1 == null)
											n1 = new _Node(mnode[i].x,
													mnode[i].y, mnode[i].z);
										else
											n2 = new _Node(mnode[i].x,
													mnode[i].y, mnode[i].z);
							for (int i = 0; i < 8; i++)
								if (!p_exist[i]) {
									Vector nodes_removing_tmp = new Vector();
									nodes_removing_tmp.addElement(null);
									String snd_tmp = null;// snd[i];
									_Node mnode_tmp = null;// mnode[i];
									for (int j = 0; j < nodes_removing.size(); j++) {
										_Node n = (_Node) nodes_removing
												.elementAt(j);
										if (size > lib3D.distance(mnode[i], n)) {
											snd_tmp = (n.x + "_" + n.y + "_" + n.z)
													.replaceAll("-0.0_", "0.0_");
											mnode_tmp = new _Node(n.x, n.y, n.z) {
												public String get_Id() {
													return "n." + this_getId()
															+ "."
															+ super.get_Id();
												}
											};
											nodes_removing_tmp.addElement(n);
										}
									}
									_Node xnode = new _Node(mnode[i].x,
											mnode[i].y, mnode[i].z) {
										public String get_Id() {
											return "n." + this_getId() + "."
													+ super.get_Id();
										}
									};
									snd[i] = snd_tmp;
									mnode[i] = mnode_tmp;

									if (mnode[i] == null) {
										float xs = 0, ys = 0, zs = 0;
										_Node n = null;
										if (p_count == 2) {
											int count = 0;
											for (int j = 0; j < nodes_removing
													.size(); j++) {
												n = (_Node) nodes_removing
														.elementAt(j);
												if (n1.x != n2.x
														&& xnode.x == n.x) {
													xs += n.x;
													ys += n.y;
													zs += n.z;
													count++;
												} else if (n1.y != n2.y
														&& xnode.y == n.y) {
													xs += n.x;
													ys += n.y;
													zs += n.z;
													count++;
												} else if (n1.z != n2.z
														&& xnode.z == n.z) {
													xs += n.x;
													ys += n.y;
													zs += n.z;
													count++;
												}
											}
											xs /= count;
											ys /= count;
											zs /= count;
										} else {
											for (int j = 0; j < nodes_removing
													.size(); j++) {
												n = (_Node) nodes_removing
														.elementAt(j);
												xs += n.x;
												ys += n.y;
												zs += n.z;
											}
											xs /= nodes_removing.size();
											ys /= nodes_removing.size();
											zs /= nodes_removing.size();
										}
										snd[i] = (xs + "_" + ys + "_" + zs)
												.replaceAll("-0.0_", "0.0_");
										mnode[i] = new _Node(xs, ys, zs) {
											public String get_Id() {
												return "n." + this_getId()
														+ "." + super.get_Id();
											}
										};
									}
									nodes_join.addElement(nodes_removing_tmp);
								}
							elements_tmp.addElement(snd);
							for (int i = 0; i < 8; i++)
								nodes_tmp.put(snd[i], mnode[i]);
						} else if (p_count == 77) {
							// message("Found 7 Master node and Slave - "+nodes_removing.size());
							nodes_removing.insertElementAt(null, 0);
							nodes_join.addElement(nodes_removing);
							_Node n = (_Node) nodes_removing.elementAt(1);
							for (int i = 0; i < 8; i++)
								if (!p_exist[i]) {
									snd[i] = (n.x + "_" + n.y + "_" + n.z)
											.replaceAll("-0.0_", "0.0_");
									mnode[i] = new _Node(n.x, n.y, n.z) {
										public String get_Id() {
											return "n." + this_getId() + "."
													+ super.get_Id();
										}
									};
								}
							elements_tmp.addElement(snd);
							for (int i = 0; i < 8; i++)
								nodes_tmp.put(snd[i], mnode[i]);
						}
						message("Re mesh " + toString() + " (" + msh_name
								+ "): Nodes-" + nodes_tmp.size()
								+ "   Elements-" + elements_tmp.size());
					}
				}
			}
			// ========================================================================
			System.out.println("\nOptimize new mesh:");
			System.out.println("Join elements - " + nodes_join.size());
			for (int i = 0; i < nodes_join.size(); i++) {
				message("Optimize new mesh - " + i);
				Vector v = (Vector) nodes_join.elementAt(i);
				float xs = 0, ys = 0, zs = 0;
				_Node n0 = (_Node) v.elementAt(0);
				String[] rn = new String[v.size() - (n0 == null ? 1 : 0)];
				for (int j = 1; j < v.size(); j++) {
					_Node n = (_Node) v.elementAt(j);
					rn[j - 1] = (n.x + "_" + n.y + "_" + n.z).replaceAll(
							"-0.0_", "0.0_");
					xs += n.x;
					ys += n.y;
					zs += n.z;
				}
				xs /= v.size() - 1;
				ys /= v.size() - 1;
				zs /= v.size() - 1;
				if (n0 != null)
					rn[v.size() - 1] = (n0.x + "_" + n0.y + "_" + n0.z)
							.replaceAll("-0.0_", "0.0_");
				nodes_tmp.put(
						(xs + "_" + ys + "_" + zs).replaceAll("-0.0_", "0.0_"),
						new _Node(xs, ys, zs));
				change_nodes(rn,
						(xs + "_" + ys + "_" + zs).replaceAll("-0.0_", "0.0_"));
				// ======================================================================
				for (int j = 0; j < v.size(); j++) {
					_Node n = (_Node) v.elementAt(j);
					if (n != null)
						for (int k = i + 1; k < nodes_join.size(); k++) {
							Vector v_upd = (Vector) nodes_join.elementAt(k);
							for (int h = 0; h < v_upd.size(); h++) {
								_Node nh = (_Node) v_upd.elementAt(h);
								if (nh != null
										&& lib3D.compare_points(new _Point(
												nh.x, nh.y, nh.z), new _Point(
												n.x, n.y, n.z), 0.001)) {
									v_upd.setElementAt(new _Node(xs, ys, zs), h);
								}
							}
						}
				}
				// ======================================================================
			}
			// remove_free_nodes_tmp();
			for (int i = 0; i < elements_tmp.size(); i++) {
				String[] el = (String[]) elements_tmp.elementAt(i);
				for (int j = 0; j < el.length; j++) {
					for (int k = j + 1; k < el.length; k++) {
						if (el[j].equalsIgnoreCase(el[k])) {
							elements_tmp.removeElementAt(i);
							i--;
							System.out.println("Error - " + el.length);
							j = el.length;
							k = j;
						}
					}
				}
			}
			for (int i = 0; i < elements_tmp.size(); i++) {
				String[] el = (String[]) elements_tmp.elementAt(i);
				elementsDB.add(new _Element8((_Node) nodes_tmp.get(el[0]),
						(_Node) nodes_tmp.get(el[1]), (_Node) nodes_tmp
								.get(el[2]), (_Node) nodes_tmp.get(el[3]),
						(_Node) nodes_tmp.get(el[4]), (_Node) nodes_tmp
								.get(el[5]), (_Node) nodes_tmp.get(el[6]),
						(_Node) nodes_tmp.get(el[7])) {
					public String get_Id() {
						return "e." + this_getId() + "." + super.get_Id();
					}
				});
			}
			for (Enumeration e = nodes_tmp.keys(); e.hasMoreElements();)
				nodesDB.add(nodes_tmp.get(e.nextElement() + ""));
			for (int i = 0; i < nodesDB.size(); i++)
				((_Object) nodesDB.elementAt(i)).set_Id(i + "");
			for (int i = 0; i < elementsDB.size(); i++)
				((_Object) elementsDB.elementAt(i)).set_Id(i + "");
		} else if (type == Canvas3D.MESH_Shell_BT_4) {

		} else if (type == Canvas3D.MESH_Contact_Triangle
				|| msh_type == Canvas3D.MESH_Shell_C0_3) {

		} else {
			for (int i = 0; i < elements.size(); i++) {
				String[] el = (String[]) elements.elementAt(i);
				elementsDB.add(new _Element3((_Node) nodes.get(el[0]),
						(_Node) nodes.get(el[1]), (_Node) nodes.get(el[2]),
						Canvas3D.MESH_Shell_C0_3, null, 1) {
					public String get_Id() {
						return "e." + this_getId() + "." + super.get_Id();
					}
				});
			}
			for (Enumeration e = nodes.keys(); e.hasMoreElements();)
				nodesDB.add(nodes.get(e.nextElement() + ""));
			for (int i = 0; i < nodesDB.size(); i++)
				((_Object) nodesDB.elementAt(i)).set_Id(i + "");
			for (int i = 0; i < elementsDB.size(); i++)
				((_Object) elementsDB.elementAt(i)).set_Id(i + "");
		}

		;
		;
		System.out.println("\nNew mesh:");
		System.out.println("Nodes - " + nodesDB.size());
		System.out.println("Elements - " + elementsDB.size());
		message("Re mesh " + toString() + " (" + msh_name + "): Nodes-"
				+ nodesDB.size() + "   Elements-" + elementsDB.size());
	}

	private void change_nodes(String[] rnodes, String nnodes) {
		for (int i = 0; i < elements_tmp.size(); i++) {
			String[] snodes = (String[]) elements_tmp.elementAt(i);
			for (int j = 0; j < snodes.length; j++) {
				for (int k = 0; k < rnodes.length; k++) {
					if (snodes[j].equals(rnodes[k]))
						snodes[j] = nnodes;
				}
			}
			elements_tmp.setElementAt(snodes, i);
		}
		// System.out.println("\n----------------Nodes - "+nodes_tmp.size());
		for (int k = 0; k < rnodes.length; k++) {
			// nodes_tmp.remove(rnodes[k]);
			// System.out.print(rnodes[k]+"\t");
		}
		// System.out.println("\n----------Nodes - "+nodes_tmp.size()+"\t"+nnodes);

	}

	private void remove_free_nodes_tmp() {
		for (Enumeration e = nodes_tmp.keys(); e.hasMoreElements();) {
			Object key = e.nextElement();
			boolean exist = false;
			for (int i = 0; i < elements_tmp.size() && !exist; i++) {
				String[] snodes = (String[]) elements_tmp.elementAt(i);
				for (int j = 0; j < snodes.length && !exist; j++) {
					if (snodes[j].equals(key))
						exist = true;
				}
			}
			if (!exist)
				nodes_tmp.remove(key);
		}
	}

	private boolean contains(float x, float y, float z) {
		Vector v = new Vector();
		boolean xy = false;
		boolean xz = false;
		boolean yz = false;
		for (int i = 0; i < elements.size(); i++) {
			String[] nds = (String[]) elements.elementAt(i);
			_Node nd0 = (_Node) nodes.get(nds[0]);
			_Node nd1 = (_Node) nodes.get(nds[1]);
			_Node nd2 = (_Node) nodes.get(nds[2]);
			float[] p = lib3D.IntersectLineAndTriangle(new _Point(nd0.x, nd0.y,
					nd0.z), new _Point(nd1.x, nd1.y, nd1.z), new _Point(nd2.x,
					nd2.y, nd2.z), new _Point(x, ymin, z), new _Point(x, ymax,
					z));
			if (p != null) {
				v.addElement(new Float(p[1]));
			}
		}
		float[] arr = new float[v.size()];
		// System.out.println(v.size()+"     "+elements.size());
		if (((int) (arr.length / 2)) * 2 == arr.length) {
			for (int i = 0; i < v.size(); i++) {
				arr[i] = ((Float) v.elementAt(i)).floatValue();
			}
			for (int i = 0; i < arr.length; i++) {
				for (int j = i; j < arr.length; j++) {
					if (arr[i] > arr[j]) {
						float a = arr[i];
						arr[i] = arr[j];
						arr[j] = a;
					}
				}
			}
			for (int i = 0; i < arr.length / 2; i++) {
				if (arr[i * 2] <= y && arr[i * 2 + 1] >= y)
					return true;
			}
		} else if (arr.length > 0)
			xz = true;
		v = new Vector();
		for (int i = 0; i < elements.size(); i++) {
			String[] nds = (String[]) elements.elementAt(i);
			_Node nd0 = (_Node) nodes.get(nds[0]);
			_Node nd1 = (_Node) nodes.get(nds[1]);
			_Node nd2 = (_Node) nodes.get(nds[2]);
			float[] p = lib3D.IntersectLineAndTriangle(new _Point(nd0.x, nd0.y,
					nd0.z), new _Point(nd1.x, nd1.y, nd1.z), new _Point(nd2.x,
					nd2.y, nd2.z), new _Point(x, y, zmin), new _Point(x, y,
					zmax));
			if (p != null) {
				v.addElement(new Float(p[2]));
			}
		}
		arr = new float[v.size()];
		if (((int) (arr.length / 2)) * 2 == arr.length) {
			for (int i = 0; i < v.size(); i++) {
				arr[i] = ((Float) v.elementAt(i)).floatValue();
			}
			for (int i = 0; i < arr.length; i++) {
				for (int j = i; j < arr.length; j++) {
					if (arr[i] > arr[j]) {
						float a = arr[i];
						arr[i] = arr[j];
						arr[j] = a;
					}
				}
			}
			for (int i = 0; i < arr.length / 2; i++) {
				if (arr[i * 2] <= z && arr[i * 2 + 1] >= z)
					return true;
			}
		} else if (arr.length > 0)
			xy = true;
		v = new Vector();
		for (int i = 0; i < elements.size(); i++) {
			String[] nds = (String[]) elements.elementAt(i);
			_Node nd0 = (_Node) nodes.get(nds[0]);
			_Node nd1 = (_Node) nodes.get(nds[1]);
			_Node nd2 = (_Node) nodes.get(nds[2]);
			float[] p = lib3D.IntersectLineAndTriangle(new _Point(nd0.x, nd0.y,
					nd0.z), new _Point(nd1.x, nd1.y, nd1.z), new _Point(nd2.x,
					nd2.y, nd2.z), new _Point(xmin, y, z), new _Point(xmax, y,
					z));
			if (p != null) {
				v.addElement(new Float(p[0]));
			}
		}
		arr = new float[v.size()];
		if (((int) (arr.length / 2)) * 2 == arr.length) {
			for (int i = 0; i < v.size(); i++) {
				arr[i] = ((Float) v.elementAt(i)).floatValue();
			}
			for (int i = 0; i < arr.length; i++) {
				for (int j = i; j < arr.length; j++) {
					if (arr[i] > arr[j]) {
						float a = arr[i];
						arr[i] = arr[j];
						arr[j] = a;
					}
				}
			}
			for (int i = 0; i < arr.length / 2; i++) {
				if (arr[i * 2] <= x && arr[i * 2 + 1] >= x)
					return true;
			}
		} else if (arr.length > 0)
			yz = true;
		return (xy && xz && yz);
	}

	public _Stl(String file) {
		file_name = file;
		nodesDB.clear();
		elementsDB.clear();
		nodes = new Hashtable();
		elements = new Vector();
		try {
			RandomAccessFile in = new RandomAccessFile(file, "r");
			byte[] b = new byte[80];
			in.read(b, 0, 80);
			in.close();
			in = new RandomAccessFile(file, "r");
			if (new String(b).indexOf("\n") != -1) {
				// ascii
				String st;
				while ((st = in.readLine()) != null) {
					StringTokenizer stt = new StringTokenizer(st);
					if (stt.nextToken().equalsIgnoreCase("vertex")) {
						float nx, ny, nz;
						String[] el = new String[3];
						nx = Float.parseFloat(stt.nextToken());
						ny = Float.parseFloat(stt.nextToken());
						nz = Float.parseFloat(stt.nextToken());
						el[0] = nx + "_" + ny + "_" + nz;
						nodes.put(el[0], new _Node(nx, ny, nz) {
							public String get_Id() {
								return "n." + this_getId() + "."
										+ super.get_Id();
							}
						});
						st = in.readLine();
						stt = new StringTokenizer(st);
						stt.nextToken();
						nx = Float.parseFloat(stt.nextToken());
						ny = Float.parseFloat(stt.nextToken());
						nz = Float.parseFloat(stt.nextToken());
						el[1] = nx + "_" + ny + "_" + nz;
						nodes.put(el[1], new _Node(nx, ny, nz) {
							public String get_Id() {
								return "n." + this_getId() + "."
										+ super.get_Id();
							}
						});
						st = in.readLine();
						stt = new StringTokenizer(st);
						stt.nextToken();
						nx = Float.parseFloat(stt.nextToken());
						ny = Float.parseFloat(stt.nextToken());
						nz = Float.parseFloat(stt.nextToken());
						el[2] = nx + "_" + ny + "_" + nz;
						nodes.put(el[2], new _Node(nx, ny, nz) {
							public String get_Id() {
								return "n." + this_getId() + "."
										+ super.get_Id();
							}
						});
						elements.addElement(el);
					}
				}
			} else {
				// binary
				in.read(b, 0, 80);
				b = new byte[4];
				in.read(b, 0, 4);
				int facets = arr4byte_to_int(b);
				for (int i = 0; i < facets; i++) {
					String[] el = new String[3];
					float nx, ny, nz;
					in.read(b, 0, 4);
					in.read(b, 0, 4);
					in.read(b, 0, 4);
					in.read(b, 0, 4);
					nx = arr4byte_to_float(b);
					in.read(b, 0, 4);
					ny = arr4byte_to_float(b);
					in.read(b, 0, 4);
					nz = arr4byte_to_float(b);
					el[0] = nx + "_" + ny + "_" + nz;
					nodes.put(el[0], new _Node(nx, ny, nz) {
						public String get_Id() {
							return "n." + this_getId() + "." + super.get_Id();
						}
					});
					in.read(b, 0, 4);
					nx = arr4byte_to_float(b);
					in.read(b, 0, 4);
					ny = arr4byte_to_float(b);
					in.read(b, 0, 4);
					nz = arr4byte_to_float(b);
					el[1] = nx + "_" + ny + "_" + nz;
					nodes.put(el[1], new _Node(nx, ny, nz) {
						public String get_Id() {
							return "n." + this_getId() + "." + super.get_Id();
						}
					});
					in.read(b, 0, 4);
					nx = arr4byte_to_float(b);
					in.read(b, 0, 4);
					ny = arr4byte_to_float(b);
					in.read(b, 0, 4);
					nz = arr4byte_to_float(b);
					el[2] = nx + "_" + ny + "_" + nz;
					nodes.put(el[2], new _Node(nx, ny, nz) {
						public String get_Id() {
							return "n." + this_getId() + "." + super.get_Id();
						}
					});
					elements.addElement(el);
					in.readByte();
					in.readByte();
				}
			}
			in.close();

			xmin = Float.MAX_VALUE;
			ymin = Float.MAX_VALUE;
			zmin = Float.MAX_VALUE;
			xmax = Float.MIN_VALUE;
			ymax = Float.MIN_VALUE;
			zmax = Float.MIN_VALUE;
			for (Enumeration e = nodes.keys(); e.hasMoreElements();) {
				String key = e.nextElement() + "";
				_Node nd = (_Node) nodes.get(key);
				xmin = Math.min(xmin, nd.x);
				ymin = Math.min(ymin, nd.y);
				zmin = Math.min(zmin, nd.z);
				xmax = Math.max(xmax, nd.x);
				ymax = Math.max(ymax, nd.y);
				zmax = Math.max(zmax, nd.z);
			}
			System.out.println("\nBounding box:\n" + xmin + ",\t" + ymin
					+ ",\t" + zmin + "\n" + xmax + ",\t" + ymax + ",\t" + zmax);
			System.out.println("Loaded " + file + "\nFacets - "
					+ elements.size());
			System.out.println("Nodes - " + nodes.size());

			for (int i = 0; i < elements.size(); i++) {
				String[] el = (String[]) elements.elementAt(i);
				elementsDB.add(new _Element3((_Node) nodes.get(el[0]),
						(_Node) nodes.get(el[1]), (_Node) nodes.get(el[2]),
						Canvas3D.MESH_Shell_C0_3, null, 1) {
					public String get_Id() {
						return "e." + this_getId() + "." + super.get_Id();
					}
				});
			}
			for (Enumeration e = nodes.keys(); e.hasMoreElements();)
				nodesDB.add(nodes.get(e.nextElement() + ""));
			for (int i = 0; i < nodesDB.size(); i++)
				((_Object) nodesDB.elementAt(i)).set_Id(i + "");
			for (int i = 0; i < elementsDB.size(); i++)
				((_Object) elementsDB.elementAt(i)).set_Id(i + "");

		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}

	}

	private final int arr4byte_to_int(byte[] b) {
		int res = 0;
		for (int i = 0; i < 4; i++) {
			res |= (b[i] & 0xff) << (i * 8);
		}
		return res;
	}

	private final float arr4byte_to_float(byte[] b) {
		int res = 0;
		for (int i = 0; i < 4; i++) {
			res |= (b[i] & 0xff) << (i * 8);
		}
		return Float.intBitsToFloat(res);
	}

	public Object[] get_Array(Canvas3D j3d) {
		arr = new Vector();
		Object[] shparr;

		// Generate nodes
		for (int i = 0; i < nodesDB.size(); i++) {
			_Node nd = (_Node) nodesDB.elementAt(i);
			shparr = nd.get_Array(j3d);
			for (int j = 0; j < shparr.length; j++)
				arr.add(shparr[j]);
		}

		// Generate elements
		for (int i = 0; i < elementsDB.size(); i++) {
			_Object el = (_Object) elementsDB.elementAt(i);
			shparr = el.get_Array(j3d);
			for (int j = 0; j < shparr.length; j++)
				arr.add(shparr[j]);
		}
		return arr.toArray();
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean sel) {
		selected = sel;
		if (!sel) {
			for (int i = 0; i < nodesDB.size(); i++)
				((_Node) nodesDB.elementAt(i)).setSelected(true);
			for (int i = 0; i < elementsDB.size(); i++)
				((_Object) elementsDB.elementAt(i)).setSelected(true);
		}
	}

	public void deselectRequiredObjects() {
		// Do nothing
	}

	public String toString() {
		return "Stl ID=" + Id;
	}

	public MutableTreeNode get_TreeNode() {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(this);
		DefaultMutableTreeNode n_nodes = new DefaultMutableTreeNode("Nodes");
		DefaultMutableTreeNode n_elements = new DefaultMutableTreeNode(
				"Elements");
		node.add(new DefaultMutableTreeNode(file_name));
		for (int i = 0; i < nodesDB.size(); i++)
			n_nodes.add(((_Object) nodesDB.elementAt(i)).get_TreeNode());
		for (int i = 0; i < elementsDB.size(); i++)
			n_elements.add(((_Object) elementsDB.elementAt(i)).get_TreeNode());
		node.add(n_nodes);
		node.add(n_elements);
		return node;
	}

	public void transform3D(Matrix3D t) {
		if (!selected || processed)
			return;
		for (int i = 0; i < nodesDB.size(); i++) {
			_Node nd = (_Node) nodesDB.elementAt(i);
			if (nd.isSelected()) {
				nd.transform3D(t);
				nd.setSelected(true);
			}
		}
	}

	public JPanel getEditPanel(Canvas3D j3d, PreProcessor pp) {
		J3D = j3d;
		JPanel p = new JPanel(new VerticalFlowLayout());
		JPanel p1 = new JPanel(new BorderLayout());
		JPanel p2 = new JPanel(new BorderLayout());
		JPanel p3 = new JPanel();
		JLabel lb = new JLabel("Edit - " + toString());
		JCheckBox cb = new JCheckBox("Auto Mesh Size", AutoMesh);
		cb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AutoMesh = ((JCheckBox) e.getSource()).isSelected();
			}
		});
		lb.setForeground(Color.blue);
		JButton b_upd = new JButton("Update");
		b_upd.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mesh();
				J3D.tree_reset();
				J3D.view_reset();
			}
		});
		p1.add(new JLabel("Mesh"), BorderLayout.WEST);
		p1.add(cb_msh_name, BorderLayout.CENTER);
		p2.add(new JLabel("Mesh size"), BorderLayout.WEST);
		p2.add(tf_mesh_size, BorderLayout.CENTER);
		p3.add(b_upd);
		p.add(lb);
		p.add(p1);
		p.add(cb);
		p.add(p2);
		p.add(p3);
		return p;
	}

	public _Node[] get_Nodes() {
		_Node[] arr = new _Node[nodesDB.size()];
		for (int i = 0; i < nodesDB.size(); i++)
			arr[i] = (_Node) nodesDB.elementAt(i);
		return arr;
	}

	public _Object[] get_Elements() {
		_Object[] arr = new _Object[elementsDB.size()];
		for (int i = 0; i < elementsDB.size(); i++)
			arr[i] = (_Object) elementsDB.elementAt(i);
		return arr;
	}

	public _Object duplicate(Canvas3D out, boolean add) {
		_Stl o = null;
		try {
			o = (_Stl) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		if (add)
			out.add3D(o);

		return o;
	}

	public boolean isPickPoint(int x, int y, boolean shw, boolean ogl) {
		boolean check = false;
		_Node nd;
		_Object el;

		/*
		 * if (show == shw) for (int i=0; i< arr.length; i++) check =
		 * (((shp)arr[i]).isPickPoint(x,y) == true ? true : check);
		 */
		// Check nodes
		for (int i = 0; i < nodesDB.size(); i++) {
			nd = (_Node) nodesDB.elementAt(i);
			if (nd.isPickPoint(x, y, shw, ogl))
				nd.setSelected(true);
		}

		// Check elements
		for (int i = 0; i < elementsDB.size(); i++) {
			el = (_Object) elementsDB.elementAt(i);
			if (el.isPickPoint(x, y, shw, ogl))
				el.setSelected(true);
		}

		return check;
	}

	public boolean isPickPoint(Rectangle2D r, boolean shw, boolean ogl) {
		boolean check = false;
		_Node nd;
		_Object el;
		/*
		 * if (show == shw) for (int i=0; i< arr.length; i++) check =
		 * (((shp)arr[i]).isPickPoint(r) == true ? true : check);
		 */
		// Check nodes
		for (int i = 0; i < nodesDB.size(); i++) {
			nd = (_Node) nodesDB.elementAt(i);
			if (nd.isPickPoint(r, shw, ogl))
				nd.setSelected(true);
		}

		// Check elements
		for (int i = 0; i < elementsDB.size(); i++) {
			el = (_Object) elementsDB.elementAt(i);
			if (el.isPickPoint(r, shw, ogl))
				el.setSelected(true);
		}

		return check;
	}

	public void setShow(boolean s) {
		_Node nd;
		_Object el;

		// Check nodes
		for (int i = 0; i < nodesDB.size(); i++) {
			nd = (_Node) nodesDB.elementAt(i);
			if (nd.isSelected())
				nd.setShow(s);
		}

		// Check elements
		for (int i = 0; i < elementsDB.size(); i++) {
			el = (_Object) elementsDB.elementAt(i);
			if (el.isSelected())
				el.setShow(s);
		}

		show = s;
	}

	private String this_getId() {
		return this.get_Id();
	}

	public void replaceObjectWith(_Object o, _Object replacement) {
		// Do nothing
	}

}
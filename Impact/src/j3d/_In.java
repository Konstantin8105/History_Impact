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

public class _In extends _Object implements Serializable {
	public Hashtable MatDB = new Hashtable();
	public Hashtable ConstDB = new Hashtable();
	public Hashtable LoadDB = new Hashtable();
	public Vector nodesDB = new Vector();
	public Vector elementsDB = new Vector();
	private String file_name;
	private String file_src;
	public String ContpolsDB = "", TrackersDB = "";
	public boolean selected = false;
	private JTextArea ta_src;

	public void mesh(int type, float size) {
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(nodesDB);
		out.writeObject(elementsDB);
		out.writeObject(file_name);
		out.writeObject(file_src);
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		nodesDB = (Vector) in.readObject();
		elementsDB = (Vector) in.readObject();
		file_name = (String) in.readObject();
		file_src = (String) in.readObject();
		selected = false;
		for (int i = 0; i < nodesDB.size(); i++)
			((_Object) nodesDB.elementAt(i)).set_Id(i + "");
		for (int i = 0; i < elementsDB.size(); i++)
			((_Object) elementsDB.elementAt(i)).set_Id(i + "");
	}

	public _In(String file, Hashtable MDB, Hashtable CDB, Hashtable LDB) {
		file_name = file;
		MatDB = MDB;
		ConstDB = CDB;
		LoadDB = LDB;
		try {
			RandomAccessFile in = new RandomAccessFile(file_name, "r");
			byte[] b = new byte[(int) in.length()];
			in.read(b, 0, (int) in.length());
			file_src = new String(b);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		reset(true);
	}

	public _In(String file) {
		file_name = file;
		try {
			RandomAccessFile in = new RandomAccessFile(file_name, "r");
			byte[] b = new byte[(int) in.length()];
			in.read(b, 0, (int) in.length());
			file_src = new String(b);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		reset(true);
	}

	public void reset(boolean do_mesh) {
		nodesDB.clear();
		elementsDB.clear();
		Hashtable NDB = new Hashtable();
		try {
			file_src = file_src.replaceAll("\t", " ");

			// CONTROLS
			StringTokenizer stt = new StringTokenizer(file_src, "\n");
			while (stt.hasMoreTokens()) {
				String st = stt.nextToken().trim().toUpperCase();
				if (st.startsWith("CONTROLS")) {
					ContpolsDB = st + "\n";
					try {
						while (stt.hasMoreTokens()) {
							st = stt.nextToken().trim().toUpperCase();
							if (st.startsWith("#") || st.length() == 0)
								continue;
							if (st.startsWith("NODES")
									|| st.startsWith("ELEMENTS")
									|| st.startsWith("MATERIALS")
									|| st.startsWith("TRACKERS")
									|| st.indexOf("BOUNDARY_CONDITION") != -1
									|| st.indexOf("RIGID_BODY") != -1
									|| st.indexOf("LOADS") != -1)
								break;
							ContpolsDB += st + "\n";
						}
					} catch (Exception e1) {
					}
				}
			}

			// TRACKERS
			stt = new StringTokenizer(file_src, "\n");
			while (stt.hasMoreTokens()) {
				String st = stt.nextToken().trim().toUpperCase();
				if (st.startsWith("TRACKERS")) {
					TrackersDB = st + "\n";
					try {
						while (stt.hasMoreTokens()) {
							st = stt.nextToken().trim().toUpperCase();
							if (st.startsWith("#") || st.length() == 0)
								continue;
							if (st.startsWith("NODES")
									|| st.startsWith("ELEMENTS")
									|| st.startsWith("CONTROLS")
									|| st.startsWith("MATERIALS")
									|| st.indexOf("BOUNDARY_CONDITION") != -1
									|| st.indexOf("RIGID_BODY") != -1
									|| st.indexOf("LOADS") != -1)
								break;
							TrackersDB += st + "\n";
						}
					} catch (Exception e1) {
					}
				}
			}

			// Material
			stt = new StringTokenizer(file_src, "\n");
			while (stt.hasMoreTokens()) {
				String st = stt.nextToken().trim().toUpperCase();
				if (st.startsWith("MATERIALS")) {
					String mat_type = null;
					if (st.indexOf("ELASTIC") != -1) {
						mat_type = "ELASTIC";
					} else if (st.indexOf("ELASTOPLASTIC") != -1) {
						mat_type = "ELASTOPLASTIC";
					} else if (st.indexOf("THERMOELASTOPLASTIC") != -1) {
						mat_type = "THERMOELASTOPLASTIC";
					} else if (st.indexOf("SPRING") != -1) {
						mat_type = "SPRING";
					}
					try {
						while (stt.hasMoreTokens()) {
							st = stt.nextToken().trim().toUpperCase();
							if (st.startsWith("#") || st.length() == 0
									|| st.startsWith("MATERIALS"))
								continue;
							if (st.startsWith("NODES")
									|| st.startsWith("ELEMENTS")
									|| st.startsWith("CONTROLS")
									|| st.startsWith("TRACKERS")
									|| st.indexOf("BOUNDARY_CONDITION") != -1
									|| st.indexOf("RIGID_BODY") != -1
									|| st.indexOf("LOADS") != -1)
								break;
							StringTokenizer stt1 = new StringTokenizer(st,
									" =\t");
							String mat_name = stt1.nextToken();
							String mat_discription = st.substring(mat_name
									.length());
							Material mat = new Material();
							mat.color = Color.gray;
							mat.description = mat_discription;
							mat.name = mat_name;
							mat.type = mat_type;
							if (!MatDB.containsKey(mat.name))
								MatDB.put(mat.name, mat);
						}
					} catch (Exception e1) {
					}
				}
			}

			// Constraints
			stt = new StringTokenizer(file_src, "\n");
			while (stt.hasMoreTokens()) {
				String st = stt.nextToken().trim().toUpperCase();
				if (st.startsWith("CONSTRAINTS")) {
					String constr_type = null;
					if (st.indexOf("BOUNDARY_CONDITION") != -1) {
						constr_type = "BOUNDARY_CONDITION";
					} else if (st.indexOf("RIGID_BODY") != -1) {
						constr_type = "RIGID_BODY";
					}
					try {
						while (stt.hasMoreTokens()) {
							st = stt.nextToken().trim().toUpperCase();
							if (st.startsWith("#") || st.length() == 0
									|| st.startsWith("CONSTRAINTS"))
								continue;
							if (st.startsWith("NODES")
									|| st.startsWith("ELEMENTS")
									|| st.startsWith("CONTROLS")
									|| st.startsWith("TRACKERS")
									|| st.startsWith("MATERIALS")
									|| st.indexOf("LOADS") != -1)
								break;
							StringTokenizer stt1 = new StringTokenizer(st,
									" =\t");
							String constr_name = stt1.nextToken();
							String constr_discription = st
									.substring(constr_name.length());
							Constraints constr = new Constraints();
							constr.color = Color.red;
							constr.description = constr_discription;
							constr.name = constr_name;
							constr.type = constr_type;
							if (!ConstDB.containsKey(constr.name))
								ConstDB.put(constr.name, constr);
							// System.out.println(constr);
						}
					} catch (Exception e1) {
					}
				}
			}
			// Loads
			stt = new StringTokenizer(file_src, "\n");
			while (stt.hasMoreTokens()) {
				String st = stt.nextToken().trim().toUpperCase();
				if (st.startsWith("LOADS")) {
					try {
						while (stt.hasMoreTokens()) {
							st = stt.nextToken().trim().toUpperCase();
							if (st.startsWith("#") || st.length() == 0
									|| st.startsWith("LOADS"))
								continue;
							if (st.startsWith("NODES")
									|| st.startsWith("ELEMENTS")
									|| st.startsWith("CONTROLS")
									|| st.startsWith("TRACKERS")
									|| st.startsWith("MATERIALS")
									|| st.indexOf("BOUNDARY_CONDITION") != -1
									|| st.indexOf("RIGID_BODY") != -1)
								break;
							StringTokenizer stt1 = new StringTokenizer(st,
									" =\t");
							String load_name = stt1.nextToken();
							String load_discription = st.substring(load_name
									.length());
							Loads load = new Loads();
							load.color = Color.blue;
							load.description = load_discription;
							load.name = load_name;
							if (!LoadDB.containsKey(load.name))
								LoadDB.put(load.name, load);
						}
					} catch (Exception e1) {
					}
				}
			}

			// NODES
			stt = new StringTokenizer(file_src, "\n");
			while (stt.hasMoreTokens()) {
				String st = stt.nextToken().trim().toUpperCase();
				if (st.equals("NODES")) {
					try {
						while (stt.hasMoreTokens()) {
							st = stt.nextToken().trim().toUpperCase();
							if (st.startsWith("#") || st.length() == 0)
								continue;
							StringTokenizer stt1 = new StringTokenizer(st,
									" =XYZ\t");
							String key = stt1.nextToken();
							_Node nd = new _Node(Float.parseFloat(stt1
									.nextToken()), Float.parseFloat(stt1
									.nextToken()), Float.parseFloat(stt1
									.nextToken()));
							/*
							 * { public String get_Id(){ return
							 * (this_getId().length
							 * ()==0?"":"n."+this_getId()+".")+super.get_Id(); }
							 * };
							 */
							nd.set_Id(this.get_Id()
									+ (this.get_Id() != "" ? "." : "") + key);

							if (st.indexOf("CONSTRAINT") != -1) {
								String st1 = st.substring(st
										.indexOf("CONSTRAINT") + 10);
								stt1 = new StringTokenizer(st1, " \t");
								stt1.nextToken();
								nd.constraint = (Constraints) ConstDB.get(stt1
										.nextToken());
							}
							if (st.indexOf("LOAD") != -1) {
								String st1 = st
										.substring(st.indexOf("LOAD") + 4);
								stt1 = new StringTokenizer(st1, " \t");
								stt1.nextToken();
								nd.load = (Loads) LoadDB.get(stt1.nextToken());
							}
							NDB.put(key, nd);
							nodesDB.addElement(nd);
						}
					} catch (Exception e1) {
					}
				}
			}

			// ELEMENTS
			stt = new StringTokenizer(file_src, "\n");
			while (stt.hasMoreTokens()) {
				String st = stt.nextToken().trim().toUpperCase();
				if (st.startsWith("ELEMENTS")) {
					String el_msh_name = null;
					int el_msh_type = -1;
					if (st.indexOf("BEAM_2") != -1) {
						el_msh_name = "Beam_2";
						el_msh_type = Canvas3D.MESH_Beam_2;
					} else if (st.indexOf("CONTACT_TRIANGLE") != -1) {
						el_msh_name = "Contact_Triangle";
						el_msh_type = Canvas3D.MESH_Contact_Triangle;
					} else if (st.indexOf("CONTACT_LINE") != -1) {
						el_msh_name = "Contact_Line";
						el_msh_type = Canvas3D.MESH_Contact_Line;
					} else if (st.indexOf("ROD_2") != -1) {
						el_msh_name = "Rod_2";
						el_msh_type = Canvas3D.MESH_Rod_2;
					} else if (st.indexOf("BEAM_SPRING_2") != -1) {
						el_msh_name = "Beam_Spring_2";
						el_msh_type = Canvas3D.MESH_Beam_Spring_2;
					} else if (st.indexOf("SHELL_BT_4") != -1) {
						el_msh_name = "Shell_BT_4";
						el_msh_type = Canvas3D.MESH_Shell_BT_4;
					} else if (st.indexOf("SHELL_C0_3") != -1) {
						el_msh_name = "Shell_C0_3";
						el_msh_type = Canvas3D.MESH_Shell_C0_3;
					} else if (st.indexOf("SOLID_ISO_4") != -1) {
						el_msh_name = "Solid_Iso_4";
						el_msh_type = Canvas3D.MESH_Solid_Iso_4;
					} else if (st.indexOf("SOLID_ISO_6") != -1) {
						el_msh_name = "Solid_Iso_6";
						el_msh_type = Canvas3D.MESH_Solid_Iso_6;
					}
					// System.out.println(el_msh_name);
					try {
						while (stt.hasMoreTokens()) {
							st = stt.nextToken().trim().toUpperCase();
							if (st.startsWith("#") || st.length() == 0)
								continue;
							if (st.startsWith("ELEMENTS")) {
								el_msh_name = null;
								el_msh_type = -1;
								if (st.indexOf("BEAM_2") != -1) {
									el_msh_name = "Beam_2";
									el_msh_type = Canvas3D.MESH_Beam_2;
								} else if (st.indexOf("CONTACT_TRIANGLE") != -1) {
									el_msh_name = "Contact_Triangle";
									el_msh_type = Canvas3D.MESH_Contact_Triangle;
								} else if (st.indexOf("CONTACT_LINE") != -1) {
									el_msh_name = "Contact_Line";
									el_msh_type = Canvas3D.MESH_Contact_Line;
								} else if (st.indexOf("ROD_2") != -1) {
									el_msh_name = "Rod_2";
									el_msh_type = Canvas3D.MESH_Rod_2;
								} else if (st.indexOf("BEAM_SPRING_2") != -1) {
									el_msh_name = "Beam_Spring_2";
									el_msh_type = Canvas3D.MESH_Beam_Spring_2;
								} else if (st.indexOf("SHELL_BT_4") != -1) {
									el_msh_name = "Shell_BT_4";
									el_msh_type = Canvas3D.MESH_Shell_BT_4;
								} else if (st.indexOf("SHELL_C0_3") != -1) {
									el_msh_name = "Shell_C0_3";
									el_msh_type = Canvas3D.MESH_Shell_C0_3;
								} else if (st.indexOf("SOLID_ISO_4") != -1) {
									el_msh_name = "Solid_Iso_4";
									el_msh_type = Canvas3D.MESH_Solid_Iso_4;
								} else if (st.indexOf("SOLID_ISO_6") != -1) {
									el_msh_name = "Solid_Iso_6";
									el_msh_type = Canvas3D.MESH_Solid_Iso_6;
								}
								// System.out.println(el_msh_name);
								continue;
							}
							if (st.startsWith("NODES")
									|| st.startsWith("CONTROLS")
									|| st.startsWith("TRACKERS")
									|| st.startsWith("MATERIALS")
									|| st.indexOf("BOUNDARY_CONDITION") != -1
									|| st.indexOf("RIGID_BODY") != -1
									|| st.indexOf("LOADS") != -1)
								break;
							// System.out.println(st);
							StringTokenizer stt1 = new StringTokenizer(st, "[]");
							String key = stt1.nextToken();
							key = new StringTokenizer(key, " \t").nextToken();
							String st_nd = stt1.nextToken();
							String st_load = stt1.nextToken();
							StringTokenizer stt2 = new StringTokenizer(st_nd,
									", \t");
							int[] arr = new int[stt2.countTokens()];
							int i = 0;
							while (stt2.hasMoreTokens()) {
								arr[i] = Integer.parseInt(stt2.nextToken());
								i++;
							}
							_Object el = null;
							if (el_msh_type == Canvas3D.MESH_Rod_2) {
								el = new _Element2(
										(_Node) NDB.get(arr[0] + ""),
										(_Node) NDB.get(arr[1] + ""),
										el_msh_type,
										(Material) MatDB.get(getValue(st_load,
												"MATERIAL")),
										java.lang.Float.parseFloat(getValue(
												st_load, "D")));
								/*
								 * { public String get_Id(){ return
								 * (this_getId()
								 * .length()==0?"":"e."+this_getId()
								 * +".")+super.get_Id(); } };
								 */
								try {
									((_Element2) el).factor = new java.lang.Float(
											getValue(st_load, "FACTOR"));
								} catch (Exception e1) {
								}
								try {
									((_Element2) el).contact = getValue(
											st_load, "CONTACT");
								} catch (Exception e1) {
								}
								try {
									((_Element2) el).friction = new java.lang.Float(
											getValue(st_load, "FRICTION"));
								} catch (Exception e1) {
								}
							} else if (el_msh_type == Canvas3D.MESH_Contact_Triangle) {
								Material mat = new Material();
								mat.color = Color.gray;
								mat.name = "null";
								MatDB.put(mat.name, mat);
								el = new _Element3(
										(_Node) NDB.get(arr[0] + ""),
										(_Node) NDB.get(arr[1] + ""),
										(_Node) NDB.get(arr[2] + ""),
										el_msh_type, mat,
										java.lang.Float.parseFloat(getValue(
												st_load, "T")));
								/*
								 * { public String get_Id(){ return
								 * (this_getId()
								 * .length()==0?"":"e."+this_getId()
								 * +".")+super.get_Id(); } };
								 */
							} else if (el_msh_type == Canvas3D.MESH_Contact_Line) {
								Material mat = new Material();
								mat.color = Color.gray;
								mat.name = "null";
								MatDB.put(mat.name, mat);
								el = new _Element2(
										(_Node) NDB.get(arr[0] + ""),
										(_Node) NDB.get(arr[1] + ""),
										el_msh_type, mat,
										java.lang.Float.parseFloat(getValue(
												st_load, "D")));
								/*
								 * { public String get_Id(){ return
								 * (this_getId()
								 * .length()==0?"":"e."+this_getId()
								 * +".")+super.get_Id(); } };
								 */
								try {
									((_Element2) el).factor = new java.lang.Float(
											getValue(st_load, "FACTOR"));
								} catch (Exception e1) {
								}
								try {
									((_Element2) el).contact = getValue(
											st_load, "CONTACT");
								} catch (Exception e1) {
								}
								try {
									((_Element2) el).friction = new java.lang.Float(
											getValue(st_load, "FRICTION"));
								} catch (Exception e1) {
								}
							} else if (el_msh_type == Canvas3D.MESH_Beam_2) {
								el = new _Element2(
										(_Node) NDB.get(arr[0] + ""),
										(_Node) NDB.get(arr[1] + ""),
										el_msh_type,
										(Material) MatDB.get(getValue(st_load,
												"MATERIAL")),
										java.lang.Float.parseFloat(getValue(
												st_load, "D")));
								/*
								 * { public String get_Id(){ return
								 * (this_getId()
								 * .length()==0?"":"e."+this_getId()
								 * +".")+super.get_Id(); } };
								 */
							} else if (el_msh_type == Canvas3D.MESH_Beam_Spring_2) {
								// ---------------------------------
							} else if (el_msh_type == Canvas3D.MESH_Shell_BT_4) {
								el = new _Element4(
										(_Node) NDB.get(arr[0] + ""),
										(_Node) NDB.get(arr[1] + ""),
										(_Node) NDB.get(arr[2] + ""),
										(_Node) NDB.get(arr[3] + ""),
										el_msh_type,
										(Material) MatDB.get(getValue(st_load,
												"MATERIAL")),
										java.lang.Float.parseFloat(getValue(
												st_load, "T")));
								/*
								 * { public String get_Id(){ return
								 * (this_getId()
								 * .length()==0?"":"e."+this_getId()
								 * +".")+super.get_Id(); } };
								 */
								try {
									((_Element4) el).NIP = new Integer(
											getValue(st_load, "NIP"));
								} catch (Exception e1) {
								}
								try {
									((_Element4) el).PIP = new Integer(
											getValue(st_load, "PIP"));
								} catch (Exception e1) {
								}
								try {
									((_Element4) el).shear_factor = new java.lang.Float(
											getValue(st_load, "SHEAR_FACTOR"));
								} catch (Exception e1) {
								}
								try {
									((_Element4) el).hourglass = getValue(
											st_load, "HOURGLASS");
								} catch (Exception e1) {
								}
								try {
									((_Element4) el).MHC = new java.lang.Float(
											getValue(st_load, "MHC"));
								} catch (Exception e1) {
								}
								try {
									((_Element4) el).OOPHC = new java.lang.Float(
											getValue(st_load, "OOPHC"));
								} catch (Exception e1) {
								}
								try {
									((_Element4) el).RHC = new java.lang.Float(
											getValue(st_load, "RHC"));
								} catch (Exception e1) {
								}
								try {
									((_Element4) el).load = (Loads) LoadDB
											.get(getValue(st_load, "LOAD"));
								} catch (Exception e1) {
								}
								try {
									((_Element4) el).factor = new java.lang.Float(
											getValue(st_load, "FACTOR"));
								} catch (Exception e1) {
								}
								try {
									((_Element4) el).contact = getValue(
											st_load, "CONTACT");
								} catch (Exception e1) {
								}
								try {
									((_Element4) el).friction = new java.lang.Float(
											getValue(st_load, "FRICTION"));
								} catch (Exception e1) {
								}
								try {
									((_Element4) el).thinning = getValue(
											st_load, "THINNING");
								} catch (Exception e1) {
								}
							} else if (el_msh_type == Canvas3D.MESH_Solid_Iso_4) {
								el = new _Element4(
										(_Node) NDB.get(arr[0] + ""),
										(_Node) NDB.get(arr[1] + ""),
										(_Node) NDB.get(arr[2] + ""),
										(_Node) NDB.get(arr[3] + ""),
										el_msh_type,
										(Material) MatDB.get(getValue(st_load,
												"MATERIAL")), 0);
								/*
								 * { public String get_Id(){ return
								 * (this_getId()
								 * .length()==0?"":"e."+this_getId()
								 * +".")+super.get_Id(); } };
								 */
								try {
									((_Element4) el).NIP = new Integer(
											getValue(st_load, "NIP"));
								} catch (Exception e1) {
								}
							} else if (el_msh_type == Canvas3D.MESH_Shell_C0_3) {
								el = new _Element3(
										(_Node) NDB.get(arr[0] + ""),
										(_Node) NDB.get(arr[1] + ""),
										(_Node) NDB.get(arr[2] + ""),
										el_msh_type,
										(Material) MatDB.get(getValue(st_load,
												"MATERIAL")),
										java.lang.Float.parseFloat(getValue(
												st_load, "T")));
								/*
								 * { public String get_Id(){ return
								 * (this_getId()
								 * .length()==0?"":"e."+this_getId()
								 * +".")+super.get_Id(); } };
								 */
								try {
									((_Element3) el).NIP = new Integer(
											getValue(st_load, "NIP"));
								} catch (Exception e1) {
								}
								try {
									((_Element3) el).PIP = new Integer(
											getValue(st_load, "PIP"));
								} catch (Exception e1) {
								}
								try {
									((_Element3) el).load = (Loads) LoadDB
											.get(getValue(st_load, "LOAD"));
								} catch (Exception e1) {
								}
								try {
									((_Element3) el).factor = new java.lang.Float(
											getValue(st_load, "FACTOR"));
								} catch (Exception e1) {
								}
								try {
									((_Element3) el).contact = getValue(
											st_load, "CONTACT");
								} catch (Exception e1) {
								}
								try {
									((_Element3) el).friction = new java.lang.Float(
											getValue(st_load, "FRICTION"));
								} catch (Exception e1) {
								}
								try {
									((_Element3) el).thinning = getValue(
											st_load, "THINNING");
								} catch (Exception e1) {
								}
							} else if (el_msh_type == Canvas3D.MESH_Solid_Iso_6) {
								el = new _Element8(
										(_Node) NDB.get(arr[0] + ""),
										(_Node) NDB.get(arr[1] + ""),
										(_Node) NDB.get(arr[2] + ""),
										(_Node) NDB.get(arr[3] + ""),
										(_Node) NDB.get(arr[4] + ""),
										(_Node) NDB.get(arr[5] + ""),
										(_Node) NDB.get(arr[6] + ""),
										(_Node) NDB.get(arr[7] + ""),
										(Material) MatDB.get(getValue(st_load,
												"MATERIAL")));
								/*
								 * { public String get_Id(){ return
								 * (this_getId()
								 * .length()==0?"":"e."+this_getId()
								 * +".")+super.get_Id(); } };
								 */
								try {
									((_Element8) el).NIP = new Integer(
											getValue(st_load, "NIP"));
								} catch (Exception e1) {
								}
							}

							if (el != null) {
								// el.set_Id(key);
								el.set_Id(this.get_Id()
										+ (this.get_Id() != "" ? "." : "")
										+ key);
								elementsDB.addElement(el);
							}
						}
					} catch (Exception e1) {
					}
				}
			}
		} catch (Exception e1) {
		}
	}

	public Object[] get_Array(Canvas3D j3d) {
		Vector v = new Vector();

		for (int i = 0; i < nodesDB.size(); i++) {
			_Node nd = (_Node) nodesDB.elementAt(i);
			Object[] nd_arr = nd.get_Array(j3d);
			for (int j = 0; j < nd_arr.length; j++)
				v.addElement(nd_arr[j]);
		}

		for (int i = 0; i < elementsDB.size(); i++) {
			_Object el = (_Object) elementsDB.elementAt(i);
			Object[] el_arr = el.get_Array(j3d);
			for (int j = 0; j < el_arr.length; j++)
				v.addElement(el_arr[j]);
		}

		return v.toArray();
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean sel) {
		selected = sel;
		if (!sel) {
			for (int i = 0; i < nodesDB.size(); i++)
				((_Node) nodesDB.elementAt(i)).setSelected(false);
			for (int i = 0; i < elementsDB.size(); i++)
				((_Object) elementsDB.elementAt(i)).setSelected(false);
		}
	}

	public void deselectRequiredObjects() {
		// Do nothing
	}

	public String toString() {
		return "In ID=" + Id;
	}

	public MutableTreeNode get_TreeNode() {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(this);
		DefaultMutableTreeNode n_nodes = new DefaultMutableTreeNode("Nodes");
		DefaultMutableTreeNode n_elements = new DefaultMutableTreeNode(
				"Elements");
		node.add(new DefaultMutableTreeNode("Loaded from - " + file_name));
		for (int i = 0; i < nodesDB.size(); i++)
			n_nodes.add(((_Object) nodesDB.elementAt(i)).get_TreeNode());
		node.add(n_nodes);
		for (int i = 0; i < elementsDB.size(); i++)
			n_elements.add(((_Object) elementsDB.elementAt(i)).get_TreeNode());
		node.add(n_elements);
		return node;
	}

	public void transform3D(Matrix3D t) {
		if (!selected || processed)
			return;
		for (int i = 0; i < nodesDB.size(); i++)
			((_Node) nodesDB.elementAt(i)).transform3D(t);
	}

	public void error(Object st) {
		JOptionPane.showMessageDialog(null, "Error: " + st, "Error!",
				JOptionPane.ERROR_MESSAGE);
		if (st instanceof Exception)
			((Exception) st).printStackTrace();
		else
			System.out.println("Error: " + st);
	}

	public JPanel getEditPanel(Canvas3D j3d, PreProcessor pp) {
		this.J3D = j3d;
		this.PreP = pp;

		ta_src = new JTextArea(file_src);
		JPanel p = new JPanel(new BorderLayout());
		JScrollPane sp = new JScrollPane();
		sp.setPreferredSize(new Dimension(200, 250));
		JPanel p1 = new JPanel();
		JButton b_up = new JButton("Update");
		b_up.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				file_src = ta_src.getText();
				reset(true);
				J3D.tree_reset();
				J3D.view_reset();
			}
		});
		JLabel lb = new JLabel("Edit - " + toString());
		lb.setForeground(Color.blue);
		p.add(sp, BorderLayout.CENTER);
		p.add(p1, BorderLayout.SOUTH);
		p1.add(b_up);
		p.add(lb, BorderLayout.NORTH);
		sp.getViewport().add(ta_src);
		p.validate();
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

	private String getValue(String src, String key) {
		key = " " + key + " ";
		if (src.indexOf(key) == -1)
			return null;
		src = src.substring(src.indexOf(key) + key.length());
		StringTokenizer stt = new StringTokenizer(src, " =\t\n");
		return stt.nextToken();
	}

	public _Object duplicate(Canvas3D out, boolean add) {
		_In o = null;
		try {
			o = (_In) this.clone();
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

	private String this_getId() {
		return this.get_Id();
	}

	public void replaceObjectWith(_Object o, _Object replacement) {
		// Do nothing
	}

}
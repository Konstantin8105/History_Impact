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

package gui;

import javax.swing.*;
import java.awt.*;
import j3d.*;
import j3d.utilities.*;

import java.awt.event.*;

import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.*;

import java.text.ParseException;
import java.util.*;

import javax.swing.table.*;

import com.stevesoft.pat.*;

import java.io.*;

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy & Jonas Forssell
 */

public class PreProcessor extends JPanel {
	public static final String ver = "Pre Processor";
	private String path;
	private String CONTROLS_RUN_FROM = "0.0";
	private String CONTROLS_RUN_TO = "1.0";
	private String CONTROLS_RUN_STEP = "";
	private String CONTROLS_PRINT_STEP = "0.01";
	private String CONTROLS_PRINT_TRACKER_STEP = "";
	private String[] graphicsmode = { "WIREFRAME", "SURFACE", "SOLID" };
	public Hashtable MatDB = new Hashtable();
	public Hashtable ConstDB = new Hashtable();
	public Hashtable LoadDB = new Hashtable();
	public Properties ConfDB = new Properties();
	private int id_color = 0;
	private String[] MatHdr = { "Name", "Color", "Type", "Description" };
	private String[] LoadHdr = { "Name", "Color", "Description" };
	private TableModel MatModel = new AbstractTableModel() {
		public int getColumnCount() {
			return 4;
		}

		public int getRowCount() {
			return MatDB.size();
		}

		public Object getValueAt(int row, int col) {
			int i = 0;
			for (Enumeration en = MatDB.keys(); en.hasMoreElements();) {
				String key = en.nextElement() + "";
				Material mat = (Material) MatDB.get(key);
				if (i == row) {
					if (col == 0)
						return key;
					else if (col == 1)
						return "";
					else if (col == 2)
						return mat.type;
					else if (col == 3)
						return mat.description;
					else
						return "";
				}
				i++;
			}
			return "";
		}

		public String getColumnName(int col) {
			return MatHdr[col];
		}

		public boolean isCellEditable(int row, int col) {
			if (col == 3 || col == 1)
				return true;
			else
				return false;
		}

		public void setValueAt(Object aValue, int row, int col) {
			Material mat = (Material) MatDB.get(getValueAt(row, 0));
			if (col == 1)
				mat.color = (Color) aValue;
			if (col == 3)
				mat.description = aValue + "";
		}

		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}
	};
	private TableModel ConstModel = new AbstractTableModel() {
		public int getColumnCount() {
			return 4;
		}

		public int getRowCount() {
			return ConstDB.size();
		}

		public Object getValueAt(int row, int col) {
			int i = 0;
			for (Enumeration en = ConstDB.keys(); en.hasMoreElements();) {
				String key = en.nextElement() + "";
				Constraints con = (Constraints) ConstDB.get(key);
				if (i == row) {
					if (col == 0)
						return key;
					else if (col == 1)
						return "";
					else if (col == 2)
						return con.type;
					else if (col == 3)
						return con.description;
					else
						return "";
				}
				i++;
			}
			return "";
		}

		public String getColumnName(int col) {
			return MatHdr[col];
		}

		public boolean isCellEditable(int row, int col) {
			if (col == 3 || col == 1)
				return true;
			else
				return false;
		}

		public void setValueAt(Object aValue, int row, int col) {
			Constraints con = (Constraints) ConstDB.get(getValueAt(row, 0));
			if (col == 1)
				con.color = (Color) aValue;
			if (col == 3)
				con.description = aValue + "";
		}

		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}
	};
	private TableModel LoadModel = new AbstractTableModel() {
		public int getColumnCount() {
			return 3;
		}

		public int getRowCount() {
			return LoadDB.size();
		}

		public Object getValueAt(int row, int col) {
			int i = 0;
			for (Enumeration en = LoadDB.keys(); en.hasMoreElements();) {
				String key = en.nextElement() + "";
				Loads ld = (Loads) LoadDB.get(key);
				if (i == row) {
					if (col == 0)
						return key;
					else if (col == 1)
						return "";
					else if (col == 2)
						return ld.description;
					else
						return "";
				}
				i++;
			}
			return "";
		}

		public String getColumnName(int col) {
			return LoadHdr[col];
		}

		public boolean isCellEditable(int row, int col) {
			if (col == 2 || col == 1)
				return true;
			else
				return false;
		}

		public void setValueAt(Object aValue, int row, int col) {
			Loads ld = (Loads) LoadDB.get(getValueAt(row, 0));
			if (col == 1)
				ld.color = (Color) aValue;
			if (col == 2)
				ld.description = aValue + "";
		}

		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}
	};

	private JTable MatTable = new JTable(MatModel) {
		public TableCellRenderer getCellRenderer(int row, int column) {
			if (column == 1) {
				DefaultTableCellRenderer render = new DefaultTableCellRenderer();
				render.setBackground(((Material) MatDB.get(getValueAt(row, 0))).color);
				render.setForeground(((Material) MatDB.get(getValueAt(row, 0))).color);
				return render;
			} else
				return super.getCellRenderer(row, column);
		}

		public TableCellEditor getCellEditor(int row, int column) {
			if (column == 1) {
				Color cl = JColorChooser.showDialog(this, "Pick a Color",
						((Material) MatDB.get(getValueAt(row, 0))).color);
				if (cl != null)
					((Material) MatDB.get(getValueAt(row, 0))).color = cl;
			}
			return super.getCellEditor(row, column);
		}
	};
	private JTable ConstTable = new JTable(ConstModel) {
		public TableCellRenderer getCellRenderer(int row, int column) {
			if (column == 1) {
				DefaultTableCellRenderer render = new DefaultTableCellRenderer();
				render.setBackground(((Constraints) ConstDB.get(getValueAt(row,
						0))).color);
				render.setForeground(((Constraints) ConstDB.get(getValueAt(row,
						0))).color);
				return render;
			} else
				return super.getCellRenderer(row, column);
		}

		public TableCellEditor getCellEditor(int row, int column) {
			if (column == 1) {
				Color cl = JColorChooser.showDialog(this, "Pick a Color",
						((Constraints) ConstDB.get(getValueAt(row, 0))).color);
				if (cl != null)
					((Constraints) ConstDB.get(getValueAt(row, 0))).color = cl;
			}
			return super.getCellEditor(row, column);
		}
	};
	private JTable LoadTable = new JTable(LoadModel) {
		public TableCellRenderer getCellRenderer(int row, int column) {
			if (column == 1) {
				DefaultTableCellRenderer render = new DefaultTableCellRenderer();
				render.setBackground(((Loads) LoadDB.get(getValueAt(row, 0))).color);
				render.setForeground(((Loads) LoadDB.get(getValueAt(row, 0))).color);
				return render;
			} else
				return super.getCellRenderer(row, column);
		}

		public TableCellEditor getCellEditor(int row, int column) {
			if (column == 1) {
				Color cl = JColorChooser.showDialog(this, "Pick a Color",
						((Loads) LoadDB.get(getValueAt(row, 0))).color);
				if (cl != null)
					((Loads) LoadDB.get(getValueAt(row, 0))).color = cl;
			}
			return super.getCellEditor(row, column);
		}
	};

	ImageIcon img_new = new ImageIcon(PreProcessor.class.getResource("new.gif"));
	ImageIcon img_open = new ImageIcon(
			PreProcessor.class.getResource("open.gif"));
	ImageIcon img_save = new ImageIcon(
			PreProcessor.class.getResource("save.gif"));
	ImageIcon img_list = new ImageIcon(
			PreProcessor.class.getResource("list.gif"));
	ImageIcon img_add = new ImageIcon(PreProcessor.class.getResource("add.gif"));
	ImageIcon img_groupadd = new ImageIcon(
			PreProcessor.class.getResource("groupadd.png"));

	ImageIcon img_zoomin = new ImageIcon(
			PreProcessor.class.getResource("zoomin.gif"));
	ImageIcon img_zoomout = new ImageIcon(
			PreProcessor.class.getResource("zoomout.gif"));
	ImageIcon img_zoomall = new ImageIcon(
			PreProcessor.class.getResource("zooma.gif"));
	ImageIcon img_center = new ImageIcon(
			PreProcessor.class.getResource("center.png"));
	ImageIcon img_show = new ImageIcon(
			PreProcessor.class.getResource("show.png"));
	ImageIcon img_hide = new ImageIcon(
			PreProcessor.class.getResource("hide.png"));
	ImageIcon img_view_top = new ImageIcon(
			PreProcessor.class.getResource("view_top.gif"));
	ImageIcon img_view_bottom = new ImageIcon(
			PreProcessor.class.getResource("view_bottom.gif"));
	ImageIcon img_view_left = new ImageIcon(
			PreProcessor.class.getResource("view_left.gif"));
	ImageIcon img_view_right = new ImageIcon(
			PreProcessor.class.getResource("view_right.gif"));
	ImageIcon img_view_front = new ImageIcon(
			PreProcessor.class.getResource("view_front.gif"));
	ImageIcon img_view_back = new ImageIcon(
			PreProcessor.class.getResource("view_back.gif"));
	ImageIcon img_view_sw = new ImageIcon(
			PreProcessor.class.getResource("view_sw.gif"));
	ImageIcon img_view_se = new ImageIcon(
			PreProcessor.class.getResource("view_se.gif"));
	ImageIcon img_view_ne = new ImageIcon(
			PreProcessor.class.getResource("view_ne.gif"));
	ImageIcon img_view_nw = new ImageIcon(
			PreProcessor.class.getResource("view_nw.gif"));
	ImageIcon img_config = new ImageIcon(
			PreProcessor.class.getResource("config.gif"));
	ImageIcon img_build = new ImageIcon(
			PreProcessor.class.getResource("build.png"));

	ImageIcon img_point = new ImageIcon(
			PreProcessor.class.getResource("node.png"));
	ImageIcon img_arc = new ImageIcon(PreProcessor.class.getResource("arc.png"));
	ImageIcon img_nurbcurve = new ImageIcon(
			PreProcessor.class.getResource("nurbcurve.gif"));

	ImageIcon img_surf_dir = new ImageIcon(
			PreProcessor.class.getResource("surf_dir.gif"));
	ImageIcon img_surf_rev = new ImageIcon(
			PreProcessor.class.getResource("surf_rev.gif"));
	ImageIcon img_surf_rule = new ImageIcon(
			PreProcessor.class.getResource("surf_rule.gif"));
	ImageIcon img_surf_nurb = new ImageIcon(
			PreProcessor.class.getResource("surf_nurb.gif"));
	ImageIcon img_surf_bil = new ImageIcon(
			PreProcessor.class.getResource("surf_bil.png"));

	ImageIcon img_border = new ImageIcon(
			PreProcessor.class.getResource("border.png"));
	ImageIcon img_intersect = new ImageIcon(
			PreProcessor.class.getResource("intersect.png"));
	ImageIcon img_project = new ImageIcon(
			PreProcessor.class.getResource("project.png"));
	ImageIcon img_break = new ImageIcon(
			PreProcessor.class.getResource("break.png"));

	ImageIcon img_node = new ImageIcon(
			PreProcessor.class.getResource("node.png"));
	ImageIcon img_beam = new ImageIcon(
			PreProcessor.class.getResource("beam.png"));
	ImageIcon img_spring = new ImageIcon(
			PreProcessor.class.getResource("spring.gif"));
	ImageIcon img_triangle = new ImageIcon(
			PreProcessor.class.getResource("triangle.png"));
	ImageIcon img_quad = new ImageIcon(
			PreProcessor.class.getResource("quad.png"));
	ImageIcon img_solid_iso_4 = new ImageIcon(
			PreProcessor.class.getResource("solid_iso_4.gif"));
	ImageIcon img_solid_iso_6 = new ImageIcon(
			PreProcessor.class.getResource("solid_iso_6.gif"));
	ImageIcon img_material = new ImageIcon(
			PreProcessor.class.getResource("material.gif"));
	ImageIcon img_thickness = new ImageIcon(
			PreProcessor.class.getResource("thickness.png"));

	ImageIcon img_erase = new ImageIcon(
			PreProcessor.class.getResource("erase.gif"));
	ImageIcon img_move = new ImageIcon(
			PreProcessor.class.getResource("move.gif"));
	ImageIcon img_rotate = new ImageIcon(
			PreProcessor.class.getResource("rotate.gif"));
	ImageIcon img_scale = new ImageIcon(
			PreProcessor.class.getResource("scale.gif"));
	ImageIcon img_transform = new ImageIcon(
			PreProcessor.class.getResource("transform.gif"));
	ImageIcon img_properties = new ImageIcon(
			PreProcessor.class.getResource("properties.gif"));
	ImageIcon img_duplicate = new ImageIcon(
			PreProcessor.class.getResource("duplicate.png"));

	ImageIcon img_constraints = new ImageIcon(
			PreProcessor.class.getResource("constraints.gif"));
	ImageIcon img_loads = new ImageIcon(
			PreProcessor.class.getResource("loads.gif"));

	ImageIcon img_grid = new ImageIcon(
			PreProcessor.class.getResource("grid.gif"));

	BorderLayout borderLayout1 = new BorderLayout();
	JSplitPane jSplitPane1 = new JSplitPane();
	JPanel PanelInspector = new JPanel();
	private Canvas3D J3D;
	JToolBar jToolBar1 = new JToolBar();
	JButton b_new = new JButton();
	JButton b_open = new JButton();
	JButton b_save = new JButton();
	JButton b_groupadd = new JButton();

	JPanel jPanel1 = new JPanel();
	GridLayout gridLayout1 = new GridLayout();
	JLabel coordinates = new JLabel();
	JToolBar jToolBar2 = new JToolBar();
	JToolBar jToolBar3 = new JToolBar();
	BorderLayout borderLayout2 = new BorderLayout();
	ButtonGroup CommandGroup = new ButtonGroup();
	JPanel jPanel5 = new JPanel();
	JPanel jPanel6 = new JPanel();
	JButton b_zoomin = new JButton();
	JButton b_zoomout = new JButton();
	JButton b_zoomall = new JButton();
	JButton b_center = new JButton();
	JButton b_showhide = new JButton();
	JButton b_viewtop = new JButton();
	JButton b_viewleft = new JButton();
	JButton b_viewsw = new JButton();
	JButton b_viewne = new JButton();
	JButton b_viewse = new JButton();
	JButton b_viewbottom = new JButton();
	JButton b_viewright = new JButton();
	JButton b_viewnw = new JButton();
	JButton b_viewfront = new JButton();
	JButton b_viewback = new JButton();
	JButton b_build = new JButton();

	BorderLayout borderLayout4 = new BorderLayout();
	JTabbedPane jTabbedPane2 = new JTabbedPane();
	JToolBar jToolBar6 = new JToolBar();
	JToolBar jToolBar5 = new JToolBar();
	JButton b_point = new JButton();
	JButton b_erase = new JButton();
	JButton b_duplicate = new JButton();
	JButton b_move = new JButton();
	JButton b_rotate = new JButton();
	JComboBox cb_graphicsmode = new JComboBox(graphicsmode);
	JButton b_del = new JButton();
	JToolBar jToolBar4 = new JToolBar();
	JScrollPane jScrollPane1 = new JScrollPane();
	JTree Tree = new JTree();
	JButton b_scale = new JButton();
	JButton b_node = new JButton();
	JButton b_element2 = new JButton();
	JButton b_element3 = new JButton();
	JButton b_element4 = new JButton();
	private JButton b_config = new JButton();
	private JPanel p_cmd = new JPanel();
	private JButton b_element8 = new JButton();
	private JButton b_surf_dir = new JButton();
	private JButton b_surf_rev = new JButton();
	private JButton b_surf_rule = new JButton();
	private JButton b_nurbcurve = new JButton();
	private JButton b_surf_bil = new JButton();

	private JButton b_border = new JButton();
	private JButton b_intersect = new JButton();
	private JButton b_project = new JButton();
	private JButton b_break = new JButton();

	private JButton b_arc = new JButton();
	private JButton b_material = new JButton();
	private JButton b_constraints = new JButton();
	private JButton b_loads = new JButton();
	JCheckBox cb_node_0 = new JCheckBox("Nodes", true);
	JCheckBox cb_node = new JCheckBox("Nodes label", false);
	JCheckBox cb_element = new JCheckBox("Elements label", false);
	JCheckBox cb_constraints = new JCheckBox("Constraints", true);
	JCheckBox cb_loads = new JCheckBox("Loads", true);
	JCheckBox cb_trackers = new JCheckBox("Trackers", true);
	JCheckBox cb_materials = new JCheckBox("Materials", true);
	private JButton b_setmaterial = new JButton();
	private JButton b_setthickness = new JButton();
	private JButton c_setconstraints = new JButton();
	private JButton b_setloads = new JButton();
	private JButton b_transform = new JButton();
	private JButton b_surf_nurb = new JButton();
	private JButton b_grid = new JButton();
	private JButton b_properties = new JButton();
	private JButton b_element3s = new JButton();

	public _Object edit_panel;
	private boolean block = false;
	private boolean show = true;
	private JButton b_element4s = new JButton();

	public PreProcessor(boolean ogl) {
		try {
			jbInit(ogl);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void exit() {
		save_loads();
		save_materials();
		save_constraints();
		System.out.println("PreProcessor finalize.");
	}

	private void jbInit(boolean openGL) throws Exception {
		J3D = CanvasFactory.getCanvas(this, openGL);
		load_materials();
		load_constraints();
		load_loads();
		load_configuration();
		MatTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		MatTable.getColumnModel().getColumn(3).setPreferredWidth(500);
		ConstTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		ConstTable.getColumnModel().getColumn(3).setPreferredWidth(500);
		LoadTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		LoadTable.getColumnModel().getColumn(2).setPreferredWidth(500);
		this.setLayout(borderLayout1);
		jSplitPane1.setMinimumSize(new Dimension(0, 0));
		jSplitPane1.setPreferredSize(new Dimension(0, 0));
		jSplitPane1.setLastDividerLocation(220);
		jSplitPane1.setOneTouchExpandable(true);
		PanelInspector.setMinimumSize(new Dimension(0, 0));
		PanelInspector.setPreferredSize(new Dimension(0, 0));
		PanelInspector.setLayout(borderLayout2);

		b_new.setToolTipText("New");
		b_new.setIcon(img_new);
		b_new.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				J3D.remove_all();
				p_cmd.removeAll();
				p_cmd.validate();
				PanelInspector.validate();
			}
		});
		b_open.setToolTipText("Open .impact models or add .stl, .in models to current model");
		b_open.setIcon(img_open);
		b_open.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					b_open_actionPerformed(e);
				} catch (Exception e1) {
					error(e1);
				}
			}
		});
		b_save.setToolTipText("Save complete model (.impact) or model for solver (.in)");
		b_save.setIcon(img_save);
		b_save.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_save_actionPerformed(e);
			}
		});
		b_groupadd.setToolTipText("Add a new group");
		b_groupadd.setActionCommand("Groupadd");
		b_groupadd.setIcon(img_groupadd);
		b_groupadd.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command(e);
			}
		});
		b_build.setToolTipText("Rebuild model");
		b_build.setIcon(img_build);
		b_build.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				J3D.rebuild();
			}
		});
		jPanel1.setLayout(gridLayout1);
		gridLayout1.setColumns(3);
		jToolBar1.setFloatable(false);
		b_zoomin.setToolTipText("Zoom in");
		b_zoomin.setIcon(img_zoomin);
		b_zoomin.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_zoomin_actionPerformed(e);
			}
		});
		b_zoomout.setToolTipText("Zoom out");
		b_zoomout.setIcon(img_zoomout);
		b_zoomout.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_zoomout_actionPerformed(e);
			}
		});
		b_zoomall.setToolTipText("Zoom all");
		b_zoomall.setIcon(img_zoomall);
		b_zoomall.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_zoomall_actionPerformed(e);
			}
		});
		b_center.setToolTipText("Set the selected object to center of rotation");
		b_center.setIcon(img_center);
		b_center.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				J3D.setCenterOfRotation();
			}
		});
		b_showhide
				.setToolTipText("Hides or shows the selected objects. Press this without anything selected to switch view");
		b_showhide.setIcon(img_hide);
		b_showhide.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				show = J3D.showhide();

				if (show)
					b_showhide.setIcon(img_hide);
				else
					b_showhide.setIcon(img_show);

			}
		});
		b_viewtop.setToolTipText("Top View");
		b_viewtop.setIcon(img_view_top);
		b_viewtop.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_viewtop_actionPerformed(e);
			}
		});
		b_viewbottom.setToolTipText("Bottom View");
		b_viewbottom.setIcon(img_view_bottom);
		b_viewbottom.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_viewbottom_actionPerformed(e);
			}
		});
		b_viewleft.setToolTipText("Left View");
		b_viewleft.setIcon(img_view_left);
		b_viewleft.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_viewleft_actionPerformed(e);
			}
		});
		b_viewright.setToolTipText("Right View");
		b_viewright.setIcon(img_view_right);
		b_viewright.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_viewright_actionPerformed(e);
			}
		});
		b_viewsw.setToolTipText("SW Isometric View");
		b_viewsw.setIcon(img_view_sw);
		b_viewsw.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_viewsw_actionPerformed(e);
			}
		});
		b_viewse.setToolTipText("SE Isometric View");
		b_viewse.setIcon(img_view_se);
		b_viewse.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_viewse_actionPerformed(e);
			}
		});
		b_viewne.setToolTipText("NE Isometric View");
		b_viewne.setIcon(img_view_ne);
		b_viewne.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_viewne_actionPerformed(e);
			}
		});
		b_viewnw.setToolTipText("NW Isometric View");
		b_viewnw.setIcon(img_view_nw);
		b_viewnw.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_viewnw_actionPerformed(e);
			}
		});
		b_viewfront.setToolTipText("Front View");
		b_viewfront.setIcon(img_view_front);
		b_viewfront.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_viewfront_actionPerformed(e);
			}
		});
		b_viewback.setToolTipText("Back View");
		b_viewback.setIcon(img_view_back);
		b_viewback.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_viewback_actionPerformed(e);
			}
		});
		jPanel5.setLayout(borderLayout4);
		coordinates.setText(" ");
		jToolBar6.setFloatable(false);
		jToolBar5.setFloatable(false);
		b_point.setToolTipText("Point");
		b_point.setActionCommand("Point");
		b_point.setIcon(img_point);
		b_point.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command(e);
			}
		});
		b_erase.setToolTipText("Erase");
		b_erase.setActionCommand("Erase");
		b_erase.setIcon(img_erase);
		b_erase.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetselect();
				J3D.removeSelectedObjects3D();
			}
		});
		b_move.setToolTipText("Move");
		b_move.setActionCommand("Move");
		b_move.setIcon(img_move);
		b_move.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command(e);
			}
		});
		b_rotate.setToolTipText("Rotate");
		b_rotate.setActionCommand("Rotate");
		b_rotate.setIcon(img_rotate);
		b_rotate.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command(e);
			}
		});
		cb_graphicsmode.setPreferredSize(new Dimension(25, 21));
		cb_graphicsmode.setToolTipText("Graphics mode");
		cb_graphicsmode.setSelectedIndex(1);
		J3D.setGRAPHICSMODE(Canvas3D.GRAPHICSMODE_SURFACE);
		cb_graphicsmode.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				cb_graphicsmode_itemStateChanged(e);
			}
		});
		b_del.setToolTipText("Erase");
		b_del.setActionCommand("Erase");
		b_del.setIcon(img_erase);
		b_del.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetselect();
				J3D.removeSelectedObjects3D();
			}
		});
		jToolBar4.setFloatable(false);

		Tree.setModel(J3D.getTree3d());
		Tree.setRootVisible(false);
		Tree.setShowsRootHandles(true);
		Tree.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				showEditPanel(e);
			}
		});
		Tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				if (!block)
					treeSelectionChanged();
			}
		});
		Tree.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();
				if (c == KeyEvent.VK_DELETE) {
					resetselect();
					J3D.removeSelectedObjects3D();
				}
			}
		});

		b_scale.setToolTipText("Scale");
		b_scale.setActionCommand("Scale");
		b_scale.setIcon(img_scale);
		b_scale.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command(e);
			}
		});
		b_node.setToolTipText("Node");
		b_node.setActionCommand("Node");
		b_node.setIcon(img_node);
		b_node.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command(e);
			}
		});
		b_element2.setToolTipText("Elements with 2 nodes");
		b_element2.setActionCommand("Element2");
		b_element2.setIcon(img_beam);
		b_element2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command(e);
			}
		});
		b_element3.setToolTipText("Elements with 3 nodes");
		b_element3.setActionCommand("Element3");
		b_element3.setIcon(img_triangle);
		b_element3.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command(e);
			}
		});
		b_element4.setToolTipText("Elements with 4 nodes");
		b_element4.setActionCommand("Element4");
		b_element4.setIcon(img_quad);
		b_element4.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command(e);
			}
		});
		b_config.setToolTipText("Properties");
		b_config.setIcon(img_config);
		b_config.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_config_actionPerformed(e);
			}
		});
		b_element8.setToolTipText("Elements with 8 nodes");
		b_element8.setActionCommand("Element8");
		b_element8.setIcon(img_solid_iso_6);
		b_element8.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command(e);
			}
		});
		b_surf_dir
				.setToolTipText("Tabulated surface from a path curve and a direction");
		b_surf_dir.setActionCommand("SurfDir");
		b_surf_dir.setIcon(img_surf_dir);
		b_surf_dir.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command(e);
			}
		});
		b_surf_rev.setToolTipText("Revolved surface about a selected axis");
		b_surf_rev.setActionCommand("SurfRev");
		b_surf_rev.setIcon(img_surf_rev);
		b_surf_rev.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command(e);
			}
		});
		b_surf_rule.setToolTipText("Ruled surface between curves");
		b_surf_rule.setActionCommand("SurfRul");
		b_surf_rule.setIcon(img_surf_rule);
		b_surf_rule.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command(e);
			}
		});
		b_surf_bil.setToolTipText("Bilinear surface between edge points");
		b_surf_bil.setActionCommand("SurfBil");
		b_surf_bil.setIcon(img_surf_bil);
		b_surf_bil.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command(e);
			}
		});

		b_border.setToolTipText("Extract border curves from surfaces or end points from curves. Select object and then press this button");
		b_border.setActionCommand("Border");
		b_border.setIcon(img_border);
		b_border.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command(e);
			}
		});

		b_intersect
				.setToolTipText("Intersect two objects and generate resulting point or curve. Just select two objects and then press this button");
		b_intersect.setActionCommand("Intersect");
		b_intersect.setIcon(img_intersect);
		b_intersect.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command(e);
			}
		});

		b_project
				.setToolTipText("Project one object onto another object generating a resulting point or curve. Just select two objects and then press this button");
		b_project.setActionCommand("Project");
		b_project.setIcon(img_project);
		b_project.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command(e);
			}
		});

		b_break.setToolTipText("Break a curve using another object. Just select the curve and object and then press this button");
		b_break.setActionCommand("Break");
		b_break.setIcon(img_break);
		b_break.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command(e);
			}
		});

		b_arc.setToolTipText("Arc");
		b_arc.setActionCommand("Arc");
		b_arc.setIcon(img_arc);
		b_arc.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command(e);
			}
		});
		b_nurbcurve.setToolTipText("Spline");
		b_nurbcurve.setActionCommand("Spline");
		b_nurbcurve.setIcon(img_nurbcurve);
		b_nurbcurve.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command(e);
			}
		});
		b_material.setToolTipText("Materials");
		b_material.setActionCommand("Material");
		b_material.setIcon(img_material);
		b_material.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_material_actionPerformed(e);
			}
		});
		b_constraints.setToolTipText("Constraints");
		b_constraints.setActionCommand("Constraints");
		b_constraints.setIcon(img_constraints);
		b_constraints.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_constraints_actionPerformed(e);
			}
		});
		b_loads.setToolTipText("Loads");
		b_loads.setActionCommand("Loads");
		b_loads.setIcon(img_loads);
		b_loads.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_loads_actionPerformed(e);
			}
		});
		b_setmaterial.setToolTipText("Material for ...");
		b_setmaterial.setActionCommand("SetMaterial");
		b_setmaterial.setIcon(img_material);
		b_setmaterial.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command(e);
			}
		});
		b_setthickness.setToolTipText("Set thickness for ...");
		b_setthickness.setActionCommand("SetThickness");
		b_setthickness.setIcon(img_thickness);
		b_setthickness.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command(e);
			}
		});
		c_setconstraints.setToolTipText("Constraints for ...");
		c_setconstraints.setActionCommand("SetConstraints");
		c_setconstraints.setIcon(img_constraints);
		c_setconstraints.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command(e);
			}
		});
		b_setloads.setToolTipText("Loads for ...");
		b_setloads.setActionCommand("SetLoads");
		b_setloads.setIcon(img_loads);
		b_setloads.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command(e);
			}
		});
		b_transform.setToolTipText("Transform 4X4");
		b_transform.setActionCommand("Transform");
		b_transform.setIcon(img_transform);
		b_transform.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command(e);
			}
		});
		b_surf_nurb.setIcon(img_surf_nurb);
		b_surf_nurb.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command(e);
			}
		});
		b_surf_nurb.setToolTipText("Surface from edge curves");
		b_surf_nurb.setActionCommand("SurfNurb");
		b_grid.setToolTipText("Grid");
		b_grid.setActionCommand("Grid");
		b_grid.setIcon(img_grid);
		b_grid.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command(e);
			}
		});
		b_properties.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command(e);
			}
		});
		b_properties.setIcon(img_properties);
		b_properties.setActionCommand("Properties");
		b_properties.setToolTipText("Properties");
		b_duplicate.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				J3D.duplicate();
			}
		});
		b_duplicate.setIcon(img_duplicate);
		b_duplicate.setActionCommand("Duplicate");
		b_duplicate.setToolTipText("Duplicate");
		b_element3s.setToolTipText("Element Spring");
		b_element3s.setActionCommand("Element3s");
		b_element3s.setIcon(img_spring);
		b_element4s.setToolTipText("Solid elements with 4 nodes");
		b_element4s.setActionCommand("Element4s");
		b_element4s.setIcon(img_solid_iso_4);
		b_element4s.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				command(e);
			}
		});

		this.add(jSplitPane1, BorderLayout.CENTER);
		jSplitPane1.add(PanelInspector, JSplitPane.TOP);
		PanelInspector.add(jToolBar4, BorderLayout.NORTH);
		jToolBar4.add(cb_graphicsmode);
		PanelInspector.add(jScrollPane1, BorderLayout.CENTER);
		PanelInspector.add(p_cmd, BorderLayout.SOUTH);
		jScrollPane1.getViewport().add(Tree);

		jPanel6.setLayout(new BorderLayout());
		if (J3D instanceof Canvas3DGL)
			jPanel6.add((Canvas3DGL) J3D, BorderLayout.CENTER);
		else
			jPanel6.add((JPanel) J3D, BorderLayout.CENTER);

		jPanel5.add(jPanel6, BorderLayout.CENTER);

		jPanel5.add(jTabbedPane2, BorderLayout.NORTH);

		jTabbedPane2.add(jToolBar5, "Draw");
		jToolBar5.add(b_point, null);
		jToolBar5.add(b_nurbcurve, null);
		jToolBar5.add(b_arc, null);
		jToolBar5.addSeparator();
		jToolBar5.add(b_surf_dir, null);
		jToolBar5.add(b_surf_rev, null);
		jToolBar5.add(b_surf_rule, null);
		jToolBar5.add(b_surf_nurb, null);
		jToolBar5.add(b_surf_bil, null);
		jToolBar5.addSeparator();
		jToolBar5.add(b_border, null);
		jToolBar5.add(b_intersect, null);
		jToolBar5.add(b_project, null);
		jToolBar5.add(b_break, null);
		jToolBar5.addSeparator();
		jToolBar5.add(b_node, null);
		jToolBar5.add(b_element2, null);
		jToolBar5.add(b_element3s, null);
		jToolBar5.add(b_element3, null);
		jToolBar5.add(b_element4, null);
		jToolBar5.add(b_element4s, null);
		jToolBar5.add(b_element8, null);

		jTabbedPane2.add(jToolBar6, "Modify");
		jToolBar6.add(b_duplicate, null);
		jToolBar6.add(b_erase, null);
		jToolBar6.add(b_move, null);
		jToolBar6.add(b_rotate, null);
		jToolBar6.add(b_scale, null);
		jToolBar6.add(b_transform, null);
		jToolBar6.addSeparator();
		jToolBar6.add(b_setmaterial, null);
		jToolBar6.add(b_setthickness, null);
		jToolBar6.add(c_setconstraints, null);
		jToolBar6.add(b_setloads, null);
		jToolBar6.addSeparator();
		jToolBar6.add(b_grid, null);
		jSplitPane1.add(jPanel5, JSplitPane.BOTTOM);

		this.add(jToolBar1, BorderLayout.NORTH);
		jToolBar1.add(b_new, null);
		jToolBar1.add(b_open, null);
		jToolBar1.add(b_save, null);
		jToolBar1.addSeparator();
		jToolBar1.add(b_groupadd, null);
		jToolBar1.add(b_build, null);
		jToolBar1.addSeparator();
		jToolBar1.add(b_showhide, null);
		jToolBar1.add(b_zoomin, null);
		jToolBar1.add(b_zoomout, null);
		jToolBar1.add(b_zoomall, null);
		jToolBar1.add(b_center, null);
		jToolBar1.addSeparator();
		jToolBar1.add(b_viewtop, null);
		jToolBar1.add(b_viewbottom, null);
		jToolBar1.add(b_viewleft, null);
		jToolBar1.add(b_viewright, null);
		jToolBar1.add(b_viewfront, null);
		jToolBar1.add(b_viewback, null);
		jToolBar1.add(b_viewsw, null);
		jToolBar1.add(b_viewse, null);
		jToolBar1.add(b_viewne, null);
		jToolBar1.add(b_viewnw, null);
		jToolBar1.addSeparator();
		jToolBar1.add(b_config, null);
		jToolBar1.addSeparator();
		jToolBar1.add(b_material, null);
		jToolBar1.add(b_constraints, null);
		jToolBar1.add(b_loads, null);

		this.add(jPanel1, BorderLayout.SOUTH);
		jPanel1.add(coordinates, null);
		jPanel1.add(jToolBar3, null);
		jPanel1.add(jToolBar2, null);
		jSplitPane1.setDividerLocation(220);
		// J3D.coodinates=coordinates;
	}

	public static Frame getFrame(Component component) {
		for (; component != null && !(component instanceof Frame); component = component
				.getParent())
			;
		return (Frame) component;
	}

	public void setVisible(boolean v) {
		super.setVisible(v);

		if (J3D instanceof Canvas3DGL)
			J3D.setVisible(v);
	}

	public void error(Object st) {
		JOptionPane.showMessageDialog(this, "Error: " + st, "Error!",
				JOptionPane.ERROR_MESSAGE);
		if (st instanceof Exception)
			((Exception) st).printStackTrace();
		else
			System.out.println("Error: " + st);
	}

	void cb_graphicsmode_itemStateChanged(ItemEvent e) {
		J3D.setGRAPHICSMODE((byte) cb_graphicsmode.getSelectedIndex());
		if (cb_graphicsmode.getSelectedIndex() == 0)
			cb_materials.setSelected(false);
		else
			cb_materials.setSelected(true);
		J3D.repaint();
	}

	void b_zoomin_actionPerformed(ActionEvent e) {
		J3D.view_scale(1.2f);
	}

	void b_zoomout_actionPerformed(ActionEvent e) {
		J3D.view_scale(0.8f);
	}

	void b_zoomall_actionPerformed(ActionEvent e) {
		J3D.view_all();
	}

	void b_viewtop_actionPerformed(ActionEvent e) {
		J3D.view_top();
	}

	void b_viewsw_actionPerformed(ActionEvent e) {
		J3D.view_sw();
	}

	void b_viewse_actionPerformed(ActionEvent e) {
		J3D.view_se();
	}

	void b_viewne_actionPerformed(ActionEvent e) {
		J3D.view_ne();
	}

	void b_viewnw_actionPerformed(ActionEvent e) {
		J3D.view_nw();
	}

	void b_viewbottom_actionPerformed(ActionEvent e) {
		J3D.view_bottom();
	}

	void b_viewleft_actionPerformed(ActionEvent e) {
		J3D.view_left();
	}

	void b_viewright_actionPerformed(ActionEvent e) {
		J3D.view_right();
	}

	void b_viewfront_actionPerformed(ActionEvent e) {
		J3D.view_front();
	}

	void b_viewback_actionPerformed(ActionEvent e) {
		J3D.view_back();
	}

	void command(ActionEvent e) {
		p_cmd.removeAll();
		int w = p_cmd.getWidth();
		String st_cmd = "" + e.getActionCommand();
		if (st_cmd.equalsIgnoreCase("GROUPADD")) {
			_Group g = new _GroupUserDefined(true, true);
			p_cmd.add(g.getEditPanel(J3D, this), BorderLayout.CENTER);
			g.requestFocus();
		} else if (st_cmd.equalsIgnoreCase("POINT")) {
			_Point t = new _Point(true);
			p_cmd.add(t.getEditPanel(J3D, this), BorderLayout.CENTER);
			t.requestFocus();
		} else if (st_cmd.equalsIgnoreCase("NODE")) {
			edit_panel = new _Node(true);
			p_cmd.add(edit_panel.getEditPanel(J3D, this), BorderLayout.CENTER);
			edit_panel.requestFocus();
		} else if (st_cmd.equalsIgnoreCase("ELEMENT8")) {
			edit_panel = new _Element8(true);
			p_cmd.add(edit_panel.getEditPanel(J3D, this), BorderLayout.CENTER);
			edit_panel.requestFocus();
		}

		else if (st_cmd.equalsIgnoreCase("ELEMENT4S")) {
			edit_panel = new _Element4(true, Canvas3D.MESH_Solid_Iso_4);
			p_cmd.add(edit_panel.getEditPanel(J3D, this), BorderLayout.CENTER);
			edit_panel.requestFocus();
		} else if (st_cmd.equalsIgnoreCase("ELEMENT4")) {
			edit_panel = new _Element4(true, Canvas3D.MESH_Shell_BT_4);
			p_cmd.add(edit_panel.getEditPanel(J3D, this), BorderLayout.CENTER);
			edit_panel.requestFocus();
		}

		else if (st_cmd.equalsIgnoreCase("ELEMENT3")) {
			edit_panel = new _Element3(true);
			p_cmd.add(edit_panel.getEditPanel(J3D, this), BorderLayout.CENTER);
			edit_panel.requestFocus();
		} else if (st_cmd.equalsIgnoreCase("ELEMENT2")) {
			edit_panel = new _Element2(true);
			p_cmd.add(edit_panel.getEditPanel(J3D, this), BorderLayout.CENTER);
			edit_panel.requestFocus();
		} else if (st_cmd.equalsIgnoreCase("MOVE"))
			p_cmd.add(new PreProcessor_Move(J3D, this), BorderLayout.CENTER);
		else if (st_cmd.equalsIgnoreCase("ROTATE"))
			p_cmd.add(new PreProcessor_Rotate(J3D, this), BorderLayout.CENTER);
		else if (st_cmd.equalsIgnoreCase("SCALE"))
			p_cmd.add(new PreProcessor_Scale(J3D, this), BorderLayout.CENTER);
		else if (st_cmd.equalsIgnoreCase("TRANSFORM"))
			p_cmd.add(new PreProcessor_Transform(J3D), BorderLayout.CENTER);
		else if (st_cmd.equalsIgnoreCase("ARC")) {
			edit_panel = new _Arc(true, J3D);
			p_cmd.add(edit_panel.getEditPanel(J3D, this), BorderLayout.CENTER);
			edit_panel.requestFocus();
		} else if (st_cmd.equalsIgnoreCase("SPLINE")) {
			edit_panel = new _Spline(true, J3D);
			p_cmd.add(edit_panel.getEditPanel(J3D, this), BorderLayout.CENTER);
			edit_panel.requestFocus();
		} else if (st_cmd.equalsIgnoreCase("SURFDIR")) {
			edit_panel = new _SurfDir(true, J3D);
			p_cmd.add(edit_panel.getEditPanel(J3D, this), BorderLayout.CENTER);
			edit_panel.requestFocus();
		} else if (st_cmd.equalsIgnoreCase("SURFREV")) {
			edit_panel = new _SurfRev(true, J3D);
			p_cmd.add(edit_panel.getEditPanel(J3D, this), BorderLayout.CENTER);
			edit_panel.requestFocus();
		} else if (st_cmd.equalsIgnoreCase("SURFRUL")) {
			edit_panel = new _SurfRul(true, J3D);
			p_cmd.add(edit_panel.getEditPanel(J3D, this), BorderLayout.CENTER);
			edit_panel.requestFocus();
		} else if (st_cmd.equalsIgnoreCase("SURFNURB")) {
			edit_panel = new _SurfFill(true, J3D);
			p_cmd.add(edit_panel.getEditPanel(J3D, this), BorderLayout.CENTER);
			edit_panel.requestFocus();
		} else if (st_cmd.equalsIgnoreCase("SURFBIL")) {
			edit_panel = new _SurfBil(true, J3D);
			p_cmd.add(edit_panel.getEditPanel(J3D, this), BorderLayout.CENTER);
			edit_panel.requestFocus();
		} else if (st_cmd.equalsIgnoreCase("BORDER")) {
			J3D.addBorderObjects();
		} else if (st_cmd.equalsIgnoreCase("INTERSECT")) {
			try {
				J3D.intersectObjects();
			} catch (IllegalStateException ise) {
				error(ise);
			}
		} else if (st_cmd.equalsIgnoreCase("PROJECT")) {
			try {
				J3D.projectObjects();
			} catch (IllegalStateException ise) {
				error(ise);
			}
		} else if (st_cmd.equalsIgnoreCase("BREAK")) {
			try {
				J3D.breakObjects();
			} catch (IllegalStateException ise) {
				error(ise);
			}
		} else if (st_cmd.equalsIgnoreCase("PROPERTIES"))
			showEditPanel();
		else if (st_cmd.equalsIgnoreCase("SETCONSTRAINTS"))
			p_cmd.add(new PreProcessor_SetConstraints(J3D, this),
					BorderLayout.CENTER);
		else if (st_cmd.equalsIgnoreCase("SETLOADS"))
			p_cmd.add(new PreProcessor_SetLoads(J3D, this), BorderLayout.CENTER);
		else if (st_cmd.equalsIgnoreCase("SETMATERIAL"))
			p_cmd.add(new PreProcessor_SetMaterial(J3D, this),
					BorderLayout.CENTER);
		else if (st_cmd.equalsIgnoreCase("SETTHICKNESS"))
			p_cmd.add(new PreProcessor_SetThickness(J3D, this),
					BorderLayout.CENTER);
		else if (st_cmd.equalsIgnoreCase("GRID"))
			p_cmd.add(new PreProcessor_Grid(J3D), BorderLayout.CENTER);
		p_cmd.validate();
		PanelInspector.validate();
	}

	void b_save_actionPerformed(ActionEvent e) {

		FileFilter f1 = new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory())
					return true;
				return f.getName().toLowerCase().endsWith(".impact");
			}

			public String getDescription() {
				return "Complete preprocessor model (.impact)";
			}
		};

		FileFilter f2 = new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory())
					return true;
				return f.getName().toLowerCase().endsWith(".in");
			}

			public String getDescription() {
				return "Indata model for solver (.in)";
			}
		};

		try {
			JFileChooser fd = new JFileChooser(ConfDB.getProperty("Filepath"));
			fd.setSize(350, 200);
			fd.addChoosableFileFilter(f1);
			fd.addChoosableFileFilter(f2);

			int choise = fd.showSaveDialog(this);

			if (choise == JFileChooser.APPROVE_OPTION
					&& fd.getSelectedFile() != null) {
				String st = fd.getSelectedFile().getAbsolutePath();

				if (fd.getFileFilter().equals(f1)) {
					if (!st.toLowerCase().endsWith(".impact"))
						st += ".impact";

					File f = new File(st);
					f.delete();
					try {
						FileOutputStream ostream = new FileOutputStream(st);
						ObjectOutputStream p = new ObjectOutputStream(ostream);
						p.writeObject(MatDB);
						p.writeObject(ConstDB);
						p.writeObject(LoadDB);
						p.writeObject(J3D.getVMatrix3D());
						_Object[] arr_obj = J3D.getAllObjects3D();
						for (int i = 0; i < arr_obj.length; i++) {
							p.writeObject(arr_obj[i]);
						}
						p.flush();
						ostream.flush();
						ostream.close();
					} catch (Exception ie) {
						error(ie);
					}
					path = st;
				}

				else if (fd.getFileFilter().equals(f2)) {
					saveSolverModel(st);
				}
			}

			// Update configuration file with path
			if (fd.getSelectedFile() != null) {
				ConfDB.setProperty("Filepath", fd.getSelectedFile()
						.getAbsolutePath());
				save_configuration();
				header(fd.getSelectedFile().getAbsolutePath());
			}

		} catch (Exception e1) {
			error(e1);
		}
	}

	void b_open_actionPerformed(ActionEvent e) throws IOException {
		FileFilter f1 = new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory())
					return true;
				return f.getName().toLowerCase().endsWith(".stl");
			}

			public String getDescription() {
				return "Add STL surface (.stl)";
			}
		};

		FileFilter f2 = new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory())
					return true;
				if (f.getName().toLowerCase().endsWith(".in"))
					return true;
				return false;
			}

			public String getDescription() {
				return "Add impact model (.in) to current model";
			}
		};

		FileFilter f3 = new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory())
					return true;
				if (f.getName().toLowerCase().endsWith(".nas")
						|| f.getName().toLowerCase().endsWith(".nastran")
						|| f.getName().toLowerCase().endsWith(".bdf"))
					return true;
				return false;
			}

			public String getDescription() {
				return "Add nastran model (.nas, .nastran, .bdf) to current model";
			}
		};

		FileFilter f4 = new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory())
					return true;
				if (f.getName().toLowerCase().endsWith(".msh"))
					return true;
				return false;
			}

			public String getDescription() {
				return "Add Gmsh model (.msh) to current model";
			}
		};

		FileFilter f5 = new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory())
					return true;
				if (f.getName().toLowerCase().endsWith(".unv"))
					return true;
				return false;
			}

			public String getDescription() {
				return "Add UNV model (.unv) to current model";
			}
		};

		JFileChooser fd = new JFileChooser(ConfDB.getProperty("Filepath"));
		fd.setSize(350, 200);
		fd.addChoosableFileFilter(f1);
		fd.addChoosableFileFilter(f2);
		fd.addChoosableFileFilter(f3);
		fd.addChoosableFileFilter(f4);
		fd.addChoosableFileFilter(f5);

		fd.setFileFilter(f2);

		try {

			int choise = fd.showOpenDialog(this);

			if (choise == JFileChooser.APPROVE_OPTION
					&& fd.getSelectedFile() != null) {
				String st = fd.getSelectedFile().getAbsolutePath();

				if (fd.getFileFilter().equals(f1)) {
					edit_panel = new _Stl(st);
					J3D.add3D(edit_panel);
					p_cmd.removeAll();
					p_cmd.add(edit_panel.getEditPanel(J3D, this),
							BorderLayout.CENTER);
					edit_panel.requestFocus();
					PanelInspector.validate();
					J3D.tree_reset();
					J3D.view_reset();
					J3D.view_all();
				} else if (fd.getFileFilter().equals(f2)) {
					this.readIN(st);
					J3D.tree_reset();
					J3D.view_reset();
					J3D.view_all();
				} else if (fd.getFileFilter().equals(f3)) {
					this.readNastran(st);
					J3D.tree_reset();
					J3D.view_reset();
					J3D.view_all();
				} else if (fd.getFileFilter().equals(f4)) {
					this.readGmsh(st);
					J3D.tree_reset();
					J3D.view_reset();
					J3D.view_all();
				} else if (fd.getFileFilter().equals(f5)) {
					this.readUNV(st);
					J3D.tree_reset();
					J3D.view_reset();
					J3D.view_all();
				}
			}

			if (fd.getSelectedFile() != null) {
				ConfDB.setProperty("Filepath", fd.getSelectedFile()
						.getAbsolutePath());
				save_configuration();
				header(fd.getSelectedFile().getName());
			}

		} catch (Exception e1) {
			error(e1);
		}
	}

	public Color getColorLine(int cl) {
		int i = (int) (cl - Math.round(cl / 10) * 10);
		if (i == 0)
			return Color.black;
		else if (i == 1)
			return Color.gray;
		else if (i == 2)
			return Color.cyan;
		else if (i == 3)
			return Color.blue;
		else if (i == 4)
			return Color.green;
		else if (i == 5)
			return Color.magenta;
		else if (i == 6)
			return Color.orange;
		else if (i == 7)
			return Color.pink;
		else if (i == 8)
			return Color.red;
		else
			return Color.yellow;
	}

	void b_material_actionPerformed(ActionEvent e) {
		JPanel p = new JPanel(new BorderLayout());
		JPanel p1 = new JPanel();
		JButton m_new = new JButton("New");
		m_new.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JPanel p2 = new JPanel(new VerticalFlowLayout());
				JPanel p3 = new JPanel(new BorderLayout());
				JPanel p4 = new JPanel(new BorderLayout());
				JPanel p5 = new JPanel(new BorderLayout());
				JPanel p6 = new JPanel(new BorderLayout());
				String[] st = { "Elastic", "Elastoplastic",
						"ThermoElastoplastic", "Spring" };
				JComboBox cb_type = new JComboBox(st);
				JTextField tf_name = new JTextField("mat" + id_color);
				JTextField tf_disc = new JTextField();
				JButton b_col = new JButton();
				b_col.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						Color cl = JColorChooser.showDialog(
								getFrame(PanelInspector), "Pick a Color",
								getColorLine(id_color));
						if (cl != null)
							((JButton) e.getSource()).setBackground(cl);
					}
				});
				b_col.setBackground(getColorLine(id_color));
				p2.add(p3);
				p3.add(new JLabel("Type of material"), BorderLayout.WEST);
				p3.add(cb_type, BorderLayout.CENTER);
				p2.add(p4);
				p4.add(new JLabel("Name"), BorderLayout.WEST);
				p4.add(tf_name, BorderLayout.CENTER);
				p2.add(p5, null);
				p5.add(new JLabel("Description"), BorderLayout.WEST);
				p5.add(tf_disc, BorderLayout.CENTER);
				p2.add(p6);
				p6.add(new JLabel("Color"), BorderLayout.WEST);
				p6.add(b_col, BorderLayout.CENTER);
				int result = JOptionPane.showOptionDialog(
						getFrame(PanelInspector), p2, "New Material",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
						null, null, null);
				if (result != 0)
					return;
				id_color++;
				Material mat = new Material();
				mat.name = tf_name.getText();
				mat.type = cb_type.getSelectedItem() + "";
				mat.color = b_col.getBackground();
				mat.description = tf_disc.getText();
				MatDB.put(tf_name.getText(), mat);
				MatTable.revalidate();
			}
		});
		JButton m_del = new JButton("Delete");
		m_del.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] sel = MatTable.getSelectedRows();
				String[] sel_st = new String[sel.length];
				for (int i = 0; i < sel.length; i++) {
					sel_st[i] = MatTable.getValueAt(sel[i], 0) + "";
				}
				for (int i = 0; i < sel.length; i++) {
					MatDB.remove(sel_st[i]);
				}
				MatTable.revalidate();
			}
		});
		JScrollPane spl = new JScrollPane();
		p.add(spl, BorderLayout.CENTER);
		p.add(p1, BorderLayout.NORTH);
		p1.add(m_new);
		p1.add(m_del);
		spl.getViewport().add(MatTable);
		JOptionPane.showMessageDialog(getFrame(PanelInspector), p, "Material",
				JOptionPane.PLAIN_MESSAGE);
		// if(result!=0) load_materials(); else save_materials();
	}

	private void load_configuration() {
		FileInputStream fin = null;
		try {
			ConfDB.clear();
			fin = new FileInputStream("Pre.conf");
			ConfDB.load(fin);
			fin.close();
		} catch (IOException e) {
			error(e);
		}
	}

	public void load_materials() {
		try {
			MatDB.clear();
			RandomAccessFile fin = new RandomAccessFile("Material.properties",
					"r");
			String st;
			while ((st = fin.readLine()) != null) {
				Material mat = new Material();
				mat.type = st;
				mat.name = fin.readLine();
				mat.description = fin.readLine();
				StringTokenizer stt = new StringTokenizer(fin.readLine());
				mat.color = new Color(Integer.parseInt(stt.nextToken()),
						Integer.parseInt(stt.nextToken()), Integer.parseInt(stt
								.nextToken()));
				MatDB.put(mat.name, mat);
			}
			fin.close();
		} catch (Exception ex) {
			error(ex);
		}
	}

	public void load_constraints() {
		try {
			ConstDB.clear();
			RandomAccessFile fin = new RandomAccessFile(
					"Constraints.properties", "r");
			String st;
			while ((st = fin.readLine()) != null) {
				Constraints con = new Constraints();
				con.type = st;
				con.name = fin.readLine();
				con.description = fin.readLine();
				StringTokenizer stt = new StringTokenizer(fin.readLine());
				con.color = new Color(Integer.parseInt(stt.nextToken()),
						Integer.parseInt(stt.nextToken()), Integer.parseInt(stt
								.nextToken()));
				ConstDB.put(con.name, con);
			}
			fin.close();
		} catch (Exception ex) {
			error(ex);
		}
	}

	public void load_loads() {
		try {
			LoadDB.clear();
			RandomAccessFile fin = new RandomAccessFile("Loads.properties", "r");
			String st;
			while ((st = fin.readLine()) != null) {
				Loads ld = new Loads();
				ld.name = st;
				ld.description = fin.readLine();
				StringTokenizer stt = new StringTokenizer(fin.readLine());
				ld.color = new Color(Integer.parseInt(stt.nextToken()),
						Integer.parseInt(stt.nextToken()), Integer.parseInt(stt
								.nextToken()));
				LoadDB.put(ld.name, ld);
			}
			fin.close();
		} catch (Exception ex) {
			error(ex);
		}
	}

	private void save_configuration() {
		FileOutputStream fout;
		try {
			fout = new FileOutputStream("Pre.conf");
			ConfDB.store(fout, "Configuration file for Impact Preprocessor");
			fout.close();
		} catch (IOException e) {
			error(e);
		}
	}

	public void save_materials() {
		try {
			File f = new File("Material.properties");
			f.delete();
			RandomAccessFile fout = new RandomAccessFile(f, "rw");
			for (Enumeration en = MatDB.keys(); en.hasMoreElements();) {
				String key = en.nextElement() + "";
				Material mat = (Material) MatDB.get(key);
				fout.writeBytes(mat.type + "\n");
				fout.writeBytes(mat.name + "\n");
				fout.writeBytes(mat.description + "\n");
				fout.writeBytes(mat.color.getRed() + " " + mat.color.getGreen()
						+ " " + mat.color.getBlue() + "\n");
			}
			fout.close();
			J3D.tree_reset();
			J3D.view_reset();
		} catch (Exception ex) {
			error(ex);
		}
	}

	public void save_constraints() {
		try {
			File f = new File("Constraints.properties");
			f.delete();
			RandomAccessFile fout = new RandomAccessFile(f, "rw");
			for (Enumeration en = ConstDB.keys(); en.hasMoreElements();) {
				String key = en.nextElement() + "";
				Constraints con = (Constraints) ConstDB.get(key);
				fout.writeBytes(con.type + "\n");
				fout.writeBytes(con.name + "\n");
				fout.writeBytes(con.description + "\n");
				fout.writeBytes(con.color.getRed() + " " + con.color.getGreen()
						+ " " + con.color.getBlue() + "\n");
			}
			fout.close();
			J3D.tree_reset();
			J3D.view_reset();
		} catch (Exception ex) {
			error(ex);
		}
	}

	public void save_loads() {
		try {
			File f = new File("Loads.properties");
			f.delete();
			RandomAccessFile fout = new RandomAccessFile(f, "rw");
			for (Enumeration en = LoadDB.keys(); en.hasMoreElements();) {
				String key = en.nextElement() + "";
				Loads ld = (Loads) LoadDB.get(key);
				fout.writeBytes(ld.name + "\n");
				fout.writeBytes(ld.description + "\n");
				fout.writeBytes(ld.color.getRed() + " " + ld.color.getGreen()
						+ " " + ld.color.getBlue() + "\n");
			}
			fout.close();
			J3D.tree_reset();
			J3D.view_reset();
		} catch (Exception ex) {
			error(ex);
		}
	}

	void b_config_actionPerformed(ActionEvent e) {
		JTabbedPane p = new JTabbedPane();
		JPanel p1 = new JPanel(new VerticalFlowLayout());
		JPanel p2 = new JPanel(new VerticalFlowLayout());
		JPanel p3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel p4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel p5 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p1.add(cb_node_0);
		p1.add(cb_node);
		p1.add(cb_element);
		p1.add(cb_constraints);
		p1.add(cb_loads);
		p1.add(cb_trackers);
		p1.add(cb_materials);
		p2.add(p3);
		p2.add(p4);
		p2.add(p5);
		p.add("Controls", p2);
		p.add("Show", p1);
		JTextField tf_CONTROLS_RUN_FROM = new JTextField(CONTROLS_RUN_FROM, 5);
		JTextField tf_CONTROLS_RUN_TO = new JTextField(CONTROLS_RUN_TO, 5);
		JTextField tf_CONTROLS_RUN_STEP = new JTextField(CONTROLS_RUN_STEP, 5);
		JTextField tf_CONTROLS_PRINT_STEP = new JTextField(CONTROLS_PRINT_STEP,
				5);
		JTextField tf_CONTROLS_PRINT_TRACKER_STEP = new JTextField(
				CONTROLS_PRINT_TRACKER_STEP, 5);
		p3.add(new JLabel("Run from"));
		p3.add(tf_CONTROLS_RUN_FROM);
		p3.add(new JLabel("to"));
		p3.add(tf_CONTROLS_RUN_TO);
		p3.add(new JLabel("step"));
		p3.add(tf_CONTROLS_RUN_STEP);
		p4.add(new JLabel("Print every"));
		p4.add(tf_CONTROLS_PRINT_STEP);
		p4.add(new JLabel("step"));
		p5.add(new JLabel("Print tracker every"));
		p5.add(tf_CONTROLS_PRINT_TRACKER_STEP);
		p5.add(new JLabel("step"));
		JOptionPane.showMessageDialog(this, p, "Properties",
				JOptionPane.PLAIN_MESSAGE, null);
		if (cb_node_0.isSelected())
			J3D.setPOINTSIZE(4);
		else
			J3D.setPOINTSIZE(0);
		J3D.setSHOW_ID_NODE(cb_node.isSelected());
		J3D.setSHOW_ID_ELEMENT(cb_element.isSelected());
		J3D.setSHOW_ID_CONSTRAINTS(cb_constraints.isSelected());
		J3D.setSHOW_ID_LOADS(cb_loads.isSelected());
		J3D.setSHOW_ID_TRACKERS(cb_trackers.isSelected());
		if (!cb_materials.isSelected()) {
			cb_graphicsmode.setSelectedIndex(0);
		} else
			cb_graphicsmode.setSelectedIndex(1);
		CONTROLS_RUN_FROM = tf_CONTROLS_RUN_FROM.getText();
		CONTROLS_RUN_TO = tf_CONTROLS_RUN_TO.getText();
		CONTROLS_RUN_STEP = tf_CONTROLS_RUN_STEP.getText();
		CONTROLS_PRINT_STEP = tf_CONTROLS_PRINT_STEP.getText();
		CONTROLS_PRINT_TRACKER_STEP = tf_CONTROLS_PRINT_TRACKER_STEP.getText();
		J3D.view_reset();
		J3D.tree_reset();
	}

	void b_constraints_actionPerformed(ActionEvent e) {
		JPanel p = new JPanel(new BorderLayout());
		JPanel p1 = new JPanel();
		JButton m_new = new JButton("New");
		m_new.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JPanel p2 = new JPanel(new VerticalFlowLayout());
				JPanel p3 = new JPanel(new BorderLayout());
				JPanel p4 = new JPanel(new BorderLayout());
				JPanel p5 = new JPanel(new BorderLayout());
				JPanel p6 = new JPanel(new BorderLayout());
				String[] st = { "Boundary_Condition", "Rigid_Body" };
				JComboBox cb_type = new JComboBox(st);
				JTextField tf_name = new JTextField("Fixed" + id_color);
				JTextField tf_disc = new JTextField();
				JButton b_col = new JButton();
				b_col.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						Color cl = JColorChooser.showDialog(
								getFrame(PanelInspector), "Pick a Color",
								getColorLine(id_color));
						if (cl != null)
							((JButton) e.getSource()).setBackground(cl);
					}
				});
				b_col.setBackground(getColorLine(id_color));
				p2.add(p3);
				p3.add(new JLabel("Type of constraint"), BorderLayout.WEST);
				p3.add(cb_type, BorderLayout.CENTER);
				p2.add(p4);
				p4.add(new JLabel("Name"), BorderLayout.WEST);
				p4.add(tf_name, BorderLayout.CENTER);
				p2.add(p5, null);
				p5.add(new JLabel("Description"), BorderLayout.WEST);
				p5.add(tf_disc, BorderLayout.CENTER);
				p2.add(p6);
				p6.add(new JLabel("Color"), BorderLayout.WEST);
				p6.add(b_col, BorderLayout.CENTER);
				int result = JOptionPane.showOptionDialog(
						getFrame(PanelInspector), p2, "New Constraints",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
						null, null, null);
				if (result != 0)
					return;
				id_color++;
				Constraints con = new Constraints();
				con.name = tf_name.getText();
				con.type = cb_type.getSelectedItem() + "";
				con.color = b_col.getBackground();
				con.description = tf_disc.getText();
				ConstDB.put(tf_name.getText(), con);
				ConstTable.revalidate();
			}
		});
		JButton m_del = new JButton("Delete");
		m_del.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] sel = ConstTable.getSelectedRows();
				String[] sel_st = new String[sel.length];
				for (int i = 0; i < sel.length; i++) {
					sel_st[i] = ConstTable.getValueAt(sel[i], 0) + "";
				}
				for (int i = 0; i < sel.length; i++) {
					ConstDB.remove(sel_st[i]);
				}
				ConstTable.revalidate();
			}
		});
		JScrollPane spl = new JScrollPane();
		p.add(spl, BorderLayout.CENTER);
		p.add(p1, BorderLayout.NORTH);
		p1.add(m_new);
		p1.add(m_del);
		spl.getViewport().add(ConstTable);
		JOptionPane.showMessageDialog(getFrame(PanelInspector), p,
				"Constraints", JOptionPane.PLAIN_MESSAGE);
		// if(result!=0) load_constraints(); else save_constraints();
	}

	void b_loads_actionPerformed(ActionEvent e) {
		JPanel p = new JPanel(new BorderLayout());
		JPanel p1 = new JPanel();
		JButton m_new = new JButton("New");
		m_new.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JPanel p2 = new JPanel(new VerticalFlowLayout());
				JPanel p3 = new JPanel(new BorderLayout());
				JPanel p4 = new JPanel(new BorderLayout());
				JPanel p5 = new JPanel(new BorderLayout());
				JPanel p6 = new JPanel(new BorderLayout());
				// String[] st = {"Boundary_Condition","Rigid_Body"};
				// JComboBox cb_type = new JComboBox(st);
				JTextField tf_name = new JTextField("Force" + id_color);
				JTextField tf_disc = new JTextField();
				JButton b_col = new JButton();
				b_col.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						Color cl = JColorChooser.showDialog(
								getFrame(PanelInspector), "Pick a Color",
								getColorLine(id_color));
						if (cl != null)
							((JButton) e.getSource()).setBackground(cl);
					}
				});
				b_col.setBackground(getColorLine(id_color));
				p2.add(p3);
				// p3.add(new JLabel("Type of constraint"), BorderLayout.WEST);
				// p3.add(cb_type, BorderLayout.CENTER);
				p2.add(p4);
				p4.add(new JLabel("Name"), BorderLayout.WEST);
				p4.add(tf_name, BorderLayout.CENTER);
				p2.add(p5, null);
				p5.add(new JLabel("Description"), BorderLayout.WEST);
				p5.add(tf_disc, BorderLayout.CENTER);
				p2.add(p6);
				p6.add(new JLabel("Color"), BorderLayout.WEST);
				p6.add(b_col, BorderLayout.CENTER);
				int result = JOptionPane.showOptionDialog(
						getFrame(PanelInspector), p2, "New Loads",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
						null, null, null);
				if (result != 0)
					return;
				id_color++;
				Loads ld = new Loads();
				ld.name = tf_name.getText();
				ld.color = b_col.getBackground();
				ld.description = tf_disc.getText();
				LoadDB.put(tf_name.getText(), ld);
				LoadTable.revalidate();
			}
		});
		JButton m_del = new JButton("Delete");
		m_del.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] sel = LoadTable.getSelectedRows();
				String[] sel_st = new String[sel.length];
				for (int i = 0; i < sel.length; i++) {
					sel_st[i] = LoadTable.getValueAt(sel[i], 0) + "";
				}
				for (int i = 0; i < sel.length; i++) {
					LoadDB.remove(sel_st[i]);
				}
				LoadTable.revalidate();
			}
		});
		JScrollPane spl = new JScrollPane();
		p.add(spl, BorderLayout.CENTER);
		p.add(p1, BorderLayout.NORTH);
		p1.add(m_new);
		p1.add(m_del);
		spl.getViewport().add(LoadTable);
		JOptionPane.showMessageDialog(getFrame(PanelInspector), p, "Loads",
				JOptionPane.PLAIN_MESSAGE);
		// if(result!=0) load_loads(); else save_loads();
	}

	public void showEditPanel(MouseEvent e) {
		if (e.getClickCount() == 2) {
			TreePath tp = Tree.getSelectionPath();
			if (tp == null)
				return;
			Object[] obj = tp.getPath();
			for (int j = 0; j < obj.length; j++) {
				Object uobj = ((DefaultMutableTreeNode) obj[j]).getUserObject();
				if (uobj instanceof _Object) {
					p_cmd.removeAll();
					edit_panel = (_Object) uobj;
					p_cmd.add(edit_panel.getEditPanel(J3D, this),
							BorderLayout.CENTER);
					p_cmd.validate();
					PanelInspector.validate();
				}
			}
		}
	}

	public void showEditPanel() {
		TreePath tp = Tree.getSelectionPath();
		if (tp == null)
			return;
		Object[] obj = tp.getPath();
		for (int j = 0; j < obj.length; j++) {
			Object uobj = ((DefaultMutableTreeNode) obj[j]).getUserObject();
			if (uobj instanceof _Object) {
				p_cmd.removeAll();
				p_cmd.add(((_Object) uobj).getEditPanel(J3D, this),
						BorderLayout.CENTER);
				p_cmd.validate();
				PanelInspector.validate();
			}
		}
	}

	public void clearEditPanel() {
		p_cmd.removeAll();
		p_cmd.validate();
		PanelInspector.validate();
		Tree.requestFocus();
	}

	public void resetselect() {
		TreePath[] tp = Tree.getSelectionPaths();
		if (tp == null)
			return;
		J3D.clearSelectOnAllObjects3D();
		for (int i = 0; i < tp.length; i++) {
			DefaultMutableTreeNode tn = (DefaultMutableTreeNode) tp[i]
					.getLastPathComponent();
			Object obj = tn.getUserObject();
			if (obj instanceof _Object)
				((_Object) obj).setSelected(true);
		}
	}

	private void mergeNodes(float merge_distance) {
		float dist;
		_Node[] narr = J3D.getAllNodes3D();

		// Set up references and Id:s
		for (int i = 0; i < narr.length; i++) {
			narr[i].setMergedReference(narr[i]);
		}

		// Merge nodes to common node with lowerst master type
		merge_distance = merge_distance * merge_distance;
		for (int i = 0; i < narr.length; i++)
			for (int j = i + 1; j < narr.length; j++) {

				dist = (narr[i].x - narr[j].x) * (narr[i].x - narr[j].x)
						+ (narr[i].y - narr[j].y) * (narr[i].y - narr[j].y)
						+ (narr[i].z - narr[j].z) * (narr[i].z - narr[j].z);

				if (dist <= merge_distance)
					if (narr[j].getMergedReference().getMaster_type() < narr[i]
							.getMergedReference().getMaster_type())
						narr[i].setMergedReference(narr[j].getMergedReference());
					else
						narr[j].setMergedReference(narr[i].getMergedReference());
			}

		// Replace all instances in the database
		for (int i = 0; i < narr.length; i++)
			if (narr[i].getMergedReference() != narr[i])
				J3D.replaceAllInstancesOf(narr[i], narr[i].getMergedReference());

	}

	private void saveSolverModel(String file_in) {
		_Node[] narr;
		_Object[] earr;
		_Group[] garr;
		_Geometry[] geoarr;
		String st;

		if (!file_in.endsWith(".in"))
			file_in += ".in";

		StringBuffer st_model = new StringBuffer(
				"# Created Impact "
						+ ver
						+ "\n# "
						+ new Date()
						+ "\n\nCONTROLS\nrun from "
						+ CONTROLS_RUN_FROM
						+ " to "
						+ CONTROLS_RUN_TO
						+ (CONTROLS_RUN_STEP.trim().length() != 0 ? " step "
								+ CONTROLS_RUN_STEP : "")
						+ "\nprint every "
						+ CONTROLS_PRINT_STEP
						+ " step\n"
						+ (CONTROLS_PRINT_TRACKER_STEP.trim().length() != 0 ? "Print TRACKER every "
								+ CONTROLS_PRINT_TRACKER_STEP + " step"
								: "") + "\n\n");
		StringBuffer st_nodes = new StringBuffer("");

		StringBuffer st_elements = new StringBuffer("");
		StringBuffer st_elements_Rod_2 = new StringBuffer(
				"ELEMENTS OF TYPE Rod_2\n");
		StringBuffer st_elements_Beam_2 = new StringBuffer(
				"ELEMENTS OF TYPE Beam_2\n");
		StringBuffer st_elements_Beam_Spring_2 = new StringBuffer(
				"ELEMENTS OF TYPE Beam_Spring_2\n");
		StringBuffer st_elements_Contact_Line = new StringBuffer(
				"ELEMENTS OF TYPE Contact_Line\n");
		StringBuffer st_elements_Contact_Triangle = new StringBuffer(
				"ELEMENTS OF TYPE Contact_Triangle\n");
		StringBuffer st_elements_Shell_C0_3 = new StringBuffer(
				"ELEMENTS OF TYPE Shell_C0_3\n");
		StringBuffer st_elements_Shell_BT_4 = new StringBuffer(
				"ELEMENTS OF TYPE Shell_BT_4\n");
		StringBuffer st_elements_Solid_Iso_4 = new StringBuffer(
				"ELEMENTS OF TYPE Solid_Iso_4\n");
		StringBuffer st_elements_Solid_Iso_6 = new StringBuffer(
				"ELEMENTS OF TYPE Solid_Iso_6\n");

		StringBuffer st_geometry = new StringBuffer("");
		StringBuffer geo_Point = new StringBuffer("GEOMETRY OF TYPE Point\n");
		StringBuffer geo_Spline = new StringBuffer("GEOMETRY OF TYPE Spline\n");
		StringBuffer geo_Arc = new StringBuffer("GEOMETRY OF TYPE Arc\n");
		StringBuffer geo_SurfBil = new StringBuffer(
				"GEOMETRY OF TYPE SurfBil\n");
		StringBuffer geo_SurfDir = new StringBuffer(
				"GEOMETRY OF TYPE SurfDir\n");
		StringBuffer geo_SurfFill = new StringBuffer(
				"GEOMETRY OF TYPE SurfFill\n");
		StringBuffer geo_SurfRev = new StringBuffer(
				"GEOMETRY OF TYPE SurfRev\n");
		StringBuffer geo_SurfRul = new StringBuffer(
				"GEOMETRY OF TYPE SurfRul\n");

		String s = JOptionPane
				.showInputDialog(
						"Enter node merge distance\nor select Cancel to prevent merging",
						new Float(J3D.getNODE_MERGE_TOLERANCE()));

		// Merge the nodes
		if (s != null)
			this.mergeNodes(Float.parseFloat(s));

		// Initialize
		narr = J3D.getAllNodes3D();
		earr = J3D.getAllElements3D();
		garr = J3D.getAllGroups3D();
		geoarr = J3D.getAllGeometry3D();

		// Create printout of nodes
		headerMessage("Printing Nodes");
		if (narr.length > 0)
			st_nodes.append("NODES\n");
		for (int i = 0; i < narr.length; i++)
			st_nodes.append(narr[i].writeObject() + "\n");

		// Create printout of elements
		headerMessage("Printing elements");
		for (int j = 0; j < earr.length; j++) {
			if (earr[j] instanceof _Element2
					&& ((_Element2) earr[j]).msh_type == Canvas3D.MESH_Beam_2)
				st_elements_Beam_2.append(((_Element2) earr[j]).writeObject()
						+ "\n");
			else if (earr[j] instanceof _Element2
					&& ((_Element2) earr[j]).msh_type == Canvas3D.MESH_Contact_Line)
				st_elements_Contact_Line.append(((_Element2) earr[j])
						.writeObject() + "\n");
			else if (earr[j] instanceof _Element2
					&& ((_Element2) earr[j]).msh_type == Canvas3D.MESH_Rod_2)
				st_elements_Rod_2.append(((_Element2) earr[j]).writeObject()
						+ "\n");
			else if (earr[j] instanceof _Element3
					&& ((_Element3) earr[j]).msh_type == Canvas3D.MESH_Contact_Triangle)
				st_elements_Contact_Triangle.append(((_Element3) earr[j])
						.writeObject() + "\n");
			else if (earr[j] instanceof _Element3
					&& ((_Element3) earr[j]).msh_type == Canvas3D.MESH_Shell_C0_3)
				st_elements_Shell_C0_3.append(((_Element3) earr[j])
						.writeObject() + "\n");
			else if (earr[j] instanceof _Element4
					&& ((_Element4) earr[j]).msh_type == Canvas3D.MESH_Shell_BT_4)
				st_elements_Shell_BT_4.append(((_Element4) earr[j])
						.writeObject() + "\n");
			else if (earr[j] instanceof _Element4
					&& ((_Element4) earr[j]).msh_type == Canvas3D.MESH_Solid_Iso_4)
				st_elements_Solid_Iso_4.append(((_Element4) earr[j])
						.writeObject() + "\n");
			else if (earr[j] instanceof _Element8)
				st_elements_Solid_Iso_6.append(((_Element8) earr[j])
						.writeObject() + "\n");
		}

		st_elements
				.append((st_elements_Rod_2.length() > 23 ? st_elements_Rod_2
						.toString() + "\n\n" : "")
						+ (st_elements_Beam_2.length() > 24 ? st_elements_Beam_2
								.toString() + "\n\n"
								: "")
						+ (st_elements_Beam_Spring_2.length() > 31 ? st_elements_Beam_Spring_2
								.toString() + "\n\n"
								: "")
						+ (st_elements_Contact_Line.length() > 30 ? st_elements_Contact_Line
								.toString() + "\n\n"
								: "")
						+ (st_elements_Contact_Triangle.length() > 34 ? st_elements_Contact_Triangle
								.toString() + "\n\n"
								: "")
						+ (st_elements_Shell_C0_3.length() > 28 ? st_elements_Shell_C0_3
								.toString() + "\n\n"
								: "")
						+ (st_elements_Shell_BT_4.length() > 28 ? st_elements_Shell_BT_4
								.toString() + "\n\n"
								: "")
						+ (st_elements_Solid_Iso_4.length() > 29 ? st_elements_Solid_Iso_4
								.toString() + "\n\n"
								: "")
						+ (st_elements_Solid_Iso_6.length() > 29 ? st_elements_Solid_Iso_6
								.toString() : ""));

		// Create printout of geometry
		headerMessage("Printing geometry");
		for (int j = 0; j < geoarr.length; j++) {
			if (geoarr[j] instanceof _Point)
				geo_Point.append(geoarr[j].writeObject() + "\n");
			else if (geoarr[j] instanceof _Arc)
				geo_Arc.append(geoarr[j].writeObject() + "\n");
			else if (geoarr[j] instanceof _Spline)
				geo_Spline.append(geoarr[j].writeObject() + "\n");
			else if (geoarr[j] instanceof _SurfBil)
				geo_SurfBil.append(geoarr[j].writeObject() + "\n");
			else if (geoarr[j] instanceof _SurfDir)
				geo_SurfDir.append(geoarr[j].writeObject() + "\n");
			else if (geoarr[j] instanceof _SurfFill)
				geo_SurfFill.append(geoarr[j].writeObject() + "\n");
			else if (geoarr[j] instanceof _SurfRev)
				geo_SurfRev.append(geoarr[j].writeObject() + "\n");
			else if (geoarr[j] instanceof _SurfRul)
				geo_SurfRul.append(geoarr[j].writeObject() + "\n");

		}

		st_geometry.append((geo_Point.length() > 25 ? geo_Point.toString()
				+ "\n\n" : "")
				+ (geo_Arc.length() > 25 ? geo_Arc.toString() + "\n\n" : "")
				+ (geo_Spline.length() > 25 ? geo_Spline.toString() + "\n\n"
						: "")
				+ (geo_SurfBil.length() > 25 ? geo_SurfBil.toString() + "\n\n"
						: "")
				+ (geo_SurfDir.length() > 25 ? geo_SurfDir.toString() + "\n\n"
						: "")
				+ (geo_SurfFill.length() > 26 ? geo_SurfFill.toString()
						+ "\n\n" : "")
				+ (geo_SurfRev.length() > 25 ? geo_SurfRev.toString() + "\n\n"
						: "")
				+ (geo_SurfRul.length() > 25 ? geo_SurfRul.toString() + "\n\n"
						: ""));

		headerMessage("Printing materials");
		for (Enumeration en = MatDB.keys(); en.hasMoreElements();) {
			String key = en.nextElement() + "";
			Material mat = (Material) MatDB.get(key);
			st = "MATERIALS OF TYPE " + mat.type + "\n" + mat.name + " "
					+ mat.description + "\n\n";
			if (st_elements.indexOf("Material = " + mat.name) != -1)
				st_model.append(st);
		}

		headerMessage("Printing constraints");
		for (Enumeration en = ConstDB.keys(); en.hasMoreElements();) {
			String key = en.nextElement() + "";
			Constraints con = (Constraints) ConstDB.get(key);
			st = "CONSTRAINTS OF TYPE " + con.type + "\n" + con.name + " "
					+ con.description + "\n\n";
			if (st_nodes.indexOf("Constraint = " + con.name) != -1)
				st_model.append(st);
		}

		headerMessage("Printing loads");
		for (Enumeration en = LoadDB.keys(); en.hasMoreElements();) {
			String key = en.nextElement() + "";
			Loads ld = (Loads) LoadDB.get(key);
			st = "LOADS " + "\n" + ld.name + " " + ld.description + "\n\n";
			if (st_nodes.indexOf("Load = " + ld.name) != -1
					|| st_elements.indexOf("Load = " + ld.name) != -1)
				st_model.append(st);
		}

		headerMessage("Printing Groups");
		boolean firsttime = true;

		for (int i = 3; i < garr.length; i++) {
			// Only print groups that are user defined
			if (garr[i] instanceof _GroupUserDefined) {
				if (firsttime) {
					st_model.append("GROUPS");
					firsttime = false;
				}
				st_model.append(garr[i].writeObject());
			}
		}

		st_model.append("\n\n");

		st_model.append(st_geometry.toString() + "\n\n" + st_nodes.toString()
				+ "\n\n" + st_elements.toString());

		headerMessage("Writing file");
		try {
			File f = new File(file_in);
			f.delete();
			RandomAccessFile fout = new RandomAccessFile(f, "rw");
			fout.writeBytes(st_model.toString());
			fout.close();
		} catch (Exception ex) {
			error(ex);
		}

		headerMessage(null);
	}

	/**
	 * This method goes through the Canvas 3D view and checks with _Objects that
	 * are selected. If an object is found, all occurances of this object in the
	 * tree is selected.
	 * 
	 * A block variable is set during this update to avoid triggering of the
	 * SelectionChanged event for each change.
	 * 
	 */

	public void rescanTree() {
		Object obj;
		TreePath tp = Tree.getPathForRow(0);
		Vector tps = new Vector();
		TreePath[] ftps;

		if (tp == null)
			return;

		DefaultMutableTreeNode tn = (DefaultMutableTreeNode) tp
				.getPathComponent(0);
		Enumeration en = tn.depthFirstEnumeration();

		block = true;

		while (en.hasMoreElements()) {
			tn = (DefaultMutableTreeNode) en.nextElement();
			obj = tn.getUserObject();
			if (obj instanceof _Object)
				if (((_Object) obj).isSelected()) {
					tps.add(new TreePath(tn.getPath()));
				}
		}

		ftps = new TreePath[tps.size()];

		for (int i = 0; i < ftps.length; i++) {
			ftps[i] = (TreePath) tps.elementAt(i);
		}

		Tree.setExpandsSelectedPaths(false);

		Tree.addSelectionPaths(ftps);

		block = false;

		Tree.repaint();
	}

	/**
	 * 
	 * This method goes through the tree and checks which nodes that have been
	 * selected. If a node is selected, the corresponding _Object is set as
	 * selected in the canvas3D view. Finally, the view is redrawn.
	 * 
	 * @param e
	 *            TreeSelectionEvent
	 */

	public void treeSelectionChanged() {
		J3D.clearSelectOnAllObjects3D();
		TreePath[] tp = Tree.getSelectionPaths();
		if (tp == null)
			return;
		for (int i = 0; i < tp.length; i++) {
			DefaultMutableTreeNode tn = (DefaultMutableTreeNode) tp[i]
					.getLastPathComponent();
			Object obj = tn.getUserObject();
			int count = 0;
			if (obj instanceof _Object) {
				((_Object) obj).setSelected(true);
				count++;
			}
			/*
			 * int lev = tn.getLevel(); while((tn=tn.getNextNode())!=null &&
			 * lev<tn.getLevel()){ obj = tn.getUserObject(); if(obj instanceof
			 * _Object){ ((_Object)obj).Select(tp[i]+""); count++; } }
			 */
			if (count == 0) {
				obj = ((DefaultMutableTreeNode) tp[i].getParentPath()
						.getLastPathComponent()).getUserObject();
				if (obj instanceof _Object)
					((_Object) obj).setSelected(true);
			}
		}
		J3D.view_reset();
	}

	public void clearTree() {
		Tree.clearSelection();
	}

	public void headerMessage(String m) {
		Frame f = getFrame(this);
		String s = f.getTitle();
		if (m == null)
			m = ImpactGUI.ver;
		String s1 = s.substring(0, s.lastIndexOf("]") + 1);
		f.setTitle(s1 + " " + m);

	}

	public void headerMessage(String m, float progress) {
		Frame f = getFrame(this);
		String s = f.getTitle();
		String p = ">>>>>>>>>>";
		String e = "----------|";
		progress *= 10.0;
		String s1 = s.substring(0, s.lastIndexOf("]") + 1);
		f.setTitle(s1 + " " + m + "|" + p.substring(0, (int) progress)
				+ e.substring((int) progress, 11));

	}

	public void header(String m) {
		Frame f = getFrame(this);
		String s = f.getTitle();

		String s1 = s.substring(0, s.indexOf("[PRE:") + 6);
		String s2 = s.substring(s.indexOf("[PRO:"), s.length());
		f.setTitle(s1 + m + "] " + s2);
	}

	public void requestAction() {
		if (edit_panel != null)
			edit_panel.requestAction();
	}

	/**
	 * Reads a Gmsh version 2 mesh file and generates elements and nodes in the
	 * preprocessor
	 * 
	 * @param filename
	 */
	public void readGmsh(String filename) throws FileNotFoundException,
			IOException, ParseException {

		// Define regexp patterns for the data to import
		Regex Element = new Regex(
				"^([0-9]+)\\s+([0-9]+)\\s+([0-9]+)\\s+([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*([0-9]+)?\\s*$");

		Regex Node = new Regex(
				"^([0-9]+)\\s+([-+]?[0-9]+[.]?[0-9]*([eE][-+]?[0-9]+)?)\\s+([-+]?[0-9]+[.]?[0-9]*([eE][-+]?[0-9]+)?)\\s+([-+]?[0-9]+[.]?[0-9]*([eE][-+]?[0-9]+)?)\\s*$");
		Regex NrOf = new Regex("^\\s*([0-9]+)\\s*$");

		Regex RNodes = new Regex("^\\s*\\$Nodes\\s*$");
		Regex RElem = new Regex("^\\s*\\$Elements\\s*$");

		String str;
		_Node n;
		;
		Hashtable nodes = new Hashtable();
		int nr_of_nodes, i;
		int linenr = 0;
		int nr_of_elements = 0;
		String[] nn = new String[30];
		_Element el;

		BufferedReader br = new BufferedReader(new FileReader(filename));

		// Speed up the parsing
		Node.optimize();
		Element.optimize();

		// Define group
		_Group g = new _Group(false, true);
		g.setName(filename);
		J3D.add3D(g);

		_Group g_n = new _Group(false, false);
		g_n.setName("Node");
		J3D.add3D(g_n);

		_Group g_e = new _Group(false, false);
		g_e.setName("Elem");
		J3D.add3D(g_e);

		g.addToGroup(g_n);
		g.addToGroup(g_e);

		// Start parsing
		while (!RNodes.search(str = br.readLine()))
			linenr++;

		// Start reading nodes
		str = br.readLine();
		linenr++;

		if (NrOf.search(str))
			nr_of_nodes = Integer.parseInt(NrOf.stringMatched(1));
		else
			throw new ParseException("No number of nodes found in Gmsh file:"
					+ filename, linenr);

		i = 0;
		while (Node.search(str = br.readLine())) {

			linenr++;
			n = new _Node(Float.parseFloat(Node.stringMatched(2)),
					Float.parseFloat(Node.stringMatched(4)),
					Float.parseFloat(Node.stringMatched(6)));
			nodes.put(Node.stringMatched(1), n); // Nodenr, nodeobject
			g_n.addToGroup(n);
			J3D.add3D(n);
			headerMessage("Reading Nodes ", (float) i++ / (float) nr_of_nodes);

		}

		// Read elements
		while (!RElem.search(str = br.readLine()))
			linenr++;

		str = br.readLine();
		linenr++;

		if (NrOf.search(str))
			nr_of_elements = Integer.parseInt(NrOf.stringMatched(1));
		else
			throw new ParseException(
					"No number of elements found in Gmsh file:" + filename,
					linenr);

		i = 0;
		while (Element.search(str = br.readLine())) {

			linenr++;
			headerMessage("Reading Elements ", (float) i++
					/ (float) nr_of_elements);

			switch (Integer.parseInt(Element.stringMatched(2))) {

			case 1:
				nn[0] = Element.stringMatched(Integer.parseInt(Element
						.stringMatched(3)) + 4);
				nn[1] = Element.stringMatched(Integer.parseInt(Element
						.stringMatched(3)) + 5);
				if (nn[0] == null || nn[1] == null)
					throw new ParseException("No node number for element:"
							+ Element.stringMatched(1), linenr);
				// Generate element
				el = new _Element2((_Node) nodes.get(nn[0]),
						(_Node) nodes.get(nn[1]));
				g_e.addToGroup(el);
				J3D.add3D(el);
				break;

			case 2:
				nn[0] = Element.stringMatched(Integer.parseInt(Element
						.stringMatched(3)) + 4);
				nn[1] = Element.stringMatched(Integer.parseInt(Element
						.stringMatched(3)) + 5);
				nn[2] = Element.stringMatched(Integer.parseInt(Element
						.stringMatched(3)) + 6);
				if (nn[0] == null || nn[1] == null || nn[2] == null)
					throw new ParseException("No node number for element:"
							+ Element.stringMatched(1), linenr);
				// Generate element
				el = new _Element3((_Node) nodes.get(nn[0]),
						(_Node) nodes.get(nn[1]), (_Node) nodes.get(nn[2]));
				g_e.addToGroup(el);
				J3D.add3D(el);
				break;

			case 3:
				nn[0] = Element.stringMatched(Integer.parseInt(Element
						.stringMatched(3)) + 4);
				nn[1] = Element.stringMatched(Integer.parseInt(Element
						.stringMatched(3)) + 5);
				nn[2] = Element.stringMatched(Integer.parseInt(Element
						.stringMatched(3)) + 6);
				nn[3] = Element.stringMatched(Integer.parseInt(Element
						.stringMatched(3)) + 7);
				if (nn[0] == null || nn[1] == null || nn[2] == null
						|| nn[3] == null)
					throw new ParseException("No node number for element:"
							+ Element.stringMatched(1), linenr);
				// Generate element
				el = new _Element4((_Node) nodes.get(nn[0]),
						(_Node) nodes.get(nn[1]), (_Node) nodes.get(nn[2]),
						(_Node) nodes.get(nn[3]), Canvas3D.MESH_Shell_BT_4);
				g_e.addToGroup(el);
				J3D.add3D(el);
				break;

			case 4:
				nn[0] = Element.stringMatched(Integer.parseInt(Element
						.stringMatched(3)) + 4);
				nn[1] = Element.stringMatched(Integer.parseInt(Element
						.stringMatched(3)) + 5);
				nn[2] = Element.stringMatched(Integer.parseInt(Element
						.stringMatched(3)) + 6);
				nn[3] = Element.stringMatched(Integer.parseInt(Element
						.stringMatched(3)) + 7);
				if (nn[0] == null || nn[1] == null || nn[2] == null
						|| nn[3] == null)
					throw new ParseException("No node number for element:"
							+ Element.stringMatched(1), linenr);
				// Generate element
				el = new _Element4((_Node) nodes.get(nn[0]),
						(_Node) nodes.get(nn[1]), (_Node) nodes.get(nn[2]),
						(_Node) nodes.get(nn[3]), Canvas3D.MESH_Solid_Iso_4);
				g_e.addToGroup(el);
				J3D.add3D(el);
				break;

			case 5:
				for (int j = 0; j < 7; j++) {
					nn[j] = Element.stringMatched(Integer.parseInt(Element
							.stringMatched(3)) + j + 4);
					if (nn[j] == null)
						throw new ParseException("No node number for element:"
								+ Element.stringMatched(1), linenr);
				}
				// Generate element
				el = new _Element8((_Node) nodes.get(nn[0]),
						(_Node) nodes.get(nn[1]), (_Node) nodes.get(nn[2]),
						(_Node) nodes.get(nn[3]), (_Node) nodes.get(nn[4]),
						(_Node) nodes.get(nn[5]), (_Node) nodes.get(nn[6]),
						(_Node) nodes.get(nn[7]));
				g_e.addToGroup(el);
				J3D.add3D(el);
				break;

			case 6: // 6-node prism
				for (int j = 0; j < 5; j++) {
					nn[j] = Element.stringMatched(Integer.parseInt(Element
							.stringMatched(3)) + j + 4);
					if (nn[j] == null)
						throw new ParseException("No node number for element:"
								+ Element.stringMatched(1), linenr);
				}
				// Generate element
				el = new _Element8((_Node) nodes.get(nn[0]),
						(_Node) nodes.get(nn[1]), (_Node) nodes.get(nn[2]),
						(_Node) nodes.get(nn[2]), (_Node) nodes.get(nn[3]),
						(_Node) nodes.get(nn[4]), (_Node) nodes.get(nn[5]),
						(_Node) nodes.get(nn[5]));
				g_e.addToGroup(el);
				J3D.add3D(el);
				break;

			case 7: // 5-node pyramid
				for (int j = 0; j < 4; j++) {
					nn[j] = Element.stringMatched(Integer.parseInt(Element
							.stringMatched(3)) + j + 4);
					if (nn[j] == null)
						throw new ParseException("No node number for element:"
								+ Element.stringMatched(1), linenr);
				}
				// Generate element
				el = new _Element8((_Node) nodes.get(nn[0]),
						(_Node) nodes.get(nn[1]), (_Node) nodes.get(nn[2]),
						(_Node) nodes.get(nn[3]), (_Node) nodes.get(nn[4]),
						(_Node) nodes.get(nn[4]), (_Node) nodes.get(nn[4]),
						(_Node) nodes.get(nn[4]));
				g_e.addToGroup(el);
				J3D.add3D(el);
				break;

			case 8: // 3-node second order line
				break;

			case 9: // 6-node second order triangle
				break;

			case 10: // 9-node second order quadrangle
				break;

			case 11: // 10-node second order tetrahedron
				break;

			case 12: // 27-node second order hedahedron
				break;

			case 13: // 18-node second order prism
				break;

			case 14: // 14-node second order pyramid
				break;

			case 15: // 1-node Point
				break;

			case 16: // 8-node second order quadrangle
				break;

			case 17: // 20-node second order hexahedron
				break;

			case 18: // 15-node second order
				break;

			case 19: // 13-node second order pyramid
				break;

			default:
				throw new ParseException(
						"Unsupported element type in Gmsh file:" + filename,
						linenr);

			}

		}

		// Finished, now close

		headerMessage("File Read Successfully");
		br.close();

	}

	/**
	 * Reads a Gmsh version 2 mesh file and generates elements and nodes in the
	 * preprocessor
	 * 
	 * @param filename
	 */
	public void readNastran(String filename) throws FileNotFoundException,
			IOException, ParseException {

		// Define regexp patterns for the data to import
		Regex GRID = new Regex(
				"^GRID    ([0-9 ]{8})([0-9 ]{8})([+\\-0-9.eE ]{8})([+\\-0-9.eE ]{8})([+\\-0-9.eE ]{1,8})([0-9 ]{1,8})?([0-9eE ]{1,8})? *$");
		Regex GRIDS1 = new Regex(
				"^GRID\\*   ([0-9 ]{16})([0-9 ]{16})([+\\-0-9.eE ]{16})([+\\-0-9.eE ]{1,16}) *$");
		Regex GRIDS2 = new Regex(
				"^\\*       ([+\\-0-9.eE ]{1,16})([0-9 ]{1,16})?([0-9eE ]{1,16})? *$");
		Regex FGRID = new Regex(
				"^GRID\\*,([0-9 ]+),([0-9 ]+),([-+]?[0-9]+[.]?[0-9]*(?:[eE][-+]?[0-9]+)?),([-+]?[0-9]+[.]?[0-9]*(?:[eE][-+]?[0-9]+)?),([-+]?[0-9]+[.]?[0-9]*(?:[eE][-+]?[0-9]+)?),([0-9 ]+)?,?([0-9 ]{8})? *");

		Regex CBAR = new Regex(
				"^CBAR    ([0-9 ]{8})([0-9 ]{8})([0-9 ]{8})([0-9 ]{1,8})([+\\-0-9. eE]{1,8})?([+\\-0-9. eE]{1,8})?([+\\-0-9. eE]{1,8})? *$");
		Regex CTRIA3 = new Regex(
				"^CTRIA3  ([0-9 ]{8})([0-9 ]{8})([0-9 ]{8})([0-9 ]{8})([0-9 ]{1,8})([+\\-0-9. eE]{8})?([0-9 ]{8})?([+\\-0-9. eE]{8})? *$");
		Regex CQUAD4 = new Regex(
				"^CQUAD4  ([0-9 ]{8})([0-9 ]{8})([0-9 ]{8})([0-9 ]{8})([0-9 ]{8})([0-9 ]{1,8})([+\\-0-9. eE]{8})?([0-9 ]{8})?([+\\-0-9. eE]{8})? *$");
		Regex CTETRA = new Regex(
				"^CTETRA  ([0-9 ]{8})([0-9 ]{8})([0-9 ]{8})([0-9 ]{8})([0-9 ]{8})([0-9 ]{1,8})([+\\-0-9. eE]{8})?([0-9 ]{8})?([+\\-0-9. eE]{8})? *$");
		Regex CHEXA1 = new Regex(
				"^CHEXA   ([0-9 ]{8})([0-9 ]{8})([0-9 ]{8})([0-9 ]{8})([0-9 ]{8})([0-9 ]{8})([0-9 ]{8})([0-9 ]{1,8}).*$");
		Regex CHEXA2 = new Regex(
				"^........([0-9 ]{8})([0-9 ]{1,8})([+\\-0-9. eE]{8})?([0-9 ]{8})?([+\\-0-9. eE]{8})? *$");
		Regex CPENTA = new Regex(
				"^CPENTA  ([0-9 ]{8})([0-9 ]{8})([0-9 ]{8})([0-9 ]{8})([0-9 ]{8})([0-9 ]{8})([0-9 ]{8})([0-9 ]{1,8})[ +]*$");

		Regex TO_ENG = new Regex("( *[-+][0-9.]*)([-+])", "${1}E${2}");

		String str;
		_Node n;
		Hashtable nodes = new Hashtable();
		int i;
		int linenr = 0;
		String[] nn = new String[30];
		_Node[] nod = new _Node[16];
		_Element el;

		BufferedReader br = new BufferedReader(new FileReader(filename));

		// Speed up the parsing
		GRID.optimize();
		GRIDS1.optimize();
		GRIDS2.optimize();
		FGRID.optimize();
		CBAR.optimize();
		CTRIA3.optimize();
		CQUAD4.optimize();
		CTETRA.optimize();
		CHEXA1.optimize();
		CHEXA2.optimize();
		CPENTA.optimize();

		// Define group
		_Group g = new _Group(false, true);
		g.setName(filename);
		J3D.add3D(g);

		_Group g_n = new _Group(false, false);
		g_n.setName("Node");
		J3D.add3D(g_n);

		_Group g_e = new _Group(false, false);
		g_e.setName("Elem");
		J3D.add3D(g_e);

		g.addToGroup(g_n);
		g.addToGroup(g_e);

		// Start reading nodes
		i = 0;
		while ((str = br.readLine()) != null) {

			if (GRID.search(str)) {
				linenr++;
				n = new _Node(Float.parseFloat(TO_ENG.replaceAllFrom(
						GRID.stringMatched(3), 2)), Float.parseFloat(TO_ENG
						.replaceAllFrom(GRID.stringMatched(4), 2)),
						Float.parseFloat(TO_ENG.replaceAllFrom(
								GRID.stringMatched(5), 2)));
				nodes.put(GRID.stringMatched(1).trim(), n); // Nodenr,
															// nodeobject
				g_n.addToGroup(n);
				J3D.add3D(n);
				headerMessage("Reading Node: " + i++);
			} else if (GRIDS1.search(str)) {
				linenr++;
				float x, y, z;
				x = Float
						.parseFloat(TO_ENG.replaceAll(GRIDS1.stringMatched(3)));
				y = Float
						.parseFloat(TO_ENG.replaceAll(GRIDS1.stringMatched(4)));
				if (!GRIDS2.search(str = br.readLine()))
					throw new ParseException("No second line found for GRID*:"
							+ CHEXA1.stringMatched(1), linenr);
				z = Float
						.parseFloat(TO_ENG.replaceAll(GRIDS2.stringMatched(1)));
				n = new _Node(x, y, z);
				nodes.put(GRIDS1.stringMatched(1).trim(), n); // Nodenr,
																// nodeobject
				g_n.addToGroup(n);
				J3D.add3D(n);
				headerMessage("Reading Node: " + i++);
			} else if (FGRID.search(str)) {
				linenr++;
				n = new _Node(Float.parseFloat(TO_ENG.replaceAll(FGRID
						.stringMatched(3))), Float.parseFloat(TO_ENG
						.replaceAll(FGRID.stringMatched(4))),
						Float.parseFloat(TO_ENG.replaceAll(FGRID
								.stringMatched(5))));
				nodes.put(FGRID.stringMatched(1).trim(), n); // Nodenr,
																// nodeobject
				g_n.addToGroup(n);
				J3D.add3D(n);
				headerMessage("Reading Node: " + i++);
			}

		}

		br.close();

		// Now, read the elements
		br = new BufferedReader(new FileReader(filename));
		linenr = 0;
		i = 0;

		while ((str = br.readLine()) != null) {
			linenr++;

			if (CBAR.search(str)) {
				for (int j = 0; j < 2; j++) {
					nn[j] = CBAR.stringMatched(j + 3).trim();
					if (nn[j] == null)
						throw new ParseException("No node number for element:"
								+ CBAR.stringMatched(1), linenr);
					nod[j] = (_Node) nodes.get(nn[j]);
					if (nod[j] == null)
						throw new ParseException(
								"No matching node found for element:"
										+ CBAR.stringMatched(1), linenr);
				}

				// Generate element
				el = new _Element2(nod[0], nod[1]);
				g_e.addToGroup(el);
				J3D.add3D(el);
				headerMessage("Reading CBAR: " + i++);
			} else if (CTRIA3.search(str)) {
				for (int j = 0; j < 3; j++) {
					nn[j] = CTRIA3.stringMatched(j + 3).trim();
					if (nn[j] == null)
						throw new ParseException("No node number for element:"
								+ CTRIA3.stringMatched(1), linenr);
					nod[j] = (_Node) nodes.get(nn[j]);
					if (nod[j] == null)
						throw new ParseException(
								"No matching node found for element:"
										+ CTRIA3.stringMatched(1), linenr);
				}

				// Generate element
				el = new _Element3(nod[0], nod[1], nod[2]);
				g_e.addToGroup(el);
				J3D.add3D(el);
				headerMessage("Reading CTRIA3: " + i++);
			} else if (CQUAD4.search(str)) {
				for (int j = 0; j < 4; j++) {
					nn[j] = CQUAD4.stringMatched(j + 3).trim();
					if (nn[j] == null)
						throw new ParseException("No node number for element:"
								+ CQUAD4.stringMatched(1), linenr);
					nod[j] = (_Node) nodes.get(nn[j]);
					if (nod[j] == null)
						throw new ParseException(
								"No matching node found for element:"
										+ CQUAD4.stringMatched(1), linenr);
				}

				// Generate element
				el = new _Element4(nod[0], nod[1], nod[2], nod[3],
						Canvas3D.MESH_Shell_BT_4);
				g_e.addToGroup(el);
				J3D.add3D(el);
				headerMessage("Reading CQUAD4: " + i++);
			} else if (CTETRA.search(str)) {
				for (int j = 0; j < 4; j++) {
					nn[j] = CTETRA.stringMatched(j + 3).trim();
					if (nn[j] == null)
						throw new ParseException("No node number for element:"
								+ CTETRA.stringMatched(1), linenr);
					nod[j] = (_Node) nodes.get(nn[j]);
					if (nod[j] == null)
						throw new ParseException(
								"No matching node found for element:"
										+ CTETRA.stringMatched(1), linenr);
				}

				// Generate element
				el = new _Element4(nod[0], nod[1], nod[2], nod[3],
						Canvas3D.MESH_Solid_Iso_4);
				g_e.addToGroup(el);
				J3D.add3D(el);
				headerMessage("Reading CTETRA: " + i++);
			} else if (CHEXA1.search(str)) {
				for (int j = 0; j < 6; j++) {
					nn[j] = CHEXA1.stringMatched(j + 3).trim();
					if (nn[j] == null)
						throw new ParseException("No node number for element:"
								+ CHEXA1.stringMatched(1), linenr);
					nod[j] = (_Node) nodes.get(nn[j]);
					if (nod[j] == null)
						throw new ParseException("No matching node " + nn[j]
								+ " found for element:"
								+ CHEXA1.stringMatched(1), linenr);
				}

				if (!CHEXA2.search(str = br.readLine()))
					throw new ParseException(
							"No last two nodes found for element:"
									+ CHEXA1.stringMatched(1), linenr);
				for (int j = 6; j < 8; j++) {
					nn[j] = CHEXA2.stringMatched(j - 5).trim();
					if (nn[j] == null)
						throw new ParseException("No node number for element:"
								+ CHEXA1.stringMatched(1), linenr);
					nod[j] = (_Node) nodes.get(nn[j]);
					if (nod[j] == null)
						throw new ParseException("No matching node " + nn[j]
								+ " found for element:"
								+ CHEXA1.stringMatched(1), linenr);
				}

				// Generate element
				el = new _Element8(nod[0], nod[1], nod[2], nod[3], nod[4],
						nod[5], nod[6], nod[7]);
				g_e.addToGroup(el);
				J3D.add3D(el);
				headerMessage("Reading CHEXA: " + i++);
			} else if (CPENTA.search(str)) {
				for (int j = 0; j < 6; j++) {
					nn[j] = CPENTA.stringMatched(j + 3).trim();
					if (nn[j] == null)
						throw new ParseException("No node number for element:"
								+ CPENTA.stringMatched(1), linenr);
					nod[j] = (_Node) nodes.get(nn[j]);
					if (nod[j] == null)
						throw new ParseException("No matching node " + nn[j]
								+ " found for element:"
								+ CPENTA.stringMatched(1), linenr);
				}

				// Generate element
				el = new _Element8(nod[0], nod[1], nod[2], nod[2], nod[3],
						nod[4], nod[5], nod[5]);
				g_e.addToGroup(el);
				J3D.add3D(el);
				headerMessage("Reading CPENTA: " + i++);
			}
		}

		headerMessage("File Read Successfully");
		br.close();

	}

	/**
	 * Reads a Gmsh version 2 mesh file and generates elements and nodes in the
	 * preprocessor
	 * 
	 * @param filename
	 */
	public void readUNV(String filename) throws FileNotFoundException,
			IOException, ParseException {

		// Define regexp patterns for the data to import

		Regex SETNODES = new Regex("^\\s*2411\\s*$");
		Regex SETELEMENTS = new Regex("^\\s*2412\\s*$");
		Regex END = new Regex("^\\s*-1\\s*$");

		Regex NODE1 = new Regex(
				"^([+\\-0-9 ]{10})([+\\-0-9 ]{10})([+\\-0-9 ]{10})([+\\-0-9 ]{1,10})\\s*$");
		Regex NODE2 = new Regex(
				"^([+\\-0-9.eED ]{25})([+\\-0-9.eED ]{25})([+\\-0-9.eED ]{1,25})\\s*$");

		Regex ELEMENT1 = new Regex(
				"^([0-9 ]{10})([0-9 ]{10})([0-9 ]{10})([0-9 ]{10})([0-9 ]{10})([0-9 ]{10})\\s*$");
		Regex ELEMENT2 = new Regex(
				"^([0-9 ]{10})([0-9 ]{10})([0-9 ]{10})?([0-9 ]{10})?([0-9 ]{10})?([0-9 ]{10})?([0-9 ]{10})?([0-9 ]{10})?\\s*$");

		Regex TO_ENG = new Regex("D", "E");

		String str;
		_Node n;
		Hashtable nodes = new Hashtable();
		int i, eltype, nnodes;
		boolean inblock = false;
		int linenr = 0;
		String[] nn = new String[30];
		_Node[] nod = new _Node[16];
		String elname;
		_Object o;

		BufferedReader br = new BufferedReader(new FileReader(filename));

		// Speed up the parsing
		SETNODES.optimize();
		SETELEMENTS.optimize();
		END.optimize();
		NODE1.optimize();
		NODE2.optimize();
		ELEMENT1.optimize();
		ELEMENT2.optimize();

		// Define group
		_Group g = new _Group(false, true);
		g.setName(filename);
		J3D.add3D(g);

		_Group g_n = new _Group(false, false);
		g_n.setName("Node");
		J3D.add3D(g_n);

		_Group g_e = new _Group(false, false);
		g_e.setName("Elem");
		J3D.add3D(g_e);

		g.addToGroup(g_n);
		g.addToGroup(g_e);

		// Start reading nodes
		i = 0;
		inblock = false;

		while ((str = br.readLine()) != null) {
			linenr++;

			if (SETNODES.search(str))
				inblock = true;
			if (END.search(str))
				inblock = false;

			if (inblock) {

				if (NODE1.search(str))
					nn[0] = NODE1.stringMatched(1).trim();

				if (NODE2.search(str)) {
					n = new _Node(Float.parseFloat(TO_ENG.replaceAll(NODE2
							.stringMatched(1))), Float.parseFloat(TO_ENG
							.replaceAll(NODE2.stringMatched(2))),
							Float.parseFloat(TO_ENG.replaceAll(NODE2
									.stringMatched(3))));
					nodes.put(nn[0], n); // Nodenr, nodeobject
					g_n.addToGroup(n);
					J3D.add3D(n);
					headerMessage("Reading Node: " + i++);
				}

			}
		}
		br.close();

		// Now, read the elements
		br = new BufferedReader(new FileReader(filename));
		linenr = 0;
		i = 0;
		nnodes = 0;
		elname = "";
		eltype = 0;

		while ((str = br.readLine()) != null) {
			linenr++;

			if (SETELEMENTS.search(str))
				inblock = true;
			if (END.search(str))
				inblock = false;

			if (inblock) {

				if (ELEMENT1.search(str)) {
					elname = ELEMENT1.stringMatched(1).trim();
					eltype = Integer.parseInt(ELEMENT1.stringMatched(2).trim());
					nnodes = Integer.parseInt(ELEMENT1.stringMatched(6).trim());
					str = br.readLine();
				}

				if (ELEMENT2.search(str) && eltype < 41) // Skip one line for
															// beam and rod
															// elements
					str = br.readLine();

				if (nnodes > 8)
					throw new ParseException(
							"Impact does not support higher order elements. Error at element:"
									+ elname, linenr);

				if (ELEMENT2.search(str) && nnodes <= 8) {
					for (int j = 0; j < nnodes; j++) {
						nn[j] = ELEMENT2.stringMatched(j + 1).trim();
						if (nn[j] == null)
							throw new ParseException(
									"No node number for element:" + elname,
									linenr);
						nod[j] = (_Node) nodes.get(nn[j]);
						if (nod[j] == null)
							throw new ParseException(
									"No matching node found for element:"
											+ elname, linenr);
					}

					// Generate element

					switch (eltype) {

					case 11:
						o = new _Element2(nod[0], nod[1]);
						J3D.add3D(o);
						g_e.addToGroup(o);
						headerMessage("Reading BAR: " + i++);
						break;

					case 31:
						o = new _Element2(nod[0], nod[1]);
						J3D.add3D(o);
						g_e.addToGroup(o);
						headerMessage("Reading PIPE: " + i++);
						break;

					case 41:
						o = new _Element3(nod[0], nod[1], nod[2]);
						J3D.add3D(o);
						g_e.addToGroup(o);
						headerMessage("Reading TRIA: " + i++);
						break;

					case 51:
						o = new _Element3(nod[0], nod[1], nod[2]);
						J3D.add3D(o);
						g_e.addToGroup(o);
						headerMessage("Reading TRIA: " + i++);
						break;

					case 61:
						o = new _Element3(nod[0], nod[1], nod[2]);
						J3D.add3D(o);
						g_e.addToGroup(o);
						headerMessage("Reading TRIA: " + i++);
						break;

					case 74:
						o = new _Element3(nod[0], nod[1], nod[2]);
						J3D.add3D(o);
						g_e.addToGroup(o);
						headerMessage("Reading TRIA: " + i++);
						break;

					case 91:
						o = new _Element3(nod[0], nod[1], nod[2]);
						J3D.add3D(o);
						g_e.addToGroup(o);
						headerMessage("Reading TRIA: " + i++);
						break;

					case 44:
						o = new _Element4(nod[0], nod[1], nod[2], nod[3],
								Canvas3D.MESH_Shell_BT_4);
						J3D.add3D(o);
						g_e.addToGroup(o);
						headerMessage("Reading QUAD: " + i++);
						break;

					case 54:
						o = new _Element4(nod[0], nod[1], nod[2], nod[3],
								Canvas3D.MESH_Shell_BT_4);
						J3D.add3D(o);
						g_e.addToGroup(o);
						headerMessage("Reading QUAD: " + i++);
						break;

					case 64:
						o = new _Element4(nod[0], nod[1], nod[2], nod[3],
								Canvas3D.MESH_Shell_BT_4);
						J3D.add3D(o);
						g_e.addToGroup(o);
						headerMessage("Reading QUAD: " + i++);
						break;

					case 84:
						o = new _Element4(nod[0], nod[1], nod[2], nod[3],
								Canvas3D.MESH_Shell_BT_4);
						J3D.add3D(o);
						g_e.addToGroup(o);
						headerMessage("Reading QUAD: " + i++);
						break;

					case 94:
						o = new _Element4(nod[0], nod[1], nod[2], nod[3],
								Canvas3D.MESH_Shell_BT_4);
						J3D.add3D(o);
						g_e.addToGroup(o);
						headerMessage("Reading QUAD: " + i++);
						break;

					case 111:
						o = new _Element4(nod[0], nod[1], nod[2], nod[3],
								Canvas3D.MESH_Solid_Iso_4);
						J3D.add3D(o);
						g_e.addToGroup(o);
						headerMessage("Reading TETRA: " + i++);
						break;

					case 112:
						o = new _Element8(nod[0], nod[1], nod[2], nod[2],
								nod[3], nod[4], nod[5], nod[5]);
						J3D.add3D(o);
						g_e.addToGroup(o);
						headerMessage("Reading WEDGE: " + i++);
						break;

					case 115:
						o = new _Element8(nod[0], nod[1], nod[2], nod[3],
								nod[4], nod[5], nod[6], nod[7]);
						J3D.add3D(o);
						g_e.addToGroup(o);
						headerMessage("Reading BRICK: " + i++);
						break;

					}

				}
			}
		}

		headerMessage("File Read Successfully");
		br.close();
	}

	public void readIN(String filename) throws FileNotFoundException,
			IOException, ParseException {
		Hashtable NDB = new Hashtable();
		Hashtable EDB = new Hashtable();
		Hashtable GDB = new Hashtable();
		Hashtable GRDB = new Hashtable();
		Hashtable MatDB = new Hashtable();
		Hashtable ConstDB = new Hashtable();
		Hashtable LoadDB = new Hashtable();
		String file_src;
		String ContpolsDB = "", TrackersDB = "";
		String st;
		boolean inblock = false;

		// Define group
		_Group g = new _Group(false, true);
		g.setName(filename);
		J3D.add3D(g);

		_Group g_n = new _Group(false, false);
		g_n.setName("Node");
		J3D.add3D(g_n);

		_Group g_e = new _Group(false, false);
		g_e.setName("Elem");
		J3D.add3D(g_e);

		_Group g_g = new _Group(false, false);
		g_g.setName("Geo");
		J3D.add3D(g_g);

		g.addToGroup(g_n);
		g.addToGroup(g_e);
		g.addToGroup(g_g);

		// Start parsing
		RandomAccessFile in = new RandomAccessFile(filename, "r");
		byte[] b = new byte[(int) in.length()];
		in.read(b, 0, (int) in.length());
		file_src = new String(b);

		file_src = file_src.replaceAll("\t", " ");

		// CONTROLS
		StringTokenizer stt = new StringTokenizer(file_src, "\n");
		while (stt.hasMoreTokens()) {
			st = stt.nextToken().trim().toUpperCase();
			if (st.startsWith("CONTROLS")) {
				ContpolsDB = st + "\n";
				while (stt.hasMoreTokens()) {
					st = stt.nextToken().trim().toUpperCase();
					if (st.startsWith("#") || st.length() == 0)
						continue;
					if (st.startsWith("GROUPS") || st.startsWith("NODES")
							|| st.startsWith("ELEMENTS")
							|| st.startsWith("MATERIALS")
							|| st.startsWith("TRACKERS")
							|| st.indexOf("BOUNDARY_CONDITION") != -1
							|| st.indexOf("RIGID_BODY") != -1
							|| st.indexOf("LOADS") != -1)
						break;
					ContpolsDB += st + "\n";
				}
			}
		}

		// TRACKERS
		stt = new StringTokenizer(file_src, "\n");
		while (stt.hasMoreTokens()) {
			st = stt.nextToken().trim().toUpperCase();
			if (st.startsWith("TRACKERS")) {
				TrackersDB = st + "\n";
				while (stt.hasMoreTokens()) {
					st = stt.nextToken().trim().toUpperCase();
					if (st.startsWith("#") || st.length() == 0)
						continue;
					if (st.startsWith("GROUPS") || st.startsWith("NODES")
							|| st.startsWith("ELEMENTS")
							|| st.startsWith("CONTROLS")
							|| st.startsWith("MATERIALS")
							|| st.indexOf("BOUNDARY_CONDITION") != -1
							|| st.indexOf("RIGID_BODY") != -1
							|| st.indexOf("LOADS") != -1)
						break;
					TrackersDB += st + "\n";
				}
			}

			// Material
			stt = new StringTokenizer(file_src, "\n");
			while (stt.hasMoreTokens()) {
				st = stt.nextToken().trim().toUpperCase();
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
					while (stt.hasMoreTokens()) {
						st = stt.nextToken().trim().toUpperCase();
						if (st.startsWith("#") || st.length() == 0
								|| st.startsWith("MATERIALS"))
							continue;
						if (st.startsWith("GROUPS") || st.startsWith("NODES")
								|| st.startsWith("ELEMENTS")
								|| st.startsWith("CONTROLS")
								|| st.startsWith("TRACKERS")
								|| st.indexOf("BOUNDARY_CONDITION") != -1
								|| st.indexOf("RIGID_BODY") != -1
								|| st.indexOf("LOADS") != -1)
							break;
						StringTokenizer stt1 = new StringTokenizer(st, " =\t");
						String mat_name = stt1.nextToken();
						String mat_discription = st
								.substring(mat_name.length());
						Material mat = new Material();
						mat.color = Color.gray;
						mat.description = mat_discription;
						mat.name = mat_name;
						mat.type = mat_type;
						if (!MatDB.containsKey(mat.name))
							MatDB.put(mat.name, mat);
					}
				}
			}

			// Constraints
			stt = new StringTokenizer(file_src, "\n");
			while (stt.hasMoreTokens()) {
				st = stt.nextToken().trim().toUpperCase();
				if (st.startsWith("CONSTRAINTS")) {
					String constr_type = null;
					if (st.indexOf("BOUNDARY_CONDITION") != -1) {
						constr_type = "BOUNDARY_CONDITION";
					} else if (st.indexOf("RIGID_BODY") != -1) {
						constr_type = "RIGID_BODY";
					}
					while (stt.hasMoreTokens()) {
						st = stt.nextToken().trim().toUpperCase();
						if (st.startsWith("#") || st.length() == 0
								|| st.startsWith("CONSTRAINTS"))
							continue;
						if (st.startsWith("GROUPS") || st.startsWith("NODES")
								|| st.startsWith("ELEMENTS")
								|| st.startsWith("CONTROLS")
								|| st.startsWith("TRACKERS")
								|| st.startsWith("MATERIALS")
								|| st.indexOf("LOADS") != -1)
							break;
						StringTokenizer stt1 = new StringTokenizer(st, " =\t");
						String constr_name = stt1.nextToken();
						String constr_discription = st.substring(constr_name
								.length());
						Constraints constr = new Constraints();
						constr.color = Color.red;
						constr.description = constr_discription;
						constr.name = constr_name;
						constr.type = constr_type;
						if (!ConstDB.containsKey(constr.name))
							ConstDB.put(constr.name, constr);
					}
				}
			}

			// Loads
			stt = new StringTokenizer(file_src, "\n");
			while (stt.hasMoreTokens()) {
				st = stt.nextToken().trim().toUpperCase();
				if (st.startsWith("LOADS")) {
					while (stt.hasMoreTokens()) {
						st = stt.nextToken().trim().toUpperCase();
						if (st.startsWith("#") || st.length() == 0
								|| st.startsWith("LOADS"))
							continue;
						if (st.startsWith("GROUPS") || st.startsWith("NODES")
								|| st.startsWith("ELEMENTS")
								|| st.startsWith("CONTROLS")
								|| st.startsWith("TRACKERS")
								|| st.startsWith("MATERIALS")
								|| st.indexOf("BOUNDARY_CONDITION") != -1
								|| st.indexOf("RIGID_BODY") != -1)
							break;
						StringTokenizer stt1 = new StringTokenizer(st, " =\t");
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
				}
			}

			// NODES
			stt = new StringTokenizer(file_src, "\n");
			while (stt.hasMoreTokens()) {
				st = stt.nextToken().trim().toUpperCase();
				if (st.equals("NODES")) {
					while (stt.hasMoreTokens()) {
						st = stt.nextToken().trim().toUpperCase();
						if (st.startsWith("GROUPS") || st.startsWith("LOADS")
								|| st.startsWith("ELEMENTS")
								|| st.startsWith("CONTROLS")
								|| st.startsWith("TRACKERS")
								|| st.startsWith("MATERIALS")
								|| st.indexOf("BOUNDARY_CONDITION") != -1
								|| st.indexOf("RIGID_BODY") != -1)
							break;
						if (st.startsWith("#") || st.length() == 0)
							continue;
						StringTokenizer stt1 = new StringTokenizer(st,
								" =XYZ\t");
						String key = stt1.nextToken();
						_Node nd = new _Node(
								Float.parseFloat(stt1.nextToken()),
								Float.parseFloat(stt1.nextToken()),
								Float.parseFloat(stt1.nextToken()));
						nd.set_Id(key);

						if (st.indexOf("CONSTRAINT") != -1) {
							String st1 = st
									.substring(st.indexOf("CONSTRAINT") + 10);
							stt1 = new StringTokenizer(st1, " \t");
							stt1.nextToken();
							nd.constraint = (Constraints) ConstDB.get(stt1
									.nextToken());
						}
						if (st.indexOf("LOAD") != -1) {
							String st1 = st.substring(st.indexOf("LOAD") + 4);
							stt1 = new StringTokenizer(st1, " \t");
							stt1.nextToken();
							nd.load = (Loads) LoadDB.get(stt1.nextToken());
						}
						NDB.put(key, nd);
					}
				}
			}

			// ELEMENTS
			stt = new StringTokenizer(file_src, "\n");
			while (stt.hasMoreTokens()) {
				st = stt.nextToken().trim().toUpperCase();
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
							continue;
						}

						if (st.startsWith("GROUPS") || st.startsWith("NODES")
								|| st.startsWith("CONTROLS")
								|| st.startsWith("TRACKERS")
								|| st.startsWith("MATERIALS")
								|| st.indexOf("BOUNDARY_CONDITION") != -1
								|| st.indexOf("RIGID_BODY") != -1
								|| st.indexOf("LOADS") != -1)
							break;
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
							el = new _Element2((_Node) NDB.get(arr[0] + ""),
									(_Node) NDB.get(arr[1] + ""), el_msh_type,
									(Material) MatDB.get(getValue(st_load,
											"MATERIAL")),
									java.lang.Float.parseFloat(getValue(
											st_load, "D")));
							((_Element2) el).readObject(st_load);
						} else if (el_msh_type == Canvas3D.MESH_Contact_Triangle) {
							Material mat = new Material();
							mat.color = Color.gray;
							mat.name = "null";
							MatDB.put(mat.name, mat);
							el = new _Element3((_Node) NDB.get(arr[0] + ""),
									(_Node) NDB.get(arr[1] + ""),
									(_Node) NDB.get(arr[2] + ""), el_msh_type,
									mat, java.lang.Float.parseFloat(getValue(
											st_load, "T")));
							((_Element3) el).readObject(st_load, LoadDB);
						} else if (el_msh_type == Canvas3D.MESH_Contact_Line) {
							Material mat = new Material();
							mat.color = Color.gray;
							mat.name = "null";
							MatDB.put(mat.name, mat);
							el = new _Element2((_Node) NDB.get(arr[0] + ""),
									(_Node) NDB.get(arr[1] + ""), el_msh_type,
									mat, java.lang.Float.parseFloat(getValue(
											st_load, "D")));
							((_Element2) el).readObject(st_load);
						} else if (el_msh_type == Canvas3D.MESH_Beam_2) {
							el = new _Element2((_Node) NDB.get(arr[0] + ""),
									(_Node) NDB.get(arr[1] + ""), el_msh_type,
									(Material) MatDB.get(getValue(st_load,
											"MATERIAL")),
									java.lang.Float.parseFloat(getValue(
											st_load, "D")));
							((_Element2) el).readObject(st_load);
						} else if (el_msh_type == Canvas3D.MESH_Beam_Spring_2) {
							// ---------------------------------
						} else if (el_msh_type == Canvas3D.MESH_Shell_BT_4) {
							el = new _Element4((_Node) NDB.get(arr[0] + ""),
									(_Node) NDB.get(arr[1] + ""),
									(_Node) NDB.get(arr[2] + ""),
									(_Node) NDB.get(arr[3] + ""), el_msh_type,
									(Material) MatDB.get(getValue(st_load,
											"MATERIAL")),
									java.lang.Float.parseFloat(getValue(
											st_load, "T")));
							((_Element4) el).readObject(st_load, LoadDB);
						} else if (el_msh_type == Canvas3D.MESH_Solid_Iso_4) {
							el = new _Element4((_Node) NDB.get(arr[0] + ""),
									(_Node) NDB.get(arr[1] + ""),
									(_Node) NDB.get(arr[2] + ""),
									(_Node) NDB.get(arr[3] + ""), el_msh_type,
									(Material) MatDB.get(getValue(st_load,
											"MATERIAL")), 0);
							try {
								((_Element4) el).NIP = new Integer(getValue(
										st_load, "NIP"));
							} catch (Exception e1) {
							}
						} else if (el_msh_type == Canvas3D.MESH_Shell_C0_3) {
							el = new _Element3((_Node) NDB.get(arr[0] + ""),
									(_Node) NDB.get(arr[1] + ""),
									(_Node) NDB.get(arr[2] + ""), el_msh_type,
									(Material) MatDB.get(getValue(st_load,
											"MATERIAL")),
									java.lang.Float.parseFloat(getValue(
											st_load, "T")));
							((_Element3) el).readObject(st_load, LoadDB);
						} else if (el_msh_type == Canvas3D.MESH_Solid_Iso_6) {
							el = new _Element8((_Node) NDB.get(arr[0] + ""),
									(_Node) NDB.get(arr[1] + ""),
									(_Node) NDB.get(arr[2] + ""),
									(_Node) NDB.get(arr[3] + ""),
									(_Node) NDB.get(arr[4] + ""),
									(_Node) NDB.get(arr[5] + ""),
									(_Node) NDB.get(arr[6] + ""),
									(_Node) NDB.get(arr[7] + ""),
									(Material) MatDB.get(getValue(st_load,
											"MATERIAL")));
							((_Element8) el).readObject(st_load);
						}

						if (el != null) {
							el.set_Id(key);
							EDB.put(key, el);
						}
					}
				}
			}
		}

		// GEOMETRY

		// POINTS
		inblock = false;
		String geom = new String();
		Regex gtype = new Regex("^(?i) *GEOMETRY *OF *TYPE *POINT *$");
		Regex token = new Regex("^(?i) *[0-9]+");
		_Geometry geo;

		gtype.optimize();

		stt = new StringTokenizer(file_src, "\n");
		while (stt.hasMoreTokens()) {
			st = stt.nextToken();

			block_one: if (inblock) {

				if (st.startsWith("NODES") || st.startsWith("GEOMETRY")
						|| st.startsWith("GROUPS") || st.startsWith("LOADS")
						|| st.startsWith("ELEMENTS")
						|| st.startsWith("CONTROLS")
						|| st.startsWith("TRACKERS")
						|| st.startsWith("MATERIALS")
						|| st.indexOf("BOUNDARY_CONDITION") != -1
						|| st.indexOf("RIGID_BODY") != -1) {
					inblock = false;
					break block_one;
				}

				if (!token.search(st))
					continue;

				geo = new _Point(false);
				((_Point) geo).readObject(st);
				GDB.put(geo.get_Id(), geo);
				J3D.add3D(geo);

			} // end inblock

			if (gtype.search(st)) {
				inblock = true;
			}
		} // End while geometry block

		// CURVES
		inblock = false;
		geom = new String();
		gtype = new Regex("^(?i) *GEOMETRY *OF *TYPE *(ARC|SPLINE) *$");

		gtype.optimize();

		stt = new StringTokenizer(file_src, "\n");
		while (stt.hasMoreTokens()) {
			st = stt.nextToken();

			block_two: if (inblock) {

				if (st.startsWith("NODES") || st.startsWith("GEOMETRY")
						|| st.startsWith("GROUPS") || st.startsWith("LOADS")
						|| st.startsWith("ELEMENTS")
						|| st.startsWith("CONTROLS")
						|| st.startsWith("TRACKERS")
						|| st.startsWith("MATERIALS")
						|| st.indexOf("BOUNDARY_CONDITION") != -1
						|| st.indexOf("RIGID_BODY") != -1) {
					inblock = false;
					break block_two;
				}

				if (!token.search(st))
					continue;

				if (geom.equals("SPLINE")) {
					geo = new _Spline(false, J3D);
					((_Spline) geo).readObject(st, GDB);
					GDB.put(geo.get_Id(), geo);
					J3D.add3D(geo);
				}

				if (geom.equals("ARC")) {
					geo = new _Arc(false, J3D);
					((_Arc) geo).readObject(st, GDB);
					GDB.put(geo.get_Id(), geo);
					J3D.add3D(geo);
				}

			} // end inblock

			if (gtype.search(st)) {
				inblock = true;
				geom = gtype.stringMatched(1).toUpperCase();
			}
		} // End while geometry block

		// SURFACES
		inblock = false;
		geom = new String();
		gtype = new Regex(
				"^(?i) *GEOMETRY *OF *TYPE *(SURFBIL|SURFDIR|SURFFILL|SURFREV|SURFRUL) *$");

		gtype.optimize();

		stt = new StringTokenizer(file_src, "\n");
		while (stt.hasMoreTokens()) {
			st = stt.nextToken();

			block_three: if (inblock) {

				if (st.startsWith("NODES") || st.startsWith("GEOMETRY")
						|| st.startsWith("GROUPS") || st.startsWith("LOADS")
						|| st.startsWith("ELEMENTS")
						|| st.startsWith("CONTROLS")
						|| st.startsWith("TRACKERS")
						|| st.startsWith("MATERIALS")
						|| st.indexOf("BOUNDARY_CONDITION") != -1
						|| st.indexOf("RIGID_BODY") != -1) {
					inblock = false;
					break block_three;
				}

				if (!token.search(st))
					continue;

				if (geom.equals("SURFBIL")) {
					geo = new _SurfBil(false, J3D);
					((_SurfBil) geo).readObject(st, GDB);
					GDB.put(geo.get_Id(), geo);
					J3D.add3D(geo);
				}

				if (geom.equals("SURFDIR")) {
					geo = new _SurfDir(false, J3D);
					((_SurfDir) geo).readObject(st, GDB);
					GDB.put(geo.get_Id(), geo);
					J3D.add3D(geo);
				}

				if (geom.equals("SURFFILL")) {
					geo = new _SurfFill(false, J3D);
					((_SurfFill) geo).readObject(st, GDB);
					GDB.put(geo.get_Id(), geo);
					J3D.add3D(geo);
				}

				if (geom.equals("SURFREV")) {
					geo = new _SurfRev(false, J3D);
					((_SurfRev) geo).readObject(st, GDB);
					GDB.put(geo.get_Id(), geo);
					J3D.add3D(geo);
				}

				if (geom.equals("SURFRUL")) {
					geo = new _SurfRul(false, J3D);
					((_SurfRul) geo).readObject(st, GDB);
					GDB.put(geo.get_Id(), geo);
					J3D.add3D(geo);
				}

			} // end inblock

			if (gtype.search(st)) {
				inblock = true;
				geom = gtype.stringMatched(1).toUpperCase();
			}
		} // End while geometry block

		// GROUPS
		Regex Group1 = new Regex(
				"^(?i) *([0-9]+) *NAME *= *([a-zA-Z\\/.0-9_]*)");
		_Group ng;

		// Groups defined in two passes
		for (int i = 0; i < 2; i++) {
			inblock = false;
			stt = new StringTokenizer(file_src, "\n");

			while (stt.hasMoreTokens()) {
				st = stt.nextToken().trim().toUpperCase();

				if (st.startsWith("GROUPS"))
					inblock = true;
				if (st.startsWith("NODES") || st.startsWith("LOADS")
						|| st.startsWith("ELEMENTS")
						|| st.startsWith("CONTROLS")
						|| st.startsWith("TRACKERS")
						|| st.startsWith("MATERIALS")
						|| st.indexOf("BOUNDARY_CONDITION") != -1
						|| st.indexOf("RIGID_BODY") != -1)
					inblock = false;

				if (inblock)
					if (Group1.search(st)) {
						// Make a new group
						if (i == 0)
							ng = new _GroupUserDefined(false, true);
						else
							ng = (_Group) GRDB.get(Group1.stringMatched(1)); // Get
																				// group
																				// defined
																				// in
																				// first
																				// pass

						ng.readObject((i == 0), st, NDB, EDB, GDB, GRDB);

						if (i == 0) {
							GRDB.put(ng.get_Id(), ng); // Key is Id defined in
														// file
							J3D.add3D(ng); // This will set a new Id
						}
					}
			}
		}

		// Finally, feed the database

		Object q;
		Iterator it = NDB.keySet().iterator();
		while (it.hasNext()) {
			J3D.add3D((_Node) NDB.get(q = it.next()));
			g_n.addToGroup((_Node) NDB.get(q));
		}

		it = EDB.keySet().iterator();
		while (it.hasNext()) {
			J3D.add3D((_Element) EDB.get(q = it.next()));
			g_e.addToGroup((_Element) EDB.get(q));
		}

		it = GDB.keySet().iterator();
		while (it.hasNext()) {
			g_g.addToGroup((_Geometry) GDB.get(it.next()));
		}

	}

	private String getValue(String src, String key) {
		key = " " + key + " ";
		if (src.indexOf(key) == -1)
			return null;
		src = src.substring(src.indexOf(key) + key.length());
		StringTokenizer stt = new StringTokenizer(src, " =\t\n");
		return stt.nextToken();
	}

}
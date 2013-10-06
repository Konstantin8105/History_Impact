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

import java.awt.event.*;
import javax.swing.table.*;
import java.util.*;
import javax.swing.tree.*;

public class PreProcessor_SetLoads extends JPanel {
	Canvas3D J3D;
	Hashtable db = new Hashtable();
	PreProcessor PreP;
	BorderLayout borderLayout1 = new BorderLayout();
	private JPanel jPanel3 = new JPanel();
	private JButton b_set = new JButton();
	private JScrollPane jScrollPane1 = new JScrollPane();
	private String[] LoadHdr = { "Name", "Color", "Description" };
	private TableModel LoadModel = new AbstractTableModel() {
		public int getColumnCount() {
			return 3;
		}

		public int getRowCount() {
			return db.size();
		}

		public Object getValueAt(int row, int col) {
			int i = 0;
			for (Enumeration en = db.keys(); en.hasMoreElements();) {
				String key = en.nextElement() + "";
				Loads ld = (Loads) db.get(key);
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
			return false;
		}

		public void setValueAt(Object aValue, int row, int col) {
			Loads ld = (Loads) db.get(getValueAt(row, 0));
			if (col == 1)
				ld.color = (Color) aValue;
			if (col == 3)
				ld.description = aValue + "";
		}

		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}
	};
	private JLabel jLabel1 = new JLabel();
	private JTable LoadTable = new JTable(LoadModel) {
		public TableCellRenderer getCellRenderer(int row, int column) {
			if (column == 1) {
				DefaultTableCellRenderer render = new DefaultTableCellRenderer();
				render.setBackground(((Loads) db.get(getValueAt(row, 0))).color);
				render.setForeground(((Loads) db.get(getValueAt(row, 0))).color);
				return render;
			} else
				return super.getCellRenderer(row, column);
		}
	};
	private JButton b_remove = new JButton();

	public PreProcessor_SetLoads(Canvas3D j3d, PreProcessor p) {
		try {
			J3D = j3d;
			PreP = p;
			db = PreP.LoadDB;
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		LoadTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		LoadTable.getColumnModel().getColumn(1).setPreferredWidth(20);
		LoadTable.getColumnModel().getColumn(2).setPreferredWidth(200);
		LoadTable.setSelectionMode(0);
		this.setLayout(borderLayout1);
		b_set.setText("Set");
		b_set.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_set_actionPerformed(e);
			}
		});
		jScrollPane1.setPreferredSize(new Dimension(200, 200));
		jLabel1.setForeground(Color.blue);
		jLabel1.setText("Loads");
		b_remove.setText("Remove");
		b_remove.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_remove_actionPerformed(e);
			}
		});
		this.add(jPanel3, BorderLayout.SOUTH);
		jPanel3.add(b_set, null);
		jPanel3.add(b_remove, null);
		this.add(jScrollPane1, BorderLayout.CENTER);
		jScrollPane1.getViewport().add(LoadTable, null);
		this.add(jLabel1, BorderLayout.NORTH);
		this.validate();
	}

	public void error(Object st) {
		JOptionPane.showMessageDialog(this, "Error: " + st, "Error!",
				JOptionPane.ERROR_MESSAGE);
		if (st instanceof Exception)
			((Exception) st).printStackTrace();
		else
			System.out.println("Error: " + st);
	}

	void b_set_actionPerformed(ActionEvent e) {
		Loads ld = (Loads) db.get(LoadTable.getValueAt(
				LoadTable.getSelectedRow(), 0));
		if (ld == null)
			return;

		_Object[] obj = J3D.getSelectedObjects3D();
		for (int i = 0; i < obj.length; i++) {
			if (obj[i] instanceof _Node) {
				((_Node) obj[i]).load = ld;
			} else if (obj[i] instanceof _Element3) {
				((_Element3) obj[i]).load = ld;
			} else if (obj[i] instanceof _Element4) {
				((_Element4) obj[i]).load = ld;
			}
		}

		J3D.tree_reset();
		J3D.view_reset();
	}

	void b_remove_actionPerformed(ActionEvent e) {
		TreePath[] tp = PreP.Tree.getSelectionPaths();
		if (tp == null)
			return;
		for (int i = 0; i < tp.length; i++) {
			DefaultMutableTreeNode tn = (DefaultMutableTreeNode) tp[i]
					.getLastPathComponent();
			Object obj = tn.getUserObject();
			if (obj instanceof _Node) {
				((_Node) obj).load = null;
			} else if (obj instanceof _Element3) {
				((_Element3) obj).load = null;
			} else if (obj instanceof _Element4) {
				((_Element4) obj).load = null;
			}
		}
		J3D.tree_reset();
		J3D.view_reset();
	}

}

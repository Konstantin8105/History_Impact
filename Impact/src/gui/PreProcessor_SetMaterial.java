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

public class PreProcessor_SetMaterial extends JPanel {
	Canvas3D J3D;
	Hashtable db = new Hashtable();
	PreProcessor PreP;
	BorderLayout borderLayout1 = new BorderLayout();
	private JPanel jPanel3 = new JPanel();
	private JButton b_set = new JButton();
	private JScrollPane jScrollPane1 = new JScrollPane();
	private String[] MatHdr = { "Name", "Color", "Type", "Description" };
	private TableModel MatModel = new AbstractTableModel() {
		public int getColumnCount() {
			return 4;
		}

		public int getRowCount() {
			return db.size();
		}

		public Object getValueAt(int row, int col) {
			int i = 0;
			for (Enumeration en = db.keys(); en.hasMoreElements();) {
				String key = en.nextElement() + "";
				Material mat = (Material) db.get(key);
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
			return false;
		}

		public void setValueAt(Object aValue, int row, int col) {
			Material mat = (Material) db.get(getValueAt(row, 0));
			if (col == 1)
				mat.color = (Color) aValue;
			if (col == 3)
				mat.description = aValue + "";
		}

		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}
	};
	private JLabel jLabel1 = new JLabel();
	private JTable MatTable = new JTable(MatModel) {
		public TableCellRenderer getCellRenderer(int row, int column) {
			if (column == 1) {
				DefaultTableCellRenderer render = new DefaultTableCellRenderer();
				render.setBackground(((Material) db.get(getValueAt(row, 0))).color);
				render.setForeground(((Material) db.get(getValueAt(row, 0))).color);
				return render;
			} else
				return super.getCellRenderer(row, column);
		}

		public TableCellEditor getCellEditor(int row, int column) {
			if (column == 1) {
				Color cl = JColorChooser.showDialog(this, "Pick a Color",
						((Material) db.get(getValueAt(row, 0))).color);
				if (cl != null)
					((Material) db.get(getValueAt(row, 0))).color = cl;
			}
			return super.getCellEditor(row, column);
		}
	};

	public PreProcessor_SetMaterial(Canvas3D j3d, PreProcessor p) {
		try {
			J3D = j3d;
			PreP = p;
			db = PreP.MatDB;
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		MatTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		MatTable.getColumnModel().getColumn(1).setPreferredWidth(20);
		MatTable.getColumnModel().getColumn(3).setPreferredWidth(200);
		MatTable.setSelectionMode(0);
		this.setLayout(borderLayout1);
		b_set.setText("Set");
		b_set.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_set_actionPerformed(e);
			}
		});
		jScrollPane1.setPreferredSize(new Dimension(200, 200));
		jLabel1.setForeground(Color.blue);
		jLabel1.setText("Material");
		this.add(jPanel3, BorderLayout.SOUTH);
		jPanel3.add(b_set, null);
		this.add(jScrollPane1, BorderLayout.CENTER);
		jScrollPane1.getViewport().add(MatTable, null);
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
		Material mat = (Material) db.get(MatTable.getValueAt(
				MatTable.getSelectedRow(), 0));
		if (mat == null)
			return;

		_Object[] obj = J3D.getSelectedObjects3D();
		for (int i = 0; i < obj.length; i++) {
			if (obj[i] instanceof _Element8) {
				((_Element8) obj[i]).material = mat;
			} else if (obj[i] instanceof _Element3) {
				((_Element3) obj[i]).material = mat;
			} else if (obj[i] instanceof _Element4) {
				((_Element4) obj[i]).material = mat;
			} else if (obj[i] instanceof _Element2) {
				((_Element2) obj[i]).material = mat;
			}
		}
		J3D.tree_reset();
		J3D.view_reset();
	}

}

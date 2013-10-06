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

import util.PngEncoderB;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.geom.Line2D;
import java.io.*;
import java.util.*;
import java.awt.image.*;

import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;

import java.awt.event.*;

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy
 */

public class Graph extends JPanel {
	public static final String ver = "Graph";
	BufferedImage offscreen;
	Graphics2D offgraphics;
	private Properties ConfDB = new Properties();
	JSplitPane sp = new JSplitPane();
	ImageIcon img_new = new ImageIcon(Graph.class.getResource("new.gif"));
	ImageIcon img_open = new ImageIcon(Graph.class.getResource("open.gif"));
	ImageIcon img_add = new ImageIcon(Graph.class.getResource("add.gif"));
	ImageIcon img_del = new ImageIcon(Graph.class.getResource("del.gif"));
	ImageIcon img_union = new ImageIcon(Graph.class.getResource("union.png"));
	ImageIcon img_subtract = new ImageIcon(
			Graph.class.getResource("subtract.png"));
	ImageIcon img_multiply = new ImageIcon(
			Graph.class.getResource("multiply.png"));
	ImageIcon img_multiply_with_factor = new ImageIcon(
			Graph.class.getResource("multiply_k.png"));
	ImageIcon img_divide = new ImageIcon(Graph.class.getResource("divide.png"));
	ImageIcon img_y_vs_y = new ImageIcon(Graph.class.getResource("y_vs_y.png"));
	ImageIcon img_save = new ImageIcon(Graph.class.getResource("save.gif"));
	ImageIcon img_saveall = new ImageIcon(
			Graph.class.getResource("saveall.gif"));
	ImageIcon img_flip = new ImageIcon(Graph.class.getResource("flip.png"));
	ImageIcon img_flop = new ImageIcon(Graph.class.getResource("flop.png"));
	float scalex = 1, scaley = 1;
	float dx = 0;
	float dy = 0;
	float minx = 0, miny = 0, maxy = 0, maxx = 0;
	float factor = -1;
	String[][] table_dat = new String[0][0];
	Object[][] table_dat_info = new Object[0][3];
	String[] table_hdr_info = { "Description", "Color", "Show" };
	private TableModel tableModel = new AbstractTableModel() {
		public int getColumnCount() {
			return table_dat[0].length;
		}

		public int getRowCount() {
			return table_dat.length;
		}

		public Object getValueAt(int row, int col) {
			return table_dat[row][col] + "";
		}

		public String getColumnName(int col) {
			if (((int) (col / 2)) * 2 == col)
				return "X";
			else
				return "Y";
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}
	};
	private TableModel tableModelInfo = new AbstractTableModel() {
		public int getColumnCount() {
			return 3;
		}

		public int getRowCount() {
			return table_dat_info.length;
		}

		public Object getValueAt(int row, int col) {
			if (col == 1)
				return "";
			else
				return table_dat_info[row][col];
		}

		public String getColumnName(int col) {
			return table_hdr_info[col];
		}

		public boolean isCellEditable(int row, int col) {
			if (col == 0)
				return false;
			else
				return true;
		}

		public void setValueAt(Object aValue, int row, int col) {
			table_dat_info[row][col] = aValue;
			min_max();
			p_chart.repaint();
		}

		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}
	};
	JTextField tf_maxy = new JTextField();
	JTextField tf_miny = new JTextField();
	JTextField tf_minx = new JTextField();
	JTextField tf_maxx = new JTextField();
	JPanel p_chart = new JPanel() {
		public void paint(Graphics g) {
			super.paint(g);
			offscreen = (BufferedImage) createImage(p_chart.getWidth() - 100,
					p_chart.getHeight() - 50);
			offgraphics = (Graphics2D) offscreen.getGraphics();
			p_chart.setBackground(Color.white);
			tf_maxy.setLocation(0, 0);
			tf_miny.setLocation(0, p_chart.getHeight() - 40);
			tf_minx.setLocation(80, p_chart.getHeight() - 30);
			tf_maxx.setLocation(p_chart.getWidth() - 80,
					p_chart.getHeight() - 30);
			tf_maxy.setText(maxy + "");
			tf_miny.setText(miny + "");
			tf_maxx.setText(maxx + "");
			tf_minx.setText(minx + "");
			Graphics2D g2 = (Graphics2D) g;
			g2.drawLine(100, 0, 100, p_chart.getHeight() - 50);
			g2.drawLine(100, p_chart.getHeight() - 50, p_chart.getWidth(),
					p_chart.getHeight() - 50);
			offgraphics.setColor(Color.white);
			offgraphics.fillRect(0, 0, offscreen.getWidth(),
					offscreen.getHeight());
			offgraphics.setColor(Color.black);
			float stepx = (p_chart.getWidth() - 100) / 10f;
			int j = 0;
			for (float i = 100; i <= p_chart.getWidth(); i += stepx) {
				offgraphics.draw(new Line2D.Float(i - 100, 0, i - 100, p_chart
						.getHeight() - 50));
				String st = "" + (minx + (maxx - minx) / 10f * j);
				String st1 = "";
				if (8 < st.length() && st.indexOf("E") != -1)
					st1 = st.substring(st.indexOf("E"), st.length());
				int index = st.indexOf(".");
				if (index != -1 && index + 8 < st.length())
					st = st.substring(0, index + 8);
				if (st1.length() != 0)
					st = st.substring(0, st.length() - st1.length()) + st1;
				g2.drawString(st, i, p_chart.getHeight() - 35);
				j++;
			}
			float stepy = (p_chart.getHeight() - 50) / 10f;
			j = 0;
			for (float i = 0; i <= p_chart.getHeight() - 50; i += stepy) {
				offgraphics
						.draw(new Line2D.Double(0, i, p_chart.getWidth(), i));
				g2.drawString("" + (maxy - (maxy - miny) / 10f * j), 5, i);
				j++;
			}
			scalex = offscreen.getWidth() / (maxx - minx);
			scaley = offscreen.getHeight() / (maxy - miny);
			dx = -minx * scalex;
			dy = offscreen.getHeight() + miny * scaley;
			if (table_dat.length > 0) {
				offgraphics.setStroke(new BasicStroke(2));
				for (j = 0; j < table_dat[0].length; j += 2) {
					if (((Boolean) table_dat_info[j / 2][2]).booleanValue()) {
						offgraphics.setColor((Color) table_dat_info[j / 2][1]);
						for (int i = 1; i < table_dat.length; i++) {
							try {
								offgraphics
										.drawLine(
												(int) (Float
														.parseFloat(table_dat[i - 1][j])
														* scalex + dx),
												(int) (dy - Float
														.parseFloat(table_dat[i - 1][j + 1])
														* scaley),
												(int) (Float
														.parseFloat(table_dat[i][j])
														* scalex + dx),
												(int) (dy - Float
														.parseFloat(table_dat[i][j + 1])
														* scaley));
							} catch (Exception e) {
							}
						}
					}
				}
			}
			g2.drawImage(offscreen, 100, 0, this);
		}
	};
	JPanel jPanel1 = new JPanel();
	BorderLayout borderLayout2 = new BorderLayout();
	JSplitPane spanel = new JSplitPane();
	JPanel p_table1 = new JPanel();
	JPanel p_table2 = new JPanel();
	BorderLayout borderLayout1 = new BorderLayout();
	BorderLayout borderLayout3 = new BorderLayout();
	JScrollPane s_table_info = new JScrollPane();
	JTable table_info = new JTable(tableModelInfo);
	JTable table = new JTable();
	JScrollPane s_table = new JScrollPane();
	JScrollPane s_ta_info = new JScrollPane();
	JTextArea ta_info = new JTextArea();
	BorderLayout borderLayout4 = new BorderLayout();
	JToolBar ToolBar = new JToolBar();
	JButton b_new = new JButton();
	JButton b_add = new JButton();
	JButton b_del = new JButton();
	JButton b_save = new JButton();
	JButton b_saveall = new JButton();
	JButton b_union = new JButton();
	JButton b_subtract = new JButton();
	JButton b_multiply = new JButton();
	JButton b_divide = new JButton();
	JButton b_multiply_k = new JButton();
	JButton b_y_vs_y = new JButton();
	JButton b_inv = new JButton();
	JTextField tf_factor = new JTextField();

	boolean inverted = false;

	public Graph() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {

		load_configuration();

		sp.setOrientation(JSplitPane.VERTICAL_SPLIT);
		sp.setOneTouchExpandable(true);
		sp.setRightComponent(p_chart);
		tf_maxy.setBounds(new Rectangle(9, 7, 63, 21));
		tf_maxy.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				tf_maxy_keyPressed(e);
			}
		});
		p_chart.setLayout(null);
		tf_miny.setBounds(new Rectangle(8, 60, 63, 21));
		tf_miny.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				tf_miny_keyPressed(e);
			}
		});
		tf_minx.setBounds(new Rectangle(38, 107, 63, 21));
		tf_minx.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				tf_minx_keyPressed(e);
			}
		});
		tf_maxx.setBounds(new Rectangle(298, 109, 63, 21));
		tf_maxx.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				tf_maxx_keyPressed(e);
			}
		});
		jPanel1.setBounds(new Rectangle(109, 9, 168, 119));
		jPanel1.setLayout(borderLayout2);
		p_table1.setLayout(borderLayout1);
		p_table2.setLayout(borderLayout3);
		s_table_info.setPreferredSize(new Dimension(200, 404));
		spanel.setOneTouchExpandable(true);

		tf_factor.setText("-1");
		tf_factor.setPreferredSize(new Dimension(30, 21));
		tf_factor.setMinimumSize(new Dimension(30, 21));
		tf_factor.setMaximumSize(new Dimension(30, 30));

		ta_info.setRows(5);
		ta_info.setEditable(false);
		this.setLayout(borderLayout4);
		b_new.setToolTipText("New from PostProcessor");
		b_new.setIcon(img_new);
		b_new.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new_action();
			}
		});
		ToolBar.setFloatable(false);
		b_add.setToolTipText("Open from .trk file");
		b_add.setIcon(img_add);
		b_add.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				add_action();
			}
		});
		b_del.setToolTipText("Remove");
		b_del.setIcon(img_del);
		b_del.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				del_action();
			}
		});
		b_save.setToolTipText("Save image");
		b_save.setIcon(img_save);
		b_save.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save_action();
			}
		});
		b_saveall.setToolTipText("Save all");
		b_saveall.setIcon(img_saveall);
		b_saveall.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveall_action();
			}
		});
		b_union.setToolTipText("Add");
		b_union.setIcon(img_union);
		b_union.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_union_actionPerformed(e);
			}
		});
		b_subtract.setToolTipText("Subtract");
		b_subtract.setIcon(img_subtract);
		b_subtract.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_subtract_actionPerformed(e);
			}
		});
		b_multiply.setToolTipText("Multiply Y1 with Y2");
		b_multiply.setIcon(img_multiply);
		b_multiply.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_multiply_actionPerformed(e);
			}
		});
		b_multiply_k.setToolTipText("Multiply with factor");
		b_multiply_k.setIcon(img_multiply_with_factor);
		b_multiply_k.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_multiply_with_K_actionPerformed(e);
			}
		});
		tf_factor.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				tf_factor_keyPressed(e);
			}
		});

		b_divide.setToolTipText("Divide Y1 with Y2");
		b_divide.setIcon(img_divide);
		b_divide.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_divide_actionPerformed(e);
			}
		});
		b_y_vs_y.setToolTipText("Plot Y1 vs Y2");
		b_y_vs_y.setIcon(img_y_vs_y);
		b_y_vs_y.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_value_vs_value_actionPerformed(e);
			}
		});
		b_inv.setToolTipText("Invert order of selection");
		b_inv.setIcon(img_flip);
		b_inv.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_inv_actionPerformed(e);
			}
		});
		sp.add(p_chart, JSplitPane.BOTTOM);
		p_chart.add(tf_maxy, null);
		p_chart.add(tf_miny, null);
		p_chart.add(tf_minx, null);
		p_chart.add(tf_maxx, null);
		p_chart.add(jPanel1, null);
		sp.add(spanel, JSplitPane.TOP);
		spanel.add(p_table1, JSplitPane.LEFT);
		p_table1.add(s_table_info, BorderLayout.CENTER);
		p_table1.add(s_ta_info, BorderLayout.SOUTH);
		s_ta_info.getViewport().add(ta_info, null);
		s_table_info.getViewport().add(table_info, null);
		spanel.add(p_table2, JSplitPane.RIGHT);
		p_table2.add(s_table, BorderLayout.CENTER);
		this.add(ToolBar, BorderLayout.NORTH);
		ToolBar.add(b_new, null);
		ToolBar.add(b_add, null);
		ToolBar.add(b_del, null);
		ToolBar.add(b_save, null);
		ToolBar.add(b_saveall, null);
		ToolBar.addSeparator();
		ToolBar.add(b_inv, null);
		ToolBar.addSeparator();
		ToolBar.add(b_union, null);
		ToolBar.add(b_subtract, null);
		ToolBar.add(b_multiply, null);
		ToolBar.add(b_divide, null);
		ToolBar.addSeparator();
		ToolBar.add(b_multiply_k, null);
		ToolBar.add(tf_factor, null);
		ToolBar.addSeparator();
		ToolBar.add(b_y_vs_y, null);
		s_table.getViewport().add(table, null);
		this.add(sp, BorderLayout.CENTER);
		sp.setDividerLocation(200);
		spanel.setDividerLocation(250);
	}

	public void error(Object st) {
		JOptionPane.showMessageDialog(null, "Error: " + st, "Error!",
				JOptionPane.ERROR_MESSAGE);
		System.out.println("Error: " + st);
	}

	public void openXY(String file) {
		try {
			String st_info = "";
			String dat;
			Vector v = new Vector();
			RandomAccessFile in = new RandomAccessFile(file, "r");
			while ((dat = in.readLine()) != null) {
				dat = dat.trim();
				if (dat.startsWith("#") || dat.length() == 0)
					st_info += dat.replace('\t', ' ') + "\n";
				else {
					StringTokenizer st_t = new StringTokenizer(dat);
					v.addElement(st_t.nextElement());
					v.addElement(st_t.nextElement());
				}
			}
			in.close();
			String[][] tmp_dat;
			if (table_dat.length == 0)
				tmp_dat = new String[v.size() / 2][2];
			else
				tmp_dat = new String[Math.max(table_dat.length, v.size() / 2)][table_dat[0].length + 2];
			for (int i = 0; i < tmp_dat.length; i++)
				for (int j = 0; j < tmp_dat[0].length; j++)
					tmp_dat[i][j] = "";
			for (int i = 0; i < tmp_dat.length; i++) {
				for (int j = 0; j < 2; j++) {
					try {
						Object obj = v.elementAt(i * 2 + j);
						if (table_dat.length == 0)
							tmp_dat[i][j] = "" + obj;
						else
							tmp_dat[i][table_dat[0].length + j] = "" + obj;
					} catch (Exception e1) {
					}
				}
			}
			for (int i = 0; i < table_dat.length; i++) {
				for (int j = 0; j < table_dat[0].length; j++) {
					tmp_dat[i][j] = table_dat[i][j];
				}
			}
			table_dat = tmp_dat;
			Object[][] tmp_info;
			if (table_dat_info.length == 0) {
				tmp_info = new Object[1][3];
				tmp_info[0][0] = st_info;
				tmp_info[0][1] = Color.black;
				tmp_info[0][2] = new Boolean(true);
			} else {
				tmp_info = new Object[table_dat_info.length + 1][3];
				tmp_info[table_dat_info.length][0] = st_info;
				tmp_info[table_dat_info.length][1] = this
						.getColorLine(tmp_info.length);
				tmp_info[table_dat_info.length][2] = new Boolean(true);
			}
			for (int i = 0; i < table_dat_info.length; i++) {
				for (int j = 0; j < 3; j++) {
					tmp_info[i][j] = table_dat_info[i][j];
				}
			}
			table_dat_info = tmp_info;
			upd();
		} catch (Exception e) {
			error(e);
		}
	}

	public void addXY(String[][] arr, String st_info) {
		try {
			String[][] tmp_dat;
			if (table_dat.length == 0)
				tmp_dat = new String[arr.length][2];
			else
				tmp_dat = new String[Math.max(table_dat.length, arr.length)][table_dat[0].length + 2];
			for (int i = 0; i < tmp_dat.length; i++)
				for (int j = 0; j < tmp_dat[0].length; j++)
					tmp_dat[i][j] = "";
			for (int i = 0; i < tmp_dat.length; i++) {
				for (int j = 0; j < 2; j++) {
					try {
						Object obj = arr[i][j];
						if (table_dat.length == 0)
							tmp_dat[i][j] = "" + obj;
						else
							tmp_dat[i][table_dat[0].length + j] = "" + obj;
					} catch (Exception e1) {
					}
				}
			}
			for (int i = 0; i < table_dat.length; i++) {
				for (int j = 0; j < table_dat[0].length; j++) {
					tmp_dat[i][j] = table_dat[i][j];
				}
			}
			table_dat = tmp_dat;
			Object[][] tmp_info;
			if (table_dat_info.length == 0) {
				tmp_info = new Object[1][3];
				tmp_info[0][0] = st_info;
				tmp_info[0][1] = Color.black;
				tmp_info[0][2] = new Boolean(true);
			} else {
				tmp_info = new Object[table_dat_info.length + 1][3];
				tmp_info[table_dat_info.length][0] = st_info;
				tmp_info[table_dat_info.length][1] = this
						.getColorLine(tmp_info.length);
				tmp_info[table_dat_info.length][2] = new Boolean(true);
			}
			for (int i = 0; i < table_dat_info.length; i++) {
				for (int j = 0; j < 3; j++) {
					tmp_info[i][j] = table_dat_info[i][j];
				}
			}
			table_dat_info = tmp_info;
			upd();
		} catch (Exception e) {
			error(e);
		}
	}

	public void upd() {
		p_table1.removeAll();
		p_table2.removeAll();
		table = new JTable(tableModel);
		table.setColumnSelectionAllowed(true);
		table.setRowSelectionAllowed(false);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.getTableHeader().setReorderingAllowed(false);
		s_table = new JScrollPane();
		s_table.getViewport().add(table, null);
		p_table2.add(s_table, BorderLayout.CENTER);
		table_info = new JTable(tableModelInfo) {
			public TableCellRenderer getCellRenderer(int row, int column) {
				if (column == 1) {
					DefaultTableCellRenderer render = new DefaultTableCellRenderer();
					render.setBackground((Color) table_dat_info[row][1]);
					render.setForeground((Color) table_dat_info[row][1]);
					return render;
				} else
					return super.getCellRenderer(row, column);
			}

			public TableCellEditor getCellEditor(int row, int column) {
				if (column == 1) {
					Color cl = JColorChooser.showDialog(this, "Pick a Color",
							(Color) table_dat_info[row][1]);
					if (cl != null)
						table_dat_info[row][1] = cl;
					table_info.clearSelection();
					p_table2.revalidate();
					p_chart.repaint();
				}
				return super.getCellEditor(row, column);
			}
		};
		table_info.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						int i;
						if ((i = table_info.getSelectedRow()) != -1) {
							ta_info.setText(""
									+ table_dat_info[table_info
											.getSelectedRow()][0]);
							table.setColumnSelectionInterval(i * 2, i * 2 + 1);
						}
					}
				});
		s_table_info = new JScrollPane();
		s_table_info.setPreferredSize(new Dimension(200, 400));
		s_table_info.getViewport().add(table_info, null);
		ta_info = new JTextArea();
		ta_info.setRows(5);
		ta_info.setEditable(false);
		s_ta_info = new JScrollPane();
		s_ta_info.getViewport().add(ta_info, null);
		p_table1.add(s_table_info, BorderLayout.CENTER);
		p_table1.add(s_ta_info, BorderLayout.SOUTH);
		p_table1.revalidate();
		p_table2.revalidate();
		min_max();
		p_chart.repaint();
	}

	public void min_max() {
		minx = Float.MAX_VALUE;
		miny = Float.MAX_VALUE;
		maxx = Float.MIN_VALUE;
		maxy = Float.MIN_VALUE;
		for (int i = 0; i < table_dat.length; i++) {
			for (int j = 0; j < table_dat[0].length; j += 2) {
				if (((Boolean) table_dat_info[j / 2][2]).booleanValue())
					try {
						minx = Math
								.min(minx, Float.parseFloat(table_dat[i][j]));
						maxx = Math
								.max(maxx, Float.parseFloat(table_dat[i][j]));
						miny = Math.min(miny,
								Float.parseFloat(table_dat[i][j + 1]));
						maxy = Math.max(maxy,
								Float.parseFloat(table_dat[i][j + 1]));
					} catch (Exception e) {
					}
			}
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

	void tf_maxy_keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			try {
				maxy = Float.parseFloat(tf_maxy.getText());
				repaint();
			} catch (Exception e1) {
				error(e1);
			}
		} else if (e.getKeyCode() == e.VK_ESCAPE)
			tf_maxy.setText(maxy + "");
	}

	void tf_miny_keyPressed(KeyEvent e) {
		if (e.getKeyCode() == e.VK_ENTER) {
			try {
				miny = Float.parseFloat(tf_miny.getText());
				repaint();
			} catch (Exception e1) {
				error(e1);
			}
		} else if (e.getKeyCode() == e.VK_ESCAPE)
			tf_miny.setText(miny + "");
	}

	void tf_minx_keyPressed(KeyEvent e) {
		if (e.getKeyCode() == e.VK_ENTER) {
			try {
				minx = Float.parseFloat(tf_minx.getText());
				repaint();
			} catch (Exception e1) {
				error(e1);
			}
		} else if (e.getKeyCode() == e.VK_ESCAPE)
			tf_minx.setText(minx + "");
	}

	void tf_maxx_keyPressed(KeyEvent e) {
		if (e.getKeyCode() == e.VK_ENTER) {
			try {
				maxx = Float.parseFloat(tf_maxx.getText());
				repaint();
			} catch (Exception e1) {
				error(e1);
			}
		} else if (e.getKeyCode() == e.VK_ESCAPE)
			tf_maxx.setText(maxx + "");
	}

	public void save_Image(String file) {
		byte[] pngbytes;
		PngEncoderB png;

		try {
			if (file.toLowerCase().indexOf(".png") == -1)
				file += ".png";
			FileOutputStream out = new FileOutputStream(file);
			BufferedImage image = (BufferedImage) createImage(
					p_chart.getWidth(), p_chart.getHeight());
			Graphics g = image.getGraphics();
			p_chart.paint(g);

			png = new PngEncoderB(image, false, 0, 5); // Encode at level 5
														// compression
			pngbytes = png.pngEncode();
			out.write(pngbytes);

			g.dispose();
			out.flush();
			out.close();

		} catch (FileNotFoundException fnfe) {
			System.err.println("File not found: " + file);
		} catch (IOException ioe) {
			System.err.println("Could not write file: " + file);
		}
	}

	public void save_ImageColor(String file, Color cl) {
		byte[] pngbytes;
		PngEncoderB png;

		try {
			if (file.toLowerCase().indexOf(".jpg") == -1
					&& file.toLowerCase().indexOf(".jpeg") == -1)
				file += ".jpg";
			FileOutputStream out = new FileOutputStream(file);
			BufferedImage image = (BufferedImage) createImage(50, 5);
			Graphics g = image.getGraphics();
			g.setColor(cl);
			g.fillRect(0, 0, 50, 15);
			png = new PngEncoderB(image, false, 0, 5); // Encode at level 5
														// compression
			pngbytes = png.pngEncode();
			out.write(pngbytes);

			g.dispose();
			out.flush();
			out.close();

		} catch (FileNotFoundException fnfe) {
			System.err.println("File not found: " + file);
		} catch (IOException ioe) {
			System.err.println("Could not write file: " + file);
		}
	}

	void add_action() {

		JFileChooser fd = new JFileChooser(ConfDB.getProperty("Filepath"));
		fd.setSize(350, 200);

		try {

			int choise = fd.showOpenDialog(this);

			if (choise == JFileChooser.APPROVE_OPTION
					&& fd.getSelectedFile() != null) {
				String infile = fd.getSelectedFile().getAbsolutePath();
				openXY(infile);
			}

			// Update configuration file with path
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

	public static Frame getFrame(Component component) {
		for (; component != null && !(component instanceof Frame); component = component
				.getParent())
			;
		return (Frame) component;
	}

	void new_action() {

	}

	void del_action() {
		int[] i_arr = table_info.getSelectedRows();
		for (int ii = i_arr.length - 1; ii >= 0; ii--) {
			// int index = table_info.getSelectedRow();
			int index = i_arr[ii];
			if (index == -1)
				return;
			Object[][] tmp_info = new Object[table_dat_info.length - 1][3];
			String[][] tmp_dat = new String[table_dat.length][table_dat[0].length - 2];
			int z = 0;
			for (int i = 0; i < table_dat_info.length; i++) {
				if (index != i) {
					for (int j = 0; j < 3; j++)
						tmp_info[z][j] = table_dat_info[i][j];
					z++;
				}
			}
			z = 0;
			for (int i = 0; i < table_dat_info.length; i++) {
				if (index != i) {
					for (int j = 0; j < table_dat.length; j++) {
						tmp_dat[j][z * 2] = table_dat[j][i * 2];
						tmp_dat[j][z * 2 + 1] = table_dat[j][i * 2 + 1];
					}
					z++;
				}
			}
			table_dat_info = tmp_info;
			table_dat = tmp_dat;
			upd();
		}
	}

	void save_action() {
		FileFilter f1 = new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory())
					return true;
				return f.getName().toLowerCase().endsWith(".png");
			}

			public String getDescription() {
				return "Image file (.png)";
			}
		};

		try {

			JFileChooser fd = new JFileChooser(ConfDB.getProperty("Filepath"));
			fd.setSize(350, 200);
			fd.addChoosableFileFilter(f1);

			int choise = fd.showSaveDialog(this);

			if (choise == JFileChooser.APPROVE_OPTION
					&& fd.getSelectedFile() != null) {
				String file = fd.getSelectedFile().getAbsolutePath();

				if (fd.getFileFilter().equals(f1)) {
					File f = new File(file);
					if (f.exists()) {
						error("File '" + file + "' exists!");
						return;
					}
					f.mkdir();
					String st = "<html><b><font size=+2><center>Result - "
							+ new Date()
							+ "</center></font></b><br><img src=index.png><br><hr>";
					for (int i = 0; i < table_dat_info.length; i++) {
						if (((Boolean) table_info.getValueAt(i, 2))
								.booleanValue() == true) {
							st += "<pre>" + table_dat_info[i][0]
									+ "</pre><img src=" + i + ".jpg><a href="
									+ i + ".trk>Show data</a><hr>";
							RandomAccessFile o = new RandomAccessFile(file
									+ File.separator + i + ".trk", "rw");
							o.writeBytes(table_dat_info[i][0] + "\n");
							for (int k = 0; k < table_dat.length; k++)
								o.writeBytes(table_dat[k][i * 2] + "   "
										+ table_dat[k][i * 2 + 1] + "\n");
							o.close();
							this.save_ImageColor(
									file + File.separator + i + "",
									((Color) table_dat_info[i][1]));
						}
						repaint();
						this.save_Image(file + File.separator + "index.png");
					}
					RandomAccessFile out = new RandomAccessFile(file
							+ File.separator + "index.html", "rw");
					out.writeBytes(st + "</html>");
					out.close();
				}
			}

			// Update configuration file with path
			if (fd.getSelectedFile() != null) {
				ConfDB.setProperty("Filepath", fd.getSelectedFile()
						.getAbsolutePath());
				save_configuration();
			}

		} catch (Exception e1) {
			error(e1);
		}
	}

	void saveall_action() {
		FileFilter f1 = new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory())
					return true;
				return f.getName().toLowerCase().endsWith(".png");
			}

			public String getDescription() {
				return "Image file (.png)";
			}
		};

		try {

			JFileChooser fd = new JFileChooser(ConfDB.getProperty("Filepath"));
			fd.setSize(350, 200);
			fd.addChoosableFileFilter(f1);

			int choise = fd.showSaveDialog(this);

			if (choise == JFileChooser.APPROVE_OPTION
					&& fd.getSelectedFile() != null) {
				String file = fd.getSelectedFile().getAbsolutePath();

				if (fd.getFileFilter().equals(f1)) {
					File f = new File(file);
					if (f.exists()) {
						error("File '" + file + "' exists!");
						return;
					}
					f.mkdir();
					String st = "<html><b><font size=+2><center>Result - "
							+ new Date() + "</center></font></b><br><br><hr>";
					for (int i = 0; i < table_dat_info.length; i++) {
						st += "<pre>" + table_dat_info[i][0]
								+ "</pre><br><a href=" + i
								+ ".trk>Show data</a><br><img src=" + i
								+ ".png><hr><br><br>";
						for (int j = 0; j < table_dat_info.length; j++) {
							if (i == j) {
								table_info.setValueAt(new Boolean(true), j, 2);
								RandomAccessFile o = new RandomAccessFile(file
										+ File.separator + i + ".trk", "rw");
								o.writeBytes(table_dat_info[i][0] + "\n");
								for (int k = 0; k < table_dat.length; k++)
									o.writeBytes(table_dat[k][j * 2] + "   "
											+ table_dat[k][j * 2 + 1] + "\n");
								o.close();
							} else
								table_info.setValueAt(new Boolean(false), j, 2);
						}
						repaint();
						this.save_Image(file + File.separator + i + ".png");
					}
					RandomAccessFile out = new RandomAccessFile(file
							+ File.separator + "index.html", "rw");
					out.writeBytes(st + "</html>");
					out.close();
				}
			}

			// Update configuration file with path
			if (fd.getSelectedFile() != null) {
				ConfDB.setProperty("Filepath", fd.getSelectedFile()
						.getAbsolutePath());
				save_configuration();
			}

		} catch (Exception e1) {
			error(e1);
		}
	}

	void b_union_actionPerformed(ActionEvent e) {
		int index[] = table_info.getSelectedRows();
		String[][] arr = new String[table_dat.length][2];
		String arr_info = "# UNION NEXT DATA:\n# ------------------------------------------------\n";
		for (int i = 0; i < index.length; i++) {
			arr_info += table_dat_info[index[i]][0]
					+ "\n# ------------------------------------------------\n";
		}
		for (int i = 0; i < table_dat.length; i++) {
			arr[i][0] = table_dat[i][index[0] * 2];
			for (int j = 0; j < index.length; j++) {
				if (arr[i][0].length() != 0)
					if (arr[i][1] == null || arr[i][1].length() == 0)
						arr[i][1] = ""
								+ Float.parseFloat(table_dat[i][index[j] * 2 + 1]);
					else if (table_dat[i][index[j] * 2 + 1].length() > 0)
						arr[i][1] = ""
								+ (Float.parseFloat(arr[i][1]) + Float
										.parseFloat(table_dat[i][index[j] * 2 + 1]));
			}
		}
		this.addXY(arr, arr_info);
	}

	void b_subtract_actionPerformed(ActionEvent e) {
		int index[] = table_info.getSelectedRows();
		if (inverted)
			index = this.invert(index);
		String[][] arr = new String[table_dat.length][2];
		String arr_info = "# SUBTRACT NEXT DATA:\n# ------------------------------------------------\n";
		for (int i = 0; i < index.length; i++) {
			arr_info += table_dat_info[index[i]][0]
					+ "\n# ------------------------------------------------\n";
		}
		for (int i = 0; i < table_dat.length; i++) {
			arr[i][0] = table_dat[i][index[0] * 2];
			for (int j = 0; j < index.length; j++) {
				if (arr[i][0].length() != 0)
					if (arr[i][1] == null || arr[i][1].length() == 0)
						arr[i][1] = ""
								+ Float.parseFloat(table_dat[i][index[j] * 2 + 1]);
					else if (table_dat[i][index[j] * 2 + 1].length() > 0)
						arr[i][1] = ""
								+ (Float.parseFloat(arr[i][1]) - Float
										.parseFloat(table_dat[i][index[j] * 2 + 1]));
			}
		}
		this.addXY(arr, arr_info);
	}

	void b_multiply_actionPerformed(ActionEvent e) {
		int index[] = table_info.getSelectedRows();
		String[][] arr = new String[table_dat.length][2];
		String arr_info = "# MULTIPLY NEXT DATA:\n# ------------------------------------------------\n";
		for (int i = 0; i < index.length; i++) {
			arr_info += table_dat_info[index[i]][0]
					+ "\n# ------------------------------------------------\n";
		}
		for (int i = 0; i < table_dat.length; i++) {
			arr[i][0] = table_dat[i][index[0] * 2];
			for (int j = 0; j < index.length; j++) {
				if (arr[i][0].length() != 0)
					if (arr[i][1] == null || arr[i][1].length() == 0)
						arr[i][1] = ""
								+ Float.parseFloat(table_dat[i][index[j] * 2 + 1]);
					else if (table_dat[i][index[j] * 2 + 1].length() > 0)
						arr[i][1] = ""
								+ (Float.parseFloat(arr[i][1]) * Float
										.parseFloat(table_dat[i][index[j] * 2 + 1]));
			}
		}
		this.addXY(arr, arr_info);
	}

	void b_multiply_with_K_actionPerformed(ActionEvent e) {
		int index[] = table_info.getSelectedRows();
		String[][] arr = new String[table_dat.length][2];
		String arr_info = "# MULTIPLY DATA WITH FACTOR:\n# ------------------------------------------------\n";
		for (int i = 0; i < index.length; i++) {
			arr_info += table_dat_info[index[i]][0]
					+ "\n# ------------------------------------------------\n";
		}
		for (int i = 0; i < table_dat.length; i++) {
			arr[i][0] = table_dat[i][index[0] * 2];
			for (int j = 0; j < index.length; j++) {
				if (arr[i][0].length() != 0)
					if (arr[i][1] == null || arr[i][1].length() == 0)
						arr[i][1] = ""
								+ (Float.parseFloat(table_dat[i][index[j] * 2 + 1]) * factor);
					else if (table_dat[i][index[j] * 2 + 1].length() > 0)
						arr[i][1] = "" + (Float.parseFloat(arr[i][1]) * factor);
			}
		}
		this.addXY(arr, arr_info);
	}

	void tf_factor_keyPressed(KeyEvent e) {
		if (e.getKeyCode() == e.VK_ENTER) {
			try {
				factor = Float.parseFloat(tf_factor.getText());
			} catch (Exception e1) {
				error(e1);
			}
		} else if (e.getKeyCode() == e.VK_ESCAPE)
			tf_factor.setText(factor + "");
	}

	void b_divide_actionPerformed(ActionEvent e) {
		int index[] = table_info.getSelectedRows();
		if (inverted)
			index = this.invert(index);
		String[][] arr = new String[table_dat.length][2];
		String arr_info = "# DIVIDE NEXT DATA:\n# ------------------------------------------------\n";
		for (int i = 0; i < index.length; i++) {
			arr_info += table_dat_info[index[i]][0]
					+ "\n# ------------------------------------------------\n";
		}
		for (int i = 0; i < table_dat.length; i++) {
			arr[i][0] = table_dat[i][index[0] * 2];
			for (int j = 0; j < index.length; j++) {
				if (arr[i][0].length() != 0)
					if (arr[i][1] == null || arr[i][1].length() == 0)
						arr[i][1] = ""
								+ Float.parseFloat(table_dat[i][index[j] * 2 + 1]);
					else if (table_dat[i][index[j] * 2 + 1].length() > 0)
						arr[i][1] = ""
								+ (Float.parseFloat(arr[i][1]) / Float
										.parseFloat(table_dat[i][index[j] * 2 + 1]));
			}
		}
		this.addXY(arr, arr_info);
	}

	void b_value_vs_value_actionPerformed(ActionEvent e) {
		int index[] = table_info.getSelectedRows();
		if (inverted)
			index = this.invert(index);
		String[][] arr = new String[table_dat.length][2];
		String arr_info = "# Y1 vs Y2 DATA:\n# ------------------------------------------------\n";
		for (int i = 0; i < index.length; i++) {
			arr_info += table_dat_info[index[i]][0]
					+ "\n# ------------------------------------------------\n";
		}
		for (int i = 0; i < table_dat.length; i++) {
			arr[i][0] = table_dat[i][index[0] * 2 + 1];
			for (int j = 0; j < index.length; j++) {
				if (arr[i][0].length() != 0)
					if (arr[i][1] == null || arr[i][1].length() == 0)
						arr[i][1] = ""
								+ Float.parseFloat(table_dat[i][index[j] * 2 + 1]);
					else if (table_dat[i][index[j] * 2 + 1].length() > 0)
						arr[i][1] = ""
								+ (Float.parseFloat(table_dat[i][index[j] * 2 + 1]));
			}
		}
		this.addXY(arr, arr_info);
	}

	void b_inv_actionPerformed(ActionEvent e) {
		inverted = !inverted;

		if (inverted)
			b_inv.setIcon(img_flop);
		else
			b_inv.setIcon(img_flip);

	}

	private int[] invert(int[] input) {

		int j = input.length;
		int[] temp = new int[j];

		for (int i = 0; i < input.length; i++)
			temp[--j] = input[i];

		return temp;
	}

	public void exit() {
		System.out.println("Graph finalize.");
	}

	private void load_configuration() {
		FileInputStream fin = null;
		try {
			ConfDB.clear();
			fin = new FileInputStream("Graph.conf");
			ConfDB.load(fin);
			fin.close();
		} catch (IOException e) {
			error(e);
		}
	}

	private void save_configuration() {
		FileOutputStream fout;
		try {
			fout = new FileOutputStream("Graph.conf");
			ConfDB.store(fout, "Configuration file for Impact Grapher");
			fout.close();
		} catch (IOException e) {
			error(e);
		}
	}

	public void headerMessage(String m) {
		Frame f = getFrame(this);
		String s = f.getTitle();
		if (m == null)
			m = ImpactGUI.ver;
		String s1 = s.substring(0, s.lastIndexOf("]") + 1);
		f.setTitle(s1 + " " + m);

	}

	public void header(String m) {
		Frame f = getFrame(this);
		String s = f.getTitle();
		if (m == null)
			m = ImpactGUI.ver;
		String s1 = s.substring(0, s.indexOf("[GRPH:") + 6);
		String s2 = s.substring(s.lastIndexOf("]"), s.length());
		f.setTitle(s1 + m + s2);
	}
}
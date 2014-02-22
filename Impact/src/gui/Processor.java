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
import java.awt.event.*;
import java.io.*;
import java.net.*;

import run.*;
import java.util.*;

import j3d.*;

import javax.swing.table.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import jp.lang.DistributedRuntime;
import jp.sync.Barrier;
import jp.sync.BarrierFactory;

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy, Jonas Forssell
 * 
 */

public class Processor extends JPanel implements ExceptionListener {
	private boolean openGL;
	public static final String ver = "Processor";
	public Hashtable db_node = new Hashtable();
	public Hashtable db_constraints = new Hashtable();
	public Hashtable db_loads = new Hashtable();
	public Hashtable db_element = new Hashtable();
	public Hashtable db_element_loads = new Hashtable();
	private Properties ConfDB = new Properties();

	public String path = "";

	JDialog dialog;
	ImageIcon img_open = new ImageIcon(Processor.class.getResource("open.gif"));
	ImageIcon img_save = new ImageIcon(Processor.class.getResource("save.gif"));
	ImageIcon img_run = new ImageIcon(Processor.class.getResource("run.gif"));
	//ImageIcon img_stats = new ImageIcon(Processor.class.getResource("stats.gif"));
	ImageIcon img_stop = new ImageIcon(Processor.class.getResource("stop.gif"));
	ImageIcon img_find = new ImageIcon(Processor.class.getResource("find.gif"));
	ImageIcon img_solid = new ImageIcon(
			Processor.class.getResource("solid.gif"));
	ImageIcon img_show = new ImageIcon(Processor.class.getResource("show.png"));
	ImageIcon img_hide = new ImageIcon(Processor.class.getResource("hide.png"));
	ImageIcon img_zooma = new ImageIcon(
			Processor.class.getResource("zooma.gif"));
	ImageIcon img_zoomin = new ImageIcon(
			Processor.class.getResource("zoomin.gif"));
	ImageIcon img_zoomout = new ImageIcon(
			Processor.class.getResource("zoomout.gif"));
	ImageIcon img_center = new ImageIcon(
			PreProcessor.class.getResource("center.png"));
	ImageIcon img_view_top = new ImageIcon(
			Processor.class.getResource("view_top.gif"));
	ImageIcon img_view_bottom = new ImageIcon(
			Processor.class.getResource("view_bottom.gif"));
	ImageIcon img_view_left = new ImageIcon(
			Processor.class.getResource("view_left.gif"));
	ImageIcon img_view_right = new ImageIcon(
			Processor.class.getResource("view_right.gif"));
	ImageIcon img_view_front = new ImageIcon(
			Processor.class.getResource("view_front.gif"));
	ImageIcon img_view_back = new ImageIcon(
			Processor.class.getResource("view_back.gif"));
	ImageIcon img_view_sw = new ImageIcon(
			Processor.class.getResource("view_sw.gif"));
	ImageIcon img_view_se = new ImageIcon(
			Processor.class.getResource("view_se.gif"));
	ImageIcon img_view_ne = new ImageIcon(
			Processor.class.getResource("view_ne.gif"));
	ImageIcon img_view_nw = new ImageIcon(
			Processor.class.getResource("view_nw.gif"));
	ImageIcon img_config = new ImageIcon(
			Processor.class.getResource("config.gif"));
	Thread[] tsolve;
	Thread tend;
	boolean isSolve = false;
	JCheckBox cb_node = new JCheckBox("Nodes label", true);
	JCheckBox cb_element = new JCheckBox("Elements label", true);
	JCheckBox cb_constraints = new JCheckBox("Constraints", true);
	JCheckBox cb_loads = new JCheckBox("Loads", true);
	JCheckBox cb_trackers = new JCheckBox("Trackers", true);
	JCheckBox cb_materials = new JCheckBox("Materials", true);
	Object[][] table_materials = new Object[0][4];
	String[] table_hdr_materials = { "Show", "Color", "Material" };
	private TableModel tableModel = new AbstractTableModel() {
		public int getColumnCount() {
			return 3;
		}

		public int getRowCount() {
			return table_materials.length;
		}

		public Object getValueAt(int row, int col) {
			return table_materials[row][col] + "";
		}

		public String getColumnName(int col) {
			return table_hdr_materials[col];
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}
	};
	JSplitPane jSplitPane1 = new JSplitPane();
	JTextArea result = new JTextArea();
	JScrollPane jScrollPane1 = new JScrollPane();
	JPanel jPanel3 = new JPanel();
	JPanel jPanel6 = new JPanel();
	JSplitPane jSplitPane2 = new JSplitPane();
	JPanel jPanel4 = new JPanel();
	JPanel jPanel5 = new JPanel();
	JScrollPane jScrollPane2 = new JScrollPane();
	JTextArea problem = new JTextArea();
	JToolBar jToolBar1 = new JToolBar();
	JButton b_showhide = new JButton();
	JButton b_zooma = new JButton();
	JButton b_zoomin = new JButton();
	JButton b_zoomout = new JButton();
	JButton b_center = new JButton();
	Canvas3D J3D;
	JButton b_viewbottom = new JButton();
	JButton b_viewright = new JButton();
	JButton b_viewfront = new JButton();
	JButton b_viewleft = new JButton();
	JButton b_viewtop = new JButton();
	JButton b_viewback = new JButton();
	JButton b_viewsw = new JButton();
	JButton b_viewse = new JButton();
	JButton b_viewne = new JButton();
	JButton b_viewnw = new JButton();
	JToolBar jToolBar2 = new JToolBar();
	JButton b_open = new JButton();
	JButton b_model = new JButton();
	JButton b_start = new JButton();
	//JButton b_stats = new JButton();
	JButton b_config = new JButton();
	JButton b_save = new JButton();
	JButton b_find = new JButton();
	JSlider js = new JSlider(JSlider.HORIZONTAL, 1, 16, 1);
	private boolean show = true;

	public Processor(boolean openGL) {
		this.openGL = openGL;

		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		J3D = CanvasFactory.getCanvas(null, openGL);
		J3D.setRENDERMODE(Canvas3D.RENDERMODE_HIDE);

		load_configuration();

		this.setLayout(new BorderLayout());
		jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jSplitPane1.setLastDividerLocation(0);
		jSplitPane1.setOneTouchExpandable(true);
		result.setEditable(false);
		jPanel3.setLayout(new BorderLayout());
		jPanel4.setLayout(new BorderLayout());
		jPanel5.setLayout(new BorderLayout());
		jToolBar1.setFloatable(false);

		b_showhide
				.setToolTipText("Hides or shows the selected objects. Press this without anything selected to switch view");
		b_showhide.setIcon(img_hide);
		b_showhide.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_showhide_actionPerformed(e);
			}
		});
		b_zooma.setToolTipText("Zoom all");
		b_zooma.setIcon(img_zooma);
		b_zooma.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_zooma_actionPerformed(e);
			}
		});
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
		b_center.setToolTipText("Set the selected object to center of rotation");
		b_center.setIcon(img_center);
		b_center.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				J3D.setCenterOfRotation();
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
		jSplitPane2.setOneTouchExpandable(true);
		b_open.setToolTipText("Open model *.in");
		b_open.setIcon(img_open);
		b_open.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_open_actionPerformed(e);
			}
		});
		b_start.setToolTipText("Start / Stop");
		b_start.setIcon(img_run);
		b_start.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_start_actionPerformed();
			}
		});
		/*
		b_stats.setToolTipText("Java Micro Benchmark  http://opt.sf.net/");
		b_stats.setIcon(img_stats);
		b_stats.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_stats_actionPerformed();
			}
		});
		*/
		b_model.setToolTipText("Reload view");
		b_model.setIcon(img_solid);
		b_model.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_model_actionPerformed();
			}
		});
		jToolBar2.setFloatable(false);
		b_config.setToolTipText("Configure");
		b_config.setIcon(img_config);
		b_config.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_config_actionPerformed();
			}
		});
		b_save.setToolTipText("Save");
		b_save.setIcon(img_save);
		b_save.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_save_actionPerformed();
			}
		});
		b_find.setIcon(img_find);
		b_find.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_find_actionPerformed(e);
			}
		});
		js.setToolTipText("Number of CPU for solution process.");
		js.setMajorTickSpacing(1);
		js.setMinorTickSpacing(1);
		js.setPaintTicks(true);
		js.setPaintLabels(true);
		js.setSnapToTicks(true);
		js.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				js_stateChanged(arg0);
			}
		});

		// Build the GUI

		this.add(jSplitPane1, BorderLayout.CENTER);
		jSplitPane1.setDividerLocation(100);
		jSplitPane1.add(jPanel6, JSplitPane.LEFT);
		jPanel6.setLayout(new BorderLayout());
		jPanel6.add(jScrollPane1, BorderLayout.CENTER);
		jScrollPane1.getViewport().add(result);
		jSplitPane1.add(jPanel3, JSplitPane.RIGHT);
		jPanel3.add(jSplitPane2, BorderLayout.CENTER);
		jSplitPane2.add(jPanel4, JSplitPane.LEFT);
		jPanel4.add(jScrollPane2, BorderLayout.CENTER);
		jScrollPane2.getViewport().add(problem);
		jPanel4.add(jToolBar2, BorderLayout.NORTH);
		jToolBar2.add(b_open, null);
		jToolBar2.add(b_save, null);
		jToolBar2.addSeparator();
		jToolBar2.add(b_start, null);
		//jToolBar2.add(b_stats, null);
		jToolBar2.addSeparator();
		jToolBar2.add(b_find, null);
		jToolBar2.add(b_model, null);
		jToolBar2.addSeparator();
		// jToolBar2.add(cb_priority);
		// jToolBar2.addSeparator();
		jToolBar2.add(js);
		jSplitPane2.setDividerLocation(500);
		jSplitPane2.add(jPanel5, JSplitPane.RIGHT);
		jPanel5.add(jToolBar1, BorderLayout.NORTH);
		jToolBar1.add(b_showhide, null);
		jToolBar1.add(b_zoomin, null);
		jToolBar1.add(b_zoomout, null);
		jToolBar1.add(b_zooma, null);
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
		if (J3D instanceof Canvas3DGL)
			jPanel5.add((Canvas) J3D, BorderLayout.CENTER);
		else
			jPanel5.add((JPanel) J3D, BorderLayout.CENTER);
		js.setValue(Runtime.getRuntime().availableProcessors());

	}

	public void setVisible(boolean v) {
		super.setVisible(v);
		if (J3D instanceof Canvas3DGL)
			J3D.setVisible(v);
	}

	void b_open_actionPerformed(ActionEvent e) {
		FileFilter f1 = new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory())
					return true;
				return f.getName().toLowerCase().endsWith(".in");
			}

			public String getDescription() {
				return "Open solver model (.in)";
			}
		};

		JFileChooser fd = new JFileChooser(ConfDB.getProperty("Filepath"));
		fd.setSize(350, 200);
		fd.addChoosableFileFilter(f1);

		try {

			int choise = fd.showOpenDialog(this);

			if (choise == JFileChooser.APPROVE_OPTION
					&& fd.getSelectedFile() != null) {
				path = fd.getSelectedFile().getAbsolutePath();

				if (fd.getFileFilter().equals(f1)
						|| path.toLowerCase().endsWith(".in")) {

					RandomAccessFile in = new RandomAccessFile(path, "r");
					problem.setText("");
					String st;
					while ((st = in.readLine()) != null)
						problem.append(st + "\n");
					in.close();
					result.setText("Selected problem - " + path);
					show_model();
				}
			}

			// Update configuration file with path
			if (fd.getSelectedFile() != null) {
				ConfDB.setProperty("Filepath", fd.getSelectedFile()
						.getAbsolutePath());
				save_configuration();
				header(fd.getSelectedFile().getName());
			}

			problem.setCaretPosition(0);

		} catch (Exception e1) {
			error(e1);
		}
	}

	private void error(Object st) {
		JOptionPane.showMessageDialog(this, "Error: " + st, "Error!",
				JOptionPane.ERROR_MESSAGE);
		if (st instanceof Exception)
			((Exception) st).printStackTrace();
		else
			System.out.println("Error: " + st);
	}

	private void message(Object st) {
		JOptionPane.showMessageDialog(this, st, "Information",
				JOptionPane.INFORMATION_MESSAGE);
	}

	public static Frame getFrame(Component component) {
		for (; component != null && !(component instanceof Frame); component = component
				.getParent())
			;
		return (Frame) component;
	}

	/**
	 * This method starts or stops the solver.
	 * 
	 */

	void b_start_actionPerformed() {

		// Stop the solution if the thread is running
		if (isSolve) {
			stopThreads();
			System.out.println("*** Solver terminated ***");
			message("Termination requested. Please wait");
			return;
		}

		// Start solutions if no thread is running
		File f = new File(path);
		f.delete();

		try {
			RandomAccessFile o = new RandomAccessFile(path, "rw");
			o.seek(0);
			o.writeBytes(problem.getText());
			o.close();
		} catch (Exception ex) {
			error(ex);
			return;
		}

		System.setOut(new PrintStream(System.out) {
			public void println(String x) {
				super.println(x);
				result.append(x + "\n");
				result.setCaretPosition(result.getText().length());
			}
		});

		result.setText("");
		int nr_CPU = DistributedRuntime.getMachineCnt();

		if (nr_CPU > 0) {
			// Run a cluster version since clusters are present
			System.out.println("********************************");
			System.out.println("*** Cluster has been invoked ***");
			System.out.println("********************************\n\n");
			System.out.println("Running on " + nr_CPU + " clients\n\n");

			// Create an instance of the solver (no preprocess)
			final ModelCluster[] nodes = new ModelCluster[nr_CPU];
			final Barrier barrier = BarrierFactory.createBarrier(nodes.length);

			System.out.println("*** Creating cluster processes ***");

			for (int n = 0; n < nr_CPU; n++) {
				/**
				 * @at n
				 */
				nodes[n] = new ModelCluster(nr_CPU, n, path, barrier);
				nodes[n].addExceptionListener(this);
			}

			for (int n = 0; n < nr_CPU; n++)
				nodes[n].setCluster_nodes(nodes);

			// Create threads
			tsolve = new Thread[nr_CPU];
			for (int n = 0; n < tsolve.length; n++) {
				tsolve[n] = new Thread(nodes[n]);
				tsolve[n].setPriority(Thread.MIN_PRIORITY);
			}

		}

		else

		{
			// A standard smp version is run -> make nr_CPU positive
			nr_CPU = js.getValue();

			System.out.println("*******************************");
			System.out.println("*** Solver has been invoked ***");
			System.out.println("*******************************\n\n");
			System.out.println("Running on " + nr_CPU + " clients\n\n");

			// Create an instance of the solver (no preprocess)
			ModelSmp node = new ModelSmp(nr_CPU, path);
			node.addExceptionListener(this);

			tsolve = new Thread[1];
			tsolve[0] = new Thread(node);
			tsolve[0].setPriority(Thread.MIN_PRIORITY);
		}

		// Start worker threads
		for (int n = 0; n < tsolve.length; n++)
			tsolve[n].start();

		// Set GUI in running state
		isSolve = true;
		b_start.setIcon(img_stop);
		b_open.setEnabled(false);
		js.setEnabled(false);

		// Wait for completion using a dedicated thread
		tend = new Thread() {
			public void run() {
				for (int i = 0; i < tsolve.length; i++) {
					try {
						if (tsolve[i] != null)
							tsolve[i].join();
					} catch (InterruptedException e) {
						// If interrupted, just continue resetting GUI
					}
				}
				// Finished, set back GUI to stopped state
				isSolve = false;
				b_start.setIcon(img_run);
				b_open.setEnabled(true);
				js.setEnabled(true);
				// result.append("\n*** End ***\n");
				// result.setCaretPosition(result.getText().length());
			}
		};
		tend.start();

		// Finished start process. Solution is running
	}

	/**
	 * This method start Java Micro Benchmark http://opt.sourceforge.net/ Java
	 * Micro Benchmark - control tasks required to determine the comparative
	 * performance characteristics of the computer system on different
	 * platforms. Can be used to guide optimization decisions and to compare
	 * different Java implementations.
	 * 
	 */
	/*
	void b_stats_actionPerformed() {
		try {
			Desktop.getDesktop().open(
					new File(this.getClass()
							.getResource("JavaMicroBenchmark.jnlp").toURI()));
		} catch (Exception e) {
			try {
				Desktop.getDesktop().browse(
						new URI("http://opt.sourceforge.net/"));
			} catch (Exception e1) {
			}
		}
	}
	*/

	public void show_model() {
		J3D.remove_all();
		db_node.clear();
		db_element.clear();
		db_constraints.clear();
		db_loads.clear();
		db_element_loads.clear();

		J3D.setSHOW_ID_NODE(cb_node.isSelected());
		J3D.setSHOW_ID_ELEMENT(cb_element.isSelected());
		J3D.setSHOW_ID_CONSTRAINTS(cb_constraints.isSelected());
		J3D.setSHOW_ID_LOADS(cb_loads.isSelected());
		J3D.setSHOW_ID_TRACKERS(cb_trackers.isSelected());
		J3D.setSHOW_ID_MATERIALS(cb_materials.isSelected());

		StringTokenizer stt = new StringTokenizer(problem.getText().replaceAll(
				"\t", " "), "\n");
		String st;
		_Node nd;

		while (stt.hasMoreTokens()) {
			st = stt.nextToken().trim().toUpperCase();
			if (st.equals("NODES")) {
				try {
					while (stt.hasMoreTokens()) {
						st = stt.nextToken().trim().toUpperCase();
						if (st.startsWith("#") || st.length() == 0)
							continue;
						StringTokenizer stt1 = new StringTokenizer(st,
								" =XYZ\t");
						String key = stt1.nextToken();
						nd = new _Node(Float.parseFloat(stt1.nextToken()),
								Float.parseFloat(stt1.nextToken()),
								Float.parseFloat(stt1.nextToken()));
						db_node.put(key, nd);
						if (st.indexOf("CONSTRAINT =") != -1) {
							String st1 = st
									.substring(st.indexOf("CONSTRAINT") + 10);
							stt1 = new StringTokenizer(st1, " \t");
							stt1.nextToken();
							nd.constraint = new Constraints(stt1.nextToken(),
									Color.BLUE);
						}
						if (st.indexOf("LOAD =") != -1) {
							String st1 = st.substring(st.indexOf("LOAD") + 4);
							stt1 = new StringTokenizer(st1, " \t");
							stt1.nextToken();
							nd.load = new Loads(stt1.nextToken(), Color.RED);
						}
					}
				} catch (Exception e1) {
				}
			}
		}

		stt = new StringTokenizer(problem.getText(), "\n");
		while (stt.hasMoreTokens()) {
			st = stt.nextToken().trim().toUpperCase();
			if (st.startsWith("ELEMENTS")) {
				boolean is_Solid_Iso_4 = (st.toUpperCase().indexOf(
						"SOLID_ISO_4") != -1);
				try {
					while (stt.hasMoreTokens()) {
						st = stt.nextToken().trim().toUpperCase();
						if (st.startsWith("#") || st.length() == 0
								|| st.startsWith("ELEMENTS")) {
							if (st.startsWith("ELEMENTS"))
								is_Solid_Iso_4 = (st.toUpperCase().indexOf(
										"SOLID_ISO_4") != -1);
							continue;
						}
						StringTokenizer stt1 = new StringTokenizer(st, "[]");
						String key = stt1.nextToken();
						key = new StringTokenizer(key, " \t").nextToken();
						String st_nd = stt1.nextToken();
						String st_load = stt1.nextToken();
						StringTokenizer stt2 = new StringTokenizer(st_nd,
								", \t");
						int[] arr = new int[stt2.countTokens()
								+ (is_Solid_Iso_4 ? 1 : 0)];
						int i = 0;
						while (stt2.hasMoreTokens()) {
							arr[i] = Integer.parseInt(stt2.nextToken());
							i++;
						}
						db_element.put(key, arr);
						if (st_load.toUpperCase().indexOf("LOAD") != -1) {
							st_load = st_load.substring(st_load.toUpperCase()
									.indexOf("LOAD"));
							StringTokenizer stt3 = new StringTokenizer(st_load,
									"= \t");
							stt3.nextToken();
							db_element_loads.put(key, stt3.nextToken());
						}
					}
				} catch (Exception e1) {
				}
			}
		}

		// Databases created. Now generate nodes and elements

		for (Enumeration en = db_node.keys(); en.hasMoreElements();) {
			String key = en.nextElement() + "";
			_Node p = (_Node) db_node.get(key);
			J3D.add3D(p);
			p.set_Id(key);
		}

		for (Enumeration en = db_element.keys(); en.hasMoreElements();) {
			String key = "" + en.nextElement();
			int[] arr = (int[]) db_element.get(key);
			if (arr.length == 2) {
				_Node p1 = (_Node) db_node.get(arr[0] + "");
				_Node p2 = (_Node) db_node.get(arr[1] + "");
				_Element2 el2 = new _Element2(p1, p2);
				if (db_element_loads.get(key) != null)
					el2.load = new Loads((String) db_element_loads.get(key),
							Color.GREEN);
				J3D.add3D(el2);
				el2.set_Id(key);
			} else if (arr.length == 3) {
				_Node p1 = (_Node) db_node.get(arr[0] + "");
				_Node p2 = (_Node) db_node.get(arr[1] + "");
				_Node p3 = (_Node) db_node.get(arr[2] + "");
				_Element3 el3 = new _Element3(p1, p2, p3);
				if (db_element_loads.get(key) != null)
					el3.load = new Loads((String) db_element_loads.get(key),
							Color.GREEN);
				J3D.add3D(el3);
				el3.set_Id(key);
			} else if (arr.length == 4) {
				_Node p1 = (_Node) db_node.get(arr[0] + "");
				_Node p2 = (_Node) db_node.get(arr[1] + "");
				_Node p3 = (_Node) db_node.get(arr[2] + "");
				_Node p4 = (_Node) db_node.get(arr[3] + "");
				_Element4 el4 = new _Element4(p1, p2, p3, p4,
						Canvas3D.MESH_Shell_BT_4);
				if (db_element_loads.get(key) != null)
					el4.load = new Loads((String) db_element_loads.get(key),
							Color.GREEN);
				J3D.add3D(el4);
				el4.set_Id(key);
			} else if (arr.length == 5) {
				_Node p1 = (_Node) db_node.get(arr[0] + "");
				_Node p2 = (_Node) db_node.get(arr[1] + "");
				_Node p3 = (_Node) db_node.get(arr[2] + "");
				_Node p4 = (_Node) db_node.get(arr[3] + "");
				_Element4 el4 = new _Element4(p1, p2, p3, p4,
						Canvas3D.MESH_Solid_Iso_4);
				if (db_element_loads.get(key) != null)
					el4.load = new Loads((String) db_element_loads.get(key),
							Color.GREEN);
				J3D.add3D(el4);
				el4.set_Id(key);
			} else if (arr.length == 8) {
				_Node p1 = (_Node) db_node.get(arr[0] + "");
				_Node p2 = (_Node) db_node.get(arr[1] + "");
				_Node p3 = (_Node) db_node.get(arr[2] + "");
				_Node p4 = (_Node) db_node.get(arr[3] + "");
				_Node p5 = (_Node) db_node.get(arr[4] + "");
				_Node p6 = (_Node) db_node.get(arr[5] + "");
				_Node p7 = (_Node) db_node.get(arr[6] + "");
				_Node p8 = (_Node) db_node.get(arr[7] + "");
				_Element8 el8 = new _Element8(p1, p2, p3, p4, p5, p6, p7, p8);
				if (db_element_loads.get(key) != null)
					el8.load = new Loads((String) db_element_loads.get(key),
							Color.GREEN);
				J3D.add3D(el8);
				el8.set_Id(key);
			}
		}
		J3D.repaint();
	}

	void b_showhide_actionPerformed(ActionEvent e) {
		show = J3D.showhide();

		if (show)
			b_showhide.setIcon(img_hide);
		else
			b_showhide.setIcon(img_show);
	}

	void b_zoomin_actionPerformed(ActionEvent e) {
		J3D.view_scale(1.2f);
	}

	void b_zoomout_actionPerformed(ActionEvent e) {
		J3D.view_scale(0.8f);
	}

	void b_zooma_actionPerformed(ActionEvent e) {
		J3D.view_all();
	}

	void b_viewtop_actionPerformed(ActionEvent e) {
		J3D.view_top();
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

	void b_model_actionPerformed() {
		show_model();
	}

	void b_config_actionPerformed() {
		JPanel p = new JPanel(new VerticalFlowLayout());
		p.add(cb_node);
		p.add(cb_element);
		p.add(cb_constraints);
		p.add(cb_loads);
		p.add(cb_trackers);
		p.add(cb_materials);
		JOptionPane.showMessageDialog(this, p, "Show",
				JOptionPane.PLAIN_MESSAGE, null);
		show_model();
	}

	void b_save_actionPerformed() {
		FileFilter f1 = new FileFilter() {
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

			int choise = fd.showSaveDialog(this);

			if (choise == JFileChooser.APPROVE_OPTION
					&& fd.getSelectedFile() != null) {
				String st = fd.getSelectedFile().getAbsolutePath();

				if (fd.getFileFilter().equals(f1)) {
					if (!st.toLowerCase().endsWith(".in"))
						st += ".in";
					File f = new File(st);
					f.delete();
					RandomAccessFile o = new RandomAccessFile(st, "rw");
					o.writeBytes(problem.getText());
					o.close();
					result.append("\nSaved problem as - " + st + "\n");
					path = st;
				}
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

	void b_find_actionPerformed(ActionEvent e) {
		if (dialog == null)
			dialog = new JDialog(getFrame(this));
		dialog.setTitle("Find/Replace");
		dialog.getContentPane().removeAll();
		dialog.getContentPane().add(new Processor_Find(problem));
		dialog.pack();
		dialog.setVisible(true);
	}

	void js_stateChanged(ChangeEvent arg) {
		result.setText("Number of CPU:s set to:" + js.getValue());
	}

	public void exit() {
		System.out.println("Processor finalize.");
	}

	/**
	 * This is where we will end up if an error has occurred
	 */
	public void exceptionOccurred(Exception e, Object o) {

		if (!(e instanceof InterruptedException)) {
			System.out.println("*** Solver Error ***");
			e.printStackTrace();
			// result.append("\n*** Solver Error ***\n");
			result.append(e.toString() + "\n");
			result.setCaretPosition(result.getText().length());
		}
		// Kill all threads
		stopThreads();
	}

	private void stopThreads() {
		for (int i = 0; i < tsolve.length; i++)
			if (tsolve[i] != null)
				tsolve[i].interrupt();

		if (tend != null)
			tend.interrupt();
	}

	private void load_configuration() {
		FileInputStream fin = null;
		try {
			ConfDB.clear();
			fin = new FileInputStream("Pro.conf");
			ConfDB.load(fin);
			fin.close();
		} catch (IOException e) {
			error(e);
		}
	}

	private void save_configuration() {
		FileOutputStream fout;
		try {
			fout = new FileOutputStream("Pro.conf");
			ConfDB.store(fout, "Configuration file for Impact Preprocessor");
			fout.close();
		} catch (IOException e) {
			error(e);
		}
	}

	private void headerMessage(String m) {
		Frame f = getFrame(this);
		if (m == null)
			m = ImpactGUI.ver;
		String s = f.getTitle();
		String s1 = s.substring(0, s.lastIndexOf("]") + 1);
		f.setTitle(s1 + " " + m);

	}

	private void header(String m) {
		Frame f = getFrame(this);
		String s = f.getTitle();
		String s1 = s.substring(0, s.indexOf("[PRO:") + 6);
		String s2 = s.substring(s.indexOf("[OPT:"), s.length());
		f.setTitle(s1 + m + "] " + s2);
	}

}
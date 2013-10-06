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

import run.*;

import java.io.*;
import java.util.*;

import j3d.*;

import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy
 */

public class ProcessorOpt extends JPanel implements ExceptionListener {
	public static final String ver = "Optimizer";

	public String path = "";

	private JSlider js = new JSlider(JSlider.HORIZONTAL, 1, 8, 1);

	private JTextArea result = new JTextArea();
	private JTextArea problem = new JTextArea();

	private Hashtable j3Dstep = new Hashtable();
	private Properties ConfDB = new Properties();

	private ImageIcon img_open = new ImageIcon(
			Processor.class.getResource("open.gif"));
	private ImageIcon img_save = new ImageIcon(
			Processor.class.getResource("save.gif"));
	private ImageIcon img_run = new ImageIcon(
			Processor.class.getResource("run.gif"));
	private ImageIcon img_stop = new ImageIcon(
			Processor.class.getResource("stop.gif"));
	private ImageIcon img_runstep = new ImageIcon(
			Processor.class.getResource("runstep.gif"));

	private JSplitPane jSplitPane1 = new JSplitPane();
	private JSplitPane jSplitPane2 = new JSplitPane();

	private JScrollPane jScrollPane1 = new JScrollPane();
	private JScrollPane jScrollPane2 = new JScrollPane();
	private JScrollPane jScrollPane3 = new JScrollPane();

	private JPanel panel3d = new JPanel();
	private JPanel jPanel1 = new JPanel();
	private JPanel jPanel2 = new JPanel();
	private JPanel jPanel3 = new JPanel();
	private JPanel jPanel4 = new JPanel();
	private JPanel jPanel5 = new JPanel();
	private JPanel jPanel6 = new JPanel();
	private JPanel jPanel7 = new JPanel();
	private JPanel jPanel8 = new JPanel();
	private JPanel jPanel9 = new JPanel();
	private JPanel jPanel11 = new JPanel();
	private JPanel jPanel12 = new JPanel();
	private JPanel jPanel13 = new JPanel();
	private JPanel jPanel14 = new JPanel();
	private JPanel jPanel15 = new JPanel();
	private JPanel jPanel16 = new JPanel();

	private JLabel lbstepstress = new JLabel();
	private JLabel lbstepstrain = new JLabel();
	private JLabel lbstepremesh = new JLabel();

	private Border border1;
	private Border border2;

	private TitledBorder titledBorder1;

	public JTextField tfstep = new JTextField();
	private JTextField tfstress = new JTextField();
	private JTextField tfstrain = new JTextField();
	private JTextField jTextField8 = new JTextField();
	private JTextField tfstressstep = new JTextField();
	private JTextField tfstrainstep = new JTextField();
	private JTextField aval = new JTextField();
	private JTextField bval = new JTextField();
	private JTextField cval = new JTextField();
	private JTextField dval = new JTextField();
	private JTextField maxaval = new JTextField();
	private JTextField minaval = new JTextField();
	private JTextField maxbval = new JTextField();
	private JTextField minbval = new JTextField();
	private JTextField maxcval = new JTextField();
	private JTextField mincval = new JTextField();
	private JTextField maxdval = new JTextField();
	private JTextField mindval = new JTextField();

	public JCheckBox cbstress = new JCheckBox();
	public JCheckBox cbstrain = new JCheckBox();
	private JCheckBox cbremesh = new JCheckBox();

	private JComboBox cbstep = new JComboBox();

	private JToolBar jToolBar4 = new JToolBar();

	private JButton b_save = new JButton();
	public JButton b_open = new JButton();
	public JButton b_start = new JButton();
	public JButton b_start_step = new JButton();

	private Thread tsolve;
	private Thread tend;
	private boolean isSolve;

	public ProcessorOpt(boolean openGL) {
		isSolve = false;

		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void js_stateChanged(ChangeEvent arg) {
		result.setText("Number of CPU:s set to:" + js.getValue());
	}

	private void jbInit() throws Exception {

		load_configuration();

		jSplitPane1.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		jSplitPane1.setLastDividerLocation(0);
		jSplitPane1.setOneTouchExpandable(true);

		jSplitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jSplitPane2.setLastDividerLocation(0);
		jSplitPane2.setOneTouchExpandable(true);

		result = new JTextArea();
		result.setEditable(false);

		titledBorder1 = new TitledBorder(new EtchedBorder(EtchedBorder.RAISED,
				Color.white, new Color(148, 145, 140)), "Step optimization");

		border1 = new TitledBorder(new EtchedBorder(EtchedBorder.RAISED,
				Color.white, new Color(148, 145, 140)),
				"Parametric optimization");
		border2 = new TitledBorder(new EtchedBorder(EtchedBorder.RAISED,
				Color.white, new Color(148, 145, 140)), "Topology optimization");

		this.setLayout(new BorderLayout());

		panel3d.setLayout(new VerticalFlowLayout());
		jPanel1.setLayout(new BorderLayout());
		jPanel3.setLayout(new GridLayout(2, 1));
		jPanel4.setLayout(new GridLayout(4, 1));
		jPanel5.setLayout(new GridLayout(4, 1));
		jPanel5.setBorder(titledBorder1);
		jPanel14.setLayout(new BorderLayout());
		jPanel15.setLayout(new GridLayout(1, 3));
		jPanel16.setLayout(new GridLayout(4, 7));

		lbstepremesh.setText("Remesh - ");

		tfstep.setText("20");
		tfstep.setColumns(3);
		tfstep.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				reset_steps();
			}
		});

		cbstress.setSelected(true);
		cbstress.setText("Stress(I)");
		cbstress.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				reset_stress();
			}
		});

		tfstress.setText("35");
		tfstress.setColumns(3);
		tfstress.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				reset_stress();
			}
		});

		cbstrain.setSelected(true);
		cbstrain.setText("Strain(i)");
		cbstrain.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				reset_strain();
			}
		});

		tfstrain.setText("35");
		tfstrain.setColumns(3);
		tfstrain.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				reset_strain();
			}
		});

		jTextField8.setColumns(10);
		jTextField8.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				reset_remesh();
			}
		});

		cbremesh.setText("Remesh");
		cbremesh.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				reset_remesh();
			}
		});
		tfstressstep.setColumns(5);
		tfstressstep.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				reset_strain();
			}
		});
		tfstrainstep.setColumns(5);
		tfstrainstep.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				reset_strain();
			}
		});
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

		jPanel16.setBorder(border1);
		jPanel4.setBorder(border2);

		jScrollPane1.setPreferredSize(new Dimension(150, 50));
		jScrollPane2.setPreferredSize(new Dimension(400, 50));
		jScrollPane3.setPreferredSize(new Dimension(400, 50));

		jToolBar4.setFloatable(false);

		b_save.setIcon(img_save);
		b_save.setToolTipText("Save file");
		b_save.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_save_actionPerformed(e);
			}
		});

		b_open.setIcon(img_open);
		b_open.setToolTipText("Open a file for calculation");
		b_open.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_open_actionPerformed(e);
			}
		});

		b_start.setIcon(img_run);
		b_start.setToolTipText("Start a Topology Optimization");
		b_start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_startOpt_actionPerformed(e);
			}
		});

		b_start_step.setIcon(img_runstep);
		b_start_step.setToolTipText("Start a Step Optimization");
		b_start.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_startOpt_step_actionPerformed(e);
			}
		});

		// Start building GUI
		this.add(jSplitPane1, BorderLayout.CENTER);
		jSplitPane1.add(jPanel1, JSplitPane.RIGHT);
		jPanel1.add(jScrollPane1, BorderLayout.CENTER);
		jScrollPane1.getViewport().add(panel3d, null);

		jSplitPane1.add(jSplitPane2, JSplitPane.LEFT);
		jSplitPane2.add(jPanel3, JSplitPane.TOP);
		jPanel3.add(jScrollPane3);
		jScrollPane3.getViewport().add(result);
		jPanel3.add(jPanel15);
		jPanel15.add(jPanel16);
		jPanel16.add(new JLabel("A:"));
		jPanel16.add(new JLabel("Value"));
		jPanel16.add(aval);
		jPanel16.add(new JLabel("Max"));
		jPanel16.add(maxaval);
		jPanel16.add(new JLabel("Min"));
		jPanel16.add(minaval);
		jPanel16.add(new JLabel("B:"));
		jPanel16.add(new JLabel("Value"));
		jPanel16.add(bval);
		jPanel16.add(new JLabel("Max"));
		jPanel16.add(maxbval);
		jPanel16.add(new JLabel("Min"));
		jPanel16.add(minbval);
		jPanel16.add(new JLabel("C:"));
		jPanel16.add(new JLabel("Value"));
		jPanel16.add(cval);
		jPanel16.add(new JLabel("Max"));
		jPanel16.add(maxcval);
		jPanel16.add(new JLabel("Min"));
		jPanel16.add(mincval);
		jPanel16.add(new JLabel("D:"));
		jPanel16.add(new JLabel("Value"));
		jPanel16.add(dval);
		jPanel16.add(new JLabel("Max"));
		jPanel16.add(maxdval);
		jPanel16.add(new JLabel("Min"));
		jPanel16.add(mindval);
		jPanel15.add(jPanel4);
		jPanel4.add(jPanel6, null);
		jPanel6.add(new JLabel("Steps"), null);
		jPanel6.add(tfstep, null);
		jPanel4.add(jPanel7, null);
		jPanel7.add(cbstress, null);
		jPanel7.add(tfstress, null);
		jPanel7.add(new JLabel("%"), null);
		jPanel7.add(tfstressstep, null);
		jPanel4.add(jPanel9, null);
		jPanel9.add(cbstrain, null);
		jPanel9.add(tfstrain, null);
		jPanel9.add(new JLabel("%"), null);
		jPanel9.add(tfstrainstep, null);
		jPanel4.add(jPanel11, null);
		jPanel11.add(cbremesh, null);
		jPanel11.add(jTextField8, null);
		jPanel15.add(jPanel5);
		jPanel5.add(jPanel12, null);
		jPanel12.add(cbstep, null);
		jPanel5.add(jPanel8, null);
		jPanel8.add(new JLabel("Stress(I) ="), null);
		jPanel8.add(lbstepstress, null);
		jPanel5.add(jPanel13, null);
		jPanel13.add(new JLabel("Strain(I) ="), null);
		jPanel13.add(lbstepstrain, null);

		jSplitPane2.add(jPanel14, JSplitPane.BOTTOM);
		jPanel14.add(jToolBar4, BorderLayout.NORTH);
		jToolBar4.setFloatable(false);
		jToolBar4.add(b_open, null);
		jToolBar4.add(b_save, null);
		jToolBar4.add(b_start, null);
		jToolBar4.add(b_start_step, null);
		jPanel14.add(jScrollPane2, BorderLayout.CENTER);
		jScrollPane2.getViewport().add(problem);

		jPanel2.add(lbstepremesh, null);

		// Action listeners

		// Final initialization

		reset_steps();
		reset_stress();
		reset_strain();
		reset_remesh();
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

	private void reset_steps() {
		cbstep.removeAllItems();
		try {
			int n = Integer.parseInt(tfstep.getText());
			if (n > 100 || n < 2) {
				error("Steps = " + n + "!\n 2 <= Steps <= 100");
				return;
			}
			for (int i = 1; i <= n; i++) {
				cbstep.insertItemAt("" + i, i - 1);
			}
			cbstep.setSelectedIndex(0);
		} catch (Exception e1) {/* error(e1); */
		}
		cbstep.setVisible(false);
		cbstep.setVisible(true);
		cbstep.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				reset_stress();
				reset_strain();
			}
		});
		cbstep.repaint();
		jPanel5.revalidate();
		panel3d.removeAll();
		try {
			int n = Integer.parseInt(tfstep.getText());
			for (int i = 1; i <= n; i++) {
				Canvas3DSW j3d = new Canvas3DSW(null);
				j3d.setPreferredSize(new Dimension(50, 100));
				if (!j3Dstep.containsKey("" + i))
					j3Dstep.put("" + i, j3d);
			}
			for (int i = 1; i <= n; i++) {
				panel3d.add(new JLabel("" + i));
				Canvas3DSW j3d = new Canvas3DSW(null);
				j3d.setSize(new Dimension(50, 100));
				panel3d.add((Canvas3DSW) j3Dstep.get("" + i));
			}
		} catch (Exception e1) {/* error(e1); */
		}
		jPanel5.add(jPanel2, null);

	}

	private void reset_stress() {
		if (cbstress.isSelected()) {
			int n = cbstep.getSelectedIndex() + 1;
			int s = Integer.parseInt(tfstep.getText());
			float is = Float.parseFloat(tfstress.getText());
			lbstepstress.setText((is * n / s) + "%");
		} else
			lbstepstress.setText("NaN%");
	}

	private void reset_strain() {
		if (cbstrain.isSelected()) {
			int n = cbstep.getSelectedIndex() + 1;
			int s = Integer.parseInt(tfstep.getText());
			float is = Float.parseFloat(tfstrain.getText());
			lbstepstrain.setText((is * n / s) + "%");
		} else
			lbstepstrain.setText("NaN%");
	}

	/**
	 * Opens a file for edit and solution
	 * 
	 * @param e
	 *            The action event triggering the opening of the file
	 */

	private void b_open_actionPerformed(ActionEvent e) {
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

	private void reset_remesh() {
		if (cbremesh.isSelected()) {
			lbstepremesh.setText("Remesh - Yes");
		} else
			lbstepremesh.setText("Remesh - No");
	}

	private String[] getSwitches() {
		String[] args = null;
		try {
			Vector v = new Vector();
			Integer.parseInt(tfstep.getText());
			v.addElement("steps=" + tfstep.getText());
			if (cbstress.isSelected()) {
				Float.parseFloat(tfstress.getText());
				v.addElement("ignore.stress_i=" + tfstress.getText());
			}
			if (cbstrain.isSelected()) {
				Float.parseFloat(tfstrain.getText());
				v.addElement("ignore.strain_i=" + tfstrain.getText());
			}
			args = new String[v.size() + 1];
			for (int i = 0; i < v.size(); i++)
				args[i] = "" + v.elementAt(i);
		} catch (Exception e) {
			error(e);
		}
		return args;
	}

	/**
	 * This is where we will end up if a serious error has occurred
	 */
	public void exceptionOccurred(Exception e, Object o) {

		if (!(e instanceof InterruptedException)) {
			System.out.println("*** Solver Error ***");
			e.printStackTrace();
		}

		result.append(e.toString() + "\n");
		result.setCaretPosition(result.getText().length());

		// Kill all threads
		stopThreads();
	}

	/**
	 * Stops all running threads
	 * 
	 */
	private void stopThreads() {
		if (tsolve != null)
			tsolve.interrupt();

		if (tend != null)
			tend.interrupt();

		try {
			if (tsolve != null)
				tsolve.join();
		} catch (InterruptedException ex) {
		}
	}

	void exit() {
		System.out.println("Optimizer finalize.");
	}

	/**
	 * Runs a complete optimization pass
	 * 
	 * @param e
	 *            The event that triggered this method
	 */
	private void b_startOpt_actionPerformed(ActionEvent e) {

		// Stop the solution if the thread is running
		if (isSolve) {
			tsolve.interrupt();
			message("Termination requested. Please wait");
			return;
		}

		// Start optimization if no thread is running
		File f = new File(path);
		f.delete();

		try {
			result.append("\n Saving file in editor");
			RandomAccessFile o = new RandomAccessFile(path, "rw");
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

		String[] tmp = this.getSwitches();
		String[] args = new String[tmp.length + 1];
		args[0] = path;
		for (int i = 1; i < args.length; i++)
			args[i] = tmp[i - 1];
		ImpactOpt node = new ImpactOpt(args);
		node.addExceptionListener(this);

		tsolve = new Thread(node);

		// Start Optimization
		tsolve.setPriority(Thread.MIN_PRIORITY);
		tsolve.start();

		// Set GUI in a running state
		isSolve = true;
		b_start.setIcon(img_stop);
		b_open.setEnabled(false);
		b_save.setEnabled(false);
		b_start_step.setEnabled(false);
		js.setEnabled(false);

		// Wait for completion using a dedicated thread
		tend = new Thread() {
			public void run() {
				try {
					if (tsolve != null)
						tsolve.join();
				} catch (InterruptedException e) {
					// If interrupted, just continue resetting GUI
				}

				// Finished. Get GUI into stopped state again
				isSolve = false;
				b_start.setIcon(img_run);
				b_start.setIcon(img_run);
				b_open.setEnabled(true);
				b_save.setEnabled(true);
				b_start_step.setEnabled(true);
				js.setEnabled(true);
				result.append("*** End ***");
				result.setCaretPosition(result.getText().length());
			}
		};
		tend.start();

		// Finished process, solution is running.
	}

	/**
	 * Runs a single step of the optimization
	 * 
	 * @param e
	 *            The event that triggered this method
	 */
	private void b_startOpt_step_actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

	/**
	 * Saves the current file in the editor
	 * 
	 * @param e
	 *            The event that triggered this action
	 */
	private void b_save_actionPerformed(ActionEvent e) {
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

	private void load_configuration() {
		FileInputStream fin = null;
		try {
			ConfDB.clear();
			fin = new FileInputStream("Opt.conf");
			ConfDB.load(fin);
			fin.close();
		} catch (IOException e) {
			error(e);
		}
	}

	private void save_configuration() {
		FileOutputStream fout;
		try {
			fout = new FileOutputStream("Opt.conf");
			ConfDB.store(fout, "Configuration file for Impact Optimizer");
			fout.close();
		} catch (IOException e) {
			error(e);
		}
	}

	public void headerMessage(String m) {
		Frame f = getFrame(this);
		String s = f.getTitle();

		String s1 = s.substring(0, s.lastIndexOf("]") + 1);
		f.setTitle(s1 + " " + m);

	}

	public void header(String m) {
		Frame f = getFrame(this);
		String s = f.getTitle();

		String s1 = s.substring(0, s.indexOf("[OPT:") + 6);
		String s2 = s.substring(s.indexOf("[POST:"), s.length());
		f.setTitle(s1 + m + "] " + s2);
	}

	public static Frame getFrame(Component component) {
		for (; component != null && !(component instanceof Frame); component = component
				.getParent())
			;
		return (Frame) component;
	}

}
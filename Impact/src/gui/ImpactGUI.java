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
import javax.swing.UIManager.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.util.*;
import log.*;

/**
 * Impact GUI - Pre Processor, Processor, Processor 0ptimization, Post
 * Processor, Graph, Help.
 * 
 * @author: Yuriy Mikhaylovskiy
 */

public class ImpactGUI extends JFrame {
	private boolean openGL;
	static final String ver = "Impact Version 0.7.06.040";

	PreProcessor pre;
	Processor processor;
	PostProcessor post;
	// ProcessorOpt optimizer;

	Help help = new Help();
	Graph graph = new Graph() {
		void new_action() {
			if (post.time_step.getModel().getSize() < 1) {
				error("Can not read data!");
				return;
			}
			Graph_add p = new Graph_add();
			int result = JOptionPane.showOptionDialog(this, p,
					"Create new Chart", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, null, null);
			if (result == 0) {
				String st = p.FromTo.getText();
				while (st.indexOf('-') != -1) {
					int i = st.indexOf('-');
					String st1 = st.substring(0, i);
					String st2 = st.substring(i + 1);
					StringTokenizer st_t1 = new StringTokenizer(st1, ", ");
					StringTokenizer st_t2 = new StringTokenizer(st2, ", ");
					String s1 = "";
					String s2 = "";
					String s3 = "";
					while (st_t1.hasMoreTokens())
						s1 = st_t1.nextToken();
					s2 = st_t2.nextToken();
					int i1 = Integer.parseInt(s1);
					int i2 = Integer.parseInt(s2);
					for (int j = i1 + 1; j < i2; j++)
						s3 += " " + j;
					st = st1 + s3 + " " + st2;
				}
				StringTokenizer st_t = new StringTokenizer(st, ", ");
				while (st_t.hasMoreTokens()) {
					String nd_el = st_t.nextToken();
					try {
						post.db_graph_preparation(p.AxisY.getSelectedIndex(),
								nd_el);
						graph.addXY(post.db_graph_tmp, post.db_graph_inf);
					} catch (Exception e1) {
						error("Result for node/element '" + nd_el
								+ "' not fount!");
					}
				}
			} else
				return;
		}
	};
	BorderLayout borderLayout1 = new BorderLayout();
	JTabbedPane TabbedPane = new JTabbedPane();

	public ImpactGUI(boolean openGL) {
		this.openGL = openGL;
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new log().put(ver);
		/*
		 * try{ UIManager.put("swing.boldMetal", Boolean.FALSE);
		 * JDialog.setDefaultLookAndFeelDecorated(true);
		 * JFrame.setDefaultLookAndFeelDecorated(true);
		 * Toolkit.getDefaultToolkit().setDynamicLayout(true);
		 * System.setProperty("sun.awt.noerasebackground","true");
		 * UIManager.setLookAndFeel(new
		 * javax.swing.plaf.metal.MetalLookAndFeel()); }catch(Exception e) {
		 * e.printStackTrace(); }
		 */
		/*
		 * try { for (LookAndFeelInfo info :
		 * UIManager.getInstalledLookAndFeels()) {
		 * //System.out.println(info.getName()); if
		 * ("Nimbus".equals(info.getName())) {
		 * UIManager.setLookAndFeel(info.getClassName()); break; } } } catch
		 * (Exception e) { // If Nimbus is not available, you can set the GUI to
		 * another look and feel. }
		 */
		ImpactLogo IL = new ImpactLogo(null, ImpactGUI.ver);

		if (args.length > 0)
			new ImpactGUI(args[0].trim().toUpperCase().equals("-OPENGL"));
		else
			new ImpactGUI(false);

		IL.dispose();

	}

	private void jbInit() throws Exception {
		new ImpactGUINewVersionInfo(ver).start();
		pre = new PreProcessor(openGL);
		processor = new Processor(openGL);
		// optimizer = new ProcessorOpt(openGL);
		post = new PostProcessor();

		this.setIconImage(new ImageIcon(ImpactGUI.class
				.getResource("Impact.gif")).getImage());
		getContentPane().setLayout(borderLayout1);
		super.setTitle("[PRE: ] [PRO: ] [OPT: ] [POST: ] [GRPH: ] " + ver);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds(50, 50, screenSize.width - 100, screenSize.height - 100);
		setExtendedState(JFrame.MAXIMIZED_BOTH);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				pre.exit();
				processor.exit();
				// optimizer.exit();
				post.exit();
				graph.exit();
				System.exit(0);
			}
		});

		this.getContentPane().add(TabbedPane, BorderLayout.CENTER);
		TabbedPane.add(pre, PreProcessor.ver);
		TabbedPane.add(processor, Processor.ver);
		// TabbedPane.add(optimizer, ProcessorOpt.ver);
		TabbedPane.add(post, PostProcessor.ver);
		TabbedPane.add(graph, Graph.ver);
		TabbedPane.add(help, Help.ver);
		this.setVisible(true);
		Thread.currentThread().join();
	}

}

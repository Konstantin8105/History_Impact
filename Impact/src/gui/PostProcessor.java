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
import java.util.*;
import javax.media.j3d.*;
import javax.vecmath.*;

import com.sun.j3d.utils.universe.*;

import com.sun.j3d.utils.behaviors.mouse.*;

//import j3d.*;

import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;

import util.PngEncoderB;

import java.awt.image.*;

/**
 * 
 * @author: Yuriy Mikhaylovskiy
 */

public class PostProcessor extends JPanel {
	String project;
	public static final String ver = "Post Processor";
	Hashtable db_node = new Hashtable();
	Hashtable db_node_tmp = new Hashtable();
	Hashtable db_element = new Hashtable();
	Hashtable db_element_type = new Hashtable();
	Hashtable db_displacements = new Hashtable();
	Hashtable db_strains = new Hashtable();
	Hashtable db_stresses = new Hashtable();
	private Properties ConfDB = new Properties();

	String[][] db_graph_tmp;
	String db_graph_inf;
	String[] st_result = { "DISPLACEMENTS (I)", "DISPLACEMENTS (X)",
			"DISPLACEMENTS (Y)", "DISPLACEMENTS (Z)", "STRAINS (I)",
			"STRAINS (X)", "STRAINS (Y)", "STRAINS (Z)", "STRAINS (XY)",
			"STRAINS (YZ)", "STRAINS (XZ)", "STRESSES (I)", "STRESSES (X)",
			"STRESSES (Y)", "STRESSES (Z)", "STRESSES (XY)", "STRESSES (YZ)",
			"STRESSES (XZ)" };
	// xx yy zz xy yz xz

	double maxdx, maxdy, maxdz, maxdi, maxex, maxey, maxez, maxexy, maxeyz,
			maxexz, maxsx, maxsy, maxsz, maxsxy, maxsyz, maxsxz, maxei, maxsi,
			mindx, mindy, mindz, mindi, minex, miney, minez, minexy, mineyz,
			minexz, minei, minsx, minsy, minsz, minsxy, minsyz, minsxz, minsi;
	double tfmax, tfmin;
	double maxx, maxy, maxz;
	double minx, miny, minz;
	int fontsize = 0;

	ImageIcon img_new = new ImageIcon(
			PostProcessor.class.getResource("new.gif"));
	ImageIcon img_open = new ImageIcon(
			PostProcessor.class.getResource("open.gif"));
	ImageIcon img_add = new ImageIcon(
			PostProcessor.class.getResource("add.gif"));
	ImageIcon img_save = new ImageIcon(
			PostProcessor.class.getResource("save.gif"));
	ImageIcon img_saveall = new ImageIcon(
			PostProcessor.class.getResource("saveall.gif"));
	ImageIcon img_zooma = new ImageIcon(
			PostProcessor.class.getResource("zooma.gif"));
	ImageIcon img_zoomin = new ImageIcon(
			PostProcessor.class.getResource("zoomin.gif"));
	ImageIcon img_zoomout = new ImageIcon(
			PostProcessor.class.getResource("zoomout.gif"));
	ImageIcon img_view_top = new ImageIcon(
			PostProcessor.class.getResource("view_top.gif"));
	ImageIcon img_view_bottom = new ImageIcon(
			PostProcessor.class.getResource("view_bottom.gif"));
	ImageIcon img_view_left = new ImageIcon(
			PostProcessor.class.getResource("view_left.gif"));
	ImageIcon img_view_right = new ImageIcon(
			PostProcessor.class.getResource("view_right.gif"));
	ImageIcon img_view_front = new ImageIcon(
			PostProcessor.class.getResource("view_front.gif"));
	ImageIcon img_view_back = new ImageIcon(
			PostProcessor.class.getResource("view_back.gif"));
	ImageIcon img_view_sw = new ImageIcon(
			PostProcessor.class.getResource("view_sw.gif"));
	ImageIcon img_view_se = new ImageIcon(
			PostProcessor.class.getResource("view_se.gif"));
	ImageIcon img_view_ne = new ImageIcon(
			PostProcessor.class.getResource("view_ne.gif"));
	ImageIcon img_view_nw = new ImageIcon(
			PostProcessor.class.getResource("view_nw.gif"));
	ImageIcon img_gradient = new ImageIcon(
			PostProcessor.class.getResource("gradient.gif"));

	BufferedImage bi_grad;

	JTextField tf_deformed = new JTextField();
	JTextField tf_max = new JTextField();
	JTextField tf_min = new JTextField();

	JPanel jPanel2 = new JPanel();
	JPanel jPanel3 = new JPanel();
	JPanel jPanel5 = new JPanel();
	JPanel jPanel7 = new JPanel();
	JPanel jPanel8 = new JPanel();
	JPanel jPanel9 = new JPanel();

	JButton b_viewbottom = new JButton();
	JButton b_viewtop = new JButton();
	JButton b_zoomout = new JButton();
	JButton b_zoomin = new JButton();
	JButton b_zooma = new JButton();
	JButton b_open = new JButton();
	JButton b_viewleft = new JButton();
	JButton b_saveall = new JButton();
	JButton b_save = new JButton();

	JCheckBox cb_conturfill = new JCheckBox();
	JCheckBox cb_element = new JCheckBox();
	JCheckBox cb_deformed = new JCheckBox();
	JComboBox cb_result = new JComboBox(st_result);
	JCheckBox cb_node = new JCheckBox();
	JCheckBox cb_gradientresult = new JCheckBox();
	JCheckBox cb_mesh = new JCheckBox();

	JScrollPane jScrollPane1 = new JScrollPane();

	JList time_step = new JList();
	PostProcessor_Clip_Panel clip_panel = new PostProcessor_Clip_Panel();

	JToolBar jToolBar1 = new JToolBar();

	JLabel jLabel1 = new JLabel();
	JLabel jLabel2 = new JLabel();
	JLabel jLabel3 = new JLabel();

	GridLayout gridLayout1 = new GridLayout();

	JButton b_viewright = new JButton();
	JButton b_viewsw = new JButton();
	JButton b_viewse = new JButton();
	JButton b_viewne = new JButton();
	JButton b_viewnw = new JButton();
	JButton b_viewfront = new JButton();
	JButton b_viewback = new JButton();

	private Canvas3D J3D = new Canvas3D(
			SimpleUniverse.getPreferredConfiguration());
	public SimpleUniverse universe;
	private BranchGroup brGroup = new BranchGroup();
	private TransformGroup trGroup = new TransformGroup();
	private Transform3D trans3d = new Transform3D();
	private Background bgNode;
	private BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0,
			0.0), 1000000.0);
	private MouseRotate mouseRotate = new MouseRotate();
	private MouseZoom mouseZoom = new MouseZoom();
	private MouseTranslate mouseTranslate = new MouseTranslate();
	private BranchGroup brGroupMouseRotate = new BranchGroup();
	private BranchGroup brGroupMouseZoom = new BranchGroup();
	private BranchGroup brGroupMouseTranslate = new BranchGroup();
	private BranchGroup brGroupClip = new BranchGroup();
	private BranchGroup bgXYZ = new BranchGroup();
	private BranchGroup resultGroup = new BranchGroup();
	private ModelClip model_clip = new ModelClip();

	public PostProcessor() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		// -------------brGroup = new BranchGroup();
		// Create the transform group node and initialize it to the
		// identity. Enable the TRANSFORM_WRITE capability so that
		// our behavior code can modify it at runtime. Add it to the
		// root of the subgraph.
		trGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		trGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		trans3d.setScale(0.005);
		trGroup.setTransform(trans3d);
		brGroup.addChild(trGroup);
		// Set up the background
		bgNode = new Background(new Color3f(1.0f, 1.0f, 1.0f));
		bgNode.setCapability(Background.ALLOW_COLOR_READ);
		bgNode.setCapability(Background.ALLOW_COLOR_WRITE);
		bgNode.setCapability(Background.ALLOW_IMAGE_READ);
		bgNode.setCapability(Background.ALLOW_IMAGE_WRITE);
		bgNode.setCapability(Background.ALLOW_BOUNDS_READ);
		bgNode.setCapability(Background.ALLOW_BOUNDS_WRITE);
		bgNode.setCapability(Background.ALLOW_APPLICATION_BOUNDS_READ);
		bgNode.setCapability(Background.ALLOW_APPLICATION_BOUNDS_WRITE);
		trGroup.setCapability(Background.ALLOW_AUTO_COMPUTE_BOUNDS_READ);
		trGroup.setCapability(Background.ALLOW_AUTO_COMPUTE_BOUNDS_WRITE);
		resultGroup.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		resultGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		resultGroup.setCapability(BranchGroup.ALLOW_DETACH);

		bgNode.setApplicationBounds(bounds);
		brGroup.addChild(bgNode);
		// Set up the global lights
		AmbientLight aLgt = new AmbientLight(new Color3f(0.2f, 0.2f, 0.2f));
		aLgt.setInfluencingBounds(bounds);
		DirectionalLight lgt1 = new DirectionalLight(new Color3f(0.7f, 0.7f,
				0.7f), new Vector3f(1.0f, 1.0f, -1.0f));
		lgt1.setInfluencingBounds(bounds);
		brGroup.addChild(aLgt);
		brGroup.addChild(lgt1);
		// Controls the interactive viewing of objects in 3D
		brGroupMouseRotate.addChild(mouseRotate);
		brGroupMouseZoom.addChild(mouseZoom);
		brGroupMouseTranslate.addChild(mouseTranslate);
		brGroupMouseRotate.setCapability(BranchGroup.ALLOW_DETACH);
		brGroupMouseZoom.setCapability(BranchGroup.ALLOW_DETACH);
		brGroupMouseTranslate.setCapability(BranchGroup.ALLOW_DETACH);
		brGroupClip.setCapability(BranchGroup.ALLOW_DETACH);
		bgXYZ.addChild(new PostProcessor_shp_xyz());
		bgXYZ.addChild(new PostProcessor_shp_xyz_x());
		bgXYZ.addChild(new PostProcessor_shp_xyz_y());
		bgXYZ.addChild(new PostProcessor_shp_xyz_z());
		trGroup.addChild(bgXYZ);
		brGroup.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		brGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		brGroup.setCapability(BranchGroup.ALLOW_COLLISION_BOUNDS_READ);
		brGroup.setCapability(BranchGroup.ALLOW_COLLISION_BOUNDS_WRITE);
		brGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		brGroup.setCapability(BranchGroup.ALLOW_BOUNDS_READ);
		brGroup.setCapability(BranchGroup.ALLOW_BOUNDS_WRITE);
		trGroup.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		trGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		trGroup.setCapability(BranchGroup.ALLOW_COLLISION_BOUNDS_READ);
		trGroup.setCapability(BranchGroup.ALLOW_COLLISION_BOUNDS_WRITE);
		trGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		bgXYZ.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		bgXYZ.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		bgXYZ.setCapability(BranchGroup.ALLOW_DETACH);
		model_clip.setCapability(model_clip.ALLOW_PLANE_READ);
		model_clip.setCapability(model_clip.ALLOW_PLANE_WRITE);
		model_clip.setCapability(model_clip.ALLOW_INFLUENCING_BOUNDS_READ);
		model_clip.setCapability(model_clip.ALLOW_INFLUENCING_BOUNDS_WRITE);
		universe = new SimpleUniverse(J3D);
		universe.addBranchGraph(brGroup);
		universe.getViewingPlatform().setNominalViewingTransform();
		// Create the rotate behavior node
		mouseRotate.setTransformGroup(trGroup);
		brGroupMouseRotate.detach();
		trGroup.addChild(brGroupMouseRotate);
		mouseRotate.setSchedulingBounds(bounds);
		// Create the zoom behavior node
		mouseZoom.setTransformGroup(trGroup);
		brGroupMouseZoom.detach();
		trGroup.addChild(brGroupMouseZoom);
		mouseZoom.setSchedulingBounds(bounds);
		// Create the translate behavior node
		mouseTranslate.setTransformGroup(trGroup);
		brGroupMouseTranslate.detach();
		trGroup.addChild(brGroupMouseTranslate);
		mouseTranslate.setSchedulingBounds(bounds);

		// Create Model Clip
		boolean enables[] = { false, false, false, false, false, false };
		Vector4d eqnx = new Vector4d(-1.0, 0.0, 0.0, 0.0);
		Vector4d eqny = new Vector4d(-1.0, 0.0, 0.0, 0.0);
		Vector4d eqnz = new Vector4d(-1.0, 0.0, 0.0, 0.0);
		model_clip.setEnables(enables);
		model_clip.setPlane(0, eqnx);
		model_clip.setEnable(0, true);
		model_clip.setPlane(1, eqny);
		model_clip.setEnable(1, true);
		model_clip.setPlane(2, eqnz);
		model_clip.setEnable(2, true);
		model_clip.setInfluencingBounds(bounds);
		brGroupClip.addChild(model_clip);
		clip_panel.sl_x.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Vector4d eqn = new Vector4d(clip_panel.cb_x_reverse
						.isSelected() ? 1 : -1.0, 0.0, 0.0,
						(clip_panel.cb_x_reverse.isSelected() ? -1 : 1)
								* clip_panel.sl_x.getValue());
				model_clip.setPlane(0, eqn);
			}
		});
		clip_panel.sl_y.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Vector4d eqn = new Vector4d(0.0, clip_panel.cb_y_reverse
						.isSelected() ? 1 : -1.0, 0.0, (clip_panel.cb_y_reverse
						.isSelected() ? -1 : 1) * clip_panel.sl_y.getValue());
				model_clip.setPlane(1, eqn);
			}
		});
		clip_panel.sl_z.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Vector4d eqn = new Vector4d(0.0, 0.0, clip_panel.cb_z_reverse
						.isSelected() ? 1 : -1.0, (clip_panel.cb_z_reverse
						.isSelected() ? -1 : 1) * clip_panel.sl_z.getValue());
				model_clip.setPlane(2, eqn);
			}
		});
		clip_panel.cb_x_reverse
				.addItemListener(new java.awt.event.ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						Vector4d eqn = new Vector4d(clip_panel.cb_x_reverse
								.isSelected() ? 1 : -1.0, 0.0, 0.0,
								(clip_panel.cb_x_reverse.isSelected() ? -1 : 1)
										* clip_panel.sl_x.getValue());
						model_clip.setPlane(0, eqn);
					}
				});
		clip_panel.cb_y_reverse
				.addItemListener(new java.awt.event.ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						Vector4d eqn = new Vector4d(
								0.0,
								clip_panel.cb_y_reverse.isSelected() ? 1 : -1.0,
								0.0, (clip_panel.cb_y_reverse.isSelected() ? -1
										: 1) * clip_panel.sl_y.getValue());
						model_clip.setPlane(1, eqn);
					}
				});
		clip_panel.cb_z_reverse
				.addItemListener(new java.awt.event.ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						Vector4d eqn = new Vector4d(
								0.0,
								0.0,
								clip_panel.cb_z_reverse.isSelected() ? 1 : -1.0,
								(clip_panel.cb_z_reverse.isSelected() ? -1 : 1)
										* clip_panel.sl_z.getValue());
						model_clip.setPlane(2, eqn);
					}
				});

		load_configuration();

		mindi = 0;
		minsi = 0;
		minei = 0;

		bi_grad = new BufferedImage(img_gradient.getIconWidth(),
				img_gradient.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D big_grad = bi_grad.createGraphics();
		big_grad.drawImage(img_gradient.getImage(), 0, 0, this);
		jPanel7.setLayout(new BorderLayout());
		tf_deformed.setText("1");
		tf_deformed.setPreferredSize(new Dimension(30, 21));
		tf_deformed.setMinimumSize(new Dimension(30, 21));

		tf_max.setPreferredSize(new Dimension(70, 21));
		tf_min.setPreferredSize(new Dimension(70, 21));
		tf_max.setMinimumSize(new Dimension(30, 21));
		tf_min.setMinimumSize(new Dimension(30, 21));

		this.setLayout(new BorderLayout());
		b_viewbottom.setToolTipText("Bottom View");
		b_viewbottom.setIcon(img_view_bottom);
		b_viewbottom.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_viewbottom_actionPerformed(e);
			}
		});
		cb_conturfill.setSelected(true);
		cb_conturfill.setText("Contour fill");
		cb_conturfill.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				draw();
			}
		});
		b_viewtop.setToolTipText("Top View");
		b_viewtop.setIcon(img_view_top);
		b_viewtop.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_viewtop_actionPerformed(e);
			}
		});

		b_zoomout.setToolTipText("Zoom out");
		b_zoomout.setIcon(img_zoomout);
		b_zoomout.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_zoomout_actionPerformed(e);
			}
		});
		b_zoomin.setToolTipText("Zoom in");
		b_zoomin.setIcon(img_zoomin);
		b_zoomin.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_zoomin_actionPerformed(e);
			}
		});
		jPanel3.setLayout(new BorderLayout());
		b_zooma.setToolTipText("Zoom All");
		b_zooma.setIcon(img_zooma);
		b_zooma.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				view_zoom_all();
			}
		});
		cb_element.setText("Elements label");
		cb_element.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				draw();
			}
		});
		cb_deformed.setSelected(true);
		cb_deformed.setText("Deformed");
		cb_deformed.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				draw();
			}
		});
		cb_node.setText("Nodes label");
		cb_node.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				draw();
			}
		});
		jScrollPane1.setPreferredSize(new Dimension(26, 160));
		time_step
				.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						draw();
					}
				});
		b_open.setToolTipText("Open *.res");
		b_open.setIcon(img_open);
		b_open.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_open_actionPerformed(e);
			}
		});
		cb_mesh.setSelected(true);
		cb_mesh.setText("Mesh");
		cb_mesh.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				draw();
			}
		});
		b_viewleft.setToolTipText("Left View");
		b_viewleft.setIcon(img_view_left);
		b_viewleft.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_viewleft_actionPerformed(e);
			}
		});
		b_saveall.setToolTipText("Save images");
		b_saveall.setIcon(img_saveall);
		b_saveall.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_saveall_actionPerformed(e);
			}
		});
		jToolBar1.setFloatable(false);
		b_save.setToolTipText("Save image");
		b_save.setIcon(img_save);
		b_save.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				b_save_actionPerformed(e);
			}
		});
		jLabel3.setText("Time step");
		J3D.setBackground(Color.white);
		J3D.setBackground(Color.white);
		jLabel2.setText("Result");
		jLabel1.setText("Show");
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
		cb_gradientresult.setSelected(true);
		cb_gradientresult.setText("Gradient Result");
		cb_gradientresult.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				draw();
			}
		});
		cb_result.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(ItemEvent e) {

				switch (cb_result.getSelectedIndex()) {
				case 0:
					tf_max.setText("" + maxdi);
					tfmax = maxdi;
					tf_min.setText("" + mindi);
					tfmin = mindi;
					break;
				case 1:
					tf_max.setText("" + maxdx);
					tfmax = maxdx;
					tf_min.setText("" + mindx);
					tfmin = mindx;
					break;
				case 2:
					tf_max.setText("" + maxdy);
					tfmax = maxdy;
					tf_min.setText("" + mindy);
					tfmin = mindy;
					break;
				case 3:
					tf_max.setText("" + maxdz);
					tfmax = maxdz;
					tf_min.setText("" + mindz);
					tfmin = mindz;
					break;
				case 4:
					tf_max.setText("" + maxei);
					tfmax = maxei;
					tf_min.setText("" + minei);
					tfmin = minei;
					break;
				case 5:
					tf_max.setText("" + maxex);
					tfmax = maxex;
					tf_min.setText("" + minex);
					tfmin = minex;
					break;
				case 6:
					tf_max.setText("" + maxey);
					tfmax = maxey;
					tf_min.setText("" + miney);
					tfmin = miney;
					break;
				case 7:
					tf_max.setText("" + maxez);
					tfmax = maxez;
					tf_min.setText("" + minez);
					tfmin = minez;
					break;
				case 8:
					tf_max.setText("" + maxexy);
					tfmax = maxexy;
					tf_min.setText("" + minexy);
					tfmin = minexy;
					break;
				case 9:
					tf_max.setText("" + maxeyz);
					tfmax = maxeyz;
					tf_min.setText("" + mineyz);
					tfmin = mineyz;
					break;
				case 10:
					tf_max.setText("" + maxexz);
					tfmax = maxexz;
					tf_min.setText("" + minexz);
					tfmin = minexz;
					break;
				case 11:
					tf_max.setText("" + maxsi);
					tfmax = maxsi;
					tf_min.setText("" + minsi);
					tfmin = minsi;
					break;
				case 12:
					tf_max.setText("" + maxsx);
					tfmax = maxsx;
					tf_min.setText("" + minsx);
					tfmin = minsx;
					break;
				case 13:
					tf_max.setText("" + maxsy);
					tfmax = maxsy;
					tf_min.setText("" + minsy);
					tfmin = minsy;
					break;
				case 14:
					tf_max.setText("" + maxsz);
					tfmax = maxsz;
					tf_min.setText("" + minsz);
					tfmin = minsz;
					break;
				case 15:
					tf_max.setText("" + maxsxy);
					tfmax = maxsxy;
					tf_min.setText("" + minsxy);
					tfmin = minsxy;
					break;
				case 16:
					tf_max.setText("" + maxsyz);
					tfmax = maxsyz;
					tf_min.setText("" + minsyz);
					tfmin = minsyz;
					break;
				case 17:
					tf_max.setText("" + maxsxz);
					tfmax = maxsxz;
					tf_min.setText("" + minsxz);
					tfmin = minsxz;
					break;
				}

				draw();
			}
		});

		tf_max.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				tf_max_keyPressed(e);
			}
		});
		tf_min.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				tf_min_keyPressed(e);
			}
		});

		clip_panel.cb_clip.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				clip_panel.p_clip.setEnabled(clip_panel.cb_clip.isSelected());
				if (clip_panel.cb_clip.isSelected()) {
					trGroup.addChild(brGroupClip);
				} else {
					brGroupClip.detach();
				}
			}
		});

		this.add(jPanel2, BorderLayout.CENTER);
		jPanel2.setLayout(new BorderLayout());
		jPanel2.add(J3D, BorderLayout.CENTER);

		this.add(jPanel9, BorderLayout.NORTH);
		jPanel9.setLayout(new BorderLayout());
		jPanel9.add(jToolBar1, BorderLayout.WEST);
		jPanel9.add(jPanel8, BorderLayout.EAST);
		jPanel8.add(new JLabel("Min"), BorderLayout.WEST);
		jPanel8.add(tf_min, BorderLayout.WEST);
		jPanel8.add(new JLabel("Max"), BorderLayout.EAST);
		jPanel8.add(tf_max, BorderLayout.EAST);

		jToolBar1.add(b_open, null);
		jToolBar1.add(b_save, null);
		jToolBar1.add(b_saveall, null);
		jToolBar1.addSeparator();
		jToolBar1.add(b_zoomin, null);
		jToolBar1.add(b_zoomout, null);
		jToolBar1.add(b_zooma, null);
		jToolBar1.addSeparator();
		jToolBar1.add(b_viewtop, null);
		jToolBar1.add(b_viewbottom, null);
		jToolBar1.add(b_viewleft, null);
		jToolBar1.add(b_viewright, null);
		jToolBar1.add(b_viewfront, null);
		jToolBar1.add(b_viewback, null);
		jToolBar1.addSeparator();
		jToolBar1.add(b_viewsw, null);
		jToolBar1.add(b_viewse, null);
		jToolBar1.add(b_viewne, null);
		jToolBar1.add(b_viewnw, null);
		jToolBar1.addSeparator();

		this.add(jPanel3, BorderLayout.WEST);

		jPanel3.add(jScrollPane1, BorderLayout.CENTER);
		jScrollPane1.getViewport().add(time_step, null);

		jPanel3.add(jPanel5, BorderLayout.NORTH);
		jPanel3.add(clip_panel, BorderLayout.SOUTH);
		jPanel5.setLayout(gridLayout1);
		gridLayout1.setRows(10);
		jPanel5.add(jLabel1, null);
		jPanel5.add(cb_mesh, null);
		jPanel5.add(jPanel7, null);
		jPanel7.add(cb_deformed, BorderLayout.CENTER);
		jPanel7.add(tf_deformed, BorderLayout.EAST);
		jPanel5.add(cb_conturfill, null);
		jPanel5.add(cb_node, null);
		jPanel5.add(cb_element, null);
		jPanel5.add(cb_gradientresult, null);
		jPanel5.add(jLabel2, null);
		jPanel5.add(cb_result, null);
		jPanel5.add(jLabel3, null);

	}

	public void setVisible(boolean v) {
		super.setVisible(v);
		J3D.setVisible(v);
	}

	public void error(Object st) {
		JOptionPane.showMessageDialog(null, "Error: " + st, "Error!",
				JOptionPane.ERROR_MESSAGE);
		System.out.println("Error: " + st);
	}

	public synchronized void load(String st) {
		try {
			// Rod_2 "Type100" ElemType Linear "MeshType100"
			// Beam_2 "Type110" ElemType Linear "MeshType110"
			// Beam_Spring_2,Contact_Line "Type120" ElemType Linear
			// "MeshType120"
			// Contact_Triangle "Type420" ElemType Triangle "MeshType420"
			// Shell_C0_3 "Type210" ElemType Triangle "MeshType210"
			// Shell_BT_4 "Type300" ElemType Quadrilateral "MeshType300"
			// Solid_Iso_6 "Type500" ElemType Hexahedra "MeshType500"

			time_step.removeAll();
			maxdx = maxdy = maxdz = maxdi = maxex = maxey = maxez = maxexy = maxeyz = maxexz = maxsx = maxsy = maxsz = maxsxy = maxsyz = maxsxz = maxei = maxsi = Float.MIN_VALUE;
			mindx = mindy = mindz = minex = miney = minez = minexy = mineyz = minexz = minsx = minsy = minsz = minsxy = minsyz = minsxz = Float.MAX_VALUE;
			maxx = maxy = maxz = -Double.MAX_VALUE;
			minx = miny = minz = Double.MAX_VALUE;
			db_node = new Hashtable();
			db_element = new Hashtable();
			db_element_type = new Hashtable();
			db_displacements = new Hashtable();
			db_strains = new Hashtable();
			db_stresses = new Hashtable();
			DefaultListModel model = new DefaultListModel();
			String st_msh = st.substring(0, st.length() - 3) + "msh";
			String st_dat = st;
			RandomAccessFile in = new RandomAccessFile(st_msh, "r");
			String dat;
			System.out.print("\nLoading mesh ...");
			while ((dat = in.readLine()) != null) {
				if (dat.startsWith("COORDINATES")) {
					Point3d p3d;
					while (!(dat = in.readLine()).startsWith("END")) {
						StringTokenizer st_t = new StringTokenizer(dat);
						String index = st_t.nextToken();
						p3d = toPoint3d(dat);
						maxx = Math.max(maxx, p3d.x);
						maxy = Math.max(maxy, p3d.y);
						maxz = Math.max(maxz, p3d.z);
						minx = Math.min(minx, p3d.x);
						miny = Math.min(miny, p3d.y);
						minz = Math.min(minz, p3d.z);
						db_node.put(index, p3d);
					}
				} else if (dat.startsWith("ELEMENTS")) {
					fontsize = (int) ((maxx - minx + maxy - miny + maxz - minz) / db_node
							.size());
					if (fontsize == 0)
						fontsize = 1;
					while (!(dat = in.readLine()).startsWith("END")) {
						StringTokenizer st_t = new StringTokenizer(dat);
						String index = st_t.nextToken();
						int[] arr = toArrayInt(dat);
						db_element.put(index, arr);
					}
				}
			}
			in.close();
			in = new RandomAccessFile(st_dat, "r");
			System.out.print("... OK\nLoading data ...");
			dat = in.readLine();
			int n = 1;
			while (dat != null) {
				headerMessage("loading block: " + (n++) + " (" + dat + ")");
				if (dat.toUpperCase().startsWith("GAUSSPOINTS")) {
					StringTokenizer st_t = new StringTokenizer(dat, "\"");
					st_t.nextToken();
					String et = st_t.nextToken();
					dat = in.readLine();
					st_t = new StringTokenizer(dat, ":");
					st_t.nextToken();
					int np = Integer.parseInt(st_t.nextToken().trim());
					db_element_type.put(et, "" + np);
					while (!(dat = in.readLine()).toUpperCase().startsWith(
							"END")) {
					}
				} else if (dat.indexOf("DISPLACEMENTS") != -1) {
					StringTokenizer st_t = new StringTokenizer(dat.trim());
					st_t.nextToken();
					st_t.nextToken();
					String step = st_t.nextToken();
					model.addElement(step);
					Hashtable v = new Hashtable();
					while ((dat = in.readLine()) != null
							&& dat.indexOf("LOCAL") == -1
							&& dat.indexOf("GLOBAL") == -1) {
						st_t = new StringTokenizer(dat);
						String key = st_t.nextToken();
						Point3d p = toPoint3d(dat);
						mindx = Math.min(mindx, p.x);
						mindy = Math.min(mindy, p.y);
						mindz = Math.min(mindz, p.z);
						maxdx = Math.max(maxdx, p.x);
						maxdy = Math.max(maxdy, p.y);
						maxdz = Math.max(maxdz, p.z);
						maxdi = Math.max(
								maxdi,
								(float) Math.sqrt(p.x * p.x + p.y * p.y + p.z
										* p.z));
						v.put(key, p);
					}
					db_displacements.put(step, v);
				} else if (dat.indexOf("STRESSES") != -1) {
					StringTokenizer st_t = new StringTokenizer(dat.trim());
					st_t.nextToken();
					st_t.nextToken();
					String step = st_t.nextToken();
					Hashtable v = (Hashtable) db_stresses.get(step);
					if (v == null)
						v = new Hashtable();
					while ((dat = in.readLine()) != null
							&& dat.indexOf("LOCAL") == -1
							&& dat.indexOf("GLOBAL") == -1
							&& dat.indexOf("DISPLACEMENTS") == -1) {
						st_t = new StringTokenizer(dat);
						if (st_t.countTokens() == 7) {
							long pos = in.getFilePointer();
							String dat1 = in.readLine();
							StringTokenizer st_t1 = null;
							if (dat1 != null)
								st_t1 = new StringTokenizer(dat1);
							if (dat1 == null || st_t1.countTokens() == 7
									|| dat1.indexOf("LOCAL") != -1
									|| dat1.indexOf("GLOBAL") != -1
									|| dat1.indexOf("DISPLACEMENTS") != -1) {
								in.seek(pos);
							} else {
								dat += " " + dat1 + " " + in.readLine() + " "
										+ in.readLine() + " " + in.readLine()
										+ " " + in.readLine() + " "
										+ in.readLine() + " " + in.readLine();
							}
						}
						String key = st_t.nextToken();
						float[] arr = toArrayFloat(dat);
						if (arr.length == 1) {
							minsx = Math.min(minsx, arr[0]);
							maxsx = Math.max(maxsx, arr[0]);
							maxsi = Math.max(maxsi, Math.abs(arr[0]));
						} else if (arr.length == 6) {
							minsx = Math.min(minsx, arr[0]);
							minsy = Math.min(minsy, arr[1]);
							minsz = Math.min(minsz, arr[2]);
							minsxy = Math.min(minsxy, arr[3]);
							minsyz = Math.min(minsyz, arr[4]);
							minsxz = Math.min(minsxz, arr[5]);
							maxsx = Math.max(maxsx, arr[0]);
							maxsy = Math.max(maxsy, arr[1]);
							maxsz = Math.max(maxsz, arr[2]);
							maxsxy = Math.max(maxsxy, arr[3]);
							maxsyz = Math.max(maxsyz, arr[4]);
							maxsxz = Math.max(maxsxz, arr[5]);
							maxsi = (float) Math.max(
									maxsi,
									Math.sqrt(2)
											/ 2
											* Math.sqrt(Math.pow(arr[0]
													- arr[1], 2)
													+ Math.pow(arr[1] - arr[2],
															2)
													+ Math.pow(arr[2] - arr[0],
															2)
													+ 6
													* (arr[3] * arr[3] + arr[4]
															* arr[4] + arr[5]
															* arr[5])));
						} else {
							minsx = Math
									.min(minsx, (arr[0] + arr[6] + arr[12]
											+ arr[18] + arr[24] + arr[30]
											+ arr[36] + arr[42]) / 8);
							minsy = Math
									.min(minsy, (arr[1] + arr[7] + arr[13]
											+ arr[19] + arr[25] + arr[31]
											+ arr[37] + arr[43]) / 8);
							minsz = Math
									.min(minsz, (arr[2] + arr[8] + arr[14]
											+ arr[20] + arr[26] + arr[32]
											+ arr[38] + arr[44]) / 8);
							minsxy = Math.min(minsxy, (arr[3] + arr[9]
									+ arr[15] + arr[21] + arr[27] + arr[33]
									+ arr[39] + arr[45]) / 8);
							minsyz = Math.min(minsyz, (arr[4] + arr[10]
									+ arr[16] + arr[22] + arr[28] + arr[34]
									+ arr[40] + arr[46]) / 8);
							minsxz = Math.min(minsxz, (arr[5] + arr[11]
									+ arr[17] + arr[23] + arr[29] + arr[35]
									+ arr[41] + arr[47]) / 8);
							maxsx = Math
									.max(maxsx, (arr[0] + arr[6] + arr[12]
											+ arr[18] + arr[24] + arr[30]
											+ arr[36] + arr[42]) / 8);
							maxsy = Math
									.max(maxsy, (arr[1] + arr[7] + arr[13]
											+ arr[19] + arr[25] + arr[31]
											+ arr[37] + arr[43]) / 8);
							maxsz = Math
									.max(maxsz, (arr[2] + arr[8] + arr[14]
											+ arr[20] + arr[26] + arr[32]
											+ arr[38] + arr[44]) / 8);
							maxsxy = Math.max(maxsxy, (arr[3] + arr[9]
									+ arr[15] + arr[21] + arr[27] + arr[33]
									+ arr[39] + arr[45]) / 8);
							maxsyz = Math.max(maxsyz, (arr[4] + arr[10]
									+ arr[16] + arr[22] + arr[28] + arr[34]
									+ arr[40] + arr[46]) / 8);
							maxsxz = Math.max(maxsxz, (arr[5] + arr[11]
									+ arr[17] + arr[23] + arr[29] + arr[35]
									+ arr[41] + arr[47]) / 8);
							maxsi = (float) Math.max(
									maxsi,
									Math.sqrt(2)
											/ 2
											* Math.sqrt(Math.pow(arr[0]
													- arr[1], 2)
													+ Math.pow(arr[1] - arr[2],
															2)
													+ Math.pow(arr[2] - arr[0],
															2)
													+ 6
													* (arr[3] * arr[3] + arr[4]
															* arr[4] + arr[5]
															* arr[5])));
							maxsi = (float) Math
									.max(maxsi,
											Math.sqrt(2)
													/ 2
													* Math.sqrt(Math.pow(arr[6]
															- arr[7], 2)
															+ Math.pow(arr[7]
																	- arr[8], 2)
															+ Math.pow(arr[8]
																	- arr[6], 2)
															+ 6
															* (arr[9] * arr[9]
																	+ arr[10]
																	* arr[10] + arr[11]
																	* arr[11])));
							maxsi = (float) Math
									.max(maxsi,
											Math.sqrt(2)
													/ 2
													* Math.sqrt(Math.pow(
															arr[12] - arr[13],
															2)
															+ Math.pow(arr[13]
																	- arr[14],
																	2)
															+ Math.pow(arr[14]
																	- arr[12],
																	2)
															+ 6
															* (arr[15]
																	* arr[15]
																	+ arr[16]
																	* arr[16] + arr[17]
																	* arr[17])));
							maxsi = (float) Math
									.max(maxsi,
											Math.sqrt(2)
													/ 2
													* Math.sqrt(Math.pow(
															arr[18] - arr[19],
															2)
															+ Math.pow(arr[19]
																	- arr[20],
																	2)
															+ Math.pow(arr[20]
																	- arr[18],
																	2)
															+ 6
															* (arr[21]
																	* arr[21]
																	+ arr[22]
																	* arr[22] + arr[23]
																	* arr[23])));
							maxsi = (float) Math
									.max(maxsi,
											Math.sqrt(2)
													/ 2
													* Math.sqrt(Math.pow(
															arr[24] - arr[25],
															2)
															+ Math.pow(arr[25]
																	- arr[26],
																	2)
															+ Math.pow(arr[26]
																	- arr[24],
																	2)
															+ 6
															* (arr[27]
																	* arr[27]
																	+ arr[28]
																	* arr[28] + arr[29]
																	* arr[29])));
							maxsi = (float) Math
									.max(maxsi,
											Math.sqrt(2)
													/ 2
													* Math.sqrt(Math.pow(
															arr[30] - arr[31],
															2)
															+ Math.pow(arr[31]
																	- arr[32],
																	2)
															+ Math.pow(arr[32]
																	- arr[30],
																	2)
															+ 6
															* (arr[33]
																	* arr[33]
																	+ arr[34]
																	* arr[34] + arr[35]
																	* arr[35])));
							maxsi = (float) Math
									.max(maxsi,
											Math.sqrt(2)
													/ 2
													* Math.sqrt(Math.pow(
															arr[36] - arr[37],
															2)
															+ Math.pow(arr[36]
																	- arr[38],
																	2)
															+ Math.pow(arr[38]
																	- arr[36],
																	2)
															+ 6
															* (arr[39]
																	* arr[39]
																	+ arr[40]
																	* arr[40] + arr[41]
																	* arr[41])));
							maxsi = (float) Math
									.max(maxsi,
											Math.sqrt(2)
													/ 2
													* Math.sqrt(Math.pow(
															arr[42] - arr[43],
															2)
															+ Math.pow(arr[43]
																	- arr[44],
																	2)
															+ Math.pow(arr[44]
																	- arr[42],
																	2)
															+ 6
															* (arr[45]
																	* arr[45]
																	+ arr[46]
																	* arr[46] + arr[47]
																	* arr[47])));
						}
						v.put(key, arr);
					}
					db_stresses.put(step, v);
				} else if (dat.indexOf("STRAINS") != -1) {
					StringTokenizer st_t = new StringTokenizer(dat.trim());
					st_t.nextToken();
					st_t.nextToken();
					String step = st_t.nextToken();
					Hashtable v = (Hashtable) db_strains.get(step);
					if (v == null)
						v = new Hashtable();
					while ((dat = in.readLine()) != null
							&& dat.indexOf("LOCAL") == -1
							&& dat.indexOf("GLOBAL") == -1
							&& dat.indexOf("DISPLACEMENTS") == -1) {
						st_t = new StringTokenizer(dat);
						if (st_t.countTokens() == 7) {
							long pos = in.getFilePointer();
							String dat1 = in.readLine();
							StringTokenizer st_t1 = null;
							if (dat1 != null)
								st_t1 = new StringTokenizer(dat1);
							if (dat1 == null || st_t1.countTokens() == 7
									|| dat1.indexOf("LOCAL") != -1
									|| dat1.indexOf("GLOBAL") != -1
									|| dat1.indexOf("DISPLACEMENTS") != -1) {
								in.seek(pos);
							} else {
								dat += " " + dat1 + " " + in.readLine() + " "
										+ in.readLine() + " " + in.readLine()
										+ " " + in.readLine() + " "
										+ in.readLine() + " " + in.readLine();
							}
						}
						String key = st_t.nextToken();
						float[] arr = toArrayFloat(dat);
						if (arr.length == 1) {
							minex = Math.min(minex, arr[0]);
							maxex = Math.max(maxex, arr[0]);
							maxei = Math.max(maxei, Math.abs(arr[0]));
						} else if (arr.length == 6) {
							minex = Math.min(minex, arr[0]);
							miney = Math.min(miney, arr[1]);
							minez = Math.min(minez, arr[2]);
							minexy = Math.min(minexy, arr[3]);
							mineyz = Math.min(mineyz, arr[4]);
							minexz = Math.min(minexz, arr[5]);
							maxex = Math.max(maxex, arr[0]);
							maxey = Math.max(maxey, arr[1]);
							maxez = Math.max(maxez, arr[2]);
							maxexy = Math.max(maxexy, arr[3]);
							maxeyz = Math.max(maxeyz, arr[4]);
							maxexz = Math.max(maxexz, arr[5]);
							maxei = (float) Math.max(
									maxei,
									Math.sqrt(2)
											/ 3
											* Math.sqrt(Math.pow(arr[0]
													- arr[1], 2)
													+ Math.pow(arr[1] - arr[2],
															2)
													+ Math.pow(arr[2] - arr[0],
															2)
													+ 1.5
													* (arr[3] * arr[3] + arr[4]
															* arr[4] + arr[5]
															* arr[5])));
						} else {
							minex = Math
									.min(minex, (arr[0] + arr[6] + arr[12]
											+ arr[18] + arr[24] + arr[30]
											+ arr[36] + arr[42]) / 8);
							miney = Math
									.min(miney, (arr[1] + arr[7] + arr[13]
											+ arr[19] + arr[25] + arr[31]
											+ arr[37] + arr[43]) / 8);
							minez = Math
									.min(minez, (arr[2] + arr[8] + arr[14]
											+ arr[20] + arr[26] + arr[32]
											+ arr[38] + arr[44]) / 8);
							minexy = Math.min(minexy, (arr[3] + arr[9]
									+ arr[15] + arr[21] + arr[27] + arr[33]
									+ arr[39] + arr[45]) / 8);
							mineyz = Math.min(mineyz, (arr[4] + arr[10]
									+ arr[16] + arr[22] + arr[28] + arr[34]
									+ arr[40] + arr[46]) / 8);
							minexz = Math.min(minexz, (arr[5] + arr[11]
									+ arr[17] + arr[23] + arr[29] + arr[35]
									+ arr[41] + arr[47]) / 8);
							maxex = Math
									.max(maxex, (arr[0] + arr[6] + arr[12]
											+ arr[18] + arr[24] + arr[30]
											+ arr[36] + arr[42]) / 8);
							maxey = Math
									.max(maxey, (arr[1] + arr[7] + arr[13]
											+ arr[19] + arr[25] + arr[31]
											+ arr[37] + arr[43]) / 8);
							maxez = Math
									.max(maxez, (arr[2] + arr[8] + arr[14]
											+ arr[20] + arr[26] + arr[32]
											+ arr[38] + arr[44]) / 8);
							maxexy = Math.max(maxexy, (arr[3] + arr[9]
									+ arr[15] + arr[21] + arr[27] + arr[33]
									+ arr[39] + arr[45]) / 8);
							maxeyz = Math.max(maxeyz, (arr[4] + arr[10]
									+ arr[16] + arr[22] + arr[28] + arr[34]
									+ arr[40] + arr[46]) / 8);
							maxexz = Math.max(maxexz, (arr[5] + arr[11]
									+ arr[17] + arr[23] + arr[29] + arr[35]
									+ arr[41] + arr[47]) / 8);
							maxei = (float) Math.max(
									maxei,
									Math.sqrt(2)
											/ 3
											* Math.sqrt(Math.pow(arr[0]
													- arr[1], 2)
													+ Math.pow(arr[1] - arr[2],
															2)
													+ Math.pow(arr[2] - arr[0],
															2)
													+ 1.5
													* (arr[3] * arr[3] + arr[4]
															* arr[4] + arr[5]
															* arr[5])));
							maxei = (float) Math
									.max(maxei,
											Math.sqrt(2)
													/ 3
													* Math.sqrt(Math.pow(arr[6]
															- arr[7], 2)
															+ Math.pow(arr[7]
																	- arr[8], 2)
															+ Math.pow(arr[8]
																	- arr[6], 2)
															+ 1.5
															* (arr[9] * arr[9]
																	+ arr[10]
																	* arr[10] + arr[11]
																	* arr[11])));
							maxei = (float) Math
									.max(maxei,
											Math.sqrt(2)
													/ 3
													* Math.sqrt(Math.pow(
															arr[12] - arr[13],
															2)
															+ Math.pow(arr[13]
																	- arr[14],
																	2)
															+ Math.pow(arr[14]
																	- arr[12],
																	2)
															+ 1.5
															* (arr[15]
																	* arr[15]
																	+ arr[16]
																	* arr[16] + arr[17]
																	* arr[17])));
							maxei = (float) Math
									.max(maxei,
											Math.sqrt(2)
													/ 3
													* Math.sqrt(Math.pow(
															arr[18] - arr[19],
															2)
															+ Math.pow(arr[19]
																	- arr[20],
																	2)
															+ Math.pow(arr[20]
																	- arr[18],
																	2)
															+ 1.5
															* (arr[21]
																	* arr[21]
																	+ arr[22]
																	* arr[22] + arr[23]
																	* arr[23])));
							maxei = (float) Math
									.max(maxei,
											Math.sqrt(2)
													/ 3
													* Math.sqrt(Math.pow(
															arr[24] - arr[25],
															2)
															+ Math.pow(arr[25]
																	- arr[26],
																	2)
															+ Math.pow(arr[26]
																	- arr[24],
																	2)
															+ 1.5
															* (arr[27]
																	* arr[27]
																	+ arr[28]
																	* arr[28] + arr[29]
																	* arr[29])));
							maxei = (float) Math
									.max(maxei,
											Math.sqrt(2)
													/ 3
													* Math.sqrt(Math.pow(
															arr[30] - arr[31],
															2)
															+ Math.pow(arr[31]
																	- arr[32],
																	2)
															+ Math.pow(arr[32]
																	- arr[30],
																	2)
															+ 1.5
															* (arr[33]
																	* arr[33]
																	+ arr[34]
																	* arr[34] + arr[35]
																	* arr[35])));
							maxei = (float) Math
									.max(maxei,
											Math.sqrt(2)
													/ 3
													* Math.sqrt(Math.pow(
															arr[36] - arr[37],
															2)
															+ Math.pow(arr[36]
																	- arr[38],
																	2)
															+ Math.pow(arr[38]
																	- arr[36],
																	2)
															+ 1.5
															* (arr[39]
																	* arr[39]
																	+ arr[40]
																	* arr[40] + arr[41]
																	* arr[41])));
							maxei = (float) Math
									.max(maxei,
											Math.sqrt(2)
													/ 3
													* Math.sqrt(Math.pow(
															arr[42] - arr[43],
															2)
															+ Math.pow(arr[43]
																	- arr[44],
																	2)
															+ Math.pow(arr[44]
																	- arr[42],
																	2)
															+ 1.5
															* (arr[45]
																	* arr[45]
																	+ arr[46]
																	* arr[46] + arr[47]
																	* arr[47])));
						}
						v.put(key, arr);
					}
					db_strains.put(step, v);
				} else
					dat = in.readLine();
			}
			in.close();
			clip_panel.sl_x.setMinimum((int) (minx + mindx - Math
					.abs(minx * 0.2)));
			clip_panel.sl_x.setMaximum((int) (maxx + maxdx + Math
					.abs(maxx * 0.2)));
			clip_panel.sl_x
					.setValue((int) (minx + mindx - Math.abs(minx * 0.2)));
			clip_panel.sl_y.setMinimum((int) (miny + mindy - Math
					.abs(miny * 0.2)));
			clip_panel.sl_y.setMaximum((int) (maxy + maxdy + Math
					.abs(maxy * 0.2)));
			clip_panel.sl_y
					.setValue((int) (miny + mindy - Math.abs(miny * 0.2)));
			clip_panel.sl_z.setMinimum((int) (minz + mindz - Math
					.abs(minz * 0.2)));
			clip_panel.sl_z.setMaximum((int) (maxz + maxdz + Math
					.abs(maxz * 0.2)));
			clip_panel.sl_z
					.setValue((int) (minz + mindz - Math.abs(minz * 0.2)));
			model_clip.setInfluencingBounds(bounds);
			System.out.print("... OK\n");
			headerMessage(null);
			try {
				time_step.setModel(model);
			} catch (Exception e2) {
			}

			// Set initial state
			tf_max.setText("" + maxdi);
			tfmax = maxdi;
			tf_min.setText("" + mindi);
			tfmin = mindi;

			time_step.setSelectedIndex(0);
			J3D.repaint();
		} catch (Exception e1) {
			error(e1);
		}

	}

	public Point3d toPoint3d(String st) throws Exception {
		StringTokenizer st_t = new StringTokenizer(st);
		st_t.nextToken();
		return new Point3d(new Float(st_t.nextToken()).floatValue(), new Float(
				st_t.nextToken()).floatValue(),
				new Float(st_t.nextToken()).floatValue());
	}

	public int[] toArrayInt(String st) throws Exception {
		StringTokenizer st_t = new StringTokenizer(st);
		int[] arr = new int[st_t.countTokens() - 1];
		st_t.nextToken();
		for (int i = 0; i < arr.length; i++) {
			arr[i] = Integer.parseInt(st_t.nextToken());
		}
		return arr;
	}

	public float[] toArrayFloat(String st) throws Exception {
		StringTokenizer st_t = new StringTokenizer(st);
		float[] arr = new float[st_t.countTokens() - 1];
		st_t.nextToken();
		for (int i = 0; i < arr.length; i++) {
			arr[i] = Float.parseFloat(st_t.nextToken());
		}
		return arr;
	}

	public Color getGradient(float min, float val, float max) {
		int d = Math.round((val - min) / (max - min) * 256);
		if (d > 255)
			d = 255;
		if (d < 0)
			d = 0;
		return new Color(bi_grad.getRGB(0, d));
	}

	public void draw() {
		if (time_step.getModel().getSize() == 0)
			return;
		resultGroup.detach();
		resultGroup.removeAllChildren();
		Vector vLines = new Vector();
		Vector vTriangle = new Vector();
		Vector vQuad = new Vector();
		Vector vLinesColor = new Vector();
		Vector vTriangleColor = new Vector();
		Vector vQuadColor = new Vector();
		float kd = 1;
		if (cb_deformed.isSelected())
			try {
				kd = Float.parseFloat(tf_deformed.getText());
			} catch (Exception e) {
				error("Factor of deformation - " + e);
			}
		else
			kd = 0;
		String time = time_step.getSelectedValue() + "";
		// Show nodes label
		if (cb_node.isSelected()) {
			Hashtable db_node_tmp = (Hashtable) db_displacements.get(time);
			for (Enumeration en = db_node_tmp.keys(); en.hasMoreElements();) {
				String key = en.nextElement() + "";
				Point3d p1 = (Point3d) db_node.get(key);
				Point3d p2 = (Point3d) db_node_tmp.get(key);
				resultGroup.addChild(new PostProcessor_shp_oriented_text(key,
						new Point3f((float) (p1.x + p2.x * kd),
								(float) (p1.y + p2.y * kd),
								(float) (p1.z + p2.z * kd)), fontsize));
			}
		}

		// Show elements label
		if (cb_element.isSelected()) {
			Hashtable db_node_tmp = (Hashtable) db_displacements.get(time);
			Hashtable db_stresses_tmp = (Hashtable) db_stresses.get(time);
			for (Enumeration en = db_stresses_tmp.keys(); en.hasMoreElements();) {
				String key = en.nextElement() + "";

				int[] arr = (int[]) db_element.get(key);
				float x = 0, y = 0, z = 0;
				for (int i = 0; i < arr.length; i++) {
					Point3d p = (Point3d) db_node.get(arr[i] + "");
					Point3d pt = (Point3d) db_node_tmp.get(arr[i] + "");
					x += p.x + pt.x * kd;
					y += p.y + pt.y * kd;
					z += p.z + pt.z * kd;
				}
				x /= arr.length;
				y /= arr.length;
				z /= arr.length;
				resultGroup
						.addChild(new PostProcessor_shp_oriented_text(key,
								new Point3f((float) x, (float) y, (float) z),
								fontsize));
			}
		}

		// DISPLACEMENTS (I)
		if (cb_result.getSelectedIndex() < 4) {
			Hashtable db_node_tmp = (Hashtable) db_displacements.get(time);
			Hashtable db_stresses_tmp = (Hashtable) db_stresses.get(time);
			for (Enumeration en = db_stresses_tmp.keys(); en.hasMoreElements();) {
				String key = en.nextElement() + "";

				int[] arr = (int[]) db_element.get(key);
				if (arr.length == 2) {
					Point3d p11 = (Point3d) db_node.get(arr[0] + "");
					Point3d p12 = (Point3d) db_node.get(arr[1] + "");
					Point3d p21 = (Point3d) db_node_tmp.get(arr[0] + "");
					Point3d p22 = (Point3d) db_node_tmp.get(arr[1] + "");
					double x1 = p11.x + p21.x * kd;
					double x2 = p12.x + p22.x * kd;
					double y1 = p11.y + p21.y * kd;
					double y2 = p12.y + p22.y * kd;
					double z1 = p11.z + p21.z * kd;
					double z2 = p12.z + p22.z * kd;
					vLines.addElement(new Point3d(x1, y1, z1));
					vLines.addElement(new Point3d(x2, y2, z2));
					if (cb_result.getSelectedIndex() == 0) {
						if (this.cb_conturfill.isSelected()) {
							vLinesColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p21.x * p21.x + p21.y
											* p21.y + p21.z * p21.z),
									(float) maxdi));
							vLinesColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p22.x * p22.x + p22.y
											* p22.y + p22.z * p22.z),
									(float) maxdi));
						} else {
							vLinesColor.addElement(Color.black);
							vLinesColor.addElement(Color.black);
						}
					} else if (cb_result.getSelectedIndex() == 1) {
						if (this.cb_conturfill.isSelected()) {
							vLinesColor.addElement(getGradient((float) mindx,
									(float) p21.x, (float) maxdx));
							vLinesColor.addElement(getGradient((float) mindx,
									(float) p22.x, (float) maxdx));
						} else {
							vLinesColor.addElement(Color.black);
							vLinesColor.addElement(Color.black);
						}
					} else if (cb_result.getSelectedIndex() == 2) {
						if (this.cb_conturfill.isSelected()) {
							vLinesColor.addElement(getGradient((float) mindy,
									(float) p21.y, (float) maxdy));
							vLinesColor.addElement(getGradient((float) mindy,
									(float) p22.y, (float) maxdy));
						} else {
							vLinesColor.addElement(Color.black);
							vLinesColor.addElement(Color.black);
						}
					} else if (cb_result.getSelectedIndex() == 3) {
						if (this.cb_conturfill.isSelected()) {
							vLinesColor.addElement(getGradient((float) mindz,
									(float) p21.z, (float) maxdz));
							vLinesColor.addElement(getGradient((float) mindz,
									(float) p22.z, (float) maxdz));
						} else {
							vLinesColor.addElement(Color.black);
							vLinesColor.addElement(Color.black);
						}
					}
				} else if (arr.length == 3) {
					Point3d p11 = (Point3d) db_node.get(arr[0] + "");
					Point3d p12 = (Point3d) db_node.get(arr[1] + "");
					Point3d p13 = (Point3d) db_node.get(arr[2] + "");
					Point3d p21 = (Point3d) db_node_tmp.get(arr[0] + "");
					Point3d p22 = (Point3d) db_node_tmp.get(arr[1] + "");
					Point3d p23 = (Point3d) db_node_tmp.get(arr[2] + "");
					double x1 = p11.x + p21.x * kd;
					double x2 = p12.x + p22.x * kd;
					double x3 = p13.x + p23.x * kd;
					double y1 = p11.y + p21.y * kd;
					double y2 = p12.y + p22.y * kd;
					double y3 = p13.y + p23.y * kd;
					double z1 = p11.z + p21.z * kd;
					double z2 = p12.z + p22.z * kd;
					double z3 = p13.z + p23.z * kd;
					if (this.cb_mesh.isSelected()) {
						vLines.addElement(new Point3d(x1, y1, z1));
						vLines.addElement(new Point3d(x2, y2, z2));
						vLines.addElement(new Point3d(x2, y2, z2));
						vLines.addElement(new Point3d(x3, y3, z3));
						vLines.addElement(new Point3d(x3, y3, z3));
						vLines.addElement(new Point3d(x1, y1, z1));
						for (int i = 0; i < 6; i++)
							vLinesColor.addElement(Color.black);
					}
					if (this.cb_conturfill.isSelected()) {
						if (cb_result.getSelectedIndex() == 0) {
							vTriangle.addElement(new Point3d(x1, y1, z1));
							vTriangle.addElement(new Point3d(x2, y2, z2));
							vTriangle.addElement(new Point3d(x3, y3, z3));
							vTriangleColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p21.x * p21.x + p21.y
											* p21.y + p21.z * p21.z),
									(float) maxdi));
							vTriangleColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p22.x * p22.x + p22.y
											* p22.y + p22.z * p22.z),
									(float) maxdi));
							vTriangleColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p23.x * p23.x + p23.y
											* p23.y + p23.z * p23.z),
									(float) maxdi));
						} else if (cb_result.getSelectedIndex() == 1) {
							vTriangle.addElement(new Point3d(x1, y1, z1));
							vTriangle.addElement(new Point3d(x2, y2, z2));
							vTriangle.addElement(new Point3d(x3, y3, z3));
							vTriangleColor
									.addElement(getGradient((float) mindx,
											(float) p21.x, (float) maxdx));
							vTriangleColor
									.addElement(getGradient((float) mindx,
											(float) p22.x, (float) maxdx));
							vTriangleColor
									.addElement(getGradient((float) mindx,
											(float) p23.x, (float) maxdx));
						} else if (cb_result.getSelectedIndex() == 2) {
							vTriangle.addElement(new Point3d(x1, y1, z1));
							vTriangle.addElement(new Point3d(x2, y2, z2));
							vTriangle.addElement(new Point3d(x3, y3, z3));
							vTriangleColor
									.addElement(getGradient((float) mindy,
											(float) p21.y, (float) maxdy));
							vTriangleColor
									.addElement(getGradient((float) mindy,
											(float) p22.y, (float) maxdy));
							vTriangleColor
									.addElement(getGradient((float) mindy,
											(float) p23.y, (float) maxdy));
						} else if (cb_result.getSelectedIndex() == 3) {
							vTriangle.addElement(new Point3d(x1, y1, z1));
							vTriangle.addElement(new Point3d(x2, y2, z2));
							vTriangle.addElement(new Point3d(x3, y3, z3));
							vTriangleColor
									.addElement(getGradient((float) mindz,
											(float) p21.z, (float) maxdz));
							vTriangleColor
									.addElement(getGradient((float) mindz,
											(float) p22.z, (float) maxdz));
							vTriangleColor
									.addElement(getGradient((float) mindz,
											(float) p23.z, (float) maxdz));
						}
					}
				} else if (arr.length == 4) {
					Point3d p11 = (Point3d) db_node.get(arr[0] + "");
					Point3d p12 = (Point3d) db_node.get(arr[1] + "");
					Point3d p13 = (Point3d) db_node.get(arr[2] + "");
					Point3d p14 = (Point3d) db_node.get(arr[3] + "");
					Point3d p21 = (Point3d) db_node_tmp.get(arr[0] + "");
					Point3d p22 = (Point3d) db_node_tmp.get(arr[1] + "");
					Point3d p23 = (Point3d) db_node_tmp.get(arr[2] + "");
					Point3d p24 = (Point3d) db_node_tmp.get(arr[3] + "");
					double x1 = p11.x + p21.x * kd;
					double x2 = p12.x + p22.x * kd;
					double x3 = p13.x + p23.x * kd;
					double x4 = p14.x + p24.x * kd;
					double y1 = p11.y + p21.y * kd;
					double y2 = p12.y + p22.y * kd;
					double y3 = p13.y + p23.y * kd;
					double y4 = p14.y + p24.y * kd;
					double z1 = p11.z + p21.z * kd;
					double z2 = p12.z + p22.z * kd;
					double z3 = p13.z + p23.z * kd;
					double z4 = p14.z + p24.z * kd;
					if (this.cb_mesh.isSelected()) {
						vLines.addElement(new Point3d(x1, y1, z1));
						vLines.addElement(new Point3d(x2, y2, z2));
						vLines.addElement(new Point3d(x2, y2, z2));
						vLines.addElement(new Point3d(x3, y3, z3));
						vLines.addElement(new Point3d(x3, y3, z3));
						vLines.addElement(new Point3d(x4, y4, z4));
						vLines.addElement(new Point3d(x4, y4, z4));
						vLines.addElement(new Point3d(x1, y1, z1));
						for (int i = 0; i < 8; i++)
							vLinesColor.addElement(Color.black);
					}
					if (this.cb_conturfill.isSelected()) {
						if (cb_result.getSelectedIndex() == 0) {
							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x4, y4, z4));
							vQuadColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p21.x * p21.x + p21.y
											* p21.y + p21.z * p21.z),
									(float) maxdi));
							vQuadColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p22.x * p22.x + p22.y
											* p22.y + p22.z * p22.z),
									(float) maxdi));
							vQuadColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p23.x * p23.x + p23.y
											* p23.y + p23.z * p23.z),
									(float) maxdi));
							vQuadColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p24.x * p24.x + p24.y
											* p24.y + p24.z * p24.z),
									(float) maxdi));
						} else if (cb_result.getSelectedIndex() == 1) {
							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x4, y4, z4));
							vQuadColor.addElement(getGradient((float) mindx,
									(float) p21.x, (float) maxdx));
							vQuadColor.addElement(getGradient((float) mindx,
									(float) p22.x, (float) maxdx));
							vQuadColor.addElement(getGradient((float) mindx,
									(float) p23.x, (float) maxdx));
							vQuadColor.addElement(getGradient((float) mindx,
									(float) p24.x, (float) maxdx));
						} else if (cb_result.getSelectedIndex() == 2) {
							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x4, y4, z4));
							vQuadColor.addElement(getGradient((float) mindy,
									(float) p21.y, (float) maxdy));
							vQuadColor.addElement(getGradient((float) mindy,
									(float) p22.y, (float) maxdy));
							vQuadColor.addElement(getGradient((float) mindy,
									(float) p23.y, (float) maxdy));
							vQuadColor.addElement(getGradient((float) mindy,
									(float) p24.y, (float) maxdy));
						} else if (cb_result.getSelectedIndex() == 3) {
							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x4, y4, z4));
							vQuadColor.addElement(getGradient((float) mindz,
									(float) p21.z, (float) maxdz));
							vQuadColor.addElement(getGradient((float) mindz,
									(float) p22.z, (float) maxdz));
							vQuadColor.addElement(getGradient((float) mindz,
									(float) p23.z, (float) maxdz));
							vQuadColor.addElement(getGradient((float) mindz,
									(float) p24.z, (float) maxdz));
						}
					}
				} else if (arr.length == 8) {
					Point3d p11 = (Point3d) db_node.get(arr[0] + "");
					Point3d p12 = (Point3d) db_node.get(arr[1] + "");
					Point3d p13 = (Point3d) db_node.get(arr[2] + "");
					Point3d p14 = (Point3d) db_node.get(arr[3] + "");
					Point3d p15 = (Point3d) db_node.get(arr[4] + "");
					Point3d p16 = (Point3d) db_node.get(arr[5] + "");
					Point3d p17 = (Point3d) db_node.get(arr[6] + "");
					Point3d p18 = (Point3d) db_node.get(arr[7] + "");
					Point3d p21 = (Point3d) db_node_tmp.get(arr[0] + "");
					Point3d p22 = (Point3d) db_node_tmp.get(arr[1] + "");
					Point3d p23 = (Point3d) db_node_tmp.get(arr[2] + "");
					Point3d p24 = (Point3d) db_node_tmp.get(arr[3] + "");
					Point3d p25 = (Point3d) db_node_tmp.get(arr[4] + "");
					Point3d p26 = (Point3d) db_node_tmp.get(arr[5] + "");
					Point3d p27 = (Point3d) db_node_tmp.get(arr[6] + "");
					Point3d p28 = (Point3d) db_node_tmp.get(arr[7] + "");
					double x1 = p11.x + p21.x * kd;
					double x2 = p12.x + p22.x * kd;
					double x3 = p13.x + p23.x * kd;
					double x4 = p14.x + p24.x * kd;
					double x5 = p15.x + p25.x * kd;
					double x6 = p16.x + p26.x * kd;
					double x7 = p17.x + p27.x * kd;
					double x8 = p18.x + p28.x * kd;
					double y1 = p11.y + p21.y * kd;
					double y2 = p12.y + p22.y * kd;
					double y3 = p13.y + p23.y * kd;
					double y4 = p14.y + p24.y * kd;
					double y5 = p15.y + p25.y * kd;
					double y6 = p16.y + p26.y * kd;
					double y7 = p17.y + p27.y * kd;
					double y8 = p18.y + p28.y * kd;
					double z1 = p11.z + p21.z * kd;
					double z2 = p12.z + p22.z * kd;
					double z3 = p13.z + p23.z * kd;
					double z4 = p14.z + p24.z * kd;
					double z5 = p15.z + p25.z * kd;
					double z6 = p16.z + p26.z * kd;
					double z7 = p17.z + p27.z * kd;
					double z8 = p18.z + p28.z * kd;
					if (this.cb_mesh.isSelected()) {
						vLines.addElement(new Point3d(x1, y1, z1));
						vLines.addElement(new Point3d(x2, y2, z2));
						vLines.addElement(new Point3d(x2, y2, z2));
						vLines.addElement(new Point3d(x3, y3, z3));
						vLines.addElement(new Point3d(x3, y3, z3));
						vLines.addElement(new Point3d(x4, y4, z4));
						vLines.addElement(new Point3d(x4, y4, z4));
						vLines.addElement(new Point3d(x1, y1, z1));
						vLines.addElement(new Point3d(x5, y5, z5));
						vLines.addElement(new Point3d(x6, y6, z6));
						vLines.addElement(new Point3d(x6, y6, z6));
						vLines.addElement(new Point3d(x7, y7, z7));
						vLines.addElement(new Point3d(x7, y7, z7));
						vLines.addElement(new Point3d(x8, y8, z8));
						vLines.addElement(new Point3d(x8, y8, z8));
						vLines.addElement(new Point3d(x5, y5, z5));
						vLines.addElement(new Point3d(x2, y2, z2));
						vLines.addElement(new Point3d(x6, y6, z6));
						vLines.addElement(new Point3d(x5, y5, z5));
						vLines.addElement(new Point3d(x1, y1, z1));
						vLines.addElement(new Point3d(x3, y3, z3));
						vLines.addElement(new Point3d(x7, y7, z7));
						vLines.addElement(new Point3d(x8, y8, z8));
						vLines.addElement(new Point3d(x4, y4, z4));
						for (int i = 0; i < 24; i++)
							vLinesColor.addElement(Color.black);
					}
					if (this.cb_conturfill.isSelected()) {
						if (cb_result.getSelectedIndex() == 0) {
							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x4, y4, z4));

							vQuad.addElement(new Point3d(x5, y5, z5));
							vQuad.addElement(new Point3d(x6, y6, z6));
							vQuad.addElement(new Point3d(x7, y7, z7));
							vQuad.addElement(new Point3d(x8, y8, z8));

							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x6, y6, z6));
							vQuad.addElement(new Point3d(x5, y5, z5));

							vQuad.addElement(new Point3d(x4, y4, z4));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x7, y7, z7));
							vQuad.addElement(new Point3d(x8, y8, z8));

							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x7, y7, z7));
							vQuad.addElement(new Point3d(x6, y6, z6));

							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x4, y4, z4));
							vQuad.addElement(new Point3d(x8, y8, z8));
							vQuad.addElement(new Point3d(x5, y5, z5));
							vQuadColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p21.x * p21.x + p21.y
											* p21.y + p21.z * p21.z),
									(float) maxdi));
							vQuadColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p22.x * p22.x + p22.y
											* p22.y + p22.z * p22.z),
									(float) maxdi));
							vQuadColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p23.x * p23.x + p23.y
											* p23.y + p23.z * p23.z),
									(float) maxdi));
							vQuadColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p24.x * p24.x + p24.y
											* p24.y + p24.z * p24.z),
									(float) maxdi));

							vQuadColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p25.x * p25.x + p25.y
											* p25.y + p25.z * p25.z),
									(float) maxdi));
							vQuadColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p26.x * p26.x + p26.y
											* p26.y + p26.z * p26.z),
									(float) maxdi));
							vQuadColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p27.x * p27.x + p27.y
											* p27.y + p27.z * p27.z),
									(float) maxdi));
							vQuadColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p28.x * p28.x + p28.y
											* p28.y + p28.z * p28.z),
									(float) maxdi));

							vQuadColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p21.x * p21.x + p21.y
											* p21.y + p21.z * p21.z),
									(float) maxdi));
							vQuadColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p22.x * p22.x + p22.y
											* p22.y + p22.z * p22.z),
									(float) maxdi));
							vQuadColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p26.x * p26.x + p26.y
											* p26.y + p26.z * p26.z),
									(float) maxdi));
							vQuadColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p25.x * p25.x + p25.y
											* p25.y + p25.z * p25.z),
									(float) maxdi));

							vQuadColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p24.x * p24.x + p24.y
											* p24.y + p24.z * p24.z),
									(float) maxdi));
							vQuadColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p23.x * p23.x + p23.y
											* p23.y + p23.z * p23.z),
									(float) maxdi));
							vQuadColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p27.x * p27.x + p27.y
											* p27.y + p27.z * p27.z),
									(float) maxdi));
							vQuadColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p28.x * p28.x + p28.y
											* p28.y + p28.z * p28.z),
									(float) maxdi));

							vQuadColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p22.x * p22.x + p22.y
											* p22.y + p22.z * p22.z),
									(float) maxdi));
							vQuadColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p23.x * p23.x + p23.y
											* p23.y + p23.z * p23.z),
									(float) maxdi));
							vQuadColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p27.x * p27.x + p27.y
											* p27.y + p27.z * p27.z),
									(float) maxdi));
							vQuadColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p26.x * p26.x + p26.y
											* p26.y + p26.z * p26.z),
									(float) maxdi));

							vQuadColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p21.x * p21.x + p21.y
											* p21.y + p21.z * p21.z),
									(float) maxdi));
							vQuadColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p24.x * p24.x + p24.y
											* p24.y + p24.z * p24.z),
									(float) maxdi));
							vQuadColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p28.x * p28.x + p28.y
											* p28.y + p28.z * p28.z),
									(float) maxdi));
							vQuadColor.addElement(getGradient(
									0,
									(float) Math.sqrt(p25.x * p25.x + p25.y
											* p25.y + p25.z * p25.z),
									(float) maxdi));
						} else if (cb_result.getSelectedIndex() == 1) {
							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x4, y4, z4));

							vQuad.addElement(new Point3d(x5, y5, z5));
							vQuad.addElement(new Point3d(x6, y6, z6));
							vQuad.addElement(new Point3d(x7, y7, z7));
							vQuad.addElement(new Point3d(x8, y8, z8));

							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x6, y6, z6));
							vQuad.addElement(new Point3d(x5, y5, z5));

							vQuad.addElement(new Point3d(x4, y4, z4));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x7, y7, z7));
							vQuad.addElement(new Point3d(x8, y8, z8));

							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x7, y7, z7));
							vQuad.addElement(new Point3d(x6, y6, z6));

							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x4, y4, z4));
							vQuad.addElement(new Point3d(x8, y8, z8));
							vQuad.addElement(new Point3d(x5, y5, z5));
							vQuadColor.addElement(getGradient((float) mindx,
									(float) p21.x, (float) maxdx));
							vQuadColor.addElement(getGradient((float) mindx,
									(float) p22.x, (float) maxdx));
							vQuadColor.addElement(getGradient((float) mindx,
									(float) p23.x, (float) maxdx));
							vQuadColor.addElement(getGradient((float) mindx,
									(float) p24.x, (float) maxdx));

							vQuadColor.addElement(getGradient((float) mindx,
									(float) p25.x, (float) maxdx));
							vQuadColor.addElement(getGradient((float) mindx,
									(float) p26.x, (float) maxdx));
							vQuadColor.addElement(getGradient((float) mindx,
									(float) p27.x, (float) maxdx));
							vQuadColor.addElement(getGradient((float) mindx,
									(float) p28.x, (float) maxdx));

							vQuadColor.addElement(getGradient((float) mindx,
									(float) p21.x, (float) maxdx));
							vQuadColor.addElement(getGradient((float) mindx,
									(float) p22.x, (float) maxdx));
							vQuadColor.addElement(getGradient((float) mindx,
									(float) p26.x, (float) maxdx));
							vQuadColor.addElement(getGradient((float) mindx,
									(float) p25.x, (float) maxdx));

							vQuadColor.addElement(getGradient((float) mindx,
									(float) p24.x, (float) maxdx));
							vQuadColor.addElement(getGradient((float) mindx,
									(float) p23.x, (float) maxdx));
							vQuadColor.addElement(getGradient((float) mindx,
									(float) p27.x, (float) maxdx));
							vQuadColor.addElement(getGradient((float) mindx,
									(float) p28.x, (float) maxdx));

							vQuadColor.addElement(getGradient((float) mindx,
									(float) p22.x, (float) maxdx));
							vQuadColor.addElement(getGradient((float) mindx,
									(float) p23.x, (float) maxdx));
							vQuadColor.addElement(getGradient((float) mindx,
									(float) p27.x, (float) maxdx));
							vQuadColor.addElement(getGradient((float) mindx,
									(float) p26.x, (float) maxdx));

							vQuadColor.addElement(getGradient((float) mindx,
									(float) p21.x, (float) maxdx));
							vQuadColor.addElement(getGradient((float) mindx,
									(float) p24.x, (float) maxdx));
							vQuadColor.addElement(getGradient((float) mindx,
									(float) p28.x, (float) maxdx));
							vQuadColor.addElement(getGradient((float) mindx,
									(float) p25.x, (float) maxdx));
						} else if (cb_result.getSelectedIndex() == 2) {
							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x4, y4, z4));

							vQuad.addElement(new Point3d(x5, y5, z5));
							vQuad.addElement(new Point3d(x6, y6, z6));
							vQuad.addElement(new Point3d(x7, y7, z7));
							vQuad.addElement(new Point3d(x8, y8, z8));

							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x6, y6, z6));
							vQuad.addElement(new Point3d(x5, y5, z5));

							vQuad.addElement(new Point3d(x4, y4, z4));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x7, y7, z7));
							vQuad.addElement(new Point3d(x8, y8, z8));

							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x7, y7, z7));
							vQuad.addElement(new Point3d(x6, y6, z6));

							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x4, y4, z4));
							vQuad.addElement(new Point3d(x8, y8, z8));
							vQuad.addElement(new Point3d(x5, y5, z5));
							vQuadColor.addElement(getGradient((float) mindy,
									(float) p21.y, (float) maxdy));
							vQuadColor.addElement(getGradient((float) mindy,
									(float) p22.y, (float) maxdy));
							vQuadColor.addElement(getGradient((float) mindy,
									(float) p23.y, (float) maxdy));
							vQuadColor.addElement(getGradient((float) mindy,
									(float) p24.y, (float) maxdy));

							vQuadColor.addElement(getGradient((float) mindy,
									(float) p25.y, (float) maxdy));
							vQuadColor.addElement(getGradient((float) mindy,
									(float) p26.y, (float) maxdy));
							vQuadColor.addElement(getGradient((float) mindy,
									(float) p27.y, (float) maxdy));
							vQuadColor.addElement(getGradient((float) mindy,
									(float) p28.y, (float) maxdy));

							vQuadColor.addElement(getGradient((float) mindy,
									(float) p21.y, (float) maxdy));
							vQuadColor.addElement(getGradient((float) mindy,
									(float) p22.y, (float) maxdy));
							vQuadColor.addElement(getGradient((float) mindy,
									(float) p26.y, (float) maxdy));
							vQuadColor.addElement(getGradient((float) mindy,
									(float) p25.y, (float) maxdy));

							vQuadColor.addElement(getGradient((float) mindy,
									(float) p24.y, (float) maxdy));
							vQuadColor.addElement(getGradient((float) mindy,
									(float) p23.y, (float) maxdy));
							vQuadColor.addElement(getGradient((float) mindy,
									(float) p27.y, (float) maxdy));
							vQuadColor.addElement(getGradient((float) mindy,
									(float) p28.y, (float) maxdy));

							vQuadColor.addElement(getGradient((float) mindy,
									(float) p22.y, (float) maxdy));
							vQuadColor.addElement(getGradient((float) mindy,
									(float) p23.y, (float) maxdy));
							vQuadColor.addElement(getGradient((float) mindy,
									(float) p27.y, (float) maxdy));
							vQuadColor.addElement(getGradient((float) mindy,
									(float) p26.y, (float) maxdy));

							vQuadColor.addElement(getGradient((float) mindy,
									(float) p21.y, (float) maxdy));
							vQuadColor.addElement(getGradient((float) mindy,
									(float) p24.y, (float) maxdy));
							vQuadColor.addElement(getGradient((float) mindy,
									(float) p28.y, (float) maxdy));
							vQuadColor.addElement(getGradient((float) mindy,
									(float) p25.y, (float) maxdy));
						} else if (cb_result.getSelectedIndex() == 3) {
							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x4, y4, z4));

							vQuad.addElement(new Point3d(x5, y5, z5));
							vQuad.addElement(new Point3d(x6, y6, z6));
							vQuad.addElement(new Point3d(x7, y7, z7));
							vQuad.addElement(new Point3d(x8, y8, z8));

							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x6, y6, z6));
							vQuad.addElement(new Point3d(x5, y5, z5));

							vQuad.addElement(new Point3d(x4, y4, z4));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x7, y7, z7));
							vQuad.addElement(new Point3d(x8, y8, z8));

							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x7, y7, z7));
							vQuad.addElement(new Point3d(x6, y6, z6));

							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x4, y4, z4));
							vQuad.addElement(new Point3d(x8, y8, z8));
							vQuad.addElement(new Point3d(x5, y5, z5));
							vQuadColor.addElement(getGradient((float) mindz,
									(float) p21.z, (float) maxdz));
							vQuadColor.addElement(getGradient((float) mindz,
									(float) p22.z, (float) maxdz));
							vQuadColor.addElement(getGradient((float) mindz,
									(float) p23.z, (float) maxdz));
							vQuadColor.addElement(getGradient((float) mindz,
									(float) p24.z, (float) maxdz));

							vQuadColor.addElement(getGradient((float) mindz,
									(float) p25.z, (float) maxdz));
							vQuadColor.addElement(getGradient((float) mindz,
									(float) p26.z, (float) maxdz));
							vQuadColor.addElement(getGradient((float) mindz,
									(float) p27.z, (float) maxdz));
							vQuadColor.addElement(getGradient((float) mindz,
									(float) p28.z, (float) maxdz));

							vQuadColor.addElement(getGradient((float) mindz,
									(float) p21.z, (float) maxdz));
							vQuadColor.addElement(getGradient((float) mindz,
									(float) p22.z, (float) maxdz));
							vQuadColor.addElement(getGradient((float) mindz,
									(float) p26.z, (float) maxdz));
							vQuadColor.addElement(getGradient((float) mindz,
									(float) p25.z, (float) maxdz));

							vQuadColor.addElement(getGradient((float) mindz,
									(float) p24.z, (float) maxdz));
							vQuadColor.addElement(getGradient((float) mindz,
									(float) p23.z, (float) maxdz));
							vQuadColor.addElement(getGradient((float) mindz,
									(float) p27.z, (float) maxdz));
							vQuadColor.addElement(getGradient((float) mindz,
									(float) p28.z, (float) maxdz));

							vQuadColor.addElement(getGradient((float) mindz,
									(float) p22.z, (float) maxdz));
							vQuadColor.addElement(getGradient((float) mindz,
									(float) p23.z, (float) maxdz));
							vQuadColor.addElement(getGradient((float) mindz,
									(float) p27.z, (float) maxdz));
							vQuadColor.addElement(getGradient((float) mindz,
									(float) p26.z, (float) maxdz));

							vQuadColor.addElement(getGradient((float) mindz,
									(float) p21.z, (float) maxdz));
							vQuadColor.addElement(getGradient((float) mindz,
									(float) p24.z, (float) maxdz));
							vQuadColor.addElement(getGradient((float) mindz,
									(float) p28.z, (float) maxdz));
							vQuadColor.addElement(getGradient((float) mindz,
									(float) p25.z, (float) maxdz));
						}
					}
				}
			}
			// STRAINS
		} else if (cb_result.getSelectedIndex() < 11) {
			Hashtable db_node_tmp = (Hashtable) db_displacements.get(time);
			Hashtable db_strains_tmp = (Hashtable) db_strains.get(time);
			for (Enumeration en = db_strains_tmp.keys(); en.hasMoreElements();) {
				String key = en.nextElement() + "";

				int[] arr = (int[]) db_element.get(key);
				if (arr.length == 2) {
					Point3d p11 = (Point3d) db_node.get(arr[0] + "");
					Point3d p12 = (Point3d) db_node.get(arr[1] + "");
					Point3d p21 = (Point3d) db_node_tmp.get(arr[0] + "");
					Point3d p22 = (Point3d) db_node_tmp.get(arr[1] + "");
					double x1 = p11.x + p21.x * kd;
					double x2 = p12.x + p22.x * kd;
					double y1 = p11.y + p21.y * kd;
					double y2 = p12.y + p22.y * kd;
					double z1 = p11.z + p21.z * kd;
					double z2 = p12.z + p22.z * kd;
					float[] ee = (float[]) db_strains_tmp.get(key);
					vLines.addElement(new Point3d(x1, y1, z1));
					vLines.addElement(new Point3d(x2, y2, z2));

					if (cb_result.getSelectedIndex() == 4
							|| cb_result.getSelectedIndex() == 5) {
						if (this.cb_conturfill.isSelected()) {
							vLinesColor.addElement(getGradient((float) minei,
									ee[0], (float) maxei));
							vLinesColor.addElement(getGradient((float) minei,
									ee[0], (float) maxei));
						} else {
							vLinesColor.addElement(Color.black);
							vLinesColor.addElement(Color.black);
						}
					} else {
						vLinesColor.addElement(Color.black);
						vLinesColor.addElement(Color.black);
					}
				} else if (arr.length == 3) {
					Point3d p11 = (Point3d) db_node.get(arr[0] + "");
					Point3d p12 = (Point3d) db_node.get(arr[1] + "");
					Point3d p13 = (Point3d) db_node.get(arr[2] + "");
					Point3d p21 = (Point3d) db_node_tmp.get(arr[0] + "");
					Point3d p22 = (Point3d) db_node_tmp.get(arr[1] + "");
					Point3d p23 = (Point3d) db_node_tmp.get(arr[2] + "");
					double x1 = p11.x + p21.x * kd;
					double x2 = p12.x + p22.x * kd;
					double x3 = p13.x + p23.x * kd;
					double y1 = p11.y + p21.y * kd;
					double y2 = p12.y + p22.y * kd;
					double y3 = p13.y + p23.y * kd;
					double z1 = p11.z + p21.z * kd;
					double z2 = p12.z + p22.z * kd;
					double z3 = p13.z + p23.z * kd;
					float[] ee = (float[]) db_strains_tmp.get(key);
					if (this.cb_mesh.isSelected()) {
						vLines.addElement(new Point3d(x1, y1, z1));
						vLines.addElement(new Point3d(x2, y2, z2));
						vLines.addElement(new Point3d(x2, y2, z2));
						vLines.addElement(new Point3d(x3, y3, z3));
						vLines.addElement(new Point3d(x3, y3, z3));
						vLines.addElement(new Point3d(x1, y1, z1));
						for (int i = 0; i < 6; i++)
							vLinesColor.addElement(Color.black);
					}
					if (this.cb_conturfill.isSelected()) {
						if (cb_result.getSelectedIndex() == 4) {
							vTriangle.addElement(new Point3d(x1, y1, z1));
							vTriangle.addElement(new Point3d(x2, y2, z2));
							vTriangle.addElement(new Point3d(x3, y3, z3));
							float ei = (float) (Math.sqrt(2) / 3 * Math
									.sqrt(Math.pow(ee[0] - ee[1], 2)
											+ Math.pow(ee[1] - ee[2], 2)
											+ Math.pow(ee[2] - ee[0], 2)
											+ 1.5
											* (ee[3] * ee[3] + ee[4] * ee[4] + ee[5]
													* ee[5])));
							Color fill = getGradient((float) minei, ei,
									(float) maxei);
							for (int i = 0; i < 3; i++)
								vTriangleColor.addElement(fill);
						} else if (cb_result.getSelectedIndex() == 5) {
							vTriangle.addElement(new Point3d(x1, y1, z1));
							vTriangle.addElement(new Point3d(x2, y2, z2));
							vTriangle.addElement(new Point3d(x3, y3, z3));
							Color fill = getGradient((float) minex, ee[0],
									(float) maxex);
							for (int i = 0; i < 3; i++)
								vTriangleColor.addElement(fill);
						} else if (cb_result.getSelectedIndex() == 6) {
							vTriangle.addElement(new Point3d(x1, y1, z1));
							vTriangle.addElement(new Point3d(x2, y2, z2));
							vTriangle.addElement(new Point3d(x3, y3, z3));
							Color fill = getGradient((float) miney, ee[1],
									(float) maxey);
							for (int i = 0; i < 3; i++)
								vTriangleColor.addElement(fill);
						} else if (cb_result.getSelectedIndex() == 7) {
							vTriangle.addElement(new Point3d(x1, y1, z1));
							vTriangle.addElement(new Point3d(x2, y2, z2));
							vTriangle.addElement(new Point3d(x3, y3, z3));
							Color fill = getGradient((float) minez, ee[2],
									(float) maxez);
							for (int i = 0; i < 3; i++)
								vTriangleColor.addElement(fill);
						} else if (cb_result.getSelectedIndex() == 8) {
							vTriangle.addElement(new Point3d(x1, y1, z1));
							vTriangle.addElement(new Point3d(x2, y2, z2));
							vTriangle.addElement(new Point3d(x3, y3, z3));
							Color fill = getGradient((float) minexy, ee[3],
									(float) maxexy);
							for (int i = 0; i < 3; i++)
								vTriangleColor.addElement(fill);
						} else if (cb_result.getSelectedIndex() == 9) {
							vTriangle.addElement(new Point3d(x1, y1, z1));
							vTriangle.addElement(new Point3d(x2, y2, z2));
							vTriangle.addElement(new Point3d(x3, y3, z3));
							Color fill = getGradient((float) mineyz, ee[4],
									(float) maxeyz);
							for (int i = 0; i < 3; i++)
								vTriangleColor.addElement(fill);
						} else if (cb_result.getSelectedIndex() == 10) {
							vTriangle.addElement(new Point3d(x1, y1, z1));
							vTriangle.addElement(new Point3d(x2, y2, z2));
							vTriangle.addElement(new Point3d(x3, y3, z3));
							Color fill = getGradient((float) minexz, ee[5],
									(float) maxexz);
							for (int i = 0; i < 3; i++)
								vTriangleColor.addElement(fill);
						}
					}
				} else if (arr.length == 4) {
					Point3d p11 = (Point3d) db_node.get(arr[0] + "");
					Point3d p12 = (Point3d) db_node.get(arr[1] + "");
					Point3d p13 = (Point3d) db_node.get(arr[2] + "");
					Point3d p14 = (Point3d) db_node.get(arr[3] + "");
					Point3d p21 = (Point3d) db_node_tmp.get(arr[0] + "");
					Point3d p22 = (Point3d) db_node_tmp.get(arr[1] + "");
					Point3d p23 = (Point3d) db_node_tmp.get(arr[2] + "");
					Point3d p24 = (Point3d) db_node_tmp.get(arr[3] + "");
					double x1 = p11.x + p21.x * kd;
					double x2 = p12.x + p22.x * kd;
					double x3 = p13.x + p23.x * kd;
					double x4 = p14.x + p24.x * kd;
					double y1 = p11.y + p21.y * kd;
					double y2 = p12.y + p22.y * kd;
					double y3 = p13.y + p23.y * kd;
					double y4 = p14.y + p24.y * kd;
					double z1 = p11.z + p21.z * kd;
					double z2 = p12.z + p22.z * kd;
					double z3 = p13.z + p23.z * kd;
					double z4 = p14.z + p24.z * kd;
					float[] ee = (float[]) db_strains_tmp.get(key);
					if (this.cb_mesh.isSelected()) {
						vLines.addElement(new Point3d(x1, y1, z1));
						vLines.addElement(new Point3d(x2, y2, z2));
						vLines.addElement(new Point3d(x2, y2, z2));
						vLines.addElement(new Point3d(x3, y3, z3));
						vLines.addElement(new Point3d(x3, y3, z3));
						vLines.addElement(new Point3d(x4, y4, z4));
						vLines.addElement(new Point3d(x4, y4, z4));
						vLines.addElement(new Point3d(x1, y1, z1));
						for (int i = 0; i < 8; i++)
							vLinesColor.addElement(Color.black);
					}
					if (this.cb_conturfill.isSelected()) {
						if (cb_result.getSelectedIndex() == 4) {
							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x4, y4, z4));
							float ei = (float) (Math.sqrt(2) / 3 * Math
									.sqrt(Math.pow(ee[0] - ee[1], 2)
											+ Math.pow(ee[1] - ee[2], 2)
											+ Math.pow(ee[2] - ee[0], 2)
											+ 1.5
											* (ee[3] * ee[3] + ee[4] * ee[4] + ee[5]
													* ee[5])));
							Color fill = getGradient((float) minei, ei,
									(float) maxei);
							for (int i = 0; i < 4; i++)
								vQuadColor.addElement(fill);
						} else if (cb_result.getSelectedIndex() == 5) {
							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x4, y4, z4));
							Color fill = getGradient((float) minex, ee[0],
									(float) maxex);
							for (int i = 0; i < 4; i++)
								vQuadColor.addElement(fill);
						} else if (cb_result.getSelectedIndex() == 6) {
							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x4, y4, z4));
							Color fill = getGradient((float) miney, ee[1],
									(float) maxey);
							for (int i = 0; i < 4; i++)
								vQuadColor.addElement(fill);
						} else if (cb_result.getSelectedIndex() == 7) {
							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x4, y4, z4));
							Color fill = getGradient((float) minez, ee[2],
									(float) maxez);
							for (int i = 0; i < 4; i++)
								vQuadColor.addElement(fill);
						} else if (cb_result.getSelectedIndex() == 8) {
							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x4, y4, z4));
							Color fill = getGradient((float) minexy, ee[3],
									(float) maxexy);
							for (int i = 0; i < 4; i++)
								vQuadColor.addElement(fill);
						} else if (cb_result.getSelectedIndex() == 9) {
							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x4, y4, z4));
							Color fill = getGradient((float) mineyz, ee[4],
									(float) maxeyz);
							for (int i = 0; i < 4; i++)
								vQuadColor.addElement(fill);
						} else if (cb_result.getSelectedIndex() == 10) {
							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x4, y4, z4));
							Color fill = getGradient((float) minexz, ee[5],
									(float) maxexz);
							for (int i = 0; i < 4; i++)
								vQuadColor.addElement(fill);
						}
					}
				} else if (arr.length == 8) {
					Point3d p11 = (Point3d) db_node.get(arr[0] + "");
					Point3d p12 = (Point3d) db_node.get(arr[1] + "");
					Point3d p13 = (Point3d) db_node.get(arr[2] + "");
					Point3d p14 = (Point3d) db_node.get(arr[3] + "");
					Point3d p15 = (Point3d) db_node.get(arr[4] + "");
					Point3d p16 = (Point3d) db_node.get(arr[5] + "");
					Point3d p17 = (Point3d) db_node.get(arr[6] + "");
					Point3d p18 = (Point3d) db_node.get(arr[7] + "");
					Point3d p21 = (Point3d) db_node_tmp.get(arr[0] + "");
					Point3d p22 = (Point3d) db_node_tmp.get(arr[1] + "");
					Point3d p23 = (Point3d) db_node_tmp.get(arr[2] + "");
					Point3d p24 = (Point3d) db_node_tmp.get(arr[3] + "");
					Point3d p25 = (Point3d) db_node_tmp.get(arr[4] + "");
					Point3d p26 = (Point3d) db_node_tmp.get(arr[5] + "");
					Point3d p27 = (Point3d) db_node_tmp.get(arr[6] + "");
					Point3d p28 = (Point3d) db_node_tmp.get(arr[7] + "");
					double x1 = p11.x + p21.x * kd;
					double x2 = p12.x + p22.x * kd;
					double x3 = p13.x + p23.x * kd;
					double x4 = p14.x + p24.x * kd;
					double x5 = p15.x + p25.x * kd;
					double x6 = p16.x + p26.x * kd;
					double x7 = p17.x + p27.x * kd;
					double x8 = p18.x + p28.x * kd;
					double y1 = p11.y + p21.y * kd;
					double y2 = p12.y + p22.y * kd;
					double y3 = p13.y + p23.y * kd;
					double y4 = p14.y + p24.y * kd;
					double y5 = p15.y + p25.y * kd;
					double y6 = p16.y + p26.y * kd;
					double y7 = p17.y + p27.y * kd;
					double y8 = p18.y + p28.y * kd;
					double z1 = p11.z + p21.z * kd;
					double z2 = p12.z + p22.z * kd;
					double z3 = p13.z + p23.z * kd;
					double z4 = p14.z + p24.z * kd;
					double z5 = p15.z + p25.z * kd;
					double z6 = p16.z + p26.z * kd;
					double z7 = p17.z + p27.z * kd;
					double z8 = p18.z + p28.z * kd;
					float[] ee = (float[]) db_strains_tmp.get(key);
					if (this.cb_mesh.isSelected()) {
						vLines.addElement(new Point3d(x1, y1, z1));
						vLines.addElement(new Point3d(x2, y2, z2));
						vLines.addElement(new Point3d(x2, y2, z2));
						vLines.addElement(new Point3d(x3, y3, z3));
						vLines.addElement(new Point3d(x3, y3, z3));
						vLines.addElement(new Point3d(x4, y4, z4));
						vLines.addElement(new Point3d(x4, y4, z4));
						vLines.addElement(new Point3d(x1, y1, z1));
						vLines.addElement(new Point3d(x5, y5, z5));
						vLines.addElement(new Point3d(x6, y6, z6));
						vLines.addElement(new Point3d(x6, y6, z6));
						vLines.addElement(new Point3d(x7, y7, z7));
						vLines.addElement(new Point3d(x7, y7, z7));
						vLines.addElement(new Point3d(x8, y8, z8));
						vLines.addElement(new Point3d(x8, y8, z8));
						vLines.addElement(new Point3d(x5, y5, z5));
						vLines.addElement(new Point3d(x2, y2, z2));
						vLines.addElement(new Point3d(x6, y6, z6));
						vLines.addElement(new Point3d(x5, y5, z5));
						vLines.addElement(new Point3d(x1, y1, z1));
						vLines.addElement(new Point3d(x3, y3, z3));
						vLines.addElement(new Point3d(x7, y7, z7));
						vLines.addElement(new Point3d(x8, y8, z8));
						vLines.addElement(new Point3d(x4, y4, z4));
						for (int i = 0; i < 24; i++)
							vLinesColor.addElement(Color.black);
					}
					if (this.cb_conturfill.isSelected()) {
						if (cb_result.getSelectedIndex() == 4) {
							if (ee.length == 6) {
								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x4, y4, z4));

								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x5, y5, z5));

								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));

								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x6, y6, z6));

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuad.addElement(new Point3d(x5, y5, z5));
								float ei = (float) (Math.sqrt(2) / 3 * Math
										.sqrt(Math.pow(ee[0] - ee[1], 2)
												+ Math.pow(ee[1] - ee[2], 2)
												+ Math.pow(ee[2] - ee[0], 2)
												+ 1.5
												* (ee[3] * ee[3] + ee[4]
														* ee[4] + ee[5] * ee[5])));
								Color fill = getGradient(0, ei, (float) maxei);
								for (int i = 0; i < 24; i++)
									vQuadColor.addElement(fill);
							} else {
								float ei1 = (float) (Math.sqrt(2) / 3 * Math
										.sqrt(Math.pow(ee[0] - ee[1], 2)
												+ Math.pow(ee[1] - ee[2], 2)
												+ Math.pow(ee[2] - ee[0], 2)
												+ 1.5
												* (ee[3] * ee[3] + ee[4]
														* ee[4] + ee[5] * ee[5])));
								float ei2 = (float) (Math.sqrt(2) / 3 * Math
										.sqrt(Math.pow(ee[6] - ee[7], 2)
												+ Math.pow(ee[7] - ee[8], 2)
												+ Math.pow(ee[8] - ee[6], 2)
												+ 1.5
												* (ee[9] * ee[9] + ee[10]
														* ee[10] + ee[11]
														* ee[11])));
								float ei3 = (float) (Math.sqrt(2) / 3 * Math
										.sqrt(Math.pow(ee[12] - ee[13], 2)
												+ Math.pow(ee[13] - ee[14], 2)
												+ Math.pow(ee[14] - ee[12], 2)
												+ 1.5
												* (ee[15] * ee[15] + ee[16]
														* ee[16] + ee[17]
														* ee[17])));
								float ei4 = (float) (Math.sqrt(2) / 3 * Math
										.sqrt(Math.pow(ee[18] - ee[19], 2)
												+ Math.pow(ee[19] - ee[20], 2)
												+ Math.pow(ee[20] - ee[18], 2)
												+ 1.5
												* (ee[21] * ee[21] + ee[22]
														* ee[22] + ee[23]
														* ee[23])));
								float ei5 = (float) (Math.sqrt(2) / 3 * Math
										.sqrt(Math.pow(ee[24] - ee[25], 2)
												+ Math.pow(ee[25] - ee[26], 2)
												+ Math.pow(ee[26] - ee[24], 2)
												+ 1.5
												* (ee[27] * ee[27] + ee[28]
														* ee[28] + ee[29]
														* ee[29])));
								float ei6 = (float) (Math.sqrt(2) / 3 * Math
										.sqrt(Math.pow(ee[30] - ee[31], 2)
												+ Math.pow(ee[31] - ee[32], 2)
												+ Math.pow(ee[32] - ee[30], 2)
												+ 1.5
												* (ee[33] * ee[33] + ee[34]
														* ee[34] + ee[35]
														* ee[35])));
								float ei7 = (float) (Math.sqrt(2) / 3 * Math
										.sqrt(Math.pow(ee[36] - ee[37], 2)
												+ Math.pow(ee[37] - ee[38], 2)
												+ Math.pow(ee[38] - ee[36], 2)
												+ 1.5
												* (ee[39] * ee[39] + ee[40]
														* ee[40] + ee[41]
														* ee[41])));
								float ei8 = (float) (Math.sqrt(2) / 3 * Math
										.sqrt(Math.pow(ee[42] - ee[43], 2)
												+ Math.pow(ee[43] - ee[44], 2)
												+ Math.pow(ee[44] - ee[42], 2)
												+ 1.5
												* (ee[45] * ee[45] + ee[46]
														* ee[46] + ee[47]
														* ee[47])));
								Color fill1 = getGradient(0, ei1, (float) maxei);
								Color fill2 = getGradient(0, ei2, (float) maxei);
								Color fill3 = getGradient(0, ei3, (float) maxei);
								Color fill4 = getGradient(0, ei4, (float) maxei);
								Color fill5 = getGradient(0, ei5, (float) maxei);
								Color fill6 = getGradient(0, ei6, (float) maxei);
								Color fill7 = getGradient(0, ei7, (float) maxei);
								Color fill8 = getGradient(0, ei8, (float) maxei);
								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill4);

								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuadColor.addElement(fill5);
								vQuadColor.addElement(fill6);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill8);

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill6);
								vQuadColor.addElement(fill5);

								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuadColor.addElement(fill4);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill8);

								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill6);

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill4);
								vQuadColor.addElement(fill8);
								vQuadColor.addElement(fill5);
							}
						} else if (cb_result.getSelectedIndex() == 5) {
							if (ee.length == 6) {
								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x4, y4, z4));

								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x5, y5, z5));

								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));

								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x6, y6, z6));

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuad.addElement(new Point3d(x5, y5, z5));
								Color fill = getGradient((float) minex, ee[0],
										(float) maxex);
								for (int i = 0; i < 24; i++)
									vQuadColor.addElement(fill);
							} else {
								Color fill1 = getGradient((float) minex, ee[0],
										(float) maxex);
								Color fill2 = getGradient((float) minex, ee[6],
										(float) maxex);
								Color fill3 = getGradient((float) minex,
										ee[12], (float) maxex);
								Color fill4 = getGradient((float) minex,
										ee[18], (float) maxex);
								Color fill5 = getGradient((float) minex,
										ee[24], (float) maxex);
								Color fill6 = getGradient((float) minex,
										ee[30], (float) maxex);
								Color fill7 = getGradient((float) minex,
										ee[36], (float) maxex);
								Color fill8 = getGradient((float) minex,
										ee[42], (float) maxex);
								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill4);

								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuadColor.addElement(fill5);
								vQuadColor.addElement(fill6);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill8);

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill6);
								vQuadColor.addElement(fill5);

								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuadColor.addElement(fill4);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill8);

								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill6);

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill4);
								vQuadColor.addElement(fill8);
								vQuadColor.addElement(fill5);
							}
						} else if (cb_result.getSelectedIndex() == 6) {
							if (ee.length == 6) {
								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x4, y4, z4));

								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x5, y5, z5));

								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));

								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x6, y6, z6));

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuad.addElement(new Point3d(x5, y5, z5));
								Color fill = getGradient((float) miney, ee[1],
										(float) maxey);
								for (int i = 0; i < 24; i++)
									vQuadColor.addElement(fill);
							} else {
								Color fill1 = getGradient((float) miney, ee[1],
										(float) maxey);
								Color fill2 = getGradient((float) miney, ee[7],
										(float) maxey);
								Color fill3 = getGradient((float) miney,
										ee[13], (float) maxey);
								Color fill4 = getGradient((float) miney,
										ee[19], (float) maxey);
								Color fill5 = getGradient((float) miney,
										ee[25], (float) maxey);
								Color fill6 = getGradient((float) miney,
										ee[31], (float) maxey);
								Color fill7 = getGradient((float) miney,
										ee[37], (float) maxey);
								Color fill8 = getGradient((float) miney,
										ee[43], (float) maxey);
								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill4);

								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuadColor.addElement(fill5);
								vQuadColor.addElement(fill6);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill8);

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill6);
								vQuadColor.addElement(fill5);

								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuadColor.addElement(fill4);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill8);

								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill6);

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill4);
								vQuadColor.addElement(fill8);
								vQuadColor.addElement(fill5);
							}
						} else if (cb_result.getSelectedIndex() == 7) {
							if (ee.length == 6) {
								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x4, y4, z4));

								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x5, y5, z5));

								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));

								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x6, y6, z6));

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuad.addElement(new Point3d(x5, y5, z5));
								Color fill = getGradient((float) minez, ee[2],
										(float) maxez);
								for (int i = 0; i < 24; i++)
									vQuadColor.addElement(fill);
							} else {
								Color fill1 = getGradient((float) minez, ee[2],
										(float) maxez);
								Color fill2 = getGradient((float) minez, ee[8],
										(float) maxez);
								Color fill3 = getGradient((float) minez,
										ee[14], (float) maxez);
								Color fill4 = getGradient((float) minez,
										ee[20], (float) maxez);
								Color fill5 = getGradient((float) minez,
										ee[26], (float) maxez);
								Color fill6 = getGradient((float) minez,
										ee[32], (float) maxez);
								Color fill7 = getGradient((float) minez,
										ee[38], (float) maxez);
								Color fill8 = getGradient((float) minez,
										ee[44], (float) maxez);
								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill4);

								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuadColor.addElement(fill5);
								vQuadColor.addElement(fill6);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill8);

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill6);
								vQuadColor.addElement(fill5);

								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuadColor.addElement(fill4);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill8);

								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill6);

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill4);
								vQuadColor.addElement(fill8);
								vQuadColor.addElement(fill5);
							}
						} else if (cb_result.getSelectedIndex() == 8) {
							if (ee.length == 6) {
								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x4, y4, z4));

								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x5, y5, z5));

								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));

								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x6, y6, z6));

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuad.addElement(new Point3d(x5, y5, z5));
								Color fill = getGradient((float) minexy, ee[3],
										(float) maxexy);
								for (int i = 0; i < 24; i++)
									vQuadColor.addElement(fill);
							} else {
								Color fill1 = getGradient((float) minexy,
										ee[3], (float) maxexy);
								Color fill2 = getGradient((float) minexy,
										ee[9], (float) maxexy);
								Color fill3 = getGradient((float) minexy,
										ee[15], (float) maxexy);
								Color fill4 = getGradient((float) minexy,
										ee[21], (float) maxexy);
								Color fill5 = getGradient((float) minexy,
										ee[27], (float) maxexy);
								Color fill6 = getGradient((float) minexy,
										ee[33], (float) maxexy);
								Color fill7 = getGradient((float) minexy,
										ee[39], (float) maxexy);
								Color fill8 = getGradient((float) minexy,
										ee[45], (float) maxexy);
								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill4);

								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuadColor.addElement(fill5);
								vQuadColor.addElement(fill6);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill8);

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill6);
								vQuadColor.addElement(fill5);

								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuadColor.addElement(fill4);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill8);

								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill6);

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill4);
								vQuadColor.addElement(fill8);
								vQuadColor.addElement(fill5);
							}
						} else if (cb_result.getSelectedIndex() == 9) {
							if (ee.length == 6) {
								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x4, y4, z4));

								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x5, y5, z5));

								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));

								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x6, y6, z6));

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuad.addElement(new Point3d(x5, y5, z5));
								Color fill = getGradient((float) mineyz, ee[4],
										(float) maxeyz);
								for (int i = 0; i < 24; i++)
									vQuadColor.addElement(fill);
							} else {
								Color fill1 = getGradient((float) mineyz,
										ee[4], (float) maxeyz);
								Color fill2 = getGradient((float) mineyz,
										ee[10], (float) maxeyz);
								Color fill3 = getGradient((float) mineyz,
										ee[16], (float) maxeyz);
								Color fill4 = getGradient((float) mineyz,
										ee[22], (float) maxeyz);
								Color fill5 = getGradient((float) mineyz,
										ee[28], (float) maxeyz);
								Color fill6 = getGradient((float) mineyz,
										ee[34], (float) maxeyz);
								Color fill7 = getGradient((float) mineyz,
										ee[40], (float) maxeyz);
								Color fill8 = getGradient((float) mineyz,
										ee[46], (float) maxeyz);
								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill4);

								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuadColor.addElement(fill5);
								vQuadColor.addElement(fill6);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill8);

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill6);
								vQuadColor.addElement(fill5);

								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuadColor.addElement(fill4);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill8);

								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill6);

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill4);
								vQuadColor.addElement(fill8);
								vQuadColor.addElement(fill5);
							}
						} else if (cb_result.getSelectedIndex() == 10) {
							if (ee.length == 6) {
								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x4, y4, z4));

								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x5, y5, z5));

								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));

								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x6, y6, z6));

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuad.addElement(new Point3d(x5, y5, z5));
								Color fill = getGradient((float) minexz, ee[5],
										(float) maxexz);
								for (int i = 0; i < 24; i++)
									vQuadColor.addElement(fill);
							} else {
								Color fill1 = getGradient((float) minexz,
										ee[5], (float) maxexz);
								Color fill2 = getGradient((float) minexz,
										ee[11], (float) maxexz);
								Color fill3 = getGradient((float) minexz,
										ee[17], (float) maxexz);
								Color fill4 = getGradient((float) minexz,
										ee[23], (float) maxexz);
								Color fill5 = getGradient((float) minexz,
										ee[29], (float) maxexz);
								Color fill6 = getGradient((float) minexz,
										ee[35], (float) maxexz);
								Color fill7 = getGradient((float) minexz,
										ee[41], (float) maxexz);
								Color fill8 = getGradient((float) minexz,
										ee[47], (float) maxexz);
								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill4);

								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuadColor.addElement(fill5);
								vQuadColor.addElement(fill6);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill8);

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill6);
								vQuadColor.addElement(fill5);

								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuadColor.addElement(fill4);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill8);

								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill6);

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill4);
								vQuadColor.addElement(fill8);
								vQuadColor.addElement(fill5);
							}
						}
					}
				}
			}
			// STRESSES
		} else {
			Hashtable db_node_tmp = (Hashtable) db_displacements.get(time);
			Hashtable db_stresses_tmp = (Hashtable) db_stresses.get(time);
			for (Enumeration en = db_stresses_tmp.keys(); en.hasMoreElements();) {
				String key = en.nextElement() + "";

				int[] arr = (int[]) db_element.get(key);
				if (arr.length == 2) {
					Point3d p11 = (Point3d) db_node.get(arr[0] + "");
					Point3d p12 = (Point3d) db_node.get(arr[1] + "");
					Point3d p21 = (Point3d) db_node_tmp.get(arr[0] + "");
					Point3d p22 = (Point3d) db_node_tmp.get(arr[1] + "");
					double x1 = p11.x + p21.x * kd;
					double x2 = p12.x + p22.x * kd;
					double y1 = p11.y + p21.y * kd;
					double y2 = p12.y + p22.y * kd;
					double z1 = p11.z + p21.z * kd;
					double z2 = p12.z + p22.z * kd;
					float[] ee = (float[]) db_stresses_tmp.get(key);
					vLines.addElement(new Point3d(x1, y1, z1));
					vLines.addElement(new Point3d(x2, y2, z2));

					if (cb_result.getSelectedIndex() == 11
							|| cb_result.getSelectedIndex() == 12) {
						if (this.cb_conturfill.isSelected()) {
							vLinesColor.addElement(getGradient((float) minsi,
									ee[0], (float) maxsi));
							vLinesColor.addElement(getGradient((float) minsi,
									ee[0], (float) maxsi));
						} else {
							vLinesColor.addElement(Color.black);
							vLinesColor.addElement(Color.black);
						}
					} else {
						vLinesColor.addElement(Color.black);
						vLinesColor.addElement(Color.black);
					}
				} else if (arr.length == 3) {
					Point3d p11 = (Point3d) db_node.get(arr[0] + "");
					Point3d p12 = (Point3d) db_node.get(arr[1] + "");
					Point3d p13 = (Point3d) db_node.get(arr[2] + "");
					Point3d p21 = (Point3d) db_node_tmp.get(arr[0] + "");
					Point3d p22 = (Point3d) db_node_tmp.get(arr[1] + "");
					Point3d p23 = (Point3d) db_node_tmp.get(arr[2] + "");
					double x1 = p11.x + p21.x * kd;
					double x2 = p12.x + p22.x * kd;
					double x3 = p13.x + p23.x * kd;
					double y1 = p11.y + p21.y * kd;
					double y2 = p12.y + p22.y * kd;
					double y3 = p13.y + p23.y * kd;
					double z1 = p11.z + p21.z * kd;
					double z2 = p12.z + p22.z * kd;
					double z3 = p13.z + p23.z * kd;
					float[] ee = (float[]) db_stresses_tmp.get(key);
					if (this.cb_mesh.isSelected()) {
						vLines.addElement(new Point3d(x1, y1, z1));
						vLines.addElement(new Point3d(x2, y2, z2));
						vLines.addElement(new Point3d(x2, y2, z2));
						vLines.addElement(new Point3d(x3, y3, z3));
						vLines.addElement(new Point3d(x3, y3, z3));
						vLines.addElement(new Point3d(x1, y1, z1));
						for (int i = 0; i < 6; i++)
							vLinesColor.addElement(Color.black);
					}
					if (this.cb_conturfill.isSelected()) {
						if (cb_result.getSelectedIndex() == 11) {
							vTriangle.addElement(new Point3d(x1, y1, z1));
							vTriangle.addElement(new Point3d(x2, y2, z2));
							vTriangle.addElement(new Point3d(x3, y3, z3));
							float ei = (float) (Math.sqrt(2) / 2 * Math
									.sqrt(Math.pow(ee[0] - ee[1], 2)
											+ Math.pow(ee[1] - ee[2], 2)
											+ Math.pow(ee[2] - ee[0], 2)
											+ 6
											* (ee[3] * ee[3] + ee[4] * ee[4] + ee[5]
													* ee[5])));
							Color fill = getGradient((float) minsi, ei,
									(float) maxsi);
							for (int i = 0; i < 3; i++)
								vTriangleColor.addElement(fill);
						} else if (cb_result.getSelectedIndex() == 12) {
							vTriangle.addElement(new Point3d(x1, y1, z1));
							vTriangle.addElement(new Point3d(x2, y2, z2));
							vTriangle.addElement(new Point3d(x3, y3, z3));
							Color fill = getGradient((float) minsx, ee[0],
									(float) maxsx);
							for (int i = 0; i < 3; i++)
								vTriangleColor.addElement(fill);
						} else if (cb_result.getSelectedIndex() == 13) {
							vTriangle.addElement(new Point3d(x1, y1, z1));
							vTriangle.addElement(new Point3d(x2, y2, z2));
							vTriangle.addElement(new Point3d(x3, y3, z3));
							Color fill = getGradient((float) minsy, ee[1],
									(float) maxsy);
							for (int i = 0; i < 3; i++)
								vTriangleColor.addElement(fill);
						} else if (cb_result.getSelectedIndex() == 14) {
							vTriangle.addElement(new Point3d(x1, y1, z1));
							vTriangle.addElement(new Point3d(x2, y2, z2));
							vTriangle.addElement(new Point3d(x3, y3, z3));
							Color fill = getGradient((float) minsz, ee[2],
									(float) maxsz);
							for (int i = 0; i < 3; i++)
								vTriangleColor.addElement(fill);
						} else if (cb_result.getSelectedIndex() == 15) {
							vTriangle.addElement(new Point3d(x1, y1, z1));
							vTriangle.addElement(new Point3d(x2, y2, z2));
							vTriangle.addElement(new Point3d(x3, y3, z3));
							Color fill = getGradient((float) minsxy, ee[3],
									(float) maxsxy);
							for (int i = 0; i < 3; i++)
								vTriangleColor.addElement(fill);
						} else if (cb_result.getSelectedIndex() == 16) {
							vTriangle.addElement(new Point3d(x1, y1, z1));
							vTriangle.addElement(new Point3d(x2, y2, z2));
							vTriangle.addElement(new Point3d(x3, y3, z3));
							Color fill = getGradient((float) minsyz, ee[4],
									(float) maxsyz);
							for (int i = 0; i < 3; i++)
								vTriangleColor.addElement(fill);
						} else if (cb_result.getSelectedIndex() == 17) {
							vTriangle.addElement(new Point3d(x1, y1, z1));
							vTriangle.addElement(new Point3d(x2, y2, z2));
							vTriangle.addElement(new Point3d(x3, y3, z3));
							Color fill = getGradient((float) minsxz, ee[5],
									(float) maxsxz);
							for (int i = 0; i < 3; i++)
								vTriangleColor.addElement(fill);
						}
					}
				} else if (arr.length == 4) {
					Point3d p11 = (Point3d) db_node.get(arr[0] + "");
					Point3d p12 = (Point3d) db_node.get(arr[1] + "");
					Point3d p13 = (Point3d) db_node.get(arr[2] + "");
					Point3d p14 = (Point3d) db_node.get(arr[3] + "");
					Point3d p21 = (Point3d) db_node_tmp.get(arr[0] + "");
					Point3d p22 = (Point3d) db_node_tmp.get(arr[1] + "");
					Point3d p23 = (Point3d) db_node_tmp.get(arr[2] + "");
					Point3d p24 = (Point3d) db_node_tmp.get(arr[3] + "");
					double x1 = p11.x + p21.x * kd;
					double x2 = p12.x + p22.x * kd;
					double x3 = p13.x + p23.x * kd;
					double x4 = p14.x + p24.x * kd;
					double y1 = p11.y + p21.y * kd;
					double y2 = p12.y + p22.y * kd;
					double y3 = p13.y + p23.y * kd;
					double y4 = p14.y + p24.y * kd;
					double z1 = p11.z + p21.z * kd;
					double z2 = p12.z + p22.z * kd;
					double z3 = p13.z + p23.z * kd;
					double z4 = p14.z + p24.z * kd;
					float[] ee = (float[]) db_stresses_tmp.get(key);
					if (this.cb_mesh.isSelected()) {
						vLines.addElement(new Point3d(x1, y1, z1));
						vLines.addElement(new Point3d(x2, y2, z2));
						vLines.addElement(new Point3d(x2, y2, z2));
						vLines.addElement(new Point3d(x3, y3, z3));
						vLines.addElement(new Point3d(x3, y3, z3));
						vLines.addElement(new Point3d(x4, y4, z4));
						vLines.addElement(new Point3d(x4, y4, z4));
						vLines.addElement(new Point3d(x1, y1, z1));
						for (int i = 0; i < 8; i++)
							vLinesColor.addElement(Color.black);
					}
					if (this.cb_conturfill.isSelected()) {
						if (cb_result.getSelectedIndex() == 11) {
							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x4, y4, z4));
							float ei = (float) (Math.sqrt(2) / 2 * Math
									.sqrt(Math.pow(ee[0] - ee[1], 2)
											+ Math.pow(ee[1] - ee[2], 2)
											+ Math.pow(ee[2] - ee[0], 2)
											+ 6
											* (ee[3] * ee[3] + ee[4] * ee[4] + ee[5]
													* ee[5])));
							Color fill = getGradient((float) minsi, ei,
									(float) maxsi);
							for (int i = 0; i < 4; i++)
								vQuadColor.addElement(fill);
						} else if (cb_result.getSelectedIndex() == 12) {
							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x4, y4, z4));
							Color fill = getGradient((float) minsx, ee[0],
									(float) maxsx);
							for (int i = 0; i < 4; i++)
								vQuadColor.addElement(fill);
						} else if (cb_result.getSelectedIndex() == 13) {
							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x4, y4, z4));
							Color fill = getGradient((float) minsy, ee[1],
									(float) maxsy);
							for (int i = 0; i < 4; i++)
								vQuadColor.addElement(fill);
						} else if (cb_result.getSelectedIndex() == 14) {
							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x4, y4, z4));
							Color fill = getGradient((float) minsz, ee[2],
									(float) maxsz);
							for (int i = 0; i < 4; i++)
								vQuadColor.addElement(fill);
						} else if (cb_result.getSelectedIndex() == 15) {
							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x4, y4, z4));
							Color fill = getGradient((float) minsxy, ee[3],
									(float) maxsxy);
							for (int i = 0; i < 4; i++)
								vQuadColor.addElement(fill);
						} else if (cb_result.getSelectedIndex() == 16) {
							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x4, y4, z4));
							Color fill = getGradient((float) minsyz, ee[4],
									(float) maxsyz);
							for (int i = 0; i < 4; i++)
								vQuadColor.addElement(fill);
						} else if (cb_result.getSelectedIndex() == 17) {
							vQuad.addElement(new Point3d(x1, y1, z1));
							vQuad.addElement(new Point3d(x2, y2, z2));
							vQuad.addElement(new Point3d(x3, y3, z3));
							vQuad.addElement(new Point3d(x4, y4, z4));
							Color fill = getGradient((float) minsxz, ee[5],
									(float) maxsxz);
							for (int i = 0; i < 4; i++)
								vQuadColor.addElement(fill);
						}
					}
				} else if (arr.length == 8) {
					Point3d p11 = (Point3d) db_node.get(arr[0] + "");
					Point3d p12 = (Point3d) db_node.get(arr[1] + "");
					Point3d p13 = (Point3d) db_node.get(arr[2] + "");
					Point3d p14 = (Point3d) db_node.get(arr[3] + "");
					Point3d p15 = (Point3d) db_node.get(arr[4] + "");
					Point3d p16 = (Point3d) db_node.get(arr[5] + "");
					Point3d p17 = (Point3d) db_node.get(arr[6] + "");
					Point3d p18 = (Point3d) db_node.get(arr[7] + "");
					Point3d p21 = (Point3d) db_node_tmp.get(arr[0] + "");
					Point3d p22 = (Point3d) db_node_tmp.get(arr[1] + "");
					Point3d p23 = (Point3d) db_node_tmp.get(arr[2] + "");
					Point3d p24 = (Point3d) db_node_tmp.get(arr[3] + "");
					Point3d p25 = (Point3d) db_node_tmp.get(arr[4] + "");
					Point3d p26 = (Point3d) db_node_tmp.get(arr[5] + "");
					Point3d p27 = (Point3d) db_node_tmp.get(arr[6] + "");
					Point3d p28 = (Point3d) db_node_tmp.get(arr[7] + "");
					double x1 = p11.x + p21.x * kd;
					double x2 = p12.x + p22.x * kd;
					double x3 = p13.x + p23.x * kd;
					double x4 = p14.x + p24.x * kd;
					double x5 = p15.x + p25.x * kd;
					double x6 = p16.x + p26.x * kd;
					double x7 = p17.x + p27.x * kd;
					double x8 = p18.x + p28.x * kd;
					double y1 = p11.y + p21.y * kd;
					double y2 = p12.y + p22.y * kd;
					double y3 = p13.y + p23.y * kd;
					double y4 = p14.y + p24.y * kd;
					double y5 = p15.y + p25.y * kd;
					double y6 = p16.y + p26.y * kd;
					double y7 = p17.y + p27.y * kd;
					double y8 = p18.y + p28.y * kd;
					double z1 = p11.z + p21.z * kd;
					double z2 = p12.z + p22.z * kd;
					double z3 = p13.z + p23.z * kd;
					double z4 = p14.z + p24.z * kd;
					double z5 = p15.z + p25.z * kd;
					double z6 = p16.z + p26.z * kd;
					double z7 = p17.z + p27.z * kd;
					double z8 = p18.z + p28.z * kd;
					float[] ee = (float[]) db_stresses_tmp.get(key);
					if (this.cb_mesh.isSelected()) {
						vLines.addElement(new Point3d(x1, y1, z1));
						vLines.addElement(new Point3d(x2, y2, z2));
						vLines.addElement(new Point3d(x2, y2, z2));
						vLines.addElement(new Point3d(x3, y3, z3));
						vLines.addElement(new Point3d(x3, y3, z3));
						vLines.addElement(new Point3d(x4, y4, z4));
						vLines.addElement(new Point3d(x4, y4, z4));
						vLines.addElement(new Point3d(x1, y1, z1));
						vLines.addElement(new Point3d(x5, y5, z5));
						vLines.addElement(new Point3d(x6, y6, z6));
						vLines.addElement(new Point3d(x6, y6, z6));
						vLines.addElement(new Point3d(x7, y7, z7));
						vLines.addElement(new Point3d(x7, y7, z7));
						vLines.addElement(new Point3d(x8, y8, z8));
						vLines.addElement(new Point3d(x8, y8, z8));
						vLines.addElement(new Point3d(x5, y5, z5));
						vLines.addElement(new Point3d(x2, y2, z2));
						vLines.addElement(new Point3d(x6, y6, z6));
						vLines.addElement(new Point3d(x5, y5, z5));
						vLines.addElement(new Point3d(x1, y1, z1));
						vLines.addElement(new Point3d(x3, y3, z3));
						vLines.addElement(new Point3d(x7, y7, z7));
						vLines.addElement(new Point3d(x8, y8, z8));
						vLines.addElement(new Point3d(x4, y4, z4));
						for (int i = 0; i < 24; i++)
							vLinesColor.addElement(Color.black);
					}
					if (this.cb_conturfill.isSelected()) {
						if (cb_result.getSelectedIndex() == 11) {
							if (ee.length == 6) {
								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x4, y4, z4));

								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x5, y5, z5));

								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));

								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x6, y6, z6));

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuad.addElement(new Point3d(x5, y5, z5));
								float ei = (float) (Math.sqrt(2) / 2 * Math
										.sqrt(Math.pow(ee[0] - ee[1], 2)
												+ Math.pow(ee[1] - ee[2], 2)
												+ Math.pow(ee[2] - ee[0], 2)
												+ 6
												* (ee[3] * ee[3] + ee[4]
														* ee[4] + ee[5] * ee[5])));
								Color fill = getGradient(0, ei, (float) maxsi);
								for (int i = 0; i < 24; i++)
									vQuadColor.addElement(fill);
							} else {
								float ei1 = (float) (Math.sqrt(2) / 2 * Math
										.sqrt(Math.pow(ee[0] - ee[1], 2)
												+ Math.pow(ee[1] - ee[2], 2)
												+ Math.pow(ee[2] - ee[0], 2)
												+ 6
												* (ee[3] * ee[3] + ee[4]
														* ee[4] + ee[5] * ee[5])));
								float ei2 = (float) (Math.sqrt(2) / 2 * Math
										.sqrt(Math.pow(ee[6] - ee[7], 2)
												+ Math.pow(ee[7] - ee[8], 2)
												+ Math.pow(ee[8] - ee[6], 2)
												+ 6
												* (ee[9] * ee[9] + ee[10]
														* ee[10] + ee[11]
														* ee[11])));
								float ei3 = (float) (Math.sqrt(2) / 2 * Math
										.sqrt(Math.pow(ee[12] - ee[13], 2)
												+ Math.pow(ee[13] - ee[14], 2)
												+ Math.pow(ee[14] - ee[12], 2)
												+ 6
												* (ee[15] * ee[15] + ee[16]
														* ee[16] + ee[17]
														* ee[17])));
								float ei4 = (float) (Math.sqrt(2) / 2 * Math
										.sqrt(Math.pow(ee[18] - ee[19], 2)
												+ Math.pow(ee[19] - ee[20], 2)
												+ Math.pow(ee[20] - ee[18], 2)
												+ 6
												* (ee[21] * ee[21] + ee[22]
														* ee[22] + ee[23]
														* ee[23])));
								float ei5 = (float) (Math.sqrt(2) / 2 * Math
										.sqrt(Math.pow(ee[24] - ee[25], 2)
												+ Math.pow(ee[25] - ee[26], 2)
												+ Math.pow(ee[26] - ee[24], 2)
												+ 6
												* (ee[27] * ee[27] + ee[28]
														* ee[28] + ee[29]
														* ee[29])));
								float ei6 = (float) (Math.sqrt(2) / 2 * Math
										.sqrt(Math.pow(ee[30] - ee[31], 2)
												+ Math.pow(ee[31] - ee[32], 2)
												+ Math.pow(ee[32] - ee[30], 2)
												+ 6
												* (ee[33] * ee[33] + ee[34]
														* ee[34] + ee[35]
														* ee[35])));
								float ei7 = (float) (Math.sqrt(2) / 2 * Math
										.sqrt(Math.pow(ee[36] - ee[37], 2)
												+ Math.pow(ee[37] - ee[38], 2)
												+ Math.pow(ee[38] - ee[36], 2)
												+ 6
												* (ee[39] * ee[39] + ee[40]
														* ee[40] + ee[41]
														* ee[41])));
								float ei8 = (float) (Math.sqrt(2) / 2 * Math
										.sqrt(Math.pow(ee[42] - ee[43], 2)
												+ Math.pow(ee[43] - ee[44], 2)
												+ Math.pow(ee[44] - ee[42], 2)
												+ 6
												* (ee[45] * ee[45] + ee[46]
														* ee[46] + ee[47]
														* ee[47])));
								Color fill1 = getGradient(0, ei1, (float) maxsi);
								Color fill2 = getGradient(0, ei2, (float) maxsi);
								Color fill3 = getGradient(0, ei3, (float) maxsi);
								Color fill4 = getGradient(0, ei4, (float) maxsi);
								Color fill5 = getGradient(0, ei5, (float) maxsi);
								Color fill6 = getGradient(0, ei6, (float) maxsi);
								Color fill7 = getGradient(0, ei7, (float) maxsi);
								Color fill8 = getGradient(0, ei8, (float) maxsi);
								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill4);

								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuadColor.addElement(fill5);
								vQuadColor.addElement(fill6);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill8);

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill6);
								vQuadColor.addElement(fill5);

								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuadColor.addElement(fill4);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill8);

								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill6);

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill4);
								vQuadColor.addElement(fill8);
								vQuadColor.addElement(fill5);
							}
						} else if (cb_result.getSelectedIndex() == 12) {
							if (ee.length == 6) {
								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x4, y4, z4));

								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x5, y5, z5));

								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));

								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x6, y6, z6));

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuad.addElement(new Point3d(x5, y5, z5));
								Color fill = getGradient((float) minsx, ee[0],
										(float) maxsx);
								for (int i = 0; i < 24; i++)
									vQuadColor.addElement(fill);
							} else {
								Color fill1 = getGradient((float) minsx, ee[0],
										(float) maxsx);
								Color fill2 = getGradient((float) minsx, ee[6],
										(float) maxsx);
								Color fill3 = getGradient((float) minsx,
										ee[12], (float) maxsx);
								Color fill4 = getGradient((float) minsx,
										ee[18], (float) maxsx);
								Color fill5 = getGradient((float) minsx,
										ee[24], (float) maxsx);
								Color fill6 = getGradient((float) minsx,
										ee[30], (float) maxsx);
								Color fill7 = getGradient((float) minsx,
										ee[36], (float) maxsx);
								Color fill8 = getGradient((float) minsx,
										ee[42], (float) maxsx);
								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill4);

								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuadColor.addElement(fill5);
								vQuadColor.addElement(fill6);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill8);

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill6);
								vQuadColor.addElement(fill5);

								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuadColor.addElement(fill4);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill8);

								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill6);

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill4);
								vQuadColor.addElement(fill8);
								vQuadColor.addElement(fill5);
							}
						} else if (cb_result.getSelectedIndex() == 13) {
							if (ee.length == 6) {
								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x4, y4, z4));

								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x5, y5, z5));

								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));

								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x6, y6, z6));

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuad.addElement(new Point3d(x5, y5, z5));
								Color fill = getGradient((float) minsy, ee[1],
										(float) maxsy);
								for (int i = 0; i < 24; i++)
									vQuadColor.addElement(fill);
							} else {
								Color fill1 = getGradient((float) minsy, ee[1],
										(float) maxsy);
								Color fill2 = getGradient((float) minsy, ee[7],
										(float) maxsy);
								Color fill3 = getGradient((float) minsy,
										ee[13], (float) maxsy);
								Color fill4 = getGradient((float) minsy,
										ee[19], (float) maxsy);
								Color fill5 = getGradient((float) minsy,
										ee[25], (float) maxsy);
								Color fill6 = getGradient((float) minsy,
										ee[31], (float) maxsy);
								Color fill7 = getGradient((float) minsy,
										ee[37], (float) maxsy);
								Color fill8 = getGradient((float) minsy,
										ee[43], (float) maxsy);
								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill4);

								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuadColor.addElement(fill5);
								vQuadColor.addElement(fill6);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill8);

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill6);
								vQuadColor.addElement(fill5);

								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuadColor.addElement(fill4);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill8);

								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill6);

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill4);
								vQuadColor.addElement(fill8);
								vQuadColor.addElement(fill5);
							}
						} else if (cb_result.getSelectedIndex() == 14) {
							if (ee.length == 6) {
								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x4, y4, z4));

								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x5, y5, z5));

								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));

								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x6, y6, z6));

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuad.addElement(new Point3d(x5, y5, z5));
								Color fill = getGradient((float) minsz, ee[2],
										(float) maxsz);
								for (int i = 0; i < 24; i++)
									vQuadColor.addElement(fill);
							} else {
								Color fill1 = getGradient((float) minsz, ee[2],
										(float) maxsz);
								Color fill2 = getGradient((float) minsz, ee[8],
										(float) maxsz);
								Color fill3 = getGradient((float) minsz,
										ee[14], (float) maxsz);
								Color fill4 = getGradient((float) minsz,
										ee[20], (float) maxsz);
								Color fill5 = getGradient((float) minsz,
										ee[26], (float) maxsz);
								Color fill6 = getGradient((float) minsz,
										ee[32], (float) maxsz);
								Color fill7 = getGradient((float) minsz,
										ee[38], (float) maxsz);
								Color fill8 = getGradient((float) minsz,
										ee[44], (float) maxsz);
								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill4);

								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuadColor.addElement(fill5);
								vQuadColor.addElement(fill6);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill8);

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill6);
								vQuadColor.addElement(fill5);

								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuadColor.addElement(fill4);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill8);

								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill6);

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill4);
								vQuadColor.addElement(fill8);
								vQuadColor.addElement(fill5);
							}
						} else if (cb_result.getSelectedIndex() == 15) {
							if (ee.length == 6) {
								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x4, y4, z4));

								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x5, y5, z5));

								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));

								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x6, y6, z6));

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuad.addElement(new Point3d(x5, y5, z5));
								Color fill = getGradient((float) minsxy, ee[3],
										(float) maxsxy);
								for (int i = 0; i < 24; i++)
									vQuadColor.addElement(fill);
							} else {
								Color fill1 = getGradient((float) minsxy,
										ee[3], (float) maxsxy);
								Color fill2 = getGradient((float) minsxy,
										ee[9], (float) maxsxy);
								Color fill3 = getGradient((float) minsxy,
										ee[15], (float) maxsxy);
								Color fill4 = getGradient((float) minsxy,
										ee[21], (float) maxsxy);
								Color fill5 = getGradient((float) minsxy,
										ee[27], (float) maxsxy);
								Color fill6 = getGradient((float) minsxy,
										ee[33], (float) maxsxy);
								Color fill7 = getGradient((float) minsxy,
										ee[39], (float) maxsxy);
								Color fill8 = getGradient((float) minsxy,
										ee[45], (float) maxsxy);
								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill4);

								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuadColor.addElement(fill5);
								vQuadColor.addElement(fill6);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill8);

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill6);
								vQuadColor.addElement(fill5);

								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuadColor.addElement(fill4);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill8);

								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill6);

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill4);
								vQuadColor.addElement(fill8);
								vQuadColor.addElement(fill5);
							}
						} else if (cb_result.getSelectedIndex() == 16) {
							if (ee.length == 6) {
								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x4, y4, z4));

								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x5, y5, z5));

								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));

								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x6, y6, z6));

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuad.addElement(new Point3d(x5, y5, z5));
								Color fill = getGradient((float) minsyz, ee[4],
										(float) maxsyz);
								for (int i = 0; i < 24; i++)
									vQuadColor.addElement(fill);
							} else {
								Color fill1 = getGradient((float) minsyz,
										ee[4], (float) maxsyz);
								Color fill2 = getGradient((float) minsyz,
										ee[10], (float) maxsyz);
								Color fill3 = getGradient((float) minsyz,
										ee[16], (float) maxsyz);
								Color fill4 = getGradient((float) minsyz,
										ee[22], (float) maxsyz);
								Color fill5 = getGradient((float) minsyz,
										ee[28], (float) maxsyz);
								Color fill6 = getGradient((float) minsyz,
										ee[34], (float) maxsyz);
								Color fill7 = getGradient((float) minsyz,
										ee[40], (float) maxsyz);
								Color fill8 = getGradient((float) minsyz,
										ee[46], (float) maxsyz);
								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill4);

								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuadColor.addElement(fill5);
								vQuadColor.addElement(fill6);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill8);

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill6);
								vQuadColor.addElement(fill5);

								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuadColor.addElement(fill4);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill8);

								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill6);

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill4);
								vQuadColor.addElement(fill8);
								vQuadColor.addElement(fill5);
							}
						} else if (cb_result.getSelectedIndex() == 17) {
							if (ee.length == 6) {
								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x4, y4, z4));

								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x5, y5, z5));

								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));

								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x6, y6, z6));

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuad.addElement(new Point3d(x5, y5, z5));
								Color fill = getGradient((float) minsxz, ee[5],
										(float) maxsxz);
								for (int i = 0; i < 24; i++)
									vQuadColor.addElement(fill);
							} else {
								Color fill1 = getGradient((float) minsxz,
										ee[5], (float) maxsxz);
								Color fill2 = getGradient((float) minsxz,
										ee[11], (float) maxsxz);
								Color fill3 = getGradient((float) minsxz,
										ee[17], (float) maxsxz);
								Color fill4 = getGradient((float) minsxz,
										ee[23], (float) maxsxz);
								Color fill5 = getGradient((float) minsxz,
										ee[29], (float) maxsxz);
								Color fill6 = getGradient((float) minsxz,
										ee[35], (float) maxsxz);
								Color fill7 = getGradient((float) minsxz,
										ee[41], (float) maxsxz);
								Color fill8 = getGradient((float) minsxz,
										ee[47], (float) maxsxz);
								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill4);

								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuadColor.addElement(fill5);
								vQuadColor.addElement(fill6);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill8);

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill6);
								vQuadColor.addElement(fill5);

								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuadColor.addElement(fill4);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill8);

								vQuad.addElement(new Point3d(x2, y2, z2));
								vQuad.addElement(new Point3d(x3, y3, z3));
								vQuad.addElement(new Point3d(x7, y7, z7));
								vQuad.addElement(new Point3d(x6, y6, z6));
								vQuadColor.addElement(fill2);
								vQuadColor.addElement(fill3);
								vQuadColor.addElement(fill7);
								vQuadColor.addElement(fill6);

								vQuad.addElement(new Point3d(x1, y1, z1));
								vQuad.addElement(new Point3d(x4, y4, z4));
								vQuad.addElement(new Point3d(x8, y8, z8));
								vQuad.addElement(new Point3d(x5, y5, z5));
								vQuadColor.addElement(fill1);
								vQuadColor.addElement(fill4);
								vQuadColor.addElement(fill8);
								vQuadColor.addElement(fill5);
							}
						}
					}
				}
			}
		}

		if (cb_gradientresult.isSelected()) {
			BufferedImage image = (BufferedImage) createImage(130, 290);
			Graphics g = image.getGraphics();
			double min = 0;
			double max = 0;
			if (cb_result.getSelectedIndex() == 0) {
				max = maxdi;
				min = mindi;
			} else if (cb_result.getSelectedIndex() == 1) {
				max = maxdx;
				min = mindx;
			} else if (cb_result.getSelectedIndex() == 2) {
				max = maxdy;
				min = mindy;
			} else if (cb_result.getSelectedIndex() == 3) {
				max = maxdz;
				min = mindz;
			} else if (cb_result.getSelectedIndex() == 4) {
				max = maxei;
				min = minei;
			} else if (cb_result.getSelectedIndex() == 5) {
				max = maxex;
				min = minex;
			} else if (cb_result.getSelectedIndex() == 6) {
				max = maxey;
				min = miney;
			} else if (cb_result.getSelectedIndex() == 7) {
				max = maxez;
				min = minez;
			} else if (cb_result.getSelectedIndex() == 8) {
				max = maxexy;
				min = minexy;
			} else if (cb_result.getSelectedIndex() == 9) {
				max = maxeyz;
				min = mineyz;
			} else if (cb_result.getSelectedIndex() == 10) {
				max = maxexz;
				min = minexz;
			} else if (cb_result.getSelectedIndex() == 11) {
				max = maxsi;
				min = minsi;
			} else if (cb_result.getSelectedIndex() == 12) {
				max = maxsx;
				min = minsx;
			} else if (cb_result.getSelectedIndex() == 13) {
				max = maxsy;
				min = minsy;
			} else if (cb_result.getSelectedIndex() == 14) {
				max = maxsz;
				min = minsz;
			} else if (cb_result.getSelectedIndex() == 15) {
				max = maxsxy;
				min = minsxy;
			} else if (cb_result.getSelectedIndex() == 16) {
				max = maxsyz;
				min = minsyz;
			} else if (cb_result.getSelectedIndex() == 17) {
				max = maxsxz;
				min = minsxz;
			}
			if (max < min)
				min = 0;
			g.setColor(Color.white);
			g.fillRect(0, 0, image.getWidth(), image.getHeight());
			g.setColor(Color.black);
			g.drawRect(0, 0, image.getWidth() - 2, image.getHeight() - 2);
			g.drawString(cb_result.getSelectedItem() + "", 5, 15);
			for (int i = 0; i < 17; i++) {
				String st_val = "" + (min + (max - min) / 16 * i);
				int len = 16;
				if (st_val.length() > len) {
					if (st_val.toUpperCase().indexOf("E") != -1) {
						String st2 = st_val.substring(st_val.toUpperCase()
								.indexOf("E"), st_val.length());
						String st1 = st_val.substring(0, len - st2.length());
						st_val = st1 + st2;
					} else {
						st_val = st_val.substring(0, len);
					}
				}
				g.drawString(st_val, 16, i * 16 + 5 + 25);
			}
			for (int i = 5; i < 15; i++)
				g.drawImage(img_gradient.getImage(), i, 25, null);
			bgNode.setImage(new ImageComponent2D(ImageComponent2D.FORMAT_RGB8,
					image));
		} else {
			bgNode.setImage(null);
		}

		// Shape3D Line
		if (vLines.size() > 0) {
			Point3d[] MPoint = new Point3d[vLines.size()];
			Color3f[] MColor = new Color3f[vLinesColor.size()];
			for (int i = 0; i < vLines.size(); i++) {
				MPoint[i] = (Point3d) vLines.elementAt(i);
				MColor[i] = new Color3f((Color) vLinesColor.elementAt(i));
			}
			Shape3D Shape3DLine = new Shape3D();
			Shape3DLine.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
			LineArray gla = new LineArray(MPoint.length, LineArray.COORDINATES
					| LineArray.COLOR_3);
			gla.setCapability(LineArray.ALLOW_COUNT_READ);
			gla.setCapability(LineArray.ALLOW_FORMAT_READ);
			gla.setCapability(LineArray.ALLOW_COORDINATE_READ);
			gla.setCoordinates(0, MPoint);
			gla.setColors(0, MColor);
			Shape3DLine.setGeometry(gla, 0);
			resultGroup.addChild(Shape3DLine);
		}
		// Shape3D Triangle
		if (vTriangle.size() > 0) {
			Point3d[] MPoint = new Point3d[vTriangle.size()];
			Color3f[] MColor = new Color3f[vTriangleColor.size()];
			for (int i = 0; i < vTriangle.size(); i++) {
				MPoint[i] = (Point3d) vTriangle.elementAt(i);
				MColor[i] = new Color3f((Color) vTriangleColor.elementAt(i));
			}
			Appearance app = new Appearance();
			PolygonAttributes pa = new PolygonAttributes();
			pa.setCullFace(PolygonAttributes.CULL_NONE);
			app.setPolygonAttributes(pa);
			Shape3D Shape3DTriangle = new Shape3D();
			Shape3DTriangle.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
			TriangleArray gta = new TriangleArray(MPoint.length,
					TriangleArray.COORDINATES | TriangleArray.COLOR_3);
			gta.setCapability(TriangleArray.ALLOW_COUNT_READ);
			gta.setCapability(TriangleArray.ALLOW_FORMAT_READ);
			gta.setCapability(TriangleArray.ALLOW_COORDINATE_READ);
			gta.setCoordinates(0, MPoint);
			gta.setColors(0, MColor);
			Shape3DTriangle.setAppearance(app);
			Shape3DTriangle.setGeometry(gta, 0);
			resultGroup.addChild(Shape3DTriangle);
		}
		// Shape3D Quad
		if (vQuad.size() > 0) {
			Point3d[] MPoint = new Point3d[vQuad.size()];
			Color3f[] MColor = new Color3f[vQuadColor.size()];
			for (int i = 0; i < vQuad.size(); i++) {
				MPoint[i] = (Point3d) vQuad.elementAt(i);
				MColor[i] = new Color3f((Color) vQuadColor.elementAt(i));
			}
			Appearance app = new Appearance();
			PolygonAttributes pa = new PolygonAttributes();
			pa.setCullFace(PolygonAttributes.CULL_NONE);
			app.setPolygonAttributes(pa);
			Shape3D Shape3DQuad = new Shape3D();
			Shape3DQuad.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
			QuadArray gqa = new QuadArray(MPoint.length,
					TriangleArray.COORDINATES | TriangleArray.COLOR_3);
			gqa.setCapability(QuadArray.ALLOW_COUNT_READ);
			gqa.setCapability(QuadArray.ALLOW_FORMAT_READ);
			gqa.setCapability(QuadArray.ALLOW_COORDINATE_READ);
			gqa.setCoordinates(0, MPoint);
			gqa.setColors(0, MColor);
			Shape3DQuad.setAppearance(app);
			Shape3DQuad.setGeometry(gqa, 0);
			resultGroup.addChild(Shape3DQuad);
		}
		trGroup.addChild(resultGroup);
	}

	public static Frame getFrame(Component component) {
		for (; component != null && !(component instanceof Frame); component = component
				.getParent())
			;
		return (Frame) component;
	}

	void b_open_actionPerformed(ActionEvent e) {
		FileFilter f1 = new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory())
					return true;
				return f.getName().toLowerCase().endsWith(".res");
			}

			public String getDescription() {
				return "Open solver result (.res)";
			}
		};

		JFileChooser fd = new JFileChooser(ConfDB.getProperty("Filepath"));
		fd.setSize(350, 200);
		fd.addChoosableFileFilter(f1);

		try {

			int choise = fd.showOpenDialog(this);

			if (choise == JFileChooser.APPROVE_OPTION
					&& fd.getSelectedFile() != null) {
				String infile = fd.getSelectedFile().getAbsolutePath();

				if (fd.getFileFilter().equals(f1)
						|| infile.toLowerCase().endsWith(".in")) {
					load(infile);
					view_zoom_all();
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

	void db_graph_preparation(int Y, String nd_el) {
		String info = null;
		String[][] arr = new String[time_step.getModel().getSize()][2];
		switch (Y) {
		case 0:// DISPLACEMENTS (I)
			info = "#The results are from node: " + nd_el
					+ "\n#X: time   Y: displacement in direction (I)";
			for (int i = 0; i < arr.length; i++) {
				arr[i][0] = time_step.getModel().getElementAt(i) + "";
				Hashtable ht = (Hashtable) db_displacements.get(arr[i][0]);
				Point3d p3d = (Point3d) ht.get(nd_el);
				arr[i][1] = Math.sqrt(p3d.x * p3d.x + p3d.y * p3d.y + p3d.z
						* p3d.z)
						+ "";
			}
			break;
		case 1:// DISPLACEMENTS (X)
			info = "#The results are from node: " + nd_el
					+ "\n#X: time   Y: displacement in direction (X)";
			for (int i = 0; i < arr.length; i++) {
				arr[i][0] = time_step.getModel().getElementAt(i) + "";
				Hashtable ht = (Hashtable) db_displacements.get(arr[i][0]);
				Point3d p3d = (Point3d) ht.get(nd_el);
				arr[i][1] = p3d.x + "";
			}
			break;
		case 2:// DISPLACEMENTS (Y)
			info = "#The results are from node: " + nd_el
					+ "\n#X: time   Y: displacement in direction (Y)";
			for (int i = 0; i < arr.length; i++) {
				arr[i][0] = time_step.getModel().getElementAt(i) + "";
				Hashtable ht = (Hashtable) db_displacements.get(arr[i][0]);
				Point3d p3d = (Point3d) ht.get(nd_el);
				arr[i][1] = p3d.y + "";
			}
			break;
		case 3:// DISPLACEMENTS (Z)
			info = "#The results are from node: " + nd_el
					+ "\n#X: time   Y: displacement in direction (Z)";
			for (int i = 0; i < arr.length; i++) {
				arr[i][0] = time_step.getModel().getElementAt(i) + "";
				Hashtable ht = (Hashtable) db_displacements.get(arr[i][0]);
				Point3d p3d = (Point3d) ht.get(nd_el);
				arr[i][1] = p3d.z + "";
			}
			break;
		case 4:// STRAINS (I)
			info = "#The results are from element: " + nd_el
					+ "\n#X: time   Y: strain in direction (I)";
			for (int i = 0; i < arr.length; i++) {
				arr[i][0] = time_step.getModel().getElementAt(i) + "";
				Hashtable ht = (Hashtable) db_strains.get(arr[i][0]);
				float[] ee = (float[]) ht.get(nd_el);
				try {
					arr[i][1] = (Math.sqrt(2) / 3 * Math.sqrt(Math.pow(ee[0]
							- ee[1], 2)
							+ Math.pow(ee[1] - ee[2], 2)
							+ Math.pow(ee[2] - ee[0], 2)
							+ 1.5
							* (ee[3] * ee[3] + ee[4] * ee[4] + ee[5] * ee[5])))
							+ "";
				} catch (Exception e1) {
					arr[i][1] = "";
				}
			}
			break;
		case 5:// STRAINS (X)
			info = "#The results are from element: " + nd_el
					+ "\n#X: time   Y: strain in direction (X)";
			for (int i = 0; i < arr.length; i++) {
				arr[i][0] = time_step.getModel().getElementAt(i) + "";
				Hashtable ht = (Hashtable) db_strains.get(arr[i][0]);
				float[] ee = (float[]) ht.get(nd_el);
				try {
					arr[i][1] = ee[0] + "";
				} catch (Exception e1) {
					arr[i][1] = "";
				}
			}
			break;
		case 6:// STRAINS (Y)
			info = "#The results are from element: " + nd_el
					+ "\n#X: time   Y: strain in direction (Y)";
			for (int i = 0; i < arr.length; i++) {
				arr[i][0] = time_step.getModel().getElementAt(i) + "";
				Hashtable ht = (Hashtable) db_strains.get(arr[i][0]);
				float[] ee = (float[]) ht.get(nd_el);
				try {
					arr[i][1] = ee[1] + "";
				} catch (Exception e1) {
					arr[i][1] = "";
				}
			}
			break;
		case 7:// STRAINS (Z)
			info = "#The results are from element: " + nd_el
					+ "\n#X: time   Y: strain in direction (Z)";
			for (int i = 0; i < arr.length; i++) {
				arr[i][0] = time_step.getModel().getElementAt(i) + "";
				Hashtable ht = (Hashtable) db_strains.get(arr[i][0]);
				float[] ee = (float[]) ht.get(nd_el);
				try {
					arr[i][1] = ee[2] + "";
				} catch (Exception e1) {
					arr[i][1] = "";
				}
			}
			break;
		case 8:// STRAINS (XY)
			info = "#The results are from element: " + nd_el
					+ "\n#X: time   Y: strain in direction (XY)";
			for (int i = 0; i < arr.length; i++) {
				arr[i][0] = time_step.getModel().getElementAt(i) + "";
				Hashtable ht = (Hashtable) db_strains.get(arr[i][0]);
				float[] ee = (float[]) ht.get(nd_el);
				try {
					arr[i][1] = ee[3] + "";
				} catch (Exception e1) {
					arr[i][1] = "";
				}
			}
			break;
		case 9:// STRAINS (YZ)
			info = "#The results are from element: " + nd_el
					+ "\n#X: time   Y: strain in direction (YZ)";
			for (int i = 0; i < arr.length; i++) {
				arr[i][0] = time_step.getModel().getElementAt(i) + "";
				Hashtable ht = (Hashtable) db_strains.get(arr[i][0]);
				float[] ee = (float[]) ht.get(nd_el);
				try {
					arr[i][1] = ee[4] + "";
				} catch (Exception e1) {
					arr[i][1] = "";
				}
			}
			break;
		case 10:// STRAINS (XZ)
			info = "#The results are from element: " + nd_el
					+ "\n#X: time   Y: strain in direction (XZ)";
			for (int i = 0; i < arr.length; i++) {
				arr[i][0] = time_step.getModel().getElementAt(i) + "";
				Hashtable ht = (Hashtable) db_strains.get(arr[i][0]);
				float[] ee = (float[]) ht.get(nd_el);
				try {
					arr[i][1] = ee[5] + "";
				} catch (Exception e1) {
					arr[i][1] = "";
				}
			}
			break;
		case 11:// STRESSES (I)
			info = "#The results are from element: " + nd_el
					+ "\n#X: time   Y: stress in direction (I)";
			for (int i = 0; i < arr.length; i++) {
				arr[i][0] = time_step.getModel().getElementAt(i) + "";
				Hashtable ht = (Hashtable) db_stresses.get(arr[i][0]);
				float[] ee = (float[]) ht.get(nd_el);
				try {
					arr[i][1] = (Math.sqrt(2) / 2 * Math.sqrt(Math.pow(ee[0]
							- ee[1], 2)
							+ Math.pow(ee[1] - ee[2], 2)
							+ Math.pow(ee[2] - ee[0], 2)
							+ 6
							* (ee[3] * ee[3] + ee[4] * ee[4] + ee[5] * ee[5])))
							+ "";
				} catch (Exception e1) {
					arr[i][1] = "";
				}
			}
			break;
		case 12:// STRESSES (X)
			info = "#The results are from element: " + nd_el
					+ "\n#X: time   Y: stress in direction (X)";
			for (int i = 0; i < arr.length; i++) {
				arr[i][0] = time_step.getModel().getElementAt(i) + "";
				Hashtable ht = (Hashtable) db_stresses.get(arr[i][0]);
				float[] ee = (float[]) ht.get(nd_el);
				try {
					arr[i][1] = ee[0] + "";
				} catch (Exception e1) {
					arr[i][1] = "";
				}
			}
			break;
		case 13:// STRESSES (Y)
			info = "#The results are from element: " + nd_el
					+ "\n#X: time   Y: stress in direction (Y)";
			for (int i = 0; i < arr.length; i++) {
				arr[i][0] = time_step.getModel().getElementAt(i) + "";
				Hashtable ht = (Hashtable) db_stresses.get(arr[i][0]);
				float[] ee = (float[]) ht.get(nd_el);
				try {
					arr[i][1] = ee[1] + "";
				} catch (Exception e1) {
					arr[i][1] = "";
				}
			}
			break;
		case 14:// STRESSES (Z)
			info = "#The results are from element: " + nd_el
					+ "\n#X: time   Y: stress in direction (Z)";
			for (int i = 0; i < arr.length; i++) {
				arr[i][0] = time_step.getModel().getElementAt(i) + "";
				Hashtable ht = (Hashtable) db_stresses.get(arr[i][0]);
				float[] ee = (float[]) ht.get(nd_el);
				try {
					arr[i][1] = ee[2] + "";
				} catch (Exception e1) {
					arr[i][1] = "";
				}
			}
			break;
		case 15:// STRESSES (XY)
			info = "#The results are from element: " + nd_el
					+ "\n#X: time   Y: stress in direction (XY)";
			for (int i = 0; i < arr.length; i++) {
				arr[i][0] = time_step.getModel().getElementAt(i) + "";
				Hashtable ht = (Hashtable) db_stresses.get(arr[i][0]);
				float[] ee = (float[]) ht.get(nd_el);
				try {
					arr[i][1] = ee[3] + "";
				} catch (Exception e1) {
					arr[i][1] = "";
				}
			}
			break;
		case 16:// STRESSES (YZ)
			info = "#The results are from element: " + nd_el
					+ "\n#X: time   Y: stress in direction (YZ)";
			for (int i = 0; i < arr.length; i++) {
				arr[i][0] = time_step.getModel().getElementAt(i) + "";
				Hashtable ht = (Hashtable) db_stresses.get(arr[i][0]);
				float[] ee = (float[]) ht.get(nd_el);
				try {
					arr[i][1] = ee[4] + "";
				} catch (Exception e1) {
					arr[i][1] = "";
				}
			}
			break;
		case 17:// STRESSES (XZ)
			info = "#The results are from element: " + nd_el
					+ "\n#X: time   Y: stress in direction (XZ)";
			for (int i = 0; i < arr.length; i++) {
				arr[i][0] = time_step.getModel().getElementAt(i) + "";
				Hashtable ht = (Hashtable) db_stresses.get(arr[i][0]);
				float[] ee = (float[]) ht.get(nd_el);
				try {
					arr[i][1] = ee[5] + "";
				} catch (Exception e1) {
					arr[i][1] = "";
				}
			}
			break;
		default:
			break;
		}
		db_graph_tmp = arr;
		db_graph_inf = info;
	}

	void b_save_actionPerformed(ActionEvent e) {
		FileFilter f1 = new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory())
					return true;
				return f.getName().toLowerCase().endsWith(".png");
			}

			public String getDescription() {
				return "Picture of result (.png)";
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
					if (!st.toLowerCase().endsWith(".png"))
						st += ".png";
					save_Image(st);
					System.out.println("Save image file: " + st);
					// Update configuration file with path
					ConfDB.setProperty("Filepath", fd.getSelectedFile()
							.getAbsolutePath());
					save_configuration();
				}
			}
		} catch (Exception e1) {
			error(e1);
		}
	}

	void b_zoomin_actionPerformed(ActionEvent e) {
		view_zoom_in();
	}

	void b_zoomout_actionPerformed(ActionEvent e) {
		view_zoom_out();
	}

	void b_viewtop_actionPerformed(ActionEvent e) {
		view_top();
	}

	void b_viewbottom_actionPerformed(ActionEvent e) {
		view_bottom();
	}

	void b_viewsw_actionPerformed(ActionEvent e) {
		view_sw();
	}

	void b_viewse_actionPerformed(ActionEvent e) {
		view_se();
	}

	void b_viewne_actionPerformed(ActionEvent e) {
		view_ne();
	}

	void b_viewnw_actionPerformed(ActionEvent e) {
		view_nw();
	}

	void b_viewleft_actionPerformed(ActionEvent e) {
		view_left();
	}

	void b_viewright_actionPerformed(ActionEvent e) {
		view_right();
	}

	void b_viewfront_actionPerformed(ActionEvent e) {
		view_front();
	}

	void b_viewback_actionPerformed(ActionEvent e) {
		view_back();
	}

	void b_saveall_actionPerformed(ActionEvent e) {
		/*
		 * FileFilter f1 = new FileFilter() { public boolean accept(File f) { if
		 * (f.isDirectory()) return true; return
		 * f.getName().toLowerCase().endsWith(".png"); }
		 * 
		 * public String getDescription() { return "Directory for pictures"; }
		 * };
		 */

		try {

			JFileChooser fd = new JFileChooser(ConfDB.getProperty("Filepath"));
			fd.setSize(350, 200);
			// fd.addChoosableFileFilter(f1);
			fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			int choise = fd.showSaveDialog(this);

			if (choise == JFileChooser.APPROVE_OPTION
					&& fd.getSelectedFile() != null) {
				String file = fd.getSelectedFile().getAbsolutePath();

				// if (fd.getFileFilter().equals(f1)) {
				if (file.toLowerCase().endsWith(".png"))
					file = file.substring(0, file.lastIndexOf("."));
				File f = new File(file);
				if (f.exists()) {
					error("File '" + file + "' exists!");
					return;
				}
				f.mkdir();
				for (int i = 0; i < time_step.getModel().getSize(); i++) {
					time_step.setSelectedIndex(i);
					String resultFilename = file + File.separator
							+ time_step.getSelectedValue() + ".png";
					headerMessage("Save result to image: " + resultFilename);
					save_Image(resultFilename);
					System.out.println("Save image file: " + resultFilename);
				}
				// Update configuration file with path
				ConfDB.setProperty("Filepath", fd.getSelectedFile()
						.getAbsolutePath());
				save_configuration();
				headerMessage(null);
				// }
			}
		} catch (Exception e1) {
			error(e1);
		}
	}

	void tf_max_keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			try {
				tfmax = Float.parseFloat(tf_max.getText());

				switch (cb_result.getSelectedIndex()) {
				case 0:
					maxdi = tfmax;
					break;
				case 1:
					maxdx = tfmax;
					break;
				case 2:
					maxdy = tfmax;
					break;
				case 3:
					maxdz = tfmax;
					break;
				case 4:
					maxei = tfmax;
					break;
				case 5:
					maxex = tfmax;
					break;
				case 6:
					maxey = tfmax;
					break;
				case 7:
					maxez = tfmax;
					break;
				case 8:
					maxexy = tfmax;
					break;
				case 9:
					maxeyz = tfmax;
					break;
				case 10:
					maxexz = tfmax;
					break;
				case 11:
					maxsi = tfmax;
					break;
				case 12:
					maxsx = tfmax;
					break;
				case 13:
					maxsy = tfmax;
					break;
				case 14:
					maxsz = tfmax;
					break;
				case 15:
					maxsxy = tfmax;
					break;
				case 16:
					maxsyz = tfmax;
					break;
				case 17:
					maxsxz = tfmax;
					break;

				}

				draw();
			} catch (Exception e1) {
				error(e1);
			}
		} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			tf_max.setText(tfmax + "");
	}

	void tf_min_keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			try {
				tfmin = Float.parseFloat(tf_min.getText());

				switch (cb_result.getSelectedIndex()) {
				case 0:
					mindi = tfmin;
					break;
				case 1:
					mindx = tfmin;
					break;
				case 2:
					mindy = tfmin;
					break;
				case 3:
					mindz = tfmin;
					break;
				case 4:
					minei = tfmin;
					break;
				case 5:
					minex = tfmin;
					break;
				case 6:
					miney = tfmin;
					break;
				case 7:
					minez = tfmin;
					break;
				case 8:
					minexy = tfmin;
					break;
				case 9:
					mineyz = tfmin;
					break;
				case 10:
					minexz = tfmin;
					break;
				case 11:
					minsi = tfmin;
					break;
				case 12:
					minsx = tfmin;
					break;
				case 13:
					minsy = tfmin;
					break;
				case 14:
					minsz = tfmin;
					break;
				case 15:
					minsxy = tfmin;
					break;
				case 16:
					minsyz = tfmin;
					break;
				case 17:
					minsxz = tfmin;
					break;

				}
				draw();
			} catch (Exception e1) {
				error(e1);
			}
		} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			tf_min.setText(tfmin + "");
	}

	public void exit() {
		System.out.println("PostProcessor finalize.");
	}

	private void load_configuration() {
		FileInputStream fin = null;
		try {
			ConfDB.clear();
			fin = new FileInputStream("Post.conf");
			ConfDB.load(fin);
			fin.close();
		} catch (IOException e) {
			error(e);
		}
	}

	private void save_configuration() {
		FileOutputStream fout;
		try {
			fout = new FileOutputStream("Post.conf");
			ConfDB.store(fout, "Configuration file for Impact Postprocessor");
			fout.close();
		} catch (IOException e) {
			error(e);
		}
	}

	public void headerMessage(String m) {
		Frame f = getFrame(this);
		if (m == null)
			m = ImpactGUI.ver;
		String s = f.getTitle();
		String s1 = s.substring(0, s.lastIndexOf("]") + 1);
		f.setTitle(s1 + " " + m);

	}

	public void header(String m) {
		Frame f = getFrame(this);
		String s = f.getTitle();

		String s1 = s.substring(0, s.indexOf("[POST:") + 6);
		String s2 = s.substring(s.indexOf("[GRPH:"), s.length());
		f.setTitle(s1 + m + "] " + s2);
	}

	public void save_Image(String file) {
		byte[] pngbytes;
		PngEncoderB png;
		try {
			if (file.toLowerCase().indexOf(".png") == -1)
				file += ".png";
			FileOutputStream out = new FileOutputStream(file);
			try {
				J3D.waitForOffScreenRendering();
			} catch (Exception e) {
			}
			GraphicsContext3D ctx = J3D.getGraphicsContext3D();
			int w = J3D.getWidth();
			int h = J3D.getHeight();
			BufferedImage image = new BufferedImage(w, h,
					BufferedImage.TYPE_INT_RGB);
			ImageComponent2D image2d = new ImageComponent2D(
					ImageComponent.FORMAT_RGB, image);
			javax.media.j3d.Raster ras = new javax.media.j3d.Raster(
					new Point3f(-1.0f, -1.0f, -1.0f),
					javax.media.j3d.Raster.RASTER_COLOR, 0, 0, w, h, image2d,
					null);
			ctx.flush(true);
			ctx.readRaster(ras);
			png = new PngEncoderB(ras.getImage().getImage(), false, 0, 5); // Encode
																			// at
																			// level
																			// 5
																			// compression
			pngbytes = png.pngEncode();
			out.write(pngbytes);
			out.flush();
			out.close();
		} catch (FileNotFoundException fnfe) {
			System.err.println("File not found: " + file);
		} catch (IOException ioe) {
			System.err.println("Could not write file: " + file);
		}
	}

	public void view_top() {
		if (time_step.getModel().getSize() == 0)
			return;
		Transform3D tr3D = new Transform3D();
		Transform3D tr3D_new = new Transform3D();
		trGroup.getTransform(tr3D);
		tr3D_new.setScale(tr3D.getScale());
		trGroup.setTransform(tr3D_new);
	}

	public void view_bottom() {
		if (time_step.getModel().getSize() == 0)
			return;
		Transform3D tr3D = new Transform3D();
		Transform3D tr3D_new = new Transform3D();
		trGroup.getTransform(tr3D);
		tr3D_new.rotY(Math.PI);
		tr3D_new.setScale(tr3D.getScale());
		trGroup.setTransform(tr3D_new);
	}

	public void view_left() {
		if (time_step.getModel().getSize() == 0)
			return;
		Transform3D tr3D = new Transform3D();
		Transform3D tr3D_new = new Transform3D();
		trGroup.getTransform(tr3D);
		tr3D_new.rotY(Math.PI / 2);
		tr3D_new.setScale(tr3D.getScale());
		trGroup.setTransform(tr3D_new);
	}

	public void view_right() {
		if (time_step.getModel().getSize() == 0)
			return;
		Transform3D tr3D = new Transform3D();
		Transform3D tr3D_new = new Transform3D();
		trGroup.getTransform(tr3D);
		tr3D_new.rotY(-Math.PI / 2);
		tr3D_new.setScale(tr3D.getScale());
		trGroup.setTransform(tr3D_new);
	}

	public void view_front() {
		if (time_step.getModel().getSize() == 0)
			return;
		Transform3D tr3D = new Transform3D();
		Transform3D tr3D_new = new Transform3D();
		trGroup.getTransform(tr3D);
		tr3D_new.rotX(Math.PI / 2);
		tr3D_new.setScale(tr3D.getScale());
		trGroup.setTransform(tr3D_new);
	}

	public void view_back() {
		if (time_step.getModel().getSize() == 0)
			return;
		Transform3D tr3D = new Transform3D();
		Transform3D tr3D_new = new Transform3D();
		trGroup.getTransform(tr3D);
		tr3D_new.rotX(-Math.PI / 2);
		tr3D_new.setScale(tr3D.getScale());
		trGroup.setTransform(tr3D_new);
	}

	public void view_sw() {
		if (time_step.getModel().getSize() == 0)
			return;
		Transform3D tr3D = new Transform3D();
		Transform3D tr3D_new = new Transform3D();
		trGroup.getTransform(tr3D);
		Matrix3d m3dx = new Matrix3d();
		Matrix3d m3dy = new Matrix3d();
		m3dx.rotX(Math.PI / 4);
		m3dy.rotY(-Math.PI / 4);
		m3dx.mul(m3dy);
		tr3D_new.setRotation(m3dx);
		tr3D_new.setScale(tr3D.getScale());
		trGroup.setTransform(tr3D_new);
	}

	public void view_se() {
		if (time_step.getModel().getSize() == 0)
			return;
		Transform3D tr3D = new Transform3D();
		Transform3D tr3D_new = new Transform3D();
		trGroup.getTransform(tr3D);
		Matrix3d m3dx = new Matrix3d();
		Matrix3d m3dy = new Matrix3d();
		m3dx.rotX(-Math.PI / 4);
		m3dy.rotY(Math.PI / 4);
		m3dx.mul(m3dy);
		tr3D_new.setRotation(m3dx);
		tr3D_new.setScale(tr3D.getScale());
		trGroup.setTransform(tr3D_new);
	}

	public void view_ne() {
		if (time_step.getModel().getSize() == 0)
			return;
		Transform3D tr3D = new Transform3D();
		Transform3D tr3D_new = new Transform3D();
		trGroup.getTransform(tr3D);
		Matrix3d m3dx = new Matrix3d();
		Matrix3d m3dy = new Matrix3d();
		m3dx.rotX(-Math.PI / 4);
		m3dy.rotY(-Math.PI / 4);
		m3dx.mul(m3dy);
		tr3D_new.setRotation(m3dx);
		tr3D_new.setScale(tr3D.getScale());
		trGroup.setTransform(tr3D_new);
	}

	public void view_nw() {
		if (time_step.getModel().getSize() == 0)
			return;
		Transform3D tr3D = new Transform3D();
		Transform3D tr3D_new = new Transform3D();
		trGroup.getTransform(tr3D);
		Matrix3d m3dx = new Matrix3d();
		Matrix3d m3dy = new Matrix3d();
		m3dx.rotX(Math.PI / 4);
		m3dy.rotY(Math.PI / 4);
		m3dx.mul(m3dy);
		tr3D_new.setRotation(m3dx);
		tr3D_new.setScale(tr3D.getScale());
		trGroup.setTransform(tr3D_new);
	}

	public void view_zoom_all() {
		if (time_step.getModel().getSize() == 0)
			return;
		Transform3D tr3D_new = new Transform3D();
		trGroup.getTransform(tr3D_new);
		tr3D_new.getScale();
		// BoundingSphere bs = new BoundingSphere(trGroup.getBounds());
		// tr3D.setScale(1/bs.getRadius());
		tr3D_new.setScale(1 / Math.max(maxx - minx,
				Math.max(maxy - miny, maxz - minz)));
		trGroup.setTransform(tr3D_new);
	}

	public void view_zoom_in() {
		if (time_step.getModel().getSize() == 0)
			return;
		Transform3D tr3D_new = new Transform3D();
		trGroup.getTransform(tr3D_new);
		tr3D_new.setScale(tr3D_new.getScale() * 1.2);
		trGroup.setTransform(tr3D_new);
	}

	public void view_zoom_out() {
		if (time_step.getModel().getSize() == 0)
			return;
		Transform3D tr3D_new = new Transform3D();
		trGroup.getTransform(tr3D_new);
		tr3D_new.setScale(tr3D_new.getScale() * 0.8);
		trGroup.setTransform(tr3D_new);
	}

}

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

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.tree.DefaultTreeModel;

/**
 * @author pc58410
 * 
 */
public interface Canvas3D {

	public static final byte GRAPHICSMODE_WIREFRAME = 0;
	public static final byte GRAPHICSMODE_SURFACE = 1;
	public static final byte GRAPHICSMODE_SOLID = 2;
	public static final byte GRAPHICSMODE_NADA = -1;
	public static final byte RENDERMODE_HIDE = 0;
	public static final byte RENDERMODE_SHADE = 1;
	public static final byte RENDERMODE_RENDER = 2;
	public static final Color SELECTCOLOR = Color.red;
	// public static float GRADIENTCONTRAST=0.2f;
	// public static int GRADIENTCOLOR=50;
	public static final byte GRIDPLANE_XY = 0;
	public static final byte GRIDPLANE_YZ = 1;
	public static final byte GRIDPLANE_XZ = 2;
	public static final int MESH_Dummy_2 = 0;
	public static final int MESH_Rod_2 = 1;
	public static final int MESH_Beam_2 = 2;
	public static final int MESH_Beam_Spring_2 = 3;
	public static final int MESH_Contact_Line = 4;
	public static final int MESH_Contact_Triangle = 5;
	public static final int MESH_Dummy_4 = 6;
	public static final int MESH_Shell_C0_3 = 7;
	public static final int MESH_Shell_BT_4 = 8;
	public static final int MESH_Solid_Iso_6 = 9;
	public static final int MESH_Shell_Opt_3_or_4 = 10;
	public static final int MESH_Solid_Iso_4 = 11;

	// Codes for handling of geometry
	public static final int NONE = 0;
	public static final int CURVE = 1;
	public static final int SURFACE = 2;
	public static final int VOLUME = 3;
	public static final double PICKDISTANCE = 50; // Picking sensing distance
													// squared (d*d)
	public static final double MINMESHANGLE = 2; // Minimum value of B for
													// acceptable mesh

	public void view_top();

	public void view_sw();

	public void view_se();

	public void view_scale(float f);

	public void view_right();

	public void view_reset();

	public void view_repaint();

	public void view_nw();

	public void view_ne();

	public void view_left();

	public void view_front();

	public void view_bottom();

	public void view_back();

	public void view_all();

	public void tree_reset();

	public void transform3D(Matrix3D m);

	public boolean showhide();

	public void setSize(Dimension dimension);

	public void setCenterOfRotation();

	public void setBackground(Color white);

	public void save_Image(String st);

	public void repaint();

	public void removeSelectedObjects3D();

	public void clearSelectOnAllObjects3D();

	public void remove_all();

	public void rebuild();

	public void projectObjects();

	public void intersectObjects();

	public void breakObjects();

	public _Object[] getSelectedObjects3D();

	public _Object getSelectedObject3D();

	public _Object[] getAllObjects3D();

	public _Node[] getAllNodes3D();

	public _Object[] getAllElements3D();

	public _Geometry[] getAllGeometry3D();

	public _Group[] getAllGroups3D();

	public void duplicate();

	public void addBorderObjects();

	public int add3D(Object obj);

	public boolean getSHOW_ID_NODE();

	public void setSHOW_ID_NODE(boolean show_id_node);

	public Color getBGCOLOR();

	public void setBGCOLOR(Color bgcolor);

	public boolean getDRAFTMODE();

	public void setDRAFTMODE(boolean draftmode);

	public byte getGRAPHICSMODE();

	public void setGRAPHICSMODE(byte graphicsmode);

	public Color getGRIDCOLOR();

	public void setGRIDCOLOR(Color gridcolor);

	public float getGRIDLEVEL();

	public void setGRIDLEVEL(float gridlevel);

	public boolean getGRIDMODE();

	public void setGRIDMODE(boolean gridmode);

	public byte getGRIDPLANE();

	public void setGRIDPLANE(byte gridplane);

	public float getGRIDSIZE();

	public void setGRIDSIZE(float gridsize);

	public float[] getLIMITS();

	public void setLIMITS(float[] limits);

	public float getNODE_MERGE_TOLERANCE();

	public void setNODE_MERGE_TOLERANCE(float node_merge_tolerance);

	public int getNODESIZE();

	public void setNODESIZE(int nodesize);

	public int getPOINTSIZE();

	public void setPOINTSIZE(int pointsize);

	public byte getRENDERMODE();

	public void setRENDERMODE(byte rendermode);

	public boolean getSHOW_ID_CONSTRAINTS();

	public void setSHOW_ID_CONSTRAINTS(boolean show_id_constraints);

	public boolean getSHOW_ID_ELEMENT();

	public void setSHOW_ID_ELEMENT(boolean show_id_element);

	public boolean getSHOW_ID_LOADS();

	public void setSHOW_ID_LOADS(boolean show_id_loads);

	public boolean getSHOW_ID_MATERIALS();

	public void setSHOW_ID_MATERIALS(boolean show_id_materials);

	public boolean getSHOW_ID_TRACKERS();

	public void setSHOW_ID_TRACKERS(boolean show_id_trackers);

	public Color getSTLCOLOR();

	public void setSTLCOLOR(Color stlcolor);

	public Matrix3D getVMatrix3D();

	public void setVMatrix3D(Matrix3D matrix3D);

	public DefaultTreeModel getTree3d();

	public float getGeometricTolerance();

	public void setGeometricTolerance(float geometricTolerance);

	public void setVisible(boolean v);

	public void replaceAllInstancesOf(_Object o, _Object replacement);

}

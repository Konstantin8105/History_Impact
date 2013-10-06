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

import j3d.utilities.Comparer;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import javax.swing.tree.*;
import java.io.*;
import gui.*;

import javax.swing.*;

import com.stevesoft.pat.Regex;

import java.awt.event.*;

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy
 */

public class _Group extends _Object implements Serializable {
	public Vector objects;
	protected JButton b_1, b_2, b_3;
	protected JTextField groupname;
	protected String name_of_group = new String("Groupname");
	protected boolean topgroup = false;
	private static Regex r_index = new Regex("^ *([0-9]+) *");
	private static Regex r_groupname = new Regex(
			"(?i)NAME *= *([a-zA-Z\\/.0-9_]+)");
	private static Regex r_level = new Regex("(?i)LEVEL *= *(TOP|SUB) *");
	private static Regex r_gnodes = new Regex(
			"(?i)GNODES *= *\\[([0-9, \\-]*)\\]");
	private static Regex r_gelements = new Regex(
			"(?i)GELEMENTS *= *\\[([0-9, \\-]*)\\]");
	private static Regex r_geometry = new Regex(
			"(?i)GEOMETRY *= *\\[([0-9, \\-]*)\\]");
	private static Regex r_groups = new Regex(
			"(?i)GROUPS *= *\\[([0-9, \\-]*)\\]");
	private static Regex r_indexer = new Regex("\\G,?([0-9]+)(?:-([0-9]+))?,?");

	public _Group(boolean add, boolean topgroup) {
		this.add = add;
		this.topgroup = topgroup;
		objects = new Vector();
	}

	public String writeObject() {
		Vector v;
		StringBuffer st_model = new StringBuffer();
		_Node[] n;
		_Object[] e;

		// If nothing to print..
		if (objects.size() == 0)
			return "";

		st_model.append("\n" + Id + " NAME = " + name_of_group + " LEVEL = "
				+ (topgroup ? "TOP" : "SUB") + " ");

		// Generate Nodes
		n = get_Nodes();

		v = new Vector();
		for (int j = 0; j < n.length; j++)
			v.add(n[j]);

		// Sort vector
		Collections.sort(v, new Comparer());

		generateSpanText(st_model, "GNODES = [", v);

		// Generate Elements
		e = get_Elements();

		v = new Vector();
		for (int j = 0; j < e.length; j++)
			v.add(e[j]);

		// Sort vector
		Collections.sort(v, new Comparer());

		// Generate output
		generateSpanText(st_model, "GELEMENTS = [", v);

		// Generate Geometry
		e = get_Geometry();

		v = new Vector();
		for (int j = 0; j < e.length; j++)
			v.add(e[j]);

		// Sort vector
		Collections.sort(v, new Comparer());

		// Generate output
		generateSpanText(st_model, "GEOMETRY = [", v);

		// Generate Groups
		e = get_Groups();

		v = new Vector();
		for (int j = 0; j < e.length; j++)
			v.add(e[j]);

		// Sort vector
		Collections.sort(v, new Comparer());

		// Generate output
		generateSpanText(st_model, "GROUPS = [", v);

		return st_model.toString();
	}

	public void readObject(boolean firstpass, String st, Hashtable NDB,
			Hashtable EDB, Hashtable GDB, Hashtable GRDB) {
		int start, end;

		if (r_index.search(st))
			Id = r_index.stringMatched(1);
		if (r_groupname.search(st))
			name_of_group = r_groupname.stringMatched(1);
		if (r_level.search(st))
			topgroup = r_level.stringMatched(1).equals("TOP");

		if (firstpass)
			return;

		if (r_gnodes.search(st))
			while (r_indexer.search(r_gnodes.stringMatched(1))) {
				start = Integer.parseInt(r_indexer.stringMatched(1));
				end = (r_indexer.stringMatched(2) == null ? start : Integer
						.parseInt(r_indexer.stringMatched(2)));

				for (int i = start; i <= end; i++)
					addToGroup(NDB.get("" + i));
			}
		if (r_gelements.search(st))
			while (r_indexer.search(r_gelements.stringMatched(1))) {
				start = Integer.parseInt(r_indexer.stringMatched(1));
				end = (r_indexer.stringMatched(2) == null ? start : Integer
						.parseInt(r_indexer.stringMatched(2)));

				for (int i = start; i <= end; i++)
					addToGroup(EDB.get("" + i));
			}
		if (r_geometry.search(st))
			while (r_indexer.search(r_geometry.stringMatched(1))) {
				start = Integer.parseInt(r_indexer.stringMatched(1));
				end = (r_indexer.stringMatched(2) == null ? start : Integer
						.parseInt(r_indexer.stringMatched(2)));

				for (int i = start; i <= end; i++)
					addToGroup(GDB.get("" + i));
			}
		if (r_groups.search(st))
			while (r_indexer.search(r_groups.stringMatched(1))) {
				start = Integer.parseInt(r_indexer.stringMatched(1));
				end = (r_indexer.stringMatched(2) == null ? start : Integer
						.parseInt(r_indexer.stringMatched(2)));

				for (int i = start; i <= end; i++)
					addToGroup(GRDB.get("" + i));
			}

	}

	private void generateSpanText(StringBuffer st, String st_type, Vector v) {
		int start = 0;
		int end = -1;
		int val;

		for (int i = 0; i < v.size(); i++) {
			val = Integer.parseInt(((_Object) v.elementAt(i)).get_Id());

			if (i == 0) {
				st.append(st_type);
				start = val;
				end = val;
			}

			if (val > end + 1) {
				st.append(start + (end == start ? "" : "-" + end)
						+ (i == v.size() - 1 ? "" : ","));
				start = val;
				end = val;
			}

			if (val == end + 1)
				end++;
		}

		if (start == end)
			st.append("] ");

		if (end > start)
			st.append(start + "-" + end + "] ");

	}

	public boolean isTopgroup() {
		return topgroup;
	}

	public void reset(boolean do_mesh) {
	};

	public void mesh(int type, float size) {
	}

	public Object[] get_Array(Canvas3D j3d) {
		return new Object[0];
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean sel) {
		selected = sel;

		// A group selects all its elements
		for (int i = 0; i < objects.size(); i++)
			((_Object) objects.elementAt(i)).setSelected(sel);
	}

	public String toString() {
		return name_of_group;
	}

	public MutableTreeNode get_TreeNode() {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(this);

		for (int i = 0; i < objects.size(); i++)
			node.add(((_Object) objects.elementAt(i)).get_TreeNode());

		return node;
	}

	public void setName(String name) {
		this.name_of_group = name;
	}

	public void addSelectedObjects(Vector v) {
		// if (selected) v.add(this);
	}

	public void deselectRequiredObjects() {

	}

	public void transform3D(Matrix3D t) {
	}

	public void error(Object st) {
		JOptionPane.showMessageDialog(null, "Error: " + st, "Error!",
				JOptionPane.ERROR_MESSAGE);
		System.out.println("Error: " + st);
		if (st instanceof Exception)
			((Exception) st).printStackTrace();
	}

	public JPanel getEditPanel(Canvas3D j3d, PreProcessor pp) {
		this.J3D = j3d;
		this.PreP = pp;
		JPanel p = new JPanel(new GridLayout(5, 1));
		JPanel p2 = new JPanel();
		b_1 = new JButton("INCLUDE");
		b_2 = new JButton("REMOVE");
		b_3 = new JButton(add == true ? "Add" : "Update");
		groupname = new JTextField(name_of_group);
		JLabel lb1 = new JLabel("Groupname");
		lb1.setForeground(Color.blue);
		JLabel lb2 = new JLabel("Items In Group");
		lb1.setForeground(Color.black);

		b_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton1_actionPerformed(e);
			}
		});
		b_1.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					jButton1_actionPerformed(null);
				}
			}
		});

		b_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton2_actionPerformed(e);
			}
		});
		b_2.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					jButton2_actionPerformed(null);
				}
			}
		});

		b_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton3_actionPerformed(e);
			}
		});
		b_3.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				checkDefaultKey(e);
			}

			public void keyReleased(KeyEvent e) {
				int c = e.getKeyCode();

				if (c == KeyEvent.VK_ENTER) {
					jButton3_actionPerformed(null);
				}
			}
		});

		p.add(lb1);
		p.add(groupname);
		p.add(lb2);
		p2.add(b_1);
		p2.add(b_2);
		p.add(p2);
		p.add(b_3);

		p.validate();
		return p;
	}

	void jButton1_actionPerformed(ActionEvent e) {
		try {
			Object[] obj = J3D.getSelectedObjects3D();

			addToGroup(obj);

			J3D.tree_reset();
			J3D.view_reset();
			J3D.view_all();

			b_2.requestFocus();
		} catch (Exception e1) {
			error(e1);
		}
	}

	public void addToGroup(Object[] obj) {

		for (int i = 0; i < obj.length; i++)
			if (obj[i] != this && !objects.contains(obj[i]))
				objects.add(obj[i]);

	}

	public void addToGroup(Object obj) {

		if (obj != this && !objects.contains(obj))
			objects.add(obj);

	}

	public void removeFromGroup(Object[] obj) {

		for (int i = 0; i < obj.length; i++)
			objects.remove(obj[i]);

	}

	public void removeFromGroup(Object obj) {

		objects.remove(obj);

	}

	void jButton2_actionPerformed(ActionEvent e) {
		try {
			Object[] obj = J3D.getSelectedObjects3D();

			removeFromGroup(obj);

			J3D.tree_reset();
			J3D.view_reset();
			J3D.view_all();

			b_1.requestFocus();
		} catch (Exception e1) {
			error(e1);
		}
	}

	public void requestFocus() {

		groupname.requestFocus();
		groupname.selectAll();

	}

	void jButton3_actionPerformed(ActionEvent e) {
		try {
			_Group tmp = this;

			name_of_group = groupname.getText();

			if (add == true) {
				try {
					tmp = (_Group) this.clone();
					J3D.add3D(tmp);
				} catch (CloneNotSupportedException e1) {
					e1.printStackTrace();
				}
				tmp.add = false;
			} else
				tmp.reset(true);

			J3D.tree_reset();
			J3D.view_reset();
		} catch (Exception e1) {
			error(e1);
		}
	}

	public void requestAction() {
		if (add == true)
			if (b_1.hasFocus())
				jButton1_actionPerformed(null);
			else if (b_2.hasFocus())
				jButton2_actionPerformed(null);
	}

	public _Node[] get_Nodes() {
		Vector v = new Vector();
		_Node[] n;

		for (int i = 0; i < objects.size(); i++)
			if (objects.elementAt(i) instanceof _Node)
				v.add(objects.elementAt(i));

		n = new _Node[v.size()];

		for (int i = 0; i < v.size(); i++)
			n[i] = (_Node) v.elementAt(i);

		return n;
	}

	public _Object[] get_Elements() {
		Vector v = new Vector();
		_Object[] n;

		for (int i = 0; i < objects.size(); i++)
			if (objects.elementAt(i) instanceof _Element)
				v.add(objects.elementAt(i));

		n = new _Object[v.size()];

		for (int i = 0; i < v.size(); i++)
			n[i] = (_Object) v.elementAt(i);

		return n;
	}

	public _Object[] get_Geometry() {
		Vector v = new Vector();
		_Object[] n;

		for (int i = 0; i < objects.size(); i++)
			if (objects.elementAt(i) instanceof _Geometry)
				v.add(objects.elementAt(i));

		n = new _Object[v.size()];

		for (int i = 0; i < v.size(); i++)
			n[i] = (_Object) v.elementAt(i);

		return n;
	}

	public _Object[] get_Groups() {
		Vector v = new Vector();
		_Object[] n;

		for (int i = 0; i < objects.size(); i++)
			if (objects.elementAt(i) instanceof _Group)
				v.add(objects.elementAt(i));

		n = new _Object[v.size()];

		for (int i = 0; i < v.size(); i++)
			n[i] = (_Object) v.elementAt(i);

		return n;
	}

	public _Object duplicate(Canvas3D out, boolean add) {
		_Group o = null;
		_Object op = null;

		try {
			o = (_Group) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < objects.size(); i++) {
			op = ((_Object) objects.elementAt(i)).duplicate(out, add);
			o.objects.add(op);
		}

		if (add)
			out.add3D(o);

		return o;
	}

	public Vector3D getCenter() {
		Vector3D s = new Vector3D();

		return s;
	}

	public boolean isPickPoint(int x, int y, boolean shw, boolean ogl) {
		boolean check = false;
		return check;

	}

	public boolean isPickPoint(Rectangle2D r, boolean shw, boolean ogl) {
		boolean check = false;
		return check;
	}

	public void replaceObjectWith(_Object o, _Object replacement) {

		for (int i = 0; i < objects.size(); i++)
			if (objects.elementAt(i) == o)
				objects.setElementAt(replacement, i);

	}

	public String getName() {
		return name_of_group;
	}

}
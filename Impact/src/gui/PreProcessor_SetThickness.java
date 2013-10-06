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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import java.awt.*;
import j3d.*;

import java.awt.event.*;

public class PreProcessor_SetThickness extends JPanel {
	Canvas3D J3D;
	PreProcessor PreP;
	JTextField x1 = new JTextField();
	JPanel jPanel2 = new JPanel();
	JButton jButton1 = new JButton();
	private JLabel jLabel1 = new JLabel();

	public PreProcessor_SetThickness(Canvas3D j3d, PreProcessor p) {
		try {
			J3D = j3d;
			PreP = p;
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		this.setLayout(new BorderLayout());
		jButton1.setText("Set");
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton1_actionPerformed(e);
			}
		});
		jLabel1.setForeground(Color.blue);
		jLabel1.setText("Thickness or Diameter");
		x1.setText("1.0");
		this.add(x1, BorderLayout.CENTER);
		this.add(jPanel2, BorderLayout.SOUTH);
		jPanel2.add(jButton1, null);
		this.add(jLabel1, BorderLayout.NORTH);
	}

	void jButton1_actionPerformed(ActionEvent e) {
		float t = 1.0f;
		try {
			t = Float.parseFloat(x1.getText());
		} catch (Exception e1) {
			error(e1);
		}

		TreePath[] tp = PreP.Tree.getSelectionPaths();
		if (tp == null)
			return;
		for (int i = 0; i < tp.length; i++) {
			DefaultMutableTreeNode tn = (DefaultMutableTreeNode) tp[i]
					.getLastPathComponent();
			Object obj = tn.getUserObject();
			if (obj instanceof _Element2) {
				((_Element2) obj).diameter = t;
			} else if (obj instanceof _Element3) {
				((_Element3) obj).thickness = t;
			} else if (obj instanceof _Element4) {
				((_Element4) obj).thickness = t;
			}
		}
		J3D.tree_reset();
		J3D.view_reset();
	}

	public void error(Object st) {
		JOptionPane.showMessageDialog(this, "Error: " + st, "Error!",
				JOptionPane.ERROR_MESSAGE);
		if (st instanceof Exception)
			((Exception) st).printStackTrace();
		else
			System.out.println("Error: " + st);
	}

}

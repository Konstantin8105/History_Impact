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

/**
 * 
 * @author: Yuriy Mikhaylovskiy
 */
public class PostProcessor_Clip_Panel extends JPanel {
	public JCheckBox cb_clip;
	public JCheckBox cb_x_reverse;
	public JCheckBox cb_y_reverse;
	public JCheckBox cb_z_reverse;
	private JLabel jLabel1;
	private JPanel jPanel3;
	private JPanel jPanel4;
	private JPanel jPanel5;
	private JPanel jPanel6;
	public JPanel p_clip;
	public JSlider sl_x;
	public JSlider sl_y;
	public JSlider sl_z;

	public PostProcessor_Clip_Panel() {
		initComponents();
	}

	private void initComponents() {

		cb_clip = new JCheckBox();
		p_clip = new JPanel();
		jPanel3 = new JPanel();
		jLabel1 = new JLabel();
		jPanel4 = new JPanel();
		jPanel5 = new JPanel();
		cb_x_reverse = new JCheckBox("X");
		cb_y_reverse = new JCheckBox("Y");
		cb_z_reverse = new JCheckBox("Z");
		jPanel6 = new JPanel();
		sl_x = new JSlider();
		sl_y = new JSlider();
		sl_z = new JSlider();

		setLayout(new java.awt.BorderLayout());

		cb_clip.setText("Clip planes in direction");
		add(cb_clip, java.awt.BorderLayout.PAGE_START);

		p_clip.setLayout(new java.awt.BorderLayout());

		jPanel3.setLayout(new java.awt.BorderLayout());

		jLabel1.setText(" Reverse direction");
		jPanel3.add(jLabel1, java.awt.BorderLayout.PAGE_START);

		jPanel4.setLayout(new java.awt.BorderLayout());

		jPanel5.setLayout(new java.awt.GridLayout(1, 3));
		jPanel5.add(cb_x_reverse);
		jPanel5.add(cb_y_reverse);
		jPanel5.add(cb_z_reverse);

		jPanel4.add(jPanel5, java.awt.BorderLayout.PAGE_START);

		jPanel6.setLayout(new java.awt.GridLayout(1, 3));

		sl_x.setOrientation(JSlider.VERTICAL);
		sl_x.setPreferredSize(new java.awt.Dimension(10, 150));
		jPanel6.add(sl_x);

		sl_y.setOrientation(JSlider.VERTICAL);
		sl_y.setPreferredSize(new java.awt.Dimension(10, 150));
		jPanel6.add(sl_y);

		sl_z.setOrientation(JSlider.VERTICAL);
		sl_z.setPreferredSize(new java.awt.Dimension(10, 150));
		jPanel6.add(sl_z);

		jPanel4.add(jPanel6, java.awt.BorderLayout.CENTER);

		jPanel3.add(jPanel4, java.awt.BorderLayout.CENTER);

		p_clip.add(jPanel3, java.awt.BorderLayout.CENTER);

		add(p_clip, java.awt.BorderLayout.CENTER);
	}

}

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

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy
 */

public class Processor_Find extends JPanel {
	JTextArea text;
	JTextField tFind = new JTextField(20);
	JButton jButton1 = new JButton();
	JTextField tReplace = new JTextField(20);
	JButton jButton2 = new JButton();
	JCheckBox cb_all = new JCheckBox("Replace All");
	JPanel panel1 = new JPanel(new BorderLayout());
	JPanel panel2 = new JPanel(new BorderLayout());
	JPanel panel3 = new JPanel(new GridLayout(1, 3));

	public Processor_Find(JTextArea t) {
		try {
			text = t;
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		this.setLayout(new GridLayout(3, 1));
		jButton1.setText("Find");
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton1_actionPerformed(e);
			}
		});
		jButton2.setText("Replace");
		jButton2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButton2_actionPerformed(e);
			}
		});
		jButton1.setPreferredSize(new Dimension(100,
				jButton1.getMinimumSize().height));
		jButton2.setPreferredSize(new Dimension(100,
				jButton2.getMinimumSize().height));
		JLabel l1 = new JLabel("Text to find");
		JLabel l2 = new JLabel("Replace");
		l1.setPreferredSize(new Dimension(90, l1.getMinimumSize().height));
		l2.setPreferredSize(new Dimension(90, l2.getMinimumSize().height));
		panel1.add(l1, BorderLayout.WEST);
		panel1.add(tFind, BorderLayout.CENTER);
		panel1.add(jButton1, BorderLayout.EAST);
		panel2.add(l2, BorderLayout.WEST);
		panel2.add(tReplace, BorderLayout.CENTER);
		panel2.add(jButton2, BorderLayout.EAST);
		panel3.add(new JLabel(""));
		panel3.add(cb_all);
		panel3.add(new JLabel(""));
		this.add(panel1);
		this.add(panel2);
		this.add(panel3);

	}

	void jButton1_actionPerformed(ActionEvent e) {
		String f_st = tFind.getText();
		if (f_st.length() == 0)
			return;
		boolean stop = false;
		int pos = text.getCaretPosition();
		text.grabFocus();
		try {
			while (pos != text.getText().length() && !stop) {
				text.setSelectionStart(pos);
				text.setSelectionEnd(pos + f_st.length());
				if (text.getSelectedText().equals(f_st))
					stop = true;
				pos++;
			}
		} catch (Exception e1) {
		}
	}

	void jButton2_actionPerformed(ActionEvent e) {
		String r_st = tReplace.getText();
		String f_st = tFind.getText();
		if (f_st.length() == 0)
			return;
		text.grabFocus();
		try {
			if (cb_all.isSelected()) {
				text.setSelectionStart(text.getCaretPosition());
				text.setSelectionEnd(text.getText().length());
				if (text.getSelectedText() != null
						&& text.getSelectedText().trim().length() != 0)
					text.replaceSelection(text.getSelectedText().replaceAll(
							f_st, r_st));
			} else {
				if (text.getSelectedText() != null
						&& text.getSelectedText().trim().length() != 0) {
					if (!text.getSelectedText().equals(f_st))
						text.setCaretPosition(text.getSelectionStart());
					else
						text.replaceSelection(tReplace.getText());
				}
				jButton1_actionPerformed(e);
			}
		} catch (Exception e1) {
		}
	}
}
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

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy
 */
public class ImpactLogo extends JWindow implements Runnable {

	public ImpactLogo(Frame frame, String v) {
		super(frame != null ? frame : new Frame());
		m_name = v;
		setBackground(Color.white);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		reshape((screenSize.width - m_w) / 2, (screenSize.height - m_h) / 2,
				m_w, m_h);
		hide();
		show();
		m_run = new Thread(this);
		m_run.start();
	}

	public void run() {
		try {
			m_run.sleep(10000);
		} catch (Exception e) {
		}
		this.dispose();
	}

	public void paint(Graphics g) {
		int i = m_w;
		int j = m_h;
		int i3 = -1;
		int j3 = -1;
		Color acolor[] = new Color[4];
		acolor[0] = Color.black;
		acolor[1] = Color.gray;
		acolor[2] = SystemColor.control;
		acolor[3] = Color.white;
		g.setColor(SystemColor.control);
		g.fillRect(0, 0, i, j);
		while (i3 + j3 < 0) {
			if (i3 < 0)
				i3 = 0;
			if (j3 < 0) {
				j3 = 0;
				g.setColor(Color.white);
				g.setFont(new Font("SansSerif", 1, 13));
				int i1 = 20;
				g.drawString(m_name, i1, 105);
				g.setColor(Color.black);
				g.drawString(m_name, i1 + 1, 106);
			}
		}
		int l2 = 1;
		int j2 = 0;
		i3 = 0x6da440;
		j3 = 0x2936d0;
		// border
		for (int l1 = 0; l1 < 2; l1++) {
			for (int j1 = 0; j1 < 7; j1++) {
				g.setColor(acolor[(j3 >>= 3) & 0x3]);
				g.drawLine(l2, ++j2, l2, --j);
				g.drawLine(l2, j2, --i, j2);
				g.setColor(acolor[(i3 >>= 3) & 0x3]);
				g.drawLine(l2++, j, i, j);
				g.drawLine(i, j2, i, j);
			}
			i3 = 0x4d9090;
			j3 = 0x53408;
		}
		g.drawImage(
				new ImageIcon(ImpactGUI.class.getResource("ImpactLogo.png"))
						.getImage(), 15, 15, this);
	}

	private static int m_w = 530;
	private static int m_h = 130;
	private Thread m_run;
	private String m_name;
}
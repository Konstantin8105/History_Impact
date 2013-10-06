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

/**
 *
 * @author Yuriy Mikhaylovskiy
 * @email YuriyMikhaylovskiy@yahoo.com
 * 
 */

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class ImpactGUINewVersionInfo extends Thread {
	String VERSION;

	public ImpactGUINewVersionInfo(String version) {
		VERSION = version;
	}

	public void run() {
		String st = "";
		try {
			Thread.sleep(10000);
			URL url = new URL(
					"ftp://www.mirrorservice.org/sites/kent.dl.sourceforge.net/pub/sourceforge/i/project/im/impact/impact/");
			URLConnection urlc = url.openConnection();
			InputStream in = urlc.getInputStream();
			int b;
			while ((b = in.read()) != -1) {
				st += (char) b;
			}
			in.close();
			StringTokenizer stt = new StringTokenizer(st, " \t\n");
			int ver_ftp = 0;
			int ver_impact = getVersion(VERSION);
			while (stt.hasMoreTokens()) {
				String st1 = stt.nextToken();
				ver_ftp = Math.max(ver_ftp, getVersion(st1));
			}
			System.out.println("New Impact Version: " + ver_ftp);
			System.out.println("Current Impact Version: " + ver_impact);
			if (ver_ftp > ver_impact) {
				int res = JOptionPane.showConfirmDialog(null,
						"A new version Impact " + ver_ftp + " is available!"
								+ "\n" + "Current version Impact " + ver_impact
								+ "\n" + "Open site for downloading?",
						"A new version Impact is available!",
						JOptionPane.YES_NO_OPTION);
				if (res == 0)
					try {
						Desktop.getDesktop()
								.browse(new URI(
										"http://sourceforge.net/projects/impact/"));
					} catch (Exception e1) {
					}
			}
		} catch (Exception e) {
		}
	}

	private int getVersion(String st) {
		int v = 0;
		String res = "";
		try {
			if (st.indexOf('.') != -1) {
				StringTokenizer stt = new StringTokenizer(
						st,
						"impactIMPACTjmcadJMCAD_=+!@#$%^&*()|[]{}:;\"',`~/-.zipZIPtTrRlLwWnNeEbBqQyYuUoOsSfFgGhHkKxXvV \t\n");
				while (stt.hasMoreTokens()) {
					res += stt.nextToken();

				}
				v = Integer.parseInt(res);
			}
		} catch (Exception e) {
		}
		return v;
	}

}

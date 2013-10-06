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

import java.awt.*;
import java.awt.image.*;

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy
 */

public class libColor {
	public static Color transformColor(Color color, double z_normal) {
		int i = color.getRed();
		int j = color.getGreen();
		int k = color.getBlue();
		if (i > 230)
			i -= 50;
		else
			i += 50;
		if (j > 230)
			j -= 50;
		else
			j += 50;
		if (k > 230)
			k -= 50;
		else
			k += 50;
		// z_normal = 0.0d-z_normal;
		i = (int) (i + i * z_normal * 0.2);
		j = (int) (j + j * z_normal * 0.2);
		k = (int) (k + k * z_normal * 0.2);
		if (i < 0)
			i = 0;
		else if (i > 255)
			i = 255;
		if (j < 0)
			j = 0;
		else if (j > 255)
			j = 255;
		if (k < 0)
			k = 0;
		else if (k > 255)
			k = 255;
		// System.out.println(z_normal);
		return new Color(i, j, k);
	}

	public static BufferedImage gradient3p(int x1, int y1, Color c1, int x2,
			int y2, Color c2, int x3, int y3, Color c3) {
		double l1 = 0, l2 = 0, l3 = 0;
		try {
			int xmin = Math.min(Math.min(x1, x2), x3);
			int xmax = Math.max(Math.max(x1, x2), x3);
			int ymin = Math.min(Math.min(y1, y2), y3);
			int ymax = Math.max(Math.max(y1, y2), y3);
			int w = Math.abs(xmax - xmin);
			int h = Math.abs(ymax - ymin);
			if (w == 0 || h == 0)
				return null;
			int pixels[] = new int[w * h];
			int c[] = new int[3];
			int index = 0;
			int r1 = c1.getRed();
			int r2 = c2.getRed();
			int r3 = c3.getRed();
			int g1 = c1.getGreen();
			int g2 = c2.getGreen();
			int g3 = c3.getGreen();
			int b1 = c1.getBlue();
			int b2 = c2.getBlue();
			int b3 = c3.getBlue();
			/*
			 * int x12 = x1-x2; int x13 = x1-x3; int y12 = y1-y2; int y13 =
			 * y1-y3; int r12 = r1-r2; int r13 = r1-r3; float rsx12 =
			 * Math.abs(r12/x12); float rsy12 = Math.abs(r12/y12); float rsx13 =
			 * Math.abs(r13/x13); float rsy13 = Math.abs(r13/y13);
			 */
			for (int i = 0; i < h; i++) {
				for (int j = 0; j < w; j++) {
					try {
						c[0] = c[1] = c[2] = 0;
						// l1 =
						// Math.sqrt(Math.pow(x1-xmin-j,2)+Math.pow(ymax-i-y2,2));
						// l2 =
						// Math.sqrt(Math.pow(x2-xmin-j,2)+Math.pow(ymax-i-y2,2));
						// l3 =
						// Math.sqrt(Math.pow(x3-xmin-j,2)+Math.pow(ymax-i-y3,2));
						c[0] = (int) (r1
								* Math.abs((x2 - xmin - j) / (x1 - x2)) + r2
								* Math.abs((x1 - xmin - j) / (x1 - x2)));
						/*
						 * c[0] =
						 * (int)(r1+l1*(r2-r1)/(l1+l2)+r1+l1*(r3-r1)/(l1+l3))/2;
						 * c[1] =
						 * (int)(g1+l1*(g2-r1)/(l1+l2)+g1+l1*(g3-g1)/(l1+l3))/2;
						 * c[2] =
						 * (int)(b1+l1*(b2-r1)/(l1+l2)+b1+l1*(b3-b1)/(l1+l3))/2;
						 */
						// c[1] = (int)(g1+l1*(g2-g1)/(l1+l2));
						// c[2] = (int)(b1+l1*(b2-b1)/(l1+l2));
						// c[0] = (int)(r1/l2*l1-r2/l1*l2+r1/l3*l1-r3/l1*l3)/2;
						// c[1] = (int)(g1/l2*l1-g2/l1*l2+g1/l3*l1-g3/l1*l3)/2;
						// c[2] = (int)(b1/l2*l1-b2/l1*l2+b1/l3*l1-b3/l1*l3)/2;
					} catch (Exception e1) {
						System.out.println(e1 + "---  " + l1 + "  " + l2 + "  "
								+ l3);
						return null;
					}
					// c[1] = (int)(g1/l1*l2+g2/l2*l1+g1/l1*l3+g3/l3*l1)/2;
					// c[2] = (int)(b1/l1*l2+b2/l2*l1+b1/l1*l3+b3/l3*l1)/2;
					// c[0]=r1+(int)((Math.abs(x1-j-xmin)/rsx12+Math.abs(y1-i-ymin)/rsy12)/2);
					if (c[0] < 0) {
						c[0] = 0;
					}
					if (c[0] > 255) {
						c[0] = 255;
					}
					if (c[1] < 0) {
						c[0] = 0;
					}
					if (c[1] > 255) {
						c[1] = 255;
					}
					if (c[2] < 0) {
						c[0] = 0;
					}
					if (c[2] > 255) {
						c[2] = 255;
					}
					// c[0] = 155;
					pixels[index++] = ((c[0] << 16) | (c[1] << 8) | (c[2] << 0));
				}
			}
			BufferedImage bimg = new BufferedImage(w, h,
					BufferedImage.TYPE_INT_RGB);
			bimg.setRGB(0, 0, w, h, pixels, 0, w);
			return bimg;
		} catch (Exception e) {
			System.out.println(e + "  " + l1 + "  " + l2 + "  " + l3);
			return null;
		}
	}
}
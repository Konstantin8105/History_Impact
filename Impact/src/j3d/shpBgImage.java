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
import java.awt.geom.*;
import java.awt.image.*;

import javax.media.opengl.GL;

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy
 */

public class shpBgImage implements shp {
	private BufferedImage img;
	private int x = 0;
	private int y = 0;
	private boolean show;

	public void paint(Graphics2D g, Canvas3D j3d) {
		g.drawImage(img, null, x, y);
	}

	public shpBgImage(BufferedImage i, int xx, int yy) {
		img = i;
		x = xx;
		y = yy;
	}

	public shpBgImage(BufferedImage i) {
		img = i;
	}

	public void transform2D(Matrix3D t, Vector3D cor, Canvas3D J3D) {
	}

	public float get_Z() {
		return -java.lang.Float.MAX_VALUE;
	}

	public boolean isPickPoint(int x, int y, boolean ogl) {
		return false;
	}

	public boolean isPickPoint(Rectangle2D r, boolean ogl) {
		return false;
	}

	public void setPicked(boolean s) {
		// do nothing
	}

	public void setShow(boolean s) {
		show = s;
	}

	public boolean isShow() {
		return show;
	}

	public float[] getBoundaries() {
		// TODO Auto-generated method stub
		return null;
	}

	public float[] get2DBoundaries() {
		// TODO Auto-generated method stub
		return null;
	}

	public void paintGL(GL gl, Graphics2D g2D, Canvas3D j3d, float rcx,
			float rcy, float rcz, float scale) {

		this.paint(g2D, j3d);

	}

}
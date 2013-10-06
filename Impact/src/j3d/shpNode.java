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

import javax.media.opengl.GL;

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy
 */

public class shpNode extends Rectangle2D.Double implements shp {
	private float sx, sy, sz;
	private float cr, cg, cb;
	private Color color;
	private float z_sort;
	private boolean show;
	private boolean picked = false;

	public void paint(Graphics2D g, Canvas3D j3d) {
		g.setPaint(color);
		g.draw(this);
	}

	public shpNode(float x, float y, float z, Color c) {
		sx = x;
		sy = y;
		sz = z;
		color = c;
		cr = c.getRed() / 255f;
		cg = c.getGreen() / 255f;
		cb = c.getBlue() / 255f;
	}

	public void transform2D(Matrix3D t, Vector3D cor, Canvas3D J3D) {
		sx += cor.x;
		sy += cor.y;
		sz += cor.z;

		x = sx * t.xx + sy * t.xy + sz * t.xz + t.xo - J3D.getNODESIZE() / 2;
		y = sx * t.yx + sy * t.yy + sz * t.yz + t.yo - J3D.getNODESIZE() / 2;
		z_sort = sx * t.zx + sy * t.zy + sz * t.zz + t.zo;
		this.width = J3D.getNODESIZE();
		this.height = J3D.getNODESIZE();

		sx -= cor.x;
		sy -= cor.y;
		sz -= cor.z;
	}

	public float get_Z() {
		return z_sort;
	}

	public boolean isPickPoint(int x, int y, boolean ogl) {

		if (ogl == true)
			return picked;

		return this.contains((double) x, (double) y);

	}

	public boolean isPickPoint(Rectangle2D r, boolean ogl) {

		if (ogl == true)
			return picked;

		return r.contains((double) x, (double) y);

	}

	public void setPicked(boolean s) {
		picked = s;
	}

	public void setShow(boolean s) {
		show = s;
	}

	public boolean isShow() {
		return show;
	}

	public float[] getBoundaries() {
		float[] b = new float[6];

		b[0] = sx;
		b[1] = sx;
		b[2] = sy;
		b[3] = sy;
		b[4] = sz;
		b[5] = sz;

		return b;
	}

	public float[] get2DBoundaries() {
		float[] b = new float[4];

		b[0] = (float) x;
		b[1] = (float) x;
		b[2] = (float) y;
		b[3] = (float) y;

		return b;
	}

	public void paintGL(GL gl, Graphics2D g2D, Canvas3D j3d, float rcx,
			float rcy, float rcz, float scale) {
		gl.glColor3f(cr, cg, cb);
		gl.glPointSize(2.0f);

		gl.glBegin(GL.GL_POINTS);

		gl.glVertex3f(sx - rcx, sy - rcy, sz - rcz);

		gl.glEnd();

	}
}
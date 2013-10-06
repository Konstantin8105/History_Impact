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

public class shpLine extends Line2D.Double implements shp {
	private float sx1, sx2, sy1, sy2, sz1, sz2;
	private float cr, cg, cb;
	private Color cl;
	private float z_sort;
	private boolean show;
	private boolean picked;

	public void paint(Graphics2D g, Canvas3D j3d) {
		Stroke s;
		g.setColor(cl);
		g.getStroke();
		if (cl == j3d.SELECTCOLOR) {
			s = g.getStroke();
			g.setStroke(new BasicStroke(2));
			g.draw(this);
			g.setStroke(s);
		} else
			g.draw(this);
	}

	public shpLine(float x1, float y1, float z1, float x2, float y2, float z2,
			Color c) {
		sx1 = x1;
		sx2 = x2;
		sy1 = y1;
		sy2 = y2;
		sz1 = z1;
		sz2 = z2;
		cl = c;
		cr = c.getRed() / 255f;
		cg = c.getGreen() / 255f;
		cb = c.getBlue() / 255f;

	}

	public void transform2D(Matrix3D t, Vector3D cor, Canvas3D J3D) {
		sx1 += cor.x;
		sx2 += cor.x;
		sy1 += cor.y;
		sy2 += cor.y;
		sz1 += cor.z;
		sz2 += cor.z;

		x1 = sx1 * t.xx + sy1 * t.xy + sz1 * t.xz + t.xo;
		x2 = sx2 * t.xx + sy2 * t.xy + sz2 * t.xz + t.xo;
		y1 = sx1 * t.yx + sy1 * t.yy + sz1 * t.yz + t.yo;
		y2 = sx2 * t.yx + sy2 * t.yy + sz2 * t.yz + t.yo;
		z_sort = ((sx1 + sx2) * t.zx + (sy1 + sy2) * t.zy + (sz1 + sz2) * t.zz)
				/ 2 + t.zo;

		sx1 -= cor.x;
		sx2 -= cor.x;
		sy1 -= cor.y;
		sy2 -= cor.y;
		sz1 -= cor.z;
		sz2 -= cor.z;
	}

	public float get_Z() {
		return z_sort;
	}

	public boolean isPickPoint(int x, int y, boolean ogl) {

		if (ogl == true)
			return picked;

		if (ptSegDistSq((double) x, (double) y) <= Canvas3D.PICKDISTANCE)
			return true;

		return false;

	}

	public boolean isPickPoint(Rectangle2D r, boolean ogl) {
		float[] b = this.get2DBoundaries();

		if (ogl == true)
			return picked;

		return r.contains((int) b[1], (int) b[3], (int) (b[0] - b[1]) + 1,
				(int) (b[2] - b[3]) + 1);

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
		b[0] = Math.max(sx1, sx2);
		b[1] = Math.min(sx1, sx2);
		b[2] = Math.max(sy1, sy2);
		b[3] = Math.min(sy1, sy2);
		b[4] = Math.max(sz1, sz2);
		b[5] = Math.min(sz1, sz2);
		return b;
	}

	public float[] get2DBoundaries() {
		float[] b = new float[4];
		b[0] = (float) Math.max(x1, x2);
		b[1] = (float) Math.min(x1, x2);
		b[2] = (float) Math.max(y1, y2);
		b[3] = (float) Math.min(y1, y2);
		return b;
	}

	public void paintGL(GL gl, Graphics2D g2D, Canvas3D j3d, float rcx,
			float rcy, float rcz, float scale) {
		gl.glColor3f(cr, cg, cb);

		gl.glBegin(GL.GL_LINES);

		gl.glVertex3f(sx1 - rcx, sy1 - rcy, sz1 - rcz);
		gl.glVertex3f(sx2 - rcx, sy2 - rcy, sz2 - rcz);

		gl.glEnd();

	}

}
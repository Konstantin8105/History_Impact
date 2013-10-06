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
import java.awt.font.*;
import java.awt.geom.*;

import javax.media.opengl.GL;
import com.sun.opengl.util.GLUT;

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy
 */

public class shpOrientedText extends Rectangle2D.Double implements shp {
	private float sx, sy, sz, x, y, z;
	private float cr, cg, cb;
	private String text;
	private Color color;
	private boolean show = true;
	static final GLUT glut = new GLUT();

	public void paint(Graphics2D g, Canvas3D j3d) {
		if (!j3d.getDRAFTMODE()) {
			TextLayout tx = new TextLayout(text, g.getFont(),
					g.getFontRenderContext());
			g.setColor(color);
			tx.draw(g, x, y);
		}
	}

	public void paintGL(GL gl, Graphics2D g2D, Canvas3D j3d, float rcx,
			float rcy, float rcz, float scale) {
		if (!j3d.getDRAFTMODE()) {
			gl.glColor3f(cr, cg, cb);
			gl.glRasterPos3f(sx - rcx, sy - rcy, sz - rcz);
			glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, text);
		}
	}

	public shpOrientedText(String st, float x, float y, float z) {
		text = st;
		sx = x;
		sy = y;
		sz = z;
		color = Color.black;
		cr = color.getRed() / 255f;
		cg = color.getGreen() / 255f;
		cb = color.getBlue() / 255f;

	}

	public shpOrientedText(String st, float x, float y, float z, Color cl) {
		text = st;
		sx = x;
		sy = y;
		sz = z;
		color = cl;
		cr = cl.getRed() / 255f;
		cg = cl.getGreen() / 255f;
		cb = cl.getBlue() / 255f;
	}

	public void transform2D(Matrix3D t, Vector3D cor, Canvas3D J3D) {
		sx += cor.x;
		sy += cor.y;
		sz += cor.z;

		x = sx * t.xx + sy * t.xy + sz * t.xz + t.xo;
		y = sx * t.yx + sy * t.yy + sz * t.yz + t.yo;
		z = sx * t.zx + sy * t.zy + sz * t.zz + t.zo;
		this.width = 0;
		this.height = 0;

		sx -= cor.x;
		sy -= cor.y;
		sz -= cor.z;
	}

	public float get_Z() {
		return z;
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

}
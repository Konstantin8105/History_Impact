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
import java.awt.font.*;

import javax.media.opengl.GL;

import com.sun.opengl.util.GLUT;

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy
 */

public class shpXYZ implements shp {
	private float x0, y0, xx, xy, yx, yy, zx, zy, sx, sy, sz;
	private float size = 20;
	public boolean show = true;
	public boolean picked = false;
	static final GLUT glut = new GLUT();

	public void paint(Graphics2D g, Canvas3D j3d) {
		g.setColor(Color.black);
		// g.setStroke(new BasicStroke(2));
		g.draw(new Line2D.Double(x0, y0, xx, xy));
		g.draw(new Line2D.Double(x0, y0, yx, yy));
		g.draw(new Line2D.Double(x0, y0, zx, zy));
		g.setColor(Color.blue);
		TextLayout tx = new TextLayout("X", g.getFont(),
				g.getFontRenderContext());
		tx.draw(g, xx, xy);
		TextLayout ty = new TextLayout("Y", g.getFont(),
				g.getFontRenderContext());
		ty.draw(g, yx, yy);
		TextLayout tz = new TextLayout("Z", g.getFont(),
				g.getFontRenderContext());
		tz.draw(g, zx, zy);
	}

	public shpXYZ() {
	}

	public void transform2D(Matrix3D t, Vector3D cor, Canvas3D J3D) {

		sx = 0; // Set point at center of rotation
		sy = 0;
		sz = 0;

		x0 = sx * t.xx / t.sx + sy * t.xy / t.sy + sz * t.xz / t.sz + t.xo;
		y0 = sx * t.yx / t.sx + sy * t.yy / t.sy + sz * t.yz / t.sz + t.yo;

		sx += size;

		xx = sx * t.xx / t.sx + sy * t.xy / t.sy + sz * t.xz / t.sz + t.xo;
		xy = sx * t.yx / t.sx + sy * t.yy / t.sy + sz * t.yz / t.sz + t.yo;

		sx -= size;
		sy -= size;

		yx = sx * t.xx / t.sx + sy * t.xy / t.sy + sz * t.xz / t.sz + t.xo;
		yy = sx * t.yx / t.sx + sy * t.yy / t.sy + sz * t.yz / t.sz + t.yo;

		sy += size;
		sz += size;

		zx = sx * t.xx / t.sx + sy * t.xy / t.sy + sz * t.xz / t.sz + t.xo;
		zy = sx * t.yx / t.sx + sy * t.yy / t.sy + sz * t.yz / t.sz + t.yo;

		sz -= size;

		sx -= cor.x;
		sy -= cor.y;
		sz -= cor.z;

	}

	public float get_Z() {
		return java.lang.Float.MAX_VALUE;
	}

	public boolean isPickPoint(int x, int y, boolean ogl) {

		return false;

	}

	public boolean isPickPoint(Rectangle2D r, boolean ogl) {

		return false;

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
		// TODO Auto-generated method stub
		return null;
	}

	public float[] get2DBoundaries() {
		// TODO Auto-generated method stub
		return null;
	}

	public void paintGL(GL gl, Graphics2D g2D, Canvas3D j3d, float rcx,
			float rcy, float rcz, float scale) {
		float p = 20 * scale;
		// TODO Auto-generated method stub
		gl.glColor3f(0f, 0f, 0f);

		gl.glBegin(GL.GL_LINES);
		gl.glVertex3f(0, 0, 0);
		gl.glVertex3f(p, 0, 0);
		gl.glEnd();
		gl.glRasterPos3f(p, 0, 0);
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, "X");

		gl.glColor3f(1f, 0f, 0f);

		gl.glBegin(GL.GL_LINES);
		gl.glVertex3f(0, 0, 0);
		gl.glVertex3f(0, p, 0);
		gl.glEnd();
		gl.glRasterPos3f(0, p, 0);
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, "Y");

		gl.glColor3f(0f, 1f, 0f);

		gl.glBegin(GL.GL_LINES);
		gl.glVertex3f(0, 0, 0);
		gl.glVertex3f(0, 0, p);
		gl.glEnd();
		gl.glRasterPos3f(0, 0, p);
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, "Z");

	}

}
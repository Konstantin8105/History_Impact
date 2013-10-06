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

public class shpTriangle extends Area implements shp {
	private float sx1, sx2, sx3, sy1, sy2, sy3, sz1, sz2, sz3;
	private float x1, x2, x3, y1, y2, y3, z1, z2, z3;
	private float cr, cg, cb;
	private Color contur;
	private Paint fill;
	private Color[] gradient = null;
	private float z_sort;
	private double z_normal;
	private boolean show;
	private boolean picked = false;
	private float l, vx1, vx2, vy1, vy2, vz1, vz2, nx, ny, nz;

	public shpTriangle(float x1, float y1, float z1, float x2, float y2,
			float z2, float x3, float y3, float z3, Paint f, Color cont) {
		sx1 = x1;
		sx2 = x2;
		sx3 = x3;
		sy1 = y1;
		sy2 = y2;
		sy3 = y3;
		sz1 = z1;
		sz2 = z2;
		sz3 = z3;
		fill = f;
		gradient = null;
		contur = cont;
		cr = cont.getRed() / 255f;
		cg = cont.getGreen() / 255f;
		cb = cont.getBlue() / 255f;
		calculateNormal();
	}

	public shpTriangle(float x1, float y1, float z1, Color c1, float x2,
			float y2, float z2, Color c2, float x3, float y3, float z3,
			Color c3, Color cont) {
		sx1 = x1;
		sx2 = x2;
		sx3 = x3;
		sy1 = y1;
		sy2 = y2;
		sy3 = y3;
		sz1 = z1;
		sz2 = z2;
		sz3 = z3;
		fill = null;
		gradient = new Color[4];
		gradient[0] = c1;
		gradient[1] = c2;
		gradient[2] = c3;
		gradient[3] = new Color((c1.getRed() + c2.getRed() + c3.getRed()) / 3,
				(c1.getGreen() + c2.getGreen() + c3.getGreen()) / 3,
				(c1.getBlue() + c2.getBlue() + c3.getBlue()) / 3);
		contur = cont;
		cr = cont.getRed() / 255f;
		cg = cont.getGreen() / 255f;
		cb = cont.getBlue() / 255f;
		calculateNormal();
	}

	public shpTriangle(float x1, float y1, float z1, Color c1, float x2,
			float y2, float z2, Color c2, float x3, float y3, float z3,
			Color c3, Color f, Color cont) {
		sx1 = x1;
		sx2 = x2;
		sx3 = x3;
		sy1 = y1;
		sy2 = y2;
		sy3 = y3;
		sz1 = z1;
		sz2 = z2;
		sz3 = z3;
		fill = null;
		gradient = new Color[4];
		gradient[0] = c1;
		gradient[1] = c2;
		gradient[2] = c3;
		gradient[3] = f;
		contur = cont;
		cr = cont.getRed() / 255f;
		cg = cont.getGreen() / 255f;
		cb = cont.getBlue() / 255f;
		calculateNormal();
	}

	public void paint(Graphics2D g, Canvas3D j3d) {
		if (j3d.getDRAFTMODE()) {
			g.setColor(contur);
			g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
			g.drawLine((int) x2, (int) y2, (int) x3, (int) y3);
			g.drawLine((int) x1, (int) y1, (int) x3, (int) y3);
			return;
		}
		if ((fill != null || gradient != null)
				&& (j3d.getGRAPHICSMODE() == Canvas3D.GRAPHICSMODE_SOLID || j3d
						.getGRAPHICSMODE() == Canvas3D.GRAPHICSMODE_SURFACE)) {
			if (gradient == null) {
				try {
					g.setPaint(libColor.transformColor((Color) fill, z_normal));
				} catch (Exception e) {
					g.setPaint(fill);
				}
				g.fill(this);
			} else if (j3d.getRENDERMODE() == Canvas3D.RENDERMODE_HIDE) {
				g.setPaint(j3d.getBGCOLOR());
				g.fill(this);
			} else if (j3d.getRENDERMODE() == Canvas3D.RENDERMODE_SHADE) {
				g.setPaint(libColor.transformColor(gradient[3], z_normal));
				g.fill(this);
			} else {
				try {
					Color[] gradient0 = new Color[gradient.length];
					for (int i = 0; i < gradient.length; i++) {
						gradient0[i] = libColor.transformColor(gradient[i],
								z_normal);
					}
					Point2D p12 = lib2D.centerL(x1, y1, x2, y2);
					Point2D p13 = lib2D.centerL(x1, y1, x3, y3);
					Point2D p23 = lib2D.centerL(x3, y3, x2, y2);
					Point2D pc1 = lib2D.centerL(p12.getX(), p12.getY(),
							p13.getX(), p13.getY());
					Point2D pc2 = lib2D.centerL(p12.getX(), p12.getY(),
							p23.getX(), p23.getY());
					Point2D pc3 = lib2D.centerL(p13.getX(), p13.getY(),
							p23.getX(), p23.getY());
					Point2D pc = lib2D.intersectionLL(
							new Point2D.Double(x1, y1), p23,
							new Point2D.Double(x3, y3), p12, false);
					Color cc = gradient0[3];
					GeneralPath gp = new GeneralPath();
					gp.moveTo(x1, y1);
					gp.lineTo((float) p12.getX(), (float) p12.getY());
					gp.lineTo((float) pc.getX(), (float) pc.getY());
					gp.lineTo((float) p13.getX(), (float) p13.getY());
					gp.closePath();
					g.setPaint(new GradientPaint(x1, y1, gradient0[0],
							(float) pc1.getX(), (float) pc1.getY(), cc));
					g.fill(gp);
					gp = new GeneralPath();
					gp.moveTo(x2, y2);
					gp.lineTo((float) p12.getX(), (float) p12.getY());
					gp.lineTo((float) pc.getX(), (float) pc.getY());
					gp.lineTo((float) p23.getX(), (float) p23.getY());
					gp.closePath();
					g.setPaint(new GradientPaint(x2, y2, gradient0[1],
							(float) pc2.getX(), (float) pc2.getY(), cc));
					g.fill(gp);
					gp = new GeneralPath();
					gp.moveTo(x3, y3);
					gp.lineTo((float) p13.getX(), (float) p13.getY());
					gp.lineTo((float) pc.getX(), (float) pc.getY());
					gp.lineTo((float) p23.getX(), (float) p23.getY());
					gp.closePath();
					g.setPaint(new GradientPaint(x3, y3, gradient0[2],
							(float) pc3.getX(), (float) pc3.getY(), cc));
					g.fill(gp);
				} catch (Exception e) {/*
										 * System.out.println(
										 * "Area of a triangle = 0");
										 */
				}
			}
		}
		if (contur != null
				&& (j3d.getGRAPHICSMODE() == Canvas3D.GRAPHICSMODE_WIREFRAME
						|| j3d.getGRAPHICSMODE() == Canvas3D.GRAPHICSMODE_SURFACE || j3d
						.getRENDERMODE() == Canvas3D.RENDERMODE_HIDE)) {
			g.setColor(contur);
			g.draw(this);
		}
	}

	public void paintGL(GL gl, Graphics2D g2D, Canvas3D j3d, float rcx,
			float rcy, float rcz, float scale) {
		Color c;

		if (j3d.getDRAFTMODE()) {
			gl.glColor3f(cr, cg, cb);
			gl.glBegin(GL.GL_LINE_LOOP);
			gl.glVertex3f(sx1 - rcx, sy1 - rcy, sz1 - rcz);
			gl.glVertex3f(sx2 - rcx, sy2 - rcy, sz2 - rcz);
			gl.glVertex3f(sx3 - rcx, sy3 - rcy, sz3 - rcz);
			gl.glEnd();
			return;
		}
		if ((fill != null || gradient != null)
				&& (j3d.getGRAPHICSMODE() == Canvas3D.GRAPHICSMODE_SOLID || j3d
						.getGRAPHICSMODE() == Canvas3D.GRAPHICSMODE_SURFACE)) {
			if (gradient == null) {
				c = (Color) fill;
				gl.glColor3f(0.3f * c.getRed() / 255f,
						0.3f * c.getGreen() / 255f, 0.3f * c.getBlue() / 255f);
				gl.glBegin(GL.GL_TRIANGLES);
				gl.glNormal3f(nx, ny, nz);
				gl.glVertex3f(sx1 - rcx, sy1 - rcy, sz1 - rcz);
				gl.glVertex3f(sx2 - rcx, sy2 - rcy, sz2 - rcz);
				gl.glVertex3f(sx3 - rcx, sy3 - rcy, sz3 - rcz);
				gl.glEnd();
			} else if (j3d.getRENDERMODE() == Canvas3D.RENDERMODE_HIDE) {
				gl.glColor3f(0.3f * j3d.getBGCOLOR().getRed() / 255f,
						0.3f * j3d.getBGCOLOR().getGreen() / 255f, 0.3f * j3d
								.getBGCOLOR().getBlue() / 255f);
				gl.glBegin(GL.GL_TRIANGLES);
				gl.glNormal3f(nx, ny, nz);
				gl.glVertex3f(sx1 - rcx, sy1 - rcy, sz1 - rcz);
				gl.glVertex3f(sx2 - rcx, sy2 - rcy, sz2 - rcz);
				gl.glVertex3f(sx3 - rcx, sy3 - rcy, sz3 - rcz);
				gl.glEnd();
				gl.glColor3f(cr, cg, cb);
				gl.glBegin(GL.GL_LINE_LOOP);
				gl.glVertex3f(sx1 - rcx, sy1 - rcy, sz1 - rcz);
				gl.glVertex3f(sx2 - rcx, sy2 - rcy, sz2 - rcz);
				gl.glVertex3f(sx3 - rcx, sy3 - rcy, sz3 - rcz);
				gl.glEnd();
			} else if (j3d.getRENDERMODE() == Canvas3D.RENDERMODE_SHADE) {
				c = libColor.transformColor(gradient[3], z_normal);
				gl.glColor3f(0.3f * c.getRed() / 255f,
						0.3f * c.getGreen() / 255f, 0.3f * c.getBlue() / 255f);
				gl.glBegin(GL.GL_TRIANGLES);
				gl.glNormal3f(nx, ny, nz);
				gl.glVertex3f(sx1 - rcx, sy1 - rcy, sz1 - rcz);
				gl.glVertex3f(sx2 - rcx, sy2 - rcy, sz2 - rcz);
				gl.glVertex3f(sx3 - rcx, sy3 - rcy, sz3 - rcz);
				gl.glEnd();
			} else {
				gl.glColor3f(0.3f * cr, 0.3f * cg, 0.3f * cb);
				gl.glBegin(GL.GL_TRIANGLES);
				gl.glNormal3f(nx, ny, nz);
				gl.glVertex3f(sx1 - rcx, sy1 - rcy, sz1 - rcz);
				gl.glVertex3f(sx2 - rcx, sy2 - rcy, sz2 - rcz);
				gl.glVertex3f(sx3 - rcx, sy3 - rcy, sz3 - rcz);
				gl.glEnd();

			}/*
			 * else{ try{ Color[] gradient0 = new Color[gradient.length];
			 * for(int i=0; i<gradient.length; i++){
			 * gradient0[i]=libColor.transformColor(gradient[i],z_normal); }
			 * Point2D p12 = lib2D.centerL(x1,y1,x2,y2); Point2D p13 =
			 * lib2D.centerL(x1,y1,x3,y3); Point2D p23 =
			 * lib2D.centerL(x3,y3,x2,y2); Point2D pc1 =
			 * lib2D.centerL(p12.getX(),p12.getY(),p13.getX(),p13.getY());
			 * Point2D pc2 =
			 * lib2D.centerL(p12.getX(),p12.getY(),p23.getX(),p23.getY());
			 * Point2D pc3 =
			 * lib2D.centerL(p13.getX(),p13.getY(),p23.getX(),p23.getY());
			 * Point2D pc = lib2D.intersectionLL(new
			 * Point2D.Double(x1,y1),p23,new Point2D.Double(x3,y3),p12,false);
			 * Color cc=gradient0[3]; GeneralPath gp = new GeneralPath();
			 * gp.moveTo(x1,y1); gp.lineTo((float)p12.getX(),(float)p12.getY());
			 * gp.lineTo((float)pc.getX(),(float)pc.getY());
			 * gp.lineTo((float)p13.getX(),(float)p13.getY()); gp.closePath();
			 * g.setPaint(new
			 * GradientPaint(x1,y1,gradient0[0],(float)pc1.getX(),
			 * (float)pc1.getY(),cc)); g.fill(gp); gp = new GeneralPath();
			 * gp.moveTo(x2,y2); gp.lineTo((float)p12.getX(),(float)p12.getY());
			 * gp.lineTo((float)pc.getX(),(float)pc.getY());
			 * gp.lineTo((float)p23.getX(),(float)p23.getY()); gp.closePath();
			 * g.setPaint(new
			 * GradientPaint(x2,y2,gradient0[1],(float)pc2.getX(),
			 * (float)pc2.getY(),cc)); g.fill(gp); gp = new GeneralPath();
			 * gp.moveTo(x3,y3); gp.lineTo((float)p13.getX(),(float)p13.getY());
			 * gp.lineTo((float)pc.getX(),(float)pc.getY());
			 * gp.lineTo((float)p23.getX(),(float)p23.getY()); gp.closePath();
			 * g.setPaint(new
			 * GradientPaint(x3,y3,gradient0[2],(float)pc3.getX(),
			 * (float)pc3.getY(),cc)); g.fill(gp); }catch(Exception
			 * e){System.out.println("Area of a triangle = 0");} }
			 */
		} else {
			gl.glColor3f(cr, cg, cb);
			gl.glBegin(GL.GL_LINE_LOOP);
			gl.glVertex3f(sx1 - rcx, sy1 - rcy, sz1 - rcz);
			gl.glVertex3f(sx2 - rcx, sy2 - rcy, sz2 - rcz);
			gl.glVertex3f(sx3 - rcx, sy3 - rcy, sz3 - rcz);
			gl.glEnd();
		}

	}

	public void transform2D(Matrix3D t, Vector3D cor, Canvas3D J3D) {
		sx1 += cor.x;
		sy1 += cor.y;
		sz1 += cor.z;
		sx2 += cor.x;
		sy2 += cor.y;
		sz2 += cor.z;
		sx3 += cor.x;
		sy3 += cor.y;
		sz3 += cor.z;

		x1 = sx1 * t.xx + sy1 * t.xy + sz1 * t.xz + t.xo;
		x2 = sx2 * t.xx + sy2 * t.xy + sz2 * t.xz + t.xo;
		x3 = sx3 * t.xx + sy3 * t.xy + sz3 * t.xz + t.xo;
		y1 = sx1 * t.yx + sy1 * t.yy + sz1 * t.yz + t.yo;
		y2 = sx2 * t.yx + sy2 * t.yy + sz2 * t.yz + t.yo;
		y3 = sx3 * t.yx + sy3 * t.yy + sz3 * t.yz + t.yo;
		z1 = sx1 * t.zx + sy1 * t.zy + sz1 * t.zz + t.zo;
		z2 = sx2 * t.zx + sy2 * t.zy + sz2 * t.zz + t.zo;
		z3 = sx3 * t.zx + sy3 * t.zy + sz3 * t.zz + t.zo;
		this.reset();
		GeneralPath gp = new GeneralPath();
		gp.moveTo(x1, y1);
		gp.lineTo(x2, y2);
		gp.lineTo(x3, y3);
		gp.closePath();
		this.add(new Area(gp));
		z_sort = (z1 + z2 + z3) / 3.0f;
		// z_normal =
		// (Math.abs(z1-z2)/Math.sqrt(Math.pow(x1-x2,2)+Math.pow(y1-y2,2))
		// +Math.abs(z1-z3)/Math.sqrt(Math.pow(x1-x3,2)+Math.pow(y1-y3,2))
		// +Math.abs(z3-z2)/Math.sqrt(Math.pow(x3-x2,2)+Math.pow(y3-y2,2)))/3d;
		float xn = (y2 - y1) * (z3 - z1) - (z2 - z1) * (y3 - y1);
		float yn = (z2 - z1) * (x3 - x1) - (x2 - x1) * (z3 - z1);
		float zn = (x2 - x1) * (y3 - y1) - (y2 - y1) * (x3 - x1);
		z_normal = -Math.abs(zn / (Math.sqrt(xn * xn + yn * yn + zn * zn)));

		sx1 -= cor.x;
		sy1 -= cor.y;
		sz1 -= cor.z;
		sx2 -= cor.x;
		sy2 -= cor.y;
		sz2 -= cor.z;
		sx3 -= cor.x;
		sy3 -= cor.y;
		sz3 -= cor.z;

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
		b[0] = Math.max(sx1, Math.max(sx2, sx3));
		b[1] = Math.min(sx1, Math.min(sx2, sx3));
		b[2] = Math.max(sy1, Math.max(sy2, sy3));
		b[3] = Math.min(sy1, Math.min(sy2, sy3));
		b[4] = Math.max(sz1, Math.max(sz2, sz3));
		b[5] = Math.min(sz1, Math.min(sz2, sz3));
		return b;
	}

	public float[] get2DBoundaries() {
		float[] b = new float[4];
		b[0] = Math.max(x1, Math.max(x2, x3));
		b[1] = Math.min(x1, Math.min(x2, x3));
		b[2] = Math.max(y1, Math.max(y2, y3));
		b[3] = Math.min(y1, Math.min(y2, y3));
		return b;
	}

	private void calculateNormal() {
		vx1 = sx2 - sx1;
		vy1 = sy2 - sy1;
		vz1 = sz2 - sz1;

		vx2 = sx3 - sx1;
		vy2 = sy3 - sy1;
		vz2 = sz3 - sz1;

		nx = vy1 * vz2 - vz1 * vy2;
		ny = vz1 * vx2 - vx1 * vz2;
		nz = vx1 * vy2 - vy1 * vx2;

		l = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);

		nx = nx / l;
		ny = ny / l;
		nz = nz / l;

	}

}
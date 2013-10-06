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

import java.io.*;

/**
 * A fairly conventional 3D matrix object that can transform sets of 3D points
 * and perform a variety of manipulations on the transform
 * 
 * @author: Yuriy Mikhaylovskiy
 */

public class Matrix3D implements Serializable {
	float sx, sy, sz;
	float xangle, yangle, zangle;
	float xx, xy, xz, xo;
	float yx, yy, yz, yo;
	float zx, zy, zz, zo;
	static final double pi = 3.14159265;

	/** Create a new unit matrix */
	public Matrix3D() {
		xx = 1.0f;
		yy = 1.0f;
		zz = 1.0f;
		sx = 1.0f;
		sy = 1.0f;
		sz = 1.0f;
		xangle = 0;
		yangle = 0;
		zangle = 0;
	}

	public Matrix3D(float[][] m) {
		xx = m[0][0];
		xy = m[0][1];
		xz = m[0][2];
		xo = m[0][3];
		yx = m[1][0];
		yy = m[1][1];
		yz = m[1][2];
		yo = m[1][3];
		zx = m[2][0];
		zy = m[2][1];
		zz = m[2][2];
		zo = m[2][3];
		sx = 1.0f;
		sy = 1.0f;
		sz = 1.0f;
		xangle = 0;
		yangle = 0;
		zangle = 0;
	}

	/**
	 * Creates a new matrix as a copy of the given matrix
	 * 
	 * @param m
	 *            The matrix to be copied
	 */
	public Matrix3D(Matrix3D m) {
		xx = m.xx;
		xy = m.xy;
		xz = m.xz;
		xo = m.xo;
		yx = m.yx;
		yy = m.yy;
		yz = m.yz;
		yo = m.yo;
		zx = m.zx;
		zy = m.zy;
		zz = m.zz;
		zo = m.zo;
		sx = m.sx;
		sy = m.sy;
		sz = m.sz;
		xangle = m.xangle;
		yangle = m.yangle;
		zangle = m.zangle;
	}

	/**
	 * Creates a matrix using the three vectors as columns The matrix will be
	 * useable as a transformation matrix
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public Matrix3D(Vector3D x, Vector3D y, Vector3D z) {
		xx = x.x;
		xy = y.x;
		xz = z.x;
		xo = 0;
		yx = x.y;
		yy = y.y;
		yz = z.y;
		yo = 0;
		zx = x.z;
		zy = y.z;
		zz = z.z;
		zo = 0;
		sx = 1.0f;
		sy = 1.0f;
		sz = 1.0f;
		xangle = 0;
		yangle = 0;
		zangle = 0;
	}

	/** Scale by f in all dimensions */
	public void scale(float f) {
		sx *= f;
		sy *= f;
		sz *= f;
		xx *= f;
		xy *= f;
		xz *= f;
		xo *= f;
		yx *= f;
		yy *= f;
		yz *= f;
		yo *= f;
		zx *= f;
		zy *= f;
		zz *= f;
		zo *= f;
	}

	/** Scale along each axis independently */
	void scale(float xf, float yf, float zf) {
		sx *= xf;
		sy *= yf;
		sz *= zf;
		xx *= xf;
		xy *= xf;
		xz *= xf;
		xo *= xf;
		yx *= yf;
		yy *= yf;
		yz *= yf;
		yo *= yf;
		zx *= zf;
		zy *= zf;
		zz *= zf;
		zo *= zf;
	}

	/** Translate the origin */
	public void translate(float x, float y, float z) {
		xo += x;
		yo += y;
		zo += z;
	}

	/** rotate theta degrees about the y axis */
	public void yrot(double theta) {
		yangle += theta;
		theta *= (pi / 180);
		double ct = Math.cos(theta);
		double st = Math.sin(theta);

		float Nxx = (float) (xx * ct + zx * st);
		float Nxy = (float) (xy * ct + zy * st);
		float Nxz = (float) (xz * ct + zz * st);
		float Nxo = (float) (xo * ct + zo * st);

		float Nzx = (float) (zx * ct - xx * st);
		float Nzy = (float) (zy * ct - xy * st);
		float Nzz = (float) (zz * ct - xz * st);
		float Nzo = (float) (zo * ct - xo * st);

		xo = Nxo;
		xx = Nxx;
		xy = Nxy;
		xz = Nxz;
		zo = Nzo;
		zx = Nzx;
		zy = Nzy;
		zz = Nzz;
	}

	/** rotate theta degrees about the x axis */
	public void xrot(double theta) {
		xangle += theta;
		theta *= (pi / 180);
		double ct = Math.cos(theta);
		double st = Math.sin(theta);

		float Nyx = (float) (yx * ct + zx * st);
		float Nyy = (float) (yy * ct + zy * st);
		float Nyz = (float) (yz * ct + zz * st);
		float Nyo = (float) (yo * ct + zo * st);

		float Nzx = (float) (zx * ct - yx * st);
		float Nzy = (float) (zy * ct - yy * st);
		float Nzz = (float) (zz * ct - yz * st);
		float Nzo = (float) (zo * ct - yo * st);

		yo = Nyo;
		yx = Nyx;
		yy = Nyy;
		yz = Nyz;
		zo = Nzo;
		zx = Nzx;
		zy = Nzy;
		zz = Nzz;
	}

	/** rotate theta degrees about the z axis */
	public void zrot(double theta) {
		zangle += theta;
		theta *= (pi / 180);
		double ct = Math.cos(theta);
		double st = Math.sin(theta);

		float Nyx = (float) (yx * ct + xx * st);
		float Nyy = (float) (yy * ct + xy * st);
		float Nyz = (float) (yz * ct + xz * st);
		float Nyo = (float) (yo * ct + xo * st);

		float Nxx = (float) (xx * ct - yx * st);
		float Nxy = (float) (xy * ct - yy * st);
		float Nxz = (float) (xz * ct - yz * st);
		float Nxo = (float) (xo * ct - yo * st);

		yo = Nyo;
		yx = Nyx;
		yy = Nyy;
		yz = Nyz;
		xo = Nxo;
		xx = Nxx;
		xy = Nxy;
		xz = Nxz;
	}

	/** viev top */
	public void view_top() {
		xx = sx;
		xy = 0;
		xz = 0;
		yx = 0;
		yy = sy;
		yz = 0;
		zx = 0;
		zy = 0;
		zz = sz;
	}

	/** viev bottom */
	public void view_bottom() {
		float dx = xo;
		float dy = yo;
		float dz = zo;
		xo = 0;
		yo = 0;
		zo = 0;
		view_top();
		yrot(180);
		xo = dx;
		yo = dy;
		zo = dz;
	}

	/** viev NE Isometric */
	public void view_ne() {
		float dx = xo;
		float dy = yo;
		float dz = zo;
		xo = 0;
		yo = 0;
		zo = 0;
		view_top();
		zrot(135);
		xrot(-45);
		xo = dx;
		yo = dy;
		zo = dz;
	}

	/** viev SW Isometric */
	public void view_sw() {
		float dx = xo;
		float dy = yo;
		float dz = zo;
		xo = 0;
		yo = 0;
		zo = 0;
		view_top();
		zrot(-45);
		xrot(-45);
		xo = dx;
		yo = dy;
		zo = dz;
	}

	/** viev SE Isometric */
	public void view_se() {
		float dx = xo;
		float dy = yo;
		float dz = zo;
		xo = 0;
		yo = 0;
		zo = 0;
		view_top();
		zrot(45);
		xrot(-45);
		xo = dx;
		yo = dy;
		zo = dz;
	}

	/** viev NW Isometric */
	public void view_nw() {
		float dx = xo;
		float dy = yo;
		float dz = zo;
		xo = 0;
		yo = 0;
		zo = 0;
		view_top();
		zrot(225);
		xrot(-45);
		xo = dx;
		yo = dy;
		zo = dz;
	}

	/** viev left */
	public void view_left() {
		float dx = xo;
		float dy = yo;
		float dz = zo;
		xo = 0;
		yo = 0;
		zo = 0;
		view_top();
		yrot(90);
		zrot(-90);
		xo = dx;
		yo = dy;
		zo = dz;
	}

	/** viev right */
	public void view_right() {
		float dx = xo;
		float dy = yo;
		float dz = zo;
		xo = 0;
		yo = 0;
		zo = 0;
		view_top();
		yrot(-90);
		zrot(90);
		xo = dx;
		yo = dy;
		zo = dz;
	}

	/** viev front */
	public void view_front() {
		float dx = xo;
		float dy = yo;
		float dz = zo;
		xo = 0;
		yo = 0;
		zo = 0;
		view_top();
		xrot(-90);
		xo = dx;
		yo = dy;
		zo = dz;
	}

	/** viev back */
	public void view_back() {
		float dx = xo;
		float dy = yo;
		float dz = zo;
		xo = 0;
		yo = 0;
		zo = 0;
		view_top();
		yrot(-90);
		xrot(90);
		zrot(90);
		xo = dx;
		yo = dy;
		zo = dz;
	}

	/** Multiply this matrix by a second: M = M*R */
	public void mult(Matrix3D rhs) {
		float lxx = xx * rhs.xx + yx * rhs.xy + zx * rhs.xz;
		float lxy = xy * rhs.xx + yy * rhs.xy + zy * rhs.xz;
		float lxz = xz * rhs.xx + yz * rhs.xy + zz * rhs.xz;
		float lxo = xo * rhs.xx + yo * rhs.xy + zo * rhs.xz + rhs.xo;

		float lyx = xx * rhs.yx + yx * rhs.yy + zx * rhs.yz;
		float lyy = xy * rhs.yx + yy * rhs.yy + zy * rhs.yz;
		float lyz = xz * rhs.yx + yz * rhs.yy + zz * rhs.yz;
		float lyo = xo * rhs.yx + yo * rhs.yy + zo * rhs.yz + rhs.yo;

		float lzx = xx * rhs.zx + yx * rhs.zy + zx * rhs.zz;
		float lzy = xy * rhs.zx + yy * rhs.zy + zy * rhs.zz;
		float lzz = xz * rhs.zx + yz * rhs.zy + zz * rhs.zz;
		float lzo = xo * rhs.zx + yo * rhs.zy + zo * rhs.zz + rhs.zo;

		xx = lxx;
		xy = lxy;
		xz = lxz;
		xo = lxo;

		yx = lyx;
		yy = lyy;
		yz = lyz;
		yo = lyo;

		zx = lzx;
		zy = lzy;
		zz = lzz;
		zo = lzo;
	}

	/** Reinitialize to the unit matrix */
	public void unit() {
		xo = 0;
		xx = 1;
		xy = 0;
		xz = 0;
		yo = 0;
		yx = 0;
		yy = 1;
		yz = 0;
		zo = 0;
		zx = 0;
		zy = 0;
		zz = 1;
	}

	/**
	 * Transform nvert points from v into tv. v contains the input coordinates
	 * in floating point. Three successive entries in the array constitute a
	 * point. tv ends up holding the transformed points as integers; three
	 * successive entries per point
	 */
	public void transform(float v[], int tv[], int nvert) {
		float lxx = xx, lxy = xy, lxz = xz, lxo = xo;
		float lyx = yx, lyy = yy, lyz = yz, lyo = yo;
		float lzx = zx, lzy = zy, lzz = zz, lzo = zo;
		for (int i = nvert * 3; (i -= 3) >= 0;) {
			float x = v[i];
			float y = v[i + 1];
			float z = v[i + 2];
			tv[i] = (int) (x * lxx + y * lxy + z * lxz + lxo);
			tv[i + 1] = (int) (x * lyx + y * lyy + z * lyz + lyo);
			tv[i + 2] = (int) (x * lzx + y * lzy + z * lzz + lzo);
		}
	}

	public String toString() {
		return ("\n[" + xx + "\t" + xy + "\t" + xz + "\t" + xo + "]\n" + "["
				+ yx + "\t" + yy + "\t" + yz + "\t" + yo + "]\n" + "[" + zx
				+ "\t" + zy + "\t" + zz + "\t" + zo + "]\n");
	}

	public void toCoordinateSystem(Vector3D v_normal) {
	}

	/**
	 * Calculates the determinant of the matrix
	 * 
	 * @return the determinant
	 */
	public float det() {
		return xx * yy * zz + xy * yz * zx + xz * yx * zy - xx * yz * zy - xy
				* yx * zz - xz * yy * zx;
	}

	/**
	 * Will invert the current matrix. Note - does not affect the transfer
	 * coordinates ox oy oz or the angles
	 */
	public void invert() {
		float t;
		float d = 1.0f / this.det();

		xx *= d;
		yy *= d;
		zz *= d;

		t = xy;
		xy = yx * d;
		yx = t * d;

		t = xz;
		xz = zx * d;
		zx = t * d;

		t = yz;
		yz = zy * d;
		zy = t * d;
	}

}

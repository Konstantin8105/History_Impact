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

public class Matrix2D implements Serializable {
	float sx, sy;
	// float angle;
	float xx, xy, xo;
	float yx, yy, yo;
	static final double pi = 3.14159265;

	/** Create a new unit matrix */
	public Matrix2D() {
		xx = 1.0f;
		yy = 1.0f;
		sx = 1.0f;
		sy = 1.0f;
	}

	/** Scale by f in all dimensions */
	public void scale(float f) {
		sx *= f;
		sy *= f;
		xx *= f;
		xy *= f;
		xo *= f;
		yx *= f;
		yy *= f;
		yo *= f;
	}

	/** Scale along each axis independently */
	void scale(float xf, float yf, float zf) {
		sx *= xf;
		sy *= yf;
		xx *= xf;
		xy *= xf;
		xo *= xf;
		yx *= yf;
		yy *= yf;
		yo *= yf;
	}

	/** Translate the origin */
	public void translate(float x, float y) {
		xo += x;
		yo += y;
	}

	/** rotate theta degrees about the 0,0 point */
	public void rot(double theta) {
		// angle += theta;
		theta *= (pi / 180);
		double ct = Math.cos(theta);
		double st = Math.sin(theta);

		float Nyx = (float) (yx * ct + xx * st);
		float Nyy = (float) (yy * ct + xy * st);
		float Nyo = (float) (yo * ct + xo * st);

		float Nxx = (float) (xx * ct - yx * st);
		float Nxy = (float) (xy * ct - yy * st);
		float Nxo = (float) (xo * ct - yo * st);

		yo = Nyo;
		yx = Nyx;
		yy = Nyy;
		xo = Nxo;
		xx = Nxx;
		xy = Nxy;
	}

	public void rot(double st, double ct) {

		float Nyx = (float) (yx * ct + xx * st);
		float Nyy = (float) (yy * ct + xy * st);
		float Nyo = (float) (yo * ct + xo * st);

		float Nxx = (float) (xx * ct - yx * st);
		float Nxy = (float) (xy * ct - yy * st);
		float Nxo = (float) (xo * ct - yo * st);

		yo = Nyo;
		yx = Nyx;
		yy = Nyy;
		xo = Nxo;
		xx = Nxx;
		xy = Nxy;
	}

	/** Multiply this matrix by a second: M = M*R */
	public void mult(Matrix3D rhs) {
		float lxx = xx * rhs.xx + yx * rhs.xy;
		float lxy = xy * rhs.xx + yy * rhs.xy;
		float lxo = xo * rhs.xx + yo * rhs.xy + rhs.xo;

		float lyx = xx * rhs.yx + yx * rhs.yy;
		float lyy = xy * rhs.yx + yy * rhs.yy;
		float lyo = xo * rhs.yx + yo * rhs.yy + rhs.yo;

		xx = lxx;
		xy = lxy;
		xo = lxo;

		yx = lyx;
		yy = lyy;
		yo = lyo;
	}

	/** Reinitialize to the unit matrix */
	public void unit() {
		xo = 0;
		xx = 1;
		xy = 0;
		yo = 0;
		yx = 0;
		yy = 1;
	}

	/**
	 * Transform nvert points from v into tv. v contains the input coordinates
	 * in floating point. Three successive entries in the array constitute a
	 * point. tv ends up holding the transformed points as integers; three
	 * successive entries per point
	 */
	public void transform(float v[], int tv[], int nvert) {
		float lxx = xx, lxy = xy, lxo = xo;
		float lyx = yx, lyy = yy, lyo = yo;
		for (int i = nvert * 2; (i -= 2) >= 0;) {
			float x = v[i];
			float y = v[i + 1];
			tv[i] = (int) (x * lxx + y * lxy + lxo);
			tv[i + 1] = (int) (x * lyx + y * lyy + lyo);
		}
	}

	public String toString() {
		return ("\n[" + xx + "\t" + xy + "\t" + xo + "]\n" + "[" + yx + "\t"
				+ yy + "\t" + yo + "]\n");
	}
}

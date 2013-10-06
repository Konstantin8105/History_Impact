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
 * A fairly conventional 3D vector object that can be used for calculation of
 * vectors and matrices. Mainly in the lib3D class.
 * 
 * @author: Jonas Forssell , Yuriy Mikhaylovskiy
 */
public class Vector3D implements Serializable {
	float x, y, z;

	public Vector3D() {
		super();
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}

	public Vector3D(float x, float y, float z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3D(float[] v) {
		super();
		this.x = v[0];
		this.y = v[1];
		this.z = v[2];
	}

	public void add(Vector3D a, Vector3D b) {
		this.x = a.x + b.x;
		this.y = a.y + b.y;
		this.z = a.z + b.z;
	}

	public void sub(Vector3D a, Vector3D b) {
		this.x = a.x - b.x;
		this.y = a.y - b.y;
		this.z = a.z - b.z;
	}

	public void scale(float f) {
		this.x = this.x * f;
		this.y = this.y * f;
		this.z = this.z * f;
	}

	public static float dot(Vector3D a, Vector3D b) {
		return a.x * b.x + a.y * b.y + a.z * b.z;
	}

	public final double dot(Vector3D a) {
		return x * a.x + y * a.y + z * a.z;
	}

	public void mult(Matrix3D a, Vector3D b) {
		float tx, ty, tz;

		tx = a.xx * b.x + a.xy * b.y + a.xz * b.z;
		ty = a.yx * b.x + a.yy * b.y + a.yz * b.z;
		tz = a.zx * b.x + a.zy * b.y + a.zz * b.z;

		this.x = tx;
		this.y = ty;
		this.z = tz;
	}

	public void transMult(Matrix3D a, Vector3D b) {
		float tx, ty, tz;

		tx = a.xx * b.x + a.yx * b.y + a.zx * b.z;
		ty = a.xy * b.x + a.yy * b.y + a.zy * b.z;
		tz = a.xz * b.x + a.yz * b.y + a.zz * b.z;

		this.x = tx;
		this.y = ty;
		this.z = tz;
	}

	public void cross(Vector3D a, Vector3D b) {
		float tx, ty, tz;

		tx = a.y * b.z - b.y * a.z;
		ty = a.z * b.x - b.z * a.x;
		tz = a.x * b.y - b.x * a.y;

		this.x = tx;
		this.y = ty;
		this.z = tz;
	}

	public float getLength() {
		return (float) Math.sqrt(this.x * this.x + this.y * this.y + this.z
				* this.z);
	}

	public float toUnitLength() {
		float l = this.getLength();

		this.x = this.x / l;
		this.y = this.y / l;
		this.z = this.z / l;

		return l;
	}

	public float[] toArray() {
		float[] a = new float[3];

		a[0] = this.x;
		a[1] = this.y;
		a[2] = this.z;

		return a;
	}

	public void copy(Vector3D v) {
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
	}

	public final double length() {
		return Math.sqrt(x * x + y * y + z * z);
	}

	public final void set(float d0, float d1, float d2) {
		x = d0;
		y = d1;
		z = d2;
	}

	public final void set(_Point p) {
		x = p.x;
		y = p.y;
		z = p.z;
	}

}

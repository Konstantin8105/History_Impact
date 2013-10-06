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

import gui.Material;
import j3d.functions.DirectedDistancePointToCurveFunction;
import j3d.functions.DirectedDistancePointToSurfaceFunction;
import j3d.functions.DistanceCurveToCurveFunction;
import j3d.functions.DistanceCurveToSurfaceFunction;
import j3d.functions.DistancePointToCurveFunction;
import j3d.functions.DistancePointToSurfaceFunction;

import java.awt.*;
import java.io.Serializable;
import java.util.*;

import Jama.Matrix;

import util.ConstrainedDownhillSimplex;
import util.GoldenSection;

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy
 */

public class lib3D implements Serializable {

	/**
	 * Caluates the intersection between a <code>_Line</code> object and a
	 * <code>_Triangle</code>.
	 * 
	 * @param line
	 *            The segment to use in the intersection test.
	 * @param triangle
	 *            An array holding the triangle data.
	 * @return point if the <code>_Line</code> intersects the
	 *         <code>_Triangle</code>, <code>null</code> if the
	 *         <code>_Line</code> does not intersect the <code>_Triangle</code>.
	 */
	public static float[] LineAndTriangle(_Element2Post line,
			_Element3Post triangle) {
		_Point pnts[] = new _Point[3];
		pnts[0] = new _Point(triangle.x1, triangle.y1, triangle.z1);
		pnts[1] = new _Point(triangle.x2, triangle.y2, triangle.z2);
		pnts[2] = new _Point(triangle.x3, triangle.y3, triangle.z3);
		_Point spnts[] = new _Point[2];
		spnts[0] = new _Point(line.x1, line.y1, line.z1);
		spnts[1] = new _Point(line.x2, line.y2, line.z2);
		return IntersectLineAndPoly(pnts, spnts);
	}

	/**
	 * Caluates the intersection between a line (l1,l2) object and a triangle
	 * (t1,t2,t3).
	 * 
	 * @param l1
	 *            , l2 The segment to use in the intersection test.
	 * @param t1
	 *            , t2 t3 An array holding the triangle data.
	 * @return point if the line intersects the triangle, <code>null</code> if
	 *         the line does not intersect the object.
	 */
	public static float[] IntersectLineAndTriangle(_Point t1, _Point t2,
			_Point t3, _Point l1, _Point l2) {
		_Point pnts[] = new _Point[3];
		pnts[0] = t1;
		pnts[1] = t2;
		pnts[2] = t3;
		_Point spnts[] = new _Point[2];
		spnts[0] = l1;
		spnts[1] = l2;
		return IntersectLineAndPoly(pnts, spnts);
	}

	/**
	 * Return point if triangle or quad intersects with segment.
	 * */

	private static float[] IntersectLineAndPoly(_Point coordinates[],
			_Point segment[]) {
		Vector3D vec0 = new Vector3D(); // Edge vector from point 0 to point 1;
		Vector3D vec1 = new Vector3D(); // Edge vector from point 0 to point 2
										// or 3;
		Vector3D pNrm = new Vector3D();
		double absNrmX, absNrmY, absNrmZ, pD = 0.0;
		Vector3D tempV3d = new Vector3D();
		Vector3D direction = new Vector3D();
		double pNrmDotrDir = 0.0;
		int axis, nc, sh, nsh;
		_Point start = segment[0];
		_Point end = segment[1];
		double[] dist = new double[1];
		_Point iPnt = new _Point(0, 0, 0); // Point of intersection.
		double uCoor[] = new double[4]; // Only need to support up to quad.
		double vCoor[] = new double[4];
		double tempD;
		int i, j;
		// Compute plane normal.
		for (i = 0; i < coordinates.length - 1;) {
			vec0.x = coordinates[i + 1].x - coordinates[i].x;
			vec0.y = coordinates[i + 1].y - coordinates[i].y;
			vec0.z = coordinates[i + 1].z - coordinates[i++].z;
			if (vec0.length() > 0.0)
				break;
		}
		for (j = i; j < coordinates.length - 1; j++) {
			vec1.x = coordinates[j + 1].x - coordinates[j].x;
			vec1.y = coordinates[j + 1].y - coordinates[j].y;
			vec1.z = coordinates[j + 1].z - coordinates[j].z;
			if (vec1.length() > 0.0)
				break;
		}
		if (j == (coordinates.length - 1)) {
			// System.out.println("(1) Degenerated polygon.");
			return null; // Degenerated polygon.
		}
		pNrm.cross(vec0, vec1);
		if (pNrm.length() == 0.0) {
			return null; // Degenerated polygon.
		}
		// Compute plane D.
		tempV3d.set(coordinates[0]);
		pD = pNrm.dot(tempV3d);
		direction.x = end.x - start.x;
		direction.y = end.y - start.y;
		direction.z = end.z - start.z;
		pNrmDotrDir = pNrm.dot(direction);
		// Segment is parallel to plane.
		if (pNrmDotrDir == 0.0)
			return null;
		tempV3d.set(start);
		dist[0] = (pD - pNrm.dot(tempV3d)) / pNrmDotrDir;
		// Segment intersects the plane behind the segment's start.
		// or exceed the segment's length.
		if ((dist[0] < 0.0) || (dist[0] > 1.0)) {
			// System.out.println("Segment intersects the plane behind the start or exceed end.");
			return null;
		}
		// Now, one thing for sure the segment intersect the plane.
		// Find the intersection point.
		iPnt.x = (float) (start.x + direction.x * dist[0]);
		iPnt.y = (float) (start.y + direction.y * dist[0]);
		iPnt.z = (float) (start.z + direction.z * dist[0]);
		// Project 3d points onto 2d plane and apply Jordan curve theorem.
		// Note : Area of polygon is not preserve in this projection, but
		// it doesn't matter here.
		// Find the axis of projection.
		absNrmX = Math.abs(pNrm.x);
		absNrmY = Math.abs(pNrm.y);
		absNrmZ = Math.abs(pNrm.z);
		if (absNrmX > absNrmY)
			axis = 0;
		else
			axis = 1;
		if (axis == 0) {
			if (absNrmX < absNrmZ)
				axis = 2;
		} else if (axis == 1) {
			if (absNrmY < absNrmZ)
				axis = 2;
		}
		for (i = 0; i < coordinates.length; i++) {
			switch (axis) {
			case 0:
				uCoor[i] = coordinates[i].y - iPnt.y;
				vCoor[i] = coordinates[i].z - iPnt.z;
				break;
			case 1:
				uCoor[i] = coordinates[i].x - iPnt.x;
				vCoor[i] = coordinates[i].z - iPnt.z;
				break;
			case 2:
				uCoor[i] = coordinates[i].x - iPnt.x;
				vCoor[i] = coordinates[i].y - iPnt.y;
				break;
			}
		}

		// initialize number of crossing, nc.
		nc = 0;
		if (vCoor[0] < 0.0)
			sh = -1;
		else
			sh = 1;
		for (i = 0; i < coordinates.length; i++) {
			j = i + 1;
			if (j == coordinates.length)
				j = 0;
			if (vCoor[j] < 0.0)
				nsh = -1;
			else
				nsh = 1;
			if (sh != nsh) {
				if ((uCoor[i] > 0.0) && (uCoor[j] > 0.0)) {
					// This line must cross U+.
					nc++;
				} else if ((uCoor[i] > 0.0) || (uCoor[j] > 0.0)) {
					// This line might cross U+. We need to compute intersection
					// on U azis.
					tempD = uCoor[i] - vCoor[i] * (uCoor[j] - uCoor[i])
							/ (vCoor[j] - vCoor[i]);
					if (tempD > 0)
						// This line cross U+.
						nc++;
				}
				sh = nsh;
			} // sh != nsh
		}

		if ((nc % 2) == 1) {
			// calculate the distance
			dist[0] *= direction.length();

			// System.out.println("Segment Intersected!");
			/*
			 * System.out.println("Segment orgin : " + start + " dir " +
			 * direction); System.out.println("Triangle/Quad :"); for(i=0;
			 * i<coordinates.length; i++) System.out.println("P" + i + " " +
			 * coordinates[i]); System.out.println("dist " + dist[0] +
			 * " iPnt : " + iPnt);
			 */
			float[] arr = new float[3];
			arr[0] = iPnt.x;
			arr[1] = iPnt.y;
			arr[2] = iPnt.z;
			return arr;
		} else {
			// System.out.println("Segment Not Intersected!");
			return null;
		}
	}

	public static float[] PL3T(_Point p0, _Point p1, _Point p2) {
		float[] p = new float[4];
		float a1 = (p1.y - p0.y) * (p2.z - p0.z);
		float a2 = (p1.z - p0.z) * (p2.x - p0.x);
		float a3 = (p1.x - p0.x) * (p2.y - p0.y);
		float a4 = (p1.y - p0.y) * (p2.x - p0.x);
		float a5 = (p1.z - p0.z) * (p2.y - p0.y);
		float a6 = (p1.x - p0.x) * (p2.z - p0.z);
		p[0] = a1 - a5;
		p[1] = a2 - a6;
		p[2] = a3 - a4;
		p[3] = p0.y * a6 + p0.x * a5 + p0.z * a4 - p0.z * a3 - p0.y * a2 - p0.x
				* a1;
		return p;
	}

	public static float[] PL3T_LINE(_Point p1, _Point p2, _Point p3, _Point t1,
			_Point t2) {
		float[] ABCD = PL3T(p1, p2, p3);
		float m = t2.x - t1.x;
		float n = t2.y - t1.y;
		float p = t2.z - t1.z;
		if (ABCD[0] * m + ABCD[1] * n + ABCD[2] * p == 0
				|| java.lang.Float.isInfinite(ABCD[0] * m + ABCD[1] * n
						+ ABCD[2] * p))
			return null;
		float t = (-ABCD[0] * t1.x - ABCD[1] * t1.y - ABCD[2] * t1.z - ABCD[3])
				/ (ABCD[0] * m + ABCD[1] * n + ABCD[2] * p);
		float[] xyz = new float[3];
		xyz[0] = t1.x + m * t;
		xyz[1] = t1.y + n * t;
		xyz[2] = t1.z + p * t;
		return xyz;
	}

	public static boolean compare_double(double d1, double d2, double e) {
		double min = Math.min(d1, d2);
		double max = Math.max(d1, d2);
		return (max - min) / max <= e;
	}

	public static boolean compare_points(_Point p1, _Point p2, double e) {
		return (p1.x == p2.x && p1.y == p2.y && p1.z == p2.z)
				|| (compare_double(p1.x, p2.x, e)
						&& compare_double(p1.y, p2.y, e) && compare_double(
							p1.z, p2.z, e));
	}

	public static double distance(_Node n1, _Node n2) {
		return Math.sqrt(Math.pow(n1.x - n2.x, 2) + Math.pow(n1.y - n2.y, 2)
				+ Math.pow(n1.z - n2.z, 2));
	}

	// ***************************************************************
	// Various Nurb methods
	// ***************************************************************

	/**
	 * Generates a _Point on a NURBS surface determined by the internal curve
	 * coordinates u & v
	 * 
	 * @param n
	 *            Number of control points in U direction
	 * @param p
	 *            Curve segment degree in U direction
	 * @param U
	 *            Knot vector U
	 * @param m
	 *            Number of control points in V direction
	 * @param q
	 *            Curve segment degree in V direction
	 * @param V
	 *            Knot vector V
	 * @param Pw
	 *            Array of Control points (weighted values used)
	 * @param u
	 *            Internal surface coordinate for the generated point in u
	 *            direction
	 * @param v
	 *            Internal surface coordinate for the generated point in v
	 *            direction
	 * @return A _Point in 3D space on the surface
	 */

	public static _Point surfacePoint(int n, int p, float[] U, int m, int q,
			float[] V, _CtrlPoint[][] Pw, float u, float v) {
		int uspan, vspan, k, l;
		float[] Nu = new float[p + 1];
		float[] Nv = new float[q + 1];
		_CtrlPoint[] temp = new _CtrlPoint[q + 1];
		_CtrlPoint Sw;
		_Point S;

		uspan = findSpan(n - 1, p, u, U);
		Nu = basisFuns(uspan, u, p, U);
		vspan = findSpan(m - 1, q, v, V);
		Nv = basisFuns(vspan, v, q, V);

		for (l = 0; l <= q; l++) {

			temp[l] = new _CtrlPoint(0, 0, 0, 0, Color.BLUE);

			for (k = 0; k <= p; k++) {
				temp[l].x += Nu[k] * Pw[uspan - p + k][vspan - q + l].x;
				temp[l].y += Nu[k] * Pw[uspan - p + k][vspan - q + l].y;
				temp[l].z += Nu[k] * Pw[uspan - p + k][vspan - q + l].z;
				temp[l].w += Nu[k] * Pw[uspan - p + k][vspan - q + l].w;
			}

		}

		// Project to 3D view

		Sw = new _CtrlPoint(0, 0, 0, 0, Color.BLUE);

		for (l = 0; l <= q; l++) {
			Sw.x += Nv[l] * temp[l].x;
			Sw.y += Nv[l] * temp[l].y;
			Sw.z += Nv[l] * temp[l].z;
			Sw.w += Nv[l] * temp[l].w;
		}

		S = new _Point(false);

		S.x = Sw.x / Sw.w;
		S.y = Sw.y / Sw.w;
		S.z = Sw.z / Sw.w;

		return S;
	}

	/**
	 * Generates a _Point in space set by the internal curve coordinate u
	 * 
	 * @param n
	 *            Number of control points
	 * @param p
	 *            Curve segment degree
	 * @param U
	 *            Knot vector
	 * @param Pw
	 *            Vector of Control points (weighted values used)
	 * @param u
	 *            Internal curve coordinate for the generated point
	 * @return A _Point in 3D space on the curve
	 */

	public static _Point curvePoint(int n, int p, float[] U, _CtrlPoint[] Pw,
			float u) {
		int span, i;
		float[] N = new float[p + 1];
		_Point C = new _Point(false);
		float Cw = 0;

		span = findSpan(n - 1, p, u, U);
		N = basisFuns(span, u, p, U);

		for (i = 0; i <= p; i++) {
			C.x += N[i] * Pw[span - p + i].x;
			C.y += N[i] * Pw[span - p + i].y;
			C.z += N[i] * Pw[span - p + i].z;
			Cw += N[i] * Pw[span - p + i].w;
		}

		// Project to 3D view
		C.x = C.x / Cw;
		C.y = C.y / Cw;
		C.z = C.z / Cw;

		return C;
	}

	/**
	 * This method determines the knot span index and returns it
	 * 
	 * @param n
	 *            set to nr_of_controlpoints -1
	 * @param p
	 *            Curve degree
	 * @param u
	 *            position on Nurb (usually between 0 and 1)
	 * @param U
	 *            Knot vector
	 * @return i Knot vector index for u
	 */
	public static int findSpan(int n, int p, float u, float[] U) {
		int low, high, mid;

		if (u == U[n + 1])
			return (n);
		low = p;
		high = n + 1;
		mid = (int) (low + high) / 2;
		while (u < U[mid] || u >= U[mid + 1]) {
			if (u < U[mid])
				high = mid;
			else
				low = mid;
			mid = (int) (low + high) / 2;
		}
		return mid;
	}

	/**
	 * This method computes the non-vanishing basis functions
	 * 
	 * @param i
	 *            Function index
	 * @param u
	 *            Curve internal coordinate
	 * @param p
	 *            Curve degree
	 * @param U
	 *            Knot vector
	 * @return Basis function array
	 */
	public static float[] basisFuns(int i, float u, int p, float[] U) {
		int j, r;
		float[] left = new float[p + 1];
		float[] right = new float[p + 1];
		float saved, temp;
		float[] N = new float[p + 1];

		N[0] = 1.0f;
		for (j = 1; j <= p; j++) {
			left[j] = u - U[i + 1 - j];
			right[j] = U[i + j] - u;
			saved = 0.0f;
			for (r = 0; r < j; r++) {
				temp = N[r] / (right[r + 1] + left[j - r]);
				N[r] = saved + right[r + 1] * temp;
				saved = left[j - r] * temp;
			}
			N[j] = saved;
		}

		return N;
	}

	/**
	 * This function calculates all non-zero basis functions and their
	 * derivatives
	 * 
	 * @param i
	 *            Basis function index
	 * @param u
	 *            Point internal u coordinate
	 * @param p
	 *            Curve degree
	 * @param n
	 *            number of controlpoints
	 * @param U
	 *            Knot vector
	 * @return ders basis functions
	 */
	public static float[][] dersBasisFuns(int i, float u, int p, int n,
			float[] U) {
		int j, k, r, s1, s2, rk, pk, j1, j2;

		float[][] ndu = new float[p + 1][];
		for (j = 0; j <= p; j++)
			ndu[j] = new float[p + 1];

		float[][] ders = new float[p + 1][];
		for (j = 0; j <= n; j++)
			ders[j] = new float[p + 1];

		float[] left = new float[p + 1];
		float[] right = new float[p + 1];
		float saved, temp, d;

		float[][] a = new float[p + 1][];
		for (j = 0; j < n + 1; j++)
			a[j] = new float[n + 1];

		ndu[0][0] = 1.0f;
		for (j = 1; j <= p; j++) {
			left[j] = u - U[i + 1 - j];
			right[j] = U[i + j] - u;
			saved = 0.0f;

			for (r = 0; r < j; r++) {
				// lower triangle
				ndu[j][r] = right[r + 1] + left[j - r];
				temp = ndu[r][j - 1] / ndu[j][r];
				// upper triangle
				ndu[r][j] = saved + right[r + 1] * temp;
				saved = left[j - r] * temp;
			}

			ndu[j][j] = saved;

		}

		// Load the basis functions
		for (j = 0; j <= p; j++)
			ders[0][j] = ndu[j][p];

		// This section computes the derivatives (Eq. [2.9])
		for (r = 0; r <= p; r++) {
			s1 = 0;
			s2 = 1;
			a[0][0] = 1.0f;

			// Loop to compute kth derivative
			for (k = 1; k <= n; k++) {
				d = 0.0f;
				rk = r - k;
				pk = p - k;

				if (r >= k) {
					a[s2][0] = a[s1][0] / ndu[pk + 1][rk];
					d = a[s2][0] * ndu[rk][pk];
				}

				if (rk >= -1)
					j1 = 1;
				else
					j1 = -rk;

				if (r - 1 <= pk)
					j2 = k - 1;
				else
					j2 = p - r;

				for (j = j1; j <= j2; j++) {
					a[s2][j] = (a[s1][j] - a[s1][j - 1]) / ndu[pk + 1][rk + j];
					d += a[s2][j] * ndu[rk + j][pk];
				}

				if (r <= pk) {
					a[s2][k] = -a[s1][k - 1] / ndu[pk + 1][r];
					d += a[s2][k] * ndu[r][pk];
				}

				ders[k][r] = d;

				// Switch rows
				j = s1;
				s1 = s2;
				s2 = j;

			}
		}

		// Multiply with the correct factors (eq [2.9])
		r = p;
		for (k = 1; k <= n; k++) {
			for (j = 0; j <= p; j++)
				ders[k][j] *= r;
			r *= (p - k);
		}

		return ders;
	}

	/**
	 * This function computes the derivatives for a nurb curve. All derivatives
	 * from 0 to d are calculated at point u on the curve. They are delivered as
	 * _CtrlPoints.
	 * 
	 * @param n
	 *            Number of controlpoints
	 * @param p
	 *            Curve segment degree (order)
	 * @param U
	 *            knot vector
	 * @param P
	 *            Vector of controlpoints
	 * @param u
	 *            position on curve which to calculate derivatives (usually 0 <=
	 *            u <= 1)
	 * @param d
	 *            highest derivative to calculate
	 * @return An array of derivatives from 0 to d, at point u
	 */

	public static _CtrlPoint[] curveDerivsAlg1(int n, int p, float[] U,
			_CtrlPoint[] P, float u, int d) {
		int du, k, span, j;
		_CtrlPoint[] CK;
		float[][] nders;

		CK = new _CtrlPoint[d + 1];

		for (k = p + 1; k <= d; k++) {
			CK[k] = new _CtrlPoint();
			CK[k].w = 0;
		}

		du = Math.min(d, p);
		span = findSpan(n - 1, p, u, U);
		nders = dersBasisFuns(span, u, p, du, U);

		for (k = 0; k <= du; k++) {
			CK[k] = new _CtrlPoint();
			CK[k].w = 0.0f;

			for (j = 0; j <= p; j++) {
				CK[k].x = CK[k].x + nders[k][j] * P[span - p + j].x;
				CK[k].y = CK[k].y + nders[k][j] * P[span - p + j].y;
				CK[k].z = CK[k].z + nders[k][j] * P[span - p + j].z;
				CK[k].w = CK[k].w + nders[k][j] * P[span - p + j].w;
			}
		}

		return CK;
	}

	/**
	 * Calculates the curve derivative for a rational B-spline as an array of
	 * _CtrlPoints where [0] is the curve point in space, [1] is the first
	 * derivative at that point and so on up to [d]
	 * 
	 * @param CKa
	 *            Derivatives for a normal B-spline as given by
	 *            curveDerivsAlg1()
	 * @param d
	 *            Max number of the derivative requested
	 * @return An array of _Points with derivative at point u in normal space
	 */

	public static _Point[] ratCurveDerivs(_CtrlPoint[] CKa, int d) {
		int k, i;
		float vx, vy, vz;
		_Point[] CK;

		CK = new _Point[d + 1];
		for (i = 0; i < d + 1; i++)
			CK[i] = new _Point(false);

		for (k = 0; k <= d; k++) {
			vx = CKa[k].x;
			vy = CKa[k].y;
			vz = CKa[k].z;

			for (i = 1; i <= k; i++) {
				vx = vx - bin(k, i) * CKa[i].w * CK[k - i].x;
				vy = vy - bin(k, i) * CKa[i].w * CK[k - i].y;
				vz = vz - bin(k, i) * CKa[i].w * CK[k - i].z;
			}

			// Project to 3D space
			CK[k].x = vx / CKa[0].w;
			CK[k].y = vy / CKa[0].w;
			CK[k].z = vz / CKa[0].w;
		}

		return CK;
	}

	// Compute logarithm of the gamma function
	// Algorithm from 'Numerical Recipes in C, 2nd Edition' pg214.
	private static float _gammaln(float xx) {
		float x, y, tmp, ser;
		final float[] cof = { 76.18009172947146f, -86.50532032291677f,
				24.01409824083091f, -1.231739572450155f,
				0.12086650973866179e-2f, -0.5395239384953e-5f };
		int j;
		y = x = xx;
		tmp = x + 5.5f;
		tmp -= (x + 0.5f) * Math.log(tmp);
		ser = 1.000000000190015f;
		for (j = 0; j <= 5; j++)
			ser += cof[j] / ++y;
		return -tmp + (float) Math.log(2.5066282746310005f * ser / x);
	}

	// computes ln(n!)
	// Numerical Recipes in C
	// Algorithm from 'Numerical Recipes in C, 2nd Edition' pg215.
	private static float _factln(int n) {
		int ntop = 0;
		float[] a = new float[101];

		if (n <= 1)
			return 0.0f;
		while (n > ntop) {
			++ntop;
			a[ntop] = _gammaln(ntop + 1.0f);
		}
		return a[n];
	}

	/**
	 * This method calculates the binomial of (n over k)
	 * 
	 * @param n
	 * @param k
	 * @return The binomial
	 */
	private static float bin(int n, int k) {
		return (float) Math.floor(0.5 + Math.exp(_factln(n) - _factln(k)
				- _factln(n - k)));
	}

	/**
	 * Projects a point onto a line (vector) and returns the projected position
	 * 
	 * @param start
	 *            Start point of vector
	 * @param direction
	 *            The vector
	 * @param point
	 *            The point to be projected
	 * @return The projected point
	 */
	public static Vector3D pointToLine(Vector3D start, Vector3D direction,
			Vector3D point) {
		Vector3D result = new Vector3D();
		float a;

		result.sub(point, start);
		a = Vector3D.dot(result, direction);

		result.copy(direction);
		result.scale(a);
		result.add(start, result);

		return result;
	}

	// *******************************************************************************
	// Other functions which perform various operations on Nurb curves and
	// surfaces *
	// *******************************************************************************

	/**
	 * Global curve interpolation of controlpoints in order to make the curve
	 * pass through given points
	 * 
	 * @param Q
	 *            Points curve should be fitted to
	 * @param p
	 *            Curve degree
	 * @param U
	 *            Resulting knot vector
	 * @param P
	 *            Resulting control point vector
	 */

	public static void globalCurveInterp(_Point[] Q, int p, float[] U,
			_CtrlPoint[] P) {
		int n = Q.length - 1; // Number of points
		int m = n + p + 1; // number of knots
		float d = 0; // total chord length
		float[] uk = new float[n + 1];
		float[][] A = new float[n + 1][n + 1];
		Matrix M, Minv;
		float[] tmp;

		if (P.length != Q.length)
			throw new IllegalArgumentException(
					"Points and Control Points must be of equal number");

		// Compute the uk evaluation point array
		for (int k = 1; k <= n; k++)
			d += Q[k].distance(Q[k - 1]);

		uk[0] = 0f;
		uk[n] = 1f;

		for (int k = 1; k < n; k++)
			uk[k] = uk[k - 1] + Q[k].distance(Q[k - 1]) / d;

		// Compute the knot vector U through averaging
		for (int i = 0; i <= p; i++)
			U[i] = 0f;
		for (int i = m - p; i <= m; i++)
			U[i] = 1f;
		for (int j = 1; j <= n - p; j++) {
			float sum = 0f;
			for (int i = j; i <= j + p - 1; i++)
				sum += uk[i];
			U[j + p] = (1f / p) * sum;
		}

		// Set up coefficient matrix
		for (int i = 0; i <= n; i++) {
			int span = findSpan(n, p, uk[i], U);
			tmp = basisFuns(span, uk[i], p, U);
			for (int j = 0; j < tmp.length; j++)
				A[i][span - p + j] = tmp[j];
		}

		M = new Matrix(A);

		// Solve the equation
		Minv = M.inverse();

		// Generate control points
		for (int i = 0; i <= n; i++) {
			P[i].x = 0f;
			P[i].y = 0f;
			P[i].z = 0f;
			for (int j = 0; j <= n; j++) {
				P[i].x += Minv.get(i, j) * Q[j].x;
				P[i].y += Minv.get(i, j) * Q[j].y;
				P[i].z += Minv.get(i, j) * Q[j].z;
			}
		}

		// Control points P automatically updated. Nothing to return
	}

	/**
	 * Creates one or more points which is the normal projection of the
	 * p_original point onto the c_target curve
	 * 
	 * @param p_original
	 *            Point to be projected
	 * @param c_target
	 *            Curve the point is to be projected onto
	 * @return The projected point or points
	 */
	public static _Point[] projectPointOntoCurve(_Point p_original,
			_NurbCurve c_target, float gtol) {
		float[] u = { 0.5f, 0.0f };
		float[] du = { 0.1f, 0.1f };
		_Point[] p;
		_Point p1, p2;

		DirectedDistancePointToCurveFunction distfunction;
		DistancePointToCurveFunction distfunction2;

		float eps = gtol * gtol;
		int maxiter = 1000000;

		if (p_original.vx != 0.0f || p_original.vy != 0.0f
				|| p_original.vz != 0.0f) {

			// Project point along the point vector
			distfunction = new DirectedDistancePointToCurveFunction(p_original,
					c_target);

			try {
				ConstrainedDownhillSimplex.minimize(distfunction, u, du, eps,
						maxiter);
			} catch (IllegalStateException e) {
				throw new IllegalStateException();
			}

			p1 = c_target.getPointAt(u[0], true);
			p2 = new _Point(p_original.x + p_original.vx * u[1], p_original.y
					+ p_original.vy * u[1],
					p_original.z + p_original.vz * u[1], p_original.vx,
					p_original.vy, p_original.vz);

			// We return one point on each curve and the middle point as well

			if (p1.distance(p2) < gtol) {
				p = new _Point[1];
				p[0] = p1;
			} else {
				p = new _Point[3];
				p[0] = new _Point(0.5f * (p1.x + p2.x), 0.5f * (p1.y + p2.y),
						0.5f * (p1.z + p2.z));
				p[1] = p1;
				p[2] = p2;
			}

		} else {

			// Project point perpendicular to the curve
			distfunction2 = new DistancePointToCurveFunction(p_original,
					c_target);

			try {
				u[0] = (float) GoldenSection.minimize(0.0, 1.0, distfunction2,
						eps);
			} catch (IllegalStateException e) {
				throw new IllegalStateException();
			}

			// For now, we only return one intersection point
			p = new _Point[1];

			p[0] = c_target.getPointAt(u[0], true);

		}

		return p;
	}

	/**
	 * Creates one or more points which is the normal projection of the
	 * p_original point onto the s_target surface
	 * 
	 * @param p_original
	 *            Point to be projected
	 * @param s_target
	 *            Surface the point is to be projected onto
	 * @return The projected point or points
	 */
	public static _Point[] projectPointOntoSurface(_Point p_original,
			_NurbSurface s_target, float gtol) {
		float[] u = { 0.5f, 0.5f, 0.0f };
		float[] du = { 0.1f, 0.1f, 0.1f };
		_Point[] p;
		_Point p1;

		DirectedDistancePointToSurfaceFunction distfunction;
		DistancePointToSurfaceFunction distfunction2;

		float eps = gtol * gtol;
		int maxiter = 1000000;

		if (p_original.vx != 0.0f || p_original.vy != 0.0f
				|| p_original.vz != 0.0f) {

			// Project point along the point vector
			distfunction = new DirectedDistancePointToSurfaceFunction(
					p_original, s_target);

			try {
				ConstrainedDownhillSimplex.minimize(distfunction, u, du, eps,
						maxiter);
			} catch (IllegalStateException e) {
				throw new IllegalStateException();
			}

		} else {
			// Project point perpendicular to surface
			distfunction2 = new DistancePointToSurfaceFunction(p_original,
					s_target);

			try {
				ConstrainedDownhillSimplex.minimize(distfunction2, u, du, eps,
						maxiter);
			} catch (IllegalStateException e) {
				throw new IllegalStateException();
			}
		}

		p1 = s_target.getPointAt(u[0], u[1]);

		// We return the point on the surface

		p = new _Point[1];
		p[0] = p1;

		return p;
	}

	/**
	 * Creates one or more curves which is the normal projection of the
	 * c_original curve onto the s_target surface
	 * 
	 * @param c_original
	 *            Curve to be projected
	 * @param s_target
	 *            Surface the curve is to be projected onto
	 * @return The projected curve or curves
	 */
	public static _NurbCurve[] projectCurveOntoSurface(_NurbCurve c_original,
			_NurbSurface s_target) {

		_NurbCurve[] new_curve;

		return null;
	}

	/**
	 * Creates two curves which are the result of breaking the c_target curve at
	 * the p_breaker point
	 * 
	 * @param c_target
	 *            Curve to be broken
	 * @param p_breaker
	 *            Point (residing on curve) where the curve is to be broken
	 * @return Resulting curves after the curve has been broken
	 */

	public static _NurbCurve[] breakCurve(_NurbCurve c_target,
			_Point p_breaker, float gtol) {
		_NurbCurve[] new_curve;
		float eps = gtol * gtol;
		float u, sz, d;
		float[] UQ, UP, UR;
		_CtrlPoint[] Qw, Pw, Rw;
		int p, k, s, uq, type;
		Material mat;

		// Initialize
		p = c_target.order;
		UP = c_target.knots;
		Pw = c_target.controlPoints;
		sz = c_target.mesh_size;
		type = c_target.mesh_type_index;
		d = c_target.diameter;
		mat = c_target.material;

		UQ = new float[0];
		Qw = new _CtrlPoint[0];
		new_curve = new _NurbCurve[2];

		// Determine point of break
		DistancePointToCurveFunction distfunction = new DistancePointToCurveFunction(
				p_breaker, c_target);
		u = (float) GoldenSection.minimize(0.0, 1.0, distfunction, eps);

		// Determine index of knot
		k = findSpan(Pw.length - 1, p, u, UP);

		// Determine the knot multiplicity
		s = 0;
		while (UP.length > (k + s + 1) && UP[k + s + 1] == u)
			s++;

		// Determine broken curve by inserting a point p times
		uq = curveKnotIns(p, UP, Pw, u, k, s, p, UQ, Qw);

		// Create curves
		Rw = new _CtrlPoint[uq + 1];
		for (int i = 0; i < uq + 1; i++)
			Rw[i] = new _CtrlPoint(Qw[i]);

		// Create knots
		UR = new float[k];
		for (int i = 0; i < k; i++)
			UR[i] = UQ[i];

		// Create first curve
		new_curve[0] = new _NurbCurve(Rw, UR, p, sz, type, d, mat, null);

		// Create curves
		Rw = new _CtrlPoint[Qw.length - uq - 1];
		for (int i = uq + 1; i < Qw.length; i++)
			Rw[i - uq - 1] = new _CtrlPoint(Qw[i]);

		// Create knots
		UR = new float[UQ.length - k];
		for (int i = k; i < UQ.length; i++)
			UR[i - k] = UQ[i];

		// Create second curve
		new_curve[1] = new _NurbCurve(Rw, UR, p, sz, type, d, mat, null);

		// Return new curves
		return new_curve;
	}

	/**
	 * Compute new curve from knot insertion
	 * 
	 * @param p
	 *            Curve degree
	 * @param UP
	 *            Knot vector for curve
	 * @param Pw
	 *            Control point vector for curve
	 * @param u
	 *            Parametric coordinate of breaking point
	 * @param k
	 *            Knot index of insertion point
	 * @param s
	 *            Knot multiplicity at k
	 * @param r
	 *            Number of times a knot should be inserted
	 * @param UQ
	 *            Knot vector for new curve
	 * @param Qw
	 *            Control point vector for new curve
	 * @return uq splitpoint index
	 */

	public static int curveKnotIns(int p, float[] UP, _CtrlPoint[] Pw, float u,
			int k, int s, int r, float[] UQ, _CtrlPoint[] Qw) {
		int np = UP.length - 1;
		int mp = np + p + 1;
		int nq = np + r;
		_CtrlPoint[] Rw;
		float alpha;
		int L = 0;

		// Define array
		UQ = new float[mp + r + 1];
		Qw = new _CtrlPoint[nq + 1];
		Rw = new _CtrlPoint[p + 1];

		// Load new knot vector
		for (int i = 0; i <= k; i++)
			UQ[i] = UP[i];
		for (int i = 1; i <= r; i++)
			UQ[k + i] = u;
		for (int i = k + 1; i <= mp; i++)
			UQ[i + r] = UP[i];

		// Save unaltered control points
		for (int i = 0; i <= k - p; i++)
			Qw[i] = new _CtrlPoint(Pw[i]);
		for (int i = k - s; i <= np; i++)
			Qw[i + r] = new _CtrlPoint(Pw[i]);
		for (int i = 0; i <= p - s; i++)
			Rw[i] = new _CtrlPoint(Pw[k - p + i]);

		// Insert the knot r times
		for (int j = 1; j <= r; j++) {
			L = k - p + j;
			for (int i = 0; i <= p - j - s; i++) {
				alpha = (u - UP[L + i]) / (UP[i + k + 1] - UP[L + i]);
				Rw[i].x = alpha * Rw[i + 1].x + (1.0f - alpha) * Rw[i].x;
				Rw[i].y = alpha * Rw[i + 1].y + (1.0f - alpha) * Rw[i].y;
				Rw[i].z = alpha * Rw[i + 1].z + (1.0f - alpha) * Rw[i].z;
				Rw[i].w = alpha * Rw[i + 1].w + (1.0f - alpha) * Rw[i].w;
			}
			Qw[L] = new _CtrlPoint(Rw[0]);
			Qw[k + r - j - s] = new _CtrlPoint(Rw[p - j - s]);
		}

		// Load remaining control points
		for (int i = L + 1; i < k - s; i++)
			Qw[i] = new _CtrlPoint(Rw[i - L]);

		// Return split index
		return k - p + 1;
	}

	/**
	 * Creates a curve which is the result of joining the connected c_original
	 * curves
	 * 
	 * @param c_original
	 *            Curves to be joined
	 * @return Resulting curve after joining
	 */
	public static _NurbCurve joinCurves(_NurbCurve[] c_original) {

		_NurbCurve new_curve;

		return null;
	}

	/**
	 * Creates one or more points at the intersection points between two
	 * c_original curves
	 * 
	 * @param c1
	 *            Curve to be intersected
	 * @param c2
	 *            Second curve to be intersected
	 * @return Resulting point or points at intersection. Alternatively the
	 *         point at minimum distance
	 */
	public static _Point[] intersectCurves(_NurbCurve c1, _NurbCurve c2,
			float gtol) throws IllegalStateException {
		float[] u = { 0.5f, 0.5f };
		float[] du = { 0.1f, 0.1f };
		_Point p1, p2;
		_Point[] p;

		DistanceCurveToCurveFunction distfunction;
		float eps = gtol * gtol;
		int maxiter = 100000;

		// Initialize
		distfunction = new DistanceCurveToCurveFunction(c1, c2);

		try {
			ConstrainedDownhillSimplex.minimize(distfunction, u, du, eps,
					maxiter);
		} catch (IllegalStateException e) {
			throw new IllegalStateException();
		}

		p1 = c1.getPointAt(u[0], true);
		p2 = c2.getPointAt(u[1], true);

		// We return one point on each curve and the middle point as well

		if (p1.distance(p2) < gtol) {
			p = new _Point[2];
			p[0] = p1;
			p[1] = p2;

			return p;
		} else {
			p = new _Point[3];
			p[0] = new _Point(0.5f * (p1.x + p2.x), 0.5f * (p1.y + p2.y),
					0.5f * (p1.z + p2.z));
			p[1] = p1;
			p[2] = p2;

			return p;
		}
	}

	/**
	 * Creates one or more curves at the intersection of two s_original surfaces
	 * 
	 * @param s1
	 *            , s2 Surfaces to be intersected (always 2)
	 * @return Resulting curves after intersection
	 */
	public static _NurbCurve[] intersectSurfaces(_NurbSurface s1,
			_NurbSurface s2) throws IllegalStateException {

		return null;
	}

	/**
	 * Creates one or more points at the intersection of c_original curve to the
	 * s_original surface
	 * 
	 * @param c_original
	 *            Curve to be intersected with surface
	 * @param s_original
	 *            Surface to be intersected with curve
	 * @return Resulting point or points at intersection
	 */

	public static _Point[] intersectCurveWithSurface(_NurbCurve c1,
			_NurbSurface s1, float gtol) throws IllegalStateException {

		float[] u = { 0.5f, 0.5f, 0.5f };
		float[] du = { 0.1f, 0.1f, 0.1f };
		_Point p1, p2;
		_Point[] p;

		DistanceCurveToSurfaceFunction distfunction;
		float eps = gtol * gtol;
		int maxiter = 100000;

		// Initialize
		distfunction = new DistanceCurveToSurfaceFunction(c1, s1);

		try {
			ConstrainedDownhillSimplex.minimize(distfunction, u, du, eps,
					maxiter);
		} catch (IllegalStateException e) {
			throw new IllegalStateException();
		}

		p1 = c1.getPointAt(u[0], false);
		p2 = s1.getPointAt(u[1], u[2]);

		// For now, we only return one intersection point
		p = new _Point[1];

		p[0] = new _Point(0.5f * (p1.x + p2.x), 0.5f * (p1.y + p2.y),
				0.5f * (p1.z + p2.z));

		return p;
	}

	/**
	 * Creates one or more points on the c_target curve, placed at equal
	 * interval Selecting one point will put it in the middle of the curve.
	 * Selecting zero points will give the endpoints of the curve.
	 * 
	 * @param c_target
	 *            Curve on which the points are to be determined
	 * @param nr_of_points
	 *            Amount of points to be determined
	 * @return Resulting points
	 */
	public static _Point[] createSpacedPoints(_NurbCurve c_target,
			int nr_of_points) {

		_Point[] new_point;

		return null;
	}

	/**
	 * Calulates the intersection point of two lines represented by points and
	 * direction vectors
	 * 
	 * @param v_p1
	 *            Point 1
	 * @param v_d1
	 *            Direction vector from this point
	 * @param v_p2
	 *            Point 2
	 * @param v_d2
	 *            Direction vector from this point
	 * @return The intersection point
	 */
	public static Vector3D intersection(Vector3D p1, Vector3D v1, Vector3D p2,
			Vector3D v2) {
		float b, a;
		Vector3D coords = new Vector3D();
		Vector3D check = new Vector3D();

		// Solve in x/y plane
		b = (p2.y * v1.x - p1.y * v1.x - p2.x * v1.y + p1.x * v1.y)
				/ (v2.x * v1.y - v2.y * v1.x);
		// a= (p2.x+b*v2.x-p1.x)/v1.x;

		// If failed, try y/x plane
		if (java.lang.Float.isNaN(b)) {
			b = (p2.z * v1.y - p1.z * v1.y - p2.y * v1.z + p1.y * v1.z)
					/ (v2.y * v1.z - v2.z * v1.y);
			// a= (p2.y+b*v2.y-p1.y)/v1.y;
		}

		// If failed, try z/x plane
		if (java.lang.Float.isNaN(b)) {
			b = (p2.x * v1.z - p1.x * v1.z - p2.z * v1.x + p1.z * v1.x)
					/ (v2.z * v1.x - v2.x * v1.z);
			// a= (p2.z+b*v2.z-p1.z)/v1.z;
		}

		coords.copy(v2);
		coords.scale(b);
		coords.add(p2, coords);

		// Verify and return null if no intersection
		/*
		 * check.copy(v1); check.scale(a); check.add(p1,coords); if (coords.z !=
		 * check.z) return null;
		 */
		// Intersect OK. Return coordinate
		return coords;
	}

	/**
	 * This method returns the shortest distance between a point and a line
	 * 
	 * @param p
	 *            Point
	 * @param s
	 *            Start point of line
	 * @param e
	 *            End point of line
	 * @return Shortest distance from point to line
	 */

	public static float distancePoint2Line(_Point p, _Point s, _Point e) {
		Vector3D vp, vs, ve;

		vp = p.getVector();
		vs = s.getVector();
		ve = e.getVector();

		vp.sub(vp, vs);
		ve.sub(ve, vs);

		vs.cross(vp, ve);

		return vs.getLength() / ve.getLength();
	}

	/**
	 * Merges two knot vectors
	 * 
	 * @param vec1
	 *            First vector to be merged
	 * @param vec2
	 *            Second vector to be merged
	 * @return Merged vector
	 */
	public static float[] mergeKnots(float[] vec1, float[] vec2) {
		Vector vecm = new Vector();
		int j = 0;
		int i = 0;
		float[] f;

		while (i < vec1.length && j < vec2.length) {
			if (vec1[i] < vec2[j]) {
				vecm.add(new java.lang.Float(vec1[i]));
				i++;
			} else if (vec1[i] == vec2[j]) {
				vecm.add(new java.lang.Float(vec1[i]));
				i++;
				j++;
			} else {
				vecm.add(new java.lang.Float(vec2[j]));
				j++;
			}
		}

		f = new float[vecm.size()];
		for (i = 0; i < vecm.size(); i++)
			f[i] = ((java.lang.Float) vecm.elementAt(i)).floatValue();

		return f;
	}

	/**
	 * Singles out the difference between two knot vectors
	 * 
	 * @param vec1
	 *            First vector to be compared
	 * @param vec2
	 *            Second vector to be compared
	 * @return The values not included in vec2 but present in vec1. Usually,
	 *         vec2 is elevated by this value.
	 */
	public static float[] diffKnots(float[] vec1, float[] vec2) {
		Vector vecd = new Vector();
		int j = 0;
		int i = 0;
		float[] f;

		while (i < vec1.length && j < vec2.length) {
			if (vec1[i] < vec2[j]) {
				vecd.add(new java.lang.Float(vec1[i]));
				i++;
			} else if (vec1[i] == vec2[j]) {
				i++;
				j++;
			} else {
				j++;
			}
		}

		f = new float[vecd.size()];
		for (i = 0; i < vecd.size(); i++)
			f[i] = ((java.lang.Float) vecd.elementAt(i)).floatValue();

		return f;
	}

	public static void refineKnotVectCurve(int p, float[] U, _CtrlPoint[] Pw,
			float[] X, float[] Ubar, _CtrlPoint[] Qw) {
		int i, j, k, l, ind;
		int n = Pw.length - 1;
		int r = X.length - 1;
		float alfa;

		if (p == 0) {
			for (i = 0; i < U.length; i++)
				Ubar[i] = U[i];
			for (i = 0; i < Pw.length; i++)
				Qw[i] = Pw[i];
			return;
		}

		int m = n + p + 1;
		int a = lib3D.findSpan(n, p, X[0], U);
		int b = lib3D.findSpan(n, p, X[r], U) + 1;

		for (j = 0; j <= a - p; j++)
			Qw[j] = new _CtrlPoint(Pw[j]);
		for (j = b - 1; j <= n; j++)
			Qw[j + r + 1] = new _CtrlPoint(Pw[j]);
		for (j = 0; j <= a; j++)
			Ubar[j] = U[j];
		for (j = b + p; j <= m; j++)
			Ubar[j + r + 1] = U[j];

		i = b + p - 1;
		k = b + p + r;

		for (j = r; j >= 0; j--) {
			while (X[j] <= U[i] && i > a) {
				Qw[k - p - 1] = new _CtrlPoint(Pw[i - p - 1]);
				Ubar[k] = U[i];
				k = k - 1;
				i = i - 1;
			}
			Qw[k - p - 1] = new _CtrlPoint(Qw[k - p]);
			for (l = 1; l <= p; l++) {
				ind = k - p + l;
				alfa = Ubar[k + l] - X[j];
				if (Math.abs(alfa) == 0)
					Qw[ind - 1].setTo(Qw[ind]);
				else {
					alfa = alfa / (Ubar[k + l] - U[i - p + l]);
					Qw[ind - 1].x = alfa * Qw[ind - 1].x + (1.0f - alfa)
							* Qw[ind].x;
					Qw[ind - 1].y = alfa * Qw[ind - 1].y + (1.0f - alfa)
							* Qw[ind].y;
					Qw[ind - 1].z = alfa * Qw[ind - 1].z + (1.0f - alfa)
							* Qw[ind].z;
					Qw[ind - 1].w = alfa * Qw[ind - 1].w + (1.0f - alfa)
							* Qw[ind].w;
				}
			}
			Ubar[k] = X[j];
			k = k - 1;
		}

	}

	public static void refineKnotVectSurface(int p, float[] U,
			_CtrlPoint[][] Pw, float[] X, float[] Ubar, _CtrlPoint[][] Qw) {

		// Assume a U knot refinement.
		// Apply knot vector refinement to all v columns

		_CtrlPoint[] tmp = new _CtrlPoint[Pw.length];
		_CtrlPoint[] qtmp = new _CtrlPoint[Pw.length + X.length]; // dummy
																	// initialization

		for (int v = 0; v < Pw[0].length; v++) {

			for (int i = 0; i < Pw.length; i++)
				tmp[i] = Pw[i][v];

			refineKnotVectCurve(p, U, tmp, X, Ubar, qtmp);

			for (int i = 0; i < qtmp.length; i++)
				Qw[i][v] = qtmp[i];

		}

	}

	public static _CtrlPoint[][] swapUV(_CtrlPoint[][] P) {

		_CtrlPoint[][] tmp = new _CtrlPoint[P[0].length][P.length];
		for (int i = 0; i < tmp.length; i++)
			tmp[i] = new _CtrlPoint[tmp[0].length];

		for (int i = 0; i < tmp.length; i++)
			for (int j = 0; j < tmp[0].length; j++)
				tmp[i][j] = P[j][i];

		return tmp;

	}

	public static int[] degreeElevateCurve(int p, float[] U, _CtrlPoint[] Pw,
			int t, float[] Uh, _CtrlPoint[] Qw) {
		int i, j, k, mpi, mh, nh, kind, r, a, b, cind, mul, oldr, lbz, rbz, save, s, first, last, kj, tr;
		float inv, ua, ub, numer, den, bet, alf, gam;
		int[] out;

		_CtrlPoint[] bpts = new _CtrlPoint[p + 1];
		_CtrlPoint[] ebpts = new _CtrlPoint[p + t + 1];
		_CtrlPoint[] Nextbpts = new _CtrlPoint[p - 1];
		int n = Pw.length - 1;
		int m = n + p + 1;
		int ph = p + t;
		int ph2 = ph / 2;
		float[][] bezalfs = new float[ph + 1][p + 1];
		for (i = 0; i <= ph; i++)
			bezalfs[i] = new float[p + 1];
		float[] alfs = new float[p - 1];

		// Compute bezalfs coefficients
		bezalfs[0][0] = 1.0f;
		bezalfs[ph][p] = 1.0f;
		for (i = 1; i <= ph2; i++) {
			inv = 1.0f / bin(ph, i);
			mpi = Math.min(p, i);
			for (j = Math.max(0, i - t); j <= mpi; j++)
				bezalfs[i][j] = inv * bin(p, j) * bin(t, i - j);
		}
		for (i = ph2 + 1; i <= ph - 1; i++) {
			mpi = Math.min(p, i);
			for (j = Math.max(0, i - t); j <= mpi; j++)
				bezalfs[i][j] = bezalfs[ph - i][p - j];
		}

		// Initialize other values
		mh = ph;
		kind = ph + 1;
		r = -1;
		a = p;
		b = p + 1;
		cind = 1;
		ua = U[0];
		Qw[0] = new _CtrlPoint(Pw[0]);
		for (i = 0; i <= ph; i++)
			Uh[i] = ua;

		// Initialize first Bezier segment
		for (i = 0; i <= p; i++)
			bpts[i] = new _CtrlPoint(Pw[i]);

		// Main loop
		while (b < m) {
			i = b;
			while (b < m && U[b] == U[b + 1])
				b = b + 1;
			mul = b - i + 1;
			mh = mh + mul + t;
			ub = U[b];
			oldr = r;
			r = p - mul;
			// Insert kont u(b) r times
			if (oldr > 0)
				lbz = (oldr + 2) / 2;
			else
				lbz = 1;
			if (r > 0)
				rbz = ph - (r + 1) / 2;
			else
				rbz = ph;
			if (r > 0) {
				// Insert knot to bezier segment
				numer = ub - ua;
				for (k = p; k > mul; k--)
					alfs[k - mul - 1] = numer / (U[a + k] - ua);
				for (j = 1; j <= r; j++) {
					save = r - j;
					s = mul + j;
					for (k = p; k >= s; k--) {
						bpts[k].x = alfs[k - s] * bpts[k].x
								+ (1.0f - alfs[k - s]) * bpts[k - 1].x;
						bpts[k].y = alfs[k - s] * bpts[k].y
								+ (1.0f - alfs[k - s]) * bpts[k - 1].y;
						bpts[k].z = alfs[k - s] * bpts[k].z
								+ (1.0f - alfs[k - s]) * bpts[k - 1].z;
						bpts[k].w = alfs[k - s] * bpts[k].w
								+ (1.0f - alfs[k - s]) * bpts[k - 1].w;
					}
					Nextbpts[save] = new _CtrlPoint(bpts[p]);
				}
			} // End of insert knot

			for (i = lbz; i <= ph; i++) {
				ebpts[i] = new _CtrlPoint(0, 0, 0);
				ebpts[i].w = 0.0f;
				mpi = Math.min(p, i);
				for (j = Math.max(0, i - t); j <= mpi; j++) {
					ebpts[i].x = ebpts[i].x + bezalfs[i][j] * bpts[j].x;
					ebpts[i].y = ebpts[i].y + bezalfs[i][j] * bpts[j].y;
					ebpts[i].z = ebpts[i].z + bezalfs[i][j] * bpts[j].z;
					ebpts[i].w = ebpts[i].w + bezalfs[i][j] * bpts[j].w;
				}
			} // End of degree elevating Bezier

			if (oldr > 1) {
				// Must remove knot u = U[a] oldr times
				first = kind - 2;
				last = kind;
				den = ub - ua;
				bet = (ub - Uh[kind - 1]) / den;
				for (tr = 1; tr < oldr; tr++) {
					// Knot removal loop
					i = first;
					j = last;
					kj = j - kind + 1;
					while (j - i > tr) {
						// Loop and compute the new control points for one
						// removal step
						if (i < cind) {
							alf = (ub - Uh[i]) / (ua - Uh[i]);
							Qw[i].x = alf * Qw[i].x + (1.0f - alf)
									* Qw[i - 1].x;
							Qw[i].y = alf * Qw[i].y + (1.0f - alf)
									* Qw[i - 1].y;
							Qw[i].z = alf * Qw[i].z + (1.0f - alf)
									* Qw[i - 1].z;
							Qw[i].w = alf * Qw[i].w + (1.0f - alf)
									* Qw[i - 1].w;
						}
						if (j >= lbz) {
							if (j - tr <= kind - ph + oldr) {
								gam = (ub - Uh[j - tr]) / den;
								ebpts[kj].x = gam * ebpts[kj].x + (1.0f - gam)
										* ebpts[kj + 1].x;
								ebpts[kj].y = gam * ebpts[kj].y + (1.0f - gam)
										* ebpts[kj + 1].y;
								ebpts[kj].z = gam * ebpts[kj].z + (1.0f - gam)
										* ebpts[kj + 1].z;
								ebpts[kj].w = gam * ebpts[kj].w + (1.0f - gam)
										* ebpts[kj + 1].w;
							} else {
								ebpts[kj].x = bet * ebpts[kj].x + (1.0f - bet)
										* ebpts[kj + 1].x;
								ebpts[kj].y = bet * ebpts[kj].y + (1.0f - bet)
										* ebpts[kj + 1].y;
								ebpts[kj].z = bet * ebpts[kj].z + (1.0f - bet)
										* ebpts[kj + 1].z;
								ebpts[kj].w = bet * ebpts[kj].w + (1.0f - bet)
										* ebpts[kj + 1].w;
							}
						}
						i = i + 1;
						j = j - 1;
						kj = kj - 1;
					}
					first = first - 1;
					last = last + 1;
				}
			} // End of removing knot u = U[a]

			if (a != p)
				for (i = 0; i < ph - oldr; i++) {
					Uh[kind] = ua;
					kind = kind + 1;
				}

			for (j = lbz; j <= rbz; j++) {
				Qw[cind] = new _CtrlPoint(ebpts[j]);
				cind = cind + 1;
			}

			if (b < m) {
				// Set up for next pass though loop
				for (j = 0; j < r; j++)
					bpts[j] = new _CtrlPoint(Nextbpts[j]);
				for (j = r; j <= p; j++)
					bpts[j] = new _CtrlPoint(Pw[b - p + j]);
				a = b;
				b = b + 1;
				ua = ub;
			} else {
				// End knot
				for (i = 0; i <= ph; i++)
					Uh[kind + i] = ub;
			}
		} // End of while loop (b < m)

		nh = mh - ph - 1;

		// Return knot array length, control point array length and new degree
		out = new int[3];
		out[0] = mh + 1;
		out[1] = nh + 1;
		out[2] = ph;
		return out;
	}

	public static boolean isClose(_Point a, _Point b, float mtol) {
		float d;

		d = (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y) + (a.z - b.z)
				* (a.z - b.z);

		return (d < mtol);
	}

	public static boolean contains(_Object[] a, _Object b) {
		boolean contains = false;

		if (a == null || b == null)
			return false;

		for (int i = 0; i < a.length; i++)
			if (a[i] != null && b != null && a[i].equals(b)) {
				contains = true;
				break;
			}

		return contains;
	}

}
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

package j3d.functions;

import j3d._Point;
import j3d._Spline;
import util.VectorFunction;

/**
 * @author Jonas Forssell
 * 
 * 
 */
public class FitCurveToPointsFunction implements VectorFunction {

	_Spline s;
	_Point p, q;
	double dist;

	public FitCurveToPointsFunction(_Spline s) {
		this.s = s;
	}

	/**
	 * Compute the distance between two nurb curves at given parameter value. If
	 * the value is out of bounds, a large value is returned to create a border.
	 * 
	 * @param x
	 *            parameter input vector. Always size 2 (two curves).
	 * 
	 * @return Distance squared between the curves at given points.
	 */
	public float f(float[] x) {
		int l = s.controlPoints.length;

		// Update the control point position exept end points
		for (int i = 1; i < l - 1; i++) {
			s.controlPoints[i].x = x[i - 1]
					* ((_Point) s.points.elementAt(i)).x;
			s.controlPoints[i].y = x[i - 1]
					* ((_Point) s.points.elementAt(i)).y;
			s.controlPoints[i].z = x[i - 1]
					* ((_Point) s.points.elementAt(i)).z;
		}

		dist = 0;

		// calculate the distance
		for (int i = 0; i < l; i++) {
			p = s.getPointAt((float) (i + 1) / (float) (l + 1), false);
			q = (_Point) s.points.elementAt(i);

			dist += (p.x - q.x) * (p.x - q.x) + (p.y - q.y) * (p.y - q.y)
					+ (p.z - q.z) * (p.z - q.z);
		}

		return (float) dist;
	}

}

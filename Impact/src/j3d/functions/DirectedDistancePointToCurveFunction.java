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

import j3d._NurbCurve;
import j3d._Point;
import util.VectorFunction;

/**
 * @author Jonas Forssell
 * 
 * 
 */
public class DirectedDistancePointToCurveFunction implements VectorFunction {

	_NurbCurve c1;
	_Point p1, p2, p;
	float dist;

	public DirectedDistancePointToCurveFunction(_Point p, _NurbCurve c1) {
		this.p = p;
		this.c1 = c1;
		p2 = new _Point(0, 0, 0);
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

		if (x[0] < 0 || x[0] > 1)
			return 1E9f;

		p1 = c1.getPointAt(x[0], false);

		p2.x = p.x + p.vx * x[1];
		p2.y = p.y + p.vy * x[1];
		p2.z = p.z + p.vz * x[1];

		return (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y)
				+ (p1.z - p2.z) * (p1.z - p2.z);

	}

}

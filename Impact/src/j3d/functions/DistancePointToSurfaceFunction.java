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

import j3d._NurbSurface;
import j3d._Point;
import util.VectorFunction;

/**
 * @author Jonas Forssell
 * 
 * 
 */
public class DistancePointToSurfaceFunction implements VectorFunction {

	_NurbSurface s1;
	_Point pp, p1;
	double dist;

	public DistancePointToSurfaceFunction(_Point pp, _NurbSurface s1) {
		this.pp = pp;
		this.s1 = s1;
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
		if (x[1] < 0 || x[1] > 1)
			return 1E9f;

		p1 = s1.getPointAt(x[0], x[1]);

		return (p1.x - pp.x) * (p1.x - pp.x) + (p1.y - pp.y) * (p1.y - pp.y)
				+ (p1.z - pp.z) * (p1.z - pp.z);

	}

}

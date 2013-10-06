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

import java.awt.geom.Point2D;

public class Circle {

	double r, q;
	Point2D.Double c;

	Circle() {
		c = new Point2D.Double();
		r = 0.0;
		q = 0.0;
	}

	public void circumCircle(Point2D p1, Point2D p2, Point2D p3) {
		// redefine the circumCircle to pass through the three points

		double cp, p1Sq, p2Sq, p3Sq, num, cx, cy;
		;

		cp = lib2D.crossProduct(p1, p2, p3);

		if (Math.abs(cp) > 1E-9) {
			p1Sq = p1.getX() * p1.getX() + p1.getY() * p1.getY();
			p2Sq = p2.getX() * p2.getX() + p2.getY() * p2.getY();
			p3Sq = p3.getX() * p3.getX() + p3.getY() * p3.getY();

			num = p1Sq * (p2.getY() - p3.getY()) + p2Sq
					* (p3.getY() - p1.getY()) + p3Sq * (p1.getY() - p2.getY());
			cx = num / (2.0 * cp);

			num = p1Sq * (p3.getX() - p2.getX()) + p2Sq
					* (p1.getX() - p3.getX()) + p3Sq * (p2.getX() - p1.getX());
			cy = num / (2.0 * cp);

			c.setLocation(cx, cy);
		}

		// Radius
		r = c.distance(p1);

	}

	public boolean inside(Point2D p) {
		// Check if specified node is inside the circumcircle

		if (c.distanceSq(p) < r * r)
			return true;
		else
			return false;
	}

	public boolean isWithinLimits() {
		// Check if specified node is inside the circumcircle

		if (c.x >= 0 && c.x <= 1 && c.y >= 0 && c.y <= 1)
			return true;
		else
			return false;
	}

	/**
	 * Calculates the quota between actual radius and wanted radius
	 * 
	 * @param qa
	 * @param qb
	 * @param qc
	 * @param qd
	 */

	public void calculateQuota(float qa, float qb, float qc, float qd) {

		q = r / (0.5 * (qa + c.x * (qc - qa) + qd + c.y * (qb - qd)));

	}

}

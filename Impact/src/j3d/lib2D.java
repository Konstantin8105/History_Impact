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

import java.awt.geom.*;

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy
 */

public class lib2D {

	public static double angle(double x1, double y1, double x2, double y2) {
		return angle(new Point2D.Double(x1, y1), new Point2D.Double(x2, y2));
	}

	public static double angle(Point2D p1, Point2D p2) {
		double a = Math.acos((p2.getX() - p1.getX()) / p1.distance(p2)) * 180
				/ Math.PI;
		if (p1.getY() > p2.getY())
			a = 360 - a;
		return a;
	}

	public static Point2D centerL(double x1, double y1, double x2, double y2) {
		return new Point2D.Double((x1 + x2) / 2, (y1 + y2) / 2);
	}

	public static Point2D intersectionLL(Point2D p11, Point2D p12, Point2D p21,
			Point2D p22, boolean contains) {
		return intersectionLL(p11.getX(), p11.getY(), p12.getX(), p12.getY(),
				p21.getX(), p21.getY(), p22.getX(), p22.getY(), contains);
	}

	public static Point2D intersectionLL(double x11, double y11, double x12,
			double y12, double x21, double y21, double x22, double y22,
			boolean contains) {
		try {
			double k1 = 0, k2 = 0, b1 = 0, b2 = 0, y, x;
			if (y11 == y12) {
				k1 = 0;
				b1 = y11;
			} else if (x11 != x12) {
				k1 = (y12 - y11) / (x12 - x11);
				b1 = y11 - x11 * k1;
			}
			if (y21 == y22) {
				k2 = 0;
				b2 = y21;
			} else if (x21 != x22) {
				k2 = (y22 - y21) / (x22 - x21);
				b2 = y21 - x21 * k2;
			}
			if (x11 == x12) {
				x = x11;
				y = k2 * x + b2;
			} else if (x21 == x22) {
				x = x22;
				y = k1 * x + b1;
			} else {
				x = (b2 - b1) / (k1 - k2);
				y = k2 * x + b2;
			}
			if ((x11 == x12 && x21 == x22) || java.lang.Double.isNaN(x)
					|| java.lang.Double.isNaN(y)
					|| java.lang.Double.isInfinite(x)
					|| java.lang.Double.isInfinite(y))
				return null;
			double minx1 = Math.min(x11, x12);
			double maxx1 = Math.max(x11, x12);
			double miny1 = Math.min(y11, y12);
			double maxy1 = Math.max(y11, y12);
			double minx2 = Math.min(x21, x22);
			double maxx2 = Math.max(x21, x22);
			double miny2 = Math.min(y21, y22);
			double maxy2 = Math.max(y21, y22);
			if (contains && minx1 <= x && x <= maxx1 && miny1 <= y
					&& y <= maxy1 && minx2 <= x && x <= maxx2 && miny2 <= y
					&& y <= maxy2) {
				return new Point2D.Double(x, y);
			} else if (!contains) {
				return new Point2D.Double(x, y);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}

	public static Point2D[] intersectionCC(Point2D p1, double r1, Point2D p2,
			double r2) {
		return intersectionCC(p1.getX(), p1.getY(), r1, p2.getX(), p2.getY(),
				r2);
	}

	public static Point2D[] intersectionCC(double x1, double y1, double r1,
			double x2, double y2, double r2) {
		try {
			Point2D[] arr = new Point2D[2];
			double a, b, f, d;
			d = new Point2D.Double(x1, y1).distance(x2, y2);
			if (d > (r1 + r2))
				return null;
			if (x1 <= x2 && y1 <= y2) {
				f = angle(x1, y1, x2, y2);
				a = Math.acos((r1 * r1 + d * d - r2 * r2) / 2 / r1 / d) * 180
						/ Math.PI + f;
				b = Math.acos((r2 * r2 + d * d - r1 * r1) / 2 / r2 / d) * 180
						/ Math.PI + f;
				arr[0] = new Point2D.Double(x1 + r1
						* Math.cos(a / 180 * Math.PI), y1 + r1
						* Math.sin(a / 180 * Math.PI));
				arr[1] = new Point2D.Double(x2 - r2
						* Math.cos(b / 180 * Math.PI), y2 - r2
						* Math.sin(b / 180 * Math.PI));
				return arr;
			} else if (x1 < x2 && y1 > y2) {
				f = 180 - angle(x2, y2, x1, y1);
				a = Math.acos((r1 * r1 + d * d - r2 * r2) / 2 / r1 / d) * 180
						/ Math.PI + f;
				b = Math.acos((r2 * r2 + d * d - r1 * r1) / 2 / r2 / d) * 180
						/ Math.PI + f;
				arr[0] = new Point2D.Double(x2 - r2
						* Math.cos(a / 180 * Math.PI), y2 + r2
						* Math.sin(a / 180 * Math.PI));
				arr[1] = new Point2D.Double(x1 + r1
						* Math.cos(b / 180 * Math.PI), y1 - r1
						* Math.sin(b / 180 * Math.PI));
				return arr;
			} else if (x1 >= x2 && y1 >= y2) {
				f = angle(x2, y2, x1, y1);
				a = Math.acos((r2 * r2 + d * d - r1 * r1) / 2 / r2 / d) * 180
						/ Math.PI + f;
				b = Math.acos((r1 * r1 + d * d - r2 * r1) / 2 / r1 / d) * 180
						/ Math.PI + f;
				arr[0] = new Point2D.Double(x2 + r2
						* Math.cos(a / 180 * Math.PI), y2 + r2
						* Math.sin(a / 180 * Math.PI));
				arr[1] = new Point2D.Double(x1 - r1
						* Math.cos(b / 180 * Math.PI), y1 - r1
						* Math.sin(b / 180 * Math.PI));
				return arr;
			} else if (x1 > x2 && y1 < y2) {
				f = 180 - angle(x1, y1, x2, y2);
				a = Math.acos((r2 * r2 + d * d - r1 * r1) / 2 / r2 / d) * 180
						/ Math.PI + f;
				b = Math.acos((r1 * r1 + d * d - r2 * r2) / 2 / r1 / d) * 180
						/ Math.PI + f;
				arr[0] = new Point2D.Double(x1 - r1
						* Math.cos(b / 180 * Math.PI), y1 + r1
						* Math.sin(b / 180 * Math.PI));
				arr[1] = new Point2D.Double(x2 + r2
						* Math.cos(a / 180 * Math.PI), y2 - r2
						* Math.sin(a / 180 * Math.PI));
				return arr;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}

	public static Point2D[] intersectionCL(Point2D sp, double r1, Point2D p1,
			Point2D p2, boolean contains) {
		return intersectionCL(sp.getX(), sp.getY(), r1, p1.getX(), p1.getY(),
				p2.getX(), p2.getY(), contains);
	}

	public static Point2D[] intersectionCL(double x1, double y1, double r1,
			double x2, double y2, double x3, double y3, boolean contains) {
		try {
			Point2D[] arr;
			double k = 0, b = 0, aa, bb, cc, dd, xx1, xx2;
			if (y2 == y3) {
				k = 0;
				b = y2;
			} else if (x2 != x3) {
				k = (y3 - y2) / (x3 - x2);
				b = y2 - x2 * k;
			}
			if (x2 == x3) {
				if (x1 - r1 > x2 || x2 > x1 + r1)
					return null;
				if (x2 == x1 + r1) {
					arr = new Point2D[1];
					arr[0] = new Point2D.Double(x1 + r1, y1);
					return arr;
				}
				if (x2 == x1 - r1) {
					arr = new Point2D[1];
					arr[0] = new Point2D.Double(x1 - r1, y1);
					return arr;
				}
				aa = 1;
				bb = -2 * y1;
				cc = -y1 * y1 + (x2 - x1) * (x2 - x1) - r1 * r1;
				dd = bb * bb - 4 * aa * cc;
				xx1 = (-bb + Math.sqrt(dd)) / 2 / aa;
				xx2 = (-bb - Math.sqrt(dd)) / 2 / aa;
				Point2D p1, p2;
				if (contains
						&& (Math.min(y2, y3) > xx1 || xx1 > Math.max(y2, y3)))
					p1 = null;
				else
					p1 = new Point2D.Double(x2, xx1);
				if (contains
						&& (Math.min(y2, y3) > xx2 || xx2 > Math.max(y2, y3)))
					p2 = null;
				else
					p2 = new Point2D.Double(x2, xx2);
				if (p1 != null && p2 != null) {
					arr = new Point2D[2];
					arr[0] = p1;
					arr[1] = p2;
					return arr;
				} else if (p1 == null && p2 != null) {
					arr = new Point2D[1];
					arr[0] = p2;
					return arr;
				} else if (p1 != null && p2 == null) {
					arr = new Point2D[1];
					arr[0] = p1;
					return arr;
				} else
					return null;
			}
			aa = 1 + k * k;
			bb = 2 * (-x1 + b * k - y1 * k);
			cc = x1 * x1 + b * b - 2 * y1 * b + y1 * y1 - r1 * r1;
			dd = bb * bb - 4 * aa * cc;
			xx1 = (-bb + Math.sqrt(dd)) / 2 / aa;
			xx2 = (-bb - Math.sqrt(dd)) / 2 / aa;
			if (java.lang.Double.isNaN(xx1) || java.lang.Double.isNaN(xx2)
					|| java.lang.Double.isInfinite(xx1)
					|| java.lang.Double.isInfinite(xx2))
				return null;
			if (dd == 0) {
				if (contains
						&& (Math.min(x2, x3) > xx1 || xx1 > Math.max(x2, x3)))
					return null;
				arr = new Point2D[1];
				arr[0] = new Point2D.Double(xx1, k * xx1 + b);
				return arr;
			} else {
				Point2D p1, p2;
				if (contains
						&& (Math.min(x2, x3) > xx1 || xx1 > Math.max(x2, x3)))
					p1 = null;
				else
					p1 = new Point2D.Double(xx1, k * xx1 + b);
				if (contains
						&& (Math.min(x2, x3) > xx2 || xx2 > Math.max(x2, x3)))
					p2 = null;
				else
					p2 = new Point2D.Double(xx2, k * xx2 + b);
				if (p1 != null && p2 != null) {
					arr = new Point2D[2];
					arr[0] = p1;
					arr[1] = p2;
					return arr;
				} else if (p1 == null && p2 != null) {
					arr = new Point2D[1];
					arr[0] = p2;
					return arr;
				} else if (p1 != null && p2 == null) {
					arr = new Point2D[1];
					arr[0] = p1;
					return arr;
				} else
					return null;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}

	public static Point2D[] intersectionAA(double x1, double y1, double r1,
			double sa1, double ea1, double x2, double y2, double r2,
			double sa2, double ea2, boolean contains) {
		try {
			Point2D[] arr = intersectionCC(x1, y1, r1, x2, y2, r2);
			if (!contains || arr == null)
				return arr;
			Point2D[] arr_new = new Point2D[2];
			double a1 = angle(x1, y1, arr[0].getX(), arr[0].getY());
			double a2 = angle(x2, y2, arr[0].getX(), arr[0].getY());
			if (a1 >= sa1 && a1 <= ea1 && a2 >= sa2 && a2 <= ea2)
				arr_new[0] = arr[0];
			if (arr.length == 1)
				return arr_new;
			a1 = angle(x1, y1, arr[1].getX(), arr[1].getY());
			a2 = angle(x2, y2, arr[1].getX(), arr[1].getY());
			if (a1 >= sa1 && a1 <= ea1 && a2 >= sa2 && a2 <= ea2)
				arr_new[1] = arr[1];
			else {
				Point2D[] arr_new1 = new Point2D[1];
				arr_new1[0] = arr_new[0];
				return arr_new1;
			}
			return arr_new;

		} catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}

	public static Point2D[] intersectionAL(double x1, double y1, double r1,
			double sa1, double ea1, double x2, double y2, double x3, double y3,
			boolean contains) {
		try {
			Point2D[] arr = intersectionCL(x1, y1, r1, x2, y2, x3, y3, contains);
			if (!contains || arr == null)
				return arr;
			Point2D[] arr_new = new Point2D[2];
			double a1 = angle(x1, y1, arr[0].getX(), arr[0].getY());
			if (a1 >= sa1 && a1 <= ea1)
				arr_new[0] = arr[0];
			if (arr.length == 1)
				return arr_new;
			a1 = angle(x1, y1, arr[1].getX(), arr[1].getY());
			if (a1 >= sa1 && a1 <= ea1)
				arr_new[1] = arr[1];
			else {
				Point2D[] arr_new1 = new Point2D[1];
				arr_new1[0] = arr_new[0];
				return arr_new1;
			}
			return arr_new;
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}

	public static Point2D[] intersectionAC(double x1, double y1, double r1,
			double sa1, double ea1, double x2, double y2, double r2,
			boolean contains) {
		return intersectionAA(x1, y1, r1, sa1, ea1, x2, y2, r2, 0, 360,
				contains);
	}

	public static double crossProduct(Point2D p1, Point2D p2, Point2D p3) {
		Point2D.Double v1 = new Point2D.Double(p2.getX() - p1.getX(), p2.getY()
				- p1.getY());
		Point2D.Double v2 = new Point2D.Double(p3.getX() - p1.getX(), p3.getY()
				- p1.getY());

		return v1.getX() * v2.getY() - v2.getX() * v1.getY();
	}

	public static boolean insideCircumCircle(Point2D p, Point2D p1, Point2D p2,
			Point2D p3) {
		// redefine the circumCircle to pass through the three points

		double cp, p1Sq, p2Sq, p3Sq, num, cx, cy, rsq, psq;

		cp = lib2D.crossProduct(p1, p2, p3);

		if (cp != 0) {
			p1Sq = p1.getX() * p1.getX() + p1.getY() * p1.getY();
			p2Sq = p2.getX() * p2.getX() + p2.getY() * p2.getY();
			p3Sq = p3.getX() * p3.getX() + p3.getY() * p3.getY();

			num = p1Sq * (p2.getY() - p3.getY()) + p2Sq
					* (p3.getY() - p1.getY()) + p3Sq * (p1.getY() - p2.getY());
			cx = num / (2.0 * cp);

			num = p1Sq * (p3.getX() - p2.getX()) + p2Sq
					* (p1.getX() - p3.getX()) + p3Sq * (p2.getX() - p1.getX());
			cy = num / (2.0 * cp);
		} else
			return false;

		// Radius
		rsq = (cx - p1.getX()) * (cx - p1.getX()) + (cy - p1.getY())
				* (cy - p1.getY());

		psq = (cx - p.getX()) * (cx - p.getX()) + (cy - p.getY())
				* (cy - p.getY());

		if (psq < rsq)
			return true;
		else
			return false;

	}

	public static boolean circumCircleIsWithinLimits(Point2D p1, Point2D p2,
			Point2D p3) {
		// redefine the circumCircle to pass through the three points

		double cp, p1Sq, p2Sq, p3Sq, num, cx, cy;

		cp = lib2D.crossProduct(p1, p2, p3);

		if (cp != 0) {
			p1Sq = p1.getX() * p1.getX() + p1.getY() * p1.getY();
			p2Sq = p2.getX() * p2.getX() + p2.getY() * p2.getY();
			p3Sq = p3.getX() * p3.getX() + p3.getY() * p3.getY();

			num = p1Sq * (p2.getY() - p3.getY()) + p2Sq
					* (p3.getY() - p1.getY()) + p3Sq * (p1.getY() - p2.getY());
			cx = num / (2.0 * cp);

			num = p1Sq * (p3.getX() - p2.getX()) + p2Sq
					* (p1.getX() - p3.getX()) + p3Sq * (p2.getX() - p1.getX());
			cy = num / (2.0 * cp);
		} else
			return false;

		// Radius

		if (cx >= 0 && cx <= 1 && cy >= 0 && cy <= 1)
			return true;
		else
			return false;

	}

}
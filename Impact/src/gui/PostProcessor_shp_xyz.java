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
package gui;

import javax.media.j3d.*;
import javax.vecmath.*;

/**
 * 
 * @author: Yuriy Mikhaylovskiy
 */
public class PostProcessor_shp_xyz extends Shape3D {
	public PostProcessor_shp_xyz() {
		setCapability(Shape3D.ALLOW_GEOMETRY_READ);
		LineArray gla = new LineArray(6, LineArray.COORDINATES
				| LineArray.COLOR_3);
		gla.setCapability(LineArray.ALLOW_COUNT_READ);
		gla.setCapability(LineArray.ALLOW_FORMAT_READ);
		gla.setCapability(LineArray.ALLOW_COORDINATE_READ);
		Point3d[] MPoint = new Point3d[6];
		Color3f[] colors = new Color3f[6];
		MPoint[0] = new Point3d(0, 0, 0);
		MPoint[1] = new Point3d(50, 0, 0);
		MPoint[2] = new Point3d(0, 0, 0);
		MPoint[3] = new Point3d(0, 50, 0);
		MPoint[4] = new Point3d(0, 0, 0);
		MPoint[5] = new Point3d(0, 0, 50);
		colors[0] = new Color3f(java.awt.Color.red);
		colors[1] = new Color3f(java.awt.Color.red);
		colors[2] = new Color3f(java.awt.Color.green);
		colors[3] = new Color3f(java.awt.Color.green);
		colors[4] = new Color3f(java.awt.Color.BLUE);
		colors[5] = new Color3f(java.awt.Color.BLUE);
		gla.setCoordinates(0, MPoint);
		gla.setColors(0, colors);
		this.setGeometry(gla, 0);
		Appearance app = new Appearance();
		ColoringAttributes ca = new ColoringAttributes();
		ca.setColor(new Color3f(java.awt.Color.blue));
		app.setColoringAttributes(ca);
		LineAttributes la = new LineAttributes();
		la.setLineWidth(2);
		app.setLineAttributes(la);
		this.setAppearance(app);
	}
}
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
import com.sun.j3d.utils.geometry.*;

/**
 * 
 * @author: Yuriy Mikhaylovskiy
 */
public class PostProcessor_shp_oriented_text extends OrientedShape3D {// Shape3D
																		// {
	public PostProcessor_shp_oriented_text(String st, Point3f p, int size) {
		setCapability(Shape3D.ALLOW_GEOMETRY_READ);
		Appearance app = new Appearance();
		ColoringAttributes ca = new ColoringAttributes();
		ca.setColor(new Color3f(java.awt.Color.black));
		app.setColoringAttributes(ca);
		this.setAppearance(app);
		Font3D f3d = new Font3D(new java.awt.Font("serif", java.awt.Font.PLAIN,
				size), null);// new FontExtrusion());
		Text3D txt_x = new Text3D(f3d, st, p);
		txt_x.setCapability(LineArray.ALLOW_COUNT_READ);
		txt_x.setCapability(LineArray.ALLOW_FORMAT_READ);
		txt_x.setCapability(LineArray.ALLOW_COORDINATE_READ);
		this.setAlignmentMode(OrientedShape3D.ROTATE_ABOUT_POINT);
		this.setRotationPoint(p);
		this.setGeometry(txt_x);
	}
}
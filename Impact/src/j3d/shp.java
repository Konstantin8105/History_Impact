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

import java.awt.*;
import java.awt.geom.*;

import javax.media.opengl.GL;

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy
 */

public interface shp {
	public void transform2D(Matrix3D trans, Vector3D cor, Canvas3D J3D);

	public void paint(Graphics2D g, Canvas3D j3d);

	public void paintGL(GL gl, Graphics2D g2D, Canvas3D j3d, float rcx,
			float rcy, float rcz, float scale);

	public float get_Z();

	public boolean isPickPoint(int x, int y, boolean openGL);

	public boolean isPickPoint(Rectangle2D r, boolean openGL);

	public void setPicked(boolean selected);

	public void setShow(boolean s);

	public boolean isShow();

	public float[] getBoundaries();

	public float[] get2DBoundaries();

}
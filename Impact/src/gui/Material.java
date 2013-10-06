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

import java.awt.*;
import java.io.*;

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy
 */

public class Material implements Serializable, Cloneable {
	public Color color;
	public String name;
	public String type;
	public String description;

	public Material() {

	}

	public Material(Material n) {
		this.color = n.color;
		this.name = new String(n.name);
		this.type = new String(n.type);
		this.description = new String(n.description);
	}

	public Material(String n, String t, String d, Color cl) {
		this.color = cl;
		this.name = n;
		this.type = t;
		this.description = d;
	}

}
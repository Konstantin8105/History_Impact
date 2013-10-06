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

package run.elements;

/**
 * @author: Jonas Forssell, Yuriy Mikhaylovskiy.
 */
public class Ldata implements java.io.Serializable {
	public Contact_Line cline;
	public double q;
	public double vec;
	public boolean vec_is_set;
	public boolean checked;

	public Ldata(Contact_Line line, double q) {
		cline = line;
		this.q = q;
		checked = true;
	}

}

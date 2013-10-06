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

package j3d.utilities;

import j3d._Object;

import java.util.Comparator;

public class Comparer implements Comparator {

	/**
	 * @param args
	 */
	public int compare(Object obj1, Object obj2) {
		int i1 = Integer.parseInt(((_Object) obj1).get_Id());
		int i2 = Integer.parseInt(((_Object) obj2).get_Id());

		return Math.abs(i1) - Math.abs(i2);
	}

}

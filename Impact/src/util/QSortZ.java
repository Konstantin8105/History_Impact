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
package util;

import j3d.shp;

import java.util.*;

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy
 */

public class QSortZ {
	private void QuickSort(Vector v, int lo0, int hi0) {
		int lo = lo0;
		int hi = hi0;
		float mid;

		if (hi0 > lo0) {

			/*
			 * Arbitrarily establishing partition element as the midpoint of the
			 * array.
			 */
			mid = ((shp) v.elementAt((lo0 + hi0) / 2)).get_Z();

			// loop through the array until indices cross
			while (lo <= hi) {
				/*
				 * find the first element that is greater than or equal to the
				 * partition element starting from the left Index.
				 */
				while ((lo < hi0) && (((shp) v.elementAt(lo)).get_Z() < mid))
					++lo;

				/*
				 * find an element that is smaller than or equal to the
				 * partition element starting from the right Index.
				 */
				while ((hi > lo0) && (((shp) v.elementAt(hi)).get_Z() > mid))
					--hi;

				// if the indexes have not crossed, swap
				if (lo <= hi) {
					swap(v, lo, hi);
					++lo;
					--hi;
				}
			}

			/*
			 * If the right index has not reached the left side of array must
			 * now sort the left partition.
			 */
			if (lo0 < hi)
				QuickSort(v, lo0, hi);

			/*
			 * If the left index has not reached the right side of array must
			 * now sort the right partition.
			 */
			if (lo < hi0)
				QuickSort(v, lo, hi0);
		}
	}

	private void swap(Vector v, int i, int j) {
		Object T = v.elementAt(i);
		v.setElementAt(v.elementAt(j), i);
		v.setElementAt(T, j);
	}

	public void sort(Vector v) {
		QuickSort(v, 0, v.size() - 1);
	}

}

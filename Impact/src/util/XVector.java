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

import java.io.Serializable;
import java.util.Vector;

/**
 * @author Jonas Forssell
 * 
 *         A simple two dimensional version of java.util.Vector
 * 
 */
public class XVector implements Serializable {

	Vector row, latest;
	int c_row;

	/**
     * 
     */
	public XVector() {
		super();
		// TODO Auto-generated constructor stub
		row = new Vector();
		latest = new Vector();
		row.add(latest);
		c_row = 0;
	}

	public void addRow() {
		latest = new Vector();
		row.add(latest);
		c_row++;
	}

	public void add(Object obj) {
		latest.add(obj);
	}

	public void removeElementAt(int r, int c) {
		((Vector) row.elementAt(r)).removeElementAt(c);
	}

	public Object elementAt(int r, int c) {
		return ((Vector) row.elementAt(r)).elementAt(c);
	}

	public void setVectorAtRow(Vector v, int r) {
		row.setElementAt(v, r);
	}

	public Object[][] toArray() {
		int nr, nc;
		Vector tmp;
		Object[][] out;

		nr = row.size();
		tmp = (Vector) row.elementAt(0);
		nc = tmp.size();

		out = new Object[nr][nc];

		for (int i = 0; i < nr; i++) {
			tmp = (Vector) row.elementAt(i);
			out[i] = tmp.toArray();
		}

		return out;
	}

	public int rowsize() {
		return row.size();
	}

	public int colsize() {
		return ((Vector) row.elementAt(0)).size();
	}
}

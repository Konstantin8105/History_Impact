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
package run;

import java.util.Collection;
import java.util.Vector;

/**
 * @author Jonas Forssell, Yuriy Mikhaylovskiy
 * 
 */
public class RplVector extends Vector implements java.io.Serializable {

	/**
	 * 
	 */
	public RplVector() {
		super();
	}

	/**
	 * @param arg0
	 */
	public RplVector(int arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public RplVector(int arg0, int arg1) {
		super(arg0, arg1);
	}

	/**
	 * @param arg0
	 */
	public RplVector(Collection arg0) {
		super(arg0);
	}
}

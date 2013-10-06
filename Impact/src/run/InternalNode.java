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

/**
 * This is the internal node class. It extends the standard node class to give a
 * near identical node behaviour, but with some methods removed to save
 * computation time. The internal node is used in contact handling for
 * shell_bt_4 element when the advanced_edge option is selected.
 * 
 * @author Jonas Forssell
 * 
 * @see OtherClasses
 */
public class InternalNode extends Node {
	Element master;

	public InternalNode() {
		super();
		type = INTERNAL_NODE;
	}

	/**
	 * This method updates the node position. Note that since the internal node
	 * get its location set by the element that uses it, it is just a waste of
	 * resource to have code here and since the internal nodes are also part of
	 * the main loop, this method will be called. Clearing this out solves this
	 * issue. Creation date: (2004-11-17 01.37.59)
	 */
	public void calculateNewPosition(double timestep, double currtime) {
		master.setInternalNodePosition();
	}

	public void registerMasterElement(Element el) {
		master = el;
	}

	public void setInitialConditions() {
		super.setInitialConditions();
		master.setInternalNodePosition();
	}
}

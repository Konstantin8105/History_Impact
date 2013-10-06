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

import java.io.*;

import java.util.*;

import uka.karmi.rmi.RemoteException;

import jp.lang.RemoteObject;

/**
 * Insert the type's description here. Creation date: (2001-10-30 23.58.22)
 * 
 * @author:
 */
public abstract class Reader {
	/**
	 * Reader constructor comment.
	 */
	public Reader() {
		super();
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-31
	 * 00.08.26)
	 */
	public abstract void close();

	/**
	 * Insert the method's description here. Creation date: (2001-10-31
	 * 00.08.57)
	 */
	public abstract void getControlSet(Controlset controlset)
			throws java.text.ParseException;

	/**
	 * Insert the method's description here. Creation date: (2001-10-31
	 * 00.14.11)
	 * 
	 * @return krockpackage.Constraint
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public abstract Constraint getNextConstraint(RplVector nodelist)
			throws java.text.ParseException;

	/**
	 * Insert the method's description here. Creation date: (2001-10-31
	 * 00.22.08)
	 * 
	 * @param param
	 *            java.util.Vector
	 * @param param2
	 *            java.util.Vector
	 * 
	 * @return krockpackage.Element
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public abstract Element getNextElement(RplVector materiallist,
			RplVector nodelist, RplVector loadlist, Hashtable nodetable)
			throws java.text.ParseException;

	/**
	 * Insert the method's description here. Creation date: (2001-10-31
	 * 00.22.08)
	 * 
	 * @param param
	 *            java.util.Vector
	 * @param param2
	 *            java.util.Vector
	 * 
	 * @return krockpackage.Element
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public abstract Tracker getNextTracker(RplVector nodelist,
			RplVector elementlist) throws java.text.ParseException;

	/**
	 * Insert the method's description here. Creation date: (2001-10-31
	 * 00.25.08)
	 * 
	 * @return krockpackage.Load
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public abstract Load getNextLoad(RplVector nodelist)
			throws java.text.ParseException;

	/**
	 * Insert the method's description here. Creation date: (2001-10-31
	 * 00.34.45)
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public abstract Material getNextMaterial() throws java.text.ParseException;

	/**
	 * Insert the method's description here. Creation date: (2001-10-31
	 * 00.36.05)
	 * 
	 * @param constraintlist
	 *            java.util.Vector
	 * @param nodelist
	 *            java.util.Vector
	 * 
	 * @return krockpackage.Node
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public abstract Node getNextNode(RplVector constraintlist,
			RplVector loadlist) throws java.text.ParseException;

	/**
	 * Insert the method's description here. Creation date: (2001-10-31
	 * 00.46.27)
	 * 
	 * @return int
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public abstract int numberOfConstraints() throws java.text.ParseException;

	/**
	 * Insert the method's description here. Creation date: (2001-10-31
	 * 00.46.27)
	 * 
	 * @return int
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public abstract int numberOfControls() throws java.text.ParseException;

	/**
	 * Insert the method's description here. Creation date: (2001-10-31
	 * 00.46.27)
	 * 
	 * @return int
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public abstract int numberOfElements() throws java.text.ParseException;

	/**
	 * Insert the method's description here. Creation date: (2001-10-31
	 * 00.46.27)
	 * 
	 * @return int
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public abstract int numberOfTrackers() throws java.text.ParseException;

	/**
	 * Insert the method's description here. Creation date: (2001-10-31
	 * 00.46.27)
	 * 
	 * @return int
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public abstract int numberOfGroups() throws java.text.ParseException;

	/**
	 * Insert the method's description here. Creation date: (2001-10-31
	 * 00.46.27)
	 * 
	 * @return int
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public abstract int numberOfLoads() throws java.text.ParseException;

	/**
	 * Insert the method's description here. Creation date: (2001-10-31
	 * 00.46.27)
	 * 
	 * @return int
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public abstract int numberOfMaterials() throws java.text.ParseException;

	/**
	 * Insert the method's description here. Creation date: (2001-10-31
	 * 00.46.27)
	 * 
	 * @return int
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public abstract int numberOfNodes() throws java.text.ParseException;

	/**
	 * Insert the method's description here. Creation date: (2001-10-31
	 * 00.08.26)
	 */
	public abstract void open();

	/**
	 * Neat little routine that chops up a string into an array of Tokens
	 */
	public Token[] tokenize(java.io.StreamTokenizer str) throws IOException {
		int i;
		Vector v = new Vector();
		Token[] arr;

		while ((str.nextToken() != java.io.StreamTokenizer.TT_EOL)
				&& (str.ttype != java.io.StreamTokenizer.TT_EOF)) {
			if (str.ttype == java.io.StreamTokenizer.TT_WORD) {
				v.add(new Token(str.sval));
			} else {
				v.add(new Token(str.nval));
			}
		}

		arr = new Token[v.size()];

		for (i = 0; i < v.size(); i++) {
			arr[i] = (Token) v.elementAt(i);
		}

		return arr;
	}

	/**
	 * This method returns the writer to be used for the results
	 */
	public abstract Writer getWriter(RplVector nodelist, RplVector elementlist,
			Controlset control, RemoteObject[] cluster_nodes)
			throws RemoteException;

	/**
	 * This method returns the Trackwriter to be used for the trackers
	 */
	public abstract TrackWriter getTrackWriter(RplVector trackerlist,
			Controlset control, RemoteObject[] cluster_nodes)
			throws RemoteException;

	/**
	 * Insert the method's description here. Creation date: (2001-10-31
	 * 00.46.27)
	 * 
	 * @return int
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public abstract void preProcess() throws java.text.ParseException;

}

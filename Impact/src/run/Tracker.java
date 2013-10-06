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

import run.trackers.*;
import java.io.*;

/**
 * This is the mother class for all Trackers Here, some of the common methods
 * are implemented which can be called by any of the implementations. In
 * addition, all the nessesarry methods needed for a new tracker implementation
 * are represented as abstract methods. This will generate error messages (or
 * warnings) at compilation time unless all the trackers are complete. A
 * description of each method and what it is supposed to do will be found at
 * each method header.
 * 
 * @author Jonas Forssell, Yuriy Mikhaylovskiy
 * 
 */
public abstract class Tracker implements java.io.Serializable {
	public static final int RESULT = -2;
	public static final int RESULT_HEADER = -1;
	protected String filename;
	protected int number;
	protected double result;
	protected double target;
	protected double tolerance;
	protected double targettime;
	protected double timetolerance;

	// Operations

	/**
	 * An operation that does ...
	 */
	public Tracker() {
	}

	public abstract void collectData() throws IllegalArgumentException;

	/**
	 * An operation that does ...
	 */
	public abstract void calculate();

	/**
	 * An operation that does ...
	 * 
	 * @param firstParamName
	 *            a description of this parameter
	 */
	public static Tracker getTrackerOfType_Fembic(String type)
			throws java.lang.IllegalArgumentException {
		// 1
		if (type.toUpperCase().equals("SECTIONFORCE")) {
			return new SectionForceTracker();
		}

		// 2
		if (type.toUpperCase().equals("CONTACTFORCE")) {
			return new ContactForceTracker();
		}

		// 3
		if (type.toUpperCase().equals("NODEFORCE")) {
			return new NodeForceTracker();
		}

		// 4
		if (type.toUpperCase().equals("NODEDISPLACEMENT")) {
			return new NodeDisplacementTracker();
		}

		// 5
		if (type.toUpperCase().equals("NODEACCELERATION")) {
			return new NodeAccelerationTracker();
		}

		// 6
		if (type.toUpperCase().equals("NODEVELOCITY")) {
			return new NodeVelocityTracker();
		}

		// 7
		if (type.toUpperCase().equals("ENERGY")) {
			return new EnergyTracker();
		}

		// 8
		if (type.toUpperCase().equals("NODEDISTANCE")) {
			return new NodeDistanceTracker();
		}

		// 9
		if (type.toUpperCase().equals("RODFORCE")) {
			return new RodForceTracker();
		}

		// 10
		if (type.toUpperCase().equals("BEAMSPRING")) {
			return new BeamSpringTracker();
		}

		// 11
		if (type.toUpperCase().equals("NODEMOMENT")) {
			return new NodeMomentTracker();
		}

		throw new IllegalArgumentException("Illegal Tracker type detected");
	}

	/**
	 * An operation that does ...
	 * 
	 * @param firstParamName
	 *            a description of this parameter
	 */
	public static Tracker getTrackerOfType_Nastran(String type)
			throws java.lang.IllegalArgumentException {
		return new NodeForceTracker();
	}

	/**
	 * An operation that does ...
	 * 
	 * @param firstParamName
	 *            a description of this parameter
	 */
	public static Tracker getTrackerOfType_Gmsh(int type)
			throws java.lang.IllegalArgumentException {
		return new NodeForceTracker();
	}

	/**
	 * This method searches the given arg string to return the number of defined
	 * nodes The methods throws an parseException if no number is found. The arg
	 * is assumed to be defined as [nodenr,nodenr] Creation date: (08/09/01 %T)
	 * 
	 * @param nr
	 *            int
	 * @param arg
	 *            java.lang.String
	 * 
	 * @return int
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public int getNumberOfNodes(String arg) throws java.text.ParseException,
			IllegalArgumentException {
		int index;
		int indexcount;
		int lastindex;

		// Find the index of the separator
		index = 0;
		indexcount = 0;

		// Search for the last occurrance of the separator ",". If a -1 is
		// returned, there is none.
		lastindex = arg.lastIndexOf(',');

		while (index < lastindex) {
			index = arg.indexOf(',', index + 1);
			indexcount++;
		}

		// Ok, now return the number of nodes defined
		return indexcount + 1;
	}

	/**
	 * This method searches the given arg string to return the nodenumber of
	 * order defined in nr. The methods throws an parseException if no number is
	 * found. The arg is assumed to be defined as [nodenr,nodenr] Creation date:
	 * (08/09/01 %T)
	 * 
	 * @param nr
	 *            int
	 * @param arg
	 *            java.lang.String
	 * 
	 * @return int
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public int getNodeNumber(int nr, String arg)
			throws java.text.ParseException, IllegalArgumentException {
		int i;
		int index;
		int nextindex;

		// Find the index of the separator
		index = 0;

		for (i = 0; i < (nr - 1); i++) {
			index = arg.indexOf(',', index + 1);
		}

		nextindex = arg.indexOf(',', index + 1);

		if (nextindex == -1) {
			nextindex = arg.length() - 1;
		}

		if (index == -1) {
			throw new IllegalArgumentException(
					"Incorrect number of element nodes defined");
		}

		// Ok, now read the number and convert it to integer
		i = Integer.parseInt(arg.substring(index + 1, nextindex).trim());

		return i;
	}

	/**
	 * This method searches the given arg string to return the number of order
	 * defined in nr. The methods throws an parseException if no number is
	 * found. The arg is assumed to be defined as [nr,nr,...,nr] Creation date:
	 * (08/09/01 %T)
	 * 
	 * @param nr
	 *            int
	 * @param arg
	 *            java.lang.String
	 * 
	 * @return double
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public double getNumber(int nr, String arg)
			throws java.text.ParseException, IllegalArgumentException {
		int i;
		int index;
		int nextindex;

		// Find the index of the separator
		index = 0;

		for (i = 0; i < (nr - 1); i++) {
			index = arg.indexOf(',', index + 1);
		}

		nextindex = arg.indexOf(',', index + 1);

		if (nextindex == -1) {
			nextindex = arg.length() - 1;
		}

		if (index == -1) {
			throw new IllegalArgumentException(
					"Incorrect number of element nodes defined");
		}

		// Ok, now read the number and convert it to integer
		return Double.parseDouble(arg.substring(index + 1, nextindex).trim());
	}

	/**
	 * This method returns a handle to the node with the node number nodenumber
	 * Creation date: (08/09/01 %T)
	 * 
	 * @param nodenumber
	 *            int
	 * @param nodelist
	 *            java.util.Vector
	 * 
	 * @return int
	 * 
	 * @exception java.lang.IllegalArgumentException
	 *                The exception description.
	 */
	public Node findNode(int nodenumber, RplVector nodelist)
			throws java.lang.IllegalArgumentException {
		int i;
		Node tempnode;

		for (i = 0; i < nodelist.size(); i++) {
			tempnode = (Node) nodelist.elementAt(i);

			if (tempnode.getNumber() == nodenumber) {
				return tempnode;
			}
		}

		throw new java.lang.IllegalArgumentException("No node with number" + i
				+ "exists");
	}

	/**
	 * This method returns a handle to the element with the element number
	 * elementnumber Creation date: (08/09/01 %T)
	 * 
	 * @param nodenumber
	 *            int
	 * @param nodelist
	 *            java.util.Vector
	 * 
	 * @return int
	 * 
	 * @exception java.lang.IllegalArgumentException
	 *                The exception description.
	 */
	public Element findElement(int elementnumber, RplVector elementlist)
			throws java.lang.IllegalArgumentException {
		int i;
		Element tempelement;

		for (i = 0; i < elementlist.size(); i++) {
			tempelement = (Element) elementlist.elementAt(i);

			if (tempelement.getNumber() == elementnumber) {
				return tempelement;
			}
		}

		throw new java.lang.IllegalArgumentException("No element with number"
				+ i + "exists");
	}

	/**
	 * Insert the method's description here. Creation date: (08/09/01 %T)
	 * 
	 * @return int
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * This method returns the Tracker type. It is a constant static variable
	 * which consists of a number depending on the Tracker class. Please look at
	 * the explanation under the Tracker class Creation date: (09/12/01 %T)
	 * 
	 * @return int
	 */
	public abstract int getType();

	/**
	 * This method checks the data in the indatafile and sets the corresponding
	 * parameters for the Tracker. It is defined in the element due to the fact
	 * of isolating the Tracker from the main program, thus making adding a new
	 * Tracker a simpler issue. Creation date: (08/09/01 %T)
	 * 
	 * @param param2
	 *            java.lang.String
	 * @param param3
	 *            java.lang.String
	 * @param param1
	 *            java.lang.String
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public abstract void parse_Fembic(Token[] param, int lineno,
			RplVector nodelist, RplVector elementlist)
			throws java.text.ParseException;

	/**
	 * This method checks the data in the indatafile and sets the corresponding
	 * parameters for the Tracker. It is defined in the element due to the fact
	 * of isolating the Tracker from the main program, thus making adding a new
	 * Tracker a simpler issue. Creation date: (08/09/01 %T)
	 * 
	 * @param param2
	 *            java.lang.String
	 * @param param3
	 *            java.lang.String
	 * @param param1
	 *            java.lang.String
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public abstract void parse_Nastran(Token[] param, int lineno,
			RplVector nodelist, RplVector elementlist)
			throws java.text.ParseException;

	/**
	 * This method checks the data in the indatafile and sets the corresponding
	 * parameters for the Tracker. It is defined in the element due to the fact
	 * of isolating the Tracker from the main program, thus making adding a new
	 * Tracker a simpler issue. Creation date: (08/09/01 %T)
	 * 
	 * @param param2
	 *            java.lang.String
	 * @param param3
	 *            java.lang.String
	 * @param param1
	 *            java.lang.String
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public abstract void parse_Gmsh(Token[] param, int lineno,
			RplVector nodelist, RplVector elementlist)
			throws java.text.ParseException;

	/**
	 * This method is used to check that all mandatory parameters have been set
	 */
	public abstract void checkIndata() throws IllegalArgumentException;

	/**
	 * This method is used to create the lines needed in the result file. The
	 * method creates a string which is printed directly. However, due to the
	 * fact that the line may be different depending on what is requested to be
	 * printed and that the number of methods should be kept down, the first
	 * parameter here is a control parameter. This parameter describes what
	 * should be printed. The second parameter is the current time.
	 * 
	 * @param ctrl
	 *            int is the control parameter
	 * @param currtime
	 *            The current time
	 */
	public abstract void print_Gid(int ctrl, double currtime)
			throws IOException, IllegalArgumentException;

	/**
	 * Abstract method to set the inital conditions of all trackers. Each
	 * tracker must have this method.
	 */
	public abstract void setInitialConditions();

	/**
	 * Sets the number of the tracker.
	 * 
	 * @param newNumber
	 *            int
	 */
	public void setNumber(int newNumber) {
		number = newNumber;
	}

	public double getResult() {
		return result;
	}

	/**
	 * This method checks if target value has been reached. The method is used
	 * for debugging using any tracker.
	 * 
	 * @return true or false if target is reached.
	 */
	protected boolean checkTarget(double currtime) {
		if ((Math.abs(result - target) < tolerance)
				&& (Math.abs(currtime - targettime) < timetolerance)) {
			return true;
		} else {
			return false;
		}
	}
}

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

import run.constraints.*;

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
 * @see OtherClasses
 */
public abstract class Constraint implements java.io.Serializable {
	protected int number;
	protected java.lang.String name;
	protected String type;
	protected boolean processed;

	/**
     *
     */

	// Operations

	/**
	 * An operation that does ...
	 */
	public Constraint() {
	}

	public static Constraint getConstraintOfType_Fembic(String type)
			throws java.lang.IllegalArgumentException {
		if (type.toUpperCase().equals("BOUNDARY_CONDITION")) {
			return new BoundaryCondition();
		}

		if (type.toUpperCase().equals("RIGID_BODY")) {
			return new RigidBody();
		}

		throw new IllegalArgumentException("Illegal Element Type");
	}

	public static Constraint getConstraintOfType_Nastran(String type)
			throws java.lang.IllegalArgumentException {
		if (type.toUpperCase().equals("SPC1")) {
			return new BoundaryCondition();
		}

		if (type.toUpperCase().equals("RBE2")) {
			return new RigidBody();
		}

		throw new IllegalArgumentException("Illegal Element Type");
	}

	public static Constraint getConstraintOfType_Gmsh(int type)
			throws java.lang.IllegalArgumentException {

		// Currently, there is only one type of BC available in this translator

		return new BoundaryCondition();

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

		if (lastindex == -1) {
			throw new IllegalArgumentException(
					"No ',' separator in the node definition");
		}

		// Ok, there is at least one comma separator. Let's continue.
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
			throws IllegalArgumentException {
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

		throw new java.lang.IllegalArgumentException("No node with number"
				+ nodenumber + "exists");
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
	 * Insert the method's description here. Creation date: (08/09/01 %T)
	 * 
	 * @return int
	 */
	public String getName() {
		return name;
	}

	/**
	 * Insert the method's description here. Creation date: (08/09/01 %T)
	 * 
	 * @return int
	 */
	public void setName(String nam) {
		name = new String(nam);
	}

	/**
	 * This method returns the Tracker type. It is a constant static variable
	 * which consists of a number depending on the Tracker class. Please look at
	 * the explanation under the Tracker class Creation date: (09/12/01 %T)
	 * 
	 * @return int
	 */
	public String getType() {
		return type;
	}

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
			RplVector nodelist) throws java.text.ParseException;

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
			RplVector nodelist) throws java.text.ParseException;

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
			RplVector nodelist) throws java.text.ParseException;

	/**
	 * This method is used to check that all mandatory parameters have been set
	 */
	public abstract void checkIndata() throws IllegalArgumentException;

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public abstract void setInitialConditions();

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public abstract void applyAccelerationConditions(Node nod, double currtime);

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public abstract void applyVelocityConditions(Node nod, double currtime);

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public abstract void registerNode(Node nod);

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public abstract void update();

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public abstract void determineMassMatrix(RplVector nodelist);

	/**
	 * Insert the method's description here. Creation date: (08/09/01 %T)
	 * 
	 * @param newNumber
	 *            int
	 */
	public void setNumber(int newNumber) {
		number = newNumber;
	}

	public abstract String print_Fembic(int ctrl);

	/**
	 * This method is a common method for all the constraint types to use. It
	 * calculates the local base vectors for a local axis system. The input are
	 * three points in space. They are used as follows: 1. The first point is
	 * the base point. 2. The local x-axis is defined from base point to the
	 * second point. 3. A temporary second axis is defined from the base point
	 * to the third point 4. The cross vector between the x-axis and the
	 * temporary axis, defines the normal to the plane defined by the x-axis and
	 * the temporary axis. This normal is normal to the x-axis and will be the
	 * local z-axis. 5. Finally, the y-axis is calculated as the cross product
	 * of the x-axis and z-axis. Now, all the three axis are normal to each
	 * other and a correct coordinate system is defined. The method will return
	 * a matrix with these vectors: x-vec y-vec z-vec x y z Creation date:
	 * (26/08/01 %T) Jonas Forssell
	 */
	public Jama.Matrix calculateLocalBaseVectors(double x1, double y1,
			double z1, double x2, double y2, double z2, double x3, double y3,
			double z3) {
		Jama.Matrix base_vector_system = new Jama.Matrix(3, 3);
		Jama.Matrix local_x_axis = new Jama.Matrix(3, 1);
		Jama.Matrix local_y_axis = new Jama.Matrix(3, 1);
		Jama.Matrix local_z_axis = new Jama.Matrix(3, 1);

		// 1&2. Define the local x-axis.
		local_x_axis.set(0, 0, x2 - x1);
		local_x_axis.set(1, 0, y2 - y1);
		local_x_axis.set(2, 0, z2 - z1);

		// 3. Define temporary axis (here disguised as y-axis)
		local_y_axis.set(0, 0, x3 - x1);
		local_y_axis.set(1, 0, y3 - y1);
		local_y_axis.set(2, 0, z3 - z1);

		// 4. Calculate the z-axis
		local_z_axis = local_x_axis.vectorProduct(local_y_axis);

		// 5. Calculate the y-axis
		local_y_axis = local_x_axis.vectorProduct(local_z_axis);

		// Now, normalise and set up the base vector system matrix.
		local_x_axis.timesEquals(1.0 / local_x_axis.length());
		local_y_axis.timesEquals(1.0 / local_y_axis.length());
		local_z_axis.timesEquals(1.0 / local_z_axis.length());

		//
		base_vector_system.setMatrix(0, 2, 0, 0, local_x_axis);
		base_vector_system.setMatrix(0, 2, 1, 1, local_y_axis);
		base_vector_system.setMatrix(0, 2, 2, 2, local_z_axis);

		// Return it
		return base_vector_system;
	}

	/**
	 * Returns the processed.
	 * 
	 * @return boolean
	 */
	public boolean isProcessed() {
		return processed;
	}

	/**
	 * Sets the processed.
	 * 
	 * @param processed
	 *            The processed to set
	 */
	public void setProcessed(boolean processed) {
		this.processed = processed;
	}

}

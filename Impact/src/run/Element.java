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

import run.elements.*;
import java.util.*;
import jp.lang.*;

/**
 * This is the mother class for all elements Here, some of the common methods
 * are implemented which can be called by any of the implementations. In
 * addition, all the nessesarry methods needed for a new element implementation
 * are represented as abstract methods. This will generate error messages (or
 * warnings) at compilation time unless all the elements are complete. A
 * description of each method and what it is supposed to do will be found at
 * each method header. Sub-element classes are designated a unique type which is
 * defined by a three digit number where the first number represent the element
 * type class and the second two are a unique number. Available type classes
 * are: 0 = point elements such as a one dimensional node spring 1 = Linear
 * elements such as rods, beams etc, 2 or more nodes 2 = Triangle elements such
 * as tria shells or plates, 3 or more nodes 3 = Quadilaterial elements such as
 * Shell elements or plates, 4 or more nodes 4 = Tetrahedra elements, 4 or more
 * nodes 5 = Hexahedra elements, 6 or more nodes >5 = future use
 * 
 * @author Jonas Forssell, Yuriy Mikhaylovskiy
 * 
 * @see OtherClasses
 */
public abstract class Element implements java.io.Serializable {
	public static final int MESH = -2;
	public static final int RESULT_HEADER = -1;
	public static final int MESH_HEADER = -3;
	public static final int RESULT_SUB_HEADER = -4;

	/*
	 * These are the different result types that can be printed by the elements.
	 * If you want to add a new format, start here by adding another one
	 */
	public static final int RESULT_STRESS_GLOBAL = 0;
	public static final int RESULT_STRESS_LOCAL = 1;
	public static final int RESULT_STRAIN_GLOBAL = 2;
	public static final int RESULT_STRAIN_LOCAL = 3;
	public boolean processed;
	protected Object owner;
	protected int number;
	protected String type;
	protected Node[] node;
	protected Node middle_node;
	protected int cpu_number;
	protected boolean failed, deactivated;

	/**
     *
     */

	// Operations

	/**
	 * An operation that does ...
	 */
	public Element() {
		failed = false;
	}

	/**
	 * This metod calculates the elements contribution regarding mass and
	 * rotational interta to the nodes. Since this program uses lumped mass, all
	 * the mass are concentrated to the nodes, and so are the Inertia of
	 * rotation as well. The mass is usually quite simple to calculate. In this
	 * example it is half of the rod in each node. For a shell element it could
	 * be slighty more trickier. The difficult part comes when the rotational
	 * inertia is to be calculated since this is needs to be calculated in three
	 * dimensions and then transformed to the global xyz coordinate system
	 * before adding to the node. Roughly, the procedure is as follows:
	 * Calculate element mass distribution and add to node Calculate element
	 * intertia Transform to global directions Add to node inertia matrix
	 * Naturally, the specifics will be different for each element type.
	 */
	public abstract void assembleMassMatrix() throws IllegalArgumentException;

	/**
	 * An operation that does ...
	 */
	public abstract void calculateContactForces();

	/**
	 * An operation that does ...
	 * 
	 * @param firstParamName
	 *            a description of this parameter
	 */
	public abstract void calculateExternalForces(double currtime);

	/**
	 * This method is a common method for all the element types to use. It
	 * calculates the local base vectors for the element. These vectors defines
	 * the local coordinate system for the element on which all the stress,
	 * strain and force calculations are based. The input are three points in
	 * space. They are used as follows: 1. The first point is the base point. 2.
	 * The local x-axis is defined from base point to the second point. 3. A
	 * temporary second axis is defined from the base point to the third point
	 * 4. The cross vector between the x-axis and the temporary axis, defines
	 * the normal to the plane defined by the x-axis and the temporary axis.
	 * This normal is normal to the x-axis and will be the local z-axis. 5.
	 * Finally, the y-axis is calculated as the cross product of the x-axis and
	 * z-axis. Now, all the three axis are normal to each other and a correct
	 * coordinate system is defined. The method will return a matrix with these
	 * vectors: x-vec y-vec z-vec x y z Creation date: (26/08/01 %T) Jonas
	 * Forssell
	 */
	public void calculateLocalBaseVectors(double x1, double y1, double z1,
			double x2, double y2, double z2, double x3, double y3, double z3,
			Jama.Matrix bvs) {
		// 1&2. Define the local x-axis.
		bvs.set(0, 0, x2 - x1);
		bvs.set(0, 1, y2 - y1);
		bvs.set(0, 2, z2 - z1);

		// 3. Define the second-axis
		bvs.set(1, 0, x3 - x1);
		bvs.set(1, 1, y3 - y1);
		bvs.set(1, 2, z3 - z1);

		// 4. Calculate the z-axis
		// local_z_axis = local_x_axis.vectorProduct(local_y_axis);
		bvs.set(2,
				0,
				(bvs.get(0, 1) * bvs.get(1, 2))
						- (bvs.get(0, 2) * bvs.get(1, 1)));
		bvs.set(2,
				1,
				(bvs.get(0, 2) * bvs.get(1, 0))
						- (bvs.get(0, 0) * bvs.get(1, 2)));
		bvs.set(2,
				2,
				(bvs.get(0, 0) * bvs.get(1, 1))
						- (bvs.get(0, 1) * bvs.get(1, 0)));

		// and normalize
		// local_z_axis = local_z_axis.times(1.0/local_z_axis.length());
		bvs.set(1,
				0,
				Math.sqrt((bvs.get(2, 0) * bvs.get(2, 0))
						+ (bvs.get(2, 1) * bvs.get(2, 1))
						+ (bvs.get(2, 2) * bvs.get(2, 2))));
		bvs.set(2, 0, bvs.get(2, 0) / bvs.get(1, 0));
		bvs.set(2, 1, bvs.get(2, 1) / bvs.get(1, 0));
		bvs.set(2, 2, bvs.get(2, 2) / bvs.get(1, 0));

		// 5. Adjust the x-axis
		// local_x_axis = local_x_axis.times(1.0/local_x_axis.length());
		bvs.set(1,
				0,
				Math.sqrt((bvs.get(0, 0) * bvs.get(0, 0))
						+ (bvs.get(0, 1) * bvs.get(0, 1))
						+ (bvs.get(0, 2) * bvs.get(0, 2))));
		bvs.set(0, 0, bvs.get(0, 0) / bvs.get(1, 0));
		bvs.set(0, 1, bvs.get(0, 1) / bvs.get(1, 0));
		bvs.set(0, 2, bvs.get(0, 2) / bvs.get(1, 0));

		// 6. Adjust the y-axis
		// local_y_axis = local_z_axis.vectorProduct(local_x_axis);
		bvs.set(1,
				0,
				(bvs.get(2, 1) * bvs.get(0, 2))
						- (bvs.get(2, 2) * bvs.get(0, 1)));
		bvs.set(1,
				1,
				(bvs.get(2, 2) * bvs.get(0, 0))
						- (bvs.get(2, 0) * bvs.get(0, 2)));
		bvs.set(1,
				2,
				(bvs.get(2, 0) * bvs.get(0, 1))
						- (bvs.get(2, 1) * bvs.get(0, 0)));
	}

	/**
	 * An operation that does ...
	 * 
	 * @param firstParamName
	 *            a description of this parameter
	 */
	public abstract void calculateNodalForces(int j, double timestep);

	/**
	 * An operation that does ...
	 * 
	 * @param firstParamName
	 *            a description of this parameter
	 */
	public abstract void calculateStrain(double timestep, int i);

	/**
	 * An operation that does ...
	 * 
	 * @param firstParamName
	 *            a description of this parameter
	 */
	public abstract void calculateStress(int i, double timestep);

	/**
	 * Insert the method's description here. Creation date: (10/12/01 %T)
	 * 
	 * @param current_timestep
	 *            double
	 * 
	 * @return double
	 */
	public abstract double checkTimestep(double current_timestep);

	/**
	 * This method returns a handle to the material with the corresponding
	 * material name Creation date: (08/09/01 %T)
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
	public Material findMaterial(String name, RplVector materiallist)
			throws java.lang.IllegalArgumentException {
		int i;
		Material tempmaterial;

		for (i = 0; i < materiallist.size(); i++) {
			tempmaterial = (Material) materiallist.elementAt(i);

			if (tempmaterial.getName().equals(name)) {
				return tempmaterial;
			}
		}

		throw new java.lang.IllegalArgumentException("No material with name"
				+ name + "is defined");
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
	public Node findNode(int nodenumber, Hashtable nodelist)
			throws java.lang.IllegalArgumentException {
		Node tempnode;

		tempnode = (Node) nodelist.get(new Integer(nodenumber));

		if (tempnode != null)
			return tempnode;

		throw new java.lang.IllegalArgumentException("No node with number"
				+ nodenumber + "exists");
	}

	/**
	 * This method scans all element types and returns an instance of that
	 * element if it finds one. The reason for placing this method here is that
	 * as a programmer, adding a new element will only involve changing the
	 * element class and subclasses. No messing around in the other classes
	 * should be needed. Creation date: (09/12/01 %T)
	 * 
	 * @param type
	 *            java.lang.String
	 * 
	 * @return krockpackage.Element
	 * 
	 * @exception java.lang.IllegalArgumentException
	 *                The exception description.
	 */
	public static Element getElementOfType_Fembic(String type)
			throws java.lang.IllegalArgumentException {
		if (type.toUpperCase().equals("ROD_2")) {
			return new Rod_2();
		}

		if (type.toUpperCase().equals("BEAM_2")) {
			throw new IllegalArgumentException(
					"The Beam element has not yet been implemented");
		}

		if (type.toUpperCase().equals("SHELL_BT_4")) {
			return new Shell_BT_4();
		}

		if (type.toUpperCase().equals("SOLID_ISO_6")) {
			return new Solid_Iso_6();
		}

		if (type.toUpperCase().equals("SOLID_ISO_4")) {
			return new Solid_Iso_4();
		}

		if (type.toUpperCase().equals("SHELL_C0_3")) {
			return new Shell_C0_3();
		}

		if (type.toUpperCase().equals("CONTACT_TRIANGLE")) {
			return new Contact_Triangle();
		}

		if (type.toUpperCase().equals("CONTACT_LINE")) {
			return new Contact_Line();
		}

		if (type.toUpperCase().equals("BEAM_SPRING_2")) {
			return new Beam_Spring_2();
		}

		throw new IllegalArgumentException("Illegal Element Type");
	}

	/**
	 * This method scans all element types and returns an instance of that
	 * element if it finds one. The reason for placing this method here is that
	 * as a programmer, adding a new element will only involve changing the
	 * element class and subclasses. No messing around in the other classes
	 * should be needed. Creation date: (09/12/01 %T)
	 * 
	 * @param type
	 *            java.lang.String
	 * 
	 * @return krockpackage.Element
	 * 
	 * @exception java.lang.IllegalArgumentException
	 *                The exception description.
	 */
	public static Element getElementOfType_Gmsh(int type, int subtype)
			throws java.lang.IllegalArgumentException {
		if (type == 1 && subtype == 0) {
			return new Rod_2();
		}

		if (type == 1 && subtype == 1) {
			return new Contact_Line();
		}

		if (type == 1 && subtype == 2) {
			return new Beam_Spring_2();
		}

		if (type == 2 && subtype == 0) {
			return new Shell_C0_3();
		}

		if (type == 2 && subtype == 1) {
			return new Contact_Triangle();
		}

		if (type == 3 && subtype == 0) {
			return new Shell_BT_4();
		}

		if (type == 5 && subtype == 0) {
			return new Solid_Iso_6();
		}

		if (type == 5 && subtype == 1) {
			return new Solid_Iso_4();
		}

		throw new IllegalArgumentException("Illegal Element Type");
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
		i = (int) Double
				.parseDouble(arg.substring(index + 1, nextindex).trim());

		return i;
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
	 * Insert the method's description here. Creation date: (26/12/01 %T)
	 * 
	 * @return int
	 */
	public abstract int getNumberOfIntegrationPoints();

	/**
	 * This method returns the element type. It is a constant static variable
	 * which consists of a three digit number depending on the element class.
	 * Please look at the explanation under the element class Creation date:
	 * (09/12/01 %T)
	 * 
	 * @return int
	 */
	public String getType() {
		return type;
	}

	/**
	 * Insert the method's description here. Creation date: (09/12/01 %T)
	 * 
	 * @return boolean
	 */
	public boolean isProcessed() {
		return processed;
	}

	/**
	 * This method is used to check the stress and strain levels in the element
	 * and determine if it has fractured. The result should be placed in the
	 * fractured variable.
	 * 
	 * @return boolean
	 */
	public abstract void checkIfFailed();

	/**
	 * This method returns true if an element is destroyed (has failed)
	 * 
	 * @return boolean
	 */
	public boolean hasFailed() {
		return failed;
	}

	/**
	 * This method checks the data in the indatafile and sets the corresponding
	 * parameters for the element. It is defined in the element due to the fact
	 * of isolating the element from the main program, thus making adding a new
	 * element a simpler issue. Creation date: (08/09/01 %T)
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
			RplVector nodelist, RplVector materiallist, RplVector loadlist,
			Hashtable nodetable) throws java.text.ParseException,
			IllegalArgumentException;

	/**
	 * This method is used to create the lines needed in the result file. The
	 * method returns a string which is printed directly. However, due to the
	 * fact that the line may be different depending on what is requested to be
	 * printed and that the number of methods should be kept down, the first
	 * parameter here is a control parameter. This parameter describes what
	 * should be printed. The second parameter is a required input when gauss
	 * point results are to be printed. Creation date: (09/12/01 %T)
	 */
	/**
	 * This method is used to check that all mandatory parameters have been set
	 */
	public abstract void checkIndata() throws IllegalArgumentException;

	public abstract String print_Gid(int ctrl, int gpn);

	public abstract String print_Fembic(int ctrl, int gpn);

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public abstract void setInitialConditions() throws IllegalArgumentException;

	/**
	 * Insert the method's description here. Creation date: (08/09/01 %T)
	 * 
	 * @param newNumber
	 *            int
	 */
	public void setNumber(int newNumber) {
		number = newNumber;
	}

	/**
	 * Insert the method's description here. Creation date: (09/12/01 %T)
	 * 
	 * @param newProcessed
	 *            boolean
	 */
	public void setProcessed(boolean newProcessed) {
		processed = newProcessed;
	}

	/**
	 * This method is reserved for those element which uses internal contact
	 * nodes for enhanced contact control
	 * 
	 * Default is that nothing happens here.
	 * 
	 * @param newProcessed
	 *            boolean
	 */
	public void setInternalNodePosition() {
	}

	/**
	 * This method is reserved for those element which uses internal contact
	 * nodes for enhanced contact control
	 * 
	 * Default is that nothing is returned.
	 * 
	 * @param newProcessed
	 *            boolean
	 */
	public Node getInternalNode() {
		return null;
	}

	/**
	 * Insert the method's description here. Creation date: (19/11/01 %T)
	 */
	public abstract void updateLocalCoordinateSystem();

	/**
	 * This method is used for the element to determine it's most optimal CPU
	 * position with regards to its nodes. Ideally, the element should be placed
	 * on the same CPU as its nodes.
	 * 
	 */

	public int determineCpu_number() {
		int index, max;
		int[] vote = new int[node.length];
		int[] node_nr = new int[node.length];

		// Create an array with node numbers
		for (int i = 0; i < node.length; i++)
			node_nr[i] = node[i].getCpu_number();

		// Create a parallel array with "votes"
		Arrays.fill(vote, 0);

		// Now, scan and vote
		for (int i = 0; i < node_nr.length; i++) {
			index = 0;
			while (node_nr[index] != node_nr[i])
				index++;
			vote[index]++;
		}

		// Scan the vote array and find the index which has most votes
		index = 0;
		max = 0;

		for (int i = 0; i < vote.length; i++)
			if (vote[i] > max) {
				index = i;
				max = vote[i];
			}

		// Finally, set the preferred cpu_number for the element
		cpu_number = node_nr[index];
		return cpu_number;

	}

	/**
	 * @return Returns the cpu_number.
	 */
	public int getCpu_number() {
		return cpu_number;
	}

	/**
	 * @param cpu_number
	 *            The cpu_number to set.
	 */
	public void setCpu_number(int cpu_number) {
		this.cpu_number = cpu_number;
	}

	public int getMachineID() {
		System.out.print("Element:" + number + " @:"
				+ DistributedRuntime.getMachineID());
		for (int i = 0; i < node.length; i++)
			System.out.print(" N" + i + " @:" + node[i].getMachineID());
		System.out.println("");
		return DistributedRuntime.getMachineID();
	}

	/**
	 * @return Returns the middle_node.
	 */
	public Node getMiddle_node() {
		return middle_node;
	}

	/**
	 * @return Returns the node.
	 */
	public Node[] getNodes() {
		return node;
	}

	/**
	 * @return Deactivates the elelement
	 */
	public void deActivate() {
		deactivated = true;
		if (number > 0)
			System.out.println("CPU " + cpu_number + " deactivated element ("
					+ type + ") N=" + number + "   " + new Date());
	}

	/**
	 * @return Determines if the element is deactivated
	 */
	public boolean isDeActivated() {
		return deactivated;
	}

}

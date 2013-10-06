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

import Jama.*;
import run.*;

import java.util.*;

/**
 * This is a Triangular contact element. Its sole purpuse is to sense contact
 * against nodes and determine reaction forces to apply to the contacting node
 * and it's own nodes. It can be used on its own or incorporated into other
 * elements.
 * 
 * @author Jonas Forssell, Yuriy Mikhaylovskiy
 * 
 * @see OtherClasses
 */
public class Contact_Triangle extends Element {
	private double area;
	private Jama.Matrix local_coordinate_system;
	private Jama.Matrix force;
	private Jama.Matrix p;
	private Jama.Matrix p0;
	private Jama.Matrix pos;
	private Jama.Matrix trash;
	private double thickness;
	private double factor;
	private double contact_force;
	private boolean Nodes_are_set;
	private boolean Factor_is_set;
	private boolean T_is_set;
	private boolean Friction_is_set;
	private double A00;
	private double A01;
	private double A02;
	private double A11;
	private double A12;
	private double A22;
	private double xsi1;
	private double xsi2;
	private double xsi3;
	private double distance;
	private double friction_factor;
	private double y_max;
	private double y_min;
	private double z_max;
	private double z_min;
	private Vector Ftable;

	/**
	 * Insert the method's description here. Creation date: (2001-08-10
	 * 19:45:45)
	 */
	public Contact_Triangle() {
		super();
		type = new String("CONTACT_TRIANGLE");

		int i;
		p = new Jama.Matrix(9, 1);
		trash = new Jama.Matrix(9, 1);
		p0 = new Jama.Matrix(3, 1);
		node = new Node[3];
		force = new Jama.Matrix(3, 1);
		local_coordinate_system = new Jama.Matrix(3, 3);

		// Set the default value. These will change if the user defines
		// something.
		factor = 100;
	}

	/**
	 * Insert the method's description here. Creation date: (25/12/01 %PC%T)
	 */
	public void assembleMassMatrix() throws IllegalArgumentException {
		// Update local coordinate system
		this.updateLocalCoordinateSystem();

		// Determine the element Area
		this.calculateLocalVariables();
	}

	/**
	 * Insert the method's description here. Creation date: (25/12/01 %PC%T)
	 */
	public synchronized void calculateContactForces() {
		Node smallest;
		Node largest;
		Node current;
		int i;
		boolean finished;

		this.updateLocalCoordinateSystem();
		this.calculateLocalVariables();

		// Reset the total contact force
		contact_force = 0;

		// Find out the end nodes with smallest and largest x-coordinate
		smallest = node[0];

		if (node[1].getX_pos() < smallest.getX_pos()) {
			smallest = node[1];
		}

		if (node[2].getX_pos() < smallest.getX_pos()) {
			smallest = node[2];
		}

		current = smallest;

		while ((current.getLeft_neighbour() != null)
				&& (current.getX_pos() > (smallest.getX_pos() - (thickness / 2.0d)))) {
			current = current.getLeft_neighbour();
		}

		smallest = current;

		largest = node[0];

		if (node[1].getX_pos() > largest.getX_pos()) {
			largest = node[1];
		}

		if (node[2].getX_pos() > largest.getX_pos()) {
			largest = node[2];
		}

		current = largest;

		while ((current.getRight_neighbour() != null)
				&& (current.getX_pos() < (largest.getX_pos() + (thickness / 2.0d)))) {
			current = current.getRight_neighbour();
		}

		largest = current;

		current = smallest;

		// Determine max and min coordinates of the element
		y_min = Math.min(node[0].getY_pos(),
				Math.min(node[1].getY_pos(), node[2].getY_pos()))
				- thickness;
		y_max = Math.max(node[0].getY_pos(),
				Math.max(node[1].getY_pos(), node[2].getY_pos()))
				+ thickness;
		z_min = Math.min(node[0].getZ_pos(),
				Math.min(node[1].getZ_pos(), node[2].getZ_pos()))
				- thickness;
		z_max = Math.max(node[0].getZ_pos(),
				Math.max(node[1].getZ_pos(), node[2].getZ_pos()))
				+ thickness;

		// Check nodes from smallest to largest
		finished = false;

		while (!finished) {
			// Check for contact
			if (this.isInContact(current)) {
				// We are in contact.
				// Calculate the force
				force.set(0, 0, 0);
				force.set(1, 0, 0);
				force.set(2, 0, (factor * distance) / (thickness / 2.0d));

				// Add this contact force to the total contact force (for
				// tracking)
				contact_force += force.get(2, 0);

				// Now, add friction if requested
				if (Friction_is_set == true) {
					this.addFriction(current);
				}

				// Convert the force to global coordinate system
				force = local_coordinate_system.transpose().times(force);

				// Add it to the node in contact.
				current.addContactForce(force.times(-1));

				/*
				 * .. and add a reaction force to the three element nodes The
				 * natural coordinate detemines how the force should be
				 * distributed. The negative sign comes from the fact that it is
				 * a reaction force.
				 */
				node[0].addContactForce(force.times(xsi1));
				node[1].addContactForce(force.times(xsi2));
				node[2].addContactForce(force.times(xsi3));
			}

			// Now, check next node.
			if (current == largest) {
				finished = true;
			} else {
				current = current.getRight_neighbour();
			}
		}

		// All nodes checked. Clean out the Ftable vector if used.
		if (Friction_is_set == true) {
			for (i = 0; i < Ftable.size(); i++) {
				if (((Fdata) Ftable.elementAt(i)).checked == false) {
					Ftable.removeElementAt(i);
					i--;
				}
			}

			// Set the remaining ones to unchecked status.
			for (i = 0; i < Ftable.size(); i++) {
				((Fdata) Ftable.elementAt(i)).checked = false;
			}
		}
	}

	/**
	 * Checks if node is in contact with the element
	 */
	private boolean isInContact(Node contact_node) {
		// Skip nodes outside the element "box"
		if ((contact_node.getY_pos() < y_min)
				|| (contact_node.getY_pos() > y_max)) {
			return false;
		}

		if ((contact_node.getZ_pos() < z_min)
				|| (contact_node.getZ_pos() > z_max)) {
			return false;
		}

		// Exclude any of the own nodes
		if ((contact_node == node[0]) || (contact_node == node[1])
				|| (contact_node == node[2])) {
			return false;
		}

		// Extract node coordinates
		pos = contact_node.getPos();

		// Convert to local coordinate system with centre in node 1.
		pos = local_coordinate_system.times(pos).minus(p0);

		// Convert to natural coordinate system (assumes a position matrix
		// [1,x,y])
		xsi1 = A00 + (A01 * pos.get(0, 0)) + (A02 * pos.get(1, 0));
		xsi2 = (A11 * pos.get(0, 0)) + (A12 * pos.get(1, 0));
		xsi3 = A22 * pos.get(1, 0);

		// Check if any of the natural coordinates are negative (means it is not
		// inside the contact element borders)
		if (xsi1 < 0) {
			return false;
		}

		if (xsi2 < 0) {
			return false;
		}

		if (xsi3 < 0) {
			return false;
		}

		// All values are positive. This node may be in contact. Now, check the
		// distance to the surface.
		if (Math.abs(pos.get(2, 0)) > (thickness / 2)) {
			return false;
		}

		if (pos.get(2, 0) > 0) {
			distance = pos.get(2, 0) - (thickness / 2);
		} else {
			distance = pos.get(2, 0) + (thickness / 2);
		}

		return true;
	}

	/**
	 * addFriction(Node node) This method will check the given node against a
	 * local database to determine if any sliding has occurred against the
	 * element. If so, a vector will be calculated and a friction force
	 * determined.
	 */
	private void addFriction(Node contact_node) {
		Fdata tmp;
		double length1;
		double length2;
		double angle;
		int i;
		Matrix v1;
		Matrix v2;

		// Check if node is present in Ftable already
		for (i = 0; i < Ftable.size(); i++) {
			if (((Fdata) Ftable.elementAt(i)).cnode.equals(contact_node)) {
				break;
			}
		}

		if (i == Ftable.size()) {
			// This is first time. Add node to the table
			Ftable.add(new Fdata(contact_node, pos.get(0, 0), pos.get(1, 0)));

			// And do nothing more
			return;
		}

		// Getting here means node is present. See if a vector has been set
		tmp = (Fdata) Ftable.elementAt(i);

		if (tmp.vec_is_set == false) {
			// Calculate the vector
			tmp.vecX = pos.get(0, 0) - tmp.posX;
			tmp.vecY = pos.get(1, 0) - tmp.posY;

			// Set the new node position
			tmp.posX = pos.get(0, 0);
			tmp.posY = pos.get(1, 0);

			// Set the vector
			tmp.vec_is_set = true;

			// Notify action has been done
			tmp.checked = true;

			// Calculate vector length
			length1 = Math.sqrt((tmp.vecX * tmp.vecX) + (tmp.vecY * tmp.vecY));

			// Check if there has been any conciderable movement
			if (length1 < 1E-15) {
				return;
			}

			// Add the friction force (with a ramp up).
			force.set(0, 0, force.get(0, 0)
					+ ((((pos.get(2, 0) > 0) ? (-0.5) : 0.5) * friction_factor
							* force.get(2, 0) * tmp.vecX) / length1));
			force.set(1, 0, force.get(1, 0)
					+ ((((pos.get(2, 0) > 0) ? (-0.5) : 0.5) * friction_factor
							* force.get(2, 0) * tmp.vecY) / length1));

			// And return
			return;
		}

		// Getting here means node is present and a vector has been set
		// Make a temporary vector
		v1 = new Matrix(2, 1);
		v2 = new Matrix(2, 1);

		v1.set(0, 0, pos.get(0, 0) - tmp.posX);
		v1.set(1, 0, pos.get(1, 0) - tmp.posY);
		v2.set(0, 0, tmp.vecX);
		v2.set(1, 0, tmp.vecY);

		// Update the vector
		tmp.vecX = v1.get(0, 0);
		tmp.vecY = v1.get(1, 0);

		// Set the new node position
		tmp.posX = pos.get(0, 0);
		tmp.posY = pos.get(1, 0);

		// Set the vector
		tmp.vec_is_set = true;

		// Notify action has been done
		tmp.checked = true;

		// Calculate vector length
		length1 = Math.sqrt((tmp.vecX * tmp.vecX) + (tmp.vecY * tmp.vecY));

		// Check if there has been any conciderable movement
		if (length1 < 1E-15) {
			return;
		}

		// OK, there is movement, continue.
		length2 = Math.sqrt((v2.get(0, 0) * v2.get(0, 0))
				+ (v2.get(1, 0) * v2.get(1, 0)));

		// Calculate the angle between the vectors
		angle = Math.abs(Math.acos(v1.transpose().times(v2).get(0, 0)
				/ (length1 * length2)));

		// Add the friction force (with a ramp up if angle > 90 degrees to
		// prevent oscillation)
		force.set(0, 0, force.get(0, 0)
				+ ((((pos.get(2, 0) > 0) ? (-1) : 1)
						* ((angle < 1.5708) ? 1.0 : 0.5) * friction_factor
						* force.get(2, 0) * tmp.vecX) / length1));
		force.set(1, 0, force.get(1, 0)
				+ ((((pos.get(2, 0) > 0) ? (-1) : 1)
						* ((angle < 1.5708) ? 1.0 : 0.5) * friction_factor
						* force.get(2, 0) * tmp.vecY) / length1));

		// And return
		return;
	}

	/**
	 * Insert the method's description here. Creation date: (25/12/01 %PC%T)
	 */
	public void calculateExternalForces(double currtime) {
	}

	/**
	 * This calculates and adds the element internal forces to the nodes. For
	 * this contact element, nothing is done here Creation date: (2001-10-23
	 * 14.03.06)
	 * 
	 */
	public void calculateNodalForces(int integration_point, double timestep) {
	}

	/**
	 * This method normally calculates the strain in an element. For this
	 * contact element, nothing is done here Creation date: (25/12/01 %PC%T)
	 * Jonas Forssell
	 * 
	 * @param tstep
	 *            double
	 */
	public void calculateStrain(double tstep, int integration_point) {
	}

	/**
	 * This method normally calculates the stress in the element For this
	 * contact element, nothing is done here Insert the method's description
	 * here. Creation date: (25/12/01 %PC%T)
	 */
	public void calculateStress(int integration_point, double timestep) {
	}

	/**
	 * Checks and returns the smallest timestep that the element needs. For this
	 * contact element, nothing is done here Creation date: (25/12/01 %PC%T)
	 * 
	 * @param current_timestep
	 *            double
	 * 
	 * @return double
	 */
	public double checkTimestep(double current_timestep) {
		return current_timestep;
	}

	/**
	 * 
	 * The method checks destroyed element or not. If the element is destroyed
	 * variable failed = true else failed = false.
	 * 
	 */
	public void checkIfFailed() {
	}

	/**
	 * This element only has no integration point, but returns one since it
	 * needs to calculate at least one loop. Creation date: (26/12/01 %PC%T)
	 * 
	 * @return int
	 */
	public int getNumberOfIntegrationPoints() {
		return 1;
	}

	/**
	 * This method is used to read and parse the element indata. Creation date:
	 * (25/12/01 %PC%T)
	 * 
	 * @param arg1
	 *            java.lang.String
	 * @param arg2
	 *            java.lang.String
	 * @param arg3
	 *            java.lang.String
	 * @param lineno
	 *            int
	 * @param nodelist
	 *            java.util.Vector
	 * @param materiallist
	 *            java.util.Vector
	 */
	public void parse_Fembic(Token[] param, int lineno, RplVector nodelist,
			RplVector materiallist, RplVector loadlist, Hashtable nodetable)
			throws java.text.ParseException {
		int nodenumber;
		int j;
		int i = 0;
		int index;
		Load temp_load;

		while (i < param.length) {
			// The nodes of the element are defined
			if (param[i].getw().toUpperCase().equals("NODES")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// Assume now that the nodes are delivered in param3, with the
				// format
				// [nodenr,nodenr,nodenr,nodenr]
				if (!param[i + 2].getw().toUpperCase().startsWith("[")
						|| !param[i + 2].getw().toUpperCase().endsWith("]")) {
					throw new java.text.ParseException(
							"Error, node number definition should be [nodenr1,nodenr2,nodenr3,nodenr4]",
							lineno);
				}

				// Ok, now find the numbers
				try {
					for (j = 0; j < 3; j++) {
						node[j] = super
								.findNode(
										super.getNodeNumber(j + 1,
												param[i + 2].getw()), nodetable);
					}

					i += 3;
					Nodes_are_set = true;
				} catch (IllegalArgumentException e) {
					throw new java.text.ParseException(e.getMessage()
							+ " in line ", lineno);
				}
			} else
			// The number of integration points of the element is defined
			if (param[i].getw().toUpperCase().equals("FACTOR")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in param3. Set this in
				// the element
				factor = param[i + 2].getn();
				i += 3;
				Factor_is_set = true;
			} else
			// The thickness of the shell is defined
			if (param[i].getw().toUpperCase().equals("T")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the element thickness is in param3. Set this in
				// the element
				thickness = param[i + 2].getn();
				i += 3;
				T_is_set = true;
			} else
			// The friction factor is defined
			if (param[i].getw().toUpperCase().equals("FRICTION")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the element thickness is in param3. Set this in
				// the element
				friction_factor = param[i + 2].getn();
				i += 3;
				Friction_is_set = true;

				// Friction is defined. Now, set up a friction table for this
				// element.
				Ftable = new Vector();
			} else {
				// Neither material or nodes are defined. Then the parameter is
				// wrong.
				throw new java.text.ParseException(
						"Unknown Contact_Triangle element parameter ", lineno);
			}
		}
	}

	/**
	 * Checks that all mandatory variables have been set
	 */
	public void checkIndata() throws IllegalArgumentException {
		// Check that all the required parameters have been parsed
		if (!Nodes_are_set) {
			throw new IllegalArgumentException(
					"No nodes defined for Contact_Triangle element nr" + number);
		}

		if (!T_is_set) {
			throw new IllegalArgumentException(
					"No Contact sensing thickness (T) defined for Contact_Triangle element nr"
							+ number);
		}
	}

	/**
	 * This method is used to create the lines needed in the result file. The
	 * method returns a string which is printed directly. However, due to the
	 * fact that the line may be different depending on what is requested to be
	 * printed and that the number of methods should be kept down, the first
	 * parameter here is a control parameter. This parameter describes what
	 * should be printed. The second parameter is a required input when gauss
	 * point results are to be printed. Creation date: (09/12/01 %PC%T)
	 * 
	 * @param ctrl
	 *            int
	 * @param gpn
	 *            int
	 * 
	 * @return java.lang.String
	 */
	public String print_Gid(int ctrl, int gpn) {
		String out;
		int i;
		double average;

		switch (ctrl) {
		case MESH_HEADER:

			/*
			 * Print the header for the mesh. In this case, the type of element
			 * is Triangular and it uses 3 nodes
			 */
			out = new String("MESH \"MeshType" + type
					+ "\" Dimension 3 ElemType Triangle Nnode 3\n");

			return out;

		case MESH:

			/* Print the element number and connected nodes */
			out = new String(number + "\t" + node[0].getNumber() + "\t"
					+ node[1].getNumber() + "\t" + node[2].getNumber() + "\n");

			return out;

		case RESULT_HEADER:

			/*
			 * Print the header of the result file, for the block of Triangular
			 * elements. The element has one gauss point where the result is
			 * calculated. In fact, the element has up to five gauss points but
			 * GID does not support gauss points through the thickness of a
			 * Quadrilateral right now. Therefore, we will pick the middle
			 * gausspoint to show the results. The point is placed on the
			 * natural position 0,0
			 */
			out = new String("GaussPoints \"Type" + type
					+ "\" ElemType Triangle \"MeshType" + type + "\"\n");
			out += "Number Of Gauss Points: 1\n";
			out += "Nodes Not Included\n"; // There are no gauss points in the
											// nodes.
			out += "Natural Coordinates: Given\n"; // They are on the optimum
													// gauss coordinates
													// instead, which GID will
													// know by default when this
													// switch is set to
													// internal.
			out += "0.0 0.0 \n";
			out += "End GaussPoints\n";

			return out;

		case RESULT_SUB_HEADER:

			/*
			 * Print the subheader for the resultfile to initate each block of
			 * data from this element type First parameter: Kind of results ( 1=
			 * scalar, 2=vector, 3=matrix, 4=2D plane deformation matrix, 5=Main
			 * stresser, 6=Euler angles (for local axes) Second param: Location
			 * of the data (1= on the nodes, 2= in the gauss points); Third
			 * param: Will there be a description on the results button in GID?
			 * (0= Make one up GID! 1= Yes, a description will be given) Fourth
			 * param: Specify the name of the gauss point set that will be used
			 * "name"
			 */
			out = new String(" 3 2 0 \"Type" + type + "\"\n");

			return out;

		case RESULT_STRESS_LOCAL:

			/*
			 * Print the Gauss stresses for this element and the requested gauss
			 * point.
			 */
			if (gpn == 0) {
				out = new String(number + "\t 0 \t 0 \t 0 \t 0 \t 0 \t 0 \n"); // Element
																				// number
																				// must
																				// start
																				// the
																				// first
																				// gauss
																				// point
																				// results

				// The result is printed as [Sxx Syy Szz Sxy Syz Sxz]
			} else {
				out = new String("");
			}

			return out; // Nothing more to print

		case RESULT_STRAIN_LOCAL:

			/*
			 * Print the Gauss strains for this element and the requested gauss
			 * point.
			 */
			if (gpn == 0) {
				out = new String(number + "\t 0 \t 0 \t 0 \t 0 \t 0 \t 0 \n"); // Element
																				// number
																				// must
																				// start
																				// the
																				// first
																				// gauss
																				// point
																				// results

				// The result is printed as [exx eyy ezz exy eyz exz]
			} else {
				out = new String("");
			}

			return out; // Nothing more to print

		default:
			return new String("");
		}
	}

	/**
	 * This method is used to create the lines needed in the result file. The
	 * method returns a string which is printed directly. However, due to the
	 * fact that the line may be different depending on what is requested to be
	 * printed and that the number of methods should be kept down, the first
	 * parameter here is a control parameter. This parameter describes what
	 * should be printed. The second parameter is a required input when gauss
	 * point results are to be printed. Creation date: (09/12/01 %T)
	 * 
	 * @param ctrl
	 *            The control number to say if a header of result file is to be
	 *            printed.
	 * @param gpn
	 *            The gauss point number.
	 * 
	 * @return java.lang.String
	 */
	public String print_Fembic(int ctrl, int gpn) {
		String out;

		switch (ctrl) {

		case MESH:

			/* Print the element number and connected nodes */
			out = new String(number + "\t nodes = [" + node[0].getNumber()
					+ "," + node[1].getNumber() + "," + node[2].getNumber()
					+ "]\t" + "t = " + thickness + "\t");

			if (Factor_is_set)
				out += " factor = " + factor;

			if (Friction_is_set)
				out += " friction = " + friction_factor;

			out += "\n";

			return out;

		default:
			return new String("");
		}
	}

	/**
	 * The update of the area is needed for the internal contact elements used
	 * in Shell BT 4 for advanced_edge option. Otherwise, the middle node has
	 * not been updated and the contact element area gets wrong.
	 */
	public void setInitialConditions() {

		// Update local coordinate system
		this.updateLocalCoordinateSystem();

		// Determine the element Area
		this.calculateLocalVariables();
	}

	/**
	 * This method calculates the local coordinate system for the element. The
	 * method returns a handle to the system and this matrix is then stored in
	 * the local_coordinate_system for later use. The matrix can be used in the
	 * transformation of displacements and forces between the local and global
	 * coordinates. However, in this element, the transformation is made
	 * automatically in the matrix algebra of calculating the element strains
	 * (they are derived in global directions) The element is assumed to have
	 * the following node numbering y 3 I 2 I I________ x 0 1
	 */
	public synchronized void updateLocalCoordinateSystem() {
		this.calculateLocalBaseVectors(node[0].getX_pos(), node[0].getY_pos(),
				node[0].getZ_pos(), node[1].getX_pos(), node[1].getY_pos(),
				node[1].getZ_pos(), node[2].getX_pos(), node[2].getY_pos(),
				node[2].getZ_pos(), local_coordinate_system);
	}

	private void calculateLocalVariables() {
		// Update the p matrix
		trash.set(0, 0, node[0].getX_pos());
		trash.set(1, 0, node[0].getY_pos());
		trash.set(2, 0, node[0].getZ_pos());
		trash.set(3, 0, node[1].getX_pos());
		trash.set(4, 0, node[1].getY_pos());
		trash.set(5, 0, node[1].getZ_pos());
		trash.set(6, 0, node[2].getX_pos());
		trash.set(7, 0, node[2].getY_pos());
		trash.set(8, 0, node[2].getZ_pos());

		// Transform into local coordinate system
		// p.setMatrix(0,2,0,0,local_coordinate_system.times(trash.getMatrix(0,2,0,0)));
		p.set(0, 0, (local_coordinate_system.get(0, 0) * trash.get(0, 0))
				+ (local_coordinate_system.get(0, 1) * trash.get(1, 0))
				+ (local_coordinate_system.get(0, 2) * trash.get(2, 0)));
		p.set(1, 0, (local_coordinate_system.get(1, 0) * trash.get(0, 0))
				+ (local_coordinate_system.get(1, 1) * trash.get(1, 0))
				+ (local_coordinate_system.get(1, 2) * trash.get(2, 0)));
		p.set(2, 0, (local_coordinate_system.get(2, 0) * trash.get(0, 0))
				+ (local_coordinate_system.get(2, 1) * trash.get(1, 0))
				+ (local_coordinate_system.get(2, 2) * trash.get(2, 0)));

		// p.setMatrix(3,5,0,0,local_coordinate_system.times(trash.getMatrix(3,5,0,0)));
		p.set(3, 0, (local_coordinate_system.get(0, 0) * trash.get(3, 0))
				+ (local_coordinate_system.get(0, 1) * trash.get(4, 0))
				+ (local_coordinate_system.get(0, 2) * trash.get(5, 0)));
		p.set(4, 0, (local_coordinate_system.get(1, 0) * trash.get(3, 0))
				+ (local_coordinate_system.get(1, 1) * trash.get(4, 0))
				+ (local_coordinate_system.get(1, 2) * trash.get(5, 0)));
		p.set(5, 0, (local_coordinate_system.get(2, 0) * trash.get(3, 0))
				+ (local_coordinate_system.get(2, 1) * trash.get(4, 0))
				+ (local_coordinate_system.get(2, 2) * trash.get(5, 0)));

		// p.setMatrix(6,8,0,0,local_coordinate_system.times(trash.getMatrix(6,8,0,0)));
		p.set(6, 0, (local_coordinate_system.get(0, 0) * trash.get(6, 0))
				+ (local_coordinate_system.get(0, 1) * trash.get(7, 0))
				+ (local_coordinate_system.get(0, 2) * trash.get(8, 0)));
		p.set(7, 0, (local_coordinate_system.get(1, 0) * trash.get(6, 0))
				+ (local_coordinate_system.get(1, 1) * trash.get(7, 0))
				+ (local_coordinate_system.get(1, 2) * trash.get(8, 0)));
		p.set(8, 0, (local_coordinate_system.get(2, 0) * trash.get(6, 0))
				+ (local_coordinate_system.get(2, 1) * trash.get(7, 0))
				+ (local_coordinate_system.get(2, 2) * trash.get(8, 0)));

		// Create the p0 matrix
		p0.set(0, 0, p.get(0, 0));
		p0.set(1, 0, p.get(1, 0));
		p0.set(2, 0, p.get(2, 0));

		// Set centre of local coordinate system on node 1.
		p.set(3, 0, p.get(3, 0) - p.get(0, 0));
		p.set(4, 0, p.get(4, 0) - p.get(1, 0));
		p.set(5, 0, p.get(5, 0) - p.get(2, 0));
		p.set(6, 0, p.get(6, 0) - p.get(0, 0));
		p.set(7, 0, p.get(7, 0) - p.get(1, 0));
		p.set(8, 0, p.get(8, 0) - p.get(2, 0));
		p.set(0, 0, 0.0);
		p.set(1, 0, 0.0);
		p.set(2, 0, 0.0);

		// Calculate element area (area = (1/2)*X2*Y3)
		area = 0.5 * p.get(3, 0) * p.get(7, 0);

		// Calculate components in the A matrix
		A00 = (p.get(3, 0) * p.get(7, 0)) / (2 * area);
		A01 = -p.get(7, 0) / (2 * area);
		A02 = (p.get(6, 0) - p.get(3, 0)) / (2 * area);
		A11 = p.get(7, 0) / (2 * area);
		A12 = -p.get(6, 0) / (2 * area);
		A22 = p.get(3, 0) / (2 * area);
	}

	public double getArea() {
		return area;
	}

	public double getContactForce() {
		return contact_force;
	}

}

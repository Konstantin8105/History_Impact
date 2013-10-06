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
 * This is a Line contact element. Its sole purpuse is to sense contact against
 * other contact_line elements and determine reaction forces to apply to the
 * contacting element and it's own nodes. It can be used on its own or
 * incorporated into other elements.
 * 
 * @author Jonas Forssell, Yuriy Mikhaylovskiy
 * 
 * @see OtherClasses
 */
public class Contact_Line extends Element {
	private Jama.Matrix force;
	private Node n1;
	private Node n2;
	private Node n3;
	private Node n4;
	private Jama.Matrix v1;
	private Jama.Matrix v2;
	private Jama.Matrix v3;
	private Jama.Matrix P;
	private Jama.Matrix trash;
	private double radius;
	private double factor;
	private double friction_factor;
	private boolean Nodes_are_set;
	private boolean Factor_is_set;
	private boolean D_is_set;
	private boolean Friction_is_set;
	private Matrix a_b_distance;
	private double y_max;
	private double y_min;
	private double z_max;
	private double z_min;
	private double l1;
	private Vector Ftable, Ltable;

	/**
	 * Insert the method's description here. Creation date: (2001-08-10
	 * 19:45:45)
	 */
	public Contact_Line() {
		super();
		type = new String("CONTACT_LINE");

		node = new Node[2];
		force = new Jama.Matrix(3, 1);
		P = new Matrix(3, 3);
		a_b_distance = new Matrix(3, 1);
		v1 = new Matrix(3, 1);
		v2 = new Matrix(3, 1);
		v3 = new Matrix(3, 1);
		trash = new Matrix(3, 1);

		// Set the default value. These will change if the user defines
		// something.
		factor = 10;
	}

	/**
	 * Insert the method's description here. Creation date: (25/12/01 %PC%T)
	 */
	public void assembleMassMatrix() {
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
		Contact_Line c_element;
		boolean finished;
		double frictionforce;

		this.calculateLocalVariables();

		// Find out the end nodes with smallest and largest x-coordinate
		if (node[1].getX_pos() < node[0].getX_pos()) {
			smallest = node[1];
			largest = node[0];
		} else {
			smallest = node[0];
			largest = node[1];
		}

		current = smallest;

		while ((current.getLeft_neighbour() != null)
				&& (current.getLeft_neighbour().getX_pos() > (smallest
						.getX_pos() - radius))) {
			current = current.getLeft_neighbour();
		}

		smallest = current;

		current = largest;

		while ((current.getRight_neighbour() != null)
				&& (current.getRight_neighbour().getX_pos() < (largest
						.getX_pos() + radius))) {
			current = current.getRight_neighbour();
		}

		largest = current;

		current = smallest;

		// Determine max and min coordinates of the element
		y_min = Math.min(node[0].getY_pos(), node[1].getY_pos()) - radius;
		y_max = Math.max(node[0].getY_pos(), node[1].getY_pos()) + radius;
		z_min = Math.min(node[0].getZ_pos(), node[1].getZ_pos()) - radius;
		z_max = Math.max(node[0].getZ_pos(), node[1].getZ_pos()) + radius;

		// Check nodes from smallest to largest
		finished = false;

		while (!finished) {
			i = 0;

			// Check nodal contact
			if (this.isInContact(current)) {
				current.addContactForce(v3.times(factor
						* (1 - (a_b_distance.get(2, 0) / radius))));
				n1.addContactForce(v3.times(-factor
						* (1 - (a_b_distance.get(2, 0) / radius))
						* (1 - a_b_distance.get(0, 0))));
				n2.addContactForce(v3.times(-factor
						* (1 - (a_b_distance.get(2, 0) / radius))
						* a_b_distance.get(0, 0)));

				// Now, add friction if requested
				if (Friction_is_set == true) {

					frictionforce = this.addFriction(current,
							a_b_distance.get(0, 0));

					current.addContactForce(v1.times(factor
							* (1 - (a_b_distance.get(2, 0) / radius))
							* frictionforce));

					n1.addContactForce(v1.times(-0.5 * factor
							* (1 - (a_b_distance.get(2, 0) / radius))
							* frictionforce));

					n2.addContactForce(v1.times(-0.5 * factor
							* (1 - (a_b_distance.get(2, 0) / radius))
							* frictionforce));

				}

			}

			// Now, check line contact with all lines connected to this node.
			c_element = current.getContact_Line(i);

			while (c_element != null) {
				n3 = c_element.getNode(0);
				n4 = c_element.getNode(1);

				// Check for contact
				if (this.isInContact(c_element)) {
					// We are in contact.
					// Calculate the force and add to the nodes. The force is
					// directed along the v3 axis.
					n1.addContactForce(v3.times(-1
							* (1 - a_b_distance.get(0, 0)) * factor
							* (1 - (a_b_distance.get(2, 0) / radius))));
					n2.addContactForce(v3.times(-1 * a_b_distance.get(0, 0)
							* factor * (1 - (a_b_distance.get(2, 0) / radius))));
					n3.addContactForce(v3.times((1 - a_b_distance.get(1, 0))
							* factor * (1 - (a_b_distance.get(2, 0) / radius))));
					n4.addContactForce(v3.times(a_b_distance.get(1, 0) * factor
							* (1 - (a_b_distance.get(2, 0) / radius))));

					// Now, add friction if requested
					if (Friction_is_set == true) {

						frictionforce = this.addFriction(c_element,
								a_b_distance.get(0, 0));

						n1.addContactForce(v1.times(-0.5 * factor
								* (1 - (a_b_distance.get(2, 0) / radius))
								* frictionforce));

						n2.addContactForce(v1.times(-0.5 * factor
								* (1 - (a_b_distance.get(2, 0) / radius))
								* frictionforce));

						n3.addContactForce(v1.times((1 - a_b_distance.get(1, 0))
								* factor
								* (1 - (a_b_distance.get(2, 0) / radius))
								* frictionforce));

						n4.addContactForce(v1.times(a_b_distance.get(1, 0)
								* factor
								* (1 - (a_b_distance.get(2, 0) / radius))
								* frictionforce));

					}

				}

				// Now, check next element attached to the node
				i++;
				c_element = current.getContact_Line(i);
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
			for (i = 0; i < Ltable.size(); i++) {
				if (((Ldata) Ltable.elementAt(i)).checked == false) {
					Ltable.removeElementAt(i);
					i--;
				}
			}

			// Set the remaining ones to unchecked status.
			for (i = 0; i < Ftable.size(); i++) {
				((Fdata) Ftable.elementAt(i)).checked = false;
			}

			for (i = 0; i < Ltable.size(); i++) {
				((Ldata) Ltable.elementAt(i)).checked = false;
			}

		}

	}

	private boolean isInContact(Node c_node) {
		double a;

		// Skip own node
		if (c_node.equals(n1) || c_node.equals(n2)) {
			return false;
		}

		// Skip nodes outside the element "box"
		if ((c_node.getY_pos() < y_min) || (c_node.getY_pos() > y_max)) {
			return false;
		}

		if ((c_node.getZ_pos() < z_min) || (c_node.getZ_pos() > z_max)) {
			return false;
		}

		// Compute distance
		v2 = c_node.getPos().minus(n1.getPos());

		// v3 = v1.vectorProduct(v2);
		v3.set(0, 0,
				(v1.get(1, 0) * v2.get(2, 0)) - (v1.get(2, 0) * v2.get(1, 0)));
		v3.set(1, 0,
				(v1.get(2, 0) * v2.get(0, 0)) - (v1.get(0, 0) * v2.get(2, 0)));
		v3.set(2, 0,
				(v1.get(0, 0) * v2.get(1, 0)) - (v1.get(1, 0) * v2.get(0, 0)));

		a_b_distance.set(2, 0, v3.length() / l1);

		// Skip if outside distance
		if (a_b_distance.get(2, 0) > radius) {
			return false;
		}

		// Do the detailed computing
		// v3 = v3.vectorProduct(v1);
		trash.set(0, 0,
				(v3.get(1, 0) * v1.get(2, 0)) - (v3.get(2, 0) * v1.get(1, 0)));
		trash.set(1, 0,
				(v3.get(2, 0) * v1.get(0, 0)) - (v3.get(0, 0) * v1.get(2, 0)));
		trash.set(2, 0,
				(v3.get(0, 0) * v1.get(1, 0)) - (v3.get(1, 0) * v1.get(0, 0)));
		v3.set(0, 0, trash.get(0, 0));
		v3.set(1, 0, trash.get(1, 0));
		v3.set(2, 0, trash.get(2, 0));

		if (v3.length() != 0) {
			v3.timesEquals(1 / v3.length());
		}

		v2 = v2.minus(v3.times(a_b_distance.get(2, 0)));

		// And determine a
		a = v2.length() / l1;

		// Figure out if vector is going the other way
		v2 = v1.plus(v2);

		if (v2.length() < l1) {
			a = -a;
		}

		/*
		 * Check if contact node is outside the endpoints. If this is the case,
		 * make a direct radial vector and recalculate the distance to eliminate
		 * nodes that "slip in" from the end side
		 */
		if (a < 0) {
			v3 = c_node.getPos().minus(n1.getPos());
			a_b_distance.set(2, 0, v3.length());

			if (a_b_distance.get(2, 0) > radius) {
				return false;
			}

			v3.timesEquals(1 / a_b_distance.get(2, 0));
			a = 0;
		} else if (a > 1) {
			v3 = c_node.getPos().minus(n2.getPos());
			a_b_distance.set(2, 0, v3.length());

			if (a_b_distance.get(2, 0) > radius) {
				return false;
			}

			v3.timesEquals(1 / a_b_distance.get(2, 0));
			a = 1;
		}

		a_b_distance.set(0, 0, a);

		return true;
	}

	/**
	 * Checks if node is in contact with the element
	 */
	private boolean isInContact(Contact_Line cl) {
		double area;

		// Skip own element
		if (cl.equals(this)) {
			return false;
		}

		// Skip nodes outside the element "box"
		if ((n3.getY_pos() < y_min) && (n4.getY_pos() < y_min)) {
			return false;
		}

		if ((n3.getY_pos() > y_max) && (n4.getY_pos() > y_max)) {
			return false;
		}

		if ((n3.getZ_pos() < z_min) && (n4.getZ_pos() < z_min)) {
			return false;
		}

		if ((n3.getZ_pos() > z_max) && (n4.getZ_pos() > z_max)) {
			return false;
		}

		// Skip c_elements directly connected to this element
		if (n3.equals(n1) || n3.equals(n2)) {
			return false;
		}

		if (n4.equals(n1) || n4.equals(n2)) {
			return false;
		}

		// The element is crossing this element.
		// Calculate a basic vector
		v2 = n4.getPos().minus(n3.getPos());

		// Calculate the crossing direction
		// v3 = v1.vectorProduct(v2,v3);
		v3.set(0, 0,
				(v1.get(1, 0) * v2.get(2, 0)) - (v1.get(2, 0) * v2.get(1, 0)));
		v3.set(1, 0,
				(v1.get(2, 0) * v2.get(0, 0)) - (v1.get(0, 0) * v2.get(2, 0)));
		v3.set(2, 0,
				(v1.get(0, 0) * v2.get(1, 0)) - (v1.get(1, 0) * v2.get(0, 0)));

		area = v3.length();

		// Check if parallel
		if (area < 1E-15) {
			return false;
		}

		// The lines are not parallel. Proceed as usual
		// Normalize the v3 vector in length
		v3.timesEquals(1 / area);

		// A crossing point exists.
		// Fill in on the P matrix
		P.setMatrix(0, 2, 1, 1, v2.times(-1));
		P.setMatrix(0, 2, 2, 2, v3);

		// Calculate the values
		a_b_distance = P.inverse().times(n3.getPos().minus(n1.getPos()));

		// Check if the crossing point is within the lines
		if ((a_b_distance.get(0, 0) < 0) || (a_b_distance.get(0, 0) > 1)) {
			return false;
		}

		if ((a_b_distance.get(1, 0) < 0) || (a_b_distance.get(1, 0) > 1)) {
			return false;
		}

		// Check if the distance is too great
		if (Math.abs(a_b_distance.get(2, 0)) > radius) {
			return false;
		}

		// No, this is definately in contact
		if (a_b_distance.get(2, 0) < 0) {
			a_b_distance.set(2, 0, Math.abs(a_b_distance.get(2, 0)));
			v3.timesEquals(-1);
		}

		return true;
	}

	/**
	 * addFriction(Node node) This method will check the given node against a
	 * local database to determine if any sliding has occurred against the
	 * element. If so, a vector will be calculated and a friction force
	 * determined.
	 */
	private double addFriction(Node contact_node, double qa) {
		Fdata tmp;
		int i;
		double vec2;

		// Check if node is present in Ftable already
		for (i = 0; i < Ftable.size(); i++) {
			if (((Fdata) Ftable.elementAt(i)).cnode.equals(contact_node)) {
				break;
			}
		}

		if (i == Ftable.size()) {
			// This is first time. Add node to the table.
			// qa is the fraction describing contact point along the element.
			Ftable.add(new Fdata(contact_node, qa));

			// And do nothing more
			return 0;
		}

		// Getting here means node is present. See if a vector has been set
		tmp = (Fdata) Ftable.elementAt(i);

		if (tmp.vec_is_set == false) {
			// Calculate the vector
			tmp.vecX = qa - tmp.q;

			// Set the new node position
			tmp.q = qa;

			// Set the vector
			tmp.vec_is_set = true;

			// Notify action has been done
			tmp.checked = true;

			// Check if there has been any conciderable movement
			if (Math.abs(tmp.vecX) < 1E-15) {
				return 0;
			}

			// Add the friction force (with a ramp up).
			return ((tmp.vecX > 0) ? (-0.5) : 0.5) * friction_factor / l1;
		}

		// Getting here means node is present and a vector has been set
		// Make a temporary vector

		vec2 = tmp.vecX;

		// Update the vector
		tmp.vecX = qa - tmp.q;

		// Set the new node position
		tmp.q = qa;

		// Set the vector
		tmp.vec_is_set = true;

		// Notify action has been done
		tmp.checked = true;

		// Check if there has been any conciderable movement
		if (Math.abs(tmp.vecX) < 1E-15) {
			return 0;
		}

		// Add the friction force (with a ramp up if new vector has opposite
		// direction to prevent oscillation)
		return ((tmp.vecX > 0) ? (-1) : 1)
				* ((Math.abs(tmp.vecX + vec2) > Math.abs(vec2)) ? 1.0 : 0.5)
				* friction_factor / l1;

	}

	/**
	 * addFriction(Contact_line contact_line) This method will check the given
	 * lnie against a local database to determine if any sliding has occurred
	 * against the element. If so, a vector will be calculated and a friction
	 * force determined.
	 */
	private double addFriction(Contact_Line contact_line, double qa) {
		Ldata tmp;
		int i;
		double vec2;

		// Check if node is present in Ftable already
		for (i = 0; i < Ltable.size(); i++) {
			if (((Ldata) Ltable.elementAt(i)).cline.equals(contact_line)) {
				break;
			}
		}

		if (i == Ltable.size()) {
			// This is first time. Add line to the table.
			// qa is the fraction describing contact point along the element.
			Ltable.add(new Ldata(contact_line, qa));

			// And do nothing more
			return 0;
		}

		// Getting here means line is present. See if a vector has been set
		tmp = (Ldata) Ltable.elementAt(i);

		if (tmp.vec_is_set == false) {
			// Calculate the vector
			tmp.vec = qa - tmp.q;

			// Set the new contact point position
			tmp.q = qa;

			// Set the vector
			tmp.vec_is_set = true;

			// Notify action has been done
			tmp.checked = true;

			// Check if there has been any conciderable movement
			if (Math.abs(tmp.vec) < 1E-15) {
				return 0;
			}

			// Add the friction force (with a ramp up).
			return ((tmp.vec > 0) ? (-0.5) : 0.5) * friction_factor / l1;
		}

		// Getting here means line is present and a vector has been set
		// Make a temporary vector

		vec2 = tmp.vec;

		// Update the vector
		tmp.vec = qa - tmp.q;

		// Set the new contact point position
		tmp.q = qa;

		// Set the vector
		tmp.vec_is_set = true;

		// Notify action has been done
		tmp.checked = true;

		// Check if there has been any conciderable movement
		if (Math.abs(tmp.vec) < 1E-15) {
			return 0;
		}

		// Add the friction force (with a ramp up if new vector has opposite
		// direction to prevent oscillation)
		return ((tmp.vec > 0) ? (-1) : 1)
				* ((Math.abs(tmp.vec + vec2) > Math.abs(vec2)) ? 1.0 : 0.5)
				* friction_factor / l1;

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
	 * This element only has no integration point, but returns one since it
	 * needs to calculate at least one loop. Creation date: (26/12/01 %PC%T)
	 * 
	 * @return int
	 */
	public int getNumberOfIntegrationPoints() {
		return 1;
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
		int j;
		int i = 0;
		while (i < param.length) {
			// The nodes of the element are defined
			if (param[i].getw().toUpperCase().equals("NODES")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// Assume now that the nodes are delivered in param3, with the
				// format
				// [nodenr,nodenr]
				if (!param[i + 2].getw().toUpperCase().startsWith("[")
						|| !param[i + 2].getw().toUpperCase().endsWith("]")) {
					throw new java.text.ParseException(
							"Error, node number definition should be [nodenr1,nodenr2,nodenr3,nodenr4]",
							lineno);
				}

				// Ok, now find the numbers
				try {
					for (j = 0; j < 2; j++) {
						node[j] = super
								.findNode(
										super.getNodeNumber(j + 1,
												param[i + 2].getw()), nodetable);

						// Now, add this element to the handle list in each node
						// for later reference
						// in the contact search algorithm
						node[j].addContact_Line(this);
					}

					i += 3;
					Nodes_are_set = true;
				} catch (IllegalArgumentException e) {
					throw new java.text.ParseException(e.getMessage()
							+ " in line ", lineno);
				}
			} else
			// The contact force factor is set
			if (param[i].getw().toUpperCase().equals("FACTOR")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the force
				factor = param[i + 2].getn();
				i += 3;
				Factor_is_set = true;
			} else
			// The contact friction is set
			if (param[i].getw().toUpperCase().equals("FRICTION")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the force
				friction_factor = param[i + 2].getn();
				i += 3;
				Friction_is_set = true;

				// Friction is defined. Now, set up a friction table for this
				// element.
				Ftable = new Vector();
				Ltable = new Vector();

			} else
			// The radius of the line is defined
			if (param[i].getw().toUpperCase().equals("D")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the element radius is in param3. Set this in the
				// element
				radius = 0.5 * param[i + 2].getn();
				i += 3;
				D_is_set = true;
			} else {
				// Neither material or nodes are defined. Then the parameter is
				// wrong.
				throw new java.text.ParseException(
						"Unknown Contact_Line element parameter ", lineno);
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
					"No nodes defined for Contact_Line element nr" + number);
		}

		if (!D_is_set) {
			throw new IllegalArgumentException(
					"No Contact sensing diameter (D) defined for Contact_Line element nr"
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
		switch (ctrl) {
		case MESH_HEADER:

			/*
			 * Print the header for the mesh. In this case, the type of element
			 * is Triangular and it uses 3 nodes
			 */
			out = new String("MESH \"MeshType" + type
					+ "\" Dimension 3 ElemType Linear Nnode 2\n");

			return out;

		case MESH:

			/* Print the element number and connected nodes */
			out = new String(number + "\t" + node[0].getNumber() + "\t"
					+ node[1].getNumber() + "\n");

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
					+ "\" ElemType Linear \"MeshType" + type + "\"\n");
			out += "Number Of Gauss Points: 1\n";
			out += "Nodes Not Included\n"; // There are no gauss points in the
											// nodes.
			out += "Natural Coordinates: Internal\n"; // They are on the optimum
														// gauss coordinates
														// instead, which GID
														// will know by default
														// when this switch is
														// set to internal.
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
			out = new String(" 1 2 0 \"Type" + type + "\"\n");

			return out;

		case RESULT_STRESS_LOCAL:

			/*
			 * Print the Gauss stresses for this element and the requested gauss
			 * point.
			 */
			if (gpn == 0) {
				out = new String(number + " 0.0\n"); // Element number must
														// start the first gauss
														// point results
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
				out = new String(number + " 0.0\n"); // Element number must
														// start the first gauss
														// point results
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
			out = new String(number + "\t  nodes = [" + node[0].getNumber()
					+ "," + node[1].getNumber() + "]\t" + "D = " + 2 * radius
					+ "\t");

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
	 * Insert the method's description here. Creation date: (25/12/01 %PC%T)
	 */
	public void setInitialConditions() {
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
	}

	private void calculateLocalVariables() {
		// Update the p matrix
		n1 = node[0];
		n2 = node[1];

		// Own vector
		v1 = n2.getPos().minus(n1.getPos());

		// vector length
		l1 = v1.length();

		// The P matrix
		P.setMatrix(0, 2, 0, 0, v1);
	}

	public Node getNode(int nr) {
		return node[nr];
	}

}

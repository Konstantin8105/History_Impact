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

import run.*;

import java.util.Hashtable;

/**
 * Insert the type's description here. Creation date: (08/09/01 %T)
 * 
 * @author Jonas Forssell, Yuriy Mikhaylovskiy
 */
public class Rod_2 extends Element {
	private static int DISABLED = 0;
	private static int BASIC = 1;
	private double cross_section_area;
	private Material material;
	private Jama.Matrix local_coordinate_system;
	private double diameter;
	private double initial_length;
	private Jama.Matrix inertia;
	private Jama.Matrix strain;
	private Jama.Matrix stress;
	private Jama.Matrix force;
	private Jama.Matrix dstrain;
	private double initial_cross_section_area;
	private double factor, friction;
	private boolean D_is_set;
	private boolean Nodes_are_set;
	private boolean Material_is_set;
	private boolean Factor_is_set, Friction_is_set;
	private Contact_Line internal_contact_element;
	private int Contact;

	/**
	 * This is the Rod element. A simple element without moment capacity.
	 * However, it is a good template to look at in order to understand the
	 * buildup of an element.
	 */
	public Rod_2() {
		super();
		type = new String("ROD_2");

		inertia = new Jama.Matrix(3, 3);
		force = new Jama.Matrix(3, 1);
		stress = new Jama.Matrix(6, 1);
		strain = new Jama.Matrix(6, 1);
		dstrain = new Jama.Matrix(6, 1);
		local_coordinate_system = new Jama.Matrix(3, 3);
		processed = false;
		node = new Node[2];

		// Enable contact by default
		Contact = BASIC;
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
	 * before adding to the node. Some simplifications has been made though as
	 * seen later. Creation date: (10/5/2001 3:32:57 PM)
	 */
	public void assembleMassMatrix() throws IllegalArgumentException {
		double mass;

		// Calculate element mass distribution
		initial_cross_section_area = (Math.PI * diameter * diameter) / 4;
		cross_section_area = initial_cross_section_area;

		// Check area
		if (cross_section_area <= 0) {
			throw new IllegalArgumentException("Error in rod element " + number
					+ ", Cross section area is zero or negative");
		}

		//
		initial_length = java.lang.Math.sqrt(((node[0].getX_pos() - node[1]
				.getX_pos()) * (node[0].getX_pos() - node[1].getX_pos()))
				+ ((node[0].getY_pos() - node[1].getY_pos()) * (node[0]
						.getY_pos() - node[1].getY_pos()))
				+ ((node[0].getZ_pos() - node[1].getZ_pos()) * (node[0]
						.getZ_pos() - node[1].getZ_pos())));
		mass = material.getDensity() * cross_section_area * initial_length;

		// Check mass
		if (mass <= 0) {
			throw new IllegalArgumentException("Error in rod element " + number
					+ ". Element mass is zero or negative.\nElement length: "
					+ initial_length + "   Material density: "
					+ material.getDensity());
		}

		// Distribute the mass to the connected nodes. Half mass on each node.
		node[0].addMass(mass / 2.0);
		node[1].addMass(mass / 2.0);

		/*
		 * Now, calculate the element local inertia For a rod, there are two
		 * different inertias; 1. Rotational inertia around the center
		 * longitudinal axis (m*d2/8) 2. Rotational inertia around the middle
		 * point of the bar. Any centripetal axis (m*l2/12).
		 * 
		 * It has been determined that inertia applied to nodes has limited
		 * influence and that some simplifications can be made. In this case,
		 * the largest inertia will be kept and applied to all directions.
		 * Couplings will be set to zero. This simplification is also used in
		 * the other elements. 2005-09-17 version 0.6.3 - JF
		 */

		inertia.set(
				0,
				0,
				Math.max((mass * diameter * diameter) / 8, (mass
						* initial_length * initial_length) / 12));
		inertia.set(1, 1, inertia.get(0, 0));
		inertia.set(2, 2, inertia.get(0, 0));
		inertia.set(1, 0, 0);
		inertia.set(2, 0, 0);
		inertia.set(0, 1, 0);
		inertia.set(0, 2, 0);
		inertia.set(2, 1, 0);
		inertia.set(1, 2, 0);

		node[0].addInertia(inertia);
		node[1].addInertia(inertia);

		//
		if (Contact == BASIC) {
			internal_contact_element.assembleMassMatrix();
		}
	}

	/**
	 * This is a biggie. The contact forces are calculated here. This is done in
	 * the following way: Let this element check it's segment(s) against any
	 * neighbouring nodes. calculate distance from segment to node if distance
	 * &lt; tolerance then reactionforce = f(tolerance) add reactionforce to
	 * node & element nodes Continue this check until all the nodes within the
	 * contact tolerance has been checked. Creation date: (2001-10-26 01.06.35)
	 */
	public void calculateContactForces() {
		// Use the internal contact element to calculate the contact forces.
		if (Contact == BASIC) {
			internal_contact_element.calculateContactForces();
		}
	}

	/**
	 * Here, the contribution from gravity and external forces on ELEMENTS are
	 * contributed for. Creation date: (2001-10-26 01.01.21)
	 */
	public void calculateExternalForces(double currtime) {
		// Calculate gravity force here.
		// convert and add to node.
		// Calculate element loads here
		// convert and add to node.
		//
		if (Contact == BASIC) {
			internal_contact_element.calculateExternalForces(currtime);
		}
	}

	/**
	 * This calculates and adds the element internal forces to the nodes. Note,
	 * by definition, the internal forces are subtracted from the external loads
	 * Thus, the "addition" is negative. Creation date: (2001-10-23 14.03.06)
	 * 
	 */
	public void calculateNodalForces(int integration_point, double timestep) {
		Jama.Matrix global_force;

		// Forces
		force.set(0, 0, stress.get(0, 0) * cross_section_area);
		force.set(1, 0, 0);
		force.set(2, 0, 0);

		// Transform to global coordinates
		global_force = local_coordinate_system.transpose().times(force);

		// Add this force contribution to the node
		node[0].addInternalForce(global_force.times(1));
		node[1].addInternalForce(global_force.times(-1));
	}

	/**
	 * This method calculates the true strain of the rod. Creation date:
	 * (2001-10-23 00.52.18) Jonas Forssell
	 * 
	 * @exception java.lang.Error
	 *                The exception description.
	 */
	public void calculateStrain(double timestep, int integration_point) {
		double xpos1;
		double ypos1;
		double zpos1;
		double xpos2;
		double ypos2;
		double zpos2;
		double new_length;

		//
		xpos1 = node[0].getX_pos();
		ypos1 = node[0].getY_pos();
		zpos1 = node[0].getZ_pos();

		//
		xpos2 = node[1].getX_pos();
		ypos2 = node[1].getY_pos();
		zpos2 = node[1].getZ_pos();

		//
		new_length = java.lang.Math.sqrt(((xpos2 - xpos1) * (xpos2 - xpos1))
				+ ((ypos2 - ypos1) * (ypos2 - ypos1))
				+ ((zpos2 - zpos1) * (zpos2 - zpos1)));

		// True strain is calculated as strain = ln(1+(l-l0)/l0)
		dstrain.set(0, 0,
				Math.log(1 + ((new_length - initial_length) / initial_length))
						- strain.get(0, 0));

		// Calculate also the new cross section area (assuming incompressible
		// material)
		cross_section_area = (initial_cross_section_area * initial_length)
				/ new_length;
	}

	/**
	 * This method calculates the stress of the element. In most cases, this is
	 * a matrix. Here, it is only one value, which makes things easier. Creation
	 * date: (2001-10-23 13.51.34)
	 * 
	 * @exception java.lang.Error
	 *                The exception description.
	 */
	public void calculateStress(int integration_point, double timestep) {
		// Now let the material law do its job and update them
		material.calculateStressOneDimensional(strain, dstrain, stress,
				timestep);
	}

	/**
	 * This method calculates the timestep for the element and returns the
	 * smallest of the current timestep and the element timestep since it is the
	 * smallest element that determines the stability of the solution. Creation
	 * date: (10/12/01 %T)
	 * 
	 * @param current_timestep
	 *            double
	 * 
	 * @return double
	 */
	public double checkTimestep(double current_timestep) {
		double timestep;
		double critical_length;
		double xpos1;
		double ypos1;
		double zpos1;
		double xpos2;
		double ypos2;
		double zpos2;

		//
		xpos1 = node[0].getX_pos();
		ypos1 = node[0].getY_pos();
		zpos1 = node[0].getZ_pos();

		//
		xpos2 = node[1].getX_pos();
		ypos2 = node[1].getY_pos();
		zpos2 = node[1].getZ_pos();

		//
		critical_length = java.lang.Math
				.sqrt(((xpos2 - xpos1) * (xpos2 - xpos1))
						+ ((ypos2 - ypos1) * (ypos2 - ypos1))
						+ ((zpos2 - zpos1) * (zpos2 - zpos1)));

		/*
		 * Calculate critical timestep using the Courant condition timestep <=
		 * L/c L= element length c= aucustic wave speed = sqrt(E/density)
		 */
		timestep = critical_length / material.wavespeedOneDimensional(0.0, 0.0);

		return Math.min(0.9 * timestep, current_timestep);
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-22
	 * 23.50.50)
	 * 
	 * @return double
	 */
	private double getI1() {
		return inertia.get(0, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-22
	 * 23.50.59)
	 * 
	 * @return double
	 */
	private double getI2() {
		return inertia.get(1, 1);
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-22
	 * 23.50.50)
	 * 
	 * @return double
	 */
	private double getI3() {
		return inertia.get(2, 2);
	}

	/**
	 * This element only has one integration point (gauss point) Creation date:
	 * (26/12/01 %T)
	 * 
	 * @return int
	 */
	public int getNumberOfIntegrationPoints() {
		return 1;
	}

	/**
	 * Returns the local force of the rod.
	 * 
	 * @return double
	 */
	public double getForce() {
		return force.get(0, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (14/09/01 %T)
	 * 
	 * @param param1
	 *            java.lang.String
	 * @param param2
	 *            java.lang.String
	 * @param param4
	 *            double
	 * @param lineno
	 *            int
	 * @param nodelist
	 *            java.util.Vector
	 * @param materiallist
	 *            java.util.Vector
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public void parse_Fembic(Token[] param, int lineno, RplVector nodelist,
			RplVector materiallist, RplVector loadlist, Hashtable nodetable)
			throws java.text.ParseException {
		int nodenumber;
		int i = 0;
		Token[] contact_input;

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
							"Error, node number definition should be [nodenr,nodenr]",
							lineno);
				}

				// Ok, now find the numbers
				try {
					nodenumber = super.getNodeNumber(1, param[i + 2].getw());

					// We now have the node number, but want to have the handle
					// to the node object.
					node[0] = super.findNode(nodenumber, nodetable);

					// And repeat for second node
					nodenumber = super.getNodeNumber(2, param[i + 2].getw());
					node[1] = super.findNode(nodenumber, nodetable);
					i += 3;
					Nodes_are_set = true;
				} catch (IllegalArgumentException e) {
					throw new java.text.ParseException(
							"Error in Rod_2 element\n" + e.getMessage()
									+ "in line ", lineno);
				}
			} else
			// The material of the element is defined
			if (param[i].getw().toUpperCase().equals("MATERIAL")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// Assume now that the material name is delivered in param3
				try {
					// We want the handle to the material object.
					material = super.findMaterial(param[i + 2].getw()
							.toUpperCase(), materiallist);
					i += 3;
					Material_is_set = true;
				} catch (IllegalArgumentException e) {
					throw new java.text.ParseException(e.getMessage(), lineno);
				}
			} else
			// The cross section area of the element is defined
			if (param[i].getw().toUpperCase().equals("D")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in param4. Set this in
				// the element
				diameter = param[i + 2].getn();
				i += 3;
				D_is_set = true;
			} else
			// The factor for the internal contact element is defined
			if (param[i].getw().toUpperCase().equals("FACTOR")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the element thickness is in param3. Set this in
				// the element
				factor = param[i + 2].getn();
				i += 3;
				Factor_is_set = true;
			} else
			// The friction for the internal contact element is defined
			if (param[i].getw().toUpperCase().equals("FRICTION")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the element friction is in param3. Set this in
				// the element
				friction = param[i + 2].getn();
				i += 3;
				Friction_is_set = true;
			} else
			// The contact element of the shell is specified
			if (param[i].getw().toUpperCase().equals("CONTACT")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the element thickness is in param3. Set this in
				// the element
				if (param[i + 2].getw().toUpperCase().equals("OFF")) {
					Contact = DISABLED;
				} else {
					throw new java.text.ParseException(
							"Unrecognized contact parameter", lineno);
				}

				i += 3;
			} else {
				// Neither material or nodes are defined. Then the parameter is
				// wrong.
				throw new java.text.ParseException(
						"Unknown Rod element parameter ", lineno);
			}
		}

		// Make a local instance of a contact element to handle the contacts
		if (Contact == BASIC) {
			internal_contact_element = new Contact_Line();
			internal_contact_element.setNumber(-1);

			i = 6;

			// Make the input for the contact element
			if (Factor_is_set)
				i += 3;
			if (Friction_is_set)
				i += 3;

			contact_input = new Token[i];

			contact_input[0] = new Token(new String("nodes"));
			contact_input[1] = new Token(new String("="));
			contact_input[2] = new Token(new String("[" + node[0].getNumber()
					+ "," + node[1].getNumber() + "]"));
			contact_input[3] = new Token(new String("D"));
			contact_input[4] = new Token(new String("="));
			contact_input[5] = new Token(diameter);

			i = 6;

			if (Factor_is_set) {
				contact_input[i] = new Token(new String("factor"));
				contact_input[i + 1] = new Token(new String("="));
				contact_input[i + 2] = new Token(factor);
				i += 3;
			}

			if (Friction_is_set) {
				contact_input[i] = new Token(new String("friction"));
				contact_input[i + 1] = new Token(new String("="));
				contact_input[i + 2] = new Token(friction);
				i += 3;
			}

			// Now, set the input for the contact element also
			internal_contact_element.parse_Fembic(contact_input, lineno,
					nodelist, materiallist, loadlist, nodetable);
		}
	}

	public void checkIndata() throws IllegalArgumentException {
		// Check that all the required parameters have been parsed
		if (!D_is_set) {
			throw new IllegalArgumentException(
					"No Diameter defined for Rod element nr" + number);
		}

		if (!Nodes_are_set) {
			throw new IllegalArgumentException(
					"No nodes defined for Rod element nr" + number);
		}

		if (!Material_is_set) {
			throw new IllegalArgumentException(
					"No Material defined for Rod element nr" + number);
		}

		//
		if (Contact == BASIC) {
			internal_contact_element.checkIndata();
		}
	}

	/**
	 * 
	 * The method checks destroyed element or not. If the element is destroyed
	 * variable failed = true else failed = false.
	 * 
	 */

	public void checkIfFailed() {
		if (!material.failureStrainIsSet() && !material.failureStressIsSet()) {
			failed = false;
			return;
		}

		if (material.failureStressIsSet()) {
			if (stress.get(0, 0) > material.getFailureStress()) {
				failed = true;
				return;
			}
		}

		if (material.failureStrainIsSet()) {
			if (strain.get(0, 0) > material.getFailureStrain()) {
				failed = true;
				return;
			}
		}

		failed = false;
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
			 * is Linear and it a Rod uses 2 nodes
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
			 * Print the header of the result file, for the block of Rod
			 * elements
			 */
			out = new String("GaussPoints \"Type" + type
					+ "\" ElemType Linear \"MeshType" + type + "\"\n");
			out += "Number Of Gauss Points: 1\n";
			out += "Nodes Not Included\n";
			out += "Natural Coordinates: Internal\n";
			out += "End GaussPoints\n";

			return out;

		case RESULT_SUB_HEADER:

			/*
			 * Print the subheader for the resultfile to initate each block of
			 * data from this element type
			 */
			out = new String(" 1 2 0 \"Type" + type + "\"\n");

			return out;

		case RESULT_STRESS_LOCAL:

			/*
			 * Print the Gauss stresses for this element, remembering the
			 * previous line index
			 */
			out = new String(number + " ");
			out += (stress.get(0, 0) + "\n");

			return out;

		case RESULT_STRAIN_LOCAL:

			/*
			 * Print the Gauss stresses for this element, remembering the
			 * previous line index
			 */
			out = new String(number + " ");
			out += (strain.get(0, 0) + "\n");

			return out;

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
					+ "," + node[1].getNumber() + "]\t" + "D = " + diameter
					+ "\t" + "material = " + material.getName() + "\t");

			if (Factor_is_set)
				out += " factor = " + factor;

			if (Friction_is_set)
				out += " friction = " + friction;

			if (Contact == DISABLED)
				out += " contact = OFF";

			out += "\n";

			return out;

		default:
			return new String("");
		}
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public void setInitialConditions() throws IllegalArgumentException {
		/*
		 * Make a local copy of the material object and keep it inside the
		 * element. The reason for this is twofold. 1. The material law is now
		 * tied to the element and will remember the element properties and
		 * history 2. Since the material is cloned now, after Initialize, all
		 * the parameters are defined in the material object and are now
		 * automatically transferred to the local copy.
		 */
		try {
			material = (Material) material.copy();
		} catch (CloneNotSupportedException e) {
			System.err.println("Object cannot clone");
		}

		// Now call any necessary initialisations in the law.
		material.setInitialConditions();

		// Initalize the contact element also
		if (Contact == BASIC) {
			internal_contact_element.setInitialConditions();
		}
	}

	/**
	 * This method sets up the local coordinate system for the element. The
	 * local x-axis will run from node 1 to node 2. The local y-axis will be in
	 * the plane given by the local x-axis and the point formed by the
	 * coordinates of node 1, but with and offset of 1 mm in x and y direction.
	 * The local z-axis will be orthogonal to the same plane. In the case of a
	 * rod, the directions of the y-and z-axis are really not that important
	 * since it is symmetric. The local system is unified in length. The forces
	 * and inertias can now be expresses relative to this system and then
	 * transformed Creation date: (19/11/01 %T)
	 */
	public void updateLocalCoordinateSystem() {
		this.calculateLocalBaseVectors(0, 0, 0,
				node[1].getX_pos() - node[0].getX_pos(), node[1].getY_pos()
						- node[0].getY_pos(),
				node[1].getZ_pos() - node[0].getZ_pos(), node[1].getZ_pos()
						- node[0].getZ_pos(),
				node[0].getX_pos() - node[1].getX_pos(), node[1].getY_pos()
						- node[0].getY_pos(), local_coordinate_system);

		// Update local coordiante system on the contact element
		if (Contact == BASIC) {
			internal_contact_element.updateLocalCoordinateSystem();
		}
	}

	/**
	 * Is used to deactivate the element when fractured.
	 */
	public void deActivate() {
		super.deActivate();
		if (internal_contact_element != null)
			internal_contact_element.deActivate();
	}

}

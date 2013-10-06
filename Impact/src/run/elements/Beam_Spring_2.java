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
import Jama.Matrix;

/**
 * The BeamSpring element is a spring with beam properties. The element accept
 * both axial and torsional spring properties and is useful to model spotwelds.
 * It can also be used to model joints by releasing some degrees of freedom. The
 * elment requires a local coordinate system to be defined by using other nodes
 * as directional guidance.
 * 
 * @author Jonas Forssell, Yuriy Mikhaylovskiy
 * 
 */
public class Beam_Spring_2 extends Element {
	private static int DISABLED = 0;
	private static int BASIC = 1;
	private Jama.Matrix global_force;
	private Jama.Matrix global_moment;
	private Jama.Matrix displacement;
	private Jama.Matrix velocity;
	private Jama.Matrix force;
	private Jama.Matrix global_m;
	private Jama.Matrix dpos0, dpos1;
	private Jama.Matrix drot0, drot1;
	private double factor;
	private double diameter;
	private boolean Nodes_are_set;
	private boolean Factor_is_set;
	private boolean Material_is_set;
	private boolean D_is_set;
	private Contact_Line internal_contact_element;
	private int Contact;
	private Material material;

	/**
	 * The constructor of this element.
	 */
	public Beam_Spring_2() {
		super();
		type = new String("BEAM_SPRING_2");
		force = new Jama.Matrix(6, 1);
		displacement = new Jama.Matrix(6, 1);
		velocity = new Jama.Matrix(6, 1);
		global_force = new Jama.Matrix(3, 1);
		global_moment = new Jama.Matrix(3, 1);
		dpos0 = new Jama.Matrix(3, 1);
		drot0 = new Matrix(3, 1);
		dpos1 = new Jama.Matrix(3, 1);
		drot1 = new Matrix(3, 1);
		global_m = new Jama.Matrix(3, 1);
		processed = false;
		node = new Node[2];

		// Disable contact by default
		Contact = DISABLED;
	}

	/**
	 * This metod calculates the elements contribution regarding mass and
	 * rotational interta to the nodes. Since this spring is concidered
	 * massless, the contribution here is neglected.
	 */
	public void assembleMassMatrix() throws IllegalArgumentException {

		this.updateLocalCoordinateSystem();

		/*
		 * Update the contact element if it is enabled
		 */
		if (Contact == BASIC) {
			internal_contact_element.assembleMassMatrix();
		}
	}

	/**
	 * If contact is enabled, the contact forces are calculated here.
	 */
	public void calculateContactForces() {
		// Use the internal contact element to calculate the contact forces.
		if (Contact == BASIC) {
			internal_contact_element.calculateContactForces();
		}
	}

	/**
	 * Here, any external force contribution such as pressure is calculated, but
	 * on this element there is no such feature. The only one to concider is the
	 * internal contact element, should it be enabled.
	 * 
	 * @param currtime
	 *            The current time.
	 */
	public void calculateExternalForces(double currtime) {
		if (Contact == BASIC) {
			internal_contact_element.calculateExternalForces(currtime);
		}
	}

	/**
	 * This calculates and adds the element internal forces to the nodes. Note,
	 * by definition, the internal forces are subtracted from the external
	 * loads, thus the "addition" is negative. The incoming loads are already in
	 * the 'stress' martrix as axial forces and moments as [fx fy fz mx my mz]
	 * where the directions are local.
	 * 
	 * @param integration_point
	 *            The current integration point
	 * 
	 */
	public void calculateNodalForces(int integration_point, double timestep) {

		for (int i = 0; i < 3; i++)
			global_force.set(i, 0, force.get(i, 0));

		for (int i = 0; i < 3; i++)
			global_moment.set(i, 0, force.get(i + 3, 0));

		// Add this force contribution to the node
		node[0].addInternalForce(global_force.times(1));
		node[1].addInternalForce(global_force.times(-1));

		// Add the moment to the node
		node[0].addInternalMoment(global_moment.times(1));
		node[1].addInternalMoment(global_moment.times(-1));

	}

	/**
	 * This method normally calculates the true strain, but in this case, it is
	 * the displacement that is calculated as input to the force / displacement
	 * relation used in the associated material law. The displacement is
	 * calculated as [dx dy dz rx ry rz] in local directions Velocity is
	 * calculated as [vx vy vz vrx vry vrz] in local directions
	 * 
	 * @param timestep
	 *            The timestep of the simulation
	 * @param integration_point
	 *            Which integration point of which the strain should be
	 *            calculated
	 */
	public void calculateStrain(double timestep, int integration_point)
			throws IllegalArgumentException {

		dpos0 = node[0].getPos().minus(node[0].getPos_orig());
		dpos1 = node[1].getPos().minus(node[1].getPos_orig());

		drot0 = node[0].getRot().minus(node[0].getRot_orig());
		drot1 = node[1].getRot().minus(node[1].getRot_orig());

		// Create the global displacements
		displacement.set(0, 0, dpos1.get(0, 0) - dpos0.get(0, 0));
		displacement.set(1, 0, dpos1.get(1, 0) - dpos0.get(1, 0));
		displacement.set(2, 0, dpos1.get(2, 0) - dpos0.get(2, 0));
		displacement.set(3, 0, drot1.get(0, 0) - drot0.get(0, 0));
		displacement.set(4, 0, drot1.get(1, 0) - drot0.get(1, 0));
		displacement.set(5, 0, drot1.get(2, 0) - drot0.get(2, 0));

		// reuse position matrix for velocities (used for damping)
		dpos0 = node[0].getVel();
		dpos1 = node[1].getVel();

		drot0 = node[0].getRotVel();
		drot1 = node[1].getRotVel();

		velocity.set(0, 0, dpos1.get(0, 0) - dpos0.get(0, 0));
		velocity.set(1, 0, dpos1.get(1, 0) - dpos0.get(1, 0));
		velocity.set(2, 0, dpos1.get(2, 0) - dpos0.get(2, 0));
		velocity.set(3, 0, drot1.get(0, 0) - drot0.get(0, 0));
		velocity.set(4, 0, drot1.get(1, 0) - drot0.get(1, 0));
		velocity.set(5, 0, drot1.get(2, 0) - drot0.get(2, 0));

	}

	/**
	 * This method calculates the stress of the element, but since the material
	 * law for the spring is used for the displacement / force relation, the
	 * parameters are "faked" here. The force matrix is updated by the
	 * "material" laws and used in the calculateNodalForces.
	 * 
	 * @param integration_point
	 *            The integration point of which the stress is to be calculated
	 */
	public void calculateStress(int integration_point, double timestep)
			throws IllegalArgumentException {
		material.calculateStressOneDimensional(displacement, velocity, force,
				timestep);
	}

	/**
	 * This method calculates the timestep for the element and returns the
	 * smallest of the current timestep and the element timestep since it is the
	 * smallest element that determines the stability of the solution.
	 * 
	 * @param current_timestep
	 *            The current timestep
	 * 
	 * @return double
	 */
	public double checkTimestep(double current_timestep) {
		double I1;
		double I2;

		I1 = (node[0].getInertia().get(0, 0) + node[0].getInertia().get(1, 1) + node[0]
				.getInertia().get(2, 2)) / 3.0;
		I2 = (node[1].getInertia().get(0, 0) + node[1].getInertia().get(1, 1) + node[1]
				.getInertia().get(2, 2)) / 3.0;

		return Math.min(current_timestep, material.wavespeedOneDimensional(
				(4 * node[0].getMass() * node[1].getMass())
						/ (node[0].getMass() + node[1].getMass()),
				(4 * I1 * I2) / (I1 + I2)));
	}

	/**
	 * This element only has one integration point (gauss point)
	 * 
	 * @return Number of integration points
	 */
	public int getNumberOfIntegrationPoints() {
		return 1;
	}

	/**
	 * Returns the local force matrix of the BeamSpring.
	 * 
	 * @return Jama.Matrix Local force matrix
	 */
	public Jama.Matrix getForce() {
		return force;
	}

	/**
	 * This method parses the input data to the element from the Fembic indata
	 * file.
	 * 
	 * @param param
	 *            The input, split up into an array of Tokens
	 * @param lineno
	 *            The line number.
	 * @param nodelist
	 *            The global nodelist
	 * @param materiallist
	 *            The global material list
	 * @param loadlist
	 *            The global load list
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
							"Error in BeamSpring element\n" + e.getMessage()
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
			// The contact element of the shell is specified
			if (param[i].getw().toUpperCase().equals("CONTACT")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the element thickness is in param3. Set this in
				// the element
				if (param[i + 2].getw().toUpperCase().equals("BASIC")) {
					Contact = BASIC;
				} else {
					throw new java.text.ParseException(
							"Unrecognized contact parameter", lineno);
				}

				i += 3;
			} else {
				// Neither material or nodes are defined. Then the parameter is
				// wrong.
				throw new java.text.ParseException(
						"Unknown BeamSpring element parameter ", lineno);
			}
		}

		// Make a local instance of a contact element to handle the contacts
		if (Contact == BASIC) {
			internal_contact_element = new Contact_Line();
			internal_contact_element.setNumber(-1);

			// Make the input for the contact element
			if (Factor_is_set) {
				contact_input = new Token[9];
			} else {
				contact_input = new Token[6];
			}

			contact_input[0] = new Token(new String("nodes"));
			contact_input[1] = new Token(new String("="));
			contact_input[2] = new Token(new String("[" + node[0].getNumber()
					+ "," + node[1].getNumber() + "]"));
			contact_input[3] = new Token(new String("D"));
			contact_input[4] = new Token(new String("="));
			contact_input[5] = new Token(diameter);

			if (Factor_is_set) {
				contact_input[6] = new Token(new String("factor"));
				contact_input[7] = new Token(new String("="));
				contact_input[8] = new Token(factor);
			}

			// Now, set the input for the contact element also
			internal_contact_element.parse_Fembic(contact_input, lineno,
					nodelist, materiallist, loadlist, nodetable);
		}
	}

	/**
	 * Checks the indata so that everyting required has been set. Note that the
	 * diameter is only needed for contact purpouse.
	 */
	public void checkIndata() throws IllegalArgumentException {
		// Check that all the required parameters have been parsed
		if ((Contact == BASIC) && !D_is_set) {
			throw new IllegalArgumentException(
					"No Diameter defined for BeamSpring element nr" + number);
		}

		if (!Nodes_are_set) {
			throw new IllegalArgumentException(
					"No nodes defined for BeamSpring element nr" + number);
		}

		if (!Material_is_set) {
			throw new IllegalArgumentException(
					"No Material defined for BeamSpring element nr" + number);
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
			 * Print a dummy value for this element, remembering the previous
			 * line index
			 */
			out = new String(number + " ");
			out += "0.0 \n";

			return out;

		case RESULT_STRAIN_LOCAL:

			/*
			 * Print a dummy value for this element, remembering the previous
			 * line index
			 */
			out = new String(number + " ");
			out += "0.0 \n";

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
			out = new String(number + "\t  nodes = [" + node[0].getNumber()
					+ ","
					+
					// node[1].getNumber() + "," + node[2].getNumber() + "]\t"
					node[1].getNumber() + "]\t" + "material = "
					+ material.getName() + "\t");

			if (D_is_set)
				out += " D = " + diameter;

			if (Factor_is_set)
				out += " factor = " + factor;

			if (Contact == BASIC)
				out += " contact = BASIC";

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

		// Update local coordiante system on the contact element
		if (Contact == BASIC) {
			internal_contact_element.updateLocalCoordinateSystem();
		}
	}

	/**
	 * Returns the local Fx force in the spring
	 * 
	 * @return the local Fx force in the spring
	 */
	public double getFx() {
		return force.get(0, 0);
	}

	/**
	 * Returns the local Fy force in the spring
	 * 
	 * @return the local Fy force in the spring
	 */
	public double getFy() {
		return force.get(1, 0);
	}

	/**
	 * Returns the local Fz force in the spring
	 * 
	 * @return the local Fz force in the spring
	 */
	public double getFz() {
		return force.get(2, 0);
	}

	/**
	 * Returns the local Mx force in the spring
	 * 
	 * @return the local Mx force in the spring
	 */
	public double getMx() {
		return force.get(3, 0);
	}

	/**
	 * Returns the local My force in the spring
	 * 
	 * @return the local My force in the spring
	 */
	public double getMy() {
		return force.get(4, 0);
	}

	/**
	 * Returns the local Mz force in the spring
	 * 
	 * @return the local Mz force in the spring
	 */
	public double getMz() {
		return force.get(5, 0);
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

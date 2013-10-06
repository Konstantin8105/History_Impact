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

// FILE: c:/temp/krockpackage/Shell.java
import java.text.ParseException;

import java.util.*;

/**
 * This is a Belytchko Tsai Lin element. It is based on material from various
 * sources such as: Explicit Algorithms for the Nonlinear Dynamics of Shells -
 * Computer Methods in Applied Mechanics and Engineering 42 (1984) 225-251 Dyna
 * Theoretical Manual Radioss Manual
 * 
 * @author Jonas Forssell, Yuriy Mikhaylovskiy
 * 
 * @see OtherClasses
 */
public class Shell_BT_4 extends Element {
	private static int DISABLED = 0;
	private static int BASIC = 1;
	private static int ADVANCED = 2;
	private static int EDGE = 3;
	private static int ADVANCED_EDGE = 4;
	private double area;
	private Jama.Matrix local_coordinate_system;
	private Jama.Matrix[] strain;
	private Jama.Matrix[] dstrain;
	private Jama.Matrix strainrate;
	private Jama.Matrix[] stress;
	private Jama.Matrix force;
	private Jama.Matrix hourglass_force;
	private Jama.Matrix moment;
	private Jama.Matrix hourglass_moment;
	private Jama.Matrix B;
	private Jama.Matrix p;
	private Jama.Matrix v;
	private Jama.Matrix a;
	private Jama.Matrix trash;
	private double dmx;
	private double dmy;
	private double dmxy;
	private double kappax;
	private double kappay;
	private double kappaxy;
	private double[] z;
	private double[] weight_factor;
	private int number_of_integration_points;
	private int Contact;
	private int printed_integration_point;
	private double thickness;
	private double factor;
	private double friction;
	private double original_thickness;
	private Material[] material;
	private double shear_factor;
	private boolean hourglass_control;
	private double membrane_hourglass_coeff;
	private double out_of_plane_hourglass_coeff;
	private double rotation_hourglass_coeff;
	private Jama.Matrix hourglass_vector;
	private Load load;
	private boolean NIP_is_set;
	private boolean PIP_is_set;
	private boolean Nodes_are_set;
	private boolean Material_is_set;
	private boolean T_is_set;
	private boolean Factor_is_set;
	private boolean Friction_is_set;
	private boolean Thinning_is_enabled;
	private Contact_Triangle internal_contact_element_1;
	private Contact_Triangle internal_contact_element_2;
	private Contact_Triangle internal_contact_element_3;
	private Contact_Triangle internal_contact_element_4;
	private Contact_Line internal_contact_line_element_1;
	private Contact_Line internal_contact_line_element_2;
	private Contact_Line internal_contact_line_element_3;
	private Contact_Line internal_contact_line_element_4;

	// Attributes
	// Associations
	// Operations

	/**
	 * Insert the method's description here. Creation date: (2001-08-10
	 * 19:45:45)
	 */
	public Shell_BT_4() {
		super();
		type = new String("SHELL_BT_4");

		int i;
		p = new Jama.Matrix(12, 1);
		v = new Jama.Matrix(12, 1);
		a = new Jama.Matrix(12, 1);
		trash = new Jama.Matrix(12, 1);
		B = new Jama.Matrix(2, 4);
		node = new Node[4];
		force = new Jama.Matrix(3, 1);
		hourglass_force = new Jama.Matrix(3, 1);
		moment = new Jama.Matrix(3, 1);
		hourglass_moment = new Jama.Matrix(3, 1);
		material = new Material[5];
		strainrate = new Jama.Matrix(6, 1);
		local_coordinate_system = new Jama.Matrix(3, 3);

		//
		strain = new Jama.Matrix[5];

		for (i = 0; i < 5; i++) {
			strain[i] = new Jama.Matrix(6, 1);
		}

		//
		dstrain = new Jama.Matrix[5];

		for (i = 0; i < 5; i++) {
			dstrain[i] = new Jama.Matrix(6, 1);
		}

		//
		stress = new Jama.Matrix[5];

		for (i = 0; i < 5; i++) {
			stress[i] = new Jama.Matrix(6, 1);
		}

		//
		z = new double[5];
		weight_factor = new double[5];

		//
		hourglass_vector = new Jama.Matrix(4, 1);

		// Set the default values
		// These will change if the user has defined them in the input file
		shear_factor = 1.0;
		hourglass_control = true;
		membrane_hourglass_coeff = 0.1;
		out_of_plane_hourglass_coeff = 0.1;
		rotation_hourglass_coeff = 0.1;
		Contact = BASIC;
		Thinning_is_enabled = true;
		number_of_integration_points = 5;
	}

	/**
	 * Insert the method's description here. Creation date: (25/12/01 %T)
	 */
	public void assembleMassMatrix() throws IllegalArgumentException {
		Jama.Matrix inertia;
		inertia = new Jama.Matrix(3, 3);

		double I;
		double mass;

		// Update local coordinate system
		this.updateLocalCoordinateSystem();

		// Determine the element Area
		// and set the internal node coordinate (if applicable)
		this.calculateLocalVariables();

		// Distribute the mass contribution on the nodes
		mass = material[0].getDensity() * area * thickness;
		node[0].addMass(mass / 4);
		node[1].addMass(mass / 4);
		node[2].addMass(mass / 4);
		node[3].addMass(mass / 4);

		/*
		 * Now, calculate the inertia. [Ixx Iyx Izx] I = [Ixy Iyy Izy] [Ixz Iyz
		 * Izz] With the exact formula for intertia, the solution tends to
		 * diverge for large rotation rates. A way to stabalize the rotation is
		 * to let Izz = Iyy = Ixx , Ixy... = 0 and concider the rectangle as a
		 * square. This gives the following:
		 */
		I = (mass / 4) * ((area / 12) + ((thickness * thickness) / 12));

		inertia.set(0, 0, I);
		inertia.set(1, 1, I);
		inertia.set(2, 2, I);
		inertia.set(1, 0, 0);
		inertia.set(2, 0, 0);
		inertia.set(0, 1, 0);
		inertia.set(0, 2, 0);
		inertia.set(2, 1, 0);
		inertia.set(1, 2, 0);

		// Now, add to the nodes
		node[0].addInertia(inertia);
		node[1].addInertia(inertia);
		node[2].addInertia(inertia);
		node[3].addInertia(inertia);

		//
		if ((Contact == BASIC) || (Contact == EDGE)) {
			internal_contact_element_1.assembleMassMatrix();
			internal_contact_element_2.assembleMassMatrix();
		}

		if ((Contact == ADVANCED) || (Contact == ADVANCED_EDGE)) {
			internal_contact_element_1.assembleMassMatrix();
			internal_contact_element_2.assembleMassMatrix();
			internal_contact_element_3.assembleMassMatrix();
			internal_contact_element_4.assembleMassMatrix();
		}

		if ((Contact == EDGE) || (Contact == ADVANCED_EDGE)) {
			if (internal_contact_line_element_1 != null) {
				internal_contact_line_element_1.assembleMassMatrix();
			}

			if (internal_contact_line_element_2 != null) {
				internal_contact_line_element_2.assembleMassMatrix();
			}

			if (internal_contact_line_element_3 != null) {
				internal_contact_line_element_3.assembleMassMatrix();
			}

			if (internal_contact_line_element_4 != null) {
				internal_contact_line_element_4.assembleMassMatrix();
			}
		}
	}

	/**
	 * Insert the method's description here. Creation date: (25/12/01 %T)
	 */
	public void calculateContactForces() {
		if ((Contact == BASIC) || (Contact == EDGE)) {
			internal_contact_element_1.calculateContactForces();
			internal_contact_element_2.calculateContactForces();
		}

		if ((Contact == ADVANCED) || (Contact == ADVANCED_EDGE)) {
			internal_contact_element_1.calculateContactForces();
			internal_contact_element_2.calculateContactForces();
			internal_contact_element_3.calculateContactForces();
			internal_contact_element_4.calculateContactForces();

			// Distribute the load from the internal node onto the others
			node[0].addInternalForce(middle_node.getForce().times(0.25));
			node[1].addInternalForce(middle_node.getForce().times(0.25));
			node[2].addInternalForce(middle_node.getForce().times(0.25));
			node[3].addInternalForce(middle_node.getForce().times(0.25));

			// And reset it
			middle_node.clearNodalForces();
		}

		if ((Contact == EDGE) || (Contact == ADVANCED_EDGE)) {
			if (internal_contact_line_element_1 != null) {
				internal_contact_line_element_1.calculateContactForces();
			}

			if (internal_contact_line_element_2 != null) {
				internal_contact_line_element_2.calculateContactForces();
			}

			if (internal_contact_line_element_3 != null) {
				internal_contact_line_element_3.calculateContactForces();
			}

			if (internal_contact_line_element_4 != null) {
				internal_contact_line_element_4.calculateContactForces();
			}
		}
	}

	/**
	 * Insert the method's description here. Creation date: (25/12/01 %T)
	 */
	public void calculateExternalForces(double currtime) {
		int i;

		// Add the pressure force in local z-direction if there is a load object
		// defined
		if (load != null) {
			force.set(0, 0, 0);
			force.set(1, 0, 0);
			force.set(2, 0, area * load.getPressure(currtime) * 0.25);

			// Transform to global directions
			force = local_coordinate_system.transpose().times(force);

			// Distribute on nodes (note this is external forces so no sign
			// reversal)
			for (i = 0; i < 4; i++) {
				node[i].addInternalForce(force);
			}
		}

		if ((Contact == BASIC) || (Contact == EDGE)) {
			internal_contact_element_1.calculateExternalForces(currtime);
			internal_contact_element_2.calculateExternalForces(currtime);
		}

		if ((Contact == ADVANCED) || (Contact == ADVANCED_EDGE)) {
			internal_contact_element_1.calculateExternalForces(currtime);
			internal_contact_element_2.calculateExternalForces(currtime);
			internal_contact_element_3.calculateExternalForces(currtime);
			internal_contact_element_4.calculateExternalForces(currtime);
		}

		if ((Contact == EDGE) || (Contact == ADVANCED_EDGE)) {
			if (internal_contact_line_element_1 != null) {
				internal_contact_line_element_1
						.calculateExternalForces(currtime);
			}

			if (internal_contact_line_element_2 != null) {
				internal_contact_line_element_2
						.calculateExternalForces(currtime);
			}

			if (internal_contact_line_element_3 != null) {
				internal_contact_line_element_3
						.calculateExternalForces(currtime);
			}

			if (internal_contact_line_element_4 != null) {
				internal_contact_line_element_4
						.calculateExternalForces(currtime);
			}
		}
	}

	/**
	 * This method is redesigned for the shell element. It calculates the local
	 * base vectors for the element. These vectors defines the local coordinate
	 * system for the element on which all the stress, strain and force
	 * calculations are based.
	 * 
	 * The input are four points in space. They are used as follows: 1. The
	 * first point is the base point. 2. The local x-axis is defined from base
	 * point to the second point. 3. The local z-axis is defined as the cross
	 * product of the diagonals 1-3, 2-4
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 3. The local y-axis is defined from the base point to the third point 4.
	 * The cross vector between the x-axis and the y axis, defines the normal to
	 * the plane 5. A new adjusted x-axis is calculated. 6. Finally, the y-axis
	 * is calculated as the cross product of the x-axis and z-axis. Now, all the
	 * three axis are normal to each other and a correct coordinate system is
	 * defined. The method will return a matrix with these vectors: x-vec y-vec
	 * z-vec x y z Creation date: (26/08/01 %T) Jonas Forssell
	 */
	public void calculateLocalBaseVectors(double x1, double y1, double z1,
			double x2, double y2, double z2, double x3, double y3, double z3,
			double x4, double y4, double z4, Jama.Matrix bvs) {
		// 1&2. Define the local x-axis.
		bvs.set(0, 0, x2 - x1);
		bvs.set(0, 1, y2 - y1);
		bvs.set(0, 2, z2 - z1);

		// 3. Define the y-axis
		bvs.set(1, 0, x4 - x1);
		bvs.set(1, 1, y4 - y1);
		bvs.set(1, 2, z4 - z1);

		// 4. Calculate the z-axis (cross product of the diagonals)

		bvs.set(2, 0, (y3 - y1) * (z4 - z2) - (z3 - z1) * (y4 - y2));
		bvs.set(2, 1, (z3 - z1) * (x4 - x2) - (z4 - z2) * (x3 - x1));
		bvs.set(2, 2, (x3 - x1) * (y4 - y2) - (y3 - y1) * (x4 - x2));

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

		// 5. Adjust the x-axis (using the y-axis as a temporary variable)
		// local_y_axis =
		// local_x_axis.times(local_z_axis.transpose()).times(local_z_axis);
		bvs.set(1,
				0,
				(bvs.get(0, 0) * bvs.get(2, 0) * bvs.get(2, 0))
						+ (bvs.get(0, 0) * bvs.get(2, 1) * bvs.get(2, 1))
						+ (bvs.get(0, 0) * bvs.get(2, 2) * bvs.get(2, 2)));
		bvs.set(1,
				1,
				(bvs.get(0, 1) * bvs.get(2, 0) * bvs.get(2, 0))
						+ (bvs.get(0, 1) * bvs.get(2, 1) * bvs.get(2, 1))
						+ (bvs.get(0, 1) * bvs.get(2, 2) * bvs.get(2, 2)));
		bvs.set(1,
				2,
				(bvs.get(0, 2) * bvs.get(2, 0) * bvs.get(2, 0))
						+ (bvs.get(0, 2) * bvs.get(2, 1) * bvs.get(2, 1))
						+ (bvs.get(0, 2) * bvs.get(2, 2) * bvs.get(2, 2)));

		// local_x_axis = local_y_axis.times(1.0/local_y_axis.length());
		bvs.set(0,
				2,
				Math.sqrt((bvs.get(1, 0) * bvs.get(1, 0))
						+ (bvs.get(1, 1) * bvs.get(1, 1))
						+ (bvs.get(1, 2) * bvs.get(1, 2))));
		bvs.set(0, 0, bvs.get(1, 0) / bvs.get(0, 2));
		bvs.set(0, 1, bvs.get(1, 1) / bvs.get(0, 2));
		bvs.set(0, 2, bvs.get(1, 2) / bvs.get(0, 2));

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
	 * This calculates and adds the element internal forces to the nodes. Note,
	 * by definition, the internal forces are subtracted from the external loads
	 * Thus, the "addition" is negative. Creation date: (2001-10-23 14.03.06)
	 * 
	 */
	public void calculateNodalForces(int integration_point, double timestep) {
		/*
		 * The nodal forces are computed as a function of the gauss integration
		 * of the stresses. This means a summary must be made for all
		 * integration points. This routine will be called once for every
		 * integration point so this routine only has to calculate the force
		 * contribution of a specific integration point and distribute it on the
		 * nodes.
		 */
		int i;
		double ki;
		double ko;
		double kr;
		Jama.Matrix transposed_coordinate_system;

		ki = 0.0;
		ko = 0.0;
		kr = 0.0;
		transposed_coordinate_system = local_coordinate_system.transpose();

		// Check the area of the element
		if (integration_point == 0) {
			if (area <= 0) {
				throw new IllegalArgumentException(
						"Error in Shell_BT_4 element. Negative or zero area. ");
			}
		}

		// Loop through the nodes
		for (i = 0; i < 4; i++) {
			// Calculate forces fx, fy , fz
			force.set(
					0,
					0,
					0.5
							* thickness
							* area
							* weight_factor[integration_point]
							* ((B.get(0, i) * stress[integration_point].get(0,
									0)) + (B.get(1, i) * stress[integration_point]
									.get(3, 0))));
			force.set(
					1,
					0,
					0.5
							* thickness
							* area
							* weight_factor[integration_point]
							* ((B.get(1, i) * stress[integration_point].get(1,
									0)) + (B.get(0, i) * stress[integration_point]
									.get(3, 0))));
			force.set(
					2,
					0,
					0.5
							* thickness
							* area
							* shear_factor
							* weight_factor[integration_point]
							* ((B.get(0, i) * stress[integration_point].get(5,
									0)) + (B.get(1, i) * stress[integration_point]
									.get(4, 0))));

			/*
			 * Now, add the hourglass forces These are calculated according to a
			 * viscous model where the forces are related to the rate of
			 * displacement (node velocity). The formulation is based on work
			 * done by Kosloff and Frasier.
			 */
			if ((integration_point == 0) && (hourglass_control == true)) {
				if (i == 0) {
					ki = 0.25 * material[0].getDensity()
							* material[0].wavespeedTwoDimensional(0.0, 0.0)
							* thickness
							* Math.sqrt((membrane_hourglass_coeff * area) / 2);
					ko = 0.25 * material[0].getDensity()
							* material[0].wavespeedTwoDimensional(0.0, 0.0)
							* thickness * thickness
							* Math.sqrt(out_of_plane_hourglass_coeff / 10);
					kr = 0.02 * material[0].getDensity()
							* material[0].wavespeedTwoDimensional(0.0, 0.0)
							* thickness * thickness * area
							* Math.sqrt(rotation_hourglass_coeff / 2);
				}

				// In-plane mode (x and y directions)
				hourglass_force
						.set(0,
								0,
								ki
										* ((v.get(0, 0) - v.get(3, 0) + v.get(
												6, 0)) - v.get(9, 0))
										* hourglass_vector.get(i, 0));
				hourglass_force
						.set(1,
								0,
								ki
										* ((v.get(1, 0) - v.get(4, 0) + v.get(
												7, 0)) - v.get(10, 0))
										* hourglass_vector.get(i, 0));

				// Out-of-plane mode (z directions)
				hourglass_force
						.set(2,
								0,
								ko
										* ((v.get(2, 0) - v.get(5, 0) + v.get(
												8, 0)) - v.get(11, 0))
										* hourglass_vector.get(i, 0));

				// .. and transform to global coordinate system
				hourglass_force = transposed_coordinate_system
						.times(hourglass_force);

				// add to the node
				node[i].addHourglassForce(hourglass_force.times(-1.0));
			}

			// .. and transform to global coordinate system
			force = transposed_coordinate_system.times(force);

			// Calculate moments mx, my, mz
			// For those who are studying this in detail and is wondering why
			// the formula is 0.5*thickness and not 0.25*thickness*thickness,
			// remember that z[integration_point] already includes 0.5*thickness
			moment.set(
					0,
					0,
					0.5
							* thickness
							* area
							* weight_factor[integration_point]
							* (((B.get(1, i) * (-z[integration_point] * stress[integration_point]
									.get(1, 0))) + (B.get(0, i) * (-z[integration_point] * stress[integration_point]
									.get(3, 0)))) - (0.25 * shear_factor * stress[integration_point]
									.get(4, 0))));
			moment.set(
					1,
					0,
					0.5
							* thickness
							* area
							* weight_factor[integration_point]
							* ((-B.get(0, i) * (-z[integration_point] * stress[integration_point]
									.get(0, 0)))
									- (B.get(1, i) * (-z[integration_point] * stress[integration_point]
											.get(3, 0))) + (0.25 * shear_factor * stress[integration_point]
									.get(5, 0))));
			moment.set(2, 0, 0);

			// Hourglass
			if ((integration_point == 0) && (hourglass_control == true)) {
				hourglass_moment
						.set(0,
								0,
								kr
										* ((a.get(0, 0) - a.get(3, 0) + a.get(
												6, 0)) - a.get(9, 0))
										* hourglass_vector.get(i, 0));
				hourglass_moment
						.set(1,
								0,
								kr
										* ((a.get(1, 0) - a.get(4, 0) + a.get(
												7, 0)) - a.get(10, 0))
										* hourglass_vector.get(i, 0));
				hourglass_moment
						.set(2,
								0,
								kr
										* ((a.get(2, 0) - a.get(5, 0) + a.get(
												8, 0)) - a.get(11, 0))
										* hourglass_vector.get(i, 0));

				// .. and transform to global coordinate system
				hourglass_moment = transposed_coordinate_system
						.times(hourglass_moment);

				// add to the node.
				node[i].addHourglassMoment(hourglass_moment.times(-1.0));
			}

			// .. and transform to global coordinate system
			moment = transposed_coordinate_system.times(moment);

			/*
			 * Now, add the force and moment loads to the node. Remember that
			 * internal forces are negative and external are positive.
			 */
			node[i].addInternalForce(force.times(-1.0));
			node[i].addInternalMoment(moment.times(-1.0));
		}
	}

	/**
	 * v is an array of node velocities expressed in the local coordinate system
	 * v = [u1 v1 w1 u2 v2 w2 u3 v3 w3 u4 v4 w4] p is an array of node
	 * coordinates expessed in the local coordinate system p = [x1 y1 z1 x2 y2
	 * z2 x3 y3 z3 x4 y4 z4] a is an array of node rotation velocity expressed
	 * in the local coordinate system a = [ax1 ay1 az1 ax2 ay2 az2 ax3 ay3 az3
	 * ax4 ay4 az4] B is a matrix of the shape function derivatives. Creation
	 * date: (25/12/01 %T) Jonas Forssell
	 * 
	 * @param tstep
	 *            double
	 */
	public void calculateStrain(double tstep, int integration_point) {
		// Do this block only once for each timestep. Not for each integration
		// point
		if (integration_point == 0) {
			this.updateLocalCoordinateSystem();
			this.calculateLocalVariables();

			// Correct the area if element is set to high accuracy
			if ((Contact == ADVANCED) || (Contact == ADVANCED_EDGE)) {
				area = internal_contact_element_1.getArea()
						+ internal_contact_element_2.getArea()
						+ internal_contact_element_3.getArea()
						+ internal_contact_element_4.getArea();
			}

			/*
			 * Now that we have the input in local coordinate system, it is time
			 * to calculate the strain rates. Start by creating the B matrix
			 * (the derivate of the shape functions) which will be used
			 * frequently in the rest of the calculations.
			 */
			B.set(0, 0, p.get(4, 0) - p.get(10, 0));
			B.set(0, 1, p.get(7, 0) - p.get(1, 0));
			B.set(0, 2, p.get(10, 0) - p.get(4, 0));
			B.set(0, 3, p.get(1, 0) - p.get(7, 0));
			B.set(1, 0, p.get(9, 0) - p.get(3, 0));
			B.set(1, 1, p.get(0, 0) - p.get(6, 0));
			B.set(1, 2, p.get(3, 0) - p.get(9, 0));
			B.set(1, 3, p.get(6, 0) - p.get(0, 0));
			B.timesEquals(1.0 / (2.0 * area));

			/*
			 * Now, the strain rates can be determined. They are calculated in
			 * the middle of the shell and then transformed out through the
			 * thickness.
			 * 
			 * Start by calculating the middle strain rates:
			 */
			dmx = (B.get(0, 0) * (v.get(0, 0) - v.get(6, 0)))
					+ (B.get(0, 1) * (v.get(3, 0) - v.get(9, 0)));
			dmy = (B.get(1, 0) * (v.get(1, 0) - v.get(7, 0)))
					+ (B.get(1, 1) * (v.get(4, 0) - v.get(10, 0)));
			dmxy = (B.get(1, 0) * (v.get(0, 0) - v.get(6, 0)))
					+ (B.get(1, 1) * (v.get(3, 0) - v.get(9, 0)))
					+ (B.get(0, 0) * (v.get(1, 0) - v.get(7, 0)))
					+ (B.get(0, 1) * (v.get(4, 0) - v.get(10, 0)));

			//
			kappax = -((B.get(0, 0) * (a.get(1, 0) - a.get(7, 0))) + (B.get(0,
					1) * (a.get(4, 0) - a.get(10, 0))));
			kappay = (B.get(1, 0) * (a.get(0, 0) - a.get(6, 0)))
					+ (B.get(1, 1) * (a.get(3, 0) - a.get(9, 0)));
			kappaxy = (-B.get(1, 0) * (a.get(1, 0) - a.get(7, 0)))
					- (B.get(1, 1) * (a.get(4, 0) - a.get(10, 0)))
					+ (B.get(0, 0) * (a.get(0, 0) - a.get(6, 0)))
					+ (B.get(0, 1) * (a.get(3, 0) - a.get(9, 0)));
		}

		// Now, compute the strainrate for this integration point.
		strainrate.set(0, 0, dmx - (z[integration_point] * kappax));
		strainrate.set(1, 0, dmy - (z[integration_point] * kappay));
		strainrate.set(3, 0, dmxy - (z[integration_point] * kappaxy));

		// The bending strain rates
		// dyz
		strainrate
				.set(4,
						0,
						((B.get(1, 0) * (v.get(2, 0) - v.get(8, 0))) + (B.get(
								1, 1) * (v.get(5, 0) - v.get(11, 0))))
								- (0.25 * (a.get(0, 0) + a.get(3, 0)
										+ a.get(6, 0) + a.get(9, 0))));

		// dxz
		strainrate.set(
				5,
				0,
				(B.get(0, 0) * (v.get(2, 0) - v.get(8, 0)))
						+ (B.get(0, 1) * (v.get(5, 0) - v.get(11, 0)))
						+ (0.25 * (a.get(1, 0) + a.get(4, 0) + a.get(7, 0) + a
								.get(10, 0))));

		// Knowing the strainrate, the strain increase for this integration
		// point is easily computed.
		dstrain[integration_point] = strainrate.times(tstep);
	}

	/**
	 * Insert the method's description here. Creation date: (25/12/01 %T)
	 */
	public void calculateStress(int integration_point, double timestep) {
		/*
		 * This calculates the stresses using the strain and strain increase.
		 * The result is put back into the stress matrix. The element contains
		 * one local material object for each integration point since some
		 * materials has a memory which must be maintaned.
		 */
		material[integration_point].calculateStressTwoDimensionalPlaneStress(
				strain[integration_point], dstrain[integration_point],
				stress[integration_point], timestep);

		// Update the thickness for thickness reduction using the mid-most
		// integration point.
		if (Thinning_is_enabled
				&& (integration_point == (number_of_integration_points / 2))) {
			thickness = (1 + strain[number_of_integration_points / 2].get(2, 0))
					* original_thickness;
		}
	}

	/**
	 * Insert the method's description here. Creation date: (25/12/01 %T)
	 * 
	 * @param current_timestep
	 *            double
	 * 
	 * @return double
	 */
	public double checkTimestep(double current_timestep) {
		double critical_length;
		double timestep;

		// Find the longest edge on the element
		critical_length = Math
				.sqrt(((node[1].getX_pos() - node[0].getX_pos()) * (node[1]
						.getX_pos() - node[0].getX_pos()))
						+ ((node[1].getY_pos() - node[0].getY_pos()) * (node[1]
								.getY_pos() - node[0].getY_pos()))
						+ ((node[1].getZ_pos() - node[0].getZ_pos()) * (node[1]
								.getZ_pos() - node[0].getZ_pos())));
		critical_length = Math.max(critical_length,
				Math.sqrt(((node[2].getX_pos() - node[1].getX_pos()) * (node[2]
						.getX_pos() - node[1].getX_pos()))
						+ ((node[2].getY_pos() - node[1].getY_pos()) * (node[2]
								.getY_pos() - node[1].getY_pos()))
						+ ((node[2].getZ_pos() - node[1].getZ_pos()) * (node[2]
								.getZ_pos() - node[1].getZ_pos()))));
		critical_length = Math.max(critical_length,
				Math.sqrt(((node[3].getX_pos() - node[2].getX_pos()) * (node[3]
						.getX_pos() - node[2].getX_pos()))
						+ ((node[3].getY_pos() - node[2].getY_pos()) * (node[3]
								.getY_pos() - node[2].getY_pos()))
						+ ((node[3].getZ_pos() - node[2].getZ_pos()) * (node[3]
								.getZ_pos() - node[2].getZ_pos()))));
		critical_length = Math.max(critical_length,
				Math.sqrt(((node[0].getX_pos() - node[3].getX_pos()) * (node[0]
						.getX_pos() - node[3].getX_pos()))
						+ ((node[0].getY_pos() - node[3].getY_pos()) * (node[0]
								.getY_pos() - node[3].getY_pos()))
						+ ((node[0].getZ_pos() - node[3].getZ_pos()) * (node[0]
								.getZ_pos() - node[3].getZ_pos()))));
		critical_length = area / critical_length;

		// Determine the timestep.
		timestep = critical_length
				/ material[0].wavespeedTwoDimensional(0.0, 0.0);

		// Now, return the smallest of the suggested and the calculated
		// timestep.
		// The smallest element will decide the timestep for the whole
		// simulation.
		return Math.min(0.96 * timestep, current_timestep);
	}

	/**
	 * This element only has one integration point (gauss point) Creation date:
	 * (26/12/01 %T)
	 * 
	 * @return int
	 */
	public int getNumberOfIntegrationPoints() {
		return number_of_integration_points;
	}

	/**
	 * Insert the method's description here. Creation date: (25/12/01 %T)
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
		int index;
		Load temp_load;
		Token[] contact_input;

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
					for (j = 0; j < 4; j++) {
						node[j] = super
								.findNode(
										super.getNodeNumber(j + 1,
												param[i + 2].getw()), nodetable);
					}

					i += 3;
					Nodes_are_set = true;
				} catch (IllegalArgumentException e) {
					throw new ParseException(e.getMessage(), lineno);
				}
			} else
			// The material of the element is defined
			if (param[i].getw().toUpperCase().equals("MATERIAL")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// Assume now that the material name is delivered in param3
				try {
					// We want the handle to the material object.
					material[0] = super.findMaterial(param[i + 2].getw()
							.toUpperCase(), materiallist);
					i += 3;
					Material_is_set = true;
				} catch (IllegalArgumentException e) {
					throw new java.text.ParseException(
							"Error in Shell_BT_4 element\n" + e.getMessage(),
							lineno);
				}
			} else
			// The number of integration points of the element is defined
			if (param[i].getw().toUpperCase().equals("NIP")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in param3. Set this in
				// the element
				number_of_integration_points = (int) param[i + 2].getn();
				i += 3;
				NIP_is_set = true;
			} else
			// The number of the integration point to print results from
			if (param[i].getw().toUpperCase().equals("PIP")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in param3. Set this in
				// the element
				printed_integration_point = (int) param[i + 2].getn() - 1;
				i += 3;
				PIP_is_set = true;
			} else
			// The thickness of the shell is defined
			if (param[i].getw().toUpperCase().equals("T")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the element thickness is in param3. Set this in
				// the element
				thickness = param[i + 2].getn();
				original_thickness = thickness;
				i += 3;
				T_is_set = true;
			} else
			// The friction of the shell is defined
			if (param[i].getw().toUpperCase().equals("FRICTION")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the element thickness is in param3. Set this in
				// the element
				friction = param[i + 2].getn();
				i += 3;
				Friction_is_set = true;
			} else
			// The shear factor of the shell is defined
			if (param[i].getw().toUpperCase().equals("SHEAR_FACTOR")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				shear_factor = param[i + 2].getn();
				i += 3;
			} else
			// Hourglass control of the shell element is defined
			// This can be either ON or OFF
			if (param[i].getw().toUpperCase().equals("HOURGLASS")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// Default is horglass control is ON. Anything other than OFF
				// means default.
				if (param[i + 2].getw().toUpperCase().equals("OFF")) {
					hourglass_control = false;
				}

				i += 3;
			} else
			// The shell membrane hourglass coefficient
			if (param[i].getw().toUpperCase().equals("MHC")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				membrane_hourglass_coeff = param[i + 2].getn();
				i += 3;
			} else
			// The shell out of plane hourglass coefficient
			if (param[i].getw().toUpperCase().equals("OOPHC")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				out_of_plane_hourglass_coeff = param[i + 2].getn();
				i += 3;
			} else
			// The shell rotation hourglass coefficient
			if (param[i].getw().toUpperCase().equals("RHC")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				rotation_hourglass_coeff = param[i + 2].getn();
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("LOAD")
					&& param[i + 2].is_a_word()) {
				for (index = 0; index < loadlist.size(); index++) {
					temp_load = (Load) loadlist.elementAt(index);

					if (temp_load.name
							.equals(param[i + 2].getw().toUpperCase())) {
						load = temp_load;

						break;
					}
				}

				if (index == loadlist.size()) {
					throw new java.text.ParseException(
							"Load name specified does not exist", lineno);
				}

				i += 3;
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
				if (param[i + 2].getw().toUpperCase().equals("OFF")) {
					Contact = DISABLED;
				} else if (param[i + 2].getw().toUpperCase().equals("ADVANCED")) {
					Contact = ADVANCED;
				} else if (param[i + 2].getw().toUpperCase().equals("EDGE")) {
					Contact = EDGE;
				} else if (param[i + 2].getw().toUpperCase()
						.equals("ADVANCED_EDGE")) {
					Contact = ADVANCED_EDGE;
				} else {
					throw new ParseException("Unrecognized contact parameter",
							lineno);
				}

				i += 3;
			} else
			// The thickness reduction feature of the shell is specified
			if (param[i].getw().toUpperCase().equals("THINNING")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the parameter is in param3.
				if (param[i + 2].getw().toUpperCase().equals("OFF")) {
					Thinning_is_enabled = false;
				} else {
					throw new ParseException("Unrecognized thinning parameter",
							lineno);
				}

				i += 3;
			} else {
				// Neither material or nodes are defined. Then the parameter is
				// wrong.
				throw new java.text.ParseException(
						"Unknown Shell element parameter ", lineno);
			}
		}

		// **** Time to parse and initialize the contact elements *****
		// Make the input for the contact element 1
		i = 6;

		if (Factor_is_set) {
			i += 3;
		}

		if (Friction_is_set) {
			i += 3;
		}

		contact_input = new Token[i];

		// Make a local instance of a contact element to handle the contacts
		i = 6;

		if ((Contact == BASIC) || (Contact == EDGE)) {
			internal_contact_element_1 = new Contact_Triangle();
			internal_contact_element_2 = new Contact_Triangle();

			internal_contact_element_1.setNumber(-1);
			internal_contact_element_2.setNumber(-2);

			contact_input[0] = new Token(new String("nodes"));
			contact_input[1] = new Token(new String("="));
			contact_input[2] = new Token(new String("[" + node[0].getNumber()
					+ "," + node[1].getNumber() + "," + node[2].getNumber()
					+ "]"));
			contact_input[3] = new Token(new String("t"));
			contact_input[4] = new Token(new String("="));
			contact_input[5] = new Token(thickness);

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

			// Now, set the input for the contact element
			internal_contact_element_1.parse_Fembic(contact_input, lineno,
					nodelist, materiallist, loadlist, nodetable);

			// Make the input for the contact element 2
			contact_input[2] = new Token(new String("[" + node[2].getNumber()
					+ "," + node[3].getNumber() + "," + node[0].getNumber()
					+ "]"));

			// Now, set the input for the contact element also
			internal_contact_element_2.parse_Fembic(contact_input, lineno,
					nodelist, materiallist, loadlist, nodetable);
		}

		if ((Contact == ADVANCED) || (Contact == ADVANCED_EDGE)) {
			internal_contact_element_1 = new Contact_Triangle();
			internal_contact_element_2 = new Contact_Triangle();
			internal_contact_element_3 = new Contact_Triangle();
			internal_contact_element_4 = new Contact_Triangle();

			internal_contact_element_1.setNumber(-1);
			internal_contact_element_2.setNumber(-2);
			internal_contact_element_3.setNumber(-3);
			internal_contact_element_4.setNumber(-4);

			// Set up a middle node to connect all contact triangles to
			// Note that it is an internal node which makes it special compared
			// to a
			// standard node.
			// Use a negative number which is same as element number to make it
			// unique

			middle_node = new InternalNode();
			middle_node.setNumber(-number);
			middle_node.registerMasterElement(this);

			// Add the internal node to the nodelist so it becomes part of the
			// main loop
			nodelist.add(middle_node);
			nodetable.put(new Integer(-number), middle_node);

			contact_input[0] = new Token(new String("nodes"));
			contact_input[1] = new Token(new String("="));
			contact_input[2] = new Token(new String("[" + node[0].getNumber()
					+ "," + node[1].getNumber() + "," + middle_node.getNumber()
					+ "]"));
			contact_input[3] = new Token(new String("t"));
			contact_input[4] = new Token(new String("="));
			contact_input[5] = new Token(thickness);

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

			// Now, set the input for the contact element
			internal_contact_element_1.parse_Fembic(contact_input, lineno,
					nodelist, materiallist, loadlist, nodetable);

			// Make the input for the contact element 2
			contact_input[2] = new Token(new String("[" + node[1].getNumber()
					+ "," + node[2].getNumber() + "," + middle_node.getNumber()
					+ "]"));
			internal_contact_element_2.parse_Fembic(contact_input, lineno,
					nodelist, materiallist, loadlist, nodetable);

			// Make the input for the contact element 3
			contact_input[2] = new Token(new String("[" + node[2].getNumber()
					+ "," + node[3].getNumber() + "," + middle_node.getNumber()
					+ "]"));
			internal_contact_element_3.parse_Fembic(contact_input, lineno,
					nodelist, materiallist, loadlist, nodetable);

			// Make the input for the contact element 4
			contact_input[2] = new Token(new String("[" + node[3].getNumber()
					+ "," + node[0].getNumber() + "," + middle_node.getNumber()
					+ "]"));
			internal_contact_element_4.parse_Fembic(contact_input, lineno,
					nodelist, materiallist, loadlist, nodetable);

		}

		/*
		 * Make local instances of the edges contact elements to handle the
		 * contacts. Note that some of the contact_inputs are assumed set in the
		 * previous block
		 */
		if ((Contact == EDGE) || (Contact == ADVANCED_EDGE)) {
			if (!node[0].hasContact_LineElementConnectedTo(node[1])) {
				internal_contact_line_element_1 = new Contact_Line();
				internal_contact_line_element_1.setNumber(-1);

				contact_input[2] = new Token(
						new String("[" + node[0].getNumber() + ","
								+ node[1].getNumber() + "]"));
				contact_input[3] = new Token(new String("D"));
				contact_input[4] = new Token(new String("="));
				contact_input[5] = new Token(thickness);

				// Set the input
				internal_contact_line_element_1.parse_Fembic(contact_input,
						lineno, nodelist, materiallist, loadlist, nodetable);
			}

			if (!node[1].hasContact_LineElementConnectedTo(node[2])) {
				internal_contact_line_element_2 = new Contact_Line();
				internal_contact_line_element_2.setNumber(-2);

				contact_input[2] = new Token(
						new String("[" + node[1].getNumber() + ","
								+ node[2].getNumber() + "]"));
				contact_input[3] = new Token(new String("D"));
				contact_input[4] = new Token(new String("="));
				contact_input[5] = new Token(thickness);

				// Set the input
				internal_contact_line_element_2.parse_Fembic(contact_input,
						lineno, nodelist, materiallist, loadlist, nodetable);
			}

			if (!node[2].hasContact_LineElementConnectedTo(node[3])) {
				internal_contact_line_element_3 = new Contact_Line();
				internal_contact_line_element_3.setNumber(-3);

				contact_input[2] = new Token(
						new String("[" + node[2].getNumber() + ","
								+ node[3].getNumber() + "]"));
				contact_input[3] = new Token(new String("D"));
				contact_input[4] = new Token(new String("="));
				contact_input[5] = new Token(thickness);

				// Set the input
				internal_contact_line_element_3.parse_Fembic(contact_input,
						lineno, nodelist, materiallist, loadlist, nodetable);
			}

			if (!node[3].hasContact_LineElementConnectedTo(node[0])) {
				internal_contact_line_element_4 = new Contact_Line();
				internal_contact_line_element_4.setNumber(-4);

				contact_input[2] = new Token(
						new String("[" + node[3].getNumber() + ","
								+ node[0].getNumber() + "]"));
				contact_input[3] = new Token(new String("D"));
				contact_input[4] = new Token(new String("="));
				contact_input[5] = new Token(thickness);

				// Set the input
				internal_contact_line_element_4.parse_Fembic(contact_input,
						lineno, nodelist, materiallist, loadlist, nodetable);
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
					"No nodes defined for Shell_BT_4 element nr" + number);
		}

		if (!Material_is_set) {
			throw new IllegalArgumentException(
					"No Material defined for Shell_BT_4 element nr" + number);
		}

		if (!T_is_set) {
			throw new IllegalArgumentException(
					"No Thickness (T) defined for Shell_BT_4 element nr"
							+ number);
		}

		if (PIP_is_set) {
			if (printed_integration_point >= number_of_integration_points) {
				throw new IllegalArgumentException(
						"Printed integration point larger than available points");
			}

			if (printed_integration_point < 0) {
				throw new IllegalArgumentException(
						"Printed integration point less than 1");
			}
		}

		//
		if ((Contact == BASIC) || (Contact == EDGE)) {
			internal_contact_element_1.checkIndata();
			internal_contact_element_2.checkIndata();
		}

		if ((Contact == ADVANCED) || (Contact == ADVANCED_EDGE)) {
			internal_contact_element_1.checkIndata();
			internal_contact_element_2.checkIndata();
			internal_contact_element_3.checkIndata();
			internal_contact_element_4.checkIndata();
		}

		if ((Contact == EDGE) || (Contact == ADVANCED_EDGE)) {
			if (internal_contact_line_element_1 != null) {
				internal_contact_line_element_1.checkIndata();
			}

			if (internal_contact_line_element_2 != null) {
				internal_contact_line_element_2.checkIndata();
			}

			if (internal_contact_line_element_3 != null) {
				internal_contact_line_element_3.checkIndata();
			}

			if (internal_contact_line_element_4 != null) {
				internal_contact_line_element_4.checkIndata();
			}
		}
	}

	/**
	 * 
	 * The method checks destroyed element or not. If the element is destroyed
	 * variable failed = true else failed = false.
	 * 
	 */
	public void checkIfFailed() {
		if (!material[0].failureStrainIsSet()
				&& !material[0].failureStressIsSet()) {
			failed = false;
			return;
		}

		if (material[0].failureStressIsSet()) {
			// 0.70710678118654752440084436210485 = Math.sqrt(2) / 2.0
			double s = 0.7071f * Math
					.sqrt(Math.pow(stress[printed_integration_point].get(0, 0)
							- stress[printed_integration_point].get(1, 0), 2)
							+ Math.pow(
									stress[printed_integration_point].get(1, 0)
											- stress[printed_integration_point]
													.get(2, 0), 2)
							+ Math.pow(
									stress[printed_integration_point].get(2, 0)
											- stress[printed_integration_point]
													.get(0, 0), 2)
							+ (6 * (Math.pow(
									stress[printed_integration_point].get(3, 0),
									2)
									+ Math.pow(
											stress[printed_integration_point]
													.get(4, 0), 2) + Math.pow(
									stress[printed_integration_point].get(5, 0),
									2))));

			if (s > material[0].getFailureStress()) {
				failed = true;
				return;
			}
		}

		if (material[0].failureStrainIsSet()) {
			// 0.47140452079103168293389624140323 = Math.sqrt(2) / 3.0
			double e = 0.4714f * Math
					.sqrt(Math.pow(strain[printed_integration_point].get(0, 0)
							- strain[printed_integration_point].get(1, 0), 2)
							+ Math.pow(
									strain[printed_integration_point].get(1, 0)
											- strain[printed_integration_point]
													.get(2, 0), 2)
							+ Math.pow(
									strain[printed_integration_point].get(2, 0)
											- strain[printed_integration_point]
													.get(0, 0), 2)
							+ (1.5 * (Math.pow(
									strain[printed_integration_point].get(3, 0),
									2)
									+ Math.pow(
											strain[printed_integration_point]
													.get(4, 0), 2) + Math.pow(
									strain[printed_integration_point].get(5, 0),
									2))));

			if (e > material[0].getFailureStrain()) {
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
		int i;
		switch (ctrl) {
		case MESH_HEADER:

			/*
			 * Print the header for the mesh. In this case, the type of element
			 * is Quadilateral and it uses 4 nodes
			 */
			out = new String("MESH \"MeshType" + type
					+ "\" Dimension 3 ElemType Quadrilateral Nnode 4\n");

			return out;

		case MESH:

			/* Print the element number and connected nodes */
			out = new String(number + "\t" + node[0].getNumber() + "\t"
					+ node[1].getNumber() + "\t" + node[2].getNumber() + "\t"
					+ node[3].getNumber() + "\n");

			return out;

		case RESULT_HEADER:

			/*
			 * Print the header of the result file, for the block of
			 * Quadrilateral elements. The element has one gauss point where the
			 * result is calculated. In fact, the element has up to five gauss
			 * points but GID does not support gauss points through the
			 * thickness of a Quadrilateral right now. Therefore, we will pick
			 * the middle gausspoint to show the results. The point is placed on
			 * the natural position 0,0
			 */
			out = new String("GaussPoints \"Type" + type
					+ "\" ElemType Quadrilateral \"MeshType" + type + "\"\n");
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
				out = new String(number + " "); // Element number must start the
												// first gauss point results

				// The result is printed as [Sxx Syy Szz Sxy Syz Sxz]
				for (i = 0; i < 6; i++) {
					out += (stress[printed_integration_point].get(i, 0) + "\t");
				}

				out += "\n";
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
				out = new String(number + " "); // Element number must start the
												// first gauss point results

				// The result is printed as [exx eyy ezz exy eyz exz]
				for (i = 0; i < 6; i++) {
					out += (strain[printed_integration_point].get(i, 0) + "\t");
				}

				out += "\n";
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

		case Element.MESH:

			/* Print the element number and connected nodes */
			out = new String(number + "\t nodes = [" + node[0].getNumber()
					+ "," + node[1].getNumber() + "," + node[2].getNumber()
					+ "," + node[3].getNumber() + "]\t" + "T = " + thickness
					+ "\t" + "material = " + material[0].getName() + "\t");

			if (Factor_is_set)
				out += "factor = " + factor;

			if (Friction_is_set)
				out += "friction = " + friction;

			if (Contact == DISABLED)
				out += " contact = OFF";

			if (Contact == EDGE)
				out += " contact = EDGE";

			if (Contact == ADVANCED)
				out += " contact = ADVANCED";

			if (Contact == ADVANCED_EDGE)
				out += " contact = ADVANCED_EDGE";

			if (NIP_is_set)
				out += " nip = " + number_of_integration_points;

			if (PIP_is_set)
				out += " pip = " + printed_integration_point;

			if (shear_factor != 0)
				out += " shear_factor = " + shear_factor;

			if (hourglass_control)
				out += " hourglass = ON";

			if (membrane_hourglass_coeff != 0)
				out += " mhc = " + membrane_hourglass_coeff;

			if (out_of_plane_hourglass_coeff != 0)
				out += " oophc = " + out_of_plane_hourglass_coeff;

			if (rotation_hourglass_coeff != 0)
				out += " rhc = " + rotation_hourglass_coeff;

			if (load != null)
				out += " load = " + load.getName();

			if (!Thinning_is_enabled)
				out += " thinning = OFF";

			out += "\n";

			return out;

		default:
			return new String("");
		}
	}

	/**
	 * Insert the method's description here. Creation date: (25/12/01 %T)
	 */
	public void setInitialConditions() throws IllegalArgumentException {
		int i;

		/*
		 * Make a local copy of the material object and keep it inside the
		 * element. The reason for this is twofold. 1. The material law is now
		 * tied to the element and will remember the element properties and
		 * history 2. Since the material is cloned now, after Initialize, all
		 * the parameters are defined in the material object and are now
		 * automatically transferred to the local copy.
		 * 
		 * Note that one copy of the materiallaw is required for each
		 * intergration point in the element.
		 */
		try {
			for (i = 0; i < number_of_integration_points; i++) {
				material[i] = (Material) material[0].copy();
			}
		} catch (CloneNotSupportedException e) {
			System.err.println("Object cannot clone");
		}

		// Now call any necessary initialisations in the law.
		for (i = 0; i < number_of_integration_points; i++) {
			material[i].setInitialConditions();
		}

		// Set up the index and weights of the integration points for a gaussian
		// integration
		if (number_of_integration_points == 1) {
			z[0] = 0 * (thickness / 2.0);
			weight_factor[0] = 2.0;
		} else if (number_of_integration_points == 2) {
			z[0] = 0.577350269189626 * (thickness / 2.0);
			weight_factor[0] = 1.0;
			z[1] = -0.577350269189626 * (thickness / 2.0);
			weight_factor[1] = 1.0;
		} else if (number_of_integration_points == 3) {
			z[0] = 0.774596669241483 * (thickness / 2.0);
			weight_factor[0] = 5.0 / 9.0;
			z[1] = 0 * (thickness / 2.0);
			weight_factor[1] = 8.0 / 9.0;
			z[2] = -0.774596669241483 * (thickness / 2.0);
			weight_factor[2] = 5.0 / 9.0;
		} else if (number_of_integration_points == 4) {
			z[0] = 0.861136311594053 * (thickness / 2.0);
			weight_factor[0] = 0.347854845137454;
			z[1] = 0.339981043584856 * (thickness / 2.0);
			weight_factor[1] = 0.652145154862546;
			z[2] = -0.339981043584856 * (thickness / 2.0);
			weight_factor[2] = 0.652145154862546;
			z[3] = -0.861136311594053 * (thickness / 2.0);
			weight_factor[1] = 0.347854845137454;
		} else if (number_of_integration_points == 5) {
			z[0] = 0.90618 * (thickness / 2.0);
			weight_factor[0] = 0.236927;
			z[1] = 0.538469 * (thickness / 2.0);
			weight_factor[1] = 0.478629;
			z[2] = 0 * (thickness / 2.0);
			weight_factor[2] = 0.568889;
			z[3] = -0.538469 * (thickness / 2.0);
			weight_factor[3] = 0.478629;
			z[4] = -0.90618 * (thickness / 2.0);
			weight_factor[4] = 0.236927;
		}

		// Define the hourglass vector
		hourglass_vector.set(0, 0, 1);
		hourglass_vector.set(1, 0, -1);
		hourglass_vector.set(2, 0, 1);
		hourglass_vector.set(3, 0, -1);

		// Set initial conditions on the contact elements
		if ((Contact == BASIC) || (Contact == EDGE)) {
			internal_contact_element_1.setInitialConditions();
			internal_contact_element_2.setInitialConditions();
		}

		if ((Contact == ADVANCED) || (Contact == ADVANCED_EDGE)) {
			internal_contact_element_1.setInitialConditions();
			internal_contact_element_2.setInitialConditions();
			internal_contact_element_3.setInitialConditions();
			internal_contact_element_4.setInitialConditions();

		}

		if ((Contact == EDGE) || (Contact == ADVANCED_EDGE)) {
			if (internal_contact_line_element_1 != null) {
				internal_contact_line_element_1.setInitialConditions();
			}

			if (internal_contact_line_element_2 != null) {
				internal_contact_line_element_2.setInitialConditions();
			}

			if (internal_contact_line_element_3 != null) {
				internal_contact_line_element_3.setInitialConditions();
			}

			if (internal_contact_line_element_4 != null) {
				internal_contact_line_element_4.setInitialConditions();
			}
		}

		// Print by default the middle integration point if nothing else
		// specified
		if (!PIP_is_set) {
			printed_integration_point = number_of_integration_points / 2;
		}
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
	public void updateLocalCoordinateSystem() {
		this.calculateLocalBaseVectors(node[0].getX_pos(), node[0].getY_pos(),
				node[0].getZ_pos(), node[1].getX_pos(), node[1].getY_pos(),
				node[1].getZ_pos(), node[2].getX_pos(), node[2].getY_pos(),
				node[2].getZ_pos(), node[3].getX_pos(), node[3].getY_pos(),
				node[3].getZ_pos(), local_coordinate_system);

		if ((Contact == BASIC) || (Contact == EDGE)) {
			internal_contact_element_1.updateLocalCoordinateSystem();
			internal_contact_element_2.updateLocalCoordinateSystem();
		}

		if ((Contact == ADVANCED) || (Contact == ADVANCED_EDGE)) {
			internal_contact_element_1.updateLocalCoordinateSystem();
			internal_contact_element_2.updateLocalCoordinateSystem();
			internal_contact_element_3.updateLocalCoordinateSystem();
			internal_contact_element_4.updateLocalCoordinateSystem();
		}

		if ((Contact == EDGE) || (Contact == ADVANCED_EDGE)) {
			if (internal_contact_line_element_1 != null) {
				internal_contact_line_element_1.updateLocalCoordinateSystem();
			}

			if (internal_contact_line_element_2 != null) {
				internal_contact_line_element_2.updateLocalCoordinateSystem();
			}

			if (internal_contact_line_element_3 != null) {
				internal_contact_line_element_3.updateLocalCoordinateSystem();
			}

			if (internal_contact_line_element_4 != null) {
				internal_contact_line_element_4.updateLocalCoordinateSystem();
			}
		}
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
		trash.set(9, 0, node[3].getX_pos());
		trash.set(10, 0, node[3].getY_pos());
		trash.set(11, 0, node[3].getZ_pos());

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

		// p.setMatrix(9,11,0,0,local_coordinate_system.times(trash.getMatrix(9,11,0,0)));
		p.set(9, 0, (local_coordinate_system.get(0, 0) * trash.get(9, 0))
				+ (local_coordinate_system.get(0, 1) * trash.get(10, 0))
				+ (local_coordinate_system.get(0, 2) * trash.get(11, 0)));
		p.set(10, 0, (local_coordinate_system.get(1, 0) * trash.get(9, 0))
				+ (local_coordinate_system.get(1, 1) * trash.get(10, 0))
				+ (local_coordinate_system.get(1, 2) * trash.get(11, 0)));
		p.set(11, 0, (local_coordinate_system.get(2, 0) * trash.get(9, 0))
				+ (local_coordinate_system.get(2, 1) * trash.get(10, 0))
				+ (local_coordinate_system.get(2, 2) * trash.get(11, 0)));

		// Update the v matrix
		trash.set(0, 0, node[0].getX_vel());
		trash.set(1, 0, node[0].getY_vel());
		trash.set(2, 0, node[0].getZ_vel());
		trash.set(3, 0, node[1].getX_vel());
		trash.set(4, 0, node[1].getY_vel());
		trash.set(5, 0, node[1].getZ_vel());
		trash.set(6, 0, node[2].getX_vel());
		trash.set(7, 0, node[2].getY_vel());
		trash.set(8, 0, node[2].getZ_vel());
		trash.set(9, 0, node[3].getX_vel());
		trash.set(10, 0, node[3].getY_vel());
		trash.set(11, 0, node[3].getZ_vel());

		// And transform into local coordinate system
		// v.setMatrix(0,2,0,0,local_coordinate_system.times(trash.getMatrix(0,2,0,0)));
		v.set(0, 0, (local_coordinate_system.get(0, 0) * trash.get(0, 0))
				+ (local_coordinate_system.get(0, 1) * trash.get(1, 0))
				+ (local_coordinate_system.get(0, 2) * trash.get(2, 0)));
		v.set(1, 0, (local_coordinate_system.get(1, 0) * trash.get(0, 0))
				+ (local_coordinate_system.get(1, 1) * trash.get(1, 0))
				+ (local_coordinate_system.get(1, 2) * trash.get(2, 0)));
		v.set(2, 0, (local_coordinate_system.get(2, 0) * trash.get(0, 0))
				+ (local_coordinate_system.get(2, 1) * trash.get(1, 0))
				+ (local_coordinate_system.get(2, 2) * trash.get(2, 0)));

		// v.setMatrix(3,5,0,0,local_coordinate_system.times(trash.getMatrix(3,5,0,0)));
		v.set(3, 0, (local_coordinate_system.get(0, 0) * trash.get(3, 0))
				+ (local_coordinate_system.get(0, 1) * trash.get(4, 0))
				+ (local_coordinate_system.get(0, 2) * trash.get(5, 0)));
		v.set(4, 0, (local_coordinate_system.get(1, 0) * trash.get(3, 0))
				+ (local_coordinate_system.get(1, 1) * trash.get(4, 0))
				+ (local_coordinate_system.get(1, 2) * trash.get(5, 0)));
		v.set(5, 0, (local_coordinate_system.get(2, 0) * trash.get(3, 0))
				+ (local_coordinate_system.get(2, 1) * trash.get(4, 0))
				+ (local_coordinate_system.get(2, 2) * trash.get(5, 0)));

		// v.setMatrix(6,8,0,0,local_coordinate_system.times(trash.getMatrix(6,8,0,0)));
		v.set(6, 0, (local_coordinate_system.get(0, 0) * trash.get(6, 0))
				+ (local_coordinate_system.get(0, 1) * trash.get(7, 0))
				+ (local_coordinate_system.get(0, 2) * trash.get(8, 0)));
		v.set(7, 0, (local_coordinate_system.get(1, 0) * trash.get(6, 0))
				+ (local_coordinate_system.get(1, 1) * trash.get(7, 0))
				+ (local_coordinate_system.get(1, 2) * trash.get(8, 0)));
		v.set(8, 0, (local_coordinate_system.get(2, 0) * trash.get(6, 0))
				+ (local_coordinate_system.get(2, 1) * trash.get(7, 0))
				+ (local_coordinate_system.get(2, 2) * trash.get(8, 0)));

		// v.setMatrix(9,11,0,0,local_coordinate_system.times(trash.getMatrix(9,11,0,0)));
		v.set(9, 0, (local_coordinate_system.get(0, 0) * trash.get(9, 0))
				+ (local_coordinate_system.get(0, 1) * trash.get(10, 0))
				+ (local_coordinate_system.get(0, 2) * trash.get(11, 0)));
		v.set(10, 0, (local_coordinate_system.get(1, 0) * trash.get(9, 0))
				+ (local_coordinate_system.get(1, 1) * trash.get(10, 0))
				+ (local_coordinate_system.get(1, 2) * trash.get(11, 0)));
		v.set(11, 0, (local_coordinate_system.get(2, 0) * trash.get(9, 0))
				+ (local_coordinate_system.get(2, 1) * trash.get(10, 0))
				+ (local_coordinate_system.get(2, 2) * trash.get(11, 0)));

		// Calculate the rotational velocity array
		trash.set(0, 0, node[0].getX_rot_vel());
		trash.set(1, 0, node[0].getY_rot_vel());
		trash.set(2, 0, node[0].getZ_rot_vel());
		trash.set(3, 0, node[1].getX_rot_vel());
		trash.set(4, 0, node[1].getY_rot_vel());
		trash.set(5, 0, node[1].getZ_rot_vel());
		trash.set(6, 0, node[2].getX_rot_vel());
		trash.set(7, 0, node[2].getY_rot_vel());
		trash.set(8, 0, node[2].getZ_rot_vel());
		trash.set(9, 0, node[3].getX_rot_vel());
		trash.set(10, 0, node[3].getY_rot_vel());
		trash.set(11, 0, node[3].getZ_rot_vel());

		// And transform into local coordinate system
		// a.setMatrix(0,2,0,0,local_coordinate_system.times(trash.getMatrix(0,2,0,0)));
		a.set(0, 0, (local_coordinate_system.get(0, 0) * trash.get(0, 0))
				+ (local_coordinate_system.get(0, 1) * trash.get(1, 0))
				+ (local_coordinate_system.get(0, 2) * trash.get(2, 0)));
		a.set(1, 0, (local_coordinate_system.get(1, 0) * trash.get(0, 0))
				+ (local_coordinate_system.get(1, 1) * trash.get(1, 0))
				+ (local_coordinate_system.get(1, 2) * trash.get(2, 0)));
		a.set(2, 0, (local_coordinate_system.get(2, 0) * trash.get(0, 0))
				+ (local_coordinate_system.get(2, 1) * trash.get(1, 0))
				+ (local_coordinate_system.get(2, 2) * trash.get(2, 0)));

		// a.setMatrix(3,5,0,0,local_coordinate_system.times(trash.getMatrix(3,5,0,0)));
		a.set(3, 0, (local_coordinate_system.get(0, 0) * trash.get(3, 0))
				+ (local_coordinate_system.get(0, 1) * trash.get(4, 0))
				+ (local_coordinate_system.get(0, 2) * trash.get(5, 0)));
		a.set(4, 0, (local_coordinate_system.get(1, 0) * trash.get(3, 0))
				+ (local_coordinate_system.get(1, 1) * trash.get(4, 0))
				+ (local_coordinate_system.get(1, 2) * trash.get(5, 0)));
		a.set(5, 0, (local_coordinate_system.get(2, 0) * trash.get(3, 0))
				+ (local_coordinate_system.get(2, 1) * trash.get(4, 0))
				+ (local_coordinate_system.get(2, 2) * trash.get(5, 0)));

		// a.setMatrix(6,8,0,0,local_coordinate_system.times(trash.getMatrix(6,8,0,0)));
		a.set(6, 0, (local_coordinate_system.get(0, 0) * trash.get(6, 0))
				+ (local_coordinate_system.get(0, 1) * trash.get(7, 0))
				+ (local_coordinate_system.get(0, 2) * trash.get(8, 0)));
		a.set(7, 0, (local_coordinate_system.get(1, 0) * trash.get(6, 0))
				+ (local_coordinate_system.get(1, 1) * trash.get(7, 0))
				+ (local_coordinate_system.get(1, 2) * trash.get(8, 0)));
		a.set(8, 0, (local_coordinate_system.get(2, 0) * trash.get(6, 0))
				+ (local_coordinate_system.get(2, 1) * trash.get(7, 0))
				+ (local_coordinate_system.get(2, 2) * trash.get(8, 0)));

		// a.setMatrix(9,11,0,0,local_coordinate_system.times(trash.getMatrix(9,11,0,0)));
		a.set(9, 0, (local_coordinate_system.get(0, 0) * trash.get(9, 0))
				+ (local_coordinate_system.get(0, 1) * trash.get(10, 0))
				+ (local_coordinate_system.get(0, 2) * trash.get(11, 0)));
		a.set(10, 0, (local_coordinate_system.get(1, 0) * trash.get(9, 0))
				+ (local_coordinate_system.get(1, 1) * trash.get(10, 0))
				+ (local_coordinate_system.get(1, 2) * trash.get(11, 0)));
		a.set(11, 0, (local_coordinate_system.get(2, 0) * trash.get(9, 0))
				+ (local_coordinate_system.get(2, 1) * trash.get(10, 0))
				+ (local_coordinate_system.get(2, 2) * trash.get(11, 0)));

		/*
		 * Calculate element area Normal formula works for small distorsions:
		 * area = (1/2)*(X31*Y42 + X24*Y31) Note that for large deformations,
		 * the area will become very small as the element reaches self contact.
		 * The area is then corrected and recalculated in the calculateStrain()
		 * method if the ADVANCED contact option is used.
		 */
		area = 0.5 * (((p.get(6, 0) - p.get(0, 0)) * (p.get(10, 0) - p
				.get(4, 0))) + ((p.get(3, 0) - p.get(9, 0)) * (p.get(7, 0) - p
				.get(1, 0))));

	}

	/**
	 * This method is called by any internal nodes that this element uses. This
	 * is the case if advanced or advanced_edge contact options have been used.
	 * The method is called by the internal node when the calculateNewPosition
	 * is run for all the nodes in the main loop.
	 */

	public void setInternalNodePosition() {

		// Update the position of the local node if needed
		middle_node.setX_pos_orig((node[0].getX_pos() + node[1].getX_pos()
				+ node[2].getX_pos() + node[3].getX_pos()) / 4);
		middle_node.setY_pos_orig((node[0].getY_pos() + node[1].getY_pos()
				+ node[2].getY_pos() + node[3].getY_pos()) / 4);
		middle_node.setZ_pos_orig((node[0].getZ_pos() + node[1].getZ_pos()
				+ node[2].getZ_pos() + node[3].getZ_pos()) / 4);
	}

	public Node getInternalNode() {
		return middle_node;
	}

	/**
	 * Is used to deactivate the element when fractured.
	 */
	public void deActivate() {
		super.deActivate();
		if (internal_contact_element_1 != null)
			internal_contact_element_1.deActivate();
		if (internal_contact_element_2 != null)
			internal_contact_element_2.deActivate();
		if (internal_contact_element_3 != null)
			internal_contact_element_3.deActivate();
		if (internal_contact_element_4 != null)
			internal_contact_element_4.deActivate();
		if (internal_contact_line_element_1 != null)
			internal_contact_line_element_1.deActivate();
		if (internal_contact_line_element_2 != null)
			internal_contact_line_element_2.deActivate();
		if (internal_contact_line_element_3 != null)
			internal_contact_line_element_3.deActivate();
		if (internal_contact_line_element_4 != null)
			internal_contact_line_element_4.deActivate();
		if (middle_node != null)
			middle_node.deActivate();
	}

}

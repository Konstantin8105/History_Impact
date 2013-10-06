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
public class Shell_C0_3 extends Element {
	private static int DISABLED = 0;
	private static int BASIC = 1;
	private static int EDGE = 2;
	private double area;
	private Jama.Matrix local_coordinate_system;
	private Jama.Matrix[] strain;
	private Jama.Matrix[] dstrain;
	private Jama.Matrix strainrate;
	private Jama.Matrix[] stress;
	private Jama.Matrix force;
	private Jama.Matrix moment;
	private Jama.Matrix Bm;
	private Jama.Matrix Bb;
	private Jama.Matrix Bs;
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
	private Load load;
	private boolean NIP_is_set;
	private boolean PIP_is_set;
	private boolean Nodes_are_set;
	private boolean Material_is_set;
	private boolean T_is_set;
	private boolean Factor_is_set;
	private boolean Friction_is_set;
	private boolean Thinning_is_enabled;
	private Contact_Triangle internal_contact_element;
	private Contact_Line internal_contact_line_element_1;
	private Contact_Line internal_contact_line_element_2;
	private Contact_Line internal_contact_line_element_3;

	// Attributes
	// Associations
	// Operations

	/**
	 * Constructor Shell_C0_3
	 */
	public Shell_C0_3() {
		super();
		type = new String("SHELL_C0_3");

		int i;
		p = new Jama.Matrix(9, 1);
		v = new Jama.Matrix(9, 1);
		a = new Jama.Matrix(9, 1);
		trash = new Jama.Matrix(9, 1);
		Bm = new Jama.Matrix(3, 6);
		Bb = new Jama.Matrix(3, 6);
		Bs = new Jama.Matrix(2, 6);
		node = new Node[3];
		force = new Jama.Matrix(9, 1);
		moment = new Jama.Matrix(9, 1);
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

		// Default setting is that contact sensitivity is enabled
		Contact = BASIC;
		Thinning_is_enabled = true;
		number_of_integration_points = 5;
	}

	/**
	 * Assemble Mass Matrix
	 */
	public void assembleMassMatrix() throws IllegalArgumentException {
		Jama.Matrix inertia;
		inertia = new Jama.Matrix(3, 3);

		double I;
		double mass;

		// Update local coordinate system
		this.updateLocalCoordinateSystem();

		// Determine the element Area
		this.calculateLocalVariables();

		// Distribute the mass contribution on the nodes
		mass = material[0].getDensity() * area * thickness;
		node[0].addMass(mass / 3);
		node[1].addMass(mass / 3);
		node[2].addMass(mass / 3);

		/*
		 * Now, calculate the inertia. [Ixx Iyx Izx] I = [Ixy Iyy Izy] [Ixz Iyz
		 * Izz] With the exact formula for intertia, the solution tends to
		 * diverge for large rotation rates. A way to stabalize the rotation is
		 * to let Izz = Iyy = Ixx , Ixy... = 0 and concider the rectangle as a
		 * square. This gives the following:
		 */
		I = (mass / 3) * ((area / 12) + ((thickness * thickness) / 12));

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

		//
		if ((Contact == BASIC) || (Contact == EDGE)) {
			internal_contact_element.assembleMassMatrix();
		}

		if (Contact == EDGE) {
			if (internal_contact_line_element_1 != null) {
				internal_contact_line_element_1.assembleMassMatrix();
			}

			if (internal_contact_line_element_2 != null) {
				internal_contact_line_element_2.assembleMassMatrix();
			}

			if (internal_contact_line_element_3 != null) {
				internal_contact_line_element_3.assembleMassMatrix();
			}
		}
	}

	/**
	 * Calculate Contact Forces
	 */
	public void calculateContactForces() {
		// Use the internal contact element to calculate the contact forces.
		if ((Contact == BASIC) || (Contact == EDGE)) {
			internal_contact_element.calculateContactForces();
		}

		if (Contact == EDGE) {
			if (internal_contact_line_element_1 != null) {
				internal_contact_line_element_1.calculateContactForces();
			}

			if (internal_contact_line_element_2 != null) {
				internal_contact_line_element_2.calculateContactForces();
			}

			if (internal_contact_line_element_3 != null) {
				internal_contact_line_element_3.calculateContactForces();
			}
		}
	}

	/**
	 * Calculate External Forces
	 */
	public void calculateExternalForces(double currtime) {
		int i;
		Matrix f = new Matrix(3, 1);

		// Add the pressure force in local z-direction if there is a load object
		// defined
		if (load != null) {
			f.set(0, 0, 0);
			f.set(1, 0, 0);
			f.set(2, 0, (area * load.getPressure(currtime)) / 3);

			// Transform to global directions
			f = local_coordinate_system.transpose().times(f);

			// Distribute on nodes (note this is external forces so no sign
			// reversal)
			for (i = 0; i < 3; i++) {
				node[i].addExternalForce(f);
			}
		}

		//
		if ((Contact == BASIC) || (Contact == EDGE)) {
			internal_contact_element.calculateExternalForces(currtime);
		}

		if (Contact == EDGE) {
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
		}
	}

	/**
	 * This calculates and adds the element internal forces to the nodes. Note,
	 * by definition, the internal forces are subtracted from the external loads
	 * Thus, the "addition" is negative.
	 * 
	 */
	public void calculateNodalForces(int integration_point, double timestep) {
		Matrix f;
		Matrix ft;
		Matrix transposed_coordinate_system;

		// Check the area of the element
		if (integration_point == 0) {
			if (area <= 0) {
				throw new IllegalArgumentException(
						"Error in Shell_C0_3 element. Negative or zero area. ");
			}
		}

		// Calculate forces fx, fy , fz
		f = new Matrix(3, 1);
		f.set(0, 0, 0.5 * thickness * weight_factor[integration_point]
				* stress[integration_point].get(0, 0));
		f.set(1, 0, 0.5 * thickness * weight_factor[integration_point]
				* stress[integration_point].get(1, 0));
		f.set(2, 0, 0.5 * thickness * weight_factor[integration_point]
				* stress[integration_point].get(3, 0));
		force.setMatrix(0, 5, 0, 0, Bm.transpose().times(f).times(area));

		// Calculate moments mx, my, mz
		// For those who are studying this in detail and is wondering why the
		// formula is 0.5*thickness and not 0.25*thickness*thickness, remember
		// that z[integration_point] already includes 0.5*thickness
		f.timesEquals(-z[integration_point]);

		ft = new Matrix(2, 1);
		ft.set(0, 0, 0.5 * thickness * weight_factor[integration_point]
				* stress[integration_point].get(5, 0));
		ft.set(1, 0, 0.5 * thickness * weight_factor[integration_point]
				* stress[integration_point].get(4, 0));
		moment.setMatrix(
				0,
				5,
				0,
				0,
				Bb.transpose().times(f).plus(Bs.transpose().times(ft))
						.times(area));

		// Now, rearrange a little and calculate the fz contributions
		force.setMatrix(6, 7, 0, 0, force.getMatrix(4, 5, 0, 0));
		force.setMatrix(3, 4, 0, 0, force.getMatrix(2, 3, 0, 0));
		moment.setMatrix(6, 7, 0, 0, moment.getMatrix(4, 5, 0, 0));
		moment.setMatrix(3, 4, 0, 0, moment.getMatrix(2, 3, 0, 0));

		force.set(
				8,
				0,
				-(moment.get(0, 0) + moment.get(3, 0) + moment.get(6, 0))
						/ p.get(7, 0));
		force.set(
				5,
				0,
				((moment.get(1, 0) + moment.get(4, 0) + moment.get(7, 0)) - (p
						.get(6, 0) * force.get(8, 0))) / p.get(3, 0));
		force.set(2, 0, -force.get(5, 0) - force.get(8, 0));

		// No moment loads around local z-axis.
		moment.set(2, 0, 0);
		moment.set(5, 0, 0);
		moment.set(8, 0, 0);

		// .. and transform to global coordinate system
		transposed_coordinate_system = local_coordinate_system.transpose();

		moment.setMatrix(0, 2, 0, 0, transposed_coordinate_system.times(moment
				.getMatrix(0, 2, 0, 0)));
		moment.setMatrix(3, 5, 0, 0, transposed_coordinate_system.times(moment
				.getMatrix(3, 5, 0, 0)));
		moment.setMatrix(6, 8, 0, 0, transposed_coordinate_system.times(moment
				.getMatrix(6, 8, 0, 0)));
		force.setMatrix(0, 2, 0, 0,
				transposed_coordinate_system.times(force.getMatrix(0, 2, 0, 0)));
		force.setMatrix(3, 5, 0, 0,
				transposed_coordinate_system.times(force.getMatrix(3, 5, 0, 0)));
		force.setMatrix(6, 8, 0, 0,
				transposed_coordinate_system.times(force.getMatrix(6, 8, 0, 0)));

		/*
		 * Now, add the force and moment loads to the node. Remember that
		 * internal forces are negative and external are positive.
		 */
		force.timesEquals(-1.0);
		moment.timesEquals(-1.0);

		node[0].addInternalForce(force.getMatrix(0, 2, 0, 0));
		node[0].addInternalMoment(moment.getMatrix(0, 2, 0, 0));
		node[1].addInternalForce(force.getMatrix(3, 5, 0, 0));
		node[1].addInternalMoment(moment.getMatrix(3, 5, 0, 0));
		node[2].addInternalForce(force.getMatrix(6, 8, 0, 0));
		node[2].addInternalMoment(moment.getMatrix(6, 8, 0, 0));
	}

	/**
	 * v is an array of node velocities expressed in the local coordinate system
	 * v = [u1 v1 w1 u2 v2 w2 u3 v3 w3] p is an array of node coordinates
	 * expessed in the local coordinate system p = [x1 y1 z1 x2 y2 z2 x3 y3 z3]
	 * a is an array of node rotation velocity expressed in the local coordinate
	 * system a = [ax1 ay1 az1 ax2 ay2 az2 ax3 ay3 az3] Bm, Bb and Bs are
	 * matrices of the shape function derivatives.
	 * 
	 * @param tstep
	 *            double
	 */
	public void calculateStrain(double tstep, int integration_point) {
		double rbx;
		double rby;

		// Do this block only once for each timestep. Not for each integration
		// point
		if (integration_point == 0) {
			this.updateLocalCoordinateSystem();
			this.calculateLocalVariables();

			/*
			 * Now that we have the input in local coordinate system, it is time
			 * to calculate the strain rates. Start by creating the B matrix
			 * (the derivate of the shape functions) which will be used
			 * frequently in the rest of the calculations.
			 */
			Bm.set(0, 0, -p.get(7, 0));
			Bm.set(0, 2, p.get(7, 0));
			Bm.set(1, 1, p.get(6, 0) - p.get(3, 0));
			Bm.set(1, 3, -p.get(6, 0));
			Bm.set(1, 5, p.get(3, 0));
			Bm.set(2, 0, p.get(6, 0) - p.get(3, 0));
			Bm.set(2, 1, -p.get(7, 0));
			Bm.set(2, 2, -p.get(6, 0));
			Bm.set(2, 3, p.get(7, 0));
			Bm.set(2, 4, p.get(3, 0));

			Bm.timesEquals(1.0 / (2.0 * area));

			Bb.set(0, 1, -p.get(7, 0));
			Bb.set(0, 3, p.get(7, 0));
			Bb.set(1, 0, p.get(3, 0) - p.get(6, 0));
			Bb.set(1, 2, p.get(6, 0));
			Bb.set(1, 4, -p.get(3, 0));
			Bb.set(2, 0, p.get(7, 0));
			Bb.set(2, 1, p.get(6, 0) - p.get(3, 0));
			Bb.set(2, 2, -p.get(7, 0));
			Bb.set(2, 3, -p.get(6, 0));
			Bb.set(2, 5, p.get(3, 0));

			Bb.timesEquals(-1.0 / (2.0 * area));

			Bs.set(0, 0, -p.get(7, 0) * p.get(7, 0));
			Bs.set(0, 1, p.get(7, 0) * ((2.0 * p.get(3, 0)) + p.get(6, 0)));
			Bs.set(0, 2, p.get(7, 0) * p.get(7, 0));
			Bs.set(0, 3, p.get(7, 0) * ((3.0 * p.get(3, 0)) - p.get(6, 0)));
			Bs.set(0, 5, p.get(3, 0) * p.get(7, 0));
			Bs.set(1, 0, p.get(7, 0) * (p.get(6, 0) - (2.0 * p.get(3, 0))));
			Bs.set(1, 1,
					(p.get(3, 0) * p.get(3, 0)) - (p.get(6, 0) * p.get(6, 0)));
			Bs.set(1, 2, -p.get(7, 0) * (p.get(3, 0) + p.get(6, 0)));
			Bs.set(1, 3, p.get(6, 0) * (p.get(6, 0) - (2.0 * p.get(3, 0))));
			Bs.set(1, 4, -3.0 * p.get(3, 0) * p.get(7, 0));
			Bs.set(1, 5, p.get(3, 0) * ((2.0 * p.get(6, 0)) - p.get(3, 0)));

			Bs.timesEquals(1.0 / (12.0 * area));

			// Determine the rigid body rotation and subtract
			rbx = (((v.get(8, 0) - v.get(2, 0)) * p.get(3, 0)) - ((v.get(5, 0) - v
					.get(2, 0)) * p.get(6, 0))) / (p.get(3, 0) * p.get(7, 0));
			rby = (v.get(2, 0) - v.get(5, 0)) / p.get(3, 0);

			a.set(0, 0, a.get(0, 0) - rbx);
			a.set(1, 0, a.get(1, 0) - rby);
			a.set(3, 0, a.get(3, 0) - rbx);
			a.set(4, 0, a.get(4, 0) - rby);
			a.set(6, 0, a.get(6, 0) - rbx);
			a.set(7, 0, a.get(7, 0) - rby);

			/*
			 * Now, the strain rates can be determined. They are calculated in
			 * the middle of the shell and then transformed out through the
			 * thickness. Start by calculating the middle strain rates:
			 */
			dmx = (Bm.get(0, 0) * v.get(0, 0)) + (Bm.get(0, 2) * v.get(3, 0));
			dmy = (Bm.get(1, 1) * v.get(1, 0)) + (Bm.get(1, 3) * v.get(4, 0))
					+ (Bm.get(1, 5) * v.get(7, 0));
			dmxy = (Bm.get(2, 0) * v.get(0, 0)) + (Bm.get(2, 1) * v.get(1, 0))
					+ (Bm.get(2, 2) * v.get(3, 0))
					+ (Bm.get(2, 3) * v.get(4, 0))
					+ (Bm.get(2, 4) * v.get(6, 0));

			//
			kappax = (Bb.get(0, 1) * a.get(1, 0))
					+ (Bb.get(0, 3) * a.get(4, 0));
			kappay = (Bb.get(1, 0) * a.get(0, 0))
					+ (Bb.get(1, 2) * a.get(3, 0))
					+ (Bb.get(1, 4) * a.get(6, 0));
			kappaxy = (Bb.get(2, 0) * a.get(0, 0))
					+ (Bb.get(2, 1) * a.get(1, 0))
					+ (Bb.get(2, 2) * a.get(3, 0))
					+ (Bb.get(2, 3) * a.get(4, 0))
					+ (Bb.get(2, 5) * a.get(7, 0));
		}

		// Now, compute the strainrate for this integration point.
		strainrate.set(0, 0, dmx - (z[integration_point] * kappax));
		strainrate.set(1, 0, dmy - (z[integration_point] * kappay));
		strainrate.set(3, 0, dmxy - (z[integration_point] * kappaxy));

		// The bending strain rates
		// dyz
		strainrate.set(
				4,
				0,
				(Bs.get(1, 0) * a.get(0, 0)) + (Bs.get(1, 1) * a.get(1, 0))
						+ (Bs.get(1, 2) * a.get(3, 0))
						+ (Bs.get(1, 3) * a.get(4, 0))
						+ (Bs.get(1, 4) * a.get(6, 0))
						+ (Bs.get(1, 5) * a.get(7, 0)));

		// dxz
		strainrate.set(
				5,
				0,
				(Bs.get(0, 0) * a.get(0, 0)) + (Bs.get(0, 1) * a.get(1, 0))
						+ (Bs.get(0, 2) * a.get(3, 0))
						+ (Bs.get(0, 3) * a.get(4, 0))
						+ (Bs.get(0, 5) * a.get(7, 0)));

		// Knowing the strainrate, the strain increase for this integration
		// point is easily computed.
		dstrain[integration_point] = strainrate.times(tstep);
	}

	/*
	 * This calculates the stresses using the strain and strain increase. The
	 * result is put back into the stress matrix. The element contains one local
	 * material object for each integration point since some materials has a
	 * memory which must be maintaned.
	 */
	public void calculateStress(int integration_point, double timestep) {

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
	 * Check Time Step.
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
				Math.sqrt(((node[0].getX_pos() - node[2].getX_pos()) * (node[0]
						.getX_pos() - node[2].getX_pos()))
						+ ((node[0].getY_pos() - node[2].getY_pos()) * (node[0]
								.getY_pos() - node[2].getY_pos()))
						+ ((node[0].getZ_pos() - node[2].getZ_pos()) * (node[0]
								.getZ_pos() - node[2].getZ_pos()))));
		critical_length = (2.0 * area) / critical_length;

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
	 * This element only has one integration point (gauss point)
	 * 
	 * @return int
	 */
	public int getNumberOfIntegrationPoints() {
		return number_of_integration_points;
	}

	/**
	 * Insert the method's description here. Creation date: (25/12/01 %PC%T)
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
							"Error, node number definition should be [nodenr1,nodenr2,nodenr3]",
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
					throw new ParseException(e.getMessage() + "In line ",
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
			// The contact element of the shell is specified
			if (param[i].getw().toUpperCase().equals("CONTACT")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the element thickness is in param3. Set this in
				// the element
				if (param[i + 2].getw().toUpperCase().equals("OFF")) {
					Contact = DISABLED;
				} else if (param[i + 2].getw().toUpperCase().equals("EDGE")) {
					Contact = EDGE;
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

		// Make a local instance of a contact element to handle the contacts
		i = 6;

		if (Factor_is_set) {
			i += 3;
		}

		if (Friction_is_set) {
			i += 3;
		}

		contact_input = new Token[i];

		// Make the input for the contact element
		i = 6;

		if ((Contact == BASIC) || (Contact == EDGE)) {
			internal_contact_element = new Contact_Triangle();
			internal_contact_element.setNumber(-1);

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

			// Now, set the input for the contact element also
			internal_contact_element.parse_Fembic(contact_input, lineno,
					nodelist, materiallist, loadlist, nodetable);
		}

		/*
		 * Make local instances of the edges contact elements to handle the
		 * contacts. Note that some of the contact_inputs are assumed set in the
		 * previous block
		 */
		if (Contact == EDGE) {
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

			if (!node[2].hasContact_LineElementConnectedTo(node[0])) {
				internal_contact_line_element_3 = new Contact_Line();
				internal_contact_line_element_3.setNumber(-3);

				contact_input[2] = new Token(
						new String("[" + node[2].getNumber() + ","
								+ node[0].getNumber() + "]"));
				contact_input[3] = new Token(new String("D"));
				contact_input[4] = new Token(new String("="));
				contact_input[5] = new Token(thickness);

				// Set the input
				internal_contact_line_element_3.parse_Fembic(contact_input,
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
					"No nodes defined for Shell_C0_3 element nr" + number);
		}

		if (!Material_is_set) {
			throw new IllegalArgumentException(
					"No Material defined for Shell_C0_3 element nr" + number);
		}

		if (!T_is_set) {
			throw new IllegalArgumentException(
					"No Thickness (T) defined for Shell_C0_3 element nr"
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
			internal_contact_element.checkIndata();
		}

		if (Contact == EDGE) {
			if (internal_contact_line_element_1 != null) {
				internal_contact_line_element_1.checkIndata();
			}

			if (internal_contact_line_element_2 != null) {
				internal_contact_line_element_2.checkIndata();
			}

			if (internal_contact_line_element_3 != null) {
				internal_contact_line_element_3.checkIndata();
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

		case MESH:

			/* Print the element number and connected nodes */
			out = new String(number + "\t nodes = [" + node[0].getNumber()
					+ "," + node[1].getNumber() + "," + node[2].getNumber()
					+ "]\t" + "T = " + thickness + "\t" + "material = "
					+ material[0].getName() + "\t");

			if (Factor_is_set)
				out += "factor = " + factor;

			if (Friction_is_set)
				out += "friction = " + friction;

			if (Contact == DISABLED)
				out += " contact = OFF";

			if (Contact == EDGE)
				out += " contact = EDGE";

			if (NIP_is_set)
				out += " nip = " + number_of_integration_points;

			if (PIP_is_set)
				out += " pip = " + printed_integration_point;

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
	 * Insert the method's description here. Creation date: (25/12/01 %PC%T)
	 */
	public void setInitialConditions() {
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

		//
		if ((Contact == BASIC) || (Contact == EDGE)) {
			internal_contact_element.setInitialConditions();
		}

		if (Contact == EDGE) {
			if (internal_contact_line_element_1 != null) {
				internal_contact_line_element_1.setInitialConditions();
			}

			if (internal_contact_line_element_2 != null) {
				internal_contact_line_element_2.setInitialConditions();
			}

			if (internal_contact_line_element_3 != null) {
				internal_contact_line_element_3.setInitialConditions();
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
				node[2].getZ_pos(), local_coordinate_system);

		//
		if ((Contact == BASIC) || (Contact == EDGE)) {
			internal_contact_element.updateLocalCoordinateSystem();
		}

		if (Contact == EDGE) {
			if (internal_contact_line_element_1 != null) {
				internal_contact_line_element_1.updateLocalCoordinateSystem();
			}

			if (internal_contact_line_element_2 != null) {
				internal_contact_line_element_2.updateLocalCoordinateSystem();
			}

			if (internal_contact_line_element_3 != null) {
				internal_contact_line_element_3.updateLocalCoordinateSystem();
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

		// Set centre of local coordinate system on node 1.
		p.setMatrix(3, 5, 0, 0,
				p.getMatrix(3, 5, 0, 0).minus(p.getMatrix(0, 2, 0, 0)));
		p.setMatrix(6, 8, 0, 0,
				p.getMatrix(6, 8, 0, 0).minus(p.getMatrix(0, 2, 0, 0)));
		p.setMatrix(0, 2, 0, 0,
				p.getMatrix(0, 2, 0, 0).minus(p.getMatrix(0, 2, 0, 0)));

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

		// Calculate element area (area = (1/2)*X2*Y3)
		area = 0.5 * p.get(3, 0) * p.get(7, 0);
	}

	/**
	 * Is used to deactivate the element when fractured.
	 */
	public void deActivate() {
		super.deActivate();
		if (internal_contact_element != null)
			internal_contact_element.deActivate();
		if (internal_contact_line_element_1 != null)
			internal_contact_line_element_1.deActivate();
		if (internal_contact_line_element_2 != null)
			internal_contact_line_element_2.deActivate();
		if (internal_contact_line_element_3 != null)
			internal_contact_line_element_3.deActivate();
	}

}

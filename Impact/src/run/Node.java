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

import Jama.*;
import run.elements.*;
import jp.lang.*;

/**
 * A class that represents ...
 * 
 * @author Jonas Forssell, Yuriy Mikhaylovskiy
 * 
 * @see OtherClasses
 */
public class Node implements java.io.Serializable {
	private Object owner;
	private double mass;
	private double internal_energy;
	private double external_energy;
	private double contact_energy;
	private double hourglass_energy;
	private double halfstep, oldstep;
	private Matrix force;
	private Matrix external_force;
	private Matrix internal_force;
	private Matrix hourglass_force;
	private Matrix contact_force;
	private Matrix force_positive;
	private Matrix lastload;
	private Matrix inertia;
	private Matrix inv_inertia;
	private Matrix acc;
	private Matrix vel;
	private Matrix external_force_old;
	private Matrix internal_force_old;
	private Matrix hourglass_force_old;
	private Matrix contact_force_old;
	private Matrix dpl;
	private Matrix pos;
	private Matrix pos_orig;
	private Constraint constraint;
	private Load load;
	private int number;
	private Node left_neighbour;
	private Node right_neighbour;
	private Jama.Matrix dpl_old;
	private boolean x_is_set;
	private boolean y_is_set;
	private boolean z_is_set;
	private Contact_Line[] linelist;
	private int cpu_number;
	public static int NODE = 1;
	public static int INTERNAL_NODE = 2;
	protected int type;
	protected boolean deactivated;

	/*
	 * This will set up the matrix for the node to be used in all calculations
	 * 
	 * The matrix is organized as follows:
	 * 
	 * Force matrix: [ Fx Fy Fz Mx My Mz ]
	 * 
	 * Position matrix: [ x y z x_rot y_rot z_rot ]
	 * 
	 * Velocity matrix: [ x_vel y_vel z_vel x_rot_vel y_rot_vel z_rot_vel ]
	 * 
	 * Acceleration matrix [ x_acc y_acc z_acc x_rot_acc y_rot_acc z_rot_acc ]
	 * 
	 * Displacement matrix: [ x_dpl y_dpl z_dpl x_rot_dpl y_rot_dpl z_rot_dpl ]
	 * 
	 * Mass matrix: mass
	 * 
	 * Inertia matrix: [ Ixx Ixy Ixz ] [ Iyx Iyy Iyz ] [ Izx Izy Izz ]
	 * 
	 * Load is a reference to any external boundary condition applied to the
	 * node. If no boundary condition exists, the load matrix looks like the
	 * force matrix, but is filled with zeroes.
	 */
	public Node() {
		vel = new Jama.Matrix(6, 1);
		acc = new Jama.Matrix(6, 1);
		dpl = new Jama.Matrix(6, 1);
		dpl_old = new Jama.Matrix(6, 1);
		pos = new Jama.Matrix(6, 1);
		pos_orig = new Jama.Matrix(6, 1);
		force = new Jama.Matrix(6, 1);
		internal_force = new Jama.Matrix(6, 1);
		external_force = new Jama.Matrix(6, 1);
		hourglass_force = new Jama.Matrix(6, 1);
		contact_force = new Jama.Matrix(6, 1);
		internal_force_old = new Jama.Matrix(6, 1);
		external_force_old = new Jama.Matrix(6, 1);
		hourglass_force_old = new Jama.Matrix(6, 1);
		contact_force_old = new Jama.Matrix(6, 1);
		force_positive = new Jama.Matrix(6, 1);
		lastload = new Jama.Matrix(6, 1);
		inertia = new Jama.Matrix(3, 3);
		type = NODE;
	}

	/**
	 * Adds an external force contribution to the node
	 * 
	 * @param param
	 *            Jama.Matrix - The force contribution as [fx fy fz]
	 */
	public synchronized void addExternalForce(Jama.Matrix param) {
		external_force.set(0, 0, external_force.get(0, 0) + param.get(0, 0));
		external_force.set(1, 0, external_force.get(1, 0) + param.get(1, 0));
		external_force.set(2, 0, external_force.get(2, 0) + param.get(2, 0));
		force.set(0, 0, force.get(0, 0) + param.get(0, 0));
		force.set(1, 0, force.get(1, 0) + param.get(1, 0));
		force.set(2, 0, force.get(2, 0) + param.get(2, 0));

		// Update the component force
		if (param.get(0, 0) > 0) {
			force_positive
					.set(0, 0, force_positive.get(0, 0) + param.get(0, 0));
		}

		if (param.get(1, 0) > 0) {
			force_positive
					.set(1, 0, force_positive.get(1, 0) + param.get(1, 0));
		}

		if (param.get(2, 0) > 0) {
			force_positive
					.set(2, 0, force_positive.get(2, 0) + param.get(2, 0));
		}
	}

	/**
	 * Adds an internal force contribution to the node
	 * 
	 * @param param
	 *            Jama.Matrix - The force contribution as [fx fy fz]
	 */
	public synchronized void addInternalForce(Jama.Matrix param) {
		internal_force.set(0, 0, internal_force.get(0, 0) + param.get(0, 0));
		internal_force.set(1, 0, internal_force.get(1, 0) + param.get(1, 0));
		internal_force.set(2, 0, internal_force.get(2, 0) + param.get(2, 0));
		force.set(0, 0, force.get(0, 0) + param.get(0, 0));
		force.set(1, 0, force.get(1, 0) + param.get(1, 0));
		force.set(2, 0, force.get(2, 0) + param.get(2, 0));

		// Update the component force
		if (param.get(0, 0) > 0) {
			force_positive
					.set(0, 0, force_positive.get(0, 0) + param.get(0, 0));
		}

		if (param.get(1, 0) > 0) {
			force_positive
					.set(1, 0, force_positive.get(1, 0) + param.get(1, 0));
		}

		if (param.get(2, 0) > 0) {
			force_positive
					.set(2, 0, force_positive.get(2, 0) + param.get(2, 0));
		}
	}

	/**
	 * Adds a contact force contribution to the node
	 * 
	 * @param param
	 *            Jama.Matrix - The force contribution as [fx fy fz]
	 */
	public synchronized void addContactForce(Jama.Matrix param) {
		contact_force.set(0, 0, contact_force.get(0, 0) + param.get(0, 0));
		contact_force.set(1, 0, contact_force.get(1, 0) + param.get(1, 0));
		contact_force.set(2, 0, contact_force.get(2, 0) + param.get(2, 0));
		force.set(0, 0, force.get(0, 0) + param.get(0, 0));
		force.set(1, 0, force.get(1, 0) + param.get(1, 0));
		force.set(2, 0, force.get(2, 0) + param.get(2, 0));

		// Update the component force
		if (param.get(0, 0) > 0) {
			force_positive
					.set(0, 0, force_positive.get(0, 0) + param.get(0, 0));
		}

		if (param.get(1, 0) > 0) {
			force_positive
					.set(1, 0, force_positive.get(1, 0) + param.get(1, 0));
		}

		if (param.get(2, 0) > 0) {
			force_positive
					.set(2, 0, force_positive.get(2, 0) + param.get(2, 0));
		}
	}

	/**
	 * Adds a hourglass force contribution to the node
	 * 
	 * @param param
	 *            Jama.Matrix - The force contribution as [fx fy fz]
	 */
	public synchronized void addHourglassForce(Jama.Matrix param) {
		hourglass_force.set(0, 0, hourglass_force.get(0, 0) + param.get(0, 0));
		hourglass_force.set(1, 0, hourglass_force.get(1, 0) + param.get(1, 0));
		hourglass_force.set(2, 0, hourglass_force.get(2, 0) + param.get(2, 0));
		force.set(0, 0, force.get(0, 0) + param.get(0, 0));
		force.set(1, 0, force.get(1, 0) + param.get(1, 0));
		force.set(2, 0, force.get(2, 0) + param.get(2, 0));

		// Update the component force
		if (param.get(0, 0) > 0) {
			force_positive
					.set(0, 0, force_positive.get(0, 0) + param.get(0, 0));
		}

		if (param.get(1, 0) > 0) {
			force_positive
					.set(1, 0, force_positive.get(1, 0) + param.get(1, 0));
		}

		if (param.get(2, 0) > 0) {
			force_positive
					.set(2, 0, force_positive.get(2, 0) + param.get(2, 0));
		}
	}

	/**
	 * Adds an inertia contribution to the node.
	 * 
	 * @param param
	 *            Jama.Matrix - The inertia contribution as <br>
	 *            [ Ixx Ixy Ixz ] <br>
	 *            [ Iyx Iyy Iyz ] <br>
	 *            [ Izx Izy Izz ]
	 */
	public void addInertia(Jama.Matrix param) {
		inertia.plusEquals(param);
	}

	/**
	 * Returns the inertia matrix of the node
	 * 
	 * @return param Jama.Matrix - The inertia matrix as <br>
	 *         [ Ixx Ixy Ixz ] <br>
	 *         [ Iyx Iyy Iyz ] <br>
	 *         [ Izx Izy Izz ]
	 */
	public Jama.Matrix getInertia() {
		return inertia;
	}

	/**
	 * Adds a mass to the node mass matrix
	 * 
	 * @param addmass
	 *            double - The mass contribution
	 */
	public void addMass(double addmass) {
		mass += addmass;
	}

	/**
	 * Adds an external moment contribution to the node
	 * 
	 * @param param
	 *            Jama.Matrix - The moment contribution in global directions as
	 *            [mx my mz]
	 */
	public synchronized void addExternalMoment(Jama.Matrix param) {
		external_force.set(3, 0, external_force.get(3, 0) + param.get(0, 0));
		external_force.set(4, 0, external_force.get(4, 0) + param.get(1, 0));
		external_force.set(5, 0, external_force.get(5, 0) + param.get(2, 0));
		force.set(3, 0, force.get(3, 0) + param.get(0, 0));
		force.set(4, 0, force.get(4, 0) + param.get(1, 0));
		force.set(5, 0, force.get(5, 0) + param.get(2, 0));

		// Update the component force
		if (param.get(0, 0) > 0) {
			force_positive
					.set(3, 0, force_positive.get(3, 0) + param.get(0, 0));
		}

		if (param.get(1, 0) > 0) {
			force_positive
					.set(4, 0, force_positive.get(4, 0) + param.get(1, 0));
		}

		if (param.get(2, 0) > 0) {
			force_positive
					.set(5, 0, force_positive.get(5, 0) + param.get(2, 0));
		}
	}

	/**
	 * Adds an internal moment contribution to the node
	 * 
	 * @param param
	 *            Jama.Matrix - The moment contribution in global directions as
	 *            [mx my mz]
	 */
	public synchronized void addInternalMoment(Jama.Matrix param) {
		internal_force.set(3, 0, internal_force.get(3, 0) + param.get(0, 0));
		internal_force.set(4, 0, internal_force.get(4, 0) + param.get(1, 0));
		internal_force.set(5, 0, internal_force.get(5, 0) + param.get(2, 0));
		force.set(3, 0, force.get(3, 0) + param.get(0, 0));
		force.set(4, 0, force.get(4, 0) + param.get(1, 0));
		force.set(5, 0, force.get(5, 0) + param.get(2, 0));

		// Update the component force
		if (param.get(0, 0) > 0) {
			force_positive
					.set(3, 0, force_positive.get(3, 0) + param.get(0, 0));
		}

		if (param.get(1, 0) > 0) {
			force_positive
					.set(4, 0, force_positive.get(4, 0) + param.get(1, 0));
		}

		if (param.get(2, 0) > 0) {
			force_positive
					.set(5, 0, force_positive.get(5, 0) + param.get(2, 0));
		}
	}

	/**
	 * Adds a hourglass moment contribution to the node
	 * 
	 * @param param
	 *            Jama.Matrix - The moment contribution in global directions as
	 *            [mx my mz]
	 */
	public synchronized void addHourglassMoment(Jama.Matrix param) {
		hourglass_force.set(3, 0, hourglass_force.get(3, 0) + param.get(0, 0));
		hourglass_force.set(4, 0, hourglass_force.get(4, 0) + param.get(1, 0));
		hourglass_force.set(5, 0, hourglass_force.get(5, 0) + param.get(2, 0));
		force.set(3, 0, force.get(3, 0) + param.get(0, 0));
		force.set(4, 0, force.get(4, 0) + param.get(1, 0));
		force.set(5, 0, force.get(5, 0) + param.get(2, 0));

		// Update the component force
		if (param.get(0, 0) > 0) {
			force_positive
					.set(3, 0, force_positive.get(3, 0) + param.get(0, 0));
		}

		if (param.get(1, 0) > 0) {
			force_positive
					.set(4, 0, force_positive.get(4, 0) + param.get(1, 0));
		}

		if (param.get(2, 0) > 0) {
			force_positive
					.set(5, 0, force_positive.get(5, 0) + param.get(2, 0));
		}
	}

	/**
	 * This method updates the node position. It uses the central difference
	 * scheme, without damping. Note that the methods applyXXXXXCondition() is
	 * also used. The method applies the boundary condition to the calculations
	 * and just changes the calculated value to the one set in the boundary
	 * condition. Simple but effective. Creation date: (2001-10-17 01.37.59)
	 */
	public void calculateNewPosition(double timestep, double currtime) {
		int i;
		double t3;
		double t4;
		double t5;

		for (i = 0; i < 6; i++) {
			// Remember old matrix
			dpl_old.set(i, 0, dpl.get(i, 0));
			external_force_old.set(i, 0, external_force.get(i, 0));
			internal_force_old.set(i, 0, internal_force.get(i, 0));
			hourglass_force_old.set(i, 0, hourglass_force.get(i, 0));
			contact_force_old.set(i, 0, contact_force.get(i, 0));
		}

		// Add external forces
		if (load != null) {
			lastload = load.getLoad(currtime);
			force.plusEquals(lastload);
		}

		// x_dotdot_n = f/m
		//
		/*
		 * ax fx m ay fy m az = fz / m arx Mx * ary My * arz Mz *
		 */
		// linear part
		// acc.setMatrix(0,2,0,0,force.getMatrix(0,2,0,0).times(1/mass));
		acc.set(0, 0, force.get(0, 0) / mass);
		acc.set(1, 0, force.get(1, 0) / mass);
		acc.set(2, 0, force.get(2, 0) / mass);

		// The rotational accelerations (Written like this since the principle
		// nodal axis of rotation are not used here, but the global ones)
		// acc.setMatrix(3,5,0,0,inv_inertia.times(force.getMatrix(3,5,0,0).minus(vel.getMatrix(3,5,0,0).vectorProduct(inertia.times(vel.getMatrix(3,5,0,0))))));
		t3 = (inertia.get(0, 0) * vel.get(3, 0))
				+ (inertia.get(0, 1) * vel.get(4, 0))
				+ (inertia.get(0, 2) * vel.get(5, 0));
		t4 = (inertia.get(1, 0) * vel.get(3, 0))
				+ (inertia.get(1, 1) * vel.get(4, 0))
				+ (inertia.get(1, 2) * vel.get(5, 0));
		t5 = (inertia.get(2, 0) * vel.get(3, 0))
				+ (inertia.get(2, 1) * vel.get(4, 0))
				+ (inertia.get(2, 2) * vel.get(5, 0));

		acc.set(3, 0, (vel.get(4, 0) * t5) - (vel.get(5, 0) * t4));
		acc.set(4, 0, (vel.get(5, 0) * t3) - (vel.get(3, 0) * t5));
		acc.set(5, 0, (vel.get(3, 0) * t4) - (vel.get(4, 0) * t3));

		t3 = force.get(3, 0) - acc.get(3, 0);
		t4 = force.get(4, 0) - acc.get(4, 0);
		t5 = force.get(5, 0) - acc.get(5, 0);

		acc.set(3, 0, (inv_inertia.get(0, 0) * t3)
				+ (inv_inertia.get(0, 1) * t4) + (inv_inertia.get(0, 2) * t5));
		acc.set(4, 0, (inv_inertia.get(1, 0) * t3)
				+ (inv_inertia.get(1, 1) * t4) + (inv_inertia.get(1, 2) * t5));
		acc.set(5, 0, (inv_inertia.get(2, 0) * t3)
				+ (inv_inertia.get(2, 1) * t4) + (inv_inertia.get(2, 2) * t5));

		// Add gravity etc.
		if (load != null) {
			acc.plusEquals(load.getAcc(currtime));
		}

		if (constraint != null) {
			constraint.applyAccelerationConditions(this, currtime);
		}

		// Calculate halfstep
		halfstep = 0.5 * (oldstep + timestep);

		// Remember timestep
		oldstep = timestep;

		// Compute velocities
		vel.plusEquals(acc.times(halfstep));

		// Compute displacements
		dpl.plusEquals(vel.times(timestep));

		// Compute new position
		updatePos();

		/*
		 * dx_n_1 = dx_n + (vel_n_-0.5)*dt + acc_n*dt*dt
		 * dpl.plusEquals(vel.times(timestep));
		 * dpl.plusEquals(acc.times(timestep * timestep));
		 * 
		 * // vel_n_+0.5 = (1/dt)*(dx_n_1 - dx_n) vel =
		 * dpl.minus(dpl_old).times(1 / timestep);
		 */

		if (constraint != null) {
			constraint.applyVelocityConditions(this, currtime);
		}

		// Calculate the energies (reverse sign for the internal & hourglass
		// energy)
		external_energy += (0.5 * timestep * ((vel.get(0, 0) * (external_force_old
				.get(0, 0) + external_force.get(0, 0)))
				+ (vel.get(1, 0) * (external_force_old.get(1, 0) + external_force
						.get(1, 0)))
				+ (vel.get(2, 0) * (external_force_old.get(2, 0) + external_force
						.get(2, 0)))
				+ (vel.get(3, 0) * (external_force_old.get(3, 0) + external_force
						.get(3, 0)))
				+ (vel.get(4, 0) * (external_force_old.get(4, 0) + external_force
						.get(4, 0))) + (vel.get(5, 0) * (external_force_old
				.get(5, 0) + external_force.get(5, 0)))));
		internal_energy -= (0.5 * timestep * ((vel.get(0, 0) * (internal_force_old
				.get(0, 0) + internal_force.get(0, 0)))
				+ (vel.get(1, 0) * (internal_force_old.get(1, 0) + internal_force
						.get(1, 0)))
				+ (vel.get(2, 0) * (internal_force_old.get(2, 0) + internal_force
						.get(2, 0)))
				+ (vel.get(3, 0) * (internal_force_old.get(3, 0) + internal_force
						.get(3, 0)))
				+ (vel.get(4, 0) * (internal_force_old.get(4, 0) + internal_force
						.get(4, 0))) + (vel.get(5, 0) * (internal_force_old
				.get(5, 0) + internal_force.get(5, 0)))));
		contact_energy += (0.5 * timestep * ((vel.get(0, 0) * (contact_force_old
				.get(0, 0) + contact_force.get(0, 0)))
				+ (vel.get(1, 0) * (contact_force_old.get(1, 0) + contact_force
						.get(1, 0)))
				+ (vel.get(2, 0) * (contact_force_old.get(2, 0) + contact_force
						.get(2, 0)))
				+ (vel.get(3, 0) * (contact_force_old.get(3, 0) + contact_force
						.get(3, 0)))
				+ (vel.get(4, 0) * (contact_force_old.get(4, 0) + contact_force
						.get(4, 0))) + (vel.get(5, 0) * (contact_force_old.get(
				5, 0) + contact_force.get(5, 0)))));
		hourglass_energy -= (0.5 * timestep * ((vel.get(0, 0) * (hourglass_force_old
				.get(0, 0) + hourglass_force.get(0, 0)))
				+ (vel.get(1, 0) * (hourglass_force_old.get(1, 0) + hourglass_force
						.get(1, 0)))
				+ (vel.get(2, 0) * (hourglass_force_old.get(2, 0) + hourglass_force
						.get(2, 0)))
				+ (vel.get(3, 0) * (hourglass_force_old.get(3, 0) + hourglass_force
						.get(3, 0)))
				+ (vel.get(4, 0) * (hourglass_force_old.get(4, 0) + hourglass_force
						.get(4, 0))) + (vel.get(5, 0) * (hourglass_force_old
				.get(5, 0) + hourglass_force.get(5, 0)))));

	}

	private void updatePos() {
		pos.set(0, 0, pos_orig.get(0, 0) + dpl.get(0, 0));
		pos.set(1, 0, pos_orig.get(1, 0) + dpl.get(1, 0));
		pos.set(2, 0, pos_orig.get(2, 0) + dpl.get(2, 0));
		pos.set(3, 0, pos_orig.get(3, 0) + dpl.get(3, 0));
		pos.set(4, 0, pos_orig.get(4, 0) + dpl.get(4, 0));
		pos.set(5, 0, pos_orig.get(5, 0) + dpl.get(5, 0));
	}

	/**
	 * This method is needed for the contact algorithm. Each node has a handle
	 * to the closest neighbouring node to the right and to the left. After the
	 * position of the node has beed updated, the node should check it's
	 * neighbour to see if if has moved past it in space. If that is the case,
	 * the handles must be shifted and the node must find a new neighbour.
	 * Creation date: (2001-10-19 03.48.52)
	 */
	public void checkNeighbours() {
		Node temp_neighbour;
		boolean finished = false;

		while (!finished) {
			// Assume we will finish this time
			finished = true;

			// Check if the left node is not to the left side anymore.
			if (left_neighbour != null) {
				if (left_neighbour.getX_pos() > this.getX_pos()) {
					/*
					 * If we are here, the handle must be changed. We will
					 * update the handle and check again
					 */
					temp_neighbour = left_neighbour.getLeft_neighbour();

					left_neighbour.setRight_neighbour(right_neighbour);

					if (right_neighbour != null) {
						right_neighbour.setLeft_neighbour(left_neighbour);
					}

					left_neighbour.setLeft_neighbour(this);
					right_neighbour = left_neighbour;

					left_neighbour = temp_neighbour;

					if (left_neighbour != null) {
						left_neighbour.setRight_neighbour(this);
					}

					// Ok, now we have swapped the references. Repeat loop to
					// check again
					finished = false;
				} else
				// Check if the right node is not to the right side anymore.
				if (right_neighbour != null) {
					if (right_neighbour.getX_pos() < this.getX_pos()) {
						/*
						 * If we are here, the handle must be changed. We will
						 * update the handle and check again
						 */
						temp_neighbour = right_neighbour.getRight_neighbour();

						right_neighbour.setLeft_neighbour(left_neighbour);

						if (left_neighbour != null) {
							left_neighbour.setRight_neighbour(right_neighbour);
						}

						right_neighbour.setRight_neighbour(this);
						left_neighbour = right_neighbour;

						right_neighbour = temp_neighbour;

						if (right_neighbour != null) {
							right_neighbour.setLeft_neighbour(this);
						}

						// Ok, now we have swapped the references. Repeat loop
						// to check again.
						finished = false;
					}
				}
			}

			// Ok, getting here means the handles are now updated so we can
			// leave the loop.
		}
	}

	/**
	 * This is a dual function method. It resets the force on the node for the
	 * next time step and also initialises the external load on the node.
	 * Internal and other loads from the elements are added later in the main
	 * loop. Creation date: (19/11/01 %T)
	 */
	public void clearNodalForces() {
		int i;

		for (i = 0; i < 6; i++) {
			internal_force.set(i, 0, 0);
			external_force.set(i, 0, 0);
			contact_force.set(i, 0, 0);
			hourglass_force.set(i, 0, 0);
			force_positive.set(i, 0, 0);
			force.set(i, 0, 0);
		}
	}

	/**
	 * The inertias from the elements connected to this node, are all added up
	 * into the inertia matrix. Depending on their coordinates in space, there
	 * are several different couplings between the moments and angular
	 * accelerations. This is more than needed. The matrix can be recalculated
	 * to find the main intertias and then single out the main inertia. This
	 * inertia will the be used for all the directions. So: // Calculate main
	 * inertias // Keep largest I // Set up the mass matrix correctly
	 */
	public void determineMassMatrix() {
		// To avoid errors when using only elements with no rotational inertia,
		// set a small inertia rather than 0.
		if ((inertia.get(0, 0) == 0) && (inertia.get(1, 1) == 0)
				&& (inertia.get(2, 2) == 0)) {
			// And implement it in the mass matrix
			inertia.set(0, 0, 1E-15);
			inertia.set(1, 1, 1E-15);
			inertia.set(2, 2, 1E-15);
		}

		// Calculate the inverted Inertia matrix
		inv_inertia = inertia.inverse();

	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-19
	 * 03.57.29)
	 * 
	 * @return krockpackage.Constraint
	 */
	public Constraint getConstraint() {
		return constraint;
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-19
	 * 04.09.39)
	 * 
	 * @return krockpackage.Node
	 */
	public Node getLeft_neighbour() {
		return left_neighbour;
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-19
	 * 03.57.45)
	 * 
	 * @return krockpackage.Load
	 */
	public Load getLoad() {
		return load;
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-19
	 * 03.57.45)
	 * 
	 * @return krockpackage.Load
	 */
	public Matrix getForce() {
		return force;
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-19
	 * 03.58.01)
	 * 
	 * @return int
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * Returns the current node position. Creation date: (2001-10-19 03.58.01)
	 * 
	 * @return int
	 */
	public Matrix getPos() {

		return pos.getMatrix(0, 2, 0, 0);
	}

	/**
	 * Returns the original node position. Creation date: (2001-10-19 03.58.01)
	 * 
	 * @return int
	 */
	public Matrix getPos_orig() {

		return pos_orig.getMatrix(0, 2, 0, 0);
	}

	/**
	 * Returns the current node rotation. Creation date: (2001-10-19 03.58.01)
	 * 
	 * @return int
	 */
	public Matrix getRot() {

		return pos.getMatrix(3, 5, 0, 0);
	}

	/**
	 * Returns the current node velocity. Creation date: (2001-10-19 03.58.01)
	 * 
	 * @return int
	 */
	public Matrix getVel() {

		return vel.getMatrix(0, 2, 0, 0);
	}

	/**
	 * Returns the current node rotation. Creation date: (2001-10-19 03.58.01)
	 * 
	 * @return int
	 */
	public Matrix getRotVel() {

		return vel.getMatrix(3, 5, 0, 0);
	}

	/**
	 * Returns the original node rotation. Creation date: (2001-10-19 03.58.01)
	 * 
	 * @return int
	 */
	public Matrix getRot_orig() {

		return pos_orig.getMatrix(3, 5, 0, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-19
	 * 04.10.00)
	 * 
	 * @return krockpackage.Node
	 */
	public Node getRight_neighbour() {
		return right_neighbour;
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getX_acc() {
		return acc.get(0, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getX_dpos() {
		return dpl.get(0, 0) - dpl_old.get(0, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getX_force() {
		return force.get(0, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-19
	 * 03.57.45)
	 * 
	 * @return krockpackage.Load
	 */
	public double getX_force_component(boolean positive) {
		// Add load component
		if (load != null) {
			if (lastload.get(0, 0) > 0) {
				force_positive.set(0, 0,
						force_positive.get(0, 0) + lastload.get(0, 0));
			}
		}

		// Select which component to return
		if (positive) {
			return force_positive.get(0, 0);
		} else {
			return force.get(0, 0) - force_positive.get(0, 0);
		}
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getX_moment() {
		return force.get(3, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-19
	 * 03.57.45)
	 * 
	 * @return krockpackage.Load
	 */
	public double getX_moment_component(boolean positive) {
		// Add load component
		if (load != null) {
			if (lastload.get(3, 0) > 0) {
				force_positive.set(3, 0,
						force_positive.get(3, 0) + lastload.get(3, 0));
			}
		}

		// Select which component to return
		if (positive) {
			return force_positive.get(3, 0);
		} else {
			return force.get(3, 0) - force_positive.get(3, 0);
		}
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getX_pos() {
		// return pos.get(0, 0);
		return pos.get(0, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getX_pos_orig() {
		return pos_orig.get(0, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getX_rot() {
		// return pos.get(3, 0);
		return pos_orig.get(0, 0) + dpl.get(0, 0);

	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getX_rot_acc() {
		return acc.get(3, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getX_rot_dpl() {
		return dpl.get(3, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getX_rot_orig() {
		return pos_orig.get(3, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getX_rot_vel() {
		return vel.get(3, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getX_vel() {
		return vel.get(0, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getY_acc() {
		return acc.get(1, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getY_dpos() {
		return dpl.get(1, 0) - dpl_old.get(1, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getY_force() {
		return force.get(1, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-19
	 * 03.57.45)
	 * 
	 * @return krockpackage.Load
	 */
	public double getY_force_component(boolean positive) {
		// Add load component
		if (load != null) {
			if (lastload.get(1, 0) > 0) {
				force_positive.set(1, 0,
						force_positive.get(1, 0) + lastload.get(1, 0));
			}
		}

		// Select which component to return
		if (positive) {
			return force_positive.get(1, 0);
		} else {
			return force.get(1, 0) - force_positive.get(1, 0);
		}
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getY_moment() {
		return force.get(4, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-19
	 * 03.57.45)
	 * 
	 * @return krockpackage.Load
	 */
	public double getY_moment_component(boolean positive) {
		// Add load component
		if (load != null) {
			if (lastload.get(4, 0) > 0) {
				force_positive.set(4, 0,
						force_positive.get(4, 0) + lastload.get(4, 0));
			}
		}

		// Select which component to return
		if (positive) {
			return force_positive.get(4, 0);
		} else {
			return force.get(4, 0) - force_positive.get(4, 0);
		}
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getY_pos() {
		return pos.get(1, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getY_pos_orig() {
		return pos_orig.get(1, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getY_rot() {
		return pos.get(4, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getY_rot_acc() {
		return acc.get(4, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getY_rot_dpl() {
		return dpl.get(4, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getY_rot_orig() {
		return pos_orig.get(4, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getY_rot_vel() {
		return vel.get(4, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getY_vel() {
		return vel.get(1, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getZ_acc() {
		return acc.get(2, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getZ_dpos() {
		return dpl.get(2, 0) - dpl_old.get(2, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getZ_force() {
		return force.get(2, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-19
	 * 03.57.45)
	 * 
	 * @return krockpackage.Load
	 */
	public double getZ_force_component(boolean positive) {
		// Add load component
		if (load != null) {
			if (lastload.get(2, 0) > 0) {
				force_positive.set(2, 0,
						force_positive.get(2, 0) + lastload.get(2, 0));
			}
		}

		// Select which component to return
		if (positive) {
			return force_positive.get(2, 0);
		} else {
			return force.get(2, 0) - force_positive.get(2, 0);
		}
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getZ_moment() {
		return force.get(5, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-19
	 * 03.57.45)
	 * 
	 * @return krockpackage.Load
	 */
	public double getZ_moment_component(boolean positive) {
		// Add load component
		if (load != null) {
			if (lastload.get(5, 0) > 0) {
				force_positive.set(5, 0,
						force_positive.get(5, 0) + lastload.get(5, 0));
			}
		}

		// Select which component to return
		if (positive) {
			return force_positive.get(5, 0);
		} else {
			return force.get(5, 0) - force_positive.get(5, 0);
		}
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getZ_pos() {
		return pos.get(2, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getZ_pos_orig() {
		return pos_orig.get(2, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getZ_rot() {
		return pos.get(5, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getZ_rot_acc() {
		return acc.get(5, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getZ_rot_dpl() {
		return dpl.get(5, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getZ_rot_orig() {
		return pos_orig.get(5, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getZ_rot_vel() {
		return vel.get(5, 0);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public double getZ_vel() {
		return vel.get(2, 0);
	}

	/**
	 * Returns the energy resulting from internal forces acting on the node
	 * 
	 * @return param double - The internal energy
	 */
	public double getInternalEnergy() {
		return internal_energy;
	}

	/**
	 * Returns the energy resulting from external forces acting on the node
	 * 
	 * @return param double - The external energy
	 */
	public double getExternalEnergy() {
		return external_energy;
	}

	/**
	 * Returns the energy resulting from contact forces acting on the node
	 * 
	 * @return param double - The contact energy
	 */
	public double getContactEnergy() {
		return contact_energy;
	}

	/**
	 * Returns the energy resulting from hourglass forces acting on the node
	 * 
	 * @return param double - The hourglass energy
	 */
	public double getHourglassEnergy() {
		return hourglass_energy;
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-19
	 * 03.57.29)
	 * 
	 * @param newConstraint
	 *            krockpackage.Constraint
	 */
	public void setConstraint(Constraint newConstraint) {
		constraint = newConstraint;
	}

	/**
	 * Insert the method's description here. Creation date: (17/09/01 %T)
	 */
	public void setInitialConditions() {
		/*
		 * The matrix is organized as follows:
		 * 
		 * Position matrix: [ x y z x_rot y_rot z_rot ] Velocity matrix: [ x_vel
		 * y_vel z_vel x_rot_vel y_rot_vel z_rot_vel ] Acceleration matrix [
		 * x_acc y_acc z_acc x_rot_acc y_rot_acc z_rot_acc ]
		 * 
		 * Mass matrix: [ mass mass mass Imax Imax Imax ]
		 */

		// Set initial node rotations to 0
		pos_orig.set(3, 0, 0);
		pos_orig.set(4, 0, 0);
		pos_orig.set(5, 0, 0);

		// Set initial displacements to 0
		dpl.set(0, 0, 0);
		dpl.set(1, 0, 0);
		dpl.set(2, 0, 0);
		dpl.set(3, 0, 0);
		dpl.set(4, 0, 0);
		dpl.set(5, 0, 0);

		// Set original position
		pos = pos_orig.plus(dpl);

		// The velocities should be set according to any related constraint
		if (constraint == null) {
			this.setX_vel(0);
			this.setY_vel(0);
			this.setZ_vel(0);
			this.setX_rot_vel(0);
			this.setY_rot_vel(0);
			this.setY_rot_vel(0);

			//
			this.setX_acc(0);
			this.setY_acc(0);
			this.setZ_acc(0);
			this.setX_rot_acc(0);
			this.setY_rot_acc(0);
			this.setY_rot_acc(0);

			//
		} else {
			constraint.applyAccelerationConditions(this, 0);

			//
			constraint.applyVelocityConditions(this, 0);

			// Register this node to the constraint that needs it
			constraint.registerNode(this);
		}

		// Old displacements are equal to start conditions
		dpl_old = dpl.copy();

		// Set initial energies
		internal_energy = 0;
		external_energy = 0;
		hourglass_energy = 0;
		contact_energy = 0;

		// To aviod division by zero for a node with no mass, set a very small
		// mass
		if (mass == 0.0) {
			mass = 1E-15;
		}

		// Set first step values
		oldstep = 0;

	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-19
	 * 04.09.39)
	 * 
	 * @param newLeft_neighbour
	 *            krockpackage.Node
	 */
	public void setLeft_neighbour(Node newLeft_neighbour) {
		left_neighbour = newLeft_neighbour;
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-19
	 * 03.57.45)
	 * 
	 * @param newLoad
	 *            krockpackage.Load
	 */
	public void setLoad(Load newLoad) {
		load = newLoad;
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-19
	 * 03.58.01)
	 * 
	 * @param newNumber
	 *            int
	 */
	public void setNumber(int newNumber) {
		number = newNumber;
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-19
	 * 04.10.00)
	 * 
	 * @param newRight_neighbour
	 *            krockpackage.Node
	 */
	public void setRight_neighbour(Node newRight_neighbour) {
		right_neighbour = newRight_neighbour;
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 * 
	 * @param param
	 *            double
	 */
	public synchronized void setX_acc(double param) {
		acc.set(0, 0, param);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 * 
	 * @param param
	 *            double
	 */
	public synchronized void setX_force(double param) {
		force.set(0, 0, param);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 * 
	 * @param param
	 *            double
	 */
	public synchronized void setX_moment(double param) {
		force.set(3, 0, param);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 * 
	 * @param param
	 *            double
	 */
	public synchronized void setX_pos_orig(double param) {
		pos_orig.set(0, 0, param);
		pos.set(0, 0, param + dpl.get(0, 0));
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 * 
	 * @param param
	 *            double
	 */
	public synchronized void setX_rot_acc(double param) {
		acc.set(3, 0, param);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 * 
	 * @param param
	 *            double
	 */
	public synchronized void setX_rot_pos_orig(double param) {
		pos_orig.set(3, 0, param);
		pos.set(3, 0, param + dpl.get(3, 0));
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 * 
	 * @param param
	 *            double
	 */
	public synchronized void setX_rot_vel(double param) {
		vel.set(3, 0, param);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 * 
	 * @param param
	 *            double
	 */
	public synchronized void setX_vel(double param) {
		vel.set(0, 0, param);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 * 
	 * @param param
	 *            double
	 */
	public synchronized void setY_acc(double param) {
		acc.set(1, 0, param);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 * 
	 * @param param
	 *            double
	 */
	public synchronized void setY_force(double param) {
		force.set(1, 0, param);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 * 
	 * @param param
	 *            double
	 */
	public synchronized void setY_moment(double param) {
		force.set(4, 0, param);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 * 
	 * @param param
	 *            double
	 */
	public synchronized void setY_pos_orig(double param) {
		pos_orig.set(1, 0, param);
		pos.set(1, 0, param + dpl.get(1, 0));

	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 * 
	 * @param param
	 *            double
	 */
	public synchronized void setY_rot_acc(double param) {
		acc.set(4, 0, param);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 * 
	 * @param param
	 *            double
	 */
	public synchronized void setY_rot_pos_orig(double param) {
		pos_orig.set(4, 0, param);
		pos.set(4, 0, param + dpl.get(4, 0));
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 * 
	 * @param param
	 *            double
	 */
	public synchronized void setY_rot_vel(double param) {
		vel.set(4, 0, param);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 * 
	 * @param param
	 *            double
	 */
	public synchronized void setY_vel(double param) {
		vel.set(1, 0, param);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 * 
	 * @param param
	 *            double
	 */
	public synchronized void setZ_acc(double param) {
		acc.set(2, 0, param);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 * 
	 * @param param
	 *            double
	 */
	public synchronized void setZ_force(double param) {
		force.set(2, 0, param);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 * 
	 * @param param
	 *            double
	 */
	public synchronized void setZ_moment(double param) {
		force.set(5, 0, param);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 * 
	 * @param param
	 *            double
	 */
	public synchronized void setZ_pos_orig(double param) {
		pos_orig.set(2, 0, param);
		pos.set(2, 0, param + dpl.get(2, 0));
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 * 
	 * @param param
	 *            double
	 */
	public synchronized void setZ_rot_acc(double param) {
		acc.set(5, 0, param);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 * 
	 * @param param
	 *            double
	 */
	public synchronized void setZ_rot_pos_orig(double param) {
		pos_orig.set(5, 0, param);
		pos.set(5, 0, param + dpl.get(5, 0));
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 * 
	 * @param param
	 *            double
	 */
	public synchronized void setZ_rot_vel(double param) {
		vel.set(5, 0, param);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 * 
	 * @param param
	 *            double
	 */
	public synchronized void setZ_vel(double param) {
		vel.set(2, 0, param);
	}

	public double getMass() {
		return mass;
	}

	public void parse_Fembic(Token[] param, int lineno,
			RplVector constraintlist, RplVector loadlist)
			throws java.text.ParseException {
		int i = 0;
		int index;
		Constraint temp_constraint;
		Load temp_load;

		while (i < param.length) {
			if (param[i].getw().toUpperCase().equals("X")
					&& param[i + 2].is_a_number()) {
				setX_pos_orig(param[i + 2].getn());
				i += 3;
				x_is_set = true;
			} else if (param[i].getw().toUpperCase().equals("Y")
					&& param[i + 2].is_a_number()) {
				setY_pos_orig(param[i + 2].getn());
				i += 3;
				y_is_set = true;
			} else if (param[i].getw().toUpperCase().equals("Z")
					&& param[i + 2].is_a_number()) {
				setZ_pos_orig(param[i + 2].getn());
				i += 3;
				z_is_set = true;
			} else if (param[i].getw().toUpperCase().equals("M")
					&& param[i + 2].is_a_number()) {
				mass = param[i + 2].getn();
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("IXX")
					&& param[i + 2].is_a_number()) {
				inertia.set(0, 0, param[i + 2].getn());
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("IYY")
					&& param[i + 2].is_a_number()) {
				inertia.set(1, 1, param[i + 2].getn());
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("IZZ")
					&& param[i + 2].is_a_number()) {
				inertia.set(2, 2, param[i + 2].getn());
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("IXY")
					&& param[i + 2].is_a_number()) {
				inertia.set(0, 1, param[i + 2].getn());
				inertia.set(1, 0, param[i + 2].getn());
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("IYZ")
					&& param[i + 2].is_a_number()) {
				inertia.set(1, 2, param[i + 2].getn());
				inertia.set(2, 1, param[i + 2].getn());
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("IXZ")
					&& param[i + 2].is_a_number()) {
				inertia.set(0, 2, param[i + 2].getn());
				inertia.set(2, 0, param[i + 2].getn());
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("CONSTRAINT")
					&& param[i + 2].is_a_word()) {
				for (index = 0; index < constraintlist.size(); index++) {
					temp_constraint = (Constraint) constraintlist
							.elementAt(index);

					if (temp_constraint.name.equals(param[i + 2].getw()
							.toUpperCase())) {
						setConstraint(temp_constraint);

						break;
					}
				}

				if (index == constraintlist.size()) {
					throw new java.text.ParseException(
							"Constraint name specified does not exist in line ",
							lineno);
				}

				i += 3;
			} else if (param[i].getw().toUpperCase().equals("LOAD")
					&& param[i + 2].is_a_word()) {
				for (index = 0; index < loadlist.size(); index++) {
					temp_load = (Load) loadlist.elementAt(index);

					if (temp_load.name
							.equals(param[i + 2].getw().toUpperCase())) {
						setLoad(temp_load);

						break;
					}
				}

				if (index == loadlist.size()) {
					throw new java.text.ParseException(
							"Load name specified does not exist in line ",
							lineno);
				}

				i += 3;
			} else {
				throw new java.text.ParseException(
						"Unidentified node parameter indata in line ", lineno);
			}
		}

		// Update position (needed for correct mass calculation of elements
		updatePos();

	}

	public void parse_Gmsh(Token[] param, int lineno, RplVector constraintlist,
			RplVector loadlist) throws java.text.ParseException {
		int i = 0;
		int index;
		Constraint temp_constraint;
		Load temp_load;

		if (param[0].is_a_number() && param[1].is_a_number()
				&& param[2].is_a_number()) {
			setX_pos_orig(param[0].getn());
			setY_pos_orig(param[1].getn());
			setZ_pos_orig(param[i + 2].getn());
			x_is_set = true;
			y_is_set = true;
			z_is_set = true;
		} else {
			throw new java.text.ParseException(
					"Unidentified node parameter indata in line ", lineno);
		}

		// Update position (needed for correct mass calculation of elements
		updatePos();

	}

	public void parse_Nastran(Token[] param, int lineno,
			RplVector constraintlist, RplVector loadlist)
			throws java.text.ParseException {
		int i = 0;
		int index;
		Constraint temp_constraint;
		Load temp_load;

		if (param[1].is_a_number()) {
			number = (int) param[1].getn();
		} else
			throw new java.text.ParseException(
					"Illegal identification number of GRID: " + param[1].getn(),
					lineno);
		/*
		 * if (param[2].is_a_number()) { number = (int)param[1].getn(); } else
		 * throw new java.text.ParseException(
		 * "Illegal identification number of GRID: " + param[1].getn(),lineno );
		 */

		if (param[3].is_a_number()) {
			setX_pos_orig(param[3].getn());
			x_is_set = true;
		} else
			throw new java.text.ParseException(
					"Illegal x-coordinate for Grid: " + param[1].getn(), lineno);

		if (param[4].is_a_number()) {
			setY_pos_orig(param[4].getn());
			y_is_set = true;

		} else
			throw new java.text.ParseException(
					"Illegal y-coordinate for Grid: " + param[1].getn(), lineno);

		if (param[5].is_a_number()) {
			setZ_pos_orig(param[5].getn());
			z_is_set = true;

		} else
			throw new java.text.ParseException(
					"Illegal z-coordinate for GRID: " + param[1].getn(), lineno);

		// Update position (needed for correct mass calculation of elements
		updatePos();

	}

	/**
	 * This method is used to check that all mandatory parameters have been set
	 */
	public void checkIndata() throws IllegalArgumentException {
		// Check that all the required parameters have been parsed
		if (!x_is_set) {
			throw new IllegalArgumentException(
					"No x-coordinate defined for Node nr" + number);
		}

		if (!y_is_set) {
			throw new IllegalArgumentException(
					"No y-coordinate defined for Node nr" + number);
		}

		if (!z_is_set) {
			throw new IllegalArgumentException(
					"No z-coordinate defined for Node nr" + number);
		}

		if (mass < 0) {
			throw new IllegalArgumentException(
					"Negative mass not allowed for node nr " + number);
		}

		if (inertia.get(0, 0) < 0) {
			throw new IllegalArgumentException(
					"Negative Ixx not allowed for node nr " + number);
		}

		if (inertia.get(1, 0) < 0) {
			throw new IllegalArgumentException(
					"Negative Iyy not allowed for node nr " + number);
		}

		if (inertia.get(2, 0) < 0) {
			throw new IllegalArgumentException(
					"Negative Izz not allowed for node nr " + number);
		}
	}

	public Contact_Line getContact_Line(int nr) {
		if (linelist == null) {
			return null;
		}

		if (nr > (linelist.length - 1)) {
			return null;
		}

		return linelist[nr];
	}

	/**
	 * This baby increases the list of contact_line handles by one item and adds
	 * the latest element to that list. A vector could be used here but to keep
	 * memory overhead low, it is done like this.
	 */
	public void addContact_Line(Contact_Line c_element) {
		Contact_Line[] tmp;
		int i = 0;

		if (linelist == null) {
			linelist = new Contact_Line[1];
			linelist[0] = c_element;
		} else {
			tmp = linelist;
			linelist = new Contact_Line[linelist.length + 1];
			i = 0;

			// Copy all the data in the previous array (tmp)
			while (i < (linelist.length - 1)) {
				linelist[i] = tmp[i];
				i++;
			}

			// And add the last one to the end of the new array
			linelist[linelist.length - 1] = c_element;
		}
	}

	/**
	 * Checks if there is a contact_line element connected to the other node
	 */
	public boolean hasContact_LineElementConnectedTo(Node endnode) {
		int i = 0;

		// Is there any element connected to this node?
		if (linelist == null) {
			return false;
		}

		// Check all elements
		while (i < linelist.length) {
			if (linelist[i].getNode(0).equals(endnode)) {
				return true;
			}

			if (linelist[i].getNode(1).equals(endnode)) {
				return true;
			}

			i++;
		}

		// No elements matched.
		return false;
	}

	/**
	 * This baby is used for intenal nodes sometimes used inside elements to
	 * provide enhanced contact sensitivity. It is present here since the
	 * InternalNode class extends the Node class, but default is that nothing
	 * happens, so it is empty here.
	 */

	public void registerMasterElement(Element el) {
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
		return DistributedRuntime.getMachineID();
	}

	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param force
	 */
	public void setForceReference(Matrix force) {
		this.force = force;
	}

	/**
	 * @param force
	 */
	public void setInternalForceReference(Matrix force) {
		this.internal_force = force;
	}

	/**
	 * @param force
	 */
	public void setExternalForceReference(Matrix force) {
		this.external_force = force;
	}

	/**
	 * @param force
	 */
	public void setHourglassForceReference(Matrix force) {
		this.hourglass_force = force;
	}

	/**
	 * @param force
	 */
	public void setContactForceReference(Matrix force) {
		this.contact_force = force;
	}

	/**
	 * @param force
	 */
	public void setForcePositiveReference(Matrix force) {
		this.force_positive = force;
	}

	/**
	 * This method is used when the node is to be removed from the simulation
	 * and dereferenced from all lists
	 */
	public void deActivate() {
		left_neighbour.setRight_neighbour(right_neighbour);
		right_neighbour.setLeft_neighbour(left_neighbour);
		deactivated = true;
	}

	public boolean isDeActivated() {
		return deactivated;
	}

}

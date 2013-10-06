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

package run.constraints;

import run.*;

import java.util.*;

/**
 * Insert the type's description here. Creation date: (28/08/01 %T)
 * 
 * @author: Jonas Forssell
 */
public class RigidBody extends Constraint {
	private boolean master_node_number_is_set;
	private boolean master_node_update;
	private int master_node_number;
	private Vector nodes;
	private Jama.Matrix force;
	private Jama.Matrix moment;
	private Jama.Matrix moment_arm;
	private Node master_node;

	/**
	 * Constraint constructor comment.
	 */
	public RigidBody() {
		super();
		nodes = new Vector();
		master_node_update = false;
		type = new String("RIGID_BODY");

	}

	/**
	 * Insert the method's description here. Creation date: (08/09/01 %T)
	 * 
	 * @param param
	 *            krockpackage.Constraint
	 */
	public RigidBody(Constraint param) {
		name = new String(param.getName());
		type = new String("RIGID_BODY");

	}

	/**
     */

	/**
	 * This method is used to check that all mandatory parameters have been set
	 */
	public void checkIndata() throws IllegalArgumentException {
		if (!master_node_number_is_set) {
			throw new IllegalArgumentException(
					"No master_node defined for Rigid_Body nr" + number);
		}
	}

	/**
	 * This method reads and parses the indata file in format Fembic.
	 */
	public void parse_Fembic(Token[] param, int lineno, RplVector nodelist)
			throws java.text.ParseException {
		int i = 0;

		while (i < param.length) {
			if (param[i].getw().toUpperCase().equals("MASTER_NODE")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				if (param[i + 2].is_a_number()) {
					master_node_number = (int) param[i + 2].getn();

					// We now have the node number, but want to have the handle
					// to the node object.
					i += 3;
					master_node_number_is_set = true;
				}
			} else if (param[i].getw().toUpperCase().equals("UPDATE_POSITION")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				if (param[i + 2].getw().toUpperCase().equals("ON")) {
					master_node_update = true;
				}

				i += 3;
			} else {
				throw new java.text.ParseException(
						"Syntax error, unrecognized Rigid_Body constraint parameter",
						lineno);
			}
		}
	}

	/**
	 * This method reads and parses the indata file in format Fembic.
	 */
	public void parse_Nastran(Token[] param, int lineno, RplVector nodelist)
			throws java.text.ParseException {
	}

	/**
	 * This method reads and parses the indata file in format Fembic.
	 */
	public void parse_Gmsh(Token[] param, int lineno, RplVector nodelist)
			throws java.text.ParseException {
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
	public String print_Fembic(int ctrl) {
		String out;

		switch (ctrl) {

		case Element.MESH:

			/* Print the element number and connected nodes */
			out = new String(name + "\tMaster_node = "
					+ master_node.getNumber());

			if (master_node_update)
				out += " Update_position = ON";

			out += "\n";

			return out;

		default:
			return new String("");
		}
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public void applyAccelerationConditions(Node nod, double currtime) {
		// This constraint only applies velocity constraints to set this to 0
		// Since the slave nodes are controlled by velocity, this is the case
		// for all times.
		nod.setX_acc(0);
		nod.setY_acc(0);
		nod.setZ_acc(0);
		nod.setX_rot_acc(0);
		nod.setY_rot_acc(0);
		nod.setZ_rot_acc(0);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public void applyVelocityConditions(Node nod, double currtime) {
		// Calculate the vector from slavenode to masternode
		// Assume that the masternode movements have already been determined.
		moment_arm = master_node.getPos().minus(nod.getPos());

		// Set the velocity
		nod.setX_vel((master_node.getX_vel() + (moment_arm.get(1, 0) * master_node
				.getZ_rot_vel()))
				- (moment_arm.get(2, 0) * master_node.getY_rot_vel()));
		nod.setY_vel((master_node.getY_vel() + (moment_arm.get(2, 0) * master_node
				.getX_rot_vel()))
				- (moment_arm.get(0, 0) * master_node.getZ_rot_vel()));
		nod.setZ_vel((master_node.getZ_vel() + (moment_arm.get(0, 0) * master_node
				.getY_rot_vel()))
				- (moment_arm.get(1, 0) * master_node.getX_rot_vel()));

		// The rotations are easier
		nod.setX_rot_vel(master_node.getX_rot_vel());
		nod.setY_rot_vel(master_node.getY_rot_vel());
		nod.setZ_rot_vel(master_node.getZ_rot_vel());
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public void registerNode(Node nod) {
		// Add this slave node to the list for use in the update method.
		nodes.add(nod);
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public void setInitialConditions() {
		Node nod;
		int i;
		Jama.Matrix inertia;
		Jama.Matrix pos;
		double mass;
		double vx;
		double vy;
		double vz;

		inertia = new Jama.Matrix(3, 3);

		// Calculate the centre of mass
		pos = master_node.getPos().times(master_node.getMass());
		mass = master_node.getMass();

		for (i = 0; i < nodes.size(); i++) {
			nod = (Node) nodes.elementAt(i);
			pos.plusEquals(nod.getPos().times(nod.getMass()));
			mass += nod.getMass();
		}

		pos.timesEquals(1 / mass);

		// Now, if wanted, update the master node position to the centre of mass
		if (master_node_update) {
			master_node.setX_pos_orig(pos.get(0, 0));
			master_node.setY_pos_orig(pos.get(1, 0));
			master_node.setZ_pos_orig(pos.get(2, 0));
		}

		// Set up the mass and inertia for the master node
		// Loop through all the slave nodes
		for (i = 0; i < nodes.size(); i++) {
			nod = (Node) nodes.elementAt(i);

			// Add mass
			master_node.addMass(nod.getMass());

			// Read node inertia
			inertia = nod.getInertia();

			// Calculate the vector components
			vx = nod.getX_pos() - pos.get(0, 0);
			vy = nod.getY_pos() - pos.get(1, 0);
			vz = nod.getZ_pos() - pos.get(2, 0);

			// Now, add the offset contribution
			inertia.set(0, 0, inertia.get(0, 0)
					+ (nod.getMass() * ((vy * vy) + (vz * vz)))); // Ixx
			inertia.set(1, 1, inertia.get(1, 1)
					+ (nod.getMass() * ((vx * vx) + (vz * vz)))); // Iyy
			inertia.set(2, 2, inertia.get(2, 2)
					+ (nod.getMass() * ((vx * vx) + (vy * vy)))); // Izz
			inertia.set(0, 1, inertia.get(0, 1) + (nod.getMass() * (vx + vy))); // Ixy
			inertia.set(1, 0, inertia.get(1, 0) + (nod.getMass() * (vx + vy))); // Iyx
			inertia.set(1, 2, inertia.get(1, 2) + (nod.getMass() * (vy + vz))); // Iyz
			inertia.set(2, 1, inertia.get(2, 1) + (nod.getMass() * (vy + vz))); // Izy
			inertia.set(2, 0, inertia.get(2, 0) + (nod.getMass() * (vx + vz))); // Ixz
			inertia.set(0, 2, inertia.get(0, 2) + (nod.getMass() * (vx + vz))); // Izx

			// Finally, set the master node inertia
			master_node.addInertia(inertia);
		}

		if (!master_node_update) {
			// Calculate the vector components for the master node
			vx = master_node.getX_pos() - pos.get(0, 0);
			vy = master_node.getY_pos() - pos.get(1, 0);
			vz = master_node.getZ_pos() - pos.get(2, 0);

			// Now, add the inertia from the master node offset as well
			inertia.set(0, 0, master_node.getMass() * ((vy * vy) + (vz * vz))); // Ixx
			inertia.set(1, 1, master_node.getMass() * ((vx * vx) + (vz * vz))); // Iyy
			inertia.set(2, 2, master_node.getMass() * ((vx * vx) + (vy * vy))); // Izz
			inertia.set(0, 1, master_node.getMass() * (vx + vy)); // Ixy
			inertia.set(1, 0, master_node.getMass() * (vx + vy)); // Iyx
			inertia.set(1, 2, master_node.getMass() * (vy + vz)); // Iyz
			inertia.set(2, 1, master_node.getMass() * (vy + vz)); // Izy
			inertia.set(2, 0, master_node.getMass() * (vx + vz)); // Ixz
			inertia.set(0, 2, master_node.getMass() * (vx + vz)); // Izx

			// Finally, set the master node inertia
			master_node.addInertia(inertia);
		}

		// This is done pretty late, so update the master node
		master_node.determineMassMatrix();
		master_node.setInitialConditions();
	}

	/**
	 * This metod collects the data from the slave nodes and calculates the
	 * resulting moment and force on the master node. Creation date: (27/09/01
	 * %T)
	 */
	public void update() {
		int i = 0;
		Node nod;

		// Loop through all the slave nodes
		for (i = 0; i < nodes.size(); i++) {
			nod = (Node) nodes.elementAt(i);

			// Calculate the force contribution
			force = nod.getForce().getMatrix(0, 2, 0, 0);

			// Add it to the masternode
			master_node.addInternalForce(force);

			// Calculate the moment contribution
			moment = nod.getForce().getMatrix(3, 5, 0, 0);

			// Calculate the vector from the master node to the slave node
			moment_arm = nod.getPos().minus(master_node.getPos());

			// Now calculate the additional moment contribution from the force
			// and arm
			moment.plusEquals(moment_arm.vectorProduct(force));

			// Add it to the masternode
			master_node.addInternalMoment(moment);
		}
	}

	public void determineMassMatrix(RplVector nodelist) {
		// This should have ideally been made already in the initialization
		// phase, but constraints are
		// defined first by necessity. This means no nodelist is available at
		// that time, so we do it here instead.
		// We now have the node number, but want to have the handle to the node
		// object.
		master_node = super.findNode(master_node_number, nodelist);

		// For this constraint to work, it is vital that the master node is
		// updated first
		// since the other slave nodes are dependent on the resulting
		// displacement.
		// Therefore, place the master node in the beginning of the global
		// nodelist.
		nodelist.remove(master_node);
		nodelist.insertElementAt(master_node, 0);
	}
}

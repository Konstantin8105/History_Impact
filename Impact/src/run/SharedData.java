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

import Jama.Matrix;

import jp.lang.DistributedRuntime;
import jp.lang.RMICompatibility;
import jp.lang.RemoteObject;
import uka.karmi.rmi.RemoteException;
import uka.karmi.rmi.server.ReplicatedObject;

/**
 * @author pc58410
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class SharedData extends ReplicatedObject {

	private Matrix[] force;
	private Matrix[] external_force;
	private Matrix[] internal_force;
	private Matrix[] hourglass_force;
	private Matrix[] contact_force;
	private Matrix[] force_positive;
	int length;

	// Constructor
	public SharedData(RemoteObject[] cluster_nodes, int length)
			throws RemoteException {
		super(RMICompatibility.getStubs(cluster_nodes));

		this.length = length;

		force = new Matrix[length];
		external_force = new Matrix[length];
		internal_force = new Matrix[length];
		hourglass_force = new Matrix[length];
		contact_force = new Matrix[length];
		force_positive = new Matrix[length];

		for (int i = 0; i < length; i++) {
			force[i] = new Jama.Matrix(6, 1);
			internal_force[i] = new Jama.Matrix(6, 1);
			external_force[i] = new Jama.Matrix(6, 1);
			hourglass_force[i] = new Jama.Matrix(6, 1);
			contact_force[i] = new Jama.Matrix(6, 1);
			force_positive[i] = new Jama.Matrix(6, 1);

		}

	}

	public Matrix getForce(int index) {
		return force[index];
	}

	public Matrix getExternalForce(int index) {
		return external_force[index];
	}

	public Matrix getInternalForce(int index) {
		return internal_force[index];
	}

	public Matrix getHourglassForce(int index) {
		return hourglass_force[index];
	}

	public Matrix getContactForce(int index) {
		return contact_force[index];
	}

	public Matrix getForcePositive(int index) {
		return force_positive[index];
	}

	/**
	 * This method optimizes the transfer of data in a cluster by preventing
	 * some data to be exchanged which is only needed when trackers are used.
	 */
	public void freeze() {

		for (int i = 0; i < length; i++) {
			DistributedRuntime.setReadOnly(this, external_force[i]);
			DistributedRuntime.setReadOnly(this, internal_force[i]);
			DistributedRuntime.setReadOnly(this, contact_force[i]);
			DistributedRuntime.setReadOnly(this, hourglass_force[i]);
			DistributedRuntime.setReadOnly(this, force_positive[i]);
		}

		DistributedRuntime.setReadOnly(this, external_force);
		DistributedRuntime.setReadOnly(this, internal_force);
		DistributedRuntime.setReadOnly(this, contact_force);
		DistributedRuntime.setReadOnly(this, hourglass_force);
		DistributedRuntime.setReadOnly(this, force_positive);

	}

}

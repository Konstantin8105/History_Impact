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

import jp.sync.Barrier;

import java.util.*;

/**
 * 
 * Worker for Solver SMP
 * 
 * @author Jonas Forssell, Yuriy Mikhaylovskiy
 */
public class Worker implements Runnable {
	private Element temporary_element;
	private Node temporary_node;
	private Vector elementlist;
	private Vector nodelist;
	private Barrier barrier;
	private double time, timestep, ttemp;
	private boolean keep_running;
	private boolean autostep;
	private Set exception_listeners;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public Worker(Barrier barrier, double time, double timestep,
			boolean autostep) {
		// TODO Auto-generated method stub
		this.barrier = barrier;
		this.time = time;
		this.timestep = timestep;
		this.autostep = autostep;
		this.elementlist = new Vector();
		this.nodelist = new Vector();
		exception_listeners = Collections.synchronizedSet(new HashSet());
	}

	public void addElement(Element el) {
		this.elementlist.addElement(el);
	}

	public void removeElement(Element el) {
		this.elementlist.removeElement(el);
	}

	public void addNode(Node node) {
		this.nodelist.addElement(node);
	}

	public void removeNode(Node node) {
		this.nodelist.removeElement(node);
	}

	public void run() {
		int i, j, number_of_integration_points;
		int number_of_nodes = nodelist.size();

		try {
			// Repeat this loop until interrup is caught
			while (true) {

				// Main loop sets the time for next loop
				// Main loop sets the timestep for next loop

				barrier.sync(); // InterruptedException will be thrown if thread
								// interrupted

				// Each loop, reset the smallest timestep if autostep
				if (autostep == true) {
					ttemp = 1E10;
				}

				// Loop elements
				for (i = 0; i < elementlist.size(); i++) {

					temporary_element = (Element) elementlist.elementAt(i);

					if (!temporary_element.isDeActivated()) {
						// Determine the element local base vectors (local
						// coordinate system)
						temporary_element.updateLocalCoordinateSystem();

						// To reduce amount of memory in each element, loop
						// these steps for each integration point
						number_of_integration_points = temporary_element
								.getNumberOfIntegrationPoints();

						for (j = 0; j < number_of_integration_points; j++) {
							// Calculate element strain (strain velocity) matrix
							temporary_element.calculateStrain(timestep, j);

							// Calculate element stress (stress velocity) matrix
							temporary_element.calculateStress(j, timestep);
						}

						/*
						 * The strains and stresses are now calculated for all
						 * integration points. Continue with the forces. By
						 * knowing all the strains in advance, the thickness
						 * reduction of shells can be accurately determined
						 */

						for (j = 0; j < number_of_integration_points; j++) {
							// Calculate internal forces
							temporary_element.calculateNodalForces(j, timestep);
						}

						// Calculate external forces (gravity etc)
						temporary_element.calculateExternalForces(time);

						// Calculate contact forces
						temporary_element.calculateContactForces();

						// Update timestep if needed
						if (autostep == true) {
							ttemp = temporary_element.checkTimestep(ttemp);
						}
					}
					// End loop
				}

				barrier.sync();

				// Remove failed elements
				// Loop constraints
				// Loop Nodes
				// Main loop does some stuff
				// Main loop reads ttemp to determine smallest timestep

				barrier.sync(); // InterruptedException will be thrown if thread
								// interrupted

				// Loop Nodes again to clear forces
				for (i = 0; i < nodelist.size(); i++) {

					temporary_node = (Node) nodelist.elementAt(i);

					if (!temporary_node.isDeActivated()) {
						// Clear the nodal forces and prepare for next timestep
						temporary_node.clearNodalForces();
					}
				}

				barrier.sync(); // InterruptedException will be thrown if thread
								// interrupted
			} // End of loop

		} catch (InterruptedException e) {
			// Just exit when interrupted
			try {
				barrier.sync();
			} catch (Exception eb) {
			}
			System.err.println("\nWorker Interrupted: Time: " + time
					+ "   Time step: " + timestep);
			sendException(e);
			return;
		} catch (Exception e) {
			// Notify listeners that an error has occurred
			try {
				barrier.sync();
			} catch (Exception eb) {
			}
			System.err.println("\nWorker Exception: Time: " + time
					+ "   Time step: " + timestep);
			sendException(e);
			return;
		}

	}

	/**
	 * @return Returns the time.
	 */
	public double getTime() {
		return time;
	}

	/**
	 * @param time
	 *            The time to set.
	 */
	public void setTime(double time) {
		this.time = time;
	}

	/**
	 * @return Returns the timestep.
	 */
	public double getTimestep() {
		return timestep;
	}

	/**
	 * @param timestep
	 *            The timestep to set.
	 */
	public void setTimestep(double timestep) {
		this.timestep = timestep;
	}

	/**
	 * @return Returns the ttemp.
	 */
	public double getTtemp() {
		return ttemp;
	}

	/**
	 * @return Returns the autostep.
	 */
	public boolean isAutostep() {
		return autostep;
	}

	/**
	 * @param autostep
	 *            The autostep to set.
	 */
	public void setAutostep(boolean autostep) {
		this.autostep = autostep;
	}

	/**
	 * This method sends an exception to registred listeners. This is needed
	 * since exception migrates normally only to the run() method and not
	 * outside.
	 * 
	 * @param e
	 *            The exception to send
	 */
	private void sendException(Exception e) {
		if (exception_listeners.size() == 0) {
			e.printStackTrace();
			return;
		}

		Iterator iter = exception_listeners.iterator();

		while (iter.hasNext()) {
			ExceptionListener l = (ExceptionListener) iter.next();
			l.exceptionOccurred(e, this);
		}
	}

	/**
	 * Add exception listener to the object
	 * 
	 * @param l
	 *            The listener to add
	 */
	void addExceptionListener(ExceptionListener l) {
		exception_listeners.add(l);
	}

}

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

/**
 * Solver Cluster
 *
 * @author Jonas Forssell, Yuriy Mikhaylovskiy
 *
 */
import java.text.ParseException;
import java.util.*;
import java.io.*;

import run.readers.FembicReader;

import uka.karmi.rmi.RemoteException;
import jp.sync.*;
import jp.lang.*;

public class ModelCluster extends RemoteObject implements Runnable {
	private int number_of_elements;
	private int number_of_trackers;
	private int number_of_nodes;
	private int number_of_materials;
	private int number_of_controls;
	private int number_of_constraints;
	private int number_of_loads;
	private RplVector constraintlist;
	private RplVector loadlist;
	private RplVector nodelist;
	private Hashtable nodetable;
	private RplVector elementlist;
	private RplVector trackerlist;
	private RplVector materiallist;
	private SharedData shareddata;
	private volatile double time, ttemp, exported_ttemp;
	private int i;
	private Controlset controlset;
	private Element temporary_element;
	private Tracker temporary_tracker;
	private Constraint temporary_constraint;
	private Node temporary_node;
	private Writer resultwriter;
	private TrackWriter trackwriter;
	private java.lang.String filename;
	private double timestep;
	private boolean failure_is_set = false;
	private boolean autostep;
	private int nr_of_CPUs, client_nr;
	private ModelCluster[] cluster_nodes;
	private int[] nodeindicies, elementindicies;
	private long benchmark;
	private Barrier barrier;
	private Set exception_listeners;

	/**
	 * ModelSmp constructor comment.
	 */
	public ModelCluster(int nr_of_CPUs, int client_nr, String path,
			Barrier barrier) {
		super();
		this.nr_of_CPUs = nr_of_CPUs;
		this.client_nr = client_nr;
		this.filename = path;
		this.barrier = barrier;
		exception_listeners = Collections.synchronizedSet(new HashSet());
	}

	/**
	 * This method assembles the mass matrix for the solution
	 * 
	 */
	public void assembleMassMatrix() throws Exception {
		int i;
		Node temp_node;
		Element temp_element;
		Constraint temp_constraint;

		// Loop elements
		// Calculate element mass distribution and add to node
		// Calculate element intertia
		// Transform to global directions
		// Add to node inertia matrix
		// End loop
		try {
			System.out.println("Assembling Elements");

			for (i = 0; i < number_of_elements; i++) {
				temp_element = (Element) elementlist.elementAt(i);
				temp_element.assembleMassMatrix();
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ArrayIndexOutOfBoundsException(
					"\n* Error during Assembly phase - Terminating *\n " + e);
		} catch (NullPointerException e) {
			throw new NullPointerException(
					"\n* Error during Assembly phase of Element *\n* This error could be the result of a missing (or wrong) parameter in the indata file *\n"
							+ e);
		}

		// Loop nodes
		// Calculate main inertias
		// Keep largest I
		// End loop
		try {
			System.out.println("Assembling Nodes");

			for (i = 0; i < nodelist.size(); i++) {
				temp_node = (Node) nodelist.elementAt(i);
				temp_node.determineMassMatrix();
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ArrayIndexOutOfBoundsException(
					"\n* Error during Assembly phase - Terminating *\n" + e);
		} catch (NullPointerException e) {
			throw new NullPointerException(
					"\n* Error during Assembly phase of Node *\n* This error could be the result of a missing (or wrong) parameter in the indata file *\n"
							+ e);
		}

		try {
			System.out.println("Assembling Constraints");

			for (i = 0; i < number_of_constraints; i++) {
				temp_constraint = (Constraint) constraintlist.elementAt(i);
				temp_constraint.determineMassMatrix(nodelist);
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ArrayIndexOutOfBoundsException(
					"\n* Error during Assembly phase - Terminating *\n" + e);
		} catch (NullPointerException e) {
			throw new NullPointerException(
					"\n* Error during Assembly phase of Constraint *\n* This error could be the result of a missing (or wrong) parameter in the indata file *\n"
							+ e);
		}
	}

	/**
	 * This method initializes the problem in the following steps: 1. Create a
	 * file object so that the indata file can be read. 2. Question the file
	 * object of all the variables etc that is required to set up all the memory
	 * variables etc correctly. 3. Set up the variables 4. Finished Creation
	 * date: (2001-08-20 22:01:54)
	 * 
	 * @param filename
	 *            java.lang.String
	 */
	public void initialize() throws Exception {
		// Create an indata file object that we can ask for the things we
		// require
		Reader indatafile = new FembicReader(filename);

		// Set number of elements, materials and nodes
		try {
			number_of_elements = indatafile.numberOfElements();
			number_of_trackers = indatafile.numberOfTrackers();
			number_of_materials = indatafile.numberOfMaterials();
			number_of_nodes = indatafile.numberOfNodes();
			number_of_constraints = indatafile.numberOfConstraints();
			number_of_loads = indatafile.numberOfLoads();
			number_of_controls = indatafile.numberOfControls();
		} catch (ParseException e) {
			// If we end up here, something is wrong in the indata file
			throw new ParseException("Error in initialization phase \n" + e,
					e.getErrorOffset());
		}

		// Set up the arrays to handle the entries
		elementlist = new RplVector(number_of_elements);
		trackerlist = new RplVector(number_of_trackers);
		materiallist = new RplVector(number_of_materials);
		nodelist = new RplVector(number_of_nodes);
		nodetable = new Hashtable(number_of_nodes);
		constraintlist = new RplVector(number_of_constraints);
		loadlist = new RplVector(number_of_loads);
		controlset = new Controlset();

		try {
			// Fill the constraintlist array
			System.out.println("Reading Constraints");
			indatafile.open();

			for (i = 0; i < number_of_constraints; i++) {
				constraintlist.addElement(indatafile
						.getNextConstraint(nodelist));
			}

			indatafile.close();
			System.out.println("Filled constraintlist");

			// Fill the loadlist array
			System.out.println("Reading Loads");
			indatafile.open();

			for (i = 0; i < number_of_loads; i++) {
				loadlist.addElement(indatafile.getNextLoad(nodelist));
			}

			indatafile.close();
			System.out.println("Filled loadlist");

			// Fill the nodelist array
			indatafile.open();
			System.out.println("Reading Nodes");

			for (i = 0; i < number_of_nodes; i++) {

				temporary_node = indatafile.getNextNode(constraintlist,
						loadlist);

				nodelist.addElement(temporary_node);
				nodetable.put(new Integer(temporary_node.getNumber()),
						temporary_node);
			}

			indatafile.close();
			System.out.println("Filled nodelist");

			// Fill the materials array
			indatafile.open();
			System.out.println("Reading Materials");

			Material m;

			for (i = 0; i < number_of_materials; i++) {
				m = indatafile.getNextMaterial();

				if (m.failure_strain_is_set || m.failure_stress_is_set) {
					failure_is_set = true;
				}

				materiallist.addElement(m);
			}

			indatafile.close();
			System.out.println("Filled materiallist");

			// Fill the Elements array
			indatafile.open();
			System.out.println("Reading Elements");

			for (i = 0; i < number_of_elements; i++) {
				elementlist.addElement(indatafile.getNextElement(materiallist,
						nodelist, loadlist, nodetable));
			}

			indatafile.close();
			System.out.println("Filled elementlist");

			// Fill the Trackers array
			indatafile.open();
			System.out.println("Reading Trackers");

			for (i = 0; i < number_of_trackers; i++) {
				trackerlist.addElement(indatafile.getNextTracker(nodelist,
						elementlist));
			}

			indatafile.close();
			System.out.println("Filled trackerlist");

			// Get the controlset
			indatafile.open();
			System.out.println("Reading Controls");

			for (i = 0; i < number_of_controls; i++) {
				indatafile.getControlSet(controlset);
			}

			indatafile.close();
			System.out.println("Filled controlset");

			// Create an outdata file that will print the results in the format
			// we wish
			resultwriter = indatafile.getWriter(nodelist, elementlist,
					controlset, cluster_nodes);

			// Create a trackwriter that will print all trackresults in right
			// format
			trackwriter = indatafile.getTrackWriter(trackerlist, controlset,
					cluster_nodes);
		} catch (ParseException e) {
			// If we end up here, something is wrong in the indata file, reading
			// the parameters
			throw new ParseException("Parameter error in indata file \n" + e
					+ " in line " + e.getErrorOffset(), e.getErrorOffset());
		}

		// Initialize the writer
		try {
			resultwriter.initialize();
		} catch (Exception e) {
			throw new Exception("Initialization error of writer");
		}

		// Initialize the trackwriter
		try {
			trackwriter.initialize();
		} catch (Exception e) {
			throw new Exception("Initialization error of trackwriter");
		}
	}

	/**
	 * Insert the method's description here. Creation date: (2001-08-20
	 * 22:03:57)
	 */
	public void post() {
	}

	/**
	 * This method prints the requested state of the solution to a file The file
	 * will be called the same name as the indatafile, but with an ending of
	 * .res Creation date: (2001-10-17 01.51.01)
	 */
	private void print() {
		System.out.println();
		System.out.println();
		System.out.println("Time: " + time);
		try {
			resultwriter.write(filename, time);
			trackwriter.write(time);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return;
		}

	}

	/**
	 * Insert the method's description here. Creation date: (2001-08-20
	 * 22:03:07)
	 */
	public void setInitialConditions() {
		int i;
		int j;
		Node temp_node;
		Element temp_element;
		Tracker temp_tracker;
		Constraint temp_constraint;
		double total_mass;

		// Benchmark the initialization process in order to distribute the
		// work optimally between the different cluster clients
		benchmark = System.currentTimeMillis();

		// Initialize all the nodes
		System.out.println("Initializing nodes");

		for (i = 0; i < nodelist.size(); i++) {
			temp_node = (Node) nodelist.elementAt(i);
			temp_node.setInitialConditions();
		}

		/*
		 * Sort the nodes in ascending x-coordinate order.
		 */
		System.out.println("Sorting nodes");

		Collections.sort(nodelist, new NodeComparator());

		/*
		 * Set up the neighbour pointers needed for contact algorithm Give each
		 * node a handle to it's neighbour
		 */
		System.out.println("Setting up node neighbour handles");

		for (j = 0; j < nodelist.size(); j++) {
			temp_node = (Node) nodelist.elementAt(j);

			if (j < (nodelist.size() - 1)) {
				temp_node.setRight_neighbour((Node) nodelist.elementAt(j + 1));
			}

			if (j > 0) {
				temp_node.setLeft_neighbour((Node) nodelist.elementAt(j - 1));
			}
		}

		// Initialize all the elements
		System.out.println("Initializing elements");

		for (i = 0; i < number_of_elements; i++) {
			temp_element = (Element) elementlist.elementAt(i);
			temp_element.setInitialConditions();
		}

		// Initialize all the trackers
		System.out.println("Initializing trackers");

		for (i = 0; i < number_of_trackers; i++) {
			temp_tracker = (Tracker) trackerlist.elementAt(i);
			temp_tracker.setInitialConditions();
		}

		// Initialize all the constraints
		System.out.println("Initializing constraints");

		for (i = 0; i < number_of_constraints; i++) {
			temp_constraint = (Constraint) constraintlist.elementAt(i);
			temp_constraint.setInitialConditions();
		}

		// Initialize the controlset
		controlset.setInitialConditions();

		// Now, if autostep is used, loop through all elements and determine the
		// smallest timestep
		System.out.println("Determining smallest time step size");

		if (controlset.getTimestep(0) == 0) {
			autostep = true;
			timestep = 1E10;
			for (i = 0; i < number_of_elements; i++) {
				timestep = ((Element) elementlist.elementAt(i))
						.checkTimestep(timestep);
			}
		} else {
			autostep = false;
			timestep = controlset.getTimestep(0);
		}

		System.out.println("Determined time step: " + timestep);
		// Sum up total model mass and print
		System.out.println("Calculating total model mass");
		total_mass = 0;

		for (i = 0; i < nodelist.size(); i++) {
			temp_node = (Node) nodelist.elementAt(i);
			total_mass += temp_node.getMass();
		}

		System.out.println("Total model mass = " + total_mass);

		benchmark = System.currentTimeMillis() - benchmark;

	}

	/**
	 * This is the main loop of the program. Here, the solution is conducted.
	 * Creation date: (2001-08-20 22:03:47)
	 */
	public void solve(Barrier barrier) throws Exception, InterruptedException {
		final boolean DEBUG = false;
		int j;
		long time_info;
		long time_info_tmp;
		long time_remained;
		int number_of_integration_points;
		int time_h;
		int time_m;
		int time_s;
		String time_str;

		time_info = new Date().getTime();
		ttemp = 1E10;

		System.out.println("Calculating " + elementindicies.length
				+ " elements");

		// This is the main loop. Each client has one.
		for (time = controlset.getStarttime(); time <= controlset.getEndtime(); time += timestep) {

			barrier.sync();

			// Loop elements
			if (DEBUG)
				System.out.println("Client: " + client_nr + " Loop elements");
			for (i = 0; i < elementindicies.length; i++) {

				temporary_element = (Element) elementlist
						.elementAt(elementindicies[i]);

				if (!temporary_element.isDeActivated()) {

					// Determine the element local base vectors (local
					// coordinate
					// system)
					temporary_element.updateLocalCoordinateSystem();

					// To reduce amount of memory in each element, loop these
					// steps
					// for each integration point
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
					 * integration points. Continue with the forces. By knowing
					 * all the strains in advance, the thickness reduction of
					 * shells can be accurately determined
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

			exported_ttemp = ttemp;

			// Remove failed elements
			if (DEBUG)
				System.out.println("Client: " + client_nr + " Failed elements");
			if (failure_is_set) {
				for (i = 0; i < elementindicies.length; i++) {
					temporary_element = (Element) elementlist
							.elementAt(elementindicies[i]);
					if (!temporary_element.isDeActivated()) {
						temporary_element.checkIfFailed();
						if (temporary_element.hasFailed())
							temporary_element.deActivate();
					}
				}
			}

			// Loop constraints
			if (DEBUG)
				System.out
						.println("Client: " + client_nr + " Loop constraints");
			for (i = 0; i < number_of_constraints; i++) {
				temporary_constraint = (Constraint) constraintlist.elementAt(i);
				temporary_constraint.update();
			}

			// Update forces across network
			if (DEBUG)
				System.out.println("Client: " + client_nr + " Update forces");
			shareddata.collectiveUpdate();

			// Loop Nodes (not distributed due to contact algorithm)
			if (DEBUG)
				System.out.println("Client: " + client_nr + " Loop nodes");
			for (i = 0; i < nodelist.size(); i++) {
				// Get a node from the array
				temporary_node = (Node) nodelist.elementAt(i);

				if (!temporary_node.isDeActivated()) {
					// Update acceleration, velocity and position of node
					// (supply
					// timestep because it is needed in the calculations)
					// Note, the update includes application of boundary
					// conditions.
					// The update also includes addition of any external forces
					// from
					// boundary conditions.
					temporary_node.calculateNewPosition(timestep, time);

					// Check for neighbours (needed for contact algorithm)
					temporary_node.checkNeighbours();
				}
				// End loop
			}

			// Loop Trackers
			if (DEBUG)
				System.out.println("Client: " + client_nr + " Loop Trackers");
			for (i = 0; i < number_of_trackers; i++) {
				// Get a tracker from the array
				temporary_tracker = (Tracker) trackerlist.elementAt(i);

				// Collect the data
				temporary_tracker.collectData();

				// Perform needed calculations
				temporary_tracker.calculate();
			}

			// Update timestep
			if (DEBUG)
				System.out.println("Client: " + client_nr + " Update timestep");
			if (controlset.getTimestep(time) == 0) {
				autostep = true;
				for (i = 0; i < nr_of_CPUs; i++) {
					if (i != client_nr)
						ttemp = Math.min(ttemp, cluster_nodes[i].getTtemp());
				}
				timestep = ttemp;
				ttemp = 1E10;
			} else {
				autostep = false;
				timestep = controlset.getTimestep(time);
			}

			// Check if it is time to print something
			if (DEBUG)
				System.out.println("Client: " + client_nr + " Print Results");
			if (controlset.timeToPrint(time)) {

				System.out.println();
				System.out.println();
				time_info_tmp = new Date().getTime();
				time_remained = (((time_info_tmp - time_info) * ((int) ((controlset
						.getEndtime() - time) / controlset.getPrintstep()))) / 1000);
				time_h = (int) (time_remained / 3600);
				time_m = (int) ((time_remained / 60) - (time_h * 60));
				time_s = (int) (time_remained - (((int) (time_remained / 60)) * 60));
				time_str = "";

				if (time_h < 10) {
					time_str += ("0" + time_h);
				} else {
					time_str += time_h;
				}

				if (time_m < 10) {
					time_str += (":0" + time_m);
				} else {
					time_str += (":" + time_m);
				}

				if (time_s < 10) {
					time_str += (":0" + time_s);
				} else {
					time_str += (":" + time_s);
				}

				System.out.println("Cluster node ["
						+ client_nr
						+ "] Time: "
						+ ((time + "").length() > 16 ? (time + "").substring(0,
								16) : (time + ""))
						+ "\tRemaining time (hh:mm:ss) " + time_str);
				time_info = time_info_tmp;

				try {
					resultwriter.writeParallel(filename, time, elementindicies,
							barrier, client_nr, nr_of_CPUs);

				} catch (IOException ioe) {
					System.out.println(ioe);
					return;
				}
			}

			// Check if it is time to print something on the trackers
			if (DEBUG)
				System.out.println("Client: " + client_nr + " Print trackers");
			if (controlset.timeToPrintTracker(time)) {
				if (client_nr == 0) {
					trackwriter.write(time);
				}
			}

			barrier.sync();

			// Clear nodal forces
			if (DEBUG)
				System.out.println("Client: " + client_nr
						+ " Clear nodal forces");
			for (i = 0; i < nodeindicies.length; i++) {

				temporary_node = (Node) nodelist.elementAt(nodeindicies[i]);

				if (!temporary_node.isDeActivated()) {

					temporary_node.clearNodalForces();

				}

			}

			// End loop
		}

		// end this solution
		this.post();
	}

	/**
	 * This method passes the references of the replicated lists to be shared
	 * with the other processes. It is only used for the first node.
	 * 
	 * @param nodes
	 * @throws RemoteException
	 */
	public void passReferences() throws RemoteException {
		if (client_nr == 0) {
			// Create the common shared data object
			shareddata = new SharedData(cluster_nodes, nodelist.size());

			// Now, distribute handle to the other processes
			for (int i = 1; i < cluster_nodes.length; i++)
				cluster_nodes[i].setReferences(shareddata);

			// Update the shared object to synchronize with the others
			try {
				shareddata.exclusiveUpdate();
			} catch (java.io.IOException ioe) {
				ioe.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param elementlist
	 *            The elementlist to set.
	 */
	public void setReferences(SharedData sd) {
		this.shareddata = sd;
	}

	/**
	 * @return Returns the ttemp.
	 */
	public double getTtemp() {
		return exported_ttemp;
	}

	/**
	 * This method calculates which objects are to be calculated in this client
	 * process. Note that this is calculated in parallel on all clients and
	 * should not give overlapping results is maximum efficiency is to be gained
	 */
	public void determineDistribution() {
		Node tempnode;
		Element tempelement;
		int index = 0;

		long[] benchdata;
		double[] q;
		int c = 0;
		double sum = 0;

		// ***********************************************************

		// Collect benchmark data
		benchdata = new long[nr_of_CPUs];
		q = new double[nr_of_CPUs];

		for (int i = 0; i < nr_of_CPUs; i++)
			benchdata[i] = cluster_nodes[i].getBenchmark();

		for (int i = 0; i < nr_of_CPUs; i++)
			sum += 1 / (double) benchdata[i];

		for (int i = 0; i < nr_of_CPUs; i++)
			q[i] = 1 / ((double) benchdata[i] * sum);

		for (int i = 1; i < nr_of_CPUs; i++) {
			q[i] += q[i - 1];
		}

		// First get leftmost node
		tempnode = (Node) nodelist.firstElement();

		while (tempnode.getLeft_neighbour() != null)
			tempnode = tempnode.getLeft_neighbour();

		for (int i = 0; i < nodelist.size(); i++) {

			if (c == client_nr)
				index++;

			if (i < (double) nodelist.size() * q[c])
				tempnode.setCpu_number(c);
			else
				tempnode.setCpu_number(c++);

			tempnode = tempnode.getRight_neighbour();
		}

		nodeindicies = new int[index];
		index = 0;
		for (int i = 0; i < nodelist.size(); i++) {
			tempnode = (Node) nodelist.elementAt(i);
			if (tempnode.getCpu_number() == client_nr)
				nodeindicies[index++] = i;
		}

		index = 0;
		for (int i = 0; i < elementlist.size(); i++) {
			tempelement = (Element) elementlist.elementAt(i);
			tempelement.determineCpu_number();
			if (tempelement.getCpu_number() == client_nr)
				index++;
		}

		elementindicies = new int[index];
		index = 0;
		for (int i = 0; i < elementlist.size(); i++) {
			tempelement = (Element) elementlist.elementAt(i);
			if (tempelement.getCpu_number() == client_nr)
				elementindicies[index++] = i;
		}

	}

	/**
	 * This method changes the reference on the node objects to use the shared
	 * data in the shareddata object.
	 */
	public void referenceNodes() {

		Node tempnode;
		for (int i = 0; i < nodelist.size(); i++) {
			tempnode = (Node) nodelist.elementAt(i);
			tempnode.setForceReference(shareddata.getForce(i));
			tempnode.setInternalForceReference(shareddata.getInternalForce(i));
			tempnode.setContactForceReference(shareddata.getContactForce(i));
			tempnode.setExternalForceReference(shareddata.getExternalForce(i));
			tempnode.setHourglassForceReference(shareddata.getHourglassForce(i));
			tempnode.setForcePositiveReference(shareddata.getForcePositive(i));
		}

	}

	/**
	 * This method makes any possible optimizations to the cluster setup such as
	 * minimization of data transferred etc.
	 */
	public void optimize() {

		// If no trackers are used, there are several forces which can be
		// eliminated from the shared data. Exclude them from the updates
		// in this local replica
		if (number_of_trackers == 0)
			shareddata.freeze();

		// Future 0.5.6
		// freeze down all forces for nodeincicies[] (located on this client)

	}

	/**
	 * @return Returns the benchmark.
	 */
	public long getBenchmark() {
		return benchmark;
	}

	/**
	 * This is the main process. Each instance of this class will be run in a
	 * separate thread. When started, this method will be run and most errors
	 * (exceptions) will be migrated here.
	 */
	public void run() {

		try {
			initialize();

			if (Thread.currentThread().isInterrupted())
				throw new InterruptedException();

			assembleMassMatrix();

			if (Thread.currentThread().isInterrupted())
				throw new InterruptedException();

			setInitialConditions();

			barrier.sync();

			passReferences();

			barrier.sync();

			determineDistribution();

			if (Thread.currentThread().isInterrupted())
				throw new InterruptedException();

			referenceNodes();

			barrier.sync();

			optimize();

			if (Thread.currentThread().isInterrupted())
				throw new InterruptedException();

			solve(barrier);
		} catch (Exception e) {
			// An error has occurred or thread is stopped
			sendException(e);
			// Stop internal processes before exiting
			stopThreads();
			return;
		}

		// Successful completion of this solution
	}

	private void stopThreads() {
		// Reserved for future use with SMP
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
	public void addExceptionListener(ExceptionListener l) {
		exception_listeners.add(l);
	}

	public void setCluster_nodes(ModelCluster[] cluster_nodes) {
		this.cluster_nodes = cluster_nodes;
	}

}

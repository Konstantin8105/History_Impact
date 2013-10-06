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

import java.util.*;
import java.io.*;
import java.nio.channels.*;

import run.readers.FembicReader;

import jp.sync.*;

/**
 * Solver SMP
 * 
 * @author Jonas Forssell, Yuriy Mikhaylovskiy
 * 
 */

public class ModelSmp implements Runnable, ExceptionListener {
	private volatile double time, ttemp;
	private double timestep;
	private boolean autostep;
	private boolean failure_is_set = false;
	private int number_of_elements;
	private int number_of_trackers;
	private int number_of_nodes;
	private int number_of_materials;
	private int number_of_controls;
	private int number_of_groups;
	private int number_of_constraints;
	private int number_of_loads;
	private int nr_of_CPUs;
	private String filename;
	private Hashtable nodetable;
	private RplVector nodelist;
	private RplVector elementlist;
	private RplVector trackerlist;
	private RplVector materiallist;
	private RplVector constraintlist;
	private RplVector loadlist;
	private Controlset controlset;
	private int i;
	private Element temporary_element;
	private Tracker temporary_tracker;
	private Constraint temporary_constraint;
	private Node temporary_node;
	private Writer resultwriter;
	private TrackWriter trackwriter;
	private Thread[] tsolve;
	private Worker[] worker;
	private Set exception_listeners;
	private double timestep_error = Double.MAX_VALUE;

	/**
	 * ModelSmp constructor comment.
	 */
	public ModelSmp(int nr_of_CPUs, String fname) {
		super();
		this.nr_of_CPUs = nr_of_CPUs;
		exception_listeners = Collections.synchronizedSet(new HashSet());
		this.filename = fname;
	}

	/**
	 * Assemble Mass Matrix
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
					"\n* Error during Assembly phase - Terminating" + e);
		} catch (NullPointerException e) {
			throw new NullPointerException(
					"\n* Error during Assembly phase of Element\n* This error could be the result of a missing (or wrong) parameter in the indata file \n* "
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
					"\n* Error during Assembly phase - Terminating" + e);
		} catch (NullPointerException e) {
			throw new NullPointerException(
					"\n* Error during Assembly phase of Node \n* This error could be the result of a missing (or wrong) parameter in the indata file \n* "
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
					"\n* Error during Assembly phase - Terminating" + e);
		} catch (NullPointerException e) {
			throw new NullPointerException(
					"\n* Error during Assembly phase of Constraint \n* This error could be the result of a missing (or wrong) parameter in the indata file \n* "
							+ e);
		}
	}

	/**
	 * This method initializes the problem in the following steps: 1. Create a
	 * file object so that the indata file can be read. 2. Question the file
	 * object of all the variables etc that is required to set up all the memory
	 * variables etc correctly. 3. Set up the variables 4. Finished
	 * 
	 * @param filename
	 *            java.lang.String
	 */
	public void initialize(String fname) throws Exception {
		double frac, frac2;

		filename = new String(fname);

		// Create an indata file object that we can ask for the things we
		// require
		Reader indatafile = new FembicReader(filename);

		// Set number of elements, materials and nodes
		number_of_elements = indatafile.numberOfElements();
		number_of_trackers = indatafile.numberOfTrackers();
		number_of_materials = indatafile.numberOfMaterials();
		number_of_nodes = indatafile.numberOfNodes();
		number_of_constraints = indatafile.numberOfConstraints();
		number_of_loads = indatafile.numberOfLoads();
		number_of_controls = indatafile.numberOfControls();
		number_of_groups = indatafile.numberOfGroups();

		// Set up the arrays to handle the entries
		elementlist = new RplVector(number_of_elements);
		trackerlist = new RplVector(number_of_trackers);
		materiallist = new RplVector(number_of_materials);
		nodelist = new RplVector(number_of_nodes);
		nodetable = new Hashtable(number_of_nodes);
		constraintlist = new RplVector(number_of_constraints);
		loadlist = new RplVector(number_of_loads);
		controlset = new Controlset();
		// grouplist = new RplVector(number_of_groups);

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
			frac2 = 0;

			for (i = 0; i < number_of_nodes; i++) {

				temporary_node = indatafile.getNextNode(constraintlist,
						loadlist);

				nodelist.addElement(temporary_node);
				nodetable.put(new Integer(temporary_node.getNumber()),
						temporary_node);

				frac = i * 10 / number_of_nodes;
				frac %= 10;

				if (frac != frac2) {
					System.out.println("" + 10 * frac + "% complete");
					frac2 = frac;
				}
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
			frac2 = 0;

			for (i = 0; i < number_of_elements; i++) {
				elementlist.addElement(indatafile.getNextElement(materiallist,
						nodelist, loadlist, nodetable));

				frac = i * 10 / number_of_elements;
				frac %= 10;

				if (frac != frac2) {
					System.out.println("" + 10 * frac + "% complete");
					frac2 = frac;
				}
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

			// Fill the grouplist array
			/*
			 * Here, the grouplist could be filled.
			 */

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
					controlset, null);

			// Create a trackwriter that will print all trackresults in right
			// format
			trackwriter = indatafile.getTrackWriter(trackerlist, controlset,
					null);
		} catch (java.text.ParseException e) {
			// If we end up here, something is wrong in the indata file, reading
			// the parameters
			throw new java.text.ParseException(
					"Parameter error in indata file \n" + e + " in line "
							+ e.getErrorOffset(), e.getErrorOffset());
		}

		// Initialize the writer
		try {
			resultwriter.initialize();
		} catch (Exception e) {
			throw new Exception("Initialization error of writer" + e, e);
		}

		// Initialize the trackwriter
		try {
			trackwriter.initialize();
		} catch (Exception e) {
			throw new Exception("Initialization error of trackwriter" + e, e);
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
	 * *.res
	 */
	private void print() {
		System.out.println();
		System.out.println();
		System.out.println("time:" + time);
		try {
			resultwriter.write(filename, time);
			trackwriter.write(time);
		} catch (IOException ioe) {
			System.out.println(ioe);
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
		Node neighbour_node;
		Element temp_element;
		Tracker temp_tracker;
		Constraint temp_constraint;
		double total_mass;

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

		// Assigning nodes to a CPU
		System.out.println("Determining optimal model distribution for nodes");

		// First get leftmost node
		temp_node = (Node) nodelist.firstElement();

		while (temp_node.getLeft_neighbour() != null)
			temp_node = temp_node.getLeft_neighbour();

		// Now, assign in increasing x-coordinate to CPU
		for (i = 0; i < nodelist.size(); i++) {
			temp_node.setCpu_number(i * nr_of_CPUs / nodelist.size());
			temp_node = temp_node.getRight_neighbour();
		}

		// Initialize all the elements
		System.out.println("Initializing elements");

		for (i = 0; i < number_of_elements; i++) {
			temp_element = (Element) elementlist.elementAt(i);
			temp_element.setInitialConditions();
		}

		// Assign each element to a CPU
		System.out.println("Assigning each element to a CPU");

		for (i = 0; i < number_of_elements; i++) {
			temp_element = (Element) elementlist.elementAt(i);
			temp_element.determineCpu_number();
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
		System.out.println("Determining smallest timestep size");

		if (controlset.getTimestep(0) == 0) {
			timestep = 1E10;
			autostep = true;

			for (i = 0; i < number_of_elements; i++) {
				timestep = ((Element) elementlist.elementAt(i))
						.checkTimestep(timestep);
			}
		} else {
			timestep = controlset.getTimestep(0);
			autostep = false;
		}

		System.out.println("Determined timestep: " + timestep);
		// Sum up total model mass and print
		System.out.println("Calculating total model mass");
		total_mass = 0;

		for (i = 0; i < nodelist.size(); i++) {
			temp_node = (Node) nodelist.elementAt(i);
			total_mass += temp_node.getMass();
		}

		System.out.println("Total model mass = " + total_mass);

	}

	/**
	 * This is the main loop of the program. Here, the solution is conducted.
	 */
	public void solve() throws InterruptedException {
		long time_info;
		long time_info_tmp;
		long time_remained;
		int time_h;
		int time_m;
		int time_s;
		String time_str;
		final Barrier barrier;
		boolean autosave = false;

		time_info = new Date().getTime();
		ttemp = 1E10;

		// Set up an array of processes to distribute on each CPU
		tsolve = new Thread[nr_of_CPUs];
		worker = new Worker[nr_of_CPUs];
		int worker_element[] = new int[nr_of_CPUs];
		int worker_node[] = new int[nr_of_CPUs];
		barrier = BarrierFactory.createBarrier(nr_of_CPUs + 1);

		// These threads represent a subset of the work done in the main thread.
		// Over time, most of the processes will be parallel

		time = controlset.getStarttime();

		readAutoSave();
		readAutoSaveError();

		// Create workers and threads
		for (i = 0; i < nr_of_CPUs; i++) {
			worker[i] = new Worker(barrier, time, timestep, autostep);
			worker[i].addExceptionListener(this);
			tsolve[i] = new Thread(worker[i]);
			worker_element[i] = 0;
			worker_node[i] = 0;
		}

		System.out
				.println("Distributing the model on " + nr_of_CPUs + " CPU:s");
		for (int i = 0; i < elementlist.size(); i++) {
			temporary_element = (Element) elementlist.elementAt(i);
			worker[temporary_element.getCpu_number()]
					.addElement(temporary_element);
			worker_element[temporary_element.getCpu_number()]++;
		}

		for (int i = 0; i < nodelist.size(); i++) {
			temporary_node = (Node) nodelist.elementAt(i);
			worker[temporary_node.getCpu_number()].addNode(temporary_node);
			worker_node[temporary_node.getCpu_number()]++;
		}
		for (i = 0; i < nr_of_CPUs; i++) {
			System.out.println("CPU " + i + " Nodes: " + worker_node[i]
					+ " \tElements: " + worker_element[i]);
		}

		// Start all threads
		for (i = 0; i < nr_of_CPUs; i++) {
			tsolve[i].setPriority(Thread.MIN_PRIORITY);
			tsolve[i].start();
		}

		// This is the main loop which will run in tandem with the other
		// threads;
		for (time = time; time <= controlset.getEndtime(); time += timestep) {

			// Set next loop time and timestep for the workers
			for (i = 0; i < nr_of_CPUs; i++) {
				worker[i].setTime(time);
				worker[i].setTimestep(timestep);
				worker[i].setAutostep(autostep);
			}

			barrier.sync();

			// *********** Worker ***************
			// Loop elements
			// **********************************

			barrier.sync();

			// Remove failed elements
			if (failure_is_set) {
				for (i = 0; i < number_of_elements; i++) {
					temporary_element = (Element) elementlist.elementAt(i);
					if (!temporary_element.isDeActivated()) {
						temporary_element.checkIfFailed();
						if (temporary_element.hasFailed())
							temporary_element.deActivate();
					}
				}
			}

			// Loop constraints
			for (i = 0; i < number_of_constraints; i++) {
				temporary_constraint = (Constraint) constraintlist.elementAt(i);
				temporary_constraint.update();
			}

			// Loop Nodes
			for (i = 0; i < nodelist.size(); i++) {
				// Get a node from the array
				temporary_node = (Node) nodelist.elementAt(i);

				if (!temporary_node.isDeActivated()) {
					// Update acceleration, velocity and position of node
					// (supply timestep because it is needed in the
					// calculations)
					// Note, the update includes application of boundary
					// conditions.
					// The update also includes addition of any external forces
					// from boundary conditions.
					temporary_node.calculateNewPosition(timestep, time);

					// Check for neighbours (needed for contact algorithm)
					temporary_node.checkNeighbours();
				}
				// End loop
			}

			// Loop Trackers
			for (i = 0; i < number_of_trackers; i++) {
				// Get a tracker from the array
				temporary_tracker = (Tracker) trackerlist.elementAt(i);

				// Collect the data
				temporary_tracker.collectData();

				// Perform needed calculations
				temporary_tracker.calculate();
			}

			// Update timestep
			if (controlset.getTimestep(time) == 0) {
				for (i = 0; i < nr_of_CPUs; i++) {
					ttemp = Math.min(ttemp, worker[i].getTtemp());
				}
				timestep = ttemp;
				ttemp = 1E10;
				autostep = true;
			} else {
				timestep = controlset.getTimestep(time);
				if (timestep > timestep_error)
					timestep = timestep_error;
				autostep = false;
			}

			// Check if it is time to print something
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

				System.out.println("Time: " + time
						+ "\tRemaining time (hh:mm:ss) " + time_str);
				time_info = time_info_tmp;
				try {
					resultwriter.write(filename, time);
					autosave = true;
				} catch (IOException ioe) {
					System.out.println(ioe);
					return;
				}

			}

			// Check if it is time to print something on the trackers
			if (controlset.timeToPrintTracker(time)) {
				trackwriter.write(time);
			}

			barrier.sync();

			// *********** Worker ***************
			// Loop Nodes again to clear forces
			// **********************************

			barrier.sync();

			if (autosave) {
				writeAutoSave();
				autosave = false;
			}

		}

		// Solution is finished correctly. Finish off the workers
		for (i = 0; i < nr_of_CPUs; i++)
			tsolve[i].interrupt();

		// and end this solution
		this.post();
	}

	/**
	 * This is where we end up when a serious exception has occurred in the
	 * worker threads
	 * 
	 */
	public void exceptionOccurred(Exception e, Object o) {

		stopThreads();
		// Pass this error on to the GUI
		sendException(e);
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
		if (!(e instanceof InterruptedException))
			writeAutoSaveError();
	}

	public void stopThreads() {
		for (int i = 0; i < nr_of_CPUs; i++)
			if (tsolve != null && tsolve[i] != null)
				tsolve[i].interrupt();

		for (int i = 0; i < nr_of_CPUs; i++) {
			try {
				if (tsolve != null && tsolve[i] != null)
					tsolve[i].join();
			} catch (InterruptedException e) {
			}
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

	public void run() {
		long stime;

		System.out.println("Processing file: " + filename);
		System.out.println("*** Initializing ***");

		try {
			initialize(filename);

			if (Thread.currentThread().isInterrupted())
				throw new InterruptedException();

			// Assemble the mass matrix
			System.out.println("*** Assembling the Mass Matrix ***");
			assembleMassMatrix();

			if (Thread.currentThread().isInterrupted())
				throw new InterruptedException();

			// Set up the prerequisites
			System.out.println("*** Setting Initial Conditions ***");
			setInitialConditions();

			if (Thread.currentThread().isInterrupted())
				throw new InterruptedException();

			// Solve the problem
			stime = System.currentTimeMillis();
			System.out.println("*** Initiating Solver ***");

			solve();

		} catch (Exception e) {
			stopThreads();
			sendException(e); // Notify GUI
			return;

		}

		System.out.println("Solving took "
				+ (System.currentTimeMillis() - stime) + " ms");

		// Post the results and clean up
		post();

		// Solution has ended successfully
	}

	// Auto save
	public void writeAutoSave() {
		if (controlset.getRestoreSaveStatus() == Controlset.RESTORE_SAVE_OFF)
			return;
		try {
			ObjectOutputStream aout = new ObjectOutputStream(
					new FileOutputStream(filename + ".autosave"));
			writeObject(aout);
			aout.flush();
			aout.close();
			if (controlset.getRestoreSaveStatus() == Controlset.RESTORE_SAVE_PREVIOUS) {
				System.out.println("Auto Save Time: " + time + " ... ok");
				System.out.println(new Date() + "\n");
				return;
			} else {
				FileChannel fain = new FileInputStream(filename + ".autosave")
						.getChannel();
				FileChannel faout = new FileOutputStream(filename
						+ ".autosave." + time).getChannel();
				fain.transferTo(0, fain.size(), faout);
				fain.close();
				faout.close();
				FileChannel frin = new FileInputStream(filename + ".flavia.res")
						.getChannel();
				FileChannel frout = new FileOutputStream(filename
						+ ".flavia.res." + time).getChannel();
				frin.transferTo(0, frin.size(), frout);
				frin.close();
				frout.close();
				for (i = 0; i < number_of_trackers; i++) {
					temporary_tracker = (Tracker) trackerlist.elementAt(i);
					FileChannel ftin = new FileInputStream(
							temporary_tracker.filename).getChannel();
					FileChannel ftout = new FileOutputStream(
							temporary_tracker.filename + "." + time)
							.getChannel();
					ftin.transferTo(0, ftin.size(), ftout);
					ftin.close();
					ftout.close();
				}
				System.out.println("Auto Save Time: " + time + " ... ok");
				System.out.println(new Date() + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void readAutoSave() {
		if (controlset.getRestoreSaveStatus() == Controlset.RESTORE_SAVE_OFF
				|| !new File(filename + ".autosave").exists())
			return;
		try {
			ObjectInputStream ain = new ObjectInputStream(new FileInputStream(
					filename + ".autosave"));
			readObject(ain);
			ain.close();
			System.out.println("Found saved result for Time: " + time
					+ "\tTime step: " + timestep);
		} catch (Exception e) {
			if (!(e instanceof FileNotFoundException))
				e.printStackTrace();
		}
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.writeDouble(time);
		out.writeDouble(ttemp);
		out.writeDouble(timestep);
		out.writeBoolean(autostep);
		out.writeBoolean(failure_is_set);
		out.writeInt(number_of_elements);
		out.writeInt(number_of_trackers);
		out.writeInt(number_of_nodes);
		out.writeInt(number_of_materials);
		out.writeInt(number_of_controls);
		out.writeInt(number_of_groups);
		out.writeInt(number_of_constraints);
		out.writeInt(number_of_loads);
		out.writeInt(nr_of_CPUs);
		out.writeObject(filename);
		out.writeObject(nodetable);
		out.writeObject(nodelist);
		out.writeObject(elementlist);
		out.writeObject(trackerlist);
		out.writeObject(materiallist);
		out.writeObject(constraintlist);
		out.writeObject(loadlist);
		out.writeObject(controlset);
		out.writeObject(resultwriter);
		out.writeObject(trackwriter);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		time = in.readDouble();
		ttemp = in.readDouble();
		timestep = in.readDouble();
		autostep = in.readBoolean();
		failure_is_set = in.readBoolean();
		number_of_elements = in.readInt();
		number_of_trackers = in.readInt();
		number_of_nodes = in.readInt();
		number_of_materials = in.readInt();
		number_of_controls = in.readInt();
		number_of_groups = in.readInt();
		number_of_constraints = in.readInt();
		number_of_loads = in.readInt();
		nr_of_CPUs = in.readInt();
		filename = (String) in.readObject();
		nodetable = (Hashtable) in.readObject();
		nodelist = (RplVector) in.readObject();
		elementlist = (RplVector) in.readObject();
		trackerlist = (RplVector) in.readObject();
		materiallist = (RplVector) in.readObject();
		constraintlist = (RplVector) in.readObject();
		loadlist = (RplVector) in.readObject();
		controlset = (Controlset) in.readObject();
		resultwriter = (Writer) in.readObject();
		trackwriter = (TrackWriter) in.readObject();
	}

	/*
	 * At occurrence of an error calculation stops. In a file error time, an
	 * error step are saved. At restart of model time step decreases twice (
	 * timestep = timestep_error / 2 ). So there is each time at model start. It
	 * allows to define a step correctly.
	 * 
	 * При возникновении ошибки расчет останавливается. В файл сохранены время
	 * ошибки, шаг ошибки. При повторном запуске модели шаг времени уменьшается
	 * в два раза ( timestep = timestep_error / 2 ). Так происходит каждый раз
	 * при запуске модели. Это позволяет правильно определить шаг.
	 */

	public void writeAutoSaveError() {
		if (controlset.getRestoreSaveStatus() == Controlset.RESTORE_SAVE_OFF)
			return;
		try {
			String info = " At occurrence of an error calculation stops."
					+ "\n In a file error time, an error step are saved."
					+ "\n At restart of model time step decreases twice"
					+ "\n ( timestep = timestep_error / 2 )."
					+ "\n So there is each time at model start."
					+ "\n It allows to define a step correctly.\n";
			Properties prop = new Properties();
			prop.setProperty("Time", time + "");
			prop.setProperty("TimeStep", timestep + "");
			prop.store(new FileOutputStream(filename + ".error"), info);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void readAutoSaveError() {
		if (controlset.getRestoreSaveStatus() == Controlset.RESTORE_SAVE_OFF
				|| !new File(filename + ".error").exists())
			return;
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream(filename + ".error"));
			timestep_error = Double.parseDouble(prop.getProperty("TimeStep"));
			System.out.println("Found saved error for Time: " + time
					+ "\tTime step: " + timestep_error);
			timestep_error /= 2.0d;
			if (timestep > timestep_error)
				timestep = timestep_error;
			System.out.println("Changed time step: " + timestep_error);
		} catch (Exception e) {
			if (!(e instanceof FileNotFoundException))
				e.printStackTrace();
		}
	}

}

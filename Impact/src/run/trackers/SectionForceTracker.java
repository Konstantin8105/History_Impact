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

package run.trackers;

import run.*;

import java.io.*;

import java.util.*;

/**
 * This is the section force tracker The tracker calculates the force through a
 * section defined by a range of nodes The procedure is as follows: 1. Calculate
 * a plane using the node coordinates 2. Determine the normal vector to that
 * plane 3. Calculate the nodal force component in that direction 4. Summarize
 * all nodal forces 5. Print the foce as a function of time
 * 
 * @author Jonas Forssell, Yuriy Mikhaylovskiy
 * 
 * @see Tracker, TrackWriter, GidTrackWriter
 */
public class SectionForceTracker extends Tracker {
	private static int TYPE = 1;
	private Vector nodelist;
	private Jama.Matrix xv;
	private Jama.Matrix yv;
	private boolean Nodes_are_set;
	private boolean File_is_set;
	private boolean negative;
	private boolean Target_is_set;

	/**
	 * Constructor
	 */
	public SectionForceTracker() {
		xv = new Jama.Matrix(3, 1);
		yv = new Jama.Matrix(3, 1);
		nodelist = new Vector();
		negative = false;
	}

	/**
	 * Collects the data from the nodes
	 */
	public void collectData() throws IllegalArgumentException {
		// No data needs to be continuously collected so this is empty.
		// Data is only collected just before printing.
	}

	private void collectOwnData() throws IllegalArgumentException {
		int i;
		Node tempnode;
		Jama.Matrix normalvector;
		// Reset the result
		result = 0;

		// Calculate the normal vector to the section by using three nodes.
		// First, determine the x-vector
		xv.set(0, 0, ((Node) nodelist.elementAt(1)).getX_pos()
				- ((Node) nodelist.elementAt(0)).getX_pos());
		xv.set(1, 0, ((Node) nodelist.elementAt(1)).getY_pos()
				- ((Node) nodelist.elementAt(0)).getY_pos());
		xv.set(2, 0, ((Node) nodelist.elementAt(1)).getZ_pos()
				- ((Node) nodelist.elementAt(0)).getZ_pos());

		// Next, the y-vector (will not strictly be perpendicular but it does
		// not matter)
		yv.set(0, 0, ((Node) nodelist.elementAt(2)).getX_pos()
				- ((Node) nodelist.elementAt(0)).getX_pos());
		yv.set(1, 0, ((Node) nodelist.elementAt(2)).getY_pos()
				- ((Node) nodelist.elementAt(0)).getY_pos());
		yv.set(2, 0, ((Node) nodelist.elementAt(2)).getZ_pos()
				- ((Node) nodelist.elementAt(0)).getZ_pos());

		// Now, the normal vector will be the vectorproduct of x and y vectors.
		normalvector = xv.vectorProduct(yv);

		// Make it a unit vector for later.
		normalvector.timesEquals(1.0 / normalvector.length());

		// Loop through the list of nodes and collect new data from them
		// Use the xv vector to hold the result data temporarily
		for (i = 0; i < nodelist.size(); i++) {
			tempnode = (Node) nodelist.elementAt(i);

			//
			if (negative) {
				xv.set(0,
						0,
						((normalvector.get(0, 0) > 0) ? tempnode
								.getX_force_component(false) : tempnode
								.getX_force_component(true)));
				xv.set(1,
						0,
						((normalvector.get(1, 0) > 0) ? tempnode
								.getY_force_component(false) : tempnode
								.getY_force_component(true)));
				xv.set(2,
						0,
						((normalvector.get(2, 0) > 0) ? tempnode
								.getZ_force_component(false) : tempnode
								.getZ_force_component(true)));
			} else {
				xv.set(0,
						0,
						((normalvector.get(0, 0) > 0) ? tempnode
								.getX_force_component(true) : tempnode
								.getX_force_component(false)));
				xv.set(1,
						0,
						((normalvector.get(1, 0) > 0) ? tempnode
								.getY_force_component(true) : tempnode
								.getY_force_component(false)));
				xv.set(2,
						0,
						((normalvector.get(2, 0) > 0) ? tempnode
								.getZ_force_component(true) : tempnode
								.getZ_force_component(false)));
			}

			result += xv.transpose().times(normalvector).get(0, 0);
		}
	}

	/**
	 * Calculate the resulting force
	 */
	public void calculate() {
		// Dummy here since it is only calculated at printing
	}

	/**
	 * This method checks the data in the indatafile and sets the corresponding
	 * parameters for the Tracker. It is defined in the element due to the fact
	 * of isolating the Tracker from the main program, thus making adding a new
	 * Tracker a simpler issue. Creation date: (08/09/01 %T)
	 * 
	 * @param param2
	 *            java.lang.String
	 * @param param3
	 *            java.lang.String
	 * @param param1
	 *            java.lang.String
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public void parse_Fembic(Token[] param, int lineno,
			RplVector globalnodelist, RplVector globalelementlist)
			throws java.text.ParseException {
		int number_of_nodes;
		int j;
		Jama.Matrix dummy = new Jama.Matrix(3, 1);
		int i = 0;

		while (i < param.length) {
			// The nodes of the element are defined
			if (param[i].getw().toUpperCase().equals("NODES")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// Assume now that the nodes are delivered in param3, with the
				// format
				// [nodenr,nodenr,nodenr,nodenr,nodenr,nodenr,nodenr,nodenr]
				if (!param[i + 2].getw().toUpperCase().startsWith("[")
						|| !param[i + 2].getw().toUpperCase().endsWith("]")) {
					throw new java.text.ParseException(
							"Error, node number definition should be [nodenr1,nodenr2,.....,nodenrN]",
							lineno);
				}

				// Ok, now find the numbers
				try {
					number_of_nodes = super.getNumberOfNodes(param[i + 2]
							.getw().toUpperCase());

					if (number_of_nodes < 3) {
						throw new java.text.ParseException(
								"Too few nodes defined for this tracker. Minimum three required",
								lineno);
					}

					// Now, add all the nodes to the local nodelist.
					for (j = 0; j < number_of_nodes; j++) {
						nodelist.add(super.findNode(super.getNodeNumber(j + 1,
								param[i + 2].getw().toUpperCase()),
								globalnodelist));
					}
				} catch (IllegalArgumentException e) {
					System.out.println(e + "In line " + lineno);
				}

				i += 3;
				Nodes_are_set = true;
			} else
			// A target for the tracker is set
			if (param[i].getw().toUpperCase().equals("TARGET")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// Assume now that the node is delivered in param3, with the
				// simple nodenumber
				if (param[i + 2].is_a_number()) {
					throw new java.text.ParseException(
							"Illegal argument, Target should be defined as [time,timetolerance,targetvalue,targetvaluetolerance]",
							lineno);
				}

				try {
					targettime = super.getNumber(1, param[i + 2].getw()
							.toUpperCase());
					timetolerance = super.getNumber(2, param[i + 2].getw()
							.toUpperCase());
					target = super.getNumber(3, param[i + 2].getw()
							.toUpperCase());
					tolerance = super.getNumber(4, param[i + 2].getw()
							.toUpperCase());
				} catch (IllegalArgumentException e) {
					throw new java.text.ParseException(
							"Illegal argument or value missing; Target should be defined as [time,timetolerance,targetvalue,targetvaluetolerance]",
							lineno);
				}

				i += 3;
				Target_is_set = true;
			} else
			// The filename of the tracker is defined
			if (param[i].getw().toUpperCase().equals("FILENAME")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// Assume now that the material name is delivered in param3
				filename = new String(param[i + 2].getw().trim());
				i += 3;
				File_is_set = true;
			} else
			// The chosen direction to track is defined.
			if (param[i].getw().toUpperCase().equals("DIRECTION")
					&& param[i + 1].getw().toUpperCase().equals("=")
					&& param[i + 2].getw().toUpperCase().equals("NEGATIVE")) {
				negative = true;
				i += 3;
			} else {
				// Neither nodes nor filename is defined. Then the parameter is
				// wrong.
				throw new java.text.ParseException(
						"Unknown Tracker parameter ", lineno);
			}
		}
	}

	/**
	 * This method checks the data in the indatafile in Nastran format and sets
	 * the corresponding parameters for the Tracker. It is defined in the
	 * element due to the fact of isolating the Tracker from the main program,
	 * thus making adding a new Tracker a simpler issue. Creation date:
	 * (08/09/01 %T)
	 * 
	 * @param param
	 *            run.Token[] - An array containing the text to be parsed but
	 *            split into tokens.
	 * @param lineno
	 *            int - The line number of the indata file being parsed
	 * @param globalnodelist
	 *            java.util.Vector - A vector containing all the nodes in the
	 *            solution. This resides in the ModelSmp object.
	 * @param globalelementlist
	 *            java.util.Vector - A vector containing all the elements in the
	 *            solution. This resides in the ModelSmp object.
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public void parse_Nastran(Token[] param, int lineno,
			RplVector globalnodelist, RplVector globalelementlist)
			throws java.text.ParseException {
	}

	/**
	 * This method checks the data in the indatafile in Gmsh format and sets the
	 * corresponding parameters for the Tracker. It is defined in the element
	 * due to the fact of isolating the Tracker from the main program, thus
	 * making adding a new Tracker a simpler issue. Creation date: (08/09/01 %T)
	 * 
	 * @param param
	 *            run.Token[] - An array containing the text to be parsed but
	 *            split into tokens.
	 * @param lineno
	 *            int - The line number of the indata file being parsed
	 * @param globalnodelist
	 *            java.util.Vector - A vector containing all the nodes in the
	 *            solution. This resides in the ModelSmp object.
	 * @param globalelementlist
	 *            java.util.Vector - A vector containing all the elements in the
	 *            solution. This resides in the ModelSmp object.
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public void parse_Gmsh(Token[] param, int lineno, RplVector globalnodelist,
			RplVector globalelementlist) throws java.text.ParseException {
	}

	/**
	 * This method checks that all mandatory parameters have been set
	 */
	public void checkIndata() throws IllegalArgumentException {
		if (!Nodes_are_set) {
			throw new IllegalArgumentException(
					"No nodes defined for this section force tracker: "
							+ number);
		}

		if (!File_is_set) {
			throw new IllegalArgumentException(
					"No Filename defined for this section force tracker: "
							+ number);
		}
	}

	/**
	 * This method is used to create the lines needed in the result file. The
	 * method creates a string which is printed directly. However, due to the
	 * fact that the line may be different depending on what is requested to be
	 * printed and that the number of methods should be kept down, the first
	 * parameter here is a control parameter. This parameter describes what
	 * should be printed. The second parameter is the current time. Creation
	 * date: (09/12/01 %T)
	 * 
	 * @param ctrl
	 *            int
	 * @param currtime
	 *            int
	 */
	public void print_Gid(int ctrl, double currtime) throws IOException,
			IllegalArgumentException {
		BufferedWriter bw;
		// Collect data
		collectOwnData();

		// Check for target and if reached, write this into target file
		if (Target_is_set) {
			if (super.checkTarget(currtime)) {
				try {
					bw = new BufferedWriter(new FileWriter(
							filename + ".target", true));

					// OK, file was openend allright. Now, proceed to write the
					// results.
					bw.write("Target was reached at time: " + currtime
							+ " with result: " + result + "\n");
					bw.flush();
					bw.close();
				} catch (IOException ioe) {
					System.out.println("Error in writing target result file: "
							+ filename + ".target");
					throw ioe;
				}
			}
		}

		// Now, do the job
		String out;
		int i;

		switch (ctrl) {
		case RESULT_HEADER:

			/*
			 * This is the first time the tracker is asked to write something.
			 * Therefore, a header is suitable to start with and the file should
			 * be created
			 */
			try {
				bw = new BufferedWriter(new FileWriter(filename));

				// OK, file was openend allright. Now, proceed to write the
				// header.
				out = new String(
						"# Impact section force tracker results from tracker number: "
								+ number + "\n");
				out += "# The following nodes are included in the section:\n";
				out += "# \n# ";

				for (i = 0; i < nodelist.size(); i++) {
					out += (((Node) nodelist.elementAt(i)).getNumber() + ": ");
				}

				out += "\n# \n";
				out += "# X: time \t Y: force \n";
				out += "#\n";
				bw.write(out);
				bw.flush();
				bw.close();
			} catch (IOException ioe) {
				System.out
						.println("Error in creating the SectionForceTracker file: "
								+ filename);
				throw ioe;
			}

			// If all goes well, we should just return.
			return;

		case RESULT:

			/*
			 * This is not the first time the tracker is asked to write
			 * something. Therefore, the file must be opened for append.
			 */
			try {
				bw = new BufferedWriter(new FileWriter(filename, true));

				// OK, file was openend allright. Now, proceed to write the
				// results.
				bw.write(currtime + "\t" + result + "\n");
				bw.flush();
				bw.close();
			} catch (IOException ioe) {
				System.out
						.println("Error in writing results to the SectionForceTracker file: "
								+ filename);
				throw ioe;
			}

			// If everything went well, we will just return.
			return;

		default:
			throw new IllegalArgumentException(
					"Unknown parameter for print_Gid in SectionForceTracker number: "
							+ number);
		}
	}

	/**
	 * Sets up any initial conditions needed. Creation date: (27/09/01 %T)
	 */
	public void setInitialConditions() {
	}

	/**
	 * Returns the tracker type number The type variable is defined in the
	 * superclass Tracker
	 */
	public int getType() {
		return TYPE;
	}
}

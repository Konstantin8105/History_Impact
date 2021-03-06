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

import run.elements.*;

import java.io.*;

/**
 * This tracker reads the force in a rod element and plots this into a file as a
 * function of time.
 * 
 * @author Jonas Forssell, Yuriy Mikhaylovskiy
 * 
 * @see Tracker, TrackWriter, GidTrackWriter
 */
public class RodForceTracker extends Tracker {
	private static int TYPE = 9;
	private Rod_2 rod;
	private boolean Element_is_set;
	private boolean File_is_set;
	private boolean Target_is_set;

	/**
	 * Constructor
	 */
	public RodForceTracker() {
	}

	/**
	 * Collects the data from the nodes
	 */
	public void collectData() throws IllegalArgumentException {
		// No data needs to be continuously collected so this is empty.
		// Data is only collected just before printing.
	}

	/**
	 * Collects the data from the nodes only on request at printtime.
	 */
	private void collectOwnData() throws IllegalArgumentException {
		// Calculate the distance by collecting positions from the nodes.
		result = rod.getForce();
	}

	/**
	 * Calculates results based on collected data.
	 */
	public void calculate() {
		// No calculation needed. Already done in collection stage.
	}

	/**
	 * This method checks the data in the indatafile and sets the corresponding
	 * parameters for the Tracker. It is defined in the element due to the fact
	 * of isolating the Tracker from the main program, thus making adding a new
	 * Tracker a simpler issue. Creation date: (08/09/01 %T)
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
	public void parse_Fembic(Token[] param, int lineno,
			RplVector globalnodelist, RplVector globalelementlist)
			throws java.text.ParseException {
		int number_of_elements;
		int j;
		Jama.Matrix dummy = new Jama.Matrix(3, 1);
		int i = 0;

		while (i < param.length) {
			// The element is defined
			if (param[i].getw().toUpperCase().equals("ELEMENT")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// Assume now that the element is defined in param3, with the
				// format
				// [elementnr]
				if (!param[i + 2].getw().toUpperCase().startsWith("[")
						|| !param[i + 2].getw().toUpperCase().endsWith("]")) {
					throw new java.text.ParseException(
							"Error, element number definition should be [elementnr]",
							lineno);
				}

				// Ok, now find the numbers
				try {
					number_of_elements = super.getNumberOfNodes(param[i + 2]
							.getw().toUpperCase());

					if (number_of_elements != 1) {
						throw new java.text.ParseException(
								"Illegal number of elements defined. Only one allowed",
								lineno);
					}

					// Now, find the element
					rod = (Rod_2) super.findElement(super.getNodeNumber(1,
							param[i + 2].getw().toUpperCase()),
							globalelementlist);
				} catch (IllegalArgumentException e) {
					System.out.println(e + "In line " + lineno);
				}

				i += 3;
				Element_is_set = true;
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
			} else {
				// Neither element nor filename is defined. Then the parameter
				// is wrong.
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
		if (!Element_is_set) {
			throw new IllegalArgumentException(
					"No element defined for this RodForceTracker: " + number);
		}

		if (!File_is_set) {
			throw new IllegalArgumentException(
					"No Filename defined for this RodForceTracker: " + number);
		}
	}

	/**
	 * This method is used to create the lines needed in the result file. The
	 * method creates a string which is printed directly. However, due to the
	 * fact that the line may be different depending on what is requested to be
	 * printed and that the number of methods should be kept down, the first
	 * parameter here is a control parameter. This parameter describes what
	 * should be printed. The second parameter is the current time.
	 * 
	 * @param ctrl
	 *            int - A control parameter, used to control the printing of a
	 *            result or header file.
	 * @param currtime
	 *            int - The current solution time.
	 */
	public void print_Gid(int ctrl, double currtime) throws IOException,
			IllegalArgumentException {
		BufferedWriter bw;
		// Start by collecting data (needed here since it is not done
		// continuously
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

		// Now proceed
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
						"# Impact RodForceTracker results from tracker number: "
								+ number + "\n");
				out += "# The following element is read in the section:\n";
				out += "# \n# ";
				out += (rod.getNumber() + ": ");
				out += "\n# \n";
				out += "# X: time \t Y: rod force: ";
				out += "\n#\n";
				bw.write(out);
				bw.flush();
				bw.close();
			} catch (IOException ioe) {
				System.out
						.println("Error in creating the RodForceTracker file: "
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
						.println("Error in writing results to the RodForceTracker file: "
								+ filename);
				throw ioe;
			}

			// If everything went well, we will just return.
			return;

		default:
			throw new IllegalArgumentException(
					"Unknown parameter for print_Gid in RodForceTracker number: "
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

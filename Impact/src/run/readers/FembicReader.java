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

package run.readers;

import run.Constraint;
import run.Controlset;
import run.Element;
import run.Load;
import run.Material;
import run.ModifiedStreamTokenizer;
import run.Node;
import run.Reader;
import run.RplVector;
import run.Token;
import run.TrackWriter;
import run.Tracker;
import run.Writer;
import uka.karmi.rmi.RemoteException;

import java.io.*;
import java.text.ParseException;
import java.util.*;

import jp.lang.RemoteObject;

/**
 * Insert the type's description here. Creation date: (2001-08-20 22:51:18)
 * 
 * @author: Jonas Forssell
 */
public class FembicReader extends Reader {
	private java.io.BufferedReader br;
	private java.lang.String filename;
	private java.lang.String[] keywords = { "TITLE", "CONTROLS", "ELEMENTS",
			"NODES", "LOADS", "CONSTRAINTS", "MATERIALS", "TRACKERS", "GROUPS",
			"GEOMETRY" };
	private boolean in_block = false;
	private java.io.StreamTokenizer str;
	private Node temporary_node;
	private Load temporary_load;
	private boolean element_in_block = false;
	private boolean tracker_in_block = false;
	private boolean material_in_block = false;
	private java.lang.String current_element_type;
	private java.lang.String current_tracker_type;
	private java.lang.String current_constraint_type;
	private boolean control_in_block;
	private boolean constraint_in_block;

	/**
	 * This constructor immediately tries to open the file.
	 */
	public FembicReader(String fn) {
		// Remember this filename
		filename = new String(fn);
	}

	/**
	 * Insert the method's description here. Creation date: (2001-08-20
	 * 23:00:59)
	 */
	public void close() {
		try {
			br.close();
		} catch (IOException ioe) {
			System.out
					.println("An IOException has occurred when closing the indata file");

			return;
		}
	}

	/**
	 * This method reads the next constraint, sets up all the paremeters and
	 * returns a handle to the constraint. Creation date: (26/08/01 %T)
	 * 
	 * @return krockpackage.Node
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public void getControlSet(Controlset temporary_controlset)
			throws IllegalArgumentException {
		// Assume the file has been opened
		// Go to beginning of constraint block, search until we find one.
		try {
			if (!control_in_block) {
				while (str.nextToken() != java.io.StreamTokenizer.TT_EOF) {
					if (str.ttype == java.io.StreamTokenizer.TT_WORD) {
						if (str.sval.toUpperCase().equals("CONTROLS")
								&& (str.nextToken() == str.TT_EOL)) {
							control_in_block = true;

							break;
						}
					}
				}
			}

			// We should now be in a block. If not, throw an error
			if (!control_in_block) {
				throw new java.text.ParseException(
						"No controls block found or missing controls ",
						str.lineno());
			}

			// Time to read the next control set
			// Assume that we are at the beginning of a line, so the first token
			// should now be the control command
			// However, start with "cleaning" out extra lines etc.
			while (str.nextToken() == java.io.StreamTokenizer.TT_EOL) {
				;
			}

			str.pushBack();

			// Now, loop this until we reach another block
			while (str.nextToken() != str.TT_EOF) {
				if (str.ttype == str.TT_WORD) {
					if (!this.isAKeyword(str.sval)) {
						// Go ahead and read this line. Feed it to the
						// controlset.
						str.pushBack();
						temporary_controlset.parse_Fembic(tokenize(str),
								str.lineno());
					} else {
						str.pushBack();

						break;
					}
				}
			}

			// Check all required data has been filled in
			temporary_controlset.checkIndata();

			// OK, everything read now. Lets end. The data has already been
			// filled into the controlset given as an argument
			// so there is nothing to return.
		} catch (java.io.IOException e) {
			throw new IllegalArgumentException(
					"IO error in when generating control set" + e.toString());
		} catch (ParseException e) {
			throw new IllegalArgumentException(
					"Failed to generate control set, Parse exception" + e);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	/**
	 * This method reads the next element from the indata file, sets up all the
	 * parameters and creates the corresponding element object. Since the
	 * parameters for each element may vary, it makes sence to keep all that
	 * varability inside the definition of the element. Therefore, a new method
	 * called parse_Fembic is designed for each element where the checking and
	 * parsing of the variables are taken care of. A beauty of object oriented
	 * design is that the parent class is returned from this method since the
	 * functionality really is irrelevant to the program from here on. Creation
	 * date: (26/08/01 %T)
	 * 
	 * @return krockpackage.Element
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public Element getNextElement(RplVector materiallist, RplVector nodelist,
			RplVector loadlist, Hashtable nodetable)
			throws java.text.ParseException {
		Element temporary_element;
		// Assume the file has been opened
		// If we are not in an element block, search until we find one.
		try {
			// Start with "cleaning" out extra lines etc.
			while (str.nextToken() == java.io.StreamTokenizer.TT_EOL) {
				;
			}

			str.pushBack();

			// Now, assume we are reading an element line
			while (str.nextToken() != java.io.StreamTokenizer.TT_NUMBER) {
				str.pushBack();

				// This is not the case, so search until the next block of
				// elements is found
				element_in_block = false;

				while (str.nextToken() != java.io.StreamTokenizer.TT_EOF) {
					if (str.ttype == java.io.StreamTokenizer.TT_WORD) {
						if (str.sval.toUpperCase().equals("ELEMENTS")) {
							if (str.nextToken() == java.io.StreamTokenizer.TT_WORD) {
								if (str.sval.toUpperCase().equals("OF")) {
									;
								} else {
									throw new java.text.ParseException(
											"No 'OF' defined in element block header",
											str.lineno());
								}
							}

							if (str.nextToken() == java.io.StreamTokenizer.TT_WORD) {
								if (str.sval.toUpperCase().equals("TYPE")) {
									;
								} else {
									throw new java.text.ParseException(
											"No 'TYPE' defined in element block header",
											str.lineno());
								}
							}

							if (str.nextToken() == java.io.StreamTokenizer.TT_WORD) {
								current_element_type = new String(
										str.sval.toUpperCase());
							} else {
								throw new java.text.ParseException(
										"Unknown type defined in element block header",
										str.lineno());
							}

							System.out.println("Element block found!");
							element_in_block = true;

							// Clear EOL
							str.nextToken();

							break;
						}
					}
				}
			}

			// We should now be in a block. If not, throw an error
			if (!element_in_block) {
				throw new java.text.ParseException(
						"No elements block found or missing elements",
						str.lineno());
			}

			// Parse the element number and put into the node object
			temporary_element = Element
					.getElementOfType_Fembic(current_element_type);
			temporary_element.setNumber((int) str.nval);
			// Continue and read the element parameters, assuming the format
			// 'param = val'
			temporary_element.parse_Fembic(tokenize(str), str.lineno(),
					nodelist, materiallist, loadlist, nodetable);

			// Check all required data has been filled in
			temporary_element.checkIndata();

			/*
			 * Getting this far means that all parameters for the element is
			 * defined, so return the temporary element now Note the upcasting!!
			 */
			return (Element) temporary_element;

			//
		} catch (java.io.IOException e) {
			throw new IllegalArgumentException(
					"IO Exception when generating next element"
							+ e.getMessage());
		} catch (ParseException e) {
			throw new IllegalArgumentException(e.getMessage() + " in line: "
					+ (e.getErrorOffset() - 1));
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(e.toString());
		}
	}

	/**
	 * This method reads the next tracker from the indata file, sets up all the
	 * parameters and creates the corresponding tracker object. Since the
	 * parameters for each tracker may vary, it makes sence to keep all that
	 * varability inside the definition of the tracker. Therefore, a new method
	 * called parse_Fembic is designed for each tracker where the checking and
	 * parsing of the variables are taken care of. A beauty of object oriented
	 * design is that the parent class is returned from this method since the
	 * functionality really is irrelevant to the program from here on. Creation
	 * date: (26/08/01 %T)
	 * 
	 * @return krockpackage.Element
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public Tracker getNextTracker(RplVector nodelist, RplVector elementlist)
			throws java.text.ParseException {
		Tracker temporary_tracker;
		// Assume the file has been opened
		// If we are not in a tracker block, search until we find one.
		try {
			// Start with "cleaning" out extra lines etc.
			while (str.nextToken() == java.io.StreamTokenizer.TT_EOL) {
				;
			}

			str.pushBack();

			// Now, assume we are reading a tracker line
			while (str.nextToken() != java.io.StreamTokenizer.TT_NUMBER) {
				str.pushBack();

				// This is not the case, so search until the next block of
				// trackers is found
				tracker_in_block = false;

				while (str.nextToken() != java.io.StreamTokenizer.TT_EOF) {
					if (str.ttype == java.io.StreamTokenizer.TT_WORD) {
						if (str.sval.toUpperCase().equals("TRACKERS")) {
							if (str.nextToken() == java.io.StreamTokenizer.TT_WORD) {
								if (str.sval.toUpperCase().equals("OF")) {
									;
								} else {
									throw new java.text.ParseException(
											"No 'OF' defined in tracker block header",
											str.lineno());
								}
							}

							if (str.nextToken() == java.io.StreamTokenizer.TT_WORD) {
								if (str.sval.toUpperCase().equals("TYPE")) {
									;
								} else {
									throw new java.text.ParseException(
											"No 'TYPE' defined in tracker block header",
											str.lineno());
								}
							}

							if (str.nextToken() == java.io.StreamTokenizer.TT_WORD) {
								current_tracker_type = new String(
										str.sval.toUpperCase());
							} else {
								throw new java.text.ParseException(
										"Unknown type defined in tracker block header",
										str.lineno());
							}

							System.out.println("Tracker block found!");
							tracker_in_block = true;

							// Clear EOL
							str.nextToken();

							break;
						}
					}
				}
			}

			// We should now be in a block. If not, throw an error
			if (!tracker_in_block) {
				throw new java.text.ParseException(
						"No trackers block found or missing trackers",
						str.lineno());
			}

			// Parse the tracker number and put into the tracker object
			temporary_tracker = Tracker
					.getTrackerOfType_Fembic(current_tracker_type);
			temporary_tracker.setNumber((int) str.nval);
			// Continue and read the tracker parameters, assuming the format
			// 'param = val'
			temporary_tracker.parse_Fembic(tokenize(str), str.lineno(),
					nodelist, elementlist);

			// Check all required data has been filled in
			temporary_tracker.checkIndata();

			/*
			 * Getting this far means that all parameters for the tracker is
			 * defined, so return the temporary tracker now Note the upcasting!!
			 */
			return (Tracker) temporary_tracker;

			//
		} catch (java.io.IOException e) {
			throw new IllegalArgumentException(
					"IO Exception when reading Tracker" + e.getMessage());
		} catch (ParseException e) {
			throw new IllegalArgumentException(e.getMessage() + " in line: "
					+ (e.getErrorOffset() - 1));
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	/**
	 * This method reads the next constraint, sets up all the paremeters and
	 * returns a handle to the constraint. Creation date: (26/08/01 %T)
	 * 
	 * @return krockpackage.Node
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public Constraint getNextConstraint(RplVector nodelist)
			throws java.text.ParseException {
		Constraint temporary_constraint;
		// Assume the file has been opened
		// If we are not in a tracker block, search until we find one.
		try {
			// Start with "cleaning" out extra lines etc.
			while (str.nextToken() == java.io.StreamTokenizer.TT_EOL) {
				;
			}

			str.pushBack();

			// Now, assume we are reading a tracker line
			while ((str.nextToken() != java.io.StreamTokenizer.TT_WORD)
					|| this.isAKeyword(str.sval)) {
				str.pushBack();

				// This is not the case, so search until the next block of
				// trackers is found
				constraint_in_block = false;

				while (str.nextToken() != java.io.StreamTokenizer.TT_EOF) {
					if (str.ttype == java.io.StreamTokenizer.TT_WORD) {
						if (str.sval.toUpperCase().equals("CONSTRAINTS")) {
							if (str.nextToken() == java.io.StreamTokenizer.TT_WORD) {
								if (str.sval.toUpperCase().equals("OF")) {
									;
								} else {
									throw new java.text.ParseException(
											"No 'OF' defined in constraint block header",
											str.lineno());
								}
							}

							if (str.nextToken() == java.io.StreamTokenizer.TT_WORD) {
								if (str.sval.toUpperCase().equals("TYPE")) {
									;
								} else {
									throw new java.text.ParseException(
											"No 'TYPE' defined in constraint block header",
											str.lineno());
								}
							}

							if (str.nextToken() == java.io.StreamTokenizer.TT_WORD) {
								current_constraint_type = new String(
										str.sval.toUpperCase());
							} else {
								throw new java.text.ParseException(
										"Unknown type defined in constraint block header",
										str.lineno());
							}

							System.out.println("Constraint block found!");
							constraint_in_block = true;

							// Clear EOL
							str.nextToken();

							break;
						}
					}
				}
			}

			// We should now be in a block. If not, throw an error
			if (!constraint_in_block) {
				throw new java.text.ParseException(
						"No constraint block found or missing constraints",
						str.lineno());
			}

			// Parse the tracker number and put into the tracker object
			temporary_constraint = Constraint
					.getConstraintOfType_Fembic(current_constraint_type);
			temporary_constraint.setName(str.sval.toUpperCase().trim());
			// Continue and read the tracker parameters, assuming the format
			// 'param = val'
			temporary_constraint.parse_Fembic(tokenize(str), str.lineno(),
					nodelist);

			// Check all required data has been filled in
			temporary_constraint.checkIndata();

			/*
			 * Getting this far means that all parameters for the tracker is
			 * defined, so return the temporary tracker now Note the upcasting!!
			 */
			return (Constraint) temporary_constraint;

			//
		} catch (java.io.IOException e) {
			throw new IllegalArgumentException(
					"IO Exception when reading Constraint" + e.getMessage());
		} catch (ParseException e) {
			throw new IllegalArgumentException(e.getMessage() + " in line: "
					+ (e.getErrorOffset() - 1));
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	/**
	 * This method reads the next load, sets up all the paremeters and returns a
	 * handle to the load. Creation date: (26/08/01 %T)
	 * 
	 * @return krockpackage.Node
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public Load getNextLoad(RplVector nodelist) throws java.text.ParseException {
		// Assume the file has been opened
		// If we are not in a load block, search until we find one.
		try {
			if (!in_block) {
				while (str.nextToken() != java.io.StreamTokenizer.TT_EOF) {
					if (str.ttype == java.io.StreamTokenizer.TT_WORD) {
						if (str.sval.toUpperCase().equals("LOADS")
								&& (str.nextToken() == str.TT_EOL)) {
							in_block = true;

							break;
						}
					}
				}
			}

			// We should now be in a block. If not, throw an error
			if (!in_block) {
				throw new java.text.ParseException(
						"No loads block found or missing loads", str.lineno());
			}

			// Time to read the next load set
			// Assume that we are at the beginning of a line, so the first token
			// should now be the load set name
			// However, start with "cleaning" out extra lines etc.
			while (str.nextToken() == java.io.StreamTokenizer.TT_EOL) {
				;
			}

			// Now, check that we are reading the load name
			if (str.ttype != java.io.StreamTokenizer.TT_WORD) {
				throw new java.text.ParseException("No load name found",
						str.lineno());
			}

			// Parse and put into the load object
			temporary_load = new Load();
			temporary_load.name = str.sval.toUpperCase();

			// Continue and read the parameters for the Load
			temporary_load.parse_Fembic(tokenize(str), str.lineno());

			// Check all required data has been filled in
			temporary_load.checkIndata();

			// OK, everything read now. Lets return the load
			return temporary_load;
		} catch (java.io.IOException e) {
			throw new IllegalArgumentException("IO Exception when reading Load"
					+ e.getMessage());
		} catch (ParseException e) {
			throw new IllegalArgumentException(e.getMessage() + " in line: "
					+ (e.getErrorOffset() - 1));
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	/**
	 * This method reads the next material type, sets up the paremeters and
	 * returns a handle Creation date: (26/08/01 %T)
	 * 
	 * @return krockpackage.Material
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public Material getNextMaterial() throws java.text.ParseException {
		Material temporary_material;
		// Assume the file has been opened
		// If we are not in a material block, search until we find one.
		try {
			// Start with "cleaning" out extra lines etc.
			while (str.nextToken() == java.io.StreamTokenizer.TT_EOL) {
				;
			}

			str.pushBack();

			// Now, assume we are reading a material line
			while ((str.nextToken() != java.io.StreamTokenizer.TT_WORD)
					|| this.isAKeyword(str.sval)) {
				str.pushBack();

				// This is not the case, so search until the next block of
				// elements is found
				material_in_block = false;

				while (str.nextToken() != java.io.StreamTokenizer.TT_EOF) {
					if (str.ttype == java.io.StreamTokenizer.TT_WORD) {
						if (str.sval.toUpperCase().equals("MATERIALS")) {
							if (str.nextToken() == java.io.StreamTokenizer.TT_WORD) {
								if (str.sval.toUpperCase().equals("OF")) {
									;
								} else {
									throw new java.text.ParseException(
											"No 'OF' defined in material block header",
											str.lineno());
								}
							}

							if (str.nextToken() == java.io.StreamTokenizer.TT_WORD) {
								if (str.sval.toUpperCase().equals("TYPE")) {
									;
								} else {
									throw new java.text.ParseException(
											"No 'TYPE' defined in material block header",
											str.lineno());
								}
							}

							if (str.nextToken() == java.io.StreamTokenizer.TT_WORD) {
								current_element_type = new String(
										str.sval.toUpperCase());
							} else {
								throw new java.text.ParseException(
										"Unknown type defined in element block header",
										str.lineno());
							}

							System.out.println("Block found!");
							material_in_block = true;

							// Clear EOL
							str.nextToken();

							break;
						}
					}
				}
			}

			// We should now be in a block. If not, throw an error
			if (!material_in_block) {
				throw new java.text.ParseException(
						"No materials block found or missing materials",
						str.lineno());
			}

			// Parse the material type and create the suitable material object
			temporary_material = Material
					.getMaterialOfType_Fembic(current_element_type);
			temporary_material.setName(str.sval.toUpperCase());
			// Now, feed the parameters to the object
			temporary_material.parse_Fembic(tokenize(str), str.lineno());

			// Check all required data has been filled in
			temporary_material.checkIndata();

			/*
			 * Getting this far means that all parameters for the element is
			 * defined, so return the temporary element now Note the upcasting!!
			 */
			return (Material) temporary_material;

			//
		} catch (java.io.IOException e) {
			throw new IllegalArgumentException(
					"IO Exception when reading Material" + e.getMessage());
		} catch (ParseException e) {
			throw new IllegalArgumentException(e.getMessage() + " in line: "
					+ (e.getErrorOffset() - 1));
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	/**
	 * This method reads the next node, sets up all the paremeters and returns a
	 * handle to the node. Creation date: (26/08/01 %T)
	 * 
	 * @return krockpackage.Node
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public Node getNextNode(RplVector constraintlist, RplVector loadlist)
			throws java.text.ParseException {
		// Assume the file has been opened
		// If we are not in a node block, search until we find one.
		try {
			// Start with "cleaning" out extra lines etc.
			while (str.nextToken() == java.io.StreamTokenizer.TT_EOL) {
				;
			}

			str.pushBack();

			// Now, check that we are reading the node number
			while ((str.nextToken() != java.io.StreamTokenizer.TT_NUMBER)
					&& (str.ttype != java.io.StreamTokenizer.TT_EOF)) {
				// This is not the case. Now search until next block of nodes is
				// found and try again.
				in_block = false;
				str.pushBack();

				while (str.nextToken() != java.io.StreamTokenizer.TT_EOF) {
					if (str.ttype == java.io.StreamTokenizer.TT_WORD) {
						if (str.sval.toUpperCase().equals("NODES")
								&& (str.nextToken() == str.TT_EOL)) {
							in_block = true;

							break;
						}
					}
				}
			}

			// We should now be in a block. If not, throw an error
			if (!in_block) {
				throw new java.text.ParseException(
						"No nodes block found or missing nodes", str.lineno());
			}

			// Time to read the next node element
			// Parse and put into the node object

			temporary_node = new Node();
			temporary_node.setNumber((int) str.nval);

			// Continue and read the X coordinates for the node
			temporary_node.parse_Fembic(tokenize(str), str.lineno(),
					constraintlist, loadlist);

			// Check all required data has been filled in
			temporary_node.checkIndata();

			return temporary_node;
		} catch (java.io.IOException e) {
			throw new IllegalArgumentException(e.getMessage());
		} catch (ParseException e) {
			throw new IllegalArgumentException(e.getMessage() + " in line: "
					+ (e.getErrorOffset() - 1));
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	/**
	 * Insert the method's description here. Creation date: (26/08/01 %T)
	 * 
	 * @param param
	 *            java.lang.String
	 * 
	 * @return boolean
	 */
	private boolean isAKeyword(String param) {
		int i;

		for (i = 0; i < keywords.length; i++) {
			if (keywords[i].equals(param.toUpperCase())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Insert the method's description here. Creation date: (28/08/01 %T)
	 * 
	 * @return int
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public int numberOfConstraints() throws java.text.ParseException {
		int noc = 0;

		// Open file and start reading from beginning
		this.open();

		try {
			// Start examining the file and continue until end of file is
			// reached
			while (str.nextToken() != str.TT_EOF) {
				// check token for the key word "constraints"
				if (str.ttype == str.TT_WORD) {
					if (str.sval.toUpperCase().equals("CONSTRAINTS")) {
						while (str.nextToken() != str.TT_EOL) {
							;
						}

						str.pushBack();

						while (str.nextToken() != str.TT_EOF) {
							// Repeat this until a keyword is found. That means
							// block is finished.
							if (str.ttype == str.TT_WORD) {
								if (this.isAKeyword(str.sval)) {
									str.pushBack();

									break;
								}
							}

							/*
							 * getting this far means we are now about to read
							 * trackers The first token should be a word if a
							 * tracker is defined
							 */
							if (str.ttype == str.TT_WORD) {
								noc++;

								// Move to end of line
								while ((str.nextToken() != str.TT_EOL)
										&& (str.ttype != str.TT_EOF)) {
									;
								}
							}
						}
					}
				}
			}

			System.out.println("Found " + noc + " constraints");
		} catch (IOException ioe) {
			System.out
					.println("IOException when reading a constraintline from indata file");
			this.close();

			return -1;
		}

		this.close();

		return noc;
	}

	/**
	 * Insert the method's description here. Creation date: (28/08/01 %T)
	 * 
	 * @return int
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public int numberOfControls() throws java.text.ParseException {
		int noc = 0;

		// Open file and start reading from beginning
		this.open();

		try {
			// Start examining the file and continue until end of file is
			// reached
			while (str.nextToken() != str.TT_EOF) {
				// check token for the key word "CONTROLS"
				if (str.ttype == str.TT_WORD) {
					if (str.sval.toUpperCase().equals("CONTROLS")
							&& (str.nextToken() == str.TT_EOL)) {
						while (str.nextToken() != str.TT_EOF) {
							// getting this far means we are now about to read
							// constraints
							// The first token should be a constraint name if an
							// element is defined
							if (str.ttype == str.TT_WORD) {
								if (!this.isAKeyword(str.sval)) {
									noc++;

									// Move to end of line
									while ((str.nextToken() != str.TT_EOL)
											&& (str.ttype != str.TT_EOF)) {
										;
									}
								} else {
									str.pushBack();

									break;
								}
							}
						}
					}
				}
			}

			System.out.println("Found " + noc + " controls");
		} catch (IOException ioe) {
			System.out
					.println("IOException when reading a line from indata file");
			this.close();

			return -1;
		}

		if (noc == 0) {
			this.close();
			throw new java.text.ParseException("No controls found", 0);
		}

		this.close();

		return noc;
	}

	/**
	 * This method examines the indata file to determine the number of elements
	 * supplied there. Note that there may be several different types of
	 * elements, each under a new headline. Example: elements of type beam 1
	 * nodes=[1,2] A=5.0 ..... elements of type rod 1 nodes=[2,3] A=3.2 .....
	 * Creation date: (21/08/01 %T)
	 * 
	 * @return int
	 */
	public int numberOfElements() throws java.text.ParseException {
		int noe = 0;

		// Open file and start reading from beginning
		this.open();

		try {
			// Start examining the file and continue until end of file is
			// reached
			while (str.nextToken() != str.TT_EOF) {
				// check token for the key word "elements"
				if (str.ttype == str.TT_WORD) {
					if (str.sval.toUpperCase().equals("ELEMENTS")) {
						while (str.nextToken() != str.TT_EOL) {
							;
						}

						str.pushBack();

						while (str.nextToken() != str.TT_EOF) {
							// Repeat this until a keyword is found. That means
							// block is finished.
							if (str.ttype == str.TT_WORD) {
								if (this.isAKeyword(str.sval)) {
									str.pushBack();

									break;
								}
							}

							/*
							 * getting this far means we are now about to read
							 * elements The first token should be a line number
							 * if an element is defined
							 */
							if (str.ttype == str.TT_NUMBER) {
								noe++;

								// Move to end of line
								while ((str.nextToken() != str.TT_EOL)
										&& (str.ttype != str.TT_EOF)) {
									;
								}
							}
						}
					}
				}
			}

			System.out.println("Found " + noe + " elements");
		} catch (IOException ioe) {
			System.out
					.println("IOException when reading a line from indata file");
			this.close();

			return -1;
		}

		if (noe == 0) {
			this.close();
			throw new java.text.ParseException("No elements found", 0);
		}

		this.close();

		return noe;
	}

	/**
	 * This method examines the indata file to determine the number of trackers
	 * supplied there. Note that there may be several different types of
	 * trackers, each under a new headline. Example: trackers of type
	 * sectionforce 1 nodes = [1,2,3] filename = ..... trackers of type
	 * sectionmoment 1 nodes = [2,3] filename = ..... Creation date: (21/08/01
	 * %T)
	 * 
	 * @return int
	 */
	public int numberOfTrackers() throws java.text.ParseException {
		int not = 0;

		// Open file and start reading from beginning
		this.open();

		try {
			// Start examining the file and continue until end of file is
			// reached
			while (str.nextToken() != str.TT_EOF) {
				// check token for the key word "trackers"
				if (str.ttype == str.TT_WORD) {
					if (str.sval.toUpperCase().equals("TRACKERS")) {
						while (str.nextToken() != str.TT_EOL) {
							;
						}

						str.pushBack();

						while (str.nextToken() != str.TT_EOF) {
							// Repeat this until a keyword is found. That means
							// block is finished.
							if (str.ttype == str.TT_WORD) {
								if (this.isAKeyword(str.sval)) {
									str.pushBack();

									break;
								}
							}

							/*
							 * getting this far means we are now about to read
							 * trackers The first token should be a line number
							 * if a tracker is defined
							 */
							if (str.ttype == str.TT_NUMBER) {
								not++;

								// Move to end of line
								while ((str.nextToken() != str.TT_EOL)
										&& (str.ttype != str.TT_EOF)) {
									;
								}
							}
						}
					}
				}
			}

			System.out.println("Found " + not + " trackers");
		} catch (IOException ioe) {
			System.out
					.println("IOException when reading a trackerline from indata file");
			this.close();

			return -1;
		}

		this.close();

		return not;
	}

	/**
	 * Insert the method's description here. Creation date: (28/08/01 %T)
	 * 
	 * @return int
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public int numberOfGroups() throws java.text.ParseException {
		int noc = 0;

		// Open file and start reading from beginning
		this.open();

		try {
			// Start examining the file and continue until end of file is
			// reached
			while (str.nextToken() != str.TT_EOF) {
				// check token for the key word "loads"
				if (str.ttype == str.TT_WORD) {
					if (str.sval.toUpperCase().equals("GROUPS")
							&& (str.nextToken() == str.TT_EOL)) {
						while (str.nextToken() != str.TT_EOF) {
							// getting this far means we are now about to read
							// groups
							// The first token should be a group set name if a
							// set is defined
							if (str.ttype == str.TT_WORD) {
								if (!this.isAKeyword(str.sval)) {
									noc++;

									// Move to end of line
									while ((str.nextToken() != str.TT_EOL)
											&& (str.ttype != str.TT_EOF)) {
										;
									}
								} else {
									str.pushBack();

									break;
								}
							}
						}
					}
				}
			}

			System.out.println("Found " + noc + " groups");
		} catch (IOException ioe) {
			System.out
					.println("IOException when reading a line from indata file");
			this.close();

			return -1;
		}

		this.close();

		return noc;
	}

	/**
	 * Insert the method's description here. Creation date: (28/08/01 %T)
	 * 
	 * @return int
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public int numberOfLoads() throws java.text.ParseException {
		int noc = 0;

		// Open file and start reading from beginning
		this.open();

		try {
			// Start examining the file and continue until end of file is
			// reached
			while (str.nextToken() != str.TT_EOF) {
				// check token for the key word "loads"
				if (str.ttype == str.TT_WORD) {
					if (str.sval.toUpperCase().equals("LOADS")
							&& (str.nextToken() == str.TT_EOL)) {
						while (str.nextToken() != str.TT_EOF) {
							// getting this far means we are now about to read
							// loads
							// The first token should be a load set name if a
							// set is defined
							if (str.ttype == str.TT_WORD) {
								if (!this.isAKeyword(str.sval)) {
									noc++;

									// Move to end of line
									while ((str.nextToken() != str.TT_EOL)
											&& (str.ttype != str.TT_EOF)) {
										;
									}
								} else {
									str.pushBack();

									break;
								}
							}
						}
					}
				}
			}

			System.out.println("Found " + noc + " loads");
		} catch (IOException ioe) {
			System.out
					.println("IOException when reading a line from indata file");
			this.close();

			return -1;
		}

		this.close();

		return noc;
	}

	/**
	 * Insert the method's description here. Creation date: (21/08/01 %T)
	 * 
	 * @return int
	 */
	public int numberOfMaterials() throws java.text.ParseException {
		int nom = 0;

		// Open file and start reading from beginning
		this.open();

		try {
			// Start examining the file and continue until end of file is
			// reached
			while (str.nextToken() != str.TT_EOF) {
				// check token for the key word "materials"
				if (str.ttype == str.TT_WORD) {
					if (str.sval.toUpperCase().equals("MATERIALS")) {
						// Clean out line
						while (str.nextToken() != str.TT_EOL) {
							;
						}

						// go in to block
						while (str.nextToken() != str.TT_EOF) {
							// getting this far means we are now about to read
							// elements
							// The first token should be a material name if an
							// element is defined
							if (str.ttype == str.TT_WORD) {
								if (!this.isAKeyword(str.sval)) {
									nom++;

									// Move to end of line
									while ((str.nextToken() != str.TT_EOL)
											&& (str.ttype != str.TT_EOF)) {
										;
									}
								} else {
									str.pushBack();

									break;
								}
							}
						}
					}
				}
			}

			System.out.println("Found " + nom + " materials");
		} catch (IOException ioe) {
			System.out
					.println("IOException when reading a line from indata file");
			this.close();

			return -1;
		}

		if (nom == 0) {
			this.close();
			throw new java.text.ParseException("No materials found", 0);
		}

		this.close();

		return nom;
	}

	/**
	 * Insert the method's description here. Creation date: (21/08/01 %T)
	 * 
	 * @return int
	 */
	public int numberOfNodes() throws java.text.ParseException {
		int non = 0;

		// Open file and start reading from beginning
		this.open();

		try {
			// Start examining the file and continue until end of file is
			// reached
			while (str.nextToken() != str.TT_EOF) {
				// check token for the key word "nodes"
				if (str.ttype == str.TT_WORD) {
					if (str.sval.toUpperCase().equals("NODES")
							&& (str.nextToken() == str.TT_EOL)) {
						while (str.nextToken() != str.TT_EOF) {
							// Repeat this until a keyword is found. That means
							// block is finished.
							if (str.ttype == str.TT_WORD) {
								if (this.isAKeyword(str.sval)) {
									break;
								}
							}

							// getting this far means we are now about to read
							// nodes
							// The first token should be a line number if an
							// element is defined
							if (str.ttype == str.TT_NUMBER) {
								non++;

								// Move to end of line
								while ((str.nextToken() != str.TT_EOL)
										&& (str.ttype != str.TT_EOF)) {
									;
								}
							}
						}
					}
				}
			}

			System.out.println("Found " + non + " nodes");
		} catch (IOException ioe) {
			System.out
					.println("IOException when reading a line from indata file");
			this.close();

			return -1;
		}

		if (non == 0) {
			this.close();
			throw new java.text.ParseException("No nodes found", 0);
		}

		this.close();

		return non;
	}

	/**
	 * This constructor immediately tries to open the file.
	 */
	public void open() {
		FileReader fr;

		// Opening the file means that parameters should be set to zero
		in_block = false;
		control_in_block = false;
		constraint_in_block = false;
		element_in_block = false;

		// Try to open the indata file
		try {
			fr = new FileReader(filename);
		} catch (FileNotFoundException fnfe) {
			System.out.println("Fembic file does not exist");

			return;
		}

		try {
			br = new BufferedReader(fr);
		} catch (IllegalArgumentException ioe) {
			System.out
					.println("An IllegalArgumentException has occurred when opening the indata file");

			return;
		}

		str = new ModifiedStreamTokenizer(br);
	}

	/**
	 * This method returns the writer to be used for the results
	 */
	public Writer getWriter(RplVector nodelist, RplVector elementlist,
			Controlset control, RemoteObject[] cluster_nodes)
			throws RemoteException {
		Writer a;
		a = Writer.getWriterOfType_Fembic(control.getWriter(), nodelist,
				elementlist, cluster_nodes);

		a.checkIndata();

		return a;
	}

	/**
	 * This method returns the Trackwriter to be used for the trackers
	 */
	public TrackWriter getTrackWriter(RplVector trackerlist,
			Controlset control, RemoteObject[] cluster_nodes)
			throws RemoteException {
		TrackWriter a;
		a = TrackWriter.getTrackWriterOfType_Fembic(control.getTrackWriter(),
				trackerlist);

		a.checkIndata();

		return a;
	}

	/**
	 * Overriding this routine to add some more features related to Fembic.
	 */
	public Token[] tokenize(java.io.StreamTokenizer str) throws IOException {
		int i;
		String temp;
		Vector v = new Vector();
		Token t;
		Token[] arr;

		// Start by reading all the words and numbers on the line into the p
		// vector.
		while ((str.nextToken() != java.io.StreamTokenizer.TT_EOL)
				&& (str.ttype != java.io.StreamTokenizer.TT_EOF)) {
			if (str.ttype == java.io.StreamTokenizer.TT_WORD) {
				v.add(new Token(str.sval.trim()));
			} else {
				v.add(new Token(str.nval));
			}
		}

		// Now, begin examining the vector for correct parsing.
		for (i = 0; i < v.size(); i++) {
			// Check if this token is a word.
			t = (Token) v.elementAt(i);

			if (t.is_a_word()) {
				temp = new String(t.getw());

				// This may be a beginning of a [ set. If so, search until next
				// set if found.
				if (temp.startsWith("[") && !temp.endsWith("]")) {
					v.remove(i);

					while (!temp.endsWith("]")) {
						if (i >= v.size()) {
							throw new IOException(
									"IO Exception trying to find end ] of a parameter set in string\n");
						}

						t = (Token) v.elementAt(i);
						v.remove(i);

						if (t.is_a_number()) {
							temp += new Double(t.getn()).toString();
						} else {
							temp += t.getw().trim();
						}
					}

					v.insertElementAt(new Token(temp), i);
				} else
				// Check to see if there is an = sign in there (i.e. no spaces
				// around the =)
				// If so, split up into three tokens and make the parser read
				// them again.
				if ((temp.indexOf("=") != -1) && (temp.length() > 1)) {
					v.remove(i);

					if (temp.indexOf("=") != 0) {
						v.insertElementAt(
								new Token(temp.substring(0, temp.indexOf("="))),
								i);
					} else {
						i--;
					}

					v.insertElementAt(new Token(new String("=")), i + 1);

					if (temp.indexOf("=") != (temp.length() - 1)) {
						v.insertElementAt(
								new Token(temp.substring(temp.indexOf("=") + 1,
										temp.length())), i + 2);
					}

					i--;
				}
			} else
			// t is a number, leave it alone
			{
			}
		}

		arr = new Token[v.size()];

		for (i = 0; i < v.size(); i++) {
			arr[i] = (Token) v.elementAt(i);
		}

		return arr;
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-31
	 * 00.46.27)
	 * 
	 * @return int
	 * 
	 * @exception java.text.ParseException
	 *                The exception description.
	 */
	public void preProcess() throws java.text.ParseException {
	}

}

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

package run.writers;

import java.io.*;

import jp.sync.Barrier;

import run.Element;
import run.Node;
import run.RplVector;
import run.Writer;

/**
 * Insert the type's description here. Creation date: (2001-10-31 00.44.39)
 * 
 * @author:
 */
public class GidWriter extends Writer {
	private java.lang.String filename;
	private double time;
	private int counter;

	/**
	 * GidWriter constructor comment.
	 */
	public GidWriter(RplVector nlist, RplVector elist) {
		super(nlist, elist);
		counter = 0;
	}

	/**
	 * Insert the method's description here. Creation date: (23/11/01 %T)
	 * 
	 * @throws IOException
	 */
	private BufferedWriter open(boolean append) throws IOException {
		// Open a file to write to (note that it is opend for append the second
		// time or
		// if another client is assumed to have written before)
		return new BufferedWriter(new FileWriter(filename + ".flavia.res",
				append));
	}

	/**
	 * This method does any nessessary checking and initializations for the
	 * writer Creation date: (23/11/01 %T)
	 */
	public void initialize() {
	}

	/**
	 * Insert the method's description here. Creation date: (07/11/01 %T)
	 */
	public void write(String fname, double currtime) {
		filename = new String(fname);
		time = currtime;

		try {
			if (counter == 0) {
				writeMesh();
			}

			writeResult();
			counter++;
		} catch (IOException ioe) {
			System.out.println(ioe);

			return;
		}
	}

	/**
	 * Insert the method's description here. Creation date: (07/11/01 %T)
	 * 
	 * @throws InterruptedException
	 */
	public void writeParallel(String fname, double currtime, int[] indicies,
			Barrier barrier, int client_nr, int nr_of_clients)
			throws InterruptedException {
		filename = new String(fname);
		time = currtime;

		try {
			if (counter == 0 && client_nr == 0) {
				this.writeMesh();
			}

			this.writeResultParallel(barrier, client_nr, nr_of_clients);

			counter++;
		} catch (IOException ioe) {
			System.out.println(ioe);
			return;
		}

	}

	/**
	 * Insert the method's description here. Creation date: (24/11/01 %T)
	 * 
	 * @exception java.io.IOException
	 *                The exception description.
	 */
	private void writeMesh() throws java.io.IOException {
		int i;
		int j;
		int k;
		String type;
		Element temp_element;

		// Open a file to write to
		BufferedWriter bw = new BufferedWriter(new FileWriter(filename
				+ ".flavia.msh"));
		bw.write("# Output file from impact, describing the model mesh \n");
		bw.write("# \n");

		/*
		 * Format of the mesh data is according to the new GID format as
		 * follows:
		 * 
		 * MESH "meshname" DIMENSION number_of_dimensions ELEMTYPE
		 * type_of_element NNODE number_of_nodes
		 * 
		 * where number_of_dimensions = number of space dimensions (always 3
		 * here) type_of_element = which type of element is described. Can be
		 * one of the following: Point, Linear, Triangle, Quadilateral,
		 * Tetrahedra, Hexahedra number_of_nodes = number of nodes the selected
		 * element type will have on: Point: 1 node Linear: 2 or 3 nodes
		 * Triangle: 3 or 6 nodes Quadila... 4, 8 or 9 nodes Tetra.... 4 or 10
		 * nodes Hexahed.. 8, 20 or 27 nodes
		 * 
		 * The mesh is then followed by nodes description within a COORDINATES /
		 * END COORDINATES block. Thereafter comes the elements
		 */

		// Loop through the element list and make each element of a new type
		// write its header
		for (i = 0; i < elementlist.size(); i++) {
			temp_element = (Element) elementlist.elementAt(i);

			if (!temp_element.isProcessed()) {
				// We have found an unprocessed element. Print a subheader and
				// then all data from elements of same type.
				type = temp_element.getType();
				bw.write(temp_element.print_Gid(Element.MESH_HEADER, 0));
				bw.write("\n");

				// If this is the first time we write this header, follow up
				// with a listing of all the nodes
				if (i == 0) {
					// Start listing the nodes
					bw.write("COORDINATES \n");

					for (k = 0; k < nodelist.size(); k++) {

						if (((Node) nodelist.elementAt(k)).getNumber() >= 0) {

							bw.write(((Node) nodelist.elementAt(k)).getNumber()
									+ " \t");
							bw.write(((Node) nodelist.elementAt(k))
									.getX_pos_orig() + " \t");
							bw.write(((Node) nodelist.elementAt(k))
									.getY_pos_orig() + " \t");
							bw.write(((Node) nodelist.elementAt(k))
									.getZ_pos_orig() + "\n");
						}
					}
					bw.write("END COORDINATES\n\n");
				}

				// Now, run through the array, print element data and try to
				// find elements of the same type and mark as processed
				bw.write("ELEMENTS\n");

				for (j = i; j < elementlist.size(); j++) {
					temp_element = (Element) elementlist.elementAt(j);

					if ((temp_element.getType().equals(type))
							&& !temp_element.isProcessed()) {
						bw.write(temp_element.print_Gid(Element.MESH, 0));
						temp_element.setProcessed(true);
					}
				}

				bw.write("END ELEMENTS\n");
			}
		}

		// Finished writing headers. Now set them as unprocessed.
		for (k = 0; k < elementlist.size(); k++) {
			((Element) elementlist.elementAt(k)).setProcessed(false);
		}

		// finished! flush and close.
		bw.flush();
		bw.close();
	}

	/**
	 * This method writes the calculated results from the solutions. The amount
	 * of data to be written is decided by the controlobject. Creation date:
	 * (24/11/01 %T)
	 * 
	 * @exception java.io.IOException
	 *                The exception description.
	 */
	private void writeResult() throws java.io.IOException {
		int i;
		int j;
		String type;
		int gpn;
		Element temp_element;
		String temp_out;
		String temp_header;
		Node temp_node;

		// Open a file to write to (note that it is opend for append the second
		// time).
		BufferedWriter bw = new BufferedWriter(new FileWriter(filename
				+ ".flavia.res", (counter != 0)));

		/*
		 * Format of the result data is according to the new GID format as
		 * follows:
		 * 
		 * label solutiontype time data_type data_loc desc_comp
		 * 
		 * where label = Data label to show up on the buttons solutiontype = 1
		 * for time analysis (this program) time = current time data_type = type
		 * of data to be expected 1 - scalar (only one data) 2 - vector (three
		 * data on one line) 3 - matrix (six lines) .... data_loc = position of
		 * the data 1 - in nodes 2 - on the Gauss points desc_comp = what to be
		 * displayed, specified or not 0 - No description 1 - Description
		 * (following the header)
		 * 
		 * The results are then followed in blocks. One for each timestep.
		 */
		if (counter == 0) {
			// Loop through the element list and make each element of a new type
			// write its header
			for (i = 0; i < elementlist.size(); i++) {
				temp_element = (Element) elementlist.elementAt(i);

				if (!temp_element.isProcessed()
						&& !temp_element.isDeActivated()) {
					// We have found an unprocessed element. Print a subheader
					// and then all data from elements of same type.
					type = temp_element.getType();
					bw.write(temp_element.print_Gid(Element.RESULT_HEADER, 0));
					bw.write("\n");

					// Now, run through the array and try to find elements of
					// the same type and mark as processed
					for (j = i; j < elementlist.size(); j++) {
						temp_element = (Element) elementlist.elementAt(j);

						if ((temp_element.getType().equals(type))
								&& !temp_element.isProcessed()) {
							temp_element.setProcessed(true);
						}
					}
				}
			}

			// Finished writing headers. Now set them as unprocessed.
			for (i = 0; i < elementlist.size(); i++) {
				((Element) elementlist.elementAt(i)).setProcessed(false);
			}
		}

		//
		bw.write("  DISPLACEMENTS" + " 1 " + time + " 2 1 0\n");

		// Start listing the nodes (do not print internal nodes with negative
		// node numbers)
		for (i = 0; i < nodelist.size(); i++) {

			temp_node = (Node) nodelist.elementAt(i);

			if (temp_node.getNumber() >= 0 && !temp_node.isDeActivated()) {
				bw.write(temp_node.getNumber() + " \t");
				bw.write(temp_node.getX_pos() - temp_node.getX_pos_orig()
						+ " \t\t");
				bw.write(temp_node.getY_pos() - temp_node.getY_pos_orig()
						+ " \t\t");
				bw.write(temp_node.getZ_pos() - temp_node.getZ_pos_orig()
						+ "\n");
			}
		}
		/*
		 * Now print the data with corresponding header Loop through the list of
		 * elements and print the unprocessed elements
		 */

		// This is for LOCAL STRESSES
		for (i = 0; i < elementlist.size(); i++) {
			temp_element = (Element) elementlist.elementAt(i);

			if (!temp_element.isProcessed() && !temp_element.isDeActivated()) {
				// We have found an unprocessed element. Prepare a subheader and
				// then all data from elements of same type.
				type = temp_element.getType();
				temp_header = new String("LOCAL_STRESSES_ 1 " + time
						+ temp_element.print_Gid(Element.RESULT_SUB_HEADER, 0));

				// Now, run through the array and try to find elements of the
				// same type to print results from
				for (j = i; j < elementlist.size(); j++) {
					temp_element = (Element) elementlist.elementAt(j);

					if ((temp_element.getType().equals(type))
							&& !temp_element.isProcessed()
							&& !temp_element.isDeActivated()) {
						temp_element.setProcessed(true);
						temp_out = new String("");

						// Loop through the integration points and print the
						// result from each one of them
						for (gpn = 0; gpn < temp_element
								.getNumberOfIntegrationPoints(); gpn++) {
							temp_out = temp_out
									+ temp_element.print_Gid(
											Element.RESULT_STRESS_LOCAL, gpn);
						}

						/*
						 * Now, if there is something in temp_out, the result
						 * type is supported and we can print the header and the
						 * result
						 */
						if ((i == j) && !temp_out.equals("")) {
							bw.write(temp_header + temp_out);
						} else {
							bw.write(temp_out);
						}
					}
				}
			}
		}

		for (i = 0; i < elementlist.size(); i++) {
			((Element) elementlist.elementAt(i)).setProcessed(false);
		}

		// This is for GLOBAL STRESSES
		for (i = 0; i < elementlist.size(); i++) {
			temp_element = (Element) elementlist.elementAt(i);

			if (!temp_element.isProcessed() && !temp_element.isDeActivated()) {
				// We have found an unprocessed element. Prepare a subheader and
				// then all data from elements of same type.
				type = temp_element.getType();
				temp_header = new String("GLOBAL_STRESSES 1 " + time
						+ temp_element.print_Gid(Element.RESULT_SUB_HEADER, 0));

				// Now, run through the array and try to find elements of the
				// same type to print results from
				for (j = i; j < elementlist.size(); j++) {
					temp_element = (Element) elementlist.elementAt(j);

					if ((temp_element.getType().equals(type))
							&& !temp_element.isProcessed()
							&& !temp_element.isDeActivated()) {
						temp_element.setProcessed(true);
						temp_out = new String("");

						// Loop through the integration points and print the
						// result from each one of them
						for (gpn = 0; gpn < temp_element
								.getNumberOfIntegrationPoints(); gpn++) {
							temp_out = temp_out
									+ temp_element.print_Gid(
											Element.RESULT_STRESS_GLOBAL, gpn);
						}

						/*
						 * Now, if there is something in temp_out, the result
						 * type is supported and we can print the header and the
						 * result
						 */
						if ((i == j) && !temp_out.equals("")) {
							bw.write(temp_header + temp_out);
						} else {
							bw.write(temp_out);
						}
					}
				}
			}
		}

		for (i = 0; i < elementlist.size(); i++) {
			((Element) elementlist.elementAt(i)).setProcessed(false);
		}

		// This is for LOCAL STRESSES
		for (i = 0; i < elementlist.size(); i++) {
			temp_element = (Element) elementlist.elementAt(i);

			if (!temp_element.isProcessed() && !temp_element.isDeActivated()) {
				// We have found an unprocessed element. Prepare a subheader and
				// then all data from elements of same type.
				type = temp_element.getType();
				temp_header = new String("LOCAL_STRAINS__ 1 " + time
						+ temp_element.print_Gid(Element.RESULT_SUB_HEADER, 0));

				// Now, run through the array and try to find elements of the
				// same type to print results from
				for (j = i; j < elementlist.size(); j++) {
					temp_element = (Element) elementlist.elementAt(j);

					if ((temp_element.getType().equals(type))
							&& !temp_element.isProcessed()
							&& !temp_element.isDeActivated()) {
						temp_element.setProcessed(true);
						temp_out = new String("");

						// Loop through the integration points and print the
						// result from each one of them
						for (gpn = 0; gpn < temp_element
								.getNumberOfIntegrationPoints(); gpn++) {
							temp_out = temp_out
									+ temp_element.print_Gid(
											Element.RESULT_STRAIN_LOCAL, gpn);
						}

						/*
						 * Now, if there is something in temp_out, the result
						 * type is supported and we can print the header and the
						 * result
						 */
						if ((i == j) && !temp_out.equals("")) {
							bw.write(temp_header + temp_out);
						} else {
							bw.write(temp_out);
						}
					}
				}
			}
		}

		for (i = 0; i < elementlist.size(); i++) {
			((Element) elementlist.elementAt(i)).setProcessed(false);
		}

		// This is for LOCAL STRESSES
		for (i = 0; i < elementlist.size(); i++) {
			temp_element = (Element) elementlist.elementAt(i);

			if (!temp_element.isProcessed() && !temp_element.isDeActivated()) {
				// We have found an unprocessed element. Prepare a subheader and
				// then all data from elements of same type.
				type = temp_element.getType();
				temp_header = new String("GLOBAL_STRAINS_ 1 " + time
						+ temp_element.print_Gid(Element.RESULT_SUB_HEADER, 0));

				// Now, run through the array and try to find elements of the
				// same type to print results from
				for (j = i; j < elementlist.size(); j++) {
					temp_element = (Element) elementlist.elementAt(j);

					if ((temp_element.getType().equals(type))
							&& !temp_element.isProcessed()
							&& !temp_element.isDeActivated()) {
						temp_element.setProcessed(true);
						temp_out = new String("");

						// Loop through the integration points and print the
						// result from each one of them
						for (gpn = 0; gpn < temp_element
								.getNumberOfIntegrationPoints(); gpn++) {
							temp_out = temp_out
									+ temp_element.print_Gid(
											Element.RESULT_STRAIN_GLOBAL, gpn);
						}

						/*
						 * Now, if there is something in temp_out, the result
						 * type is supported and we can print the header and the
						 * result
						 */
						if ((i == j) && !temp_out.equals("")) {
							bw.write(temp_header + temp_out);
						} else {
							bw.write(temp_out);
						}
					}
				}
			}
		}

		for (i = 0; i < elementlist.size(); i++) {
			((Element) elementlist.elementAt(i)).setProcessed(false);
		}

		// finished! flush and close.
		bw.flush();
		bw.close();
	}

	/**
	 * This method writes the calculated results from the solutions. The amount
	 * of data to be written is decided by the controlobject. This method is
	 * very similar to the writeResult method, but designed for parallel writing
	 * (assuming a shared file system across network)
	 * 
	 * @exception java.io.IOException
	 *                The exception description.
	 * @throws InterruptedException
	 */
	private void writeResultParallel(Barrier barrier, int client_nr,
			int nr_of_clients) throws java.io.IOException, InterruptedException {
		int i;
		int j;
		String type;
		int gpn;
		Element temp_element;
		String temp_out, temp;
		String temp_header;
		BufferedWriter bw;
		Node temp_node;
		boolean something_to_print;

		if (counter == 0 && client_nr == 0) {

			bw = this.open(counter != 0 && client_nr != 0);

			// Loop through the element list and make each element of a new type
			// write its header
			for (i = 0; i < elementlist.size(); i++) {
				temp_element = (Element) elementlist.elementAt(i);

				if (!temp_element.isProcessed()
						&& !temp_element.isDeActivated()) {
					// We have found an unprocessed element. Print a subheader
					// and then all data from elements of same type.
					type = temp_element.getType();
					bw.write(temp_element.print_Gid(Element.RESULT_HEADER, 0));
					bw.write("\n");

					// Now, run through the array and try to find elements of
					// the same type and mark as processed
					for (j = i; j < elementlist.size(); j++) {
						temp_element = (Element) elementlist.elementAt(j);

						if ((temp_element.getType().equals(type))
								&& !temp_element.isProcessed()) {
							temp_element.setProcessed(true);
						}
					}
				}
			}

			// Finished writing headers. Now set them as unprocessed.
			for (i = 0; i < elementlist.size(); i++) {
				((Element) elementlist.elementAt(i)).setProcessed(false);
			}
			bw.flush();
			bw.close();
		}

		if (client_nr == 0) {
			bw = this.open(true);
			//
			bw.write("  DISPLACEMENTS" + " 1 " + time + " 2 1 0\n");

			// Start listing the nodes (do not print internal nodes with
			// negative node numbers)
			for (i = 0; i < nodelist.size(); i++) {

				temp_node = (Node) nodelist.elementAt(i);

				if (temp_node.getNumber() >= 0 && !temp_node.isDeActivated()) {
					bw.write(temp_node.getNumber() + " \t");
					bw.write(temp_node.getX_pos() - temp_node.getX_pos_orig()
							+ " \t\t");
					bw.write(temp_node.getY_pos() - temp_node.getY_pos_orig()
							+ " \t\t");
					bw.write(temp_node.getZ_pos() - temp_node.getZ_pos_orig()
							+ "\n");
				}
			}
			bw.flush();
			bw.close();

		}

		/*
		 * Now print the data with corresponding header Loop through the list of
		 * elements and print the unprocessed elements
		 */

		// This is for LOCAL STRESSES

		for (i = 0; i < elementlist.size(); i++) {
			temp_element = (Element) elementlist.elementAt(i);

			if (!temp_element.isProcessed() && !temp_element.isDeActivated()) {
				// We have found an unprocessed element. Prepare a subheader and
				// then all data from elements of same type.
				type = temp_element.getType();
				temp_header = new String("LOCAL_STRESSES_ 1 " + time
						+ temp_element.print_Gid(Element.RESULT_SUB_HEADER, 0));

				something_to_print = false;

				for (int cn = 0; cn < nr_of_clients; cn++) {
					if (cn == client_nr) {

						bw = this.open(true);
						temp_out = new String("");

						// Now, run through the array and try to find elements
						// of the same type to print results from
						for (j = i; j < elementlist.size(); j++) {
							temp_element = (Element) elementlist.elementAt(j);

							if (temp_element.getType().equals(type)
									&& !temp_element.isProcessed()) {

								temp_element.setProcessed(true);
								temp = new String("");

								// Loop through the integration points and print
								// the result from each one of them
								for (gpn = 0; gpn < temp_element
										.getNumberOfIntegrationPoints(); gpn++)
									temp += temp_element.print_Gid(
											Element.RESULT_STRESS_LOCAL, gpn);

								if (!temp.equals(""))
									something_to_print = true;
								if (temp_element.getCpu_number() == client_nr
										&& !temp_element.isDeActivated())
									temp_out += temp;
							}
						}

						if (something_to_print && client_nr == 0)
							bw.write(temp_header);

						bw.write(temp_out);

						bw.flush();
						bw.close();

					}

					barrier.sync();
				}
			}
		}

		for (i = 0; i < elementlist.size(); i++) {
			((Element) elementlist.elementAt(i)).setProcessed(false);
		}

		// This is for GLOBAL STRESSES

		for (i = 0; i < elementlist.size(); i++) {
			temp_element = (Element) elementlist.elementAt(i);

			if (!temp_element.isProcessed() && !temp_element.isDeActivated()) {
				// We have found an unprocessed element. Prepare a subheader and
				// then all data from elements of same type.
				type = temp_element.getType();
				temp_header = new String("GLOBAL_STRESSES_ 1 " + time
						+ temp_element.print_Gid(Element.RESULT_SUB_HEADER, 0));

				something_to_print = false;

				for (int cn = 0; cn < nr_of_clients; cn++) {
					if (cn == client_nr) {

						bw = this.open(true);
						temp_out = new String("");

						// Now, run through the array and try to find elements
						// of the same type to print results from
						for (j = i; j < elementlist.size(); j++) {
							temp_element = (Element) elementlist.elementAt(j);

							if (temp_element.getType().equals(type)
									&& !temp_element.isProcessed()) {

								temp_element.setProcessed(true);
								temp = new String("");

								// Loop through the integration points and print
								// the result from each one of them
								for (gpn = 0; gpn < temp_element
										.getNumberOfIntegrationPoints(); gpn++)
									temp += temp_element.print_Gid(
											Element.RESULT_STRESS_GLOBAL, gpn);

								if (!temp.equals(""))
									something_to_print = true;
								if (temp_element.getCpu_number() == client_nr
										&& !temp_element.isDeActivated())
									temp_out += temp;

							}
						}

						if (something_to_print && client_nr == 0)
							bw.write(temp_header);

						bw.write(temp_out);

						bw.flush();
						bw.close();

					}

					barrier.sync();
				}
			}
		}

		for (i = 0; i < elementlist.size(); i++) {
			((Element) elementlist.elementAt(i)).setProcessed(false);
		}

		// This is for LOCAL STRAINS

		for (i = 0; i < elementlist.size(); i++) {
			temp_element = (Element) elementlist.elementAt(i);

			if (!temp_element.isProcessed() && !temp_element.isDeActivated()) {
				// We have found an unprocessed element. Prepare a subheader and
				// then all data from elements of same type.
				type = temp_element.getType();
				temp_header = new String("LOCAL_STRAINS_ 1 " + time
						+ temp_element.print_Gid(Element.RESULT_SUB_HEADER, 0));

				something_to_print = false;

				for (int cn = 0; cn < nr_of_clients; cn++) {
					if (cn == client_nr) {

						bw = this.open(true);
						temp_out = new String("");

						// Now, run through the array and try to find elements
						// of the same type to print results from
						for (j = i; j < elementlist.size(); j++) {
							temp_element = (Element) elementlist.elementAt(j);

							if (temp_element.getType().equals(type)
									&& !temp_element.isProcessed()) {

								temp_element.setProcessed(true);
								temp = new String("");

								// Loop through the integration points and print
								// the result from each one of them
								for (gpn = 0; gpn < temp_element
										.getNumberOfIntegrationPoints(); gpn++)
									temp += temp_element.print_Gid(
											Element.RESULT_STRAIN_LOCAL, gpn);

								if (!temp.equals(""))
									something_to_print = true;
								if (temp_element.getCpu_number() == client_nr
										&& !temp_element.isDeActivated())
									temp_out += temp;

							}
						}

						if (something_to_print && client_nr == 0)
							bw.write(temp_header);

						bw.write(temp_out);

						bw.flush();
						bw.close();

					}

					barrier.sync();
				}
			}
		}

		for (i = 0; i < elementlist.size(); i++) {
			((Element) elementlist.elementAt(i)).setProcessed(false);
		}

		// This is for GLOBAL STRAINS

		for (i = 0; i < elementlist.size(); i++) {
			temp_element = (Element) elementlist.elementAt(i);

			if (!temp_element.isProcessed() && !temp_element.isDeActivated()) {
				// We have found an unprocessed element. Prepare a subheader and
				// then all data from elements of same type.
				type = temp_element.getType();
				temp_header = new String("GLOBAL_STRAINS_ 1 " + time
						+ temp_element.print_Gid(Element.RESULT_SUB_HEADER, 0));

				something_to_print = false;

				for (int cn = 0; cn < nr_of_clients; cn++) {
					if (cn == client_nr) {

						bw = this.open(true);
						temp_out = new String("");

						// Now, run through the array and try to find elements
						// of the same type to print results from
						for (j = i; j < elementlist.size(); j++) {
							temp_element = (Element) elementlist.elementAt(j);

							if (temp_element.getType().equals(type)
									&& !temp_element.isProcessed()) {

								temp_element.setProcessed(true);
								temp = new String("");

								// Loop through the integration points and print
								// the result from each one of them
								for (gpn = 0; gpn < temp_element
										.getNumberOfIntegrationPoints(); gpn++)
									temp += temp_element.print_Gid(
											Element.RESULT_STRAIN_GLOBAL, gpn);

								if (!temp.equals(""))
									something_to_print = true;
								if (temp_element.getCpu_number() == client_nr
										&& !temp_element.isDeActivated())
									temp_out += temp;

							}
						}

						if (something_to_print && client_nr == 0)
							bw.write(temp_header);

						bw.write(temp_out);

						bw.flush();
						bw.close();

					}

					barrier.sync();
				}
			}
		}

		for (i = 0; i < elementlist.size(); i++) {
			((Element) elementlist.elementAt(i)).setProcessed(false);
		}

		// finished!
	}

	/**
	 * This method checks that all mandatory parameters have been set
	 */
	public void checkIndata() throws IllegalArgumentException {
	}
}

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

import run.Constraint;
import run.Controlset;
import run.Element;
import run.Load;
import run.Material;
import run.Node;
import run.RplVector;
import run.Writer;

/**
 * Insert the type's description here. Creation date: (2001-10-31 00.44.39)
 * 
 * @author:
 */
public class FembicWriter extends Writer {
	private RplVector constraintlist;
	private Controlset controlset;
	private RplVector loadlist;
	private RplVector materiallist;

	/**
	 * FembicWriter constructor comment.
	 */
	public FembicWriter(RplVector nlist, RplVector elist) {
		super(nlist, elist);
	}

	public FembicWriter(RplVector nlist, RplVector elist, Controlset cset,
			RplVector clist, RplVector mlist, RplVector llist) {
		super(nlist, elist);
		constraintlist = clist;
		controlset = cset;
		loadlist = llist;
		materiallist = mlist;
	}

	/**
	 * Insert the method's description here. Creation date: (23/11/01 %T)
	 */
	private void open() {
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
	public void write(String fname, double currtime) throws java.io.IOException {
		int i;
		int j;
		int k;
		String type;
		Element temp_element;
		Node temp_node;
		Load temp_load;
		Constraint temp_constraint;
		Material temp_material;

		try {

			// Open a file to write to
			BufferedWriter bw = new BufferedWriter(new FileWriter(fname));

			bw.write("# This file has been translated into Fembic format\n");
			bw.write("# by Impact acting as a translator. \n");
			bw.write("# \n");

			// Start by writing all the nodes
			bw.write("\nNODES\n");

			for (k = 0; k < nodelist.size(); k++) {

				temp_node = (Node) nodelist.elementAt(k);

				if (temp_node.getNumber() >= 0) {

					bw.write(temp_node.getNumber() + " \t");
					bw.write(" x = " + temp_node.getX_pos_orig() + " \t");
					bw.write(" y = " + temp_node.getY_pos_orig() + " \t");
					bw.write(" z = " + temp_node.getZ_pos_orig() + " \t");

					if (temp_node.getConstraint() != null)
						bw.write(" constraint = "
								+ temp_node.getConstraint().getName() + "\t");

					if (temp_node.getLoad() != null)
						bw.write(" load = " + temp_node.getLoad().getName()
								+ "\t");

					bw.write("\n");

				}
			}

			bw.write("\n");

			// Time to write all the elements.
			// Loop through the element list and make each element of a new type
			// write its header

			for (i = 0; i < elementlist.size(); i++) {
				temp_element = (Element) elementlist.elementAt(i);

				if (!temp_element.isProcessed()) {

					// We have found an unprocessed element. Print a subheader
					// and then all data from elements of same type.
					type = temp_element.getType();
					bw.write("\nELEMENTS OF TYPE " + type);
					bw.write("\n");

					// Now, run through the array, print element data and try to
					// find elements of the same type and mark as processed

					for (j = i; j < elementlist.size(); j++) {
						temp_element = (Element) elementlist.elementAt(j);

						if ((temp_element.getType().equals(type))
								&& !temp_element.isProcessed()) {
							bw.write(temp_element.print_Fembic(Element.MESH, 0));
							temp_element.setProcessed(true);
						}
					}

				}
			}

			// Finished writing elements. Now set them as unprocessed.
			for (k = 0; k < elementlist.size(); k++) {
				((Element) elementlist.elementAt(k)).setProcessed(false);
			}

			// Start by writing all the loads

			if (loadlist.size() > 0) {
				bw.write("\nLOADS\n");

				for (k = 0; k < loadlist.size(); k++) {

					temp_load = (Load) loadlist.elementAt(k);

					bw.write(temp_load.getName() + " \t");
					bw.write(temp_load.print_Fembic(Element.MESH) + " \t");

				}

				bw.write("\n");
			}

			// Time to write all the constraints.
			// Loop through the constraint list and make each constraint of a
			// new type write its header

			if (constraintlist.size() > 0)
				for (i = 0; i < constraintlist.size(); i++) {
					temp_constraint = (Constraint) constraintlist.elementAt(i);

					if (!temp_constraint.isProcessed()) {

						// We have found an unprocessed element. Print a
						// subheader and then all data from elements of same
						// type.
						type = temp_constraint.getType();
						bw.write("\nCONSTRAINTS OF TYPE " + type);
						bw.write("\n");

						// Now, run through the array, print constraint data and
						// try to find constraints of the same type and mark as
						// processed

						for (j = i; j < constraintlist.size(); j++) {
							temp_constraint = (Constraint) constraintlist
									.elementAt(j);

							if ((temp_constraint.getType().equals(type))
									&& !temp_constraint.isProcessed()) {
								bw.write(temp_constraint
										.print_Fembic(Element.MESH));
								temp_constraint.setProcessed(true);
							}
						}

					}
				}

			// Finished writing elements. Now set them as unprocessed.
			for (k = 0; k < constraintlist.size(); k++) {
				((Constraint) constraintlist.elementAt(k)).setProcessed(false);
			}

			// Time to write all the materials.
			// Loop through the material list and make each material of a new
			// type write its header

			for (i = 0; i < materiallist.size(); i++) {
				temp_material = (Material) materiallist.elementAt(i);

				if (!temp_material.isProcessed()) {

					// We have found an unprocessed material. Print a subheader
					// and then all data from materials of same type.
					type = temp_material.getType();
					bw.write("\nMATERIALS OF TYPE " + type);
					bw.write("\n");

					// Now, run through the array, print material data and try
					// to find materials of the same type and mark as processed

					for (j = i; j < materiallist.size(); j++) {
						temp_material = (Material) materiallist.elementAt(j);

						if ((temp_material.getType().equals(type))
								&& !temp_material.isProcessed()) {
							bw.write(temp_material.print_Fembic(Element.MESH));
							temp_material.setProcessed(true);
						}
					}

				}
			}

			// Finished writing materials. Now set them as unprocessed.
			for (k = 0; k < materiallist.size(); k++) {
				((Material) materiallist.elementAt(k)).setProcessed(false);
			}

			// Write controlset
			bw.write("\nCONTROLS\n");
			bw.write(controlset.print_Fembic(Element.MESH));

			// finished! flush and close.
			bw.flush();
			bw.close();

		} catch (IOException ioe) {
			System.out.println(ioe);
		}

	}

	/**
	 * Dummy method. This will never happen. Used only for translator.
	 */
	public void writeParallel(String fname, double time, int[] indicies,
			Barrier barrier, int client_nr, int nr_of_clients)
			throws java.io.IOException {

	}

	/**
	 * This method checks that all mandatory parameters have been set
	 */
	public void checkIndata() throws IllegalArgumentException {
	}
}

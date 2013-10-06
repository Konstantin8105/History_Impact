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

import run.writers.GidWriter;
import uka.karmi.rmi.RemoteException;
import jp.lang.RemoteObject;
import jp.sync.Barrier;

/**
 * This object is the Writer. There can be several different writers derived
 * from this class, but they all have one thing in common. The purpouse is to
 * write output files from the simulation in a format decided by the class. A
 * subclass called GidWriter is supplied as default. This writes the output so
 * that it is readable from the GID pre- and postprocessor.
 * 
 * @author: Jonas Forssell, Yuriy Mikhaylovskiy.
 */
public abstract class Writer implements java.io.Serializable {
	protected RplVector elementlist;
	protected RplVector nodelist;
	protected boolean[] element_print_types;

	/**
	 * Writer constructor comment.
	 */
	public Writer(RplVector nlist, RplVector elist) {
		nodelist = nlist;
		elementlist = elist;
	}

	/**
	 * This method scans all writer types and returns an instance of that writer
	 * if it finds one. The reason for placing this method here is that as a
	 * programmer, adding a new writer will only involve changing the writer
	 * class and subclasses. No messing around in the other classes should be
	 * needed.
	 */
	public static Writer getWriterOfType_Fembic(String type,
			RplVector nodelist, RplVector elementlist,
			RemoteObject[] cluster_nodes)
			throws java.lang.IllegalArgumentException, RemoteException {
		if (type.toUpperCase().equals("GIDWRITER")) {
			return new GidWriter(nodelist, elementlist);
		}

		/*
		 * if (type.toUpperCase().equals("DYNAWRITER")) return new DynaWriter();
		 * if (type.toUpperCase().equals("RADIOSSWRITER")) return new
		 * RadiossWriter();
		 */
		throw new IllegalArgumentException("Illegal Element Type");
	}

	/**
	 * Insert the method's description here. Creation date: (07/11/01 %T)
	 */
	public abstract void write(String fname, double time)
			throws java.io.IOException;

	/**
	 * Insert the method's description here. Creation date: (07/11/01 %T)
	 */
	public abstract void writeParallel(String fname, double time,
			int[] indicies, Barrier barrier, int client_nr, int nr_of_clients)
			throws java.io.IOException, InterruptedException;

	public abstract void initialize();

	/**
	 * This method is used to check that all mandatory parameters have been set
	 */
	public abstract void checkIndata() throws IllegalArgumentException;
}

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

package run.trackwriters;

import java.io.*;

import run.RplVector;
import run.TrackWriter;
import run.Tracker;

/**
 * Insert the type's description here. Creation date: (2001-10-31 00.44.39)
 * 
 * @author:
 */
public class GidTrackWriter extends TrackWriter {
	private double time;
	private int counter;

	/**
	 * GidTrackWriter constructor comment.
	 */
	public GidTrackWriter(RplVector tlist) {
		super(tlist);
		counter = 0;
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
	public void write(double currtime) {
		try {
			writeResult(currtime);
			counter++;
		} catch (IOException ioe) {
			System.out.println("Error during Track Writing phase:" + ioe);

			return;
		}
	}

	/**
	 * This method writes the calculated results from the solutions. The amount
	 * of data to be written is decided by the controlobject. Creation date:
	 * (24/11/01 %T)
	 * 
	 * @exception java.io.IOException
	 *                The exception description.
	 */
	private void writeResult(double currtime) throws java.io.IOException {
		int i;
		Tracker temp_tracker;

		/*
		 * The Graph file that GID uses is a standard ASCII file. Every line of
		 * the file is a poit of the Graph with X and Y coordinates separated by
		 * a space. Comment lines are also allowed and should begin with a '#'.
		 * The title of the Graph and the labels for the X and Y axis can also
		 * be configured. If a comment line contains the keyword 'Graf:' the
		 * string between quotes that follows this keyword will be used as the
		 * title of the graph. The string between quotes the follows the keyword
		 * 'X:' will be used as label for the X axis. The same is also true for
		 * the Y axis but for the keyword 'Y:'
		 * 
		 * An example:
		 * 
		 * #" Graf: "Nodes 26, 27, .. Graf." # # X: "Time" Y: "Sxx Strains"
		 * 0.001 0.1234 0.002 0.1323 0.003 0.1412 # End
		 */

		// Loop through the tracker list and make each tracker write its data
		for (i = 0; i < trackerlist.size(); i++) {
			temp_tracker = (Tracker) trackerlist.elementAt(i);

			if (counter == 0) {
				temp_tracker.print_Gid(Tracker.RESULT_HEADER, currtime);
			} else {
				temp_tracker.print_Gid(Tracker.RESULT, currtime);
			}
		}
	}

	/**
	 * This method checks that all mandatory parameters have been set
	 */
	public void checkIndata() throws IllegalArgumentException {
	}
}

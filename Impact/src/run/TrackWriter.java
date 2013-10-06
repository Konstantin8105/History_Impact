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

import run.trackwriters.GidTrackWriter;

/**
 * This object is the TrackWriter. There can be several different trackwriters
 * derived from this class, but they all have one thing in common. The purpouse
 * is to write output files from the simulation in a format decided by the
 * Tracker class. The layout here is very similar to the Writer class which
 * creates output from the elements. A subclass called GidTrackWriter is
 * supplied as default. This writes the output so that it is readable from the
 * GID pre- and postprocessor. Creation date: (2002-09-20 23.59.42)
 * 
 * @author: Jonas Forssell, Yuriy Mikhaylovskiy.
 */
public abstract class TrackWriter implements java.io.Serializable {
	protected RplVector trackerlist;
	protected boolean[] tracker_print_types;

	/**
	 * TrackWriter constructor comment.
	 */
	public TrackWriter(RplVector tlist) {
		trackerlist = tlist;
	}

	public static TrackWriter getTrackWriterOfType_Fembic(String type,
			RplVector trackerlist) throws java.lang.IllegalArgumentException {
		if (type.toUpperCase().equals("GIDTRACKWRITER")) {
			return new GidTrackWriter(trackerlist);
		}

		/*
		 * if (type.toUpperCase().equals("DYNATRACKWRITER")) return new
		 * DynaTrackWriter(); if
		 * (type.toUpperCase().equals("RADIOSSTRACKWRITER")) return new
		 * RadiossTrackWriter();
		 */
		throw new IllegalArgumentException("Illegal TrackWriter Type");
	}

	/**
	 * This method is the used when the writing of tracker data is to be done.
	 * The current solution time is supplied. Creation date: (20/09/02 %T)
	 */
	public abstract void write(double time);

	/**
	 * This method is the used for any initialization. Creation date: (20/09/02
	 * %T)
	 */
	public abstract void initialize();

	/**
	 * This method is used to check that all mandatory parameters have been set
	 */
	public abstract void checkIndata() throws IllegalArgumentException;
}

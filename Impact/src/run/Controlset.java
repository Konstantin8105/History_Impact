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

import java.text.ParseException;

import uka.karmi.rmi.RemoteException;

/**
 * Insert the type's description here.
 * 
 * @author: Jonas Forssell, Yuriy Mikhaylovskiy
 */
public class Controlset implements java.io.Serializable {
	public static int RESTORE_SAVE_OFF = 0;
	public static int RESTORE_SAVE_ALL = 1;
	public static int RESTORE_SAVE_PREVIOUS = 2;
	private int restore_save = 0;
	private double starttime;
	private double endtime;
	private double printstep;
	private double printstep_tracker;
	private Variable timestep;
	private int counter;
	private int counter_tracker;
	private String writer;
	private String trackwriter;
	private boolean Run_is_set;
	private boolean Print_is_set;
	private boolean Track_Print_is_set;
	private boolean Restore_save_is_set;

	/**
	 * Controlset constructor comment.
	 */
	public Controlset() throws RemoteException {
		// Dummy for conventional versions (not cluster)
		counter = 0;
		counter_tracker = 0;

		// Default to autostep (timestep = 0)
		timestep = new Variable(0);

		// Default is a GidWriter
		writer = new String("GIDWRITER");

		// Default is also GidTrackWriter
		trackwriter = new String("GIDTRACKWRITER");
	}

	public int getRestoreSaveStatus() {
		return restore_save;
	}

	public void parse_Fembic(Token[] param, int lineno)
			throws java.text.ParseException {
		int i = 0;

		while (i < param.length) {
			if (param[i].getw().toUpperCase().equals("RUN")) {
				if (param[i + 1].getw().toUpperCase().equals("FROM")) {
					if (param[i + 2].is_a_number()) {
						starttime = param[i + 2].getn();

						if (param[i + 3].getw().toUpperCase().equals("TO")) {
							if (param[i + 4].is_a_number()) {
								endtime = param[i + 4].getn();
								i += 5;
								Run_is_set = true;

								if ((i + 1) < param.length) {
									if (param[i].getw().toUpperCase()
											.equals("STEP")) {
										if (param[i + 1].is_a_word()) {
											if (param[i + 1].getw().startsWith(
													"[")
													&& param[i + 1].getw()
															.endsWith("]")) {
												timestep = new Variable(
														param[i + 1]
																.getw()
																.substring(
																		1,
																		param[i + 1]
																				.getw()
																				.length() - 1));

												i += 2;

											} else {
												throw new ParseException(
														"Illegal parameter. Syntax: STEP [t1,ts1,t2,ts2,...] ",
														lineno);
											}
										} else if (param[i + 1].is_a_number()) {
											timestep = new Variable(
													param[i + 1].getn());
											i += 2;
										} else {
											throw new ParseException(
													"No number xx defined in RUN FROM .. TO .. STEP xx",
													lineno);
										}
									}
								}

								// No spec means autostep
							} else {
								throw new ParseException(
										"No number xx defined in RUN FROM .. TO xx",
										lineno);
							}
						}
					} else {
						throw new ParseException(
								"No number xx defined in RUN FROM xx TO ..",
								lineno);
					}
				}
			} else if (param[i].getw().toUpperCase().equals("RESTORE")) {
				if (param[i + 1].getw().toUpperCase().equals("SAVE")) {
					if (param[i + 2].getw().toUpperCase().equals("ALL")) {
						restore_save = RESTORE_SAVE_ALL;
						Restore_save_is_set = true;
						i += 3;
					} else if (param[i + 2].getw().toUpperCase().equals("OFF")) {
						restore_save = RESTORE_SAVE_OFF;
						Restore_save_is_set = true;
						i += 3;
					} else if (param[i + 2].getw().toUpperCase()
							.equals("PREVIOUS")) {
						restore_save = RESTORE_SAVE_PREVIOUS;
						Restore_save_is_set = true;
						i += 3;
					} else {
						throw new ParseException(
								"Missing parameter: OFF, PREVIOUS, ALL", lineno);
					}

				} else {
					throw new ParseException(
							"No recognized control command found: 'RESTORE SAVE' ...",
							lineno);
				}
			} else if (param[i].getw().toUpperCase().equals("PRINT")) {
				if (param[i + 1].getw().toUpperCase().equals("EVERY")) {
					if (param[i + 2].is_a_number()) {
						printstep = param[i + 2].getn();

						if (param[i + 3].getw().toUpperCase().equals("STEP")) {
							i += 4;
						}

						Print_is_set = true;
					} else {
						throw new ParseException(
								"No number xx defined in PRINT EVERY xx STEP",
								lineno);
					}
				} else if (param[i + 1].getw().toUpperCase().equals("TRACKER")) {
					if (param[i + 2].getw().toUpperCase().equals("EVERY")) {
						if (param[i + 3].is_a_number()) {
							printstep_tracker = param[i + 3].getn();

							if (param[i + 4].getw().toUpperCase()
									.equals("STEP")) {
								i += 5;
							}

							Track_Print_is_set = true;
						} else {
							throw new ParseException(
									"No number xx defined in PRINT TRACKER EVERY xx STEP",
									lineno);
						}
					}
				}
			} else if (param[i].getw().toUpperCase().equals("FOR")) {
				if (param[i + 1].getw().toUpperCase().equals("WRITER")) {
					if (param[i + 2].getw().toUpperCase().equals("USE")) {
						if (param[i + 3].is_a_word()) {
							writer = new String(param[i + 3].getw());
							i += 4;
						}
					}
				}

				else if (param[i + 1].getw().toUpperCase()
						.equals("TRACKWRITER")) {
					if (param[i + 2].getw().toUpperCase().equals("USE")) {
						if (param[i + 3].is_a_word()) {
							trackwriter = new String(param[i + 3].getw());
							i += 4;
						}
					}
				}
			} else {
				throw new java.text.ParseException(
						"No recognized control command found or missing parameter",
						lineno);
			}
		}
	}

	public void parse_Nastran(Token[] param, int lineno)
			throws java.text.ParseException {
	}

	/**
	 * This method is used to create the lines needed in the result file. The
	 * method returns a string which is printed directly. However, due to the
	 * fact that the line may be different depending on what is requested to be
	 * printed and that the number of methods should be kept down, the first
	 * parameter here is a control parameter. This parameter describes what
	 * should be printed. The second parameter is a required input when gauss
	 * point results are to be printed. Creation date: (09/12/01 %T)
	 * 
	 * @param ctrl
	 *            The control number to say if a header of result file is to be
	 *            printed.
	 * @param gpn
	 *            The gauss point number.
	 * 
	 * @return java.lang.String
	 */
	public String print_Fembic(int ctrl) {
		String out;

		switch (ctrl) {

		case Element.MESH:

			/* Print the element number and connected nodes */
			out = new String("");

			if (Run_is_set)
				out += "Run from " + starttime + " to " + endtime;

			if (timestep.value(0) != 0)
				out += " step " + timestep.printFembic();

			out += "\n";

			if (Print_is_set)
				out += "Print every " + printstep + " step \n";

			if (Track_Print_is_set)
				out += "Print tracker every " + printstep_tracker + " step \n";
			if (Restore_save_is_set)
				out += "Restore save "
						+ (restore_save == 0 ? "OFF"
								: restore_save == 1 ? "ALL" : "PREVIOUS")
						+ "\n";

			out += "For writer use " + writer + "\n";

			out += "For trackwriter use " + trackwriter + "\n";

			out += "\n";

			return out;

		default:
			return new String("");
		}
	}

	/**
	 * This method is used to check that all mandatory parameters have been set
	 */
	public void checkIndata() throws IllegalArgumentException {
		if (!Run_is_set) {
			throw new IllegalArgumentException(
					"No RUN FROM .. TO ..  defined in the controlset");
		}

		if (!Print_is_set) {
			throw new IllegalArgumentException(
					"No PRINT EVERY .. STEP defined in the controlset");
		}
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-17
	 * 01.14.04)
	 * 
	 * @return double
	 */
	public void setInitialConditions() throws IllegalArgumentException {
		if (!Track_Print_is_set) {
			printstep_tracker = printstep;
		}
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-17
	 * 01.14.04)
	 * 
	 * @return double
	 */
	public double getEndtime() {
		return endtime;
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-17
	 * 01.15.10)
	 * 
	 * @return double
	 */
	public double getPrintstep() {
		return printstep;
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-17
	 * 01.13.39)
	 * 
	 * @return double
	 */
	public double getStarttime() {
		return starttime;
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-17
	 * 01.17.17)
	 * 
	 * @return double
	 */
	public double getTimestep(double time) {
		if (timestep.on(time))
			return timestep.value(time);
		else
			return 0;
	}

	/**
	 * Returns the name of the writer
	 */
	public String getWriter() {
		return writer;
	}

	/**
	 * Returns the name of the trackwriter
	 */
	public String getTrackWriter() {
		return trackwriter;
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-17
	 * 01.14.04)
	 * 
	 * @param newEndtime
	 *            double
	 */
	public void setEndtime(double newEndtime) {
		endtime = newEndtime;
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-17
	 * 01.15.10)
	 * 
	 * @param newPrintstep
	 *            double
	 */
	public void setPrintstep(double newPrintstep) {
		printstep = newPrintstep;
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-17
	 * 01.13.39)
	 * 
	 * @param newStarttime
	 *            double
	 */
	public void setStarttime(double newStarttime) {
		starttime = newStarttime;
	}

	/**
	 * This method checks if the current_time is right for a print action
	 * (decided by printstep) Creation date: (2001-10-17 01.51.58)
	 * 
	 * @param current_time
	 *            double
	 */
	public boolean timeToPrint(double current_time) {
		if (current_time >= (printstep * counter)) {
			counter++;

			return true;
		} else {
			return false;
		}
	}

	/**
	 * This method checks if the current_time is right for a print action
	 * (decided by printstep) Creation date: (2001-10-17 01.51.58)
	 * 
	 * @param current_time
	 *            double
	 */
	public boolean timeToPrintTracker(double current_time) {
		if (current_time >= (printstep_tracker * counter_tracker)) {
			counter_tracker++;

			return true;
		} else {
			return false;
		}
	}
}

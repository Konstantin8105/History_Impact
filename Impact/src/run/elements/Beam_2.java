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

package run.elements;

import run.*;

// FILE: c:/temp/krockpackage/Beam.java
import java.util.*;

/**
 * A class that represents ...
 * 
 * @author Jonas Forssell, Yuriy Mikhaylovskiy
 * 
 * @see OtherClasses
 */
public class Beam_2 extends Element {
	private Material material;

	// Attributes
	// Associations
	// Operations

	/**
	 * Insert the method's description here. Creation date: (2001-08-10
	 * 19:44:35)
	 */
	public Beam_2() {
		super();
		type = new String("BEAM_2");
	}

	/**
	 * Insert the method's description here. Creation date: (25/12/01 %T)
	 */
	public void assembleMassMatrix() throws IllegalArgumentException {
	}

	/**
	 * Insert the method's description here. Creation date: (25/12/01 %T)
	 */
	public void calculateContactForces() {
	}

	/**
	 * Insert the method's description here. Creation date: (25/12/01 %T)
	 */
	public void calculateExternalForces(double currtime) {
	}

	/**
	 * This calculates and adds the element internal forces to the nodes. Note,
	 * by definition, the internal forces are subtracted from the external loads
	 * Thus, the "addition" is negative. Creation date: (2001-10-23 14.03.06)
	 * 
	 * @exception IllegalArgumentException
	 *                The exception description.
	 */
	public void calculateNodalForces(int integration_point, double timestep)
			throws IllegalArgumentException {
		// Rememeber negative addition
	}

	/**
	 * Insert the method's description here. Creation date: (25/12/01 %T)
	 * 
	 * @param tstep
	 *            double
	 */
	public void calculateStrain(double tstep, int integration_point) {
	}

	/**
	 * Insert the method's description here. Creation date: (25/12/01 %T)
	 */
	public void calculateStress(int integration_point, double timestep) {
	}

	/**
	 * Insert the method's description here. Creation date: (25/12/01 %T)
	 * 
	 * @param current_timestep
	 *            double
	 * 
	 * @return double
	 */
	public double checkTimestep(double current_timestep) {
		return 0;
	}

	/**
	 * This element only has one integration point (gauss point) Creation date:
	 * (26/12/01 %T)
	 * 
	 * @return int
	 */
	public int getNumberOfIntegrationPoints() {
		return 1;
	}

	/**
	 * Insert the method's description here. Creation date: (25/12/01 %T)
	 * 
	 * @param arg1
	 *            java.lang.String
	 * @param arg2
	 *            java.lang.String
	 * @param arg3
	 *            java.lang.String
	 * @param lineno
	 *            int
	 * @param nodelist
	 *            java.util.Vector
	 * @param materiallist
	 *            java.util.Vector
	 */
	public void parse_Fembic(Token[] param, int lineno, RplVector nodelist,
			RplVector materiallist, RplVector loadlist, Hashtable nodetable)
			throws java.text.ParseException {
	}

	/**
	 * Checks that all mandatory variables have been set
	 */
	public void checkIndata() throws IllegalArgumentException {
	}

	/**
	 * 
	 * The method checks destroyed element or not. If the element is destroyed
	 * variable failed = true else failed = false.
	 * 
	 */
	public void checkIfFailed() {
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
	 *            int
	 * @param gpn
	 *            int
	 * 
	 * @return java.lang.String
	 */
	public String print_Gid(int ctrl, int gpn) {
		return new String("");
	}

	public String print_Fembic(int ctrl, int gpn) {
		return new String("");
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public void setInitialConditions() {
		/*
		 * Make a local copy of the material object and keep it inside the
		 * element. The reason for this is twofold. 1. The material law is now
		 * tied to the element and will remember the element properties and
		 * history 2. Since the material is cloned now, after Initialize, all
		 * the parameters are defined in the material object and are now
		 * automatically transferred to the local copy.
		 */
		try {
			material = (Material) material.copy();
		} catch (CloneNotSupportedException e) {
			System.err.println("Object cannot clone");
		}

		// Now call any necessary initialisations in the law.
		material.setInitialConditions();
	}

	/**
	 * Insert the method's description here. Creation date: (25/12/01 %T)
	 */
	public void updateLocalCoordinateSystem() {
	}
}

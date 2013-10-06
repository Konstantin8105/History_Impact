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

import run.materials.*;

/**
 * A class that represents ...
 * 
 * @author Jonas Forssell, Yuriy Mikhaylovskiy
 * 
 * @see OtherClasses
 */
public abstract class Material implements Cloneable, java.io.Serializable {
	protected java.lang.String name;
	protected boolean name_is_set = false;
	protected double density, nu;
	protected boolean density_is_set = false;
	protected double youngs_modulus;
	protected boolean youngs_modulus_is_set = false;
	protected double failure_strain;
	protected double failure_stress;
	protected boolean failure_strain_is_set = false;
	protected boolean failure_stress_is_set = false;
	protected boolean processed;
	protected String type;

	// Associations

	/**
     *
     */
	/**
     *
     */

	// Operations

	/**
	 * An operation that does ...
	 */
	public Material() {
	}

	/**
	 * Insert the method's description here. Creation date: (13/12/01 %T)
	 * 
	 * @param stress
	 *            Jama.Matrix
	 * 
	 * @return Jama.Matrix
	 * 
	 */
	public abstract void calculateStressOneDimensional(Jama.Matrix strain,
			Jama.Matrix dstrain, Jama.Matrix stress, double timestep);

	/**
	 * Insert the method's description here. Creation date: (13/12/01 %T)
	 * 
	 * @param stress
	 *            Jama.Matrix
	 * 
	 * @return Jama.Matrix
	 * 
	 */
	public abstract void calculateStressThreeDimensional(Jama.Matrix strain,
			Jama.Matrix dstrain, Jama.Matrix stress, double timestep);

	/**
	 * Insert the method's description here. Creation date: (13/12/01 %T)
	 * 
	 * @param stress
	 *            Jama.Matrix
	 * 
	 * @return Jama.Matrix
	 * 
	 */
	public abstract void calculateStressTwoDimensionalPlaneStress(
			Jama.Matrix strain, Jama.Matrix dstrain, Jama.Matrix stress,
			double timestep);

	/**
	 * Insert the method's description here. Creation date: (16/12/01 %T)
	 * 
	 * @return krockpackage.Material
	 */
	public Object copy() throws CloneNotSupportedException {
		Object o = null;

		o = super.clone();
		/*
		 * try { } catch (CloneNotSupportedException e) { o = super.clone();
		 * System.err.println("Object cannot clone"); }
		 */
		return o;
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-17
	 * 02.59.24)
	 * 
	 * @return double
	 */
	public double getDensity() {
		return density;
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-17
	 * 02.59.24)
	 * 
	 * @return double
	 */
	public double getNu() {
		return nu;
	}

	/**
	 * Insert the method's description here. Creation date: (13/09/01 %T)
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getName() {
		return name;
	}

	/**
	 * This method is common for all materials and is used to get any arbitrary
	 * number from the argument string. Example of format: [1,45,34.2] The line
	 * xxxx.getNumber(3,stringexample) returns 34.2 Creation date: (13/09/01 %T)
	 * 
	 * @param nr
	 *            int
	 * @param arg
	 *            java.lang.String
	 * 
	 * @return double
	 * 
	 * @exception java.lang.IllegalArgumentException
	 *                The exception description.
	 */
	protected double getNumber(int nr, String arg)
			throws java.lang.IllegalArgumentException {
		int i;
		int index;
		int nextindex;
		double value;

		// Find the index of the separator
		index = 0;

		for (i = 0; i < nr; i++) {
			index = arg.indexOf(',', index + 1);
		}

		nextindex = arg.indexOf(',', index + 1);

		if (nextindex == -1) {
			nextindex = arg.length() - 1;
		}

		if (index == -1) {
			throw new IllegalArgumentException(
					"Incorrect amount of numbers defined");
		}

		// Ok, now read the number and convert it to double
		value = Double.valueOf(arg.substring(index + 1, nextindex))
				.doubleValue();

		return value;
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-23
	 * 14.00.07)
	 * 
	 * @return double
	 */
	public double getYoungsModulus() {
		return youngs_modulus;
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-23
	 * 14.00.07)
	 * 
	 * @return double
	 */
	public double getFailureStrain() {
		return failure_strain;
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-23
	 * 14.00.07)
	 * 
	 * @return double
	 */
	public boolean failureStrainIsSet() {
		return failure_strain_is_set;
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-23
	 * 14.00.07)
	 * 
	 * @return double
	 */
	public double getFailureStress() {
		return failure_stress;
	}

	/**
	 * Insert the method's description here. Creation date: (2001-10-23
	 * 14.00.07)
	 * 
	 * @return double
	 */
	public boolean failureStressIsSet() {
		return failure_stress_is_set;
	}

	/**
	 * This method returns the number of points defined in the argument string.
	 * This is determined from the amount of commas used in the string (brutal I
	 * know, but OK for now). Creation date: (13/09/01 %T)
	 * 
	 * @param nr
	 *            int
	 * @param arg
	 *            java.lang.String
	 * 
	 * @return double
	 * 
	 * @exception java.lang.IllegalArgumentException
	 *                The exception description.
	 */
	protected int numberOfPoints(String arg)
			throws java.lang.IllegalArgumentException {
		int i;
		int index;

		// Find the amount of commas (separators) in this string
		// From this assume that the amount of points defined are the same as
		// the amount of separators plus one divided by 2.
		// Example: [1,1,2,2,3,3] gives 5 separators +1 = 6 / 2 = 3
		index = 0;
		i = 0;

		do {
			index = arg.indexOf(',', index + 1);
			i++;
		} while (index != -1);

		i = i / 2;

		return i;
	}

	public static Material getMaterialOfType_Gmsh(int type)
			throws java.lang.IllegalArgumentException {

		// Currently, there is only one type of BC available in this translator

		return new Elastic();

	}

	public static Material getMaterialOfType_Nastran(String type)
			throws java.lang.IllegalArgumentException {

		if (type.toUpperCase().equals("MAT1")) {
			return new Elastic();
		}

		throw new IllegalArgumentException("Illegal Element Type");
	}

	public static Material getMaterialOfType_Fembic(String type)
			throws java.lang.IllegalArgumentException {

		if (type.toUpperCase().equals("ELASTIC")) {
			return new Elastic();
		}

		if (type.toUpperCase().equals("ELASTOPLASTIC")) {
			return new Elastoplastic();
		}
		if (type.toUpperCase().equals("THERMOELASTOPLASTIC")) {
			return new ThermoElastoplastic();
		}
		if (type.toUpperCase().equals("SPRING")) {
			return new SpringMaterial();
		}

		throw new IllegalArgumentException("Illegal Element Type");
	}

	/**
	 * Insert the method's description here. Creation date: (13/09/01 %T)
	 * 
	 * @param arg1
	 *            java.lang.String
	 * @param arg2
	 *            java.lang.String
	 * @param arg3
	 *            java.lang.String
	 * 
	 * @exception java.lang.IllegalArgumentException
	 *                The exception description.
	 */
	public abstract void parse_Fembic(Token[] arg, int lineno)
			throws java.text.ParseException;

	/**
	 * Insert the method's description here. Creation date: (13/09/01 %T)
	 * 
	 * @param arg1
	 *            java.lang.String
	 * @param arg2
	 *            java.lang.String
	 * @param arg3
	 *            java.lang.String
	 * 
	 * @exception java.lang.IllegalArgumentException
	 *                The exception description.
	 */
	public abstract void parse_Nastran(Token[] arg, int lineno)
			throws java.text.ParseException;

	/**
	 * Insert the method's description here. Creation date: (13/09/01 %T)
	 * 
	 * @param arg1
	 *            java.lang.String
	 * @param arg2
	 *            java.lang.String
	 * @param arg3
	 *            java.lang.String
	 * 
	 * @exception java.lang.IllegalArgumentException
	 *                The exception description.
	 */
	public abstract void parse_Gmsh(Token[] arg, int lineno)
			throws java.text.ParseException;

	public abstract String print_Fembic(int ctrl);

	/**
	 * This method is used to check that all mandatory parameters have been set
	 */
	public abstract void checkIndata() throws IllegalArgumentException;

	/**
	 * Insert the method's description here. Creation date: (16/12/01 %T)
	 */
	public abstract void setInitialConditions();

	/**
	 * Insert the method's description here. Creation date: (13/09/01 %T)
	 * 
	 * @param newName
	 *            java.lang.String
	 */
	public void setName(java.lang.String newName) {
		name = new String(newName);
		name_is_set = true;
	}

	/**
	 * Insert the method's description here. Creation date: (13/12/01 %T)
	 * 
	 * @param stress
	 *            Jama.Matrix
	 * 
	 * @return Jama.Matrix
	 * 
	 */
	public abstract double wavespeedOneDimensional(double param, double param2);

	/**
	 * Insert the method's description here. Creation date: (13/12/01 %T)
	 * 
	 * @param stress
	 *            Jama.Matrix
	 * 
	 * @return Jama.Matrix
	 * 
	 */
	public abstract double wavespeedThreeDimensional(double param, double param2);

	/**
	 * Insert the method's description here. Creation date: (13/12/01 %T)
	 * 
	 * @param stress
	 *            Jama.Matrix
	 * 
	 * @return Jama.Matrix
	 * 
	 */
	public abstract double wavespeedTwoDimensional(double param, double param2);

	/**
	 * Returns the processed.
	 * 
	 * @return boolean
	 */
	public boolean isProcessed() {
		return processed;
	}

	/**
	 * Sets the processed.
	 * 
	 * @param processed
	 *            The processed to set
	 */
	public void setProcessed(boolean processed) {
		this.processed = processed;
	}

	/**
	 * Returns the type.
	 * 
	 * @return String
	 */
	public String getType() {
		return type;
	}

}

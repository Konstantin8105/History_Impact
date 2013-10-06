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

package run.materials;

import run.*;

/**
 * Insert the type's description here. Creation date: (09/09/01 %T)
 * 
 * @author: Jonas Forssell
 */
public class Elastic extends Material implements Cloneable {
	private Jama.Matrix new_stress;
	private boolean nu_initialized = false;
	private boolean youngs_modulus_initialized = false;
	private Jama.Matrix stiffness_matrix_3d;
	private Jama.Matrix stiffness_matrix_plane_stress;
	private boolean E_is_set;
	private boolean NU_is_set;

	/**
	 * This material law calculates stresses based on strain, strain increase
	 * and old stress as input. Since this material law is isotropic and purly
	 * elastic, the history is not important. Therefore, the old stresses are
	 * never used, but are kept here for consistency. The constitutive matrices
	 * are defined in the setInitialConditions method.
	 */
	public Elastic() {
		type = new String("ELASTIC");
		new_stress = new Jama.Matrix(6, 1);
		stiffness_matrix_3d = new Jama.Matrix(6, 6);
		stiffness_matrix_plane_stress = new Jama.Matrix(6, 6);
	}

	/**
	 * This method calculates a stress based on a given strain, strain increase
	 * and stress for an element This is the default input for a material law.
	 * In this case, the job is simple. Just to multiply with youngs modulus and
	 * then we are OK. Note that the incoming objects are modified. No return on
	 * the method is needed. All Matrices are assumed to be of the standard 6x1
	 * type: strain = [epsilonx, epsilony, epsilonz, gammaxy, gammayz, gammazx]T
	 * dstrain = same format as strain but latest increment stress = [sigmax,
	 * sigmay, sigmaz, tauxy, tauyz, tauzx]T Creation date: (17/12/01 %T)
	 * 
	 * @param strain
	 *            double
	 */
	public void calculateStressOneDimensional(Jama.Matrix strain,
			Jama.Matrix dstrain, Jama.Matrix stress, double timestep) {
		strain.plusEquals(dstrain);

		// Update only one value in stress, since this is one dimensional
		stress.set(0, 0, strain.get(0, 0) * youngs_modulus);
	}

	/**
	 * This method calculates a stress based on a given strain, strain increase
	 * and stress for an element This is the default input for a material law.
	 * In this case, the job is simple. Just to multiply with the constitutive
	 * matrix and then we are OK. Note that the incoming objects are modified.
	 * No return on the method is needed. All Matrices are assumed to be of the
	 * standard 6x1 type: strain = [epsilonx, epsilony, epsilonz, gammaxy,
	 * gammayz, gammazx]T dstrain = same format as strain but latest increment
	 * stress = [sigmax, sigmay, sigmaz, tauxy, tauyz, tauzx]T Creation date:
	 * (13/12/01 %T)
	 * 
	 * @param stress
	 *            Jama.Matrix
	 * 
	 * @return Jama.Matrix
	 * 
	 */
	public void calculateStressThreeDimensional(Jama.Matrix strain,
			Jama.Matrix dstrain, Jama.Matrix stress, double timestep) {
		strain.plusEquals(dstrain);
		stress.setMatrix(0, 5, 0, 0, stiffness_matrix_3d.times(strain));
	}

	/**
	 * This method calculates a stress based on a given strain, strain increase
	 * and stress for an element This is the default input for a material law.
	 * In this case, the job is simple. Just to multiply with the constitutive
	 * matrix and then we are OK. Note that the incoming objects are modified.
	 * No return on the method is needed. All Matrices are assumed to be of the
	 * standard 6x1 type: strain = [epsilonx, epsilony, epsilonz, gammaxy,
	 * gammayz, gammazx]T dstrain = same format as strain but latest increment
	 * stress = [sigmax, sigmay, sigmaz, tauxy, tauyz, tauzx]T Creation date:
	 * (13/12/01 %T)
	 * 
	 * @param stress
	 *            Jama.Matrix
	 * 
	 * @return Jama.Matrix
	 * 
	 */
	public void calculateStressTwoDimensionalPlaneStress(Jama.Matrix strain,
			Jama.Matrix dstrain, Jama.Matrix stress, double timestep) {
		/*
		 * This is the simple and clean way of calculating the stress
		 * stress.setMatrix
		 * (0,5,0,0,stiffness_matrix_plane_stress.times(strain));
		 * 
		 * but there are many zeroes there, and speed by can be improved 37% by
		 * just multiplying the terms that are nonzero, so here it is: stress =
		 * stress + dstress
		 */
		stress.set(
				0,
				0,
				stress.get(0, 0)
						+ (stiffness_matrix_plane_stress.get(0, 0) * dstrain
								.get(0, 0))
						+ (stiffness_matrix_plane_stress.get(0, 1) * dstrain
								.get(1, 0)));
		stress.set(
				1,
				0,
				stress.get(1, 0)
						+ (stiffness_matrix_plane_stress.get(1, 0) * dstrain
								.get(0, 0))
						+ (stiffness_matrix_plane_stress.get(1, 1) * dstrain
								.get(1, 0)));
		stress.set(
				3,
				0,
				stress.get(3, 0)
						+ (stiffness_matrix_plane_stress.get(3, 3) * dstrain
								.get(3, 0)));
		stress.set(
				4,
				0,
				stress.get(4, 0)
						+ (stiffness_matrix_plane_stress.get(4, 4) * dstrain
								.get(4, 0)));
		stress.set(
				5,
				0,
				stress.get(5, 0)
						+ (stiffness_matrix_plane_stress.get(5, 5) * dstrain
								.get(5, 0)));

		// To predict thinning of element, update the strain increment in
		// thickness direction as an isotropic
		dstrain.set(2, 0,
				-(nu / (1 - nu)) * (dstrain.get(0, 0) + dstrain.get(1, 0)));

		// Update the strain
		strain.plusEquals(dstrain);
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
	public void parse_Fembic(Token[] arg, int lineno)
			throws java.text.ParseException {
		// The Youngs modulus of the material is defined
		int i = 0;

		while (i < arg.length) {
			if (arg[i].getw().toUpperCase().equals("E")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in arg3. Set this in
				// the element
				youngs_modulus = arg[i + 2].getn();
				youngs_modulus_initialized = true;
				i += 3;
				E_is_set = true;
			} else
			// The density of the material is defined
			if (arg[i].getw().toUpperCase().equals("RHO")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in arg3. Set this in
				// the element
				density = arg[i + 2].getn();
				density_is_set = true;
				i += 3;
			} else
			// Poissons constant of the material is defined
			if (arg[i].getw().toUpperCase().equals("NU")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in arg3. Set this in
				// the element
				nu = arg[i + 2].getn();
				nu_initialized = true;
				i += 3;
				NU_is_set = true;
			} else
			// Failure strain for the material is set
			if (arg[i].getw().toUpperCase().equals("FAILURE_STRAIN")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				failure_strain = arg[i + 2].getn();
				i += 3;
				failure_strain_is_set = true;
			} else
			// Failure strain for the material is set
			if (arg[i].getw().toUpperCase().equals("FAILURE_STRESS")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				failure_stress = arg[i + 2].getn();
				i += 3;
				failure_stress_is_set = true;
			} else {
				throw new java.text.ParseException(
						"Unknown material parameter in line ", lineno);
			}
		}
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
	public void parse_Nastran(Token[] param, int lineno)
			throws java.text.ParseException {
		int i = 0;
		int index;
		Constraint temp_constraint;
		Load temp_load;

		if (param[1].is_a_number()) {
			name = new String("Mat_" + (int) param[1].getn());
			name_is_set = true;
		} else
			throw new java.text.ParseException(
					"Illegal identification number of MAT1: " + param[1].getn(),
					lineno);

		if (param[2].is_a_number()) {
			youngs_modulus = param[1].getn();
			E_is_set = true;
			youngs_modulus_initialized = true;

		} else {
			youngs_modulus = 2 * (1 + param[4].getn()) * param[3].getn();
			E_is_set = true;
			youngs_modulus_initialized = true;

		}

		if (param[4].is_a_number()) {
			nu = param[4].getn();
			NU_is_set = true;
			nu_initialized = true;

		} else {
			nu = youngs_modulus / (2 * param[3].getn()) - 1;
			NU_is_set = true;
			nu_initialized = true;
		}

		if (param[5].is_a_number()) {
			density = param[5].getn();
			density_is_set = true;

		} else
			throw new java.text.ParseException("Illegal density for MAT1: "
					+ param[1].getn(), lineno);

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
	public void parse_Gmsh(Token[] param, int lineno)
			throws java.text.ParseException {
		int index;

		if (param[2].is_a_number()) {
			// Extract number of material
			index = (int) (param[2].getn() / 1E3);
			index %= 100;

			name = new String("MAT_" + index);
			name_is_set = true;
		} else
			throw new java.text.ParseException(
					"Illegal identification number of material "
							+ param[1].getn(), lineno);

		// Hard code material propoerties
		youngs_modulus = 210;
		E_is_set = true;
		youngs_modulus_initialized = true;

		nu = 0.3;
		NU_is_set = true;
		nu_initialized = true;

		density = 7.8e-6;
		density_is_set = true;

	}

	/**
	 * Checks that all mandatory variables have been set
	 */
	public void checkIndata() throws IllegalArgumentException {
		if (!E_is_set) {
			throw new IllegalArgumentException(
					"No Youngs modulus defined for this material: " + name);
		}

		if (!density_is_set) {
			throw new IllegalArgumentException(
					"No Density defined for this material: " + name);
		}

		if (!NU_is_set) {
			throw new IllegalArgumentException(
					"No Poissons_constant defined for this material: " + name);
		}
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
	 * 
	 * @return java.lang.String
	 */
	public String print_Fembic(int ctrl) {
		String out;

		switch (ctrl) {

		case Element.MESH:

			/* Print the element number and connected nodes */
			out = new String(name + "\tE = " + youngs_modulus + "\tRHO = "
					+ density + "\tNU = " + nu);

			if (failure_strain_is_set)
				out += " failure_strain = " + failure_strain;

			if (failure_stress_is_set)
				out += " failure_stress = " + failure_stress;

			out += "\n";

			return out;

		default:
			return new String("");
		}
	}

	/**
	 * Insert the method's description here. Creation date: (16/12/01 %T)
	 */
	public void setInitialConditions() {
		double c;
		double c2;
		double G;

		// Check all indata is there
		if (!density_is_set) {
			throw new IllegalArgumentException(
					"Material density not intialized");
		} else if (!name_is_set) {
			throw new IllegalArgumentException("Material name not intialized");
		} else if (!nu_initialized) {
			throw new IllegalArgumentException(
					"Material Poisson constant (nu) not intialized");
		} else if (!youngs_modulus_initialized) {
			throw new IllegalArgumentException(
					"Material youngs_modulus not intialized");
		}

		// Initialize all the stiffness matrices
		c = youngs_modulus / ((1.0 + nu) * (1.0 - (2.0 * nu)));
		c2 = youngs_modulus / (1.0 - (nu * nu));
		G = youngs_modulus / (2.0 * (1.0 + nu));

		//
		stiffness_matrix_3d.set(0, 0, (1.0 - nu) * c);
		stiffness_matrix_3d.set(0, 1, nu * c);
		stiffness_matrix_3d.set(0, 2, nu * c);
		stiffness_matrix_3d.set(0, 3, 0);
		stiffness_matrix_3d.set(0, 4, 0);
		stiffness_matrix_3d.set(0, 5, 0);

		//
		stiffness_matrix_3d.set(1, 0, nu * c);
		stiffness_matrix_3d.set(1, 1, (1.0 - nu) * c);
		stiffness_matrix_3d.set(1, 2, nu * c);
		stiffness_matrix_3d.set(1, 3, 0);
		stiffness_matrix_3d.set(1, 4, 0);
		stiffness_matrix_3d.set(1, 5, 0);

		//
		stiffness_matrix_3d.set(2, 0, nu * c);
		stiffness_matrix_3d.set(2, 1, nu * c);
		stiffness_matrix_3d.set(2, 2, (1.0 - nu) * c);
		stiffness_matrix_3d.set(2, 3, 0);
		stiffness_matrix_3d.set(2, 4, 0);
		stiffness_matrix_3d.set(2, 5, 0);

		//
		stiffness_matrix_3d.set(3, 0, 0);
		stiffness_matrix_3d.set(3, 1, 0);
		stiffness_matrix_3d.set(3, 2, 0);
		stiffness_matrix_3d.set(3, 3, G / 2.0);
		stiffness_matrix_3d.set(3, 4, 0);
		stiffness_matrix_3d.set(3, 5, 0);

		//
		stiffness_matrix_3d.set(4, 0, 0);
		stiffness_matrix_3d.set(4, 1, 0);
		stiffness_matrix_3d.set(4, 2, 0);
		stiffness_matrix_3d.set(4, 3, 0);
		stiffness_matrix_3d.set(4, 4, G / 2.0);
		stiffness_matrix_3d.set(4, 5, 0);

		//
		stiffness_matrix_3d.set(5, 0, 0);
		stiffness_matrix_3d.set(5, 1, 0);
		stiffness_matrix_3d.set(5, 2, 0);
		stiffness_matrix_3d.set(5, 3, 0);
		stiffness_matrix_3d.set(5, 4, 0);
		stiffness_matrix_3d.set(5, 5, G / 2.0);

		// Plane stress matrix
		stiffness_matrix_plane_stress.set(0, 0, c2);
		stiffness_matrix_plane_stress.set(0, 1, nu * c2);
		stiffness_matrix_plane_stress.set(0, 2, 0);
		stiffness_matrix_plane_stress.set(0, 3, 0);
		stiffness_matrix_plane_stress.set(0, 4, 0);
		stiffness_matrix_plane_stress.set(0, 5, 0);

		//
		stiffness_matrix_plane_stress.set(1, 0, nu * c2);
		stiffness_matrix_plane_stress.set(1, 1, c2);
		stiffness_matrix_plane_stress.set(1, 2, 0);
		stiffness_matrix_plane_stress.set(1, 3, 0);
		stiffness_matrix_plane_stress.set(1, 4, 0);
		stiffness_matrix_plane_stress.set(1, 5, 0);

		//
		stiffness_matrix_plane_stress.set(2, 0, 0);
		stiffness_matrix_plane_stress.set(2, 1, 0);
		stiffness_matrix_plane_stress.set(2, 2, 0);
		stiffness_matrix_plane_stress.set(2, 3, 0);
		stiffness_matrix_plane_stress.set(2, 4, 0);
		stiffness_matrix_plane_stress.set(2, 5, 0);

		//
		stiffness_matrix_plane_stress.set(3, 0, 0);
		stiffness_matrix_plane_stress.set(3, 1, 0);
		stiffness_matrix_plane_stress.set(3, 2, 0);
		stiffness_matrix_plane_stress.set(3, 3, ((1 - nu) * c2) / 2);
		stiffness_matrix_plane_stress.set(3, 4, 0);
		stiffness_matrix_plane_stress.set(3, 5, 0);

		//
		stiffness_matrix_plane_stress.set(4, 0, 0);
		stiffness_matrix_plane_stress.set(4, 1, 0);
		stiffness_matrix_plane_stress.set(4, 2, 0);
		stiffness_matrix_plane_stress.set(4, 3, 0);
		stiffness_matrix_plane_stress.set(4, 4,
				((5.0 / 6.0) * (1 - nu) * c2) / 2);
		stiffness_matrix_plane_stress.set(4, 5, 0);

		//
		stiffness_matrix_plane_stress.set(5, 0, 0);
		stiffness_matrix_plane_stress.set(5, 1, 0);
		stiffness_matrix_plane_stress.set(5, 2, 0);
		stiffness_matrix_plane_stress.set(5, 3, 0);
		stiffness_matrix_plane_stress.set(5, 4, 0);
		stiffness_matrix_plane_stress.set(5, 5,
				((5.0 / 6.0) * (1 - nu) * c2) / 2);
	}

	/**
	 * Insert the method's description here. Creation date: (06/01/02 %T)
	 * 
	 * @return double
	 */
	public double wavespeedOneDimensional(double param, double param2) {
		return Math.sqrt(youngs_modulus / density);
	}

	/**
	 * Insert the method's description here. Creation date: (06/01/02 %T)
	 * 
	 * @return double
	 */
	public double wavespeedThreeDimensional(double param, double param2) {
		return Math.sqrt((youngs_modulus * (1 - nu))
				/ (density * (1 + nu) * (1 - (2 * nu))));
	}

	/**
	 * Insert the method's description here. Creation date: (06/01/02 %T)
	 * 
	 * @return double
	 */
	public double wavespeedTwoDimensional(double param, double param2) {
		return Math.sqrt(youngs_modulus / (density * (1 - (nu * nu))));
	}
}

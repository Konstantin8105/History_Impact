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
 * This is an isoparametric thermo-elasto-plastic material law. The elastic
 * Young's modulus defines the stress-strain relation up to the yield stress .
 * Above yield stress, there are several options. The plastic behaviour can be
 * described by the plastic modulus (EP) which defines a linear relation between
 * the stress and effective plastic strain. This relation can also be a curve,
 * defined by a range of stress/strain coordinates. The EP in this case has no
 * function and can be omitted. Finally, the relation can also be dependent on
 * the strain rate in which several stress/strain curves are defined together
 * with a parameter setting the velocity for which each curve is representative.
 * The stress for a certain effective strain value is detemined as a linear
 * interpolation from these curves.
 * 
 * @author: Yuriy Mikhaylovskiy
 */
public class ThermoElastoplastic extends Material {
	private Variable yield_stress;
	private Variable[] yield_array;
	private double[] V;
	private double factor;
	private int Y_is_set;
	private int V_is_set;
	private Jama.Matrix stiffness_matrix_3d;
	private Jama.Matrix stiffness_matrix_plane_stress;
	private Jama.Matrix trial_stress;
	private double eps;
	private double eps_vel;
	private Jama.Matrix A;
	private Jama.Matrix I;
	private boolean E_is_set;
	private boolean NU_is_set;
	private boolean RHO_is_set;
	private boolean YIELD_is_set;
	private boolean EP_is_set;
	private boolean EP_is_required;
	private boolean TOL_is_set;
	private boolean thermal_expantion_is_set;
	private boolean temperature_is_set;
	private double thermal_expantion;
	private double temperature;
	private boolean isInitialTemperature;

	/**
	 * ThermoElastoplastic constructor comment.
	 */
	public ThermoElastoplastic() {
		type = new String("THERMOELASTOPLASTIC");
		trial_stress = new Jama.Matrix(6, 1);
		stiffness_matrix_3d = new Jama.Matrix(6, 6);
		stiffness_matrix_plane_stress = new Jama.Matrix(6, 6);
		A = new Jama.Matrix(6, 6);
		I = new Jama.Matrix(6, 6);
		eps = 0;
		eps_vel = 0;
		thermal_expantion = 0;
		temperature = 0;
		isInitialTemperature = false;

		// Note also special constructurs in the check indata method
	}

	/**
	 * This method updates the stress and strain given the strain, strain
	 * increase and stress It is quite simple since it is one-dimensional.
	 * 
	 * @param strain
	 *            double
	 * 
	 * @return double
	 * 
	 */
	public void calculateStressOneDimensional(Jama.Matrix strain,
			Jama.Matrix dstrain, Jama.Matrix stress, double timestep) {
		double yield_function;
		double strain_vel;

		if (!isInitialTemperature) {
			strain.set(0, 0, -thermal_expantion * temperature);
			isInitialTemperature = !isInitialTemperature;
		}

		// Update the strain
		strain.plusEquals(dstrain);

		// Calculate trail stress (let that be default)
		stress.set(
		// 0, 0, stress.get(0, 0) + (youngs_modulus * dstrain.get(0, 0))
				0, 0, youngs_modulus * strain.get(0, 0));

		// Calculate yield function
		yield_function = Math.abs(stress.get(0, 0))
				- this.yieldStress(eps, eps_vel);

		// Check if above yield stress
		if (yield_function > 0) {
			// Update effective plastic strain
			eps_vel = Math.abs(dstrain.get(0, 0) / timestep);
			eps += Math.abs(dstrain.get(0, 0));

			// Update the stress. The effective strain is the same as epsilonxx
			// since it is one-dimensional.
			stress.set(0, 0, yieldStress(eps, eps_vel));
		}
	}

	/**
	 * This method calculates a new stress based on a given strain, strain
	 * increase and old stress matrix for an element Note that the strain is not
	 * corrected! This method works only if the material object has a "memory"
	 * since values from the previous timestep of the effective plastic strain
	 * is used in the function. The previous timestep means the previous time
	 * that this method was called. Creation date: (13/12/01 %T)
	 * 
	 * @param stress
	 *            Jama.Matrix
	 * 
	 * @return Jama.Matrix
	 * 
	 */
	public void calculateStressThreeDimensional(Jama.Matrix strain,
			Jama.Matrix dstrain, Jama.Matrix stress, double timestep) {
		double pressure;
		double vm_stress;

		if (!isInitialTemperature) {
			Jama.Matrix dstrain_tmp = new Jama.Matrix(6, 1);
			for (int i = 0; i < 3; i++)
				dstrain_tmp.set(i, 0, -thermal_expantion * temperature);
			for (int i = 3; i < 6; i++)
				dstrain_tmp.set(i, 0, 0);
			stress.plusEquals(stiffness_matrix_3d.times(dstrain_tmp));
			/* update the strain */
			strain.plusEquals(dstrain_tmp);
			isInitialTemperature = !isInitialTemperature;
		}

		// Determine the stress increase elastically to get the trial stress and
		// put it back in the stress matrix
		stress.plusEquals(stiffness_matrix_3d.times(dstrain));

		// Calculate the deviatoric stresses (note the sign of pressure!)
		pressure = -(stress.get(0, 0) + stress.get(1, 0) + stress.get(2, 0)) / 3.0;
		stress.set(0, 0, stress.get(0, 0) + pressure);
		stress.set(1, 0, stress.get(1, 0) + pressure);
		stress.set(2, 0, stress.get(2, 0) + pressure);

		// Determine yield function
		vm_stress = Math
				.sqrt(
				// ((3.0 / 2.0) * ((stress.get(0, 0) * stress.get(0, 0)) +
				(1.5 * ((stress.get(0, 0) * stress.get(0, 0))
						+ (stress.get(1, 0) * stress.get(1, 0)) + (stress.get(
						2, 0) * stress.get(2, 0))))
						+ (3.0 * ((stress.get(3, 0) * stress.get(3, 0))
								+ (stress.get(4, 0) * stress.get(4, 0)) + (stress
								.get(5, 0) * stress.get(5, 0)))));

		// Now check if the new stress estimation is above the yield surface and
		// needs correction
		if (vm_stress > yieldStress(eps, eps_vel)) {
			// Compute plastic strain increment and increment eps...
			eps_vel = ((vm_stress - yieldStress(eps, eps_vel)) / (((3.0 * youngs_modulus) / (2.0 * (1.0 + nu))) + yieldStressDerivate(
					eps, eps_vel))) / timestep;
			eps += (eps_vel * timestep);

			// ... and Scale back the estemated trial stress to the yield
			// surface
			stress.timesEquals(yieldStress(eps, eps_vel) / vm_stress);
		}

		// Since this method works directly onto the matrices given, there is
		// nothing to return.
		// They are already changed. However, the stress must be converted back
		// from deviatoric format.
		stress.set(0, 0, stress.get(0, 0) - pressure);
		stress.set(1, 0, stress.get(1, 0) - pressure);
		stress.set(2, 0, stress.get(2, 0) - pressure);
		/* Finally, update the strain */
		strain.plusEquals(dstrain);
	}

	/**
	 * This method calculates a stress based on a given strain, strain increase
	 * and stress for an element This is the default input for a material law.
	 * Note that the incoming objects are modified. No return on the method is
	 * needed. All Matrices are assumed to be of the standard 6x1 type: strain =
	 * [epsilonx, epsilony, epsilonz, gammaxy, gammayz, gammazx]T dstrain = same
	 * format as strain but latest increment stress = [sigmax, sigmay, sigmaz,
	 * tauxy, tauyz, tauzx]T Creation date: (13/12/01 %T)
	 * 
	 * @param stress
	 *            Jama.Matrix
	 * 
	 * @return Jama.Matrix
	 * 
	 */
	public void calculateStressTwoDimensionalPlaneStress(Jama.Matrix strain,
			Jama.Matrix dstrain, Jama.Matrix stress, double timestep) {
		double vm_stress;
		if (!isInitialTemperature) {
			stress.set(0, 0,
					stress.get(0, 0)
							+ (stiffness_matrix_plane_stress.get(0, 0)
									* -thermal_expantion * temperature)
							+ (stiffness_matrix_plane_stress.get(0, 1)
									* -thermal_expantion * temperature));
			stress.set(1, 0,
					stress.get(1, 0)
							+ (stiffness_matrix_plane_stress.get(1, 0)
									* -thermal_expantion * temperature)
							+ (stiffness_matrix_plane_stress.get(1, 1)
									* -thermal_expantion * temperature));
			isInitialTemperature = !isInitialTemperature;
		}

		/*
		 * Determine the stress increase elastically to get the trial stress and
		 * put it back in the stress matrix This is the simple and clean way of
		 * writing it:
		 * stress.plusEquals(stiffness_matrix_plane_stress.times(dstrain)); But
		 * for improved speed, we only multiply the terms that are non-zero
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

		// Determine yield function
		vm_stress = Math.sqrt(((stress.get(0, 0) * stress.get(0, 0)) + (stress
				.get(1, 0) * stress.get(1, 0)))
				- (stress.get(0, 0) * stress.get(1, 0))
				+ (3.0 * (stress.get(3, 0) * stress.get(3, 0))));

		// Now check if the new stress estimation is above the yield surface and
		// needs correction
		if (vm_stress > yieldStress(eps, eps_vel)) {
			// Compute plastic strain increment and increment eps...
			eps_vel = (vm_stress - yieldStress(eps, eps_vel))
					/ (youngs_modulus * timestep);
			eps += (eps_vel * timestep);

			// ... and Scale back the estemated trial stress to the yield
			// surface
			stress.timesEquals(yieldStress(eps, eps_vel) / vm_stress);

			// The strain increment in thickness direction is in this case just
			// to keep the volume constant.
			dstrain.set(2, 0, -(dstrain.get(0, 0) + dstrain.get(1, 0)));
		} else {
			// If elastic, update the strain increment in thickness direction
			// elastically
			dstrain.set(2, 0,
					-(nu / (1.0 - nu))
							* (dstrain.get(0, 0) + dstrain.get(1, 0)));
		}

		/* Finally, update the strain */
		strain.plusEquals(dstrain);

		// Since this method works directly onto the matrices given, there is
		// nothing to return.
		// They are already changed.
	}

	/**
	 * Insert the method's description here. Creation date: (13/12/01 %T)
	 * 
	 * @param arg1
	 *            java.lang.String
	 * @param arg2
	 *            java.lang.String
	 * @param arg3
	 *            java.lang.String
	 * @param lineno
	 *            int
	 * 
	 * @exception java.lang.IllegalArgumentException
	 *                The exception description.
	 */
	public void parse_Fembic(Token[] arg, int lineno)
			throws java.text.ParseException {
		int i = 0;

		while (i < arg.length) {
			// The Youngs modulus of the material is defined
			if (arg[i].getw().toUpperCase().equals("E")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in arg3. Set this in
				// the element
				youngs_modulus = arg[i + 2].getn();
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
				RHO_is_set = true;
			} else
			// Poissons ratio of the material is defined
			if (arg[i].getw().toUpperCase().equals("NU")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in arg3. Set this in
				// the element
				nu = arg[i + 2].getn();
				i += 3;
				NU_is_set = true;
			} else

			// Thermal expantion
			if (arg[i].getw().toUpperCase().equals("A")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in arg3. Set this in
				// the element
				thermal_expantion = arg[i + 2].getn();
				i += 3;
				thermal_expantion_is_set = true;
			} else

			// Temperature
			if (arg[i].getw().toUpperCase().equals("T")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in arg3. Set this in
				// the element
				temperature = arg[i + 2].getn();
				i += 3;
				temperature_is_set = true;
			} else

			// Poissons constant of the material is defined
			if (arg[i].getw().toUpperCase().equals("FAILURE_STRAIN")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in arg3. Set this in
				// the element
				failure_strain = arg[i + 2].getn();
				i += 3;
				failure_strain_is_set = true;
			} else
			// Poissons constant of the material is defined
			if (arg[i].getw().toUpperCase().equals("FAILURE_STRESS")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in arg3. Set this in
				// the element
				failure_stress = arg[i + 2].getn();
				i += 3;
				failure_stress_is_set = true;
			} else if (arg[i].getw().toUpperCase().equals("V1")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in arg3. Set this in
				// the element
				this.check_array();

				if (arg[i + 2].is_a_number()) {
					V[1] = arg[i + 2].getn();
				} else {
					throw new java.text.ParseException(
							"Unknown V1 parameter in line ", lineno);
				}

				V_is_set = ((V_is_set < 1) ? 1 : V_is_set);
				i += 3;
			} else if (arg[i].getw().toUpperCase().equals("V2")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in arg3. Set this in
				// the element
				this.check_array();

				if (arg[i + 2].is_a_number()) {
					V[2] = arg[i + 2].getn();
				} else {
					throw new java.text.ParseException(
							"Unknown V2 parameter in line ", lineno);
				}

				V_is_set = ((V_is_set < 2) ? 2 : V_is_set);
				i += 3;
			} else if (arg[i].getw().toUpperCase().equals("V3")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in arg3. Set this in
				// the element
				this.check_array();

				if (arg[i + 2].is_a_number()) {
					V[3] = arg[i + 2].getn();
				} else {
					throw new java.text.ParseException(
							"Unknown V3 parameter in line ", lineno);
				}

				V_is_set = ((V_is_set < 3) ? 3 : V_is_set);
				i += 3;
			} else if (arg[i].getw().toUpperCase().equals("V4")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in arg3. Set this in
				// the element
				this.check_array();

				if (arg[i + 2].is_a_number()) {
					V[4] = arg[i + 2].getn();
				} else {
					throw new java.text.ParseException(
							"Unknown V4 parameter in line ", lineno);
				}

				V_is_set = ((V_is_set < 4) ? 4 : V_is_set);
				i += 3;
			} else if (arg[i].getw().toUpperCase().equals("V5")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in arg3. Set this in
				// the element
				this.check_array();

				if (arg[i + 2].is_a_number()) {
					V[5] = arg[i + 2].getn();
				} else {
					throw new java.text.ParseException(
							"Unknown V5 parameter in line ", lineno);
				}

				V_is_set = ((V_is_set < 5) ? 5 : V_is_set);
				i += 3;
			} else if (arg[i].getw().toUpperCase().equals("V6")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in arg3. Set this in
				// the element
				this.check_array();

				if (arg[i + 2].is_a_number()) {
					V[6] = arg[i + 2].getn();
				} else {
					throw new java.text.ParseException(
							"Unknown V6 parameter in line ", lineno);
				}

				V_is_set = ((V_is_set < 6) ? 6 : V_is_set);
				i += 3;
			} else if (arg[i].getw().toUpperCase().equals("V7")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in arg3. Set this in
				// the element
				this.check_array();

				if (arg[i + 2].is_a_number()) {
					V[7] = arg[i + 2].getn();
				} else {
					throw new java.text.ParseException(
							"Unknown V7 parameter in line ", lineno);
				}

				V_is_set = ((V_is_set < 7) ? 7 : V_is_set);
				i += 3;
			} else if (arg[i].getw().toUpperCase().equals("V8")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in arg3. Set this in
				// the element
				this.check_array();

				if (arg[i + 2].is_a_number()) {
					V[8] = arg[i + 2].getn();
				} else {
					throw new java.text.ParseException(
							"Unknown V8 parameter in line ", lineno);
				}

				V_is_set = ((V_is_set < 8) ? 8 : V_is_set);
				i += 3;
			} else if (arg[i].getw().toUpperCase().equals("V9")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in arg3. Set this in
				// the element
				this.check_array();

				if (arg[i + 2].is_a_number()) {
					V[9] = arg[i + 2].getn();
				} else {
					throw new java.text.ParseException(
							"Unknown V9 parameter in line ", lineno);
				}

				V_is_set = ((V_is_set < 9) ? 9 : V_is_set);
				i += 3;
			} else
			// The yield stress of the material is defined
			if (arg[i].getw().toUpperCase().equals("YIELD_STRESS")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in arg3. Set this in
				// the element
				if (arg[i + 2].is_a_number()) {
					yield_stress = new Variable(arg[i + 2].getn());
					EP_is_required = true;
				} else if (arg[i + 2].getw().startsWith("[")
						&& arg[i + 2].getw().endsWith("]")) {
					yield_stress = new Variable(arg[i + 2].getw().substring(1,
							arg[i + 2].getw().length() - 1));
				} else {
					throw new java.text.ParseException(
							"Unknown Yield_Stress parameter in line ", lineno);
				}

				i += 3;
				YIELD_is_set = true;
			} else
			// The Plastic Strain vs Plastic Stress andgle of the material is
			// defined
			if (arg[i].getw().toUpperCase().toUpperCase().equals("EP")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in arg3. Set this in
				// the element
				factor = arg[i + 2].getn();
				i += 3;
				EP_is_set = true;
			} else if (arg[i].getw().toUpperCase().equals("Y1")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in arg3. Set this in
				// the element
				this.check_array();

				if (arg[i + 2].is_a_number()) {
					throw new java.text.ParseException(
							"Only curves accepted for the Y1 option.", lineno);
				} else if (arg[i + 2].getw().startsWith("[")
						&& arg[i + 2].getw().endsWith("]")) {
					yield_array[1] = new Variable(arg[i + 2].getw().substring(
							1, arg[i + 2].getw().length() - 1));
				} else {
					throw new java.text.ParseException(
							"Unknown Yield_Stress parameter in line ", lineno);
				}

				Y_is_set = ((Y_is_set < 1) ? 1 : Y_is_set);
				i += 3;
			} else if (arg[i].getw().toUpperCase().equals("Y2")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in arg3. Set this in
				// the element
				this.check_array();

				if (arg[i + 2].is_a_number()) {
					throw new java.text.ParseException(
							"Only curves accepted for the Y2 option.", lineno);
				} else if (arg[i + 2].getw().startsWith("[")
						&& arg[i + 2].getw().endsWith("]")) {
					yield_array[2] = new Variable(arg[i + 2].getw().substring(
							1, arg[i + 2].getw().length() - 1));
				} else {
					throw new java.text.ParseException(
							"Unknown Yield_Stress parameter in line ", lineno);
				}

				Y_is_set = ((Y_is_set < 2) ? 2 : Y_is_set);
				i += 3;
			} else if (arg[i].getw().toUpperCase().equals("Y3")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in arg3. Set this in
				// the element
				this.check_array();

				if (arg[i + 2].is_a_number()) {
					throw new java.text.ParseException(
							"Only curves accepted for the Y3 option.", lineno);
				} else if (arg[i + 2].getw().startsWith("[")
						&& arg[i + 2].getw().endsWith("]")) {
					yield_array[3] = new Variable(arg[i + 2].getw().substring(
							1, arg[i + 2].getw().length() - 1));
				} else {
					throw new java.text.ParseException(
							"Unknown Yield_Stress parameter in line ", lineno);
				}

				Y_is_set = ((Y_is_set < 3) ? 3 : Y_is_set);
				i += 3;
			} else if (arg[i].getw().toUpperCase().equals("Y4")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in arg3. Set this in
				// the element
				this.check_array();

				if (arg[i + 2].is_a_number()) {
					throw new java.text.ParseException(
							"Only curves accepted for the Y4 option.", lineno);
				} else if (arg[i + 2].getw().startsWith("[")
						&& arg[i + 2].getw().endsWith("]")) {
					yield_array[4] = new Variable(arg[i + 2].getw().substring(
							1, arg[i + 2].getw().length() - 1));
				} else {
					throw new java.text.ParseException(
							"Unknown Yield_Stress parameter in line ", lineno);
				}

				Y_is_set = ((Y_is_set < 4) ? 4 : Y_is_set);
				i += 3;
			} else if (arg[i].getw().toUpperCase().equals("Y5")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in arg3. Set this in
				// the element
				this.check_array();

				if (arg[i + 2].is_a_number()) {
					throw new java.text.ParseException(
							"Only curves accepted for the Y5 option.", lineno);
				} else if (arg[i + 2].getw().startsWith("[")
						&& arg[i + 2].getw().endsWith("]")) {
					yield_array[5] = new Variable(arg[i + 2].getw().substring(
							1, arg[i + 2].getw().length() - 1));
				} else {
					throw new java.text.ParseException(
							"Unknown Yield_Stress parameter in line ", lineno);
				}

				Y_is_set = ((Y_is_set < 5) ? 5 : Y_is_set);
				i += 3;
			} else if (arg[i].getw().toUpperCase().equals("Y6")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in arg3. Set this in
				// the element
				this.check_array();

				if (arg[i + 2].is_a_number()) {
					throw new java.text.ParseException(
							"Only curves accepted for the Y6 option.", lineno);
				} else if (arg[i + 2].getw().startsWith("[")
						&& arg[i + 2].getw().endsWith("]")) {
					yield_array[6] = new Variable(arg[i + 2].getw().substring(
							1, arg[i + 2].getw().length() - 1));
				} else {
					throw new java.text.ParseException(
							"Unknown Yield_Stress parameter in line ", lineno);
				}

				Y_is_set = ((Y_is_set < 6) ? 6 : Y_is_set);
				i += 3;
			} else if (arg[i].getw().toUpperCase().equals("Y7")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in arg3. Set this in
				// the element
				this.check_array();

				if (arg[i + 2].is_a_number()) {
					throw new java.text.ParseException(
							"Only curves accepted for the Y7 option.", lineno);
				} else if (arg[i + 2].getw().startsWith("[")
						&& arg[i + 2].getw().endsWith("]")) {
					yield_array[7] = new Variable(arg[i + 2].getw().substring(
							1, arg[i + 2].getw().length() - 1));
				} else {
					throw new java.text.ParseException(
							"Unknown Yield_Stress parameter in line ", lineno);
				}

				Y_is_set = ((Y_is_set < 7) ? 7 : Y_is_set);
				i += 3;
			} else if (arg[i].getw().toUpperCase().equals("Y8")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in arg3. Set this in
				// the element
				this.check_array();

				if (arg[i + 2].is_a_number()) {
					throw new java.text.ParseException(
							"Only curves accepted for the Y8 option.", lineno);
				} else if (arg[i + 2].getw().startsWith("[")
						&& arg[i + 2].getw().endsWith("]")) {
					yield_array[8] = new Variable(arg[i + 2].getw().substring(
							1, arg[i + 2].getw().length() - 1));
				} else {
					throw new java.text.ParseException(
							"Unknown Yield_Stress parameter in line ", lineno);
				}

				Y_is_set = ((Y_is_set < 8) ? 8 : Y_is_set);
				i += 3;
			} else if (arg[i].getw().toUpperCase().equals("Y9")
					&& arg[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in arg3. Set this in
				// the element
				this.check_array();

				if (arg[i + 2].is_a_number()) {
					throw new java.text.ParseException(
							"Only curves accepted for the Y9 option.", lineno);
				} else if (arg[i + 2].getw().startsWith("[")
						&& arg[i + 2].getw().endsWith("]")) {
					yield_array[9] = new Variable(arg[i + 2].getw().substring(
							1, arg[i + 2].getw().length() - 1));
				} else {
					throw new java.text.ParseException(
							"Unknown Yield_Stress parameter in line ", lineno);
				}

				Y_is_set = ((Y_is_set < 9) ? 9 : Y_is_set);
				i += 3;
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

	}

	public void checkIndata() throws IllegalArgumentException {
		int i;
		String out = new String("");

		if (!E_is_set) {
			throw new IllegalArgumentException(
					"No Youngs modulus (E) defined for this material: " + name);
		}

		if (!RHO_is_set) {
			throw new IllegalArgumentException(
					"No Density (RHO) defined for this material: " + name);
		}

		if (!NU_is_set) {
			throw new IllegalArgumentException(
					"No Poissons_constant (NU) defined for this material: "
							+ name);
		}

		if (EP_is_required && !EP_is_set) {
			throw new IllegalArgumentException(
					"No Plastic Modulus (EP) defined for this material: "
							+ name);
		}

		if (!thermal_expantion_is_set) {
			throw new IllegalArgumentException(
					"No Thermal Expantion (A) defined for this material: "
							+ name);
		}

		if (!temperature_is_set) {
			throw new IllegalArgumentException(
					"No Temperature (T) defined for this material: " + name);
		}

		if (!YIELD_is_set) {
			throw new IllegalArgumentException(
					"No Yield stress level (YIELD_STRESS) defined for this material: "
							+ name);
		}

		if (!name_is_set) {
			throw new IllegalArgumentException("Material name not intialized");
		}

		if (((V_is_set > 0) || (Y_is_set > 0)) && (V_is_set != Y_is_set)) {
			throw new IllegalArgumentException(
					"Number of V:s (strain rates) specified is not same as number of Y:s (stress/strain curves)"
							+ name);
		}

		// Initialize the multi array if needed.
		if ((V_is_set > 0) && (Y_is_set > 0)) {
			yield_array[0] = yield_stress;

			for (i = 0; i < (V_is_set + 1); i++) {
				out += (V[i] + "," + i + ",");
			}

			out = out.substring(0, out.length() - 1);

			yield_stress = new Variable(new Variable(out), yield_array);
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
		stiffness_matrix_plane_stress.set(3, 3, ((1.0 - nu) * c2) / 2.0);
		stiffness_matrix_plane_stress.set(3, 4, 0);
		stiffness_matrix_plane_stress.set(3, 5, 0);

		//
		stiffness_matrix_plane_stress.set(4, 0, 0);
		stiffness_matrix_plane_stress.set(4, 1, 0);
		stiffness_matrix_plane_stress.set(4, 2, 0);
		stiffness_matrix_plane_stress.set(4, 3, 0);
		stiffness_matrix_plane_stress.set(4, 4,
				((5.0 / 6.0) * (1.0 - nu) * c2) / 2.0);
		stiffness_matrix_plane_stress.set(4, 5, 0);

		//
		stiffness_matrix_plane_stress.set(5, 0, 0);
		stiffness_matrix_plane_stress.set(5, 1, 0);
		stiffness_matrix_plane_stress.set(5, 2, 0);
		stiffness_matrix_plane_stress.set(5, 3, 0);
		stiffness_matrix_plane_stress.set(5, 4, 0);
		stiffness_matrix_plane_stress.set(5, 5,
				((5.0 / 6.0) * (1.0 - nu) * c2) / 2.0);

		// A matrix for material law plane stress
		A.set(0, 0, 1.0);
		A.set(0, 1, -0.5);
		A.set(0, 2, 0);
		A.set(0, 3, 0);
		A.set(0, 4, 0);
		A.set(0, 5, 0);

		//
		A.set(1, 0, -0.5);
		A.set(1, 1, 1.0);
		A.set(1, 2, 0);
		A.set(1, 3, 0);
		A.set(1, 4, 0);
		A.set(1, 5, 0);

		//
		A.set(2, 0, 0);
		A.set(2, 1, 0);
		A.set(2, 2, 0);
		A.set(2, 3, 0);
		A.set(2, 4, 0);
		A.set(2, 5, 0);

		//
		A.set(3, 0, 0);
		A.set(3, 1, 0);
		A.set(3, 2, 0);
		A.set(3, 3, 3.0);
		A.set(3, 4, 0);
		A.set(3, 5, 0);

		//
		A.set(4, 0, 0);
		A.set(4, 1, 0);
		A.set(4, 2, 0);
		A.set(4, 3, 0);
		A.set(4, 4, 0);
		A.set(4, 5, 0);

		//
		A.set(5, 0, 0);
		A.set(5, 1, 0);
		A.set(5, 2, 0);
		A.set(5, 3, 0);
		A.set(5, 4, 0);
		A.set(5, 5, 0);

		// I matrix (Unity matrix) for material law plane stress
		I.set(0, 0, 1.0);
		I.set(0, 1, 0);
		I.set(0, 2, 0);
		I.set(0, 3, 0);
		I.set(0, 4, 0);
		I.set(0, 5, 0);

		//
		I.set(1, 0, 0);
		I.set(1, 1, 1.0);
		I.set(1, 2, 0);
		I.set(1, 3, 0);
		I.set(1, 4, 0);
		I.set(1, 5, 0);

		//
		I.set(2, 0, 0);
		I.set(2, 1, 0);
		I.set(2, 2, 1.0);
		I.set(2, 3, 0);
		I.set(2, 4, 0);
		I.set(2, 5, 0);

		//
		I.set(3, 0, 0);
		I.set(3, 1, 0);
		I.set(3, 2, 0);
		I.set(3, 3, 1.0);
		I.set(3, 4, 0);
		I.set(3, 5, 0);

		//
		I.set(4, 0, 0);
		I.set(4, 1, 0);
		I.set(4, 2, 0);
		I.set(4, 3, 0);
		I.set(4, 4, 1.0);
		I.set(4, 5, 0);

		//
		I.set(5, 0, 0);
		I.set(5, 1, 0);
		I.set(5, 2, 0);
		I.set(5, 3, 0);
		I.set(5, 4, 0);
		I.set(5, 5, 1.0);
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
		return Math.sqrt((youngs_modulus * (1.0 - nu))
				/ (density * (1.0 + nu) * (1.0 - (2.0 * nu))));
	}

	/**
	 * Insert the method's description here. Creation date: (06/01/02 %T)
	 * 
	 * @return double
	 */
	public double wavespeedTwoDimensional(double param, double param2) {
		return Math.sqrt(youngs_modulus / (density * (1.0 - (nu * nu))));
	}

	/**
	 * This function returns the yield stress as a function of effective plastic
	 * strain Creation date: (13/12/01 %T)
	 * 
	 * @param plastic_strain
	 *            double
	 * 
	 */
	private double yieldStress(double plastic_strain, double strain_vel) {
		if (yield_stress.isAConstant()) {
			return yield_stress.value(plastic_strain)
					+ (factor * plastic_strain);
		} else if (V_is_set == 0) {
			return yield_stress.value(plastic_strain);
		} else {
			return yield_stress.value(plastic_strain, strain_vel);
		}
	}

	/**
	 * This function returns the yield stress derivate (increase) as a function
	 * of effective plastic strain Creation date: (13/12/01 %T)
	 * 
	 * @param plastic_strain
	 *            double
	 * 
	 */
	private double yieldStressDerivate(double plastic_strain, double strain_vel) {
		if (yield_stress.isAConstant()) {
			return factor;
		} else if (V_is_set == 0) {
			return yield_stress.derivate(plastic_strain);
		} else {
			return yield_stress.derivate(plastic_strain, strain_vel);
		}
	}

	/**
	 * This function initializes the yield_array since it is only used when a
	 * strain rate is defined. Creation date: (13/12/01 %T)
	 * 
	 */
	private void check_array() {
		if (yield_array == null) {
			yield_array = new Variable[10];
			V = new double[10];
			V[0] = 0;
		}
	}
}

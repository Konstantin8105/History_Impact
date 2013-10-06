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

import java.text.ParseException;

/**
 * This is a simple material used to hold spring force/displacement properties.
 */
public class SpringMaterial extends Material implements Cloneable {
	private boolean kx_is_set;
	private boolean ky_is_set;
	private boolean kz_is_set;
	private boolean krx_is_set;
	private boolean kry_is_set;
	private boolean krz_is_set;
	private boolean cx_is_set;
	private boolean cy_is_set;
	private boolean cz_is_set;
	private boolean crx_is_set;
	private boolean cry_is_set;
	private boolean crz_is_set;
	private Variable vkx;
	private Variable vky;
	private Variable vkz;
	private Variable vkrx;
	private Variable vkry;
	private Variable vkrz;
	private Variable vcx;
	private Variable vcy;
	private Variable vcz;
	private Variable vcrx;
	private Variable vcry;
	private Variable vcrz;
	private double kx;
	private double ky;
	private double kz;
	private double krx;
	private double kry;
	private double krz;
	private double cx;
	private double cy;
	private double cz;
	private double crx;
	private double cry;
	private double crz;

	/**
	 * This material law calculates forces based on displacements. The
	 * possibility of including damping is also available since velocities are
	 * also incorporated. The constitutive matrices are defined in the
	 * setInitialConditions method.
	 */
	public SpringMaterial() {
	}

	/**
	 * This material law calculates forces based on displacements. The
	 * possibility of including damping is also available since velocities are
	 * also incorporated. Note that nothing is returned since the incoming
	 * matrices are directly modified. All Matrices are assumed to be of the
	 * standard 6x1 type expressed in local coordinate system: displacement =
	 * [cx, cy, cz, rx, ry, rz]T velocity = [vx, vy, vz, vrx, vry, vrz]T force =
	 * [kx, ky, kz, krx, kry, krz]T
	 * 
	 * @param strain
	 *            double
	 */
	public void calculateStressOneDimensional(Jama.Matrix displacement,
			Jama.Matrix velocity, Jama.Matrix force, double timestep) {
		// Update only one value in stress, since this is one dimensional
		kx = vkx.value(displacement.get(0, 0));
		ky = vky.value(displacement.get(1, 0));
		kz = vkz.value(displacement.get(2, 0));
		krx = vkrx.value(displacement.get(3, 0));
		kry = vkry.value(displacement.get(4, 0));
		krz = vkrz.value(displacement.get(5, 0));

		cx = vcx.value(displacement.get(0, 0));
		cy = vcy.value(displacement.get(1, 0));
		cz = vcz.value(displacement.get(2, 0));
		crx = vcrx.value(displacement.get(3, 0));
		cry = vcry.value(displacement.get(4, 0));
		crz = vcrz.value(displacement.get(5, 0));

		force.set(0, 0,
				(kx * displacement.get(0, 0)) + (cx * velocity.get(0, 0)));
		force.set(1, 0,
				(ky * displacement.get(1, 0)) + (cy * velocity.get(1, 0)));
		force.set(2, 0,
				(kz * displacement.get(2, 0)) + (cz * velocity.get(2, 0)));
		force.set(3, 0,
				(krx * displacement.get(3, 0)) + (crx * velocity.get(3, 0)));
		force.set(4, 0,
				(kry * displacement.get(4, 0)) + (cry * velocity.get(4, 0)));
		force.set(5, 0,
				(krz * displacement.get(5, 0)) + (crz * velocity.get(5, 0)));
	}

	/**
	 * This method is currently not used.
	 * 
	 */
	public void calculateStressThreeDimensional(Jama.Matrix strain,
			Jama.Matrix dstrain, Jama.Matrix stress, double timestep) {
	}

	/**
	 * This method is currently not used.
	 * 
	 */
	public void calculateStressTwoDimensionalPlaneStress(Jama.Matrix strain,
			Jama.Matrix dstrain, Jama.Matrix stress, double timestep) {
	}

	/**
	 * This method parses the indata.
	 * 
	 * @exception java.lang.IllegalArgumentException
	 *                The exception description.
	 */
	public void parse_Fembic(Token[] param, int lineno)
			throws java.text.ParseException {
		int i = 0;

		while (i < param.length) {
			if (param[i].getw().toUpperCase().equals("KX")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						vkx = new Variable(param[i + 2].getw().substring(1,
								param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: KX = [ax,ay,bx,by,...] ",
								lineno);
					}
				} else {
					vkx = new Variable(param[i + 2].getn());
				}

				kx_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("KY")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						vky = new Variable(param[i + 2].getw().substring(1,
								param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: KY = [ax,ay,bx,by,...] ",
								lineno);
					}
				} else {
					vky = new Variable(param[i + 2].getn());
				}

				ky_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("KZ")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						vkz = new Variable(param[i + 2].getw().substring(1,
								param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: KZ = [ax,ay,bx,by,...] ",
								lineno);
					}
				} else {
					vkz = new Variable(param[i + 2].getn());
				}

				kz_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("KRX")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						vkrx = new Variable(param[i + 2].getw().substring(1,
								param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: KRX = [ax,ay,bx,by,...] ",
								lineno);
					}
				} else {
					vkrx = new Variable(param[i + 2].getn());
				}

				krx_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("KRY")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						vkry = new Variable(param[i + 2].getw().substring(1,
								param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: KRY = [ax,ay,bx,by,...] ",
								lineno);
					}
				} else {
					vkry = new Variable(param[i + 2].getn());
				}

				kry_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("KRZ")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						vkrz = new Variable(param[i + 2].getw().substring(1,
								param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: KRZ = [ax,ay,bx,by,...] ",
								lineno);
					}
				} else {
					vkrz = new Variable(param[i + 2].getn());
				}

				krz_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("CX")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						vcx = new Variable(param[i + 2].getw().substring(1,
								param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: CX = [ax,ay,bx,by,...] ",
								lineno);
					}
				} else {
					vcx = new Variable(param[i + 2].getn());
				}

				cx_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("CY")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						vcy = new Variable(param[i + 2].getw().substring(1,
								param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: CY = [ax,ay,bx,by,...] ",
								lineno);
					}
				} else {
					vcy = new Variable(param[i + 2].getn());
				}

				cy_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("CZ")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						vcz = new Variable(param[i + 2].getw().substring(1,
								param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: CZ = [ax,ay,bx,by,...] ",
								lineno);
					}
				} else {
					vcz = new Variable(param[i + 2].getn());
				}

				cz_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("CRX")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						vcrx = new Variable(param[i + 2].getw().substring(1,
								param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: CRX = [ax,ay,bx,by,...] ",
								lineno);
					}
				} else {
					vcrx = new Variable(param[i + 2].getn());
				}

				crx_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("CRY")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						vcry = new Variable(param[i + 2].getw().substring(1,
								param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: CRY = [ax,ay,bx,by,...] ",
								lineno);
					}
				} else {
					vcry = new Variable(param[i + 2].getn());
				}

				cry_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("CRZ")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						vcrz = new Variable(param[i + 2].getw().substring(1,
								param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: CRZ = [ax,ay,bx,by,...] ",
								lineno);
					}
				} else {
					vcrz = new Variable(param[i + 2].getn());
				}

				crz_is_set = true;
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

	/**
	 * Checks that all mandatory variables have been set
	 */
	public void checkIndata() throws IllegalArgumentException {
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
			out = new String(name);

			if (kx_is_set)
				out += " kx = " + vkx.printFembic();

			if (ky_is_set)
				out += " ky = " + vky.printFembic();

			if (kz_is_set)
				out += " kz = " + vkz.printFembic();

			if (krx_is_set)
				out += " krx = " + vkrx.printFembic();

			if (kry_is_set)
				out += " kry = " + vkry.printFembic();

			if (krz_is_set)
				out += " krz = " + vkrz.printFembic();

			if (cx_is_set)
				out += " cx = " + vcx.printFembic();

			if (cy_is_set)
				out += " cy = " + vcy.printFembic();

			if (cz_is_set)
				out += " cz = " + vcz.printFembic();

			if (crx_is_set)
				out += " crx = " + vcrx.printFembic();

			if (cry_is_set)
				out += " cry = " + vcry.printFembic();

			if (crz_is_set)
				out += " crz = " + vcrz.printFembic();

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
		// If no stiffness is defined, assume a zero relation.
		if (!kx_is_set) {
			vkx = new Variable(0.0);
		}

		if (!krx_is_set) {
			vkrx = new Variable(0.0);
		}

		if (!cx_is_set) {
			vcx = new Variable(0.0);
		}

		if (!crx_is_set) {
			vcrx = new Variable(0.0);
		}

		// If only one parameter is set, copy the other two directions.
		try {
			if (!ky_is_set) {
				vky = (Variable) vkx.copy();
			}

			if (!kz_is_set) {
				vkz = (Variable) vkx.copy();
			}

			if (!kry_is_set) {
				vkry = (Variable) vkrx.copy();
			}

			if (!krz_is_set) {
				vkrz = (Variable) vkrx.copy();
			}

			if (!cy_is_set) {
				vcy = (Variable) vcx.copy();
			}

			if (!cz_is_set) {
				vcz = (Variable) vcx.copy();
			}

			if (!cry_is_set) {
				vcry = (Variable) vcrx.copy();
			}

			if (!crz_is_set) {
				vcrz = (Variable) vcrx.copy();
			}
		} catch (CloneNotSupportedException e) {
			System.err.println("Object cannot clone");
		}

		kx = vkx.value(0);
		ky = vky.value(0);
		kz = vkz.value(0);
		krx = vkrx.value(0);
		kry = vkry.value(0);
		krz = vkrz.value(0);

		cx = vcx.value(0);
		cy = vcy.value(0);
		cz = vcz.value(0);
		crx = vcrx.value(0);
		cry = vcry.value(0);
		crz = vcrz.value(0);
	}

	/**
	 * This method is used here to define general critical timestep for a
	 * spring. The parameter is used here to supply the mass. The smallest of
	 * the possible timesteps are calculated.
	 * 
	 * @return double
	 */
	public double wavespeedOneDimensional(double mass, double inertia) {
		// Assume a huge timestep to allow zero stiffness of spring.
		double timestep = 1E15;

		// Different formulas for different cases:
		// Both stiffness and damping?
		if (kx_is_set && cx_is_set && (kx != 0)) {
			timestep = (Math.sqrt((mass * kx) + (cx * cx)) - cx) / kx;
		} else if (cx_is_set && (cx != 0)) {
			timestep = (0.5 * mass) / cx;
		} else if (kx_is_set && (kx != 0)) {
			timestep = Math.sqrt(mass / kx);
		}

		if (ky_is_set && cy_is_set && (ky != 0)) {
			timestep = Math.min(timestep,
					(Math.sqrt((mass * ky) + (cy * cy)) - cy) / ky);
		} else if (cy_is_set && (cy != 0)) {
			timestep = Math.min(timestep, (0.5 * mass) / cy);
		} else if (ky_is_set && (ky != 0)) {
			timestep = Math.min(timestep, Math.sqrt(mass / ky));
		}

		if (kz_is_set && cz_is_set && (kz != 0)) {
			timestep = Math.min(timestep,
					(Math.sqrt((mass * kz) + (cz * cz)) - cz) / kz);
		} else if (cz_is_set && (cz != 0)) {
			timestep = Math.min(timestep, (0.5 * mass) / cz);
		} else if (kz_is_set && (kz != 0)) {
			timestep = Math.min(timestep, Math.sqrt(mass / kz));
		}

		if (crx_is_set && crx_is_set && (krx != 0)) {
			timestep = Math.min(timestep,
					(Math.sqrt((inertia * krx) + (crx * crx)) - crx) / krx);
		} else if (crx_is_set && (crx != 0)) {
			timestep = Math.min(timestep, (0.5 * inertia) / crx);
		} else if (krx_is_set && (krx != 0)) {
			timestep = Math.min(timestep, Math.sqrt(inertia / krx));
		}

		if (kry_is_set && cry_is_set && (kry != 0)) {
			timestep = Math.min(timestep,
					(Math.sqrt((inertia * kry) + (cry * cry)) - cry) / kry);
		} else if (cry_is_set && (cry != 0)) {
			timestep = Math.min(timestep, (0.5 * inertia) / cry);
		} else if (kry_is_set && (kry != 0)) {
			timestep = Math.min(timestep, Math.sqrt(inertia / kry));
		}

		if (krz_is_set && crz_is_set && (krz != 0)) {
			timestep = Math.min(timestep,
					(Math.sqrt((inertia * krz) + (crz * crz)) - crz) / krz);
		} else if (crz_is_set && (crz != 0)) {
			timestep = Math.min(timestep, (0.5 * inertia) / crz);
		} else if (krz_is_set && (krz != 0)) {
			timestep = Math.min(timestep, Math.sqrt(inertia / krz));
		}

		return timestep;
	}

	/**
	 * This method is not used here
	 * 
	 * @return double
	 */
	public double wavespeedThreeDimensional(double param, double param2) {
		return 0.0;
	}

	/**
	 * This method is not used here
	 * 
	 * @return double
	 */
	public double wavespeedTwoDimensional(double param, double param2) {
		return 0.0;
	}
}

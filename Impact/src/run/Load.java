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

/**
 * Insert the type's description here. Creation date: (28/08/01 %T)
 * 
 * @author: Jonas Forssell, Yuriy Mikhaylovskiy
 */
public class Load implements java.io.Serializable {
	public java.lang.String name;
	private Jama.Matrix load;
	private Jama.Matrix acc;
	private boolean x_force_is_set = false;
	private boolean y_force_is_set = false;
	private boolean z_force_is_set = false;
	private boolean z_moment_is_set = false;
	private boolean y_moment_is_set = false;
	private boolean x_moment_is_set = false;
	private boolean x_acc_is_set = false;
	private boolean z_acc_is_set = false;
	private boolean y_acc_is_set = false;
	private boolean x_rot_acc_is_set = false;
	private boolean z_rot_acc_is_set = false;
	private boolean y_rot_acc_is_set = false;
	private boolean pressure_is_set = false;
	private Variable x_force;
	private Variable y_force;
	private Variable z_force;
	private Variable x_moment;
	private Variable y_moment;
	private Variable z_moment;
	private Variable x_acc;
	private Variable y_acc;
	private Variable z_acc;
	private Variable x_rot_acc;
	private Variable y_rot_acc;
	private Variable z_rot_acc;
	private Variable pressure;

	/**
	 * When a load is initialized, the default setting is 0 on everything. Any
	 * changes will be applied by the initialization object.
	 */
	public Load() {
		int i;
		load = new Jama.Matrix(6, 1);
		acc = new Jama.Matrix(6, 1);

		for (i = 0; i < 6; i++) {
			load.set(i, 0, 0);
			acc.set(i, 0, 0);
		}
	}

	/**
	 * This method returns the force matrix (fx fy fz mx my mz)T Creation date:
	 * (19/11/01 %T)
	 * 
	 * @return Jama.Matrix
	 */
	public Jama.Matrix getLoad(double currtime) {
		// Update the matrix
		if (x_force_is_set) {
			load.set(0, 0, x_force.value(currtime)
					* (x_force.on(currtime) ? 1.0 : 0.0));
		}

		if (y_force_is_set) {
			load.set(1, 0, y_force.value(currtime)
					* (y_force.on(currtime) ? 1.0 : 0.0));
		}

		if (z_force_is_set) {
			load.set(2, 0, z_force.value(currtime)
					* (z_force.on(currtime) ? 1.0 : 0.0));
		}

		if (x_moment_is_set) {
			load.set(3, 0, x_moment.value(currtime)
					* (x_moment.on(currtime) ? 1.0 : 0.0));
		}

		if (y_moment_is_set) {
			load.set(4, 0, y_moment.value(currtime)
					* (y_moment.on(currtime) ? 1.0 : 0.0));
		}

		if (z_moment_is_set) {
			load.set(5, 0, z_moment.value(currtime)
					* (z_moment.on(currtime) ? 1.0 : 0.0));
		}

		// return it
		return load;
	}

	/**
	 * This method returns the acceleration matrix (ax ay az arx ary arz)T
	 * Creation date: (19/11/01 %T)
	 * 
	 * @return Jama.Matrix
	 */
	public Jama.Matrix getAcc(double currtime) {
		// Update the matrix
		if (x_acc_is_set) {
			acc.set(0, 0, x_acc.value(currtime)
					* (x_acc.on(currtime) ? 1.0 : 0.0));
		}

		if (y_acc_is_set) {
			acc.set(1, 0, y_acc.value(currtime)
					* (y_acc.on(currtime) ? 1.0 : 0.0));
		}

		if (z_acc_is_set) {
			acc.set(2, 0, z_acc.value(currtime)
					* (z_acc.on(currtime) ? 1.0 : 0.0));
		}

		if (x_rot_acc_is_set) {
			acc.set(3, 0, x_rot_acc.value(currtime)
					* (x_rot_acc.on(currtime) ? 1.0 : 0.0));
		}

		if (y_rot_acc_is_set) {
			acc.set(4, 0, y_rot_acc.value(currtime)
					* (y_rot_acc.on(currtime) ? 1.0 : 0.0));
		}

		if (z_rot_acc_is_set) {
			acc.set(5, 0, z_rot_acc.value(currtime)
					* (z_rot_acc.on(currtime) ? 1.0 : 0.0));
		}

		// return it
		return acc;
	}

	/**
	 * This method returns the pressure Creation date: (19/11/01 %T)
	 * 
	 * @return Jama.Matrix
	 */
	public double getPressure(double currtime) {
		// Update the pressure and return it
		if (pressure_is_set) {
			return pressure.value(currtime)
					* (pressure.on(currtime) ? 1.0 : 0.0);
		}

		// Otherwise, return zero
		return 0.0;
	}

	public void parse_Fembic(Token[] param, int lineno)
			throws java.text.ParseException {
		int i = 0;

		while (i < param.length) {
			if (param[i].getw().toUpperCase().equals("FX")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						x_force = new Variable(param[i + 2].getw().substring(1,
								param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: FX = [t1,fx1,t2,fx2,...] ",
								lineno);
					}
				} else {
					x_force = new Variable(param[i + 2].getn());
				}

				x_force_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("FY")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						y_force = new Variable(param[i + 2].getw().substring(1,
								param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: FY = [t1,fy1,t2,fy2,...] ",
								lineno);
					}
				} else {
					y_force = new Variable(param[i + 2].getn());
				}

				y_force_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("FZ")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						z_force = new Variable(param[i + 2].getw().substring(1,
								param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: FZ = [t1,fz1,t2,fz2,...] ",
								lineno);
					}
				} else {
					z_force = new Variable(param[i + 2].getn());
				}

				z_force_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("MX")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						x_moment = new Variable(param[i + 2].getw().substring(
								1, param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: MX = [t1,mx1,t2,mx2,...] ",
								lineno);
					}
				} else {
					x_moment = new Variable(param[i + 2].getn());
				}

				x_moment_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("MY")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						y_moment = new Variable(param[i + 2].getw().substring(
								1, param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: MY = [t1,my1,t2,my2,...] ",
								lineno);
					}
				} else {
					y_moment = new Variable(param[i + 2].getn());
				}

				y_moment_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("MZ")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						z_moment = new Variable(param[i + 2].getw().substring(
								1, param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: MZ = [t1,mz1,t2,mz2,...] ",
								lineno);
					}
				} else {
					z_moment = new Variable(param[i + 2].getn());
				}

				z_moment_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("AX")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						x_acc = new Variable(param[i + 2].getw().substring(1,
								param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: AX = [t1,ax1,t2,ax2,...] ",
								lineno);
					}
				} else {
					x_acc = new Variable(param[i + 2].getn());
				}

				x_acc_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("AY")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						y_acc = new Variable(param[i + 2].getw().substring(1,
								param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: AY = [t1,ay1,t2,ay2,...] ",
								lineno);
					}
				} else {
					y_acc = new Variable(param[i + 2].getn());
				}

				y_acc_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("AZ")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						z_acc = new Variable(param[i + 2].getw().substring(1,
								param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: AZ = [t1,az1,t2,az2,...] ",
								lineno);
					}
				} else {
					z_acc = new Variable(param[i + 2].getn());
				}

				z_acc_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("ARX")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						x_rot_acc = new Variable(param[i + 2].getw().substring(
								1, param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: ARX = [t1,arx1,t2,arx2,...] ",
								lineno);
					}
				} else {
					x_rot_acc = new Variable(param[i + 2].getn());
				}

				x_rot_acc_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("ARY")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						y_rot_acc = new Variable(param[i + 2].getw().substring(
								1, param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: ARY = [t1,ary1,t2,ary2,...] ",
								lineno);
					}
				} else {
					y_rot_acc = new Variable(param[i + 2].getn());
				}

				y_rot_acc_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("ARZ")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						z_rot_acc = new Variable(param[i + 2].getw().substring(
								1, param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: ARZ = [t1,arz1,t2,arz2,...] ",
								lineno);
					}
				} else {
					z_rot_acc = new Variable(param[i + 2].getn());
				}

				z_rot_acc_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("P")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						pressure = new Variable(param[i + 2].getw().substring(
								1, param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: P = [t1,p1,t2,p2,...] ",
								lineno);
					}
				} else {
					pressure = new Variable(param[i + 2].getn());
				}

				pressure_is_set = true;
				i += 3;
			} else {
				throw new java.text.ParseException(
						"Syntax error, unknown force parameter", lineno);
			}
		}
	}

	public void parse_Nastran(Token[] param, int lineno)
			throws java.text.ParseException {
		int i = 0;

		while (i < param.length) {
			if (param[i].getw().toUpperCase().equals("FX")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						x_force = new Variable(param[i + 2].getw().substring(1,
								param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: FX = [t1,fx1,t2,fx2,...] ",
								lineno);
					}
				} else {
					x_force = new Variable(param[i + 2].getn());
				}

				x_force_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("FY")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						y_force = new Variable(param[i + 2].getw().substring(1,
								param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: FY = [t1,fy1,t2,fy2,...] ",
								lineno);
					}
				} else {
					y_force = new Variable(param[i + 2].getn());
				}

				y_force_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("FZ")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						z_force = new Variable(param[i + 2].getw().substring(1,
								param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: FZ = [t1,fz1,t2,fz2,...] ",
								lineno);
					}
				} else {
					z_force = new Variable(param[i + 2].getn());
				}

				z_force_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("MX")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						x_moment = new Variable(param[i + 2].getw().substring(
								1, param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: MX = [t1,mx1,t2,mx2,...] ",
								lineno);
					}
				} else {
					x_moment = new Variable(param[i + 2].getn());
				}

				x_moment_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("MY")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						y_moment = new Variable(param[i + 2].getw().substring(
								1, param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: MY = [t1,my1,t2,my2,...] ",
								lineno);
					}
				} else {
					y_moment = new Variable(param[i + 2].getn());
				}

				y_moment_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("MZ")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						z_moment = new Variable(param[i + 2].getw().substring(
								1, param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: MZ = [t1,mz1,t2,mz2,...] ",
								lineno);
					}
				} else {
					z_moment = new Variable(param[i + 2].getn());
				}

				z_moment_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("AX")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						x_acc = new Variable(param[i + 2].getw().substring(1,
								param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: AX = [t1,ax1,t2,ax2,...] ",
								lineno);
					}
				} else {
					x_acc = new Variable(param[i + 2].getn());
				}

				x_acc_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("AY")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						y_acc = new Variable(param[i + 2].getw().substring(1,
								param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: AY = [t1,ay1,t2,ay2,...] ",
								lineno);
					}
				} else {
					y_acc = new Variable(param[i + 2].getn());
				}

				y_acc_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("AZ")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						z_acc = new Variable(param[i + 2].getw().substring(1,
								param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: AZ = [t1,az1,t2,az2,...] ",
								lineno);
					}
				} else {
					z_acc = new Variable(param[i + 2].getn());
				}

				z_acc_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("ARX")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						x_rot_acc = new Variable(param[i + 2].getw().substring(
								1, param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: ARX = [t1,arx1,t2,arx2,...] ",
								lineno);
					}
				} else {
					x_rot_acc = new Variable(param[i + 2].getn());
				}

				x_rot_acc_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("ARY")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						y_rot_acc = new Variable(param[i + 2].getw().substring(
								1, param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: ARY = [t1,ary1,t2,ary2,...] ",
								lineno);
					}
				} else {
					y_rot_acc = new Variable(param[i + 2].getn());
				}

				y_rot_acc_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("ARZ")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						z_rot_acc = new Variable(param[i + 2].getw().substring(
								1, param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: ARZ = [t1,arz1,t2,arz2,...] ",
								lineno);
					}
				} else {
					z_rot_acc = new Variable(param[i + 2].getn());
				}

				z_rot_acc_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("P")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						pressure = new Variable(param[i + 2].getw().substring(
								1, param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: P = [t1,p1,t2,p2,...] ",
								lineno);
					}
				} else {
					pressure = new Variable(param[i + 2].getn());
				}

				pressure_is_set = true;
				i += 3;
			} else {
				throw new java.text.ParseException(
						"Syntax error, unknown force parameter", lineno);
			}
		}
	}

	public void parse_Gmsh(Token[] param, int lineno, RplVector nodelist)
			throws java.text.ParseException {
		int index = 0;
		Node temp_node;

		name = new String("LD_" + lineno);

		x_force = new Variable(0);
		x_force_is_set = true;

		y_force = new Variable(0);
		y_force_is_set = true;

		z_force = new Variable(0);
		z_force_is_set = true;

		x_moment = new Variable(0);
		x_moment_is_set = true;

		y_moment = new Variable(0);
		y_moment_is_set = true;

		z_moment = new Variable(0);
		z_moment_is_set = true;

		// Assign this constraint to the corresponding nodes
		for (index = 0; index < nodelist.size(); index++) {
			temp_node = (Node) nodelist.elementAt(index);

			if (temp_node.getNumber() == param[5].getn())
				temp_node.setLoad(this);

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

			if (x_acc_is_set)
				out += " ax = " + x_acc.printFembic();

			if (y_acc_is_set)
				out += " ay = " + y_acc.printFembic();

			if (z_acc_is_set)
				out += " az = " + z_acc.printFembic();

			if (x_rot_acc_is_set)
				out += " arx = " + x_rot_acc.printFembic();

			if (y_rot_acc_is_set)
				out += " ary = " + y_rot_acc.printFembic();

			if (z_rot_acc_is_set)
				out += " arz = " + z_rot_acc.printFembic();

			if (x_force_is_set)
				out += " fx = " + x_force.printFembic();

			if (y_force_is_set)
				out += " fy = " + y_force.printFembic();

			if (z_force_is_set)
				out += " fz = " + z_force.printFembic();

			if (x_moment_is_set)
				out += " mx = " + x_moment.printFembic();

			if (y_moment_is_set)
				out += " my = " + y_moment.printFembic();

			if (z_moment_is_set)
				out += " mz = " + z_moment.printFembic();

			if (pressure_is_set)
				out += " p = " + pressure.printFembic();

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
	}

	/**
	 * Returns the name.
	 * 
	 * @return java.lang.String
	 */
	public java.lang.String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            The name to set
	 */
	public void setName(java.lang.String name) {
		this.name = name;
	}

}

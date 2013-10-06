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

package run.constraints;

import run.*;

import java.text.ParseException;

/**
 * The BoundaryCondition class creates the boundary_condition element.
 * 
 * @author: Jonas Forssell
 */
public class BoundaryCondition extends Constraint {
	private boolean x_vel_is_set = false;
	private boolean y_vel_is_set = false;
	private boolean z_vel_is_set = false;
	private boolean z_rot_vel_is_set = false;
	private boolean y_rot_vel_is_set = false;
	private boolean x_rot_vel_is_set = false;
	private boolean x_acc_is_set = false;
	private boolean z_acc_is_set = false;
	private boolean y_acc_is_set = false;
	private boolean x_rot_acc_is_set = false;
	private boolean z_rot_acc_is_set = false;
	private boolean y_rot_acc_is_set = false;
	private boolean axis_is_set = false;
	private boolean update_is_set = false;
	private Variable x_vel;
	private Variable y_vel;
	private Variable z_vel;
	private Variable x_rot_vel;
	private Variable y_rot_vel;
	private Variable z_rot_vel;
	private Variable x_acc;
	private Variable y_acc;
	private Variable z_acc;
	private Variable x_rot_acc;
	private Variable y_rot_acc;
	private Variable z_rot_acc;
	private Node[] node;
	private Jama.Matrix axis;
	private String nodes;
	private double x;
	private double y;
	private double z;

	/**
	 * The dafault constructor of the boundary condition
	 */
	public BoundaryCondition() {
		super();
		type = new String("BOUNDARY_CONDITION");
	}

	/**
	 * Creates a BoundaryCondition with the name given.
	 * 
	 * @param param
	 *            is the constraint.
	 */
	public BoundaryCondition(Constraint param) {
		name = new String(param.getName());
		type = new String("BOUNDARY_CONDITION");
	}

	/**
	 * Checks if the x_acceleration is on at the time currtime
	 * 
	 * @param currtime
	 *            is the time at which to check
	 */
	private boolean x_acc_is_on(double currtime) {
		if (!x_acc_is_set) {
			return false;
		}

		return x_acc.on(currtime);
	}

	/**
	 * Checks if the y_acceleration is on at the time currtime
	 * 
	 * @param currtime
	 *            is the time at which to check
	 */
	private boolean y_acc_is_on(double currtime) {
		if (!y_acc_is_set) {
			return false;
		}

		return y_acc.on(currtime);
	}

	/**
	 * Checks if the z_acceleration is on at the time currtime
	 * 
	 * @param currtime
	 *            is the time at which to check
	 */
	private boolean z_acc_is_on(double currtime) {
		if (!z_acc_is_set) {
			return false;
		}

		return z_acc.on(currtime);
	}

	/**
     */
	private boolean x_vel_is_on(double currtime) {
		if (!x_vel_is_set) {
			return false;
		}

		return x_vel.on(currtime);
	}

	/**
     */
	private boolean y_vel_is_on(double currtime) {
		if (!y_vel_is_set) {
			return false;
		}

		return y_vel.on(currtime);
	}

	/**
     */
	private boolean z_vel_is_on(double currtime) {
		if (!z_vel_is_set) {
			return false;
		}

		return z_vel.on(currtime);
	}

	/**
     */
	private boolean x_rot_acc_is_on(double currtime) {
		if (!x_rot_acc_is_set) {
			return false;
		}

		return x_rot_acc.on(currtime);
	}

	/**
     */
	private boolean y_rot_acc_is_on(double currtime) {
		if (!y_rot_acc_is_set) {
			return false;
		}

		return y_rot_acc.on(currtime);
	}

	/**
     */
	private boolean z_rot_acc_is_on(double currtime) {
		if (!z_rot_acc_is_set) {
			return false;
		}

		return z_rot_acc.on(currtime);
	}

	/**
     */
	private boolean x_rot_vel_is_on(double currtime) {
		if (!x_rot_vel_is_set) {
			return false;
		}

		return x_rot_vel.on(currtime);
	}

	/**
     */
	private boolean y_rot_vel_is_on(double currtime) {
		if (!y_rot_vel_is_set) {
			return false;
		}

		return y_rot_vel.on(currtime);
	}

	/**
     */
	private boolean z_rot_vel_is_on(double currtime) {
		if (!z_rot_vel_is_set) {
			return false;
		}

		return z_rot_vel.on(currtime);
	}

	/**
	 * This method is used to check that all mandatory parameters have been set
	 */
	public void checkIndata() throws IllegalArgumentException {
	}

	/**
	 * This method reads and parses the indata file in format Fembic.
	 */
	public void parse_Fembic(Token[] param, int lineno, RplVector nodelist)
			throws java.text.ParseException {
		int i = 0;
		while (i < param.length) {
			if (param[i].getw().toUpperCase().equals("VX")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						x_vel = new Variable(param[i + 2].getw().substring(1,
								param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: VX = [ax,ay,bx,by,...] ",
								lineno);
					}
				} else {
					x_vel = new Variable(param[i + 2].getn());
				}

				x_vel_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("VY")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						y_vel = new Variable(param[i + 2].getw().substring(1,
								param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: VY = [ax,ay,bx,by,...] ",
								lineno);
					}
				} else {
					y_vel = new Variable(param[i + 2].getn());
				}

				y_vel_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("VZ")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						z_vel = new Variable(param[i + 2].getw().substring(1,
								param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: VZ = [ax,ay,bx,by,...] ",
								lineno);
					}
				} else {
					z_vel = new Variable(param[i + 2].getn());
				}

				z_vel_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("VRX")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						x_rot_vel = new Variable(param[i + 2].getw().substring(
								1, param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: VRX = [ax,ay,bx,by,...] ",
								lineno);
					}
				} else {
					x_rot_vel = new Variable(param[i + 2].getn());
				}

				x_rot_vel_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("VRY")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						y_rot_vel = new Variable(param[i + 2].getw().substring(
								1, param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: VRY = [ax,ay,bx,by,...] ",
								lineno);
					}
				} else {
					y_rot_vel = new Variable(param[i + 2].getn());
				}

				y_rot_vel_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("VRZ")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						z_rot_vel = new Variable(param[i + 2].getw().substring(
								1, param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: VRZ = [ax,ay,bx,by,...] ",
								lineno);
					}
				} else {
					z_rot_vel = new Variable(param[i + 2].getn());
				}

				z_rot_vel_is_set = true;
				i += 3;
			} else if (param[i].getw().toUpperCase().equals("AX")) {
				if (param[i + 2].is_a_word()) {
					if (param[i + 2].getw().startsWith("[")
							&& param[i + 2].getw().endsWith("]")) {
						x_acc = new Variable(param[i + 2].getw().substring(1,
								param[i + 2].getw().length() - 1));
					} else {
						throw new ParseException(
								"Illegal parameter. Syntax: AX = [ax,ay,bx,by,...] ",
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
								"Illegal parameter. Syntax: AY = [ax,ay,bx,by,...] ",
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
								"Illegal parameter. Syntax: AZ = [ax,ay,bx,by,...] ",
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
								"Illegal parameter. Syntax: ARX = [ax,ay,bx,by,...] ",
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
								"Illegal parameter. Syntax: ARY = [ax,ay,bx,by,...] ",
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
								"Illegal parameter. Syntax: ARZ = [ax,ay,bx,by,...] ",
								lineno);
					}
				} else {
					z_rot_acc = new Variable(param[i + 2].getn());
				}

				z_rot_acc_is_set = true;
				i += 3;
			} else
			// The nodes of the local coordinate system are defined
			if (param[i].getw().toUpperCase().equals("AXIS")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// Assume now that the nodes are delivered in param3, with the
				// format
				// [nodenr,nodenr,nodenr]
				if (!param[i + 2].getw().toUpperCase().startsWith("[")
						|| !param[i + 2].getw().toUpperCase().endsWith("]")) {
					throw new java.text.ParseException(
							"Error, Axis node number definition should be [nodenr1,nodenr2,nodenr3]",
							lineno);
				}

				// Initialize the array
				nodes = param[i + 2].getw();
				node = new Node[3];
				i += 3;
				axis_is_set = true;
			} else
			// The user wishes the local coordinate system to be updated during
			// solution
			if (param[i].getw().toUpperCase().equals("UPDATE")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the parameter is in param3.
				if (param[i + 2].getw().toUpperCase().equals("ON")) {
					update_is_set = true;
				} else {
					throw new ParseException(
							"Unrecognized update parameter. Only option is ON",
							lineno);
				}

				i += 3;
			} else {
				throw new java.text.ParseException(
						"Syntax error, unrecognized constraint parameter",
						lineno);
			}
		}
	}

	/**
	 * This method reads and parses the indata file in format Fembic.
	 */
	public void parse_Nastran(Token[] param, int lineno, RplVector nodelist)
			throws java.text.ParseException {

		String temp;
		Node temp_node;
		int start, end, i, j, index;

		if (param[0].is_a_word() && param[0].getw().trim().equals("SPC1")) {
			if (param[1].is_a_number()) {
				name = new String("BC_" + lineno);
			} else {
				throw new ParseException(
						"Illegal identification number of SPC1: "
								+ param[1].getn(), lineno);
			}

			if (param[2].is_a_number()) {

				temp = new String("" + param[2].getn());

				if (temp.indexOf("1") != -1) {
					x_acc = new Variable(0);
					x_acc_is_set = true;
					x_vel = new Variable(0);
					x_vel_is_set = true;
				}

				if (temp.indexOf("2") != -1) {
					y_acc = new Variable(0);
					y_acc_is_set = true;
					y_vel = new Variable(0);
					y_vel_is_set = true;
				}

				if (temp.indexOf("3") != -1) {
					z_acc = new Variable(0);
					z_acc_is_set = true;
					z_vel = new Variable(0);
					z_vel_is_set = true;
				}

				if (temp.indexOf("4") != -1) {
					x_rot_acc = new Variable(0);
					x_rot_acc_is_set = true;
					x_rot_vel = new Variable(0);
					x_rot_vel_is_set = true;
				}

				if (temp.indexOf("5") != -1) {
					y_rot_acc = new Variable(0);
					y_rot_acc_is_set = true;
					y_rot_vel = new Variable(0);
					y_rot_vel_is_set = true;
				}

				if (temp.indexOf("6") != -1) {
					z_rot_acc = new Variable(0);
					z_rot_acc_is_set = true;
					z_rot_vel = new Variable(0);
					z_rot_vel_is_set = true;
				}

			} else
				throw new ParseException("Illegal component number for SPC1: "
						+ param[1].getn(), lineno);

			i = 3;

		} else {
			i = 1;
		}

		do {
			start = 0;
			end = 0;
			if (param[i].is_a_number()
					&& (i == param.length - 1 || i == 8 || param[i + 1]
							.is_a_number())) {
				start = (int) param[i].getn();
				end = start;
				i++;
			} else if (param[i].is_a_number() && i <= param.length - 3
					&& param[i + 1].is_a_word() && param[i + 2].is_a_number()) {
				start = (int) param[i].getn();
				end = (int) param[i + 2].getn();
				i += 3;
			} else if (param[i].is_a_word() && i == 9)
				break;

			else

			{
				throw new ParseException("Illegal node number in SPC1: "
						+ param[1].getn(), lineno);
			}

			// Assign this constraint to the corresponding nodes
			for (j = start; j <= end; j++)
				for (index = 0; index < nodelist.size(); index++) {
					temp_node = (Node) nodelist.elementAt(index);

					if (temp_node.getNumber() == j)
						temp_node.setConstraint(this);

				}

		} while (i < param.length);

	}

	/**
	 * This method reads and parses the indata file in format Fembic.
	 */
	public void parse_Gmsh(Token[] param, int val, RplVector nodelist)
			throws java.text.ParseException {

		Node temp_node;
		int i, index;

		name = new String("BC_" + val);

		// Default setting is fixed constraint
		x_vel = new Variable(0);
		x_vel_is_set = true;

		y_vel = new Variable(0);
		y_vel_is_set = true;

		z_vel = new Variable(0);
		z_vel_is_set = true;

		x_rot_vel = new Variable(0);
		x_rot_vel_is_set = true;

		y_rot_vel = new Variable(0);
		y_rot_vel_is_set = true;

		z_rot_vel = new Variable(0);
		z_rot_vel_is_set = true;

		// Assign this constraint to the corresponding nodes
		for (i = 5; i < 5 + (int) param[4].getn(); i++)
			for (index = 0; index < nodelist.size(); index++) {
				temp_node = (Node) nodelist.elementAt(index);

				if (temp_node.getNumber() == param[i].getn())
					temp_node.setConstraint(this);

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

			if (x_vel_is_set)
				out += " vx = " + x_vel.printFembic();

			if (y_vel_is_set)
				out += " vy = " + y_vel.printFembic();

			if (z_vel_is_set)
				out += " vz = " + z_vel.printFembic();

			if (x_rot_vel_is_set)
				out += " vrx = " + x_rot_vel.printFembic();

			if (y_rot_vel_is_set)
				out += " vry = " + y_rot_vel.printFembic();

			if (z_rot_vel_is_set)
				out += " vrz = " + z_rot_vel.printFembic();

			if (axis_is_set)
				out += " axis = [" + node[0].getNumber() + ","
						+ node[1].getNumber() + "," + node[2].getNumber() + "]";

			if (update_is_set)
				out += " Update = ON";

			out += "\n";

			return out;

		default:
			return new String("");
		}
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public void applyAccelerationConditions(Node nod, double currtime) {
		if (axis_is_set) {
			if (x_vel_is_on(currtime)) {
				x = 0;
			} else if (x_acc_is_on(currtime)) {
				x = x_acc.value(currtime);
			} else {
				x = (nod.getX_acc() * axis.get(0, 0))
						+ (nod.getY_acc() * axis.get(1, 0))
						+ (nod.getZ_acc() * axis.get(2, 0));
			}

			if (y_vel_is_on(currtime)) {
				y = 0;
			} else if (y_acc_is_on(currtime)) {
				y = y_acc.value(currtime);
			} else {
				y = (nod.getX_acc() * axis.get(0, 1))
						+ (nod.getY_acc() * axis.get(1, 1))
						+ (nod.getZ_acc() * axis.get(2, 1));
			}

			if (z_vel_is_on(currtime)) {
				z = 0;
			} else if (z_acc_is_on(currtime)) {
				z = z_acc.value(currtime);
			} else {
				z = (nod.getX_acc() * axis.get(0, 2))
						+ (nod.getY_acc() * axis.get(1, 2))
						+ (nod.getZ_acc() * axis.get(2, 2));
			}

			nod.setX_acc((x * axis.get(0, 0)) + (y * axis.get(0, 1))
					+ (z * axis.get(0, 2)));
			nod.setY_acc((x * axis.get(1, 0)) + (y * axis.get(1, 1))
					+ (z * axis.get(1, 2)));
			nod.setZ_acc((x * axis.get(2, 0)) + (y * axis.get(2, 1))
					+ (z * axis.get(2, 2)));

			if (x_rot_vel_is_on(currtime)) {
				x = 0;
			} else if (x_rot_acc_is_on(currtime)) {
				x = x_rot_acc.value(currtime);
			} else {
				x = (nod.getX_rot_acc() * axis.get(0, 0))
						+ (nod.getY_rot_acc() * axis.get(1, 0))
						+ (nod.getZ_rot_acc() * axis.get(2, 0));
			}

			if (y_rot_vel_is_on(currtime)) {
				y = 0;
			} else if (y_rot_acc_is_on(currtime)) {
				y = y_rot_acc.value(currtime);
			} else {
				y = (nod.getX_rot_acc() * axis.get(0, 1))
						+ (nod.getY_rot_acc() * axis.get(1, 1))
						+ (nod.getZ_rot_acc() * axis.get(2, 1));
			}

			if (z_rot_vel_is_on(currtime)) {
				z = 0;
			} else if (z_rot_acc_is_on(currtime)) {
				z = z_rot_acc.value(currtime);
			} else {
				z = (nod.getX_rot_acc() * axis.get(0, 2))
						+ (nod.getY_rot_acc() * axis.get(1, 2))
						+ (nod.getZ_rot_acc() * axis.get(2, 2));
			}

			nod.setX_rot_acc((x * axis.get(0, 0)) + (y * axis.get(0, 1))
					+ (z * axis.get(0, 2)));
			nod.setY_rot_acc((x * axis.get(1, 0)) + (y * axis.get(1, 1))
					+ (z * axis.get(1, 2)));
			nod.setZ_rot_acc((x * axis.get(2, 0)) + (y * axis.get(2, 1))
					+ (z * axis.get(2, 2)));
		} else {
			if (x_vel_is_on(currtime)) {
				nod.setX_acc(0);
			} else if (x_acc_is_on(currtime)) {
				nod.setX_acc(x_acc.value(currtime));
			}

			if (y_vel_is_on(currtime)) {
				nod.setY_acc(0);
			} else if (y_acc_is_on(currtime)) {
				nod.setY_acc(y_acc.value(currtime));
			}

			if (z_vel_is_on(currtime)) {
				nod.setZ_acc(0);
			} else if (z_acc_is_on(currtime)) {
				nod.setZ_acc(z_acc.value(currtime));
			}

			if (x_rot_vel_is_on(currtime)) {
				nod.setX_rot_acc(0);
			} else if (x_rot_acc_is_on(currtime)) {
				nod.setX_rot_acc(x_rot_acc.value(currtime));
			}

			if (y_rot_vel_is_on(currtime)) {
				nod.setY_rot_acc(0);
			} else if (y_rot_acc_is_on(currtime)) {
				nod.setY_rot_acc(y_rot_acc.value(currtime));
			}

			if (z_rot_vel_is_on(currtime)) {
				nod.setZ_rot_acc(0);
			} else if (z_rot_acc_is_on(currtime)) {
				nod.setZ_rot_acc(z_rot_acc.value(currtime));
			}
		}
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public void applyVelocityConditions(Node nod, double currtime) {
		if (axis_is_set) {
			if (x_vel_is_on(currtime)) {
				x = x_vel.value(currtime);
			} else {
				x = (nod.getX_vel() * axis.get(0, 0))
						+ (nod.getY_vel() * axis.get(1, 0))
						+ (nod.getZ_vel() * axis.get(2, 0));
			}

			if (y_vel_is_on(currtime)) {
				y = y_vel.value(currtime);
			} else {
				y = (nod.getX_vel() * axis.get(0, 1))
						+ (nod.getY_vel() * axis.get(1, 1))
						+ (nod.getZ_vel() * axis.get(2, 1));
			}

			if (z_vel_is_on(currtime)) {
				z = z_vel.value(currtime);
			} else {
				z = (nod.getX_vel() * axis.get(0, 2))
						+ (nod.getY_vel() * axis.get(1, 2))
						+ (nod.getZ_vel() * axis.get(2, 2));
			}

			nod.setX_vel((x * axis.get(0, 0)) + (y * axis.get(0, 1))
					+ (z * axis.get(0, 2)));
			nod.setY_vel((x * axis.get(1, 0)) + (y * axis.get(1, 1))
					+ (z * axis.get(1, 2)));
			nod.setZ_vel((x * axis.get(2, 0)) + (y * axis.get(2, 1))
					+ (z * axis.get(2, 2)));

			if (x_rot_vel_is_on(currtime)) {
				x = x_rot_vel.value(currtime);
			} else {
				x = (nod.getX_rot_vel() * axis.get(0, 0))
						+ (nod.getY_rot_vel() * axis.get(1, 0))
						+ (nod.getZ_rot_vel() * axis.get(2, 0));
			}

			if (y_rot_vel_is_on(currtime)) {
				y = y_rot_vel.value(currtime);
			} else {
				y = (nod.getX_rot_vel() * axis.get(0, 1))
						+ (nod.getY_rot_vel() * axis.get(1, 1))
						+ (nod.getZ_rot_vel() * axis.get(2, 1));
			}

			if (z_rot_vel_is_on(currtime)) {
				z = z_rot_vel.value(currtime);
			} else {
				z = (nod.getX_rot_vel() * axis.get(0, 2))
						+ (nod.getY_rot_vel() * axis.get(1, 2))
						+ (nod.getZ_rot_vel() * axis.get(2, 2));
			}

			nod.setX_rot_vel((x * axis.get(0, 0)) + (y * axis.get(0, 1))
					+ (z * axis.get(0, 2)));
			nod.setY_rot_vel((x * axis.get(1, 0)) + (y * axis.get(1, 1))
					+ (z * axis.get(1, 2)));
			nod.setZ_rot_vel((x * axis.get(2, 0)) + (y * axis.get(2, 1))
					+ (z * axis.get(2, 2)));
		} else {
			if (x_vel_is_on(currtime)) {
				nod.setX_vel(x_vel.value(currtime));
			}

			if (y_vel_is_on(currtime)) {
				nod.setY_vel(y_vel.value(currtime));
			}

			if (z_vel_is_on(currtime)) {
				nod.setZ_vel(z_vel.value(currtime));
			}

			if (x_rot_vel_is_on(currtime)) {
				nod.setX_rot_vel(x_rot_vel.value(currtime));
			}

			if (y_rot_vel_is_on(currtime)) {
				nod.setY_rot_vel(y_rot_vel.value(currtime));
			}

			if (z_rot_vel_is_on(currtime)) {
				nod.setZ_rot_vel(z_rot_vel.value(currtime));
			}
		}
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public void registerNode(Node nod) {
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public void setInitialConditions() {
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public void update() {
		if (axis_is_set && update_is_set) {
			axis = this.calculateLocalBaseVectors(node[0].getX_pos(),
					node[0].getY_pos(), node[0].getZ_pos(), node[1].getX_pos(),
					node[1].getY_pos(), node[1].getZ_pos(), node[2].getX_pos(),
					node[2].getY_pos(), node[2].getZ_pos());
		}
	}

	public void determineMassMatrix(RplVector nodelist) {
		int j;

		// This should have ideally been made already in the initialization
		// phase, but constraints are
		// defined first by necessity. This means no nodelist is available at
		// that time, so we do it here instead.
		// Ok, now find the numbers & nodes
		if (axis_is_set) {

			for (j = 0; j < 3; j++) {
				node[j] = super.findNode(super.getNodeNumber(j + 1, nodes),
						nodelist);
			}

			// Now, initialize the local coordinate system
			axis = this.calculateLocalBaseVectors(node[0].getX_pos(),
					node[0].getY_pos(), node[0].getZ_pos(), node[1].getX_pos(),
					node[1].getY_pos(), node[1].getZ_pos(), node[2].getX_pos(),
					node[2].getY_pos(), node[2].getZ_pos());
		}
	}
}

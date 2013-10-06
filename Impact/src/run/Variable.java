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

/**
 * This class parses and stores a defined variable range. The variable should be
 * defined in coordinate pairs as follows: 0,0,3,5.4,24,12 It can also be
 * defined as: 1,1,3,off,4,5 This means that the interval from 3 to 4 has no
 * value. This is useful for example contraints which should only be active
 * during a certain time. The class can also store a range of variables and
 * interpolate between these. The constructor is then an array of variables z[]
 * and a control variable v. The v variable is used to relate values to which
 * variable to interpolate between. Example: v = 0 0 1.23 1 3.42 2 -3.43 3 This
 * means that a value 2.23 should be an interpolation of variable 1 and 2 in the
 * variable array z[].
 */
public class Variable implements Cloneable, java.io.Serializable {
	private double[] x;
	private double[] y;
	private boolean[] on;
	private boolean is_a_constant;
	private int index;
	private String token;
	private Variable[] z;
	private Variable v;

	public Variable(String arg) throws IllegalArgumentException {
		int number_of_commas;
		int i;
		int temp_index;
		int next_index;
		number_of_commas = 0;
		is_a_constant = false;

		// Chop up the input into tokens and put them into the arrays
		// Start by determining numeber of coordinate pairs
		for (i = 0; i < arg.length(); i++) {
			if (arg.charAt(i) == ',') {
				number_of_commas++;
			}
		}

		// Even amount of commas means an error in one of the coordinate pairs.
		if (((number_of_commas / 2) * 2) == number_of_commas) {
			throw new IllegalArgumentException("Missing coordinate pairs in: "
					+ arg);
		}

		// Now, dimension the arrays
		x = new double[(number_of_commas + 1) / 2];
		y = new double[(number_of_commas + 1) / 2];
		on = new boolean[(number_of_commas + 1) / 2];

		// Next, convert and put into arrays.
		next_index = -1;

		for (i = 0; i < ((number_of_commas + 1) / 2); i++) {
			temp_index = next_index;
			next_index = arg.indexOf(',', temp_index + 1);
			token = new String(arg.substring(temp_index + 1, next_index).trim());

			if (this.is_a_number(token)) {
				x[i] = Double.valueOf(token).doubleValue();
			} else {
				throw new IllegalArgumentException(
						"No number specified for x-value in this indata: "
								+ arg);
			}

			temp_index = next_index;
			next_index = arg.indexOf(',', temp_index + 1);

			if (next_index == -1) {
				next_index = arg.length();
			}

			token = new String(arg.substring(temp_index + 1, next_index).trim());

			if (this.is_a_number(token)) {
				y[i] = Double.valueOf(token).doubleValue();
				on[i] = true;
			} else if (i > 0) {
				y[i] = y[i - 1];
				on[i] = false;
			} else {
				y[i] = 0.0;
				on[i] = false;
			}
		}
	}

	public Variable(double arg) throws IllegalArgumentException {
		is_a_constant = true;
		y = new double[1];
		y[0] = arg;
	}

	public Variable(Variable q, Variable[] a) {
		v = q;
		z = a;
	}

	public double value(double x_val) {
		// First, check if this variable has been defined as a constant
		if (is_a_constant == true) {
			return y[0];
		}

		// No? then continue
		// Find the right index. Start from the last_index and search upwards
		while ((index < x.length) && (x_val > x[index + 1])) {
			index++;
		}

		// Now, search downwards
		while ((x_val < x[index]) && (index > 0)) {
			index--;
		}

		// We have found the right index. Return the value as an interpolation
		// between the levels
		if (index == x.length) {
			return y[index];
		}

		//
		if (index > x.length) {
			throw new IllegalArgumentException("Parameter out of bounds");
		}

		//
		return (((y[index + 1] - y[index]) * (x_val - x[index])) / (x[index + 1] - x[index]))
				+ y[index];
	}

	public double derivate(double x_val) {
		// First, check if this variable has been defined as a constant
		if (is_a_constant == true) {
			return 0;
		}

		// No? then continue
		// Find the right index. Start from the last_index and search upwards
		while ((index < x.length) && (x_val > x[index + 1])) {
			index++;
		}

		// Now, search downwards
		while ((x_val < x[index]) && (index > 0)) {
			index--;
		}

		// We have found the right index. Return the value as an interpolation
		// between the levels
		if (index == x.length) {
			return y[index];
		}

		//
		if (index > x.length) {
			throw new IllegalArgumentException("Parameter out of bounds");
		}

		//
		return (y[index + 1] - y[index]) / (x[index + 1] - x[index]);
	}

	public double value(double x_value, double y_value) {
		// This is only useable if this variable is a multi array
		if (z == null) {
			throw new IllegalArgumentException(
					"This variable has not been initalized as multi variable");
		}

		// Find the right index.
		index = (int) v.value(y_value);

		// We have found the right index. Return the value as an interpolation
		// between the levels
		if (index == z.length) {
			return z[index - 1].value(x_value);
		}

		//
		if (index > z.length) {
			throw new IllegalArgumentException("Parameter out of bounds");
		}

		//
		return ((z[index + 1].value(x_value) - z[index].value(x_value)) * (v
				.value(y_value) - (int) v.value(y_value)))
				+ z[index].value(x_value);
	}

	public double derivate(double x_value, double y_value) {
		// This is only useable if this variable is a multi array
		if (z == null) {
			throw new IllegalArgumentException(
					"This variable has not been initalized as multi variable");
		}

		// Find the right index.
		index = (int) v.value(y_value);

		// We have found the right index. Return the value as an interpolation
		// between the levels
		if (index == z.length) {
			return z[index - 1].derivate(x_value);
		}

		//
		if (index > z.length) {
			throw new IllegalArgumentException("Parameter out of bounds");
		}

		//
		return ((z[index + 1].derivate(x_value) - z[index].derivate(x_value)) * (v
				.value(y_value) - (int) v.value(y_value)))
				+ z[index].derivate(x_value);
	}

	public boolean on(double x_val) {
		if (is_a_constant) {
			return true;
		}

		// Find the right index. Start from the last_index and search upwards
		while ((index < x.length) && (x_val > x[index + 1])) {
			index++;
		}

		// Now, search downwards
		while ((x_val < x[index]) && (index > 0)) {
			index--;
		}

		// We have found the right index. Return the value as an interpolation
		// between the levels
		if (index > x.length) {
			throw new IllegalArgumentException("Parameter out of bounds");
		}

		return on[index];
	}

	private boolean is_a_number(String w) {
		Double d;

		try {
			d = new Double(w);
		} catch (NumberFormatException n) {
			return false;
		}

		return true;
	}

	public String printFembic() {
		int i;
		String out;

		out = "";

		if (is_a_constant) {
			out += y[0];
			return out;
		}

		out = "[";

		for (i = 0; i < x.length; i++) {
			out += x[i] + ",";
			if (on[i])
				out += y[i];
			else
				out += "off";
			out += ",";
		}

		out = out.substring(0, out.length() - 1) + "]";

		return out;

	}

	/**
	 * This method clones the Variable set.
	 * 
	 * @return Object A copy of the Variable
	 */

	public Object copy() throws CloneNotSupportedException {
		Object o = null;

		try {
			o = super.clone();
		} catch (CloneNotSupportedException e) {
			System.err.println("Object cannot clone");
		}

		return o;
	}

	public boolean isAConstant() {
		return is_a_constant;
	}
}

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

public class Token {
	String s;
	double d;
	boolean is_a_word;

	public Token(String w) {
		// Check if a number in disguise
		try {
			d = Double.parseDouble(w);
		} catch (NumberFormatException e) {
			// It is a word.
			s = new String(w);
			is_a_word = true;

			return;
		}

		// It is a number!
		is_a_word = false;
	}

	public Token(double n) {
		d = n;
		is_a_word = false;
	}

	public String getw() {
		return s;
	}

	public double getn() {
		return d;
	}

	public boolean is_a_word() {
		return is_a_word;
	}

	public boolean is_a_number() {
		return !is_a_word;
	}
}

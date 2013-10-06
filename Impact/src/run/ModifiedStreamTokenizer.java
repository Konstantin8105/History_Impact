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

/*
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU
 General Public License as published by the Free Software Foundation; either version 2 of the
 License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOUSE. See the GNU General Public License for more details.
 You should have recieved a copy of the GNU General Public License along with this program;
 if not write to the Free Software Foundation, inc., 59 Temple Place, Suite 330, Boston MA
 02111-1307 USA
 */
import java.io.*;

/**
 * A simple StreamTokenizer wrapper class to help the understanding of
 * engineering number format.
 */
public class ModifiedStreamTokenizer extends StreamTokenizer {
	public ModifiedStreamTokenizer(java.io.BufferedReader br) {
		super(br);

		this.resetSyntax();
		this.whitespaceChars(0, 32);
		this.wordChars(33, 255);
		this.commentChar('#');
		this.eolIsSignificant(true);
	}

	public int nextToken() throws IOException {
		double d;

		try {
			super.nextToken();
		} catch (IOException ioe) {
			throw new IOException();
		}

		if (ttype == TT_WORD) {
			// Check if a number in disguise
			try {
				d = Double.parseDouble(sval);
			} catch (NumberFormatException e) {
				// It is a word.
				return ttype;
			}

			// It is a number!
			nval = d;
			ttype = TT_NUMBER;
		}

		return ttype;
	}
}

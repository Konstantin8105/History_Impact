//******************************************************************************
//
// File:    VectorFunction.java
// Package: ---
// Unit:    Interface VectorFunction
//
// This Java source file is copyright (C) 2005 by Alan Kaminsky. All rights
// reserved. For further information, contact the author, Alan Kaminsky, at
// ark@cs.rit.edu.
//
// This program is free software; you can redistribute it and/or modify it under
// the terms of the GNU General Public License as published by the Free Software
// Foundation; either version 2 of the License, or (at your option) any later
// version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
// details.
//
// You may obtain a copy of the GNU General Public License on the World Wide
// Web at http://www.gnu.org/licenses/gpl.html or by writing to the Free
// Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
// USA.
//
//******************************************************************************
package util;

/**
 * Interface VectorFunction specifies the interface for a function of a vector
 * of values.
 * 
 * @author Jonas Forssell
 * @version 11-Feb-2006
 */
public interface SingleFunction {

	/**
	 * Compute the function of the given variable.
	 * 
	 * @param x
	 *            Variable.
	 * 
	 * @return Function value, f(x).
	 */
	public double f(double x);

}

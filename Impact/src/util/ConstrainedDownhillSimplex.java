//******************************************************************************
//
// File:    ConstrainedDownhillSimplex.java
// Package: ---
// Unit:    Class ConstrainedDownhillSimplex
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
 * Class ConstrainedDownhillSimplex provides a method for finding the minimum of
 * a vector function, constrained so that all the vector components are
 * nonnegative.
 * 
 * @author Alan Kaminsky
 * @version 11-Apr-2005
 */
public class ConstrainedDownhillSimplex {

	/**
	 * Find the vector that minimizes the given function using the constrained
	 * downhill simplex method. On input, <TT>x</TT> is one vertex of the
	 * initial simplex. The remaining vertices of the initial simplex are found
	 * by adding <TT>xdelta[i]</TT> to <TT>x</TT> for each index <TT>i</TT>. On
	 * output, <TT>x</TT> is replaced with the vector that minimizes
	 * <TT>theFunction</TT>. The algorithm terminates when the relative
	 * difference between the function value at the high point of the simplex
	 * and the function value at the low point of the simplex is less than or
	 * equal to <TT>eps</TT>. The algorithm aborts and throws an exception if it
	 * does <TT>maxiters</TT> iterations without terminating.
	 * 
	 * @param theFunction
	 *            Vector function to be minimized.
	 * @param x
	 *            On input, one point of the initial simplex. On output, the
	 *            vector that minimizes <TT>theFunction</TT>.
	 * @param xdelta
	 *            Vector of increments added to each component of <TT>x</TT> to
	 *            form the remaining points of the initial simplex.
	 * @param eps
	 *            Termination criterion.
	 * @param maxiter
	 *            Maximum number of iterations.
	 * 
	 * @return The value of <TT>theFunction</TT> evaluated at the output vector
	 *         <TT>x</TT>; that is, the minimum function value the algorithm
	 *         found.
	 * 
	 * @exception NullPointerException
	 *                (unchecked exception) Thrown if <TT>theFunction</TT> is
	 *                null, <TT>x</TT> is null, or <TT>xdelta</TT> is null.
	 * @exception IllegalArgumentException
	 *                (unchecked exception) Thrown if <TT>x</TT> or
	 *                <TT>xdelta</TT> has fewer than 2 elements. Thrown if
	 *                <TT>x</TT> and <TT>xdelta</TT> do not have the same number
	 *                of elements. Thrown if <TT>eps</TT> &lt;= 0. Thrown if
	 *                <TT>maxiter</TT> &lt;= 0. Thrown if any point of the
	 *                initial simplex has a negative component.
	 * @exception IllegalStateException
	 *                (unchecked exception) Thrown if more than <TT>maxiter</TT>
	 *                iterations occurred without finding a minimum.
	 */
	public static float minimize(VectorFunction theFunction, float[] x,
			float[] xdelta, float eps, int maxiter) {
		// Verify preconditions.
		if (theFunction == null) {
			throw new NullPointerException(
					"ConstrainedDownhillSimplex.minimize(): theFunction is null");
		}
		if (x == null) {
			throw new NullPointerException(
					"ConstrainedDownhillSimplex.minimize(): x is null");
		}
		if (xdelta == null) {
			throw new NullPointerException(
					"ConstrainedDownhillSimplex.minimize(): xdelta is null");
		}

		int N = x.length;
		if (N < 2) {
			throw new IllegalArgumentException(
					"ConstrainedDownhillSimplex.minimize(): x.length < 2");
		}
		if (xdelta.length < 2) {
			throw new IllegalArgumentException(
					"ConstrainedDownhillSimplex.minimize(): xdelta.length < 2");
		}
		if (xdelta.length != N) {
			throw new IllegalArgumentException(
					"ConstrainedDownhillSimplex.minimize(): xdelta.length != x.length");
		}
		if (eps <= 0.0f) {
			throw new IllegalArgumentException(
					"ConstrainedDownhillSimplex.minimize(): eps <= 0");
		}
		if (maxiter <= 0) {
			throw new IllegalArgumentException(
					"ConstrainedDownhillSimplex.minimize(): maxiter <= 0");
		}

		// Allocate storage for N+1 simplex points, each a vector of length N.
		float[][] simplex = new float[N + 1][N];

		// Compute the initial simplex points, and verify that all coordinates
		// are nonnegative.
		for (int i = 0; i < N; ++i) {
			System.arraycopy(x, 0, simplex[i], 0, N);
			simplex[i][i] += xdelta[i];
			verifyNonNegative(simplex[i], i, N);
		}
		System.arraycopy(x, 0, simplex[N], 0, N);
		verifyNonNegative(simplex[N], N, N);

		// Allocate storage for the simplex points' function values.
		float[] funcvalue = new float[N + 1];

		// Evaluate the function at each initial simplex point.
		for (int i = 0; i <= N; ++i) {
			funcvalue[i] = theFunction.f(simplex[i]);
		}

		// Allocate storage for the sum of the simplex points' coordinates along
		// each dimension.
		float[] coordsum = new float[N];

		// Compute the sum of the coordinates along each dimension of the
		// initial simplex points.
		computeCoordsum(simplex, coordsum, N);

		// Allocate storage for two temporary simplex points.
		float[] simplex_e = new float[N];
		float[] simplex_f = new float[N];

		// Iterate until convergence or until too many iterations.
		int iter = 0;
		int a = 0;
		int b = 0;
		int c = 0;
		for (;;) {
			// Find the indexes of the simplex points with the highest (a),
			// second-highest (b), and lowest (c) function values.
			a = 0;
			b = 0;
			c = 0;
			for (int i = 1; i <= N; ++i) {
				if (funcvalue[i] > funcvalue[a]) {
					b = a;
					a = i;
				} else if (funcvalue[i] > funcvalue[b]) {
					b = i;
				} else if (funcvalue[i] < funcvalue[c]) {
					c = i;
				}
			}

			// Check for convergence.
			// if (relativeDifference (funcvalue[a], funcvalue[c]) <= eps)
			// break;
			if (abs(funcvalue[a] - funcvalue[c]) < eps)
				break;

			// Check for too many iterations.
			if (iter >= maxiter) {
				throw new IllegalStateException(
						"ConstrainedDownhillSimplex.minimize(): Too many iterations");
			}

			// No convergence yet. Make a move.
			move(theFunction, simplex, funcvalue, coordsum, a, b, c, simplex_e,
					simplex_f, N);

			// Next iteration.
			++iter;
		}

		// Return solution.
		System.arraycopy(simplex[c], 0, x, 0, N);
		return funcvalue[c];
	}

	// Hidden operations.

	/**
	 * Make a move.
	 */
	private static void move(VectorFunction theFunction, float[][] simplex,
			float[] funcvalue, float[] coordsum, int a, int b, int c,
			float[] simplex_e, float[] simplex_f, int N) {
		float funcvalue_e;
		float funcvalue_f;

		// Try a reflection.
		reflection(simplex[a], coordsum, simplex_e, N);
		if (isNonNegative(simplex_e, N)) {
			// Reflection is legal.
			funcvalue_e = theFunction.f(simplex_e);
			if (funcvalue_e < funcvalue[c]) {
				// Try an expansion.
				expansion(simplex[a], coordsum, simplex_f, N);
				if (isNonNegative(simplex_f, N)) {
					// Expansion is legal.
					funcvalue_f = theFunction.f(simplex_f);
					if (funcvalue_f < funcvalue[a]) {
						// Keep the expansion. Replace a with f.
						keep(simplex[a], coordsum, simplex_f, N);
						funcvalue[a] = funcvalue_f;
						return;
					} else // (funcvalue_f >= funcvalue[a])
					{
						// Keep the reflection. Replace a with e.
						keep(simplex[a], coordsum, simplex_e, N);
						funcvalue[a] = funcvalue_e;
						return;
					}
				} else // (! isNonNegative (simplex_f, N))
				{
					// Expansion is illegal. Keep the reflection. Replace a
					// with e.
					keep(simplex[a], coordsum, simplex_e, N);
					funcvalue[a] = funcvalue_e;
					return;
				}
			} else if (funcvalue_e < funcvalue[b]) {
				// Keep the reflection. Replace a with e.
				keep(simplex[a], coordsum, simplex_e, N);
				funcvalue[a] = funcvalue_e;
				return;
			} else // (funcvalue_e >= funcvalue[b])
			{
				// Don't keep the reflection.
			}
		} else // (! isNonNegative (simplex_e, N))
		{
			// Reflection is illegal. Try a contracted reflection.
			contractedReflection(simplex[a], coordsum, simplex_e, N);
			if (isNonNegative(simplex_e, N)) {
				// Contracted reflection is legal.
				funcvalue_e = theFunction.f(simplex_e);
				if (funcvalue_e < funcvalue[b]) {
					// Keep the contracted reflection. Replace a with e.
					keep(simplex[a], coordsum, simplex_e, N);
					funcvalue[a] = funcvalue_e;
					return;
				} else // (funcvalue_e >= funcvalue[b])
				{
					// Don't keep the contracted reflection.
				}
			} else // (! isNonNegative (simplex_e, N))
			{
				// Contracted reflection is illegal. Don't keep the
				// contracted reflection.
			}
		}

		// Try a contraction.
		contraction(simplex[a], coordsum, simplex_e, N);
		funcvalue_e = theFunction.f(simplex_e);
		if (funcvalue_e < funcvalue[b]) {
			// Keep the contraction. Replace a with e.
			keep(simplex[a], coordsum, simplex_e, N);
			funcvalue[a] = funcvalue_e;
			return;
		} else // (funcvalue_e >= funcvalue[b])
		{
			// Don't keep the contraction.
		}

		// Give up. Do a collapse.
		collapse(theFunction, simplex, funcvalue, coordsum, c, N);
	}

	/**
	 * Do a reflection.
	 */
	private static void reflection(float[] simplex_a, float[] coordsum,
			float[] newpoint, int N) {
		// d = average of all simplex points except a = (coordsum-a)/N.
		// newpoint = d+(d-a) = 2*d - a = 2*(coordsum-a)/N - a
		// = 2*coordsum/N - 2*a/N - a = 2*coordsum/N - (N+2)*a/N
		float U = 2.0f / N;
		float V = (N + 2.0f) / N;
		for (int j = 0; j < N; ++j) {
			newpoint[j] = U * coordsum[j] - V * simplex_a[j];
		}
	}

	/**
	 * Do an expansion.
	 */
	private static void expansion(float[] simplex_a, float[] coordsum,
			float[] newpoint, int N) {
		// d = average of all simplex points except a = (coordsum-a)/N.
		// newpoint = d+2*(d-a) = 3*d - 2*a = 3*(coordsum-a)/N - 2*a
		// = 3*coordsum/N - 3*a/N - 2*a = 3*coordsum/N - (2*N+3)*a/N
		float U = 3.0f / N;
		float V = (2.0f * N + 3.0f) / N;
		for (int j = 0; j < N; ++j) {
			newpoint[j] = U * coordsum[j] - V * simplex_a[j];
		}
	}

	/**
	 * Do a contracted reflection.
	 */
	private static void contractedReflection(float[] simplex_a,
			float[] coordsum, float[] newpoint, int N) {
		// d = average of all simplex points except a = (coordsum-a)/N.
		// newpoint = d+(d-a)/2 = 3*d/2 - a/2 = 3*(coordsum-a)/N/2 - a/2
		// = 3*coordsum/N/2 - 3*a/N/2 - a/2
		// = 3*coordsum/N/2 - (N+3)*a/N/2
		float U = 3.0f / N / 2.0f;
		float V = (N + 3.0f) / N / 2.0f;
		for (int j = 0; j < N; ++j) {
			newpoint[j] = U * coordsum[j] - V * simplex_a[j];
		}
	}

	/**
	 * Do a contraction.
	 */
	private static void contraction(float[] simplex_a, float[] coordsum,
			float[] newpoint, int N) {
		// d = average of all simplex points except a = (coordsum-a)/N.
		// newpoint = (d+a)/2 = d/2 + a/2 = (coordsum-a)/N/2 + a/2
		// = coordsum/N/2 - a/N/2 + a/2
		// = coordsum/N/2 + (N-1)*a/N/2
		float U = 1.0f / N / 2.0f;
		float V = (N - 1.0f) / N / 2.0f;
		for (int j = 0; j < N; ++j) {
			newpoint[j] = U * coordsum[j] + V * simplex_a[j];
		}
	}

	/**
	 * Do a collapse.
	 */
	private static void collapse(VectorFunction theFunction, float[][] simplex,
			float[] funcvalue, float[] coordsum, int c, int N) {
		// For all simplex points except c, replace point with the average of
		// itself and c, and recompute the function value.
		for (int i = 0; i < c; ++i) {
			for (int j = 0; j < N; ++j) {
				simplex[i][j] = 0.5f * (simplex[i][j] + simplex[c][j]);
			}
			funcvalue[i] = theFunction.f(simplex[i]);
		}
		for (int i = c + 1; i <= N; ++i) {
			for (int j = 0; j < N; ++j) {
				simplex[i][j] = 0.5f * (simplex[i][j] + simplex[c][j]);
			}
			funcvalue[i] = theFunction.f(simplex[i]);
		}

		// Recompute coordinate sums.
		computeCoordsum(simplex, coordsum, N);
	}

	/**
	 * Replace the simplex point at index a with the new point.
	 */
	private static void keep(float[] simplex_a, float[] coordsum,
			float[] newpoint, int N) {
		// Update coordinate sums.
		for (int j = 0; j < N; ++j) {
			coordsum[j] = coordsum[j] - simplex_a[j] + newpoint[j];
		}

		// Replace simplex point.
		System.arraycopy(newpoint, 0, simplex_a, 0, N);
	}

	/**
	 * Verify that all components of the given vector are nonnegative.
	 */
	private static void verifyNonNegative(float[] v, int i, int N) {
		if (!isNonNegative(v, N)) {
			throw new IllegalArgumentException(
					"ConstrainedDownhillSimplex.minimize(): simplex[" + i
							+ "] has a negative component");
		}
	}

	/**
	 * Determine whether all components of the given vector are nonnegative.
	 */
	private static boolean isNonNegative(float[] v, int N) {
		for (int j = 0; j < N; ++j) {
			if (v[j] < 0.0f) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Compute the sum of the simplex point coordinates along each dimension.
	 */
	private static void computeCoordsum(float[][] simplex, float[] coordsum,
			int N) {
		for (int j = 0; j < N; ++j) {
			float sum = 0.0f;
			for (int i = 0; i <= N; ++i) {
				sum += simplex[i][j];
			}
			coordsum[j] = sum;
		}
	}

	/**
	 * Returns the relative difference between <TT>x</TT> and <TT>y</TT>.
	 */
	private static float relativeDifference(float x, float y) {
		return 2.0f * abs(x - y) / abs(x + y);
	}

	/**
	 * Returns the absolute value of <TT>x</TT>.
	 */
	private static float abs(float x) {
		return x >= 0.0f ? x : -x;
	}

}

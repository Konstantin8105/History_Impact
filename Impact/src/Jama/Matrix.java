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

package Jama;

import Jama.util.*;

import uka.transport.*;
import uka.patch.*;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StreamTokenizer;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

import java.util.Locale;

import uka.util.Printable;
import uka.util.ToString;

// **********

/**
 * Jama = Java Matrix class.
 * 
 * <P>
 * The Java Matrix Class provides the fundamental operations of numerical linear
 * algebra. Various constructors create Matrices from two dimensional arrays of
 * double precision floating point numbers. Various "gets" and "sets" provide
 * access to submatrices and matrix elements. Several methods implement basic
 * matrix arithmetic, including matrix addition and multiplication, matrix
 * norms, and element-by-element array operations. Methods for reading and
 * printing matrices are also included. All the operations in this version of
 * the Matrix Class involve real matrices. Complex matrices may be handled in a
 * future version.
 * </p>
 * 
 * <P>
 * Five fundamental matrix decompositions, which consist of pairs or triples of
 * matrices, permutation vectors, and the like, produce results in five
 * decomposition classes. These decompositions are accessed by the Matrix class
 * to compute solutions of simultaneous linear equations, determinants, inverses
 * and other matrix functions. The five decompositions are:
 * </p>
 * 
 * <P>
 * 
 * <UL>
 * <li>
 * Cholesky Decomposition of symmetric, positive definite matrices.</li>
 * <li>
 * LU Decomposition of rectangular matrices.</li>
 * <li>
 * QR Decomposition of rectangular matrices.</li>
 * <li>
 * Singular Value Decomposition of rectangular matrices.</li>
 * <li>
 * Eigenvalue Decomposition of both symmetric and nonsymmetric square matrices.</li>
 * </ul>
 * 
 * 
 * <DL>
 * <dt>
 * <B>Example of use:</B></dt>
 * <dd>
 * Solve a linear system A x = b and compute the residual norm, ||b - A x||.
 * 
 * <P>
 * 
 * <PRE>
 * double[][] vals = { { 1., 2., 3 }, { 4., 5., 6. }, { 7., 8., 10. } };
 * Matrix A = new Matrix(vals);
 * Matrix b = Matrix.random(3, 1);
 * Matrix x = A.solve(b);
 * Matrix r = A.times(x).minus(b);
 * double rnorm = r.normInf();
 * </PRE>
 * 
 * </p>
 * </dd>
 * </dl>
 * </p>
 * 
 * @author The MathWorks, Inc. and the National Institute of Standards and
 *         Technology.
 * @version 5 August 1998
 */
public class Matrix implements Printable, Cloneable, Transportable, Patchable,
		java.io.Serializable {
	/*
	 * ------------------------ Class variables ------------------------
	 */

	/**
	 * Array for internal storage of elements.
	 * 
	 * @serial internal array storage.
	 */
	protected double[][] A;

	/**
	 * Row and column dimensions.
	 * 
	 * @serial column dimension.
	 */
	protected int m;

	/**
	 * Row and column dimensions.
	 * 
	 * @serial column dimension.
	 */
	protected int n;

	/*
	 * ------------------------ Constructors ------------------------
	 */

	/**
	 * Construct an m-by-n matrix of zeros.
	 * 
	 * @param m
	 *            Number of rows.
	 * @param n
	 *            Number of colums.
	 */
	public Matrix(int m, int n) {
		this.m = m;
		this.n = n;
		A = new double[m][n];
		// System.out.println("---Matrix---(" + this.getClass().getName() + "@"
		// + Integer.toHexString(this.hashCode()) + ")\n" +
		// "            Array: " + A.getClass().getName() + "@" +
		// Integer.toHexString(A.hashCode()));
	}

	/**
	 * Construct an m-by-n constant matrix.
	 * 
	 * @param m
	 *            Number of rows.
	 * @param n
	 *            Number of colums.
	 * @param s
	 *            Fill the matrix with this scalar value.
	 */
	public Matrix(int m, int n, double s) {
		this.m = m;
		this.n = n;
		A = new double[m][n];

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				A[i][j] = s;
			}
		}
	}

	/**
	 * Construct a matrix from a one-dimensional packed array
	 * 
	 * @param vals
	 *            One-dimensional array of doubles, packed by columns (ala
	 *            Fortran).
	 * @param m
	 *            Number of rows.
	 * 
	 * @exception IllegalArgumentException
	 *                Array length must be a multiple of m.
	 */
	public Matrix(double[] vals, int m) {
		this.m = m;
		n = ((m != 0) ? (vals.length / m) : 0);

		if ((m * n) != vals.length) {
			throw new IllegalArgumentException(
					"Array length must be a multiple of m.");
		}

		A = new double[m][n];

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				A[i][j] = vals[i + (j * m)];
			}
		}
	}

	/**
	 * Construct a matrix from a 2-D array.
	 * 
	 * @param A
	 *            Two-dimensional array of doubles.
	 * 
	 * @exception IllegalArgumentException
	 *                All rows must have the same length
	 * 
	 * @see #constructWithCopy
	 */
	public Matrix(double[][] A) {
		m = A.length;
		n = A[0].length;

		for (int i = 0; i < m; i++) {
			if (A[i].length != n) {
				throw new IllegalArgumentException(
						"All rows must have the same length.");
			}
		}

		this.A = A;
	}

	/**
	 * Construct a matrix from a 2-D array.
	 * 
	 * @param A
	 *            Two-dimensional array of floats.
	 * 
	 * @exception IllegalArgumentException
	 *                All rows must have the same length
	 * 
	 * @see #constructWithCopy
	 */
	public Matrix(float[][] A) {
		m = A.length;
		n = A[0].length;

		for (int i = 0; i < m; i++) {
			if (A[i].length != n) {
				throw new IllegalArgumentException(
						"All rows must have the same length.");
			}
		}

		this.A = new double[m][n];

		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++)
				this.A[i][j] = (double) A[i][j];
	}

	/**
	 * Construct a matrix quickly without checking arguments.
	 * 
	 * @param A
	 *            Two-dimensional array of doubles.
	 * @param m
	 *            Number of rows.
	 * @param n
	 *            Number of colums.
	 */
	public Matrix(double[][] A, int m, int n) {
		this.A = A;
		this.m = m;
		this.n = n;
	}

	/**
	 * Element-by-element left division, C = A.\B
	 * 
	 * @param B
	 *            another matrix
	 * 
	 * @return A.\B
	 */
	public Matrix arrayLeftDivide(Matrix B) {
		checkMatrixDimensions(B);

		Matrix X = new Matrix(m, n);
		double[][] C = X.getArray();

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				C[i][j] = B.A[i][j] / A[i][j];
			}
		}

		return X;
	}

	/**
	 * Element-by-element left division in place, A = A.\B
	 * 
	 * @param B
	 *            another matrix
	 * 
	 * @return A.\B
	 */
	public Matrix arrayLeftDivideEquals(Matrix B) {
		checkMatrixDimensions(B);

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				A[i][j] = B.A[i][j] / A[i][j];
			}
		}

		return this;
	}

	/**
	 * Element-by-element right division, C = A./B
	 * 
	 * @param B
	 *            another matrix
	 * 
	 * @return A./B
	 */
	public Matrix arrayRightDivide(Matrix B) {
		checkMatrixDimensions(B);

		Matrix X = new Matrix(m, n);
		double[][] C = X.getArray();

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				C[i][j] = A[i][j] / B.A[i][j];
			}
		}

		return X;
	}

	/**
	 * Element-by-element right division in place, A = A./B
	 * 
	 * @param B
	 *            another matrix
	 * 
	 * @return A./B
	 */
	public Matrix arrayRightDivideEquals(Matrix B) {
		checkMatrixDimensions(B);

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				A[i][j] = A[i][j] / B.A[i][j];
			}
		}

		return this;
	}

	/**
	 * Element-by-element multiplication, C = A.B
	 * 
	 * @param B
	 *            another matrix
	 * 
	 * @return A.B
	 */
	public Matrix arrayTimes(Matrix B) {
		checkMatrixDimensions(B);

		Matrix X = new Matrix(m, n);
		double[][] C = X.getArray();

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				C[i][j] = A[i][j] * B.A[i][j];
			}
		}

		return X;
	}

	/**
	 * Element-by-element multiplication in place, A = A.B
	 * 
	 * @param B
	 *            another matrix
	 * 
	 * @return A.B
	 */
	public Matrix arrayTimesEquals(Matrix B) {
		checkMatrixDimensions(B);

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				A[i][j] = A[i][j] * B.A[i][j];
			}
		}

		return this;
	}

	/*
	 * ------------------------ Private Methods ------------------------
	 */

	/**
	 * Check if size(A) == size(B)
	 */
	private void checkMatrixDimensions(Matrix B) {
		if ((B.m != m) || (B.n != n)) {
			throw new IllegalArgumentException("Matrix dimensions must agree.");
		}
	}

	/**
	 * Cholesky Decomposition
	 * 
	 * @return CholeskyDecomposition
	 * 
	 * @see CholeskyDecomposition
	 */
	public CholeskyDecomposition chol() {
		return new CholeskyDecomposition(this);
	}

	/**
	 * Matrix condition (2 norm)
	 * 
	 * @return ratio of largest to smallest singular value.
	 */
	public double cond() {
		return new SingularValueDecomposition(this).cond();
	}

	/*
	 * ------------------------ Public Methods ------------------------
	 */

	/**
	 * Construct a matrix from a copy of a 2-D array.
	 * 
	 * @param A
	 *            Two-dimensional array of doubles.
	 * 
	 * @exception IllegalArgumentException
	 *                All rows must have the same length
	 */
	public static Matrix constructWithCopy(double[][] A) {
		int m = A.length;
		int n = A[0].length;
		Matrix X = new Matrix(m, n);
		double[][] C = X.getArray();

		for (int i = 0; i < m; i++) {
			if (A[i].length != n) {
				throw new IllegalArgumentException(
						"All rows must have the same length.");
			}

			for (int j = 0; j < n; j++) {
				C[i][j] = A[i][j];
			}
		}

		return X;
	}

	/**
	 * Make a deep copy of a matrix
	 */
	public Matrix copy() {
		Matrix X = new Matrix(m, n);
		double[][] C = X.getArray();

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				C[i][j] = A[i][j];
			}
		}

		return X;
	}

	/**
	 * Matrix determinant
	 * 
	 * @return determinant
	 */
	public double det() {
		return new LUDecomposition(this).det();
	}

	/**
	 * Eigenvalue Decomposition
	 * 
	 * @return EigenvalueDecomposition
	 * 
	 * @see EigenvalueDecomposition
	 */
	public EigenvalueDecomposition eig() {
		return new EigenvalueDecomposition(this);
	}

	/**
	 * Get a single element.
	 * 
	 * @param i
	 *            Row index.
	 * @param j
	 *            Column index.
	 * 
	 * @return A(i,j)
	 * 
	 * @exception ArrayIndexOutOfBoundsException
	 */
	public double get(int i, int j) {
		return A[i][j];
	}

	/**
	 * Access the internal two-dimensional array.
	 * 
	 * @return Pointer to the two-dimensional array of matrix elements.
	 */
	public double[][] getArray() {
		return A;
	}

	/**
	 * Copy the internal two-dimensional array.
	 * 
	 * @return Two-dimensional array copy of matrix elements.
	 */
	public double[][] getArrayCopy() {
		double[][] C = new double[m][n];

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				C[i][j] = A[i][j];
			}
		}

		return C;
	}

	/**
	 * Get column dimension.
	 * 
	 * @return n, the number of columns.
	 */
	public int getColumnDimension() {
		return n;
	}

	/**
	 * Make a one-dimensional column packed copy of the internal array.
	 * 
	 * @return Matrix elements packed in a one-dimensional array by columns.
	 */
	public double[] getColumnPackedCopy() {
		double[] vals = new double[m * n];

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				vals[i + (j * m)] = A[i][j];
			}
		}

		return vals;
	}

	/**
	 * Get a submatrix.
	 * 
	 * @param i0
	 *            Initial row index
	 * @param i1
	 *            Final row index
	 * @param j0
	 *            Initial column index
	 * @param j1
	 *            Final column index
	 * 
	 * @return A(i0:i1,j0:j1)
	 * 
	 * @exception ArrayIndexOutOfBoundsException
	 *                Submatrix indices
	 */
	public Matrix getMatrix(int i0, int i1, int j0, int j1) {
		Matrix X = new Matrix(i1 - i0 + 1, j1 - j0 + 1);
		double[][] B = X.getArray();

		try {
			for (int i = i0; i <= i1; i++) {
				for (int j = j0; j <= j1; j++) {
					B[i - i0][j - j0] = A[i][j];
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ArrayIndexOutOfBoundsException("Submatrix indices");
		}

		return X;
	}

	/**
	 * Get a submatrix.
	 * 
	 * @param i0
	 *            Initial row index
	 * @param i1
	 *            Final row index
	 * @param c
	 *            Array of column indices.
	 * 
	 * @return A(i0:i1,c(:))
	 * 
	 * @exception ArrayIndexOutOfBoundsException
	 *                Submatrix indices
	 */
	public Matrix getMatrix(int i0, int i1, int[] c) {
		Matrix X = new Matrix(i1 - i0 + 1, c.length);
		double[][] B = X.getArray();

		try {
			for (int i = i0; i <= i1; i++) {
				for (int j = 0; j < c.length; j++) {
					B[i - i0][j] = A[i][c[j]];
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ArrayIndexOutOfBoundsException("Submatrix indices");
		}

		return X;
	}

	/**
	 * Get a submatrix.
	 * 
	 * @param r
	 *            Array of row indices.
	 * @param i0
	 *            Initial column index
	 * @param i1
	 *            Final column index
	 * 
	 * @return A(r(:),j0:j1)
	 * 
	 * @exception ArrayIndexOutOfBoundsException
	 *                Submatrix indices
	 */
	public Matrix getMatrix(int[] r, int j0, int j1) {
		Matrix X = new Matrix(r.length, j1 - j0 + 1);
		double[][] B = X.getArray();

		try {
			for (int i = 0; i < r.length; i++) {
				for (int j = j0; j <= j1; j++) {
					B[i][j - j0] = A[r[i]][j];
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ArrayIndexOutOfBoundsException("Submatrix indices");
		}

		return X;
	}

	/**
	 * Get a submatrix.
	 * 
	 * @param r
	 *            Array of row indices.
	 * @param c
	 *            Array of column indices.
	 * 
	 * @return A(r(:),c(:))
	 * 
	 * @exception ArrayIndexOutOfBoundsException
	 *                Submatrix indices
	 */
	public Matrix getMatrix(int[] r, int[] c) {
		Matrix X = new Matrix(r.length, c.length);
		double[][] B = X.getArray();

		try {
			for (int i = 0; i < r.length; i++) {
				for (int j = 0; j < c.length; j++) {
					B[i][j] = A[r[i]][c[j]];
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ArrayIndexOutOfBoundsException("Submatrix indices");
		}

		return X;
	}

	/**
	 * Get row dimension.
	 * 
	 * @return m, the number of rows.
	 */
	public int getRowDimension() {
		return m;
	}

	/**
	 * Make a one-dimensional row packed copy of the internal array.
	 * 
	 * @return Matrix elements packed in a one-dimensional array by rows.
	 */
	public double[] getRowPackedCopy() {
		double[] vals = new double[m * n];

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				vals[(i * n) + j] = A[i][j];
			}
		}

		return vals;
	}

	/**
	 * Generate identity matrix
	 * 
	 * @param m
	 *            Number of rows.
	 * @param n
	 *            Number of colums.
	 * 
	 * @return An m-by-n matrix with ones on the diagonal and zeros elsewhere.
	 */
	public static Matrix identity(int m, int n) {
		Matrix A = new Matrix(m, n);
		double[][] X = A.getArray();

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				X[i][j] = ((i == j) ? 1.0 : 0.0);
			}
		}

		return A;
	}

	/**
	 * Matrix inverse or pseudoinverse
	 * 
	 * @return inverse(A) if A is square, pseudoinverse otherwise.
	 */
	public Matrix inverse() {
		return solve(identity(m, m));
	}

	/**
	 * Returns the length of the vector Creation date: (2001-10-21 20.57.43)
	 * 
	 * @return double
	 * 
	 * @exception java.lang.IllegalArgumentException
	 *                The exception description.
	 */
	public double length() throws java.lang.IllegalArgumentException {
		double length;

		if (this.getColumnDimension() != 1) {
			throw new IllegalArgumentException(
					"Vector Lengths can only be applied to vectors. There can only be one column.");
		}

		if (this.getRowDimension() != 3) {
			throw new IllegalArgumentException(
					"The length method currently applies only to three dimensional vectors");
		}

		//
		length = Math.sqrt((this.get(0, 0) * this.get(0, 0))
				+ (this.get(1, 0) * this.get(1, 0))
				+ (this.get(2, 0) * this.get(2, 0)));

		return length;
	}

	/**
	 * LU Decomposition
	 * 
	 * @return LUDecomposition
	 * 
	 * @see LUDecomposition
	 */
	public LUDecomposition lu() {
		return new LUDecomposition(this);
	}

	/**
	 * C = A - B
	 * 
	 * @param B
	 *            another matrix
	 * 
	 * @return A - B
	 */
	public Matrix minus(Matrix B) {
		checkMatrixDimensions(B);

		Matrix X = new Matrix(m, n);
		double[][] C = X.getArray();

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				C[i][j] = A[i][j] - B.A[i][j];
			}
		}

		return X;
	}

	/**
	 * A = A - B
	 * 
	 * @param B
	 *            another matrix
	 * 
	 * @return A - B
	 */
	public Matrix minusEquals(Matrix B) {
		checkMatrixDimensions(B);

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				A[i][j] = A[i][j] - B.A[i][j];
			}
		}

		return this;
	}

	/**
	 * One norm
	 * 
	 * @return maximum column sum.
	 */
	public double norm1() {
		double f = 0;

		for (int j = 0; j < n; j++) {
			double s = 0;

			for (int i = 0; i < m; i++) {
				s += Math.abs(A[i][j]);
			}

			f = Math.max(f, s);
		}

		return f;
	}

	/**
	 * Two norm
	 * 
	 * @return maximum singular value.
	 */
	public double norm2() {
		return (new SingularValueDecomposition(this).norm2());
	}

	/**
	 * Frobenius norm
	 * 
	 * @return sqrt of sum of squares of all elements.
	 */
	public double normF() {
		double f = 0;

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				f = Maths.hypot(f, A[i][j]);
			}
		}

		return f;
	}

	/**
	 * Infinity norm
	 * 
	 * @return maximum row sum.
	 */
	public double normInf() {
		double f = 0;

		for (int i = 0; i < m; i++) {
			double s = 0;

			for (int j = 0; j < n; j++) {
				s += Math.abs(A[i][j]);
			}

			f = Math.max(f, s);
		}

		return f;
	}

	/**
	 * C = A + B
	 * 
	 * @param B
	 *            another matrix
	 * 
	 * @return A + B
	 */
	public Matrix plus(Matrix B) {
		checkMatrixDimensions(B);

		Matrix X = new Matrix(m, n);
		double[][] C = X.getArray();

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				C[i][j] = A[i][j] + B.A[i][j];
			}
		}

		return X;
	}

	/**
	 * A = A + B
	 * 
	 * @param B
	 *            another matrix
	 * 
	 * @return A + B
	 */
	public Matrix plusEquals(Matrix B) {
		checkMatrixDimensions(B);

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				A[i][j] = A[i][j] + B.A[i][j];
			}
		}

		return this;
	}

	/**
	 * Print the matrix to stdout. Line the elements up in columns with a
	 * Fortran-like 'Fw.d' style format.
	 * 
	 * @param w
	 *            Column width.
	 * @param d
	 *            Number of digits after the decimal.
	 */
	public void print(int w, int d) {
		print(new PrintWriter(System.out, true), w, d);
	}

	/**
	 * Print the matrix to the output stream. Line the elements up in columns
	 * with a Fortran-like 'Fw.d' style format.
	 * 
	 * @param output
	 *            Output stream.
	 * @param w
	 *            Column width.
	 * @param d
	 *            Number of digits after the decimal.
	 */
	public void print(PrintWriter output, int w, int d) {
		DecimalFormat format = new DecimalFormat();
		format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
		format.setMinimumIntegerDigits(1);
		format.setMaximumFractionDigits(d);
		format.setMinimumFractionDigits(d);
		format.setGroupingUsed(false);
		print(output, format, w + 2);
	}

	// DecimalFormat is a little disappointing coming from Fortran or C's
	// printf.
	// Since it doesn't pad on the left, the elements will come out different
	// widths. Consequently, we'll pass the desired column width in as an
	// argument and do the extra padding ourselves.

	/**
	 * Print the matrix to the output stream. Line the elements up in columns.
	 * Use the format object, and right justify within columns of width
	 * characters. Note that is the matrix is to be read back in, you probably
	 * will want to use a NumberFormat that is set to US Locale.
	 * 
	 * @param output
	 *            the output stream.
	 * @param format
	 *            A formatting object to format the matrix elements
	 * @param width
	 *            Column width.
	 * 
	 * @see java.text.DecimalFormat#setDecimalFormatSymbols
	 */
	public void print(PrintWriter output, NumberFormat format, int width) {
		output.println(); // start on new line.

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				String s = format.format(A[i][j]); // format the number
				int padding = Math.max(1, width - s.length()); // At _least_ 1
																// space

				for (int k = 0; k < padding; k++) {
					output.print(' ');
				}

				output.print(s);
			}

			output.println();
		}

		output.println(); // end with blank line.
	}

	/**
	 * Print the matrix to stdout. Line the elements up in columns. Use the
	 * format object, and right justify within columns of width characters. Note
	 * that is the matrix is to be read back in, you probably will want to use a
	 * NumberFormat that is set to US Locale.
	 * 
	 * @param format
	 *            A Formatting object for individual elements.
	 * @param width
	 *            Field width for each column.
	 * 
	 * @see java.text.DecimalFormat#setDecimalFormatSymbols
	 */
	public void print(NumberFormat format, int width) {
		print(new PrintWriter(System.out, true), format, width);
	}

	/**
	 * QR Decomposition
	 * 
	 * @return QRDecomposition
	 * 
	 * @see QRDecomposition
	 */
	public QRDecomposition qr() {
		return new QRDecomposition(this);
	}

	/**
	 * Generate matrix with random elements
	 * 
	 * @param m
	 *            Number of rows.
	 * @param n
	 *            Number of colums.
	 * 
	 * @return An m-by-n matrix with uniformly distributed random elements.
	 */
	public static Matrix random(int m, int n) {
		Matrix A = new Matrix(m, n);
		double[][] X = A.getArray();

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				X[i][j] = Math.random();
			}
		}

		return A;
	}

	/**
	 * Matrix rank
	 * 
	 * @return effective numerical rank, obtained from SVD.
	 */
	public int rank() {
		return new SingularValueDecomposition(this).rank();
	}

	/**
	 * Read a matrix from a stream. The format is the same the print method, so
	 * printed matrices can be read back in (provided they were printed using US
	 * Locale). Elements are separated by whitespace, all the elements for each
	 * row appear on a single line, the last row is followed by a blank line.
	 * 
	 * @param input
	 *            the input stream.
	 */
	public static Matrix read(BufferedReader input) throws java.io.IOException {
		StreamTokenizer tokenizer = new StreamTokenizer(input);

		// Although StreamTokenizer will parse numbers, it doesn't recognize
		// scientific notation (E or D); however, Double.valueOf does.
		// The strategy here is to disable StreamTokenizer's number parsing.
		// We'll only get whitespace delimited words, EOL's and EOF's.
		// These words should all be numbers, for Double.valueOf to parse.
		tokenizer.resetSyntax();
		tokenizer.wordChars(0, 255);
		tokenizer.whitespaceChars(0, ' ');
		tokenizer.eolIsSignificant(true);

		java.util.Vector v = new java.util.Vector();

		// Ignore initial empty lines
		while (tokenizer.nextToken() == StreamTokenizer.TT_EOL) {
			;
		}

		if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
			throw new java.io.IOException("Unexpected EOF on matrix read.");
		}

		do {
			v.addElement(Double.valueOf(tokenizer.sval)); // Read & store 1st
															// row.
		} while (tokenizer.nextToken() == StreamTokenizer.TT_WORD);

		int n = v.size(); // Now we've got the number of columns!
		double[] row = new double[n];

		for (int j = 0; j < n; j++) // extract the elements of the 1st row.
		{
			row[j] = ((Double) v.elementAt(j)).doubleValue();
		}

		v.removeAllElements();
		v.addElement(row); // Start storing rows instead of columns.

		while (tokenizer.nextToken() == StreamTokenizer.TT_WORD) {
			// While non-empty lines
			v.addElement(row = new double[n]);

			int j = 0;

			do {
				if (j >= n) {
					throw new java.io.IOException("Row " + v.size()
							+ " is too long.");
				}

				row[j++] = Double.valueOf(tokenizer.sval).doubleValue();
			} while (tokenizer.nextToken() == StreamTokenizer.TT_WORD);

			if (j < n) {
				throw new java.io.IOException("Row " + v.size()
						+ " is too short.");
			}
		}

		int m = v.size(); // Now we've got the number of rows.
		double[][] A = new double[m][];
		v.copyInto(A); // copy the rows out of the vector

		return new Matrix(A);
	}

	/**
	 * Set a single element.
	 * 
	 * @param i
	 *            Row index.
	 * @param j
	 *            Column index.
	 * @param s
	 *            A(i,j).
	 * 
	 * @exception ArrayIndexOutOfBoundsException
	 */
	public void set(int i, int j, double s) {
		A[i][j] = s;
	}

	/**
	 * Set the complete data.
	 * 
	 * @param X
	 *            other matrix
	 */
	public void setMatrix(Matrix X) {
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				A[i][j] = X.get(i, j);
			}
		}
	}

	/**
	 * Set a submatrix.
	 * 
	 * @param i0
	 *            Initial row index
	 * @param i1
	 *            Final row index
	 * @param j0
	 *            Initial column index
	 * @param j1
	 *            Final column index
	 * @param X
	 *            A(i0:i1,j0:j1)
	 * 
	 * @exception ArrayIndexOutOfBoundsException
	 *                Submatrix indices
	 */
	public void setMatrix(int i0, int i1, int j0, int j1, Matrix X) {
		try {
			for (int i = i0; i <= i1; i++) {
				for (int j = j0; j <= j1; j++) {
					A[i][j] = X.get(i - i0, j - j0);
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ArrayIndexOutOfBoundsException("Submatrix indices");
		}
	}

	/**
	 * Set a submatrix.
	 * 
	 * @param i0
	 *            Initial row index
	 * @param i1
	 *            Final row index
	 * @param c
	 *            Array of column indices.
	 * @param X
	 *            A(i0:i1,c(:))
	 * 
	 * @exception ArrayIndexOutOfBoundsException
	 *                Submatrix indices
	 */
	public void setMatrix(int i0, int i1, int[] c, Matrix X) {
		try {
			for (int i = i0; i <= i1; i++) {
				for (int j = 0; j < c.length; j++) {
					A[i][c[j]] = X.get(i - i0, j);
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ArrayIndexOutOfBoundsException("Submatrix indices");
		}
	}

	/**
	 * Set a submatrix.
	 * 
	 * @param r
	 *            Array of row indices.
	 * @param j0
	 *            Initial column index
	 * @param j1
	 *            Final column index
	 * @param X
	 *            A(r(:),j0:j1)
	 * 
	 * @exception ArrayIndexOutOfBoundsException
	 *                Submatrix indices
	 */
	public void setMatrix(int[] r, int j0, int j1, Matrix X) {
		try {
			for (int i = 0; i < r.length; i++) {
				for (int j = j0; j <= j1; j++) {
					A[r[i]][j] = X.get(i, j - j0);
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ArrayIndexOutOfBoundsException("Submatrix indices");
		}
	}

	/**
	 * Set a submatrix.
	 * 
	 * @param r
	 *            Array of row indices.
	 * @param c
	 *            Array of column indices.
	 * @param X
	 *            A(r(:),c(:))
	 * 
	 * @exception ArrayIndexOutOfBoundsException
	 *                Submatrix indices
	 */
	public void setMatrix(int[] r, int[] c, Matrix X) {
		try {
			for (int i = 0; i < r.length; i++) {
				for (int j = 0; j < c.length; j++) {
					A[r[i]][c[j]] = X.get(i, j);
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ArrayIndexOutOfBoundsException("Submatrix indices");
		}
	}

	/**
	 * Solve AX = B
	 * 
	 * @param B
	 *            right hand side
	 * 
	 * @return solution if A is square, least squares solution otherwise
	 */
	public Matrix solve(Matrix B) {
		return ((m == n) ? (new LUDecomposition(this)).solve(B)
				: (new QRDecomposition(this)).solve(B));
	}

	/**
	 * Solve XA = B, which is also A'X' = B'
	 * 
	 * @param B
	 *            right hand side
	 * 
	 * @return solution if A is square, least squares solution otherwise.
	 */
	public Matrix solveTranspose(Matrix B) {
		return transpose().solve(B.transpose());
	}

	/**
	 * Singular Value Decomposition
	 * 
	 * @return SingularValueDecomposition
	 * 
	 * @see SingularValueDecomposition
	 */
	public SingularValueDecomposition svd() {
		return new SingularValueDecomposition(this);
	}

	/**
	 * Multiply a matrix by a scalar, C = sA
	 * 
	 * @param s
	 *            scalar
	 * 
	 * @return sA
	 */
	public Matrix times(double s) {
		Matrix X = new Matrix(m, n);
		double[][] C = X.getArray();

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				C[i][j] = s * A[i][j];
			}
		}

		return X;
	}

	/**
	 * Linear algebraic matrix multiplication, A B
	 * 
	 * @param B
	 *            another matrix
	 * 
	 * @return Matrix product, A B
	 * 
	 * @exception IllegalArgumentException
	 *                Matrix inner dimensions must agree.
	 */
	public Matrix times(Matrix B) {
		if (B.m != n) {
			throw new IllegalArgumentException(
					"Matrix inner dimensions must agree.");
		}

		Matrix X = new Matrix(m, B.n);
		double[][] C = X.getArray();
		double[] Bcolj = new double[n];

		for (int j = 0; j < B.n; j++) {
			for (int k = 0; k < n; k++) {
				Bcolj[k] = B.A[k][j];
			}

			for (int i = 0; i < m; i++) {
				double[] Arowi = A[i];
				double s = 0;

				for (int k = 0; k < n; k++) {
					s += (Arowi[k] * Bcolj[k]);
				}

				C[i][j] = s;
			}
		}

		return X;
	}

	/**
	 * Multiply a matrix by a scalar in place, A = sA
	 * 
	 * @param s
	 *            scalar
	 * 
	 * @return replace A by sA
	 */
	public Matrix timesEquals(double s) {
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				A[i][j] = s * A[i][j];
			}
		}

		return this;
	}

	/**
	 * Matrix trace.
	 * 
	 * @return sum of the diagonal elements.
	 */
	public double trace() {
		double t = 0;

		for (int i = 0; i < Math.min(m, n); i++) {
			t += A[i][i];
		}

		return t;
	}

	/**
	 * Matrix transpose.
	 * 
	 * @return A'
	 */
	public Matrix transpose() {
		Matrix X = new Matrix(n, m);
		double[][] C = X.getArray();

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				C[j][i] = A[i][j];
			}
		}

		return X;
	}

	/**
	 * Unary minus
	 * 
	 * @return -A
	 */
	public Matrix uminus() {
		Matrix X = new Matrix(m, n);
		double[][] C = X.getArray();

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				C[i][j] = -A[i][j];
			}
		}

		return X;
	}

	/**
	 * This method calculates the vectorProduct between two matrices. The
	 * matrices must be three dimensional. Creation date: (2001-10-21 19.46.07)
	 * Jonas Forssell
	 * 
	 * @param a
	 *            Jama.Matrix
	 * @param b
	 *            Jama.Matrix
	 * 
	 * @return result_vector Jama.Matrix
	 * 
	 * @exception java.lang.IllegalArgumentException
	 *                The exception description.
	 */
	public Matrix vectorProduct(Jama.Matrix B)
			throws java.lang.IllegalArgumentException {
		//
		Matrix result_vector = new Matrix(3, 1);

		//
		if ((B.getColumnDimension() != 1) || (this.getColumnDimension() != 1)) {
			throw new IllegalArgumentException(
					"Vector Products can only be applied to vectors. There can only be one column.");
		}

		if ((B.getRowDimension() != 3) || (this.getRowDimension() != 3)) {
			throw new IllegalArgumentException(
					"Matrix inner dimensions must agree.");
		}

		//
		// Calculate the vector product term by term.
		result_vector.set(0, 0, (this.get(1, 0) * B.get(2, 0))
				- (B.get(1, 0) * this.get(2, 0)));
		result_vector.set(1, 0, (this.get(2, 0) * B.get(0, 0))
				- (B.get(2, 0) * this.get(0, 0)));
		result_vector.set(2, 0, (this.get(0, 0) * B.get(1, 0))
				- (B.get(0, 0) * this.get(1, 0)));

		// Now, return the completed vector.
		return result_vector;
	}

	/**
	 * The following methods are custom written to allow patching of variable
	 * values during synchronization of replicated objects in the cluster
	 * version of Impact.
	 * 
	 * They are replacements and extensions to methods provided by default in
	 * the JavaParty environment.
	 * 
	 */

	public void appendTo(ToString s) {
		s.append("A", A);
	}

	// Implementation of the Patchable interface
	public void createPatch(Object _copy, uka.patch.PatchOutput po)
			throws java.io.IOException {
		Matrix copy = (Matrix) _copy;

		if (this.n != copy.n) {
			throw new AssertionError("Matrix dimension has changed");
		}

		if (this.m != copy.m) {
			throw new AssertionError("Matrix dimension has changed");
		}

		for (int row = 0; row < getRowDimension(); row++) {
			for (int col = 0; col < getColumnDimension(); col++) {
				if (po.writeDiff(this.get(row, col) - copy.get(row, col), 0.0d)) {
					copy.set(row, col, this.get(row, col));
				}
			}
		}
	}

	public void applyPatch(Object _copy, uka.patch.PatchInput pi)
			throws java.io.IOException, ClassNotFoundException {
		Matrix copy = (Matrix) _copy;

		// Assertion
		//
		// if (((PatchReader) pi).objects.findID(A) != -1) {
		// throw new
		// AssertionError("A reference to the private matrix state was given away");
		// }

		if (this.n != copy.n) {
			throw new AssertionError("Matrix dimension has changed");
		}

		if (this.m != copy.m) {
			throw new AssertionError("Matrix dimension has changed");
		}

		for (int row = 0; row < getRowDimension(); row++) {
			for (int col = 0; col < getColumnDimension(); col++) {
				if (pi.hasDiff()) {
					double dd = pi.getDiffAsDouble();
					this.set(row, col, this.get(row, col) + dd);
					copy.set(row, col, copy.get(row, col) + dd);
				}
			}
		}
	}

	public Object clone() {
		// return a deep copy
		return copy();
	}

	public void descendReferences(uka.patch.ReferenceConsumer c)
			throws java.io.IOException {
		// empty
	}

	public void filterReferences(uka.patch.ReferenceFilter f) {
	}

	// *****************************************
	// Implementation of the Transportable interface

	public static final uka.transport.TransportDescriptor TRANSPORT_DESCRIPTOR = new uka.transport.TransportDescriptor() {
		public Object unmarshalReference(uka.transport.UnmarshalStream s, int id)
				throws java.io.IOException, ClassNotFoundException {
			return new Matrix(s, id);
		}

		public boolean unmarshal(Object obj, uka.transport.UnmarshalStream s)
				throws ClassNotFoundException, java.io.IOException {
			((Matrix) obj).unmarshal(s);
			return false;
		}

		public void marshalReference(Object obj, uka.transport.MarshalStream s)
				throws java.io.IOException {
			((Matrix) obj).marshalReference(s);
		}

		public void marshal(Object obj, uka.transport.MarshalStream s)
				throws java.io.IOException {
			((Matrix) obj).marshal(s);
		}

		public Object deepClone(Object orig, int id,
				uka.transport.DeepClone _helper)
				throws CloneNotSupportedException {
			Matrix c = ((Matrix) orig).copy();
			_helper.add(id, c);
			return c;

		}

		public boolean deepCloneReferences(Object orig, Object copy,
				uka.transport.DeepClone _helper)
				throws CloneNotSupportedException {
			return false;
		}

		public Class getType() {
			return Matrix.class;
		}
	};

	public uka.transport.TransportDescriptor getTransportDescriptor() {
		return TRANSPORT_DESCRIPTOR;
	}

	private static final int _SIZE = uka.transport.BasicIO.SIZEOF_int
			+ uka.transport.BasicIO.SIZEOF_int;

	public Matrix(uka.transport.UnmarshalStream _stream, int _id)
			throws java.io.IOException, ClassNotFoundException {
		_stream.register(this, _id);
	}

	public void unmarshal(uka.transport.UnmarshalStream _stream)
			throws java.io.IOException, ClassNotFoundException {
		_stream.request(Matrix._SIZE);
		byte[] _buffer = _stream.getBuffer();
		int _pos = _stream.getPosition();
		this.n = uka.transport.BasicIO.extractInt(_buffer, _pos);
		_pos += uka.transport.BasicIO.SIZEOF_int;
		this.m = uka.transport.BasicIO.extractInt(_buffer, _pos);
		_pos += uka.transport.BasicIO.SIZEOF_int;
		_stream.accept(Matrix._SIZE);

		this.A = uka.transport.ValueIO.readValueArrayArrayDouble(_stream);
	}

	public void marshalReference(uka.transport.MarshalStream _stream)
			throws java.io.IOException {
	}

	public void marshal(uka.transport.MarshalStream _stream)
			throws java.io.IOException {
		_stream.reserve(Matrix._SIZE);
		byte[] _buffer = _stream.getBuffer();
		int _pos = _stream.getPosition();
		_pos = uka.transport.BasicIO.insert(_buffer, _pos, this.n);
		_pos = uka.transport.BasicIO.insert(_buffer, _pos, this.m);
		_stream.deliver(_SIZE);

		uka.transport.ValueIO.writeValue(_stream, this.A);
	}

	public Object flatClone() {
		return copy();
	}

}

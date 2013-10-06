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

package run.elements;

import run.*;

// FILE: c:/temp/krockpackage/Solid.java
import java.util.*;

/**
 * A class that represents ...
 * 
 * @author Jonas Forssell, Yuriy Mikhaylovskiy
 * 
 * @see OtherClasses
 */
public class Solid_Iso_6HG extends Element {
	private Material[] material;
	private Jama.Matrix D;
	private int number_of_integration_points;
	private double xsi;
	private double phi;
	private double etha;
	private double AHR;
	private Jama.Matrix H;
	private Jama.Matrix M;
	private Jama.Matrix N;
	private Jama.Matrix B;
	private Jama.Matrix J;
	private Jama.Matrix J_inv;
	private Jama.Matrix strain;
	private Jama.Matrix dstrain;
	private Jama.Matrix stress;
	private Jama.Matrix[] hforce;

	private double[][] elemp;

	private Jama.Matrix d;
	private Jama.Matrix f;
	private Jama.Matrix P;
	private double[] W;
	private boolean AHR_is_set;
	private boolean NIP_is_set;
	private boolean Nodes_are_set;
	private boolean Material_is_set;

	/**
	 * This is the Solid Isoparametric element. The element can have one or
	 * eight integration points. Creation date: (2001-08-10 19:46:30)
	 */
	public Solid_Iso_6HG() {
		super();
		type = new String("SOLID_ISO_6HG");

		int i;

		//
		material = new Material[1];

		//
		xsi = 0;
		etha = 0;
		phi = 0;
		node = new Node[8];
		W = new double[1];
		H = new Jama.Matrix(6, 9);
		M = new Jama.Matrix(8, 3);
		d = new Jama.Matrix(24, 1);
		f = new Jama.Matrix(24, 1);

		B = new Jama.Matrix(6, 24);

		D = new Jama.Matrix(3, 8);

		J = new Jama.Matrix(3, 3);

		J_inv = new Jama.Matrix(3, 3);

		P = new Jama.Matrix(9, 9);
		N = new Jama.Matrix(9, 24);

		strain = new Jama.Matrix(6, 1);
		//
		dstrain = new Jama.Matrix(6, 1);
		//
		stress = new Jama.Matrix(6, 1);

		hforce = new Jama.Matrix[8];

		for (i = 0; i < 8; i++) {
			hforce[i] = new Jama.Matrix(3, 1);
		}

		// Setting default value for number of integration points
		number_of_integration_points = 1;
	}

	/**
	 * This method calculates the mass matrix of the element. The matrix is
	 * lumped, i.e. the mass is concentrated to the element nodes. There is a
	 * consistent way of determining the mass distribution by using the HRZ
	 * Lumping Scheme 1. Compute the diagonal coefficients of the consistent
	 * mass matrix 2. Compute the total mass of the element m 3. Compute a
	 * number s by adding the diagonal coefficients Mii associated with
	 * translational d.o.f (but not rotational d.o.f, if any) that are mutually
	 * parallell and in the same direction 4. Scale all the diagonal
	 * coefficients by multiplying them by the ratio m/s, thus preserving the
	 * total mass of the element. The mass matrix m = integral
	 * (densityN_transposeNdV) This integral can be translated into a gauss
	 * point summation. Creation date: (25/12/01 %T)
	 */
	public void assembleMassMatrix() throws IllegalArgumentException {
		int i;
		int j;
		int k;
		Jama.Matrix mass;
		double total_mass;
		double s;
		double det_j;

		// Initialize mass
		mass = new Jama.Matrix(24, 24);
		total_mass = 0;
		s = 0;

		/*
		 * This element has 8 nodes and with 3 direction each. This gives a
		 * 24x24 matrix. The matrix will be different for each integration
		 * point.
		 */

		// Define M (the node coordinate matrix)
		for (j = 0; j < 8; j++) {
			M.set(j, 0, node[j].getX_pos());
			M.set(j, 1, node[j].getY_pos());
			M.set(j, 2, node[j].getZ_pos());
		}

		// Now, start by computing N, via D
		// Compute D. Use only one integration point. Then xsi=phi=etha=0
		calculateD(0, 0, 0);

		// Create the N matrix by expanding the D matrix
		calculateN();

		// Now, do the matrix calculation to derive the jacobian.
		J = D.times(M);
		det_j = J.det();

		// Sum up the mass contribution (The weight function for one gauss point
		// is 2^3 = 8)
		mass = N.transpose().times(N).times(material[0].getDensity())
				.times(det_j).times(8.0);

		// Keep only the diagonal elements
		for (j = 0; j < 24; j++) {
			for (k = 0; k < 24; k++) {
				mass.set(j, k, (j == k) ? mass.get(j, k) : 0);
			}
		}

		/*
		 * Now, we have the 24x24 mass matrix. Move on to step 2, compute total
		 * mass of the element. The total mass is equal to the volume integrand
		 * of density. Gauss translation means
		 * sum[density*jacobian*weightfunction] over all integration points.
		 */
		// Compute D
		// calculateD(0, 0, 0);

		// Now, do the matrix calculation to derive the jacobian.
		// J = D.times(M);

		// Sum up the mass contribution (Weight function 2^3 = 8)
		total_mass = material[0].getDensity() * det_j * 8;

		// Check mass for element
		if (total_mass <= 0) {
			throw new IllegalArgumentException(
					"Error in Solid Hexahedron Element "
							+ number
							+ ". Element mass is zero or negative.\nMaterial density: "
							+ material[0].getDensity());
		}

		if (det_j <= 0) {
			throw new IllegalArgumentException(
					"Error in Solid Hexahedron Element "
							+ number
							+ ". Element Volume is zero or negative. Check node defintion in indata file.");
		}

		// Third stage is to compute a scaling factor s using u-direction for
		// all nodes
		for (i = 0; i < 8; i++) {
			s += mass.get(3 * i, 0);
		}

		// Fourth stage is to scale all diagonal coefficients using total_mass/s
		// as a scaling factor
		for (i = 0; i < 24; i++) {
			mass.set(i, i, (mass.get(i, i) * total_mass) / s);
		}

		// We now have a lumped mass matrix stored in the mass matrix. It must
		// be distributed to the nodes.
		// Use only the u-component. Assume the others are the same. (They
		// should be)
		for (i = 0; i < 8; i++) {
			node[i].addMass(mass.get(3 * i, 3 * i) / 8.0);
		}

		// That's it! Finish.
	}

	/**
	 * Insert the method's description here. Creation date: (25/12/01 %T)
	 */
	public void calculateContactForces() {
	}

	/**
	 * Calculates the D-matrix for integration point i Creation date: (05/01/02
	 * %T)
	 * 
	 * @param integration_point
	 *            int
	 */
	private void calculateD(double xsi, double phi, double etha) {
		// Calculate the D-matrix for integration point i
		// WAS CHANGED TO REFLECT PARAMETRIC COORDINATES OF HOURGLASS
		D.set(0, 0, -(1.0 / 8));
		D.set(0, 1, +(1.0 / 8));
		D.set(0, 2, +(1.0 / 8));
		D.set(0, 3, -(1.0 / 8));
		D.set(0, 4, -(1.0 / 8));
		D.set(0, 5, +(1.0 / 8));
		D.set(0, 6, +(1.0 / 8));
		D.set(0, 7, -(1.0 / 8));

		//
		D.set(1, 0, -(1.0 / 8));
		D.set(1, 1, -(1.0 / 8));
		D.set(1, 2, (1.0 / 8));
		D.set(1, 3, (1.0 / 8));
		D.set(1, 4, -(1.0 / 8));
		D.set(1, 5, -(1.0 / 8));
		D.set(1, 6, (1.0 / 8));
		D.set(1, 7, (1.0 / 8));

		//
		D.set(2, 0, -(1.0 / 8));
		D.set(2, 1, -(1.0 / 8));
		D.set(2, 2, -(1.0 / 8));
		D.set(2, 3, -(1.0 / 8));
		D.set(2, 4, (1.0 / 8));
		D.set(2, 5, (1.0 / 8));
		D.set(2, 6, (1.0 / 8));
		D.set(2, 7, (1.0 / 8));
	}

	/**
	 * Insert the method's description here. Creation date: (25/12/01 %T)
	 */
	public void calculateExternalForces(double currtime) {
	}

	/**
	 * Calculates the N-matrix for integration point i Creation date: (05/01/02
	 * %T)
	 */
	private void calculateN() {
		// Calculate the N-matrix for integration point i by expanding the
		// D-matrix
		N.setMatrix(0, 2, 0, 0, D.getMatrix(0, 2, 0, 0));
		N.setMatrix(3, 5, 1, 1, D.getMatrix(0, 2, 0, 0));
		N.setMatrix(6, 8, 2, 2, D.getMatrix(0, 2, 0, 0));
		N.setMatrix(0, 2, 3, 3, D.getMatrix(0, 2, 1, 1));
		N.setMatrix(3, 5, 4, 4, D.getMatrix(0, 2, 1, 1));
		N.setMatrix(6, 8, 5, 5, D.getMatrix(0, 2, 1, 1));
		N.setMatrix(0, 2, 6, 6, D.getMatrix(0, 2, 2, 2));
		N.setMatrix(3, 5, 7, 7, D.getMatrix(0, 2, 2, 2));
		N.setMatrix(6, 8, 8, 8, D.getMatrix(0, 2, 2, 2));
		N.setMatrix(0, 2, 9, 9, D.getMatrix(0, 2, 3, 3));
		N.setMatrix(3, 5, 10, 10, D.getMatrix(0, 2, 3, 3));
		N.setMatrix(6, 8, 11, 11, D.getMatrix(0, 2, 3, 3));
		N.setMatrix(0, 2, 12, 12, D.getMatrix(0, 2, 4, 4));
		N.setMatrix(3, 5, 13, 13, D.getMatrix(0, 2, 4, 4));
		N.setMatrix(6, 8, 14, 14, D.getMatrix(0, 2, 4, 4));
		N.setMatrix(0, 2, 15, 15, D.getMatrix(0, 2, 5, 5));
		N.setMatrix(3, 5, 16, 16, D.getMatrix(0, 2, 5, 5));
		N.setMatrix(6, 8, 17, 17, D.getMatrix(0, 2, 5, 5));
		N.setMatrix(0, 2, 18, 18, D.getMatrix(0, 2, 6, 6));
		N.setMatrix(3, 5, 19, 19, D.getMatrix(0, 2, 6, 6));
		N.setMatrix(6, 8, 20, 20, D.getMatrix(0, 2, 6, 6));
		N.setMatrix(0, 2, 21, 21, D.getMatrix(0, 2, 7, 7));
		N.setMatrix(3, 5, 22, 22, D.getMatrix(0, 2, 7, 7));
		N.setMatrix(6, 8, 23, 23, D.getMatrix(0, 2, 7, 7));
	}

	/**
	 * This method will calculate the nodal forces resulting from the stresses
	 * in the element. The forces are all added up in a force matrix f with the
	 * following format: f = [ fx1 fy1 fz1 fx2 fy2 ..... fz8 ] Creation date:
	 * (25/12/01 %T)
	 */
	public void calculateNodalForces(int dummy, double dt) {
		int n;
		int i, j, k, l;
		Jama.Matrix global_force;
		double A = 0;
		double vol = 0;
		double S0 = 0;
		double G = 0;
		double X[][];
		double V[][];
		double BB[][];
		double GS[][] = { { 1., 1., -1., -1., -1., -1., 1., 1. },
				{ 1., -1., -1., 1., -1., 1., 1., -1. },
				{ 1., -1., 1., -1., 1., -1., 1., -1. },
				{ -1., 1., -1., 1., 1., -1., 1., -1. } };
		double GB[][] = { { 1., 1., -1., -1., -1., -1., 1., 1. },
				{ 1., -1., -1., 1., -1., 1., 1., -1. },
				{ 1., -1., 1., -1., 1., -1., 1., -1. },
				{ -1., 1., -1., 1., 1., -1., 1., -1. } };

		/*
		 * The nodal force array is the sum of all integration point
		 * contributions times the gauss weight factor. In this particular
		 * element, where the order of gauss evaluation is 2, the weight factor
		 * is 1 and the multiplication is therefore not done. Otherwise, the
		 * expression would have been:
		 * 
		 * f =
		 * B.transpose().times(stress[i]).times(J.det()).times(weightfactor);
		 * 
		 * Since this method is called once for every integration point, this
		 * will happen. Start by calculating the force vector contribution
		 */
		f = B.transpose().times(stress).times(J.det()).times(8);

		/*
		 * Now, all the node contributions are represented in this matrix. It
		 * needs to be split up. Each node contribution must then be added to
		 * each node
		 */

		for (n = 0; n < 8; n++) {
			// Split up; extract a 3x1 matrix from the large matrix.
			// global_force = [ fxN fyN fzN ]
			global_force = f.getMatrix(3 * n, (3 * n) + 2, 0, 0);

			// Add to node (Internal forces are subtracted)
			node[n].addInternalForce(global_force.times(-1.0));
		}

		// the jama is realy time consuming and ineficient for small mtrices...
		// I prefer arrays, instead... Sorry.
		BB = B.getArray();
		vol = 8 * J.det();

		/*
		 * 
		 * GS=GB; // I don't know if this will work, but... for(i=0; i<8; i++) {
		 * for(j=0; i<3; j++) { A=A+BB[i][j]*BB[i][j]; S0=BB[i][j]/vol; for(k=0;
		 * k<4; k++) { //GS[i][k]=GB[i][k]; for(l=0; l<8; l++) { GS[i][k] =
		 * GS[i][k] - S0 * X[l][j]*GB[l][k]; } } } }
		 * 
		 * 
		 * // Shear modulus... Need to be used here... Please help in doing
		 * so... G=
		 * (material[0].getYoungsModulus())/(2*(1+(material[0].getNu())));
		 * 
		 * // Global time step, dt, is needed here... How to get it? // AHR is
		 * an user defined parameter to control HG... we need it together //
		 * with the material properties... A=
		 * dt*AHR*(material[0].getYoungsModulus()+G+G)*A/(24*vol); for(i=0; i<3;
		 * i++) { for(k=0; k<4; k++) { S0=0.; for( j=0; j<8; j++) { S0= S0 +
		 * GS[j][k]*V[i][j]; } S0=elemp[i][k]+A*S0; elemp[i][k]=S0; for( j=0;
		 * j<8; j++) { // We need to add the hour glass stabilization force to
		 * // the global force vector... The indices are a little confusing //
		 * but it works... // i is the direction (x, y or z) // j is the local
		 * node number // k is the assiciated HG mode //
		 * F[i][j]=F[i][j]+S0*GS[j][k];
		 * hforce[j].set(i,1,hforce[j].get(i,1)+S0*GS[j][k]); } } }
		 * 
		 * for (i=0; i<8; i++) node[i].addHourglassForce(hforce[i]);
		 */
	}

	/**
	 * Definition of strain vector is [exx eyy ezz gammaxy gammayz gammxz] The
	 * strain is derived as strain = B d where d is an array of node
	 * displacements d = [u1 v1 w1 u2 v2 w2 .... w8] The B-matrix is derived
	 * from three main matrices, H, J and D according to: strain = HPN where P
	 * expanded version of the inverted jacobian J N is an expanded version of D
	 * J is called the jacobian. It consists of an array M which includes the
	 * node coordinates and the D matrix again as: J = DM H is a coupling
	 * matrix. H is defined in the setInitialConditions method since it is not
	 * dependent on any variable. Note, this method is run several times. i is
	 * the number of the integration point currently run. Creation date:
	 * (25/12/01 %T)
	 * 
	 * @param tstep
	 *            double
	 */
	public void calculateStrain(double tstep, int i) {
		int j;

		// Calculate the D-matrix
		calculateD(xsi, phi, etha);

		// Create the N matrix by expanding the D matrix
		calculateN();

		// Update M (the node coordinate matrix)
		for (j = 0; j < 8; j++) {
			M.set(j, 0, node[j].getX_pos());
			M.set(j, 1, node[j].getY_pos());
			M.set(j, 2, node[j].getZ_pos());
		}

		// And determine the displacement matrix (difference between old, one
		// timestep ago and new node positions)
		for (j = 0; j < 8; j++) {
			d.set(3 * j, 0, node[j].getX_dpos());
			d.set((3 * j) + 1, 0, node[j].getY_dpos());
			d.set((3 * j) + 2, 0, node[j].getZ_dpos());
		}

		// Now, do the matrix calculation to derive the jacobian.
		J = D.times(M);

		// Now, create the P matrix by first inverting it..
		J_inv = J.inverse();

		// ... and then expanding it
		P.setMatrix(0, 2, 0, 2, J_inv);
		P.setMatrix(3, 5, 3, 5, J_inv);
		P.setMatrix(6, 8, 6, 8, J_inv);

		// Now, calculate the B matrix
		B = H.times(P.times(N));

		// Finally, calculate the strain increment array for this integration
		// point
		dstrain = B.times(d);
	}

	/**
	 * This method calculates the stresses in each integration point based on
	 * old strain, strain increment and old stress. The material law will update
	 * the strain and stress matrices to new values using constitutive law and
	 * strain increment dstrain. The stresses are: [Sxx Syy Szz Sxy Syz Sxz]T
	 * (or [sigmaxx sigmayy sigmazz tauxy tauyz tauxz]T). Note tauxz = tauzx and
	 * so on. Creation date: (25/12/01 %T)
	 */
	public void calculateStress(int i, double timestep) {
		// i is the current integration point.
		material[i].calculateStressThreeDimensional(strain, dstrain, stress,
				timestep);
	}

	/**
	 * The critical timestep for a solid element is related to the minimum
	 * distance through the element. This can normally be found by dividing the
	 * element volume by the largest side area. However, in this special case
	 * with a hexahedron, the easiest way is to to calculate the shortest edge
	 * length since this is always the critical length. Creation date: (25/12/01
	 * %T)
	 * 
	 * @param current_timestep
	 *            double
	 * 
	 * @return double
	 */
	public double checkTimestep(double current_timestep) {
		double critical_length;
		double timestep;
		double c;

		// Lower edges
		critical_length = Math
				.sqrt(((node[1].getX_pos() - node[0].getX_pos()) * (node[1]
						.getX_pos() - node[0].getX_pos()))
						+ ((node[1].getY_pos() - node[0].getY_pos()) * (node[1]
								.getY_pos() - node[0].getY_pos()))
						+ ((node[1].getZ_pos() - node[0].getZ_pos()) * (node[1]
								.getZ_pos() - node[0].getZ_pos())));

		c = Math.sqrt(((node[2].getX_pos() - node[1].getX_pos()) * (node[2]
				.getX_pos() - node[1].getX_pos()))
				+ ((node[2].getY_pos() - node[1].getY_pos()) * (node[2]
						.getY_pos() - node[1].getY_pos()))
				+ ((node[2].getZ_pos() - node[1].getZ_pos()) * (node[2]
						.getZ_pos() - node[1].getZ_pos())));

		if (c > 0)
			critical_length = Math.min(critical_length, c);

		c = Math.sqrt(((node[3].getX_pos() - node[2].getX_pos()) * (node[3]
				.getX_pos() - node[2].getX_pos()))
				+ ((node[3].getY_pos() - node[2].getY_pos()) * (node[3]
						.getY_pos() - node[2].getY_pos()))
				+ ((node[3].getZ_pos() - node[2].getZ_pos()) * (node[3]
						.getZ_pos() - node[2].getZ_pos())));

		if (c > 0)
			critical_length = Math.min(critical_length, c);

		c = Math.sqrt(((node[0].getX_pos() - node[3].getX_pos()) * (node[0]
				.getX_pos() - node[3].getX_pos()))
				+ ((node[0].getY_pos() - node[3].getY_pos()) * (node[0]
						.getY_pos() - node[3].getY_pos()))
				+ ((node[0].getZ_pos() - node[3].getZ_pos()) * (node[0]
						.getZ_pos() - node[3].getZ_pos())));

		// Upper edges
		if (c > 0)
			critical_length = Math.min(critical_length, c);

		c = Math.sqrt(((node[5].getX_pos() - node[4].getX_pos()) * (node[5]
				.getX_pos() - node[4].getX_pos()))
				+ ((node[5].getY_pos() - node[4].getY_pos()) * (node[5]
						.getY_pos() - node[4].getY_pos()))
				+ ((node[5].getZ_pos() - node[4].getZ_pos()) * (node[5]
						.getZ_pos() - node[4].getZ_pos())));

		if (c > 0)
			critical_length = Math.min(critical_length, c);

		c = Math.sqrt(((node[6].getX_pos() - node[5].getX_pos()) * (node[6]
				.getX_pos() - node[5].getX_pos()))
				+ ((node[6].getY_pos() - node[5].getY_pos()) * (node[6]
						.getY_pos() - node[5].getY_pos()))
				+ ((node[6].getZ_pos() - node[5].getZ_pos()) * (node[6]
						.getZ_pos() - node[5].getZ_pos())));

		if (c > 0)
			critical_length = Math.min(critical_length, c);

		c = Math.sqrt(((node[7].getX_pos() - node[6].getX_pos()) * (node[7]
				.getX_pos() - node[6].getX_pos()))
				+ ((node[7].getY_pos() - node[6].getY_pos()) * (node[7]
						.getY_pos() - node[6].getY_pos()))
				+ ((node[7].getZ_pos() - node[6].getZ_pos()) * (node[7]
						.getZ_pos() - node[6].getZ_pos())));

		if (c > 0)
			critical_length = Math.min(critical_length, c);

		c = Math.sqrt(((node[4].getX_pos() - node[7].getX_pos()) * (node[4]
				.getX_pos() - node[7].getX_pos()))
				+ ((node[4].getY_pos() - node[7].getY_pos()) * (node[4]
						.getY_pos() - node[7].getY_pos()))
				+ ((node[4].getZ_pos() - node[7].getZ_pos()) * (node[4]
						.getZ_pos() - node[7].getZ_pos())));

		if (c > 0)
			critical_length = Math.min(critical_length, c);

		// Vertical edges
		c = Math.sqrt(((node[4].getX_pos() - node[0].getX_pos()) * (node[4]
				.getX_pos() - node[0].getX_pos()))
				+ ((node[4].getY_pos() - node[0].getY_pos()) * (node[4]
						.getY_pos() - node[0].getY_pos()))
				+ ((node[4].getZ_pos() - node[0].getZ_pos()) * (node[4]
						.getZ_pos() - node[0].getZ_pos())));

		if (c > 0)
			critical_length = Math.min(critical_length, c);

		c = Math.sqrt(((node[5].getX_pos() - node[1].getX_pos()) * (node[5]
				.getX_pos() - node[1].getX_pos()))
				+ ((node[5].getY_pos() - node[1].getY_pos()) * (node[5]
						.getY_pos() - node[1].getY_pos()))
				+ ((node[5].getZ_pos() - node[1].getZ_pos()) * (node[5]
						.getZ_pos() - node[1].getZ_pos())));

		if (c > 0)
			critical_length = Math.min(critical_length, c);

		c = Math.sqrt(((node[6].getX_pos() - node[2].getX_pos()) * (node[6]
				.getX_pos() - node[2].getX_pos()))
				+ ((node[6].getY_pos() - node[2].getY_pos()) * (node[6]
						.getY_pos() - node[2].getY_pos()))
				+ ((node[6].getZ_pos() - node[2].getZ_pos()) * (node[6]
						.getZ_pos() - node[2].getZ_pos())));

		if (c > 0)
			critical_length = Math.min(critical_length, c);

		c = Math.sqrt(((node[7].getX_pos() - node[3].getX_pos()) * (node[7]
				.getX_pos() - node[3].getX_pos()))
				+ ((node[7].getY_pos() - node[3].getY_pos()) * (node[7]
						.getY_pos() - node[3].getY_pos()))
				+ ((node[7].getZ_pos() - node[3].getZ_pos()) * (node[7]
						.getZ_pos() - node[3].getZ_pos())));

		if (c > 0)
			critical_length = Math.min(critical_length, c);

		//
		timestep = critical_length
				/ material[0].wavespeedThreeDimensional(0.0, 0.0);

		// Now, return the smallest of the suggested and the calculated
		// timestep.
		// The smallest element will decide the timestep for the whole
		// simulation.
		return Math.min(timestep, current_timestep);
	}

	/**
	 * Insert the method's description here. Creation date: (26/12/01 %T)
	 * 
	 * @return int
	 */
	public int getNumberOfIntegrationPoints() {
		return number_of_integration_points;
	}

	/**
	 * Insert the method's description here. Creation date: (25/12/01 %T)
	 * 
	 * @param arg1
	 *            java.lang.String
	 * @param arg2
	 *            java.lang.String
	 * @param arg3
	 *            java.lang.String
	 * @param lineno
	 *            int
	 * @param nodelist
	 *            java.util.Vector
	 * @param materiallist
	 *            java.util.Vector
	 */
	public void parse_Fembic(Token[] param, int lineno, RplVector nodelist,
			RplVector materiallist, RplVector loadlist, Hashtable nodetable)
			throws java.text.ParseException {
		int nodenumber;
		int j;
		int i = 0;

		while (i < param.length) {
			// The nodes of the element are defined
			if (param[i].getw().toUpperCase().equals("NODES")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// Assume now that the nodes are delivered in param3, with the
				// format
				// [nodenr,nodenr,nodenr,nodenr,nodenr,nodenr,nodenr,nodenr]
				if (!param[i + 2].getw().toUpperCase().startsWith("[")
						|| !param[i + 2].getw().toUpperCase().endsWith("]")) {
					throw new java.text.ParseException(
							"Error, node number definition should be [nodenr1,nodenr2,nodenr3,nodenr4,nodenr5,nodenr6,nodenr7,nodenr8]",
							lineno);
				}

				// Ok, now find the numbers
				try {
					for (j = 0; j < 8; j++) {
						node[j] = super
								.findNode(
										super.getNodeNumber(j + 1,
												param[i + 2].getw()), nodetable);
					}
				} catch (IllegalArgumentException e) {
					throw new java.text.ParseException(e.getMessage(), lineno);
				}

				i += 3;
				Nodes_are_set = true;
			} else
			// The material of the element is defined
			if (param[i].getw().toUpperCase().equals("MATERIAL")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// Assume now that the material name is delivered in param3
				try {
					// We want the handle to the material object.
					material[0] = super.findMaterial(param[i + 2].getw()
							.toUpperCase(), materiallist);
					i += 3;
					Material_is_set = true;
				} catch (IllegalArgumentException e) {
					throw e;
				}
			} else
			// The cross section area of the element is defined
			if (param[i].getw().toUpperCase().equals("NIP")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in param4. Set this in
				// the element
				number_of_integration_points = (int) param[i + 2].getn();
				i += 3;
				NIP_is_set = true;
			} else
			// The cross section area of the element is defined
			if (param[i].getw().toUpperCase().equals("AHR")
					&& param[i + 1].getw().toUpperCase().equals("=")) {
				// The value of the cross section area is in param4. Set this in
				// the element
				AHR = param[i + 2].getn();
				i += 3;
				AHR_is_set = true;
			} else {
				// Neither material or nodes are defined. Then the parameter is
				// wrong.
				throw new java.text.ParseException(
						"Unknown Solid (Hexahedran) element parameter ", lineno);
			}
		}
	}

	/**
	 * Checks that all mandatory variables have been set
	 */
	public void checkIndata() throws IllegalArgumentException {
		// Check that all the required parameters have been parsed
		if (!Nodes_are_set) {
			throw new IllegalArgumentException(
					"No nodes defined for Solid_Iso_6 element nr" + number);
		}

		if (!Material_is_set) {
			throw new IllegalArgumentException(
					"No Material defined for Solid_Iso_6 element nr" + number);
		}

		if (!AHR_is_set) {
			throw new IllegalArgumentException(
					"No Hourglass parameter AHR set for Solid_Iso_6HG element nr"
							+ number);
		}
	}

	/**
	 * 
	 * The method checks destroyed element or not. If the element is destroyed
	 * variable failed = true else failed = false.
	 * 
	 */
	public void checkIfFailed() {
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
	 *            int
	 * @param gpn
	 *            int
	 * 
	 * @return java.lang.String
	 */
	public String print_Gid(int ctrl, int gpn) {
		String out;

		switch (ctrl) {
		case MESH_HEADER:

			/*
			 * Print the header for the mesh. In this case, the type of element
			 * is Hexahedra (6-sided solid element) and it uses 8 nodes
			 */
			out = new String("MESH \"MeshType" + type
					+ "\" Dimension 3 ElemType Hexahedra Nnode 8\n");

			return out;

		case MESH:

			/* Print the element number and connected nodes */
			out = new String(number + "\t" + node[0].getNumber() + "\t"
					+ node[1].getNumber() + "\t" + node[2].getNumber() + "\t"
					+ node[3].getNumber() + "\t" + node[4].getNumber() + "\t"
					+ node[5].getNumber() + "\t" + node[6].getNumber() + "\t"
					+ node[7].getNumber() + "\n");

			return out;

		case RESULT_HEADER:

			/*
			 * Print the header of the result file, for the block of Hexahedra
			 * (solid) elements. The element has eight gauss points where the
			 * results are calculated. They are placed on the natural positions
			 * which is where the gauss coordinates are most accurate.
			 */
			out = new String("GaussPoints \"Type" + type
					+ "\" ElemType Hexahedra \"MeshType" + type + "\"\n");
			out += ("Number Of Gauss Points: " + number_of_integration_points + "\n");
			out += "Nodes Not Included\n"; // There are no gauss points in the
											// nodes.
			out += "Natural Coordinates: Given\n"; // They are on the optimum
													// gauss coordinates
													// instead, which GID will
													// know by default when this
													// switch is set to
													// internal.

			if (number_of_integration_points == 1) {
				out += "0.0 0.0 0.0\n";
			} else if (number_of_integration_points == 8) {
				out += "-0.57735 -0.57735 -0.57735\n";
				out += "0.57735 -0.57735 -0.57735\n";
				out += "0.57735 0.57735 -0.57735\n";
				out += "-0.57735 0.57735 -0.57735\n";
				out += "-0.57735 -0.57735 0.57735\n";
				out += "0.57735 -0.57735 0.57735\n";
				out += "0.57735 0.57735 0.57735\n";
				out += "-0.57735 0.57735 0.57735\n";
			}

			out += "End GaussPoints\n";

			return out;

		case RESULT_SUB_HEADER:

			/*
			 * Print the subheader for the resultfile to initate each block of
			 * data from this element type First parameter: Kind of results ( 1=
			 * scalar, 2=vector, 3=matrix, 4=2D plane deformation matrix, 5=Main
			 * stresser, 6=Euler angles (for local axes) Second param: Location
			 * of the data (1= on the nodes, 2= in the gauss points); Third
			 * param: Will there be a description on the results button in GID?
			 * (0= Make one up GID! 1= Yes, a description will be given) Fourth
			 * param: Specify the name of the gauss point set that will be used
			 * "name"
			 */
			out = new String(" 3 2 0 \"Type" + type + "\"\n");

			return out;

		case RESULT_STRESS_GLOBAL:

			/*
			 * Print the Gauss stresses for this element and the requested gauss
			 * point.
			 */
			if (gpn == 0) {
				out = new String(number + " "); // Element number must start the
												// first gauss point results
			} else {
				out = new String(""); // The rest of the lines will have no
										// initial element number
			}

			/*
			 * 
			 * // The result is printed as [Sxx Syy Szz Sxy Syz Sxz] out +=
			 * (stress[gpn].get(0, 0) + "\t" + stress[gpn].get(1, 0) + "\t" +
			 * stress[gpn].get(2, 0) + "\t" + stress[gpn].get(3, 0) + "\t" +
			 * stress[gpn].get(4, 0) + "\t" + stress[gpn].get(5, 0) + "\n");
			 */
			return out;

		case RESULT_STRAIN_GLOBAL:

			/*
			 * Print the Gauss stresses for this element and the requested gauss
			 * point.
			 */
			if (gpn == 0) {
				out = new String(number + " "); // Element number must start the
												// first gauss point results
			} else {
				out = new String(""); // The rest of the lines will have no
										// initial element number
			}

			/*
			 * // The result is printed as [Sxx Syy Szz Sxy Syz Sxz] out +=
			 * (strain[gpn].get(0, 0) + "\t" + strain[gpn].get(1, 0) + "\t" +
			 * strain[gpn].get(2, 0) + "\t" + strain[gpn].get(3, 0) + "\t" +
			 * strain[gpn].get(4, 0) + "\t" + strain[gpn].get(5, 0) + "\n");
			 */
			return out;

		default:
			return new String("");
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
	public String print_Fembic(int ctrl, int gpn) {
		String out;

		switch (ctrl) {

		case MESH:

			/* Print the element number and connected nodes */
			out = new String(number + "\t nodes = [" + node[0].getNumber()
					+ "," + node[1].getNumber() + "," + node[2].getNumber()
					+ "," + node[3].getNumber() + "," + node[4].getNumber()
					+ "," + node[5].getNumber() + "," + node[6].getNumber()
					+ "," + node[7].getNumber() + "]\t" + "material = "
					+ material[0].getName() + "\t");

			if (NIP_is_set)
				out += " nip = " + number_of_integration_points;

			out += "\n";

			return out;

		default:
			return new String("");
		}
	}

	/**
	 * Insert the method's description here. Creation date: (27/09/01 %T)
	 */
	public void setInitialConditions() throws IllegalArgumentException {
		double t = 1.0 / Math.sqrt(3);
		int i;
		int j;

		/*
		 * Make a local copy of the material object and keep it inside the
		 * element. The reason for this is twofold. 1. The material law is now
		 * tied to the element and will remember the element properties and
		 * history 2. Since the material is cloned now, after Initialize, all
		 * the parameters are defined in the material object and are now
		 * automatically transferred to the local copy.
		 * 
		 * Note that one copy of the materiallaw is required for each
		 * intergration point in the element.
		 */
		try {
			for (i = 0; i < number_of_integration_points; i++) {
				material[i] = (Material) material[0].copy();
			}
		} catch (CloneNotSupportedException e) {
			System.err.println("Object cannot clone");
		}

		// Now call any necessary initialisations in the law.
		for (i = 0; i < number_of_integration_points; i++) {
			material[i].setInitialConditions();
		}

		//
		// Define all important matrices
		//
		// For a single gauss point per element

		/*
		 * if (number_of_integration_points == 1) { xsi[0] = 0; etha[0] = 0;
		 * phi[0] = 0; W[0] = 2.0; } else // For eight gauss points per element
		 * { xsi[0] = -t; xsi[1] = -t; xsi[2] = -t; xsi[3] = -t; xsi[4] = t;
		 * xsi[5] = t; xsi[6] = t; xsi[7] = t;
		 * 
		 * // etha[0] = -t; etha[1] = -t; etha[2] = t; etha[3] = t; etha[4] =
		 * -t; etha[5] = -t; etha[6] = t; etha[7] = t;
		 * 
		 * // phi[0] = t; phi[1] = -t; phi[2] = -t; phi[3] = t; phi[4] = t;
		 * phi[5] = -t; phi[6] = -t; phi[7] = t;
		 * 
		 * // W[0] = 1.0; W[1] = 1.0; W[2] = 1.0; W[3] = 1.0; W[4] = 1.0; W[5] =
		 * 1.0; W[6] = 1.0; W[7] = 1.0; }
		 */
		H.set(0, 0, 1.0);
		H.set(0, 1, 0);
		H.set(0, 2, 0);
		H.set(0, 3, 0);
		H.set(0, 4, 0);
		H.set(0, 5, 0);
		H.set(0, 6, 0);
		H.set(0, 7, 0);
		H.set(0, 8, 0);

		//
		H.set(1, 0, 0);
		H.set(1, 1, 0);
		H.set(1, 2, 0);
		H.set(1, 3, 0);
		H.set(1, 4, 1.0);
		H.set(1, 5, 0);
		H.set(1, 6, 0);
		H.set(1, 7, 0);
		H.set(1, 8, 0);

		//
		H.set(2, 0, 0);
		H.set(2, 1, 0);
		H.set(2, 2, 0);
		H.set(2, 3, 0);
		H.set(2, 4, 0);
		H.set(2, 5, 0);
		H.set(2, 6, 0);
		H.set(2, 7, 0);
		H.set(2, 8, 1.0);

		//
		H.set(3, 0, 0);
		H.set(3, 1, 1.0);
		H.set(3, 2, 0);
		H.set(3, 3, 1.0);
		H.set(3, 4, 0);
		H.set(3, 5, 0);
		H.set(3, 6, 0);
		H.set(3, 7, 0);
		H.set(3, 8, 0);

		//
		H.set(4, 0, 0);
		H.set(4, 1, 0);
		H.set(4, 2, 0);
		H.set(4, 3, 0);
		H.set(4, 4, 0);
		H.set(4, 5, 1.0);
		H.set(4, 6, 0);
		H.set(4, 7, 1.0);
		H.set(4, 8, 0);

		//
		H.set(5, 0, 0);
		H.set(5, 1, 0);
		H.set(5, 2, 1.0);
		H.set(5, 3, 0);
		H.set(5, 4, 0);
		H.set(5, 5, 0);
		H.set(5, 6, 1.0);
		H.set(5, 7, 0);
		H.set(5, 8, 0);

		// Set all elements in N to zero
		for (i = 0; i < 9; i++) {
			for (j = 0; j < 24; j++) {
				N.set(i, j, 0.0);
			}
		}

		// Set all elements in P to zero
		for (i = 0; i < 9; i++) {
			for (j = 0; j < 9; j++) {
				P.set(i, j, 0.0);
			}
		}
	}

	/**
	 * This method calculates the local coordinate system for the element. The
	 * method returns a handle to the system and this matrix is then stored in
	 * the local_coordinate_system for later use. The matrix can be used in the
	 * transformation of displacements and forces between the local and global
	 * coordinates. However, in this element, the transformation is made
	 * automatically in the matrix algebra of calculating the element strains
	 * (they are derived in global directions) Creation date: (25/12/01 %T)
	 */
	public void updateLocalCoordinateSystem() {
	}

}

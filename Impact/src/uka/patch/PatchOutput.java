/* ********************************************************************
 * KaRMI
 *
 * Copyright (C) 1998-2003 The JavaParty Team, University of Karlsruhe
 *
 * Permission is hereby granted to use and modify this software.
 * The software, or modifications thereof, may be redistributed only
 * if the source code is also provided and this copyright notice stays 
 * attached.
 **********************************************************************/

package uka.patch;

import uka.transport.MarshalStream;

import java.io.IOException;

/**
 * Inferface for creating object patches in an abstract way. An object
 * implementing {@link PatchOutput} is used as target in the
 * {@link Patchable#createPatch(Object, PatchOutput)} method of a
 * {@link Patchable patchable} object. The {@link Patchable patchable} object
 * calls the corresponding {@link #writeDiff(int, int)} method for each of its
 * instance variables and passes the corresponding value from its backup copy as
 * second parameter.
 */
public interface PatchOutput {
	/** @see #writeDiff(int, int) */
	public boolean writeDiff(boolean v, boolean c) throws IOException;

	/** @see #writeDiff(int, int) */
	public boolean writeDiff(byte v, byte c) throws IOException;

	/** @see #writeDiff(int, int) */
	public boolean writeDiff(char v, char c) throws IOException;

	/** @see #writeDiff(int, int) */
	public boolean writeDiff(short v, short c) throws IOException;

	/**
	 * @param v
	 *            the value of currently inspected instance variable of a
	 *            {@link Patchable patchable} object.
	 * 
	 * @param c
	 *            the value of the corresponding instance variable of the backup
	 *            copy.
	 * 
	 * @return whether a difference was detected and a patch record was written.
	 *         If there is a difference, the instance variable of the backup
	 *         copy needs to be updated to the current value.
	 */
	public boolean writeDiff(int v, int c) throws IOException;

	/** @see #writeDiff(int, int) */
	public boolean writeDiff(float v, float c) throws IOException;

	/** @see #writeDiff(int, int) */
	public boolean writeDiff(long v, long c) throws IOException;

	/** @see #writeDiff(int, int) */
	public boolean writeDiff(double v, double c) throws IOException;

	/**
	 * Like {@link #writeDiff(int, int)} for instance variables of reference
	 * type. But instead of a modification notification the new reference
	 * (normally the one that was passed as first argument) is returned. In case
	 * of partial replication, there is an exception to that rule: If the
	 * application has decided not to replicate an object on the node, where it
	 * was created on, the reference is deleted during update. In that case,
	 * this method will return <code>null</code> insted. The responsibility of
	 * the {@link Patchable#createPatch(Object, PatchOutput)} method is to
	 * assign the returned reference to the corresponding instance variables of
	 * the original and the backup copy.
	 * 
	 * @see #writeDiff(int, int)
	 * 
	 * @return the value of parameter <code>r</code>, or null, if <code>r</code>
	 *         is not being replicated on the local node.
	 */
	public Object writeDiff(Object r, Object c) throws IOException;

	public void createPatchAnonymous(Object r, Object c) throws IOException;

	/**
	 * Experimental low-level API: Patch creation for the expert.
	 */
	public MarshalStream getOutput(int rank) throws IOException;
}

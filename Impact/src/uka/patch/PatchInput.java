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

import uka.transport.UnmarshalStream;

import java.io.IOException;

/**
 * Interface for reading patches in an abstract way and applying them to
 * {@link Patchable patchable} objects.
 */
public interface PatchInput {
	/**
	 * Checks whether there is a patch record available for the next instance
	 * variable.
	 * 
	 * @see Patchable#applyPatch
	 */
	public boolean hasDiff() throws IOException, ClassNotFoundException;

	/** @see #getDiffAsInt */
	public boolean getDiffAsBoolean() throws IOException;

	/** @see #getDiffAsInt */
	public byte getDiffAsByte() throws IOException;

	/** @see #getDiffAsInt */
	public char getDiffAsChar() throws IOException;

	/** @see #getDiffAsInt */
	public short getDiffAsShort() throws IOException;

	/**
	 * Reads the updated value, if a patch record is available.
	 * 
	 * @see #hasDiff
	 * @see Patchable#applyPatch
	 */
	public int getDiffAsInt() throws IOException;

	/** @see #getDiffAsInt */
	public float getDiffAsFloat() throws IOException;

	/** @see #getDiffAsInt */
	public long getDiffAsLong() throws IOException;

	/** @see #getDiffAsInt */
	public double getDiffAsDouble() throws IOException;

	/**
	 * Reads the updated reference value, if the last call to {@link #hasDiff}
	 * returned <code>true</code>. The returned object reference is assigned to
	 * the currently patched instance variable of the {@link Patchable
	 * patchable} object and the corresponding instance variable of its backup
	 * copy.
	 */
	public Object getDiffAsObject() throws IOException, ClassNotFoundException;

	public void applyPatchAnonymous(Object r, Object c) throws IOException,
			ClassNotFoundException;

	/**
	 * Experimental low-level API.
	 * 
	 * @return the underlying stream for raw unmarshaling.
	 */
	public UnmarshalStream getInput() throws IOException;

	/**
	 * Experimental low-level API.
	 * 
	 * @return the rank that sent the corresponding patch.
	 */
	public int getFromRank() throws IOException;
}

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

import java.io.IOException;

/**
 * Consumes all references produced by a {@link ReferenceProducer}. This
 * interface is used by a {@link PatchAdapter} to traverse {@link Patchable}
 * object graphs.
 */
public interface ReferenceConsumer {
	/**
	 * Consumes all references announced by the
	 * {@link ReferenceProducer#descendReferences(ReferenceConsumer)} method.
	 */
	public void descend(Object ref) throws IOException;
}

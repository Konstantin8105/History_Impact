/* ********************************************************************
 * KaRMI
 *
 * Copyright (C) 1998-2002 The JavaParty Team, University of Karlsruhe
 *
 * Permission is hereby granted to use and modify this software.
 * The software, or modifications thereof, may be redistributed only
 * if the source code is also provided and this copyright notice stays 
 * attached.
 **********************************************************************/
/*
 * $Revision: 1.1 $
 * $Date: 2005/07/05 20:19:08 $
 */

package uka.karmi.rmi.server;

public abstract class RemoteStub implements uka.transport.Transportable {

	/**
	 * Default contructor, transportable classes have no implicit default
	 * constructor.
	 */
	public RemoteStub() {
	}

}
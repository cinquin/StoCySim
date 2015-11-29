/****************************************************************************
 * StoCySim Copyright (c) 2013-2015 Cinquin Lab.
 * All rights reserved. This code is made available under a dual license:
 * the two-clause BSD license or the GNU Public License v2.
 ***************************************************************************/

package uk.org.cinquin.stochastic_integrator;

public interface SimulatedEntity {
	public abstract void setZombie();
	public abstract boolean isZombie();
	public abstract void terminate();
}

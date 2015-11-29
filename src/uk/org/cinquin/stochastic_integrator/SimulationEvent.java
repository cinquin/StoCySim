/****************************************************************************
 * StoCySim Copyright (c) 2013-2015 Cinquin Lab.
 * All rights reserved. This code is made available under a dual license:
 * the two-clause BSD license or the GNU Public License v2.
 ***************************************************************************/

package uk.org.cinquin.stochastic_integrator;

import java.lang.reflect.Method;
import java.util.Collection;

public class SimulationEvent {

	@FunctionalInterface
	public interface Action {
		public void doAction();
	}

	public SimulationEvent(double eventTime, Action action, SimulatedEntity entity) {
		this.eventTime = eventTime;
		this.action = action;
		this.entity = entity;
	}

	public SimulationEvent(Action action, SimulatedEntity entity) {
		this.action = action;
		this.entity = entity;
		this.eventTime = Float.NaN;
	}

	/**
	 * Simulation time at which the event should occur.
	 */
	public double eventTime;

	/**
	 * Action to perform when simulation time reaches {@link #eventTime}.
	 */
	public final Action action;

	public final SimulatedEntity entity;

	/**
	 * NOT YET IMPLEMENTED
	 * Method that takes the current time as an argument, current environmental
	 * conditions, and returns an updated time at which the event will occur.
	 * The method is provided by the associated simulation object.
	 */
	public Method recomputeEventTime;

	/**
	 * NOT YET IMPLEMENTED
	 * Environmental conditions that the evenTime is dependent on. If those conditions
	 * change, the eventTime needs to be adjusted and will be recomputed by a call
	 * to {@link #recomputeEventTime}.
	 */
	public Collection<Class<? /*extends EnvironmentalCondition*/>> conditionDependencies;

}

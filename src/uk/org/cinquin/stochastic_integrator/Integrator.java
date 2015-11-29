/****************************************************************************
 * StoCySim Copyright (c) 2013-2015 Cinquin Lab.
 * All rights reserved. This code is made available under a dual license:
 * the two-clause BSD license or the GNU Public License v2.
 ***************************************************************************/

package uk.org.cinquin.stochastic_integrator;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;


public class Integrator implements SimulatedEntity {
	
	public static int MEAN_STARTING_CAPACITY = 846;
	public static double MEAN_ON_TIME = 2;
	public static double MEAN_OFF_TIME = 2;
	public static double LOSS_RATE = 9.4;
	//TODO Control seeds of random number generators underlying the
	//following distributions
	static AbstractRealDistribution distOn = 
			new LogNormalDistribution(MEAN_ON_TIME, MEAN_ON_TIME);
	static AbstractRealDistribution distOff = 
			new LogNormalDistribution(MEAN_OFF_TIME, MEAN_OFF_TIME);
	static NormalDistribution distStartCapacity = 
			new NormalDistribution(MEAN_STARTING_CAPACITY, 0.12 * MEAN_STARTING_CAPACITY);

	public final boolean startedOn;
	private boolean on;
	private double lastTimeOn = 0;
	public double remainingCapacity = distStartCapacity.sample();
		
	@Override
	public void setZombie() {
	}

	@Override
	public boolean isZombie() {
		return false;
	}
	
	private Simulation sim;
	
	public Integrator(Simulation sim, boolean startedOn) {
		sim.add(this);
		this.startedOn = startedOn;
		on = startedOn;
		this.sim = sim;
		if (startedOn) {
			sim.addEvent(new SimulationEvent(() -> {turnOff();}, this), getTimeToSpendInOnState());
		} else {
			sim.addEvent(new SimulationEvent(() -> {turnOn();}, this), getTimeToSpendInOffState());
		}
		if (startedOn) {
			lastTimeOn = sim.getCurrentTime();
		} else {
			lastTimeOn = Float.NaN;
		}
	}
	
	private static double getTimeToSpendInOnState() {
		return Math.abs(distOn.sample());
	}
	
	private static double getTimeToSpendInOffState() {
		return Math.abs(distOff.sample());
	}
	
	private void turnOn() {
		on = true;
		lastTimeOn = sim.getCurrentTime();
		sim.addEvent(new SimulationEvent(() -> {turnOff();}, this), getTimeToSpendInOnState());
		//System.err.println("On: " + remainingCapacity + " at time " + sim.getCurrentTime());
	}
	
	private void turnOff() {
		if (!on) {
			throw new AssertionFailedException();
		}
		on = false;
		final double timeToSpendOff = getTimeToSpendInOffState();
		sim.addEvent(new SimulationEvent(() -> {turnOn();}, this), timeToSpendOff);
		remainingCapacity -= (sim.getCurrentTime() - lastTimeOn) * LOSS_RATE;
		if (remainingCapacity < 0) {
			remainingCapacity = 0;
		}
		//System.err.println("Off: " + remainingCapacity + " at time " + sim.getCurrentTime() +
		//		"; will wake up in " + timeToSpendOff);
		lastTimeOn = Float.NaN;
	}
	
	public void updateCapacity() {
		if (on) {
			remainingCapacity -= (sim.getCurrentTime() - lastTimeOn) * LOSS_RATE;
			//System.err.println("Update capacity: " + remainingCapacity + " at time " +
			//sim.getCurrentTime());
			if (remainingCapacity < 0) {
				remainingCapacity = 0;
			}
			lastTimeOn = sim.getCurrentTime();
		}
	}

	@Override
	public void terminate() {
		updateCapacity();
	}
}

/****************************************************************************
 * StoCySim Copyright (c) 2013-2015 Cinquin Lab.
 * All rights reserved. This code is made available under a dual license:
 * the two-clause BSD license or the GNU Public License v2.
 ***************************************************************************/

package uk.org.cinquin.stochastic_integrator;

import static org.junit.Assert.assertTrue;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.junit.Test;

public class SimulationTest {
	
	private final SecureRandom random = new SecureRandom();
	
	//TODO Reduce code duplication across tests
	
	//TODO Make a class that holds all simulation parameters
	//(the cost will be that each entity takes a slightly larger amount
	//of memory because of the extra reference; but it's probably better
	//than dealing with the complications introduced by static variables).

	@Test
	public void test0LossRate() {
		final double savedLossRate = Integrator.LOSS_RATE;
		Integrator.LOSS_RATE = 0;

		try {
			Simulation sim = new Simulation();
			sim.setup();
			Map<Integrator, Consumer<Integrator>> integratorsNoLoss = new HashMap<>();

			for (int l = 0; l < 10; l++) {
				Integrator integrator = new Integrator(sim, random.nextDouble() < 0.5);
				final double capacity = integrator.remainingCapacity;
				integratorsNoLoss.put(integrator, i -> assertTrue(i.remainingCapacity == capacity));
			}

			sim.runUntil(100);
			sim.terminate();
			integratorsNoLoss.forEach((i, check) -> check.accept(i));	
		} finally {
			Integrator.LOSS_RATE = savedLossRate;
		}
	}

	@Test
	public void testAlwaysOff() {
		AbstractRealDistribution distOff = Integrator.distOff;
		try {
			Integrator.distOff = new NormalDistribution() {

				private static final long serialVersionUID = 3873787060050503441L;

				@Override
				public double sample() {
					return Double.POSITIVE_INFINITY;
				}
			};
			Simulation sim = new Simulation();
			sim.setup();
			Map<Integrator, Consumer<Integrator>> integratorsNoLoss = new HashMap<>();

			for (int l = 0; l < 10; l++) {
				Integrator integrator = new Integrator(sim, false);
				final double capacity = integrator.remainingCapacity;
				integratorsNoLoss.put(integrator, i -> assertTrue(i.remainingCapacity == capacity));
			}

			sim.runUntil(100);
			sim.terminate();
			integratorsNoLoss.forEach((i, check) -> check.accept(i));	
		} finally {
			Integrator.distOff = distOff; 
		}
	}

	
	@Test
	public void testAlwaysOn() {
		AbstractRealDistribution distOff = Integrator.distOff;
		try {
			Integrator.distOff = new NormalDistribution() {

				private static final long serialVersionUID = 3873787060050503441L;

				@Override
				public double sample() {
					return 0;
				}
			};
			Simulation sim = new Simulation();
			sim.setup();
			Map<Integrator, Consumer<Integrator>> integratorsConstLoss = new HashMap<>();
			
			double runLength = (Integrator.MEAN_STARTING_CAPACITY * 0.5) / Integrator.LOSS_RATE;

			for (int l = 0; l < 100; l++) {
				Integrator integrator = new Integrator(sim, true);
				integrator.remainingCapacity = Integrator.MEAN_STARTING_CAPACITY;
				final double capacity = integrator.remainingCapacity;
				integratorsConstLoss.put(integrator, i -> assertTrue(
						i.remainingCapacity + " vs " + (capacity * 0.5),
						Math.abs(i.remainingCapacity - capacity * 0.5) < 1E-10));
			}

			sim.runUntil((float) runLength);
			sim.terminate();
			integratorsConstLoss.forEach((i, check) -> check.accept(i));
		} finally {
			Integrator.distOff = distOff; 
		}
	}
	
	@Test
	//Note that this test will fail at a low frequency
	//TODO Make tests that verify that distribution of values collected
	//across multiple simulation runs is as expected
	public void testOnHalfTime() {
		Simulation sim = new Simulation();
		sim.setup();
		Map<Integrator, Consumer<Integrator>> integrators = new HashMap<>();

		double runLength = (Integrator.MEAN_STARTING_CAPACITY * 0.1) / Integrator.LOSS_RATE;

		for (int l = 0; l < 100_000; l++) {
			Integrator integrator = new Integrator(sim, random.nextDouble() < 0.5);
			integrator.remainingCapacity = Integrator.MEAN_STARTING_CAPACITY;
			integrators.put(integrator, null);
		}

		sim.runUntil((float) runLength);
		sim.terminate();
		double averageCapacity = integrators.keySet().stream().
				mapToDouble(i -> i.remainingCapacity).average().getAsDouble();
		assertTrue(averageCapacity + " vs " + (Integrator.MEAN_STARTING_CAPACITY * 0.95),
				Math.abs(averageCapacity - Integrator.MEAN_STARTING_CAPACITY * 0.95) < 
					0.5 /* Leave some leeway so account for noise */);
	}
	
	@Test
	//Note that this test will fail at a low frequency
	public void testUpdateCapacity() {
		Simulation sim = new Simulation();
		sim.setup();
		Map<Integrator, Consumer<Integrator>> integrators = new HashMap<>();

		double runLength = (Integrator.MEAN_STARTING_CAPACITY * 0.1) / Integrator.LOSS_RATE;

		for (int l = 0; l < 100_000; l++) {
			Integrator integrator = new Integrator(sim, random.nextDouble() < 0.5);
			integrator.remainingCapacity = Integrator.MEAN_STARTING_CAPACITY;
			integrators.put(integrator, null);
		}

		for (int it = 0; it < 10; it ++) {
			sim.runUntil((float) runLength * it / 9);
			for (Integrator i: integrators.keySet()) {
				i.updateCapacity();
			}
		}
		sim.terminate();
		double averageCapacity = integrators.keySet().stream().
				mapToDouble(i -> i.remainingCapacity).average().getAsDouble();
		assertTrue(averageCapacity + " vs " + (Integrator.MEAN_STARTING_CAPACITY * 0.95),
				Math.abs(averageCapacity - Integrator.MEAN_STARTING_CAPACITY * 0.95) < 
					0.5 /* Leave some leeway so account for noise */);
	}

}

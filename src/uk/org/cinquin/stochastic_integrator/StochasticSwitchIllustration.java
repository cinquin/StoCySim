/****************************************************************************
 * StoCySim Copyright (c) 2013-2015 Cinquin Lab.
 * All rights reserved. This code is made available under a dual license:
 * the two-clause BSD license or the GNU Public License v2.
 ***************************************************************************/

package uk.org.cinquin.stochastic_integrator;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;

public class StochasticSwitchIllustration {
		
	public static void main(String[] args) {
		
		Simulation sim = new Simulation();
		sim.setup();
		List<Integrator> integrators = new ArrayList<>();

		for (int i = 0; i < 10_000 ; i++) {
			integrators.add(new Integrator(sim, Math.random() <= 0.5));
		}

		for (float time = 0; time <= 24 * 2; time += 24) {
			sim.runUntil(time);

			for (Integrator i: integrators) {
				i.updateCapacity();
			}
			
			//Collect stats here if desired
		}
		
		sim.terminate();
				
		DoubleSummaryStatistics stats = 
				integrators.stream().mapToDouble(i -> i.remainingCapacity).summaryStatistics();
		final double average = stats.getAverage();
		System.out.println("Average reproductive capacity after 2 days of cycling: " + average);
		
		final double var = integrators.stream().mapToDouble(i -> Math.pow(
					i.remainingCapacity - average, 2)).
				summaryStatistics().getAverage();
				
		System.out.println("CV in reproductive capacity: " + Math.pow(var, 0.5) / average);

	}

}

/****************************************************************************
 * StoCySim Copyright (c) 2013-2015 Cinquin Lab.
 * All rights reserved. This code is made available under a dual license:
 * the two-clause BSD license or the GNU Public License v2.
 ***************************************************************************/

package uk.org.cinquin.stochastic_integrator;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

public class Simulation {

	static final SecureRandom secureRandom = new SecureRandom();
	public final Random randomNumberGenerator = new Random();
	{
		randomNumberGenerator.setSeed(secureRandom.nextLong());
	}

	private final FibonacciHeap<SimulationEvent> simulationEvents = new FibonacciHeap<>();
	private List<SimulatedEntity> entities = new ArrayList<>();

	private int maxPopulationSize = Integer.MAX_VALUE;

	private long currentStep;
	protected long birthCounter;

	public Simulation() {
	}

	public int getCurrentNEntities() {
		return entities.size();
	}

	public List<SimulatedEntity> getEntities() {
		return entities;
	}

	private void nextStep() {
		FibonacciHeapNode<SimulationEvent> nextEvent = simulationEvents.removeMin();
		if (nextEvent == null)
			throw new RuntimeException("Simulation finished early at time " + currentTime + " and " + currentStep
					+ " steps (population extinction?)");
		SimulationEvent event = nextEvent.getData();
		if (event.entity != null && event.entity.isZombie())
			return;
		if (event.eventTime < currentTime)
			throw new AssertionFailedException();
		currentTime = event.eventTime;
		currentStep++;
		event.action.doAction();
	}
	
	public void terminate() {
		for (SimulatedEntity e: entities) {
			e.terminate();
		}
	}

	/**
	 * Add a future event to the simulation. The time specified in e should be
	 * absolute.
	 * 
	 * @param e
	 */
	public final void addEvent(SimulationEvent e) {
		if (Double.isNaN(e.eventTime)) {
			throw new IllegalArgumentException();
		}
		simulationEvents.insert(new FibonacciHeapNode<SimulationEvent>(e), e.eventTime);
	}
	
	/**
	 * Add a future event to the simulation. The time specified in e should be relative to the current time,
	 * and will be updated using current simulation time.
	 * @param e
	 * @param d
	 */
	public final void addEvent(SimulationEvent e, double relativeTime) {
		e.eventTime = relativeTime + getCurrentTime();
		addEvent(e);
	}

	/*
	 * TODO Change the constructor and callers so setup can be done from there
	 */
	public void setup() {
		if (entities instanceof ArrayList<?> && maxPopulationSize < Integer.MAX_VALUE)
			((ArrayList<?>) entities).ensureCapacity(maxPopulationSize);
	}

	private double currentTime = 0;

	public double getCurrentTime() {
		return currentTime;
	}

	public void run(long nSteps) {
		while (currentStep < nSteps && !done) {
			nextStep();
		}
	}

	public final void runUntil(float endTime) {
		//Make sure simulation time stops right at endTime
		//This could be important for entities that update their
		//state when the simulation is terminated
		addEvent(new SimulationEvent(endTime, () -> {}, null));
		while (currentTime < endTime && !done) {
			nextStep();
		}
	}

	@FunctionalInterface
	public interface AddEntity {
		public void addEntity(Simulation sim, SimulatedEntity e);
	}

	@FunctionalInterface
	public interface ShouldContinue {
		public boolean shouldContinue(Simulation sim);
	}

	protected ShouldContinue shouldContinueMethod;

	public static final AddEntity randomlyDiscardExcess = (Simulation sim, SimulatedEntity simulatedEntity) -> {
		sim.birthCounter++;
		if (sim.entities.size() == sim.maxPopulationSize) {
			// Replace a random entity
			int entityIndexToReplace = sim.randomNumberGenerator.nextInt(sim.maxPopulationSize);
			sim.entities.get(entityIndexToReplace).setZombie();
			sim.entities.set(entityIndexToReplace, simulatedEntity);
		} else {
			sim.entities.add(simulatedEntity);
		}
	};

	public static final AddEntity shrinkBackWhenFull = (Simulation sim, SimulatedEntity simulatedEntity) -> {
		sim.birthCounter++;
		sim.entities.add(simulatedEntity);
		throw new RuntimeException("Unimplemented");
	};
	
	public static final AddEntity addWithoutLimit = (Simulation sim, SimulatedEntity simulatedEntity) -> {
		sim.birthCounter++;
		sim.entities.add(simulatedEntity);
	};

	private AddEntity addMethod = addWithoutLimit;
	
	protected boolean done = false;

	public void add(SimulatedEntity simulatedEntity) {
		addMethod.addEntity(this, simulatedEntity);
	}

	public void remove(SimulatedEntity entity) {
		entity.setZombie();
		entities.remove(entity);
	}

}

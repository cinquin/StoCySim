[![Build Status](https://travis-ci.org/cinquin/StoCySim.svg?branch=master)](https://travis-ci.org/cinquin/StoCySim)

**General notes**

This is a simple stochastic simulation to illustrate how an entity switching
stochastically between "on" and "off" states leads to noise in the total
amount of time that has been spent in the "on" state (the context is worm
reproduction). Given suitable assumptions, one could probably derive
analytical results on this sort of question.

To compile the code and run the tests, use `ant junitreport`. To run the
simulation, use `ant StochasticSwitchIllustration`.

This simulation will be expanded upon in the future to explore worm
population dynamics.

**Credits**

This simulation relies on the Apache Commons Mathematics library (Apache
license), the JGraphT library for its Fibonacci heap implementation (LGPL
and EPL licenses), JUnit (EPL), and Hamcrest (BSD license). All are
included as jar files in the `lib` directory.

**License**

This code is released under the BSD two-clause license.

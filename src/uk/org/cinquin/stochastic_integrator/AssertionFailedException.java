/****************************************************************************
 * StoCySim Copyright (c) 2013-2015 Cinquin Lab.
 * All rights reserved. This code is made available under a dual license:
 * the two-clause BSD license or the GNU Public License v2.
 ***************************************************************************/

package uk.org.cinquin.stochastic_integrator;

public class AssertionFailedException extends RuntimeException {

	private static final long serialVersionUID = 1215436196681641265L;

	public AssertionFailedException() {
	}
	
	public AssertionFailedException(String message) {
		super(message);
	}
	
	public AssertionFailedException(String message, Throwable cause) {
		super(message, cause);
	}
	
}

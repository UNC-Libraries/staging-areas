package edu.unc.lib.staging;

import static org.junit.Assert.*;

import org.junit.Test;

public class IrodsURIPatternTest {

	@Test
	public void testMyURI() {
		IrodsURIPattern p = new IrodsURIPattern();
		assertTrue("Should match", p.matches("irods:cdr-stage.lib.unc.edu:5555/stagingZone/home/stage/alpha/"));
	}
}

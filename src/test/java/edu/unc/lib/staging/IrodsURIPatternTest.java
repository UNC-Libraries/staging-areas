package edu.unc.lib.staging;

import static org.junit.Assert.*;

import java.net.URI;

import org.junit.Test;

public class IrodsURIPatternTest {

	@Test
	public void testMyURI() {
		IrodsURIPattern p = new IrodsURIPattern();
		assertTrue("Should match", p.matches(URI.create("irods://cdr-stage.lib.unc.edu:5555/stagingZone/home/stage/")));
	}
}

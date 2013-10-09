package edu.unc.lib.staging;

import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.junit.Test;

public class IrodsURIPatternTest {

	@Test
	public void testMyURI() {
		IrodsURIPattern p = new IrodsURIPattern();
		URI stageURI= URI.create("irods://cdr-stage.lib.unc.edu:5555/stagingZone/home/stage/");
		assertTrue("Should match", p.matches(stageURI));
		URI storeURI = p.makeURI(stageURI, "lalala/foo/bar/stuff space.txt");
		URI expected = URI.create("irods://"+System.getProperty("user.name")+"@cdr-stage.lib.unc.edu:5555/stagingZone/home/stage/lalala/foo/bar/stuff%20space.txt");
		assertTrue("Wrong value:\n"+storeURI+"\n"+expected, expected.equals(storeURI));
	}
}

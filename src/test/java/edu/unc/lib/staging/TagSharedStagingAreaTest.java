package edu.unc.lib.staging;

import static org.junit.Assert.*;

import org.junit.Test;

public class TagSharedStagingAreaTest {

	@Test
	public void testMatches() {
		String test1 = "tag:count0@cdr.lib.unc.edu,2013-01-01:storhouse/bla/bla/bla";
		TagSharedStagingArea area = new TagSharedStagingArea();
		String stageURI = "tag:cdr.lib.unc.edu,2013:storhouse";
		area.setUri(stageURI);
		assertTrue(stageURI+" must match "+test1, area.matches(test1));
	}

}

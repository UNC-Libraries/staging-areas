package edu.unc.lib.staging;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class TagSharedStagingAreaTest {
	Stages stages;
	
	@Before
	public void setUp() throws Exception {
		File lconfig = new File("src/test/resources/localConfig.json");
		StringBuilder sb = new StringBuilder();
		BufferedReader r = null;
		try {
		    r = new BufferedReader(new FileReader(lconfig));
		for(String line = r.readLine(); line != null; line = r.readLine()) {
			sb.append(line).append('\n');
		}
		} finally {
			if(r != null) r.close();
		}
		this.stages = new Stages(new String[] {"file:src/test/resources/stages.json"}, sb.toString(), new FileResolver());
	}
	
	@Test
	public void testMatches() {
		String test1 = "tag:count0@cdr.lib.unc.edu,2013-01-01:storhouse/bla/bla/bla";
		TagSharedStagingArea area = new TagSharedStagingArea();
		String stageURI = "tag:cdr.lib.unc.edu,2013:storhouse";
		area.setUri(stageURI);
		assertTrue(stageURI+" must match "+test1, area.matches(test1));
	}

	@Test
	public void testTagUriResolvesRegardlessOfDateAndUser() throws StagingException, URISyntaxException {
		String lDir = new File("src/test/resources").toURI().toString();
		lDir = lDir.substring(0, lDir.length()-1);
		this.stages.setCustomMapping("tag:cdr.lib.unc.edu,2013:storhouse_shc", lDir);
		File f = new File("src/test/resources/local.txt");
		URI uri = f.toURI();
		String local = this.stages.getLocalURL("tag:user@cdr.lib.unc.edu,2013-01-01:storhouse_shc/local.txt");
		boolean match = uri.equals(new URI(local));
		assertTrue("Local mapping "+local+" must match expected path "+uri, match);
	}

	@Test
	public void testMakeStagedFileURI() {
		String stageID = "tag:cdr.lib.unc.edu,2013:digitalarchive";
		String isoDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
		String expectedValue = MessageFormat.format("tag:{0}@cdr.lib.unc.edu,{1}:digitalarchive/resources/stages.json", System.getProperty("user.name"), isoDate);
		File lDir = new File("src/test");
		File testFile = new File("src/test/resources/stages.json"); // just using JSON file as a data file
		String lDirStr = lDir.toURI().toString();
		lDirStr = lDirStr.substring(0, lDirStr.length()-1); // trimming off the trailing slash
		this.stages.setCustomMapping(stageID, lDirStr);
		StagingArea stage = this.stages.getStage("tag:cdr.lib.unc.edu,2013:digitalarchive");
		String fileStagedURI = stage.getStagedURI(testFile);
		assertTrue(fileStagedURI+" must match "+expectedValue, expectedValue.equals(fileStagedURI));
	}
	
}

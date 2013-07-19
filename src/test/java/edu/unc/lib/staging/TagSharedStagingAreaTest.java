package edu.unc.lib.staging;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
		List<URIPattern> patterns = new ArrayList<URIPattern>();
		patterns.add(new TagURIPattern());
		patterns.add(new IrodsURIPattern());
		this.stages = new Stages(sb.toString(), new FileResolver());
	}
	
	@Test
	public void testMatches() throws Exception {
		URI test1 = URI.create("tag:count0@cdr.lib.unc.edu,2013-01-01:/storhouse/bla/bla/bla");
		SharedStagingArea area = new SharedStagingArea();
		URI stageURI = URI.create("tag:cdr.lib.unc.edu,2013:/storhouse/");
		area.setUri(stageURI);
		area.setUriPattern(new TagURIPattern());
		assertTrue(stageURI+" must match "+test1, area.isWithin(test1));
	}

	@Test
	public void testTagUriResolvesRegardlessOfDateAndUser() throws StagingException, URISyntaxException, MalformedURLException {
		String lDir = new File("src/test/resources/").toURI().toString();
		lDir = lDir.substring(0, lDir.length());
		this.stages.setCustomMapping(URI.create("tag:cdr.lib.unc.edu,2013:/storhouse_shc/"), URI.create(lDir).toURL());
		File f = new File("src/test/resources/local.txt");
		URI uri = f.toURI();
		URL local = this.stages.getLocalURL(URI.create("tag:user@cdr.lib.unc.edu,2013-01-01:/storhouse_shc/local.txt"));
		boolean match = uri.equals(local.toURI());
		assertTrue("Local mapping "+local+" must match expected path "+uri, match);
	}

	@Test
	public void testMakeStagedFileURI() throws Exception {
		URI stageID = URI.create("tag:cdr.lib.unc.edu,2013:/digitalarchive/");
		String isoDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
		String expectedValue = MessageFormat.format("tag:{0}@cdr.lib.unc.edu,{1}:/digitalarchive/resources/stages.json", System.getProperty("user.name"), isoDate);
		File lDir = new File("src/test");
		File testFile = new File("src/test/resources/stages.json"); // just using JSON file as a data file
		String lDirStr = lDir.toURI().toString();
		lDirStr = lDirStr.substring(0, lDirStr.length());
		this.stages.setCustomMapping(stageID, URI.create(lDirStr).toURL());
		StagingArea stage = this.stages.getStage(stageID);
		URI fileStagedURI = stage.getManifestURI(testFile.toURI().toURL());
		assertTrue(fileStagedURI+" must match "+expectedValue, expectedValue.equals(fileStagedURI.toString()));
	}
	
}

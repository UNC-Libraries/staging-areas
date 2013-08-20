package edu.unc.lib.staging;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class TagURIStagingTest {
	Stages stages;
	SharedStagingArea stage;
	
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
		this.stages = new Stages(sb.toString(), new FileResolver());
		URI stageURI = URI.create("tag:cdr.lib.unc.edu,2013:/storhouse_shc/");
		String lDir = new File("src/test/resources/").toURI().toString();
		lDir = lDir.substring(0, lDir.length());
		this.stages.setStorageMapping(stageURI, URI.create(lDir));
		stage = (SharedStagingArea)this.stages.getStage(stageURI);
	}
	
	// TODO implement standard URIStagingTests
	@Test
	public void testConfigWorks() throws IOException {
		URI stageURI = URI.create("tag:cdr.lib.unc.edu,2013:/storhouse_shc/");
		assertTrue("URI must match expected stage", stageURI.equals(stage.getURI()));
		assertTrue("IrodsURIPattern instance", stage.getUriPattern() instanceof TagURIPattern);
		assertTrue("Should be autoconnected: "+stage.getStatus(), stage.isConnected());
		assertTrue("Must have a URL", stage.getConnectedStorageURI() != null);
		assertTrue("Storage URI must be absolute", stage.getConnectedStorageURI().isAbsolute());
	}
	
	
	@Test
	public void testMatches() throws Exception {
		URI test1 = URI.create("tag:count0@cdr.lib.unc.edu,2013-01-01:/storhouse_shc/bla/bla/bla");
		assertTrue("must be within staging area "+test1, stage.isWithin(test1));
	}

	@Test
	public void testTagUriResolvesRegardlessOfDateAndUser() throws StagingException, URISyntaxException, MalformedURLException {
		File f = new File("src/test/resources/local.txt");
		URI uri = f.toURI();
		URI local = this.stages.getStorageURI(URI.create("tag:user@cdr.lib.unc.edu,2013-01-01:/storhouse_shc/local.txt"));
		boolean match = uri.equals(local);
		assertTrue("Local mapping "+local+" must match expected path "+uri, match);
	}

	@Test
	public void testMakeStagedFileURI() throws StagingException {
		URI stageID = URI.create("tag:cdr.lib.unc.edu,2013:/digitalarchive/");
		String isoDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
		String expectedValue = MessageFormat.format("tag:{0}@cdr.lib.unc.edu,{1}:/digitalarchive/stages.json", System.getProperty("user.name"), isoDate);
		File lDir = new File("src/test/resources");
		File testFile = new File("src/test/resources/stages.json"); // just using JSON file as a data file
		String lDirStr = lDir.toURI().toString();
		lDirStr = lDirStr.substring(0, lDirStr.length());
		this.stages.setStorageMapping(stageID, URI.create(lDirStr));
		SharedStagingArea stage = (SharedStagingArea)this.stages.getStage(stageID);
		stage.connect();
		URI fileStagedURI = stage.getManifestURI(testFile.toURI());
		assertTrue(fileStagedURI+" must match "+expectedValue, expectedValue.equals(fileStagedURI.toString()));
	}

	@Test
	public void testManifestStorageRoundTrip() throws StagingException {
		String path = "my/file is relative.txt";
		String folder = "folder A";
		stage.connect();
		URI madeURI = stage.makeURI(folder, path);
		URI testStorageURI = stage.getStorageURI(madeURI);
		URI testManifestURI = stage.getManifestURI(testStorageURI);

		// in iRODS staging areas the manifest and storage URIs are identical
		URI resultStorageURI = stage.getStorageURI(testManifestURI);
		assertTrue("storage URI must match: "+resultStorageURI,testStorageURI.equals(resultStorageURI));
	}
}

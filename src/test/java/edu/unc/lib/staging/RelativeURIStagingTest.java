package edu.unc.lib.staging;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;

import org.junit.Before;
import org.junit.Test;

public class RelativeURIStagingTest {

	Stages stages;
	LocalResolver resolver = mock(LocalResolver.class);
	SharedStagingArea stage = null;
	
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
		// setup resolver spy for iRODS answers
		//URI irodsStage = URI.create("irods://cdr-stage.lib.unc.edu:3333/stagingZone/projects");
		//when(resolver.exists(eq(irodsStage))).thenReturn(true);
		this.stages = new Stages(sb.toString(), resolver);
		stage = (SharedStagingArea)this.stages.getStage(URI.create("data/"));
	}

	@Test
	public void testConfigWorks() throws IOException {
		assertTrue("URI must match expected stage", URI.create("data/").equals(stage.getURI()));
		assertTrue("RelativeURIPattern instance", stage.getUriPattern() instanceof RelativeURIPattern);
		assertTrue("Should be connected: "+stage.getStatus(), stage.isConnected());
		assertTrue("Must have a URL", stage.getConnectedStorageURI() != null);
		assertTrue("Storage URI must be relative", !stage.getConnectedStorageURI().isAbsolute());
	}
	
	@Test
	public void testMakeStorageURI() throws StagingException {
		URI stagedFile = stage.makeStorageURI("my/file is relative.txt");
		assertTrue("match",stagedFile.toString().equals("data/my/file+is+relative.txt"));
	}
	
	@Test
	public void testManifestStorageRoundTrip() throws StagingException {
		URI testStorageURI = URI.create("data/my/file+is+relative.txt");
		URI testManifestURI = stage.getManifestURI(testStorageURI);
		// in relative staging areas the manifest and storage URIs are identical
		assertTrue("manifest URI must match: "+testManifestURI,testStorageURI.equals(testManifestURI));
		URI resultStorageURI = stage.getStorageURI(testManifestURI);
		assertTrue("storage URI must match: "+resultStorageURI,testStorageURI.equals(resultStorageURI));
	}
	
	@Test
	public void testPathsAreDecoded() throws StagingException {
		String originalPath = "my/file is relative.txt";
		URI stagedFile = stage.makeStorageURI(null, originalPath);
		String path = stage.getRelativePath(stagedFile);
		assertTrue("path must match: "+path,originalPath.equals(path));
	}
	
	@Test
	public void testSharedFoldersAreSupported() throws StagingException {
		String originalPath = "my/file is relative.txt";
		URI stagedFile = stage.makeStorageURI("folderA", originalPath);
		URI expectedURI = URI.create("data/folderA/my/file+is+relative.txt");
		assertTrue("URI must match: "+stagedFile,expectedURI.equals(stagedFile));
		String path = stage.getRelativePath(stagedFile);
		String expectedPath = "folderA/my/file is relative.txt";
		assertTrue("path must match: "+path,expectedPath.equals(path));
	}

}

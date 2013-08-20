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
import java.text.MessageFormat;

import org.junit.Before;
import org.junit.Test;

public class IrodsURIStagingTest {

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
		URI irodsStage = URI.create("irods://cdr-stage.lib.unc.edu:3333/stagingZone/projects/");
		when(resolver.exists(eq(irodsStage))).thenReturn(true);
		this.stages = new Stages(sb.toString(), resolver);
		stage = (SharedStagingArea)this.stages.getStage(irodsStage);
	}

	@Test
	public void testConfigWorks() throws IOException {
		URI irodsStage = URI.create("irods://cdr-stage.lib.unc.edu:3333/stagingZone/projects/");
		assertTrue("URI must match expected stage", irodsStage.equals(stage.getURI()));
		assertTrue("IrodsURIPattern instance", stage.getUriPattern() instanceof IrodsURIPattern);
		assertTrue("Should not be autoconnected: "+stage.getStatus(), !stage.isConnected());
		stage.connect();
		assertTrue("Should now be connected: "+stage.getStatus(), stage.isConnected());
		assertTrue("Must have a URL", stage.getConnectedStorageURI() != null);
		assertTrue("Storage URI must be absolute", stage.getConnectedStorageURI().isAbsolute());
	}
	
	@Test
	public void testMakeStorageURI() throws StagingException {
		String path = "my/file is relative.txt";
		String folder = "folder A";
		String uriStr = MessageFormat.format("irods://{0}@cdr-stage.lib.unc.edu:3333/stagingZone/projects/{1}/{2}",
				System.getProperty("user.name"),
				URIPattern.encodePath(folder),
				URIPattern.encodePath(path));
		URI expectedStorageURI = URI.create(uriStr);
		stage.connect();
		URI storageURI = stage.makeStorageURI("folder A", path);
		assertTrue("match expected: "+storageURI+"\n"+expectedStorageURI,expectedStorageURI.equals(storageURI));
	}
	
	@Test
	public void testManifestStorageRoundTrip() throws StagingException {
		String path = "my/file is relative.txt";
		String folder = "folder A";
		String uriStr = MessageFormat.format("irods://{0}@cdr-stage.lib.unc.edu:3333/stagingZone/projects/{1}/{2}",
				System.getProperty("user.name"),
				URIPattern.encodePath(folder),
				URIPattern.encodePath(path));
		URI testStorageURI = URI.create(uriStr);
		stage.connect();
		URI testManifestURI = stage.getManifestURI(testStorageURI);
		// in iRODS staging areas the manifest and storage URIs are identical
		assertTrue("manifest URI must match: "+testManifestURI,testStorageURI.equals(testManifestURI));
		URI resultStorageURI = stage.getStorageURI(testManifestURI);
		assertTrue("storage URI must match: "+resultStorageURI,testStorageURI.equals(resultStorageURI));
	}
	
	@Test
	public void testPathsAreDecoded() throws StagingException {
		String originalPath = "my/file is relative.txt";
		stage.connect();
		URI stagedFile = stage.makeStorageURI(originalPath);
		String path = stage.getRelativePath(stagedFile);
		assertTrue("path must match: "+path,originalPath.equals(path));
	}
	
	@Test
	public void testSharedFoldersAreSupported() throws StagingException {
		String path = "my/file is relative.txt";
		String folder = "folder A";
		String uriStr = MessageFormat.format("irods://{0}@cdr-stage.lib.unc.edu:3333/stagingZone/projects/{1}/{2}",
				System.getProperty("user.name"),
				URIPattern.encodePath(folder),
				URIPattern.encodePath(path));
		URI expectedStorageURI = URI.create(uriStr);
		stage.connect();
		URI stagedFile = stage.makeStorageURI(folder, path);
		assertTrue("URI must match: "+stagedFile,expectedStorageURI.equals(stagedFile));
		String testPath = stage.getRelativePath(stagedFile);
		String expectedPath = "folder A/my/file is relative.txt";
		assertTrue("path must match: "+testPath,expectedPath.equals(testPath));
	}

}

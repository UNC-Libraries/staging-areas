package edu.unc.lib.staging;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class StagesTest {
	Stages stages;
	LocalResolver resolver = mock(LocalResolver.class);
	
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
		URI irodsStage = URI.create("irods://cdr-stage.lib.unc.edu:3333/stagingZone/projects");
		when(resolver.exists(eq(irodsStage))).thenReturn(true);
		this.stages = new Stages(sb.toString(), resolver);
	}
	
	@Test
	public void testParsedJSON() {
		assertTrue("5 staging areas must have been loaded, got "+this.stages.getAllAreas().size(), this.stages.getAllAreas().size() == 5);
	}
	
	@Test
	public void testCustomMappingsLoaded() {
		URI stageId = URI.create("tag:cdr.lib.unc.edu,2013:/storhouse_shc/");
		String customMapping = "file:/Z:/in_process/";
		SharedStagingArea stage = (SharedStagingArea)this.stages.getStage(stageId);
		assertNotNull("Must have located a stage "+stage, stage);
		URI foundMapping = stage.getStorageMapping();
		assertNotNull("Must have located a mapping for "+stageId, foundMapping);
		assertTrue("Mapping must match local JSON config", customMapping.equals(foundMapping.toString()));
	}
	
	@Test
	public void testLocalStageDefinitionsOverrideRemote() {
		URI test = URI.create("irods://cdr-stage.lib.unc.edu:3333/stagingZone/projects/");
		StagingArea override = this.stages.getStage(test);
		assertTrue("Local definitions must take precendence over remote.", "My Override iRODS Stage for Projects".equals(override.getName()));
	}
	
	/**
	 * Make sure that additional local mappings can be configured and then exported to JSON
	 */
	@Test
	public void testCustomLocalMappingUpdateAndExport() throws StagingException {
		String config = this.stages.getLocalConfig();
		Stages testStages = new Stages(config, new FileResolver());
		StagingArea st = testStages.getStage(URI.create("data/"));
		assertNotNull("must persist bag it staging area", st);
		String test1Config = testStages.getLocalConfig();
		Stages test2Stages = new Stages(test1Config, new FileResolver());
		String test2Config = test2Stages.getLocalConfig();
		assertTrue("Configs must match", test2Config.equals(test1Config));
	}
	
	@Test
	public void testLocalFileResolvesFromRemoteConfig() throws StagingException, URISyntaxException, MalformedURLException {
		String mappedDir = new File("src/test/resources/").toURI().toString();
		mappedDir = mappedDir.substring(0, mappedDir.length());
		URI mappedStorageURI = URI.create(mappedDir);
		File tagFile = new File("src/test/resources/tag_7a882b56");
		when(resolver.exists(Mockito.eq(mappedStorageURI))).thenReturn(true);
		when(resolver.exists(Mockito.eq(tagFile.toURI()))).thenReturn(true);
		this.stages.setStorageMapping(URI.create("tag:cdr.lib.unc.edu,2013:/storhouse_shc/"), mappedStorageURI);
		URI expectedStorageURI = new File("src/test/resources/local.txt").toURI();
		URI testStorageURI = this.stages.getStorageURI(URI.create("tag:cdr.lib.unc.edu,2013:/storhouse_shc/local.txt"));
		assertTrue("Returned local URL must not be null.", testStorageURI != null);
		boolean match = expectedStorageURI.equals(testStorageURI);
		assertTrue("Local mapping "+testStorageURI+" must match expected path "+expectedStorageURI, match);
	}
	
	@Test
	public void testIRODSStagingConnects() {
		URI irodsExample = URI.create("irods://cdr-stage.lib.unc.edu:3333/stagingZone/projects/foo/I+am+staged.txt");
		SharedStagingArea st = this.stages.findMatchingArea(irodsExample);
		assertNotNull("Must find irods staging area", st);
		String expectedPath = "foo/I am staged.txt";
		String relPath = st.getRelativePath(irodsExample);
		assertTrue("Must match expected path: "+relPath, expectedPath.equals(relPath));
	}
}

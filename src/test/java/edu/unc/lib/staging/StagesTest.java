package edu.unc.lib.staging;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class StagesTest {
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
	public void testParsedJSON() {
		assertTrue("4 staging areas must have been loaded", this.stages.getAllAreas().size() == 4);
	}
	
	@Test
	public void testCustomMappingsLoaded() {
		URI stageId = URI.create("tag:cdr.lib.unc.edu,2013:/storhouse_shc/");
		String customMapping = "file:/Z:/in_process/";
		SharedStagingArea stage = (SharedStagingArea)this.stages.getStage(stageId);
		assertNotNull("Must have located a stage "+stage, stage);
		URL foundMapping = stage.getCustomMapping();
		assertNotNull("Must have located a mapping for "+stageId, foundMapping);
		System.err.println(foundMapping.toString());
		System.err.println(customMapping);
		assertTrue("Mapping must match local JSON config", customMapping.equals(foundMapping.toString()));
	}
	
	@Test
	public void testLocalStageDefinitionsOverrideRemote() {
		URI test = URI.create("irods://cdr-stage.lib.unc.edu:3333/stagingZone/projects");
		StagingArea override = this.stages.getStage(test);
		assertTrue("Local definitions must take precendence over remote.", "My Override iRODS Stage for Projects".equals(override.getName()));
	}
	
	/**
	 * Make sure that additional local mappings can be configured and then exported to JSON
	 */
	@Test
	public void testCustomLocalMappingUpdateAndExport() throws StagingException {
		String config = this.stages.getLocalConfig();
		Stages test = new Stages(config, new FileResolver());
	}
	
	@Test
	public void testLocalFileResolvesFromRemoteConfig() throws StagingException, URISyntaxException, MalformedURLException {
		String lDir = new File("src/test/resources/").toURI().toString();
		lDir = lDir.substring(0, lDir.length());
		this.stages.setCustomMapping(URI.create("tag:cdr.lib.unc.edu,2013:/storhouse_shc/"), URI.create(lDir).toURL());
		File f = new File("src/test/resources/local.txt");
		URI uri = f.toURI();
		URL local = this.stages.getLocalURL(URI.create("tag:cdr.lib.unc.edu,2013:/storhouse_shc/local.txt"));
		assertTrue("Returned local URL must not be null.", local != null);
		boolean match = uri.equals(local.toURI());
		assertTrue("Local mapping "+local+" must match expected path "+uri, match);
	}
	
}

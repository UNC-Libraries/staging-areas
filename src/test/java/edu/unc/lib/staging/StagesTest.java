package edu.unc.lib.staging;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.net.URISyntaxException;
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
		this.stages = new Stages(sb.toString(), new FileResolver(), patterns);
	}
	
	@Test
	public void testParsedJSON() {
		assertTrue("4 staging areas must have been loaded", this.stages.getAreas().size() == 4);
	}
	
	@Test
	public void testCustomMappingsLoaded() {
		String stageId = "tag:cdr.lib.unc.edu,2013:/storhouse_shc/";
		String customMapping = "file:/Z:/in_process/";
		String foundMapping = ((SharedStagingArea)this.stages.getStage(stageId)).getCustomMapping();
		assertTrue("Mapping must match local JSON config", customMapping.equals(foundMapping));
	}
	
	@Test
	public void testLocalStageDefinitionsOverrideRemote() {
		StagingArea override = this.stages.getStage("irods:cdr-stage.lib.unc.edu:5555/stagingZone/home/stage/alpha/");
		assertTrue("Local definitions must take precendence over remote.", "My Override iRODS Stage Alpha".equals(override.getName()));
	}
	
	@Test
	public void testCustomLocalMappingUpdateAndExport() {
		// make sure that additional local mappings can be configured and then exported to JSON	
		fail("not implemented");
	}
	
	@Test
	public void testLocalFileResolvesFromRemoteConfig() throws StagingException, URISyntaxException {
		String lDir = new File("src/test/resources/").toURI().toString();
		lDir = lDir.substring(0, lDir.length());
		this.stages.setCustomMapping("tag:cdr.lib.unc.edu,2013:/storhouse_shc/", lDir);
		File f = new File("src/test/resources/local.txt");
		URI uri = f.toURI();
		String local = this.stages.getLocalURL("tag:cdr.lib.unc.edu,2013:/storhouse_shc/local.txt");
		assertTrue("Returned local URL must not be null.", local != null);
		boolean match = uri.equals(new URI(local));
		assertTrue("Local mapping "+local+" must match expected path "+uri, match);
	}
	
}

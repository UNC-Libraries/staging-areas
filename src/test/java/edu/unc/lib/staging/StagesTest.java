package edu.unc.lib.staging;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.net.URISyntaxException;

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
		this.stages = new Stages(new String[] {"file:src/test/resources/stages.json"}, sb.toString(), new FileResolver());
	}
	
	@Test
	public void testParsedJSON() {
		assertTrue("4 staging areas must have been loaded", this.stages.getAreas().size() == 4);
	}
	
	@Test
	public void testCustomLocalMappingImportExport() {
		// make sure that additional local mappings can be configured
	}
	
	@Test
	public void testLocalFileResolvesFromRemoteConfig() throws StagingException, URISyntaxException {
		String lDir = new File("src/test/resources").toURI().toString();
		lDir = lDir.substring(0, lDir.length()-1);
		this.stages.setCustomMapping("tag:cdr.lib.unc.edu,2013:storhouse_shc", lDir);
		File f = new File("src/test/resources/local.txt");
		URI uri = f.toURI();
		String local = this.stages.getLocalURL("tag:cdr.lib.unc.edu,2013:storhouse_shc/local.txt");
		boolean match = uri.equals(new URI(local));
		assertTrue("Local mapping "+local+" must match expected path "+uri, match);
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
	
}

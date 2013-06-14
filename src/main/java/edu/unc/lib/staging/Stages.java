package edu.unc.lib.staging;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Stages {
	Logger log = LoggerFactory.getLogger(Stages.class);
	private String[] remoteConfigURLs;
	private Map<String, StagingArea> areas = new HashMap<String, StagingArea>();
	private LocalResolver resolver;
	private String localConfig;
	private Map<String, String> customMappings = new HashMap<String, String>();

	public Stages(String[] remoteConfigURLs, String localConfig,
			LocalResolver resolver) throws StagingException {
		this.remoteConfigURLs = remoteConfigURLs;
		this.resolver = resolver;
		this.localConfig = localConfig;
		if (localConfig != null) {
			loadLocalConfig();
		}
		for (String configURL : this.remoteConfigURLs) {
			loadStages(configURL);
		}
	}

	public Map<String, StagingArea> getAreas() {
		return Collections.unmodifiableMap(this.areas);
	}

	private void loadLocalConfig() throws StagingException {
		try {
			JsonNode rnode;
			ObjectMapper om = new ObjectMapper();
			rnode = om.readTree(this.localConfig);
			if (rnode.has("customMappings")) {
				this.customMappings = om.treeToValue(
						rnode.get("customMappings"), Map.class);
			}
			if (rnode.has("stagingAreas")) {
				JsonNode node = rnode.get("stagingAreas");
				for (Iterator<String> areaIter = node.fieldNames(); areaIter
						.hasNext();) {
					String uri = areaIter.next();
					JsonNode areaNode = node.get(uri);
					addStagingArea(om, uri, areaNode, "LOCAL");
				}
			}
		} catch (IOException e) {
			log.error("Cannot read local staging config.", e);
			throw new StagingException("Cannot read local staging config.", e);
		}
	}

	private void addStagingArea(ObjectMapper om, String uri, JsonNode areaNode,
			String origin) throws StagingException {
		try {
			URI u = new URI(uri);
			StagingArea stage;
			if ("tag".equals(u.getScheme())) {
				stage = om.treeToValue(areaNode, TagSharedStagingArea.class);
			} else if ("irods".equals(u.getScheme())) {
				log.error("IRODS staging area not yet implemented");
				return;
			} else {
				return;
			}
			stage.setOrigin(origin);
			stage.setUri(uri);
			stage.setResolver(this.resolver);
			if (this.customMappings.containsKey(uri)) {
				stage.setCustomMapping(this.customMappings.get(uri));
			}
			stage.init();
			areas.put(stage.getUri(), stage);
		} catch (JsonProcessingException e) {
			log.error("Cannot parse staging area config '"+uri+"' ("+origin+")", e);
			return;
		} catch (URISyntaxException e) {
			log.error("Staging area URI is invalid '"+uri+"' ("+origin+")", e);
			return;
		}
	}

	/**
	 * Export the current configuration of local mappings and locally defined
	 * stages. This does not include stages defined remotely, but may include
	 * their mappings.
	 * 
	 * @return
	 */
	public String getLocalConfig() {
		throw new IllegalArgumentException();
	}

	public void setCustomMapping(String stageURI, String localURI) {
		this.customMappings.put(stageURI, localURI);
		if (this.areas.get(stageURI) != null) {
			this.areas.get(stageURI).setCustomMapping(localURI);
		}
	}

	private void loadStages(String configURL) {
		JsonNode node;
		ObjectMapper om = new ObjectMapper();
		try {
			URL config = new URL(configURL);
			node = om.readTree(config.openStream());
		} catch (IOException e) {
			e.printStackTrace(); // FIXME
			return;
		}
		for (Iterator<String> areaIter = node.fieldNames(); areaIter.hasNext();) {
			String uri = areaIter.next();
			JsonNode areaNode = node.get(uri);
			try {
				this.addStagingArea(om, uri, areaNode, configURL);
			} catch(StagingException e) {
				log.error("Cannot load stage '"+uri+"' from config at "+configURL, e);
			}
		}
	}

	/**
	 * Find the local URI that maps to the staged URI.
	 * 
	 * @param stagedURI
	 */
	public String getLocalURL(String stagedURI) throws StagingException {
		String result = null;
		List<StagingArea> possible = new ArrayList<StagingArea>();
		// look through the staging areas, see if id matches above
		for (StagingArea area : areas.values()) {
			// FIXME this really needs to be pattern based, i.e. tag and irods
			// URIs have more variety within a stage
			if (area.matches(stagedURI)) {
				possible.add(area);
			}
		}
		if (possible.size() == 0) {
			throw new StagingException(
					"No known staging areas match the supplied URI: "
							+ stagedURI);
		}
		// see if any matching areas are connected
		List<StagingArea> problems = new ArrayList<StagingArea>();
		for (StagingArea a : possible) {
			if (!a.isConnected()) {
				problems.add(a);
			}
		}
		possible.removeAll(problems);
		if (possible.size() == 0) {
			StringBuilder sb = new StringBuilder();
			sb.append(problems.remove(0).getName());
			for (StagingArea a : problems)
				sb.append(", ").append(a.getName());
			throw new StagingException(sb.append(
					" matched the supplied URI, but are not connected.")
					.toString());
		}
		problems.clear();
		// if connected, see if verified by keyfile
		for (StagingArea a : possible) {
			if (!a.isVerified()) {
				problems.add(a);
			}
		}
		possible.removeAll(problems);
		if (possible.size() == 0) {
			StringBuilder sb = new StringBuilder();
			sb.append(problems.remove(0).getName());
			for (StagingArea a : problems)
				sb.append(", ").append(a.getName());
			throw new StagingException(
					sb.append(
							" matched the supplied URI, are connected, but cannot be verified by key file.")
							.toString());
		}
		problems.clear();
		// if verified, convert stagedURI to localURI
		result = possible.get(0).getLocalURL(stagedURI);
		return result;
	}

}

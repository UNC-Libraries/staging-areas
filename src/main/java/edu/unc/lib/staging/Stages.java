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
	private List<String> remoteConfigURLs;
	private Map<String, SharedStagingArea> areas = new HashMap<String, SharedStagingArea>();
	private List<URIPattern> uriPatterns;
	private LocalResolver resolver;
	private String localConfig;
	private Map<String, String> customMappings = new HashMap<String, String>();

	public Stages(String localConfig,
			LocalResolver resolver, List<URIPattern> uriPatterns) throws StagingException {
		this.resolver = resolver;
		this.uriPatterns = uriPatterns;
		this.localConfig = localConfig;
		if (localConfig != null) {
			loadLocalConfig();
		}
		for (String configURL : this.remoteConfigURLs) {
			loadStages(configURL);
		}
	}

	public Map<String, StagingArea> getAreas() {
		Map<String, StagingArea> result = new HashMap<String, StagingArea>();
		result.putAll(areas);
		return Collections.unmodifiableMap(result);
	}

	@SuppressWarnings("unchecked")
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
			if(rnode.has("remoteConfigurations")) {
				JsonNode rconfigs = rnode.get("remoteConfigurations");
				remoteConfigURLs = new ArrayList<String>(rconfigs.size());
				for(int i = 0; i < rconfigs.size(); i++) {
					remoteConfigURLs.add(rconfigs.get(i).asText());
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
			if(areas.containsKey(uri)) return;
			URI u = new URI(uri);
			SharedStagingArea stage = om.treeToValue(areaNode, SharedStagingArea.class);
			for(URIPattern p : uriPatterns) {
				if(p.matches(uri)) {
					stage.setUriPattern(p);
					break;
				}
			}
			if(stage.getUriPattern() == null) {
				log.error("Cannot find a URI pattern for stage: "+stage.getUri());
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

	public void setCustomMapping(String stageURI, String localURI) throws StagingException {
		if (this.areas.get(stageURI) != null) {
			this.customMappings.put(stageURI, localURI);
			this.areas.get(stageURI).setCustomMapping(localURI);
		} else {
			throw new StagingException("No configuration found for the stage with id: "+stageURI );
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
			if (area.isWithin(stagedURI)) {
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

	public StagingArea getStage(String string) {
		return this.areas.get(string);
	}

}

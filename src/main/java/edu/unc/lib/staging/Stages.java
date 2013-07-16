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
	private List<String> repositoryConfigURLs = new ArrayList<String>();
	private Map<String, Map<String, SharedStagingArea>> areas = new HashMap<String, Map<String, SharedStagingArea>>();
	private List<URIPattern> uriPatterns;
	private LocalResolver resolver;
	private String localConfig;
	private Map<String, String> customMappings = new HashMap<String, String>();

	public Stages(String localConfig, LocalResolver resolver)
			throws StagingException {
		this(localConfig, resolver, loadPatterns());
	}

	public Stages(String localConfig, LocalResolver resolver,
			List<URIPattern> uripatterns) throws StagingException {
		this.resolver = resolver;
		this.uriPatterns = uripatterns;
		this.localConfig = localConfig;
		if (localConfig != null) {
			loadLocalConfig();
		}
		for (String configURL : this.repositoryConfigURLs) {
			loadStages(configURL);
		}
	}

	public void addRepositoryConfigURL(String remoteConfigURL)
			throws StagingException {
		// can be a full URL to JSON or a repository base URL
		if (!remoteConfigURL.endsWith("stagingLocations")) {
			if (!remoteConfigURL.endsWith("/")) {
				remoteConfigURL = remoteConfigURL + "/";
			}
			remoteConfigURL = remoteConfigURL + "stagingLocations";
		}
		loadStages(remoteConfigURL);
		this.repositoryConfigURLs.add(remoteConfigURL);
	}

	public void removeRepositoryConfigURL(String repositoryConfigURL) {
		this.repositoryConfigURLs.remove(repositoryConfigURL);
		this.areas.remove(repositoryConfigURL);
	}

	private static List<URIPattern> loadPatterns() {
		ArrayList<URIPattern> result = new ArrayList<URIPattern>();
		result.add(new IrodsURIPattern());
		result.add(new TagURIPattern());
		return result;
	}

	public Map<String, SharedStagingArea> getAllAreas() {
		Map<String, SharedStagingArea> result = new HashMap<String, SharedStagingArea>();
		for (Map<String, SharedStagingArea> val : this.areas.values()) {
			result.putAll(val);
		}
		return Collections.unmodifiableMap(result);
	}

	public Map<String, SharedStagingArea> getAreas(String repositoryConfigURL) {
		return Collections.unmodifiableMap(this.areas.get(repositoryConfigURL));
	}

	@SuppressWarnings("unchecked")
	private void loadLocalConfig() throws StagingException {
		Map<String, SharedStagingArea> localAreas = new HashMap<String, SharedStagingArea>();
		this.areas.put("LOCAL", localAreas);
		try {
			JsonNode rnode;
			ObjectMapper om = new ObjectMapper();
			rnode = om.readTree(this.localConfig);
			if (rnode.has("customMappings")) {
				this.customMappings = om.treeToValue(
						rnode.get("customMappings"), Map.class);
			}
			if (rnode.has("repositoryConfigurations")) {
				JsonNode rconfigs = rnode.get("repositoryConfigurations");
				repositoryConfigURLs = new ArrayList<String>(rconfigs.size());
				for (int i = 0; i < rconfigs.size(); i++) {
					repositoryConfigURLs.add(rconfigs.get(i).asText());
				}
			}
			if (rnode.has("stagingAreas")) {
				JsonNode node = rnode.get("stagingAreas");
				for (Iterator<String> areaIter = node.fieldNames(); areaIter
						.hasNext();) {
					String uri = areaIter.next();
					JsonNode areaNode = node.get(uri);
					SharedStagingArea stage = parseStagingArea(om, uri,
							areaNode, "LOCAL");
					localAreas.put(uri, stage);
				}
			}
		} catch (IOException e) {
			log.error("Cannot read local staging config.", e);
			throw new StagingException("Cannot read local staging config.", e);
		}
	}

	private SharedStagingArea parseStagingArea(ObjectMapper om, String uri,
			JsonNode areaNode, String origin) throws StagingException {
		try {
			@SuppressWarnings("unused")
			URI u = new URI(uri);
			SharedStagingArea stage = om.treeToValue(areaNode,
					SharedStagingArea.class);
			for (URIPattern p : uriPatterns) {
				if (p.matches(uri)) {
					stage.setUriPattern(p);
					break;
				}
			}
			if (stage.getUriPattern() == null) {
				throw new StagingException(
						"Cannot find a URI pattern for stage: "
								+ stage.getUri());
			}
			stage.setOrigin(origin);
			stage.setUri(uri);
			stage.setResolver(this.resolver);
			if (this.customMappings.containsKey(uri)) {
				stage.setCustomMapping(this.customMappings.get(uri));
			}
			stage.init();
			return stage;
		} catch (JsonProcessingException e) {
			throw new StagingException("Cannot parse staging area config '"
					+ uri + "' (" + origin + ")", e);
		} catch (URISyntaxException e) {
			throw new StagingException("Staging area URI is invalid '" + uri
					+ "' (" + origin + ")", e);
		}
	}

	public List<String> getRepositoryConfigURLs() {
		return Collections.unmodifiableList(this.repositoryConfigURLs);
	}

	/**
	 * Export the current configuration of local mappings and locally defined
	 * stages. This does not include stages defined remotely, but may include
	 * their mappings.
	 * 
	 * @return
	 */
	public String getLocalConfig() {
		StringBuilder result = new StringBuilder();
		result.append("{\n");
		ObjectMapper om = new ObjectMapper();
		try {
			// customMappings
			result.append("\"customMappings\":");
			result.append(om.writeValueAsString(this.customMappings)).append(
					",\n");
			// repositoryConfigurations
			result.append("\"repositoryConfigurations\":");
			result.append(om.writeValueAsString(this.repositoryConfigURLs))
					.append("\n");
			// TODO stagingAreas
		} catch (JsonProcessingException ignored) {
		}
		result.append("}");
		return result.toString();
	}

	public void setCustomMapping(String stageURI, String localURI)
			throws StagingException {
		if (getAllAreas().get(stageURI) != null) {
			this.customMappings.put(stageURI, localURI);
			getAllAreas().get(stageURI).setCustomMapping(localURI);
		} else {
			throw new StagingException(
					"No configuration found for the stage with id: " + stageURI);
		}
	}

	private void loadStages(String configURL) throws StagingException {
		JsonNode node;
		ObjectMapper om = new ObjectMapper();
		try {
			URL config = new URL(configURL);
			node = om.readTree(config.openStream());
		} catch (IOException e) {
			throw new StagingException("Cannot load config from " + configURL,
					e);
		}
		HashMap<String, SharedStagingArea> stages = new HashMap<String, SharedStagingArea>();
		for (Iterator<String> areaIter = node.fieldNames(); areaIter.hasNext();) {
			String uri = areaIter.next();
			JsonNode areaNode = node.get(uri);
			SharedStagingArea stage = this.parseStagingArea(om, uri, areaNode,
					configURL);
			stages.put(uri, stage);
		}
		areas.put(configURL, stages);
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
		for (StagingArea area : this.getAllAreas().values()) {
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

	public StagingArea getStage(String baseURI) {
		StagingArea result = null;
		if (this.areas.get("LOCAL").containsKey(baseURI)) {
			return this.areas.get("LOCAL").get(baseURI);
		}
		for (Map<String, SharedStagingArea> val : this.areas.values()) {
			if (val.containsKey(baseURI)) {
				result = val.get(baseURI);
				break;
			}
		}
		return result;
	}

}

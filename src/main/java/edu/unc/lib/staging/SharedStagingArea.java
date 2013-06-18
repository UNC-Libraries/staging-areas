package edu.unc.lib.staging;

import java.util.ArrayList;
import java.util.List;


/**
 * These are staging areas common to a work group. Files staged in these spaces
 * may be accessed from many platforms and clients, making manifests and therefore
 * projects more portable between users and software installs.
 * @author count0
 *
 */
/**
 * @author count0
 *
 */
public class SharedStagingArea implements StagingArea {
	transient String configURL; // injected at runtime
	String sharedBaseURI;
	transient String localBaseURI; // determined at runtime
	String name;
	CleanupPolicy ingestCleanupPolicy;
	String keyFile;
	transient URIPattern uriPattern; // determined at runtime
	
	public URIPattern getUriPattern() {
		return this.uriPattern;
	}

	public void setUriPattern(URIPattern p) {
		this.uriPattern = p;
	}

	/* (non-Javadoc)
	 * @see edu.unc.lib.staging.StagingArea#getKeyFile()
	 */
	public String getKeyFile() {
		return keyFile;
	}

	public void setKeyFile(String keyFile) {
		this.keyFile = keyFile;
	}

	String putPattern;
	List<String> mappings = new ArrayList<String>();
	String customMapping;
	private LocalResolver resolver;
	
	public void init() throws StagingException {
		this.localBaseURI = findLocalBase();
	}

	public void setOrigin(String configURL) {
		this.configURL = configURL;
	}

	public String getConfigURL() {
		return configURL;
	}

	public void setConfigURL(String configURL) {
		this.configURL = configURL;
	}

	/* (non-Javadoc)
	 * @see edu.unc.lib.staging.StagingArea#getUri()
	 */
	public String getUri() {
		return sharedBaseURI;
	}

	public void setUri(String uri) {
		this.sharedBaseURI = uri;
	}

	/* (non-Javadoc)
	 * @see edu.unc.lib.staging.StagingArea#getName()
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see edu.unc.lib.staging.StagingArea#getIngestCleanupPolicy()
	 */
	public CleanupPolicy getIngestCleanupPolicy() {
		return ingestCleanupPolicy;
	}

	public void setIngestCleanupPolicy(CleanupPolicy ingestCleanupPolicy) {
		this.ingestCleanupPolicy = ingestCleanupPolicy;
	}

	/* (non-Javadoc)
	 * @see edu.unc.lib.staging.StagingArea#getPutPattern()
	 */
	public String getPutPattern() {
		return putPattern;
	}

	public void setPutPattern(String putPattern) {
		this.putPattern = putPattern;
	}

	/* (non-Javadoc)
	 * @see edu.unc.lib.staging.StagingArea#getMappings()
	 */
	public List<String> getMappings() {
		return mappings;
	}

	public void setMappings(List<String> mappings) {
		this.mappings = mappings;
	}

	public boolean isConnected() {
		return (localBaseURI != null && this.resolver.exists(localBaseURI));
	}

	public boolean isVerified() {
		return (this.keyFile != null && this.resolver.exists(localBaseURI+"/"+this.keyFile));
	}
	
	private String findLocalBase() {
		if(this.customMapping != null && this.resolver.exists(this.customMapping)) {
			return this.customMapping;
		}
		for(String mapping : this.mappings) {
			if(this.resolver.exists(mapping)) {
				return mapping;
			}
		}
		if(this.resolver.exists(sharedBaseURI)) { // some are shared and local, i.e. iRODS
			return sharedBaseURI;
		}
		return null;
	}

	public void setResolver(LocalResolver resolver) {
		this.resolver = resolver;
	}

	public void setCustomMapping(String localURL) {
		this.customMapping = localURL;
		String mapping = findLocalBase();
		if(mapping != null) {
			this.localBaseURI = mapping;
		}
	}
	
	public String getCustomMapping() {
		return this.customMapping;
	}
	
	/**
	 * Parse the staged file URI and extract the path relative to the staging area base URI.
	 * @param stagedFileURI a URI representing a file in this stage
	 * @return the path
	 */
	public String getRelativePath(String stagedFileURI) {
		return this.uriPattern.getRelativePath(this.sharedBaseURI, stagedFileURI);
	}

	public String getLocalURL(String stagedURI) {
		String relativePath = this.getRelativePath(stagedURI);
		return this.localBaseURI+relativePath;
	}

	public boolean isWithin(String stagedURI) {
		return this.uriPattern.isWithin(this.sharedBaseURI, stagedURI);
	}

	public String getSharedURI(String localURL) {
		String relativePath = localURL.substring(this.localBaseURI.length());
		return this.uriPattern.makeURI(sharedBaseURI, relativePath);
	}

}

package edu.unc.lib.staging;


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
public abstract class AbstractSharedStagingArea implements StagingArea {
	String configURL;
	String uri;
	String name;
	CleanupPolicy ingestCleanupPolicy;
	String keyFile;
	
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
	String[] mappings;
	String customMapping;
	private LocalResolver resolver;
	String resolvedMapping = null;
	
	public void init() throws StagingException {
		this.resolvedMapping = findLocalMapping();
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
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
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
	public String[] getMappings() {
		return mappings;
	}

	public void setMappings(String[] mappings) {
		this.mappings = mappings;
	}

	public boolean isConnected() {
		return (resolvedMapping != null && this.resolver.exists(resolvedMapping));
	}

	public boolean isVerified() {
		return (this.keyFile != null && this.resolver.exists(resolvedMapping+"/"+this.keyFile));
	}

	public abstract String getLocalURL(String stagedURI);
	
	private String findLocalMapping() {
		if(this.customMapping != null && this.resolver.exists(this.customMapping)) {
			return this.customMapping;
		}
		for(String mapping : this.mappings) {
			if(this.resolver.exists(mapping)) {
				return mapping;
			}
		}
		return null;
	}

	public void setResolver(LocalResolver resolver) {
		this.resolver = resolver;
	}

	public void setCustomMapping(String localURL) {
		this.customMapping = localURL;
		String mapping = findLocalMapping();
		if(mapping != null) {
			this.resolvedMapping = mapping;
		}
	}
	
	/**
	 * Parse the staged file URI and extract the path relative to the staging area base URI.
	 * @param stagedFileURI a URI representing a file in this stage
	 * @return the path
	 */
	abstract String getRelativePath(String stagedFileURI);
	
	/**
	 * Convert the local URL for a staged file into a URI within the namespace of this stage.
	 * The local URL must be within the resolved local mapping URL.
	 * @param localURL
	 * @return
	 */
	abstract String makeStagedFileURI(String localURL);

}

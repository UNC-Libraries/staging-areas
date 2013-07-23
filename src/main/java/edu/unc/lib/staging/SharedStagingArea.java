package edu.unc.lib.staging;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;
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
	transient URL configURL; // injected at runtime
	URI uRI;
	transient URL localBaseURL; // determined at runtime
	String name;
	CleanupPolicy ingestCleanupPolicy;
	String confirmFile;
	transient URIPattern uriPattern; // determined at runtime

	private static final String disconnectedStatus = Messages
			.getString("SharedStagingArea.Disconnected"); //$NON-NLS-1$
	private static final String notAutoconnectedStatus = Messages
			.getString("SharedStagingArea.NotAutoconnected"); //$NON-NLS-1$
	private static final String notVerifiedStatus = Messages
			.getString("SharedStagingArea.Unverified"); //$NON-NLS-1$
	private static final String failedStatus = Messages
			.getString("SharedStagingArea.ConnectionFailed"); //$NON-NLS-1$
	private static final String connectedStatus = Messages
			.getString("SharedStagingArea.Connected"); //$NON-NLS-1$
	private static final String connectedVerifiedStatus = Messages
			.getString("SharedStagingArea.ConnectedVerified"); //$NON-NLS-1$

	private boolean isConnected = false;
	private String status = disconnectedStatus;

	public URIPattern getUriPattern() {
		return this.uriPattern;
	}

	public void setUriPattern(URIPattern p) {
		this.uriPattern = p;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unc.lib.staging.StagingArea#getKeyFile()
	 */
	public String getConfirmFile() {
		return confirmFile;
	}

	public void setConfirmFile(String confirmFile) {
		this.confirmFile = confirmFile;
	}

	List<URL> mappings = new ArrayList<URL>();
	URL customMapping;
	private LocalResolver resolver;

	public void init() throws StagingException {
		if (this.uriPattern.isAutoconnected()) {
			connect();
		} else {
			this.status = notAutoconnectedStatus;
		}
	}

	public URL getConfigURL() {
		return configURL;
	}

	public void setConfigURL(URL configURL) {
		this.configURL = configURL;
	}

	public URL getLocalBaseURL() {
		return this.localBaseURL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unc.lib.staging.StagingArea#getUri()
	 */
	public URI getURI() {
		return uRI;
	}

	public void setUri(URI uri) {
		this.uRI = uri;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unc.lib.staging.StagingArea#getName()
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unc.lib.staging.StagingArea#getIngestCleanupPolicy()
	 */
	public CleanupPolicy getIngestCleanupPolicy() {
		return ingestCleanupPolicy;
	}

	public void setIngestCleanupPolicy(CleanupPolicy ingestCleanupPolicy) {
		this.ingestCleanupPolicy = ingestCleanupPolicy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unc.lib.staging.StagingArea#getMappings()
	 */
	public List<URL> getMappings() {
		return mappings;
	}

	public void setMappings(List<URL> mappings) {
		this.mappings = mappings;
	}

	public boolean isConnected() {
		return isConnected;
	}

	protected void connect() {
		this.status = disconnectedStatus;
		this.isConnected = false;
		if (this.customMapping != null) {
			if (this.resolver.exists(this.customMapping)) {
				this.localBaseURL = this.customMapping;
				this.isConnected = true;
				this.status = connectedStatus;
			} else {
				this.status = MessageFormat.format(failedStatus,
						"Custom mapping unreachable (" + this.customMapping
								+ ")");
				return;
			}
		}
		if (!this.isConnected) {
			if (this.mappings != null && this.mappings.size() > 0) {
				for (URL mapping : this.mappings) {
					if (this.resolver.exists(mapping)) {
						this.localBaseURL = mapping;
						this.isConnected = true;
						this.status = connectedStatus;
						break;
					}
				}
				this.status = MessageFormat.format(failedStatus,
						"Common folder mappings not found.");
				return;
			}
		}

		// not a mapped thing
		if (!this.isConnected && !this.uriPattern.isLocallyMapped()) {
			URL testURL = null;
			try {
				testURL = new URL(uRI.toString());
			} catch (MalformedURLException e) {
				// fail: Stage URI does not resolve to a known location
				this.status = MessageFormat
						.format(failedStatus, e.getMessage());
				return;
			}
			if (this.resolver.exists(testURL)) {
				// some are shared and local, i.e. iRODS
				try {
					this.localBaseURL = this.uriPattern.makeURI(uRI, "")
							.toURL();
					this.isConnected = true;
					this.status = connectedStatus;
				} catch (MalformedURLException unexpected) {
					throw new Error(unexpected);
				}
			}
		}

		if (!this.isConnected) {
			this.status = MessageFormat.format(failedStatus, "Not found");
			return;
		}

		// Verify if applicable
		if (this.confirmFile != null) {
			try {
				URL keyFileURL = new URL(localBaseURL, this.confirmFile);
				if (this.resolver.exists(keyFileURL)) {
					this.status = connectedVerifiedStatus;
					return;
				} else {
					this.isConnected = false;
					this.status = MessageFormat.format(notVerifiedStatus,
							localBaseURL, this.confirmFile);
				}
			} catch (MalformedURLException e) {
				this.isConnected = false;
				this.status = MessageFormat
						.format(failedStatus, e.getMessage());
			}
		}
	}

	public void setResolver(LocalResolver resolver) {
		this.resolver = resolver;
	}

	protected void setCustomMapping(URL localURL) {
		this.customMapping = localURL;
		connect();
	}

	public URL getCustomMapping() {
		return this.customMapping;
	}

	/**
	 * Parse the staged file URI and extract the path relative to the staging
	 * area base URI.
	 * 
	 * @param stagedFileURI
	 *            a URI representing a file in this stage
	 * @return the path
	 */
	public String getRelativePath(URI stagedFileURI) {
		return this.uriPattern.getRelativePath(this.uRI, stagedFileURI);
	}

	public URL getStagedURL(URI stagedURI) {
		String relativePath = this.getRelativePath(stagedURI);
		if (relativePath.startsWith("/"))
			relativePath = relativePath.substring(1);
		try {
			if (this.localBaseURL.toString().endsWith("/")) {
				return URI.create(this.localBaseURL.toString() + relativePath)
						.toURL();
			} else {
				return URI.create(
						this.localBaseURL.toString() + "/" + relativePath)
						.toURL();
			}
		} catch (MalformedURLException e) {
			throw new Error(e);
		}
	}

	public boolean isWithin(URI stagedURI) {
		return this.uriPattern.isWithin(this.uRI, stagedURI);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unc.lib.staging.StagingArea#getSharedURI(java.lang.String)
	 */
	public URI getManifestURI(URL stagedURL) {
		String relativePath = stagedURL.toString().substring(
				this.localBaseURL.toString().length());
		return this.uriPattern.makeURI(uRI, relativePath);
	}

	public URI makeURI(String relativePath) {
		return this.uriPattern.makeURI(uRI, relativePath);
	}

	public String getStatus() {
		return this.status;
	}

	public String getScheme() {
		return this.uriPattern.getScheme();
	}

	public URL makeStagedFileURL(String projectName, String originalPath) {
		try {
			List<String> pathSegments = new ArrayList<String>();
			// combine base URL, project name and original path
			String basePath = this.localBaseURL.getPath();
			if (basePath != null) {
				for (String s : basePath.split("/")) {
					if (s.trim().length() > 0) {
						pathSegments.add(s);
					}
				}
			}
			pathSegments.add(projectName);
			for (String s : originalPath.split("/")) {
				if (s.trim().length() > 0) {
					pathSegments.add(s);
				}
			}
			StringBuilder pathBuilder = new StringBuilder();
			for (String s : pathSegments) {
				pathBuilder.append("/").append(s);
			}
			String path = pathBuilder.toString();
			if(!"file".equals(this.localBaseURL.getProtocol())) {
				path = URLEncoder.encode(path, "utf-8");
			}
			return new URL(this.localBaseURL, pathBuilder.toString());
		} catch (MalformedURLException e) {
			throw new Error(e);
		} catch (UnsupportedEncodingException e) {
			throw new Error(e);
		}
	}

}

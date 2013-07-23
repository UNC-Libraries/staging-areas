package edu.unc.lib.staging;

import java.net.URI;
import java.net.URL;


public interface StagingArea {

	/**
	 * URI that identifies this staging area
	 * @return
	 */
	public abstract URI getURI();

	/**
	 * Name of this staging area.
	 * @return
	 */
	public abstract String getName();

	/**
	 * The action the repository will take on the staging area after ingest.
	 * @return
	 */
	public abstract CleanupPolicy getIngestCleanupPolicy();

	/**
	 * Is the local mapping for this stage connected.
	 * @return true if connected
	 */
	public abstract boolean isConnected();

	public abstract String getStatus();
	
	public abstract String getScheme();
	
	public abstract URL makeStagedFileURL(String projectName, String originalPath);

	/**
	 * Converts a manifest URI into a locally resolvable URL.
	 * @param manifestURI
	 * @return a URL
	 */
	public abstract URL getStagedURL(URI manifestURI);

	/**
	 * Does this manifest URI match this staging area.
	 * @param manifestURI a manifest URI
	 * @return true if manifestURI falls within the staging area
	 */
	public abstract boolean isWithin(URI manifestURI);

	/**
	 * Creates a manifest reference for a staged file location.
	 * @param testFile
	 * @return
	 */
	public abstract URI getManifestURI(URL stagedURL);

}
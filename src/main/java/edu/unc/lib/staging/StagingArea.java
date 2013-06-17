package edu.unc.lib.staging;

import java.io.File;

public interface StagingArea {

	/**
	 * Name of the key file which is present at the base of this staging area.
	 * @return
	 */
	public abstract String getKeyFile();

	/**
	 * URI that identifies this staging area
	 * @return
	 */
	public abstract String getUri();

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
	 * The naming pattern by which files are staged in this space,
	 * includes placeholders. 
	 * @return
	 */
	public abstract String getPutPattern();

	/**
	 * These are the commonly used URL patterns for this stage.
	 * @return an array of mapping URLs, usually drive mappings
	 */
	public abstract String[] getMappings();

	/**
	 * Is the local mapping for this stage connected.
	 * @return true if connected
	 */
	public abstract boolean isConnected();

	/**
	 * Is this the local mapping for this stage verified by the presence of a key file?
	 * @return
	 */
	public abstract boolean isVerified();

	/**
	 * Converts the stagedURI into a locally resolvable URL.
	 * @param stagedURI
	 * @return a URL
	 */
	public abstract String getLocalURL(String stagedURI);

	/**
	 * Set the locally resolvable URL that maps to the root of this staging area.
	 * @param stageURI the URL of the staging
	 */
	public abstract void setCustomMapping(String localURL);
	
	/**
	 * Retrieve the custom local mapping for this staging area.
	 * @return
	 */
	public abstract String getCustomMapping();

	/**
	 * Does this staged file URI match the staging area.
	 * @param stagedURI a staged file URI
	 * @return true if URI 
	 */
	public abstract boolean matches(String stagedURI);

	/**
	 * Creates a common staged file reference for a local file.
	 * @param testFile
	 * @return
	 */
	public abstract String getStagedURI(File testFile);

}
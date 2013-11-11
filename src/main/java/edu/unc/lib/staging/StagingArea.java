package edu.unc.lib.staging;

import java.net.URI;


public interface StagingArea {

	/**
	 * URI that identifies this staging area
	 * @return
	 */
	public URI getURI();

	/**
	 * Name of this staging area.
	 * @return
	 */
	public String getName();

	/**
	 * The action the repository will take on the staging area after ingest.
	 * @return
	 */
	public CleanupPolicy getIngestCleanupPolicy();

	/**
	 * Is this stage available for read and write operations?
	 * @return true if connected
	 */
	public boolean isConnected();
	
	/**
	 * Is this stage a destination for newly staged files? If not, then it is only used for staging in place.
	 * @return true if read only
	 */
	public boolean isReadOnly();

	public String getStatus();
	
	/**
	 * Create a staging URL for a given set relative paths.
	 * @param pathParts as many path strings as you like, in order
	 * @return the URL for staging
	 */
	public URI makeStorageURI(String... pathParts) throws StagingException;

	/**
	 * Converts a manifest URI into a locally resolvable URL.
	 * @param manifestURI
	 * @return a URL
	 */
	public URI getStorageURI(URI manifestURI) throws StagingException;

	/**
	 * Does this manifest URI match this staging area?
	 * @param manifestURI a manifest URI
	 * @return true if manifestURI matches or falls within the staging area
	 */
	public boolean isWithinManifestNamespace(URI manifestURI);
	
	/**
	 * Does this storage URI match the connected storage area?
	 * @param storageURI a storage URI
	 * @return true if storageURI matches or falls within the storage area
	 */
	public boolean isWithinStorage(URI storageURI);

	/**
	 * Creates a manifest reference for a staged file location.
	 * @param testFile
	 * @return
	 */
	public URI getManifestURI(URI storageURI) throws StagingException;
	
	public String getScheme();

	public URI getConnectedStorageURI();

}
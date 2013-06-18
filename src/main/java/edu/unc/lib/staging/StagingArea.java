package edu.unc.lib.staging;


public interface StagingArea {

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
	 * Does this staged file URI match the staging area.
	 * @param stagedURI a staged file URI
	 * @return true if URI 
	 */
	public abstract boolean isWithin(String stagedURI);

	/**
	 * Creates a shared staged file reference for a local file.
	 * @param testFile
	 * @return
	 */
	public abstract String getSharedURI(String localURL);

}
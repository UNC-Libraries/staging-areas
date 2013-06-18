package edu.unc.lib.staging;

public abstract class URIPattern {

	/**
	 * Get the path part of this URI
	 * @param uri
	 * @return
	 */
	public abstract String getPath(String uri);

	/**
	 * Does this file URI reside within the given base URI.
	 * @param baseURI
	 * @param fileURI
	 * @return true if file is within base URI
	 */
	public abstract boolean isWithin(String baseURI, String fileURI);

	/**
	 * Creates a URI for the relative path within the given base URI.
	 * @param baseURI
	 * @param relativePath
	 * @return
	 */
	public abstract String makeURI(String baseURI, String relativePath);

	/**
	 * Gets the relative path between the given base URI and the file URI.
	 * @param baseURI
	 * @param fileURI
	 * @return
	 */
	public final String getRelativePath(String baseURI, String fileURI) {
		String filePath = getPath(fileURI);
		String basePath = getPath(baseURI);
		return filePath.substring(basePath.length());
	}

	/**
	 * Does the given URI match this pattern
	 * @param uri
	 * @return true if this URI is a match
	 */
	public abstract boolean matches(String uri);
	
}
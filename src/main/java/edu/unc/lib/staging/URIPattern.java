package edu.unc.lib.staging;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.StringTokenizer;

public abstract class URIPattern {

	public boolean isAutoconnected() {
		return false;
	}

	public boolean isLocallyMapped() {
		return false;
	}

	/**
	 * Builds a single URL encode path from a set of path strings. Retains all
	 * slashes.
	 * 
	 * @param path
	 * @return
	 */
	public static String encodePath(String... pathArray) {
		StringBuilder encodedPath = new StringBuilder();
		if (pathArray[0] != null && pathArray[0].startsWith("/"))
			encodedPath.append("/");
		for (String path : pathArray) {
			if(path == null) continue;
			String sofar = encodedPath.toString();
			if(sofar.length() > 0 && !sofar.endsWith("/")) encodedPath.append("/");
			try {
				StringTokenizer st = new StringTokenizer(path, "/");
				while (st.hasMoreTokens()) {
					encodedPath.append(URLEncoder.encode(st.nextToken(),
							"utf-8"));
					if (st.hasMoreTokens())
						encodedPath.append("/");
				}
			} catch (UnsupportedEncodingException e) {
				throw new Error(e);
			}
		}
		if (pathArray[pathArray.length-1].endsWith("/"))
			encodedPath.append("/");
		return encodedPath.toString();
	}

	/**
	 * Get the path part of this URI
	 * 
	 * @param uri
	 * @return
	 */
	public abstract String getPath(URI uri);

	/**
	 * Does this file URI reside within the given base URI.
	 * 
	 * @param baseURI
	 * @param fileURI
	 * @return true if file is within base URI
	 */
	public abstract boolean isWithin(URI baseURI, URI fileURI);

	/**
	 * Creates a URI for the relative path within the given base URI.
	 * 
	 * @param baseURI
	 * @param pathParts
	 * @return
	 */
	public abstract URI makeURI(URI baseURI, String... pathParts);

	/**
	 * Gets the relative path between the given base URI and the file URI.
	 * 
	 * @param baseURI
	 * @param fileURI
	 * @return
	 */
	public final String getRelativePath(URI baseURI, URI fileURI) {
		String filePath = getPath(fileURI);
		String basePath = getPath(baseURI);
		return filePath.substring(basePath.length());
	}

	/**
	 * Does the given URI match this pattern
	 * 
	 * @param uri
	 * @return true if this URI is a match
	 */
	public abstract boolean matches(URI uri);

	public abstract String getScheme();

}
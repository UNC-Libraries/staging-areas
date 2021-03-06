package edu.unc.lib.staging;

import java.net.URI;
import java.net.URLStreamHandlerFactory;

/**
 * These objects test for the existence of a resource at a URL. They may be implemented to fit specific environments.
 * @author count0
 *
 */
public interface LocalResolver {
	public abstract boolean exists(URI locationURI);
	public abstract URLStreamHandlerFactory getURLStreamHandlerFactory();
}

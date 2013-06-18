package edu.unc.lib.staging;

/**
 * These objects test for the existence of a resource at a URL. They may be implemented to fit specific environments.
 * @author count0
 *
 */
public interface LocalResolver {
	public abstract boolean exists(String locationURL);
}

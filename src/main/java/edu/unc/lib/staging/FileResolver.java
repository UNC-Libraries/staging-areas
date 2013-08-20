package edu.unc.lib.staging;

import java.io.File;
import java.net.URI;
import java.net.URLStreamHandlerFactory;

public class FileResolver implements LocalResolver {

	public boolean exists(URI locationURI) {
		System.err.println(locationURI);
		if (!"file".equals(locationURI.getScheme())) {
			return false;
		}
		File f = new File(locationURI);
		return f.exists();
	}

	public URLStreamHandlerFactory getURLStreamHandlerFactory() {
		return new StagesURLStreamHandlerFactory();
	}

}

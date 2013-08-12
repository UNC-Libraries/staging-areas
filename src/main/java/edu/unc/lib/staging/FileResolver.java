package edu.unc.lib.staging;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLStreamHandlerFactory;

public class FileResolver implements LocalResolver {

	public boolean exists(URL locationURL) {
		try {
			if(!"file".equals(locationURL.getProtocol())) {
				return false;
			}
			File f = new File(locationURL.toURI());
			return f.exists();
		} catch (URISyntaxException e) {
			return false;
		}
	}

	public URLStreamHandlerFactory getURLStreamHandlerFactory() {
		return new StagesURLStreamHandlerFactory();
	}

}

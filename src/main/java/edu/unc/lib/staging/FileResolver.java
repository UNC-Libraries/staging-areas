package edu.unc.lib.staging;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class FileResolver implements LocalResolver {

	public boolean exists(String locationURL) {
		try {
			URI uri = new URI(locationURL);
			if(!"file".equals(uri.getScheme())) {
				return false;
			}
			File f = new File(uri);
			return f.exists();
		} catch (URISyntaxException e) {
			return false;
		}
	}

}

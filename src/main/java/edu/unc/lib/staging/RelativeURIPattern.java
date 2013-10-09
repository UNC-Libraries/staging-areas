package edu.unc.lib.staging;

import java.net.URI;
import java.util.ArrayList;

public class RelativeURIPattern extends URIPattern {

	@Override
	public String getPath(URI uri) {
		return uri.getPath();
	}

	@Override
	public boolean isWithin(URI baseURI, URI fileURI) {
		return fileURI.toString().startsWith(baseURI.toString());
	}

	@Override
	public URI makeURI(URI baseURI, String... relativePath) {
		String path = baseURI.getPath();
		ArrayList<String> pathSegs = new ArrayList<String>();
		pathSegs.add(path);
		for(String s : relativePath) { pathSegs.add(s); }
		path = encodePath(pathSegs.toArray(new String[] {}));
		return URI.create(path);
	}

	@Override
	public boolean matches(URI uri) {
		return uri.getScheme() == null && uri.getPath() != null && !uri.getPath().startsWith("/");
	}

	@Override
	public boolean isLocallyMapped() {
		return false;
	}

	@Override
	public boolean isAutoconnected() {
		return true;
	}

	@Override
	public String getScheme() {
		return null;
	}
	
	

}

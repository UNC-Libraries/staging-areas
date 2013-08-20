package edu.unc.lib.staging;

import java.net.URI;
import java.util.ArrayList;

public class FileURIPattern extends URIPattern {

	@Override
	public String getPath(URI uri) {
		return uri.getPath().replaceAll("\\+", " ");
	}

	@Override
	public boolean isWithin(URI baseURI, URI fileURI) {
		return fileURI.toString().startsWith(baseURI.toString());
	}

	@Override
	public URI makeURI(URI baseURI, String... pathParts) {
		String path = baseURI.getPath();
		ArrayList<String> pathSegs = new ArrayList<String>();
		pathSegs.add(path);
		for(String s : pathParts) { pathSegs.add(s); }
		path = encodePath(pathSegs.toArray(new String[] {}));
		return URI.create("file:"+path);
	}

	@Override
	public boolean matches(URI uri) {
		return "file".equals(uri.getScheme());
	}

	@Override
	public String getScheme() {
		return "file";
	}

}

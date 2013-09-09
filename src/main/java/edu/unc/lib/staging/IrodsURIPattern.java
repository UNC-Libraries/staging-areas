package edu.unc.lib.staging;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IrodsURIPattern extends URIPattern {
	// e.g. "irods://cdr-stage.lib.unc.edu:5555/stagingZone/home/stage/alpha/"
	static final String regex = "irods://((.*)@)?([a-zA-Z0-9\\.\\-]*):(\\d{1,5})?(/.*)?";
	static final Pattern uriPattern = Pattern.compile(regex);
	static final int hostIdx = 3;
	static final int portIdx = 4;
	static final int pathIdx = 5;

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unc.lib.staging.URIPattern#getPath(java.lang.String)
	 */
	public String getPath(URI uri) {
		String result = null;
		Matcher m = uriPattern.matcher(uri.toString());
		if (m.matches()) {
			String test = m.group(pathIdx);
			if (test != null) {
				result = test;
			} else {
				result = "";
			}
		}
		try {
			return URLDecoder.decode(result,"utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new Error(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unc.lib.staging.URIPattern#isWithin(java.lang.String,
	 * java.lang.String)
	 */
	public boolean isWithin(URI baseURI, URI fileURI) {
		Matcher mFile = uriPattern.matcher(fileURI.toString());
		Matcher mBase = uriPattern.matcher(baseURI.toString());
		if (mFile.matches() && mBase.matches()) {
			if (!mFile.group(hostIdx).equals(mBase.group(hostIdx)))
				return false;
			if (!mFile.group(portIdx).equals(mBase.group(portIdx)))
				return false;
			return mFile.group(pathIdx).startsWith(mBase.group(pathIdx));
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unc.lib.staging.URIPattern#getSharedURI(java.lang.String,
	 * java.lang.String)
	 */
	public URI makeURI(URI baseURI, String... putPath) {
		Matcher mBase = uriPattern.matcher(baseURI.toString());
		if (!mBase.matches())
			return null;
		
		String user = System.getProperty("user.name");
		
		StringBuilder sb = new StringBuilder("irods://");
		sb.append(user).append("@").append(mBase.group(hostIdx)).append(":")
				.append(mBase.group(portIdx));
		
		String path = mBase.group(pathIdx);
		ArrayList<String> pathSegs = new ArrayList<String>();
		pathSegs.add(path);
		for(String s : putPath) { pathSegs.add(s); }
		path = encodePath(pathSegs.toArray(new String[] {}));
		sb.append(path);
		return URI.create(sb.toString());
	}

	@Override
	public boolean matches(URI uri) {
		return uriPattern.matcher(uri.toString()).matches();
	}

	@Override
	public String getScheme() {
		return "irods";
	}
}

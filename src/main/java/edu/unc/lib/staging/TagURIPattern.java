package edu.unc.lib.staging;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagURIPattern extends URIPattern {
	static final String regex = "tag:((.*)@)?([a-zA-Z0-9\\.]*),([0-9\\-]*):(/.*)?";
	static final Pattern uriPattern = Pattern.compile(regex);
	static final int hostIdx = 3;
	static final int dateIdx = 4;
	static final int pathIdx = 5;
	static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	@Override
	public boolean isAutoconnected() {
		return true;
	}
	
	@Override
	public boolean isLocallyMapped() {
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.unc.lib.staging.URIPattern#getPath(java.lang.String)
	 */
	public String getPath(URI uri) {
		String result = null;
		Matcher m = uriPattern.matcher(uri.toString());
		if(m.matches()) {
			String test = m.group(pathIdx);
			if(test != null) {
				result = test;
			} else {
				result = "";
			}
		}
		try {
			result = URLDecoder.decode(result, "utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new Error(e);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see edu.unc.lib.staging.URIPattern#isWithin(java.lang.String, java.lang.String)
	 */
	public boolean isWithin(URI baseURI, URI fileURI) {
		Matcher mFile = uriPattern.matcher(fileURI.toString());
		Matcher mBase = uriPattern.matcher(baseURI.toString());
		if (mFile.matches() && mBase.matches()) {
			if (!mFile.group(hostIdx).equals(mBase.group(hostIdx)))
				return false;
			return mFile.group(pathIdx).startsWith(mBase.group(pathIdx));
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.unc.lib.staging.URIPattern#getSharedURI(java.lang.String, java.lang.String)
	 */
	public URI makeURI(URI baseURI, String... putPath) {
		Matcher mBase = uriPattern.matcher(baseURI.toString());
		if(!mBase.matches()) return null;
		String user = System.getProperty("user.name");
		Date date = new Date(System.currentTimeMillis());
		String isoDate = dateFormat.format(date);
		StringBuilder sb = new StringBuilder("tag:");
		sb.append(user).append("@").append(mBase.group(hostIdx))
		.append(",").append(isoDate).append(":");
		
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
		return "tag";
	}

}

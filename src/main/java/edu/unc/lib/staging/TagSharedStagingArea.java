package edu.unc.lib.staging;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagSharedStagingArea extends AbstractSharedStagingArea {
	static String regex = "tag:((.*)@)?([a-zA-Z0-9\\.]*),([0-9\\-]*):([^/]*)(/.*)?";
	static Pattern tagURIPattern = Pattern.compile(regex);
	static final int hostIdx = 3;
	static final int dateIdx = 4;
	static final int baseNameIdx = 5;
	static final int relPathIdx = 6;
	String baseName;
	String host;
	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	public boolean matches(String stagedURI) {
		Matcher mIn = tagURIPattern.matcher(stagedURI);
		if(!mIn.matches()) return false;
		Matcher mBase = tagURIPattern.matcher(uri);
		if(!mBase.matches()) {
			// FIXME log this
			return false;
		}
		if(!mIn.group(hostIdx).equals(mBase.group(hostIdx))) return false;
		return mIn.group(baseNameIdx).equals(mBase.group(baseNameIdx));
	}

	@Override
	String getRelativePath(String stagedFileURI) {
		Matcher m = tagURIPattern.matcher(stagedFileURI);
		if(m.matches()) return m.group(relPathIdx);
		return null;
	}

	@Override
	public String makeStagedFileURI(String localURL) {
		//  FIXME include date and user
		String result = null;
		if(this.resolvedMapping != null && localURL.startsWith(this.resolvedMapping)) {
			StringBuilder sb = new StringBuilder();
			String relPath = localURL.substring(this.resolvedMapping.length());
			sb.append("tag:");
			String user = System.getProperty("user.name");
			if(user != null) sb.append(user).append("@");
			sb.append(this.host);
			sb.append(":").append(this.baseName).append(relPath);
			result = sb.toString();
		}
		return result;
	}

	@Override
	public void init() throws StagingException {
		super.init();
		Matcher m = tagURIPattern.matcher(this.uri);
		if(m.matches()) {
			this.baseName = m.group(baseNameIdx);
			this.host = m.group(hostIdx);
		} else {
			throw new StagingException("Staging Area URI does not match Tag URI pattern.");
		}
	}

	@Override
	public String getLocalURL(String stagedFileURI) {
		String relPath = this.getRelativePath(stagedFileURI);
		if(relPath == null) return null;
		return this.resolvedMapping+relPath;
	}

	public String getStagedURI(File file) {
		String result = null;
		if(this.resolvedMapping != null) {
			String uri = file.toURI().toString();
			String relPath = uri.substring(this.resolvedMapping.length(), uri.length());
			String user = System.getProperty("user.name");
			Date date = new Date(System.currentTimeMillis());
			String isoDate = dateFormat.format(date);
			StringBuilder sb = new StringBuilder("tag:");
			sb.append(user).append("@")
				.append(this.host).append(",").append(isoDate).append(":");
			sb.append(this.baseName).append(relPath);
			result = sb.toString();
		}
		return result;
	}

	public String getCustomMapping() {
		return this.customMapping;
	}

}

package edu.unc.lib.staging;

import java.text.MessageFormat;
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
	String makeStagedFileURI(String localURL) {
		String result = null;
		if(this.resolvedMapping != null && localURL.startsWith(this.resolvedMapping)) {
			String relPath = null;
		}
		return result;
	}

	@Override
	public void init() throws StagingException {
		super.init();
		Matcher m = tagURIPattern.matcher(this.uri);
		if(m.matches()) {
			
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

}

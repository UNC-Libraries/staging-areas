package edu.unc.lib.staging;

public class StagingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5080246589080348851L;

	public StagingException(String message) {
		super(message);
	}
	
	public StagingException(String message, Exception e) {
		super(message, e);
	}

}

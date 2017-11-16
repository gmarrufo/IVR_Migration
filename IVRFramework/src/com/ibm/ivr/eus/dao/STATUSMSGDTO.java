package com.ibm.ivr.eus.dao;


/**
* @author Sibyl Sullivan
* @doc
* 		The STATUSMSGDTO class is a container 
* 		used by the STATUSMSGDAO to interface to the EUSDB database 
* 
*       This class represents a complete status message record
* 
*/

public class STATUSMSGDTO {

	private String appName = null;
	private String messageID = null;
	private String language = null; 
	private String startTime = null;
	private String endTime = null;
	private boolean  bargein = false;
	private String VDN = null;

	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getMessageID() {
		return messageID;
	}
	public void setMessageID(String messageID) {
		this.messageID = messageID;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public boolean isBargein() {
		return bargein;
	}
	public void setBargein(boolean bargein) {
		this.bargein = bargein;
	}
	// convert string of "true" to boolean true all other strings set bargein to false
	public void setBargein(String bargeinStr) {
		if (bargeinStr.equalsIgnoreCase("true") ) {
			this.bargein = true;
		}
		else {
			this.bargein = false;
		}		
	}

	public String getVDN() {
		return VDN;
	}
	public void setVDN(String vdn) {
		VDN = vdn;
	}

}




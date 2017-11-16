package com.ibm.ivr.eus.handler;

public class VT_Request_Data {
	
	private String primaryURL = null;
	private String backupURL = null;
	private String vruNumber = null;
	private String serialNumber = null;
	private String tenant = null;
	private String operationType = null;
	private String language = null;
	private String origCallerId = null;
	private String storeId = null;
	private String sessionId = null;
	/**
	 * @return the vruNumber
	 */
	public final String getVruNumber() {
		return vruNumber;
	}
	/**
	 * @param vruNumber the vruNumber to set
	 */
	public final void setVruNumber(String vruNumber) {
		this.vruNumber = vruNumber;
	}
	/**
	 * @return the serialNumber
	 */
	public final String getSerialNumber() {
		return serialNumber;
	}
	/**
	 * @param serialNumber the serialNumber to set
	 */
	public final void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	/**
	 * @return the tenant
	 */
	public final String getTenant() {
		return tenant;
	}
	/**
	 * @param tenant the tenant to set
	 */
	public final void setTenant(String tenant) {
		this.tenant = tenant;
	}
	/**
	 * @return the operationType
	 */
	public final String getOperationType() {
		return operationType;
	}
	/**
	 * @param operationType the operationType to set
	 */
	public final void setOperationType(String operationType) {
		this.operationType = operationType;
	}
	/**
	 * @return the language
	 */
	public final String getLanguage() {
		return language;
	}
	/**
	 * @param language the language to set
	 */
	public final void setLanguage(String language) {
		String lang = null;
		if (language.compareToIgnoreCase("en_US") == 0) {
			lang = "en-US";
		} else if (language.compareToIgnoreCase("en_US") == 0) {
			lang = "en-UK";
		} else if (language.compareToIgnoreCase("it_IT") == 0) {
			lang = "it-IT";
		} else if (language.compareToIgnoreCase("fr_FR") == 0) {
			lang = "fr-FR";
		} else if (language.compareToIgnoreCase("sp_SP") == 0) {
			lang = "sp-SP";
		} else if (language.compareToIgnoreCase("de_DE") == 0) {
			lang = "de-DE";
		}
		
		this.language = lang;
	}
	/**
	 * @return the origCallerId
	 */
	public final String getOrigCallerId() {
		return origCallerId;
	}
	/**
	 * @param origCallerId the origCallerId to set
	 */
	public final void setOrigCallerId(String origCallerId) {
		this.origCallerId = origCallerId;
	}
	/**
	 * @return the primaryURL
	 */
	public final String getPrimaryURL() {
		return primaryURL;
	}
	/**
	 * @param primaryURL the primaryURL to set
	 */
	public final void setPrimaryURL(String primaryURL) {
		this.primaryURL = primaryURL;
	}
	/**
	 * @return the backupURL
	 */
	public final String getBackupURL() {
		return backupURL;
	}
	/**
	 * @param backupURL the backupURL to set
	 */
	public final void setBackupURL(String backupURL) {
		this.backupURL = backupURL;
	}
	/**
	 * @return the storeId
	 */
	public final String getStoreId() {
		return storeId;
	}
	/**
	 * @param storeId the storeId to set
	 */
	public final void setStoreId(String storeId) {
		this.storeId = storeId;
	}
	/**
	 * @return the sessionId
	 */
	public final String getSessionId() {
		return sessionId;
	}
	/**
	 * @param sessionId the sessionId to set
	 */
	public final void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

}

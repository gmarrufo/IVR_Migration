package com.ibm.ivr.eus.handler;

public class VT_Response_Data {
	
	private int retCode = -1;
	private String retStatus = null;
	private String lastModified = null;
	private String enrollStatus = null;
	private String userInfo = null;
	private String retData = null;
	/**
	 * @return the retCode
	 */
	public final int getRetCode() {
		return retCode;
	}
	/**
	 * @param retCode the retCode to set
	 */
	public final void setRetCode(int retCode) {
		this.retCode = retCode;
	}
	/**
	 * @return the retStatus
	 */
	public final String getRetStatus() {
		return retStatus;
	}
	/**
	 * @param retStatus the retStatus to set
	 */
	public final void setRetStatus(String retStatus) {
		this.retStatus = retStatus;
	}
	/**
	 * @return the lastModified
	 */
	public final String getLastModified() {
		return lastModified;
	}
	/**
	 * @param lastModified the lastModified to set
	 */
	public final void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}
	/**
	 * @return the enrollStatus
	 */
	public final String getEnrollStatus() {
		return enrollStatus;
	}
	/**
	 * @param enrollStatus the enrollStatus to set
	 */
	public final void setEnrollStatus(String enrollStatus) {
		this.enrollStatus = enrollStatus;
	}
	/**
	 * @return the userInfo
	 */
	public final String getUserInfo() {
		return userInfo;
	}
	/**
	 * @param userInfo the userInfo to set
	 */
	public final void setUserInfo(String userInfo) {
		this.userInfo = userInfo;
	}
	/**
	 * @return the retData
	 */
	public final String getRetData() {
		return retData;
	}
	/**
	 * @param retData the retData to set
	 */
	public final void setRetData(String retData) {
		this.retData = retData;
	}

}

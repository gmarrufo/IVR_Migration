/*
 * Created on Nov 12, 2010
 *
 */
package com.ibm.ivr.eus.dao;


/**
 * @author mcdonley
  * 	The EUSVARDTO class is a container 
 * 		used by the EUSVARDAO interface to the EUSDB database 
 *   	EUSVARDTO objects contain one appName-KEY-VALUE triplet
 *   
 *      appName is used as the table name in the EUSDB - i.e. IBMHELP for the IBM Help application
 * 		KEY is the application variable name
 * 		VALUE is the unique String value for the KEY
 * 				 
 * 		There are six methods - a public getter and a setter for each private variable
*
 * 
 * 
 */
public class EUSVARDTO {

	private String appName = null;
	private String KEY = null;
	private String VALUE = null;
	/**
	 * @return the KEY
	 */
	public String getKEY() {
		return KEY;
	}
	/**
	 * @param key the KEY to set
	 */
	public void setKEY(String key) {
		KEY = key;
	}
	/**
	 * @return the VALUE
	 */
	public String getVALUE() {
		return VALUE;
	}
	/**
	 * @param value the VALUE to set
	 */
	public void setVALUE(String value) {
		VALUE = value;
	}
	/**
	 * @return the appName
	 */
	public String getAppName() {
		return appName;
	}
	/**
	 * @param appName the appName to set
	 */
	public void setAppName(String appName) {
		this.appName = appName;
	}
	
}

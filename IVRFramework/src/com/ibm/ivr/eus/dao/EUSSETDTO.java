/*
 * Created on Nov 16, 2010
 *
 * 
 */
package com.ibm.ivr.eus.dao;

import java.util.Vector;




/**
 * @author mcdonley
 * @doc
 * 		The EUSSETDTO class is a container 
 * 		used by the EUSSETDAO interface to the EUSDB database 
 *   	EUSSETDTO objects contain one setName-KEY-VALUESET triplet
 *   
 *      setName is used as the table name in the EUSDB - ie IBM_NA_EFH_ALPHA for North American serial numbers
 * 		KEY is the search string that will come out of a telephony "get serial" dialog 
 * 															(with '*' entered for alpha chars)
 * 		VALUESET is a Java StringBuffer Vector filled with one or more DB entries which match the KEY
 * 
 * 		There are six methods - a public getter and a setter for each private variable
 * 
 * 
 */
public class EUSSETDTO {

	private String setName = null;
	private String KEY = null;
	private Vector<StringBuffer> VALUESET = null;
	/**
	 * @return the setName
	 */
	public String getSetName() {
		return setName;
	}
	/**
	 * @param setName the setName to set
	 */
	public void setSetName(String setName) {
		this.setName = setName;
	}
	/**
	 * @return the KEY
	 */
	public String getKEY() {
		return KEY;
	}
	/**
	 * @param key  KEY for VALUESET rows
	 */
	public void setKEY(String key) {
		KEY = key;
	}
	/**
	 * @return the VALUESET
	 */
	public Vector<StringBuffer> getVALUESET() {
		return VALUESET;
	}
	/**
	 * @param valueset the VALUESET
	 */
	public void setVALUESET(Vector<StringBuffer> valueset) {
		VALUESET = valueset;
	}

}

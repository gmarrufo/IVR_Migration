package com.ibm.ivr.eus.plugin;


import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;


import org.apache.log4j.Logger;

import com.ibm.websphere.wim.model.Context;

/**
 * The default gsmart logger to write to GSMART log file
 * <p>
 * 
 * Revision history:
 * <p>
 * 
 * 2010-11-22: initial version
 * <p>
 * 
 * @author Sibyl Sullivan
 * @version 2010-11-22
 *  
 */
public class GsmartLoggerHelper {
	
    // add a class variable for the last uniqueID used
    private static int lastID = 0;

	//Option Descriptions	
	private static String STATUS_MESSAGE ="STATUS_MESSAGE";
	private static String MENU_AVOIDED = "MENU AVOIDED";
	private static String AUTOMATION = "AUTOMATION";
	private static String NOT_AVAILABLE = "NOT_AVAILABLE";
	private static String BLANK = "";
	private static String NO_ERROR = "NO_ERROR";
	private static String CALL_TRANSFERRED	= "CALL TRANSFERRED";
	private static String HANGUP = "HANGUP";	
	private static String CALLER_HANGUP = "CALLER_HANGUP";
	
	//define the private logger
	private static Logger LOGGER = Logger.getLogger(GsmartLoggerHelper.class);


	/**
	 * Used by IVR to report call end
	 * 
	 * @param sessionID
	 *            unique session id of the call
	 * @param appName
	 *            application reached by the call
	 * @param separator
	 *            the character to use to separate fields 
	 * @param callStartTS
	 *            the time stamp for the beginning of the call 
	 *                     	 
	 * @param DNIS
	 *            the DNIS (called number) SCA VDN
	 *            
	 * @param menuLevel
	 *            session variable provided by XML app  	 
	 *            
	 * @param callDisposition
	 *            session variable provided by XML app  - defaults to other 
 	 *
	 * @param xfer_var
	 *            session variable provided by XML app  - used only when CallDisp = TF 	 
	 *
	 */
	

	public void callEnd(ServletContext arg0, String sessionID, String appName, Timestamp callStartTS, String DNIS, String menuLevel, String callDisposition, String xfer_var) {

		String logToken = new StringBuffer("[").append(sessionID).append("] ").append("[").append(appName).append("]: ")
		.toString();

		boolean localTestCall = false;


		
		if (localTestCall) {
			LOGGER.info("*** in GsmartLoggerHelper - sessionID = " + sessionID + "  , appName = " + appName + " , menuLevel = " + menuLevel);
		}
		
		if (sessionID == null) {
			LOGGER.warn(new StringBuffer(logToken)
					.append("sessionID is null, returning..."));
			return;
		}
			// Start time
		String startDate = new SimpleDateFormat("yyyy-MM-dd").format(callStartTS);
		String startTime = new SimpleDateFormat("HHmmss").format(callStartTS);

			// End time
		Timestamp curTS = new Timestamp((new java.util.Date()).getTime());
 
		String endDate = new SimpleDateFormat("yyyy-MM-dd").format(curTS);  		
		String endTime = new SimpleDateFormat("HHmmss").format(curTS);
		
	    	// create unique id for today on this machine
		if (GsmartLoggerHelper.lastID > 9990) 
			GsmartLoggerHelper.lastID = 0;
	    StringBuffer uniqueID = new StringBuffer(endTime).append("-").append(Integer.toString(GsmartLoggerHelper.lastID++));

			// get global properties needed 
		Properties globalProp = (Properties) arg0.getAttribute("globalProp");
		String separator = globalProp.getProperty("gsmartSeparator");
		if (separator.equalsIgnoreCase("SPACE_CHAR")) {
			separator = " ";
		}
		
			// SCS June 01, 2011 - need to modify menuLevel which is really cur_prompt to required format
		String tempMenuLevel = "M";
		if ( menuLevel.endsWith("main") || menuLevel.endsWith("Main") || menuLevel.endsWith("MAIN") ) {
			// done
			menuLevel = "";
		} else {
			menuLevel = menuLevel.substring(5);
			if (localTestCall) {
				LOGGER.info("*** in GsmartLoggerHelper - changing value now  menuLevel = " + menuLevel);
			}
		}
		if (menuLevel.contains("_")) {
			menuLevel = menuLevel.replaceAll("_", "");
		}

		tempMenuLevel = tempMenuLevel.concat(menuLevel);
		menuLevel = tempMenuLevel;
		if (localTestCall) {
			LOGGER.info("*** in GsmartLoggerHelper - DONE changing >>>  menuLevel = " + menuLevel);
		}
		
		
		String paddedAppName = padRight(appName, 20);
		
//		LOGGER.info("padRight test:" + padRight("XX", 10) + ":");
//		LOGGER.info("padLeft test:" + padLeft("XX", 10)  + ":");
		
		// SCS June 14, 2011 - modified to pad newVDN on right not left 
		String VDN; 
		String newVDN;		
		if (DNIS.length() > 6) {
			VDN = "XXXXXX";
			newVDN = padRight(DNIS, 15);
		}
		else {
			VDN = padRight(DNIS, 6);
			newVDN = padRight(" ", 15);
		}
		
//		LOGGER.debug("VDNs are " + VDN + " and " + newVDN);
		
		String statusAvoided = null;
		String autoAvoided = null;
		String callTransfer = null;
		String conditionCode = null; 
		String conditionDescription = null;
		String optionDescription = null;
		
// Sibyl chgd conditionCode padding to padRight as per Ian's note on 7-18-2011
		
		if (callDisposition.equalsIgnoreCase("SA")){
			statusAvoided = padLeft("1", 7);
			autoAvoided = padLeft("0", 7);
			callTransfer = padLeft("0", 7);
			conditionCode = padRight("0", 5);
			conditionDescription = padRight(NO_ERROR, 25);
			optionDescription = padRight(STATUS_MESSAGE, 25);
		} else {
			if (callDisposition.equalsIgnoreCase("AD")){
				statusAvoided = padLeft("1", 7);
				autoAvoided = padLeft("0", 7);
				callTransfer = padLeft("0", 7);
				conditionCode = padRight("0", 5);
				conditionDescription = padRight(NO_ERROR, 25);
				optionDescription = padRight(MENU_AVOIDED, 25);
			}
			else {
				if (callDisposition.equalsIgnoreCase("AT")){
					statusAvoided = padLeft("0", 7);
					autoAvoided = padLeft("1", 7);
					callTransfer = padLeft("0", 7);
					conditionCode = padRight("0", 5);
					conditionDescription = padRight(NO_ERROR, 25);
					optionDescription = padRight(AUTOMATION, 25);
				}
				else {
					if (callDisposition.equalsIgnoreCase("TF")){
						statusAvoided = padLeft("0", 7);
						autoAvoided = padLeft("0", 7);
						callTransfer = padLeft("1", 7);
						conditionCode = padRight("0", 5);
						conditionDescription = padRight(CALL_TRANSFERRED, 25);
						optionDescription = padRight(xfer_var, 25);
						
					}
					else {
						if (callDisposition.equalsIgnoreCase("HU")){
							statusAvoided = padLeft("0", 7);
							autoAvoided = padLeft("0", 7);
							callTransfer = padLeft("0", 7);
							conditionCode = padRight("HUP", 5);
							conditionDescription = padRight(HANGUP, 25);
							optionDescription = padRight(NOT_AVAILABLE, 25);
						}
						else {
							// other case
							statusAvoided = padLeft("0", 7);
							autoAvoided = padLeft("0", 7);
							callTransfer = padLeft("0", 7);
							conditionCode = padRight("HUP", 5);
							conditionDescription = padRight(CALLER_HANGUP, 25);
							optionDescription = padRight(NOT_AVAILABLE, 25);	
						}				
					}
				}
			}		
		}

		String uniqueRecordID = padRight(uniqueID.toString(), 11);
		
		// MenuLevel 
		String fixedLengthMenuLevel = (String) padRight(menuLevel, 6);
		
		
		// SCS June 14, 2011 - modified to add missing separator btwn VDN and fixedLengthMenuLevel 

		StringBuffer sb = new StringBuffer("DT2").append(separator).append(startDate).append(separator).append(startTime).append(separator)
		.append(paddedAppName).append(separator).append(VDN).append(separator).append(fixedLengthMenuLevel)
		.append(separator).append(optionDescription).append(separator).append(statusAvoided).append(separator)
		.append(autoAvoided).append(separator).append(callTransfer).append(separator).append(conditionCode).append(separator).append(conditionDescription).append(separator)
		.append(endDate).append(separator).append(endTime).append(separator).append(uniqueRecordID).append(separator).append(newVDN);

		LOGGER.info(sb);
		
	}
	
	public static String padRight(String s, int n) {
		// If the string is longer than the field width, truncate the string on the right side
		if(n > 0 && s != null && s.length() > n)
			s = s.substring(0,n);
	    return String.format("%1$-" + n + "s", s);   
	} 

	public static String padLeft(String s, int n) {
		// If the string is longer than the field width, truncate the string on the left side
		if(n > 0 && s != null && s.length() > n)
			s = s.substring(s.length()-n);
		
	    return String.format("%1$" + n + "s", s);   
	} 

	// Test out padding functions
	public static void main(String[] args) {
		System.out.println("R"+ padRight("abcde", 10) + "R");
		System.out.println("R"+ padRight("abcde", 5) + "R");
		System.out.println("R"+ padRight("abcde", 2) + "R");
		System.out.println("L"+ padLeft("abcde", 10) + "L");
		System.out.println("L"+ padLeft("abcde", 5) + "L");
		System.out.println("L"+ padLeft("abcde", 2) + "L");
	}
}
package com.ibm.ivr.eus.plugin;


import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;

import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Appender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.apache.log4j.PropertyConfigurator;

import com.ibm.ejs.jms.listener.ServerSession;
import com.ibm.ivr.eus.plugin.expressions.ExpressionHelperFunctions;
import com.ibm.ivr.framework.plugin.PluginException;
import com.ibm.websphere.wim.model.Context;

/**
 * The application logger to write to an application specific log file
 * <p>
 * 
 * Revision history:
 * <p>
 * 2010-12-07: initial version
 * 2011-01-07 - SCS - removed parsing replaced with call to expression handler to evaluate ApplicationLogData string
 * 2011-03-14 - SCS - cleaned up logging for function itself
 * 2011-05-26 - SCS - added logic to modify some session variables being logged look for for logging purposes in desc
 * <p>
 * 
 * @author Sibyl Sullivan
 * @version 2010-12-07
 *  
 */
public class ApplicationLoggerHelper {
	
 
	//define the private logger
	private static Logger LOGGER = Logger.getLogger(ApplicationLoggerHelper.class);
	//this logger definition is used to create application specific loggers
	//ensure that this logger is defined in log4j.properties file
	private static Logger APPLOGGER = Logger.getLogger("applogger");

	/**
	 * Used by IVR to report call end
	 * 
 	 * @param context
	 *            ServletContext for the applicaition
	 * @param session
	 *            HttpSession for this call
	 * @param sessionID
	 *            unique session id of the call
	 * @param appName
	 *            application reached by the call
	 * @param callStartTS
	 *            the time stamp for the beginning of the call -- NOT USED at this time
	 *                     	 
	 * @param applicationLogData
	 *            the string of what needs to be logged
	 *            any text in (parenthesis)is treated as session variable and will be substituted with value 
	 *            if no session variable exits null will be used as value
	 *            
	 * @param applicationLogDataEvaluate
	 *            if set to FALSE - will not send data to expression plugin to be evaluated.
	 *            
	 *            
	 *            
	 */
	
	public void callEnd(ServletContext context, HttpSession session, String sessionID, String appName, Timestamp callStartTS, 
			String applicationLogData, String applicationLogDataEvaluate) {

		boolean testCall = ((Boolean) session.getAttribute("testCall")).booleanValue();
		String callid = (String) session.getAttribute("callid");
		//create the log Token for later use, use StringBuffer to reduce number
		//of String objects
		String logToken = new StringBuffer("[").append(callid).append("] ").toString();
		
		if (testCall) {
			LOGGER.info(new StringBuffer(logToken).append("*** in ApplicationLoggerHelper - sessionID = " + sessionID + "  , appName = " + appName));
			LOGGER.info(new StringBuffer(logToken).append("*** applicationLogData = " + applicationLogData 
					+ " , applicationLogDataEvaluate = " + applicationLogDataEvaluate));
			LOGGER.info(new StringBuffer(logToken).append("*** before modifications menuLevel = " + (String) session.getAttribute("menuLevel")
						+ " cur_prompt = " + (String) session.getAttribute("cur_prompt") + " cur_ini = " + (String) session.getAttribute("cur_ini")
					+ " xfer_number = " + (String) session.getAttribute("xfer_number") + " ANI = " + (String) session.getAttribute("ANI"))
					+ " last_prompt = " + (String) session.getAttribute("last_prompt"));
		}
		
		// SCS 05/11/2011 added for logging purposes,  they wanted to have a line number of 0
		session.setAttribute("voice_line", 0);
		
		// SCS 05/27/2011 added for logging purposes used by ACE,  they need to set VDN to framework DNIS
		// SCS 06/02/2011 - removed as this is now being set by Framework
		// String vdn = (String) session.getAttribute("DNIS");
		// session.setAttribute("VDN", vdn);
		
		// SCS 05/17/2011 modified cur_prompt value for logging purposes - based on input form Greg cur_prompt must be generated from menuLevel
//		if ( (String) session.getAttribute("menuLevel") != null  )	{
//			if (! (((String) session.getAttribute("menuLevel")).equalsIgnoreCase("M")) ) {
//				session.setAttribute("cur_prompt", ( "MAIN_" + (String) session.getAttribute("menuLevel")));
//			}
//			else {
//				session.setAttribute("cur_prompt", "MAIN");
//			}
//		}
		
		// SCS 05/26/2011 modifed for logging purposes setting cur_prompt to new shadow var
		// SCE 05/31/2011 removed logic - latest is that cur_prompt will have correct value
//		if ( (String) session.getAttribute("last_prompt") != null  )	{
//			String cur_prompt = (String) session.getAttribute("last_prompt");		
//			session.setAttribute("cur_prompt", cur_prompt);			
//		} else {
//			session.setAttribute("cur_prompt", "MAIN");
//		}
		
		if (testCall) {
			LOGGER.info(new StringBuffer(logToken).append("*** after modifications menuLevel = " + (String) session.getAttribute("menuLevel")
					+ " cur_prompt = " + (String) session.getAttribute("cur_prompt") +" VDN = " + (String) session.getAttribute("VDN")));
		}
		
		// SCS 05/17/2011 modified cur_ini for logging purposes to drop the _VARS 
		// SCS 05/30/2011 modified to use cur_ini is blank
		if ((String) session.getAttribute("cur_ini") != null) {
			String cur_ini = ((String) session.getAttribute("cur_ini"));
			if (cur_ini.endsWith("_VARS")) {
				cur_ini = cur_ini.substring(0, (cur_ini.length() - 5));
				session.setAttribute("cur_ini", cur_ini);
			}
		} else {
			// SCS 05/30/2011 assign the subMenuName to cur_ini  -- 
			String subMenuName = ((String) session.getAttribute("iSubMenuName"));		
			session.setAttribute("cur_ini", subMenuName);	
		}

		
		// SCS 05/17/2011 modified transfer number  for logging purposes to drop the ending # key 
//		if ((String) session.getAttribute("xfer_number") != null) {
//			String xfer_number = ((String) session.getAttribute("xfer_number"));
//			if (xfer_number.endsWith("#")) {
//				xfer_number = xfer_number.substring(0, (xfer_number.length() - 1));
//				session.setAttribute("xfer_number", xfer_number);
//			}
//		}

		// SCS 05/30/2011 modified transfer number for logging purposes to include the prefix 
		String prefix = "";
		String transfer_agent_type = (String) session.getAttribute("transfer_agent_type");
		if (transfer_agent_type != null) {
			if (transfer_agent_type.equals("agent")) {
				prefix = "";
			} else if (transfer_agent_type.equals("lineagent")) {
				prefix = "&," + session.getAttribute("lineAgentPrefix");
			} else if (transfer_agent_type.equals("converseagent")) {
				prefix = "&," + session.getAttribute("converseAgentPrefix");
			}
		}
		String xfer_number = prefix + ((String) session.getAttribute("xfer_number"));
		session.setAttribute("xfer_number", xfer_number);	
		
		
		// SCS 05/16/2011 modified to get ANI for logging purposes
		// SCS 05/27/2011 modified to log ANI
		String ani = (String) session.getAttribute("ANI");
		if (testCall) {
			LOGGER.info(new StringBuffer(logToken).append("*** ANI (ani) = " + ani + " *** "));
		}		
		if (ani == null || ani.isEmpty())
			ani = (String) context.getAttribute("ANI");
		if (ani == null || ani.isEmpty())
			ani = "NA";
		
		session.setAttribute("ANI", ani);
		
		
		if (testCall) {
			LOGGER.info(new StringBuffer(logToken).append("*** after modifications cur_ini = " + (String) session.getAttribute("cur_ini")
					+ " xfer_number = " + (String) session.getAttribute("xfer_number") + " ANI = " + (String) session.getAttribute("ANI")));
		}		
		
		
		// set applicationLogDataEvaluate to default of true is null
		if (applicationLogDataEvaluate == null ) {
			applicationLogDataEvaluate = "TRUE";
			if (testCall) {
				LOGGER.info(new StringBuffer(logToken).append("*** applicationLogDataEvaluate was not set - using default of TRUE"));
			}
		}

		// check if Logger for appname exists in Hashtable
		Hashtable<String, Logger> attribute = (Hashtable<String, Logger>) context.getAttribute("appLoggers");
		Hashtable<String, Logger> appLoggersTable =  attribute;																								
//		LOGGER.debug("*** - have hashTable out of session ");
																								   																									   
		Logger appLogger = null; 
		
		if (! appLoggersTable.equals(null)) {
			
			if (testCall) {
				LOGGER.info(new StringBuffer(logToken).append("*** attemting to get appLogger for this application"));
			}
			appLogger = (Logger) appLoggersTable.get(appName);
			if (! (appLogger == null)) {
//				LOGGER.debug("*** - stored appLogger is " + appLogger.toString());
				
			} else {
				if (testCall) {
					LOGGER.info(new StringBuffer(logToken).append("*** appLogger is null - will need to create new logger for this app"));
				}
			}


		} else {		
			LOGGER.warn("-- could not get appLoggersTable out of context -- ");
		}
		
		// if no logger for this app then create it and save in hashtable
		if (appLogger == null)  {

//			LOGGER.debug("***  - appLogger is null so need to create a new Appender for this application");

			DailyRollingFileAppender appndr = (DailyRollingFileAppender) APPLOGGER.getAppender("AppLog");

			if (appndr == null)
				LOGGER.fatal(logToken + " -- Can not create the template appender for application logging - check EUS log4j properties -- ");

			// appndr.getFile() should be returning = /eus_ivr/applog/applog.log in std config 
			
			String fileName = (((FileAppender) appndr).getFile());

//			LOGGER.debug("*** -  new appndr.getFile() = " + fileName);
			
			// get base Directory from fileName
			File logDirFile = new File(fileName).getParentFile();			
			String appDirName = logDirFile.getPath() + "/" + appName;
			File appLogDirFile = new File(appDirName);
//			LOGGER.debug("*** -  applogDirFile = " + appLogDirFile);
		
			// create directory if it does not exist;
			appLogDirFile.mkdirs();
			
			// create new filename using appDirName = (base app directory + "/" + "applog.log")			
			String applogFileName = appDirName + "/" + "applog.log";
			if (testCall) {
				LOGGER.info(new StringBuffer(logToken).append("*** new applogFileName = " + applogFileName));
			}
			
			//Create a new appender
			DailyRollingFileAppender appAppender;
			try {
				appAppender = new DailyRollingFileAppender(appndr.getLayout(), applogFileName, ((DailyRollingFileAppender) appndr).getDatePattern());
			// set logging threshold based on template from log4j.properties
			appAppender.setThreshold(appndr.getThreshold());
			appAppender.activateOptions();
			
			//Create new Logger
			appLogger = Logger.getLogger(appName);
			appLogger.addAppender(appAppender);
			
			// save it in the hashtable
//			LOGGER.debug("***  -  about to save in the context, the new appLogger = " + appLogger.toString() + " , appApender = "+ appAppender.toString());

			appLoggersTable.put(appName, appLogger);
//			appLogger.info("This is a test of an applicaiton log entry just created");
			} catch (IOException e) {

				LOGGER.error(logToken + " could not create the new appender for applicaiton " + appName + " , applogFileName = "+ applogFileName);
				e.printStackTrace();
			}
			
		}
			
			// then use appLogger

		// create a token to include at start of each line -- NOT USED 
		// String logToken = new StringBuffer("[").append(sessionID).append("] ").append("[").append(appName).append("]: ")
		// .toString();
	
		if (sessionID == null) {
			LOGGER.warn(logToken + " -- sessionID is null, returning...");
			return;
		}

		
		// test to see if data string needs to be sent to expression handler 
		if (applicationLogDataEvaluate.compareToIgnoreCase("TRUE") == 0) {

			// call expression plugin to evaluate applicationLogData
			ExpressionPluginImpl ep = new ExpressionPluginImpl();
			String evalString;
			try {
				evalString = ""+ep.evaluateExpression(applicationLogData, session);
				LOGGER.info(logToken + "*** Data string to log = " + evalString);
				appLogger.info(evalString);	  

			} catch (PluginException e) {

				LOGGER.warn(logToken + "exception on expression evlauation of applicationLogData");
				appLogger.warn("WARNING ************ exception on expression evlauation of applicationLogData");
				e.printStackTrace();
			}
		} else {
			// do not evaluate data string
			appLogger.info(applicationLogData);	  
		}
		
	}
	

	 /*            parseData3 - no longer used -- the expressison handler is being called instead 
	  * 
	  * 		   any text in (parenthesis)is treated as session variable and will be substituted with value 
	  *            if no session variable exits null will be used as value
	  *            
	  */
	public String parseData3( HttpSession session, String inputString) {

		LOGGER.debug("*** in parseData3 *** ");
		
		int i = 0;
		StringTokenizer st = new StringTokenizer(inputString, "(");
		int varCount = st.countTokens();
		LOGGER.debug("varCount/numOfTokens is = " + varCount);
		String [] tokenList = new String[varCount];
		StringBuffer tempString = new StringBuffer("");

		
		while (st.hasMoreTokens()) {
			String subString = st.nextToken();
			if ( (subString != null) && (subString.length() != 0) ) {
				tokenList[i] = subString;
				i++;
			}
		}
		
		// process tokens
		i = 0;
		while (i < varCount ) {
			LOGGER.debug(" ** (parseData) -varList[" + i + "] = " + tokenList[i]);


			int indexOfEndParen = tokenList[i].indexOf(")");
			if (indexOfEndParen != -1){
				// this token includes a session variable
				String sessionVarName = tokenList[i].substring(0, indexOfEndParen);
				
				// get the session variable value and append to string
//				LOGGER.debug("getting session var: "+ sessionVarName);		
				String sessionVarValue =  (String) session.getAttribute(sessionVarName);
				tempString.append(sessionVarValue);

				String remainderOfToken = tokenList[i].substring(indexOfEndParen+1);
//				LOGGER.debug("remainder of string is : "+ remainderOfToken);
				
				tempString.append(remainderOfToken);
				
			} else {
				tempString.append(tokenList[i]);
			}
				i++;
		}
		
		LOGGER.debug(" ** (parseData3) is done and tempString = " + tempString.toString());
		return tempString.toString();
	}

}
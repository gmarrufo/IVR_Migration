package com.ibm.ivr.eus.plugin;

import java.util.Properties;
import java.util.Vector;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import com.ibm.ivr.framework.plugin.CleanupPlugin;
import com.ibm.ivr.framework.plugin.PluginException;
import com.ibm.ivr.eus.common.LFileFunctions;
// import com.ibm.ivr.framework.utilities.Reporter;
import com.ibm.ivr.eus.plugin.GsmartLoggerHelper;
import com.ibm.ivr.eus.plugin.expressions.ExpressionHelperFunctions;

public class Cleanup implements CleanupPlugin {
	/**
	 * Generated serialVersionUID for Rational
	 */
	private static final long serialVersionUID = -4243990691584973217L;
	
	/**
	 * The private log4j logger
	 */
	private static Logger LOGGER = Logger.getLogger(Cleanup.class);
	@Override
	public void cleanup(ServletContext arg0, HttpSession session)
			throws PluginException {
		// TODO Auto-generated method stub
		
		boolean testCall = ((Boolean) session.getAttribute("testCall")).booleanValue();
		String callid = (String) session.getAttribute("callid");
		//create the log Token for later use, use StringBuffer to reduce number
		//of String objects
		String logToken = new StringBuffer("[").append(callid).append("] ").toString();
		
		if (testCall)
			LOGGER.info(new StringBuffer(logToken).append("*** CleanUp PlugIn"));
				
		String sessionID = session.getId();	
		
		// set call disposition to other in case session variable does not exist
		String callDisposition = "other"; 
		
		//----------------------------------------------------------------
		// July 17, 2011 SCS - modify call_disposition to HUP if caller hung up
		// 						CMVC defect 1705995S
		//----------------------------------------------------------------
		String reason = null;
		
		// Jan 17, 2012 needed to add logic to look at status_avoided and reset call_disposiiton if set to Y
		if (
				((String) session.getAttribute("exitReasonForCleanupPlugin") != null) &&
				((String) session.getAttribute("call_disposition") != null) && 
				((String) session.getAttribute("status_avoided") != null)
			) {
		   reason = (String) session.getAttribute("exitReasonForCleanupPlugin");
		   String cd = (String) session.getAttribute("call_disposition");
		   String sa = (String) session.getAttribute("status_avoided");
		   
		   // if call is transferred then set status_avoided to 'N' - this was previously done in
		   // XFERLOOKUP handler but this code will take care of cases when the caller hangs up
		   // during a transfer and the WVR browser catches the hangup event and notifies the application
		   // This logic depends on the exitReason being set to xferAgent, xferLineAgent or xferConverseAgent
		   // which should be the case as these target names are set in common-globals.xml and should not be
		   // changed.  If any new XML code is written to transfer a call then ensure that the targetName in
		   // the new code contains the string 'xfer'.
		   if (reason.indexOf("xfer") != -1) {
			   session.setAttribute("status_avoided", "N");
			   sa = "N";
		   }
		   
			if ((reason.indexOf("Hungup") != -1) 
					|| (reason.indexOf("Hangup") != -1)) {
				
				if (cd.equalsIgnoreCase("TF")   ){
					session.setAttribute("call_disposition", "HU"); 
				}
				// Jan 17, 2012 SCS added logic to override to SA status avoided if the caller hungup and he the status_avoided is set to Y
				if (sa.equalsIgnoreCase("Y")   ){
					session.setAttribute("call_disposition", "SA"); 
				}
			}
		}
		
		if (testCall) {
			LOGGER.info(new StringBuffer(logToken).append("*** LOOKHERE exitReasonForCleanupPlugin = " + ((String) session.getAttribute("exitReasonForCleanupPlugin"))));
			LOGGER.info(new StringBuffer(logToken).append("*** LOOKHERE call_disposition = " + ((String) session.getAttribute("call_disposition"))));
		}
		
		// ---------------------------------------------------------------
		// GSMART LOGGING 
		// ----------------------------------------------------------------
		String gsmartLogging =  (String) session.getAttribute("gsmartLogging");

		if ((gsmartLogging != null) && (gsmartLogging.equalsIgnoreCase("true"))) {	
			if (testCall)
				LOGGER.info(new StringBuffer(logToken).append("gsmartLogging is TRUE"));
			GsmartLoggerHelper gsmartLogger = new GsmartLoggerHelper();
						
			// SCS - June 01, 2011 fixed to get call_disposition (not callDisposition) 
			if ((String) session.getAttribute("call_disposition") != null)
				callDisposition = (String) session.getAttribute("call_disposition"); 
			
			// jan 19, 2012 SCS - OVERRIDE call_disposition if needed for GSMART LOGGING
			if ((String) session.getAttribute("override_final_call_disposition") != null) {
				String cd_override = (String) session.getAttribute("override_final_call_disposition");
				if (cd_override.length() > 0 && !(cd_override.equals("NO_OVERRIDE")))
					callDisposition = cd_override; 
				
				if (testCall) {
					LOGGER.info(new StringBuffer(logToken).append("*** override_final_call_disposition reset gsmart callDisposition to = " 
							+ callDisposition));
				}
			}
				
			// set xfer_var to "" in case session variable does not exist
			// xfer_var is only used if callDisposition is TF
			String xfer_var = "";
			if ((String) session.getAttribute("xfer_var") != null)
				xfer_var = (String) session.getAttribute("xfer_var"); 

			// Aug 11, 2011 - GSMART and application logs may use different application name
			// check if variable gsmartApplicationName is set, if not then use applicationName
			String appName = (String) session.getAttribute("gsmartApplicationName");
			if (appName == null || appName.trim().isEmpty()) {
				appName = (String) session.getAttribute("applicationName");
			}
			
			// SCS June 01, 2011 - sending in cur_prompt as menuLevel since menuLevel is missing last selection
			gsmartLogger.callEnd(arg0,
								sessionID, 
								appName, 
								((Timestamp) session.getAttribute("callStartTimestamp")),
								(String) session.getAttribute("DNIS"),
								(String) session.getAttribute("cur_prompt"),
								callDisposition,
								xfer_var
								);
		}
		else {
			if (testCall)
				LOGGER.info(new StringBuffer(logToken).append("Gsmart Logging is OFF because session var gsmartLogging is NOT true"));
		}
		
		// Check for BELK Application Name
		String appName=(String) session.getAttribute("applicationName");
		if(appName !=null && appName.equalsIgnoreCase("BELKS")){
			createTrnlogFile(session,logToken);
		}
		
		// GMC - Change related to ticket IN2228775 
		String appNameMacys=(String) session.getAttribute("applicationName");
		if(appNameMacys !=null && (appNameMacys.equalsIgnoreCase("MACYS") || appNameMacys.equalsIgnoreCase("MACYS_PWRESET") || appNameMacys.equalsIgnoreCase("MACYS_POSHARDWARE") || appNameMacys.equalsIgnoreCase("MACYS_PHONETROUBLE") || appNameMacys.equalsIgnoreCase("MACYS_PHONEHARDWARE") || appNameMacys.equalsIgnoreCase("MACYS_RFTROUBLE") || appNameMacys.equalsIgnoreCase("MACYS_RFHARDWARE") || appNameMacys.equalsIgnoreCase("MACYS_CRTRESET") || appNameMacys.equalsIgnoreCase("MACYS_PRINTSTART"))){
			createMacyslogFile(session,logToken);
		}
		
		// ---------------------------------------------------------------
		// APPLICATION SPECIFIC LOGGING 
		// ----------------------------------------------------------------
		String applicationLogData =  (String) session.getAttribute("applicationLogData");
		String applicationLogDataEvaluate =  (String) session.getAttribute("applicationLogDataEvaluate");
		if ((applicationLogData != null) && (applicationLogData.length()>0)) {	
			if (testCall)
				LOGGER.info(new StringBuffer(logToken).append("**** application Logging is required - session var applicationLogData exists"));
			ApplicationLoggerHelper appLogger = new ApplicationLoggerHelper();
			
			// jan 19, 2012 SCS - OVERRIDE call_disposition in session prior to calling app logging 
			if ((String) session.getAttribute("override_final_call_disposition") != null) {
				String cd_override = (String) session.getAttribute("override_final_call_disposition");
				if (cd_override.length() > 0 && !(cd_override.equals("NO_OVERRIDE")))
				    session.setAttribute("call_disposition", cd_override); 
				
				if (testCall) {
					LOGGER.info(new StringBuffer(logToken).append("*** override_final_call_disposition reset session call_disposition to = " 
							+ cd_override));
				}
			}
			
			appLogger.callEnd(arg0,
								session,
								sessionID, 
								((String) session.getAttribute("applicationName")), 
								((Timestamp) session.getAttribute("callStartTimestamp")),
								(String) applicationLogData,
								(String) applicationLogDataEvaluate
								);
			if (testCall)
				LOGGER.info(new StringBuffer(logToken).append("**** application Logging has completed"));
		}
		else {
			if (testCall)
				LOGGER.info(new StringBuffer(logToken).append("Application Logging is OFF because session var applicationLog does not exist"));
		}
	}
	
	   /**
	    * Creates the Transaction Log for Belk.  This file will be used by another process.
	    * @param session - HttpSession
	    * @param logToken - used for writing to the log file
	    */
	   public void createTrnlogFile(HttpSession session, String logToken){		   		  
		   Vector<String[]> replaceValues=new Vector<String[]>();
		   String[] replaceArrayValues=null;
		   
		   try{
			   boolean testCall = ((Boolean) session.getAttribute("testCall")).booleanValue();
			   long start=System.currentTimeMillis();
			   
			   setEndCallVars(session,logToken);
			   
			   String replacePropertiesFile=(String)session.getAttribute("replace_file_properties");
			   replaceValues=getReplaceValues(replacePropertiesFile, logToken);
			   
			   String trnLogTemplate=(String)session.getAttribute("trnlog_template");
			   String xmlData=getXMLContentsFromTemplate(trnLogTemplate, logToken);
			   
			   if(replaceValues != null && replaceValues.size() != 0){
				   Object attributeObj = null;
				   String attributeKey="";
				   String attributeValue="";
				   for(int i=0;i<replaceValues.size();i++){
					   replaceArrayValues=(String[])replaceValues.get(i);
					   if(replaceArrayValues != null && replaceArrayValues.length == 2){
						   attributeObj=replaceArrayValues[0];
						   if(attributeObj != null){
							   attributeKey=attributeObj.toString();
							   attributeObj=session.getAttribute(attributeKey);
							   if(attributeObj != null){
								   attributeValue=attributeObj.toString();
							   }else{
								   attributeValue="";
								   if(testCall)
									   LOGGER.debug(new StringBuffer(logToken).append("Unable to replace ").append(attributeKey));
								   
								   continue;
							   }
						   }else{
							   attributeValue="";
							   continue;
						   }
						   xmlData=xmlData.replace(replaceArrayValues[1], attributeValue);
					   }
				   }
				   // Now Write contents to Transaction Log
				   Object tempObj=null;
				   tempObj=session.getAttribute("trnlog_soap_XMLFile");
				   if(tempObj != null){
					   String trnLog=tempObj.toString();
					   LFileFunctions.writeToFile(logToken, trnLog, xmlData, false);					   
				   }else{
					   LOGGER.debug(new StringBuffer(logToken).append("trnlog_soap_XMLFile does not exist"));
				   }
				   
				   long finish=System.currentTimeMillis();
				   long elapsed=finish-start;
				   if(testCall)
					   LOGGER.info(new StringBuffer(logToken).append("Writing xml file took::" +elapsed));
			   }
		   }catch(Exception e){
			   LOGGER.error(new StringBuffer(logToken).append("Error creating Transaction Log. "+e.toString()),e);
		   }
	   }
	   
	   /**
	    * setEndCallVars - sets the end call variables in the xml callflow.
	    * @param session - HttpSession
	    * @param logToken - used for logging purposes
	    */
	   private void setEndCallVars(HttpSession session, String logToken){
		   ExpressionPluginImpl ep=null;
		   Object tempVar=null;
		   try{
			   ep=new ExpressionPluginImpl();
			   String stopDate=ep.DATE("S");
			   String stopTime=ep.TIME("N");
			   stopTime=stopTime.replace(":", "");
			   session.setAttribute("call_stoptime", stopTime);
			   session.setAttribute("call_stopdate", stopDate);
			   
			   String xfer_number="";
			   tempVar=session.getAttribute("xfer_number");
			   if(tempVar != null){
				   xfer_number=tempVar.toString();
			   }
			   if((xfer_number == null) || xfer_number.equals("XFER_NUMBER") || xfer_number.trim().equals("")){
				   session.setAttribute("trnlog_return_code", "1000");
				   session.setAttribute("trn_call_status", "Hang-up");
			   }
			   
			   String transfer_to="NA";
			   tempVar=session.getAttribute("transfer_to");
			   if(tempVar != null)
				   transfer_to=tempVar.toString();
			   
			   if(xfer_number != null && !(xfer_number.trim().equals(""))){
				   xfer_number=xfer_number.replace("&,*90", "");
				   xfer_number=xfer_number.replace("&,*9091", "");
				   xfer_number=xfer_number.replace("&,*90", "");
				   xfer_number=xfer_number.replace("*8,", "");
				   if(xfer_number.length() >= 2){
					   String firstTwoChars=xfer_number.substring(0,2);
					   if(firstTwoChars.equals("91") || firstTwoChars.equals("90")){
						   xfer_number=xfer_number.substring(2);
					   }
				   }
				   transfer_to=xfer_number.replace("#", "");
			   }
			   session.setAttribute("transfer_to", transfer_to);

			   String cur_prompt=(String)session.getAttribute("cur_prompt");
			   if(cur_prompt != null){
				   String menuLevel=ep.TRANSLATE(cur_prompt);
				   if(menuLevel.equalsIgnoreCase("MAIN_1")){
					   session.setAttribute("menu_option", "1");
				   }else if(menuLevel.equalsIgnoreCase("MAIN_4_1")){
					   session.setAttribute("menu_option", "41");
				   }else if(menuLevel.equalsIgnoreCase("MAIN_4_1_1")){
					   session.setAttribute("menu_option", "411");
				   }
			   }
		   }catch(Exception e){
			   LOGGER.error(new StringBuffer(logToken).append("Error in setEndCallVars"),e);
		   }
	   }
	   
	   /**
	    * Reads the contents of the XML Template that will be used to send the soap Request to WebService
	    * @param filename
	    * @return
	    */
	   public String getXMLContentsFromTemplate(String filename, String logToken){
		   StringBuffer xml_data=new StringBuffer();
		   FileInputStream fstream=null;
		   DataInputStream dstream=null;
		   BufferedReader bReader=null;
		   String strLine="";
		   String eol = System.getProperty("line.separator");
		   try{
			   fstream=new FileInputStream(filename);
			   dstream=new DataInputStream(fstream);
			   bReader=new BufferedReader(new InputStreamReader(dstream));
			   
			   while ((strLine = bReader.readLine()) != null)   {
				   // Print the content on the console
				   xml_data.append(strLine).append(eol);
			   }			   
		   }catch(Exception e){
			   LOGGER.error(new StringBuffer(logToken).append("Error in getXMLContentsFromTemplate:"+e.toString()),e);
		   }finally{
			   try{
				   if(fstream != null){
					   fstream.close();
				   }
				   if(dstream != null){
					   dstream.close();
				   }		
				   if(bReader != null){
					   bReader.close();
				   }
			   }catch(IOException io){
				   LOGGER.error(new StringBuffer(logToken).append("Error Closing Readers in getXMLContentsFromTemplate--"+io.toString()));
			   }
		   }
		   return xml_data.toString();
	   }
	   
	   /**
	    * This method reads a property file containing all of the session variables and place holders
	    * that are used to replace the values in the transaction log template with the values of the 
	    * session variables.
	    * @param filename - Path and file name of the properties file
	    * @param logToken
	    * @return
	    */
	   private java.util.Vector<String[]> getReplaceValues(String filename, String logToken){
		   Vector<String[]> data=new Vector<String[]>();
		   FileInputStream fstream=null;
		   DataInputStream dstream=null;
		   BufferedReader bReader=null;
		   String strLine="";
		   try{
			   fstream=new FileInputStream(filename);
			   dstream=new DataInputStream(fstream);
			   bReader=new BufferedReader(new InputStreamReader(dstream));
			   
			   while ((strLine = bReader.readLine()) != null)   {
				   // Print the content on the console
				   data.add((String[]) strLine.split("="));
			   }			   
		   }catch(Exception e){
			   LOGGER.error(new StringBuffer(logToken).append("Error in getReplaceValues:"+e.toString()),e);
		   }finally{
			   try{
				   if(fstream != null){
					   fstream.close();
				   }
				   if(dstream != null){
					   dstream.close();
				   }		
				   if(bReader != null){
					   bReader.close();
				   }
			   }catch(IOException io){
				   LOGGER.error(new StringBuffer(logToken).append("Error Closing Readers in getXMLContentsFromTemplate--"+io.toString()),io);
			   }
		   }
		   return data;
	   }
	   
	   /**
	    * Creates the Transaction Log for Macys.
	    * @param session - HttpSession
	    * @param logToken - used for writing to the log file
	    */
	   public void createMacyslogFile(HttpSession session, String logToken){		   		  
		   ExpressionHelperFunctions exp = new ExpressionHelperFunctions();
		   try{
			   boolean testCall = ((Boolean) session.getAttribute("testCall")).booleanValue();
			   long start=System.currentTimeMillis();
			   String output_file = (String)session.getAttribute("output_file");
			   String callflow_ini = (String)session.getAttribute("callflow_ini");
			   String automation_url = (String)session.getAttribute("automation_url");
			   String CallDate = (String)session.getAttribute("CallDate");
			   String CallTime = (String)session.getAttribute("CallTime");
			   String LineNumber = (String)session.getAttribute("LineNumber");
			   String Division = (String)session.getAttribute("Division");
			   String Store = (String)session.getAttribute("Store");
			   String VRUOption = (String)session.getAttribute("VRUOption");
			   String SelfHelp = (String)session.getAttribute("SelfHelp");
			   String SelfHelpStep = (String)session.getAttribute("SelfHelpStep");
			   String Device = (String)session.getAttribute("Device");
			   String CallerAction = (String)session.getAttribute("CallerAction");
			   String CompletionCode = (String)session.getAttribute("CompletionCode");
			   String CompletionMessage = (String)session.getAttribute("CompletionMessage");
			   String Attempts = (String)session.getAttribute("Attempts");
			   if(!output_file.equals("")){
				   if(!callflow_ini.equals("")){
					   exp.LINEOUT(output_file, callflow_ini);
				   }else{
					   exp.LINEOUT(output_file, "callflow_ini=&quot;MCY_STAT_OUT.ini&quot;");
				   }
				   
				   // GMC - Change related to ticket IN3065419 
				   // GMC - Checking for call_disposition = AT (Macys ticketing fails)
				   String sCallDisp = (String) session.getAttribute("call_disposition");
				   if(sCallDisp.equals("AT")){
					   exp.LINEOUT(output_file, "automation_url=&quot;http://129.39.17.119/automation/LogStatistics.asp?&quot;");
				   }else{
					   if(!automation_url.equals("")){
						   exp.LINEOUT(output_file, automation_url);
					   }else{
						   exp.LINEOUT(output_file, "automation_url=&quot;http://129.39.17.119/automation/LogStatistics.asp?&quot;");
					   }
				   }
				   
				   if(!CallDate.equals("")){
					   exp.LINEOUT(output_file, CallDate);
				   }else{
					   exp.LINEOUT(output_file, "CallDate=&quot;NA&quot;");
				   }
				   if(!CallTime.equals("")){
					   exp.LINEOUT(output_file, CallTime);
				   }else{
					   exp.LINEOUT(output_file, "CallTime=&quot;NA&quot;");
				   }
				   if(!LineNumber.equals("")){
					   exp.LINEOUT(output_file, LineNumber);
				   }else{
					   exp.LINEOUT(output_file, "LineNumber=&quot;NA&quot;");
				   }
				   if(!Division.equals("")){
					   exp.LINEOUT(output_file, Division);
				   }else{
					   exp.LINEOUT(output_file, "Division=&quot;NA&quot;");
				   }
				   if(!Store.equals("")){
					   exp.LINEOUT(output_file, Store);
				   }else{
					   exp.LINEOUT(output_file, "Store=&quot;0&quot;");
				   }
				   if(!VRUOption.equals("")){
					   exp.LINEOUT(output_file, VRUOption);
				   }else{
					   exp.LINEOUT(output_file, "VRUOption=&quot;NA&quot;");
				   }
				   if(!SelfHelp.equals("")){
					   exp.LINEOUT(output_file, SelfHelp);
				   }else{
					   exp.LINEOUT(output_file, "SelfHelp=&quot;N&quot;");
				   }
				   if(!SelfHelpStep.equals("")){
					   exp.LINEOUT(output_file, SelfHelpStep);
				   }else{
					   exp.LINEOUT(output_file, "SelfHelpStep=&quot;0&quot;");
				   }
				   if(!Device.equals("")){
					   exp.LINEOUT(output_file, Device);
				   }else{
					   exp.LINEOUT(output_file, "Device=&quot;0&quot;");
				   }
				   if(!CallerAction.equals("") || CallerAction.equals("-1")){
					   exp.LINEOUT(output_file, CallerAction);
				   }else{
					   exp.LINEOUT(output_file, "CallerAction=&quot;2&quot;");
				   }
				   if(!CompletionCode.equals("")){
					   exp.LINEOUT(output_file, CompletionCode);
				   }else{
					   exp.LINEOUT(output_file, "CompletionCode=&quot;0&quot;");
				   }
				   if(!CompletionMessage.equals("")){
					   exp.LINEOUT(output_file, CompletionMessage);
				   }else{
					   exp.LINEOUT(output_file, "CompletionMessage=&quot;NA&quot;");
				   }
				   if(!Attempts.equals("")){
					   exp.LINEOUT(output_file, Attempts);
				   }else{
					   exp.LINEOUT(output_file, "Attempts=&quot;0&quot;");
				   }
				   long finish=System.currentTimeMillis();
				   long elapsed=finish-start;
				   if(testCall)
					   LOGGER.info(new StringBuffer(logToken).append("Writing Macys Transaction file took:" +elapsed));
			   }else{
				   long finish=System.currentTimeMillis();
				   long elapsed=finish-start;
				   if(testCall)
					   LOGGER.info(new StringBuffer(logToken).append("Error creating Macys Transaction Log. Output File Variable Empty!"));
			   }
		   }catch(Exception e){
			   LOGGER.error(new StringBuffer(logToken).append("Error creating Macys Transaction Log. "+e.toString()),e);
		   }
	   }
}
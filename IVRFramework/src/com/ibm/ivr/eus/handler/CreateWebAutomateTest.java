package com.ibm.ivr.eus.handler;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Properties;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import com.ibm.ivr.eus.handler.CreateWebAutomate;
import com.ibm.ivr.eus.common.GetCurrentTimeStamp;

public class CreateWebAutomateTest extends HttpServlet implements Servlet{

	private static final long serialVersionUID = 1L;
	private static Logger LOGGER = Logger.getLogger(CreateWebAutomateTest.class);
	private GetCurrentTimeStamp gcts = null;
	
	public static void main(String[] args) {
		CreateWebAutomateTest cwa = new CreateWebAutomateTest();
		// cwa.processRequest();
		cwa.processRequestGET();
	}
	
	protected void processRequestGET(){
		HttpURLConnection connection = null;
		OutputStreamWriter wr = null;
		BufferedReader rd  = null;
		StringBuilder sb = null;
		String line = null;
		URL serverAddress = null;
	  
		try
		{
	    	// String automation_url = "http://129.39.17.119/automation/HardwareTicket.asp?Division=Y&Store=001&Component=MONITOR&Device=MON999&Floor=99&Extension=9999";
	    	String automation_url = "http://bldaut01.boulder.ibm.com:9080/EUSTTBelk/services/EUSTTBelk";
			serverAddress = new URL(automation_url);
			
			int iAutomation_timeout;
			iAutomation_timeout = 250;
			
			//set up out communications stuff
			connection = null;
	  
			//Set up the initial connection
			connection = (HttpURLConnection)serverAddress.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
    		connection.setConnectTimeout(iAutomation_timeout * 1000);    		
			connection.setReadTimeout(iAutomation_timeout * 1000);
			connection.connect();
	  
			//get the output stream writer and write the output to the server
			//not needed in this example
			//wr = new OutputStreamWriter(connection.getOutputStream());
			//wr.write("");
			//wr.flush();
			//read the result from the server
			rd  = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			sb = new StringBuilder();
			while ((line = rd.readLine()) != null)
			{
				sb.append(line + '\n');
			}
			System.out.println(sb.toString());
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
		catch (ProtocolException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			//close the connection, set all objects to null
			connection.disconnect();
			rd = null;
			sb = null;
			wr = null;
			connection = null;
		}
	}
	
	// Override processRequest
	protected void processRequest(){
		
    	// Instantiate the DateTimeNow
    	gcts = new GetCurrentTimeStamp();
    	
		String callid = "001";
		boolean testCall = false;

    	// Create the log Token for later use, use StringBuffer to reduce number of String objects
    	String logToken = new StringBuffer("[").append(callid).append("] ").toString();
    	
    	if (testCall){
    		LOGGER.info(new StringBuffer(logToken).append("Entering CreateWebAutomate Handler"));
    	}else{
        	// Start Timer - Create a starter time stamp and put it in the log or saved for later use
    		LOGGER.info(new StringBuffer(logToken).append("Entering CreateWebAutomate Handler at: " + gcts.getDateTimeNow()));
    	}
    	
    	try{
	    	// Hard Code values for session attributes
    		String problem_number = "";
    		String automation_data = "";
	    	String automation_func = "HARDWARETICKET";
	    	String automation_rc = "";
	    	String automation_status = "";
	    	String automation_timeout = "250";
	    	String automation_type = "GET";
	    	String automation_url = "http://129.39.17.119/automation/HardwareTicket.asp?";
	    	String division_code = "Y";
	    	String store_number = "0";
	    	String component_name = "MONITOR";
	    	String device_type = "MON";
	    	String register_number = "";
	    	String floor_number = "123";
	    	String contact_extension = "1234";
	    	String device = device_type + "||" + register_number;
	    	String parameters = "Division=" + URLEncoder.encode(division_code, "UTF-8") + "&Store=" + URLEncoder.encode(store_number, "UTF-8") + "&Component=" + URLEncoder.encode(component_name, "UTF-8") + "&Device=" + URLEncoder.encode(device, "UTF-8") + "&Floor=" + URLEncoder.encode(floor_number, "UTF-8") + "&Extension=" + URLEncoder.encode(contact_extension, "UTF-8");
	    	automation_url = automation_url + parameters;
	    	
	    	/*
	    	// automation_url = "http://129.39.17.119/automation/HardwareTicket.asp?Division=Y&Store=001&Component=MONITOR&Device=MON||1100&Floor=1&Extension=1100";
	    	automation_url = "http://129.39.17.119/automation/HardwareTicket.asp?Division=Y&Store=001&Component=MONITOR&Device=MON999&Floor=99&Extension=9999";
	
	    	// Execute the Post
	    	String result = executePost(automation_url, parameters,automation_type);
	    	*/

	    	automation_url = "http://129.39.17.119/automation/HardwareTicket.asp?Division=Y&Store=001&Component=MONITOR&Device=MON999&Floor=99&Extension=9999";
	    	
	    	// GMC - Change related to ticket IN3065419 
	    	int iAutomation_timeout;
	    	
	    	if (automation_timeout != null) {
				iAutomation_timeout = Integer.parseInt(automation_timeout);
			} else {
				iAutomation_timeout = 250;
			}	    	
	    	
	    	// Execute the Post
	    	// String result = executePost(automation_url, parameters, automation_type, logToken, testCall);
	    	String result = executePost(automation_url, parameters, automation_type, logToken, testCall, iAutomation_timeout);	    	
	    	
	    	// Check if result is good
	    	if(result == null){
	    		
	    		// GMC - Change related to ticket IN3065419 
	    		// session.setAttribute("return_data", "null");
	    		String sReturnData = "Could not communicate with URL";
	    		
	    		automation_data = "RC=97|Could not communicate with URL";
	            automation_rc = "97";
	            automation_status = "Could not communicate with URL";    		
	    	}else{
	    		String temp_data = result.toUpperCase();
	    		if(result.indexOf("|") != 0){
	    			if(result.contains("RC=0")){
	    				if(automation_func.equals("HARDWARETICKET")){
		    				automation_rc = "0";
		    				String[] strTemp = temp_data.split("=");
		    				problem_number = strTemp[2];
	    				}
	    			}else{
	    				automation_rc = "51";
	    				String[] strTemp = temp_data.split("\\|");
	    				automation_status = strTemp[1];
	    			}
	    		}else{
	    			automation_rc = "50";
	    			automation_status = temp_data.replaceAll("RC=' automation_rc ',", "");
	    		}
	    		
	    		if(automation_rc.equals("531")){
	                automation_status = "HardwareTicket (CreateTicket) SQL 2005 CONNECTION ERROR";
		            automation_data = "RC=531|HardwareTicket (CreateTicket) SQL 2005 CONNECTION ERROR";
	    		}
	    	}
	    	
	    	// Check if result is good
	    	/*
	    	if(result == null){
	    		automation_data = "RC=97|Could not communicate with URL";
	            automation_rc = "97";
	            automation_status = "Could not communicate with URL";    		
	    	}else{
	    		String temp_data = result.toUpperCase();
	    		if(result.indexOf("|") != 0){
	    			// Strip function in T-REXX which will strip temp_data and get the automation_rc and automation_status values	
	                // PARSE VAR temp_data "RC="automation_rc"|"automation_status .
	    			automation_status = temp_data.replaceAll("RC='automation_rc'|", "");
	    		}else{
	    			// Strip function in T-REXX which will strip temp_data and get the automation_rc and automation_status values	
	                // PARSE VAR temp_data "RC=" automation_rc "," automation_status
	    			automation_status = temp_data.replaceAll("RC=' automation_rc ',", "");
	    		}
	    		
	    		if(automation_func.equals("HARDWARETICKET")){
	    			String problem_number = temp_data.replaceAll("TICKETNUMBER=", "");
	    		}
	    		
	    		if(automation_rc.equals("531")){
	                automation_status = "HardwareTicket (CreateTicket) SQL 2005 CONNECTION ERROR";
		            automation_data = "RC=531|HardwareTicket (CreateTicket) SQL 2005 CONNECTION ERROR";
	    		}
	    	}
	    	*/
	    	
    	}catch(Exception ex){
    		System.out.println(ex.getMessage());
    	}
    	
    	if (testCall){
    		LOGGER.info(new StringBuffer(logToken).append("Exiting CreateWebAutomate Handler"));
    	}else{
	    	// Stop Timer - Create a stop time stamp and put it in the log or saved for later use
	    	LOGGER.info(new StringBuffer(logToken).append("Exiting CreateWebAutomate Handler at: " + gcts.getDateTimeNow()));
    	}    	
    	
    	return;
	}
	
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest();
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest();
    }
    
    // public String executePost(String targetURL, String urlParameters, String automation_type)
    
	// GMC - Change related to ticket IN3065419 
    // public String executePost(String targetURL, String urlParameters, String automation_type, String logToken, boolean testCall)  {
    public String executePost(String targetURL, String urlParameters, String automation_type, String logToken, boolean testCall, int iTimeOutSeconds)  
    {
    	URL url;
    	HttpURLConnection connection = null;
    	try {
    		//Create connection
    		url = new URL(targetURL);
    		connection = (HttpURLConnection)url.openConnection();
    		connection.setRequestMethod(automation_type);
    		connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
    		connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
    		connection.setRequestProperty("Content-Language", "en-US");
    		connection.setUseCaches (false);
    		connection.setDoInput(true);
    		connection.setDoOutput(true);
    		
    		// GMC - Change related to ticket IN3065419
    		connection.setConnectTimeout(iTimeOutSeconds * 1000);    		
    		
    		//Send request
    		DataOutputStream wr = new DataOutputStream (connection.getOutputStream ());
    		wr.writeBytes (urlParameters);
    		wr.flush ();
    		wr.close ();
    		
    		//Get Response
    		InputStream is = connection.getInputStream();
    		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
    		String line;
    		StringBuffer response = new StringBuffer();
    		
    		while((line = rd.readLine()) != null) {
    			response.append(line);
    			response.append('\r');
    		}
    		rd.close();
    		return response.toString();
    	}catch (Exception e){
    		e.printStackTrace();
    		return null;
    	}finally{
    		if(connection != null){
    			connection.disconnect();
    		}
    	}
    }
}
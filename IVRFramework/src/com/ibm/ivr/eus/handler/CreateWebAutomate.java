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
import com.ibm.ivr.eus.common.GetCurrentTimeStamp;

public class CreateWebAutomate extends HttpServlet implements Servlet{
	private static final long serialVersionUID = 1L;
	private static Logger LOGGER = Logger.getLogger(CreateWebAutomate.class);
	private GetCurrentTimeStamp gcts = null;
	
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

    	// Instantiate the DateTimeNow
    	gcts = new GetCurrentTimeStamp();
    	
    	// Get session from Servlet request
    	HttpSession session = request.getSession(false);
    	
		String callid = (String) session.getAttribute("callid");
		boolean testCall = ((Boolean) session.getAttribute("testCall")).booleanValue();

    	// Create the log Token for later use, use StringBuffer to reduce number of String objects
    	String logToken = new StringBuffer("[").append(callid).append("] ").toString();
    	
    	if (testCall){
    		LOGGER.info(new StringBuffer(logToken).append("Entering CreateWebAutomate Handler"));
    	}else{
        	// Start Timer - Create a starter time stamp and put it in the log or saved for later use
    		LOGGER.info(new StringBuffer(logToken).append("Entering CreateWebAutomate Handler at: " + gcts.getDateTimeNow()));
    	}
    	
    	try{
	    	// Get session attributes
	    	String problem_number = "";
	    	String automation_data = (String) session.getAttribute("automation_data");
	    	String automation_func = (String) session.getAttribute("automation_func");
	    	String automation_rc = (String) session.getAttribute("automation_rc");
	    	String automation_status = (String) session.getAttribute("automation_status");
	    	// String automation_type = "GET";
	    	String automation_url = (String) session.getAttribute("automation_url");
	    	// String parameters = "";
	
	    	// GMC - Change related to ticket IN3065419 
	    	String automation_timeout = (String) session.getAttribute("automation_timeout");
	    	int iAutomation_timeout = 0;
	    	
	    	if (automation_timeout != null) {
				iAutomation_timeout = Integer.parseInt(automation_timeout);
			} else {
				iAutomation_timeout = 250;
			}	    	
	    	
	    	session.setMaxInactiveInterval(-1);
	    	
	    	// GMC - Change related to ticket IN3065419 
	    	// Execute the Post
	    	// String result = executePost(automation_url, parameters, automation_type, logToken, testCall);
	    	// String result = executePost(automation_url, parameters, automation_type, logToken, testCall, iAutomation_timeout);
	    	String result = executePostGET(automation_url, logToken, testCall,iAutomation_timeout);
	    	
	    	// Check if result is good
	    	if(result == null){
	    		
	    		// GMC - Change related to ticket IN3065419 
	    		// session.setAttribute("return_data", "null");
	    		String sReturnData = "Could not communicate with URL";
	    		session.setAttribute("return_data", sReturnData);
	    		
	    		automation_data = "RC=97|Could not communicate with URL";
	            automation_rc = "97";
	            automation_status = "Could not communicate with URL";    		
	            session.setAttribute("automation_data", automation_data);
	            session.setAttribute("automation_rc", automation_rc);
	            session.setAttribute("automation_status", automation_status);
	    	}else{
		    	session.setAttribute("return_data", result);
	    		
	    		// GMC - 04/18/12 - Change related to ticket IN3607108
	    		// String temp_data = result.toUpperCase();
	    		String s1 = result.toUpperCase();
	    		String temp_data = s1.replaceAll("[\n\r]", "");
	    		
	    		if(result.indexOf("|") != 0){
	    			if(result.contains("RC=0")){
	    				if(automation_func.equals("HARDWARETICKET")){
		    				automation_rc = "0";
		    				String[] strTemp = temp_data.split("=");
		    				problem_number = strTemp[2];
		    	            session.setAttribute("automation_rc", automation_rc);
				            session.setAttribute("problem_number", problem_number);
				            
				            // GMC - 04/18/12 - Change related to ticket IN2152631
		    	            session.setAttribute("automation_status", temp_data);
	    				}
	    			}else{
	    				automation_rc = "51";
	    				String[] strTemp = temp_data.split("\\|");
	    				automation_status = strTemp[1];
	    	            session.setAttribute("automation_rc", automation_rc);
	    	            session.setAttribute("automation_status", automation_status);
	    			}
	    		}else{
	    			automation_rc = "50";
	    			automation_status = temp_data.replaceAll("RC=' automation_rc ',", "");
		            session.setAttribute("automation_rc", automation_rc);
		            session.setAttribute("automation_status", automation_status);
	    		}
	    		
	    		if(automation_rc.equals("531")){
	                automation_status = "HardwareTicket (CreateTicket) SQL 2005 CONNECTION ERROR";
		            automation_data = "RC=531|HardwareTicket (CreateTicket) SQL 2005 CONNECTION ERROR";
		            session.setAttribute("automation_data", automation_data);
		            session.setAttribute("automation_status", automation_status);
	    		}
	    	}
	    	if(testCall){
	    		LOGGER.debug(new StringBuffer(logToken).append("automation_rc="+automation_rc));
	    		LOGGER.debug(new StringBuffer(logToken).append("automation_status="+automation_status));
	    		LOGGER.debug(new StringBuffer(logToken).append("automation_data="+automation_data));
	    	}
    	}catch(Exception ex){
    		LOGGER.error(new StringBuffer(logToken).append("Exception caught"), ex);
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
        processRequest(request, response);
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
        processRequest(request, response);
    }
    
	// GMC - Change related to ticket IN3065419 
    // public String executePost(String targetURL, String urlParameters, String automation_type, String logToken, boolean testCall)  {
    /*
    public String executePost(String targetURL, String urlParameters, String automation_type, String logToken, boolean testCall, int iTimeOutSeconds)  {
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
    		connection.setReadTimeout(iTimeOutSeconds * 1000);
    		
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
    		if(testCall)
    			LOGGER.debug(new StringBuffer(logToken).append("ExecutePost Response:"+response.toString()));
    		
    		return response.toString();
    	}catch (Exception e){
    		LOGGER.error("Error in executePost:", e);
    		return null;
    	}finally{
    		if(connection != null){
    			connection.disconnect();
    		}
    	}
    }
    */
    
	// GMC - Change related to ticket IN3065419 
    public String executePostGET(String targetURL, String logToken, boolean testCall, int iTimeOutSeconds)  {
		HttpURLConnection connection = null;
		BufferedReader rd  = null;
		StringBuilder sb = null;
		String line = null;
		URL serverAddress = null;
		String sResult = "";
	  
		try
		{
			serverAddress = new URL(targetURL);

			//set up out communications stuff
			connection = null;
	  
			//Set up the initial connection
			connection = (HttpURLConnection)serverAddress.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
    		connection.setConnectTimeout(iTimeOutSeconds * 1000);    		
			connection.setReadTimeout(iTimeOutSeconds * 1000);
			connection.connect();
	  
			//read the result from the server
			rd  = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			sb = new StringBuilder();
			while ((line = rd.readLine()) != null)
			{
				sb.append(line + '\n');
			}
			// System.out.println(sb.toString());
			
			sResult = sb.toString();
			
			if(testCall)
    			LOGGER.debug(new StringBuffer(logToken).append("executePostGET Response: " + sResult));
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
			connection = null;
		}
		return sResult;
    }
}
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ibm.ivr.eus.handler;

import com.ibm.ivr.eus.data.XferProperties;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Properties;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 *
 * @author mansey
 */
public class XFERLOOKUP extends HttpServlet implements Servlet {

	private static final long serialVersionUID = 1L;
	private static Logger LOGGER = Logger.getLogger(XFERLOOKUP.class);
	private static HashMap<String, XferProperties> xferPropertiesMap = new HashMap<String, XferProperties>();
	
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    	
		// get session from Servlet request
		HttpSession session = request.getSession(false);

		String callid = (String) session.getAttribute("callid");
		boolean testCall = ((Boolean) session.getAttribute("testCall")).booleanValue();
		// get value of ewt_tones and ewt_roundup_seconds from session
		//Properties globalProp = (Properties) session.getServletContext().getAttribute("globalProp");

		//create the log Token for later use, use StringBuffer to reduce number of String objects
		String logToken = new StringBuffer("[").append(callid).append("] ").toString();
		
		// get path where property files are stored
		String propertyFileRoot = (String) session.getServletContext().getAttribute("propertyFilePath");

		if (testCall)
			LOGGER.info(new StringBuffer(logToken).append("Entering XFERLOOKUP Handler"));
		
        // get xfer file name
        String xferFileName = (String) session.getAttribute("xfer_file");
        String xferSearch = (String) session.getAttribute("xfer_search");
        String xferVar = (String) session.getAttribute("xfer_var");
        String xferPrefix = (String) session.getAttribute("xfer_prefix");
        
        if (testCall) {
			LOGGER.info(new StringBuffer(logToken).append("Retrieved from session: ")
					.append("xfer_file = ").append(xferFileName)
					.append("; xfer_search = ").append(xferSearch)
					.append("; xfer_var = ").append(xferVar)
					.append("; xfer_prefix = ").append(xferPrefix));
        }
        String xferNumber = null;
        
        if (checkNumber(xferVar)) {
        	xferNumber = xferVar;
        	if (testCall)
    			LOGGER.info(new StringBuffer(logToken).append("xfer_var is a number, no lookup required"));
        } else {
        	// lookup xferNumber in transfer mapping file
	        if (xferFileName != null) {
	        	URI fileURI = null;
	        	String xferFilePath = propertyFileRoot + xferFileName;
	        	try {
	        		fileURI = new URI(xferFilePath.replace('\\', '/'));
	        	} catch (Exception e) {
	        		LOGGER.error(new StringBuffer(logToken).append("Error creating URI for xfer file: ")
							.append(xferFilePath).append("; ").append(e.getMessage()));
	        	}
	        	File propertiesFile = new File(fileURI);
	        	XferProperties xferProperties = xferPropertiesMap.get(xferFileName);
	        	if (xferProperties != null) {
	        		if (testCall)
	        			LOGGER.info(new StringBuffer(logToken).append("found xferProperties in cache"));
	        		if (xferProperties.getTimestamp() != propertiesFile.lastModified()) {
	        			if (testCall)
		        			LOGGER.info(new StringBuffer(logToken).append("xferProperties file has changed since it was last cached, reloading it"));
	        			xferProperties.setTimestamp(propertiesFile.lastModified());
	        			try {
	        				xferProperties.getProperties().load(new FileReader(propertiesFile));
	        				
	        			} catch (Exception e) {
	        				// log exception
	        				if (testCall)
	        					LOGGER.error(new StringBuffer(logToken).append("Error processing properties file: ")
	        							.append(xferFileName).append("; ").append(e.getMessage()));
	        			}
	        		}
	        		
	        	} else {
	        		if (testCall)
	        			LOGGER.info(new StringBuffer(logToken).append("did not find xferProperties in cache, loading it and putting in cache:"+propertiesFile.getAbsolutePath()));
	        		xferProperties = new XferProperties();
	        		Properties properties = new Properties();
	        		xferProperties.setTimestamp(propertiesFile.lastModified());
	        		xferProperties.setProperties(properties);
	        		try {
	    				xferProperties.getProperties().load(new FileReader(propertiesFile));
	    				
	    			} catch (Exception e) {
	    				// log exception
	    				if (testCall)
        					LOGGER.error(new StringBuffer(logToken).append("Error processing properties file: ")
        							.append(xferFileName).append("; ").append(e.getMessage()));
	    			}
	    			xferPropertiesMap.put(xferFileName, xferProperties);
	        		
	        	}
	        	// create property name to lookup number
	        	if (xferProperties != null) {
	        		String xferKey = null;
	        		Properties prop = xferProperties.getProperties();
	        		xferKey = xferSearch + "." + xferVar;
	        		if (testCall)
	        			LOGGER.info(new StringBuffer(logToken).append("searching for xferKey = ").append(xferKey));
	        		xferNumber = prop.getProperty(xferKey);
	        		if (xferNumber == null) {
	        			xferKey = "DEFAULT" + "." + xferVar;
	        			if (testCall)
		        			LOGGER.info(new StringBuffer(logToken).append("searching for xferKey = ").append(xferKey));
	        			xferNumber = prop.getProperty(xferKey);
	        			if (xferNumber == null) {
	        				xferKey = xferSearch + "." + "DEFAULT";
	        				if (testCall)
	    	        			LOGGER.info(new StringBuffer(logToken).append("searching for xferKey = ").append(xferKey));
		        			xferNumber = prop.getProperty(xferKey);
		        			if (xferNumber == null) {
		        				xferKey = "DEFAULT.DEFAULT";
		        				if (testCall)
		    	        			LOGGER.info(new StringBuffer(logToken).append("searching for xferKey = ").append(xferKey));
			        			xferNumber = prop.getProperty(xferKey);
		        			}
	        			}
	        		}
	        		if (testCall)
	        			LOGGER.info(new StringBuffer(logToken).append("found transfer number = ").append(xferNumber));
	        	}
	        }
        }
        
        /* check to see if xferNumber is null.  if there is no transfer number then
         * no need to attach data
         * 
         */
        if (xferNumber != null) {
        // check to see if data needs to be attached
	        String sendData = (String) session.getAttribute("send_data");
	        if (sendData != null && !sendData.equals("") && !sendData.equals("SEND_DATA")) {
		        String sendKVP = (String) session.getAttribute("send_kvp");
		        if (testCall)
	    			LOGGER.info(new StringBuffer(logToken).append("CTI send_data = ").append(sendData)
	    					.append("; send_kvp = ").append(sendKVP));
		        // attach CTI data if any
		        HashMap<String, String> attachedData = (HashMap<String, String>) session
				.getAttribute("attachedData");
		        if (attachedData != null) {
		        	if (testCall)
	        			LOGGER.info(new StringBuffer(logToken).append("attaching data for CTI"));
		        	attachedData.put("UU_DATA", sendData);
		        	attachedData.put("screenpop_data", sendData);
		        	// parse sendKVP (expect format to be "!key1!value1!key2!value2!...)
		        	// the first character is the token separator
		        	if (sendKVP != null && sendKVP.length() > 3 && !sendKVP.equals("SEND_KVP")) {
		        		String kvpSep = sendKVP.substring(0, 1);
		        		String[] tokens = sendKVP.split(kvpSep);
		        		for (int i = 1; i < tokens.length; i += 2) {
		        			// if key is valid then put in attachedData
		        			if (tokens[i] != null && !tokens[i].equals("")) {
		        				attachedData.put(new String(tokens[i]), 
		        						(i+1 < tokens.length && tokens[i+1] != null) ? new String(tokens[i+1]) : "");
		        			}
		        		}
			        	if (testCall)
		        			LOGGER.info(new StringBuffer(logToken).append("session.attachedData="+attachedData.toString()));
		        	}
		        }
	        }
        }
        
        // strip "&," in xfer_prefix if present as it is not needed when doing a transfer in WVR AIX
        if (xferPrefix != null && xferPrefix.length() >= 2 && xferPrefix.substring(0,2).equals("&,")) {
        	xferPrefix = xferPrefix.substring(2);
        	if (testCall)
    			LOGGER.info(new StringBuffer(logToken).append("stripped &, from xferPrefix; value = ")
    					.append(xferPrefix));
        }

        // set transfer_agent_type based on xfer_type and/or xferNumber
        // set xfer_prefix based on xfer_type and/or xferNumber
        if (testCall)
			LOGGER.info(new StringBuffer(logToken).append("determining transferAgentType"));
        String transferAgentType = null;
        //check if number starts with *8 or xferPrefix starts with *8 - then it is an agent type call
        if (xferNumber != null && xferNumber.length() >= 3 && xferNumber.substring(0,2).equals("*8")) {
        	transferAgentType = new String("agent");	//external transfer
        	session.setAttribute("xfer_prefix", "");
        	session.setAttribute("lineAgentPrefix", "");
        	session.setAttribute("converseAgentPrefix", "");
        } else if (xferPrefix != null && xferPrefix.length() >= 2 && xferPrefix.substring(0,2).equals("*8")) {
        	// SCS June 20, 2011 - added prefix to number for transferconnect case 
        	session.setAttribute("xfer_number", xferPrefix+xferNumber);
        	transferAgentType = new String("agent");	//external transfer
        } else {
        	String dnisSource = (String)session.getAttribute("dnisSource");
        	// SCS 04/06/2011 -- added # to speed up transfer switch know end of data (Defect 170522 - CP004)
        	// SCS 10/17/2011 -- added logic to check for session variable flag to NOT append with #
        	String NO_PoundOnXfer = (String)session.getAttribute("NO_PoundOnXfer");
			if (xferNumber != null && (!((NO_PoundOnXfer != null) && NO_PoundOnXfer.length() > 0 ))) { xferNumber = xferNumber + "#"; }
        	if (dnisSource != null && dnisSource.equals("port")) {
        			transferAgentType = new String("lineagent");	// line transfer
        			if (xferPrefix != null)
        				session.setAttribute("lineAgentPrefix", xferPrefix);	// if app has set prefix then set it for this call
        	} else {
        		transferAgentType = new String("converseagent");	// converse transfer
        		if (xferPrefix != null)
        			session.setAttribute("converseAgentPrefix", xferPrefix);	// if app has set prefix then set it for this call
        	}
        }
        
        // set xferNumber to empty string
        if (xferNumber == null) {
        	xferNumber = "";
        }
        
        // set all return variables in the session
        session.setAttribute("transfer_agent_type", transferAgentType);
        session.setAttribute("xfer_number", xferNumber);
        session.setAttribute("call_status", "HUP");
        session.setAttribute("call_disposition", "TF");
        // commented out next line as this logic is implemented in plugin Cleanup.java 
        // session.setAttribute("status_avoided", "N");
        

			LOGGER.info(new StringBuffer(logToken).append(" - Values in session:"));
			LOGGER.info(new StringBuffer(logToken).append(" - xfer_number: ").append(session.getAttribute("xfer_number")));
			LOGGER.info(new StringBuffer(logToken).append(" - transfer_agent_type: ").append(session.getAttribute("transfer_agent_type")));
			LOGGER.info(new StringBuffer(logToken).append(" - call_status: ").append(session.getAttribute("call_status")));
			LOGGER.info(new StringBuffer(logToken).append(" - call_disposition: ").append(session.getAttribute("call_disposition")));
			LOGGER.info(new StringBuffer(logToken).append(" - status_avoided: ").append(session.getAttribute("status_avoided")));
			LOGGER.info(new StringBuffer(logToken).append(" - lineAgentPrefix: ").append(session.getAttribute("lineAgentPrefix")));
			LOGGER.info(new StringBuffer(logToken).append(" - converseAgentPrefix: ").append(session.getAttribute("converseAgentPrefix")));
			LOGGER.info(new StringBuffer(logToken).append("Exiting XFERLOOKUP handler"));
		
		return;
     }
     

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
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
    
    /** 
     * Checks whether the string is a number or starts with *8
     * @param str String to check
     * @return boolean true if string is a number or starts with *8, false otherwise
     */
    private boolean checkNumber(String str) {
    	boolean isNumber = false;
    	
    	if (str == null)
    		return false;
    	str = str.trim();
    	int length = str.length();
    	
    	if (length > 0) {
    		if (length >= 2 && str.substring(0,2).equals("*8")) {
    			isNumber = true;
    		} else {
    			isNumber = true;
	    		for (int i = 0; i < str.length(); i++) {
	    			if (!Character.isDigit(str.charAt(i))) {
	    				isNumber = false;
	    				break;
	    			}
	    		}
    		}
    	}
    	return isNumber;
    }

}

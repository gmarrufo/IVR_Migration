/*
 * Created on 13 Dec 2010
 *
 */
package com.ibm.ivr.eus.handler;

import java.io.IOException;
import java.util.Vector;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;


/**
 * @author McDonley
 *
 */

/************************************************************************
 *              - Create_return_LE8_CC_vector -                           *
 * Handler called by IBMHELP to create a vector of 1 to 8 country codes  *
 * to be used by ALPHAENT_PLAY_COUNTRY
 *  			             						*
 * Parameters needing to be defined in session include:                  *                                   *
 *   - return_vector
 *                                                           *
 *
 * Outputs:
 * 	- return_LE8_CC
 *  - return_LE8_CC_press
 *
 *
 ************************************************************************/

public class Create_return_LE8_CC_vector extends HttpServlet implements Servlet{
	

	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final String fileversion = " %1.1% ";

	private static Logger LOGGER = Logger.getLogger(Create_return_LE8_CC_vector.class);

	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
							throws ServletException, IOException {

	// get session from Servlet request, created if not existed yet
	HttpSession session = req.getSession(true);

	String callid = (String) session.getAttribute("callid");

	boolean testCall = ((Boolean) session.getAttribute("testCall")).booleanValue();

		//create the log Token for later use, use StringBuffer to reduce number
		// of String objects
	String logToken = new StringBuffer("[").append(callid).append("] ").toString();
		 
	 if (testCall)
	 	LOGGER.debug(new StringBuffer(logToken).append("Entering Create_return_LE8_CC_vector handler"));
	
		
	String rc = "-1";
	String employee_long_serial = null;
	String[] splitRow = null;
	Integer extract_index = 0;
	String row = null;
	String[] return_LE8_CC = null;
	String[] return_LE8_CC_press = null;
	
    try {
        // Get attributes from the request
 
    	Vector<StringBuffer> valueSet = (Vector<StringBuffer>) session.getAttribute("return_vector");
    	Integer return_count = valueSet.size();
    	return_LE8_CC = new String[return_count];
    	return_LE8_CC_press = new String[return_count];
    	
    	for (extract_index = 0; extract_index < return_count;extract_index++) {
        	row = valueSet.elementAt(extract_index).toString();
   			    //653006;;653006-897;; NA1NA1;;5618622140;;NA;;amcdonle@us.ibm.com
     			//key;;long_serial;;service_location;;phone_tmp;;phone_ext_tmp;;email_tmp
        	splitRow = row.split(";;");
        	employee_long_serial = splitRow[1];
        	int iDelim = employee_long_serial.lastIndexOf("-");
        	return_LE8_CC[extract_index]= "CC"+employee_long_serial.substring(iDelim+1);
        	return_LE8_CC_press[extract_index] = ""+(extract_index+1);
            // Debug Logging		
        	if (testCall){
        		LOGGER.debug(new StringBuffer(logToken).append("added to return_LE8_CC:"+employee_long_serial));	
        			
        		}

    		
    		
    	}
			
    	rc = "0";
			
			
    	
    	

       	
 
 
    
	} catch (Exception e) {
		
		e.printStackTrace();
	}	
	
	
		// Set session variables needed for reply
		session.setAttribute("return_LE8_CC", return_LE8_CC);
		session.setAttribute("return_LE8_CC_press", return_LE8_CC_press);
		session.setAttribute("rc", rc);	

	 
	// Log session variables updated
	
	 if (testCall){
	 	LOGGER.debug(new StringBuffer(logToken).append("return_LE8_CC put in session"));
	 	LOGGER.debug(new StringBuffer(logToken).append("return_LE8_CC_press put in session"));
	 	LOGGER.debug(new StringBuffer(logToken).append("returning rc:"+rc));
	 }	
   return;
  }
}

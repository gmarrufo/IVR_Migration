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
 *              - Create_return_LE8_short_serials_vector -                           *
 * Handler called by IBMHELP to create a vector of 1 to 8 short_serial  *
 * to be used by ALPHAENT_PLAY_SERIAL
 *  			             						*
 * Parameters needing to be defined in session include:                  *                                   *
 *   - return_vector
 *                                                           *
 *
 * Outputs:
 * 	- return_LE8_short_serials
 *  - return_LE8_short_serial_press
 *
 *
 ************************************************************************/

public class Create_return_LE8_short_serials_vector extends HttpServlet implements Servlet{
	

	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final String fileversion = " %1.2% ";

	private static Logger LOGGER = Logger.getLogger(Create_return_LE8_short_serials_vector.class);

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
	 	LOGGER.debug(new StringBuffer(logToken).append("Entering Create_return_LE8_short_serials_vector handler"));
	
		
	String rc = "-1";
	String employee_long_serial = null;
	String[] splitRow = null;
	Integer extract_index = 0;
	String row = null;
	String[] return_LE8_short_serials = null;
	String[] return_LE8_short_serial_press = null;
	String[] temp_array = null;
	Integer unique_count = 0;
	
    try {
        // Get attributes from the request
 
    	Vector<StringBuffer> valueSet = (Vector<StringBuffer>) session.getAttribute("return_vector");
    	Integer return_count = valueSet.size();
    	//return_LE8_short_serials = new String[return_count];
    	//return_LE8_short_serial_press = new String[return_count];
    	temp_array = new String[return_count];

    	for (extract_index = 0; extract_index < return_count;extract_index++) {
        	row = valueSet.elementAt(extract_index).toString();
        	splitRow = row.split(";;");
        	employee_long_serial = splitRow[1];
        	int iDelim = employee_long_serial.lastIndexOf("-");
        	String serial = employee_long_serial.substring(0,iDelim);
        	boolean exists = false;
        	if (testCall){
        		LOGGER.debug(new StringBuffer(logToken).append("checking serial:"+serial));	
        		LOGGER.debug(new StringBuffer(logToken).append("unique_count:"+unique_count));
        	}
        	for (int i = 0; i < unique_count; i++) {
        		if (temp_array[i].equals(serial)) {
        			exists = true;
        			if (testCall){
                		LOGGER.debug(new StringBuffer(logToken).append("serial exists:"+serial));	
                	}
        		}
        	}
        	if (!exists) {
        		if (testCall){
            		LOGGER.debug(new StringBuffer(logToken).append("adding serial:"+serial));	
            	}
        		temp_array[unique_count]= serial;
        		unique_count++;
        	}
    	}
    	
    	return_LE8_short_serials = new String[unique_count];
    	return_LE8_short_serial_press = new String[unique_count];
    	
    	for (int i = 0;i < unique_count; i++) {
    		return_LE8_short_serials[i] = temp_array[i];
    		return_LE8_short_serial_press[i] = Integer.toString(i+1);
    		if (testCall){
        		LOGGER.debug(new StringBuffer(logToken).append("added to return_LE8_short_serial:"+return_LE8_short_serials[i]));	
        	}
    	}
    	
/*  	
    	for (extract_index = 0; extract_index < return_count;extract_index++) {
        	row = valueSet.elementAt(extract_index).toString();
   			    //653006;;653006-897;; NA1NA1;;5618622140;;NA;;amcdonle@us.ibm.com
     			//key;;long_serial;;service_location;;phone_tmp;;phone_ext_tmp;;email_tmp
        	splitRow = row.split(";;");
        	employee_long_serial = splitRow[1];
        	int iDelim = employee_long_serial.lastIndexOf("-");
        	return_LE8_short_serials[extract_index]= employee_long_serial.substring(0,iDelim);
        	return_LE8_short_serial_press[extract_index] = ""+(extract_index+1);
            // Debug Logging		
        	if (testCall){
        		LOGGER.debug(new StringBuffer(logToken).append("added to return_LE8_short_serial:"+return_LE8_short_serials[extract_index]));	
        			
        		}

    		
    		
    	}
*/
			
    	rc = "0";
    
	} catch (Exception e) {
		
		LOGGER.error(new StringBuffer(logToken).append("Exception:").append(e.getMessage()), e);
	}	
	
	
		// Set session variables needed for reply
		session.setAttribute("return_LE8_short_serials", return_LE8_short_serials);
		session.setAttribute("return_LE8_short_serial_press", return_LE8_short_serial_press);
		session.setAttribute("unique_serial_count", unique_count);
		session.setAttribute("rc", rc);	

	 
	// Log session variables updated
	
	 if (testCall){
	 	LOGGER.debug(new StringBuffer(logToken).append("return_LE8_short_serials put in session"));
	 	LOGGER.debug(new StringBuffer(logToken).append("return_LE8_short_serial_press put in session"));
	 	LOGGER.debug(new StringBuffer(logToken).append("unique_serial_count put in session"));
	 	LOGGER.debug(new StringBuffer(logToken).append("returning rc:"+rc));
	 }	
   return;
  }
}

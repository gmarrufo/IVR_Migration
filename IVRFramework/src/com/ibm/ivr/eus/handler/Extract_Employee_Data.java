/*
 * Created on 30 Nov 2010
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
 *                           - Extract Employee Data -                           *
 * Handler called by IBMHELP sub-menus to split a DB return row          *
 *  			             						*
 * Parameters needing to be defined in session include:                  *                                   *
 *   - return_vector
 *   - extract_index                                                        *
 *
 * Outputs:
 * 	- employee_long_serial
 *  - employee_search
 *  - employee_short_serial
 *  - employee_country
 *  - employee_location
 *  - employee_phone
 *  - employee_phone_ext
 *  - employee_email
 *  - send_data
 *
 *
 ************************************************************************/

public class Extract_Employee_Data extends HttpServlet implements Servlet{
	

	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final String fileversion = " %1.2% ";

	private static Logger LOGGER = Logger.getLogger(Extract_Employee_Data.class);

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
	 	LOGGER.debug(new StringBuffer(logToken).append("Entering Extract_Employee_Data handler"));
	
		
	String rc = "-1";
	String employee_long_serial = null;
	String employee_search = null;
	String employee_short_serial = null;
	String employee_country = null;
	String employee_location = null;
	String employee_phone = null;
	String employee_phone_ext = null;
	String employee_email = null;
	String send_data = null;
	String[] splitRow = null;
	Integer extract_index = 0;
	String strExtract_index = null;
	
    try {
        // Get attributes from the request
    	strExtract_index = (String) session.getAttribute("extract_index");
    	extract_index = Integer.parseInt(strExtract_index);
    	Vector<StringBuffer> valueSet = (Vector<StringBuffer>) session.getAttribute("return_vector");
    	String row = valueSet.elementAt(extract_index).toString();
    	
        // Debug Logging		
    	if (testCall){
    		LOGGER.debug(new StringBuffer(logToken).append("extract_index = "+extract_index));	
    		LOGGER.debug(new StringBuffer(logToken).append("row = "+row));	

     			//653006;;653006-897;; NA1NA1;;5618622140;;NA;;amcdonle@us.ibm.com
    			
    		}
			//contact_id;;service_location;;phone_tmp;;phone_ext_tmp;;email_tmp
    	send_data = row.substring(row.indexOf(";;")+2);
    	splitRow = row.split(";;");
    	employee_long_serial = splitRow[1];
    	employee_search = splitRow[0];
    	int iDelim = employee_long_serial.lastIndexOf("-");
    	employee_short_serial = employee_long_serial.substring(0,iDelim);
    	
    	// SCS April 13, 2011 - replace two lines below values where reversed (per Pradeep)
    	// employee_country = splitRow[2];
    	// employee_location= employee_long_serial.substring(iDelim+1);
       	employee_country = employee_long_serial.substring(iDelim+1);
        employee_location= splitRow[2];		
        
    	employee_phone = splitRow[3];
    	employee_phone_ext = splitRow[4];
    	employee_email = splitRow[5];

    	rc = "0";
			
			
    	employee_country = employee_long_serial.substring(iDelim+1);
    employee_location= splitRow[2];		
    	
    	

       	
 
 
    
	} catch (Exception e) {
		
		e.printStackTrace();
	}	
	
	
		// Set session variables needed for reply
		session.setAttribute("employee_long_serial", employee_long_serial);
		session.setAttribute("employee_search", employee_search);
		session.setAttribute("employee_short_serial", employee_short_serial);
		session.setAttribute("employee_country", employee_country);
		session.setAttribute("employee_location",employee_location);
		session.setAttribute("employee_phone", employee_phone);
		session.setAttribute("employee_phone_ext", employee_phone_ext);
		session.setAttribute("employee_email", employee_email);
		session.setAttribute("send_data",send_data);
		session.setAttribute("rc", rc);	

	 
	// Log session variables updated
	
	 if (testCall){
	 	LOGGER.debug(new StringBuffer(logToken).append("employee_long_serial:"+employee_long_serial));
	 	LOGGER.debug(new StringBuffer(logToken).append("employee_search:"+employee_search));
	 	LOGGER.debug(new StringBuffer(logToken).append("employee_short_serial:"+employee_short_serial));
	 	LOGGER.debug(new StringBuffer(logToken).append("employee_country:"+employee_country));
	 	LOGGER.debug(new StringBuffer(logToken).append("employee_location:"+employee_location));
	 	LOGGER.debug(new StringBuffer(logToken).append("employee_phone:"+employee_phone));
	 	LOGGER.debug(new StringBuffer(logToken).append("employee_phone_ext:"+employee_phone_ext));
	 	LOGGER.debug(new StringBuffer(logToken).append("employee_email:"+employee_email));
	 	LOGGER.debug(new StringBuffer(logToken).append("send_data:"+send_data));	 	
	 	LOGGER.debug(new StringBuffer(logToken).append("returning rc:"+rc));
	 }	
   return;
  }
}

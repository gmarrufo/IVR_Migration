/*
 * Created on  Dec 14 2010
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
 *                           - Prune_By_Serial -                           *
 * Handler called by IBMHELP sub-menus to remove rows from return_vector  *
 * that do not match short serial number 
 * (perhaps leaving multiple records that differ only by country code)
 * 			             						*
 * Parameters needing to be defined in session include:                  *                                   *
 *   - return_vector
 *   - selected_index                                                       *
 *
 * Outputs:
 *   - return_vector (pruned of rows that do not match short serial of selected index)
 *   - return_count (after pruned)
 * 
 ************************************************************************/

public class Prune_By_Serial extends HttpServlet implements Servlet{
	

	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final String fileversion = " %1.1% ";

	private static Logger LOGGER = Logger.getLogger(Prune_By_Serial.class);

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
	 	LOGGER.debug(new StringBuffer(logToken).append("Entering Prune_By_Serial handler"));
	
		
	String rc = "-1";
	String employee_long_serial = null;
	String employee_short_serial = null;
	String search_long_serial = null;
	String search_short_serial = null;
	String[] splitRow = null;
	Integer return_count = 0;
	Integer row_index = 0;
	String row = null;
	Integer selected_index = null;
	String strSelected_index = null;
   	Vector<StringBuffer> valueSet = null;
   	String[] valueSet1 = null;
   	int iDelim = -1;
   	
    try {
    	if(session.getAttribute("selected_index") == null)
    		session.setAttribute("selected_index","0"); 
        // Get attributes from the request
    	strSelected_index = (String) session.getAttribute("selected_index");
    	selected_index = Integer.parseInt(strSelected_index); 
    	valueSet = (Vector<StringBuffer>) session.getAttribute("return_vector");
    	// added 4/7/2011 - get selection from list of unique serial numbers
    	valueSet1 = (String[]) session.getAttribute("return_LE8_short_serials");

    	return_count = valueSet.size();
        // Debug Logging		
    	if (testCall){
    		LOGGER.debug(new StringBuffer(logToken).append("selected_index = "+selected_index));	
    		LOGGER.debug(new StringBuffer(logToken).append("vector rows incoming = "+return_count));	

    			
    		}
    	//Typical row format
		//653006;;653006-897;; NA1NA1;;5618622140;;NA;;amcdonle@us.ibm.com
		//search;;contact_id;;service_location;;phone_tmp;;phone_ext_tmp;;email_tmp
    	
    	// prune any rows from return_vector which do not match the serial number in the selected_index
    	// added 4/7/2011 - select from list of unique serial numbers
    	search_short_serial = valueSet1[selected_index];
    	/**** commented 4/7/2011
    	row = valueSet.elementAt(selected_index).toString();
    	splitRow = row.split(";;");
    	search_long_serial = splitRow[1];
     	iDelim = search_long_serial.lastIndexOf("-");
    	search_short_serial = search_long_serial.substring(0,iDelim);
    	****/
    	for (row_index=0;row_index < return_count;row_index++) {
        	row = valueSet.elementAt(row_index).toString();
        	splitRow = row.split(";;");
        	employee_long_serial = splitRow[1];
        	iDelim = employee_long_serial.lastIndexOf("-");
        	employee_short_serial = employee_long_serial.substring(0,iDelim);
        	if (employee_short_serial.compareTo(search_short_serial) != 0)  {
            	if (testCall){
            		LOGGER.debug(new StringBuffer(logToken).append("pruning:"+employee_long_serial));	
            	}
        		valueSet.removeElementAt(row_index);
        		return_count=valueSet.size();
        		row_index--;
        	} else {
        		if (testCall) {
            		LOGGER.debug(new StringBuffer(logToken).append("keeping:"+employee_long_serial));	

        		}
        	}
    	}
    	rc = "0";
 
    
	} catch (Exception e) {
		
		e.printStackTrace();
	}	
	
	
		// Set session variables needed for reply
		session.setAttribute("return_vector", valueSet);
		session.setAttribute("return_count", return_count);	
		session.setAttribute("rc",rc);

	 
	// Log session variables updated
	
	 if (testCall){
	 	LOGGER.debug(new StringBuffer(logToken).append("vector rows out:"+return_count));
	 }	
   return;
  }
}

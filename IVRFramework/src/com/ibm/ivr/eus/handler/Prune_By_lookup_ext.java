/*
 * Created on   2010
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
 *                           - Prune_By_lookup_ext -                           *
 * Handler called by IBMHELP sub-menus to remove rows from return_vector  *
 * that do not match lookup_ext
 * 			             						*
 * Parameters needing to be defined in session include:                  *                                   *
 *   - return_vector
 *   - lookup_ext                                                       *
 *
 * Outputs:
 *   - return_vector (pruned of rows that do not match phone ext)
 *   - return_count (after pruned)
 * 
 ************************************************************************/

public class Prune_By_lookup_ext extends HttpServlet implements Servlet{
	

	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final String fileversion = " %1.1% ";

	private static Logger LOGGER = Logger.getLogger(Prune_By_lookup_ext.class);

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
	 	LOGGER.debug(new StringBuffer(logToken).append("Entering Prune_By_lookup_ext handler"));
	
		
	String rc = "-1";
	String employee_ext = null;
	String[] splitRow = null;
	Integer return_count = 0;
	Integer row_index = 0;
	String row = null;
	String lookup_ext = null;
   	Vector<StringBuffer> valueSet = null;
   	
    try {
        // Get attributes from the request
    	lookup_ext = (String) session.getAttribute("lookup_ext");
    	valueSet = (Vector<StringBuffer>) session.getAttribute("return_vector");

    	return_count = valueSet.size();
        // Debug Logging		
    	if (testCall){
    		LOGGER.debug(new StringBuffer(logToken).append("lookup_ext = "+lookup_ext));	
    		LOGGER.debug(new StringBuffer(logToken).append("vector rows in = "+return_count));	

    			
    		}
    	//Typical row format
		//653006;;653006-897;; NA1NA1;;5618622140;;NA;;amcdonle@us.ibm.com
		//search;;contact_id;;service_location;;phone_tmp;;phone_ext_tmp;;email_tmp
    	
    	// prune any rows from return_vector which do not match the lookup_ext
    	for (row_index=0;row_index < return_count;row_index++) {
        	row = valueSet.elementAt(row_index).toString();
        	splitRow = row.split(";;");
        	employee_ext = splitRow[4];
        	if (employee_ext.compareTo(lookup_ext) != 0)  {
            	if (testCall){
            		LOGGER.debug(new StringBuffer(logToken).append("pruning row:"+row));	
            	}
        		valueSet.removeElementAt(row_index);
        		return_count=valueSet.size();
        		row_index--;
        	}
    	}
    	
 
   
			
			
    	
    	

       	
 
 
    
	} catch (Exception e) {
		
		e.printStackTrace();
	}	
	
	
		// Set session variables needed for reply
		session.setAttribute("return_vector", valueSet);
		session.setAttribute("return_count", return_count);	

	 
	// Log session variables updated
	
	 if (testCall){
	 	LOGGER.debug(new StringBuffer(logToken).append("vector rows out:"+return_count));
	 }	
   return;
  }
}

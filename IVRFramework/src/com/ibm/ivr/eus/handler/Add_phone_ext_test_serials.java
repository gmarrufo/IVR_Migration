/*
 * Created on 30 Nov 2010
 *
 */
package com.ibm.ivr.eus.handler;

import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.ibm.ivr.eus.dao.EUSSETDAO;
import com.ibm.ivr.eus.dao.EUSSETDTO;


/**
 * @author McDonley
 *
 */

/************************************************************************
 *                           - Add_phone_ext_test_serials -                           
 * Handler called by IBMHELP to add test serials to return_vector    
 *  			             						*
 * 
 *   
 * Outputs:
 *  - return_count
 *  - return_vector
 *
 *
 ************************************************************************/

public class Add_phone_ext_test_serials extends HttpServlet implements Servlet{
	

	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final String fileversion = " %1.1% ";

	private static Logger LOGGER = Logger.getLogger(Add_phone_ext_test_serials.class);

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
	 	LOGGER.debug(new StringBuffer(logToken).append("Entering Add_phone_ext_test_serials"));
	
		
 
	String row = null;
	Integer return_count = 0;
	String rc = "-1";
	Integer orig_return_count = 0;
	Vector<StringBuffer> valueSet = (Vector<StringBuffer>) session.getAttribute("return_vector");


	
    try {
    		return_count = valueSet.size();
    	    // Debug Logging		
    		if (testCall){
    			LOGGER.debug(new StringBuffer(logToken).append("input row_count = "+return_count));	
    		}
    		// need more than max_serial (8) to test phone_extension
    		orig_return_count = return_count;
    		valueSet.add(new StringBuffer("//******;;TESTAAA-897;; NA1NA1;;5555555555;;000;;testaaa@us.ibm.com"));
    		valueSet.add(new StringBuffer("//******;;TESTAAB-897;; NA1NA1;;5555555555;;001;;testaab@us.ibm.com"));
    		valueSet.add(new StringBuffer("//******;;TESTAAC-897;; NA1NA1;;5555555555;;002;;testaac@us.ibm.com"));
    		valueSet.add(new StringBuffer("//******;;TESTAAD-897;; NA1NA1;;5555555555;;003;;testaad@us.ibm.com"));
    		valueSet.add(new StringBuffer("//******;;TESTAAE-897;; NA1NA1;;5555555555;;004;;testaae@us.ibm.com"));
    		valueSet.add(new StringBuffer("//******;;TESTAAF-897;; NA1NA1;;5555555555;;005;;testaaf@us.ibm.com"));
    		valueSet.add(new StringBuffer("//******;;TESTAAG-897;; NA1NA1;;5555555555;;006;;testaag@us.ibm.com"));
    		valueSet.add(new StringBuffer("//******;;TESTAAH-897;; NA1NA1;;5555555555;;007;;testaah@us.ibm.com"));
    		valueSet.add(new StringBuffer("//******;;TESTAAI-897;; NA1NA1;;5555555555;;008;;testaai@us.ibm.com"));
    		return_count = valueSet.size();
    		for (int i=orig_return_count;i<return_count;i++) {
        		row = valueSet.get(i).toString();
    			if (LOGGER.isDebugEnabled()) LOGGER.debug("added row:"+row);
    			//653006;;653006-897;; NA1NA1;;5618622140;;NA;;amcdonle@us.ibm.com
    			
    		}
			rc = "0";
			
			
			
    	
    	

       	
 
 
    
	} catch (Exception e) {
		
		e.printStackTrace();
	}	
	
	
		// Set session variables needed for reply
		session.setAttribute("return_vector", valueSet);
		session.setAttribute("return_count", return_count);
		session.setAttribute("rc", rc);	

	 
	// Set session variables needed for reply
	
	 if (testCall){
	 	LOGGER.debug(new StringBuffer(logToken).append("return_count:"+return_count));
	 	LOGGER.debug(new StringBuffer(logToken).append("returning rc:"+rc));
	 }	
   return;
  }
}

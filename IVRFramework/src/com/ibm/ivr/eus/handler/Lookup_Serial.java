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
import com.ibm.ivr.eus.data.CommonValues;
import com.ibm.ivr.eus.data.DbQueryTimeout;


/**
 * @author McDonley
 *
 */

/************************************************************************
 *                           - Lookup_Serial -                           
 * Handler called by IBMHELP sub-menus to lookup an employee serial     
 *  			             						*
 * Properties needed from global.properties file:
 * 	 - eusDataSource
 * 
 * Parameters needing to be defined in session include:                                                     
 *   - temp_serial  - six digit serial to be searched                                                      
 *   - globalProp   - set by framework
 *   
 * Outputs:
 * 	- db_data
 *  - return_count
 *  - return_vector
 *
 *
 ************************************************************************/

public class Lookup_Serial extends HttpServlet implements Servlet{
	

	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final String fileversion = " %1.2% ";

	private static Logger LOGGER = Logger.getLogger(Lookup_Serial.class);
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		doGet(req, resp);
	}

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
	 	LOGGER.info(new StringBuffer(logToken).append("Entering Lookup_Serial handler"));
	
		
    // Get attributes from the request	
	String temp_serial = (String) session.getAttribute("temp_serial");
	String active_sca_ent_db = (String) session.getAttribute("active_sca_ent_db");
	String row = null;
	Integer return_count = 0;
	String rc = "-1";

	
	String jndiName = null;
	
	EUSSETDTO returndto;   
	Vector<StringBuffer> valueSet = new Vector<StringBuffer>();
	
    // Debug Logging		
	if (testCall){
		LOGGER.info(new StringBuffer(logToken).append("temp_serial = ").append(temp_serial));	
		LOGGER.info(new StringBuffer(logToken).append("active_sca_ent_db = ").append(active_sca_ent_db));	
	}
    try {
    	Properties globalProp = null;

    	globalProp = (Properties) session.getServletContext().getAttribute("globalProp");

    	jndiName = globalProp.getProperty(CommonValues.EUS_DATA_SOURCE_PROPERTY);
    	
    	/* create data access object interface to db */
    	int queryTimeout = DbQueryTimeout.getDbQueryTimout(globalProp);
    	EUSSETDAO eussetdao = new EUSSETDAO(jndiName, callid, testCall, queryTimeout);
	
        /* lookup finds row */
    	returndto = eussetdao.geteusSET(active_sca_ent_db, temp_serial);
   
       	if (returndto != null) {
    		valueSet = returndto.getVALUESET();	
    		return_count = valueSet.size();
    		for (int i=0;i<return_count;i++) {
        		row = valueSet.get(i).toString();
    			if (testCall) LOGGER.info(new StringBuffer(logToken).append("DB search returning row: ")
    					.append(row));
    			//653006;;653006-897;; NA1NA1;;5618622140;;NA;;amcdonle@us.ibm.com
    			
    		}
			//contact_id;;service_location;;phone_tmp;;phone_ext_tmp;;email_tmp
			rc = "0";
		
    	}

    
	} catch (Exception e) {
		
		e.printStackTrace();
	}	
	
	
		// Set session variables needed for reply
		session.setAttribute("return_vector", valueSet);
		session.setAttribute("return_count", return_count);
		session.setAttribute("rc", rc);	

	 
	// Set session variables needed for reply
	
	 if (testCall){
	 	LOGGER.info(new StringBuffer(logToken).append("return_count: ").append(return_count));
	 	LOGGER.info(new StringBuffer(logToken).append("returning rc: ").append(rc));
	 }	
   return;
  }
}

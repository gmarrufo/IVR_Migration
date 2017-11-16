package com.ibm.ivr.eus.handler;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;



/**
 * @author Sibyl Sullivan
 * 
 * This handler written for EUS, set the xfer_connect session variable base on lookup based on DNIS 
 * in ACE_transferConnect.properties.
 */

public class Set_xfer_connect extends HttpServlet implements Servlet {

	private static Logger LOGGER = Logger.getLogger(Set_xfer_connect.class);

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// get session from Servlet request, created if not existed yet
		HttpSession session = req.getSession(true);
		
		String callid = (String) session.getAttribute("callid");

		boolean testCall = ((Boolean) session.getAttribute("testCall")).booleanValue();

		//create the log Token for later use, use StringBuffer to reduce number
		//of String objects
		String logToken = new StringBuffer("[").append(callid).append("] ").toString();

		if (testCall)
			LOGGER.debug(new StringBuffer(logToken).append("Set_xfer_connect:  Entering xferConnect handler"));
		
		Properties callProp = (Properties) session.getAttribute("callProp");

		//get the session variable data
		String DNIS = (String)session.getAttribute("DNIS");

		// Set xfer_connect to the default of N.
		String xfer_connect = "N";
				
		// Check if the directory exists.
		String lookupDNISKey = DNIS + ".xfer_connect";
		if( callProp.getProperty( lookupDNISKey ) != null )
		{
//			session.setAttribute("dirExists", "true");
			xfer_connect = callProp.getProperty( lookupDNISKey );

			// save the values in the session.
			session.setAttribute("xfer_connect", xfer_connect);
			// need to set this to handle hard coded value 
            if ( xfer_connect.equalsIgnoreCase("Y")) {
            	session.setAttribute("xfer_prefix", "*8,");
            }

		}
		else {
			LOGGER.warn(new StringBuffer(logToken).append("Set_xfer_connect: was set to default of 'N' no entry in was found for DNIS = " + DNIS + 
					     " in the ACE call properties file "));
		}
			
		// Print out the values.
		
			LOGGER.debug(new StringBuffer(logToken).append("LOOKHERE session variable xfer_connect: " + session.getAttribute("xfer_connect") + 
					" xfer_prefix: " + session.getAttribute("xfer_prefix")));
	

		return;
	}
}
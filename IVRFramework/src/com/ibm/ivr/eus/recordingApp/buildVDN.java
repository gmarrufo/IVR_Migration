package com.ibm.ivr.eus.recordingApp;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * @author Val Peco
 * 
 * This handler written for EUS, saves the temporary VDN list for saving.
 */
public class buildVDN extends HttpServlet implements Servlet {

	private static Logger LOGGER = Logger.getLogger(buildVDN.class);

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
			LOGGER.debug(new StringBuffer(logToken).append("buildVDN: Entering buildVDN handler"));
		
		//get the session variable data.
		String TempVDN = (String) session.getAttribute("TempVDN");
		String TempVDNList[] = (String [])session.getAttribute("TempVDNList");;
		Integer TempVDNIndex = (Integer) session.getAttribute("TempVDNIndex");;
		Integer TempVDNCount = (Integer) session.getAttribute("TempVDNCount");;

		if (testCall)
		{
			LOGGER.debug(new StringBuffer(logToken).append("buildVDN: TempVDN: " + TempVDN));
			for( int i=0; TempVDNList[i] != ""; i++ )
				LOGGER.debug(new StringBuffer(logToken).append("buildVDN: TempVDNList: " + TempVDNList[i]));
			LOGGER.debug(new StringBuffer(logToken).append("buildVDN: TempVDNCount: " + TempVDNCount));
			LOGGER.debug(new StringBuffer(logToken).append("buildVDN: TempVDNIndex: " + TempVDNIndex));
		}
		
		// Check if the VDN list is ALL.
		if( TempVDN.equalsIgnoreCase("ALL") )
		{
			TempVDNList[0] = TempVDN;
			TempVDNCount++;
		}

		// Remove the last VDN.
		else if( TempVDN.equalsIgnoreCase("REMOVE") )
		{
			TempVDNList[TempVDNIndex-1] = "";
			TempVDNCount = TempVDNCount - 1;
			TempVDNIndex--;
			if( TempVDNIndex == 0 )
				TempVDNIndex = 1;
		}

		// Keep a list of all the VDNs.
		else
		{
			TempVDNList[TempVDNIndex-1] = TempVDN;
			TempVDNCount = TempVDNCount + 1;
			TempVDNIndex++;
		}

		// Set the session data.
		session.setAttribute("TempVDNList", TempVDNList);
		session.setAttribute("TempVDNCount", TempVDNCount);
		session.setAttribute("TempVDNIndex", TempVDNIndex);

		if (testCall)
		{
			for( int i=0; TempVDNList[i] != ""; i++ )
				LOGGER.debug(new StringBuffer(logToken).append("buildVDN: TempVDNList: " + TempVDNList[i]));
			LOGGER.debug(new StringBuffer(logToken).append("buildVDN: TempVDNCount: " + TempVDNCount));
			LOGGER.debug(new StringBuffer(logToken).append("buildVDN: TempVDNIndex: " + TempVDNIndex));
		}
		
		return;
	}
}

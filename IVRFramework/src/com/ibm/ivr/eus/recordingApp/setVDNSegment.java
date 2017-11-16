package com.ibm.ivr.eus.recordingApp;

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

import com.ibm.ivr.eus.dao.STATUSMSGDAO;
import com.ibm.ivr.eus.data.CommonValues;
import com.ibm.ivr.eus.data.DbQueryTimeout;

/**
 * @author Val Peco
 * 
 * This handler written for EUS, sets the next VDN message to play.
 */
public class setVDNSegment extends HttpServlet implements Servlet {

	private static Logger LOGGER = Logger.getLogger(setVDNSegment.class);

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String fileName = "";
		String currentVDN ="";

		String startDateTime = null;
		String startDate = null;
		String startTime = null;
		String endDateTime = null;
		String endDate = null;
		String endTime = null;
	    String dbTimes[] = {"",""};
	    String jndiName = null;
	    String status = "S";
	    boolean exists = false;

		// get session from Servlet request, created if not existed yet
		HttpSession session = req.getSession(true);

		String callid = (String) session.getAttribute("callid");

		boolean testCall = ((Boolean) session.getAttribute("testCall")).booleanValue();

		//create the log Token for later use, use StringBuffer to reduce number
		//of String objects
		String logToken = new StringBuffer("[").append(callid).append("] ").toString();

		if (testCall)
			LOGGER.info(new StringBuffer(logToken).append("setVDNSegment: Entering setVDNSegment handler"));
		
		Properties prop = (Properties) session.getServletContext().getAttribute("globalProp");
		jndiName = prop.getProperty(CommonValues.EUS_DATA_SOURCE_PROPERTY);
		
		//get the session variable data.
		String VDN = (String) session.getAttribute("VDN");
		String segmentID = (String)session.getAttribute("segmentID");
		String fileList[] = (String[])session.getAttribute("FileList");
		Integer VDNIndex = (Integer)session.getAttribute("VDNIndex");
		Integer VDNCount = (Integer)session.getAttribute("VDNCount");
		String VDNList[] = (String[]) session.getAttribute("VDNList");;
		String dirPath = (String) session.getAttribute("dirPath");;
		String dirName = (String)session.getAttribute("dirName");
		String language = (String)session.getAttribute("dirLanguage");

		if (testCall)
		{
			LOGGER.info(new StringBuffer(logToken).append("setVDNSegment: VDN: ").append(VDN));
			LOGGER.info(new StringBuffer(logToken).append("setVDNSegment: segmentID: ").append(segmentID));
			for( int i=0; fileList[i] != ""; i++ )
				LOGGER.info(new StringBuffer(logToken).append("setVDNSegment: fileList: ").append(fileList[i]));
			
			LOGGER.info(new StringBuffer(logToken).append("setVDNSegment: VDNIndex: ").append(VDNIndex));
			for( int i=0; VDNList[i] != null; i++ )
				LOGGER.info(new StringBuffer(logToken).append("setVDNSegment: VDNList: ").append(VDNList[i]));
			LOGGER.info(new StringBuffer(logToken).append("setVDNSegment: dirPath: ").append(dirPath));
		}
		
		// Play all the messages so loop thru the file list.
		if( VDN.equalsIgnoreCase("ALL") )
		{
			if( VDNIndex > VDNCount)
			{
				session.setAttribute("VDNIndex", 1);
				return;
			}
			
			// set the VDN.
			else
			{
				fileName = fileList[VDNIndex-1];

				// find the VDN from the filename
				currentVDN = VDNList[VDNIndex-1];
				VDN = currentVDN;

				session.setAttribute("CurrentVDN", currentVDN);
				session.setAttribute("VDNIndex", VDNIndex + 1);
				session.setAttribute("FileName", fileName);

			}
			if (testCall) {
				LOGGER.info(new StringBuffer(logToken).append("setVDNSegment: fileName: ").append(fileName));
				LOGGER.info(new StringBuffer(logToken).append("setVDNSegment: VDNIndex: ").append(VDNIndex));
				LOGGER.info(new StringBuffer(logToken).append("setVDNSegment: CurrentVDN: ").append(currentVDN));
			}

		}
		
		// Set the filename to the entered VDN
		else
		{
			// check if the VDN is in the list
			for( int i = 0; VDNList[i] != null; i++ )
			{
				if(( VDNList[i].compareTo(VDN) == 0 ) || ( VDNList[i].compareTo(VDN + "_NEW") == 0 ))
				{
					fileName = fileList[i];
					VDN = VDNList[i];
					exists = true;
					break;
				}
			}

			if( exists )
				session.setAttribute("FileExist", "true");
			else
				session.setAttribute("FileExist", "false");

			session.setAttribute("CurrentVDN", VDN);
			session.setAttribute("FileName", fileName);

			if (testCall) {
				LOGGER.info(new StringBuffer(logToken).append("setVDNSegment: fileName: ").append(fileName));
				LOGGER.info(new StringBuffer(logToken).append("setVDNSegment: VDNIndex: ").append(VDNIndex));
				LOGGER.info(new StringBuffer(logToken).append("setVDNSegment: CurrentVDN: ").append(VDN));
			}
		}

		// get the associated data from the database
		if( exists )
		{
			try {
		    	/* create data access object interface to db */
				int queryTimeout = DbQueryTimeout.getDbQueryTimout(prop);
		    	STATUSMSGDAO eusstatusmsg = new STATUSMSGDAO(jndiName, callid, testCall, queryTimeout);
		    	
		    	/* get specified status message */
		    	dbTimes = eusstatusmsg.getStatusMsgTimes(dirName,language,segmentID,VDN);
			}
			
			catch (Exception e) {
				e.printStackTrace();
				status = "E";
			}	

			// parse the start time
	    	if( dbTimes[0] == null )
	    	{
				startDateTime = "NOW";
	    	}
	    	
	    	else if( dbTimes[0].compareTo("NOTSET") == 0 )
	    	{
	    		if (testCall)
	    			LOGGER.info(new StringBuffer(logToken).append("setVDNSegment: getStatusMsg failed."));
	    		status = "E";
	    	}

	    		    	
	    	// parse the start time.
	    	else
			{
				startDateTime = dbTimes[0];
				startDate = startDateTime.substring(0, 8);
				startTime = startDateTime.substring(8);
				if (testCall)
					LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: startDateTime: ").append(startDateTime));
			}
					

		    	// parse the end time.
	    	if( dbTimes[1] == null )
	    	{
				endDateTime = "NEVER";
	    	}

	    	else if( dbTimes[1].compareTo("NOTSET") == 0 )
	    	{
	    		if (testCall)
	    			LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: getStatusMsg failed."));
	    		status = "E";
	    	}
	    	
	    	else
			{
	    		endDateTime = dbTimes[1];
				endDate = endDateTime.substring(0, 8);
				endTime = endDateTime.substring(8);
				if (testCall)
					LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: endDateTime: ").append(endDateTime));
			}
			
	    	// set the session data.
			session.setAttribute("StartDateTime", startDateTime);
			session.setAttribute("StartDate", startDate);
			session.setAttribute("StartTime", startTime);
			session.setAttribute("EndDateTime", endDateTime);
			session.setAttribute("EndDate", endDate);
			session.setAttribute("EndTime", endTime);

		}
		
		// Set the session variables for the return data.
		session.setAttribute("Status", status);
		return;
	}
}

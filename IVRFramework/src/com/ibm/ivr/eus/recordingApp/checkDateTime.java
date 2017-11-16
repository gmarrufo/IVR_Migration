package com.ibm.ivr.eus.recordingApp;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
 * This handler written for EUS, checks if a valid start time or end time was entered.
 */
public class checkDateTime extends HttpServlet implements Servlet {

	private static Logger LOGGER = Logger.getLogger(checkDateTime.class);
	public static final String DATE_FORMAT_NOW = "MMddyyyyHHmm";
	public static final String DATE_FORMAT_PASSED = "ddMMHHmm";

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// get session from Servlet request, created if not existed yet
		HttpSession session = req.getSession(true);
		
		String formatDateTime = "";
		String date = "";
		String time = "";
		String status = "S";
		String TempVDNList[] = {"","","","","","","","","",""};
		
		String callid = (String) session.getAttribute("callid");

		boolean testCall = ((Boolean) session.getAttribute("testCall")).booleanValue();

		//create the log Token for later use, use StringBuffer to reduce number
		//of String objects
		String logToken = new StringBuffer("[").append(callid).append("] ").toString();

		if (testCall)
			LOGGER.debug(new StringBuffer(logToken).append("checkDateTime: Entering checkDateTime handler"));
		
		//get the session variable data
		String dateTime = (String)session.getAttribute("DateTime");
	    Calendar cal = Calendar.getInstance();

	    // this is the start time
	    String indicator = (String)session.getAttribute("StartDateTime");
	    if( indicator.equalsIgnoreCase("NOTSET"))
	    {
	    	// Get the current time.
	    	if( dateTime.equalsIgnoreCase("NOW") )
	    	{
	    		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
	    		formatDateTime = sdf.format(cal.getTime());
	    		date = formatDateTime.substring(0, 8);
	    		time = formatDateTime.substring( 8 );
	    	}

	    	// Check the date and time passed. The format is DDMMHHMM.
	    	else
	    	{
	    		try
	    		{
	    			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_PASSED);
	    			sdf.setLenient(false);
	    			sdf.parse(dateTime);
	    			int year=cal.get(Calendar.YEAR);

	    			String day = dateTime.substring(0, 2);
	    			String month = dateTime.substring(2, 4);
	    			date = month + day + year;
	    			time = dateTime.substring(4);
	    			dateTime = date + time;
	    		}
	    		catch (ParseException e) {
	    			status = "E";
	    		}
	    		catch (IllegalArgumentException e) {
	    			status = "E";
	    		}
	    	}

	    	// the start date was successfully validated
	    	if( status == "S" )
	    	{
	    		session.setAttribute("StartDateTime", dateTime);
	    		session.setAttribute("StartDate", date);
	    		session.setAttribute("StartTime", time);
	    	}
	    }

	    
	    // This is the end time.
	    else
	    {
	    	// stop time was specified.
	    	if( !dateTime.equalsIgnoreCase("NEVER") )
	    	{
	    		try
	    		{
	    			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_PASSED);
	    			sdf.setLenient(false);
	    			sdf.parse(dateTime);
	    			int year=cal.get(Calendar.YEAR);

	    			String day = dateTime.substring(0, 2);
	    			String month = dateTime.substring(2, 4);
	    			
	    			// if the end month is less then the start month, add 1 to the year.
	    			String startDate = (String)session.getAttribute("StartDate");
	    			if( Integer.parseInt(month) < Integer.parseInt(startDate.substring(0, 2)))
	    			{
//	    				if( startDate.substring(0, 2) != "12")
	    					year = year + 1;
	    			}
	    			date = month + day + year;
	    			time = dateTime.substring(4);
	    			dateTime = date + time;
	    		}
	    		catch (ParseException e) {
	    			LOGGER.debug(new StringBuffer(logToken).append("checkDateTime: Invalid parse end time."));
	    			status = "E";
	    		}
	    		catch (IllegalArgumentException e) {
	    			LOGGER.debug(new StringBuffer(logToken).append("checkDateTime: Invalid argument end time."));
	    			status = "E";
	    		}

	    	}

	    	// end time successfully validated.
	    	if( status == "S" )
	    	{
	    		session.setAttribute("EndDateTime", dateTime);
	    		session.setAttribute("EndDate", date);
	    		session.setAttribute("EndTime", time);
	    		session.setAttribute("TempVDN", "");
	    		session.setAttribute("TempVDNCount", 0);
	    		session.setAttribute("TempVDNList", TempVDNList);
	    		session.setAttribute("TempVDNIndex", 1);
	    	}
	    }
	    
	    // Print out the values.
		if (testCall)
		{
			LOGGER.debug(new StringBuffer(logToken).append("checkDateTime: StartDateTime: " + session.getAttribute("StartDateTime")));
			LOGGER.debug(new StringBuffer(logToken).append("checkDateTime: EndDateTime: " + session.getAttribute("EndDateTime")));
			LOGGER.debug(new StringBuffer(logToken).append("checkDateTime: date: " + date));
			LOGGER.debug(new StringBuffer(logToken).append("checkDateTime: time: " + time));
			LOGGER.debug(new StringBuffer(logToken).append("checkDateTime: StartDate: " + session.getAttribute("StartDate")));
			LOGGER.debug(new StringBuffer(logToken).append("checkDateTime: StartTime: " + session.getAttribute("StartTime")));
			LOGGER.debug(new StringBuffer(logToken).append("checkDateTime: EndDate: " + session.getAttribute("EndDate")));
			LOGGER.debug(new StringBuffer(logToken).append("checkDateTime: EndTime: " + session.getAttribute("EndTime")));
		}

		session.setAttribute("Status", status);
		return;
	}
}

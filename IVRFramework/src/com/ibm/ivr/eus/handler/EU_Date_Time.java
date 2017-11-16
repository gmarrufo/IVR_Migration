package com.ibm.ivr.eus.handler;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

// Handler for Voice Trust interface

public class EU_Date_Time extends HttpServlet implements Servlet {
	
	private static final long serialVersionUID = 1L;
	private static Logger LOGGER = Logger.getLogger(EU_Date_Time.class);

	public EU_Date_Time() {
		// TODO Auto-generated constructor stub
		super();
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}   	

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		// get session from Servlet request
		HttpSession session = req.getSession(false);

		String callid = (String) session.getAttribute("callid");
		boolean testCall = ((Boolean) session.getAttribute("testCall")).booleanValue();

		//create the log Token for later use, use StringBuffer to reduce number
		// of String objects
		String logToken = new StringBuffer("[").append(callid).append("] ").toString();

		if (testCall)
			LOGGER.info(new StringBuffer(logToken).append("Entering EU_Date_Time Handler"));

		// use Europe/London as timezone to account for Day Light Savings time in UK
		// using GMT will result in incorrect date when DST is active in UK
		TimeZone ukTZ = TimeZone.getTimeZone("Europe/London");
		Calendar ukCal = Calendar.getInstance(ukTZ);

		Date localDate = new Date(ukCal.getTimeInMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		sdf.setTimeZone(ukTZ);

		int day = ukCal.get(Calendar.DAY_OF_WEEK);
		// Monday = 1
		day--;
		// convert Sunday to 0
		if (day == 0)
			day = 7;

		StringBuffer emeaTimeBuf = new StringBuffer().append(day).append(sdf.format(localDate));
		String emeaTime = emeaTimeBuf.toString();

		if (testCall) {
			LOGGER.info(new StringBuffer(logToken).append(" emeaTime = ").append(emeaTime));
		}

		session.setAttribute("emea_datetime", emeaTime);

		if (testCall) {
			LOGGER.info(new StringBuffer(logToken).append(" - Values in session:"));
			LOGGER.info(new StringBuffer(logToken).append(" - emea_datetime: ").append(session.getAttribute("emea_datetime")));
			LOGGER.info(new StringBuffer(logToken).append("Exiting EU_Date_Time handler"));
		}

		return;
	}

}

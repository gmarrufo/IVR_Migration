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

public class EWT extends HttpServlet implements Servlet {
	
	private static final long serialVersionUID = 1L;
	private static Logger LOGGER = Logger.getLogger(EWT.class);

	public EWT() {
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
		// get value of ewt_tones and ewt_roundup_seconds from session
		String ewtTonesStr = (String) session.getAttribute("ewt_tones");
		String ewtRoundupSecondsStr = (String) session.getAttribute("ewt_roundup_seconds");


		//create the log Token for later use, use StringBuffer to reduce number of String objects
		String logToken = new StringBuffer("[").append(callid).append("] ").toString();

		if (testCall)
			LOGGER.info(new StringBuffer(logToken).append("Entering EWT Handler"));
		if (testCall)
			LOGGER.info(new StringBuffer(logToken).append("ewt_tones = ").append(ewtTonesStr));

		int ewtHours = 0;
		int ewtMinutes = 0;
		int ewtSeconds = 0;
		int ewtRoundupSeconds = 0;
		
		if (ewtRoundupSecondsStr != null) {
			ewtRoundupSeconds = Integer.parseInt(ewtRoundupSecondsStr);
		}

		if (ewtTonesStr != null) {

			int ewtTones = Integer.parseInt(ewtTonesStr);
			ewtMinutes = ewtTones / 60;
			ewtSeconds = ewtTones % 60;
			
			if (ewtRoundupSeconds > 0) {
				int countInt = 60 / ewtRoundupSeconds;
				if ((60 % ewtRoundupSeconds) != 0)
					countInt = countInt + 1;
				for (int i = 1; i <= countInt; i++) {
					if ((ewtSeconds > ((i - 1) * ewtRoundupSeconds)) && (ewtSeconds < (i * ewtRoundupSeconds))) {
						ewtSeconds = ((i)*(ewtRoundupSeconds));
						if (ewtSeconds >= 60) {
							ewtSeconds = 0;
							ewtMinutes = ewtMinutes + 1;
						}
						break;
					}
				}

			}

			ewtHours = ewtMinutes / 60;
			if(ewtHours > 0)
				ewtMinutes = ewtMinutes % 60;
		}

		if (testCall) {
			LOGGER.info(new StringBuffer(logToken).append(" ewtHours = ").append(ewtHours)
					.append(";").append(" ewtMinutes = ").append(ewtMinutes)
					.append(";").append(" ewtSeconds = ").append(ewtSeconds));
		}

		session.setAttribute("ewt_hours", new Integer(ewtHours));
		session.setAttribute("ewt_minutes", new Integer(ewtMinutes));
		session.setAttribute("ewt_seconds", new Integer(ewtSeconds));

		if (testCall) {
			LOGGER.info(new StringBuffer(logToken).append(" - Values in session:"));
			LOGGER.info(new StringBuffer(logToken).append(" - ewt_hours: ").append(session.getAttribute("ewt_hours")));
			LOGGER.info(new StringBuffer(logToken).append(" - ewt_minutes: ").append(session.getAttribute("ewt_minutes")));
			LOGGER.info(new StringBuffer(logToken).append(" - ewt_seconds: ").append(session.getAttribute("ewt_seconds")));
			LOGGER.info(new StringBuffer(logToken).append("Exiting EWT handler"));
		}

		return;
	}

}

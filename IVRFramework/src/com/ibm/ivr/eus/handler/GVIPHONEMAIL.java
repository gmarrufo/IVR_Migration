/*
 * Created on 4 april 2011
 *
 */
package com.ibm.ivr.eus.handler;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.ibm.ivr.eus.websv.Socket_Connection;


/**
 * @author mansey
 *
 */

/************************************************************************
 *                           - GVIPHONEMAIL -                           *
 * Handler called by IBMHELP sub-menus to process automation            *
 * using Socket connection 			             						*
 * Parameters needed to be defined in session include:                  *
 *   - gvi_server_name                                                  *
 *   - gvi_server_port  												*
 *   - send_data                                                        *
 *   - phone                                                            *
 *   - gvi_socket_timeout                                               *
 *                                                                      *
 *   Parameters returned in session:                                    *
 *   - gvi_result                                                       *
 *   - call_disposition (if not null)                                   *
 *   																	*
 ************************************************************************/

public class GVIPHONEMAIL extends HttpServlet implements Servlet{
	

	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static final String fileversion = " %1.1% ";

	private static Logger LOGGER = Logger.getLogger(GVIPHONEMAIL.class);

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
			LOGGER.info(new StringBuffer(logToken).append("Entering GVIPHONEMAIL handler"));


		// Get attributes from the session	
		String gvi_server_name = (String) session.getAttribute("gvi_server_name");
		String gvi_server_port = (String) session.getAttribute("gvi_server_port");
		String send_data = (String) session.getAttribute("send_data");
		String phone = (String) session.getAttribute("phone");
		String gvi_socket_timeout = (String) session.getAttribute("gvi_socket_timeout");
		String strRequest = null;
		String strResponse = null;
		String gviphonemail_result = null;
		String call_disposition = null;
		Socket_Connection sc = null;

		int iServer_port;
		int iSocket_timeout;

		// Debug Logging		
		if (testCall){
			LOGGER.info(new StringBuffer(logToken).append("gvi_server_name = ").append(gvi_server_name));	
			LOGGER.info(new StringBuffer(logToken).append("gvi_server_port = ").append(gvi_server_port));	
			LOGGER.info(new StringBuffer(logToken).append("send_data = ").append(send_data));	
			LOGGER.info(new StringBuffer(logToken).append("phone = ").append(phone));
			LOGGER.info(new StringBuffer(logToken).append("gvi_socket_timeout = ").append(gvi_socket_timeout));
		}

		gviphonemail_result = "FAILED";

		if (gvi_server_port != null) {
			iServer_port = Integer.parseInt(gvi_server_port);
		}
		else {
			iServer_port = 7556;
		}

		if (gvi_socket_timeout != null) {
			iSocket_timeout = Integer.parseInt(gvi_socket_timeout);
		}
		else {
			iSocket_timeout = 30;
		}
		
		if (gvi_server_name != null && send_data != null && send_data.length() != 0 && phone != null && phone.length() != 0) {
			strRequest = send_data + " " + phone + "\r\n";
			if (testCall)
				LOGGER.info(new StringBuffer(logToken).append("sending PHONEMAIL password reset request to server; strRequest = ").append(strRequest));
	
			sc = new Socket_Connection();
			strResponse = sc.vru_automate(gvi_server_name, iServer_port, strRequest, iSocket_timeout, testCall, callid);
	
			if (testCall)
				LOGGER.info(new StringBuffer(logToken).append("received strResponse = ").append(strResponse));
	
			if (strResponse != null && strResponse.length() >= 2) {
				if (strResponse.contains("OK")) {
					gviphonemail_result = "OK";
					call_disposition = "AT";
				}
			} else {
				gviphonemail_result = "FAILED";
			}
		} else {
			gviphonemail_result = "FAILED";
			LOGGER.error(new StringBuffer(logToken).append("PHONEMAIL password reset command not sent to server as NULL/EMPTY values for required variables"));
		}

		// Set session variables needed for reply
		session.setAttribute("gviphonemail_result", gviphonemail_result);
		if (call_disposition != null) {
			session.setAttribute("call_disposition", call_disposition);
		}

		if (testCall){
			LOGGER.info(new StringBuffer(logToken).append("returning gviphonemail_result: ").append(gviphonemail_result));
			if (call_disposition != null)
				LOGGER.info(new StringBuffer(logToken).append("returning call_disposition: ").append(call_disposition));
			LOGGER.info(new StringBuffer(logToken).append("Leaving GVIPHONEMAIL handler"));
		}	
		return;
	}
}

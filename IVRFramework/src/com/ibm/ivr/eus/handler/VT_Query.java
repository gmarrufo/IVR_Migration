package com.ibm.ivr.eus.handler;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.ibm.ivr.eus.handler.VT_Request_Data;
import com.ibm.ivr.eus.handler.VT_Response_Data;
import com.ibm.ivr.eus.websv.VT_Server_Query;

// Handler for Voice Trust interface

public class VT_Query extends HttpServlet implements Servlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger LOGGER = Logger.getLogger(VT_Query.class);

	public VT_Query() {
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
			LOGGER.info(new StringBuffer(logToken).append("Entering VT_Query Handler"));

		Properties globalProp = null;
		String url = null;
		String cTimeout = null;
		String sTimeout = null;
		String vtPrimaryURL = null;
		String vtBackupURL = null;

		globalProp = (Properties) session.getServletContext().getAttribute("globalProp");
		cTimeout = (String) globalProp.getProperty("VTConnectionTimeout");
		sTimeout = (String) globalProp.getProperty("VTSocketTimeout");
		vtPrimaryURL = (String) globalProp.getProperty("VTPrimaryURL");
		vtBackupURL = (String) globalProp.getProperty("VTBackupURL");
		 
		String vtOperation = (String) session.getAttribute("VT_Operation");
		String vruNumber = (String) session.getAttribute("vru_number");
		String serialNumber = (String) session.getAttribute("emp_number");
		String tenant = (String) session.getAttribute("account_name");
		String language = (String) session.getAttribute("VT_Language");
		
		VT_Request_Data vtreq = new VT_Request_Data();
		vtreq.setPrimaryURL(vtPrimaryURL);
		vtreq.setBackupURL(vtBackupURL);
		vtreq.setOperationType(vtOperation);
		vtreq.setVruNumber(vruNumber);
		vtreq.setSerialNumber(serialNumber);
		vtreq.setTenant(tenant);
		vtreq.setLanguage(language);
		
		if (testCall) {
			LOGGER.info(new StringBuffer(logToken).append(" - Got all attributes from the session. ")
					.append("VT_Operation: ").append(vtOperation)
					.append("vru_number: ").append(vruNumber)
					.append("emp_number: ").append(serialNumber)
					.append("account_name: ").append(tenant)
					.append("VT_Language: ").append(language)
					.append("vtPrimaryURL: ").append(vtPrimaryURL)
					.append("vtBackupURL: ").append(vtBackupURL)
					);
			
		}

		VT_Response_Data vtresp = null;
		VT_Server_Query vtTrans = null; 

		try {
			// send query to VT
			vtTrans = new VT_Server_Query(cTimeout, sTimeout, callid, testCall);
			vtresp = vtTrans.sendRequest(vtreq);
			
			if (testCall) {
				LOGGER.info(new StringBuffer(logToken).append(" - vtresp:").append(url));
				LOGGER.info(new StringBuffer(logToken).append(" - Payment Gateway ConnectionTimeout: ").append(cTimeout));
				LOGGER.info(new StringBuffer(logToken).append(" - Payment Gateway SocketTimeout: ").append(sTimeout));
			}

		} catch (Exception e) {
			LOGGER.error(new StringBuffer(logToken).append("Error in VoiceTrust interface:").append(e.getMessage()));
		}

		if (vtresp != null) {
			if (testCall) {
				LOGGER.info(new StringBuffer(logToken).append(" VT Response:"));
				LOGGER.info(new StringBuffer(logToken).append(" - retCode: ").append(vtresp.getRetCode()));
				LOGGER.info(new StringBuffer(logToken).append(" - retStatus: ").append(vtresp.getRetStatus()));
				LOGGER.info(new StringBuffer(logToken).append(" - enrollStatus: ").append(vtresp.getEnrollStatus()));
				LOGGER.info(new StringBuffer(logToken).append(" - userInfo: ").append(vtresp.getUserInfo()));
				LOGGER.info(new StringBuffer(logToken).append(" - retData: ").append(vtresp.getRetData()));
			}
			session.setAttribute("automation_rc", Integer.toString(vtresp.getRetCode()));
			session.setAttribute("automation_status", vtresp.getRetStatus());
			session.setAttribute("enrollment_status", vtresp.getEnrollStatus());
			session.setAttribute("automation_data", vtresp.getRetData());
		} else {
			if (testCall) {
				LOGGER.info(new StringBuffer(logToken).append(" - vtresp = null; setting default error values"));
			}
			session.setAttribute("automation_rc", "97");
			session.setAttribute("automation_status", "VT did not Return Headers");
			session.setAttribute("automation_data", "RC=97|VT did not Return Headers");
		}
		
		if (testCall) {
			LOGGER.info(new StringBuffer(logToken).append(" - Values in session:"));
			LOGGER.info(new StringBuffer(logToken).append(" - automation_rc: ").append((String)session.getAttribute("automation_rc")));
			LOGGER.info(new StringBuffer(logToken).append(" - automation_status: ").append((String)session.getAttribute("automation_status")));
			LOGGER.info(new StringBuffer(logToken).append(" - enrollment_status: ").append((String)session.getAttribute("enrollment_status")));
			LOGGER.info(new StringBuffer(logToken).append(" - automation_data: ").append((String)session.getAttribute("automation_data")));
			LOGGER.info(new StringBuffer(logToken).append("Exiting VT_Query handler"));
		}

	return;
	}

}

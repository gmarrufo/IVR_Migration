package com.ibm.ivr.eus.handler;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/*
 * Handler for EWT action.  Sets the action to be performed - BOTH, EWT, QPOS, NONE and the appropriate variables used by these actions.
 */

public class Get_EWT_Action extends HttpServlet implements Servlet {
	
	private static final long serialVersionUID = 1L;
	private static Logger LOGGER = Logger.getLogger(Get_EWT_Action.class);

	public Get_EWT_Action() {
		// TODO Auto-generated constructor stub
		super();
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}   	

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		// get session from Servlet request
		HttpSession session = req.getSession(false);

		String ewt_action = "NONE";
		String play_ewt_tones = "";
		String play_qpos_tones = "";
		String wait_time = "";
		String q_pos = "";
		String call_status = "";
		
		String callid = (String) session.getAttribute("callid");
		boolean testCall = ((Boolean) session.getAttribute("testCall")).booleanValue();
		
		//create the log Token for later use, use StringBuffer to reduce number of String objects
		String logToken = new StringBuffer("[").append(callid).append("] ").toString();

		if (testCall)
			LOGGER.info(new StringBuffer(logToken).append("Entering Get_EWT_Action Handler"));
		
		// get value of ewt_tones and ewt_roundup_seconds from session
		String switch_tones_1 = (String) session.getAttribute("switch_tones_1");
		String switch_tones_2 = (String) session.getAttribute("switch_tones_2");
		String caller_treatment = (String) session.getAttribute("caller_treatment");
		String lower_threshold = (String) session.getAttribute("lower_threshold");
		String upper_threshold = (String) session.getAttribute("upper_threshold");
		String lower_threshold_action = (String) session.getAttribute("lower_threshold_action");
		String upper_threshold_action = (String) session.getAttribute("upper_threshold_action");
		int switch_tones_1_int = 0;
		int switch_tones_2_int = 0;
		int lower_threshold_int = -1;
		int upper_threshold_int = -1;
		
		if (testCall) {
			LOGGER.info(new StringBuffer(logToken).append("switch_tones_1 = ").append(switch_tones_1));
			LOGGER.info(new StringBuffer(logToken).append("switch_tones_2 = ").append(switch_tones_2));
			LOGGER.info(new StringBuffer(logToken).append("caller_treatment = ").append(caller_treatment));
			LOGGER.info(new StringBuffer(logToken).append("lower_threshold = ").append(lower_threshold));
			LOGGER.info(new StringBuffer(logToken).append("upper_threshold = ").append(upper_threshold));
		}
		if (switch_tones_1 != null) {
			switch_tones_1.trim();
			if (switch_tones_1.length() == 0) {
				switch_tones_1 = "0";
			} else {
				try {
					switch_tones_1_int = Integer.parseInt(switch_tones_1);
				} catch (NumberFormatException e) {
					switch_tones_1_int = 0;
				}
			}
		} else {
			switch_tones_1 = "0";
		}
		
		if (switch_tones_2 != null) {
			switch_tones_2.trim();
			if (switch_tones_2.length() == 0) {
				switch_tones_2 = "0";
			} else {
				try {
					switch_tones_2_int = Integer.parseInt(switch_tones_2);
				} catch (NumberFormatException e) {
					switch_tones_2_int = 0;
				}
			}
		} else {
			switch_tones_2 = "0";
		}
		
		if (lower_threshold != null) {
			try {
				lower_threshold_int = Integer.parseInt(lower_threshold);
			} catch (NumberFormatException e) {
			}
		}
		
		if (upper_threshold != null) {
			try {
				upper_threshold_int = Integer.parseInt(upper_threshold);
			} catch (NumberFormatException e) {
			}
		}
		if (testCall) {
			LOGGER.info(new StringBuffer(logToken).append(" - switch_tones_1_int: ").append(switch_tones_1_int)
					.append("; switch_tones_2_int: ").append(switch_tones_2_int)
					.append("; lower_threshold_int: ").append(lower_threshold_int)
					.append("; upper_threshold_int: ").append(upper_threshold_int));
		}

		if (caller_treatment != null) {
			caller_treatment = caller_treatment.toUpperCase();
			if (caller_treatment.equals("BOTH")) {
				ewt_action = "BOTH";
				play_ewt_tones = switch_tones_1;
				play_qpos_tones = switch_tones_2;
				wait_time = switch_tones_1;
				q_pos = switch_tones_2;
			} else if (switch_tones_1_int < lower_threshold_int) {	// tone value less than lower threshold
				wait_time = switch_tones_1;
				q_pos = switch_tones_1;
				call_status = processThresholdAction(lower_threshold_action);
			} else if (switch_tones_1_int >= upper_threshold_int) {	// tone value higher than upper threshold
				wait_time = switch_tones_1;
				q_pos = switch_tones_1;
				call_status = processThresholdAction(upper_threshold_action);
			} else if (switch_tones_1_int >= lower_threshold_int && switch_tones_1_int < upper_threshold_int) {	// tones within threshold
				if (caller_treatment.equals("EWT")) {	// EWT
					ewt_action = "EWT";
					play_ewt_tones = switch_tones_1;
					wait_time = switch_tones_1;
				} else if (caller_treatment.equals("QPOS")) {	// queue position
					ewt_action = "QPOS";
					play_qpos_tones = switch_tones_1;
					q_pos = switch_tones_1;
				}
			}
		}
		
		session.setAttribute("ewt_action", ewt_action);
		session.setAttribute("play_ewt_tones", play_ewt_tones);
		session.setAttribute("play_qpos_tones", play_qpos_tones);
		session.setAttribute("wait_time", wait_time);
		session.setAttribute("q_pos", q_pos);
		if (call_status.equals("HUP")) {
			session.setAttribute("call_status", call_status);
		}
		
		if (testCall) {
			LOGGER.info(new StringBuffer(logToken).append(" - Values in session:"));
			LOGGER.info(new StringBuffer(logToken).append(" - ewt_action: ").append(session.getAttribute("ewt_action")));
			LOGGER.info(new StringBuffer(logToken).append(" - play_ewt_tones: ").append(session.getAttribute("play_ewt_tones")));
			LOGGER.info(new StringBuffer(logToken).append(" - play_qpos_tones: ").append(session.getAttribute("play_qpos_tones")));
			LOGGER.info(new StringBuffer(logToken).append(" - wait_time: ").append(session.getAttribute("wait_time")));
			LOGGER.info(new StringBuffer(logToken).append(" - q_pos: ").append(session.getAttribute("q_pos")));
			LOGGER.info(new StringBuffer(logToken).append(" - call_status: ").append(session.getAttribute("call_status")));
			LOGGER.info(new StringBuffer(logToken).append("Exiting Get_EWT_Action handler"));
		}
		
		return;
	}
	/**
	 * processThresholdAction:  returns string "HUP" - can add more logic in this function later if required
	 * 
	 * @param action
	 * @return
	 */
	
	private String processThresholdAction(String action) {
		
		String retStr = "";
		
		if (action != null) {
			if (action.equals("") ||
					action.equals("HUP") ||
					action.compareToIgnoreCase("LOWER_THRESHOLD_ACTION") == 0 ||
					action.compareToIgnoreCase("UPPER_THRESHOLD_ACTION") == 0) {
				retStr = "HUP";
			}
	    } else {
	    	retStr = "HUP";
	    }
	    
		return retStr;
	}

}

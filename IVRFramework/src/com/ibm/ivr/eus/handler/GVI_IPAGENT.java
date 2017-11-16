/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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

/**
 *
 * @author MAYMITRA
 */
public class GVI_IPAGENT extends HttpServlet implements Servlet {
	
	private static final long serialVersionUID = 1L;
	private static Logger LOGGER = Logger.getLogger(GVI_IPAGENT.class);
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    	
		// get session from Servlet request
		HttpSession session = request.getSession(false);

		String callid = (String) session.getAttribute("callid");
		boolean testCall = ((Boolean) session.getAttribute("testCall")).booleanValue();
		// get value of ewt_tones and ewt_roundup_seconds from session
		Properties globalProp = (Properties) session.getServletContext().getAttribute("globalProp");

		//create the log Token for later use, use StringBuffer to reduce number of String objects
		String logToken = new StringBuffer("[").append(callid).append("] ").toString();

		if (testCall)
			LOGGER.debug(new StringBuffer(logToken).append("Entering GVI_IPAGENT Handler"));

        String filename = globalProp.getProperty("gviIPagentFile");// property value as in xml file
        String phone = (String) session.getAttribute("phone");
        
        String route = GviHelper.getGviLocation(filename, phone, testCall, logToken);

        session.setAttribute("xfer_var", route);
        session.setAttribute("gvi_code", route);
		if (testCall) {
			LOGGER.debug(new StringBuffer(logToken).append(" - Values in session:"));
			LOGGER.debug(new StringBuffer(logToken).append(" - xfer_var: ").append(session.getAttribute("xfer_var")));
			LOGGER.debug(new StringBuffer(logToken).append(" - gvi_code: ").append(session.getAttribute("gvi_code")));
			LOGGER.debug(new StringBuffer(logToken).append("Exiting GVI_IPAGENT handler"));
		}
		return;

    } 

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

}

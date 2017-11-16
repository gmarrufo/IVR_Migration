/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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

/**
 *
 * @author MAYMITRA
 */
public class Get_Variable_Length extends HttpServlet implements Servlet {
	
	private static final long serialVersionUID = 1L;
	private static Logger LOGGER = Logger.getLogger(Get_Variable_Length.class);
   
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
		int varLength = 0;
		
		//create the log Token for later use, use StringBuffer to reduce number of String objects
		String logToken = new StringBuffer("[").append(callid).append("] ").toString();

		if (testCall)
			LOGGER.info(new StringBuffer(logToken).append("Entering Get_Variable_Length Handler"));

        String varName = (String) session.getAttribute("var_length_name");
        if (varName != null) {
	        String var = (String) session.getAttribute(varName);
	        if (var != null) {
	        	varLength = var.length();
	        	if (testCall)
	    			LOGGER.info(new StringBuffer(logToken).append("length of variable: ").append(var)
	    					.append(" = ").append(varLength));
	        } else {
	        	if (testCall)
	    			LOGGER.info(new StringBuffer(logToken).append("variable: ").append(var)
	    					.append(" not in session"));
	        }
        }
        
        session.setAttribute("var_length_result", new Integer(varLength));
        
		if (testCall) {
			LOGGER.info(new StringBuffer(logToken).append(" - Values in session:"));
			LOGGER.info(new StringBuffer(logToken).append(" - var_length_result: ").append(session.getAttribute("var_length_result")));
			LOGGER.info(new StringBuffer(logToken).append("Exiting Get_Variable_Length handler"));
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

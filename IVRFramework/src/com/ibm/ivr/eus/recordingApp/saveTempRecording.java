package com.ibm.ivr.eus.recordingApp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
 * @author Val Peco
 * 
 * This handler written for EUS, saves a temporary file.
 */
public class saveTempRecording extends HttpServlet implements Servlet {

	private static Logger LOGGER = Logger.getLogger(saveTempRecording.class);

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// get session from Servlet request, created if not existed yet
		HttpSession session = req.getSession(true);
		
		File f;
		String dirPath = "";
		String status = "S";

		String callid = (String) session.getAttribute("callid");

		boolean testCall = ((Boolean) session.getAttribute("testCall")).booleanValue();

		//create the log Token for later use, use StringBuffer to reduce number
		//of String objects
		String logToken = new StringBuffer("[").append(callid).append("] ").toString();

		LOGGER.debug(new StringBuffer(logToken).append("saveTempRecording: Entering saveTempRecording handler"));
		
		Properties prop = (Properties) session.getServletContext().getAttribute("globalProp");

		byte[] recording = (byte[]) req.getAttribute("recording");

		// Save the recorded audio from the caller
		try
		{
			dirPath = (String)session.getAttribute("dirPath");
	    	  
	    	f = new File(dirPath + "tempRecording" + prop.getProperty("audioFileExtension"));
			LOGGER.debug(new StringBuffer(logToken).append("saveTempRecording: temp file: " + f));
	           
			FileOutputStream fos = new FileOutputStream(f);
	 		 
	 		// write byte array to a file.
	 		fos.write(recording);
	          
	        fos.flush();
	        fos.close();

	     }catch(FileNotFoundException ex){
	    	 LOGGER.error(new StringBuffer(logToken).append("saveTempRecording Exception::").append(ex));
	    	 status = "E";
	     }catch(IOException ioe){
	    	 LOGGER.error(new StringBuffer(logToken).append("saveTempRecording Exception::").append(ioe));
	    	 status = "E";
	     }
		
		// Set the session variables for the return data.
		session.setAttribute("Status", status);

		// Print out the values.
		if (testCall)
		{
			LOGGER.debug(new StringBuffer(logToken).append("saveTempRecording: Status: " + status));
		}

		return;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}
}

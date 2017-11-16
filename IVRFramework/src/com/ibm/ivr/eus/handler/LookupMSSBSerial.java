package com.ibm.ivr.eus.handler;

import java.io.IOException;
import java.util.Properties;
import java.util.Vector;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import com.ibm.ivr.eus.dao.EUSSETDAO;
import com.ibm.ivr.eus.dao.EUSSETDTO;
import com.ibm.ivr.eus.data.CommonValues;
import com.ibm.ivr.eus.data.DbQueryTimeout;

public class LookupMSSBSerial extends HttpServlet implements Servlet{

	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")

	private static Logger LOGGER = Logger.getLogger(LookupMSSBSerial.class);
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		doGet(req, resp);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
							throws ServletException, IOException {

	// get session from Servlet request, created if not existed yet
	HttpSession session = req.getSession(true);
	String temp_serial="";
	String active_sca_ent_db="";
	int maxCount=0;
	
	String callid = (String) session.getAttribute("callid");

	boolean testCall = ((Boolean) session.getAttribute("testCall")).booleanValue();

		//create the log Token for later use, use StringBuffer to reduce number
		// of String objects
	String logToken = new StringBuffer("[").append(callid).append("] ").toString();
		 
	 if (testCall)
	 	LOGGER.info(new StringBuffer(logToken).append("Entering LookupMSSBSerial handler"));
		
    // Get attributes from the request	
	Object tempObj=session.getAttribute("serial_number");
	if(tempObj != null)
		temp_serial=tempObj.toString();
	
	tempObj=session.getAttribute("db_table_name");
	if(tempObj != null)
		active_sca_ent_db=tempObj.toString();
	
	tempObj=session.getAttribute("max_count");
	if(tempObj != null && !(tempObj.toString().trim().equals("")))
		maxCount=Integer.parseInt(tempObj.toString());
	
	String row = null;
	Integer return_count = 0;
	String rc = "-1";
	
	String jndiName = null;
	
	EUSSETDTO returndto;   
	Vector<StringBuffer> valueSet = new Vector<StringBuffer>();
	
    // Debug Logging		
	if (testCall){
		LOGGER.info(new StringBuffer(logToken).append("temp_serial = ").append(temp_serial));	
		LOGGER.info(new StringBuffer(logToken).append("active_sca_ent_db = ").append(active_sca_ent_db));	
	}
    try {
    	Properties globalProp = null;

    	globalProp = (Properties) session.getServletContext().getAttribute("globalProp");

    	jndiName = globalProp.getProperty(CommonValues.EUS_DATA_SOURCE_PROPERTY);
    	
    	/* create data access object interface to db */
    	int queryTimeout = DbQueryTimeout.getDbQueryTimout(globalProp);
    	EUSSETDAO eussetdao = new EUSSETDAO(jndiName, callid, testCall, queryTimeout);
	
        /* lookup finds row */
    	returndto = eussetdao.geteusSET(active_sca_ent_db, temp_serial);
   
       	if (returndto != null) {
    		valueSet = returndto.getVALUESET();	
 		   if(valueSet != null){
			   return_count=valueSet.size();
			   if(testCall)
				   LOGGER.info(new StringBuffer(logToken).append("Setting n_returned to "+return_count));
			   
			   session.setAttribute("n_returned", return_count);
			   String result="";
			   Object tempResult=null;
			   String[] resultValues=null;
			   int index=0;
			   for(int i=0;i<return_count && i<maxCount;i++){
				   tempResult=valueSet.get(i);
				   index++;
				   if(tempResult != null){
					   result=tempResult.toString();
					   // now separate the serial from the level
					   resultValues=result.split(":");
					   if(resultValues != null && resultValues.length == 2){
						   if(resultValues[0] != null & resultValues[1] != null){
							   // Set Attrbute Values
							   if(testCall)
								   LOGGER.info(new StringBuffer(logToken).append("Serial is "+resultValues[0] + " Level ="+resultValues[1]));
							   session.setAttribute("Serial_Number_"+index, resultValues[0]);
							   session.setAttribute("Serial_Level_"+index, resultValues[1]);
						   }
					   }
				   }
			   }
			   rc = "0";
		   }
    	}
	} catch (Exception e) {
		
		e.printStackTrace();
	}	
	
	// Set session variables needed for reply
	session.setAttribute("return_vector", valueSet);
	session.setAttribute("return_count", return_count);
	session.setAttribute("rc", rc);	
	 
	// Set session variables needed for reply
	
	 if (testCall){
	 	LOGGER.info(new StringBuffer(logToken).append("return_count: ").append(return_count));
	 	LOGGER.info(new StringBuffer(logToken).append("returning rc: ").append(rc));
	 }	
   return;
  }
}
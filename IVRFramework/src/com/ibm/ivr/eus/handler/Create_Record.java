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

import com.ibm.ivr.eus.dao.EUSVARDAO;
import com.ibm.ivr.eus.dao.EUSVARDTO;
import com.ibm.ivr.eus.data.CommonValues;
import com.ibm.ivr.eus.data.DbQueryTimeout;


/************************************************************************
 *                           - Create_Record -                           
 * Handler called by applications to update a record or create it if it 
 * does not exist in the database table     
 *  			             					
 * Properties needed from global.properties file:
 * 	 - eusDataSource
 * 
 * Parameters needing to be defined in session include:
 *   - db_table - DB table name                                                 
 *   - db_key  - table key column
 *   - db_value - table value column                                               
 *   - globalProp   - set by framework
 *   
 * Outputs:
  *  - db_rc:  0 is success; -1 is failure
 * 
 *
 ************************************************************************/

public class Create_Record extends HttpServlet implements Servlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger LOGGER = Logger.getLogger(Create_Record.class);

	public Create_Record() {
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
			LOGGER.info(new StringBuffer(logToken).append("Entering Create_Record"));

		Properties globalProp = null;
		String jndiName = null;

		globalProp = (Properties) session.getServletContext().getAttribute("globalProp");
		 
		String dbTable = (String) session.getAttribute("db_table");
		String dbKey = (String) session.getAttribute("db_key");
		String dbValue = (String) session.getAttribute("db_value");
		EUSVARDTO eusvardto = new EUSVARDTO();
		String rc = "-1";
		
		if (testCall) {
			LOGGER.info(new StringBuffer(logToken).append(" - Got all attributes from the session. ")
					.append("db_table: ").append(dbTable)
					.append("; db_key: ").append(dbKey)
					.append("; db_value: ").append(dbValue)
					);
			
		}

		try {
			eusvardto.setAppName(dbTable);
			eusvardto.setKEY(dbKey);
			eusvardto.setVALUE(dbValue);
			
	    	jndiName = globalProp.getProperty(CommonValues.EUS_DATA_SOURCE_PROPERTY);
	    	int queryTimeout = DbQueryTimeout.getDbQueryTimout(globalProp);
	    	
	    	/* create data access object interface to db */
	    	EUSVARDAO eusvardao = new EUSVARDAO(jndiName, callid, testCall, queryTimeout);
	    	if (eusvardao.seteusKEY(eusvardto)) {
	    		if (testCall)
	    			LOGGER.info(new StringBuffer(logToken).append(" - Insert successful "));
	    		rc = "0";
	    	}
			
		} catch (Exception e) {
			LOGGER.error(new StringBuffer(logToken).append("Error in eusvardao.seteusKEY():").append(e.getMessage()));
		}

		session.setAttribute("db_rc", rc);
		
		if (testCall) {
			LOGGER.info(new StringBuffer(logToken).append(" - Values in session:"));
			LOGGER.info(new StringBuffer(logToken).append(" - db_rc: ").append((String)session.getAttribute("db_rc")));
			LOGGER.info(new StringBuffer(logToken).append("Exiting Create_Record"));
		}

	return;
	}

}

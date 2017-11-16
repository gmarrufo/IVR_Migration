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
 *                           - Get_Record -                           
 * Handler called by applications to get a record from the database table     
 *  			             					
 * Properties needed from global.properties file:
 * 	 - eusDataSource
 * 
 * Parameters needing to be defined in session include:
 *   - db_table - DB table name                                                 
 *   - db_key  - key to be looked up in table                                                      
 *   - globalProp   - set by framework
 *   
 * Outputs:
 * 	- db_value
 *  - db_rc:  0 is success; -1 is failure
 * 
 *
 ************************************************************************/

public class Get_Record extends HttpServlet implements Servlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger LOGGER = Logger.getLogger(Get_Record.class);

	public Get_Record() {
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
			LOGGER.info(new StringBuffer(logToken).append("Entering Get_Record"));

		Properties globalProp = null;
		String jndiName = null;

		globalProp = (Properties) session.getServletContext().getAttribute("globalProp");
		 
		String dbTable = (String) session.getAttribute("db_table");
		String dbKey = (String) session.getAttribute("db_key");
		String dbValue = null;
		EUSVARDTO eusvardto = null;
		String rc = "-1";
		
		if (testCall) {
			LOGGER.info(new StringBuffer(logToken).append(" - Got all attributes from the session. ")
					.append("db_table: ").append(dbTable)
					.append("; db_key: ").append(dbKey)
					);
			
		}

		try {
			
	    	jndiName = globalProp.getProperty(CommonValues.EUS_DATA_SOURCE_PROPERTY);
	    	
	    	/* create data access object interface to db */
	    	int queryTimeout = DbQueryTimeout.getDbQueryTimout(globalProp);
	    	EUSVARDAO eusvardao = new EUSVARDAO(jndiName, callid, testCall, queryTimeout);
	    	eusvardto = eusvardao.geteusKEY(dbTable, dbKey);
			
		} catch (Exception e) {
			LOGGER.error(new StringBuffer(logToken).append("Error in eusvardao.geteusKEY():").append(e.getMessage()));
		}

		if (eusvardto != null) {
			if (testCall) {
				LOGGER.info(new StringBuffer(logToken).append(" Query result:"));
				LOGGER.info(new StringBuffer(logToken).append(" - value: ").append(eusvardto.getVALUE()));
			}
			dbValue = eusvardto.getVALUE();
			rc = "0";
			session.setAttribute("db_value", eusvardto.getVALUE());
			session.setAttribute("db_rc", "0");
		} else {
			if (testCall) {
				LOGGER.info(new StringBuffer(logToken).append(" - eusvardto = null"));
			}
		}
		
		if (dbValue == null)
			dbValue = "";
		session.setAttribute("db_value", dbValue);
		session.setAttribute("db_rc", rc);
		
		if (testCall) {
			LOGGER.info(new StringBuffer(logToken).append(" - Values in session:"));
			LOGGER.info(new StringBuffer(logToken).append(" - db_value: ").append((String)session.getAttribute("db_value")));
			LOGGER.info(new StringBuffer(logToken).append(" - db_rc: ").append((String)session.getAttribute("db_rc")));
			LOGGER.info(new StringBuffer(logToken).append("Exiting Get_Record"));
		}

	return;
	}

}

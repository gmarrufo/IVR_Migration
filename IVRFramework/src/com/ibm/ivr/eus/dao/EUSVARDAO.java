/*
 * Created on Nov 11, 2010
 *
 * @Author Alan McDonley
 * 
 * 
 */
package com.ibm.ivr.eus.dao;

//jdbc imports
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.ibm.ivr.eus.data.CommonValues;
import com.ibm.websphere.ce.cm.StaleConnectionException;

/**
 * 
 * @doc
 * 		The EUSVARDAO class is used to manipulate 
 *   	EUSVARDTO objects each containing appName-KEY-VALUE information
 * 
 *      appName is used as the table name in the EUSDB - ie IBMHELP for the IBM Help application
 * 		KEY is the application variable name
 * 		VALUE is the unique String value for the KEY
 * 
 * 		There are three methods:
 * 				<EUSVARDAO>.seteusKEY(<EUSVARDTO>)  puts a single KEY-VALUE pair (row) into the DB
 * 				<EUSVARDAO>.geteusKEY("appName","KEY") retrieve a single KEY-VALUE pair from the DB
 * 				<EUSVARDAO>.deleteKey("appName","KEY") deletes one row (or more) from DB
 * 								(KEY may contain '_' to match any char in that position
 * 											or   '%' to match one or more chars in that position)
 * 				<EUSVARDAO>.getAllAppVars("appName") returns an array of EUSVARDTO objects for all KEY-VALUE pairs in the DB
 * 		
 */
public class EUSVARDAO extends BaseDAO {
	 /**
	 * Commons logger for <code>logger</code>.
	 */
	private static Logger LOGGER = Logger.getLogger(EUSVARDAO.class);
	private boolean testCall = false;
	private String logToken = null;
	private int queryTimeout = 0;

	public EUSVARDAO(String ds, String callId, boolean testCall, int queryTimeout) {

//		this.dsSource = this.getDataSource(ds);
		this.getDataSource(ds);
		this.testCall = testCall;
		this.queryTimeout = queryTimeout;
		logToken = new StringBuffer("[").append(callId).append("] ").toString();
	}


	/**
	 * Update (or insert) a key value pair in an application's table
	 * 
	 * @param eusvardto
	 *            EUSVARDTO containing appName-KEY-VALUE set to update 
	 *            in the appName table in the EUSDB
	 * @return true if added successfully, false otherwise
	 */
	public boolean seteusKEY(EUSVARDTO eusvardto){
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = null;


		if (eusvardto.getAppName()==null || eusvardto.getKEY()==null) {
			LOGGER.error(new StringBuffer(logToken).append("Error updateeusKEY - Null key: [")
					.append(eusvardto.getKEY())  
					.append(" and/or null Table:")
					.append(eusvardto.getAppName())
					.append(" ]"));
			return false;
			
		}
		
		String appName = eusvardto.getAppName().toUpperCase();
		String key = eusvardto.getKEY();
		String value = eusvardto.getVALUE();
		boolean retry = false;
        int retryAttempts = 0;
        
        do {
        	try {
        		retry = false;
        		conn = getConnection();	

        		sql = "UPDATE "+appName+" SET VALUE = ? WHERE KEY = ?"; 
        		if (testCall)
        			LOGGER.info(new StringBuffer(logToken).append("Executing:\"")
        					.append(sql).append("\""));
        		stmt = conn.prepareStatement(sql);
        		if (queryTimeout > 0) {
    				stmt.setQueryTimeout(queryTimeout);
    			}
        		stmt.setString(1, value);
        		stmt.setString(2, key);
        		stmt.execute();
        		if (stmt.getUpdateCount()==0) {
        			stmt.close();
        			sql = "INSERT INTO "+appName+" (KEY, VALUE) values (?, ?)";
        			stmt = conn.prepareStatement(sql);
        			stmt.setString(1, key);
            		stmt.setString(2, value);
        			if (testCall)
        				LOGGER.info(new StringBuffer(logToken).append("Executing:\"")
        						.append(sql).append("\""));
        			stmt.execute();

        		}

        	}//end try
        	catch (StaleConnectionException sce) {
        		if (retryAttempts++ < CommonValues.MAX_CONN_RETRIES) {
        			retry = true;
        			if (testCall) {
        				LOGGER.info(new StringBuffer(logToken).append(" StaleConnection - retrying, attempt = ").append(retryAttempts));
        			}
        		} else {
        			retry = false;
        			LOGGER.error(new StringBuffer(logToken).append(" StaleConnectionException seteusKEY: ")
        					.append(key).append(", value = ").append(value).append(" Table:").append(appName)
        					.append("; exception: ").append(sce.getMessage()));
        			return false;
        		}
        	} //end catch
        	catch (SQLException e) {
        		LOGGER.error(new StringBuffer(logToken).append("SQLException seteusKEY: [")
        				.append(key).append(",").append(value)
        				.append(" Table:").append(appName).append(" ]"), e);
        		return false;
        	}//end catch
        	catch (Exception e) {
        		LOGGER.error(new StringBuffer(logToken).append("Exception seteusKEY: [")
        				.append(key).append(",").append(value)
        				.append(" Table:").append(appName).append(" ]"), e);
        		return false;
        	}//end catch
        	finally {
        		releaseResource(conn, stmt, rs);
        	}//end finally
        } while (retry);
		
		return true;
	}
	
	/**
	 * Get an App Var
	 * 
	 * @param appName
	 * 			application name is used as table name ie. IBMHELP
	 * @param key
	 *            KEY to retrieve 
	 *            from the appName table in the EUSDB
	 * @return EUSVARDTO object, null if not found
	 */
	public EUSVARDTO geteusKEY(String appName, String key) {
		EUSVARDTO dto = new EUSVARDTO(); 
		dto.setAppName(appName);
		dto.setKEY(key);
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = null;
		
		boolean isResultSetEmpty = true;

		if (appName==null || key==null) {
			LOGGER.error(new StringBuffer(logToken).append("Error geteusKEY - Null key: [")
					.append(key)  
					.append(" and/or null Table:")
					.append(appName)
					.append(" ]"));
			return null;
			
		}
		boolean retry = false;
        int retryAttempts = 0;
        
        do {
        	try {
        		retry = false;
        		conn = getConnection();			
        		sql = "SELECT VALUE FROM " + appName.toUpperCase() 
        		+ " WHERE KEY = ?";
        		if (testCall)
        			LOGGER.info(new StringBuffer(logToken).append("Executing:\"")
        					.append(sql).append("\""));
        		stmt = conn.prepareStatement(sql);
        		if (queryTimeout > 0) {
    				stmt.setQueryTimeout(queryTimeout);
    			}
        		stmt.setString(1, key);
        		rs = stmt.executeQuery();

        		while ( rs.next() ) {
        			isResultSetEmpty = false;
        			dto.setVALUE(rs.getString(1));
        		}//end while

        	}//end try
        	catch (StaleConnectionException sce) {
        		if (retryAttempts++ < CommonValues.MAX_CONN_RETRIES) {
        			retry = true;
        			if (testCall) {
        				LOGGER.info(new StringBuffer(logToken).append(" StaleConnection - retrying, attempt = ").append(retryAttempts));
        			}
        		} else {
        			retry = false;
        			LOGGER.error(new StringBuffer(logToken).append(" StaleConnectionException getting value for appName: ")
        					.append(appName).append(", key =").append(key)
        					.append(sce.getMessage()));
        			return null;
        		}
        	} //end catch
        	catch (SQLException e) {
        		LOGGER.error(new StringBuffer(logToken).append("SQLException getting value for appName = ")
        				.append(appName).append(", key =").append(key), e);
        		return null;
        	}//end catch
        	catch (Exception e) {
        		LOGGER.error(new StringBuffer(logToken).append("Exception getting value for appName = ")
        				.append(appName).append(", key =").append(key), e);
        		return null;
        	}//end catch
        	finally {
        		releaseResource(conn, stmt, rs);
        	}//end finally
        } while (retry);

		if (isResultSetEmpty){
			LOGGER.warn("No rows found for appName = " + appName + ", key = " + key);
			return null;
		}
		else return dto;
	}

	/**
	 * Get all app vars 
	 * 
	 * @return an array of EUSVARDTO objects belonging to an application, null if not found
	 */
	@SuppressWarnings("unchecked")
	public EUSVARDTO [] getAllAppVars(String appName) {
		Vector vcs = new Vector();
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = null;
		
		if (appName==null ) {
			LOGGER.error(new StringBuffer(logToken).append("Error getAllAppVars - Null Table:"));

			return null;
			
		}
		boolean retry = false;
        int retryAttempts = 0;
        
        do {
        	try {
        		retry = false;
        		conn = getConnection();			
        		sql = "SELECT KEY, VALUE FROM " + appName.toUpperCase() ; 
        		if (testCall)
        			LOGGER.info(new StringBuffer(logToken).append("Executing:\"")
        					.append(sql).append("\""));
        		stmt = conn.prepareStatement(sql);
        		if (queryTimeout > 0) {
    				stmt.setQueryTimeout(queryTimeout);
    			}
        		rs = stmt.executeQuery();

        		while ( rs.next() ){
        			EUSVARDTO dto = new EUSVARDTO(); 
        			dto.setKEY(rs.getString(1));
        			dto.setVALUE(rs.getString(2));
        			vcs.add(dto);
        		}//end while

        	}//end try
        	catch (StaleConnectionException sce) {
        		if (retryAttempts++ < CommonValues.MAX_CONN_RETRIES) {
        			retry = true;
        			if (testCall) {
        				LOGGER.info(new StringBuffer(logToken).append(" StaleConnection - retrying, attempt = ").append(retryAttempts));
        			}
        		} else {
        			retry = false;
        			LOGGER.error(new StringBuffer(logToken).append(" StaleConnectionException getting all vars for eusApp: ")
        					.append(appName).append("; exception: ").append(sce.getMessage()));
        			return null;
        		}
        	} //end catch
        	catch (SQLException e) {
        		LOGGER.error(new StringBuffer(logToken).append("SQLException getting all vars for eusApp = ")
        				.append(appName), e);
        		return null;
        	}//end catch
        	catch (Exception e) {
        		LOGGER.error(new StringBuffer(logToken).append("Exception getting all vars for eusApp = ")
        				.append(appName), e);
        		return null;
        	}//end catch
        	finally {
        		releaseResource(conn, stmt, rs);
        	}//end finally
        } while (retry);

		if (vcs.size() == 0){
			LOGGER.warn("No vars for appName:" + appName +" found ");
			return null;
		}
		else {
			EUSVARDTO [] dtos = new EUSVARDTO[vcs.size()];
			for(int i = 0; i < vcs.size(); i++){
				dtos[i] = (EUSVARDTO)vcs.get(i);
			}
			return dtos;
		}
	}
	
	/**
	 * Delete a var 
	 * 
	 * @param appName
	 * @param key
	 * 			key to delete
	 * @return true if deleted successfully, false otherwise
	 */
	public boolean deleteKEY(String appName, String key) {
	
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = null;

		if (appName==null || key==null) {
			LOGGER.error(new StringBuffer(logToken).append("Error deleteeusKEY - Null key: [")
					.append(key)  
					.append(" and/or null Table:")
					.append(appName)
					.append(" ]"));
			return false;
			
		}
		boolean retry = false;
        int retryAttempts = 0;
        
        do {
        	try {
        		retry = false;
        		conn = getConnection();			
        		sql = "DELETE FROM " + appName
        		+ " WHERE KEY = ?";
        		
        		if (testCall)
        			LOGGER.info(new StringBuffer(logToken).append("Executing:\"")
        					.append(sql).append("\""));
        		stmt = conn.prepareStatement(sql);
        		if (queryTimeout > 0) {
    				stmt.setQueryTimeout(queryTimeout);
    			}
        		stmt.setString(1, key);
        		stmt.execute();

        	}//end try
        	catch (StaleConnectionException sce) {
        		if (retryAttempts++ < CommonValues.MAX_CONN_RETRIES) {
        			retry = true;
        			if (testCall) {
        				LOGGER.info(new StringBuffer(logToken).append(" StaleConnection - retrying, attempt = ").append(retryAttempts));
        			}
        		} else {
        			retry = false;
        			LOGGER.error(new StringBuffer(logToken).append(" StaleConnectionException deleting key: ")
        					.append(key).append(" for appName:").append(appName)
        					.append("; exception: ").append(sce.getMessage()));
        			return false;
        		}
        	} //end catch
        	catch (SQLException e) {
        		LOGGER.error(new StringBuffer(logToken).append("SQLException deleting key = ")
        				.append(key).append(" for appName:").append(appName), e);
        		return false;
        	}//end catch
        	catch (Exception e) {
        		LOGGER.error(new StringBuffer(logToken).append("Exception deleting key = ")
        				.append(key).append(" for appName:").append(appName), e);
        		return false;
        	}//end catch
        	finally {
        		releaseResource(conn, stmt, rs);
        	}//end finally
        } while (retry);

		return true;
	}
}

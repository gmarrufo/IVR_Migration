/*
 * Created on Dec 13, 2010
 *
 * @Author Val Peco
 * 
 * 
 */
package com.ibm.ivr.eus.dao;

//jdbc imports
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.log4j.Logger;

import com.ibm.ivr.eus.data.CommonValues;
import com.ibm.websphere.ce.cm.StaleConnectionException;

/**
 * @author Val Peco
 * @doc
 * 		The STATUSMSGDAO class is used to manipulate data in the 
 *   	STATUS_MESSAGE_DATA table from the EUSDB.
 *   
 */

/* -------------------------------------------
 * Change history:
 *
 * SCS - 12-15-2010 added getAllStatusMessages
 * VRP - 12-16-2010 Save the date in YYYYMMDDHHMM format
 * SCS - 12-16-2010 modified getAllStatusMessages to get rs # of rows properly
 * VRP - 12-17-2010 added activateStatusMessage method
 * 
 */  


public class STATUSMSGDAO extends BaseDAO {
	 /**
	 * Commons logger for <code>logger</code>.
	 */
	private static Logger LOGGER = Logger.getLogger(STATUSMSGDAO.class);

	// for the insert-update method
	private final static String updateSQLNoVDN = "UPDATE STATUS_MESSAGE_DATA SET STARTTIME = ?, ENDTIME = ?, BARGEIN = ?, VDN = ? " +
		" WHERE APPNAME = ? AND LANGUAGE = ? AND MESSAGEID = ?";
	private final static String updateSQLVDN = "UPDATE STATUS_MESSAGE_DATA SET STARTTIME = ?, ENDTIME = ?, BARGEIN = ?, VDN = ? " +
	" WHERE APPNAME = ? AND LANGUAGE = ? AND MESSAGEID = ? AND VDN = ?";
	private final static String insertSQL = "INSERT INTO STATUS_MESSAGE_DATA (StartTime,EndTime,Bargein,VDN,AppName,Language,MessageID) values" +
		" (?, ?, ?, ?, ?, ?, ?)"; 

	// for the activate method
	private final static String activateSQL = "UPDATE STATUS_MESSAGE_DATA SET VDN = ? " +
		" WHERE APPNAME = ? AND LANGUAGE = ? AND MESSAGEID = ?";

	// for the get method
	private final static String getSQL = "SELECT StartTime, EndTime FROM STATUS_MESSAGE_DATA WHERE AppName = ? AND Language = ? AND MessageID = ? AND VDN = ?";
	private final static String getNullSQL = "SELECT StartTime, EndTime FROM STATUS_MESSAGE_DATA WHERE AppName = ? AND Language = ? AND MessageID = ? AND VDN IS NULL";

	// for the getAllStatusMessages method
	private final static String getAllNullAndVdnSQL = "SELECT StartTime, EndTime, Bargein, VDN, AppName, Language, MessageID FROM STATUS_MESSAGE_DATA WHERE AppName = ? AND (VDN IS NULL OR VDN = ?)"; 
	private final static String getAllNullSQL = "SELECT StartTime, EndTime, Bargein, VDN, AppName, Language, MessageID FROM STATUS_MESSAGE_DATA WHERE AppName = ? AND VDN IS NULL";
	
	// for the delete method
	private final static String deleteSQL = "DELETE FROM STATUS_MESSAGE_DATA WHERE AppName = ? AND Language = ? AND MessageID = ? AND VDN = ?";
	private final static String deleteNullSQL = "DELETE FROM STATUS_MESSAGE_DATA WHERE AppName = ? AND Language = ? AND MessageID = ? AND VDN IS NULL";
	
	private boolean testCall = false;
	private String logToken = null;
	private int queryTimeout = 0;

	public STATUSMSGDAO(String ds, String callId, boolean testCall, int queryTimeout) {

//		this.dsSource = this.getDataSource(ds);
		logToken = new StringBuffer("[").append(callId).append("] ").toString();
		if (testCall)
			LOGGER.info(new StringBuffer(logToken).append("STATUSMSGDAO:  start constructor"));
		this.getDataSource(ds);
		this.testCall = testCall;
		this.queryTimeout = queryTimeout;
		if (testCall)
			LOGGER.info(new StringBuffer(logToken).append("STATUSMSGDAO:  end constructor"));
		
	}

	
	/**
	 * Add or Update a status message entry
	 * 
	 * @param appName
	 * @param language
	 * @param segmentID
	 * @param VDN
	 * @param startTime
	 * @param endTime
	 * @param bargein
	 * 			
	 * @return true if added successfully, false otherwise
	 */
	public boolean addStatusMsg(boolean update, String appName, String language, String segmentID, String VDN,
			String startTime, String endTime, String bargein) {
	
		Connection conn = null;
		PreparedStatement stmt = null;
		String sql = null;

		String month;
		String day;
		String year;
		String time;
		String newTimeFormat;

		// check if the passed values are valid.
		if (appName == null || language == null || segmentID == null)
		{
			LOGGER.error(new StringBuffer(logToken).append("addStatusMsg: Error: Null key: [appName = ")
					.append(appName)  
					.append(" language = ")
					.append(language)
					.append(" segmentID = ")
					.append(segmentID)
					.append(" ]"));
			return false;
		}
		
		if( update )
		{
			if( VDN == null )	
				sql = updateSQLNoVDN;
			else
				sql = updateSQLVDN;
		}
		else
			sql = insertSQL;
		
		boolean retry = false;
        int retryAttempts = 0;

        do {
        	try {
        		retry = false;
        		conn = getConnection();			
        		stmt = conn.prepareStatement(sql);
        		if (queryTimeout > 0) {
    				stmt.setQueryTimeout(queryTimeout);
    			}

        		if( startTime == null )
        			stmt.setNull(1, Types.CHAR);
        		else
        		{
        			month = startTime.substring(0, 2);
        			day = startTime.substring(2, 4);
        			year = startTime.substring(4, 8);
        			time = startTime.substring(8);
        			newTimeFormat = year + month + day + time;
        			stmt.setString(1, newTimeFormat);
        		}

        		if( endTime == null )
        			stmt.setNull(2, Types.CHAR);
        		else
        		{
        			month = endTime.substring(0, 2);
        			day = endTime.substring(2, 4);
        			year = endTime.substring(4, 8);
        			time = endTime.substring(8);
        			newTimeFormat = year + month + day + time;
        			stmt.setString(2, newTimeFormat);
        		}

        		stmt.setString(3, bargein);

        		if( VDN == null )
        			stmt.setNull(4, Types.VARCHAR);
        		else
        			stmt.setString(4, VDN);

        		stmt.setString(5, appName);				
        		stmt.setString(6, language);				
        		stmt.setString(7, segmentID);

        		if( update )
        		{
        			if( VDN != null )
        				stmt.setString(8, VDN);
        		}

        		if (LOGGER.isDebugEnabled())
        			LOGGER.debug("addStatusMsg: SQL: " + stmt);

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
        			LOGGER.error(new StringBuffer(logToken).append(" StaleConnectionException addStatusMsg: Error adding record. ")
        					.append("; exception: ").append(sce.getMessage()));
        			return false;
        		}
        	} //end catch
        	catch (SQLException e) {
        		LOGGER.error(new StringBuffer(logToken).append("SQLException addStatusMsg: Error adding record."), e);
        		return false;
        	}//end catch
        	catch (Exception e) {
        		LOGGER.error(new StringBuffer(logToken).append("Exception addStatusMsg: Error adding record."), e);
        		return false;
        	}//end catch

        	finally {
        		releaseResource(conn, stmt, null);
        	}//end finally
        } while (retry);

		return true;
	}
	

	/**
	 * Activate a status message entry by updating the VDN
	 * 
	 * @param appName
	 * @param language
	 * @param segmentID
	 * @param VDN
	 * 			
	 * @return true if updated successfully, false otherwise
	 */
	public boolean activateStatusMsg(String appName, String language, String segmentID, String VDN) {
	
		Connection conn = null;
		PreparedStatement stmt = null;
		String sql = null;

		// check if the passed values are valid.
		if (appName == null || language == null || segmentID == null)
		{
			LOGGER.error(new StringBuffer(logToken).append("activateStatusMsg: Error: Null key: [appName = ")
					.append(appName)  
					.append(" language = ")
					.append(language)
					.append(" segmentID = ")
					.append(segmentID)
					.append(" ]"));
			return false;
		}
		
		sql = activateSQL;

		boolean retry = false;
        int retryAttempts = 0;

        do {
        	try {
        		retry = false;
        		conn = getConnection();			
        		stmt = conn.prepareStatement(sql);
        		if (queryTimeout > 0) {
    				stmt.setQueryTimeout(queryTimeout);
    			}

        		if( VDN == null )
        			stmt.setNull(1, Types.VARCHAR);
        		else
        			stmt.setString(1, VDN);

        		stmt.setString(2, appName);				
        		stmt.setString(3, language);				
        		stmt.setString(4, segmentID);

        		if (LOGGER.isDebugEnabled())
        			LOGGER.debug("activateStatusMsg: SQL: " + stmt);

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
        			LOGGER.error(new StringBuffer(logToken).append(" StaleConnectionException activateStatusMsg: Error adding record. ")
        					.append("; exception: ").append(sce.getMessage()));
        			return false;
        		}
        	} //end catch
        	catch (SQLException e) {
        		LOGGER.error(new StringBuffer(logToken).append("SQLException activateStatusMsg: Error adding record."), e);
        		return false;
        	}//end catch
        	catch (Exception e) {
        		LOGGER.error(new StringBuffer(logToken).append("Exception activateStatusMsg: Error adding record."), e);
        		return false;
        	}//end catch

        	finally {
        		releaseResource(conn, stmt, null);
        	}//end finally
        } while (retry);

		return true;
	}
	
	

	/**
	 * Get a status message entry
	 * 
	 * @param appName
	 * @param language
	 * @param segmentID
	 * @param VDN
	 * 			
	 * @return true if deleted successfully, false otherwise
	 */
	public String[] getStatusMsgTimes(String appName, String language, String segmentID, String VDN) {
	
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		String sql = null;
		String month;
		String day;
		String year;
		String time;
		String newTimeFormat;
		String times[] = {"NOTSET","NOTSET"};

		// check if the passed values are valid.
		if (appName == null || language == null || segmentID == null)
		{
			LOGGER.error(new StringBuffer(logToken).append("getStatusMsg: Error: Null key: [appName = ")
					.append(appName)  
					.append(" language = ")
					.append(language)
					.append(" segmentID = ")
					.append(segmentID)
					.append(" ]"));
			return null;
		}
		
		if (testCall) {
			LOGGER.info(new StringBuffer(logToken).append("STATUSMSGDAO: appName: " + appName + ", language: " + language 
								+ " , segmentID: " + segmentID + " , VDN " + VDN));
		}

		if( VDN == null )
			sql = getNullSQL;
		else
			sql = getSQL;

		boolean retry = false;
        int retryAttempts = 0;

        do {
        	try {
        		retry = false;
        		conn = getConnection();			

        		stmt = conn.prepareStatement(sql);
        		if (queryTimeout > 0) {
    				stmt.setQueryTimeout(queryTimeout);
    			}
        		stmt.setString(1, appName);
        		stmt.setString(2, language);
        		stmt.setString(3, segmentID);
        		if( VDN != null )
        			stmt.setString(4, VDN);

        		if (LOGGER.isDebugEnabled())
        			LOGGER.debug("getStatusMsg: SQL: " + stmt.toString());

        		rs = stmt.executeQuery();
        		
        		// will return false if nothing was returned
        		if (testCall) {
        			LOGGER.info(new StringBuffer(logToken).append("STATUSMSGDAO: just executed the Query"));
        		}
        		
        		while (rs.next())  // will return false if nothing was returned
        		{
        			if (testCall) {
            			LOGGER.info(new StringBuffer(logToken).append("STATUSMSGDAO: top of rs.next loop"));
            		}
        			
        			newTimeFormat = rs.getString(1);
         			if (testCall) {
            			LOGGER.info(new StringBuffer(logToken).append("STATUSMSGDAO:  rs.getString(1) = "+ newTimeFormat));
            		}
        			
        			
        			if( newTimeFormat != null )
        			{
        				year = newTimeFormat.substring(0, 4);
        				month = newTimeFormat.substring(4, 6);
        				day = newTimeFormat.substring(6, 8);
        				time = newTimeFormat.substring(8);
        				times[0] = month + day + year + time;
        			}

        			else
        				times[0] = null;

        			newTimeFormat = rs.getString(2);
        			if (testCall) {
            			LOGGER.info(new StringBuffer(logToken).append("STATUSMSGDAO:  rs.getString(2) = "+ newTimeFormat));
            		}
        			if( newTimeFormat != null )
        			{
        				year = newTimeFormat.substring(0, 4);
        				month = newTimeFormat.substring(4, 6);
        				day = newTimeFormat.substring(6, 8);
        				time = newTimeFormat.substring(8);
        				times[1] = month + day + year + time;
        			}

        			else
        				times[1] = null;

        			if (testCall) {
            			LOGGER.info(new StringBuffer(logToken).append("STATUSMSGDAO: bottom of loop - times[0] = "+ times[0] 
            			            + " , times[1] = " + times[1]));
            		}
        			
        		}  // end of while
        		
        		// print here 
        		if (testCall) {
        			LOGGER.info(new StringBuffer(logToken).append("STATUSMSGDAO: DONE with loop - times[0] = "+ times[0] 
        			            + " , times[1] = " + times[1]));
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
        			LOGGER.error(new StringBuffer(logToken).append(" StaleConnectionException getStatusMsg: Error getting record. ")
        					.append("; exception: ").append(sce.getMessage()));
        			return null;
        		}
        	} //end catch
        	catch (SQLException e) {
        		LOGGER.error(new StringBuffer(logToken).append("SQLException getStatusMsg: Error getting record."), e);
        		return null;
        	}//end catch
        	catch (Exception e) {
        		LOGGER.error(new StringBuffer(logToken).append("Exception getStatusMsg: Error getting record."), e);
        		return null;
        	}//end catch
        	finally {
        		releaseResource(conn, stmt, rs);
        	}//end finally
        } while (retry);
        if (testCall) {
			LOGGER.info(new StringBuffer(logToken).append("STATUSMSGDAO: RETURNING NOW"));
		}
		return times;
	}
	
	// SCS - 12-15-2010 added getAllStatusMessages 
	/**
	 * Get all the status messages for this application  
	 * 
	 * @param appName
	 * @param VDN
	 * 			
	 * @return array of STATUSMSGDTO objects - one for each status msg record 
	 * -- or null if result set empty
	 */
	public STATUSMSGDTO [] getAllStatusMessages(String appName, String VDN) {
	
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = null;
		STATUSMSGDTO statusMessageMetaData[] = null;
		
		if (testCall)
			LOGGER.info(new StringBuffer(logToken).append("STATUSMSGDAO.getAllStatusMessages():  start; appName = ")
					.append(appName).append(", VDN = ").append(VDN));
		// check if the passed values are valid.
		if (appName == null)
		{
			LOGGER.error(new StringBuffer(logToken).append("getAllStatusMsg: Error: Null key: [appName = ")
					.append(appName)  
					.append(" ]"));
			return null;
		}

		if( VDN == null )
			sql = getAllNullSQL;
		else
			sql = getAllNullAndVdnSQL;


		boolean retry = false;
        int retryAttempts = 0;

        do {
        	try {
        		retry = false;
        		if (testCall)
        			LOGGER.info(new StringBuffer(logToken).append("STATUSMSGDAO.getAllStatusMessages():  calling getConnection"));
        		conn = getConnection();			
        		if (testCall)
        			LOGGER.info(new StringBuffer(logToken).append("STATUSMSGDAO.getAllStatusMessages():  finished getConnection, calling prepareStatement"));
        		//			stmt = conn.prepareStatement(sql);
        		stmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY );
        		if (testCall)
        			LOGGER.info(new StringBuffer(logToken).append("STATUSMSGDAO.getAllStatusMessages():  finished prepareStatement, calling setQueryTimeout"));
        		if (queryTimeout > 0) {
    				stmt.setQueryTimeout(queryTimeout);
    			}
        		if (testCall)
        			LOGGER.info(new StringBuffer(logToken).append("STATUSMSGDAO.getAllStatusMessages():  finished setQueryTimeout, calling executeQuery"));
        		stmt.setString(1, appName);

        		if( VDN != null )
        			stmt.setString(2, VDN);

        		if (testCall)
        			LOGGER.info(new StringBuffer(logToken).append("getAllStatusMsg: SQL: ").append(sql));

        		rs = stmt.executeQuery();
        		
        		if (testCall)
        			LOGGER.info(new StringBuffer(logToken).append("STATUSMSGDAO.getAllStatusMessages():  finished executeQuery"));

        		//			int numOfRows = rs.getFetchSize(); 
        		//			LOGGER.debug("*** numOfRows = " + numOfRows);

        		// get size of result set 
        		int size =0;  
        		if (rs != null)    {  
        			rs.beforeFirst();  
        			rs.last();  
        			size = rs.getRow();  
        		}  
        		if (testCall)
        			LOGGER.info(new StringBuffer(logToken).append("STATUSMSGDAO.getAllStatusMessages(): size = ").append(size));


        		if (size > 0) {
	        		statusMessageMetaData = new STATUSMSGDTO [size];
	
	        		int i = 0;
	        		rs.beforeFirst(); 
	        		while (rs.next())
	        		{
	        			if (testCall)
	        				LOGGER.info(new StringBuffer(logToken).append("STATUSMSGDAO.getAllStatusMessages(): rs.toString() = ").append(rs.toString()));
	
	        			STATUSMSGDTO dto = new STATUSMSGDTO();
	
	        			dto.setAppName(rs.getString("APPNAME"));
	        			dto.setLanguage(rs.getString("LANGUAGE"));
	        			dto.setMessageID(rs.getString("MESSAGEID"));
	        			dto.setVDN(rs.getString("VDN"));
	        			dto.setBargein(rs.getString("BARGEIN"));
	        			dto.setStartTime(rs.getString("STARTTIME"));
	        			dto.setEndTime(rs.getString("ENDTIME"));
	
	        			statusMessageMetaData[i++] = dto;
	
	        		}
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
        			LOGGER.error(new StringBuffer(logToken).append(" StaleConnectionException getAllStatusMsg: Error getting record. ")
        					.append("; exception: ").append(sce.getMessage()));
        			return null;
        		}
        	} //end catch
        	catch (SQLException e) {
        		LOGGER.error(new StringBuffer(logToken).append("SQLException getAllStatusMsg: Error getting record."), e);
        		return null;
        	}//end catch
        	catch (Exception e) {
        		LOGGER.error(new StringBuffer(logToken).append("Exception getAllStatusMsg: Error getting record."), e);
        		return null;
        	}//end catch
        	finally {
        		releaseResource(conn, stmt, rs);
        	}//end finally
        } while (retry);

		return statusMessageMetaData;
	}
	
		
	
	/**
	 * Delete a status message entry
	 * 
	 * @param appName
	 * @param language
	 * @param segmentID
	 * @param VDN
	 * 			
	 * @return true if deleted successfully, false otherwise
	 */
	public boolean deleteStatusMsg(String appName, String language, String segmentID, String VDN) {
	
		Connection conn = null;
		PreparedStatement stmt = null;
		String sql = null;

		// check if the passed values are valid.
		if (appName == null || language == null || segmentID == null)
		{
			LOGGER.error(new StringBuffer(logToken).append("deleteStatusMsg: Error: Null key: [appName = ")
					.append(appName)  
					.append(" language = ")
					.append(language)
					.append(" segmentID = ")
					.append(segmentID)
					.append(" ]"));
			return false;
		}

		if( VDN == null )
			sql = deleteNullSQL;
		else
			sql = deleteSQL;

		boolean retry = false;
        int retryAttempts = 0;

        do {
        	try {
        		retry = false;
        		conn = getConnection();			

        		stmt = conn.prepareStatement(sql);
        		if (queryTimeout > 0) {
    				stmt.setQueryTimeout(queryTimeout);
    			}
        		stmt.setString(1, appName);
        		stmt.setString(2, language);
        		stmt.setString(3, segmentID);
        		if( VDN != null )
        			stmt.setString(4, VDN);

        		if (LOGGER.isDebugEnabled())
        			LOGGER.debug("deleteStatusMsg: SQL: " + stmt.toString());

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
        			LOGGER.error(new StringBuffer(logToken).append(" StaleConnectionException deleteStatusMsg: Error deleting record. ")
        					.append("; exception: ").append(sce.getMessage()));
        			return false;
        		}
        	} //end catch
        	catch (SQLException e) {
        		LOGGER.error(new StringBuffer(logToken).append("SQLException deleteStatusMsg: Error deleting record."), e);
        		return false;
        	}//end catch
        	catch (Exception e) {
        		LOGGER.error(new StringBuffer(logToken).append("Exception deleteStatusMsg: Error deleting record."), e);
        		return false;
        	}//end catch
        	finally {
        		releaseResource(conn, stmt, null);
        	}//end finally
        } while (retry);

		return true;
	}
	
}

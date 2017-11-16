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
import com.ibm.websphere.ce.cm.StaleConnectionException;

import org.apache.log4j.Logger;

import com.ibm.ivr.eus.data.CommonValues;

/**
 * @author Alan McDonley
 * @doc
 * 		The EUSSETDAO class is used to manipulate 
 *   	EUSSETDTO objects each containing setName-KEY-VALUESET information
 *   
 *      setName is used as the table name in the EUSDB - ie IBM_NA_EFH_ALPHA for North American serial numbers
 * 		KEY is the search string that will come out of a telephony "get serial" dialog 
 * 															(with '*' entered for alpha chars)
 * 				Additionally, for all methods
 * 					'_' in the key matches any character in that position
 * 					'%' in the key matches one or more characters 
 * 		VALUESET is a Java StringBuffer Vector filled with one or more DB entries which match the KEY
 * 
 * 		There are three methods:
 * 				<EUSSETDAO>.addtoSET(<EUSSETDTO>)  puts rows into the DB
 * 				<EUSSETDAO>.geteusSET("setName","KEY") retrieve rows from DB
 * 				<EUSSETDAO>.deleteKeyFromSET("setName","KEY") deletes rows from DB
 * 
 */
public class EUSSETDAO extends BaseDAO {
	 /**
	 * Commons logger for <code>logger</code>.
	 */
	private static Logger LOGGER = Logger.getLogger(EUSSETDAO.class);
	private boolean testCall = false;
	private String logToken = null;
	private int queryTimeout = 0;

	public EUSSETDAO(String ds, String callId, boolean testCall, int queryTimeout) {
//		this.dsSource = this.getDataSource(ds);
		this.getDataSource(ds);
		this.testCall = testCall;
		this.queryTimeout = queryTimeout;
		logToken = new StringBuffer("[").append(callId).append("] ").toString();
	}

	/**
	 * Add an key value pair to an set table
	 * 
	 * @param eusvardto
	 *            EUSSETDTO containing setName-KEY-VALUESET to insert into 
	 *            the setName table in the EUSDB
	 * @return true if added successfully, false otherwise
	 */
	public boolean addtoSET(EUSSETDTO eussetdto) {	
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = null;
		String key = eussetdto.getKEY();
		String value = null;
		int index = 0;
		Vector<StringBuffer> strVec = eussetdto.getVALUESET();
		String table = eussetdto.getSetName();
		
		if (key==null || table==null) {
			LOGGER.error(new StringBuffer(logToken).append("Error addtoSET - Null key: [")
					.append(key)  
					.append(" and/or null Table:")
					.append(table)
					.append(" ]"));
			return false;
			
		}
		
		// ok to upcase the table name since not null
		table = table.toUpperCase();
		boolean retry = false;
        int retryAttempts = 0;
        
        do {

        	try {
        		retry = false;
        		conn = getConnection();			
        		sql = "INSERT into "+table+" values (?,?)";
        		stmt = conn.prepareStatement(sql);
        		if (queryTimeout > 0) {
    				stmt.setQueryTimeout(queryTimeout);
    			}
        		stmt.setString(1, eussetdto.getKEY());

        		for (index=0; index<eussetdto.getVALUESET().size();index++) {
        			value = strVec.elementAt(index).toString();
        			stmt.setString(2, value );
        			if (testCall)
        				LOGGER.info(new StringBuffer(logToken).append("Executing:\"")
        						.append(sql).append("\""));
        			stmt.execute();
        			if (testCall)
        				LOGGER.info(new StringBuffer(logToken).append("addtoSet: insert success KEY: ")
        						.append(key).append(" VALUE: ").append(value));

        		}
        	} // end try
        	catch (StaleConnectionException sce) {
        		if (retryAttempts++ < CommonValues.MAX_CONN_RETRIES) {
        			retry = true;
        			if (testCall) {
        				LOGGER.info(new StringBuffer(logToken).append(" StaleConnection - retrying, attempt = ").append(retryAttempts));
        			}
        		} else {
        			retry = false;
        			LOGGER.error(new StringBuffer(logToken).append(" StaleConnectionException addtoSET: ")
        					.append(table).append(" KEY: [").append(key)
            				.append("(").append(index).append("),")
            				.append(value).append(" ]")
        					.append("; exception: ").append(sce.getMessage()));
        			return false;
        		}

        	} //end catch
        	catch (SQLException e) {
        		LOGGER.error(new StringBuffer(logToken).append("SQLException addtoSET: ")
        				.append(table)
        				.append(" KEY: [").append(key)
        				.append("(").append(index).append("),")
        				.append(value).append(" ]"), e);
        		return false;
        	} //end catch
        	catch (Exception e) {
        		LOGGER.error(new StringBuffer(logToken).append("Exception addtoSET: ")
        				.append(table)
        				.append(" KEY: [").append(key)
        				.append("(").append(index).append("),")
        				.append(value).append(" ]"), e);
        		return false;
        	}//end catch
        	finally {
        		releaseResource(conn, stmt, rs);
        	}//end finally
        } while(retry);
		
		return true;
	}

	
	/**
	 * Get a set from DB
	 * 
	 * @param setName
	 * 			table name ie. IBM_NA_EFH_ALPHA
	 * @param key
	 *            KEY for set to retrieve 
	 *            
	 * @return EUSSETDTO object, null if not found
	 */
	public EUSSETDTO geteusSET(String setName, String key) {
		EUSSETDTO dto = new EUSSETDTO(); 
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = null;
		
		boolean isResultSetEmpty = true;

		if (setName==null || key==null) {
			LOGGER.error(new StringBuffer(logToken).append("Error geteusSET - Null key: [")
					.append(key)  
					.append(" and/or null Table:")
					.append(setName)
					.append(" ]"));
			return null;
			
		}
		dto.setSetName(setName.toUpperCase());
		dto.setKEY(key);

		boolean retry = false;
        int retryAttempts = 0;
        
        do {
        	try {
        		retry = false;
        		conn = getConnection();			
        		sql = "SELECT VALUE FROM " + setName.toUpperCase() 
        		+ " WHERE KEY LIKE '" + key + "%'";
        		if (testCall)
        			LOGGER.info(new StringBuffer(logToken).append("Executing:\"")
        					.append(sql).append("\""));
        		stmt = conn.prepareStatement(sql);
        		if (queryTimeout > 0) {
    				stmt.setQueryTimeout(queryTimeout);
    			}
        		rs = stmt.executeQuery();
        		Vector<StringBuffer> rset= new Vector<StringBuffer>();
        		while ( rs.next() ) {
        			isResultSetEmpty = false;
        			rset.add(new StringBuffer(rs.getString(1)));
        		}//end while
        		dto.setVALUESET(rset);
        	} //end try
        	catch (StaleConnectionException sce) {
        		if (retryAttempts++ < CommonValues.MAX_CONN_RETRIES) {
        			retry = true;
        			if (testCall) {
        				LOGGER.info(new StringBuffer(logToken).append(" StaleConnection - retrying, attempt = ").append(retryAttempts));
        			}
        		} else {
        			retry = false;
        			LOGGER.error(new StringBuffer(logToken).append(" StaleConnectionException getting setName: ")
        					.append(setName).append(", key = ").append(key)
        					.append("; exception: ").append(sce.getMessage()));
        			return null;
        		}
        	} //end catch
        	catch (SQLException e) {
        		LOGGER.error(new StringBuffer(logToken).append("SQLException getting setName = ")
        				.append(setName).append(", key = ").append(key), e);
        		return null;
        	} //end catch
        	catch (Exception e) {
        		LOGGER.error(new StringBuffer(logToken).append("Exception getting setName = ")
        				.append(setName).append(", key = ").append(key), e);
        		return null;
        	}//end catch
        	finally {
        		releaseResource(conn, stmt, rs);
        	}//end finally
        } while(retry);

		if (isResultSetEmpty){
			LOGGER.warn(new StringBuffer(logToken).append("No rows found for setName = ")
					.append(setName).append(", key = ").append(key));
			return null;
		}
		else return dto;
	}

	// GMC - 06/14/12 - Change related to ticket IN2425536
	public EUSSETDTO geteusSETMC(String setName, String key) {
		EUSSETDTO dto = new EUSSETDTO(); 
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = null;
		
		boolean isResultSetEmpty = true;

		if (setName==null || key==null) {
			LOGGER.error(new StringBuffer(logToken).append("Error geteusSET - Null key: [")
					.append(key)  
					.append(" and/or null Table:")
					.append(setName)
					.append(" ]"));
			return null;
			
		}
		dto.setSetName(setName.toUpperCase());
		dto.setKEY(key);

		boolean retry = false;
        int retryAttempts = 0;
        
        do {
        	try {
        		retry = false;
        		conn = getConnection();			
        		sql = "SELECT VALUE FROM " + setName.toUpperCase() 
        		+ " WHERE KEY = '" + key + "'";
        		if (testCall)
        			LOGGER.info(new StringBuffer(logToken).append("Executing:\"")
        					.append(sql).append("\""));
        		stmt = conn.prepareStatement(sql);
        		if (queryTimeout > 0) {
    				stmt.setQueryTimeout(queryTimeout);
    			}
        		rs = stmt.executeQuery();
        		Vector<StringBuffer> rset= new Vector<StringBuffer>();
        		while ( rs.next() ) {
        			isResultSetEmpty = false;
        			rset.add(new StringBuffer(rs.getString(1)));
        		}//end while
        		dto.setVALUESET(rset);
        	} //end try
        	catch (StaleConnectionException sce) {
        		if (retryAttempts++ < CommonValues.MAX_CONN_RETRIES) {
        			retry = true;
        			if (testCall) {
        				LOGGER.info(new StringBuffer(logToken).append(" StaleConnection - retrying, attempt = ").append(retryAttempts));
        			}
        		} else {
        			retry = false;
        			LOGGER.error(new StringBuffer(logToken).append(" StaleConnectionException getting setName: ")
        					.append(setName).append(", key = ").append(key)
        					.append("; exception: ").append(sce.getMessage()));
        			return null;
        		}
        	} //end catch
        	catch (SQLException e) {
        		LOGGER.error(new StringBuffer(logToken).append("SQLException getting setName = ")
        				.append(setName).append(", key = ").append(key), e);
        		return null;
        	} //end catch
        	catch (Exception e) {
        		LOGGER.error(new StringBuffer(logToken).append("Exception getting setName = ")
        				.append(setName).append(", key = ").append(key), e);
        		return null;
        	}//end catch
        	finally {
        		releaseResource(conn, stmt, rs);
        	}//end finally
        } while(retry);

		if (isResultSetEmpty){
			LOGGER.warn(new StringBuffer(logToken).append("No rows found for setName = ")
					.append(setName).append(", key = ").append(key));
			return null;
		}
		else return dto;
	}
	
	/**
	 * Delete all rows with key from setName
	 * 
	 * @param setName
	 * @param key
	 * 			
	 * @return true if deleted successfully, false otherwise
	 */
	public boolean deleteKeyFromSET(String setName, String key) {
	
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String sql = null;

		if (setName==null || key==null) {
			LOGGER.error(new StringBuffer(logToken).append("Error deleteeusSET - Null key: [")
					.append(key)  
					.append(" and/or null Table:")
					.append(setName)
					.append(" ]"));
			return false;
			
		}
		boolean retry = false;
        int retryAttempts = 0;

        do {
        	try {
        		retry = false;
        		conn = getConnection();			
        		sql = "DELETE FROM " + setName
        		+ " WHERE KEY LIKE '" + key +"'";
        		if (testCall)
        			LOGGER.info(new StringBuffer(logToken).append("Executing:\"")
        					.append(sql).append("\""));
        		stmt = conn.prepareStatement(sql);
        		if (queryTimeout > 0) {
    				stmt.setQueryTimeout(queryTimeout);
    			}
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
        					.append(key).append(" for setName:").append(setName)
        					.append("; exception: ").append(sce.getMessage()));
        			return false;
        		}
        	} //end catch
        	catch (SQLException e) {
        		LOGGER.error(new StringBuffer(logToken).append("SQLException deleting key = ")
        				.append(key).append(" for setName:").append(setName), e);
        		return false;
        	}//end catch
        	catch (Exception e) {
        		LOGGER.error(new StringBuffer(logToken).append("Exception deleting key = ")
        				.append(key).append(" for setName:").append(setName), e);
        		return false;
        	}//end catch
        	finally {
        		releaseResource(conn, stmt, rs);
        	}//end finally

        } while (retry);

		return true;
	}
	
}

package com.ibm.ivr.eus.dao;

//jdbc imports
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import javax.naming.InitialContext;
import javax.sql.DataSource;

//logging imports
import org.apache.log4j.Logger;

/**
* This is the base class for all DAO objects. Contains generic operations used
* by all DAO objects.
* 
* @author Kapil
*/
class BaseDAO {
	/**
	 * Commons logger for <code>logger</code>.
	 */
	private static Logger LOGGER = Logger.getLogger(BaseDAO.class);
	/**
	 * DataSource holder <code>dsSource</code>.
	 */
	//	public DataSource dsSource = null;
	private static DataSource dsSource = null;

	 /**
	 * @return DataSource javax.sql.DataSource
	 */
	public DataSource getDataSource(String ds) {
		
		if (dsSource == null) {
			try {
				InitialContext ic = new InitialContext();
				dsSource = (DataSource) ic.lookup(ds);
			} catch (Exception e) {
				LOGGER.error(e.getMessage());
			}
		}

		return dsSource;
	}
	
	/**
	 * Returns a connection to DB
	 * 
	 * @return DB connection object
	 * @throws SQLException if the connection to the DB fails
	 */
	public final Connection getConnection() throws SQLException {
//		DataSource ds = getDataSource();
		
		if (dsSource == null) {
			throw new SQLException("DB DataSource is null, it may not be defined - ");
		}//end if
		
		return dsSource.getConnection();
	}//end method getConnection
	
	/**
	 * Closes the given DB resources
	 * 
	 * @param conn DB connection object
	 * @param stmt Statement object
	 * @param rs ResultSet object
	 */
	public final void releaseResource(final Connection conn, final Statement stmt, final ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				LOGGER.warn("Problem closing ResultSet. Resources might not be released", e);
			}//end catch
		}//end if
		
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				LOGGER.warn("Problem closing PreparedStatement. Resources might not be released", e);
			}//end catch
		}//end if
		
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				LOGGER.warn("Problem closing Connection. Resources might not be released", e);
			}//end catch
		}//end if				
	}//end method releaseResource
	
}//end class BaseDAO

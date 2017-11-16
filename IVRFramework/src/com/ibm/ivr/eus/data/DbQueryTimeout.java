package com.ibm.ivr.eus.data;

import java.util.Properties;

public class DbQueryTimeout {

	public static int getDbQueryTimout(Properties prop) {
		
		int queryTimeout = 0;
		if (prop != null) {
			String queryTimeoutStr = prop.getProperty(CommonValues.DB_QUERY_TIMEOUT_PROPERTY);
	    	if (queryTimeoutStr != null) {
	    		try {
	    			queryTimeout = Integer.parseInt(queryTimeoutStr);
	    		}
	    		catch (NumberFormatException e) {
	    			queryTimeout = 0;
	    		}	
	    	}
		}
		
		return queryTimeout;
	}
}

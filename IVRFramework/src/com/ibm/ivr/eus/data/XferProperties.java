package com.ibm.ivr.eus.data;

import java.util.Properties;

public class XferProperties {
	private long timestamp = -1;
	private Properties properties = null;
	/**
	 * @return the timestamp
	 */
	public final long getTimestamp() {
		return timestamp;
	}
	/**
	 * @param timestamp the timestamp to set
	 */
	public final void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	/**
	 * @return the properties
	 */
	public final Properties getProperties() {
		return properties;
	}
	/**
	 * @param properties the properties to set
	 */
	public final void setProperties(Properties properties) {
		this.properties = properties;
	}

}

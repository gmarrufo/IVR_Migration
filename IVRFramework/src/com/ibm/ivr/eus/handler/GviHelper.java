package com.ibm.ivr.eus.handler;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.apache.log4j.Logger;

public class GviHelper {
	private final static String DELIM = "\\|";
	private static Logger LOGGER = Logger.getLogger(GviHelper.class);

	public static String getGviLocation(String gviFilename, String phoneNumber, boolean testCall, String logToken) {

		BufferedReader br = null;
		FileReader fr = null;
		Closeable resource = null;
		String gviLocationCode = null;
		try {
			fr = new FileReader(gviFilename);
			resource = fr;
			br = new BufferedReader(fr);
			resource = br;
			String strLine;
			String lowerStr = null;
			String upperStr = null;

			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
				if (strLine != null) {
					strLine = strLine.trim();
					// skip blank lines
					if (strLine.length() == 0)
						continue;
					// each line should have 4 tokens separated by '|'
					String[] tokens = strLine.split(DELIM);
					if (tokens.length >= 3 && tokens[1] != null && tokens[2] != null) {

						lowerStr = tokens[1];
						upperStr = tokens[2];

						if (phoneNumber.compareTo(lowerStr) >= 0 && phoneNumber.compareTo(upperStr) <= 0) {
							if (testCall)
								LOGGER.debug(new StringBuffer(logToken).append("found phone number between ")
										.append(lowerStr).append(" and ").append(upperStr));
							if (tokens.length >= 4) {
								gviLocationCode = tokens[3];
							}
							break;
						}

					}
				}
			}
			//Close the input stream
			resource.close();
			resource = null;
		} catch (FileNotFoundException e){//Catch file exception if any
			if (testCall)
				LOGGER.error(new StringBuffer(logToken).append("Error opening file: gviFilename; ").append(e.getMessage()));
		} catch (Exception e){//Catch any other exceptions
			if (testCall)
				LOGGER.error(new StringBuffer(logToken).append("Exception: ").append(e.getMessage()));
		} finally {
			if (resource != null) {
				try {
					resource.close();
				} catch (Exception e) {

				}
			}
		}
		if (testCall)
			LOGGER.debug(new StringBuffer(logToken).append("gviLocationCode = ").append(gviLocationCode));

		return gviLocationCode;

	}

}

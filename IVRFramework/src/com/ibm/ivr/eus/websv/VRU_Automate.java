/**
 * 
 */
package com.ibm.ivr.eus.websv;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.CharBuffer;
import org.apache.log4j.Logger;
import com.ibm.ivr.eus.dao.TestEUSSETservlet;

/**
 * @author Alan McDonley
 * 
 */
public class VRU_Automate {
	private String logToken = null;
	private static Logger LOGGER = Logger.getLogger(VRU_Automate.class);

	/**
	 * @param strHostName
	 * @param iPort
	 * @param strRequest
	 *            ie: A=RSAUSA,M=CTI,D=rsa2.dll,F=RSA,S=ssssss,P=ccc ie:
	 *            A=RSACAN,M=CTI,D=rsa2.dll,F=RSA,S=ssssss,P=ccc RSAxxx is
	 *            appropriate to country ssssss is serial number, ccc is country
	 *            code
	 * @param iTimeOutSeconds
	 *            max amount of time in seconds to wait for a response from
	 *            server after successful connect and after strRequest has been
	 *            sent.
	 * @return
	 */
	public static String vru_automate(String strHostName, int iPort,
			String strRequest, int iTimeOutSeconds) {
		CharBuffer cBuffer = CharBuffer.allocate(100);
		String strResponse = null;
		String automation_rc = null;
		int nCharsRead = -1;
		int readTimeout = 2; // seconds
		int connectTimeout = 10; // seconds

		// GMC - Change related to ticket IN3512974
		Socket skt = null;

		try {
			LOGGER.debug("vru_automate(): connecting to " + strHostName + ":"
					+ iPort);

			// GMC - Change related to ticket IN3512974
			skt = TimedGetSocket.timedGetSocket(strHostName, iPort,
					connectTimeout * 1000);

			// set socket read timeout to 10s
			skt.setSoTimeout(readTimeout * 1000);

			// got connection - send request
			LOGGER.debug("vru_automate() connection established to "
					+ skt.toString());

			// set up socket reader and writer
			PrintStream sktOutput = new PrintStream(skt.getOutputStream());
			InputStreamReader sktReader = new InputStreamReader(skt
					.getInputStream());

			// send request to server
			sktOutput.print(strRequest);
			LOGGER.debug("vru_automate(): sent request:" + strRequest);
			LOGGER.debug("vru_automate(): will wait " + iTimeOutSeconds
					+ "s maximum for response");

			// get server response
			int waited = 0;

			while (waited < iTimeOutSeconds) {
				try {
					nCharsRead = sktReader.read(cBuffer);

				} catch (InterruptedIOException ex) {
					waited = waited + readTimeout;
					LOGGER.debug("vru_automate() waited " + waited + " sec");
				}
				if (nCharsRead > 0)
					break;
			}

			LOGGER.debug("vru_automate():" + nCharsRead + " char response");
			if (nCharsRead > 0) {
				strResponse = cBuffer.flip().toString();
				LOGGER.debug("vru_automate(): response from server:"
						+ strResponse);
				/*
				 * int iRC=strResponse.indexOf("RC="); int
				 * iComma=strResponse.indexOf(","); if (iRC==0) {
				 * automation_rc=strResponse.substring(iRC+3, iComma); }
				 * LOGGER.debug("vru_automate(): automation_rc:"+automation_rc);
				 * } else { automation_rc="-1";
				 */
			}

			// close the connection to server
			skt.close();
			LOGGER.debug("vru_automate() exiting");
		} catch (Exception ex) {
			LOGGER.debug("vru_automate() exception" + ex);
		} finally {
			// GMC - Change related to ticket IN3512974
			try {
				if (skt != null) {
					skt.close();
				}
			} catch (Exception e) {
				// do nothing
			}
		}
		return strResponse;
	}
}
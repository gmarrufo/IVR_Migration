/**
 * 
 */
package com.ibm.ivr.eus.websv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.CharBuffer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;


/**
 * @author Alan McDonley
 *
 */
public class Socket_Connection {
	private static Logger LOGGER = Logger.getLogger(com.ibm.ivr.eus.websv.Socket_Connection.class);

	/**
	 * @param strHostName  
	 * @param iPort
	 * @param strRequest
	 * 			ie: A=RSAUSA,M=CTI,D=rsa2.dll,F=RSA,S=ssssss,P=ccc
	 * 			ie: A=RSACAN,M=CTI,D=rsa2.dll,F=RSA,S=ssssss,P=ccc
	 * 			RSAxxx is appropriate to country
	 * 			ssssss is serial number, 
	 * 			ccc is country code
	 * @param iTimeOutSeconds
	 * 			max amount of time in seconds to wait for a response from server
	 * 			after successful connect and after strRequest has been sent.
	 * @return strResponse
	 * 			response string received from server
	 */
	public String vru_automate(String strHostName, int iPort, String strRequest, int iTimeOutSeconds, boolean testCall, String callid) {
		CharBuffer cBuffer = CharBuffer.allocate(1024);
		String strResponse = null;
		int nCharsRead = -1;
		int readTimeout = iTimeOutSeconds * 1000; // convert to milliseconds
		int connectTimeout = 10 * 1000; // convert to milliseconds
		Socket skt = null;
		PrintStream sktOutput = null;
		BufferedReader sktReader = null;

		String logToken = new StringBuffer("[").append(callid).append("] ").toString();

		try {
			if (testCall)
				LOGGER.info(new StringBuffer(logToken).append("vru_automate(): connecting to ").append(strHostName)
						.append(":").append(iPort));

			skt = new Socket();

			InetAddress inetAddr = InetAddress.getByName(strHostName);
			SocketAddress sockaddr = new InetSocketAddress(inetAddr, iPort);

			// connect to server - timeout if not connected within specified time
			skt.connect(sockaddr, connectTimeout);
			
			// set socket read timeout to value passed as parameter
			skt.setSoTimeout(readTimeout);
			
			// got connection - send request
			if (testCall) {
				LOGGER.info(new StringBuffer(logToken).append("vru_automate() connection established to ").append(skt.toString()));
				LOGGER.info(new StringBuffer(logToken).append("vru_automate() SoTimeout value: ").append(skt.getSoTimeout()));
			}

			// set up socket reader and writer
			sktOutput = new PrintStream(skt.getOutputStream());
			sktReader = new BufferedReader(new InputStreamReader(skt.getInputStream()));

			// ensure that a null request is not sent
			if (strRequest == null) {
				strRequest = "";
			}
			// send request to server
			sktOutput.print(strRequest);
			sktOutput.flush();
			if (testCall) {
				LOGGER.info(new StringBuffer(logToken).append("vru_automate(): sent request:").append(strRequest));
				LOGGER.info(new StringBuffer(logToken).append("vru_automate(): will wait ")
						.append(iTimeOutSeconds).append("s maximum for response"));
			}
			
			nCharsRead = sktReader.read(cBuffer);
			if (nCharsRead > 0) {
				strResponse = cBuffer.flip().toString();
				if (testCall)
					LOGGER.info(new StringBuffer(logToken).append("vru_automate(): read ").append(nCharsRead)
							.append(" characters: ").append(strResponse));
			}

			if (testCall)
				LOGGER.info(new StringBuffer(logToken).append("vru_automate(): response from server: ").append(strResponse));

			// close i/o streams
			sktOutput.close();
			sktOutput = null;
			sktReader.close();
			sktReader = null;
			// close the connection to server
			skt.close();
			skt = null;
			if (testCall)
				LOGGER.info(new StringBuffer(logToken).append("vru_automate() exiting"));

		} catch (UnknownHostException e) {
			LOGGER.error(new StringBuffer(logToken).append("vru_automate() UnknownHostException: ").append(e.getMessage()));
		} catch (SocketTimeoutException e) {
			LOGGER.error(new StringBuffer(logToken).append("vru_automate() SocketTimeoutException: ").append(e.getMessage()));
		} catch (SocketException e) {
			LOGGER.error(new StringBuffer(logToken).append("vru_automate() SocketException: ").append(e.getMessage()));
		} catch (IOException e) {
			LOGGER.error(new StringBuffer(logToken).append("vru_automate() IOException: ").append(e.getMessage()));
		} catch (Exception e) {
			LOGGER.error(new StringBuffer(logToken).append("vru_automate() Exception: ").append(e.getMessage()));

		} finally {
			try {
				if (sktOutput != null) {
					sktOutput.close();
				}
				if (sktReader != null) {
					sktReader.close();
				}
				if (skt != null) {
					skt.close();
				}
			} catch(Exception e) {
				// do nothing
			}
		}
		return strResponse;
	}

}

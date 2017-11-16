package com.ibm.ivr.eus.websv;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.Header;

import org.apache.log4j.Logger;

import java.io.IOException;
import com.ibm.ivr.eus.handler.VT_Request_Data;
import com.ibm.ivr.eus.handler.VT_Response_Data;

public class VT_Server_Query {
	
	private static Logger LOGGER = Logger.getLogger(VT_Server_Query.class);
	private int coTimeout = 0;
	private int soTimeout = 0;
	private String callid = null;
	private boolean testCall = false;
	private HttpClient httpclient = null;
	private String logToken = null;
	private PostMethod httpPost = null;

	public VT_Server_Query(String connectionTimeout, String socketTimeout, String callid, boolean testcall) {
		
		super();
		this.coTimeout = new Integer(connectionTimeout).intValue();
		this.soTimeout = new Integer(socketTimeout).intValue();
		this.callid = callid;
		this.testCall = testcall;
		logToken = new StringBuffer("[").append(callid).append("] ").toString();
	}
	
	public VT_Response_Data sendRequest(VT_Request_Data vtreq) {
		
		VT_Response_Data vtresp = null;
		String automationData = null;
		
		httpclient = new HttpClient();
		httpPost = new PostMethod();
		String vtOperation = vtreq.getOperationType();
		if (vtOperation.equalsIgnoreCase("Update") || vtOperation.equalsIgnoreCase("QueryStatus")) {
			httpPost.setRequestHeader("OperationType", vtreq.getOperationType());
			httpPost.setRequestHeader("IBMVRUNumber", "VRU:" + vtreq.getVruNumber());
			httpPost.setRequestHeader("SerialNumber", "SN:" + vtreq.getSerialNumber());
			httpPost.setRequestHeader("Tenant", vtreq.getTenant());
			if (vtreq.getLanguage() != null) {
				httpPost.setRequestHeader("Language", vtreq.getLanguage());
			} else {
				httpPost.setRequestHeader("Language", "en-US");
			}
		
			vtresp = postRequest(vtreq.getPrimaryURL(), vtreq.getSerialNumber());
			
			if (vtresp == null) {
				automationData = "RC=99|Communication Failure";
				LOGGER.error(new StringBuffer(logToken).append("Primary server failed for serial number: ")
						.append(vtreq.getSerialNumber()).append("trying backup server"));
				vtresp = postRequest(vtreq.getBackupURL(), vtreq.getSerialNumber());
			}
		} else {
			vtresp = new VT_Response_Data();
			vtresp.setRetCode(99);
			vtresp.setRetStatus("No VT_Operation Defined");
			vtresp.setRetData("RC=99|No VT_Operation Defined");
		}
		// set vtresp object if not already set
		if (vtresp != null) {
			automationData = "RC=" + + vtresp.getRetCode() + "|" + vtresp.getRetStatus();
			vtresp.setRetData(automationData);
		} else {
			vtresp = new VT_Response_Data();
			vtresp.setRetCode(97);
			vtresp.setRetStatus("VT did not Return Headers");
			vtresp.setRetData("RC=97|VT did not Return Headers");
		}
		
		return vtresp;
		
	}
	
	private VT_Response_Data postRequest(String url, String serialNumber) {
		VT_Response_Data vtresp = null;
		
		try {
			httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(coTimeout);
			httpclient.getHttpConnectionManager().getParams().setSoTimeout(soTimeout);
			httpPost.setURI(new URI(url, false));

			Header tempHdr = null;
			String retCodeStr = null;
			int retCode = -1;
			
			httpclient.executeMethod(httpPost);
			if (httpPost.getStatusCode() == 200) {
				// get return code
				vtresp = new VT_Response_Data();
				tempHdr = httpPost.getResponseHeader("RetCode");
				if (tempHdr != null) {
					retCodeStr = tempHdr.getValue();
					if (retCodeStr != null) {
						retCode = new Integer(retCodeStr).intValue();
						vtresp.setRetCode(retCode);
					}
				}
				// get return status
				tempHdr = httpPost.getResponseHeader("RetStatus");
				if (tempHdr != null) {
					vtresp.setRetStatus(tempHdr.getValue());
				}
				
				// get enrollment status
				tempHdr = httpPost.getResponseHeader("EnrollStatus");
				if (tempHdr != null) {
					vtresp.setEnrollStatus(tempHdr.getValue());
				}
				
				// get user info
				tempHdr = httpPost.getResponseHeader("UserInfo");
				if (tempHdr != null) {
					vtresp.setUserInfo(tempHdr.getValue());
				}
			}

		}
		catch(HttpException e) {
			LOGGER.error(new StringBuffer(logToken).append(" - HttpException in VT_Server_Query transaction for serial number: ")
					.append(serialNumber).append("; exception: ").append(e.getMessage()));
		}
		catch(IOException e) {
			LOGGER.error(new StringBuffer(logToken).append(" - IOException in VT_Server_Query transaction for serial number: ")
					.append(serialNumber).append("; exception: ").append(e.getMessage()));
		}
		catch(Exception e) {
			LOGGER.error(new StringBuffer(logToken).append(" - Exception in VT_Server_Query transaction for serial number: ")
					.append(serialNumber).append("; exception: ").append(e.getMessage()));
		}
		finally {
			httpPost.releaseConnection();
		}
		
		return vtresp;
	}

}

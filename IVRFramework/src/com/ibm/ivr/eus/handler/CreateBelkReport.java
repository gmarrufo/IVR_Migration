package com.ibm.ivr.eus.handler;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Properties;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.xpath.XPathConstants;

import com.ibm.ivr.eus.websrv.belk.EUSTTBelkService;
import com.ibm.ivr.eus.websrv.belk.EUSTTBelk;
import com.ibm.ivr.eus.websrv.belk.EUSBelkImpl;

import org.apache.log4j.Logger;

import com.ibm.ivr.eus.data.XferProperties;

import com.ibm.ivr.eus.common.LFileFunctions;
import com.ibm.ivr.eus.common.XPathReader;

public class CreateBelkReport extends HttpServlet implements Servlet {

	private static final long serialVersionUID 					= 1L;
	private static Logger LOGGER 								= Logger.getLogger(CreateBelkReport.class);
	private static HashMap<String,String> call_values			=new HashMap<String,String>();
	private static String REPLACE_PRINTER_NAME					="REPLACE-PRINTERNAME";
	private static String REPLACE_REPORT_TYPE					="REPLACE-REPORTTYPE";

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    	
		// get session from Servlet request
		HttpSession session = request.getSession(false);
		initCallValues(session);
		
		if((call_values.get("servlet_flag").toString()).equals("1")){
			processEUSTTSOAP_post(request,response);
			return;
		}
		String callid = call_values.get("callid");
		boolean testCall = Boolean.getBoolean(call_values.get("testCall"));

		//create the log Token for later use, use StringBuffer to reduce number of String objects
		String logToken = new StringBuffer("[").append(callid).append("] ").toString();
		if (testCall)
			LOGGER.info(new StringBuffer(logToken).append("Entering CreateBelkReport Handler"));
		
		String report_url=(String)call_values.get("report_url");
		String printer_name=(String)call_values.get("printer_name");
		String report_type=(String)call_values.get("report_type");
		
		if(!isValidURL()){
			return;
		}
		
		long starttime=System.currentTimeMillis();
		EUSBelkImpl impl=new EUSBelkImpl();
		String result=impl.xPrintOverNightReports(printer_name, report_type);
		
		long endtime=System.currentTimeMillis();
		long elapsed=endtime - starttime;
		
		call_values.put("automation_data", result);
		call_values.put("automation_rc", "0");		   		     

		if(testCall){
			LOGGER.info(new StringBuffer(logToken).append("Result from WebService:").append(result));
			LOGGER.info(new StringBuffer(logToken).append("Elapsed Time for WebService:").append(elapsed));
		}
		
		String response_XMLFile=(String)call_values.get("response_XMLFile");
		if(result != null && result.length() > 0){
			LFileFunctions.writeToFile(logToken, response_XMLFile, result, false);
		}
		session.setAttribute("printer_response", result);
		session.setAttribute("automation_data", call_values.get("automation_data"));
		session.setAttribute("automation_rc", call_values.get("automation_rc"));
		return;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /*****************************************************************************
     *                             - EUSTTSOAP_post -                            *
     * This routine is for the new EUS Tools Team Web Service Survey Solution.   *
     * This routine processed the soap XML file Built previously and processes   *
     * the xml file via a webservice. This routine then parses out the returned  *
     * values of the XML to determin success or failure.                         *
     *---------------------------------------------------------------------------*
     * Return Codes:                                                             *
     *     0 - Successfully Processed request                                    *
     *    99 - Returned from WEBCONNECT automation_URL was not set or passed     *
     *    98 - Returned from WEBCONNECT automation_type was not set or passed    *
     *    97 - Returned from WEBCONNECT Timed out trying to Communicate to URL   *
     *    96 - Returned from WEBCONNECT XML file doesn't exist                   *
     *    94 - Generated here EUSTTSurvey_post unkown status from webservice     *
     *    93 - caller hung up during submission                                  * 
     *  (From web service denoting failure to process)                           *
     *  5300 - 5319 -> EUS Survey Codes                                          *
     *  26011 - 26220 -> Belk Automation                                         *
     *****************************************************************************/   
    protected void processEUSTTSOAP_post(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
 
		// get session from Servlet request
		HttpSession session = request.getSession(true);
		initCallValues(session);
		
		String callid = (String) call_values.get("callid");
		boolean testCall = Boolean.parseBoolean(call_values.get("testCall"));

		//create the log Token for later use, use StringBuffer to reduce number of String objects
		String logToken = new StringBuffer("[").append(callid).append("] ").toString();
		if (testCall)
			LOGGER.info(new StringBuffer(logToken).append("Entering CreateBelkReport Handler"));
		
		if(isValidURL()){
			callWebService();
		}
		
		if(testCall){
			LOGGER.debug(new StringBuffer(logToken).append("Returning automation_rc==" + call_values.get("automation_rc")));
			LOGGER.debug(new StringBuffer(logToken).append("Returning automation_data==" + call_values.get("automation_data")));
			LOGGER.debug(new StringBuffer(logToken).append("Returning automation_status==" + call_values.get("automation_status")));
		}
		
		session.setAttribute("automation_rc", call_values.get("automation_rc"));
		session.setAttribute("automation_data", call_values.get("automation_data"));
		session.setAttribute("automation_status", call_values.get("automation_status"));
    }
    
    /**
     * InitCallValues - initializes the HashMap with the values from the callflow
     * @param session
     */
    private void initCallValues(HttpSession session){
    	
    	Object tempValue			="";
		String report_url			="";
		String printer_name			="";
		String report_type			="";
		String automation_url		="";
		String automation_type		="";
		String automation_data		="";
		String soap_XMLFile			="";
		String response_XMLFile		="";
		String automation_rc		="";
		String callid 				="";
		String testCall				="";
		String xmlTemplateFile		="";
		String automation_timeout	="";
		String servlet_flag			="";
		String automation_status_path="";
		String automation_status	="";
		Boolean myTestCall			=false;
    	try{
    		tempValue=session.getAttribute("report_url");
    		if(tempValue != null)
    			report_url=tempValue.toString();
    		
    		tempValue=session.getAttribute("printer_name");
    		if(tempValue != null)
    			printer_name=tempValue.toString();
    		
    		tempValue=session.getAttribute("report_type");
    		if(tempValue != null)
    			report_type=tempValue.toString();
    		
    		tempValue=session.getAttribute("automation_url");
    		if(tempValue != null)
    			automation_url=tempValue.toString();
    		
    		tempValue=session.getAttribute("automation_type");
    		if(tempValue != null)
    			automation_type=tempValue.toString();
    		
    		tempValue=session.getAttribute("automation_data");
    		if(tempValue != null)
    			automation_data=tempValue.toString();    		

    		tempValue=session.getAttribute("soap_XMLFile");
    		if(tempValue != null)
    			soap_XMLFile=tempValue.toString();
    		
    		tempValue=session.getAttribute("response_XMLFile");
    		if(tempValue != null)
    			response_XMLFile=tempValue.toString();
    		
    		tempValue=session.getAttribute("automation_rc");
    		if(tempValue != null)
    			automation_rc=tempValue.toString();
    		
    		tempValue=session.getAttribute("callid");
    		if(tempValue != null)
    			callid=tempValue.toString();
    		
    		tempValue=session.getAttribute("testCall");
    		if(tempValue != null){
    			testCall=tempValue.toString();
    			myTestCall=Boolean.parseBoolean(testCall);
    		}
    		
    		tempValue=session.getAttribute("xml_report_template");
    		if(tempValue != null)
    			xmlTemplateFile=tempValue.toString();
    		
    		tempValue=session.getAttribute("automation_timeout");
    		if(tempValue != null)
    			automation_timeout=tempValue.toString();
    		
    		tempValue=session.getAttribute("servlet_flag");
    		if(tempValue != null)
    			servlet_flag=tempValue.toString();
    		    		
    		tempValue=session.getAttribute("automation_status_path");
    		if(tempValue != null)
    			automation_status_path=tempValue.toString();
    		
    		tempValue=session.getAttribute("automation_status");
    		if(tempValue != null)
    			automation_status=tempValue.toString();
    		
    		// Now put into HashMap
    		call_values.clear();
    		call_values.put("testCall"          , testCall);
    		if(myTestCall)
    			LOGGER.debug("Added testCall with a value of "+ testCall);
    		
    		call_values.put("report_url"		, report_url);
    		if(myTestCall)
    			LOGGER.debug("Added report_url with a value of "+report_url);
    		
    		call_values.put("printer_name"		, printer_name);
    		call_values.put("report_type"		, report_type);
    		call_values.put("automation_url"	, automation_url);
    		call_values.put("automation_type"	, automation_type);
    		call_values.put("automation_data"	, automation_data);
    		call_values.put("soap_XMLFile"		, soap_XMLFile);
    		call_values.put("response_XMLFile"	, response_XMLFile);
    		call_values.put("automation_rc"		, automation_rc);
    		call_values.put("callid"            , callid);
    		call_values.put("xml_report_template", xmlTemplateFile);
    		call_values.put("automation_timeout", automation_timeout);
    		call_values.put("servlet_flag"		, servlet_flag);
    		call_values.put("automation_status_path", automation_status_path);
       		call_values.put("automation_status"	, automation_status);
       		
    		if(!testCall.equals("")){
    			myTestCall=Boolean.parseBoolean(testCall);
    			if(myTestCall){
    				StringBuffer logToken=new StringBuffer();
    				java.util.Collection collection=call_values.values();
    				java.util.Iterator iterator=collection.iterator();
    				while(iterator.hasNext()){
    					LOGGER.debug(new StringBuffer(logToken).append("["+call_values.get("callid")).append("]").append("Value in call_values ").append(iterator.next()));
    				}
    			}
    		}
    	}catch(Exception e){
    		LOGGER.error("Error Initializing call values Hash Map:", e);
    	}
    	
    	return;
    }
   
    /**
     * isValidURL - checks for a valid URL
     * @return
     */
    private boolean isValidURL(){
    	boolean valid=true;
    	
		Object automation_url=call_values.get("automation_url");
		Object automation_type=call_values.get("automation_type");
		
		if((automation_url == null) || automation_url.toString().equals("")){
			call_values.put("automation_data", "RC=99|AUTOMATION_URL not passed");
			call_values.put("automation_rc", "99");
			return false;
		}
		
		if((automation_type == null) || automation_type.toString().equals("")){
			call_values.put("automation_data", "RC=98|AUOTMATION_TYPE not passed");
			call_values.put("automation_rc", "98");			
			return false;
		}		
		
    	return valid;
    }
    
    /**
     * callWebService - Calls the web service at the "automation_url".
     */
    private void callWebService(){
       	String soapMessage="";
    	String soapAction="";
    	StringBuffer outputString=new StringBuffer();
    	String responseString="";
    	HttpURLConnection httpConn=null;
    	InputStreamReader isr=null;
    	BufferedReader in=null;
    	OutputStream out=null;
    	OutputStreamWriter br=null;
    	int timeoutValue=30;
    	String callid="";
    	String logToken="";
    	String response_file="";
    	try{
			long start=System.currentTimeMillis();
    		String automation_url=call_values.get("automation_url");
    		String automation_type=call_values.get("automation_type");
    		callid=call_values.get("callid");
    		response_file=call_values.get("response_XMLFile");
    		logToken=new StringBuffer("[").append(callid).append("] ").toString();
    		Object automation_timeout=call_values.get("automation_timeout");
    		String eol = System.getProperty("line.separator");
    		boolean testCall= (Boolean)Boolean.parseBoolean(call_values.get("testCall").toString());
    		if(automation_timeout != null && !automation_timeout.equals("") )
    			timeoutValue=Integer.parseInt(automation_timeout.toString());
    		
    		URL wsURL=new URL(automation_url);
    		URLConnection connection=wsURL.openConnection();
    		httpConn=(HttpURLConnection)connection;
    		ByteArrayOutputStream bOut=new ByteArrayOutputStream();
    		
    		soapAction="urn:getMessage";
    		soapMessage=getXMLContentsFromTemplate(call_values.get("xml_report_template"));
    		if(soapMessage.length() < 1){
    			return;
    		}
    		soapMessage=soapMessage.replace(REPLACE_PRINTER_NAME,call_values.get("printer_name"));
    		soapMessage=soapMessage.replace(REPLACE_REPORT_TYPE, call_values.get("report_type"));
    		if(testCall)
    			LOGGER.debug("SoapMessage:"+soapMessage);
    		
    		byte[] buffers=new byte[soapMessage.length()];
    		buffers=soapMessage.getBytes();
    		bOut.write(buffers);
    		
    		byte[] byteArray=bOut.toByteArray();
    		
    		httpConn.setRequestProperty("SOAPAction", soapAction);   		
    		httpConn.setRequestProperty("Content-Type", "text/xml");
    		httpConn.setRequestMethod(automation_type);
    		httpConn.setConnectTimeout(timeoutValue);
    		httpConn.setDoOutput(true);
    		httpConn.setDoInput(true);
    		
    		br=new OutputStreamWriter(httpConn.getOutputStream());
    	    br.write(soapMessage);
    	    br.flush();
   		    
   		    //Read the response.
   		     isr =  new InputStreamReader(httpConn.getInputStream());
   		     in = new BufferedReader(isr);
   		     //Write the SOAP message response to a String.
   		     while ((responseString = in.readLine()) != null) {
   		         outputString.append(responseString).append(eol);
   		     }
   		     
   		     boolean created=LFileFunctions.createFile(response_file, logToken);
   		     if(created){
   		    	 LFileFunctions.writeToFile(logToken, response_file, outputString.toString(), true);
   		    	 String automation_status=getAutomationStatus(response_file,"");
   		    	 call_values.put("automation_status", automation_status);
   		    	 LFileFunctions.deleteFile(logToken, response_file);
   		    	parseStatusValue(automation_status,logToken);
   		    	 if(testCall)
   		    		 LOGGER.debug(new StringBuffer(logToken).append("Automation_Status:"+automation_status));
   		     }else{
   		    	 LOGGER.info(new StringBuffer(logToken).append("Result File could not be created"));
   		    	 parseStatusValue("96|Result File could not be created", logToken);
   		     }
   		     
			long finish=System.currentTimeMillis();
			long elapsed=finish-start;
			
			
			if(testCall){
				LOGGER.debug("ELAPSED TIME FOR WEBSERVICE:"+elapsed);
				LOGGER.debug("OutputString=="+outputString.toString());
			}
    	}catch(IOException io){
			call_values.put("automation_data", "RC=93|"+io.toString());
			call_values.put("automation_rc", "93");		   		
    		LOGGER.error("Error:" +io.toString(),io);
    	}catch(Exception e){
			call_values.put("automation_data", "RC=92|Error "+e.toString());
			call_values.put("automation_rc", "92");		    		
    		LOGGER.error("Error:" +e.toString(),e);
    	}
    	finally{
    		try{
    			if(in != null){
    				in.close();
    			}
    			if (httpConn != null){
    				httpConn.disconnect();
    			}
    			if(isr != null){
    				isr.close();
    			}
    			if(out != null){
    				out.close();
    			}
    			if(br != null){
    				br.close();
    			}
    		}catch(IOException ioe){
    			LOGGER.error("Finally IOException:"+ioe.toString(), ioe);
    		}
    	}   	
    }
    
	   /**
	    * Reads the contents of the XML Template that will be used to send the soap Request to WebService
	    * @param filename
	    * @return
	    */
	   public String getXMLContentsFromTemplate(String filename){
		   StringBuffer xml_data=new StringBuffer();
		   FileInputStream fstream=null;
		   DataInputStream dstream=null;
		   BufferedReader bReader=null;
		   String strLine="";
		   File file=null;
		   String eol = System.getProperty("line.separator");
		   try{
			   file = new File(filename);
			   if(!file.exists()){
					call_values.put("automation_data", "RC=96|Specified SOAP or XML File not found");
					call_values.put("automation_rc", "96");		
					return "";
			   }
			   fstream=new FileInputStream(filename);
			   dstream=new DataInputStream(fstream);
			   bReader=new BufferedReader(new InputStreamReader(dstream));
			   
			   while ((strLine = bReader.readLine()) != null)   {
				   xml_data.append(strLine).append(eol);
			   }			   
		   }catch(Exception e){
			   LOGGER.error("Error in getXMLContentsFromTemplate:"+e.toString());
		   }finally{
			   try{
				   if(fstream != null){
					   fstream.close();
				   }
				   if(dstream != null){
					   dstream.close();
				   }		
				   if(bReader != null){
					   bReader.close();
				   }
			   }catch(IOException io){
				   LOGGER.error("Error Closing Readers in getXMLContentsFromTemplate--"+io.toString());
			   }
		   }
		   return xml_data.toString();
	   }
	   
	   /**
	    * Returns the xml value in the response file for automation_status
	    * @param filename
	    * @param logToken
	    * @return
	    */
	   private String getAutomationStatus(String filename, String logToken){
		   String status="";
		   Object tempValue="";
		   String expression="";
		   try{
				 XPathReader reader = new XPathReader(filename, logToken);
				 
				    // To get a child element's value.'
				 	tempValue= call_values.get("automation_status_path");
				 	if(tempValue != null){
				 		expression=tempValue.toString();
				 	}
			        tempValue=reader.read(expression, XPathConstants.STRING);
			        if(tempValue != null){
			        	status=tempValue.toString();
			        }
		   }catch(Exception e){
			   LOGGER.error(new StringBuffer(logToken).append("Error getting automation_status"),e);
		   }
		   return status;
	   }
	   
		/**
		 * Parses the automation_status value from the belk web service xml response.
		 * @param status
		 */
		public void parseStatusValue(String status, String logToken){
			String automation_rc="";
			String newAutomation_status="";
			String[] values=null;
			try{
				if(status != null || !status.equals("")){
					values=status.split("\\|");
					if(values != null && values.length == 2){
						// | was found
						automation_rc=values[0];
						newAutomation_status=values[1];
					}else{
						if(status.equals("0")){
							automation_rc="0";
							newAutomation_status="Successful Post";			
						}else if(isNumber(status) && !status.equals("0")){
							automation_rc=status;
							newAutomation_status="Check support doc for return code meaning";									
						}else{
							automation_rc="94";
							newAutomation_status="Unknown return code value";
						}
						
					}
				}
				call_values.put("automation_rc", automation_rc);
				call_values.put("automation_status", newAutomation_status);
				
			}catch(Exception e){
				LOGGER.error(new StringBuffer(logToken).append("Error in parsing Status Value:"),e);
			}
		}
		
		/**
		 * Checks to see if a string is an integer.
		 * @param data -  string to see if it's an integer.
		 * @return
		 */
		private boolean isNumber(String data){
			try{
				Integer.parseInt(data);
				return true;
			}catch(Exception e){
				System.out.println("Error checking for valid number.");
				return false;
			}
		}
}


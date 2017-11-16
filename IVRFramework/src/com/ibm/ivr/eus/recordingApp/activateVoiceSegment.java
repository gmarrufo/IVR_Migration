package com.ibm.ivr.eus.recordingApp;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.ibm.ivr.eus.dao.STATUSMSGDAO;
import com.ibm.ivr.eus.data.CommonValues;
import com.ibm.ivr.eus.data.DbQueryTimeout;

/**
 * @author Val Peco
 * 
 * This handler written for EUS, activates a voice segment.
 */
public class activateVoiceSegment extends HttpServlet implements Servlet {

	private static Logger LOGGER = Logger.getLogger(activateVoiceSegment.class);

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// get session from Servlet request, created if not existed yet
		HttpSession session = req.getSession(true);
		
		File old_f;
		File new_f;
		String VDNList[] = {null,null,null,null,null,null,null,null,null,null};
		String fileList[] = {"","","","","","","","","",""};
		
		String VDN = null;
		String status = "S";
		String fileName = "";
	    String jndiName = null;

	    boolean exists = false;
	    boolean haveActive = false;
		boolean rc;
		int i;

		STATUSMSGDAO eusstatusmsg = null;

		String callid = (String) session.getAttribute("callid");

		boolean testCall = ((Boolean) session.getAttribute("testCall")).booleanValue();

		//create the log Token for later use, use StringBuffer to reduce number
		//of String objects
		String logToken = new StringBuffer("[").append(callid).append("] ").toString();

		if (testCall)
			LOGGER.info(new StringBuffer(logToken).append("activateVoiceSegment: Entering activateVoiceSegment handler"));
		
		Properties prop = (Properties) session.getServletContext().getAttribute("globalProp");
		String fileExtension = prop.getProperty("audioFileExtension");
		jndiName = prop.getProperty(CommonValues.EUS_DATA_SOURCE_PROPERTY);

		// Get the session variables.
		String dirPath = (String)session.getAttribute("dirPath");
		String dirType = (String)session.getAttribute("dirType");
		String dirName = (String)session.getAttribute("dirName");
		String dirLanguage = (String)session.getAttribute("dirLanguage");
		String segmentID = (String)session.getAttribute("segmentID");
		String activateVDN = (String)session.getAttribute("ActivateVDN");
		
		// Aug 8, 2011 SCS - initialize/create session variable alreadyActive - will reset to true later if true
		session.setAttribute("alreadyActive", "false");
		

    	// Rename the given voice segment from _NEW.
    	if( dirType.compareToIgnoreCase("V") == 0 )
    	{
    		fileName = segmentID;
    	}
    	
    	else
    	{
    		if( activateVDN.compareToIgnoreCase("false") == 0 )
    			fileName = "S" + segmentID;    		
    		else
    			fileName = "S" + segmentID + "_" + activateVDN;
    	}

		// check if the file name is in the list. 
		fileList = (String [])session.getAttribute("FileList");
		VDNList = (String [])session.getAttribute("VDNList");
		for( i = 0; fileList[i].compareTo("") != 0; i++ )
		{
			LOGGER.info(new StringBuffer(logToken).append("activateVoiceSegment: fileList: ").append(fileList[i]));
			if( fileList[i].compareTo(fileName + "_NEW") == 0 )
			{
				LOGGER.info(new StringBuffer(logToken).append("activateVoiceSegment: fileList compared."));
				exists = true;
				break;
			}
			//-------------------------------------------------------------------------------------------------
			// Aug 8, 2011 - SCS - added logic to determine if an active version of this file already exists
			//-------------------------------------------------------------------------------------------------
			if( fileList[i].compareTo(fileName) == 0 )
			{
				LOGGER.info(new StringBuffer(logToken).append("activateVoiceSegment: found active segment"));
				haveActive = true;
			}
		}
		

    	// check if the files exists.
		
		if( !exists )
		{
			// Aug 8, 2011 SCS chgd logic to look at if active file exists too 
			if (!haveActive){
				session.setAttribute("FileExist", "false");
				session.setAttribute("alreadyActive", "false");
				LOGGER.info(new StringBuffer(logToken).append("activateVoiceSegment: file does not exist: ").append(fileName));
			} else {
				LOGGER.info(new StringBuffer(logToken).append("activateVoiceSegment: file already active: ").append(fileName));
				session.setAttribute("FileExist", "true");    // need to set this to true to allow caller to continue working with this segement
				session.setAttribute("alreadyActive", "true");
			}	
			return;
		}
		
		// Note: on return need: 
		// if FileExist = T and alreadyActive = F - file was activated
		// if FileExist = T and alreadyActive = T - already activated
		// if FileExist = F and alreadyActive = F - no file to activate
		
		session.setAttribute("FileExist", "true");
		session.setAttribute("alreadyActive", "false");
		
				
       	new_f = new File(dirPath + fileName + "_NEW" + fileExtension);
   		old_f = new File(dirPath + fileName + fileExtension);
   		if (testCall) {
   			LOGGER.info(new StringBuffer(logToken).append("activateVoiceSegment: old file: ").append(old_f));
   			LOGGER.info(new StringBuffer(logToken).append("activateVoiceSegment: new file: ").append(new_f));
   		}

   	// Aug 09, 2011 - need to update the session FileList to replace SXXX_NEW with SXXX
		for (int x=0; fileList[x] != ""; x++)
	    {

	    	// remove the filename from the file list.
			if( fileList[x].compareToIgnoreCase(fileName + "_NEW") == 0 )
	    	{
	    		if (testCall)
	    			LOGGER.info(new StringBuffer(logToken).append("activateVoiceSegment: update fileList."));
				fileList[x] = fileName;				
	    	}
	    }
		session.setAttribute("FileList", fileList);
   		
   		
   		// delete the old file first and the database entry if it is a status message
		if( old_f.exists() )
		{
			old_f.delete();
  			if( dirType.compareTo("S") == 0 )
   			{
  				try {
  					/* create data access object interface to db */
  					int queryTimeout = DbQueryTimeout.getDbQueryTimout(prop);
  					eusstatusmsg = new STATUSMSGDAO(jndiName, callid, testCall, queryTimeout);
		    	
  					/* delete specified status message */
  					rc = eusstatusmsg.deleteStatusMsg(dirName,dirLanguage,segmentID,VDN);
  					if( !rc )
  					{
  						if (testCall)
  							LOGGER.info(new StringBuffer(logToken).append("activateVoiceSegment: activateStatusMsg failed."));
  						status = "E";
  					}
  				}
			
  				// delete failed.
  				catch (Exception e) {
  					e.printStackTrace();
  					status = "E";
  				}
   			}
		}

   		// rename the file
   		rc = new_f.renameTo(old_f);
   		if( !rc )
   		{
   	   		LOGGER.error(new StringBuffer(logToken).append("activateVoiceSegment: rename file failed: ").append(rc));
   			status = "E";
 		}    		
    	
   		// update the NEW database entry.
   		else
   		{
  			if( dirType.compareTo("S") == 0 )
   			{
				LOGGER.info(new StringBuffer(logToken).append("activateVoiceSegment: VDNList ").append(VDNList[i]));
				
   				if( VDNList[i] != null )
   				{
   	   				if( VDNList[i].compareToIgnoreCase("NEW") == 0 )
   	   					VDN = null;
   	   				else
   	   					VDN = VDNList[i].replace("_NEW", "");
   				}
   				
				if( VDN != null )
					LOGGER.info(new StringBuffer(logToken).append("activateVoiceSegment: VDN = ").append(VDN));
   				try {
   					
   					/* create data access object interface to db */
   					if( eusstatusmsg == null ) {
   						int queryTimeout = DbQueryTimeout.getDbQueryTimout(prop);
   						eusstatusmsg = new STATUSMSGDAO(jndiName, callid, testCall, queryTimeout);
   					}
   					
   					/* update the NEW status message */
   					rc = eusstatusmsg.activateStatusMsg(dirName, dirLanguage, segmentID, VDN);
   					if( !rc )
   					{
   						if (testCall)
   	   						LOGGER.info(new StringBuffer(logToken).append("activateVoiceSegment: activateStatusMsg failed."));
   						status = "E";
   					}
   				}
			
   				catch (Exception e) {
   					e.printStackTrace();
   					status = "E";
   				}	
   			}
   		}
   		  		
   		
		// Set the session variables for the return data.
		session.setAttribute("FileName", fileName);
		session.setAttribute("RecordingStatus", status);

		// Print out the values.
		if (testCall)
		{
			
			LOGGER.info(new StringBuffer(logToken).append("activateVoiceSegment: status: ").append(status));
			LOGGER.info(new StringBuffer(logToken).append("activateVoiceSegment: FileExist: ").append(session.getAttribute("FileExist")));
			LOGGER.info(new StringBuffer(logToken).append("activateVoiceSegment: FileCount: ").append(session.getAttribute("FileCount")));			
			LOGGER.info(new StringBuffer(logToken).append("activateVoiceSegment: FileName: ").append(session.getAttribute("FileName")));
			LOGGER.info(new StringBuffer(logToken).append("activateVoiceSegment: SegmentID: ").append(session.getAttribute("segmentID")));
			LOGGER.info(new StringBuffer(logToken).append("activateVoiceSegment: FileList: ").append(session.getAttribute("FileList")));
		}

		return;
	}

}

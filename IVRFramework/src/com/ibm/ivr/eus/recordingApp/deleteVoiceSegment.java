package com.ibm.ivr.eus.recordingApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Properties;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.ibm.ivr.eus.dao.*;
import com.ibm.ivr.eus.data.CommonValues;
import com.ibm.ivr.eus.data.DbQueryTimeout;

/**
 * @author Val Peco
 * 
 * This handler written for EUS, deletes a voice segment.
 */
public class deleteVoiceSegment extends HttpServlet implements Servlet {

	private static Logger LOGGER = Logger.getLogger(deleteVoiceSegment.class);

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		File inputFile = null;
		File backupFile = null;
		Integer VDNCount = 0;
		String VDNList[] = {null,null,null,null,null,null,null,null,null,null};
		String fileList[] = {"","","","","","","","","",""};
	    String jndiName = null;
	    String status = "S";
	    boolean rc = true;
		
		// get session from Servlet request, created if not existed yet
		HttpSession session = req.getSession(true);

		String callid = (String) session.getAttribute("callid");

		boolean testCall = ((Boolean) session.getAttribute("testCall")).booleanValue();

		//create the log Token for later use, use StringBuffer to reduce number
		//of String objects
		String logToken = new StringBuffer("[").append(callid).append("] ").toString();

		if (testCall)
			LOGGER.info(new StringBuffer(logToken).append("deleteVoiceSegment: Entering deleteVoiceSegment handler"));
		
		Properties prop = (Properties) session.getServletContext().getAttribute("globalProp");
		jndiName = prop.getProperty(CommonValues.EUS_DATA_SOURCE_PROPERTY);
		
		//get the session variable data to build the path
		String dirPath = (String) session.getAttribute("dirPath");
		String fileName = (String)session.getAttribute("FileName");
		String dirType = (String)session.getAttribute("dirType");
		String dirName = (String)session.getAttribute("dirName");
		String language = (String)session.getAttribute("dirLanguage");
		String segmentID = (String)session.getAttribute("segmentID");
		Integer fileCount = (Integer)session.getAttribute("FileCount");
		String VDN = (String)session.getAttribute("CurrentVDN");

		if (testCall)
		{
			LOGGER.info(new StringBuffer(logToken).append("deleteVoiceSegment: dirPath: ").append(dirPath));
			LOGGER.info(new StringBuffer(logToken).append("deleteVoiceSegment: fileName: ").append(fileName));
			LOGGER.info(new StringBuffer(logToken).append("deleteVoiceSegment: fileCount: ").append(fileCount));
		}
		
		inputFile = new File(dirPath + fileName + prop.getProperty("audioFileExtension"));

		// don't back up a NEW file
		if( fileName.contains("NEW") )
		{
			inputFile.delete();
			fileCount = fileCount - 1;
		}
		
		// back up the current file.
		else
		{
			backupFile = new File(dirPath + "bak." + fileName + prop.getProperty("audioFileExtension"));

			if(!backupFile.exists())
			{
				if (testCall)
					LOGGER.info(new StringBuffer(logToken).append("deleteVoiceSegment: backup file does not exist: ").append(backupFile));
				backupFile.createNewFile();
			}

			FileChannel source = null;
			FileChannel destination = null;
			try {
				source = new FileInputStream(inputFile).getChannel();
				destination = new FileOutputStream(backupFile).getChannel();
				destination.transferFrom(source, 0, source.size());
			}
			finally {
				if(source != null) {
					source.close();
					inputFile.delete();
				}
				if(destination != null) {
					destination.close();
					fileCount = fileCount - 1;
				}
			}
		}

		session.setAttribute("FileCount", fileCount);
		if( fileCount == 0)
		{
			session.setAttribute("FileExist", "false");
			session.setAttribute("FileName", "");
			session.setAttribute("VDNCount", 0);
			session.setAttribute("CurrentVDN", "");
			session.setAttribute("VDNIndex", 1);

			session.setAttribute("FileList", fileList);
			session.setAttribute("VDNList", VDNList);
		}

		// Adjust the status message variables.
		else
		{
			if( dirType.compareTo("S") == 0 )
			{
				VDNCount = (Integer)session.getAttribute("VDNCount");
				if(  VDNCount > 0 )
				{
					VDNCount = VDNCount - 1;
					session.setAttribute("VDNCount", VDNCount );
				}

				fileList = (String [])session.getAttribute("FileList");
			    for (int i=0; fileList[i] != ""; i++)
			    {

			    	// remove the filename from the file list.
			    	if( fileList[i].compareToIgnoreCase(fileName) == 0 )
			    	{
			    		if (testCall)
			    			LOGGER.info(new StringBuffer(logToken).append("deleteVoiceSegment: FileList contains fileName."));
						fileList[i] = "";
						int j = i;
						while( fileList[j+1] != "" )
						{
							fileList[j] = fileList[j+1];
							j++;
						}
						fileList[j] = "";
			    	}
			    }
			    
				VDNList = (String[])session.getAttribute("VDNList");

		    	// remove the VDN from the VDNlist.
			    for (int i=0; VDNList[i] != null; i++)
			    {
			    	if( VDNList[i].compareToIgnoreCase(VDN) == 0)
			    	{
			    		if (testCall)
			    			LOGGER.info(new StringBuffer(logToken).append("deleteVoiceSegment: VDNList contains VDN"));
						VDNList[i] = null;
						int j = i;
						while( VDNList[j+1] != null )
						{
							VDNList[j] = VDNList[j+1];
							j++;
						}
						VDNList[j] = null;
			    	}
			    }

				session.setAttribute("CurrentVDN",  VDNList[0]);
				session.setAttribute("FileName", fileList[0]);
			}
		}
		
		// remove the entry from the DB.
		if( dirType.compareTo("S") == 0 )
		{
			try {
		    	/* create data access object interface to db */
				int queryTimeout = DbQueryTimeout.getDbQueryTimout(prop);
		    	STATUSMSGDAO eusstatusmsg = new STATUSMSGDAO(jndiName, callid, testCall, queryTimeout);
		    	
		    	/* delete specified status message */
		    	rc = eusstatusmsg.deleteStatusMsg(dirName,language,segmentID,VDN);
		    	if( !rc )
		    	{
		    		if (testCall)
		    			LOGGER.info(new StringBuffer(logToken).append("deleteVoiceSegment: deleteStatusMsg failed."));
					status = "E";
		    	}
			}
			
			catch (Exception e) {
				e.printStackTrace();
				status = "E";
			}	
		}
			
		if (testCall)
		{
			LOGGER.info(new StringBuffer(logToken).append("deleteVoiceSegment: VDNCount: ").append(VDNCount));
			for( int i=0; VDNList[i] != null; i++ )
				LOGGER.info(new StringBuffer(logToken).append("deleteVoiceSegment: VDNList: ").append(VDNList[i]));
			for( int i=0; fileList[i] != ""; i++ )
				LOGGER.info(new StringBuffer(logToken).append("deleteVoiceSegment: FileList: ").append(fileList[i]));
			LOGGER.info(new StringBuffer(logToken).append("deleteVoiceSegment: FileCount: ").append(fileCount));
		}

		session.setAttribute("Status", status);
		return;
	}
}

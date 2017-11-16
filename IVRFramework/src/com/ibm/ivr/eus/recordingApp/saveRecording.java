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

import com.ibm.ivr.eus.dao.STATUSMSGDAO;
import com.ibm.ivr.eus.data.CommonValues;
import com.ibm.ivr.eus.data.DbQueryTimeout;


/**
 * @author Val Peco
 * 
 * This handler written for EUS, saves a voice recording.
 */
public class saveRecording extends HttpServlet implements Servlet {

	private static Logger LOGGER = Logger.getLogger(saveRecording.class);

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// get session from Servlet request, created if not existed yet
		HttpSession session = req.getSession(true);
		
		File temp_f;
		File new_f;
		String status = "S";
		String fileName = "";
		String tempFile = "";
		String tempStartDateTime = null;
		String tempEndDateTime = null;
		String fileList[] = {"","","","","","","","","",""};
		String VDNList[] = {null,null,null,null,null,null,null,null,null,null};
		String tempVDNList[] = {"","","","","","","","","",""};
	    String jndiName = null;
		boolean rc;
		boolean update = false;

		String callid = (String) session.getAttribute("callid");

		boolean testCall = ((Boolean) session.getAttribute("testCall")).booleanValue();

		//create the log Token for later use, use StringBuffer to reduce number
		//of String objects
		String logToken = new StringBuffer("[").append(callid).append("] ").toString();

		if (testCall)
			LOGGER.info(new StringBuffer(logToken).append("saveRecording: Entering saveRecording handler"));
		
		Properties prop = (Properties) session.getServletContext().getAttribute("globalProp");
		jndiName = prop.getProperty(CommonValues.EUS_DATA_SOURCE_PROPERTY);

		// Get the session variables.
		String dirPath = (String)session.getAttribute("dirPath");
		String dirType = (String)session.getAttribute("dirType");
		String dirName = (String)session.getAttribute("dirName");
		String dirLanguage = (String)session.getAttribute("dirLanguage");
		String segmentID = (String)session.getAttribute("segmentID");
		Integer fileCount = (Integer)session.getAttribute("FileCount");
		String startDateTime = (String)session.getAttribute("StartDateTime");
		String endDateTime = (String)session.getAttribute("EndDateTime");
		String bargein = (String)session.getAttribute("Bargein");
		String replicateNow = (String)session.getAttribute("dirReplicateNow");
		
		fileList = (String[])session.getAttribute("FileList");
		Integer VDNCount = (Integer)session.getAttribute("VDNCount");
		Integer tempVDNCount = (Integer)session.getAttribute("TempVDNCount");
		
		VDNList = (String[])session.getAttribute("VDNList");
		tempVDNList = (String[])session.getAttribute("TempVDNList");
		
    	temp_f = new File(dirPath + "tempRecording" + prop.getProperty("audioFileExtension"));
    	
    	// This is a Voice segment save.
    	if( dirType.compareToIgnoreCase("V") == 0 )
    	{
    		// don't replicate now so save to a 'NEW' file.
    		fileName = segmentID;
    		if( replicateNow.compareToIgnoreCase("N") == 0)
    		{
        		fileName = segmentID + "_NEW";
        		new_f = new File(dirPath + segmentID + "_NEW" + prop.getProperty("audioFileExtension"));
        		if( new_f.exists())
        		{
        			if (testCall)
        				LOGGER.info(new StringBuffer(logToken).append("saveRecording: NEW already exists, deleting file: ").append(new_f));
        			new_f.delete();
        		}
    		}
   		
    		// replicate now.
    		else
    		{
    			new_f = new File(dirPath + segmentID + prop.getProperty("audioFileExtension"));
    		}
    		
    		if (testCall) {
    			LOGGER.info(new StringBuffer(logToken).append("saveRecording: temp file: ").append(temp_f));
    			LOGGER.info(new StringBuffer(logToken).append("saveRecording: new file: ").append(new_f));
    		}

			// check if the file already exists. If it does, copy the existing file to a backup
			if( new_f.exists() )
			{
	    		File backupFile = new File(dirPath + "bak." + segmentID + prop.getProperty("audioFileExtension"));
	    		if(!backupFile.exists())
	    		{
	    			if (testCall)
	    				LOGGER.info(new StringBuffer(logToken).append("saveRecording: backup file does not exist: ").append(backupFile));
	    			backupFile.createNewFile();
	    		}
	    		
	    		else
	    		{
	    			backupFile.delete();
	    			backupFile.createNewFile();
	    		}

	    		FileChannel source = null;
	    		FileChannel destination = null;
	    		try {
	    			source = new FileInputStream(new_f).getChannel();
	    			destination = new FileOutputStream(backupFile).getChannel();
	    			destination.transferFrom(source, 0, source.size());
	    		}
	    		finally {
	    			if(source != null) {
	    				source.close();
	    			}
	    			if(destination != null) {
	    				destination.close();
	    			}
	    		}
			}

			// delete the old file.
    		if( replicateNow.compareToIgnoreCase("Y") == 0 )
    		{
           		if( new_f.exists())
           		{
           			if (testCall)
           				LOGGER.info(new StringBuffer(logToken).append("saveRecording: deleting file: ").append(new_f));
           			new_f.delete();
        		}
    		}
    		    		
			// rename the temporary file to the new file.
    		rc = temp_f.renameTo(new_f);
       		if( !rc )
       		{
       			status = "E";
       		}

       		// set the session variables.
    		else
    		{
    			session.setAttribute("FileName", fileName);
       			session.setAttribute("FileCount", fileCount + 1);
       			session.setAttribute("FileExist", "true");
       			fileList[fileCount] = fileName;
       			session.setAttribute("FileList", fileList);
    		}
    	}
    	
    	// This is a status message save.
    	else
    	{
    		for( int i = 0; i < tempVDNCount; i++ )
    		{
    			if( tempVDNList[i].compareToIgnoreCase("ALL") == 0 )
    				fileName = "S" + segmentID;
    			else
    				fileName = "S" + segmentID + "_" + tempVDNList[i];
        		new_f = new File(dirPath + fileName + prop.getProperty("audioFileExtension"));
 
        		if (testCall) {
        			LOGGER.info(new StringBuffer(logToken).append("saveRecording: temp file: ").append(temp_f));
        			LOGGER.info(new StringBuffer(logToken).append("saveRecording: new file: ").append(new_f));
        		}

    			// check if the file already exists. If it does, copy the existing file to a backup
    			if( new_f.exists() )
    			{
    	    		File backupFile = new File(dirPath + "bak." + fileName + prop.getProperty("audioFileExtension"));
    	    		if(!backupFile.exists())
    	    		{
    	    			if (testCall)
    	    				LOGGER.info(new StringBuffer(logToken).append("saveRecording: backup file does not exist: ").append(backupFile));
    	    			backupFile.createNewFile();
    	    		}

    	    		FileChannel source = null;
    	    		FileChannel destination = null;
    	    		try {
    	    			source = new FileInputStream(new_f).getChannel();
    	    			destination = new FileOutputStream(backupFile).getChannel();
    	    			destination.transferFrom(source, 0, source.size());
    	    		}
    	    		finally {
    	    			if(source != null) {
    	    				source.close();
    	    				if (testCall)
    	    					LOGGER.info(new StringBuffer(logToken).append("saveRecording: deleting voice segment."));
    	    			}
    	    			if(destination != null) {
    	    				destination.close();
    	    			}
    	    		}
    			}

    			// delete the old file.
        		if( replicateNow.compareToIgnoreCase("Y") == 0 )
        		{
    				new_f.delete();
          		}
       			
        		// don't replicate now so save the file to a NEW file.
        		else
        		{
            		new_f = new File(dirPath + fileName + "_NEW" + prop.getProperty("audioFileExtension"));
        		}

        		// save the file
	    		FileChannel source = null;
	    		FileChannel destination = null;
	    		try {
	    			source = new FileInputStream(temp_f).getChannel();
	    			destination = new FileOutputStream(new_f).getChannel();
	    			destination.transferFrom(source, 0, source.size());
	    		}
	    		finally {
	    			if(source != null) {
	    				source.close();
	    				if (testCall)
	    					LOGGER.info(new StringBuffer(logToken).append("saveRecording: deleting voice segment."));
	    			}
	    			if(destination != null) {
	    				destination.close();
	    			}
	    		}
    		}

    		// delete the temporary file.
    		temp_f.delete();
   		
    		// set all the VDN session variables
    		if( status != "E" )
    		{
				if( startDateTime.compareToIgnoreCase("NOW") != 0)
					tempStartDateTime = startDateTime;
				if( endDateTime.compareToIgnoreCase("NEVER") != 0)
					tempEndDateTime = endDateTime;

    			if( tempVDNList[0].compareToIgnoreCase("ALL") == 0 )
    			{
    				// save to a new file if not distributing now.
    	    		if( replicateNow.compareToIgnoreCase("N") == 0)
    	    		{
        				tempFile = "S" + segmentID + "_NEW";    	    			
    	    		}
    	    		
    	    		else
    	    		{
    	    			tempFile = "S" + segmentID;
    	    		}
					if( fileList[0].compareToIgnoreCase(tempFile) == 0 )
						update = true;
    				fileList[0] = tempFile;
    				fileCount = 1;
    				session.setAttribute("FileName", tempFile);

    				// remove all the other VDN messages
    				for( int i = 0; i < VDNCount; i++ )
    				{
       	    			fileName = "S" + segmentID + "_" + VDNList[i];        	    			
                		new_f = new File(dirPath + fileName + prop.getProperty("audioFileExtension"));
                		
                		if( new_f.exists())
                		{
                			new_f.delete();

                			// delete any specific VDN messages because a new ALL VDN message has been saved.
                			try {
                				/* create data access object interface to db */
                				int queryTimeout = DbQueryTimeout.getDbQueryTimout(prop);
                				STATUSMSGDAO eusstatusmsg = new STATUSMSGDAO(jndiName, callid, testCall, queryTimeout);
    				    	
                				/* delete specified status message */
                				rc = eusstatusmsg.deleteStatusMsg(dirName,dirLanguage,segmentID,VDNList[i]);
        				    	if( !rc )
        				    	{
        				    		if (testCall)
        				    			LOGGER.info(new StringBuffer(logToken).append("saveRecording: addStatusMsg failed."));
        				    		status = "E";
        				    	}
                			}
    					
                			catch (Exception e) {
                				e.printStackTrace();
                				status = "E";    						
                			}
                		}
    				}
    				
    				// save each status message data to the database.
    				try {
				    	/* create data access object interface to db */
    					int queryTimeout = DbQueryTimeout.getDbQueryTimout(prop);
				    	STATUSMSGDAO eusstatusmsg = new STATUSMSGDAO(jndiName, callid, testCall, queryTimeout);
				    
				    	// Set the VDN to NEW if we are not distributing now.
				    	String tempVDN = null;
				    	if( replicateNow.compareToIgnoreCase("N") == 0)
        	    		{
				    		tempVDN = "NEW";
        	    		}
        	    						    	
				    	/* add specified status message */
				    	rc = eusstatusmsg.addStatusMsg(update,dirName,dirLanguage,segmentID,tempVDN,tempStartDateTime,tempEndDateTime,bargein);
				    	if( !rc )
				    	{
				    		if (testCall)
				    			LOGGER.info(new StringBuffer(logToken).append("saveRecording: addStatusMsg failed."));
							status = "E";
				    	}
					}
					
					catch (Exception e) {
						e.printStackTrace();
			    		status = "E";
					}
					
					VDNCount = 0;
					for( int t=0;t < 10; t++)
						VDNList[t] = null;
    			}

    			// This is a status message for specific VDNs.
    			else
    			{
					LOGGER.info(new StringBuffer(logToken).append("saveRecording: Status message specific VDN."));
    				// delete any ALL VDN type messages.
	        		if( replicateNow.compareToIgnoreCase("N") == 0 )
	        		{
	        			temp_f = new File(dirPath + "S" + segmentID + "_NEW" + prop.getProperty("audioFileExtension"));
	        		}
	        		
	        		else
	        		{
	        			temp_f = new File(dirPath + "S" + segmentID + prop.getProperty("audioFileExtension"));	        			
	        		}
	        		
            		if( temp_f.exists() )
            		{
            			temp_f.delete();

            			// Delete the ALL VDN message data from the database.
    					try {
    				    	/* create data access object interface to db */
    						int queryTimeout = DbQueryTimeout.getDbQueryTimout(prop);
    				    	STATUSMSGDAO eusstatusmsg = new STATUSMSGDAO(jndiName, callid, testCall, queryTimeout);
    				    	
    				    	/* get specified status message */
    				    	String tempVDN = null;
    				    	if( replicateNow.compareToIgnoreCase("N") == 0)
            	    		{
    				    		tempVDN = "NEW";
            	    		}
            				rc = eusstatusmsg.deleteStatusMsg(dirName,dirLanguage,segmentID,tempVDN);
            				
    				    	if( !rc )
    				    	{
    				    		if (testCall)
    				    			LOGGER.info(new StringBuffer(logToken).append("saveRecording: deleteStatusMsg failed."));
    							status = "E";
    				    	}
    					}
    					
    					catch (Exception e) {
    						e.printStackTrace();
							status = "E";
    					}	
            		}		

            		// loop thru theVDNs
    				int k, j;
    				if (testCall)
    					LOGGER.info(new StringBuffer(logToken).append("saveRecording: **** Starting loop."));
    				for( j = 0; tempVDNList[j].compareTo("") != 0; j++ )
    				{
    					if (testCall)
    						LOGGER.info(new StringBuffer(logToken).append("saveRecording: **** tempVDNList: ").append(tempVDNList[j]));
				    	update = false;
    					if( replicateNow.compareToIgnoreCase("N") == 0)
        	    		{
				    		tempVDNList[j] = tempVDNList[j] + "_NEW"; 
        	    		}
				    	
    					tempFile = "S" + segmentID + "_" + tempVDNList[j];

        				for( k = 0; k < fileCount; k++ )
        				{
        					if( fileList[k].compareToIgnoreCase(tempFile) == 0 )
        					{
        						if (testCall)
        							LOGGER.info(new StringBuffer(logToken).append("saveRecording: **** found a matching file: ").append(tempFile));
        						update = true;
        						break;
        					}
        				}

        				// a match was not found, add it
        				if( k == fileCount )
        				{
        					if (testCall)
        						LOGGER.info(new StringBuffer(logToken).append("saveRecording: **** No match found"));
            				fileList[fileCount] = tempFile;
            				fileCount++;
            				VDNList[VDNCount] = tempVDNList[j];
            				VDNCount++;
        				}
        				
        				// Save the specific VDN message data to the database.
    					try {
    				    	/* create data access object interface to db */
    						int queryTimeout = DbQueryTimeout.getDbQueryTimout(prop);
    				    	STATUSMSGDAO eusstatusmsg = new STATUSMSGDAO(jndiName, callid, testCall, queryTimeout);
    				    	
    				    	/* get specified status message */
    				    	rc = eusstatusmsg.addStatusMsg(update,dirName,dirLanguage,segmentID,tempVDNList[j],tempStartDateTime,tempEndDateTime,bargein);
    				    	if( !rc )
    				    	{
    				    		if (testCall)
    				    			LOGGER.info(new StringBuffer(logToken).append("saveRecording: addStatusMsg failed."));
    							status = "E";
    				    	}
    					}
    					
    					catch (Exception e) {
    						e.printStackTrace();
							status = "E";
    					}
    					
         			}

    				session.setAttribute("FileName", "S" + segmentID + "_" + VDNList[0]);

    			}    			
    		}

    		// save the session data.
    		session.setAttribute("FileCount", fileCount);
			session.setAttribute("FileExist", "true");

			session.setAttribute("FileList", fileList);
			session.setAttribute("VDNCount", VDNCount);
			session.setAttribute("VDNList", VDNList);
			session.setAttribute("CurrentVDN", VDNList[0]);
			session.setAttribute("VDNIndex", 1);

    	}	
		
		// Set the session variables for the return data.
		session.setAttribute("Status", status);

		// Print out the values.
		if (testCall)
		{
			LOGGER.info(new StringBuffer(logToken).append("saveRecording: status: ").append(status));
			LOGGER.info(new StringBuffer(logToken).append("saveRecording: FileExist: ").append(session.getAttribute("FileExist")));
			LOGGER.info(new StringBuffer(logToken).append("saveRecording: FileCount: ").append(session.getAttribute("FileCount")));
			for( int i=0; fileList[i] != ""; i++ )
				LOGGER.info(new StringBuffer(logToken).append("saveRecording: FileList: ").append(fileList[i]));
			
			LOGGER.info(new StringBuffer(logToken).append("saveRecording: FileName: ").append(session.getAttribute("FileName")));
			LOGGER.info(new StringBuffer(logToken).append("saveRecording: VDNCount: ").append(session.getAttribute("VDNCount")));
			LOGGER.info(new StringBuffer(logToken).append("saveRecording: VDNIndex: ").append(session.getAttribute("VDNIndex")));
			for( int i=0; VDNList[i] != null; i++ )
				LOGGER.info(new StringBuffer(logToken).append("saveRecording: VDNList: ").append(VDNList[i]));
			
			LOGGER.info(new StringBuffer(logToken).append("saveRecording: CurrentVDN: ").append(session.getAttribute("CurrentVDN")));
			LOGGER.info(new StringBuffer(logToken).append("saveRecording: SegmentID: ").append(session.getAttribute("segmentID")));
		}

		return;
	}
}

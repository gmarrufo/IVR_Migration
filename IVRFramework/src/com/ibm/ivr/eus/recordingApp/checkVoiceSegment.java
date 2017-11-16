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
 * This handler written for EUS, checks is a voice segment exists, how many exist and what type it is.
 * 
 * Aug 04, 2011 - SCS added logic to handle allowed_segements and blocked_segments (session vars: dirAllowedSegments and dirBlockedSegments) 
 * 
 */
public class checkVoiceSegment extends HttpServlet implements Servlet {

	private static Logger LOGGER = Logger.getLogger(checkVoiceSegment.class);

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		File f = null;
		Integer count = 0;
		String VDN = null;
		String fileList[] = {"","","","","","","","","",""};
		String VDNList[] = {null,null,null,null,null,null,null,null,null,null};
	    String jndiName = null;
	    String status = "S";
	    boolean newFlag = false;

	    String dbTimes[] = {"",""};
		String startDateTime = null;
		String startDate = null;
		String startTime = null;
		String endDateTime = null;
		String endDate = null;
		String endTime = null;
		
		boolean segAllowed = true;
		
		// get session from Servlet request, created if not existed yet
		HttpSession session = req.getSession(true);

		String callid = (String) session.getAttribute("callid");

		boolean testCall = ((Boolean) session.getAttribute("testCall")).booleanValue();

		//create the log Token for later use, use StringBuffer to reduce number
		//of String objects
		String logToken = new StringBuffer("[").append(callid).append("] ").toString();

		if (testCall)
			LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: Entering checkVoiceSegment handler"));
		
		//get the session variable data.
		String dirPath = (String) session.getAttribute("dirPath");
		String segmentID = (String)session.getAttribute("segmentID");
		String dirType = (String)session.getAttribute("dirType");
		String dirName = (String)session.getAttribute("dirName");
		String dirLanguage = (String)session.getAttribute("dirLanguage");

		// Aug 04, 2011 - SCS get dirAllowedSegments and dirBlockedSegments
		String dirAllowedSegments = (String) session.getAttribute("dirAllowedSegments");
		String dirBlockedSegments = (String) session.getAttribute("dirBlockedSegments");
		
		if (testCall)
		{
			LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: dirPath: " + dirPath));
			LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: segment ID: " + segmentID));
			LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: dirType: " + dirType));
			LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: dirName: " + dirName));
			LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: dirLanguage: " + dirLanguage));
			LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: dirAllowedSegments: " + dirAllowedSegments));
			LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: dirBlockedSegments: " + dirBlockedSegments));
			
		}
		
		// Set the session variables for the return data.
		session.setAttribute("FileExist", "false");
		session.setAttribute("FileCount", 0);
		session.setAttribute("FileName", "");
		session.setAttribute("FileList", fileList);
		session.setAttribute("VDNList", VDNList);
		session.setAttribute("VDNIndex", 1);
		session.setAttribute("VDNCount", 0);

		Properties prop = (Properties) session.getServletContext().getAttribute("globalProp");
		String fileExtension = prop.getProperty("audioFileExtension");
		jndiName = prop.getProperty(CommonValues.EUS_DATA_SOURCE_PROPERTY);

		// Check if this a Main Menu id.
		if( segmentID.compareTo("#") == 0)
		{
			if (testCall)
				LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: main menu status message"));
			segmentID = "M";
		}
		
		// This is not a Main Menu id so check if there is a #.
		else
		{
			if( segmentID.contains("#"))
			{
				segmentID = segmentID.replace("#", "");
			}
		}

		session.setAttribute("segmentID", segmentID);
		
		//-----------------------------------------------------
		// Aug 4, 2011 - SCS add logic for allowed and blocked 
		// format of allowed_segements="M;4;41"
		//-----------------------------------------------------
		if (dirAllowedSegments != null ) {
			
			dirAllowedSegments = ";" + dirAllowedSegments + ";" ;
			
			
			if (dirAllowedSegments.contains(";" + segmentID + ";")) {
				// allowed segement 
				if (testCall)
				{
					LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: segment" + segmentID + " is allowed: "));
				}
			} else {
				segAllowed = false;
				status = "SEG_NOT_ALLOWED";
				if (testCall)
				{
					LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: segment" + segmentID + "is NOT allowed: "));
				}
			}
		} else {
			if  (dirBlockedSegments != null) {

				dirBlockedSegments = ";" + dirBlockedSegments + ";" ;
				
				if (dirBlockedSegments.contains(";" + segmentID + ";")) {
					// blocked segment 
					segAllowed = false;
					status = "SEG_NOT_ALLOWED";
					if (testCall)
					{
						LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: segment" + segmentID + " is blocked: "));
					}
				} else {

					if (testCall)
					{
						LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: segment" + segmentID + "is NOT blocked: "));
					}
				}
			}
		}

		if (segAllowed) {
		
			// if this is a prompt directory, just check if the voice segment exists.
			if( dirType.compareTo("V") == 0 )
			{
				// check if a _NEW file exists.
				f = new File(dirPath + segmentID +"_NEW" + fileExtension);
				if( f.exists() ) {
					session.setAttribute("FileExist", "true");
					session.setAttribute("FileCount", 1);
					session.setAttribute("FileName", segmentID + "_NEW");
					fileList[0] = segmentID + "_NEW";
				}
				
				else
				{
					f = new File(dirPath + segmentID + fileExtension);
					if( f.exists() ) {
						session.setAttribute("FileExist", "true");
						session.setAttribute("FileCount", 1);
						session.setAttribute("FileName", segmentID);
						fileList[0] = segmentID;
					}
				}
			}
			
			// this is a status type directory. Check if multiple files exist.
			else
			{			
				String fileName = "S" + segmentID;
				
				// count the number of files that begin with the file name.
				File dir = new File(dirPath);
				String[] children = dir.list();
	
				// Either the directory does not exist or is not a directory
				if (children == null)
				{
					if (testCall)
						LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: Audio Directory does not exist"));
					status = "E";
				}
	
				// loop thru the files
				else
				{ 
					for (int i=0; i<children.length; i++)
					{
						// Get any files that start with the segment id.
						newFlag = false;
						String dirfile = children[i];
						if( dirfile.startsWith(fileName + ".") || dirfile.startsWith(fileName + "_"))
						{
							dirfile = dirfile.replace(fileExtension, "");
							if( dirfile.contains("_NEW"))
							{
								// replace any segment id files with the new file.
								for( int t=0; fileList[t] != ""; t++ )
								{
									if( fileList[t].compareToIgnoreCase(dirfile.substring(0, fileList[t].length())) == 0)
									{
										fileList[t] = dirfile;
										newFlag = true;
										break;
									}
								}
								
								if( !newFlag )
								{
									fileList[count] = dirfile;
									count++;									
								}
							}
							
							// not a _NEW file, just add it.
							else
							{
								fileList[count] = dirfile;
								count++;
							}
						}
					}
					 
					// found a file that matches
					if( count != 0 )
					{
						session.setAttribute("FileExist", "true");
						session.setAttribute("FileCount", count);
						session.setAttribute("FileList", fileList);
						
						// count the number of VDN messages
					    int VDNCount = 0;
					    for (int i=0; fileList[i] != ""; i++)
					    {
					        if (fileList[i].contains("_"))
					        {
								VDNList[VDNCount] = fileList[i].substring(fileList[i].indexOf('_') + 1);
								if( VDNList[VDNCount].compareTo("NEW") != 0 )
									VDNCount++;
					        }
					    }
	
						// Set the current VDN location
						session.setAttribute("VDNIndex", 1);
						session.setAttribute("VDNCount", VDNCount);
						session.setAttribute("VDNList", VDNList);
	
						// get the first file name in the list
						fileName = fileList[0];
						session.setAttribute("FileName", fileName);
	
						// get the first VDN in the list
						VDN = VDNList[0];
						session.setAttribute("CurrentVDN", VDN);
	
						// get the associated db data
						try {
					    	/* create data access object interface to db */
							int queryTimeout = DbQueryTimeout.getDbQueryTimout(prop);
					    	STATUSMSGDAO eusstatusmsg = new STATUSMSGDAO(jndiName, callid, testCall, queryTimeout);
					    	
					    	/* get specified status message */
					    	dbTimes = eusstatusmsg.getStatusMsgTimes(dirName,dirLanguage,segmentID,VDN);
						}
						
						catch (Exception e) {
							e.printStackTrace();
							status = "E";
						}	
						
						// parse the start time
				    	if( dbTimes[0] == null )
				    	{
							startDateTime = "NOW";
				    	}
				    	
				    	else if( dbTimes[0].compareTo("NOTSET") == 0 )
				    	{
				    		if (testCall)
				    			LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: getStatusMsg failed."));
				    		status = "E";
				    	}
				    	
				    	else
						{
							startDateTime = dbTimes[0];
							startDate = startDateTime.substring(0, 8);
							startTime = startDateTime.substring(8);
							if (testCall)
								LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: startDateTime: ").append(startDateTime));
						}
						
				    	// parse the end time
				    	if( dbTimes[1] == null )
				    	{
							endDateTime = "NEVER";
				    	}
	
				    	else if( dbTimes[1].compareTo("NOTSET") == 0 )
				    	{
				    		if (testCall)
				    			LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: getStatusMsg failed."));
				    		status = "E";
				    	}
				    	
				    	else
						{
				    		endDateTime = dbTimes[1];
							endDate = endDateTime.substring(0, 8);
							endTime = endDateTime.substring(8);
							if (testCall)
								LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: endDateTime: ").append(endDateTime));
						}
						
				    	// set the times in the session.
						session.setAttribute("StartDateTime", startDateTime);
						session.setAttribute("StartDate", startDate);
						session.setAttribute("StartTime", startTime);
						session.setAttribute("EndDateTime", endDateTime);
						session.setAttribute("EndDate", endDate);
						session.setAttribute("EndTime", endTime);
					}
				}
			} // end of status message processing

			// Print the values.
			if (testCall)
			{
				LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: FileExist: ").append(session.getAttribute("FileExist")));
				LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: FileCount: ").append(session.getAttribute("FileCount")));
				for( int i=0; fileList[i] != ""; i++ )
					LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: FileList: ").append(fileList[i]));
				
				LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: FileName: ").append(session.getAttribute("FileName")));
				LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: VDNCount: ").append(session.getAttribute("VDNCount")));
				LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: VDNIndex: ").append(session.getAttribute("VDNIndex")));
				for( int i=0; VDNList[i] != null; i++ )
					LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: VDNList: ").append(VDNList[i]));
				LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: CurrentVDN: ").append(session.getAttribute("CurrentVDN")));
				LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: SegmentID: ").append(session.getAttribute("segmentID")));
	
				LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: StartDate: ").append(startDate));
				LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: StartTime: ").append(startTime));
				LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: EndDate: ").append(endDate));
				LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: EndTime: ").append(endTime));
	
			}
		} // end of segAllowed flag 
		
		session.setAttribute("Status", status);
		if (testCall)
		{
			LOGGER.info(new StringBuffer(logToken).append("checkVoiceSegment: status: ").append(session.getAttribute("status")));
		}
		return;
	}
}

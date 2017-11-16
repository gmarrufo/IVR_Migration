/*
 * Created on June 25, 2007
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
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

/**
 * @author Val Peco
 * 
 * This handler written for EUS, checks if an application directory exists.
 * 
 * Aug 04, 2011 SCS added logic to get the allowed_segments and the blocked_segments from the properties file
 * 				and store them in the session as dirAllowedSegments and dirBlockedSegments.
 * Aug 08, 2011 SCS added logic to get the create_mode=ADVANCED or BASIC and specify_VDN=Y or N and store them in the session
 *              as dirCreateMode and dirSpecifyVDN. Defaults are BASIC and specify_VDN=N.
 * 
 */
public class checkDirectory extends HttpServlet implements Servlet {

	private static Logger LOGGER = Logger.getLogger(checkDirectory.class);

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// get session from Servlet request, created if not existed yet
		HttpSession session = req.getSession(true);
		
		String dirPath = "";
		String segmentPath ="";
		String dirPlayTypeDefault = "";
		String dirPlayTypeChoice = "";
		String bargein = "true";

		String callid = (String) session.getAttribute("callid");

		boolean testCall = ((Boolean) session.getAttribute("testCall")).booleanValue();

		//create the log Token for later use, use StringBuffer to reduce number
		//of String objects
		String logToken = new StringBuffer("[").append(callid).append("] ").toString();

		if (testCall)
			LOGGER.debug(new StringBuffer(logToken).append("checkDirectory: Entering checkDirectory handler"));
		
		Properties callProp = (Properties) session.getAttribute("callProp");

		//get the session variable data
		String dirNum = (String)session.getAttribute("dirNum");

		// Set the session variables for the return data.
		session.setAttribute("dirExists", "false");
				
		// Check if the directory exists.
		String dirName = dirNum + ".appname";
		if( callProp.getProperty( dirName ) != null )
		{
			session.setAttribute("dirExists", "true");
			dirName = callProp.getProperty( dirName );

			String dirLanguage = callProp.getProperty( dirNum + ".language" );
			if( dirLanguage == null )
			{
				LOGGER.warn(new StringBuffer(logToken).append("checkDirectory: setting default dirLanguage to en_US."));
				dirLanguage = "en_US";
			}

			String dirType = callProp.getProperty( dirNum + ".type" );
			if( dirType == null )
			{
				LOGGER.warn(new StringBuffer(logToken).append("checkDirectory: setting default dirType to S."));
				dirType = "S";
			}

			String dirReplicateNow = callProp.getProperty( dirNum + ".replicate_now" );
			if( dirReplicateNow == null )
			{
				LOGGER.warn(new StringBuffer(logToken).append("checkDirectory: setting default dirReplicateNow to Y."));
				dirReplicateNow = "Y";
			}

			dirPlayTypeChoice = callProp.getProperty( dirNum + ".play_type_choice" );
			if( dirPlayTypeChoice == null )
			{
				LOGGER.warn(new StringBuffer(logToken).append("checkDirectory: setting default dirPlayTypeChoice to Y."));
				dirPlayTypeChoice = "Y";
			}

			String dirPassCode = callProp.getProperty( dirNum + ".passcode" );
			if( dirPassCode != null )
			{
				LOGGER.warn(new StringBuffer(logToken).append("checkDirectory: passcode = ").append(dirPassCode));
				if( dirPassCode.toUpperCase().compareToIgnoreCase("NONE") == 0)
				{
					LOGGER.warn(new StringBuffer(logToken).append("checkDirectory: setting passcode to null."));
					dirPassCode = null;
				}
			}
			
			//----------------------------------------------------------------------------
			// Aug 04, 2011 - SCS added the allowed_segments and the blocked_segments 
			//----------------------------------------------------------------------------

			String dirAllowedSegments = callProp.getProperty( dirNum + ".allowed_segments" );
			if( dirAllowedSegments != null )
			{
				LOGGER.info(new StringBuffer(logToken).append("checkDiretory: allowed_segments = ").append(dirAllowedSegments));		
			}
			String dirBlockedSegments = callProp.getProperty( dirNum + ".blocked_segments" );
			if( dirBlockedSegments != null )
			{
				LOGGER.info(new StringBuffer(logToken).append("checkDiretory: blocked_segments = ").append(dirBlockedSegments));	
			}
			//----------------------------------------------------------------------------------------------------------------------------
			// Aug 08, 2011 SCS added logic to get the create_mode=ADVANCED or BASIC and specify_VDN=Y or N and store them in the session.
			//              as dirCreateMode and dirSpecifyVDN. Defaults are BASIC and specify_VDN=N.
			//-----------------------------------------------------------------------------------------------------------------------------
			String dirCreateMode = callProp.getProperty( dirNum + ".create_mode" );
			if( dirCreateMode != null )
			{
				LOGGER.info(new StringBuffer(logToken).append("checkDiretory: create_mode = ").append(dirCreateMode));		
			} else {
				// default is BASIC if not specified
				dirCreateMode = "BASIC";
			}
			String dirSpecifyVDN = callProp.getProperty( dirNum + ".specify_VDN" );
			if( dirSpecifyVDN != null )
			{
				LOGGER.info(new StringBuffer(logToken).append("checkDiretory: specify_VDN = ").append(dirSpecifyVDN));	
			} else {
				// default is N if not specified
				dirSpecifyVDN = "N";
			}
			
			// save the values in the session.
			if (dirAllowedSegments !=null) {
				session.setAttribute("dirAllowedSegments", dirAllowedSegments);		
			}
			if (dirBlockedSegments !=null) {
				session.setAttribute("dirBlockedSegments", dirBlockedSegments);		
			}			
			session.setAttribute("dirCreateMode", dirCreateMode);						
			session.setAttribute("dirSpecifyVDN", dirSpecifyVDN);	
			session.setAttribute("dirPassCode", dirPassCode);			
			session.setAttribute("dirLanguage", dirLanguage);
			session.setAttribute("dirType", dirType);
			session.setAttribute("dirReplicateNow", dirReplicateNow);
			session.setAttribute("dirName", dirName);

			dirPlayTypeDefault = callProp.getProperty( dirNum + ".play_type_default" );
			
			// Set up the default bargein setting.
			if( dirPlayTypeDefault != null )
			{
				if( dirPlayTypeDefault.compareToIgnoreCase("F") == 0)
				{
					bargein = "false";
				}
			}

			session.setAttribute("Bargein", bargein);
			session.setAttribute("dirPlayTypeDefault", dirPlayTypeDefault);
			session.setAttribute("dirPlayTypeChoice", dirPlayTypeChoice);
			
			Properties prop = (Properties) session.getServletContext().getAttribute("globalProp");
			String directPath = prop.getProperty("directAudioFilePath");

			// Set up the directory path.
			if( dirType.compareTo("V") == 0 )
			{
				segmentPath = "/prompt/" + dirName + "/";
				dirPath = directPath + segmentPath;

				File checkDir = new File(dirPath);
				if( !checkDir.exists() )
				{
					checkDir.mkdir();
				}

				dirPath = dirPath + dirLanguage + "/";
				checkDir = new File(dirPath);
				if( !checkDir.exists() )
				{
					checkDir.mkdir();					
				}				
			}
			else
			{
				segmentPath = "/status/" + dirName + "/";
				dirPath = directPath + segmentPath;

				File checkDir = new File(dirPath);
				if( !checkDir.exists() )
				{
					checkDir.mkdir();
				}

				dirPath = dirPath + dirLanguage + "/";
				checkDir = new File(dirPath);
				if( !checkDir.exists() )
				{
					checkDir.mkdir();					
				}				
			}

			session.setAttribute("segmentPath", segmentPath);
			session.setAttribute("dirPath", dirPath);
		}

		// Print out the values.
		// Aug 04 - SCS Added logging of new dirAllowedSegments and dirBlockedSegments
		// Aug 08 - SCS Added logging of the new dirCreateMode and dirSpecifyVDN	
		
		if (testCall)
		{
			LOGGER.debug(new StringBuffer(logToken).append("checkDirectory: segmentPath: " + segmentPath));
			LOGGER.debug(new StringBuffer(logToken).append("checkDirectory: dirPath: " + dirPath));
			LOGGER.debug(new StringBuffer(logToken).append("checkDirectory: dirPlayTypeChoice: " + dirPlayTypeChoice));
			LOGGER.debug(new StringBuffer(logToken).append("checkDirectory: dirPlayTypeDefault: " + dirPlayTypeDefault));
			LOGGER.debug(new StringBuffer(logToken).append("checkDirectory: dirAllowedSegments: " + session.getAttribute("dirAllowedSegments")));
			LOGGER.debug(new StringBuffer(logToken).append("checkDirectory: dirBlockedSegments: " + session.getAttribute("dirBlockedSegments")));
			LOGGER.debug(new StringBuffer(logToken).append("checkDirectory: dirCreateMode: " + session.getAttribute("dirCreateMode")));
			LOGGER.debug(new StringBuffer(logToken).append("checkDirectory: dirSpecifyVDN: " + session.getAttribute("dirSpecifyVDN")));
			
		}

		return;
	}
}

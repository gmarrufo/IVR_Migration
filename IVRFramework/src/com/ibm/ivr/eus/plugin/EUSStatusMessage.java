package com.ibm.ivr.eus.plugin;


import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.ibm.ivr.framework.controller.HotSwapPlugin;
import com.ibm.ivr.framework.model.CallRoutingType;
import com.ibm.ivr.framework.model.SubMenuType;
import com.ibm.ivr.framework.plugin.PluginException;
import com.ibm.ivr.framework.plugin.StatusMessage;
import com.ibm.ivr.framework.utilities.CallRoutingHelper;
import com.ibm.ivr.eus.dao.STATUSMSGDAO;
import com.ibm.ivr.eus.dao.STATUSMSGDTO;
import com.ibm.ivr.eus.data.CommonValues;
import com.ibm.ivr.eus.data.DbQueryTimeout;

public class EUSStatusMessage implements
		com.ibm.ivr.framework.plugin.StatusMessagePlugin {

	/**
	 * The private log4j logger
	 */
	
	private static Logger LOGGER = Logger.getLogger(EUSStatusMessage.class);

	@Override

	// Status Plugin code
	//
	// History: 
	// Feb 07, 2011 - SCS  - modified to 
	//   1) remove check to only play audio for M if selection is null
	//   2) add check to never play audio for a choice (selection is not null)
	//   3) add logic to process for PromptPlay when menu name == XFER_MENU - which is being chgd to a PromptPlay type
	//         in XFER_MENU case need to build the menuLevel based on session variable selection and build override name
	//
	// May 31, 2011  - SCS ONLY for NULL selections - need to set: 
	// status_avoided to N if no status message is returned and set to Y if a status message is specified. 
	//
	// July 07, 2011 - SCS added logic to test for NO_STATUS_MESSAGES and added overloaded getStatusMessage to accommodate 
	// free standing status messages
	// 
	// July 27, 2011 - SCS Defect 170604 - modified to add back in selection logic
	//
	// Oct 03, 2011 - SCS added logic to use lookupAppName for query - set to callFlowName passed in and overridden with statusMsgAppName if it exists
	//
	// Jan 13, 2012 - SCS modified status_avoided logic to 1) only set to Y (never set back to N) 2) set status_avoided Only for NO selection/choice, and free standing
	//					3) do not set for Choice/selection even if found 
	//					core framework is being modified to turn off and on if needed for choices
	// 
	//
	
	public StatusMessage getStatusMessage(String callFlowName,
			String menuName, String selection, HttpSession session)
			throws PluginException {

			
		 
		boolean keepGoing = true;
		boolean testCall = ((Boolean) session.getAttribute("testCall")).booleanValue();
		String callid = (String) session.getAttribute("callid");
		String lookupAppName = callFlowName;
		
		// May 31, 2011 - SCS added to keep track of status avoided 
		String status_avoided = "N";
		boolean set_status_avoided = false;

		//create the log Token for later use, use StringBuffer to reduce number
		//of String objects
		String logToken = new StringBuffer("[").append(callid).append("] ").toString();
		if (testCall) {
			LOGGER.info(new StringBuffer(logToken).append("***  EUSStatusMessage PlugIn called "));
			LOGGER.info(new StringBuffer(logToken).append("***  Inputs: callFlowName = ").append(callFlowName).append(" menuName = ").append(menuName)
					.append(" selection = ").append(selection));
		}
		Properties prop = (Properties) session.getServletContext().getAttribute("globalProp");
		String fileExtension = prop.getProperty("audioFileExtension");
		String jndiName = prop.getProperty(CommonValues.EUS_DATA_SOURCE_PROPERTY);
		
		StatusMessage rtnStatusMsg = new StatusMessage(null, true);	
//		String sessionID = session.getId();	

		STATUSMSGDTO [] statusMessagesDtoTable = null;
		boolean haveMenuLevel;
		boolean stopNow = false;     // set if NO_STATUS_MESSAGES is set to true/TRUE for this app
		
		//-----------------------------------------------------------------------
		// July 07, 2011 - SCS added test to see if the session variable NO_STATUS_MESSAGES set to TRUE
		//-----------------------------------------------------------------------
		if ((String) session.getAttribute("NO_STATUS_MESSAGES") != null) {
			String noStatusMessages = (String) session.getAttribute("NO_STATUS_MESSAGES");
			if (testCall) {
				LOGGER.info(new StringBuffer(logToken).append("*** LOOKHERE have session var NO_STATUS_MESSAGES = ").append(noStatusMessages).append(" "));
			}
			if (noStatusMessages.compareToIgnoreCase("true") == 0 ) {
				stopNow = true;
			}
			else {
				stopNow = false;
			}
			
		} else {
			stopNow = false;
			if (testCall) {
				LOGGER.debug(new StringBuffer(logToken).append("*** session varaible NO_STATUS_MESSAGES does not exist - so carry on "));
			}
		}	// at this point stopNow is set if NO_STATUS_MESSAGES == TRUE
		
		// Defect 170604 check if in recursive menu, if so we don't want to play the status message again
		// the plugin saves the last menu name in session if it does not exist; if the variable exists and matches the current menu name
		// the plugin will return NULL when the selection parameter is NULL.
		// Rationale is that the FW will call the plugin multiple times for the same menu - once for each possible selection
		// It uses the audio URLs returned to build the page with the appropriate messages on selections
		// The same menu will be called again when a caller selects an option but in this case the selection parameter will be set to NULL
		// We do not want to play the same status message again so we will return null when current menuName is same as last and selection is NULL
		String lastMenuName = (String) session.getAttribute("__SM_LAST_MENU");
		if (lastMenuName != null) {
			if (lastMenuName.equals(menuName)) {
				if (selection == null) {
					stopNow = true;
					if (testCall) {
						LOGGER.info(new StringBuffer(logToken).append("*** stopping as selection is NULL and __SM_LAST_MENU same as last one; menuName = ")
							.append(menuName).append("; __SM_LAST_MENU = ").append(lastMenuName));
					}
				}
			} else {
				session.setAttribute("__SM_LAST_MENU", menuName);
				if (testCall)
					LOGGER.info(new StringBuffer(logToken).append("*** setting __SM_LAST_MENU =  ").append(menuName));
			}
		} else {
			session.setAttribute("__SM_LAST_MENU", menuName);
			if (testCall)
				LOGGER.info(new StringBuffer(logToken).append("*** setting __SM_LAST_MENU =  ").append(menuName));
		}
		
		//----------------------------------------------------------------------------------------
		// July 07, 2011 added in test on stopNow`which is set if NO_STATUS_MESSAGES flag was set
		//----------------------------------------------------------------------------------------
		if (stopNow) {  // note: stopNow can also be set by logic above for recusive call - 
			if (testCall) {  
				LOGGER.debug("*** session varaible NO_STATUS_MESSAGES is set to TRUE - so return now with no status message ");
			}
		} else {
		
			// ----------------------------------------------------------------------------------------------------------------------------------
			// Test to see if this is a PromptPlay call -- if so just return - we do not want to play any status messages for PromptPlay submenus
			// ----------------------------------------------------------------------------------------------------------------------------------
			String menuLevel = null;
			String mode = "DTMF";
	
	
			//use menu name and mode as key to locate the actual submenu object in the prebuilt index table
			if ((String) session.getAttribute("mode") != null) {
					mode = (String) session.getAttribute("mode");
			}
			String nMenuNameMode = menuName + mode;
			if (testCall) {
				LOGGER.info(new StringBuffer(logToken).append("*** nMenuNameMode = ").append(nMenuNameMode));
				LOGGER.info(new StringBuffer(logToken).append("*** About to begin check for subMenu type"));
			}
			// get the subMenu type out of sesseion 
			try {
					Hashtable<String, Object> hs = (Hashtable<String, Object>) session.getServletContext().getAttribute("callRoutingTable");
					String xmlKey = (String) session.getAttribute("xmlKey");
					if (testCall)
						LOGGER.info(new StringBuffer(logToken).append("*** have hs and xmlKey at this point"));
					CallRoutingType iCallRouting = (CallRoutingType) HotSwapPlugin.getConfig(session, xmlKey, hs.get(xmlKey));
					Hashtable<String, Object> iCRHelperHashtable = (Hashtable<String, Object>) session.getServletContext().getAttribute("callRoutingHelperHash");
					if (testCall)
						LOGGER.info(new StringBuffer(logToken).append("*** have iCallRouting = ").append(iCallRouting.toString()).append(",  and iCRHelperHashtable at this point"));		
					CallRoutingHelper iCRHelper = (CallRoutingHelper) HotSwapPlugin.getConfig(session, xmlKey+":helper",iCRHelperHashtable.get(xmlKey));
					if (testCall)
						LOGGER.info(new StringBuffer(logToken).append("*** have iCRHelper at this point = ").append(iCRHelper.toString()));	
					SubMenuType subMenu = iCRHelper.getMenu(nMenuNameMode);
					if (testCall)
					LOGGER.info(new StringBuffer(logToken).append("subMenu.toString() = ").append(subMenu.toString()));
						String subMenuType = subMenu.getType_();
					if (testCall) {
						LOGGER.info(new StringBuffer(logToken).append("*** subMenuType = ").append(subMenuType));
					}
					
					// test the subMenu type
					if ( subMenuType == null || subMenuType.equals(null)) {
						// regular menu
						keepGoing = true;
						if (testCall) {
							LOGGER.info(new StringBuffer(logToken).append("*** subMenu.getType is null - this is a regular subMenu"));
						}
					} else {
						// test for PromptPlay and NOT the XFER_MENU case
						if ((subMenuType.compareToIgnoreCase("PromptPlay") == 0))  {
							
		// ---------------- July 27, 2011 - SCS -  Removed all logic related to testing for XFER_MENU	-------------------				
		//					if (!(menuName.equalsIgnoreCase("XFER_MENU"))) {
		//						// not the XFER_MENU so do not want to process
		//						keepGoing = false;
		//						if (testCall) {
		//							LOGGER.info(new StringBuffer(logToken).append("*** subMenu.getType is PromptPlay - return with no status message"));
		//						}  
		//					} else {
		//						// this is the XFER_MENU so test for selection in session 
		//						String sessionSelection = ((String) session.getAttribute("selection")); 
		//						if (sessionSelection != null){
		//							// XFER_MENU
		//							keepGoing = true;
		//							if (testCall) {
		//								LOGGER.info(new StringBuffer(logToken).append("*** PromptPlay but XFER_MENU so Continueing with menuName= " + menuName));
		//							}
		//						} else {
		//							// XFER_MENU - but selection is not provided 
		//							keepGoing = false;
		//							if (testCall) {
		//								LOGGER.warn(new StringBuffer(logToken).append("*** XFER_MENU but no selection session variable was set - can not get Status msg"));
		//							}
		//						}
		//					}
		// ---------------------------------- end of logic related to testing for XFER_MENU	-------------
							
		// -------------------July 27, 2011 - SCS -  replaced with this ---------------------------------------
							keepGoing = false;
							if (testCall) {
								LOGGER.info(new StringBuffer(logToken).append("*** subMenu.getType is PromptPlay - return with no status message"));
							}  
		// ------------------ end of replace code ----------------------------------------------------------------
						} // end of test for PromptPlay
					}
				} catch (Exception e) {	
					LOGGER.error(new StringBuffer(logToken).append("*** Failed to get subMenu type from session ").append(e.getMessage()));
				} // end of logic to see if SubMenu is type PromptPlay
	
			
			// set menu level - if it does not exist then we can not pull messages
			if ((String) session.getAttribute("menuLevel") != null) {
				menuLevel = (String) session.getAttribute("menuLevel");
				if (testCall) {
					LOGGER.info(new StringBuffer(logToken).append("*** LOOKHERE have session var menuLevel = ").append(menuLevel).append(" can continue if not a PromptPlay"));
				}
				haveMenuLevel = true;
			}
			else {
				haveMenuLevel = false;
				LOGGER.warn("*** Do not have session var menuLevel - so can not get Status messages ");
			}	
			
			if ((haveMenuLevel) && (keepGoing)) {	
				// -----------------------------------------------------------------------------------------------------
				// At this point we have both a menuLevel and this is a regular menu (Not a promptPlay) 
				// -----------------------------------------------------------------------------------------------------

				// get VDN 
				String VDN = ((String) session.getAttribute("DNIS"));
				if (testCall) {
					LOGGER.info(new StringBuffer(logToken).append("*** from session DNIS/VDN = ").append(VDN));
				}
				
				// check for statusMessagesDtoTable session variable			
				if ((STATUSMSGDTO []) session.getAttribute("statusMessagesDtoTable") != null) {
	
					// use session variable table
					statusMessagesDtoTable = (STATUSMSGDTO []) session.getAttribute("statusMessagesDtoTable"); 
					if (testCall) {
						LOGGER.info(new StringBuffer(logToken).append("*** using the stored the statusMessagesDtoTable from the session "));
					}
	
				} 
				else {
				// session variable does not exist need to create table
				   try {
	//					LOGGER.info(new StringBuffer(logToken).append("*** about to try to create a new statusMessagesDtoTable *** ");
					    			    	
					    /* create data access object interface to statusMessages db */
					   int queryTimeout = DbQueryTimeout.getDbQueryTimout(prop);
					   if (testCall) {
					   		LOGGER.info(new StringBuffer(logToken).append("*** calling: STATUSMSGDAO with queryTimeout = ").append(queryTimeout));
					   	}
				    	STATUSMSGDAO statusmsgdao = new STATUSMSGDAO(jndiName, callid, testCall, queryTimeout);
	//					LOGGER.info(new StringBuffer(logToken).append("*** created new statusmsgdao *** ");
				    	
				    	// Oct 3, 2011 SCS added logic to override lookupAppName if (new) statusMsgAppName exists
				    	if ((String) session.getAttribute("statusMsgAppName") != null) {
								lookupAppName = (String) session.getAttribute("statusMsgAppName");
						}
				    	
				        /* lookup finds all status message rows for this application */
				    	statusMessagesDtoTable = statusmsgdao.getAllStatusMessages(lookupAppName, VDN);	
				    	if (testCall) {
				    		LOGGER.info(new StringBuffer(logToken).append("*** executed:  statusmsgdao.getAllStatusMessages(lookupAppName, VDN) *** "));
				    		LOGGER.info(new StringBuffer(logToken).append("*** Where: lookupAppName= " + lookupAppName + " and VDN= " + VDN));		
				    	}
	
				    	
				       	if (statusMessagesDtoTable != null) {
				       		// store in session 
				       		session.setAttribute("statusMessagesDtoTable", statusMessagesDtoTable);	
				       	}
				       	// July 11, 2011 - SCS need to indicate that there are no stored messages here
				       	else {
				       		session.setAttribute("NO_STATUS_MESSAGES","true");
				       	}
	
					} catch (Exception e) {	
						LOGGER.error(new StringBuffer(logToken).append("*** Failed to create NEW statusMessagesDtoTable - DB failure ").append(e.getMessage()));
					}
				}	
				
				//--------------------------------------------
				// verify we have status messages to look at to continue
				//--------------------------------------------
		       	if (statusMessagesDtoTable != null) {
		       			       		
				    // if in here - have table to work with		
		       		int tableSize = statusMessagesDtoTable.length;
		       		
		       		
		       		//-----------------------------------------------------------------
		       		// Work on determining status overrides - 1) name of and 2)if it exists
		       		//-----------------------------------------------------------------
		       		String overrideVarName = "status_override_main";
		       		String overrideValue = "";
		       		if (testCall)
		       			LOGGER.info(new StringBuffer(logToken).append("*** have a statusMessagesDtoTable to work with.  Length is = ").append(tableSize));
					
	
		       		
		       		// Feb 07, 2011 new code chgd to provide status for selection == null only and for special case XFERMenu - old comment - keep for ref.
		       		
		       		// ------------------------------------------------------------------------------------
		       		// July 27, 2011 - SCS - modified to support selections on regular menus
		       		//
		       		// the selection here is the one passed in from framework not the session value
		       		// -----------------------------------------------------------------------------------
		       		if (selection != null)  {
		       			// have a selection - will need to adjust menuLevel adding in selection

		       			// removed this code 
		       			//-----------------
		       			// keepGoing = false;
		       			// if (testCall)
			       		//	LOGGER.info(new StringBuffer(logToken).append("*** selection is NOT null so keepGoing is set to FALSE "));
		       			
			       		// added this code instead
		       			//-------------------------
						// update menuLevel for selection specified

		       			// do not want to set for a selection 
						set_status_avoided = false;
	    				if (menuLevel.equalsIgnoreCase("M")) {
	    					menuLevel = selection;		     								
	    				}
	    				else {
	    					// Menu level is not M  (defect 170604 needed _ btwn selections)
	    					menuLevel = menuLevel.concat("_").concat(selection);   					
	    				}
	    				// always need the _ menulevel in the selection case to determine the override names
	   					overrideVarName = overrideVarName.concat("_");
						overrideVarName = overrideVarName.concat(menuLevel);
					
						// ------------ end of replacement code 07-27-2011
						
		       		} else {
		       			//selection is NULL (this means the passed in selection not session selection - so keep going 
		       			
		       			// May 31, 2011 - SCS will need to set the status_avoided as this is for the selection is null case
		       			// status_avoided is set to "N" at this point will reset to "Y" if a status messages is found. 
		       			
		       			
		       			// 	will only set at end if the value is  yes	
						set_status_avoided = true;
		       			
		       			if (menuLevel.equalsIgnoreCase("M") ) {
		       				// menuLevel is M so already have name
	    					menuLevel = "M";

		       			}
		       			else {
		       				// menuLevel is not M so determine override name 
		  					overrideVarName = overrideVarName.concat("_");
							overrideVarName = overrideVarName.concat(menuLevel);
							
		       			}
						
		       			//------------------------------------------------------------------------------------
		       			// July 27, 2011 - SCS - commented out special processing for XFER_MENU - no longer needed
		       			
		       			// ----------------------------------
		       			// special processing for XFER_MENU
		       			// ----------------------------------
		      // 			if (menuName.equalsIgnoreCase("XFER_MENU")) {
	
		      // 				String sessionSelection = ((String) session.getAttribute("selection"));
	
		      // 				overrideVarName = "status_override_main_";
		       				
		      // 				if (menuLevel.equalsIgnoreCase("M")) {
		      //					menuLevel = sessionSelection;
					
		    //				} else {
		    					
		    					// Menu level is not M
		    //    				menuLevel = menuLevel.concat(sessionSelection);   						    					
		    //				}
		       				// need to add session selection to name 
		    //   				overrideVarName = overrideVarName.concat(menuLevel);
	
		    //   				if (testCall) {
		    //    				LOGGER.info(new StringBuffer(logToken).append("in XFER_MENU, sessionSelection =  ").append(sessionSelection)
		    //    						.append(" , ending menuLevel = ").append(menuLevel)
		    //    						.append(" , overrideVarName = ").append(overrideVarName));
		    //    			}
		    //   			} // end of XFER_MENU special processing 
		    
		    // ------------------------------- end of commenting out of XFER_MENU special logic
		       			
		       		}  // framework passed in selection is null (ie for regular menu)
		       				       		
	    			if (testCall) {
	    				LOGGER.info(new StringBuffer(logToken).append("*** adjusted menuLevel= ")
	    						.append(menuLevel).append(" , keepGoing= ").append(keepGoing)
	    						.append(" , overrideVarName= ").append(overrideVarName));
	    			}
	    			
	    			//--------------------------------------------------------------------
	    			// get overrides from session 
	    			//	 
	    			//---------------------------------------------------------------------------------------
	    			// Note:  keepGoing will be FALSE at this point if the framework called for a PromptPlay
	    			// 			(OLD usage before July27, 2011 chg -- 
	    			//				keepGoing was set to false if a selection was specified or PromptPlay was not XFER_MENU)
	    			//---------------------------------------------------------------------------------------
	    			
	    			if ((keepGoing) &&  ((String) session.getAttribute(overrideVarName) != null)) {
	    				overrideValue = (String) session.getAttribute(overrideVarName);
	    				if (testCall) {
	    					LOGGER.info(new StringBuffer(logToken).append("*** have override value session var = ").append(overrideValue));
	    				}
	    				if (overrideValue.length()>0) {
	    					menuLevel = overrideValue;
	    				// Defect 170604 - needed to add line below to fix menuLevel is more than 1 level deep  	
	    				} else {
	    					menuLevel = menuLevel.replace("_", "");
	    				}
					}
					else {
					    // Defect 170604 - needed to add line below to fix menuLevel is more than 1 level deep 
						menuLevel = menuLevel.replace("_", "");
						if (testCall) {
							LOGGER.info(new StringBuffer(logToken).append("*** Do not have session var to override this or keepGoing is False "));
						}
					}		
	    			//--------------------------------------------
	    			// menuLevel (local) is now the messageID
	    			//--------------------------------------------
	    			if (keepGoing) {
	    				if (testCall)
	    					LOGGER.info(new StringBuffer(logToken).append("after overrides -- the value of menuLevel is now = ").append(menuLevel));	
	
	    				for (int i=0; i < tableSize; i++) {
	
	    					// look for Menulevel
	    					// if match check VDN if null done
	    					// if not null keep looking
	
	    					//	    			LOGGER.info(new StringBuffer(logToken).append("*** In Loop looking at entry at index = " + i);
	    					// check  menu level
	    					
	    					if (testCall) {
								LOGGER.info(new StringBuffer(logToken).append("*** \n *** DB info: MESSAGEID: " + statusMessagesDtoTable[i].getMessageID() +
										" , VDN: " + statusMessagesDtoTable[i].getVDN() +
										" , LANG: " + statusMessagesDtoTable[i].getLanguage() + " . " ) );
							}
	    					
	    					if (statusMessagesDtoTable[i].getMessageID().equals(menuLevel)) { 
	    						if (testCall) {
	    							LOGGER.info(new StringBuffer(logToken).append("*** menu Levels match - next will test VDN/DNIS = ").append(VDN));
	    							LOGGER.info(new StringBuffer(logToken).append("statusMessagesDtoTable[i].getVDN() = ").append(statusMessagesDtoTable[i].getVDN()));
	    						}
	
	    						// check VND
	
	    						//		    			if ( (statusMessagesDtoTable[i].getVDN() == null)
	    						//		    				  ||  (statusMessagesDtoTable[i].getVDN().equals(""))
	    						//		    				  || (statusMessagesDtoTable[i].getVDN().equals(VDN))
	    						//		    			    )  {
	
	    						if ( (statusMessagesDtoTable[i].getVDN() == null)
	    								||  (statusMessagesDtoTable[i].getVDN().equals(""))
	    								|| (statusMessagesDtoTable[i].getVDN().equals(VDN))
	
	    						)  {	
	    							
	    							if (testCall) {
	    									LOGGER.debug(new StringBuffer(logToken).append("*** VDN matches  -- next test for LANG "));
	    							}
	
	    							// check LANG
	    							if ( (statusMessagesDtoTable[i].getLanguage().equals(null)) ||
	    									(statusMessagesDtoTable[i].getLanguage().equals((String)session.getAttribute("LANG")) )
	    							) {
	    								if (testCall) {
	    									LOGGER.info(new StringBuffer(logToken).append("*** LANG match -- next will work on time checks  "));
	    								}
	
	    								// check times		    						
	    								Timestamp curTS = new Timestamp((new java.util.Date()).getTime());
	    								String curDateTime = new SimpleDateFormat("yyyyMMddHHmm").format(curTS);
	
	
	    								String startDateTime = statusMessagesDtoTable[i].getStartTime(); 
	    								String endDateTime = statusMessagesDtoTable[i].getEndTime();
	
	    								if (startDateTime == null || endDateTime == null) {
	    									if (testCall)
	    										LOGGER.info(new StringBuffer(logToken).append("Have null times"));		    		    				
	    								} else {
	    									if (testCall)
	    										LOGGER.info(new StringBuffer(logToken).append("Current Time = ").append(curDateTime.toString())  
	    												.append(" ==>> Start Time = ").append(startDateTime.toString()) 
	    												.append("  End Time = ").append(endDateTime.toString()));
	    								}
	    								if ( (startDateTime == null || (startDateTime.compareTo(curDateTime) <= 0  ))
	    										&& 	
	    										(endDateTime == null || (endDateTime.compareTo(curDateTime) > 0  ))
	    								) {
	    									// This meets all criteria - have a status message to play		    							
	    									// set up data to return
	    									// LOGGER.info(new StringBuffer(logToken).append("*** meets time criteria ");
	
	     									/*
	    									 * Can specify AudioURL two ways 
	    									 * 
	    									 * 1) just file name and framework will calc the rest
	    									 * 
	    									 * 2) for URL 
	    									 *  
	    									 *  use audioFileRoot from global.properteis 
	    									 *  append application name 
	    									 *  append "/status/" then append lang (in session var?)
	    									 *  file name 'S' + menuLevel + "_" + VDN + "." audioFileExtenstion from global.properties 
	    									 */
	    									//				    						String audioURL = statusMessagesDtoTable[i].getMessageID();
	    									
	    									// May 31, 2011 - SCS need to set status_avoided = "Y"
	    									// will only set if the set_status_avoided is also set to true
	    									status_avoided = "Y";
	    									
	    									
	    									StringBuffer audioURL = new StringBuffer("S" + menuLevel);
	
	    									if (statusMessagesDtoTable[i].getVDN() != null)
	    										audioURL = audioURL.append("_" + VDN);
	    									// looked in framework fileExtension includes the . dot)
	    									audioURL.append(fileExtension);
	    									rtnStatusMsg.setAudioURL(audioURL.toString());
	    									if (statusMessagesDtoTable[i].isBargein()) {
	    										rtnStatusMsg.setBargeinFlag(true);
	    									}
	    									else {
	    										rtnStatusMsg.setBargeinFlag(false);
	    									}
	    								}  // end of times check
	    							}  // end of lang check				    					
	    						}  // end of VND check
	    					} // end of menu level check			        		
	    				}  // end of for loop	
	    			}  // end of keepGoing
		       	}  // end of have status msgs table to work with - 
			}  // end of have menuLevel AND menu is either a regular menu or XFER_MENU
			
			// add logic to set (String) session.setAttribute("status_avoided", "Y") in case where we are returning a status message 
			// AND the set_status_avoided is also set to true (
			// and to not touch for all other cases 
			if ( set_status_avoided && (status_avoided.equals("Y"))) {
				session.setAttribute("status_avoided", "Y");
			}
			
	}  // end of stopNow - NO_STATUS_MESSAGES flag was set
		
		if (testCall) {
	//		LOGGER.info(new StringBuffer(logToken).append("****** returning StatusMsg =  " + rtnStatusMsg.toString());
			LOGGER.info(new StringBuffer(logToken).append("****** returning StatusMsg.audioURL =  ").append(rtnStatusMsg.getAudioURL()));
			LOGGER.info(new StringBuffer(logToken).append("****** returning StatusMsg.bargeinFlag =  ").append(rtnStatusMsg.isBargeinFlag()));
			LOGGER.info(new StringBuffer(logToken).append("****** set_status_avoided =  ").append(set_status_avoided).append(" if true, then set status_avoided = " + status_avoided));
			LOGGER.info(new StringBuffer(logToken).append("*********************************  EUSStatusMessage PlugIn is done"));
		}

	    return rtnStatusMsg;
	}
	
	
	//---------------------------------------------------------------------------------------
	// --------------------------------------------------------------------------------------
	// July 11, 2011 - SCS added this new method to support freestanding status messages
	//---------------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------------
	
	public StatusMessage getStatusMessage(String callFlowName,
			String statusMessageID, HttpSession session)
			throws PluginException {
	 
		boolean keepGoing = true;
		boolean testCall = ((Boolean) session.getAttribute("testCall")).booleanValue();
		String callid = (String) session.getAttribute("callid");
		
		String status_avoided = "N";
//		boolean set_status_avoided = true;   01-16-2012 SCS do not need this as we always set if a message is found
		String lookupAppName = callFlowName;

		//create the log Token for later use, use StringBuffer to reduce number
		//of String objects
		String logToken = new StringBuffer("[").append(callid).append("] ").toString();
		if (testCall) {
			LOGGER.info(new StringBuffer(logToken).append("***  EUSStatusMessage PlugIn called for Free standing status message"));
			LOGGER.info(new StringBuffer(logToken).append("***  Inputs: callFlowName = ").append(callFlowName).append(" statusMessageID = ").append(statusMessageID));
		}
		Properties prop = (Properties) session.getServletContext().getAttribute("globalProp");
		String fileExtension = prop.getProperty("audioFileExtension");
		String jndiName = prop.getProperty(CommonValues.EUS_DATA_SOURCE_PROPERTY);
		
		StatusMessage rtnStatusMsg = new StatusMessage(null, true);	

		STATUSMSGDTO [] statusMessagesDtoTable = null;
		boolean stopNow;     // set if NO_STATUS_MESSAGES is set to true/TRUE for this app
		String menuLevel = null;
		
		//-----------------------------------------------------------------------
		// July 07, 2011 - SCS added test to see if the session variable NO_STATUS_MESSAGES set to TRUE
		//-----------------------------------------------------------------------
		if ((String) session.getAttribute("NO_STATUS_MESSAGES") != null) {
			String noStatusMessages = (String) session.getAttribute("NO_STATUS_MESSAGES");
			if (testCall) {
				LOGGER.info(new StringBuffer(logToken).append("*** LOOKHERE have session var NO_STATUS_MESSAGES = ").append(noStatusMessages).append(" "));
			}
			if (noStatusMessages.compareToIgnoreCase("true") == 0 ) {
				stopNow = true;
			}
			else {
				stopNow = false;
			}			
		}
		else {
			stopNow = false;
			if (testCall) {
				LOGGER.debug("*** session varaible NO_STATUS_MESSAGES does not exist - so carry on ");
			}
		}		
		//----------------------------------------------------------------------------------------
		// test on stopNow`which is set if NO_STATUS_MESSAGES flag was set
		//----------------------------------------------------------------------------------------
		if (stopNow) {
			if (testCall) {
				LOGGER.debug("*** session varaible NO_STATUS_MESSAGES is set to TRUE - so return now with no status message ");
			}
		} else {
						
			// set menu level - if it does not exist then we can not pull messages
			if (statusMessageID != null) {
				
				if (testCall) {
					LOGGER.info(new StringBuffer(logToken).append("*** LOOKHERE have session var statusMessageID = ").append(statusMessageID).append(" can continue "));
				}
				keepGoing = true;
			}
			else {
				keepGoing = false;
				LOGGER.warn("*** Do not have a statusMessageID - so can not get Status messages ");
			}		
			if (keepGoing) {			
				// get VDN 
				String VDN = ((String) session.getAttribute("DNIS"));
				if (testCall) {
					LOGGER.info(new StringBuffer(logToken).append("*** from session DNIS/VDN = ").append(VDN));
				}
				
				// check for statusMessagesDtoTable session variable			
				if ((STATUSMSGDTO []) session.getAttribute("statusMessagesDtoTable") != null) {
	
					// use session variable table
					statusMessagesDtoTable = (STATUSMSGDTO []) session.getAttribute("statusMessagesDtoTable"); 
					if (testCall) {
						LOGGER.info(new StringBuffer(logToken).append("*** using the stored the statusMessagesDtoTable from the session "));
					}
				} 
				else {
				// session variable does not exist need to create table
				   try {
	//					LOGGER.info(new StringBuffer(logToken).append("*** about to try to create a new statusMessagesDtoTable *** ");
					    			    	
					    /* create data access object interface to statusMessages db */
					   	int queryTimeout = DbQueryTimeout.getDbQueryTimout(prop);
					   	if (testCall) {
					   		LOGGER.info(new StringBuffer(logToken).append("*** calling: STATUSMSGDAO with queryTimeout = ").append(queryTimeout));
					   	}
				    	STATUSMSGDAO statusmsgdao = new STATUSMSGDAO(jndiName, callid, testCall, queryTimeout);
	//					LOGGER.info(new StringBuffer(logToken).append("*** created new statusmsgdao *** ");
				    	
				    	// Oct 3, 2011 SCS added logic to override lookupAppName if (new) statusMsgAppName exists
				    	if ((String) session.getAttribute("statusMsgAppName") != null) {
								lookupAppName = (String) session.getAttribute("statusMsgAppName");
						}
				    	
				    	
				        /* lookup finds all status message rows for this application */
				    	statusMessagesDtoTable = statusmsgdao.getAllStatusMessages(lookupAppName, VDN);	
				    	if (testCall) {
				    		LOGGER.info(new StringBuffer(logToken).append("*** executed:  statusmsgdao.getAllStatusMessages(lookupAppName, VDN) *** "));
				    		LOGGER.info(new StringBuffer(logToken).append("*** Where: lookupAppName= " + lookupAppName + " and VDN= " + VDN));		
				    	}
	
				    	
				       	if (statusMessagesDtoTable != null) {
				       		// store in session 
				       		session.setAttribute("statusMessagesDtoTable", statusMessagesDtoTable);
	
				       	}
				       	// July 11, 2011 - SCS need to indicate that there are no stored messages here
				       	else {
				       		session.setAttribute("NO_STATUS_MESSAGES","true");
				       	}
				       	
					} catch (Exception e) {	
						LOGGER.error(new StringBuffer(logToken).append("*** Failed to create NEW statusMessagesDtoTable - DB failure ").append(e.getMessage()));
					}
				}	
					    
		       	if (statusMessagesDtoTable != null) {
		       			       		
				    // have table to work with		
		       		int tableSize = statusMessagesDtoTable.length;

//		       		String overrideVarName = "status_override_main";
//		       		String overrideValue = "";
		       		if (testCall)
		       			LOGGER.info(new StringBuffer(logToken).append("*** have a statusMessagesDtoTable to work with.  Length is = ").append(tableSize));
					
	       			
	       			if (statusMessageID.equalsIgnoreCase("MAIN") ) {
	       				// menuLevel is M
    					menuLevel = "M";  
	       			}
	       			else {
	       				menuLevel = statusMessageID;
	       			}
	       				       				       		
		       		
	    			if (testCall) {
	    				LOGGER.info(new StringBuffer(logToken).append("*** adjusted menuLevel= ")
	    						.append(menuLevel).append(" , keepGoing= ").append(keepGoing));
	    			}
	    			
	    			if (keepGoing) {
	
	    				for (int i=0; i < tableSize; i++) {
	
	    					// look for Menulevel
	    					// if match check VDN if null done
	    					// if not null keep looking
	
	    					//	    			LOGGER.info(new StringBuffer(logToken).append("*** In Loop looking at entry at index = " + i);
	    					// check  menu level
	    					
	    					if (testCall) {
								LOGGER.info(new StringBuffer(logToken).append("*** \n *** DB info: MESSAGEID: " + statusMessagesDtoTable[i].getMessageID() +
										" , VDN: " + statusMessagesDtoTable[i].getVDN() +
										" , LANG: " + statusMessagesDtoTable[i].getLanguage() + " . " ) );
							}
	    					
	    					if (statusMessagesDtoTable[i].getMessageID().equals(menuLevel)) { 
	    						if (testCall) {
	    							LOGGER.info(new StringBuffer(logToken).append("*** menu Levels match - next will test VDN/DNIS = ").append(VDN));
	    							LOGGER.info(new StringBuffer(logToken).append("statusMessagesDtoTable[i].getVDN() = ").append(statusMessagesDtoTable[i].getVDN()));
	    						}
	
	    						// check VND
	
	    						//		    			if ( (statusMessagesDtoTable[i].getVDN() == null)
	    						//		    				  ||  (statusMessagesDtoTable[i].getVDN().equals(""))
	    						//		    				  || (statusMessagesDtoTable[i].getVDN().equals(VDN))
	    						//		    			    )  {
	
	    						if ( (statusMessagesDtoTable[i].getVDN() == null)
	    								||  (statusMessagesDtoTable[i].getVDN().equals(""))
	    								|| (statusMessagesDtoTable[i].getVDN().equals(VDN))
	
	    						)  {		    				
	
	    							//		    				LOGGER.info(new StringBuffer(logToken).append("*** VDN matches  -- next test for LANG ");
	
	    							// check LANG
	    							if ( (statusMessagesDtoTable[i].getLanguage().equals(null)) ||
	    									(statusMessagesDtoTable[i].getLanguage().equals((String)session.getAttribute("LANG")) )
	    							) {
	    								if (testCall) {
	    									LOGGER.info(new StringBuffer(logToken).append("*** LANG match -- next will work on time checks  "));
	    								}
	
	    								// check times		    						
	    								Timestamp curTS = new Timestamp((new java.util.Date()).getTime());
	    								String curDateTime = new SimpleDateFormat("yyyyMMddHHmm").format(curTS);
	
	
	    								String startDateTime = statusMessagesDtoTable[i].getStartTime(); 
	    								String endDateTime = statusMessagesDtoTable[i].getEndTime();
	
	    								if (startDateTime == null || endDateTime == null) {
	    									if (testCall)
	    										LOGGER.info(new StringBuffer(logToken).append("Have null times"));		    		    				
	    								} else {
	    									if (testCall)
	    										LOGGER.info(new StringBuffer(logToken).append("Current Time = ").append(curDateTime.toString())  
	    												.append(" ==>> Start Time = ").append(startDateTime.toString()) 
	    												.append("  End Time = ").append(endDateTime.toString()));
	    								}
	    								if ( (startDateTime == null || (startDateTime.compareTo(curDateTime) <= 0  ))
	    										&& 	
	    										(endDateTime == null || (endDateTime.compareTo(curDateTime) > 0  ))
	    								) {
	    									// This meets all criteria - have a status message to play		    							
	    									// set up data to return
	    									// LOGGER.info(new StringBuffer(logToken).append("*** meets time criteria ");
	
	     									/*
	    									 * Can specify AudioURL two ways 
	    									 * 
	    									 * 1) just file name and framework will calc the rest
	    									 * 
	    									 * 2) for URL 
	    									 *  
	    									 *  use audioFileRoot from global.properteis 
	    									 *  append application name 
	    									 *  append "/status/" then append lang (in session var?)
	    									 *  file name 'S' + menuLevel + "_" + VDN + "." audioFileExtenstion from global.properties 
	    									 */
	    									//				    						String audioURL = statusMessagesDtoTable[i].getMessageID();
	    									
	    									// May 31, 2011 - SCS need to set status_avoided = "Y"
	    									// found a message set to true
	    									status_avoided = "Y";

	    									
	    									StringBuffer audioURL = new StringBuffer("S" + menuLevel);
	
	    									if (statusMessagesDtoTable[i].getVDN() != null)
	    										audioURL = audioURL.append("_" + VDN);
	    									// looked in framework fileExtension includes the . dot)
	    									audioURL.append(fileExtension);
	    									rtnStatusMsg.setAudioURL(audioURL.toString());
	    									if (statusMessagesDtoTable[i].isBargein()) {
	    										rtnStatusMsg.setBargeinFlag(true);
	    									}
	    									else {
	    										rtnStatusMsg.setBargeinFlag(false);
	    									}
	    								}  // end of times check
	    							}  // end of lang check				    					
	    						}  // end of VND check
	    					} // end of menu level check			        		
	    				}  // end of for loop	
	    			}  // end of keepGoing
		       	}  // end of have table to work with 
			}  // end of have statusMessageID
			
			// need to add logic to set (String) session.setAttribute("status_avoided", "Y") in case where we are returning a status message and 
			// set to "N if for the null selection, and to not touch for all other cases 
			if ( status_avoided.equalsIgnoreCase("Y") ) {
				session.setAttribute("status_avoided", "Y");
			}
			
	}  // end of stopNow - NO_STATUS_MESSAGES flag was set
		
		if (testCall) {
	//		LOGGER.info(new StringBuffer(logToken).append("****** returning StatusMsg =  " + rtnStatusMsg.toString());
			LOGGER.info(new StringBuffer(logToken).append("****** returning StatusMsg.audioURL =  ").append(rtnStatusMsg.getAudioURL()));
			LOGGER.info(new StringBuffer(logToken).append("****** returning StatusMsg.bargeinFlag =  ").append(rtnStatusMsg.isBargeinFlag()));
			LOGGER.info(new StringBuffer(logToken).append("****** local status_avoided =  ").append(status_avoided).append(" if Y, set in session"));
			LOGGER.info(new StringBuffer(logToken).append("*********************************  EUSStatusMessage PlugIn is done"));
		}

	    return rtnStatusMsg;
	}
	
	
}

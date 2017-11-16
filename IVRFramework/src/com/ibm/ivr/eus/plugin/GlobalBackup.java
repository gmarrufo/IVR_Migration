package com.ibm.ivr.eus.plugin;

import javax.servlet.ServletContext;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.ibm.ivr.framework.plugin.BackupPlugin;
import com.ibm.ivr.framework.plugin.PluginException;

/***************************************
 * GlobalBackup Plugin 
 *
 * - sets previousMenu to be used by framework i.e. where to go for backup cmd
 * - modifies menuLevel to move "up" one level if not already at M
 * 
 *Revision history:
	 * <p>
	 * 
	 * 2011-01-12: initial version
	 * <p>
	 * 
	 * @author Sibyl Sullivan
	 * @version 2011-07-25
	 * 
	 * July 25, 2011 SCS - modified to also set cur_prompt_override=true & cur_prompt=main (or main_1, or what ever)  required to make back up work  
	 *
	 */

public class GlobalBackup implements BackupPlugin {

	
	/**
	 * Generated serialVersionUID for Rational
	 */
	private static final long serialVersionUID = -4243990691584973217L;
	
	/**
	 * The private log4j logger
	 */
	private static Logger LOGGER = Logger.getLogger(GlobalBackup.class);
	
	
	@Override
	public void getPreviousMenu(ServletContext arg0, HttpSession session)
			throws PluginException {
				
		// If MenuLevel exists decrement MenuLevel if not already M (main menu)
		// Menu levels 
		// start as M (main menu) 
		// chg to 1  - if option one is picked then
		// additional options are appended so if at Main menu option one then option 3 was picked on sub menu menuLevel is 13
		
		String menuLevel = "";
		String cur_prompt = "";
		
		// SCS July 25, 2011 - added logic to set new cur_promt to correct value.  
		// Note cur_prompt is in main_1_2 format while menuLevel is in M12 format
		
		LOGGER.debug("*** entered the GlobalBackup plugin *** ");
		if ((String) session.getAttribute("menuLevel") != null) {
			menuLevel = (String) session.getAttribute("menuLevel");
			LOGGER.debug("*** before going up a level the session var menuLevel = "+ menuLevel );
			if (!( (menuLevel.compareToIgnoreCase("M") == 0) || (menuLevel.contains("m")) || (menuLevel.contains("M")) )) {
				// menu is not at M - 
				if (menuLevel.length() > 1) {
					// drop last digit
					menuLevel = menuLevel.substring(0, (menuLevel.length()- 2) );
					cur_prompt = cur_prompt.substring(0, (menuLevel.length()- 3) );
				}
				else {
					// change to "M"
					menuLevel = "M";
					cur_prompt = "main";
				}
			}
			// SCS July 25, 2011 added logic to also set cur_prompt_override=true & cur_prompt in session 
			session.setAttribute("cur_prompt_override", "true");
			session.setAttribute("cur_prompt", cur_prompt);
			session.setAttribute("menuLevel", menuLevel);
			LOGGER.debug("session var menuLevel reset to = " + menuLevel + " , cur_prompt = " + cur_prompt + " , cur_prompt_override = " + session.getAttribute("cur_prompt_override"));
		}
		else {
			LOGGER.warn("menuLevel session variable does not exist - goback cmd treated same as repeat");
		}
		
		//find the previous submenu
		String subMenuName = ((String) session.getAttribute("iSubMenuName"));		
		session.setAttribute("previousMenu", subMenuName);

		LOGGER.debug("*** GoBack.getPreviousMenu() ends with *** previousMenu = " + subMenuName +  " , menuLevel = " + (String) session.getAttribute("menuLevel"));

	}

}

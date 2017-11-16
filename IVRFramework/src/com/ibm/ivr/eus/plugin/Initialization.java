package com.ibm.ivr.eus.plugin;


import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.apache.log4j.PropertyConfigurator;

import com.ibm.ivr.framework.plugin.InitializationPlugin;
import com.ibm.ivr.framework.plugin.PluginException;


/***************************************
 *Initialization Plugin 
 *
 * - creates an empty hashtable of Loggers for application logging
 * 
 *Revision history:
	 * <p>
	 * 
	 * 2010-11-19: initial version
	 * <p>
	 * 
	 * @author Sibyl Sullivan
	 * @version 2010-11-19
	 *
	 */

public class Initialization implements InitializationPlugin {

	/**
	 * Generated serialVersionUID for Rational
	 */
	private static final long serialVersionUID = -4243990691584973217L;
	
	/**
	 * The private log4j logger
	 */
	private static Logger LOGGER = Logger.getLogger(Initialization.class);

	//	private static String propertyFileRoot = null;

	@Override
	public void load(ServletContext context, Properties globalProp)
	throws PluginException {
	
		// load() is what is called at initialization.
		LOGGER.info("*** InitializationPlugIn");		
		
		
		// TODO create directory structure for .../gsmart and .../applog
		
		
		// Create the application Log table here 
		Hashtable<String, Logger> appLoggers = new Hashtable<String, Logger>();		
		context.setAttribute("appLoggers", appLoggers);
//		Hashtable<String, Object> appLoggers = new Hashtable<String, Object>();		
//		context.setAttribute("appLoggers", appLoggers);

		LOGGER.info("appLoggers hashtable created sucessfully");
	}

	@Override
	public void replace(ServletContext arg0, Properties arg1,
			HttpServletRequest arg2) throws PluginException {
		// TODO Auto-generated method stub

	}

}

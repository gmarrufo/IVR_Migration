package com.ibm.ivr.eus.plugin;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.ibm.ivr.eus.plugin.expressions.ExpressionHelperFunctions;
import com.ibm.ivr.framework.plugin.ExpressionPlugin;
import com.ibm.ivr.framework.plugin.PluginException;

/**
 *  The interface called by Common utilities class when evaluating the 
 * conditions and called by CallRoutingServlet class when evaluating 
 * the expression in Set element
 * This interface should be implemented by individual application implementation
*/
public class ExpressionPluginImpl extends ExpressionHelperFunctions implements ExpressionPlugin {
	// A reference to the session this plugin instance serves
	HttpSession session = null;
	
	private static Logger LOGGER = Logger.getLogger(ExpressionPluginImpl.class);
	
	// Token holding this sessions call id for logging purposes
	String logToken = "";

	/** Returns true if the name is a valid identifier
	 * 
	 */
	boolean isValidIdentifier(String ident) {
		if(ident == null || ident.length() == 0)
			return false;
		for(int i=0;i<ident.length();i++) {
			if(i==0 && !Character.isJavaIdentifierStart(ident.charAt(i)))
				return false;
			if(!Character.isJavaIdentifierPart(ident.charAt(i)))
				return false;
		}
		return true;
	}
		
		/** Gets a variable from the session
		 *  Overrides the base getVar() in Expressions class. 
		 *  If a regular expression is passed in for the variable name, a map of matching results is returned 
		 * 
		 */
	 	@Override
	    public Object getVar(String name) {
			return getVar(name, null);
		}
	    public Object getVar(String name, Object def) {
	    	
	    	// If an expression is passed in instead of a name, return a map of values
	    	try {
		    	if(!isValidIdentifier(name) && session != null) {
		    		Map<String, Object> result = new HashMap<String, Object>();
			 		Enumeration en = session.getAttributeNames();
		 			Pattern p = Pattern.compile(name);
			 		for(;en.hasMoreElements();) {
			 			String aname = (String)en.nextElement();
			 			Matcher m = p.matcher(aname);
			 			if(m.matches()) { 
			 				result.put(aname, session.getAttribute(aname));
			 			}
			 		}
			 		return result;
		    	}
	    	} catch(Exception e) {
				LOGGER.warn("getVar("+name+"): Failed, returning default value. cause="+e.getMessage());
	    	}
	    	
	    	// If the caller asks for the session, return the session object
	    	if("session".equalsIgnoreCase(name))
	    		return session;
	    		
	    	
			Object value = session == null ? super.getVar(name) : session.getAttribute(name);
			if(value == null && def != null)
				value = def;
			
			if(value == null)
				value = "";
			
			String s = ""+value;
			if(value instanceof String && s.startsWith("'")) {
				value = s.substring(1,s.length()-1);
			}

			if(debug) log(logToken+"getVar(): "+name+"='"+value+"'");
			
			return value;
	    }
	    
		/** Sets a variable into the session
		 *  Overrides the base serVar() in Expressions class. THis version uses an internal map to store variables
		 * 
		 */
	    @Override
	    public Object setVar(String name, Object value) {
			if(debug) log("setVar(): "+name+"='"+value+"'");
			if(session == null)
				super.setVar(name, value);
			else
				session.setAttribute(name, value);
			return value;
	    }
	 
		/** Evaluates an expression. Expressions have the form specified in the Migration design specification
		 *  
		 */
	 	@Override
		public Object evaluateExpression(String expression, HttpSession session) throws PluginException {
			ExpressionPluginImpl ep = getInstance(session);
			
			String log = ep.logToken+" evaluateExpression("+expression+")";

			Object result = null;
			try {
				result = ep.eval(expression);
				//if(debug) log(log+" = "+result);
				//return result;
			} catch(Exception e) {
				LOGGER.warn(log+" : Failed, returning '': "+e.getMessage());
				result = e.getMessage();
			}
			//LOGGER.info(ep.logToken+"expression: ("+expression+") = "+result);
			if(result == null)
				result = "";
			return result;
		}

		/** Gets the instance of the expression plugin that is tied to this session
		 * We tie the expression plugin to the session so that when the expression plugin sets variables
		 * they can go into the session
		 * @param session HttpSession
		 * @return A reference to this sessions expression plugin
		 */
		private ExpressionPluginImpl getInstance(HttpSession session) {
			ExpressionPluginImpl ep = null;
			try {
				ep = (ExpressionPluginImpl) session.getAttribute("ExpressionPlugin");
				if(ep == null) {
					ep = new ExpressionPluginImpl();
					ep.session = session;
					ep.logToken = "[" + ((session.getAttribute("callid") == null) ? session.getId() : session.getAttribute("callid").toString()) +"] ";
					session.setAttribute("ExpressionPlugin", ep);
					ep.debug = LOGGER.isDebugEnabled();
				}
				
				String basePath = (String)ep.session.getAttribute("basePath");
				if(basePath == null || basePath.length() == 0) {
					basePath = new File(ep.session.getServletContext().getRealPath("/WEB-INF/CallRoutingConfig")).getAbsolutePath();
					ep.setVar("basePath", basePath+"/");
				}

				
			} catch(Exception e) {
				return this;
			}
			return ep;
		}

		
		/** Evaluates a boolean condition. Evaluates the condition as an expression and converts the result
		 * to a boolean. The result must be "true" or "1". Conditions have the form specified in the
		 * Migration design specification
		 *  
		 */
		@Override
		public boolean evaluateCondition(String condition, HttpSession session) throws PluginException {
			ExpressionPluginImpl ep = getInstance(session);
			
			String log = ep.logToken+" evaluateCondition("+condition+")";
			if(debug) log(log);
			
			String cond = ""+evaluateExpression(condition, session);
			
			boolean result = cond.equalsIgnoreCase("true") || cond.equals("1");
			
			if(debug) log(log+" = "+result);
			
			LOGGER.info(ep.logToken+"condition: ("+condition+") = "+result);
			
			return result;
		}

	

		/** Central function to call logging for running offline tests de-coupled from the logger
		 */
		public void log(String msg) {
			log(DEBUG, msg);
		}
		public void log(int level, String msg) {
			// If we are testing without a session, call the default logger
			if(session == null) {
				super.log(level, msg);
				return;
			}
			if(level == ERROR)
				LOGGER.error(msg);
			else if(level == WARN)
				LOGGER.warn(msg);
			else if(level == INFO)
				LOGGER.info(msg);
			else if(level == DEBUG)
				LOGGER.debug(msg);
		}
//	    public static final int INFO = 1;
//	    public static final int WARNING = 2;
//	    public static final int ERROR = 4;

	 
}

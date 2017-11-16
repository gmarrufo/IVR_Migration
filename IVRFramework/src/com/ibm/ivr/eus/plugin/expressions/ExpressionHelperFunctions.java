package com.ibm.ivr.eus.plugin.expressions;

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map; 
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import com.ibm.ivr.eus.plugin.ExpressionPluginImpl;
import com.ibm.ivr.framework.utilities.Common;
import com.ibm.ivr.framework.utilities.EmailAlert;
import com.sun.faces.util.Timer;

//import com.ibm.ivr.eus.plugin.ExpressionPluginImpl;
//import com.ibm.ivr.eus.plugin.Logger;


/**  Class that provides implementations for functions that are referenced by the framework 
 * inside of parenthesis (). For example, if the framework calls a function to round up the 
 * users wait time to the nearest 15 seconds, that function could be referenced like this
 * 
 * @author Greg 
 *
 */
public class ExpressionHelperFunctions extends Expressions {

	
	public boolean haveExtraPrompts() {
		String rc = ""+getVar("rc");
		if(debug) log(DEBUG, "haveExtraPrompts(): rc="+rc);
		if(rc.indexOf("play_segment") >= 0) {
			String prompt = ""+eval(rc.replaceAll("`", "'"));
			if(debug) log(DEBUG, "haveExtraPrompts(): prompt="+prompt);
			return true;
		}
		return false;
	}
	
	/** Sets up a prompt segment to play
	 * @param segment The id of the segment to play
	 * @return segment.wav
	 */
	public void play_segment(String segment) {
		String prompts = ""+getVar("prompts");
		prompts = (prompts + " " + segment+".wav").trim();
		setVar("prompts", prompts);
		if(debug) log(DEBUG, "play_segment(): prompts="+prompts);
	}
	public void play_segment(int segment) {
		play_segment(""+segment);
	}
	
	
	/** Implementation of POS() function called by expressions in the application XML
	 * 
	 * @param needle Find this string
	 * @param haystack Inside this string
	 * @return The position
	 */
	 public int POS(String needle, String haystack) {
		 return(haystack.indexOf(needle) + 1);
	 }


		static String[] namesDate = "D,E,J,M,N,O,S,U,W".split(",");
		static SimpleDateFormat[] formatsDate = new SimpleDateFormat[namesDate.length];

		
/**	Returns the current date in the format specified by the format parameter. The format parameter is the first letter of one of the formats below:

<pre>
				       Days       Days so far in this year D
				       European   dd/mm/yy 
				       Julian     yyddd
				       Month      Month M
				       Normal     dd Mon yyyy 
				       Ordered    yy/mm/dd
				       Standard   yyyymmdd
				       Usa        mm/dd/yy
				       Weekday    Tuesday E
</pre>
Formats E, J, N, O, S and U are supported. The other formats are included for reference to the rexx DATE function.
*/


			public String DATE(String format) {
				for(int i=0;i<namesDate.length;i++) {
					if(namesDate[i].equals(format)) {
						if(formatsDate[i] == null)
							formatsDate[i] = new SimpleDateFormat(" D,dd/MM/yy,yyDDD,M,dd MMM yyyy,yy/MM/dd,yyyyMMdd,MM/dd/yy,EEEEE".split(",")[i]);
						//if(namesDate[i].equals("M"))
							//return valDouble(DATE("H")) * 60 + formats[i].format(new Date());
						return formatsDate[i].format(new Date());
					}
				}
				return(DATE("N"));
			}

			/** Causes the current thread to sleep for the specified number of milliseconds
			 * 
			 * @param ms Number of milliseconds to sleep
			 */
			public void SYSSLEEP(int ms) {
				try { Thread.sleep(ms);} catch(Throwable t){t.printStackTrace();}
			}
	 
		static String[] names = "C,E,H,L,M,N,R,S".split(",");
		static SimpleDateFormat[] formats = new SimpleDateFormat[names.length];
		long startTime = new Date().getTime();
	/**	
		Civil      hh:mmxx (1-12, 00-59, am/pm)
	    Elapsed    sssssssss.uuuuuu (seconds, microseconds)
	    Hours      Hours since midnight
	    Long       hh:mm:ss.uuuuuu
	    Minutes    Minutes since midnight
	    Normal     hh:mm:ss
	    Reset      Returns elapsed time, restarts timer
	    Seconds    Seconds since midnight
	*/
		public String TIME(String format) {
			for(int i=0;i<names.length;i++) {
				if(names[i].equals(format)) {
					if(formats[i] == null)
						formats[i] = new SimpleDateFormat("hh:mma,ss.SS,kk,kk:mm:ss.SSS000,mm,kk:mm:ss,SSSS,ss".split(",")[i]);
					if(format.equals("M"))
						return String.format("%d",(val(TIME("H")) * 60 + val(formats[i].format(new Date()))));
					if(format.equals("S"))
						return String.format("%d",(val(TIME("M")) * 60 + val(formats[i].format(new Date()))));
					if(format.equals("R") || format.equals("E")) {
						long now = new Date().getTime();
						long difference = now - startTime;
						if(format.equals("R"))
							startTime = now;
						return String.format("%.6f",difference/1000.0);
					}
					return formats[i].format(new Date()).toLowerCase();
				}
			}
			return(TIME("N"));
		}

		/** Strips both leading and trailing spaces from string
		 * 
		 * @param str String to trim
		 */
		public String STRIP(String s) {
			return s.trim();
		}
		
		/** Returns leftmost characters padded out to length with the pad string
		 * 
		 * @param str string to pad
		 * @param length length to pad (or trim)
		 * @param pad character to use to pad
		 */
		public String LEFT(String str, int length, String pad) {
			if(length < 0)
				return str;
			if(str.length() < length) 
				return str + String.format("%"+(length - str.length())+"s", "").replace(" ", pad);
			return str.substring(0, length);
		}
		public String LEFT(String str, int length) {
			return LEFT(str, length, " "); 
		}
		
		
		/** Returns rightmost characters padded out to length with the pad string
		 * 
		 * @param str string to pad
		 * @param length length to pad (or trim)
		 * @param pad character to use to pad
		 */
		public String RIGHT(String str, int length, String pad) {
			if(length < 0)
				return str;
			if(str.length() < length) 
				return  String.format("%"+(length - str.length())+"s", "").replace(" ", pad) + str;
			return str.substring(str.length() - length);
		}
		public String RIGHT(String str, int length) {
			return RIGHT(str, length, " "); 
		}

		/**  Returns a portion of string beginning at start which
		   is length long, length is either satisfied from string itself
		   or by padding the result with pad. 
		 * @param str String to operate on 
		 * @param start Starting position
		 * @param length Length of string
		 * @param pad Padding character
		 */
		public String SUBSTR(String str, int start, int length, String pad) {
			if(start <= 0) {
				log(WARN, "SUBSTR('"+str+"',"+start+","+length+",'"+pad+"'): invalid arguments - returning '"+str+"'");
				return str;
			}
			if(start >= str.length()) {
				//log(WARN, "SUBSTR('"+str+"',"+start+","+length+",'"+pad+"'): starting position is greater than the string length - returning ''");
				start = str.length() + 1;
				//return "";
			}
			String result = str.substring(start - 1);
			if(length < 0)
				length = str.length() - start + 1 ;
			if(result.length() < length) 
				return result + String.format("%"+(length - result.length())+"s", "").replace(" ", pad);
			return result.substring(0, length);
		}
		public String SUBSTR(String s, int start, int length) {
			return SUBSTR(s,start,length," ");
		}
		public String SUBSTR(String s, int start) {
			return SUBSTR(s,start,-1," ");
		}


		/** Clears the prompt_  and status_ override variables. 
		 * 
		 * @param menuName The name of the menu
		 * 
		 */
		public void INITOVERRIDES(String menuName) {
			Object o = getVar("session");
			if(o == null || !(o instanceof HttpSession) ) {
				log(WARN,"Session not available.");
			}

			HttpSession session = (HttpSession) o;
			Enumeration e = session.getAttributeNames();
			for(;e.hasMoreElements();) {
				String name = ""+e.nextElement();
				if(name.startsWith("status_override") || name.startsWith("prompt_override")) {
					if(debug)
						log(DEBUG,"INITOVERRIDES(): Clearing "+name+"="+session.getAttribute(name));
					session.removeAttribute(name);
				}
			}

			applySelection();
		}

		/** If there has been a user selection, apply it to cur_prompt */
		private void applySelection() {
			// If cur_prompt is not set, default it to "main"
			String cur_prompt = ""+getVar("cur_prompt");
			if(cur_prompt == null || cur_prompt.length() == 0) {
				cur_prompt = "main";
				setVar("cur_prompt", "main");
			}
		
			boolean cur_prompt_override = (""+getVar("cur_prompt_override")).equalsIgnoreCase("true");

			// If the user did not override the prompt and we have a selection, apply the selection to cur_prompt
			String selection = ""+getVar("selection");
			boolean selection_used = (""+getVar("selection_used")).equalsIgnoreCase("true");
			if(!cur_prompt_override && !selection_used && selection != null && selection.length() > 0) {
				if(selection.length() > 0) {
					cur_prompt += "_" + selection;
					setVar("cur_prompt", cur_prompt);
					setVar("selection_used", "true");
				}
			}
		}
		
		
		/** Initializes the menuLevel. Depends on the variable 'selection' to be set. Called from application XML
		 * 
		 * @param menuName The name of the menu
		 * @return The current menuLevel
		 * 
		 */
		/* TODO: Verify this table of languages has the correct values */
		static String languages = "US=en_US, UK=en_UK, CA=fr_CA, CF=fr_CA, CS=es_ES, FR=fr_FR, GE=de_DE, IT=it_IT, DK=da_DK, NO=no_NO, SE=sv_SE, FI=fi_FI, PG=pt_PT";
		public String INITMENU(String menuName) {
			String menuLevel = INITMENU2(menuName);
			setVar("menuLevel",menuLevel);
			setVar("selection","");
			return menuLevel;
		}
		/** Initializes the menuLevel. Depends on the variable 'selection' to be set. Called from application XML
		 * Returns the menuLevel without setting the menuLevel session variable
		 * 
		 * @param menuName The name of the menu
		 * @return The current menuLevel
		 * 
		 */
		public String INITMENU2(String menuName) {
			// Fix any calls to language_country
			String value = ""+getVar("language_country");
			if(value.length() > 0) {
				int pos = languages.indexOf(value+"=");
				value = pos < 0 ? value : languages.substring(pos+3,pos+8);
				setVar("LANG", value);
			}
			
			applySelection();
			setVar("selection_used", "");
			
			// menulevel is being set so it is now ok to clear the prompt override flag
			setVar("cur_prompt_override", "");
			
			String oldMenuLevel = ""+getVar("menuLevel");
			String cur_prompt = ""+getVar("cur_prompt");
			String menuLevel = cur_prompt.toLowerCase().replace("main_", "").replace("main", "M");
			if(menuLevel.length() == 0)
				menuLevel = "M";

			String currmenu = ""+getVar("currmenu");
			setVar("currmenu",menuName);
			if(debug) log("INITMENU("+menuName+"): "+currmenu+"."+oldMenuLevel+" -> "+menuName+"."+menuLevel);
			return menuLevel;
		}
		
		


		/** Implementation of LENGTH() function called by expressions in the application XML
		 * 
		 * @param s String to find length of
		 */
		 public int LENGTH(String s) {
			 return s.length();
		 } 
		 

		 //public String LEFT()
		
		/** Evaluates text containing parenthesis. Called from application XML
		 * For example hello (var1) this (var) is (var3)(var4) a test (var5)!
		*/
		public String EVALUATE_EMBEDS(String var) {
			var = ""+getVar(var);
			//var = var.substring(2,var.length()-2);//eval(var);
			StringBuffer result = new StringBuffer("");
			
			// Every other part is an expression so call eval() on those parts
			String[] parts = var.split("\\(|\\)");
			for(int i=0;i<parts.length;i++)
				result.append((i%2 == 1) ? getVar(parts[i]) : parts[i]);
			
			//System.err.println("EVALUATE_EMBEDS(): "+result.toString());
			if(debug) log("EVALUATE_EMBEDS(): "+result.toString());
			return result.toString();
		}

		
		/** Used by initmenu to override the menu level
		 */
		private String menuLevelOverride(String defaultMenulevel) {
			String cur_prompt = (""+getVar("cur_prompt")).toLowerCase();
			cur_prompt = cur_prompt.replace("main_", "").replace("main", "M").replaceAll("'", "");
			if(cur_prompt.length() > 0) {
				if(debug) log("menuLevelOverride(): Override menuLevel to ="+cur_prompt);
				// Clear cur_prompt so that the menu is not overridden forever
				//$$$
				setVar("cur_prompt", "");
				return cur_prompt;
			}
			return defaultMenulevel;
		}
		
		
		
		/** Look up target in the xfer.ini file. Called from application XML
		 * @param target The name or number to look up 
		 */
		static long xferPropertiesTimestamp = -1;
		static Properties xferProperties = new Properties();
		public String XFERLOOKUP(String target) {
			
			// Use the latest properties file to get xfer targets. We dont need to defer
			// using an updated copy here because this is the endpoint of the call, not the call logic itself
			File propertiesFile = new File(getVar("basePath")+"xfer.properties");// new File(session.getServletContext().getRealPath("/WEB-INF/CallRoutingConfig/xfer.properties"));
			if(xferPropertiesTimestamp != propertiesFile.lastModified()) {
				xferPropertiesTimestamp = propertiesFile.lastModified();
				try { 
					xferProperties.load(new FileReader(propertiesFile)); 
				} catch(Exception e){
						throw new RuntimeException("Critical Error: Unable to load transfer properties file "+propertiesFile.getAbsolutePath(),e);
				};
			}
			
			String appname = ""+getVar("xfer_search");
			String newTarget = xferProperties.getProperty(appname+"."+target);
			
			// If the newTarget was not found
			if((newTarget == null || newTarget.length() == 0)) {
				// If the target is a string look for APPNAME.DEFAULT
				if(!Character.isDigit((""+target+" ").charAt(0)))
					newTarget = xferProperties.getProperty(appname+".DEFAULT");
				// Else the target is used by default 
				else 
					newTarget = target;
			}
			
			log(" xferLookup(): target="+target+" translated target="+newTarget);
			return newTarget;

		}
		
		
		/** Command line expression test
		 * 
		 * @param args Path to the file with expressions to test - 1 per line
		 */
		public static void main(String[] args) throws Exception {
			String testFile = "C:\\Users\\Greg\\workspace\\ANTLR1\\expression.gooda.txt";
			if(args != null && args.length > 0) {
				testFile = args[0];
			} else {
				System.err.println("Test file not provided - using: "+testFile);
			}
			
			
			
/*			String result = ep.eval("DATE('U')+'   '+TIME('C')+' Server: '+RIGHT(voice_server,10)+' Current Application: '+RIGHT(cur_appl,5)+' Current INI: '+RIGHT(cur_ini,10)+' Call Flow: '+right(call_flow_name,10)+' Line: '+RIGHT(voice_line,2)+' Status Avoided: '+RIGHT(status_avoided,1)+' ANI: '+RIGHT(ANI,15)+' DNIS: '+RIGHT(DNIS,5)+' VDN: '+VDN+' Menu Pos: '+RIGHT(cur_prompt,15)+' Transfer Number: '+RIGHT(xfer_number,15)+' Send Data: '+RIGHT(send_data,12)+' Language: '+RIGHT(language_country,3)+' Transfer Type: '+RIGHT(xfer_type,10)+' INI File: '+RIGHT(ini_file,20)");
			System.out.println(result);
			System.exit(0);
*/			
			
/*			System.err.println(ep.LEFT("GREG",10,"X"));
			System.err.println(ep.LEFT("GREG",3,"X"));
			System.err.println(ep.LEFT("",2,"X"));
			System.err.println(ep.LEFT("",0,"X"));
			System.err.println(ep.LEFT("",-1,"X"));
			*/

			//String ald = 
//"DATE('U')+'   '+TIME('C')+' Server: '+RIGHT(voice_server,10)+' Current Application: '+RIGHT(cur_appl,5)+' Current INI: '+RIGHT(cur_ini,10)+' Call Flow: '+right(call_flow_name,10)+' Line: '+RIGHT(voice_line,2)+' Status Avoided: '+RIGHT(status_avoided,1)+' ANI: '+RIGHT(ANI,15)+' DNIS: '+RIGHT(DNIS,5)+' VDN: '+VDN+' Menu Pos: '+RIGHT(cur_prompt,15)+' Transfer Number: '+RIGHT(xfer_number,15)+' Send Data: '+RIGHT(send_data,12)+' Language: '+RIGHT(language_country,3)+' Transfer Type: '+RIGHT(xfer_type,10)+' INI File: '+RIGHT(ini_file,20)";
//			ep.vars.put("applicationlogdata", ald);
	
			ExpressionPluginImpl ep = new ExpressionPluginImpl();
			ep.setVar("voice_server", "WVR");

			String[] expressions = new String[]{
					"'Reset      Returns elapsed time, restarts timer      (~0.05 s) =' + TIME('R')",
					"SYSSLEEP(123)",
					"'Elapsed    sssssssss.uuuuuu (seconds, microseconds)  (~0.15 s) =' + TIME('E')",
					"SYSSLEEP(321)",
					"'Elapsed    sssssssss.uuuuuu (seconds, microseconds)  (~0.5 s) =' + TIME('E')",
					"'Reset      Returns elapsed time, restarts timer      (~0.5 s) =' + TIME('R')",
					"SYSSLEEP(777)",
					"'Elapsed    sssssssss.uuuuuu (seconds, microseconds)  (~0.8 s) =' + TIME('E')",
					"'Civil      hh:mmxx (1-12, 00-59, am/pm)              =' + TIME('C')",
					"'Hours      Hours since midnight                      =' + TIME('H')",
					"'Long       hh:mm:ss.uuuuuu                           =' + TIME('L')",
					"'Minutes    Minutes since midnight                    =' + TIME('M')",
					"'Normal     hh:mm:ss                                  =' + TIME('N')",
					"'Seconds    Seconds since midnight                    =' + TIME('S')",
					"automation_data='RC=0,PASSWORD=NewPassword'",
					"PARSE_VAR(automation_data,',`PASSWORD=`,new_password,`,`'))",
//					"PARSE_VAR('RC=0,PASSWORD=NewPassword',',`PASSWORD=`,new_password,`,`')=true " +
					"1 + 3",
					"4 + '1'",
					"'5' + 1",
					"'6' + '1'",
					"'7.5' + 1",
					"8.5 + 1",
					"1 + 9.5",
					"send_data='653006-897'",
					"serial_short=SUBSTR(send_data,1,LASTPOS('-',send_data)-1)",
					"serial_cc= (STRIP(SUBSTR(send_data,LASTPOS('-',send_data)+1)))",
					
					"rc=Substr( 'abracadabra', 5 )",

					"rc=Substr( 'abracadabra', 5, 3 )",

					"rc=Substr( 'abracadabra', 7, 7, '+' )",

					"rc=Substr( 'abracadabra', 99, 3, '+' )",

					"rc=Substr( '', 0, 3, '+' )",

					"rc=Substr( '', 1, 3, '+' )", 
					"rc=substr('promotional',4,6)",
					"rc=substr('abcdefghij',1,3)",
					"rc=substr('1234567890',5,5)",
					"rc=SUBSTR('653006-897','5')",
					"TODAY=(DATE('U'))", 
					"time_now=(TIME('N'))", 
					"applicationName='RecordingApp'",
					"applicationLogData=('START:'+TODAY+'-'+time_now)",
					"TIME('R')",
					"'X'+Right('long',6)+'X'",
					"'X'+Right('long2',6)+'X'",
					"'X'+Right('longer',6)+'X'",
					"'X'+Right('longest',6)+'X'",
					"'L'+Left('long',6)+'X'",
					"'L'+Left('long2',6)+'X'",
					"'L'+Left('longer',6)+'X'",
					"'L'+Left('longest',6)+'X'",
			};
			
			//TODO: Enable nested quotes
			String[] expressions2 = new String[]{
		//			"ApplicationLogData=\""+ald+"\"",
//					"log(4, ApplicationLogData)",
		//			"eval(ApplicationLogData)" };
					"DATE('D')",
					"DATE('E')",
					"DATE('J')",
					"DATE('M')",
					"DATE('N')",
					"DATE('O')",
					"DATE('S')",
					"DATE('U')",
					"DATE('W')"
					//E,J,M,O,S,U,W
			};
//					"POS('ANA',nco_location) GT 0",
//					"1 GT 0",
//					"(1 GT 0)",
//					"((1 GT 0))",
//					"(((1 GT 0)))",
//					"POS('ANA',nco_location) GT 0",
//					"POS('BNA',nco_location) GT '0'",
//					"nco_location NE 'G' && 1 GT '0'",
//					"(1 GT '0') AND (nco_location NE 'G')",
//					"(POS('CNA',nco_location) GT '0') AND (nco_location != '')",
//					"((POS('CNA',nco_location) GT '0') AND (nco_location != ''))",
//					"(((POS('CNA',nco_location) GT '0') AND (nco_location != '')) == 'true')",
//					"(((POS('CNA',nco_location) EQ '0') AND (nco_location == '')) == true)",
//			};
//			String[] expressions = Util.getFile(testFile).split("\n");
			Expressions.debug = true;
			for(int i=0;i<expressions2.length;i++) {
				Object rc = ep.eval(expressions2[i].trim());
				//ep.setVar("rc", rc);
			}
				//System.out.println("eval("+expressions[i].trim()+"): "+ep.eval(expressions[i].trim()));
			
			
		}

		
		
		/** Converts the menuLevel into a more usable form
		 * 
		 * @param menuLevel Takes the form M or 1 or 1_2, etc
		 * @return menuLevel in the form MAIN(_SUBLEVEL_...) e.g. MAIN, MAIN_1, MAIN_1_2 
		 */
		public String TRANSLATE(String menuLevel) {
			menuLevel = menuLevel.toUpperCase();
			if(menuLevel.equalsIgnoreCase("M") || menuLevel.equals(""))
				return "MAIN";

			if(menuLevel.startsWith("MAIN"))
				return menuLevel;

			return("MAIN_"+menuLevel);
			
		}
		
		/** Sets the timeout value for entering DTMF input
		 * Causes the timeout property to be set in root.jsv
		 * 
		 * @param count Number of seconds to give the user
		 * @return
		 */
		public String SET_TIMEOUT(int count) {
			log(INFO, "SET_TIMEOUT(): Setting timeout (property) to "+count+"s");
			setVar("timeout",""+count);
			return "true";
		}
		
		/** Finds the last index of the string starting at fromIndex
		 * 
		 * @param needle String to find
		 * @param haystack String to look in
		 * @param fromIndex Index to begin the search
		 * @return index of where string was found or -1 if unsuccessful
		 */
		public int LASTPOS(String needle, String haystack, int fromIndex) {
			return haystack.lastIndexOf(needle, fromIndex - 1) + 1;
			
		}
		public int LASTPOS(String needle, String haystack) {
			return LASTPOS(needle, haystack, haystack.length());
		}
		
		/** Sends an email with the name=value arguments passed in.  
		 * "email" variable is the TO email address. 
		 * globalProp.emailSender is FROM,
		 * globalProp.SMTP is the SMTP server
		 * 
		 * @param args FILE and SUBJECT name=value pairs. Optionally ATTACHMENT, BODY, FROM, TO can be included to override using emailSender, and email, respectively 
		 * @return true on success
		 */
		public boolean EMAIL(String args) {
			String[] args2 = args.split("=");
			
			// Put the name=value pairs into a map
			if(debug) log(DEBUG, "Sending email with the following arguments:");
			Map arguments = new HashMap();
			for(int i=0;i<args2.length - 1;i++) {
				String name = i==0 ? args2[i].trim() : args2[i].substring(args2[i].lastIndexOf(' ') + 1);
				String value = args2[i+1].substring(0, args2[i+1].lastIndexOf(' '));
				arguments.put(name, value);
				if(debug) log(DEBUG,"EMAIL():  "+name+"="+value);
			}
			
			Object o = getVar("session");
			if(o == null || !(o instanceof HttpSession) ) {
				log(WARN,"Session not available. Email not sent, returning false");
				return false;
			}

			HttpSession session = (HttpSession) o;
			Properties prop = (Properties) session.getServletContext().getAttribute("globalProp");
			EmailAlert alert = new EmailAlert(prop.getProperty("SMTP"));


			//if EmailType doesn't have sender and recipient set, use the values from global.properties
			String to = (String)arguments.get("TO");
			if (to == null || to.length() == 0)
				to = ""+getVar("email");
			if (to == null || to.length() == 0) 
				to = prop.getProperty("emailRecipient");
			
			String from = (String)arguments.get("FROM");
			if (from == null || from.length() == 0)
				from = prop.getProperty("emailSender");
			
			String subject = (String)arguments.get("SUBJECT");
			if (subject == null || subject.length() == 0) {
				log(WARN,"SUBJECT missing. Email not sent, returning false");
				return false;
			}

			String body = (String)arguments.get("BODY");
			String file = (String)arguments.get("FILE");
			if (file != null && file.length() > 0) {
				body = null;
				file = new File(getVar("basePath")+file).getAbsolutePath().replace('\\', '/');// new File(session.getServletContext().getRealPath("/WEB-INF/CallRoutingConfig/xfer.properties"));
			} else {
				file = null;
			}
			
			String attachment = (String)arguments.get("ATTACHMENT");
			if (attachment != null && attachment.length() > 0) {
				body = null;
				attachment = new File(getVar("basePath")+attachment).getAbsolutePath().replace('\\', '/');// new File(session.getServletContext().getRealPath("/WEB-INF/CallRoutingConfig/xfer.properties"));
			} else {
				attachment = null;
			}

			//String attachment = (String)evaluate(emailType.getAttachment(),session);
			if(debug) log(DEBUG,"EMAIL: to="+to+" subject="+subject+(body == null ? " file="+file : " body="+body.length()+"characters"));
				
			if (file == null && (body == null || body.length() == 0))
				alert.sendEmailSubjectOnly(from, to, subject);
			else
				alert.sendEmail(from, to, subject, body, file, attachment);
				
			return true;
		}
		
		/** Parses inVar and puts results in names contained in outVars
		 * Note: implementation is provided as an example, it is recommended that the app designer thoroughly test the expression
		 * Example:  PARSE_VAR('NAME=xyz,PASSWORD=foo,ADDRESS=BAR',',`PASSWORD=`,new_password,`,`')
		 * new_password becomes 'foo'
		 * @param inVar String to parse
		 * @param outVars Comma separated list of variable names, string literal delimiters, or numbers. The first comma is ignored
		 * If a string literal, then the string represents a string to find
		 * If a number, it represents an offset
		 * If a variable name, then the result is put in there using setVar
		 * @return True if parse was successful
		 */
		public boolean PARSE_VAR(String inVar, String outVars) {
			// Escape comma in case it winds up being a delimiter
			String[] args = outVars.replaceAll("`,`", "`~~~`").split(",");
			int start = -1;
			int end = -1;
			int lastpos = 0;
			//inVar = "NAME=xyz,PASSWORD=foo,ADDRESS=BAR";
			String var = "";
			for(int i=1;i<args.length;i++) {
				// Unescape possible comma delimiter
				String arg = args[i].replaceAll("`~~~`","`,`");
				int pos = -1;
				// If the arg is a literal delimiter string, find that string 
				if(arg.startsWith("`")) {
					pos = inVar.indexOf(arg.substring(1, arg.length() - 1), lastpos);
					if(pos < 0) {
						if(start < 0 || var.length() ==0)
							return false;
						pos = inVar.length();
						if(debug) log("PARSE_VAR(): EOL found at position="+pos);
					} else {
						if(debug) log("PARSE_VAR(): "+arg+" found at position="+pos);
					}

					// If we found the start of the string, skip past the delimiter
					if(start < 0) {
						pos += arg.length() - 2;
						lastpos = pos;
					} else if(end < 0) { 
						lastpos = pos + arg.length();
					}

				// If the arg is a number, set the position to that number
				} else if(arg.length() > 0 && !Character.isJavaIdentifierStart(arg.charAt(0))) {
					pos = val(arg);
				} else if(arg.length() > 0) {
					var = arg;
					if(debug) log("PARSE_VAR(): setting var="+var);
					if(i == args.length - 1)
						pos = inVar.length();
				}
				if(pos >= 0 && start < 0) {
					start = pos;
					if(debug) log("PARSE_VAR(): setting start="+start);
				} else if(pos >= 0 && end < 0) {
					end = pos;
					if(debug) log("PARSE_VAR(): setting end="+end);
				}
				
				if(start >= 0 && end >= 0 && var.length() > 0) {
					setVar(var, inVar.substring(start, end));
					start = -1;
					end = -1;
					var = "";
				}
			}
			return true; 
		}
		
		/** The next_ini variable is set by the application to the name of the menu, e.g. WELCOME
		 * However, this name needs "_VARS" appended so that in order to use next_ini to go to a menu 
		 * For example, to use next_ini, call 
		 * < Set var="next_menu" expr="(MENUNAME(next_ini))"/ >
		 * ...
		 * < MenuDefault ... targetName="$next_menu" .../ >  
		 * @param next_ini Name of the menu
		 * @return Normalized menu name. If the input menu ends in _VARS, _CHECK, or _MENU, next_ini is returned without any changes
		 */
		public String MENUNAME(String next_ini) {
			if(next_ini.endsWith("_VARS") || next_ini.endsWith("_MENU") || next_ini.endsWith("_CHECK"))
				return next_ini;
			return next_ini + "_VARS";
		}
		
		/** The TREXX function was a mechanism by which the application could call into the root namespace
		 * Since the migrated code provides only one namespace, we simply convert the trexx(...) call to a function call
		 * For example: <br/>
		 * trexx(Clear_Tones) calls Clear_Tones()
		 * trexx(Put_Tone_String,"1") calls Put_Tone_String('1')
		 * trexx(Get_Tone_String,1,30,"#") calls Get_Tone_String('1','30','#')
		 * Note: Expressions.eval() does not split up the arguments for TREXX to make it easier for us to handle them
   
		 * @param expression The function name followed by its arguments
		 * @return The result of the function
		 */
		public Object TREXX(String args) {
			String expression = args.indexOf(',') > 0 ? args.replaceFirst(",","\\(") + ")" : args + "()";
			expression = expression.replaceAll("`","'");
			return eval(expression);
		}

		/**
		 * REXX Function that creates a file and writes/appends data to the file
		 * @param filename
		 * @param fileData
		 */
		public void LINEOUT(String filename, String fileData){
			File file=null;
			FileWriter writer=null;
			BufferedWriter buffWriter=null;
			try{
				file=new File(filename);
				if(!file.exists()){
					if(!file.createNewFile()){
						log(ERROR,"File:"+filename+" cannot be created");
						return;
					}
					writer=new FileWriter(filename);
				}else{
					writer=new FileWriter(filename, true);
										
				}
				buffWriter=new BufferedWriter(writer);
				fileData=fileData.replace("&quot;", "\"");
				fileData=fileData.replace("&#60;", "<");
				fileData=fileData.replace("&lt;", "<");
				fileData=fileData.replace("&#62;", ">");
				fileData=fileData.replace("&gt;", ">");
				fileData=fileData.replace("&#61;", "=");
				fileData=fileData.replace("&#63;", "?");
				fileData=fileData.replace("&lsquo;", "'");
				fileData=fileData.replace("&rsquo;", "'");
				buffWriter.write(fileData);
				buffWriter.newLine();
				buffWriter.flush();
			}catch(Exception e){
				log(ERROR,"Error in LINEOUT--"+e.toString());
				e.printStackTrace();
				
			}finally{
				if(buffWriter != null)
					try{
						buffWriter.close();
					}catch(java.io.IOException io){
						log(ERROR,"Error in LINEOUT closing BufferedWriter--"+io.toString());
						io.printStackTrace();
					}
					
					if(writer != null)
						try{
							writer.close();
						}catch(java.io.IOException io){
							log(ERROR,"Error in LINEOUT closing FileWriter--"+io.toString());
							io.printStackTrace();
						}
			}
		}
		
		/**
		 * Returns the Hostname of the local server
		 * @return
		 */
		public String GETHOSTNAME(){
			String hostname="";
			
			try{
				InetAddress addr=InetAddress.getLocalHost();
				hostname=addr.getHostName();
			}catch(Exception e){
				log(ERROR,"Error getting Hostname:"+e.toString());
				e.printStackTrace();
			}
			return hostname;
		}
		
		/**
		 * Function that deletes a file from the file system.
		 * @param filename
		 */
		public void DELETEFILE(String filename){
			File file=null;
			
			try{
				file=new File(filename);
				if(file.exists()){
					if(!file.delete()){
						log(INFO,"Error Deleting File "+filename);
					}
				}else{
					log(INFO,"File:"+filename+" does not exist");
				}
			}catch(Exception e){
				log(ERROR,"Error in DELETEFILE:"+filename+" Exception:"+e.toString());
				e.printStackTrace();
			}
		}
		
		/**
		 * This function returns a String that remains when a substring is removed
		 * from another String.
		 * @param data
		 * @param offset
		 * @return
		 */
		public String DELSTR(String data, String offset){
			String newString="";
			int stringLength=0;
			int intOffset=0;
			try{
				if(data == null || offset == null)
					return data;
				
				stringLength=data.length();
				intOffset=Integer.parseInt(offset);
				if(intOffset > stringLength){
					return data;
				}
				
				if(intOffset != 0)
					newString=data.substring(0,intOffset - 1);
				
			}catch(Exception e){
				log(ERROR,"Exception in DELSTR:"+e.toString());
				
			}
			return newString;
		}
		
		/**
		 * This function returns a String that remains when a substring is removed
		 * from another String.
		 * @param data
		 * @param offset
		 * @param length
		 * @return newString
		 */
		public String DELSTR(String pData, String pOffset, String pLength){
			String newString="";
			int dataLength=0;
			int offset=0;
			int subStrLength=0;
			try{
				if(pData == null || pOffset == null || pLength == null)
					return pData;
				
				pData=pData+" ";
				dataLength=pData.length();
				subStrLength=Integer.parseInt(pLength);
				offset=Integer.parseInt(pOffset);
				if(offset > dataLength){
					return pData;
				}
				
				// This is to prevent AOB Error
				if((dataLength - offset) < subStrLength)
					return DELSTR(pData,pOffset)+pData.substring(offset+1);
				
				// Add one to the offset because rexx starts the index at 1 ... not zero
				//System.out.println("Display:"+pData.substring(offset+1,offset+subStrLength+1));
				newString=DELSTR(pData,pOffset)+pData.substring(offset+1,offset+subStrLength+1);
			}catch(Exception e){
				log(ERROR,"Exception in DELSTR:"+e.toString());
				e.printStackTrace();
			}
			
			return newString.trim();
		}		
		
		/** Figures out the prompt overrides to apply when the overrides are determined at runtime */ 
		public boolean cpretail_prompt_fix() {
			if(debug) {
				Object[] names = ((Map)getVar("prompt_override.*")).keySet().toArray();
				for(int i=0;i<names.length;i++) {
					log(DEBUG, "cpretail_prompt_fix override: "+names[i]+"="+getVar(""+names[i]));
				}
			}
			
			String menuLevel = ""+getVar("menuLevel");
			menuLevel = menuLevel.equals("M") ? "" : "_"+menuLevel;
			String override = ""+getVar("prompt_override_main"+menuLevel);
			if(override != null && override.length() > 0) {
				if(debug) log("cpretail_prompt_fix(): cpretail_prompt="+override);
				setVar("cpretail_prompt", override);
				return true;
			}
			return false;
		}
		
		 public String digits(String varStr) {
			 StringBuffer result = new StringBuffer();
		        for(int i = 0; i < varStr.length(); i++)
		        {
		        	//String audioDir = (String)session.getAttribute("audioDir");
		            char c = varStr.charAt(i);
		            if(c == ' ')
		                result.append("1secSilence").append(".wav").append(Common.SPACE);
		            else
		                result.append(c).append(".wav").append(Common.SPACE);
		        }
		        return result.toString();
	    }
		 
		 
		 /** gps 170448 Eliminate prompt for duplicate serial number
		  * Called by ALPHAENT_PRUNE_BY_SERIAL
		  * 
		  * Prune_by_Serial will prune the return_vector list to get rid of
		  * serial numbers that do not match the users selection (selected_index)
		  * 
		  * Before it does that we replace return_vector with the original raw return_vector
		  * and select the first matching serial number
		  * // gps 170448 Eliminate prompt for duplicate serial number
		  * 
		  * @param selected_index The selected index in return_vector
		  * @return The selected_index in the original raw return_vector
		  */
		 public String useRawSerialNumberList(int selected_index) {
			 // Get the selected row e.g. 653006;;653006-897;; NA1NA1;;5618622140;;NA;;amcdonle@us.ibm.com
			 String row = ((Vector<StringBuffer>)getVar("return_vector")).get(selected_index).toString();
			 String serial = row.split(";")[0];

			 // Replace the return_vector with the original return_vector
			 Vector<StringBuffer> serialNumberList = getRawSerialNumberList();
			 setVar("return_vector", serialNumberList);
			 setVar("return_count", new Integer(serialNumberList.size()));
			 
			 // Find the serial in the raw list
			 for(int i=0;i<serialNumberList.size();i++) {
				 row = ""+serialNumberList.get(i);
				 if(row.startsWith(serial)) {
					 return ""+i;	
				 }
			 }
			 return "0";
		 }
		 
		 /** gps 170448 Eliminate prompt for duplicate serial number
		  * Called by ALPHAENT_EVAL_LOOKUP
		  * 
		  * Creates a list of unique serial numbers
		  * 
		  * @return The number of unique serial numbers
		  *  
		  */
		 public String uniqueSerialCount() {
			 Vector<StringBuffer> serialNumberList = getRawSerialNumberList();
			 
			 Map uniqueSerialMap = new HashMap();
			 
			 // For each unique serial number, create a map entry to a list of serial number rows
			 // contact_id;;service_location;;phone_tmp;;phone_ext_tmp;;email_tmp
			 Vector<StringBuffer> return_vector = new Vector<StringBuffer>();
			 for(int i=0;i<serialNumberList.size();i++) {
				 // Get the row e.g.: 653006;;653006-897;; NA1NA1;;5618622140;;NA;;amcdonle@us.ibm.com
				 String row = ""+serialNumberList.get(i); 
				 if(row.indexOf(";") < 0)
					continue;
				
				 String shortSerial = row.split(";")[0];
				 Vector<StringBuffer> rows = (Vector<StringBuffer>)uniqueSerialMap.get(shortSerial);
				 if(rows == null) {
					rows = new Vector<StringBuffer>();
				 	uniqueSerialMap.put(shortSerial, rows);
				 	return_vector.add(new StringBuffer(row));
				 }
				 rows.add(new StringBuffer(row));
			 }
			 
	    	 setVar("uniqueSerialMap", uniqueSerialMap);
	    	 //setVar("return_vector", return_vector);
	    	 //setVar("return_count", new Integer(return_vector.size()));
	    	 return ""+uniqueSerialMap.size();
		}

		 
		 /** gps 170448 Eliminate prompt for duplicate serial number
		  * Called by ALPHAENT_PLAY_SERIAL
		  * 
		  * Creates a list of unique serial numbers
		  * 
		  * @return The number of unique serial numbers
		  *  
		  */
		 public void useUniqueSerialNumberList() {
			 Map uniqueSerialMap = (Map)getVar("uniqueSerialMap");
			 
			 // For each unique serial number, create a map entry to a list of serial number rows
			 // contact_id;;service_location;;phone_tmp;;phone_ext_tmp;;email_tmp
			 Vector<StringBuffer> return_vector = new Vector<StringBuffer>();
			 Object[] keys = uniqueSerialMap.keySet().toArray();
			 for(int i=0;i<keys.length;i++) {
				 // Get the row e.g.: 653006;;653006-897;; NA1NA1;;5618622140;;NA;;amcdonle@us.ibm.com
				 StringBuffer row = ((Vector<StringBuffer>)uniqueSerialMap.get(keys[i])).get(0); 
				 if(row.indexOf(";") < 0)
					continue;
				 return_vector.add(row);
			 }
			 
	    	 setVar("return_vector", return_vector);
	    	 setVar("return_count", new Integer(keys.length));
		}

		 
		 /** gps 170448 Eliminate prompt for duplicate serial number
		  * Called by ALPHAENT_EVAL_LOOKUP
		  * 
		  * Sorts the serial number list by country
		  * Saves off this sorted list as the original list for later use by Prune_SerialXXX function
		  * 
		  * @return The new sorted list
		  */
		 public Vector<StringBuffer> saveSortedRawSerialNumberList() {
			Vector<StringBuffer> sortedList = getRawSerialNumberList();
			String temp = ""+getVar("country_priority");
			final String country_priority	= temp.length() > 0 ? temp : "897;US5;US6;649;CA2;CA3;CA4;CA6;CA7;CA8";
			 
			try {
				Collections.sort(sortedList, new Comparator<StringBuffer>() {
					  public int compare(StringBuffer a, StringBuffer b) {
						  // Get the country e.g. 897 for: 653006;;653006-897;; NA1NA1;;5618622140;;NA;;amcdonle@us.ibm.com
						  // Country defaults to 897
						  String countryA = (a.toString()+";;XXXXXX-897").split(";")[2].split("-")[1];
						  String countryB = (b.toString()+";;XXXXXX-897").split(";")[2].split("-")[1];
					      return ((""+country_priority.indexOf(countryA)).compareTo(""+country_priority.indexOf(countryB)));
					  }
				}); 
			} catch(Throwable t) {
				log(ERROR,"Unable to sort serial number list: "+sortedList.toString());
			}
			return sortedList;
		 }
		 
		 /** gps 170448 Eliminate prompt for duplicate serial number
		  * Gets the original serial number list. If test_serials is set, use them
		  * 
		  * @return The serial number list (return_vector)
		  */
		private Vector<StringBuffer> getRawSerialNumberList() {
			Vector<StringBuffer> serialNumberList = null; 
			 
			 // If the raw serial number list returns "" then 
			 // this is the first time so save off the raw serial number list
			 if(getVar("rawSerialNumberList") instanceof String) {
				 
				 // If test_serials are available, use them instead
				 String test_serials = ""+getVar("test_serials");
				 if(test_serials.length() > 0) {
					 log(INFO, "Test mode: Using test_serials="+test_serials);
					 
					 Vector<StringBuffer> testVector = new Vector<StringBuffer>(); 
					 String[] list = test_serials.split("\\|");
					 for(int i=0;i<list.length && list[i].trim().length() > 0;i++) {
						testVector.add(new StringBuffer(list[i].replace(',', ';'))); 
					 }
					 setVar("return_vector",testVector);
					 setVar("return_count", new Integer(testVector.size()));
				 }

				 // Save off the raw serial number list
				 if(getVar("return_vector") instanceof String)
					 setVar("rawSerialNumberList", new Vector<StringBuffer>());
				 else 
					 setVar("rawSerialNumberList", getVar("return_vector"));
			 }
			 
			 // Start with the raw serial number list
			 serialNumberList = (Vector<StringBuffer>)getVar("rawSerialNumberList");
			return serialNumberList;
		}
}
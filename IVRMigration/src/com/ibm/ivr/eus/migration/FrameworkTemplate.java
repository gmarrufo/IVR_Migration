package com.ibm.ivr.eus.migration;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.*;

/** Templates for the framework properties files
 * 
 * @author Greg
 *
 */
class FrameworkTemplate {
	String basePath = null;
	String name = "NAME";
	public static int DEBUG = 4;
	public static int INFO = 2;
	public static int WARN = 1;
	public static int ERROR = 0;
	
	String urlmappingProperties = 
			"#TESTING\r\n" + 
			"TESTING_ENTRIES" + 
			"\r\n" + 
			"APP_ENTRIES" + 
			"#866-111-1111: test TFN into primary WVR system\r\n" + 
			"#url.3123=SCA_IVR_\r\n" + 
			"#866-111-1112: test TFN into secondary WVR system\r\n" + 
			"#url.3122=SCA_IVR_\r\n" + 
			"\r\n" + 
			"#Global flags to be used during MainServlet\r\n" + 
			"#Flag to indicate if need to simulate the access number\r\n" + 
			"#simulateAccessNumber=true\r\n" + 
			"\r\n" + 
			"#Flag to indicate if testing is done or Desktop\r\n" + 
			"testingOnDesktop=true\r\n" + 
			"\r\n" + 
			"#Field to specify the environment the IVRs are running\r\n" + 
			"envPlatform=FVT\r\n"; 

	String reportMappingProperties = 
		"APP_ENTRIES"+
		"\r\n"+
		"systemtype=Primary\r\n";

	
	String log4jProperties = 
		"# BEGIN SECTION NAME_IVR_APP\r\n" + 
		"#UNCOMMENTED log4j.appender.R.File=/thomson/ivr/NAME_IVR_APP/log/call.log\r\n" + 
		"#UNCOMMENTED log4j.appender.R1.File=/thomson/ivr/NAME_IVR_APP/stats/stats.log\r\n" + 
		"#UNCOMMENTED log4j.appender.R2.File=/thomson/ivr/NAME_IVR_APP/log/callroute.log\r\n" + 
		"# END SECTION\r\n";

	String ivrmappingProperties = 
		"# BEGIN REPLACED SECTION\r\n" + 
		"#callflow.01111=ASCLocator\r\n" + 
		"TESTNUMBERS" + 
		"# Virtual dnis to jump to another IVR app\r\n" + 
		"# callflow.APPNAME_mainmenu_dtmf=APPNAME.xml#APPNAMEMenu.DTMF\r\n" + 
		"# END REPLACED SECTION\r\n" + 
		"\r\n" + 
		"APPSECTIONS";
	
	String ivrmappingAppSection = 
		"# BEGIN SECTION IVRNAME_IVR_APPNAME\r\n" + 
		"TFNENTRIES" + 
		"# END SECTION\r\n\r\n";

	/** <h3>ivrmapping.properties</h3>
	 * Maps the dnis to the application configuration xml file
For example:
<pre>
callflow.1000=Test.xml#DTMFMenu1.DTMF
callflow.2000=Test.xml#SpeechMenu1.speech
callflow.3000=Test.xml#HybridMenu1.hybrid
</pre>

The vru.ini file contains several types of mappings<br>
<ol>
<li><b>Simple Case</b><br>
<pre>
1011125;cingular.ini
</pre>
Translates to:<br>
<pre>
callflow.1011125=cingular.xml
</pre>
<br>

<li><b>Port/line case </b><br>
Thic case is typical for outbound calling support<br> 
We can add the port lines to the test section of the properties file to enable more logging.
We will check for "port" lines first. If nothing matches, then use the converse/dnis method (default)
<pre>
92;newrc.ini
</pre>
Translates to:<br>
<pre>
port.92=newrc.xml
</pre>

<br>
<li><b>Converse case</b><br>
Collects between 1 and 10 digits terminated by #.  If get_call_data is present then sends NotifyCallStart to Genesys, then calls GetCallInfo on ANI and DNIS, then calls UDataGetKVP on UU_DATA, then calls UDataGetKVP on screenpop_data.  It uses values in the UU_DATA and screenpop_data fields to set cur_appl, cur_ini, cur_prompt, and send_data variable values
<pre>
*5;ibmhelp_inqueue_status_infomercial.ini;language_country='US'
</pre>
Translates to:<br>
<pre>
callflow.*5=ibmhelp_inqueue_status_infomercial.xml#;converse=true;language_country='US'
</pre>
The converse=true variable causes the application to use a <grammar> to pick up the 7 ot 8 digit dnis which will be terminated by a #<br>
<br>
<li><b>Genesys case</b><br>
<pre>
1012664;pfzdm.ini;get_call_data='Y';cti_platform='GENESYS'
</pre>
Translates to:<br>
<pre>
callflow.1012664=pfzdm.xml#;get_call_data='Y';cti_platform='GENESYS'

or

callflow.1012664=pfzdm.xml#OPTIONALMENUNAME.dtmf;get_call_data='Y';cti_platform='GENESYS'
</pre>
OPTIONALMENUNAME.dtmf is the optional starting menu name e.g. GLOBAL_VARS<br>
Notes:<br>
Genesys - answers call on line and behavior is same for get_call_data to get DNIS, this DNIS is used to start application<br>

I don't see any examples for Genesys answer in the 2 VRU.ini files we have received but Ian did say that this support is required during the requirements review.  For reference you can look at answr.trx and scgen.trx.<br>


<br>

</ol>

Notes: 
dnis routing has two ways of getting dnis, current standard way is obtaining dnis through standard VXML var per the VXML spec which should be treated as the default way for framework. Then need to introduce a new property in global.properties to inform the framework other ways of getting dnis, e.g., converse. In the case this property is not in global.properties, the stand way should be used (back compatibility)
In ctiStart.jsv (which is the first page if CTIEnabled=true) can have logic to check if line number is matching any xml in ivrmapping.properties. If yes, go to EntryPointServlet to locate the right xml object etc. (special session var needs to be set to notify framework code in all later part, use line-port to locate XML)  If not, based on the global property, to decide if use the standard mechanism or converse step to collect DNIS and submit as normal way. 
In the code, we use dnis as a key to get the next menu. Now we will also put the port in dnis – under the covers the code remains the same.
If we do the converse step, we set up a grammar to collect the ANI digits – typically 7 (old apps) or 8 (new apps) digits ended by a #
Currently, if CTIenabled = false, framework is collecting DNIS and ANI from converse step (EntryPoint.jsv is used for this purpose which is also used for collecting dnis and ani from caller during testing). This needs to be updated to consider line port scenario too.  In the line port scenario, entrypoint.jsv doesn’t need to invoked since we already know what app to start. Line_port can be added to testDNIS or debugDNIS to force test call level of logging.
We can control getting the ANI through setting XXXX in global.properties

	 */
	public void genivrmappingProperties(List<VRUFile> entries) {
		Map<String, String> sections = new HashMap<String, String>();
		for(int i=0;i<entries.size();i++) {
			String appName = entries.get(i).appName;
			
			String entry = "#TFN 800DNIS\r\n" + "callflow.DNIS=APPNAME.xmlPARMS";
			entry = entry.replaceAll("APPNAME", appName);
			entry = entry.replaceAll("IVRNAME", name);
			entry = entry.replaceAll("DNIS", entries.get(i).dnis);
			String parms = entries.get(i).parameters.replace('&', ';');
			parms = (parms.length() == 0) ? "" : ";"+parms;
			entry = entry.replaceAll("PARMS", parms);
			
			String section = sections.get(appName);
			if(section == null)
				section = "";
			section += entry;
			sections.put(appName, section);
		}

		Object[] keys = sections.keySet().toArray();
		String app = "";
		for(int i=0;i<keys.length;i++) {
			String buff = ivrmappingAppSection.replaceAll("TFNENTRIES", sections.get(keys[i]));
			buff = buff.replaceAll("IVRNAME", name);
			buff = buff.replaceAll("APPNAME", ""+keys[i]);
			app += buff;
		}
				
		String path = basePath+"/ivrmapping.txt";
		String buff = ivrmappingProperties; 			//Util.getFile(path);
		buff = buff.replaceAll("APPSECTIONS",app);
		buff = buff.replaceAll("TESTNUMBERS","");
		path = path.replace("txt", "properties");
		log(INFO,"Writing "+path);
		Util.putFile(path, buff, false);
	}

	/** log4j.properties
	 * This file configures the logging
	 * For each application entry in vru.ini, a call.log, stats.log and callroute.log entry is created
	 * In the example below, $NAME is always SCA. $APP is the app name
	 * The sections start out commented so that the ant build script can uncomment and enable groups of applications to be deployed on websphere app server together (rather than have them all enabled)
		"# BEGIN SECTION $NAME_IVR_$APP\r\n" + 
		"#UNCOMMENTED log4j.appender.R.File=/thomson/ivr/$NAME_IVR_$APP/log/call.log\r\n" + 
		"#UNCOMMENTED log4j.appender.R1.File=/thomson/ivr/$NAME_IVR_$APP/stats/stats.log\r\n" + 
		"#UNCOMMENTED log4j.appender.R2.File=/thomson/ivr/$NAME_IVR_$APP/log/callroute.log\r\n" + 
		"# END SECTION\r\n";
	 * @param entries
	 */
	public void genlog4jProperties(List<VRUFile> entries) {
		String app="";
		Set<String> used = new HashSet<String>();
		for(int i=0;i<entries.size();i++) {
			String appName = entries.get(i).appName;

			if(used.contains(appName))
				continue;
			used.add(appName);
			
			String buff = log4jProperties.replaceAll("APP", appName);
			buff = buff.replaceAll("NAME", name);
			app += buff;
		}
				
		String path = basePath+"/log4j.txt";
		String buff = Util.getFileOrResource(path);
		
		buff = buff.replaceAll("APP_SECTION",app);
		buff = buff.replaceAll("APPNAME",name);
		path = path.replace("txt", "properties");
		log(INFO,"Writing "+path);
		Util.putFile(path, buff, false);
	}

	/** reportmapping.properties
	 *
	 * For each app name in vru.ini, generate this line
appname.$appName=SCA/ivr/SCA_IVR_$appName/stats
For example 
appname.pfzdm=/SCA/ivr/SCA_IVR_pfzdm/stats
	 * 
	 * @param entries
	 * @deprecated
	 */
	public void genReportMappingProperties(List<VRUFile> entries) {
		String app=""; 
		for(int i=0;i<entries.size();i++) {
			//appname.GEConsumerElectronics=/thomson/ivr/Thomson_IVR_Communications/stats\r\n"" +
			app += "appname."+entries.get(i).appName+"=/"+name+"/ivr/"+name+"_IVR_"+entries.get(i).appName+"/stats\r\n";
		}
		String buff = reportMappingProperties.replaceAll("APP_ENTRIES", app);
		
		String path = basePath+"/reportmapping.properties";
		log(INFO,"Writing "+path);
		Util.putFile(path, buff, false);
	}

	/**  urlmapping.properties
	 * This file mapps the dnis to the web application. For each entry in vru.ini, an entry in urlmapping.properties is created. There are two types of entries - test entries and application entries
	 * Test Entries have the form 
	 * url.$entryIndexNumber=SCA_IVR_$appname
	 * 
	 * App Entries are 2 lines with the form
	 * # $appName IVR
	 * url.$appdnis=SCA_IVR_$appname
	 * 
	 * Output: urlmapping.properties is written to /urlmapping.properties 
	 * @deprecated
	 */
	public void genUrlmappingProperties(List<VRUFile> entries) {
		String testing = "", app=""; 
		for(int i=0;i<entries.size();i++) {
			//TESTING_ENTRIES			"#url.01111=SCA_IVR_ibmhelp\r\n" +
			testing += "url."+(i%10)+(i%10)+(i%10)+"="+name+"_IVR_"+entries.get(i).appName+"\r\n";
			// APP_ENTRIES 			"# ibmhelp IVR\r\n" +		"url.3062=SCA_IVR_ibmhelp\r\n" + 
			app += "# "+entries.get(i).appName+" IVR\r\n"
				+ "url."+entries.get(i).dnis+"="+name+"_IVR_"+entries.get(i).appName+"\r\n";
		}
		String buff = urlmappingProperties.replaceAll("TESTING_ENTRIES", testing);
		buff = buff.replace("APP_ENTRIES", app);
		
		String path = basePath+"/urlmapping.properties";
		log(INFO,"Writing "+path);
		Util.putFile(path, buff, false);
	}
	
	/** Unpacks template zip file
	 * 
	 * @param pathToTemplateZip
	 * @param appName
	 * @deprecated We don't build a template application
	 */
	public void unpack(String pathToTemplateZip, String appName) {
		name = appName;
		try {
			log(INFO,"Deleting "+basePath);
			if(!new File(basePath).exists()) {
				try { rmdir(new File(basePath)); } catch(Exception e){log(ERROR,"Failed to delete file:"+e.getMessage());}
				log(INFO,"Begin Unpack to "+basePath);
				unzip(pathToTemplateZip, basePath);
			}
		} catch(Exception e){throw new RuntimeException(e);}
		log(INFO,"Unpack Complete");
	}

	public void unpackIni(String pathToIniZip) {
		try {
			if(!new File(Util.normalizePath("ini")).exists()) {
				log(INFO,"Begin Unpack to ini/");
				unzip(pathToIniZip, ".");
			}
		} catch(Exception e){throw new RuntimeException(e);}
		log(INFO,"Unpack Complete");
	}

	public void log(int level, String msg) {
		System.out.println(msg);
	}

	/** Unzips filepath zip file into todir
	 * 
	 * @param filepath Path to zip file
	 * @param todir Basepath to unzip to
	 * @return true on success
	 * @throws IOException If the file cannot be found
	 */
	public boolean unzip(String filepath, String todir) throws IOException {
		todir = Util.normalizePath(todir) + "/";
		filepath = Util.normalizePath(filepath);
		(new File(todir)).mkdirs();
		ZipFile zipFile = new ZipFile(filepath);

	    Enumeration<ZipEntry> enumeration = (Enumeration<ZipEntry>)zipFile.entries();
        byte buff[] = new byte[8192];
	    while (enumeration.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) enumeration.nextElement();

			if (entry.isDirectory()) {
				(new File(todir + entry.getName())).mkdirs();
				continue;
			}

			InputStream in = null;
			OutputStream out = null;
			new File((new File(todir+entry.getName())).getParent()).mkdirs();
            try {
            	in = zipFile.getInputStream(entry);
            	out = new BufferedOutputStream(new FileOutputStream(todir+entry.getName()));
                int size;
                while ((size = in.read(buff)) != -1)
                    if (out != null)
                        out.write(buff, 0, size);
            }
            catch (IOException e) { 
            	return false; 
            }
            finally {
	            try {
	                in.close();
	                if (out != null)
	                    out.close();
	            }
	            catch (IOException e) { 
	            	return false; 
	            }
            }
	    }
		
		return true;
	}
	
	
	
	/** Recursively deletes directory
	 * 
	 * @param dir Folder to remove
	 * @return true on success or false if there was a failure removing a file or folder
	 */
	public static boolean rmdir(File dir) {
		if (!dir.exists())
			return true;
		
		String path = dir.getAbsolutePath().replace('\\', '/');
		if(path.equals("/") || path.equals(".") || path.endsWith(":/"))
			throw new RuntimeException("Cannot safely remove directory: "+path);

		boolean result = true;
		File[] list = dir.listFiles();
		for(int i=0;i<list.length;i++) {
			if(list[i].isDirectory())
				result = result && rmdir(list[i]);
			else
				result = result && list[i].delete();
		}
		return result && dir.delete();
		
	}


	public void genapplicationCallProperties(List<VRUFile> entries) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 
	 * @param entries
	 * 
	<h3>global.properties</h3>
	 * Adds the following global settings from vru.ini
	 * - default language
	 */
	public void genGlobalProperties(List<VRUFile> entries) {
		// TODO Auto-generated method stub
		
	}

}
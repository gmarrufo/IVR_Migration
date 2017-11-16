package com.ibm.ivr.eus.migration;

import java.util.ArrayList;
import java.util.List;


/** Helper class used to parse ini file sections
 * 
 * @author Greg
 *
 */
class INIFile {
	static String currIniText = "";
 	String dnis;
	String appName;
	String parameters = "";
	
	String[] lines = null;
	int lineNumber = 0;
	String sectionName = "";
	
	boolean inSection() {
		if(lines.length <= lineNumber)
			return false;
		return(lines[lineNumber].indexOf('[') != 0);
	}
	
	int startSection(String sectionName) {
		lineNumber = sectionName == null ? lineNumber : 0;
		for(;lineNumber<lines.length;lineNumber++) {
			if(sectionName == null && lines[lineNumber].startsWith("["))
				return lineNumber;
			else if(lines[lineNumber].startsWith("["+sectionName+"]"))
				return lineNumber;
		}
		return -2;
	}
	
	public String[] getSection(String pathToIni, String name) {
				//		FOR each line in [CALLFLOW_INDENTIFIERS_TYPE_CONVERSE] 
		//			1012664;pfzdm.ini;get_call_data='Y';cti_platform='GENESYS'
		// Create a VRU entry
		if(lines == null) {
			currIniText = Util.getFile(pathToIni).replace("\\", "/");
			lines = currIniText.split("\n");
		}
		if(name == null && lineNumber >= lines.length)
			return null;
		int lineSave = lineNumber;
		List<String> result = new ArrayList<String>();
		for(lineNumber=startSection(name)+1;lineNumber > 0 && inSection();lineNumber++) {
			if(result.isEmpty())
				result.add(lines[lineNumber-1].replaceAll("\\[", "").replaceAll("\\]",""));
			result.add(lines[lineNumber]);
		}
		
		if(lineNumber < 0)
			return null; //No sections
		// If we went backwards to get a section, restore the line number
		if(lineNumber < lineSave)
			lineNumber = lineSave;
		
		return (String [])result.toArray(new String[result.size()]);
		
	}

	public List<String []> getSections(String pathToIni) {
		List<String []> sections = new ArrayList<String []>();
		String[] section = getSection(pathToIni, "GLOBAL_VARS"); 
		if(section == null || section.length == 0)
			throw new RuntimeException("NoGLOBAL_VARS:GLOBAL_VARS not found in "+pathToIni);
		do {
			sections.add(section);
			section = getSection(pathToIni, null);
		} while(section != null);
		return sections;
	}
	
}
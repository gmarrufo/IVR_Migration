package com.ibm.ivr.eus.migration;

import java.util.ArrayList;
import java.util.List;


/**  Parses VRU file which is the global ini file 
 * 
 * @author Greg
 *
 */
class VRUFile extends INIFile {
	
	VRUFile parse(String line) {
		VRUFile result = new VRUFile();
		String[] items = line.split(";");
		if(items.length < 3)
			return null;
		result.dnis = items[0];
		result.appName = items[1].split("\\.")[0];
		for(int i=2;i<items.length;i++)
			result.parameters += ((i>2) ? "&" : "") + items[i];
		return result;
	}
	
	public List<VRUFile> interpret(String pathToIni) {
		//		FOR each line in [CALLFLOW_INDENTIFIERS_TYPE_CONVERSE] 
		//			1012664;pfzdm.ini;get_call_data='Y';cti_platform='GENESYS'
		// Create a VRU entry
		lines = Util.getFile(pathToIni).split("\n");
		List<VRUFile> result = new ArrayList<VRUFile>();
		for(lineNumber=startSection("CALLFLOW_INDENTIFIERS_TYPE_CONVERSE")+1;lineNumber > 0 && inSection();lineNumber++) {
			VRUFile vru = parse(lines[lineNumber]);
			if(vru != null)
				result.add(vru);
		}
		return result;
		
	}
	
}
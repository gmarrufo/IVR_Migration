package com.ibm.ivr.eus.plugin.expressions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class Util {

	/** Gets the contents of a file
	 * @param path Path to the file
	 */
	public static String getFile(String path) {
		FileInputStream fr = null;
	
		try {
			File f = new File(path.replace("\\", "/"));
			fr = new FileInputStream(f);
			byte[] buff = new byte[(int)f.length()];
			fr.read(buff);
			fr.close();
			return(new String(buff,"UTF8"));
		} catch(Exception e) {
		}
		try {fr.close();} catch(Exception e2) {}
		return null;
	}
	
	public static boolean putFile(String path, String file, boolean append)	{
		FileOutputStream fos = null;
		OutputStreamWriter out = null;
		try
		{
			File f = new File(path.replace("\\", "/"));
			
			// Create the directory tree
			if(f.getParent() != null)
				new File(f.getParent()).mkdirs();

			fos = new FileOutputStream(f, append);
			out = new OutputStreamWriter(fos, "UTF8");

			out.write(file);

			out.close();
			fos.close();
			return true;
		}
		catch (Exception e){}
	
		try {out.close(); fos.close();} catch(Exception e2) {}

		return false;
	}

	/** Joins each element of args with delim
	 * 
	 * @param args list of strings to join
	 * @param string seperator character
	 * @return Joined string
	 */
	public static String join(Object[] args, String string) {
		StringBuffer result = new StringBuffer();
		for(int i=0;args != null && i < args.length;i++)
			result.append((i>0 ? string : "") + (args[i] == null ? null : args[i].toString()));
		return result.toString();
	}
}
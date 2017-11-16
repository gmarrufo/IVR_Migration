package com.ibm.ivr.eus.migration;

import java.io.*;

/** Utility functions
 * 
 * @author Greg
 *
 */
public class Util {

	/** Normalizes the path to use the directory containing the jar file (or the base directory if there is no jar file)
	 * 
	 * @param path
	 * @return
	 */
	public static String normalizePath(String path) {
		try{
			String cd = new File("").getCanonicalPath();
			String path2 = new File(path).getCanonicalPath();
			if(path2.indexOf(cd) == 0) {
				//System.out.println("$$$ 0. cd ="+cd+", path2="+path2+", path="+path);
				path = path2.substring(cd.length() + (cd.length() < path2.length() ? 1 : 0));
				//path2 = Thread.currentThread().getContextClassLoader().getResource("").getPath() + path;
				//System.out.println("$$$ 1. path2 ="+path2);
				//path2 = new File(path2).getCanonicalPath();
				//System.out.println("$$$ 2. path2 ="+path2);
				path2 = new File(Util.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent() +"/"+ path;
				//System.out.println("$$$ 3. path2 ="+path2);
				path = path2.replace('\\','/');
				//System.out.println("$$$ 4. path ="+path);
			}
		}catch(Exception e) {e.printStackTrace();}
		return path;
	}

	
	/** Gets the contents of a file
	 * @param path Path to the file
	 */
	public static String getFile(String path) {
		FileInputStream fr = null;
	
		try {
			path = normalizePath(path);
			//System.out.println("$$$ READING FROM "+path);

			File f = new File(path);
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

	
	public static String getFileOrResource(String path) {
	String buff = Util.getFile(path);
	
	try { 
		// If the file was not found, look for it in the jar file
		if(buff == null || buff.length() == 0) {
			String path2 = path.replace('\\','/');
			// Strip off the base path to the current directory
			String basePath = new File(".").getCanonicalPath().replace('\\','/');
			if(path2.indexOf(basePath) >= 0)
				path2 = path2.substring(basePath.length() + (basePath.endsWith("/") ? 0 : 1));
			
			buff = Util.getFile(Thread.currentThread().getContextClassLoader().getResourceAsStream(path2));
			System.out.println("Read "+buff.length()+" bytes from jar file path:" + path2+" basepath="+basePath);
		}
	} catch(Exception e) {
			e.printStackTrace();
	}
	
	return buff;
}

	public static String getFile(InputStream stream) {
		StringBuffer result = new StringBuffer();		
		try {
			
			BufferedInputStream bi = new BufferedInputStream(stream);
			byte[] buff = new byte[4096];
			while(bi.available() > 0) {
				int count = bi.read(buff);
				result.append(new String(buff,0,count,"UTF8"));
			}
			bi.close();

		} catch(Exception e) {
		}
		return result.toString();
	}

	/** Writes a file specified by path
	 * 
	 * @param path Path to file
	 * @param file File data
	 * @param append True if we should append data to the file
	 * @return
	 */
	public static boolean putFile(String path, String file, boolean append)	{
		path = normalizePath(path);
		FileOutputStream fos = null;
		OutputStreamWriter out = null;
		try
		{
			File f = new File(path);
			
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

	 /** Returns the integer value of a string */
	 public static int val(String term) {
		int result = 0;
		try {
			result = Integer.parseInt(term.trim());
		}
		catch (Exception e)	{
			try	{
				result = (int)Double.parseDouble(term.trim());
			}
			catch (Exception ex) {}
		}
		return (result);
	 }

}

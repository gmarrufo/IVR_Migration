package com.ibm.ivr.eus.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.apache.log4j.Logger;

import com.ibm.ivr.eus.handler.CreateBelkReport;

public class LFileFunctions {

	private static Logger LOGGER 					= Logger.getLogger(LFileFunctions.class);

	
	/**
	 * WriteToFile - writes contents to a file
	 * @param logToken - String containing the callid. ex. ([callid])
	 * @param filename - Path and filename of the file to write to.
	 * @param fileData - The data to write to the file.
	 * @param append - Whether or not to append to a file.
	 */
	public static void writeToFile(String logToken, String filename, String fileData, boolean append){
		File file=null;
		FileWriter writer=null;
		BufferedWriter buffWriter=null;
		try{
			file=new File(filename);
			if(!file.exists()){
				if(!file.createNewFile()){
					LOGGER.info(new StringBuffer(logToken).append("Cannot create file:"+filename));
					return;
				}
				writer=new FileWriter(filename);
			}else{
				writer=new FileWriter(filename, append);
									
			}
			buffWriter=new BufferedWriter(writer);
			buffWriter.write(fileData);
			buffWriter.newLine();
			buffWriter.flush();
		}catch(Exception e){
			LOGGER.error(new StringBuffer(logToken).append("Exception::"+e.toString()),e);
		}finally{
			if(buffWriter != null){
				try{
					buffWriter.close();
				}catch(java.io.IOException io){
					LOGGER.error(new StringBuffer(logToken).append("Exception::"),io);
				}
			}
				
			if(writer != null){
				try{
					writer.close();
				}catch(java.io.IOException io){
					io.printStackTrace();
					LOGGER.error(new StringBuffer(logToken).append("Exception::"+io.toString()),io);
				}
			}
		}
	}	
	
	/**
	 * Function that deletes a file from the file system.
	 * @param filename - File and directory path of the file to delete.
	 * @param logToken - StringBuffer containing the callid. ex. ([callid])
	 */
	public static void deleteFile(String logToken, String filename){
		File file=null;
		
		try{
			file=new File(filename);
			if(file.exists()){
				if(!file.delete()){
					LOGGER.debug(new StringBuffer(logToken).append("Error deleting File:"+filename));
				}
			}else{
				LOGGER.info(new StringBuffer(logToken).append("File:"+filename+" does not exist"));				
			}
		}catch(Exception e){
			LOGGER.error(new StringBuffer(logToken).append("Error in deleteFile:"+filename+" Exception:"+e.toString()),e);
		}
	}	
	
	/**
	 * Creates a file
	 * @param filename - path and file name
	 * @param logToken - string token containing the callid.  Used for logging purposes.
	 * @return
	 */
	public static boolean createFile(String filename, String logToken){
		boolean wasCreated=false;
		File file=null;
		try{
			file=new File(filename);
			if(!file.exists()){
				if(!file.createNewFile()){
					System.out.println(new StringBuffer(logToken).append("Cannot create file:"+filename));
					wasCreated=false;
				}else{
					wasCreated=true;
				}
			}		
		}catch(Exception e){
			System.out.println(new StringBuffer(logToken).append("Error creating file:"+e.toString()));
		}
		return wasCreated;
	}
}

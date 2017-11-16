package com.ibm.ivr.eus.websrv.belk;

import org.apache.log4j.Logger;

public class EUSBelkImpl implements EUSTTBelk {
	private static Logger LOGGER 								= Logger.getLogger(EUSBelkImpl.class);


	@Override
	public String xPrintOverNightReports(String printerName,
			String reportsToPrint) {
		// TODO Auto-generated method stub
		String result="";
		try{
		EUSTTBelkService service=new EUSTTBelkService();
		EUSTTBelk port=service.getEUSTTBelk();
		result=port.xPrintOverNightReports(printerName, reportsToPrint);
		}catch(Exception e){
			LOGGER.error("ERROR EUSBelkImpl",e);
		}
		return result;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		EUSBelkImpl impl=new EUSBelkImpl();
		String result=impl.xPrintOverNightReports("P514", "SPIF");
		System.out.println("Result:"+result);
	}

}

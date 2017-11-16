package com.ibm.ivr.eus.common;

import java.util.Calendar;
import java.text.SimpleDateFormat;
 
public class GetCurrentTimeStamp {
    public String getDateTimeNow(){
	  Calendar currentDate = Calendar.getInstance();
	  SimpleDateFormat formatter= 
	  new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss");
	  String dateNow = formatter.format(currentDate.getTime());
	  return dateNow;
    }
}
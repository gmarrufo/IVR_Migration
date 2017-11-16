<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!-- Version 09/8/2010 -->

<HTML>
<HEAD>
<%@page import="java.util.*"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.net.URL"%>
<%@page import="java.io.*"%>
<%@page import="com.ibm.ivr.framework.utilities.*"%>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<META http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<META name="GENERATOR" content="IBM Software Development Platform">
<META http-equiv="Content-Style-Type" content="text/css">

<TITLE>TestDAO.jsp</TITLE>
</HEAD>
<BODY>
<P>Place content here.</P>
<%		
		IVRStatisticsDAO iDAO = new IVRStatisticsDAO("jdbc/IVRFramework");
		
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			long d1 = formatter.parse("2008-10-11").getTime();
			long d2 = formatter.parse("2008-10-14").getTime();
			long num = iDAO.queryByAreaCode("IVRApp", "972", "Hangup", d1, d2);
			out.println("" + num + " calls from area code 972 for Hangup");
			 
		}
		catch (Exception ex) {
			System.out.println("General Exception while saving/appending into DB: " + ex.getMessage());
		}//end catch			
%>
</BODY>
</HTML>

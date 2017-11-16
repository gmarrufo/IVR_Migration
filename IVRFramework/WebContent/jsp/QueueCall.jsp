<?xml version="1.0" encoding ="iso-8859-1"?>
<!-- Version 09/8/2010 -->

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!-- This is the example file for issuing request to URS to queue the call on GVP platform -->
<!-- Set this variable in the session, so when the call is sent back to the app again, it can be identified
	 as coming back from URS vs. completely new call -->
<%session.setAttribute("queueCall", "DONE"); %>

<XMLPage TYPE="IVR" PAGEID="" SESSIONID="" HREF="<%=request.getContextPath()%>/TransferServlet?routeRequest=DONE">

	<QUEUE_CALL USR_PARAMS="GenesysRouteDN:<%=request.getParameter("dest")%>"/>
		
	<LEG_WAIT TIMEOUT="5" HREF="<%=request.getContextPath()%>/TNTServlet?exitReason=QueueCallError&amp;defaultRouting=queuecall&amp;errorMessage=QueueCallTimedOut"/>

</XMLPage>

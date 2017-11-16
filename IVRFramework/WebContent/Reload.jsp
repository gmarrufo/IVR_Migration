<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<%@page import="java.io.FileReader"%><html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta http-equiv="Content-Style-Type" content="text/css">
<link rel="stylesheet" href="theme/blue.css" type="text/css">
<title>Reload</title>

<%  
	String propertyPath = (String)this.getServletContext().getAttribute("propertyFilePath");
	java.util.Properties prop = new java.util.Properties();
	try {
	java.net.URL url = new java.net.URL(propertyPath +"/global.properties");
	java.io.InputStream is = url.openStream();
	prop.load(is);
	is.close();
	} catch(Exception e) {
		// gps: If the propertyFilePath is not set, by default look for property files in
		// /WEB-INF/classes/global.properties Files get copied here from your 
		// java src dir. This was added to facilitate "out of the box" operation
		// without necessitating workspace specific overrides
		String defaultPath = "/WEB-INF/classes/global.properties";
		//Log(WARN, "propertyFilePath not set. Using "+defaultPath);
		propertyPath = this.getServletContext().getRealPath("/WEB-INF/classes/global.properties");
		prop.load(new FileReader(propertyPath));
	}
%>


<script type="text/javascript">
function Validate()
{
    if (document.ivrmappingForm.dnis.value.length > 0)
        return true;
    alert('You must enter a dnis to have the XML file loaded and mapped');
    return false;
}
</script>

</head>
<body>
<table width="760" cellspacing="0" cellpadding="0" border="0">
	<caption align="top"></caption>
	<tbody>
      <tr>
         <td valign="top">
         <table class="header" cellspacing="0" cellpadding="0" border="0" width="100%">
            <tbody>
               <tr>
                  <td width="150"><!-- Place your page content here. --><img border="0" width="150" height="55" alt="Company's LOGO" src="theme/logo_blue.gif"></td>
                  <td valign="bottom" align="right"><img border="0" width="80" height="55" alt="IBM's LOGO" src="theme/ibm-logo_28.jpg"></td>
               </tr>
            </tbody>
         </table>
         </td>
      </tr>
      <tr>
         <td height="1"></td>
      </tr>
      <tr class="content-area">
         <td ivalign="top" height="150"><font
				size="-1"></font><center><table border="10">
				<tbody>
					<tr>
						<td><b>Framework Properties</b></td>
						<td><b>Action</b></td>
					</tr>
					<tr>
						<td>
						<form action="ReloadServlet" method="get">global.properties
						</td>
						<td><input type="submit" name="GlobalProp" value="Reload"></td>
						</form>
					</tr>
					<tr>
						<td>
						<form name="ivrmappingForm" action="ReloadServlet" method="get" onsubmit="return Validate()">ivrmapping.properties --&gt; dnis: 
						<input type="text" name="dnis" size="10"> 
						</td>
						<td><input type="submit" name="IVRMapping" value="Map"></td>
						</form>
					</tr>
					<tr>
						<td><form action="ReloadServlet" method="get">logging level
						<select name="level">
							<option value="info" selected>info</option>
							<option value="debug">debug</option>
							<option value="trace">trace</option>
						</select>
						</td>
						<td><input type="submit" name="Logging" value="Set"></td>
						</form>
					</tr>
				</tbody>
			</table></center>
			<td height="1"></td>
		</td>
		</tr>
		<tr>
			<td ivalign="top" height="150"> <!-- Place your page content here. -->
			<hr>
			<hr>
			<font
				size="3"><b>Please select an application properties file to reload.</b></font>
			<form action="ReloadServlet" method="get">Application properties: <select name="app_properties">
				<option value="all" selected>ALL</option>
				<%for(int i=0; ; i++){
					String appprop = prop.getProperty("app.prop" + i);
					if (appprop == null || appprop.trim().length() == 0)
						break;
					else %>
						<option value="<%=appprop%>"><%=appprop%></option>
				<%		
				} %>
			</select><input type="submit" name="Reload" value="Reload"></form>
			<br>
			<hr>
			<hr>
			<font
				size="3"><b>Please select an application XML file to reload.</b></font>
			<form action="ReloadServlet" method="get">Application XML files: <select name="app_xml">
				<option value="all" selected>ALL</option>
				<%
					String callflowDir = prop.getProperty("callflowPath");
					if (callflowDir != null) {
						callflowDir = callflowDir.trim();
						if (callflowDir.length() == 0) {
							callflowDir = this.getServletContext().getRealPath("/WEB-INF/CallRoutingConfig");
						} else if (!callflowDir.endsWith("/")){
							callflowDir = callflowDir + "/";
						}
					} else {
						callflowDir = this.getServletContext().getRealPath("/WEB-INF/CallRoutingConfig");
					}
					String[] files = new java.io.File(callflowDir).list();
					for(int i=0;i<files.length;i++) {
						if (files[i].toUpperCase().endsWith(".XML"))
				 %>
						<option value="<%=files[i]%>"><%=files[i]%></option>
				<%		
				} %>
			</select><input type="submit" name="AppXML" value="Reload"></form>
			<br>
			<hr>
			<hr>
			<font size="3"><b>Please select the alert to be cleared off.</b></form>
			<form action="ReloadServlet" method="get">Alert name:  
				<select name="emailAlert">
				<%java.util.Properties emailAlerts = (java.util.Properties)this.getServletContext().getAttribute("emailAlerts");
				java.util.Enumeration e = emailAlerts.keys();
				while (e.hasMoreElements()){
					String key = e.nextElement().toString(); %>
						<option value="<%=key%>"><%=key%></option>
				<%		
				} %>
			</select>
			<input type="submit" name="Clear" value="Clear"></form>
			<br>
			</td>	
      </tr>
      
      <tr>
         <td valign="top" align="center" height="20" class="footer">&copy;2010
    IBM Corporation</td>
      </tr>
   </tbody>
</table>
</body>
</html>

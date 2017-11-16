<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta http-equiv="Content-Style-Type" content="text/css">
<link rel="stylesheet" href="theme/blue.css" type="text/css">
<title>Reload</title>

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
						<td><b>Reload Request Result</b></td>
						<td><b>Action</b></td>
					</tr>
					<tr>
						<td>
						<form action="Reload.jsp" method="get"><%=request.getAttribute("ReloadResult")%>
						</td>
						<td><input type="submit" value="Return to Reload Page"></td>
						</form>
					</tr>
				</tbody>
			</table></center>
			<td height="1"></td>
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

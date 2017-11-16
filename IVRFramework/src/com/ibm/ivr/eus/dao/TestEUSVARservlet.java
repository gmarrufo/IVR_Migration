package com.ibm.ivr.eus.dao;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/*
 *  
 *      appName is used as the table name in the EUSDB - ie IBMHELP for the IBM Help application
 * 		KEY is the application variable name
 * 		VALUE is the unique String value for the KEY
 * 
 * 		There are three methods:
 * 				<EUSVARDAO>.seteusKEY(<EUSVARDTO>)  puts a single KEY-VALUE pair (row) into the DB
 * 				<EUSVARDAO>.geteusKEY("appName","KEY") retrieve a single KEY-VALUE pair from the DB
 * 				<EUSVARDAO>.deleteKey("appName","KEY") deletes one row (or more) from DB
 * 								(KEY may contain '_' to match any char in that position
 * 											or   '%' to match one or more chars in that position)
 * 				<EUSVARDAO>.getAllAppVars("appName") returns an array of EUSVARDTO objects for all KEY-VALUE pairs in the DB

 */

public class TestEUSVARservlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    private static Logger LOGGER = Logger.getLogger(TestEUSVARservlet.class);
    
  public void doGet (HttpServletRequest req,
                     HttpServletResponse res)
    throws ServletException, IOException
  {
    
    String jndiName = "jdbc/EUSDB";

    boolean rcb = false;
    EUSVARDTO returndto;
    EUSVARDTO returndto2;
    EUSVARDTO [] allvars = null;
		
	String appName = "TESTAPP";
	
	LOGGER.debug(new StringBuffer("********Starting TestEUSVAR Servlet********"));
    
    try {
    	EUSVARDTO eusvardto = new EUSVARDTO();
    	eusvardto.setAppName(appName);
    	eusvardto.setKEY("testKEY");
    	eusvardto.setVALUE("testVALUE");
 
       	EUSVARDTO eusvardto2 = new EUSVARDTO();
    	eusvardto2.setAppName(appName);
    	eusvardto2.setKEY("testKEY2");
    	eusvardto2.setVALUE("testVALUE2");

 
    	/* create data access object interface to db */
    	EUSVARDAO eusvardao = new EUSVARDAO(jndiName, "test", true, 8);

    	/*  Delete any existing test rows from table to start off test */
       	rcb = eusvardao.deleteKEY(appName, "testKEY_");
      	rcb = eusvardao.deleteKEY(appName, "%");
      	    	
    	/* add values */
    	
    	rcb = eusvardao.seteusKEY(eusvardto);
    	rcb = eusvardao.seteusKEY(eusvardto2);
    	LOGGER.debug(new StringBuffer("Added KEY-VALUE pairs"));
    	
    	/* look up some data values */
    	returndto = eusvardao.geteusKEY(appName, "testKEY");
    	returndto2 = eusvardao.geteusKEY(appName, "testKEY2");

  		/* return all vars */
    	allvars = eusvardao.getAllAppVars(appName);
   	
    	/* delete a row */
    	rcb = eusvardao.deleteKEY(appName, "testKEY");
    	
    	/* delete a row that doesn't exist */
    	rcb = eusvardao.deleteKEY(appName, "testKEY");
   	
    	/* update a key-value pair that exists*/
    	eusvardto2.setVALUE("updatedTestVALUE2");
   		rcb = eusvardao.seteusKEY(eusvardto2);
   		
   		/* check value of updated var */
    	returndto2 = eusvardao.geteusKEY(appName, "testKEY2");
    	LOGGER.debug(new StringBuffer("Updated KEY:testKEY2, VALUE:"+returndto2.getVALUE()));
  		
  		
   		/* ============================ error cases =================================================================== */
   		/* null app/tablename */
       	eusvardto.setAppName(null);
    	eusvardto.setKEY("testKEY");
    	eusvardto.setVALUE("testVALUE");
    	
       	rcb = eusvardao.seteusKEY(eusvardto);
       	returndto = eusvardao.geteusKEY(null, "testKEY");
    	rcb = eusvardao.deleteKEY(null, "testKEY");
    	allvars = eusvardao.getAllAppVars(null);
    	
    	
 
   		/* null key */
       	eusvardto.setAppName(appName);
    	eusvardto.setKEY(null);
    	eusvardto.setVALUE("testVALUE");
    	
       	rcb = eusvardao.seteusKEY(eusvardto);
       	returndto = eusvardao.geteusKEY(appName, null);
    	rcb = eusvardao.deleteKEY(appName, null);
    	
       	/*  Delete any test rows  */
       	rcb = eusvardao.deleteKEY(appName, "testKEY_");

        
    	
    
	} catch (Exception e) {
		
		e.printStackTrace();
	}	
		
	}
}


package com.ibm.ivr.eus.dao;

import java.io.IOException;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;



public class TestEUSSETservlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    private String logToken = null;
    private static Logger LOGGER = Logger.getLogger(TestEUSSETservlet.class);
    
  public void doGet (HttpServletRequest req,
                     HttpServletResponse res)
    throws ServletException, IOException
  {
	  LOGGER.debug(new StringBuffer("********Starting TestServlet********"));
    
    String jndiName = "jdbc/EUSDB";

    String rc = null;
    String temp = null;
    boolean rcb = false;
    EUSSETDTO returndto;

    
		
	String setName = "IBM_NA_EFH_ALPHA";
	Vector<StringBuffer> valueSet = new Vector<StringBuffer>();
	
    
    try {
    	/* 
    	 */

 
    	
 
    	/* create data access object interface to db */
    	EUSSETDAO eussetdao = new EUSSETDAO(jndiName, "test", true, 8);
    	
    	/* first delete all test rows from set */
    	eussetdao.deleteKeyFromSET(setName, "xyzzy_");

    	/* add some data values */
       	EUSSETDTO eussetdto = new EUSSETDTO();
    	eussetdto.setSetName(setName);
    	valueSet.clear();
    	valueSet.add(new StringBuffer("xyzzy1;;6530LQ-897;; NA2NA1;;9192942662;;NA;;NA"));
    	eussetdto.setKEY(valueSet.elementAt(0).substring(0,6));
    	eussetdto.setVALUESET(valueSet);   	
    	rcb = eussetdao.addtoSET(eussetdto);
 
    	valueSet.clear();
       	valueSet.add(new StringBuffer("xyzzy2;;653006-897;; NA1NA1;;5618622140;;NA;;amcdonle@us.ibm.com"));
    	eussetdto.setKEY(valueSet.elementAt(0).substring(0,6));
    	eussetdto.setVALUESET(valueSet);   	
    	rcb = eussetdao.addtoSET(eussetdto);
    	
    	valueSet.clear();
    	valueSet.add(new StringBuffer("xyzzy3;;653009-897;; NA2NA1;;4258035836;;NA;;broman@us.ibm.com"));
    	eussetdto.setKEY(valueSet.elementAt(0).substring(0,6));
    	eussetdto.setVALUESET(valueSet);   	
    	rcb = eussetdao.addtoSET(eussetdto);
    	
    	valueSet.clear();
    	valueSet.add(new StringBuffer("xyzzy4;;653014-897;; NA2NA1;;4258035848;;NA;;jbez@us.ibm.com"));   	
    	eussetdto.setKEY(valueSet.elementAt(0).substring(0,6));
    	eussetdto.setVALUESET(valueSet);   	
    	rcb = eussetdao.addtoSET(eussetdto);
    	
       	/* lookup finds no rows */
    	returndto = eussetdao.geteusSET(setName, "youwontfindthis");

       	if (returndto == null) {
        		
			if (LOGGER.isDebugEnabled()) LOGGER.debug("Key: youwontfindthis returned null");

    	}
   	
    	/* lookup finds one row */
    	returndto = eussetdao.geteusSET(setName, "xyzzy1");
    	if (returndto != null) {
    		valueSet = returndto.getVALUESET();		
			if (LOGGER.isDebugEnabled()) LOGGER.debug("Key: xyzzy1 returned value:"+valueSet.get(0).toString());

    	}
       	/* lookup finds one row */
    	returndto = eussetdao.geteusSET(setName, "653006");
    	valueSet = returndto.getVALUESET();	
       	if (returndto != null) {
    		valueSet = returndto.getVALUESET();		
			if (LOGGER.isDebugEnabled()) LOGGER.debug("Key: 653006 returned value:"+valueSet.get(0).toString());

    	}
    	

       	
   		/* lookup return multiple matches */
    	returndto = eussetdao.geteusSET(setName, "xyzzy_");
    	valueSet = returndto.getVALUESET();	
    	for (int i=0;i<valueSet.size();i++) {
			if (LOGGER.isDebugEnabled()) LOGGER.debug("Key: xyzzy _ returned value:"+valueSet.get(i).toString());

    	}
   		
   		
   		/* ============================ error cases =================================================================== */
   		/* null set/tablename */
       	eussetdto.setSetName(null);
    	eussetdto.setKEY("xyzzy1");

       	rcb = eussetdao.addtoSET(eussetdto); 	
       	
       	returndto = eussetdao.geteusSET(null,"xyzzy1");
       	
       	rcb = eussetdao.deleteKeyFromSET(null, "xyzzy1");
       	
   	
 
   		/* null key */
       	eussetdto.setSetName(setName);
    	eussetdto.setKEY(null);
    	
       	rcb = eussetdao.addtoSET(eussetdto);
       	returndto = eussetdao.geteusSET(setName, null);
    	rcb = eussetdao.deleteKeyFromSET(setName, null);
    	
   		/* add where already in table */
   		/* update where not in table */
        
       	/* delete any test rows from set */
    	eussetdao.deleteKeyFromSET(setName, "xyzzy_");

    
	} catch (Exception e) {
		
		e.printStackTrace();
	}	
		
	}
}


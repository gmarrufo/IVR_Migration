package com.ibm.ivr.eus.common;

import java.io.IOException;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.*;
import javax.xml.xpath.*;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XPathReader {
    
	private static Logger LOGGER = Logger.getLogger(XPathReader.class);
    private String xmlFile;
    private Document xmlDocument;
    private XPath xPath;
    private String logToken="";
    private String callId="";
    
    public XPathReader(String xmlFile, String logToken) {
        this.xmlFile = xmlFile;
        this.logToken=logToken;
        this.callId=callId;
        initObjects();
    }
    
    private void initObjects(){        
        try {
        	String logToken = new StringBuffer("[").append(callId).append("] ").toString();
            xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);            
            xPath =  XPathFactory.newInstance().newXPath();
        } catch (IOException io) {
        	LOGGER.error(new StringBuffer(logToken).append("IO Exception"),io);
        } catch (SAXException saxEx) {
        	LOGGER.error(new StringBuffer(logToken).append("SAX Exception"),saxEx);
        } catch (ParserConfigurationException ex) {
        	LOGGER.error(new StringBuffer(logToken).append("ParseConfiguration Exception"),ex);
        }catch(Exception e){
           	LOGGER.error(new StringBuffer(logToken).append("Exception"),e);       	
        }
    }
    
    public Object read(String expression, 
			QName returnType){
        try {
           	LOGGER.debug(new StringBuffer(logToken).append("Searching XML for path:").append(expression));
            XPathExpression xPathExpression = 
			xPath.compile(expression);
	        return xPathExpression.evaluate
			(xmlDocument, returnType);
        } catch (XPathExpressionException ex) {
           	LOGGER.error(new StringBuffer(logToken).append("XPathExpression Exception"),ex);
            return null;
        }catch (Exception e){
           	LOGGER.error(new StringBuffer(logToken).append("Exception"),e);
           	return null;
        }
    }

/**
 * @param args
 */
public static void main(String[] args) {
	 XPathReader reader = new XPathReader("C:\\Brian\\mig\\Belk\\BelkResponse.xml", "");
	 
	    // To get a child element's value.'
        String expression = "/Envelope/Body/xPrintOverNightReportsResponse/xPrintOverNightReportsReturn";
        System.out.println(reader.read(expression, 
			XPathConstants.STRING) + "\n");
}

}

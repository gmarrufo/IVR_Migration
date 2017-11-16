package com.ibm.ivr.eus.dao;

/**
 * This is the exception that will be thrown by all DAO objects, if any data access 
 * error occur. This is considered critical exception and should be caught & logged 
 * and an appropriate error message should be passed. If any DAO access is not
 * considered critical for the transaction success, they would need to subclass 
 * from this object.
 * 
 * @author Kapil
 * @version 11-11-10 mcdonley: added serialVersionUID to remove warning
 */
public class DAOException extends Exception {
	static final long serialVersionUID = 2;
	
	/** 
     * Creates a new instance of DAOException 
     */
    public DAOException() {
        super();
    }//end constructor
    
    /** 
     * Creates a new instance of DAOException 
     */
    public DAOException(String s) {
        super(s);
    }//end constructor    
    
    /** 
     * Creates a new instance of DAOException
     */
    public DAOException(Throwable exception) {
        super(exception);
    }//end constructor
    
    /** 
     * Creates a new instance of DAOException 
     */
    public DAOException(Throwable exception, String s) {
        super(s, exception);
    }//end constructor
    
}//end class DAOException
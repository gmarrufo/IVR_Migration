package com.ibm.ivr.eus.plugin.expressions;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map; 

import com.ibm.ivr.eus.plugin.ExpressionPluginImpl;


/**  Class that provides implementations for functions that are referenced by the framework 
 * inside of parenthesis (). For example, if the framework calls a function to round up the 
 * users wait time to the nearest 15 seconds, that function could be referenced like this
 * 
 * <pre>
 *  <Set name="waitTime" expr="(round(waitTime))"/>
 * </pre>
 *  
 *  In addition to the cond and expr attributes which reference functions, any element or 
 *  attribute that currently supports (varname:metadata) syntax will also be extended to 
 *  support variables. The change to support this extension should be made in a single location for 
 *  easier maintenance
 *  
 *  In order to register your functions reference a handler that extends BaseFunctionHandler
 * 
 * @author Greg 
 *
 */
public class Expressions  {

	/** Used by expressions not tied to a servlet session */
	 static Map vars = new HashMap();


	 /** Returns the integer value of a string */
	 public int val(String term) {
		int result = 0;
		try {
			result = Integer.parseInt(term.trim());
		}
		catch (Exception e)	{
			try	{
				result = (int)Double.parseDouble(term.trim());
			}
			catch (Exception ex) {}
		}
		return (result);
	 }

	 /** Returns the double value of a string */
	 public double valDouble(String term) {
		double result = 0;
		try {
			result = Double.parseDouble(term);
		}
		catch (Exception e) {}
		return (result);
	}

	 /** Check a boolean relation betewwn two expressions
	  *  
	  * @param term1 
	  * @param op
	  * @param term2
	  * @return
	  */
	 public String relation(Object t1, String op, Object t2) {
		 //var1 == 'VMPW_RESET' && LENGTH(send_data) >= 9 && POS('-',send_data) <> 0
		 String term1 = ""+t1;//get(t1);
		 String term2 = ""+t2;//get(t2);
		 
		 String value = "" +((op.equals("EQ") || op.equals("==")) ? term1.equalsIgnoreCase(term2) :
			 				 (op.equals("GTEQ") || op.equals(">=")) ? val(term1) >= val(term2) :
			 				 (op.equals("GT") || op.equals(">")) ? val(term1) > val(term2) :
			 				 (op.equals("LT") || op.equals("<")) ? val(term1) < val(term2) :
			 				 (op.equals("LTEQ") || op.equals("<=")) ? val(term1) <= val(term2) :
			 				 (op.equals("NE") || op.equals("!=")|| op.equals("<>")) ? !term1.equalsIgnoreCase(term2) :
			 				 (op.equals("OR") || op.equals("||")) ? term1.equalsIgnoreCase("true") || term2.equalsIgnoreCase("true") :
		 					 (op.equals("AND") || op.equals("&&")) ? term1.equalsIgnoreCase("true") && term2.equalsIgnoreCase("true") : false);
			 					
		 if(debug) log("relation():'"+term1 + "' "+op +" '"+ term2 +"'="+value);
		 return value;
	 }

	 
	 /** Perform math on 2 expressions or add 2 strings.  If the operator is '+' and the first term is non-numeric, add them as strings
	  * 
	  * @param term1 String or numeric term. 
	  * @param op Operator
	  * @param term2 String or numeric term
	  * @return result of the math or string operation
	  */
	 public Object math(Object term1, String op, Object term2) {
		 Object value = null;
		 if(term1 instanceof String && op.equals("+") &&
			!(!(term2 instanceof String) && ((String)term1).length() == 0)
		 	) {
			 
			 if(((String)term1).length() == 0)
				 System.out.println(""+term1);
			 
			 value = ""+term1+term2;
		 
		 } else if((""+term1).indexOf('.') >= 0) {
			 double d =  (op.equals("+") ? valDouble(""+term1) + valDouble(""+term2) :
				  op.equals("-") ? valDouble(""+term1) - valDouble(""+term2) :
				  op.equals("*") ? valDouble(""+term1) * valDouble(""+term2) :
				  op.equals("/") ? valDouble(""+term1) / valDouble(""+term2) : 0.0);
			 value = d;
		 } else {
			 int i = 	  (op.equals("+") ? val(""+term1) + val(""+term2) :
				 			  op.equals("-") ? val(""+term1) - val(""+term2) :
			 				  op.equals("*") ? val(""+term1) * val(""+term2) :
			 				  op.equals("/") ? val(""+term1) / val(""+term2) : 0);
			 value = i;
		 }
		 if(debug) log("math() :"+term1 + op + term2 +"="+value);
			 					
		 return value;
	 }

	 //TODO: Pass in array of objects instead of a string of args to parse here
	 public Object function(String name, Object o) {
		 Object[] args = (Object[]) o;
		 name = name.toUpperCase();
		 Method[] methods = this.getClass().getMethods();
		 for(int i=0;i<methods.length;i++) {
			 /*if(methods[i].getName().equalsIgnoreCase("TREXX")) {
				 args = new Object[]{"'"+Util.join(args, "','")+"'"};
			 }*/
			 if(methods[i].getName().equalsIgnoreCase(name) && 
					 ((args == null && (methods[i].getParameterTypes() == null || methods[i].getParameterTypes().length == 0)) ||
					 args.length == methods[i].getParameterTypes().length)) {
				 //if(debug) log("function(): Calling method "+methods[i].getName()+"("+args+")");
				 //String[] argList = args.split(",");
				 
				 Object result = null;
				 try {
					 Class[] types = methods[i].getParameterTypes();
					 for(int j=0;j<types.length;j++) {
						 if(!args[j].getClass().equals(types[j]))
							 args[j] = changeType(args[j], types[j]);
					 }
					 result = methods[i].invoke(this, args);
				 } catch(Exception e) {
					 log(ERROR,"ERROR Evaluating "+ name+"("+arrayToString(args)+"): "+e.getMessage());
					 e.printStackTrace();
				 }
				 if(result == null)
					 result = "";
				 if(debug) log(""+methods[i].getName()+"("+arrayToString(args)+")="+result+" ");
				 return result;
			 }
		 }
		 log("*** "+name+"("+arrayToString(args)+")='' [METHOD NOT FOUND] *** ");
		 //log(WARN, "function(): Could not find method:"+name+", returning empty string");
		 return "";
	 }
	 

	 /** Converts object from a string to integer, boolean, or double by default
	  *    
	  * @param object The object to convert (typically a String)
	  * @param class1 The class to convert to
	  * @return The converted object
	  */
	 private Object changeType(Object object, Class class1) {
		 //System.err.println("changeType(): class="+class1.getName());
		 
		 if(class1.equals(int.class))
			 return(val(""+object));
		 if(class1.equals(boolean.class))
			 return((""+object).equalsIgnoreCase("true") || (""+object).equals("1"));
		 if(class1.equals(double.class))
			 return(valDouble(""+object));
		 return ""+object;
	}

	private String arrayToString(Object[] args) {
		 String result = "";
		 for(int i=0;args != null && i < args.length;i++)
			 result += (i>0 ? "," : "") + ("'"+args[i]+"'");
		return result;
	}

	//TODO: Get and Set values to the session object
	 public Object setVar(String key, Object value) {
		key = key.toLowerCase();
		//value = value.replaceAll("'","");
		//System.err.println("!!!! setVar() "+key+"="+value);
		vars.put(key, value);
		return value;
	 }
	 
	 public Object getVar(String key) {
		key = key.toLowerCase();
		Object result = vars.get(key);
		//System.err.println("!!!! getVar() "+key+"="+result);
		return result == null  
				? "" 
				: result;
	 }

	 /*
	 public String get(String key) {
		 if(key != null && key.length() > 0 && Character.isDigit(key.charAt(0)) ||(key.length() > 1 && key.charAt(0) == '-' &&  Character.isDigit(key.charAt(1))))
				 return key;
		 String value = null;
		 if(key.startsWith("'"))
			 value = key.length() == 2 ? "" : key.substring(1, key.length()-1);
		 else 
			 value = getVar(key);

		 //System.err.println("!!!! get() "+key+"="+value);
		return value;
	 }*/
	 

	 /** Calls the expression parser on this expression
	  */
	 public Object eval(String expression) {
		try {
			 if(debug) log("\neval("+expression+")");
		/*	 int trexxStart = expression.toUpperCase().indexOf("TREXX(");
			 if(trexxStart >= 0) {
				 int trexxEnd = expression.indexOf(')', trexxStart + 1); 
				 String trexx = expression.substring(trexxStart, trexxEnd+1);
				 trexxEnd = trexxStart + (trexx.indexOf(',') > 0 ? trexx.indexOf(',') : trexx.indexOf(')'));
				 expression = expression.substring(0,trexxStart+6) + "'"+ expression.substring(trexxStart+6,trexxEnd) + "'" + expression.substring(trexxEnd); 
				 if(debug) log("\neval("+expression+")");
			 }
		*/
			 //expression = expression.replace("`", "'");
	   		ExpressionParser parser =  new ExpressionParser(new StringReader(expression));
	   		parser.handler = this;
	   		Object result = parser.statements();
			 if(debug) log("eval("+expression+")="+result);
			
			return(result);
		} catch(Exception e){
				e.printStackTrace();
				throw new RuntimeException("eval("+expression+") failed: "+e.getMessage(), e);
		}
	 }


	 public static int logLevel = -1;
	 public static boolean debug = false;
     public static final int ERROR = 4, WARN = 2, INFO = 1, DEBUG = 0;
	 public void log(String msg) {
		 log(logLevel, msg);
	 }
	 public void log(int level, String msg) {
		 if(log2buff.length() > 0) {
			 String s = log2buff.toString();
			 log2buff.setLength(0);
			 log(s);
		 }
		 if(level == 4 || level == 2)
			System.err.println(msg);
		 else
			System.out.println(msg);
	 }
	 StringBuffer log2buff = new StringBuffer();
	 public void log2(String s) {
		 log2buff.append(s);
	 }

	 
	 
}

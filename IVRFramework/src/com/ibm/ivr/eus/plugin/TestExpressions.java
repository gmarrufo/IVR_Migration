package com.ibm.ivr.eus.plugin;

import com.ibm.ivr.eus.plugin.expressions.ExpressionHelperFunctions;;
public class TestExpressions {

	public TestExpressions(){
		
	}
	
	public void cleanUp(String xfer_number){
		ExpressionPluginImpl ep=null;
		String transfer_to=null;
		int pos=0;
		try{
			ep=new ExpressionPluginImpl();
		
			   if(xfer_number != null){

				   xfer_number=xfer_number.replace("&,*90", "");
				   xfer_number=xfer_number.replace("&,*9091", "");
				   xfer_number=xfer_number.replace("&,*90", "");
				   if(xfer_number.length() >= 2){
					   String firstTwoChars=xfer_number.substring(0,2);
					   if(firstTwoChars.equals("91") || firstTwoChars.equals("90")){
						   xfer_number=xfer_number.substring(2);
					   }
				   }
				   transfer_to=xfer_number.replace("#", "");
			   }
			   System.out.println("transfer_to=="+transfer_to);

		}catch(Exception e){
			System.out.println("Error in cleanUp:"+e.toString());
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TestExpressions te=new TestExpressions();
		te.cleanUp("1029338");
	}

}

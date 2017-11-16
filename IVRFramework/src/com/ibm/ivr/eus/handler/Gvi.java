/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ibm.ivr.eus.handler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

/**
 *
 * @author MAYMITRA
 */
public class Gvi {

    public String gvi_return(String f,String p) throws FileNotFoundException
	{
        String filename="c:/"+f;
        String token;
            /*******************************************************************************
            /*Validation and cleaning for input phone number*******************************/
            if (p.equalsIgnoreCase("ZZ0000-ZZZ") || p.equalsIgnoreCase("ZZ0001-ZZZ")) {
                return "NA";
            }
            if (p.length() < 10) {
                return "Invalid Input";
            }
            p = removeHyphen(p);
            /******************************************************************************/
            long param = Long.parseLong(p);
            long lower = 0;
            long upper = 0;
            String result = "NOT FOUND";
        
        Scanner s = null;
        try {
            s = new Scanner(new BufferedReader(new FileReader(filename)));
            

            while (s.hasNext()) {
//                System.out.println(s.next());
                token=s.next();
                lower=Long.parseLong(getnew(token,1));
                upper = Long.parseLong(getnew(token,3));
//                System.out.println("lower= "+lower+" and upper= "+upper );
                if(lower<=param && param<=upper){
                    result=token.substring(token.lastIndexOf('|')+1);
                System.out.println("Got it:  Number "+param+" is between "+lower+" and "+upper);
                System.out.println("Hence we return "+result);
                break;
                }
               
            }
        } finally {
            if (s != null) {
                s.close();
            }
        }
            return result;
		}

    public  String getnew(String s, int num){

        String newToken="";
        if(num==1){
        newToken  = s.substring(s.indexOf('|')+1);
        newToken=newToken.substring(0, newToken.indexOf('|'));
        }
        else{
            newToken=s.substring(0,s.lastIndexOf('|'));
            newToken=newToken.substring(newToken.lastIndexOf('|')+1);
            }


//        System.out.println("--------------------"+newToken);

           try{
               long i = Long.parseLong(newToken);
           }
           catch(NumberFormatException nex){
               newToken="0";
//               System.out.println("Cannot Format Number "+nex.getMessage());
           }
        return newToken;
    }

    public String removeHyphen(String p){

        StringBuffer freshP= new StringBuffer();

        for(int i=0;i<p.length();i++){
            if(p.charAt(i)!='-')
                freshP.append(p.charAt(i));
            
        }

        return freshP.toString();
    }
               
	}




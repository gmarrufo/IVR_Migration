/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ibm.ivr.eus.handler;

import java.util.HashMap;

/**
 *
 * @author MAYMITRA
 */
public class FormatHelper {




    public static   HashMap<String, String> formatSeconds(long seconds){
        HashMap<String,String> mp = new HashMap<String, String>();


        long hour = (long) Math.floor(seconds/3600);
        long new_sec = seconds - (hour*3600);
        long minute = (long) Math.floor(new_sec/60);
        long secnd = new_sec - minute*60;


        

        mp.put("hour", String.valueOf(hour));
        mp.put("min", String.valueOf(minute));
        mp.put("sec", String.valueOf(secnd));


        return mp;
    }

}

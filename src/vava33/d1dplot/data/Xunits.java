package com.vava33.d1dplot.data;

/**
 * D1Dplot
 * 
 * Enum to identify X axis units
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import com.vava33.d1dplot.D1Dplot_global;

public enum Xunits {
    tth("2"+D1Dplot_global.theta+" (º)"), dsp("d-spacing"), dspInv("1/dsp²"), Q("Q"), G("G(r)"), none(" "); //Q is 4pi(sinT)/lambda
    private final String name;
    
    private Xunits(String s){
        this.name=s;
    }

    public String getName(){
        return this.name;
    }
    
    public static Xunits getEnum(String n) {
        for (Xunits x: Xunits.values()) {
            if (n.equalsIgnoreCase(x.toString()))return x;
            if (n.equalsIgnoreCase(x.getName()))return x;
        }
        return null;
    }
}

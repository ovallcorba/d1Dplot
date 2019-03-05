package com.vava33.d1dplot.auxi;

/**
 * D1Dplot
 * 
 * HKL point data type
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import com.vava33.cellsymm.HKLrefl;

public class DataPoint_hkl {

    private HKLrefl hkl;
    private double tth_deg;

    //calculem el tth i el guardem a aquesta classe datapoint_hkl per evitar posteriors calculs (dummy arg es per evitar duplicar constructor)
    public DataPoint_hkl(HKLrefl hkl, float wave, boolean dummyarg){
        this.hkl = hkl;
        //calculem el tth per la longitud d'ona del pattern
        this.tth_deg=this.hkl.calct2(wave, true);
    }
    
    public DataPoint_hkl(HKLrefl hkl, double tth_deg){
        this.hkl = hkl;
        //calculem el tth per la longitud d'ona del pattern
        this.tth_deg=tth_deg;
    }
    
    public DataPoint_hkl(int h, int k, int l, double tth_deg, double wave){
        this.hkl = new HKLrefl(h,k,l,wave,tth_deg);
        this.tth_deg=tth_deg;
    }
    
    public double get2th() {
        return tth_deg;
    }
    
    public String toString(){
        return hkl.toString();
    }
    
    public DataPoint_hkl returnCopyWithZoff(double zoffset) {
        return new DataPoint_hkl(this.hkl,this.tth_deg+zoffset);
    }
}

package com.vava33.d1dplot.data;

/**
 * D1Dplot
 * 
 * HKL point data type
 * Extends BasicPoint from com.vava33.BasicPlotPanel
 * which implements plottable_point
 * 
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import com.vava33.BasicPlotPanel.BasicPoint;
import com.vava33.BasicPlotPanel.core.Plottable;
import com.vava33.cellsymm.HKLrefl;


//TODO. posar hkl com a label?
public class DataPoint_hkl extends BasicPoint {

    private HKLrefl hkl;

    //uses hkl dsp as xval
    public DataPoint_hkl(HKLrefl hkl, Plottable parent) {
        this(hkl.getDsp(),hkl.getYcalc(),0,hkl,parent);
    }
    
    public DataPoint_hkl(HKLrefl hkl,double xval, Plottable parent) {
        this(xval,hkl.getYcalc(),0,hkl,parent);
    }
    
    public DataPoint_hkl(double px, double py, double pysd, HKLrefl hkl, Plottable parent) {
        super(px,py,0,pysd,hkl.toString(),parent);
        this.hkl=hkl;
    }
    
    @Override
    public String toString(){
        return hkl.toString();
    }
    
    @Override
    public String getInfo() {
        return hkl.toString();
    }
}

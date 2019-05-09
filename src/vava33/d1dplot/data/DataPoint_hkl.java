package com.vava33.d1dplot.data;

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

public class DataPoint_hkl extends DataPoint implements Plottable_point{

    private HKLrefl hkl;

    //uses hkl dsp as xval
    public DataPoint_hkl(HKLrefl hkl) {
        this(hkl.getDsp(),hkl.getYcalc(),0,hkl);
    }
    
    public DataPoint_hkl(HKLrefl hkl,double xval) {
        this(xval,hkl.getYcalc(),0,hkl);
    }
    
    public DataPoint_hkl(double px, double py, double pysd, HKLrefl hkl) {
        super(px,py,pysd);
        this.hkl=hkl;
    }
    
    @Override
    public Plottable_point getCorrectedDataPoint(double incX, double incY, double factorY,boolean addYbkg) {
        if (addYbkg) {
            return new DataPoint_hkl(this.getX()+incX,(this.getY()+this.getYbkg())*factorY+incY,this.getSdy()*factorY,hkl);
        }else {
            return new DataPoint_hkl(this.getX()+incX,this.getY()*factorY+incY,this.getSdy()*factorY,hkl);    
        }
    }
    
    public String toString(){
        return hkl.toString();
    }
    
    @Override
    public String getInfo() {
        return hkl.toString();
    }
}

package com.vava33.d1dplot.auxi;

/**
 * D1Dplot
 * 
 * HKL data tipe
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

public class DataHKL {

    private int h,k,l;
    private double tth;
    
    public DataHKL(int h, int k, int l, double tth){
        this.setH(h);
        this.setK(k);
        this.setL(l);
        this.setTth(tth);
    }
    public double getTth() {
        return tth;
    }
    public void setTth(double tth) {
        this.tth = tth;
    }
    public int getH() {
        return h;
    }
    public void setH(int h) {
        this.h = h;
    }
    public int getL() {
        return l;
    }
    public void setL(int l) {
        this.l = l;
    }
    public int getK() {
        return k;
    }
    public void setK(int k) {
        this.k = k;
    }
    
    public String toString(){
        return String.format("%d %d %d", this.getH(),this.getK(),this.getL());
    }
    
}

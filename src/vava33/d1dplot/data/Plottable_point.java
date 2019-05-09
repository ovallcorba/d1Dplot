package com.vava33.d1dplot.data;

/**
 * D1Dplot
 * 
 * Interface representing a Plottable Point
 * to be implemented by all kind of data points to include in plots
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

public interface Plottable_point extends Comparable<Plottable_point>{
//    public enum pptype{def,hkl};
    public double getX();
    public double getY();
    public double getYbkg();
    public double getSdy();
    public void setX(double x);
    public void setY(double y);
    public void setSdy(double sdy);
    public Plottable_point getCorrectedDataPoint(double incX, double incY, double factorY, boolean addYbkg);
    public String getInfo(); //with useful info of the point (e.g. hkl)
}

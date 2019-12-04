package com.vava33.d1dplot.data;

/**
 * D1Dplot
 * 
 * Data point contained in data series
 * Extends BasicPoint from com.vava33.BasicPlotPanel
 * which implements plottable_point
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import com.vava33.BasicPlotPanel.BasicPoint;
import com.vava33.BasicPlotPanel.core.Plottable;
import com.vava33.BasicPlotPanel.core.Plottable_point;

/*
 * Per si de cas volem extendre-la en un futur
 */

public class DataPoint extends BasicPoint {

    public DataPoint(double x, double y, double sdy, Plottable parent) {
        super(x,y,0,sdy,parent);
    }
    
    public DataPoint(double x, double y, Plottable parent) {
        super(x, y, parent);
    }

    public DataPoint(double x, double y, double z, double sdy, Plottable parent) {
        super(x, y, z, sdy, parent);
    }

    public DataPoint(double x, double y, double z, double sdy, String label, Plottable parent) {
        super(x, y, z, sdy, label, parent);
    }

    public DataPoint(double x, double y, double ybkg, double z, double sdy, String label, Plottable parent) {
        super(x, y, ybkg, z, sdy, label, parent);
    }

    public DataPoint(Plottable_point pp) {
        super(pp);
    }

}

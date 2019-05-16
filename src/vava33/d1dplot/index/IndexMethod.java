package com.vava33.d1dplot.index;

/*
 * Abstract class to be extended by the different methods
 * 
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.util.Arrays;

import javax.swing.JProgressBar;

import org.apache.commons.math3.util.FastMath;

import com.vava33.cellsymm.Cell;
import com.vava33.d1dplot.D1Dplot_global;
import com.vava33.d1dplot.auxi.CartesianProduct;
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.jutils.FileUtils;
import com.vava33.jutils.VavaLogger;

public abstract class IndexMethod {

    private static final String className = "IndexMethod";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);

    protected double aMin,bMin,cMin,alMin,beMin,gaMin; //in radians
    protected double aMax,bMax,cMax,alMax,beMax,gaMax;
    protected double vMin, vMax;

    protected boolean cubic,tetra,hexa,orto,mono,tric;
    protected double[] Qobs;
    protected double Qmax; //valors de les reflexions entrades
    protected float deltaQerr,minFoM;
    protected int nPeaksToUse=20; //default 20 maximum
    public int nImp; //impurities/spurious


    public IndexMethod(DataSerie ds, int nPeaksToUse, double deltaQerror, float minfom, int spurious) {
        Qobs = ds.getListXvaluesAsInvdsp2();
        Arrays.sort(Qobs);
        this.nPeaksToUse = FastMath.min(nPeaksToUse, Qobs.length);
        if (Qobs.length>this.nPeaksToUse) {
            Qobs = Arrays.copyOfRange(Qobs, 0, this.nPeaksToUse-1);
        }
        this.minFoM=minfom;
        this.nImp=spurious;
        deltaQerr = (float) deltaQerror;
        Qmax = Qobs[Qobs.length-1]+deltaQerr*100;
        log.writeNameNumPairs("config", true, "deltaQerr", deltaQerr);
    }


    public void setMinPars(double a, double b, double c, double al, double be, double ga, boolean anglesInDegrees) {
        this.aMin=a;
        this.bMin=b;
        this.cMin=c;
        if (anglesInDegrees) {
            this.alMin=FastMath.toRadians(al);
            this.beMin=FastMath.toRadians(be);
            this.gaMin=FastMath.toRadians(ga);
        }else {
            this.alMin=al;
            this.beMin=be;
            this.gaMin=ga;
        }   
    }

    public void setMaxPars(double a, double b, double c, double al, double be, double ga, boolean anglesInDegrees) {
        this.aMax=a;
        this.bMax=b;
        this.cMax=c;
        if (anglesInDegrees) {
            this.alMax=FastMath.toRadians(al);
            this.beMax=FastMath.toRadians(be);
            this.gaMax=FastMath.toRadians(ga);
        }else {
            this.alMax=al;
            this.beMax=be;
            this.gaMax=ga;
        }
    }

    public void setVMinMax(double volMin, double volMax) {
        this.vMin=volMin;
        this.vMax=volMax;
    }

    public void setSystemsSearch(boolean cubic, boolean tetra, boolean hexa, boolean orto, boolean mono, boolean tric) {
        this.cubic=cubic;
        this.tetra=tetra;
        this.hexa=hexa;
        this.orto=orto;
        this.mono=mono;
        this.tric=tric;
    }
    
    protected int calcTimePercent(long startTime, int processedComb, int totalComb, int valuePercentToShow) {
        processedComb++;
        if (processedComb%valuePercentToShow==0) {
            long elapTime = (System.currentTimeMillis() - startTime)/1000;
            float percent = ((float)processedComb/(float)totalComb)*100;
            float estTime = (((100-percent)*elapTime)/percent)/60; //en minuts
            log.info(String.format(" %6.2f %% (est. time %6.2f min.)",percent,estTime));
        }
        return processedComb;
    }
    
    protected float calcEstTime(long startTime, int processedComb, int totalComb) {
        long elapTime = (System.currentTimeMillis() - startTime)/1000;
        float percent = ((float)processedComb/(float)totalComb)*100;
        float estTime = (((100-percent)*elapTime)/percent)/60; //en minuts
        return estTime; 
    }
    
    protected void prepareProgressBar(JProgressBar pbar, int maxValue) {
        pbar.setMaximum(maxValue);
        pbar.setMinimum(0);
        pbar.setIndeterminate(false);
        pbar.setValue(0);
        pbar.setStringPainted(true);
    }
    
    protected void finishProgressBar(JProgressBar pbar, int maxValue) {
        pbar.setString("indexing finished");
        if (maxValue>0)pbar.setValue(maxValue);
    }


    private boolean areColinear(double d1, double d2) {
        final float tol = 0.05f; //TODO check
        int[] nvals = FileUtils.range(1, 4, 1);
        int[] mvals = FileUtils.range(1, 4, 1);
        int[] lengths = new int[] { nvals.length, mvals.length };
        for (int[] indices : new CartesianProduct(lengths)) {
            int n = nvals[indices[0]];
            int m = mvals[indices[1]];
            if (m>=n)continue;
            double q1 = (double)n/(double)m;
            double q2 = d1/d2;
            if (FastMath.abs(q1-q2)<tol) return true;
        }
        return false;
    }
    
    double d1,d2;
    final float tolD = 0.25f;
    boolean hasNonColinear;
    
    public void prepareD1D2Colinear() {
        d1 = FastMath.sqrt(1/Qobs[0]); //dspacing 1st peak
        //find dspacing of the non-colinear next dspacing
        hasNonColinear=false;
        d2 = 0;
        for (int i=1; i<Qobs.length;i++) {
            d2 = FastMath.sqrt(1/Qobs[i]);
            if (!areColinear(d1,d2)) {
                hasNonColinear=true;
                break;
            }
        }
    }
    
    public boolean considerForIndexing(Cell c) {
        
        double[] pars = c.getCellParameters(false);
        boolean consider = false;
        switch (c.getCrystalFamily()){
        case CUBIC:
            if (pars[0]>=(d1-tolD))consider = true;
            break;
        case TETRA:
            if ((pars[0]>=(d1-tolD)) || (pars[2]>=(d1-tolD))) {
                if (hasNonColinear) {
                    if((pars[0]>=(d2-tolD))&&(pars[2]>=(d2-tolD))) {
                        if (pars[2]>pars[0]) {
                            if (pars[0]>=(d2-tolD))consider=true;
                        }    
                    }
                }
            }
            
            break;
        case HEXA:
            double _arr3 = 2./FastMath.sqrt(3);
            if ((pars[0]>=(_arr3*(d1-tolD))) || (pars[2]>=(_arr3*(d1-tolD)))) {
                if (hasNonColinear) {
                    if((pars[0]>=(d2-tolD))&&(pars[2]>=(d2-tolD))) {
                        if (pars[2]>pars[0]) {
                            if (pars[0]>=(_arr3*(d2-tolD)))consider = true;
                        }
                    }
                }
            }
            
        case ORTO:     
            if ((pars[0]>pars[1])&&(pars[1]>pars[2])) {
                if (hasNonColinear) {
                    if (pars[0]>(d1-tolD)) {
                        if (pars[1]>(d2-tolD))consider=true;
                    }
                }
            }
            
            break;
        case MONO:
            if (pars[0]>pars[2]) {
                if (pars[0]>=pars[1]) {
                    if (pars[0]>=((d1-tolD)/FastMath.sin(pars[3])))consider = true;
                }else {
                    if (pars[1]>=(d1-tolD))consider = true;                    
                }
            }
            break;
        case TRIC:
            consider=true;
            break;
        default:
            consider=true;
            break;
        }
        return consider;
    }
    
}

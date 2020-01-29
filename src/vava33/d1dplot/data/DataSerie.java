package com.vava33.d1dplot.data;

/**
 * D1Dplot
 * 
 * Data Serie to plot
 *  
 * Extends BasicSerie from com.vava33.BasicPlotPanel
 * that implements Plottable interface
 * 
 * Contains specifics such as wavelengh, xUnits, parent,...
 * 
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

import com.vava33.BasicPlotPanel.BasicPoint;
import com.vava33.BasicPlotPanel.BasicSerie;
import com.vava33.BasicPlotPanel.core.Plottable_point;
import com.vava33.BasicPlotPanel.core.SerieType;
import com.vava33.d1dplot.D1Dplot_global;
import com.vava33.d1dplot.auxi.PattOps;
import com.vava33.jutils.VavaLogger;


public class DataSerie extends BasicSerie<Plottable_point>{
 
    private static final String className = "DataSerie";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);
    
    //data
    private DataSet parent; //ATENCIO, pot no tenirne, es a dir, ser NUL
    private double wavelength;
    private Xunits xUnits;
    
    //empty dataserie...
    public DataSerie(SerieType tipusSerie, Xunits xunits, DataSet parent){
        super("",tipusSerie);
        this.parent=parent;
        this.wavelength=-1;
        if (parent!=null) this.wavelength=this.parent.getOriginalWavelength();    
        this.xUnits=xunits;
    }

    //copia tot menys les dades (empty dataserie)
    public DataSerie(DataSerie inds, List<Plottable_point> punts, Xunits xunits){
        super(inds,punts);
        this.xUnits=xunits;
        this.parent=inds.parent;
        this.wavelength=inds.wavelength;
    }
    
    //copia tot menys les dades que s'afegeixen apart
    public DataSerie(DataSerie inds, SerieType tipusSerie, boolean copyIntensities){
        super(inds,tipusSerie,copyIntensities);
        this.xUnits=inds.xUnits;
        this.parent=inds.parent;
        this.wavelength=inds.wavelength;
    }
    
    public DataSerie getSubDataSerie(double t2i, double t2f){
        DataSerie newds = new DataSerie(this.getSerieType(),this.xUnits,this.parent);
        for (int i=0;i<this.getNPoints();i++){
            if (this.getCorrectedPoint(i,false).getX()<t2i)continue;
            if (this.getCorrectedPoint(i,false).getX()>t2f)continue;
            newds.addPoint(this.getCorrectedPoint(i,false));
        }
        return newds;
    }
    
    
    protected List<Plottable_point> getCorrectedpoints(){
        List<Plottable_point> correctedpoints = new ArrayList<Plottable_point>();
        for (int i=0;i<this.getNPoints();i++) {
            correctedpoints.add(this.getCorrectedPoint(i,false));
        }
        return correctedpoints;
    }
    
    //un duplicat
    private List<Plottable_point> getUNCorrectedpoints(){
        List<Plottable_point> uncorrectedpoints = new ArrayList<Plottable_point>();
        for (int i=0;i<this.getNPoints();i++) {
            uncorrectedpoints.add(this.getRawPoint(i));
        }
        return uncorrectedpoints;
    }
 
    protected void convertpointsWavelength(double newWL){
//        List<Plottable_point> newpoints = new ArrayList<Plottable_point>();
        for (Plottable_point dp:this.getPoints()) {
            double t2 = this.getDataPointX_as(Xunits.tth,dp); //en principi obliguem a tenir-ho en 2theta
            double asn = (newWL/this.getWavelength())*FastMath.sin(Math.toRadians(t2/2.));
            if (asn>=-1 && asn<=1){
                double t2new = Math.asin(asn)* 2.f;
                dp.setX(FastMath.toDegrees(t2new));
            }
        }
    }
    
    private boolean isPossibleToConvertToXunits(Xunits destXunits){
        
        switch (destXunits) {
        case tth:
            if (this.xUnits!=Xunits.tth) {
                if (this.wavelength<0)PattOps.askForWavelengthAndAddToDS(this);
                if (this.wavelength<0) {
                    log.info("Wavelength required for units conversion");
                    return false;
                }
            }
            return true;
        case G:
            log.info("not possible to convert G units");
            return false;
        case Q: case dsp: case dspInv:
            if (this.xUnits==Xunits.tth) {
                if (this.wavelength<0)PattOps.askForWavelengthAndAddToDS(this);
                if (this.wavelength<0) {
                    log.info("Wavelength required for units conversion");
                    return false;
                }
            }
            return true;
        default:
            return false;
        }
    }
    
    public float[] getListXvaluesAsDsp() {
        float[] pksDSP = new float[this.getNPoints()];
        for(int i=0;i<this.getNPoints();i++) {
            pksDSP[i]=(float) this.getDataPointX_as(Xunits.dsp,this.getCorrectedPoint(i,false));    
        }
        return pksDSP;
    }
    
    public double[] getListXvaluesAsInvdsp2() {
        double[] pksQ = new double[this.getNPoints()];
        for(int i=0;i<this.getNPoints();i++) {
            pksQ[i]= this.getDataPointX_as(Xunits.dspInv,this.getCorrectedPoint(i,false));    
        }
        return pksQ;
    }
    
    public float getMinXvalueAsDsp() { //busco per si de cas no estan ordenats a la llista
        float mindsp = Float.MAX_VALUE;
        for(int i=0;i<this.getNPoints();i++) {
            float dsp =(float) this.getDataPointX_as(Xunits.dsp,this.getCorrectedPoint(i,false));
            if (dsp<mindsp)mindsp=dsp;
        }
        return mindsp;
    }
    
    //ATENCIO EL PUNT QUE ENTREM HAURIA D'ESTAR JA CORREGIT!!
    public double getDataPointX_as(Xunits destXunits, Plottable_point dp) {
        double q = 0;
        double tth = 0;
        double dsp = 0;
        double invdsp2 = 0;
        double g = 0;
        
        //mirem les actuals i fem els calculs pertinents
        switch (this.xUnits) {
        case G:
            g=dp.getX(); //a partir de G no podem calcular res més
            log.info("not possible to convert G units");
            return g;
        case Q:
            q=dp.getX();
            tth=2*FastMath.toDegrees(FastMath.asin((this.getWavelength()*q)/(4*FastMath.PI)));
            dsp=(2*FastMath.PI)/q;
            invdsp2=1/(dsp*dsp);
            break;
        case dsp:
            dsp=dp.getX();
            tth=2*FastMath.toDegrees(FastMath.asin(this.getWavelength()/(2*dsp)));
            invdsp2=1/(dsp*dsp);
            q=(1/dsp) * FastMath.PI*2;
            break;
        case dspInv:
            invdsp2=dp.getX();
            dsp=FastMath.sqrt(1/dp.getX());
            tth=2*FastMath.toDegrees(FastMath.asin(this.getWavelength()/(2*dsp)));
            q=(1/dsp) * FastMath.PI*2;
            break;
        case tth:
            tth=dp.getX();
            dsp=this.getWavelength()/(2*FastMath.sin(FastMath.toRadians(tth/2.)));
            invdsp2=1/(dsp*dsp);
            q=(4*FastMath.PI*FastMath.sin(FastMath.toRadians(tth/2.)))/this.getWavelength();
            break;
        default:
            log.info("not possible to convert units");
            return dp.getX();
        }
        
        switch (destXunits) {
        case Q:
            return q;
        case dsp:
            return dsp;
        case dspInv:
            return invdsp2;
        case tth:
            return tth;
        default:
            log.info("not possible to convert units");
            return dp.getX();
        }
    }
    
    protected void convertpointsXunits(Xunits destXunits){
        
        if (this.xUnits==destXunits) {
            log.info("conversion to the same units?");
            return;
        }
        
        for (Plottable_point dp:this.getPoints()) {        
            double t2 = this.getDataPointX_as(Xunits.tth,dp); //directly in degrees
            double dsp = this.getDataPointX_as(Xunits.dsp,dp);
            double q = this.getDataPointX_as(Xunits.Q,dp);
            
            switch (destXunits) {
            case tth:
                dp.setX(t2);
                break;
            case dsp:
                dp.setX(dsp);
                break;
            case dspInv:
                dp.setX(1/(dsp*dsp));
                break;
            case Q:
                dp.setX(q);
                break;
            default:
                log.info("unit conversion not supported");
                break;
            }
        }
    }
    
    public boolean convertDStoXunits(Xunits destXunits){
        // Mirar requeriments
        if(!this.isPossibleToConvertToXunits(destXunits)) {
            return false;
        }
        //backup old points
        List<Plottable_point> bakPoints = this.getUNCorrectedpoints();
        this.convertpointsXunits(destXunits);
        if (containNansOrInf(this.getPoints())) {
            log.infof("Error converting %s to X-units=%s",this.getName(),destXunits.name());
            //reverting
            this.setPoints(bakPoints);
            return false;
        }
        
        this.xUnits=destXunits;
        //escala i yoff es mantenen
        //zero cal convertir-lo
        Plottable_point zerdp = new BasicPoint(this.getXOffset(),0,null);
        this.setXOffset(this.getDataPointX_as(destXunits, zerdp));
        return true;
    }
    

    
    public void convertDStoWavelength(double newWL) {
        //nomes ho puc fer amb les dades 2theta, es l'unic dependent de wavelength
        if (this.xUnits!=Xunits.tth) {
            log.info("Change X units to 2-theta before changing wavelength");
            return;
        }
        //backup old points
        List<Plottable_point> bakPoints = this.getUNCorrectedpoints();
        convertpointsWavelength(newWL);    
        //si es buida o té nans ho diem
        if (containNansOrInf(this.getPoints())) {
            log.infof("Error converting %s to wavelength %.5fA",this.getName(),newWL);
            //reverting
            this.setPoints(bakPoints);
            return;
        }
        this.wavelength=newWL;
        //escala, yoff, zero... no ho canvio
    }
    

    public DataSet getParent() {
        return parent;
    }
    
    public void setParent(DataSet p) {
        this.parent=p;
    }

    public double getWavelength() {
        if (this.wavelength>0) {
            return this.wavelength;
        }else {
            if (parent!=null) {
                return this.parent.getOriginalWavelength();   
            }else {
                return -1;
            }
            
        }
    }
    
    public List<String> getCommentLines(){
        if (parent!=null) {
            return this.parent.getCommentLines();
        }else {
            return new ArrayList<String>(); //empty
        }
        
    }
    
    public double getOriginalWavelength() {
        if (parent!=null) {
            return this.parent.getOriginalWavelength();  
        }else {
            return -1;
        }
    }
    
    public void setWavelength(double wavelA) {
        this.wavelength=wavelA;
    }

    public Xunits getxUnits() {
        return xUnits;
    }

    public void setxUnits(Xunits xUnits) {
        this.xUnits = xUnits;
    }

}

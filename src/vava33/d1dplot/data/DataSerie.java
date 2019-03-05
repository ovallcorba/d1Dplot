package com.vava33.d1dplot.auxi;

/**
 * D1Dplot
 * 
 * Data Serie to plot
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.commons.math3.util.FastMath;
import com.vava33.d1dplot.D1Dplot_global;
import com.vava33.jutils.VavaLogger;

public class DataSerie {
    private static float def_markerSize=0;
    private static float def_lineWidth=1;
    
    private static final String className = "DataSerie";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);
    
    public enum serieType {
        dat, obs, cal, hkl, diff, bkg, bkgEstimP, gr, ref
    }
    
    public enum xunits {
        tth("2Theta"), dsp("d-spacing"), dspInv("1/dsp"), Q("Q"), G("G(r)"); //Q is 4pi(sinT)/lambda
        private String name;
        private xunits(String s){
            this.name=s;
        }
        public String getName(){
            return this.name;
        }
        public xunits getEnum(String n){
//            log.debug(n + " vs " +this.getName());
//            log.debug(Boolean.toString(n.equalsIgnoreCase(this.getName())));
//            log.debug(Boolean.toString(n.equalsIgnoreCase(this.toString())));

            if (n.equalsIgnoreCase(this.getName()))return this;
            if (n.equalsIgnoreCase(this.toString()))return this;
            return null;
        }
    }
    
    private ArrayList<DataPoint> seriePoints;
    private ArrayList<DataPoint> peaks;
    private ArrayList<DataPoint_hkl> serieHKL;
    
    private double wavelength = -1;
    private float markerSize;
    private float lineWidth;
    private boolean showErrBars = false;
    private boolean plotThis;
    private float scale = 1.0f;
    private double zerrOff = 0.0f;
    private double YOff = 0.0f;
    private double t2i,t2f,step;
    private Color color;
    private Pattern1D Patt1D;
    private xunits xUnits;
    private serieType tipusSerie;
    private String serieName;
    
    
    //empty dataserie
    public DataSerie(){
        this.setSeriePoints(new ArrayList<DataPoint>());
        this.setSerieHKL(new ArrayList<DataPoint_hkl>());
        this.setSeriePeaks(new ArrayList<DataPoint>());
        this.setPlotThis(true);
        this.setPatt1D(null);
        this.setT2i(-999);
        this.setT2f(-999);
        this.setStep(-999);
        this.setWavelength(-1);
        this.setScale(1);
        this.setZerrOff(0.0f);
        this.setYOff(0.0f);
        this.setxUnits(xunits.tth);
        this.setTipusSerie(serieType.dat);
        this.setColor(Color.BLACK);
        this.setMarkerSize(def_markerSize);
        this.setLineWidth(def_lineWidth);
        this.setSerieName("");
    }
    
    public DataSerie(serieType stype){
        this();
        this.setTipusSerie(stype);
    }

    //copia tots els parametres excepte les dades
    public DataSerie(DataSerie inds, serieType stype,boolean addToPatt){
        this();
        this.setT2i(inds.getT2i());
        this.setT2f(inds.getT2f());
        this.setStep(inds.getStep());
        this.setWavelength(inds.getWavelength());
        this.setScale(inds.getScale());
        this.setZerrOff(inds.getZerrOff());
        this.setYOff(inds.getYOff());
        this.setxUnits(inds.getxUnits());
        this.setColor(inds.getColor());
        this.setTipusSerie(stype);
        this.setSerieName(inds.getSerieName());
        if (addToPatt)this.setPatt1D(inds.getPatt1D());
    }
    
    public DataSerie(ArrayList<DataPoint> punts, Pattern1D patt, double t2i, double t2f, double step, double wavel){
        this();
        this.setSeriePoints(punts);
        this.setPatt1D(patt);
        this.setT2i(t2i);
        this.setT2f(t2f);
        this.setStep(step);
        this.setWavelength(wavel);
    }
    
    public DataSerie(ArrayList<DataPoint_hkl> punts, Pattern1D patt, double wavel){
        this();
        this.setSerieHKL(punts);
        this.setPatt1D(patt);
        this.setWavelength(wavel);
    }

    public void copySeriePoints(DataSerie inDS) {
    	this.setSeriePoints(inDS.seriePoints);
    }
    public void copySeriePeaks(DataSerie inDS) {
    	this.setSeriePeaks(inDS.peaks);
    }
    public void copySerieHKL(DataSerie inDS) {
    	this.setSerieHKL(inDS.serieHKL);
    }
    
    public DataPoint getPoint(int arrayPosition){
        DataPoint dp = this.seriePoints.get(arrayPosition);
        DataPoint ndp = new DataPoint(dp.getX()+this.zerrOff,dp.getY()*this.scale+this.getYOff(),dp.getSdy()*this.scale,dp.getyBkg()*this.scale);
        if (Pattern1D.isPlotwithbkg()){
            ndp.setY(ndp.getY()+(ndp.getyBkg()*this.scale));
        }
        return ndp; 
    }
    
    public int getIndexOfDP(DataPoint dp){
        return this.seriePoints.indexOf(dp);
    }
    
    public DataSerie getSubDataSerie(double t2i, double t2f){
        DataSerie newds = new DataSerie(this,this.getTipusSerie(),false);
        for (int i=0;i<this.getNpoints();i++){
            if (this.getPoint(i).getX()<t2i)continue;
            if (this.getPoint(i).getX()>t2f)continue;
            newds.addPoint(this.getPoint(i));
        }
        return newds;
    }
    
    public DataPoint getPeak(int arrayPosition){
        DataPoint dp = this.peaks.get(arrayPosition);
        DataPoint ndp = new DataPoint(dp.getX()+this.zerrOff,dp.getY()*this.scale+this.getYOff(),dp.getSdy()*this.scale,dp.getyBkg()*this.scale);
        if (Pattern1D.isPlotwithbkg()){
            ndp.setY(ndp.getY()+(ndp.getyBkg()*this.scale));
        }
        return ndp; 
    }
    
    public float[] getListT2Peaks(boolean inRadians) {
    	Iterator<DataPoint> itr = this.peaks.iterator();
    	float[] pksT2 = new float[this.peaks.size()];
    	int count = 0;
    	while (itr.hasNext()){
    		DataPoint pk = itr.next();
    		if (inRadians) {
    			pksT2[count]=(float) FastMath.toRadians(pk.getX()+this.zerrOff);
    		}else {//direct in degrees
    			pksT2[count] = (float) (pk.getX()+this.zerrOff);
    		}
    		count++;
    	}
    	return pksT2;
    }
    
    //dobs = wave/(2*np.sin(np.radians(t2obs)/2.))
    public float[] getListPeaksDsp() {
    	Iterator<DataPoint> itr = this.peaks.iterator();
    	float[] pksDsp = new float[this.peaks.size()];
    	int count = 0;
    	while (itr.hasNext()){
    		DataPoint pk = itr.next();
  			float t2rad =(float) FastMath.toRadians(pk.getX()+this.zerrOff);
  			pksDsp[count] = (float) (this.getWavelength()/(2*FastMath.sin(t2rad/2.f)));
  			count++;
    	}
    	return pksDsp;
    }
    
    public double[] getListPeaksQ() {
    	Iterator<DataPoint> itr = this.peaks.iterator();
    	double[] pksQ = new double[this.peaks.size()];
    	int count = 0;
    	while (itr.hasNext()){
    		DataPoint pk = itr.next();
  			double t2rad = FastMath.toRadians(pk.getX()+this.zerrOff);
  			double dsp = this.getWavelength()/(2*FastMath.sin(t2rad/2.f));
  			pksQ[count] = 1/(dsp*dsp);
  			count++;
    	}
    	return pksQ;
    }
    
    //dobs = wave/(2*np.sin(np.radians(t2obs)/2.))
    public float getMinPeakDsp() {
    	Iterator<DataPoint> itr = this.peaks.iterator();
    	float[] pksDsp = new float[this.peaks.size()];
    	int count = 0;
    	float mindsp = Float.MAX_VALUE;
    	while (itr.hasNext()){
    		DataPoint pk = itr.next();
  			float t2rad =(float) FastMath.toRadians(pk.getX()+this.zerrOff);
  			pksDsp[count] = (float) (this.getWavelength()/(2*FastMath.sin(t2rad/2.f)));
  			if (pksDsp[count]<mindsp)mindsp = pksDsp[count];
  			count++;
    	}
    	return mindsp;
    }
    
    public float getMaxPeakDsp() {
    	Iterator<DataPoint> itr = this.peaks.iterator();
    	float[] pksDsp = new float[this.peaks.size()];
    	int count = 0;
    	float maxdsp = Float.MIN_VALUE;
    	while (itr.hasNext()){
    		DataPoint pk = itr.next();
  			float t2rad =(float) FastMath.toRadians(pk.getX()+this.zerrOff);
  			pksDsp[count] = (float) (this.getWavelength()/(2*FastMath.sin(t2rad/2.f)));
  			if (pksDsp[count]>maxdsp)maxdsp = pksDsp[count];
  			count++;
    	}
    	return maxdsp;
    }
    
    //dobs = wave/(2*np.sin(np.radians(t2obs)/2.))
    public float[] getMinMaxPeakDsp() {
    	Iterator<DataPoint> itr = this.peaks.iterator();
    	float[] pksDsp = new float[this.peaks.size()];
    	int count = 0;
    	float mindsp = Float.MAX_VALUE;
    	float maxdsp = Float.MIN_VALUE;
    	while (itr.hasNext()){
    		DataPoint pk = itr.next();
  			float t2rad =(float) FastMath.toRadians(pk.getX()+this.zerrOff);
  			pksDsp[count] = (float) (this.getWavelength()/(2*FastMath.sin(t2rad/2.f)));
  			if (pksDsp[count]<mindsp)mindsp = pksDsp[count];
  			if (pksDsp[count]>maxdsp)maxdsp = pksDsp[count];
  			count++;
    	}
    	return new float[] {mindsp,maxdsp};
    }
    
    
    //TODO l'he borrat perquè m'interessa que això ho faci al plot, no aqui
    public DataPoint_hkl getHKLPoint(int arrayPosition){
//        DataPoint_hkl dhkl = this.serieHKL.get(arrayPosition);
//        return new DataPoint_hkl(dhkl.getH(),dhkl.getK(),dhkl.getL(),dhkl.getTth()+this.zerrOff);
        return  this.serieHKL.get(arrayPosition).returnCopyWithZoff(this.zerrOff);
        
    }
    
    public void addPoint(DataPoint dp){
        this.seriePoints.add(dp);
    }

    public void addHKLPoint(DataPoint_hkl dhkl){
        this.serieHKL.add(dhkl);
    }
    
    public void removePoint(DataPoint dp){
        logdebug("index of the point to remove="+this.seriePoints.indexOf(dp));
        boolean removed = this.seriePoints.remove(dp);
        logdebug(Boolean.toString(removed));
    }
    public void removePoint(int index){
        this.seriePoints.remove(index);
    }
    
    public void removePeak(DataPoint dp){
        logdebug("index of the peak to remove="+this.peaks.indexOf(dp));
        boolean removed = this.peaks.remove(dp);
        logdebug(Boolean.toString(removed));
    }

    public void removeHKLPoint(DataPoint_hkl dhkl){
        this.serieHKL.remove(dhkl);
    }
    
    //pels dos casos
    public int getNpoints(){
        if (getTipusSerie()==serieType.hkl){
            return serieHKL.size();
        }else{
            return seriePoints.size();            
        }
    }

    public int getNpeaks(){
        return peaks.size();
    }

    public void setSeriePoints(ArrayList<DataPoint> seriePoints) {
        this.seriePoints = seriePoints;
    }

    private void setSerieHKL(ArrayList<DataPoint_hkl> seriehkl) {
        this.serieHKL = seriehkl;
    }
    
    private void setSeriePeaks(ArrayList<DataPoint> seriePeaks) {
        this.peaks = seriePeaks;
    }
    
    public float getMarkerSize() {
        return markerSize;
    }

    public void setMarkerSize(float markerSize) {
        this.markerSize = markerSize;
    }
    
    public double[] getPuntsMaxXMinXMaxYMinY(){
        if (seriePoints!=null){
            double minX = Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE;
            double maxY = Double.MIN_VALUE;
            for (int i=0;i<seriePoints.size();i++){
                DataPoint punt = this.getPoint(i);
                if (punt.getX() < minX){
                    minX = punt.getX();
                }
                if (punt.getX() > maxX){
                    maxX = punt.getX();
                }
                if (punt.getY() < minY){
                    minY = punt.getY();
                }
                if (punt.getY() > maxY){
                    maxY = punt.getY();
                }
            }
            if (FastMath.abs(minY-maxY)<1)maxY=minY+100;
            if (FastMath.abs(minX-maxX)<1)maxX=minX+1;
            return new double[]{maxX,minX,maxY,minY};
        }else{
            return null;
        }
    }
    
    public boolean isPlotThis() {
        return plotThis;
    }

    public void setPlotThis(boolean plotThis) {
        this.plotThis = plotThis;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    public boolean isShowErrBars() {
        return showErrBars;
    }

    public void setShowErrBars(boolean showErrBars) {
        this.showErrBars = showErrBars;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public double getZerrOff() {
        return zerrOff;
    }

    public void setZerrOff(double zerrOff) {
        this.zerrOff = zerrOff;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Pattern1D getPatt1D() {
        return Patt1D;
    }

    public void setPatt1D(Pattern1D patt1d) {
        Patt1D = patt1d;
        if (patt1d!=null){
            if (Patt1D.getOriginal_wavelength()<0){
                Patt1D.setOriginal_wavelength(wavelength);
            }
            if (patt1d.indexOfSerie(this)<0){ //ONLY IF IT IS NOT IN THE LIST!!!
                patt1d.addDataSerie(this);
            }
        }
    }
    
    public String toString(){
        return this.getPatt1D().getFile().getName();
    }

    public double getStep() {
        return step;
    }

    public void setStep(double step) {
        this.step = step;
    }

    public double getT2f() {
        return t2f+this.zerrOff;
    }

    public void setT2f(double t2f) {
        this.t2f = t2f;
    }

    public double getT2i() {
        return t2i+this.zerrOff;
    }

    public void setT2i(double t2i) {
        this.t2i = t2i;
    }
    
    public double calcStep(){
        return (this.t2f - this.t2i)/this.getNpoints();
    }

    public double getWavelength() {
        return wavelength;
    }

    public void setWavelength(double wavelength) {
        this.wavelength = wavelength;
    }
    
    //NO APLICA ZERO NI ESCALA (els posa als valors per defecte) treballa amb l'arrayList directament (es deia getNewSerieWL)
    public DataSerie convertToNewWL(double newWL){
        DataSerie newDS = new DataSerie(this,this.getTipusSerie(),false);
        Iterator<DataPoint> itdp = this.seriePoints.iterator();
        while (itdp.hasNext()){
            DataPoint dp = itdp.next();
            double t2 = dp.getX();
            double asn = (newWL/this.getWavelength())*Math.sin(Math.toRadians(t2/2.));
            if (asn>=-1 && asn<=1){
                double t2new = Math.asin(asn)* 2.f;
                newDS.addPoint(new DataPoint(Math.toDegrees(t2new),dp.getY(),dp.getSdy()));
            }
        }
        newDS.setWavelength(newWL);
        return newDS;
    }

    public xunits getxUnits() {
        return xUnits;
    }

    public void setxUnits(xunits xUnits) {
        this.xUnits = xUnits;
    }
    
    //es podria dir tranquilament getNewSerieXunits, pero com que genera una nova dataserie...
    //HAURA DE CONVERTIR EL ZERO I MANTENIR L'ESCALA i WAVLENGTH
    //TODO: T2F T2I step!?
    //      DE MOMENT DESACTIVO EL ZERO PERQUE NO ESTA BEN CALCULAT (no es conversió sinó que es desplaçament absout!)
    public DataSerie convertToXunits(xunits destXunits){
        DataSerie newDS = new DataSerie(this,this.getTipusSerie(),false);
        Iterator<DataPoint> itdp = this.seriePoints.iterator();
        Iterator<DataPoint> itdpk = this.peaks.iterator();
        //TODO: afegir serieHKL?
        
        logdebug(String.format("convert from %s to %s",this.getxUnits(), destXunits));
        
        switch (this.getxUnits()){
            case tth:
                switch (destXunits){
                    case tth:
                        logdebug("tth to tth");
                        //do nothing
                        break;
                    case dsp:
                        //TTH to DSP
                        logdebug("tth to dsp");
                        while (itdp.hasNext()){
                            DataPoint dp = itdp.next();
                            double t2 = dp.getX();
                            double dsp = this.getWavelength()/(2*FastMath.sin(FastMath.toRadians(t2/2.)));
                            newDS.addPoint(new DataPoint(dsp,dp.getY(),dp.getSdy()));
                        }
                        while (itdpk.hasNext()){
                            DataPoint dp = itdpk.next();
                            double t2 = dp.getX();
                            double dsp = this.getWavelength()/(2*FastMath.sin(FastMath.toRadians(t2/2.)));
                            newDS.addPeak(new DataPoint(dsp,dp.getY(),dp.getSdy()));
                        }
                        //Zero and scale
                        newDS.setScale(this.getScale());
//                        float zer = (float) (this.getWavelength()/(2*FastMath.sin(FastMath.toRadians(this.getZerrOff()/2.))));
//                        newDS.setZerrOff(zer);
                        break;
                    case dspInv:
                        //TTH to DSP inv
                        logdebug("tth to dspInv");
                        while (itdp.hasNext()){
                            DataPoint dp = itdp.next();
                            double t2 = dp.getX();
//                            double dsp = this.getWavelength()/(2*FastMath.sin(FastMath.toRadians(t2/2.)));
//                            newDS.addPoint(new DataPoint(1/dsp,dp.getY(),dp.getSdy()));
                            double dsp2 = this.getWavelength()/(2*FastMath.sin(FastMath.toRadians(t2/2.)))*this.getWavelength()/(2*FastMath.sin(FastMath.toRadians(t2/2.)));
                            newDS.addPoint(new DataPoint(1/dsp2,dp.getY(),dp.getSdy()));
                        }
                        while (itdpk.hasNext()){
                            DataPoint dp = itdpk.next();
                            double t2 = dp.getX();
//                            double dsp = this.getWavelength()/(2*FastMath.sin(FastMath.toRadians(t2/2.)));
//                            newDS.addPeak(new DataPoint(1/dsp,dp.getY(),dp.getSdy()));
                            double dsp2 = this.getWavelength()/(2*FastMath.sin(FastMath.toRadians(t2/2.)))*this.getWavelength()/(2*FastMath.sin(FastMath.toRadians(t2/2.)));
                            newDS.addPoint(new DataPoint(1/dsp2,dp.getY(),dp.getSdy()));
                        }
                        //Zero and scale
                        newDS.setScale(this.getScale());
//                        zer = (float) (this.getWavelength()/(2*FastMath.sin(FastMath.toRadians(this.getZerrOff()/2.))));
//                        newDS.setZerrOff(1/zer);
                        break;
                    case Q:
                        //TTH to Q
                        logdebug("tth to Q");
                        while (itdp.hasNext()){
                            DataPoint dp = itdp.next();
                            double t2 = dp.getX();
                            double q = (4*FastMath.PI*FastMath.sin(FastMath.toRadians(t2/2.)))/this.getWavelength();
                            newDS.addPoint(new DataPoint(q,dp.getY(),dp.getSdy()));
                        }
                        while (itdpk.hasNext()){
                            DataPoint dp = itdpk.next();
                            double t2 = dp.getX();
                            double q = (4*FastMath.PI*FastMath.sin(FastMath.toRadians(t2/2.)))/this.getWavelength();
                            newDS.addPeak(new DataPoint(q,dp.getY(),dp.getSdy()));
                        }
                        //Zero and scale
                        newDS.setScale(this.getScale());
//                        zer = (float) (4*FastMath.PI*FastMath.sin(FastMath.toRadians(this.getZerrOff()/2.))/this.getWavelength());
//                        newDS.setZerrOff(zer);
                        break;
                    default:
                        logdebug("unit conversion not supported");
                        break;
                }
                break;
            case dsp:
                switch (destXunits){
                    case tth:
                        //DSP to TTH
                        logdebug("dsp to tth");
                        while (itdp.hasNext()){
                            DataPoint dp = itdp.next();
                            double dsp = dp.getX();
                            double t2 = 2*FastMath.asin(this.getWavelength()/(2*dsp));
                            newDS.addPoint(new DataPoint(FastMath.toDegrees(t2),dp.getY(),dp.getSdy()));
                        }
                        while (itdpk.hasNext()){
                            DataPoint dp = itdpk.next();
                            double dsp = dp.getX();
                            double t2 = 2*FastMath.asin(this.getWavelength()/(2*dsp));
                            newDS.addPeak(new DataPoint(FastMath.toDegrees(t2),dp.getY(),dp.getSdy()));
                        }
                        //Zero and scale
                        newDS.setScale(this.getScale());
//                        float zer = (float) (2*FastMath.asin(this.getWavelength()/2*this.getZerrOff()));
//                        newDS.setZerrOff((float) FastMath.toDegrees(zer));
                        break;
                    case dsp:
                        //DSP to DSP
                        logdebug("dsp to dsp");
                        break;
                    case dspInv:
                        //DSP to DSP inv
                        logdebug("dsp to dspInv");
                        while (itdp.hasNext()){
                            DataPoint dp = itdp.next();
                            double dsp = dp.getX();
                            newDS.addPoint(new DataPoint(1/dsp,dp.getY(),dp.getSdy()));
                        }
                        while (itdpk.hasNext()){
                            DataPoint dp = itdpk.next();
                            double dsp = dp.getX();
                            newDS.addPeak(new DataPoint(1/dsp,dp.getY(),dp.getSdy()));
                        }
                        //Zero and scale
                        newDS.setScale(this.getScale());
//                        newDS.setZerrOff(1/this.getZerrOff());
                        break;
                    case Q:
                        //DSP to Q invertir i multiplicar per 2pi crec...
                        logdebug("dsp to Q");
                        while (itdp.hasNext()){
                            DataPoint dp = itdp.next();
                            double dsp = dp.getX();
                            double q = (1/dsp) * FastMath.PI*2;
                            newDS.addPoint(new DataPoint(q,dp.getY(),dp.getSdy()));
                        }
                        while (itdpk.hasNext()){
                            DataPoint dp = itdpk.next();
                            double dsp = dp.getX();
                            double q = (1/dsp) * FastMath.PI*2;
                            newDS.addPeak(new DataPoint(q,dp.getY(),dp.getSdy()));
                        }
                        //Zero and scale
                        newDS.setScale(this.getScale());
//                        zer = (float) ((1/this.getZerrOff()) * FastMath.PI*2);;
//                        newDS.setZerrOff(zer);
                        break;
                    default:
                        logdebug("unit conversion not supported");
                        break;
                }
                break;
            case dspInv:
                switch (destXunits){
                    case tth:
                        //DSPinv to TTH
                        logdebug("dspInv to tth");
                        while (itdp.hasNext()){
                            DataPoint dp = itdp.next();
                            double dsp = 1/dp.getX();
                            double t2 = 2*FastMath.asin(this.getWavelength()/(2*dsp));
                            newDS.addPoint(new DataPoint(FastMath.toDegrees(t2),dp.getY(),dp.getSdy()));
                        }
                        while (itdpk.hasNext()){
                            DataPoint dp = itdpk.next();
                            double dsp = 1/dp.getX();
                            double t2 = 2*FastMath.asin(this.getWavelength()/(2*dsp));
                            newDS.addPeak(new DataPoint(FastMath.toDegrees(t2),dp.getY(),dp.getSdy()));
                        }
                        //Zero and scale
                        newDS.setScale(this.getScale());
//                        float zer = (float) (2*FastMath.asin(this.getWavelength()/2*(1/this.getZerrOff())));
//                        newDS.setZerrOff((float) FastMath.toDegrees(zer));
                        break;
                    case dsp:
                        //DSPinv to DSP
                        logdebug("dspInv to dsp");
                        while (itdp.hasNext()){
                            DataPoint dp = itdp.next();
                            double dsp = dp.getX();
                            newDS.addPoint(new DataPoint(1/dsp,dp.getY(),dp.getSdy()));
                        }
                        while (itdpk.hasNext()){
                            DataPoint dp = itdpk.next();
                            double dsp = dp.getX();
                            newDS.addPeak(new DataPoint(1/dsp,dp.getY(),dp.getSdy()));
                        }
                        //Zero and scale
                        newDS.setScale(this.getScale());
//                        newDS.setZerrOff(1/this.getZerrOff());
                        break;
                    case dspInv:
                        //DSPinv to DSP inv
                        logdebug("dspInv to dspInv");
                        break;
                    case Q:
                        //DSPinv to Q invertir i multiplicar per 2pi crec...
                        logdebug("dspInv to Q");
                        while (itdp.hasNext()){
                            DataPoint dp = itdp.next();
                            double dsp = 1/dp.getX();
                            double q = (1/dsp) * FastMath.PI*2;
                            newDS.addPoint(new DataPoint(q,dp.getY(),dp.getSdy()));
                        }
                        while (itdpk.hasNext()){
                            DataPoint dp = itdpk.next();
                            double dsp = 1/dp.getX();
                            double q = (1/dsp) * FastMath.PI*2;
                            newDS.addPeak(new DataPoint(q,dp.getY(),dp.getSdy()));
                        }
                        //Zero and scale
                        newDS.setScale(this.getScale());
//                        zer = (float) ((1/(1/this.getZerrOff())) * FastMath.PI*2);;
//                        newDS.setZerrOff(zer);
                        break;
                    default:
                        logdebug("unit conversion not supported");
                        break;
                }
                break;
            case Q:
                switch (destXunits){
                    case tth:
                        logdebug("Q to tth");
                        while (itdp.hasNext()){
                            DataPoint dp = itdp.next();
                            double q = dp.getX();
                            double tth = FastMath.toDegrees(2*FastMath.asin((this.getWavelength()*q)/(4*FastMath.PI)));
                            newDS.addPoint(new DataPoint(tth,dp.getY(),dp.getSdy()));
                        }
                        while (itdpk.hasNext()){
                            DataPoint dp = itdpk.next();
                            double q = dp.getX();
                            double tth = FastMath.toDegrees(2*FastMath.asin((this.getWavelength()*q)/(4*FastMath.PI)));
                            newDS.addPeak(new DataPoint(tth,dp.getY(),dp.getSdy()));
                        }
                        //Zero and scale
                        newDS.setScale(this.getScale());
//                        float zer = (float) FastMath.toDegrees(2*FastMath.asin(this.getWavelength()*this.getZerrOff()/4*FastMath.PI));
//                        newDS.setZerrOff(zer);
                        break;
                    case dsp:
                        //Q to DSP (2pi/Q)
                        logdebug("Q to dsp");
                        while (itdp.hasNext()){
                            DataPoint dp = itdp.next();
                            double q = dp.getX();
                            double dsp = (2*FastMath.PI)/q;
                            newDS.addPoint(new DataPoint(dsp,dp.getY(),dp.getSdy()));
                        }
                        while (itdpk.hasNext()){
                            DataPoint dp = itdpk.next();
                            double q = dp.getX();
                            double dsp = (2*FastMath.PI)/q;
                            newDS.addPeak(new DataPoint(dsp,dp.getY(),dp.getSdy()));
                        }
                        //Zero and scale
                        newDS.setScale(this.getScale());
//                        zer = (float) (2*FastMath.PI/this.getZerrOff());
//                        newDS.setZerrOff(zer);
                        break;
                    case dspInv:
                        //Q to DSP inv
                        logdebug("Q to dspInv");
                        while (itdp.hasNext()){
                            DataPoint dp = itdp.next();
                            double q = dp.getX();
                            double dsp = (2*FastMath.PI)/q;
                            newDS.addPoint(new DataPoint(1/dsp,dp.getY(),dp.getSdy()));
                        }
                        while (itdpk.hasNext()){
                            DataPoint dp = itdpk.next();
                            double q = dp.getX();
                            double dsp = (2*FastMath.PI)/q;
                            newDS.addPeak(new DataPoint(1/dsp,dp.getY(),dp.getSdy()));
                        }
                        //Zero and scale
                        newDS.setScale(this.getScale());
//                        zer = (float) (2*FastMath.PI/this.getZerrOff());
//                        newDS.setZerrOff(1/zer);
                        break;
                    case Q:
                        //Q to Q
                        logdebug("Q to Q");
                        break;
                    default:
                        logdebug("unit conversion not supported");
                        break;
                }
                break;
            default:
                logdebug("unit conversion not supported");
                break;
        }

        if (newDS.getNpoints()<=0){
            return null;
        }
        newDS.setxUnits(destXunits);
        newDS.setWavelength(this.getWavelength());
        
        //DEBUG print 10 first points of DS and newDS
//        double[] ds10 = new double[10];
//        double[] nds10 = new double[10];
//        for (int i=0; i<10;i++){
//            ds10[i]=this.getPoint(i).getX();
//            nds10[i]=newDS.getPoint(i).getX();
//        }
//        if(D1Dplot_global.isDebug())log.writeNameNums("CONFIG", true, "ds10", ds10);
//        if(D1Dplot_global.isDebug())log.writeNameNums("CONFIG", true, "nds10", nds10);
        return newDS;

    }

    public serieType getTipusSerie() {
        return tipusSerie;
    }

    public void setTipusSerie(serieType tipusSerie) {
        this.tipusSerie = tipusSerie;
        switch(tipusSerie){
            case bkg:
                this.setColor(Color.PINK);
                this.setLineWidth(getDef_lineWidth()+1);
                break;
            case bkgEstimP:
                this.setColor(Color.PINK);
                this.setMarkerSize(getDef_markerSize()+2.0f);
                this.setLineWidth(0.0f);
                break;
            //TODO AFEGIR COSES PEL REF?
            default:
                break;
        }
    }

    public void clearDataPoints(){
        serieHKL.clear();
        seriePoints.clear();
        peaks.clear();
    }
    
    public void clearPeaks(){
        peaks.clear();
    }
    
    public double getYOff() {
        return YOff;
    }

    public void setYOff(double yOff) {
        YOff = yOff;
    }
    
    public ArrayList<DataPoint_hkl> getClosestReflections(double centralTTh, double tol){
        if (tol<0) tol = 0.025;
        ArrayList<DataPoint_hkl> found = new ArrayList<DataPoint_hkl>();
        if (this.getTipusSerie()!=serieType.hkl)return found;
        for (int i=0;i<this.getNpoints();i++){
            if (FastMath.abs(centralTTh-this.getHKLPoint(i).get2th())<tol){
                found.add(this.getHKLPoint(i));
            }
        }
        return found;
    }
    
    public double[] calcYmeanYDesvYmaxYmin(){
        int puntIni = 0;
        int puntFin = this.getNpoints()-1;
        return calcYmeanYDesvYmaxYmin(puntIni,puntFin);
    }
    
    public DataPoint getMinYDataPoint(int puntIni, int puntFin){
        double ymin = Double.MAX_VALUE;
        DataPoint dpMin = null;
        for (int i=puntIni;i<=puntFin;i++){
            DataPoint dp = this.getPoint(i);
            if (dp.getY()<ymin){
                ymin=dp.getY();
                dpMin = dp;
            }
        }
        return dpMin;
    }
    
    //punt Fin està inclos
    public double[] calcYmeanYDesvYmaxYmin(int puntIni, int puntFin){
        double[] ymean_ydesv_ymax_ymin = new double[4];
        ymean_ydesv_ymax_ymin[2] = Double.MIN_VALUE;
        ymean_ydesv_ymax_ymin[3] = Double.MAX_VALUE;
        int npoints = FastMath.abs(puntFin - puntIni + 1);
        double sumY = 0;
        for (int i=puntIni;i<=puntFin;i++){
            DataPoint dp = this.getPoint(i);
            sumY = sumY + dp.getY();
            if (dp.getY()<ymean_ydesv_ymax_ymin[3])ymean_ydesv_ymax_ymin[3]=dp.getY();
            if (dp.getY()>ymean_ydesv_ymax_ymin[2])ymean_ydesv_ymax_ymin[2]=dp.getY();
        }
        ymean_ydesv_ymax_ymin[0] = sumY/npoints;
        //ara desviacio
        sumY=0;
        for (int i=puntIni;i<=puntFin;i++){
            DataPoint dp = this.getPoint(i);
            sumY = sumY + (dp.getY()-ymean_ydesv_ymax_ymin[0])*(dp.getY()-ymean_ydesv_ymax_ymin[0]);
        }
        ymean_ydesv_ymax_ymin[1] = FastMath.sqrt(sumY/(npoints-1));
        return ymean_ydesv_ymax_ymin;
    }
    
    //delsig defineix el llindar (factor delsig*sigmaPattern)
    //SIMPLE AGAFA EL PUNT MES ALT
    public void findPeaks(float delsig){      
        peaks.clear();
        double desv = this.calcYmeanYDesvYmaxYmin()[1];
        //limits menys 1
        for (int i=1;i<this.getNpoints()-1;i++){
            double ycent = this.getPoint(i).getY();
            if (ycent < desv*delsig)continue;
            double yleft = this.getPoint(i-1).getY();
            double yright = this.getPoint(i+1).getY();

            if (ycent>yleft && ycent>yright){
                peaks.add(this.getPoint(i));
            }
        }
    }
    
    //delsig defineix el llindar (factor delsig*sigmaPattern), faig ponderacio...
    //FA PONDERACIO, S'ACOSTA MES AL MILLOR
    public void findPeaksBetter(float delsig){      
        peaks.clear();
        double desv = this.calcYmeanYDesvYmaxYmin()[1];
        //limits menys 1
        for (int i=1;i<this.getNpoints()-1;i++){
            double ycent = this.getPoint(i).getY();
            if (ycent < desv*delsig)continue;
            double yleft = this.getPoint(i-1).getY();
            double yright = this.getPoint(i+1).getY();
            double xcent = this.getPoint(i).getX();
            double xleft = this.getPoint(i-1).getX();
            double xright = this.getPoint(i+1).getX();
            if (ycent>yleft && ycent>yright){
                
                //ara pondero entre els tres
                double xpeak = (xcent*ycent + xleft*yleft + xright*yright)/(ycent+yleft+yright);
                peaks.add(new DataPoint(xpeak,ycent,this.getPoint(i).getSdy()));
            }
        }
    }
    
    //retorna el fons-llindar si s'ha fet servir el fons
    public DataSerie findPeaksEvenBetter(float delsig,boolean usebkg,double minX, double maxX){
        //interpolar mirar quin està mes avall dels dos del costat i interpolar el valor de y elevat a la recta centre-inferior. alsehores buscar el mig de x
        peaks.clear();
        double desv = 1;
        DataSerie bkg = null;
        
        if((minX>maxX)||(minX<0&&maxX<0)){
            minX=this.getT2i();
            maxX=this.getT2f();
        }
        
        if (usebkg){
            bkg = PattOps.bkg_Bruchner(this, 20, 50, true, this.getT2i()-1, false, null);
        }else{
            desv = this.calcYmeanYDesvYmaxYmin()[1];            
        }
        if (bkg==null)usebkg=false;
        
        
        //limits menys 1
        for (int i=1;i<this.getNpoints()-1;i++){
            if (this.getPoint(i).getX()<minX)continue; //rang on buscar pics
            if (this.getPoint(i).getX()>maxX)continue;
            double ycent = this.getPoint(i).getY();
            if (usebkg){
                if (ycent < bkg.getPoint(i).getY()*delsig)continue;
            }else{
                if (ycent < desv*delsig)continue;    
            }
            
            double yleft = this.getPoint(i-1).getY();
            double yright = this.getPoint(i+1).getY();
            double xcent = this.getPoint(i).getX();
            double xleft = this.getPoint(i-1).getX();
            double xright = this.getPoint(i+1).getX();
            if (ycent>yleft && ycent>yright){
                
                double xpeak = 0;
                //ara interpolo
                if (yleft>yright){
                    //fem recta centre-right i interpolem el valor de yleft a aquesta
                    double pen = (yright-ycent)/(xright-xcent);
                    double ord = pen*(xcent)*-1+ycent;
                    //interpolem yleft
                    double xinter = (yleft-ord)/pen;
                    //ara busquem el centre
                    xpeak = (xinter+xleft)/2.;
                }else{
                    //al reves, recte centre-left i interpolem yright
                    double pen = (yleft-ycent)/(xleft-xcent);
                    double ord = pen*(xcent)*-1+ycent;
                    //interpolem yright
                    double xinter = (yright-ord)/pen;
                    xpeak = (xinter+xright)/2.;
                }

                peaks.add(new DataPoint(xpeak,ycent,this.getPoint(i).getSdy()));
            }
        }
        return bkg;
    }
    
    public void addPeak(DataPoint dp){
        peaks.add(dp);
    }
    
    public void addPeak(double xpeak){
        double inten = 0;
        double sdinten = 0;
        //busquem els dos pics més propers (del voltant) i agafem la intensitat més alta
        for (int i=1;i<this.getNpoints()-1;i++){
            if ((this.getPoint(i).getX()<=xpeak) && (this.getPoint(i+1).getX()>=xpeak)){
                //el punt clicat està entre aquests dos
                inten = FastMath.max(this.getPoint(i).getY(), this.getPoint(i+1).getY());
                break;
            }
        }
        peaks.add(new DataPoint(xpeak,inten,sdinten));
    }

    public static float getDef_markerSize() {
        return def_markerSize;
    }

    public static void setDef_markerSize(float def_markerSize) {
        DataSerie.def_markerSize = def_markerSize;
    }

    public static float getDef_lineWidth() {
        return def_lineWidth;
    }

    public static void setDef_lineWidth(float def_lineWidth) {
        DataSerie.def_lineWidth = def_lineWidth;
    }

    //returns the closest DP to the one entered (usually by clicking)
    public DataPoint getClosestDP(DataPoint click, double tolX, double tolY){
        if (tolX<0)tolX=1.0;
        if (tolY<0)tolY=5000;
        DataPoint closest = null;
        double minDiffX = Double.MAX_VALUE/2.5;
        double minDiffY = Double.MAX_VALUE/2.5;
        for (int i=0; i<this.getNpoints();i++){
            DataPoint dp = this.getPoint(i);
            double diffX = FastMath.abs(dp.getX()-click.getX());
            double diffY = FastMath.abs(dp.getY()-click.getY());
            if ((diffX<tolX)&&(diffY<tolY)){
                if ((diffX+diffY)<(minDiffX+minDiffY)){
                    minDiffX=diffX;
                    minDiffY=diffY;
                    closest = dp;
                    log.fine("index of the closest in loop (i)= "+i);
                    log.fine("index of the closest in loop (indexof dp)= "+seriePoints.indexOf(dp));
                }
            }
        }
        log.fine("index of the closest="+this.seriePoints.indexOf(closest));
        return closest;
    }
    
    //returns the closest DP to the one entered (usually by clicking)
    public DataPoint getClosestDP_xonly(double xvalue, double tolX){
        if (tolX<0)tolX=1.0;
        DataPoint closest = null;
        double minDiffX = Double.MAX_VALUE/2.5;
        for (int i=0; i<this.getNpoints();i++){
            DataPoint dp = this.getPoint(i);
            double diffX = FastMath.abs(dp.getX()-xvalue);
            if (diffX<tolX){
                if (diffX<minDiffX){
                    minDiffX=diffX;
                    closest = dp;
                    log.fine("index of the closest X in loop (i)= "+i);
                    log.fine("index of the closest X in loop (indexof dp)= "+seriePoints.indexOf(dp));
                }
            }
        }
        log.fine("index of the closest X ="+this.seriePoints.indexOf(closest));
        return closest;
    }
    
    public DataPoint[] getSurroundingDPs(double xvalue){
        
        for (int i=0;i<this.getNpoints()-1;i++){
            if ((this.getPoint(i).getX()<=xvalue) && (this.getPoint(i+1).getX()>=xvalue)){
                DataPoint[] dps = {this.getPoint(i), this.getPoint(i+1)};
                return dps;
            }
        }
        return null;
    }
    
    //return Y
    public double interpolateY(double xval, DataPoint dp1, DataPoint dp2){
        double yleft = dp1.getY();
        double yright = dp2.getY();
        double xleft = dp1.getX();
        double xright = dp2.getX();
        
        double pen = (yright-yleft)/(xright-xleft);
        double ord = pen*(xleft)*-1+yleft;
        return pen*xval + ord;
    }
    //return SDY
    public double interpolateSDY(double xval, DataPoint dp1, DataPoint dp2){
        double yleft = dp1.getSdy();
        double yright = dp2.getSdy();
        double xleft = dp1.getX();
        double xright = dp2.getX();
        
        double pen = (yright-yleft)/(xright-xleft);
        double ord = pen*(xleft)*-1+yleft;
        return pen*xval + ord;
    }
    
    //returns the closest DP to the one entered (usually by clicking)
    public DataPoint getClosestPeak(DataPoint click, double tolX){
        if (tolX<0)tolX=1.0;
        DataPoint closest = null;
        double minDiffX = Double.MAX_VALUE;
        for (int i=0; i<this.getNpeaks();i++){
            DataPoint dp = this.getPeak(i);
            double diffX = FastMath.abs(dp.getX()-click.getX());
            if (diffX<tolX){
                log.fine("Xpeak("+i+")="+dp.getX()+" diff="+diffX);
                if (diffX<minDiffX){
                    minDiffX=diffX;
                    closest = dp;
                    log.fine("index of the closest in loop (i)= "+i);
                    log.fine("index of the closest in loop (indexof dp)= "+peaks.indexOf(dp));
                }
            }
        }
        log.fine("index of the closest="+this.seriePoints.indexOf(closest));
        return closest;
    }

    /**
     * @return the serieName
     */
    public String getSerieName() {
        return serieName;
    }

    /**
     * @param serieName the serieName to set
     */
    public void setSerieName(String serieName) {
        this.serieName = serieName;
    }
    
    private void logdebug(String s){
        if(D1Dplot_global.isDebug())log.debug(s);
    }
    
}
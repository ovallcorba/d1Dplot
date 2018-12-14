package com.vava33.d1dplot.auxi;


/**
 * D1Dplot
 * 
 * Representation of a 1D powder diffraction pattern
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import com.vava33.d1dplot.D1Dplot_global;
import com.vava33.jutils.FileUtils;

public class Pattern1D {

    private File file;
    private double original_wavelength = -1f;
    private ArrayList<String> commentLines;
    private ArrayList<DataSerie> series; //usually one, maybe more (eg. PRF files) 
    private static int globalNseries = 0; //TOTAL EN TOTES LES CLASSES

    //prf exclusive
    private boolean isPrf = false;
    private static boolean plotwithbkg=false; //static perque afectara a totes les series!
    private static int hkloff=-10;//pixels
    private static int hklticksize=6;
    private static boolean prfFullprofColors=false;
    
    //create empty pattern
    public Pattern1D(){
        commentLines = new ArrayList<String>();
        series = new ArrayList<DataSerie>();
        this.setOriginal_wavelength(-1f);
    }
    
    private DataSerie containsBkgSerie(){
        Iterator<DataSerie> itrDS = this.getSeriesIterator();
        while (itrDS.hasNext()){
            DataSerie ds = itrDS.next();
            if (ds.getTipusSerie()==DataSerie.serieType.bkg){
                return ds;
            }
        }
        return null;
    }
    private DataSerie containsBkgEstimP(){
        Iterator<DataSerie> itrDS = this.getSeriesIterator();
        while (itrDS.hasNext()){
            DataSerie ds = itrDS.next();
            if (ds.getTipusSerie()==DataSerie.serieType.bkgEstimP){
                return ds;
            }
        }
        return null;
    }
    
    private void prepareSerie(DataSerie ds) {
    	DataSerie.serieType st = ds.getTipusSerie();

    	//si el tipus de serie es bkgEstimP o bkg, hem de mirar si el pattern ja en te alguna igual (en aquest cas, substituïrlo, no afegir de nou)
    	if (st==DataSerie.serieType.bkg){
    		DataSerie dsbkg = containsBkgSerie();
    		if (dsbkg!=null){
    			removeDataSerie(dsbkg);
    		}
    	}
    	if (st==DataSerie.serieType.bkgEstimP){
    		DataSerie dsbkg = containsBkgEstimP();
    		if (dsbkg!=null){
    			removeDataSerie(dsbkg);
    		}
    	}

    	switch(st){
    	case dat:
    		ds.setColor(getNextColor());
    		break;
    	case obs:
    		if (isPrfFullprofColors()){
    			ds.setColor(Color.RED);                    
    		}else{
    			ds.setColor(Color.BLACK);
    		}
    		ds.setLineWidth(0);
    		ds.setMarkerSize(4);
    		break;
    	case cal:
    		if (isPrfFullprofColors()){
    			ds.setColor(Color.BLACK);    
    		}else{
    			ds.setColor(Color.RED);                    
    		}
    		ds.setMarkerSize(0);
    		break;
    	case diff:
    		if (isPrfFullprofColors()){
    			ds.setColor(Color.BLUE);    
    		}else{
    			ds.setColor(Color.BLUE);
    		}
    		ds.setMarkerSize(0.0f);
    		break;
    	case hkl:
    		if (isPrfFullprofColors()){
    			ds.setColor(Color.GREEN.darker());    
    		}else{
    			ds.setColor(Color.GREEN.darker());    
    		}
    		ds.setMarkerSize(0.0f);
    		break;
    	case bkg:
    		ds.setColor(Color.PINK);
    		ds.setLineWidth(DataSerie.getDef_lineWidth()+1);
    		break;
    	case bkgEstimP:
    		ds.setColor(Color.PINK);
    		ds.setMarkerSize(DataSerie.getDef_markerSize()+2.0f);
    		ds.setLineWidth(0.0f);
    		break;
    	case gr:
    		ds.setColor(getNextColor());
    		break;
    	default:
    		ds.setColor(getNextColor());
    		break;
    	}
    }
    
    public void addDataSerie(DataSerie ds){
        this.prepareSerie(ds);
        globalNseries = globalNseries + 1;
        this.series.add(ds);
        ds.setPatt1D(this); //important que estigui aqui baix
    }
    
    public void addDataSerie(int pos, DataSerie ds){
    	this.prepareSerie(ds);
    	try {
    		this.series.get(pos);
    	}catch(IndexOutOfBoundsException ex) {
    		globalNseries = globalNseries + 1;
    	}
    	this.series.add(pos,ds);
    }
    
    public void removeDataSerie(DataSerie ds){
        this.series.remove(ds);
        globalNseries = globalNseries - 1;
    }
    
    public void removeDataSerie(int index){
        this.series.remove(index);
        globalNseries = globalNseries - 1;
    }
    
    public int getNseriesPattern(){
        return this.series.size();
    }
    
    public void removeAllSeries(){
        globalNseries = globalNseries - this.series.size();
        this.series.clear();
    }
    
    public void swapSeries(int pos1, int pos2) {
    	Collections.swap(series, pos1, pos2);
    }
    
    public void removeBkgSerie(){
        Iterator<DataSerie> itrDS = this.getSeriesIterator();
        DataSerie toRemove = null;
        while (itrDS.hasNext()){
            DataSerie ds = itrDS.next();
            if (ds.getTipusSerie()==DataSerie.serieType.bkg){
                toRemove = ds;
            }
        }
        if (toRemove!=null)this.removeDataSerie(toRemove);
    }

    public void removeBkgEstimPSerie(){
        Iterator<DataSerie> itrDS = this.getSeriesIterator();
        DataSerie toRemove = null;
        while (itrDS.hasNext()){
            DataSerie ds = itrDS.next();
            if (ds.getTipusSerie()==DataSerie.serieType.bkgEstimP){
                toRemove = ds;
            }
        }
        if (toRemove!=null)this.removeDataSerie(toRemove);
    }
    
    public DataSerie getBkgEstimPSerie(){
        Iterator<DataSerie> itrDS = this.getSeriesIterator();
        while (itrDS.hasNext()){
            DataSerie ds = itrDS.next();
            if (ds.getTipusSerie()==DataSerie.serieType.bkgEstimP){
                return ds;
            }
        }
        return null;
    }

    
    public static Color getNextColor(){
        //aqui segons el "TEMA" s'assignarà el color
        if (D1Dplot_global.isLightTheme()){
            int ncol = (globalNseries)%D1Dplot_global.lightColors.length;
            return FileUtils.parseColorName(D1Dplot_global.lightColors[ncol]);
        }else{
            int ncol = (globalNseries)%D1Dplot_global.DarkColors.length;
            return FileUtils.parseColorName(D1Dplot_global.DarkColors[ncol]);
        }
    }
    
    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public ArrayList<String> getCommentLines() {
        return commentLines;
    }

    public void setCommentLines(ArrayList<String> commentLines) {
        this.commentLines = commentLines;
    }

//    public ArrayList<DataSerie> getSeries() {
//        return series;
//    }

    public DataSerie getSerie(int index){
        return series.get(index);
    }
    
    public Iterator<DataSerie> getSeriesIterator(){
        return series.iterator();
    }
    
    public int getNseries(){
        return series.size();
    }
    
    public int indexOfSerie(DataSerie ds){
        return series.indexOf(ds);
    }
    
//    public void setSeries(ArrayList<DataSerie> series) {
//        this.series = series;
//    }

    public double getOriginal_wavelength() {
        return original_wavelength;
    }

    public boolean isPrf() {
        return isPrf;
    }

    public void setPrf(boolean isPrf) {
        this.isPrf = isPrf;
    }

    public void setOriginal_wavelength(double original_wavelength) {
        this.original_wavelength = original_wavelength;
    }

    public static int getHklticksize() {
        return hklticksize;
    }

    public static void setHklticksize(int hklticksize) {
        Pattern1D.hklticksize = hklticksize;
    }

    public static int getHkloff() {
        return hkloff;
    }

    public static void setHkloff(int hkloff) {
        Pattern1D.hkloff = hkloff;
    }

    public static boolean isPlotwithbkg() {
        return plotwithbkg;
    }

    public static void setPlotwithbkg(boolean plotwithbkg) {
        Pattern1D.plotwithbkg = plotwithbkg;
    }

    public static boolean isPrfFullprofColors() {
        return prfFullprofColors;
    }

    public static void setPrfFullprofColors(boolean prfFullprofColors) {
        Pattern1D.prfFullprofColors = prfFullprofColors;
    }
}

package com.vava33.d1dplot.data;

/**
 * D1Dplot
 * 
 * Common class for all types of data to be extended by others (plottables)
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.vava33.d1dplot.D1Dplot_global;
import com.vava33.jutils.VavaLogger;

public class Data_Common implements Plottable{ //TODO no fa falta implementar el plottable des d'aqui
    
    private static final String className = "Data_Common";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);
    
    private File file;
    private double original_wavelength;
    private List<String> commentLines;
    protected List<DataSerie> series;
    
    public Data_Common(double wave, File f) {
        this.file=f;
        this.original_wavelength = wave;
        this.commentLines = new ArrayList<String>();
        this.series = new ArrayList<DataSerie>();
    }
    
    public Data_Common(double wave) {
        this(wave,null);
    }
    
    public Data_Common() {
        this(-1,null);
    }
    
    @Override
    public double getOriginalWavelength() {
        return original_wavelength;
    }
    
    public void setOriginalWavelength(double wavelA) {
        this.original_wavelength=wavelA;
    }
    
    public void addCommentLine(String s) {
        this.commentLines.add(s);
    }
    
    public void addCommentLines(List<String> commentlines) {
        this.commentLines.addAll(commentlines);
    }
    
    @Override
    public List<String> getCommentLines() {
        return commentLines;
    }
    @Override
    public File getFile() {
        return file;
    }
    @Override
    public void setFile(File file) {
        this.file = file;
    }
    
    @Override
    public void addDataSerie(DataSerie ds) {
        this.series.add(ds);
        ds.setParent(this);
    }
    
    @Override
    public void removeDataSerie(DataSerie ds) {
        this.series.remove(ds);
    }
    
    @Override
    public void removeDataSeries(List<DataSerie> ds) {
        this.series.removeAll(ds);
    }
    
    @Override
    public int getNSeries() {
        return series.size();
    }
    
//    public void swapSeries(int orig, int dest) {
//        Collections.swap(series, orig, dest);
//    }
    
    /**
     * replaces first occurence of the stype, otherwise adds the dataserie
     */
    @Override
    public void replaceDataSerie(DataSerie newDS, SerieType stypeToReplace, boolean addIfNotFound) {
        int index = -1;
        try {
            index = this.series.indexOf(this.getDataSeriesByType(stypeToReplace).get(0));
        }catch(Exception ex) {
            log.info("DataSerie not found, adding as new");
        }
        newDS.setTipusSerie(stypeToReplace);
        newDS.setParent(this);
        if (index>=0) {
            series.set(index, newDS);
        }else {
            series.add(newDS);
        }
    }
    
    @Override
    public List<DataSerie> getDataSeries() {
        return series;
    }
    
    @Override //o es podria fer manualment 1 2 3 4 you know..
    public int indexOfDS(DataSerie ds) {
        return this.getDataSeries().indexOf(ds);
    }

    @Override
    public DataSerie getDataSerie(int n) {
        return getDataSeries().get(n);
    }

    @Override
    public List<DataSerie> getDataSeriesByType(SerieType tipus) {
        List<DataSerie> series = new ArrayList<DataSerie>();
        for (DataSerie ds:this.getDataSeries()) {
            if(ds.getTipusSerie()==tipus)series.add(ds);
        }
        return series;
    }
    @Override
    public DataSerie getFirstDataSerieByType(SerieType tipus) {
        for (DataSerie ds:this.getDataSeries()) {
            if(ds.getTipusSerie()==tipus)return ds;
        }
        return null;
    }
    
    
    public void setWavelengthToAllSeries(double wave) {
        for (DataSerie ds:this.getDataSeries()) {
            ds.setWavelength(wave);
        }
    }
    
    public void setZeroToAllSeries(double zero) {
        for (DataSerie ds:this.getDataSeries()) {
            ds.setZerrOff(zero);
        }
    }

    @Override
    public DataSerie getMainSerie() {
        if (series.size()>0)return series.get(0);
        return null;
    }
    
}


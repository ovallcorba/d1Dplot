package com.vava33.d1dplot.data;

/**
 * D1Dplot
 * 
 * DataSet is a container for DataSeries
 * 
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.vava33.BasicPlotPanel.core.SerieType;
import com.vava33.d1dplot.D1Dplot_global;
import com.vava33.jutils.VavaLogger;

public class DataSet {
    
    private static final String className = "DataSet";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);
    
    private File file;
    private double original_wavelength;
    private List<String> commentLines;
    protected List<DataSerie> series;
    
    public DataSet(double wave, File f) {
        this.file=f;
        this.original_wavelength = wave;
        this.commentLines = new ArrayList<String>();
        this.series = new ArrayList<DataSerie>();
    }
    
    public DataSet(double wave) {
        this(wave,null);
    }
    
    public DataSet() {
        this(-1,null);
    }
    
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
    
    public List<String> getCommentLines() {
        return commentLines;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
    
    public void addDataSerie(DataSerie ds) {
        this.series.add(ds);
        ds.setParent(this);
    }
    
    public void removeDataSerie(DataSerie ds) {
        this.series.remove(ds);
    }
    
    public void removeDataSeries(List<DataSerie> ds) {
        this.series.removeAll(ds);
    }
    
    public int getNSeries() {
        return series.size();
    }
    
    /**
     * replaces first occurence of the stype, otherwise adds the dataserie
     */
    public void replaceDataSerie(DataSerie newDS, SerieType stypeToReplace, boolean addIfNotFound) {
        int index = -1;
        try {
            index = this.series.indexOf(this.getDataSeriesByType(stypeToReplace).get(0));
        }catch(Exception ex) {
            log.info("DataSerie not found, adding as new");
        }
        newDS.setSerieType(stypeToReplace);
        newDS.setParent(this);
        if (index>=0) {
            series.set(index, newDS);
        }else {
            series.add(newDS);
        }
    }
    
    public List<DataSerie> getDataSeries() {
        return series;
    }
    
    public int indexOfDS(DataSerie ds) {
        return this.getDataSeries().indexOf(ds);
    }

    public DataSerie getDataSerie(int n) {
        return getDataSeries().get(n);
    }

    public List<DataSerie> getDataSeriesByType(SerieType tipus) {
        List<DataSerie> series = new ArrayList<DataSerie>();
        for (DataSerie ds:this.getDataSeries()) {
            if(ds.getSerieType()==tipus)series.add(ds);
        }
        return series;
    }
    
    public void removeDataSeriesByType(SerieType tipus) {
        List<DataSerie> seriesToRemove = new ArrayList<DataSerie>();
        for (DataSerie ds:this.getDataSeries()) {
            if(ds.getSerieType()==tipus)seriesToRemove.add(ds);
        }
        series.removeAll(seriesToRemove);
    }

    public DataSerie getFirstDataSerieByType(SerieType tipus) {
        for (DataSerie ds:this.getDataSeries()) {
            if(ds.getSerieType()==tipus)return ds;
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
            ds.setXOffset(zero);
        }
    }

    public DataSerie getMainSerie() {
        if (series.size()>0)return series.get(0);
        return null;
    }
}
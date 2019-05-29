package com.vava33.d1dplot.data;

/**
 * D1Dplot
 * 
 * Interface representing a plottable object. 
 * It will contain dataseries of plottable points to plot.
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.io.File;
import java.util.List;

public interface Plottable {

    public void addDataSerie(DataSerie ds);
    public void removeDataSerie(DataSerie ds);
    public void removeDataSeries(List<DataSerie> ds);
    public int getNSeries();
    public List<DataSerie> getDataSeries();
    public DataSerie getMainSerie();
    public File getFile();
    public void setFile(File file);
    public double getOriginalWavelength();
    public List<String> getCommentLines();
    public int indexOfDS(DataSerie ds);
    public DataSerie getDataSerie(int n);
    public List<DataSerie> getDataSeriesByType(SerieType tipus);
    public DataSerie getFirstDataSerieByType(SerieType tipus);
    public void replaceDataSerie(DataSerie ds, SerieType stype, boolean addIfNotFound); //first occurrence
    public void addCommentLines(List<String> commentLines);
    public void addCommentLine(String string);
}

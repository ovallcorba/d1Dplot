package vava33.d1dplot.auxi;

/**
 * D1Dplot
 * 
 * Representation of a 1D powder diffraction pattern
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.io.File;
import java.util.ArrayList;

public class Pattern1D {

    private File file;
    private Float wavelength;
    private Float t2i,t2f,step;
    private ArrayList<DataPoint> points;
    private ArrayList<String> commentLines;
    private ArrayList<DataSerie> series; //usually one, maybe more (eg. PRF files) 
    
    //create empty pattern
    public Pattern1D(){
        points = new ArrayList<DataPoint>();
        commentLines = new ArrayList<String>();
        setSeries(new ArrayList<DataSerie>());
        this.setWavelength(-1f);
        this.setT2i(-1f);
        this.setT2f(-1f);
        this.setStep(-1f);
    }
    
    public void AddDefaultDataSerie(){
        this.getSeries().add(new DataSerie(points));
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Float getWavelength() {
        return wavelength;
    }

    public void setWavelength(Float wavelength) {
        this.wavelength = wavelength;
    }

    public Float getT2i() {
        return t2i;
    }

    public void setT2i(Float t2i) {
        this.t2i = t2i;
    }

    public Float getT2f() {
        return t2f;
    }

    public void setT2f(Float t2f) {
        this.t2f = t2f;
    }

    public Float getStep() {
        return step;
    }

    public void setStep(Float step) {
        this.step = step;
    }

    public ArrayList<DataPoint> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<DataPoint> points) {
        this.points = points;
    }

    public ArrayList<String> getCommentLines() {
        return commentLines;
    }

    public void setCommentLines(ArrayList<String> commentLines) {
        this.commentLines = commentLines;
    }

    public ArrayList<DataSerie> getSeries() {
        return series;
    }

    public void setSeries(ArrayList<DataSerie> series) {
        this.series = series;
    }
    
    
    
}

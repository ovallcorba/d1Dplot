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

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;

import vava33.d1dplot.D1Dplot_global;

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
//        points = new ArrayList<DataPoint>();
        commentLines = new ArrayList<String>();
        setSeries(new ArrayList<DataSerie>());
        this.setOriginal_wavelength(-1f);
    }
    
    
    public void AddDataSerie(DataSerie ds){
        ds.setPatt1D(this);
        globalNseries = globalNseries + 1;
        DataSerie.serieType st = ds.getTipusSerie();
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
//                ds.setMarkerSize(markerSize);TODO posar markersizedefault a d1dplotglobal iaugmentar a partir d'aquest valor
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
                ds.setColor(getNextColor()); //TODO:ja veurem
                break;
            default:
                ds.setColor(getNextColor());
                break;
        }
        this.getSeries().add(ds);
    }
    
    public void removeDataSerie(DataSerie ds){
        this.getSeries().remove(ds);
        globalNseries = globalNseries - 1;
    }
    
    public void removeDataSerie(int index){
        this.getSeries().remove(index);
        globalNseries = globalNseries - 1;
    }
    
    public int getNseriesPattern(){
        return this.series.size();
    }
    
    private Color getNextColor(){
        //aqui segons el "TEMA" s'assignarà el color
        if (D1Dplot_global.isLightTheme()){
            int ncol = (globalNseries-1)%D1Dplot_global.lightColors.length;
            return D1Dplot_global.parseColorName(D1Dplot_global.lightColors[ncol]);
        }else{
            int ncol = (globalNseries-1)%D1Dplot_global.DarkColors.length;
            return D1Dplot_global.parseColorName(D1Dplot_global.DarkColors[ncol]);
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

    public ArrayList<DataSerie> getSeries() {
        return series;
    }

    public void setSeries(ArrayList<DataSerie> series) {
        this.series = series;
    }

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

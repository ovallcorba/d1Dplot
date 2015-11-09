package vava33.d1dplot.auxi;

/**
 * D1Dplot
 * 
 * Data Serie to plot
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.util.ArrayList;
import java.util.Iterator;

public class DataSerie {

    private ArrayList<DataPoint> seriePoints;
    private float markerSize = 2;
    private boolean plotThis;
    
    public DataSerie(ArrayList<DataPoint> punts){
        this.setSeriePoints(punts);
        this.setPlotThis(true);
    }

    public ArrayList<DataPoint> getSeriePoints() {
        return seriePoints;
    }

    public void setSeriePoints(ArrayList<DataPoint> seriePoints) {
        this.seriePoints = seriePoints;
    }

    public float getMarkerSize() {
        return markerSize;
    }

    public void setMarkerSize(float markerSize) {
        this.markerSize = markerSize;
    }
    
    public DataPoint getPuntMaxY(){
        if (seriePoints!=null){
            Iterator<DataPoint> itrp = seriePoints.iterator();
            DataPoint puntMaxY = null;
            float maxY = Float.MIN_VALUE;
            while (itrp.hasNext()){
                DataPoint punt = itrp.next();
                if (punt.getY() > maxY){
                    puntMaxY = punt;
                    maxY = punt.getY();
                }
            }
            return puntMaxY;
        }else{
            return null;
        }
    }
    
    public DataPoint getPuntMaxX(){
        if (seriePoints!=null){
            Iterator<DataPoint> itrp = seriePoints.iterator();
            DataPoint puntMaxX = null;
            float maxX = Float.MIN_VALUE;
            while (itrp.hasNext()){
                DataPoint punt = itrp.next();
                if (punt.getX() > maxX){
                    puntMaxX = punt;
                    maxX = punt.getX();
                }
            }
            return puntMaxX;
        }else{
            return null;
        }
    }

    public DataPoint getPuntMinY(){
        if (seriePoints!=null){
            Iterator<DataPoint> itrp = seriePoints.iterator();
            DataPoint puntMinY = null;
            float minY = Float.MAX_VALUE;
            while (itrp.hasNext()){
                DataPoint punt = itrp.next();
                if (punt.getY() < minY){
                    puntMinY = punt;
                    minY = punt.getY();
                }
            }
            return puntMinY;
        }else{
            return null;
        }
    }
    
    public DataPoint getPuntMinX(){
        if (seriePoints!=null){
            Iterator<DataPoint> itrp = seriePoints.iterator();
            DataPoint puntMinX = null;
            float minX = Float.MAX_VALUE;
            while (itrp.hasNext()){
                DataPoint punt = itrp.next();
                if (punt.getX() < minX){
                    puntMinX = punt;
                    minX = punt.getX();
                }
            }
            return puntMinX;
        }else{
            return null;
        }
    }
    
    public float[] getPuntsMaxXMinXMaxYMinY(){
        if (seriePoints!=null){
            Iterator<DataPoint> itrp = seriePoints.iterator();
            float minX = Float.MAX_VALUE;
            float minY = Float.MAX_VALUE;
            float maxX = Float.MIN_VALUE;
            float maxY = Float.MIN_VALUE;
            while (itrp.hasNext()){
                DataPoint punt = itrp.next();
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
            return new float[]{maxX,minX,maxY,minY};
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
    
}

package vava33.d1dplot.auxi;

/**
 * D1Dplot
 * 
 * Data point contained in data series
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

public class DataPoint {

    private double x;
    private double y;
    private double sdy;
    private double yBkg;
    
    public DataPoint(double px, double py, double pysd){
        this.setX(px);
        this.setY(py);
        this.setSdy(pysd);
        this.setyBkg(0);
    }
    
    public DataPoint(double px, double py, double pysd, double ybkg){
        this.setX(px);
        this.setY(py);
        this.setSdy(pysd);
        this.setyBkg(ybkg);
    }
    
    public double getX() {
        return x;
    }
    public void setX(double x) {
        this.x = x;
    }
    public double getY() {
        return y;
    }
    public void setY(double y) {
        this.y = y;
    }
    public double getSdy() {
        return sdy;
    }
    public void setSdy(double sdy) {
        this.sdy = sdy;
    }
    public double getyBkg() {
        return yBkg;
    }
    public void setyBkg(double yBkg) {
        this.yBkg = yBkg;
    }
}

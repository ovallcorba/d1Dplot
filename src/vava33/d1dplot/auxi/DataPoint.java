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

    private float x;
    private float y;
    private float sdy;
    
    public DataPoint(float px, float py, float pysd){
        this.setX(px);
        this.setY(py);
        this.setSdy(pysd);
    }
    public float getX() {
        return x;
    }
    public void setX(float x) {
        this.x = x;
    }
    public float getY() {
        return y;
    }
    public void setY(float y) {
        this.y = y;
    }
    public float getSdy() {
        return sdy;
    }
    public void setSdy(float sdy) {
        this.sdy = sdy;
    }
}

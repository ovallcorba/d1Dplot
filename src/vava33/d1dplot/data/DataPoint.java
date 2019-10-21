package com.vava33.d1dplot.data;

/**
 * D1Dplot
 * 
 * Data point contained in data series
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

public class DataPoint implements Plottable_point{ //plottable point ja inclou comparable

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
    public void addY(double y) {
        this.y = this.y + y;
    }
    public double getSdy() {
        return sdy;
    }
    public void setSdy(double sdy) {
        this.sdy = sdy;
    }
    public double getYbkg() {
        return yBkg;
    }
    public void setyBkg(double yBkg) {
        this.yBkg = yBkg;
    }

    //incrementoY i multiplico o al revés??
    @Override //caldra implementar-la a totes les classes per mantenir el TIPUS
    public Plottable_point getCorrectedDataPoint(double incX, double incY, double factorY,boolean addYbkg) {
        if (addYbkg) {
            return new DataPoint(this.getX()+incX,(this.getY()+this.getYbkg())*factorY+incY,this.getSdy()*factorY,this.getYbkg()*factorY);
        }else {
            return new DataPoint(this.getX()+incX,this.getY()*factorY+incY,this.getSdy()*factorY,this.getYbkg()*factorY);    
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        DataPoint dp = (DataPoint)obj;
        if ((dp.getX()==this.getX())){
            return true;
        }else{
            return false;
        }
    }
    
	@Override
	public int compareTo(Plottable_point otherDP) {
		if (this.x>otherDP.getX()) {
			return 1; //otherDP va primer
		}else {
			return -1;
		}
	}

    @Override
    public String getInfo() {
        return String.format("(%.4f, %.4f)", x,y);
    }


}

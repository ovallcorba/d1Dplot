package com.vava33.d1dplot.auxi;

import javax.swing.JSlider;

public class DoubleJSlider extends JSlider {

    private static final long serialVersionUID = 1L;
    final int scale; //multiple de 10 segons decimals que volguem

    public DoubleJSlider(double min, double max, double value, int scale) {
        super((int)(min*scale), (int)(max*scale), (int)(value*scale));
        this.scale = scale;
    }

    public void setScaledValue(double val) {
        super.setValue((int)(val*scale));
        System.out.println(this.getValue());
    }
    
    public double getScaledValue() {
        return ((double)super.getValue()) / this.scale;
    }

}

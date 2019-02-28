package com.vava33.d1dplot.auxi;

/**
 * D1Dplot
 * 
 * Represents a powder diffraction hkl reflection (peak)
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import org.apache.commons.math3.util.FastMath;

public class PDReflection {

        private int h;
        private int k;
        private int l;
        private float dsp;
        private float inten;
        
        public PDReflection(int h, int k, int l, float dsp, float inten){
            this.h=h;
            this.k=k;
            this.l=l;
            this.dsp=dsp;
            this.inten=inten;
        }
        
        public int getH() {
            return h;
        }
        public void setH(int h) {
            this.h = h;
        }
        public int getK() {
            return k;
        }
        public void setK(int k) {
            this.k = k;
        }
        public int getL() {
            return l;
        }
        public void setL(int l) {
            this.l = l;
        }
        public float getDsp() {
            return dsp;
        }
        public void setDsp(float dsp) {
            this.dsp = dsp;
        }
        public float getInten() {
            return inten;
        }
        public void setInten(float inten) {
            this.inten = inten;
        }
        
        public float getT2(float wavelength, boolean degrees){
            return (float) (2 * FastMath.toDegrees(FastMath.asin(wavelength / (2*this.getDsp())))); //RADIANTS?
        }
}
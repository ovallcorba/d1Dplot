package com.vava33.d1dplot.auxi;

/**
 * D1Dplot
 * 
 * XRD Reference substance class (d-spacings)
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.util.ArrayList;
import org.apache.commons.math3.util.FastMath;

public class Reference {

    public static double[] LaB6_d = { 4.156878635, 2.939357609, 2.399975432, 2.078432243, 1.859004281, 1.697043447, 1.469674856, 1.385628455, 1.314520218, 
    		1.25335391, 1.199991704, 1.152911807, 1.110975349, 1.039218708, 1.008191043 };
    
    public static double[] Silicon_d = { 3.1356938, 1.9202274, 1.6375760, 1.3578002, 1.2460120, 1.1086449, 1.0452367, 0.9601140, 0.9180468, 0.8587511, 0.8282547};

    private class DspInten {
    	private double dsp;
    	private double inten;
    	private DataHKL hkl;
    	public DspInten(double dsp, double inten) {
    		this.setDsp(dsp);
    		this.setInten(inten);
    	}
    	public DspInten(int h, int k, int l, double inten, double wave) {
    		this.getHkl().setH(h);
    		this.getHkl().setK(k);
    		this.getHkl().setL(l);
    		this.setInten(inten);
    		//TODO:wave to calculate dsp
    		
    	}
		public double getDsp() {
			return dsp;
		}
		public void setDsp(double dsp) {
			this.dsp = dsp;
		}
		public double getInten() {
			return inten;
		}
		public void setInten(double inten) {
			this.inten = inten;
		}
		public DataHKL getHkl() {
			return hkl;
		}
		public void setHkl(DataHKL hkl) {
			this.hkl = hkl;
		}
    }
    
    private String name;
    private ArrayList<DspInten> dspsInten;
//    private Pattern1D pattToPlotRef;
    
    public Reference(String calName) {
        this.setName(calName);
        this.dspsInten = new ArrayList<DspInten>();
    }
    
    public Reference(String calName, double[] dspss) {
        this(calName);
        for (int i=0;i<dspss.length;i++) {
        	this.dspsInten.add(new DspInten(dspss[i],0));
        }
    }
    
//    public Reference(String calName, double[] dsps, Pattern1D toPlot) {
//        this(calName,dsps);
//        this.setPattToPlotRef(toPlot);
//    }

    
    public void addDspInten(double dsp, double inten) {
    	this.getDspsInten().add(new DspInten(dsp,inten));
    }
    
//    public void addDspInten(int h, int k, int l) {
//    	this.getDspsInten().add(new DspInten(h,k,l));
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<DspInten> getDspsInten() {
        return dspsInten;
    }

    public void setDspsInten(ArrayList<DspInten> dsp) {
        this.dspsInten = dsp;
    }
    
    
    public int getNpoints() {
    	return this.getDspsInten().size();
    }
    
    public double getInten(int pos) {
    	return this.getDspsInten().get(pos).getInten();
    }
    public double getDsp(int pos) {
    	return this.getDspsInten().get(pos).getDsp();
    }
    public double getTth(double wave, int pos) {
    	return FastMath.toDegrees(PattOps.get2thRadFromDsp(wave, this.dspsInten.get(pos).getDsp()));
    }
    
    public double[] gettthDeg(double wave) {
    	double[] tth = new double[this.dspsInten.size()];
    	for (int i=0;i<this.dspsInten.size();i++) {
    		tth[i] = FastMath.toDegrees(PattOps.get2thRadFromDsp(wave, this.dspsInten.get(i).getDsp()));
    	}
    	return tth;
    }

//
//	public Pattern1D getPattToPlotRef() {
//		return pattToPlotRef;
//	}
//
//	public void setPattToPlotRef(Pattern1D whereToPlotRef) {
//		this.pattToPlotRef = whereToPlotRef;
//	}

	@Override
	public String toString() {
    	return name;
	}
}

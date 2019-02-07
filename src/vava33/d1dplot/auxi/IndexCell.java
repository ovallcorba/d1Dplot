package com.vava33.d1dplot.auxi;

/*
 *
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.util.FastMath;

import com.vava33.d1dplot.D1Dplot_global;
import com.vava33.jutils.VavaLogger;

public class IndexCell {

    private static final String className = "IndexCell";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);
	
    private double a,b,c,al,be,ga; //in radians
    private double aStar,bStar,cStar,alStar,beStar,gaStar; 
    private double QA,QB,QC,QD,QE,QF;
    
    private RealMatrix G; //metric
    private RealMatrix Gstar;
    
	public IndexCell(double a, double b, double c, double al, double be, double ga, boolean radians) {
		this.a=a;
		this.b=b;
		this.c=c;
		if (!radians) {
			this.al=FastMath.toRadians(al);
			this.be=FastMath.toRadians(be);
			this.ga=FastMath.toRadians(ga);
		}else {
			this.al=al;
			this.be=be;
			this.ga=ga;
		}
		calcMetricMatrix();
		calcGstarMatrix();
		calcReciprocalParameters();
		calcQvect();
	}
	
	
	
	private void calcMetricMatrix() {
		G = new Array2DRowRealMatrix(3, 3); //rows, columns
		G.setEntry(0, 0, a*a);
		G.setEntry(1, 1, b*b);
		G.setEntry(2, 2, c*c);
		G.setEntry(0, 1, a*b*FastMath.cos(ga));
		G.setEntry(0, 2, a*c*FastMath.cos(be));
		G.setEntry(1, 0, G.getEntry(0, 1));
		G.setEntry(2, 0, G.getEntry(0, 2));
		G.setEntry(2, 1, b*c*FastMath.cos(al));
		G.setEntry(1, 2, G.getEntry(2, 1));
	}
	
	private void calcGstarMatrix() {
		try {
			Gstar = MatrixUtils.inverse(G);	
		}catch(Exception e) {
			log.info("Error inverting metric matrix");
		}
	}
	
	private void calcReciprocalParameters() {
		aStar = FastMath.sqrt(Gstar.getEntry(0, 0));
		bStar = FastMath.sqrt(Gstar.getEntry(1, 1));
		cStar = FastMath.sqrt(Gstar.getEntry(2, 2));
		gaStar = FastMath.acos(Gstar.getEntry(0, 1)/(aStar*bStar)); 
		beStar = FastMath.acos(Gstar.getEntry(0, 2)/(aStar*cStar));
		alStar = FastMath.acos(Gstar.getEntry(2, 1)/(cStar*bStar));
	}
	private void calcQvect() {
		QA=Gstar.getEntry(0, 0);
		QB=Gstar.getEntry(1, 1);
		QC=Gstar.getEntry(2, 2);
		QD=Gstar.getEntry(0, 1)*2;
		QE=Gstar.getEntry(2, 1)*2;
		QF=Gstar.getEntry(0, 2)*2;
	}
	
	public double[] getQabcdef() {
		return new double[]{QA,QB,QC,QD,QE,QF};
	}
	
	//ALTERNATIVA calc parametres reciprocs sense fer servir matriu metrica... NOMES PER COMPROVACIONS, comentar-ho si veiem que el normal funciona bé.
	private void recip() {
	    
	    double d2 = 1-FastMath.cos(al)*FastMath.cos(al)-FastMath.cos(be)*FastMath.cos(be)-FastMath.cos(ga)*FastMath.cos(ga)+2*FastMath.cos(al)*FastMath.cos(be)*FastMath.cos(ga);
	    double cr11 = (float) ((FastMath.sin(al)*FastMath.sin(al))/(d2*a*a));
	    double cr22 = (float) ((FastMath.sin(be)*FastMath.sin(be))/(d2*b*b));
	    double cr33 = (float) ((FastMath.sin(ga)*FastMath.sin(ga))/(d2*c*c));
	    double cr12 = (float) ((FastMath.cos(al)*FastMath.cos(be)-FastMath.cos(ga))/(d2*a*b));
	    double cr13 = (float) ((FastMath.cos(al)*FastMath.cos(ga)-FastMath.cos(be))/(d2*a*c));
	    double cr23 = (float) ((FastMath.cos(be)*FastMath.cos(ga)-FastMath.cos(al))/(d2*b*c));
	    log.writeFloats("config", cr11,cr22,cr33, cr12,cr13,cr23);
	}
	
	
}


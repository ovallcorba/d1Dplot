package com.vava33.d1dplot.tests;

/*
 *
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import com.vava33.d1dplot.D1Dplot_global;
import com.vava33.d1dplot.auxi.IndexCell;
import com.vava33.jutils.VavaLogger;

public class testsIndexing {
	
    private static final String className = "testsIndexing";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);

	double a,b,c,al,be,ga;
	double incr;
	
	double[] QkL; //Q coefficients low
	double[] QkH; //high
	
	public testsIndexing(double a, double b, double c, double al, double be, double ga) {
		// TODO Auto-generated constructor stub
		
		double incPar = 0.5;
		double incAng = 1.0;
		
//		this.a=a;
//		this.b=b;
//		this.c=c;
//		this.al=FastMath.toRadians(al);
//		this.be=FastMath.toRadians(be);
//		this.ga=FastMath.toRadians(ga);
		
		IndexCell icL = new IndexCell(a,b,c,al,be,ga,false);
		IndexCell icH = new IndexCell(a+incPar,b+incPar,c+incPar,al,be,ga,false);
		
		QkL = icL.getQabcdef();
		QkH = icH.getQabcdef();
		
		double[] Qlim111 = calcQlimits(1,1,1);
		
		System.out.println(String.format("Qlim111_LOW %f, Qlim111_HIGH %f", Qlim111[0],Qlim111[1]));
//		log.writeNameNums("info", true, "Qlim111_LOW, Qlim111_HIGH", Qlim111[0],Qlim111[1]);
		
	}
	
	public double[] calcQlimits(int h, int k, int l){

		double h2=h*h;
		double k2=k*k;
		double l2=l*l;

		double hk=h*k;
		double kl=k*l;
		double lh=l*h;
		double lamhk=hk;
		double lamkl=kl;
		double lamlh=lh;
		if (hk<0)lamhk=0;
		if (kl<0)lamkl=0;
		if (lh<0)lamlh=0;

		double Qlow = h2*QkL[0] + k2*QkL[1] + l2*QkL[2] + lamhk*QkL[3] + (hk-lamhk)*QkH[3] + lamkl*QkL[4] + (kl-lamkl)*QkH[4] + lamlh*QkL[5] + (lh-lamlh)*QkH[5];
		double Qhigh = h2*QkH[0] + k2*QkH[1] + l2*QkH[2] + lamhk*QkH[3] + (hk-lamhk)*QkL[3] + lamkl*QkH[4] + (kl-lamkl)*QkL[4] + lamlh*QkH[5] + (lh-lamlh)*QkL[5];
		
		return new double[]{Qlow,Qhigh};
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		testsIndexing ts = new testsIndexing(5,5,5,90,90,90);
//		ts.run();

	}

	
	
}

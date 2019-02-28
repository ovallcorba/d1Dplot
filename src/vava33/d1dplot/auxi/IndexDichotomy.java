package com.vava33.d1dplot.auxi;

/*
 * Implementation of the dichotomy method
 * 
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.math3.util.FastMath;

import com.vava33.cellsymm.Cell;
import com.vava33.cellsymm.HKLrefl;
import com.vava33.cellsymm.CellSymm_global.CrystalFamily;
import com.vava33.cellsymm.CellSymm_global.CrystalSystem;
import com.vava33.d1dplot.D1Dplot_global;
import com.vava33.jutils.FileUtils;
import com.vava33.jutils.VavaLogger;

public class IndexDichotomy {

    private static final String className = "IndexDichotomy";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);
	
    private static final float DEF_iniIncPar = 0.5f;
    private static final float DEF_iniIncAng = 5.0f;
    private static final int DEF_numIter = 6;
    
    private double aMin,bMin,cMin,alMin,beMin,gaMin; //in radians
    private double aMax,bMax,cMax,alMax,beMax,gaMax;
	
    private boolean cubic,tetra,hexa,orto,mono,tric;
	private double[] Qobs;
	private int nPeaksToUse=20; //default 20 maximum
    
    private double mindsp, Qmax, maxdsp, Qmin; //valors de les reflexions entrades
    private float deltaQerr;
    
    
    
    //constructor mínim
	public IndexDichotomy(DataSerie ds, int nPeaksToUse, double deltaQerror) {
		Qobs = ds.getListPeaksQ();
		Arrays.sort(Qobs);
		this.nPeaksToUse = FastMath.min(nPeaksToUse, Qobs.length);
		if (Qobs.length>this.nPeaksToUse) {
			Qobs = Arrays.copyOfRange(Qobs, 0, this.nPeaksToUse-1);
		}

		deltaQerr = (float) deltaQerror;
		Qmax = Qobs[Qobs.length-1]+deltaQerr;
		Qmin = Qobs[0];

		log.writeNameNumPairs("config", true, "deltaQerr", deltaQerr);
	}
	
	//TODO:es podria fer un constructor complert
	
	public void setMinPars(double a, double b, double c, double al, double be, double ga, boolean anglesInDegrees) {
		this.aMin=a;
		this.bMin=b;
		this.cMin=c;
		if (anglesInDegrees) {
			this.alMin=FastMath.toRadians(al);
			this.beMin=FastMath.toRadians(be);
			this.gaMin=FastMath.toRadians(ga);
		}else {
			this.alMin=al;
			this.beMin=be;
			this.gaMin=ga;
		}	
	}

	public void setMaxPars(double a, double b, double c, double al, double be, double ga, boolean anglesInDegrees) {
		this.aMax=a;
		this.bMax=b;
		this.cMax=c;
		if (anglesInDegrees) {
			this.alMax=FastMath.toRadians(al);
			this.beMax=FastMath.toRadians(be);
			this.gaMax=FastMath.toRadians(ga);
		}else {
			this.alMax=al;
			this.beMax=be;
			this.gaMax=ga;
		}
	}
	
	public void setSystemsSearch(boolean cubic, boolean tetra, boolean hexa, boolean orto, boolean mono, boolean tric) {
		this.cubic=cubic;
		this.tetra=tetra;
		this.hexa=hexa;
		this.orto=orto;
		this.mono=mono;
		this.tric=tric;
	}
    
	
	public ArrayList<IndexSolutionDichotomy> runIndexing(int numIter, float iniIncPar, float iniIncAng) {
	    
	    if (iniIncPar<=0)iniIncPar=DEF_iniIncPar;
	    if (iniIncAng<=0)iniIncAng=DEF_iniIncAng;
	    if (numIter<=0)numIter=DEF_numIter;
	    
	    //first iteration
//	    IndexSolutionDichotomy sol = new IndexSolutionDichotomy(this.aMin,this.aMax,this.bMin,this.bMax,this.cMin,this.cMax,this.alMin,this.alMax,this.beMin,this.beMax,this.gaMin,this.gaMax,iniIncPar,(float) FastMath.toRadians(iniIncAng));
	    Cell iniCell = new Cell(this.aMin,this.bMin,this.cMin,this.alMin,this.beMin,this.gaMin,false,CrystalFamily.NONE);
//	    IndexSolutionDichotomy sol = new IndexSolutionDichotomy((float)this.aMin,(float)this.bMin,(float)this.cMin,(float)this.alMin,(float)this.beMin,(float)this.gaMin,CrystalFamily.NONE,iniIncPar,(float) FastMath.toRadians(iniIncAng),Qobs);
	    IndexSolutionDichotomy startSol = new IndexSolutionDichotomy(iniCell,iniIncPar,(float) FastMath.toRadians(iniIncAng),Qobs,Qmax,deltaQerr);
	    log.info("start SOL= "+startSol.toString());
	    //al ser la primera iteracio hem de generar el conjunt de parametres per min-max parameters
	    ArrayList<IndexSolutionDichotomy> sols = this.runIteration(startSol,true);

	    log.info("iter 1, sols="+sols.size());

	    
	    for (int i=2;i<=numIter;i++) {
	        //ara hem de seguir la iteració per cadascuna de les solucions
	        
	        Iterator<IndexSolutionDichotomy> itrS = sols.iterator();
	        ArrayList<IndexSolutionDichotomy> sols2 = new ArrayList<IndexSolutionDichotomy>();
	        while (itrS.hasNext()) {
	            IndexSolutionDichotomy is = itrS.next();
	            sols2.addAll(this.runIteration(is,false));
	        }
	        //borrem sols i copiem les de sols2
	        sols.clear();
	        sols.addAll(sols2);
	        
	        log.info("iter "+i+", sols="+sols.size());
	        
	    
	    //mostrar solucions DEBUG (abans de afinar solucions i calcular figures mèrit)
	    Iterator<IndexSolutionDichotomy> itrS = sols.iterator();
	    int n=1;
	    while (itrS.hasNext()) {
	        log.info("*** SOL "+n+" ***\n"+itrS.next().toString());
	        n++;
	    }
	    
	    return sols;
	}
	
	private ArrayList<IndexSolutionDichotomy> runIteration(IndexSolutionDichotomy is, boolean firstIter){

	    ArrayList<IndexSolutionDichotomy> solutions = new ArrayList<IndexSolutionDichotomy>();
	    
	    //PRIMER GENEREM PARAMETRES
	    float[] aVals = new float[] {is.getAbase()};
	    float[] bVals = new float[] {is.getBbase()};
	    float[] cVals = new float[] {is.getCbase()};
	    float[] alVals = new float[] {is.getAlBaseRad()};
	    float[] beVals = new float[] {is.getBeBaseRad()};
	    float[] gaVals = new float[] {is.getGaBaseRad()};
	    
	    if (firstIter) {
	        aVals = is.getAVals((float)this.aMax);
	        bVals = is.getBVals((float)this.bMax);
	        cVals = is.getCVals((float)this.cMax);
	        alVals = is.getAlVals((float)this.alMax);
	        beVals = is.getBeVals((float)this.beMax);
	        gaVals = is.getGaVals((float)this.gaMax);
	    }else{
            aVals = is.getAVals(-1);
            bVals = is.getBVals(-1);
            cVals = is.getCVals(-1);
            alVals = is.getAlVals(-1);
            beVals = is.getBeVals(-1);
            gaVals = is.getGaVals(-1);
	    }
	    	    
	    
		//ho he separat per sistemes cristalins. Més complicat de mantenir (menys general) però més eficient.
	    //a=b=c 90º
	    if (this.cubic) {
            log.debug(Arrays.toString(aVals));
	        //temps
	        long startTime = System.currentTimeMillis();
	        int processedComb = 0;
	        int totalComb = aVals.length;
	        float onePercent = (float)totalComb/100.f;
	        
	        //iteracio celles
	        for (int j=0;j<aVals.length;j++) {
	            //ACTUALITZO PARAMETRE
	            float a= aVals[j]; //TODO: no he implementat limit parametres, cal?
	            Cell candidateCell = new Cell(a,a,a,is.getAlBaseRad(), is.getBeBaseRad(),is.getGaBaseRad(),false, CrystalFamily.CUBIC);
	            IndexSolutionDichotomy candidateSol = new IndexSolutionDichotomy(candidateCell,is.incPar/2.f,0,Qobs,Qmax,deltaQerr); //0 increment a angle (cubic)
	            
                //Iteracio HKLs
	            //A PARTIR D'AQUI HO PODRIEM MOURE A UN ALTRE METODE JA QEU SERA EQUIVALENT PER CUBIC,TETRA,HEXA,ORTO
	            //finalment això passa a ser responsabilitat de candidateSol... li preguntem si aquesta cel·la es solucio considerant els Qobs actuals (que ja sap també)
	            if (candidateSol.areAllQobsInsideHKLQintervals()) {
                  solutions.add(candidateSol);
                  log.debug("is solution:"+candidateSol.toString());    
	            }
	            
//	            if(this.iterHKLhighSymm(candidateSol)) {
//	                solutions.add(candidateSol);
//	                log.debug("is solution:"+candidateSol.toString());    
//	            }
	            
	            //temps
	            processedComb++;
	            if (processedComb%onePercent==0) {
	                long elapTime = (System.currentTimeMillis() - startTime)/1000;
	                float percent = ((float)processedComb/(float)totalComb)*100;
	                float estTime = (((100-percent)*elapTime)/percent)/60; //en minuts
	                log.info(String.format("[%s] %6.2f %% (est. time %6.2f min.)",CrystalFamily.CUBIC.getNameString(), percent,estTime));
	            }
	        }//for avals
	    }//iscubic
	    
	    //a=b!=c 90º
	    if (this.tetra) {


	    }

	    //a=b!=c gamma=120º
	    if (this.hexa) {

	    }
	    
	    //a!=b!=c 90º
	    if (this.orto) {

	    }
	    
	    return solutions;

	}
}



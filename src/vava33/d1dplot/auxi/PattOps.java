package com.vava33.d1dplot.auxi;

/**
 * D1Dplot
 * 
 * Operations with 1D XRD Powder Patterns
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

import javax.swing.table.DefaultTableModel;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.util.FastMath;

import com.vava33.d1dplot.D1Dplot_global;
import com.vava33.d1dplot.auxi.DataSerie.serieType;
import com.vava33.d1dplot.auxi.DataSerie.xunits;
import com.vava33.d1dplot.auxi.IndexSolutionGrid;
import com.vava33.d1dplot.auxi.PDReflection;
import com.vava33.jutils.FileUtils;
import com.vava33.jutils.VavaLogger;



public final class PattOps {
    
    
    private static final String className = "PattOps";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);
    
    private static DataSerie bkg_Bruchner_firstPass(DataSerie ds,double t2ini){  //no depen de N
        DataSerie ds0 = new DataSerie(ds,serieType.bkg,false);
        double[] vals = ds.calcYmeanYDesvYmaxYmin(); 
        double Imean = vals[0];
        double Imin = vals[3];
        
        log.debug("Imean= "+Imean+" Imin= "+Imin);
        
        //ara corregim els punts
        for(int i=0; i<ds.getNpoints(); i++){
            double x = ds.getPoint(i).getX();
            if (x<t2ini)continue;
            double y = ds.getPoint(i).getY();
            
            if(y>(Imean+10*(Imean-Imin))){
                ds0.addPoint(new DataPoint(x,Imean+10*(Imean-Imin),0));
            }else{
                ds0.addPoint(new DataPoint(x,y,0));
            }
        }
        return ds0;
    }
    //normal=true invers==normal=false
    //si multi = true, treure info de defaulttablemodel
    public static DataSerie bkg_Bruchner(DataSerie ds, int niter, int nveins, boolean edgenormal,double t2ini,boolean multi,DefaultTableModel m) {
        
        //primer fem el pas preliminar (es guarda a PATT[1])
        DataSerie ds0 = bkg_Bruchner_firstPass(ds,t2ini);
        DataSerie ds1 = new DataSerie(ds0,serieType.bkg,false);

        for(int p=0;p<niter;p++){
            ds1 = new DataSerie(ds0,serieType.bkg,false);
            
            for(int i=0; i<ds0.getNpoints(); i++){
                //rempla�arem cada punt i del diagrama per un de fons, que es la mitja dels +-N veins
                
                if(ds0.getPoint(i).getX()<t2ini)continue;
                
                //en cas que tinguem N variable:
                if(multi){
                    double t2punt= ds0.getPoint(i).getX();
                    int rows = m.getRowCount();
                    for(int j=0;j<rows;j++){//per cada fila mirem
                            double Ti = (Double) (m.getValueAt(j, 0));
                            double Tf = (Double) (m.getValueAt(j, 1));
                            if(t2punt>=Ti&&t2punt<=Tf){//si el punt esta al rang
                                nveins = (Integer)(m.getValueAt(j, 2));
                                break; //hem trobat rang, sortim del for
                            }
                    }
                }
                
                double sumI=0;
                for(int j=i-nveins; j<=i+nveins; j++){
                    if(j<=0){
                        //agafem intensitat del primer punt si s'ha triat NORMAL
                        //ORI2: AGAFEM LA COMPLEMENTARIA INVERTIDA si s'ha triat INVERT
                        if(edgenormal){
                            sumI=sumI+ds0.getPoint(0).getY();
                        }else{
                            sumI=sumI+ds0.getPoint(0).getY()+(ds0.getPoint(0).getY()-ds0.getPoint(-j).getY());
                        }
                        continue;

                    }
                    //AQUI FALTA IMPLEMENTAR LA COMPLEMENTARIA INVERTIDA TAMB�
                    if(j>=ds0.getNpoints()-1){
                        //agafem intensitat de l'ultim punt
                        sumI=sumI+ds0.getPoint(ds0.getNpoints()-1).getY();
                        continue;
                    }
                    if(j==i){
                        //el propi punt no el considerem
                        continue;
                    }
                    //cas punt "centre" diagrama
                    sumI=sumI+ds0.getPoint(j).getY();
                }

               //CANVI 130313: Comparem Ynew amb diagrama original (Patt[0]) i ens quedem amb la intensitat m�s petita
                double Ynew=sumI/(2*nveins);
                if(Ynew<ds0.getPoint(i).getY()){
                    //agafem el nou
                    ds1.addPoint(new DataPoint(ds0.getPoint(i).getX(),Ynew,0));
                }else{
                    //ens quedem l'Yobs original
                    ds1.addPoint(new DataPoint(ds0.getPoint(i).getX(),ds0.getPoint(i).getY(),0));
                }
            }
            
            //ARA ABANS D'ENTRAR AL SEGÜENT BUCLE POSEM ds1 com a ds0, ja que es creara un nou ds1
            ds0 = ds1;
        }
        
        //estem al final, retornem la serie final
        return ds1;
    }
    
    public static DataSerie findBkgPoints(DataSerie ds,int npoints){
        //dividirem la dataserie en troços (npoints) i a cada lloc buscarem el punt amb la I mes petita que estigui per sota del promig
        int npzona = (int)(ds.getNpoints()/npoints);
        log.debug("npuntsZona="+npzona);
        DataSerie dpPuntsFons = new DataSerie(ds,serieType.bkgEstimP,false);
        int startP = 0;
        while ((startP+npzona)<ds.getNpoints()){
            double[] meanYzonaYdesv = ds.calcYmeanYDesvYmaxYmin(startP, startP+npzona);
            DataPoint dp = ds.getMinYDataPoint(startP, startP+npzona);
            if (dp.getY()>(meanYzonaYdesv[0]+2*meanYzonaYdesv[1]))continue; //el saltem
            if (dp!=null){
                dpPuntsFons.addPoint(dp);
            }
            startP = startP+npzona+1;
        }
        return dpPuntsFons;
    }
    
    //SPLINE INTERP
    public static DataSerie bkg_FitSpline(DataSerie ds,DataSerie puntsFons) {
            
           double[] x = new double[puntsFons.getNpoints()];
           double[] y = new double[puntsFons.getNpoints()];
           
           for (int i=0;i<puntsFons.getNpoints();i++){
               x[i]=puntsFons.getPoint(i).getX();
               y[i]=puntsFons.getPoint(i).getY();
           }
           
           UnivariateInterpolator in = new SplineInterpolator();
           UnivariateFunction f = in.interpolate(x, y);

           DataSerie fons = new DataSerie(ds,serieType.bkg,false);
           

           double ini2t = puntsFons.getPoint(0).getX();
           DataPoint close = ds.getClosestDP(new DataPoint(ini2t,puntsFons.getPoint(0).getY(),0), 0.1, 50);
           int index = ds.getIndexOfDP(close);
           double curr2t = ds.getPoint(index).getX();
           while (curr2t<puntsFons.getPoint(puntsFons.getNpoints()-1).getX()){
               fons.addPoint(new DataPoint(curr2t,f.value(curr2t),0));
               index = index +1;
               curr2t = ds.getPoint(index).getX();
           }

           return fons;
       }
    
    
    public static DataSerie bkg_FitPoly(DataSerie ds,DataSerie puntsFons,int degree) {
        // Collect data.
           // Instantiate a n-degree polynomial fitter.
           final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(degree);

           final WeightedObservedPoints obs2 = new WeightedObservedPoints();
           
           for (int i=0;i<puntsFons.getNpoints();i++){
               obs2.add(puntsFons.getPoint(i).getX(),puntsFons.getPoint(i).getY());
           }
           final double[] coeff = fitter.fit(obs2.toList());

           log.writeFloats("config", coeff);
           
           final PolynomialFunction f = new PolynomialFunction(coeff);

           DataSerie fons = new DataSerie(ds,serieType.bkg,false);
           
           //pintem el fons polinomicalculat
           double ini2t = puntsFons.getPoint(0).getX();
           DataPoint close = ds.getClosestDP(new DataPoint(ini2t,puntsFons.getPoint(0).getY(),0), 0.1, 50);
           int index = ds.getIndexOfDP(close);
           double curr2t = ds.getPoint(index).getX();
           while (curr2t<puntsFons.getPoint(puntsFons.getNpoints()-1).getX()){
               fons.addPoint(new DataPoint(curr2t,f.value(curr2t),0));
               index = index +1;
               curr2t = ds.getPoint(index).getX();
           }
           return fons;
       }
    
    //it returns the coincident zone only
    // double t2i,t2f for factor calculation in case factor < 1
    public static DataSerie subtractDataSeriesCoincidentPoints(DataSerie ds1, DataSerie ds2, float factor, double fac_t2i, double fac_t2f){
        int tol =30;
        float percent_auto_factor = 0.95f; //this will be multiplied to the minimum factor to give some margin //TODO Passar a parametre global?
        
        DataSerie result = new DataSerie(ds1,ds1.getTipusSerie(),false);
        double t2i = FastMath.max(ds1.getPoint(0).getX(), ds2.getPoint(0).getX());
        double t2f = FastMath.min(ds1.getPoint(ds1.getNpoints()-1).getX(), ds2.getPoint(ds2.getNpoints()-1).getX());    
        
        
        DataPoint dp1ini = ds1.getClosestDP_xonly(t2i, tol);
        DataPoint dp1fin = ds1.getClosestDP_xonly(t2f, tol);
        
        int iinidp1 = ds1.getIndexOfDP(dp1ini);
        int ifindp1 = ds1.getIndexOfDP(dp1fin);
        
        DataPoint dp2ini = ds2.getClosestDP_xonly(t2i, tol);
        DataPoint dp2fin = ds2.getClosestDP_xonly(t2f, tol);
        
        int iinidp2 = ds2.getIndexOfDP(dp2ini);
        int ifindp2 = ds2.getIndexOfDP(dp2fin);
        
        int rangedp1 = ifindp1 - iinidp1;
        int rangedp2 = ifindp2 - iinidp2;
        
        if (rangedp1!=rangedp2){
            log.debug("different nr of points in the coincident range");
            return null;
        }
        
        //PRIMER RECALCULEM EL FACTOR EN CAS DE SER NEGATIU (AUTO)
        if (factor<0) {
            factor = getScaleFactor(ds1,ds2,iinidp1,iinidp2,rangedp1,fac_t2i,fac_t2f) * percent_auto_factor;
            log.info(String.format("Subtraction factor used = %.2f",factor));
        }

        
        for (int i=0;i<rangedp1;i++){ //TODO HAURIA DE SER <=  (Comprovar-ho)
            result.addPoint(new DataPoint(ds1.getPoint(iinidp1+i).getX(),ds1.getPoint(iinidp1+i).getY()-factor*ds2.getPoint(iinidp2+i).getY(),ds1.getPoint(iinidp1+i).getSdy()+factor*ds2.getPoint(iinidp2+i).getSdy()));
        }
        
        return result;
    }
    
    // get the factor such as all the intensities are (y1 - F*y2 >= 0), in a given range (rangedp) starting each serie in a specific point (iinidpX)
    public static float getScaleFactor(DataSerie ds1, DataSerie ds2, int iinidp1, int iinidp2, int rangedp, double fac_t2i, double fac_t2f) {
        float factor = 100;
        for (int i=0;i<rangedp;i++){ //TODO HAURIA DE SER <=  (Comprovar-ho)
            double x1 = ds1.getPoint(iinidp1+i).getX();
            if (x1<fac_t2i)continue;
            if (x1>fac_t2f)break;
            double y1 = ds1.getPoint(iinidp1+i).getY();
            double y2 = ds2.getPoint(iinidp2+i).getY();
            float fac = (float) (y1/y2);
            if (fac<factor)factor = fac;
        }
        return factor;
    }
    
    //ADDITION OF DATASERIES (with coincident points)
    public static DataSerie addDataSeriesCoincidentPoints(DataSerie[] dss){
      
      DataSerie result = new DataSerie(dss[0],dss[0].getTipusSerie(),false);
      
      int tol = 30;
      //aqui fer un for per totes les dataseries
      
      double[] t2is = new double[dss.length];
      double[] t2fs = new double[dss.length];
      for (int i=0; i<dss.length;i++){
          t2is[i]=dss[i].getPoint(0).getX();
          t2fs[i]=dss[i].getPoint(dss[i].getNpoints()-1).getX();
      }
      double t2i = findMax(t2is);
      double t2f = findMin(t2fs);
      
      DataPoint[] dpini = new DataPoint[dss.length];
      DataPoint[] dpfin = new DataPoint[dss.length];
      int[] iinidp = new int[dss.length];
      int[] ifindp = new int[dss.length];
      int[] rangedp = new int[dss.length];
      
      for (int i=0; i<dss.length;i++){
          dpini[i] = dss[i].getClosestDP_xonly(t2i, tol);
          dpfin[i] = dss[i].getClosestDP_xonly(t2f, tol);
          iinidp[i] = dss[i].getIndexOfDP(dpini[i]);
          ifindp[i] = dss[i].getIndexOfDP(dpfin[i]);
          rangedp[i] = ifindp[i] - iinidp[i];
      }

      //check ranges
      int totRange = 0;
      for (int i=0;i<dss.length;i++){
          totRange = totRange + rangedp[i];
      }
      if (totRange/dss.length != rangedp[0]){
          log.debug("inconsitency on nr of points in the coincident range");
          return null;
      }

      
      for (int i=0;i<rangedp[0];i++){
          
          double inten = 0;
          double sdinten = 0;
          for (int j=0; j<dss.length;j++){
              inten = inten + dss[j].getPoint(iinidp[j]+i).getY();
              sdinten = sdinten + dss[j].getPoint(iinidp[j]+i).getSdy();
          }
                    result.addPoint(new DataPoint(dss[0].getPoint(iinidp[0]+i).getX(),inten,sdinten));
      }
      return result;
  }
    
    //Convert DStoConvert to the same step and coinciding points with DSorigin
    //Start/end points (range) may be different if there is no data
    public static DataSerie rebinDS(DataSerie DSorigin, DataSerie DStoConvert){
        
        DataSerie out = new DataSerie(DSorigin,DSorigin.getTipusSerie(),false); //ja l'afegirem al pattern despres si cal
        
        DataPoint ini = DSorigin.getClosestDP_xonly(DStoConvert.getPoint(1).getX(), 50.0);
        DataPoint fin = DSorigin.getClosestDP_xonly(DStoConvert.getPoint(DStoConvert.getNpoints()-2).getX(), 50.0);
        int iini = DSorigin.getIndexOfDP(ini);
        int ifin = DSorigin.getIndexOfDP(fin);
        
        log.writeNameNumPairs("config", true, "iini,ifin", iini,ifin);
        
        for (int i=iini; i<=ifin; i++){
            DataPoint dp = DSorigin.getPoint(i);
            DataPoint[] surr = DStoConvert.getSurroundingDPs(dp.getX());
            if (surr==null)continue;
            //interpolem
            double yinter = DStoConvert.interpolateY(dp.getX(), surr[0], surr[1]);
            double sdyinter = DStoConvert.interpolateSDY(dp.getX(), surr[0], surr[1]);
            out.addPoint(new DataPoint(dp.getX(),yinter,sdyinter));
        }
        
        return out;
    }
    
    
    public static boolean haveSameNrOfPointsDS(DataSerie dp1, DataSerie dp2){
        if (dp1.getNpoints()!=dp2.getNpoints()){
            return false;
        }
        return true;
    }
    
    //coincident points in 2theta, not necessarily the same range
    public static boolean haveCoincidentPointsDS(DataSerie dp1, DataSerie dp2){
        //ha de coincidir l'stepsize i un minim de 100 punts?
        
        double tol = 0.0001;
        int minequals = 100;
        
        double t2i = FastMath.max(dp1.getPoint(0).getX(), dp2.getPoint(0).getX());
        double step1 = dp1.calcStep();
        double step2 = dp2.calcStep();
        
        if (FastMath.abs(step1-step2)>0.01){
            return false;
        }
        
        int iniIndex = dp1.getIndexOfDP(dp1.getClosestDP_xonly(t2i, tol));
        if (iniIndex == -1){
            log.debug("datapoint with t2i not found");
            return false;
        }
        int ncoincidents = 0;
        for (int i=iniIndex; i<dp1.getNpoints(); i++){
            DataPoint p1 = dp1.getPoint(i);
            DataPoint p2 = dp2.getClosestDP_xonly(p1.getX(), tol);
            if (p2!=null){
                if (FastMath.abs(p1.getX()-p2.getX())<(tol/2)){ //NO SE SI CAL TORNAR A COMPROVAR-HO
                    ncoincidents = ncoincidents +1;
                }
            }
            if (ncoincidents >=minequals){
                return true;
            }
        }
        return false;
        
    }
    
    public static double findMax(double... vals) {
        double max = -99999999;

        for (double d : vals) {
            if (d > max) max = d;
        }
        return max;
    }

    public static double findMin(double... vals) {
        double min = 99999999;

        for (double d : vals) {
            if (d < min) min = d;
        }
        return min;
    }
    
    public static double get2thRadFromDsp(double wave,double dsp) {
    	return 2*FastMath.asin(wave/(2*dsp));
    }
    public static double getDspFrom2ThetaRad(double wave,double tthRad) {
    	return (wave/(2*FastMath.sin(tthRad/2)));
    }
    public static float getDspacingFromHKL(int h, int k, int l, float a, float b, float c, float alfaDeg, float betaDeg, float gammaDeg){
        
        double cosal = FastMath.cos(FastMath.toRadians(alfaDeg));
        double cosbe = FastMath.cos(FastMath.toRadians(betaDeg));
        double cosga = FastMath.cos(FastMath.toRadians(gammaDeg));
        double sinal = FastMath.sin(FastMath.toRadians(alfaDeg));
        double sinbe = FastMath.sin(FastMath.toRadians(betaDeg));
        double singa = FastMath.sin(FastMath.toRadians(gammaDeg));
        
        double s11 = b*b*c*c*sinal*sinal;
        double s22 = a*a*c*c*sinbe*sinbe;
        double s33 = a*a*b*b*sinbe*singa;
        double s12 = a*b*c*c*(cosal*cosbe-cosga);
        double s23 = a*a*b*c*(cosbe*cosga-cosal);
        double s13 = a*b*b*c*(cosga*cosal-cosbe);
        
        double insqrt = 1 - cosal*cosal - cosbe*cosbe - cosga*cosga + 2*cosal*cosbe*cosga;
        double vol = a*b*c*(FastMath.sqrt(insqrt)); //Ang3
        
        double fact = s11*h*h + s22*k*k + s33*l*l + 2*s12*h*k + 2*s23*k*l + 2*s13*h*l;
        double invdsp2 = fact*(1/(vol*vol));
        double dsp = FastMath.sqrt(1/invdsp2);
        
        return (float)dsp;
    }
    
    public static void getDspacingFromHKL(ArrayList<PDReflection> refs, float a, float b, float c, float alfaDeg, float betaDeg, float gammaDeg){
        
        double cosal = FastMath.cos(FastMath.toRadians(alfaDeg));
        double cosbe = FastMath.cos(FastMath.toRadians(betaDeg));
        double cosga = FastMath.cos(FastMath.toRadians(gammaDeg));
        double sinal = FastMath.sin(FastMath.toRadians(alfaDeg));
        double sinbe = FastMath.sin(FastMath.toRadians(betaDeg));
        double singa = FastMath.sin(FastMath.toRadians(gammaDeg));
        
        double s11 = b*b*c*c*sinal*sinal;
        double s22 = a*a*c*c*sinbe*sinbe;
        double s33 = a*a*b*b*sinbe*singa;
        double s12 = a*b*c*c*(cosal*cosbe-cosga);
        double s23 = a*a*b*c*(cosbe*cosga-cosal);
        double s13 = a*b*b*c*(cosga*cosal-cosbe);
        
        double insqrt = 1 - cosal*cosal - cosbe*cosbe - cosga*cosga + 2*cosal*cosbe*cosga;
        double vol = a*b*c*(FastMath.sqrt(insqrt)); //Ang3
        
        Iterator<PDReflection> itrr = refs.iterator();
        while (itrr.hasNext()){
            PDReflection p = itrr.next();
            int h = p.getH();
            int k = p.getL();
            int l = p.getL();
            double fact = s11*h*h + s22*k*k + s33*l*l + 2*s12*h*k + 2*s23*k*l + 2*s13*h*l;
            double invdsp2 = fact*(1/(vol*vol));
            p.setDsp((float)FastMath.sqrt(1/invdsp2));
        }
    }
    
    public static DataSerie getPDCompoundAsDspDataSerie(PDCompound pdc) {
		DataSerie ds = new DataSerie(serieType.ref);
		ds.setxUnits(xunits.dsp);
		Iterator<PDReflection> itrPD = pdc.getPeaks().iterator();
		while (itrPD.hasNext()) {
			PDReflection pdr = itrPD.next();
			ds.addPoint(new DataPoint(pdr.getDsp(),pdr.getInten(),0));
		}
		return ds;
    }
    
    //list of 2-theta values and lists of every parameter for combination, ANGLES IN RADIANS
    //tornara ranked list of 50 best solutions?? o algo així? hauriem de crear classe solucio o fer un array de floats
    public static ArrayList<IndexSolutionGrid> indexGridSearchBrute(float[] dobs, float factor, float wave, float[] aL,float[] bL, float[] cL, float[] alL, float[] beL, float[] gaL, int threadN) {
    	int[] lengths = new int[] { aL.length, bL.length, cL.length, alL.length, beL.length, gaL.length };
    	
        int totalComb = aL.length*bL.length*cL.length*alL.length*beL.length*gaL.length;
        int processedComb = 0;
        float fivePercent = (float)totalComb/20.f;
        float onePercent = (float)totalComb/100.f;
        log.info(String.format("Thread #%d: %d combinations of parameters will be evaluated! (%d*%d*%d*%d*%d*%d)", threadN, totalComb,aL.length,bL.length,cL.length,alL.length,beL.length,gaL.length));

    	//llista de solucions TODO
    	IndexGrid iResult = new IndexGrid();

    	long startTime = System.currentTimeMillis();
    	
    	for (int[] indices : new CartesianProduct(lengths)) {
    		float a = aL[indices[0]];
    		float b = bL[indices[1]];
    		float c = cL[indices[2]];
    		float al = alL[indices[3]];
    		float be = beL[indices[4]];
    		float ga = gaL[indices[5]];
    		
//    		log.info(String.format("cell: %f %f %f %f %f %f", a,b,c,al,be,ga));
    		
    		//precalculs
    		float a2 = a*a;
    		float b2 = b*b;
    	    float c2 = c*c;
    	    float sbe = (float) FastMath.sin(be);
    	    float cbe = (float) FastMath.cos(be);
    	    //necessaris per tric;
    	    float sal = (float) FastMath.sin(al);
    	    float cal = (float) FastMath.cos(al);
    	    float sga = (float) FastMath.sin(ga);
    	    float cga = (float) FastMath.cos(ga);
    	    	    
    	    float paramResidual=0; //residual pels paràmetres actuals
    	    
//    	    indexSolucio is = iResult.new indexSolucio(a, b, c, al, be, ga, paramResidual);
    	    
    		//ara hkl
            int hmax = (int) (factor*a/3); //TODO: he afegit el /2 i he tret el +1 a hklmax
            int kmax = (int) (factor*b/3);
            int lmax = (int) (factor*c/3);
            int[] hs = FileUtils.range(-hmax, hmax, 1);
            int[] ks = FileUtils.range(-kmax, kmax, 1);
            int[] ls = FileUtils.range(-lmax, lmax, 1);
            
            int[] lenHKL = new int[] { hs.length, ks.length, ls.length };
            
            float[] dcalcs = new float[hs.length*hs.length*ls.length];
            
            int ihkl = 0;
            
            for (int[] indexHKL : new CartesianProduct(lenHKL)) {
            	int h = hs[indexHKL[0]];
            	int k = ks[indexHKL[1]];
            	int l = ls[indexHKL[2]];
            	
            	if ((h==0)&&(k==0)&&(l==0))continue;
            	
            	//TODO:intentar fer minhkl i maxhkl "dinamic" a partir limit dsp. CAL FER ARRAYLIST DCALCS PERQUE SIGUI DINAMIC. ALESHORES COMPROVAREM DSP CADA COP I EN CONSEQUIENCIA SEGUIREM O NO (segons dmin definit). GUARDEM MAX I MIN H K L en variable per posar-ho despres a la solucio
            	
                //monoclinic
//                float hsqcalc = (1/(sbe*sbe))*((h*h)/a2 + (k*k*sbe*sbe)/b2 + (l*l)/c2 - (2*h*l*cbe)/(a*c));
//                float dcalc = (float) FastMath.sqrt(1/hsqcalc);
                
                //triclinic
                float hsqcalc = b2*c2*sal*sal*h*h + a2*c2*sbe*sbe*k*k + a2*b2*sga*sga*l*l + 2*a*b*c2*(cal*cbe-cga)*h*k + 2*a2*b*c*(cbe*cga-cal)*k*l + 2*a*b2*c*(cga*cal-cbe)*h*l;
                float v2 = (float) (a*b*c*FastMath.sqrt(1-cal*cal-cbe*cbe-cga*cga+2*cal*cbe*cga));
                v2 = v2*v2;
                hsqcalc = hsqcalc/v2;
                float dcalc = (float) FastMath.sqrt(1/hsqcalc);
                
                //ara he de mirar la diferència menor amb tots els dobs entrats
                //TODO: optimitzar, llista dcalcs i llista dobs... segur que es pot fer millor.
                
                //NOOOOOOOOOOOOOOOOOOOOO, està malament. No hem de fer per cada dcalc mirar la dobs més propera SINO al revés, quan totes les dcalc estan calculades mirar PER CADA dobs la dcalc més propera (i potser ignorar si hi ha alguna molt llunyana... TODO) 
//                float mindiff = Float.MAX_VALUE;
//                for (int i=0; i<dobs.length;i++) {
//                	float diff = FastMath.abs(dcalc-dobs[i]);
//                	if (diff<mindiff)mindiff=diff;
//                }
//                paramResidual = paramResidual + mindiff; //afegim al residual
                
                dcalcs[ihkl] = dcalc;
                
                ihkl++;
                
            }
            
            //calculem el residual (per cada dobs comprovem les dcalc)
            for (int i=0; i<dobs.length;i++) {
            	float mindiff = Float.MAX_VALUE;
            	float dvaluemindif=0f;
            	for (int j=0; j<dcalcs.length; j++) {
                	float diff = FastMath.abs(dcalcs[j]-dobs[i]);
                	if (diff<mindiff) {
                		mindiff=diff;
                		dvaluemindif=dcalcs[j];
                	}
            	}
//            	log.info(String.format("dobs %.4f dcal %.4f", dobs[i],dvaluemindif));
            	paramResidual = paramResidual + mindiff;
            }
            
            //ara ja tenim el residual per aquest joc de paràmetres, l'afegim com a solució
            IndexSolutionGrid is = new IndexSolutionGrid(a, b, c, al, be, ga, paramResidual);
            is.sethklmaxmin(-hmax, hmax, -kmax, kmax, -lmax, lmax);
            iResult.addSolucio(is);
//            iResult.addSolucio(a, b, c, al, be, ga, paramResidual);
//            is.res=paramResidual;
//            iResult.addSolucio(is);
            
            //temps
            processedComb++;
            if (processedComb%onePercent==0) {
            	long elapTime = (System.currentTimeMillis() - startTime)/1000;
    			float percent = ((float)processedComb/(float)totalComb)*100;
    			float estTime = (((100-percent)*elapTime)/percent)/60; //en minuts
    			log.info(String.format(" %6.2f %% (est. time %6.2f min.)",percent,estTime));
            }
    	}
    	
    	//finito, imprimim 50 millors solucions
//    	float[][] sols = iResult.get50bestSolutions();
//    	for (int i=0;i<sols.length;i++) {
//    		log.info(String.format("sol %d: %8.4f %8.4f %8.4f %6.2f %6.2f %6.2f res=%8.4f", i+1, sols[i][0],sols[i][1],sols[i][2],sols[i][3],sols[i][4],sols[i][5],sols[i][6]));
//    	}
    	
    	ArrayList<IndexSolutionGrid> sols = iResult.get50bestSolutions();
    	return sols;
    }
    
    public static float dspFromHKL(float a, float b, float c, float al, float be, float ga, int h, int k, int l) {
		float a2 = a*a;
		float b2 = b*b;
	    float c2 = c*c;
	    float sbe = (float) FastMath.sin(be);
	    float cbe = (float) FastMath.cos(be);
	    //necessaris per tric;
	    float sal = (float) FastMath.sin(al);
	    float cal = (float) FastMath.cos(al);
	    float sga = (float) FastMath.sin(ga);
	    float cga = (float) FastMath.cos(ga);
	    
        float hsqcalc = b2*c2*sal*sal*h*h + a2*c2*sbe*sbe*k*k + a2*b2*sga*sga*l*l + 2*a*b*c2*(cal*cbe-cga)*h*k + 2*a2*b*c*(cbe*cga-cal)*k*l + 2*a*b2*c*(cga*cal-cbe)*h*l;
        float v2 = (float) (a*b*c*FastMath.sqrt(1-cal*cal-cbe*cbe-cga*cga+2*cal*cbe*cga));
        v2 = v2*v2;
        hsqcalc = hsqcalc/v2;
        float dcalc = (float) FastMath.sqrt(1/hsqcalc);
        return dcalc;
        
//        float hsqcalc = (1/(sbe*sbe))*((h*h)/a2 + (k*k*sbe*sbe)/b2 + (l*l)/c2 - (2*h*l*cbe)/(a*c));
//        float dcalc = (float) FastMath.sqrt(1/hsqcalc);
//        return dcalc;
    }
    

    
}

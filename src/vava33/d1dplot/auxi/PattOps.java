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

import javax.swing.table.DefaultTableModel;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.util.FastMath;

import com.vava33.d1dplot.D1Dplot_global;
import com.vava33.d1dplot.data.DataPoint;
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.d1dplot.data.Plottable_point;
import com.vava33.d1dplot.data.SerieType;
import com.vava33.jutils.FileUtils;
import com.vava33.jutils.VavaLogger;



public final class PattOps {
    
    
    private static final String className = "PattOps";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);
    
    private final static float percent_auto_factor=0.999f;//this will be multiplied to the minimum factor to give some margin

    
    private static DataSerie bkg_Bruchner_firstPass(DataSerie ds,double t2ini){  //no depen de N
        DataSerie ds0 = new DataSerie(ds,SerieType.bkg,false);
        double[] vals = ds.calcYmeanYDesvYmaxYmin(false); 
        double Imean = vals[0];
        double Imin = vals[3];
        
        log.debug("Imean= "+Imean+" Imin= "+Imin);
        
        //ara corregim els punts
        for(int i=0; i<ds.getNpoints(); i++){
            double x = ds.getPointWithCorrections(i,false).getX();
            if (x<t2ini)continue;
            double y = ds.getPointWithCorrections(i,false).getY();
            
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
        DataSerie ds1 = new DataSerie(ds0,SerieType.bkg,false);

        for(int p=0;p<niter;p++){
            ds1 = new DataSerie(ds0,SerieType.bkg,false);
            
            for(int i=0; i<ds0.getNpoints(); i++){
                //rempla�arem cada punt i del diagrama per un de fons, que es la mitja dels +-N veins
                
                if(ds0.getPointWithCorrections(i,false).getX()<t2ini)continue;
                
                //en cas que tinguem N variable:
                if(multi){
                    double t2punt= ds0.getPointWithCorrections(i,false).getX();
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
                            sumI=sumI+ds0.getPointWithCorrections(0,false).getY();
                        }else{
                            sumI=sumI+ds0.getPointWithCorrections(0,false).getY()+(ds0.getPointWithCorrections(0,false).getY()-ds0.getPointWithCorrections(-j,false).getY());
                        }
                        continue;

                    }
                    //AQUI FALTA IMPLEMENTAR LA COMPLEMENTARIA INVERTIDA TAMB�
                    if(j>=ds0.getNpoints()-1){
                        //agafem intensitat de l'ultim punt
                        sumI=sumI+ds0.getPointWithCorrections(ds0.getNpoints()-1,false).getY();
                        continue;
                    }
                    if(j==i){
                        //el propi punt no el considerem
                        continue;
                    }
                    //cas punt "centre" diagrama
                    sumI=sumI+ds0.getPointWithCorrections(j,false).getY();
                }

               //CANVI 130313: Comparem Ynew amb diagrama original (Patt[0]) i ens quedem amb la intensitat m�s petita
                double Ynew=sumI/(2*nveins);
                if(Ynew<ds0.getPointWithCorrections(i,false).getY()){
                    //agafem el nou
                    ds1.addPoint(new DataPoint(ds0.getPointWithCorrections(i,false).getX(),Ynew,0));
                }else{
                    //ens quedem l'Yobs original
                    ds1.addPoint(new DataPoint(ds0.getPointWithCorrections(i,false).getX(),ds0.getPointWithCorrections(i,false).getY(),0));
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
        DataSerie dpPuntsFons = new DataSerie(ds,SerieType.bkgEstimP,false);
        int startP = 0;
        while ((startP+npzona)<ds.getNpoints()){
            double[] meanYzonaYdesv = ds.calcYmeanYDesvYmaxYmin(startP, startP+npzona,false);
            Plottable_point dp = ds.getMinYDataPoint(startP, startP+npzona,false);
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
           
           //TODO ordenar punts fons per ordre creixent
           
           for (int i=0;i<puntsFons.getNpoints();i++){
               x[i]=puntsFons.getPointWithCorrections(i,false).getX();
               y[i]=puntsFons.getPointWithCorrections(i,false).getY();
           }
           
           UnivariateInterpolator in = new SplineInterpolator();
           UnivariateFunction f = in.interpolate(x, y);

           DataSerie fons = new DataSerie(ds,SerieType.bkg,false);
           

           double ini2t = puntsFons.getPointWithCorrections(0,false).getX();
           Plottable_point close = ds.getClosestDP(new DataPoint(ini2t,puntsFons.getPointWithCorrections(0,false).getY(),0), 0.1, 50,false);
           int index = ds.getIndexOfDP(close);
           double curr2t = ds.getPointWithCorrections(index,false).getX();
           while (curr2t<puntsFons.getPointWithCorrections(puntsFons.getNpoints()-1,false).getX()){
               fons.addPoint(new DataPoint(curr2t,f.value(curr2t),0));
               index = index +1;
               curr2t = ds.getPointWithCorrections(index,false).getX();
           }

           return fons;
       }
    
    
    public static DataSerie bkg_FitPoly(DataSerie ds,DataSerie puntsFons,int degree) {
        // Collect data.
           // Instantiate a n-degree polynomial fitter.
           final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(degree);

           final WeightedObservedPoints obs2 = new WeightedObservedPoints();
           
           for (int i=0;i<puntsFons.getNpoints();i++){
               obs2.add(puntsFons.getPointWithCorrections(i,false).getX(),puntsFons.getPointWithCorrections(i,false).getY());
           }
           final double[] coeff = fitter.fit(obs2.toList());

           log.writeFloats("config", coeff);
           
           final PolynomialFunction f = new PolynomialFunction(coeff);

           DataSerie fons = new DataSerie(ds,SerieType.bkg,false);
           
           //pintem el fons polinomicalculat
           double ini2t = puntsFons.getPointWithCorrections(0,false).getX();
           Plottable_point close = ds.getClosestDP(new DataPoint(ini2t,puntsFons.getPointWithCorrections(0,false).getY(),0), 0.1, 50,false);
           int index = ds.getIndexOfDP(close);
           double curr2t = ds.getPointWithCorrections(index,false).getX();
           while (curr2t<puntsFons.getPointWithCorrections(puntsFons.getNpoints()-1,false).getX()){
               fons.addPoint(new DataPoint(curr2t,f.value(curr2t),0));
               index = index +1;
               curr2t = ds.getPointWithCorrections(index,false).getX();
           }
           return fons;
       }
    
    //it returns the coincident zone only
    // double t2i,t2f for factor calculation in case factor < 1
    public static DataSerie subtractDataSeriesCoincidentPoints(DataSerie ds1, DataSerie ds2, float factor, double fac_t2i, double fac_t2f){
        int tol =30;
        
        
        DataSerie result = new DataSerie(ds1,ds1.getTipusSerie(),false);
        double t2i = FastMath.max(ds1.getPointWithCorrections(0,false).getX(), ds2.getPointWithCorrections(0,false).getX());
        double t2f = FastMath.min(ds1.getPointWithCorrections(ds1.getNpoints()-1,false).getX(), ds2.getPointWithCorrections(ds2.getNpoints()-1,false).getX());    
        
        
        Plottable_point dp1ini = ds1.getClosestDP_xonly(t2i, tol);
        Plottable_point dp1fin = ds1.getClosestDP_xonly(t2f, tol);
        
        int iinidp1 = ds1.getIndexOfDP(dp1ini);
        int ifindp1 = ds1.getIndexOfDP(dp1fin);
        
        Plottable_point dp2ini = ds2.getClosestDP_xonly(t2i, tol);
        Plottable_point dp2fin = ds2.getClosestDP_xonly(t2f, tol);
        
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
            result.addPoint(new DataPoint(ds1.getPointWithCorrections(iinidp1+i,false).getX(),ds1.getPointWithCorrections(iinidp1+i,false).getY()-factor*ds2.getPointWithCorrections(iinidp2+i,false).getY(),ds1.getPointWithCorrections(iinidp1+i,false).getSdy()+factor*ds2.getPointWithCorrections(iinidp2+i,false).getSdy()));
        }
        result.setScale(factor);
        return result;
    }
    
    // get the factor such as all the intensities are (y1 - F*y2 >= 0), in a given range (rangedp) starting each serie in a specific point (iinidpX)
    public static float getScaleFactor(DataSerie ds1, DataSerie ds2, int iinidp1, int iinidp2, int rangedp, double fac_t2i, double fac_t2f) {
        float factor = 100;
        for (int i=0;i<rangedp;i++){ //TODO HAURIA DE SER <=  (Comprovar-ho)
            double x1 = ds1.getPointWithCorrections(iinidp1+i,false).getX();
            if (x1<fac_t2i)continue;
            if (x1>fac_t2f)break;
            double y1 = ds1.getPointWithCorrections(iinidp1+i,false).getY();
            double y2 = ds2.getPointWithCorrections(iinidp2+i,false).getY();
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
          t2is[i]=dss[i].getPointWithCorrections(0,false).getX();
          t2fs[i]=dss[i].getPointWithCorrections(dss[i].getNpoints()-1,false).getX();
      }
      double t2i = findMax(t2is);
      double t2f = findMin(t2fs);
      
      Plottable_point[] dpini = new DataPoint[dss.length];
      Plottable_point[] dpfin = new DataPoint[dss.length];
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
              inten = inten + dss[j].getPointWithCorrections(iinidp[j]+i,false).getY();
              sdinten = sdinten + dss[j].getPointWithCorrections(iinidp[j]+i,false).getSdy();
          }
                    result.addPoint(new DataPoint(dss[0].getPointWithCorrections(iinidp[0]+i,false).getX(),inten,sdinten));
      }
      return result;
  }
    
    //Convert DStoConvert to the same step and coinciding points with DSorigin
    //Start/end points (range) may be different if there is no data
    public static DataSerie rebinDS(DataSerie DSorigin, DataSerie DStoConvert){
        
        DataSerie out = new DataSerie(DSorigin,DSorigin.getTipusSerie(),false); //ja l'afegirem al pattern despres si cal
        
        Plottable_point ini = DSorigin.getClosestDP_xonly(DStoConvert.getPointWithCorrections(1,false).getX(), 50.0);
        Plottable_point fin = DSorigin.getClosestDP_xonly(DStoConvert.getPointWithCorrections(DStoConvert.getNpoints()-2,false).getX(), 50.0);
        int iini = DSorigin.getIndexOfDP(ini);
        int ifin = DSorigin.getIndexOfDP(fin);
        
        log.writeNameNumPairs("config", true, "iini,ifin", iini,ifin);
        
        for (int i=iini; i<=ifin; i++){
            Plottable_point dp = DSorigin.getPointWithCorrections(i,false);
            Plottable_point[] surr = DStoConvert.getSurroundingDPs(dp.getX());
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
        
        double t2i = FastMath.max(dp1.getPointWithCorrections(0,false).getX(), dp2.getPointWithCorrections(0,false).getX());
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
            Plottable_point p1 = dp1.getPointWithCorrections(i,false);
            Plottable_point p2 = dp2.getClosestDP_xonly(p1.getX(), tol);
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
    
    public static boolean askForWavelengthAndAddToDS(DataSerie ds) {
        double wave = FileUtils.DialogAskForPositiveDouble(D1Dplot_global.getD1DmainFrame(),"Wavelength (Ang) =","Wavelength required to perform current operation", ""); 
        if (wave<=0)return false;
        ds.setWavelength(wave);
        return true;
    }
    
    public static DataSerie findPeaksGetBkgLlindar(DataSerie data) {
        return PattOps.bkg_Bruchner(data, 20, 50, true, data.getMinX()-1, false, null);
    }
    
    //RETORNA DATASERIE PEAKS
    public static DataSerie findPeaksEvenBetter(DataSerie data, DataSerie bkgLlindar, float delsig,double minX, double maxX, boolean takeIntoAccountYBkg){
        //interpolar mirar quin està mes avall dels dos del costat i interpolar el valor de y elevat a la recta centre-inferior. alsehores buscar el mig de x
        double desv = 1;
        
        if((minX>maxX)||(minX<0&&maxX<0)){
            minX=data.getMinX();
            maxX=data.getMaxX();
        }
         
        if (bkgLlindar == null)desv = data.calcYmeanYDesvYmaxYmin(takeIntoAccountYBkg)[1];    

        DataSerie peaks = new DataSerie(SerieType.peaks,data.getxUnits(),data.getParent());
        
        //limits menys 1
        for (int i=1;i<data.getNpoints()-1;i++){
            if (data.getPointWithCorrections(i,false).getX()<minX)continue; //rang on buscar pics
            if (data.getPointWithCorrections(i,false).getX()>maxX)continue;
            double ycent = data.getPointWithCorrections(i,false).getY();
            if (bkgLlindar!=null){
                if (ycent < bkgLlindar.getPointWithCorrections(i,false).getY()*delsig)continue;
            }else{
                if (ycent < desv*delsig)continue;    
            }
            
            double yleft = data.getPointWithCorrections(i-1,false).getY();
            double yright = data.getPointWithCorrections(i+1,false).getY();
            double xcent = data.getPointWithCorrections(i,false).getX();
            double xleft = data.getPointWithCorrections(i-1,false).getX();
            double xright = data.getPointWithCorrections(i+1,false).getX();
            if (ycent>yleft && ycent>yright){
                
                double xpeak = 0;
                //ara interpolo
                if (yleft>yright){
                    //fem recta centre-right i interpolem el valor de yleft a aquesta
                    double pen = (yright-ycent)/(xright-xcent);
                    double ord = pen*(xcent)*-1+ycent;
                    //interpolem yleft
                    double xinter = (yleft-ord)/pen;
                    //ara busquem el centre
                    xpeak = (xinter+xleft)/2.;
                }else{
                    //al reves, recte centre-left i interpolem yright
                    double pen = (yleft-ycent)/(xleft-xcent);
                    double ord = pen*(xcent)*-1+ycent;
                    //interpolem yright
                    double xinter = (yright-ord)/pen;
                    xpeak = (xinter+xright)/2.;
                }

                peaks.addPoint(new DataPoint(xpeak,ycent,data.getPointWithCorrections(i,false).getSdy()));
            }
        }
        return peaks;
    }

    
}

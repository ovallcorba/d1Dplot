package com.vava33.d1dplot.auxi;

/**
 * D1Dplot
 * 
 * pseudoVoigt peak fitting class
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.util.Arrays;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.util.FastMath;

import com.vava33.d1dplot.D1Dplot_global;
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.jutils.VavaLogger;


public class PseudoVoigt {

    private static final String className = "PseudoVoigt";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);
    
    double eta,mean; //fraction of gauss/lor
    double fwhm;
    double bkgI;
    double bkgP;
    double ymax;
    Gaussian gaus;
    Lorentzian lor;

    public PseudoVoigt(double mean, double fwhm, double eta,double ymax,double bkgI,double bkgP) {
        this.gaus=new Gaussian(mean,fwhm);
        this.lor=new Lorentzian(mean,fwhm);
        this.eta=eta;
        this.fwhm=fwhm;
        this.mean=mean;
        this.bkgI=bkgI;
        this.bkgP=bkgP;
        this.ymax=ymax;
    }
    
    //actualitza els parametres a partir d'una altra PV
    public PseudoVoigt(PseudoVoigt pvToCopyParametersFrom) {
        this.mean=pvToCopyParametersFrom.mean;
        this.fwhm=pvToCopyParametersFrom.fwhm;
        this.eta=pvToCopyParametersFrom.eta;
        this.ymax=pvToCopyParametersFrom.ymax;
        this.bkgI=pvToCopyParametersFrom.bkgI;
        this.bkgP=pvToCopyParametersFrom.bkgP;
        this.gaus=new Gaussian(this.mean,this.fwhm);
        this.lor=new Lorentzian(this.mean,this.fwhm);
    }
    
    public double eval(double x, boolean addbkg) {
         double pv = eta*this.lor.eval(x)+(1-eta)*this.gaus.eval(x);
         if(addbkg) {
             return this.ymax*pv + this.bkgP*(x-this.mean)+this.bkgI;    
         }else {
             return this.ymax*pv;
         }
         
    }
    
    public double getBkgValue(double x) {
        return this.bkgP*(x-this.mean)+this.bkgI;
    }
    
    public double[] getPars() {
        return new double[] {mean,fwhm,eta,ymax,bkgI,bkgP};
    }
    
    public void fit(final DataSerie obsdata) {
        
        MultivariateFunction function = new MultivariateFunction() {
            @Override
            public double value(double[] pars) {
                double res = 0;
                for (int i=0; i<obsdata.getNpoints();i++) {
                    PseudoVoigt pv = new PseudoVoigt(pars[0],pars[1],pars[2],pars[3],pars[4],pars[5]);
                    double ycal = pv.eval(obsdata.getPointWithCorrections(i, false).getX(),true);
                    res = res + FastMath.abs(ycal-obsdata.getPointWithCorrections(i,false).getY());
                }
                return res;
            }
        };
        double[] pars = new double[] {this.mean, this.fwhm,this.eta,this.ymax,this.bkgI,this.bkgP};
        double[] inc = new double[] {0.01,0.01,0.05,10.00,10.0,0.05};
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-5, 1e-10);
        PointValuePair optimum = optimizer.optimize(
                new MaxEval(10000), 
                new ObjectiveFunction(function), 
                GoalType.MINIMIZE, 
                new InitialGuess(pars), 
                new NelderMeadSimplex(inc));//,1,2,0.5,0.5
//                new MultiDirectionalSimplex(inc));

        log.debug("opt sol="+Arrays.toString(optimum.getPoint()) + " : " + optimum.getSecond());
        
        this.mean=optimum.getPoint()[0];
        this.fwhm=optimum.getPoint()[1];
        this.eta=optimum.getPoint()[2];
        this.ymax=optimum.getPoint()[3];
        this.bkgI=optimum.getPoint()[4];
        this.bkgP=optimum.getPoint()[5];
        this.gaus=new Gaussian(this.mean,this.fwhm);
        this.lor=new Lorentzian(this.mean,this.fwhm);
        
    }

    
    //agafa els parametres d'aquesta pseudovoigt pero ajusta a varis valors de mean i ymax
    //retorna els valors mean i ymax actualitzats (i s'actualitza la pseudovoigt amb els nous parametres) --> array continu
    public double[] fitMultipleMeans(final DataSerie obsdata, final double[] means, final double[] ymaxs) {
        
        MultivariateFunction function = new MultivariateFunction() {
            @Override
            public double value(double[] pars) {
                double res = 0;
                for (int i=0; i<obsdata.getNpoints();i++) {
                    
                    //calculem la contribució al punt de totes les PVs
                    double ycal = 0;
                    for (int j=0;j<means.length;j++) {
                        PseudoVoigt pv = new PseudoVoigt(pars[j+4],pars[0],pars[1],pars[j+4+means.length],pars[2],pars[3]);
                        ycal = ycal + pv.eval(obsdata.getPointWithCorrections(i, false).getX(),true);   //REALMENT PODRIA FER EVAL SENSE FONS I FER EL FONS GLOBAL AL FINAL --> SERIA MOLT MILLOR
                    }
                    
                    res = res + FastMath.abs(ycal-obsdata.getPointWithCorrections(i,false).getY());
                }
                return res;
            }
        };
        double[] pars = new double[means.length+ymaxs.length+4];
        double[] inc = new double[means.length+ymaxs.length+4];
        
        pars[0]=this.fwhm;
        pars[1]=this.eta;
//        pars[2]=this.ymax;
        pars[2]=this.bkgI;
        pars[3]=this.bkgP;
        inc[0]=0.001;
        inc[1]=0.01;
//        inc[2]=10;
        inc[2]=1;
        inc[3]=0.1;
        for (int i=0;i<(means.length);i++) {
            pars[i+4]=means[i];
            inc[i+4]=0.00005;
        }
        for (int i=0;i<(ymaxs.length);i++) {
            pars[i+4+means.length]=ymaxs[i];
            inc[i+4+means.length]=1;
        }
        
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-5, 1e-10);
        PointValuePair optimum = optimizer.optimize(
                new MaxEval(10000), 
                new ObjectiveFunction(function), 
                GoalType.MINIMIZE, 
                new InitialGuess(pars), 
                new NelderMeadSimplex(inc));//,1,2,0.5,0.5
//                new MultiDirectionalSimplex(inc));

        log.debug("opt sol="+Arrays.toString(optimum.getPoint()) + " : " + optimum.getSecond());
        
        this.fwhm=optimum.getPoint()[0];
        this.eta=optimum.getPoint()[1];
//        this.ymax=optimum.getPoint()[2];
        this.bkgI=optimum.getPoint()[2];
        this.bkgP=optimum.getPoint()[3];
//        this.mean=optimum.getPoint()[5]; //TODO no se si posar-ho aqui
//        this.gaus=new Gaussian(this.mean,this.fwhm);
//        this.lor=new Lorentzian(this.mean,this.fwhm);
        
        //ara preparem retornar els altres means
        double[] optmeans = new double[means.length+ymaxs.length];
        for (int i=0;i<means.length;i++) { //ATENCIO l'actual també es posa!
            optmeans[i]=optimum.getPoint()[i+4];
        }
        for (int i=0;i<ymaxs.length;i++) { //ATENCIO l'actual també es posa!
            optmeans[i+means.length]=optimum.getPoint()[i+means.length+4];
        }
        return optmeans;
    }
    
//    public void updateFunctions(double mean, double fwhm) {
//        this.mean=mean;
//        this.fwhm=fwhm;
//        this.gaus=new Gaussian(mean,fwhm);
//        this.lor=new Lorentzian(mean,fwhm);
//    }
    
    public void updateFunctions(double mean, double ymax) {
        this.mean=mean;
        this.ymax=ymax;
        this.gaus=new Gaussian(mean,this.fwhm);
        this.lor=new Lorentzian(mean,this.fwhm);
    }
    
    //actualitza els parametres a partir d'una altra PV
    public void set_fwhm_eta_bkg_from_other_PV(PseudoVoigt pvToCopyParametersFrom) {
        this.fwhm=pvToCopyParametersFrom.fwhm;
        this.eta=pvToCopyParametersFrom.eta;
        this.bkgI=pvToCopyParametersFrom.bkgI;
        this.bkgP=pvToCopyParametersFrom.bkgP;
    }
    
    public class Gaussian{
        double mean, fwhm;
        
        public Gaussian(double mean, double fwhm) {
            this.mean=mean;
            this.fwhm=fwhm;
        }
        
        public double eval(double x) {
            //2.7726 is 4*ln(2)
            //1.66 es l'arrel de lo de sobre
            //1.77 arrel de pi
            //0.939437278 es el quocient entre els dos anteriors
            return (0.939437278)*FastMath.exp((-2.7726*(x-mean)*(x-mean))/(fwhm*fwhm));
        }
    }
    
    public class Lorentzian{
        double mean, fwhm;
        
        public Lorentzian(double mean, double fwhm) {
            this.mean=mean;
            this.fwhm=fwhm;
        }
        
        public double eval(double x) {
            return 2./FastMath.PI/(1+4*(x-mean)*(x-mean)/fwhm/fwhm);
        }
    }

    @Override
    public String toString() {
        return String.format("PV: mean=%.4f fwhm=%.4f eta=%.4f ymax=%.4f bkgI=%.4f bkgP=%.4f", mean,fwhm,eta,ymax,bkgI,bkgP);
    }
    
    
    
}

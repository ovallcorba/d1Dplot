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
    
    public double eval(double x) {
         double pv = eta*this.lor.eval(x)+(1-eta)*this.gaus.eval(x);
         return this.ymax*pv + this.bkgP*(x-this.mean)+this.bkgI;
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
                    double ycal = pv.eval(obsdata.getPointWithCorrections(i, false).getX());
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
            return (1.6651126/1.7724539)*FastMath.exp((-1*2.7726*(x-mean)*(x-mean))/(fwhm*fwhm));
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

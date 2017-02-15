package vava33.d1dplot.auxi;

/**
 * D1Dplot
 * 
 * Data Serie to plot
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;

//import org.apache.commons.math3.util.FastMath;



import vava33.d1dplot.D1Dplot_global;

import com.vava33.jutils.FastMath;
import com.vava33.jutils.VavaLogger;

public class DataSerie {

    public enum serieType {
        dat, obs, cal, hkl, diff, bkg
    }
    
    public enum xunits {
        tth("2Theta"), dsp("d-spacing"), dspInv("1/dsp"), Q("Q"); //Q is 4pi(sinT)/lambda
        private String name;
        private xunits(String s){
            this.name=s;
        }
        public String getName(){
            return this.name;
        }
        public xunits getEnum(String n){
            if (this.getName()==n)return this;
            return null;
        }
    }
    
    private ArrayList<DataPoint> seriePoints;
    private ArrayList<DataPoint> peaks;
    
    private double wavelength = -1;
    private float markerSize = 3;
    private float lineWidth = 1;
    private boolean showErrBars = false;
    private boolean plotThis;
    private float scale = 1.0f;
    private double zerrOff = 0.0f;
    private double YOff = 0.0f;
    private double t2i,t2f,step;
    private Color color;
    private Pattern1D Patt1D;
    private xunits xUnits;
    private serieType tipusSerie;
    private static VavaLogger log = D1Dplot_global.getVavaLogger(DataSerie.class.getName());

    //prf exclusive
    private ArrayList<DataHKL> serieHKL;
    
    //empty dataserie
    public DataSerie(){
        this.setSeriePoints(new ArrayList<DataPoint>());
        this.setSerieHKL(new ArrayList<DataHKL>());
        this.setSeriePeaks(new ArrayList<DataPoint>());
        this.setPlotThis(true);
        this.setPatt1D(null);
        this.setT2i(-1f);
        this.setT2f(-1f);
        this.setStep(-1f);
        this.setWavelength(-1);
        this.setScale(1);
        this.setZerrOff(0.0f);
        this.setYOff(0.0f);
        this.setxUnits(xunits.tth);
        this.setTipusSerie(serieType.dat);
    }
    
    //copia tots els parametres excepte les dades
    public DataSerie(DataSerie inds){
        this();
        this.setPatt1D(inds.getPatt1D());
        this.setT2i(inds.getT2i());
        this.setT2f(inds.getT2f());
        this.setStep(inds.getStep());
        this.setWavelength(inds.getStep());
        this.setScale(inds.getScale());
        this.setZerrOff(inds.getZerrOff());
        this.setYOff(inds.getYOff());
        this.setxUnits(inds.getxUnits());
        this.setTipusSerie(inds.getTipusSerie());
    }
    
    public DataSerie(ArrayList<DataPoint> punts, Pattern1D patt, double t2i, double t2f, double step, double wavel){
        this();
        this.setSeriePoints(punts);
        this.setPatt1D(patt);
        this.setT2i(t2i);
        this.setT2f(t2f);
        this.setStep(step);
        this.setWavelength(wavel);
    }
    
    public DataSerie(ArrayList<DataHKL> punts, Pattern1D patt, double wavel){
        this();
        this.setSerieHKL(punts);
        this.setPatt1D(patt);
        this.setWavelength(wavel);
    }

    public DataPoint getPoint(int arrayPosition){
        DataPoint dp = this.seriePoints.get(arrayPosition);
        //TODO: CONSIDERAR UNITATS DSP,Q,1/D,2THETA
        DataPoint ndp = new DataPoint(dp.getX()+this.zerrOff,dp.getY()*this.scale+this.getYOff(),dp.getSdy()*this.scale,dp.getyBkg()*this.scale);
        if (Pattern1D.isPlotwithbkg()){
            ndp.setY(ndp.getY()+(ndp.getyBkg()*this.scale));
        }
        return ndp; 
    }
    
    public DataPoint getPeak(int arrayPosition){
        DataPoint dp = this.peaks.get(arrayPosition);
        DataPoint ndp = new DataPoint(dp.getX()+this.zerrOff,dp.getY()*this.scale+this.getYOff(),dp.getSdy()*this.scale,dp.getyBkg()*this.scale);
        if (Pattern1D.isPlotwithbkg()){
            ndp.setY(ndp.getY()+(ndp.getyBkg()*this.scale));
        }
        return ndp; 
    }
    
    public DataHKL getHKLPoint(int arrayPosition){
        DataHKL dhkl = this.serieHKL.get(arrayPosition);
        return new DataHKL(dhkl.getH(),dhkl.getK(),dhkl.getL(),dhkl.getTth()+this.zerrOff);
    }
    
    public void addPoint(DataPoint dp){
        this.seriePoints.add(dp);
    }

    public void addHKLPoint(DataHKL dhkl){
        this.serieHKL.add(dhkl);
    }
    
    //pels dos casos
    public int getNpoints(){
        if (getTipusSerie()==serieType.hkl){
            return serieHKL.size();
        }else{
            return seriePoints.size();            
        }
    }

    public int getNpeaks(){
        return peaks.size();
    }

    private void setSeriePoints(ArrayList<DataPoint> seriePoints) {
        this.seriePoints = seriePoints;
    }

    private void setSerieHKL(ArrayList<DataHKL> seriehkl) {
        this.serieHKL = seriehkl;
    }
    
    private void setSeriePeaks(ArrayList<DataPoint> seriePeaks) {
        this.peaks = seriePeaks;
    }
    
    public float getMarkerSize() {
        return markerSize;
    }

    public void setMarkerSize(float markerSize) {
        this.markerSize = markerSize;
    }
    
    public double[] getPuntsMaxXMinXMaxYMinY(){
        if (seriePoints!=null){
            double minX = Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE;
            double maxY = Double.MIN_VALUE;
            for (int i=0;i<seriePoints.size();i++){
                DataPoint punt = this.getPoint(i);
                if (punt.getX() < minX){
                    minX = punt.getX();
                }
                if (punt.getX() > maxX){
                    maxX = punt.getX();
                }
                if (punt.getY() < minY){
                    minY = punt.getY();
                }
                if (punt.getY() > maxY){
                    maxY = punt.getY();
                }
            }
            return new double[]{maxX,minX,maxY,minY};
        }else{
            return null;
        }
    }
    
    public boolean isPlotThis() {
        return plotThis;
    }

    public void setPlotThis(boolean plotThis) {
        this.plotThis = plotThis;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    public boolean isShowErrBars() {
        return showErrBars;
    }

    public void setShowErrBars(boolean showErrBars) {
        this.showErrBars = showErrBars;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public double getZerrOff() {
        return zerrOff;
    }

    public void setZerrOff(double zerrOff) {
        this.zerrOff = zerrOff;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Pattern1D getPatt1D() {
        return Patt1D;
    }

    public void setPatt1D(Pattern1D patt1d) {
        Patt1D = patt1d;
        if (patt1d!=null){
            if (Patt1D.getOriginal_wavelength()<0){
                Patt1D.setOriginal_wavelength(wavelength);
            }
        }
    }
    
    public String toString(){
        return this.getPatt1D().getFile().getName();
    }

    public double getStep() {
        return step;
    }

    public void setStep(double step) {
        this.step = step;
    }

    public double getT2f() {
        return t2f+this.zerrOff;
    }

    public void setT2f(double t2f) {
        this.t2f = t2f;
    }

    public double getT2i() {
        return t2i+this.zerrOff;
    }

    public void setT2i(double t2i) {
        this.t2i = t2i;
    }
    
    public double calcStep(){
        return (this.t2f - this.t2i)/this.getNpoints();
    }

    public double getWavelength() {
        return wavelength;
    }

    public void setWavelength(double wavelength) {
        this.wavelength = wavelength;
    }
    
    //NO APLICA ZERO NI ESCALA (els posa als valors per defecte) treballa amb l'arrayList directament (es deia getNewSerieWL)
    public DataSerie convertToNewWL(double newWL){
        DataSerie newDS = new DataSerie();
        Iterator<DataPoint> itdp = this.seriePoints.iterator();
        while (itdp.hasNext()){
            DataPoint dp = itdp.next();
            double t2 = dp.getX();
            double asn = (newWL/this.getWavelength())*Math.sin(Math.toRadians(t2/2.));
            if (asn>=-1 && asn<=1){
                double t2new = Math.asin(asn)* 2.f;
                newDS.addPoint(new DataPoint(Math.toDegrees(t2new),dp.getY(),dp.getSdy()));
            }
        }
        return newDS;
    }

    public xunits getxUnits() {
        return xUnits;
    }

    public void setxUnits(xunits xUnits) {
        this.xUnits = xUnits;
    }
    
    //es podria dir tranquilament getNewSerieXunits, pero com que genera una nova dataserie...
    //HAURA DE CONVERTIR EL ZERO I MANTENIR L'ESCALA i WAVLENGTH
    //TODO: T2F T2I step!?
    //TODO DE MOMENT DESACTIVO EL ZERO PERQUE NO ESTA BEN CALCULAT (no es conversió sinó que es desplaçament absout!)
    public DataSerie convertToXunits(xunits destXunits){
        DataSerie newDS = new DataSerie();
        Iterator<DataPoint> itdp = this.seriePoints.iterator();
        
        log.info(String.format("convert from %s to %s",this.getxUnits(), destXunits));
        
        switch (this.getxUnits()){
            case tth:
                switch (destXunits){
                    case tth:
                        log.info("tth to tth");
                        //do nothing
                        break;
                    case dsp:
                        //TTH to DSP
                        log.info("tth to dsp");
                        while (itdp.hasNext()){
                            DataPoint dp = itdp.next();
                            double t2 = dp.getX();
                            double dsp = this.getWavelength()/(2*FastMath.sin(FastMath.toRadians(t2/2.)));
                            newDS.addPoint(new DataPoint(dsp,dp.getY(),dp.getSdy()));
                        }
                        //Zero and scale
                        newDS.setScale(this.getScale());
//                        float zer = (float) (this.getWavelength()/(2*FastMath.sin(FastMath.toRadians(this.getZerrOff()/2.))));
//                        newDS.setZerrOff(zer);
                        break;
                    case dspInv:
                        //TTH to DSP inv
                        log.info("tth to dspInv");
                        while (itdp.hasNext()){
                            DataPoint dp = itdp.next();
                            double t2 = dp.getX();
                            double dsp = this.getWavelength()/(2*FastMath.sin(FastMath.toRadians(t2/2.)));
                            newDS.addPoint(new DataPoint(1/dsp,dp.getY(),dp.getSdy()));
                        }
                        //Zero and scale
                        newDS.setScale(this.getScale());
//                        zer = (float) (this.getWavelength()/(2*FastMath.sin(FastMath.toRadians(this.getZerrOff()/2.))));
//                        newDS.setZerrOff(1/zer);
                        break;
                    case Q:
                        //TTH to Q
                        log.info("tth to Q");
                        while (itdp.hasNext()){
                            DataPoint dp = itdp.next();
                            double t2 = dp.getX();
                            double q = (4*FastMath.PI*FastMath.sin(FastMath.toRadians(t2/2.)))/this.getWavelength();
                            newDS.addPoint(new DataPoint(q,dp.getY(),dp.getSdy()));
                        }
                        //Zero and scale
                        newDS.setScale(this.getScale());
//                        zer = (float) (4*FastMath.PI*FastMath.sin(FastMath.toRadians(this.getZerrOff()/2.))/this.getWavelength());
//                        newDS.setZerrOff(zer);
                        break;
                }
                break;
            case dsp:
                switch (destXunits){
                    case tth:
                        //DSP to TTH
                        log.info("dsp to tth");
                        while (itdp.hasNext()){
                            DataPoint dp = itdp.next();
                            double dsp = dp.getX();
                            double t2 = 2*FastMath.asin(this.getWavelength()/(2*dsp));
                            newDS.addPoint(new DataPoint(FastMath.toDegrees(t2),dp.getY(),dp.getSdy()));
                        }
                        //Zero and scale
                        newDS.setScale(this.getScale());
//                        float zer = (float) (2*FastMath.asin(this.getWavelength()/2*this.getZerrOff()));
//                        newDS.setZerrOff((float) FastMath.toDegrees(zer));
                        break;
                    case dsp:
                        //DSP to DSP
                        log.info("dsp to dsp");
                        break;
                    case dspInv:
                        //DSP to DSP inv
                        log.info("dsp to dspInv");
                        while (itdp.hasNext()){
                            DataPoint dp = itdp.next();
                            double dsp = dp.getX();
                            newDS.addPoint(new DataPoint(1/dsp,dp.getY(),dp.getSdy()));
                        }
                        //Zero and scale
                        newDS.setScale(this.getScale());
//                        newDS.setZerrOff(1/this.getZerrOff());
                        break;
                    case Q:
                        //DSP to Q invertir i multiplicar per 2pi crec...
                        log.info("dsp to Q");
                        while (itdp.hasNext()){
                            DataPoint dp = itdp.next();
                            double dsp = dp.getX();
                            double q = (1/dsp) * FastMath.PI*2;
                            newDS.addPoint(new DataPoint(q,dp.getY(),dp.getSdy()));
                        }
                        //Zero and scale
                        newDS.setScale(this.getScale());
//                        zer = (float) ((1/this.getZerrOff()) * FastMath.PI*2);;
//                        newDS.setZerrOff(zer);
                        break;
                }
                break;
            case dspInv:
                switch (destXunits){
                    case tth:
                        //DSPinv to TTH
                        log.info("dspInv to tth");
                        while (itdp.hasNext()){
                            DataPoint dp = itdp.next();
                            double dsp = 1/dp.getX();
                            double t2 = 2*FastMath.asin(this.getWavelength()/(2*dsp));
                            newDS.addPoint(new DataPoint(FastMath.toDegrees(t2),dp.getY(),dp.getSdy()));
                        }
                        //Zero and scale
                        newDS.setScale(this.getScale());
//                        float zer = (float) (2*FastMath.asin(this.getWavelength()/2*(1/this.getZerrOff())));
//                        newDS.setZerrOff((float) FastMath.toDegrees(zer));
                        break;
                    case dsp:
                        //DSPinv to DSP
                        log.info("dspInv to dsp");
                        while (itdp.hasNext()){
                            DataPoint dp = itdp.next();
                            double dsp = dp.getX();
                            newDS.addPoint(new DataPoint(1/dsp,dp.getY(),dp.getSdy()));
                        }
                        //Zero and scale
                        newDS.setScale(this.getScale());
//                        newDS.setZerrOff(1/this.getZerrOff());
                        break;
                    case dspInv:
                        //DSPinv to DSP inv
                        log.info("dspInv to dspInv");
                        break;
                    case Q:
                        //DSPinv to Q invertir i multiplicar per 2pi crec...
                        log.info("dspInv to Q");
                        while (itdp.hasNext()){
                            DataPoint dp = itdp.next();
                            double dsp = 1/dp.getX();
                            double q = (1/dsp) * FastMath.PI*2;
                            newDS.addPoint(new DataPoint(q,dp.getY(),dp.getSdy()));
                        }
                        //Zero and scale
                        newDS.setScale(this.getScale());
//                        zer = (float) ((1/(1/this.getZerrOff())) * FastMath.PI*2);;
//                        newDS.setZerrOff(zer);
                        break;
                }
                break;
            case Q:
                switch (destXunits){
                    case tth:
                        log.info("Q to tth");
                        while (itdp.hasNext()){
                            DataPoint dp = itdp.next();
                            double q = dp.getX();
                            double tth = FastMath.toDegrees(2*FastMath.asin((this.getWavelength()*q)/(4*FastMath.PI)));
                            newDS.addPoint(new DataPoint(tth,dp.getY(),dp.getSdy()));
                        }
                        //Zero and scale
                        newDS.setScale(this.getScale());
//                        float zer = (float) FastMath.toDegrees(2*FastMath.asin(this.getWavelength()*this.getZerrOff()/4*FastMath.PI));
//                        newDS.setZerrOff(zer);
                        break;
                    case dsp:
                        //Q to DSP (2pi/Q)
                        log.info("Q to dsp");
                        while (itdp.hasNext()){
                            DataPoint dp = itdp.next();
                            double q = dp.getX();
                            double dsp = (2*FastMath.PI)/q;
                            newDS.addPoint(new DataPoint(dsp,dp.getY(),dp.getSdy()));
                        }
                        //Zero and scale
                        newDS.setScale(this.getScale());
//                        zer = (float) (2*FastMath.PI/this.getZerrOff());
//                        newDS.setZerrOff(zer);
                        break;
                    case dspInv:
                        //Q to DSP inv
                        log.info("Q to dspInv");
                        while (itdp.hasNext()){
                            DataPoint dp = itdp.next();
                            double q = dp.getX();
                            double dsp = (2*FastMath.PI)/q;
                            newDS.addPoint(new DataPoint(1/dsp,dp.getY(),dp.getSdy()));
                        }
                        //Zero and scale
                        newDS.setScale(this.getScale());
//                        zer = (float) (2*FastMath.PI/this.getZerrOff());
//                        newDS.setZerrOff(1/zer);
                        break;
                    case Q:
                        //Q to Q
                        log.info("Q to Q");
                        break;
                }
                break;
        }

        if (newDS.getNpoints()<=0){
            return null;
        }
        newDS.setxUnits(destXunits);
        newDS.setWavelength(this.getWavelength());
        
        //DEBUG print 10 first points of DS and newDS
        double[] ds10 = new double[10];
        double[] nds10 = new double[10];
        for (int i=0; i<10;i++){
            ds10[i]=this.getPoint(i).getX();
            nds10[i]=newDS.getPoint(i).getX();
        }
        log.writeNameNums("CONFIG", true, "ds10", ds10);
        log.writeNameNums("CONFIG", true, "nds10", nds10);
        return newDS;

    }

    public serieType getTipusSerie() {
        return tipusSerie;
    }

    public void setTipusSerie(serieType tipusSerie) {
        this.tipusSerie = tipusSerie;
    }

    public double getYOff() {
        return YOff;
    }

    public void setYOff(double yOff) {
        YOff = yOff;
    }
    
    public ArrayList<DataHKL> getClosestReflections(double centralTTh, double tol){
        if (tol<0) tol = 0.025;
        ArrayList<DataHKL> found = new ArrayList<DataHKL>();
        if (this.getTipusSerie()!=serieType.hkl)return found;
        for (int i=0;i<this.getNpoints();i++){
            if (FastMath.abs(centralTTh-this.getHKLPoint(i).getTth())<tol){
                found.add(this.getHKLPoint(i));
            }
        }
        return found;
    }
    
    public double[] calcYmeanYDesvYmaxYmin(){
        double[] ymean_ydesv_ymax_ymin = new double[4];
        ymean_ydesv_ymax_ymin[2] = Double.MIN_VALUE;
        ymean_ydesv_ymax_ymin[3] = Double.MAX_VALUE;
        double sumY = 0;
        for (int i=0;i<this.getNpoints();i++){
            DataPoint dp = this.getPoint(i);
            sumY = sumY + dp.getY();
            if (dp.getY()<ymean_ydesv_ymax_ymin[3])ymean_ydesv_ymax_ymin[3]=dp.getY();
            if (dp.getY()>ymean_ydesv_ymax_ymin[2])ymean_ydesv_ymax_ymin[2]=dp.getY();
        }
        ymean_ydesv_ymax_ymin[0] = sumY/this.getNpoints();
        //ara desviacio
        sumY=0;
        for (int i=0;i<this.getNpoints();i++){
            DataPoint dp = this.getPoint(i);
            sumY = sumY + (dp.getY()-ymean_ydesv_ymax_ymin[0])*(dp.getY()-ymean_ydesv_ymax_ymin[0]);
        }
        ymean_ydesv_ymax_ymin[1] = FastMath.sqrt(sumY/(this.getNpoints()-1));
        return ymean_ydesv_ymax_ymin;
    }
    
    //delsig defineix el llindar (factor delsig*sigmaPattern)
    public void findPeaks(float delsig){      
        peaks.clear();
        double desv = this.calcYmeanYDesvYmaxYmin()[1];
        //limits menys 1
        for (int i=1;i<this.getNpoints()-1;i++){
            double ycent = this.getPoint(i).getY();
            if (ycent < desv*delsig)continue;
            double yleft = this.getPoint(i-1).getY();
            double yright = this.getPoint(i+1).getY();

            if (ycent>yleft && ycent>yright){
                peaks.add(this.getPoint(i));
            }
        }
    }
    
    //delsig defineix el llindar (factor delsig*sigmaPattern), faig ponderacio...
    public void findPeaksBetter(float delsig){      
        peaks.clear();
        double desv = this.calcYmeanYDesvYmaxYmin()[1];
        //limits menys 1
        for (int i=1;i<this.getNpoints()-1;i++){
            double ycent = this.getPoint(i).getY();
            if (ycent < desv*delsig)continue;
            double yleft = this.getPoint(i-1).getY();
            double yright = this.getPoint(i+1).getY();
            double xcent = this.getPoint(i).getX();
            double xleft = this.getPoint(i-1).getX();
            double xright = this.getPoint(i+1).getX();
            if (ycent>yleft && ycent>yright){
                
                //ara pondero entre els tres
                double xpeak = (xcent*ycent + xleft*yleft + xright*yright)/(ycent+yleft+yright);
                peaks.add(new DataPoint(xpeak,ycent,this.getPoint(i).getSdy()));
            }
        }
    }
    
    public void findPeaksEvenBetter(float delsig){
        //interpolar mirar quin està mes avall dels dos del costat i interpolar el valor de y elevat a la recta centre-inferior. alsehores buscar el mig de x
        peaks.clear();
        double desv = this.calcYmeanYDesvYmaxYmin()[1];
        //limits menys 1
        for (int i=1;i<this.getNpoints()-1;i++){
            double ycent = this.getPoint(i).getY();
            if (ycent < desv*delsig)continue;
            double yleft = this.getPoint(i-1).getY();
            double yright = this.getPoint(i+1).getY();
            double xcent = this.getPoint(i).getX();
            double xleft = this.getPoint(i-1).getX();
            double xright = this.getPoint(i+1).getX();
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

                peaks.add(new DataPoint(xpeak,ycent,this.getPoint(i).getSdy()));
            }
        }
    }
}

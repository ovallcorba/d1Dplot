package com.vava33.d1dplot.data;

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
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.util.FastMath;
import com.vava33.d1dplot.D1Dplot_global;
import com.vava33.d1dplot.auxi.PattOps;
import com.vava33.jutils.VavaLogger;

public class DataSerie {
    
    public static float def_markerSize=0;
    public static float def_lineWidth=1;
    public static int def_hklticksize=12;
    public static boolean prfFullprofColors=false;
    public static int def_hklYOff=-16; //ho està a les opcions

    
    
    private static final String className = "DataSerie";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);
    
    //data
    public String serieName;
    private List<Plottable_point> seriePoints;
    private Plottable parent; //ATENCIO, pot no tenirne, es a dir, ser NUL
    private SerieType tipusSerie;
    private Xunits xUnits;
    private double wavelength;
    private float scale = 1.0f;
    private double zerrOff = 0.0f;
    private double YOff = 0.0f;    
    
//    private Data_Xray expInfo;

    //visual options - les faig publiques per evitar getters/setters
    public float markerSize;
    public float lineWidth;
    public boolean showErrBars = false;
    public Color color;
    public boolean plotThis;
    

    
    //empty dataserie... TODO potser hauriem d'obligar a tenir dades?
    public DataSerie(SerieType tipusSerie, Xunits xunits, Plottable parent){
//        this.tipusSerie=tipusSerie;
        this.setTipusSerie(tipusSerie);//AIXO JA HO POSA TOT, color marker, etc...
        this.seriePoints=new ArrayList<Plottable_point>();
        this.plotThis=true;
        this.scale=1.0f;    
        if (tipusSerie==SerieType.hkl)this.scale=def_hklticksize;
        this.zerrOff=0;
        this.YOff=0;
        if (tipusSerie==SerieType.hkl)this.YOff=def_hklYOff;
        this.parent=parent;
        this.wavelength=-1;
        if (parent!=null) this.wavelength=this.parent.getOriginalWavelength();    
        this.xUnits=xunits;
//        this.color=tipusSerie.ini_color;
//        this.markerSize=tipusSerie.ini_markerSize;
//        this.lineWidth=tipusSerie.ini_lineWidth;
        this.serieName="";


    }

    //copia tot menys les dades (empty dataserie)
    public DataSerie(DataSerie inds, List<Plottable_point> punts, Xunits xunits){
        this(inds.getTipusSerie(),xunits,inds.parent);
        this.seriePoints=punts;
        this.scale=inds.getScale();
        this.zerrOff=inds.getZerrOff();
        this.YOff=inds.getYOff();
        this.color=inds.color;
        this.markerSize=inds.markerSize;
        this.lineWidth=inds.lineWidth;
        this.serieName=inds.serieName;
        this.wavelength=inds.wavelength;
    }
    
    //copia tot menys les dades que s'afegeixen apart
    public DataSerie(DataSerie inds, SerieType tipusSerie, boolean copyIntensities){
        this(inds, new ArrayList<Plottable_point>(), inds.getxUnits());
//        this.tipusSerie=tipusSerie;
        this.setTipusSerie(tipusSerie);//AIXO JA HO POSA TOT, color marker, etc...
        if(copyIntensities)this.copySeriePoints(inds);
    }

    public void copySeriePoints(DataSerie inDS) {
    	this.seriePoints=inDS.seriePoints;
    }

    
    //TODO: revisar lo del fons, que era una variable global de patt1D pel prf, no hauria de variar la Y, això hauria de ser a nivell de plot
    //      això ho hauré de fer a una dataserie que sigui PRF
    /**
     * get datapoint with scale and offsets (x and y) applied
     */
//    public Plottable_point getPointWithCorrections(int arrayPosition){ //TODO ens el podem carregar?
//        return getPointWithCorrections(arrayPosition,false);
//    }
    
    public Plottable_point getPointWithCorrections(int arrayPosition, boolean addYBkg){
        return this.seriePoints.get(arrayPosition).getCorrectedDataPoint(this.zerrOff, this.YOff, this.scale, addYBkg);
    }
    
    //manual corrections!
    public Plottable_point getPointWithCorrections(int arrayPosition, double zOff, double yOff, double sca, boolean addYBkg){
        return this.seriePoints.get(arrayPosition).getCorrectedDataPoint(zOff, yOff, sca, addYBkg);
    }
    
    public int getIndexOfDP(Plottable_point dp){
        return this.seriePoints.indexOf(dp);
    }
    
    public DataSerie getSubDataSerie(double t2i, double t2f){
        DataSerie newds = new DataSerie(this.getTipusSerie(),this.xUnits,this.parent);
        for (int i=0;i<this.getNpoints();i++){
            if (this.getPointWithCorrections(i,false).getX()<t2i)continue;
            if (this.getPointWithCorrections(i,false).getX()>t2f)continue;
            newds.addPoint(this.getPointWithCorrections(i,false));
        }
        return newds;
    }
    
    public void clearPoints() {
        this.seriePoints.clear();
    }
    
    public void sortSeriePoints() {
        Collections.sort(seriePoints);
    }
    
    //TODO:revisar si calen els metodes add/remove/set o ordenar a l'afegir pics
    public void addPoint(Plottable_point dp){
        this.seriePoints.add(dp);
        //si afegim pics ordenem
//        if (this.tipusSerie==SerieType.peaks)this.sortSeriePoints();
    }

    public void removePoint(Plottable_point dp){
        logdebug("index of the point to remove="+this.seriePoints.indexOf(dp));
        boolean removed = this.seriePoints.remove(dp);
        logdebug(Boolean.toString(removed));
    }
    public void removePoint(int index){
        this.seriePoints.remove(index);
    }
        
    public int getNpoints(){
        return seriePoints.size();            
    }
    
    public boolean isEmpty() {
        return seriePoints.isEmpty();
    }

//    public Plottable_point getPoint(int i) {
//        return seriePoints.get(i);
//    }
    
//    public Iterator<DataPoint> getIteratorSeriePoints(){
//        return seriePoints.iterator();
//    }
    
//    protected ArrayList<DataPoint> getSeriePoints() {
//        return seriePoints;
//    }
    
    protected List<Plottable_point> getCorrectedSeriePoints(){
        List<Plottable_point> correctedSeriePoints = new ArrayList<Plottable_point>();
        for (int i=0;i<seriePoints.size();i++) {
            correctedSeriePoints.add(this.getPointWithCorrections(i,false));
        }
        return correctedSeriePoints;
    }
    
    //un duplicat
    private List<Plottable_point> getUNCorrectedSeriePoints(){
        List<Plottable_point> uncorrectedSeriePoints = new ArrayList<Plottable_point>();
        for (int i=0;i<seriePoints.size();i++) {
            uncorrectedSeriePoints.add(seriePoints.get(i));
        }
        return uncorrectedSeriePoints;
    }
    
    
    //les subclasses poden tenir DataSeries addicionals... les podem retornar aquí
//    public abstract ArrayList<DataSerie> getComplementarySeries();
    
    public double[] getPuntsMaxXMinXMaxYMinY(){
        if (seriePoints!=null){
            double minX = Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double maxX = Double.MIN_VALUE;
            double maxY = Double.MIN_VALUE;
            for (int i=0;i<seriePoints.size();i++){
                Plottable_point punt = this.getPointWithCorrections(i,false);
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
            if (FastMath.abs(minY-maxY)<1)maxY=minY+100;
            if (FastMath.abs(minX-maxX)<1)maxX=minX+1;
            return new double[]{maxX,minX,maxY,minY};
        }else{
            return null;
        }
    }
 

    
    protected void convertSeriePointsWavelength(double newWL){
//        List<Plottable_point> newSeriePoints = new ArrayList<Plottable_point>();
        for (Plottable_point dp:seriePoints) {
            double t2 = this.getDataPointX_as(Xunits.tth,dp); //en principi obliguem a tenir-ho en 2theta
            double asn = (newWL/this.getWavelength())*FastMath.sin(Math.toRadians(t2/2.));
            if (asn>=-1 && asn<=1){
                double t2new = Math.asin(asn)* 2.f;
                dp.setX(FastMath.toDegrees(t2new));
            }
        }
    }
    
    private boolean isPossibleToConvertToXunits(Xunits destXunits){
        
        switch (destXunits) {
        case tth:
            if (this.xUnits!=Xunits.tth) {
                if (this.wavelength<0)PattOps.askForWavelengthAndAddToDS(this);
                if (this.wavelength<0) {
                    log.info("Wavelength required for units conversion");
                    return false;
                }
            }
            return true;
        case G:
            log.info("not possible to convert G units");
            return false;
        case Q: case dsp: case dspInv:
            if (this.xUnits==Xunits.tth) {
                if (this.wavelength<0)PattOps.askForWavelengthAndAddToDS(this);
                if (this.wavelength<0) {
                    log.info("Wavelength required for units conversion");
                    return false;
                }
            }
            return true;
        default:
            return false;
        }
    }
    
    public float[] getListXvaluesAsDsp() {
        float[] pksDSP = new float[this.getNpoints()];
        for(int i=0;i<this.getNpoints();i++) {
            pksDSP[i]=(float) this.getDataPointX_as(Xunits.dsp,this.getPointWithCorrections(i,false));    
        }
        return pksDSP;
    }
    
    public double[] getListXvaluesAsInvdsp2() {
        double[] pksQ = new double[this.getNpoints()];
        for(int i=0;i<this.getNpoints();i++) {
            pksQ[i]= this.getDataPointX_as(Xunits.dspInv,this.getPointWithCorrections(i,false));    
        }
        return pksQ;
    }
    
    public float getMinXvalueAsDsp() { //busco per si de cas no estan ordenats a la llista
        float mindsp = Float.MAX_VALUE;
        for(int i=0;i<this.getNpoints();i++) {
            float dsp =(float) this.getDataPointX_as(Xunits.dsp,this.getPointWithCorrections(i,false));
            if (dsp<mindsp)mindsp=dsp;
        }
        return mindsp;
    }
    
    //TODO ATENCIO EL PUNT QUE ENTREM HAURIA D'ESTAR JA CORREGIT!!
    public double getDataPointX_as(Xunits destXunits, Plottable_point dp) {
        double q = 0;
        double tth = 0;
        double dsp = 0;
        double invdsp2 = 0;
        double g = 0;
        
        //mirem les actuals i fem els calculs pertinents
        switch (this.getxUnits()) {
        case G:
            g=dp.getX(); //a partir de G no podem calcular res més
            log.info("not possible to convert G units");
            return g;
        case Q:
            q=dp.getX();
            tth=2*FastMath.toDegrees(FastMath.asin((this.getWavelength()*q)/(4*FastMath.PI)));
            dsp=(2*FastMath.PI)/q;
            invdsp2=1/(dsp*dsp);
            break;
        case dsp:
            dsp=dp.getX();
            tth=2*FastMath.toDegrees(FastMath.asin(this.getWavelength()/(2*dsp)));
            invdsp2=1/(dsp*dsp);
            q=(1/dsp) * FastMath.PI*2;
            break;
        case dspInv:
            invdsp2=dp.getX();
            dsp=FastMath.sqrt(1/dp.getX());
            tth=2*FastMath.toDegrees(FastMath.asin(this.getWavelength()/(2*dsp)));
            q=(1/dsp) * FastMath.PI*2;
            break;
        case tth:
            tth=dp.getX();
            dsp=this.getWavelength()/(2*FastMath.sin(FastMath.toRadians(tth/2.)));
            invdsp2=1/(dsp*dsp);
            q=(4*FastMath.PI*FastMath.sin(FastMath.toRadians(tth/2.)))/this.getWavelength();
            break;
        default:
            log.info("not possible to convert units");
            return dp.getX();
        }
        
        switch (destXunits) {
        case Q:
            return q;
        case dsp:
            return dsp;
        case dspInv:
            return invdsp2;
        case tth:
            return tth;
        default:
            log.info("not possible to convert units");
            return dp.getX();
        }
    }
    
    protected void convertSeriePointsXunits(Xunits destXunits){
//        List<Plottable_point> newSeriePoints = new ArrayList<Plottable_point>();
        
        if (this.getxUnits()==destXunits) {
            log.info("conversion to the same units?");
            return;
        }
        
        for (Plottable_point dp:seriePoints) {
//            double t2 = this.getDataPointX_as_tthDeg(dp);
//            double dsp = this.getDataPointX_as_dsp(dp);
//            double q = this.getDataPointX_as_Q(dp);
            
            double t2 = this.getDataPointX_as(Xunits.tth,dp); //directly in degrees
            double dsp = this.getDataPointX_as(Xunits.dsp,dp);
            double q = this.getDataPointX_as(Xunits.Q,dp);
            
            switch (destXunits) {
            case tth:
                dp.setX(t2);
                break;
            case dsp:
                dp.setX(dsp);
                break;
            case dspInv:
                dp.setX(1/(dsp*dsp));
                break;
            case Q:
                dp.setX(q);
                break;
            default:
                log.info("unit conversion not supported");
                break;
            }
        }
//        return newSeriePoints;
    }
    
    
    public boolean convertDStoXunits(Xunits destXunits){
        //TODO Mirar requeriments
        if(!this.isPossibleToConvertToXunits(destXunits)) {
            return false;
        }
        //backup old points
        List<Plottable_point> bakPoints = this.getUNCorrectedSeriePoints();
        this.convertSeriePointsXunits(destXunits);
        if (containNansOrInf(this.seriePoints)) {
            log.infof("Error converting %s to X-units=%s",this.serieName,destXunits.name());
            //reverting
            this.seriePoints=bakPoints;
            return false;
        }
        
//        this.seriePoints=newPoints;
        this.xUnits=destXunits;
        //escala i yoff es mantenen
        //zero cal convertir-lo
        DataPoint zerdp = new DataPoint(this.zerrOff,0,0);
        this.zerrOff=this.getDataPointX_as(destXunits, zerdp);
        return true;
    }
    
    public static boolean containNansOrInf(List<Plottable_point> data) {
        for (Plottable_point p:data) {
            //if (!Double.isFinite(p.getX()))return true; //isFinite not found in java6
            if (Double.isInfinite(p.getX()))return true;
            if (Double.isNaN(p.getX()))return true;
        }
        //return false;
        return false;
    }
    
    public void convertDStoWavelength(double newWL) {
        //nomes ho puc fer amb les dades 2theta, es l'unic dependent de wavelength
        if (this.getxUnits()!=Xunits.tth) {
            log.info("Change X units to 2-theta before changing wavelength");
            return;
        }
        //backup old points
        List<Plottable_point> bakPoints = this.getUNCorrectedSeriePoints();
        convertSeriePointsWavelength(newWL);    
        //si es buida o té nans ho diem
        if (containNansOrInf(this.seriePoints)) {
            log.infof("Error converting %s to wavelength %.5fA",this.serieName,newWL);
            //reverting
            this.seriePoints=bakPoints;
            return;
        }
//        this.seriePoints=newPoints;
        this.wavelength=newWL;
        //escala, yoff, zero... no ho canvio
    }
    
    protected double[] getListXvaluesCorrected() {
        double[] xvals = new double[this.getNpoints()];
        for (int i=0;i<seriePoints.size();i++) {
            xvals[i]=this.getPointWithCorrections(i,false).getX();
            
        }
        return xvals;
    }
    


    protected void clearDataPoints(){
        seriePoints.clear();
    }

    
    public double[] calcYmeanYDesvYmaxYmin(boolean takeIntoAccountYBkg){
        int puntIni = 0;
        int puntFin = this.getNpoints()-1;
        return calcYmeanYDesvYmaxYmin(puntIni,puntFin,takeIntoAccountYBkg);
    }
    
    public Plottable_point getMinYDataPoint(int puntIni, int puntFin, boolean takeIntoAccountYBkg){
        double ymin = Double.MAX_VALUE;
        Plottable_point dpMin = null;
        for (int i=puntIni;i<=puntFin;i++){
            Plottable_point dp = this.getPointWithCorrections(i,takeIntoAccountYBkg); //podria ser true...
            if (dp.getY()<ymin){
                ymin=dp.getY();
                dpMin = dp;
            }
        }
        return dpMin;
    }
    
    //punt Fin està inclos
    public double[] calcYmeanYDesvYmaxYmin(int puntIni, int puntFin, boolean takeIntoAccountYBkg){
        double[] ymean_ydesv_ymax_ymin = new double[4];
        ymean_ydesv_ymax_ymin[2] = Double.MIN_VALUE;
        ymean_ydesv_ymax_ymin[3] = Double.MAX_VALUE;
        int npoints = FastMath.abs(puntFin - puntIni + 1);
        double sumY = 0;
        for (int i=puntIni;i<=puntFin;i++){
            Plottable_point dp = this.getPointWithCorrections(i,takeIntoAccountYBkg);
            sumY = sumY + dp.getY();
            if (dp.getY()<ymean_ydesv_ymax_ymin[3])ymean_ydesv_ymax_ymin[3]=dp.getY();
            if (dp.getY()>ymean_ydesv_ymax_ymin[2])ymean_ydesv_ymax_ymin[2]=dp.getY();
        }
        ymean_ydesv_ymax_ymin[0] = sumY/npoints;
        //ara desviacio
        sumY=0;
        for (int i=puntIni;i<=puntFin;i++){
            Plottable_point dp = this.getPointWithCorrections(i,takeIntoAccountYBkg);
            sumY = sumY + (dp.getY()-ymean_ydesv_ymax_ymin[0])*(dp.getY()-ymean_ydesv_ymax_ymin[0]);
        }
        ymean_ydesv_ymax_ymin[1] = FastMath.sqrt(sumY/(npoints-1));
        return ymean_ydesv_ymax_ymin;
    }


    //returns the closest DP to the one entered (usually by clicking)
    public Plottable_point getClosestDP(Plottable_point click, double tolX, double tolY, boolean takeIntoAccountYBkg){
        if (tolX<0)tolX=1.0;
        if (tolY<0)tolY=5000;
        Plottable_point closest = null;
        double minDiffX = Double.MAX_VALUE/2.5;
        double minDiffY = Double.MAX_VALUE/2.5;
        for (int i=0; i<this.getNpoints();i++){
            Plottable_point dp = this.getPointWithCorrections(i,takeIntoAccountYBkg);
            double diffX = FastMath.abs(dp.getX()-click.getX());
            double diffY = FastMath.abs(dp.getY()-click.getY());
            if ((diffX<tolX)&&(diffY<tolY)){
                if ((diffX+diffY)<(minDiffX+minDiffY)){
                    minDiffX=diffX;
                    minDiffY=diffY;
                    closest = dp;
                    log.fine("index of the closest in loop (i)= "+i);
                    log.fine("index of the closest in loop (indexof dp)= "+seriePoints.indexOf(dp));
                }
            }
        }
        log.fine("index of the closest="+this.seriePoints.indexOf(closest));
        return closest;
    }
    
    //returns the closest DP to the one entered (usually by clicking)
    public Plottable_point getClosestDP_xonly(double xvalue, double tolX){
        if (tolX<0)tolX=1.0;
        Plottable_point closest = null;
        double minDiffX = Double.MAX_VALUE/2.5;
        for (int i=0; i<this.getNpoints();i++){
            Plottable_point dp = this.getPointWithCorrections(i,false);
            double diffX = FastMath.abs(dp.getX()-xvalue);
            if (diffX<tolX){
                if (diffX<minDiffX){
                    minDiffX=diffX;
                    closest = dp;
                    log.fine("index of the closest X in loop (i)= "+i);
                    log.fine("index of the closest X in loop (indexof dp)= "+seriePoints.indexOf(dp));
                }
            }
        }
        log.fine("index of the closest X ="+this.seriePoints.indexOf(closest));
        return closest;
    }
    
    public Plottable_point[] getSurroundingDPs(double xvalue){
        
        for (int i=0;i<this.getNpoints()-1;i++){
            if ((this.getPointWithCorrections(i,false).getX()<=xvalue) && (this.getPointWithCorrections(i+1,false).getX()>=xvalue)){
                Plottable_point[] dps = {this.getPointWithCorrections(i,false), this.getPointWithCorrections(i+1,false)};
                return dps;
            }
        }
        return null;
    }
    
    //return Y
    public double interpolateY(double xval, Plottable_point dp1, Plottable_point dp2){
        double yleft = dp1.getY();
        double yright = dp2.getY();
        double xleft = dp1.getX();
        double xright = dp2.getX();
        
        double pen = (yright-yleft)/(xright-xleft);
        double ord = pen*(xleft)*-1+yleft;
        return pen*xval + ord;
    }
    //return SDY
    public double interpolateSDY(double xval, Plottable_point dp1, Plottable_point dp2){
        double yleft = dp1.getSdy();
        double yright = dp2.getSdy();
        double xleft = dp1.getX();
        double xright = dp2.getX();
        
        double pen = (yright-yleft)/(xright-xleft);
        double ord = pen*(xleft)*-1+yleft;
        return pen*xval + ord;
    }
    



//    private void logdebug(String s){
//        if(D1Dplot_global.isDebug())log.debug(s);
//    }
 
    
    
    
    
    
    //SETTERS/GETTERS PER ELIMINAR?? en deixo alguns malgrat son publics
    public Xunits getxUnits() {
        return xUnits;
    }
    public SerieType getTipusSerie() {
        return tipusSerie;
    }
//    public boolean isPlotThis() {
//        return plotThis;
//    }
    
    //
    public void setxUnits(Xunits xUnits) {
        this.xUnits = xUnits;
    }


    public void setTipusSerie(SerieType tipusSerie) {
        this.tipusSerie = tipusSerie;
        this.color=SerieType.getDefColor(tipusSerie);
        this.markerSize=SerieType.getDefMarkerSize(tipusSerie);
        this.lineWidth=SerieType.getDefLineWidth(tipusSerie);
        if (tipusSerie==SerieType.hkl)this.setScale(DataSerie.def_hklticksize);
    }
    
    public float getScale() {
        return scale;
    }
//
    public void setScale(float scale) {
        this.scale = scale;
    }
//
    public double getZerrOff() {
        return zerrOff;
    }
//
    public void setZerrOff(double zerrOff) {
        this.zerrOff = zerrOff;
    }
//
//    public Color getColor() {
//        return color;
//    }
//
//    protected void setColor(Color color) {
//        this.color = color;
//    }
//    
    public double getYOff() {
        return YOff;
    }
//
    public void setYOff(double yOff) {
        YOff = yOff;
    }

    public double calcStep(){
        return (this.getMaxX() - this.getMinX())/this.getNpoints();
    }
    
    private void logdebug(String s){
        if(D1Dplot_global.isDebug())log.debug(s);
    }

    public Plottable getParent() {
        return parent;
    }
    
    protected void setParent(Plottable p) {
        this.parent=p;
    }


    //TODO: MIN i MAX podrien no estar ordenats!!
    public double getMinX() {
        return this.getPointWithCorrections(0,false).getX();
    }

    public double getMaxX() {
        return this.getPointWithCorrections(seriePoints.size()-1,false).getX();
    }

    public double getWavelength() {
        if (this.wavelength>0) {
            return this.wavelength;
        }else {
            if (parent!=null) {
                return this.parent.getOriginalWavelength();   
            }else {
                return -1;
            }
            
        }
    }
    
    public List<String> getCommentLines(){
        if (parent!=null) {
            return this.parent.getCommentLines();
        }else {
            return new ArrayList<String>(); //empty
        }
        
    }
    
    public double getOriginalWavelength() {
        if (parent!=null) {
            return this.parent.getOriginalWavelength();  
        }else {
            return -1;
        }
    }
    
    public void setWavelength(double wavelA) {
        this.wavelength=wavelA;
    }

    public List<Plottable_point> getClosestPointsToAGivenX(double centralX, double tol){
        if (tol<0) tol = 0.025;
        List<Plottable_point> found = new ArrayList<Plottable_point>();
        for (int i=0;i<this.getNpoints();i++){
            if (FastMath.abs(centralX-this.getPointWithCorrections(i,false).getX())<tol){
                found.add(this.getPointWithCorrections(i,false));
            }
        }
        return found;
    }
    
    
}

package com.vava33.d1dplot.auxi;

/*
 * Implementation of indexing solution for dichotomy
 * 
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.math3.util.FastMath;

import com.vava33.cellsymm.Cell;
import com.vava33.cellsymm.HKLrefl;
import com.vava33.d1dplot.D1Dplot_global;
import com.vava33.jutils.FileUtils;
import com.vava33.jutils.VavaLogger;


public class IndexSolutionDichotomy implements IndexSolution{

    private static final String className = "IndexSolutionDichotomy";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);

    private class Qinterval{
        float Qlow,Qhigh,Qerr;
        double hkl_qobs; //in case there is a observed hkl inside the interval, its Qobs (if not it will be -1)
        HKLrefl hkl;
        public Qinterval(float qlow,float qhigh,float qerr, HKLrefl hkl) {
            if (qlow<qhigh) {
                this.Qlow=qlow;
                this.Qhigh=qhigh;
            }else {
                this.Qlow=qhigh;
                this.Qhigh=qlow;
            }
            this.Qerr=qerr;
            this.hkl=hkl;
            this.hkl_qobs=-1;
        }
        public boolean intervallContains(double qval) {
            return ((qval >= (Qlow-Qerr)) && (qval <= (Qhigh+Qerr)));
        }
        public String print() {
            return String.format("Qinterval [%.5f,%.5f]",(Qlow-Qerr),(Qhigh+Qerr));
        }
        public String printLong() {
            return String.format("reflection %s with Qcalc=%.5f, %s",hkl.toString(),hkl.calcQvalue(),this.print());
            
        }
    }
    
    ArrayList<Qinterval> Qintervals;
    Cell cell_base;
    float incPar, incAngRAD; //les Seguents!! les de la propera iteracio
    double[] Qobs;
    double Qmax, Qerr;
    double m20,f20;
    int i20;
    
    public IndexSolutionDichotomy(Cell celMin, float currentIncPar, float currentIncAng,double[] qobs, double qmax, double deltaQerr) {
        this.cell_base=celMin;
        this.incPar = currentIncPar;
        this.incAngRAD = currentIncAng;
        this.Qobs=qobs;
        this.Qerr=deltaQerr;
        this.Qmax=qmax;
        this.Qintervals = new ArrayList<Qinterval>();
    }
    
    public boolean areAllQobsInsideHKLQintervals(){
        
        for (HKLrefl hkl: this.cell_base.generateHKLsAsymetricUnitCrystalFamily(this.Qmax)) { //qmax ja conte l'error, ho he posat al constructor
            log.fine(hkl.toString());
            float[] qlowhigh = this.calcQminusQplus(hkl,incPar*2); //*2 perquè ara volem l'ACTUAL!
            this.Qintervals.add(new Qinterval(qlowhigh[0],qlowhigh[1],(float)this.Qerr,hkl));
            log.configf("reflection %s with Qcalc=%.5f, %s",hkl.toString(),hkl.calcQvalue(),this.Qintervals.get(this.Qintervals.size()-1).print());
        }
        
        //comprovem amb els Qobs entrats si aquesta solucio els accepta
        boolean esSolucio=true;
        for (int k=0; k<Qobs.length;k++) {
            log.config("*** checking reflection with qobs="+Qobs[k]);
            //mirem que estigui a algun interval de Q
            if (!this.checkIfinQintevals(Qobs[k])) {
                //només que una reflexio no es trobi podem ja passar a la següent cel·la base (TODO: a no ser que implementem impureses que seria facil posar-ho aquí com un contador)
                log.config("not in interval");
                esSolucio=false;
                break;
            }
        }
        
        return esSolucio;
    }
    
    //limits Q for a given reflections to apply dicothomy method
    //for cubic, tetra, hexa and ortho ==> highsymm
    //mono i tric, individually
    public float[] calcQminusQplus(HKLrefl hkl, float currentIncPar) {
        float[] qlowhigh = new float[2];
        int h = hkl.getHKLindices()[0];
        int k = hkl.getHKLindices()[1];
        int l = hkl.getHKLindices()[2];
        double a = this.cell_base.getCellParameters(false)[0];
        double b = this.cell_base.getCellParameters(false)[1];
        double c = this.cell_base.getCellParameters(false)[2];
        switch (this.cell_base.getCrystalFamily()) {
        case CUBIC: case TETRA: case HEXA: case ORTO:
            qlowhigh[0] = (float) (((h*h)/((a+currentIncPar)*(a+currentIncPar))) + ((k*k)/((b+currentIncPar)*(b+currentIncPar))) + ((l*l)/((c+currentIncPar)*(c+currentIncPar))));
            qlowhigh[1] = (float) (((h*h)/(a*a)) + ((k*k)/(b*b)) + ((l*l)/(c*c)));
            break;
        case MONO:
            break;
        case TRIC:
            break;
        default:
            break;
        }
        return qlowhigh;            
    }
    
    //uses variable Qintervals in the current state
    public boolean checkIfinQintevals(double qval) {

        
        Iterator<Qinterval> itQ = Qintervals.iterator();
        boolean containsIt = false;
        while (itQ.hasNext()) {
            Qinterval qinter = itQ.next();
            if (!qinter.intervallContains(qval)) {
                containsIt=false;
            }else {
                containsIt=true;
                //GUARDEM EL QVAL DEL HKL
                qinter.hkl_qobs=qval;
                log.config("... in interval "+qinter.printLong());
//              log.info(String.format("checking interval [%.3f,%.3f] %d %d %d --> %b", qinter.Qlow,qinter.Qhigh,qinter.h,qinter.k,qinter.l,containsIt));
                break;
            }
//          log.info(String.format("checking interval [%.3f,%.3f] %d %d %d --> %b", qinter.Qlow,qinter.Qhigh,qinter.h,qinter.k,qinter.l,containsIt));
        }
        return containsIt;
    }
    
    @Override
    public IndexSolution getIS() {
        return this;
    }
    
    @Override
    public String toString() {
         String out = String.format("cell_base= %s [%s] NextIncPar=%.4f NextIncAng=%.4f", 
                cell_base.toStringCellParamOnly(),this.cell_base.getCrystalFamily().getNameString(),this.incPar, FastMath.toDegrees(this.incAngRAD));
         return out;
    }

    
    //-1 per agafar 2*increment (es a dir una iteracio que no sigui la primera)
    public float[] getAVals(float aMax) {
        if (aMax<0) aMax = this.getAbase()+(incPar*2);
        return FileUtils.arange(this.getAbase(), aMax, incPar);
    }
    public float[] getBVals(float bMax) {
        if (bMax<0) bMax = this.getBbase()+(incPar*2);
        return FileUtils.arange(this.getBbase(), bMax, incPar);
    }
    public float[] getCVals(float cMax) {
        if (cMax<0) cMax = this.getCbase()+(incPar*2);
        return FileUtils.arange(this.getCbase(), cMax, incPar);
    }
    public float[] getAlVals(float alMax) {
        if (alMax<0) alMax = this.getAlBaseRad()+(incAngRAD*2);
        return FileUtils.arange(this.getAlBaseRad(), alMax, incAngRAD);
    }
    public float[] getBeVals(float beMax) {
        if (beMax<0) beMax = this.getBeBaseRad()+(incAngRAD*2);
        return FileUtils.arange(this.getBeBaseRad(), beMax, incAngRAD);
    }
    public float[] getGaVals(float gaMax) {
        if (gaMax<0) gaMax = this.getGaBaseRad()+(incAngRAD*2);
        return FileUtils.arange(this.getGaBaseRad(), gaMax, incAngRAD);
    }   

    public float getAbase() {
        return (float) this.cell_base.getCellParameters(false)[0];
    }
    public float getBbase() {
        return (float) this.cell_base.getCellParameters(false)[1];
    }
    public float getCbase() {
        return (float) this.cell_base.getCellParameters(false)[2];
    }
    public float getAlBaseRad() {
        return (float) this.cell_base.getCellParameters(false)[3];
    }
    public float getBeBaseRad() {
        return (float) this.cell_base.getCellParameters(false)[4];
    }
    public float getGaBaseRad() {
        return (float) this.cell_base.getCellParameters(false)[5];
    }

    @Override
    public Cell getRefinedCell() {
        return this.cell_base.refineCellByQobs(Qobs, Qmax);
    }

    @Override
    public Cell getUnRefinedCell() {
        return this.cell_base.getIncrementedCell(this.incPar, this.incAngRAD);
    }
    @Override
    public double getM20() {
        calcM20();
        return m20;
    }
    @Override
    public int getI20() {
        return i20;
    }
    
    private void calcM20() {
        double Q20=0;
        i20=0;
        if (Qobs.length>=20) {
            Q20=Qobs[19];
            i20=20;
        }else {
            Q20=Qobs[Qobs.length-1];
            i20=Qobs.length;
        }
        
        /*AQUI ARA TENIM UNA MICA DE COMPLEXITAT PER FER-HO BE:
         * .. es com fer una nova iteracio
         * 
         * 1- afinar cel·la
         * 2- calcular reflexions
         * 3- calcular intervalsQ
         * 4- calcular desviacions, i.e. M20
         */
        Cell c = getRefinedCell();   
//        ArrayList<HKLrefl> hkls = c.generateHKLsAsymetricUnitCrystalFamily(Q20);
        ArrayList<HKLrefl> hkls = c.generateHKLsAsymetricUnitCrystalFamily(Q20+Qerr, false, false, true, false, true); //afegeixo Qerr pero no se si caldria...
//        int N20 = hkls.size();
//        ArrayList<Qinterval> finalQintervals = new ArrayList<Qinterval>();
        //TODO: solapo la variable Qintervals... potser no faria falta una variable per això ja que només ho farem servir aquí no? bé, en tot cas millor solapar perquè si ens interessa és a partir d'aquí
        Qintervals.clear(); 
        for (HKLrefl hkl: hkls) {
            float[] qlowhigh = this.calcQminusQplus(hkl,incPar*2); //*2 perquè ara volem l'ACTUAL!
            Qintervals.add(new Qinterval(qlowhigh[0],qlowhigh[1],(float)this.Qerr,hkl));
//            Qintervals.add(new Qinterval(qlowhigh[0],qlowhigh[1],this.deltaQerr,hkl));
        }
        //Posem cada Qobs al seu interval, i.e. fem matching de reflexions (ja que algunes no seran observades)
        for (int k=0; k<Qobs.length;k++) {
            this.checkIfinQintevals(Qobs[k]); //aixo ja grava hkl_qobs dins l'interval si es que hi és
        }
        //ara ja podem calcular les diferencies acumulades i la M20 mirant reflexio per reflexio de cada interval
        double acumDiff=0;
        int N20=0;
        for(Qinterval qint:Qintervals) {
            if (qint.hkl_qobs>Q20) {//ja estem a la Qmax, de totes formes nomes he generat fins a Q20... es comprovacio redundant
                break;
            }
            N20++;
            if (qint.hkl_qobs>0) { //vol dir que tenim reflexio observada dins l'interval 
                double qcalc = qint.hkl.calcQvalue();
                acumDiff = acumDiff + FastMath.abs(qcalc-qint.hkl_qobs);
            }
            //fem un print per pantalla equivalent al dicvol
            log.infof("%d %d %d Qcalc=%.5f Qobs=%.5f",qint.hkl.getHKLindices()[0],qint.hkl.getHKLindices()[1],qint.hkl.getHKLindices()[2],qint.hkl.calcQvalue(),qint.hkl_qobs);
        }
        
        double meandiff = acumDiff/i20;
        this.m20=Q20/(2*meandiff*N20);
        
        log.infof("m20=%.3f i20=%d N20=%d Q20=%.5f",m20,i20,N20,Q20);
        
        
    }
    
}
    
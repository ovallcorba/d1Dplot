package com.vava33.d1dplot.index;

/*
 * Implementation of indexing solution for dichotomy
 * 
 * Aqui tindre una cel·la base (a0,b0,c0) corresponent a un 
 * triplet d'intervals (m,n,q a la publicacio), en cas ortorombic
 * es a dir a0+p, b0+p, c0+p es la cel·la "màxima". i p es incpar
 * 
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

import com.vava33.cellsymm.Cell;
import com.vava33.cellsymm.HKLrefl;
import com.vava33.cellsymm.CellSymm_global.CrystalFamily;
import com.vava33.d1dplot.D1Dplot_global;
import com.vava33.d1dplot.auxi.CartesianProduct;
import com.vava33.d1dplot.auxi.PattOps;
import com.vava33.jutils.VavaLogger;


public class IndexSolutionDichotomy extends IndexSolution{

    private static final String className = "IndexSolutionDichotomy";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);
    
    float incPar, incAngRAD; //les Seguents!! les de la propera iteracio
    IndexDichotomy id;

    
    public IndexSolutionDichotomy(Cell celMin, float currentIncPar, float currentIncAng, IndexDichotomy id) {
        this.cella=celMin;
        this.incPar = currentIncPar;
        this.incAngRAD = (float) FastMath.toRadians(currentIncAng);
        this.Qintervals = new ArrayList<Qinterval>();
        this.m20Calc=false;
        this.id=id;
    }
    
    //copia les cel·les, intervals i Q's
    public IndexSolutionDichotomy(IndexSolutionDichotomy is) {
        this(is.cella,is.incPar,is.incAngRAD,is.id);
        this.cell_refined=new Cell(is.getRefinedCell());
    }
    
    public Cell getRefinedCell() {
        if(this.cell_refined==null) {
            this.cell_refined=this.cella.refineCellByQobs(this.getQobsWithoutSpurious(), this.getIndexMethod().Qmax+3*this.getIndexMethod().deltaQerr, incPar/2., incAngRAD/2.);
//            this.cell_refined=this.cella.refineCellByQobs(this.getIndexMethod().Qobs, this.getIndexMethod().Qmax+2*this.getIndexMethod().deltaQerr, incPar/2., incAngRAD/2.);
        }
        return this.cell_refined;
    }
    
    public List<IndexSolutionDichotomy> generateNextIter(){
        ArrayList<IndexSolutionDichotomy> sols = new ArrayList<IndexSolutionDichotomy>();
        
        double[] cel = this.cella.getCellParameters(false);
        double[] aVals = {cel[0], cel[0]+this.incPar/2.};
        double[] bVals = {cel[1], cel[1]+this.incPar/2.};
        double[] cVals = {cel[2], cel[2]+this.incPar/2.};
        double[] alVals = {cel[3], cel[3]+this.incAngRAD/2.};
        double[] beVals = {cel[4], cel[4]+this.incAngRAD/2.};
        double[] gaVals = {cel[5], cel[5]+this.incAngRAD/2.};
                
        switch(cella.getCrystalFamily()) {
        case CUBIC: //cada interval es parteix en 2, facil 
            sols.add(new IndexSolutionDichotomy(this.cella,this.incPar/2.f,0,this.id));
            sols.add(new IndexSolutionDichotomy(this.cella.getIncrementedCell(this.incPar/2.f, 0),this.incPar/2.f,0,this.id));
            break;
        case TETRA: 
            int[] lengths = new int[] { aVals.length, cVals.length };
            for (int[] indices : new CartesianProduct(lengths)) {
                sols.add(new IndexSolutionDichotomy(
                        new Cell(aVals[indices[0]],aVals[indices[0]],cVals[indices[1]],90.0,90.0,90.0,true,CrystalFamily.TETRA),
                        this.incPar/2.f,0,this.id));
            }
            break;
        case HEXA:
            lengths = new int[] { aVals.length, cVals.length };
            for (int[] indices : new CartesianProduct(lengths)) {
                sols.add(new IndexSolutionDichotomy(
                        new Cell(aVals[indices[0]],aVals[indices[0]],cVals[indices[1]],90.0,90.0,120.0,true,CrystalFamily.HEXA),
                        this.incPar/2.f,0,this.id));
            }
            break;
        case ORTO:
            lengths = new int[] { aVals.length, bVals.length, cVals.length };
            for (int[] indices : new CartesianProduct(lengths)) {
                sols.add(new IndexSolutionDichotomy(
                        new Cell(aVals[indices[0]],bVals[indices[1]],cVals[indices[2]],90.0,90.0,90.0,true,CrystalFamily.ORTO),
                        this.incPar/2.f,0,this.id));
            }
            break;
        case MONO:
            lengths = new int[] { aVals.length, bVals.length, cVals.length, beVals.length };
            for (int[] indices : new CartesianProduct(lengths)) {
                sols.add(new IndexSolutionDichotomy(
                        new Cell(aVals[indices[0]],bVals[indices[1]],cVals[indices[2]],FastMath.PI/2.,beVals[indices[3]],FastMath.PI/2,false,CrystalFamily.MONO),
                        this.incPar/2.f,this.incAngRAD/2,this.id));
            }
            break;
        default: //triclinic and undefined
            lengths = new int[] { aVals.length, bVals.length, cVals.length, alVals.length, beVals.length, gaVals.length };
            for (int[] indices : new CartesianProduct(lengths)) {
                sols.add(new IndexSolutionDichotomy(
                        new Cell(aVals[indices[0]],bVals[indices[1]],cVals[indices[2]],alVals[indices[3]],beVals[indices[4]],gaVals[indices[5]],false,CrystalFamily.TRIC),
                        this.incPar/2.f,this.incAngRAD/2,this.id));
            }
            break;
        }
        return sols;
    }
    
    public boolean areAllQobsInsideHKLQintervals(){
                
        for (HKLrefl hkl: this.cella.generateHKLsAsymetricUnitCrystalFamily(id.Qmax)) { //qmax ja conte l'error, ho he posat al constructor
            log.fine(hkl.toString());
            float[] qlowhigh = this.calcQminusQplus(hkl,incPar); //l'ACTUAL!
            this.Qintervals.add(new Qinterval(qlowhigh[0],qlowhigh[1],id.deltaQerr,hkl));
            log.finef("reflection %s with Qcalc=%.5f, %s",hkl.toString(),hkl.calcQvalue(),this.Qintervals.get(this.Qintervals.size()-1).print());
        }
        
        //comprovem amb els Qobs entrats si aquesta solucio els accepta
        boolean esSolucio=true;
        int impureses= 0; //implementades impureses el 9/5/19
        for (int k=0; k<id.Qobs.length;k++) {
            log.fine("*** checking reflection with qobs="+id.Qobs[k]);
            //mirem que estigui a algun interval de Q
            if (!this.checkIfinQintevals(id.Qobs[k])) {
                //només que una reflexio no es trobi podem ja passar a la següent cel·la base (a no ser que s'acceptin impureses)
                log.config("not in interval");
                impureses = impureses + 1;
                if (impureses>id.nImp) {
                    esSolucio=false;
                    break;                    
                }
            }
        }
        
        return esSolucio;
    }
    
    
    //calculs previs (TODO: es pot fer extern a aquest metode per nomes fer-ho un cop per solucio i ACCELERAR)
    double Amin;
    double Bmin;
    double Cmin;
    double Aplus;
    double Bplus;
    double Cplus;
    double beMin;
    double bePlus;
    double cosBeMin;
    double cosBePlus;
    double a,b,c,al,be,ga;
    boolean calculated = false;
    
    private void calculsPrevisQminQplus() {
        double[] pars = this.cella.getCellParameters(false);
        a = pars[0];
        b = pars[1];
        c = pars[2];
        al = pars[3];
        be = pars[4];
        ga = pars[5];
        
        Amin = a *FastMath.sin(be);
        Bmin = b;
        Cmin = c *FastMath.sin(be);
        Aplus = Amin + incPar;
        Bplus = Bmin + incPar;
        Cplus = Cmin + incPar;
        beMin = be;
        bePlus = beMin + this.incAngRAD;
        cosBeMin = FastMath.cos(beMin);
        cosBePlus = FastMath.cos(bePlus);
        
        calculated = true;
    }
    
    //limits Q for a given reflections to apply dicothomy method
    //for cubic, tetra, hexa and ortho ==> highsymm
    //mono i tric, individually
    public float[] calcQminusQplus(HKLrefl hkl, float currentIncPar) {
        if (!calculated)this.calculsPrevisQminQplus();
        float[] qlowhigh = new float[2];
        int h = hkl.getHKLindices()[0];
        int k = hkl.getHKLindices()[1];
        int l = hkl.getHKLindices()[2];

        switch (this.cella.getCrystalFamily()) {
        case CUBIC: case TETRA: case HEXA: case ORTO:
            qlowhigh[0] = (float) (((h*h)/((a+currentIncPar)*(a+currentIncPar))) + ((k*k)/((b+currentIncPar)*(b+currentIncPar))) + ((l*l)/((c+currentIncPar)*(c+currentIncPar))));
            qlowhigh[1] = (float) (((h*h)/(a*a)) + ((k*k)/(b*b)) + ((l*l)/(c*c)));
            break;
        case MONO:
            double fApCpBEm = (h*h)/(Aplus*Aplus) + (l*l)/(Cplus*Cplus) - 2*h*l*cosBeMin/(Aplus*Cplus);
            double fAmCmBEp = (h*h)/(Amin*Amin) + (l*l)/(Cmin*Cmin) - 2*h*l*cosBePlus/(Amin*Cmin);
            double gBp = (k*k)/(Bplus*Bplus);
            double gBm = (k*k)/(Bmin*Bmin);
            if ((h*l)>=0) {
                qlowhigh[0] = (float) (fApCpBEm + gBp);
                qlowhigh[1] = (float) (fAmCmBEp + gBm);
            }else {
                double fApCpBEp = (h*h)/(Aplus*Aplus) + (l*l)/(Cplus*Cplus) - 2*h*l*cosBePlus/(Aplus*Cplus);
                double fAmCpBEp = (h*h)/(Amin*Amin) + (l*l)/(Cplus*Cplus) - 2*h*l*cosBePlus/(Amin*Cplus);
                double fApCmBEp = (h*h)/(Aplus*Aplus) + (l*l)/(Cmin*Cmin) - 2*h*l*cosBePlus/(Aplus*Cmin);
                qlowhigh[0] = (float) PattOps.findMin(fApCpBEp+gBp,fAmCpBEp+gBp,fApCmBEp+gBp,fAmCmBEp+gBp);
                double fAmCpBEm = (h*h)/(Amin*Amin) + (l*l)/(Cplus*Cplus) - 2*h*l*cosBeMin/(Amin*Cplus);
                double fApCmBEm = (h*h)/(Aplus*Aplus) + (l*l)/(Cmin*Cmin) - 2*h*l*cosBeMin/(Aplus*Cmin);
                double fAmCmBEm = (h*h)/(Amin*Amin) + (l*l)/(Cmin*Cmin) - 2*h*l*cosBeMin/(Amin*Cmin);
                qlowhigh[1] = (float) PattOps.findMax(fApCpBEm+gBm,fAmCpBEm+gBm,fApCmBEm+gBm,fAmCmBEm+gBm);

                
            }
            break;
        case TRIC:
            break;
        default:
            break;
        }
        return qlowhigh;            
    }
    

    @Override
    public void calcM20() {
        i20=0;
        if (id.Qobs.length>=20) {
            q20=id.Qobs[19];
            i20=20;
        }else {
            q20=id.Qobs[id.Qobs.length-1];
            i20=id.Qobs.length;
        }
        
        /*AQUI ARA TENIM UNA MICA DE COMPLEXITAT PER FER-HO BE:
         * .. es com fer una nova iteracio
         * 
         * 1- afinar cel·la
         * 2- calcular reflexions
         * 3- calcular intervalsQ
         * 4- calcular desviacions, i.e. M20
         */
        List<HKLrefl> hkls = getRefinedCell().generateHKLsAsymetricUnitCrystalFamily(q20+id.deltaQerr, true, true, true, false, true); //afegeixo Qerr pero no se si caldria... tenia false false al centering i sg però millor ho tinc en compte i així puc reutilitzar canviant la cel·la
        //TODO: solapo la variable Qintervals... potser no faria falta una variable per això ja que només ho farem servir aquí no? bé, en tot cas millor solapar perquè si ens interessa és a partir d'aquí
        Qintervals.clear(); 
        for (HKLrefl hkl: hkls) {
            float[] qlowhigh = this.calcQminusQplus(hkl,incPar); //el de la solucio actual
            Qintervals.add(new Qinterval(qlowhigh[0],qlowhigh[1],(float)id.deltaQerr,hkl));
        }
        //Posem cada Qobs al seu interval, i.e. fem matching de reflexions (ja que algunes no seran observades)
        for (int k=0; k<id.Qobs.length;k++) {
//            this.checkIfinQintevals(id.Qobs[k],false); //aixo ja grava hkl_qobs dins l'interval si es que hi és --- CANVIAT
            this.assignQobsToIntervals(id.Qobs[k]);
        }
        //ara ja podem calcular les diferencies acumulades i la M20 mirant reflexio per reflexio de cada interval
        double acumDiff=0;
        n20=0;
        for(Qinterval qint:Qintervals) {
            if (qint.hkl_qobs>q20) {//ja estem a la Qmax, de totes formes nomes he generat fins a Q20... es comprovacio redundant
                break;
            }
            n20++;
            if (qint.hkl_qobs>0) { //vol dir que tenim reflexio observada dins l'interval 
                double qcalc = qint.hkl.calcQvalue();
                acumDiff = acumDiff + FastMath.abs(qcalc-qint.hkl_qobs);
            }
            //fem un print per pantalla equivalent al dicvol
            log.configf("%d %d %d Qcalc=%.5f Qobs=%.5f",qint.hkl.getHKLindices()[0],qint.hkl.getHKLindices()[1],qint.hkl.getHKLindices()[2],qint.hkl.calcQvalue(),qint.hkl_qobs);
        }
        
        double meandiff = acumDiff/i20;
        this.m20=q20/(2*meandiff*n20);
        
        log.infof("m20=%.3f i20=%d N20=%d Q20=%.5f",m20,i20,n20,q20);

        this.m20Calc=true;
    }

    @Override
    public Cell getUnRefinedCell() { //fem un promig
        return this.cella.getIncrementedCell(this.incPar/2., this.incAngRAD/2.);
    }
    
    @Override
    public IndexSolution getDuplicate() {
        return new IndexSolutionDichotomy(this);
    }
    
    @Override
    public String toString() {
         String out = String.format("cell_base= %s [%s] IncPar=%.4f IncAng=%.4f", 
                cella.toStringCellParamOnly(),this.cella.getCrystalFamily().getNameString(),this.incPar, FastMath.toDegrees(this.incAngRAD));
         return out;
    }

    @Override
    public IndexMethod getIndexMethod() {
        return id;
    }
}
    
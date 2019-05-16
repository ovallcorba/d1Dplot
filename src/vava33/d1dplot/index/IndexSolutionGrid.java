package com.vava33.d1dplot.index;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

import com.vava33.cellsymm.Cell;
import com.vava33.d1dplot.D1Dplot_global;
import com.vava33.jutils.VavaLogger;
import com.vava33.cellsymm.HKLrefl;

public class IndexSolutionGrid extends IndexSolution implements Comparable<IndexSolutionGrid> {

    private static final String className = "IndexSolutionGrid";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);
    
    public float res; //TODO: he posat public per proves, tornar a privat
    IndexGrid ig;
    
    public IndexSolutionGrid(Cell c, float res, IndexGrid ig) {
        this.cella=c;
        this.res=res;
        this.ig=ig;
        this.Qintervals = new ArrayList<Qinterval>();
    }
    
    //copia les cel·les, intervals i Q's
    public IndexSolutionGrid(IndexSolutionGrid isg) {
        this(isg.cella,isg.res,isg.ig);
        this.cell_refined=new Cell(isg.getRefinedCell());
    }

    public Cell getRefinedCell() {
        if(this.cell_refined==null) {
            //en cas d'espuris hem d'enviar Qobs sense considerar-los
            double incPar = FastMath.max(FastMath.max(ig.aStep, ig.bStep),ig.cStep);
            double incAngRad = FastMath.max(FastMath.max(ig.alStep, ig.beStep),ig.gaStep);
            this.cell_refined=this.cella.refineCellByQobs(this.getQobsWithoutSpurious(), this.getIndexMethod().Qmax+3*this.getIndexMethod().deltaQerr, incPar, incAngRad);
        }
        return this.cell_refined;
    }

    //returns false if it is not a valid solution according to impurities
    public boolean calcResidual() {
        List<HKLrefl> hkls = cella.generateHKLsAsymetricUnitCrystalFamily(ig.Qmax);
        Qintervals.clear(); 
        for (HKLrefl hkl: hkls) {
            float q = (float) hkl.calcQvalue();
            Qintervals.add(new Qinterval(q-ig.deltaQerr,q+ig.deltaQerr,(float)this.ig.deltaQerr,hkl));
        }
        
        //comprovem amb els Qobs entrats si aquesta solucio els accepta
        int impureses= 0; //implementades impureses el 9/5/19
        for (int k=0; k<ig.Qobs.length;k++) {
            log.config("*** checking reflection with qobs="+ig.Qobs[k]);
            //mirem que estigui a algun interval de Q
            if (!this.assignQobsToIntervals(ig.Qobs[k])) { //aquest metode nomes assigna la Qobs a la Qcalc més propera (i torna false si no hi és a cap interval) -- necessari per calcular be el residual
                //només que una reflexio NO es trobi podem ja passar a la següent cel·la base (a no ser que s'acceptin impureses)
                log.config(ig.Qobs[k]+" not in interval");
                impureses = impureses + 1;
                if (impureses>ig.nImp) {
                    return false;                    
                }
            }
        }
        //calculem el residual
        this.res=0;
        for (Qinterval q:this.Qintervals) {
            res = res + (float)FastMath.abs(q.hkl_qobs-q.hkl.calcQvalue());
        }
        log.config("res="+res);
        return true;
    }
    
    @Override
    public void calcM20() {
        i20=0;
        if (ig.Qobs.length>=20) {
            q20=ig.Qobs[19];
            i20=20;
        }else {
            q20=ig.Qobs[ig.Qobs.length-1];
            i20=ig.Qobs.length;
        }
        
        //a cada reflexio calculada cal assignar, si s'escau una observada (la mes propera? o definim interval per considerar no observada??)
        List<HKLrefl> hkls = getRefinedCell().generateHKLsAsymetricUnitCrystalFamily(q20+ig.deltaQerr, true, true, true, false, true); //afegeixo Qerr pero no se si caldria... tenia false false al centering i sg però millor ho tinc en compte i així puc reutilitzar canviant la cel·la
        Qintervals.clear(); 
        for (HKLrefl hkl: hkls) {
            float q = (float) hkl.calcQvalue();
//            Qintervals.add(new Qinterval(q-ig.deltaQerr,q+ig.deltaQerr,(float)this.ig.deltaQerr,hkl));
            Qintervals.add(new Qinterval(q-this.ig.deltaQerr,q+this.ig.deltaQerr,(float)this.ig.deltaQerr,hkl));
        }
        
        //TODO PASA QUE QUAN NO HI HA CAP ASSIGNADA LA M20 PUJA MOLT... AQUI HAURIEM DE CONSIDERAR UN MINIM D'IMPURESES per FORÇA i a la LLISTA INCLOURE TANT LES QCALC que no tenen QOBS (com fins ara) **COM** les QOBS que no tenen QCALC!!!
        
        //Posem cada Qobs al seu interval, i.e. fem matching de reflexions (ja que algunes no seran observades)
        for (int k=0; k<ig.Qobs.length;k++) {
//            this.checkIfinQintevals(ig.Qobs[k],false); //aixo ja grava hkl_qobs dins l'interval si es que hi és
            this.assignQobsToIntervals(ig.Qobs[k]);
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
            log.finef("%d %d %d Qcalc=%.5f Qobs=%.5f",qint.hkl.getHKLindices()[0],qint.hkl.getHKLindices()[1],qint.hkl.getHKLindices()[2],qint.hkl.calcQvalue(),qint.hkl_qobs);
        }
        
        double meandiff = acumDiff/i20;
        this.m20=q20/(2*meandiff*n20);
        
        log.infof("m20=%.3f i20=%d N20=%d Q20=%.5f",m20,i20,n20,q20);

        this.m20Calc=true;
    }

    @Override
    public IndexSolution getDuplicate() {
        return new IndexSolutionGrid(this);
    }
    
    @Override
    public String toString() {
        return String.format("%s res=%8.4f", cella.toStringCellParamOnly(),res);
    }
    
    @Override
    public int compareTo(IndexSolutionGrid o) {
        IndexSolutionGrid otherSol = (IndexSolutionGrid) o;
        if (this.res>otherSol.res) return -1;
        if (this.res<=otherSol.res) return 1;
        return 0; //it never happens
    }

    @Override
    public IndexMethod getIndexMethod() {
        return ig;
    }
}

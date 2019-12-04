package com.vava33.d1dplot.index;

/*
 * Interface for an indexing solution (independent of the method)
 * 
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.util.Iterator;
import java.util.List;
import org.apache.commons.math3.util.FastMath;

import com.vava33.BasicPlotPanel.core.SerieType;
import com.vava33.cellsymm.Cell;
import com.vava33.cellsymm.HKLrefl;
import com.vava33.d1dplot.D1Dplot_global;
import com.vava33.d1dplot.data.DataPoint_hkl;
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.d1dplot.data.Xunits;
import com.vava33.jutils.FileUtils;
import com.vava33.jutils.VavaLogger;

public abstract class IndexSolution {
    
    private static final String className = "IndexSolution";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);
    
    List<Qinterval> Qintervals;
    Cell cella,cell_refined;
    double m20,f20,q20;
    boolean m20Calc;
    int i20,n20;
    
    abstract public void calcM20();
    abstract public IndexMethod getIndexMethod();
    abstract public IndexSolution getDuplicate();
    abstract public Cell getRefinedCell();
    
    public DataSerie getAsHKL_dsp_dataserie() {
        DataSerie ds = new DataSerie(SerieType.hkl,Xunits.dsp, null);
        for (HKLrefl pdr:this.getRefinedCell().generateHKLsAsymetricUnitCrystalFamily(this.getIndexMethod().Qmax, true,true,true,true,true)) {
            ds.addPoint(new DataPoint_hkl(pdr.getDsp(),pdr.getYcalc(),0,pdr,ds));
        }
        ds.setName(this.getRefinedCell().toStringCellParamOnly());
        return ds;
    }
    
    public String getInfoAsStringToSaveResults() {
        if (!this.m20Calc)this.calcM20();        
        
        StringBuilder sb = new StringBuilder();
        String LS = FileUtils.lineSeparator;
        sb.append(this.getRefinedCell().toStringCellParamOnly()+LS);
        
        n20=0;
        sb.append("  h  k  l    Qcalc     Qobs"+LS);
        
        for(Qinterval qint:Qintervals) {
            //fem un print per pantalla equivalent al dicvol
            sb.append(String.format(" %2d %2d %2d %8.5f %8.5f",qint.hkl.getHKLindices()[0],qint.hkl.getHKLindices()[1],qint.hkl.getHKLindices()[2],qint.hkl.calcQvalue(),qint.hkl_qobs)+LS);
            n20++;
        }
        //posem aqui les impureses... mes facil que entremig
        if (this.getIndexMethod().nImp>0) {
            sb.append(" List of spurious peaks:"+LS);
            int n = 0;
            //cas Impureses...cal mirar les Qobs no justificades
            for (double q:this.getIndexMethod().Qobs) {
                if (!this.checkIfinQintevals(q)) {
                    sb.append(String.format("   Qobs = %8.5f",q)+LS);
                    n++;
                }
            }
            if (n==0) {
                sb.append("   No spurious peaks"+LS);
                sb.append(LS);
            }
        }
        sb.append(String.format("     M(%d)=%.2f",i20,m20)+LS);
        sb.append(String.format("     Q(%d)=%.5f",i20,q20)+LS); //Qintervals.get(Qintervals.size()-1).hkl_qobs)+LS)
        sb.append(String.format("     Nrefs up to Q(%d)=%d",i20,n20)+LS);

        log.info(sb.toString());
        return sb.toString();
    }

    //refem el metode checkIfinQintervals de forma que no guarda Qobs i torna sempre que troba la primera solucio (breakonfirst)
    // i.. en fem un altre que guarda valor de qval però amb opcions (de moment nomes implemento la primera fins que trobi necessari la segona):
    //   -- unicament a un interval (o més d'un amb igual Qcalc), el que té el qval més proper a la reflexio de l'interval
    //   -- a tots els intervals que entren en Q+-Qer, es a dir que tornen TRUE a intervalContains
    public boolean checkIfinQintevals(double qval) {
        Iterator<Qinterval> itQ = Qintervals.iterator();
        while (itQ.hasNext()) {
            Qinterval qinter = itQ.next();
            if (qinter.intervallContains(qval)) {
                log.fine("... in interval "+qinter.printLong());
                return true;
            }
        }
        return false;
    }
    
    public int getNumSpurious() {
        if (Qintervals.isEmpty())this.calcM20();
        int n = 0;
        for (double q:this.getIndexMethod().Qobs) {
            if (!this.checkIfinQintevals(q)) {
                n++;
            }
        }
        return n;
    }
    
    public double[] getQobsWithoutSpurious() {
        if (this.getIndexMethod().nImp>0) {
            if (Qintervals.isEmpty())this.calcM20();
            double[] QobsClean = new double[this.getIndexMethod().Qobs.length-this.getNumSpurious()];
            int i=0;
            for (double q:this.getIndexMethod().Qobs) {
                if (this.checkIfinQintevals(q)) {
                    QobsClean[i]=q;
                    i++;
                }
            }
            return QobsClean;
        }
        return this.getIndexMethod().Qobs;
        
    }
    
    
    //hem de fer dues passades per trobar primer mindiff i despres assignar
    public boolean assignQobsToIntervals(double qval) {
        Iterator<Qinterval> itQ = Qintervals.iterator();
        boolean containsIt = false;
        float mindiff = Float.MAX_VALUE;
        while (itQ.hasNext()) {
            Qinterval qinter = itQ.next();
            if (qinter.intervallContains(qval)) {
                containsIt = true;
                float diff = (float) FastMath.abs(qinter.hkl.calcQvalue()-qval);
                if (diff<mindiff) {
                    mindiff = diff;
                }
            }
        }
        
        if (!containsIt)return false; //no cal fer segona passada si no el conte
        
        //segona pasada
        itQ = Qintervals.iterator();
        while (itQ.hasNext()) {
            Qinterval qinter = itQ.next();
            if (qinter.intervallContains(qval)) {
                float diff = (float) FastMath.abs(qinter.hkl.calcQvalue()-qval);
                if (diff<=(mindiff*1.001)) { //poso un factor 1permil de seguretat
                    qinter.hkl_qobs=qval;
                }
            }
        }
        return true;
    }
    
    public Cell getUnRefinedCell() {
        return cella;
    }
    
    public double getM20() {
        if (!this.m20Calc)calcM20();
        return m20;
    }
    
    public int getI20() {
        if (!this.m20Calc)calcM20();
        return i20;
    }

    public int getN20() {
        if (!this.m20Calc)calcM20();
        return n20;
    }

    public double getQ20() {
        if (!this.m20Calc)calcM20();
        return q20;
    }
    
    public IndexSolution getIS() {
        return this;
    }
    
    @Override
    public boolean equals(Object obj) {
        return (this.getRefinedCell().equals(((IndexSolution)obj).getRefinedCell()));
    }
    
    @Override
    public int hashCode() {
//      return Objects.hash(this.getRefinedCell().toStringCellParamOnly(),this.getRefinedCell().getCrystalFamily(),this.getRefinedCell().getCrystalCentering(),this.getRefinedCell().getSg());
        
//        return Objects.hash(this.getRefinedCell().getCellParameters(true),this.getRefinedCell().getCrystalFamily(),this.getRefinedCell().getCrystalCentering(),this.getRefinedCell().getSg());
        //en aquest cas probablement es suficient sumant els parametres?
        Cell c = this.getRefinedCell();
        String nums = c.toStringCellParamOnly().trim().replaceAll("\\s+", "").replace(".", "").substring(0, 8);
        int num = Integer.parseInt(nums);
        return num;
        
    }
}


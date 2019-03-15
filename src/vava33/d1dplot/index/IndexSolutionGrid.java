package com.vava33.d1dplot.index;

/*
 * Implementation of indexing solution for grid
 * 
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import org.apache.commons.math3.util.FastMath;

import com.vava33.cellsymm.Cell;
import com.vava33.cellsymm.CellSymm_global.CrystalFamily;
import com.vava33.d1dplot.auxi.PattOps;
import com.vava33.d1dplot.data.DataPoint_hkl;
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.cellsymm.HKLrefl;

public class IndexSolutionGrid implements Comparable<IndexSolutionGrid>,IndexSolution{
    Cell cella;
    float res;
    int hmax,hmin,kmax,kmin,lmax,lmin;
    DataSerie hkls;
    
    public IndexSolutionGrid(double a, double b, double c, double al, double be, double ga, float res) {
        cella = new Cell(a,b,c,al,be,ga,false,CrystalFamily.NONE);
        this.res=res;
        hkls = new DataSerie(hkls,com.vava33.d1dplot.data.SerieType.hkl, false); //TODO revisar que he afegit arguments per evitar error ho he fet perque no peti
    }
    
    public void sethklmaxmin(int hmin, int hmax, int kmin, int kmax, int lmin, int lmax) {
        this.hmax=hmax;
        this.hmin=hmin;
        this.kmax=kmax;
        this.kmin=kmin;
        this.lmax=lmax;
        this.lmin=lmin;
        
    }
    
    public void addRef(HKLrefl hkl, float dsp, float wave) {
        double t2 = FastMath.toDegrees(PattOps.get2thRadFromDsp(wave, dsp));
        this.hkls.addPoint(new DataPoint_hkl(hkl,t2)); //TODO revisar que he afegit arguments per evitar error ho he fet perque no peti
//      this.hkls.addHKLPoint(new DataHKL(h,k,l,dsp));
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
    public Cell getRefinedCell() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Cell getUnRefinedCell() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IndexSolution getIS() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getM20() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void calcM20() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int getI20() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getN20() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getQ20() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getInfoAsStringToSaveResults() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DataSerie getAsHKL_dsp_dataserie() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IndexSolution getDuplicate() {
        // TODO Auto-generated method stub
        return null;
    }
    
}

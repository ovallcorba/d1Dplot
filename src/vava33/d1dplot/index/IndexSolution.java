package com.vava33.d1dplot.index;

/*
 * Interface for an indexing solution (independent of the method)
 * 
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import com.vava33.cellsymm.Cell;
import com.vava33.d1dplot.data.DataSerie;

public interface IndexSolution {
    public Cell getRefinedCell();
    public Cell getUnRefinedCell();
//    public double[] getQobsUsedAscending();
    public IndexSolution getIS();
    public double getM20();
    public void calcM20();
    public int getI20();
    int getN20();
    double getQ20();
    public String getInfoAsStringToSaveResults();
    public DataSerie getAsHKL_dsp_dataserie();
//    public IndexSolution getDuplicate();
//    void setRefinedCell(Cell c);
    IndexSolution getDuplicate();
}

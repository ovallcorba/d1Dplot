package com.vava33.d1dplot.auxi;

/*
 * Interface for an indexing solution (independent of the method)
 * 
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import com.vava33.cellsymm.Cell;

public interface IndexSolution {
    public Cell getRefinedCell();
    public Cell getUnRefinedCell();
//    public double[] getQobsUsedAscending();
    public IndexSolution getIS();
    public double getM20();
    public int getI20();
}

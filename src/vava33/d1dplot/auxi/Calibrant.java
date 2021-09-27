package com.vava33.d1dplot.auxi;

/**
 * D1Dplot
 * 
 * XRD calibrant substance (list of dsp)
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import com.vava33.cellsymm.Cell;

public class Calibrant {

    private String name;
    private Cell cell;

    public Calibrant(String calName, Cell c) {
        this.setName(calName);
        this.setCell(c);
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

	public Cell getCell() {
		return cell;
	}

	public void setCell(Cell cell) {
		this.cell = cell;
	}

	//FORMAT: calibrant: NAME; a b c alfa beta gamma; SGnum
	public String printEntryOpt() {
		return String.format("%s; %s; %d", name, cell.toStringCellParamOnly(), cell.getSg().getsgNum());
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}

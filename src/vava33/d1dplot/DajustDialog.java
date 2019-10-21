package com.vava33.d1dplot;

/**
 * D1Dplot
 * 
 * Dialog to launch a Pattern Matching (DAjust)
 *  (TODO)
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import javax.swing.JPanel;
import com.vava33.cellsymm.CellSymm_global;
import com.vava33.jutils.VavaLogger;

public class DajustDialog extends JDialog {

    private static final String className = "DAjustDialog";
    private static VavaLogger log = CellSymm_global.getVavaLogger(className);  
    
    private final JPanel contentPanel = new JPanel();
    private PlotPanel plotpanel;
    private D1Dplot_main main;
    
    /**
     * Create the dialog.
     */
    public DajustDialog(PlotPanel p,D1Dplot_main m) {
        setBounds(100, 100, 893, 633);
        this.plotpanel=p;
        this.main = m;
        //TODO
    }
}


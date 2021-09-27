package com.vava33.d1dplot;

/**
 * D1Dplot
 * 
 * Wavelength calibration
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import javax.swing.JDialog;
import javax.swing.JPanel;

import com.vava33.cellsymm.Cell;
import com.vava33.cellsymm.CellSymm_global;
import com.vava33.d1dplot.auxi.Calibrant;
import com.vava33.jutils.VavaLogger;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JTextPane;
import javax.swing.JButton;
import javax.swing.JTextArea;

public class CalibrationDialog {

	
	private JDialog CalibDialog;
    XRDPlot1DPanel plotpanel;
    D1Dplot_data dades;
    
    private static Calibrant silicon_640D = new Calibrant("Silicon NIST-640D",new Cell(5.43123,5.43123,5.43123,90,90,90,true,CellSymm_global.getSpaceGroupByNum(227)));
	private static Calibrant lab6_660B = new Calibrant("LaB6 NIST-660B",new Cell(4.15689,4.15689,4.15689,90,90,90,true,CellSymm_global.getSpaceGroupByNum(221)));
	protected static ArrayList<Calibrant> calibrants;
    
    private static final String className = "CalibrationDialog";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);
    
    private JTextField txtSiparam;
    private final ButtonGroup buttonGroup = new ButtonGroup();
    private JTextField txtNrpeaks;
    private JTextField txtEnergy;

    /**
     * Create the dialog.
     */
    public CalibrationDialog(XRDPlot1DPanel p,D1Dplot_data d) {
        this.plotpanel = p;
        this.dades=d;
        this.CalibDialog = new JDialog(D1Dplot_global.getD1DmainFrame(),"Fit Peak(s) **EXPERIMENTAL**",false);
        CalibDialog.setTitle("Wavelength Calibration");
        CalibDialog.getContentPane().setLayout(new MigLayout("", "[][grow][]", "[][][][][][][grow][]"));
        
        final JTextPane txtpnSelectEitherA = new JTextPane();
        txtpnSelectEitherA.setEditable(false);
        txtpnSelectEitherA.setOpaque(false);
        txtpnSelectEitherA.setText("Select either a calibrant from the list or set the silicon cell parameter");
        CalibDialog.getContentPane().add(txtpnSelectEitherA, "cell 0 0 3 1,grow");
        
        final JCheckBox checkBoxCalibrant = new JCheckBox("Calibrant");
        buttonGroup.add(checkBoxCalibrant);
        CalibDialog.getContentPane().add(checkBoxCalibrant, "cell 0 1");
        
        final JComboBox comboCalib = new JComboBox();
        CalibDialog.getContentPane().add(comboCalib, "cell 1 1 2 1,growx");
        
        final JCheckBox checkBoxSilicon = new JCheckBox("Si cell parameter (A)");
        buttonGroup.add(checkBoxSilicon);
        CalibDialog.getContentPane().add(checkBoxSilicon, "cell 0 2");
        
        txtSiparam = new JTextField();
        CalibDialog.getContentPane().add(txtSiparam, "cell 1 2 2 1,growx");
        txtSiparam.setColumns(10);
        
        final JLabel lblNrOfPeaks = new JLabel("Nr. of peaks to use");
        CalibDialog.getContentPane().add(lblNrOfPeaks, "cell 0 3,alignx trailing");
        
        txtNrpeaks = new JTextField();
        CalibDialog.getContentPane().add(txtNrpeaks, "cell 1 3,growx");
        txtNrpeaks.setColumns(10);
        
        final JCheckBox chckbxFitPeaks = new JCheckBox("Fit peaks");
        chckbxFitPeaks.setToolTipText("To determine peak position. Otherwise the 2-theta at maximum intensity will be used");
        CalibDialog.getContentPane().add(chckbxFitPeaks, "cell 2 3");
        
        final JLabel lblEnergyUsedkev = new JLabel("Energy used (keV)");
        lblEnergyUsedkev.setToolTipText("You can also input wavelength in Angstrom here...");
        CalibDialog.getContentPane().add(lblEnergyUsedkev, "cell 0 4,alignx trailing");
        
        txtEnergy = new JTextField();
        CalibDialog.getContentPane().add(txtEnergy, "cell 1 4 2 1,growx");
        txtEnergy.setColumns(10);
        
        final JButton btnRefine = new JButton("Refine");
        CalibDialog.getContentPane().add(btnRefine, "cell 0 5 3 1,growx");
        
        final JTextArea textArea = new JTextArea();
        CalibDialog.getContentPane().add(textArea, "cell 0 6 3 1,grow");
        
        final JPanel panel = new JPanel();
        CalibDialog.getContentPane().add(panel, "cell 0 7 3 1,grow");
        panel.setLayout(new MigLayout("", "[][208px][]", "[25px]"));
        
        final JButton btnSaveToText = new JButton("Save to text file");
        panel.add(btnSaveToText, "cell 0 0,growx,aligny center");
        
        final JButton btnApplyValuesTo = new JButton("Apply to patterns");
        panel.add(btnApplyValuesTo, "cell 1 0,growx,aligny center");
        
        final JButton btnClose = new JButton("Close");
        panel.add(btnClose, "cell 2 0,growx,aligny center");
        log.info("Wavelength calibration is IN DEVELOPMENT. It may contain errors"); 

    }
    
    private void init() {
    	//aqui afegirem els calibrants per defecte
    	calibrants.add(silicon_640D);
    	calibrants.add(lab6_660B);
	   
    }
}

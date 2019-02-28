package com.vava33.d1dplot;
/*
 * Indexing dialog (tests)
 * 
 * REMEMBER TO CHECK LOG WINDOW IN MAIN WHEN LAUNCHING INDEX
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.math3.util.FastMath;

import com.vava33.cellsymm.Cell;
import com.vava33.d1dplot.auxi.DataSerie;
import com.vava33.d1dplot.auxi.IndexDichotomy;
import com.vava33.d1dplot.auxi.IndexSolution;
import com.vava33.d1dplot.auxi.IndexSolutionDichotomy;
import com.vava33.d1dplot.auxi.IndexSolutionGrid;
import com.vava33.d1dplot.auxi.PattOps;
import com.vava33.jutils.FileUtils;
import com.vava33.jutils.VavaLogger;

import net.miginfocom.swing.MigLayout;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;

public class IndexDialog {

    private static final String className = "Index_dialog";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);
	
    private JPanel contentPanel;
	private JDialog indexDialog;
    private PlotPanel plotpanel;
    
    private JTextField txtNpeaks;
    private JTextField txtAmax;
    private JTextField txtBmax;
    private JTextField txtCmax;
    private JTextField txtBetamin;
    private JTextField txtBetamax;
    private JTextField txtVmin;
    private JTextField txtVmax;
    private JTextField txtWave;
    private JTextField txtMw;
    private JTextField txtDensity;
    private JTextField txtDensityerr;
    private JTextField txtEps;
    private JTextField txtFom;
    private JTextField txtSpurious;
    private JCheckBox chckbxCubic;
    private JCheckBox chckbxTetragonal;
    private JCheckBox chckbxHexagonal;
    private JCheckBox chckbxOrthorhombic;
    private JCheckBox chckbxMonoclinic;
    private JCheckBox chckbxTriclinic;
    private JCheckBox chckbxZeroRefienment;
    private JCheckBox chckbxPrevZeroSearch;
    private JCheckBox chckbxDicvolOpt;
    
//    private float amax=25.0f;
//    private float bmax=25.0f;
//    private float cmax=25.0f;
//    private float amin=1.0f;
//    private float bmin=1.0f;
//    private float cmin=1.0f;
//    private float alfamin=90.0f;
//    private float alfamax=120.0f;
//    private float betamin=90.0f;
//    private float betamax=120.0f;
//    private float gammamin=90.0f;
//    private float gammamax=120.0f;
//    private float astep = 0.1f;
//    private float bstep = 0.1f;
//    private float cstep = 0.1f;
//    private float alstep = 0.5f;
//    private float bestep = 0.5f;
//    private float gastep = 0.5f;
    
    private float amax=6f;
    private float bmax=6f;
    private float cmax=6f;
    private float amin=5f;
    private float bmin=5f;
    private float cmin=5f;
    private float alfamin=90.0f;
    private float alfamax=90.0f;
    private float betamin=90.0f;
    private float betamax=90.0f;
    private float gammamin=90.0f;
    private float gammamax=90.0f;
    private float astep = 0.02f;
    private float bstep = 0.02f;
    private float cstep = 0.02f;
    private float alstep = 0.2f;
    private float bestep = 0.2f;
    private float gastep = 0.2f;
    
    private float vmin=0.0f;
    private float vmax=3000.0f;
    
    private float wavel = -1.0f;
    private float mw = 0.0f;
    private float density = 0.0f;
    private float densityerr = 0.0f;
    
    private float eps = 0.02f;
    private float minfom = 10.0f;
    private int spurious = 0;
    
    private boolean cubic = true;
    private boolean tetra = true;
    private boolean hexa = true;
    private boolean orto = true;
    private boolean mono = true;
    private boolean tric = false;
    
    private boolean zeroRef = true;
    private boolean prevzero = false;
//    private boolean dic06 = false;
    private int npeaks = 20;
    
    private DataSerie ds;
    ProgressMonitor pm;
    IndexGridSearchBruteWorker gridSwk;
    DichothomyWorker dicW;
    
//    private boolean everythingOK = true;
    private JLabel lblMin;
    private JLabel lblMax;
    private JLabel lblMin_1;
    private JLabel lblMax_1;
    private JLabel lbl_step;
    private JLabel label;
    private JLabel label_1;
    private JTextField txtAlfaMin;
    private JTextField txtAlfaMax;
    private JTextField txtGaMin;
    private JTextField txtGaMax;
    private JLabel lblaMin;
    private JLabel lblbMin;
    private JLabel lblcMin;
    private JTextField txtAmin;
    private JTextField txtBmin;
    private JTextField txtCmin;
    private JLabel label_2;
    private JLabel label_3;
    private JLabel label_4;
    private JTextField txtAstep;
    private JTextField txtBstep;
    private JTextField txtCstep;
    private JTextField txtAlstep;
    private JTextField txtBestep;
    private JTextField txtGastep;
    private JPanel panel_1;
    private JLabel lblMethod;
    private JRadioButton rdbtnGridSearch;
    private JRadioButton rdbtnDicothomy;
    private final ButtonGroup buttonGroupMethod = new ButtonGroup();
    
	public IndexDialog(JFrame parent, PlotPanel p, DataSerie datas) {
        this.plotpanel=p;
        this.ds=datas;
        this.contentPanel = new JPanel();
        this.indexDialog = new JDialog(parent,"Index",false);
        this.indexDialog.setIconImage(D1Dplot_global.getIcon());
        this.indexDialog.setBounds(100, 100, 700, 568);
        this.indexDialog.getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.indexDialog.getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new MigLayout("", "[grow][grow][]", "[][][][grow][grow]"));
        {
            JLabel lblNrPeaksTo = new JLabel("Nr. Peaks to use=");
            contentPanel.add(lblNrPeaksTo, "cell 0 0,alignx trailing");
        }
        {
            txtNpeaks = new JTextField();
            txtNpeaks.setText("npeaks");
            contentPanel.add(txtNpeaks, "cell 1 0,growx");
            txtNpeaks.setColumns(10);
        }
        {
            JLabel lblXunits = new JLabel("(xunits)");
            contentPanel.add(lblXunits, "cell 2 0");
        }
        {
            JPanel panel = new JPanel();
            panel.setBorder(new TitledBorder(null, "Crystal Systems", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            contentPanel.add(panel, "cell 0 1 3 1,grow");
            panel.setLayout(new MigLayout("", "[][][][][][]", "[]"));
            {
                chckbxCubic = new JCheckBox("Cubic");
                chckbxCubic.setSelected(true);
                panel.add(chckbxCubic, "cell 0 0");
            }
            {
                chckbxTetragonal = new JCheckBox("Tetragonal");
                chckbxTetragonal.setSelected(true);
                panel.add(chckbxTetragonal, "cell 1 0");
            }
            {
                chckbxHexagonal = new JCheckBox("Hexagonal");
                chckbxHexagonal.setSelected(true);
                panel.add(chckbxHexagonal, "cell 2 0");
            }
            {
                chckbxOrthorhombic = new JCheckBox("Orthorhombic");
                chckbxOrthorhombic.setSelected(true);
                panel.add(chckbxOrthorhombic, "cell 3 0");
            }
            {
                chckbxMonoclinic = new JCheckBox("Monoclinic");
                chckbxMonoclinic.setSelected(true);
                panel.add(chckbxMonoclinic, "cell 4 0");
            }
            {
                chckbxTriclinic = new JCheckBox("Triclinic");
                panel.add(chckbxTriclinic, "cell 5 0");
            }
        }
        {
            JPanel panel = new JPanel();
            panel.setBorder(new TitledBorder(null, "Cell dimensions", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            contentPanel.add(panel, "cell 0 2 3 1,grow");
            panel.setLayout(new MigLayout("", "[][grow][][grow][][grow]", "[][][][][][][]"));
            {
            	lblaMin = new JLabel("<html>\n<i>a</i> min (Å)=\n</html>");
            	panel.add(lblaMin, "cell 0 0");
            }
            {
            	txtAmin = new JTextField();
            	txtAmin.setText("25.0");
            	txtAmin.setColumns(10);
            	panel.add(txtAmin, "cell 1 0,growx");
            }
            {
                JLabel lblAMax = new JLabel("<html>\n<i>a</i> max ("+D1Dplot_global.angstrom+")=\n</html>");
                panel.add(lblAMax, "cell 2 0,alignx trailing");
            }
            {
                txtAmax = new JTextField();
                txtAmax.setText("25.0");
                panel.add(txtAmax, "cell 3 0,growx");
                txtAmax.setColumns(10);
            }
            {
            	label_2 = new JLabel("step");
            	panel.add(label_2, "cell 4 0,alignx trailing");
            }
            {
            	txtAstep = new JTextField();
            	panel.add(txtAstep, "cell 5 0,growx");
            	txtAstep.setColumns(10);
            }
            {
            	lblbMin = new JLabel("<html>\n<i>b</i> min (Å)=\n</html>");
            	panel.add(lblbMin, "cell 0 1,alignx trailing");
            }
            {
            	txtBmin = new JTextField();
            	panel.add(txtBmin, "cell 1 1,growx");
            	txtBmin.setColumns(10);
            }
            {
                JLabel lblBMax = new JLabel("<html>\n<i>b</i> max ("+D1Dplot_global.angstrom+")=\n</html>");
                panel.add(lblBMax, "cell 2 1,alignx trailing");
            }
            {
                txtBmax = new JTextField();
                txtBmax.setText("25.0");
                panel.add(txtBmax, "cell 3 1,growx");
                txtBmax.setColumns(10);
            }
            {
            	label_3 = new JLabel("step");
            	panel.add(label_3, "cell 4 1,alignx trailing");
            }
            {
            	txtBstep = new JTextField();
            	panel.add(txtBstep, "cell 5 1,growx");
            	txtBstep.setColumns(10);
            }
            {
            	lblcMin = new JLabel("<html>\n<i>c</i> min (Å)=\n</html>");
            	panel.add(lblcMin, "cell 0 2,alignx trailing");
            }
            {
            	txtCmin = new JTextField();
            	panel.add(txtCmin, "cell 1 2,growx");
            	txtCmin.setColumns(10);
            }
            {
                JLabel lblCMax = new JLabel("<html>\n<i>c</i> max ("+D1Dplot_global.angstrom+")=\n</html>");
                panel.add(lblCMax, "cell 2 2,alignx trailing");
            }
            {
                txtCmax = new JTextField();
                txtCmax.setText("25.0");
                panel.add(txtCmax, "cell 3 2,growx");
                txtCmax.setColumns(10);
            }
            {
            	label_4 = new JLabel("step");
            	panel.add(label_4, "cell 4 2,alignx trailing");
            }
            {
            	txtCstep = new JTextField();
            	panel.add(txtCstep, "cell 5 2,growx");
            	txtCstep.setColumns(10);
            }
            {
            	lblMin = new JLabel("<html>\n<i>α</i> min (º)=\n</html>");
            	panel.add(lblMin, "cell 0 3,alignx trailing");
            }
            {
            	txtAlfaMin = new JTextField();
            	panel.add(txtAlfaMin, "cell 1 3,growx");
            	txtAlfaMin.setColumns(10);
            }
            {
            	lblMax = new JLabel("<html>\n<i>α</i> max (º)=\n</html>");
            	panel.add(lblMax, "cell 2 3,alignx trailing");
            }
            {
            	txtAlfaMax = new JTextField();
            	txtAlfaMax.setColumns(10);
            	panel.add(txtAlfaMax, "cell 3 3,growx");
            }
            {
            	lbl_step = new JLabel("step");
            	panel.add(lbl_step, "cell 4 3,alignx trailing,aligny top");
            }
            {
            	txtAlstep = new JTextField();
            	panel.add(txtAlstep, "cell 5 3,growx");
            	txtAlstep.setColumns(10);
            }
            {
                JLabel lblBetaMin = new JLabel("<html>\n<i>"+D1Dplot_global.beta+"</i> min (º)=\n</html>");
                panel.add(lblBetaMin, "cell 0 4,alignx trailing");
            }
            {
                txtBetamin = new JTextField();
                txtBetamin.setText("90.0");
                panel.add(txtBetamin, "cell 1 4,growx");
                txtBetamin.setColumns(10);
            }
            {
                JLabel lblBetaMax = new JLabel("<html>\n<i>"+D1Dplot_global.beta+"</i> max (º)=\n</html>");
                panel.add(lblBetaMax, "cell 2 4,alignx trailing");
            }
            {
                txtBetamax = new JTextField();
                txtBetamax.setText("125.0");
                panel.add(txtBetamax, "cell 3 4,growx");
                txtBetamax.setColumns(10);
            }
            {
            	label = new JLabel("step");
            	panel.add(label, "cell 4 4,alignx trailing");
            }
            {
            	txtBestep = new JTextField();
            	panel.add(txtBestep, "cell 5 4,growx");
            	txtBestep.setColumns(10);
            }
            {
            	lblMin_1 = new JLabel("<html>\n<i>γ</i> min (º)=\n</html>");
            	panel.add(lblMin_1, "cell 0 5,alignx trailing");
            }
            {
            	txtGaMin = new JTextField();
            	panel.add(txtGaMin, "cell 1 5,growx");
            	txtGaMin.setColumns(10);
            }
            {
            	lblMax_1 = new JLabel("<html>\n<i>γ</i> max (º)=\n</html>");
            	panel.add(lblMax_1, "cell 2 5,alignx trailing");
            }
            {
            	txtGaMax = new JTextField();
            	panel.add(txtGaMax, "cell 3 5,growx");
            	txtGaMax.setColumns(10);
            }
            {
            	label_1 = new JLabel("step");
            	panel.add(label_1, "cell 4 5,alignx trailing");
            }
            {
            	txtGastep = new JTextField();
            	panel.add(txtGastep, "cell 5 5,growx");
            	txtGastep.setColumns(10);
            }
            {
                JLabel lblVMin = new JLabel("V min ("+D1Dplot_global.angstrom+"3)=");
                panel.add(lblVMin, "cell 0 6,alignx trailing");
            }
            {
                txtVmin = new JTextField();
                txtVmin.setText("0");
                panel.add(txtVmin, "cell 1 6,growx");
                txtVmin.setColumns(10);
            }
            {
                JLabel lblVMax = new JLabel("V max ("+D1Dplot_global.angstrom+"3)=");
                panel.add(lblVMax, "cell 2 6,alignx trailing");
            }
            {
                txtVmax = new JTextField();
                txtVmax.setText("3000");
                panel.add(txtVmax, "cell 3 6,growx");
                txtVmax.setColumns(10);
            }
        }
        {
            JPanel panel = new JPanel();
            panel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "General", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
            contentPanel.add(panel, "cell 0 3 3 1,grow");
            panel.setLayout(new MigLayout("", "[][grow][][grow][]", "[][][][]"));
            {
                JLabel lblWavelengha = new JLabel("Wavelengh ("+D1Dplot_global.angstrom+")=");
                panel.add(lblWavelengha, "cell 0 0,alignx trailing");
            }
            {
                txtWave = new JTextField();
                txtWave.setText("wave");
                panel.add(txtWave, "cell 1 0,growx");
                txtWave.setColumns(10);
            }
            {
                JLabel lblEps = new JLabel("EPS=");
                panel.add(lblEps, "cell 2 0,alignx trailing");
            }
            {
                txtEps = new JTextField();
                txtEps.setText("0.02");
                panel.add(txtEps, "cell 3 0,growx");
                txtEps.setColumns(10);
            }
            {
                chckbxZeroRefienment = new JCheckBox("Zero Refienment");
                chckbxZeroRefienment.setSelected(true);
                panel.add(chckbxZeroRefienment, "cell 4 0");
            }
            {
                JLabel lblMwgmol = new JLabel("MW (g)=");
                panel.add(lblMwgmol, "cell 0 1,alignx trailing");
            }
            {
                txtMw = new JTextField();
                txtMw.setText("0");
                panel.add(txtMw, "cell 1 1,growx");
                txtMw.setColumns(10);
            }
            {
                JLabel lblMinFom = new JLabel("Min FOM=");
                panel.add(lblMinFom, "cell 2 1,alignx trailing");
            }
            {
                txtFom = new JTextField();
                txtFom.setText("10.0");
                panel.add(txtFom, "cell 3 1,growx");
                txtFom.setColumns(10);
            }
            {
                chckbxPrevZeroSearch = new JCheckBox("Prev Zero Search");
                panel.add(chckbxPrevZeroSearch, "cell 4 1");
            }
            {
                JLabel lblDensitygcm = new JLabel("Density (g/cm3)=");
                panel.add(lblDensitygcm, "cell 0 2,alignx trailing");
            }
            {
                txtDensity = new JTextField();
                txtDensity.setText("0");
                panel.add(txtDensity, "cell 1 2,growx");
                txtDensity.setColumns(10);
            }
            {
                JLabel lblImpurities = new JLabel("Spurious=");
                panel.add(lblImpurities, "cell 2 2,alignx trailing");
            }
            {
                txtSpurious = new JTextField();
                txtSpurious.setText("0");
                panel.add(txtSpurious, "cell 3 2,growx");
                txtSpurious.setColumns(10);
            }
            {
                chckbxDicvolOpt = new JCheckBox("Dicvol06 opt");
                panel.add(chckbxDicvolOpt, "cell 4 2");
            }
            {
                JLabel lblDensityerr = new JLabel("Density Desv=");
                panel.add(lblDensityerr, "cell 0 3,alignx trailing");
            }
            {
                txtDensityerr = new JTextField();
                txtDensityerr.setText("0");
                panel.add(txtDensityerr, "cell 1 3,growx");
                txtDensityerr.setColumns(10);
            }
        }
        {
        	panel_1 = new JPanel();
        	contentPanel.add(panel_1, "cell 0 4 3 1,grow");
        	panel_1.setLayout(new MigLayout("", "[][][]", "[]"));
        	{
        		lblMethod = new JLabel("Method:");
        		panel_1.add(lblMethod, "cell 0 0");
        	}
        	{
        		rdbtnGridSearch = new JRadioButton("Grid Search");
        		buttonGroupMethod.add(rdbtnGridSearch);
        		panel_1.add(rdbtnGridSearch, "cell 1 0");
        	}
        	{
        		rdbtnDicothomy = new JRadioButton("Dicothomy");
        		rdbtnDicothomy.setSelected(true);
        		buttonGroupMethod.add(rdbtnDicothomy);
        		panel_1.add(rdbtnDicothomy, "cell 2 0");
        	}
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            this.indexDialog.getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("Save DIC");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        do_okButton_actionPerformed(e);
                    }
                });
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                this.indexDialog.getRootPane().setDefaultButton(okButton);
            }
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                	public void actionPerformed(ActionEvent e) {
                        do_cancelButton_actionPerformed(e);
                    }
                });
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }
        }
        
        inicia();
    }

    private void inicia(){
        if (this.npeaks>ds.getNpeaks())this.npeaks=ds.getNpeaks();
        txtNpeaks.setText(String.valueOf(this.npeaks));
        txtAmax.setText(FileUtils.dfX_2.format(this.amax));
        txtBmax.setText(FileUtils.dfX_2.format(this.bmax));
        txtCmax.setText(FileUtils.dfX_2.format(this.cmax));
        txtAmin.setText(FileUtils.dfX_2.format(this.amin));
        txtBmin.setText(FileUtils.dfX_2.format(this.bmin));
        txtCmin.setText(FileUtils.dfX_2.format(this.cmin));
        txtAlfaMin.setText(FileUtils.dfX_2.format(this.alfamin));
        txtAlfaMax.setText(FileUtils.dfX_2.format(this.alfamax));
        txtBetamin.setText(FileUtils.dfX_2.format(this.betamin));
        txtBetamax.setText(FileUtils.dfX_2.format(this.betamax));
        txtGaMin.setText(FileUtils.dfX_2.format(this.gammamin));
        txtGaMax.setText(FileUtils.dfX_2.format(this.gammamax));
        txtAstep.setText(FileUtils.dfX_2.format(this.astep));
        txtBstep.setText(FileUtils.dfX_2.format(this.bstep));
        txtCstep.setText(FileUtils.dfX_2.format(this.cstep));
        txtAlstep.setText(FileUtils.dfX_2.format(this.alstep));
        txtBestep.setText(FileUtils.dfX_2.format(this.bestep));
        txtGastep.setText(FileUtils.dfX_2.format(this.gastep));
        txtVmin.setText(FileUtils.dfX_2.format(this.vmin));
        txtVmax.setText(FileUtils.dfX_2.format(this.vmax));
//        if (this.getWavel()<0){
//            //mirem el de la dataserie
//            txtWave.setText(FileUtils.dfX_5.format(ds.getWavelength()));
//        }else{
//            txtWave.setText(FileUtils.dfX_5.format(this.getWavel()));    
//        }

        txtMw.setText(FileUtils.dfX_2.format(this.mw));
        txtDensity.setText(FileUtils.dfX_2.format(this.density));
        txtDensityerr.setText(FileUtils.dfX_2.format(this.densityerr));
        txtEps.setText(FileUtils.dfX_2.format(this.eps));
        txtFom.setText(FileUtils.dfX_2.format(this.minfom));
        txtSpurious.setText(String.valueOf(this.spurious));
        
        //checkboxes
        chckbxCubic.setSelected(this.cubic);
        chckbxTetragonal.setSelected(this.tetra);
        chckbxHexagonal.setSelected(this.hexa);
        chckbxOrthorhombic.setSelected(this.orto);
        chckbxMonoclinic.setSelected(this.mono);
        chckbxTriclinic.setSelected(this.tric);
        chckbxZeroRefienment.setSelected(this.zeroRef);
        chckbxPrevZeroSearch.setSelected(this.prevzero);
        
//        everythingOK = true;
    }

    private void updateDS(DataSerie newds){
        this.ds=newds;
        inicia();
    }
    
    private void tanca() {
    	this.indexDialog.dispose();
    }
    
    protected void do_cancelButton_actionPerformed(ActionEvent e) {
        this.tanca();
    }
    protected void do_okButton_actionPerformed(ActionEvent e) {
        //PARSE ALL
    	boolean everythingOK=true;
        try{
        	this.amax=Float.parseFloat(txtAmax.getText());
        }catch(Exception ex){
            log.warning("Error reading amax");
            everythingOK=false;
        }
        try{
            this.amin=Float.parseFloat(txtAmin.getText());
        }catch(Exception ex){
            log.warning("Error reading amin");
            everythingOK=false;
        }
        try{
            this.astep=Float.parseFloat(txtAstep.getText());
        }catch(Exception ex){
            log.warning("Error reading astep");
            everythingOK=false;
        }
        try{
            this.bmax=Float.parseFloat(txtBmax.getText());
        }catch(Exception ex){
            log.warning("Error reading bmax");
            everythingOK=false;
        }
        try{
            this.bmin=Float.parseFloat(txtBmin.getText());
        }catch(Exception ex){
            log.warning("Error reading bmin");
            everythingOK=false;
        }
        try{
            this.bstep=Float.parseFloat(txtBstep.getText());
        }catch(Exception ex){
            log.warning("Error reading bstep");
            everythingOK=false;
        }
        try{
            this.cmax=Float.parseFloat(txtCmax.getText());
        }catch(Exception ex){
            log.warning("Error reading cmax");
            everythingOK=false;
        }
        try{
            this.cmin=Float.parseFloat(txtCmin.getText());
        }catch(Exception ex){
            log.warning("Error reading cmin");
            everythingOK=false;
        }
        try{
            this.cstep=Float.parseFloat(txtCstep.getText());
        }catch(Exception ex){
            log.warning("Error reading cstep");
            everythingOK=false;
        }
        try{
            this.alfamin=(Float.parseFloat(txtAlfaMin.getText()));
        }catch(Exception ex){
            log.warning("Error reading Alfa min");
            everythingOK=false;
        }
        try{
            this.alfamax=(Float.parseFloat(txtAlfaMax.getText()));
        }catch(Exception ex){
            log.warning("Error reading Alfa max");
            everythingOK=false;
        }
        try{
            this.alstep=Float.parseFloat(txtAlstep.getText());
        }catch(Exception ex){
            log.warning("Error reading alfa step");
            everythingOK=false;
        }
        try{
            this.betamin=Float.parseFloat(txtBetamin.getText());
        }catch(Exception ex){
            log.warning("Error reading Beta min");
            everythingOK=false;
        }
        try{
            this.betamax=Float.parseFloat(txtBetamax.getText());
        }catch(Exception ex){
            log.warning("Error reading Beta max");
            everythingOK=false;
        }
        try{
            this.bestep=Float.parseFloat(txtBestep.getText());
        }catch(Exception ex){
            log.warning("Error reading beta step");
            everythingOK=false;
        }
        try{
            this.gammamin=(Float.parseFloat(txtGaMin.getText()));
        }catch(Exception ex){
            log.warning("Error reading Gamma min");
            everythingOK=false;
        }
        try{
            this.gammamax=(Float.parseFloat(txtGaMax.getText()));
        }catch(Exception ex){
            log.warning("Error reading Gamma max");
            everythingOK=false;
        }
        try{
            this.gastep=Float.parseFloat(txtGastep.getText());
        }catch(Exception ex){
            log.warning("Error reading gamma step");
            everythingOK=false;
        }
        try{
            this.vmin=Float.parseFloat(txtVmin.getText());
        }catch(Exception ex){
            log.warning("Error reading vmin");
            everythingOK=false;
        }
        try{
            this.vmax=Float.parseFloat(txtVmax.getText());
        }catch(Exception ex){
            log.warning("Error reading vmax");
            everythingOK=false;
        }
        try{
            this.eps=Float.parseFloat(txtEps.getText());
        }catch(Exception ex){
            log.warning("Error reading Eps");
            everythingOK=false;
        }
        try{
            this.minfom=Float.parseFloat(txtFom.getText());
        }catch(Exception ex){
            log.warning("Error reading min FOM");
            everythingOK=false;
        }
        
        try{
            this.npeaks=Integer.parseInt(txtNpeaks.getText());
        }catch(Exception ex){
            log.warning("Error reading Npeaks");
            everythingOK=false;
        }
        try{
            this.spurious=Integer.parseInt(txtSpurious.getText());
        }catch(Exception ex){
            log.warning("Error reading Spurious");
            everythingOK=false;
        }
        
        //TODO:comprovacio de wavelengh dins de DS
        
        //checkboxes
        this.cubic=chckbxCubic.isSelected();
        this.tetra=chckbxTetragonal.isSelected();
        this.hexa=chckbxHexagonal.isSelected();
        this.orto=chckbxOrthorhombic.isSelected();
        this.mono=chckbxMonoclinic.isSelected();
        this.tric=chckbxTriclinic.isSelected();
        this.zeroRef=chckbxZeroRefienment.isSelected();
        this.prevzero=chckbxPrevZeroSearch.isSelected();
        
        if (!everythingOK) {
        	//TODO: complain message
        	log.info("error reading indexing parameters");
        	return;
        }
        
        //METHOD SELECTION AND START INDEXING
        if (rdbtnGridSearch.isSelected()) {
        	this.executeGridSearch();
        }
        
        if (rdbtnDicothomy.isSelected()) {
        	IndexDichotomy id = new IndexDichotomy(ds,this.npeaks,this.eps);
        	id.setMaxPars(this.amax, this.bmax, this.cmax, this.alfamax, this.betamax, this.gammamax, true);
        	id.setMinPars(this.amin, this.bmin, this.cmin, this.alfamin, this.betamin, this.gammamin, true);
        	id.setSystemsSearch(this.cubic, this.tetra, this.hexa, this.orto, this.mono, this.tric);
        	this.executeDicothomy(id);
        }
        

       
        
    }
  
    //TODO afegir STOP button
    private void executeGridSearch() {
    	
    	float[] avals = FileUtils.arange(this.amin, this.amax, this.astep);
        float[] bvals = FileUtils.arange(this.bmin, this.bmax, this.bstep);
        float[] cvals = FileUtils.arange(this.cmin, this.cmax, this.cstep);
        float[] alvals = FileUtils.arange((float) FastMath.toRadians(this.alfamin), (float) FastMath.toRadians(this.alfamax), (float) FastMath.toRadians(this.alstep));
        if (alvals.length==0) {
        	alvals = new float[] {(float) FastMath.toRadians(this.alfamin)};
        }
        float[] bevals = FileUtils.arange((float) FastMath.toRadians(this.betamin), (float) FastMath.toRadians(this.betamax), (float) FastMath.toRadians(this.bestep));
        if (bevals.length==0) {
        	bevals = new float[] {(float) FastMath.toRadians(this.betamin)};
        }
        float[] gavals = FileUtils.arange((float) FastMath.toRadians(this.gammamin), (float) FastMath.toRadians(this.gammamax), (float) FastMath.toRadians(this.gastep));
        if (gavals.length==0) {
        	gavals = new float[] {(float) FastMath.toRadians(this.gammamin)};
        }
        
        int totalComb = avals.length*bvals.length*cvals.length*alvals.length*bevals.length*gavals.length;
        float hpercent = (float)totalComb/200.f;
        log.info(String.format("%d combinations of parameters will be evaluated! (%d*%d*%d*%d*%d*%d)", totalComb,avals.length,bvals.length,cvals.length,alvals.length,bevals.length,gavals.length));
        
        //ara llista dobs dels pics seleccionats
        float[] dobs = ds.getListPeaksDsp(); //TODO IMPLEMENTAR LIMIT DE PICS
        float hsqmin = 1/ds.getMinPeakDsp();
        float factor = (float) (2*FastMath.sqrt(hsqmin));
        
        //TODO aqui hauriem de partir problema per fer multithreading cridant la funcio index per cada thread
        
        
//        this.executeGridSearch(dobs, factor, this.getWavel(), avals, bvals, cvals, alvals, bevals, gavals, 1);
        
//        PattOps.index(dobs, factor, avals, bvals, cvals, alvals, bevals, gavals, 1);
        
        //DEBUG (aixo ho farem a pattops
//      int[] lengths = new int[] { avals.length, bvals.length, cvals.length, alvals.length, bevals.length, gavals.length };
//        int count = 0;
//        for (int[] indices : new CartesianProduct(lengths)) {
//        	System.out.println(count + " " + Arrays.toString(indices) //
//        			+ " " + avals[indices[0]] //
//        					+ ", " + bvals[indices[1]] //
//        							+ ", " + cvals[indices[2]]
//        									+ ", " + alvals[indices[3]]
//        											+ ", " + bevals[indices[4]]
//        													+ ", " + gavals[indices[5]]);
//        	count++;
//        }
//        System.out.println("total "+count+" combinations (should be "+totalComb+")");
    	
        pm = new ProgressMonitor(this.indexDialog,
                "Indexing...",
                "", 0, 100);
        pm.setProgress(0);
        gridSwk = new IndexGridSearchBruteWorker(dobs, factor, wavel, avals, bvals, cvals, alvals, bevals, gavals, 1);
        gridSwk.addPropertyChangeListener(new PropertyChangeListener() {
    
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                log.debug(evt.getPropertyName());
                if ("progress" == evt.getPropertyName() ) {
                    int progress = (Integer) evt.getNewValue();
                    pm.setProgress(progress);
                    pm.setNote(String.format("%d%%\n", progress));
                    if (pm.isCanceled() || gridSwk.isDone()) {
                        Toolkit.getDefaultToolkit().beep();
                        if (pm.isCanceled()) {
                            gridSwk.cancel(true);
                            log.info("Sum canceled");
                        } else {
                            log.info("Sum finished!!");
                        }
                        pm.close();
                    }
                }
                if (gridSwk.isDone()){
                	log.debug("finished indexing worker");
//                	fillSolucions(gridSwk.getSolucions());
                	//TODO afegir llista solucions per plotejar les posicions de les reflexions i executar d'ajust per fer fit!! PATTERN MATCHING 
                	
//                    Pattern2D suma = sumwk.getpattSum();
//                    if (suma==null){
//                        log.warning("Error summing files");
//                        return;
//                    }else{
//                        suma.recalcMaxMinI();
//                        suma.calcMeanI();
//                        suma.recalcExcludedPixels();
//                        updatePatt2D(suma,true,true);    
//                    }
                }
            }
        });
        gridSwk.execute();
    }

    private void executeDicothomy(IndexDichotomy id) {
//    	float incPar = 0.5f;
//    	float incAng = 1.0f;
//        float[] Qobs = ds.getListPeaksQ(); //TODO IMPLEMENTAR LIMIT DE PICS
//        int numIter = 6;
//
//        //to improve factor
////        float[] dobs = ds.getListPeaksDsp(); //TODO IMPLEMENTAR LIMIT DE PICS
//        float hsqmin = 1/ds.getMinPeakDsp();
//        float factor = (float) (2*FastMath.sqrt(hsqmin));
//        
//        IndexCell ic = new IndexCell(this.amin,this.bmin,this.cmin,this.alfamin,this.betamin,this.gammamin,false);
//        ic.setMaxCell(this.amax,this.bmax,this.cmax,this.alfamax,this.betamax,this.gammamax,false);
//        
//        float[] avals = ic.getAVals(incPar);
//        float[] bvals = ic.getBVals(incPar);
//        float[] cvals = ic.getCVals(incPar);
//        float[] alvals = ic.getAlVals(incAng);
//        float[] bevals = ic.getBeVals(incAng);
//        float[] gavals = ic.getGaVals(incAng);
//        
        pm = new ProgressMonitor(this.indexDialog,"Indexing...","", 0, 100);
        pm.setProgress(0);
        dicW = new DichothomyWorker(id);
        dicW.addPropertyChangeListener(new PropertyChangeListener() {
    
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                log.debug(evt.getPropertyName());
                if ("progress" == evt.getPropertyName() ) {
                    int progress = (Integer) evt.getNewValue();
                    pm.setProgress(progress);
                    pm.setNote(String.format("%d%%\n", progress));
                    if (pm.isCanceled() || dicW.isDone()) {
                        Toolkit.getDefaultToolkit().beep();
                        if (pm.isCanceled()) {
                        	dicW.cancel(true);
                            log.info("Indexing canceled");
                        } else {
                            log.info("Indexing finished!!");
                        }
                        pm.close();
                    }
                }
                if (dicW.isDone()){
                	log.debug("finished indexing worker");
                	fillSolucions(dicW.getSolutions());
                	//TODO afegir llista solucions per plotejar les posicions de les reflexions i executar d'ajust per fer fit!! PATTERN MATCHING 
                	
//                    Pattern2D suma = sumwk.getpattSum();
//                    if (suma==null){
//                        log.warning("Error summing files");
//                        return;
//                    }else{
//                        suma.recalcMaxMinI();
//                        suma.calcMeanI();
//                        suma.recalcExcludedPixels();
//                        updatePatt2D(suma,true,true);    
//                    }
                }
            }
        });
        dicW.execute();
    }

    
    //TODO: CLARAMENT HO HE DE PENSAR MILLOR, podria retornar objecte Indexing (interface que hagin de implementar tots els metodes, amb Qobs,... altres condicions i apart les solucions (que també han de ser un interface??).. un pel confus
    private void fillSolucions(ArrayList<IndexSolution> sols) {
        
//        ArrayList<Cell> refinedCells = new ArrayList<Cell>();
        
        for (IndexSolution s:sols) {
            Cell c = s.getRefinedCell();
            log.info("ref cell= "+c.toStringCellParamOnly());
            double m20 = s.getM20();
            double i20 = s.getI20();
        }

    }
    
    
    public void visible(boolean vis) {
    	this.indexDialog.setVisible(vis);
    }
    
    public static class IndexGridSearchBruteWorker extends SwingWorker<Integer,Integer> {

		@Override
		protected Integer doInBackground() throws Exception {
			sols = PattOps.indexGridSearchBrute(dobs, factor, wave, aL, bL, cL, alL, beL, gaL, threadN);
			return 0;
		}
    	ArrayList<IndexSolutionGrid> sols;
		float[] dobs,aL,bL,cL,alL,beL,gaL;
		float factor,wave;
		int threadN;
		
		public IndexGridSearchBruteWorker(float[] dobs, float factor, float wavel, float[] aL,float[] bL, float[] cL, float[] alL, float[] beL, float[] gaL, int threadN) {
			this.dobs=dobs;
			this.factor=factor;
			this.wave=wavel;
			this.aL=aL;
			this.bL=bL;
			this.cL=cL;
			this.alL=alL;
			this.beL=beL;
			this.gaL=gaL;
			this.threadN=threadN;
		}
		
		public ArrayList<IndexSolutionGrid> getSolucions() {
			return sols;
		}
    }
    
    public static class DichothomyWorker extends SwingWorker<Integer,Integer> {

		@Override
		protected Integer doInBackground() throws Exception {
			
			sols = id.runIndexing(-1, -1, -1); 
			return 0;
		}
		
		IndexDichotomy id;
		ArrayList<IndexSolutionDichotomy> sols;
		
		public DichothomyWorker(IndexDichotomy id) {
			this.id=id;
		}
		
		public ArrayList<IndexSolution> getSolutions(){
		    ArrayList<IndexSolution> iss = new ArrayList<IndexSolution>();
		    for (IndexSolutionDichotomy sol:this.sols) {
		        iss.add(sol.getIS());
		    }
		    return iss;
		}
		
    }
}

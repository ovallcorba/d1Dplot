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
import java.util.Iterator;

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

import com.vava33.d1dplot.auxi.CartesianProduct;
import com.vava33.d1dplot.auxi.DataSerie;
import com.vava33.d1dplot.auxi.IndexResult;
import com.vava33.d1dplot.auxi.PattOps;
import com.vava33.jutils.FileUtils;
import com.vava33.jutils.VavaLogger;

import net.miginfocom.swing.MigLayout;

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
    
    private float amax=5.6f;
    private float bmax=5.6f;
    private float cmax=5.6f;
    private float amin=5.3f;
    private float bmin=5.3f;
    private float cmin=5.3f;
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
    private boolean dic06 = false;
    
    private int npeaks = 20;
    
    private DataSerie ds;
    
    private boolean everythingOK = true;
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

    
	public IndexDialog(JFrame parent, PlotPanel p, DataSerie datas) {
        this.plotpanel=p;
        this.ds=datas;
        this.contentPanel = new JPanel();
        this.indexDialog = new JDialog(parent,"Index",false);
        this.indexDialog.setIconImage(D1Dplot_global.getIcon());
        this.indexDialog.setBounds(100, 100, 700, 500);
        this.indexDialog.getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.indexDialog.getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new MigLayout("", "[grow][grow][]", "[][][][grow]"));
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
        if (this.getNpeaks()>ds.getNpeaks())this.setNpeaks(ds.getNpeaks());
        txtNpeaks.setText(String.valueOf(this.getNpeaks()));
        txtAmax.setText(FileUtils.dfX_2.format(this.getAmax()));
        txtBmax.setText(FileUtils.dfX_2.format(this.getBmax()));
        txtCmax.setText(FileUtils.dfX_2.format(this.getCmax()));
        txtAmin.setText(FileUtils.dfX_2.format(this.amin));
        txtBmin.setText(FileUtils.dfX_2.format(this.bmin));
        txtCmin.setText(FileUtils.dfX_2.format(this.cmin));
        txtAlfaMin.setText(FileUtils.dfX_2.format(this.alfamin));
        txtAlfaMax.setText(FileUtils.dfX_2.format(this.alfamax));
        txtBetamin.setText(FileUtils.dfX_2.format(this.getBetamin()));
        txtBetamax.setText(FileUtils.dfX_2.format(this.getBetamax()));
        txtGaMin.setText(FileUtils.dfX_2.format(this.betamin));
        txtGaMax.setText(FileUtils.dfX_2.format(this.betamax));
        txtAstep.setText(FileUtils.dfX_2.format(this.astep));
        txtBstep.setText(FileUtils.dfX_2.format(this.bstep));
        txtCstep.setText(FileUtils.dfX_2.format(this.cstep));
        txtAlstep.setText(FileUtils.dfX_2.format(this.alstep));
        txtBestep.setText(FileUtils.dfX_2.format(this.bestep));
        txtGastep.setText(FileUtils.dfX_2.format(this.gastep));
        txtVmin.setText(FileUtils.dfX_2.format(this.getVmin()));
        txtVmax.setText(FileUtils.dfX_2.format(this.getVmax()));
        if (this.getWavel()<0){
            //mirem el de la dataserie
            txtWave.setText(FileUtils.dfX_5.format(ds.getWavelength()));
        }else{
            txtWave.setText(FileUtils.dfX_5.format(this.getWavel()));    
        }

        txtMw.setText(FileUtils.dfX_2.format(this.getMw()));
        txtDensity.setText(FileUtils.dfX_2.format(this.getDensity()));
        txtDensityerr.setText(FileUtils.dfX_2.format(this.getDensityerr()));
        txtEps.setText(FileUtils.dfX_2.format(this.getEps()));
        txtFom.setText(FileUtils.dfX_2.format(this.getMinfom()));
        txtSpurious.setText(String.valueOf(this.getSpurious()));
        
        //checkboxes
        chckbxCubic.setSelected(this.isCubic());
        chckbxTetragonal.setSelected(this.isTetra());
        chckbxHexagonal.setSelected(this.isHexa());
        chckbxOrthorhombic.setSelected(this.isOrto());
        chckbxMonoclinic.setSelected(this.isMono());
        chckbxTriclinic.setSelected(this.isTric());
        chckbxZeroRefienment.setSelected(this.isZeroRef());
        chckbxPrevZeroSearch.setSelected(this.isPrevzero());
        chckbxDicvolOpt.setSelected(this.isDic06());
        
        everythingOK = true;
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
        try{
            this.setAmax(Float.parseFloat(txtAmax.getText()));
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
            this.setBmax(Float.parseFloat(txtBmax.getText()));
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
            this.setCmax(Float.parseFloat(txtCmax.getText()));
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
            this.setBetamin(Float.parseFloat(txtBetamin.getText()));
        }catch(Exception ex){
            log.warning("Error reading Beta min");
            everythingOK=false;
        }
        try{
            this.setBetamax(Float.parseFloat(txtBetamax.getText()));
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
            this.setVmin(Float.parseFloat(txtVmin.getText()));
        }catch(Exception ex){
            log.warning("Error reading vmin");
            everythingOK=false;
        }
        try{
            this.setVmax(Float.parseFloat(txtVmax.getText()));
        }catch(Exception ex){
            log.warning("Error reading vmax");
            everythingOK=false;
        }
        try{
            this.setWavel(Float.parseFloat(txtWave.getText()));
        }catch(Exception ex){
            log.warning("Error reading wavelength");
            everythingOK=false;
        }
        
        try{
            this.setMw(Float.parseFloat(txtMw.getText()));
        }catch(Exception ex){
            log.warning("Error reading MW");
            everythingOK=false;
        }
        try{
            this.setDensity(Float.parseFloat(txtDensity.getText()));
        }catch(Exception ex){
            log.warning("Error reading Density");
            everythingOK=false;
        }
        try{
            this.setDensityerr(Float.parseFloat(txtDensityerr.getText()));
        }catch(Exception ex){
            log.warning("Error reading Density Desv");
            everythingOK=false;
        }
        try{
            this.setEps(Float.parseFloat(txtEps.getText()));
        }catch(Exception ex){
            log.warning("Error reading Eps");
            everythingOK=false;
        }
        try{
            this.setMinfom(Float.parseFloat(txtFom.getText()));
        }catch(Exception ex){
            log.warning("Error reading min FOM");
            everythingOK=false;
        }
        
        try{
            this.setNpeaks(Integer.parseInt(txtNpeaks.getText()));
            if (this.getNpeaks()>this.ds.getNpeaks()){
                this.setNpeaks(this.ds.getNpeaks());
            }
        }catch(Exception ex){
            log.warning("Error reading Npeaks");
            everythingOK=false;
        }
        try{
            this.setSpurious(Integer.parseInt(txtSpurious.getText()));
        }catch(Exception ex){
            log.warning("Error reading Spurious");
            everythingOK=false;
        }
        
        //checkboxes
        this.setCubic(chckbxCubic.isSelected());
        this.setTetra(chckbxTetragonal.isSelected());
        this.setHexa(chckbxHexagonal.isSelected());
        this.setOrto(chckbxOrthorhombic.isSelected());
        this.setMono(chckbxMonoclinic.isSelected());
        this.setTric(chckbxTriclinic.isSelected());
        this.setZeroRef(chckbxZeroRefienment.isSelected());
        this.setPrevzero(chckbxPrevZeroSearch.isSelected());
        this.setDic06(chckbxDicvolOpt.isSelected());
        
        if (!everythingOK) {
        	//TODO: complain message
        	log.info("error reading indexing parameters");
        	return;
        }
        
        //NOW WE START INDEXING
        float[] avals = PattOps.arange(this.amin, this.amax, this.astep);
        float[] bvals = PattOps.arange(this.bmin, this.bmax, this.bstep);
        float[] cvals = PattOps.arange(this.cmin, this.cmax, this.cstep);
        float[] alvals = PattOps.arange((float) FastMath.toRadians(this.alfamin), (float) FastMath.toRadians(this.alfamax), (float) FastMath.toRadians(this.alstep));
        if (alvals.length==0) {
        	alvals = new float[] {(float) FastMath.toRadians(this.alfamin)};
        }
        float[] bevals = PattOps.arange((float) FastMath.toRadians(this.betamin), (float) FastMath.toRadians(this.betamax), (float) FastMath.toRadians(this.bestep));
        if (bevals.length==0) {
        	bevals = new float[] {(float) FastMath.toRadians(this.betamin)};
        }
        float[] gavals = PattOps.arange((float) FastMath.toRadians(this.gammamin), (float) FastMath.toRadians(this.gammamax), (float) FastMath.toRadians(this.gastep));
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
        
        
        this.execute(dobs, factor, this.getWavel(), avals, bvals, cvals, alvals, bevals, gavals, 1);
        
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
       
        
    }
    ProgressMonitor pm;
    indexWorker sumwk;
    //TODO afegir STOP button
    private void execute(float[] dobs, float factor, float wavel, float[] aL,float[] bL, float[] cL, float[] alL, float[] beL, float[] gaL, int threadN) {
        pm = new ProgressMonitor(this.indexDialog,
                "Indexing...",
                "", 0, 100);
        pm.setProgress(0);
        sumwk = new indexWorker(dobs, factor, wavel, aL, bL, cL, alL, beL, gaL, 1);
        sumwk.addPropertyChangeListener(new PropertyChangeListener() {
    
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                log.debug(evt.getPropertyName());
                if ("progress" == evt.getPropertyName() ) {
                    int progress = (Integer) evt.getNewValue();
                    pm.setProgress(progress);
                    pm.setNote(String.format("%d%%\n", progress));
                    if (pm.isCanceled() || sumwk.isDone()) {
                        Toolkit.getDefaultToolkit().beep();
                        if (pm.isCanceled()) {
                            sumwk.cancel(true);
                            log.info("Sum canceled");
                        } else {
                            log.info("Sum finished!!");
                        }
                        pm.close();
                    }
                }
                if (sumwk.isDone()){
                	log.debug("finished indexing worker");
                	fillSolucions();
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
        sumwk.execute();
    }

    private void fillSolucions() {
    	ArrayList<IndexResult.indexSolucio> sols = sumwk.getSolucions();
    	Iterator<IndexResult.indexSolucio> itrS = sols.iterator();
    	int i = 0;
    	while (itrS.hasNext()) {
    		IndexResult.indexSolucio is = itrS.next();
    		//TODO print and fill table to select solution to plot?
    		log.info(String.format("sol %d: %s", i+1,is.toString()));
    		
    		//també afegim la llista d'hkl per aquesta solucio
    		
            int[] hs = PattOps.range(is.hmin, is.hmax, 1);
            int[] ks = PattOps.range(is.kmin, is.kmax, 1);
            int[] ls = PattOps.range(is.lmin, is.lmax, 1);
            
            int[] lenHKL = new int[] { hs.length, ks.length, ls.length };
//            log.info(String.format("sol: %f %f %f %f %f %f", is.a, is.b, is.c, is.al, is.be, is.ga));
            for (int[] indexHKL : new CartesianProduct(lenHKL)) {
            	int h = hs[indexHKL[0]];
            	int k = ks[indexHKL[1]];
            	int l = ls[indexHKL[2]];
            	
            	if ((h==0)&&(k==0)&&(l==0))continue;
            	
            	float dsp = PattOps.dspFromHKL(is.a, is.b, is.c, is.al, is.be, is.ga, h, k, l);
            	
//            	log.info(String.format("%d %d %d dcalc=%f",h,k,l,dsp)); //fins aquí dcalc esta be... igual que al calcul anterior
            	
            	is.addRef(h, k, l, dsp, this.getWavel());
            }
    		i++;
    	}
    	//test for now plot only the first one
    	ds.getPatt1D().addDataSerie(sols.get(0).hkls);
//    	plotpanel.actualitzaPlot();
    	plotpanel.getMainframe().updateData(false);
    	
//		log.info(String.format("sol %d: %8.4f %8.4f %8.4f %6.2f %6.2f %6.2f res=%8.4f", i+1, sols[i][0],sols[i][1],sols[i][2],sols[i][3],sols[i][4],sols[i][5],sols[i][6]));

    }
    
    private float getAmax() {
        return amax;
    }

    private void setAmax(float amax) {
        this.amax = amax;
    }

    private float getBmax() {
        return bmax;
    }

    private void setBmax(float bmax) {
        this.bmax = bmax;
    }

    private float getCmax() {
        return cmax;
    }

    private void setCmax(float cmax) {
        this.cmax = cmax;
    }

    private float getBetamin() {
        return betamin;
    }

    private void setBetamin(float betamin) {
        this.betamin = betamin;
    }

    private float getBetamax() {
        return betamax;
    }

    private void setBetamax(float betamax) {
        this.betamax = betamax;
    }

    private float getVmin() {
        return vmin;
    }

    private void setVmin(float vmin) {
        this.vmin = vmin;
    }

    private float getVmax() {
        return vmax;
    }

    private void setVmax(float vmax) {
        this.vmax = vmax;
    }

    private float getWavel() {
        return wavel;
    }

    private void setWavel(float wavel) {
        this.wavel = wavel;
    }

    private float getMw() {
        return mw;
    }

    private void setMw(float mw) {
        this.mw = mw;
    }

    private float getDensity() {
        return density;
    }

    private void setDensity(float density) {
        this.density = density;
    }

    private float getDensityerr() {
        return densityerr;
    }

    private void setDensityerr(float densityerr) {
        this.densityerr = densityerr;
    }

    private float getEps() {
        return eps;
    }

    private void setEps(float eps) {
        this.eps = eps;
    }

    private float getMinfom() {
        return minfom;
    }

    private void setMinfom(float minfom) {
        this.minfom = minfom;
    }

    private int getSpurious() {
        return spurious;
    }

    private void setSpurious(int spurious) {
        this.spurious = spurious;
    }

    private boolean isCubic() {
        return cubic;
    }
    private int isCubicInt() {
        if (cubic) return 1;
        return 0;
    }

    private void setCubic(boolean cubic) {
        this.cubic = cubic;
    }

    private boolean isTetra() {
        return tetra;
    }
    private int isTetraInt() {
        if (tetra) return 1;
        return 0;
    }

    private void setTetra(boolean tetra) {
        this.tetra = tetra;
    }

    private boolean isHexa() {
        return hexa;
   }
    private int isHexaInt() {
        if (hexa) return 1;
        return 0;
    }

    private void setHexa(boolean hexa) {
        this.hexa = hexa;
    }

    private boolean isOrto() {
        return orto;
    }
    private int isOrtoInt() {
        if (orto) return 1;
        return 0;
    }
    
    private void setOrto(boolean orto) {
        this.orto = orto;
    }

    private boolean isMono() {
        return mono;
    }
    private int isMonoInt() {
        if (mono) return 1;
        return 0;
    }
    
    private void setMono(boolean mono) {
        this.mono = mono;
    }

    private boolean isTric() {
        return tric;
    }
    private int isTricInt() {
        if (tric) return 1;
        return 0;
    }
    
    private void setTric(boolean tric) {
        this.tric = tric;
    }

    private boolean isZeroRef() {
        return zeroRef;
    }
    private int isZeroRefInt() {
        if (zeroRef) return 1;
        return 0;
    }

    private void setZeroRef(boolean zeroRef) {
        this.zeroRef = zeroRef;
    }

    private boolean isPrevzero() {
        return prevzero;
    }
    private int isPrevzeroInt() {
        if (prevzero) return 1;
        return 0;
    }

    private void setPrevzero(boolean prevzero) {
        this.prevzero = prevzero;
    }

    private boolean isDic06() {
        return dic06;
    }
    private int isDic06Int() {
        if (dic06) return 1;
        return 0;
    }

    private void setDic06(boolean dic06) {
        this.dic06 = dic06;
    }

    private int getNpeaks() {
        return npeaks;
    }

    private void setNpeaks(int npeaks) {
        this.npeaks = npeaks;
    }

    private boolean isEverythingOK() {
        return everythingOK;
    }

    private void setEverythingOK(boolean everythingOK) {
        this.everythingOK = everythingOK;
    }
    
    public void visible(boolean vis) {
    	this.indexDialog.setVisible(vis);
    }
    
    public static class indexWorker extends SwingWorker<Integer,Integer> {

		@Override
		protected Integer doInBackground() throws Exception {
			sols = PattOps.index(dobs, factor, wave, aL, bL, cL, alL, beL, gaL, threadN);
			return 0;
		}
    	ArrayList<IndexResult.indexSolucio> sols;
		float[] dobs,aL,bL,cL,alL,beL,gaL;
		float factor,wave;
		int threadN;
		
		public indexWorker(float[] dobs, float factor, float wavel, float[] aL,float[] bL, float[] cL, float[] alL, float[] beL, float[] gaL, int threadN) {
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
		
		public ArrayList<IndexResult.indexSolucio> getSolucions() {
			return sols;
		}
    }
}

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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.math3.util.FastMath;

import com.vava33.cellsymm.Cell;
import com.vava33.cellsymm.SpaceGroup;
import com.vava33.cellsymm.CellSymm_global.CrystalCentering;
import com.vava33.cellsymm.CellSymm_global.CrystalFamily;
import com.vava33.d1dplot.auxi.PattOps;
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.d1dplot.data.Data_Common;
import com.vava33.d1dplot.data.SerieType;
import com.vava33.d1dplot.index.IndexDichotomy;
import com.vava33.d1dplot.index.IndexGrid;
import com.vava33.d1dplot.index.IndexSolution;
import com.vava33.d1dplot.index.IndexSolutionDichotomy;
import com.vava33.d1dplot.index.IndexSolutionGrid;
import com.vava33.jutils.FileUtils;
import com.vava33.jutils.VavaLogger;

import net.miginfocom.swing.MigLayout;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import java.awt.Font;
import javax.swing.JSplitPane;
import javax.swing.JProgressBar;

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
    private JTextField txtEps;
    private JTextField txtFom;
    private JTextField txtSpurious;
    private JCheckBox chckbxCubic;
    private JCheckBox chckbxTetragonal;
    private JCheckBox chckbxHexagonal;
    private JCheckBox chckbxOrthorhombic;
    private JCheckBox chckbxMonoclinic;
    private JCheckBox chckbxTriclinic;
    
    private float amax=25.0f;
    private float bmax=25.0f;
    private float cmax=25.0f;
    private float amin=1.0f;
    private float bmin=1.0f;
    private float cmin=1.0f;
    private float alfamin=90.0f;
    private float alfamax=120.0f;
    private float betamin=90.0f;
    private float betamax=120.0f;
    private float gammamin=90.0f;
    private float gammamax=120.0f;
    private float astep = 0.1f;
    private float bstep = 0.1f;
    private float cstep = 0.1f;
    private float alstep = 0.5f;
    private float bestep = 0.5f;
    private float gastep = 0.5f;
    
    private float vmin=0.0f;
    private float vmax=3000.0f;
    
//    private float wavel = -1.0f;
//    private float mw = 0.0f;
//    private float density = 0.0f;
//    private float densityerr = 0.0f;
    
    private float eps = 0.01f;
    private float minfom = 10.0f;
    private int spurious = 0;
    
    private boolean cubic = true;
    private boolean tetra = true;
    private boolean hexa = true;
    private boolean orto = true;
    private boolean mono = true;
    private boolean tric = false;
    
//    private boolean zeroRef = true;
//    private boolean prevzero = false;
//    private boolean dic06 = false;
    private int npeaks = 20;
    
    ProgressMonitor pm;
    IndexGridSearchBruteWorker gridSwk;
    DichothomyWorker dicW;
    Set<IndexSolution> sols;
    Set<IndexSolution> original_sols;
    IndexGuessSpaceGroupDialog sgGuessDialog;
    debug_latgen manualIndexDialog;
//    private boolean everythingOK = true;
    private JLabel lblMin;
    private JLabel lblMin_1;
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
    private JLabel lblStep;
    private JTextField txtAstep;
    private JTextField txtBstep;
    private JTextField txtCstep;
    private JTextField txtAlstep;
    private JTextField txtBestep;
    private JTextField txtGastep;
    private JPanel panel_1;
    private JRadioButton rdbtnGridSearch;
    private JRadioButton rdbtnDicothomy;
    private final ButtonGroup buttonGroupMethod = new ButtonGroup();
    private JButton btnSaveResults;
    private JLabel lblaMin_1;
    private JLabel label;
    private JLabel label_1;
    private JLabel lblStepgrid;
    private JSeparator separator;
    private JPanel panel_2;
    private JTable tableSols;
    private JScrollPane scrollPane;
    private JTextArea txtrRefs;
    private JScrollPane scrollPane_1;
    private JButton btnAddAsDataserie;
    private JButton btnGuessSg;
    private JSplitPane splitPane;
    
	public IndexDialog(JFrame parent, PlotPanel p) {
        this.plotpanel=p;
        this.contentPanel = new JPanel();
        this.indexDialog = new JDialog(parent,"Index",false);
        this.indexDialog.setIconImage(D1Dplot_global.getIcon());
        this.indexDialog.setBounds(100, 100, 925, 665); //TODO CANVIAR A LA MIDA BONA
        this.indexDialog.getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.indexDialog.getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new MigLayout("", "[grow][grow]", "[][][][grow]"));
        {
            JPanel panel = new JPanel();
            panel.setBorder(new TitledBorder(null, "Crystal Systems", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            contentPanel.add(panel, "cell 0 0,grow");
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
            panel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Cell dimensions limits", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
            contentPanel.add(panel, "cell 0 1,grow");
            panel.setLayout(new MigLayout("", "[][grow][grow][grow][][][grow][grow][grow]", "[][][][][]"));
            {
            	lblaMin = new JLabel("min");
            	panel.add(lblaMin, "cell 1 0,alignx center");
            }
            {
                JLabel lblAMax = new JLabel("max");
                panel.add(lblAMax, "cell 2 0,alignx center");
            }
            {
            	lblStep = new JLabel("step (grid)");
            	panel.add(lblStep, "cell 3 0,alignx center");
            }
            {
                label = new JLabel("min");
                panel.add(label, "cell 6 0,alignx center");
            }
            {
                label_1 = new JLabel("max");
                panel.add(label_1, "cell 7 0,alignx center");
            }
            {
                lblStepgrid = new JLabel("step (grid)");
                panel.add(lblStepgrid, "cell 8 0,alignx center");
            }
            {
                lblaMin_1 = new JLabel("a (Å)");
                panel.add(lblaMin_1, "cell 0 1,alignx trailing");
            }
            {
            	txtAmin = new JTextField();
            	txtAmin.setText("25.0");
            	txtAmin.setColumns(6);
            	panel.add(txtAmin, "cell 1 1,growx");
            }
            {
                txtAmax = new JTextField();
                txtAmax.setText("25.0");
                panel.add(txtAmax, "cell 2 1,growx");
                txtAmax.setColumns(6);
            }
            {
            	txtAstep = new JTextField();
            	panel.add(txtAstep, "cell 3 1,growx");
            	txtAstep.setColumns(6);
            }
            {
                separator = new JSeparator();
                separator.setOrientation(SwingConstants.VERTICAL);
                panel.add(separator, "cell 4 1 1 3,growy");
            }
            {
            	lblMin = new JLabel("α (º)");
            	panel.add(lblMin, "cell 5 1,alignx trailing");
            }
            {
            	txtAlfaMin = new JTextField();
            	panel.add(txtAlfaMin, "cell 6 1,growx");
            	txtAlfaMin.setColumns(6);
            }
            {
            	txtAlfaMax = new JTextField();
            	txtAlfaMax.setColumns(6);
            	panel.add(txtAlfaMax, "cell 7 1,growx");
            }
            {
            	txtAlstep = new JTextField();
            	panel.add(txtAlstep, "cell 8 1,growx");
            	txtAlstep.setColumns(6);
            }
            {
            	lblbMin = new JLabel("b (Å)");
            	panel.add(lblbMin, "cell 0 2,alignx trailing");
            }
            {
            	txtBmin = new JTextField();
            	panel.add(txtBmin, "cell 1 2,growx");
            	txtBmin.setColumns(6);
            }
            {
                txtBmax = new JTextField();
                txtBmax.setText("25.0");
                panel.add(txtBmax, "cell 2 2,growx");
                txtBmax.setColumns(6);
            }
            {
            	txtBstep = new JTextField();
            	panel.add(txtBstep, "cell 3 2,growx");
            	txtBstep.setColumns(6);
            }
            {
                JLabel lblBetaMin = new JLabel("β (º)");
                panel.add(lblBetaMin, "cell 5 2,alignx trailing");
            }
            {
                txtBetamin = new JTextField();
                txtBetamin.setText("90.0");
                panel.add(txtBetamin, "cell 6 2,growx");
                txtBetamin.setColumns(6);
            }
            {
                txtBetamax = new JTextField();
                txtBetamax.setText("125.0");
                panel.add(txtBetamax, "cell 7 2,growx");
                txtBetamax.setColumns(6);
            }
            {
            	txtBestep = new JTextField();
            	panel.add(txtBestep, "cell 8 2,growx");
            	txtBestep.setColumns(6);
            }
            {
            	lblcMin = new JLabel("c (Å)");
            	panel.add(lblcMin, "cell 0 3,alignx trailing");
            }
            {
            	txtCmin = new JTextField();
            	panel.add(txtCmin, "cell 1 3,growx");
            	txtCmin.setColumns(6);
            }
            {
                txtCmax = new JTextField();
                txtCmax.setText("25.0");
                panel.add(txtCmax, "cell 2 3,growx");
                txtCmax.setColumns(6);
            }
            {
            	txtCstep = new JTextField();
            	panel.add(txtCstep, "cell 3 3,growx");
            	txtCstep.setColumns(6);
            }
            {
            	lblMin_1 = new JLabel("γ (º)");
            	panel.add(lblMin_1, "cell 5 3,alignx trailing");
            }
            {
            	txtGaMin = new JTextField();
            	panel.add(txtGaMin, "cell 6 3,growx");
            	txtGaMin.setColumns(6);
            }
            {
            	txtGaMax = new JTextField();
            	panel.add(txtGaMax, "cell 7 3,growx");
            	txtGaMax.setColumns(6);
            }
            {
            	txtGastep = new JTextField();
            	panel.add(txtGastep, "cell 8 3,growx");
            	txtGastep.setColumns(6);
            }
            {
                JLabel lblVMin = new JLabel("V (Å3)");
                panel.add(lblVMin, "cell 0 4,alignx trailing");
            }
            {
                txtVmin = new JTextField();
                txtVmin.setText("0");
                panel.add(txtVmin, "cell 1 4,growx");
                txtVmin.setColumns(6);
            }
            {
                txtVmax = new JTextField();
                txtVmax.setText("3000");
                panel.add(txtVmax, "cell 2 4,growx");
                txtVmax.setColumns(6);
            }
        }
        {
            JPanel panel = new JPanel();
            panel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "General Options", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
            contentPanel.add(panel, "cell 1 1,grow");
            panel.setLayout(new MigLayout("", "[][grow]", "[][][][]"));
            {
                JLabel lblNrPeaksTo = new JLabel("Nr. Peaks to use=");
                panel.add(lblNrPeaksTo, "cell 0 0");
            }
            {
                txtNpeaks = new JTextField();
                panel.add(txtNpeaks, "cell 1 0,growx");
                txtNpeaks.setText("npeaks");
                txtNpeaks.setColumns(6);
            }
            {
                JLabel lblEps = new JLabel("T2err=");
                panel.add(lblEps, "cell 0 1,alignx trailing");
            }
            {
                txtEps = new JTextField();
                txtEps.setText("0.01");
                panel.add(txtEps, "cell 1 1,growx");
                txtEps.setColumns(6);
            }
            {
                JLabel lblMinFom = new JLabel("Min M20=");
                panel.add(lblMinFom, "cell 0 2,alignx trailing");
            }
            {
                txtFom = new JTextField();
                txtFom.setText("10.0");
                panel.add(txtFom, "cell 1 2,growx");
                txtFom.setColumns(6);
            }
            {
                JLabel lblImpurities = new JLabel("Spurious=");
                panel.add(lblImpurities, "cell 0 3,alignx trailing");
            }
            {
                txtSpurious = new JTextField();
                txtSpurious.setText("0");
                panel.add(txtSpurious, "cell 1 3,growx");
                txtSpurious.setColumns(6);
            }
        }
        {
            panel_1 = new JPanel();
            panel_1.setBorder(new TitledBorder(null, "Method", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            contentPanel.add(panel_1, "cell 1 0,grow");
            panel_1.setLayout(new MigLayout("", "[][]", "[][]"));
            {
                rdbtnGridSearch = new JRadioButton("Grid Search");
                rdbtnGridSearch.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        do_rdbtnGridSearch_itemStateChanged(e);
                    }
                });
                buttonGroupMethod.add(rdbtnGridSearch);
                panel_1.add(rdbtnGridSearch, "cell 0 0");
            }
            {
                rdbtnDicothomy = new JRadioButton("Dicothomy");
                rdbtnDicothomy.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        do_rdbtnDicothomy_itemStateChanged(e);
                    }
                });
                rdbtnDicothomy.setSelected(true);
                buttonGroupMethod.add(rdbtnDicothomy);
                panel_1.add(rdbtnDicothomy, "cell 1 0");
            }
            {
                btnManual = new JButton("Manual");
                btnManual.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        do_btnManual_actionPerformed(e);
                    }
                });
                panel_1.add(btnManual, "cell 0 1 2 1,growx");
            }
        }
        {
            panelRun = new JPanel();
            contentPanel.add(panelRun, "flowx,cell 0 2 2 1,growx");
            panelRun.setLayout(new MigLayout("", "[][grow][]", "[]"));
            JButton okButton = new JButton("RUN");
            panelRun.add(okButton, "cell 0 0");
            okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    do_okButton_actionPerformed(e);
                }
            });
            okButton.setActionCommand("OK");
            this.indexDialog.getRootPane().setDefaultButton(okButton);
            {
                pBarIndex = new JProgressBar();
                panelRun.add(pBarIndex, "cell 1 0,grow");
            }
            {
                btnStop = new JButton("Stop");
                panelRun.add(btnStop, "cell 2 0");
                btnStop.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        do_btnStop_actionPerformed(e);
                    }
                });
            }
        }
        {
            panel_2 = new JPanel();
            panel_2.setBorder(new TitledBorder(null, "Results", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            contentPanel.add(panel_2, "cell 0 3 2 1,grow");
            panel_2.setLayout(new MigLayout("", "[grow][grow]", "[grow][][grow]"));
            {
                {
                    splitPane = new JSplitPane();
                    splitPane.setResizeWeight(0.5);
                    panel_2.add(splitPane, "cell 0 1 2 1,grow");
                    {
                        scrollPane = new JScrollPane();
                        splitPane.setLeftComponent(scrollPane);
                        {
                            tableSols = new JTable();
                            tableSols.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                            tableSols.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
                                @Override
                                public void valueChanged(ListSelectionEvent arg0) {
                                    aplicarselecciotaula(arg0);
                                }
                            });
                            scrollPane.setViewportView(tableSols);
                        }
                    }
                    {
                        scrollPane_1 = new JScrollPane();
                        splitPane.setRightComponent(scrollPane_1);
                        {
                            txtrRefs = new JTextArea();
                            txtrRefs.setFont(new Font("Monospaced", Font.PLAIN, 12));
                            txtrRefs.setEditable(false);
                            scrollPane_1.setViewportView(txtrRefs);
                            txtrRefs.setText("refs");
                        }
                    }
                }
            }
            {
                panel_3 = new JPanel();
                panel_2.add(panel_3, "cell 0 2 2 1,grow");
                panel_3.setLayout(new MigLayout("", "[][][][][]", "[]"));
                {
                    btnGuessSg = new JButton("Guess SG");
                    panel_3.add(btnGuessSg, "cell 0 0");
                    {
                        btnRemoveSgGuess = new JButton("Remove SG guess");
                        btnRemoveSgGuess.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                do_btnRemoveSgGuess_actionPerformed(e);
                            }
                        });
                        panel_3.add(btnRemoveSgGuess, "cell 1 0");
                    }
                    {
                        btnRemoveAll = new JButton("Remove All");
                        btnRemoveAll.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                do_btnRemoveAll_actionPerformed(e);
                            }
                        });
                        panel_3.add(btnRemoveAll, "flowx,cell 2 0");
                    }
                    btnAddAsDataserie = new JButton("Add as Dataserie");
                    panel_3.add(btnAddAsDataserie, "cell 3 0");
                    btnAddAsDataserie.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            do_btnAddAsDataserie_actionPerformed(e);
                        }
                    });
                    {
                        btnSaveResults = new JButton("Save Results");
                        panel_3.add(btnSaveResults, "flowx,cell 4 0");
                        btnSaveResults.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                do_btnSaveResults_actionPerformed(e);
                            }
                        });
                    }
                    btnGuessSg.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            do_btnGuessSg_actionPerformed(e);
                        }
                    });
                }
            }
        }
        {
            JPanel buttonPane = new JPanel();
            this.indexDialog.getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                buttonPane.setLayout(new MigLayout("", "[grow][]", "[25px]"));
            }
            {
                JButton cancelButton = new JButton("Close");
                cancelButton.addActionListener(new ActionListener() {
                	public void actionPerformed(ActionEvent e) {
                        do_cancelButton_actionPerformed(e);
                    }
                });
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton, "cell 1 0,alignx left,aligny top");
            }
        }
        
        inicia();
    }

    public void visible(boolean vis) {
        this.indexDialog.setVisible(vis);
        plotpanel.setShowIndexSolution(true);
    }
	
    private void inicia(){
        DataSerie ds = getPeaksSerieToUse();
        if(ds!=null)if (this.npeaks>ds.getNpoints())this.npeaks=ds.getNpoints();
        txtNpeaks.setText(String.valueOf(this.npeaks));        
        txtAmax.setText(FileUtils.dfX_3.format(this.amax));
        txtBmax.setText(FileUtils.dfX_3.format(this.bmax));
        txtCmax.setText(FileUtils.dfX_3.format(this.cmax));
        txtAmin.setText(FileUtils.dfX_3.format(this.amin));
        txtBmin.setText(FileUtils.dfX_3.format(this.bmin));
        txtCmin.setText(FileUtils.dfX_3.format(this.cmin));
        txtAlfaMin.setText(FileUtils.dfX_2.format(this.alfamin));
        txtAlfaMax.setText(FileUtils.dfX_2.format(this.alfamax));
        txtBetamin.setText(FileUtils.dfX_2.format(this.betamin));
        txtBetamax.setText(FileUtils.dfX_2.format(this.betamax));
        txtGaMin.setText(FileUtils.dfX_2.format(this.gammamin));
        txtGaMax.setText(FileUtils.dfX_2.format(this.gammamax));
        txtAstep.setText(FileUtils.dfX_3.format(this.astep));
        txtBstep.setText(FileUtils.dfX_3.format(this.bstep));
        txtCstep.setText(FileUtils.dfX_3.format(this.cstep));
        txtAlstep.setText(FileUtils.dfX_2.format(this.alstep));
        txtBestep.setText(FileUtils.dfX_2.format(this.bestep));
        txtGastep.setText(FileUtils.dfX_2.format(this.gastep));
        txtVmin.setText(FileUtils.dfX_2.format(this.vmin));
        txtVmax.setText(FileUtils.dfX_2.format(this.vmax));
        txtEps.setText(FileUtils.dfX_4.format(this.eps));
        txtFom.setText(FileUtils.dfX_2.format(this.minfom));
        txtSpurious.setText(String.valueOf(this.spurious));
        
        //checkboxes
        chckbxCubic.setSelected(this.cubic);
        chckbxTetragonal.setSelected(this.tetra);
        chckbxHexagonal.setSelected(this.hexa);
        chckbxOrthorhombic.setSelected(this.orto);
        chckbxMonoclinic.setSelected(this.mono);
        chckbxTriclinic.setSelected(this.tric);
        
        sols = new HashSet<IndexSolution>(); //to avoid null pointers
        original_sols = new HashSet<IndexSolution>(); //to avoid null pointers
        
        txtrRefs.setText("");
    }

    
    private IndexSolution getSelectedIS() {
        int selRow = tableSols.getSelectedRow();
        if (selRow<0)return null;
        int modelRow = tableSols.convertRowIndexToModel(selRow);
        IndexSolutionTableModel m = (IndexSolutionTableModel) tableSols.getModel();
        return m.getSolution(modelRow);
    }
    
    boolean firstTime = true;
    private JPanel panel_3;
    private JButton btnRemoveSgGuess;
    private JButton btnRemoveAll;
    private JButton btnManual;
    private JButton btnStop;
    private JPanel panelRun;
    private JProgressBar pBarIndex;
    
    private void aplicarselecciotaula(ListSelectionEvent arg0) {
        //ha de mostrar info solucio al textbox de la dreta + mostrar solucio a plotpanel (hkl serie)
        IndexSolution is = getSelectedIS();
        if (is==null)return;
        txtrRefs.setText(is.getInfoAsStringToSaveResults());
        plotpanel.setIndexSolution(this.getSelectedAsHKLDataSerieWithUnitsFirstPlotted());
        if (firstTime) {
            plotpanel.fitGraph(); //per mostrar que surt la serie HKL sino no es veu
            firstTime=false;
        }else {
            plotpanel.actualitzaPlot();    
        }
        
        if (sgGuessDialog!=null)sgGuessDialog.updateIndexSolution(is);
    }
    
    private DataSerie getPeaksSerieToUse() {
        DataSerie ds = null;
        try {
            ds = plotpanel.getFirstSelectedPlottable().getFirstDataSerieByType(SerieType.peaks);
            ds.sortSeriePoints(); //la ordenem
        }catch(Exception ex) {
            log.info("no Peaks found");
        }
        return ds;
    }
    
    private void tanca() {
        //comprovem que sigui l'últim supervivent de indexing:
        boolean showsolution=false;
        if (manualIndexDialog!=null) {
            if (manualIndexDialog.isVisible())showsolution=true;
        }
        plotpanel.setShowIndexSolution(showsolution);
    	this.indexDialog.dispose();
    }
    

    private void do_btnGuessSg_actionPerformed(ActionEvent e) {
        IndexSolution is = getSelectedIS();
        if (is==null)return;
        if (sgGuessDialog==null) sgGuessDialog = new IndexGuessSpaceGroupDialog(this,is);
        sgGuessDialog.visible(true);
    }
    
    private void do_btnManual_actionPerformed(ActionEvent e) {
        if (manualIndexDialog==null) manualIndexDialog = new debug_latgen(this);
        manualIndexDialog.visible(true);
    }
    
    public PlotPanel getPlotPanel() {
        return this.plotpanel;
    }
    
    private void do_cancelButton_actionPerformed(ActionEvent e) {
        this.tanca();
    }
    private void do_okButton_actionPerformed(ActionEvent e) {
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
        
        //checkboxes
        this.cubic=chckbxCubic.isSelected();
        this.tetra=chckbxTetragonal.isSelected();
        this.hexa=chckbxHexagonal.isSelected();
        this.orto=chckbxOrthorhombic.isSelected();
        this.mono=chckbxMonoclinic.isSelected();
        this.tric=chckbxTriclinic.isSelected();
//        this.zeroRef=chckbxZeroRefienment.isSelected();
//        this.prevzero=chckbxPrevZeroSearch.isSelected();
        
        if (!everythingOK) {
        	//TODO: complain message
        	log.info("error reading indexing parameters");
        	return;
        }
        
        //TODO:comprovacio de wavelengh dins de DS
        DataSerie ds = getPeaksSerieToUse();
        if (ds==null)everythingOK=false;
        if (ds.getWavelength()<=0) {
            PattOps.askForWavelengthAndAddToDS(ds);
        }
        if (ds.getWavelength()<=0) everythingOK=false;

        if (!everythingOK) {
            //TODO: complain message
            log.info("error with dataserie");
            return;
        }
        
        //convert eps 2theta to Q
        float dsp=(float) (ds.getWavelength()/(2*FastMath.sin(FastMath.toRadians(this.eps/2.))));
        float invdsp2=1/(dsp*dsp);
        log.infof("eps 2t=%.4f q=%.10f",this.eps,invdsp2);
        this.eps=invdsp2;

        //METHOD SELECTION AND START INDEXING
        if (rdbtnGridSearch.isSelected()) {
            IndexGrid ig = new IndexGrid(ds,this.npeaks,this.eps,this.minfom,this.spurious); //TODO treure EPS?
            ig.setMaxPars(this.amax, this.bmax, this.cmax, this.alfamax, this.betamax, this.gammamax, true);
            ig.setMinPars(this.amin, this.bmin, this.cmin, this.alfamin, this.betamin, this.gammamin, true);
            ig.setSteps(this.astep, this.bstep, this.cstep, this.alstep, this.bestep, this.gastep, true);
            ig.setVMinMax(this.vmin, this.vmax);
            ig.setSystemsSearch(this.cubic, this.tetra, this.hexa, this.orto, this.mono, this.tric);
        	this.executeGridSearch(ig);
        }
        
        if (rdbtnDicothomy.isSelected()) {
        	IndexDichotomy id = new IndexDichotomy(ds,this.npeaks,this.eps,this.minfom,this.spurious);
        	id.setMaxPars(this.amax, this.bmax, this.cmax, this.alfamax, this.betamax, this.gammamax, true);
        	id.setMinPars(this.amin, this.bmin, this.cmin, this.alfamin, this.betamin, this.gammamin, true);
        	id.setVMinMax(this.vmin, this.vmax);
        	id.setSystemsSearch(this.cubic, this.tetra, this.hexa, this.orto, this.mono, this.tric);
        	this.executeDicothomy(id);
        }
    }
  
    //TODO afegir STOP button
    private void executeGridSearch(IndexGrid ig) {
        
        gridSwk = new IndexGridSearchBruteWorker(ig);
        gridSwk.execute();
    }

    private void executeDicothomy(IndexDichotomy id) {
        dicW = new DichothomyWorker(id);
        dicW.execute();
    }

    
    private void fillSolucions() {
                        
        //borrem duplicats --- JA HO FA AUTOMATICAMENT EL SET
//        Set<IndexSolution> nodupl = new HashSet<IndexSolution>();
//        
//        for (IndexSolution s:sols) {
//            boolean repe = false;
//            for (IndexSolution s2:nodupl) {
//                if (s2.equals(s))repe=true;
//            }
//            if (!repe)nodupl.add(s);
//        }
//        log.info("size original="+sols.size());
//        log.info("size corrected="+nodupl.size());
        
//        sols.clear();
//        sols=nodupl;
        
        for (IndexSolution s:sols) {
            Cell c = s.getRefinedCell();
            log.info("ref cell= "+c.toStringCellParamOnly());
        }
        
        this.fillTable(this.sols);

    }
    
    
    protected void fillTable(Set<IndexSolution> listSolutions) {
        Iterator<IndexSolution> itrSol = listSolutions.iterator();
        tableSols.setAutoCreateRowSorter(true);
        tableSols.setModel(new IndexSolutionTableModel());
        tableSols.getRowSorter().toggleSortOrder(9);
        tableSols.getRowSorter().toggleSortOrder(9);
        IndexSolutionTableModel m = (IndexSolutionTableModel) tableSols.getModel();
        while (itrSol.hasNext()) {
            m.addSolution(itrSol.next());
        }
    }
    
    private void do_btnRemoveSgGuess_actionPerformed(ActionEvent e) {
        this.fillTable(this.original_sols);
    }
    
    private void do_btnRemoveAll_actionPerformed(ActionEvent e) {
        this.sols.clear();
        this.fillSolucions();
    }
    
    private void do_btnSaveResults_actionPerformed(ActionEvent e) {
        File f = FileUtils.fchooserSaveAsk(D1Dplot_global.getD1DmainFrame(), D1Dplot_global.getWorkdirFile(), null, null, "Save Indexing Results");
        if (f==null)return;
        
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(f,true)));
            out.println("INDEXING SOLUTION"); //TODO:POSAR METODE
            out.println(FileUtils.getCharLine('=', 80));
            int isol=1;
            for (IndexSolution s:sols) {
                out.println("Solution nr = "+isol);
                out.print(s.getInfoAsStringToSaveResults());
                out.println(FileUtils.getCharLine('-', 80));
                isol++;
            }
        }catch(Exception ex) {
            if (D1Dplot_global.isDebug())ex.printStackTrace();
            log.warning("Error writting indexing results file");
        }finally {
            if(out!=null)out.close();
        }

    }
    

    private DataSerie getSelectedAsHKLDataSerieWithUnitsFirstPlotted() {
        IndexSolution is = getSelectedIS();
        if (is==null) {
            log.warning("Please select a solution first");
            return null;
        }
        DataSerie hklds = is.getAsHKL_dsp_dataserie();
        DataSerie first = this.plotpanel.getFirstPlottedSerie();     
        hklds.setWavelength(first.getWavelength());
        if (first!=null) {
            boolean ok = hklds.convertDStoXunits(first.getxUnits()); //ja pregunta per la wavelength si es necessari
            if(!ok) {
                log.warning("Error adding solution as dataserie");
                return null;                    
            }
        }
        return hklds;
    }
    
    private void do_btnAddAsDataserie_actionPerformed(ActionEvent e) {
        DataSerie hklds = this.getSelectedAsHKLDataSerieWithUnitsFirstPlotted();
        Data_Common dc = new Data_Common(hklds.getWavelength());
        dc.addDataSerie(hklds);
        plotpanel.addPlottable(dc);
        D1Dplot_global.getD1Dmain().updateData(false,false); //TODO no m'agrada massa això...
    }
    
    private void do_btnStop_actionPerformed(ActionEvent e) {
        this.stopIndexing();
    }
    
    private void stopIndexing() {
        if (this.gridSwk != null) {
            this.gridSwk.cancel(true);
            log.info("Grid indexing stopped");
        }
        if (this.dicW != null) {
            this.dicW.cancel(true);
            log.info("Dichotomy indexing stopped");
        }
    }
    
    //TODO PASSAR ELS WORKERS A CADA CLASSE CORRESPONENT (indexGrid, indexDicho)
    public class IndexGridSearchBruteWorker extends SwingWorker<List<IndexSolutionGrid>,List<IndexSolutionGrid>> {

        @Override
        protected List<IndexSolutionGrid> doInBackground() throws Exception {
            return ig.runIndexing(pBarIndex,this); //podria enviar una progress bar?
        }
        
        @Override
        protected void done() {
            log.debug("finished indexing grid worker");
            try {
                List<IndexSolutionGrid> swsol = this.get();
                if (swsol.isEmpty()) {
                  log.warning("** NO SOLUTIONS **");
                  return;                    
                }
                //borrem solucions antigues
                sols.clear();
                int i=0;
                for (IndexSolutionGrid sol:swsol) {
                    log.info(String.format("sol=%2d res=%.2f M20=%.2f %s", i,sol.res,sol.getM20(),sol.getRefinedCell().toStringCellParamOnly()));
                    sols.add(sol.getIS());
//                    log.info(""+sols.size());
                    i++;
                }
                fillSolucions();
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }
            if (this.isCancelled()) {
                try {
                    Thread.sleep(1000); //wait to finish... potser no es suficient?
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                log.warning("** INDEXING CANCELLED **");
                pBarIndex.setString("indexing cancelled");
                pBarIndex.setValue(0);
            }
        }
        
        IndexGrid ig;
        
        public IndexGridSearchBruteWorker(IndexGrid ig) {
            this.ig=ig;
        }
        
    }
    
    public class DichothomyWorker extends SwingWorker<List<IndexSolutionDichotomy>,List<IndexSolutionDichotomy>> {

		@Override
		protected List<IndexSolutionDichotomy> doInBackground() throws Exception {
		    return id.runIndexing(-1, -1, -1, pBarIndex, this);
		}

        @Override
        protected void done() {
            log.debug("finished indexing grid worker");
            try {
                List<IndexSolutionDichotomy> swsol = this.get();
                if (swsol.isEmpty()) {
                  log.warning("** NO SOLUTIONS **");
                  return;                    
                }
                //borrem solucions antigues
                sols.clear();
                int i=0;
                for (IndexSolutionDichotomy sol:swsol) {
                    log.info(String.format("sol=%2d M20=%.2f %s", i,sol.getM20(),sol.getRefinedCell().toStringCellParamOnly()));
                    sols.add(sol.getIS());
//                    log.info(""+sols.size());
                    i++;
                }
                fillSolucions();
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }
            if (this.isCancelled()) {
                try {
                    Thread.sleep(1000); //wait to finish... potser no es suficient?
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                log.warning("** INDEXING CANCELLED **");
                pBarIndex.setString("indexing cancelled");
                pBarIndex.setIndeterminate(false);
                pBarIndex.setValue(pBarIndex.getMaximum());
            }
        }
        
		IndexDichotomy id;
		
		public DichothomyWorker(IndexDichotomy id) {
			this.id=id;
		}
    }

    protected void do_rdbtnDicothomy_itemStateChanged(ItemEvent e) {
        this.enableDisableFields();
    }
    
    protected void do_rdbtnGridSearch_itemStateChanged(ItemEvent e) {
        this.enableDisableFields();
    }
    
    private void enableDisableFields() {
        if (rdbtnDicothomy.isSelected()) {
            txtAstep.setEnabled(false);
            txtBstep.setEnabled(false);
            txtCstep.setEnabled(false);
            txtAlstep.setEnabled(false);
            txtBestep.setEnabled(false);
            txtGastep.setEnabled(false);
//            txtEps.setEnabled(true);
        }
        if (rdbtnGridSearch.isSelected()) {
            txtAstep.setEnabled(true);
            txtBstep.setEnabled(true);
            txtCstep.setEnabled(true);
            txtAlstep.setEnabled(true);
            txtBestep.setEnabled(true);
            txtGastep.setEnabled(true);
//            txtEps.setEnabled(false);
        }
    }
    
    public class IndexSolutionTableModel extends AbstractTableModel
    {
        private static final long serialVersionUID = 1L;
        private String[] columnNames =
        {
            "a",
            "b",
            "c",
            "al",
            "be",
            "ga",
            "CF", //crystalfamily
            "CC", //centering
            "SG", //spaceGroup
            "M20",
            "i20",
            "N20",
            "Q20"
        };
     
        private List<IndexSolution> solsT;
     
        public IndexSolutionTableModel()
        {
            super();
            solsT = new ArrayList<IndexSolution>();
        }
     
        @Override
        public int getColumnCount()
        {
            return columnNames.length;
        }
     
        @Override
        public String getColumnName(int column)
        {
            return columnNames[column];
        }
     
        @Override
        public int getRowCount()
        {
            if (solsT!=null) {
                return solsT.size();    
            }else {
                return 0;
            }
            
        }

        @Override
        public Class<?> getColumnClass(int column)
        {
            switch (column)
            {
                case 6: return CrystalFamily.class;
                case 7: return CrystalCentering.class;
                case 8: return SpaceGroup.class;
                case 10: return Integer.class;
                default: return Double.class; //la resta
            }
        }
         
        @Override
        public Object getValueAt(int row, int column)
        {
            IndexSolution is = getSolution(row);
            double[] par = is.getRefinedCell().getCellParameters(true);
            
            switch (column)
            {
                case 0: return par[0];
                case 1: return par[1];
                case 2: return par[2];
                case 3: return par[3];
                case 4: return par[4];
                case 5: return par[5];
                case 6: return is.getRefinedCell().getCrystalFamily();
                case 7: return is.getRefinedCell().getCrystalCentering();
                case 8: return is.getRefinedCell().getSg();
                case 9: return is.getM20();
                case 10: return is.getI20();
                case 11: return is.getN20();
                case 12: return is.getQ20();
                default: return null;
                
            }
        }
         
        public IndexSolution getSolution(int row)
        {
            return solsT.get(row);
        }

        public void addSolution(IndexSolution is)
        {
            insertSolution(getRowCount(), is);
        }
         
        public void insertSolution(int row, IndexSolution is)
        {
            solsT.add(row, is);
            fireTableRowsInserted(row, row);
        }

     
    }

    public boolean isVisible() {
        return this.indexDialog.isVisible();
    }


}



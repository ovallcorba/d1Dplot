package com.vava33.d1dplot;

/**
 * D1Dplot
 * 
 * Dialog for peak fitting using pV profile
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.vava33.d1dplot.auxi.PattOps;
import com.vava33.d1dplot.auxi.PatternsTableCellRenderer;
import com.vava33.d1dplot.auxi.PseudoVoigt;
import com.vava33.d1dplot.auxi.PseudoVoigt.Gaussian;
import com.vava33.d1dplot.auxi.PseudoVoigt.Lorentzian;
import com.vava33.d1dplot.data.DataPoint;
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.d1dplot.data.Data_Common;
import com.vava33.d1dplot.data.Plottable;
import com.vava33.d1dplot.data.Plottable_point;
import com.vava33.d1dplot.data.SerieType;
import com.vava33.d1dplot.data.Xunits;
import com.vava33.jutils.FileUtils;
import com.vava33.jutils.VavaLogger;

import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import java.awt.Font;

import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.fitting.leastsquares.GaussNewtonOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.util.FastMath;

import javax.swing.border.LineBorder;

import java.awt.Color;

import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.awt.event.ItemEvent;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JRadioButton;

public class FitPeakDialog {

	private JDialog fitPkDialog;
    private PlotPanel plotpanel;
    
    private static final String className = "FitPeakDialog";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);
    
    private JPanel contentPanel;
    private JTextField txtMean;
    private JTextField txtFwhm;
    private JTextField txtEta;
    private JTextField txtYmax;
    private JTextField txtBkgi;
    private JTextField txtBkgp;
    private JTable tablePV;
    
    /**
     * Create the dialog.
     */
    public FitPeakDialog(PlotPanel p) {
        this.fitPkDialog = new JDialog(D1Dplot_global.getD1DmainFrame(),"Fit Peak(s) **EXPERIMENTAL**",false);
        fitPkDialog.setTitle("Fit Peak(s) ** IN DEVELOPMENT **");
        log.info("Fit Peak(s) is IN DEVELOPMENT. It contains errors and MAY CRASH THE PROGRAM!"); 
        this.plotpanel = p;
        this.contentPanel = new JPanel();
        fitPkDialog.setIconImage(D1Dplot_global.getIcon());
        fitPkDialog.setBounds(100, 100, 814, 227);
        fitPkDialog.getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        fitPkDialog.getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new MigLayout("", "[][]", "[][]"));
        {
            JPanel panel = new JPanel();
            panel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
            contentPanel.add(panel, "cell 0 0 2 1,grow");
            panel.setLayout(new MigLayout("", "[grow][grow][][grow][][grow][][grow][][grow][][grow][]", "[][][grow]"));
            {
                JRadioButton rdbtnPseudovoigt = new JRadioButton("PseudoVoigt");
                rdbtnPseudovoigt.setSelected(true);
                panel.add(rdbtnPseudovoigt, "cell 0 0 12 1");
            }
            {
                JLabel lblMean = new JLabel("mean=");
                panel.add(lblMean, "cell 0 1,alignx trailing");
            }
            {
                txtMean = new JTextField();
                panel.add(txtMean, "cell 1 1,growx");
                txtMean.setColumns(10);
            }
            {
                JLabel lblFwhm = new JLabel("fwhm=");
                panel.add(lblFwhm, "cell 2 1,alignx trailing");
            }
            {
                txtFwhm = new JTextField();
                panel.add(txtFwhm, "cell 3 1,growx");
                txtFwhm.setColumns(10);
            }
            {
                JLabel lblEta = new JLabel("eta=");
                panel.add(lblEta, "cell 4 1,alignx trailing");
            }
            {
                txtEta = new JTextField();
                panel.add(txtEta, "cell 5 1,growx");
                txtEta.setColumns(10);
            }
            {
                JLabel lblYmax = new JLabel("ymax=");
                panel.add(lblYmax, "cell 6 1,alignx trailing");
            }
            {
                txtYmax = new JTextField();
                panel.add(txtYmax, "cell 7 1,growx");
                txtYmax.setColumns(10);
            }
            {
                JLabel lblYbkg = new JLabel("Ybkg=");
                panel.add(lblYbkg, "cell 8 1,alignx trailing");
            }
            {
                txtBkgi = new JTextField();
                panel.add(txtBkgi, "cell 9 1,growx");
                txtBkgi.setColumns(10);
            }
            {
                JLabel lblMbkg = new JLabel("mBkg=");
                panel.add(lblMbkg, "cell 10 1,alignx trailing");
            }
            {
                txtBkgp = new JTextField();
                panel.add(txtBkgp, "cell 11 1,growx");
                txtBkgp.setColumns(10);
            }
            {
                JButton btnAdd = new JButton("add");
                btnAdd.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        do_btnAdd_actionPerformed(e);
                    }
                });
                panel.add(btnAdd, "cell 12 1");
            }
            {
                JScrollPane scrollPane = new JScrollPane();
                panel.add(scrollPane, "cell 0 2 12 1,grow");
                {
                    tablePV = new JTable();
                    tablePV.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                    tablePV.setModel(new DefaultTableModel(
                            new Object[][] {
                            },
                            new String[] {
                                    "Mean", "FWHM", "eta", "Ymax", "Y_bkg", "m_bkg"
                            }
                            ) {
                        private static final long serialVersionUID = 1L;
                        @SuppressWarnings("rawtypes")
                        Class[] columnTypes = new Class[] {
                                Double.class, Double.class, Double.class, Double.class, Double.class, Double.class
                        };
                        @SuppressWarnings({ "unchecked", "rawtypes" })
                        public Class getColumnClass(int columnIndex) {
                            return columnTypes[columnIndex];
                        }
                    });
                    scrollPane.setViewportView(tablePV);
                }
            }
            {
                JButton btnDel = new JButton("del");
                btnDel.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        do_btnDel_actionPerformed(e);
                    }
                });
                panel.add(btnDel, "cell 12 2");
            }
        }
        {
            JButton btnSubtract = new JButton("Fit!");
            btnSubtract.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    do_btnFit_actionPerformed(e);
                }
            });
            contentPanel.add(btnSubtract, "cell 0 1,growx,aligny bottom");
        }
        {
            JButton btnPlot = new JButton("Plot");
            btnPlot.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    do_btnPlot_actionPerformed(e);
                }
            });
            contentPanel.add(btnPlot, "cell 1 1");
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            fitPkDialog.getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("Close");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        do_okButton_actionPerformed(e);
                    }
                });
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                fitPkDialog.getRootPane().setDefaultButton(okButton);
            }
        }
        
        inicia();
    }

    private void inicia(){
                
    }
    
    private void do_okButton_actionPerformed(ActionEvent e) {
    	fitPkDialog.dispose();
    }

    
    private PseudoVoigt getPVfromFields() {
        double fwhm = Double.parseDouble(txtFwhm.getText());
        double eta = Double.parseDouble(txtEta.getText());
        double mean = Double.parseDouble(txtMean.getText());
        double inten = Double.parseDouble(txtYmax.getText());
        double bkgI = Double.parseDouble(txtBkgi.getText());
        double bkgP = Double.parseDouble(txtBkgp.getText());
        return new PseudoVoigt(mean,fwhm,eta,inten,bkgI,bkgP);        
    }
    
    private void setFieldFromPV(PseudoVoigt pv) {
        double[] pars = pv.getPars();
        txtFwhm.setText(String.format("%.5f",pars[1]));
        txtEta.setText(String.format("%.5f",pars[2]));
        txtMean.setText(String.format("%.5f",pars[0]));
        txtYmax.setText(String.format("%.5f",pars[3]));
        txtBkgi.setText(String.format("%.5f",pars[4]));
        txtBkgp.setText(String.format("%.5f",pars[5]));
    }
    
    protected void do_btnPlot_actionPerformed(ActionEvent e) {
        if (tablePV.getRowCount()<=0)return;
        DefaultTableModel dm = (DefaultTableModel) tablePV.getModel();
        List<PseudoVoigt> pvs = new ArrayList<PseudoVoigt>();
        for (int i=0;i<tablePV.getRowCount();i++) {
            double mean = (Double)dm.getValueAt(i, 0);
            double fwhm = (Double)dm.getValueAt(i, 1);
            double eta = (Double)dm.getValueAt(i, 2);
            double ymax = (Double)dm.getValueAt(i, 3);
            double ibkg = (Double)dm.getValueAt(i, 4);
            double mbkg = (Double)dm.getValueAt(i, 5);
            pvs.add(new PseudoVoigt(mean,fwhm,eta,ymax,ibkg,mbkg));
        }
        
        double[] xminmax = plotpanel.getXrangesMinMax();
        double step = plotpanel.getFirstSelectedPlottable().getMainSerie().calcStep();
        double xval = xminmax[0];
        List<Plottable_point> calc = new ArrayList<Plottable_point>();
        while (xval<xminmax[1]) {
            DataPoint dp = new DataPoint(xval,0,0);
            for(PseudoVoigt pv:pvs) {
                dp.setY(dp.getY()+pv.eval(xval,false)); //TODO el fons nomes s'hauria d'afegir un cpo
            }
            dp.setY(dp.getY()+pvs.get(0).getBkgValue(xval));
            calc.add(dp);
            xval = xval + step;
        }
        DataSerie dscalc = new DataSerie(plotpanel.getFirstSelectedPlottable().getMainSerie(),calc,plotpanel.getFirstSelectedPlottable().getMainSerie().getxUnits());
        dscalc.setTipusSerie(SerieType.cal);
        plotpanel.getFirstSelectedPlottable().addDataSerie(dscalc);
        D1Dplot_global.getD1Dmain().updateData(false, true);
    }
    
    private void clearTable() {
        if (tablePV.getRowCount()<=0)return;
        DefaultTableModel dm = (DefaultTableModel) tablePV.getModel();
        dm.setRowCount(0);
    }
    
    private void do_btnFit_actionPerformed(ActionEvent e) {
        //borrem taula primer
        this.clearTable();
        
        
        /*
         * Take the selected pattern region shown in the window and try to fit one or more peaks there. Ideally one should select the peaks before (findpeaks).
         * Option combobox function (gauss, lor, pV, pearson, ...)
         */
        DataSerie peaks = null;
        try {
            peaks = plotpanel.getFirstSelectedPlottable().getDataSeriesByType(SerieType.peaks).get(0);
        }catch(Exception ex) {
            log.info("Please, select the peaks first (peak search) and create a peaks serie");
            return;
        }
        if (peaks==null)return;
        
        final DataSerie dsZone = plotpanel.getFirstSelectedPlottable().getMainSerie().getSubDataSerie(plotpanel.getXrangesMinMax()[0],plotpanel.getXrangesMinMax()[1]);
        final DataSerie peaksZone = peaks.getSubDataSerie(plotpanel.getXrangesMinMax()[0],plotpanel.getXrangesMinMax()[1]);
        
//        for (int i=0;i<peaks.getNpoints();i++) {
//            double xval = peaks.getPointWithCorrections(i, false).getX();
//            if (plotpanel.isXValueInsideGraphArea(xval)) {
//                log.writeNameNumPairs("INFO", true, "xval", xval);    
//            }
//        }
        
        //AQUI HAURIA D'ANAR EL FIT?
       
        this.runOptim(0, dsZone, peaksZone);
    }
    

    
    public void visible(boolean vis) {
    	this.fitPkDialog.setVisible(vis);
    	if (vis)inicia();
    }
    
    
    //function 0=pseudoV 1=gauss, 2=lor
    private void runOptim(int funct, DataSerie punts, DataSerie peaks) {
        switch (funct) {
        case 0: //PV
            //estimates TODO intentar agafar els que hi ha als textbox si es que hi ha
            
//            double bkgI = punts.getPointWithCorrections(0, false).getY(); //primer punt
            double bkgI = punts.getPuntsMaxXMinXMaxYMinY()[3];
            double eta = 0.5;
//            double ymax = peaks.getPointWithCorrections(0, false).getY()-bkgI;
            double fwhm = 0.05;
            double bkgP = 0;
//            double xpos = peaks.getPointWithCorrections(0, false).getX(); //TODO: POSO NOMES UN PER PROVAR
//            PseudoVoigt pv = new PseudoVoigt(xpos,fwhm,eta,ymax,bkgI,bkgP);
            
            double[] xpos = peaks.getXasDoubleArray();
            double[] ymax = peaks.getYasDoubleArray();
            for (int i=0;i<ymax.length;i++) {
                ymax[i] = ymax[i] - bkgI;
            }
            
            PseudoVoigt pv = new PseudoVoigt(xpos[0],fwhm,eta,ymax[0],bkgI,bkgP); //primer punt
            
            log.debug("(guess)"+pv.toString());
            
//            pv.fit(punts);
            
            double[] opt_xpos = pv.fitMultipleMeans(punts, xpos, ymax);
            
            pv.updateFunctions(opt_xpos[0], opt_xpos[xpos.length]); //actualitzo la primera
            this.setFieldFromPV(pv);
            this.addToTable(pv);
            //ara la resta
            log.writeNameNumPairs("info", true, "xpos,ymax,opt", xpos.length,ymax.length,opt_xpos.length);
            for (int i=1;i<xpos.length;i++) {
                log.info(Integer.toString(i));
                PseudoVoigt npv = new PseudoVoigt(pv);
                npv.updateFunctions(opt_xpos[i], opt_xpos[i+xpos.length]);
                this.addToTable(npv);
            }

            this.do_btnPlot_actionPerformed(null);
            
            break;
        case 1:
            break;
        case 2:
            break;
        default:
            break;
                
        }
    }

    private void addToTable(PseudoVoigt pv) {
        DefaultTableModel dm = (DefaultTableModel) tablePV.getModel();
        double[] row = pv.getPars();
        Object[] orow = new Object[] {row[0],row[1],row[2],row[3],row[4],row[5]};
        dm.addRow(orow);
    }

    protected void do_btnAdd_actionPerformed(ActionEvent e) {
        this.addToTable(this.getPVfromFields());
    }
    protected void do_btnDel_actionPerformed(ActionEvent e) {
        int[] is = tablePV.getSelectedRows();
        DefaultTableModel dm = (DefaultTableModel) tablePV.getModel();
        for (int i=0;i<is.length;i++) {
            dm.removeRow(is[i]); //TODO peta perque al treure un row el num disminueix i hi ha un arrayoutofbounds
        }
        
    }
}

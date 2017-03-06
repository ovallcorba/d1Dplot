package vava33.d1dplot;

/**
 * D1Dplot
 * 
 * Background subtraction dialog
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */


import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;

import javax.swing.border.TitledBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.LineBorder;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.ButtonGroup;

import vava33.d1dplot.auxi.DataSerie;
import vava33.d1dplot.auxi.PattOps;
import vava33.d1dplot.auxi.Pattern1D;

import com.vava33.jutils.VavaLogger;

import javax.swing.JSeparator;
import javax.swing.JToggleButton;

//todo crear checkserieselected

public class Background_dialog extends JDialog {

    private static final long serialVersionUID = -5952734543185556697L;
    private static final int def_npoints = 30;
    private static final int def_degree = 12;

    private PlotPanel plotpanel;
    private D1Dplot_main main;
    private DataSerie puntsFons;
    private DataSerie fonsCalc;
    
    private static VavaLogger log = D1Dplot_global.getVavaLogger(Background_dialog.class.getName());

    private final JPanel contentPanel = new JPanel();
    private JTextField txtNveins;
    private JTextField txtTini;
    private JTextField txtTsup;
    private JTable table;
    private JTextField txtNiter;
    private JTextField txtDeg;
    private JLabel lbltinf;
    private JLabel lbltsup;
    private JButton btnAdd;
    private JButton btnDel;
    private JCheckBox chckbxMulti;
    private final ButtonGroup buttonGroup = new ButtonGroup();
    private JCheckBox chckbxShowBackground;
    private JRadioButton rdbtnNormal;
    private JRadioButton rdbtnInverse;
    
    private JLabel lblNbkgpoints;
    private JTextField txtNbkgpoints;
    private JButton btnEstimate;
    private JButton btnRemoveAllBkg;
    private JToggleButton btnAddPoints;
    private JToggleButton btnRemovePoints;
    private JSeparator separator;
    private JSeparator separator_1;
    private JCheckBox chckbxOnTop;
    
    /**
     * Create the dialog.
     */
    public Background_dialog(PlotPanel p,D1Dplot_main m) {
        setTitle("Background Calc");
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(Background_dialog.class.getResource("/vava33/d1dplot/img/d1Dplot.png")));
        this.setPlotpanel(p);
        this.setMain(m);
        setBounds(100, 100, 481, 646);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new MigLayout("", "[grow][]", "[][grow][grow]"));
        {
            chckbxShowBackground = new JCheckBox("Show Background");
            chckbxShowBackground.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    do_chckbxShowBackground_itemStateChanged(e);
                }
            });
            contentPanel.add(chckbxShowBackground, "cell 0 0");
        }
        {
            chckbxOnTop = new JCheckBox("on top");
            chckbxOnTop.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    do_chckbxOnTop_itemStateChanged(e);
                }
            });
            contentPanel.add(chckbxOnTop, "cell 1 0");
        }
        {
            JPanel panel = new JPanel();
            panel.setBorder(new TitledBorder(null, "Bruchner", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            contentPanel.add(panel, "cell 0 1 2 1,grow");
            panel.setLayout(new MigLayout("", "[][grow][]", "[][][][][][][grow][]"));
            {
                JLabel lblN = new JLabel("N");
                panel.add(lblN, "cell 0 0,alignx trailing");
            }
            {
                txtNveins = new JTextField();
                txtNveins.setText("80");
                panel.add(txtNveins, "cell 1 0,growx");
                txtNveins.setColumns(10);
            }
            {
                chckbxMulti = new JCheckBox("multi");
                chckbxMulti.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        do_chckbxMulti_itemStateChanged(e);
                    }
                });
                panel.add(chckbxMulti, "cell 2 0");
            }
            {
                JLabel lblNiter = new JLabel("Niter");
                panel.add(lblNiter, "cell 0 1,alignx trailing");
            }
            {
                txtNiter = new JTextField();
                txtNiter.setText("20");
                panel.add(txtNiter, "cell 1 1,growx");
                txtNiter.setColumns(10);
            }
            {
                JButton btnIterate = new JButton("Iterate");
                btnIterate.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        do_btnIterate_actionPerformed(e);
                    }
                });
                panel.add(btnIterate, "cell 2 1,growx");
            }
            {
                JLabel lblPatternEdge = new JLabel("Pattern Edge");
                panel.add(lblPatternEdge, "cell 0 2");
            }
            {
                rdbtnNormal = new JRadioButton("Normal");
                rdbtnNormal.setSelected(true);
                buttonGroup.add(rdbtnNormal);
                panel.add(rdbtnNormal, "cell 1 2");
            }
            {
                rdbtnInverse = new JRadioButton("Inverse");
                buttonGroup.add(rdbtnInverse);
                panel.add(rdbtnInverse, "cell 1 3");
            }
            {
                lbltinf = new JLabel("2Tinf");
                panel.add(lbltinf, "cell 0 4,alignx trailing");
            }
            {
                txtTini = new JTextField();
                txtTini.setText("t2ini");
                panel.add(txtTini, "cell 1 4,growx");
                txtTini.setColumns(10);
            }
            {
                btnAdd = new JButton("Add");
                btnAdd.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        do_btnAdd_actionPerformed(e);
                    }
                });
                panel.add(btnAdd, "cell 2 4,growx");
            }
            {
                lbltsup = new JLabel("2Tsup");
                panel.add(lbltsup, "cell 0 5,alignx trailing");
            }
            {
                txtTsup = new JTextField();
                txtTsup.setText("t2sup");
                panel.add(txtTsup, "cell 1 5,growx");
                txtTsup.setColumns(10);
            }
            {
                btnDel = new JButton("Del");
                btnDel.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        do_btnDel_actionPerformed(e);
                    }
                });
                panel.add(btnDel, "cell 2 5,growx");
            }
            {
                JScrollPane scrollPane = new JScrollPane();
                panel.add(scrollPane, "cell 0 6 3 1,grow");
                {
                    table = new JTable();
                    table.setModel(new DefaultTableModel(
                        new Object[][] {
                        },
                        new String[] {
                            "T2i", "T2f", "N"
                        }
                    ) {
                        /**
                         * 
                         */
                        private static final long serialVersionUID = 1527503710298754660L;
                        @SuppressWarnings("rawtypes")
                        Class[] columnTypes = new Class[] {
                            Double.class, Double.class, Integer.class
                        };
                        @SuppressWarnings({ "unchecked", "rawtypes" })
                        public Class getColumnClass(int columnIndex) {
                            return columnTypes[columnIndex];
                        }
                    });
                    scrollPane.setViewportView(table);
                }
            }
        }
        {
            JPanel panel = new JPanel();
            panel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Other Methods", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
            contentPanel.add(panel, "cell 0 2 2 1,grow");
            panel.setLayout(new MigLayout("", "[][grow][grow]", "[][][][][][][]"));
            {
                JButton btnFitPolynomial = new JButton("Fit Polynomial");
                btnFitPolynomial.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        do_btnFitPolynomial_actionPerformed(e);
                    }
                });
                {
                    lblNbkgpoints = new JLabel("nBkgPoints=");
                    panel.add(lblNbkgpoints, "cell 0 0,alignx trailing");
                }
                {
                    txtNbkgpoints = new JTextField();
                    txtNbkgpoints.setText("80");
                    panel.add(txtNbkgpoints, "cell 1 0,growx");
                    txtNbkgpoints.setColumns(10);
                }
                {
                    btnEstimate = new JButton("Estimate");
                    btnEstimate.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            do_btnEstimate_actionPerformed(e);
                        }
                    });
                    panel.add(btnEstimate, "cell 2 0,growx");
                }
                {
                    btnAddPoints = new JToggleButton("Add Points");
                    btnAddPoints.addItemListener(new ItemListener() {
                        public void itemStateChanged(ItemEvent arg0) {
                            do_btnAddPoints_itemStateChanged(arg0);
                        }
                    });
                    panel.add(btnAddPoints, "cell 0 1,growx");
                }
                {
                    btnRemovePoints = new JToggleButton("Remove Points");
                    btnRemovePoints.addItemListener(new ItemListener() {
                        public void itemStateChanged(ItemEvent e) {
                            do_btnRemovePoints_itemStateChanged(e);
                        }
                    });
                    panel.add(btnRemovePoints, "cell 1 1,growx");
                }
                {
                    btnRemoveAllBkg = new JButton("Remove All");
                    btnRemoveAllBkg.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            do_btnRemoveAllBkg_actionPerformed(e);
                        }
                    });
                    panel.add(btnRemoveAllBkg, "cell 2 1,growx");
                }
                {
                    separator = new JSeparator();
                    panel.add(separator, "cell 0 2 3 1,growx");
                }
                panel.add(btnFitPolynomial, "cell 0 3");
            }
            {
                JLabel lblDegree = new JLabel("degree");
                panel.add(lblDegree, "cell 1 3,alignx trailing");
            }
            {
                txtDeg = new JTextField();
                txtDeg.setText("12");
                panel.add(txtDeg, "cell 2 3,growx");
                txtDeg.setColumns(10);
            }
            {
                JButton btnFitSpline = new JButton("Fit Spline");
                btnFitSpline.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        do_btnFitSpline_actionPerformed(e);
                    }
                });
                {
                    separator_1 = new JSeparator();
                    panel.add(separator_1, "cell 0 4 3 1,growx");
                }
                panel.add(btnFitSpline, "cell 0 5,growx");
            }
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("Close");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        do_okButton_actionPerformed(e);
                    }
                });
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
        }
        
        inicia();
    }

    private void inicia(){
        btnDel.setEnabled(false);
        btnAdd.setEnabled(false);
        txtTini.setEnabled(false);
        txtTsup.setEnabled(false);
        lbltinf.setEnabled(false);
        lbltsup.setEnabled(false);
        table.setEnabled(false);
        chckbxShowBackground.setSelected(true);
        fonsCalc = new DataSerie();
        puntsFons = new DataSerie();
    }
    
    public PlotPanel getPlotpanel() {
        return plotpanel;
    }

    public void setPlotpanel(PlotPanel plotpanel) {
        this.plotpanel = plotpanel;
    }
    
    protected void do_chckbxMulti_itemStateChanged(ItemEvent e) {
        if(this.chckbxMulti.isSelected()){
            btnDel.setEnabled(true);
            btnAdd.setEnabled(true);
            txtTini.setEnabled(true);
            txtTsup.setEnabled(true);
            lbltinf.setEnabled(true);
            lbltsup.setEnabled(true);
            table.setEnabled(true);
        }else{
            btnDel.setEnabled(false);
            btnAdd.setEnabled(false);
            txtTini.setEnabled(false);
            txtTsup.setEnabled(false);
            lbltinf.setEnabled(false);
            lbltsup.setEnabled(false);
            table.setEnabled(false);
        }
    }
    protected void do_chckbxShowBackground_itemStateChanged(ItemEvent e) {
        this.getPlotpanel().setShowBackground(chckbxShowBackground.isSelected());
        plotpanel.repaint();
    }
    protected void do_btnAdd_actionPerformed(ActionEvent e) {
        // afegeix els valors a la taula
        if (!txtTsup.getText().equals("")&&!txtTini.getText().equals("")&&!txtNveins.getText().equals("")){
            Object[] row = new Object[3];
            try{
                row[0]=Double.parseDouble(txtTini.getText());
                row[1]=Double.parseDouble(txtTsup.getText());
                row[2]=Integer.parseInt(txtNveins.getText());
            }catch(Exception ex){
                ex.printStackTrace();
                log.info("error reading parameters");
            }
            ((DefaultTableModel)table.getModel()).addRow(row);
        }else{
            log.info("2Tinf, 2Tsup and N must be entered");
        }
    }
    protected void do_btnDel_actionPerformed(ActionEvent e) {
        // Elimina la fila seleccionada de la taula
        int numRows = table.getSelectedRows().length;
        for(int i=0; i<numRows; i++){
            ((DefaultTableModel)table.getModel()).removeRow(table.getSelectedRow());
        }
    }
    
    protected void do_btnIterate_actionPerformed(ActionEvent e) {
        if (!isOneSerieSelected())return;
        try{
            int niter = Integer.parseInt(txtNiter.getText());   
            int nveins = Integer.parseInt(txtNveins.getText());
            fonsCalc = PattOps.bkg_Bruchner(plotpanel.getSelectedSeries().get(0), niter, nveins, rdbtnNormal.isSelected(),chckbxMulti.isSelected(),((DefaultTableModel)table.getModel()));
            this.updatePlotPanelMain(plotpanel.getSelectedSeries().get(0));
        }catch(Exception ex){
            ex.printStackTrace();
        }

        
        

    }
    
    protected void do_btnFitPolynomial_actionPerformed(ActionEvent e) {
        if (!isOneSerieSelected())return;
        int npoints = def_npoints;
        int degree = def_degree;
        try{
            degree = Integer.parseInt(txtDeg.getText());
            npoints = Integer.parseInt(txtNbkgpoints.getText());
        }catch(Exception ex){
            ex.printStackTrace();
            txtDeg.setText(String.valueOf(degree));
            txtNbkgpoints.setText(String.valueOf(npoints));
        }

        if (puntsFons.getNpoints()==0){
            puntsFons = PattOps.findBkgPoints(plotpanel.getSelectedSeries().get(0), npoints);
        }
        fonsCalc = PattOps.bkg_FitPoly(plotpanel.getSelectedSeries().get(0), puntsFons, degree);
        updatePlotPanelMain(plotpanel.getSelectedSeries().get(0));
    }
    protected void do_btnFitSpline_actionPerformed(ActionEvent e) {
        if (!isOneSerieSelected())return;
        int npoints = def_npoints;
        try{
            npoints = Integer.parseInt(txtNbkgpoints.getText());
        }catch(Exception ex){
            ex.printStackTrace();
            txtNbkgpoints.setText(String.valueOf(npoints));
        }

        if (puntsFons.getNpoints()==0){
            puntsFons = PattOps.findBkgPoints(plotpanel.getSelectedSeries().get(0), npoints);
        }
        fonsCalc = PattOps.bkg_FitSpline(plotpanel.getSelectedSeries().get(0), puntsFons);
        updatePlotPanelMain(plotpanel.getSelectedSeries().get(0));
        
    }
    protected void do_btnEstimate_actionPerformed(ActionEvent e) {
        if (!isOneSerieSelected())return;
        int npoints = def_npoints;
        try{
            npoints = Integer.parseInt(txtNbkgpoints.getText());
        }catch(Exception ex){
            ex.printStackTrace();
            txtNbkgpoints.setText(String.valueOf(npoints));
        }
        puntsFons = PattOps.findBkgPoints(plotpanel.getSelectedSeries().get(0), npoints);
        updatePlotPanelMain(plotpanel.getSelectedSeries().get(0));
    }
    
    private void updatePlotPanelMain(DataSerie selectedSerie){
        
        if (selectedSerie==null){
            if (D1Dplot_global.isDebug())log.debug("something is wrong, information on the bkg serie is missing");
            return;
        }
        Pattern1D selPatt = selectedSerie.getPatt1D();
        selPatt.removeBkgSerie();
        selPatt.removeBkgEstimPSerie();
        if (fonsCalc.getNpoints()!=0){
            fonsCalc.setTipusSerie(DataSerie.serieType.bkg); //en teoria ja estava posat pero per si de cas
            fonsCalc.setPatt1D(selPatt);
            plotpanel.setBkgserie(fonsCalc);
        }
        if (puntsFons.getNpoints()!=0){
            puntsFons.setTipusSerie(DataSerie.serieType.bkgEstimP);
            puntsFons.setPatt1D(selPatt);
            plotpanel.setBkgEstimPoints(puntsFons);
        }
        plotpanel.repaint();
        main.updateTable();
    }
    
    protected void do_btnRemoveAllBkg_actionPerformed(ActionEvent e) {
        puntsFons.clearDataPoints();
        fonsCalc.clearDataPoints();
        updatePlotPanelMain(puntsFons);
    }

    public D1Dplot_main getMain() {
        return main;
    }

    public void setMain(D1Dplot_main main) {
        this.main = main;
    }

    public DataSerie getFonsCalc() {
        return fonsCalc;
    }

    public void setFonsCalc(DataSerie fonsCalc) {
        this.fonsCalc = fonsCalc;
    }
    
    protected void do_okButton_actionPerformed(ActionEvent e) {
        this.dispose();
    }

    @Override
    public void dispose() {
        // TODO Desactivar botons, i edicions mouse
        this.btnAddPoints.setText("Add Points");
        this.btnRemovePoints.setText("Remove Points");
        plotpanel.setDeletingBkgPoints(false);
        plotpanel.setSelectingBkgPoints(false);
        this.chckbxShowBackground.setSelected(false);
        super.dispose();
    }
    
    protected void do_btnAddPoints_itemStateChanged(ItemEvent arg0) {
        if (!isOneSerieSelected())return;
        if (this.btnAddPoints.isSelected()){
            this.btnAddPoints.setText("Finish");
            plotpanel.setSelectingBkgPoints(true);
        }else{
            this.btnAddPoints.setText("Add Points");
            plotpanel.setSelectingBkgPoints(false);
            this.puntsFons=plotpanel.getBkgEstimPoints();
            updatePlotPanelMain(plotpanel.getSelectedSeries().get(0));
        }
    }
    protected void do_btnRemovePoints_itemStateChanged(ItemEvent e) {

        if (this.btnRemovePoints.isSelected()){
            this.btnRemovePoints.setText("Finish");
            plotpanel.setDeletingBkgPoints(true);
        }else{
            this.btnRemovePoints.setText("Remove Points");
            plotpanel.setDeletingBkgPoints(false);
            this.puntsFons=plotpanel.getBkgEstimPoints();
            updatePlotPanelMain(plotpanel.getSelectedSeries().get(0));
        }
    }
    protected void do_chckbxOnTop_itemStateChanged(ItemEvent e) {
        this.setAlwaysOnTop(chckbxOnTop.isSelected());
    }
    
    private boolean isOneSerieSelected(){
        if (plotpanel.getSelectedSeries().isEmpty()){
            log.info("select a serie first");
            return false;
        }
        if (plotpanel.getSelectedSeries().size()>1){
            log.info("select ONE serie only");
            return false;
        }
        return true;
    }

}

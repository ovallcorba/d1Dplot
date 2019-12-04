package com.vava33.d1dplot;

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

import com.vava33.BasicPlotPanel.core.SerieType;
import com.vava33.d1dplot.auxi.PattOps;
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.jutils.VavaLogger;

import javax.swing.JSeparator;
import javax.swing.JToggleButton;

import java.awt.Font;

//todo crear checkserieselected

public class BackgroundDialog {

    private static final int def_npoints = 30;
    private static final int def_degree = 12;

    private JDialog bkgDialog;
//    private PlotPanel plotpanel;
//    private D1Dplot_main main;

    XRDPlot1DPanel plotpanel;
    D1Dplot_data dades;
    
    private static final String className = "BKG_dialog";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);

    private JPanel contentPanel;
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
    private ButtonGroup buttonGroup;
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
    private JLabel lblCitabruchnker;
    private JLabel lbltini;
    private JTextField txtTini_1;
    
    /**
     * Create the dialog.
     */
    public BackgroundDialog(XRDPlot1DPanel p,D1Dplot_data m) {
    	this.bkgDialog = new JDialog(D1Dplot_global.getD1DmainFrame(),"Background Estimation",false);
        this.contentPanel = new JPanel();
        bkgDialog.setIconImage(D1Dplot_global.getIcon());
        this.plotpanel=p;
        this.dades=m;
//        this.main = m;
        bkgDialog.setBounds(100, 100, 481, 646);
        bkgDialog.getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        bkgDialog.getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new MigLayout("", "[grow][]", "[grow][grow]"));
        {
            JPanel panel = new JPanel();
            panel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Br\u00FCchner", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
            contentPanel.add(panel, "cell 0 0 2 1,grow");
            panel.setLayout(new MigLayout("", "[][grow][]", "[][][][][][][grow][][]"));
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
                lbltini = new JLabel("2"+D1Dplot_global.theta+"ini");
                panel.add(lbltini, "cell 0 2,alignx trailing");
            }
            {
                txtTini_1 = new JTextField();
                txtTini_1.setText("0.0");
                panel.add(txtTini_1, "cell 1 2,growx");
                txtTini_1.setColumns(10);
            }
            {
                JLabel lblPatternEdge = new JLabel("Pattern Edge");
                panel.add(lblPatternEdge, "cell 0 3");
            }
            {
                rdbtnNormal = new JRadioButton("Normal");
                rdbtnNormal.setSelected(true);
                this.buttonGroup = new ButtonGroup();
                buttonGroup.add(rdbtnNormal);
                panel.add(rdbtnNormal, "flowx,cell 1 3");
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
            {
                lblCitabruchnker = new JLabel("<html>\nBrückner, S. (2000). <i>J. Appl. Crystallogr.</i> 33, 977-979.\n</html>");
                lblCitabruchnker.setFont(new Font("Dialog", Font.PLAIN, 11));
                panel.add(lblCitabruchnker, "cell 0 7 3 1");
            }
        }
        {
            JPanel panel = new JPanel();
            panel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Other Methods", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
            contentPanel.add(panel, "cell 0 1 2 1,grow");
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
            bkgDialog.getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("Close");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        do_okButton_actionPerformed(e);
                    }
                });
                buttonPane.setLayout(new MigLayout("", "[71px][grow]", "[25px]"));
                {
                    chckbxOnTop = new JCheckBox("on top");
                    buttonPane.add(chckbxOnTop, "cell 0 0,alignx left,aligny center");
                    chckbxOnTop.addItemListener(new ItemListener() {
                        public void itemStateChanged(ItemEvent e) {
                            do_chckbxOnTop_itemStateChanged(e);
                        }
                    });
                }
                okButton.setActionCommand("OK");
                buttonPane.add(okButton, "cell 1 0,alignx right,aligny top");
                bkgDialog.getRootPane().setDefaultButton(okButton);
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
        plotpanel.setShowEstimPointsBackground(true);
        plotpanel.actualitzaPlot();
        if (!isOneSerieSelected())return;
        txtTini_1.setText(String.format("%.5f", dades.getFirstSelectedDataSerie().getMinX()));
//        plotpanel.bkgEstimP.clearPoints();
    }
    
    private boolean isOneSerieSelected(){
	    return dades.isOneSerieSelected();
	}

	private void updatePlotPanelMain(DataSerie dsactive, DataSerie fonsCalc, DataSerie puntsFons){
	        
	        if (dsactive==null){
	            log.info("Please select the serie");
	            return;
	        }
	        
	        if (fonsCalc!=null){
	            if (fonsCalc.getNPoints()>=0){
	                fonsCalc.setName(dsactive.getName()+" (Background)");
	                dsactive.getParent().replaceDataSerie(fonsCalc, SerieType.bkg, true);
	            }
	        }
	        if (puntsFons!=null){
	            if (puntsFons.getNPoints()!=0){
	                
	                plotpanel.bkgEstimP=puntsFons; //TODO amb copy petava
	            }
	        }
	        dades.updateFullTable();
	        plotpanel.actualitzaPlot();
	    }

	private void do_chckbxMulti_itemStateChanged(ItemEvent e) {
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

    private void do_btnAdd_actionPerformed(ActionEvent e) {
        // afegeix els valors a la taula
        if (!txtTsup.getText().equals("")&&!txtTini.getText().equals("")&&!txtNveins.getText().equals("")){
            Object[] row = new Object[3];
            try{
                row[0]=Double.parseDouble(txtTini.getText());
                row[1]=Double.parseDouble(txtTsup.getText());
                row[2]=Integer.parseInt(txtNveins.getText());
            }catch(Exception ex){
                log.warning("Error reading bkg parameters");
            }
            ((DefaultTableModel)table.getModel()).addRow(row);
        }else{
            log.info("2Tinf, 2Tsup and N must be entered");
        }
    }
    private void do_btnDel_actionPerformed(ActionEvent e) {
        // Elimina la fila seleccionada de la taula
        int numRows = table.getSelectedRows().length;
        for(int i=0; i<numRows; i++){
            ((DefaultTableModel)table.getModel()).removeRow(table.getSelectedRow());
        }
    }
    
    //returns the dat of the selected plottable OR if not possible, the first selected serie (independentment del tipus) intentem primer el dat, sino altres
    private DataSerie getDStoApplyBKG() {
        DataSerie dsactive = dades.getFirstSelectedDataSerie();
        if (dsactive.getSerieType()!=SerieType.dat) dsactive = dades.getSelectedSeriesByType(SerieType.dat).get(0);
        if (dsactive==null)dsactive = dades.getFirstSelectedDataSerie();
        return dsactive;
    }
    
    private void do_btnIterate_actionPerformed(ActionEvent e) {
        if (!isOneSerieSelected())return;
        try{
            int niter = Integer.parseInt(txtNiter.getText());   
            int nveins = Integer.parseInt(txtNveins.getText());
            double t2i = Double.parseDouble(txtTini_1.getText());
            DataSerie dsactive = getDStoApplyBKG();
            //check the point number corresponding to this t2i
            if (t2i<dsactive.getMinX()){
                t2i=dsactive.getMinX();
//                log.debug("t2i="+t2i);
            }
            DataSerie fonsCalc = PattOps.bkg_Bruchner(dsactive, niter, nveins, rdbtnNormal.isSelected(),t2i,chckbxMulti.isSelected(),((DefaultTableModel)table.getModel()));
            
            this.updatePlotPanelMain(dsactive,fonsCalc,null);
        }catch(Exception ex){
            log.warning("Error during background iterations");
        }
    }
    
    private void do_btnFitPolynomial_actionPerformed(ActionEvent e) {
        if (!isOneSerieSelected())return;
        DataSerie dsactive = getDStoApplyBKG();
        if (dsactive==null) {
            log.info("Select a valid serie for bkg calculation");
            return;
        }
        int npoints = def_npoints;
        int degree = def_degree;
        try{
            degree = Integer.parseInt(txtDeg.getText());
            npoints = Integer.parseInt(txtNbkgpoints.getText());
        }catch(Exception ex){
            log.warning("Error parsing polynomial values");
            txtDeg.setText(String.valueOf(degree));
            txtNbkgpoints.setText(String.valueOf(npoints));
        }
        DataSerie puntsFons = plotpanel.bkgEstimP;
        if (puntsFons==null)puntsFons = new DataSerie(SerieType.bkgEstimP,dsactive.getxUnits(),dsactive.getParent());
        if (puntsFons.getNPoints()==0){
            puntsFons = new DataSerie(SerieType.bkgEstimP,dsactive.getxUnits(),dsactive.getParent()); //per si de cas s'ha canviat de pattern
            puntsFons = PattOps.findBkgPoints(dsactive, npoints);
        }
        DataSerie fonsCalc = PattOps.bkg_FitPoly(dsactive, puntsFons, degree);
        updatePlotPanelMain(dsactive,fonsCalc,puntsFons);
    }
    
    private void do_btnFitSpline_actionPerformed(ActionEvent e) {
        if (!isOneSerieSelected())return;
        DataSerie dsactive = getDStoApplyBKG();
        if (dsactive==null) {
            log.info("Select a valid serie for bkg calculation");
            return;
        }
        int npoints = def_npoints;
        try{
            npoints = Integer.parseInt(txtNbkgpoints.getText());
        }catch(Exception ex){
            log.warning("Error parsing number of points");
            txtNbkgpoints.setText(String.valueOf(npoints));
        }

        DataSerie puntsFons = plotpanel.bkgEstimP; //TODO aqui es on falla... hauria de copiar no?
        if (puntsFons==null)puntsFons = new DataSerie(SerieType.bkgEstimP,dsactive.getxUnits(),dsactive.getParent());
        if (puntsFons.getNPoints()==0){
            puntsFons = new DataSerie(SerieType.bkgEstimP,dsactive.getxUnits(),dsactive.getParent());
            puntsFons = PattOps.findBkgPoints(dsactive, npoints);
        }
        //ordenem puntsfonts
        puntsFons.sortPoints();
        DataSerie fonsCalc = PattOps.bkg_FitSpline(dsactive, puntsFons);
        updatePlotPanelMain(dsactive,fonsCalc,puntsFons);
        
    }
    private void do_btnEstimate_actionPerformed(ActionEvent e) {
        if (!isOneSerieSelected())return;
        DataSerie dsactive = getDStoApplyBKG();
        if (dsactive==null) {
            log.info("Select a valid serie for bkg calculation");
            return;
        }
        int npoints = def_npoints;
        try{
            npoints = Integer.parseInt(txtNbkgpoints.getText());
        }catch(Exception ex){
            log.warning("Error parsing number of points");
            txtNbkgpoints.setText(String.valueOf(npoints));
        }
        DataSerie puntsFons = plotpanel.bkgEstimP;
        if (puntsFons==null)puntsFons = new DataSerie(SerieType.bkgEstimP,dsactive.getxUnits(),dsactive.getParent());
        if (puntsFons.getNPoints()==0){
            puntsFons = new DataSerie(SerieType.bkgEstimP,dsactive.getxUnits(),dsactive.getParent());
            puntsFons = PattOps.findBkgPoints(dsactive, npoints);
        }
        updatePlotPanelMain(dsactive,null,puntsFons);
    }
    
    private void do_btnRemoveAllBkg_actionPerformed(ActionEvent e) {
        if (!isOneSerieSelected())return;
        dades.getFirstSelectedDataSerie().getParent().removeDataSeriesByType(SerieType.bkg);
        plotpanel.bkgEstimP.clearPoints();
        dades.updateFullTable();
        plotpanel.actualitzaPlot();
    }
    
    private void do_okButton_actionPerformed(ActionEvent e) {
        this.tanca();
    }
   
    
    //ARA NO CAL UPDATE PERQUE TREBALLEM SOBRE EL MATEIX PATTERN
    private void do_btnAddPoints_itemStateChanged(ItemEvent arg0) {
        if (!isOneSerieSelected())return;
        if (this.btnAddPoints.isSelected()){
            this.btnAddPoints.setText("Finish");
            plotpanel.setSelectingBkgPoints(true);
        }else{
            this.btnAddPoints.setText("Add Points");
            plotpanel.setSelectingBkgPoints(false);
        }
    }
    private void do_btnRemovePoints_itemStateChanged(ItemEvent e) {

        if (this.btnRemovePoints.isSelected()){
            this.btnRemovePoints.setText("Finish");
            plotpanel.setDeletingBkgPoints(true);
        }else{
            this.btnRemovePoints.setText("Remove Points");
            plotpanel.setDeletingBkgPoints(false);;
        }
    }
    
    private void do_chckbxOnTop_itemStateChanged(ItemEvent e) {
		bkgDialog.setAlwaysOnTop(chckbxOnTop.isSelected());
	}

	public void tanca() {
        this.btnAddPoints.setText("Add Points");
        this.btnRemovePoints.setText("Remove Points");
        plotpanel.setDeletingBkgPoints(false);
        plotpanel.setSelectingBkgPoints(false);
        plotpanel.setShowEstimPointsBackground(false);
        plotpanel.actualitzaPlot();
        bkgDialog.dispose();
    }
    
    public void visible(boolean vis) {
    	bkgDialog.setVisible(vis);
    	if (vis)inicia();
    }
}

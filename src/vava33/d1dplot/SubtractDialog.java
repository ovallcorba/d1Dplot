package com.vava33.d1dplot;

/**
 * D1Dplot
 * 
 * Subtraction of patterns (dialog)
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
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.d1dplot.data.Data_Common;
import com.vava33.d1dplot.data.Plottable;
import com.vava33.jutils.FileUtils;
import com.vava33.jutils.VavaLogger;

import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import java.awt.Font;

import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.LineBorder;

import java.awt.Color;

import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.JScrollPane;

public class SubtractDialog {

	private JDialog subtractDialog;
    private PlotPanel plotpanel;
    
    private static final String className = "SubtractDialog";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);
    
    private JPanel contentPanel;
    private JTextField txtFactor;
    private JCheckBox chckbxAutoScale;
    private JTextField txtTini;
    private JTextField txtTfin;
    private JTable tableDS1;
    private JTable tableDS2;
    
    /**
     * Create the dialog.
     */
    public SubtractDialog(PlotPanel p) {
        this.subtractDialog = new JDialog(D1Dplot_global.getD1DmainFrame(),"Subtract Patterns",false);
        this.plotpanel = p;
        this.contentPanel = new JPanel();
        subtractDialog.setIconImage(D1Dplot_global.getIcon());
        subtractDialog.setBounds(100, 100, 814, 240);
        subtractDialog.getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        subtractDialog.getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new MigLayout("", "[grow][][grow]", "[][grow][grow]"));
        {
            JPanel panel = new JPanel();
            panel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
            contentPanel.add(panel, "cell 0 0,grow");
            panel.setLayout(new MigLayout("", "[grow]", "[][grow]"));
            {
                JLabel lblSelectBasePattern = new JLabel("Select Base Pattern");
                panel.add(lblSelectBasePattern, "cell 0 0");
            }
            {
                {
                    JScrollPane scrollPane = new JScrollPane();
                    panel.add(scrollPane, "cell 0 1,grow");
                    tableDS1 = new JTable();
                    scrollPane.setViewportView(tableDS1);
                    tableDS1.setDefaultRenderer(String.class, new PatternsTableCellRenderer());
                    tableDS1.setDefaultRenderer(Integer.class, new PatternsTableCellRenderer());
                    tableDS1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    tableDS1.setColumnSelectionAllowed(false);
                    tableDS1.setCellSelectionEnabled(false);
                }
            }
        }
        {
            JLabel lblNewLabel = new JLabel("-");
            lblNewLabel.setFont(new Font("Dialog", Font.BOLD, 30));
            contentPanel.add(lblNewLabel, "cell 1 0,alignx trailing,growy");
        }
        {
            JPanel panel = new JPanel();
            panel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            contentPanel.add(panel, "cell 2 0,grow");
            panel.setLayout(new MigLayout("", "[][][grow]", "[][grow]"));
            {
                JLabel lblSelectPatternTo = new JLabel("Select Pattern to subtract");
                panel.add(lblSelectPatternTo, "cell 0 0 3 1");
            }
            {
                txtFactor = new JTextField();
                panel.add(txtFactor, "cell 0 1");
                txtFactor.setText("1.0");
                txtFactor.setColumns(10);
            }
            {
                JLabel lblX = new JLabel("x");
                panel.add(lblX, "cell 1 1");
                lblX.setFont(new Font("Dialog", Font.BOLD, 20));
            }
            {
                {
                    JScrollPane scrollPane = new JScrollPane();
                    panel.add(scrollPane, "cell 2 1,grow");
                    tableDS2 = new JTable();
                    scrollPane.setViewportView(tableDS2);
                    tableDS2.setDefaultRenderer(String.class, new PatternsTableCellRenderer());
                    tableDS2.setDefaultRenderer(Integer.class, new PatternsTableCellRenderer());
                    tableDS2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    tableDS2.setColumnSelectionAllowed(false);
                    tableDS2.setCellSelectionEnabled(false);
                }
            }
        }
        {
            JButton btnSubtract = new JButton("Subtract!");
            btnSubtract.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    do_btnSubtract_actionPerformed(e);
                }
            });
            {
                JPanel panel = new JPanel();
                contentPanel.add(panel, "cell 0 1 3 1,alignx center,growy");
                panel.setLayout(new MigLayout("", "[99px][][][][]", "[23px]"));
                {
                    chckbxAutoScale = new JCheckBox("Auto scale,  range (for scaling):");
                    chckbxAutoScale.addItemListener(new ItemListener() {
                        public void itemStateChanged(ItemEvent arg0) {
                            do_chckbxAutoScale_itemStateChanged(arg0);
                        }
                    });
                    panel.add(chckbxAutoScale, "cell 0 0,alignx left,aligny top");
                }
                {
                    JLabel lblti = new JLabel("2Ti");
                    panel.add(lblti, "cell 1 0,alignx trailing");
                }
                {
                    txtTini = new JTextField();
                    panel.add(txtTini, "cell 2 0,alignx left");
                    txtTini.setColumns(10);
                }
                {
                    JLabel lbltf = new JLabel("2Tf");
                    panel.add(lbltf, "cell 3 0,alignx trailing");
                }
                {
                    txtTfin = new JTextField();
                    panel.add(txtTfin, "cell 4 0,alignx left");
                    txtTfin.setColumns(10);
                }
            }
            contentPanel.add(btnSubtract, "cell 0 2 3 1,growx,aligny bottom");
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            subtractDialog.getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("Close");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        do_okButton_actionPerformed(e);
                    }
                });
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                subtractDialog.getRootPane().setDefaultButton(okButton);
            }
        }
        
        inicia();
    }

    private void inicia(){
        
        updateTables();
        
    }
    
    private void updateTables() {
        DefaultTableModel mod = new DefaultTableModel(
                new Object[][] {
                },
                new String[] {
                        "nP", "nS", "Name"
                }
                ){
                    private static final long serialVersionUID = 1L;
                    Class<?>[] columnTypes = new Class[] {
                            Integer.class, Integer.class, String.class
                    };
                    public Class<?> getColumnClass(int columnIndex) {
                        return columnTypes[columnIndex];
                    }
                };
        mod.setRowCount(0);
        for (int i=0;i<plotpanel.getNplottables();i++) {
            Plottable p = plotpanel.getPlottable(i);
            int np=plotpanel.indexOfPlottableData(p);
            for (DataSerie d: p.getDataSeries()) {
                if(d.isEmpty())continue;
                int nd = p.indexOfDS(d);
                Object[] row = {np,nd,d.serieName};
                mod.addRow(row);
            }
        }
        tableDS1.setModel(mod);
        tableDS2.setModel(mod);
    }


    private void do_okButton_actionPerformed(ActionEvent e) {
    	subtractDialog.dispose();
    }

    
    
    private void do_btnSubtract_actionPerformed(ActionEvent e) {
        int r1 = tableDS1.getSelectedRow();
        int r2 = tableDS2.getSelectedRow();
        if ((r1<0)||(r2<0)) {
            log.info("Please select patterns for the subtract operation");
            return;
        }
        int np1 = (Integer) tableDS1.getValueAt(r1, 0);
        int np2 = (Integer) tableDS2.getValueAt(r2, 0);
        int nds1 = (Integer) tableDS1.getValueAt(r1, 1);
        int nds2 = (Integer) tableDS2.getValueAt(r2, 1);
        DataSerie ds1 = plotpanel.getPlottable(np1).getDataSerie(nds1);
        DataSerie ds2 = plotpanel.getPlottable(np2).getDataSerie(nds2);

        float factor = 1.0f;
        double fac_t2i = 0.0f;
        double fac_t2f = 100.0f;
        if (chckbxAutoScale.isSelected()) {
            factor = -1.0f;
            try{
                fac_t2i = Double.parseDouble(txtTini.getText());
            }catch(Exception ex){
                log.warning("Error reading factor t2i, using 0.0");
            }
            try{
                fac_t2f = Double.parseDouble(txtTfin.getText());
            }catch(Exception ex){
                log.warning("Error reading factor t2f, using 100.0");
            }
        }else {
            try{
                factor = Float.parseFloat(txtFactor.getText());
            }catch(Exception ex){
                log.warning("Error reading factor, using 1.0");
            }
        }
        
        DataSerie result = null;
        if (!PattOps.haveCoincidentPointsDS(ds1, ds2)){
            boolean cont = FileUtils.YesNoDialog(subtractDialog, "No coincident points, rebinning required. Continue?");
            if (!cont)return;
            DataSerie ds2reb = PattOps.rebinDS(ds1, ds2);
            log.info("Rebinning performed on serie "+ds2.serieName);
            ds2reb.serieName="rebinned serie";
            result = PattOps.subtractDataSeriesCoincidentPoints(ds1, ds2reb, factor,fac_t2i,fac_t2f);
        }else{
            result = PattOps.subtractDataSeriesCoincidentPoints(ds1, ds2, factor,fac_t2i,fac_t2f);
        }
        if (result==null){
            log.warning("Error in subtraction");
            return;
        }

        //he posat a l'scale el factor utilitzat
        float usedFactor = result.getScale();
        txtFactor.setText(FileUtils.dfX_2.format(usedFactor));
        result.setScale(1);
        
        result.serieName=String.format("#Sub of: P%dS%d - %.2f*P%dS%d", np1,nds1,factor,np2,nds2);
        Data_Common dc = new Data_Common(ds1.getWavelength()); //agafem la wavelength del dataserie per si s'ha actualitzat
        
        dc.addDataSerie(result);
        dc.addCommentLines(ds1.getCommentLines());
        String s = String.format("#Subtracted pattern: %s - %.2f*%s",ds1.serieName,factor,ds2.serieName);
        dc.addCommentLine(s);
        dc.setOriginalWavelength(ds1.getOriginalWavelength());
        dc.setWavelengthToAllSeries(ds1.getWavelength());
        plotpanel.addPlottable(dc);
        D1Dplot_global.getD1Dmain().updateData(false,false);
    }
    
    private void do_chckbxAutoScale_itemStateChanged(ItemEvent arg0) {
        if (chckbxAutoScale.isSelected()) {
            int r1 = tableDS1.getSelectedRow();
            if ((r1<0)) {
                log.info("Please select patterns for the subtract operation");
                return;
            }
            DataSerie ds1 = plotpanel.getPlottable((Integer) tableDS1.getValueAt(r1, 0)).getDataSerie((Integer) tableDS1.getValueAt(r1, 1));
            txtFactor.setEnabled(false);
            txtTini.setText(Double.toString(ds1.getMinX()));
            txtTfin.setText(Double.toString(ds1.getMaxX()));
        }else {
            txtFactor.setEnabled(true);
        }
    }
    
    public void visible(boolean vis) {
    	this.subtractDialog.setVisible(vis);
    	if (vis)inicia();
    }
}

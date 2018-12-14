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

import com.vava33.d1dplot.auxi.DataSerie;
import com.vava33.d1dplot.auxi.PattOps;
import com.vava33.d1dplot.auxi.Pattern1D;
import com.vava33.jutils.FileUtils;
import com.vava33.jutils.VavaLogger;

import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JComboBox;

import java.awt.Font;

import javax.swing.border.TitledBorder;
import javax.swing.border.LineBorder;

import java.awt.Color;

import javax.swing.JTextField;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.JCheckBox;

public class SubtractDialog {

	private JDialog subtractDialog;
    private PlotPanel plotpanel;
    
    private static final String className = "SubtractDialog";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);
    
    private JPanel contentPanel;
    private D1Dplot_main main;
    private JTextField txtFactor;
    private JComboBox<Integer> combo_patt1;
    private JComboBox<Integer> combo_serie1;
    private JComboBox<Integer> combo_patt2;
    private JComboBox<Integer> combo_serie2;
    private JCheckBox chckbxAutoScale;
    private JTextField txtTini;
    private JTextField txtTfin;
    
    /**
     * Create the dialog.
     */
    public SubtractDialog(D1Dplot_main m) {
        this.subtractDialog = new JDialog(m.getMainFrame(),"Subtract Patterns",false);
        this.main = m;
        this.plotpanel = m.getPanel_plot();
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
            panel.setLayout(new MigLayout("", "[grow][grow]", "[][]"));
            {
                JLabel lblPattern = new JLabel("Pattern");
                panel.add(lblPattern, "cell 0 0");
            }
            {
                combo_patt1 = new JComboBox<Integer>();
                combo_patt1.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent arg0) {
                        do_combo_patt1_itemStateChanged(arg0);
                    }
                });
                panel.add(combo_patt1, "cell 1 0,growx");
            }
            {
                JLabel lblSerie = new JLabel("Serie");
                panel.add(lblSerie, "cell 0 1");
            }
            {
                combo_serie1 = new JComboBox<Integer>();
                panel.add(combo_serie1, "cell 1 1,growx");
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
            panel.setLayout(new MigLayout("", "[][][][grow]", "[][]"));
            {
                JLabel lblPattern_1 = new JLabel("Pattern");
                panel.add(lblPattern_1, "cell 2 0,alignx trailing");
            }
            {
                combo_patt2 = new JComboBox<Integer>();
                combo_patt2.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        do_combo_patt2_itemStateChanged(e);
                    }
                });
                panel.add(combo_patt2, "cell 3 0,growx");
            }
            {
                txtFactor = new JTextField();
                panel.add(txtFactor, "cell 0 0 1 2");
                txtFactor.setText("1.0");
                txtFactor.setColumns(10);
            }
            {
                JLabel lblX = new JLabel("x");
                panel.add(lblX, "cell 1 0 1 2");
                lblX.setFont(new Font("Dialog", Font.BOLD, 20));
            }
            {
                JLabel lblSerie_1 = new JLabel("Serie");
                panel.add(lblSerie_1, "cell 2 1,alignx trailing");
            }
            {
                combo_serie2 = new JComboBox<Integer>();
                panel.add(combo_serie2, "cell 3 1,growx");
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
        combo_patt1.removeAllItems();
        for (int i=0; i<plotpanel.getPatterns().size(); i++){
            combo_patt1.addItem(i);    
        }
        combo_patt2.removeAllItems();
        for (int i=0; i<plotpanel.getPatterns().size(); i++){
            combo_patt2.addItem(i);    
        }
        
        updateComboSerie1();
        updateComboSerie2();
        
    }
    
    private void updateComboSerie1(){
        int p1 = (Integer) combo_patt1.getSelectedItem();
        combo_serie1.removeAllItems();
        for (int i=0; i<plotpanel.getPatterns().get(p1).getNseries();i++){
            combo_serie1.addItem(i);
        }
    }
    private void updateComboSerie2(){
        int p2 = (Integer) combo_patt2.getSelectedItem();
        combo_serie2.removeAllItems();
        for (int i=0; i<plotpanel.getPatterns().get(p2).getNseries();i++){
            combo_serie2.addItem(i);
        }
    }

    private void do_okButton_actionPerformed(ActionEvent e) {
    	subtractDialog.dispose();
    }
    private void do_combo_patt1_itemStateChanged(ItemEvent arg0) {
        updateComboSerie1();
    }
    private void do_combo_patt2_itemStateChanged(ItemEvent e) {
        updateComboSerie2();
    }
    
    
    private void do_btnSubtract_actionPerformed(ActionEvent e) {
        int np1 = (Integer) combo_patt1.getSelectedItem();
        int ns1 = (Integer) combo_serie1.getSelectedItem();
        int np2 = (Integer) combo_patt2.getSelectedItem();
        int ns2 = (Integer) combo_serie2.getSelectedItem();
        
        DataSerie ds1 = plotpanel.getPatterns().get(np1).getSerie(ns1);
        DataSerie ds2 = plotpanel.getPatterns().get(np2).getSerie(ns2);
        
        if (ds1.getNpoints()!=ds2.getNpoints()){
            log.debug("Different number of points between series");
        }
        if (ds1.getPoint(0).getX()!=ds2.getPoint(0).getX()){
            log.debug("Different first point on both series");
        }

        float factor = 1.0f;
        double fac_t2i = 0.0f;
        double fac_t2f = 100.0f;
        if (chckbxAutoScale.isSelected()) {
            factor = -1.0f;
            try{
                fac_t2i = Double.parseDouble(txtTini.getText());
            }catch(Exception ex){
                ex.printStackTrace();
                log.warning("Error reading factor t2i, using 0.0");
            }
            try{
                fac_t2f = Double.parseDouble(txtTfin.getText());
            }catch(Exception ex){
                ex.printStackTrace();
                log.warning("Error reading factor t2f, using 100.0");
            }
        }else {
            try{
                factor = Float.parseFloat(txtFactor.getText());
            }catch(Exception ex){
                ex.printStackTrace();
                log.warning("Error reading factor, using 1.0");
            }
        }
        
        DataSerie result = null;
        if (!PattOps.haveCoincidentPointsDS(ds1, ds2)){
            boolean cont = FileUtils.YesNoDialog(subtractDialog, "No coincident points, rebinning required. Continue?");
            if (!cont)return;
            DataSerie ds2reb = PattOps.rebinDS(ds1, ds2);
            log.info("Rebinning performed on serie "+ds2.getSerieName());
            //debug
            ds2reb.setPatt1D(ds2.getPatt1D());
            main.updateData(false);
            ds2reb.setSerieName("rebinned serie");
            result = PattOps.subtractDataSeriesCoincidentPoints(ds1, ds2reb, factor,fac_t2i,fac_t2f);
        }else{
            result = PattOps.subtractDataSeriesCoincidentPoints(ds1, ds2, factor,fac_t2i,fac_t2f);
        }
        if (result==null){
            log.warning("Error in subtraction");
            return;
        }

        result.setSerieName(String.format("#Sub of: P%dS%d - %.2f*P%dS%d", np1,ns1,factor,np2,ns2));
        Pattern1D patt = new Pattern1D();
        patt.getCommentLines().addAll(ds1.getPatt1D().getCommentLines());
        String s = String.format("#Subtracted pattern: %s - %.2f*%s",ds1.getSerieName(),factor,ds2.getSerieName());
        patt.getCommentLines().add(s);
        patt.setOriginal_wavelength(ds1.getPatt1D().getOriginal_wavelength());
        patt.addDataSerie(result);
        plotpanel.getPatterns().add(patt);
        main.updateData(false);
    }
    
    private void do_chckbxAutoScale_itemStateChanged(ItemEvent arg0) {
        if (chckbxAutoScale.isSelected()) {
            int np1 = (Integer) combo_patt1.getSelectedItem();
            int ns1 = (Integer) combo_serie1.getSelectedItem();
            txtFactor.setEnabled(false);
            txtTini.setText(Double.toString(plotpanel.getPatterns().get(np1).getSerie(ns1).getT2i()));
            txtTfin.setText(Double.toString(plotpanel.getPatterns().get(np1).getSerie(ns1).getT2f()));
        }else {
            txtFactor.setEnabled(true);
        }
    }
    
    public void visible(boolean vis) {
    	this.subtractDialog.setVisible(vis);
    	if (vis)inicia();
    }
}

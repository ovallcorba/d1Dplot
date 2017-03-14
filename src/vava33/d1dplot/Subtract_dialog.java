package vava33.d1dplot;

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

import vava33.d1dplot.auxi.DataSerie;
import vava33.d1dplot.auxi.PattOps;
import vava33.d1dplot.auxi.Pattern1D;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

public class Subtract_dialog extends JDialog {

    private static final long serialVersionUID = 8174469328033594668L;
    private PlotPanel plotpanel;
    private D1Dplot_main main;
    private static VavaLogger log = D1Dplot_global.getVavaLogger(Subtract_dialog.class.getName());

    private final JPanel contentPanel = new JPanel();
    private JTextField txtFactor;
    private JComboBox combo_patt1;
    private JComboBox combo_serie1;
    private JComboBox combo_patt2;
    private JComboBox combo_serie2;
    
    /**
     * Create the dialog.
     */
    public Subtract_dialog(PlotPanel p,D1Dplot_main m) {
        setTitle("Subtract Patterns");
        this.setIconImage(D1Dplot_global.getIcon());
        this.setPlotpanel(p);
        this.setMain(m);
        setBounds(100, 100, 814, 198);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new MigLayout("", "[grow][][grow]", "[][grow]"));
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
                combo_patt1 = new JComboBox();
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
                combo_serie1 = new JComboBox();
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
                combo_patt2 = new JComboBox();
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
                combo_serie2 = new JComboBox();
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
            contentPanel.add(btnSubtract, "cell 0 1 3 1,growx,aligny bottom");
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

    public void inicia(){
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
    
    public void updateComboSerie1(){
        int p1 = (Integer) combo_patt1.getSelectedItem();
        combo_serie1.removeAllItems();
        for (int i=0; i<plotpanel.getPatterns().get(p1).getNseries();i++){
            combo_serie1.addItem(i);
        }
    }
    public void updateComboSerie2(){
        int p2 = (Integer) combo_patt2.getSelectedItem();
        combo_serie2.removeAllItems();
        for (int i=0; i<plotpanel.getPatterns().get(p2).getNseries();i++){
            combo_serie2.addItem(i);
        }
    }
    
    public PlotPanel getPlotpanel() {
        return plotpanel;
    }

    public void setPlotpanel(PlotPanel plotpanel) {
        this.plotpanel = plotpanel;
    }

    public D1Dplot_main getMain() {
        return main;
    }

    public void setMain(D1Dplot_main main) {
        this.main = main;
    }

    protected void do_okButton_actionPerformed(ActionEvent e) {
        this.dispose();
    }
    protected void do_combo_patt1_itemStateChanged(ItemEvent arg0) {
        updateComboSerie1();
    }
    protected void do_combo_patt2_itemStateChanged(ItemEvent e) {
        updateComboSerie2();
    }
    
    
    protected void do_btnSubtract_actionPerformed(ActionEvent e) {
        int np1 = (Integer) combo_patt1.getSelectedItem();
        int ns1 = (Integer) combo_serie1.getSelectedItem();
        int np2 = (Integer) combo_patt2.getSelectedItem();
        int ns2 = (Integer) combo_serie2.getSelectedItem();
        
        DataSerie ds1 = plotpanel.getPatterns().get(np1).getSerie(ns1);
        DataSerie ds2 = plotpanel.getPatterns().get(np2).getSerie(ns2);
        
        if (ds1.getNpoints()!=ds2.getNpoints()){
            loginfo("different number of points");
        }
        if (ds1.getPoint(0).getX()!=ds2.getPoint(0).getX()){
            loginfo("different first point");
        }
        float factor = 1.0f;
        try{
            factor = Float.parseFloat(txtFactor.getText());
        }catch(Exception ex){
            ex.printStackTrace();
            loginfo("error reading factor, using 1.0");
        }
        
        DataSerie result = null;
        if (!PattOps.haveCoincidentPointsDS(ds1, ds2)){
            boolean cont = FileUtils.YesNoDialog(this, "No coincident points, rebinning required. Continue?");
            if (!cont)return;
            DataSerie ds2reb = PattOps.rebinDS(ds1, ds2);
            loginfo("rebinning performed on serie "+ds2.getSerieName());
            //debug
            ds2reb.setPatt1D(ds2.getPatt1D());
            main.updateData();
            ds2reb.setSerieName("rebinned serie");
            result = PattOps.subtractDataSeriesCoincidentPoints(ds1, ds2reb, factor);
        }else{
            result = PattOps.subtractDataSeriesCoincidentPoints(ds1, ds2, factor);
        }
        if (result==null){
            loginfo("error in subtraction");
            return;
        }
        result.setSerieName(String.format("Sub of: P%dS%d - %.2f*P%dS%d", np1,ns1,factor,np2,ns2));
        Pattern1D patt = new Pattern1D();
        patt.getCommentLines().addAll(ds1.getPatt1D().getCommentLines());
        String s = String.format("Subtracted pattern: %s - %.2f*%s",ds1.getSerieName(),factor,ds2.getSerieName());
        patt.getCommentLines().add(s);
        patt.setOriginal_wavelength(ds1.getPatt1D().getOriginal_wavelength());
        patt.AddDataSerie(result);
        plotpanel.getPatterns().add(patt);
        main.updateData();
    }
    
    private void loginfo(String s){
        if (D1Dplot_global.logging){
            log.info(s);
        }
        if(main!=null)main.getTAOut().stat(s); //ho passem pel txtArea
    }
}

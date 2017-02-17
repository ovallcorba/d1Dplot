package vava33.d1dplot;

/**
 * D1Dplot
 * 
 * Find Peaks dialog
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;

import javax.swing.JCheckBox;

import com.vava33.jutils.FileUtils;
import com.vava33.jutils.VavaLogger;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileNameExtensionFilter;

import vava33.d1dplot.auxi.DataFileUtils;
import vava33.d1dplot.auxi.DataSerie;

import javax.swing.JToggleButton;

import org.apache.commons.math3.util.FastMath;

public class FindPeaks_dialog extends JDialog {

    private PlotPanel plotpanel;
    private D1Dplot_main main;
    private int sliderFactDiv=100;
    private static VavaLogger log = D1Dplot_global.getVavaLogger(FindPeaks_dialog.class.getName());

    private final JPanel contentPanel = new JPanel();
    private JCheckBox chckbxShowPeaks;
    private JCheckBox chckbxOnTop;
    private JLabel lblFact;
    private JSlider slider;
    private JCheckBox chckbxUseBkgEstimation;
    private JButton btnAutoPeakSearch;
    private JToggleButton btnAddPeaks;
    private JToggleButton btnRemovePeaks;
    private JButton btnSetFactminmax;


    /**
     * Create the dialog.
     */
    public FindPeaks_dialog(PlotPanel p, D1Dplot_main m) {
        this.plotpanel=p;
        this.main=m;
        setTitle("Find Peaks");
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(FindPeaks_dialog.class.getResource("/vava33/d1dplot/img/d1Dplot.png")));
        setBounds(100, 100, 450, 290);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new MigLayout("", "[grow][]", "[][grow][]"));
        {
            chckbxShowPeaks = new JCheckBox("Show peaks");
            chckbxShowPeaks.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    do_chckbxShowPeaks_itemStateChanged(e);
                }
            });
            contentPanel.add(chckbxShowPeaks, "cell 0 0");
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
            contentPanel.add(panel, "cell 0 1 2 1,grow");
            panel.setLayout(new MigLayout("", "[][][grow]", "[][][][]"));
            {
                btnAutoPeakSearch = new JButton("Auto Peak Search");
                btnAutoPeakSearch.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        do_btnAutoPeakSearch_actionPerformed(e);
                    }
                });
                panel.add(btnAutoPeakSearch, "cell 0 0,growx");
            }
            {
                chckbxUseBkgEstimation = new JCheckBox("use bkg estimation");
                chckbxUseBkgEstimation.setSelected(true);
                panel.add(chckbxUseBkgEstimation, "cell 1 0 2 1");
            }
            {
                btnAddPeaks = new JToggleButton("Add Peaks");
                btnAddPeaks.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        do_btnAddPeaks_itemStateChanged(e);
                    }
                });
                panel.add(btnAddPeaks, "cell 0 1,growx");
            }
            {
                JLabel lblFactor = new JLabel("factor:");
                panel.add(lblFactor, "cell 1 1");
            }
            {
                lblFact = new JLabel("fact");
                panel.add(lblFact, "cell 2 1,alignx center");
            }
            {
                btnRemovePeaks = new JToggleButton("Remove Peaks");
                btnRemovePeaks.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        do_btnRemovePeaks_itemStateChanged(e);
                    }
                });
                panel.add(btnRemovePeaks, "cell 0 2,growx");
            }
            {
                slider = new JSlider();
                slider.setMaximum(400);
                slider.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent arg0) {
                        do_slider_stateChanged(arg0);
                    }
                });
                slider.setValue(105);
                slider.setPaintTicks(true);
                slider.setPaintLabels(true);
                panel.add(slider, "cell 1 2 2 1,grow");
            }
            {
                JButton btnRemoveAll = new JButton("Remove All");
                btnRemoveAll.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        do_btnRemoveAll_actionPerformed(e);
                    }
                });
                panel.add(btnRemoveAll, "cell 0 3,growx");
            }
            {
                btnSetFactminmax = new JButton("set factMinMax");
                btnSetFactminmax.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        do_btnSetFactminmax_actionPerformed(arg0);
                    }
                });
                panel.add(btnSetFactminmax, "cell 2 3,alignx right");
            }
        }
        {
            JButton btnSavePeaks = new JButton("Save Peaks");
            btnSavePeaks.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    do_btnSavePeaks_actionPerformed(e);
                }
            });
            contentPanel.add(btnSavePeaks, "cell 0 2 2 1,growx");
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
        chckbxShowPeaks.setSelected(true);
        slider.setValue(115);
        lblFact.setText(Float.toString((float)slider.getValue()/sliderFactDiv));
    }

    protected void do_chckbxShowPeaks_itemStateChanged(ItemEvent e) {
        plotpanel.setShowPeaks(chckbxShowPeaks.isSelected());
    }
    
    protected void do_chckbxOnTop_itemStateChanged(ItemEvent e) {
        this.setAlwaysOnTop(chckbxOnTop.isSelected());
    }
    
    protected void do_btnAutoPeakSearch_actionPerformed(ActionEvent e) {
        
        if (!isOneSerieSelected())return;

        try{
            float fact = (float)slider.getValue()/sliderFactDiv;
            boolean usebkg = chckbxUseBkgEstimation.isSelected();
            DataSerie llindar = plotpanel.getSelectedSeries().get(0).findPeaksEvenBetter(fact,usebkg);
            if (llindar!=null){
                llindar.setScale(fact);
                plotpanel.setBkgseriePeakSearch(llindar);
            }else{
                plotpanel.getBkgseriePeakSearch().clearDataPoints();
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        plotpanel.repaint();
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
    protected void do_slider_stateChanged(ChangeEvent arg0) {
        lblFact.setText(Float.toString((float)slider.getValue()/sliderFactDiv));
        btnAutoPeakSearch.doClick();
    }
    protected void do_okButton_actionPerformed(ActionEvent e) {
        this.dispose();
    }
    protected void do_btnAddPeaks_itemStateChanged(ItemEvent e) {
        if (!isOneSerieSelected())return;
        if (this.btnAddPeaks.isSelected()){
            this.btnAddPeaks.setText("Finish");
            plotpanel.setSelectingPeaks(true);
        }else{
            this.btnAddPeaks.setText("Add Peaks");
            plotpanel.setSelectingPeaks(false);
        }
    }

    protected void do_btnRemovePeaks_itemStateChanged(ItemEvent e) {
        if (!isOneSerieSelected())return;
        if (this.btnRemovePeaks.isSelected()){
            this.btnRemovePeaks.setText("Finish");
            plotpanel.setDeletingPeaks(true);
        }else{
            this.btnRemovePeaks.setText("Remove Peaks");
            plotpanel.setDeletingPeaks(false);
        }
    }
    
    protected void do_btnRemoveAll_actionPerformed(ActionEvent e) {
        if (!isOneSerieSelected())return;
        plotpanel.getSelectedSeries().get(0).clearPeaks();
        plotpanel.repaint();
    }
    protected void do_btnSetFactminmax_actionPerformed(ActionEvent arg0) {
        String s = (String)JOptionPane.showInputDialog(
                this,
                "Enter factor slide Min Max=",
                "Set Factor slide Min Max",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                FileUtils.dfX_2.format((float)slider.getMinimum()/100f)+" "+FileUtils.dfX_2.format((float)slider.getMaximum()/100f));
        
        if ((s != null) && (s.length() > 0)) {
            try{
                String[] vals = s.split("\\s+");
                float min = Float.parseFloat(vals[0]);
                slider.setMinimum(FastMath.round(min*100));
                float max = Float.parseFloat(vals[1]);
                slider.setMaximum(FastMath.round(max*100));
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }
    protected void do_btnSavePeaks_actionPerformed(ActionEvent e) {
        if (!isOneSerieSelected())return;
        File pksFile = FileUtils.fchooser(this,new File(D1Dplot_global.getWorkdir()), null, 0, true, true);
        if (pksFile == null){
            log.info("No data file selected");
            return;
        }
        
        pksFile = DataFileUtils.writePeaksFile(pksFile, plotpanel.getSelectedSeries().get(0).getPatt1D(), plotpanel.getSelectedSeries().get(0).getPatt1D().indexOfSerie(plotpanel.getSelectedSeries().get(0)), true);
        log.info(pksFile.toString()+" written!");
        log.debug("saveDataFile exited");
    }
}

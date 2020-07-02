package com.vava33.d1dplot;

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
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;

import javax.swing.JCheckBox;

import com.vava33.BasicPlotPanel.core.SerieType;
import com.vava33.d1dplot.auxi.DataFileUtils;
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.jutils.FileUtils;
import com.vava33.jutils.VavaLogger;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.JLabel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JToggleButton;

import org.apache.commons.math3.util.FastMath;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import com.vava33.d1dplot.auxi.DoubleJSlider;
import com.vava33.d1dplot.auxi.PattOps;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class FindPeaksDialog {

	private JDialog findPeaksDialog;
    XRDPlot1DPanel plotpanel;
    D1Dplot_data dades;
    private IndexDialog id;
    private int sliderFactDiv=100;
    
    private static final String className = "PKS_dialog";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);
    
    private JPanel contentPanel;
    private JCheckBox chckbxOnTop;
    private JLabel lblFact;
    private DoubleJSlider slider;
    private JCheckBox chckbxUseBkgEstimation;
    private JButton btnAutoPeakSearch;
    private JToggleButton btnAddPeaks;
    private JToggleButton btnRemovePeaks;
    private JButton btnSetFactminmax;
    private JLabel lblTmin;
    private JLabel lblTmax;
    private JTextField txtTthmin;
    private JTextField txtTthmax;
    private JButton btnIndex;


    /**
     * Create the dialog.
     */
    public FindPeaksDialog(XRDPlot1DPanel p, D1Dplot_data d) {
        this.plotpanel=p;
        this.dades=d;
        this.contentPanel = new JPanel();
        this.findPeaksDialog = new JDialog(D1Dplot_global.getD1DmainFrame(),"Find Peaks",false);
        findPeaksDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                do_findPeaksDialog_windowClosing(e);
            }
        });
        findPeaksDialog.setIconImage(D1Dplot_global.getIcon());
//        findPeaksDialog.setBounds(100, 100, 370, 540);
        findPeaksDialog.setSize(370,540);
        D1Dplot_global.showOnScreen(D1Dplot_global.getDisplayMonitor(), findPeaksDialog, true);
        findPeaksDialog.getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        findPeaksDialog.getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new MigLayout("", "[grow][]", "[grow][][]"));
        {
            JPanel panel = new JPanel();
            contentPanel.add(panel, "cell 0 0 2 1,grow");
            panel.setLayout(new MigLayout("", "[50:n][grow][][]", "[][][][][grow][][]"));
            {
                btnAutoPeakSearch = new JButton("Auto Peak Search");
                btnAutoPeakSearch.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        do_btnAutoPeakSearch_actionPerformed(e);
                    }
                });
                panel.add(btnAutoPeakSearch, "cell 0 0 4 1,growx");
            }
            {
                {
                    lblTmin = new JLabel("t2min");
                    panel.add(lblTmin, "cell 2 1,alignx trailing");
                }
                {
                    txtTthmin = new JTextField();
                    txtTthmin.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyPressed(KeyEvent e) {
                            do_txtTthmin_keyPressed(e);
                        }
                    });
                    panel.add(txtTthmin, "cell 3 1,growx");
                    txtTthmin.setColumns(5);
                }
                {
                    lblTmax = new JLabel("t2max");
                    panel.add(lblTmax, "cell 2 2,alignx right,aligny top");
                }
                {
                    txtTthmax = new JTextField();
                    txtTthmax.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyPressed(KeyEvent e) {
                            do_txtTthmax_keyPressed(e);
                        }
                    });
                    panel.add(txtTthmax, "cell 3 2,growx,aligny top");
                    txtTthmax.setColumns(5);
                }
                {
                    chckbxUseBkgEstimation = new JCheckBox("use bkg estimation");
                    chckbxUseBkgEstimation.addItemListener(new ItemListener() {
                        public void itemStateChanged(ItemEvent e) {
                            do_chckbxUseBkgEstimation_itemStateChanged(e);
                        }
                    });
                    panel.add(chckbxUseBkgEstimation, "cell 2 3 2 1,alignx center");
                }

            }
            {
                btnAddPeaks = new JToggleButton("Add Peaks");
                btnAddPeaks.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        do_btnAddPeaks_itemStateChanged(e);
                    }
                });
                panel.add(btnAddPeaks, "cell 3 4,growx,aligny bottom");
            }
            {
                JLabel lblFactor = new JLabel("factor:");
                panel.add(lblFactor, "flowx,cell 0 5");
            }
            {
                lblFact = new JLabel("fact");
                panel.add(lblFact, "cell 1 5,alignx left");
            }
            btnSetFactminmax = new JButton("max/min");
            btnSetFactminmax.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    do_btnSetFactminmax_actionPerformed(arg0);
                }
            });
            btnRemovePeaks = new JToggleButton("Remove Peaks");
            btnRemovePeaks.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    do_btnRemovePeaks_itemStateChanged(e);
                }
            });
            panel.add(btnRemovePeaks, "cell 3 5,growx,aligny bottom");
            panel.add(btnSetFactminmax, "cell 0 6 2 1,growx,aligny top");
            JButton btnRemoveAll = new JButton("Remove All");
            btnRemoveAll.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    do_btnRemoveAll_actionPerformed(e);
                }
            });
            panel.add(btnRemoveAll, "cell 3 6,growx,aligny top");
            {
                slider = new DoubleJSlider(1.0, 5.0, 1.5, 100);
                slider.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        do_slider_stateChanged(e);
                    }
                });
                slider.setOrientation(SwingConstants.VERTICAL);
                slider.setPaintTicks(true);
                slider.setPaintLabels(true);
                panel.add(slider, "cell 0 1 2 4,grow");
            }
        }
        {
            JButton btnSavePeaks = new JButton("Save Peaks");
            btnSavePeaks.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    do_btnSavePeaks_actionPerformed(e);
                }
            });
            contentPanel.add(btnSavePeaks, "cell 0 1 2 1,growx");
        }
        {
        	btnIndex = new JButton("Index");
        	btnIndex.addActionListener(new ActionListener() {
        		public void actionPerformed(ActionEvent e) {
        			do_btnIndex_actionPerformed(e);
        		}
        	});
        	contentPanel.add(btnIndex, "cell 0 2 2 1,growx");
        }
        {
            JPanel buttonPane = new JPanel();
            findPeaksDialog.getContentPane().add(buttonPane, BorderLayout.SOUTH);
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
                findPeaksDialog.getRootPane().setDefaultButton(okButton);
            }

        }
        //omplo els textboxes
        if (isOneSerieSelected()) {
            DataSerie ds = getMostFavorableDS();
            try {
                txtTthmin.setText(FileUtils.dfX_3.format(ds.getMinX()));
                txtTthmax.setText(FileUtils.dfX_3.format(ds.getMaxX()));
            }catch(Exception ex) {
                log.debug("Error reading 2th min/max");
            }
        }
        
        if (D1Dplot_global.release)this.hideThingsDebug();
        
        this.autoPeakSearch();
    }
    
    private void hideThingsDebug() {
        btnIndex.setVisible(false);
    }

    //returns the dat of the selected plottable OR if not possible, the first selected serie (independentment del tipus) intentem primer el dat, sino altres
    private DataSerie getMostFavorableDS() {
        DataSerie dsactive = dades.getFirstSelectedDataSerie();
        if (dsactive.getSerieType()!=SerieType.dat) dsactive = dades.getSelectedSeriesByType(SerieType.dat).get(0);
        if (dsactive==null)dsactive = dades.getFirstSelectedDataSerie().getParent().getMainSerie();
        return dsactive;
    }
    
    private void autoPeakSearch(){
        if (!isOneSerieSelected())return;
        DataSerie ds = getMostFavorableDS();
        
        try{
            float fact = (float)slider.getValue()/sliderFactDiv;
            boolean usebkg = chckbxUseBkgEstimation.isSelected();
            double tthmin = ds.getMinX();
            double tthmax = ds.getMaxX();            
            try{
                tthmin=Double.parseDouble(txtTthmin.getText());
                tthmax=Double.parseDouble(txtTthmax.getText());
            }catch(Exception exrange){
                log.debug("error parsing tthmin tthmax");
            }
            
            DataSerie llindar = null;
            if (usebkg) {
                llindar = PattOps.findPeaksGetBkgLlindar(ds);
            }

            DataSerie pks = PattOps.findPeaksEvenBetter(ds,llindar,fact,tthmin,tthmax,plotpanel.isPlotwithbkg());
            pks.setName(ds.getName()+" (peaks)");
            
            dades.replaceDataSerie(ds.getParent(), pks, SerieType.peaks);
            
            if (llindar==null){
                //plotejo linia recta
                double llindar_value = ds.calcYmeanYDesvYmaxYmin(plotpanel.isPlotwithbkg())[1];
                llindar = new DataSerie(ds, SerieType.bkg,false);
                PattOps.addConstantYPointsToDataserie(llindar,50,ds.getMinX(),ds.getMaxX(),llindar_value);
            }
            
            llindar.setScaleY(fact);
            llindar.setSerieType(SerieType.bkg); //canvio l'estil
            plotpanel.setBkgseriePeakSearch(llindar);
            
            
        }catch(Exception ex){
            log.warning("Error during peak search, check parameters");
        }
        plotpanel.actualitzaPlot();
    }
    
    
    private boolean isOneSerieSelected(){
        return dades.isOneSerieSelected();
    }

	private void do_chckbxOnTop_itemStateChanged(ItemEvent e) {
		findPeaksDialog.setAlwaysOnTop(chckbxOnTop.isSelected());
	}

	private void do_btnAutoPeakSearch_actionPerformed(ActionEvent e) {
	    this.autoPeakSearch();
	}

	private void do_slider_stateChanged(ChangeEvent arg0) {
	    lblFact.setText(String.format("%.2f", slider.getScaledValue()));
        this.autoPeakSearch();
    }
    private void do_okButton_actionPerformed(ActionEvent e) {
        this.tanca();
    }
    
    private void do_btnAddPeaks_itemStateChanged(ItemEvent e) {
        if (!isOneSerieSelected())return;
        if (this.btnAddPeaks.isSelected()){
            this.btnAddPeaks.setText("Finish");
            plotpanel.setSelectingPeaks(true);
        }else{
            this.btnAddPeaks.setText("Add Peaks");
            plotpanel.setSelectingPeaks(false);
        }
    }

    private void do_btnRemovePeaks_itemStateChanged(ItemEvent e) {
        if (!isOneSerieSelected())return;
        if (this.btnRemovePeaks.isSelected()){
            this.btnRemovePeaks.setText("Finish");
            plotpanel.setDeletingPeaks(true);
        }else{
            this.btnRemovePeaks.setText("Remove Peaks");
            plotpanel.setDeletingPeaks(false);
        }
    }
    
    private void do_btnRemoveAll_actionPerformed(ActionEvent e) {
        if (!isOneSerieSelected())return;
        dades.getFirstSelectedDataSerie().getParent().removeDataSeriesByType(SerieType.peaks);
        dades.updateFullTable();
        plotpanel.actualitzaPlot();
    }
    private void do_btnSetFactminmax_actionPerformed(ActionEvent arg0) {
        String s = (String)JOptionPane.showInputDialog(
        		findPeaksDialog,
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
                log.warning("Error parsing min/max values");
            }
        }
    }
    private void do_btnSavePeaks_actionPerformed(ActionEvent e) {
        if (!isOneSerieSelected())return;
        File pksFile = FileUtils.fchooserSaveAsk(findPeaksDialog,new File(D1Dplot_global.getWorkdir()), null, null);
        if (pksFile == null){
            log.warning("No data file selected");
            return;
        }
        try {
            DataSerie pksDS = dades.getFirstSelectedDataSet().getFirstDataSerieByType(SerieType.peaks);
            pksDS.sortPoints();//ordenem per 2theta abans de salvar
            pksFile = DataFileUtils.writePeaksFile(pksFile, pksDS, true);
            log.info(pksFile.toString()+" written!");   
        } catch (Exception e1) {
            log.info("Selected Data do not contain peaks");
            return;
        }
    }
    
    public void tanca() {
	    plotpanel.setSelectingPeaks(false);
	    plotpanel.setShowPeakThreshold(false);
	    plotpanel.actualitzaPlot();
	    findPeaksDialog.dispose();
	}

	public void visible(boolean vis) {
    	this.findPeaksDialog.setVisible(vis);
    	if (vis) {
        	plotpanel.setShowPeakThreshold(true);
        	plotpanel.actualitzaPlot();
    	}
    }
	protected void do_btnIndex_actionPerformed(ActionEvent e) {
		//open dialog
	    if (id==null)id = new IndexDialog(D1Dplot_global.getD1DmainFrame(), this.plotpanel, this.dades);
		id.visible(true);
	}
    protected void do_chckbxUseBkgEstimation_itemStateChanged(ItemEvent e) {
        this.autoPeakSearch();
    }
    protected void do_txtTthmin_keyPressed(KeyEvent e) {
        if (e.getKeyCode()==KeyEvent.VK_ENTER) {
            autoPeakSearch();
        }
    }
    protected void do_txtTthmax_keyPressed(KeyEvent e) {
        if (e.getKeyCode()==KeyEvent.VK_ENTER) {
            autoPeakSearch();
        }
    }
    protected void do_findPeaksDialog_windowClosing(WindowEvent e) {
        this.tanca();
    }
}

package vava33.d1dplot;

/**    
 * D1Dplot
 * Program to plot 1D X-ray Powder Diffraction Patterns
 *  
 * It uses the following libraries from the same author:
 *  - com.vava33.jutils
 *  
 * And the following 3rd party libraries: 
 *  - net.miginfocom.swing.MigLayout
 *  - org.apache.commons.math3.util.FastMath
 * 
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.UIManager;

import com.vava33.jutils.FileUtils;
import com.vava33.jutils.VavaLogger;

import net.miginfocom.swing.MigLayout;

import javax.swing.JSplitPane;
import javax.swing.JPanel;
import javax.swing.JTable;

import com.vava33.jutils.LogJTextArea;

import javax.swing.JButton;
import javax.swing.filechooser.FileNameExtensionFilter;

import vava33.d1dplot.auxi.DataFileUtils;
import vava33.d1dplot.auxi.Pattern1D;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;

public class D1Dplot_main {

    private JFrame mainFrame;
    private static VavaLogger log;
    private JTable table_files;
    private LogJTextArea tAOut;
    private PlotPanel panel_plot;
    private JTextField txtXtitle;
    private JTextField txtYtitle;
    private JTextField txtHklTickSize;
    private JCheckBox chckbxShowLegend;
    private JComboBox comboXunits;
    private JSpinner spinnerLinewidth;
    private JComboBox comboLineType;
    private JComboBox comboMarkers;
    private JSpinner spinnerMarkerSize;
    private JCheckBox chckbxShowErrorBars;
    private JCheckBox chckbxIntensityWithBackground;
    private JTextField txtLegendx;
    private JTextField txtLegendy;
    
    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        
        //LOGGER
        D1Dplot_global.initLogger("d2dplot");
        log = D1Dplot_global.log;
                
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            if(UIManager.getLookAndFeel().toString().contains("metal")){
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");    
            }
            // UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); //java metal
            
        } catch (Throwable e) {
            e.printStackTrace();
        }

        StringBuilder path = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            path.append(args[i]).append(" ");
        }
        if (args.length > 0)
            D1Dplot_global.workdir = path.toString().trim();

        
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    D1Dplot_main window = new D1Dplot_main();
                    window.mainFrame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public D1Dplot_main() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        mainFrame = new JFrame();
        mainFrame.setTitle("D1Dplot");
        mainFrame.setBounds(100, 100, 1024, 768);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.getContentPane().setLayout(new MigLayout("", "[grow]", "[grow]"));
        
        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.85);
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        mainFrame.getContentPane().add(splitPane, "cell 0 0,grow");
        
        JSplitPane splitPane_1 = new JSplitPane();
        splitPane_1.setResizeWeight(0.85);
        splitPane.setLeftComponent(splitPane_1);
        
        JPanel panel_right = new JPanel();
        splitPane_1.setRightComponent(panel_right);
        panel_right.setLayout(new MigLayout("", "[grow]", "[][][grow]"));
        
        JButton btnOpen = new JButton("Open");
        btnOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_btnOpen_actionPerformed(e);
            }
        });
        panel_right.add(btnOpen, "cell 0 0");
        
        JButton btnReset = new JButton("Reset");
        btnReset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_btnReset_actionPerformed(e);
            }
        });
        panel_right.add(btnReset, "cell 0 1");
        
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(null, "Plot settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_right.add(panel, "cell 0 2,growx,aligny top");
        panel.setLayout(new MigLayout("", "[][grow][grow][grow]", "[][][][][][][][][][][][]"));
        
        JLabel lblXTitle = new JLabel("X title");
        panel.add(lblXTitle, "cell 0 0,alignx trailing");
        
        txtXtitle = new JTextField();
        txtXtitle.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtXtitle_actionPerformed(e);
            }
        });
        txtXtitle.setText("xtitle");
        panel.add(txtXtitle, "cell 1 0 3 1,growx");
        txtXtitle.setColumns(10);
        
        JLabel lblYTitle = new JLabel("Y title");
        panel.add(lblYTitle, "cell 0 1,alignx trailing");
        
        txtYtitle = new JTextField();
        txtYtitle.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtYtitle_actionPerformed(e);
            }
        });
        txtYtitle.setText("ytitle");
        panel.add(txtYtitle, "cell 1 1 3 1,growx");
        txtYtitle.setColumns(10);
        
        chckbxShowLegend = new JCheckBox("Show Legend");
        panel.add(chckbxShowLegend, "cell 0 2 2 1");
        
        txtLegendx = new JTextField();
        txtLegendx.setText("legendX");
        panel.add(txtLegendx, "cell 2 2,growx");
        txtLegendx.setColumns(10);
        
        txtLegendy = new JTextField();
        txtLegendy.setText("legendY");
        panel.add(txtLegendy, "cell 3 2,growx");
        txtLegendy.setColumns(10);
        
        JLabel lblXUnits = new JLabel("X units");
        panel.add(lblXUnits, "cell 0 3,alignx trailing");
        
        comboXunits = new JComboBox();
        comboXunits.setModel(new DefaultComboBoxModel(new String[] {"2theta", "Q", "1/dsp"}));
        panel.add(comboXunits, "cell 1 3 3 1,growx,aligny top");
        
        JSeparator separator = new JSeparator();
        panel.add(separator, "cell 0 4 2 1,growx");
        
        JLabel lblSelectedSerie = new JLabel("Selected Serie");
        panel.add(lblSelectedSerie, "cell 0 5 2 1");
        
        JLabel lblColor = new JLabel("Color");
        panel.add(lblColor, "cell 0 6,alignx trailing");
        
        JComboBox comboColor = new JComboBox();
        panel.add(comboColor, "cell 1 6,growx");
        
        JButton btnOther = new JButton("other");
        panel.add(btnOther, "cell 2 6");
        
        JLabel lblLinewidth = new JLabel("Linewidth");
        panel.add(lblLinewidth, "cell 0 7");
        
        spinnerLinewidth = new JSpinner();
        panel.add(spinnerLinewidth, "cell 1 7");
        
        comboLineType = new JComboBox();
        panel.add(comboLineType, "cell 2 7 2 1,growx");
        
        JLabel lblMarkers = new JLabel("Markers");
        panel.add(lblMarkers, "cell 0 8,alignx trailing");
        
        comboMarkers = new JComboBox();
        panel.add(comboMarkers, "cell 1 8,growx");
        
        JLabel lblSize = new JLabel("size");
        panel.add(lblSize, "cell 2 8,alignx right");
        
        spinnerMarkerSize = new JSpinner();
        panel.add(spinnerMarkerSize, "cell 3 8");
        
        chckbxShowErrorBars = new JCheckBox("Show error bars");
        panel.add(chckbxShowErrorBars, "cell 0 9 4 1");
        
        JLabel lblHklTickSize = new JLabel("HKL tick size (PRF)");
        panel.add(lblHklTickSize, "cell 0 10 2 1");
        
        txtHklTickSize = new JTextField();
        txtHklTickSize.setText("hkl tick size");
        panel.add(txtHklTickSize, "cell 2 10 2 1,growx");
        txtHklTickSize.setColumns(10);
        
        chckbxIntensityWithBackground = new JCheckBox("Intensity with Background (PRF)");
        panel.add(chckbxIntensityWithBackground, "cell 0 11 4 1");
        
        panel_plot = new PlotPanel();
//        panel_plot = (PlotPanel) new JPanel();
        panel_plot.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        splitPane_1.setLeftComponent(panel_plot);
        
        JSplitPane splitPane_2 = new JSplitPane();
        splitPane_2.setResizeWeight(0.5);
        splitPane.setRightComponent(splitPane_2);
        
        tAOut = new LogJTextArea();
        splitPane_2.setRightComponent(tAOut);
        
        table_files = new JTable();
        splitPane_2.setLeftComponent(table_files);
        
        
        
        
        inicia();
    }
    
    private void inicia(){
        this.txtXtitle.setText(panel_plot.getXlabel());
        this.txtYtitle.setText(panel_plot.getYlabel());
    }
    
    protected void do_btnOpen_actionPerformed(ActionEvent e) {
        openDataFile();
        panel_plot.fitGraph();
    }
    protected void do_btnReset_actionPerformed(ActionEvent e) {
        panel_plot.fitGraph();
    }
    
    private String getWorkdir(){
        return D1Dplot_global.workdir;
    }
    
    public void reset(){
        //TODO
    }
    
    private void openDataFile(){
        log.debug("openDataFile entered");
        FileNameExtensionFilter filt[] = DataFileUtils.getExtensionFilterRead();
        File datFile = FileUtils.fchooser(mainFrame,new File(getWorkdir()), filt, filt.length-1, false, false);
        if (datFile == null){
            this.tAOut.stat("No data file selected");
            return;
        }
        
        // resetejem
        this.reset();
        Pattern1D patt = DataFileUtils.readDAT(datFile);
        patt.AddDefaultDataSerie();
        log.writeNameNums("CONFIG", true, "num points pattern", patt.getPoints().size());
        log.writeNameNums("CONFIG", true, "first points", patt.getPoints().get(0).getX(), patt.getPoints().get(0).getY());
        log.writeNameNums("CONFIG", true, "first points", patt.getPoints().get(1).getX(), patt.getPoints().get(1).getY());
        log.writeNameNums("CONFIG", true, "first points", patt.getPoints().get(2).getX(), patt.getPoints().get(2).getY());
        log.writeNameNums("CONFIG", true, "first points", patt.getPoints().get(20).getX(), patt.getPoints().get(20).getY());
        panel_plot.getPatterns().add(patt);
        this.updateData();
        log.debug("openDataFile exited");
    }
    
    private void updateData(){
        log.debug("updateData entered");
        //TODO
        //update table
        //update pattern list somewhere
        //update plotpanel series
        //replot
        panel_plot.repaint();
        mainFrame.repaint();
        log.debug("updateData entered");

    }
    protected void do_txtXtitle_actionPerformed(ActionEvent e) {
        if (this.txtXtitle.getText()!=null){
            this.panel_plot.setXlabel(this.txtXtitle.getText());    
        }
    }
    protected void do_txtYtitle_actionPerformed(ActionEvent e) {
        if (this.txtYtitle.getText()!=null){
            this.panel_plot.setYlabel(this.txtYtitle.getText());    
        }
    }
}

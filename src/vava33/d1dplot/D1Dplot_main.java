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
import java.awt.Frame;

import javax.swing.JFrame;
import javax.swing.UIManager;

import com.vava33.jutils.FileUtils;
import com.vava33.jutils.VavaLogger;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

import net.miginfocom.swing.MigLayout;

import javax.swing.JSplitPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTable;

import com.vava33.jutils.LogJTextArea;

import javax.swing.JButton;
import javax.swing.filechooser.FileNameExtensionFilter;

import vava33.d1dplot.auxi.DataFileUtils;
import vava33.d1dplot.auxi.Pattern1D;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;

public class D1Dplot_main {

    private JFrame mainFrame;
    private static VavaLogger log;
    private JTable table_files;
    private LogJTextArea tAOut;
    private PlotPanel panel_plot;
    
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
        panel_right.setLayout(new MigLayout("", "[]", "[]"));
        
        JButton btnOpen = new JButton("Open");
        btnOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_btnOpen_actionPerformed(e);
            }
        });
        panel_right.add(btnOpen, "cell 0 0");
        
        panel_plot = new PlotPanel();
        splitPane_1.setLeftComponent(panel_plot);
        
        JSplitPane splitPane_2 = new JSplitPane();
        splitPane_2.setResizeWeight(0.5);
        splitPane.setRightComponent(splitPane_2);
        
        tAOut = new LogJTextArea();
        splitPane_2.setRightComponent(tAOut);
        
        table_files = new JTable();
        splitPane_2.setLeftComponent(table_files);
    }
    protected void do_btnOpen_actionPerformed(ActionEvent e) {
        openDataFile();
        panel_plot.resetView();
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
        log.debug("openDataFile entered");
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
}

package com.vava33.d1dplot;

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
 *  - org.apache.batik
 *  - org.w3c.dom
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 *  
 */

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.miginfocom.swing.MigLayout;

import com.vava33.BasicPlotPanel.core.Plottable_point;
import com.vava33.BasicPlotPanel.core.SerieType;
import com.vava33.cellsymm.CellSymm_global;
import com.vava33.d1dplot.auxi.ArgumentLauncher;
import com.vava33.d1dplot.auxi.DataFileUtils;
import com.vava33.d1dplot.auxi.PattOps;
import com.vava33.d1dplot.auxi.DataFileUtils.SupportedReadExtensions;
import com.vava33.d1dplot.auxi.DataFileUtils.SupportedWriteExtensions;
import com.vava33.d1dplot.data.DataPoint;
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.d1dplot.data.DataSet;
import com.vava33.d1dplot.data.Xunits;
import com.vava33.jutils.FileUtils;
import com.vava33.jutils.LogJTextArea;
import com.vava33.jutils.VavaLogger;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class D1Dplot_main {

    private static final String className = "d1Dplot_main";
    private static VavaLogger log;

    private float tAoutFsize = 12.0f;
    private static int def_Width=1024;
    private static int def_Height=768;
    private static String LandF = "system";

    private boolean firstTime = true;
    private BackgroundDialog bkgDiag;
    private DajustDialog dajustDiag;
    private FindPeaksDialog FindPksDiag;
    private SubtractDialog subDiag;
    private WaveCalDialog waveCalDiag;
    private Database DBDiag;
    private Plot2DPanel p2;
    private AboutDialog aboutDiag;
    private FitPeakDialog fitPksDiag;
    private JTable table_files;
    private JFrame mainFrame;
    private LogJTextArea tAOut;

    private XRDPlotPanelFrontEnd plotPanel;
    private D1Dplot_data XRDdata;

    private JTabbedPane tabbedPanel_bottom;
    private JMenuBar menuBar;
    private JMenu mnFile;
    private JMenuItem mntmOpen;
    private JSplitPane splitPane;
    private JScrollPane scrollPane_2;
    private JPanel panel_DS;
    private JButton buttonAdd;
    private JButton buttonRemove;
    private JButton buttonDown;
    private JButton buttonUp;
    private JMenuItem mntmCloseAll;
    private JSeparator separator_2;
    private JMenuItem mntmQuit;
    private JMenuItem mntmSaveAs;
    private JMenu mnPlot;
    private JMenuItem mntmExportAsPng;
    private JMenuItem mntmExportAsSvg;
    private JMenu mnOps;
    private JMenuItem mntmFindPeaks_1;
    private JMenuItem mntmCalcBackground;
    private JMenuItem mntmSubtractPatterns;
    private JButton btnReload;
    private JMenuItem mntm2Dplot;
    private JMenuItem mntmSequentialyOffset;
    private JMenu mnHelp;
    private JMenuItem mntmConvertToWavelength;
    private JMenuItem mntmChangeXUnits;
    private JMenuItem mntmSumSelected;
    private JMenuItem mntmAbout;
    private JMenuItem mntmRebinning;
    private JMenuItem mntmUsersGuide;
    private JMenuItem mntmCheckForUpdates;
    private JMenuItem mntmDB;
    private JMenuItem mntmSaveProject;
    private JMenuItem mntmOpenProject;
    private JButton btnDupli;
    private JMenuItem mntmSaveProfile;
    private JMenuItem mntmFitPeaks;
    private JPanel panel_1;
    private JMenuItem mntmDajust;
    private JMenu mnNewMenu;
    private JMenuItem mntmInvertOrder;
    private JMenuItem mntmCalibwave;

    /**
     * Launch the application.
     */
    public static void main(final String[] args) {

        //first thing to do is read PAR files if exist
        FileUtils.getOS();
        FileUtils.setLocale(null);
        D1Dplot_global.readParFile();

        //LOGGER
        log = D1Dplot_global.getVavaLogger(className);
        System.out.println(log.logStatus());
        CellSymm_global.setLogLevel(log.getLogLevelString());
        CellSymm_global.setLogging(D1Dplot_global.isLoggingConsole(), D1Dplot_global.isLoggingFile(), D1Dplot_global.isLoggingTA());

        try {
            if (FileUtils.containsIgnoreCase(getLandF(), "system")){
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            if (FileUtils.containsIgnoreCase(getLandF(), "gtk")){
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            }
            if (FileUtils.containsIgnoreCase(getLandF(), "metal")){
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            }
        } catch (Throwable e) {
            log.warning("Error initializing System look and feel");
        }


        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    D1Dplot_main frame = new D1Dplot_main();
                    D1Dplot_global.setMainFrame(frame); //this is NEW març 2019
                    D1Dplot_global.printAllOptions("config",frame.plotPanel.createOptionsObject());
                    ArgumentLauncher.readArguments(frame, args);
                    if (ArgumentLauncher.isLaunchGraphics()){
                        frame.showMainFrame();
                    }else{
                        log.info("Exiting...");
                        frame.mainFrame.dispose();
//                        return;
                        System.exit(0);
                    }
                } catch (Exception e) {
                    log.severe("Error initializing main window");
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public D1Dplot_main() {
        //1 dades
        XRDdata = new D1Dplot_data();
        //2 plot1Dpanel TODO CAL COMENTAR AQUESTA LINIA PER PODER OBRIR WINDOWBUILDER
        XRDPlot1DPanel pp = new XRDPlot1DPanel(XRDdata,D1Dplot_global.getVavaLogger(XRDPlot1DPanel.getClassName()));
        //and 3rd frontend
        plotPanel = new XRDPlotPanelFrontEnd(D1Dplot_global.getReadedOpt(),pp,D1Dplot_global.getVavaLogger(XRDPlotPanelFrontEnd.getClassName()));
        initGUI();
        fitOnScreen();
        inicia();
        if (D1Dplot_global.release)hideThingsDebug();
    }
    
    private void fitOnScreen(){   
    	//COMPROVEM QUE HI CAP A LA PANTALLA EN LA MIDA SELECIONADA i SINO LA REDUIM PER FER-HO CABRE
    	
        int[] screenWH = D1Dplot_global.getDisplayMonitorDimensions(D1Dplot_global.getDisplayMonitor());
    	
    	mainFrame.setSize(D1Dplot_main.getDef_Width(), D1Dplot_main.getDef_Height());
//    	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    	
    	while(mainFrame.getWidth()>screenWH[0]){
    		mainFrame.setSize(mainFrame.getWidth()-100, mainFrame.getHeight());
    	}
    	while(mainFrame.getHeight()>screenWH[1]){
    		mainFrame.setSize(mainFrame.getWidth(), mainFrame.getHeight()-100);
    	}
    	
    	//ARA LA POSEM AL MONITOR QUE TOCA I LA CENTREM (ho fa la mateixa subrutina)
        D1Dplot_global.showOnScreen(D1Dplot_global.getDisplayMonitor(), mainFrame,true);

    }
    private void hideThingsDebug() {
        mntmDajust.setVisible(false);
//        mntmCalibwave.setVisible(false);
    }
    
    public void showMainFrame(){
        mainFrame.setVisible(true);
        this.plotPanel.showHideButtonsPanel();//amagara el menu lateral
    }
    public void disposeMainFrame(){
        mainFrame.dispose();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initGUI() {
        mainFrame = new JFrame();
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                do_mainFrame_windowClosing(e);
            }
        });
        mainFrame.setTitle("D1Dplot");
        mainFrame.setIconImage(D1Dplot_global.getIcon());
//        mainFrame.setBounds(100, 100, 1024, 768);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.getContentPane().setLayout(new MigLayout("", "[grow]", "[grow]"));

        splitPane = new JSplitPane();
        splitPane.setResizeWeight(1.0);
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        mainFrame.getContentPane().add(splitPane, "cell 0 0,grow");

        tabbedPanel_bottom = new JTabbedPane(JTabbedPane.RIGHT);
        tabbedPanel_bottom.setBorder(null);
        splitPane.setRightComponent(tabbedPanel_bottom);

        JComboBox<String> comboXunitsTable = new JComboBox<String>();
        for (Xunits a :Xunits.values()){
            comboXunitsTable.addItem(a.getName());
        }

        JComboBox<String> comboStypeTable = new JComboBox<String>();
        for (SerieType s :SerieType.values()){
            comboStypeTable.addItem(s.name());
        }

        panel_DS = new JPanel();
        tabbedPanel_bottom.addTab("Data", null, panel_DS, null);
        panel_DS.setLayout(new MigLayout("insets 0", "[grow][]", "[grow]"));

        //Creem la taula
        table_files = XRDdata.getTablePatterns();
//      //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table_files);
        panel_DS.add(scrollPane, "cell 0 0,grow");
        
        panel_1 = new JPanel();
        panel_DS.add(panel_1, "cell 1 0,grow");
        panel_1.setLayout(new MigLayout("insets 2", "[][]", "[][][]"));
                        
                                buttonUp = new JButton("^");
                                panel_1.add(buttonUp, "cell 0 0,growx,aligny top");
                                buttonUp.addActionListener(new ActionListener() {
                                    public void actionPerformed(ActionEvent e) {
                                        do_buttonUp_actionPerformed(e);
                                    }
                                });
                
                        buttonAdd = new JButton("+");
                        panel_1.add(buttonAdd, "cell 1 0,growx,aligny top");
                        buttonAdd.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                do_buttonAdd_actionPerformed(e);
                            }
                        });
        
                buttonDown = new JButton("v");
                panel_1.add(buttonDown, "cell 0 1,growx,aligny top");
                
                        buttonRemove = new JButton("-");
                        panel_1.add(buttonRemove, "cell 1 1,growx,aligny top");
                        buttonRemove.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                do_buttonRemove_actionPerformed(e);
                            }
                        });
                        buttonRemove.setPreferredSize(new Dimension(23, 28));
                        
                                btnReload = new JButton("R");
                                panel_1.add(btnReload, "cell 0 2,growx");
                                btnReload.addActionListener(new ActionListener() {
                                    public void actionPerformed(ActionEvent e) {
                                        do_btnReload_actionPerformed(e);
                                    }
                                });
                                btnReload.setToolTipText("Reload selected pattern");
                                
                                        btnDupli = new JButton("D");
                                        panel_1.add(btnDupli, "cell 1 2");
                                        btnDupli.setToolTipText("Duplicate selected pattern");
                                        btnDupli.addActionListener(new ActionListener() {
                                            public void actionPerformed(ActionEvent e) {
                                                do_buttonDupli_actionPerformed(e);
                                            }
                                        });
                buttonDown.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        do_buttonDown_actionPerformed(e);
                    }
                });
        
        
        scrollPane_2 = new JScrollPane();
        tabbedPanel_bottom.addTab("Log", null, scrollPane_2, null);
        tAOut = new LogJTextArea();
        scrollPane_2.setViewportView(tAOut);

        splitPane.setLeftComponent(plotPanel);

        menuBar = new JMenuBar();
        mainFrame.setJMenuBar(menuBar);

        mnFile = new JMenu("File");
        mnFile.setMnemonic('f');
        menuBar.add(mnFile);

        mntmOpen = new JMenuItem("Open Data File");
        mntmOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        mntmOpen.setMnemonic('o');
        mntmOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmOpen_actionPerformed(e);
            }
        });
        mnFile.add(mntmOpen);

        mntmSaveAs = new JMenuItem("Save Data as");
        mntmSaveAs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                do_mntmSaveAs_actionPerformed(arg0);
            }
        });
        mnFile.add(mntmSaveAs);

        mntmSaveProfile = new JMenuItem("Save obs/cal/hkl matching");
        mntmSaveProfile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmSaveProfile_actionPerformed(e);
            }
        });
        mnFile.add(mntmSaveProfile);

        mntmExportAsPng = new JMenuItem("Export as PNG");
        mnFile.add(mntmExportAsPng);
        mntmExportAsPng.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmExportAsPng_actionPerformed(e);
            }
        });

        mntmExportAsSvg = new JMenuItem("Export as SVG");
        mnFile.add(mntmExportAsSvg);
        mntmExportAsSvg.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmExportAsSvg_actionPerformed(e);
            }
        });

        mntmSaveProject = new JMenuItem("Save project");
        mntmSaveProject.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmSaveProject_actionPerformed(e);
            }
        });
        mnFile.add(mntmSaveProject);

        mntmOpenProject = new JMenuItem("Open project");
        mntmOpenProject.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmOpenProject_actionPerformed(e);
            }
        });
        mnFile.add(mntmOpenProject);

        mntmCloseAll = new JMenuItem("Close All");
        mntmCloseAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmCloseAll_actionPerformed(e);
            }
        });
        mnFile.add(mntmCloseAll);

        separator_2 = new JSeparator();
        mnFile.add(separator_2);

        mntmQuit = new JMenuItem("Quit");
        mntmQuit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmQuit_actionPerformed(e);
            }
        });
        mnFile.add(mntmQuit);

        mnPlot = new JMenu("Plot Options");
        menuBar.add(mnPlot);

        mntm2Dplot = new JMenuItem("2D plot");
        mnPlot.add(mntm2Dplot);

        mntmSequentialyOffset = new JMenuItem("Sequential Y offset");
        mntmSequentialyOffset.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                do_mntmSequentialyOffset_actionPerformed(arg0);
            }
        });
        mnPlot.add(mntmSequentialyOffset);
        
        mntmInvertOrder = new JMenuItem("Invert pattern order");
        mntmInvertOrder.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmInvertOrder_actionPerformed(e);
            }
        });
        mnPlot.add(mntmInvertOrder);
        mntm2Dplot.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntm2Dplot_actionPerformed(e);
            }
        });

        mnOps = new JMenu("Processing");
        menuBar.add(mnOps);

        mntmFindPeaks_1 = new JMenuItem("Find Peaks");
        mntmFindPeaks_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmFindPeaks_1_actionPerformed(e);
            }
        });
        mnOps.add(mntmFindPeaks_1);

        mntmCalcBackground = new JMenuItem("Calc Background");
        mntmCalcBackground.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmCalcBackground_actionPerformed(e);
            }
        });
        mnOps.add(mntmCalcBackground);

        mntmSubtractPatterns = new JMenuItem("Subtract patterns");
        mntmSubtractPatterns.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmSubtractPatterns_actionPerformed(e);
            }
        });
        mnOps.add(mntmSubtractPatterns);

        mntmConvertToWavelength = new JMenuItem("Convert to wavelength");
        mntmConvertToWavelength.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmConvertToWavelength_actionPerformed(e);
            }
        });

        mntmRebinning = new JMenuItem("Rebinning");
        mntmRebinning.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                do_mntmRebinning_actionPerformed(arg0);
            }
        });
        mnOps.add(mntmRebinning);
        mnOps.add(mntmConvertToWavelength);

        mntmChangeXUnits = new JMenuItem("Change X units");
        mntmChangeXUnits.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmChangeXUnits_actionPerformed(e);
            }
        });
        mnOps.add(mntmChangeXUnits);

        mntmSumSelected = new JMenuItem("Sum selected patterns");
        mntmSumSelected.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmSumSelected_actionPerformed(e);
            }
        });
        mnOps.add(mntmSumSelected);
        
        mntmFitPeaks = new JMenuItem("Fit Peaks");
        mntmFitPeaks.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmFitPeaks_actionPerformed(e);
            }
        });
        mnOps.add(mntmFitPeaks);
        
        mnNewMenu = new JMenu("Tools");
        menuBar.add(mnNewMenu);
        
                mntmDB = new JMenuItem("Compound Database");
                mnNewMenu.add(mntmDB);
                
                mntmDajust = new JMenuItem("Dajust");
                mnNewMenu.add(mntmDajust);
                
                mntmCalibwave = new JMenuItem("Wavelength Refinement");
                mntmCalibwave.addActionListener(new ActionListener() {
                	public void actionPerformed(ActionEvent arg0) {
                		do_mntmCalibwave_actionPerformed(arg0);
                	}
                });
                mnNewMenu.add(mntmCalibwave);
                mntmDajust.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        do_mntmDajust_actionPerformed(e);
                    }
                });
                mntmDB.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        do_mntmDB_actionPerformed(e);
                    }
                });

        mnHelp = new JMenu("Help");
        menuBar.add(mnHelp);

        mntmAbout = new JMenuItem("About");
        mntmAbout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmAbout_actionPerformed(e);
            }
        });
        mnHelp.add(mntmAbout);

        mntmUsersGuide = new JMenuItem("User's guide");
        mntmUsersGuide.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmUsersGuide_actionPerformed(e);
            }
        });
        mnHelp.add(mntmUsersGuide);

        mntmCheckForUpdates = new JMenuItem("Check for updates");
        mntmCheckForUpdates.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmCheckForUpdates_actionPerformed(e);
            }
        });
        mnHelp.add(mntmCheckForUpdates);
    }

    //=========================================================

    
    private void inicia(){

        if (D1Dplot_global.isLoggingTA())VavaLogger.setTArea(tAOut);

        //split and divider loc
        tabbedPanel_bottom.setMinimumSize(new Dimension(0, 5));
        tabbedPanel_bottom.setPreferredSize(new Dimension(900, 150));
//        plotPanel.setPreferredSize(new Dimension(900,100));
        splitPane.resetToPreferredSizes();

        this.tAOut.setMidaLletra(tAoutFsize);
        log.info(D1Dplot_global.welcomeMSG+D1Dplot_global.pubMSG);
        if(D1Dplot_global.getConfigFileReaded()==null){
            log.info(String.format("No config file found on: %s, it will be created on exit!",D1Dplot_global.configFilePath));
        }else{
            if(D1Dplot_global.getConfigFileReaded()==true){
                log.info(String.format("Config file readed: %s",D1Dplot_global.configFilePath));    
            }else{
                log.warning(String.format("Error reading config file: %s",D1Dplot_global.configFilePath));
            }
        }


        int iconwidth = 18;
        Image MU = new ImageIcon(D1Dplot_main.class.getResource("/com/vava33/d1dplot/img/fletxa_amunt.png")).getImage().getScaledInstance(-100, iconwidth, java.awt.Image.SCALE_SMOOTH);
        buttonUp.setText("");
        buttonUp.setIcon(new ImageIcon(MU));
        Image MD = new ImageIcon(D1Dplot_main.class.getResource("/com/vava33/d1dplot/img/fletxa_avall.png")).getImage().getScaledInstance(-100, iconwidth, java.awt.Image.SCALE_SMOOTH);
        buttonDown.setText("");
        buttonDown.setIcon(new ImageIcon(MD));
        Image ADD = new ImageIcon(D1Dplot_main.class.getResource("/com/vava33/d1dplot/img/afegir.png")).getImage().getScaledInstance(-100, iconwidth, java.awt.Image.SCALE_SMOOTH);
        buttonAdd.setText("");
        buttonAdd.setIcon(new ImageIcon(ADD));
        Image REM = new ImageIcon(D1Dplot_main.class.getResource("/com/vava33/d1dplot/img/borrar.png")).getImage().getScaledInstance(-100, iconwidth, java.awt.Image.SCALE_SMOOTH);
        buttonRemove.setText("");
        buttonRemove.setIcon(new ImageIcon(REM));
        Image REL = new ImageIcon(D1Dplot_main.class.getResource("/com/vava33/d1dplot/img/reload.png")).getImage().getScaledInstance(-100, iconwidth, java.awt.Image.SCALE_SMOOTH);
        btnReload.setText("");
        btnReload.setIcon(new ImageIcon(REL));
        Image DUP = new ImageIcon(D1Dplot_main.class.getResource("/com/vava33/d1dplot/img/duplicate.png")).getImage().getScaledInstance(-100, iconwidth, java.awt.Image.SCALE_SMOOTH);
        btnDupli.setText("");
        btnDupli.setIcon(new ImageIcon(DUP));	        

        //botons taula
        buttonAdd.setPreferredSize(new Dimension(40,28));
        buttonRemove.setPreferredSize(new Dimension(40,28));
        buttonUp.setPreferredSize(new Dimension(40,28));
        buttonDown.setPreferredSize(new Dimension(40,28));
        btnReload.setPreferredSize(new Dimension(40,28));
        btnDupli.setPreferredSize(new Dimension(40,28));
        buttonAdd.setMaximumSize(new Dimension(45,28));
        buttonRemove.setMaximumSize(new Dimension(45,28));
        buttonUp.setMaximumSize(new Dimension(45,28));
        buttonDown.setMaximumSize(new Dimension(45,28));
        btnReload.setMaximumSize(new Dimension(45,28));
        btnDupli.setMaximumSize(new Dimension(45,28));
        buttonAdd.setMinimumSize(new Dimension(40,28));
        buttonRemove.setMinimumSize(new Dimension(40,28));
        buttonUp.setMinimumSize(new Dimension(40,28));
        buttonDown.setMinimumSize(new Dimension(40,28));
        btnReload.setMinimumSize(new Dimension(40,28));
        btnDupli.setMinimumSize(new Dimension(40,28));
        buttonAdd.setMargin(new Insets(2, 2, 2, 2));
        buttonRemove.setMargin(new Insets(2, 2, 2, 2));
        buttonUp.setMargin(new Insets(2, 2, 2, 2));
        buttonDown.setMargin(new Insets(2, 2, 2, 2));
        btnReload.setMargin(new Insets(2, 2, 2, 2));
        btnDupli.setMargin(new Insets(2, 2, 2, 2));

        //posem icones al tabbedPane
        Image DAT = new ImageIcon(D1Dplot_main.class.getResource("/com/vava33/d1dplot/img/dataseries.png")).getImage().getScaledInstance(-100, 30, java.awt.Image.SCALE_SMOOTH);
        Image LOG = new ImageIcon(D1Dplot_main.class.getResource("/com/vava33/d1dplot/img/logwin.png")).getImage().getScaledInstance(-100, 30, java.awt.Image.SCALE_SMOOTH);
        tabbedPanel_bottom.setTitleAt(0, "");
        tabbedPanel_bottom.setIconAt(0, new ImageIcon(DAT));
        tabbedPanel_bottom.setTitleAt(1, "");
        tabbedPanel_bottom.setIconAt(1, new ImageIcon(LOG));
        
        //seleccionem el log panel as default
        tabbedPanel_bottom.setSelectedIndex(1);
    }

    private void openDataFile(){
        FileNameExtensionFilter filt[] = DataFileUtils.getExtensionFilterRead();
        File[] datFile = FileUtils.fchooserMultiple(mainFrame, new File(getWorkdir()), filt,  filt.length-1, "Select PDD files to open");
        if (datFile == null){
            log.warning("No data file selected");
            return;
        }
        D1Dplot_global.setWorkdir(datFile[0]);
        for (int i=0; i<datFile.length;i++){
        	if (datFile[i].exists()) {
        		readDataFile(datFile[i]);	
        	}
        }
        //per panell log
        if(firstTime) {
            tabbedPanel_bottom.setSelectedIndex(0);
            firstTime=false;
        }
    }

    public void showTableTab() {
        tabbedPanel_bottom.setSelectedIndex(0);
    }

    //creem aquest per l'argument launcher
    public DataSet readDataFile(File datfile){
        DataSet s = DataFileUtils.readPatternFile(datfile); 
        XRDdata.addDataSet(s, true, true);
        XRDdata.selectLastAdded();
//        D1Dplot_global.setWorkdir(datfile);
        return s;
    }
    
    public DataSet readDataFile(File datfile, SupportedReadExtensions fmt){
        DataSet s = DataFileUtils.readPatternFile(datfile,fmt); 
        XRDdata.addDataSet(s, true, true);
        XRDdata.selectLastAdded();
//        D1Dplot_global.setWorkdir(datfile);
        return s;
    }

    private void closeDataFile(){
        if (table_files.getSelectedRow()<0){
            log.warning("Select which pattern(s) to close by selecting the corresponding row(s)");
            return;
        }
        if (table_files.getRowCount()<=0)return;
        XRDdata.removeSelectedSeries();
    }

    private void saveDataProf() {
        if (!XRDdata.arePlottables())return;

        //PRIMER INTENTEM FER-HO AUTOMATIC SI TOT ÉS CORRECTE
        List<DataSerie> dsOBS = XRDdata.getSelectedSeriesByType(SerieType.dat);
        if (dsOBS.isEmpty()) dsOBS = XRDdata.getSelectedSeriesByType(SerieType.obs);
        List<DataSerie> dsCAL = XRDdata.getSelectedSeriesByType(SerieType.cal);
        List<DataSerie> dsHKL = XRDdata.getSelectedSeriesByType(SerieType.hkl);

        int nobs = dsOBS.size();
        int ncal = dsCAL.size();
        int nhkl = dsHKL.size();

        boolean saveIt = false;

        if ((nobs==1)&&(ncal==1)&&(nhkl>0)) {
            //tot correcte, autosave
            saveIt = true;
        }else {
            //provarem de mirar amb tot el que està obert i mostrat
            dsOBS.clear();
            dsCAL.clear();
            dsHKL.clear();
            for(int i=0;i<XRDdata.getNPlottables();i++) {
                dsOBS.addAll(XRDdata.getAllSeriesByType(SerieType.dat));
                dsOBS.addAll(XRDdata.getAllSeriesByType(SerieType.obs));
                dsCAL.addAll(XRDdata.getAllSeriesByType(SerieType.cal));
                dsHKL.addAll(XRDdata.getAllSeriesByType(SerieType.hkl));
            }
            nobs = dsOBS.size();
            ncal = dsCAL.size();
            nhkl = dsHKL.size();
            if ((nobs==1)&&(ncal==1)&&(nhkl>0)) {
                //tot correcte, autosave
                saveIt=true;
            }else {
                dsOBS.clear();
                dsCAL.clear();
                dsHKL.clear();
                //FINALMENT com a ultima opcio obrirem un dialeg on s'hauran de seleccionar les dataseries	    
                SavePRFdialog prfdiag = new SavePRFdialog(XRDdata);
                prfdiag.visible(true);
                if (prfdiag.getOBS()!=null)dsOBS.add(prfdiag.getOBS());
                if (prfdiag.getCALC()!=null)dsCAL.add(prfdiag.getCALC());
                if (prfdiag.getHKLs()!=null)dsHKL.addAll(prfdiag.getHKLs());
                nobs = dsOBS.size();
                ncal = dsCAL.size();
                nhkl = dsHKL.size();
                if ((nobs==1)&&(ncal==1)&&(nhkl>0)) saveIt=true;
            }
        }
        if (saveIt) {
            FileNameExtensionFilter[] filt = new FileNameExtensionFilter[1];
            filt[0] = new FileNameExtensionFilter("d1Dplot profile","d1p");
            File f = FileUtils.fchooserSaveAsk(this.mainFrame, D1Dplot_global.getWorkdirFile(), filt, "d1p", "Save obs/calc/hkl matching");
            if (f!=null) {
                DataFileUtils.writeProfileFile(f, dsOBS.get(0),dsCAL.get(0),dsHKL, true);
                D1Dplot_global.setWorkdir(f);
            }
        }else {
            log.info("error saving obs/calc/hkl matching");
        }
    }

    private void saveDataFile(){
        if (!XRDdata.arePlottables())return;

        List<DataSerie> dss = XRDdata.getSelectedPlottables();

        if (dss.isEmpty()) {
            log.warning("Select on the table the pattern(s) you want to save");
            return;
        }

        FileNameExtensionFilter filt[] = DataFileUtils.getExtensionFilterWrite();

        if (dss.size()==1) {
            //normal save
            File datFile = FileUtils.fchooserSaveAsk(mainFrame,new File(getWorkdir()), filt, null);
            if (datFile == null){
                log.warning("No data file selected");
                return;
            }

            datFile = DataFileUtils.writePatternFile(datFile, dss.get(0), true, plotPanel.getGraphPanel().isPlotwithbkg());
            D1Dplot_global.setWorkdir(datFile);
            log.info(datFile.toString()+" written!");
            return;
        }

        //191108 - Preguntar batch save individual files or multipleDatFile
        JRadioButton rdbtnBatch = new JRadioButton("batch");
        rdbtnBatch.setSelected(true);
        ButtonGroup btnGroup = new ButtonGroup();
        btnGroup.add(rdbtnBatch);
        JRadioButton rdbtnAll = new JRadioButton("all_in_one");
        btnGroup.add(rdbtnAll);

        final String message0 = "Batch save individual files or a single multiple data file?";
        final Object[] params0 = { message0, rdbtnBatch, rdbtnAll};
        final int n0 = JOptionPane.showConfirmDialog(null, params0, "Save multiple patterns",
                JOptionPane.OK_CANCEL_OPTION);
        if (n0==JOptionPane.CANCEL_OPTION) {
            log.info("Save cancelled");
            return;
        }
        boolean batch = true;
        if (rdbtnAll.isSelected()) batch = false;
        
        if (batch) {
            boolean applyToAll=false;
            boolean owrite = false;
            //es pot millorar el naming (keep original, o donar un nou i numerar batch, etc...)
            if (dss.size()>1) {
                //batch save
                SupportedWriteExtensions[] possibilities = SupportedWriteExtensions.values();
                SupportedWriteExtensions format = (SupportedWriteExtensions) JOptionPane
                        .showInputDialog(null, "Output format:", "(Batch) Save Files",
                                JOptionPane.PLAIN_MESSAGE, null, possibilities,
                                possibilities[0]);
                if (format == null) {
                    log.warning("No format selected");
                    return;
                }

                //output folder
                File dir = FileUtils.fchooserOpenDir(this.getMainFrame(), D1Dplot_global.getWorkdirFile(), "Destination folder");
                if (dir==null) {
                    log.warning("No output folder selected");
                    return;
                }
                D1Dplot_global.setWorkdir(dir);
                String ext = format.toString();
                int i = 0;
                boolean nameGiven = false;
                String baseName = "";
                for (DataSerie ds: dss) {
                    String fname = "";
                    if (ds.getParent().getFile()==null) {
                        //ask for a name
                        if (!nameGiven) {
                            fname = FileUtils.DialogAskForString(this.getMainFrame(),"BaseFileName=", "Enter filename", ds.getName());
                            if (fname==null) {
                                fname = ds.getName();
                                if (fname.isEmpty()) {
                                    fname = "saved_from_d1dplot_"+i;
                                }
                            }else {
                                nameGiven=true;
                                baseName = fname;
                            }
                        }else {
                            fname = baseName+"_"+i;
                        }
                    }else {
                        fname = FileUtils.getFNameNoExt(ds.getParent().getFile().getName());
                    }
                    //here I should have  a filename;
                    File f = new File(dir+FileUtils.fileSeparator+fname);
                    f = FileUtils.canviExtensio(f, ext);
                    //i salvem
                    if (f.exists()) {
                        //ask overwrite and apply to all
                        if (!applyToAll) {
                            final JCheckBox checkbox = new JCheckBox("Apply to all");
                            final String message = "Overwrite " + f.getName() + "?";
                            final Object[] params = { message, checkbox };
                            final int n = JOptionPane.showConfirmDialog(null, params, "Overwrite existing file",
                                    JOptionPane.YES_NO_OPTION);
                            applyToAll = checkbox.isSelected();
                            if (n == JOptionPane.YES_OPTION) {
                                owrite = true;
                            } else {
                                owrite = false;
                            }
                        }
                        if (!owrite)
                            continue;
                    }
                    f = DataFileUtils.writePatternFile(f, ds, owrite, plotPanel.getGraphPanel().isPlotwithbkg()); 
                    log.info(f.toString()+" written!");
                    i++;
                }
            }
        }else {//single dat (MDT)
            FileNameExtensionFilter[] mfilt = new FileNameExtensionFilter[1];
            mfilt[0]= new FileNameExtensionFilter("Multiple dat","mdat","MDAT");
            File datFile = FileUtils.fchooserSaveAsk(mainFrame,new File(getWorkdir()), mfilt, "mdat");
            if (datFile == null){
                log.warning("No data file selected");
                return;
            }
            File f = DataFileUtils.writeMDAT(dss, datFile, true, false);
            D1Dplot_global.setWorkdir(f);
            if (f!=null) {
                log.info(f.toString()+" written!");    
            }else {
                log.warning("error writting file");
            }
        }
    }

    private void saveSVG(File fsvg){

        // Get a DOMImplementation.
        org.w3c.dom.DOMImplementation domImpl = org.apache.batik.dom.GenericDOMImplementation.getDOMImplementation();

        // Create an instance of org.w3c.dom.Document.
        String svgNS = "http://www.w3.org/2000/svg";
        org.w3c.dom.Document document = domImpl.createDocument(svgNS, "svg", null);

        // Create an instance of the SVG Generator.
        org.apache.batik.svggen.SVGGraphics2D svgGenerator = new org.apache.batik.svggen.SVGGraphics2D(document);

        // Ask the test to render into the SVG Graphics2D implementation.
        plotPanel.getGraphPanel().pinta(svgGenerator,1);

        // Finally, stream out SVG to the standard output using
        // UTF-8 encoding.
        boolean useCSS = true; // we want to use CSS style attributes
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fsvg,true)));
            svgGenerator.stream(out, useCSS);
            log.info(fsvg.getName()+" written!");

        } catch (Exception e) {
            log.warning("Error saving SVG");
        }

    }

    private void savePNG(File fpng, float factor){
        if (!XRDdata.arePlottables())return;
        try {
            ImageIO.write(plotPanel.getGraphPanel().pintaPatterns(factor), "png", fpng);
        } catch (Exception ex) {
            log.warning(fpng.toString()+" error writting png file");
            return;
        }
        log.info(fpng.toString()+" written!");
    }

    private void do_mainFrame_windowClosing(WindowEvent e) {
        if (D1Dplot_global.isKeepSize()) {
            D1Dplot_global.def_Height=this.getMainFrame().getHeight();
            D1Dplot_global.def_Width=this.getMainFrame().getWidth();
        }

        D1Dplot_global.writeParFile(plotPanel.createOptionsObject());
        mainFrame.dispose();
        System.exit(0);
    }

    private void do_mntmQuit_actionPerformed(ActionEvent e) {
        do_mainFrame_windowClosing(null);
    }

    private void do_mntmOpen_actionPerformed(ActionEvent e) {
        openDataFile();
    }

    private void do_buttonAdd_actionPerformed(ActionEvent e) {
        openDataFile();
    }

    private void do_buttonRemove_actionPerformed(ActionEvent e) {
        closeDataFile();
    }

    //duplicarem les series afegint totes les seleccionades a un mateix plottable
    //TODO passar a d1Dplot_data
    private void do_buttonDupli_actionPerformed(ActionEvent e) {
        if(XRDdata.getSelectedPlottables().isEmpty()) {
            log.warning("Select which pattern(s) to duplicate by selecting the corresponding row(s)");
            return;
        }
        XRDdata.duplicateSelected();
    }

    //IT MOVES PATTERNS ONLY
    private void do_buttonUp_actionPerformed(ActionEvent e) {
        if (!XRDdata.arePlottables())return;
        if(XRDdata.getSelectedPlottables().isEmpty()) {
            log.warning("Select which pattern(s) to move by selecting the corresponding row(s)");
            return;
        }
        XRDdata.moveSelectedPlottablesUp();
    }

    private void do_buttonDown_actionPerformed(ActionEvent e) {
        if (!XRDdata.arePlottables())return;
        if(XRDdata.getSelectedPlottables().isEmpty()) {
            log.warning("Select which pattern(s) to move by selecting the corresponding row(s)");
            return;
        }
        XRDdata.moveSelectedPlottablesDown();
    }

    private void do_mntmSaveAs_actionPerformed(ActionEvent arg0) {
        saveDataFile();
    }


    private void do_mntmSaveProfile_actionPerformed(ActionEvent e) {
        saveDataProf();
    }

    private void do_mntmCloseAll_actionPerformed(ActionEvent e) {
        XRDdata.removeAllDataSeries();
    }

    private void do_mntmExportAsPng_actionPerformed(ActionEvent e) {
        File fpng = FileUtils.fchooserSaveAsk(mainFrame, new File(D1Dplot_global.getWorkdir()), null, "png");
        if (fpng!=null){
            int w = plotPanel.getGraphPanel().getSize().width;
            int h = plotPanel.getGraphPanel().getSize().height;
            String s = (String)JOptionPane.showInputDialog(
                    mainFrame,
                    "Current plot size (Width x Heigth) is "+Integer.toString(w)+" x "+Integer.toString(h)+"pixels\n"
                            + "Scale factor to apply=",
                            "Apply scale factor",
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            null,
                    "1.0");

            if ((s != null) && (s.length() > 0)) {
                float factor = 1.0f;
                try{
                    factor=Float.parseFloat(s);
                }catch(Exception ex){
                    log.warning("Error reading png scale factor");
                }
                //preguntar si volem fons transparent o no
                boolean transp = FileUtils.YesNoDialog(this.getMainFrame(), "Create PNG with transparent background?", "PNG transparent background");
                plotPanel.getGraphPanel().setTransp(transp);
                this.savePNG(fpng,factor);
                plotPanel.getGraphPanel().setTransp(false);
                D1Dplot_global.setWorkdir(fpng);
            }
        }
    }

    private void do_mntmExportAsSvg_actionPerformed(ActionEvent e) {
        File fsvg = FileUtils.fchooserSaveNoAsk(mainFrame, new File(D1Dplot_global.getWorkdir()), null,"svg");
        if (fsvg!=null){
            this.saveSVG(fsvg);
            D1Dplot_global.setWorkdir(fsvg);
        }
    }

    private void do_btnReload_actionPerformed(ActionEvent e) {
        if (!XRDdata.arePlottables())return;
        if(XRDdata.getSelectedPlottables().isEmpty()) {
            log.warning("Select which pattern(s) to reload by selecting the corresponding row(s)");
            return;
        }
        XRDdata.reloadSelectedDataSets();

    }

    private void do_mntm2Dplot_actionPerformed(ActionEvent e) {
//        if (table_files.getSelectedRow()<0)return;
//        if (table_files.getRowCount()<=0)return;
//        int[] selRows = table_files.getSelectedRows();
//        if (selRows.length<=1){
        
        if (!XRDdata.arePlottables())return;
        if (XRDdata.getNSelectedPlottables()<=1){
            log.info("Please, select more than one pattern to create the 2D plot");
            FileUtils.InfoDialog(mainFrame,"Select more than one pattern", "2D plot selected patterns");
            return;
        }
        

        //CAL COMPROVAR QUE TOTS ELS PATTERNS COINCIDEIXEN; SINO REBINNING I ZONA COINCIDENT
        //Primer comprovar punts, sino rebinning de les series que faci falta, la primera serie mana
        for (int i=1; i<XRDdata.getSelectedPlottables().size(); i++){
            boolean coin = PattOps.haveCoincidentPointsDS(XRDdata.getSelectedPlottables().get(0), XRDdata.getSelectedPlottables().get(i));
            if (!coin){
                DataSerie dsToRebin = XRDdata.getSelectedPlottables().get(i);
                dsToRebin = PattOps.rebinDS(XRDdata.getSelectedPlottables().get(0), XRDdata.getSelectedPlottables().get(i));
                XRDdata.getSelectedPlottables().set(i, dsToRebin);
                log.info("Rebinning serie "+i);
            }
        }

        
        //Pero ara encara pot ser que la serie inicial tingués més punts que les altres i això faria que peti,
        //hem de SELECCIONAR el rang coincident
        int tol = 30;
        //aqui fer un for per totes les dataseries
        double[] t2is = new double[XRDdata.getSelectedPlottables().size()];
        double[] t2fs = new double[XRDdata.getSelectedPlottables().size()];
        for (int i=0; i<XRDdata.getSelectedPlottables().size();i++){
            t2is[i]=XRDdata.getSelectedPlottables().get(i).getCorrectedPoint(0,plotPanel.getGraphPanel().isPlotwithbkg()).getX();
            t2fs[i]=XRDdata.getSelectedPlottables().get(i).getCorrectedPoint(XRDdata.getSelectedPlottables().get(i).getNPoints()-1,plotPanel.getGraphPanel().isPlotwithbkg()).getX();
        }
        double t2i = PattOps.findMax(t2is);
        double t2f = PattOps.findMin(t2fs);

        Plottable_point[] dpini = new DataPoint[XRDdata.getSelectedPlottables().size()];
        Plottable_point[] dpfin = new DataPoint[XRDdata.getSelectedPlottables().size()];
        int[] iinidp = new int[XRDdata.getSelectedPlottables().size()];
        int[] ifindp = new int[XRDdata.getSelectedPlottables().size()];
        int[] rangedp = new int[XRDdata.getSelectedPlottables().size()];

        for (int i=0; i<XRDdata.getSelectedPlottables().size();i++){
            dpini[i] = XRDdata.getSelectedPlottables().get(i).getClosestPointX(t2i, tol);
            dpfin[i] = XRDdata.getSelectedPlottables().get(i).getClosestPointX(t2f, tol);
            iinidp[i] = XRDdata.getSelectedPlottables().get(i).getIndexOfDP(dpini[i]);
            ifindp[i] = XRDdata.getSelectedPlottables().get(i).getIndexOfDP(dpfin[i]);
            rangedp[i] = ifindp[i] - iinidp[i];
        }

        //check ranges
        int totRange = 0;
        for (int i=0;i<XRDdata.getSelectedPlottables().size();i++){
            totRange = totRange + rangedp[i];
        }
        if (totRange/XRDdata.getSelectedPlottables().size() != rangedp[0]){
            log.warning("Inconsitency on nr of points in the coincident range");
        }

        //apliquem NOMES SI ES INCONSISTENT
        for (int i=0; i<XRDdata.getSelectedPlottables().size(); i++){
            if (t2is[i]!=t2i || t2fs[i]!=t2f){
                DataSerie ds = XRDdata.getSelectedPlottables().get(i);
                ds = XRDdata.getSelectedPlottables().get(i).getSubDataSerie(t2i, t2f);
                XRDdata.getSelectedPlottables().set(i, ds);
            }
        }

        p2 = new Plot2DPanel(this.getMainFrame(),plotPanel.getGraphPanel());
        List<DataSerie> adss = new ArrayList<DataSerie>();
        for (int i=0; i<XRDdata.getSelectedPlottables().size(); i++){
            adss.add(XRDdata.getSelectedPlottables().get(i));
        }
        p2.setImagePatts(adss);
    }

    private void do_mntmSequentialyOffset_actionPerformed(ActionEvent arg0) {
//        if (table_files.getSelectedRow()<0)return;
//        if (table_files.getRowCount()<=0)return;
//        int[] selRows = table_files.getSelectedRows();

        if (!XRDdata.arePlottables())return;
        if (XRDdata.getNSelectedPlottables()<=1){
            log.info("Please, select more than one pattern to apply Y offset");
            FileUtils.InfoDialog(mainFrame,"Select more than one pattern", "Apply Y offset");
            return;
        }
        
        String s = (String)JOptionPane.showInputDialog(
                null,
                "Y offset increment between patterns",
                "Sequential Y offset",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "2000");
        double yoff = 0;
        if ((s != null) && (s.length() > 0)) {
            try{
                yoff = Double.parseDouble(s);
            }catch(Exception ex){
                log.warning("Error reading y offset");
                return;
            }
        }
        double yoffIni = XRDdata.getSelectedPlottables().get(0).getYOffset();
        for (int i=1; i<=XRDdata.getSelectedPlottables().size()-1;i++){
            //ara ja tenim les linies que hem d'aplicar offset
            XRDdata.getSelectedPlottables().get(i).setYOffset(yoffIni+yoff*i);
        }
        XRDdata.updateFullTable();
        plotPanel.getGraphPanel().actualitzaPlot();

    }

    
    private void do_mntmConvertToWavelength_actionPerformed(ActionEvent e) {
        XRDdata.convertToWL(e);
    }
    private void do_mntmChangeXUnits_actionPerformed(ActionEvent e) {
        XRDdata.changeXunits(e);
    }
    private void do_mntmSumSelected_actionPerformed(ActionEvent e) {
        XRDdata.sumPatterns(e);
    }

    private void do_mntmRebinning_actionPerformed(ActionEvent arg0) {
        if (!XRDdata.arePlottables())return;
        if (!XRDdata.areSelectedPlottables()) {
            log.info("Please, select the patterns you want to do a rebinning");
            FileUtils.InfoDialog(mainFrame,"Select the patterns you want to perform the rebinning", "Rebinning");
            return;
        }

        DataSerie firstDS = XRDdata.getFirstSelectedDataSerie();
        String st2i = FileUtils.dfX_4.format(firstDS.getCorrectedPoint(0, plotPanel.getGraphPanel().isPlotwithbkg()).getX());
        String st2f = FileUtils.dfX_4.format(firstDS.getCorrectedPoint(firstDS.getNPoints()-1,plotPanel.getGraphPanel().isPlotwithbkg()).getX());
        String sstep = FileUtils.dfX_5.format(firstDS.calcStep());
        
        String s = (String)JOptionPane.showInputDialog(
                null,
                "Rebinning of selected patterns\n especify T2I STEP T2F",
                "Rebinning of selected patterns",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                (st2i+" "+sstep+" "+st2f).replace(",", "."));

        double t2i = 0;
        double t2f = 0;
        double step = 0; 
        if ((s != null) && (s.length() > 0)) {
            try{
                String[] vals = s.split("\\s+");
                t2i = Double.parseDouble(vals[0]);
                step = Double.parseDouble(vals[1]);
                t2f = Double.parseDouble(vals[2]);
            }catch(Exception ex){
                log.warning("Error parsing values");
                return;
            }
        }
        if (step==0)return;
        if (t2f==0)return;

        //crearem una dataserie falsa
        List<Plottable_point> puntsdummy = new ArrayList<Plottable_point>();
        double t2 = t2i;
        while (t2<=t2f){
            puntsdummy.add(new DataPoint(t2,0,0,null));
            t2 = t2+step;
        }
        DataSerie dummy = new DataSerie(firstDS,puntsdummy,firstDS.getxUnits());
        
        //now for all selected
        for (DataSerie ds:XRDdata.getSelectedPlottables()) {
            DataSerie newds = PattOps.rebinDS(dummy, ds);
            newds.setName(ds.getName()+" (rebinned)");
            DataSet dsp = new DataSet(newds.getWavelength());
            dsp.addDataSerie(newds);
            XRDdata.addDataSet(dsp, true, false); //actualitzarem al final
        }
        plotPanel.getGraphPanel().actualitzaPlot();
    }



    private void do_mntmSaveProject_actionPerformed(ActionEvent e) {
        FileNameExtensionFilter[] filt = new FileNameExtensionFilter[1];
        filt[0] = new FileNameExtensionFilter("d1Dplot project","d1d");
        File f = FileUtils.fchooserSaveAsk(this.mainFrame, D1Dplot_global.getWorkdirFile(), filt, "d1d", "Save d1Dplot project");
        if (f!=null) {
            //ask if write full data or relative paths only
            boolean fulldata = FileUtils.YesNoDialog(this.getMainFrame(), "Include data in the file?");
            DataFileUtils.writeProject(f, true, this.plotPanel,this.XRDdata, fulldata);
            D1Dplot_global.setWorkdir(f);
        }
    }
    private void do_mntmOpenProject_actionPerformed(ActionEvent e) {
        FileNameExtensionFilter[] filt = new FileNameExtensionFilter[1];
        filt[0] = new FileNameExtensionFilter("d1Dplot project","d1d");
        File f = FileUtils.fchooserOpen(this.mainFrame, D1Dplot_global.getWorkdirFile(), filt, 0);
        if (f!=null) {
            DataFileUtils.readProject(f, this.plotPanel, this.XRDdata, this);
            D1Dplot_global.setWorkdir(f);
        }
//        this.updateData(false,true);
        
    }
    
    private void do_mntmCheckForUpdates_actionPerformed(ActionEvent e) {
//        log.info("software updates not available");
                String bona="";
                String url="https://www.cells.es/en/beamlines/bl04-mspd/preparing-your-experiment";
                
        		try {
        			URL mspd = new URL(url);
        			BufferedReader in = new BufferedReader(new InputStreamReader(mspd.openStream()));
        	        String inputLine;
        	        while ((inputLine = in.readLine()) != null) {
        	            if (FileUtils.containsIgnoreCase(inputLine, "d1Dplot software for windows v")) {
        	            	bona = inputLine;
        	            	break;
        	            }
        	        }
        	        in.close();
        			
        		} catch (Exception e1) {
        			if(D1Dplot_global.isDebug())e1.printStackTrace();
        			log.warning("Error checking for new versions");
        		}
        
        		//d2Dplot software for linux v1811" href="https://www.cells.es/en/beamlines/bl04-mspd/d2dplot1811win_181122-tar.gz
        		if (bona.length()>0) {
        			String data = bona.split(".zip")[0];
        			data = data.split("win_")[1];
        			int webVersion = Integer.parseInt(data);
        			if (webVersion != D1Dplot_global.build_date) {
        				boolean yes = FileUtils.YesNoDialog(this.getMainFrame(), "New d1Dplot version is available ("+webVersion+"). Please download at\n"+url,"New version available!");
        				if (yes) {
        					FileUtils.openURL(url);
        				}
        			}
        			if (webVersion == D1Dplot_global.build_date) {
        				FileUtils.InfoDialog(this.getMainFrame(), "You have the last version of d1Dplot ("+D1Dplot_global.build_date+").", "d1Dplot is up to date");
        			}
        		}
    }

    private void do_mntmDB_actionPerformed(ActionEvent e) {
        if (DBDiag == null) {
            DBDiag = new Database(this.plotPanel.getGraphPanel(),this.XRDdata);
        }
        DBDiag.visible(true);
    }

    private void do_mntmFindPeaks_1_actionPerformed(ActionEvent e) {
        if (FindPksDiag == null) {
            FindPksDiag = new FindPeaksDialog(this.plotPanel.getGraphPanel(),this.XRDdata);
        }
        FindPksDiag.visible(true);
    }
    
    private void do_mntmCalibwave_actionPerformed(ActionEvent arg0) {
        if (waveCalDiag == null) {
        	waveCalDiag = new WaveCalDialog(this.plotPanel.getGraphPanel(),this.XRDdata);
        }
        waveCalDiag.visible(true);
	}
    
    private void do_mntmFitPeaks_actionPerformed(ActionEvent e) {
        if (fitPksDiag == null) {
            fitPksDiag = new FitPeakDialog(this.plotPanel.getGraphPanel(),this.XRDdata);
        }
        fitPksDiag.visible(true);
    }
    
    private void do_mntmCalcBackground_actionPerformed(ActionEvent e) {
        if (bkgDiag == null) {
            bkgDiag = new BackgroundDialog(this.plotPanel.getGraphPanel(),this.XRDdata);
        }
        bkgDiag.visible(true);
    }

    private void do_mntmSubtractPatterns_actionPerformed(ActionEvent e) {
        if (subDiag == null) {
            subDiag = new SubtractDialog(this.plotPanel.getGraphPanel(),this.XRDdata);
        }
        subDiag.visible(true);
    }

    private void do_mntmDajust_actionPerformed(ActionEvent e) {
        if (dajustDiag == null) {
            dajustDiag = new DajustDialog(this.plotPanel.getGraphPanel(),this.XRDdata);
        }
        dajustDiag.setVisible(true);
    }
    

    private void do_mntmAbout_actionPerformed(ActionEvent e) {
        if (aboutDiag==null){
            aboutDiag = new AboutDialog(this.getMainFrame());    
        }
        aboutDiag.visible(true);
    }

    private void do_mntmUsersGuide_actionPerformed(ActionEvent e) {
        if (aboutDiag==null){
            aboutDiag = new AboutDialog(this.getMainFrame());    
        }
        aboutDiag.do_btnUsersGuide_actionPerformed(e);
    }
    
    public JFrame getMainFrame() {
        return mainFrame;
    }

    public static int getDef_Width() {
        return def_Width;
    }

    public static void setDef_Width(int def_Width) {
        D1Dplot_main.def_Width = def_Width;
    }

    public static int getDef_Height() {
        return def_Height;
    }

    public static void setDef_Height(int def_Height) {
        D1Dplot_main.def_Height = def_Height;
    }

    public static String getLandF() {
        return LandF;
    }

    public static void setLandF(String landF) {
        LandF = landF;
    }

    private String getWorkdir(){
        return D1Dplot_global.getWorkdir();
    }
    
    //TODO revisar perque Abans era fitGraph, actualitzaPlot
    public void updateData(boolean updateTable, boolean updateGraph, boolean fitGraph) {
        if (updateTable)XRDdata.updateFullTable();
        if (updateGraph)plotPanel.getGraphPanel().actualitzaPlot();
        if (fitGraph)plotPanel.getGraphPanel().fitGraph();
    }
    
    protected void do_mntmInvertOrder_actionPerformed(ActionEvent e) {
        XRDdata.invertOrderTable();
    }
    
    protected void autoFindPeaksCurrentSerie() {
        if (FindPksDiag == null) {
        	FindPksDiag = new FindPeaksDialog(this.plotPanel.getGraphPanel(),this.XRDdata);
        }
    }
}


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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import net.miginfocom.swing.MigLayout;
import vava33.d1dplot.auxi.ArgumentLauncher;
import vava33.d1dplot.auxi.ColorEditor;
import vava33.d1dplot.auxi.ColorRenderer;
import vava33.d1dplot.auxi.DataFileUtils;
import vava33.d1dplot.auxi.DataSerie;
import vava33.d1dplot.auxi.PattOps;
import vava33.d1dplot.auxi.Pattern1D;
import vava33.d1dplot.auxi.PatternsTableCellRenderer;
import vava33.d1dplot.auxi.PatternsTableModel;

import com.vava33.jutils.FileUtils;
import com.vava33.jutils.LogJTextArea;
import com.vava33.jutils.VavaLogger;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

public class D1Dplot_main {

    private static float tAoutFsize = 12.0f;
    private static int def_Width=1024;
    private static int def_Height=768;
    
    private Background_dialog bkgDiag;
    private FindPeaks_dialog FindPksDiag;
    
    private JFrame mainFrame;
    private static VavaLogger log;
    private JTable table_files;
    private LogJTextArea tAOut;
    private PlotPanel panel_plot;
    private JTextField txtXtitle;
    private JTextField txtYtitle;
    private JTextField txtHklTickSize;
    private JCheckBox chckbxShowLegend;
    private JComboBox comboTheme;
    private static JCheckBox chckbxIntensityWithBackground;
    private JTextField txtLegendx;
    private JTextField txtLegendy;
    private JTabbedPane tabbedPanel_bottom;
    private JMenuBar menuBar;
    private JMenu mnFile;
    private JMenuItem mntmOpen;
    private JSeparator separator;
    private JSeparator separator_1;
    private JSplitPane splitPane;
    private JScrollPane scrollPane_1;
    private JPanel panel_DS;
    private JButton buttonAdd;
    private JButton buttonRemove;
    private JButton buttonDown;
    private JButton buttonUp;
    private JMenuItem mntmClose;
    private JMenuItem mntmCloseAll;
    private JSeparator separator_2;
    private JMenuItem mntmQuit;
    private JMenuItem mntmSaveAs;
    private JCheckBox chckbxAutopos;
    private JCheckBox chckbxHklLabels;
    private JCheckBox chckbxShowGridLines;
    private JMenu mnPlot;
    private JMenuItem mntmExportAsPng;
    private JMenuItem mntmExportAsSvg;
    private JSeparator separator_3;
    private JCheckBox chckbxShowNegativeLabels;
    private JCheckBox chckbxVerticalYLabel;
    private JMenu mnOps;
    private JMenuItem mntmFindPeaks;
    private JMenuItem mntmFindPeaks_1;
    private JMenuItem mntmSavePeaksAs;
    private JMenuItem mntmCalcBackground;
    
    /**
     * Launch the application.
     */
    public static void main(final String[] args) {
        
        //first thing to do is read PAR files if exist
        FileUtils.detectOS();
        D1Dplot_global.readParFile();
        D1Dplot_global.initPars();
        //LOGGER
        log = D1Dplot_global.getVavaLogger(D1Dplot_main.class.getName());
        System.out.println(log.logStatus());
                
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//            if(UIManager.getLookAndFeel().toString().contains("metal")){
//                UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");    
//            }
            // UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); //java metal
            
        } catch (Throwable e) {
            if (D1Dplot_global.isDebug())e.printStackTrace();
            log.warning("Error initializing System look and feel");
        }


        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    D1Dplot_main frame = new D1Dplot_main();
                    frame.showMainFrame();
//                    frame.inicia(); ja es fa al final del constructor
                    //AQUI POSO EL ARGUMENT LAUNCHER
                    D1Dplot_global.printAllOptions("info");
                    ArgumentLauncher.readArguments(frame, args);
//                    if (ArgumentLauncher.isLaunchGraphics()){
//                        frame.showMainFrame();
//                    }else{
//                        log.info("Exiting...");
//                        frame.disposeMainFrame();
//                        return;
//                    }
                } catch (Exception e) {
                    if (D1Dplot_global.isDebug())e.printStackTrace();
                    log.severe("Error initializing main window");
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

    public void showMainFrame(){
//        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);    
    }
    public void disposeMainFrame(){
        mainFrame.dispose();
    }
    
    protected void do_mainFrame_windowClosing(WindowEvent e) {
        boolean ok = D1Dplot_global.writeParFile();
        log.debug("par file written (method returned "+Boolean.toString(ok));
        mainFrame.dispose();
    }
    protected void do_mntmQuit_actionPerformed(ActionEvent e) {
        do_mainFrame_windowClosing(null);
    }
    
    
    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        mainFrame = new JFrame();
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                do_mainFrame_windowClosing(e);
            }
        });
        mainFrame.setTitle("D1Dplot");
        mainFrame.setBounds(100, 100, 1024, 768);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.getContentPane().setLayout(new MigLayout("insets 2", "[grow]", "[grow]"));
        
        splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.85);
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        mainFrame.getContentPane().add(splitPane, "cell 0 0,grow");
        
        tabbedPanel_bottom = new JTabbedPane(JTabbedPane.RIGHT);
        tabbedPanel_bottom.setBorder(null);
        splitPane.setRightComponent(tabbedPanel_bottom);

        JComboBox comboXunitsTable = new JComboBox();
        for (DataSerie.xunits a :DataSerie.xunits.values()){
            comboXunitsTable.addItem(a.getName());
        }
        
        //MENU CLICK DRETA
        final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem editValues = new JMenuItem("Edit Selected Values");
        editValues.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                editMultipleValuesTable(e);
            }
        });
        popupMenu.add(editValues);
        
        JMenuItem convertWL = new JMenuItem("Convert to WL");
        convertWL.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                convertToWL(e);
            }
        });
        popupMenu.add(convertWL);
        
        JMenuItem changeXU = new JMenuItem("Change X units");
        changeXU.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeXunits(e);
            }
        });
        popupMenu.add(changeXU);        
        
        table_files = new JTable(new PatternsTableModel());
        table_files.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table_files.setColumnSelectionAllowed(true);
        table_files.setCellSelectionEnabled(true);

        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table_files);
//        tabbedPanel_bottom.addTab("Data Series", null, scrollPane, null);
        
        table_files.setDefaultRenderer(String.class, new PatternsTableCellRenderer());
        table_files.setDefaultRenderer(Integer.class, new PatternsTableCellRenderer());
        table_files.setDefaultRenderer(Float.class, new PatternsTableCellRenderer());
        table_files.setDefaultRenderer(Double.class, new PatternsTableCellRenderer());
//        table_files.setDefaultRenderer(Boolean.class, new PatternsTableCellRenderer());
        
        //Set up renderer and editor for the Favorite Color column.
        table_files.setDefaultRenderer(Color.class,
                new ColorRenderer(true));
        table_files.setDefaultEditor(Color.class,
                new ColorEditor());

        table_files.getModel().addTableModelListener(new TableModelListener(){
            public void tableChanged(TableModelEvent e) {
                applicarModificacioTaula(e.getColumn(),e.getFirstRow(),e.getLastRow());
            }
        });
        table_files.getColumn(PatternsTableModel.columns.XUnits.toString()).setCellEditor(new DefaultCellEditor(comboXunitsTable));
        table_files.setComponentPopupMenu(popupMenu);
        
        table_files.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                aplicarselecciotaula(arg0);
            }
        });


        panel_DS = new JPanel();
        tabbedPanel_bottom.addTab("Data Series", null, panel_DS, null);
        panel_DS.setLayout(new MigLayout("insets 0", "[grow][][]", "[grow][]"));

        new JLabel("New label");
        panel_DS.add(scrollPane, "cell 0 0 1 2,grow");
        
        buttonAdd = new JButton("+");
        buttonAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_buttonAdd_actionPerformed(e);
            }
        });
        panel_DS.add(buttonAdd, "cell 1 0,growx,aligny bottom");
        
        buttonRemove = new JButton("-");
        buttonRemove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_buttonRemove_actionPerformed(e);
            }
        });
        buttonRemove.setPreferredSize(new Dimension(23, 28));
        panel_DS.add(buttonRemove, "cell 2 0,growx,aligny bottom");
        
        buttonUp = new JButton("^");
        buttonUp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_buttonUp_actionPerformed(e);
            }
        });
        panel_DS.add(buttonUp, "cell 1 1,growx,aligny top");
        
        buttonDown = new JButton("v");
        buttonDown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_buttonDown_actionPerformed(e);
            }
        });
        panel_DS.add(buttonDown, "cell 2 1,growx,aligny top");

                        
        scrollPane_1 = new JScrollPane();
        tabbedPanel_bottom.addTab("Plot Settings", null, scrollPane_1, null);

        
        JPanel panel = new JPanel();
        scrollPane_1.setViewportView(panel);
        panel.setBorder(null);
        panel.setLayout(new MigLayout("", "[][grow][][][][][][][][]", "[][][]"));
        
        JLabel lblXTitle = new JLabel("X title");
        panel.add(lblXTitle, "cell 0 0,alignx trailing");
        
        txtXtitle = new JTextField();
        txtXtitle.setText("xtitle");
        panel.add(txtXtitle, "cell 1 0,growx");
        txtXtitle.setColumns(10);
        
        txtXtitle.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateLabelX();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateLabelX();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateLabelX();
            }
        });
        
        
        separator = new JSeparator();
        separator.setOrientation(SwingConstants.VERTICAL);
        panel.add(separator, "cell 2 0 1 3,growy");
        
        chckbxShowLegend = new JCheckBox("Legend");
        chckbxShowLegend.setSelected(true);
        chckbxShowLegend.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent arg0) {
                do_chckbxShowLegend_itemStateChanged(arg0);
            }
        });
        panel.add(chckbxShowLegend, "cell 3 0 2 1");
        
        separator_1 = new JSeparator();
        separator_1.setOrientation(SwingConstants.VERTICAL);
        panel.add(separator_1, "cell 5 0 1 3,growy");
        
        JLabel lblHklTickSize = new JLabel("HKL tick size (PRF)");
        panel.add(lblHklTickSize, "cell 6 0");
        
        txtHklTickSize = new JTextField();
        txtHklTickSize.setText(Integer.toString(Pattern1D.getHklticksize()));
        panel.add(txtHklTickSize, "cell 7 0,growx");
        txtHklTickSize.setColumns(3);
        
        separator_3 = new JSeparator();
        separator_3.setOrientation(SwingConstants.VERTICAL);
        panel.add(separator_3, "cell 8 0 1 3,growy");
        
        chckbxShowGridLines = new JCheckBox("Grid lines");
        chckbxShowGridLines.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                do_chckbxShowGridLines_itemStateChanged(e);
            }
        });
        panel.add(chckbxShowGridLines, "cell 9 0");
        
        JLabel lblYTitle = new JLabel("Y title");
        panel.add(lblYTitle, "cell 0 1,alignx trailing");
        
        txtYtitle = new JTextField();
        txtYtitle.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateLabelY();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateLabelY();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateLabelY();
            }
        });
        txtYtitle.setText("ytitle");
        panel.add(txtYtitle, "cell 1 1,growx");
        txtYtitle.setColumns(10);
        
        chckbxIntensityWithBackground = new JCheckBox("Bkg Inten (PRF)");
        chckbxIntensityWithBackground.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                do_chckbxIntensityWithBackground_itemStateChanged(e);
            }
        });
        
        chckbxAutopos = new JCheckBox("autoPos");
        chckbxAutopos.setSelected(true);
        chckbxAutopos.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                do_chckbxAutopos_itemStateChanged(e);
            }
        });
        panel.add(chckbxAutopos, "cell 3 1 2 1");
        panel.add(chckbxIntensityWithBackground, "cell 6 1 2 1");
        
        chckbxShowNegativeLabels = new JCheckBox("Negative Yaxis labels");
        chckbxShowNegativeLabels.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent arg0) {
                do_chckbxShowNegativeLabels_itemStateChanged(arg0);
            }
        });
        panel.add(chckbxShowNegativeLabels, "cell 9 1");
        
        JLabel lbltheme = new JLabel("Theme");
        panel.add(lbltheme, "cell 0 2,alignx trailing");
        
        comboTheme = new JComboBox();
        comboTheme.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent arg0) {
                do_comboTheme_itemStateChanged(arg0);
            }
        });
        comboTheme.setModel(new DefaultComboBoxModel(new String[] {"Light", "Dark"}));
        panel.add(comboTheme, "cell 1 2,growx,aligny top");
        
        chckbxHklLabels = new JCheckBox("HKL labels on mouse");
        chckbxHklLabels.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent arg0) {
                do_chckbxHklLabels_itemStateChanged(arg0);
            }
        });
        
        txtLegendx = new JTextField();
        txtLegendx.setEditable(false);
        txtLegendx.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtLegendx_actionPerformed(e);
            }
        });
        txtLegendx.setText("legendX");
        panel.add(txtLegendx, "cell 3 2,growx");
        txtLegendx.setColumns(5);
        
        txtLegendy = new JTextField();
        txtLegendy.setEditable(false);
        txtLegendy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtLegendy_actionPerformed(e);
            }
        });
        txtLegendy.setText("legendY");
        panel.add(txtLegendy, "cell 4 2,growx");
        txtLegendy.setColumns(5);
        chckbxHklLabels.setSelected(true);
        panel.add(chckbxHklLabels, "cell 6 2 2 1");
        
        chckbxVerticalYLabel = new JCheckBox("Vertical Y label");
        chckbxVerticalYLabel.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                do_chckbxVerticalYLabel_itemStateChanged(e);
            }
        });
        panel.add(chckbxVerticalYLabel, "cell 9 2");

        tAOut = new LogJTextArea();
        tabbedPanel_bottom.addTab("Log", null, tAOut, null);

        panel_plot = new PlotPanel();
        splitPane.setLeftComponent(panel_plot);
        //        panel_plot = (PlotPanel) new JPanel();
        panel_plot.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        
        menuBar = new JMenuBar();
        mainFrame.setJMenuBar(menuBar);
        
        mnFile = new JMenu("File");
        mnFile.setMnemonic('f');
        menuBar.add(mnFile);
        
        mntmOpen = new JMenuItem("Open...");
        mntmOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        mntmOpen.setMnemonic('o');
        mntmOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmOpen_actionPerformed(e);
            }
        });
        mnFile.add(mntmOpen);
        
        mntmClose = new JMenuItem("Close");
        mntmClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmClose_actionPerformed(e);
            }
        });
        
        mntmSaveAs = new JMenuItem("Save as...");
        mntmSaveAs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                do_mntmSaveAs_actionPerformed(arg0);
            }
        });
        mnFile.add(mntmSaveAs);
        mnFile.add(mntmClose);
        
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
        
        mnPlot = new JMenu("Plot");
        menuBar.add(mnPlot);
        
        mntmExportAsPng = new JMenuItem("Export as PNG...");
        mntmExportAsPng.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmExportAsPng_actionPerformed(e);
            }
        });
        mnPlot.add(mntmExportAsPng);
        
        mntmExportAsSvg = new JMenuItem("Export as SVG...");
        mntmExportAsSvg.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmExportAsSvg_actionPerformed(e);
            }
        });
        mnPlot.add(mntmExportAsSvg);
        
        mnOps = new JMenu("Ops");
        menuBar.add(mnOps);
        
        mntmFindPeaks = new JMenuItem("Find Peaks (Auto)");
        mntmFindPeaks.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmFindPeaks_actionPerformed(e);
            }
        });
        mnOps.add(mntmFindPeaks);
        
        mntmFindPeaks_1 = new JMenuItem("Find Peaks...");
        mntmFindPeaks_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmFindPeaks_1_actionPerformed(e);
            }
        });
        mnOps.add(mntmFindPeaks_1);
        
        mntmSavePeaksAs = new JMenuItem("Save Peaks as...");
        mnOps.add(mntmSavePeaksAs);
        
        mntmCalcBackground = new JMenuItem("Calc Background...");
        mntmCalcBackground.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmCalcBackground_actionPerformed(e);
            }
        });
        mnOps.add(mntmCalcBackground);
                
        inicia();
    }

    //=========================================================
    
    private void changeXunits(ActionEvent e){
        if (table_files.getSelectedRow()<0)return;
        if (table_files.getRowCount()<=0)return;
        //TODO avisar que es crearan
        //        FileUtils.confirmDialog("New series will be crated", "New Series")

        String[] comboValues = new String[DataSerie.xunits.values().length];
        int i = 0;
        for (DataSerie.xunits a :DataSerie.xunits.values()){
            comboValues[i] = a.getName();
            i = i+1;
        }
        String s = (String)JOptionPane.showInputDialog(
                mainFrame,
                            "Change to X-units (new serie will be generated)",
                            "Change X-units",
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            comboValues,
                            comboValues[0]);

        if ((s != null) && (s.length() > 0)) {
            
            DataSerie.xunits destUnits = null;
            for (DataSerie.xunits x: DataSerie.xunits.values()){
                if (x.getEnum(s)!=null){
                    destUnits=x;
                    break;
                }
            }
            if (destUnits==null){
                log.info("choose a valid x-units value");
                return;
            }
            
            int[] selRows = table_files.getSelectedRows();
            log.debug("number of rows selected ="+ selRows.length);
            log.writeNameNums("CONFIG", true, "selRows", selRows);
            
            for (i=0; i<=selRows.length-1;i++){
                int selRow = selRows[i];
                //primer agafem el pattern i serie seleccionades
                int pattern = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
                int serie = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nS.toString()));
                
                Pattern1D patt = panel_plot.getPatterns().get(pattern);
                DataSerie ds = patt.getSeries().get(serie);
                
                if (ds.getWavelength()<0){
                    log.info(String.format("error: pattern %d serie %d has no wavelength assigned", pattern,serie));
                    continue;
                }
                log.debug(String.format("Pattern=%d Serie=%d", pattern,serie));
                log.debug(String.format("SerieUnits=%s DestUnits=%s", ds.getxUnits().getName(), destUnits.getName()));
                
                //CONVERTIM
                DataSerie newDS = ds.convertToXunits(destUnits);
                
//                if (destUnits==ds.getxUnits()){
//                    //nothing to do
//                }else{
//                    
//                }
                if (newDS!=null){
                    patt.AddDataSerie(newDS);                    
                }else{
                    log.info("not converted, same input and output units maybe?");
                }

            }
            panel_plot.repaint();
            this.updateData();
        }
    }
    
    private void convertToWL(ActionEvent e){
        if (table_files.getSelectedRow()<0)return;
        if (table_files.getRowCount()<=0)return;
        //TODO avisar que es crearan
        //        FileUtils.confirmDialog("New series will be crated", "New Series")

        String s = (String)JOptionPane.showInputDialog(
                mainFrame,
                "New wavelength=",
                "Covnert pattern to new wavelength",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "");
        
        if ((s != null) && (s.length() > 0)) {
            double newWL = -1;
            try{
                newWL = Double.parseDouble(s);
            }catch(Exception ex){
                ex.printStackTrace();
            }
            if (newWL<0){
                log.info("invalid wavelength entered");
                return;
            }
            
            int[] selRows = table_files.getSelectedRows();
            log.debug("number of rows selected ="+ selRows.length);
            
            for (int i=0; i<=selRows.length-1;i++){
                int selRow = selRows[i];
                //primer agafem el pattern i serie seleccionades
                int pattern = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
                int serie = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nS.toString()));
                
                Pattern1D patt = panel_plot.getPatterns().get(pattern);
                DataSerie ds = patt.getSeries().get(serie);
                
                if (ds.getWavelength()<0){
                    log.info(String.format("pattern %d serie %d has no wavelength assigned", pattern,serie));
                    continue;
                }
                patt.AddDataSerie(ds.convertToNewWL(newWL));
            }
            
            panel_plot.repaint();
            this.updateData();
        }
        
        
        
        
    }
    
    private void editMultipleValuesTable(ActionEvent e){
        
        if (table_files.getSelectedRow()<0)return;
        if (table_files.getRowCount()<=0)return;

        String s = (String)JOptionPane.showInputDialog(
                            mainFrame,
                            "New value=",
                            "Change values column-wise",
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            null,
                            "");

        //If a string was returned, say so.
        if ((s != null) && (s.length() > 0)) {
            //edit all the selected cells
          //prova amb selected rows:
            int selCol = table_files.getSelectedColumn();
            int[] selRows = table_files.getSelectedRows();
            log.debug("number of rows selected ="+ selRows.length);
            log.debug("selrows[0] ="+ selRows[0]);
            log.debug("columnSelected ="+ selCol);
            for (int i=0; i<=selRows.length-1;i++){
                int selRow = selRows[i];
                table_files.setValueAt(Float.parseFloat(s), selRow, selCol); //TODO vigilar si amb els doubles (wave, zero) peta
            }
            return;
        }
    }
    
    private void aplicarselecciotaula(ListSelectionEvent arg0){
        if (table_files.getSelectedRow()<0)return;
        if (table_files.getRowCount()<=0)return;
      //prova amb selected rows:
        int[] selRows = table_files.getSelectedRows();
        log.debug("number of rows selected ="+ selRows.length);
        if (selRows.length==0)return;
        panel_plot.getSelectedSerie().clear();
        for (int i=0; i<selRows.length;i++){
            int indexP = (Integer) table_files.getModel().getValueAt(i, this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
            int indexDS = (Integer) table_files.getModel().getValueAt(i, this.getColumnByName(table_files, PatternsTableModel.columns.nS.toString()));
            panel_plot.getSelectedSerie().add(panel_plot.getPatterns().get(indexP).getSeries().get(indexDS));    
        }
    }
    
    private void applicarModificacioTaula(int columna, int filaIni, int filaFin){

        if (table_files.getSelectedRow()<0)return;
        if (table_files.getRowCount()<=0)return;

        for (int i=filaIni; i<=filaFin;i++){
            int indexP = (Integer) table_files.getModel().getValueAt(i, this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
            int indexDS = (Integer) table_files.getModel().getValueAt(i, this.getColumnByName(table_files, PatternsTableModel.columns.nS.toString()));
            
            if (indexP<0 || indexDS<0){
                log.debug("pattern1D or DataSerie not found by row");
                return;
            }
            
            Pattern1D selPatt = panel_plot.getPatterns().get(indexP);
            PatternsTableModel.columns colName = FileUtils.searchEnum(PatternsTableModel.columns.class,table_files.getColumnName(columna));
            log.debug("column="+colName.toString()+" patt="+indexP+" serie="+indexDS+ "newValue="+table_files.getValueAt(i, columna).toString());
            switch(colName){ //        Filename, Color, Scale, ZerOff, Wavel, marker, line, errBars
                case Color:
                    selPatt.getSeries().get(indexDS).setColor((Color) table_files.getValueAt(i, columna));
                    break;
                case Scale:
                    selPatt.getSeries().get(indexDS).setScale((Float) table_files.getValueAt(i, columna));
                    break;
                case ZerOff:
                    selPatt.getSeries().get(indexDS).setZerrOff((Double) table_files.getValueAt(i, columna));
                    break;
                case Wavel:
                    selPatt.getSeries().get(indexDS).setWavelength((Double) table_files.getValueAt(i, columna));
                    break;
                case XUnits:
                    String xunits = (String) table_files.getValueAt(i, columna);
                    for (DataSerie.xunits x: DataSerie.xunits.values()){
                        if (x.getEnum(xunits)!=null){
                            selPatt.getSeries().get(indexDS).setxUnits(x);
                            if (i==0){
                                panel_plot.setXlabel(x.getName());
                            }
                            break;
                        }
                    }
                    break;
                case YOffset:
                    selPatt.getSeries().get(indexDS).setYOff((Double) table_files.getValueAt(i, columna));
                    break;
                case Marker:
                    selPatt.getSeries().get(indexDS).setMarkerSize((Float) table_files.getValueAt(i, columna));
                    break;
                case Line:
                    selPatt.getSeries().get(indexDS).setLineWidth((Float) table_files.getValueAt(i, columna));
                    break;
                case ErrBars:
                    selPatt.getSeries().get(indexDS).setShowErrBars((Boolean) table_files.getValueAt(i, columna));
                    break;
                case Show:
                    selPatt.getSeries().get(indexDS).setPlotThis((Boolean) table_files.getValueAt(i, columna));
                    break;
                default:
                    break;
            }
        }
        panel_plot.repaint();
    }
    
    private int getColumnByName(JTable table, String name) {
        for (int i = 0; i < table.getColumnCount(); ++i)
            if (table.getColumnName(i).equalsIgnoreCase(name))
                return i;
        return -1;
    }
    
    private void inicia(){
//        D1Dplot_global.initPars();

        //HO FEM CABRE
        mainFrame.setSize(D1Dplot_main.getDef_Width(), D1Dplot_main.getDef_Height()); //ho centra el metode main
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        while(mainFrame.getWidth()>screenSize.width){
            mainFrame.setSize(mainFrame.getWidth()-100, mainFrame.getHeight());
        }
        while(mainFrame.getHeight()>screenSize.height){
            mainFrame.setSize(mainFrame.getWidth(), mainFrame.getHeight()-100);
        }

        //split and divider loc
        Dimension minimumSize = new Dimension(0, 0);
        tabbedPanel_bottom.setMinimumSize(minimumSize);
        tabbedPanel_bottom.setPreferredSize(new Dimension(900,20));
        splitPane.resetToPreferredSizes();

        
        FileUtils.setLocale();
        this.tAOut.setMidaLletra(tAoutFsize);
        tAOut.stat(D1Dplot_global.welcomeMSG);

        if(D1Dplot_global.getConfigFileReaded()==null){
            tAOut.stat(String.format("No config file found on: %s, it will be created on exit!",D1Dplot_global.configFilePath));
        }else{
            if(D1Dplot_global.getConfigFileReaded()==true){
                tAOut.stat(String.format("Config file readed: %s",D1Dplot_global.configFilePath));    
            }else{
                tAOut.stat(String.format("Error reading config file: %s",D1Dplot_global.configFilePath));
            }
        }

        
        this.txtXtitle.setText(panel_plot.getXlabel());
        this.txtYtitle.setText(panel_plot.getYlabel());
    
        
        //el tema
//        if (D1Dplot_global.isLightTheme()){
//            applyLightTheme();
//        }else{
//            applyDarkTheme();
//        }
        
        //columnes mides
        
        PatternsTableModel model = (PatternsTableModel) table_files.getModel();
        for (int i=0; i<table_files.getColumnCount(); i++){
            table_files.getColumnModel().getColumn(i).setPreferredWidth(model.getColumnPrefSize(i));
            table_files.getColumnModel().getColumn(i).setMaxWidth(model.getColumnMaxSize(i));
            table_files.getColumnModel().getColumn(i).setMinWidth(model.getColumnMinSize(i));
            log.debug("columna "+i+" Min="+model.getColumnMinSize(i)+" Max="+model.getColumnMaxSize(i)+" pref="+model.getColumnPrefSize(i));
        }

        for (int i=0; i<table_files.getColumnCount(); i++){
            log.debug("col="+i+" Min="+table_files.getColumnModel().getColumn(i).getMinWidth()+" Max="+table_files.getColumnModel().getColumn(i).getMaxWidth()+" pref="+table_files.getColumnModel().getColumn(i).getPreferredWidth());
        }

        //botons taula
        buttonAdd.setPreferredSize(new Dimension(40,28));
        buttonRemove.setPreferredSize(new Dimension(40,28));
        buttonUp.setPreferredSize(new Dimension(40,28));
        buttonDown.setPreferredSize(new Dimension(40,28));
        buttonAdd.setMaximumSize(new Dimension(45,28));
        buttonRemove.setMaximumSize(new Dimension(45,28));
        buttonUp.setMaximumSize(new Dimension(45,28));
        buttonDown.setMaximumSize(new Dimension(45,28));
        buttonAdd.setMinimumSize(new Dimension(40,28));
        buttonRemove.setMinimumSize(new Dimension(40,28));
        buttonUp.setMinimumSize(new Dimension(40,28));
        buttonDown.setMinimumSize(new Dimension(40,28));
        buttonAdd.setMargin(new Insets(2, 2, 2, 2));
        buttonRemove.setMargin(new Insets(2, 2, 2, 2));
        buttonUp.setMargin(new Insets(2, 2, 2, 2));
        buttonDown.setMargin(new Insets(2, 2, 2, 2));
        
    }
    
    private String getWorkdir(){
        return D1Dplot_global.workdir;
    }
    
//    public void reset(){
//        //TODO
//    }
    
    public void openDataFile(){
        log.debug("openDataFile entered");
        FileNameExtensionFilter filt[] = DataFileUtils.getExtensionFilterRead();
        File datFile = FileUtils.fchooser(mainFrame,new File(getWorkdir()), filt, filt.length-1, false, false);
        if (datFile == null){
            this.tAOut.stat("No data file selected");
            return;
        }
        readDataFile(datFile);
    }
    
    //creem aquest per l'argument launcher
    public void readDataFile(File datfile){
        Pattern1D patt = DataFileUtils.readPatternFile(datfile);
        panel_plot.getPatterns().add(patt);
        this.updateData();
        panel_plot.fitGraph();
        log.debug("openDataFile exited");
        D1Dplot_global.setWorkdir(datfile);
        table_files.setRowSelectionInterval(table_files.getRowCount()-1, table_files.getRowCount()-1);
    }
    
    private void updateData(){
        log.debug("updateData entered");
        //TODO
        //update table
        updateTable();
        //update pattern list somewhere
        //update plotpanel series
        //replot
        panel_plot.repaint();
        mainFrame.repaint();
        log.debug("updateData exited");

    }
    //nP, nS, Filename, Color, Scale, ZerOff, Wavel, Marker, Line, ErrBars, Show,
    private void addRowToTable(int nP, int nS, String fname, Color c, float scale, double zoff, double wavel, String xunits, double yoffset, float markersize, float linewidth, boolean errbars, boolean show){
        DefaultTableModel model = (DefaultTableModel) table_files.getModel();
        Object[] row = {nP,nS,fname,c,scale,zoff,wavel,xunits,yoffset,markersize,linewidth,errbars,show};
        model.addRow(row);
    }
        
    
    private void updateTable(){
        DefaultTableModel model = (DefaultTableModel) table_files.getModel();
        model.setRowCount(0);
        
        Iterator<Pattern1D> itrP = panel_plot.getPatterns().iterator();
        while (itrP.hasNext()){
            Pattern1D p = itrP.next();
            int numP = panel_plot.getPatterns().indexOf(p);
            Iterator<DataSerie> itrD = p.getSeries().iterator();
            while (itrD.hasNext()){
                DataSerie d = itrD.next();
                int numD = p.getSeries().indexOf(d);
                
                String fnam = p.getFile().getName()+" ("+d.getTipusSerie().toString()+")";
                log.debug(fnam);
                log.debug(d.getColor().toString());
                log.debug(Float.toString(d.getScale()));
                log.debug(Double.toString(d.getZerrOff()));
                log.debug(Double.toString(d.getWavelength()));
                log.debug(Float.toString(d.getMarkerSize()));
                log.debug(Float.toString(d.getLineWidth()));
                log.debug(Boolean.toString(d.isShowErrBars()));
                log.debug(Boolean.toString(d.isPlotThis()));
                this.addRowToTable(numP, numD, fnam, d.getColor(), d.getScale(), d.getZerrOff(), d.getWavelength(), d.getxUnits().getName(), d.getYOff(), d.getMarkerSize(), d.getLineWidth(), d.isShowErrBars(), d.isPlotThis());
            }
        }

    }
    
    protected void updateLabelX() {
        if (this.panel_plot==null)return;
        if (this.txtXtitle.getText()!=null){
            this.panel_plot.setXlabel(this.txtXtitle.getText());    
        }
    }
    protected void updateLabelY() {
        if (this.panel_plot==null)return;
        if (this.txtYtitle.getText()!=null){
            this.panel_plot.setYlabel(this.txtYtitle.getText());    
        }
    }
    protected void do_mntmOpen_actionPerformed(ActionEvent e) {
        openDataFile();
    }
    
    
    protected void do_comboTheme_itemStateChanged(ItemEvent arg0) {
        if(arg0.getStateChange() == ItemEvent.DESELECTED)return;
        if (comboTheme.getSelectedItem().toString().equalsIgnoreCase("Light")){
            log.debug("light theme");
            D1Dplot_global.setLightTheme(true);
            panel_plot.setLightTheme(true);
        }else{
            log.debug("Dark theme");
            D1Dplot_global.setLightTheme(false);
            panel_plot.setLightTheme(false);
        }
        
        if (panel_plot.getPatterns().size()>0){
            boolean repaint = FileUtils.YesNoDialog(mainFrame, "Repaint current patterns?");
            if(repaint){
                reassignColorPatterns();
            }
        }
        panel_plot.repaint();
    }
    
    public void reassignColorPatterns(){
        int n=0;
        Iterator<Pattern1D> itrP = panel_plot.getPatterns().iterator();
        while (itrP.hasNext()){
            Pattern1D p = itrP.next();
            Iterator<DataSerie> itrds = p.getSeries().iterator();
            while (itrds.hasNext()){
                DataSerie ds = itrds.next();
                if (ds.getTipusSerie()==DataSerie.serieType.dat){
                    if (D1Dplot_global.isLightTheme()){
                        int ncol = n%D1Dplot_global.lightColors.length;
                        ds.setColor(D1Dplot_global.parseColorName(D1Dplot_global.lightColors[ncol]));
                    }else{
                        int ncol = n%D1Dplot_global.DarkColors.length;
                        ds.setColor(D1Dplot_global.parseColorName(D1Dplot_global.DarkColors[ncol]));
                    }    
                    n = n+1;
                }
            }            
        }
    }
    
    protected void do_buttonAdd_actionPerformed(ActionEvent e) {
        openDataFile();
    }
    protected void do_mntmClose_actionPerformed(ActionEvent e) {
        closeDataFile();
    }
    
    private void closeDataFile(){
        if (table_files.getSelectedRow()<0){
            log.info("First, select row(s)");
            return;
        }
        if (table_files.getRowCount()<=0)return;
        
        int[] selRows = table_files.getSelectedRows();
        log.debug("number of rows selected ="+ selRows.length);
        log.debug("selrows[0] ="+ selRows[0]);
        ArrayList<DataSerie> toDelete = new ArrayList<DataSerie>();
        
        for (int i=0; i<=selRows.length-1;i++){
            int selRow = selRows[i];
            int npat = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
            int nser = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nS.toString()));
            
            toDelete.add(panel_plot.getPatterns().get(npat).getSeries().get(nser));

        }
        
        //now delete
        Iterator<DataSerie> itrds = toDelete.iterator();
        while (itrds.hasNext()){
            DataSerie ds = itrds.next();
            ds.getPatt1D().removeDataSerie(ds);
            if(ds.getPatt1D().getNseriesPattern()==0){
                panel_plot.getPatterns().remove(ds.getPatt1D());
            }
        }
        updateData();
        return;
    }

    protected void do_buttonRemove_actionPerformed(ActionEvent e) {
        closeDataFile();
    }
    
    //IT MOVES PATTERNS ONLY
    protected void do_buttonUp_actionPerformed(ActionEvent e) {
        if (table_files.getSelectedRow()<0){
            log.info("First, select row(s)");
            return;
        }
        if (table_files.getRowCount()<=0)return;
        
        int[] selRows = table_files.getSelectedRows();
        log.debug("number of rows selected ="+ selRows.length);
        log.debug("selrows[0] ="+ selRows[0]);
        for (int i=0; i<=selRows.length-1;i++){
            int selRow = selRows[i];
            int npat = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
            log.debug("selRow="+selRow+" npat="+npat);
            try{
                Collections.swap(panel_plot.getPatterns(), npat, npat-1);    
            }catch(Exception ex){
                log.debug("index not existing, it only moves patterns, not series");
            }
        }
        updateData();
        return;
        
    }
    protected void do_buttonDown_actionPerformed(ActionEvent e) {
        if (table_files.getSelectedRow()<0){
            log.info("First, select row(s)");
            return;
        }
        if (table_files.getRowCount()<=0)return;
        
        int[] selRows = table_files.getSelectedRows();
        log.debug("number of rows selected ="+ selRows.length);
        log.debug("selrows[0] ="+ selRows[0]);
        for (int i=0; i<=selRows.length-1;i++){
            int selRow = selRows[i];
            int npat = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
            try{
                Collections.swap(panel_plot.getPatterns(), npat, npat+1);    
            }catch(Exception ex){
                log.debug("index not existing, it only moves patterns, not series");
            }
        }
        updateData();
        return;
    }
    
    protected void do_mntmSaveAs_actionPerformed(ActionEvent arg0) {
        saveDataFile();
    }
    
    public void saveDataFile(){
        log.debug("saveDataFile entered");
        if (table_files.getRowCount()<=0)return;
        if (table_files.getSelectedRow()<0){
            log.info("Select on the table the pattern you want to save");
            return;
        }
        
        int[] selRows = table_files.getSelectedRows();
        if (selRows.length>1){
            log.info("Please, select only ONE row (pattern)");
            return;
        }

        int npat = (Integer) table_files.getValueAt(selRows[0], this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
        int nser = (Integer) table_files.getValueAt(selRows[0], this.getColumnByName(table_files, PatternsTableModel.columns.nS.toString()));
        
        FileNameExtensionFilter filt[] = DataFileUtils.getExtensionFilterWrite();
        File datFile = FileUtils.fchooser(mainFrame,new File(getWorkdir()), filt, 0, true, true);
        if (datFile == null){
            this.tAOut.stat("No data file selected");
            return;
        }
        
        datFile = DataFileUtils.writePatternFile(datFile, panel_plot.getPatterns().get(npat), nser, true);
        log.info(datFile.toString()+" written!");
        log.debug("saveDataFile exited");
    }
    
    protected void do_chckbxShowLegend_itemStateChanged(ItemEvent arg0) {
        panel_plot.setShowLegend(chckbxShowLegend.isSelected());
        panel_plot.repaint();
    }
    protected void do_txtLegendx_actionPerformed(ActionEvent e) {
        try{
            int lx = Integer.parseInt(txtLegendx.getText());
            panel_plot.setLegendX(lx);
            panel_plot.repaint();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    protected void do_txtLegendy_actionPerformed(ActionEvent e) {
        try{
            int ly = Integer.parseInt(txtLegendy.getText());
            panel_plot.setLegendY(ly);
            panel_plot.repaint();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    protected void do_chckbxAutopos_itemStateChanged(ItemEvent e) {
        panel_plot.setAutoPosLegend(chckbxAutopos.isSelected());
        panel_plot.repaint();        
        if (chckbxAutopos.isSelected()){
            txtLegendx.setEditable(false);
            txtLegendy.setEditable(false);
            //legend pos
            txtLegendx.setText(Integer.toString(panel_plot.getLegendX()));
            txtLegendy.setText(Integer.toString(panel_plot.getLegendY()));
        }else{
            txtLegendx.setEditable(true);
            txtLegendy.setEditable(true);
        }
    }
    protected void do_chckbxIntensityWithBackground_itemStateChanged(ItemEvent e) {
        Pattern1D.setPlotwithbkg(chckbxIntensityWithBackground.isSelected());
        panel_plot.repaint();
    }
    protected void do_chckbxHklLabels_itemStateChanged(ItemEvent arg0) {
        if (this.panel_plot==null)return;
        panel_plot.setHkllabels(chckbxHklLabels.isSelected());
//        panel_plot.repaint();
    }
    protected void do_chckbxShowGridLines_itemStateChanged(ItemEvent e) {
        if (this.panel_plot==null)return;
        panel_plot.setShowGrid(chckbxShowGridLines.isSelected());
        panel_plot.repaint();
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
    
    
    private void saveSVG(File fsvg){
        
        // Get a DOMImplementation.
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

        // Create an instance of org.w3c.dom.Document.
        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNS, "svg", null);

        // Create an instance of the SVG Generator.
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

        // Ask the test to render into the SVG Graphics2D implementation.
        //            TestSVGGen test = new TestSVGGen();
        //            test.paint(svgGenerator);

        panel_plot.getGraphPanel().paintComponent(svgGenerator);

        // Finally, stream out SVG to the standard output using
        // UTF-8 encoding.
        boolean useCSS = true; // we want to use CSS style attributes
        try {
            //                Writer out = new OutputStreamWriter(System.out, "UTF-8");
            //                svgGenerator.stream(out, useCSS);

            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fsvg,true)));
            svgGenerator.stream(out, useCSS);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
    private void savePNG(File fpng, float factor){
//        BufferedImage bi = new BufferedImage(FastMath.round(panel_plot.getGraphPanel().getSize().width*factor),FastMath.round(panel_plot.getGraphPanel().getSize().height*factor), BufferedImage.TYPE_INT_ARGB); 
//        panel_plot.getGraphPanel().paintComponent(bi.createGraphics());
//        try{ImageIO.write(bi,"png",new File("test.png"));}catch (Exception e) {}

        double pageWidth = panel_plot.getGraphPanel().getSize().width*factor;
        double pageHeight = panel_plot.getGraphPanel().getSize().height*factor;
        double imageWidth = panel_plot.getGraphPanel().getSize().width;
        double imageHeight = panel_plot.getGraphPanel().getSize().height;

        double scaleFactor = DataFileUtils.getScaleFactorToFit(
                new Dimension((int) Math.round(imageWidth), (int) Math.round(imageHeight)),
                new Dimension((int) Math.round(pageWidth), (int) Math.round(pageHeight)));

        int width = (int) Math.round(pageWidth);
        int height = (int) Math.round(pageHeight);

        BufferedImage img = new BufferedImage(
                width,
                height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
//        g2d.setColor(Color.WHITE);
//        g2d.fill(new Rectangle2D.Double(0, 0, img.getWidth(), img.getHeight()));
        g2d.scale(scaleFactor, scaleFactor);
//        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
//        g2d.fillRect(0, 0, width, height);
//        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        
//        Ticket ticket = new Ticket();
        panel_plot.getGraphPanel().paintComponent(g2d);
//        ticket.paint(g2d, img.getWidth() / scaleFactor, (img.getHeight() / scaleFactor) / 4, 1);
        g2d.dispose();

        try {
            ImageIO.write(img, "png", fpng);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        log.debug(fpng.toString()+" written");
    }

    protected void do_mntmCloseAll_actionPerformed(ActionEvent e) {
        //TODO
    }
    

    
    
    protected void do_mntmExportAsPng_actionPerformed(ActionEvent e) {
        File fpng = FileUtils.fchooserSaveNoAsk(mainFrame, new File(D1Dplot_global.getWorkdir()), null); //ja preguntem despres
        if (fpng!=null){
            fpng = FileUtils.canviExtensio(fpng, "png");
            if (fpng.exists()){
                int actionDialog = JOptionPane.showConfirmDialog(mainFrame,
                        "Replace existing file?");
                if (actionDialog == JOptionPane.NO_OPTION)return;
            }
            int w = panel_plot.getGraphPanel().getSize().width;
            int h = panel_plot.getGraphPanel().getSize().height;
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
                    ex.printStackTrace();
                }
                log.writeNameNumPairs("config", true, "factor", factor);
                this.savePNG(fpng,factor);
            }
        }
    }
    
    protected void do_mntmExportAsSvg_actionPerformed(ActionEvent e) {
        File fsvg = FileUtils.fchooserSaveNoAsk(mainFrame, new File(D1Dplot_global.getWorkdir()), null);
        if (fsvg!=null){
            fsvg = FileUtils.canviExtensio(fsvg, "svg");
            if (fsvg.exists()){
                int actionDialog = JOptionPane.showConfirmDialog(mainFrame,
                        "Replace existing file?");
                if (actionDialog == JOptionPane.NO_OPTION)return;
            }
            this.saveSVG(fsvg);
        }
    }
    protected void do_chckbxShowNegativeLabels_itemStateChanged(ItemEvent arg0) {
        if (this.panel_plot==null)return;
        panel_plot.setNegativeYAxisLabels(chckbxShowNegativeLabels.isSelected());
        panel_plot.repaint();
    }
    protected void do_chckbxVerticalYLabel_itemStateChanged(ItemEvent e) {
        if (this.panel_plot==null)return;
        PlotPanel.setVerticalYlabel(chckbxVerticalYLabel.isSelected());
        panel_plot.repaint();
    }
    
    protected void do_mntmFindPeaks_actionPerformed(ActionEvent e) {
        if (table_files.getRowCount()<=0)return;
        if (table_files.getSelectedRow()<0){
            log.info("select a pattern on the table first");
            return;
        }
      //prova amb selected rows:
        int[] selRows = table_files.getSelectedRows();
        log.debug("number of rows selected ="+ selRows.length);
        if (selRows.length==0)return;
        for (int i=0; i<selRows.length;i++){
            int indexP = (Integer) table_files.getModel().getValueAt(i, this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
            int indexDS = (Integer) table_files.getModel().getValueAt(i, this.getColumnByName(table_files, PatternsTableModel.columns.nS.toString()));
            panel_plot.getPatterns().get(indexP).getSeries().get(indexDS).findPeaksEvenBetter(1.0f);    
        }
    }
    protected void do_mntmFindPeaks_1_actionPerformed(ActionEvent e) {
        
    }
    protected void do_mntmCalcBackground_actionPerformed(ActionEvent e) {
        // TODO tanquem altres dialegs?
        if (bkgDiag == null) {
            bkgDiag = new Background_dialog(this.panel_plot);
        }
        bkgDiag.setVisible(true);
//        panelImatge.setDBdialog(dbDialog);
    }

    public FindPeaks_dialog getFindPksDiag() {
        return FindPksDiag;
    }

    public void setFindPksDiag(FindPeaks_dialog findPksDiag) {
        FindPksDiag = findPksDiag;
    }

    public Background_dialog getBkgDiag() {
        return bkgDiag;
    }

    public void setBkgDiag(Background_dialog bkgDiag) {
        this.bkgDiag = bkgDiag;
    }
}

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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;

import com.vava33.cellsymm.CellSymm_global;
import com.vava33.d1dplot.auxi.ArgumentLauncher;
import com.vava33.d1dplot.auxi.ColorEditor;
import com.vava33.d1dplot.auxi.ColorRenderer;
import com.vava33.d1dplot.auxi.DataFileUtils;
import com.vava33.d1dplot.auxi.PattOps;
import com.vava33.d1dplot.auxi.PatternsTableCellRenderer;
import com.vava33.d1dplot.auxi.PatternsTableModel;
import com.vava33.d1dplot.auxi.DataFileUtils.SupportedReadExtensions;
import com.vava33.d1dplot.auxi.DataFileUtils.SupportedWriteExtensions;
import com.vava33.d1dplot.data.DataPoint;
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.d1dplot.data.Data_Common;
import com.vava33.d1dplot.data.Plottable;
import com.vava33.d1dplot.data.Plottable_point;
import com.vava33.d1dplot.data.SerieType;
import com.vava33.d1dplot.data.Xunits;
import com.vava33.jutils.FileUtils;
import com.vava33.jutils.LogJTextArea;
import com.vava33.jutils.VavaLogger;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.KeyAdapter;

public class D1Dplot_main {

    private static final String className = "d1Dplot_main";
    private static VavaLogger log;

    private float tAoutFsize = 12.0f;
    private static int def_Width=1024;
    private static int def_Height=768;
    private static String LandF = "system";

    private BackgroundDialog bkgDiag;
    private FindPeaksDialog FindPksDiag;
    private SubtractDialog subDiag;
    private Database DBDiag;
    private Plot2DPanel p2;
    private AboutDialog aboutDiag;
    private boolean customXtitle = false;
    private boolean firstTime = true;

    private JFrame mainFrame;
    private JTable table_files;
    private LogJTextArea tAOut;
    private PlotPanel panel_plot;
    private JTextField txtXtitle;
    private JTextField txtYtitle;
    private JCheckBox chckbxShowLegend;
    private JComboBox<String> comboTheme;
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
    private JScrollPane scrollPane_2;
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
    private JCheckBox chckbxShowGridY;
    private JMenu mnPlot;
    private JMenuItem mntmExportAsPng;
    private JMenuItem mntmExportAsSvg;
    private JSeparator separator_3;
    private JCheckBox chckbxShowNegativeLabels;
    private JCheckBox chckbxVerticalYLabel;
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
    private JCheckBox chckbxShowGridX;
    private JCheckBox chckbxVerticalYAxis;
    private JMenuItem mntmCheckForUpdates;
    private JButton btnReassignColors;
    private JMenuItem mntmDB;
    private JCheckBox chckbxPngTransp;
    private JSeparator separator_4;
    private JMenuItem mntmSaveProject;
    private JMenuItem mntmOpenProject;
    private JMenuItem mntmDebug;
    private JButton btnDupli;
    private JCheckBox chckbxPartialYScale;
    private JLabel lblTini;
    private JLabel lblFactor;
    private JTextField txtTini;
    private JTextField txtFactor;
    private JSeparator separator_5;
    private JMenuItem mntmSaveProfile;

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
            if (D1Dplot_global.isDebug())e.printStackTrace();
            log.warning("Error initializing System look and feel");
        }


        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    D1Dplot_main frame = new D1Dplot_main();
                    D1Dplot_global.setMainFrame(frame); //this is NEW març 2019
                    D1Dplot_global.printAllOptions("config",frame.getPanel_plot().createOptionsObject());
                    ArgumentLauncher.readArguments(frame, args);
                    if (ArgumentLauncher.isLaunchGraphics()){
                        frame.showMainFrame();
                    }else{
                        System.exit(0);
                    }
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
        mainFrame.setVisible(true);    
    }
    public void disposeMainFrame(){
        mainFrame.dispose();
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
        mainFrame.setIconImage(D1Dplot_global.getIcon());
        mainFrame.setBounds(100, 100, 1024, 768);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.getContentPane().setLayout(new MigLayout("insets 3", "[grow]", "[grow]"));

        splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.82);
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

        JMenuItem sumPatts = new JMenuItem("Sum selected patterns");
        sumPatts.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sumPatterns(e);
            }
        });
        popupMenu.add(sumPatts);

        table_files = new JTable(new PatternsTableModel());
        table_files.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table_files.setColumnSelectionAllowed(true);
        table_files.setCellSelectionEnabled(true);

        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table_files);

        table_files.setDefaultRenderer(String.class, new PatternsTableCellRenderer());
        table_files.setDefaultRenderer(Integer.class, new PatternsTableCellRenderer());
        table_files.setDefaultRenderer(Float.class, new PatternsTableCellRenderer());
        table_files.setDefaultRenderer(Double.class, new PatternsTableCellRenderer());

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
        table_files.getColumn(PatternsTableModel.columns.Type.toString()).setCellEditor(new DefaultCellEditor(comboStypeTable));
        table_files.setComponentPopupMenu(popupMenu);

        table_files.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                aplicarselecciotaula(arg0);
            }
        });

        table_files.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                int index = table_files.getTableHeader().columnAtPoint(mouseEvent.getPoint());
                if (index >= 0) {
                    logdebug("Clicked on column " + index);
                    if (table_files.getRowCount()>0){
                        table_files.setRowSelectionInterval(0, table_files.getRowCount()-1);
                        table_files.setColumnSelectionInterval(index, index);
                    }
                }
            };
        });


        panel_DS = new JPanel();
        tabbedPanel_bottom.addTab("Data Series", null, panel_DS, null);
        panel_DS.setLayout(new MigLayout("insets 0", "[grow][][]", "[grow][][]"));

        new JLabel("New label");
        panel_DS.add(scrollPane, "cell 0 0 1 3,grow");

        btnReload = new JButton("R");
        btnReload.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_btnReload_actionPerformed(e);
            }
        });

        buttonRemove = new JButton("-");
        buttonRemove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_buttonRemove_actionPerformed(e);
            }
        });

        buttonDown = new JButton("v");
        buttonDown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_buttonDown_actionPerformed(e);
            }
        });
        panel_DS.add(buttonDown, "cell 1 1,growx,aligny top");
        buttonRemove.setPreferredSize(new Dimension(23, 28));
        panel_DS.add(buttonRemove, "cell 2 1,growx,aligny top");
        btnReload.setToolTipText("Reload selected pattern");
        panel_DS.add(btnReload, "cell 1 2,growx");

        buttonAdd = new JButton("+");
        buttonAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_buttonAdd_actionPerformed(e);
            }
        });
        panel_DS.add(buttonAdd, "cell 2 0,growx,aligny bottom");

        buttonUp = new JButton("^");
        buttonUp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_buttonUp_actionPerformed(e);
            }
        });
        panel_DS.add(buttonUp, "cell 1 0,growx,aligny bottom");

        btnDupli = new JButton("D");
        btnDupli.setToolTipText("Duplicate selected pattern");
        btnDupli.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_buttonDupli_actionPerformed(e);
            }
        });
        panel_DS.add(btnDupli, "cell 2 2");


        scrollPane_1 = new JScrollPane();
        tabbedPanel_bottom.addTab("Plot Settings", null, scrollPane_1, null);

        JPanel panel = new JPanel();
        scrollPane_1.setViewportView(panel);
        panel.setBorder(null);
        panel.setLayout(new MigLayout("", "[][grow][][][][][][][][][][][][][][]", "[][][]"));

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
        panel.add(separator, "cell 4 0 1 3,growy");

        chckbxShowLegend = new JCheckBox("Legend");
        chckbxShowLegend.setSelected(true);
        chckbxShowLegend.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent arg0) {
                do_chckbxShowLegend_itemStateChanged(arg0);
            }
        });
        panel.add(chckbxShowLegend, "cell 5 0 2 1");

        separator_1 = new JSeparator();
        separator_1.setOrientation(SwingConstants.VERTICAL);
        panel.add(separator_1, "cell 7 0 1 3,growy");

        chckbxHklLabels = new JCheckBox("HKL labels");
        chckbxHklLabels.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent arg0) {
                do_chckbxHklLabels_itemStateChanged(arg0);
            }
        });

        chckbxPartialYScale = new JCheckBox("Partial Y Scale");
        chckbxPartialYScale.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                do_chckbxPartialYScale_itemStateChanged(e);
            }
        });
        panel.add(chckbxPartialYScale, "cell 8 0 2 1");

        separator_5 = new JSeparator();
        separator_5.setOrientation(SwingConstants.VERTICAL);
        panel.add(separator_5, "cell 10 0 1 3,grow");
        chckbxHklLabels.setSelected(true);
        panel.add(chckbxHklLabels, "cell 11 0");

        separator_3 = new JSeparator();
        separator_3.setOrientation(SwingConstants.VERTICAL);
        panel.add(separator_3, "cell 12 0 1 3,growy");

        chckbxShowGridY = new JCheckBox("Grid Y");
        chckbxShowGridY.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                do_chckbxShowGridLines_itemStateChanged(e);
            }
        });
        panel.add(chckbxShowGridY, "flowx,cell 13 0");

        separator_4 = new JSeparator();
        panel.add(separator_4, "cell 14 0 1 3,growy");

        JLabel lblYTitle = new JLabel("Y title");
        panel.add(lblYTitle, "cell 0 1,alignx trailing");

        txtYtitle = new JTextField();
        txtYtitle.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtYtitle_actionPerformed(e);
            }
        });
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
        panel.add(txtYtitle, "cell 1 1 3 1,growx");
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
        panel.add(chckbxAutopos, "cell 5 1 2 1");

        lblTini = new JLabel("T2ini=");
        panel.add(lblTini, "cell 8 1,alignx trailing");

        txtTini = new JTextField();
        txtTini.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                do_txtTini_keyReleased(e);
            }
        });
        txtTini.setText("30");
        panel.add(txtTini, "cell 9 1,growx");
        txtTini.setColumns(6);
        panel.add(chckbxIntensityWithBackground, "cell 11 1");

        chckbxShowNegativeLabels = new JCheckBox("Negative Yaxis labels");
        chckbxShowNegativeLabels.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent arg0) {
                do_chckbxShowNegativeLabels_itemStateChanged(arg0);
            }
        });

        lblFactor = new JLabel("factor=");
        panel.add(lblFactor, "cell 8 2,alignx trailing");

        txtFactor = new JTextField();
        txtFactor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                do_txtFactor_keyReleased(e);
            }
        });
        txtFactor.setText("1.0");
        panel.add(txtFactor, "cell 9 2,growx");
        txtFactor.setColumns(6);
        panel.add(chckbxShowNegativeLabels, "cell 15 2");

        chckbxShowGridX = new JCheckBox("Grid X");
        chckbxShowGridX.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent arg0) {
                do_chckbxShowGridX_itemStateChanged(arg0);
            }
        });
        panel.add(chckbxShowGridX, "cell 13 1");

        JLabel lbltheme = new JLabel("Theme");
        panel.add(lbltheme, "cell 0 2,alignx trailing");

        comboTheme = new JComboBox<String>();
        comboTheme.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent arg0) {
                do_comboTheme_itemStateChanged(arg0);
            }
        });
        comboTheme.setModel(new DefaultComboBoxModel<String>(new String[] {"Light", "Dark"}));
        panel.add(comboTheme, "cell 1 2,growx,aligny top");

        txtLegendx = new JTextField();
        txtLegendx.setEditable(false);
        txtLegendx.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtLegendx_actionPerformed(e);
            }
        });

        btnReassignColors = new JButton("Reassign colors");
        btnReassignColors.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_btnReassignColors_actionPerformed(e);
            }
        });
        panel.add(btnReassignColors, "cell 2 2");

        chckbxPngTransp = new JCheckBox("PNG transp bkg");
        chckbxPngTransp.setSelected(true);
        panel.add(chckbxPngTransp, "cell 3 2");
        txtLegendx.setText("legendX");
        panel.add(txtLegendx, "cell 5 2,growx");
        txtLegendx.setColumns(5);

        txtLegendy = new JTextField();
        txtLegendy.setEditable(false);
        txtLegendy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtLegendy_actionPerformed(e);
            }
        });
        txtLegendy.setText("legendY");
        panel.add(txtLegendy, "cell 6 2,growx");
        txtLegendy.setColumns(5);

        chckbxVerticalYLabel = new JCheckBox("Vertical Y label");
        chckbxVerticalYLabel.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                do_chckbxVerticalYLabel_itemStateChanged(e);
            }
        });
        panel.add(chckbxVerticalYLabel, "cell 15 1");

        chckbxVerticalYAxis = new JCheckBox("Vertical Y axis");
        chckbxVerticalYAxis.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent arg0) {
                do_chckbxVerticalYAxis_itemStateChanged(arg0);
            }
        });
        chckbxVerticalYAxis.setSelected(true);
        panel.add(chckbxVerticalYAxis, "cell 15 0");


        scrollPane_2 = new JScrollPane();
        tabbedPanel_bottom.addTab("Log", null, scrollPane_2, null);
        tAOut = new LogJTextArea();
        scrollPane_2.setViewportView(tAOut);

        panel_plot = new PlotPanel(D1Dplot_global.getReadedOpt());
        splitPane.setLeftComponent(panel_plot.getPlotPanel());
        //        panel_plot = (PlotPanel) new JPanel();
        //        panel_plot.getGraphPanel().setBorder(new BevelBorder(EtchedBorder.LOWERED, null, null, null, null));

        menuBar = new JMenuBar();
        mainFrame.setJMenuBar(menuBar);

        mnFile = new JMenu("File");
        mnFile.setMnemonic('f');
        menuBar.add(mnFile);

        mntmOpen = new JMenuItem("Open Data File");
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

        mntmDB = new JMenuItem("Compound Database");
        mntmDB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmDB_actionPerformed(e);
            }
        });
        mnPlot.add(mntmDB);
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

        mntmDebug = new JMenuItem("debug");
        mntmDebug.setVisible(false);
        mntmDebug.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmDebug_actionPerformed(e);
            }
        });
        mnHelp.add(mntmDebug);

        inicia();
    }

    //=========================================================

    private void inicia(){

        if (D1Dplot_global.isLoggingTA())VavaLogger.setTArea(tAOut);

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

        this.tAOut.setMidaLletra(tAoutFsize);
        log.info(D1Dplot_global.welcomeMSG);

        if(D1Dplot_global.getConfigFileReaded()==null){
            log.info(String.format("No config file found on: %s, it will be created on exit!",D1Dplot_global.configFilePath));
        }else{
            if(D1Dplot_global.getConfigFileReaded()==true){
                log.info(String.format("Config file readed: %s",D1Dplot_global.configFilePath));    
            }else{
                log.warning(String.format("Error reading config file: %s",D1Dplot_global.configFilePath));
            }
        }

        //parametres per defecte que agafem de PlotPanel
        this.txtXtitle.setText(panel_plot.getXlabel());
        this.txtYtitle.setText(panel_plot.getYlabel());
        chckbxShowGridX.setSelected(panel_plot.isShowGridX());
        chckbxShowGridY.setSelected(panel_plot.isShowGridY());
        chckbxHklLabels.setSelected(panel_plot.isHkllabels());
        chckbxShowLegend.setSelected(panel_plot.isShowLegend());
        chckbxShowNegativeLabels.setSelected(panel_plot.isNegativeYAxisLabels());
        chckbxIntensityWithBackground.setSelected(panel_plot.isPlotwithbkg());
        chckbxAutopos.setSelected(panel_plot.isAutoPosLegend());


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

        //columnes mides

        PatternsTableModel model = (PatternsTableModel) table_files.getModel();
        for (int i=0; i<table_files.getColumnCount(); i++){
            table_files.getColumnModel().getColumn(i).setPreferredWidth(model.getColumnPrefSize(i));
            table_files.getColumnModel().getColumn(i).setMaxWidth(model.getColumnMaxSize(i));
            table_files.getColumnModel().getColumn(i).setMinWidth(model.getColumnMinSize(i));
            //	            logdebug("columna "+i+" Min="+model.getColumnMinSize(i)+" Max="+model.getColumnMaxSize(i)+" pref="+model.getColumnPrefSize(i));
        }

        if(D1Dplot_global.isDebug()) {
            for (int i=0; i<table_files.getColumnCount(); i++){
                log.fine("col="+i+" Min="+table_files.getColumnModel().getColumn(i).getMinWidth()+" Max="+table_files.getColumnModel().getColumn(i).getMaxWidth()+" pref="+table_files.getColumnModel().getColumn(i).getPreferredWidth());
            }	        	
        }

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

        //seleccionem el log panel as default
        tabbedPanel_bottom.setSelectedIndex(2);

    }

    private void openDataFile(){
        logdebug("openDataFile entered");
        FileNameExtensionFilter filt[] = DataFileUtils.getExtensionFilterRead();
        File[] datFile = FileUtils.fchooserMultiple(mainFrame, new File(getWorkdir()), filt,  filt.length-1, "Select PDD files to open");
        if (datFile == null){
            log.warning("No data file selected");
            return;
        }
        log.debug("file exists? "+datFile[0].exists());
        for (int i=0; i<datFile.length;i++){
            readDataFile(datFile[i]);    
        }
        logdebug("openDataFile exited");
        this.updateData(true,true);

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
    public Plottable readDataFile(File datfile){
        Plottable s = DataFileUtils.readPatternFile(datfile); 
        panel_plot.addPlottable(s);
        D1Dplot_global.setWorkdir(datfile);
        return s;
    }
    
    public Plottable readDataFile(File datfile, SupportedReadExtensions fmt){
        Plottable s = DataFileUtils.readPatternFile(datfile,fmt); 
        panel_plot.addPlottable(s);
        D1Dplot_global.setWorkdir(datfile);
        return s;
    }

    public void updateData(boolean fitgraph, boolean actualitzaPlot){
        logdebug("updateData entered");
        //check the current selection
        int selRow = -1;
        if (table_files.getRowCount()>0 && table_files.getSelectedRow()>=0){
            selRow = table_files.getSelectedRow();
        }

        updateTable();

        //seleccionem l'anterior o be la ultima afegida
        if (selRow >= 0 && table_files.getRowCount()>selRow){
            table_files.setRowSelectionInterval(selRow,selRow);
        }else{
            if (table_files.getRowCount()>0){
                table_files.setRowSelectionInterval(table_files.getRowCount()-1, table_files.getRowCount()-1);    
            }
        }

        if(fitgraph)panel_plot.fitGraph();
        if(actualitzaPlot)panel_plot.actualitzaPlot();
        logdebug("updateData exited");

    }

    protected void updateTable(){
        DefaultTableModel model = (DefaultTableModel) table_files.getModel();
        model.setRowCount(0);
        boolean first = true;
        for (int i=0;i<panel_plot.getNplottables();i++) {
            Plottable p = panel_plot.getPlottable(i);

            int np=panel_plot.indexOfPlottableData(p);
            for (DataSerie d: p.getDataSeries()) {
                if(d.isEmpty())continue;
                int nd = p.indexOfDS(d);
                if (first && !isCustomXtitle()){
                    //posem be les unitats a l'eix X?
                    if (d.getxUnits()==Xunits.tth){
                        panel_plot.setXlabel("2"+D1Dplot_global.theta+" (º)");
                        txtXtitle.setText("2"+D1Dplot_global.theta+" (º)");   
                    }else{
                        panel_plot.setXlabel(d.getxUnits().getName());
                        txtXtitle.setText(d.getxUnits().getName());
                    }
                    first=false;
                }
                String fnam = d.serieName;
                logdebug(fnam);
                logdebug(d.color.toString());
                logdebug(Float.toString(d.getScale()));
                logdebug(Double.toString(d.getZerrOff()));
                logdebug(Double.toString(d.getWavelength()));
                logdebug(Float.toString(d.markerSize));
                logdebug(Float.toString(d.lineWidth));
                logdebug(Boolean.toString(d.showErrBars));
                logdebug(Boolean.toString(d.plotThis));
                logdebug(d.getTipusSerie().name());
                this.addRowToTable(np, nd, fnam, d.color, d.getScale(), d.getZerrOff(), d.getWavelength(), d.getxUnits().getName(), d.getYOff(), d.markerSize, d.lineWidth, d.showErrBars, d.plotThis,d.getTipusSerie().name());
                nd++; //no caldria numerarles... o fins i tot es podria fixar segons el tipus (obligar plottable a donar num de serie)
            }
            np++;
        }
    }

    private void updateLabelX() {
        if (this.panel_plot==null)return;
        if (this.txtXtitle.getText()!=null){
            this.panel_plot.setXlabel(this.txtXtitle.getText());    
        }
    }

    private void updateLabelY() {
        if (this.panel_plot==null)return;
        if (this.txtYtitle.getText()!=null){
            this.panel_plot.setYlabel(this.txtYtitle.getText());    
        }
    }

    private void reassignColorPatterns(){ //nomes funiona per series tipus dat o gr
        panel_plot.reassignColorPatterns();
        this.updateData(false,false); //ja fa actualitza plot el reassign color
    }

    private void closeDataFile(){
        if (table_files.getSelectedRow()<0){
            log.warning("Select which pattern(s) to close by selecting the corresponding row(s)");
            return;
        }
        if (table_files.getRowCount()<=0)return;

        int[] selRows = table_files.getSelectedRows();
        logdebug("number of rows selected ="+ selRows.length);
        logdebug("selrows[0] ="+ selRows[0]);
        List<DataSerie> toDelete = new ArrayList<DataSerie>();

        for (int i=0; i<=selRows.length-1;i++){
            int selRow = selRows[i];
            int npat = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
            int nser = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nS.toString()));

            toDelete.add(panel_plot.getPlottable(npat).getDataSeries().get(nser));
        }

        Iterator<DataSerie> itrds = toDelete.iterator();
        while (itrds.hasNext()){
            DataSerie ds = itrds.next();
            ds.getParent().removeDataSerie(ds);
            if (ds.getParent().getNSeries()==0) panel_plot.removePlottable(ds.getParent());
        }
        updateData(false,false);//el remove ja actualiza el plot
        return;

    }

    private void saveDataProf() {
        if (table_files.getRowCount()<=0)return;

        //PRIMER INTENTEM FER-HO AUTOMATIC SI TOT ÉS CORRECTE
        List<DataSerie> dsOBS = this.getPanel_plot().getFirstSelectedPlottable().getDataSeriesByType(SerieType.dat);
        if (dsOBS.isEmpty()) dsOBS = this.getPanel_plot().getFirstSelectedPlottable().getDataSeriesByType(SerieType.obs);
        List<DataSerie> dsCAL = this.getPanel_plot().getFirstSelectedPlottable().getDataSeriesByType(SerieType.cal);
        List<DataSerie> dsHKL = this.getPanel_plot().getFirstSelectedPlottable().getDataSeriesByType(SerieType.hkl);

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
            for(int i=0;i<this.getPanel_plot().getNplottables();i++) {
                dsOBS.addAll(this.getPanel_plot().getPlottable(i).getDataSeriesByType(SerieType.dat));
                dsOBS.addAll(this.getPanel_plot().getPlottable(i).getDataSeriesByType(SerieType.obs));
                dsCAL.addAll(this.getPanel_plot().getPlottable(i).getDataSeriesByType(SerieType.cal));
                dsHKL.addAll(this.getPanel_plot().getPlottable(i).getDataSeriesByType(SerieType.hkl));
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
                SavePRFdialog prfdiag = new SavePRFdialog(this.getPanel_plot());
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
            filt[0] = new FileNameExtensionFilter("d1Dplot project","d1p");
            File f = FileUtils.fchooserSaveAsk(this.mainFrame, D1Dplot_global.getWorkdirFile(), filt, "d1p", "Save obs/calc/hkl matching");
            if (f!=null) {
                DataFileUtils.writeProfileFile(f, dsOBS.get(0),dsCAL.get(0),dsHKL, true);
            }
        }else {
            log.info("error saving obs/calc/hkl matching");
        }
    }

    private void saveDataFile(){
        logdebug("saveDataFile entered");
        if (table_files.getRowCount()<=0)return;

        List<DataSerie> dss = panel_plot.getSelectedSeries();

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

            datFile = DataFileUtils.writePatternFile(datFile, dss.get(0), true, panel_plot.isPlotwithbkg());
            log.info(datFile.toString()+" written!");
        }

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

            String ext = format.toString();
            int i = 0;
            boolean nameGiven = false;
            String baseName = "";
            for (DataSerie ds: dss) {
                String fname = "";
                if (ds.getParent().getFile()==null) {
                    //ask for a name
                    if (!nameGiven) {
                        fname = FileUtils.DialogAskForString(this.getMainFrame(),"BaseFileName=", "Enter filename", ds.serieName);
                        if (fname==null) {
                            fname = ds.serieName;
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
                File f = new File(dir+D1Dplot_global.fileSeparator+fname);
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
                f = DataFileUtils.writePatternFile(f, ds, owrite, panel_plot.isPlotwithbkg()); 
                log.info(f.toString()+" written!");
                i++;
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
//        panel_plot.getGraphPanel().paintComponent(svgGenerator);
        panel_plot.getGraphPanel().pinta(svgGenerator,1);

        // Finally, stream out SVG to the standard output using
        // UTF-8 encoding.
        boolean useCSS = true; // we want to use CSS style attributes
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fsvg,true)));
            svgGenerator.stream(out, useCSS);

        } catch (Exception e) {
            if (D1Dplot_global.isDebug())e.printStackTrace();
        }

    }

    private void savePNG(File fpng, float factor){
        if (!panel_plot.arePlottables())return;
        try {
            ImageIO.write(panel_plot.getGraphPanel().pintaPatterns(factor), "png", fpng);
        } catch (Exception ex) {
            if (D1Dplot_global.isDebug())ex.printStackTrace();
            log.warning(fpng.toString()+" error writting png file");
            return;
        }
        log.info(fpng.toString()+" written!");
    }

    private void sumPatterns(ActionEvent e){
        if (table_files.getSelectedRow()<0)return;
        if (table_files.getRowCount()<=0)return;

        int[] selRows = table_files.getSelectedRows();
        logdebug("number of rows selected ="+ selRows.length);
        log.writeNameNums("CONFIG", true, "selRows", selRows);

        StringBuilder sb = new StringBuilder();
        StringBuilder sbNames = new StringBuilder();

        DataSerie[] dss = new DataSerie[selRows.length];
        for (int i=0; i<=selRows.length-1;i++){
            int selRow = selRows[i];
            //primer agafem el pattern i serie seleccionades
            int pattern = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
            int serie = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nS.toString()));
            dss[i] = panel_plot.getPlottable(pattern).getDataSeries().get(serie);
            sb.append(String.format("P%dS%d ", pattern,serie));
            sbNames.append(dss[i].serieName+" ");
        }

        //comprovar punts, sino rebinning de les series que faci falta, la primera serie mana
        for (int i=1; i<dss.length; i++){
            boolean coin = PattOps.haveCoincidentPointsDS(dss[0], dss[i]);
            if (!coin){
                dss[i] = PattOps.rebinDS(dss[0], dss[i]);
            }
        }

        //sumem
        DataSerie suma = PattOps.addDataSeriesCoincidentPoints(dss);
        suma.serieName = String.format("Sum of %s",sb.toString().trim());
        Data_Common patt = new Data_Common(panel_plot.getFirstSelectedSerie().getWavelength());
        patt.addCommentLines(dss[0].getCommentLines()); //comments of 1st serie
        patt.addCommentLine("#Sum of: "+sbNames.toString().trim());
        patt.setOriginalWavelength(dss[0].getOriginalWavelength());
        patt.addDataSerie(suma);
        panel_plot.addPlottable(patt);
        this.updateData(false,false);

    }

    private void changeXunits(ActionEvent e){
        if (table_files.getSelectedRow()<0)return;
        if (table_files.getRowCount()<=0)return;

        String[] comboValues = new String[Xunits.values().length];
        int i = 0;
        for (Xunits a :Xunits.values()){
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

            Xunits destUnits = null;
            if (Xunits.getEnum(s)!=null){
                destUnits=Xunits.getEnum(s);
            }
            if (destUnits==null){
                log.warning("Choose a valid x-units value");
                return;
            }

            if (destUnits==Xunits.G){
                log.warning("Conversion to G not supported");
                return;
            }

            int[] selRows = table_files.getSelectedRows();
            logdebug("number of rows selected ="+ selRows.length);
            log.writeNameNums("CONFIG", true, "selRows", selRows);

            for (i=0; i<=selRows.length-1;i++){
                int selRow = selRows[i];
                //primer agafem el pattern i serie seleccionades
                int pattern = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
                int serie = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nS.toString()));

                Plottable patt = panel_plot.getPlottable(pattern);
                DataSerie ds = patt.getDataSeries().get(serie);

                if (ds.getxUnits()==Xunits.G){
                    log.warning("Conversion from G not supported");
                    return;
                }

                if (ds.getWavelength()<0){
                    log.warning(String.format("Pattern %d serie %d has no wavelength assigned, skipping", pattern,serie));
                    continue;
                }
                logdebug(String.format("Pattern=%d Serie=%d", pattern,serie));
                logdebug(String.format("SerieUnits=%s DestUnits=%s", ds.getxUnits().getName(), destUnits.getName()));

                //CONVERTIM
                log.info(String.format("Conversion from %s to %s",ds.getxUnits(), destUnits));
                ds.convertDStoXunits(destUnits);
            }
            this.updateData(false,true); //cal update panel_plot perque hem modificat una dataserie ja afegida
        }
    }

    private void convertToWL(ActionEvent e){
        if (table_files.getSelectedRow()<0)return;
        if (table_files.getRowCount()<=0)return;

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
                logdebug("Error parsing wavelength");
                return;
            }
            if (newWL<0){
                log.warning("Invalid wavelength entered");
                return;
            }

            int[] selRows = table_files.getSelectedRows();
            logdebug("number of rows selected ="+ selRows.length);

            for (int i=0; i<=selRows.length-1;i++){
                int selRow = selRows[i];
                //primer agafem el pattern i serie seleccionades
                int pattern = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
                int serie = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nS.toString()));

                Plottable patt = panel_plot.getPlottable(pattern);
                DataSerie ds = patt.getDataSeries().get(serie);

                if (ds.getWavelength()<0){
                    log.warning(String.format("Pattern %d serie %d has no wavelength assigned, skipping", pattern,serie));
                    continue;
                }

                ds.convertDStoWavelength(newWL);

            }
            this.updateData(false,true);
        }




    }

    private void editMultipleValuesTable(ActionEvent e){

        if (table_files.getSelectedRow()<0)return;
        if (table_files.getRowCount()<=0)return;

        int[] selRows = table_files.getSelectedRows();
        int selCol = table_files.getSelectedColumn();
        PatternsTableModel.columns colName = FileUtils.searchEnum(PatternsTableModel.columns.class,table_files.getColumnName(selCol));

        Color newColor = null;
        String s = null;
        if (colName == PatternsTableModel.columns.Color){
            newColor = JColorChooser.showDialog(
                    mainFrame,
                    "Choose Color",
                    Color.BLACK);
            s = "changing color column";
            if(newColor == null){
                log.warning("Select a valid color");
                return;
            }
        }else{
            s = (String)JOptionPane.showInputDialog(
                    mainFrame,
                    "New value=",
                    "Change values column-wise",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "");
        }


        //If a string was returned, say so.
        if ((s != null) && (s.length() > 0)) {
            //edit all the selected cells
            //prova amb selected rows:
            logdebug("number of rows selected ="+ selRows.length);
            logdebug("selrows[0] ="+ selRows[0]);
            logdebug("columnSelected ="+ selCol);
            for (int i=0; i<=selRows.length-1;i++){
                int selRow = selRows[i];
                logdebug("changing value of row="+selRow+" and col="+selCol+" to="+s);
                switch (colName){
                case Color:
                    table_files.setValueAt(newColor, selRow, selCol);
                    break;
                case ErrBars:
                    log.info("Column not editable");
                    break;
                case Filename:
                    table_files.setValueAt(s, selRow, selCol);
                    break;
                case Line:
                    table_files.setValueAt(Float.parseFloat(s), selRow, selCol);
                    break;
                case Marker:
                    table_files.setValueAt(Float.parseFloat(s), selRow, selCol);
                    break;
                case Scale:
                    table_files.setValueAt(Float.parseFloat(s), selRow, selCol);
                    break;
                case Show:
                    log.info("Column not editable");
                    break;
                case Wavel:
                    table_files.setValueAt(Double.parseDouble(s), selRow, selCol);
                    break;
                case XUnits:
                    log.info("Column not editable");
                    break;
                case YOffset:
                    table_files.setValueAt(Double.parseDouble(s), selRow, selCol);
                    break;
                case ZerOff:
                    table_files.setValueAt(Double.parseDouble(s), selRow, selCol);
                    break;
                case nP:
                    log.info("Column not editable");
                    break;
                case nS:
                    log.info("Column not editable");
                    break;
                case Type:
                    log.info("Column not editable");
                    break;
                default:
                    log.info("Column not identified");
                    break;
                }
            }
            return;
        }
    }

    private void aplicarselecciotaula(ListSelectionEvent arg0){
        logdebug("applicarSeleccioTaula entered");
        if (table_files.getSelectedRow()<0)return;
        if (table_files.getRowCount()<=0)return;
        //prova amb selected rows:
        int[] selRows = table_files.getSelectedRows();
        logdebug("number of rows selected ="+ selRows.length);
        if (selRows.length==0)return;
        panel_plot.getSelectedSeries().clear();
        for (int i=0; i<selRows.length;i++){
            int selRow = selRows[i];
            int indexP = (Integer) table_files.getModel().getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
            int indexDS = (Integer) table_files.getModel().getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nS.toString()));
            panel_plot.getSelectedSeries().add(panel_plot.getPlottable(indexP).getDataSeries().get(indexDS));    
            logdebug("s'sha seleccionat patt="+indexP+" serie="+indexDS);
        }
    }

    private void applicarModificacioTaula(int columna, int filaIni, int filaFin){
        logdebug("applicarModificacioTaula entered");
        if (table_files.getSelectedRow()<0)return;
        if (table_files.getRowCount()<=0)return;

        for (int i=filaIni; i<=filaFin;i++){
            int indexP = (Integer) table_files.getModel().getValueAt(i, this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
            int indexDS = (Integer) table_files.getModel().getValueAt(i, this.getColumnByName(table_files, PatternsTableModel.columns.nS.toString()));

            if (indexP<0 || indexDS<0){
                logdebug("pattern1D or DataSerie not found by row");
                return;
            }

            Plottable selPatt = panel_plot.getPlottable(indexP);
            PatternsTableModel.columns colName = FileUtils.searchEnum(PatternsTableModel.columns.class,table_files.getColumnName(columna));
            logdebug("column="+colName.toString()+" patt="+indexP+" serie="+indexDS+ "newValue="+table_files.getValueAt(i, columna).toString());
            switch(colName){ //        Filename, Color, Scale, ZerOff, Wavel, marker, line, errBars
            case Color:
                selPatt.getDataSerie(indexDS).color=(Color) table_files.getValueAt(i, columna);
                break;
            case Scale:
                selPatt.getDataSerie(indexDS).setScale((Float) table_files.getValueAt(i, columna));
                break;
            case ZerOff:
                selPatt.getDataSerie(indexDS).setZerrOff((Double) table_files.getValueAt(i, columna));
                break;
            case Wavel:
                selPatt.getDataSerie(indexDS).setWavelength((Double) table_files.getValueAt(i, columna));
                break;
            case XUnits:
                String xunits = (String) table_files.getValueAt(i, columna);
                //                    for (Xunits x: Xunits.values()){
                if (Xunits.getEnum(xunits)!=null){
                    selPatt.getDataSerie(indexDS).setxUnits(Xunits.getEnum(xunits));
                    if (i==0){
                        panel_plot.setXlabel(Xunits.getEnum(xunits).getName());
                    }
                    break;
                }
                //                    }
                break;
            case Type:
                //dialog preguntant si estem segurs?
                SerieType stype = SerieType.getEnum((String) table_files.getValueAt(i, columna));
                if (stype==null)break;
                SerieType currStype = selPatt.getDataSerie(indexDS).getTipusSerie();
                if (stype!=currStype) { //aixi si responem no un cop no ens ho torna a repetir infiniatment
                    final boolean doit = FileUtils.YesNoDialog(this.mainFrame, "Are you sure you want to change dataserie Type?");
                    if (doit) {
                        selPatt.getDataSerie(indexDS).setTipusSerie(stype);
                        break;
                    }else {
                        //we put back the old value
                        table_files.setValueAt(selPatt.getDataSerie(indexDS).getTipusSerie().name(), i, columna);
                    }
                }
                break;
            case YOffset:
                selPatt.getDataSerie(indexDS).setYOff((Double) table_files.getValueAt(i, columna));
                break;
            case Marker:
                selPatt.getDataSerie(indexDS).markerSize=(Float) table_files.getValueAt(i, columna);
                break;
            case Line:
                selPatt.getDataSerie(indexDS).lineWidth=(Float) table_files.getValueAt(i, columna);
                break;
            case ErrBars:
                selPatt.getDataSerie(indexDS).showErrBars=(Boolean) table_files.getValueAt(i, columna);
                break;
            case Show:
                selPatt.getDataSerie(indexDS).plotThis=(Boolean) table_files.getValueAt(i, columna);
                break;
            case Filename:
                selPatt.getDataSerie(indexDS).serieName=(String) table_files.getValueAt(i, columna);
                break;
            default:
                break;
            }
        }
        this.updateData(false,true);
    }

    private int getColumnByName(JTable table, String name) {
        for (int i = 0; i < table.getColumnCount(); ++i)
            if (table.getColumnName(i).equalsIgnoreCase(name))
                return i;
        return -1;
    }

    //nP, nS, Filename, Color, Scale, ZerOff, Wavel, Marker, Line, ErrBars, Show,
    private void addRowToTable(int nP, int nS, String fname, Color c, float scale, double zoff, double wavel, String xunits, double yoffset, float markersize, float linewidth, boolean errbars, boolean show, String stype){
        DefaultTableModel model = (DefaultTableModel) table_files.getModel();
        Object[] row = {nP,nS,fname,c,scale,zoff,wavel,xunits,yoffset,markersize,linewidth,errbars,show,stype};
        model.addRow(row);
    }

    private void do_mainFrame_windowClosing(WindowEvent e) {
        if (D1Dplot_global.isKeepSize()) {
            D1Dplot_global.def_Height=this.getMainFrame().getHeight();
            D1Dplot_global.def_Width=this.getMainFrame().getWidth();
        }

        boolean ok = D1Dplot_global.writeParFile(panel_plot.createOptionsObject());
        logdebug("par file written (method returned "+Boolean.toString(ok)+")");
        mainFrame.dispose();
        System.exit(0);
    }

    private void do_mntmQuit_actionPerformed(ActionEvent e) {
        do_mainFrame_windowClosing(null);
    }

    private void do_mntmOpen_actionPerformed(ActionEvent e) {
        openDataFile();
    }

    private void do_comboTheme_itemStateChanged(ItemEvent arg0) {
        if(arg0.getStateChange() == ItemEvent.DESELECTED)return;
        if (comboTheme.getSelectedItem().toString().equalsIgnoreCase("Light")){
            logdebug("light theme");
            panel_plot.setLightTheme(true);
        }else{
            logdebug("Dark theme");
            panel_plot.setLightTheme(false);
        }

        if (panel_plot.arePlottables()){
            boolean repaint = FileUtils.YesNoDialog(mainFrame, "Repaint current patterns?");
            if(repaint){
                reassignColorPatterns();
            }
        }
        panel_plot.actualitzaPlot();
    }

    private void do_buttonAdd_actionPerformed(ActionEvent e) {
        openDataFile();
    }

    private void do_mntmClose_actionPerformed(ActionEvent e) {
        closeDataFile();
    }

    private void do_buttonRemove_actionPerformed(ActionEvent e) {
        closeDataFile();
    }

    //duplicarem les series afegint totes les seleccionades a un mateix plottable
    private void do_buttonDupli_actionPerformed(ActionEvent e) {
        if(panel_plot.getSelectedSeries().isEmpty()) {
            log.warning("Select which pattern(s) to duplicate by selecting the corresponding row(s)");
            return;
        }
        Data_Common dc = new Data_Common(panel_plot.getFirstSelectedSerie().getWavelength());
        for (DataSerie ds:panel_plot.getSelectedSeries()) {
            dc.addDataSerie(new DataSerie(ds,ds.getTipusSerie(),true)); //dupliquem serie
        }
        panel_plot.addPlottable(dc);
        this.updateTable();
    }

    //IT MOVES PATTERNS ONLY
    private void do_buttonUp_actionPerformed(ActionEvent e) {
        if (table_files.getSelectedRow()<0){
            log.warning("Select which pattern(s) to move by selecting the corresponding row(s)");
            return;
        }
        if (table_files.getRowCount()<=0)return;

        //prova de moure series
        int[] selRows = table_files.getSelectedRows();
        for (int i=0; i<=selRows.length-1;i++){
            int selRow = selRows[i];
            int npat = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
            int nser = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nS.toString()));
            if (nser==0){
                //moure el pattern
                try{
                    panel_plot.swapPlottables(npat, npat-1);
                    table_files.setRowSelectionInterval(selRow-1, selRow-1);
                }catch(Exception ex){
                    logdebug("moving pattern... index not existing");
                }
                continue;
            }else{
                //movem la serie dins del pattern
                try{
                    Collections.swap(panel_plot.getPlottable(npat).getDataSeries(), nser, nser-1);
                    table_files.setRowSelectionInterval(selRow-1, selRow-1);
                }catch(Exception ex){
                    logdebug("moving serie... index not existing");
                }

            }
        }
        updateData(false,true);
        return;

    }

    private void do_buttonDown_actionPerformed(ActionEvent e) {
        if (table_files.getSelectedRow()<0){
            log.warning("Select which pattern(s) to move by selecting the corresponding row(s)");
            return;
        }
        if (table_files.getRowCount()<=0)return;

        //prova de moure series
        int[] selRows = table_files.getSelectedRows();
        for (int i=0; i<=selRows.length-1;i++){
            int selRow = selRows[i];
            int npat = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
            try{
                panel_plot.swapPlottables(npat, npat+1);
                table_files.setRowSelectionInterval(selRow+1, selRow+1);
            }catch(Exception ex){
                logdebug("moving pattern... index not existing");
            }
            continue;
        }

        updateData(false,true);
        return;
    }

    private void do_mntmSaveAs_actionPerformed(ActionEvent arg0) {
        saveDataFile();
    }


    private void do_mntmSaveProfile_actionPerformed(ActionEvent e) {
        saveDataProf();
    }

    private void do_chckbxShowLegend_itemStateChanged(ItemEvent arg0) {
        if (this.panel_plot!=null)panel_plot.setShowLegend(chckbxShowLegend.isSelected());
    }

    private void do_txtLegendx_actionPerformed(ActionEvent e) {
        try{
            int lx = Integer.parseInt(txtLegendx.getText());
            panel_plot.setLegendX(lx);
            txtLegendx.setText(Integer.toString(panel_plot.getLegendX()));
        }catch(Exception ex){
            if (D1Dplot_global.isDebug())ex.printStackTrace();
        }
    }

    private void do_txtLegendy_actionPerformed(ActionEvent e) {
        try{
            int ly = Integer.parseInt(txtLegendy.getText());
            panel_plot.setLegendY(ly);
            txtLegendy.setText(Integer.toString(panel_plot.getLegendY()));
        }catch(Exception ex){
            if (D1Dplot_global.isDebug())ex.printStackTrace();
        }
    }

    private void do_chckbxAutopos_itemStateChanged(ItemEvent e) {
        if (this.panel_plot==null)return;
        panel_plot.setAutoPosLegend(chckbxAutopos.isSelected());
        if (chckbxAutopos.isSelected()){
            txtLegendx.setEditable(false);
            txtLegendy.setEditable(false);
        }else{
            txtLegendx.setEditable(true);
            txtLegendy.setEditable(true);
        }
        //legend pos Actualitzem sempre
        txtLegendx.setText(Integer.toString(panel_plot.getLegendX()));
        txtLegendy.setText(Integer.toString(panel_plot.getLegendY()));
    }

    private void do_chckbxIntensityWithBackground_itemStateChanged(ItemEvent e) {
        if (this.panel_plot!=null)panel_plot.setPlotwithbkg(chckbxIntensityWithBackground.isSelected());
    }

    private void do_chckbxHklLabels_itemStateChanged(ItemEvent arg0) {
        if (this.panel_plot!=null)panel_plot.setHkllabels(chckbxHklLabels.isSelected());
    }

    private void do_chckbxShowGridLines_itemStateChanged(ItemEvent e) {
        if (this.panel_plot!=null)panel_plot.setShowGridY(chckbxShowGridY.isSelected());
    }

    private void do_chckbxShowGridX_itemStateChanged(ItemEvent arg0) {
        if (this.panel_plot!=null)panel_plot.setShowGridX(chckbxShowGridX.isSelected());
    }

    private void do_chckbxPartialYScale_itemStateChanged(ItemEvent e) {
        applyPartialYscale();
    }

    private void do_txtTini_keyReleased(KeyEvent e) {
        if (e.getKeyCode()==KeyEvent.VK_ENTER) {
            applyPartialYscale();
        }
    }
    private void do_txtFactor_keyReleased(KeyEvent e) {
        if (e.getKeyCode()==KeyEvent.VK_ENTER) {
            applyPartialYscale();
        }
    }

    private void applyPartialYscale() {
        //default values
        float t2ini=30;
        float t2fac=1.0f;
        try {
            t2ini=Float.parseFloat(txtTini.getText());
            t2fac=Float.parseFloat(txtFactor.getText());
        }catch(Exception ex) {
            log.warning("Error reading t2 value or factor");
            txtTini.setText("30.0");
            txtFactor.setText("1.0");
        }
        if (this.panel_plot!=null)panel_plot.setPartialYScale(chckbxPartialYScale.isSelected(),t2ini,t2fac);
    }

    private void do_mntmCloseAll_actionPerformed(ActionEvent e) {
        panel_plot.removeAllPlottables();
        this.updateData(true,false);
    }

    private void do_mntmExportAsPng_actionPerformed(ActionEvent e) {
        File fpng = FileUtils.fchooserSaveAsk(mainFrame, new File(D1Dplot_global.getWorkdir()), null, "png");
        if (fpng!=null){
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
                    log.warning("Error reading png scale factor");
                }
                log.writeNameNumPairs("config", true, "factor", factor);
                this.savePNG(fpng,factor);
            }
        }
    }

    private void do_mntmExportAsSvg_actionPerformed(ActionEvent e) {
        File fsvg = FileUtils.fchooserSaveNoAsk(mainFrame, new File(D1Dplot_global.getWorkdir()), null,"svg");
        if (fsvg!=null){
            this.saveSVG(fsvg);
        }
    }
    private void do_chckbxShowNegativeLabels_itemStateChanged(ItemEvent arg0) {
        if (this.panel_plot!=null)panel_plot.setNegativeYAxisLabels(chckbxShowNegativeLabels.isSelected());
    }
    private void do_chckbxVerticalYLabel_itemStateChanged(ItemEvent e) {
        if (this.panel_plot!=null)panel_plot.setVerticalYlabel(chckbxVerticalYLabel.isSelected());
    }

    private void do_chckbxVerticalYAxis_itemStateChanged(ItemEvent arg0) {
        if (this.panel_plot!=null)panel_plot.setVerticalYAxe(chckbxVerticalYAxis.isSelected());
    }

    private void do_mntmFindPeaks_1_actionPerformed(ActionEvent e) {
        if (FindPksDiag == null) {
            FindPksDiag = new FindPeaksDialog(this,this.panel_plot);
        }
        FindPksDiag.visible(true);
    }
    private void do_mntmCalcBackground_actionPerformed(ActionEvent e) {
        if (bkgDiag == null) {
            bkgDiag = new BackgroundDialog(this.panel_plot,this);
        }
        bkgDiag.visible(true);
    }

    private void do_mntmSubtractPatterns_actionPerformed(ActionEvent e) {
        if (subDiag == null) {
            subDiag = new SubtractDialog(this.panel_plot);
        }
        subDiag.visible(true);
    }

    private void do_btnReload_actionPerformed(ActionEvent e) {
        if (table_files.getSelectedRow()<0)return;
        if (table_files.getRowCount()<=0)return;
        int[] selRows = table_files.getSelectedRows();
        logdebug("number of rows selected ="+ selRows.length);
        log.writeNameNums("CONFIG", true, "selRows", selRows);

        //reloading plottables as they are the ones related to files
        int oldPattern = -1;
        for (int i=0; i<=selRows.length-1;i++){
            int selRow = selRows[i];
            //primer agafem el pattern i serie seleccionades
            int pattern = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
            if (pattern==oldPattern)continue;
            //es un nou plottable
            oldPattern = pattern;
            panel_plot.replacePlottable(pattern, DataFileUtils.readPatternFile(panel_plot.getPlottable(pattern).getFile())); //això ja actualitza el plot
        }
        this.updateData(false,false);
    }

    private void do_mntm2Dplot_actionPerformed(ActionEvent e) {
        if (table_files.getSelectedRow()<0)return;
        if (table_files.getRowCount()<=0)return;
        int[] selRows = table_files.getSelectedRows();
        if (selRows.length<=1){
            log.info("Please, select more than one pattern to create the 2D plot");
            FileUtils.InfoDialog(mainFrame,"Select more than one pattern", "2D plot selected patterns");
            return;
        }
        logdebug("number of rows selected ="+ selRows.length);
        log.writeNameNums("CONFIG", true, "selRows", selRows);

        DataSerie[] dss = new DataSerie[selRows.length];
        for (int i=0; i<=selRows.length-1;i++){
            int selRow = selRows[i];
            //primer agafem el pattern i serie seleccionades
            int pattern = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
            int serie = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nS.toString()));
            Plottable patt = panel_plot.getPlottable(pattern);
            dss[i] = patt.getDataSerie(serie);
        }

        //CAL COMPROVAR QUE TOTS ELS PATTERNS COINCIDEIXEN; SINO REBINNING I ZONA COINCIDENT
        //Primer comprovar punts, sino rebinning de les series que faci falta, la primera serie mana
        for (int i=1; i<dss.length; i++){
            boolean coin = PattOps.haveCoincidentPointsDS(dss[0], dss[i]);
            if (!coin){
                dss[i] = PattOps.rebinDS(dss[0], dss[i]);
                logdebug("rebinning serie "+i);
            }
        }

        //Pero ara encara pot ser que la serie inicial tingués més punts que les altres i això faria que peti,
        //hem de SELECCIONAR el rang coincident
        int tol = 30;
        //aqui fer un for per totes les dataseries
        double[] t2is = new double[dss.length];
        double[] t2fs = new double[dss.length];
        for (int i=0; i<dss.length;i++){
            t2is[i]=dss[i].getPointWithCorrections(0,panel_plot.isPlotwithbkg()).getX();
            t2fs[i]=dss[i].getPointWithCorrections(dss[i].getNpoints()-1,panel_plot.isPlotwithbkg()).getX();
            if(D1Dplot_global.isDebug())log.fine(String.format("t2i(%d)=%.3f t2f(%d)=%3f", i,t2is[i],i,t2fs[i]));
        }
        double t2i = PattOps.findMax(t2is);
        double t2f = PattOps.findMin(t2fs);
        logdebug(String.format("t2i(MAX)=%.3f t2f(MIN)=%3f", t2i,t2f));

        Plottable_point[] dpini = new DataPoint[dss.length];
        Plottable_point[] dpfin = new DataPoint[dss.length];
        int[] iinidp = new int[dss.length];
        int[] ifindp = new int[dss.length];
        int[] rangedp = new int[dss.length];

        for (int i=0; i<dss.length;i++){
            dpini[i] = dss[i].getClosestDP_xonly(t2i, tol);
            dpfin[i] = dss[i].getClosestDP_xonly(t2f, tol);
            iinidp[i] = dss[i].getIndexOfDP(dpini[i]);
            ifindp[i] = dss[i].getIndexOfDP(dpfin[i]);
            rangedp[i] = ifindp[i] - iinidp[i];
        }

        //check ranges
        int totRange = 0;
        for (int i=0;i<dss.length;i++){
            totRange = totRange + rangedp[i];
        }
        if (totRange/dss.length != rangedp[0]){
            log.warning("Inconsitency on nr of points in the coincident range");
        }

        //apliquem NOMES SI ES INCONSISTENT
        for (int i=0; i<dss.length; i++){
            if (t2is[i]!=t2i || t2fs[i]!=t2f){
                dss[i] = dss[i].getSubDataSerie(t2i, t2f);
                logdebug("getsubdataserie of serie "+i);
            }
        }

        p2 = new Plot2DPanel(this.getMainFrame());
        p2.visible(true);
        List<DataSerie> adss = new ArrayList<DataSerie>();
        for (int i=0; i<dss.length; i++){
            adss.add(dss[i]);
        }
        p2.setImagePatts(adss);
    }

    private void do_mntmSequentialyOffset_actionPerformed(ActionEvent arg0) {
        if (table_files.getSelectedRow()<0)return;
        if (table_files.getRowCount()<=0)return;
        int[] selRows = table_files.getSelectedRows();
        logdebug("number of rows selected ="+ selRows.length);
        log.writeNameNums("CONFIG", true, "selRows", selRows);

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
        boolean firstLine = true;
        double yoffIni = 0;
        for (int i=0; i<=selRows.length-1;i++){
            int selRow = selRows[i];
            if (firstLine==true){
                yoffIni= (Double) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.YOffset.toString()));
                firstLine = false;
                continue;
            }
            //ara ja tenim les linies que hem d'aplicar offset
            table_files.setValueAt(yoffIni+yoff*i, selRow, this.getColumnByName(table_files, PatternsTableModel.columns.YOffset.toString()));
        }
        this.updateData(false,true);

    }

    private void do_txtXtitle_actionPerformed(ActionEvent e) {
        if (txtXtitle.getText().isEmpty()){
            this.setCustomXtitle(false);
        }else{
            panel_plot.setXlabel(txtXtitle.getText());
            this.setCustomXtitle(true);
        }
    }
    private void do_txtYtitle_actionPerformed(ActionEvent e) {
        panel_plot.setYlabel(txtYtitle.getText());
    }

    private void do_mntmAbout_actionPerformed(ActionEvent e) {
        if (aboutDiag==null){
            aboutDiag = new AboutDialog(this.getMainFrame());    
        }
        aboutDiag.visible(true);
    }
    private void do_mntmConvertToWavelength_actionPerformed(ActionEvent e) {
        this.convertToWL(e);
    }
    private void do_mntmChangeXUnits_actionPerformed(ActionEvent e) {
        this.changeXunits(e);
    }
    private void do_mntmSumSelected_actionPerformed(ActionEvent e) {
        this.sumPatterns(e);
    }

    private void do_mntmRebinning_actionPerformed(ActionEvent arg0) {
        if (table_files.getRowCount()<=0)return;
        if (table_files.getSelectedRow()<0)return;
        int[] selRows = table_files.getSelectedRows();
        logdebug("number of rows selected ="+ selRows.length);
        log.writeNameNums("CONFIG", true, "selRows", selRows);

        int pattern = (Integer) table_files.getValueAt(selRows[0], this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
        int serie = (Integer) table_files.getValueAt(selRows[0], this.getColumnByName(table_files, PatternsTableModel.columns.nS.toString()));

        String st2i = FileUtils.dfX_4.format(panel_plot.getPlottable(pattern).getDataSerie(serie).getPointWithCorrections(0,panel_plot.isPlotwithbkg()).getX());
        String st2f = FileUtils.dfX_4.format(panel_plot.getPlottable(pattern).getDataSerie(serie).getPointWithCorrections(panel_plot.getPlottable(pattern).getDataSerie(serie).getNpoints()-1,panel_plot.isPlotwithbkg()).getX());
        String sstep = FileUtils.dfX_5.format(panel_plot.getPlottable(pattern).getDataSerie(serie).calcStep());


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
            puntsdummy.add(new DataPoint(t2,0,0));
            t2 = t2+step;
        }
        DataSerie dummy = new DataSerie(panel_plot.getPlottable(pattern).getDataSerie(serie),puntsdummy,panel_plot.getPlottable(pattern).getDataSerie(serie).getxUnits());

        for (int i=0; i<=selRows.length-1;i++){
            int selRow = selRows[i];
            pattern = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
            serie = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nS.toString()));

            DataSerie newds = PattOps.rebinDS(dummy, panel_plot.getPlottable(pattern).getDataSerie(serie));
            newds.serieName=panel_plot.getPlottable(pattern).getDataSerie(serie).serieName+" (rebinned)";
            Data_Common dsp = new Data_Common(newds.getWavelength());
            dsp.addDataSerie(newds);
            panel_plot.addPlottable(dsp);
        }
        this.updateData(false,false);
    }

    private void do_mntmUsersGuide_actionPerformed(ActionEvent e) {
        if (aboutDiag==null){
            aboutDiag = new AboutDialog(this.getMainFrame());    
        }
        aboutDiag.do_btnUsersGuide_actionPerformed(e);
    }


    private void do_mntmSaveProject_actionPerformed(ActionEvent e) {
        FileNameExtensionFilter[] filt = new FileNameExtensionFilter[1];
        filt[0] = new FileNameExtensionFilter("d1Dplot project","d1d");
        File f = FileUtils.fchooserSaveAsk(this.mainFrame, D1Dplot_global.getWorkdirFile(), filt, "d1d", "Save d1Dplot project");
        if (f!=null) {
            DataFileUtils.writeProject(f, this.getPanel_plot());
        }
    }
    private void do_mntmOpenProject_actionPerformed(ActionEvent e) {
        FileNameExtensionFilter[] filt = new FileNameExtensionFilter[1];
        filt[0] = new FileNameExtensionFilter("d1Dplot project","d1d");
        File f = FileUtils.fchooserOpen(this.mainFrame, D1Dplot_global.getWorkdirFile(), filt, 0);
        if (f!=null) {
            DataFileUtils.readProject(f, this.getPanel_plot(),this);
        }
        this.updateData(false,true);

    }
    private void do_mntmDebug_actionPerformed(ActionEvent e) {
        //        debug_latgen dl = new debug_latgen(this);
        //        dl.visible(true);
    }

    private void do_mntmCheckForUpdates_actionPerformed(ActionEvent e) {
        log.info("software updates not available");
    }

    private void do_btnReassignColors_actionPerformed(ActionEvent e) {
        this.reassignColorPatterns();
    }

    private void do_mntmDB_actionPerformed(ActionEvent e) {
        if (DBDiag == null) {
            DBDiag = new Database(this.getPanel_plot());
        }
        DBDiag.visible(true);
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

    private boolean isCustomXtitle() {
        return customXtitle;
    }

    private void setCustomXtitle(boolean customXtitle) {
        this.customXtitle = customXtitle;
    }

    public PlotPanel getPanel_plot() {
        return panel_plot;
    }

    public void setPanel_plot(PlotPanel panel_plot) {
        this.panel_plot = panel_plot;
    }

    private void logdebug(String s){
        if (D1Dplot_global.isDebug()){
            log.debug(s);
        }
    }

}

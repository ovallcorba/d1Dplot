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
import vava33.d1dplot.auxi.DataPoint;
import vava33.d1dplot.auxi.DataSerie;
import vava33.d1dplot.auxi.PattOps;
import vava33.d1dplot.auxi.Pattern1D;
import vava33.d1dplot.auxi.PatternsTableCellRenderer;
import vava33.d1dplot.auxi.PatternsTableModel;
import vava33.d1dplot.auxi.DataSerie.xunits;

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
    private Subtract_dialog subDiag;
    private PlotPanel2D p2;
    private About_dialog aboutDiag;
    private boolean customXtitle = false;
    
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
    private JCheckBox chckbxShowGridLines;
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
                    D1Dplot_global.printAllOptions("info");
                    ArgumentLauncher.readArguments(frame, args);
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
    
    protected void do_mainFrame_windowClosing(WindowEvent e) {
        boolean ok = D1Dplot_global.writeParFile();
        logdebug("par file written (method returned "+Boolean.toString(ok)+")");
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
        mainFrame.setIconImage(D1Dplot_global.getIcon());
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
        
        btnReload = new JButton("Reload");
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
        panel_DS.add(btnReload, "cell 1 2 2 1,growx");
        
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

                        
        scrollPane_1 = new JScrollPane();
        tabbedPanel_bottom.addTab("Plot Settings", null, scrollPane_1, null);
        
        JPanel panel = new JPanel();
        scrollPane_1.setViewportView(panel);
        panel.setBorder(null);
        panel.setLayout(new MigLayout("", "[][grow][][][][][][][][]", "[][][]"));
        
        JLabel lblXTitle = new JLabel("X title");
        panel.add(lblXTitle, "cell 0 0,alignx trailing");
        
        txtXtitle = new JTextField();
        txtXtitle.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtXtitle_actionPerformed(e);
            }
        });
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

        
        scrollPane_2 = new JScrollPane();
        tabbedPanel_bottom.addTab("Log", null, scrollPane_2, null);
        tAOut = new LogJTextArea();
        scrollPane_2.setViewportView(tAOut);

        panel_plot = new PlotPanel(this);
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
        
        mntmExportAsPng = new JMenuItem("Export as PNG...");
        mnFile.add(mntmExportAsPng);
        mntmExportAsPng.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmExportAsPng_actionPerformed(e);
            }
        });
        
        mntmExportAsSvg = new JMenuItem("Export as SVG...");
        mnFile.add(mntmExportAsSvg);
        mntmExportAsSvg.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmExportAsSvg_actionPerformed(e);
            }
        });
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
        mntm2Dplot.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntm2Dplot_actionPerformed(e);
            }
        });
        
        mnOps = new JMenu("Processing");
        menuBar.add(mnOps);
        
        mntmFindPeaks_1 = new JMenuItem("Find Peaks...");
        mntmFindPeaks_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmFindPeaks_1_actionPerformed(e);
            }
        });
        mnOps.add(mntmFindPeaks_1);
        
        mntmCalcBackground = new JMenuItem("Calc Background...");
        mntmCalcBackground.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_mntmCalcBackground_actionPerformed(e);
            }
        });
        mnOps.add(mntmCalcBackground);
        
        mntmSubtractPatterns = new JMenuItem("Subtract patterns...");
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
        
        mntmRebinning = new JMenuItem("Rebinning...");
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
                
        inicia();
    }

    //=========================================================
    
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
            Pattern1D patt = panel_plot.getPatterns().get(pattern);
            dss[i] = patt.getSerie(serie);
            sb.append(String.format("P%dS%d ", pattern,serie));
            sbNames.append(dss[i].getSerieName()+" ");
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
        suma.setSerieName(String.format("Sum of %s",sb.toString().trim()));
        Pattern1D patt = new Pattern1D();
        patt.getCommentLines().addAll(dss[0].getPatt1D().getCommentLines()); //comments of 1st serie
//        patt.getCommentLines().add("# "+suma.getSerieName());
        patt.getCommentLines().add("#Sum of: "+sbNames.toString().trim());
        patt.setOriginal_wavelength(dss[0].getPatt1D().getOriginal_wavelength());
        patt.AddDataSerie(suma);
        panel_plot.getPatterns().add(patt);
        this.updateData();

    }
    
    private void changeXunits(ActionEvent e){
        if (table_files.getSelectedRow()<0)return;
        if (table_files.getRowCount()<=0)return;
        
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
                loginfo("choose a valid x-units value");
                return;
            }
            
            if (destUnits==xunits.G){
                loginfo("Conversion to G not supported");
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
                
                Pattern1D patt = panel_plot.getPatterns().get(pattern);
                DataSerie ds = patt.getSerie(serie);
                
                if (ds.getxUnits()==xunits.G){
                    loginfo("Conversion from G not supported");
                    return;
                }
                
                if (ds.getWavelength()<0){
                    loginfo(String.format("error: pattern %d serie %d has no wavelength assigned", pattern,serie));
                    continue;
                }
                logdebug(String.format("Pattern=%d Serie=%d", pattern,serie));
                logdebug(String.format("SerieUnits=%s DestUnits=%s", ds.getxUnits().getName(), destUnits.getName()));
                
                //CONVERTIM
                loginfo(String.format("Conversion from %s to %s",ds.getxUnits(), destUnits));
                DataSerie newDS = ds.convertToXunits(destUnits);
                
                if (newDS!=null){
                    patt.AddDataSerie(newDS);                    
                }else{
                    loginfo("not converted, same input and output units maybe?");
                }
            }
//            panel_plot.repaint();
            this.updateData();
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
                logdebug("error parsing wavelength");
            }
            if (newWL<0){
                loginfo("invalid wavelength entered");
                return;
            }
            
            int[] selRows = table_files.getSelectedRows();
            logdebug("number of rows selected ="+ selRows.length);
            
            for (int i=0; i<=selRows.length-1;i++){
                int selRow = selRows[i];
                //primer agafem el pattern i serie seleccionades
                int pattern = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
                int serie = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nS.toString()));
                
                Pattern1D patt = panel_plot.getPatterns().get(pattern);
                DataSerie ds = patt.getSerie(serie);
                
                if (ds.getWavelength()<0){
                    loginfo(String.format("pattern %d serie %d has no wavelength assigned", pattern,serie));
                    continue;
                }
                patt.AddDataSerie(ds.convertToNewWL(newWL));
            }
            
//            panel_plot.repaint();
            this.updateData();
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
                loginfo("select a valid color");
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
                        loginfo("column not editable");
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
                        loginfo("column not editable");
                        break;
                    case Wavel:
                        table_files.setValueAt(Double.parseDouble(s), selRow, selCol);
                        break;
                    case XUnits:
                        loginfo("column not editable");
                        break;
                    case YOffset:
                        table_files.setValueAt(Double.parseDouble(s), selRow, selCol);
                        break;
                    case ZerOff:
                        table_files.setValueAt(Double.parseDouble(s), selRow, selCol);
                        break;
                    case nP:
                        loginfo("column not editable");
                        break;
                    case nS:
                        loginfo("column not editable");
                        break;
                    default:
                        loginfo("column not identified");
                        break;
                }
            }
            return;
        }
    }
    
    private void aplicarselecciotaula(ListSelectionEvent arg0){
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
            panel_plot.getSelectedSeries().add(panel_plot.getPatterns().get(indexP).getSerie(indexDS));    
            logdebug("s'sha seleccionat patt="+indexP+" serie="+indexDS);
        }
    }
    
    private void applicarModificacioTaula(int columna, int filaIni, int filaFin){

        if (table_files.getSelectedRow()<0)return;
        if (table_files.getRowCount()<=0)return;
        
        for (int i=filaIni; i<=filaFin;i++){
            int indexP = (Integer) table_files.getModel().getValueAt(i, this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
            int indexDS = (Integer) table_files.getModel().getValueAt(i, this.getColumnByName(table_files, PatternsTableModel.columns.nS.toString()));
            
            if (indexP<0 || indexDS<0){
                logdebug("pattern1D or DataSerie not found by row");
                return;
            }
            
            Pattern1D selPatt = panel_plot.getPatterns().get(indexP);
            PatternsTableModel.columns colName = FileUtils.searchEnum(PatternsTableModel.columns.class,table_files.getColumnName(columna));
            logdebug("column="+colName.toString()+" patt="+indexP+" serie="+indexDS+ "newValue="+table_files.getValueAt(i, columna).toString());
            switch(colName){ //        Filename, Color, Scale, ZerOff, Wavel, marker, line, errBars
                case Color:
                    selPatt.getSerie(indexDS).setColor((Color) table_files.getValueAt(i, columna));
                    break;
                case Scale:
                    selPatt.getSerie(indexDS).setScale((Float) table_files.getValueAt(i, columna));
                    break;
                case ZerOff:
                    selPatt.getSerie(indexDS).setZerrOff((Double) table_files.getValueAt(i, columna));
                    break;
                case Wavel:
                    selPatt.getSerie(indexDS).setWavelength((Double) table_files.getValueAt(i, columna));
                    break;
                case XUnits:
                    String xunits = (String) table_files.getValueAt(i, columna);
                    for (DataSerie.xunits x: DataSerie.xunits.values()){
                        if (x.getEnum(xunits)!=null){
                            selPatt.getSerie(indexDS).setxUnits(x);
                            if (i==0){
                                panel_plot.setXlabel(x.getName());
                            }
                            break;
                        }
                    }
                    break;
                case YOffset:
                    selPatt.getSerie(indexDS).setYOff((Double) table_files.getValueAt(i, columna));
                    break;
                case Marker:
                    selPatt.getSerie(indexDS).setMarkerSize((Float) table_files.getValueAt(i, columna));
                    break;
                case Line:
                    selPatt.getSerie(indexDS).setLineWidth((Float) table_files.getValueAt(i, columna));
                    break;
                case ErrBars:
                    selPatt.getSerie(indexDS).setShowErrBars((Boolean) table_files.getValueAt(i, columna));
                    break;
                case Show:
                    selPatt.getSerie(indexDS).setPlotThis((Boolean) table_files.getValueAt(i, columna));
                    break;
                case Filename:
                    selPatt.getSerie(indexDS).setSerieName((String) table_files.getValueAt(i, columna));
                    break;
                default:
                    break;
            }
        }
//        panel_plot.repaint();
        this.updateData();
    }
    
    private int getColumnByName(JTable table, String name) {
        for (int i = 0; i < table.getColumnCount(); ++i)
            if (table.getColumnName(i).equalsIgnoreCase(name))
                return i;
        return -1;
    }
 
    private void inicia(){
//        D1Dplot_global.initPars();
//        if (D1Dplot_global.logging && !D1Dplot_global.isDebug())log.addTextAreaHandler(tAOut);
        
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
        loginfo(D1Dplot_global.welcomeMSG);

        if(D1Dplot_global.getConfigFileReaded()==null){
            loginfo(String.format("No config file found on: %s, it will be created on exit!",D1Dplot_global.configFilePath));
        }else{
            if(D1Dplot_global.getConfigFileReaded()==true){
                loginfo(String.format("Config file readed: %s",D1Dplot_global.configFilePath));    
            }else{
                loginfo(String.format("Error reading config file: %s",D1Dplot_global.configFilePath));
            }
        }
        
        this.txtXtitle.setText(panel_plot.getXlabel());
        this.txtYtitle.setText(panel_plot.getYlabel());
        
        int iconwidth = 18;
        Image MU = new ImageIcon(D1Dplot_main.class.getResource("/vava33/d1dplot/img/fletxa_amunt.png")).getImage().getScaledInstance(-100, iconwidth, java.awt.Image.SCALE_SMOOTH);
        buttonUp.setText("");
        buttonUp.setIcon(new ImageIcon(MU));
        Image MD = new ImageIcon(D1Dplot_main.class.getResource("/vava33/d1dplot/img/fletxa_avall.png")).getImage().getScaledInstance(-100, iconwidth, java.awt.Image.SCALE_SMOOTH);
        buttonDown.setText("");
        buttonDown.setIcon(new ImageIcon(MD));
        Image ADD = new ImageIcon(D1Dplot_main.class.getResource("/vava33/d1dplot/img/afegir.png")).getImage().getScaledInstance(-100, iconwidth, java.awt.Image.SCALE_SMOOTH);
        buttonAdd.setText("");
        buttonAdd.setIcon(new ImageIcon(ADD));
        Image REM = new ImageIcon(D1Dplot_main.class.getResource("/vava33/d1dplot/img/borrar.png")).getImage().getScaledInstance(-100, iconwidth, java.awt.Image.SCALE_SMOOTH);
        buttonRemove.setText("");
        buttonRemove.setIcon(new ImageIcon(REM));
        

        //columnes mides
        
        PatternsTableModel model = (PatternsTableModel) table_files.getModel();
        for (int i=0; i<table_files.getColumnCount(); i++){
            table_files.getColumnModel().getColumn(i).setPreferredWidth(model.getColumnPrefSize(i));
            table_files.getColumnModel().getColumn(i).setMaxWidth(model.getColumnMaxSize(i));
            table_files.getColumnModel().getColumn(i).setMinWidth(model.getColumnMinSize(i));
            logdebug("columna "+i+" Min="+model.getColumnMinSize(i)+" Max="+model.getColumnMaxSize(i)+" pref="+model.getColumnPrefSize(i));
        }

        for (int i=0; i<table_files.getColumnCount(); i++){
            logdebug("col="+i+" Min="+table_files.getColumnModel().getColumn(i).getMinWidth()+" Max="+table_files.getColumnModel().getColumn(i).getMaxWidth()+" pref="+table_files.getColumnModel().getColumn(i).getPreferredWidth());
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

    public void openDataFile(){
        logdebug("openDataFile entered");
        FileNameExtensionFilter filt[] = DataFileUtils.getExtensionFilterRead();
        File[] datFile = FileUtils.fchooserMultiple(mainFrame, new File(getWorkdir()), filt,  filt.length-1);
        if (datFile == null){
            loginfo("No data file selected");
            return;
        }
        if (datFile.length>8)DataSerie.setDef_markerSize(0); //perque vagi mes fluid
        for (int i=0; i<datFile.length;i++){
            readDataFile(datFile[i]);    
        }
        logdebug("openDataFile exited");
        this.updateData();
    }
    
    //creem aquest per l'argument launcher
    public void readDataFile(File datfile){
        Pattern1D patt = new Pattern1D();
        boolean ok = DataFileUtils.readPatternFile(datfile,patt);
        if (!ok){
            loginfo("Error reading data file");
            return;
        }
        panel_plot.getPatterns().add(patt);
        D1Dplot_global.setWorkdir(datfile);
    }
    
    public void updateData(){
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
        
        panel_plot.fitGraph();
        panel_plot.repaint();
//        mainFrame.repaint();
        logdebug("updateData exited");

    }
    //nP, nS, Filename, Color, Scale, ZerOff, Wavel, Marker, Line, ErrBars, Show,
    private void addRowToTable(int nP, int nS, String fname, Color c, float scale, double zoff, double wavel, String xunits, double yoffset, float markersize, float linewidth, boolean errbars, boolean show){
        DefaultTableModel model = (DefaultTableModel) table_files.getModel();
        Object[] row = {nP,nS,fname,c,scale,zoff,wavel,xunits,yoffset,markersize,linewidth,errbars,show};
        model.addRow(row);
    }
        
    
    protected void updateTable(){
        DefaultTableModel model = (DefaultTableModel) table_files.getModel();
        model.setRowCount(0);
        
        Iterator<Pattern1D> itrP = panel_plot.getPatterns().iterator();
        boolean first = true;
        while (itrP.hasNext()){
            Pattern1D p = itrP.next();
            int numP = panel_plot.getPatterns().indexOf(p);
            Iterator<DataSerie> itrD = p.getSeriesIterator();
            while (itrD.hasNext()){
                DataSerie d = itrD.next();
                int numD = p.indexOfSerie(d);
                
                if (first && !isCustomXtitle()){
                    //posem be les unitats a l'eix X?
                    if (d.getxUnits()==DataSerie.xunits.tth){
                        panel_plot.setXlabel("2"+D1Dplot_global.theta+" (º)");
                        txtXtitle.setText("2"+D1Dplot_global.theta+" (º)");   
                    }else{
                        panel_plot.setXlabel(d.getxUnits().getName());
                        txtXtitle.setText(d.getxUnits().getName());
                    }
                    first=false;
                }
                
                String fnam = d.getSerieName();
                logdebug(fnam);
                logdebug(d.getColor().toString());
                logdebug(Float.toString(d.getScale()));
                logdebug(Double.toString(d.getZerrOff()));
                logdebug(Double.toString(d.getWavelength()));
                logdebug(Float.toString(d.getMarkerSize()));
                logdebug(Float.toString(d.getLineWidth()));
                logdebug(Boolean.toString(d.isShowErrBars()));
                logdebug(Boolean.toString(d.isPlotThis()));
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
            logdebug("light theme");
            D1Dplot_global.setLightTheme(true);
            PlotPanel.setLightTheme(true);
        }else{
            logdebug("Dark theme");
            D1Dplot_global.setLightTheme(false);
            PlotPanel.setLightTheme(false);
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
            Iterator<DataSerie> itrds = p.getSeriesIterator();
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
            loginfo("First, select row(s)");
            return;
        }
        if (table_files.getRowCount()<=0)return;
        
        int[] selRows = table_files.getSelectedRows();
        logdebug("number of rows selected ="+ selRows.length);
        logdebug("selrows[0] ="+ selRows[0]);
        ArrayList<DataSerie> toDelete = new ArrayList<DataSerie>();
        
        for (int i=0; i<=selRows.length-1;i++){
            int selRow = selRows[i];
            int npat = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
            int nser = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nS.toString()));
            
            toDelete.add(panel_plot.getPatterns().get(npat).getSerie(nser));
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
            loginfo("First, select row(s)");
            return;
        }
        if (table_files.getRowCount()<=0)return;
        
        int[] selRows = table_files.getSelectedRows();
        for (int i=0; i<=selRows.length-1;i++){
            int selRow = selRows[i];
            int npat = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
            int nser = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nS.toString()));
            if (nser==0){
                //moure el pattern
                try{
                    Collections.swap(panel_plot.getPatterns(), npat, npat-1);    
                    table_files.setRowSelectionInterval(selRow-1, selRow-1);
                }catch(Exception ex){
                    logdebug("moving pattern... index not existing");
                }
                continue;
            }else{
                //movem la serie dins del pattern
                try{
                    Collections.swap(panel_plot.getPatterns().get(npat).getSeries(), nser, nser-1);
                    table_files.setRowSelectionInterval(selRow-1, selRow-1);
                }catch(Exception ex){
                    logdebug("moving serie... index not existing");
                }
                
            }
        }
        
        updateData();
        return;
        
    }
    protected void do_buttonDown_actionPerformed(ActionEvent e) {
        if (table_files.getSelectedRow()<0){
            loginfo("First, select row(s)");
            return;
        }
        if (table_files.getRowCount()<=0)return;
        
        int[] selRows = table_files.getSelectedRows();
        for (int i=0; i<=selRows.length-1;i++){
            int selRow = selRows[i];
            int npat = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
            int nser = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nS.toString()));
            if (nser==panel_plot.getPatterns().get(npat).getNseries()-1){
                //moure el pattern
                try{
                    Collections.swap(panel_plot.getPatterns(), npat, npat+1);    
                    table_files.setRowSelectionInterval(selRow+1, selRow+1);
                }catch(Exception ex){
                    logdebug("moving pattern... index not existing");
                }
                continue;
            }else{
                //movem la serie dins del pattern
                try{
                    Collections.swap(panel_plot.getPatterns().get(npat).getSeries(), nser, nser+1);
                    table_files.setRowSelectionInterval(selRow+1, selRow+1);
                }catch(Exception ex){
                    logdebug("moving serie... index not existing");
                }
            }
        }
        
        updateData();
        return;
    }
    
    protected void do_mntmSaveAs_actionPerformed(ActionEvent arg0) {
        saveDataFile();
    }
    
    public void saveDataFile(){
        logdebug("saveDataFile entered");
        if (table_files.getRowCount()<=0)return;
        if (table_files.getSelectedRow()<0){
            loginfo("Select on the table the pattern you want to save");
            return;
        }
        
        int[] selRows = table_files.getSelectedRows();
        if (selRows.length>1){
            loginfo("Please, select only ONE row (pattern)");
            return;
        }

        int npat = (Integer) table_files.getValueAt(selRows[0], this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
        int nser = (Integer) table_files.getValueAt(selRows[0], this.getColumnByName(table_files, PatternsTableModel.columns.nS.toString()));
        
        FileNameExtensionFilter filt[] = DataFileUtils.getExtensionFilterWrite();
        File datFile = FileUtils.fchooser(mainFrame,new File(getWorkdir()), filt, 0, true, true);
        if (datFile == null){
            loginfo("No data file selected");
            return;
        }
        
        datFile = DataFileUtils.writePatternFile(datFile, panel_plot.getPatterns().get(npat), nser, true);
        loginfo(datFile.toString()+" written!");
        logdebug("saveDataFile exited");
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
            txtLegendx.setText(Integer.toString(panel_plot.getLegendX()));
        }catch(Exception ex){
            if (D1Dplot_global.isDebug())ex.printStackTrace();
        }
    }
    protected void do_txtLegendy_actionPerformed(ActionEvent e) {
        try{
            int ly = Integer.parseInt(txtLegendy.getText());
            panel_plot.setLegendY(ly);
            panel_plot.repaint();
            txtLegendy.setText(Integer.toString(panel_plot.getLegendY()));
        }catch(Exception ex){
            if (D1Dplot_global.isDebug())ex.printStackTrace();
        }
    }
    
    protected void setTxtLegendFromPanel(){
        try{
            txtLegendx.setText(Integer.toString(panel_plot.getLegendX()));
            txtLegendy.setText(Integer.toString(panel_plot.getLegendY()));            
        }catch(Exception ex){
            log.debug("error filling legend textboxes");
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
        panel_plot.getGraphPanel().paintComponent(svgGenerator);

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
        g2d.scale(scaleFactor, scaleFactor);
        panel_plot.getGraphPanel().paintComponent(g2d);
        g2d.dispose();

        try {
            ImageIO.write(img, "png", fpng);
        } catch (Exception ex) {
            if (D1Dplot_global.isDebug())ex.printStackTrace();
        }
        logdebug(fpng.toString()+" written");
    }

    protected void do_mntmCloseAll_actionPerformed(ActionEvent e) {
        panel_plot.getPatterns().clear();
        this.updateData();
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
                    logdebug("error reading factor");
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

    protected void do_mntmFindPeaks_1_actionPerformed(ActionEvent e) {
        if (FindPksDiag == null) {
            FindPksDiag = new FindPeaks_dialog(this.panel_plot,this);
        }
        FindPksDiag.setVisible(true);
    }
    protected void do_mntmCalcBackground_actionPerformed(ActionEvent e) {
        if (bkgDiag == null) {
            bkgDiag = new Background_dialog(this.panel_plot,this);
        }
        bkgDiag.setVisible(true);
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
    
    protected void do_mntmSubtractPatterns_actionPerformed(ActionEvent e) {
        if (subDiag == null) {
            subDiag = new Subtract_dialog(this.panel_plot,this);
        }
        subDiag.setVisible(true);
    }
    
    protected void do_btnReload_actionPerformed(ActionEvent e) {
        if (table_files.getSelectedRow()<0)return;
        if (table_files.getRowCount()<=0)return;
        int[] selRows = table_files.getSelectedRows();
        logdebug("number of rows selected ="+ selRows.length);
        log.writeNameNums("CONFIG", true, "selRows", selRows);
        
        boolean prfUpdated = false;
        for (int i=0; i<=selRows.length-1;i++){
            int selRow = selRows[i];
            //primer agafem el pattern i serie seleccionades
            int pattern = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
            int serie = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nS.toString()));
            Pattern1D patt = panel_plot.getPatterns().get(pattern);
            if (patt.isPrf()){
                //remove the four series
                if (!prfUpdated){
                    patt.removeAllSeries();
                    DataFileUtils.readPatternFile(patt.getFile(),patt);
//                    Pattern1D nwpatt = DataFileUtils.readPatternFile(patt.getFile()); 
//                    patt.AddDataSerie(nwpatt.getSerie(0));
//                    patt.AddDataSerie(nwpatt.getSerie(1));
//                    patt.AddDataSerie(nwpatt.getSerie(2));
//                    patt.AddDataSerie(nwpatt.getSerie(3));
                }
                prfUpdated = true; //aixi nomes ho fem un cop i no 4 si les 4 series estan seleccionades
            }else{
                //hem de mirar la serie
                Color c = patt.getSerie(serie).getColor();
                patt.removeDataSerie(serie);
                DataFileUtils.readPatternFile(patt.getFile(),patt);
                patt.getSerie(0).setColor(c);
            }
        }
        this.updateData();
    }

    protected void do_mntm2Dplot_actionPerformed(ActionEvent e) {
        if (table_files.getSelectedRow()<0)return;
        if (table_files.getRowCount()<=0)return;
        int[] selRows = table_files.getSelectedRows();
        if (selRows.length<=1){
            FileUtils.InfoDialog(mainFrame,"Select more than one pattern", "2D plot selected patterns");
            return;
        }
        logdebug("number of rows selected ="+ selRows.length);
        log.writeNameNums("CONFIG", true, "selRows", selRows);
        
//        ArrayList<DataSerie> dss = new ArrayList<DataSerie>();
        DataSerie[] dss = new DataSerie[selRows.length];
        for (int i=0; i<=selRows.length-1;i++){
            int selRow = selRows[i];
            //primer agafem el pattern i serie seleccionades
            int pattern = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
            int serie = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nS.toString()));
            Pattern1D patt = panel_plot.getPatterns().get(pattern);
            dss[i] = patt.getSerie(serie);
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
            t2is[i]=dss[i].getPoint(0).getX();
            t2fs[i]=dss[i].getPoint(dss[i].getNpoints()-1).getX();
            logdebug(String.format("t2i(%d)=%.3f t2f(%d)=%3f", i,t2is[i],i,t2fs[i]));
        }
        double t2i = PattOps.findMax(t2is);
        double t2f = PattOps.findMin(t2fs);
        logdebug(String.format("t2i(MAX)=%.3f t2f(MIN)=%3f", t2i,t2f));
        
        DataPoint[] dpini = new DataPoint[dss.length];
        DataPoint[] dpfin = new DataPoint[dss.length];
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
            loginfo("inconsitency on nr of points in the coincident range");
            return;
        }
        
        //apliquem NOMES SI ES INCONSISTENT
        for (int i=0; i<dss.length; i++){
            if (t2is[i]!=t2i || t2fs[i]!=t2f){
                dss[i] = dss[i].getSubDataSerie(t2i, t2f);
                logdebug("getsubdataserie of serie "+i);
            }
        }
        
        p2 = new PlotPanel2D(this);
        p2.setVisible(true);
        ArrayList<DataSerie> adss = new ArrayList<DataSerie>();
        for (int i=0; i<dss.length; i++){
            adss.add(dss[i]);
        }
        p2.setImagePatts(adss);
    }
    
    protected void do_mntmSequentialyOffset_actionPerformed(ActionEvent arg0) {
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
                loginfo("error reading y offset");
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
        this.updateData();
        
    }
    
    protected void do_txtXtitle_actionPerformed(ActionEvent e) {
        if (txtXtitle.getText().isEmpty()){
            this.setCustomXtitle(false);
        }else{
            panel_plot.setXlabel(txtXtitle.getText());
            this.setCustomXtitle(true);
        }
    }
    protected void do_txtYtitle_actionPerformed(ActionEvent e) {
        panel_plot.setYlabel(txtYtitle.getText());
    }

    /**
     * @return the customXtitle
     */
    public boolean isCustomXtitle() {
        return customXtitle;
    }

    /**
     * @param customXtitle the customXtitle to set
     */
    public void setCustomXtitle(boolean customXtitle) {
        this.customXtitle = customXtitle;
    }
    
    private void logdebug(String s){
        if (D1Dplot_global.isDebug()){
            log.debug(s);
        }
    }
    private void loginfo(String s){
        if (D1Dplot_global.logging){
            log.info(s);
        }
        tAOut.stat(s); //ho passem pel txtArea
    }
    
    public LogJTextArea getTAOut(){
        return tAOut;
    }

    protected void do_mntmAbout_actionPerformed(ActionEvent e) {
        if (aboutDiag==null){
            aboutDiag = new About_dialog();    
        }
        aboutDiag.setVisible(true);
    }
    protected void do_mntmConvertToWavelength_actionPerformed(ActionEvent e) {
        this.convertToWL(e);
    }
    protected void do_mntmChangeXUnits_actionPerformed(ActionEvent e) {
        this.changeXunits(e);
    }
    protected void do_mntmSumSelected_actionPerformed(ActionEvent e) {
        this.sumPatterns(e);
    }
    
    protected void do_mntmRebinning_actionPerformed(ActionEvent arg0) {
        if (table_files.getRowCount()<=0)return;
        if (table_files.getSelectedRow()<0)return;
        int[] selRows = table_files.getSelectedRows();
        logdebug("number of rows selected ="+ selRows.length);
        log.writeNameNums("CONFIG", true, "selRows", selRows);
        
        int pattern = (Integer) table_files.getValueAt(selRows[0], this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
        int serie = (Integer) table_files.getValueAt(selRows[0], this.getColumnByName(table_files, PatternsTableModel.columns.nS.toString()));
        
        String st2i = FileUtils.dfX_4.format(panel_plot.getPatterns().get(pattern).getSerie(serie).getPoint(0).getX());
        String st2f = FileUtils.dfX_4.format(panel_plot.getPatterns().get(pattern).getSerie(serie).getPoint(panel_plot.getPatterns().get(pattern).getSerie(serie).getNpoints()-1).getX());
        if (panel_plot.getPatterns().get(pattern).getSerie(serie).getStep()<0){
            panel_plot.getPatterns().get(pattern).getSerie(serie).setStep(panel_plot.getPatterns().get(pattern).getSerie(serie).calcStep());
        }
        String sstep = FileUtils.dfX_5.format(panel_plot.getPatterns().get(pattern).getSerie(serie).getStep());

        
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
                loginfo("error parsing values");
                return;
            }
        }
        if (step==0)return;
        if (t2f==0)return;
        
        //crearem una dataserie falsa
        ArrayList<DataPoint> puntsdummy = new ArrayList<DataPoint>();
        double t2 = t2i;
        while (t2<=t2f){
            puntsdummy.add(new DataPoint(t2,0,0));
            t2 = t2+step;
        }
        DataSerie dummy = new DataSerie(panel_plot.getPatterns().get(pattern).getSerie(serie),panel_plot.getPatterns().get(pattern).getSerie(serie).getTipusSerie(),false);
        dummy.setSeriePoints(puntsdummy);
        
        for (int i=0; i<=selRows.length-1;i++){
            int selRow = selRows[i];
            pattern = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nP.toString()));
            serie = (Integer) table_files.getValueAt(selRow, this.getColumnByName(table_files, PatternsTableModel.columns.nS.toString()));
            
            DataSerie newds = PattOps.rebinDS(dummy, panel_plot.getPatterns().get(pattern).getSerie(serie));
            newds.setSerieName(panel_plot.getPatterns().get(pattern).getSerie(serie).getSerieName()+" (rebinned)");
            newds.setPatt1D(panel_plot.getPatterns().get(pattern).getSerie(serie).getPatt1D());
        }
        this.updateData();
    }
    
    protected void do_mntmUsersGuide_actionPerformed(ActionEvent e) {
        if (aboutDiag==null){
            aboutDiag = new About_dialog();    
        }
        aboutDiag.do_btnUsersGuide_actionPerformed(e);
    }
}

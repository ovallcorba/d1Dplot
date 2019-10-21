package com.vava33.d1dplot;

/**
 * D1Dplot
 * 
 * Plotting panel
 *
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;

import com.vava33.d1dplot.data.DataPoint;
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.d1dplot.data.Plottable;
import com.vava33.d1dplot.data.Plottable_point;
import com.vava33.d1dplot.data.SerieType;
import com.vava33.d1dplot.data.Xunits;
import com.vava33.jutils.FileUtils;
import com.vava33.jutils.Options;
import com.vava33.jutils.VavaLogger;

import net.miginfocom.swing.MigLayout;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.ItemEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JLabel;

import org.apache.commons.math3.util.FastMath;

import javax.swing.border.TitledBorder;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.LineBorder;
import java.awt.Dimension;
import javax.swing.JComboBox;
import javax.swing.border.EmptyBorder;

public class PlotPanel {

    private List<Plottable> dataToPlot; //(dataseries inside plottables)

    //TEMES (aquests son final de moment perquè no els modifico enlloc)
    private static final Color Dark_bkg = Color.BLACK;
    private static final Color Dark_frg = Color.WHITE;
    private static final Color Light_bkg = Color.WHITE;
    private static final Color Light_frg = Color.BLACK;
    private static final Color Light_Legend_line = Color.BLACK;
    private static final Color Dark_Legend_line = Color.WHITE;
    private static final Color Light_Legend_bkg = Color.WHITE;
    private static final Color Dark_Legend_bkg = Color.BLACK;
    
    //PARAMETRES VISUALS amb els valors per defecte 
    private boolean lightTheme = true;
    private int gapAxisTop = 0; //12
    private int gapAxisBottom = 0; //35
    private int gapAxisRight = 0; //12
    private int gapAxisLeft = 0; //80
    private int padding5px = 5;
    private String xlabel = "2"+D1Dplot_global.theta+" (º)";
    private String ylabel = "Intensity";
    private int def_nDecimalsX = 3;
    private int def_nDecimalsY = 1;
    //sizes relative to default one (12?)
    float def_axis_fsize = 0.f;
    float def_axisL_fsize = 0.f;
    private float def_legend_fsize = 0.f;
    private boolean plotwithbkg=false; //pel PRF!!
    private Color colorDBcomp = Color.blue;
    private double splitPanePosition = -1;
    private boolean customXtitle = false;
    
    //CONVENI DEFECTE:
    //cada 100 pixels una linia principal i cada 25 una secundaria
    //mirem l'amplada/alçada del graph area i dividim per tenir-ho en pixels        
    double incXPrimPIXELS = 100;
    double incXSecPIXELS = 25;
    private double incYPrimPIXELS = 100;
    private double incYSecPIXELS = 25;
    private static final int minZoomPixels = 5; //el faig final i general perque no el modifiquem
    private double facZoom = 1.1f;

    // DEFINICIO BUTONS DEL MOUSE
    private int MOURE = MouseEvent.BUTTON2;
    private int CLICAR = MouseEvent.BUTTON1;
    private int ZOOM_BORRAR = MouseEvent.BUTTON3;

    private int div_PrimPixSize = 8; //originalment eren 8 i 4, ho faig imparell perquè hi hagi pixel "central"
    private int div_SecPixSize = 4;
    private boolean verticalYlabel = false;
    private boolean verticalYAxe = true;

    private static final String className = "PlotPanel";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);

    private double xMin = 0;
    private double xMax = 60;
    private double yMin = 0;
    private double yMax = 100000;
    private double scalefitX = -1;
    private double scalefitY = -1;
    private double xrangeMin = -1; //rangs dels eixos X i Y en 2theta/counts per calcular scalefitX,Y
    private double xrangeMax = -1;
    private double yrangeMin = -1;
    private double yrangeMax = -1;
    private Plot1d graphPanel;
    
    //parametres interaccio/contrast
    private boolean mouseBox = false;
    private boolean sqSelect = false;
    private boolean shiftPressed = false;
    private boolean mouseDrag = false;
    private boolean mouseMove = false;
    private boolean mouseZoom = false;
    private Rectangle2D.Double zoomRect;
    private Point2D.Double dragPoint;
    private Point2D.Double clickPoint;
    
    //LINIES DIVISIO
    double div_incXPrim, div_incXSec, div_incYPrim, div_incYSec;
    double div_startValX, div_startValY;
    boolean fixAxes = false;
    boolean autoDiv = true;
    boolean negativeYAxisLabels = true;
    
    //Parametres de visualitzacio llegenda
    boolean showLegend = true;
    boolean autoPosLegend = true;
    int legendX = -99;
    int legendY = -99;
    private boolean legend_opaque = true;
    private boolean hkllabels = true;
    private boolean showGridY = false;
    private boolean showGridX = false;
    boolean showPeakThreshold = false;
	boolean showEstimPointsBackground = false;
    boolean selectingBkgPoints = false;
    boolean deletingBkgPoints = false;
    boolean selectingPeaks = false;
    boolean deletingPeaks = false;
    boolean showDBCompound = false;
    boolean showDBCompoundIntensity = false;
    boolean splitDBCompound = false;
    boolean showIndexSolution = false;
    boolean applyScaleFactorT2 = false;
    float scaleFactorT2ang = 30;
    float scaleFactorT2fact = 1;
    
    //series "propies" i arraylist de les seleccionades -- AUXILIARY DATASERIES
    DataSerie bkgseriePeakSearch; //threshold del background
    DataSerie bkgEstimP; //threshold del background
    private DataSerie indexSolution;
    DataSerie dbCompound;
    private List<DataSerie> selectedSeries;
    int nTotalOpenedDatSeries;
    
    private JTextField txtXdiv;
    private JTextField txtYdiv;
    private JCheckBox chckbxFixedAxis;
    private JTextField txtXmin;
    private JTextField txtYmin;
    private JTextField txtNdivx;
    private JTextField txtNdivy;
    private JPanel statusPanel;
    private JLabel lblTth;
    private JLabel lblDsp;
    private JLabel lblHkl;

//    private D1Dplot_main mainframe;
    private JPanel panel;
    private JPanel panel_1;
    private JPanel plotPanel;
    private JPanel plotPanelContainer;
    private JTextField txtXminwin;
    private JTextField txtXmaxwin;
    private JTextField txtYminwin;
    private JTextField txtYmaxwin;
    private JLabel lblInten;
    private JScrollPane scrollPane;
    private JSplitPane splitPane;
    private JPanel buttons_panel;
    private JPanel panelStatusCursorInfo;
    private JButton btnFitWindowStatus;
    private JButton btnShowhideOpts;
    private JLabel lblXmin;
    private JLabel lblYmin_1;
    private JLabel lblInix;
    private JLabel lblIncx;
    private JLabel lblSubx;
    private JLabel lblIniy;
    private JLabel lblIncy;
    private JLabel lblSuby;
    private JLabel lblXTitle;
    private JLabel lblYTitle;
    private JLabel lblTheme;
    private JPanel panel_2;
    private JCheckBox chckbxShow;
    private JCheckBox chckbxAutopos;
    private JTextField txtXlegend;
    private JTextField txtYlegend;
    private JPanel panel_3;
    private JTextField txtXtitle;
    private JTextField txtYtitle;
    private JComboBox<String> comboTheme;
    private JCheckBox chckbxApply;
    private JLabel lblTIni;
    private JLabel lblFactor;
    private JTextField txtZonescalexini;
    private JTextField txtZonescalefactor;
    private JLabel lblGrid;
    private JCheckBox chckbxX;
    private JCheckBox chckbxY;
    private JCheckBox chckbxVerticalYAxis;
    private JCheckBox chckbxVerticalYLabel;
    private JCheckBox chckbxNegativeYLabels;
    private JCheckBox chckbxBkgIntensityprf;
    private JCheckBox chckbxHklLabels;
    private JButton btnReassigncolors;
    private JCheckBox chckbxDbCompoundIntensity;
    private JCheckBox chckbxSplit;
    private JCheckBox chckbxOpaque;
    
	/**
     * Create the panel.
     */
    public PlotPanel(Options opt) {
        this.readOptions(opt);
        this.plotPanel = new JPanel();
        this.plotPanel.setBackground(Color.WHITE);
        this.plotPanel.setLayout(new MigLayout("insets 0", "[grow]", "[grow]"));
        
        graphPanel = new Plot1d();
        graphPanel.setBackground(Color.WHITE);
        graphPanel.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent arg0) {
                do_graphPanel_mouseWheelMoved(arg0);
            }
        });
        graphPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent arg0) {
                do_graphPanel_mousePressed(arg0);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                do_graphPanel_mouseReleased(e);
            }
        });
        graphPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent arg0) {
                do_graphPanel_mouseDragged(arg0);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                do_graphPanel_mouseMoved(e);
            }
        });
        
//        this.plotPanel.add(graphPanel, "cell 0 0,grow");
        
        splitPane = new JSplitPane();
        splitPane.setResizeWeight(1.0);
        plotPanel.add(splitPane, "cell 0 0,grow");
//        splitPane.setLeftComponent(graphPanel);
        plotPanelContainer = new JPanel();
        splitPane.setLeftComponent(plotPanelContainer); //for design
        plotPanelContainer.setLayout(new MigLayout("insets 0", "[grow]", "[grow][]"));
        plotPanelContainer.add(graphPanel, "cell 0 0,grow");
        
        panelStatusCursorInfo = new JPanel();
        plotPanelContainer.add(panelStatusCursorInfo, "cell 0 1,grow");
        panelStatusCursorInfo.setLayout(new MigLayout("insets 1 n 1 n", "[][][left][grow][][][]", "[]"));
        
        lblTth = new JLabel("X");
        panelStatusCursorInfo.add(lblTth, "cell 0 0");
        lblTth.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 12));
        
        lblInten = new JLabel("Y");
        panelStatusCursorInfo.add(lblInten, "cell 1 0");
        
        lblDsp = new JLabel("dsp");
        panelStatusCursorInfo.add(lblDsp, "cell 2 0");
        
        btnFitWindowStatus = new JButton("FitV");
        btnFitWindowStatus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_btnFitWindowStatus_actionPerformed(e);
            }
        });
        
        lblHkl = new JLabel("hkl");
        panelStatusCursorInfo.add(lblHkl, "cell 3 0,alignx left");
        btnFitWindowStatus.setToolTipText("Reset View");
        panelStatusCursorInfo.add(btnFitWindowStatus, "cell 4 0");
        
        btnShowhideOpts = new JButton("LateralOpts");
        btnShowhideOpts.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_btnShowhideOpts_actionPerformed(e);
            }
        });
        
        btnReassigncolors = new JButton("reassignColors");
        btnReassigncolors.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_btnReassigncolors_actionPerformed(e);
            }
        });
        panelStatusCursorInfo.add(btnReassigncolors, "cell 5 0");
        panelStatusCursorInfo.add(btnShowhideOpts, "cell 6 0");
        
        buttons_panel = new JPanel();
        buttons_panel.setPreferredSize(new Dimension(245, 300));
        buttons_panel.setMinimumSize(new Dimension(5, 100));
        splitPane.setRightComponent(buttons_panel);
        buttons_panel.setLayout(new MigLayout("insets 0", "[grow]", "[grow]"));
        
        scrollPane = new JScrollPane();
        scrollPane.setViewportBorder(new EmptyBorder(0, 0, 0, 0));
        buttons_panel.add(scrollPane, "cell 0 0,grow");
        
        statusPanel = new JPanel();
        scrollPane.setViewportView(statusPanel);
        statusPanel.setLayout(new MigLayout("insets 0", "[][grow]", "[][][][][][][]"));
        
        lblXTitle = new JLabel("X title");
        statusPanel.add(lblXTitle, "cell 0 0,alignx trailing");
        
        txtXtitle = new JTextField();
        txtXtitle.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtXtitle_actionPerformed(e);
            }
        });
        txtXtitle.setText("xtitle");
        statusPanel.add(txtXtitle, "cell 1 0,growx");
        txtXtitle.setColumns(5);
        
        lblYTitle = new JLabel("Y title");
        statusPanel.add(lblYTitle, "cell 0 1,alignx trailing");
        
        txtYtitle = new JTextField();
        txtYtitle.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtYtitle_actionPerformed(e);
            }
        });
        txtYtitle.setText("ytitle");
        statusPanel.add(txtYtitle, "cell 1 1,growx");
        txtYtitle.setColumns(5);
        
        lblTheme = new JLabel("Theme");
        statusPanel.add(lblTheme, "cell 0 2,alignx trailing");
        
        comboTheme = new JComboBox<String>();
        comboTheme.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                do_comboTheme_itemStateChanged(e);
            }
        });
        comboTheme.setModel(new DefaultComboBoxModel<String>(new String[] {"Light", "Dark"}));
        statusPanel.add(comboTheme, "cell 1 2,growx");
        
        panel_2 = new JPanel();
        panel_2.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Legend", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        statusPanel.add(panel_2, "cell 0 3 2 1,grow");
        panel_2.setLayout(new MigLayout("insets 2", "[][grow][grow]", "[][]"));
        
        chckbxShow = new JCheckBox("Show");
        chckbxShow.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                do_chckbxShow_itemStateChanged(e);
            }
        });
        panel_2.add(chckbxShow, "cell 0 0,alignx left");
        
        txtXlegend = new JTextField();
        txtXlegend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtXlegend_actionPerformed(e);
            }
        });
        
        chckbxAutopos = new JCheckBox("Auto");
        chckbxAutopos.setToolTipText("Automatic Position");
        chckbxAutopos.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                do_chckbxAutopos_itemStateChanged(e);
            }
        });
        
        chckbxOpaque = new JCheckBox("Opaque");
        chckbxOpaque.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                do_chckbxOpaque_itemStateChanged(e);
            }
        });
        panel_2.add(chckbxOpaque, "cell 1 0 2 1");
        panel_2.add(chckbxAutopos, "cell 0 1,alignx left");
        txtXlegend.setText("Xlegend");
        panel_2.add(txtXlegend, "cell 1 1,growx");
        txtXlegend.setColumns(2);
        
        txtYlegend = new JTextField();
        txtYlegend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtYlegend_actionPerformed(e);
            }
        });
        txtYlegend.setText("Ylegend");
        panel_2.add(txtYlegend, "cell 2 1,growx");
        txtYlegend.setColumns(2);
        
        panel_3 = new JPanel();
        panel_3.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Zone scale", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        statusPanel.add(panel_3, "cell 0 4 2 1,grow");
        panel_3.setLayout(new MigLayout("insets 2", "[][][grow][][grow]", "[]"));
        
        chckbxApply = new JCheckBox("");
        chckbxApply.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                do_chckbxApply_itemStateChanged(e);
            }
        });
        panel_3.add(chckbxApply, "cell 0 0,alignx left");
        
        lblTIni = new JLabel("Xini");
        panel_3.add(lblTIni, "cell 1 0,alignx trailing");
        
        txtZonescalexini = new JTextField();
        txtZonescalexini.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                do_txtZonescalexini_keyReleased(e);
            }
        });
        txtZonescalexini.setText("zoneScaleXini");
        panel_3.add(txtZonescalexini, "cell 2 0,growx");
        txtZonescalexini.setColumns(5);
        
        lblFactor = new JLabel("Fac");
        panel_3.add(lblFactor, "cell 3 0,alignx trailing");
        
        txtZonescalefactor = new JTextField();
        txtZonescalefactor.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                do_txtZonescalefactor_keyReleased(e);
            }
        });
        txtZonescalefactor.setText("zoneScaleFactor");
        panel_3.add(txtZonescalefactor, "cell 4 0,growx");
        txtZonescalefactor.setColumns(5);
        
        panel_1 = new JPanel();
        statusPanel.add(panel_1, "cell 0 5 2 1,growx");
        panel_1.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "X,Y window", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_1.setLayout(new MigLayout("insets 2", "[][grow][grow][grow]", "[][][][][][]"));
        
        txtXminwin = new JTextField();
        txtXminwin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtXminwin_actionPerformed(e);
            }
        });
        
        lblXmin = new JLabel("Xmin max");
        panel_1.add(lblXmin, "cell 0 0,alignx trailing");
        panel_1.add(txtXminwin, "cell 1 0 2 1,growx");
        txtXminwin.setColumns(5);
        
        txtXmaxwin = new JTextField();
        txtXmaxwin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtXmaxwin_actionPerformed(e);
            }
        });
        panel_1.add(txtXmaxwin, "cell 3 0,growx");
        txtXmaxwin.setColumns(5);
        
        lblYmin_1 = new JLabel("Ymin max");
        panel_1.add(lblYmin_1, "cell 0 1,alignx trailing");
        
        txtYminwin = new JTextField();
        txtYminwin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtYminwin_actionPerformed(e);
            }
        });
        panel_1.add(txtYminwin, "cell 1 1 2 1,growx");
        txtYminwin.setColumns(5);
        
        txtYmaxwin = new JTextField();
        txtYmaxwin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtYmaxwin_actionPerformed(e);
            }
        });
        panel_1.add(txtYmaxwin, "cell 3 1,growx");
        txtYmaxwin.setColumns(5);
        
        lblGrid = new JLabel("Grid");
        panel_1.add(lblGrid, "cell 0 2,alignx trailing");
        
        chckbxX = new JCheckBox("X");
        chckbxX.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                do_chckbxX_itemStateChanged(e);
            }
        });
        panel_1.add(chckbxX, "cell 1 2,alignx center");
        
        chckbxVerticalYAxis = new JCheckBox("Show Y axis");
        chckbxVerticalYAxis.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                do_chckbxVerticalYAxis_itemStateChanged(e);
            }
        });
        
        chckbxY = new JCheckBox("Y");
        chckbxY.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                do_chckbxY_itemStateChanged(e);
            }
        });
        panel_1.add(chckbxY, "cell 2 2 2 1,alignx left");
        panel_1.add(chckbxVerticalYAxis, "cell 0 3 2 1,alignx left");
        
        chckbxBkgIntensityprf = new JCheckBox("Add bkg (PRF)");
        chckbxBkgIntensityprf.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                do_chckbxBkgIntensityprf_itemStateChanged(e);
            }
        });
        
        chckbxVerticalYLabel = new JCheckBox("Vert. label");
        chckbxVerticalYLabel.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                do_chckbxVerticalYLabel_itemStateChanged(e);
            }
        });
        panel_1.add(chckbxVerticalYLabel, "cell 2 3 2 1,alignx left");
        panel_1.add(chckbxBkgIntensityprf, "cell 0 4 2 1");
        
        chckbxHklLabels = new JCheckBox("HKL labels");
        chckbxHklLabels.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                do_chckbxHklLabels_itemStateChanged(e);
            }
        });
        panel_1.add(chckbxHklLabels, "cell 2 4 2 1");
        
        chckbxDbCompoundIntensity = new JCheckBox("DB ref intensity");
        chckbxDbCompoundIntensity.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                do_chckbxDbCompoundIntensity_itemStateChanged(e);
            }
        });
        panel_1.add(chckbxDbCompoundIntensity, "cell 0 5 3 1");
        
        chckbxSplit = new JCheckBox("Split");
        chckbxSplit.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                do_chckbxSplit_itemStateChanged(e);
            }
        });
        panel_1.add(chckbxSplit, "cell 3 5");
        
        panel = new JPanel();
        statusPanel.add(panel, "cell 0 6 2 1,growx");
        panel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Axes divisions", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.setLayout(new MigLayout("insets 2", "[][grow][][grow]", "[][][][]"));
        
        lblInix = new JLabel("iniX");
        lblInix.setToolTipText("initial value X axis");
        panel.add(lblInix, "cell 0 0,alignx trailing");
        
        txtXmin = new JTextField();
        panel.add(txtXmin, "cell 1 0,growx");
        txtXmin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtXmin_actionPerformed(e);
            }
        });
        txtXmin.setText("Xmin");
        txtXmin.setColumns(3);
        
        lblIniy = new JLabel("iniY");
        lblIniy.setToolTipText("initial value Y axis");
        panel.add(lblIniy, "cell 2 0,alignx trailing");
        
        txtYmin = new JTextField();
        panel.add(txtYmin, "cell 3 0,growx");
        txtYmin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtYmin_actionPerformed(e);
            }
        });
        txtYmin.setText("ymin");
        txtYmin.setColumns(3);
        
        lblIncx = new JLabel("incX");
        lblIncx.setToolTipText("increment X major ticks");
        panel.add(lblIncx, "cell 0 1,alignx trailing");
        
        txtXdiv = new JTextField();
        txtXdiv.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtXdiv_actionPerformed(e);
            }
        });
        panel.add(txtXdiv, "cell 1 1,growx");
        txtXdiv.setText("xdiv");
        txtXdiv.setColumns(3);
        
        lblIncy = new JLabel("incY");
        lblIncy.setToolTipText("increment Y major ticks");
        panel.add(lblIncy, "cell 2 1,alignx trailing");
        
        txtYdiv = new JTextField();
        txtYdiv.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtYdiv_actionPerformed(e);
            }
        });
        panel.add(txtYdiv, "cell 3 1,growx");
        txtYdiv.setText("ydiv");
        txtYdiv.setColumns(3);
        
        lblSubx = new JLabel("divX");
        lblSubx.setToolTipText("subdivisions in X axis");
        panel.add(lblSubx, "cell 0 2,alignx trailing");
        
        txtNdivx = new JTextField();
        txtNdivx.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtNdivx_actionPerformed(e);
            }
        });
        panel.add(txtNdivx, "cell 1 2,growx");
        txtNdivx.setText("NdivX");
        txtNdivx.setColumns(3);
        
        lblSuby = new JLabel("divY");
        lblSuby.setToolTipText("subdivisions in Y axis");
        panel.add(lblSuby, "cell 2 2,alignx trailing");
        
        txtNdivy = new JTextField();
        txtNdivy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtNdivy_actionPerformed(e);
            }
        });
        panel.add(txtNdivy, "cell 3 2,growx");
        txtNdivy.setText("NdivY");
        txtNdivy.setColumns(3);
        
        chckbxFixedAxis = new JCheckBox("Fix Axes");
        panel.add(chckbxFixedAxis, "cell 0 3 2 1,alignx left");
        chckbxFixedAxis.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                do_chckbxFixedAxis_itemStateChanged(e);
            }
        });
        
        chckbxNegativeYLabels = new JCheckBox("Neg. Y labels");
        chckbxNegativeYLabels.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                do_chckbxNegativeYLabels_itemStateChanged(e);
            }
        });
        panel.add(chckbxNegativeYLabels, "cell 2 3 2 1");

        
        inicia();
    }

    protected void do_btnShowhideOpts_actionPerformed(ActionEvent e) {
        this.showHideButtonsPanel();
    }
    
    public void showHideButtonsPanel() {
        int iconheight = 16;
        if (this.buttons_panel.isVisible()) {
            splitPanePosition=splitPane.getDividerLocation();    
            splitPanePosition = splitPanePosition/(double)splitPane.getWidth();
            this.buttons_panel.setVisible(false);
            //icona i tooltip per MOSTRAR
            Image LAT = new ImageIcon(D1Dplot_main.class.getResource("/com/vava33/d1dplot/img/showLateral.png")).getImage().getScaledInstance(-100, iconheight, java.awt.Image.SCALE_SMOOTH);
            btnShowhideOpts.setIcon(new ImageIcon(LAT));

        }else {
            //recuperem
            this.buttons_panel.setVisible(true);
            if(splitPanePosition<0) {
                int posBut = splitPane.getWidth() - buttons_panel.getPreferredSize().width;
                splitPanePosition = posBut/(double)splitPane.getWidth();
                if (splitPanePosition<0)splitPanePosition=0.85;
            }
            splitPane.setDividerLocation(splitPanePosition);
            Image LAT = new ImageIcon(D1Dplot_main.class.getResource("/com/vava33/d1dplot/img/hideLateral.png")).getImage().getScaledInstance(-100, iconheight, java.awt.Image.SCALE_SMOOTH);
            btnShowhideOpts.setIcon(new ImageIcon(LAT));
        }
    }
    
    
    private void inicia(){
        nTotalOpenedDatSeries = 0;
        this.dataToPlot = new ArrayList<Plottable>();
        this.selectedSeries = new ArrayList<DataSerie>();
        
        bkgseriePeakSearch=new DataSerie(SerieType.bkg,Xunits.tth,null); // millorable
        bkgEstimP=new DataSerie(SerieType.bkgEstimP,Xunits.tth,null);// millorable
        
        div_incXPrim = 0;
        div_incXSec = 0;
        div_incYPrim = 0;
        div_incYSec = 0;
        div_startValX = 0;
        div_startValY = 0;

        this.txtNdivx.setText("");
        this.txtNdivy.setText("");
        this.txtXdiv.setText("");
        this.txtXmin.setText("");
        this.txtYdiv.setText("");
        this.txtYmin.setText("");
        
//        fixAxes = chckbxFixedAxis.isSelected();
        if (!fixAxes) {
            txtNdivx.setEditable(false);
            txtNdivy.setEditable(false);
            txtXdiv.setEditable(false);
            txtYdiv.setEditable(false);
        }
        
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {

            @Override
            public boolean dispatchKeyEvent(KeyEvent ke) {
                    switch (ke.getID()) {
                    case KeyEvent.KEY_PRESSED:
                        if (ke.getKeyCode() == KeyEvent.VK_SHIFT) {
                            shiftPressed = true;
                        }
                        break;

                    case KeyEvent.KEY_RELEASED:
                        if (ke.getKeyCode() == KeyEvent.VK_SHIFT) {
                            shiftPressed = false;
                        }
                        break;
                    }
                    return false;
            }
        });
        int iconheight = 16;
        int inset = 2;
        int buttonSize = iconheight+2*inset;
        Image FIT = new ImageIcon(D1Dplot_main.class.getResource("/com/vava33/d1dplot/img/fitWindow.png")).getImage().getScaledInstance(-100, iconheight, java.awt.Image.SCALE_SMOOTH);
        btnFitWindowStatus.setText("");
        btnFitWindowStatus.setIcon(new ImageIcon(FIT));
        Image LAT = new ImageIcon(D1Dplot_main.class.getResource("/com/vava33/d1dplot/img/hideLateral.png")).getImage().getScaledInstance(-100, iconheight, java.awt.Image.SCALE_SMOOTH);
        btnShowhideOpts.setText("");
        btnShowhideOpts.setIcon(new ImageIcon(LAT));
        Image COL = new ImageIcon(D1Dplot_main.class.getResource("/com/vava33/d1dplot/img/reassignColors.png")).getImage().getScaledInstance(-100, iconheight, java.awt.Image.SCALE_SMOOTH);
        btnReassigncolors.setText("");
        btnReassigncolors.setIcon(new ImageIcon(COL));
        
        btnFitWindowStatus.setPreferredSize(new Dimension(buttonSize,buttonSize));
        btnFitWindowStatus.setMaximumSize(new Dimension(buttonSize,buttonSize));
        btnFitWindowStatus.setMinimumSize(new Dimension(buttonSize,buttonSize));
        btnFitWindowStatus.setMargin(new Insets(inset,inset,inset,inset));
        btnShowhideOpts.setToolTipText("Reset View (fit patterns to window)");
        btnShowhideOpts.setPreferredSize(new Dimension(buttonSize,buttonSize));
        btnShowhideOpts.setMaximumSize(new Dimension(buttonSize,buttonSize));
        btnShowhideOpts.setMinimumSize(new Dimension(buttonSize,buttonSize));
        btnShowhideOpts.setMargin(new Insets(inset,inset,inset,inset));
        btnShowhideOpts.setToolTipText("Show/Hide Lateral Options Panel");
        btnReassigncolors.setPreferredSize(new Dimension(buttonSize,buttonSize));
        btnReassigncolors.setMaximumSize(new Dimension(buttonSize,buttonSize));
        btnReassigncolors.setMinimumSize(new Dimension(buttonSize,buttonSize));
        btnReassigncolors.setMargin(new Insets(inset,inset,inset,inset));
        btnReassigncolors.setToolTipText("Reassign colors to patterns");
//        log.debug("end plotpanel inicia");
        
        //HEM DE POSAR BE ELS CHECKBOX DEL LATERAL AMB ELS VALORS CORRECTES
        if (this.isLightTheme()) {
            comboTheme.setSelectedIndex(0);
        }else {
            comboTheme.setSelectedIndex(1);
        }
        this.txtXtitle.setText(this.xlabel);
        this.txtYtitle.setText(this.ylabel);
        this.chckbxShow.setSelected(this.isShowLegend());
        this.chckbxAutopos.setSelected(this.autoPosLegend);
        this.txtXlegend.setText("X coord");
        this.txtYlegend.setText("Y coord");
        this.txtZonescalexini.setText(FileUtils.dfX_2.format(scaleFactorT2ang));
        this.txtZonescalefactor.setText(FileUtils.dfX_2.format(scaleFactorT2fact));
        this.chckbxX.setSelected(this.showGridX);
        this.chckbxY.setSelected(this.showGridY);
        this.chckbxShow.setSelected(this.showLegend);
        this.chckbxVerticalYAxis.setSelected(this.verticalYAxe);
        this.chckbxVerticalYLabel.setSelected(this.verticalYlabel);
        this.chckbxBkgIntensityprf.setSelected(this.plotwithbkg);
        this.chckbxHklLabels.setSelected(this.hkllabels);
        this.chckbxFixedAxis.setSelected(this.fixAxes);
        this.chckbxNegativeYLabels.setSelected(this.negativeYAxisLabels);
        this.chckbxDbCompoundIntensity.setSelected(this.showDBCompoundIntensity);
        this.chckbxOpaque.setSelected(this.legend_opaque);
    }
    
	public JPanel getPlotPanel() {
		return plotPanel;
	}
	
	private void assignColorDataSeriesIfNecessary(Plottable p) {
	    for (DataSerie ds: p.getDataSeries()) {
	        //nomes visibles
	        if (!ds.plotThis)continue;
	        switch (ds.getTipusSerie()){
	        case dat:
	            this.paintIt(ds);
	            break;
	        case gr:
	            this.paintIt(ds);
	            break;
	        default:
	            break;
	        }
	    }
	}
	
	public void addPlottable(Plottable p) {
	  //nomes pintarem les series DAT
	    this.assignColorDataSeriesIfNecessary(p);
	    this.dataToPlot.add(p);
	    this.actualitzaPlot(); //TODO: sempre es voldrà? ho posem com a opcio millor? posar-ho com a opcio? ja que això fa que es repeteixi molts cops per duplicat
	}
	
	   public void reassignColorPatterns(){ //nomes es per tipusserieDAT
	       nTotalOpenedDatSeries=0;
	        for (Plottable p:this.dataToPlot) {
	            this.assignColorDataSeriesIfNecessary(p);
	        }
	        this.actualitzaPlot();
	    }

	   private void paintIt(DataSerie ds) {
	       if (isLightTheme()){
	           int ncol = (nTotalOpenedDatSeries)%D1Dplot_global.lightColors.length;
	           ds.color=FileUtils.parseColorName(D1Dplot_global.lightColors[ncol]);    
	       }else {
	           int ncol = (nTotalOpenedDatSeries)%D1Dplot_global.DarkColors.length;
	           ds.color=FileUtils.parseColorName(D1Dplot_global.DarkColors[ncol]);
	       }
	       nTotalOpenedDatSeries++;
	   }
	   
	public void removePlottable(Plottable p) {
	    this.dataToPlot.remove(p);
	    this.actualitzaPlot();
	}
	public void removePlottables(List<Plottable> p) {
	    this.dataToPlot.removeAll(p);
	    this.actualitzaPlot();
	}
	public void removePlottable(int index) {
        this.dataToPlot.remove(index);
        this.actualitzaPlot();
    }
	   public void removeAllPlottables() {
	        this.dataToPlot.clear();
	        this.actualitzaPlot();
	    }
	
	public Plottable getPlottable(int index) {
	    return this.dataToPlot.get(index);
	}
	public int getNplottables() {
	    return this.dataToPlot.size();
	}
	
	public void swapPlottables(int orig, int dest) {
	    Collections.swap(dataToPlot, orig, dest);
	}
	
	public DataSerie getFirstPlottedSerie() {
		try {
		    for (Plottable p:dataToPlot) {
		        for (DataSerie ds:p.getDataSeries()) {
		            if (ds.plotThis)return ds;
		        }
		    }
		}catch(Exception ex) {
			log.warning("Error getting first plotted serie");
		}
		return null; //or first non plotted? --> return this.dataToPlot.get(0).getDataSerie();
	}
	
	public DataSerie getFirstSelectedSerie() {
	    return selectedSeries.get(0);
	}
	
	public Plottable getFirstPlottedPlottable() {
	    try {
	        for (Plottable p:dataToPlot) {
	            for (DataSerie ds:p.getDataSeries()) {
	                if (ds.plotThis)return p;
	            }
	        }
	    }catch(Exception ex) {
            log.warning("Error getting first plotted element");
	    }
	    return null; //or first non plotted? --> return this.dataToPlot.get(0).getDataSerie();
	}

	public Plottable getFirstSelectedPlottable() {
        return selectedSeries.get(0).getParent();
    }
	
	public int indexOfPlottableData(Plottable p) {
	    return dataToPlot.indexOf(p);
	}

    public void replacePlottable(int index, Plottable newPlottable) {
        if(this.dataToPlot.get(index).getNSeries()==1) {
            newPlottable.getDataSerie(0).color=this.dataToPlot.get(index).getDataSerie(0).color;
        }
	    this.dataToPlot.set(index, newPlottable);
	    this.actualitzaPlot();
	}
	
	public boolean isOneSerieSelected(){
	    if (this.selectedSeries.isEmpty()){
	        log.warning("Select a serie first");
	        return false;
	    }
	    if (this.selectedSeries.size()>1){
	        log.warning("Select ONE serie only");
	        return false;
	    }
	    return true;
	}

	public boolean arePlottables(){
	    return !this.dataToPlot.isEmpty();
	}
	
	public void cleanEmptyPlottables() {
	    for(Iterator<Plottable> it = dataToPlot.iterator(); it.hasNext(); ) {
	        if (it.next().getNSeries()<=0) it.remove();
	    }
	}
	
    public void actualitzaPlot() {
		this.graphPanel.repaint();
	}
	    
	    // ajusta la imatge al panell, mostrant-la tota sencera (calcula l'scalefit inicial)
	public void fitGraph() {
	    this.resetView(false);
	}
	
	private void resetView(boolean resetAxes) {
	    if (arePlottables()) {
	        this.calcMaxMinXY();
	        this.xrangeMax=this.xMax;
	        this.xrangeMin=this.xMin;
	        this.yrangeMax=this.yMax;
	        this.yrangeMin=this.yMin;

	        this.calcScaleFitX();
	        this.calcScaleFitY();

	        if (!checkIfDiv() || resetAxes){
	            this.autoDivLines(false);
	        }

	        this.actualitzaPlot();
	    }
	}
    
    //CAL COMPROVAR QUE ESTIGUI DINS DEL RANG PRIMER I CORREGIR l'OFFSET sino torna NULL
    private Point2D.Double getFramePointFromDataPoint(Plottable_point dpoint){
          return new Point2D.Double(this.getFrameXFromDataPointX(dpoint.getX()),this.getFrameYFromDataPointY(dpoint.getY()));
            
    }
    
    private double getFrameXFromDataPointX(double xdpoint){
          double xfr = ((xdpoint-this.xrangeMin) * this.scalefitX) + gapAxisLeft;
          return xfr;    
    }
    
    private double getFrameYFromDataPointY(double ydpoint){
        double yfr = graphPanel.getHeight()-(((ydpoint-this.yrangeMin) * this.scalefitY) + gapAxisBottom);
        return yfr;    
  }
    

    private Point2D.Double getDataPointFromFramePoint(Point2D.Double framePoint){
        if (isFramePointInsideGraphArea(framePoint)){
            double xdp = ((framePoint.x - gapAxisLeft) / this.scalefitX) + this.xrangeMin;
            double ydp = (-framePoint.y+graphPanel.getHeight()-gapAxisBottom)/this.scalefitY +this.yrangeMin;
            return new Point2D.Double(xdp,ydp);
        }else{
            return null;
        }
    }
    
    private Point2D.Double getDataPointFromFramePointIgnoreIfInside(Point2D.Double framePoint){
            double xdp = ((framePoint.x - gapAxisLeft) / this.scalefitX) + this.xrangeMin;
            double ydp = (-framePoint.y+graphPanel.getHeight()-gapAxisBottom)/this.scalefitY +this.yrangeMin;
            return new Point2D.Double(xdp,ydp);
    }
    
    private Plottable_point getDataPointDPFromFramePoint(Point2D.Double framePoint){
        if (isFramePointInsideGraphArea(framePoint)){
            double xdp = ((framePoint.x - gapAxisLeft) / this.scalefitX) + this.xrangeMin;
            double ydp = (-framePoint.y+graphPanel.getHeight()-gapAxisBottom)/this.scalefitY +this.yrangeMin;
            return new DataPoint(xdp,ydp,0);
        }else{
            return null;
        }
    }
    
    //ens diu quant en unitats de X val un pixel (ex 1 pixel es 0.01deg de 2th)
    private double getXunitsPerPixel(){
        return (this.xrangeMax-this.xrangeMin)/this.getRectangleGraphArea().width;
    }
    
    //ens dira quant en unitats de Y val un pixels (ex. 1 pixel son 1000 counts)
    private double getYunitsPerPixel(){
        return (this.yrangeMax-this.yrangeMin)/this.getRectangleGraphArea().height;
    }
    
    private boolean isFramePointInsideGraphArea(Point2D.Double p){
        Rectangle2D.Double r = getRectangleGraphArea();
        return r.contains(p);
    }
    
    private boolean isFramePointInsideXGraphArea(double px) {
        double x_low = gapAxisLeft;
        double x_high = x_low + this.calcPlotSpaceX();
        if ((px>x_low)&&(px<x_high))return true;
        return false;
    }
    
    public boolean isXValueInsideGraphArea(double xvalue) {
        return isFramePointInsideXGraphArea(this.getFrameXFromDataPointX(xvalue));
    }
    
    private Rectangle2D.Double getRectangleGraphArea(){
        double xtop = gapAxisLeft;
        double ytop = gapAxisTop;
        return new Rectangle2D.Double(xtop,ytop,calcPlotSpaceX(),calcPlotSpaceY());
    }
    
    //it returns the same value if it is inside the graph area, otherwise it returns the closest point on the border
    private double checkFrameXValue(double xval){
        Rectangle2D.Double rect = this.getRectangleGraphArea();
        if (xval>rect.getMinX() && xval<rect.getMaxX()){ //no poso <= >= per evitar anar justos...
            return xval;
        }else{
            if (xval<rect.getMinX())return rect.getMinX()+1;
            if (xval>rect.getMaxX())return rect.getMaxX()-1;
        }
        return xval;
    }
    
    //it returns the same value if it is inside the graph area, otherwise it returns the closest point on the border
    private double checkFrameYValue(double yval){
        Rectangle2D.Double rect = this.getRectangleGraphArea();
        if (yval>rect.getMinY() && yval<rect.getMaxY()){ //no poso <= >= per evitar anar justos...
            return yval;
        }else{
            if (yval<rect.getMinY())return rect.getMinY()+1;
            if (yval>rect.getMaxY())return rect.getMaxY()-1;
        }
        return yval;
    }
    
    //NOMES S'HAURIA DE CRIDAR QUAN OBRIM UN PATTERN (per aixo private) ..  el useRange es per quan cridem des de fixAxes
    private void autoDivLines(boolean useRange){
        
        //Aqui hauriem de posar divisions tal com volem des de MIN a MAX (ignorant finestra), després ja mostrarem la zona d'interès.
        
        if (useRange) {
            this.div_startValX=this.xrangeMin;
            this.div_startValY=this.yrangeMin;            
        }else { //"normal"
            this.div_startValX=this.xMin;
            this.div_startValY=this.yMin;
        }
        
        //ara cal veure a quan es correspon en les unitats de cada eix -- a la vista actual 
        double xppix = this.getXunitsPerPixel();
        double yppix = this.getYunitsPerPixel();
                
        txtNdivx.setText(String.valueOf(incXPrimPIXELS/incXSecPIXELS));
        txtNdivy.setText(String.valueOf(incYPrimPIXELS/incYSecPIXELS));
        
        this.div_incXPrim=incXPrimPIXELS*xppix;
        this.div_incXSec=incXSecPIXELS*xppix;
        this.div_incYPrim=incYPrimPIXELS*yppix;
        this.div_incYSec=incYSecPIXELS*yppix;
        
        this.txtXdiv.setText(FileUtils.dfX_3.format(this.div_incXPrim));
        this.txtYdiv.setText(FileUtils.dfX_3.format(this.div_incYPrim));
        this.txtXmin.setText(FileUtils.dfX_3.format(this.div_startValX));
        this.txtYmin.setText(FileUtils.dfX_3.format(this.div_startValY));
    }
    
    //valor inicial, valor d'increment per les separacions principals (tindran número), n divisions secundaries entre principals
    //iniVal l'hem suprimit d'aqui, la "finestra" no es responsabilitat d'aquesta funcio
    private void customDivLinesX(double incrPrincipals, double nDivisionsSecund){
        
//        double currentXIni = this.xrangeMin;
//
//        this.div_startValX=currentXIni;
//        this.xrangeMin=currentXIni;
        
//        this.div_startValX=xMin;
        this.div_incXPrim=incrPrincipals;
        this.div_incXSec=incrPrincipals/nDivisionsSecund;
        
        this.txtXdiv.setText(FileUtils.dfX_3.format(this.div_incXPrim));
        
   }
    
    private void customDivLinesY(double incrPrincipals, double nDivisionsSecund){
//        double currentYIni = this.yrangeMin;
//
//        this.div_startValY=currentYIni;
//        this.yrangeMin=currentYIni;
        
//        this.div_startValY=yMin;
        this.div_incYPrim=incrPrincipals;
        this.div_incYSec=incrPrincipals/nDivisionsSecund;
                
        this.txtYdiv.setText(FileUtils.dfX_3.format(this.div_incYPrim));
    }

    
    //ens diu si s'han calculat els limits (o s'han assignat) per les linies de divisio
    private boolean checkIfDiv(){
        if (this.div_incXPrim == 0) return false;
        if (this.div_incXSec == 0) return false;
        if (this.div_incYPrim == 0) return false;
        if (this.div_incYSec == 0) return false;
        return true;
    }
    
    private void calcMaxMinXY(){
        double maxX = Double.MIN_VALUE;
        double minX = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        boolean thereIsHKL =false;
        int hklsize = DataSerie.def_hklticksize;
        for(Plottable p:dataToPlot) {
            //            DataSerie ds = p.getMainSerie();
            for (DataSerie ds:p.getDataSeries()) {
                if (!ds.plotThis) continue;
                double[] MxXMnXMxYMnY = ds.getPuntsMaxXMinXMaxYMinY();

                if (MxXMnXMxYMnY[0]>maxX) maxX = MxXMnXMxYMnY[0];
                if (MxXMnXMxYMnY[1]<minX) minX = MxXMnXMxYMnY[1];
                if (MxXMnXMxYMnY[2]>maxY) maxY = MxXMnXMxYMnY[2];
                if (MxXMnXMxYMnY[3]<minY) minY = MxXMnXMxYMnY[3];
                
                if (ds.getTipusSerie()==SerieType.hkl) {
                    hklsize=(int) ds.getScale();
                    thereIsHKL=true;
                }
            }
            if (this.showIndexSolution) {//per mostrar els hkl
                if (this.indexSolution!=null) {
                    thereIsHKL=true;       
                }
            }
        }
        
        if (thereIsHKL) {
            double newYframe = this.getFrameYFromDataPointY(minY)-hklsize;
            double newYdata = this.getDataPointFromFramePointIgnoreIfInside(new Point2D.Double(0, newYframe)).getY();
            minY = FastMath.min(minY, newYdata);
            
        }
        
        this.xMax=maxX;
        this.xMin=minX;
        this.yMax=maxY;
        this.yMin=minY;
    }
    
    //height in pixels of the plot area
    private double calcPlotSpaceY(){
        return graphPanel.getHeight()-gapAxisTop-gapAxisBottom;
    }
    //width in pixels of the plot area
    private double calcPlotSpaceX(){
        return graphPanel.getWidth()-gapAxisLeft-gapAxisRight;
    }
    //escala en Y per encabir el rang que s'ha de plotejar
    private void calcScaleFitY(){
        scalefitY = calcPlotSpaceY()/(this.yrangeMax-this.yrangeMin);
    }
    //escala en X per encabir el rang que s'ha de plotejar
    private void calcScaleFitX(){
        scalefitX = calcPlotSpaceX()/(this.xrangeMax-this.xrangeMin);
    }
    
    // FARE ZOOM NOMES EN Y?
    private void zoomY(boolean zoomIn, Point2D.Double centre) {
        Point2D.Double dpcentre = this.getDataPointFromFramePoint(centre); // miro a quin punt de dades estem fent zoom
        if (dpcentre == null)return;
        if (zoomIn) {
            this.yrangeMax=this.yrangeMax*(1/facZoom);
        } else {
            this.yrangeMax=this.yrangeMax*(facZoom);
        }
        calcScaleFitY();
        this.actualitzaPlot();
    }

    private void zoomX(boolean zoomIn, double inc) {
        if (zoomIn) {
            this.xrangeMin=this.xrangeMin+(inc/scalefitX);
            this.xrangeMax=this.xrangeMax-(inc/scalefitX);
        } else {
            this.xrangeMin=this.xrangeMin-(inc/scalefitX);
            this.xrangeMax=this.xrangeMax+(inc/scalefitX);
        }
        calcScaleFitX();
     }
    
    private void scrollX(double inc) {
        this.xrangeMin=this.xrangeMin+(inc/scalefitX);
        this.xrangeMax=this.xrangeMax+(inc/scalefitX);
        calcScaleFitX();
    }
    
    //es mouen en consonancia els limits de rang x i y
	private void movePattern(double incX, double incY){//, boolean repaint) {
	    this.xrangeMin=this.xrangeMin-(incX/scalefitX);
	    this.xrangeMax=this.xrangeMax-(incX/scalefitX);
	    this.yrangeMin=this.yrangeMin+(incY/scalefitY);
	    this.yrangeMax=this.yrangeMax+(incY/scalefitY);
	    this.calcScaleFitX();
	    this.calcScaleFitY();
	    
	}

	private Point2D.Double getIntersectionPoint(Line2D.Double line1, Line2D.Double line2) {
        if (! line1.intersectsLine(line2) ) return null;
          double px = line1.getX1(),
                py = line1.getY1(),
                rx = line1.getX2()-px,
                ry = line1.getY2()-py;
          double qx = line2.getX1(),
                qy = line2.getY1(),
                sx = line2.getX2()-qx,
                sy = line2.getY2()-qy;

          double det = sx*ry - sy*rx;
          if (det == 0) {
            return null;
          } else {
            double z = (sx*(qy-py)+sy*(px-qx))/det;
            if (z==0 ||  z==1) return null;  // intersection at end point!
            return new Point2D.Double((px+z*rx), (py+z*ry));
          }
     } // end intersection line-line
    
    private Point2D.Double[] getIntersectionPoint(Line2D.Double line, Rectangle2D rectangle) {

        Point2D.Double[] p = new Point2D.Double[4];

        // Top line
        p[0] = getIntersectionPoint(line,
                        new Line2D.Double(
                        rectangle.getX(),
                        rectangle.getY(),
                        rectangle.getX() + rectangle.getWidth(),
                        rectangle.getY()));
        // Bottom line
        p[1] = getIntersectionPoint(line,
                        new Line2D.Double(
                        rectangle.getX(),
                        rectangle.getY() + rectangle.getHeight(),
                        rectangle.getX() + rectangle.getWidth(),
                        rectangle.getY() + rectangle.getHeight()));
        // Left side...
        p[2] = getIntersectionPoint(line,
                        new Line2D.Double(
                        rectangle.getX(),
                        rectangle.getY(),
                        rectangle.getX(),
                        rectangle.getY() + rectangle.getHeight()));
        // Right side
        p[3] = getIntersectionPoint(line,
                        new Line2D.Double(
                        rectangle.getX() + rectangle.getWidth(),
                        rectangle.getY(),
                        rectangle.getX() + rectangle.getWidth(),
                        rectangle.getY() + rectangle.getHeight()));

        return p;

    }
    private void applyWindow(){
//        this.xMin=Double.parseDouble(txtXmin.getText());
//        this.yMin=Double.parseDouble(txtYmin.getText());
        this.xrangeMin=Double.parseDouble(txtXminwin.getText());
        this.xrangeMax=Double.parseDouble(txtXmaxwin.getText());
        this.yrangeMin=Double.parseDouble(txtYminwin.getText());
        this.yrangeMax=Double.parseDouble(txtYmaxwin.getText());
        this.calcScaleFitX();
        this.calcScaleFitY();
        this.actualitzaPlot();
    }
    
    private void fillWindowValues(){
//        this.txtXmin.setText(FileUtils.dfX_3.format(this.xMin));
//        this.txtXmax.setText(FileUtils.dfX_3.format(this.xMax));
//        this.txtYmin.setText(FileUtils.dfX_3.format(this.yMin));
//        this.txtYmax.setText(FileUtils.dfX_3.format(this.yMax));
        this.txtXmaxwin.setText(FileUtils.dfX_3.format(this.xrangeMax));
        this.txtXminwin.setText(FileUtils.dfX_3.format(this.xrangeMin));
        this.txtYmaxwin.setText(FileUtils.dfX_3.format(this.yrangeMax));
        this.txtYminwin.setText(FileUtils.dfX_3.format(this.yrangeMin));
    }
    
    private void fillWindowValuesDiv(){
        this.txtNdivx.setText(FileUtils.dfX_3.format(this.div_incXPrim/this.div_incXSec));
        this.txtNdivy.setText(FileUtils.dfX_3.format(this.div_incYPrim/this.div_incYSec));
        this.txtXdiv.setText(FileUtils.dfX_3.format(this.div_incXPrim));
        this.txtYdiv.setText(FileUtils.dfX_3.format(this.div_incYPrim));
        this.txtXmin.setText(FileUtils.dfX_3.format(this.div_startValX));
        this.txtYmin.setText(FileUtils.dfX_3.format(this.div_startValY));
    }
    
    private void applyDivisions(){
//        this.xMin=Double.parseDouble(txtXmin.getText());
//        this.yMin=Double.parseDouble(txtYmin.getText());
//        this.xMax=Double.parseDouble(txtXmax.getText());
//        this.yMax=Double.parseDouble(txtYmax.getText());
        this.div_startValX=Double.parseDouble(txtXmin.getText());
        this.div_startValY=Double.parseDouble(txtYmin.getText());
        this.customDivLinesX(Double.parseDouble(txtXdiv.getText()), Double.parseDouble(txtNdivx.getText()));
        this.customDivLinesY(Double.parseDouble(txtYdiv.getText()), Double.parseDouble(txtNdivy.getText()));
        this.actualitzaPlot();        
    }
    
    
//    private void logdebug(String s){
//        if (D1Dplot_global.isDebug()){
//            log.debug(s);
//        }
//    }
    
//    private boolean isDebug(){
//        return D1Dplot_global.isDebug();
//    }
    
    private void do_graphPanel_mouseDragged(MouseEvent e) {
	
	    Point2D.Double currentPoint = new Point2D.Double(e.getPoint().x, e.getPoint().y);
	
	    if (this.mouseDrag == true && this.mouseMove) {
	        double incX, incY;
	        // agafem el dragpoint i l'actualitzem
	        incX = currentPoint.x - dragPoint.x;
	        incY = currentPoint.y - dragPoint.y;
	        this.dragPoint = currentPoint;
	        this.movePattern(incX, incY);
	    }
	    
	    //WE DO SCROLL OR ZOOMOUT DEPENDING
	    if (this.mouseDrag == true && this.mouseZoom) {
	        double incY,incX;
	        // agafem el dragpoint i l'actualitzem
	        incX = currentPoint.x - dragPoint.x;
	        incY = currentPoint.y - dragPoint.y;
	        this.dragPoint = currentPoint;
	        
	        if (FastMath.abs(incX)>FastMath.abs(incY)){
	            //fem scrolling
//	            boolean direction = (incX < 0);
	            this.scrollX(-incX);
	        }else{
	            //fem unzoom
	            boolean zoomIn = (incY < 0);
	            this.zoomX(zoomIn, FastMath.abs(incY));
	        }
	    }
	    
	    if (this.mouseDrag == true && this.mouseBox == true){
	        Rectangle2D.Double rarea = getRectangleGraphArea();
	        double rwidth = FastMath.abs(dragPoint.x-currentPoint.x);
	        if (rwidth<minZoomPixels)return;
	        double rheight = rarea.height;
	        double yrect = rarea.y;
	        if (sqSelect){ //afecta a x i a y
	            rheight = FastMath.abs(dragPoint.y-currentPoint.y);
	        }
	        //drag
	        //defecte drag cap a la dreta
	        double xrect = dragPoint.x;
	        if (dragPoint.x > currentPoint.x){
	            //estem fent el drag cap a la esquerra, corregim vertex
	            xrect = currentPoint.x;
	        }
	        if (sqSelect){ //afecta a x i a y
	            //defecte drag cap avall
	            yrect = dragPoint.y;
	            if (dragPoint.y > currentPoint.y){
	                //drag cap amunt, corregim vertex
	                yrect = currentPoint.y;
	            }
	        }
	        zoomRect = new Rectangle2D.Double(xrect,yrect,rwidth,rheight);
	    }
	    this.actualitzaPlot();
	}

	private void do_graphPanel_mouseMoved(MouseEvent e) {
	        if (arePlottables()){
	            Point2D.Double dp = getDataPointFromFramePoint(new Point2D.Double(e.getPoint().x, e.getPoint().y));
	            if (dp!=null){
	                
	                //get the units from first pattern that is plotted
	                DataSerie ds = this.getFirstPlottedSerie();
	                if (ds==null) return;
	                String Xpref = "X=";
	                String Ypref = "Y=";
	                String Xunit = "";
	                String Yunit = "";
	                switch(ds.getxUnits()){
	                    case tth:
	                        Xpref = "X(2"+D1Dplot_global.theta+")=";
	                        Xunit = "º";
	                        break;
	                    case dsp:
	                        Xpref = "X(dsp)=";
	                        Xunit = D1Dplot_global.angstrom;
	                        break;
	                    case dspInv:
	                        Xpref = "X(1/dsp²)=";
	                        Xunit = D1Dplot_global.angstrom+"-1";
	                        break;
	                    case Q:
	                        Xpref = "X(Q)=";
	                        Xunit = D1Dplot_global.angstrom+"-1";
	                        break;
	                    case G:
	                        Xpref = "X(G)=";
	                        Xunit = D1Dplot_global.angstrom;
	                        break;
	                    default:
	                        Xpref = "X=";
	                        Xunit = "";
	                        break;
	                }
	                
	                
	                double dtth = dp.getX();
//                    lblTth.setText(String.format(" %s%.4f%s %s%.1f%s", Xpref,dtth,Xunit,Ypref,dp.getY(),Yunit));
                    lblTth.setText(String.format("%s%.4f%s", Xpref,dtth,Xunit,Ypref,dp.getY(),Yunit));
                    lblInten.setText(String.format("%s%.1f%s", Ypref,dp.getY(),Yunit));
	                double wl = ds.getWavelength();
	                if((wl>0)&&(ds.getxUnits()==Xunits.tth)){
	                    //mirem si hi ha wavelength i les unitats del primer son tth
	                    double dsp = wl/(2*FastMath.sin(FastMath.toRadians(dtth/2.)));
//	                    lblDsp.setText(String.format(" [dsp=%.4f"+D1Dplot_global.angstrom+"]", dsp));
	                    lblDsp.setText(String.format("dsp=%.4f"+D1Dplot_global.angstrom, dsp));
	                }else{
	                    lblDsp.setText("");
	                }
	                
	                //totes les hkl (ja les desactivarem si no volem veure'ls)
	                if (hkllabels){
	                    List<Plottable_point> dhkl = new ArrayList<Plottable_point>();
	                    for (Plottable p:dataToPlot) {
	                        for (DataSerie dshkl:p.getDataSeriesByType(SerieType.hkl)) {
	                            double tol = FastMath.min(10*getXunitsPerPixel(), 0.10); //provem el minim entre 10 pixels o 0.025º 2th
	                            dhkl.addAll(dshkl.getClosestPointsToAGivenX(dtth, tol)); //TODOK afegit addall 23/05/19
	                        }
	                    }
	                    

                        if (dhkl.size()>0){
                            Iterator<Plottable_point> itrhkl = dhkl.iterator();
                            StringBuilder shkl = new StringBuilder();
                            shkl.append("hkl(s)= ");
                            int nhkls = 0;                      //limitem a numero de reflexions
                            int maxREFS = 15;
                            boolean maxReached = false;
                            while (itrhkl.hasNext()){
                                Plottable_point hkl = itrhkl.next();
                                shkl.append(hkl.getInfo()).append("; ");
                                nhkls++;
                                if (nhkls>=maxREFS) {
                                    maxReached=true;
                                    break;
                                }
                            }
                            String toprinthkl = shkl.substring(0, shkl.length()-2);
                            if (maxReached) toprinthkl=toprinthkl.concat(" (... and more)");
                            lblHkl.setText(toprinthkl);
                        }else {
                            lblHkl.setText("");
                        }
	                }else{
	                    lblHkl.setText("");
	                }
	            }
	        }
	    }

	// Identificar el bot� i segons quin sigui moure o fer zoom
	    private void do_graphPanel_mousePressed(MouseEvent arg0) {
	        if (!arePlottables())return;
	        this.dragPoint = new Point2D.Double(arg0.getPoint().x, arg0.getPoint().y);
	
	        if (arg0.getButton() == MOURE) {
	            this.clickPoint = new Point2D.Double(arg0.getPoint().x, arg0.getPoint().y);
	            this.mouseDrag = true;
	            this.mouseMove = true;
	        }
	        if (arg0.getButton() == ZOOM_BORRAR) {
	            this.mouseDrag = true;
	            this.mouseZoom = true;
	        }
	        if (arg0.getButton() == CLICAR) {
	            //abans d'aplicar el moure mirem si s'està fent alguna cosa
	            if(this.selectingBkgPoints){
	                Plottable_point dp = this.getDataPointDPFromFramePoint(this.dragPoint);
	                this.bkgEstimP.addPoint(dp);
	            }else if(this.deletingBkgPoints){
	                Plottable_point dp = this.getDataPointDPFromFramePoint(this.dragPoint);
	                Plottable_point toDelete = this.bkgEstimP.getClosestDP(dp,-1,-1,plotwithbkg);
                    if (toDelete!=null){
                        this.bkgEstimP.removePoint(toDelete);
                    }  
	                
	            }else if(this.selectingPeaks){
	                if(isOneSerieSelected()){
	                    //agafar com a pic la 2theta clicada pero amb la intensitat del punt mes proper
	                    Plottable_point dp = this.getDataPointDPFromFramePoint(this.dragPoint);
	                    DataSerie ds = this.getFirstSelectedPlottable().getFirstDataSerieByType(SerieType.peaks);
	                    if (ds==null) {
	                        ds = new DataSerie(this.getFirstSelectedSerie(),SerieType.peaks,false);
	                        this.getFirstSelectedPlottable().addDataSerie(ds);
	                        
	                    }
	                    ds.addPoint(dp);	                    
	                }
	            }else if(this.deletingPeaks){
	                if(isOneSerieSelected()){
	                    Plottable_point dp = this.getDataPointDPFromFramePoint(this.dragPoint);
	                    DataSerie ds = this.getFirstSelectedPlottable().getFirstDataSerieByType(SerieType.peaks);
	                    if (ds!=null) {
	                        Plottable_point toDelete = ds.getClosestDP(dp,-1,-1,plotwithbkg);
                            if (toDelete!=null){
                                ds.removePoint(toDelete);
                            }
	                    }
	                }
	            }else{
	                if(this.shiftPressed){
	                    this.sqSelect=true;
	                }else{
	                    this.sqSelect=false;
	                }
	                this.mouseDrag = true;
	                this.zoomRect = null; //reiniciem rectangle
	                this.mouseBox = true;
	            }
	        }
	        this.actualitzaPlot();
	
	    }

	private void do_graphPanel_mouseReleased(MouseEvent e) {

	        if (e.getButton() == MOURE){
	            this.mouseDrag = false;
	            this.mouseMove = false;
	            Point2D.Double currentPoint = new Point2D.Double(e.getPoint().x, e.getPoint().y);
	            if ((FastMath.abs(this.clickPoint.x-currentPoint.x)<0.5) && (FastMath.abs(this.clickPoint.y-currentPoint.y)<0.5)){
	                this.fitGraph();
	            }
	        }
	        if (e.getButton() == ZOOM_BORRAR){
	            this.mouseDrag = false;
	            this.mouseZoom = false;            
	        }
	        if (e.getButton() == CLICAR){
	            this.mouseBox=false;
	        }
	        if (!arePlottables())return;
	        
	        if (e.getButton() == CLICAR) {
	            //comprovem que no s'estigui fent una altra cosa          
	            if(this.selectingBkgPoints||this.deletingBkgPoints)return;
	            if(this.selectingPeaks||this.deletingPeaks)return;
	
	            //COMPROVEM QUE HI HAGI UN MINIM D'AREA ENTREMIG (per evitar un click sol)
	            if (FastMath.abs(e.getPoint().x-dragPoint.x)<minZoomPixels)return;
	            if (this.sqSelect)if (FastMath.abs(e.getPoint().y-dragPoint.y)<minZoomPixels)return;
	            
	            Point2D.Double dataPointFinal = this.getDataPointFromFramePoint(new Point2D.Double(e.getPoint().x, e.getPoint().y));
	            Point2D.Double dataPointInicial = this.getDataPointFromFramePoint(dragPoint);
	            
	            if (dataPointFinal == null && dataPointInicial==null){//els dos punts a fora
	                return;
	            }
	            
	            if (dataPointFinal == null){
	                dataPointFinal = this.getDataPointFromFramePoint(new Point2D.Double(checkFrameXValue(e.getPoint().x),checkFrameYValue(e.getPoint().y)));
	            }
	            if (dataPointInicial==null){
	                dataPointInicial = this.getDataPointFromFramePoint(new Point2D.Double(checkFrameXValue(dragPoint.x),checkFrameYValue(dragPoint.y)));
	            }
	            
	            if (dataPointFinal == null || dataPointInicial==null){//algun punt final encara a fora!
	                return;
	            }
	
	            double xrmin = FastMath.min(dataPointFinal.x, dataPointInicial.x);
	            double xrmax = FastMath.max(dataPointFinal.x, dataPointInicial.x);
	            this.xrangeMin=xrmin;
	            this.xrangeMax=xrmax; //NOTODO caldria actualitzar txtboxes? -- ho fa fillwindowsvalues a repaint
	            this.calcScaleFitX();
	            
	            if (this.sqSelect){
	                double yrmin = FastMath.min(dataPointFinal.y, dataPointInicial.y);
	                double yrmax = FastMath.max(dataPointFinal.y, dataPointInicial.y);
	                this.yrangeMin=yrmin;
	                this.yrangeMax=yrmax;
	                this.calcScaleFitY();
	              }
	            
	            this.actualitzaPlot();
	        }
	        this.sqSelect=false;
	    }

	private void do_graphPanel_mouseWheelMoved(MouseWheelEvent e) {
	    Point2D.Double p = new Point2D.Double(e.getPoint().x, e.getPoint().y);
	    boolean zoomIn = (e.getWheelRotation() < 0);
	    this.zoomY(zoomIn, p);
	    this.actualitzaPlot();
	}

//	private void do_btnResetView_actionPerformed(ActionEvent e) {
//	    this.fitGraph();
//	}
	
    private void do_btnFitWindowStatus_actionPerformed(ActionEvent e) {
        this.fitGraph();
    }

	private void do_chckbxFixedAxis_itemStateChanged(ItemEvent e) {
	    this.fixAxes=chckbxFixedAxis.isSelected();
	    if (!fixAxes){
	        txtNdivx.setEditable(false);
	        txtNdivy.setEditable(false);
	        txtXdiv.setEditable(false);
	        txtYdiv.setEditable(false);
	    }else{
	        txtNdivx.setEditable(true);
	        txtNdivy.setEditable(true);
	        txtXdiv.setEditable(true);
	        txtYdiv.setEditable(true);            
	    }
	    this.actualitzaPlot();
	}

	private void do_txtNdivy_actionPerformed(ActionEvent e) {
         this.applyDivisions();
    }
    private void do_txtYdiv_actionPerformed(ActionEvent e) {
        this.applyDivisions();
    }
    private void do_txtNdivx_actionPerformed(ActionEvent e) {
        this.applyDivisions();
    }
    private void do_txtXdiv_actionPerformed(ActionEvent e) {
        this.applyDivisions();
    }
    private void do_txtYmin_actionPerformed(ActionEvent e) {
        this.applyDivisions();
    }
    private void do_txtXmin_actionPerformed(ActionEvent e) {
        this.applyDivisions();
    }
    private void do_txtXminwin_actionPerformed(ActionEvent e) {
        this.applyWindow();
    }
    protected void do_txtYminwin_actionPerformed(ActionEvent e) {
        this.applyWindow();
    }
    private void do_txtXmaxwin_actionPerformed(ActionEvent e) {
        this.applyWindow();
    }
    private void do_txtYmaxwin_actionPerformed(ActionEvent e) {
        this.applyWindow();
    }
    private void do_btnReassigncolors_actionPerformed(ActionEvent e) {
        this.reassignColorPatterns();
        mainframeUpdateData();
    }
    
    private void mainframeUpdateData() {
        if (D1Dplot_global.getD1Dmain()!=null) {
            D1Dplot_global.getD1Dmain().updateData(false, false);
        }
    }
    private void do_txtXtitle_actionPerformed(ActionEvent e) {
        if (txtXtitle.getText().isEmpty()){
            customXtitle=false;
        }else{
            setXlabel(txtXtitle.getText());
            customXtitle=true;
        }        
    }
    private void do_txtYtitle_actionPerformed(ActionEvent e) {
        if (txtYtitle.getText().isEmpty()){
            setYlabel("Intensity");
            txtYtitle.setText("Intensity");
        }else {
            setYlabel(txtYtitle.getText());    
        }
        
    }
    private void do_comboTheme_itemStateChanged(ItemEvent e) {
        if(e.getStateChange() == ItemEvent.DESELECTED)return;
        if (comboTheme.getSelectedItem().toString().equalsIgnoreCase("Light")){
            setLightTheme(true);
        }else{
            setLightTheme(false);
        }

        if (arePlottables()){
            boolean repaint = FileUtils.YesNoDialog(null, "Repaint current patterns?");
            if(repaint){
                reassignColorPatterns();
            }
        }
        actualitzaPlot();
    }
    public boolean isCustomXtitle() {
        return customXtitle;
    }
    private void do_chckbxShow_itemStateChanged(ItemEvent e) {
        setShowLegend(chckbxShow.isSelected());
    }
    private void do_chckbxAutopos_itemStateChanged(ItemEvent e) {
        setAutoPosLegend(chckbxAutopos.isSelected());
        if (chckbxAutopos.isSelected()){
            txtXlegend.setEditable(false);
            txtYlegend.setEditable(false);
        }else{
            txtXlegend.setEditable(true);
            txtYlegend.setEditable(true);
        }
        //legend pos Actualitzem sempre
        txtXlegend.setText(Integer.toString(getLegendX()));
        txtYlegend.setText(Integer.toString(getLegendY()));
    }
    private void do_txtXlegend_actionPerformed(ActionEvent e) {
        try{
            int lx = Integer.parseInt(txtXlegend.getText());
            setLegendX(lx);
            txtXlegend.setText(Integer.toString(getLegendX()));
        }catch(Exception ex){
            log.warning("Error reading legend X position");
        }
    }
    private void do_txtYlegend_actionPerformed(ActionEvent e) {
        try{
            int ly = Integer.parseInt(txtYlegend.getText());
            setLegendY(ly);
            txtYlegend.setText(Integer.toString(getLegendY()));
        }catch(Exception ex){
            log.warning("Error reading legend Y position");
        }
    }
    private void do_chckbxApply_itemStateChanged(ItemEvent e) {
        applyPartialYscale();
    }
    private void do_txtZonescalexini_keyReleased(KeyEvent e) {
        if (e.getKeyCode()==KeyEvent.VK_ENTER) {
            applyPartialYscale();
        }
    }
    private void do_txtZonescalefactor_keyReleased(KeyEvent e) {
        if (e.getKeyCode()==KeyEvent.VK_ENTER) {
            applyPartialYscale();
        }
    }
    private void applyPartialYscale() {
        //default values
        float t2ini=30;
        float t2fac=1.0f;
        try {
            t2ini=Float.parseFloat(txtZonescalexini.getText());
            t2fac=Float.parseFloat(txtZonescalefactor.getText());
        }catch(Exception ex) {
            log.warning("Error reading t2 value or factor");
            txtZonescalexini.setText("30.0");
            txtZonescalefactor.setText("1.0");
        }
        setPartialYScale(chckbxApply.isSelected(),t2ini,t2fac);
    }
    private void do_chckbxX_itemStateChanged(ItemEvent e) {
        setShowGridX(chckbxX.isSelected());
    }
    private void do_chckbxY_itemStateChanged(ItemEvent e) {
        setShowGridY(chckbxY.isSelected());
    }
    private void do_chckbxVerticalYAxis_itemStateChanged(ItemEvent e) {
        setVerticalYAxe(chckbxVerticalYAxis.isSelected());
    }
    private void do_chckbxVerticalYLabel_itemStateChanged(ItemEvent e) {
        setVerticalYlabel(chckbxVerticalYLabel.isSelected());
    }
    private void do_chckbxBkgIntensityprf_itemStateChanged(ItemEvent e) {
        setPlotwithbkg(chckbxBkgIntensityprf.isSelected());
    }
    private void do_chckbxHklLabels_itemStateChanged(ItemEvent e) {
        setHkllabels(chckbxHklLabels.isSelected());
    }
    private void do_chckbxNegativeYLabels_itemStateChanged(ItemEvent e) {
        setNegativeYAxisLabels(chckbxNegativeYLabels.isSelected());
    }
    private void do_chckbxDbCompoundIntensity_itemStateChanged(ItemEvent e) {
        setShowDBCompoundIntensity(chckbxDbCompoundIntensity.isSelected());
    }
    private void do_chckbxSplit_itemStateChanged(ItemEvent e) {
        this.splitDBCompound=chckbxSplit.isSelected();
        this.actualitzaPlot();
    }
    private void do_chckbxOpaque_itemStateChanged(ItemEvent e) {
        this.legend_opaque=chckbxOpaque.isSelected();
        this.actualitzaPlot();
    }
    
    public String getVisualParametersToSave() {
      //guardar axis info, zoom, bounds, etc...
        String theme = Boolean.toString(this.isLightTheme());
        String bkg = Boolean.toString(this.plotwithbkg);
        String hkl = Boolean.toString(this.hkllabels);
        String gridX = Boolean.toString(this.showGridX);
        String gridY = Boolean.toString(this.showGridY);
        String legend = Boolean.toString(this.showLegend);
        String autoLeg = Boolean.toString(this.autoPosLegend);
        String yVert = Boolean.toString(this.isVerticalYAxe());
        String yVertLabel = Boolean.toString(this.isVerticalYlabel());
        String yVertNeg = Boolean.toString(this.negativeYAxisLabels);
        String fixAxes = Boolean.toString(this.fixAxes);

        
        String legX = Integer.toString(this.legendX);
        String legY = Integer.toString(this.legendY);
        String xRangeMax = FileUtils.dfX_4.format(this.xrangeMax);
        String xRangeMin = FileUtils.dfX_4.format(this.xrangeMin);
        String yRangeMax = FileUtils.dfX_4.format(this.yrangeMax);
        String yRangeMin = FileUtils.dfX_4.format(this.yrangeMin);
        String scaleX = FileUtils.dfX_4.format(this.scalefitX);
        String scaleY = FileUtils.dfX_4.format(this.scalefitY);

        String incXprim = FileUtils.dfX_4.format(this.div_incXPrim);
        String incXsec = FileUtils.dfX_4.format(this.div_incXSec);
        String incYprim = FileUtils.dfX_4.format(this.div_incYPrim);
        String incYsec = FileUtils.dfX_4.format(this.div_incYSec);
        String startValX = FileUtils.dfX_4.format(this.div_startValX);
        String startValY = FileUtils.dfX_4.format(this.div_startValY);

        String xLabel = this.xlabel;
        String yLabel = this.ylabel;
        
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s %s %s %s %s %s %s %s %s %s %s\n", theme,bkg,hkl,gridX,gridY,legend,autoLeg,yVert,yVertLabel,yVertNeg,fixAxes));
        sb.append(String.format("%s %s %s %s %s %s %s %s\n", legX,legY,xRangeMax,xRangeMin,yRangeMax,yRangeMin,scaleX,scaleY));
        sb.append(String.format("%s %s %s %s %s %s\n",incXprim,incXsec,incYprim,incYsec,startValX,startValY));
        sb.append(xLabel);
        sb.append("\n");
        sb.append(yLabel);
        return sb.toString();
    }
    
    
    //UPTADED WITH NEW FORMAT... 26/08/2019
    public void setVisualParametersFromSaved(String[] vals1, String[] vals2, String[] vals3, String xlabel, String ylabel) {
        try {
            this.setLightTheme(Boolean.parseBoolean(vals1[0]));
            this.plotwithbkg=Boolean.parseBoolean(vals1[1]);
            this.hkllabels=Boolean.parseBoolean(vals1[2]);
            this.showGridX=Boolean.parseBoolean(vals1[3]);
            this.showGridY=Boolean.parseBoolean(vals1[4]);
            this.showLegend=Boolean.parseBoolean(vals1[5]);
            this.autoPosLegend=Boolean.parseBoolean(vals1[6]);
            this.setVerticalYAxe(Boolean.parseBoolean(vals1[7]));
            this.setVerticalYlabel(Boolean.parseBoolean(vals1[8]));
            this.negativeYAxisLabels=Boolean.parseBoolean(vals1[9]);
            this.chckbxFixedAxis.setSelected(Boolean.parseBoolean(vals1[10]));
        }catch(Exception e) {
            log.warning("Error recovering visual parameters (1)");
        }

        try {
            this.legendX=Integer.parseInt(vals2[0]);
            this.legendY=Integer.parseInt(vals2[1]);
            this.xrangeMax=Double.parseDouble(vals2[2]);
            this.xrangeMin=Double.parseDouble(vals2[3]);
            this.yrangeMax=Double.parseDouble(vals2[4]);
            this.yrangeMin=Double.parseDouble(vals2[5]);
            this.scalefitX=Double.parseDouble(vals2[6]);
            this.scalefitY=Double.parseDouble(vals2[7]);
        }catch(Exception e) {
            log.warning("Error recovering visual parameters (2)");
        }
        
        try {
            this.div_incXPrim=Double.parseDouble(vals3[0]);
            this.div_incXSec=Double.parseDouble(vals3[1]);
            this.div_incYPrim=Double.parseDouble(vals3[2]);
            this.div_incYSec=Double.parseDouble(vals3[3]);
            this.div_startValX=Double.parseDouble(vals3[4]);
            this.div_startValY=Double.parseDouble(vals3[5]);

            this.xlabel=xlabel;
            this.ylabel=ylabel;
            
        }catch(Exception e) {
            log.warning("Error recovering visual parameters (3)");
        }
        
        fillWindowValuesDiv();
        fillWindowValues();
        
        applyDivisions();
        applyWindow();

    }
    
	private boolean isLightTheme() {
	    return lightTheme;
	}

	public void setLightTheme(boolean lightTheme) {
	    this.lightTheme = lightTheme;
	}

	public int getMOURE() {
	    return MOURE;
	}

	public void setMOURE(int mOURE) {
	    MOURE = mOURE;
	}

	public int getCLICAR() {
	    return CLICAR;
	}

	public void setCLICAR(int cLICAR) {
	    CLICAR = cLICAR;
	}

	public int getZOOM_BORRAR() {
	    return ZOOM_BORRAR;
	}

	public void setZOOM_BORRAR(int zOOM_BORRAR) {
	    ZOOM_BORRAR = zOOM_BORRAR;
	}

	public boolean isVerticalYlabel() {
	    return verticalYlabel;
	}

	public void setVerticalYlabel(boolean verticalYlabel) {
	    this.verticalYlabel = verticalYlabel;
	    this.actualitzaPlot();
	}

	public int getDiv_PrimPixSize() {
	    return div_PrimPixSize;
	}

	public void setDiv_PrimPixSize(int div_PrimPixSize) {
	    this.div_PrimPixSize = div_PrimPixSize;
	}

	public int getDiv_SecPixSize() {
	    return div_SecPixSize;
	}

	public void setDiv_SecPixSize(int div_SecPixSize) {
	    this.div_SecPixSize = div_SecPixSize;
	}

	public boolean isVerticalYAxe() {
	    return verticalYAxe;
	}

	public void setVerticalYAxe(boolean verticalYAxe) {
	    this.verticalYAxe = verticalYAxe;
	    this.actualitzaPlot();
	}

	public String getXlabel() {
	    return xlabel;
	}
	
	public double[] getXrangesMinMax() {
	    return new double[]{this.xrangeMin,this.xrangeMax};
	}

	public void setXlabel(String xlabel) {
	    this.xlabel = xlabel;
	    txtXtitle.setText(xlabel);
	    this.actualitzaPlot();
	}

	public String getYlabel() {
	    return ylabel;
	}

	public void setYlabel(String ylabel) {
	    this.ylabel = ylabel;
	    txtYtitle.setText(ylabel);
	    this.actualitzaPlot();
	}

	public boolean isShowLegend() {
	    return showLegend;
	}

	public void setShowLegend(boolean showLegend) {
	    this.showLegend = showLegend;
	    this.actualitzaPlot();
	}

	public boolean isAutoPosLegend() {
	    return autoPosLegend;
	}

	public void setAutoPosLegend(boolean autoPosLegend) {
	    this.autoPosLegend = autoPosLegend;
	    this.actualitzaPlot();
	}

	protected int getLegendX() {
	    return legendX;
	}

	protected void setLegendX(int legendX) {
	    this.legendX = legendX;
	    this.actualitzaPlot();
	}

	protected int getLegendY() {
	    return legendY;
	}

	protected void setLegendY(int legendY) {
	    this.legendY = legendY;
	    this.actualitzaPlot();
	}

	protected boolean isHkllabels() {
	    return hkllabels;
	}

	protected void setHkllabels(boolean hkllabels) {
	    this.hkllabels = hkllabels;
	    this.actualitzaPlot();
	}
	
	protected void setPartialYScale(boolean selected, float t2, float fact) {
        this.applyScaleFactorT2=selected;
        this.scaleFactorT2ang=t2;
        this.scaleFactorT2fact=fact;
        this.actualitzaPlot();
    }
    

	protected boolean isShowGridY() {
	    return showGridY;
	}

	protected void setShowGridY(boolean showGrid) {
	    this.showGridY = showGrid;
	    this.actualitzaPlot();
	}

	protected boolean isShowGridX() {
	    return showGridX;
	}

	protected void setShowGridX(boolean showGrid) {
	    this.showGridX = showGrid;
	    this.actualitzaPlot();
	}

	protected Plot1d getGraphPanel() {
	    return graphPanel;
	}

	protected boolean isNegativeYAxisLabels() {
	    return negativeYAxisLabels;
	}

	protected void setNegativeYAxisLabels(boolean negativeYAxisLabels) {
	    this.negativeYAxisLabels = negativeYAxisLabels;
	    this.actualitzaPlot();
	}

	protected void setShowPeakThreshold(boolean showPeaks) {
	    this.showPeakThreshold = showPeaks;
	    this.actualitzaPlot();
	}

	protected void setShowDBCompoundIntensity(boolean showDBCompoundInten) {
		this.showDBCompoundIntensity = showDBCompoundInten;
		this.actualitzaPlot();
	}

	protected void setShowDBCompound(boolean showDBCompound) {
		this.showDBCompound = showDBCompound;
		this.actualitzaPlot();
	}
	
	protected void setShowIndexSolution(boolean showIS) {
        this.showIndexSolution = showIS;
        this.actualitzaPlot();
    }
	
	protected void setIndexSolution(DataSerie dsIS) {
	    this.indexSolution = dsIS;
	    this.actualitzaPlot();
	}

	protected boolean isPlotwithbkg() {
        return plotwithbkg;
    }

    public void setPlotwithbkg(boolean plotwithbkg) {
        this.plotwithbkg = plotwithbkg;
        this.actualitzaPlot();
    }

    public List<DataSerie> getSelectedSeries() {
		//TODO mirar si no hi ha cap i si es així fer que mainframe seleccioni la primera, fer el mateix per getSelectedSerie (1 de sola)
	    return selectedSeries;
	}

	public void setShowEstimPointsBackground(boolean showBackground) {
	    this.showEstimPointsBackground = showBackground;
	    this.actualitzaPlot();
	}

	public boolean isSelectingBkgPoints() {
	    return selectingBkgPoints;
	}

	public void setSelectingBkgPoints(boolean selectingBkgPoints) {
	    this.selectingBkgPoints = selectingBkgPoints;
	}

	public boolean isDeletingBkgPoints() {
	    return deletingBkgPoints;
	}
	
	public void setDeletingBkgPoints(boolean deletingBkgPoints) {
	    this.deletingBkgPoints = deletingBkgPoints;
	}

	public DataSerie getBkgseriePeakSearch() {
	    return bkgseriePeakSearch;
	}

	public void setBkgseriePeakSearch(DataSerie bkgseriePeakSearch) {
	    this.bkgseriePeakSearch = bkgseriePeakSearch;
	    this.actualitzaPlot();
	}

	public boolean isSelectingPeaks() {
	    return selectingPeaks;
	}

	public void setSelectingPeaks(boolean selectingPeaks) {
	    this.selectingPeaks = selectingPeaks;
	}

	public boolean isDeletingPeaks() {
	    return deletingPeaks;
	}

	public void setDeletingPeaks(boolean deletingPeaks) {
	    this.deletingPeaks = deletingPeaks;
	}

	public void readOptions(Options opt) {

	    if (opt==null)return;
	    
	    String but = opt.getValAsString("mouseButtonSelect", "Left");
	    if (but.equalsIgnoreCase("Left"))CLICAR=MouseEvent.BUTTON1;
	    if (but.equalsIgnoreCase("Middle"))CLICAR=MouseEvent.BUTTON2;
	    if (but.equalsIgnoreCase("Right"))CLICAR=MouseEvent.BUTTON3;
	    but = opt.getValAsString("mouseButtonMove", "Middle");
	    if (but.equalsIgnoreCase("Left"))MOURE=MouseEvent.BUTTON1;
	    if (but.equalsIgnoreCase("Middle"))MOURE=MouseEvent.BUTTON2;
	    if (but.equalsIgnoreCase("Right"))MOURE=MouseEvent.BUTTON3;
	    but = opt.getValAsString("mouseButtonZoom", "Right");
	    if (but.equalsIgnoreCase("Left"))ZOOM_BORRAR=MouseEvent.BUTTON1;
	    if (but.equalsIgnoreCase("Middle"))ZOOM_BORRAR=MouseEvent.BUTTON2;
	    if (but.equalsIgnoreCase("Right"))ZOOM_BORRAR=MouseEvent.BUTTON3;

	    String col = opt.getValAsString("colorTheme", "Light");
	    if (col.equalsIgnoreCase("Light")){
	        this.lightTheme=true;
	    }else {
	        this.lightTheme=false;
	    }
//	    this.gapAxisTop = opt.getValAsInteger("axisGapTop", this.gapAxisTop);
//	    this.gapAxisBottom = opt.getValAsInteger("axisGapBottom", this.gapAxisBottom);
//	    this.gapAxisLeft = opt.getValAsInteger("axisGapLeft", this.gapAxisLeft);
//	    this.gapAxisRight = opt.getValAsInteger("axisGapRight", this.gapAxisRight);
//	    this.padding2px = opt.getValAsInteger("paddingSmall", this.padding2px);
//	    this.padding5px = opt.getValAsInteger("paddingMid", this.padding5px);
	    this.incXPrimPIXELS = opt.getValAsDouble("axisSepPrimaryXDivInAutoMode", this.incXPrimPIXELS);
	    this.incXSecPIXELS = opt.getValAsDouble("axisSepSecundaryXDivInAutoMode", this.incXSecPIXELS);
	    this.incYPrimPIXELS = opt.getValAsDouble("axisSepPrimaryYDivInAutoMode", this.incYPrimPIXELS);
	    this.incYSecPIXELS = opt.getValAsDouble("axisSepSecundaryYDivInAutoMode", this.incYSecPIXELS);
	    this.div_PrimPixSize = opt.getValAsInteger("axisPrimDivSizePx", this.div_PrimPixSize);
	    this.div_SecPixSize = opt.getValAsInteger("axisSecunDivSizePx", this.div_SecPixSize);
	    this.facZoom = opt.getValAsDouble("zoomFactor", this.facZoom);
	    this.verticalYlabel = opt.getValAsBoolean("axisVerticalYlabel", this.verticalYlabel);
	    this.def_axis_fsize = opt.getValAsFloat("axisFontSizeRelative", this.def_axis_fsize);
	    this.def_axisL_fsize = opt.getValAsFloat("axisLabelFontSizeRelative", this.def_axisL_fsize);
	    this.def_nDecimalsX = opt.getValAsInteger("axisDecimalsX", this.def_nDecimalsX);
	    this.def_nDecimalsY = opt.getValAsInteger("axisDecimalsY", this.def_nDecimalsY);
	    this.def_legend_fsize = opt.getValAsFloat("legendFontSizeRelative", this.def_legend_fsize);
	    this.colorDBcomp = opt.getValAsColor("colorDBcomp", this.colorDBcomp);
	}

	//per tal de saber tot el que es pot personalitzar
	public Options createOptionsObject() {
    
	    Options opt = new Options();
	    
	    String but = "Left";
        if (CLICAR==MouseEvent.BUTTON2)but="Middle";
        if (CLICAR==MouseEvent.BUTTON3)but="Right";
        opt.put("mouseButtonSelect", but);
        but = "Middle";
        if (MOURE==MouseEvent.BUTTON1)but="Left";
        if (MOURE==MouseEvent.BUTTON3)but="Right";
        opt.put("mouseButtonMove", but);
        but = "Right";
        if (ZOOM_BORRAR==MouseEvent.BUTTON2)but="Middle";
        if (ZOOM_BORRAR==MouseEvent.BUTTON1)but="Left";
        opt.put("mouseButtonZoom", but);
	    
	    String col = "Light";
	    if (!isLightTheme())col="Dark";
	    opt.put("colorTheme", col);
//	    opt.put("axisGapTop", String.format("%d", this.gapAxisTop));
//	    opt.put("axisGapBottom", String.format("%d", this.gapAxisBottom));
//	    opt.put("axisGapLeft", String.format("%d", this.gapAxisLeft));
//	    opt.put("axisGapRight", String.format("%d", this.gapAxisRight));
//	    opt.put("generalPadding", String.format("%d", this.padding2px));
//	    opt.put("axisLabelsPadding", String.format("%d", this.padding5px));
	    opt.put("axisSepPrimaryXDivInAutoMode", String.format("%.2f", this.incXPrimPIXELS));
	    opt.put("axisSepSecundaryXDivInAutoMode", String.format("%.2f", this.incXSecPIXELS));
	    opt.put("axisSepPrimaryXDivInAutoMode", String.format("%.2f", this.incYPrimPIXELS));
	    opt.put("axisSepSecundaryYDivInAutoMode", String.format("%.2f", this.incYSecPIXELS));
	    
	    opt.put("axisPrimDivSizePx", String.format("%d", this.div_PrimPixSize));
        opt.put("axisSecunDivSizePx", String.format("%d", this.div_SecPixSize));
        opt.put("zoomFactor", String.format("%.2f", this.facZoom));
        opt.put("axisVerticalYlabel", Boolean.toString(verticalYlabel));
        opt.put("axisFontSizeRelative",  String.format("%.1f", this.def_axis_fsize));
        opt.put("axisLabelFontSizeRelative",  String.format("%.1f", this.def_axisL_fsize));
        opt.put("axisDecimalsX", String.format("%d", this.def_nDecimalsX));
        opt.put("axisDecimalsY", String.format("%d", this.def_nDecimalsY));
        opt.put("colorDBcomp", FileUtils.getColorName(this.colorDBcomp));
        opt.put("legendFontSizeRelative", String.format("%.1f", this.def_legend_fsize));
	    return opt;
	}
	
	

	//  ------------------------------------ PANELL DE DIBUIX
    class Plot1d extends JPanel {

        private static final long serialVersionUID = 1L;

        private int panelW, panelH;
//        private Graphics2D g2;
        private boolean saveTransp = false;
        
        private DecimalFormat def_xaxis_format = FileUtils.dfX_3;
        private DecimalFormat def_yaxis_format = FileUtils.dfX_1;
        
        public Plot1d(){
            super();
            this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
            this.setDoubleBuffered(true);
            this.setDecimalsXaxis(def_nDecimalsX);
            this.setDecimalsYaxis(def_nDecimalsY);
        }

        public void setTransp(boolean transp){
            this.saveTransp=transp;
        }
        
        public DecimalFormat getDef_xaxis_format() {
            return def_xaxis_format;
        }

        public void setDef_xaxis_format(DecimalFormat def_xaxis_format) {
            this.def_xaxis_format = def_xaxis_format;
        }

        public DecimalFormat getDef_yaxis_format() {
            return def_yaxis_format;
        }

        public void setDef_yaxis_format(DecimalFormat def_yaxis_format) {
            this.def_yaxis_format = def_yaxis_format;
        }
        
        public void setDecimalsXaxis(int dec) {
            switch (dec) {
                case 0:
                    this.setDef_xaxis_format(FileUtils.dfX_0);
                    break;
                case 1:
                    this.setDef_xaxis_format(FileUtils.dfX_1);
                    break;
                case 2:
                    this.setDef_xaxis_format(FileUtils.dfX_2);
                    break;
                case 3:
                    this.setDef_xaxis_format(FileUtils.dfX_3);
                    break;
                case 4:
                    this.setDef_xaxis_format(FileUtils.dfX_4);
                    break;
                case 5:
                    this.setDef_xaxis_format(FileUtils.dfX_5);
                    break;
                default:
                    this.setDef_xaxis_format(FileUtils.dfX_3);
            }
        }
        public void setDecimalsYaxis(int dec) {
            switch (dec) {
                case 0:
                    this.setDef_yaxis_format(FileUtils.dfX_0);
                    break;
                case 1:
                    this.setDef_yaxis_format(FileUtils.dfX_1);
                    break;
                case 2:
                    this.setDef_yaxis_format(FileUtils.dfX_2);
                    break;
                case 3:
                    this.setDef_yaxis_format(FileUtils.dfX_3);
                    break;
                case 4:
                    this.setDef_yaxis_format(FileUtils.dfX_4);
                    break;
                case 5:
                    this.setDef_yaxis_format(FileUtils.dfX_5);
                    break;
                default:
                    this.setDef_yaxis_format(FileUtils.dfX_3);
                    break;
            }
        }

        int nrefseries = 0; //for plotting as phase ID split
        
        protected void pinta(Graphics2D g2, double scale) {
            if (!this.saveTransp){
                if (lightTheme){
                    g2.setBackground(Light_bkg);
                }else{
                    g2.setBackground(Dark_bkg);
                }
                g2.clearRect(0, 0, (int)(panelW*scale), (int)(panelH*scale));
            }
            
            //TODO es pot posar una opcio quality
            g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.setTransform(AffineTransform.getScaleInstance(scale, scale));

            if (scalefitY<0){
                calcScaleFitY();    
            }
            if (scalefitX<0){
                calcScaleFitX();    
            }

            //1st draw axes (and optionally grid)
            this.drawAxes(g2,showGridY,showGridX);
            nrefseries=0;
            for (Plottable p:dataToPlot) {
                for (DataSerie ds:p.getDataSeries()) {
                    if (!ds.plotThis)continue;
                    if (ds.isEmpty())continue; //new Març 2019
                    switch (ds.getTipusSerie()){ // aqui es poden implementar peculiaritats: dat, obs, cal, hkl, diff, bkg, bkgEstimP, gr, ref, peaks;
                    case hkl:
                        drawHKL(g2,ds,ds.color);
                        break; 
                    case ref:
                        drawREF(g2,ds,ds.color);
                        nrefseries++;
                        break; 
                    case peaks:
                        //nomes els mostrem si el plottable està seleccionat? o sempre?
                        drawPeaks(g2,ds,ds.color);
                        break;
                    default: //dibuix linea normal, (dat, dif, gr, ...)
                        drawPattern(g2,ds,ds.color);
                        break;
                    }
                }
            }
            
            if(applyScaleFactorT2) {
                BasicStroke stroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[]{2,4}, 0);
                drawVerticalLine(g2, getFrameXFromDataPointX(scaleFactorT2ang), 100, 0,"x"+FileUtils.dfX_1.format(scaleFactorT2fact), Color.GRAY, stroke);
            }
            
            if (showPeakThreshold){
                //mostrar el fons pel pksearch
                if (bkgseriePeakSearch!=null) {
                    if (bkgseriePeakSearch.getNpoints()>0) {
                        bkgseriePeakSearch.setTipusSerie(SerieType.bkg); //obliguem tipus serie bkg per pintar linia rosa
                        drawPattern(g2,bkgseriePeakSearch,bkgseriePeakSearch.color);
                    }    
                }
            }
            
            if (showEstimPointsBackground) {
                //mostrem dataserie dels punts fons
                if (bkgEstimP!=null) {
                    if (bkgEstimP.getNpoints()>0) {
                        bkgseriePeakSearch.setTipusSerie(SerieType.bkgEstimP); //obliguem tipus serie per markers size i color
                        drawPattern(g2,bkgEstimP,bkgEstimP.color);
                    }
                }
            }
            
            if (showDBCompound){
                if (dbCompound != null)drawREF(g2,dbCompound,colorDBcomp);
            }

            if (showIndexSolution){
                if (indexSolution != null) {
                    drawHKL(g2,indexSolution,indexSolution.color);
                }
            }
            
            
            if(showLegend){ //a sobre de tot
                drawLegend(g2);
            }
            
            fillWindowValues();
            
            if (mouseBox == true && zoomRect != null) {
                //dibuixem el rectangle
                g2.setColor(Color.darkGray);
                BasicStroke stroke = new BasicStroke(3f);
                g2.setStroke(stroke);
                g2.draw(zoomRect);
                Color gristransp = new Color(Color.LIGHT_GRAY.getRed(), Color.LIGHT_GRAY.getGreen(),Color.LIGHT_GRAY.getBlue(), 128 );
                g2.setColor(gristransp);
                g2.fill(zoomRect);
            }
            g2.dispose();
        }
        
        //pinta al panelW i panelH, podriem fer-ho més general posant com a arguments width i height
        protected BufferedImage pintaPatterns(double scale) {
            
            BufferedImage off_Image = new BufferedImage((int)(panelW*scale), (int)(panelH*scale), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = off_Image.createGraphics();
            this.pinta(g2,scale); //He creat el metode pinta apart pel savesvg que peta si escribim imatge
            return off_Image;
//            g.drawImage(off_Image, 0, 0, null);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); //or could do...  g.clearRect(0, 0, this.getWidth(), this.getHeight());

            panelW = this.getWidth();
            panelH = this.getHeight();
            
            if (arePlottables()) {
                g.drawImage(pintaPatterns(1.0),0,0,null); //scale 1, pintem en pantalla
                g.dispose();
            }else {
                //no patterns, podem aprofitar per reiniciar algunes coes
                nTotalOpenedDatSeries=0;
            }
        }
 
        
        private void drawAxes(Graphics2D g1, boolean gridY, boolean gridX){

            //REFET el 18-Sep-2019, pintarem d'esquerra a dreta i de dalt a baix aprofitant el maxim els espais
            //i definirem des d'aquí els gaps
            
            if(lightTheme){
                g1.setColor(Light_frg);
            }else{
                g1.setColor(Dark_frg);
            }
            BasicStroke stroke = new BasicStroke(1.0f);
            g1.setStroke(stroke);
            FontRenderContext frc = g1.getFontRenderContext();
            
            if (!checkIfDiv()) {
                log.info("Error drawing division lines on the axes, please check");
                return;
            }
            if (!fixAxes) autoDivLines(true);
            
            gapAxisTop=padding5px;
            double valY_str_height = padding5px; //s'actualitza despres
            gapAxisRight=padding5px;
            gapAxisLeft=padding5px;
            gapAxisBottom=padding5px;
            
            /*
             * Primer posarem els titols dels 2 eixos i mirarem espai que necessiten
             * 
             * i definirem els GAPS
             * 
             */
            
            //EIX X
            // X-axis (abcissa) label.
            String xlabel = getXlabel();
            double xlabelY = panelH - padding5px;
            TextLayout xLabelTextLayout = new TextLayout(xlabel, g1.getFont().deriveFont(g1.getFont().getSize()+def_axisL_fsize), frc);
            double xlabelWidth = xLabelTextLayout.getBounds().getWidth();
            double xlabelHeigth = xLabelTextLayout.getBounds().getHeight();
            //la pintarem despres per buscar exactament el centre de l'espai considerant gapLeft

            //mirem el màxim que pot ocupar d'alt el valor X
            String str_valX = this.def_xaxis_format.format(xrangeMax);
            TextLayout valXTextLayout = new TextLayout(str_valX,g1.getFont().deriveFont(g1.getFont().getSize()+def_axis_fsize),frc);
            double valX_str_height = valXTextLayout.getBounds().getHeight();
            double valX_str_width = valXTextLayout.getBounds().getWidth();

            //ara per tenir la posicio Y de l'eix X podem sumar 
            //AxisLabelsPadding + labelXheight + AxisLabelsPadding + maxHeightValues + AxisLabelsPadding + meitatLiniaPrincipalDivisio
            
            double coordYeixX = panelH-(padding5px*3+xlabelHeigth+valX_str_height + (div_PrimPixSize/2.f));
            gapAxisBottom = (int)FastMath.round(panelH-coordYeixX);
                    
            //EIX Y
            double coordXeixY = padding5px; //en cas de no eix 
            
            if (verticalYAxe) {
                String s = getYlabel();
                TextLayout yLabelTextLayout = new TextLayout(ylabel, g1.getFont().deriveFont(g1.getFont().getSize()+def_axisL_fsize), frc);
                double ylabelWidth = yLabelTextLayout.getBounds().getWidth();
                double ylabelHeight =  yLabelTextLayout.getBounds().getHeight();
                //per defecte horitzontal a dalt a l'esquerra
                double ylabelX = padding5px;
                double ylabelY = ylabelHeight + padding5px;
                
                if (verticalYlabel){
                    ylabelY = (float)(panelH - gapAxisBottom)/2. + ylabelWidth/2; //may2019 fix
                    ylabelX = (ylabelHeight/2.f)+padding5px;
                    AffineTransform orig = g1.getTransform();
                    g1.rotate(-Math.PI/2,ylabelX,ylabelY);
                    yLabelTextLayout.draw(g1, (float)ylabelX,(float)ylabelY);
                    g1.setTransform(orig);
                }else{
                    //el posem sobre l'eix en horitzontal
                    yLabelTextLayout.draw(g1, (float)ylabelX,(float)ylabelY);
                }

                //mirem el màxim que pot ocupar el valor Y
                double maxYval = FastMath.max(FastMath.abs(yrangeMax),FastMath.abs(yrangeMin));
                s = this.def_yaxis_format.format(maxYval);
                TextLayout valYTextLayout = new TextLayout(s,g1.getFont().deriveFont(g1.getFont().getSize()+def_axis_fsize),frc);
                double valY_str_width = valYTextLayout.getBounds().getWidth();
                valY_str_height = valYTextLayout.getBounds().getHeight();
                
                //ara per tenir la posicio X de l'eix Y podem sumar 
                //AxisLabelsPadding + labelYheight (nomes si vertical) + AxisLabelsPadding + maxWidth + AxisLabelsPadding + meitatLiniaPrincipalDivisio
                coordXeixY = padding5px*3+valY_str_width + (div_PrimPixSize/2.f);
                
                if (verticalYlabel) {
                    coordXeixY=coordXeixY+ylabelHeight;
                }else {
                    gapAxisTop=(int)FastMath.round(gapAxisTop+ylabelHeight+padding5px);
                }
                gapAxisLeft=(int)FastMath.round(coordXeixY);
            }else {
                //cal afegir maxwidth/2. al gapLeft per encabir el label val X si treiem eix
                gapAxisLeft = gapAxisLeft + (int)(valX_str_width/2.);
            }
            
            //al gap axis top he d'afegir una mica mes perquè cal encabir la "meitat" dels valors ja que es pinten sempre que surtin sobre l'eix i a la meitat de la ratlla.
            gapAxisTop = gapAxisTop + (int)(valY_str_height/2.f);
            gapAxisRight = gapAxisRight + (int)(valX_str_width/2.);
            
            //ara ja podem centrar el xlabel, el pintem
            double xlabelX = (panelW - gapAxisLeft - gapAxisRight)/2.f - xlabelWidth/2.f + gapAxisLeft;
            xLabelTextLayout.draw(g1, (float)xlabelX,(float)xlabelY);

            //al haver canviat gaps fem calc scalefit
            calcScaleFitX();
            calcScaleFitY();
            
            /*
             * Ara ja podem pintar els eixos 
             */
            
            Point2D.Double vytop = new Point2D.Double(gapAxisLeft,gapAxisTop);
            Point2D.Double vybot = new Point2D.Double(gapAxisLeft,coordYeixX);
            Point2D.Double vxleft = vybot;
            Point2D.Double vxright = new Point2D.Double(panelW-gapAxisRight,coordYeixX);
            Line2D.Double ordenada = new Line2D.Double(vytop,vybot);  //Y axis vertical
            Line2D.Double abcissa = new Line2D.Double(vxleft, vxright);  //X axis horizontal
            g1.draw(abcissa);
            //la ordenada la dibuxarem si s'escau despres
            
            /*
             * i ara la resta de parafarnalia
             */
            ////EIX X
            //Per tots els punts les coordenades Y seran les mateixes
            double yiniPrim = coordYeixX - (div_PrimPixSize/2.f); 
            double yfinPrim = coordYeixX + (div_PrimPixSize/2.f);
            double yiniSec = coordYeixX- (div_SecPixSize/2.f); 
            double yfinSec = coordYeixX + (div_SecPixSize/2.f);
            
            int ndiv = (int) (div_incXPrim/div_incXSec);
            int idiv = 0;
            double xval = div_startValX;
            while (xval <= xrangeMax){
                if (xval >= xrangeMin){ //la pintem nomes si estem dins el rang, sino numés icrementem el num de divisions
                    double xvalPix = getFrameXFromDataPointX(xval);
                    if (idiv%ndiv==0) {
                        //primaria: linia llarga + label
                        Line2D.Double l = new Line2D.Double(xvalPix,yiniPrim,xvalPix,yfinPrim);
                        g1.draw(l);
                        //ara el label sota la linia 
                        String s = this.def_xaxis_format.format(xval);
                        valXTextLayout = new TextLayout(s,g1.getFont().deriveFont(g1.getFont().getSize()+def_axis_fsize),frc);
                        double sw = valXTextLayout.getBounds().getWidth();
                        double sh = valXTextLayout.getBounds().getHeight();
                        double xLabel = xvalPix - sw/2f; //el posem centrat a la linia
                        double yLabel = yfinPrim + padding5px + sh;
                        valXTextLayout.draw(g1, (float)xLabel, (float)yLabel);
                    }else {
                        //secundaria: linia curta
                        Line2D.Double l = new Line2D.Double(xvalPix,yiniSec,xvalPix,yfinSec);
                        g1.draw(l);
                    }
                    
                    //i ara el grid
                    //pel grid, vytop.y sera el punt superior de la linia, yiniPrim sera el punt inferior (AIXO PER LES Y, despres les X es defineixen al bucle)
                    if(gridY){
                        BasicStroke dashed = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[]{1,2}, 0);
                        g1.setStroke(dashed);
                        Line2D.Double ld = new Line2D.Double(xvalPix,vytop.y,xvalPix,yiniSec);
                        g1.draw(ld);
                        g1.setStroke(stroke); //recuperem l'anterior
                    }
                }
                xval = xval + div_incXSec;
                idiv = idiv + 1;
            }
            
            ////EIX Y
            if (verticalYAxe) {
                g1.draw(ordenada);
                //Y axis divisions and labels:
                //Per tots els punts les coordenades Y seran les mateixes

                double xiniPrim = coordXeixY - (div_PrimPixSize/2.f); 
                double xfinPrim = coordXeixY + (div_PrimPixSize/2.f);
                double xiniSec = coordXeixY - (div_SecPixSize/2.f); 
                double xfinSec = coordXeixY + (div_SecPixSize/2.f);

                ndiv = (int) (div_incYPrim/div_incYSec);
                idiv = 0;
                double yval = div_startValY;

                while (yval <= yrangeMax){
                    if (yval >= yrangeMin){ //la pintem nomes si estem dins el rang, sino numés icrementem el num de divisions
                        double yvalPix = getFrameYFromDataPointY(yval);
                        
                        if (!negativeYAxisLabels && (yval<0)){
                            yval = yval + div_incYSec;
                            continue;
                        }
                        
                        if (idiv%ndiv==0) {
                            Line2D.Double l = new Line2D.Double(xiniPrim, yvalPix, xfinPrim, yvalPix);
                            g1.draw(l);
                            //ara el label a l'esquerra de la linia (atencio a negatius, depen si hi ha l'opcio)
                            String s = this.def_yaxis_format.format(yval);
                            TextLayout valYTextLayout = new TextLayout(s,g1.getFont().deriveFont(g1.getFont().getSize()+def_axis_fsize),frc);
                            double sw = valYTextLayout.getBounds().getWidth();
                            double sh = valYTextLayout.getBounds().getHeight();
                            double xLabel = xiniPrim - padding5px - sw; 
                            double yLabel = yvalPix + sh/2f; //el posem centrat a la linia.. no se perquè queda millor amb 3 que 2
                            valYTextLayout.draw(g1, (float)xLabel, (float)yLabel); //prova d'utilitzar textLayouts -- funciona be i el getbounds va millor
                        }else {
                            Line2D.Double l = new Line2D.Double(xiniSec,yvalPix,xfinSec,yvalPix);
                            g1.draw(l);
                        }

                        //i ara el grid
                        //pel grid, vytop.y sera el punt superior de la linia, yiniPrim sera el punt inferior (AIXO PER LES Y, despres les X es defineixen al bucle)
                        if(gridX){
                            BasicStroke dashed = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[]{1,2}, 0);
                            g1.setStroke(dashed);
                            Line2D.Double ld = new Line2D.Double(vxright.x,yvalPix,xfinSec,yvalPix);
                            g1.draw(ld);
                            g1.setStroke(stroke); //recuperem l'anterior
                        }

                    }
                    yval = yval + div_incYSec;
                    idiv = idiv + 1;
                }
            }
        }

        //dibuixa linia, punts i errorbars
        private void drawPattern(Graphics2D g1, DataSerie serie, Color col){
          g1.setColor(col);
          BasicStroke stroke = new BasicStroke(serie.lineWidth);
          g1.setStroke(stroke);
          
          for (int i = 0; i < serie.getNpoints(); i++){
              
              Plottable_point pp1=serie.getPointWithCorrections(i,plotwithbkg);

              if (applyScaleFactorT2) {              //APLICAR escala global tth si s'escau
                  if (pp1.getX()>scaleFactorT2ang) {
                      pp1 = serie.getPointWithCorrections(i,serie.getZerrOff(),serie.getYOff(),serie.getScale()*scaleFactorT2fact,false);
                  }
              }

              Point2D.Double fp1 = getFramePointFromDataPoint(pp1);
              boolean isP1 = isFramePointInsideGraphArea(fp1);
              
              //linia
              if (serie.lineWidth>0) {
                  if (i<(serie.getNpoints()-1)){ //no considerem l'últim punt

                      Plottable_point pp2=serie.getPointWithCorrections(i+1,plotwithbkg);

                      if (applyScaleFactorT2) {              //APLICAR escala global tth si s'escau
                          if (pp2.getX()>scaleFactorT2ang) {
                              pp2 = serie.getPointWithCorrections(i+1,serie.getZerrOff(),serie.getYOff(),serie.getScale()*scaleFactorT2fact,false);
                          }
                      }

                      Point2D.Double fp2 = getFramePointFromDataPoint(pp2);

                      boolean isP2 = isFramePointInsideGraphArea(fp2);

                      if (!isP1){
                          if (!isP2){
                              continue;
                          }else{
                              //P1 esta fora, cal redefinirlo amb la interseccio amb l'eix pertinent
                              Point2D.Double[] p = getIntersectionPoint(new Line2D.Double(fp1,fp2),getRectangleGraphArea());
                              for (int j=0;j<p.length;j++){
                                  if (p[j]!=null){
                                      fp1 = p[j];
                                  }
                              }
                          }
                      }

                      if (!isP2){
                          if (!isP1){
                              continue;
                          }else{
                              //P2 esta fora, cal redefinirlo amb la interseccio amb l'eix pertinent
                              Point2D.Double[] p = getIntersectionPoint(new Line2D.Double(fp1,fp2),getRectangleGraphArea());//
                              for (int j=0;j<p.length;j++){
                                  if (p[j]!=null){
                                      fp2 = p[j];
                                  }
                              }
                          }
                      } 
                      //ARA JA PODEM DIBUIXAR LA LINIA
                      Line2D.Double l = new Line2D.Double(fp1.x,fp1.y,fp2.x,fp2.y);
                      g1.draw(l);
                  }
              }
              
              //si el punt esta fora ja podem mirar el seguent
              if(!isP1)continue;
              
              //MARKERS
              if(serie.markerSize>0) {
                  drawPatternPoint(g1,fp1,serie.markerSize/2.f,serie.color);
                  //cal recuperar
                  g1.setColor(col);
                  stroke = new BasicStroke(serie.lineWidth);
                  g1.setStroke(stroke);
              }
              //ERROR BARS
              if(serie.showErrBars) {
                  drawErrorBar(g1,pp1,serie.color);
                  //cal recuperar
                  g1.setColor(col);
                  stroke = new BasicStroke(serie.lineWidth);
                  g1.setStroke(stroke);
              }
//            if(ds.showErrBars)drawErrorBars(g2,ds,ds.color);
              
          }
        }

        //dibuixa un sol SENSE COMPROVACIONS
        private void drawPatternPoint(Graphics2D g1, Point2D.Double framePoint, double radiPunt, Color col) {
                g1.setColor(col);
                BasicStroke stroke = new BasicStroke(0.0f);
                g1.setStroke(stroke);
                int dia=(int) FastMath.round(radiPunt*2);
                g1.fillOval((int)FastMath.round(framePoint.x-radiPunt), (int)FastMath.round(framePoint.y-radiPunt), dia,dia);
                g1.drawOval((int)FastMath.round(framePoint.x-radiPunt), (int)FastMath.round(framePoint.y-radiPunt), dia,dia);
        }

        //dibuixa una barra d'error
        private void drawErrorBar(Graphics2D g1, Plottable_point pp, Color col) {
            g1.setColor(col);
            BasicStroke stroke = new BasicStroke(1.0f);
            g1.setStroke(stroke);
            
            double tth = pp.getX();
            double counts = pp.getY();
            double err = pp.getSdy();

            if (err<=0.0f)return;

            Point2D.Double p1 = getFramePointFromDataPoint(pp);

            double ytop = counts+err;
            double ybot = counts-err;

            Point2D.Double ptop = getFramePointFromDataPoint(new DataPoint(tth,ytop,0.0f));
            Point2D.Double pbot = getFramePointFromDataPoint(new DataPoint(tth,ybot,0.0f));

            double modulvert = FastMath.abs(pbot.y-ptop.y);
            double modulHor = FastMath.max(6,modulvert/4.f+1);

            Point2D.Double ptopl = new Point2D.Double(ptop.x-modulHor/2f,ptop.y);
            Point2D.Double ptopr = new Point2D.Double(ptop.x+modulHor/2f,ptop.y);

            Point2D.Double pbotl = new Point2D.Double(pbot.x-modulHor/2f,pbot.y);
            Point2D.Double pbotr = new Point2D.Double(pbot.x+modulHor/2f,pbot.y);

            //comprovem que tot estigui dins
            if (!isFramePointInsideGraphArea(p1) || !isFramePointInsideGraphArea(ptopl) || !isFramePointInsideGraphArea(ptopr) || !isFramePointInsideGraphArea(pbotl) || !isFramePointInsideGraphArea(pbotr)){
                return;
            }

            //ara dibuixem les 3 linies
            g1.draw(new Line2D.Double(ptop.x,ptop.y,pbot.x,pbot.y));
            g1.draw(new Line2D.Double(ptopl.x,ptopl.y,ptopr.x,ptopr.y));
            g1.draw(new Line2D.Double(pbotl.x,pbotl.y,pbotr.x,pbotr.y));
        }

        private void drawHKL(Graphics2D g1, DataSerie serie, Color col){
            for (int i = 0; i < serie.getNpoints(); i++){
                g1.setColor(col);
                BasicStroke stroke = new BasicStroke(serie.lineWidth);
                g1.setStroke(stroke);

                //despres del canvi a private de seriePoints
                double tth = serie.getPointWithCorrections(i,plotwithbkg).getX();

                //la X es la 2THETA pero la Y hauria de ser el punt de menor intensitat de OBS més un hkloffset (en pixels, definit a patt1d)
                double fx = getFrameXFromDataPointX(tth);
                double fy = getFrameYFromDataPointY(0.0+serie.getYOff());
                Point2D.Double ptop = new Point2D.Double(fx, fy);
                Point2D.Double pbot = new Point2D.Double(fx, fy+serie.getScale());
                
                //comprovem que tot estigui dins
                if (!isFramePointInsideGraphArea(ptop) || !isFramePointInsideGraphArea(pbot)){
                    continue;
                }

                //ara dibuixem la linia
                g1.draw(new Line2D.Double(ptop.x,ptop.y,pbot.x,pbot.y));
                
            }
        }
        
       
        //draw vertical lines
        private void drawREF(Graphics2D g1, DataSerie serie, Color col){
            for (int i = 0; i < serie.getNpoints(); i++){

//                serie.lineWidth=1.2f; //TODO revisar perque vaig fer aixo...
                BasicStroke stroke = new BasicStroke(serie.lineWidth);
                switch (FastMath.round(serie.markerSize)) {
                    case 1:
                        stroke = new BasicStroke(serie.lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[]{1,2}, 0);
                        break;
                    case 2:
                        stroke = new BasicStroke(serie.lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[]{1,4}, 0);
                        break;
                    case 3:
                        stroke = new BasicStroke(serie.lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[]{2,4}, 0);
                        break;
                    default:
                        stroke = new BasicStroke(serie.lineWidth);
                        break;
                    
                }

                //despres del canvi a private de seriePoints
//                Plottable_point pp = serie.getPointWithCorrections(i,plotwithbkg); //ja aplica l'escala --PROBLEMA tambe aplica Yoffset... i la lia
                Plottable_point pp = serie.getPointWithCorrections(i, serie.getZerrOff(), 0, serie.getScale(), plotwithbkg);
                
                double tth = pp.getX();
                double inten = 100;
//                double scale = 1.0;
                if(showDBCompoundIntensity) {
                    inten = pp.getY(); //normalitzada a 100
//                    scale = serie.getScale();
                }
                
                double fx = getFrameXFromDataPointX(tth);
                //check if yoff
                int yOffPix=(int) serie.getYOff();
//                if (serie.getYOff()>0) {
//                    yOffPix=(int) getFrameYFromDataPointY(serie.getYOff());
//                }
                //FAREM QUE EN AQUEST CAS EL Yoff sigui en pixels directament
                
//                log.writeNameNumPairs("config", true, "tth,fx,inten,scale,yOffPix", tth,fx,inten,scale,yOffPix);
                log.writeNameNumPairs("config", true, "tth,fx,inten,yOffPix", tth,fx,inten,yOffPix);
                
//                drawVerticalLine(g1,fx,inten,yOffPix,scale,"",col,stroke);
                drawVerticalLine(g1,fx,inten,yOffPix,"",col,stroke);
                
                if(splitDBCompound) {
                    //fem una altra linia vertical des de sota de tot d'uns 10 pixels
                    drawVerticalLine(g1,fx,-10,10*nrefseries,"",col,new BasicStroke(serie.lineWidth));
                }
            }
        }
        
        //label to put next to line at the top,
        //frameX is the pixel in X of the vertical line
        //percentage of vertical space occupied (from bottom to top)
        //Sep2019 afegit Yofset de la serie
        // if percent<0 it will be considered number of pixels from bottom
        private void drawVerticalLine(Graphics2D g1, double frameX, double percent, int yOffset, String label, Color col, BasicStroke stroke) {
            if (!isFramePointInsideXGraphArea(frameX)) return;
            if (percent==0)return;
            g1.setColor(col);
            g1.setStroke(stroke);
            int ytop = gapAxisTop+padding5px;
            int ybot = panelH-gapAxisBottom-padding5px-yOffset; //al ser en pixels aplico directament Yoffset
//            if (yOffset!=0)ybot=yOffset;
            int dist = FastMath.abs(ybot-ytop); //faig abs per si de cas...
//            int dist = (int) (FastMath.abs(ybot-ytop)*scale); //faig abs per si de cas...
            Point2D.Double ptop = new Point2D.Double(frameX, ybot - dist * (percent/100.)); //100% es com tenir Ytop
            Point2D.Double pbot = new Point2D.Double(frameX, ybot);
            if (percent<0) {
                ptop=new Point2D.Double(frameX, ybot + percent); //considero pixels (sumo perque es negatiu... com restar)
            }
            
            log.writeNameNumPairs("config", true, "ytop,ybot,dist,ybot - dist * (percent/100.)", ytop,ybot,dist,ybot - dist * (percent/100.));

            
            //linia de dalt a baix
            g1.draw(new Line2D.Double(ptop.x,ptop.y,pbot.x,pbot.y));
            
            //ara el label
            if (!label.isEmpty()) {
                //escribim al costat de la linia
                Font font = g1.getFont();
                g1.setFont(g1.getFont().deriveFont(g1.getFont().getSize()+def_axisL_fsize));
                double[] swh = getWidthHeighString(g1,label);
                double sy = gapAxisTop+padding5px + swh[1];
                double sx = frameX + padding5px;
                g1.drawString(label, (float)sx,(float)sy);
                g1.setFont(font); //recuperem font defecte
            }
            
        }
        
        private void drawLegend(Graphics2D g1){

//            int rectMaxWidth = 300;
//            int currentMaxWidth = 0;
//            int entryHeight = 25;
            int margin = 5; //podria fer servir el padding5px
            int linelength = 15; //longitud de la linia a la llegenda
            float strokewidth = 3;
            
            FontRenderContext frc = g1.getFontRenderContext();
            
            //1r mirem i definim mides
            double entryMaxWidth = 1;
            double entryMaxHeight = 1;
            int entries = 0;
            for (Plottable p:dataToPlot) {
                for (DataSerie ds:p.getDataSeries()) {
                    if (!ds.plotThis)continue;
                    if (ds.isEmpty())continue;
                    //quines series no volem mostrar?
                    if (ds.getTipusSerie()==SerieType.peaks)continue;
                    if (ds.getTipusSerie()==SerieType.bkg)continue;
                    
                    TextLayout lay = new TextLayout(ds.serieName, g1.getFont().deriveFont(g1.getFont().getSize()+def_legend_fsize), frc);
                    double entryWidth = lay.getBounds().getWidth();
                    double entryHeight = lay.getBounds().getHeight();
                    if (entryWidth>entryMaxWidth)entryMaxWidth=entryWidth;
                    if (entryHeight>entryMaxHeight)entryMaxHeight=entryHeight;
                    entries++;
                }
            }
            //ara ja podem calcular el width del quadrat == margin*2+linelength+margin+entryMaxWidth+margin*2
            int rectWidth = (int)FastMath.round(entryMaxWidth + margin*5 + linelength);
            //i el height del quadrat == margin*2 + entryMaxHeight*entries + margin*(entries-1) + margin*2
            int rectHeight = (int)FastMath.round(entryMaxHeight*entries + margin*(entries-1+4));
            
            //dibuixem el quadrat:
            if (autoPosLegend){
                legendX = panelW-padding5px*2-rectWidth;
                legendY = padding5px*2;
            }else{ //TODO revisar/actualitzar
                if (legendX>panelW-padding5px-2*margin) legendX=panelW-padding5px-2*margin;
                if (legendX<padding5px) legendX=padding5px;
                if (legendY<padding5px) legendY=padding5px;
                if (legendY>panelH-padding5px-2*margin) legendY=panelH-padding5px-2*margin;
            }

            
            //provo de desactivar el fill... hauria d'agafar el color de fons del pattern
//            if (lightTheme){
//                g1.setColor(Light_Legend_bkg);    
//            }else{
//                g1.setColor(Dark_Legend_bkg);
//            }
//            g1.fillRect(legendX,legendY,rectWidth,rectHeight);
            
            if (legend_opaque) {
                if (lightTheme){
                    g1.setColor(Light_Legend_bkg);    
                }else{
                    g1.setColor(Dark_Legend_bkg);
                }    
                g1.fillRect(legendX,legendY,rectWidth,rectHeight);
            }
            
            if (lightTheme){
                g1.setColor(Light_Legend_line);    
            }else{
                g1.setColor(Dark_Legend_line);
            }
            BasicStroke stroke = new BasicStroke(1.0f);
            g1.setStroke(stroke);
            g1.drawRect(legendX,legendY,rectWidth,rectHeight);
            
            try {
                entries = 0;
                for (Plottable p:dataToPlot) {
                    for (DataSerie ds:p.getDataSeries()) {
                        if (!ds.plotThis)continue;
                        if (ds.isEmpty())continue;
                        if (ds.getTipusSerie()==SerieType.peaks)continue;
                        if (ds.getTipusSerie()==SerieType.bkg)continue;
                        stroke = new BasicStroke(strokewidth);
                        g1.setStroke(stroke);
                        g1.setColor(ds.color);

                        //dibuixem primer la linia (si s'escau)
                        int l_iniX = legendX+margin*2;
                        int l_finX = l_iniX+linelength;
                        int l_y = (int) (legendY+margin*2+entries*(entryMaxHeight)+entries*margin); //aixo es a DALT, cal posar-ho al mig per linia horitzontal
                        if (ds.lineWidth>0){
                            if (ds.getTipusSerie()==SerieType.hkl){
                                int gap = (int) (entryMaxHeight*0.2f); //era 20...
                                //LINIA VERTICAL
                                int centreX = (int) ((l_iniX+l_finX)/2.f);
                                int l_iniY = l_y+gap;
                                int l_finY = l_iniY+(int)entryMaxHeight-gap;
                                Line2D.Float l = new Line2D.Float(centreX,l_iniY,centreX,l_finY);
                                g1.draw(l);
                            }else{
                                //LINIA NORMAL HORITZONAL
                                int l_y_cen=l_y+(int)FastMath.round(entryMaxHeight/2.);
                                Line2D.Float l = new Line2D.Float(l_iniX,l_y_cen,l_finX,l_y_cen);
                                g1.draw(l);
                            }
                        }
                        //dibuixem els markers (si s'escau)
                        if (ds.markerSize>0){
                            int sep = (int) (FastMath.abs(l_iniX-l_finX)/5.f);
                            int x1 = l_iniX+sep;
                            int x2 = l_iniX+sep*4;
                            stroke = new BasicStroke(0.0f);
                            g1.setStroke(stroke);
                            double radiPunt = ds.markerSize/2.f;
                            int l_y_cen=l_y+(int)FastMath.round(entryMaxHeight/2.);
                            g1.fillOval((int)FastMath.round(x1-radiPunt), (int)FastMath.round(l_y_cen-radiPunt), FastMath.round(ds.markerSize), FastMath.round(ds.markerSize));
                            g1.fillOval((int)FastMath.round(x2-radiPunt), (int)FastMath.round(l_y_cen-radiPunt), FastMath.round(ds.markerSize), FastMath.round(ds.markerSize));
                        }
                        //recuperem stroke width per si de cas hi havia markers
                        stroke = new BasicStroke(strokewidth);
                        g1.setStroke(stroke);

                        //ara el text
                        int t_X = l_finX+margin; 
                        int t_Y = (int) (l_y+entryMaxHeight-strokewidth/2.);
                        TextLayout lay = new TextLayout(ds.serieName, g1.getFont().deriveFont(g1.getFont().getSize()+def_legend_fsize), frc);
                        lay.draw(g1, t_X,t_Y);
                        entries++;
                    }
                }
            } catch (Exception e) {
                legendX = legendX - 10;
                repaint();
            }
        }

        private void drawPeaks(Graphics2D g1, DataSerie ds, Color col){
            //only for "peaks" series of the selected series
            int gapPixels = 5; //gap between top of peak and line
            int sizePix = 20;

            if (ds!=null){ //there is a peaks serie
                if (ds.isEmpty())return; //no peaks
                for (int i=0;i<ds.getNpoints();i++) {
                    Plottable_point pt = ds.getPointWithCorrections(i,plotwithbkg);
                    Point2D.Double ptop = getFramePointFromDataPoint(pt);
                    if(!isFramePointInsideGraphArea(ptop))continue;
//                  //ara fem una linia amunt recta
                    ptop.y=ptop.y-gapPixels;
                    //draw LIne
                    BasicStroke stroke = new BasicStroke(2.0f);
                    g1.setStroke(stroke);
                    g1.setColor(col.darker());
                    g1.drawLine((int)ptop.x, (int)ptop.y, (int)ptop.x, (int)ptop.y-sizePix);
                }
            }
        }


        private double[] getWidthHeighString(Graphics2D g1, String s){
            double[] w_h = new double[2];
            Font font = g1.getFont();
            FontRenderContext frc = g1.getFontRenderContext();
            w_h[0] = font.getStringBounds(s, frc).getWidth();
            w_h[1] =  font.getStringBounds(s, frc).getHeight();
            return w_h;
        }
    }


}
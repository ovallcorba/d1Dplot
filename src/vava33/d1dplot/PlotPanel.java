package com.vava33.d1dplot;

/**
 * D1Dplot
 * 
 * Plotting panel
 * 
 * Plotpanel pot tenir unes series propies que no s'han de mostrar a cap taula. Per exemple
 * el threshold del fons quan fem un peak search. Aquestes s'han de plotejar després de les
 * dataseries de les dades.
 * 
 * TODO: També s'hi podria afegir el estimBkg points de dataserie_pattern. De moment no ho faig però
 * si que tan bon punt tingui versio funcional ho he de fer. 
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

import com.vava33.d1dplot.auxi.PDCompound;
import com.vava33.d1dplot.data.DataPoint;
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.d1dplot.data.Plottable;
import com.vava33.d1dplot.data.Plottable_point;
import com.vava33.d1dplot.data.SerieType;
import com.vava33.d1dplot.data.Xunits;
import com.vava33.d1dplot.index.IndexSolution;
import com.vava33.jutils.FileUtils;
import com.vava33.jutils.Options;
import com.vava33.jutils.VavaLogger;

import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JLabel;

import org.apache.commons.math3.util.FastMath;

import javax.swing.border.TitledBorder;

public class PlotPanel {

    private List<Plottable> dataToPlot; //(dataseries inside plottables)

    //TEMES (aquests son final de moment perquè no els modifico enlloc)
    private static final Color Dark_bkg = Color.BLACK;
    private static final Color Dark_frg = Color.WHITE;
    private static final Color Light_bkg = Color.WHITE;
    private static final Color Light_frg = Color.BLACK;
    private static final Color Light_Legend_bkg = Color.LIGHT_GRAY.brighter();
    private static final Color Light_Legend_line = Color.BLACK;
    private static final Color Dark_Legend_bkg = Color.DARK_GRAY.darker();
    private static final Color Dark_Legend_line = Color.WHITE;
    
    //PARAMETRES VISUALS amb els valors per defecte TODO: a la llarga no harien de ser estatics, he de canviar el sistema d'opcions
    private boolean lightTheme = true;
    private int gapAxisTop = 12;
    private int gapAxisBottom = 35;
    private int gapAxisRight = 12;
    private int gapAxisLeft = 80;
    private int padding = 10;
    private int AxisLabelsPadding = 2;
    private String xlabel = "2"+D1Dplot_global.theta+" (º)";
    private String ylabel = "Intensity";
    private int def_nDecimalsX = 3;
    private int def_nDecimalsY = 1;
    //sizes relative to default one (12?)
    private float def_axis_fsize = 0.f;
    private float def_axisL_fsize = 0.f;
    private boolean plotwithbkg=false; //pel PRF!!
    private Color colorDBcomp = Color.blue;
    
    //CONVENI DEFECTE:
    //cada 100 pixels una linia principal i cada 25 una secundaria
    //mirem l'amplada/alçada del graph area i dividim per tenir-ho en pixels        
    private double incXPrimPIXELS = 100;
    private double incXSecPIXELS = 25;
    private double incYPrimPIXELS = 100;
    private double incYSecPIXELS = 25;
    private static final int minZoomPixels = 5; //el faig final i general perque no el modifiquem
    private double facZoom = 1.1f;

    // DEFINICIO BUTONS DEL MOUSE
    private int MOURE = MouseEvent.BUTTON2;
    private int CLICAR = MouseEvent.BUTTON1;
    private int ZOOM_BORRAR = MouseEvent.BUTTON3;

    private int div_PrimPixSize = 8;
    private int div_SecPixSize = 4;
    private boolean verticalYlabel = false;
    private boolean verticalYAxe = true;

    private static final String className = "PlotPanel";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);

    private double xMin = 0;
    private double xMax = 60;
    private double yMin = 0;
    private double yMax = 100000;
    private double incX = 10;
    private double incY = 10000;
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
    boolean negativeYAxisLabels = false;
    
    //Parametres de visualitzacio llegenda
    boolean showLegend = true;
    boolean autoPosLegend = true;
    int legendX = -99;
    int legendY = -99;
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
    boolean showIndexSolution = false;
    boolean applyScaleFactorT2 = false;
    float scaleFactorT2ang = 100;
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
    private JTextField txtXmax;
    private JTextField txtYmin;
    private JTextField txtYmax;
    private JTextField txtNdivx;
    private JLabel lblXdiv;
    private JLabel lblYdiv;
    private JLabel lblNdivx;
    private JLabel lblNdivy;
    private JTextField txtNdivy;
    private JLabel lblWindow;
    private JLabel lblXmax;
    private JLabel lblYmin;
    private JLabel lblYmax;
    private JPanel statusPanel;
    private JLabel lblTthInten;
    private JButton btnResetView;
    private JLabel lblDsp;
    private JLabel lblHkl;

//    private D1Dplot_main mainframe;
    private JPanel panel;
    private JPanel panel_1;
    private JPanel plotPanel;
    
	/**
     * Create the panel.
     */
    public PlotPanel(Options opt) {
//        this.mainframe=m;
        this.readOptions(opt);
        this.plotPanel = new JPanel();
        this.plotPanel.setBackground(Color.WHITE);
        this.plotPanel.setLayout(new MigLayout("insets 0", "[grow]", "[][grow][]"));
        
        JPanel buttons_panel = new JPanel();
        buttons_panel.setBorder(null);
        this.plotPanel.add(buttons_panel, "cell 0 0,grow");
        buttons_panel.setLayout(new MigLayout("insets 2", "[grow][grow][][]", "[grow]"));
        
        panel = new JPanel();
        panel.setBorder(new TitledBorder(null, "Divisions", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        buttons_panel.add(panel, "cell 0 0,grow");
        panel.setLayout(new MigLayout("insets 1", "[][grow][][grow 50][][grow][][grow 50]", "[]"));
        
        lblXdiv = new JLabel("X incr");
        lblXdiv.setToolTipText("x major division increment (with values shown, in x units)");
        panel.add(lblXdiv, "cell 0 0,alignx right");
        
        txtXdiv = new JTextField();
        txtXdiv.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtXdiv_actionPerformed(e);
            }
        });
        panel.add(txtXdiv, "cell 1 0,growx");
        txtXdiv.setText("xdiv");
        txtXdiv.setColumns(10);
        
        lblNdivx = new JLabel("X sub");
        lblNdivx.setToolTipText("number of X subdivisions");
        panel.add(lblNdivx, "cell 2 0,alignx right");
        
        txtNdivx = new JTextField();
        txtNdivx.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtNdivx_actionPerformed(e);
            }
        });
        panel.add(txtNdivx, "cell 3 0,growx");
        txtNdivx.setText("NdivX");
        txtNdivx.setColumns(10);
        
        lblYdiv = new JLabel("Y incr");
        lblYdiv.setToolTipText("y major division increment (with values shown, in y units)");
        panel.add(lblYdiv, "cell 4 0,alignx right");
        
        txtYdiv = new JTextField();
        txtYdiv.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtYdiv_actionPerformed(e);
            }
        });
        panel.add(txtYdiv, "cell 5 0,growx");
        txtYdiv.setText("ydiv");
        txtYdiv.setColumns(10);
        
        lblNdivy = new JLabel("Y sub");
        lblNdivy.setToolTipText("number of Y subdivisions");
        panel.add(lblNdivy, "cell 6 0,alignx right");
        
        txtNdivy = new JTextField();
        txtNdivy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtNdivy_actionPerformed(e);
            }
        });
        panel.add(txtNdivy, "cell 7 0,growx");
        txtNdivy.setText("NdivY");
        txtNdivy.setColumns(10);
        
        chckbxFixedAxis = new JCheckBox("Fix Axes");
        chckbxFixedAxis.setSelected(true);
        chckbxFixedAxis.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                do_chckbxFixedAxis_itemStateChanged(e);
            }
        });
        
        panel_1 = new JPanel();
        panel_1.setBorder(new TitledBorder(null, "Range", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        buttons_panel.add(panel_1, "cell 1 0,grow");
        panel_1.setLayout(new MigLayout("insets 1", "[][grow][][grow][][grow][][grow][grow]", "[]"));
        
        lblWindow = new JLabel("X min");
        panel_1.add(lblWindow, "cell 0 0,alignx right");
        lblWindow.setToolTipText("");
        
        txtXmin = new JTextField();
        txtXmin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtXmin_actionPerformed(e);
            }
        });
        panel_1.add(txtXmin, "cell 1 0,growx");
        txtXmin.setText("Xmin");
        txtXmin.setColumns(10);
        
        lblXmax = new JLabel("X max");
        panel_1.add(lblXmax, "cell 2 0,alignx right");
        
        txtXmax = new JTextField();
        txtXmax.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtXmax_actionPerformed(e);
            }
        });
        panel_1.add(txtXmax, "cell 3 0,growx");
        txtXmax.setText("xmax");
        txtXmax.setColumns(10);
        
        lblYmin = new JLabel("Y min");
        panel_1.add(lblYmin, "cell 4 0,alignx right");
        
        txtYmin = new JTextField();
        txtYmin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtYmin_actionPerformed(e);
            }
        });
        panel_1.add(txtYmin, "cell 5 0,growx");
        txtYmin.setText("ymin");
        txtYmin.setColumns(10);
        
        lblYmax = new JLabel("Y max");
        panel_1.add(lblYmax, "cell 6 0,alignx right");
        
        txtYmax = new JTextField();
        txtYmax.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtYmax_actionPerformed(e);
            }
        });
        panel_1.add(txtYmax, "cell 7 0,growx");
        txtYmax.setText("ymax");
        txtYmax.setColumns(10);
        buttons_panel.add(chckbxFixedAxis, "cell 2 0,growx");
        
        btnResetView = new JButton("Reset View");
        btnResetView.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_btnResetView_actionPerformed(e);
            }
        });
        buttons_panel.add(btnResetView, "cell 3 0,growx");
        
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
        
        this.plotPanel.add(graphPanel, "cell 0 1,grow");
        
        statusPanel = new JPanel();
        this.plotPanel.add(statusPanel, "cell 0 2,grow");
        statusPanel.setLayout(new MigLayout("insets 2", "[][][grow]", "[]"));
        
        lblTthInten = new JLabel("X,Y");
        lblTthInten.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 12));
        statusPanel.add(lblTthInten, "cell 0 0,alignx left,aligny center");
        
        lblDsp = new JLabel("dsp");
        statusPanel.add(lblDsp, "cell 1 0");
        
        lblHkl = new JLabel("hkl");
        statusPanel.add(lblHkl, "cell 2 0,alignx right");

        
        inicia();
    }

    private void inicia(){
        nTotalOpenedDatSeries = 0;
        this.dataToPlot = new ArrayList<Plottable>();
        this.selectedSeries = new ArrayList<DataSerie>();
        
        bkgseriePeakSearch=new DataSerie(SerieType.bkg,Xunits.tth,null); //TODO millorable
        bkgEstimP=new DataSerie(SerieType.bkgEstimP,Xunits.tth,null);//TODO millorable
        
        div_incXPrim = 0;
        div_incXSec = 0;
        div_incYPrim = 0;
        div_incYSec = 0;
        div_startValX = 0;
        div_startValY = 0;

        this.txtNdivx.setText("");
        this.txtNdivy.setText("");
        this.txtXdiv.setText("");
        this.txtXmax.setText("");
        this.txtXmin.setText("");
        this.txtYdiv.setText("");
        this.txtYmax.setText("");
        this.txtYmin.setText("");
        
        fixAxes = chckbxFixedAxis.isSelected();
        if (fixAxes) {
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
                            logdebug("now shift is PRESSED");
                        }
                        break;

                    case KeyEvent.KEY_RELEASED:
                        if (ke.getKeyCode() == KeyEvent.VK_SHIFT) {
                            shiftPressed = false;
                            logdebug("now shift is NOT pressed");
                        }
                        break;
                    }
                    return false;
            }
        });
        
    }
    
	public JPanel getPlotPanel() {
		return plotPanel;
	}
	
	private void assignColorDataSeriesIfNecessary(Plottable p) {
	    for (DataSerie ds: p.getDataSeries()) {
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
//	    this.setColor(p);
	  //nomes pintarem les series DAT
	    this.assignColorDataSeriesIfNecessary(p);
	    this.dataToPlot.add(p);
	    this.actualitzaPlot(); //TODO: sempre es voldrà? ho posem com a opcio millor? posar-ho com a opcio? ja que això fa que es repeteixi molts cops per duplicat
	}
	
	   public void reassignColorPatterns(){ //TODO revisar perque nomes es per tipusserieDAT
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
//	public int indexOf(Plottable p) {
//        return this.dataToPlot.indexOf(p);
//    }
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
			if (isDebug())ex.printStackTrace();
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
	        if (isDebug())ex.printStackTrace();
	    }
	    return null; //or first non plotted? --> return this.dataToPlot.get(0).getDataSerie();
	}

	public Plottable getFirstSelectedPlottable() {
        return selectedSeries.get(0).getParent();
    }
	
	public int indexOfPlottableData(Plottable p) {
	    return dataToPlot.indexOf(p);
	}

//	public Set<Plottable> getSelectedPlottables(){
//	    Set<Plottable> selectedPlottables = new HashSet();
//	    for (DataSerie ds:selectedSeries) {
//	        selectedPlottables.add(ds.getParent());
//	    }
//	    return selectedPlottables;
//	}
	
	public void replacePlottable(int index, Plottable newPlottable) {
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
	        this.calcMaxMinXY();
	        this.xrangeMax=this.xMax;
	        this.xrangeMin=this.xMin;
	        this.yrangeMax=this.yMax;
	        this.yrangeMin=this.yMin;
	        
	        this.calcScaleFitX();
	        this.calcScaleFitY();
	        
	        if (!checkIfDiv() || resetAxes){
	            this.autoDivLines();
	        }
	        this.actualitzaPlot();
	    }


    
    //CAL COMPROVAR QUE ESTIGUI DINS DEL RANG PRIMER I CORREGIR l'OFFSET sino torna NULL
    private Point2D.Double getFramePointFromDataPoint(Plottable_point dpoint){
          return new Point2D.Double(this.getFrameXFromDataPointX(dpoint.getX()),this.getFrameYFromDataPointY(dpoint.getY()));
            
    }
    
    private double getFrameXFromDataPointX(double xdpoint){
          double xfr = ((xdpoint-this.xrangeMin) * this.scalefitX) + gapAxisLeft + padding;
          return xfr;    
    }
    
    private double getFrameYFromDataPointY(double ydpoint){
        double yfr = graphPanel.getHeight()-(((ydpoint-this.yrangeMin) * this.scalefitY) + gapAxisBottom + padding);
        return yfr;    
  }
    

    private Point2D.Double getDataPointFromFramePoint(Point2D.Double framePoint){
        if (isFramePointInsideGraphArea(framePoint)){
            double xdp = ((framePoint.x - gapAxisLeft - padding) / this.scalefitX) + this.xrangeMin;
            double ydp = (-framePoint.y+graphPanel.getHeight()-gapAxisBottom - padding)/this.scalefitY +this.yrangeMin;
            return new Point2D.Double(xdp,ydp);
        }else{
            return null;
        }
    }
    
    private Point2D.Double getDataPointFromFramePointIgnoreIfInside(Point2D.Double framePoint){
            double xdp = ((framePoint.x - gapAxisLeft - padding) / this.scalefitX) + this.xrangeMin;
            double ydp = (-framePoint.y+graphPanel.getHeight()-gapAxisBottom - padding)/this.scalefitY +this.yrangeMin;
            return new Point2D.Double(xdp,ydp);
    }
    
    private Plottable_point getDataPointDPFromFramePoint(Point2D.Double framePoint){
        if (isFramePointInsideGraphArea(framePoint)){
            double xdp = ((framePoint.x - gapAxisLeft - padding) / this.scalefitX) + this.xrangeMin;
            double ydp = (-framePoint.y+graphPanel.getHeight()-gapAxisBottom - padding)/this.scalefitY +this.yrangeMin;
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
        double x_low = gapAxisLeft+padding;
        double x_high = x_low + this.calcPlotSpaceX();
        if ((px>x_low)&&(px<x_high))return true;
        return false;
    }
    
    private Rectangle2D.Double getRectangleGraphArea(){
        double xtop = gapAxisLeft+padding;
        double ytop = gapAxisTop+padding;
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
    
    
    //NOMES S'HAURIA DE CRIDAR QUAN OBRIM UN PATTERN (per aixo private)
    private void autoDivLines(){
        this.div_startValX=this.xrangeMin;
        this.div_startValY=this.yrangeMin;
        
        //ara cal veure a quan es correspon en les unitats de cada eix
        double xppix = this.getXunitsPerPixel();
        double yppix = this.getYunitsPerPixel();
        
//        if(isDebug())log.writeNameNumPairs("fine", true, "xppix,yppix",xppix,yppix);
        
        txtNdivx.setText(String.valueOf(incXPrimPIXELS/incXSecPIXELS));
        txtNdivy.setText(String.valueOf(incYPrimPIXELS/incYSecPIXELS));
        
        this.div_incXPrim=incXPrimPIXELS*xppix;
        this.div_incXSec=incXSecPIXELS*xppix;
        this.div_incYPrim=incYPrimPIXELS*yppix;
        this.div_incYSec=incYSecPIXELS*yppix;
        
        this.txtXdiv.setText(FileUtils.dfX_3.format(this.div_incXPrim));
        this.txtYdiv.setText(FileUtils.dfX_3.format(this.div_incYPrim));

//        this.txtXdiv.setText(String.valueOf(this.getDiv_incXPrim()));
//        this.txtYdiv.setText(String.valueOf(this.getDiv_incYPrim()));
        
//        if(isDebug())log.writeNameNumPairs("fine", true, "div_incXPrim, div_incXSec, div_incYPrim, div_incYSec",div_incXPrim, div_incXSec, div_incYPrim, div_incYSec);

    }
    
    //valor inicial, valor d'increment per les separacions principals (tindran número), n divisions secundaries entre principals
    //iniVal l'hem suprimit d'aqui, la "finestra" no es responsabilitat d'aquesta funcio
    private void customDivLinesX(double incrPrincipals, double nDivisionsSecund){
        
        double currentXIni = this.xrangeMin;
        
//        this.setXrangeMin((int)this.getxMin());

//        this.setDiv_startValX(this.getXrangeMin());
        this.div_startValX=currentXIni;
        this.xrangeMin=currentXIni;
        
        this.div_incXPrim=incrPrincipals;
        this.div_incXSec=incrPrincipals/nDivisionsSecund;
        
        this.txtXdiv.setText(FileUtils.dfX_3.format(this.div_incXPrim));
//        this.txtXdiv.setText(String.valueOf(this.getDiv_incXPrim()));
        
   }
    
    private void customDivLinesY(double incrPrincipals, double nDivisionsSecund){
        double currentYIni = this.yrangeMin;
        
//        this.setYrangeMin(0); //TODO REVISAR SI ES EL COMPORTAMENT QUE VOLEM
        
//        this.setDiv_startValY(this.getYrangeMin());
        this.div_startValY=currentYIni;
        this.yrangeMin=currentYIni;
        
        this.div_incYPrim=incrPrincipals;
        this.div_incYSec=incrPrincipals/nDivisionsSecund;
                
//        this.txtYdiv.setText(String.valueOf(this.getDiv_incYPrim()));
        this.txtYdiv.setText(FileUtils.dfX_3.format(this.div_incYPrim));
        
        if(isDebug())log.writeNameNumPairs("config", true, "div_incXPrim, div_incXSec, div_incYPrim, div_incYSec",div_incXPrim, div_incXSec, div_incYPrim, div_incYSec);
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
        int hkloff = DataSerie.def_hklYOff;
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
                    hkloff=(int)ds.getYOff();
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
            
            double newYframe = this.getFrameYFromDataPointY(0)+hklsize-hkloff;
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
        return graphPanel.getHeight()-gapAxisTop-gapAxisBottom-2*padding;
    }
    //width in pixels of the plot area
    private double calcPlotSpaceX(){
        return graphPanel.getWidth()-gapAxisLeft-gapAxisRight-2*padding;
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
            // TODO: posem maxim?
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
            // TODO: posem maxim?
        } else {
            this.xrangeMin=this.xrangeMin-(inc/scalefitX);
            this.xrangeMax=this.xrangeMax+(inc/scalefitX);
        }
        calcScaleFitX();
     }
    
    private void scrollX(double inc) {
        this.xrangeMin=this.xrangeMin+(inc/scalefitX);
        this.xrangeMax=this.xrangeMax+(inc/scalefitX);
          // TODO: posem maxim?
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
	    
//	    if(isDebug())log.writeNameNums("fine", true, "ranges x y min max", getXrangeMin(),getXrangeMax(),getYrangeMin(),getYrangeMax());
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
        this.xrangeMin=Double.parseDouble(txtXmin.getText());
        this.xrangeMax=Double.parseDouble(txtXmax.getText());
        this.yrangeMin=Double.parseDouble(txtYmin.getText());
        this.yrangeMax=Double.parseDouble(txtYmax.getText());
        this.calcScaleFitX();
        this.calcScaleFitY();
        this.actualitzaPlot();
    }
    
    private void fillWindowValues(){
        this.txtXmax.setText(FileUtils.dfX_3.format(this.xrangeMax));
        this.txtXmin.setText(FileUtils.dfX_3.format(this.xrangeMin));
        this.txtYmax.setText(FileUtils.dfX_3.format(this.yrangeMax));
        this.txtYmin.setText(FileUtils.dfX_3.format(this.yrangeMin));
    }
    
    private void fillWindowValuesDiv(){
        this.txtNdivx.setText(FileUtils.dfX_3.format(this.div_incXPrim/this.div_incXSec));
        this.txtNdivy.setText(FileUtils.dfX_3.format(this.div_incYPrim/this.div_incYSec));
        this.txtXdiv.setText(FileUtils.dfX_3.format(this.div_incXPrim));
        this.txtYdiv.setText(FileUtils.dfX_3.format(this.div_incYPrim));
    }
    
    private void applyDivisions(){
        this.customDivLinesX(Double.parseDouble(txtXdiv.getText()), Double.parseDouble(txtNdivx.getText()));
        this.customDivLinesY(Double.parseDouble(txtYdiv.getText()), Double.parseDouble(txtNdivy.getText()));
        this.actualitzaPlot();        
    }
    
    
    private void logdebug(String s){
        if (D1Dplot_global.isDebug()){
            log.debug(s);
        }
    }
    
    private boolean isDebug(){
        return D1Dplot_global.isDebug();
    }
    private void do_graphPanel_mouseDragged(MouseEvent e) {
//    	log.fine("mouseDragged!!");
//    	log.fine(Boolean.toString(this.mouseDrag));
//    	log.fine(Boolean.toString(e.getButton() == MOURE));
	
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
	            boolean direction = (incX < 0);
	            logdebug("incY"+incY+" zoomIn"+Boolean.toString(direction));
	            this.scrollX(-incX);
	        }else{
	            //fem unzoom
	            boolean zoomIn = (incY < 0);
	            logdebug("incY"+incY+" zoomIn"+Boolean.toString(zoomIn));
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
//	        log.fine("mouseMoved!!");
	        if (arePlottables()){
	            Point2D.Double dp = getDataPointFromFramePoint(new Point2D.Double(e.getPoint().x, e.getPoint().y));
	            if (dp!=null){
	                
	                //get the units from first pattern that is plotted
	                DataSerie ds = this.getFirstPlottedSerie();
	                if (ds==null) return;
	                String Xpref = "X=";
	                String Ypref = "Y(Intensity)=";
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
	                lblTthInten.setText(String.format(" %s%.4f%s %s%.1f%s", Xpref,dtth,Xunit,Ypref,dp.getY(),Yunit));
	                double wl = ds.getWavelength();
	                if((wl>0)&&(ds.getxUnits()==Xunits.tth)){
	                    //mirem si hi ha wavelength i les unitats del primer son tth
	                    double dsp = wl/(2*FastMath.sin(FastMath.toRadians(dtth/2.)));
	                    lblDsp.setText(String.format(" [dsp=%.4f"+D1Dplot_global.angstrom+"]", dsp));
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
                            while (itrhkl.hasNext()){
                                Plottable_point hkl = itrhkl.next();
                                shkl.append(hkl.getInfo()).append(" ; ");
                            }
                            lblHkl.setText(shkl.substring(0, shkl.length()-2));
                        }else {
                            lblHkl.setText("");
                        }
	                }else{
	                    lblHkl.setText("");
	                }
	            }
	        }else{
	//            lblTthInten.setText("");
	//            lblDsp.setText("");
	//            lblHkl.setText("");
	        }
	    }

	// Identificar el bot� i segons quin sigui moure o fer zoom
	    private void do_graphPanel_mousePressed(MouseEvent arg0) {
	        if (!arePlottables())return;
	        this.dragPoint = new Point2D.Double(arg0.getPoint().x, arg0.getPoint().y);
	
	        if (arg0.getButton() == MOURE) {
	            logdebug("Mouse button="+Integer.toString(MOURE));
	            this.clickPoint = new Point2D.Double(arg0.getPoint().x, arg0.getPoint().y);
	            this.mouseDrag = true;
	            this.mouseMove = true;
	        }
	        if (arg0.getButton() == ZOOM_BORRAR) {
	            logdebug("Mouse button="+Integer.toString(ZOOM_BORRAR));
	            this.mouseDrag = true;
	            this.mouseZoom = true;
	        }
	        if (arg0.getButton() == CLICAR) {
	            logdebug("Mouse button="+Integer.toString(CLICAR));
	            //abans d'aplicar el moure mirem si s'està fent alguna cosa
	            if(this.selectingBkgPoints){
	                Plottable_point dp = this.getDataPointDPFromFramePoint(this.dragPoint);
	                this.bkgEstimP.addPoint(dp);
//	                for (DataSerie ds:selectedSeries) {
//	                    if (ds.getTipusSerie()==SerieType.bkgEstimP) {
//	                        ds.addPoint(dp);}
//	                }
//	                DataSerie bkgEstimPoints = this.selectedSeries.get(0).getPatt1D().getBkgEstimPSerie(); //AQUESTA LINIA ES NOVA!!
//	                if (bkgEstimPoints==null)return;
//	                bkgEstimPoints.addPoint(dp);
	            }else if(this.deletingBkgPoints){
	                Plottable_point dp = this.getDataPointDPFromFramePoint(this.dragPoint);
	                Plottable_point toDelete = this.bkgEstimP.getClosestDP(dp,-1,-1,plotwithbkg);
                    if (toDelete!=null){
                        if(isDebug())log.writeNameNums("config", true, "toDelete X,Y",toDelete.getX(),toDelete.getY());
                        logdebug("bkgEstimPoints N = "+this.bkgEstimP.getNpoints());
                        this.bkgEstimP.removePoint(toDelete);
                        logdebug("bkgEstimPoints N = "+this.bkgEstimP.getNpoints());
                    }else{
                        logdebug("toDelete is null");
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
	            logdebug("Mouse button="+Integer.toString(MOURE));
	            this.mouseDrag = false;
	            this.mouseMove = false;
	            Point2D.Double currentPoint = new Point2D.Double(e.getPoint().x, e.getPoint().y);
	            if ((FastMath.abs(this.clickPoint.x-currentPoint.x)<0.5) && (FastMath.abs(this.clickPoint.y-currentPoint.y)<0.5)){
	                this.fitGraph();
	            }
	        }
	        if (e.getButton() == ZOOM_BORRAR){
	            logdebug("Mouse button="+Integer.toString(ZOOM_BORRAR));
	            this.mouseDrag = false;
	            this.mouseZoom = false;            
	        }
	        if (e.getButton() == CLICAR){
	            logdebug("Mouse button="+Integer.toString(CLICAR));
	            this.mouseBox=false;
	        }
	        if (!arePlottables())return;
	        
	        if (e.getButton() == CLICAR) {
	            logdebug("Mouse button="+Integer.toString(CLICAR));
	            //comprovem que no s'estigui fent una altra cosa          
	            if(this.selectingBkgPoints||this.deletingBkgPoints)return;
	            if(this.selectingPeaks||this.deletingPeaks)return;
	
	            //COMPROVEM QUE HI HAGI UN MINIM D'AREA ENTREMIG (per evitar un click sol)
	            if (FastMath.abs(e.getPoint().x-dragPoint.x)<minZoomPixels)return;
	            if (this.sqSelect)if (FastMath.abs(e.getPoint().y-dragPoint.y)<minZoomPixels)return;
	            
	            Point2D.Double dataPointFinal = this.getDataPointFromFramePoint(new Point2D.Double(e.getPoint().x, e.getPoint().y));
	            Point2D.Double dataPointInicial = this.getDataPointFromFramePoint(dragPoint);
	            if(isDebug()){
	                if (dataPointFinal!=null)log.writeNameNums("CONFIG", true, "dataPointFinal", dataPointFinal.x,dataPointFinal.y);
	                log.writeNameNums("CONFIG", true, "e.getPoint", e.getPoint().x, e.getPoint().y);
	                if (dataPointInicial!=null)log.writeNameNums("CONFIG", true, "dataPointInicial", dataPointInicial.x,dataPointInicial.y);
	                log.writeNameNums("CONFIG", true, "dragPoint", dragPoint.x, dragPoint.y);
	            }
	            
	            if (dataPointFinal == null && dataPointInicial==null){
	                logdebug("els dos punts a fora!");
	                return;
	            }
	            
	            if (dataPointFinal == null){
	                logdebug("dataPoint final is null");
	                dataPointFinal = this.getDataPointFromFramePoint(new Point2D.Double(checkFrameXValue(e.getPoint().x),checkFrameYValue(e.getPoint().y)));
	
	              if (dataPointFinal!=null && isDebug())log.writeNameNums("CONFIG", true, "dataPointFinal (after)", dataPointFinal.x,dataPointFinal.y);
	                
	                
	            }
	            if (dataPointInicial==null){
	                logdebug("dataPoint inicial is null");
	                dataPointInicial = this.getDataPointFromFramePoint(new Point2D.Double(checkFrameXValue(dragPoint.x),checkFrameYValue(dragPoint.y)));
	                if (dataPointInicial!=null && isDebug())log.writeNameNums("CONFIG", true, "dataPointInicial (after)", dataPointInicial.x,dataPointInicial.y);
	            }
	            
	            if (dataPointFinal == null || dataPointInicial==null){
	                logdebug("algun punt final encara a fora!");
	                return;
	            }
	
	            double xrmin = FastMath.min(dataPointFinal.x, dataPointInicial.x);
	            double xrmax = FastMath.max(dataPointFinal.x, dataPointInicial.x);
	            if(isDebug())log.writeNameNums("config", true, "xrangeMin xrangeMax", xrmin,xrmax);
	            this.xrangeMin=xrmin;
	            this.xrangeMax=xrmax;
	            this.calcScaleFitX();
	            
	            if (this.sqSelect){
	                double yrmin = FastMath.min(dataPointFinal.y, dataPointInicial.y);
	                double yrmax = FastMath.max(dataPointFinal.y, dataPointInicial.y);
	                if(isDebug())log.writeNameNums("config", true, "yrangeMin yrangeMax", yrmin,yrmax);
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

	private void do_btnResetView_actionPerformed(ActionEvent e) {
	    this.fitGraph();
	}

	private void do_chckbxFixedAxis_itemStateChanged(ItemEvent e) {
	    this.fixAxes=chckbxFixedAxis.isSelected();
	    if (fixAxes==true){
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
    private void do_txtYmax_actionPerformed(ActionEvent e) {
        this.applyWindow();
    }
    private void do_txtXmax_actionPerformed(ActionEvent e) {
        this.applyWindow();
    }
    private void do_txtYmin_actionPerformed(ActionEvent e) {
        this.applyWindow();
    }
    private void do_txtXmin_actionPerformed(ActionEvent e) {
        this.applyWindow();
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
        String legX = Integer.toString(this.legendX);
        String legY = Integer.toString(this.legendY);
        String yVert = Boolean.toString(this.isVerticalYAxe());
        String yVertLabel = Boolean.toString(this.isVerticalYlabel());
        String yVertNeg = Boolean.toString(this.negativeYAxisLabels);
        String fixAxes = Boolean.toString(this.fixAxes);
        
        String incXprim = FileUtils.dfX_4.format(this.div_incXPrim);
        String incXsec = FileUtils.dfX_4.format(this.div_incXSec);
        String incYprim = FileUtils.dfX_4.format(this.div_incYPrim);
        String incYsec = FileUtils.dfX_4.format(this.div_incYSec);
        String startValX = FileUtils.dfX_4.format(this.div_startValX);
        String startValY = FileUtils.dfX_4.format(this.div_startValY);
        String incX = FileUtils.dfX_4.format(this.incX);
        String incY = FileUtils.dfX_4.format(this.incY);
        String scaleX = FileUtils.dfX_4.format(this.scalefitX);
        String scaleY = FileUtils.dfX_4.format(this.scalefitY);
//        String xMax = Double.toString(this.xMax);
//        String xMin = Double.toString(this.xMin);
        String xRangeMax = FileUtils.dfX_4.format(this.xrangeMax);
        String xRangeMin = FileUtils.dfX_4.format(this.xrangeMin);
//        String yMax = Double.toString(this.yMax);
//        String yMin = Double.toString(this.yMin);
        String yRangeMax = FileUtils.dfX_4.format(this.yrangeMax);
        String yRangeMin = FileUtils.dfX_4.format(this.yrangeMin);
        
        String xLabel = this.xlabel;
        String yLabel = this.ylabel;
        
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s %s %s %s %s %s %s %s %s %s %s %s %s\n", theme,bkg,hkl,gridX,gridY,legend,autoLeg,legX,legY,yVert,yVertLabel,yVertNeg,fixAxes));
//        sb.append(String.format("%s %s %s %s %s %s %s %s %s %s %s %s %s %s %s %s\n",incXprim,incXsec,incYprim,incYsec,startValX,startValY,incX,incY,scaleX,scaleY,xMax,xMin,xRangeMax,xRangeMin,yMax,yMin));
        sb.append(String.format("%s %s %s %s %s %s %s %s %s %s %s %s %s %s\n",incXprim,incXsec,incYprim,incYsec,startValX,startValY,incX,incY,scaleX,scaleY,xRangeMax,xRangeMin,yRangeMax,yRangeMin));
        sb.append(xLabel);
        sb.append("\n");
        sb.append(yLabel);
        return sb.toString();
    }
    
    public void setVisualParametersFromSaved(String[] vals1, String[] vals2, String xlabel, String ylabel) {
        try {
            this.setLightTheme(Boolean.parseBoolean(vals1[0]));
            this.plotwithbkg=Boolean.parseBoolean(vals1[1]);
            this.hkllabels=Boolean.parseBoolean(vals1[2]);
            this.showGridX=Boolean.parseBoolean(vals1[3]);
            this.showGridY=Boolean.parseBoolean(vals1[4]);
            this.showLegend=Boolean.parseBoolean(vals1[5]);
            this.autoPosLegend=Boolean.parseBoolean(vals1[6]);
            this.legendX=Integer.parseInt(vals1[7]);
            this.legendY=Integer.parseInt(vals1[8]);
            this.setVerticalYAxe(Boolean.parseBoolean(vals1[9]));
            this.setVerticalYlabel(Boolean.parseBoolean(vals1[10]));
            this.negativeYAxisLabels=Boolean.parseBoolean(vals1[11]);
            this.chckbxFixedAxis.setSelected(Boolean.parseBoolean(vals1[12]));
        }catch(Exception e) {
            e.printStackTrace();
        }

        try {
            this.div_incXPrim=Double.parseDouble(vals2[0]);
            this.div_incXSec=Double.parseDouble(vals2[1]);
            this.div_incYPrim=Double.parseDouble(vals2[2]);
            this.div_incYSec=Double.parseDouble(vals2[3]);
            this.div_startValX=Double.parseDouble(vals2[4]);
            this.div_startValY=Double.parseDouble(vals2[5]);
            this.incX=Double.parseDouble(vals2[6]);
            this.incY=Double.parseDouble(vals2[7]);
            this.scalefitX=Double.parseDouble(vals2[8]);
            this.scalefitY=Double.parseDouble(vals2[9]);
//            this.xMax=Double.parseDouble(vals2[10]);
//            this.xMin=Double.parseDouble(vals2[11]);
            this.xrangeMax=Double.parseDouble(vals2[10]);
            this.xrangeMin=Double.parseDouble(vals2[11]);
//            this.yMax=Double.parseDouble(vals2[12]);
//            this.yMin=Double.parseDouble(vals2[13]);
            this.yrangeMax=Double.parseDouble(vals2[12]);
            this.yrangeMin=Double.parseDouble(vals2[13]);

            this.xlabel=xlabel;
            this.ylabel=ylabel;

            fillWindowValuesDiv();
            fillWindowValues();
            
            applyDivisions();
            applyWindow();

        }catch(Exception e) {
            e.printStackTrace();
        }

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

	public void setXlabel(String xlabel) {
	    this.xlabel = xlabel;
	    this.actualitzaPlot();
	}

	public String getYlabel() {
	    return ylabel;
	}

	public void setYlabel(String ylabel) {
	    this.ylabel = ylabel;
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
	    this.gapAxisTop = opt.getValAsInteger("axisGapTop", this.gapAxisTop);
	    this.gapAxisBottom = opt.getValAsInteger("axisGapBottom", this.gapAxisBottom);
	    this.gapAxisLeft = opt.getValAsInteger("axisGapLeft", this.gapAxisLeft);
	    this.gapAxisRight = opt.getValAsInteger("axisGapRight", this.gapAxisRight);
	    this.padding = opt.getValAsInteger("generalPadding", this.padding);
	    this.AxisLabelsPadding = opt.getValAsInteger("axisLabelsPadding", this.AxisLabelsPadding);
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
	    this.colorDBcomp = opt.getValAsColor("colorDBcomp", this.colorDBcomp);
	}

	//per tal de saber tot el que es pot personalitzar
	public Options createOptionsObject() {
	    /*
	     * Podria posar totes les opcions a un Map<String,Object> de forma que fos automatic. TODO future
	     */
	    
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
	    opt.put("axisGapTop", String.format("%d", this.gapAxisTop));
	    opt.put("axisGapBottom", String.format("%d", this.gapAxisBottom));
	    opt.put("axisGapLeft", String.format("%d", this.gapAxisLeft));
	    opt.put("axisGapRight", String.format("%d", this.gapAxisRight));
	    opt.put("generalPadding", String.format("%d", this.padding));
	    opt.put("axisLabelsPadding", String.format("%d", this.AxisLabelsPadding));
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
        
	    return opt;
	}
	
	

	//  ------------------------------------ PANELL DE DIBUIX
    class Plot1d extends JPanel {

        private static final long serialVersionUID = 1L;

        private int panelW, panelH;
        private Graphics2D g2;
        private boolean saveTransp = false;
//        private int svgFontSize= 30;
        private boolean saveSVG = false;
        
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
        
        public boolean isSaveSVG() {
            return saveSVG;
        }
        public void setSaveSVG(boolean saveSVG) {
            this.saveSVG = saveSVG;
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


        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
//            logdebug("paintComponent PlotPanel");

            if (!this.saveTransp){
                if (lightTheme){
                    this.setBackground(Light_bkg);
                }else{
                    this.setBackground(Dark_bkg);
                }
            }
            
            if (arePlottables()) {

                panelW = this.getWidth();
                panelH = this.getHeight();
                
                BufferedImage off_Image = null;

                if (isSaveSVG()) {
                    g2 = (Graphics2D) g;
                    g2.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON)); // perque es vegin mes suaus...
                  }else {
                      off_Image =
                              new BufferedImage(panelW, panelH,
                                                BufferedImage.TYPE_INT_ARGB);
                      g2 = off_Image.createGraphics();
                      
                      g2.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON)); // perque es vegin mes suaus...
                  }
                
                if (scalefitY<0){
                    calcScaleFitY();    
                }
                if (scalefitX<0){
                    calcScaleFitX();    
                }

                //1st draw axes (and optionally grid)
                this.drawAxes(g2,showGridY,showGridX);

//                int nplottable = 0; //TODO start at 1 or zero, is it necessary?
//                int nds = 0;
                for (Plottable p:dataToPlot) {
                    for (DataSerie ds:p.getDataSeries()) {
                        if (!ds.plotThis)continue;
                        if (ds.isEmpty())continue; //new Març 2019
                        switch (ds.getTipusSerie()){ //TODO aqui es poden implementar peculiaritats: dat, obs, cal, hkl, diff, bkg, bkgEstimP, gr, ref, peaks;
                        case hkl:
                            drawHKL(g2,ds,ds.color);
                            break; 
                        case ref:
                            drawREF(g2,ds,ds.color);
                            break; 
                        case peaks:
                            //nomes els mostrem si el plottable està seleccionat? o sempre?
                            drawPeaks(g2,ds,ds.color);
                            break;
                        default: //dibuix linea normal, (dat, dif, gr, ...)
//                            if(ds.lineWidth>0)drawPatternLine(g2,ds,ds.color); 
//                            if(ds.markerSize>0)drawPatternPoints(g2,ds,ds.color);
//                            if(ds.showErrBars)drawErrorBars(g2,ds,ds.color);
                            drawPattern(g2,ds,ds.color);
                            break;
                        }
//                        nds++;
                    }
//                    nplottable++;
                }
                
                if(applyScaleFactorT2) {
                    BasicStroke stroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[]{2,4}, 0);
                    drawVerticalLine(g2, getFrameXFromDataPointX(scaleFactorT2ang), 100, "x"+FileUtils.dfX_1.format(scaleFactorT2fact), Color.GRAY, stroke);
                }
                
                if (showPeakThreshold){
//                    drawPeaks(g2);
                    //mostrar el fons pel pksearch
                    if (bkgseriePeakSearch==null)return;
                    if (bkgseriePeakSearch.getNpoints()>0) {
                        bkgseriePeakSearch.setTipusSerie(SerieType.bkg); //obliguem tipus serie bkg per pintar linia rosa
//                        drawPatternLine(g2,bkgseriePeakSearch,bkgseriePeakSearch.color);
                        drawPattern(g2,bkgseriePeakSearch,bkgseriePeakSearch.color);
                    }
                }
                
                if (showEstimPointsBackground) {
                    //mostrem dataserie dels punts fons
                    if (bkgEstimP==null)return;
                    if (bkgEstimP.getNpoints()>0) {
                        bkgseriePeakSearch.setTipusSerie(SerieType.bkgEstimP); //obliguem tipus serie per markers size i color
//                        drawPatternPoints(g2,bkgEstimP,bkgEstimP.color);
                        drawPattern(g2,bkgEstimP,bkgEstimP.color);
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
                
                if (!isSaveSVG()) {
                    g.drawImage(off_Image, 0, 0, null);
                  }else {
                      //TODO:aixo estava buit...
                  }
                
            }else {
                //no patterns, podem aprofitar per reiniciar algunes coes
                nTotalOpenedDatSeries=0;
            }
        }

        private void drawAxes(Graphics2D g1, boolean gridY, boolean gridX){
//            logdebug("drawAxes entered");

            //provem de fer linia a 60 pixels de l'esquerra i a 60 pixels de baix (40 i 40 de dalt i a la dreta com a marges)

            double coordXeixY = gapAxisLeft+padding;
            double coordYeixX = panelH-gapAxisBottom-padding;

            Point2D.Double vytop = new Point2D.Double(coordXeixY,gapAxisTop+padding);
            Point2D.Double vybot = new Point2D.Double(coordXeixY,coordYeixX);
            Point2D.Double vxleft = vybot;
            Point2D.Double vxright = new Point2D.Double(panelW-gapAxisRight-padding,coordYeixX);

//            if(isDebug())log.writeNameNums("fine", true, "(axes) vy vx", vytop.x,vytop.y,vybot.x,vybot.y,vxleft.x,vxleft.y,vxright.x,vxright.y);

            if(lightTheme){
                g1.setColor(Light_frg);
            }else{
                g1.setColor(Dark_frg);
            }
            BasicStroke stroke = new BasicStroke(1.0f);
            g1.setStroke(stroke);

            Line2D.Double ordenada = new Line2D.Double(vytop,vybot);  //Y axis vertical
            Line2D.Double abcissa = new Line2D.Double(vxleft, vxright);  //X axis horizontal

            if (verticalYAxe)g1.draw(ordenada);
            g1.draw(abcissa);

            //PINTEM ELS TITOLS DELS EIXOS
            Font font = g1.getFont();
            FontRenderContext frc = g1.getFontRenderContext();
//            if(isDebug())log.fine("default font size="+font.getSize());

            // X-axis (abcissa) label.
            String s = getXlabel();
            double sy = panelH - AxisLabelsPadding;
            g1.setFont(g1.getFont().deriveFont(g1.getFont().getSize()+def_axisL_fsize));
            double sw = g1.getFont().getStringBounds(s, frc).getWidth();
            double sx = (panelW - sw)/2;
//            log.fine("Xaxis label font size="+g1.getFont().getSize());
            g1.drawString(s, (float)sx,(float)sy);
            g1.setFont(font); //recuperem font defecte


            // **** linies divisio eixos
            if (!checkIfDiv())return;
            if (fixAxes) autoDivLines(); //es pot fer mes eficient sense fer-ho cada cop
            //---eix X
            //Per tots els punts les coordenades Y seran les mateixes
            double yiniPrim = coordYeixX - (div_PrimPixSize/2.f); 
            double yfinPrim = coordYeixX + (div_PrimPixSize/2.f);

            //ara dibuixem les Primaries i posem els labels
            double xval = div_startValX;
            while (xval <= xrangeMax){
                if (xval < xrangeMin){
                    xval = xval + div_incXPrim;
                    continue;
                }
                double xvalPix = getFrameXFromDataPointX(xval);
                Line2D.Double l = new Line2D.Double(xvalPix,yiniPrim,xvalPix,yfinPrim);
                g1.draw(l);
                //ara el label sota la linia 
                //                    s = String.format("%.3f", xval);
                s = this.def_xaxis_format.format(xval);
                g1.setFont(g1.getFont().deriveFont(g1.getFont().getSize()+def_axis_fsize));
//                log.fine("Xaxis font size="+g1.getFont().getSize());
                sw = g1.getFont().getStringBounds(s, frc).getWidth();
                double sh = g1.getFont().getStringBounds(s, frc).getHeight();
                double xLabel = xvalPix - sw/2f; //el posem centrat a la linia
                double yLabel = yfinPrim + AxisLabelsPadding + sh;
                g1.drawString(s, (float)xLabel, (float)yLabel);
                xval = xval + div_incXPrim;
                g1.setFont(font);

                if(xval> (int)(1+xMax))break; //provem de posar-ho aqui perque no dibuixi mes enllà
            }

            //ara les secundaries
            double yiniSec = coordYeixX- (div_SecPixSize/2.f); 
            double yfinSec = coordYeixX + (div_SecPixSize/2.f);
            xval = div_startValX;
            while (xval <= xrangeMax){
                if (xval < xrangeMin){
                    xval = xval + div_incXSec;
                    continue;
                }
                double xvalPix = getFrameXFromDataPointX(xval);
                Line2D.Double l = new Line2D.Double(xvalPix,yiniSec,xvalPix,yfinSec);
                g1.draw(l);
                xval = xval + div_incXSec;

                //i ara el grid
                //pel grid, vytop.y sera el punt superior de la linia, yiniPrim sera el punt inferior (AIXO PER LES Y, despres les X es defineixen al bucle)
                if(gridY){
                    BasicStroke dashed = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[]{1,2}, 0);
                    g1.setStroke(dashed);
                    Line2D.Double ld = new Line2D.Double(xvalPix,vytop.y,xvalPix,yiniSec);
                    g1.draw(ld);
                    g1.setStroke(stroke); //recuperem l'anterior
                }

                if(xval> (int)(1+xMax))break; //provem de posar-ho aqui perque no dibuixi mes enllà
            }

            if (verticalYAxe) {
                // Y-axis (ordinate) label.
                s = getYlabel();
                sw = g1.getFont().getStringBounds(s, frc).getWidth();
                double sh =  g1.getFont().getStringBounds(s, frc).getHeight();
                double ylabelheight = sh; //per utilitzar-ho despres
                sx = AxisLabelsPadding;
                sy = sh + AxisLabelsPadding;
                g1.setFont(g1.getFont().deriveFont(g1.getFont().getSize()+def_axisL_fsize));
//                log.fine("Yaxis label font size="+g1.getFont().getSize());
                if (verticalYlabel){
                    sy = (panelH - sw)/2;
                    sx = (ylabelheight/2)+padding;
                    AffineTransform orig = g1.getTransform();
                    g1.rotate(-Math.PI/2,sx,sy);
                    g1.drawString(s,(float)sx,(float)sy);
                    g1.setTransform(orig);
                }else{
                    //el posem sobre l'eix en horitzontal
                    g1.drawString(s,(float)sx,(float)sy);
                }
                g1.setFont(font);
                //---eix Y
                //Per tots els punts les coordenades Y seran les mateixes
                double xiniPrim = coordXeixY - (div_PrimPixSize/2.f); 
                double xfinPrim = coordXeixY + (div_PrimPixSize/2.f);
                //ara dibuixem les Primaries i posem els labels
                double yval = div_startValY;
                while (yval <= yrangeMax){
                    if (yval < yrangeMin){
                        yval = yval + div_incYPrim;
                        continue;
                    }
                    if (!negativeYAxisLabels && (yval<0)){
                        yval = yval + div_incYPrim;
                        continue;
                    }

                    double yvalPix = getFrameYFromDataPointY(yval);
                    Line2D.Double l = new Line2D.Double(xiniPrim, yvalPix, xfinPrim, yvalPix);
                    g1.draw(l);
                    //ara el label a l'esquerra de la linia (atencio a negatius, depen si hi ha l'opcio)
                    //                s = String.format("%.1f", yval);
                    s = this.def_yaxis_format.format(yval);
                    g1.setFont(g1.getFont().deriveFont(g1.getFont().getSize()+def_axis_fsize));
//                    log.fine("Yaxis font size="+g1.getFont().getSize());
                    sw = g1.getFont().getStringBounds(s, frc).getWidth();
                    sh = g1.getFont().getStringBounds(s, frc).getHeight();
                    double xLabel = xiniPrim - AxisLabelsPadding - sw; 
                    double yLabel = yvalPix + sh/2f; //el posem centrat a la linia

                    //Sino hi cap fem la font mes petita
                    double limit = gapAxisLeft;
                    if (verticalYlabel)limit = gapAxisLeft-ylabelheight;
                    while(sw>limit){
                        g1.setFont(g1.getFont().deriveFont(g1.getFont().getSize()-1f));
                        sw = g1.getFont().getStringBounds(s, g1.getFontRenderContext()).getWidth();
                        xLabel = xiniPrim - AxisLabelsPadding - sw;
                    }

                    g1.drawString(s, (float)xLabel, (float)yLabel);
                    g1.setFont(font);                //recuperem font
                    yval = yval + div_incYPrim;
                }

                //ara les secundaries
                double xiniSec = coordXeixY - (div_SecPixSize/2.f); 
                double xfinSec = coordXeixY + (div_SecPixSize/2.f);
                yval = div_startValY;
                while (yval <= yrangeMax){
                    if (yval < yrangeMin){
                        yval = yval + div_incYSec;
                        continue;
                    }
                    if (!negativeYAxisLabels && (yval<0)){
                        yval = yval + div_incYSec;
                        continue;
                    }
                    double yvalPix = getFrameYFromDataPointY(yval);
                    Line2D.Double l = new Line2D.Double(xiniSec,yvalPix,xfinSec,yvalPix);
                    g1.draw(l);
                    yval = yval + div_incYSec;

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
            }
//            log.debug("drawAxes exit");
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
//            if (isFramePointInsideGraphArea(framePoint)){
                g1.setColor(col);
                BasicStroke stroke = new BasicStroke(0.0f);
                g1.setStroke(stroke);
                int dia=(int) FastMath.round(radiPunt*2);
                g1.fillOval((int)FastMath.round(framePoint.x-radiPunt), (int)FastMath.round(framePoint.y-radiPunt), dia,dia);
                g1.drawOval((int)FastMath.round(framePoint.x-radiPunt), (int)FastMath.round(framePoint.y-radiPunt), dia,dia);
//            }
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
//            log.fine("drawHKL entered");
            for (int i = 0; i < serie.getNpoints(); i++){
                g1.setColor(col);
                BasicStroke stroke = new BasicStroke(serie.lineWidth);
                g1.setStroke(stroke);

                //despres del canvi a private de seriePoints
//                double tth = serie.getHKLPoint(i).get2th();
                double tth = serie.getPointWithCorrections(i,plotwithbkg).getX();

                //la X es la 2THETA pero la Y hauria de ser el punt de menor intensitat de OBS més un hkloffset (en pixels, definit a patt1d)
                double fx = getFrameXFromDataPointX(tth);
                double fy = getFrameYFromDataPointY(0.0+serie.getYOff());
                Point2D.Double ptop = new Point2D.Double(fx, fy);
                Point2D.Double pbot = new Point2D.Double(fx, fy+serie.getScale());
                
                
//                fy = fy - hkloff +hklticksize/2f;  //pensem que la Y es cap avall!
//
//                Point2D.Double ptop = new Point2D.Double(fx, fy-hklticksize/2);
//                Point2D.Double pbot = new Point2D.Double(fx, fy+hklticksize/2);

                //comprovem que tot estigui dins
                if (!isFramePointInsideGraphArea(ptop) || !isFramePointInsideGraphArea(pbot)){
                    continue;
                }

                //ara dibuixem la linia
                g1.draw(new Line2D.Double(ptop.x,ptop.y,pbot.x,pbot.y));

            }
//            log.debug("drawHKL exit");
        }
        
        
        //draw vertical lines
        private void drawREF(Graphics2D g1, DataSerie serie, Color col){
//            log.fine("drawREF entered");
            for (int i = 0; i < serie.getNpoints(); i++){

                serie.lineWidth=1.2f;
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
//                BasicStroke stroke = new BasicStroke(serie.getLineWidth());
//                g1.setStroke(stroke);
//                BasicStroke dashed = new BasicStroke(serie.getLineWidth(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[]{1,2}, 0);
                
                //despres del canvi a private de seriePoints
                Plottable_point pp = serie.getPointWithCorrections(i,plotwithbkg);
                double tth = pp.getX();
                double inten = 100;
                if(showDBCompoundIntensity)inten = pp.getY(); //normalitzada a 100
                
                double fx = getFrameXFromDataPointX(tth);
                
                drawVerticalLine(g1,fx,inten,"",col,stroke);
                
            }
//            log.debug("drawREF exit");
        }
        
        //label to put next to line at the top,
        //frameX is the pixel in X of the vertical line
        //percentage of vertical space occupied (from bottom to top)
        private void drawVerticalLine(Graphics2D g1, double frameX, double percent, String label, Color col, BasicStroke stroke) {
            if (!isFramePointInsideXGraphArea(frameX)) return;
            g1.setColor(col);
            g1.setStroke(stroke);
            int ytop = gapAxisTop+padding;
            int ybot = panelH-gapAxisBottom-padding;
            int dist = FastMath.abs(ybot-ytop); //faig abs per si de cas...
            Point2D.Double ptop = new Point2D.Double(frameX, ybot - dist * (percent/100.)); //100% es com tenir Ytop
            Point2D.Double pbot = new Point2D.Double(frameX, ybot);
            //linia de dalt a baix
            g1.draw(new Line2D.Double(ptop.x,ptop.y,pbot.x,pbot.y));
            
            //ara el label
            if (!label.isEmpty()) {
                //escribim al costat de la linia
                Font font = g1.getFont();
                g1.setFont(g1.getFont().deriveFont(g1.getFont().getSize()+def_axisL_fsize));
                //TODO aqui podem canviar la font
                double[] swh = getWidthHeighString(g1,label);
                double sy = gapAxisTop+padding + swh[1];
                double sx = frameX + padding;
                g1.drawString(label, (float)sx,(float)sy);
                g1.setFont(font); //recuperem font defecte
            }
            
//            if ((isFramePointInsideGraphArea(ptop))&&isFramePointInsideGraphArea(pbot)){
//            
//            if (isFramePointInsideGraphArea(new Point2D.Double(ptop.x,ptop.y+1))){//TODO perquè!?=?!
//                //ara dibuixem la linia
//                g1.draw(new Line2D.Double(ptop.x,ptop.y,pbot.x,pbot.y));
//                
//            }
        }

        
        private void drawLegend(Graphics2D g1){

            int rectMaxWidth = 300;
            int currentMaxWidth = 0;
            int entryHeight = 25;
            int margin = 10;
            int linelength = 15;
            float strokewidth = 3;
            Font font = g1.getFont(); //font inicial
            
            if (autoPosLegend){
                legendX = panelW-padding-rectMaxWidth;
                legendY = padding;
//                mainframe.setTxtLegendFromPanel();
            }else{
                if (legendX>panelW-padding-2*margin) legendX=panelW-padding-2*margin;
                if (legendX<padding) legendX=padding;
                if (legendY<padding) legendY=padding;
                if (legendY>panelH-padding-2*margin) legendY=panelH-padding-2*margin;
//                mainframe.setTxtLegendFromPanel();
            }

            
            try {
                int entries = 0;
                for (Plottable p:dataToPlot) {
                    for (DataSerie ds:p.getDataSeries()) {
                        if (!ds.plotThis)continue;
                        if (ds.isEmpty())continue;
                        //quines series no volem mostrar?
                        if (ds.getTipusSerie()==SerieType.peaks)continue;
                        if (ds.getTipusSerie()==SerieType.bkg)continue;
                        //dibuixem primer la linia
                        int l_iniX = legendX+margin;
                        int l_finX = legendX+margin+linelength;
                        int l_y = (int) (legendY+margin+entries*(entryHeight)+FastMath.round(entryHeight/2.));

                        g1.setColor(ds.color);
                        BasicStroke stroke = new BasicStroke(strokewidth);
                        g1.setStroke(stroke);

                        Line2D.Float l = new Line2D.Float(l_iniX,l_y,l_finX,l_y);
                        g1.draw(l);

                        //ara el text
                        int t_X = l_finX+margin; //TODO: revisar si queda millor x2
                        int maxlength = panelW-padding-margin-t_X;
                        String s = ds.serieName; //TODO: POSAR CORRECTAMENT EL NOM
                        g1.setFont(g1.getFont().deriveFont(g1.getFont().getSize()+def_axisL_fsize));
                        double[] swh = getWidthHeighString(g1,s);
                        int count=0;
                        while (swh[0]>maxlength){
                            g1.setFont(g1.getFont().deriveFont(g1.getFont().getSize()-1f));
                            swh = getWidthHeighString(g1,s);
                            count = count +1;
                            if (count>20)throw new Exception();
                        }
                        int t_Y = (int) (l_y-strokewidth+(swh[1]/2.));
                        g1.drawString(s, t_X,t_Y);
                        g1.setFont(font);                //recuperem font

                        int currentWidth = (int) (margin + linelength + margin + swh[0] + margin);
                        if (currentWidth>currentMaxWidth)currentMaxWidth = currentWidth;
                        entries++;
                    }
                }
                

                int rerctheight = entries*entryHeight+2*margin;
                if (lightTheme){
                    g1.setColor(Light_Legend_bkg);    
                }else{
                    g1.setColor(Dark_Legend_bkg);
                }
                g1.fillRect(legendX,legendY,FastMath.min(currentMaxWidth,rectMaxWidth),rerctheight);
                if (lightTheme){
                    g1.setColor(Light_Legend_line);    
                }else{
                    g1.setColor(Dark_Legend_line);
                }
                BasicStroke stroke = new BasicStroke(1.0f);
                g1.setStroke(stroke);
                g1.drawRect(legendX,legendY,FastMath.min(currentMaxWidth,rectMaxWidth),rerctheight);

                //repeteixo lo d'abans per pintar a sobre... no es gaire eficient...
                //NO REPETEIXO EXACTE, AQUI TINDRE EN COMPTE ELS TIPUS DE LINIA
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
                        int l_iniX = legendX+margin;
                        int l_finX = legendX+margin+linelength;
                        int l_y = (int) (legendY+margin+entries*(entryHeight)+FastMath.round(entryHeight/2.));
                        if (ds.lineWidth>0){

                            if (ds.getTipusSerie()==SerieType.hkl){
//                                int gap = (int) ((entryHeight - ds.getScale())/2.); //TODO no se perquè tenia getScale/2
                                int gap = 20;
                                //LINIA VERTICAL
                                int centreX = (int) ((l_iniX+l_finX)/2.f);
                                int l_iniY = (int) (legendY+margin+entries*(entryHeight)+gap);
                                int l_finY = (int) (legendY+margin+entries*(entryHeight)+entryHeight-gap);
                                Line2D.Float l = new Line2D.Float(centreX,l_iniY,centreX,l_finY);
                                g1.draw(l);
                            }else{
                                //LINIA NORMAL HORITZONAL
                                Line2D.Float l = new Line2D.Float(l_iniX,l_y,l_finX,l_y);
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
                            g1.fillOval((int)FastMath.round(x1-radiPunt), (int)FastMath.round(l_y-radiPunt), FastMath.round(ds.markerSize), FastMath.round(ds.markerSize));
                            g1.fillOval((int)FastMath.round(x2-radiPunt), (int)FastMath.round(l_y-radiPunt), FastMath.round(ds.markerSize), FastMath.round(ds.markerSize));
                        }
                        //recuperem stroke width per si de cas hi havia markers
                        stroke = new BasicStroke(strokewidth);
                        g1.setStroke(stroke);

                        //ara el text
                        int t_X = l_finX+margin; //TODO: revisar si queda millor x2
                        int maxlength = panelW-padding-margin-t_X;
                        String s =  ds.serieName; //TODO: POSAR CORRECTAMENT EL NOM
                        g1.setFont(g1.getFont().deriveFont(g1.getFont().getSize()+def_axisL_fsize));
                        double[] swh = getWidthHeighString(g1,s);
                        int count=0;
                        while (swh[0]>maxlength){
                            g1.setFont(g1.getFont().deriveFont(g1.getFont().getSize()-1f));
                            swh = getWidthHeighString(g1,s);
                            count++;
                            if (count>20)throw new Exception();
                        }
                        int t_Y = (int) (l_y-strokewidth+(swh[1]/2.));
                        g1.drawString(s, t_X,t_Y);
                        g1.setFont(font);                //recuperem font

                        int currentWidth = (int) (margin + linelength + margin + swh[0] + margin);
                        if (currentWidth>currentMaxWidth)currentMaxWidth = currentWidth;
                        
                        entries++;
                    }
                }
            } catch (Exception e) {
                if(isDebug())e.printStackTrace();
                logdebug("error writting legend");
                legendX = legendX - 10;
                repaint();
            }
        }

        private void drawPeaks(Graphics2D g1, DataSerie ds, Color col){
            //only for "peaks" series of the selected series
            int gapPixels = 5; //gap between top of peak and line
            int sizePix = 20;

//            DataSerie ds = getFirstSelectedPlottable().getDataSerieByType(SerieType.peaks);
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
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
import java.util.Iterator;

import javax.swing.JPanel;

import com.vava33.d1dplot.auxi.DataHKL;
import com.vava33.d1dplot.auxi.DataPoint;
import com.vava33.d1dplot.auxi.DataSerie;
import com.vava33.d1dplot.auxi.Pattern1D;
import com.vava33.jutils.FileUtils;
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

    private ArrayList<Pattern1D> patterns; //data to plot (series inside pattern1d)

    //TEMES
    private static final Color Dark_bkg = Color.BLACK;
    private static final Color Dark_frg = Color.WHITE;
    private static final Color Light_bkg = Color.WHITE;
    private static final Color Light_frg = Color.BLACK;
    private static final Color Light_Legend_bkg = Color.LIGHT_GRAY.brighter();
    private static final Color Light_Legend_line = Color.BLACK;
    private static final Color Dark_Legend_bkg = Color.DARK_GRAY.darker();
    private static final Color Dark_Legend_line = Color.WHITE;
    private static boolean lightTheme = true;
    
    //PARAMETRES VISUALS
    private static int gapAxisTop = 18;
    private static int gapAxisBottom = 30;
    private static int gapAxisRight = 15;
    private static int gapAxisLeft = 60;
    private static int padding = 10;
    private static int AxisLabelsPadding = 2;
    private String xlabel = "2"+D1Dplot_global.theta+" (º)";
    private String ylabel = "Intensity";
    private static int def_nDecimalsX = 3;
    private static int def_nDecimalsY = 1;
    //sizes relative to default one (12?)
    private static float def_axis_fsize = 0.f;
    private static float def_axisL_fsize = 0.f;
    
    //CONVENI DEFECTE:
    //cada 100 pixels una linia principal i cada 25 una secundaria
    //mirem l'amplada/alçada del graph area i dividim per tenir-ho en pixels        
    private static double incXPrimPIXELS = 100;
    private static double incXSecPIXELS = 25;
    private static double incYPrimPIXELS = 100;
    private static double incYSecPIXELS = 25;

    private static int minZoomPixels = 5; //AQUEST NO CAL COM A OPCIO, ES PER EVITAR FER ZOOM SI NOMES CLICKEM I ENS QUEDEM A MENYS DE 5 PIXELS
    private static double facZoom = 1.1f;

    // DEFINICIO BUTONS DEL MOUSE
    private static int MOURE = MouseEvent.BUTTON2;
    private static int CLICAR = MouseEvent.BUTTON1;
    private static int ZOOM_BORRAR = MouseEvent.BUTTON3;

    private static int div_PrimPixSize = 8;
    private static int div_SecPixSize = 4;
    private static boolean verticalYlabel = false;
    private static boolean verticalYAxe = true;

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
    
    //llegenda
    boolean showLegend = true;
    boolean autoPosLegend = true;
    int legendX = -99;
    int legendY = -99;
    private boolean hkllabels = true;
    private boolean showGridY = false;
    private boolean showGridX = false;
    
    boolean showPeaks = true;
    boolean showBackground = false;
    boolean selectingBkgPoints = false;
    boolean deletingBkgPoints = false;
    boolean selectingPeaks = false;
    boolean deletingPeaks = false;
    DataSerie bkgseriePeakSearch;
    public ArrayList<DataSerie> selectedSeries;
    
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
    public PlotPanel() {
//        this.mainframe = mf;
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
        this.patterns = new ArrayList<Pattern1D>();
        this.selectedSeries = new ArrayList<DataSerie>();
        this.bkgseriePeakSearch = new DataSerie(DataSerie.serieType.bkgEstimP); //la faig estimP perque no em dibuixi linia... TODO
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
	
	public DataSerie getFirstPlottedSerie() {
		try {
			return this.getPatterns().get(0).getSerie(0);
		}catch(Exception ex) {
			if (isDebug())ex.printStackTrace();
		}
		return null;
	}
	
    public void actualitzaPlot() {
		this.getGraphPanel().repaint();
	}
	    
	    // ajusta la imatge al panell, mostrant-la tota sencera (calcula l'scalefit inicial)
	public void fitGraph() {
	    this.resetView(false);
	}

		private void resetView(boolean resetAxes) {
	        this.calcMaxMinXY();
	        this.setXrangeMax(this.getxMax());
	        this.setXrangeMin(this.getxMin());
	        this.setYrangeMax(this.getyMax());
	        this.setYrangeMin(this.getyMin());
	        
	        this.calcScaleFitX();
	        this.calcScaleFitY();
	        
	        if (!checkIfDiv() || resetAxes){
	            this.autoDivLines();
	        }

	        this.actualitzaPlot();
	    }

	private boolean isOneSerieSelected(){
        if (this.getSelectedSeries().isEmpty()){
            log.warning("Select a serie first");
            return false;
        }
        if (this.getSelectedSeries().size()>1){
            log.warning("Select ONE serie only");
            return false;
        }
        return true;
    }

    private boolean arePatterns(){
        return !this.getPatterns().isEmpty();
    }
    
    //CAL COMPROVAR QUE ESTIGUI DINS DEL RANG PRIMER I CORREGIR l'OFFSET sino torna NULL
    private Point2D.Double getFramePointFromDataPoint(DataPoint dpoint){
          return new Point2D.Double(this.getFrameXFromDataPointX(dpoint.getX()),this.getFrameYFromDataPointY(dpoint.getY()));
            
    }
    
    private double getFrameXFromDataPointX(double xdpoint){
          double xfr = ((xdpoint-this.getXrangeMin()) * this.getScalefitX()) + getGapAxisLeft() + padding;
          return xfr;    
    }
    
    private double getFrameYFromDataPointY(double ydpoint){
        double yfr = graphPanel.getHeight()-(((ydpoint-this.getYrangeMin()) * this.getScalefitY()) + getGapAxisBottom() + padding);
        return yfr;    
  }
    

    private Point2D.Double getDataPointFromFramePoint(Point2D.Double framePoint){
        if (isFramePointInsideGraphArea(framePoint)){
            double xdp = ((framePoint.x - getGapAxisLeft() - padding) / this.getScalefitX()) + this.getXrangeMin();
            double ydp = (-framePoint.y+graphPanel.getHeight()-getGapAxisBottom() - padding)/this.getScalefitY() +this.getYrangeMin();
            return new Point2D.Double(xdp,ydp);
        }else{
            return null;
        }
    }
    
    private DataPoint getDataPointDPFromFramePoint(Point2D.Double framePoint){
        if (isFramePointInsideGraphArea(framePoint)){
            double xdp = ((framePoint.x - getGapAxisLeft() - padding) / this.getScalefitX()) + this.getXrangeMin();
            double ydp = (-framePoint.y+graphPanel.getHeight()-getGapAxisBottom() - padding)/this.getScalefitY() +this.getYrangeMin();
            return new DataPoint(xdp,ydp,0);
        }else{
            return null;
        }
    }
    
    //ens diu quant en unitats de X val un pixel (ex 1 pixel es 0.01deg de 2th)
    private double getXunitsPerPixel(){
        return (this.getXrangeMax()-this.getXrangeMin())/this.getRectangleGraphArea().width;
    }
    
    //ens dira quant en unitats de Y val un pixels (ex. 1 pixel son 1000 counts)
    private double getYunitsPerPixel(){
        return (this.getYrangeMax()-this.getYrangeMin())/this.getRectangleGraphArea().height;
    }
    
    private boolean isFramePointInsideGraphArea(Point2D.Double p){
        Rectangle2D.Double r = getRectangleGraphArea();
        return r.contains(p);
    }
    
    private Rectangle2D.Double getRectangleGraphArea(){
        double xtop = getGapAxisLeft()+padding;
        double ytop = getGapAxisTop()+padding;
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
    
//    private boolean isDataPointInsidePlotRange(DataPoint dp){
//        if (dp.getX()>this.getXrangeMin() && dp.getX()<this.getXrangeMax() && dp.getY()>this.getYrangeMin() && dp.getY()<this.getYrangeMax()){
//            return true;
//        }else{
//            return false;
//        }
//    }
    
    //NOMES S'HAURIA DE CRIDAR QUAN OBRIM UN PATTERN (per aixo private)
    private void autoDivLines(){
        this.setDiv_startValX(this.getXrangeMin());
        this.setDiv_startValY(this.getYrangeMin());
        
        //ara cal veure a quan es correspon en les unitats de cada eix
        double xppix = this.getXunitsPerPixel();
        double yppix = this.getYunitsPerPixel();
        
//        if(isDebug())log.writeNameNumPairs("fine", true, "xppix,yppix",xppix,yppix);
        
        txtNdivx.setText(String.valueOf(incXPrimPIXELS/incXSecPIXELS));
        txtNdivy.setText(String.valueOf(incYPrimPIXELS/incYSecPIXELS));
        
        
        this.setDiv_incXPrim(incXPrimPIXELS*xppix);
        this.setDiv_incXSec(incXSecPIXELS*xppix);
        this.setDiv_incYPrim(incYPrimPIXELS*yppix);
        this.setDiv_incYSec(incYSecPIXELS*yppix);
        
        this.txtXdiv.setText(FileUtils.dfX_3.format(this.getDiv_incXPrim()));
        this.txtYdiv.setText(FileUtils.dfX_3.format(this.getDiv_incYPrim()));

//        this.txtXdiv.setText(String.valueOf(this.getDiv_incXPrim()));
//        this.txtYdiv.setText(String.valueOf(this.getDiv_incYPrim()));
        
//        if(isDebug())log.writeNameNumPairs("fine", true, "div_incXPrim, div_incXSec, div_incYPrim, div_incYSec",div_incXPrim, div_incXSec, div_incYPrim, div_incYSec);

    }
    
    //valor inicial, valor d'increment per les separacions principals (tindran número), n divisions secundaries entre principals
    //iniVal l'hem suprimit d'aqui, la "finestra" no es responsabilitat d'aquesta funcio
    private void customDivLinesX(double incrPrincipals, double nDivisionsSecund){
        
        double currentXIni = this.getXrangeMin();
        
//        this.setXrangeMin((int)this.getxMin());

//        this.setDiv_startValX(this.getXrangeMin());
        this.setDiv_startValX(currentXIni);
        this.setXrangeMin(currentXIni);
        
        this.setDiv_incXPrim(incrPrincipals);
        this.setDiv_incXSec(incrPrincipals/nDivisionsSecund);
        
        this.txtXdiv.setText(FileUtils.dfX_3.format(this.getDiv_incXPrim()));
//        this.txtXdiv.setText(String.valueOf(this.getDiv_incXPrim()));
        
   }
    
    private void customDivLinesY(double incrPrincipals, double nDivisionsSecund){
        double currentYIni = this.getYrangeMin();
        
//        this.setYrangeMin(0); //TODO REVISAR SI ES EL COMPORTAMENT QUE VOLEM
        
//        this.setDiv_startValY(this.getYrangeMin());
        this.setDiv_startValY(currentYIni);
        this.setYrangeMin(currentYIni);
        
        this.setDiv_incYPrim(incrPrincipals);
        this.setDiv_incYSec(incrPrincipals/nDivisionsSecund);
        
//        this.txtYdiv.setText(String.valueOf(this.getDiv_incYPrim()));
        this.txtYdiv.setText(FileUtils.dfX_3.format(this.getDiv_incYPrim()));
        
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
        Iterator<Pattern1D> itrp = getPatterns().iterator();
        double maxX = Double.MIN_VALUE;
        double minX = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        while (itrp.hasNext()){
            Pattern1D patt = itrp.next();
            for (int i=0; i<patt.getNseries(); i++){
                DataSerie s = patt.getSerie(i);
                if (!s.isPlotThis()) continue;
                double[] MxXMnXMxYMnY = s.getPuntsMaxXMinXMaxYMinY();
                
                if (MxXMnXMxYMnY[0]>maxX) maxX = MxXMnXMxYMnY[0];
                if (MxXMnXMxYMnY[1]<minX) minX = MxXMnXMxYMnY[1];
                if (MxXMnXMxYMnY[2]>maxY) maxY = MxXMnXMxYMnY[2];
                if (MxXMnXMxYMnY[3]<minY) minY = MxXMnXMxYMnY[3];
            }
        }
        this.setxMax(maxX);
        this.setxMin(minX);
        this.setyMax(maxY);
        this.setyMin(minY);
    }
    
    //height in pixels of the plot area
    private double calcPlotSpaceY(){
        return graphPanel.getHeight()-getGapAxisTop()-getGapAxisBottom()-2*padding;
    }
    //width in pixels of the plot area
    private double calcPlotSpaceX(){
        return graphPanel.getWidth()-getGapAxisLeft()-getGapAxisRight()-2*padding;
    }
    //escala en Y per encabir el rang que s'ha de plotejar
    private void calcScaleFitY(){
        scalefitY = calcPlotSpaceY()/(this.getYrangeMax()-this.getYrangeMin());
    }
    //escala en X per encabir el rang que s'ha de plotejar
    private void calcScaleFitX(){
        scalefitX = calcPlotSpaceX()/(this.getXrangeMax()-this.getXrangeMin());
    }
    
    // FARE ZOOM NOMES EN Y?
    private void zoomY(boolean zoomIn, Point2D.Double centre) {
        Point2D.Double dpcentre = this.getDataPointFromFramePoint(centre); // miro a quin punt de dades estem fent zoom
        if (dpcentre == null)return;
        if (zoomIn) {
            this.setYrangeMax(this.getYrangeMax()*(1/facZoom));
            // TODO: posem maxim?
        } else {
            this.setYrangeMax(this.getYrangeMax()*(facZoom));
        }
        calcScaleFitY();
        this.actualitzaPlot();
    }

    private void zoomX(boolean zoomIn, double inc) {
        if (zoomIn) {
            this.setXrangeMin(this.getXrangeMin()+(inc/scalefitX));
            this.setXrangeMax(this.getXrangeMax()-(inc/scalefitX));
            // TODO: posem maxim?
        } else {
            this.setXrangeMin(this.getXrangeMin()-(inc/scalefitX));
            this.setXrangeMax(this.getXrangeMax()+(inc/scalefitX));
        }
        calcScaleFitX();
     }
    
    private void scrollX(double inc) {
        this.setXrangeMin(this.getXrangeMin()+(inc/scalefitX));
        this.setXrangeMax(this.getXrangeMax()+(inc/scalefitX));
          // TODO: posem maxim?
      calcScaleFitX();
    }
    
    //es mouen en consonancia els limits de rang x i y
	private void movePattern(double incX, double incY){//, boolean repaint) {
	    this.setXrangeMin(this.getXrangeMin()-(incX/scalefitX));
	    this.setXrangeMax(this.getXrangeMax()-(incX/scalefitX));
	    this.setYrangeMin(this.getYrangeMin()+(incY/scalefitY));
	    this.setYrangeMax(this.getYrangeMax()+(incY/scalefitY));
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
        this.setXrangeMin(Double.parseDouble(txtXmin.getText()));
        this.setXrangeMax(Double.parseDouble(txtXmax.getText()));
        this.setYrangeMin(Double.parseDouble(txtYmin.getText()));
        this.setYrangeMax(Double.parseDouble(txtYmax.getText()));
        this.calcScaleFitX();
        this.calcScaleFitY();
        this.actualitzaPlot();
    }
    
    private void fillWindowValues(){
        this.txtXmax.setText(FileUtils.dfX_3.format(this.getXrangeMax()));
        this.txtXmin.setText(FileUtils.dfX_3.format(this.getXrangeMin()));
        this.txtYmax.setText(FileUtils.dfX_3.format(this.getYrangeMax()));
        this.txtYmin.setText(FileUtils.dfX_3.format(this.getYrangeMin()));
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
	        if (arePatterns()){
	            Point2D.Double dp = getDataPointFromFramePoint(new Point2D.Double(e.getPoint().x, e.getPoint().y));
	            if (dp!=null){
	                
	                //get the units from first pattern
	                DataSerie ds = this.getPatterns().get(0).getSerie(0);
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
	                        Xpref = "X(1/dsp)=";
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
	                if((wl>0)&&(ds.getxUnits()==DataSerie.xunits.tth)){
	                    //mirem si hi ha wavelength i les unitats del primer son tth
	                    double dsp = wl/(2*FastMath.sin(FastMath.toRadians(dtth/2.)));
	                    lblDsp.setText(String.format(" [dsp=%.4f"+D1Dplot_global.angstrom+"]", dsp));
	                }else{
	                    lblDsp.setText("");
	                }
	                
	                if (hkllabels){
	                    Iterator<Pattern1D> itrPt = this.getPatterns().iterator();
	                    while (itrPt.hasNext()){
	                        Pattern1D p = itrPt.next();
	                        if (!p.isPrf())continue;
	                        Iterator<DataSerie> ids = p.getSeriesIterator();
	                        while (ids.hasNext()){
	                            ds = ids.next();
	                            ArrayList<DataHKL> dhkl = new ArrayList<DataHKL>();
	                            if (ds.getTipusSerie()==DataSerie.serieType.hkl){
	                                double tol = FastMath.min(10*getXunitsPerPixel(), 0.025); //provem el minim entre 10 pixels o 0.025º 2th
	                                dhkl = ds.getClosestReflections(dtth,tol);
	                            }
	                            if (dhkl.size()>0){
	                                Iterator<DataHKL> itrhkl = dhkl.iterator();
	                                StringBuilder shkl = new StringBuilder();
	                                shkl.append("hkl(s)= ");
	                                while (itrhkl.hasNext()){
	                                    DataHKL hkl = itrhkl.next();
	                                    shkl.append(hkl.toString()).append(" ; ");
	                                }
	                                lblHkl.setText(shkl.substring(0, shkl.length()-2));
	                            }
	                        }
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
	        if (!arePatterns())return;
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
	            if(this.isSelectingBkgPoints()){
	                DataPoint dp = this.getDataPointDPFromFramePoint(this.dragPoint);
	                DataSerie bkgEstimPoints = this.getSelectedSeries().get(0).getPatt1D().getBkgEstimPSerie(); //AQUESTA LINIA ES NOVA!!
	                if (bkgEstimPoints==null)return;
	                bkgEstimPoints.addPoint(dp);
	            }else if(this.isDeletingBkgPoints()){
	                DataPoint dp = this.getDataPointDPFromFramePoint(this.dragPoint);
	                DataSerie bkgEstimPoints = this.getSelectedSeries().get(0).getPatt1D().getBkgEstimPSerie(); //AQUESTA LINIA ES NOVA!!
	                if (bkgEstimPoints==null)return;
	                DataPoint toDelete = bkgEstimPoints.getClosestDP(dp,-1,-1);
	                if (toDelete!=null){
	                    if(isDebug())log.writeNameNums("config", true, "toDelete X,Y",toDelete.getX(),toDelete.getY());
	                    logdebug("bkgEstimPoints N = "+bkgEstimPoints.getNpoints());
	                    bkgEstimPoints.removePoint(toDelete);
	                    logdebug("bkgEstimPoints N = "+bkgEstimPoints.getNpoints());
	                }else{
	                    logdebug("toDelete is null");
	                }
	                
	            }else if(this.isSelectingPeaks()){
	                if(isOneSerieSelected()){
	                    //agafar com a pic la 2theta clicada pero amb la intensitat del punt mes proper
	                    DataPoint dp = this.getDataPointDPFromFramePoint(this.dragPoint);
	                    this.getSelectedSeries().get(0).addPeak(dp.getX());
	                }
	            }else if(this.isDeletingPeaks()){
	                if(isOneSerieSelected()){
	                    DataPoint dp = this.getDataPointDPFromFramePoint(this.dragPoint);
	                    DataPoint toDelete = this.getSelectedSeries().get(0).getClosestPeak(dp,-1);
	                    if (toDelete!=null){
	                        if(isDebug())log.writeNameNums("config", true, "toDelete X,Y",toDelete.getX(),toDelete.getY());
	                        this.getSelectedSeries().get(0).removePeak(toDelete);
	                    }else{
	                        logdebug("toDelete is null");
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
	                this.setMouseBox(true);
	            }
	        }
	//        continuousRepaint=true;
	        this.actualitzaPlot();
	
	    }

	private void do_graphPanel_mouseReleased(MouseEvent e) {
	//        continuousRepaint=false;
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
	            this.setMouseBox(false);
	        }
	        if (!arePatterns())return;
	        
	        if (e.getButton() == CLICAR) {
	            logdebug("Mouse button="+Integer.toString(CLICAR));
	            //comprovem que no s'estigui fent una altra cosa          
	            if(this.isSelectingBkgPoints()||this.isDeletingBkgPoints())return;
	            if(this.isSelectingPeaks()||this.isDeletingPeaks())return;
	
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
	            this.setXrangeMin(xrmin);
	            this.setXrangeMax(xrmax);
	            this.calcScaleFitX();
	            
	            if (this.sqSelect){
	                double yrmin = FastMath.min(dataPointFinal.y, dataPointInicial.y);
	                double yrmax = FastMath.max(dataPointFinal.y, dataPointInicial.y);
	                if(isDebug())log.writeNameNums("config", true, "yrangeMin yrangeMax", yrmin,yrmax);
	                this.setYrangeMin(yrmin);
	                this.setYrangeMax(yrmax);
	                this.calcScaleFitY();
	              }
	            
	            this.actualitzaPlot();
	        }
	//        continuousRepaint=false;
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

	public static float getDef_axis_fsize() {
        return PlotPanel.def_axis_fsize;
    }

    public static void setDef_axis_fsize(float def_axis_fsize) {
        PlotPanel.def_axis_fsize = def_axis_fsize;
    }

    public static float getDef_axisL_fsize() {
        return PlotPanel.def_axisL_fsize;
    }

    public static void setDef_axisL_fsize(float def_axisL_fsize) {
        PlotPanel.def_axisL_fsize = def_axisL_fsize;
    }

    public static int getDefNdecimalsx() {
        return PlotPanel.def_nDecimalsX;
    }

    public static int getDefNdecimalsy() {
        return PlotPanel.def_nDecimalsY;
    }
    public static void setDefNdecimalsx(int ndec) {
        PlotPanel.def_nDecimalsX=ndec;
    }
    public static void setDefNdecimalsy(int ndec) {
        PlotPanel.def_nDecimalsY=ndec;
    }
    
public static int getGapAxisTop() {
	    return gapAxisTop;
	}

	public static void setGapAxisTop(int gapAxisTop) {
	    PlotPanel.gapAxisTop = gapAxisTop;
	}

	public static int getGapAxisBottom() {
	    return gapAxisBottom;
	}

	public static void setGapAxisBottom(int gapAxisBottom) {
	    PlotPanel.gapAxisBottom = gapAxisBottom;
	}

	public static int getGapAxisRight() {
	    return gapAxisRight;
	}

	public static void setGapAxisRight(int gapAxisRight) {
	    PlotPanel.gapAxisRight = gapAxisRight;
	}

	public static int getGapAxisLeft() {
	    return gapAxisLeft;
	}

	public static void setGapAxisLeft(int gapAxisLeft) {
	    PlotPanel.gapAxisLeft = gapAxisLeft;
	}

	public static boolean isLightTheme() {
	    return lightTheme;
	}

	public static void setLightTheme(boolean lightTheme) {
	    PlotPanel.lightTheme = lightTheme;
	}

	public static int getPadding() {
	    return padding;
	}

	public static void setPadding(int padding) {
	    PlotPanel.padding = padding;
	}

	public static int getAxisLabelsPadding() {
	    return AxisLabelsPadding;
	}

	public static void setAxisLabelsPadding(int axisLabelsPadding) {
	    AxisLabelsPadding = axisLabelsPadding;
	}

	public static double getIncXPrimPIXELS() {
	    return incXPrimPIXELS;
	}

	public static void setIncXPrimPIXELS(double incXPrimPIXELS) {
	    PlotPanel.incXPrimPIXELS = incXPrimPIXELS;
	}

	public static double getIncXSecPIXELS() {
	    return incXSecPIXELS;
	}

	public static void setIncXSecPIXELS(double incXSecPIXELS) {
	    PlotPanel.incXSecPIXELS = incXSecPIXELS;
	}

	public static double getIncYPrimPIXELS() {
	    return incYPrimPIXELS;
	}

	public static void setIncYPrimPIXELS(double incYPrimPIXELS) {
	    PlotPanel.incYPrimPIXELS = incYPrimPIXELS;
	}

	public static double getIncYSecPIXELS() {
	    return incYSecPIXELS;
	}

	public static void setIncYSecPIXELS(double incYSecPIXELS) {
	    PlotPanel.incYSecPIXELS = incYSecPIXELS;
	}

	public static int getMinZoomPixels() {
	    return minZoomPixels;
	}

	public static void setMinZoomPixels(int minZoomPixels) {
	    PlotPanel.minZoomPixels = minZoomPixels;
	}

	public static double getFacZoom() {
	    return facZoom;
	}

	public static void setFacZoom(double facZoom) {
	    PlotPanel.facZoom = facZoom;
	}

	public static int getMOURE() {
	    return MOURE;
	}

	public static void setMOURE(int mOURE) {
	    MOURE = mOURE;
	}

	public static int getCLICAR() {
	    return CLICAR;
	}

	public static void setCLICAR(int cLICAR) {
	    CLICAR = cLICAR;
	}

	public static int getZOOM_BORRAR() {
	    return ZOOM_BORRAR;
	}

	public static void setZOOM_BORRAR(int zOOM_BORRAR) {
	    ZOOM_BORRAR = zOOM_BORRAR;
	}

	public static boolean isVerticalYlabel() {
	    return verticalYlabel;
	}

	public static void setVerticalYlabel(boolean verticalYlabel) {
	    PlotPanel.verticalYlabel = verticalYlabel;
	}

	public static int getDiv_PrimPixSize() {
	    return div_PrimPixSize;
	}

	public static void setDiv_PrimPixSize(int div_PrimPixSize) {
	    PlotPanel.div_PrimPixSize = div_PrimPixSize;
	}

	public static int getDiv_SecPixSize() {
	    return div_SecPixSize;
	}

	public static void setDiv_SecPixSize(int div_SecPixSize) {
	    PlotPanel.div_SecPixSize = div_SecPixSize;
	}

	public static boolean isVerticalYAxe() {
	    return verticalYAxe;
	}

	public static void setVerticalYAxe(boolean verticalYAxe) {
	    PlotPanel.verticalYAxe = verticalYAxe;
	}

	public double getXrangeMin() {
	    return xrangeMin;
	}

	public void setXrangeMin(double xrangeMin) {
	    this.xrangeMin = xrangeMin;
	}

	public double getXrangeMax() {
	    return xrangeMax;
	}

	public void setXrangeMax(double xrangeMax) {
	    this.xrangeMax = xrangeMax;
	}

	public double getYrangeMin() {
	    return yrangeMin;
	}

	public void setYrangeMin(double yrangeMin) {
	    this.yrangeMin = yrangeMin;
	}

	public double getYrangeMax() {
	    return yrangeMax;
	}

	public void setYrangeMax(double yrangeMax) {
	    this.yrangeMax = yrangeMax;
	}

	public ArrayList<Pattern1D> getPatterns(){
	    return this.patterns;
	}

	public double getxMin() {
	    return xMin;
	}

	public void setxMin(double xMin) {
	    this.xMin = xMin;
	}

	public double getxMax() {
	    return xMax;
	}

	public void setxMax(double xMax) {
	    this.xMax = xMax;
	}

	public double getyMin() {
	    return yMin;
	}

	public void setyMin(double yMin) {
	    this.yMin = yMin;
	}

	public double getyMax() {
	    return yMax;
	}

	public void setyMax(double yMax) {
	    this.yMax = yMax;
	}

	public double getScalefitX() {
	    return scalefitX;
	}

	public void setScalefitX(double scalefitX) {
	    this.scalefitX = scalefitX;
	}

	public double getScalefitY() {
	    return scalefitY;
	}

	public void setScalefitY(double scalefitY) {
	    this.scalefitY = scalefitY;
	}

	public boolean isMouseBox() {
	    return mouseBox;
	}

	public void setMouseBox(boolean mouseBox) {
	    this.mouseBox = mouseBox;
	}

	public double getIncX() {
	    return incX;
	}

	public void setIncX(double incX) {
	    this.incX = incX;
	}

	public double getIncY() {
	    return incY;
	}

	public void setIncY(double incY) {
	    this.incY = incY;
	}

	public double getDiv_incXPrim() {
	    return div_incXPrim;
	}

	public void setDiv_incXPrim(double div_incXPrim) {
	    this.div_incXPrim = div_incXPrim;
	}

	public double getDiv_incXSec() {
	    return div_incXSec;
	}

	public void setDiv_incXSec(double div_incXSec) {
	    this.div_incXSec = div_incXSec;
	}

	public double getDiv_incYPrim() {
	    return div_incYPrim;
	}

	public void setDiv_incYPrim(double div_incYPrim) {
	    this.div_incYPrim = div_incYPrim;
	}

	public double getDiv_incYSec() {
	    return div_incYSec;
	}

	public void setDiv_incYSec(double div_incYSec) {
	    this.div_incYSec = div_incYSec;
	}

	public double getDiv_startValX() {
	    return div_startValX;
	}

	public void setDiv_startValX(double div_startValX) {
	    this.div_startValX = div_startValX;
	}

	public double getDiv_startValY() {
	    return div_startValY;
	}

	public void setDiv_startValY(double div_startValY) {
	    this.div_startValY = div_startValY;
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
	}

	public boolean isAutoPosLegend() {
	    return autoPosLegend;
	}

	public void setAutoPosLegend(boolean autoPosLegend) {
	    this.autoPosLegend = autoPosLegend;
	}

	public int getLegendX() {
	    return legendX;
	}

	public void setLegendX(int legendX) {
	    this.legendX = legendX;
	}

	public int getLegendY() {
	    return legendY;
	}

	public void setLegendY(int legendY) {
	    this.legendY = legendY;
	}

	public boolean isHkllabels() {
	    return hkllabels;
	}

	public void setHkllabels(boolean hkllabels) {
	    this.hkllabels = hkllabels;
	}

	public boolean isShowGridY() {
	    return showGridY;
	}

	public void setShowGridY(boolean showGrid) {
	    this.showGridY = showGrid;
	}

	public boolean isShowGridX() {
	    return showGridX;
	}

	public void setShowGridX(boolean showGrid) {
	    this.showGridX = showGrid;
	}

	public Plot1d getGraphPanel() {
	    return graphPanel;
	}

	public void setGraphPanel(Plot1d graphPanel) {
	    this.graphPanel = graphPanel;
	}

	public boolean isNegativeYAxisLabels() {
	    return negativeYAxisLabels;
	}

	public void setNegativeYAxisLabels(boolean negativeYAxisLabels) {
	    this.negativeYAxisLabels = negativeYAxisLabels;
	}

	public boolean isShowPeaks() {
	    return showPeaks;
	}

	public void setShowPeaks(boolean showPeaks) {
	    this.showPeaks = showPeaks;
	}

	public ArrayList<DataSerie> getSelectedSeries() {
	    return selectedSeries;
	}

	public void setSelectedSerie(ArrayList<DataSerie> selectedSeries) {
	    this.selectedSeries = selectedSeries;
	}

	public boolean isShowBackground() {
	    return showBackground;
	}

	public void setShowBackground(boolean showBackground) {
	    this.showBackground = showBackground;
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
            this.setDecimalsXaxis(PlotPanel.def_nDecimalsX);
            this.setDecimalsYaxis(PlotPanel.def_nDecimalsY);
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
            
            if (getPatterns().size() > 0) {

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
                
                if (getScalefitY()<0){
                    calcScaleFitY();    
                }
                if (getScalefitX()<0){
                    calcScaleFitX();    
                }

                //1st draw axes (and optionally grid)
                this.drawAxes(g2,showGridY,showGridX);

                Iterator<Pattern1D> itrp = getPatterns().iterator();
//                int npatt = getPatterns().size();
                int ipatt = 0;
                while (itrp.hasNext()){
//                    log.fine(String.format("Drawing patt %d of %d", ipatt,npatt));
                    Pattern1D patt = itrp.next();
                    for (int i=0; i<patt.getNseries(); i++){
                        DataSerie ds = patt.getSerie(i);
                        if (!ds.isPlotThis())continue;
                        
                        switch (ds.getTipusSerie()){
                            case hkl:
                                drawHKL(g2,ds,ds.getColor());
                                break;
                            case ref:
                                drawREF(g2,ds,ds.getColor());
                                break;
                            default: //dibuix linea normal, (dat, dif, gr, ...)
                                if(ds.getLineWidth()>0)drawPatternLine(g2,ds,ds.getColor()); 
                                if(ds.getMarkerSize()>0)drawPatternPoints(g2,ds,ds.getColor());
                                break;
                        }
                        
                        if (patt.getSerie(i).isShowErrBars()){
                            drawErrorBars(g2,patt.getSerie(i),patt.getSerie(i).getColor());
                        }
                    }
                    ipatt=ipatt+1;
                }

//                logdebug(Float.toString(PlotPanel.getDef_axis_fsize()));
//                logdebug(Float.toString(PlotPanel.getDef_axis_fsize()));
//                logdebug(Boolean.toString(isSaveSVG()));
                
                if(showLegend){
                    drawLegend(g2);
                }

                if (showPeaks){
                    drawPeaks(g2);
                    if (bkgseriePeakSearch.getNpoints()>0){
                        drawPatternLine(g2,bkgseriePeakSearch,bkgseriePeakSearch.getColor());
                    }
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
            }
        }

        private void drawAxes(Graphics2D g1, boolean gridY, boolean gridX){
//            logdebug("drawAxes entered");

            //provem de fer linia a 60 pixels de l'esquerra i a 60 pixels de baix (40 i 40 de dalt i a la dreta com a marges)

            double coordXeixY = getGapAxisLeft()+padding;
            double coordYeixX = panelH-getGapAxisBottom()-padding;

            Point2D.Double vytop = new Point2D.Double(coordXeixY,getGapAxisTop()+padding);
            Point2D.Double vybot = new Point2D.Double(coordXeixY,coordYeixX);
            Point2D.Double vxleft = vybot;
            Point2D.Double vxright = new Point2D.Double(panelW-getGapAxisRight()-padding,coordYeixX);

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
            double xval = getDiv_startValX();
            while (xval <= getXrangeMax()){
                if (xval < getXrangeMin()){
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

                if(xval> (int)(1+getxMax()))break; //provem de posar-ho aqui perque no dibuixi mes enllà
            }

            //ara les secundaries
            double yiniSec = coordYeixX- (div_SecPixSize/2.f); 
            double yfinSec = coordYeixX + (div_SecPixSize/2.f);
            xval = getDiv_startValX();
            while (xval <= getXrangeMax()){
                if (xval < getXrangeMin()){
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

                if(xval> (int)(1+getxMax()))break; //provem de posar-ho aqui perque no dibuixi mes enllà
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
                double yval = getDiv_startValY();
                while (yval <= getYrangeMax()){
                    if (yval < getYrangeMin()){
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
                    double limit = getGapAxisLeft();
                    if (verticalYlabel)limit = getGapAxisLeft()-ylabelheight;
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
                yval = getDiv_startValY();
                while (yval <= getYrangeMax()){
                    if (yval < getYrangeMin()){
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

        private void drawPatternLine(Graphics2D g1, DataSerie serie, Color col){
//            log.debug("drawPatternLine entered");
            g1.setColor(col);
            BasicStroke stroke = new BasicStroke(serie.getLineWidth());
            g1.setStroke(stroke);
            if(serie.getLineWidth()<=0)return;
            for (int i = 0; i < serie.getNpoints(); i++){
                //PRIMER DIBUIXEM TOTA LA LINIA --> ATENCIO AMB ELS PUNTS QUE ESTAN FORA!!

                //despres del canvi a private l'arraylist
                Point2D.Double p1 = getFramePointFromDataPoint(serie.getPoint(i));
                if (i==(serie.getNpoints()-1)){ //p1 es l'ultim punt, ja podem sortir del for
                    break;
                }
                Point2D.Double p2 = getFramePointFromDataPoint(serie.getPoint(i+1));

                //ara:
                // si els 2 son fora de l'area de dibuix passem als seguents
                // si un dels 2 es fora de l'area de dibuix cal mirar per quin costat i agafar la interseccio com a punt
                // si els 2 son dins doncs es fa la linia normal

                boolean isP1 = isFramePointInsideGraphArea(p1);
                boolean isP2 = isFramePointInsideGraphArea(p2);
//                boolean trobat = false;

//                if (!isP1 ||!isP2)continue;
                
                if (!isP1){
                    if (!isP2){
                        continue;
                    }else{
                        //P1 esta fora, cal redefinirlo amb la interseccio amb l'eix pertinent
                        Point2D.Double[] p = getIntersectionPoint(new Line2D.Double(p1,p2),getRectangleGraphArea());
                        for (int j=0;j<p.length;j++){
                            if (p[j]!=null){
                                p1 = p[j];
//                                trobat = true;
                            }
                        }
                    }
//                    if (trobat) {
//                        log.fine("P1 redefinit");
//                    }else{
//                        log.fine("P1 NO redefinit");
//                    }
                }

                if (!isP2){
                    if (!isP1){
                        continue;
                    }else{
                        //P2 esta fora, cal redefinirlo amb la interseccio amb l'eix pertinent
                        Point2D.Double[] p = getIntersectionPoint(new Line2D.Double(p1,p2),getRectangleGraphArea());
                        for (int j=0;j<p.length;j++){
                            if (p[j]!=null){
                                p2 = p[j];
//                                trobat = true;
                            }
                        }
                    }
//                    if (trobat){
//                        log.fine("P2 redefinit");
//                    }else{
//                        log.fine("P2 NO redefinit");
//                    }
                }                

                //ARA JA PODEM DIBUIXAR LA LINIA
                Line2D.Double l = new Line2D.Double(p1.x,p1.y,p2.x,p2.y);
                g1.draw(l);

            }
//            log.debug("drawPatternLine exit");
        }

        //separo linia i punts per si volem canviar l'ordre de dibuix
        private void drawPatternPoints(Graphics2D g1, DataSerie serie, Color col){
//            log.debug("drawPatternPoints entered");
            for (int i = 0; i < serie.getNpoints(); i++){
                g1.setColor(col);
                BasicStroke stroke = new BasicStroke(0.0f);
                g1.setStroke(stroke);

                //despres del canvi a private de seriePoints
                Point2D.Double p1 = getFramePointFromDataPoint(serie.getPoint(i));

                if (isFramePointInsideGraphArea(p1)){
                    double radiPunt = serie.getMarkerSize()/2.f;
                    g1.fillOval((int)FastMath.round(p1.x-radiPunt), (int)FastMath.round(p1.y-radiPunt), FastMath.round(serie.getMarkerSize()), FastMath.round(serie.getMarkerSize()));
                    g1.drawOval((int)FastMath.round(p1.x-radiPunt), (int)FastMath.round(p1.y-radiPunt), FastMath.round(serie.getMarkerSize()), FastMath.round(serie.getMarkerSize()));
                }
            }
//            log.debug("drawPatternPoints exit");
        }


        //separo linia i punts per si volem canviar l'ordre de dibuix
        private void drawErrorBars(Graphics2D g1, DataSerie serie, Color col){
//            log.debug("drawErrorBars entered");
            for (int i = 0; i < serie.getNpoints(); i++){
                g1.setColor(col);
                BasicStroke stroke = new BasicStroke(1.0f);
                g1.setStroke(stroke);

                //despres del canvi a private de seriePoints
                double tth = serie.getPoint(i).getX();
                double counts = serie.getPoint(i).getY();
                double err = serie.getPoint(i).getSdy();

                if (err<=0.0f)continue;

                Point2D.Double p1 = getFramePointFromDataPoint(serie.getPoint(i));

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
                    continue;
                }

                //ara dibuixem les 3 linies
                g1.draw(new Line2D.Double(ptop.x,ptop.y,pbot.x,pbot.y));
                g1.draw(new Line2D.Double(ptopl.x,ptopl.y,ptopr.x,ptopr.y));
                g1.draw(new Line2D.Double(pbotl.x,pbotl.y,pbotr.x,pbotr.y));

            }
//            log.debug("drawErrorBars exit");
        }

        private void drawHKL(Graphics2D g1, DataSerie serie, Color col){
//            log.fine("drawHKL entered");
            for (int i = 0; i < serie.getNpoints(); i++){
                g1.setColor(col);
                BasicStroke stroke = new BasicStroke(serie.getLineWidth());
                g1.setStroke(stroke);

                //despres del canvi a private de seriePoints
                double tth = serie.getHKLPoint(i).getTth();

                //la X es la 2THETA pero la Y hauria de ser el punt de menor intensitat de OBS més un hkloffset (en pixels, definit a patt1d)
                double fx = getFrameXFromDataPointX(tth);
                double fy = getFrameYFromDataPointY(0.0+serie.getYOff());
                fy = fy - Pattern1D.getHkloff() +Pattern1D.getHklticksize()/2f;  //pensem que la Y es cap avall!

                Point2D.Double ptop = new Point2D.Double(fx, fy-Pattern1D.getHklticksize()/2);
                Point2D.Double pbot = new Point2D.Double(fx, fy+Pattern1D.getHklticksize()/2);

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
                g1.setColor(col);
                BasicStroke stroke = new BasicStroke(serie.getLineWidth());
                switch (FastMath.round(serie.getMarkerSize())) {
                    case 1:
                        stroke = new BasicStroke(serie.getLineWidth(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[]{1,2}, 0);
                        break;
                    case 2:
                        stroke = new BasicStroke(serie.getLineWidth(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[]{1,4}, 0);
                        break;
                    case 3:
                        stroke = new BasicStroke(serie.getLineWidth(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[]{2,4}, 0);
                        break;
                    default:
                        stroke = new BasicStroke(serie.getLineWidth());
                        break;
                    
                }
//                BasicStroke stroke = new BasicStroke(serie.getLineWidth());
//                g1.setStroke(stroke);
//                BasicStroke dashed = new BasicStroke(serie.getLineWidth(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[]{1,2}, 0);
                g1.setStroke(stroke);
                

                //despres del canvi a private de seriePoints
                double tth = serie.getPoint(i).getX();

                //la X es la 2THETA i s'ha de fer una linea de dalt a baix
                double fx = getFrameXFromDataPointX(tth);
                Point2D.Double ptop = new Point2D.Double(fx, getGapAxisTop()+padding);
                Point2D.Double pbot = new Point2D.Double(fx, panelH-getGapAxisBottom()-padding);

                //comprovem que tot estigui dins
//                if (!isFramePointInsideGraphArea(ptop) || !isFramePointInsideGraphArea(pbot)){
//                    continue;
//                }

                //ara dibuixem la linia
                g1.draw(new Line2D.Double(ptop.x,ptop.y,pbot.x,pbot.y));

            }
//            log.debug("drawREF exit");
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

            Iterator<Pattern1D> itrp = getPatterns().iterator();

            try {
                int entries = 0;
                while (itrp.hasNext()){
                    Pattern1D patt = itrp.next();
                    for (int i=0; i<patt.getNseries(); i++){
                        if (!patt.getSerie(i).isPlotThis())continue;

                        //dibuixem primer la linia
                        int l_iniX = legendX+margin;
                        int l_finX = legendX+margin+linelength;
                        int l_y = (int) (legendY+margin+entries*(entryHeight)+FastMath.round(entryHeight/2.));

                        g1.setColor(patt.getSerie(i).getColor());
                        BasicStroke stroke = new BasicStroke(strokewidth);
                        g1.setStroke(stroke);

                        Line2D.Float l = new Line2D.Float(l_iniX,l_y,l_finX,l_y);
                        g1.draw(l);

                        //ara el text
                        int t_X = l_finX+margin; //TODO: revisar si queda millor x2
                        int maxlength = panelW-padding-margin-t_X;
                        String s =  patt.getSerie(i).getSerieName(); //TODO: POSAR CORRECTAMENT EL NOM
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

                        entries = entries +1;
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
                itrp = getPatterns().iterator();
                entries = 0;
                while (itrp.hasNext()){
                    Pattern1D patt = itrp.next();
                    for (int i=0; i<patt.getNseries(); i++){
                        if (!patt.getSerie(i).isPlotThis())continue;

                        stroke = new BasicStroke(strokewidth);
                        g1.setStroke(stroke);
                        g1.setColor(patt.getSerie(i).getColor());

                        //dibuixem primer la linia (si s'escau)
                        int l_iniX = legendX+margin;
                        int l_finX = legendX+margin+linelength;
                        int l_y = (int) (legendY+margin+entries*(entryHeight)+FastMath.round(entryHeight/2.));
                        if (patt.getSerie(i).getLineWidth()>0){

                            if (patt.getSerie(i).getTipusSerie()==DataSerie.serieType.hkl){
                                int gap = (int) ((entryHeight - Pattern1D.getHklticksize())/2.);
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
                        if (patt.getSerie(i).getMarkerSize()>0){
                            int sep = (int) (FastMath.abs(l_iniX-l_finX)/5.f);
                            int x1 = l_iniX+sep;
                            int x2 = l_iniX+sep*4;


                            stroke = new BasicStroke(0.0f);
                            g1.setStroke(stroke);
                            double radiPunt = patt.getSerie(i).getMarkerSize()/2.f;
                            g1.fillOval((int)FastMath.round(x1-radiPunt), (int)FastMath.round(l_y-radiPunt), FastMath.round(patt.getSerie(i).getMarkerSize()), FastMath.round(patt.getSerie(i).getMarkerSize()));
                            g1.fillOval((int)FastMath.round(x2-radiPunt), (int)FastMath.round(l_y-radiPunt), FastMath.round(patt.getSerie(i).getMarkerSize()), FastMath.round(patt.getSerie(i).getMarkerSize()));
                        }
                        //recuperem stroke width per si de cas hi havia markers
                        stroke = new BasicStroke(strokewidth);
                        g1.setStroke(stroke);

                        //ara el text
                        int t_X = l_finX+margin; //TODO: revisar si queda millor x2
                        int maxlength = panelW-padding-margin-t_X;
                        String s =  patt.getSerie(i).getSerieName(); //TODO: POSAR CORRECTAMENT EL NOM
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

                        entries = entries +1;
                    }
                }
            } catch (Exception e) {
                if(isDebug())e.printStackTrace();
                logdebug("error writting legend");
                legendX = legendX - 10;
                repaint();
            }
        }

        private void drawPeaks(Graphics2D g1){
            //only selected series
            int gapPixels = 5; //gap between top of peak and line
            int sizePix = 20;

            Iterator<DataSerie> itrds = getSelectedSeries().iterator();
            while(itrds.hasNext()){
                DataSerie ds = itrds.next();
                if (ds.getNpeaks()==0){
//                    logdebug("no peaks on serie "+ds.getPatt1D().indexOfSerie(ds)+" (patt "+getPatterns().indexOf(ds.getPatt1D())+")");
                    return;
                }
                for (int i=0; i<ds.getNpeaks();i++){
                    DataPoint dp = ds.getPeak(i);
                    Point2D.Double ptop = getFramePointFromDataPoint(dp);
                    //ara fem una linia amunt recta
                    ptop.y=ptop.y-gapPixels;

                    //draw LIne
                    BasicStroke stroke = new BasicStroke(2.0f);
                    g1.setStroke(stroke);
                    g1.setColor(ds.getColor().darker());
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

        
        //TODO: TEST PER FER UN ESCALAT REAL
        private void paintPNG(Graphics g, int w, int h) {
//          if (g2 == null) {
//              super.paintComponent(g);
//          }else {
//              super.paintComponent(g2);    
//          }
          
          super.paintComponent(g);
          log.debug("paintComponent PlotPanel");
          
//          n=n+1;
//          if (n>6) {
//              log.debug("n>6 exiting");
//              return;
//          }
          
          
//          final Graphics2D g2 = (Graphics2D) g;


          if (!this.saveTransp){
              if (lightTheme){
                  this.setBackground(Light_bkg);
              }else{
                  this.setBackground(Dark_bkg);
              }
          }
          
//          final Graphics2D g1 = (Graphics2D) g2.create();

          if (getPatterns().size() > 0) {

              panelW = this.getWidth();
              panelH = this.getHeight();
              
              BufferedImage off_Image = null;
//              int fsz = this.getFont().getSize();
//              int diff = svgFontSize-fsz;
              if (isSaveSVG()) {
//                  PlotPanel.setDef_axis_fsize(PlotPanel.getDef_axis_fsize()+diff);
//                  PlotPanel.setDef_axisL_fsize(PlotPanel.getDef_axisL_fsize()+diff);
                  g2 = (Graphics2D) g;
                  g2.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                          RenderingHints.VALUE_ANTIALIAS_ON)); // perque es vegin mes suaus...
                }else {
                    off_Image =
                            new BufferedImage(w, h,
                                              BufferedImage.TYPE_INT_ARGB);
                    g2 = off_Image.createGraphics();
                    
                    g2.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                      RenderingHints.VALUE_ANTIALIAS_ON)); // perque es vegin mes suaus...
                }
              

              
              //primer caculem els limits -- ho trec, no crec que faci falta...
//              calcMaxMinXY();
              if (getScalefitY()<0){
                  calcScaleFitY();    
              }
              if (getScalefitX()<0){
                  calcScaleFitX();    
              }


//              if (mouseBox == true && zoomRect != null) {
//                  //dibuixem el rectangle
//                  g2.setColor(Color.darkGray);
//                  BasicStroke stroke = new BasicStroke(3f);
//                  g2.setStroke(stroke);
//                  g2.draw(zoomRect);
//                  Color gristransp = new Color(Color.LIGHT_GRAY.getRed(), Color.LIGHT_GRAY.getGreen(),Color.LIGHT_GRAY.getBlue(), 128 );
//                  g2.setColor(gristransp);
//                  g2.fill(zoomRect);
//                  return; //no cal seguir pintant...
//              }
//              if (continuousRepaint)this.repaint();


              //1st draw axes (and optionally grid)
              this.drawAxes(g2,showGridY,showGridX);

              Iterator<Pattern1D> itrp = getPatterns().iterator();
              int npatt = getPatterns().size();
              int ipatt = 0;
              while (itrp.hasNext()){
                  log.debug(String.format("Patt %d of %d", ipatt,npatt));
                  Pattern1D patt = itrp.next();
                  for (int i=0; i<patt.getNseries(); i++){
                      DataSerie ds = patt.getSerie(i);
                      if (!ds.isPlotThis())continue;
                      
                      switch (ds.getTipusSerie()){
                          case hkl:
                              drawHKL(g2,ds,ds.getColor());
                              break;
                          case ref:
                              drawREF(g2,ds,ds.getColor());
                              break;
                          default: //dibuix linea normal, (dat, dif, gr, ...)
                              if(ds.getLineWidth()>0)drawPatternLine(g2,ds,ds.getColor()); 
                              if(ds.getMarkerSize()>0)drawPatternPoints(g2,ds,ds.getColor());
                              break;
                      }
                      
                      
//                      if(ds.getTipusSerie()==DataSerie.serieType.hkl){
//                          drawHKL(g2,ds,ds.getColor());
//                      }else{
//                          if(ds.getLineWidth()>0)drawPatternLine(g2,ds,ds.getColor()); 
//                          if(ds.getMarkerSize()>0)drawPatternPoints(g2,ds,ds.getColor());
//                      }
                      
                      if (patt.getSerie(i).isShowErrBars()){
                          drawErrorBars(g2,patt.getSerie(i),patt.getSerie(i).getColor());
                      }
                  }
                  ipatt=ipatt+1;
              }

              log.debug(Float.toString(PlotPanel.getDef_axis_fsize()));
              log.debug(Float.toString(PlotPanel.getDef_axis_fsize()));
              log.debug(Boolean.toString(isSaveSVG()));
              
              if(showLegend){
                  drawLegend(g2);
              }

              if (showPeaks){
                  drawPeaks(g2);
                  if (bkgseriePeakSearch.getNpoints()>0){
                      drawPatternLine(g2,bkgseriePeakSearch,bkgseriePeakSearch.getColor());
                  }
              }

//              if (isShowBackground()){
//                  logdebug("showbackground");
//                  if (bkgserie.getNpoints()!=0){
//                      drawPatternLine(g2,bkgserie,bkgserie.getColor()); 
//                      drawPatternPoints(g2,bkgserie,bkgserie.getColor());
//                  }
//                  if (bkgEstimPoints.getNpoints()!=0){
//                      drawPatternPoints(g2,bkgEstimPoints,bkgEstimPoints.getColor());
//                  }
//              }

//              g1.dispose();
//              g2.dispose();

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
//                    PlotPanel.setDef_axis_fsize(PlotPanel.getDef_axis_fsize()-diff);
//                    PlotPanel.setDef_axisL_fsize(PlotPanel.getDef_axisL_fsize()-diff);
                }
//              g2.dispose();
//              if(continuousRepaint)this.repaint();
          }
      }
        
        
    }
    
    
    
 
    
    
    
    
    
}
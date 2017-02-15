package vava33.d1dplot;

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
import java.awt.RenderingHints;
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
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JPanel;

//import org.apache.commons.math3.util.FastMath;


import com.vava33.jutils.FastMath;
import com.vava33.jutils.VavaLogger;

import vava33.d1dplot.auxi.DataHKL;
import vava33.d1dplot.auxi.DataPoint;
import vava33.d1dplot.auxi.DataSerie;
import vava33.d1dplot.auxi.Pattern1D;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

public class PlotPanel extends JPanel {

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

    private static VavaLogger log = D1Dplot_global.getVavaLogger(PlotPanel.class.getName());

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
    private String xlabel = "2"+D1Dplot_global.theta+" (º)";
    private String ylabel = "Intensity";
    private Plot1d graphPanel;
    //parametres interaccio/contrast
    private boolean mouseBox = false;
    private boolean mouseDrag = false;
    private boolean mouseMove = false;
    private boolean mouseZoom = false;
    private Rectangle2D.Double zoomRect;
    private Point2D.Double dragPoint;
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
    //paint triggers
    private boolean continuousRepaint = false;
    private boolean hkllabels = true;
    private boolean showGrid = false;
    
    boolean showPeaks = true;
    boolean showBackground = false;
    DataSerie bkgserie;
    public ArrayList<DataSerie> selectedSeries;
    
    private JTextField txtXdiv;
    private JTextField txtYdiv;
    private JCheckBox chckbxFixedAxis;
    private JTextField txtXmin;
    private JTextField txtXmax;
    private JTextField txtYmin;
    private JTextField txtYmax;
    private JButton btnApply;
    private JButton btnApplydiv;
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
    private JSeparator separator;
    private JLabel lblDsp;
    private JLabel lblHkl;

    /**
     * Create the panel.
     */
    public PlotPanel() {
        setBackground(Color.WHITE);
        setLayout(new MigLayout("insets 0", "[grow]", "[][grow][]"));
        
        JPanel buttons_panel = new JPanel();
        buttons_panel.setBorder(null);
        add(buttons_panel, "cell 0 0,grow");
        buttons_panel.setLayout(new MigLayout("", "[][grow][][grow][][grow][][][][][]", "[][]"));
        
        lblXdiv = new JLabel("Xdiv=");
        buttons_panel.add(lblXdiv, "cell 0 0,alignx right");
        
        txtXdiv = new JTextField();
        txtXdiv.setText("xdiv");
        buttons_panel.add(txtXdiv, "cell 1 0,growx");
        txtXdiv.setColumns(10);
        
        lblNdivx = new JLabel("NdivX=");
        buttons_panel.add(lblNdivx, "cell 2 0,alignx right");
        
        txtNdivx = new JTextField();
        txtNdivx.setText("NdivX");
        buttons_panel.add(txtNdivx, "cell 3 0,growx");
        txtNdivx.setColumns(10);
        
        lblYdiv = new JLabel("Ydiv=");
        buttons_panel.add(lblYdiv, "cell 4 0,alignx right");
        
        txtYdiv = new JTextField();
        txtYdiv.setText("ydiv");
        buttons_panel.add(txtYdiv, "cell 5 0,growx");
        txtYdiv.setColumns(10);
        
        lblNdivy = new JLabel("NdivY=");
        buttons_panel.add(lblNdivy, "cell 6 0,alignx right");
        
        txtNdivy = new JTextField();
        txtNdivy.setText("NdivY");
        buttons_panel.add(txtNdivy, "cell 7 0,growx");
        txtNdivy.setColumns(10);
        
        btnApplydiv = new JButton("ApplyDiv");
        btnApplydiv.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_btnApplydiv_actionPerformed(e);
            }
        });
        buttons_panel.add(btnApplydiv, "cell 8 0,growx");
        
        chckbxFixedAxis = new JCheckBox("Fix Axes");
        chckbxFixedAxis.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                do_chckbxFixedAxis_itemStateChanged(e);
            }
        });
        
        separator = new JSeparator();
        separator.setOrientation(SwingConstants.VERTICAL);
        buttons_panel.add(separator, "cell 9 0 1 2,alignx center,growy");
        buttons_panel.add(chckbxFixedAxis, "cell 10 0,growx");
        
        lblWindow = new JLabel("Xmin=");
        lblWindow.setToolTipText("");
        buttons_panel.add(lblWindow, "cell 0 1,alignx right");
        
        txtXmin = new JTextField();
        txtXmin.setText("Xmin");
        buttons_panel.add(txtXmin, "cell 1 1,growx");
        txtXmin.setColumns(10);
        
        lblXmax = new JLabel("Xmax=");
        buttons_panel.add(lblXmax, "cell 2 1,alignx right");
        
        txtXmax = new JTextField();
        txtXmax.setText("xmax");
        buttons_panel.add(txtXmax, "cell 3 1,growx");
        txtXmax.setColumns(10);
        
        lblYmin = new JLabel("Ymin=");
        buttons_panel.add(lblYmin, "cell 4 1,alignx right");
        
        txtYmin = new JTextField();
        txtYmin.setText("ymin");
        buttons_panel.add(txtYmin, "cell 5 1,growx");
        txtYmin.setColumns(10);
        
        lblYmax = new JLabel("Ymax=");
        buttons_panel.add(lblYmax, "cell 6 1,alignx right");
        
        txtYmax = new JTextField();
        txtYmax.setText("ymax");
        buttons_panel.add(txtYmax, "cell 7 1,growx");
        txtYmax.setColumns(10);
        
        btnApply = new JButton("ApplyWin");
        btnApply.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                do_btnApply_actionPerformed(arg0);
            }
        });
        buttons_panel.add(btnApply, "cell 8 1");
        
        btnResetView = new JButton("Reset View");
        btnResetView.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_btnResetView_actionPerformed(e);
            }
        });
        buttons_panel.add(btnResetView, "cell 10 1,growx");
        
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
        
        add(graphPanel, "cell 0 1,grow");
        
        statusPanel = new JPanel();
        add(statusPanel, "cell 0 2,grow");
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
        this.bkgserie = new DataSerie();
        div_incXPrim = 0;
        div_incXSec = 0;
        div_incYPrim = 0;
        div_incYSec = 0;
        div_startValX = 0;
        div_startValY = 0;

    }
    
    protected void do_graphPanel_mouseDragged(MouseEvent e) {
        log.fine("mouseDragged!!");
        log.fine(Boolean.toString(this.mouseDrag));
        log.fine(Boolean.toString(e.getButton() == MOURE));

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
                log.debug("incY"+incY+" zoomIn"+Boolean.toString(direction));
                this.scrollX(-incX);
            }else{
                //fem unzoom
                boolean zoomIn = (incY < 0);
                log.debug("incY"+incY+" zoomIn"+Boolean.toString(zoomIn));
                this.zoomX(zoomIn, FastMath.abs(incY));
               
            }
            
        }
        
        if (this.mouseDrag == true && this.mouseBox == true){
            Rectangle2D.Double rarea = getRectangleGraphArea();
            double rwidth = FastMath.abs(dragPoint.x-currentPoint.x);
            if (rwidth<minZoomPixels)return;
            double rheight = rarea.height;
            double yrect = rarea.y;
            //defecte drag cap a la dreta
            double xrect = dragPoint.x;
            if (dragPoint.x > currentPoint.x){
                //estem fent el drag cap a la esquerra, corregim vertex
                xrect = currentPoint.x;
            }
            zoomRect = new Rectangle2D.Double(xrect,yrect,rwidth,rheight);
        }
        
    }

    //es mouen en consonancia els limits de rang x i y
    public void movePattern(double incX, double incY){//, boolean repaint) {
        //TODO VIGILAR LIMITS
        this.setXrangeMin(this.getXrangeMin()-(incX/scalefitX));
        this.setXrangeMax(this.getXrangeMax()-(incX/scalefitX));
        this.setYrangeMin(this.getYrangeMin()+(incY/scalefitY));
        this.setYrangeMax(this.getYrangeMax()+(incY/scalefitY));
        this.calcScaleFitX();
        this.calcScaleFitY();
        
        log.writeNameNums("fine", true, "ranges x y min max", getXrangeMin(),getXrangeMax(),getYrangeMin(),getYrangeMax());
    }
    

    protected void do_graphPanel_mouseMoved(MouseEvent e) {
        if (arePatterns()){
            Point2D.Double dp = getDataPointFromFramePoint(new Point2D.Double(e.getPoint().x, e.getPoint().y));
            if (dp!=null){
                
                //get the units from first pattern
                DataSerie ds = this.getPatterns().get(0).getSeries().get(0);
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
                }
                
                
                double dtth = dp.getX();
//                lblTthInten.setText(String.format(" X=%.4f Y=%.1f", dtth,dp.getY()));
                lblTthInten.setText(String.format(" %s%.4f%s %s%.1f%s", Xpref,dtth,Xunit,Ypref,dp.getY(),Yunit));
//                String tth = String.format("X=%.4f", dtth);
//                String inten = String.format("Y=%.1f", dp.getY());
                double wl = ds.getWavelength();
                if((wl>0)&&(ds.getxUnits()==DataSerie.xunits.tth)){
                    //mirem si hi ha wavelength i les unitats del primer son tth
                    double dsp = wl/(2*FastMath.sin(FastMath.toRadians(dtth/2.)));
                    lblDsp.setText(String.format(" [dsp=%.4f"+D1Dplot_global.angstrom+"]", dsp));
                }else{
                    lblDsp.setText("");
                }
//                lblTthInten.setText(tth+"  "+inten);
                
//                if (D1Dplot_global.isdebug){
//                    lblTthInten.setText(lblTthInten.getText()+" "+String.format("%d %d", e.getPoint().x,e.getPoint().y));
//                }
                
                if (hkllabels){
                    Iterator<Pattern1D> itrPt = this.getPatterns().iterator();
                    while (itrPt.hasNext()){
                        Pattern1D p = itrPt.next();
                        if (!p.isPrf())continue;
                        Iterator<DataSerie> ids = p.getSeries().iterator();
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
            lblTthInten.setText("");
            lblDsp.setText("");
            lblHkl.setText("");
        }
    }

    
    // Identificar el botó i segons quin sigui moure o fer zoom
    protected void do_graphPanel_mousePressed(MouseEvent arg0) {
        if (!arePatterns())return;
        this.dragPoint = new Point2D.Double(arg0.getPoint().x, arg0.getPoint().y);

        if (arg0.getButton() == MOURE) {
            log.debug("button MOURE");
            this.mouseDrag = true;
            this.mouseMove = true;
        }
        if (arg0.getButton() == ZOOM_BORRAR) {
            this.mouseDrag = true;
            this.mouseZoom = true;
        }
        if (arg0.getButton() == CLICAR) {
            this.mouseDrag = true;
//            this.zoomPoint = new Point2D.Double(arg0.getPoint().x, arg0.getPoint().y); //ES PODRIA UTILITZAR DRAGPOINT
            this.zoomRect = null; //reiniciem rectangle
            this.setMouseBox(true);
        }
        continuousRepaint=true;
        this.repaint();

    }

    protected void do_graphPanel_mouseReleased(MouseEvent e) {
        if (e.getButton() == MOURE){
            this.mouseDrag = false;
            this.mouseMove = false;            
        }
        if (e.getButton() == ZOOM_BORRAR){
            this.mouseDrag = false;
            this.mouseZoom = false;            
        }
        if (e.getButton() == CLICAR){
            this.setMouseBox(false);
        }
        if (!arePatterns())return;
        
        if (e.getButton() == CLICAR) {
            //COMPROVEM QUE HI HAGI UN MINIM D'AREA ENTREMIG (per evitar un click sol)
            if (FastMath.abs(e.getPoint().x-dragPoint.x)<minZoomPixels)return;
            
            Point2D.Double dataPointFinal = this.getDataPointFromFramePoint(new Point2D.Double(e.getPoint().x, e.getPoint().y));
            Point2D.Double dataPointInicial = this.getDataPointFromFramePoint(dragPoint);
            if (dataPointFinal!=null)log.writeNameNums("CONFIG", true, "dataPointFinal", dataPointFinal.x,dataPointFinal.y);
            log.writeNameNums("CONFIG", true, "e.getPoint", e.getPoint().x, e.getPoint().y);
            if (dataPointInicial!=null)log.writeNameNums("CONFIG", true, "dataPointInicial", dataPointInicial.x,dataPointInicial.y);
            log.writeNameNums("CONFIG", true, "dragPoint", dragPoint.x, dragPoint.y);
            
            if (dataPointFinal == null && dataPointInicial==null){
                log.debug("els dos punts a fora!");
                return;
            }
            
            if (dataPointFinal == null){
                //mirem si podem considerar l'extrem
                if ((e.getPoint().x-dragPoint.x)<0){
                    //vol dir que el final es mes petit que l'inicial, hem fet rectangle cap a l'esquerra, cap al zero, cal buscar
                    //el limit de l'esquerra
                    //poso +1 per assegurar
//                    dataPointFinal = this.getDataPointFromFramePoint(new Point2D.Double((double)this.getRectangleGraphArea().getMinX()+1, e.getPoint().y));
                    dataPointFinal = this.getDataPointFromFramePoint(new Point2D.Double(this.getRectangleGraphArea().getMinX()+1, e.getPoint().y));
                    if (dataPointFinal!=null)log.writeNameNums("CONFIG", true, "dataPointFinal (after)", dataPointFinal.x,dataPointFinal.y);

                }else{
                    //el final es mes gran, doncs al reves
                    dataPointFinal = this.getDataPointFromFramePoint(new Point2D.Double(this.getRectangleGraphArea().getMaxX()-1, e.getPoint().y));
                    if (dataPointFinal!=null)log.writeNameNums("CONFIG", true, "dataPointFinal (after)", dataPointFinal.x,dataPointFinal.y);

                }
            }
            if (dataPointInicial==null){
                //fem el mateix amb l'inicial
                if ((e.getPoint().x-dragPoint.x)<0){
                    //hem començat per la dreta, si es null es que estem fora per la dreta.
                    dataPointInicial = this.getDataPointFromFramePoint(new Point2D.Double(this.getRectangleGraphArea().getMaxX()-1, dragPoint.y));
                }else{
                    dataPointInicial = this.getDataPointFromFramePoint(new Point2D.Double(this.getRectangleGraphArea().getMinX()+1, dragPoint.y));
                }
                
            }
            
            if (dataPointFinal == null || dataPointInicial==null){
                log.debug("algun punt final encara a fora!");
                return;
            }

            double xrmin = FastMath.min(dataPointFinal.x, dataPointInicial.x);
            double xrmax = FastMath.max(dataPointFinal.x, dataPointInicial.x);
            log.writeNameNums("config", true, "xrangeMin xrangeMax", xrmin,xrmax);
            this.setXrangeMin(xrmin);
            this.setXrangeMax(xrmax);
            this.calcScaleFitX();
        }
        continuousRepaint=false;
    }
    

    protected void do_graphPanel_mouseWheelMoved(MouseWheelEvent e) {
        Point2D.Double p = new Point2D.Double(e.getPoint().x, e.getPoint().y);
        boolean zoomIn = (e.getWheelRotation() < 0);
        this.zoomY(zoomIn, p);
        this.repaint();
    }
    
    public boolean arePatterns(){
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
    

    //TODO:SHOULD BE DATAPOINT??
    private Point2D.Double getDataPointFromFramePoint(Point2D.Double framePoint){
        if (isFramePointInsideGraphArea(framePoint)){
            double xdp = ((framePoint.x - getGapAxisLeft() - padding) / this.getScalefitX()) + this.getXrangeMin();
//            double ydp = (((framePoint.y - getGapAxisBottom() - padding) + graphPanel.getHeight()) / this.getScalefitY()) + this.getYrangeMin();
            double ydp = (-framePoint.y+graphPanel.getHeight()-getGapAxisBottom() - padding)/this.getScalefitY() +this.getYrangeMin();
            return new Point2D.Double(xdp,ydp);
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
    
    private boolean isDataPointInsidePlotRange(DataPoint dp){
        if (dp.getX()>this.getXrangeMin() && dp.getX()<this.getXrangeMax() && dp.getY()>this.getYrangeMin() && dp.getY()<this.getYrangeMax()){
            return true;
        }else{
            return false;
        }
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
        
        this.repaint();
    }
    
    //NOMES S'HAURIA DE CRIDAR QUAN OBRIM UN PATTERN (per aixo private)
    private void autoDivLines(){
        this.setDiv_startValX(this.getXrangeMin());
        this.setDiv_startValY(this.getYrangeMin());
        
        //ara cal veure a quan es correspon en les unitats de cada eix
        double xppix = this.getXunitsPerPixel();
        double yppix = this.getYunitsPerPixel();
        
        log.writeNameNumPairs("config", true, "xppix,yppix",xppix,yppix);
        
        txtNdivx.setText(String.valueOf(incXPrimPIXELS/incXSecPIXELS));
        txtNdivy.setText(String.valueOf(incYPrimPIXELS/incYSecPIXELS));
        
        
        this.setDiv_incXPrim(incXPrimPIXELS*xppix);
        this.setDiv_incXSec(incXSecPIXELS*xppix);
        this.setDiv_incYPrim(incYPrimPIXELS*yppix);
        this.setDiv_incYSec(incYSecPIXELS*yppix);
        
        this.txtXdiv.setText(String.valueOf(this.getDiv_incXPrim()));
        this.txtYdiv.setText(String.valueOf(this.getDiv_incYPrim()));
        
        log.writeNameNumPairs("config", true, "div_incXPrim, div_incXSec, div_incYPrim, div_incYSec",div_incXPrim, div_incXSec, div_incYPrim, div_incYSec);

    }
    
    //valor inicial, valor d'increment per les separacions principals (tindran número), n divisions secundaries entre principals
    //iniVal l'hem suprimit d'aqui, la "finestra" no es responsabilitat d'aquesta funcio
    private void customDivLinesX(double incrPrincipals, double nDivisionsSecund){
        
        double currentXIni = this.getXrangeMin();
        
        this.setXrangeMin((int)this.getxMin());

        this.setDiv_startValX(this.getXrangeMin());
        this.setXrangeMin(currentXIni);
//        this.calcScaleFitX(); no cal perque en pricipi no toquem res d'aixo
        
        this.setDiv_incXPrim(incrPrincipals);
        this.setDiv_incXSec(incrPrincipals/nDivisionsSecund);
        
        this.txtXdiv.setText(String.valueOf(this.getDiv_incXPrim()));
        
   }
    
    private void customDivLinesY(double incrPrincipals, double nDivisionsSecund){
        double currentYIni = this.getYrangeMin();
        
        this.setYrangeMin(0); //TODO REVISAR SI ES EL COMPORTAMENT QUE VOLEM
        
//        if(this.getyMin()<0){
//            this.setYrangeMin((int)this.getyMin());
//        }else{
//            this.setYrangeMin(0);
//        }
        this.setDiv_startValY(this.getYrangeMin());
        this.setYrangeMin(currentYIni);
        
        this.setDiv_incYPrim(incrPrincipals);
        this.setDiv_incYSec(incrPrincipals/nDivisionsSecund);
        
        this.txtYdiv.setText(String.valueOf(this.getDiv_incYPrim()));
        
        log.writeNameNumPairs("config", true, "div_incXPrim, div_incXSec, div_incYPrim, div_incYSec",div_incXPrim, div_incXSec, div_incYPrim, div_incYSec);
    }

    
    //ens diu si s'han calculat els limits (o s'han assignat) per les linies de divisio
    private boolean checkIfDiv(){
        if (this.div_incXPrim == 0) return false;
        if (this.div_incXSec == 0) return false;
        if (this.div_incYPrim == 0) return false;
        if (this.div_incYSec == 0) return false;
        return true;
    }
    
    // ajusta la imatge al panell, mostrant-la tota sencera (calcula l'scalefit inicial)
    public void fitGraph() {
        this.resetView(false);
    }
    


//    private void calcMaxMinX(){
//        //TODO
//    }
//    
//    private void calcMaxMinY(){
//        //TODO
//    }
    
    private void calcMaxMinXY(){
        Iterator<Pattern1D> itrp = getPatterns().iterator();
        double maxX = Double.MIN_VALUE;
        double minX = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        while (itrp.hasNext()){
            Pattern1D patt = itrp.next();
            for (int i=0; i<patt.getSeries().size(); i++){
                DataSerie s = patt.getSeries().get(i);
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
        this.repaint();
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
    
    public Point2D.Double getIntersectionPoint(Line2D.Double line1, Line2D.Double line2) {
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
    
    public Point2D.Double[] getIntersectionPoint(Line2D.Double line, Rectangle2D rectangle) {

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
    protected void do_chckbxFixedAxis_itemStateChanged(ItemEvent e) {
        this.fixAxes=chckbxFixedAxis.isSelected();
        this.repaint();
    }
    
    protected void do_btnApply_actionPerformed(ActionEvent arg0) {
        this.setXrangeMin(Double.parseDouble(txtXmin.getText()));
        this.setXrangeMax(Double.parseDouble(txtXmax.getText()));
        this.setYrangeMin(Double.parseDouble(txtYmin.getText()));
        this.setYrangeMax(Double.parseDouble(txtYmax.getText()));
        this.calcScaleFitX();
        this.calcScaleFitY();
        this.repaint();
    }
    
    protected void fillWindowValues(){
        this.txtXmin.setText(String.valueOf(this.getXrangeMin()));
        this.txtXmax.setText(String.valueOf(this.getXrangeMax()));
        this.txtYmin.setText(String.valueOf(this.getYrangeMin()));
        this.txtYmax.setText(String.valueOf(this.getYrangeMax()));
    }
    
    protected void do_btnApplydiv_actionPerformed(ActionEvent e) {
        this.customDivLinesX(Double.parseDouble(txtXdiv.getText()), Double.parseDouble(txtNdivx.getText()));
        this.customDivLinesY(Double.parseDouble(txtYdiv.getText()), Double.parseDouble(txtNdivy.getText()));
        this.repaint();
    }
    
    
//  ------------------------------------ PANELL DE DIBUIX
    class Plot1d extends JPanel {

        private static final long serialVersionUID = 1L;

        private int panelW, panelH;
//        private double xScale, yScale;
        
        public Plot1d(){
            super();
            this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        }
        
        
//        private void drawGrid(Graphics2D g1){
//            log.fine("drawGrid entered");
//            
//        }
        
        private void drawAxes(Graphics2D g1, boolean grid){
            log.fine("drawAxes entered");

            //provem de fer linia a 60 pixels de l'esquerra i a 60 pixels de baix (40 i 40 de dalt i a la dreta com a marges)
            
            double coordXeixY = getGapAxisLeft()+padding;
            double coordYeixX = panelH-getGapAxisBottom()-padding;
            
            Point2D.Double vytop = new Point2D.Double(coordXeixY,getGapAxisTop()+padding);
            Point2D.Double vybot = new Point2D.Double(coordXeixY,coordYeixX);
            Point2D.Double vxleft = vybot;
            Point2D.Double vxright = new Point2D.Double(panelW-getGapAxisRight()-padding,coordYeixX);
            
            log.writeNameNums("fine", true, "(axes) vy vx", vytop.x,vytop.y,vybot.x,vybot.y,vxleft.x,vxleft.y,vxright.x,vxright.y);
            
            if(lightTheme){
                g1.setColor(Light_frg);
            }else{
                g1.setColor(Dark_frg);
            }
            BasicStroke stroke = new BasicStroke(1.0f);
            g1.setStroke(stroke);
            
            Line2D.Double ordenada = new Line2D.Double(vytop,vybot);  //Y axis vertical
            Line2D.Double abcissa = new Line2D.Double(vxleft, vxright);  //X axis horizontal
            
            g1.draw(ordenada);
            g1.draw(abcissa);
            
            //PINTEM ELS TITOLS DELS EIXOS
            Font font = g1.getFont();
            FontRenderContext frc = g1.getFontRenderContext();
            
            
            // Y-axis (ordinate) label.
            String s = getYlabel();
            double sw = font.getStringBounds(s, frc).getWidth();
            double sh =  font.getStringBounds(s, frc).getHeight();
            double ylabelheight = sh; //per utilitzar-ho despres
            double sx = AxisLabelsPadding;
            double sy = sh + AxisLabelsPadding;
            if (verticalYlabel){
                sy = (panelH - sw)/2;
//                sx = getGapAxisLeft()/2;
                sx = (ylabelheight/2)+padding;
                AffineTransform orig = g1.getTransform();
                g1.rotate(-Math.PI/2,sx,sy);
                g1.drawString(s,(float)sx,(float)sy);
                g1.setTransform(orig);
            }else{
                //el posem sobre l'eix en horitzontal
                g1.drawString(s,(float)sx,(float)sy);
            }
            
            
            // X-axis (abcissa) label.
            s = getXlabel();
//            sy = panelH - getGapAxisBottom()/2;
            sy = panelH - AxisLabelsPadding;
            sw = font.getStringBounds(s, frc).getWidth();
            sx = (panelW - sw)/2;
            g1.drawString(s, (float)sx,(float)sy);
            
            
            
            // **** linies divisio eixos
            if (!checkIfDiv())return;
            if (fixAxes) autoDivLines(); //es pot fer mes eficient sense fer-ho cada cop
            //TODO
            //---eix X
            //Per tots els punts les coordenades Y seran les mateixes
//            double yiniPrim = panelH-getGapAxisBottom()-padding - (div_PrimPixSize/2.f); 
//            double yfinPrim = panelH-getGapAxisBottom()-padding + (div_PrimPixSize/2.f);
            double yiniPrim = coordYeixX - (div_PrimPixSize/2.f); 
            double yfinPrim = coordYeixX + (div_PrimPixSize/2.f);
            
            
//            double yLabel = yfinPrim + AxisLabelsPadding; //TODO de moment provo aixo
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
                s = String.format("%.3f", xval);
                sw = font.getStringBounds(s, frc).getWidth();
                sh = font.getStringBounds(s, frc).getHeight();
                double xLabel = xvalPix - sw/2f; //el posem centrat a la linia
                double yLabel = yfinPrim + AxisLabelsPadding + sh;
                g1.drawString(s, (float)xLabel, (float)yLabel);
                xval = xval + div_incXPrim;

                //i ara el grid
                //pel grid, vytop.y sera el punt superior de la linia, yiniPrim sera el punt inferior (AIXO PER LES Y, despres les X es defineixen al bucle)
//                if(grid){
//                    BasicStroke dashed = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[]{1,2}, 0);
//                    g1.setStroke(dashed);
//                    Line2D.Double ld = new Line2D.Double(xvalPix,vytop.y,xvalPix,yiniPrim);
//                    g1.draw(ld);
//                    g1.setStroke(stroke); //recuperem l'anterior
//                }
                
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
                if(grid){
                    BasicStroke dashed = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[]{1,2}, 0);
                    g1.setStroke(dashed);
                    Line2D.Double ld = new Line2D.Double(xvalPix,vytop.y,xvalPix,yiniSec);
                    g1.draw(ld);
                    g1.setStroke(stroke); //recuperem l'anterior
                }
                
                if(xval> (int)(1+getxMax()))break; //provem de posar-ho aqui perque no dibuixi mes enllà
            }
            
            //---eix Y
            //Per tots els punts les coordenades Y seran les mateixes
            double xiniPrim = coordXeixY - (div_PrimPixSize/2.f); 
            double xfinPrim = coordXeixY + (div_PrimPixSize/2.f);
//            double yLabel = yfinPrim + AxisLabelsPadding; //TODO de moment provo aixo
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
                s = String.format("%.1f", yval);
                sw = font.getStringBounds(s, frc).getWidth();
                sh = font.getStringBounds(s, frc).getHeight();
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
                if(grid){
                    BasicStroke dashed = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[]{1,2}, 0);
                    g1.setStroke(dashed);
                    Line2D.Double ld = new Line2D.Double(vxright.x,yvalPix,xfinSec,yvalPix);
                    g1.draw(ld);
                    g1.setStroke(stroke); //recuperem l'anterior
                }
                
            }
            
            log.fine("drawAxes exit");

        }
        
        private void drawPatternLine(Graphics2D g1, DataSerie serie, Color col){
            //TODO color, style, etc...
            log.fine("drawPatternLine entered");
//            g1.setColor(Color.BLUE);
            g1.setColor(col);
//            BasicStroke stroke = new BasicStroke(3f);
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
                
                //prova del zero offset i l'escala
//                DataPoint dp1 = new DataPoint(serie.getSeriePoints().get(i).getX()+serie.getZerrOff(),
//                        serie.getSeriePoints().get(i).getY()*serie.getScale(), 
//                        serie.getSeriePoints().get(i).getSdy()*serie.getScale());
//                if (i==(serie.getSeriePoints().size()-1)){ //p1 es l'ultim punt, ja podem sortir del for
//                    break;
//                }
//                DataPoint dp2 = new DataPoint(serie.getSeriePoints().get(i+1).getX()+serie.getZerrOff(),
//                        serie.getSeriePoints().get(i+1).getY()*serie.getScale(), 
//                        serie.getSeriePoints().get(i+1).getSdy()*serie.getScale());
//                
//                Point2D.Double p1 = getFramePointFromDataPoint(dp1);
//                Point2D.Double p2 = getFramePointFromDataPoint(dp2);
                
                //abans tenia directament això:
//                Point2D.Double p1 = getFramePointFromDataPoint(serie.getSeriePoints().get(i));
//                if (i==(serie.getSeriePoints().size()-1)){ //p1 es l'ultim punt, ja podem sortir del for
//                    break;
//                }
//                Point2D.Double p2 = getFramePointFromDataPoint(serie.getSeriePoints().get(i+1));
                
                //ara:
                // si els 2 son fora de l'area de dibuix passem als seguents
                // si un dels 2 es fora de l'area de dibuix cal mirar per quin costat i agafar la interseccio com a punt
                // si els 2 son dins doncs es fa la linia normal
                
                boolean isP1 = isFramePointInsideGraphArea(p1);
                boolean isP2 = isFramePointInsideGraphArea(p2);
                boolean trobat = false;
                
                if (!isP1){
                    if (!isP2){
                        continue;
                    }else{
                        //P1 esta fora, cal redefinirlo amb la interseccio amb l'eix pertinent
                        Point2D.Double[] p = getIntersectionPoint(new Line2D.Double(p1,p2),getRectangleGraphArea());
                        for (int j=0;j<p.length;j++){
                            if (p[j]!=null){
                                p1 = p[j];
                                trobat = true;
                            }
                        }
                    }
                    if (trobat) {
                        log.fine("P1 redefinit");
                    }else{
                        log.fine("P1 NO redefinit");
                    }
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
                                trobat = true;
                            }
                        }
                    }
                    if (trobat){
                        log.fine("P2 redefinit");
                    }else{
                        log.fine("P2 NO redefinit");
                    }
                }                
                
                //ARA JA PODEM DIBUIXAR LA LINIA
                Line2D.Double l = new Line2D.Double(p1.x,p1.y,p2.x,p2.y);
                g1.draw(l);
                
            }
            log.fine("drawPatternLine exit");
        }
    
    //separo linia i punts per si volem canviar l'ordre de dibuix
    private void drawPatternPoints(Graphics2D g1, DataSerie serie, Color col){
        //TODO color, style, etc...
        log.fine("drawPatternPoints entered");
        for (int i = 0; i < serie.getNpoints(); i++){
//            g1.setColor(Color.RED);
            g1.setColor(col);
//            BasicStroke stroke = new BasicStroke(2.0f);
            BasicStroke stroke = new BasicStroke(0.0f);
            g1.setStroke(stroke);
            
            //despres del canvi a private de seriePoints
            Point2D.Double p1 = getFramePointFromDataPoint(serie.getPoint(i));

            
            //prova del zero offset i l'escala
//            DataPoint dp1 = new DataPoint(serie.getSeriePoints().get(i).getX()+serie.getZerrOff(),
//                    serie.getSeriePoints().get(i).getY()*serie.getScale(), 
//                    serie.getSeriePoints().get(i).getSdy()*serie.getScale());
//            Point2D.Double p1 = getFramePointFromDataPoint(dp1);
            //abans tenia aixo només:
//            Point2D.Double p1 = getFramePointFromDataPoint(serie.getSeriePoints().get(i));
            
            
            if (isFramePointInsideGraphArea(p1)){
                double radiPunt = serie.getMarkerSize()/2.f;
//                g1.drawOval(FastMath.round(p1.x-radiPunt), FastMath.round(p1.y-radiPunt), FastMath.round(serie.getMarkerSize()), FastMath.round(serie.getMarkerSize()));
                g1.fillOval((int)FastMath.round(p1.x-radiPunt), (int)FastMath.round(p1.y-radiPunt), FastMath.round(serie.getMarkerSize()), FastMath.round(serie.getMarkerSize()));
            }
        }
        log.fine("drawPatternPoints exit");
    }
        
    
    //separo linia i punts per si volem canviar l'ordre de dibuix
    private void drawErrorBars(Graphics2D g1, DataSerie serie, Color col){
        //TODO color, style, etc...
        log.fine("drawErrorBars entered");
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
        log.fine("drawErrorBars exit");
    }

    private void drawHKL(Graphics2D g1, DataSerie serie, Color col){
        log.fine("drawHKL entered");
//        Font font = g1.getFont(); //font inicial
        for (int i = 0; i < serie.getNpoints(); i++){
            g1.setColor(col);
            BasicStroke stroke = new BasicStroke(serie.getLineWidth());
            g1.setStroke(stroke);
            
            //despres del canvi a private de seriePoints
            double tth = serie.getHKLPoint(i).getTth();
            
            //la X es la 2THETA pero la Y hauria de ser el punt de menor intensitat de OBS més un hkloffset (en pixels, definit a patt1d)
            double fx = getFrameXFromDataPointX(tth);
            double fy = getFrameYFromDataPointY(0.0);
//            log.writeNameNumPairs("config", true, "fx,fy", fx,fy);
            fy = fy - Pattern1D.getHkloff() +Pattern1D.getHklticksize()/2f;  //pensem que la Y es cap avall!
//            log.writeNameNumPairs("config", true, "fx,fy", fx,fy);
            
            Point2D.Double ptop = new Point2D.Double(fx, fy-Pattern1D.getHklticksize()/2);
            Point2D.Double pbot = new Point2D.Double(fx, fy+Pattern1D.getHklticksize()/2);
            
            //comprovem que tot estigui dins
            if (!isFramePointInsideGraphArea(ptop) || !isFramePointInsideGraphArea(pbot)){
                continue;
            }
            
            //ara dibuixem la linia
            g1.draw(new Line2D.Double(ptop.x,ptop.y,pbot.x,pbot.y));
            
//            if (hkllabels){
//                String s = serie.getHKLPoint(i).toString();
//                g1.drawString(s, t_X,t_Y);
////                g1.setFont(font);                //recuperem font
//            }
        }
        log.fine("drawHKL exit");
    }
    
    private void drawLegend(Graphics2D g1){
        
        
//        Point2D.Double vytop = new Point2D.Double(getGapAxisLeft()+padding,getGapAxisTop()+padding);
//        Point2D.Double vybot = new Point2D.Double(getGapAxisLeft()+padding,panelH-getGapAxisBottom()-padding);
//        Point2D.Double vxleft = vybot;
//        Point2D.Double vxright = new Point2D.Double(panelW-getGapAxisRight()-padding,panelH-getGapAxisBottom()-padding);

        int rectMaxWidth = 300;
        int currentMaxWidth = 0;
        if (autoPosLegend){
            legendX = panelW-padding-rectMaxWidth;
            legendY = padding;
        }
//        log.writeNameNums("config", true, "legendX,legendY", legendX,legendY);
        int entryHeight = 25;
        int margin = 10;
        int linelength = 15;
        float strokewidth = 3;
        Font font = g1.getFont(); //font inicial
        
        Iterator<Pattern1D> itrp = getPatterns().iterator();
        
        //primer dibuixare el recuadre perque si ho faig al final al pintar-lo ho borro tot.
        //cal mirar quantes entries tenim
//        int entries = 0;
//        while (itrp.hasNext()){
//            Pattern1D patt = itrp.next();
//            entries = entries + patt.getSeries().size();
//        }
//        int rerctheight = entries*entryHeight+2*margin;
//        g1.setColor(Color.WHITE);
//        g1.fillRect(xvert,yvert,FastMath.min(currentMaxWidth,rectMaxWidth),rerctheight);
//        g1.setColor(Color.BLACK);
//        BasicStroke stroke = new BasicStroke(1.0f);
//        g1.setStroke(stroke);
//        g1.drawRect(xvert,yvert,FastMath.min(currentMaxWidth,rectMaxWidth),rerctheight);
        
        
        int entries = 0;
        while (itrp.hasNext()){
            Pattern1D patt = itrp.next();
            for (int i=0; i<patt.getSeries().size(); i++){
                if (!patt.getSeries().get(i).isPlotThis())continue;
                
//                log.debug("in drawlegend loop, entry "+entries);
                
                //dibuixem primer la linia
                int l_iniX = legendX+margin;
                int l_finX = legendX+margin+linelength;
                int l_y = (int) (legendY+margin+entries*(entryHeight)+FastMath.round(entryHeight/2.));
                
//                log.writeNameNums("config", true, "l_iniX,l_finX,l_y", l_iniX,l_finX,l_y);
                
                g1.setColor(patt.getSeries().get(i).getColor());
                BasicStroke stroke = new BasicStroke(strokewidth);
                g1.setStroke(stroke);
                
                Line2D.Float l = new Line2D.Float(l_iniX,l_y,l_finX,l_y);
                g1.draw(l);
                
                //ara el text
                int t_X = l_finX+margin; //TODO: revisar si queda millor x2
                int maxlength = panelW-padding-margin-t_X;
                String s = patt.getFile().getName()+" ("+patt.getSeries().get(i).getTipusSerie().toString()+")"; 
                double[] swh = getWidthHeighString(g1,s);
                while (swh[0]>maxlength){
                    g1.setFont(g1.getFont().deriveFont(g1.getFont().getSize()-1f));
                    swh = getWidthHeighString(g1,s);
                }
                int t_Y = (int) (l_y-strokewidth+(swh[1]/2.));
                g1.drawString(s, t_X,t_Y);
                g1.setFont(font);                //recuperem font
                
                int currentWidth = (int) (margin + linelength + margin + swh[0] + margin);
                if (currentWidth>currentMaxWidth)currentMaxWidth = currentWidth;
                
//                log.writeNameNums("config", true, "t_X,t_Y", t_X,t_Y);
                
                entries = entries +1;
            }
        }
        
        int rerctheight = entries*entryHeight+2*margin;
        if (lightTheme){
            g1.setColor(Light_Legend_bkg);    
        }else{
            g1.setColor(Dark_Legend_bkg);
        }
//        g1.setColor(Color.WHITE);
        g1.fillRect(legendX,legendY,FastMath.min(currentMaxWidth,rectMaxWidth),rerctheight);
        if (lightTheme){
            g1.setColor(Light_Legend_line);    
        }else{
            g1.setColor(Dark_Legend_line);
        }
//        g1.setColor(Color.BLACK);
        BasicStroke stroke = new BasicStroke(1.0f);
        g1.setStroke(stroke);
        g1.drawRect(legendX,legendY,FastMath.min(currentMaxWidth,rectMaxWidth),rerctheight);
        
        //repeteixo lo d'abans per pintar a sobre... no es gaire eficient...
        //NO REPETEIXO EXACTE, AQUI TINDRE EN COMPTE ELS TIPUS DE LINIA
        itrp = getPatterns().iterator();
        entries = 0;
        while (itrp.hasNext()){
            Pattern1D patt = itrp.next();
            for (int i=0; i<patt.getSeries().size(); i++){
                if (!patt.getSeries().get(i).isPlotThis())continue;
                
//                log.debug("in drawlegend loop, entry "+entries);

                stroke = new BasicStroke(strokewidth);
                g1.setStroke(stroke);
                g1.setColor(patt.getSeries().get(i).getColor());

                //dibuixem primer la linia (si s'escau)
                int l_iniX = legendX+margin;
                int l_finX = legendX+margin+linelength;
                int l_y = (int) (legendY+margin+entries*(entryHeight)+FastMath.round(entryHeight/2.));
                if (patt.getSeries().get(i).getLineWidth()>0){
                    
                    if (patt.getSeries().get(i).getTipusSerie()==DataSerie.serieType.hkl){
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
                if (patt.getSeries().get(i).getMarkerSize()>0){
//                    int centreX = (int) ((l_iniX+l_finX)/2.f);
                    int sep = (int) (FastMath.abs(l_iniX-l_finX)/5.f);
                    int x1 = l_iniX+sep;
                    int x2 = l_iniX+sep*4;
                    

//                    g1.setColor(patt.getSeries().get(i).getColor());
                    stroke = new BasicStroke(0.0f);
                    g1.setStroke(stroke);
                    double radiPunt = patt.getSeries().get(i).getMarkerSize()/2.f;
//                    g1.fillOval((int)FastMath.round(centreX-radiPunt), (int)FastMath.round(l_y-radiPunt), FastMath.round(patt.getSeries().get(i).getMarkerSize()), FastMath.round(patt.getSeries().get(i).getMarkerSize()));
                    g1.fillOval((int)FastMath.round(x1-radiPunt), (int)FastMath.round(l_y-radiPunt), FastMath.round(patt.getSeries().get(i).getMarkerSize()), FastMath.round(patt.getSeries().get(i).getMarkerSize()));
                    g1.fillOval((int)FastMath.round(x2-radiPunt), (int)FastMath.round(l_y-radiPunt), FastMath.round(patt.getSeries().get(i).getMarkerSize()), FastMath.round(patt.getSeries().get(i).getMarkerSize()));
                  }
                //recuperem stroke width per si de cas hi havia markers
                stroke = new BasicStroke(strokewidth);
                g1.setStroke(stroke);
                    
                //ara el text
                int t_X = l_finX+margin; //TODO: revisar si queda millor x2
                int maxlength = panelW-padding-margin-t_X;
                String s = patt.getFile().getName()+" ("+patt.getSeries().get(i).getTipusSerie().toString()+")"; 
                double[] swh = getWidthHeighString(g1,s);
                while (swh[0]>maxlength){
                    g1.setFont(g1.getFont().deriveFont(g1.getFont().getSize()-1f));
                    swh = getWidthHeighString(g1,s);
                }
                int t_Y = (int) (l_y-strokewidth+(swh[1]/2.));
                g1.drawString(s, t_X,t_Y);
                g1.setFont(font);                //recuperem font
                
                int currentWidth = (int) (margin + linelength + margin + swh[0] + margin);
                if (currentWidth>currentMaxWidth)currentMaxWidth = currentWidth;
                
//                log.writeNameNums("config", true, "t_X,t_Y", t_X,t_Y);
                
                entries = entries +1;
            }
        }
        
        //dibuixem el rectangle  ---- ho mouré a dalt perque sino al pintar-lo m'ho borra tot...
//        int rerctheight = entries*entryHeight+2*margin;
//        g1.setColor(Color.BLACK);
//        BasicStroke stroke = new BasicStroke(1.0f);
//        g1.setStroke(stroke);
//        g1.drawRect(xvert,yvert,FastMath.min(currentMaxWidth,rectMaxWidth),rerctheight);
//        int alpha = 127; // 50% transparent
//        Color whtransp = new Color(255, 255, 255, alpha);
//        g1.setColor(whtransp);
//        g1.fillRect(xvert,yvert,FastMath.min(currentMaxWidth,rectMaxWidth),rerctheight);

    }

    private void drawPeaks(Graphics2D g1){
        //only selected series
        int gapPixels = 5; //gap between top of peak and line
        int sizePix = 20;
                
        Iterator<DataSerie> itrds = getSelectedSerie().iterator();
        while(itrds.hasNext()){
            DataSerie ds = itrds.next();
            if (ds.getNpeaks()==0){
                log.debug("no peaks on serie "+ds.getPatt1D().getSeries().indexOf(ds)+" (patt "+getPatterns().indexOf(ds.getPatt1D())+")");
                return;
            }
            for (int i=0; i<ds.getNpeaks();i++){
                DataPoint dp = ds.getPeak(i);
                Point2D.Double ptop = getFramePointFromDataPoint(dp);
                //ara fem una linia amunt recta
                ptop.y=ptop.y-gapPixels;
//                Point2D.Double ptoptop = new Point2D.Double(ptop.x, ptop.y-sizePix);
                
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
        @Override
        protected void paintComponent(Graphics g) {
//            log.fine("paintComponent entered");
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            
            g2.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON)); // perque es vegin mes suaus...
            
            if (lightTheme){
//                g2.setBackground(Light_bkg);
                this.setBackground(Light_bkg);
            }else{
                this.setBackground(Dark_bkg);
//                g2.setBackground(Dark_bkg);
            }
            
//            if (transparentBkg){
//                
//            }
            
            final Graphics2D g1 = (Graphics2D) g2.create();

            if (getPatterns().size() > 0) {
                
                panelW = this.getWidth();
                panelH = this.getHeight();
                
                //primer caculem els limits
                calcMaxMinXY();
                if (getScalefitY()<0){
                    calcScaleFitY();    
                }
                if (getScalefitX()<0){
                    calcScaleFitX();    
                }
                
                //1st draw axes (and optionally grid)
                this.drawAxes(g1,showGrid);
                
                //grid
//                if (showGrid){
//                    this.drawGrid(g1);
//                }
                
                Iterator<Pattern1D> itrp = getPatterns().iterator();
                while (itrp.hasNext()){
                    Pattern1D patt = itrp.next();
                    for (int i=0; i<patt.getSeries().size(); i++){
                        if (!patt.getSeries().get(i).isPlotThis())continue;
                        if(patt.getSeries().get(i).getTipusSerie()==DataSerie.serieType.hkl){
                            drawHKL(g1,patt.getSeries().get(i),patt.getSeries().get(i).getColor());
                        }else{
                            drawPatternLine(g1,patt.getSeries().get(i),patt.getSeries().get(i).getColor()); 
                            drawPatternPoints(g1,patt.getSeries().get(i),patt.getSeries().get(i).getColor());
                        }
                        if (patt.getSeries().get(i).isShowErrBars()){
                            drawErrorBars(g1,patt.getSeries().get(i),patt.getSeries().get(i).getColor());
                        }
                    }
                }
                
                if(showLegend){
                    drawLegend(g1);
                }
                
                if (showPeaks){
                    drawPeaks(g1);
                }
                
                if (isShowBackground()){
                    if (bkgserie.getNpoints()!=0){
                        drawPatternLine(g1,bkgserie,bkgserie.getColor()); 
                        drawPatternPoints(g1,bkgserie,bkgserie.getColor());
                    }
                }
                
                if (mouseBox == true && zoomRect != null) {
                    //dibuixem el rectangle
                    g1.setColor(Color.darkGray);
                    BasicStroke stroke = new BasicStroke(3f);
                    g1.setStroke(stroke);
                    g1.draw(zoomRect);
                    Color gristransp = new Color(Color.LIGHT_GRAY.getRed(), Color.LIGHT_GRAY.getGreen(),Color.LIGHT_GRAY.getBlue(), 128 );
                    g1.setColor(gristransp);
                    g1.fill(zoomRect);
                }
                
                g1.dispose();
                g2.dispose();
                
                fillWindowValues();
                if(continuousRepaint)this.repaint();
            }
//            log.fine("paintComponent exited");
        }
    }
    
    
    //SETTERS AND GETTERS
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
        this.repaint();
    }

    public String getYlabel() {
        return ylabel;
    }

    public void setYlabel(String ylabel) {
        this.ylabel = ylabel;
        this.repaint();
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

    public static boolean isLightTheme() {
        return lightTheme;
    }

    public static void setLightTheme(boolean lightTheme) {
        PlotPanel.lightTheme = lightTheme;
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
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

    public Plot1d getGraphPanel() {
        return graphPanel;
    }

    public void setGraphPanel(Plot1d graphPanel) {
        this.graphPanel = graphPanel;
    }

    protected void do_btnResetView_actionPerformed(ActionEvent e) {
        this.fitGraph();
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

    public ArrayList<DataSerie> getSelectedSerie() {
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

    public DataSerie getBkgserie() {
        return bkgserie;
    }

    public void setBkgserie(DataSerie bkgserie) {
        this.bkgserie = bkgserie;
    }
}

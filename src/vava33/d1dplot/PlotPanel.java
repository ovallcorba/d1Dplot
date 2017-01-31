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
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JPanel;

import org.apache.commons.math3.util.FastMath;

import com.vava33.jutils.VavaLogger;

import vava33.d1dplot.auxi.DataPoint;
import vava33.d1dplot.auxi.DataSerie;
import vava33.d1dplot.auxi.Pattern1D;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.border.BevelBorder;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class PlotPanel extends JPanel {

    private ArrayList<Pattern1D> patterns; //data to plot (series inside pattern1d)

    private int gapAxisTop = 18;
    private int gapAxisBottom = 30;
    private int gapAxisRight = 15;
    private int gapAxisLeft = 60;
    private static int padding = 10;
    private static int AxisLabelsPadding = 2;
    
    private float xMin = 0;
    private float xMax = 60;
    private float yMin = 0;
    private float yMax = 100000;
    private float incX = 10;
    private float incY = 10000;
    private float scalefitX = -1;
    private float scalefitY = -1;
    
    private float xrangeMin = -1; //rangs dels eixos X i Y en 2theta/counts per calcular scalefitX,Y
    private float xrangeMax = -1;
    private float yrangeMin = -1;
    private float yrangeMax = -1;
    
    //Parametres visuals
    private static float incXPrimPIXELS = 100;
    private static float incXSecPIXELS = 25;
    private static float incYPrimPIXELS = 100;
    private static float incYSecPIXELS = 25;
    
    private String xlabel = "2theta";
    private String ylabel = "Intensity";
    private Plot1d graphPanel;
    
    // DEFINICIO BUTONS DEL MOUSE
    private static int MOURE = MouseEvent.BUTTON2;
    private static int CLICAR = MouseEvent.BUTTON1;
    private static int ZOOM_BORRAR = MouseEvent.BUTTON3;
    //parametres interaccio/contrast
    private static int minZoomPixels = 5;
    private static float incZoom = 0.05f;
    private static float facZoom = 1.5f;
    private boolean mouseBox = false;
    private boolean mouseDrag = false;
    private boolean mouseMove = false;
    private boolean mouseZoom = false;
    private Rectangle2D.Float zoomRect;
//    private float originDPx, originDPy; //a quin datapoint correspon l'origen dels eixos en un zoom donat (inicial 0,0)
//    private int originFrX, originFrY;  //a quin pixel del frame correspon l'origen dels eixos, sempre serà el mateix (en funció del height del frame)
    private Point2D.Float zoomPoint, dragPoint;
    
    
    //LINIES DIVISIO
    float div_incXPrim, div_incXSec, div_incYPrim, div_incYSec;
    float div_startValX, div_startValY;
    private static int div_PrimPixSize = 8;
    private static int div_SecPixSize = 4;
    boolean fixAxes = false;
    boolean autoDiv = true;
    boolean YLabelVertical = false;
    
    boolean showLegend = true;
    
    private static VavaLogger log = D1Dplot_global.log;
    private JTextField txtXdiv;
    private JTextField txtYdiv;
    private JCheckBox chckbxFixedAxis;
    private JCheckBox chckbxAutodiv;
    private JTextField txtXmin;
    private JTextField txtXmax;
    private JTextField txtYmin;
    private JTextField txtYmax;
    private JButton btnApply;
    private JButton btnApplydiv;
    private JTextField txtNdivx;

    
    private boolean continuousRepaint = false;
    
    /**
     * Create the panel.
     */
    public PlotPanel() {
        setBackground(Color.WHITE);
        setLayout(new MigLayout("insets 0", "[grow]", "[][grow]"));
        
        JPanel buttons_panel = new JPanel();
        buttons_panel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
        add(buttons_panel, "cell 0 0,grow");
        buttons_panel.setLayout(new MigLayout("", "[][grow][grow][grow][grow][]", "[25px][]"));
        
        chckbxFixedAxis = new JCheckBox("Fix Axes");
        chckbxFixedAxis.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                do_chckbxFixedAxis_itemStateChanged(e);
            }
        });
        buttons_panel.add(chckbxFixedAxis, "cell 0 0");
        
        chckbxAutodiv = new JCheckBox("AutoDiv");
        chckbxAutodiv.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                do_chckbxAutodiv_itemStateChanged(e);
            }
        });
        chckbxAutodiv.setSelected(true);
        buttons_panel.add(chckbxAutodiv, "cell 1 0");
        
        txtXdiv = new JTextField();
        txtXdiv.setText("xdiv");
        buttons_panel.add(txtXdiv, "cell 2 0,growx");
        txtXdiv.setColumns(10);
        
        txtNdivx = new JTextField();
        txtNdivx.setText("NdivX");
        buttons_panel.add(txtNdivx, "cell 3 0,growx");
        txtNdivx.setColumns(10);
        
        txtYdiv = new JTextField();
        txtYdiv.setText("ydiv");
        buttons_panel.add(txtYdiv, "cell 4 0,growx");
        txtYdiv.setColumns(10);
        
        btnApplydiv = new JButton("applyDiv");
        btnApplydiv.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_btnApplydiv_actionPerformed(e);
            }
        });
        buttons_panel.add(btnApplydiv, "cell 5 0");
        
        txtXmin = new JTextField();
        txtXmin.setText("Xmin");
        buttons_panel.add(txtXmin, "cell 0 1,growx");
        txtXmin.setColumns(10);
        
        txtXmax = new JTextField();
        txtXmax.setText("xmax");
        buttons_panel.add(txtXmax, "cell 1 1,growx");
        txtXmax.setColumns(10);
        
        txtYmin = new JTextField();
        txtYmin.setText("ymin");
        buttons_panel.add(txtYmin, "cell 2 1,growx");
        txtYmin.setColumns(10);
        
        txtYmax = new JTextField();
        txtYmax.setText("ymax");
        buttons_panel.add(txtYmax, "cell 4 1,growx");
        txtYmax.setColumns(10);
        
        btnApply = new JButton("apply");
        btnApply.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                do_btnApply_actionPerformed(arg0);
            }
        });
        buttons_panel.add(btnApply, "cell 5 1");
        
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

        
        inicia();
    }

    private void inicia(){
        this.patterns = new ArrayList<Pattern1D>();
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

        Point2D.Float currentPoint = new Point2D.Float(e.getPoint().x, e.getPoint().y);

        if (this.mouseDrag == true && this.mouseMove) {
            float incX, incY;
            // agafem el dragpoint i l'actualitzem
            incX = currentPoint.x - dragPoint.x;
            incY = currentPoint.y - dragPoint.y;
            this.dragPoint = currentPoint;
            this.movePattern(incX, incY);
        }
        
        //WE DO SCROLL OR ZOOMOUT DEPENDING
        if (this.mouseDrag == true && this.mouseZoom) {
            float incY,incX;
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
            Rectangle2D.Float rarea = getRectangleGraphArea();
            float rwidth = FastMath.abs(dragPoint.x-currentPoint.x);
            if (rwidth<minZoomPixels)return;
            float rheight = rarea.height;
            float yrect = rarea.y;
            //defecte drag cap a la dreta
            float xrect = dragPoint.x;
            if (dragPoint.x > currentPoint.x){
                //estem fent el drag cap a la esquerra, corregim vertex
                xrect = currentPoint.x;
            }
            zoomRect = new Rectangle2D.Float(xrect,yrect,rwidth,rheight);
        }
        
    }

    //es mouen en consonancia els limits de rang x i y
    public void movePattern(float incX, float incY){//, boolean repaint) {
        //TODO VIGILAR LIMITS
        this.setXrangeMin(this.getXrangeMin()-(incX/scalefitX));
        this.setXrangeMax(this.getXrangeMax()-(incX/scalefitX));
        this.setYrangeMin(this.getYrangeMin()+(incY/scalefitY));
        this.setYrangeMax(this.getYrangeMax()+(incY/scalefitY));
        this.calcScaleFitX();
        this.calcScaleFitY();
        
        log.writeNameNums("fine", true, "ranges x y min max", getXrangeMin(),getXrangeMax(),getYrangeMin(),getYrangeMax());
        
//        if (repaint) {
//            this.repaint();
//        }
    }
    

    protected void do_graphPanel_mouseMoved(MouseEvent e) {
        // TODO
    }

    
    // Identificar el botó i segons quin sigui moure o fer zoom
    protected void do_graphPanel_mousePressed(MouseEvent arg0) {
        if (!arePatterns())return;
        this.dragPoint = new Point2D.Float(arg0.getPoint().x, arg0.getPoint().y);

        if (arg0.getButton() == MOURE) {
            log.debug("button MOURE");
            this.mouseDrag = true;
            this.mouseMove = true;
        }
        if (arg0.getButton() == ZOOM_BORRAR) {
            this.zoomPoint = new Point2D.Float(arg0.getPoint().x, arg0.getPoint().y);
            this.mouseDrag = true;
            this.mouseZoom = true;
        }
        if (arg0.getButton() == CLICAR) {
            this.mouseDrag = true;
//            this.zoomPoint = new Point2D.Float(arg0.getPoint().x, arg0.getPoint().y); //ES PODRIA UTILITZAR DRAGPOINT
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
            
            Point2D.Float dataPointFinal = this.getDataPointFromFramePoint(new Point2D.Float(e.getPoint().x, e.getPoint().y));
            Point2D.Float dataPointInicial = this.getDataPointFromFramePoint(dragPoint);
            if (dataPointFinal == null)return;
            if (dataPointInicial==null)return;

            float xrmin = FastMath.min(dataPointFinal.x, dataPointInicial.x);
            float xrmax = FastMath.max(dataPointFinal.x, dataPointInicial.x);
            log.writeNameNums("config", true, "xrangeMin xrangeMax", xrmin,xrmax);
            this.setXrangeMin(xrmin);
            this.setXrangeMax(xrmax);
            this.calcScaleFitX();
        }
        continuousRepaint=false;

    }
    

    protected void do_graphPanel_mouseWheelMoved(MouseWheelEvent e) {
        Point2D.Float p = new Point2D.Float(e.getPoint().x, e.getPoint().y);
        boolean zoomIn = (e.getWheelRotation() < 0);
        this.zoomY(zoomIn, p);
        this.repaint();
    }
    
    public boolean arePatterns(){
        return !this.getPatterns().isEmpty();
    }
    
    
    //CAL COMPROVAR QUE ESTIGUI DINS DEL RANG PRIMER I CORREGIR l'OFFSET sino torna NULL
    private Point2D.Float getFramePointFromDataPoint(DataPoint dpoint){
            return new Point2D.Float(this.getFrameXFromDataPointX(dpoint.getX()),this.getFrameYFromDataPointY(dpoint.getY()));
            
    }
    
    private float getFrameXFromDataPointX(float xdpoint){
          float xfr = ((xdpoint-this.getXrangeMin()) * this.getScalefitX()) + getGapAxisLeft() + padding;
          return xfr;    
    }
    
    private float getFrameYFromDataPointY(float ydpoint){
        float yfr = graphPanel.getHeight()-(((ydpoint-this.getYrangeMin()) * this.getScalefitY()) + getGapAxisBottom() + padding);
        return yfr;    
  }
    

    //TODO:SHOULD BE DATAPOINT??
    private Point2D.Float getDataPointFromFramePoint(Point2D.Float framePoint){
        if (isFramePointInsideGraphArea(framePoint)){
            float xdp = ((framePoint.x - getGapAxisLeft() - padding) / this.getScalefitX()) + this.getXrangeMin();
            float ydp = (((framePoint.y - getGapAxisBottom() - padding) + graphPanel.getHeight()) / this.getScalefitY()) + this.getYrangeMin();
            return new Point2D.Float(xdp,ydp);
        }else{
            return null;
        }
    }
    
    //ens diu quant en unitats de X val un pixel (ex 1 pixel es 0.01deg de 2th)
    private float getXunitsPerPixel(){
        return (this.getXrangeMax()-this.getXrangeMin())/this.getRectangleGraphArea().width;
    }
    
    //ens dira quant en unitats de Y val un pixels (ex. 1 pixel son 1000 counts)
    private float getYunitsPerPixel(){
        return (this.getYrangeMax()-this.getYrangeMin())/this.getRectangleGraphArea().height;
    }
    
    private boolean isFramePointInsideGraphArea(Point2D.Float p){
        Rectangle2D.Float r = getRectangleGraphArea();
        return r.contains(p);
    }
    
    private Rectangle2D.Float getRectangleGraphArea(){
        float xtop = getGapAxisLeft()+padding;
        float ytop = getGapAxisTop()+padding;
        return new Rectangle2D.Float(xtop,ytop,calcPlotSpaceX(),calcPlotSpaceY());
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
//        this.originFrX = getGapAxisLeft()+padding;
//        this.originFrY = graphPanel.getHeight()-getGapAxisBottom()-padding;
        
        this.calcScaleFitX();
        this.calcScaleFitY();
        
        if (!checkIfDiv() || resetAxes){
            this.autoDivLines();
        }

        this.repaint();
    }
    
    //NOMES S'HAURIA DE CRIDAR QUAN OBRIM UN PATTERN (per aixo private)
    private void autoDivLines(){
//        this.setDiv_startValX(FastMath.round(this.getXrangeMin()));
//        this.setDiv_startValY(FastMath.round(this.getYrangeMin()));
        this.setDiv_startValX(this.getXrangeMin());
        this.setDiv_startValY(this.getYrangeMin());
        //CONVENI:
        //cada 100 pixels una linia principal i cada 25 una secundaria
        //mirem l'amplada/alçada del graph area i dividim per tenir-ho en pixels        
//        Rectangle2D.Float graphr = this.getRectangleGraphArea();

        
        //ara cal veure a quan es correspon en les unitats de cada eix
        float xppix = this.getXunitsPerPixel();
        float yppix = this.getYunitsPerPixel();
        
        log.writeNameNumPairs("config", true, "xppix,yppix",xppix,yppix);
        
        this.setDiv_incXPrim(incXPrimPIXELS*xppix);
        this.setDiv_incXSec(incXSecPIXELS*xppix);
        this.setDiv_incYPrim(incYPrimPIXELS*yppix);
        this.setDiv_incYSec(incYSecPIXELS*yppix);
        
        this.txtXdiv.setText(String.valueOf(this.getDiv_incXSec()));
        this.txtYdiv.setText(String.valueOf(this.getDiv_incYSec()));
        
        log.writeNameNumPairs("config", true, "div_incXPrim, div_incXSec, div_incYPrim, div_incYSec",div_incXPrim, div_incXSec, div_incYPrim, div_incYSec);

    }
    
    //valor inicial, valor d'increment per les separacions principals (tindran número), n divisions secundaries entre principals
    private void customDivLinesX(float iniVal, float incrPrincipals, float nDivisionsSecund){
        
//        this.setXrangeMin(iniVal);
        
        float currentIni = this.getXrangeMin();
        
        this.setXrangeMin((int)this.getxMin());
        this.setDiv_startValX(this.getXrangeMin());
        this.setXrangeMin(currentIni);
//        this.calcScaleFitX();
        
        this.setDiv_incXPrim(incrPrincipals);
        this.setDiv_incXSec(incrPrincipals/nDivisionsSecund);
        
        this.txtXdiv.setText(String.valueOf(this.getDiv_incXSec()));
        
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
    
    public float getXrangeMin() {
        return xrangeMin;
    }

    public void setXrangeMin(float xrangeMin) {
        this.xrangeMin = xrangeMin;
    }

    public float getXrangeMax() {
        return xrangeMax;
    }

    public void setXrangeMax(float xrangeMax) {
        this.xrangeMax = xrangeMax;
    }

    public float getYrangeMin() {
        return yrangeMin;
    }

    public void setYrangeMin(float yrangeMin) {
        this.yrangeMin = yrangeMin;
    }

    public float getYrangeMax() {
        return yrangeMax;
    }

    public void setYrangeMax(float yrangeMax) {
        this.yrangeMax = yrangeMax;
    }

    // ajusta la imatge al panell, mostrant-la tota sencera (calcula l'scalefit inicial)
    public void fitGraph() {
        // l'ajustarem al frame mantenint la relacio, es a dir agafarem l'escala
        // més petita segons la mida del frame i la imatge
        this.resetView(false);
//        this.calcMaxMinXY();
//        this.calcScaleFitX();
//        this.calcScaleFitY();
        
    }
    
    public ArrayList<Pattern1D> getPatterns(){
        return this.patterns;
    }
    
    public int getGapAxisTop() {
        return gapAxisTop;
    }

    public void setGapAxisTop(int gapAxisTop) {
        this.gapAxisTop = gapAxisTop;
    }

    public int getGapAxisBottom() {
        return gapAxisBottom;
    }

    public void setGapAxisBottom(int gapAxisBottom) {
        this.gapAxisBottom = gapAxisBottom;
    }

    public int getGapAxisRight() {
        return gapAxisRight;
    }

    public void setGapAxisRight(int gapAxisRight) {
        this.gapAxisRight = gapAxisRight;
    }

    public int getGapAxisLeft() {
        return gapAxisLeft;
    }

    public void setGapAxisLeft(int gapAxisLeft) {
        this.gapAxisLeft = gapAxisLeft;
    }
    
    public float getxMin() {
        return xMin;
    }

    public void setxMin(float xMin) {
        this.xMin = xMin;
    }

    public float getxMax() {
        return xMax;
    }

    public void setxMax(float xMax) {
        this.xMax = xMax;
    }

    public float getyMin() {
        return yMin;
    }

    public void setyMin(float yMin) {
        this.yMin = yMin;
    }

    public float getyMax() {
        return yMax;
    }

    public void setyMax(float yMax) {
        this.yMax = yMax;
    }

    public float getScalefitX() {
        return scalefitX;
    }

    public void setScalefitX(float scalefitX) {
        this.scalefitX = scalefitX;
    }

    public float getScalefitY() {
        return scalefitY;
    }

    public void setScalefitY(float scalefitY) {
        this.scalefitY = scalefitY;
    }

    public boolean isMouseBox() {
        return mouseBox;
    }

    public void setMouseBox(boolean mouseBox) {
        this.mouseBox = mouseBox;
    }

    public float getIncX() {
        return incX;
    }

    public void setIncX(float incX) {
        this.incX = incX;
    }

    public float getIncY() {
        return incY;
    }

    public void setIncY(float incY) {
        this.incY = incY;
    }

    public float getDiv_incXPrim() {
        return div_incXPrim;
    }

    public void setDiv_incXPrim(float div_incXPrim) {
        this.div_incXPrim = div_incXPrim;
    }

    public float getDiv_incXSec() {
        return div_incXSec;
    }

    public void setDiv_incXSec(float div_incXSec) {
        this.div_incXSec = div_incXSec;
    }

    public float getDiv_incYPrim() {
        return div_incYPrim;
    }

    public void setDiv_incYPrim(float div_incYPrim) {
        this.div_incYPrim = div_incYPrim;
    }

    public float getDiv_incYSec() {
        return div_incYSec;
    }

    public void setDiv_incYSec(float div_incYSec) {
        this.div_incYSec = div_incYSec;
    }

    public float getDiv_startValX() {
        return div_startValX;
    }

    public void setDiv_startValX(float div_startValX) {
        this.div_startValX = div_startValX;
    }

    public float getDiv_startValY() {
        return div_startValY;
    }

    public void setDiv_startValY(float div_startValY) {
        this.div_startValY = div_startValY;
    }

    public String getXlabel() {
        return xlabel;
    }

    public void setXlabel(String xlabel) {
        this.xlabel = xlabel;
    }

    public String getYlabel() {
        return ylabel;
    }

    public void setYlabel(String ylabel) {
        this.ylabel = ylabel;
    }

    private void calcMaxMinX(){
        //TODO
    }
    
    private void calcMaxMinY(){
        //TODO
    }
    
    private void calcMaxMinXY(){
        Iterator<Pattern1D> itrp = getPatterns().iterator();
        float maxX = Float.MIN_VALUE;
        float minX = Float.MAX_VALUE;
        float maxY = Float.MIN_VALUE;
        float minY = Float.MAX_VALUE;
        while (itrp.hasNext()){
            Pattern1D patt = itrp.next();
            for (int i=0; i<patt.getSeries().size(); i++){
                DataSerie s = patt.getSeries().get(i);
                if (!s.isPlotThis()) continue;
                float[] MxXMnXMxYMnY = s.getPuntsMaxXMinXMaxYMinY();
                
                if (MxXMnXMxYMnY[0]>maxX) maxX = MxXMnXMxYMnY[0];
                if (MxXMnXMxYMnY[1]<minX) minX = MxXMnXMxYMnY[1];
                if (MxXMnXMxYMnY[2]>maxY) maxY = MxXMnXMxYMnY[2];
                if (MxXMnXMxYMnY[2]<minY) minY = MxXMnXMxYMnY[3];
            }
        }
        this.setxMax(maxX);
        this.setxMin(minX);
        this.setyMax(maxY);
        this.setyMin(minY);
    }
    
    //height in pixels of the plot area
    private float calcPlotSpaceY(){
        return graphPanel.getHeight()-getGapAxisTop()-getGapAxisBottom()-2*padding;
    }
    //width in pixels of the plot area
    private float calcPlotSpaceX(){
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
    private void zoomY(boolean zoomIn, Point2D.Float centre) {
        Point2D.Float dpcentre = this.getDataPointFromFramePoint(centre); // miro a quin punt de dades estem fent zoom
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

    private void zoomX(boolean zoomIn, float inc) {
//        Point2D.Float dpcentre = this.getDataPointFromFramePoint(centre); // miro a quin punt de dades estem fent zoom
//        if (dpcentre == null)return;
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
    
    private void scrollX(float inc) {
//      Point2D.Float dpcentre = this.getDataPointFromFramePoint(centre); // miro a quin punt de dades estem fent zoom
//      if (dpcentre == null)return;
        this.setXrangeMin(this.getXrangeMin()+(inc/scalefitX));
        this.setXrangeMax(this.getXrangeMax()+(inc/scalefitX));
          // TODO: posem maxim?
      calcScaleFitX();
    }
    
    private void zoomXY(boolean zoomIn, Point2D.Float centre) {
        //TODO
    }


    
//  ------------------------------------ PANELL DE DIBUIX
    private class Plot1d extends JPanel {

        private static final long serialVersionUID = 1L;

        private int panelW, panelH;
//        private float xScale, yScale;
        
        public Plot1d(){
            super();
        }
        
        private void drawAxes(Graphics2D g1){
            log.fine("drawAxes entered");

            //provem de fer linia a 60 pixels de l'esquerra i a 60 pixels de baix (40 i 40 de dalt i a la dreta com a marges)
            
            Point2D.Float vytop = new Point2D.Float(getGapAxisLeft()+padding,getGapAxisTop()+padding);
            Point2D.Float vybot = new Point2D.Float(getGapAxisLeft()+padding,panelH-getGapAxisBottom()-padding);
            Point2D.Float vxleft = vybot;
            Point2D.Float vxright = new Point2D.Float(panelW-getGapAxisRight()-padding,panelH-getGapAxisBottom()-padding);
            
            log.writeNameNums("fine", true, "(axes) vy vx", vytop.x,vytop.y,vybot.x,vybot.y,vxleft.x,vxleft.y,vxright.x,vxright.y);
            
            g1.setColor(Color.BLACK);
            BasicStroke stroke = new BasicStroke(1.0f);
            g1.setStroke(stroke);
            
            Line2D.Float ordenada = new Line2D.Float(vytop,vybot);  //Y axis vertical
            Line2D.Float abcissa = new Line2D.Float(vxleft, vxright);  //X axis horizontal
            
            g1.draw(ordenada);
            g1.draw(abcissa);
            
//            g1.drawLine(FastMath.round(vytop.x), FastMath.round(vytop.y), FastMath.round(vybot.x), FastMath.round(vybot.y));
//            g1.drawLine(FastMath.round(vxleft.x), FastMath.round(vxleft.y), FastMath.round(vxright.x), FastMath.round(vxright.y));
            
            
            //PINTEM ELS TITOLS DELS EIXOS
            Font font = g1.getFont();
            FontRenderContext frc = g1.getFontRenderContext();
            
            
            // Y-axis (ordinate) label.
            String s = getYlabel();
            float sw = (float)font.getStringBounds(s, frc).getWidth();
            float sh =  (float)font.getStringBounds(s, frc).getHeight();
            float sx = AxisLabelsPadding;
            float sy = sh + AxisLabelsPadding;
            if (YLabelVertical){
                sy = (panelH - sw)/2;
                sx = getGapAxisLeft()/2;
                AffineTransform orig = g1.getTransform();
                g1.rotate(-Math.PI/2,sx,sy);
                g1.drawString(s,sx,sy);
                g1.setTransform(orig);
            }else{
                //el posem sobre l'eix en horitzontal
                g1.drawString(s,sx,sy);
            }
            
            
            // X-axis (abcissa) label.
            s = getXlabel();
//            sy = panelH - getGapAxisBottom()/2;
            sy = panelH - AxisLabelsPadding;
            sw = (float)font.getStringBounds(s, frc).getWidth();
            sx = (panelW - sw)/2;
            g1.drawString(s, sx, sy);
            
            
            
            // **** linies divisio eixos
            if (!checkIfDiv())return;
            if (fixAxes) autoDivLines(); //es pot fer mes eficient sense fer-ho cada cop
            //TODO
            //---eix X
            //Per tots els punts les coordenades Y seran les mateixes
            float yiniPrim = panelH-getGapAxisBottom()-padding - (div_PrimPixSize/2.f); 
            float yfinPrim = panelH-getGapAxisBottom()-padding + (div_PrimPixSize/2.f);
//            float yLabel = yfinPrim + AxisLabelsPadding; //TODO de moment provo aixo
            //ara dibuixem les Primaries i posem els labels
            float xval = getDiv_startValX();
            while (xval <= getXrangeMax()){
                if (xval < getXrangeMin()){
                    xval = xval + div_incXPrim;
                    continue;
                }
                float xvalPix = getFrameXFromDataPointX(xval);
                Line2D.Float l = new Line2D.Float(xvalPix,yiniPrim,xvalPix,yfinPrim);
                g1.draw(l);
                //ara el label sota la linia
                s = String.format("%.3f", xval);
                sw = (float)font.getStringBounds(s, frc).getWidth();
                sh = (float)font.getStringBounds(s, frc).getHeight();
                float xLabel = xvalPix - sw/2f; //el posem centrat a la linia
                float yLabel = yfinPrim + AxisLabelsPadding + sh;
                g1.drawString(s, xLabel, yLabel);
                xval = xval + div_incXPrim;

                if(xval> (int)(1+getxMax()))break; //provem de posar-ho aqui perque no dibuixi mes enllà

            }
            
            //ara les secundaries
            float yiniSec = panelH-getGapAxisBottom()-padding - (div_SecPixSize/2.f); 
            float yfinSec = panelH-getGapAxisBottom()-padding + (div_SecPixSize/2.f);
            xval = getDiv_startValX();
            while (xval <= getXrangeMax()){
                if (xval < getXrangeMin()){
                    xval = xval + div_incXSec;
                    continue;
                }
                float xvalPix = getFrameXFromDataPointX(xval);
                Line2D.Float l = new Line2D.Float(xvalPix,yiniSec,xvalPix,yfinSec);
                g1.draw(l);
                xval = xval + div_incXSec;
                
                if(xval> (int)(1+getxMax()))break; //provem de posar-ho aqui perque no dibuixi mes enllà
            }
            
            //---eix Y
            //Per tots els punts les coordenades Y seran les mateixes
            float xiniPrim = getGapAxisLeft()+padding - (div_PrimPixSize/2.f); 
            float xfinPrim = getGapAxisLeft()+padding + (div_PrimPixSize/2.f);
//            float yLabel = yfinPrim + AxisLabelsPadding; //TODO de moment provo aixo
            //ara dibuixem les Primaries i posem els labels
            float yval = getDiv_startValY();
            while (yval <= getYrangeMax()){
                if (yval < getYrangeMin()){
                    yval = yval + div_incYPrim;
                    continue;
                }
                float yvalPix = getFrameYFromDataPointY(yval);
                Line2D.Float l = new Line2D.Float(xiniPrim, yvalPix, xfinPrim, yvalPix);
                g1.draw(l);
                //ara el label a l'esquerra de la linia
                s = String.format("%.1f", yval);
                sw = (float)font.getStringBounds(s, frc).getWidth();
                sh = (float)font.getStringBounds(s, frc).getHeight();
                float xLabel = xiniPrim - AxisLabelsPadding - sw; 
                float yLabel = yvalPix + sh/2f; //el posem centrat a la linia
                g1.drawString(s, xLabel, yLabel);
                yval = yval + div_incYPrim;
            }
            
            //ara les secundaries
            float xiniSec = getGapAxisLeft()+padding - (div_SecPixSize/2.f); 
            float xfinSec = getGapAxisLeft()+padding + (div_SecPixSize/2.f);
            yval = getDiv_startValY();
            while (yval <= getYrangeMax()){
                if (yval < getYrangeMin()){
                    yval = yval + div_incYSec;
                    continue;
                }
                float yvalPix = getFrameYFromDataPointY(yval);
                Line2D.Float l = new Line2D.Float(xiniSec,yvalPix,xfinSec,yvalPix);
                g1.draw(l);
                yval = yval + div_incYSec;
            }
            
            
            //numeracio eixos
            //TODO
            
            //labels eixos

            
            log.fine("drawAxes exit");

        }
        
        private void drawPatternLine(Graphics2D g1, DataSerie serie){
            //TODO color, style, etc...
            log.fine("drawPatternLine entered");
            g1.setColor(Color.BLUE);
            BasicStroke stroke = new BasicStroke(3f);
            g1.setStroke(stroke);
            for (int i = 0; i < serie.getSeriePoints().size(); i++){
                
                //PRIMER DIBUIXEM TOTA LA LINIA --> ATENCIO AMB ELS PUNTS QUE ESTAN FORA!!
                Point2D.Float p1 = getFramePointFromDataPoint(serie.getSeriePoints().get(i));
                if (i==(serie.getSeriePoints().size()-1)){ //p1 es l'ultim punt, ja podem sortir del for
                    break;
                }
                Point2D.Float p2 = getFramePointFromDataPoint(serie.getSeriePoints().get(i+1));
                
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
                        Point2D.Float[] p = getIntersectionPoint(new Line2D.Float(p1,p2),getRectangleGraphArea());
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
                        Point2D.Float[] p = getIntersectionPoint(new Line2D.Float(p1,p2),getRectangleGraphArea());
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
                Line2D.Float l = new Line2D.Float(p1.x,p1.y,p2.x,p2.y);
                g1.draw(l);
                
            }
            log.fine("drawPatternLine exit");
        }
    
    //separo linia i punts per si volem canviar l'ordre de dibuix
    private void drawPatternPoints(Graphics2D g1, DataSerie serie){
        //TODO color, style, etc...
        log.fine("drawPatternPoints entered");
        for (int i = 0; i < serie.getSeriePoints().size(); i++){
            g1.setColor(Color.RED);
            BasicStroke stroke = new BasicStroke(2.0f);
            g1.setStroke(stroke);
            Point2D.Float p1 = getFramePointFromDataPoint(serie.getSeriePoints().get(i));
            if (isFramePointInsideGraphArea(p1)){
                float radiPunt = serie.getMarkerSize()/2.f;
                g1.drawOval(FastMath.round(p1.x-radiPunt), FastMath.round(p1.y-radiPunt), FastMath.round(serie.getMarkerSize()), FastMath.round(serie.getMarkerSize()));
                
            }
        }
        log.fine("drawPatternPoints exit");
    }
        
        @Override
        protected void paintComponent(Graphics g) {
            log.fine("paintComponent entered");
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            
            g2.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON)); // perque es vegin mes suaus...
            
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
                //calcDefaultIncXY

                //1st draw axes
                this.drawAxes(g1);

                Iterator<Pattern1D> itrp = getPatterns().iterator();
                while (itrp.hasNext()){
                    Pattern1D patt = itrp.next();
                    for (int i=0; i<patt.getSeries().size(); i++){
                        drawPatternLine(g1,patt.getSeries().get(i)); 
                        drawPatternPoints(g1,patt.getSeries().get(i));
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
                
                log.debug(Boolean.toString(continuousRepaint));
                if(continuousRepaint)this.repaint();
            }
            log.fine("paintComponent exited");
        }
    }
    
    public Point2D.Float getIntersectionPoint(Line2D.Float line1, Line2D.Double line2) {
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
            return new Point2D.Float(
              (float)(px+z*rx), (float)(py+z*ry));
          }
     } // end intersection line-line
    
    public Point2D.Float[] getIntersectionPoint(Line2D.Float line, Rectangle2D rectangle) {

        Point2D.Float[] p = new Point2D.Float[4];

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
    
    
    protected void do_chckbxAutodiv_itemStateChanged(ItemEvent e) {
        this.autoDiv=chckbxAutodiv.isSelected();
    }
    protected void do_chckbxFixedAxis_itemStateChanged(ItemEvent e) {
        this.fixAxes=chckbxFixedAxis.isSelected();
    }
    
    protected void do_btnApply_actionPerformed(ActionEvent arg0) {
        this.setXrangeMin(Float.parseFloat(txtXmin.getText()));
        this.setXrangeMax(Float.parseFloat(txtXmax.getText()));
        this.setYrangeMin(Float.parseFloat(txtYmin.getText()));
        this.setYrangeMax(Float.parseFloat(txtYmax.getText()));
        this.calcScaleFitX();
        this.calcScaleFitY();
        this.repaint();
    }
    protected void do_btnApplydiv_actionPerformed(ActionEvent e) {
        this.customDivLinesX(Float.parseFloat(txtXmin.getText()), Float.parseFloat(txtXdiv.getText()), Float.parseFloat(txtNdivx.getText()));
        this.repaint();
    }
}

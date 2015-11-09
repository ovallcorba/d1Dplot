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
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JPanel;

import org.apache.commons.math3.util.FastMath;

import com.vava33.jutils.VavaLogger;

import vava33.d1dplot.auxi.DataPoint;
import vava33.d1dplot.auxi.DataSerie;
import vava33.d1dplot.auxi.Pattern1D;
import net.miginfocom.swing.MigLayout;

public class PlotPanel extends JPanel {

    private ArrayList<Pattern1D> patterns; //data to plot (series inside pattern1d)

    private int gapAxisTop = 40;
    private int gapAxisBottom = 60;
    private int gapAxisRight = 60;
    private int gapAxisLeft = 60;
    private static int padding = 20;
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
    
    private String xlabel = "2theta";
    private String ylabel = "Intensity";
    private Plot1d graphPanel;
    
    // DEFINICIO BUTONS DEL MOUSE
    private static int MOURE = MouseEvent.BUTTON2;
    private static int CLICAR = MouseEvent.BUTTON1;
    private static int ZOOM_BORRAR = MouseEvent.BUTTON3;
    //parametres interaccio/contrast
    private static float incZoom = 0.05f;
    private boolean mouseBox = false;
    private boolean mouseDrag = false;
    private boolean mouseZoom = false;
    private float originDPx, originDPy; //a quin datapoint correspon l'origen dels eixos en un zoom donat (inicial 0,0)
    private int originFrX, originFrY;  //a quin pixel del frame correspon l'origen dels eixos, sempre serà el mateix (en funció del height del frame)
    private Point2D.Float zoomPoint, dragPoint;
    
    
    private static VavaLogger log = D1Dplot_global.log;

    /**
     * Create the panel.
     */
    public PlotPanel() {
        setLayout(new MigLayout("", "[grow]", "[][grow]"));
        
        JPanel buttons_panel = new JPanel();
        add(buttons_panel, "cell 0 0,grow");
        buttons_panel.setLayout(new MigLayout("", "[]", "[]"));
        
        graphPanel = new Plot1d();
        
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
    }
    
    protected void do_graphPanel_mouseDragged(MouseEvent e) {
        // TODO
    }

    protected void do_graphPanel_mouseMoved(MouseEvent e) {
        // TODO
    }

    
    // Identificar el botó i segons quin sigui moure o fer zoom
    protected void do_graphPanel_mousePressed(MouseEvent arg0) {
        this.dragPoint = new Point2D.Float(arg0.getPoint().x, arg0.getPoint().y);

        if (arg0.getButton() == MOURE) {
            this.mouseDrag = true;
        }
        if (arg0.getButton() == ZOOM_BORRAR) {
            this.zoomPoint = new Point2D.Float(arg0.getPoint().x, arg0.getPoint().y);
            this.mouseZoom = true;
        }
        if (arg0.getButton() == CLICAR) {
            // TODO
        }

    }

    protected void do_graphPanel_mouseReleased(MouseEvent e) {
        if (e.getButton() == MOURE)
            this.mouseDrag = false;
        if (e.getButton() == ZOOM_BORRAR)
            this.mouseZoom = false;

        if (!arePatterns())return;
    }
    

    protected void do_graphPanel_mouseWheelMoved(MouseWheelEvent e) {
        Point2D.Float p = new Point2D.Float(e.getPoint().x, e.getPoint().y);
        boolean zoomIn = (e.getWheelRotation() < 0);
        this.zoom(zoomIn, p);
    }
    
    public boolean arePatterns(){
        return !this.getPatterns().isEmpty();
    }
    
    public Point2D.Float getFramePointFromDataPoint(DataPoint dpoint){
        return new Point2D.Float((dpoint.getX()*this.getScalefitX()+originFrX),(-dpoint.getY()*this.getScalefitY()+originFrY));
    }

    //TODO:SHOULD BE DATAPOINT??
    public Point2D.Float getDataPointFromFramePoint(Point2D.Float framePoint){
        return new Point2D.Float(((framePoint.x - originFrX) / scalefitX), (-(framePoint.y - originFrY) / scalefitY));
    }
    
    public void resetView() {
        this.calcMaxMinXY();
        this.setXrangeMax(this.getxMax());
        this.setXrangeMin(this.getxMin());
        this.setYrangeMax(this.getyMax());
        this.setYrangeMin(this.getyMin());
        this.originFrX = getGapAxisLeft();
        this.originFrY = graphPanel.getHeight()-getGapAxisBottom();
        
        this.calcScaleFitX();
        this.calcScaleFitY();

        this.repaint();
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
        this.resetView();
        this.calcMaxMinXY();
        this.calcScaleFitX();
        this.calcScaleFitY();

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

    public void calcMaxMinX(){
        
    }
    
    public void calcMaxMinY(){
        
    }
    
    public void calcMaxMinXY(){
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
    
    public void calcScaleFitY(){
        float plotSpace = graphPanel.getHeight()-getGapAxisTop()-getGapAxisBottom()-padding;
        scalefitY = plotSpace/(this.getYrangeMax()-this.getYrangeMin());
    }
    
    public void calcScaleFitX(){
        float plotSpace = graphPanel.getWidth()-getGapAxisLeft()-getGapAxisRight()-padding;
        scalefitX = plotSpace/(this.getXrangeMax()-this.getXrangeMin());
    }
    
    // al fer zoom es canviara l'origen i l'escala de la imatge
    public void zoom(boolean zoomIn, Point2D.Float centre) {
        Point2D.Float mousePosition = new Point2D.Float(centre.x, centre.y);
        Point2D.Float dpcentre = this.getDataPointFromFramePoint(centre); // miro a quin punt de dades estem fent zoom

        // aplico el zoom
        if (zoomIn) {
            scalefitX = scalefitX + (incZoom * scalefitX);
            scalefitY = scalefitY + (incZoom * scalefitY);
            // posem maxim?
            if (scalefitX >= 25.f)scalefitX = 25.f;
            if (scalefitY >= 25.f)scalefitY = 25.f;
        } else {
            scalefitX = scalefitX - (incZoom * scalefitX);
            scalefitY = scalefitY - (incZoom * scalefitY);
            if (scalefitX <= 0.10) scalefitX = 0.10f;
            if (scalefitY <= 0.10) scalefitY = 0.10f;
        }

        // ara el pixel ja no està al mateix lloc, mirem a quin punt del frame
        // està (en aquest nou scalefit)
        centre = this.getFramePointFromDataPoint(new DataPoint(dpcentre.x,dpcentre.y,0));    

        // ara tenim el punt del jframe que ha de quedar on tenim el mouse
        // apuntant, per tant hem de moure l'origen de
        // la imatge conforme això (vector nouCentre-mousePosition)
        
        originFrX = originFrX + FastMath.round(mousePosition.x - centre.x);
        originFrY = originFrY + FastMath.round(mousePosition.y - centre.y);

        this.repaint();
    }


    
//  ------------------------------------ PANELL DE DIBUIX
    public class Plot1d extends JPanel {

        private static final long serialVersionUID = 1L;

        public Plot1d(){
            super();
        }
        
        private void drawAxes(Graphics2D g1){
            log.debug("drawAxes entered");

            int w = this.getWidth();
            int h = this.getHeight();
            //provem de fer linia a 60 pixels de l'esquerra i a 60 pixels de baix (40 i 40 de dalt i a la dreta com a marges)
            
            Point2D.Float vytop = new Point2D.Float(getGapAxisLeft(),getGapAxisTop());
            Point2D.Float vybot = new Point2D.Float(getGapAxisLeft(),h-getGapAxisBottom());
            Point2D.Float vxleft = vybot;
            Point2D.Float vxright = new Point2D.Float(w-getGapAxisRight(),h-getGapAxisBottom());
            
            log.writeNameNums("CONFIG", true, "(axes) vy vx", vytop.x,vytop.y,vybot.x,vybot.y,vxleft.x,vxleft.y,vxright.x,vxright.y);
            
            g1.setColor(Color.BLACK);
            BasicStroke stroke = new BasicStroke(1.0f);
            g1.setStroke(stroke);
            g1.drawLine(FastMath.round(vytop.x), FastMath.round(vytop.y), FastMath.round(vybot.x), FastMath.round(vybot.y));
            g1.drawLine(FastMath.round(vxleft.x), FastMath.round(vxleft.y), FastMath.round(vxright.x), FastMath.round(vxright.y));
            
            //linies divisio eixos
            //TODO
            
            //numeracio eixos
            //TODO
            
            //labels eixos
            // Y-axis (ordinate) label.
            Font font = g1.getFont();
            FontRenderContext frc = g1.getFontRenderContext();
            String s = getYlabel();
            float sw = (float)font.getStringBounds(s, frc).getWidth();
            float sy = (h - sw)/2;
            float sx = getGapAxisLeft()/2;
            AffineTransform orig = g1.getTransform();
            g1.rotate(-Math.PI/2,sx,sy);
            g1.drawString(s,sx,sy);
            g1.setTransform(orig);
            
            // X-axis (abcissa) label.
            s = getXlabel();
            sy = h - getGapAxisBottom()/2;
            sw = (float)font.getStringBounds(s, frc).getWidth();
            sx = (w - sw)/2;
            g1.drawString(s, sx, sy);
            
            log.debug("drawAxes exit");

        }
        
        private void drawPattern(Graphics2D g1, DataSerie serie){
            //TODO color, style, etc...
            log.debug("drawPattern entered");
            for (int i = 0; i < serie.getSeriePoints().size(); i++){
                Point2D.Float p1 = getFramePointFromDataPoint(serie.getSeriePoints().get(i));
                Point2D.Float p2 = null;
                //dibuixem primer marker p1
                g1.setColor(Color.RED);
                BasicStroke stroke = new BasicStroke(2.0f);
                g1.setStroke(stroke);
                float radiPunt = serie.getMarkerSize()/2.f;
                g1.drawOval(FastMath.round(p1.x-radiPunt), FastMath.round(p1.y-radiPunt), FastMath.round(serie.getMarkerSize()), FastMath.round(serie.getMarkerSize()));
                //ara la linia //TODO HAURIA DE SER EL REVES PERQUE EL PUNT NO QUEDES DARRERA
                g1.setColor(Color.BLUE);
                stroke = new BasicStroke(3f);
                g1.setStroke(stroke);
                if (i==(serie.getSeriePoints().size()-1)){
                    break; //ultim punt
                }else{
                    p2 = getFramePointFromDataPoint(serie.getSeriePoints().get(i+1));
                }
                g1.drawLine(FastMath.round(p1.x), FastMath.round(p1.y), FastMath.round(p2.x), FastMath.round(p2.y));
                
                //debug
                if (i<10){
                    log.writeNameNums("CONFIG", true, "(patt) p1 p2", p1.x,p1.y,p2.x,p2.y);
                }
            }
            log.debug("drawPattern exit");
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            log.debug("paintComponent entered");
            super.paintComponent(g);


            Graphics2D g2 = (Graphics2D) g;
            
            final Graphics2D g1 = (Graphics2D) g2.create();

            // dibuixem els cercles
            g2.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON)); // perque es vegin mes suaus...
            
            if (getPatterns().size() > 0) {
                
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
                        drawPattern(g1,patt.getSeries().get(i)); //TODO: el factor ha de ser el mateix per tots, fixat per PlotPanel i que es pugui actualitzar al fer zoom
                    }
                }
                g1.dispose();
                g2.dispose();
                
                this.repaint();
            }
            log.debug("paintComponent exited");
        }
    }
    
}

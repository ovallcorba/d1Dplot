package vava33.d1dplot;

/**
 * D1Dplot
 * 
 * Panel to plot multiple 1D patterns in a 2D view
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JPanel;

import com.vava33.jutils.VavaLogger;

import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;

import org.apache.commons.math3.util.FastMath;

import vava33.d1dplot.auxi.DataSerie;

import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.JCheckBox;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class PlotPanel2D extends JPanel {

    private static VavaLogger log = D1Dplot_global.getVavaLogger(PlotPanel2D.class.getName());
    private dades2d panelImatge;
    private BufferedImage image;
    private BufferedImage subimage;
    private ArrayList<DataSerie> toPaint;
    
    // DEFINICIO BUTONS DEL MOUSE
    private static int MOURE = MouseEvent.BUTTON2;
    private static int CLICAR = MouseEvent.BUTTON1;
    private static int ZOOM_BORRAR = MouseEvent.BUTTON3;
    
    private static float incZoom = 0.05f;
    private static float maxScaleFit=40.f;
    private static float minScaleFit=0.10f;
    private static int contrast_fun=0;
    private static float factorAutoContrast = 20.0f;
    
    //l'SCALEFIT Y SEMPRE SERA PER OMPLIR LA FINESTRA? I EL X ES EL QUE ES MOURÀ?
    private float scalefitX=-1;
    private float scalefitY=-1;
//    boolean fit = true;
    private int originX, originY;
    private Point2D.Float zoomPoint, dragPoint;
    private Point2D.Float currentMousePoint;

    private boolean mouseDrag = false;
    private boolean mouseZoom = false;
    
    
    int minValSlider;
    int valSlider;
    int maxI;
    int minI;
    private JSlider slider_contrast;
    private JCheckBox chckbxColor;
    
    /**
     * Create the panel.
     */
    public PlotPanel2D() {
        setLayout(new MigLayout("", "[][grow][]", "[][grow]"));
        
        JLabel lblAaa = new JLabel("aaa");
        add(lblAaa, "cell 0 0");

        this.setPanelImatge(new dades2d());
        this.getPanelImatge().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        this.getPanelImatge().addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent arg0) {
                do_panelImatge_mouseWheelMoved(arg0);
            }
        });
        this.getPanelImatge().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent arg0) {
                do_panelImatge_mousePressed(arg0);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                do_panelImatge_mouseReleased(e);
            }
        });
        this.getPanelImatge().addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent arg0) {
                do_panelImatge_mouseDragged(arg0);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                do_panelImatge_mouseMoved(e);
            }
        });
        add(this.getPanelImatge(), "cell 0 1 2 1,grow");
        
        chckbxColor = new JCheckBox("color");
        add(chckbxColor, "cell 1 0 2 1,alignx right");
        
        slider_contrast = new JSlider();
        slider_contrast.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                do_slider_contrast_stateChanged(arg0);
            }
        });
        slider_contrast.setValue(4000);
        slider_contrast.setMaximum(10000);
        slider_contrast.setOrientation(SwingConstants.VERTICAL);
        add(slider_contrast, "cell 2 1,growy");
    
        this.resetView();
    }

    protected void do_panelImatge_mouseWheelMoved(MouseWheelEvent e) {
        Point2D.Float p = new Point2D.Float(e.getPoint().x, e.getPoint().y);
        boolean zoomIn = (e.getWheelRotation() < 0);
        this.zoom(zoomIn, p); //ja fa actualitzar
    }
    
    
    // Identificar el bot� i segons quin sigui moure o fer zoom
    protected void do_panelImatge_mousePressed(MouseEvent arg0) {
        this.dragPoint = new Point2D.Float(arg0.getPoint().x, arg0.getPoint().y);

        if (arg0.getButton() == MOURE) {
            this.mouseDrag = true;
        }
        if (arg0.getButton() == ZOOM_BORRAR) {
            this.zoomPoint = new Point2D.Float(arg0.getPoint().x, arg0.getPoint().y);
            this.mouseZoom = true;
        }
//        if (arg0.getButton() == CLICAR) {
//            //res de moment
//        }
        actualitzarVista();
    }

    protected void do_panelImatge_mouseReleased(MouseEvent e) {
        if (e.getButton() == MOURE)
            this.mouseDrag = false;
        if (e.getButton() == ZOOM_BORRAR)
            this.mouseZoom = false;
//        if (e.getButton() == CLICAR)
        actualitzarVista();
    }
    
    protected void do_panelImatge_mouseDragged(MouseEvent e) {
        if (this.mouseDrag == true) { //& this.toPaint!=null cal?
            Point2D.Float p = new Point2D.Float(e.getPoint().x, e.getPoint().y);
            float incX, incY;
            // agafem el dragpoint i l'actualitzem
            incX = (p.x - dragPoint.x);
            incY = (p.y - dragPoint.y);
            this.dragPoint = p;
            this.moveOrigin(incX, incY, true);    
            log.writeNameNumPairs("fine", true, "fX,fY,imX,imY,scfitX,scfitY,orX,orY,panw,panh", e.getPoint().x,e.getPoint().y,p.x,p.y,scalefitX,scalefitY,originX,originY,getPanelImatge().getWidth(),getPanelImatge().getHeight());
        }
        if (this.mouseZoom == true) {
            Point2D.Float p = new Point2D.Float(e.getPoint().x, e.getPoint().y);
            float incY;
            incY = p.y - dragPoint.y;
            this.dragPoint = p;
            boolean zoomIn = (incY < 0);
            this.zoom(zoomIn, zoomPoint);
        }

        actualitzarVista();

    }
    
    protected void do_panelImatge_mouseMoved(MouseEvent e) {
        // TODO
        
    }


    // es mou l'origen a traves d'un increment de les coordenades
    public void moveOrigin(float incX, float incY, boolean repaint) {
        // assignem un nou origen de la imatge amb un increment a les coordenades anteriors
        //  (util per moure'l fen drag del mouse)
        log.writeNameNums("fine", true, "incX,incY", incX,incY);
        originX = originX + FastMath.round(incX);
        originY = originY + FastMath.round(incY);
        if (repaint) {
            this.actualitzarVista();
        }
    }
    
    // al fer zoom es canviara l'origen i l'escala de la imatge
    public void zoom(boolean zoomIn, Point2D.Float centre) {
        Point2D.Float mousePosition = new Point2D.Float(centre.x, centre.y);
        centre = getPixel(centre); // miro a quin pixel estem fent zoom

        // aplico el zoom
        if (zoomIn) {
            scalefitX = scalefitX + (incZoom * scalefitX);
            scalefitY = scalefitY + (incZoom * scalefitY);
            // posem maxim?
//            if (scalefitX >= maxScaleFit)
//                scalefitX = maxScaleFit;
//            if (scalefitY >= maxScaleFit)
//                scalefitY = maxScaleFit;
        } else {
            scalefitX = scalefitX - (incZoom * scalefitX);
            scalefitY = scalefitY - (incZoom * scalefitY);
//            if (scalefitX <= minScaleFit)
//                scalefitX = minScaleFit;
//            if (scalefitY <= minScaleFit)
//                scalefitY = minScaleFit;
        }

        // ara el pixel ja no est� al mateix lloc, mirem a quin punt del frame
        // est� (en aquest nou scalefit)
        centre = getFramePointFromPixel(centre);

        // ara tenim el punt del jframe que ha de quedar on tenim el mouse
        // apuntant, per tant hem de moure l'origen de
        // la imatge conforme aix� (vector nouCentre-mousePosition)
        originX = originX + FastMath.round(mousePosition.x - centre.x);
        originY = originY + FastMath.round(mousePosition.y - centre.y);
        this.actualitzarVista();
    }
    
    //S'HA D'ADAPTAR
    public void setImagePatts(ArrayList<DataSerie> dss) {
        if(dss == null)return;
        if(dss.size()==0)return;

        this.toPaint = dss;

//        log.debug("slider value before (max,min)= "+this.slider_contrast.getValue()+" ("+this.slider_contrast.getMaximum()+","+this.slider_contrast.getMinimum()+")");
//        if(this.chckbxAuto.isSelected()){
//            this.setSliderOptimumValues();
//        }else{
//            this.setSliderContrastValues(this.patt2D.getMinI(), this.patt2D.getMaxI(), -1);
//        }
//        log.debug("slider value after (max,min)= "+this.slider_contrast.getValue()+" ("+this.slider_contrast.getMaximum()+","+this.slider_contrast.getMinimum()+")");
//        this.loadContrastValues();
        this.pintaImatge();
        this.actualitzarVista();
    }
    
    protected void do_slider_contrast_stateChanged(ChangeEvent arg0) {
        this.pintaImatge(); //ja conte actualitzar vista
   }
    
    protected void pintaImatge() {
        log.debug("ImagePanel pintaImatge called");
        
        if (toPaint == null)return;
        if (toPaint.size()==0)return;

        int dimx = toPaint.get(0).getNpoints();
        int type = BufferedImage.TYPE_INT_ARGB;
        BufferedImage im = new BufferedImage(dimx, toPaint.size(), type);

        
        minValSlider = this.slider_contrast.getMinimum();
        valSlider = this.slider_contrast.getValue();
        log.debug("ValSlider="+valSlider);

//        maxI = patt2D.getMaxI();
//        minI = patt2D.getMinI();
        minI = 0;
        maxI = 20000;
        
        for (int i = 0; i < im.getHeight(); i++) { // per cada fila (Y)
            for (int j = 0; j < im.getWidth(); j++) { // per cada columna (X)
                
                //calcular el color...
                //hem de mirar quina serie (i) quin punt (j)
                im.setRGB(j, i, this.getColorOfAPixel(j, i).getRGB());
            }
        }
        log.debug("ImagePanel pintaImatge updateImage call");
        this.updateImage(im);
    }
    
    private Color getColorOfAPixel(int jy, int ix){
        Color col;
        if (this.isColor()) {
            // pintem en color
            col = intensityColor(toPaint.get(ix).getPoint(jy).getY(), maxI,minI, minValSlider,valSlider);
        } else {
            // pintem en BW
            col = intensityBW(toPaint.get(ix).getPoint(jy).getY(), maxI,minI,minValSlider,valSlider);
        }
        return col;
    }
    
    /*
     * maxInt,minInt son maxim i minim d'intensitat de la imatge
     * minVal,maxVal corresponen a l'slide. MAX es el valor actual assenyalat per l'slide.
     * grafiques RGB entre minval i maxval amb punt inflexio a (maxval-minval)/2
     * intensitat normalitzada entre minval i maxval
     */
    protected Color intensityColor(double intensity, int maxInt, int minInt, int minVal, int maxVal) {
                
        if (intensity < 0) {// es mascara, el pintem magenta
            return new Color(255, 0, 255);
        }
        if (intensity == 0) {
            return new Color(0,0,0);
        } // poso el 0 absolut com a negre

        //LIMITS
        if(intensity>=maxVal){
            return new Color(255,0,0);
        }         
        if(intensity<=minVal){
            return new Color(0,0,255);
        }

        
        float red = 0.0f;
        float green = 0.0f;
        float blue = 0.0f;
        
        //dins rang minVal - maxVal
        
        //vermell recta de 0 a 1
        float x1 = minVal;
        float y1 = 0.f;
        float x2 = maxVal;// evitem diviso zero
        float y2 = 1.f;
        red = (float) (((y2 - y1) / (x2 - x1)) * intensity + y1 - ((y2 - y1) / (x2 - x1)) * x1);

        //BLUE recta de 1 a 0
        x1 = minVal;
        y1 = 1.f;
        x2 = maxVal;// evitem diviso zero
        y2 = 0.f;
        blue = (float) (((y2 - y1) / (x2 - x1)) * intensity + y1 - ((y2 - y1) / (x2 - x1)) * x1);

        //green fa un pic a half (equvalent a aquesta operacio)
        green = blue*red*4.f;
            
        Color c = new Color(255, 0, 255);
        try{
            c = new Color(red, green, blue);
        }catch(Exception e){
            e.printStackTrace();
            log.debug("invalid color");
        }
        return c;
    }
    
    //valor interpolat sobre una recta (fun=0) o una parabola (fun=1)
    protected Color intensityBW(double intensity, int maxInt, int minInt,int minVal, int maxVal) {
        
        if (intensity < 0) {// es mascara, el pintem magenta
            return new Color(255, 0, 255);
        }

        float ccomponent=-1.f;

        switch(contrast_fun){
        case 0:
            // el valor s'interpolara sobre la recta (sliderMin,0) a (sliderMax,1)
            //interpolem
            float x1 = minVal; // evitem diviso zero
            float y1 = 0.0f;
            float x2 = maxVal;
            float y2 = 1.0f;
            ccomponent = (float) (((y2 - y1) / (x2 - x1)) * intensity + y1 - ((y2 - y1) / (x2 - x1)) * x1);
            break;
        case 1:
            // el valor s'interpolara sobre una quadr�tica y=ax2 (centrat a 0,0)
            //nomes varia el parametre a
            float a=(1.f/(maxVal*maxVal));
            ccomponent = (float) (a*(intensity*intensity));
            break;
        case 2:
            // el valor s'interpolara sobre una quadr�tica cap avall y=ax2 + 1 (centrat a 0,1)
            //nomes varia el parametre a
            a=(-1.f/(maxVal*maxVal));
            ccomponent = (float)(a*(intensity*intensity)+1);
            break;
        }

        if(ccomponent == -1.f){return new Color(255, 0, 255);}
        
        if (ccomponent < 0) {
            ccomponent = 0;
        }
        if (ccomponent > 1) {
            ccomponent = 1;
        }
        
        return new Color(ccomponent, ccomponent, ccomponent);
    }
    
    public boolean isColor() {
        return chckbxColor.isSelected();
    }
    
    public void resetView() {
        this.originX = 0;
        this.originY = 0;
        scalefitX = 0.0f;
        scalefitY = 0.0f;
        this.actualitzarVista();
    }
    
    public BufferedImage getImage() {
        return image;
    }
    

    public int getOriginX() {
        return originX;
    }

    public int getOriginY() {
        return originY;
    }


    public float getScalefitX() {
        return scalefitX;
    }
    public float getScalefitY() {
        return scalefitY;
    }

    public BufferedImage getSubimage() {
        return subimage;
    }

    public void updateImage(BufferedImage i) {
        this.image = i;
        this.actualitzarVista();
    }
    
    public void actualitzarVista(){
        log.debug("actualitzarVista called");
        this.repaint();
    }
    
    public dades2d getPanelImatge() {
        return panelImatge;
    }
    
    public void setPanelImatge(dades2d panelImatge) {
        this.panelImatge = panelImatge;
    }

    
    public void fitImage() {
        // En aquest cas hem de fer encabir-ho a la finestra
        log.writeNameNums("CONFIG", true, "panelWidth, ImageWidth", getPanelImatge().getWidth(),getImage().getWidth());
        log.writeNameNums("CONFIG", true, "panelHeight, Imageheigh", getPanelImatge().getHeight(),getImage().getHeight());
        scalefitX = (float)getPanelImatge().getWidth() / (float)getImage().getWidth();
        scalefitY = (float)getPanelImatge().getHeight() / (float)getImage().getHeight();
//        scalefit = (float) FastMath.min(xScale, yScale);
        log.writeNameNums("CONFIG", true, "scalefitX,scalefitY", scalefitX,scalefitY);
        // CENTREM LA IMATGE AL PANELL
        //en Y
        float gap = (getPanelImatge().getHeight() - (getImage().getHeight()) * scalefitY) / 2.f;
        originY = originY + FastMath.round(gap);
        // en x
        gap = (getPanelImatge().getWidth() - (getImage().getWidth()) * scalefitX) / 2.f;
        originX = originX + FastMath.round(gap);
    }
    
    /**
     * @return the toPaint
     */
    public ArrayList<DataSerie> getToPaint() {
        return toPaint;
    }


    /**
     * @param toPaint the toPaint to set
     */
    public void setToPaint(ArrayList<DataSerie> toPaint) {
        this.toPaint = toPaint;
    }

    
    //prova utilitzant scalefit
    protected Rectangle calcSubimatgeDinsFrame() {
        Point2D.Float startCoords = getPixel(new Point2D.Float(1, 1));
        int InPixX = (int)startCoords.x; //faig int per agafar el pixel en questio des del començament
        int InPixY = (int)startCoords.y;
        int OutPixX = InPixX + (int)(getPanelImatge().getWidth()/scalefitX) + 1;
        int OutPixY = InPixY + (int)(getPanelImatge().getHeight()/scalefitY) + 1;
        
        // Que no movem la imatge fora del panell
        if (InPixX >= getImage().getWidth() || OutPixX < 0 || InPixY >= getImage().getHeight() || OutPixY < 0) {
            return null;
        }
        if (InPixX < 0)InPixX = 0;
        if (InPixY < 0)InPixY = 0;
//        if (OutPixX >= patt2D.getDimX())OutPixX = patt2D.getDimX()-1;
//        if (OutPixY >= patt2D.getDimY())OutPixY = patt2D.getDimY()-1;
        if (OutPixX >= toPaint.get(0).getNpoints())OutPixX = toPaint.get(0).getNpoints()-1;
        if (OutPixY >= toPaint.size())OutPixY = toPaint.size()-1;
        
        return new Rectangle(InPixX, InPixY, OutPixX-InPixX+1, OutPixY-InPixY+1);
    }
    
    // el pixel que entra est� al rang 0..n-1 donat un pixel px,py a quin punt x,y del JFrame est�
    public Point2D.Float getFramePointFromPixel(Point2D.Float px) {
        float x = (px.x * scalefitX) + originX; //0.5 per posar-ho al centre del pixel
        float y = (px.y * scalefitY) + originY;
        return new Point2D.Float(x,y);
    }
    
    // segons la mida de la imatge actual, les coordenades d'un punt assenyalat amb el mouse correspondran a un pixel o
    // a un altre, aquesta subrutina ho corregeix: Donat el punt p on el mouse es troba te'l torna com a pixel de la imatge
    public Point2D.Float getPixel(Point2D.Float p) {
        float x = (p.x - originX) / scalefitX;
        float y = (p.y - originY) / scalefitY;
        return new Point2D.Float(x,y);
    }
    
    public class dades2d extends JPanel {

        private static final long serialVersionUID = 1L;
        
        public dades2d(){
            super();
            log.debug("constructor dades2d called");
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            log.debug("paint Component dades2d called");
            
            Graphics2D g2 = (Graphics2D) g;
            
            if (getImage() != null) {

                if (scalefitX <= 0) {
                    fitImage();
                }
                if (scalefitY <= 0) {
                    fitImage();
                }

                Rectangle rect = calcSubimatgeDinsFrame();
                if (rect == null || rect.width == 0 || rect.height == 0) {
                    // no part of image is displayed in the panel
                    return;
                }
                try {
                    subimage = getImage().getSubimage(rect.x, rect.y, rect.width, rect.height);
                    //log.writeNameNumPairs("fine", true, "rect.x, rect.y,", rect.x, rect.y);
                } catch (Exception e) {
                    if (D1Dplot_global.isDebug())e.printStackTrace();
                    log.warning("error getting the subImage");
                }
                // Rectangle r = g2.getClipBounds();
                AffineTransform t = new AffineTransform();
                float offsetX = originX % scalefitX;
                if (originX>0)offsetX = originX;
                float offsetY = originY % scalefitY;
                if (originY>0)offsetY = originY;
                t.translate(offsetX, offsetY);
                t.scale(scalefitX, scalefitY);
                g2.drawImage(getSubimage(), t, null);
//                final Graphics2D g1 = (Graphics2D) g2.create();

//                g1.dispose();
                g2.dispose();
                
            }
        }
    }


}

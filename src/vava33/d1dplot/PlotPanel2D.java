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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import com.vava33.jutils.FileUtils;
import com.vava33.jutils.VavaLogger;

import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;

import org.apache.commons.math3.util.FastMath;

import vava33.d1dplot.auxi.DataFileUtils;
import vava33.d1dplot.auxi.DataSerie;

import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.JCheckBox;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseWheelEvent;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JButton;
import javax.swing.JTextField;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

public class PlotPanel2D extends JDialog {

    private static final long serialVersionUID = -6764605138173790197L;
    private static VavaLogger log = D1Dplot_global.getVavaLogger(PlotPanel2D.class.getName());
    private dades2d panelImatge;
    private llegenda2D panel_llegenda;
    private BufferedImage image;
    private BufferedImage subimage;
    private BufferedImage llegenda;
    private ArrayList<DataSerie> toPaint;
    
    // DEFINICIO BUTONS DEL MOUSE
    private static int MOURE = MouseEvent.BUTTON2;
    private static int CLICAR = MouseEvent.BUTTON1;
    private static int ZOOM_BORRAR = MouseEvent.BUTTON3;
    
    private static float incZoom = 0.05f;
    private static int contrast_fun=0;
    private int nameMaxWidth;
    private int nameXPos;
    private Color nameColor = Color.BLACK;
    //l'SCALEFIT Y SEMPRE SERA PER OMPLIR LA FINESTRA? I EL X ES EL QUE ES MOURÀ?
    private float scalefitX=-1;
    private float scalefitY=-1;
    private float scalefitYllegenda = -1;
    private int originX, originY;
    private Point2D.Float zoomPoint, dragPoint;
    private Point2D.Float currentMousePoint;
    private boolean reversed = false;
    private boolean mouseDrag = false;
    private boolean mouseZoom = false;
    private boolean mouseBox = false;
    
    int minValSlider;
    int valSlider;
    double maxY;
    double minY;
    double meanY;
    double minT2;
    double maxT2;
    double nXPoints;
    private JSlider slider_contrast;
    private JCheckBox chckbxColor;
    private JPanel panel;
    private JButton btnFitToWindow;
    private JTextField txtMincontrast;
    private JTextField txtMaxcontrast;
    private JLabel lblContrastValue;
    private JLabel lblPunt;
    private JPanel panel_1;
    private JLabel lbl_legMax;
    private JLabel lbl_leg3Q;
    private JLabel lbl_leg2Q;
    private JLabel lbl_leg1Q;
    private JLabel lbl_legMin;
    private JButton btnFitY;
    private JCheckBox chckbxSquareSelection;
    private JCheckBox chckbxAlwaysFitY;
    private JPanel panel_2;
    private JButton btnSaveAsPng;
    private JCheckBox chckbxShowPattNames;
    private JLabel lblMaxwidth;
    private JLabel lblXPosition;
    private JTextField txtMaxwidth;
    private JTextField txtXposition;
    private JLabel lblColor;
    
    private D1Dplot_main main;
    private JPanel panel_3;
    private JCheckBox chckbxInvertOrder;
    
    /**
     * Create the panel.
     */
    public PlotPanel2D(D1Dplot_main m) {
        this.main = m;
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                do_this_componentResized(e);
            }
        });
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 960, 567);

        getContentPane().setLayout(new MigLayout("", "[grow][]", "[][grow][]"));

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
        getContentPane().add(this.getPanelImatge(), "cell 0 1 1 1,grow");
        
        panel = new JPanel();
        getContentPane().add(panel, "cell 0 0 2 1,grow");
        panel.setLayout(new MigLayout("", "[][][][][][][][grow]", "[][][]"));
        
        btnFitToWindow = new JButton("Fit to window");
        btnFitToWindow.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                do_btnFitToWindow_actionPerformed(arg0);
            }
        });
        panel.add(btnFitToWindow, "cell 0 0");
        
        btnFitY = new JButton("Fit Y");
        btnFitY.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_btnNewButton_actionPerformed(e);
            }
        });
        panel.add(btnFitY, "cell 1 0");
        
        chckbxAlwaysFitY = new JCheckBox("auto fit Y");
        chckbxAlwaysFitY.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                do_chckbxAlwaysFitY_itemStateChanged(e);
            }
        });
        panel.add(chckbxAlwaysFitY, "flowx,cell 2 0,alignx center");
        
        chckbxSquareSelection = new JCheckBox("square selection");
        panel.add(chckbxSquareSelection, "cell 3 0 2 1,alignx center");
        
        chckbxShowPattNames = new JCheckBox("Pattern names");
        chckbxShowPattNames.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent arg0) {
                do_chckbxShowPattNames_itemStateChanged(arg0);
            }
        });
        
        btnSaveAsPng = new JButton("save as PNG");
        btnSaveAsPng.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_btnSaveAsPng_actionPerformed(e);
            }
        });
        
        chckbxInvertOrder = new JCheckBox("invert Order");
        chckbxInvertOrder.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent arg0) {
                do_chckbxInvertOrder_itemStateChanged(arg0);
            }
        });
        panel.add(chckbxInvertOrder, "cell 5 0");
        panel.add(btnSaveAsPng, "cell 6 0 2 1,alignx right");
        panel.add(chckbxShowPattNames, "cell 0 1");
        
        lblColor = new JLabel("color");
        lblColor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                do_lblColor_mouseClicked(e);
            }
        });
        panel.add(lblColor, "cell 1 1,grow");
        
        lblMaxwidth = new JLabel("Max name Widtht");
        panel.add(lblMaxwidth, "cell 2 1,alignx trailing");
        
        txtMaxwidth = new JTextField();
        txtMaxwidth.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtMaxwidth_actionPerformed(e);
            }
        });
        txtMaxwidth.setText("maxWidth");
        panel.add(txtMaxwidth, "cell 3 1,growx");
        txtMaxwidth.setColumns(5);
        
        txtXposition = new JTextField();
        txtXposition.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtXposition_actionPerformed(e);
            }
        });
        
        lblXPosition = new JLabel("Name X pos");
        panel.add(lblXPosition, "cell 4 1,alignx trailing");
        txtXposition.setText("xposition");
        panel.add(txtXposition, "cell 5 1,growx");
        txtXposition.setColumns(5);
        
        panel_3 = new JPanel();
        panel.add(panel_3, "cell 0 2 8 1,grow");
        panel_3.setLayout(new MigLayout("", "[][][grow][][][]", "[]"));
        
        txtMincontrast = new JTextField();
        panel_3.add(txtMincontrast, "cell 0 0,growx");
        txtMincontrast.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtMincontrast_actionPerformed(e);
            }
        });
        txtMincontrast.setText("minContrast");
        txtMincontrast.setColumns(6);
        
        slider_contrast = new JSlider();
        panel_3.add(slider_contrast, "cell 1 0 2 1,grow");
        
         txtMaxcontrast = new JTextField();
         panel_3.add(txtMaxcontrast, "cell 3 0,growx");
         txtMaxcontrast.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 do_txtMaxcontrast_actionPerformed(e);
             }
         });
         txtMaxcontrast.setText("maxContrast");
         txtMaxcontrast.setColumns(6);
         
         lblContrastValue = new JLabel("cvalue");
         panel_3.add(lblContrastValue, "cell 4 0,alignx center");
         
         chckbxColor = new JCheckBox("color");
         panel_3.add(chckbxColor, "cell 5 0,alignx center");
         chckbxColor.addItemListener(new ItemListener() {
             public void itemStateChanged(ItemEvent arg0) {
                 do_chckbxColor_itemStateChanged(arg0);
             }
         });
         chckbxColor.setSelected(true);
        slider_contrast.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                do_slider_contrast_stateChanged(arg0);
            }
        });
        
        panel_1 = new JPanel();
        getContentPane().add(panel_1, "cell 1 1,grow");
        panel_1.setLayout(new MigLayout("", "[20:20px:20px][]", "[][grow][][grow][]"));
        
        panel_llegenda = new llegenda2D();
        panel_1.add(panel_llegenda, "cell 0 0 1 5,grow");
        
        lbl_legMax = new JLabel("max");
        lbl_legMax.setFont(new Font("Dialog", Font.BOLD, 11));
        panel_1.add(lbl_legMax, "cell 1 0,aligny top");
        
        lbl_leg3Q = new JLabel("3Q");
        lbl_leg3Q.setFont(new Font("Dialog", Font.BOLD, 11));
        panel_1.add(lbl_leg3Q, "cell 1 1");
        
        lbl_leg2Q = new JLabel("<html>\n<br>\n2Q\n<br>\n<br>\n</html>");
        lbl_leg2Q.setFont(new Font("Dialog", Font.BOLD, 11));
        panel_1.add(lbl_leg2Q, "cell 1 2");
        
        lbl_leg1Q = new JLabel("1Q");
        lbl_leg1Q.setFont(new Font("Dialog", Font.BOLD, 11));
        panel_1.add(lbl_leg1Q, "cell 1 3");
        
        lbl_legMin = new JLabel("min");
        lbl_legMin.setFont(new Font("Dialog", Font.BOLD, 11));
        panel_1.add(lbl_legMin, "cell 1 4,aligny bottom");
        
        panel_2 = new JPanel();
        getContentPane().add(panel_2, "cell 0 2 2 1,grow");
        panel_2.setLayout(new MigLayout("", "[grow][]", "[]"));
        
        lblPunt = new JLabel("punt");
        panel_2.add(lblPunt, "cell 0 0");
    
        this.inicia();
        this.resetView();
    }
    
    private void inicia(){
        slider_contrast.setMaximum(10000);
        slider_contrast.setMinimum(0);
        slider_contrast.setValue(4000);
        txtMincontrast.setText(Integer.toString(slider_contrast.getMinimum()));
        txtMaxcontrast.setText(Integer.toString(slider_contrast.getMaximum()));
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
        if (arg0.getButton() == CLICAR) {
            this.mouseBox = true;
        }
        actualitzarVista();
    }

    protected void do_panelImatge_mouseReleased(MouseEvent e) {
        if (e.getButton() == MOURE)
            this.mouseDrag = false;
        if (e.getButton() == ZOOM_BORRAR)
            this.mouseZoom = false;
        if (e.getButton() == CLICAR){
            Point2D.Float p = new Point2D.Float(e.getPoint().x, e.getPoint().y);
            this.mouseBox = false;
            //DO ZOOM considerant quadrat dragpoint i p
            fitImageZone(dragPoint,p);
        }
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
            if(isDebug())log.writeNameNumPairs("fine", true, "fX,fY,imX,imY,scfitX,scfitY,orX,orY,panw,panh", e.getPoint().x,e.getPoint().y,p.x,p.y,scalefitX,scalefitY,originX,originY,getPanelImatge().getWidth(),getPanelImatge().getHeight());
        }
        if (this.mouseZoom == true) {
            Point2D.Float p = new Point2D.Float(e.getPoint().x, e.getPoint().y);
            float incY;
            incY = p.y - dragPoint.y;
            this.dragPoint = p;
            boolean zoomIn = (incY < 0);
            this.zoom(zoomIn, zoomPoint);
        }
        if (this.mouseBox == true) {
            this.currentMousePoint = new Point2D.Float(e.getPoint().x, e.getPoint().y);
        }

        actualitzarVista();

    }
    
    protected void do_panelImatge_mouseMoved(MouseEvent e) {
        // he de normalitzar les coordenades a la mida de la imatge en pixels
        this.currentMousePoint = new Point2D.Float(e.getPoint().x, e.getPoint().y);
        if (toPaint == null)return;
        if (toPaint.size()==0)return;
        
        Point2D.Float pix = this.getPixel(currentMousePoint);
        if (pix.x < 0 || pix.y < 0 || pix.x >= nXPoints || pix.y >= toPaint.size()) {
            return;
        }
        
        int serie = (int)pix.y;
        int punt = (int)pix.x;
        
        double t2 = toPaint.get(serie).getPoint(punt).getX();
        double inten = toPaint.get(serie).getPoint(punt).getY();
        
        lblPunt.setText(String.format("Pattern: %s   2"+D1Dplot_global.theta+"= %.4f   Intensity=%.2f" ,toPaint.get(serie).getSerieName(),t2,inten));
        
    }


    // es mou l'origen a traves d'un increment de les coordenades
    public void moveOrigin(float incX, float incY, boolean repaint) {
        // assignem un nou origen de la imatge amb un increment a les coordenades anteriors
        //  (util per moure'l fen drag del mouse)
        if(isDebug())log.writeNameNums("fine", true, "incX,incY", incX,incY);
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
            if (!isAlwaysFitY()){
                scalefitY = scalefitY + (incZoom * scalefitY);                
            }
        } else {
            scalefitX = scalefitX - (incZoom * scalefitX);
            if (!isAlwaysFitY()){
                scalefitY = scalefitY - (incZoom * scalefitY);    
            }
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
    
    public boolean isAlwaysFitY(){
        return chckbxAlwaysFitY.isSelected();
    }
    
    public boolean isPosaTitols(){
        return chckbxShowPattNames.isSelected();
    }
    
    //S'HA D'ADAPTAR
    public void setImagePatts(ArrayList<DataSerie> dss) {
        if(dss == null)return;
        if(dss.size()==0)return;

        this.toPaint = dss;
        
        maxY = Double.MIN_VALUE;
        minY = Double.MAX_VALUE;
        meanY = 0;
        
        //calculem la min2t, max2t
        for (int i=0;i<dss.size();i++){
            double[] vals = dss.get(i).calcYmeanYDesvYmaxYmin();
            if (vals[2]>maxY)maxY=vals[2];
            if (vals[3]<minY)minY=vals[3];
            meanY = meanY + vals[0];
        }
        meanY = meanY / dss.size();
        nXPoints = dss.get(0).getNpoints();
        
        //les x tots els patterns haurien de coincidir
        maxT2 = dss.get(0).getPoint(dss.get(0).getNpoints()-1).getX();
        minT2 = dss.get(0).getPoint(0).getX();
        
        int quarter = panelImatge.getWidth()/4;
        nameXPos = panelImatge.getWidth()-quarter;
        nameMaxWidth = quarter - 1;
        txtMaxwidth.setText(Integer.toString(nameMaxWidth));
        txtXposition.setText(Integer.toString(nameXPos));
        lblColor.setBackground(nameColor);
        lblColor.setText("");
        lblColor.setOpaque(true);
        
        this.pintaImatge();
        this.pintaLlegenda();
        this.actualitzarVista();
    }
    
    protected void do_slider_contrast_stateChanged(ChangeEvent arg0) {
        lblContrastValue.setText(Integer.toString(slider_contrast.getValue()));
        this.pintaImatge(); //ja conte actualitzar vista
        this.pintaLlegenda();
   }
    
    protected void pintaImatge() {
        logdebug("ImagePanel pintaImatge called");
        
        if (toPaint == null)return;
        if (toPaint.size()==0)return;

        int dimx = toPaint.get(0).getNpoints();
        int type = BufferedImage.TYPE_INT_ARGB;
        BufferedImage im = new BufferedImage(dimx, toPaint.size(), type);

        
        minValSlider = this.slider_contrast.getMinimum();
        valSlider = this.slider_contrast.getValue();
        logdebug("ValSlider="+valSlider);

        for (int i = 0; i < im.getHeight(); i++) { // per cada fila (Y)
            for (int j = 0; j < im.getWidth(); j++) { // per cada columna (X)
                
                //calcular el color...
                //hem de mirar quina serie (i) quin punt (j)
                im.setRGB(j, i, this.getColorOfAPixel(j, i).getRGB());
            }
        }
        logdebug("ImagePanel pintaImatge updateImage call");
        this.updateImage(im);
    }
    
    protected void pintaLlegenda() {
        if (toPaint == null)return;
        if (toPaint.size()==0)return;

        int maxVal = slider_contrast.getValue();
        int minVal = slider_contrast.getMinimum();
        int dimY = maxVal - minVal;
        //        int dimX = 1;
        int type = BufferedImage.TYPE_INT_ARGB;

        llegenda = new BufferedImage(1, dimY, type);
        float height = (float)panel_llegenda.getHeight();
        logdebug("panel llegenda height="+height);
        scalefitYllegenda = height /(float)dimY;

        int quarter = (int) (dimY/4.);
        lbl_legMax.setText(Integer.toString(maxVal)+" ");
        lbl_leg3Q.setText(Integer.toString(maxVal-quarter)+" ");
        lbl_leg2Q.setText("<html>\n<br>\n"+Integer.toString(maxVal-2*quarter)+" \n<br>\n<br>\n</html>");
        lbl_leg1Q.setText(Integer.toString(maxVal-3*quarter)+" ");
        lbl_legMin.setText(Integer.toString(minVal)+" ");
        
        for (int i = 0; i < llegenda.getHeight(); i++) { // per cada fila (Y)
            //en aquest cas nomes tenim una columna
            //al començar per dalt hem de començar pel maxim
            //hem de mirar quina serie (i) quin punt (j)
            Color col;
            if (this.isColor()){
                col = intensityColor(maxVal-i, maxY,minY, minValSlider,valSlider);
            }else{
                col = intensityBW(maxVal-i, maxY,minY,minValSlider,valSlider);
            }
            llegenda.setRGB(0, i, col.getRGB());
        }
        this.panel_llegenda.repaint();
    }
        
        
    
    private Color getColorOfAPixel(int jy, int ix){
        Color col;
        if (this.isColor()) {
            // pintem en color
            col = intensityColor(toPaint.get(ix).getPoint(jy).getY(), maxY,minY, minValSlider,valSlider);
        } else {
            // pintem en BW
            col = intensityBW(toPaint.get(ix).getPoint(jy).getY(), maxY,minY,minValSlider,valSlider);
        }
        return col;
    }
    
    /*
     * maxInt,minInt son maxim i minim d'intensitat de la imatge
     * minVal,maxVal corresponen a l'slide. MAX es el valor actual assenyalat per l'slide.
     * grafiques RGB entre minval i maxval amb punt inflexio a (maxval-minval)/2
     * intensitat normalitzada entre minval i maxval
     */
    protected Color intensityColor(double intensity, double maxInt, double minInt, int minVal, int maxVal) {
                
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
            logdebug("invalid color");
        }
        return c;
    }
    
    //valor interpolat sobre una recta (fun=0) o una parabola (fun=1)
    protected Color intensityBW(double intensity, double maxInt, double minInt,int minVal, int maxVal) {
        
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
        logdebug("actualitzarVista called");
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
        if(isDebug())log.writeNameNums("CONFIG", true, "panelWidth, ImageWidth", getPanelImatge().getWidth(),getImage().getWidth());
        if(isDebug())log.writeNameNums("CONFIG", true, "panelHeight, Imageheigh", getPanelImatge().getHeight(),getImage().getHeight());
        scalefitX = (float)getPanelImatge().getWidth() / (float)getImage().getWidth();
        scalefitY = (float)getPanelImatge().getHeight() / (float)getImage().getHeight();
        if(isDebug())log.writeNameNums("CONFIG", true, "scalefitX,scalefitY", scalefitX,scalefitY);
        //aixo no cal centrar perque ara estem distorsionant la imatge, l'origen sempre sera 0 
        originX=0;
        originY=0;
        this.actualitzarVista();
        
    }
    
    protected void do_btnNewButton_actionPerformed(ActionEvent e) {
        scalefitY = (float)getPanelImatge().getHeight() / (float)getImage().getHeight();
        originY=0;
        this.actualitzarVista();
    }
    
    public void fitImageZone(Point2D.Float vertexIni, Point2D.Float vertexFin){
        logdebug("fit Image zone entered");
        if(isDebug())log.writeNameNumPairs("config", true, "verteIni.x,vertexIni.y,vertexFin.x,vertexFin.y", vertexIni.x,vertexIni.y,vertexFin.x,vertexFin.y);
        //com hem pintat el quadrat (hi ha quatre variants)
        
        if (!this.isSquareSection()){
            vertexIni.y=0;
            vertexFin.y=getPanelImatge().getHeight();
        }
        
        //assignarem pini i pfin com el vertex superior esquerra i inferior dret respectivament
        Point2D.Float pini = null;
        Point2D.Float pfin = null;
        
        if (vertexIni.x>vertexFin.x){
            //hem pintat cap a l'esquerra
            if(vertexIni.y>vertexFin.y){
                //hem pintat cap amunt (inverteixo rols)
                pini = this.getPixel(vertexFin);
                pfin = this.getPixel(vertexIni);
            }else{
                //hem pintat cap avall
                pini = this.getPixel(new Point2D.Float(vertexFin.x,vertexIni.y));//x final y primer
                pfin = this.getPixel(new Point2D.Float(vertexIni.x,vertexFin.y));//x primer y final
            }
        }else{
            //hem pintat cap a la dreta
            if(vertexIni.y>vertexFin.y){
                //hem pintat cap amunt
                pini = this.getPixel(new Point2D.Float(vertexIni.x,vertexFin.y));//x primer y final
                pfin = this.getPixel(new Point2D.Float(vertexFin.x,vertexIni.y));//x final y primer
            }else{
                //hem pintat cap avall (CAS NORMAL!!)
                pini = this.getPixel(vertexIni);
                pfin = this.getPixel(vertexFin);
            }
        }
        if(isDebug())log.writeNameNumPairs("config", true, "scalefitX,scalefitY,originX,originY", scalefitX,scalefitY,originX,originY);
//        Point2D.Float pini = this.getPixel(vertexIni);
//        Point2D.Float pfin = this.getPixel(vertexFin);
        float sizeX = FastMath.abs(FastMath.abs(pfin.x) - FastMath.abs(pini.x));
        float sizeY = FastMath.abs(FastMath.abs(pfin.y) - FastMath.abs(pini.y));
        scalefitX = (float)getPanelImatge().getWidth() / sizeX;
        scalefitY = (float)getPanelImatge().getHeight() / sizeY;
        //movem origen
        Point2D.Float piniNew = getFramePointFromPixel(pini);
        originX = originX + FastMath.round(-piniNew.x);
        originY = originY + FastMath.round(-piniNew.y);
        if(isDebug())log.writeNameNumPairs("config", true, "sizeX,sizeY,scalefitX,scalefitY,verteIni.x,vertexIni.y,originX,originY", sizeX,sizeY,scalefitX,scalefitY,vertexIni.x,vertexIni.y,originX,originY);
        
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
            logdebug("constructor dades2d called");
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            logdebug("paint Component dades2d called");
            
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
                AffineTransform t = new AffineTransform();
                float offsetX = originX % scalefitX;
                if (originX>0)offsetX = originX;
                float offsetY = originY % scalefitY;
                if (originY>0)offsetY = originY;
                t.translate(offsetX, offsetY);
                t.scale(scalefitX, scalefitY);
                g2.drawImage(getSubimage(), t, null);
                final Graphics2D g1 = (Graphics2D) g2.create();
                
                if (mouseBox) {
                    if(isSquareSection()){
                        //un quadrat
                        dibuixarQuadrat(g1);
                    }else{
                        //dibueixem amb fitY
                        dibuixarSemiQuadrat(g1);
                    }
                }
                
                if (isPosaTitols()){
                    writeTitles(g1);
                }
                
                g1.dispose();
                g2.dispose();
                
            }
        }
        private void writeTitles(Graphics2D g1) {
            Font font = g1.getFont(); //font inicial
            float strokewidth = 3;
            g1.setColor(nameColor);
            int currentPatt = -99;
            int nPixPerPat = 0;
            int pixInicialPatt = 0;

            for (int i=0; i<this.getHeight(); i++){
                //mirarem pixel del centre
                double pattD = getPixel(new Point2D.Float(this.getWidth()/2,i)).getY();
                //potser esta fora...
                if ((pattD<0)||(pattD>=toPaint.size())){
                    continue;
                }
                int patt = (int)pattD;
                if (patt!=currentPatt){
                    //això vol dir que començem un nou pattern, hem d'escriure el titol de l'anterior
                    if (currentPatt==-99){
                        //era el primer
                        currentPatt=patt;
                        nPixPerPat=1;
                        pixInicialPatt=i;
                        continue;
                    }
                    //si no era el primer pattern dibuixem el nom
                    int llocY = pixInicialPatt+(nPixPerPat/2);
                    String s =  toPaint.get(currentPatt).getSerieName();
                    
                    double[] swh = getWidthHeighString(g1,s);
                    while (swh[0]>nameMaxWidth){
                        g1.setFont(g1.getFont().deriveFont(g1.getFont().getSize()-1f));
                        swh = getWidthHeighString(g1,s);
                    }
                    int t_Y = (int) (llocY-strokewidth+(swh[1]/2.));
                    g1.drawString(s, nameXPos,t_Y);
                    g1.setFont(font);                //recuperem font inicial

                    //i ara reiniciem 
                    currentPatt = patt;
                    nPixPerPat=1;
                    pixInicialPatt=i;
                }else{
                    //seguim al mateix pattern, cal incrementar
                    nPixPerPat++;
                }
            }
            //caldra escriure l'ultim pattern
            int llocY = pixInicialPatt+(nPixPerPat/2); 
            String s =  toPaint.get(currentPatt).getSerieName();
            
            double[] swh = getWidthHeighString(g1,s);
            while (swh[0]>nameMaxWidth){
                g1.setFont(g1.getFont().deriveFont(g1.getFont().getSize()-1f));
                swh = getWidthHeighString(g1,s);
            }
            int t_Y = (int) (llocY-strokewidth+(swh[1]/2.));
            g1.drawString(s, nameXPos,t_Y);
            g1.setFont(font);                //recuperem font inicial
        }
        
        private double[] getWidthHeighString(Graphics2D g1, String s){
            double[] w_h = new double[2];
            Font font = g1.getFont();
            FontRenderContext frc = g1.getFontRenderContext();
            w_h[0] = font.getStringBounds(s, frc).getWidth();
            w_h[1] =  font.getStringBounds(s, frc).getHeight();
            return w_h;
        }
        private void dibuixarQuadrat(Graphics2D g1) {
            //tenim els vertexs oposats del quadrat (dragPoint i currentPoint) que seran v1 i v3 respectivament numerant els vertexs del quadrat en sentit horari
            //cal fer linies v1-v2-v4-v3-v1
            int v1x = (int) dragPoint.x;
            int v1y = (int) dragPoint.y;
            int v3x = (int) currentMousePoint.x;
            int v3y = (int) currentMousePoint.y;
            g1.setColor(Color.BLACK);
            g1.setStroke(new BasicStroke(1.0f));
            //v1-v2
            g1.drawLine(v1x,v1y, v3x, v1y);
            //v2-v4
            g1.drawLine(v3x, v1y, v3x, v3y);
            //v4-v3
            g1.drawLine(v3x, v3y, v1x, v3y);
            //v3-v1
            g1.drawLine(v1x, v3y, v1x, v1y);
        }
        
        private void dibuixarSemiQuadrat(Graphics2D g1) {
            //igual que a dalt però Y1 sera 0 i Y3 sera panelheight
            int v1x = (int) dragPoint.x;
            int v1y = 0;
            int v3x = (int) currentMousePoint.x;
            int v3y = this.getHeight();
            g1.setColor(Color.BLACK);
            g1.setStroke(new BasicStroke(1.0f));
            //v1-v2
            g1.drawLine(v1x,v1y, v3x, v1y);
            //v2-v4
            g1.drawLine(v3x, v1y, v3x, v3y);
            //v4-v3
            g1.drawLine(v3x, v3y, v1x, v3y);
            //v3-v1
            g1.drawLine(v1x, v3y, v1x, v1y);
        }

    }

    public boolean isSquareSection(){
        return chckbxSquareSelection.isSelected();
    }
    
    protected void do_btnFitToWindow_actionPerformed(ActionEvent arg0) {
        this.fitImage();
        this.pintaLlegenda();
    }
    protected void do_txtMaxcontrast_actionPerformed(ActionEvent e) {
        try{
            int val = Integer.parseInt(txtMaxcontrast.getText());
            slider_contrast.setMaximum(val);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    protected void do_txtMincontrast_actionPerformed(ActionEvent e) {
        try{
            int val = Integer.parseInt(txtMincontrast.getText());
            slider_contrast.setMinimum(val);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    public class llegenda2D extends JPanel {

        private static final long serialVersionUID = 1L;
        
        public llegenda2D(){
            super();
            logdebug("constructor llegenda called");
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            logdebug("paint Component llegenda called");
            
            Graphics2D g2 = (Graphics2D) g;
            
            if (getLlegenda() != null) {
                
                AffineTransform t = new AffineTransform();
                
                if(isDebug())log.writeNameNumPairs("config", true, "widthPanelLLeg,scalefitYllegenda", panel_llegenda.getWidth(),scalefitYllegenda);
                
                t.scale(panel_llegenda.getWidth(), scalefitYllegenda);
                g2.drawImage(getLlegenda(), t, null);
                
//                g2.drawImage(getLlegenda(), 0, 0, null);
                g2.dispose();
                
            }
        }
    }


    public BufferedImage getLlegenda() {
        return llegenda;
    }

    public void setLlegenda(BufferedImage llegenda) {
        this.llegenda = llegenda;
    }
    
    protected void do_this_componentResized(ComponentEvent e) {
        this.btnFitToWindow.doClick();
    }
    
    protected void do_btnSaveAsPng_actionPerformed(ActionEvent e) {
        File fpng = FileUtils.fchooserSaveNoAsk(this, new File(D1Dplot_global.getWorkdir()), null); //ja preguntem despres
        if (fpng!=null){
            fpng = FileUtils.canviExtensio(fpng, "png");
            if (fpng.exists()){
                int actionDialog = JOptionPane.showConfirmDialog(this,
                        "Replace existing file?");
                if (actionDialog == JOptionPane.NO_OPTION)return;
            }
            int w = panelImatge.getSize().width+panel_llegenda.getSize().width;
            int h = panelImatge.getSize().height;
            String s = (String)JOptionPane.showInputDialog(
                    this,
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
                if(isDebug())log.writeNameNumPairs("config", true, "factor", factor);
                this.savePNG(fpng,factor);
            }
        }
    }
    
    private void savePNG(File fpng, float factor){

      double pageWidth = panelImatge.getSize().width*factor;
      double pageHeight = panelImatge.getSize().height*factor;
      double imageWidth = panelImatge.getSize().width;
      double imageHeight = panelImatge.getSize().height;

      double scaleFactor = DataFileUtils.getScaleFactorToFit(
              new Dimension((int) Math.round(imageWidth), (int) Math.round(imageHeight)),
              new Dimension((int) Math.round(pageWidth), (int) Math.round(pageHeight)));

      double legendPageWidth = panel_1.getSize().width*factor;
      double legendPageHeight = panel_1.getSize().height*factor;
      
      int widthIMG = (int) Math.round(pageWidth);
      int heightIMG = (int) Math.round(pageHeight);
      int widthLEG = (int) Math.round(legendPageWidth);
      int heightLEG = (int) Math.round(legendPageHeight);

      //creem les dues imatges
      BufferedImage img = new BufferedImage(
              widthIMG,
              heightIMG,
              BufferedImage.TYPE_INT_ARGB);

      BufferedImage imgL = new BufferedImage(
              widthLEG,
              heightLEG,
              BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2d = img.createGraphics();
      Graphics2D g2dL = imgL.createGraphics();
      g2d.scale(scaleFactor, scaleFactor);
      g2dL.scale(scaleFactor, scaleFactor);
      panelImatge.paintComponent(g2d);
      panel_1.paintComponents(g2dL);
      
      BufferedImage imgTOT = joinBufferedImage(img,imgL,0);
      g2d.dispose();

      try {
          ImageIO.write(imgTOT, "png", fpng);
      } catch (Exception ex) {
          ex.printStackTrace();
      }
      logdebug(fpng.toString()+" written");
  }
    
    public static BufferedImage joinBufferedImage(BufferedImage img1,BufferedImage img2, int offset) {
        //do some calculate first
        int wid = img1.getWidth()+img2.getWidth()+offset;
        int height = Math.max(img1.getHeight(),img2.getHeight())+offset;
        //create a new buffer and draw two image into the new image
        BufferedImage newImage = new BufferedImage(wid,height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = newImage.createGraphics();
        Color oldColor = g2.getColor();
        //fill background
        g2.setPaint(Color.WHITE);
        g2.fillRect(0, 0, wid, height);
        //draw image
        g2.setColor(oldColor);
        g2.drawImage(img1, null, 0, 0);
        g2.drawImage(img2, null, img1.getWidth()+offset, 0);
        g2.dispose();
        return newImage;
    }
    protected void do_chckbxShowPattNames_itemStateChanged(ItemEvent arg0) {
        this.actualitzarVista();
    }
    protected void do_txtMaxwidth_actionPerformed(ActionEvent e) {
        try{
            nameMaxWidth = Integer.parseInt(txtMaxwidth.getText());
            this.actualitzarVista();
        }catch(Exception ex){
            loginfo("error reading maxwidth");
        }
    }
    protected void do_txtXposition_actionPerformed(ActionEvent e) {
        try{
            nameXPos = Integer.parseInt(txtXposition.getText()); 
            this.actualitzarVista();
        }catch(Exception ex){
            loginfo("error reading maxwidth");
        }
    }
    protected void do_lblColor_mouseClicked(MouseEvent e) {
        Color newColor = JColorChooser.showDialog(
                this,
                "Choose Pattern Names Color",
                getNameColor());
           if(newColor != null){
               setNameColor(newColor);
               lblColor.setBackground(newColor);
           }
    }

    /**
     * @return the nameColor
     */
    public Color getNameColor() {
        return nameColor;
    }

    /**
     * @param nameColor the nameColor to set
     */
    public void setNameColor(Color nColor) {
        nameColor = nColor;
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
        main.getTAOut().stat(s); //ho passem pel txtArea
    }
    
    private boolean isDebug(){
        return D1Dplot_global.isDebug();
    }
    
    protected void do_chckbxColor_itemStateChanged(ItemEvent arg0) {
        this.pintaImatge();
        this.pintaLlegenda();
    }
    
    protected void do_chckbxInvertOrder_itemStateChanged(ItemEvent arg0) {
        if (chckbxInvertOrder.isSelected() && !reversed){
            Collections.reverse(toPaint);
            reversed = true;
            this.pintaImatge();
        }
        if (!chckbxInvertOrder.isSelected() && reversed){
            Collections.reverse(toPaint);
            reversed = false;  
            this.pintaImatge();
        }
        
    }
    protected void do_chckbxAlwaysFitY_itemStateChanged(ItemEvent e) {
        if (chckbxAlwaysFitY.isSelected()) btnFitY.doClick();
    }
}

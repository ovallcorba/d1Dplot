package com.vava33.d1dplot;

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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import com.vava33.d1dplot.data.DataSerie;
import com.vava33.jutils.FileUtils;
import com.vava33.jutils.VavaLogger;

import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;

import org.apache.commons.math3.util.FastMath;

import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
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
import java.io.File;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.border.TitledBorder;
import javax.swing.border.LineBorder;
import java.awt.Dimension;

public class Plot2DPanel {

    private static final String className = "PlotPanel2D";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);
    
    private JDialog plot2Ddialog;
    private dades2d panelImatge;
    private llegenda2D panel_llegenda;
    private axis2D panel_axis;
    private BufferedImage image;
    private BufferedImage subimage;
    private BufferedImage llegendaImg;
    private List<DataSerie> toPaint;
    
    // DEFINICIO BUTONS DEL MOUSE
    private static int MOURE = MouseEvent.BUTTON2;
    private static int CLICAR = MouseEvent.BUTTON1;
    private static int ZOOM_BORRAR = MouseEvent.BUTTON3;
    
    private static float incZoom = 0.05f;
    private static int contrast_fun=0;
    private int nameMaxWidth=200;
    private int nameXPos=1;
    private Color nameColor = Color.BLACK;
    //l'SCALEFIT Y SEMPRE SERA PER OMPLIR LA FINESTRA? I EL X ES EL QUE ES MOURÀ?
    private float scalefitX=-1;
    private float scalefitY=-1;
    private float scalefitYllegenda = -1;
    private int originX, originY;
    private Point2D.Double zoomPoint, dragPoint, clickPoint;
    private Point2D.Double currentMousePoint;
    private boolean reversed = false;
    private boolean mouseDrag = false;
    private boolean mouseZoom = false;
    private boolean mouseBox = false;
    private boolean gridY = false;
    
    private double div_startValX;
    private double div_incXPrim;
    private double div_incXSec;
    
    int minValSlider;
    int valSlider;
    double maxY;
    double minY;
    double xrangeMax,xrangeMin;
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
    private JPanel panel_llegenda_full;
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
    private JCheckBox chckbxInvertOrder;
    private JPanel panel_4;
    private JPanel panel_5;
    private JLabel lblInix;
    private JLabel lblIncx;
    private JLabel lblSubx;
    private JTextField txtInix;
    private JTextField txtIncx;
    private JTextField txtSubx;
    private JPanel panel_3;
    private JCheckBox chckbxGridY;
    /**
     * Create the panel.
     */
    public Plot2DPanel(JFrame parent) {
    	plot2Ddialog = new JDialog(parent,"2D plot",false);
        plot2Ddialog.setIconImage(D1Dplot_global.getIcon());
//        plot2Ddialog.addComponentListener(new ComponentAdapter() {
//            @Override
//            public void componentResized(ComponentEvent e) {
//                do_this_componentResized(e);
//            }
//        });
        plot2Ddialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        plot2Ddialog.setBounds(100, 100, 960, 567);

        plot2Ddialog.getContentPane().setLayout(new MigLayout("", "[grow][]", "[][grow][]"));

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
        
        panel_3 = new JPanel();
        plot2Ddialog.getContentPane().add(panel_3, "cell 0 1,grow");
        panel_3.setLayout(new MigLayout("", "[grow][]", "[grow][40px:40px:40px]"));
        panel_3.setBackground(Color.WHITE);
        panel_3.setOpaque(true);
        panel_3.add(this.getPanelImatge(), "cell 0 0,grow");
        

        panel_llegenda_full = new JPanel();
        panel_llegenda_full.setBackground(Color.white);
        panel_llegenda_full.setOpaque(true);
        panel_3.add(panel_llegenda_full, "cell 1 0 1 2,grow");
        panel_llegenda_full.setLayout(new MigLayout("", "[20:20px:20px][]", "[][grow][][grow][]"));
        panel_llegenda = new llegenda2D();
        panel_llegenda_full.add(panel_llegenda, "cell 0 0 1 5,grow");

        lbl_legMax = new JLabel("max");
        lbl_legMax.setFont(new Font("Dialog", Font.BOLD, 11));
        panel_llegenda_full.add(lbl_legMax, "cell 1 0,aligny top");

        lbl_leg3Q = new JLabel("3Q");
        lbl_leg3Q.setFont(new Font("Dialog", Font.BOLD, 11));
        panel_llegenda_full.add(lbl_leg3Q, "cell 1 1");

        lbl_leg2Q = new JLabel("<html>\n<br>\n2Q\n<br>\n<br>\n</html>");
        lbl_leg2Q.setFont(new Font("Dialog", Font.BOLD, 11));
        panel_llegenda_full.add(lbl_leg2Q, "cell 1 2");

        lbl_leg1Q = new JLabel("1Q");
        lbl_leg1Q.setFont(new Font("Dialog", Font.BOLD, 11));
        panel_llegenda_full.add(lbl_leg1Q, "cell 1 3");

        lbl_legMin = new JLabel("min");
        lbl_legMin.setFont(new Font("Dialog", Font.BOLD, 11));
        panel_llegenda_full.add(lbl_legMin, "cell 1 4,aligny bottom");
        panel_axis = new axis2D();
        panel_3.add(panel_axis, "cell 0 1,grow");

        panel = new JPanel();
        panel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Contrast", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
        plot2Ddialog.getContentPane().add(panel, "cell 0 0,grow");
        panel.setLayout(new MigLayout("", "[][grow][][][]", "[]"));
        
        txtMincontrast = new JTextField();
        panel.add(txtMincontrast, "cell 0 0");
        txtMincontrast.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtMincontrast_actionPerformed(e);
            }
        });
        txtMincontrast.setText("minContrast");
        txtMincontrast.setColumns(6);
        
        slider_contrast = new JSlider();
        panel.add(slider_contrast, "cell 1 0,growx");
        slider_contrast.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                do_slider_contrast_stateChanged(arg0);
            }
        });
        
         txtMaxcontrast = new JTextField();
         panel.add(txtMaxcontrast, "cell 2 0");
         txtMaxcontrast.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 do_txtMaxcontrast_actionPerformed(e);
             }
         });
         txtMaxcontrast.setText("maxContrast");
         txtMaxcontrast.setColumns(6);
        
        lblContrastValue = new JLabel("cvalue");
        panel.add(lblContrastValue, "cell 3 0");
        
        chckbxColor = new JCheckBox("color");
        panel.add(chckbxColor, "cell 4 0");
        chckbxColor.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent arg0) {
                do_chckbxColor_itemStateChanged(arg0);
            }
        });
        chckbxColor.setSelected(true);
        
        panel_4 = new JPanel();
        plot2Ddialog.getContentPane().add(panel_4, "cell 1 0 1 2,grow");
        panel_4.setLayout(new MigLayout("", "[][]", "[][][][][][][][][][][]"));
        
        btnFitToWindow = new JButton("Fit to window");
        panel_4.add(btnFitToWindow, "cell 0 0 2 1,growx");
        btnFitToWindow.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                do_btnFitToWindow_actionPerformed(arg0);
            }
        });
        
        btnFitY = new JButton("Fit Y");
        panel_4.add(btnFitY, "cell 0 1 2 1,growx");
        btnFitY.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_btnNewButton_actionPerformed(e);
            }
        });
        
        chckbxAlwaysFitY = new JCheckBox("Auto fit Y");
        panel_4.add(chckbxAlwaysFitY, "cell 0 2 2 1");
        chckbxAlwaysFitY.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                do_chckbxAlwaysFitY_itemStateChanged(e);
            }
        });
        
        chckbxSquareSelection = new JCheckBox("Square select");
        panel_4.add(chckbxSquareSelection, "cell 0 3 2 1");
        
        chckbxInvertOrder = new JCheckBox("Invert order");
        panel_4.add(chckbxInvertOrder, "cell 0 4 2 1");
        chckbxInvertOrder.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent arg0) {
                do_chckbxInvertOrder_itemStateChanged(arg0);
            }
        });
        
        chckbxShowPattNames = new JCheckBox("Patt. names");
        panel_4.add(chckbxShowPattNames, "cell 0 5");
        chckbxShowPattNames.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent arg0) {
                do_chckbxShowPattNames_itemStateChanged(arg0);
            }
        });
        
        lblColor = new JLabel("color");
        lblColor.setSize(new Dimension(15, 15));
        lblColor.setMaximumSize(new Dimension(15, 15));
        lblColor.setMinimumSize(new Dimension(15, 15));
        lblColor.setPreferredSize(new Dimension(15, 15));
        panel_4.add(lblColor, "cell 1 5,alignx left,aligny center");
        lblColor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                do_lblColor_mouseClicked(e);
            }
        });
        
        chckbxGridY = new JCheckBox("Grid Y");
        chckbxGridY.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_chckbxGridY_actionPerformed(e);
            }
        });
        panel_4.add(chckbxGridY, "cell 0 6");
        
        lblMaxwidth = new JLabel("Max name width");
        panel_4.add(lblMaxwidth, "cell 0 7");
        
        txtMaxwidth = new JTextField();
        panel_4.add(txtMaxwidth, "cell 1 7");
        txtMaxwidth.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtMaxwidth_actionPerformed(e);
            }
        });
        txtMaxwidth.setText("200");
        txtMaxwidth.setColumns(5);
        
        lblXPosition = new JLabel("Name X pos");
        panel_4.add(lblXPosition, "cell 0 8");
        
        txtXposition = new JTextField();
        panel_4.add(txtXposition, "cell 1 8");
        txtXposition.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtXposition_actionPerformed(e);
            }
        });
        txtXposition.setText("10");
        txtXposition.setColumns(5);
        
        panel_5 = new JPanel();
        panel_5.setBorder(new TitledBorder(null, "Axis", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_4.add(panel_5, "cell 0 9 2 1,grow");
        panel_5.setLayout(new MigLayout("", "[][grow]", "[][][]"));
        
        lblInix = new JLabel("iniX");
        panel_5.add(lblInix, "cell 0 0,alignx trailing");
        
        txtInix = new JTextField();
        txtInix.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtInix_actionPerformed(e);
            }
        });
        txtInix.setText("iniX");
        panel_5.add(txtInix, "cell 1 0,growx");
        txtInix.setColumns(5);
        
        lblIncx = new JLabel("incX");
        panel_5.add(lblIncx, "cell 0 1,alignx trailing");
        
        txtIncx = new JTextField();
        txtIncx.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtIncx_actionPerformed(e);
            }
        });
        txtIncx.setText("incX");
        panel_5.add(txtIncx, "cell 1 1,growx");
        txtIncx.setColumns(5);
        
        lblSubx = new JLabel("subX");
        panel_5.add(lblSubx, "cell 0 2,alignx trailing");
        
        txtSubx = new JTextField();
        txtSubx.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_txtSubx_actionPerformed(e);
            }
        });
        txtSubx.setText("subX");
        panel_5.add(txtSubx, "cell 1 2,growx");
        txtSubx.setColumns(5);
        
        btnSaveAsPng = new JButton("save as PNG");
        panel_4.add(btnSaveAsPng, "cell 0 10 2 1,growx");
        btnSaveAsPng.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_btnSaveAsPng_actionPerformed(e);
            }
        });
        
        panel_2 = new JPanel();
        plot2Ddialog.getContentPane().add(panel_2, "cell 0 2,grow");
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
        this.visible(true);
        nameXPos = getPanelImatge().getWidth()-nameMaxWidth-10;
        txtXposition.setText(Integer.toString(nameXPos));
        txtMaxwidth.setText(Integer.toString(nameMaxWidth));
    }

    //S'HA D'ADAPTAR
	public void setImagePatts(List<DataSerie> dss) {
	    if(dss == null)return;
	    if(dss.size()==0)return;
	
	    this.toPaint = dss;
	    
	    maxY = Double.MIN_VALUE;
	    minY = Double.MAX_VALUE;
	    meanY = 0;
	    
	    //calculem la min2t, max2t
	    for (int i=0;i<dss.size();i++){
	        double[] vals = dss.get(i).calcYmeanYDesvYmaxYmin(false);
	        if (vals[2]>maxY)maxY=vals[2];
	        if (vals[3]<minY)minY=vals[3];
	        meanY = meanY + vals[0];
	    }
	    meanY = meanY / dss.size();
	    nXPoints = dss.get(0).getNpoints();
	    
	    //les x tots els patterns haurien de coincidir
	    maxT2 = dss.get(0).getPointWithCorrections(dss.get(0).getNpoints()-1,false).getX();
	    minT2 = dss.get(0).getPointWithCorrections(0,false).getX();
	    
	    lblColor.setBackground(nameColor);
	    lblColor.setText("");
	    lblColor.setOpaque(true);

	    this.pintaImatge();
	    this.pintaLlegenda();
	    this.actualitzarVista();
	}

	public void actualitzarVista(){
	    this.getPanelImatge().repaint();
	}

	private void resetView() {
	    this.originX = 0;
	    this.originY = 0;
	    scalefitX = 0.0f;
	    scalefitY = 0.0f;
	}

	private void autoDivLines(){

        PlotPanel plot1D = D1Dplot_global.getD1Dmain().getPanel_plot(); //copiarem moltes coses de plot1D per l'eix
	    
	    //Aqui hauriem de posar divisions tal com volem des de MIN a MAX (ignorant finestra), després ja mostrarem la zona d'interès.
        this.div_startValX=this.xrangeMin;

        //ara cal veure a quan es correspon en les unitats de cada eix -- a la vista actual 
	    double xppix = this.getXunitsPerPixel();

	    txtSubx.setText(String.valueOf(plot1D.incXPrimPIXELS/plot1D.incXSecPIXELS));

	    this.div_incXPrim=plot1D.incXPrimPIXELS*xppix;
	    this.div_incXSec=plot1D.incXSecPIXELS*xppix;

	    this.txtIncx.setText(FileUtils.dfX_3.format(this.div_incXPrim));
	    this.txtInix.setText(FileUtils.dfX_3.format(this.div_startValX));
	}
    
	//ens diu quant en unitats de X val un pixel (ex 1 pixel es 0.01deg de 2th)
    private double getXunitsPerPixel(){
        return (this.xrangeMax-this.xrangeMin)/this.panelImatge.getWidth();
    }
    
    private void customDivLinesX(double incrPrincipals, double nDivisionsSecund){
        
      this.div_incXPrim=incrPrincipals;
      this.div_incXSec=incrPrincipals/nDivisionsSecund;
      
      this.txtIncx.setText(FileUtils.dfX_3.format(this.div_incXPrim));
      
 }
    
	// el pixel que entra est� al rang 0..n-1 donat un pixel px,py a quin punt x,y del JFrame est�
	private Point2D.Double getFramePointFromPixel(Point2D.Double px) {
	    double x = (px.x * scalefitX) + originX; //0.5 per posar-ho al centre del pixel
	    double y = (px.y * scalefitY) + originY;
	    return new Point2D.Double(x,y);
	}

	// segons la mida de la imatge actual, les coordenades d'un punt assenyalat amb el mouse correspondran a un pixel o
	// a un altre, aquesta subrutina ho corregeix: Donat el punt p on el mouse es troba te'l torna com a pixel de la imatge
	private Point2D.Double getPixel(Point2D.Double p) {
	    double x = (p.x - originX) / scalefitX;
	    double y = (p.y - originY) / scalefitY;
	    return new Point2D.Double(x,y);
	}

    private double getFrameXFromDataPointX(double xdpoint){
        
        int pixelImatgeCompleta = toPaint.get(0).getIndexOfDP(toPaint.get(0).getClosestDP_xonly(xdpoint, -1));
        double x = (pixelImatgeCompleta * scalefitX) + originX;
        return x;
    }
	
	private void pintaImatge() {        
        if (toPaint == null)return;
        if (toPaint.size()==0)return;

        int dimx = toPaint.get(0).getNpoints();
        int type = BufferedImage.TYPE_INT_ARGB;
        BufferedImage im = new BufferedImage(dimx, toPaint.size(), type);

        
        minValSlider = this.slider_contrast.getMinimum();
        valSlider = this.slider_contrast.getValue();

        for (int i = 0; i < im.getHeight(); i++) { // per cada fila (Y)
            for (int j = 0; j < im.getWidth(); j++) { // per cada columna (X)
                
                //calcular el color...
                //hem de mirar quina serie (i) quin punt (j)
                im.setRGB(j, i, this.getColorOfAPixel(j, i).getRGB());
            }
        }
        this.updateImage(im);
    }
    
    protected void do_txtInix_actionPerformed(ActionEvent e) {
        applyDivisions();
    }
    protected void do_txtIncx_actionPerformed(ActionEvent e) {
        applyDivisions();
    }
    protected void do_txtSubx_actionPerformed(ActionEvent e) {
        applyDivisions();
    }
	
    private void applyDivisions(){
      this.div_startValX=Double.parseDouble(txtInix.getText());
      this.customDivLinesX(Double.parseDouble(txtIncx.getText()), Double.parseDouble(txtSubx.getText()));
      this.actualitzarVista();        
  }
    
    private void pintaLlegenda() {
        if (toPaint == null)return;
        if (toPaint.size()==0)return;

        int maxVal = slider_contrast.getValue();
        int minVal = slider_contrast.getMinimum();
        int dimY = maxVal - minVal;
        //        int dimX = 1;
        int type = BufferedImage.TYPE_INT_ARGB;
        llegendaImg = new BufferedImage(1, dimY, type);
        float height = (float)panel_llegenda.getHeight();
        scalefitYllegenda = height /(float)dimY;

        int quarter = (int) (dimY/4.);
        lbl_legMax.setText(Integer.toString(maxVal)+" ");
        lbl_leg3Q.setText(Integer.toString(maxVal-quarter)+" ");
        lbl_leg2Q.setText("<html>\n<br>\n"+Integer.toString(maxVal-2*quarter)+" \n<br>\n<br>\n</html>");
        lbl_leg1Q.setText(Integer.toString(maxVal-3*quarter)+" ");
        lbl_legMin.setText(Integer.toString(minVal)+" ");
        
        for (int i = 0; i < llegendaImg.getHeight(); i++) { // per cada fila (Y)
            //en aquest cas nomes tenim una columna
            //al començar per dalt hem de començar pel maxim
            //hem de mirar quina serie (i) quin punt (j)
            Color col;
            if (this.isColor()){
                col = intensityColor(maxVal-i, maxY,minY, minValSlider,valSlider);
            }else{
                col = intensityBW(maxVal-i, maxY,minY,minValSlider,valSlider);
            }
            llegendaImg.setRGB(0, i, col.getRGB());
        }
        this.panel_llegenda.repaint();
    }
        
        
    
    private Color getColorOfAPixel(int jy, int ix){
        Color col;
        if (this.isColor()) {
            // pintem en color
            col = intensityColor(toPaint.get(ix).getPointWithCorrections(jy,false).getY(), maxY,minY, minValSlider,valSlider);
        } else {
            // pintem en BW
            col = intensityBW(toPaint.get(ix).getPointWithCorrections(jy,false).getY(), maxY,minY,minValSlider,valSlider);
        }
        return col;
    }
    
    /*
     * maxInt,minInt son maxim i minim d'intensitat de la imatge
     * minVal,maxVal corresponen a l'slide. MAX es el valor actual assenyalat per l'slide.
     * grafiques RGB entre minval i maxval amb punt inflexio a (maxval-minval)/2
     * intensitat normalitzada entre minval i maxval
     */
    private Color intensityColor(double intensity, double maxInt, double minInt, int minVal, int maxVal) {
                
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
            log.warning("Error in color generation");
        }
        return c;
    }
    
    //valor interpolat sobre una recta (fun=0) o una parabola (fun=1)
    private Color intensityBW(double intensity, double maxInt, double minInt,int minVal, int maxVal) {
        
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
    
    private void updateImage(BufferedImage i) {
        this.image = i;
        this.actualitzarVista();
    }
    
    private void fitImage() {
        // En aquest cas hem de fer encabir-ho a la finestra
        scalefitX = (float)getPanelImatge().getWidth() / (float)getImage().getWidth();
        scalefitY = (float)getPanelImatge().getHeight() / (float)getImage().getHeight();
        //aixo no cal centrar perque ara estem distorsionant la imatge, l'origen sempre sera 0 
        originX=0;
        originY=0;
        this.updateXrangeMinMax();
        this.actualitzarVista();
        
    }
    
    private void fitImageZone(Point2D.Double vertexIni, Point2D.Double vertexFin){
        //com hem pintat el quadrat (hi ha quatre variants)
        if (!this.isSquareSection()){
            vertexIni.y=0;
            vertexFin.y=getPanelImatge().getHeight();
        }
        
        //assignarem pini i pfin com el vertex superior esquerra i inferior dret respectivament
        Point2D.Double pini = null;
        Point2D.Double pfin = null;
        
        if (vertexIni.x>vertexFin.x){
            //hem pintat cap a l'esquerra
            if(vertexIni.y>vertexFin.y){
                //hem pintat cap amunt (inverteixo rols)
                pini = this.getPixel(vertexFin);
                pfin = this.getPixel(vertexIni);
            }else{
                //hem pintat cap avall
                pini = this.getPixel(new Point2D.Double(vertexFin.x,vertexIni.y));//x final y primer
                pfin = this.getPixel(new Point2D.Double(vertexIni.x,vertexFin.y));//x primer y final
            }
        }else{
            //hem pintat cap a la dreta
            if(vertexIni.y>vertexFin.y){
                //hem pintat cap amunt
                pini = this.getPixel(new Point2D.Double(vertexIni.x,vertexFin.y));//x primer y final
                pfin = this.getPixel(new Point2D.Double(vertexFin.x,vertexIni.y));//x final y primer
            }else{
                //hem pintat cap avall (CAS NORMAL!!)
                pini = this.getPixel(vertexIni);
                pfin = this.getPixel(vertexFin);
            }
        }
        double sizeX = FastMath.abs(FastMath.abs(pfin.x) - FastMath.abs(pini.x));
        double sizeY = FastMath.abs(FastMath.abs(pfin.y) - FastMath.abs(pini.y));
        scalefitX = (float)(getPanelImatge().getWidth() / sizeX);
        scalefitY = (float)(getPanelImatge().getHeight() / sizeY);
        //movem origen
        Point2D.Double piniNew = getFramePointFromPixel(pini);
        originX = originX + (int) FastMath.round(-piniNew.x);
        originY = originY + (int) FastMath.round(-piniNew.y);
        this.updateXrangeMinMax();
    }

    //prova utilitzant scalefit
    private Rectangle calcSubimatgeDinsFrame() {
        Point2D.Double startCoords = getPixel(new Point2D.Double(1, 1));
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
    
	// es mou l'origen a traves d'un increment de les coordenades
	private void moveOrigin(double incX, double incY, boolean repaint) {
	    // assignem un nou origen de la imatge amb un increment a les coordenades anteriors
	    //  (util per moure'l fen drag del mouse)
	    originX = originX + (int)FastMath.round(incX);
	    if(!isAlwaysFitY())originY = originY + (int)FastMath.round(incY);
	    this.updateXrangeMinMax();
	    if (repaint) {
	        this.actualitzarVista();
	    }
	}

	// al fer zoom es canviara l'origen i l'escala de la imatge
	private void zoom(boolean zoomIn, Point2D.Double centre) {
	    Point2D.Double mousePosition = new Point2D.Double(centre.x, centre.y);
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
	    originX = originX + (int)FastMath.round(mousePosition.x - centre.x);
	    originY = originY + (int)FastMath.round(mousePosition.y - centre.y);
	    this.updateXrangeMinMax();
	    this.actualitzarVista();
	}

	private void updateXrangeMinMax() {
	    
	    Point2D.Double pix0 = this.getPixel(new Point2D.Double(0,0));
	    Point2D.Double pix1 = this.getPixel(new Point2D.Double(panelImatge.getWidth(),0));
	    
	    if (pix0.x >= 0) {
	        this.xrangeMin = toPaint.get(0).getPointWithCorrections((int)pix0.x, false).getX();    
	    }
	    if (pix1.x<=nXPoints) {
	        this.xrangeMax = toPaint.get(0).getPointWithCorrections((int)pix1.x-1, false).getX();    
	    }
	    
	}
	
	private void savePNG(File fpng, float factor){

      //creem les tres imatges
	    BufferedImage img = new BufferedImage(
	            (int)(panelImatge.getSize().width*factor),
	            (int)(panelImatge.getSize().height*factor),
	            BufferedImage.TYPE_INT_ARGB);

	    BufferedImage imgL = new BufferedImage(
	            (int)(panel_llegenda_full.getSize().width*factor),
	            (int)(panel_llegenda_full.getSize().height*factor),
	            BufferedImage.TYPE_INT_ARGB);
	    
	    BufferedImage imgAx = new BufferedImage((int)(panel_axis.getSize().width*factor),(int)(panel_axis.getSize().height*factor),BufferedImage.TYPE_INT_ARGB);
	    
      Graphics2D g2d = img.createGraphics();
      Graphics2D g2dL = imgL.createGraphics();
      Graphics2D g2dA = imgAx.createGraphics();
      
      g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
      g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
      g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
      g2d.setTransform(AffineTransform.getScaleInstance(factor, factor));
      g2dL.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      g2dL.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2dL.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      g2dL.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
      g2dL.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
      g2dL.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      g2dL.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g2dL.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
      g2dL.setTransform(AffineTransform.getScaleInstance(factor, factor));
      g2dA.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      g2dA.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2dA.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      g2dA.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
      g2dA.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
      g2dA.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      g2dA.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g2dA.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
      g2dA.setTransform(AffineTransform.getScaleInstance(factor, factor));
      
      panelImatge.paintComponent(g2d);
      panel_llegenda_full.paintComponents(g2dL);
      panel_axis.paintComponent(g2dA);
      
      BufferedImage imgTOT = joinBufferedImageHorizontal(joinBufferedImageVertical(img,imgAx,0),imgL,0);
      g2d.dispose();
      g2dL.dispose();
      g2dA.dispose();
      
      try {
          ImageIO.write(imgTOT, "png", fpng);
      } catch (Exception ex) {
          log.warning("Error writting PNG image");
      }
      log.info(fpng.toString()+" written");
  }
	
    private BufferedImage joinBufferedImageVertical(BufferedImage img1,BufferedImage img2, int offset) {
        //do some calculate first
        int height = img1.getHeight()+img2.getHeight()+offset;
        int width = FastMath.max(img1.getWidth(), img2.getWidth());
        //create a new buffer and draw two image into the new image
        BufferedImage newImage = new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = newImage.createGraphics();
        
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        
        Color oldColor = g2.getColor();
        //fill background
        g2.setPaint(Color.WHITE);
        g2.fillRect(0, 0, width, height);
        //draw image
        g2.setColor(oldColor);
        g2.drawImage(img1, null, 0, 0);
        g2.drawImage(img2, null, 0, img1.getHeight()+offset);
        g2.dispose();
        return newImage;
    }
    
	
    private BufferedImage joinBufferedImageHorizontal(BufferedImage img1,BufferedImage img2, int offset) {
        //do some calculate first
        int width = img1.getWidth()+img2.getWidth()+offset;
        int height = FastMath.max(img1.getHeight(),img2.getHeight());
        //create a new buffer and draw two image into the new image
        BufferedImage newImage = new BufferedImage(width,height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = newImage.createGraphics();
        
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        
        Color oldColor = g2.getColor();
        //fill background
        g2.setPaint(Color.WHITE);
        g2.fillRect(0, 0, width, height);
        //draw image
        g2.setColor(oldColor);
        g2.drawImage(img1, null, 0, 0);
        g2.drawImage(img2, null, img1.getWidth()+offset, 0);
        g2.dispose();
        return newImage;
    }
    
    private void do_panelImatge_mouseWheelMoved(MouseWheelEvent e) {
	    Point2D.Double p = new Point2D.Double(e.getPoint().x, e.getPoint().y);
	    boolean zoomIn = (e.getWheelRotation() < 0);
	    this.zoom(zoomIn, p); //ja fa actualitzar
	}

	// Identificar el bot� i segons quin sigui moure o fer zoom
	private void do_panelImatge_mousePressed(MouseEvent arg0) {
	    this.dragPoint = new Point2D.Double(arg0.getPoint().x, arg0.getPoint().y);
        this.clickPoint = new Point2D.Double(arg0.getPoint().x, arg0.getPoint().y);
	    if (arg0.getButton() == MOURE) {
	        this.mouseDrag = true;
	    }
	    if (arg0.getButton() == ZOOM_BORRAR) {
	        this.zoomPoint = new Point2D.Double(arg0.getPoint().x, arg0.getPoint().y);
	        this.mouseZoom = true;
	    }
	    if (arg0.getButton() == CLICAR) {
	        this.mouseBox = true;
	    }
	    actualitzarVista();
	}

	private void do_panelImatge_mouseReleased(MouseEvent e) {
	    if (e.getButton() == MOURE) {
	        this.mouseDrag = false;
	        Point2D.Double currentPoint = new Point2D.Double(e.getPoint().x, e.getPoint().y);
	        if ((FastMath.abs(this.clickPoint.x-currentPoint.x)<0.5) && (FastMath.abs(this.clickPoint.y-currentPoint.y)<0.5)){
	            this.fitImage();
	        }
	    }
	    if (e.getButton() == ZOOM_BORRAR)
	        this.mouseZoom = false;
	    if (e.getButton() == CLICAR){
	        Point2D.Double p = new Point2D.Double(e.getPoint().x, e.getPoint().y);
	        this.mouseBox = false;
	        //DO ZOOM considerant quadrat dragpoint i p
	        fitImageZone(dragPoint,p);
	    }
	    actualitzarVista();
	}

	private void do_panelImatge_mouseDragged(MouseEvent e) {
	    if (this.mouseDrag == true) { //& this.toPaint!=null cal?
	        Point2D.Double p = new Point2D.Double(e.getPoint().x, e.getPoint().y);
	        double incX, incY;
	        // agafem el dragpoint i l'actualitzem
	        incX = (p.x - dragPoint.x);
	        incY = (p.y - dragPoint.y);
	        this.dragPoint = p;
	        this.moveOrigin(incX, incY, true);    
	    }
	    if (this.mouseZoom == true) {
	        Point2D.Double p = new Point2D.Double(e.getPoint().x, e.getPoint().y);
	        double incY;
	        incY = p.y - dragPoint.y;
	        this.dragPoint = p;
	        boolean zoomIn = (incY < 0);
	        this.zoom(zoomIn, zoomPoint);
	    }
	    if (this.mouseBox == true) {
	        this.currentMousePoint = new Point2D.Double(e.getPoint().x, e.getPoint().y);
	    }
	
	    actualitzarVista();
	
	}

    private void do_panelImatge_mouseMoved(MouseEvent e) {
	    // he de normalitzar les coordenades a la mida de la imatge en pixels
	    this.currentMousePoint = new Point2D.Double(e.getPoint().x, e.getPoint().y);
	    if (toPaint == null)return;
	    if (toPaint.size()==0)return;
	    
	    Point2D.Double pix = this.getPixel(currentMousePoint);
	    if (pix.x < 0 || pix.y < 0 || pix.x >= nXPoints || pix.y >= toPaint.size()) {
	        return;
	    }
	    
	    int serie = (int)pix.y;
	    int punt = (int)pix.x;
	    
	    double t2 = toPaint.get(serie).getPointWithCorrections(punt,false).getX();
	    double inten = toPaint.get(serie).getPointWithCorrections(punt,false).getY();
	    
	    lblPunt.setText(String.format("Pattern: %s   2"+D1Dplot_global.theta+"= %.4f   Intensity=%.2f" ,toPaint.get(serie).serieName,t2,inten));
	    
	}

	private void do_slider_contrast_stateChanged(ChangeEvent arg0) {
	        lblContrastValue.setText(Integer.toString(slider_contrast.getValue()));
	        this.pintaImatge(); //ja conte actualitzar vista
	        this.pintaLlegenda();
	   }

	private void do_btnNewButton_actionPerformed(ActionEvent e) {
	    scalefitY = (float)getPanelImatge().getHeight() / (float)getImage().getHeight();
	    originY=0;
	    this.actualitzarVista();
	}

	private void do_btnFitToWindow_actionPerformed(ActionEvent arg0) {
	    this.fitImage();
	    this.pintaLlegenda();
	}

	private void do_txtMaxcontrast_actionPerformed(ActionEvent e) {
	    try{
	        int val = Integer.parseInt(txtMaxcontrast.getText());
	        slider_contrast.setMaximum(val);
	    }catch(Exception ex){
	        log.warning("Error setting maximum contrast");
	    }
	}

	private void do_txtMincontrast_actionPerformed(ActionEvent e) {
	    try{
	        int val = Integer.parseInt(txtMincontrast.getText());
	        slider_contrast.setMinimum(val);
	    }catch(Exception ex){
            log.warning("Error setting minimum contrast");
	    }
	}

	
	private void do_btnSaveAsPng_actionPerformed(ActionEvent e) {
	        File fpng = FileUtils.fchooserSaveNoAsk(plot2Ddialog, new File(D1Dplot_global.getWorkdir()), null,"png"); //ja preguntem despres
	        if (fpng!=null){
	            int w = panelImatge.getSize().width+panel_llegenda.getSize().width;
	            int h = panelImatge.getSize().height;
	            String s = (String)JOptionPane.showInputDialog(
	            		plot2Ddialog,
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
	                    log.warning("Error reading factor");
	                }
	                if(isDebug())log.writeNameNumPairs("config", true, "factor", factor);
	                this.savePNG(fpng,factor);
	            }
	        }
	    }

	private void do_chckbxShowPattNames_itemStateChanged(ItemEvent arg0) {
	    this.actualitzarVista();
	}

    protected void do_chckbxGridY_actionPerformed(ActionEvent e) {
        this.gridY=chckbxGridY.isSelected();
        this.actualitzarVista();
    }
	
	private void do_txtMaxwidth_actionPerformed(ActionEvent e) {
	    try{
	        nameMaxWidth = Integer.parseInt(txtMaxwidth.getText());
	        this.actualitzarVista();
	    }catch(Exception ex){
	        log.warning("Error reading maxwidth");
	    }
	}

	private void do_txtXposition_actionPerformed(ActionEvent e) {
	    try{
	        nameXPos = Integer.parseInt(txtXposition.getText()); 
	        this.actualitzarVista();
	    }catch(Exception ex){
	        log.warning("Error reading maxwidth");
	    }
	}

	private void do_lblColor_mouseClicked(MouseEvent e) {
	    Color newColor = JColorChooser.showDialog(
	    		plot2Ddialog,
	            "Choose Pattern Names Color",
	            getNameColor());
	       if(newColor != null){
	           setNameColor(newColor);
	           lblColor.setBackground(newColor);
	       }
	}

	private void do_chckbxColor_itemStateChanged(ItemEvent arg0) {
        this.pintaImatge();
        this.pintaLlegenda();
    }
    
    private void do_chckbxInvertOrder_itemStateChanged(ItemEvent arg0) {
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
    private void do_chckbxAlwaysFitY_itemStateChanged(ItemEvent e) {
        if (chckbxAlwaysFitY.isSelected()) btnFitY.doClick();
    }
    
    public boolean isAlwaysFitY(){
	    return chckbxAlwaysFitY.isSelected();
	}

	public boolean isPosaTitols(){
	    return chckbxShowPattNames.isSelected();
	}

	public boolean isColor() {
	    return chckbxColor.isSelected();
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

	public dades2d getPanelImatge() {
	    return panelImatge;
	}

	public void setPanelImatge(dades2d panelImatge) {
	    this.panelImatge = panelImatge;
	}

	public List<DataSerie> getToPaint() {
	    return toPaint;
	}

	public void setToPaint(List<DataSerie> toPaint) {
	    this.toPaint = toPaint;
	}

	public boolean isSquareSection(){
	    return chckbxSquareSelection.isSelected();
	}

	public BufferedImage getLlegenda() {
	    return llegendaImg;
	}

	public void setLlegenda(BufferedImage llegenda) {
	    this.llegendaImg = llegenda;
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


	private boolean isDebug(){
	    return D1Dplot_global.isDebug();
	}

	public void visible(boolean vis) {
    	this.plot2Ddialog.setVisible(vis);
    	
    }
	
    private boolean checkIfDiv(){
        if (this.div_incXPrim == 0) return false;
        if (this.div_incXSec == 0) return false;
        return true;
    }

	public class dades2d extends JPanel {
	
	    private static final long serialVersionUID = 1L;
	    
	    public dades2d(){
	        super();
	    }
	    
	    @Override
	    protected void paintComponent(Graphics g) {
	        super.paintComponent(g);
	        
	        Graphics2D g2 = (Graphics2D) g;
	        
            g2.setBackground(Color.white);
            g2.clearRect(0, 0, this.getWidth(),this.getHeight());
	        
            g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
	        
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
	            } catch (Exception e) {
	                log.warning("Error getting the subImage");
	            }
	            AffineTransform t = new AffineTransform();
	            float offsetX = originX % scalefitX;
	            if (originX>0)offsetX = originX;
	            float offsetY = originY % scalefitY;
	            if (originY>0)offsetY = originY;
	            t.translate(offsetX, offsetY);
	            t.scale(scalefitX, scalefitY);
	            g2.drawImage(getSubimage(), t, null);
	            
	            if (mouseBox) {
	                if(isSquareSection()){
	                    //un quadrat
	                    dibuixarQuadrat(g2);
	                }else{
	                    //dibueixem amb fitY
	                    dibuixarSemiQuadrat(g2);
	                }
	            }
	            
	            if (isPosaTitols()){
	                writeTitles(g2);
	            }
	            
	            panel_axis.repaint();
	            
                if(gridY){
                    BasicStroke dashed = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[]{1,2}, 0);
                    g2.setStroke(dashed);
                    double xval = div_startValX;
                    while (xval <= xrangeMax){
                        if (xval >= xrangeMin){ //la pintem nomes si estem dins el rang, sino numés icrementem el num de divisions
                            double xvalPix = getFrameXFromDataPointX(xval);
                            Line2D.Double ld = new Line2D.Double(xvalPix,0,xvalPix,this.getHeight());
                            g2.draw(ld);
                        }
                        xval = xval + div_incXSec;
                    }
                }
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
	            double pattD = getPixel(new Point2D.Double(this.getWidth()/2,i)).getY();
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
	                String s =  toPaint.get(currentPatt).serieName;
	                
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
	        String s =  toPaint.get(currentPatt).serieName;
	        
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

	public class llegenda2D extends JPanel {
	
	        private static final long serialVersionUID = 1L;
	        
	        public llegenda2D(){
	            super();
	        }
	        
	        @Override
	        protected void paintComponent(Graphics g) {
	            super.paintComponent(g);
                if (getImage() == null)return;
	            
	            Graphics2D g2 = (Graphics2D) g;
	            
	            g2.setBackground(Color.white);
	            g2.clearRect(0, 0, this.getWidth(),this.getHeight());
	            
	            g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
	            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	            g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
	            g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
	            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
	            
	            if (getLlegenda() != null) {
	                
	                AffineTransform t = new AffineTransform();
	                
	                t.scale(panel_llegenda.getWidth(), scalefitYllegenda);
	                g2.drawImage(getLlegenda(), t, null);
	                
	                g2.dispose();
	                
	            }
	        }
	    }
	
	public class axis2D extends JPanel {
        private static final long serialVersionUID = 1L;
        
        public axis2D(){
            super();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (getImage() == null)return;
            if ((scalefitX <= 0) || (scalefitY <= 0))return; //encara no s'ha pintat
                
            BufferedImage axis_Image = new BufferedImage((int)(this.getWidth()), (int)(this.getHeight()), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = axis_Image.createGraphics();
            FontRenderContext frc = g2.getFontRenderContext();
            BasicStroke stroke = new BasicStroke(1.0f);
            g2.setStroke(stroke);
            g2.setColor(Color.BLACK);
            
            //TEST PINTAR BLANC EL FONS PER VEURE MIDA
            g2.setBackground(Color.white);
            g2.clearRect(0, 0, this.getWidth(),this.getHeight());
            
            
            g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            if(!checkIfDiv()) {
                updateXrangeMinMax();
                autoDivLines();
            }
            
            //REMEMBER 25 pixels d'alt
            int AxisLabelsPadding =2;
            PlotPanel plot1D = D1Dplot_global.getD1Dmain().getPanel_plot(); //copiarem moltes coses de plot1D per l'eix
            float def_axisL_fsize=plot1D.def_axisL_fsize;
            int div_PrimPixSize = plot1D.getDiv_PrimPixSize();
            int div_SecPixSize = plot1D.getDiv_SecPixSize();
            
            //PINTEM ELS TITOLS DELS EIXOS
            
            // X-axis (abcissa) label.
            String xlabel = plot1D.getXlabel();
            TextLayout xLabelTextLayout = new TextLayout(xlabel, g2.getFont().deriveFont(g2.getFont().getSize()+def_axisL_fsize), frc);
            double sw = xLabelTextLayout.getBounds().getWidth();
            double sh = xLabelTextLayout.getBounds().getHeight();
            double sy = this.getHeight() - AxisLabelsPadding;
            double sx = (this.getWidth() - sw)/2;
            xLabelTextLayout.draw(g2, (float)sx,(float)sy);

            //pintem eix
            Point2D.Double vxleft = new Point2D.Double(0,div_PrimPixSize/2.f);
            Point2D.Double vxright = new Point2D.Double(this.getWidth(),div_PrimPixSize/2.f);
            Line2D.Double abcissa = new Line2D.Double(vxleft, vxright);  //X axis horizontal
            g2.draw(abcissa);
            
            //Per tots els punts les coordenades Y seran les mateixes
            double yiniPrim = 0; 
            double yfinPrim = div_PrimPixSize;
            double yiniSec = (div_PrimPixSize/2.f) - (div_SecPixSize/2.f); 
            double yfinSec = (div_PrimPixSize/2.f) + (div_SecPixSize/2.f);
            
            int ndiv = (int)FastMath.round(div_incXPrim/div_incXSec);
            int idiv = 0;
            double xval = div_startValX;
            
            
            while (xval <= xrangeMax){
                if (xval >= xrangeMin){ //la pintem nomes si estem dins el rang, sino numés icrementem el num de divisions
                    double xvalPix = getFrameXFromDataPointX(xval);
                    if (idiv%ndiv==0) {
                        //primaria: linia llarga + label
                        Line2D.Double l = new Line2D.Double(xvalPix,yiniPrim,xvalPix,yfinPrim);
                        g2.draw(l);
                        //ara el label sota la linia
                        
                        String s = plot1D.getGraphPanel().getDef_xaxis_format().format(xval);
                        TextLayout valLayout = new TextLayout(s, g2.getFont().deriveFont(g2.getFont().getSize()+plot1D.def_axis_fsize), frc);
                        sw = valLayout.getBounds().getWidth();
                        sh = valLayout.getBounds().getHeight();
                        double xLabel = xvalPix - sw/2f; //el posem centrat a la linia
                        double yLabel = yfinPrim + AxisLabelsPadding + sh;
                        valLayout.draw(g2, (float)xLabel, (float)yLabel);
                    }else {
                        //secundaria: linia curta
                        Line2D.Double l = new Line2D.Double(xvalPix,yiniSec,xvalPix,yfinSec);
                        g2.draw(l);
                    }
                }
                xval = xval + div_incXSec;
                idiv = idiv + 1;
            }
            g.drawImage(axis_Image,0,0,null);
            g2.dispose();
        }
	}


}

package com.vava33.d1dplot;

/**
 * D1Dplot
 * 
 * Implementation of Plot1DFrontend via extension of
 * BasicPlotPanelFrontEnd from com.vava33.BasicPlotPanel
 *
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */


import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

import com.vava33.BasicPlotPanel.BasicPlotPanelFrontEnd;
import com.vava33.BasicPlotPanel.core.Plottable_point;
import com.vava33.BasicPlotPanel.core.SerieType;
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.d1dplot.data.Xunits;
import com.vava33.jutils.FileUtils;
import com.vava33.jutils.Options;
import com.vava33.jutils.VavaLogger;

public class XRDPlotPanelFrontEnd extends BasicPlotPanelFrontEnd<XRDPlot1DPanel> {

    private static final long serialVersionUID = 1L;
    private D1Dplot_data dades;
    
    public static String getClassName() {
        return "XRDPlotPanelFrontEnd";
    }
    
    public XRDPlotPanelFrontEnd(Options opt, XRDPlot1DPanel plot1Dpanel,VavaLogger logger) {
        super(opt,plot1Dpanel,logger);
        this.dades=plot1Dpanel.getDataToPlot();
    }
    
    private boolean arePlottables() {
        return dades.arePlottables();
    }
    
    @Override
    protected void inicia() {
        graphPanel.setYlabel("Intensity");
        graphPanel.setXlabel("");
        super.inicia();
    }

    @Override
    protected void do_graphPanel_mouseMoved(MouseEvent e) {
        if (arePlottables()){
            Point2D.Double dp = this.graphPanel.getDataPointFromFramePoint(new Point2D.Double(e.getPoint().x, e.getPoint().y));
            if (dp!=null){

                //get the units from first pattern that is plotted
                DataSerie ds = dades.getFirstPlottedDataSerie();
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
                //                lblTth.setText(String.format(" %s%.4f%s %s%.1f%s", Xpref,dtth,Xunit,Ypref,dp.getY(),Yunit));
                lblTth.setText(String.format("%s%.4f%s", Xpref,dtth,Xunit,Ypref,dp.getY(),Yunit));
                lblInten.setText(String.format("%s%.1f%s", Ypref,dp.getY(),Yunit));
                double wl = ds.getWavelength();
                if((wl>0)&&(ds.getxUnits()==Xunits.tth)){
                    //mirem si hi ha wavelength i les unitats del primer son tth
                    double dsp = wl/(2*FastMath.sin(FastMath.toRadians(dtth/2.)));
                    //                  lblDsp.setText(String.format(" [dsp=%.4f"+D1Dplot_global.angstrom+"]", dsp));
                    lblDsp.setText(String.format("dsp=%.4f"+D1Dplot_global.angstrom, dsp));
                }else{
                    lblDsp.setText("");
                }

                //totes les hkl (ja les desactivarem si no volem veure'ls) -- EDIT no ho poso com a opcio, sempre es ploteja
//                if (graphPanel.isHkllabels()){
                    List<Plottable_point> dhkl = new ArrayList<Plottable_point>();
                    for (DataSerie p: dades.getAllSeriesByType(SerieType.hkl)) {
                        if (!p.isPlotThis())continue;
                        double tol = FastMath.min(10*graphPanel.getXunitsPerPixel(), 0.10);//provem el minim entre 10 pixels o 0.025º 2th -- TODO es pot treure d'alguna forma el getXunitsPerPixel i fer-lo privat?
                        dhkl.addAll(p.getClosestPointsToAGivenX(dtth, tol));
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
                            shkl.append(hkl.getLabel()).append("; ");
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
//                }else{
//                    lblHkl.setText("");
//                }
            }
        }
    }

    // Identificar el bot� i segons quin sigui moure o fer zoom
    @Override
    protected void do_graphPanel_mousePressed(MouseEvent arg0) {
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
            if(graphPanel.isSelectingBkgPoints()){
                graphPanel.addBkgEstimPoint(this.dragPoint);
            }else if(graphPanel.isDeletingBkgPoints()){
                graphPanel.removeBkgEstimPoint(this.dragPoint);
            }else if(graphPanel.isSelectingPeaks()){
                if(dades.isOneSerieSelected()){
                    graphPanel.addPeakToDataSet(this.dragPoint);     
                }
            }else if(graphPanel.isDeletingPeaks()){
                if(dades.isOneSerieSelected()){
                    graphPanel.removePeakFromDataSet(this.dragPoint);
                }
            }else{
                if(this.shiftPressed){
                    this.sqSelect=true;
                }else{
                    this.sqSelect=false;
                }
                this.mouseDrag = true;
                graphPanel.zoomRect = null; //reiniciem rectangle
                graphPanel.mouseBox = true;
            }
        }
//        this.actualitzaPlot();//TODO comprovar si cal ja que cada cosa que cridem a graphPanel ja actualitza 

    }
    @Override
    protected void do_graphPanel_mouseReleased(MouseEvent e) {

        if (e.getButton() == MOURE){
            this.mouseDrag = false;
            this.mouseMove = false;
            Point2D.Double currentPoint = new Point2D.Double(e.getPoint().x, e.getPoint().y);
            if ((FastMath.abs(this.clickPoint.x-currentPoint.x)<0.5) && (FastMath.abs(this.clickPoint.y-currentPoint.y)<0.5)){
                graphPanel.fitGraph();
            }
        }
        if (e.getButton() == ZOOM_BORRAR){
            this.mouseDrag = false;
            this.mouseZoom = false;            
        }
        if (e.getButton() == CLICAR){
            graphPanel.mouseBox=false;
        }
        if (!arePlottables())return;

        if (e.getButton() == CLICAR) {
            //comprovem que no s'estigui fent una altra cosa          
            if(graphPanel.isSelectingBkgPoints()||graphPanel.isDeletingBkgPoints())return;
            if(graphPanel.isSelectingPeaks()||graphPanel.isDeletingPeaks())return;

            //COMPROVEM QUE HI HAGI UN MINIM D'AREA ENTREMIG (per evitar un click sol)
            if (FastMath.abs(e.getPoint().x-dragPoint.x)<minZoomPixels)return;
            if (this.sqSelect)if (FastMath.abs(e.getPoint().y-dragPoint.y)<minZoomPixels)return;

            Point2D.Double dataPointFinal = graphPanel.getDataPointFromFramePoint(new Point2D.Double(e.getPoint().x, e.getPoint().y));
            Point2D.Double dataPointInicial = graphPanel.getDataPointFromFramePoint(dragPoint);

            if (dataPointFinal == null && dataPointInicial==null){//els dos punts a fora
                return;
            }

            if (dataPointFinal == null){
                dataPointFinal = graphPanel.getDataPointFromFramePoint(new Point2D.Double(graphPanel.checkFrameXValue(e.getPoint().x),graphPanel.checkFrameYValue(e.getPoint().y)));
            }
            if (dataPointInicial==null){
                dataPointInicial = graphPanel.getDataPointFromFramePoint(new Point2D.Double(graphPanel.checkFrameXValue(dragPoint.x),graphPanel.checkFrameYValue(dragPoint.y)));
            }

            if (dataPointFinal == null || dataPointInicial==null){//algun punt final encara a fora!
                return;
            }
            double xrmin = FastMath.min(dataPointFinal.x, dataPointInicial.x);
            double xrmax = FastMath.max(dataPointFinal.x, dataPointInicial.x);
            if (this.sqSelect){
                double yrmin = FastMath.min(dataPointFinal.y, dataPointInicial.y);
                double yrmax = FastMath.max(dataPointFinal.y, dataPointInicial.y);
                graphPanel.applyWindowLimits(xrmin,xrmax,yrmin,yrmax);//ja fa actualitzar Plot
            }else {
                graphPanel.applyWindowLimitsX(xrmin,xrmax); //ja fa actualitzar Plot
            }
        }
        this.sqSelect=false;
    }

    //especifiques
    @Override
    public void readOptions(Options opt) {
        if (opt==null)return;
        super.readOptions(opt);
        this.graphPanel.colorDBcomp = opt.getValAsColor("colorDBcomp", this.graphPanel.colorDBcomp);
    }
    @Override
    public Options createOptionsObject() {
        Options opt = super.createOptionsObject();
        opt.put("colorDBcomp", FileUtils.getColorName(this.graphPanel.colorDBcomp));
        return opt;
    }
    
//    @Override
//    protected void do_txtXtitle_actionPerformed(ActionEvent e) {
//        if (txtXtitle.getText().isEmpty()){
//            graphPanel.setCustomXtitle(false);
//            this.dades.updateFullTable(); //aplica el label X
//        }else{
//            graphPanel.setXlabel(txtXtitle.getText());
//            graphPanel.setCustomXtitle(true);
//        }   
//        graphPanel.actualitzaPlot();
//    }
}





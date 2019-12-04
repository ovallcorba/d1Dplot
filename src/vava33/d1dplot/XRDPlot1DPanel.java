package com.vava33.d1dplot;

/**
 * D1Dplot
 * 
 * Implementation of Plot1Dpanel
 * from com.vava33.BasicPlotPanel.core
 *
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import com.vava33.BasicPlotPanel.core.Plot1DPanel;
import com.vava33.BasicPlotPanel.core.Plottable;
import com.vava33.BasicPlotPanel.core.Plottable_point;
import com.vava33.BasicPlotPanel.core.SerieType;
import com.vava33.d1dplot.data.DataPoint;
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.d1dplot.data.DataSet;
import com.vava33.d1dplot.data.Xunits;

public class XRDPlot1DPanel extends Plot1DPanel<D1Dplot_data> {

    private static final long serialVersionUID = 1L;
    private boolean showPeakThreshold = false;
    private boolean showEstimPointsBackground = false;
    private boolean selectingBkgPoints = false;
    private boolean deletingBkgPoints = false;
    private boolean selectingPeaks = false;
    private boolean deletingPeaks = false;
    private boolean showDBCompound = false;
    private boolean showIndexSolution = false;

    //series "propies" i arraylist de les seleccionades -- AUXILIARY DATASERIES
    protected DataSerie bkgseriePeakSearch; //threshold del background
    protected DataSerie bkgEstimP; //threshold del background
    protected DataSerie indexSolution;
    protected DataSerie dbCompound;
    protected Color colorDBcomp = Color.blue;

     
    public XRDPlot1DPanel(D1Dplot_data data) {
        super(data);
        bkgseriePeakSearch=new DataSerie(SerieType.bkg,Xunits.none,null); // millorable
        bkgEstimP=new DataSerie(SerieType.bkgEstimP,Xunits.none,null);// millorable
    }
    
    public void addBkgEstimPoint(Point2D.Double framePoint) {
        Point2D.Double dp = this.getDataPointFromFramePoint(framePoint);
        this.bkgEstimP.addPoint(new DataPoint(dp.x,dp.y,0,bkgEstimP));
        this.actualitzaPlot();
    }
    
    public void removeBkgEstimPoint(Point2D.Double framePoint) {
        Point2D.Double dp = this.getDataPointFromFramePoint(framePoint);
        Plottable_point toDelete = this.bkgEstimP.getClosestPointXY(new DataPoint(dp.x,dp.y,0,null),-1,-1,this.isPlotwithbkg());
        if (toDelete!=null){
            this.bkgEstimP.removePoint(toDelete);
        }
        this.actualitzaPlot();
    }
    
    public void addPeakToDataSet(Point2D.Double framePoint) {
      //agafar com a pic la 2theta clicada pero amb la intensitat del punt mes proper
        Point2D.Double dp = this.getDataPointFromFramePoint(framePoint);
        DataSet dset = this.getDataToPlot().getFirstSelectedDataSerie().getParent();
        DataSerie ds = dset.getFirstDataSerieByType(SerieType.peaks);
        if (ds==null) {
            ds = new DataSerie(dset.getMainSerie(),SerieType.peaks,false);
            this.getDataToPlot().addDataSerie(ds, dset, false, true, false);
        }
        ds.addPoint(new DataPoint(dp.x,dp.y,0,ds));
        this.actualitzaPlot();
    }

    public void removePeakFromDataSet(Point2D.Double framePoint) {
        Point2D.Double dp = this.getDataPointFromFramePoint(framePoint);
        DataSet dset = this.getDataToPlot().getFirstSelectedDataSerie().getParent();
        DataSerie ds = dset.getFirstDataSerieByType(SerieType.peaks);
        if (ds!=null) {
            Plottable_point toDelete = ds.getClosestPointXY(new DataPoint(dp.x,dp.y,0,null),-1,-1,this.isPlotwithbkg());
            if (toDelete!=null){
                ds.removePoint(toDelete);
            }
        }
        this.actualitzaPlot();
    }

    @Override
    protected void customPaintBeforeData(Graphics2D g2) {
        DataSerie ds = getDataToPlot().getFirstPlottedDataSerie();
        if ((ds!=null)&&(!this.isCustomXtitle())) {
            this.setXlabel(ds.getxUnits().getName());
        }
        System.out.println("paint called");
    }
    
    @Override
    protected void customPaintAfterData(Graphics2D g2) {
//        super.customPaint(g1);
       
        if (showPeakThreshold){
            //mostrar el fons pel pksearch
            if (bkgseriePeakSearch!=null) {
                if (bkgseriePeakSearch.getNPoints()>0) {
                    bkgseriePeakSearch.setSerieType(SerieType.bkg); //obliguem tipus serie bkg per pintar linia rosa
                    drawPattern(g2,bkgseriePeakSearch,bkgseriePeakSearch.getColor());
                }    
            }
        }
        
        if (showEstimPointsBackground) {
            //mostrem dataserie dels punts fons
            if (bkgEstimP!=null) {
                if (bkgEstimP.getNPoints()>0) {
                    bkgseriePeakSearch.setSerieType(SerieType.bkgEstimP); //obliguem tipus serie per markers size i color
                    drawPattern(g2,bkgEstimP,bkgEstimP.getColor());
                }
            }
        }
        
        if (showDBCompound){
            if (dbCompound != null)drawREF(g2,dbCompound,colorDBcomp);
        }

        if (showIndexSolution){
            if (indexSolution != null) {
                drawHKL(g2,indexSolution,indexSolution.getColor());
            }
        }
        
    }
    
    
    protected void setShowPeakThreshold(boolean showPeaks) {
        this.showPeakThreshold = showPeaks;
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

    public Plottable getBkgseriePeakSearch() {
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



}

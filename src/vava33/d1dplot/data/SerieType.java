package com.vava33.d1dplot.data;

/**
 * D1Dplot
 * 
 * Definition of series types with peculiarities and styles
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.awt.Color;


public enum SerieType {
    dat(Color.BLACK,DataSerie.def_lineWidth,DataSerie.def_markerSize), 
    peaks(Color.GRAY,DataSerie.def_lineWidth+1,0),
    bkg(Color.PINK,DataSerie.def_lineWidth+1,0), 
    bkgEstimP(Color.PINK,0,DataSerie.def_markerSize+10), 
    obs(Color.BLACK,0,4),
    cal(Color.RED,DataSerie.def_lineWidth,0),
    diff(Color.BLUE,DataSerie.def_lineWidth,0),
    hkl(Color.GREEN.darker(),DataSerie.def_lineWidth,0),
    gr(Color.BLACK,DataSerie.def_lineWidth,DataSerie.def_markerSize), 
    ref(Color.GRAY,DataSerie.def_lineWidth,0);

    public final Color ini_color;
    public final float ini_lineWidth;
    public final float ini_markerSize;
    
    private SerieType(Color color, float lineWidth, float markerSize) {
        this.ini_color=color;
        this.ini_lineWidth=lineWidth;
        this.ini_markerSize=markerSize;
    }
    
    public static SerieType getEnum(String n) {
        for (SerieType x: SerieType.values()) {
            if (n.equalsIgnoreCase(x.toString()))return x;
            if (n.equalsIgnoreCase(x.name()))return x;
        }
        return null;
    }
    
    public static Color getDefColor(SerieType st) {
        switch (st) {
        case dat:
            return Color.BLACK;
        case peaks:
            return Color.GRAY;
        case bkg:
            return Color.PINK;
        case bkgEstimP:
            return Color.PINK;
        case obs:
            if(DataSerie.prfFullprofColors)return Color.RED;
            return Color.BLACK;
        case cal:
            if(DataSerie.prfFullprofColors)return Color.BLACK;
            return Color.RED;
        case diff:
            return Color.BLUE;
        case hkl:
            return Color.GREEN.darker();
        case gr:
            return Color.BLACK;
        case ref:
            return Color.GRAY;
        default:
            return Color.BLACK;
        }
    }
    public static float getDefMarkerSize(SerieType st) {
        switch (st) {
        case dat:
            return DataSerie.def_markerSize;
        case peaks:
            return 0;
        case bkg:
            return 0;
        case bkgEstimP:
            return DataSerie.def_markerSize+10;
        case obs:
            return 4;
        case cal:
            return 0;
        case diff:
            return 0;
        case hkl:
            return 0;
        case gr:
            return DataSerie.def_markerSize;
        case ref:
            return 0;
        default:
            return DataSerie.def_markerSize;
        }
    }

    public static float getDefLineWidth(SerieType st) {
        switch (st) {
        case dat:
            return DataSerie.def_lineWidth;
        case peaks:
            return DataSerie.def_lineWidth+1;
        case bkg:
            return DataSerie.def_lineWidth+1;
        case bkgEstimP:
            return 0;
        case obs:
            return 0;
        case cal:
            return DataSerie.def_lineWidth;
        case diff:
            return DataSerie.def_lineWidth;
        case hkl:
            return DataSerie.def_lineWidth;
        case gr:
            return DataSerie.def_lineWidth;
        case ref:
            return DataSerie.def_lineWidth;
        default:
            return DataSerie.def_lineWidth;
        }
    }
    
}

package com.vava33.d1dplot.auxi;

/**
 * D1Dplot
 * 
 * Model for the table of data (1DXRD patterns)
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import com.vava33.BasicPlotPanel.core.SerieType;
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.d1dplot.data.DataSet;
import com.vava33.d1dplot.data.Xunits;

public class PatternsTableModel extends DefaultTableModel {

    private static final long serialVersionUID = -7255602787469780000L;
    public enum columns {
        nP, nS, Filename, Color, Scale, ZerOff, Wavel, XUnits, YOffset, Marker, Line, ErrBars, Show, Type
    }
    
    private List<DataSerie> dsList;
    
    @SuppressWarnings("rawtypes")
    Class[] columnTypes;
    boolean[] columnEditables;
    public int[] columnMinSize;
    public int[] columnMaxSize;
    public int[] columnPrefSize;
    
    //nP, nS, Filename, Color, Scale, ZerOff, Wavel, XUnits, YOffset, Marker, Line, ErrBars, Show,
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        DataSerie ds = dsList.get(rowIndex);
        switch (columns.values()[columnIndex]){
        case Color:
            return ds.getColor();
        case ErrBars:
            return ds.isShowErrBars();
        case Filename:
            return ds.getName();
        case Line:
            return ds.getLineWidth();
        case Marker:
            return ds.getMarkerSize();
        case Scale:
            return ds.getScaleY();
        case Show:
            return ds.isPlotThis();
        case Type:
            return ds.getSerieType().name();
        case Wavel:
            return ds.getWavelength();
        case XUnits:
            return ds.getxUnits().getName();
        case YOffset:
            return ds.getYOffset();
        case ZerOff:
            return ds.getXOffset();
        case nP:
            return datasets.indexOf(ds.getParent());
        case nS:
            return ds.getParent().indexOfDS(ds);
        default:
            return null; 
        }
    }
    
    @Override
    public void setValueAt(Object aValue, int row, int column) {
        super.setValueAt(aValue, row, column);
//        System.out.println("setValue fired");
        DataSerie ds = dsList.get(row);
        switch (columns.values()[column]){
        case Color:
            ds.setColor((Color)aValue);
            break;
        case ErrBars:
            ds.setShowErrBars((Boolean)aValue);
            break;
        case Filename:
            ds.setName((String)aValue);
            break;
        case Line:
            ds.setLineWidth((Float)aValue);
            break;
        case Marker:
            ds.setMarkerSize((Float)aValue);
            break;
        case Scale:
            ds.setScaleY((Double)aValue);
            break;
        case Show:
            ds.setPlotThis((Boolean)aValue);
            break;
        case Type:
            //dialog preguntant si estem segurs?
            SerieType stype = SerieType.getEnum((String) aValue);
            ds.setSerieType(stype);
//            if (stype==null)break;
//            SerieType currStype = ds.getSerieType();
//            if (stype!=currStype) { //aixi si responem no un cop no ens ho torna a repetir infiniatment
//                final boolean doit = FileUtils.YesNoDialog(null, "Are you sure you want to change serie Type?");
//                if (doit) {
//                    ds.setSerieType(stype);
//                }
//            }
            break;
        case Wavel:
            ds.setWavelength((Double)aValue);
            break;
        case XUnits:
            ds.setxUnits(Xunits.getEnum((String)aValue));
            break;
        case YOffset:
            ds.setYOffset((Double)aValue);
            break;
        case ZerOff:
            ds.setXOffset((Double)aValue);
            break;
        case nP:
            break;
        case nS:
            break;
        default:
            break; 
        }
    }

    private Object[] createRow(DataSerie ds) {
        int nP = datasets.indexOf(ds.getParent());
        int nS = ds.getParent().indexOfDS(ds);
        String fname = ds.getName().trim();
        Color c = ds.getColor();
        double zoff = ds.getXOffset();
        double wavel = ds.getWavelength();
        double scale = ds.getScaleY();
        String xunits = ds.getxUnits().getName();
        double yoffset = ds.getYOffset();
        float markersize = ds.getMarkerSize();
        float linewidth = ds.getLineWidth();
        boolean errbars = ds.isShowErrBars();
        boolean show = ds.isPlotThis();
        String stype = ds.getSerieType().name();
        return new Object[]{nP,nS,fname,c,scale,zoff,wavel,xunits,yoffset,markersize,linewidth,errbars,show,stype};
    }
    
    public void addRow(DataSerie ds) {
        dsList.add(ds);
        this.addRow(this.createRow(ds));
        
    }
    
    @Override
    public void setRowCount (int rowCount) {
        super.setRowCount(rowCount);
        this.dsList.clear();
    }

    public void removeRow(DataSerie ds) {
        int irow = dsList.indexOf(ds);
        dsList.remove(ds);
        this.removeRow(irow);
        
    }
    
    public DataSerie getDSAtRow(int row) {
        return dsList.get(row);
    }
    
    public int getRowOfDS(DataSerie ds) {
        return dsList.indexOf(ds);
    }
    
    public List<DataSerie> getRowDSList(){
        return dsList;
    }
    
    List<DataSet> datasets;
    
    public PatternsTableModel(List<DataSet> dsets){
        super();
        datasets=dsets;
        dsList = new ArrayList<DataSerie>();
        
        
        //preparem les dades, els tipus i l'editable
        
        String[] cols = new String[columns.values().length];
        columnTypes = new Class[columns.values().length];
        columnEditables = new boolean[columns.values().length];
        columnMinSize = new int[columns.values().length];
        columnMaxSize = new int[columns.values().length];
        columnPrefSize = new int[columns.values().length];
        for (int i=0; i<columns.values().length; i++){
            cols[i]=columns.values()[i].toString();
            switch (columns.values()[i]){   
                case nP:
                    columnTypes[i]=Integer.class;
                    columnEditables[i]=false;
                    columnMinSize[i]=30;
                    columnMaxSize[i]=70;
                    columnPrefSize[i]=30;
                    break;
                case nS:
                    columnTypes[i]=Integer.class;
                    columnEditables[i]=false;
                    columnMinSize[i]=30;
                    columnMaxSize[i]=70;
                    columnPrefSize[i]=30;
                    break;
                case Filename:
                    columnTypes[i]=String.class;
                    columnEditables[i]=true;
                    columnMinSize[i]=60;
                    columnMaxSize[i]=1200;
                    columnPrefSize[i]=80;
                    break;
                case Color:
                    columnTypes[i]=Color.class;
                    columnEditables[i]=true;
                    columnMinSize[i]=30;
                    columnMaxSize[i]=80;
                    columnPrefSize[i]=40;
                    break;
                case Scale:
                    columnTypes[i]=Double.class;
                    columnEditables[i]=true;
                    columnMinSize[i]=50;
                    columnMaxSize[i]=110;
                    columnPrefSize[i]=60;
                    break;
                case ZerOff:
                    columnTypes[i]=Double.class;
                    columnEditables[i]=true;
                    columnMinSize[i]=50;
                    columnMaxSize[i]=110;
                    columnPrefSize[i]=60;
                    break;
                case Wavel:
                    columnTypes[i]=Double.class;
                    columnEditables[i]=true;
                    columnMinSize[i]=50;
                    columnMaxSize[i]=110;
                    columnPrefSize[i]=60;
                    break;
                case XUnits:
                    columnTypes[i]=String.class;
                    columnEditables[i]=true;
                    columnMinSize[i]=50;
                    columnMaxSize[i]=110;
                    columnPrefSize[i]=60;
                    break;
                case YOffset:
                    columnTypes[i]=Double.class;
                    columnEditables[i]=true;
                    columnMinSize[i]=50;
                    columnMaxSize[i]=110;
                    columnPrefSize[i]=60;
                    break;
                case Marker:
                    columnTypes[i]=Float.class;
                    columnEditables[i]=true;
                    columnMinSize[i]=50;
                    columnMaxSize[i]=110;
                    columnPrefSize[i]=60;
                    break;
                case Line:
                    columnTypes[i]=Float.class;
                    columnEditables[i]=true;
                    columnMinSize[i]=50;
                    columnMaxSize[i]=110;
                    columnPrefSize[i]=60;
                    break;
                case ErrBars:
                    columnTypes[i]=Boolean.class;
                    columnEditables[i]=true;
                    columnMinSize[i]=30;
                    columnMaxSize[i]=80;
                    columnPrefSize[i]=40;
                    break;
                case Show:
                    columnTypes[i]=Boolean.class;
                    columnEditables[i]=true;
                    columnMinSize[i]=25;
                    columnMaxSize[i]=80;
                    columnPrefSize[i]=40;
                    break;
                case Type:
                    columnTypes[i]=String.class;
                    columnEditables[i]=true;
                    columnMinSize[i]=25;
                    columnMaxSize[i]=80;
                    columnPrefSize[i]=40;
                    break;
            }
        }
        
        this.setDataVector(new Object[][] {}, cols);
        
    }
      
      @SuppressWarnings({ "unchecked", "rawtypes" })
      @Override
      public Class getColumnClass(int columnIndex) {
          return columnTypes[columnIndex];
      }
      
      @Override
      public boolean isCellEditable(int row, int column) {
          return columnEditables[column];
      }
      
      public int getColumnMinSize(int columnIndex) {
          return columnMinSize[columnIndex];
      }
      public int getColumnMaxSize(int columnIndex) {
          return columnMaxSize[columnIndex];
      }
      public int getColumnPrefSize(int columnIndex) {
          return columnPrefSize[columnIndex];
      }
      
}

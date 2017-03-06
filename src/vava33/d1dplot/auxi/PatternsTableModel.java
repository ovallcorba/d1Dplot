package vava33.d1dplot.auxi;

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

import javax.swing.table.DefaultTableModel;

public class PatternsTableModel extends DefaultTableModel {

    private static final long serialVersionUID = -7255602787469780000L;
    public enum columns {
        nP, nS, Filename, Color, Scale, ZerOff, Wavel, XUnits, YOffset, Marker, Line, ErrBars, Show,
    }
    
    @SuppressWarnings("rawtypes")
    Class[] columnTypes;
    boolean[] columnEditables;
    public int[] columnMinSize;
    public int[] columnMaxSize;
    public int[] columnPrefSize;
    
    public PatternsTableModel(){
        super();
        
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
                    columnTypes[i]=Float.class;
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
                    columnMinSize[i]=30;
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

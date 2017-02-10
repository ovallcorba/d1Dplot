package vava33.d1dplot.auxi;

/**
 * D1Dplot
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

import vava33.d1dplot.D1Dplot_global;

import com.vava33.jutils.FileUtils;
import com.vava33.jutils.VavaLogger;

public class PatternsTableCellRenderer extends DefaultTableCellRenderer {
    private static VavaLogger log = D1Dplot_global.getVavaLogger(PatternsTableCellRenderer.class.getName());

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
          boolean isSelected, boolean hasFocus, int row, int col) {

        // PRIMER FORMATEJO EL VALOR (ABANS DE CRIDAR SUPER)

        //      nP, nS, Filename, Color, Scale, ZerOff, Wavel, XUnits, YOffset, Marker, Line, ErrBars, Show,
        if (table.getColumnName(col)==PatternsTableModel.columns.Scale.toString()){
            value = FileUtils.dfX_2.format((Number)value);
        }
        if (table.getColumnName(col)==PatternsTableModel.columns.ZerOff.toString()){
            value = FileUtils.dfX_3.format((Number)value);
        }
        if (table.getColumnName(col)==PatternsTableModel.columns.Wavel.toString()){
            log.debug("YEAH");
            value = FileUtils.dfX_4.format((Number)value);
        }
        if (table.getColumnName(col)==PatternsTableModel.columns.YOffset.toString()){
            value = FileUtils.dfX_1.format((Number)value);
        }
        if (table.getColumnName(col)==PatternsTableModel.columns.Marker.toString()){
            value = FileUtils.dfX_1.format((Number)value);
        }
        if (table.getColumnName(col)==PatternsTableModel.columns.Line.toString()){
            value = FileUtils.dfX_1.format((Number)value);
        }

        Component c = super.getTableCellRendererComponent(table, value,
                isSelected, hasFocus, row, col);

        this.setHorizontalAlignment(JLabel.CENTER);

        
        if (table.isCellSelected(row, col)){
            Color celsel = UIManager.getColor("Table.selectionBackground");
            if (celsel==null) {
                log.debug("celsel is null");
                celsel = Color.LIGHT_GRAY;
            }
            c.setBackground(celsel);
        }else if (table.isRowSelected(row)){
            Color rowsel = UIManager.getColor("Table.selectionBackground").darker();
            if (rowsel == null){
                log.debug("rowsel is null");
                rowsel = Color.GRAY.brighter();
            }
            c.setBackground(rowsel);
        }else{
            Color nosel = UIManager.getColor("Table.background");
            if (nosel == null){
                log.debug("nosel is null");
                nosel = Color.WHITE;
            }
            c.setBackground(nosel);
        }
        return c;
        }
}
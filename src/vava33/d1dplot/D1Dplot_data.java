package com.vava33.d1dplot;

/**
 * D1Dplot
 * 
 * Implementation of DataToPlot interface
 * from com.vava33.BasicPlotPanel.core
 *
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.apache.commons.math3.util.FastMath;

import com.vava33.BasicPlotPanel.core.DataToPlot;
//import com.vava33.BasicPlotPanel.core.Plot1DGlobal;
import com.vava33.BasicPlotPanel.core.Plot1DPanel;
import com.vava33.BasicPlotPanel.core.SerieType;
import com.vava33.d1dplot.auxi.ColorEditor;
import com.vava33.d1dplot.auxi.ColorRenderer;
import com.vava33.d1dplot.auxi.DataFileUtils;
import com.vava33.d1dplot.auxi.PattOps;
import com.vava33.d1dplot.auxi.PatternsTableCellRenderer;
import com.vava33.d1dplot.auxi.PatternsTableModel;
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.d1dplot.data.DataSet;
import com.vava33.d1dplot.data.Xunits;
import com.vava33.jutils.FileUtils;
import com.vava33.jutils.VavaLogger;

public class D1Dplot_data implements DataToPlot<DataSerie> {

    private static final String className = "D1Dplot_data";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);
    
    List<DataSet> datasets; //cada dataset pot tenir varies series  (p.ex. dataserie extends basicserie que implementa plottable)
    List<DataSerie> selectedSeries;
    int nColoredSeries;
    protected JTable pltTable;
    protected Plot1DPanel<?> plotpanel;
    
    public D1Dplot_data() {
        datasets = new ArrayList<DataSet>();
//        series = new ArrayList<T>();
        selectedSeries = new ArrayList<DataSerie>();
        initTablePatterns(new PatternsTableModel(datasets));
        nColoredSeries=0;
    }
    
/**************************************************************************************
 * DEALING WITH DATA (ADD,REMOVE,EDIT,etc...)
 */    
 
    public void addDataSet(DataSet dset, boolean paintIt, boolean updatePlot) {
    	if (dset==null)return;
        for (DataSerie ser:dset.getDataSeries()) {
            if (DataSerie.isPaintedType(ser.getSerieType())){
                if (paintIt)this.paintIt(nColoredSeries,ser);
                nColoredSeries++;
            }
        }
        this.datasets.add(dset);
        for (DataSerie ds:dset.getDataSeries()) {
            this.addToTable(ds);    
        }
        if (this.datasets.size()==1) {
            plotpanel.fitGraph();
            this.updateFullTable();//per el label X
        }
        if (updatePlot)plotpanel.actualitzaPlot();
    }
    
    public void addDataSerie(DataSerie ds, DataSet dst, boolean paintIt, boolean updateTable, boolean updatePlot) {
    	if (ds==null)return;
    	if (dst==null)return;
        if (DataSerie.isPaintedType(ds.getSerieType())){
            if (paintIt)this.paintIt(nColoredSeries,ds);
            nColoredSeries++;
        }
        dst.addDataSerie(ds);
        ds.setParent(dst);
        if(updateTable)this.updateFullTable();
        if(updatePlot)plotpanel.actualitzaPlot();
    }
    
    public void removeDataSet(DataSet dset, boolean updateTable, boolean updatePlot) {
    	if (dset==null)return;
        this.datasets.remove(dset);
        for (DataSerie ds:dset.getDataSeries()) {
            if (DataSerie.isPaintedType(ds.getSerieType()))nColoredSeries--;
        }
        if(updateTable)this.updateFullTable();
        if(updatePlot)plotpanel.actualitzaPlot();
    }
    
    public void removeDataSerie(DataSerie ds, boolean updateTable, boolean updatePlot) {
    	if (ds==null)return;
        DataSet dset = ds.getParent();
        dset.removeDataSerie(ds);
        if (DataSerie.isPaintedType(ds.getSerieType()))nColoredSeries--;
        if (dset.getNSeries()==0)this.removeDataSet(dset, false, false);
        if(updateTable)this.updateFullTable();
        if(updatePlot)plotpanel.actualitzaPlot();
    }
    
    public void removeSelectedSeries() {
        int ilastRemovedRow = -1;
        PatternsTableModel model = (PatternsTableModel) pltTable.getModel();
        ArrayList<DataSerie> toRemove = new ArrayList<DataSerie>();
        for (int i=0; i<selectedSeries.size();i++) {
            ilastRemovedRow = model.getRowOfDS(selectedSeries.get(i));
            toRemove.add(selectedSeries.get(i));
            this.removeDataSerie(selectedSeries.get(i), false, false);
        }
        
//        Iterator<DataSerie> itrDS = selectedSeries.iterator();
//        while (itrDS.hasNext()) {
//            DataSerie ds = itrDS.next();
//            itrDS.remove();
//            this.removeDataSerie(ds, false);
//        }
        
//        for (DataSerie ds:selectedSeries) {
//            this.removeDataSerie(ds, false);
//        }
        
        //ara seleccionem la última que quedi
//        selectedSeries.removeAll(toRemove);
        this.updateFullTable();
        if(ilastRemovedRow>=0) {
            if (this.arePlottables()) {
                int ind = FastMath.min(ilastRemovedRow, model.getRowCount()-1);
                DataSerie toSelect = model.getDSAtRow(ind);
                selectedSeries.add(toSelect);
//                selectedSeries.add(model.getDSAtRow(FastMath.min(ilastRemovedRow, model.getRowCount()-1)));
            }
        }
        this.updateFullTable();
        plotpanel.actualitzaPlot();
    }
    
    public void removeAllDataSeries() {
        this.datasets.clear();
        nColoredSeries=0;
        this.updateFullTable();
        plotpanel.actualitzaPlot();
    }
    
    public DataSet getDataSet(int i) {
        return datasets.get(i);
    }
    
    public int getNDataSets() {
        return datasets.size();
    }
    
    //TODO es podria fer un que nomes actualitzes les dades... (el list<plottable_point> i mantingues correccions. S'hauria de preguntar que es vol fer
    private void replaceDataSet(int index, DataSet newDset, boolean keepColors) {
        if (keepColors) {
            for (int i=0; i<datasets.get(index).getDataSeries().size();i++) {
                if (i<newDset.getNSeries()) {
                    newDset.getDataSerie(i).copyVisualParameters(datasets.get(index).getDataSerie(i));
                }
            }
        }
        datasets.set(index, newDset);
    }
    
    public void reloadSelectedDataSets() {
        Set<DataSet> selectedSets=new HashSet<DataSet>();
//        PatternsTableModel model = (PatternsTableModel)pltTable.getModel();
        int[] selrows = pltTable.getSelectedRows();
        for (DataSerie ds:selectedSeries) {
            selectedSets.add(ds.getParent());
        }
        for (DataSet ds:selectedSets) {
            this.replaceDataSet(datasets.indexOf(ds),DataFileUtils.readPatternFile(ds.getFile()),true);
        }
        this.updateFullTable();
        for (int i=0;i<selrows.length;i++) {
            try {
                pltTable.addRowSelectionInterval(selrows[i], selrows[i]);
            }catch(IllegalArgumentException ex) {
                //row not existing
            }
        }
        plotpanel.actualitzaPlot();
    }
    
    /**
     * replaces first occurence of the stype in a dataset, otherwise adds the dataserie
     */
    public void replaceDataSerie(DataSet dset, DataSerie newDS, SerieType stypeToReplace) {
        int index = -1;
        try {
            index = dset.indexOfDS(dset.getDataSeriesByType(stypeToReplace).get(0));
        }catch(Exception ex) {
            log.info("DataSerie not found, adding as new");
        }
        newDS.setSerieType(stypeToReplace);
        newDS.setParent(dset);
        if (index>=0) {
            dset.replaceDataSerie(index, newDS);
            this.updateFullTable();
        }else {
            this.addDataSerie(newDS, dset, true, true, true);
        }
    }
    
//    public int indexOfDataSet(DataSet ds) {
//        return datasets.indexOf(ds);
//    }
    
///END DEALING WITH DATA
    
/**************************************************************************************
 * TABLE STUFF
 */
    public void initTablePatterns(TableModel dm) {
        pltTable = new JTable(dm);

        pltTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        pltTable.setColumnSelectionAllowed(true);
        pltTable.setCellSelectionEnabled(true);

        pltTable.setDefaultRenderer(String.class, new PatternsTableCellRenderer());
        pltTable.setDefaultRenderer(Integer.class, new PatternsTableCellRenderer());
        pltTable.setDefaultRenderer(Float.class, new PatternsTableCellRenderer());
        pltTable.setDefaultRenderer(Double.class, new PatternsTableCellRenderer());

        pltTable.getModel().addTableModelListener(new TableModelListener(){
            public void tableChanged(TableModelEvent e) {
                //nomes fem algo en updatem, per evitar crides inutils aplicar modfiicacio (nomes quan es fa un setValueAt)
                switch (e.getType()) {
                case TableModelEvent.UPDATE:
                    applicarModificacioTaula(e.getColumn(),e.getFirstRow(),e.getLastRow());
                    break;
                default:
                    break;
                }
            }
        });
        
        
        //Set up renderer and editor for the Favorite Color column.
        pltTable.setDefaultRenderer(Color.class, new ColorRenderer(true));
        pltTable.setDefaultEditor(Color.class, new ColorEditor());

        pltTable.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                int index = pltTable.getTableHeader().columnAtPoint(mouseEvent.getPoint());
                if (index >= 0) {
                    if (pltTable.getRowCount()>0){
                        pltTable.setRowSelectionInterval(0, pltTable.getRowCount()-1);
                        pltTable.setColumnSelectionInterval(index, index);
                    }
                }
            };
        });
        
        pltTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                aplicarselecciotaula(arg0);
            }
        });
        
//        pltTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        

        JComboBox<String> comboXunitsTable = new JComboBox<String>();
        for (Xunits a :Xunits.values()){
            comboXunitsTable.addItem(a.getName());
        }
        pltTable.getColumn(PatternsTableModel.columns.XUnits.toString()).setCellEditor(new DefaultCellEditor(comboXunitsTable));
        
        try {
            JComboBox<String> comboStypeTable = new JComboBox<String>();
            for (SerieType s :SerieType.values()){
                comboStypeTable.addItem(s.name());
            }
            pltTable.getColumn(PatternsTableModel.columns.Type.toString()).setCellEditor(new DefaultCellEditor(comboStypeTable));           
        }catch(IllegalArgumentException ex) {
            log.debug("Type column is not shown");
        }

       
        
        final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem editValues = new JMenuItem("Edit Selected Values");
        editValues.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                editMultipleValuesTable(e);
            }
            
        });
        popupMenu.add(editValues);
        JMenuItem editSeries = new JMenuItem("Edit Selected Series");
        editSeries.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                editMultipleSeriesTable(e);
            }
            
        });
        popupMenu.add(editSeries);
        
        JMenuItem removeExtName = new JMenuItem("Remove .ext from name");
        removeExtName.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                removeExtensionFromNames(e);
            }
        });
        popupMenu.add(removeExtName);

        JMenuItem convertWL = new JMenuItem("Convert to WL");
        convertWL.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                convertToWL(e);
            }
        });
        popupMenu.add(convertWL);
        
        JMenuItem changeXU = new JMenuItem("Change X units");
        changeXU.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeXunits(e);
            }
        });
        popupMenu.add(changeXU); 
        
        JMenuItem sumPatts = new JMenuItem("Sum selected patterns");
        sumPatts.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sumPatterns(e);
            }
        });
        popupMenu.add(sumPatts);
        
        pltTable.setComponentPopupMenu(popupMenu);

        //columnes mides

        PatternsTableModel model = (PatternsTableModel) pltTable.getModel();
        for (int i=0; i<pltTable.getColumnCount(); i++){
            pltTable.getColumnModel().getColumn(i).setPreferredWidth(model.getColumnPrefSize(i));
            pltTable.getColumnModel().getColumn(i).setMaxWidth(model.getColumnMaxSize(i));
            pltTable.getColumnModel().getColumn(i).setMinWidth(model.getColumnMinSize(i));
        }
    }
    
    public JTable getTablePatterns() {
        return pltTable;
    }
    
    public void selectLastAdded() {
        if (pltTable.getRowCount()>0){
            pltTable.setRowSelectionInterval(pltTable.getRowCount()-1, pltTable.getRowCount()-1);    
        }
    }
    
    
//    private void addToTable(DataSet p) {
//        PatternsTableModel model = (PatternsTableModel) pltTable.getModel();
//        for (DataSerie ds:p.getDataSeries()) {
//            model.addRow(createRow(ds));            
//        }
//    }
    
    private void addToTable(DataSerie ds) {
        PatternsTableModel model = (PatternsTableModel) pltTable.getModel();
//        model.addRow(createRow(ds));
        model.addRow(ds);
//        pltTable.convertRowIndexToModel(model.getRowCount()-1);
    }
    
//    private void addToTable(DataSerie ds, int row) {
//        PatternsTableModel model = (PatternsTableModel) pltTable.getModel();
//        model.insertRow(row,createRow(ds));
//    }
//    
//    private void removeFromTable(DataSet p) {
//        PatternsTableModel model = (PatternsTableModel) pltTable.getModel();
//
//    }
//    
//    private void removeFromTable(DataSerie ds) {
//        PatternsTableModel model = (PatternsTableModel) pltTable.getModel();
//        
//    }
    
    private int getColumnByName(JTable table, String name) {
        for (int i = 0; i < table.getColumnCount(); ++i)
            if (table.getColumnName(i).equalsIgnoreCase(name))
                return i;
        return -1;
    }
    
    
    private void applicarModificacioTaula(int columna, int filaIni, int filaFin){
        plotpanel.actualitzaPlot();
    }
    
    private void aplicarselecciotaula(ListSelectionEvent arg0) {
//        if (pltTable.getSelectedRow()<0) {
//            this.selectedSeries.clear();
//            return;
//        }
        if (pltTable.getRowCount()<=0)return;
        //prova amb selected rows:
        int[] selRows = pltTable.getSelectedRows();
        if (selRows.length==0)return;
        this.selectedSeries.clear();
        for (int i=0; i<selRows.length;i++){
            int selRow = selRows[i];
            selectedSeries.add(((PatternsTableModel)pltTable.getModel()).getDSAtRow(selRow));
//            
//            int indexP = (Integer) pltTable.getModel().getValueAt(selRow, this.getColumnByName(pltTable, PatternsTableModel.columns.nP.toString()));
//            int indexDS = (Integer) pltTable.getModel().getValueAt(selRow, this.getColumnByName(pltTable, PatternsTableModel.columns.nS.toString()));
//            selectedSeries.add(this.datasets.get(indexP).getDataSerie(indexDS));    
        }
        
//        log.debug("SELECTED_SERIES:");
//        for (DataSerie ds:selectedSeries) {
//           log.debug(ds.getName());
//        }
        
    }

    //TODO
    private void editMultipleSeriesTable(ActionEvent e) {
        if (selectedSeries.size()>0) {
            BatchEditDialog be = new BatchEditDialog(selectedSeries);
            be.setModal(true);
            be.setVisible(true);         
            
            //when closed we update table -- millor si actualitzo selectedPlottables i despres la full table
            this.updateFullTable();
            plotpanel.actualitzaPlot();
            
        }else {
            log.info("Select the series(s) that you want to edit");
        }
    }
    
    
    private void editMultipleValuesTable(ActionEvent e) {
        if (pltTable.getSelectedRow()<0)return;
        if (pltTable.getRowCount()<=0)return;

        int[] selRows = pltTable.getSelectedRows();
        int selCol = pltTable.getSelectedColumn();
        PatternsTableModel.columns colName = FileUtils.searchEnum(PatternsTableModel.columns.class,pltTable.getColumnName(selCol));

        Color newColor = null;
        boolean ask = true;
        String s = null;
        String title = "Change values column-wise";
        String labl = "New value=";
        switch (colName){
        case Color:
            newColor = JColorChooser.showDialog(
                    pltTable,
                    "Choose Color",
                    Color.BLACK);
            s = "changing color column";
            if(newColor == null){
                log.info("Select a valid color");
                return;
            }
            ask=false;
            break;
        case Line:
            title = "Change line width";
            labl = "New value (line width)=";
            break;
        case Marker:
            title = "Change marker size";
            labl = "New value (marker size)=";
            break;
        case Filename:
            title = "Change Name";
            labl = "New value (name)=";
            break;
        case Show:
            title = "Change show data";
            labl = "New value (true/false)=";
            break;
        case YOffset:
            title = "Change Y offset";
            labl = "New value (Yoffset)=";
            break;
        case Type:
            title = "Change data serie type";
            labl = String.format("New value (%s)", SerieType.getStringAllNames("/"));
            break;
        case Scale:
            title = "Change Y scale";
            labl = "New value (scale Y)=";
            break;
        case Wavel:
            title = "Change wavelength";
            labl = "New value (wavelength Ang)=";
            break;
        case ZerOff:
            title = "Change zero offset";
            labl = "New value (zero ofset)=";
            break;
        default:
            break;

        }
        if (ask) {
            s = (String)JOptionPane.showInputDialog(
                    pltTable,
                    labl,
                    title,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "");
        }

        //If a string was returned, say so.
        if ((s != null) && (s.length() > 0)) {
            //edit all the selected cells
            //prova amb selected rows:
            for (int i=0; i<=selRows.length-1;i++){
                int selRow = selRows[i];
                switch (colName){
                case Color:
                    pltTable.setValueAt(newColor, selRow, selCol);
                    break;
                case Line:
                    pltTable.setValueAt(Float.parseFloat(s), selRow, selCol);
                    break;
                case Marker:
                    pltTable.setValueAt(Float.parseFloat(s), selRow, selCol);
                    break;
                case Show:
                    pltTable.setValueAt(Boolean.parseBoolean(s), selRow, selCol);
                    break;
                case YOffset:
                    pltTable.setValueAt(Double.parseDouble(s), selRow, selCol);
                    break;
                case Type:
                    pltTable.setValueAt(SerieType.getEnum(s), selRow, selCol);
                    break;
                case Scale:
                    pltTable.setValueAt(Double.parseDouble(s), selRow, selCol);
                    break;
                case Wavel:
                    pltTable.setValueAt(Double.parseDouble(s), selRow, selCol);
                    break;
                case ZerOff:
                    pltTable.setValueAt(Double.parseDouble(s), selRow, selCol);
                    break;
                default:
                    log.info("Column not editable");
                    break;
                }
            }
            plotpanel.actualitzaPlot(); //TODO no hi era però aquí fa falta, no?
        }
    }
    
//////END TABLE STUFF    
    
/**************************************************************************************
 * OTHER METHODS CALLED FROM THE TABLE POP-UP
 */    
    protected void sumPatterns(ActionEvent e){
        if (pltTable.getSelectedRow()<0)return;
        if (pltTable.getRowCount()<=0)return;

        int[] selRows = pltTable.getSelectedRows();

        StringBuilder sb = new StringBuilder();
        StringBuilder sbNames = new StringBuilder();

        DataSerie[] dss = new DataSerie[selRows.length];
        for (int i=0; i<selRows.length;i++){
            int selRow = selRows[i];
            dss[i] =((PatternsTableModel)pltTable.getModel()).getDSAtRow(selRow);

            //Per posar al nom agafem el nP i nS
            int pattern = (Integer) pltTable.getValueAt(selRow, this.getColumnByName(pltTable, PatternsTableModel.columns.nP.toString()));
            int serie = (Integer) pltTable.getValueAt(selRow, this.getColumnByName(pltTable, PatternsTableModel.columns.nS.toString()));
            
            sb.append(String.format("P%dS%d ", pattern,serie));
            sbNames.append(dss[i].getName()+" ");
        }

        //comprovar punts, sino rebinning de les series que faci falta, la primera serie mana
        for (int i=1; i<dss.length; i++){
            boolean coin = PattOps.haveCoincidentPointsDS(dss[0], dss[i]);
            if (!coin){
                dss[i] = PattOps.rebinDS(dss[0], dss[i]);
            }
        }

        //sumem
        DataSerie suma = PattOps.addDataSeriesCoincidentPoints(dss);
        suma.setName(String.format("Sum of %s",sb.toString().trim()));
        DataSet patt = new DataSet(dss[0].getWavelength());
        patt.addCommentLines(dss[0].getCommentLines()); //comments of 1st serie
        patt.addCommentLine("#Sum of: "+sbNames.toString().trim());
        patt.setOriginalWavelength(dss[0].getOriginalWavelength());
        patt.addDataSerie(suma); //ja el fa parent
        this.addDataSet(patt, true, true);
    }
    
    protected void changeXunits(ActionEvent e){
        if (pltTable.getSelectedRow()<0)return;
        if (pltTable.getRowCount()<=0)return;

        String[] comboValues = new String[Xunits.values().length];
        int i = 0;
        for (Xunits a :Xunits.values()){
            comboValues[i] = a.getName();
            i = i+1;
        }
        String s = (String)JOptionPane.showInputDialog(
                plotpanel,
                "Change to X-units (new serie will be generated)",
                "Change X-units",
                JOptionPane.PLAIN_MESSAGE,
                null,
                comboValues,
                comboValues[0]);

        if ((s != null) && (s.length() > 0)) {

            Xunits destUnits = null;
            if (Xunits.getEnum(s)!=null){
                destUnits=Xunits.getEnum(s);
            }
            if (destUnits==null){
                log.warning("Choose a valid x-units value");
                return;
            }

            int[] selRows = pltTable.getSelectedRows();

            for (i=0; i<=selRows.length-1;i++){
                int selRow = selRows[i];
//                //primer agafem el pattern i serie seleccionades
//                int pattern = (Integer) pltTable.getValueAt(selRow, this.getColumnByName(pltTable, PatternsTableModel.columns.nP.toString()));
//                int serie = (Integer) pltTable.getValueAt(selRow, this.getColumnByName(pltTable, PatternsTableModel.columns.nS.toString()));
//
//                DataSet patt = this.datasets.get(pattern);
//                DataSerie ds = patt.getDataSeries().get(serie);

                DataSerie ds = ((PatternsTableModel)pltTable.getModel()).getDSAtRow(selRow);
                //CONVERTIM
                log.info(String.format("Conversion from %s to %s",ds.getxUnits(), destUnits));
                ds.convertDStoXunits(destUnits); //ja mira si es possible i si hi ha wavlength
            }
            this.updateFullTable();
            plotpanel.actualitzaPlot();
        }
    }
    
    protected void convertToWL(ActionEvent e){
        if (pltTable.getSelectedRow()<0)return;
        if (pltTable.getRowCount()<=0)return;

        String s = (String)JOptionPane.showInputDialog(
                plotpanel,
                "New wavelength=",
                "Covnert pattern to new wavelength",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "");

        if ((s != null) && (s.length() > 0)) {
            double newWL = -1;
            try{
                newWL = Double.parseDouble(s);
            }catch(Exception ex){
                log.warning("Error parsing wavelength");
                return;
            }
            if (newWL<0){
                log.warning("Invalid wavelength entered");
                return;
            }

            int[] selRows = pltTable.getSelectedRows();

            for (int i=0; i<=selRows.length-1;i++){
                int selRow = selRows[i];
//                //primer agafem el pattern i serie seleccionades
//                int pattern = (Integer) pltTable.getValueAt(selRow, this.getColumnByName(pltTable, PatternsTableModel.columns.nP.toString()));
//                int serie = (Integer) pltTable.getValueAt(selRow, this.getColumnByName(pltTable, PatternsTableModel.columns.nS.toString()));
//
//                DataSet patt = this.datasets.get(pattern);
//                DataSerie ds = patt.getDataSeries().get(serie);

                DataSerie ds = ((PatternsTableModel)pltTable.getModel()).getDSAtRow(selRow);

                if (ds.getWavelength()<0){
                    log.warning(String.format("%s has no wavelength assigned, skipping", ds.getName()));
                    continue;
                }

                ds.convertDStoWavelength(newWL);

            }
            this.updateFullTable();
            plotpanel.actualitzaPlot();
       }
    }
    
    private void removeExtensionFromNames(ActionEvent e){
        if (pltTable.getSelectedRow()<0)return;
        if (pltTable.getRowCount()<=0)return;
        //prova amb selected rows:
        int[] selRows = pltTable.getSelectedRows();
        if (selRows.length==0)return;
        for (int i=0; i<selRows.length;i++){
            int selRow = selRows[i];
//            int indexP = (Integer) pltTable.getModel().getValueAt(selRow, this.getColumnByName(pltTable, PatternsTableModel.columns.nP.toString()));
//            int indexDS = (Integer) pltTable.getModel().getValueAt(selRow, this.getColumnByName(pltTable, PatternsTableModel.columns.nS.toString()));
//            DataSerie ds = this.datasets.get(indexP).getDataSeries().get(indexDS);
            DataSerie ds = ((PatternsTableModel)pltTable.getModel()).getDSAtRow(selRow);
            ds.setName(FileUtils.getFNameNoExt(ds.getName()));            
        }
        this.updateFullTable();
        plotpanel.actualitzaPlot(); //llegenda
    }
    
 /**************************************************************************************
 * OTHER DATA MANIPLUATION METHODS
 */    
    public List<DataSerie> getSelectedSeriesByType(SerieType st){
        List<DataSerie> dss = new ArrayList<DataSerie>();
        for (DataSerie ds:selectedSeries) {
            if (ds.getSerieType()==st) {
                dss.add(ds);   
            }
        }
        return dss;
    }
    
    public List<DataSerie> getAllSeriesByType(SerieType st){
        List<DataSerie> dss = new ArrayList<DataSerie>();
        for (DataSet ds:datasets) {
            dss.addAll(ds.getDataSeriesByType(st));
        }
        return dss;
    }
    
    public DataSerie getFirstSelectedDataSerie() {
        if (selectedSeries.size()>0)return selectedSeries.get(0);
        return null;
    }
    
    public DataSerie getFirstPlottedDataSerie() {
        for (DataSerie ds:((PatternsTableModel)pltTable.getModel()).getRowDSList()) {
            if (ds.isPlotThis())return ds;
        }
        return null;
    }
    
    public DataSet getFirstSelectedDataSet() {
        return this.getFirstSelectedDataSerie().getParent();
    }
    
    public DataSerie getMainSerieOfSelectedPlottable() {
        return this.getFirstSelectedDataSerie().getParent().getMainSerie();
    }
    
    //duplicarem les series afegint totes les seleccionades a un mateix plottable
    public void duplicateSelected() {
        DataSet dc = new DataSet(getFirstSelectedDataSerie().getWavelength());
        for (DataSerie ds:getSelectedPlottables()) {
            dc.addDataSerie(new DataSerie(ds,ds.getSerieType(),true)); //dupliquem serie
        }
        this.addDataSet(dc,true,true);
    }
    
    //NO ACTUALITZA!
    private void swapDataSets(int origen, int desti) {
        try {
            Collections.swap(datasets, origen, desti);    
        }catch(IndexOutOfBoundsException ex) {
            log.debug("cannot move dataset");
        }
    }
    
    private void swapDataSeries(DataSet dset, int origen, int desti) {
        try {
            Collections.swap(dset.getDataSeries(), origen, desti);    
        }catch(IndexOutOfBoundsException ex) {
            log.debug("cannot move dataserie");
        }
    }
    
    public void moveSelectedPlottablesUp() {
        
        for (int i=0; i<selectedSeries.size();i++) {
            DataSerie ds = selectedSeries.get(i);
            DataSet dset = ds.getParent();
            int actualPos = dset.indexOfDS(ds);
//            log.infof("** SelectedSerie %d:%s",i,ds.getName());
            if (actualPos==0) {//cannot move serie UP, we have to move DataSet
                int dsetPos = datasets.indexOf(dset);
//                log.infof(" Is first serie of DataSet %d",dsetPos);
                if (dsetPos>0) {
                    swapDataSets(dsetPos,dsetPos-1);
//                    log.infof(" moving dataset from %d to %d",dsetPos,dsetPos-1);
                }
            }else {
                //move dataSerie UP
//                log.infof(" moving serie from %d to %d",actualPos,actualPos-1);
                swapDataSeries(dset,actualPos, actualPos-1);    
            }
        }
        
        
        //primer movem DINS cada DataSet i si trobem que el zero també hi és movem dataset
        //HE D'ANAR DE BAIX A DALT PERQUE NO PASSIN COSES RARES
//        for (int i=selectedSeries.size()-1;i>=0;i--) {
//            DataSerie ds = selectedSeries.get(i);
//            DataSet dset = ds.getParent();
//            int actualPos = dset.indexOfDS(ds);
//            if (actualPos==0) {
//                //movem datasets
//                int dsetPos = datasets.indexOf(dset);
//                swapDataSets(dsetPos,dsetPos-1);
//            }else {
//                //movem dataseries
//                swapDataSeries(dset,actualPos, actualPos-1);    
//            }
//        }
        this.updateFullTable();
        plotpanel.actualitzaPlot(); //canvia ordre vista
    }
    
    public void moveSelectedPlottablesDown() {
        
        for (int i=selectedSeries.size()-1;i>=0;i--) {
            DataSerie ds = selectedSeries.get(i);
            DataSet dset = ds.getParent();
            int actualPos = dset.indexOfDS(ds);
//            log.infof("** SelectedSerie %d:%s",i,ds.getName());
            if (actualPos==dset.getNSeries()-1) {//cannot move serie DOWN, we have to move DataSet
                int dsetPos = datasets.indexOf(dset);
//                log.infof(" Is last serie of DataSet %d",dsetPos);
                if (dsetPos<datasets.size()-1) {
                    swapDataSets(dsetPos,dsetPos+1);
//                    log.infof(" moving dataset from %d to %d",dsetPos,dsetPos+1);
                }
            }else {
                //move dataSerie DOWN inside dataset
//                log.infof(" moving serie from %d to %d",actualPos,actualPos+1);
                swapDataSeries(dset,actualPos, actualPos+1);    
            }
        }
        
        
        //primer movem DINS cada DataSet i si trobem que es el de index més gran també hi és movem dataset
        //HE D'ANAR DE DALT a BAIX PERQUE NO PASSIN COSES RARES
//        for (int i=0;i<selectedSeries.size();i++) {
//            DataSerie ds = selectedSeries.get(i);
//            DataSet dset = ds.getParent();
//            int actualPos = dset.indexOfDS(ds);
//            if (actualPos==dset.getNSeries()-1) {
//                //movem datasets
//                int dsetPos = datasets.indexOf(dset);
//                swapDataSets(dsetPos,dsetPos+1);
//            }else {
//                //movem dataseries
//                swapDataSeries(dset,actualPos, actualPos+1);    
//            }
//        }
        this.updateFullTable();
        plotpanel.actualitzaPlot(); //canvia ordre vista
    }
    
    public boolean isOneSerieSelected(){
        if (this.selectedSeries.isEmpty()){
            log.warning("Select a serie first");
            return false;
        }
        if (this.selectedSeries.size()>1){
            log.warning("Select ONE serie only");
            return false;
        }
        return true;
    }
    
    public void invertOrderTable() {
        Collections.reverse(this.datasets);
        this.updateFullTable();
        plotpanel.actualitzaPlot();
    }
    
/**************************************************************************************
 * IMPLEMENTATIONS
 */
    @Override
    public List<DataSerie> getPlottables() {
        List<DataSerie> dss = new ArrayList<DataSerie>();
        for (DataSet s:datasets) {
           for (DataSerie ds: s.getDataSeries()) {
               dss.add(ds);
           }
        }
        return dss;
    }

    @Override
    public List<DataSerie> getSelectedPlottables() {
        return selectedSeries;
    }

    @Override
    public <T1 extends Plot1DPanel<?>> void setPlotPanel(T1 ppanel) {
        this.plotpanel=ppanel;
    }

    @Override
    public boolean arePlottables() {
        if (datasets.size()>0)return true;
        return false;
    }

    @Override
    public boolean areSelectedPlottables() {
        if(selectedSeries.size()>0)return true;
        return false;
    }

    @Override
    public int getNPlottables() {
        int i = 0;
        for (DataSet s:datasets) {
            i = i + s.getNSeries();
        }
        return i;
    }

    @Override
    public int getNSelectedPlottables() {
        return selectedSeries.size();
    }

    @Override
    public int getNplottablesOfSerieTypes(boolean onlyVisibles, SerieType... sts) {
        int nserie = 0;
        for (DataSet s: datasets) {
            for (DataSerie ds: s.getDataSeries()) {
                //nomes visibles?
                if ((onlyVisibles)&&(!ds.isPlotThis()))continue;
                for (SerieType st1 :sts) {
                    if (ds.getSerieType()==st1) {
                        nserie++;
                        continue;
                    }
                }
            }
        }
        return nserie;
    }

    @Override
    public void reAssignColorPlottables(boolean onlyPlotted, SerieType... st) {
        int nserie = 0;
        for (DataSet s: datasets) {
            for (DataSerie ds: s.getDataSeries()) {
                //nomes visibles
                if ((onlyPlotted)&&(!ds.isPlotThis()))continue;
                for (SerieType st1 :st) {
                    if (ds.getSerieType()==st1) {
                        this.paintIt(nserie,ds);
                        nserie++;
                        continue;
                    }
                }
            }
        }
        this.updateFullTable();//IDEALMENT NOMES ACTUALITZAR ELS COLORS DE LA TAULA... COM? TODO
        plotpanel.actualitzaPlot();
    }

    private void paintIt(int nserie, DataSerie ds) {
        if (plotpanel.isLightTheme()){
            int ncol = (nserie)%D1Dplot_global.lightColors.length;
            ds.setColor(FileUtils.parseColorName(D1Dplot_global.lightColors[ncol]));    
        }else {
            int ncol = (nserie)%D1Dplot_global.DarkColors.length;
            ds.setColor(FileUtils.parseColorName(D1Dplot_global.DarkColors[ncol]));
        } 
    }
    
//    private int getRowOfADataSerie(DataSerie ds) {
//        int iDset = datasets.indexOf(ds.getParent());
//        int nseriesDset = ds.getParent().getNSeries();
//        int row = iDset +
//        
//        
//    }
    
    private void reselectRowsFromSelectedSeries() {
        List<DataSerie> toDeSelect = new ArrayList<DataSerie>();
        int[] rowsToSelect = new int[selectedSeries.size()];
        int i = 0;
        for (DataSerie ds:selectedSeries) {
            PatternsTableModel model = (PatternsTableModel) pltTable.getModel();
            int row = model.getRowOfDS(ds);
            rowsToSelect[i]=row;
            if (row<0) toDeSelect.add(ds);
//            if (row>=0) {
//                pltTable.addRowSelectionInterval(row, row);    
//            }else {//no existeix ja...
//                toDeSelect.add(ds);
//            }
            i++;
        }
        selectedSeries.removeAll(toDeSelect);
        for (i=0;i<rowsToSelect.length;i++) {
            if (rowsToSelect[i]>=0)pltTable.addRowSelectionInterval(rowsToSelect[i], rowsToSelect[i]);
        }
            
    }
    
    @Override
    public void updateFullTable() {
        PatternsTableModel model = (PatternsTableModel) pltTable.getModel();
        model.setRowCount(0);
        for (DataSet s: datasets) {
            for (DataSerie ds: s.getDataSeries()) {
                this.addToTable(ds);
            }
        }
        
        //ara hauriem de mantenir els seleccionats...
        
//        List<DataSerie> toDeSelect = new ArrayList<DataSerie>();
//        for (DataSerie ds: selectedSeries) {
//            
//            pltTable.addRowSelectionInterval(index0, index1);
//        }
        reselectRowsFromSelectedSeries();
        aplicarselecciotaula(null); //TODO revisar que a vegades no fa el que volem...
    }

}

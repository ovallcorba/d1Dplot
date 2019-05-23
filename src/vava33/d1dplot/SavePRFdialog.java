package com.vava33.d1dplot;

/**
 * D1Dplot
 * 
 * Dialog to save profile file type (obs/calc/hkl)
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.vava33.d1dplot.auxi.PatternsTableCellRenderer;
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.d1dplot.data.Plottable;
import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.LineBorder;

import java.awt.Color;

import javax.swing.ListSelectionModel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.JScrollPane;

public class SavePRFdialog {

	private JDialog savePRFdialog;
    private PlotPanel plotpanel;
    
//    private static final String className = "SaveProfDialog";
//    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);
    
    private JPanel contentPanel;
    private JTable tableDS1;
    private JTable tableDS2;
    private JTable tableDS3;
    
    /**
     * Create the dialog.
     */
    public SavePRFdialog(PlotPanel p) {
        this.savePRFdialog = new JDialog(D1Dplot_global.getD1DmainFrame(),"Save OBS/CALC/DIFF matching",true);
        this.plotpanel = p;
        this.contentPanel = new JPanel();
        savePRFdialog.setIconImage(D1Dplot_global.getIcon());
        savePRFdialog.setBounds(100, 100, 814, 240);
        savePRFdialog.getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        savePRFdialog.getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new MigLayout("", "[grow][grow][grow]", "[grow]"));
        {
            JPanel panel = new JPanel();
            panel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
            contentPanel.add(panel, "cell 0 0,grow");
            panel.setLayout(new MigLayout("", "[grow]", "[][grow]"));
            {
                JLabel lblSelectBasePattern = new JLabel("Observed");
                panel.add(lblSelectBasePattern, "cell 0 0");
            }
            {
                {
                    JScrollPane scrollPane = new JScrollPane();
                    panel.add(scrollPane, "cell 0 1,grow");
                    tableDS1 = new JTable();
                    scrollPane.setViewportView(tableDS1);
                    tableDS1.setDefaultRenderer(String.class, new PatternsTableCellRenderer());
                    tableDS1.setDefaultRenderer(Integer.class, new PatternsTableCellRenderer());
                    tableDS1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    tableDS1.setColumnSelectionAllowed(false);
                    tableDS1.setCellSelectionEnabled(false);
                }
            }
        }
        {
            JPanel panel = new JPanel();
            panel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            contentPanel.add(panel, "cell 1 0,grow");
            panel.setLayout(new MigLayout("", "[grow]", "[][grow]"));
            {
                JLabel lblSelectPatternTo = new JLabel("Caclulated");
                panel.add(lblSelectPatternTo, "cell 0 0");
            }
            {
                {
                    JScrollPane scrollPane = new JScrollPane();
                    panel.add(scrollPane, "cell 0 1,grow");
                    tableDS2 = new JTable();
                    scrollPane.setViewportView(tableDS2);
                    tableDS2.setDefaultRenderer(String.class, new PatternsTableCellRenderer());
                    tableDS2.setDefaultRenderer(Integer.class, new PatternsTableCellRenderer());
                    tableDS2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    tableDS2.setColumnSelectionAllowed(false);
                    tableDS2.setCellSelectionEnabled(false);
                }
            }
        }
        {
            {
                JPanel panel = new JPanel();
                panel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
                contentPanel.add(panel, "cell 2 0,grow");
                panel.setLayout(new MigLayout("", "[grow]", "[][grow]"));
                {
                    JLabel lblHkls = new JLabel("hkl(s)");
                    panel.add(lblHkls, "cell 0 0");
                }
                {
                    JScrollPane scrollPane = new JScrollPane();
                    panel.add(scrollPane, "cell 0 1,grow");
                    {
                        tableDS3 = new JTable();
                        scrollPane.setViewportView(tableDS3);
                        tableDS3.setDefaultRenderer(String.class, new PatternsTableCellRenderer());
                        tableDS3.setDefaultRenderer(Integer.class, new PatternsTableCellRenderer());
                        tableDS3.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                        tableDS3.setColumnSelectionAllowed(false);
                        tableDS3.setCellSelectionEnabled(false);
                        
                    }
                }
            }
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
            savePRFdialog.getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("Ok");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        do_okButton_actionPerformed(e);
                    }
                });
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                savePRFdialog.getRootPane().setDefaultButton(okButton);
            }
        }
        
        inicia();
    }

    private void inicia(){
        updateTables();
    }
    
    private void updateTables() {
        DefaultTableModel mod = new DefaultTableModel(
                new Object[][] {
                },
                new String[] {
                        "nP", "nS", "Name"
                }
                ){
                    private static final long serialVersionUID = 1L;
                    Class<?>[] columnTypes = new Class[] {
                            Integer.class, Integer.class, String.class
                    };
                    public Class<?> getColumnClass(int columnIndex) {
                        return columnTypes[columnIndex];
                    }
                };
        mod.setRowCount(0);
        for (int i=0;i<plotpanel.getNplottables();i++) {
            Plottable p = plotpanel.getPlottable(i);
            int np=plotpanel.indexOfPlottableData(p);
            for (DataSerie d: p.getDataSeries()) {
                if(d.isEmpty())continue;
                int nd = p.indexOfDS(d);
                Object[] row = {np,nd,d.serieName};
                mod.addRow(row);
            }
        }
        tableDS1.setModel(mod);
        tableDS2.setModel(mod);
        tableDS3.setModel(mod);
    }

    DataSerie dsOBS;
    DataSerie dsCALC;
    List<DataSerie> dsHKL;
    
    private void do_okButton_actionPerformed(ActionEvent e) {
        dsOBS = this.getOBS();
        dsCALC = this.getCALC();
        dsHKL = this.getHKLs();
    	savePRFdialog.dispose();
    }

    DataSerie getOBS() {
        int r1 = tableDS1.getSelectedRow();
        if (r1<0)return dsOBS;
        int np1 = (Integer) tableDS1.getValueAt(r1, 0);
        int nds1 = (Integer) tableDS1.getValueAt(r1, 1);
        return plotpanel.getPlottable(np1).getDataSerie(nds1);
    }
    DataSerie getCALC() {
        int r1 = tableDS2.getSelectedRow();
        if (r1<0)return dsCALC;
        int np1 = (Integer) tableDS2.getValueAt(r1, 0);
        int nds1 = (Integer) tableDS2.getValueAt(r1, 1);
        return plotpanel.getPlottable(np1).getDataSerie(nds1);
    }
    List<DataSerie> getHKLs() {
        List<DataSerie> hkls = new ArrayList<DataSerie>();
        int[] r1 = tableDS3.getSelectedRows();
        if (r1.length==0)return dsHKL;
        for (int i=0;i<r1.length;i++) {
            int np1 = (Integer) tableDS1.getValueAt(r1[i], 0);
            int nds1 = (Integer) tableDS1.getValueAt(r1[i], 1);
            hkls.add(plotpanel.getPlottable(np1).getDataSerie(nds1));
        }
        return hkls;
    }
    
    public void visible(boolean vis) {
    	this.savePRFdialog.setVisible(vis);
    	if (vis)inicia();
    }
}

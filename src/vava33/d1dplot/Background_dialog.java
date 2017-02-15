package vava33.d1dplot;

/**
 * D1Dplot
 * 
 * Background subtraction dialog
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

import net.miginfocom.swing.MigLayout;

import javax.swing.border.TitledBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.LineBorder;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

import javax.swing.ButtonGroup;

import vava33.d1dplot.auxi.DataSerie;
import vava33.d1dplot.auxi.PattOps;

import com.vava33.jutils.VavaLogger;

public class Background_dialog extends JDialog {

    private PlotPanel plotpanel;
    private static VavaLogger log = D1Dplot_global.getVavaLogger(Background_dialog.class.getName());

    private final JPanel contentPanel = new JPanel();
    private JTextField txtNveins;
    private JTextField txtTini;
    private JTextField txtTsup;
    private JTable table;
    private JTextField txtNiter;
    private JTextField txtDeg;
    private JLabel lbltinf;
    private JLabel lbltsup;
    private JButton btnAdd;
    private JButton btnDel;
    private JCheckBox chckbxMulti;
    private final ButtonGroup buttonGroup = new ButtonGroup();
    private JCheckBox chckbxShowBackground;
    private JRadioButton rdbtnNormal;
    private JRadioButton rdbtnInverse;
    
    /**
     * Create the dialog.
     */
    public Background_dialog(PlotPanel p) {
        this.setPlotpanel(p);
        setBounds(100, 100, 451, 553);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new MigLayout("", "[grow]", "[][grow][grow][]"));
        {
            chckbxShowBackground = new JCheckBox("Show Background");
            chckbxShowBackground.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    do_chckbxShowBackground_itemStateChanged(e);
                }
            });
            contentPanel.add(chckbxShowBackground, "cell 0 0");
        }
        {
            JPanel panel = new JPanel();
            panel.setBorder(new TitledBorder(null, "Bruchner", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            contentPanel.add(panel, "cell 0 1,grow");
            panel.setLayout(new MigLayout("", "[][grow][]", "[][][][][][][grow][]"));
            {
                JLabel lblN = new JLabel("N");
                panel.add(lblN, "cell 0 0,alignx trailing");
            }
            {
                txtNveins = new JTextField();
                txtNveins.setText("nveins");
                panel.add(txtNveins, "cell 1 0,growx");
                txtNveins.setColumns(10);
            }
            {
                chckbxMulti = new JCheckBox("multi");
                chckbxMulti.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        do_chckbxMulti_itemStateChanged(e);
                    }
                });
                panel.add(chckbxMulti, "cell 2 0");
            }
            {
                JLabel lblNiter = new JLabel("Niter");
                panel.add(lblNiter, "cell 0 1,alignx trailing");
            }
            {
                txtNiter = new JTextField();
                txtNiter.setText("niter");
                panel.add(txtNiter, "cell 1 1,growx");
                txtNiter.setColumns(10);
            }
            {
                JButton btnIterate = new JButton("Iterate");
                btnIterate.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        do_btnIterate_actionPerformed(e);
                    }
                });
                panel.add(btnIterate, "cell 2 1,growx");
            }
            {
                JLabel lblPatternEdge = new JLabel("Pattern Edge");
                panel.add(lblPatternEdge, "cell 0 2");
            }
            {
                rdbtnNormal = new JRadioButton("Normal");
                rdbtnNormal.setSelected(true);
                buttonGroup.add(rdbtnNormal);
                panel.add(rdbtnNormal, "cell 1 2");
            }
            {
                rdbtnInverse = new JRadioButton("Inverse");
                buttonGroup.add(rdbtnInverse);
                panel.add(rdbtnInverse, "cell 1 3");
            }
            {
                lbltinf = new JLabel("2Tinf");
                panel.add(lbltinf, "cell 0 4,alignx trailing");
            }
            {
                txtTini = new JTextField();
                txtTini.setText("t2ini");
                panel.add(txtTini, "cell 1 4,growx");
                txtTini.setColumns(10);
            }
            {
                btnAdd = new JButton("Add");
                btnAdd.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        do_btnAdd_actionPerformed(e);
                    }
                });
                panel.add(btnAdd, "cell 2 4,growx");
            }
            {
                lbltsup = new JLabel("2Tsup");
                panel.add(lbltsup, "cell 0 5,alignx trailing");
            }
            {
                txtTsup = new JTextField();
                txtTsup.setText("t2sup");
                panel.add(txtTsup, "cell 1 5,growx");
                txtTsup.setColumns(10);
            }
            {
                btnDel = new JButton("Del");
                btnDel.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        do_btnDel_actionPerformed(e);
                    }
                });
                panel.add(btnDel, "cell 2 5,growx");
            }
            {
                JScrollPane scrollPane = new JScrollPane();
                panel.add(scrollPane, "cell 0 6 3 1,grow");
                {
                    table = new JTable();
                    table.setModel(new DefaultTableModel(
                        new Object[][] {
                        },
                        new String[] {
                            "T2i", "T2f", "N"
                        }
                    ) {
                        Class[] columnTypes = new Class[] {
                            Double.class, Double.class, Integer.class
                        };
                        public Class getColumnClass(int columnIndex) {
                            return columnTypes[columnIndex];
                        }
                    });
                    scrollPane.setViewportView(table);
                }
            }
        }
        {
            JPanel panel = new JPanel();
            panel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Other Functions", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
            contentPanel.add(panel, "cell 0 2,grow");
            panel.setLayout(new MigLayout("", "[][][grow]", "[][][]"));
            {
                JButton btnFitPolynomial = new JButton("Fit Polynomial");
                panel.add(btnFitPolynomial, "cell 0 0");
            }
            {
                JLabel lblDegree = new JLabel("degree");
                panel.add(lblDegree, "cell 1 0,alignx trailing");
            }
            {
                txtDeg = new JTextField();
                txtDeg.setText("6");
                panel.add(txtDeg, "cell 2 0,growx");
                txtDeg.setColumns(10);
            }
            {
                JButton btnFitSpline = new JButton("Fit Spline");
                panel.add(btnFitSpline, "cell 0 1,growx");
            }
        }
        {
            JButton btnSaveBackgroundAs = new JButton("Save Background as new Serie");
            contentPanel.add(btnSaveBackgroundAs, "cell 0 3,growx");
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        do_okButton_actionPerformed(e);
                    }
                });
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
        }
        
        
        
        
        
        
        
        inicia();
    }

    private void inicia(){
        btnDel.setEnabled(false);
        btnAdd.setEnabled(false);
        txtTini.setEnabled(false);
        txtTsup.setEnabled(false);
        lbltinf.setEnabled(false);
        lbltsup.setEnabled(false);
        table.setEnabled(false);
        chckbxShowBackground.setSelected(true);
    }
    
    public PlotPanel getPlotpanel() {
        return plotpanel;
    }

    public void setPlotpanel(PlotPanel plotpanel) {
        this.plotpanel = plotpanel;
    }

    protected void do_okButton_actionPerformed(ActionEvent e) {
        this.dispose();
    }
    
    protected void do_chckbxMulti_itemStateChanged(ItemEvent e) {
        if(this.chckbxMulti.isSelected()){
            btnDel.setEnabled(true);
            btnAdd.setEnabled(true);
            txtTini.setEnabled(true);
            txtTsup.setEnabled(true);
            lbltinf.setEnabled(true);
            lbltsup.setEnabled(true);
            table.setEnabled(true);
        }else{
            btnDel.setEnabled(false);
            btnAdd.setEnabled(false);
            txtTini.setEnabled(false);
            txtTsup.setEnabled(false);
            lbltinf.setEnabled(false);
            lbltsup.setEnabled(false);
            table.setEnabled(false);
        }
    }
    protected void do_chckbxShowBackground_itemStateChanged(ItemEvent e) {
        this.getPlotpanel().setShowBackground(chckbxShowBackground.isSelected());
        plotpanel.repaint();
    }
    protected void do_btnAdd_actionPerformed(ActionEvent e) {
        // afegeix els valors a la taula
        if (!txtTsup.getText().equals("")&&!txtTini.getText().equals("")&&!txtNveins.getText().equals("")){
            Object[] row = new Object[3];
            try{
                row[0]=Double.parseDouble(txtTini.getText());
                row[1]=Double.parseDouble(txtTsup.getText());
                row[2]=Integer.parseInt(txtNveins.getText());
            }catch(Exception ex){
                ex.printStackTrace();
                log.info("error reading parameters");
            }
            ((DefaultTableModel)table.getModel()).addRow(row);
        }else{
            log.info("2Tinf, 2Tsup and N must be entered");
        }
    }
    protected void do_btnDel_actionPerformed(ActionEvent e) {
        // Elimina la fila seleccionada de la taula
        int numRows = table.getSelectedRows().length;
        for(int i=0; i<numRows; i++){
            ((DefaultTableModel)table.getModel()).removeRow(table.getSelectedRow());
        }
    }
    
    protected void do_btnIterate_actionPerformed(ActionEvent e) {
        
        if (plotpanel.getSelectedSerie().isEmpty()){
            log.info("select a serie first");
            return;
        }
        
        try{
            int niter = Integer.parseInt(txtNiter.getText());   
            int nveins = Integer.parseInt(txtNveins.getText());
            DataSerie bkg = PattOps.bruchner(plotpanel.getSelectedSerie().get(0), niter, nveins, rdbtnNormal.isSelected(),chckbxMulti.isSelected(),((DefaultTableModel)table.getModel()));
            bkg.setColor(Color.BLACK);
            bkg.setTipusSerie(DataSerie.serieType.bkg); //TODO fer algo amb aixo
            plotpanel.setBkgserie(bkg);
            plotpanel.repaint();
        }catch(Exception ex){
            ex.printStackTrace();
        }

        
        

    }
}

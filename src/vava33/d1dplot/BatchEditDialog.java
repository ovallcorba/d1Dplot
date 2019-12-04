package com.vava33.d1dplot;

/**
 * D1Dplot
 *      
 * Dialog to batch edit multiple entries of a DataSerie
 * 
 * @author Oriol Vallcorba
 * Licence: GPLv3
 *  
 **/

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.vava33.BasicPlotPanel.BasicPlotPanelFrontEnd;
import com.vava33.BasicPlotPanel.BasicSerie;
import com.vava33.BasicPlotPanel.core.Plottable;
import com.vava33.BasicPlotPanel.core.SerieType;
import com.vava33.jutils.VavaLogger;

import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class BatchEditDialog<T extends BasicSerie> extends JDialog {

    private static final long serialVersionUID = 1L;
    private final JPanel contentPanel = new JPanel();
    VavaLogger log = BasicPlotPanelFrontEnd.getLog();
    List<T> seriesToEdit;  //POSO PLOTTABLE PER FER-HO GENERAL I PODER EDITAR SUBCLASSES DE BASICSERIE... pero si es vol editar ALTRES valors de les taules cal fer-ho especific
    private JTextField txtMarker;
    private JTextField txtLine;
    private JTextField txtName;
    private JLabel lblEditing;
    private JCheckBox chckbxApplyToAllName;
    private JCheckBox chckbxApplyToAllColor;
    private JCheckBox chckbxApplyToAllLine;
    private JCheckBox chckbxApplyToAllMarker;
    private JCheckBox chckbxApplyToAllType;
    private JCheckBox chckbxApplyToAllShow;
    private JCheckBox chckbxShow;
    private JComboBox<String> comboType;
    private JLabel lblColor_1;
    private JLabel lblYoff;
    private JTextField txtYoff;
    private JCheckBox chckbxApplyToAllYoff;
    /**
     * Create the dialog.
     */
    public BatchEditDialog(List<T> plts) {
        setTitle("Edit serie(s)");
        this.seriesToEdit=plts;
        setBounds(100, 100, 450, 300);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new MigLayout("", "[][grow][]", "[][][][][][][][]"));
        {
            lblEditing = new JLabel("Editing 1 serie");
            contentPanel.add(lblEditing, "cell 0 0 3 1");
        }
        {
            JLabel lblName = new JLabel("Name");
            contentPanel.add(lblName, "cell 0 1,alignx trailing");
        }
        {
            txtName = new JTextField();
            contentPanel.add(txtName, "cell 1 1,growx");
            txtName.setColumns(10);
        }
        {
            chckbxApplyToAllName = new JCheckBox("Apply to all");
            contentPanel.add(chckbxApplyToAllName, "cell 2 1");
        }
        {
            JLabel lblColor = new JLabel("Color");
            contentPanel.add(lblColor, "cell 0 2,alignx trailing");
        }
        {
            lblColor_1 = new JLabel("");
            lblColor_1.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    do_lblColor_1_mouseReleased(e);
                }
            });
            contentPanel.add(lblColor_1, "cell 1 2,grow");
        }
        {
            chckbxApplyToAllColor = new JCheckBox("Apply to all");
            contentPanel.add(chckbxApplyToAllColor, "cell 2 2");
        }
        {
            JLabel lblLineWidth = new JLabel("Line width");
            contentPanel.add(lblLineWidth, "cell 0 3,alignx trailing");
        }
        {
            txtLine = new JTextField();
            contentPanel.add(txtLine, "cell 1 3,growx");
            txtLine.setColumns(10);
        }
        {
            chckbxApplyToAllLine = new JCheckBox("Apply to all");
            contentPanel.add(chckbxApplyToAllLine, "cell 2 3");
        }
        {
            JLabel lblMarkerSize = new JLabel("Marker size");
            contentPanel.add(lblMarkerSize, "cell 0 4,alignx trailing");
        }
        {
            txtMarker = new JTextField();
            contentPanel.add(txtMarker, "cell 1 4,growx");
            txtMarker.setColumns(10);
        }
        {
            chckbxApplyToAllMarker = new JCheckBox("Apply to all");
            contentPanel.add(chckbxApplyToAllMarker, "cell 2 4");
        }
        {
            lblYoff = new JLabel("Y offset");
            contentPanel.add(lblYoff, "cell 0 5,alignx trailing");
        }
        {
            txtYoff = new JTextField();
            contentPanel.add(txtYoff, "cell 1 5,growx");
            txtYoff.setColumns(10);
        }
        {
            chckbxApplyToAllYoff = new JCheckBox("Apply to all");
            contentPanel.add(chckbxApplyToAllYoff, "cell 2 5");
        }
        {
            JLabel lblType = new JLabel("Type");
            contentPanel.add(lblType, "cell 0 6,alignx trailing");
        }
        {
            comboType = new JComboBox<String>();
            contentPanel.add(comboType, "cell 1 6,growx");
            for (SerieType s :SerieType.values()){
                comboType.addItem(s.name());
            }
        }
        {
            chckbxApplyToAllType = new JCheckBox("Apply to all");
            contentPanel.add(chckbxApplyToAllType, "cell 2 6");
        }
        {
            chckbxShow = new JCheckBox("Show");
            contentPanel.add(chckbxShow, "cell 1 7");
        }
        {
            chckbxApplyToAllShow = new JCheckBox("Apply to all");
            contentPanel.add(chckbxApplyToAllShow, "cell 2 7");
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("Apply and close");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        do_okButton_actionPerformed(e);
                    }
                });
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        do_cancelButton_actionPerformed(e);
                    }
                });
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }
        }
        inicia();
    }
    
    private void inicia() {
        lblEditing.setText(String.format("Editing %d serie(s)", seriesToEdit.size()));
        
        txtName.setText(seriesToEdit.get(0).getName());
        lblColor_1.setOpaque(true);
        lblColor_1.setBackground(seriesToEdit.get(0).getColor());
        txtLine.setText(Float.toString(seriesToEdit.get(0).getLineWidth()));
        txtMarker.setText(Float.toString(seriesToEdit.get(0).getMarkerSize()));
        comboType.setSelectedItem(seriesToEdit.get(0).getSerieType().name());
        chckbxShow.setSelected(seriesToEdit.get(0).isPlotThis());
        txtYoff.setText(Double.toString(seriesToEdit.get(0).getYOffset()));
        
        if (seriesToEdit.size()==1) {
            chckbxApplyToAllColor.setSelected(true);
            chckbxApplyToAllLine.setSelected(true);
            chckbxApplyToAllName.setSelected(true);
            chckbxApplyToAllMarker.setSelected(true);
            chckbxApplyToAllType.setSelected(true);
            chckbxApplyToAllShow.setSelected(true);
            chckbxApplyToAllYoff.setSelected(true);
        }
    }

    protected void do_lblColor_1_mouseReleased(MouseEvent e) {
        Color newColor = JColorChooser.showDialog(
                this,
                "Choose Color",
                Color.BLACK);
        if(newColor != null){
            lblColor_1.setBackground(newColor);
        }
    }
    
    protected void do_okButton_actionPerformed(ActionEvent e) {
        for (Plottable p:seriesToEdit) {
            if (chckbxApplyToAllColor.isSelected()) {
                p.setColor(lblColor_1.getBackground());
            }
            if (chckbxApplyToAllName.isSelected()) {
                p.setName(txtName.getText());
            }
            if (chckbxApplyToAllLine.isSelected()) {
                try {
                    p.setLineWidth(Float.parseFloat(txtLine.getText()));    
                }catch(Exception ex) {
                    log.warning("Error reading line width");
                }
            }
            if (chckbxApplyToAllMarker.isSelected()) {
                try {
                    p.setMarkerSize(Float.parseFloat(txtMarker.getText()));    
                }catch(Exception ex) {
                    log.warning("Error reading marker size");
                }
            }
            if (chckbxApplyToAllShow.isSelected()) {
                p.setPlotThis(chckbxShow.isSelected());
            }
            if (chckbxApplyToAllType.isSelected()) {
                p.setSerieType(SerieType.getEnum((String) comboType.getSelectedItem()));
            }
            if (chckbxApplyToAllYoff.isSelected()) {
                try {
                    p.setYOffset(Double.parseDouble(txtYoff.getText()));    
                }catch(Exception ex) {
                    log.warning("Error reading Y offset");
                }
            }
        }

        this.dispose();
    }
    
    
    
    protected void do_cancelButton_actionPerformed(ActionEvent e) {
        this.dispose();
    }
}

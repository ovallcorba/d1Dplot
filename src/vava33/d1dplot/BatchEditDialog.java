
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
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.commons.math3.util.FastMath;

import com.vava33.BasicPlotPanel.core.SerieType;
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.d1dplot.data.Xunits;
import com.vava33.jutils.FileUtils;
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
import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;


public class BatchEditDialog extends JDialog {

    private static final String className = "BatchEditDialog";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);
    
    private static final long serialVersionUID = 1L;
    private final JPanel contentPanel = new JPanel();
    List<DataSerie> seriesToEdit;  //POSO PLOTTABLE PER FER-HO GENERAL I PODER EDITAR SUBCLASSES DE BASICSERIE... pero si es vol editar ALTRES valors de les taules cal fer-ho especific
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
    private JLabel lblNewLabel;
    private JCheckBox chckbxApplyToAllScale;
    private JLabel lblZeroOffset;
    private JLabel lblWavelength;
    private JLabel lblXUnits;
    private JCheckBox chckbxErrorBars;
    private JCheckBox chckbxApplyToAllZero;
    private JCheckBox chckbxApplyToAllWave;
    private JCheckBox chckbxApplyToAllXunits;
    private JCheckBox chckbxApplyToAllErrBars;
    private JTextField txtScale;
    private JTextField txtZero;
    private JTextField txtwave;
    private JComboBox<String> comboBoxXunits;
    /**
     * Create the dialog.
     */
    public BatchEditDialog(List<DataSerie> plts) {
        setTitle("Edit serie(s)");
        this.seriesToEdit=plts;
//        setBounds(100, 100, 454, 550);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new MigLayout("", "[][grow][]", "[][][][][][][][][][][][][]"));
        {
            lblEditing = new JLabel("Editing 1 serie");
            lblEditing.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
            lblEditing.setHorizontalAlignment(SwingConstants.CENTER);
            lblEditing.setOpaque(true);
            lblEditing.setForeground(Color.YELLOW);
            lblEditing.setBackground(Color.BLACK);
            lblEditing.setFont(new Font("Dialog", Font.BOLD, 14));
            contentPanel.add(lblEditing, "cell 0 0 3 1,growx");
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
            lblNewLabel = new JLabel("Scale");
            contentPanel.add(lblNewLabel, "cell 0 3,alignx trailing");
        }
        {
            txtScale = new JTextField();
            txtScale.setText("1.0");
            contentPanel.add(txtScale, "cell 1 3,growx");
            txtScale.setColumns(10);
        }
        {
            chckbxApplyToAllScale = new JCheckBox("Apply to all");
            contentPanel.add(chckbxApplyToAllScale, "cell 2 3");
        }
        {
            lblZeroOffset = new JLabel("Zero offset");
            contentPanel.add(lblZeroOffset, "cell 0 4,alignx trailing");
        }
        {
            txtZero = new JTextField();
            txtZero.setText("0.0");
            contentPanel.add(txtZero, "cell 1 4,growx");
            txtZero.setColumns(10);
        }
        {
            chckbxApplyToAllZero = new JCheckBox("Apply to all");
            contentPanel.add(chckbxApplyToAllZero, "cell 2 4");
        }
        {
            lblWavelength = new JLabel("Wavelength (A)");
            contentPanel.add(lblWavelength, "cell 0 5,alignx trailing");
        }
        {
            txtwave = new JTextField();
            txtwave.setText("1.54");
            contentPanel.add(txtwave, "cell 1 5,growx");
            txtwave.setColumns(10);
        }
        {
            chckbxApplyToAllWave = new JCheckBox("Apply to all");
            contentPanel.add(chckbxApplyToAllWave, "cell 2 5");
        }
        {
            lblXUnits = new JLabel("X units");
            contentPanel.add(lblXUnits, "cell 0 6,alignx trailing");
        }
        {
            comboBoxXunits = new JComboBox<String>();
            contentPanel.add(comboBoxXunits, "cell 1 6,growx");
            for (Xunits xu :Xunits.values()){
                comboBoxXunits.addItem(xu.getName());
            }
        }
        {
            chckbxApplyToAllXunits = new JCheckBox("Apply to all");
            contentPanel.add(chckbxApplyToAllXunits, "cell 2 6");
        }
        {
            lblYoff = new JLabel("Y offset");
            contentPanel.add(lblYoff, "cell 0 7,alignx trailing");
        }
        {
            txtYoff = new JTextField();
            contentPanel.add(txtYoff, "cell 1 7,growx");
            txtYoff.setColumns(10);
        }
        {
            chckbxApplyToAllYoff = new JCheckBox("Apply to all");
            contentPanel.add(chckbxApplyToAllYoff, "cell 2 7");
        }
        {
            JLabel lblLineWidth = new JLabel("Line width");
            contentPanel.add(lblLineWidth, "cell 0 8,alignx trailing");
        }
        {
            txtLine = new JTextField();
            contentPanel.add(txtLine, "cell 1 8,growx");
            txtLine.setColumns(10);
        }
        {
            chckbxApplyToAllLine = new JCheckBox("Apply to all");
            contentPanel.add(chckbxApplyToAllLine, "cell 2 8");
        }
        {
            JLabel lblMarkerSize = new JLabel("Marker size");
            contentPanel.add(lblMarkerSize, "cell 0 9,alignx trailing");
        }
        {
            txtMarker = new JTextField();
            contentPanel.add(txtMarker, "cell 1 9,growx");
            txtMarker.setColumns(10);
        }
        {
            chckbxApplyToAllMarker = new JCheckBox("Apply to all");
            contentPanel.add(chckbxApplyToAllMarker, "cell 2 9");
        }
        {
            JLabel lblType = new JLabel("Type");
            contentPanel.add(lblType, "cell 0 10,alignx trailing");
        }
        comboType = new JComboBox<String>();
        contentPanel.add(comboType, "cell 1 10,growx");
        {
            for (SerieType s :SerieType.values()){
                comboType.addItem(s.name());
            }
        }
        {
            chckbxApplyToAllType = new JCheckBox("Apply to all");
            contentPanel.add(chckbxApplyToAllType, "cell 2 10");
        }
        {
            chckbxErrorBars = new JCheckBox("Error bars");
            contentPanel.add(chckbxErrorBars, "cell 1 11");
        }
        {
            chckbxApplyToAllErrBars = new JCheckBox("Apply to all");
            contentPanel.add(chckbxApplyToAllErrBars, "cell 2 11");
        }
        {
            chckbxShow = new JCheckBox("Show");
            contentPanel.add(chckbxShow, "cell 1 12");
        }
        {
            chckbxApplyToAllShow = new JCheckBox("Apply to all");
            contentPanel.add(chckbxApplyToAllShow, "cell 2 12");
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
        txtScale.setText(FileUtils.dfX_3.format(seriesToEdit.get(0).getScaleY()));
        txtZero.setText(FileUtils.dfX_4.format(seriesToEdit.get(0).getXOffset()));
        txtwave.setText(FileUtils.dfX_5.format(seriesToEdit.get(0).getWavelength()));
        comboBoxXunits.setSelectedItem(seriesToEdit.get(0).getxUnits().getName());
        txtYoff.setText(FileUtils.dfX_3.format(seriesToEdit.get(0).getYOffset()));
        txtLine.setText(FileUtils.dfX_1.format(seriesToEdit.get(0).getLineWidth()));
        txtMarker.setText(FileUtils.dfX_1.format(seriesToEdit.get(0).getMarkerSize()));
        comboType.setSelectedItem(seriesToEdit.get(0).getSerieType().name());
        chckbxShow.setSelected(seriesToEdit.get(0).isPlotThis());
        chckbxErrorBars.setSelected(seriesToEdit.get(0).isShowErrBars());
        
        if (seriesToEdit.size()==1) {
            chckbxApplyToAllName.setSelected(true);
            chckbxApplyToAllColor.setSelected(true);
            chckbxApplyToAllScale.setSelected(true);
            chckbxApplyToAllZero.setSelected(true);
            chckbxApplyToAllWave.setSelected(true);
            chckbxApplyToAllXunits.setSelected(true);
            chckbxApplyToAllYoff.setSelected(true);
            chckbxApplyToAllLine.setSelected(true);
            chckbxApplyToAllMarker.setSelected(true);
            chckbxApplyToAllType.setSelected(true);
            chckbxApplyToAllShow.setSelected(true);
            chckbxApplyToAllErrBars.setSelected(true);
        }
        this.pack();
        
        int xmain = D1Dplot_global.getD1DmainFrame().getX();
        int ymain = D1Dplot_global.getD1DmainFrame().getY();
        int wmain = D1Dplot_global.getD1DmainFrame().getWidth();
        int hmain = D1Dplot_global.getD1DmainFrame().getHeight();
        
        int x = (int) FastMath.max((wmain/2.-this.getWidth()/2.)+xmain, 1);
        int y = (int) FastMath.max((hmain/2.-this.getHeight()/2.)+ymain, 1);
        this.setBounds(x, y, this.getWidth(), this.getHeight());
        
        
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
        for (DataSerie p:seriesToEdit) {
            if (chckbxApplyToAllColor.isSelected()) {
                p.setColor(lblColor_1.getBackground());
            }
            if (chckbxApplyToAllName.isSelected()) {
                p.setName(txtName.getText());
            }
            
            if (chckbxApplyToAllScale.isSelected()) {
                try {
                    p.setScaleY(Double.parseDouble(txtScale.getText()));    
                }catch(Exception ex) {
                    log.warning("Error reading Y scale");
                }
            }
            
            if (chckbxApplyToAllZero.isSelected()) {
                try {
                    p.setXOffset(Double.parseDouble(txtZero.getText()));    
                }catch(Exception ex) {
                    log.warning("Error reading Zero shift");
                }
            }
            
            if (chckbxApplyToAllWave.isSelected()) {
                try {
                    p.setWavelength(Double.parseDouble(txtwave.getText()));    
                }catch(Exception ex) {
                    log.warning("Error reading Wavelength");
                }
            }

            if (chckbxApplyToAllXunits.isSelected()) {
                p.setxUnits(Xunits.getEnum((String)comboBoxXunits.getSelectedItem()));
            }
            
            if (chckbxApplyToAllYoff.isSelected()) {
                try {
                    p.setYOffset(Double.parseDouble(txtYoff.getText()));    
                }catch(Exception ex) {
                    log.warning("Error reading Y offset");
                }
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

            if (chckbxApplyToAllType.isSelected()) {
                p.setSerieType(SerieType.getEnum((String) comboType.getSelectedItem()));
            }
            
            if (chckbxApplyToAllShow.isSelected()) {
                p.setPlotThis(chckbxShow.isSelected());
            }            

            if (chckbxApplyToAllErrBars.isSelected()) {
                p.setShowErrBars(chckbxErrorBars.isSelected());
            }
            

        }

        this.dispose();
    }
    
    
    
    protected void do_cancelButton_actionPerformed(ActionEvent e) {
        this.dispose();
    }
}

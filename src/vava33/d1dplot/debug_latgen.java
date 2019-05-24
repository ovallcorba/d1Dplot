package com.vava33.d1dplot;


/**    
 * test class to debug lattice calculations
 *   
 * @author Oriol Vallcorba
 * Licence: GPLv3
 *   
 */

import javax.swing.JDialog;
import javax.swing.JPanel;

import com.vava33.jutils.FileUtils;
import com.vava33.jutils.VavaLogger;
import net.miginfocom.swing.MigLayout;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

import com.vava33.cellsymm.Cell;
import com.vava33.cellsymm.CellSymm_global;
import com.vava33.cellsymm.HKLrefl;
import com.vava33.cellsymm.SpaceGroup;
import com.vava33.cellsymm.CellSymm_global.CrystalCentering;
import com.vava33.cellsymm.CellSymm_global.CrystalFamily;
import com.vava33.d1dplot.auxi.DoubleJSlider;
import com.vava33.d1dplot.data.DataPoint_hkl;
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.d1dplot.data.SerieType;
import com.vava33.d1dplot.data.Xunits;
import com.vava33.d1dplot.index.IndexSolution;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.List;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;

public class debug_latgen {

    private JDialog debug_latgen_diag;
    private PlotPanel plotpanel;
    DataSerie dsLatGen;
    Cell cel;

    private static final String className = "debug_latgen";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);
    
    private IndexDialog indexDialog;
    private DoubleJSlider sliderC;
    private DoubleJSlider sliderB;
    private DoubleJSlider sliderA;
    private DoubleJSlider sliderAl;
    private DoubleJSlider sliderBe;
    private DoubleJSlider sliderGa;
    private JTextField txtA;
    private JTextField txtB;
    private JTextField txtC;
    private JTextField txtAl;
    private JTextField txtBe;
    private JTextField txtGa;
    private JTextField txtSgnum;
    private JLabel lblQmax;
    private JTextField txtQmax;
    private JComboBox<CrystalFamily> comboBoxCrystFam;
    private JComboBox<CrystalCentering> comboBox;
    private JCheckBox chckbxCalcExt;
    private JButton btnAdd;
    private JButton btnClose;
    
    public debug_latgen(IndexDialog id) {
        this.debug_latgen_diag = new JDialog(D1Dplot_global.getD1DmainFrame(),"Manual Indexing",false);
        this.indexDialog = id;
        this.plotpanel = id.getPlotPanel();
        this.debug_latgen_diag.setIconImage(D1Dplot_global.getIcon());
        debug_latgen_diag.getContentPane().setLayout(new MigLayout("", "[grow][]", "[][][][][][][][grow][][]"));
        
        sliderA = new DoubleJSlider(0,30,5,10000);
        sliderA.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                do_sliderA_stateChanged(e);
            }
        });
        debug_latgen_diag.getContentPane().add(sliderA, "cell 0 1,growx");
        
        sliderB = new DoubleJSlider(0,30,5,10000);
        sliderB.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                do_sliderB_stateChanged(e);
            }
        });
        debug_latgen_diag.getContentPane().add(sliderB, "cell 0 2,growx");
        
        sliderC = new DoubleJSlider(0,30,5,10000);
        sliderC.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                do_sliderC_stateChanged(e);
            }
        });
        debug_latgen_diag.getContentPane().add(sliderC, "cell 0 3,growx");
        
        sliderAl = new DoubleJSlider(60,120,90,10000);
        sliderAl.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                do_sliderAl_stateChanged(e);
            }
        });
        debug_latgen_diag.getContentPane().add(sliderAl, "cell 0 4,growx");
        
        sliderBe = new DoubleJSlider(60,120,90,10000);
        sliderBe.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                do_sliderBe_stateChanged(e);
            }
        });
        debug_latgen_diag.getContentPane().add(sliderBe, "cell 0 5,growx");
        
        sliderGa = new DoubleJSlider(60,120,90,10000);
        sliderGa.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                do_sliderGa_stateChanged(e);
            }
        });
        debug_latgen_diag.getContentPane().add(sliderGa, "cell 0 6,growx");
        
        txtA = new JTextField();
        txtA.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                do_txtA_keyReleased(e);
            }
        });
        
        lblQmax = new JLabel("Qmax");
        debug_latgen_diag.getContentPane().add(lblQmax, "cell 0 0,alignx trailing");
        
        txtQmax = new JTextField();
        txtQmax.setText("2");
        debug_latgen_diag.getContentPane().add(txtQmax, "cell 1 0,growx");
        txtQmax.setColumns(10);
        txtA.setText("a");
        debug_latgen_diag.getContentPane().add(txtA, "cell 1 1,growx");
        txtA.setColumns(10);
        
        txtB = new JTextField();
        txtB.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                do_txtB_keyReleased(e);
            }
        });
        txtB.setText("b");
        debug_latgen_diag.getContentPane().add(txtB, "cell 1 2,growx");
        txtB.setColumns(10);
        
        txtC = new JTextField();
        txtC.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                do_txtC_keyReleased(e);
            }
        });
        txtC.setText("c");
        debug_latgen_diag.getContentPane().add(txtC, "cell 1 3,growx");
        txtC.setColumns(10);
        
        txtAl = new JTextField();
        txtAl.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                do_txtAl_keyReleased(e);
            }
        });
        txtAl.setText("al");
        debug_latgen_diag.getContentPane().add(txtAl, "cell 1 4,growx");
        txtAl.setColumns(10);
        
        txtBe = new JTextField();
        txtBe.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                do_txtBe_keyReleased(e);
            }
        });
        txtBe.setText("be");
        debug_latgen_diag.getContentPane().add(txtBe, "cell 1 5,growx");
        txtBe.setColumns(10);
        
        txtGa = new JTextField();
        txtGa.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                do_txtGa_keyReleased(e);
            }
        });
        txtGa.setText("ga");
        debug_latgen_diag.getContentPane().add(txtGa, "cell 1 6,growx");
        txtGa.setColumns(10);
        
        JPanel panel = new JPanel();
        debug_latgen_diag.getContentPane().add(panel, "cell 0 7 2 1,grow");
        panel.setLayout(new MigLayout("", "[][grow][]", "[][][]"));
        
        JLabel lblCrystf = new JLabel("CrystF");
        panel.add(lblCrystf, "cell 0 0,alignx trailing");
        
        comboBoxCrystFam = new JComboBox<CrystalFamily>();
//        comboBoxCrystFam.addItemListener(new ItemListener() {
//            public void itemStateChanged(ItemEvent e) {
//                do_comboBoxCrystFam_itemStateChanged(e);
//            }
//        });
        comboBoxCrystFam.setModel(new DefaultComboBoxModel<CrystalFamily>(CrystalFamily.values()));
        panel.add(comboBoxCrystFam, "cell 1 0,growx");
        
        JLabel lblCentering = new JLabel("Centering");
        panel.add(lblCentering, "cell 0 1,alignx trailing");
        
        comboBox = new JComboBox<CrystalCentering>();
//        comboBox.addItemListener(new ItemListener() {
//            public void itemStateChanged(ItemEvent e) {
//                do_comboBox_itemStateChanged(e);
//            }
//        });
        comboBox.setModel(new DefaultComboBoxModel<CrystalCentering>(CrystalCentering.values()));
        panel.add(comboBox, "cell 1 1,growx");
        
        chckbxCalcExt = new JCheckBox("calc EXT");
        chckbxCalcExt.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                do_chckbxCalcExt_itemStateChanged(e);
            }
        });
        panel.add(chckbxCalcExt, "cell 2 1");
        
        JLabel lblSgNum = new JLabel("SG num");
        panel.add(lblSgNum, "cell 0 2,alignx trailing");
        
        txtSgnum = new JTextField();
        txtSgnum.setText("sgnum");
        panel.add(txtSgnum, "cell 1 2,growx");
        txtSgnum.setColumns(10);
        
        JButton btnApply = new JButton("apply");
        btnApply.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_btnApply_actionPerformed(e);
            }
        });
        panel.add(btnApply, "cell 2 2");
        
        btnAdd = new JButton("Add to list");
        btnAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_btnAdd_actionPerformed(e);
            }
        });
        debug_latgen_diag.getContentPane().add(btnAdd, "flowx,cell 0 8 2 1,alignx center");
        
        btnClose = new JButton("Close");
        btnClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_btnClose_actionPerformed(e);
            }
        });
        debug_latgen_diag.getContentPane().add(btnClose, "cell 1 9,alignx right");
        
        this.debug_latgen_diag.pack();
        init();
        
    }

    private void init() {
//        sliderA = new DoubleJSlider(0,30,5,10000);
//        sliderB = new DoubleJSlider(0,30,5,10000);
//        sliderC = new DoubleJSlider(0,30,5,10000);
//        sliderAl = new DoubleJSlider(60,120,90,10000);
//        sliderBe = new DoubleJSlider(60,120,90,10000);
//        sliderGa = new DoubleJSlider(60,120,90,10000);
        
        txtA.setText(FileUtils.dfX_4.format(sliderA.getScaledValue()));
        txtB.setText(FileUtils.dfX_4.format(sliderB.getScaledValue()));
        txtC.setText(FileUtils.dfX_4.format(sliderC.getScaledValue()));
        txtAl.setText(FileUtils.dfX_3.format(sliderAl.getScaledValue()));
        txtBe.setText(FileUtils.dfX_3.format(sliderBe.getScaledValue()));
        txtGa.setText(FileUtils.dfX_3.format(sliderGa.getScaledValue()));
        
        //creem una dataserie per mostrar les reflexions
        dsLatGen = new DataSerie(plotpanel.getSelectedSeries().get(0), SerieType.hkl,false);
        dsLatGen.serieName="LATGen hkl";
        applyCrystalFamilyRestrictions();
        
        updateDS();
        
    }
    
    private void applyCrystalFamilyRestrictions() {
        sliderA.setEnabled(true);
        sliderB.setEnabled(true);
        sliderC.setEnabled(true);
        sliderAl.setEnabled(true);
        sliderBe.setEnabled(true);
        sliderGa.setEnabled(true);
        
        switch ((CrystalFamily) comboBoxCrystFam.getSelectedItem()) {
        case CUBIC:
            sliderB.setEnabled(false);
            sliderC.setEnabled(false);
            sliderAl.setScaledValue(90.0);
            sliderBe.setScaledValue(90.0);
            sliderGa.setScaledValue(90.0);
            sliderAl.setEnabled(false);
            sliderBe.setEnabled(false);
            sliderGa.setEnabled(false);
            break;
        case TETRA:
            sliderB.setEnabled(false);
            sliderAl.setScaledValue(90.0);
            sliderBe.setScaledValue(90.0);
            sliderGa.setScaledValue(90.0);
            sliderAl.setEnabled(false);
            sliderBe.setEnabled(false);
            sliderGa.setEnabled(false);
            break;
        case HEXA: 
            sliderB.setEnabled(false);
            sliderAl.setScaledValue(90.0);
            sliderBe.setScaledValue(90.0);
            sliderGa.setScaledValue(120.0);
            sliderAl.setEnabled(false);
            sliderBe.setEnabled(false);
            sliderGa.setEnabled(false);
            break;
        case ORTO: 
            sliderAl.setScaledValue(90.0);
            sliderBe.setScaledValue(90.0);
            sliderGa.setScaledValue(90.0);
            sliderAl.setEnabled(false);
            sliderBe.setEnabled(false);
            sliderGa.setEnabled(false);
            break;
        case MONO: 
            sliderAl.setScaledValue(90.0);
            sliderGa.setScaledValue(90.0);
            sliderAl.setEnabled(false);
            sliderGa.setEnabled(false);
            break;
        default:
            break;
        }
        
        //and set space group
        
    }
    

    protected void do_chckbxCalcExt_itemStateChanged(ItemEvent e) {
        this.updateDS();
    }
    
    protected void do_btnApply_actionPerformed(ActionEvent e) {
        this.updateDS();
    }
    
    protected void do_comboBox_itemStateChanged(ItemEvent e) {
        this.updateDS();
    }
    
    protected void do_comboBoxCrystFam_itemStateChanged(ItemEvent e) {
        applyCrystalFamilyRestrictions();
        this.updateDS();
    }
    
    private void updateDS() {
        log.debug("*************************************updating latgen DS");
        //primer llegim els parametres dels texts, etc...
        
        double a = sliderA.getScaledValue();
        double b = sliderB.getScaledValue();
        double c = sliderC.getScaledValue();
        double al = sliderAl.getScaledValue();
        double be = sliderBe.getScaledValue();
        double ga = sliderGa.getScaledValue();
        double qmax = Double.parseDouble(txtQmax.getText());
        SpaceGroup sg=null;
        try {
            int sgnum = Integer.parseInt(txtSgnum.getText());
            sg = CellSymm_global.getSpaceGroupByNum(sgnum);
        }catch(Exception ex) {
            log.info("using max sg");
        }
        
        if (sg!=null) {
            cel = new Cell(a,b,c,al,be,ga,true,sg);
            //actualitzem combobox i combocentering amb les dades del grup espacial
            this.comboBoxCrystFam.setSelectedItem(sg.getCrystalFamily());
            this.comboBox.setSelectedItem(sg.getCrystalCentering());
        }else { //max symm
            cel = new Cell(a,b,c,al,be,ga,true,(CrystalFamily) comboBoxCrystFam.getSelectedItem(),(CrystalCentering) comboBox.getSelectedItem());
            this.txtSgnum.setText(Integer.toString(cel.getSg().getsgNum()));
        }
        applyCrystalFamilyRestrictions(); //ho he posat aqui al desactivar els listeners als combo
        
        List<HKLrefl> refs = cel.generateHKLsAsymetricUnitCrystalFamily(qmax,chckbxCalcExt.isSelected(),chckbxCalcExt.isSelected(),false,false,true);
        
        dsLatGen.clearPoints();
        
        dsLatGen.setxUnits(Xunits.dsp);
        Iterator<HKLrefl> itrh = refs.iterator();
        while (itrh.hasNext()) {
            HKLrefl hkl = itrh.next();
            dsLatGen.addPoint(new DataPoint_hkl(hkl));    //(float) hkl.calct2((float) dsLatGen.getWavelength(),true)
//            log.config(hkl.toString_HKL_tth_mult_Fc2((float) dsLatGen.getWavelength()));
        }
        
        //ara ho posem a les unitats de la primera serie
        dsLatGen.convertDStoXunits(this.plotpanel.getFirstPlottedSerie().getxUnits());
        //en cas que haguem posat una wave a dsLatGen
        if ((dsLatGen.getWavelength()>0)&&(this.plotpanel.getFirstPlottedSerie().getWavelength()<=0)){
            this.plotpanel.getFirstPlottedSerie().setWavelength(dsLatGen.getWavelength());
        }
        
        log.debug("tipus serie latgen="+dsLatGen.getTipusSerie()+" wave="+dsLatGen.getWavelength()+" scale="+dsLatGen.getScale());
        
        this.plotpanel.setIndexSolution(dsLatGen);
//        this.main.updateData(false,true); //TODO:revisar el TRUE
    }

    protected void do_sliderA_stateChanged(ChangeEvent e) {

        txtA.setText(FileUtils.dfX_4.format(sliderA.getScaledValue()));
        updateDS();
//        DoubleJSlider ds = (DoubleJSlider)e.getSource();
//        if(ds.getValueIsAdjusting()) return;
        switch ((CrystalFamily) comboBoxCrystFam.getSelectedItem()) {
        case CUBIC:
            sliderB.setScaledValue(sliderA.getScaledValue());
            sliderC.setScaledValue(sliderA.getScaledValue());
            break;
        case TETRA:
            sliderB.setScaledValue(sliderA.getScaledValue());
            break;
        case HEXA:
            sliderB.setScaledValue(sliderA.getScaledValue());
            break;
        default:
            break;
        }
        
        
    }
    protected void do_sliderB_stateChanged(ChangeEvent e) {
        txtB.setText(FileUtils.dfX_4.format(sliderB.getScaledValue()));
        updateDS();
    }
    protected void do_sliderC_stateChanged(ChangeEvent e) {
        txtC.setText(FileUtils.dfX_4.format(sliderC.getScaledValue()));
        updateDS();
    }
    protected void do_sliderAl_stateChanged(ChangeEvent e) {
        txtAl.setText(FileUtils.dfX_3.format(sliderAl.getScaledValue()));
        updateDS();
    }
    protected void do_sliderBe_stateChanged(ChangeEvent e) {
        txtBe.setText(FileUtils.dfX_3.format(sliderBe.getScaledValue()));
        updateDS();
    }
    protected void do_sliderGa_stateChanged(ChangeEvent e) {
        txtGa.setText(FileUtils.dfX_3.format(sliderGa.getScaledValue()));
        updateDS();
    }
    protected void do_txtA_keyReleased(KeyEvent e) {
        if (e.getKeyCode()==KeyEvent.VK_ENTER) {
            sliderA.setScaledValue(Double.parseDouble(txtA.getText()));
        }

        //        updateDS();
    }
    protected void do_txtB_keyReleased(KeyEvent e) {
        if (e.getKeyCode()==KeyEvent.VK_ENTER) {
            sliderB.setScaledValue(Double.parseDouble(txtB.getText()));
        }
//        updateDS();
    }
    protected void do_txtC_keyReleased(KeyEvent e) {
        if (e.getKeyCode()==KeyEvent.VK_ENTER) {
            sliderC.setScaledValue(Double.parseDouble(txtC.getText()));            
        }

//        updateDS();
    }
    protected void do_txtAl_keyReleased(KeyEvent e) {
        if (e.getKeyCode()==KeyEvent.VK_ENTER) {
            sliderAl.setScaledValue(Double.parseDouble(txtAl.getText()));    
        }
        
//        updateDS();
    }
    protected void do_txtBe_keyReleased(KeyEvent e) {
        if (e.getKeyCode()==KeyEvent.VK_ENTER) {
            sliderBe.setScaledValue(Double.parseDouble(txtBe.getText()));    
        }
        
//        updateDS();
    }
    protected void do_txtGa_keyReleased(KeyEvent e) {
        if (e.getKeyCode()==KeyEvent.VK_ENTER) {
            sliderGa.setScaledValue(Double.parseDouble(txtGa.getText()));    
        }
        
        
//        updateDS();
    }
    
    public void visible(boolean vis) {
        this.debug_latgen_diag.setVisible(vis);
    }
    
    public boolean isVisible() {
        return this.debug_latgen_diag.isVisible();
    }


    private void do_btnAdd_actionPerformed(ActionEvent e) {
        //TODO crear indexsolution i afegir a la llista de IndexDialog
        
        
        /*ex:
         *          IndexSolution is2 = this.indexSolToGuessSG.getDuplicate();
         *          is2.getRefinedCell().setSg(sg);
         *          is2.getRefinedCell().setCrystalCentering(CrystalCentering.P);
         *          is2.calcM20(); //TODO hauria de duplicar la indexSolution sino no funcionara
         *          indexDialog.sols.add(is2);
         */
    }
    
    private void do_btnClose_actionPerformed(ActionEvent e) {
        //comprovem que sigui l'últim supervivent de indexing:
        boolean showsolution=false;
        if (indexDialog!=null) {
            if (indexDialog.isVisible())showsolution=true;
        }
        plotpanel.setShowIndexSolution(showsolution);
        this.debug_latgen_diag.dispose();
    }
}
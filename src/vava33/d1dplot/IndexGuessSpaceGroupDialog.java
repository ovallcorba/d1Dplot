package com.vava33.d1dplot;

/*
 * Guess space group dialog
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import javax.swing.JDialog;

import com.vava33.cellsymm.CellSymm_global.CrystalCentering;
import com.vava33.cellsymm.CellSymm_global.CrystalFamily;
import com.vava33.cellsymm.SpaceGroup;
import com.vava33.d1dplot.index.IndexSolution;
//import com.vava33.jutils.VavaLogger;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class IndexGuessSpaceGroupDialog {

//    private static final String className = "GuessSpaceGroup_dialog";
//    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);
    
    private JDialog GuessSpaceGroupDialog;
    private IndexDialog indexDialog;
    private IndexSolution indexSolToGuessSG;
    private JTextField txtPars;
    private JCheckBox chckbxCubic;
    private JCheckBox chckbxTetra;
    private JCheckBox chckbxHexa;
    private JCheckBox chckbxOrto;
    private JCheckBox chckbxMono;
    private JCheckBox chckbxTric;
    private JCheckBox chckbxP;
    private JCheckBox chckbxA;
    private JCheckBox chckbxB;
    private JCheckBox chckbxC;
    private JCheckBox chckbxI;
    private JCheckBox chckbxF;
    private JCheckBox chckbxR;
    private JButton btnNewButton;
    
    public IndexGuessSpaceGroupDialog(IndexDialog d, IndexSolution is) {
//        this.plotpanel=p;
        this.indexDialog=d;
        this.indexSolToGuessSG=is;
        this.GuessSpaceGroupDialog = new JDialog(D1Dplot_global.getD1DmainFrame(),"Guess SpaceGroup",false);
        GuessSpaceGroupDialog.getContentPane().setLayout(new MigLayout("", "[][][][][][][][]", "[][][][]"));
        
        JLabel lblCellParameters = new JLabel("Cell Parameters=");
        GuessSpaceGroupDialog.getContentPane().add(lblCellParameters, "cell 0 0,alignx trailing");
        
        txtPars = new JTextField();
        txtPars.setEditable(false);
        txtPars.setText("pars");
        GuessSpaceGroupDialog.getContentPane().add(txtPars, "cell 1 0 7 1,growx");
        txtPars.setColumns(10);
        
        JLabel lblCrystalFamily = new JLabel("Crystal family");
        GuessSpaceGroupDialog.getContentPane().add(lblCrystalFamily, "cell 0 1");
        
        chckbxCubic = new JCheckBox("cubic");
        GuessSpaceGroupDialog.getContentPane().add(chckbxCubic, "cell 1 1");
        
        chckbxTetra = new JCheckBox("tetra");
        GuessSpaceGroupDialog.getContentPane().add(chckbxTetra, "cell 2 1");
        
        chckbxHexa = new JCheckBox("hexa");
        GuessSpaceGroupDialog.getContentPane().add(chckbxHexa, "cell 3 1");
        
        chckbxOrto = new JCheckBox("orto");
        GuessSpaceGroupDialog.getContentPane().add(chckbxOrto, "cell 4 1");
        
        chckbxMono = new JCheckBox("mono");
        GuessSpaceGroupDialog.getContentPane().add(chckbxMono, "cell 5 1");
        
        chckbxTric = new JCheckBox("tric");
        GuessSpaceGroupDialog.getContentPane().add(chckbxTric, "cell 6 1");
        
        JLabel lblCentering = new JLabel("Force centering");
        GuessSpaceGroupDialog.getContentPane().add(lblCentering, "cell 0 2");
        
        chckbxP = new JCheckBox("P");
        GuessSpaceGroupDialog.getContentPane().add(chckbxP, "cell 1 2");
        
        chckbxA = new JCheckBox("A");
        GuessSpaceGroupDialog.getContentPane().add(chckbxA, "cell 2 2");
        
        chckbxB = new JCheckBox("B");
        GuessSpaceGroupDialog.getContentPane().add(chckbxB, "cell 3 2");
        
        chckbxC = new JCheckBox("C");
        GuessSpaceGroupDialog.getContentPane().add(chckbxC, "cell 4 2");
        
        chckbxI = new JCheckBox("I");
        GuessSpaceGroupDialog.getContentPane().add(chckbxI, "cell 5 2");
        
        chckbxF = new JCheckBox("F");
        GuessSpaceGroupDialog.getContentPane().add(chckbxF, "cell 6 2");
        
        chckbxR = new JCheckBox("R");
        GuessSpaceGroupDialog.getContentPane().add(chckbxR, "cell 7 2");
        
        JButton btnCheck = new JButton("Calculate");
        btnCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_btnCheck_actionPerformed(e);
            }
        });
        GuessSpaceGroupDialog.getContentPane().add(btnCheck, "cell 0 3");
        
        btnNewButton = new JButton("Close");
        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                do_btnNewButton_actionPerformed(e);
            }
        });
        GuessSpaceGroupDialog.getContentPane().add(btnNewButton, "cell 7 3");
        
        init();
    }

    private void init() {
        //aqui ha de fer el que faci falta al seleccinar una IndexSolution
        txtPars.setText(this.indexSolToGuessSG.getRefinedCell().toStringCellParamOnly());
        switch(this.indexSolToGuessSG.getRefinedCell().getCrystalFamily()) {
        case CUBIC:
            this.chckbxCubic.setSelected(true);
            break;
        case HEXA:
            this.chckbxHexa.setSelected(true);
            break;
        case MONO:
            this.chckbxMono.setSelected(true);
            break;
        case ORTO:
            this.chckbxOrto.setSelected(true);
            break;
        case TETRA:
            this.chckbxTetra.setSelected(true);
            break;
        case TRIC:
            this.chckbxTric.setSelected(true);
            break;
        default:
            break;
        }
//        this.chckbxP.setSelected(true);
        this.GuessSpaceGroupDialog.pack();
    }
    
    public void visible(boolean b) {
        this.GuessSpaceGroupDialog.setVisible(b);
    }

    public void updateIndexSolution(IndexSolution is) {
        this.indexSolToGuessSG=is;
        init();
    }

    
    private void testForcedCentering(IndexSolution is, SpaceGroup sg, CrystalCentering cc) {
        IndexSolution is2 = this.indexSolToGuessSG.getDuplicate();
        is2.getRefinedCell().setSg(sg);
        is2.getRefinedCell().setCrystalCentering(cc);
        if (is.getNumSpurious()>is.getIndexMethod().nImp)return;
        is2.calcM20();
        indexDialog.sols.add(is2);
    }
    
    private void testCrystalFamily(CrystalFamily cf) {
        for (SpaceGroup sg:cf.getSpaceGroups()) {
            IndexSolution is = this.indexSolToGuessSG.getDuplicate();
            is.getRefinedCell().setSg(sg);
            if (is.getNumSpurious()>is.getIndexMethod().nImp)continue;
            is.calcM20(); //TODO hauria de duplicar la indexSolution sino no funcionara
            indexDialog.sols.add(is);
            //TODO implementar centering --- o posar-ho com a force centering
            if(this.chckbxA.isSelected()) testForcedCentering(is,sg,CrystalCentering.A);
            if(this.chckbxB.isSelected()) testForcedCentering(is,sg,CrystalCentering.B);
            if(this.chckbxC.isSelected()) testForcedCentering(is,sg,CrystalCentering.C);
            if(this.chckbxI.isSelected()) testForcedCentering(is,sg,CrystalCentering.I);
            if(this.chckbxF.isSelected()) testForcedCentering(is,sg,CrystalCentering.F);
            if(this.chckbxR.isSelected()) testForcedCentering(is,sg,CrystalCentering.R);
            if(this.chckbxP.isSelected()) testForcedCentering(is,sg,CrystalCentering.P);
        }
    }
    
    protected void do_btnCheck_actionPerformed(ActionEvent e) {
        //TODO preguntar que això borrarà els resultats de la indexació (o podem afegir-los a la taula).?? de moment afegeixo
        if(this.chckbxCubic.isSelected()) this.testCrystalFamily(CrystalFamily.CUBIC);
        if(this.chckbxTetra.isSelected()) this.testCrystalFamily(CrystalFamily.TETRA);
        if(this.chckbxHexa.isSelected()) this.testCrystalFamily(CrystalFamily.HEXA);
        if(this.chckbxOrto.isSelected()) this.testCrystalFamily(CrystalFamily.ORTO);
        if(this.chckbxMono.isSelected()) this.testCrystalFamily(CrystalFamily.MONO);
        if(this.chckbxTric.isSelected()) this.testCrystalFamily(CrystalFamily.TRIC);
        indexDialog.fillTable(indexDialog.sols);
    }
    
    protected void do_btnNewButton_actionPerformed(ActionEvent e) {
        this.GuessSpaceGroupDialog.dispose();
    }
}




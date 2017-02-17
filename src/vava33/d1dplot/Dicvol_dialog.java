package vava33.d1dplot;

/**
 * D1Dplot
 * 
 * Dialog to save peaks in dicvol indexing format
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

import com.vava33.jutils.FileUtils;
import com.vava33.jutils.VavaLogger;

import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.JCheckBox;
import javax.swing.border.LineBorder;

import vava33.d1dplot.auxi.DataSerie;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Dicvol_dialog extends JDialog {

    private final JPanel contentPanel = new JPanel();
    private static VavaLogger log = D1Dplot_global.getVavaLogger(Dicvol_dialog.class.getName());
    private JTextField txtNpeaks;
    private JTextField txtAmax;
    private JTextField txtBmax;
    private JTextField txtCmax;
    private JTextField txtBetamin;
    private JTextField txtBetamax;
    private JTextField txtVmin;
    private JTextField txtVmax;
    private JTextField txtWave;
    private JTextField txtMw;
    private JTextField txtDensity;
    private JTextField txtDensityerr;
    private JTextField txtEps;
    private JTextField txtFom;
    private JTextField txtSpurious;

    private float amax=25.0f;
    private float bmax=25.0f;
    private float cmax=25.0f;
    private float betamin=90.0f;
    private float betamax=90.0f;
    private float vmin=0.0f;
    private float vmax=3000.0f;
    
    private float wavel = -1.0f;
    private float mw = 0.0f;
    private float density = 0.0f;
    private float densityerr = 0.0f;
    
    private float eps = 0.02f;
    private float minfom = 10.0f;
    private int spurious = 0;
    
    private boolean cubic = true;
    private boolean tetra = true;
    private boolean hexa = true;
    private boolean orto = true;
    private boolean mono = true;
    private boolean tric = false;
    
    private boolean zeroRef = true;
    private boolean prevzero = false;
    private boolean dic06 = false;
    
    private int npeaks = 20;
    
    private DataSerie ds;
    
    private boolean everythingOK = true;
    private JCheckBox chckbxCubic;
    private JCheckBox chckbxTetragonal;
    private JCheckBox chckbxHexagonal;
    private JCheckBox chckbxOrthorhombic;
    private JCheckBox chckbxMonoclinic;
    private JCheckBox chckbxTriclinic;
    private JCheckBox chckbxZeroRefienment;
    private JCheckBox chckbxPrevZeroSearch;
    private JCheckBox chckbxDicvolOpt;
    
    /**
     * Create the dialog.
     */
    public Dicvol_dialog(DataSerie ds) {
        this.ds=ds;
        this.setModal(true);
        setTitle("Save DICVOL file");
        setBounds(100, 100, 700, 419);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new MigLayout("", "[grow][grow][]", "[][][][grow]"));
        {
            JLabel lblNrPeaksTo = new JLabel("Nr. Peaks to use=");
            contentPanel.add(lblNrPeaksTo, "cell 0 0,alignx trailing");
        }
        {
            txtNpeaks = new JTextField();
            txtNpeaks.setText("npeaks");
            contentPanel.add(txtNpeaks, "cell 1 0,growx");
            txtNpeaks.setColumns(10);
        }
        {
            JLabel lblXunits = new JLabel("(xunits)");
            contentPanel.add(lblXunits, "cell 2 0");
        }
        {
            JPanel panel = new JPanel();
            panel.setBorder(new TitledBorder(null, "Crystal Systems", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            contentPanel.add(panel, "cell 0 1 3 1,grow");
            panel.setLayout(new MigLayout("", "[][][][][][]", "[]"));
            {
                chckbxCubic = new JCheckBox("Cubic");
                chckbxCubic.setSelected(true);
                panel.add(chckbxCubic, "cell 0 0");
            }
            {
                chckbxTetragonal = new JCheckBox("Tetragonal");
                chckbxTetragonal.setSelected(true);
                panel.add(chckbxTetragonal, "cell 1 0");
            }
            {
                chckbxHexagonal = new JCheckBox("Hexagonal");
                chckbxHexagonal.setSelected(true);
                panel.add(chckbxHexagonal, "cell 2 0");
            }
            {
                chckbxOrthorhombic = new JCheckBox("Orthorhombic");
                chckbxOrthorhombic.setSelected(true);
                panel.add(chckbxOrthorhombic, "cell 3 0");
            }
            {
                chckbxMonoclinic = new JCheckBox("Monoclinic");
                chckbxMonoclinic.setSelected(true);
                panel.add(chckbxMonoclinic, "cell 4 0");
            }
            {
                chckbxTriclinic = new JCheckBox("Triclinic");
                panel.add(chckbxTriclinic, "cell 5 0");
            }
        }
        {
            JPanel panel = new JPanel();
            panel.setBorder(new TitledBorder(null, "Cell dimensions", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            contentPanel.add(panel, "cell 0 2 3 1,grow");
            panel.setLayout(new MigLayout("", "[][grow][][grow][][grow]", "[][][]"));
            {
                JLabel lblAMax = new JLabel("<html>\n<i>a</i> max ("+D1Dplot_global.angstrom+")=\n</html>");
                panel.add(lblAMax, "cell 0 0,alignx trailing");
            }
            {
                txtAmax = new JTextField();
                txtAmax.setText("25.0");
                panel.add(txtAmax, "cell 1 0,growx");
                txtAmax.setColumns(10);
            }
            {
                JLabel lblBetaMin = new JLabel("<html>\n<i>"+D1Dplot_global.beta+"</i> min (º)=\n</html>");
                panel.add(lblBetaMin, "cell 2 0,alignx trailing");
            }
            {
                txtBetamin = new JTextField();
                txtBetamin.setText("90.0");
                panel.add(txtBetamin, "cell 3 0,growx");
                txtBetamin.setColumns(10);
            }
            {
                JLabel lblVMin = new JLabel("V min ("+D1Dplot_global.angstrom+"3)=");
                panel.add(lblVMin, "cell 4 0,alignx trailing");
            }
            {
                txtVmin = new JTextField();
                txtVmin.setText("0");
                panel.add(txtVmin, "cell 5 0,growx");
                txtVmin.setColumns(10);
            }
            {
                JLabel lblBMax = new JLabel("<html>\n<i>b</i> max ("+D1Dplot_global.angstrom+")=\n</html>");
                panel.add(lblBMax, "cell 0 1,alignx trailing");
            }
            {
                txtBmax = new JTextField();
                txtBmax.setText("25.0");
                panel.add(txtBmax, "cell 1 1,growx");
                txtBmax.setColumns(10);
            }
            {
                JLabel lblBetaMax = new JLabel("<html>\n<i>"+D1Dplot_global.beta+"</i> max (º)=\n</html>");
                panel.add(lblBetaMax, "cell 2 1,alignx trailing");
            }
            {
                txtBetamax = new JTextField();
                txtBetamax.setText("125.0");
                panel.add(txtBetamax, "cell 3 1,growx");
                txtBetamax.setColumns(10);
            }
            {
                JLabel lblVMax = new JLabel("V max ("+D1Dplot_global.angstrom+"3)=");
                panel.add(lblVMax, "cell 4 1,alignx trailing");
            }
            {
                txtVmax = new JTextField();
                txtVmax.setText("3000");
                panel.add(txtVmax, "cell 5 1,growx");
                txtVmax.setColumns(10);
            }
            {
                JLabel lblCMax = new JLabel("<html>\n<i>c</i> max ("+D1Dplot_global.angstrom+")=\n</html>");
                panel.add(lblCMax, "cell 0 2,alignx trailing");
            }
            {
                txtCmax = new JTextField();
                txtCmax.setText("25.0");
                panel.add(txtCmax, "cell 1 2,growx");
                txtCmax.setColumns(10);
            }
        }
        {
            JPanel panel = new JPanel();
            panel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "General", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
            contentPanel.add(panel, "cell 0 3 3 1,grow");
            panel.setLayout(new MigLayout("", "[][grow][][grow][]", "[][][][]"));
            {
                JLabel lblWavelengha = new JLabel("Wavelengh ("+D1Dplot_global.angstrom+")=");
                panel.add(lblWavelengha, "cell 0 0,alignx trailing");
            }
            {
                txtWave = new JTextField();
                txtWave.setText("wave");
                panel.add(txtWave, "cell 1 0,growx");
                txtWave.setColumns(10);
            }
            {
                JLabel lblEps = new JLabel("EPS=");
                panel.add(lblEps, "cell 2 0,alignx trailing");
            }
            {
                txtEps = new JTextField();
                txtEps.setText("0.02");
                panel.add(txtEps, "cell 3 0,growx");
                txtEps.setColumns(10);
            }
            {
                chckbxZeroRefienment = new JCheckBox("Zero Refienment");
                chckbxZeroRefienment.setSelected(true);
                panel.add(chckbxZeroRefienment, "cell 4 0");
            }
            {
                JLabel lblMwgmol = new JLabel("MW (g)=");
                panel.add(lblMwgmol, "cell 0 1,alignx trailing");
            }
            {
                txtMw = new JTextField();
                txtMw.setText("0");
                panel.add(txtMw, "cell 1 1,growx");
                txtMw.setColumns(10);
            }
            {
                JLabel lblMinFom = new JLabel("Min FOM=");
                panel.add(lblMinFom, "cell 2 1,alignx trailing");
            }
            {
                txtFom = new JTextField();
                txtFom.setText("10.0");
                panel.add(txtFom, "cell 3 1,growx");
                txtFom.setColumns(10);
            }
            {
                chckbxPrevZeroSearch = new JCheckBox("Prev Zero Search");
                panel.add(chckbxPrevZeroSearch, "cell 4 1");
            }
            {
                JLabel lblDensitygcm = new JLabel("Density (g/cm3)=");
                panel.add(lblDensitygcm, "cell 0 2,alignx trailing");
            }
            {
                txtDensity = new JTextField();
                txtDensity.setText("0");
                panel.add(txtDensity, "cell 1 2,growx");
                txtDensity.setColumns(10);
            }
            {
                JLabel lblImpurities = new JLabel("Spurious=");
                panel.add(lblImpurities, "cell 2 2,alignx trailing");
            }
            {
                txtSpurious = new JTextField();
                txtSpurious.setText("0");
                panel.add(txtSpurious, "cell 3 2,growx");
                txtSpurious.setColumns(10);
            }
            {
                chckbxDicvolOpt = new JCheckBox("Dicvol06 opt");
                panel.add(chckbxDicvolOpt, "cell 4 2");
            }
            {
                JLabel lblDensityerr = new JLabel("Density Desv=");
                panel.add(lblDensityerr, "cell 0 3,alignx trailing");
            }
            {
                txtDensityerr = new JTextField();
                txtDensityerr.setText("0");
                panel.add(txtDensityerr, "cell 1 3,growx");
                txtDensityerr.setColumns(10);
            }
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("Save DIC");
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

    private void inicia(){
        if (this.getNpeaks()>ds.getNpeaks())this.setNpeaks(ds.getNpeaks());
        txtNpeaks.setText(String.valueOf(this.getNpeaks()));
        txtAmax.setText(FileUtils.dfX_2.format(this.getAmax()));
        txtBmax.setText(FileUtils.dfX_2.format(this.getBmax()));
        txtCmax.setText(FileUtils.dfX_2.format(this.getCmax()));
        txtBetamin.setText(FileUtils.dfX_2.format(this.getBetamin()));
        txtBetamax.setText(FileUtils.dfX_2.format(this.getBetamax()));
        txtVmin.setText(FileUtils.dfX_2.format(this.getVmin()));
        txtVmax.setText(FileUtils.dfX_2.format(this.getVmax()));
        if (this.getWavel()<0){
            //mirem el de la dataserie
            txtWave.setText(FileUtils.dfX_5.format(ds.getWavelength()));
        }else{
            txtWave.setText(FileUtils.dfX_5.format(this.getWavel()));    
        }

        txtMw.setText(FileUtils.dfX_2.format(this.getMw()));
        txtDensity.setText(FileUtils.dfX_2.format(this.getDensity()));
        txtDensityerr.setText(FileUtils.dfX_2.format(this.getDensityerr()));
        txtEps.setText(FileUtils.dfX_2.format(this.getEps()));
        txtFom.setText(FileUtils.dfX_2.format(this.getMinfom()));
        txtSpurious.setText(String.valueOf(this.getSpurious()));
        
        //checkboxes
        chckbxCubic.setSelected(this.isCubic());
        chckbxTetragonal.setSelected(this.isTetra());
        chckbxHexagonal.setSelected(this.isHexa());
        chckbxOrthorhombic.setSelected(this.isOrto());
        chckbxMonoclinic.setSelected(this.isMono());
        chckbxTriclinic.setSelected(this.isTric());
        chckbxZeroRefienment.setSelected(this.isZeroRef());
        chckbxPrevZeroSearch.setSelected(this.isPrevzero());
        chckbxDicvolOpt.setSelected(this.isDic06());
        
        everythingOK = true;
    }
    
    public void updateDS(DataSerie newds){
        this.ds=newds;
        inicia();
    }
    
    protected void do_cancelButton_actionPerformed(ActionEvent e) {
        this.dispose();
    }
    protected void do_okButton_actionPerformed(ActionEvent e) {
        //PARSE ALL
        try{
            this.setAmax(Float.parseFloat(txtAmax.getText()));
        }catch(Exception ex){
            log.info("error reading amax");
            everythingOK=false;
        }
        try{
            this.setBmax(Float.parseFloat(txtBmax.getText()));
        }catch(Exception ex){
            log.info("error reading bmax");
            everythingOK=false;
        }
        try{
            this.setCmax(Float.parseFloat(txtCmax.getText()));
        }catch(Exception ex){
            log.info("error reading cmax");
            everythingOK=false;
        }
        try{
            this.setBetamin(Float.parseFloat(txtBetamin.getText()));
        }catch(Exception ex){
            log.info("error reading Beta min");
            everythingOK=false;
        }
        try{
            this.setBetamax(Float.parseFloat(txtBetamax.getText()));
        }catch(Exception ex){
            log.info("error reading Beta max");
            everythingOK=false;
        }
        try{
            this.setVmin(Float.parseFloat(txtVmin.getText()));
        }catch(Exception ex){
            log.info("error reading vmin");
            everythingOK=false;
        }
        try{
            this.setVmax(Float.parseFloat(txtVmax.getText()));
        }catch(Exception ex){
            log.info("error reading vmax");
            everythingOK=false;
        }
        try{
            this.setWavel(Float.parseFloat(txtWave.getText()));
        }catch(Exception ex){
            log.info("error reading wavelength");
            everythingOK=false;
        }
        
        try{
            this.setMw(Float.parseFloat(txtMw.getText()));
        }catch(Exception ex){
            log.info("error reading MW");
            everythingOK=false;
        }
        try{
            this.setDensity(Float.parseFloat(txtDensity.getText()));
        }catch(Exception ex){
            log.info("error reading Density");
            everythingOK=false;
        }
        try{
            this.setDensityerr(Float.parseFloat(txtDensityerr.getText()));
        }catch(Exception ex){
            log.info("error reading Density Desv");
            everythingOK=false;
        }
        try{
            this.setEps(Float.parseFloat(txtEps.getText()));
        }catch(Exception ex){
            log.info("error reading Eps");
            everythingOK=false;
        }
        try{
            this.setMinfom(Float.parseFloat(txtFom.getText()));
        }catch(Exception ex){
            log.info("error reading min FOM");
            everythingOK=false;
        }
        
        try{
            this.setNpeaks(Integer.parseInt(txtNpeaks.getText()));
            if (this.getNpeaks()>this.ds.getNpeaks()){
                this.setNpeaks(this.ds.getNpeaks());
            }
        }catch(Exception ex){
            log.info("error reading Npeaks");
            everythingOK=false;
        }
        try{
            this.setSpurious(Integer.parseInt(txtSpurious.getText()));
        }catch(Exception ex){
            log.info("error reading Spurious");
            everythingOK=false;
        }
        
        //checkboxes
        this.setCubic(chckbxCubic.isSelected());
        this.setTetra(chckbxTetragonal.isSelected());
        this.setHexa(chckbxHexagonal.isSelected());
        this.setOrto(chckbxOrthorhombic.isSelected());
        this.setMono(chckbxMonoclinic.isSelected());
        this.setTric(chckbxTriclinic.isSelected());
        this.setZeroRef(chckbxZeroRefienment.isSelected());
        this.setPrevzero(chckbxPrevZeroSearch.isSelected());
        this.setDic06(chckbxDicvolOpt.isSelected());
        
        this.dispose(); //TODO hide?
    }

    public float getAmax() {
        return amax;
    }

    public void setAmax(float amax) {
        this.amax = amax;
    }

    public float getBmax() {
        return bmax;
    }

    public void setBmax(float bmax) {
        this.bmax = bmax;
    }

    public float getCmax() {
        return cmax;
    }

    public void setCmax(float cmax) {
        this.cmax = cmax;
    }

    public float getBetamin() {
        return betamin;
    }

    public void setBetamin(float betamin) {
        this.betamin = betamin;
    }

    public float getBetamax() {
        return betamax;
    }

    public void setBetamax(float betamax) {
        this.betamax = betamax;
    }

    public float getVmin() {
        return vmin;
    }

    public void setVmin(float vmin) {
        this.vmin = vmin;
    }

    public float getVmax() {
        return vmax;
    }

    public void setVmax(float vmax) {
        this.vmax = vmax;
    }

    public float getWavel() {
        return wavel;
    }

    public void setWavel(float wavel) {
        this.wavel = wavel;
    }

    public float getMw() {
        return mw;
    }

    public void setMw(float mw) {
        this.mw = mw;
    }

    public float getDensity() {
        return density;
    }

    public void setDensity(float density) {
        this.density = density;
    }

    public float getDensityerr() {
        return densityerr;
    }

    public void setDensityerr(float densityerr) {
        this.densityerr = densityerr;
    }

    public float getEps() {
        return eps;
    }

    public void setEps(float eps) {
        this.eps = eps;
    }

    public float getMinfom() {
        return minfom;
    }

    public void setMinfom(float minfom) {
        this.minfom = minfom;
    }

    public int getSpurious() {
        return spurious;
    }

    public void setSpurious(int spurious) {
        this.spurious = spurious;
    }

    public boolean isCubic() {
        return cubic;
    }
    public int isCubicInt() {
        if (cubic) return 1;
        return 0;
    }

    public void setCubic(boolean cubic) {
        this.cubic = cubic;
    }

    public boolean isTetra() {
        return tetra;
    }
    public int isTetraInt() {
        if (tetra) return 1;
        return 0;
    }

    public void setTetra(boolean tetra) {
        this.tetra = tetra;
    }

    public boolean isHexa() {
        return hexa;
   }
    public int isHexaInt() {
        if (hexa) return 1;
        return 0;
    }

    public void setHexa(boolean hexa) {
        this.hexa = hexa;
    }

    public boolean isOrto() {
        return orto;
    }
    public int isOrtoInt() {
        if (orto) return 1;
        return 0;
    }
    
    public void setOrto(boolean orto) {
        this.orto = orto;
    }

    public boolean isMono() {
        return mono;
    }
    public int isMonoInt() {
        if (mono) return 1;
        return 0;
    }
    
    public void setMono(boolean mono) {
        this.mono = mono;
    }

    public boolean isTric() {
        return tric;
    }
    public int isTricInt() {
        if (tric) return 1;
        return 0;
    }
    
    public void setTric(boolean tric) {
        this.tric = tric;
    }

    public boolean isZeroRef() {
        return zeroRef;
    }
    public int isZeroRefInt() {
        if (zeroRef) return 1;
        return 0;
    }

    public void setZeroRef(boolean zeroRef) {
        this.zeroRef = zeroRef;
    }

    public boolean isPrevzero() {
        return prevzero;
    }
    public int isPrevzeroInt() {
        if (prevzero) return 1;
        return 0;
    }

    public void setPrevzero(boolean prevzero) {
        this.prevzero = prevzero;
    }

    public boolean isDic06() {
        return dic06;
    }
    public int isDic06Int() {
        if (dic06) return 1;
        return 0;
    }

    public void setDic06(boolean dic06) {
        this.dic06 = dic06;
    }

    public int getNpeaks() {
        return npeaks;
    }

    public void setNpeaks(int npeaks) {
        this.npeaks = npeaks;
    }

    public boolean isEverythingOK() {
        return everythingOK;
    }

    public void setEverythingOK(boolean everythingOK) {
        this.everythingOK = everythingOK;
    }
}

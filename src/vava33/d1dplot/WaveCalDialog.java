package com.vava33.d1dplot;

/**
 * D1Dplot
 * 
 * Wavelength calibration
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import javax.swing.JDialog;
import javax.swing.JPanel;

import com.vava33.BasicPlotPanel.core.SerieType;
import com.vava33.cellsymm.Cell;
import com.vava33.cellsymm.CellSymm_global;
import com.vava33.cellsymm.HKLrefl;
import com.vava33.d1dplot.auxi.Calibrant;
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.d1dplot.data.Xunits;
import com.vava33.jutils.FileUtils;
import com.vava33.jutils.VavaLogger;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JTextPane;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;

import javax.swing.JButton;
import javax.swing.JTextArea;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

public class WaveCalDialog {

	
	private JDialog CalibDialog;
    XRDPlot1DPanel plotpanel;
    D1Dplot_data dades;
    
    private static Calibrant silicon_640D = new Calibrant("Silicon NIST-640D",new Cell(5.43123,5.43123,5.43123,90,90,90,true,CellSymm_global.getSpaceGroupByNum(227)));
	private static Calibrant lab6_660B = new Calibrant("LaB6 NIST-660B",new Cell(4.15689,4.15689,4.15689,90,90,90,true,CellSymm_global.getSpaceGroupByNum(221)));
	protected static ArrayList<Calibrant> calibrants;
    
    private static final String className = "CalibrationDialog";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);
    
    private JTextField txtSiparam;
    private final ButtonGroup buttonGroup = new ButtonGroup();
    private JTextField txtNrpeaks;
    private JTextField txtEnergy;
    private JComboBox<Calibrant> comboCalib;
    private JCheckBox checkBoxCalibrant;
    private JCheckBox checkBoxSilicon;
    private JCheckBox chckbxFitPeaks;

    private double refWave=0;
    private double refZero=0;
    private double errWave=0;
    private double errZero=0;
    private JTextArea textArea;
    
    /**
     * Create the dialog.
     */
    public WaveCalDialog(XRDPlot1DPanel p,D1Dplot_data d) {
        this.plotpanel = p;
        this.dades=d;
        this.CalibDialog = new JDialog(D1Dplot_global.getD1DmainFrame(),"Wavelength Refinement",false);
        CalibDialog.setTitle("Wavelength Calibration");
        CalibDialog.getContentPane().setLayout(new MigLayout("", "[][grow][]", "[][][][][][][grow][]"));
        
        CalibDialog.setIconImage(D1Dplot_global.getIcon());
//        CalibDialog.setSize(814,227);

        final JTextPane txtpnSelectEitherA = new JTextPane();
        txtpnSelectEitherA.setEditable(false);
        txtpnSelectEitherA.setOpaque(false);
        txtpnSelectEitherA.setText("Select either a calibrant from the list or set the silicon cell parameter");
        CalibDialog.getContentPane().add(txtpnSelectEitherA, "cell 0 0 3 1,grow");
        
        checkBoxCalibrant = new JCheckBox("Calibrant");
        buttonGroup.add(checkBoxCalibrant);
        CalibDialog.getContentPane().add(checkBoxCalibrant, "cell 0 1");
        
        comboCalib = new JComboBox<Calibrant>();
        comboCalib.addItemListener(new ItemListener() {
        	public void itemStateChanged(ItemEvent e) {
        		do_comboCalib_itemStateChanged(e);
        	}
        });
        CalibDialog.getContentPane().add(comboCalib, "cell 1 1 2 1,growx");
        
        checkBoxSilicon = new JCheckBox("Si cell parameter ("+D1Dplot_global.angstrom+")");
        buttonGroup.add(checkBoxSilicon);
        CalibDialog.getContentPane().add(checkBoxSilicon, "cell 0 2");
        
        txtSiparam = new JTextField();
        CalibDialog.getContentPane().add(txtSiparam, "cell 1 2 2 1,growx");
        txtSiparam.setColumns(10);
        
        final JLabel lblNrOfPeaks = new JLabel("Nr. of peaks to use");
        CalibDialog.getContentPane().add(lblNrOfPeaks, "cell 0 3,alignx trailing");
        
        txtNrpeaks = new JTextField();
        txtNrpeaks.setText("6");
        CalibDialog.getContentPane().add(txtNrpeaks, "cell 1 3,growx");
        txtNrpeaks.setColumns(10);
        
        chckbxFitPeaks = new JCheckBox("Fit peaks");
        chckbxFitPeaks.setToolTipText("To determine peak position. Otherwise the 2-theta at maximum intensity will be used");
        CalibDialog.getContentPane().add(chckbxFitPeaks, "cell 2 3");
        
        final JLabel lblEnergyUsedkev = new JLabel("E (keV) or "+D1Dplot_global.lambda+" ("+D1Dplot_global.angstrom+") used");
        lblEnergyUsedkev.setToolTipText("You can also input wavelength in Angstrom here...");
        CalibDialog.getContentPane().add(lblEnergyUsedkev, "cell 0 4,alignx trailing");
        
        txtEnergy = new JTextField();
        CalibDialog.getContentPane().add(txtEnergy, "cell 1 4 2 1,growx");
        txtEnergy.setColumns(10);
        
        final JButton btnRefine = new JButton("Refine");
        btnRefine.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		do_btnRefine_actionPerformed(e);
        	}
        });
        CalibDialog.getContentPane().add(btnRefine, "cell 0 5 3 1,growx");
        
        textArea = new JTextArea();
        CalibDialog.getContentPane().add(textArea, "cell 0 6 3 1,grow");
        
        final JPanel panel = new JPanel();
        CalibDialog.getContentPane().add(panel, "cell 0 7 3 1,grow");
        panel.setLayout(new MigLayout("", "[][208px][]", "[25px]"));
        
        final JButton btnSaveToText = new JButton("Save to text file");
        btnSaveToText.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		do_btnSaveToText_actionPerformed(e);
        	}
        });
        panel.add(btnSaveToText, "cell 0 0,growx,aligny center");
        
        final JButton btnApplyValuesTo = new JButton("Apply to patterns");
        btnApplyValuesTo.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		do_btnApplyValuesTo_actionPerformed(e);
        	}
        });
        panel.add(btnApplyValuesTo, "cell 1 0,growx,aligny center");
        
        final JButton btnClose = new JButton("Close");
        btnClose.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		do_btnClose_actionPerformed(e);
        	}
        });
        panel.add(btnClose, "cell 2 0,growx,aligny center");
        log.info("Wavelength calibration HAS NOT BEEN TESTED. It may not work properly"); 
        
        init();
        btnSaveToText.setVisible(false); //NO ESTA IMPLEMENTAT
    }
    
    private void init() {
    	//aqui afegirem els calibrants per defecte (els user ja s'haurien d'haver afegit al llegir opcions)
    	calibrants.add(silicon_640D);
    	calibrants.add(lab6_660B);
    	for (Calibrant c:calibrants) {
    		this.comboCalib.addItem(c);	
    	}
    	this.checkBoxCalibrant.setSelected(true);
    	this.comboCalib.setSelectedItem(silicon_640D);
        CalibDialog.pack();
        

    }
	protected void do_btnRefine_actionPerformed(ActionEvent e) {
		
		try{
	    	
			DataSerie peaks = dades.getFirstSelectedDataSet().getFirstDataSerieByType(SerieType.peaks);

			if (peaks==null) {
				//intentem fer-ho automaticament
				D1Dplot_global.getD1Dmain().autoFindPeaksCurrentSerie();
				peaks = dades.getFirstSelectedDataSet().getFirstDataSerieByType(SerieType.peaks);
				if (peaks==null) {
					log.info("Please, run FindPeaks first to create a peak(s) series");
					return; //TODO: missatge primer fer pics
				}else {
					log.info("Automatic peak search, check in case of strange results");
				}
			}
			
			final int nPeaks = FastMath.min(Integer.parseInt(this.txtNrpeaks.getText()),peaks.getNPoints()); //o l'entrat o el maxim de peaks trobats
			Cell calibCell;
			if (this.checkBoxCalibrant.isSelected()) {
				//agafar el calibrant
				calibCell = ((Calibrant)this.comboCalib.getSelectedItem()).getCell();
			}else {
				//agafar el parametre del silici
				final double siPar = Double.parseDouble(this.txtSiparam.getText());
				calibCell = new Cell(siPar,siPar,siPar,90,90,90,true,CellSymm_global.getSpaceGroupByNum(227));
			}
			double guessWave = Double.parseDouble(this.txtEnergy.getText());
			if (guessWave > 2) { //it is an energy -> convert
				guessWave = 12.398/guessWave;
			}
			
			//TODO aqui si hi ha wave es pot mirar de determinar quina és la qmax possible
			final double Qmax = 7; //equival a dsp=0.9 ... hauria de ser suficient
			final List<HKLrefl>reflsFULL = calibCell.generateHKLsAsymetricUnitCrystalFamily(Qmax, true, true, true, false, true);
//	    	refls.remove(3); //TODO ?
		

			//TODO hauriem de comprovar que els primers nPeaks de PEAKS i REFLS corresponen a les mateixes reflexions!! (fer una finestra de 2-theta?)
            final double tthAcceptanceWin = 0.5;
            boolean[] parellaOK = new boolean[nPeaks];
            //escapcem peaks a nPeaks
            
//            for (int i=nPeaks; i<peaks.getNPoints();i++) {
//            	peaks.removePoint(i);
//            } //SI TREIEM PUNT I I AUGMENTEM I MALAMENT
            
            peaks = peaks.getSubDataSerie(peaks.getMinX(), peaks.getCorrectedPoint(5, false).getX());
            
//			for (int i = 0; i < nPeaks; i++) {
//	    		double tthCAL = reflsFULL.get(i).calct2(guessWave, true); 
//	    		double tthOBS = peaks.getCorrectedPoint(i, false).getX();
//	    		if (FastMath.abs(tthCAL-tthOBS)>0.5){
//	    			peaks.removePoint(i);
//	    		}
//            }
            final List<HKLrefl>refls = new ArrayList<HKLrefl>();
            for (int i = 0; i < nPeaks; i++) {
            	parellaOK[i]=false;
            	double tthOBS = peaks.getCorrectedPoint(i, false).getX();
            	for (HKLrefl hkl:reflsFULL) {
            		double tthCAL = hkl.calct2(guessWave, true);
            		if (FastMath.abs(tthCAL-tthOBS)>tthAcceptanceWin){
            			//borrar (no cal) i continuar buscant
            			continue;
            		}
            		//s'ha trobat, es posa a l'array i a parellaOK
            		refls.add(hkl);
            		parellaOK[i]=true;
            		break;
            	}
            }
            
            log.debug(parellaOK.toString());
            
            for (int i = 0; i < nPeaks; i++) {
            	if (parellaOK[i]!=true) {
            		log.warning("Calculated and Observed 2-theta too different, check parameters");
            		peaks.removePoint(i); //provem d'eliminar-lo si no té equivalent calculat
            	}
            }
            if (refls.size()!=peaks.getNPoints()) {
            	log.warning("Nr of peaks and calculated reflections do not match... aborting");
            	return;
            }

	        //TEST WITH LS
	        MultivariateJacobianFunction minimize2theta = new MultivariateJacobianFunction() {
	            public Pair<RealVector, RealMatrix> value(final RealVector point) {
	            	
	            	double wave = point.getEntry(0);
	            	double zero = point.getEntry(1);
	            	
	                RealVector value = new ArrayRealVector(nPeaks);
	                RealMatrix jacobian = new Array2DRowRealMatrix(nPeaks, 2);

	                for (int i = 0; i < nPeaks; i++) {
	            		double tthCAL = refls.get(i).calct2(wave, true) + zero; //si aqui poso + després el zero l'hauré d'aplicar RESTANT a les dades 
	                    value.setEntry(i, tthCAL);
	                    // derivative with respect to p0 = wave
	                    double dsp = refls.get(i).getDsp();
	                    double arg = (wave*wave)/(4*dsp*dsp);
	                    double deriv = (1/(dsp*FastMath.sqrt(1-arg)))*(1/(2*dsp));
	                    jacobian.setEntry(i, 0, deriv);
	                    // derivative with respect to p1 = zero
	                    jacobian.setEntry(i, 1, 1);
	                }
	                return new Pair<RealVector, RealMatrix>(value, jacobian);
	            }
	        };
	        
	        RealVector pics = new ArrayRealVector(nPeaks);
	        for (int i=0;i<nPeaks;i++) {
	        	pics.setEntry(i, peaks.getDataPointX_as(Xunits.tth, peaks.getRawPoint(i)));
	        }
	        
	        LeastSquaresProblem problem = new LeastSquaresBuilder().
	        		start(new double[] { guessWave, 0.00001 }).
	        		model(minimize2theta).
	        		target(pics).
	        		lazyEvaluation(false).
	        		maxEvaluations(50000).
	        		maxIterations(50000).
//	        		checker(checker).
	        		build();
	        
	        
	        LeastSquaresOptimizer optimizer = new LevenbergMarquardtOptimizer().withInitialStepBoundFactor(0.1); //IMPORTANT el 0.1, sino no convergeix als valors correctes...

	        LeastSquaresOptimizer.Optimum optimum = optimizer.optimize(problem);

	        refWave=optimum.getPoint().getEntry(0);
	        errWave=optimum.getRMS()*optimum.getSigma(0).getEntry(0);
	        refZero=optimum.getPoint().getEntry(1);
	        errZero=optimum.getRMS()*optimum.getSigma(0).getEntry(1);
	        
	        textArea.append("========== Calibration results ================\n");
	        textArea.append("   Wavelength ("+D1Dplot_global.angstrom+") =" + FileUtils.dfX_6.format(refWave) + " +- " + FileUtils.dfX_6.format(errWave)+"\n");
	        textArea.append("   Zero shift (º) =" + FileUtils.dfX_6.format(refZero)+ " +- " + FileUtils.dfX_6.format(errZero)+"\n");
	        
	        log.info("========== Calibration results ================");
	        log.info("   Wavelength ("+D1Dplot_global.angstrom+") =" + FileUtils.dfX_6.format(refWave) + " +- " + FileUtils.dfX_6.format(errWave));
	        log.info("   Zero shift (º) =" + FileUtils.dfX_6.format(refZero)+ " +- " + FileUtils.dfX_6.format(errZero));
	        
	        log.debug("===============================================");
	        log.debug("RMS: "           + optimum.getRMS()); //Get the normalized cost. It is the square-root of the sum of squared of the residuals, divided by the number of measurements.
	        log.debug("evaluations: "   + optimum.getEvaluations());
	        log.debug("iterations: "    + optimum.getIterations());
	        log.debug("cov: "    + optimum.getSigma(0));
//	        log.debug("cov: "    + optimum.getCovariances(0));
	        log.debug("residuals: " + optimum.getResiduals());
	        log.debug("cost: " + optimum.getCost());
	        log.debug("point: " + optimum.getPoint());

	        log.debug("Nr. observations: " + problem.getObservationSize());
	        log.debug("Nr. parameters: " + problem.getParameterSize());
	        
	        int dof = problem.getObservationSize() - problem.getParameterSize();
//	        double chisq = FastMath.sqrt(optimum.getResiduals().getEntry(0)*optimum.getResiduals().getEntry(0)+(optimum.getResiduals().getEntry(1)*optimum.getResiduals().getEntry(1)))/dof;
	        double chisq = 0;
	        for (double res:optimum.getResiduals().toArray()) {
	        	chisq = chisq + (res*res);
	        }
//	        double chisq = (optimum.getResiduals().getEntry(0)*optimum.getResiduals().getEntry(0)+(optimum.getResiduals().getEntry(1)*optimum.getResiduals().getEntry(1)))/dof;
	        log.debug("chi square: " + chisq);
	        log.debug("sqrt(chisq/dof): "+ FastMath.sqrt(chisq/dof));
	        log.debug("sqrt(chisq/dof): "+ FastMath.sqrt(chisq/6)); //AQUEST CORRESPON AMB EL RMS DE APACHE
	        log.debug("sqrt(chisq)/dof: "+ FastMath.sqrt(chisq)/dof);
	        log.debug("ff way error wave: "+ optimum.getSigma(0).getEntry(0)*FastMath.sqrt(chisq/dof));
	        log.debug("error by math3: "+ optimum.getRMS()*optimum.getSigma(0).getEntry(0));
	        log.debug("error (zero) by math3: "+ optimum.getRMS()*optimum.getSigma(0).getEntry(1));
	        
	        log.debug("tth obs: "+pics);
//			final List<HKLrefl> cal = siCell.generateHKLsAsymetricUnitCrystalFamily(Qmax, true, true, true, false, true);
//			cal.remove(3);
	    	RealVector picsCAL = new ArrayRealVector(nPeaks);
	        for (int i=0;i<nPeaks;i++) {
//	        	picsCAL.setEntry(i, cal.get(i).calct2(optimum.getPoint().getEntry(0), true)+optimum.getPoint().getEntry(1));
	        	picsCAL.setEntry(i, refls.get(i).calct2(optimum.getPoint().getEntry(0), true)+optimum.getPoint().getEntry(1));
	        }
	        log.debug("tth cal: "+picsCAL);

	        /*
	         * A veure...
	         * CHI2 és la suma dels quadrats dels residuals == SUM[(2tc-2ti)**2]
	         * 
	         * Matriu de covariances és la inversa de inversion of the JTJ matrix, where J is the Jacobian matrix
	         * -- llavors la desviacio del parametre i correspon a la sqrt del valor C[i][i] de la matriu covariança
	         * (estimate of the standard deviation: the square root of the diagonal coefficients of the covariance matrix, sd(a[i]) ~= sqrt(C[i][i]), where a[i] is the optimized value of the i-th parameter, and C is the covariance matrix.)
	         *  
	         *  Per calcular l'error es multiplica aquesta sqrt de la diagonal pel sqrt(chisq/dof), que es el nostre RMS.
	         *  
	         */
	    	
		}catch(Exception ex) {
			if(D1Dplot_global.isDebug())ex.printStackTrace();
		}
		
	}
	protected void do_btnSaveToText_actionPerformed(ActionEvent e) {
		//TODO
		//fer que surti calc vs obs, diff, etc.. com el sipks
	}
	protected void do_btnApplyValuesTo_actionPerformed(ActionEvent e) {
		for (int i=0; i<dades.getNDataSets(); i++) {
			dades.getDataSet(i).setWavelengthToAllSeries(refWave);
				dades.getDataSet(i).setZeroToAllSeries(refZero);
		}
		dades.updateFullTable();
		
	}
	protected void do_btnClose_actionPerformed(ActionEvent e) {
		CalibDialog.dispose();
	}
	
    public void visible(boolean vis) {
    	this.CalibDialog.setVisible(vis);
    }
	protected void do_comboCalib_itemStateChanged(ItemEvent e) {
		this.txtSiparam.setText(String.format("%.5f",((Calibrant)this.comboCalib.getSelectedItem()).getCell().getCellParameters(true)[0]));
	}
}

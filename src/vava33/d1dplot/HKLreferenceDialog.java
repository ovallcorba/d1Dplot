package com.vava33.d1dplot;

/**
 * D1Dplot
 * 
 * HKLreference Dialog
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import javax.swing.JDialog;
import com.vava33.d1dplot.auxi.Reference;
import com.vava33.d1dplot.auxi.DataFileUtils;
import com.vava33.d1dplot.auxi.DataPoint;
import com.vava33.d1dplot.auxi.DataSerie;
import com.vava33.d1dplot.auxi.Pattern1D;
import com.vava33.d1dplot.auxi.DataSerie.serieType;
import com.vava33.d1dplot.auxi.DataSerie.xunits;
import com.vava33.jutils.FileUtils;
import com.vava33.jutils.VavaLogger;
import net.miginfocom.swing.MigLayout;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.event.ActionEvent;

public class HKLreferenceDialog {
	
    private static final String className = "HKLreferenceDialog";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);
    
    private JDialog refDialog;
    private JList<Reference> refList;
    private ArrayList<Reference> refs;
    private D1Dplot_main main;
    
    public HKLreferenceDialog(D1Dplot_main m) {
    	this.main=m;
    	refDialog = new JDialog(m.getMainFrame(), "Reference HKL positions",false);
    	refDialog.setLocationRelativeTo(m.getMainFrame());
    	refDialog.getContentPane().setLayout(new MigLayout("", "[grow]", "[][grow][][][]"));
    	
    	JButton btnAddRefTo = new JButton("Load REF file and add to list");
    	btnAddRefTo.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			do_btnAddRefTo_actionPerformed(e);
    		}
    	});
    	refDialog.getContentPane().add(btnAddRefTo, "cell 0 0,growx");
    	
    	JScrollPane scrollPane = new JScrollPane();
    	refDialog.getContentPane().add(scrollPane, "cell 0 1,grow");
    	
    	refList = new JList<Reference>();
    	scrollPane.setViewportView(refList);
    	
    	JButton btnAddRefAs = new JButton("Add REF as Plotted serie");
    	btnAddRefAs.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			do_btnAddRefAs_actionPerformed(e);
    		}
    	});
    	refDialog.getContentPane().add(btnAddRefAs, "cell 0 2,growx");
    	
    	JButton btnSaveRefsIn = new JButton("Save REFs in config file");
    	btnSaveRefsIn.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			do_btnSaveRefsIn_actionPerformed(e);
    		}
    	});
    	refDialog.getContentPane().add(btnSaveRefsIn, "cell 0 3,growx");
    	
    	JButton btnClose = new JButton("Close");
    	btnClose.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			do_btnClose_actionPerformed(e);
    		}
    	});
    	refDialog.getContentPane().add(btnClose, "cell 0 4,alignx right");
    	refDialog.pack();
    	this.inicia();
    }
    
    private void inicia() {
    	this.refs = new ArrayList<Reference>();
    	//afegim els calibrants per defecte i els de les opcions a la llista
    	this.addCalibrant(new Reference("Silicon",Reference.Silicon_d));
    	this.addCalibrant(new Reference("LaB6",Reference.LaB6_d));
    	//TODO:llista de calibrants de les opcions
    	
    	//afegim a la llista
    	this.updateList();
    }
    
    private void updateList() {
    	DefaultListModel<Reference> lm = new DefaultListModel<Reference>();
    	Iterator<Reference> itrC = this.refs.iterator();
    	while (itrC.hasNext()) {
    		lm.addElement(itrC.next());
    	}
    	this.refList.setModel(lm);
    }
    
    private void addCalibrant(Reference c) {
    	this.refs.add(c);
    }
    
	private void do_btnAddRefTo_actionPerformed(ActionEvent e) {
		File f = FileUtils.fchooserOpen(this.refDialog, D1Dplot_global.getWorkdirFile(), DataFileUtils.getExtensionFilterRefRead(), 0);
		if (f!=null) {
			Reference ref = DataFileUtils.readREFFile(f);
			if (ref==null) {
		        log.warning("Error reading reference file");
		        return;
		    }
			//afegir a la llista, aqui en principi tenim ja el calibrant llegit al pattern amb 
			this.addCalibrant(ref);
			this.updateList();
		}
	}
	private void do_btnAddRefAs_actionPerformed(ActionEvent e) {
		if (this.getSelectedRef()!=null) {
			Pattern1D patt = new Pattern1D();
			DataSerie ds = new DataSerie(serieType.ref);
			Reference c = this.getSelectedRef();
			ds.setxUnits(xunits.dsp);
			for (int i=0;i<c.getNpoints();i++) {
				ds.addPoint(new DataPoint(c.getDsp(i),c.getInten(i),0));	
			}
			
			//ara mirem si podem convertir a les unitats de la primera dataserie
			DataSerie first = main.getPanel_plot().getFirstPlottedSerie();			
			if (first!=null) {
				if (first.getxUnits()==xunits.tth) {
					//necessitem la wavelength
					if (ds.getWavelength()<=0) {
				        String s = (String)JOptionPane.showInputDialog(
				                this.refDialog,
				                "Wavelength (Ang) =",
				                "Wavelength required to add reference in 2Theta units",
				                JOptionPane.PLAIN_MESSAGE,
				                null,
				                null,
				                "");
				        
				        if ((s != null) && (s.length() > 0)) {
				            double wave = -1;
				            try{
				                wave = Double.parseDouble(s);
				                ds.setWavelength(wave);
				            }catch(Exception ex){
				                log.debug("Error parsing wavelength");
				                return;
				            }
				            if (wave<0){
				                log.warning("Invalid wavelength entered");
				                return;
				            }
				        }
					}
					if (ds.getWavelength()<=0) {
						log.warning("REFERENCE WILL BE ADDED IN D-SPACING UNITS");
						return;
					}
				}
				ds.convertToXunits(first.getxUnits());
			}
			patt.addDataSerie(ds);
			main.getPanel_plot().getPatterns().add(patt);
			main.updateData(false);
		}
	}
	
	private Reference getSelectedRef() {
		return this.refList.getModel().getElementAt(this.refList.getSelectedIndex());
	}
	
	private void do_btnSaveRefsIn_actionPerformed(ActionEvent e) {
		log.warning("Not Implemented"); //TODO
	}
	
	private void do_btnClose_actionPerformed(ActionEvent e) {
		this.tanca();
	}
	
	public void tanca() {
		this.refDialog.dispose();
	}
	
	public void visible(boolean vis) {
		this.refDialog.setVisible(vis);
	}
}

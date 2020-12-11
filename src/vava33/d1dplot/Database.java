package com.vava33.d1dplot;

/**    
 * Database dialog
 *   
 * @author Oriol Vallcorba
 * Licence: GPLv3
 *   
 */

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.event.ListSelectionEvent;

import com.vava33.BasicPlotPanel.core.Plottable_point;
import com.vava33.BasicPlotPanel.core.SerieType;
import com.vava33.cellsymm.HKLrefl;
import com.vava33.cellsymm.PDCompound;
import com.vava33.cellsymm.PDDatabase;
import com.vava33.cellsymm.PDDatabase_dialog;
import com.vava33.d1dplot.data.DataPoint_hkl;
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.d1dplot.data.DataSet;
import com.vava33.d1dplot.data.Xunits;
import com.vava33.jutils.FileUtils;

public class Database extends PDDatabase_dialog {
    
    XRDPlot1DPanel plotpanel;
    D1Dplot_data dades;
    
    /**
     * Create the dialog.
     * @wbp.parser.entryPoint
     */
    public Database(XRDPlot1DPanel plotp,D1Dplot_data data) {
    	super(D1Dplot_global.getD1DmainFrame());
        this.setPlotpanel(plotp);
        this.dades=data;
        DBdialog.setIconImage(D1Dplot_global.getIcon());
        D1Dplot_global.showOnScreen(D1Dplot_global.getDisplayMonitor(), DBdialog, true);
        this.initForD1D();
    }

    public void initForD1D(){
        chckbxIntensity.setSelected(plotpanel.isShowDBCompoundIntensity());
        chckbxPDdata.setSelected(true);
        this.listCompounds.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

	public void setPlotpanel(XRDPlot1DPanel plotpanel) {
		this.plotpanel = plotpanel;
	}

    public XRDPlot1DPanel getPlotpanel() {
		return plotpanel;
	}
	
	@Override
	protected void actualitzaPlot() {
		this.plotpanel.actualitzaPlot();
		
	}

	@Override
	protected void checkboxShowCanvia() {
        this.getPlotpanel().setShowDBCompound(this.isShowDataPeaks());
        this.actualitzaPlot(); //AIXO HAURIA D'ANAR A DINS DE CHECKBOXSHOWCANVIA
		
	}

	@Override
	protected String getWorkDir() {
		return D1Dplot_global.getWorkdir();
	}

	@Override
	protected void setWorkDir(File f) {
		D1Dplot_global.setWorkdir(f);
		
	}

	@Override
	protected String getDBFile() {
		return D1Dplot_global.DBfile;
	}

	@Override
	protected void setDBFile(String s) {
		D1Dplot_global.DBfile=s;
		
	}

	@Override
	protected void do_listCompounds_valueChanged(ListSelectionEvent arg0) {
        if (arg0.getValueIsAdjusting()) return;
        
        PDCompound comp = this.getCurrentCompound(); //TODO fer com el d2D que es poden seleccionar varis o aqui no cal perquè ho posem com a serie?
        if (comp!=null) {
          //fill the fields of DB
          this.updateInfo(comp);
        }else {
            return;
        }
        //now to plot
        DataSerie ds = this.getPDCompoundAsREFDataSerie(comp);
        
        //ara mirem si podem convertir a les unitats de la primera dataserie 
        DataSerie first = dades.getFirstPlottedDataSerie();           
        if (first!=null) {
            if (first.getxUnits()==Xunits.tth) {
                //necessitem la wavelength
                double wave = first.getWavelength();
                if (wave<=0) {
                    wave = FileUtils.DialogAskForPositiveDouble(null,"Wavelength (Ang) =","Wavelength required to add reference in 2Theta units", "");
                }
                if (wave<=0) {
                    log.warning("Wavelength required to convert to 2-theta");
                    return;
                }
                ds.setWavelength(wave);
                first.setWavelength(wave);
                dades.updateFullTable();
            }
            ds.convertDStoXunits(first.getxUnits());
        }
        
        this.getPlotpanel().dbCompound=ds;
        this.getPlotpanel().actualitzaPlot();
		
	}

	@Override
	public void searchPeaks() {
        if (!dades.isOneSerieSelected()) {
            log.info("Please select only one pattern with a peaks serie to search by them");
            return;
        }
        DataSerie pks = dades.getFirstSelectedDataSet().getFirstDataSerieByType(SerieType.peaks);
        if (pks==null) {
            log.info("Please select only one pattern with a peaks serie to search by them");
            return;
        }
    	
        if(pks.getNPoints()<=0) {
    		log.warning("no peaks selected");
    		return;
    	}
    	
        pm = new ProgressMonitor(DBdialog,
                "Searching for peak matching...",
                "", 0, 100);
        pm.setProgress(0);
        
        pBarDB.setString("Searching DB");
        pBarDB.setStringPainted(true);
        
        List<Double> dspList = new ArrayList<Double>();
        List<Double> intList = new ArrayList<Double>();
        
        for (int i=0; i<pks.getNPoints(); i++) {
        	Plottable_point pk = pks.getCorrectedPoint(i,false);
        	double dsp = pks.getDataPointX_as(Xunits.dsp, pk);
        	double inten = pk.getY();
        	if (dsp > minDspacingToSearch){
        		dspList.add(dsp);
        		intList.add(inten);
        	}
        }
        
        searchDBwk = new PDDatabase.searchDBWorker(dspList,intList);
        searchDBwk.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
              if ("progress" == evt.getPropertyName() ) {
                  int progress = (Integer) evt.getNewValue();
                  pm.setProgress(progress);
                  pBarDB.setValue(progress);
                  pm.setNote(String.format("%d%%\n", progress));
                  if (pm.isCanceled() || searchDBwk.isDone()) {
                      Toolkit.getDefaultToolkit().beep();
                      if (pm.isCanceled()) {
                          searchDBwk.cancel(true);
                          searchDBwk.setStop(true);
                          log.warning("search cancelled");
                      } else {
                    	  log.info("search finished!");
                          loadSearchPeaksResults();
                      }
                      pm.close();
                      pBarDB.setValue(100);
                      pBarDB.setStringPainted(false);
                  }
              }
            }
        });
                
        searchDBwk.execute();
		
	}

	public DataSerie getPDCompoundAsREFDataSerie(PDCompound comp) {
        DataSerie ds = new DataSerie(SerieType.ref,Xunits.dsp, null);
        for (HKLrefl pdr:comp.getPeaks()) {
            ds.addPoint(new DataPoint_hkl(pdr.getDsp(),pdr.getYcalc(),0,pdr,ds));
        }
        ds.setName(comp.getCompNames().get(0));
        return ds;
	}
	
	
	@Override
	protected void do_btnAddAsSerie_actionPerformed(ActionEvent e) {
		PDCompound pdc = this.getCurrentCompound();
		if (pdc!=null) {
			DataSerie ref = this.getPDCompoundAsREFDataSerie(pdc);
			//ara mirem si podem convertir a les unitats de la primera dataserie plotejada
			DataSerie first = dades.getFirstPlottedDataSerie();       
			ref.setWavelength(first.getWavelength());
			if (first!=null) {
				boolean ok = ref.convertDStoXunits(first.getxUnits()); //ja pregunta per la wavelength si es necessari
				if(!ok) {
					log.warning("Error adding compound as dataserie");
					return;                    
				}

				DataSet dc = new DataSet(ref.getWavelength());
				dc.addDataSerie(ref);
				dades.addDataSet(dc, true, true); //ja actualitza tot
			}
		}
	}

	@Override
	protected void do_chckbxIntensity_itemStateChanged(ItemEvent e) {
		this.plotpanel.setShowDBCompoundIntensity(chckbxIntensity.isSelected()); 
		this.plotpanel.actualitzaPlot();
	}
}
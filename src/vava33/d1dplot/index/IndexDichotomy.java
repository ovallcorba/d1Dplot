package com.vava33.d1dplot.index;

/*
 * Implementation of the dichotomy method
 * 
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.JProgressBar;

import org.apache.commons.math3.util.FastMath;

import com.vava33.cellsymm.Cell;
import com.vava33.cellsymm.CellSymm_global.CrystalFamily;
import com.vava33.d1dplot.D1Dplot_global;
import com.vava33.d1dplot.IndexDialog.DichothomyWorker;
import com.vava33.d1dplot.auxi.CartesianProduct;
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.jutils.FileUtils;
import com.vava33.jutils.VavaLogger;

public class IndexDichotomy extends IndexMethod{

    private static final String className = "IndexDichotomy";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);

    private static final float DEF_iniIncPar = 1.2f;
    private static final float DEF_iniIncAngDeg = 5.0f;
    private static final int DEF_numIter = 5;
    private static final int DEF_maxSol = 100000; //estava a 15 inicialment... ho pujo a 60, mes no poso perque trigaria massa i probablement està divergint (es podria comprovar aixo...)

    float[] aVals,bVals,cVals,alVals,beVals,gaVals; //ELS inicials (primera iteracio)
    
//    List<IndexSolutionDichotomy> finalSols;
    
    //constructor mínim
    public IndexDichotomy(DataSerie ds, int nPeaksToUse, double deltaQerror, float minfom, int spurious) {
        super(ds,nPeaksToUse,deltaQerror,minfom,spurious);
//        finalSols = new ArrayList<IndexSolutionDichotomy>();
    }
    
    protected void updateProgressBar(JProgressBar pbar, int currValue, int maxValue, String crystsystem, float estTimeMin) {
        pbar.setIndeterminate(false);
        pbar.setValue(currValue);
        pbar.setString(String.format("[%s] %d of %d iter (est. time %6.2f min.)",crystsystem,currValue,maxValue,estTimeMin));
    }
    
    private void generateIniVals(float incp, float inca) {
        //primer hem d'agafar els increments inicials i generar la primera iteració que és la general
        aVals = FileUtils.arange(aMin, aMax, incp);
        bVals = FileUtils.arange(bMin, bMax, incp);
        cVals = FileUtils.arange(cMin, cMax, incp);
        alVals = FileUtils.arange(alMin, alMax, inca);
        beVals = FileUtils.arange(beMin, beMax, inca);
        gaVals = FileUtils.arange(gaMin, gaMax, inca);
    }
    
    //agafa una llista de solucions i fa la particio (dicotomia), checkeja noves solucions i retorna les poteincials
    private List<IndexSolutionDichotomy> runIteration(List<IndexSolutionDichotomy> iterSols){
        //ara hem de seguir la iteració per cadascuna de les solucions
        Iterator<IndexSolutionDichotomy> itrS = iterSols.iterator();
        List<IndexSolutionDichotomy> iterSols2 = new ArrayList<IndexSolutionDichotomy>();
        while (itrS.hasNext()) {
            IndexSolutionDichotomy is = itrS.next();
            //ara hem de crear la particio d'aquesta is a incPar/2
            for (IndexSolutionDichotomy isnext: is.generateNextIter()) {
                //check sol
                if (isnext.areAllQobsInsideHKLQintervals()) {
                    iterSols2.add(isnext);
                    log.debug("is solution:"+isnext.toString());   
                }
            }
        }
        return iterSols2;
    }
    
    
    public List<IndexSolutionDichotomy> runIndexing(int numIter, float iniIncPar, float iniIncAng, JProgressBar pbar, DichothomyWorker sw) {

        if (iniIncPar<=0)iniIncPar=DEF_iniIncPar;
        if (iniIncAng<=0)iniIncAng=DEF_iniIncAngDeg;
        if (numIter<=0)numIter=DEF_numIter;
        
        List<IndexSolutionDichotomy> finalSols = new ArrayList<IndexSolutionDichotomy>();
        
        //debug print Qobs
        for (double q:Qobs) {
            log.infof("Qobs=%.5f",q);
        }
        
        //progress
        long startTime = System.currentTimeMillis();
        int ns=0;
        if (cubic)ns++;
        if (tetra)ns++;
        if (hexa)ns++;
        if (orto)ns++;
        if (mono)ns++;
        if (tric)ns++;
        int maxValue = ns*numIter;
        prepareProgressBar(pbar, maxValue);
        int iIter = 0;
        pbar.setIndeterminate(true);
        pbar.setString("running...");
        
        
        //preparacio i calculs previs
        float currIncPar = iniIncPar;
        float currIncAng = (float) FastMath.toRadians(iniIncAng);
        this.generateIniVals(currIncPar, currIncAng);
        //has colinear?
        this.prepareD1D2Colinear();
        
        //cal fer-ho tot per cada sistema cristal·lí
        if (this.cubic) {
            log.info("CUBIC search");
            List<IndexSolutionDichotomy> iterSols = new ArrayList<IndexSolutionDichotomy>();
            //primera iteracion (es la diferent per cada sistema)
            for (float par:aVals) {
                Cell candidateCell = new Cell(par,par,par,90.0,90.0,90.0,true, CrystalFamily.CUBIC);
                if (!this.considerForIndexing(candidateCell)) continue;
                //TODO: A partir d'aqui comú a tots els sistemes, podriem fer un metode que fos checkAndAdd(solucions) que retornes solucions actualitzat
                if (candidateCell.getVol()<this.vMin)continue; //volume only tested on first iteration
                if (candidateCell.getVol()>this.vMax)continue;
                //check sol
                IndexSolutionDichotomy candidateSol = new IndexSolutionDichotomy(candidateCell,currIncPar,0,this); //0 increment a angle (cubic)
                if (candidateSol.areAllQobsInsideHKLQintervals()) {
                    iterSols.add(candidateSol);
                    log.debug("is solution:"+candidateSol.toString());   
                }
                if (sw.isCancelled())return finalSols;
            }
            iIter++;
            this.updateProgressBar(pbar, iIter, maxValue, CrystalFamily.CUBIC.getNameString(), this.calcEstTime(startTime, iIter, maxValue));
            log.info("[cubic] iter 1, sols="+iterSols.size());

            //ara les iteracions amb les solucions que tenim per anar "afinant"  --> practicament identic per tots els sistemes
            boolean error = false;
            for (int i=2;i<=numIter;i++) {
                //primer comprovem si el num de solucions no es molt gran
                if (iterSols.size()>DEF_maxSol) {
                    log.warning("[cubic] Too many solutions, please reduce cell volume (or parameters) and/or Q error");
                    error=true;
                    iIter = iIter + (numIter-i);
                    break;
                }
                //ara hem de seguir la iteració per cadascuna de les solucions
                List<IndexSolutionDichotomy> iterSols2 = this.runIteration(iterSols);
                //borrem sols i copiem les de sols2
                iterSols.clear();
                iterSols.addAll(iterSols2);

                log.info("[cubic] iter "+i+", sols="+iterSols.size());
                iIter++;
                this.updateProgressBar(pbar, iIter, maxValue, CrystalFamily.CUBIC.getNameString(), this.calcEstTime(startTime, iIter, maxValue));
                if (sw.isCancelled())return finalSols;
            }
            if (!error) {
                finalSols.addAll(iterSols); //necessari per afegir altres sistemes cristal·lins
            }
        }
        
        if (this.tetra) {
            log.info("TETRA search");
            List<IndexSolutionDichotomy> iterSols = new ArrayList<IndexSolutionDichotomy>();
            //primera iteracion (es la diferent per cada sistema)
            int[] lengths = new int[] { aVals.length, cVals.length };
            for (int[] indices : new CartesianProduct(lengths)) {
                Cell candidateCell = new Cell(aVals[indices[0]],aVals[indices[0]],cVals[indices[1]],90.0,90.0,90.0,true, CrystalFamily.TETRA);
                if (!this.considerForIndexing(candidateCell)) continue;
                //TODO: A partir d'aqui comú a tots els sistemes, podriem fer un metode que fos checkAndAdd(solucions) que retornes solucions actualitzat
                if (candidateCell.getVol()<this.vMin)continue; //volume only tested on first iteration
                if (candidateCell.getVol()>this.vMax)continue;
                //check sol
                IndexSolutionDichotomy candidateSol = new IndexSolutionDichotomy(candidateCell,currIncPar,0,this); //0 increment a angle (cubic)
                if (candidateSol.areAllQobsInsideHKLQintervals()) {
                    iterSols.add(candidateSol);
                    log.debug("is solution:"+candidateSol.toString());   
                }
                if (sw.isCancelled())return finalSols;
            }
            iIter++;
            this.updateProgressBar(pbar, iIter, maxValue, CrystalFamily.TETRA.getNameString(), this.calcEstTime(startTime, iIter, maxValue));
            log.info("[TETRA] iter 1, sols="+iterSols.size());

            //ara les iteracions amb les solucions que tenim per anar "afinant"  --> practicament identic per tots els sistemes
            boolean error = false;
            for (int i=2;i<=numIter;i++) {
                //primer comprovem si el num de solucions no es molt gran
                if (iterSols.size()>DEF_maxSol) {
                    log.warning("[TETRA] Too many solutions, please reduce cell volume (or parameters) and/or Q error");
                    error=true;
                    iIter = iIter + (numIter-i);
                    break;
                }
                //ara hem de seguir la iteració per cadascuna de les solucions
                List<IndexSolutionDichotomy> iterSols2 = this.runIteration(iterSols);
                //borrem sols i copiem les de sols2
                iterSols.clear();
                iterSols.addAll(iterSols2);

                log.info("[TETRA] iter "+i+", sols="+iterSols.size());
                iIter++;
                this.updateProgressBar(pbar, iIter, maxValue, CrystalFamily.TETRA.getNameString(), this.calcEstTime(startTime, iIter, maxValue));
                if (sw.isCancelled())return finalSols;
            }
            if (!error) {
                finalSols.addAll(iterSols); //necessari per afegir altres sistemes cristal·lins
            }
        }
        
        if (this.hexa) {
            log.info("HEXA search");
            List<IndexSolutionDichotomy> iterSols = new ArrayList<IndexSolutionDichotomy>();
            //primera iteracion (es la diferent per cada sistema)
            int[] lengths = new int[] { aVals.length, cVals.length };
            for (int[] indices : new CartesianProduct(lengths)) {
                Cell candidateCell = new Cell(aVals[indices[0]],aVals[indices[0]],cVals[indices[1]],90.0,90.0,120.0,true, CrystalFamily.HEXA);
                if (!this.considerForIndexing(candidateCell)) continue;
                //TODO: A partir d'aqui comú a tots els sistemes, podriem fer un metode que fos checkAndAdd(solucions) que retornes solucions actualitzat
                if (candidateCell.getVol()<this.vMin)continue; //volume only tested on first iteration
                if (candidateCell.getVol()>this.vMax)continue;
                //check sol
                IndexSolutionDichotomy candidateSol = new IndexSolutionDichotomy(candidateCell,currIncPar,0,this); //0 increment a angle (cubic)
                if (candidateSol.areAllQobsInsideHKLQintervals()) {
                    iterSols.add(candidateSol);
                    log.debug("is solution:"+candidateSol.toString());   
                }
                if (sw.isCancelled())return finalSols;
            }
            iIter++;
            this.updateProgressBar(pbar, iIter, maxValue, CrystalFamily.HEXA.getNameString(), this.calcEstTime(startTime, iIter, maxValue));
            log.info("[HEXA] iter 1, sols="+iterSols.size());

            //ara les iteracions amb les solucions que tenim per anar "afinant"  --> practicament identic per tots els sistemes
            boolean error = false;
            for (int i=2;i<=numIter;i++) {
                //primer comprovem si el num de solucions no es molt gran
                if (iterSols.size()>DEF_maxSol) {
                    log.warning("[HEXA] Too many solutions, please reduce cell volume (or parameters) and/or Q error");
                    error=true;
                    iIter = iIter + (numIter-i);
                    break;
                }
                //ara hem de seguir la iteració per cadascuna de les solucions
                List<IndexSolutionDichotomy> iterSols2 = this.runIteration(iterSols);
                //borrem sols i copiem les de sols2
                iterSols.clear();
                iterSols.addAll(iterSols2);

                log.info("[HEXA] iter "+i+", sols="+iterSols.size());
                iIter++;
                this.updateProgressBar(pbar, iIter, maxValue, CrystalFamily.HEXA.getNameString(), this.calcEstTime(startTime, iIter, maxValue));
                if (sw.isCancelled())return finalSols;
            }
            if (!error) {
                finalSols.addAll(iterSols); //necessari per afegir altres sistemes cristal·lins
            }
        }
        
        if (this.orto) {
            log.info("ORTO search");
//            this.generateIniVals(currIncPar/2.f, currIncAng/2.f); //reduim mida en cas orto (sino massa solucions)
            List<IndexSolutionDichotomy> iterSols = new ArrayList<IndexSolutionDichotomy>();
            //primera iteracion (es la diferent per cada sistema)
            int[] lengths = new int[] { aVals.length, bVals.length, cVals.length };
            for (int[] indices : new CartesianProduct(lengths)) {
                Cell candidateCell = new Cell(aVals[indices[0]],bVals[indices[1]],cVals[indices[2]],90.0,90.0,90.0,true, CrystalFamily.ORTO);
                if (!this.considerForIndexing(candidateCell)) continue;
                //TODO: A partir d'aqui comú a tots els sistemes, podriem fer un metode que fos checkAndAdd(solucions) que retornes solucions actualitzat
                if (candidateCell.getVol()<this.vMin)continue; //volume only tested on first iteration
                if (candidateCell.getVol()>this.vMax)continue;
                //check sol
                IndexSolutionDichotomy candidateSol = new IndexSolutionDichotomy(candidateCell,currIncPar,0,this); //0 increment a angle (cubic)
                if (candidateSol.areAllQobsInsideHKLQintervals()) {
                    iterSols.add(candidateSol);
                    log.debug("is solution:"+candidateSol.toString());   
                }
                if (sw.isCancelled())return finalSols;
            }
            iIter++;
            this.updateProgressBar(pbar, iIter, maxValue, CrystalFamily.ORTO.getNameString(), this.calcEstTime(startTime, iIter, maxValue));
            log.info("[ORTO] iter 1, sols="+iterSols.size());

            //ara les iteracions amb les solucions que tenim per anar "afinant"  --> practicament identic per tots els sistemes
            boolean error = false;
            for (int i=2;i<=numIter;i++) {
                //primer comprovem si el num de solucions no es molt gran
                if (iterSols.size()>DEF_maxSol) {
                    log.warning("[ORTO] Too many solutions, please reduce cell volume (or parameters) and/or Q error");
                    error=true;
                    iIter = iIter + (numIter-i);
                    break;
                }
                //ara hem de seguir la iteració per cadascuna de les solucions
                List<IndexSolutionDichotomy> iterSols2 = this.runIteration(iterSols);
                //borrem sols i copiem les de sols2
                iterSols.clear();
                iterSols.addAll(iterSols2);

                log.info("[ORTO] iter "+i+", sols="+iterSols.size());
                iIter++;
                this.updateProgressBar(pbar, iIter, maxValue, CrystalFamily.ORTO.getNameString(), this.calcEstTime(startTime, iIter, maxValue));
                if (sw.isCancelled())return finalSols;
            }
            if (!error) {
                finalSols.addAll(iterSols); //necessari per afegir altres sistemes cristal·lins
            }
        }
        
        if (this.mono) {
            log.info("MONO search");
//            this.generateIniVals(currIncPar/4.f, currIncAng/4.f); //reduim mida en cas monoclinic
            this.generateIniVals(currIncPar*2, currIncAng*2); //reduim mida en cas monoclinic
            List<IndexSolutionDichotomy> iterSols = new ArrayList<IndexSolutionDichotomy>();
            //primera iteracion (es la diferent per cada sistema)
            int[] lengths = new int[] { aVals.length, bVals.length, cVals.length, beVals.length };
            int ncomb = aVals.length*bVals.length*cVals.length*beVals.length;
            int icomb = 0;
            
            for (int[] indices : new CartesianProduct(lengths)) {
                Cell candidateCell = new Cell(aVals[indices[0]],bVals[indices[1]],cVals[indices[2]],90.0,FastMath.toDegrees(beVals[indices[3]]),90.0,true, CrystalFamily.MONO);
                if (!this.considerForIndexing(candidateCell)) continue;
                //TODO: A partir d'aqui comú a tots els sistemes, podriem fer un metode que fos checkAndAdd(solucions) que retornes solucions actualitzat
                if (candidateCell.getVol()<this.vMin)continue; //volume only tested on first iteration
                if (candidateCell.getVol()>this.vMax)continue;
                //check sol
                IndexSolutionDichotomy candidateSol = new IndexSolutionDichotomy(candidateCell,currIncPar,currIncAng,this); //0 increment a angle (cubic)
                if (candidateSol.areAllQobsInsideHKLQintervals()) {
                    iterSols.add(candidateSol);
                    //debug
                    //                    log.info("is solution:"+candidateSol.toString());
                    //                    candidateSol.Qintervals.sort(null);
                    //                    for (Qinterval q:candidateSol.Qintervals) {
                    //                        log.info(q.printLong());
                    //                    }
                }
                icomb++;
                if (icomb%500==0) {
                    log.infof("comb %d of %d, nsols=%d (last cell %s)",icomb,ncomb,iterSols.size(),candidateCell.toStringCellParamOnly());
                }
                if (sw.isCancelled())return finalSols;
            }
            iIter++;
            this.updateProgressBar(pbar, iIter, maxValue, CrystalFamily.MONO.getNameString(), this.calcEstTime(startTime, iIter, maxValue));
            log.info("[MONO] iter 1, sols="+iterSols.size());

            //ara les iteracions amb les solucions que tenim per anar "afinant"  --> practicament identic per tots els sistemes
            boolean error = false;
            for (int i=2;i<=numIter;i++) {
                //primer comprovem si el num de solucions no es molt gran
                if (iterSols.size()>DEF_maxSol) {
                    log.warning("[MONO] Too many solutions, please reduce cell volume (or parameters) and/or Q error");
                    error=true;
                    iIter = iIter + (numIter-i);
                    break;
                }
                //ara hem de seguir la iteració per cadascuna de les solucions
                List<IndexSolutionDichotomy> iterSols2 = this.runIteration(iterSols);
                //borrem sols i copiem les de sols2
                iterSols.clear();
                iterSols.addAll(iterSols2);

                log.info("[MONO] iter "+i+", sols="+iterSols.size());
                iIter++;
                this.updateProgressBar(pbar, iIter, maxValue, CrystalFamily.MONO.getNameString(), this.calcEstTime(startTime, iIter, maxValue));
                if (sw.isCancelled())return finalSols;
            }
            if (!error) {
                finalSols.addAll(iterSols); //necessari per afegir altres sistemes cristal·lins
            }
        }
        
        if (this.tric) {
            //TODO
        }

        finishProgressBar(pbar,maxValue);

        //minimum FOM
        Iterator<IndexSolutionDichotomy> itrd = finalSols.iterator();
        while (itrd.hasNext()) {
            if(itrd.next().getM20()<this.minFoM)itrd.remove();
        }
        return finalSols;
    }
    
}

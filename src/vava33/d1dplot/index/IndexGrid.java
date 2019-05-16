package com.vava33.d1dplot.index;

/*
 * Implementation of a grid search indexing
 * 
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import javax.swing.JProgressBar;
import org.apache.commons.math3.util.FastMath;

import com.vava33.cellsymm.Cell;
import com.vava33.cellsymm.CellSymm_global.CrystalFamily;
import com.vava33.d1dplot.D1Dplot_global;
import com.vava33.d1dplot.IndexDialog.IndexGridSearchBruteWorker;
import com.vava33.d1dplot.auxi.CartesianProduct;
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.jutils.FileUtils;
import com.vava33.jutils.VavaLogger;

//aqui mandindré XX solucions (e.g. 50), sempre les de menor residuals

public class IndexGrid extends IndexMethod{

    private static final String className = "IndexGrid";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);

    int MAX_SIZE = 51; //faig 51 perquè el +1 es l'element que no compleix, el que pot ser que sigui MOLT pitjor, el de canvi i comprovacio
    protected double aStep,bStep,cStep,alStep,beStep,gaStep;

    //implementació amb treeset or prioritylist
    PriorityQueue<IndexSolutionGrid> sols;

    public IndexGrid(DataSerie ds,  int nPeaksToUse, double deltaQerror, float minfom, int spurious) {
        super(ds,nPeaksToUse,deltaQerror,minfom,spurious); //deltaQerror*5 perque valor defecte es massa petit per indexgrid
        this.sols=new PriorityQueue<IndexSolutionGrid>(MAX_SIZE);
    }

    public void setSteps(double a, double b, double c, double al, double be, double ga, boolean anglesInDegrees) {
        this.aStep=a;
        this.bStep=b;
        this.cStep=c;
        if (anglesInDegrees) {
            this.alStep=FastMath.toRadians(al);
            this.beStep=FastMath.toRadians(be);
            this.gaStep=FastMath.toRadians(ga);
        }else {
            this.alStep=al;
            this.beStep=be;
            this.gaStep=ga;
        }
    }

    public void addSolucio(IndexSolutionGrid currIS) {
        if (sols.size() >= MAX_SIZE) {
            IndexSolutionGrid lastIS = sols.peek();
            if (lastIS.res>currIS.res) {//el nou és millor que la última de la pila ordenada, treiem l'últim element i afegim el nou que s'ordenarà accordingly
                sols.poll();
                sols.add(currIS);
            }//en cas contrari do nothing extra, la última de la pila es millor que la nova (afegim si encara no hi ha max elements...)
        }else {
            System.out.println(sols.size()+"/"+MAX_SIZE);
            sols.add(currIS);
        }
    }

    public List<IndexSolutionGrid> get50bestSolutions(float minM20) {
        List<IndexSolutionGrid> solutions = new ArrayList<IndexSolutionGrid>(50);
        while (!sols.isEmpty()) {
            solutions.add(sols.poll());
        }
        Collections.reverse(solutions);
        if (solutions.size()>50) {
            solutions.remove(solutions.size()-1); //eliminem l'element "+1"		    
        }
        //minimum FOM
        if (minM20>0) {
            Iterator<IndexSolutionGrid> itrd = solutions.iterator();
            while (itrd.hasNext()) {
                if(itrd.next().getM20()<minM20)itrd.remove();
            }
        }
        return  solutions;
    }

    protected void updateProgressBar(JProgressBar pbar, int currValue, int maxValue, String crystsystem, float estTimeMin) {
        pbar.setValue(currValue);
        pbar.setString(String.format("[%s] %d of %d comb (est. time %6.2f min.)",crystsystem,currValue,maxValue,estTimeMin));
    }
    
    private void checksol(Cell candidateCell) {
        if (candidateCell.getVol()>this.vMax)return;
        if (candidateCell.getVol()<this.vMin)return;

        IndexSolutionGrid is = new IndexSolutionGrid(candidateCell,0,this);
        if (is.calcResidual()) {
            this.addSolucio(is);    
        }
    }
    
    public List<IndexSolutionGrid> runIndexing(JProgressBar pbar, IndexGridSearchBruteWorker sw) {

        if (this.cubic) {
            log.info("CUBIC search");
            float[] avals = FileUtils.arange(this.aMin, this.aMax, this.aStep);
            int totalComb = avals.length;
            log.info(String.format("%d combinations of parameters will be evaluated!", totalComb));
            //iniciem percentatge temps pel sistema cristal·lí
            long startTime = System.currentTimeMillis();
            int processedComb = 0;
//            int onePercent = FastMath.max(1, (int)(totalComb/100.f));
            if (pbar!=null) prepareProgressBar(pbar,totalComb);
            
            //bucle candidats
            for (float par:avals) {
                Cell candidateCell = new Cell(par,par,par,90.0,90.0,90.0,true, CrystalFamily.CUBIC);
                //check sol
                this.checksol(candidateCell);
                //temps
//                processedComb=this.calcTimePercent(startTime, processedComb, totalComb, onePercent);
                processedComb++;
                if (pbar!=null) updateProgressBar(pbar,processedComb,totalComb,candidateCell.getCrystalFamily().getNameString(),calcEstTime(startTime, processedComb, totalComb));
                if (sw.isCancelled())return this.get50bestSolutions(minFoM);
            }
        }

        if (this.tetra) { //a=b!=c all 90
            log.info("TETRA search");
            float[] avals = FileUtils.arange(this.aMin, this.aMax, this.aStep);
            float[] cvals = FileUtils.arange(this.cMin, this.cMax, this.cStep);
            int totalComb = avals.length*cvals.length;
            log.info(String.format("%d combinations of parameters will be evaluated! (%d*%d)", totalComb,avals.length,cvals.length));
            long startTime = System.currentTimeMillis();
            int processedComb = 0;
            int onePercent = FastMath.max(1, (int)(totalComb/100.f));

            // faig servir producte cartesia (tot i que amb 2 o 3 parametres encara podria fer manual nested)
            int[] lengths = new int[] { avals.length, cvals.length };

            for (int[] indices : new CartesianProduct(lengths)) {
                Cell candidateCell = new Cell(avals[indices[0]],avals[indices[0]],cvals[indices[1]],90.0,90.0,90.0,true, CrystalFamily.TETRA);
                //check sol
                this.checksol(candidateCell);
                //temps
                processedComb=this.calcTimePercent(startTime, processedComb, totalComb, onePercent);
                if (sw.isCancelled())return this.get50bestSolutions(minFoM);
            }
            
        }
        
        if (this.hexa) { //a=b!=c al=be=90 ga=120
            log.info("HEXA search");
            float[] avals = FileUtils.arange(this.aMin, this.aMax, this.aStep);
            float[] cvals = FileUtils.arange(this.cMin, this.cMax, this.cStep);
            int totalComb = avals.length*cvals.length;
            log.info(String.format("%d combinations of parameters will be evaluated! (%d*%d)", totalComb,avals.length,cvals.length));
            long startTime = System.currentTimeMillis();
            int processedComb = 0;
            int onePercent = FastMath.max(1, (int)(totalComb/100.f));

            // faig servir producte cartesia (tot i que amb 2 o 3 parametres encara podria fer manual nested)
            int[] lengths = new int[] { avals.length, cvals.length };

            for (int[] indices : new CartesianProduct(lengths)) {
                Cell candidateCell = new Cell(avals[indices[0]],avals[indices[0]],cvals[indices[1]],90.0,90.0,120.0,true, CrystalFamily.HEXA);
                //check sol
                this.checksol(candidateCell);
                //temps
                processedComb=this.calcTimePercent(startTime, processedComb, totalComb, onePercent);
                if (sw.isCancelled())return this.get50bestSolutions(minFoM);
            }
        }
        
        if (this.orto) { //a!=b!=c tot 90
            log.info("ORTO search");
            float[] avals = FileUtils.arange(this.aMin, this.aMax, this.aStep);
            float[] bvals = FileUtils.arange(this.bMin, this.bMax, this.bStep);
            float[] cvals = FileUtils.arange(this.cMin, this.cMax, this.cStep);
            int totalComb = avals.length*bvals.length*cvals.length;
            log.info(String.format("%d combinations of parameters will be evaluated! (%d*%d*%d)", totalComb,avals.length,bvals.length,cvals.length));
            long startTime = System.currentTimeMillis();
            int processedComb = 0;
            int onePercent = FastMath.max(1, (int)(totalComb/100.f));

            // faig servir producte cartesia (tot i que amb 2 o 3 parametres encara podria fer manual nested)
            int[] lengths = new int[] { avals.length, bvals.length, cvals.length };

            for (int[] indices : new CartesianProduct(lengths)) {
                Cell candidateCell = new Cell(avals[indices[0]],bvals[indices[1]],cvals[indices[2]],90.0,90.0,90.0,true, CrystalFamily.ORTO);
                //check sol
                this.checksol(candidateCell);
                //temps
                processedComb=this.calcTimePercent(startTime, processedComb, totalComb, onePercent);
                if (sw.isCancelled())return this.get50bestSolutions(minFoM);
            }
        }
        
        if (this.mono) { //a!=b!=c al=ga=90 be!=90
            log.info("MONO search");
            float[] avals = FileUtils.arange(this.aMin, this.aMax, this.aStep);
            float[] bvals = FileUtils.arange(this.bMin, this.bMax, this.bStep);
            float[] cvals = FileUtils.arange(this.cMin, this.cMax, this.cStep);
            float[] bevals = FileUtils.arange(this.beMin,this.beMax,this.beStep);
            int totalComb = avals.length*bvals.length*cvals.length*bevals.length;
            log.info(String.format("%d combinations of parameters will be evaluated! (%d*%d*%d*%d)", totalComb,avals.length,bvals.length,cvals.length,bevals.length));
            long startTime = System.currentTimeMillis();
            int processedComb = 0;
            int onePercent = FastMath.max(1, (int)(totalComb/100.f));

            // faig servir producte cartesia (tot i que amb 2 o 3 parametres encara podria fer manual nested)
            int[] lengths = new int[] { avals.length, bvals.length, cvals.length, bevals.length };

            for (int[] indices : new CartesianProduct(lengths)) {
                Cell candidateCell = new Cell(avals[indices[0]],bvals[indices[1]],cvals[indices[2]],FastMath.PI/2.,bevals[indices[3]],FastMath.PI/2.,false, CrystalFamily.MONO);
                //check sol
                this.checksol(candidateCell);
                //temps
                processedComb=this.calcTimePercent(startTime, processedComb, totalComb, onePercent);
                if (sw.isCancelled())return this.get50bestSolutions(minFoM);
            }
        }
        
        if (this.tric) {
            log.info("TRICLINIC search");
            float[] avals = FileUtils.arange(this.aMin, this.aMax, this.aStep);
            float[] bvals = FileUtils.arange(this.bMin, this.bMax, this.bStep);
            float[] cvals = FileUtils.arange(this.cMin, this.cMax, this.cStep);
            float[] alvals = FileUtils.arange(this.alMin,this.alMax,this.alStep);
            float[] bevals = FileUtils.arange(this.beMin,this.beMax,this.beStep);
            float[] gavals = FileUtils.arange(this.gaMin,this.gaMax,this.gaStep);

            int totalComb = avals.length*bvals.length*cvals.length*alvals.length*bevals.length*gavals.length;
            log.info(String.format("%d combinations of parameters will be evaluated! (%d*%d*%d*%d*%d*%d)", totalComb,avals.length,bvals.length,cvals.length,alvals.length,bevals.length,gavals.length));
            //TODO aqui hauriem de partir problema per fer multithreading cridant la funcio index per cada thread
            //              log.info(String.format("Thread #%d: %d combinations of parameters will be evaluated! (%d*%d*%d*%d*%d*%d)", threadN, totalComb,avals.length,bvals.length,cvals.length,alvals.length,bevals.length,gavals.length));
            int processedComb = 0;
            int onePercent = FastMath.max(1, (int)(totalComb/100.f));
            long startTime = System.currentTimeMillis();
            int[] lengths = new int[] { avals.length, bvals.length, cvals.length, alvals.length, bevals.length, gavals.length };

            for (int[] indices : new CartesianProduct(lengths)) {

                Cell candidateCell = new Cell(avals[indices[0]],bvals[indices[1]],cvals[indices[2]],alvals[indices[3]], bevals[indices[4]],gavals[indices[5]],false, CrystalFamily.TRIC);
                //check sol
                this.checksol(candidateCell);
                //temps
                processedComb=this.calcTimePercent(startTime, processedComb, totalComb, onePercent);
                if (sw.isCancelled())return this.get50bestSolutions(minFoM);
            }

        }
        
        if (pbar!=null) this.finishProgressBar(pbar,-1);
        
        return this.get50bestSolutions(minFoM);
    }



}

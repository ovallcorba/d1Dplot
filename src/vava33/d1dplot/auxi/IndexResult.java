package com.vava33.d1dplot.auxi;

/*
 *
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.TreeSet;

import org.apache.commons.math3.util.FastMath;

//aqui mandindré XX solucions (e.g. 50), sempre les de menor residuals

public class IndexResult {

	int MAX_SIZE = 51; //faig 51 perquè el +1 es l'element que no compleix, el que pot ser que sigui MOLT pitjor, el de canvi i comprovacio
	
	//implementació amb treeset or prioritylist
	PriorityQueue<indexSolucio> sols;
	
    public class indexSolucio implements Comparable<indexSolucio>{
		public float a,b,c,al,be,ga,res;
		public int hmax,hmin,kmax,kmin,lmax,lmin;
		public DataSerie hkls;
		public indexSolucio(float a, float b, float c, float al, float be, float ga, float res) {
			this.a=a;
			this.b=b;
			this.c=c;
			this.al=ga;
			this.be=be;
			this.ga=ga;
			this.res=res;
			hkls = new DataSerie(DataSerie.serieType.hkl);
		}
		
		public void sethklmaxmin(int hmin, int hmax, int kmin, int kmax, int lmin, int lmax) {
			this.hmax=hmax;
			this.hmin=hmin;
			this.kmax=kmax;
			this.kmin=kmin;
			this.lmax=lmax;
			this.lmin=lmin;
			
		}
		
		public void addRef(int h, int k, int l, float dsp, float wave) {
			double t2 = FastMath.toDegrees(PattOps.get2thRadFromDsp(wave, dsp));
			this.hkls.addHKLPoint(new DataHKL(h,k,l,t2));
//			this.hkls.addHKLPoint(new DataHKL(h,k,l,dsp));
		}
		
		@Override
		public String toString() {
			return String.format("%8.4f %8.4f %8.4f %6.2f %6.2f %6.2f res=%8.4f", a,b,c,FastMath.toDegrees(al),FastMath.toDegrees(be),FastMath.toDegrees(ga),res);
		}
		
		@Override
		public int compareTo(indexSolucio o) {
			indexSolucio otherSol = (indexSolucio) o;
			if (this.res>otherSol.res) return -1;
			if (this.res<=otherSol.res) return 1;
			return 0; //it never happens
		}
		
	}

	public IndexResult() {
		this.sols=new PriorityQueue<indexSolucio>(MAX_SIZE);
	}
	
	
	public void addSolucio(indexSolucio currIS) {
		if (sols.size() >= MAX_SIZE) {
			indexSolucio lastIS = sols.peek();
			if (lastIS.res>currIS.res) {//el nou és millor que la última de la pila ordenada, treiem l'últim element i afegim el nou que s'ordenarà accordingly
				sols.poll();
				sols.add(currIS);
			}//en cas contrari do nothing extra, la última de la pila es millor que la nova (afegim si encara no hi ha max elements...)
		}else {
			System.out.println(sols.size()+"/"+MAX_SIZE);
			sols.add(currIS);
		}
	}
	
	public ArrayList<indexSolucio> get50bestSolutions() {
		ArrayList<indexSolucio> solutions = new ArrayList<indexSolucio>(50);
		while (!sols.isEmpty()) {
			solutions.add(sols.poll());
		}
		Collections.reverse(solutions);
		solutions.remove(solutions.size()-1); //eliminem l'element "+1"
		return  solutions;
	}
	
}

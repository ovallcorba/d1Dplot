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
import java.util.List;
import java.util.PriorityQueue;

//aqui mandindré XX solucions (e.g. 50), sempre les de menor residuals

public class IndexGrid {

	int MAX_SIZE = 51; //faig 51 perquè el +1 es l'element que no compleix, el que pot ser que sigui MOLT pitjor, el de canvi i comprovacio
	
	//implementació amb treeset or prioritylist
	PriorityQueue<IndexSolutionGrid> sols;

	public IndexGrid() {
		this.sols=new PriorityQueue<IndexSolutionGrid>(MAX_SIZE);
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
	
	public List<IndexSolutionGrid> get50bestSolutions() {
		List<IndexSolutionGrid> solutions = new ArrayList<IndexSolutionGrid>(50);
		while (!sols.isEmpty()) {
			solutions.add(sols.poll());
		}
		Collections.reverse(solutions);
		solutions.remove(solutions.size()-1); //eliminem l'element "+1"
		return  solutions;
	}
	
}

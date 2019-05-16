package com.vava33.d1dplot.index;

/*
 * Q-interval
 * 
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */


import com.vava33.cellsymm.HKLrefl;

public class Qinterval implements Comparable<Qinterval>{

    float Qlow,Qhigh,Qerr;
    double hkl_qobs; //in case there is a observed hkl inside the interval, its Qobs (if not it will be -1)
    HKLrefl hkl;
    public Qinterval(float qlow,float qhigh,float qerr, HKLrefl hkl) {
        if (qlow<qhigh) {
            this.Qlow=qlow;
            this.Qhigh=qhigh;
        }else {
            this.Qlow=qhigh;
            this.Qhigh=qlow;
        }
        this.Qerr=qerr;
        this.hkl=hkl;
        this.hkl_qobs=-1;
    }
    public boolean intervallContains(double qval) {
        return ((qval >= (Qlow-Qerr)) && (qval <= (Qhigh+Qerr)));
    }
    public String print() {
        return String.format("Qinterval [%.5f,%.5f]",(Qlow-Qerr),(Qhigh+Qerr));
    }
    public String printLong() {
        return String.format("reflection %s with Qcalc=%.5f, %s",hkl.toString(),hkl.calcQvalue(),this.print());
    }

    @Override
    public int compareTo(Qinterval o) {
        if (this.hkl.calcQvalue()<o.hkl.calcQvalue())return -1;
        if (this.hkl.calcQvalue()>o.hkl.calcQvalue())return 1;
        return 0;
    }
    
    //ordenacio
    


}

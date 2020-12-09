package com.vava33.d1dplot.auxi;

/**
 * D1Dplot
 * 
 * Powder diffraction "compound" (i.e. cell + list of reflections)
 * So a compound with crystallographic info
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.vava33.BasicPlotPanel.core.SerieType;
import com.vava33.cellsymm.Cell;
import com.vava33.cellsymm.CellSymm_global;
import com.vava33.cellsymm.HKLrefl;
import com.vava33.d1dplot.data.DataPoint_hkl;
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.d1dplot.data.Xunits;

public class PDCompound implements Comparable<PDCompound>{
    
    private List<String> compName;
    private Cell cella;
    private String formula;
    private String reference;
    private List<String> comment;
    private List<HKLrefl> peaks;
    
    public PDCompound(String name){
        this.compName = new ArrayList<String>();
        this.compName.add(name);
        this.comment = new ArrayList<String>();
        this.peaks = new ArrayList<HKLrefl>();
        this.cella = new Cell(1,1,1,90,90,90,true); //ja posa -1
        formula="";
        reference="";
    }
    
    public PDCompound(String name, float a, float b, float c, float al, float be, float ga, String sg, String elem, List<HKLrefl> pks){
        this(name,a,b,c,al,be,ga,sg,elem);
        this.peaks = pks;
    }
    
    public PDCompound(String name, float a, float b, float c, float al, float be, float ga, String sg, String elem){
        this(name);
        this.cella = new Cell(a,b,c,al,be,ga,true,CellSymm_global.getSpaceGroupByName(sg, false));
        this.formula = elem;
    }
    
    public String toString(){
        String altnames = this.getAltNames();
        if (!altnames.isEmpty()){
            return String.format("%s [%s] (aka: %s)", this.getCompNames().get(0), this.getFormula(), this.getAltNames());    
        }else{
            return String.format("%s [%s]", this.getCompNames().get(0), this.getFormula());
        }
    }
    
    public String toStringNameFormula(){
        return String.format("%s [%s]", this.getCompNames().get(0), this.getFormula());
    }
    
    public String printInfoLine(){
        return String.format("%s: %s (s.g. %s)", this.getCompNames().get(0), this.cella.toStringCellParamOnly(),this.cella.getSg().getName());
    }
    
    public String printInfo2Line(){
        String altnames = this.getAltNames();
        if (!altnames.isEmpty()){
            return String.format("-- %s (aka: %s)\n"
                    + "cell: %s (s.g. %s)", 
                    this.getCompNames().get(0), this.getAltNames(),
                    this.cella.toStringCellParamOnly(),this.cella.getSg().getName());
        }else{
            return String.format("-- %s\n"
                    + "   cell: s (s.g. %s)", 
                    this.getCompNames().get(0),
                    this.cella.toStringCellParamOnly(),this.cella.getSg().getName());
        }
    }
    
    public String printInfoMultipleLines(){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(" %s [%s]\n", this.getCompNames().get(0),this.getFormula()));
        String altnames = this.getAltNames();
        if (!altnames.isEmpty())sb.append(String.format(" Other names: %s\n", altnames));
        sb.append(String.format(" Cell: %s (s.g. %s)\n",  this.cella.toStringCellParamOnly(),this.cella.getSg().getName()));
        if (!this.getReference().isEmpty()) {
            sb.append(String.format(" Reference: %s\n", this.getReference()));
        }   
        String comments = this.getAllComments();
        if (!comments.isEmpty())sb.append(String.format(" Comments: %s\n", comments));
        sb.append(" Reflection list:\n");
        
        sb.append(this.getHKLlines());
        
        return sb.toString();
    }
    
    public String getHKLlines(){
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<this.getPeaks().size();i++){
            int h = this.getPeaks().get(i).getH();
            int k = this.getPeaks().get(i).getK();
            int l = this.getPeaks().get(i).getL();
            double dsp = this.getPeaks().get(i).getDsp();
            double inten = this.getPeaks().get(i).getYcalc();
            sb.append(String.format("%3d %3d %3d %9.5f %7.2f\n",h,k,l,dsp,inten));
          }
          return sb.toString();
    }
    
    public String getCellParameters(){
        return this.cella.toStringCellParamOnly();
    }
    
    public Cell getCella() {
        return this.cella;
    }
    
    public int getNrRefUpToDspacing(double dspacing){
        double tolerance = 0.05;
        Iterator<HKLrefl> itpks = this.getPeaks().iterator();
        int nref = 0;
        while (itpks.hasNext()){
            HKLrefl pk = itpks.next();
            double r = pk.getDsp();
            if (r>=(dspacing-tolerance)){
                nref = nref + 1;
            }
        }
        return nref;
    }
    
    //return the maximum intensity of the first npeaks of the compound
    public double getMaxInten(int npeaks){
        Iterator<HKLrefl> itpks = this.getPeaks().iterator();
        int count = 0;
        double maxI = -1;
        while ((itpks.hasNext()) && (count < npeaks)){
            HKLrefl pk = itpks.next();
            double cI = pk.getYcalc();
            if (cI > maxI){maxI=cI;}
            count = count + 1;
        }
        return maxI;
    }

    public String getCompName() {
        return compName.get(0);
    }
    
    public List<String> getCompNames() {
        return compName;
    }

    public void addCompoundName(String name){
        this.getCompNames().add(name);
    }

    public String getAltNames(){
        StringBuilder sb = new StringBuilder();
        if (this.getCompNames().size()>1){
            int index = 1;
            while (index < getCompNames().size()){
                sb.append(this.getCompNames().get(index));
                sb.append(" ");
                index = index + 1;
            }
        }
        return sb.toString().trim();
    }
    
    public String getAllComments(){
        StringBuilder sb = new StringBuilder();
        if (this.getComment().size()>0){
            int index = 0;
            while (index < getComment().size()){
                sb.append(this.getComment().get(index));
                sb.append(" ");
                index = index + 1;
            }
        }
        return sb.toString().trim();
    }
    
    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public List<HKLrefl> getPeaks() {
        return peaks;
    }

    public void setPeaks(List<HKLrefl> pdref) {
        this.peaks = pdref;
    }
    
    public void addPeak(int h, int k, int l, float dsp, float inten){
        this.getPeaks().add(new HKLrefl(h,k,l,dsp,inten,0));
    }

    public DataSerie getPDCompoundAsREFDataSerie() {
        DataSerie ds = new DataSerie(SerieType.ref,Xunits.dsp, null);
        for (HKLrefl pdr:this.getPeaks()) {
            ds.addPoint(new DataPoint_hkl(pdr.getDsp(),pdr.getYcalc(),0,pdr,ds));
        }
        ds.setName(this.getCompNames().get(0));
        return ds;
    }
    
    private List<Double> getDspacings(){
        List<Double> dsp = new ArrayList<Double>();
        Iterator<HKLrefl> itpks = this.getPeaks().iterator();
        while (itpks.hasNext()){
            HKLrefl ref = itpks.next();
            dsp.add(ref.getDsp());
        }
        return dsp;
    }
    
    @SuppressWarnings("unused")
    private List<Float> getIntensities(){
        List<Float> inten = new ArrayList<Float>();
        Iterator<HKLrefl> itpks = this.getPeaks().iterator();
        while (itpks.hasNext()){
            HKLrefl ref = itpks.next();
            inten.add((float) ref.getYcalc());
        }
        return inten;
    }
    
    public String getDspacingsString(){
        StringBuilder sb = new StringBuilder();
        Iterator<HKLrefl> itpks = this.getPeaks().iterator();
        while (itpks.hasNext()){
            HKLrefl ref = itpks.next();
            sb.append(String.format("%.5f ", ref.getDsp()));
        }
        return sb.toString().trim();
    }
    
    public String getIntensitiesString(){
        StringBuilder sb = new StringBuilder();
        Iterator<HKLrefl> itpks = this.getPeaks().iterator();
        while (itpks.hasNext()){
            HKLrefl ref = itpks.next();
            sb.append(String.format("%.2f ", ref.getYcalc()));
        }
        return sb.toString().trim();
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public List<String> getComment() {
        return comment;
    }
  
    public void addComent(String comment){
        this.getComment().add(comment);
    }
    
    //returns the closest peak of the compound to a given dspacing
    public int closestPeak(double dsp){
        int index=-1;
        try {
            index = Collections.binarySearch(this.getDspacings(),dsp,Collections.reverseOrder());    
        }catch(Exception ex) {
            ex.printStackTrace();
        }
        

        index = Math.abs(index) - 1; //ara apunta al seguent valor, mes petit
        if (index == 0){
            return 0;
        }
        if ((index == this.getPeaks().size())||(index == (this.getPeaks().size()-1))){
            return this.getPeaks().size()-1;
        }
      
        //index -1 sempre > que valor, mentre que index sempre < que valor
        double afterdiff = this.getPeaks().get(index-1).getDsp() - dsp;
        double beforediff = dsp - this.getPeaks().get(index).getDsp();
        if (afterdiff < beforediff){
            return index-1;
        }else{
            return index;
        }
    }

    @Override
    public int compareTo(PDCompound o) {
        float tolDist = 0.01f;
        float tolAng = 0.1f;
        double[] pars = this.cella.getCellParameters(true);
        double[] parso = o.cella.getCellParameters(true);
        if ((pars[0]-parso[0])>tolDist)return 1;
        if ((pars[1]-parso[1])>tolDist)return 1;
        if ((pars[2]-parso[2])>tolDist)return 1;
        if ((pars[3]-parso[3])>tolAng)return 1;
        if ((pars[4]-parso[4])>tolAng)return 1;
        if ((pars[5]-parso[5])>tolAng)return 1;
        if (!(this.compName.get(0).equalsIgnoreCase(o.compName.get(0))))return 1;
        if (!(this.getAltNames().equalsIgnoreCase(o.getAltNames())))return 1;
        if (!(this.cella.getSg().getsgNum()==o.cella.getSg().getsgNum()))return 1;
        if (!this.getDspacingsString().equalsIgnoreCase(o.getDspacingsString()))return 1;
        if (!this.getFormula().equalsIgnoreCase(o.getFormula()))return 1;
        if (!this.getAllComments().equalsIgnoreCase(o.getAllComments()))return 1;
        if (!this.getReference().equalsIgnoreCase(o.getReference()))return 1;
        return 0;
    }

}

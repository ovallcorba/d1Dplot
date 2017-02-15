package vava33.d1dplot.auxi;

/**
 * D1Dplot
 * 
 * Operations with 1D XRD Powder Patterns
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.util.ArrayList;

import javax.swing.table.DefaultTableModel;

import vava33.d1dplot.D1Dplot_global;

import com.vava33.jutils.VavaLogger;

public final class PattOps {
    
    
    private static VavaLogger log = D1Dplot_global.getVavaLogger(PattOps.class.getName());
    
    
    private static DataSerie firstPass(DataSerie ds){  //no depen de N
        DataSerie ds0 = new DataSerie(ds);
        double[] vals = ds.calcYmeanYDesvYmaxYmin(); 
        double Imean = vals[0];
        double Imin = vals[3];
        
        log.debug("Imean= "+Imean+" Imin= "+Imin);
        
        //ara corregim els punts
        for(int i=0; i<ds.getNpoints(); i++){
            double x = ds.getPoint(i).getX();
            double y = ds.getPoint(i).getY();
            
            if(y>(Imean+2*(Imean-Imin))){
                ds0.addPoint(new DataPoint(x,Imean+2*(Imean-Imin),0));
            }else{
                ds0.addPoint(new DataPoint(x,y,0));
            }
        }
        return ds0;
    }
    //normal=true invers==normal=false
    //si multi = true, treure info de defaulttablemodel
    public static DataSerie bruchner(DataSerie ds, int niter, int nveins, boolean edgenormal,boolean multi,DefaultTableModel m) {
        
        //primer fem el pas preliminar (es guarda a PATT[1])
        DataSerie ds0 = firstPass(ds);
        DataSerie ds1 = new DataSerie(ds0);

        for(int p=0;p<niter;p++){
            ds1 = new DataSerie(ds0);
            
            for(int i=0; i<ds0.getNpoints(); i++){
                //remplaçarem cada punt i del diagrama per un de fons, que es la mitja dels +-N veins
                //en cas que tinguem N variable:
                if(multi){
                    double t2punt= ds0.getPoint(i).getX();
                    int rows = m.getRowCount();
                    for(int j=0;j<rows;j++){//per cada fila mirem
                            double Ti = (Double) (m.getValueAt(j, 0));
                            double Tf = (Double) (m.getValueAt(j, 1));
                            if(t2punt>=Ti&&t2punt<=Tf){//si el punt esta al rang
                                nveins = (Integer)(m.getValueAt(j, 2));
                                break; //hem trobat rang, sortim del for
                            }
                    }
                }
                
                double sumI=0;
                for(int j=i-nveins; j<=i+nveins; j++){
                    if(j<=0){
                        //agafem intensitat del primer punt si s'ha triat NORMAL
                        //ORI2: AGAFEM LA COMPLEMENTARIA INVERTIDA si s'ha triat INVERT
                        if(edgenormal){
                            sumI=sumI+ds0.getPoint(0).getY();
                        }else{
                            sumI=sumI+ds0.getPoint(0).getY()+(ds0.getPoint(0).getY()-ds0.getPoint(-j).getY());
                        }
                        continue;

                    }
                    //AQUI FALTA IMPLEMENTAR LA COMPLEMENTARIA INVERTIDA TAMBÉ
                    if(j>=ds0.getNpoints()-1){
                        //agafem intensitat de l'ultim punt
                        sumI=sumI+ds0.getPoint(ds0.getNpoints()-1).getY();
                        continue;
                    }
                    if(j==i){
                        //el propi punt no el considerem
                        continue;
                    }
                    //cas punt "centre" diagrama
                    sumI=sumI+ds0.getPoint(j).getY();
                }

               //CANVI 130313: Comparem Ynew amb diagrama original (Patt[0]) i ens quedem amb la intensitat més petita
                double Ynew=sumI/(2*nveins);
                if(Ynew<ds0.getPoint(i).getY()){
                    //agafem el nou
                    ds1.addPoint(new DataPoint(ds0.getPoint(i).getX(),Ynew,0));
                }else{
                    //ens quedem l'Yobs original
                    ds1.addPoint(new DataPoint(ds0.getPoint(i).getX(),ds0.getPoint(i).getY(),0));
                }
            }
            
            //ARA ABANS D'ENTRAR AL SEGÜENT BUCLE POSEM ds1 com a ds0, ja que es creara un nou ds1
            ds0 = ds1;
        }
        
        //estem al final, retornem la serie final
        return ds1;
    }
    
}

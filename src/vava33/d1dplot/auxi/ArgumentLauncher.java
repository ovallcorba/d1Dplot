package vava33.d1dplot.auxi;

/**
 * D1Dplot
 * 
 * Argument parsing and launcher
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import vava33.d1dplot.D1Dplot_global;
import vava33.d1dplot.D1Dplot_main;

import com.vava33.jutils.ConsoleWritter;
import com.vava33.jutils.VavaLogger;

public final class ArgumentLauncher {
    
    public final static String interactiveCode = "-macro";

    private static boolean launchGraphics = true; //dira si cal mostrar o no el graphical user interface o sortir del programa directament
    
    private static VavaLogger log = D1Dplot_global.getVavaLogger(ArgumentLauncher.class.getName());

    /*
     * -macro com a primer argument implica interactive
     * el segon argument aleshores ha de ser la imatge de treball
     * 
     * 
     * possibilitat macrofile? -macrofile i llegir linia a linia les opcions?
     * 
     * DE MOMENT FUNCIONA PERO CALDRIA ESCRIURE MISSATGES PER CONSOLA.
     * 
     */
    
    public static void readArguments(D1Dplot_main mf, String[] args){
        
        if (args.length==0)return; //no hi ha res
        
        if (args[0].trim().equalsIgnoreCase("-help")){
            ArgumentLauncher.setLaunchGraphics(false); //help per defecte false launch grafics
            ConsoleWritter.stat("");
            ConsoleWritter.stat(" Enter pattern filenames as arguments to open them directly");
            ConsoleWritter.stat("   d1Dplot silicon.dat mydata.xye ...");
            ConsoleWritter.stat("");
            return;
        }
        
        log.debug("args.length= "+args.length);
        
        ArrayList<File> files = new ArrayList<File>();
        for (int i = 0; i < args.length; i++) {
            files.add(new File(args[i]));
            log.debug("args["+i+"]="+args[i]);
//            log.debug("args["+i+"]="+files.get(i).toString());
        }
        
        if (files.isEmpty())return;
        //el workpath sera el del primer fitxer
        D1Dplot_global.setWorkdir(files.get(0));
        log.debug("workdir="+D1Dplot_global.getWorkdir());
        
        Iterator<File> itrF = files.iterator();
        while (itrF.hasNext()){
            File f = itrF.next();
            log.debug("evaluating: "+f.toString());
            if (f.exists()){
                log.debug("it exists!");
                mf.readDataFile(f);    
            }else{
                log.debug("it does not exist!");
            }
        }
        mf.updateData();
    }
    

    public static boolean isLaunchGraphics() {
        return launchGraphics;
    }


    public static void setLaunchGraphics(boolean launchGraphics) {
        ArgumentLauncher.launchGraphics = launchGraphics;
    }
}

package com.vava33.d1dplot.auxi;

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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.vava33.d1dplot.D1Dplot_global;
import com.vava33.d1dplot.D1Dplot_main;
import com.vava33.d1dplot.data.DataPoint;
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.d1dplot.data.Data_Common;
import com.vava33.d1dplot.data.Plottable;
import com.vava33.d1dplot.data.Plottable_point;
import com.vava33.d1dplot.data.Xunits;
import com.vava33.jutils.ConsoleWritter;
import com.vava33.jutils.FileUtils;
import com.vava33.jutils.VavaLogger;

public final class ArgumentLauncher {
    
    public final static String interactiveCode = "-macro";

    private static boolean launchGraphics = true; //dira si cal mostrar o no el graphical user interface o sortir del programa directament
    
    private static final String className = "ArgLauncher";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);

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
    
    //TODO: tabbedPanel_bottom.setSelectedIndex(2); //per evitar que es quedi mostrant el logWindow cal posar 0 despres d'obrir un?
    
    public static void readArguments(D1Dplot_main mf, String[] args){
    	
    	ConsoleWritter.afegirText(true,false,FileUtils.getCharLine('=', 65));
    	ConsoleWritter.afegirSaltLinia();
        ConsoleWritter.afegirText(false, false, "              ");
    	ConsoleWritter.afegirText(true, false, D1Dplot_global.welcomeMSG);
    	ConsoleWritter.afegirText(true,false,FileUtils.getCharLine('=', 65));

        if (args.length==0)return; //no hi ha res
        
        if (args[0].trim().equalsIgnoreCase("-macro")){
            ConsoleWritter.stat("MACRO MODE ON");
            ArgumentLauncher.setLaunchGraphics(false); //macromode per defecte false launch grafics
            ArgumentLauncher.startInteractive(mf,args);
            return;
        }
        
        if (args[0].trim().equalsIgnoreCase("-help")){
            ArgumentLauncher.setLaunchGraphics(false); //help per defecte false launch grafics
            ConsoleWritter.stat("");
            ConsoleWritter.stat(" Enter pattern filenames as arguments to open them directly");
            ConsoleWritter.stat("   d1Dplot silicon.dat mydata.xye ...");
            ConsoleWritter.stat("");
            ConsoleWritter.stat("TWO AVAILABLE OPTIONS FOR COMMAND LINE ARGUMENTS:");
            ConsoleWritter.stat("");
            ConsoleWritter.stat(" a) Entering pattern filenames as arguments will open them directly");
            ConsoleWritter.stat("");
            ConsoleWritter.stat(" b) Entering -macro as 1st argument to enable command line processing mode");
            ConsoleWritter.stat("");
            ConsoleWritter.stat("    Here several files to apply operations can be added as additional arguments");
            ConsoleWritter.stat("");
            ConsoleWritter.stat("    ** Then the following OPERATIONS are available");
            ConsoleWritter.stat("");
            ConsoleWritter.stat("     -conv");
            ConsoleWritter.stat("            Individally convert entered patterns according to the OPTIONS supplied (change format, wavel, etc...)");
            ConsoleWritter.stat("");
            ConsoleWritter.stat("     -sum");
            ConsoleWritter.stat("            Sum the input patterns, additional OPTIONS will be applied on the result");
            ConsoleWritter.stat("");
            ConsoleWritter.stat("     -diff FACT [T2I T2F]");
            ConsoleWritter.stat("            In this case, first pattern on the list will act as background. It will be subtracted to all other files");
            ConsoleWritter.stat("            The operation is: Patt - Fact*Background");
            ConsoleWritter.stat("            Additional options will be applied on the resulting files");
            ConsoleWritter.stat("            If FACT<0 automatic scaling will be performed using the range from T2I to T2F");
            ConsoleWritter.stat("            (T2I and T2F can be supplied only when FACT<0)");
            ConsoleWritter.stat("");
            ConsoleWritter.stat("     -rebin T2I STEP T2F");
            ConsoleWritter.stat("            Applies a rebinning on the input patterns according to T2I STEP T2F");
            ConsoleWritter.stat("            Additional options may be applied on the resulting files");
            ConsoleWritter.stat("");
            ConsoleWritter.stat("    ** That can be combined with the following OPTIONS:");
            ConsoleWritter.stat("");
//            ConsoleWritter.stat("     -suffix SUFFIX");
//            ConsoleWritter.stat("            SUFFIX will be added to the output filenames (before the extension)");
//            ConsoleWritter.stat("");
            ConsoleWritter.stat("     -out NAME");
            ConsoleWritter.stat("            NAME will be added as suffix to the output filenames when batch processing (before the extension),");
            ConsoleWritter.stat("            however, for sum and diff options NAME will be the full output filename (without extension)");
            ConsoleWritter.stat("");
            ConsoleWritter.stat("     -xunits XUNITS");
            ConsoleWritter.stat("            To change the x units of the pattern(s) (XUNITS= 2Theta, d-spacing, 1/dsp, Q)");
            ConsoleWritter.stat("");
            ConsoleWritter.stat("     -format FORMAT");
            ConsoleWritter.stat("            Output format of the pattern(s) (FORMAT= DAT, XYE, ASC, GSA, XRDML, FF)");            
            ConsoleWritter.stat("");
            ConsoleWritter.stat("     -wave WAVELENGTH");
            ConsoleWritter.stat("            Wavelength (A) of the input pattern(s) to be able to perform calculations");            
            ConsoleWritter.stat("");
            ConsoleWritter.stat("     -outwave WAVELENGTH");
            ConsoleWritter.stat("            To change the wavelength of the pattern(s)");   
            ConsoleWritter.stat("");
            
            return;
        }
        
        log.debug("args.length= "+args.length);
        
        List<File> files = new ArrayList<File>();
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
        mf.updateData(true,true);
        mf.showTableTab();
    }
    
    public static void startInteractive(D1Dplot_main mf, String[] args){

        List<String> argL = new ArrayList<String>(Arrays.asList(args)); 
        argL.remove(0); //-macro
        int ifound = -1;
//        
        //FIRST CHECK FOR GENERAL OPTIONS
        
        String suffix = "_new";
        boolean hasName = false;
        boolean sourceXunits = true;
        String xunits = "";
        String outformat = "ALBA";
        double wavel = -1;
        double outwavel = -1;
        
        ifound = getArgLindexOf(argL,"-out");
        if (ifound>=0){
            suffix = argL.get(ifound+1);
            argL.remove(ifound+1);
            argL.remove(ifound);
            hasName = true;
        }
        ConsoleWritter.stat("Output filename (or suffix) is "+suffix);

        ifound = getArgLindexOf(argL,"-xunits");
        if (ifound>=0){
            sourceXunits = false;
            xunits = argL.get(ifound+1);
            ConsoleWritter.stat("Convert X-units to "+xunits);
            argL.remove(ifound+1);
            argL.remove(ifound);
        }
        
        ifound = getArgLindexOf(argL,"-format");
        if (ifound>=0){
            outformat = argL.get(ifound+1);
            ConsoleWritter.stat("Convert file format to "+outformat);
            argL.remove(ifound+1);
            argL.remove(ifound);
        }else{
            ConsoleWritter.stat("Default output file format is "+outformat);
        }
        
        ifound = getArgLindexOf(argL,"-wave");
        if (ifound>=0){
            try{
                wavel = Double.parseDouble(argL.get(ifound+1));  
                ConsoleWritter.stat("Input wavelength is "+Double.toString(wavel));
            }catch(Exception ex){
                ConsoleWritter.stat("Error reading wavelength");
            }
            argL.remove(ifound+1);
            argL.remove(ifound);
        }
        
        ifound = getArgLindexOf(argL,"-outwave");
        if (ifound>=0){
            try{
                outwavel = Double.parseDouble(argL.get(ifound+1));    
                ConsoleWritter.stat("Change wavelength to "+Double.toString(outwavel));
            }catch(Exception ex){
                ConsoleWritter.stat("Error reading out wavelength");
            }
            argL.remove(ifound+1);
            argL.remove(ifound);
        }
        
        //NOW THE OPERATIONS
        
        ifound = getArgLindexOf(argL,"-conv");
        if (ifound>=0){
            argL.remove(ifound);
            for (int i=0;i<argL.size();i++){
                File f = new File(argL.get(i));
                //ConsoleWritter.stat("== Reading file "+argL.get(i));
                ConsoleWritter.stat("== Reading file "+f.toString());
                if (!f.exists()){
                    ConsoleWritter.stat("File not found");
                    continue;
                }
                //Pattern1D p = mf.readDataFile(new File(argL.get(i)));
                Plottable p = mf.readDataFile(f);
                log.debug(p.getFile().toString());
                File outf = FileUtils.canviNomFitxer(new File(p.getFile().getName()), FileUtils.getFNameNoExt(p.getFile().getName())+suffix);
                outf = new File(f.getParent()+D1Dplot_global.fileSeparator+outf.getName());
                ConsoleWritter.stat("== Out file "+outf.toString());
                
                //HEM DE CONVERTIR WAVEL??
                if (outwavel>0){
                    //first check wavelength
                    if (!checkWL(p.getDataSerie(0),wavel)){
                        ConsoleWritter.stat("Original wavelength missing, skipping pattern");
                        continue;
                    }
                    //now we change
                    p.getMainSerie().convertDStoWavelength(outwavel);
//                    p.addDataSerie(0, p.getSerie(0).convertToNewWL(outwavel));
                }
                
                
                //HEM DE CONVERTIR UNITATS??
                if (!sourceXunits){
                    //first check wavelength
                    if (!checkWL(p.getDataSerie(0),wavel)){
                        ConsoleWritter.stat("Wavelength missing, skipping pattern");
                        continue;
                    }
                    
                    //now we can change
//                    DataSerie newDS = changeXunits(p.getSerie(0),xunits);
                    if(Xunits.getEnum(xunits)!=null) {
                        p.getMainSerie().convertDStoXunits(Xunits.getEnum(xunits));
//                    }
//
//                    if (newDS!=null){
//                        p.addDataSerie(0,newDS);
                    }else{
                        ConsoleWritter.stat("Error reading destination X-units, skipping pattern");
                        continue;
                    }
                } //tindrem a serie 0 la nova amb xunits canviades
                
                writePlottableMainSerie(outf,p,outformat);

            }
            
        }
        
        ifound = getArgLindexOf(argL,"-sum");
        if (ifound>=0){
            argL.remove(ifound);
            List<Plottable> patts = new ArrayList<Plottable>();
            StringBuilder sbNames = new StringBuilder();
            
            for (int i=0;i<argL.size();i++){
                File f = new File(argL.get(i));
                ConsoleWritter.stat("== Reading file "+argL.get(i));
                if (!f.exists()){
                    ConsoleWritter.stat("File not found");
                    continue;
                }
                Plottable p =mf.readDataFile(new File(argL.get(i)));
                patts.add(p);
                sbNames.append(p.getDataSerie(0).serieName+" ");
            }
            
            String outfname = FileUtils.getFNameNoExt(patts.get(0).getFile())+suffix;
            if (hasName){
                outfname = suffix;
            }
            
            File outf = FileUtils.canviNomFitxer(patts.get(0).getFile(), outfname);
            DataSerie[] dss = new DataSerie[patts.size()];
            for (int i=0; i<patts.size();i++){
                dss[i]=patts.get(i).getDataSerie(0);
            }

            //comprovar punts, sino rebinning de les series que faci falta, la primera serie mana
            for (int i=1; i<dss.length; i++){
                boolean coin = PattOps.haveCoincidentPointsDS(dss[0], dss[i]);
                if (!coin){
                    dss[i] = PattOps.rebinDS(dss[0], dss[i]);
                }
            }
            
            //sumem
            DataSerie suma = PattOps.addDataSeriesCoincidentPoints(dss);
            suma.serieName=String.format("Sum of %s",sbNames.toString().trim());
            Data_Common patt = new Data_Common(dss[0].getWavelength()); //ja afegeix la serie
            patt.addCommentLines(dss[0].getParent().getCommentLines()); //comments of 1st serie
            patt.addCommentLine(("#Sum of: "+sbNames.toString().trim()));
            suma.setWavelength(patt.getOriginalWavelength());
            patt.addDataSerie(suma);
            //OPERACIONS ADDICIONALS
                
            //HEM DE CONVERTIR WAVEL??
            if (outwavel>0){
                //first check wavelength
                if (!checkWL(patt.getDataSerie(0),wavel)){
                    ConsoleWritter.stat("Original wavelength missing, skipping pattern");
                }
                //now we change
                patt.getMainSerie().convertDStoWavelength(outwavel);
            }


            //HEM DE CONVERTIR UNITATS??
            if (!sourceXunits){
                //first check wavelength
                if (!checkWL(patt.getDataSerie(0),wavel)){
                    ConsoleWritter.stat("Wavelength missing, skipping conversion");
                }

                //now we can change
                if(Xunits.getEnum(xunits)!=null) {
                    patt.getMainSerie().convertDStoXunits(Xunits.getEnum(xunits));
                }else{
                    ConsoleWritter.stat("Error reading destination X-units, skipping conversion");
                    
                }
            } //tindrem a serie 0 la nova amb xunits canviades

            writePlottableMainSerie(outf,patt,outformat);
        }
        
        ifound = getArgLindexOf(argL,"-diff");
        if (ifound>=0){
            float factor = 1.0f;
            double fac_t2i = 0.0f;
            double fac_t2f = 0.0f;
            try{
                factor = Float.parseFloat(argL.get(ifound+1));
                argL.remove(ifound+1);
            }catch(Exception ex){
                ConsoleWritter.stat("No factor found, using 1.0");
            }
            
            if (factor<0) {
                //reading of t2i and t2f (if any)
                try{
                    fac_t2i = Double.parseDouble(argL.get(ifound+1));
                    argL.remove(ifound+1);
                }catch(Exception ex){
                    ConsoleWritter.stat("No factor_t2I found, using 0.0");
                }
                try{
                    fac_t2f = Double.parseDouble(argL.get(ifound+1));
                    argL.remove(ifound+1);
                }catch(Exception ex){
                    ConsoleWritter.stat("No factor_t2F found, using 0.0");
                }
            }
            
            argL.remove(ifound);

            //A PARTIR D'AQUI HI HA UNA LLISTA DE FITXERS, ON EL PRIMER SERA EL FONS i LA RESTA PATTERNS ON SUBSTREURE'L
            
            List<Plottable> patts = new ArrayList<Plottable>();
            StringBuilder sbNames = new StringBuilder();

            for (int i=0;i<argL.size();i++){
                File f = new File(argL.get(i));
                ConsoleWritter.stat("== Reading file "+argL.get(i));
                if (!f.exists()){
                    ConsoleWritter.stat("File not found");
                    continue;
                }
                Plottable p =mf.readDataFile(new File(argL.get(i)));
                patts.add(p);
                sbNames.append(p.getDataSerie(0).serieName+" ");
            }
            
            
            for (int i=1; i<patts.size();i++) {
                String outfname = FileUtils.getFNameNoExt(patts.get(i).getFile().getName())+suffix;
//                if (hasName){
//                    outfname = suffix;
//                }
                File outf = FileUtils.canviNomFitxer(patts.get(i).getFile(), outfname);
                
                DataSerie ds1 = patts.get(i).getDataSerie(0);
                DataSerie ds2 = patts.get(0).getDataSerie(0); //EL FONS!

                if (ds1.getNpoints()!=ds2.getNpoints()){
                    ConsoleWritter.stat("different number of points");
                }
                if (ds1.getPointWithCorrections(0,false).getX()!=ds2.getPointWithCorrections(0,false).getX()){
                    ConsoleWritter.stat("different first point");
                }
                
                DataSerie result = null;
                if (!PattOps.haveCoincidentPointsDS(ds1, ds2)){
                    DataSerie ds2reb = PattOps.rebinDS(ds1, ds2);
                    ConsoleWritter.stat("rebinning performed on serie "+ds2.serieName);
                    //debug
                    ds2reb.serieName=ds2.serieName;
                    result = PattOps.subtractDataSeriesCoincidentPoints(ds1, ds2reb, factor,fac_t2i,fac_t2f);
                }else{
                    result = PattOps.subtractDataSeriesCoincidentPoints(ds1, ds2, factor,fac_t2i,fac_t2f);
                }
                if (result==null){
                    ConsoleWritter.stat("error in subtraction");
                    return;
                }
//                result.setSerieName("subtraction result (factor="+FileUtils.dfX_2.format(factor)+")"); //POSAR UN NOM AMB MES INFO
//                result.setSerieName(String.format("SUB: %s - %.2f*%s",ds1.getSerieName(),factor,ds2.getSerieName())); 
                String s = String.format("#Subtracted pattern: %s - %.2f*%s",ds1.serieName,factor,ds2.serieName);
                result.serieName=s;
                Data_Common patt = new Data_Common(ds1.getWavelength());
//                Pattern1D patt = new Pattern1D();
              patt.addCommentLines(ds1.getParent().getCommentLines());
              patt.addCommentLine(s);
              result.setWavelength(patt.getOriginalWavelength());
              patt.addDataSerie(result);
                
                //OPERACIONS ADDICIONALS
                
                //HEM DE CONVERTIR WAVEL??
                if (outwavel>0){
                    //first check wavelength
                    if (!checkWL(patt.getDataSerie(0),wavel)){
                        ConsoleWritter.stat("Original wavelength missing, skipping pattern");
                    }
                    //now we change
                    patt.getMainSerie().convertDStoWavelength(outwavel);
                }


                //HEM DE CONVERTIR UNITATS??
                if (!sourceXunits){
                    //first check wavelength
                    if (!checkWL(patt.getDataSerie(0),wavel)){
                        ConsoleWritter.stat("Wavelength missing, skipping pattern");
                    }

                    //now we can change
                    if(Xunits.getEnum(xunits)!=null) {
                        patt.getMainSerie().convertDStoXunits(Xunits.getEnum(xunits));
                    }else{
                        ConsoleWritter.stat("Error reading destination X-units, skipping pattern");
                        continue;
                    }
                } //tindrem a serie 0 la nova amb xunits canviades

                writePlottableMainSerie(outf,patt,outformat);
            }
        }
        
        
        ifound = getArgLindexOf(argL,"-rebin");
        if (ifound>=0){
            float t2i = -99.0f;
            float t2f = -99.0f;
            float step = -99.0f;
            try{
                t2i = Float.parseFloat(argL.get(ifound+1));
                step = Float.parseFloat(argL.get(ifound+2));
                t2f = Float.parseFloat(argL.get(ifound+3));
                argL.remove(ifound+3);
                argL.remove(ifound+2);
                argL.remove(ifound+1);
            }catch(Exception ex){
                ConsoleWritter.stat("ERROR reading t2i step t2f, aborting.");
                return;
            }
            argL.remove(ifound);
            
            for (int i=0;i<argL.size();i++){
                File f = new File(argL.get(i));
                ConsoleWritter.stat("== Reading file "+argL.get(i));
                if (!f.exists()){
                    ConsoleWritter.stat("File not found");
                    continue;
                }
                Plottable p = mf.readDataFile(new File(argL.get(i)));

                File outf = FileUtils.canviNomFitxer(p.getFile(), FileUtils.getFNameNoExt(p.getFile())+suffix);

                List<Plottable_point> puntsdummy = new ArrayList<Plottable_point>();
                double t2 = t2i;
                while (t2<=t2f){
                    puntsdummy.add(new DataPoint(t2,0,0));
                    t2 = t2+step;
                }
                DataSerie dummy = new DataSerie(p.getDataSerie(0),puntsdummy,p.getDataSerie(0).getxUnits());
//                dummy.setSeriePoints(puntsdummy);
                
                DataSerie newds = PattOps.rebinDS(dummy, p.getDataSerie(0));
//                p.getMainSerie().copySeriePoints(newds);
                newds.serieName=p.getDataSerie(0).serieName+" (rebinned)";
                Data_Common patt = new Data_Common(p.getOriginalWavelength());
                patt.addCommentLine(String.format("(rebinned to %.5f %.5f %.5f)",t2i,step,t2f));
                newds.setWavelength(p.getDataSerie(0).getWavelength());
                patt.addDataSerie(newds);
//                newds.setSerieName(p.getSerie(0).getSerieName()+" (rebinned)");
//                newds.setPatt1D(p.getSerie(0).getPatt1D());
//                p.getSerie(0).getPatt1D().removeDataSerie(0); //la primera (0) es la NO rebinned
//                p.getCommentLines().add(String.format("(rebinned to %.5f %.5f %.5f)",t2i,step,t2f));
                
                //HEM DE CONVERTIR WAVEL??
                if (outwavel>0){
                    //first check wavelength
                    if (!checkWL(p.getDataSerie(0),wavel)){
                        ConsoleWritter.stat("Original wavelength missing, skipping pattern");
                        continue;
                    }
                    //now we change
                    patt.getMainSerie().convertDStoWavelength(outwavel);
                }
                
                
                //HEM DE CONVERTIR UNITATS??
                if (!sourceXunits){
                    //first check wavelength
                    if (!checkWL(p.getDataSerie(0),wavel)){
                        ConsoleWritter.stat("Wavelength missing, skipping pattern");
                        continue;
                    }
                    
                    //now we can change
                    if(Xunits.getEnum(xunits)!=null) {
                        patt.getMainSerie().convertDStoXunits(Xunits.getEnum(xunits));
                    }else{
                        ConsoleWritter.stat("Error reading destination X-units, skipping pattern");
                        continue;
                    }
                } //tindrem a serie 0 la nova amb xunits canviades
                
                writePlottableMainSerie(outf,patt,outformat);

            }
            
            
        }
//        ifound = getArgLindexOf(argL,"-show");
//        if (ifound>=0){
//            ArgumentLauncher.setLaunchGraphics(true);
//            mf.showMainFrame();
//        }
    }
    
    private static boolean checkWL(DataSerie ds, double argwavel){
        if (ds.getWavelength()<=0){
            if (argwavel <= 0){
                return false;
            }else{
                ds.setWavelength(argwavel);
                return true;
            }
        }
        return true;
    }

    
    private static void writePlottableMainSerie(File outfile, Plottable p, String format){
        
        File out = FileUtils.canviExtensio(outfile,format); //aqui forcem extensio
        File written = DataFileUtils.writePatternFile(out,p.getMainSerie(), true,false);
        
        if (written!=null){
            ConsoleWritter.stat("File written="+out.toString());
        }else{
            ConsoleWritter.stat("Error writting file "+out.toString());
        }
        
    }

    //returns the index of the opt in the array. INGORES CASE. if not found returns -1
    private static int getArgLindexOf(List<String> argL, String opt){
        if (argL==null)return -1;
        if (argL.size()<=0)return -1;
        
        //primer provem tal qual
        if (argL.contains(opt)){
            return argL.indexOf(opt);
        }
        
        if (argL.contains(opt.toLowerCase())){
            return argL.indexOf(opt.toLowerCase());
        }
        if (argL.contains(opt.toUpperCase())){
            return argL.indexOf(opt.toUpperCase());
        }
        
        return -1;
    }
    

    public static boolean isLaunchGraphics() {
        return launchGraphics;
    }


    public static void setLaunchGraphics(boolean launchGraphics) {
        ArgumentLauncher.launchGraphics = launchGraphics;
    }
}

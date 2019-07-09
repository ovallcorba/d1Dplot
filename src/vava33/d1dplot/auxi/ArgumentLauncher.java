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
import java.util.Scanner;

import com.vava33.d1dplot.D1Dplot_global;
import com.vava33.d1dplot.D1Dplot_main;
import com.vava33.d1dplot.auxi.DataFileUtils.SupportedReadExtensions;
import com.vava33.d1dplot.auxi.DataFileUtils.SupportedWriteExtensions;
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
    static D1Dplot_main mf;
    
    private static final String className = "ArgLauncher2";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);

    /*
     * -macro com a primer argument implica interactive
     * 
     * TODO possibilitat macrofile? -macrofile i llegir linia a linia les opcions?
     */
    
    public static void readArguments(D1Dplot_main mainf, String[] args){
    	ArgumentLauncher.mf=mainf;
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
            ConsoleWritter.stat("Enter pattern filenames as arguments to open them directly");
            ConsoleWritter.stat("   d1Dplot silicon.dat mydata.xye ...");
            ConsoleWritter.stat("");
            ConsoleWritter.stat("TWO AVAILABLE OPTIONS FOR COMMAND LINE ARGUMENTS:");
            ConsoleWritter.stat(" a) Entering pattern filenames as arguments will open them directly"); //TODO dir no spaces, no paths
            ConsoleWritter.stat(" b) Entering -macro as 1st argument to enable command line processing mode");
            ConsoleWritter.stat("");
            ConsoleWritter.stat("In (b) after the -macro argument, the following OPERATIONS are available:");
            ConsoleWritter.stat("");
            ConsoleWritter.stat("  -conv   Individally convert entered patterns according to the OPTIONS supplied (change format, wavel, etc...)");
            ConsoleWritter.stat("  -sum    Sum the input patterns, additional OPTIONS will be applied on the result");
            ConsoleWritter.stat("  -diff FACT [T2I T2F]");
            ConsoleWritter.stat("          In this case, first pattern on the list will act as background. It will be subtracted to all other files");
            ConsoleWritter.stat("          The operation is: Patt - Fact*Background");
            ConsoleWritter.stat("          Additional options will be applied on the resulting files");
            ConsoleWritter.stat("          If FACT<0 automatic scaling will be performed using the range from T2I to T2F");
            ConsoleWritter.stat("          (T2I and T2F can be supplied only when FACT<0)");
            ConsoleWritter.stat("  -rebin T2I STEP T2F");
            ConsoleWritter.stat("          Applies a rebinning on the input patterns according to T2I STEP T2F");
            ConsoleWritter.stat("          Additional options may be applied on the resulting files");
            ConsoleWritter.stat("");
            ConsoleWritter.stat("Which can can be combined with the following OPTIONS:");
            ConsoleWritter.stat("");
            ConsoleWritter.stat("  -out NAME    NAME will be added as suffix to the output files when batch processing (before the extension),");
            ConsoleWritter.stat("               For sum and diff options NAME will be the full output filename (without extension)");
            ConsoleWritter.stat("  -xIn XUN     Specify the input x units of the pattern(s) (XUN= 2Theta, d-spacing, 1/dsp2, Q) (def=2Theta)");
            ConsoleWritter.stat("  -xOut XUN    To change the x units of the pattern(s) (XUN= 2Theta, d-spacing, 1/dsp2, Q)");
            ConsoleWritter.stat("  -fmtIn EXT   Specify the input file format of the pattern(s) (EXT= DAT, XYE, GSA, XRDML,...) (def=autodetect)");
            ConsoleWritter.stat("  -fmtOut EXT  Output format of the pattern(s) (EXT= DAT, XYE, GSA, XRDML,...) (def=same as input");
            ConsoleWritter.stat("  -waveIn WL   Wavelength (A) of the input pattern(s) (def= from header if available");
            ConsoleWritter.stat("  -waveOut WL  To change the wavelength of the pattern(s)");
            ConsoleWritter.stat("");
            return;
        }

        log.debug("args.length= "+args.length);
        
        List<File> files = new ArrayList<File>();
        for (int i = 0; i < args.length; i++) {
            files.add(new File(args[i]));
            log.debug("args["+i+"]="+args[i]);
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
        //arraylist opcions
        List<String> argL = new ArrayList<String>(Arrays.asList(args)); 
        argL.remove(0); //-macro
        int ifound = -1;
        
        //FIRST CHECK FOR OPTIONS, AND SET DEFAULTS
        
        String suffix = "_new";
        boolean changeName = false; //is out option found
        
        Xunits inXunits = null;
        Xunits outXunits = null;
        boolean changeXunits = false; //is outXUNITS option found
        
        SupportedReadExtensions inFormat = null;
        SupportedWriteExtensions outFormat = null;
//        boolean changeOutFormat = false; //is outformat option found
        
        double outWave = -1;
        double inWave = -1;
        boolean changeWave = false; //is outwave option found

        ifound = getArgLindexOf(argL,"-out");
        if (ifound>=0){
            suffix = argL.get(ifound+1);
            argL.remove(ifound+1);
            argL.remove(ifound);
            changeName = true;
        }
        
        //fer strings dels missatges que s'escriuran al final per resumir el que es fara??
        String s_name = String.format("%30s = %s","Output filename (or suffix)",suffix);
        String s_unitsIn = String.format("%30s = %s","Input Xunits","autodetect (def=2theta)");
        String s_unitsOut = String.format("%30s = %s","Output Xunits","same as input");
        String s_formatIn = String.format("%30s = %s","Input format","autodetect");
        String s_formatOut = String.format("%30s = %s","Output format","same as input");
        String s_waveIn = String.format("%30s = %s","Input Wavelength","taken from pattern headers (if available) or asked if needed"); 
        String s_waveOut = String.format("%30s = %s","Output Wavelength","same as input");

        ifound = getArgLindexOf(argL,"-xIn");
        if (ifound>=0){
            inXunits = Xunits.getEnum(argL.get(ifound+1));
            if (inXunits==null) {
                log.debug("Error reading input XUNITS");
//                inXunits = Xunits.tth;
            }else {
                s_unitsIn = String.format("%30s = %s","Input Xunits",inXunits.getName());    
            }
            argL.remove(ifound+1);
            argL.remove(ifound);
        }
        
        ifound = getArgLindexOf(argL,"-xOut");
        if (ifound>=0){
            outXunits = Xunits.getEnum(argL.get(ifound+1));
            if (outXunits==null) {
                log.debug("Error reading output XUNITS");
//                outXunits = Xunits.tth;
            }else {
                s_unitsOut = String.format("%30s = %s","Output Xunits",outXunits.getName());
                changeXunits=true;
            }
            argL.remove(ifound+1);
            argL.remove(ifound);
        }
        
        ifound = getArgLindexOf(argL,"-fmtIn");
        if (ifound>=0){
            inFormat = DataFileUtils.getReadExtEnum(argL.get(ifound+1));
            if (inFormat==null) {
                log.debug("Error reading input FORMAT");
//                informat = SupportedReadExtensions.DAT;
            }else {
                s_formatIn = String.format("%30s = %s","Input format",inFormat.name());    
            }
            argL.remove(ifound+1);
            argL.remove(ifound);
        }
        
        ifound = getArgLindexOf(argL,"-fmtOut");
        if (ifound>=0){
            outFormat = DataFileUtils.getWriteExtEnum(argL.get(ifound+1));
            if (outFormat==null) {
                log.debug("Error reading output FORMAT");
//                outformat = SupportedWriteExtensions.DAT;
            }else {
                s_formatOut = String.format("%30s = %s","Output format",outFormat.name());    
            }
            
            argL.remove(ifound+1);
            argL.remove(ifound);
        }
        
        ifound = getArgLindexOf(argL,"-waveIn");
        if (ifound>=0){
            try{
                inWave = Double.parseDouble(argL.get(ifound+1));  
                s_waveIn = String.format("%30s = %.5f","Input Wavelength",inWave);
            }catch(Exception ex){
                log.debug("Error reading inWavelength");
            }
            argL.remove(ifound+1);
            argL.remove(ifound);
        }
        
        ifound = getArgLindexOf(argL,"-waveOut");
        if (ifound>=0){
            try{
                outWave = Double.parseDouble(argL.get(ifound+1));    
                s_waveOut = String.format("%30s = %.5f","Output Wavelength",outWave);
                changeWave = true;
            }catch(Exception ex){
                log.debug("Error reading outWavelength");
            }
            argL.remove(ifound+1);
            argL.remove(ifound);
        }
        
        ConsoleWritter.stat(s_name);
        ConsoleWritter.stat(s_unitsIn);
        ConsoleWritter.stat(s_unitsOut);
        ConsoleWritter.stat(s_formatIn);
        ConsoleWritter.stat(s_formatOut);
        ConsoleWritter.stat(s_waveIn);
        ConsoleWritter.stat(s_waveOut);
        
        //check which operation will be performed
        boolean doconv = false;
        boolean dosum = false;
        boolean dodiff = false;
        float factor = 1.0f;
        double fac_t2i = 0.0f;
        double fac_t2f = 0.0f;
        boolean dorebin = false;
        float t2i = -99.0f;
        float t2f = -99.0f;
        float step = -99.0f;
        
        ifound = getArgLindexOf(argL,"-conv");
        if (ifound>=0){
            argL.remove(ifound);
            doconv = true;
        }
        
        ifound = getArgLindexOf(argL,"-sum");
        if (ifound>=0){
            argL.remove(ifound);
            dosum = true;
        }
        
        ifound = getArgLindexOf(argL,"-diff");
        if (ifound>=0){
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
            dodiff=true;
        }
        
        ifound = getArgLindexOf(argL,"-rebin");
        if (ifound>=0){
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
            dorebin=true;
        }
        
        //now we get the list of files
        ArrayList<File> fitxers = new ArrayList<File>();
        for (int i=0;i<argL.size();i++) {
            File f = new File(argL.get(i));
            if (f.exists()) {
                fitxers.add(f);
                log.debug("added: "+f.getAbsolutePath());
                ConsoleWritter.statf("File found: %s", f.getAbsolutePath());
            }else {
                ConsoleWritter.statf("File NOT found (please check path): %s", f.getAbsolutePath());
            }
        }
        
        if (doconv) { 
            ConsoleWritter.stat("--> CONVERSION OPT");
            for (File f:fitxers) {
                Plottable p = readPattern(f,inFormat,inXunits);
                if (p==null) {
                    ConsoleWritter.statf("== Error reading %s, skipping...",f.getAbsolutePath());
                    continue;
                }
                
                ConsoleWritter.stat("== Converting file "+f.getAbsolutePath());
                File outf = FileUtils.canviNomFitxer(new File(p.getFile().getName()), FileUtils.getFNameNoExt(p.getFile().getName())+suffix);
                outf = new File(f.getParent()+D1Dplot_global.fileSeparator+outf.getName());
                
                //operacions addicionals
                if (changeWave) p = changeWavelength(p,inWave,outWave); 
                if (changeXunits) p = changeXunits(p,inWave,outXunits);

                writePlottableMainSerie(outf,p,guessOutFormat(outFormat,inFormat,f));
            }
        }
        
        if (dosum) {
            ConsoleWritter.stat("--> SUM OPT");
            List<Plottable> patts = new ArrayList<Plottable>();
            StringBuilder sbNames = new StringBuilder();
            
            for (File f:fitxers) {
                Plottable p = readPattern(f,inFormat,inXunits);
                if (p==null) {
                    ConsoleWritter.statf("== Error reading %s, skipping...",f.getAbsolutePath());
                    continue;
                }
                patts.add(p);
                ConsoleWritter.statf("== File %s readed!",f.getAbsolutePath());
            }
            
            String outfname = FileUtils.getFNameNoExt(patts.get(0).getFile())+suffix;
            if (changeName){ //es nom sencer aqui
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
                    ConsoleWritter.statf("Rebinning required for %s",dss[i].getParent().getFile().getParent());
                    dss[i] = PattOps.rebinDS(dss[0], dss[i]);
                }
            }
            
            //sumem
            DataSerie suma = PattOps.addDataSeriesCoincidentPoints(dss);
            suma.serieName=String.format("Sum of %s",sbNames.toString().trim());
            Plottable patt = new Data_Common(dss[0].getWavelength()); //ja afegeix la serie
            patt.addCommentLines(dss[0].getParent().getCommentLines()); //comments of 1st serie
            patt.addCommentLine(("#Sum of: "+sbNames.toString().trim()));
            suma.setWavelength(patt.getOriginalWavelength());
            patt.addDataSerie(suma);

            //operacions addicionals
            if (changeWave) patt = changeWavelength(patt,inWave,outWave); 
            if (changeXunits) patt = changeXunits(patt,inWave,outXunits);
            
            writePlottableMainSerie(outf,patt,guessOutFormat(outFormat,inFormat,dss[0].getParent().getFile()));
        }

        if (dodiff) {
            ConsoleWritter.stat("--> DIFF OPT");
            Plottable fons = readPattern(fitxers.get(0),inFormat,inXunits);
            if (fons==null) {
                ConsoleWritter.stat("Error reading background file. Aborting...");
                return;
            }
            ConsoleWritter.statf("== Background file %s",fitxers.get(0).getAbsolutePath());
            fitxers.remove(0); //treiem el fons, ja esta llegit
            
            for (File f:fitxers) {
                Plottable p = readPattern(f,inFormat,inXunits);
                if (p==null) {
                    ConsoleWritter.statf("Error reading %s, skipping...",f.getAbsolutePath());
                    continue;
                }
                ConsoleWritter.statf("== Subtracting background to %s",f.getAbsolutePath());
                String outfname = FileUtils.getFNameNoExt(p.getFile().getName())+suffix;
                File outf = FileUtils.canviNomFitxer(p.getFile(), outfname);
                
                DataSerie ds1 = p.getDataSerie(0);
                DataSerie ds2 = fons.getDataSerie(0); //EL FONS!
                
                if (ds1.getNpoints()!=ds2.getNpoints()){
                    ConsoleWritter.stat("Different number of points");
                }
                if (ds1.getPointWithCorrections(0,false).getX()!=ds2.getPointWithCorrections(0,false).getX()){
                    ConsoleWritter.stat("Different first point");
                }
                
                DataSerie result = null;
                if (!PattOps.haveCoincidentPointsDS(ds1, ds2)){
                    DataSerie ds2reb = PattOps.rebinDS(ds1, ds2);
                    ConsoleWritter.stat("Rebinning performed on serie "+ds2.serieName);
                    ds2reb.serieName=ds2.serieName;
                    result = PattOps.subtractDataSeriesCoincidentPoints(ds1, ds2reb, factor,fac_t2i,fac_t2f);
                }else{
                    result = PattOps.subtractDataSeriesCoincidentPoints(ds1, ds2, factor,fac_t2i,fac_t2f);
                }
                if (result==null){
                    ConsoleWritter.stat("Error in subtraction, skipping...");
                    continue;
                }

                String s = String.format("#Subtracted pattern: %s - %.2f*%s",ds1.serieName,factor,ds2.serieName);
                result.serieName=s;
                Plottable patt = new Data_Common(ds1.getWavelength());
                patt.addCommentLines(ds1.getParent().getCommentLines());
                patt.addCommentLine(s);
                result.setWavelength(patt.getOriginalWavelength());
                patt.addDataSerie(result);

                //operacions addicionals
                if (changeWave) patt = changeWavelength(patt,inWave,outWave); 
                if (changeXunits) patt = changeXunits(patt,inWave,outXunits);
                
                writePlottableMainSerie(outf,patt,guessOutFormat(outFormat,inFormat,f));
            }
        }

        if (dorebin) {
            ConsoleWritter.stat("--> REBIN OPT");
            for (File f:fitxers) {
                Plottable p = readPattern(f,inFormat,inXunits);
                if (p==null) {
                    ConsoleWritter.statf("Error reading %s, skipping...",f.getAbsolutePath());
                    continue;
                }
                ConsoleWritter.statf("== Rebinning %s",f.getAbsolutePath());
                String outfname = FileUtils.getFNameNoExt(p.getFile().getName())+suffix;
                File outf = FileUtils.canviNomFitxer(p.getFile(), outfname);
                
                List<Plottable_point> puntsdummy = new ArrayList<Plottable_point>();
                double t2 = t2i;
                while (t2<=t2f){
                    puntsdummy.add(new DataPoint(t2,0,0));
                    t2 = t2+step;
                }
                DataSerie dummy = new DataSerie(p.getDataSerie(0),puntsdummy,p.getDataSerie(0).getxUnits());                
                DataSerie newds = PattOps.rebinDS(dummy, p.getDataSerie(0));
                newds.serieName=p.getDataSerie(0).serieName+" (rebinned)";
                Plottable patt = new Data_Common(p.getOriginalWavelength());
                patt.addCommentLine(String.format("(rebinned to %.5f %.5f %.5f)",t2i,step,t2f));
                newds.setWavelength(p.getDataSerie(0).getWavelength());
                patt.addDataSerie(newds);

              //OPERACIONS ADDICIONALS
                //operacions addicionals
                if (changeWave) patt = changeWavelength(patt,inWave,outWave); 
                if (changeXunits) patt = changeXunits(patt,inWave,outXunits);

                writePlottableMainSerie(outf,patt,guessOutFormat(outFormat,inFormat,f));
                
            }
        }
    }
    
    private static Plottable changeXunits(Plottable p, double inWave, Xunits outXunits) {
        if (!checkWL(p.getDataSerie(0),inWave)){
            ConsoleWritter.stat("Wavelength missing, skipping change of X units");
        }else {
            p.getMainSerie().convertDStoXunits(outXunits);
            ConsoleWritter.stat("Change of X units... done!");
        }
        //tindrem a serie 0 la nova amb xunits canviades
        return p;
    }
    
    private static Plottable changeWavelength(Plottable p, double inWave, double outWave) {
        if (!checkWL(p.getDataSerie(0),inWave)){
            ConsoleWritter.stat("Original wavelength missing, skipping wavelength conversion");
        }else {
            p.getMainSerie().convertDStoWavelength(outWave);
            ConsoleWritter.stat("Wavelenght conversion... done!");
        }
        //tindrem a serie 0 la nova amb xunits canviades
        return p;
    }
    
    private static Plottable readPattern(File f, SupportedReadExtensions inFormat, Xunits inXunits) {
        Plottable p = null;
        if (inFormat==null) {
            p = mf.readDataFile(f);
        }else {
            p = mf.readDataFile(f,inFormat);
        }
        if ((p!=null)&&(inXunits!=null)) {
            //we force xunits
            p.getMainSerie().setxUnits(inXunits);
        }
        return p;
    }
    
    
  //DONE: FER UN METODE A PART QUE DETERMINI L'OUTFORMAT (perque el cridarem per cada pattern si no s'ha definit un outformat)
    //-- 1) mirar si hi ha definit un informat i correspon a un supportedWrite
    //-- 2) si no hi ha informat o no s'ha trobat supported write mirar extensio entrada i mirar si correspon a un supportedWrite
    //-- 3) escriure DAT alba
    public static SupportedWriteExtensions guessOutFormat(SupportedWriteExtensions outfmt, SupportedReadExtensions infmt, File infile) {
        if (outfmt!=null)return outfmt;
        SupportedWriteExtensions ext = null;
        if (infmt!=null) ext = DataFileUtils.getWriteExtEnum(infmt.name());
        if (ext!=null)return ext;
        ext = DataFileUtils.getWriteExtEnum(FileUtils.getExtension(infile));
        if (ext!=null)return ext;
        return SupportedWriteExtensions.DAT;
    }
    
    private static double waveManualInput = -1;
    private static boolean checkWL(DataSerie ds, double argwavel){
        if (ds.getWavelength()<=0){
            if (argwavel <= 0){
                // opcio d'entrar-la amb dialog
                if (waveManualInput<0) {
                    ConsoleWritter.stat("Wavelength missing, please enter the input wavelength (it will be used for all missing ones)");
                    Scanner scanner = new Scanner(System.in);
                    ConsoleWritter.afegirText(false, true, "wavelength (A)= ");
                    String swave = scanner.next();
                    scanner.close();
                    try {
                        waveManualInput = Double.parseDouble(swave);
                    }catch(Exception ex) {
                        ConsoleWritter.stat("Error reading wavelength");
                        return false;
                    }
                    ds.setWavelength(waveManualInput);
                    return true;
                    
                }else {
                    ds.setWavelength(waveManualInput);
                }
                
                return false;
            }else{
                ds.setWavelength(argwavel);
                return true;
            }
        }
        return true;
    }

    private static void writePlottableMainSerie(File outfile, Plottable p, SupportedWriteExtensions format){
        File out = FileUtils.canviExtensio(outfile,format.name()); //aqui forcem extensio
        File written = DataFileUtils.writePatternFile(out,p.getMainSerie(),format, true,false);
        
        if (written!=null){
            ConsoleWritter.stat("Output file written "+out.toString());
        }else{
            ConsoleWritter.stat("Error writting output file "+out.toString());
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
package vava33.d1dplot;

/**
 * D1Dplot
 * 
 * Global parameters
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Scanner;

import vava33.d1dplot.auxi.DataSerie;
import vava33.d1dplot.auxi.Pattern1D;

import com.vava33.jutils.FileUtils;
import com.vava33.jutils.VavaLogger;

public final class D1Dplot_global {

    public static final int satur32 = Short.MAX_VALUE;
    public static final int satur65 = (Short.MAX_VALUE * 2) + 1;

    public static final String welcomeMSG = "d1Dplot v1702 (170217) by O.Vallcorba";
    public static final String separator = System.getProperty("file.separator");
    public static final String userDir = System.getProperty("user.dir");
    public static final String configFilePath = System.getProperty("user.dir") + separator + "d1dconfig.cfg";
    public static final String usersGuidePath = System.getProperty("user.dir") + separator + "d1Dplot_userguide.pdf";
    public static Boolean configFileReaded = null; //true=readed false=errorReading null=notFound

    //symbols and characters
    public static final String theta = "\u03B8";
    public static final String angstrom= "\u212B";
    public static final String beta= "\u03B2";
    public static final SimpleDateFormat fHora = new SimpleDateFormat("yyyy-MM-dd");

    public static VavaLogger log;
    
    private static final boolean overrideLogLevelConfigFile = false;
//    public static final boolean isdebug = false; // EN CAS QUE HAGI D'ACTIVAR COSES EXTRES (per mi basicament)

    //PARAMETRES QUE ES PODEN CANVIAR A LES OPCIONS =======================================
    //global 
    public static boolean logging = false;
    public static String loglevel = "info"; //info, config, etc...
    public static String workdir = System.getProperty("user.dir");
    private static Integer def_Width=768;
    private static Integer def_Height=1024;
    
    //PlotPanel
    private static Boolean lightTheme;  //"temes" de colors: DARK, LIGHT
    private static Integer gapAxisTop;
    private static Integer gapAxisBottom;
    private static Integer gapAxisRight;
    private static Integer gapAxisLeft;
    private static Integer padding;
    private static Integer AxisLabelsPadding;
    private static Double incXPrimPIXELS;  //per autodivlines
    private static Double incXSecPIXELS;
    private static Double incYPrimPIXELS;
    private static Double incYSecPIXELS;
    private static Double facZoom;
    private static Integer MOURE;
    private static Integer CLICAR;
    private static Integer ZOOM_BORRAR;
    private static Integer div_PrimPixSize;
    private static Integer div_SecPixSize;
    private static Boolean verticalYlabel;
//    private static String xlabel = "2"+D1Dplot_global.theta+" (º)";
//    private static String ylabel = "Intensity";
    
    //pattern1D and dataserie
    private static Integer hkloff;
    private static Integer hklticksize;
    private static Boolean prfFullprofColors;
    
    private static Float def_markerSize;
    private static Float def_lineWidth;

    //==========================================================================
    
    public static void initLogger(String name){
        log = new VavaLogger(name);
        log.enableLogger(logging);
        log.setLogLevel(loglevel);
    }
    
    public static VavaLogger getVavaLogger(String name){
        VavaLogger l = new VavaLogger(name);
        l.setLogLevel(loglevel);
        l.enableLogger(logging);
        return l;
    }
    
    public static String getWorkdir() {
        return workdir;
    }

    public static void setWorkdir(String workdir) {
        D1Dplot_global.workdir = workdir;
    }
    
    public static void setWorkdir(File workDirOrFile) {
        D1Dplot_global.workdir = workDirOrFile.getParent();
    }
    
    public static Color parseColorName(String name){
        if (FileUtils.containsIgnoreCase(name, "black")) return Color.black;
        if (FileUtils.containsIgnoreCase(name, "green")) return Color.green;
        if (FileUtils.containsIgnoreCase(name, "red")) return Color.red;
        if (FileUtils.containsIgnoreCase(name, "cyan")) return Color.cyan;
        if (FileUtils.containsIgnoreCase(name, "yellow")) return Color.yellow;
        if (FileUtils.containsIgnoreCase(name, "magenta")) return Color.magenta;
        if (FileUtils.containsIgnoreCase(name, "orange")) return Color.orange;
        if (FileUtils.containsIgnoreCase(name, "pink")) return Color.pink;
        if (FileUtils.containsIgnoreCase(name, "blue")) return Color.blue;
        if (FileUtils.containsIgnoreCase(name, "white")) return Color.white;
        return null;
    }
    
    public static String[] lightColors = {"black","blue","red","green","magenta","cyan","pink","yellow"}; //8 colors
    public static String[] DarkColors = {"yellow","white","cyan","green","magenta","blue","red","pink"}; //8 colors
    
    public static boolean isLightTheme() {
        return lightTheme;
    }

    public static void setLightTheme(boolean lightTheme) {
        D1Dplot_global.lightTheme = lightTheme;
    }

    private static Float parseFloat(String value){
        Float f=null;
        try{
            f = Float.parseFloat(value);
        }catch(Exception e){
            log.config("error parsing float "+value);
        }
        return f;
    }
    
    private static Double parseDouble(String value){
        Double d=null;
        try{
            d = Double.parseDouble(value);
        }catch(Exception e){
            log.config("error parsing double "+value);
        }
        return d;
    }
    
    private static Boolean parseBoolean(String value){
        Boolean b=null;
        if (FileUtils.containsIgnoreCase(value, "false")||FileUtils.containsIgnoreCase(value, "no")||FileUtils.containsIgnoreCase(value, "f")){
            b = false;
        }
        if (FileUtils.containsIgnoreCase(value, "true")||FileUtils.containsIgnoreCase(value, "yes")||FileUtils.containsIgnoreCase(value, "t")){
            b = true;
        }
        return b;
    }
    
    private static Integer parseInteger(String value){
        Integer i=null;
        try{
            i = Integer.parseInt(value);
        }catch(Exception e){
            log.config("error parsing integer "+value);
        }
        return i;
    }
    
    //returns true if logging is enabled and level is <= config
    public static boolean isDebug(){
        if (logging){
            if (loglevel.equalsIgnoreCase("config")||loglevel.equalsIgnoreCase("debug")||loglevel.equalsIgnoreCase("fine")||loglevel.equalsIgnoreCase("finest")){
                return true;
            }
        }
        return false;
    }
    
    public static Boolean getConfigFileReaded() {
        return configFileReaded;
    }

    public static void setConfigFileReaded(Boolean configFileReaded) {
        D1Dplot_global.configFileReaded = configFileReaded;
    }

    //aqui els inicialitzem a NULL
    public static void initEarlyPars(){
        //init logger during reading pars in config mode
        initLogger(D1Dplot_global.class.getName()); //during the par reading
        
        //global
        workdir = System.getProperty("user.dir");
        def_Width=null;
        def_Height=null;
        
        //plotpanel
        lightTheme = null;
        gapAxisTop = null;
        gapAxisBottom = null;
        gapAxisRight = null;
        gapAxisLeft = null;
        padding = null;
        AxisLabelsPadding = null;
        incXPrimPIXELS = null;
        incXSecPIXELS = null;
        incYPrimPIXELS = null;
        incYSecPIXELS = null;
        facZoom = null;
        MOURE = null;
        CLICAR = null;
        ZOOM_BORRAR = null;
        div_PrimPixSize = null;
        div_SecPixSize = null;
        verticalYlabel = null;

        //pattern1d
        hkloff = null;
        hklticksize = null;
        def_markerSize = null;
        def_lineWidth = null;
    }
    
    //els que no s'hagin establert per opcions (fitxer) es posaran per defecte aquí
    public static void initPars(){
        //from main
        if (def_Width == null){
            def_Width = D1Dplot_main.getDef_Width();
        }else{
            D1Dplot_main.setDef_Width(def_Width.intValue());
        }
        if (def_Height == null){
            def_Height = D1Dplot_main.getDef_Height();
        }else{
            D1Dplot_main.setDef_Height(def_Height.intValue());
        }
        
        //from PlotPanel
        if (lightTheme == null){
            lightTheme = PlotPanel.isLightTheme();
        }else{
            PlotPanel.setLightTheme(lightTheme.booleanValue());
        }
        if (gapAxisTop == null){
            gapAxisTop = PlotPanel.getGapAxisTop();
        }else{
            PlotPanel.setGapAxisTop(gapAxisTop.intValue());
        }
        if (gapAxisBottom == null){
            gapAxisBottom = PlotPanel.getGapAxisBottom();
        }else{
            PlotPanel.setGapAxisBottom(gapAxisBottom.intValue());
        }
        if (gapAxisRight == null){
            gapAxisRight = PlotPanel.getGapAxisRight();
        }else{
            PlotPanel.setGapAxisRight(gapAxisRight.intValue());
        }
        if (gapAxisLeft == null){
            gapAxisLeft = PlotPanel.getGapAxisLeft();
        }else{
            PlotPanel.setGapAxisLeft(gapAxisLeft.intValue());
        }
        if (padding == null){
            padding = PlotPanel.getPadding();
        }else{
            PlotPanel.setPadding(padding.intValue());
        }
        if (AxisLabelsPadding == null){
            AxisLabelsPadding = PlotPanel.getAxisLabelsPadding();
        }else{
            PlotPanel.setAxisLabelsPadding(AxisLabelsPadding.intValue());
        }
        if (incXPrimPIXELS == null){
            incXPrimPIXELS = PlotPanel.getIncXPrimPIXELS();
        }else{
            PlotPanel.setIncXPrimPIXELS(incXPrimPIXELS.doubleValue());
        }
        if (incXSecPIXELS == null){
            incXSecPIXELS = PlotPanel.getIncXSecPIXELS();
        }else{
            PlotPanel.setIncXSecPIXELS(incXSecPIXELS.doubleValue());
        }
        if (incYPrimPIXELS == null){
            incYPrimPIXELS = PlotPanel.getIncYPrimPIXELS();
        }else{
            PlotPanel.setIncYPrimPIXELS(incYPrimPIXELS.doubleValue());
        }
        if (incYSecPIXELS == null){
            incYSecPIXELS = PlotPanel.getIncYSecPIXELS();
        }else{
            PlotPanel.setIncYSecPIXELS(incYSecPIXELS.doubleValue());
        }
        if (facZoom == null){
            facZoom = PlotPanel.getFacZoom();
        }else{
            PlotPanel.setFacZoom(facZoom.doubleValue());
        }
        if (MOURE == null){
            MOURE = PlotPanel.getMOURE();
        }else{
            PlotPanel.setMOURE(MOURE.intValue());
        }
        if (CLICAR == null){
            CLICAR = PlotPanel.getCLICAR();
        }else{
            PlotPanel.setCLICAR(CLICAR.intValue());
        }
        if (ZOOM_BORRAR == null){
            ZOOM_BORRAR = PlotPanel.getZOOM_BORRAR();
        }else{
            PlotPanel.setZOOM_BORRAR(ZOOM_BORRAR.intValue());
        }
        if (div_PrimPixSize == null){
            div_PrimPixSize = PlotPanel.getDiv_PrimPixSize();
        }else{
            PlotPanel.setDiv_PrimPixSize(div_PrimPixSize.intValue());
        }
        if (div_SecPixSize == null){
            div_SecPixSize = PlotPanel.getDiv_SecPixSize();
        }else{
            PlotPanel.setDiv_SecPixSize(div_SecPixSize.intValue());
        }
        if (verticalYlabel == null){
            verticalYlabel = PlotPanel.isVerticalYlabel();
        }else{
            PlotPanel.setVerticalYlabel(verticalYlabel.booleanValue());
        }
        
        //PATTERN1D
        if (hkloff == null){
            hkloff = Pattern1D.getHkloff();
        }else{
            Pattern1D.setHkloff(hkloff.intValue());
        }
        if (hklticksize == null){
            hklticksize = Pattern1D.getHklticksize();
        }else{
            Pattern1D.setHklticksize(hklticksize.intValue());
        }
        if (prfFullprofColors == null){
            prfFullprofColors = Pattern1D.isPrfFullprofColors();
        }else{
            Pattern1D.setPrfFullprofColors(prfFullprofColors.booleanValue());
        }
        if (def_markerSize == null){
            def_markerSize = DataSerie.getDef_markerSize();
        }else{
            DataSerie.setDef_markerSize(def_markerSize.floatValue());
        }
        if (def_lineWidth == null){
            def_lineWidth = DataSerie.getDef_lineWidth();
        }else{
            DataSerie.setDef_lineWidth(def_lineWidth.floatValue());
        }
    }
    
    
    //POSEM SEMPRE LA COMPROVACIO DE SI EL VALOR PARSEJAT ES NULL PERQUÈ EN AQUEST CAS FEM SERVIR EL QUE VE DE INITPARS
    // QUE PODRIA NO SER NULL!!!   //added parameters (they already have the default value)
    public static boolean readParFile(){
        initEarlyPars();
        File confFile = new File(configFilePath);
        if (!confFile.exists()){
            return false;
        }
        try {
            Scanner scParFile = new Scanner(confFile);
            
            while (scParFile.hasNextLine()){
                String line = scParFile.nextLine();
                if (line.trim().startsWith("#"))continue;
                int iigual=line.indexOf("=")+1;
                if (iigual<0)continue;
                
                if(!overrideLogLevelConfigFile){
                    if (FileUtils.containsIgnoreCase(line, "logging")){
                        String logstr = (line.substring(iigual, line.trim().length()).trim());
                        Boolean bvalue = parseBoolean(logstr);
                        if(bvalue!=null)logging = bvalue.booleanValue();
                    }
                    if (FileUtils.containsIgnoreCase(line, "loglevel")){
                        String loglvl = (line.substring(iigual, line.trim().length()).trim());
                        if (FileUtils.containsIgnoreCase(loglvl, "debug")||FileUtils.containsIgnoreCase(loglvl, "config"))loglevel = "config";
                        if (FileUtils.containsIgnoreCase(loglvl, "fine"))loglevel = "fine";
                        if (FileUtils.containsIgnoreCase(loglvl, "warning"))loglevel = "warning";
                        if (FileUtils.containsIgnoreCase(loglvl, "info"))loglevel = "info";
                    }
                }
                
                if (FileUtils.containsIgnoreCase(line, "IniWidth")){
                    String value = (line.substring(iigual, line.trim().length()).trim());
                    Integer ivalue = parseInteger(value);
                    if(ivalue!=null)def_Width = ivalue.intValue();
                }
                if (FileUtils.containsIgnoreCase(line, "IniHeight")){
                    String value = (line.substring(iigual, line.trim().length()).trim());
                    Integer ivalue = parseInteger(value);
                    if(ivalue!=null)def_Height = ivalue.intValue();
                }
                
                if (FileUtils.containsIgnoreCase(line, "workdir")){
                    String sworkdir = (line.substring(iigual, line.trim().length()).trim());
                    if (new File(sworkdir).exists())workdir = sworkdir;
                }
                
                if (FileUtils.containsIgnoreCase(line, "ColorTheme")){
                    String themestr = (line.substring(iigual, line.trim().length()).trim());
                    if (FileUtils.containsIgnoreCase(themestr, "light"))lightTheme=true;
                    if (FileUtils.containsIgnoreCase(themestr, "dark"))lightTheme=false;
                }
                
                if (FileUtils.containsIgnoreCase(line, "MouseButtonMove")){
                    String mousestr = (line.substring(iigual, line.trim().length()).trim());
                    if (FileUtils.containsIgnoreCase(mousestr, "left"))MOURE=MouseEvent.BUTTON1;
                    if (FileUtils.containsIgnoreCase(mousestr, "right"))MOURE=MouseEvent.BUTTON3;
                    if (FileUtils.containsIgnoreCase(mousestr, "middle"))MOURE=MouseEvent.BUTTON2;
                }
                
                if (FileUtils.containsIgnoreCase(line, "MouseButtonSelect")){
                    String mousestr = (line.substring(iigual, line.trim().length()).trim());
                    if (FileUtils.containsIgnoreCase(mousestr, "left"))CLICAR=MouseEvent.BUTTON1;
                    if (FileUtils.containsIgnoreCase(mousestr, "right"))CLICAR=MouseEvent.BUTTON3;
                    if (FileUtils.containsIgnoreCase(mousestr, "middle"))CLICAR=MouseEvent.BUTTON2;
                }
                
                if (FileUtils.containsIgnoreCase(line, "MouseButtonZoom")){
                    String mousestr = (line.substring(iigual, line.trim().length()).trim());
                    if (FileUtils.containsIgnoreCase(mousestr, "left"))ZOOM_BORRAR=MouseEvent.BUTTON1;
                    if (FileUtils.containsIgnoreCase(mousestr, "right"))ZOOM_BORRAR=MouseEvent.BUTTON3;
                    if (FileUtils.containsIgnoreCase(mousestr, "middle"))ZOOM_BORRAR=MouseEvent.BUTTON2;
                }
                if (FileUtils.containsIgnoreCase(line, "gapAxisTop")){
                    String value = (line.substring(iigual, line.trim().length()).trim());
                    Integer ivalue = parseInteger(value);
                    if(ivalue!=null)gapAxisTop = ivalue.intValue();
                }
                if (FileUtils.containsIgnoreCase(line, "gapAxisBottom")){
                    String value = (line.substring(iigual, line.trim().length()).trim());
                    Integer ivalue = parseInteger(value);
                    if(ivalue!=null)gapAxisBottom = ivalue.intValue();
                }
                if (FileUtils.containsIgnoreCase(line, "gapAxisRight")){
                    String value = (line.substring(iigual, line.trim().length()).trim());
                    Integer ivalue = parseInteger(value);
                    if(ivalue!=null)gapAxisRight = ivalue.intValue();
                }
                if (FileUtils.containsIgnoreCase(line, "gapAxisLeft")){
                    String value = (line.substring(iigual, line.trim().length()).trim());
                    Integer ivalue = parseInteger(value);
                    if(ivalue!=null)gapAxisLeft = ivalue.intValue();
                }
                if (FileUtils.containsIgnoreCase(line, "GeneralPadding")){
                    String value = (line.substring(iigual, line.trim().length()).trim());
                    Integer ivalue = parseInteger(value);
                    if(ivalue!=null)padding = ivalue.intValue();
                }
                if (FileUtils.containsIgnoreCase(line, "AxisLabelsPadding")){
                    String value = (line.substring(iigual, line.trim().length()).trim());
                    Integer ivalue = parseInteger(value);
                    if(ivalue!=null)AxisLabelsPadding = ivalue.intValue();
                }
                
                if (FileUtils.containsIgnoreCase(line, "SepPrimaryXDivInAutoMode")){
                    String value = (line.substring(iigual, line.trim().length()).trim());
                    Double dvalue = parseDouble(value);
                    if(dvalue!=null)incXPrimPIXELS = dvalue.doubleValue();
                }
                if (FileUtils.containsIgnoreCase(line, "SepSecundaryXDivInAutoMode")){
                    String value = (line.substring(iigual, line.trim().length()).trim());
                    Double dvalue = parseDouble(value);
                    if(dvalue!=null)incXSecPIXELS = dvalue.doubleValue();
                }
                if (FileUtils.containsIgnoreCase(line, "SepPrimaryYDivInAutoMode")){
                    String value = (line.substring(iigual, line.trim().length()).trim());
                    Double dvalue = parseDouble(value);
                    if(dvalue!=null)incYPrimPIXELS = dvalue.doubleValue();
                }
                if (FileUtils.containsIgnoreCase(line, "SepSecundaryYDivInAutoMode")){
                    String value = (line.substring(iigual, line.trim().length()).trim());
                    Double dvalue = parseDouble(value);
                    if(dvalue!=null)incYSecPIXELS = dvalue.doubleValue();
                }
                if (FileUtils.containsIgnoreCase(line, "ZoomFactor")){
                    String value = (line.substring(iigual, line.trim().length()).trim());
                    Double dvalue = parseDouble(value);
                    if(dvalue!=null)facZoom = dvalue.doubleValue();
                }
                
                if (FileUtils.containsIgnoreCase(line, "SizePxPrimDiv")){
                    String value = (line.substring(iigual, line.trim().length()).trim());
                    Integer ivalue = parseInteger(value);
                    if(ivalue!=null)div_PrimPixSize = ivalue.intValue();
                }
                if (FileUtils.containsIgnoreCase(line, "SizePxSecunDiv")){
                    String value = (line.substring(iigual, line.trim().length()).trim());
                    Integer ivalue = parseInteger(value);
                    if(ivalue!=null)div_SecPixSize = ivalue.intValue();
                }

                if (FileUtils.containsIgnoreCase(line, "verticalYlabel")){
                    String logstr = (line.substring(iigual, line.trim().length()).trim());
                    Boolean bvalue = parseBoolean(logstr);
                    if(bvalue!=null)verticalYlabel = bvalue.booleanValue();
                }
                
                if (FileUtils.containsIgnoreCase(line, "hklOffset")){
                    String value = (line.substring(iigual, line.trim().length()).trim());
                    Integer ivalue = parseInteger(value);
                    if(ivalue!=null)hkloff = ivalue.intValue();
                }
                if (FileUtils.containsIgnoreCase(line, "hklTickSize")){
                    String value = (line.substring(iigual, line.trim().length()).trim());
                    Integer ivalue = parseInteger(value);
                    if(ivalue!=null)hklticksize = ivalue.intValue();
                }
                if (FileUtils.containsIgnoreCase(line, "prfFullprofColors")){
                    String logstr = (line.substring(iigual, line.trim().length()).trim());
                    Boolean bvalue = parseBoolean(logstr);
                    if(bvalue!=null)prfFullprofColors = bvalue.booleanValue();
                }
                if (FileUtils.containsIgnoreCase(line, "markersize")){
                    String value = (line.substring(iigual, line.trim().length()).trim());
                    Float fvalue = parseFloat(value);
                    if(fvalue!=null)def_markerSize = fvalue.floatValue();
                }
                if (FileUtils.containsIgnoreCase(line, "linewidth")){
                    String value = (line.substring(iigual, line.trim().length()).trim());
                    Float fvalue = parseFloat(value);
                    if(fvalue!=null)def_lineWidth = fvalue.floatValue();
                }
                
            }
            //per si ha canviat el loglevel/logging
            initLogger(D1Dplot_global.class.getName()); //during the par reading
            scParFile.close();
        }catch(Exception e){
            if (D1Dplot_global.isDebug())e.printStackTrace();
            log.warning("error reading config file");
            setConfigFileReaded(false);
            return false;
        }
        setConfigFileReaded(true);
        return true;
        
    }
    
    public static boolean writeParFile(){
        
        try {
            File f = new File(configFilePath);
            PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter(f)));

            // ESCRIBIM AL FITXER:
            output.println("** D1Dplot configuration file **");
            output.println("# Global");
            output.println("workdir = "+workdir);
            output.println("logging = "+Boolean.toString(logging));
            output.println("loglevel = "+loglevel);
            output.println(String.format("%s = %d", "IniWidth",def_Width));
            output.println(String.format("%s = %d", "IniHeight",def_Height));
            String but = "Left";
            if (CLICAR==MouseEvent.BUTTON2)but="Middle";
            if (CLICAR==MouseEvent.BUTTON3)but="Right";
            output.println("MouseButtonSelect = "+but);
            but = "Middle";
            if (MOURE==MouseEvent.BUTTON1)but="Left";
            if (MOURE==MouseEvent.BUTTON3)but="Right";
            output.println("MouseButtonMove = "+but);
            but = "Right";
            if (ZOOM_BORRAR==MouseEvent.BUTTON2)but="Middle";
            if (ZOOM_BORRAR==MouseEvent.BUTTON1)but="Left";
            output.println("MouseButtonZoom = "+but);

            output.println("# Plotting");
            String col = "Light";
            if (!isLightTheme())col="Dark";
            output.println("ColorTheme = "+col);
            output.println(String.format("%s = %d", "GapAxisTop",gapAxisTop));
            output.println(String.format("%s = %d", "GapAxisBottom",gapAxisBottom));
            output.println(String.format("%s = %d", "GapAxisLeft",gapAxisLeft));
            output.println(String.format("%s = %d", "GapAxisRight",gapAxisRight));
            output.println(String.format("%s = %d", "GeneralPadding",padding));
            output.println(String.format("%s = %d", "AxisLabelsPadding",AxisLabelsPadding));
            output.println(String.format("%s = %.2f", "SepPrimaryXDivInAutoMode",incXPrimPIXELS));
            output.println(String.format("%s = %.2f", "SepSecundaryXDivInAutoMode",incXSecPIXELS));
            output.println(String.format("%s = %.2f", "SepPrimaryXDivInAutoMode",incYPrimPIXELS));
            output.println(String.format("%s = %.2f", "SepSecundaryYDivInAutoMode",incYSecPIXELS));
            output.println(String.format("%s = %d", "SizePxPrimDiv",div_PrimPixSize));
            output.println(String.format("%s = %d", "SizePxSecunDiv",div_SecPixSize));
            output.println(String.format("%s = %.2f", "ZoomFactor",facZoom));
            output.println("verticalYlabel = "+Boolean.toString(verticalYlabel));
            
            output.println(String.format("%s = %d", "hklOffset",hkloff));
            output.println(String.format("%s = %d", "hklTickSize",hklticksize));
            output.println(String.format("%s = %.2f", "def_linewidth",def_lineWidth));
            output.println(String.format("%s = %.2f", "def_markerSize",def_markerSize));
            output.println("prfFullprofColors = "+Boolean.toString(prfFullprofColors));
            output.close();

        }catch(Exception e){
            if (D1Dplot_global.isDebug())e.printStackTrace();
            log.warning("error writing confing file");
            return false;
        }
        return true;
    }
    
    public static void printAllOptions(String loglevel){
      log.printmsg(loglevel,"*************************** CURRENT CONFIGURATION ***************************");
      log.printmsg(loglevel,"** D1Dplot configuration file **");
      log.printmsg(loglevel,"# Global");
      log.printmsg(loglevel,"workdir = "+workdir);
      log.printmsg(loglevel,"logging = "+Boolean.toString(logging));
      log.printmsg(loglevel,"loglevel = "+loglevel);
      log.printmsg(loglevel,String.format("%s = %d", "IniWidth",def_Width));
      log.printmsg(loglevel,String.format("%s = %d", "IniHeight",def_Height));
      String but = "Left";
      if (CLICAR==MouseEvent.BUTTON2)but="Middle";
      if (CLICAR==MouseEvent.BUTTON3)but="Right";
      log.printmsg(loglevel,"MouseButtonSelect = "+but);
      but = "Middle";
      if (MOURE==MouseEvent.BUTTON1)but="Left";
      if (MOURE==MouseEvent.BUTTON3)but="Right";
      log.printmsg(loglevel,"MouseButtonMove = "+but);
      but = "Right";
      if (ZOOM_BORRAR==MouseEvent.BUTTON2)but="Middle";
      if (ZOOM_BORRAR==MouseEvent.BUTTON1)but="Left";
      log.printmsg(loglevel,"MouseButtonZoom = "+but);
      log.printmsg(loglevel,"# Plotting");
      String col = "Light";
      if (!isLightTheme())col="Dark";
      log.printmsg(loglevel,"ColorTheme = "+col);
      log.printmsg(loglevel,String.format("%s = %d", "GapAxisTop",gapAxisTop));
      log.printmsg(loglevel,String.format("%s = %d", "GapAxisBottom",gapAxisBottom));
      log.printmsg(loglevel,String.format("%s = %d", "GapAxisLeft",gapAxisLeft));
      log.printmsg(loglevel,String.format("%s = %d", "GapAxisRight",gapAxisRight));
      log.printmsg(loglevel,String.format("%s = %d", "GeneralPadding",padding));
      log.printmsg(loglevel,String.format("%s = %d", "AxisLabelsPadding",AxisLabelsPadding));
      log.printmsg(loglevel,String.format("%s = %.2f", "SepPrimaryXDivInAutoMode",incXPrimPIXELS));
      log.printmsg(loglevel,String.format("%s = %.2f", "SepSecundaryXDivInAutoMode",incXSecPIXELS));
      log.printmsg(loglevel,String.format("%s = %.2f", "SepPrimaryXDivInAutoMode",incYPrimPIXELS));
      log.printmsg(loglevel,String.format("%s = %.2f", "SepSecundaryYDivInAutoMode",incYSecPIXELS));
      log.printmsg(loglevel,String.format("%s = %d", "SizePxPrimDiv",div_PrimPixSize));
      log.printmsg(loglevel,String.format("%s = %d", "SizePxSecunDiv",div_SecPixSize));
      log.printmsg(loglevel,String.format("%s = %.2f", "ZoomFactor",facZoom));
      log.printmsg(loglevel,"verticalYlabel = "+Boolean.toString(verticalYlabel));
      log.printmsg(loglevel,String.format("%s = %d", "hklOffset",hkloff));
      log.printmsg(loglevel,String.format("%s = %d", "hklTickSize",hklticksize));
      log.printmsg(loglevel,String.format("%s = %.2f", "def_linewidth",def_lineWidth));
      log.printmsg(loglevel,String.format("%s = %.2f", "def_markerSize",def_markerSize));
      log.printmsg(loglevel,"prfFullprofColors = "+Boolean.toString(prfFullprofColors));
      log.printmsg(loglevel,"*****************************************************************************");

    }
    
    
}

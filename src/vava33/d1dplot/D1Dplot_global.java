package com.vava33.d1dplot;

/**
 * D1Dplot
 * 
 * Global parameters
 * 
 * @author Oriol Vallcorba
 * Licence: GPLv3
 *
 */

import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import javax.swing.JFrame;

import com.vava33.d1dplot.Database;
import com.vava33.d1dplot.auxi.PDDatabase;
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.jutils.Options;
import com.vava33.jutils.VavaLogger;

public final class D1Dplot_global {

    public static final int version = 1901; //nomes canviare la versio global quan faci un per distribuir
    public static final int build_date = 190529; //nomes canviare la versio global quan faci un per distribuir
    public static final String welcomeMSG = "d1Dplot v"+version+" ("+build_date+") by O.Vallcorba\n\n"
    		+ " This is a DEVELOPMENT version and contains errors. Please USE WITH CAUTION.\n"
    		+ " Report of errors or comments about the program are appreciated.\n";
    
    private static final String className = "d1Dplot_global";
    public static final String fileSeparator = System.getProperty("file.separator");
    public static final String lineSeparator = System.getProperty("line.separator");
    public static final String userDir = System.getProperty("user.dir");
    public static final String configFilePath = System.getProperty("user.dir") + fileSeparator + "d1dconfig.cfg";
    public static final String usersGuidePath = System.getProperty("user.dir") + fileSeparator + "d1Dplot_userguide.pdf";
    public static final String loggingFilePath = System.getProperty("user.dir") + fileSeparator + "log.txt";
    public static Boolean configFileReaded = null; //true=readed false=errorReading null=notFound
    
    //symbols and characters
    public static final String theta = "\u03B8";
    public static final String angstrom= "\u212B";
    public static final String beta= "\u03B2";
    public static final SimpleDateFormat fHora = new SimpleDateFormat("yyyy-MM-dd");
    public static String[] lightColors = {"black","blue","red","green","magenta","cyan","pink","yellow"}; //8 colors
    public static String[] DarkColors = {"yellow","white","cyan","green","magenta","blue","red","pink"}; //8 colors
    
    public static VavaLogger log;
    
    private static D1Dplot_main d1DMainFrame;
    
    private static final boolean overrideLogLevelConfigFile = false;

    //PARAMETRES QUE ES PODEN CANVIAR A LES OPCIONS =======================================
    
    //global 
    private static boolean loggingConsole = false; //console
    private static boolean loggingFile = false; //file
    private static boolean loggingTA = true; //textArea -- NO ESCRIT AL FITXER DE CONFIGURACIO JA QUE VOLEM SEMPRE ACTIVAT
    private static String loglevel = "info"; //info, config, etc...
    private static String workdir = System.getProperty("user.dir");
    private static boolean keepMainWinSize = false;
    
    //parametres que no (potser) no volem que s'actualitzin a l'escriure la config file amb els nous valors
    static int def_Width;
    static int def_Height;
    static String DBfile;

    //options
    private static Options readedOpt;
    
    //==========================================================================
    
    public static void initLogger(String name){
    	log = new VavaLogger(name,loggingConsole,loggingFile,loggingTA);
        log.setLogLevel(loglevel);
        
        if (isAnyLogging()) {
        	log.enableLogger(true);
        }else {
        	log.enableLogger(false);
        }

    }
    
    public static VavaLogger getVavaLogger(String name){
        VavaLogger l = new VavaLogger(name,loggingConsole,loggingFile,loggingTA);
        l.setLogLevel(loglevel);
        if (isAnyLogging()) {
        	l.enableLogger(true);
        }else {
        	l.enableLogger(false);
        }
        return l;
    }
    
    public static boolean isAnyLogging() {
		if (loggingConsole || loggingFile || loggingTA) return true;
		return false;
	}
    
    public static boolean isLoggingConsole() {
        return loggingConsole;
    }
    public static boolean isLoggingFile() {
        return loggingFile;
    }

	//returns true if logging is enabled and level is <= config
	public static boolean isDebug(){
	    if (isAnyLogging()){
	        if (loglevel.equalsIgnoreCase("config")||loglevel.equalsIgnoreCase("debug")||loglevel.equalsIgnoreCase("fine")||loglevel.equalsIgnoreCase("finest")){
	            return true;
	        }
	    }
	    return false;
	}

	public static void setMainFrame(D1Dplot_main d1dmain){
	    d1DMainFrame = d1dmain;
	}
	
	public static D1Dplot_main getD1Dmain() {
	    return d1DMainFrame;
	}
	
	public static JFrame getD1DmainFrame() {
        return d1DMainFrame.getMainFrame();
    }

	public static boolean readParFile(){
	    initLogger(className);
	    File confFile = new File(configFilePath);
	    if (!confFile.exists()){
	        return false;
	    }
	    readedOpt = new Options();
	    readedOpt.readOptions(confFile);
	    //com que ja han estat tots inicialitzats ho puc fer així
	    if(!overrideLogLevelConfigFile){
	        loggingConsole = readedOpt.getValAsBoolean("loggingConsole", loggingConsole);
	        loggingFile = readedOpt.getValAsBoolean("loggingFile", loggingFile);
	        loggingTA = readedOpt.getValAsBoolean("loggingTextArea", loggingTA);
	        loglevel = readedOpt.getValAsString("loglevel", loglevel);
	    }
        keepMainWinSize = readedOpt.getValAsBoolean("rememberDimensions", keepMainWinSize);
        workdir = readedOpt.getValAsString("workdir", workdir);

        //ara llegim els parametres i els col·loquem on toca, tenint en compte el valor per defecte
        D1Dplot_main.setDef_Width(readedOpt.getValAsInteger("iniWidth", D1Dplot_main.getDef_Width()));
        D1Dplot_main.setDef_Height(readedOpt.getValAsInteger("iniHeight", D1Dplot_main.getDef_Height()));
        def_Width = D1Dplot_main.getDef_Width();
        def_Height = D1Dplot_main.getDef_Height(); //per si no volem sobreesciure'ls
        D1Dplot_main.setLandF(readedOpt.getValAsString("lookAndFeel", D1Dplot_main.getLandF()));
        Database.setMinDspacingToSearch(readedOpt.getValAsFloat("minDspacingToSearch", Database.getMinDspacingToSearch()));
        DataSerie.def_hklticksize=readedOpt.getValAsInteger("hklTickSize", DataSerie.def_hklticksize);
        DataSerie.def_lineWidth=readedOpt.getValAsFloat("linewidth", DataSerie.def_lineWidth);
        DataSerie.def_markerSize=readedOpt.getValAsFloat("markersize", DataSerie.def_markerSize);
        DataSerie.prfFullprofColors=readedOpt.getValAsBoolean("prfFullprofColors", DataSerie.prfFullprofColors);
        PDDatabase.setLocalDB(readedOpt.getValAsString("defCompoundDB", PDDatabase.getLocalDB()));
        DBfile = PDDatabase.getLocalDB();
        	    
	    initLogger(D1Dplot_global.class.getName()); //during the par reading
	    
	    configFileReaded=true; //el que passa es que mai sera false
	    return true;
	}

	public static Options getReadedOpt() {
        return readedOpt;
    }

    //llista per si de cas n'afegim més al futur
	public static boolean writeParFile(Options opt){
	        
	        try {
	            File f = new File(configFilePath);
	            PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter(f)));
	
	            // ESCRIBIM AL FITXER:
	            output.println("** D1Dplot configuration file **");
	            output.println("# Global");
	            output.println("workdir = "+workdir);
	            output.println("loggingConsole = "+Boolean.toString(loggingConsole));
	            output.println("loggingFile = "+Boolean.toString(loggingFile));
	            output.println("loglevel = "+loglevel);
	            output.println("lookAndFeel = "+D1Dplot_main.getLandF());
	            output.println(String.format("%s = %d", "iniWidth",def_Width));
	            output.println(String.format("%s = %d", "iniHeight",def_Height));
	            output.println("rememberDimensions = "+Boolean.toString(keepMainWinSize));
	            output.println("# Compound DB");
	            output.println("defCompoundDB = "+DBfile);
	            output.println(String.format(Locale.ROOT,"%s = %.4f", "minDspacingToSearch",Database.getMinDspacingToSearch()));
	            output.println("# Plotting");
	            if (opt!=null) {
	                Iterator<String> it = opt.getIterator();
	                while (it.hasNext()) {
	                    String key = it.next();
	                    output.println(key+" = "+opt.getValue(key));
	                }
	            }
	            
	            output.println(String.format("%s = %d", "hklTickSize",DataSerie.def_hklticksize));
	            output.println(String.format("%s = %.2f", "def_linewidth",DataSerie.def_lineWidth));
	            output.println(String.format("%s = %.2f", "def_markerSize",DataSerie.def_markerSize));
	            output.println("prfFullprofColors = "+Boolean.toString(DataSerie.prfFullprofColors));
	            output.close();
	
	        }catch(Exception e){
	            if (D1Dplot_global.isDebug())e.printStackTrace();
	            log.warning("error writing confing file");
	            return false;
	        }
	        return true;
	    }

	public static void printAllOptions(String loglevel, Options opt){
	  log.printmsg(loglevel,"*************************** CURRENT CONFIGURATION ***************************");
	  log.printmsg(loglevel,"** D1Dplot configuration file **");
	  log.printmsg(loglevel,"# Global");
	  log.printmsg(loglevel,"workdir = "+workdir);
	  log.printmsg(loglevel,"loggingConsole = "+Boolean.toString(loggingConsole));
	  log.printmsg(loglevel,"loggingFile = "+Boolean.toString(loggingFile));
	  log.printmsg(loglevel,"loggingTextArea = "+Boolean.toString(loggingTA));
	  log.printmsg(loglevel,"loglevel = "+D1Dplot_global.loglevel);
	  log.printmsg(loglevel,"lookAndFeel = "+D1Dplot_main.getLandF());
	  log.printmsg(loglevel,String.format("%s = %d", "iniWidth",def_Width));
	  log.printmsg(loglevel,String.format("%s = %d", "iniHeight",def_Height));
	  log.printmsg(loglevel,"rememberDimensions = "+Boolean.toString(keepMainWinSize));
	  log.printmsg(loglevel,"# compound DB");
	  log.printmsg(loglevel,"defCompoundDB = "+DBfile);
	  log.printmsg(loglevel,String.format(Locale.ROOT,"%s = %.4f", "minDspacingToSearch",Database.getMinDspacingToSearch()));
	  log.printmsg(loglevel,"# Plotting");
	  if (opt!=null) {
	      Iterator<String> it = opt.getIterator();
	      while (it.hasNext()) {
	          String key = it.next();
	          log.printmsg(loglevel,key+" = "+opt.getValue(key));
	      }
	  }
	  log.printmsg(loglevel,String.format("%s = %d", "hklTickSize",DataSerie.def_hklticksize));
	  log.printmsg(loglevel,String.format("%s = %.2f", "def_linewidth",DataSerie.def_lineWidth));
	  log.printmsg(loglevel,String.format("%s = %.2f", "def_markerSize",DataSerie.def_markerSize));
	  log.printmsg(loglevel,"prfFullprofColors = "+Boolean.toString(DataSerie.prfFullprofColors));
	  log.printmsg(loglevel,"*****************************************************************************");
	
	}

	public static Image getIcon(){
	    return Toolkit.getDefaultToolkit().getImage(D1Dplot_global.class.getResource("/com/vava33/d1dplot/img/d1Dplot.png"));
	}

	public static String getWorkdir() {
        return workdir;
    }
	
	public static File getWorkdirFile() {
        return new File(workdir);
    }
    
    public static void setWorkdir(File workDirOrFile) {
        D1Dplot_global.workdir = workDirOrFile.getParent();
    }

	public static boolean isLoggingTA() {
		return loggingTA;
	}

	public static void setLoggingTA(boolean loggingTA) {
		D1Dplot_global.loggingTA = loggingTA;
	}

	public static boolean isKeepSize() {
		return keepMainWinSize;
	}

	public static Boolean getConfigFileReaded() {
        return configFileReaded;
    }

    public static String getStringTimeStamp(String simpleDateFormatStr){
        SimpleDateFormat fHora = new SimpleDateFormat(simpleDateFormatStr);
        return fHora.format(new Date());
    }
}

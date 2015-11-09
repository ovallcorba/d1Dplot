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

import java.io.File;

import com.vava33.jutils.VavaLogger;

public final class D1Dplot_global {

    public static final int satur32 = Short.MAX_VALUE;
    public static final int satur65 = (Short.MAX_VALUE * 2) + 1;
    public static final boolean logging = true;
    public static final String loglevel = "config"; //info, config, etc...
    public static final String welcomeMSG = "d2Dplot v1510 (151020) by OV";
    public static final String separator = System.getProperty("file.separator");
    public static final String userDir = System.getProperty("user.dir");
    public static final String configFilePath = System.getProperty("user.dir") + separator + "d1dconfig.cfg";
    public static String workdir = System.getProperty("user.dir");

    //symbols and characters
    public static final String theta = "\u03B8";
    public static final String angstrom= "\u212B";

    public static VavaLogger log;
    
    public static void initLogger(String name){
        log = new VavaLogger(name);
        log.enableLogger(logging);
        log.setLogLevel(loglevel);
    }
    
    public static String getWorkdir() {
        return workdir;
    }

    public static void setWorkdir(String workdir) {
        D1Dplot_global.workdir = workdir;
    }
    
    public static void setWorkdir(File workDirOrFile) {
        D1Dplot_global.workdir = workDirOrFile.getPath();
    }
}

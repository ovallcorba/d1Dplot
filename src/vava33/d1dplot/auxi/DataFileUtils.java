package com.vava33.d1dplot.auxi;

/**
 * D1Dplot
 * 
 * Operations with DATA files
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.math3.util.FastMath;

import com.vava33.cellsymm.HKLrefl;
import com.vava33.d1dplot.D1Dplot_global;
import com.vava33.d1dplot.D1Dplot_main;
import com.vava33.d1dplot.DicvolDialog;
import com.vava33.d1dplot.PlotPanel;
import com.vava33.d1dplot.data.DataPoint;
import com.vava33.d1dplot.data.DataPoint_hkl;
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.d1dplot.data.Data_Common;
import com.vava33.d1dplot.data.Plottable;
import com.vava33.d1dplot.data.Plottable_point;
import com.vava33.d1dplot.data.SerieType;
import com.vava33.d1dplot.data.Xunits;
import com.vava33.jutils.VavaLogger;
import com.vava33.jutils.FileUtils;

public final class DataFileUtils {

    private static final String className = "DataFileUtils";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);    
    
    private static String[] charsets = {"","UTF-8","ISO8859-1","Windows-1251","Shift JIS","Windows-1252"};
    
    private static class FileFormat{
    	private String[] extensions;
    	private String description;
    	
    	public FileFormat(String[] exts, String description) {
    		this.extensions=exts;
    		this.description=description;
    	}
		public String[] getExtensions() {return extensions;}
		public String getDescription() {return description;}
    }
    
    public static enum SupportedReadExtensions {DAT,XYE,XY,ASC,GSA,XRDML,FF,D1P,PRF,GR,TXT;}
    public static enum SupportedWriteExtensions {DAT,ASC,GSA,XRDML,GR,FF;}
    
    public static SupportedReadExtensions getReadExtEnum(String n) {
        for (SupportedReadExtensions x: SupportedReadExtensions.values()) {
            if (n.equalsIgnoreCase(x.toString()))return x;
        }
        return null;
    }
    
    public static SupportedWriteExtensions getWriteExtEnum(String n) {
        for (SupportedWriteExtensions x: SupportedWriteExtensions.values()) {
            if (n.equalsIgnoreCase(x.toString()))return x;
        }
        return null;
    }
    
    public static final LinkedHashMap<String, FileFormat> XRDformatInfo;
    static
    {
        XRDformatInfo = new LinkedHashMap<String, FileFormat>(); //ext, description
        XRDformatInfo.put("dat", new FileFormat(new String[]{"dat","DAT"},"2 or 3 columns file 2th/int/(err) with header (.dat)"));
        XRDformatInfo.put("xye", new FileFormat(new String[]{"xye","XYE"},"3 columns file 2th/int/err (*.xye)"));
        XRDformatInfo.put("xy", new FileFormat(new String[]{"xy","XY"},"2 columns file 2th/int (*.xy)"));
        XRDformatInfo.put("asc", new FileFormat(new String[]{"asc","ASC"},"2 columns file 2th/int with no headers (*.asc)"));
        XRDformatInfo.put("gsa", new FileFormat(new String[]{"gsa","GSA"},"GSAS Standard Powder Data File (*.gsa)"));
        XRDformatInfo.put("xrdml", new FileFormat(new String[]{"xrdml","XRDML"},"Panalytical format (*.xrdml)"));
        XRDformatInfo.put("ff", new FileFormat(new String[]{"ff","FF"},"List of intensities in free format (*.ff)"));
        XRDformatInfo.put("d1p", new FileFormat(new String[]{"d1p","D1P"},"Obs,calc and difference profiles from d1Dplot (*.d1p)"));
        XRDformatInfo.put("prf", new FileFormat(new String[]{"prf","PRF"},"Obs,calc and difference profiles from fullprof (*.prf)"));
        XRDformatInfo.put("gr", new FileFormat(new String[]{"gr","GR"},"g(r) from pdfgetx3 (*.gr)"));
        XRDformatInfo.put("txt", new FileFormat(new String[]{"txt","TXT"},"2 columns space or comma separated (*.txt)"));
    }
    
    public static enum SupportedReadRefExtensions {REF,HKL,DAT,TXT;}
    public static enum SupportedWriteRefExtensions {REF;}
    public static final LinkedHashMap<String, FileFormat> REFformatInfo;
    static
    {
    	REFformatInfo = new LinkedHashMap<String, FileFormat>(); //ext, description
    	REFformatInfo.put("ref", new FileFormat(new String[]{"ref","REF"},"1 column of 2theta values (*.ref)"));
    	REFformatInfo.put("hkl", new FileFormat(new String[]{"hkl","HKL"},"reflections file (*.hkl)"));
    	REFformatInfo.put("dat", new FileFormat(new String[]{"dat","DAT"},"2 or 3 columns file 2th/int/(err) with optional header"));
    	REFformatInfo.put("txt", new FileFormat(new String[]{"txt","TXT"},"2 columns space or comma separated (*.txt)"));
    }
    
    public static enum SupportedWritePeaksFormats {DIC,TXT;}
    public static final LinkedHashMap<String, FileFormat> peakFormatInfo;
    static
    {
        peakFormatInfo = new LinkedHashMap<String, FileFormat>(); //ext, description
        peakFormatInfo.put("dic", new FileFormat(new String[]{"dic","DIC"},"DICVOL06 input file (*.dic)"));
        peakFormatInfo.put("inp", new FileFormat(new String[]{"inp","INP"},"McMaille input file (*.inp)"));
        peakFormatInfo.put("txt", new FileFormat(new String[]{"txt","TXT"},"list of peaks in 3 columns format 2th/int/err"));
    }
    
    public static FileNameExtensionFilter[] getExtensionFilterWrite(){
        Iterator<String> itrformats = DataFileUtils.XRDformatInfo.keySet().iterator();
        FileNameExtensionFilter[] filter = new FileNameExtensionFilter[DataFileUtils.SupportedWriteExtensions.values().length];
        int nfiltre=0;
        while (itrformats.hasNext()){
            FileFormat frm = DataFileUtils.XRDformatInfo.get(itrformats.next());
            DataFileUtils.SupportedWriteExtensions wfrm = null;
            for (int i=0; i<frm.getExtensions().length; i++) {
            	wfrm = FileUtils.searchEnum(DataFileUtils.SupportedWriteExtensions.class, frm.getExtensions()[i]);	
            }
            if (wfrm!=null){
                //afegim filtre
            	filter[nfiltre]=new FileNameExtensionFilter(frm.getDescription(),frm.getExtensions());
                nfiltre = nfiltre +1;
            }
        }
        return filter;
    }
    
    public static FileNameExtensionFilter[] getExtensionFilterRead(){
        Iterator<String> itrformats = DataFileUtils.XRDformatInfo.keySet().iterator();
        FileNameExtensionFilter[] filter = new FileNameExtensionFilter[DataFileUtils.SupportedReadExtensions.values().length+1];
        List<String> frmStrings = new ArrayList<String>();
        int nfiltre=0;
        while (itrformats.hasNext()){
            FileFormat frm = DataFileUtils.XRDformatInfo.get(itrformats.next());
            DataFileUtils.SupportedReadExtensions wfrm = null;
            for (int i=0; i<frm.getExtensions().length; i++) {
            	wfrm = FileUtils.searchEnum(DataFileUtils.SupportedReadExtensions.class, frm.getExtensions()[i]);	
            }
            if (wfrm!=null){
                //afegim filtre
            	filter[nfiltre]=new FileNameExtensionFilter(frm.getDescription(),frm.getExtensions());
            	frmStrings.addAll(Arrays.asList(frm.getExtensions()));
                nfiltre = nfiltre +1;
            }
        }
        //afegim filtre de tots els formats
        filter[nfiltre] = new FileNameExtensionFilter("All 1D-XRD supported formats", frmStrings.toArray(new String[frmStrings.size()]));
        return filter;
    }
    
    public static FileNameExtensionFilter[] getExtensionFilterRefWrite(){
        Iterator<String> itrformats = DataFileUtils.REFformatInfo.keySet().iterator();
        FileNameExtensionFilter[] filter = new FileNameExtensionFilter[DataFileUtils.SupportedWriteRefExtensions.values().length];
        int nfiltre=0;
        while (itrformats.hasNext()){
            FileFormat frm = DataFileUtils.REFformatInfo.get(itrformats.next());
            DataFileUtils.SupportedWriteRefExtensions wfrm = null;
            for (int i=0; i<frm.getExtensions().length; i++) {
            	wfrm = FileUtils.searchEnum(DataFileUtils.SupportedWriteRefExtensions.class, frm.getExtensions()[i]);	
            }
            if (wfrm!=null){
                //afegim filtre
            	filter[nfiltre]=new FileNameExtensionFilter(frm.getDescription(),frm.getExtensions());
                nfiltre = nfiltre +1;
            }
        }
        return filter;
    }
    
    public static FileNameExtensionFilter[] getExtensionFilterRefRead(){
        Iterator<String> itrformats = DataFileUtils.REFformatInfo.keySet().iterator();
        FileNameExtensionFilter[] filter = new FileNameExtensionFilter[DataFileUtils.SupportedReadRefExtensions.values().length+1];
        List<String> frmStrings = new ArrayList<String>();
        int nfiltre=0;
        while (itrformats.hasNext()){
            FileFormat frm = DataFileUtils.REFformatInfo.get(itrformats.next());
            DataFileUtils.SupportedReadRefExtensions wfrm = null;
            for (int i=0; i<frm.getExtensions().length; i++) {
            	wfrm = FileUtils.searchEnum(DataFileUtils.SupportedReadRefExtensions.class, frm.getExtensions()[i]);	
            }
            if (wfrm!=null){
                //afegim filtre
            	filter[nfiltre]=new FileNameExtensionFilter(frm.getDescription(),frm.getExtensions());
            	frmStrings.addAll(Arrays.asList(frm.getExtensions()));
                nfiltre = nfiltre +1;
            }
        }
        //afegim filtre de tots els formats
        filter[nfiltre] = new FileNameExtensionFilter("All supported formats", frmStrings.toArray(new String[frmStrings.size()]));
        return filter;
    }
    
    public static FileNameExtensionFilter[] getExtensionFilterPeaksWrite(){
        Iterator<String> itrformats = DataFileUtils.peakFormatInfo.keySet().iterator();
        FileNameExtensionFilter[] filter = new FileNameExtensionFilter[DataFileUtils.SupportedWritePeaksFormats.values().length];
        int nfiltre=0;
        while (itrformats.hasNext()){
            FileFormat frm = DataFileUtils.peakFormatInfo.get(itrformats.next());
            DataFileUtils.SupportedWritePeaksFormats wfrm = null;
            for (int i=0; i<frm.getExtensions().length; i++) {
            	wfrm = FileUtils.searchEnum(DataFileUtils.SupportedWritePeaksFormats.class, frm.getExtensions()[i]);	
            }
            if (wfrm!=null){
                //afegim filtre
            	filter[nfiltre]=new FileNameExtensionFilter(frm.getDescription(),frm.getExtensions());
                nfiltre = nfiltre +1;
            }
        }
        return filter;
    }
    
    //give directly the extension
    public static Plottable readPatternFile(File d1file, SupportedReadExtensions format) {
        Plottable p;
        switch (format) {
            case DAT:
                p = readDAT(d1file);
                break;
            case XYE:
                p = readXYE(d1file);
                break;
            case XY:
                p = readXYE(d1file);
                break;
            case ASC:
                p = readASC(d1file);
                break;
            case GSA:
                p = readGSA(d1file);
                break;
            case XRDML:
                p = readXRDML(d1file);
                break;
            case FF:
                p = readFF(d1file);
                break;
            case D1P:
                p = readD1P(d1file);
                break;
            case PRF:
                p = readPRF(d1file);
                break;
            case GR:
                p = readGR(d1file);
                break;
            default:
                p = readUNK(d1file); //TXT
                break;

        }
        if (p==null){
            //ho tornem a intentar...
            p = readUNK(d1file);
            if (p==null) {
                log.debug("Error reading pattern "+d1file.getAbsolutePath());
                return null;               
            }
        }
        if (p.getFile() == null) {
            p.setFile(d1file);
        }
        
        return p;
    }
    
    //Autodetect format from extension or ask
    public static Plottable readPatternFile(File d1file) {
        // comprovem extensio
        log.debug(d1file.toString());
        String ext = FileUtils.getExtension(d1file).trim();

        // this line returns the FORMAT in the ENUM or NULL
        SupportedReadExtensions format = FileUtils.searchEnum(SupportedReadExtensions.class, ext);
        
        if (format != null) {log.debug("Format=" + format.toString());}

        if (format == null) {
            SupportedReadExtensions[] possibilities = SupportedReadExtensions.values();
            SupportedReadExtensions s = (SupportedReadExtensions) JOptionPane.showInputDialog(null, "Input format:", "Read File",
                            JOptionPane.PLAIN_MESSAGE, null, possibilities,possibilities[0]);
            if (s == null) {return null;}
            format = s;
        }
        
        return readPatternFile(d1file,format);
    }
    
    public static File writePatternFile(File d1File, DataSerie serie, SupportedWriteExtensions format, boolean overwrite, boolean addYbkg) {

        boolean written = false;
        File fout = null;
        
        switch (format) {
            case DAT:
                written = writeDAT_ALBA(serie,d1File,overwrite,addYbkg);
                break;
            case ASC:
                written = writeASC(serie,d1File,overwrite,addYbkg);
                break;
            case GSA:
                written = writeGSA(serie,d1File,overwrite,addYbkg);
                break;
            case XRDML:
                written = writeXRDML(serie,d1File,true,overwrite,addYbkg);
                break;
            case FF:
                written = writeDAT_FreeFormat(serie,d1File,overwrite,addYbkg);
                break;
            case GR:
                written = writeGR(serie,d1File,overwrite,addYbkg);
                break;
            default:
                log.warning("Unknown format to write");
                return null;
        }
        if (written) fout = d1File;
        return fout;
    }
    
    public static File writePatternFile(File d1File, DataSerie serie, boolean overwrite, boolean addYbkg) {
        // comprovem extensio
        log.debug(d1File.toString());
        String ext = FileUtils.getExtension(d1File).trim();

        //DETECCIO SERIE BKG
        if(serie.getTipusSerie()==SerieType.bkg){
            boolean saveEspecial = FileUtils.YesNoDialog(null, "You are saving a Background serie\n"
                    + "Do you want so save it as a point list?\n"
                    + "(Answer NO to save it as a normal data file)");
            if (saveEspecial){
                String s = (String)JOptionPane.showInputDialog(
                        null,
                        "Number of Background points to save (may not be exact...)",
                        "Save Background",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        null,
                        "80");
                int npoints = 80;
                if ((s != null) && (s.length() > 0)) {
                    try{
                        npoints = Integer.parseInt(s);
                    }catch(Exception ex){
                        ex.printStackTrace();
                        log.warning("Error reading n points");
                        return null;
                    }
                }
                boolean written = false;
                File fout = null;
                written = writeBKG(serie,npoints,d1File,overwrite);
                if (written) fout = d1File;
                return fout;
            }
        }
        
        // this line returns the FORMAT in the ENUM or NULL
        SupportedWriteExtensions format = FileUtils.searchEnum(
                SupportedWriteExtensions.class, ext);
        if (format != null) {
            log.debug("Format=" + format.toString());
        }

        if (format == null) {
            SupportedWriteExtensions[] possibilities = SupportedWriteExtensions
                    .values();
            SupportedWriteExtensions s = (SupportedWriteExtensions) JOptionPane
                    .showInputDialog(null, "Output format:", "Save File",
                            JOptionPane.PLAIN_MESSAGE, null, possibilities,
                            possibilities[0]);
            if (s == null) {
                return null;
            }
            format = s;
        }

        return writePatternFile(d1File, serie, format, overwrite, addYbkg);
        
    }
    
    public static File writePeaksFile(File d1File, DataSerie pksDS, boolean overwrite) {
        SupportedWritePeaksFormats[] possibilities = SupportedWritePeaksFormats
                .values();
        SupportedWritePeaksFormats s = (SupportedWritePeaksFormats) JOptionPane
                .showInputDialog(null, "Output format:", "Save Peaks",
                        JOptionPane.PLAIN_MESSAGE, null, possibilities,
                        possibilities[0]);
        if (s == null) {
            return null;
        }
        boolean written = false;
        File fout = null;
        switch (s) {
            case DIC:
                d1File = FileUtils.canviExtensio(d1File, "dic");
                written = writePeaksDIC(pksDS,d1File,overwrite);
                break;
            case TXT:
                written = writePeaksTXT(pksDS,d1File,overwrite);
                break;
        }
        if (written) fout = d1File;
        return fout;
    }
    
    private static Data_Common readXYE(File f){
        return readDAT(f);
    }
    
    private static Data_Common readASC(File f){
        return readDAT(f);

    }
    
    //only 1 serie
    private static Data_Common readUNK(File datFile ) {
    	//primer mirem si es free format
    	if (detectFreeFormat(datFile)) {
    		return readFF(datFile);
    	}
    	//es poden afegir altres comprovacions...
    	
    	//Sino mirem de detectar linies on hi hagi dos valors t2, intensitat separats per espai, coma, etc...
        boolean readed = true;
        //creem un DataSerie_Pattern
        Data_Common dsP = new Data_Common();
        DataSerie ds = new DataSerie(SerieType.dat,Xunits.tth,dsP);

        //FIRST CHECK ENCODING
        String enc = getEncodingToUse(datFile);
        Scanner sf = null;
        try {
            sf = new Scanner(datFile,enc);
            while (sf.hasNextLine()){
                String line = sf.nextLine();
                if (isComment(line)){
                    dsP.addCommentLine(line);
                    double wl = searchForWavel(line);
                    if (wl>0){
                        dsP.setOriginalWavelength(wl);
                        ds.setWavelength(wl);
                    }
                    continue;
                }
                if (line.trim().isEmpty()){
                    continue;
                }

                //test csv:
                String values[] = line.trim().split(",");
                log.writeNameNumPairs("config", true, "values.length (comma)=", values.length);
                if (values.length<2){
                    values = line.trim().split("\\s+");
                    log.writeNameNumPairs("config", true, "values.length (space)=", values.length);
                    if (values.length<2){
                        continue;
                    }
                }
                
                double t2 = Double.parseDouble(values[0]);
                double inten = Double.parseDouble(values[1]);
                double sdev = 0.0;
                try{
                    sdev = Double.parseDouble(values[2]);
                }catch(Exception ex){
                    log.fine("error parsing sdev");
                }

                ds.addPoint(new DataPoint(t2,inten,sdev));

            }
            ds.serieName=datFile.getName();
            dsP.addDataSerie(ds);

        }catch(Exception e){
            if (D1Dplot_global.isDebug())e.printStackTrace();
            readed = false;
        }finally {
            if(sf!=null)sf.close();
        }
        if (readed){
            return dsP;
        }else{
            return null;
        }
    }

    
    private static String getEncodingToUse(File f) {
      //FIRST CHECK ENCODING
        Scanner sf=null;
        charsets[0] = Charset.defaultCharset().name();
        int charsetToUse = 0;
        for (int i=0;i<charsets.length;i++) {
            try {
                sf = new Scanner(f,charsets[i]);
                if (sf.hasNextLine()) {
                    charsetToUse=i;
                    break;
                }
                
            } catch (Exception e1) {
                e1.printStackTrace();
            }finally {
                sf.close();
            }
        }
        return charsets[charsetToUse];
    }
    
    
    //only 1 serie
    private static Data_Common readDAT(File datFile) { 
        boolean firstLine = true;
        boolean readed = true;
        
        //creem un DataSerie_Pattern
        Data_Common dsP = new Data_Common();
        DataSerie ds = new DataSerie(SerieType.dat,Xunits.tth,dsP);
        
        //FIRST CHECK ENCODING
        String enc = getEncodingToUse(datFile);
        
        Scanner sf = null;
        try {
            sf = new Scanner(datFile,enc);
            while (sf.hasNextLine()){
                String line = sf.nextLine();
                if (isComment(line)){
                    dsP.addCommentLine(line);
                    double wl = searchForWavel(line);
                    if (wl>0){
                        dsP.setOriginalWavelength(wl);
                        ds.setWavelength(wl);
                    }
                    continue;
                }
                if (line.trim().isEmpty()){
                    continue;
                }

                String values[] = line.trim().split("\\s+");

                //afegim comprovacio de que la primera linea pot ser no comentada (cas xye), TODO:revisar
                double t2 = 0.0;
                double inten = 0.0;
                try {
                    t2 = Double.parseDouble(values[0]);
                    inten = Double.parseDouble(values[1]);
                }catch(Exception ex) {
                	log.warning(String.format("Error reading (t2 Intensity) in line: %s",line));
                	continue;
                }
                double sdev = 0.0;
                try{
                    sdev = Double.parseDouble(values[2]);
                }catch(Exception ex){
                    log.fine("error parsing sdev");
                }

                ds.addPoint(new DataPoint(t2,inten,sdev));
                if (firstLine){
                    firstLine = false;
                }

                if (!sf.hasNextLine()){
                }
            }
            if (ds.getNpoints()<=0)return null;
            ds.serieName=datFile.getName();
            dsP.addDataSerie(ds);

        }catch(Exception e){
            if (D1Dplot_global.isDebug())e.printStackTrace();
            readed = false;
        }finally {
            if(sf!=null)sf.close();
        }
        if (readed){
            return dsP;
        }else{
            return null;
        }
    }
    
    
    private static Data_Common readGR(File datFile) {
        boolean readed = true;
        //creem un DataSerie_Gr
        Data_Common dsGr = new Data_Common();
        DataSerie ds = new DataSerie(SerieType.gr,Xunits.G,dsGr);
        
        //FIRST CHECK ENCODING
        String enc = getEncodingToUse(datFile);
        
        boolean startData = false;
        Scanner sf = null;
        try {
            sf = new Scanner(datFile,enc);
            while (sf.hasNextLine()){
                String line = sf.nextLine();

                if (line.trim().startsWith("###")){
                    startData = true;
                    continue;
                }
                
                if (!startData){
                    dsGr.addCommentLine(line);
                    if (line.contains("wave")){
                        String values[] = line.trim().split("\\s+");
                        try{
                            dsGr.setOriginalWavelength(Double.parseDouble(values[2]));
                            ds.setWavelength(Double.parseDouble(values[2]));
                        }catch(Exception e){
                            log.debug("error parsing wave");
                        }
                    }
                    continue;
                }
                
                if ((line.trim().startsWith("#S"))||(line.trim().startsWith("#L"))){
                    dsGr.addCommentLine(line);
                    continue;
                }
                
                //arribats aqui son dades
                
                String values[] = line.trim().split("\\s+");

                double x = Double.parseDouble(values[0]);
                double y = Double.parseDouble(values[1]);
                ds.addPoint(new DataPoint(x,y,0.0));
            }
            ds.serieName=datFile.getName();
            dsGr.addDataSerie(ds);
            sf.close();

        }catch(Exception e){
            if (D1Dplot_global.isDebug())e.printStackTrace();
            readed = false;
        }finally {
            if(sf!=null)sf.close();
        }
        if (readed){
            return dsGr;
        }else{
            return null;
        }
    }
    
    //es podria optimitzar omplint un datapoint i afegint-lo a la serie a cada cicle
    //TODO: afegir lectura wavelength
    private static Data_Common readXRDML(File f){
        boolean pos = false;
        boolean startend = false; //if we have start/end or ListPositions
        boolean readed = true;

        //creem un DataSerie_Pattern
        Data_Common dsP = new Data_Common();
        DataSerie ds = new DataSerie(SerieType.dat,Xunits.tth,dsP);
        
        List<Double> intensities = new ArrayList<Double>();
        List<Double> t2ang = new ArrayList<Double>();;
        
        double t2i=0;
        double t2f=0;
        double step=0;
        
        //FIRST CHECK ENCODING
        String enc = getEncodingToUse(f);
        
        Scanner sf = null;
        try {
            sf = new Scanner(f,enc);
            while (sf.hasNextLine()){
                String line = sf.nextLine();
                if (line.contains("<positions")){
                    pos = true;
                }
                if (pos){
                    //now we may have start/end positions OR list positions
                    if (line.contains("startPosition")){
                        String temp = line.split(">")[1];
                        temp = temp.split("<")[0];
                        t2i=Double.parseDouble(temp);
                        
                        //now end position
                        line = sf.nextLine();
                        temp = line.split(">")[1];
                        temp = temp.split("<")[0];
                        t2f=Double.parseDouble(temp);
                        
                        startend = true;
                    }
                    
                    if (line.contains("listPositions")){
                        String temp = line.split(">")[1];
                        temp = temp.split("<")[0];
                        String[] t2 = temp.trim().split("\\s+");
                        for (int i=0; i<t2.length;i++){
                            t2ang.add(Double.parseDouble(t2[i]));
                        }
                    }
                    
                    if (line.contains("intensities")){
                        String temp = line.split(">")[1];
                        temp = temp.split("<")[0];
                        String[] intens = temp.trim().split("\\s+");
                        for (int i=0; i<intens.length;i++){
                            intensities.add(Double.parseDouble(intens[i]));
                        }
                    }
                }
            }
            
            //check if we have to generate 2thetas
            if(startend){
                int nrint = intensities.size();
                log.debug("nrint="+nrint);
                step = (t2f-t2i)/nrint;
                double t2c = t2i;
                while(t2c<=t2f){
                    t2ang.add(t2c);
                    t2c = t2c+step;
                }
                log.debug("nrt2ang="+t2ang.size());
            }
            
            //here we should have t2ang and intensities full and same size, populate dps
            int size = FastMath.min(t2ang.size(), intensities.size());
            if (size == 0)throw new Exception("no points");
            for (int i=0; i<size;i++){
                ds.addPoint(new DataPoint(t2ang.get(i),intensities.get(i),0.0f));
            }
            ds.serieName=f.getName();
            dsP.addDataSerie(ds);
            
        }catch(Exception ex){
            if (D1Dplot_global.isDebug())ex.printStackTrace();
            readed = false;
        }finally {
            if(sf!=null)sf.close();
        }
        if (readed){
            return dsP;
        }else{
            return null;
        }
    }
    
    private static Data_Common readFF(File f){
        boolean firstLine = true;
        boolean readed = true;
        
        //creem un DataSerie_Pattern
        Data_Common dsP = new Data_Common();
        DataSerie ds = new DataSerie(SerieType.dat,Xunits.tth,dsP);
        
        List<Double> intensities = new ArrayList<Double>();
        List<Double> t2ang = new ArrayList<Double>();
        double t2i=0;
        double t2f=0;
        double step=0;
        
        //FIRST CHECK ENCODING
        String enc = getEncodingToUse(f);
        
        Scanner sf = null;
        try {
            sf = new Scanner(f,enc);
            while (sf.hasNextLine()){
                String line = sf.nextLine();
                if (isComment(line)){
                    dsP.addCommentLine(line);
                    double wl = searchForWavel(line);
                    if (wl>0){
                        dsP.setOriginalWavelength(wl);
                        ds.setWavelength(wl);
                    }
                    continue;
                }
                if (line.trim().isEmpty()){
                    continue;
                }
                
                String values[] = line.trim().split("\\s+");
                
                if (firstLine){
                    try {
                        t2i=Double.parseDouble(values[0]);
                        step=Double.parseDouble(values[1]);
                        t2f=Double.parseDouble(values[2]);
                    }catch(Exception readex){
                        if (D1Dplot_global.isDebug())readex.printStackTrace();
                        log.warning("Error reading 1st line of Free Format file (t2i step t2f)");
                        readed=false;
                        break;
                    }
                    firstLine=false;
                    continue;
                }
                //a partir d'aqui linies d'intensitats
                for (int i=0;i<values.length;i++){
                    intensities.add(Double.parseDouble(values[i]));
                }
            }
            
            //now the 2thetas
            int nrint = intensities.size();
            log.debug("nrint="+nrint);
            double t2c = t2i;
            while(t2c<=t2f){
                t2ang.add(t2c);
                t2c = t2c+step;
            }
            log.debug("nrt2ang="+t2ang.size());
            
            //here we should have t2ang and intensities full and same size, populate dps
            int size = FastMath.min(t2ang.size(), intensities.size());
            if (size == 0)throw new Exception("no points");
            for (int i=0; i<size;i++){
                ds.addPoint(new DataPoint(t2ang.get(i),intensities.get(i),0.0f));
            }
            ds.serieName=f.getName();
            dsP.addDataSerie(ds);
            
        }catch(Exception ex){
            if (D1Dplot_global.isDebug())ex.printStackTrace();
            readed=false;
        }finally {
            if(sf!=null)sf.close();
        }
        if (readed){
            return dsP;
        }else{
            return null;
        }
    }
    

    
    private static boolean detectFreeFormat(File f){
        //al free format la primera linia es 2ti step 2tf
        double t2 = 0.00f;
        double inten = 0.00f;
        double sdev = -100.00f;
        Scanner sf = null;
        try {
            sf = new Scanner(f);
            while (sf.hasNextLine()){
                String line = sf.nextLine();
                if (isComment(line)){
                    continue;
                }
                if (line.trim().isEmpty()){
                    continue;
                }
                
                String values[] = line.trim().split("\\s+");
                
                try {
                    t2 = Double.parseDouble(values[0]);
                    inten = Double.parseDouble(values[1]);
                    if (values.length>2)sdev = Double.parseDouble(values[2]);
                }catch(Exception readex){
                    log.info("Error detecting DAT format");
                    continue;
                }
                break;
            }
            
            //al free format la primera linia es 2ti step 2tf, i l'step ha de ser petit pero no zero (important per els diagrames resta)
            if (inten<0)return false;
            if (FastMath.abs(inten)>0.5)return false;
            if (FastMath.abs(inten)==0.000000f)return false;
            
            if(t2>inten){
                if(sdev>inten){
                    if (sdev>t2){
                        //probablement sera freeformat
                        log.info("Free Format detected!");
                        return true;
                    }
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if(sf!=null)sf.close();
        }

        return false;
    }
    
    private static Data_Common readGSA(File f){
        boolean startData = false;
        boolean esdev = false;
        double t2p = 0;
        double step = 0;
        double t2i = 0;
        
        boolean readed = true;
        
        //creem un DataSerie_Pattern
        Data_Common dsP = new Data_Common();
        DataSerie ds = new DataSerie(SerieType.dat,Xunits.tth,dsP);
        
        //FIRST CHECK ENCODING
        String enc = getEncodingToUse(f);
        
        Scanner sf = null;
        try {
            sf = new Scanner(f,enc);
            while (sf.hasNextLine()){
                String line = sf.nextLine();
                if (isComment(line)){
                    dsP.addCommentLine(line);
                    double wl = searchForWavel(line);
                    if (wl>0){
                        dsP.setOriginalWavelength(wl);
                        ds.setWavelength(wl);
                    }
                    continue;
                }
                if (line.trim().startsWith("Instrument")){
                    dsP.addCommentLine(line);
                    continue;
                }
                
                if (line.trim().startsWith("BANK")){
//                    Instrument parameter      bl04.prm                                              
//                    BANK 1   20951    4191 CONST    60.000     0.600  0.0 0.0 ESD   
                    String values[] = line.trim().split("\\s+");
                    t2i=Double.parseDouble(values[5])/100.0;
                    step=Double.parseDouble(values[6])/100.0;
                    t2p = t2i;
                    if(line.contains("ESD"))esdev=true;
                    startData = true;
                    continue;
                }
                
                String values[] = line.trim().split("\\s+");
                
                if (startData){
                    try {
                        for (int i=0;i<values.length;i=i+2){
                            double inten = Double.parseDouble(values[i]);
                            double sd = 0;
                            if (esdev) sd = Double.parseDouble(values[i+1]);
                            ds.addPoint(new DataPoint(t2p, inten, sd));
                            t2p = t2p + step;
                        }
                    }catch(Exception readex){
                        if (D1Dplot_global.isDebug())readex.printStackTrace();
                        log.debug("Error reading GSA file");
                        readed=false;
                        break;
                    }
                    continue;
                }
            }
            
            ds.serieName=f.getName();
            dsP.addDataSerie(ds);
            
        }catch(Exception ex){
            if (D1Dplot_global.isDebug())ex.printStackTrace();
            readed=false;;
        }finally {
            if(sf!=null)sf.close();
        }
        if (readed){
            return dsP;
        }else{
            return null;
        }
    }
    
    private static Data_Common readD1P(File f){
        boolean readed = true;
        
        //creem un DataSerie_PRF
        Data_Common dsPRF = new Data_Common();
        DataSerie dsObs = new DataSerie(SerieType.obs,Xunits.tth,dsPRF);
        DataSerie dsCal = new DataSerie(SerieType.cal,Xunits.tth,dsPRF);
        DataSerie dsDif = new DataSerie(SerieType.diff,Xunits.tth,dsPRF);
        
        //FIRST CHECK ENCODING
        String enc = getEncodingToUse(f);
        
        double wave = -1.0;
        double zero = 0.0;
        Xunits units = Xunits.tth;
        
        Scanner sf = null;
        String line = "";
        try {
            sf = new Scanner(f,enc);
            while (sf.hasNextLine()){
                line = sf.nextLine();
                if (isComment(line))continue;
                
                if (FileUtils.containsIgnoreCase(line, "name")) {
                    final int iigual = line.trim().indexOf("=") + 1;
                    dsObs.serieName=line.trim().substring(iigual, line.trim().length()).trim()+" (obs)";
                    dsCal.serieName=line.trim().substring(iigual, line.trim().length()).trim()+" (calc)";
                    dsDif.serieName=line.trim().substring(iigual, line.trim().length()).trim()+" (diff)";
                }
                
//                if (FileUtils.containsIgnoreCase(line, "cell")) {
//                    final int iigual = line.trim().indexOf("=") + 1;
//                    String cell =line.trim().substring(iigual, line.trim().length()).trim();
//                }
//                if (FileUtils.containsIgnoreCase(line, "sg")) {
//                    final int iigual = line.trim().indexOf("=") + 1;
//                    String cell =line.trim().substring(iigual, line.trim().length()).trim();
//                }
                
                if (FileUtils.containsIgnoreCase(line, "wave")) {
                    final int iigual = line.trim().indexOf("=") + 1;
                    wave =Double.parseDouble(line.trim().substring(iigual, line.trim().length()).trim());
                }
                if (FileUtils.containsIgnoreCase(line, "zero")) {
                    final int iigual = line.trim().indexOf("=") + 1;
                    zero =Double.parseDouble(line.trim().substring(iigual, line.trim().length()).trim());
                }
                if (FileUtils.containsIgnoreCase(line, "units")) {
                    final int iigual = line.trim().indexOf("=") + 1;
                    String xun =line.trim().substring(iigual, line.trim().length()).trim();
                    units = Xunits.getEnum(xun);
                    dsObs.setxUnits(units);
                    dsCal.setxUnits(units);
                    dsDif.setxUnits(units);
                }
                if (line.trim().startsWith("DATA"))break; //inici dades
            }
            //DATA
            while (sf.hasNextLine()){
                line = sf.nextLine();
                if (isComment(line))continue;
                if (line.trim().startsWith("HKL")) break; //start HKL data
                String values[] = line.trim().split("\\s+");
                double t2i = Double.parseDouble(values[0]);
                try {
                    double Iobs = Double.parseDouble(values[1]);
                    double Ical = Double.parseDouble(values[2]);
                    double Ibkg = Double.parseDouble(values[3]);
                    dsObs.addPoint(new DataPoint(t2i,Iobs,0,Ibkg));
                    dsCal.addPoint(new DataPoint(t2i,Ical,0,Ibkg));
                    dsDif.addPoint(new DataPoint(t2i,Iobs-Ical,0));
                }catch(Exception ex) {
                        //vol dir que hem llegit alguna cosa rara
                        log.warning("error reading prf intensity");
                        dsObs.addPoint(new DataPoint(t2i,0,0,0));
                        dsCal.addPoint(new DataPoint(t2i,0,0,0));
                        dsDif.addPoint(new DataPoint(t2i,0,0,0));                        
                }
            }
            dsPRF.addDataSerie(dsObs);
            dsPRF.addDataSerie(dsCal);
            double[] maxminXY = dsDif.getPuntsMaxXMinXMaxYMinY();
            double maxdif = FastMath.max(FastMath.abs(maxminXY[2]), FastMath.abs(maxminXY[3]));
            dsDif.setYOff(-1*((int)maxdif+100));
            dsPRF.addDataSerie(dsDif);
            maxminXY = dsObs.getPuntsMaxXMinXMaxYMinY();
            //HKLs  
            int nhkl=0;
            DataSerie dsHKL = new DataSerie(SerieType.hkl,units,dsPRF);
            dsHKL.serieName = line.substring(3, line.length());
            while (sf.hasNextLine()) {
                line = sf.nextLine();
                if (line.trim().startsWith("HKL")) {
                    //new hkl serie
                    //first we add the current one to the plottable
                    dsHKL.setYOff(maxminXY[3]-maxminXY[3]*(nhkl));
                    dsPRF.addDataSerie(dsHKL);
                    //and create a new one
                    dsHKL = new DataSerie(SerieType.hkl,units,dsPRF);
                    dsHKL.serieName = line.substring(3, line.length());
                    nhkl++;
                    continue;
                }                        
                
                String values[] = line.trim().split("\\s+");
                double t2i = Double.parseDouble(values[0]);
                int h = Integer.parseInt(values[1]);
                int k = Integer.parseInt(values[2]);
                int l = Integer.parseInt(values[3]);
                HKLrefl hkl = new HKLrefl(h,k,l,wave,t2i);
                dsHKL.addPoint(new DataPoint_hkl(hkl,t2i));
            }
            //afegim "ultima" HKL
            dsHKL.setYOff(maxminXY[3]-maxminXY[3]*(nhkl));
            dsPRF.addDataSerie(dsHKL);
            //and create a new one
            dsHKL = new DataSerie(SerieType.hkl,units,dsPRF);
            dsHKL.serieName = line.substring(3, line.length());

        }catch(Exception ex){
            if (D1Dplot_global.isDebug())ex.printStackTrace();
            readed=false;
        }finally {
            if(sf!=null)sf.close();
        }
        
        dsPRF.setWavelengthToAllSeries(wave);
        dsPRF.setZeroToAllSeries(zero);
        
        if (readed){
            return dsPRF;
        }else{
            return null;
        }
    }

    private static Data_Common readPRF(File f){
        boolean readed = true;
        boolean startData = false;
        boolean starthkl = false;
        double previous2t = -100.0;
        int linecount = 0;
        int phases = 0;
        
        //creem un DataSerie_PRF
        Data_Common dsPRF = new Data_Common();
        DataSerie dsObs = new DataSerie(SerieType.obs,Xunits.tth,dsPRF);
        DataSerie dsCal = new DataSerie(SerieType.cal,Xunits.tth,dsPRF);
        DataSerie dsDif = new DataSerie(SerieType.diff,Xunits.tth,dsPRF);
        ArrayList<DataSerie> dsHKL = new ArrayList<DataSerie>();
        ArrayList<DataPoint_hkl> hkls = new ArrayList<DataPoint_hkl>();
        
        //FIRST CHECK ENCODING
        String enc = getEncodingToUse(f);
        
        Scanner sf = null;
        try {
            sf = new Scanner(f,enc);
            while (sf.hasNextLine()){
                String line = sf.nextLine();
                linecount = linecount +1;
                
                if (linecount == 2){
                    String values[] = line.trim().split("\\s+");
                    dsPRF.setOriginalWavelength(Double.parseDouble(values[2]));
                    phases = Integer.parseInt(values[0]);
                    for (int i=0;i<phases;i++) {
                        dsHKL.add(new DataSerie(SerieType.hkl,Xunits.tth,dsPRF));
                    }
                }
                
                if (line.trim().startsWith("2Theta")){
                    startData=true;
                    continue;
                }
                if (!startData)continue;
                
                String values[] = line.trim().split("\\s+");
                double t2i = Double.parseDouble(values[0]);
                
                if (t2i<previous2t){
                    starthkl=true;
                }
                
                if (starthkl){
                    int ini = line.indexOf("(");
                    int fin = line.indexOf(")");
                    log.debug(line.substring(ini+1, fin));
                    String shkl[] = line.substring(ini+1, fin).trim().split("\\s+");
                    int h = Integer.parseInt(shkl[0]);
                    int k = Integer.parseInt(shkl[1]);
                    int l = Integer.parseInt(shkl[2]);
                    HKLrefl hkl = new HKLrefl(h,k,l,dsObs.getWavelength(),t2i);
                    //the y offset (more than one phase)
                    shkl = line.substring(0,ini).trim().split("\\s+");
                    double yoff = Double.parseDouble(shkl[shkl.length-1].trim());
                    hkls.add(new DataPoint_hkl(t2i,yoff,0.,hkl));
                }else{
                    //mirem si hi ha info d'hkl també aquí
                    try {
                        double Iobs = Double.parseDouble(values[1]);
                        double Ical = Double.parseDouble(values[2]);
                        double Ibkg = Double.parseDouble(values[4]);
                        dsObs.addPoint(new DataPoint(t2i,Iobs,0,Ibkg));
                        dsCal.addPoint(new DataPoint(t2i,Ical,0,Ibkg));
                        dsDif.addPoint(new DataPoint(t2i,Iobs-Ical,0));
                        
                        if (values.length>6) {                            //has hkl
                            int ini = line.indexOf("(");
                            int fin = line.indexOf(")");
                            log.debug(line.substring(ini+1, fin));
                            String shkl[] = line.substring(ini+1, fin).trim().split("\\s+");
                            int h = Integer.parseInt(shkl[0]);
                            int k = Integer.parseInt(shkl[1]);
                            int l = Integer.parseInt(shkl[2]);
                            shkl = line.substring(0,ini).trim().split("\\s+");
                            double t2r = Double.parseDouble(shkl[shkl.length-2]);
                            HKLrefl hkl = new HKLrefl(h,k,l,dsObs.getWavelength(),t2r);
                            //the y offset (more than one phase)
                            double yoff = Double.parseDouble(shkl[shkl.length-1].trim());
                            hkls.add(new DataPoint_hkl(t2r,yoff,0.,hkl));
                        }
                    }catch(Exception ex) {
                        //vol dir que hem llegit alguna cosa rara
                        log.warning("error reading prf intensity");
                        dsObs.addPoint(new DataPoint(t2i,0,0,0));
                        dsCal.addPoint(new DataPoint(t2i,0,0,0));
                        dsDif.addPoint(new DataPoint(t2i,0,0,0));                        
                    }
                    previous2t = t2i;
                }
            }
                        
            double[] maxminXY = dsDif.getPuntsMaxXMinXMaxYMinY();
            double maxdif = FastMath.max(FastMath.abs(maxminXY[2]), FastMath.abs(maxminXY[3]));
            dsDif.setYOff(-1*((int)maxdif+100));
            maxminXY = dsObs.getPuntsMaxXMinXMaxYMinY();
            
            dsObs.serieName=f.getName()+" ("+dsObs.getTipusSerie().toString()+")";
            dsCal.serieName=f.getName()+" ("+dsCal.getTipusSerie().toString()+")";
            dsDif.serieName=f.getName()+" ("+dsDif.getTipusSerie().toString()+")";
            dsPRF.addDataSerie(dsObs);
            dsPRF.addDataSerie(dsCal);
            dsPRF.addDataSerie(dsDif);
            //HKLS:
            if (phases<=1) {
                for (DataPoint_hkl hkl:hkls) {
                    hkl.setY(0);
                    dsHKL.get(0).addPoint(hkl);
                }
                dsHKL.get(0).setYOff(maxminXY[3]-DataSerie.def_hklYOff);
            }else {
                //more phases
                int[] vals = new int[phases];
                for (int i=0;i<vals.length;i++) {
                    vals[i]=Integer.MAX_VALUE;
                }
                int nph = 0;
                
                for (DataPoint_hkl hkl:hkls) {
                    boolean newVal=true;
                    int yoff = (int) hkl.getY();
                    for (int i=0;i<vals.length;i++) {
                        if (yoff==vals[i]) {
                            newVal=false;
                            break;
                        }
                    }
                    if (newVal) {
                        vals[nph] = yoff;
                        nph++;
                    }
                    if (nph==phases)break;
                }
                //ara ja tinc els Nph valors de Yoff
                for (DataPoint_hkl hkl:hkls) {
                    for (int i=0;i<vals.length;i++) {
                        int yoff = (int) hkl.getY();
                        if (yoff==vals[i]) {
                            hkl.setY(0);
                            dsHKL.get(i).addPoint(hkl);
                            break;
                        }
                    }
                }
                for (int i=0;i<phases;i++) {
                    dsHKL.get(i).setYOff(maxminXY[3]-maxminXY[3]*(i)); 
                }
            }
            
            //general info
            for (DataSerie ds:dsHKL) {
                ds.serieName=f.getName()+" ("+ds.getTipusSerie().toString()+")";
                dsPRF.addDataSerie(ds);
            }
            dsPRF.setWavelengthToAllSeries(dsPRF.getOriginalWavelength());
            
            
        }catch(Exception ex){
            if (D1Dplot_global.isDebug())ex.printStackTrace();
            readed=false;
        }finally {
            if(sf!=null)sf.close();
        }
        if (readed){
            return dsPRF;
        }else{
            return null;
        }
        
    }
    
    //DONE seria millor que plotpanel tingues un "toString" que t'ho passes tot ja directament
    //TODO lo seu seria guardar TOTES les dades i no dependre de fitxers... seria portable pero podria ocupar bastant...
    // s'hauria de preguntar al guardar (relative paths to files or all packed)
    public static boolean writeProject(File stateFile, PlotPanel p) {
    	
        boolean written = true;
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(stateFile,true)));//        	//guardar axis info, zoom, bounds, etc...
            out.println(p.getVisualParametersToSave());
            out.println("-----------");
            
        	//guardar patterns/series amb tots els seus paràmetres de visualització
            int np = 0;
//        	for (Plottable plt:p.getDataToPlot()) {
            for (int i=0;i<p.getNplottables();i++) {
                Plottable plt = p.getPlottable(i);
                String filePath = plt.getFile().getAbsolutePath();
                out.println(String.format("%d %s", np, filePath));
                int nd = 0;
        	    for(DataSerie d:plt.getDataSeries()) { //TODO cal refer. posar el tipus. etc.. o llegir de nou al carregar
                    String nam = d.serieName;
                    String typ = d.getTipusSerie().toString();
                    String col = Integer.toString(d.color.getRGB());
                    String sca = FileUtils.dfX_4.format(d.getScale());
                    String zof = FileUtils.dfX_5.format(d.getZerrOff());
                    String wav = FileUtils.dfX_5.format(d.getWavelength());
                    String xun = d.getxUnits().getName();
                    String yof = FileUtils.dfX_3.format(d.getYOff());
                    String mar = FileUtils.dfX_2.format(d.markerSize);
                    String lin = FileUtils.dfX_2.format(d.lineWidth);
                    String err = Boolean.toString(d.showErrBars);
                    String plo = Boolean.toString(d.plotThis);
                    
                    if (d.getTipusSerie()==SerieType.bkg) {
                        //TODO: cal guardarla tota
                    }
                    if (d.getTipusSerie()==SerieType.ref) {
                        //TODO: cal guardarla tota
                        
                    }
                    
                    out.println(String.format("%d %s %s", nd, typ, nam)); //el nom podria tenir espais!! per aixo el deixo com a ultim
                    out.println(String.format("%s %s %s %s %s %s %s %s %s %s", col,sca,zof,wav,xun,yof,mar,lin,err,plo));
                    
        	        nd++;
        	    }
        	    np++;
                out.println("-----------");
        	}
                        
        }catch(Exception ex) {
        	if (D1Dplot_global.isDebug())ex.printStackTrace();
        	written = false;
        }finally {
            if(out!=null)out.close();
        }
    	return written;
    }

    public static boolean readProject(File stateFile, PlotPanel p, D1Dplot_main m) {
        boolean readed = true;
        Scanner sf = null;
        try {
            sf = new Scanner(stateFile);

            //4 primere linies fixes
            String line = sf.nextLine();
            String[] vals1 = line.trim().split("\\s+");

            line = sf.nextLine();
            String[] vals2 = line.trim().split("\\s+");

            String xlabel = sf.nextLine();
            String ylabel = sf.nextLine();

            //ara els patterns series
            Plottable currentPlottable=null;
            while (sf.hasNextLine()){
                line = sf.nextLine();
                log.debug(line);
                if (line.startsWith("---")) {
                    //new pattern if nextline
                    if (sf.hasNextLine()) {
                        line = sf.nextLine();	
                        log.debug(line);
                        if (line.trim().isEmpty())break;
                        //                		vals = line.trim().split("\\s+");
                        //                		int npat = Integer.parseInt(vals[0]);
                        String fname = line.substring(line.indexOf(" "));
                        log.debug(fname);
                        currentPlottable=DataFileUtils.readPatternFile(new File(fname.trim()));
                        p.addPlottable(currentPlottable);
                        continue; //seguim llegint les dataseries
                    }else {
                        break;
                    }
                }
                if (line.trim().isEmpty())continue;

                //aqui estem dins un pattern
                if (currentPlottable!=null) {
                    String[] vals = line.trim().split("\\s+");
                    SerieType styp = SerieType.getEnum(vals[1]);
                    if (styp==null)styp=SerieType.dat;
                    String dsname = String.join(" ", Arrays.asList(vals).subList(2, vals.length));

                    //get the serie type of the currentPlottable
                    for (DataSerie ds:currentPlottable.getDataSeries()) {
                        if (ds.getTipusSerie()==styp) {
                            //es aquesta
                            ds.serieName=dsname.trim();
                            //seguent linia
                            line = sf.nextLine();
                            log.debug(line);
                            vals = line.trim().split("\\s+");
                            
                            ds.color=FileUtils.getColor(Integer.parseInt(vals[0]));
                            ds.setScale(Float.parseFloat(vals[1]));
                            ds.setZerrOff(Double.parseDouble(vals[2]));
                            ds.setWavelength(Double.parseDouble(vals[3]));
                            ds.setYOff(Double.parseDouble(vals[5]));
                            ds.markerSize=Float.parseFloat(vals[6]);
                            ds.lineWidth=Float.parseFloat(vals[7]);
                            ds.showErrBars=Boolean.parseBoolean(vals[8]);
                            ds.plotThis=Boolean.parseBoolean(vals[9]);
                            
                        }
                    }
                }
            }

            
            m.updateData(true,true);
            m.showTableTab();

            p.setVisualParametersFromSaved(vals1, vals2, xlabel, ylabel);

        }catch(Exception ex) {
            if (D1Dplot_global.isDebug())ex.printStackTrace();
            readed = false;
        }finally {
            if(sf!=null)sf.close();
        }
        
        return readed;
    }
    
//  #TITOL
//  name=XXX
//  cell=
//  sg=
//  wave=
//  zero=
//  units=tth
//  aleshores llista:
//  DATA
//  tth Yobs Ycal Ybkg
//
//  seguit de bloc(S) hkl (poden haver-hi varis)
//  HKL name
//  tth h k l
  public static File writeProfileFile(File d1File, DataSerie dsOBS, DataSerie dsCAL, List<DataSerie> dsHKL, boolean overwrite) {
      log.debug(d1File.toString());
      if (d1File.exists()&&!overwrite)return null;
      if (d1File.exists()&&overwrite)d1File.delete();
      
      PrintWriter out = null;
      try {
          out = new PrintWriter(new BufferedWriter(new FileWriter(d1File,true)));
          out.println("#d1Dplot pattern matching obs/calc/hkl data");//TODO print comments?
          out.println("name="+dsOBS.serieName);
          out.println(String.format("cell=%s",""));
          out.println(String.format("sg=%s",""));
          out.println(String.format("wave=%.5f",dsOBS.getWavelength()));
          out.println(String.format("zero=%.5f",dsOBS.getZerrOff()));
          out.println(String.format("units=%s",dsOBS.getxUnits().getName()));
          out.println("DATA");
          for (int i=0;i<dsOBS.getNpoints();i++) {
              double t2 = dsOBS.getPointWithCorrections(i, 0, 0, 1.0, false).getX();//no corrections
              double yobs = dsOBS.getPointWithCorrections(i, 0, 0, 1.0, false).getY();//no corrections
              double ycal = dsCAL.getPointWithCorrections(i, 0, 0, 1.0, false).getY();//no corrections
              double ybkg = dsOBS.getPointWithCorrections(i, 0, 0, 1.0, false).getYbkg();//no corrections
              out.println(String.format(" %10.7e  %10.7e  %10.7e  %10.7e", t2,yobs,ycal,ybkg));
          }
          for (DataSerie ds:dsHKL) {
              out.println(String.format("HKL %s",ds.serieName));
              for (int i=0;i<ds.getNpoints();i++) {
                  out.println(String.format(" %10.7e  %s", ds.getPointWithCorrections(i, 0, 0, 1.0,false).getX(),ds.getPointWithCorrections(i, 0, 0, 1.0,false).getInfo()));
              }
          }
      } catch (Exception ex) {
          ex.printStackTrace();
      }finally {
          if(out!=null)out.close();
      }
      
      return d1File;
  }
    
    private static boolean writeBKG(DataSerie ds, int npoints, File outf, boolean overwrite){
        if (outf.exists()&&!overwrite)return false;
        if (outf.exists()&&overwrite)outf.delete();
        
        boolean written = true;
        
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(outf,true)));            //primer escribim els comentaris
            Iterator<String> itrComm = ds.getCommentLines().iterator();
            while (itrComm.hasNext()){
                String cline = itrComm.next();
                out.println(cline);
            }
            //ara posem una linia comentari amb les wavelengths
            if (FastMath.abs(ds.getWavelength()-ds.getOriginalWavelength())>0.01){
                out.println("# wavelength="+FileUtils.dfX_4.format(ds.getWavelength())+" (originalWL="+FileUtils.dfX_4.format(ds.getOriginalWavelength())+")");                
            }

            if (FastMath.abs(ds.getZerrOff())>0.01f){
                out.println("# zeroOffsetApplied="+FileUtils.dfX_3.format(ds.getZerrOff()));
            }
            if (ds.getScale()>1.05 || ds.getScale()<0.95){
                out.println("# scaleFactorApplied="+FileUtils.dfX_2.format(ds.getScale()));
            }
            
            if (ds.getNpoints()<npoints){
                npoints = ds.getNpoints();
            }
            out.println(String.format("# %d BACKGROUND POINTS:", npoints));
            
            double minX = ds.getMinX();
            double maxX = ds.getMaxX();
            double stepX = (maxX-minX)/npoints;
            
            double currX = minX;
            int nwritten = 0;
            while(currX<=maxX){
                Plottable_point[] dps = ds.getSurroundingDPs(currX);
                if (dps!=null){
                    if (dps.length==2){
                        if ((dps[0]!=null)&&(dps[1]!=null)){
                            double currY = ds.interpolateY(currX,dps[0],dps[1]);
                            String towrite = String.format(" %10.7e  %10.7e  %10.7e",currX,currY,0.0);
                            towrite = towrite.replace(",", ".");
                            out.println(towrite);
                            nwritten = nwritten + 1;
                        }
                    }
                }
                currX = currX + stepX;
            }
            if (nwritten!=npoints){
                log.info(String.format("# %d BACKGROUND POINTS WRITTEN (expected %d)", nwritten,npoints));                
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            written = false;
        }finally {
            if(out!=null)out.close();
        }
        return written;
    }
    
    private static boolean writeDAT_ALBA(DataSerie ds, File outf, boolean overwrite, boolean addYbkg){
        if (outf.exists()&&!overwrite)return false;
        if (outf.exists()&&overwrite)outf.delete();
        
        boolean written = true;
        
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(outf,true)));            //primer escribim els comentaris
            Iterator<String> itrComm = ds.getCommentLines().iterator();
            while (itrComm.hasNext()){
                String cline = itrComm.next();
                out.println(cline);
            }
            //ara posem una linia comentari amb les wavelengths
            if (FastMath.abs(ds.getWavelength()-ds.getOriginalWavelength())>0.01){
                out.println("# wavelength="+FileUtils.dfX_4.format(ds.getWavelength())+" (originalWL="+FileUtils.dfX_4.format(ds.getOriginalWavelength())+")");                
            }

            if (FastMath.abs(ds.getZerrOff())>0.01f){
                out.println("# zeroOffsetApplied="+FileUtils.dfX_3.format(ds.getZerrOff()));
            }
            if (ds.getScale()>1.05 || ds.getScale()<0.95){
                out.println("# scaleFactorApplied="+FileUtils.dfX_2.format(ds.getScale()));
            }
            
            for (int i=0; i<ds.getNpoints();i++){
                Plottable_point pp = ds.getPointWithCorrections(i,addYbkg);
                String towrite = String.format(" %10.7e  %10.7e  %10.7e",pp.getX(),pp.getY(),pp.getSdy());
                towrite = towrite.replace(",", ".");
                out.println(towrite);
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            written = false;
        }finally {
            if(out!=null)out.close();
        }
        return written;
    }
    
    private static boolean writeGR(DataSerie ds, File outf, boolean overwrite, boolean addYbkg){
        if (outf.exists()&&!overwrite)return false;
        if (outf.exists()&&overwrite)outf.delete();
        
        boolean written = true;
        
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(outf,true)));            //primer escribim els comentaris
            Iterator<String> itrComm = ds.getCommentLines().iterator();
            String com_serie = "#S 1";
            String com_label = "#L r($\\AA$)  G($\\AA^{-2}$)";
            while (itrComm.hasNext()){
                String cline = itrComm.next();
                if (cline.startsWith("#S")){
                    com_serie = cline;
                    continue;
                }
                if (cline.startsWith("#L")){
                    com_label = cline;
                    continue;
                }
                out.println(cline);
            }
            
            out.println("#### start data");
            out.println(com_serie);
            out.println(com_label);
            
            for (int i=0; i<ds.getNpoints();i++){
                Plottable_point pp = ds.getPointWithCorrections(i,addYbkg);
                String towrite = String.format("%.3f %.7f",pp.getX(),pp.getY());
                towrite = towrite.replace(",", ".");
                out.println(towrite);
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            written = false;
        }finally {
            if(out!=null)out.close();
        }
        return written;
    }
    
    private static boolean writeDAT_FreeFormat(DataSerie ds, File outf, boolean overwrite, boolean addYbkg){
        if (outf.exists()&&!overwrite)return false;
        if (outf.exists()&&overwrite)outf.delete();
        boolean written = true;
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(outf,true)));            //TODO AFFEGIR MES COMENTARIS?
            StringBuilder comments = new StringBuilder();
            comments.append("#");
            if (ds.getCommentLines().size()>0){
                comments.append(ds.getCommentLines().get(0));    
            }
            
            if (FastMath.abs(ds.getWavelength()-ds.getOriginalWavelength())>0.01){
                comments.append(String.format(" wavelength=%8.4f (originalWL=%8.4f",ds.getWavelength(),ds.getOriginalWavelength()));
            }

            if (FastMath.abs(ds.getZerrOff())>0.01f){
                comments.append(" zeroOffsetApplied="+FileUtils.dfX_3.format(ds.getZerrOff()));
            }

            if (ds.getScale()>1.05 || ds.getScale()<0.95){
                comments.append("scaleFactorApplied="+FileUtils.dfX_2.format(ds.getScale()));
            }
            
            String towrite = String.format(" %10.7e %10.7e %10.7e %s", FileUtils.round(ds.getMinX(),5),FileUtils.round(ds.calcStep(),5),FileUtils.round(ds.getMaxX(),5),comments.toString());
            towrite = towrite.replace(",", ".");
            out.println(towrite);
            
            
            //
            double[] intensities = new double[10];
            int ii = 0;
            for (int i=0;i<ds.getNpoints();i++){
                intensities[ii] = ds.getPointWithCorrections(i,addYbkg).getY();
                ii++;
                if (ii==10 || i==ds.getNpoints()-1){
                    //escribim linia
                    StringBuilder towr = new StringBuilder();
                    for (int j=0;j<ii;j++){
                        towr.append(String.format("%10.7e ", intensities[j]));
                    }
                    towrite = towr.toString().trim();
                    towrite = towrite.replace(",", ".");
                    out.println(towrite);
                    ii = 0;
                }

            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            written = false;
        }finally {
            if(out!=null)out.close();
        }
        return written;
    }
    
    private static boolean writeGSA(DataSerie ds, File outf, boolean overwrite, boolean addYbkg){
        if (outf.exists()&&!overwrite)return false;
        if (outf.exists()&&overwrite)outf.delete();
        boolean written = true;
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(outf,true)));
            double maxY = ds.getPuntsMaxXMinXMaxYMinY()[2];
            double top = 1.;
            String warn_msg = "";
            if (maxY>1e5){
                top = (int)(maxY/1e5);
                log.warning("Counts devided by 10 to avoid overflow");
                warn_msg = warn_msg + " -- !!!  WARNING : Counts devided by 10 to avoid overflow ";
                top = 10.*top;
            }
            out.println("# "+ds.serieName+" "+ds.getCommentLines().get(0)+" "+warn_msg);
            out.println("Instrument parameter      bl04.prm ");

            int npts = ds.getNpoints();
            int nrec = (int)((npts - (npts%5))/5.);
            if (npts%5!=0)nrec = nrec +1;
//            BANK 1   20951    4191 CONST    60.000     0.600  0.0 0.0 ESD                   
//            if (ds.getStep()<0)ds.setStep(ds.calcStep());
            String linegsa=String.format("BANK 1 %7d %7d CONST %9.3f %9.3f  0.0 0.0 ESD ",npts,nrec,ds.getPointWithCorrections(0,addYbkg).getX()*100.0,ds.calcStep()*100.0);
            out.println(linegsa);
            
            int startIndex = 0;
            int endIndex = ds.getNpoints()-1;
            
            while (startIndex <= endIndex-4){
                try{
                    String towrite = String.format("%8.1f%8.1f%8.1f%8.1f%8.1f%8.1f%8.1f%8.1f%8.1f%8.1f",ds.getPointWithCorrections(startIndex,addYbkg).getY()/top, ds.getPointWithCorrections(startIndex,addYbkg).getSdy()/top,
                            ds.getPointWithCorrections(startIndex+1,addYbkg).getY()/top, ds.getPointWithCorrections(startIndex+1,addYbkg).getSdy()/top,
                            ds.getPointWithCorrections(startIndex+2,addYbkg).getY()/top, ds.getPointWithCorrections(startIndex+2,addYbkg).getSdy()/top,
                            ds.getPointWithCorrections(startIndex+3,addYbkg).getY()/top, ds.getPointWithCorrections(startIndex+3,addYbkg).getSdy()/top,
                            ds.getPointWithCorrections(startIndex+4,addYbkg).getY()/top, ds.getPointWithCorrections(startIndex+4,addYbkg).getSdy()/top);
                    out.println(towrite);
                    startIndex+=5;
                }catch(Exception ex){
                    ex.printStackTrace();
                    log.debug("error writting gsa");
                    written = false;
                    break;
                }
            }
//            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            written = false;
        }finally {
            if(out!=null)out.close();
        }
        return written;
    }
    
    private static boolean writeASC(DataSerie ds, File outf, boolean overwrite, boolean addYbkg){
        if (outf.exists()&&!overwrite)return false;
        if (outf.exists()&&overwrite)outf.delete();
        boolean written = true;
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(outf,true)));
            for (int i=0; i<ds.getNpoints(); i++){
                Plottable_point pp = ds.getPointWithCorrections(i,addYbkg);
                String s = String.format("%.6f  %.2f", pp.getX(),pp.getY());
                s = s.replace(",", ".");
                out.println(s);                  
            }
                        
        } catch (Exception ex) {
            ex.printStackTrace();
            written = false;
        }finally {
            if(out!=null)out.close();
        }
        return written;
    }
    
    //option list2t is to put the positionList and avoid error in 2theta when only ini and fin are given
    private static boolean writeXRDML(DataSerie ds, File outf,boolean list2T,boolean overwrite, boolean addYbkg){
        if (outf.exists()&&!overwrite)return false;
        if (outf.exists()&&overwrite)outf.delete();
        boolean written = true;
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(outf,true)));
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.println("<xrdMeasurements xmlns=\"http://www.xrdml.com/XRDMeasurement/1.3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.xrdml.com/XRDMeasurement/1.3 http://www.xrdml.com/XRDMeasurement/1.3/XRDMeasurement.xsd\" status=\"Completed\">");
            out.println("    <xrdMeasurement measurementType=\"Scan\" status=\"Completed\">");
            if (ds.getWavelength()>0){
                out.println("        <usedWavelength intended=\"K-Alpha 1\">");
                out.println("            <kAlpha1 unit=\"Angstrom\">"+ds.getWavelength()+"</kAlpha1>");
                out.println("            <kAlpha2 unit=\"Angstrom\">"+ds.getWavelength()+"</kAlpha2>");
                out.println("            <kBeta unit=\"Angstrom\">"+ds.getWavelength()+"</kBeta>");
                out.println("            <ratioKAlpha2KAlpha1>1.0000</ratioKAlpha2KAlpha1>");
                out.println("        </usedWavelength>");
            }else{
                log.warning("No wavelength given. It will not be written on the XRDML file");
            }
            out.println("        <scan appendNumber=\"0\" mode=\"Continuous\" scanAxis=\"Gonio\" status=\"Completed\">");
            out.println("            <header>");
            out.println("                <startTimeStamp>"+D1Dplot_global.fHora.format(new Date())+"</startTimeStamp>");
            out.println("                <endTimeStamp>"+D1Dplot_global.fHora.format(new Date())+"</endTimeStamp>");
            out.println("                <author>");
            out.println("                    <name>ALBA_MSPD</name>");
            out.println("                </author>");
            out.println("                <source>");
            out.println("                    <applicationSoftware>None</applicationSoftware>");
            out.println("                    <instrumentControlSoftware>None</instrumentControlSoftware>");
            out.println("                    <instrumentID>0000000000000000</instrumentID>");
            out.println("                </source>");
            out.println("            </header>");
            
            out.println("            <dataPoints>");
            out.println("                <positions axis=\"2Theta\" unit=\"deg\">");
            if(list2T){//list positions
                StringBuilder sb2T = new StringBuilder();
                for (int i=0; i<ds.getNpoints(); i++){
                    double t2 = ds.getPointWithCorrections(i,addYbkg).getX();
                    String towrite = String.format("%10.7e ", t2);  
                    towrite = towrite.replace(",", ".");
                    sb2T.append(towrite);
                }
                out.println("                <listPositions>"+sb2T.toString().trim()+"</listPositions>");                
            }else{
                out.println("                <startPosition>"+ds.getMinX()+"</startPosition>");
                out.println("                <endPosition>"+ds.getMaxX()+"</endPosition>");                
            }
            out.println("                </positions>");
            out.println("                <commonCountingTime unit=\"seconds\">100.0</commonCountingTime>");
            StringBuilder sbInt = new StringBuilder();
            
            for (int i=0; i<ds.getNpoints(); i++){
                double inten = ds.getPointWithCorrections(i,addYbkg).getY();
                int iint = (int) Math.round(inten);
                sbInt.append(Integer.toString(iint)+" ");
            }
            out.println("                 <intensities unit=\"counts\">"+sbInt.toString().trim()+"</intensities>");
            out.println("            </dataPoints>");
            out.println("        </scan>");
            out.println("    </xrdMeasurement>");
            out.println("</xrdMeasurements>");
            
        } catch (Exception ex) {
            ex.printStackTrace();
            written = false;
        }finally {
            if(out!=null)out.close();
        }
        return written;
    }
    
    private static boolean writePeaksTXT(DataSerie ds, File outf, boolean overwrite){
	    if (outf.exists()&&!overwrite)return false;
	    if (outf.exists()&&overwrite)outf.delete();
	    boolean written = true;
	    PrintWriter out = null;
	    try {
	        out = new PrintWriter(new BufferedWriter(new FileWriter(outf,true)));
	        Iterator<String> itrComm = ds.getCommentLines().iterator();
	        while (itrComm.hasNext()){
	            String cline = itrComm.next();
	            if (!cline.startsWith("#"))cline = "# "+cline;
	            out.println(cline);
	        }
	        out.println("# file generated with d1Dplot");
	        //ARA ELS PICS
	        for(int i=0;i<ds.getNpoints();i++){
	            Plottable_point pp = ds.getPointWithCorrections(i,false); //no bkg in this case
	            String s = String.format(" %12.5f %15.3f %15.3f",pp.getX(),pp.getY(),pp.getSdy());
	            s = s.replace(",", ".");
	            out.println(s);
	        }
	        
	    } catch (Exception ex) {
	        if (D1Dplot_global.isDebug())ex.printStackTrace();
	        written = false;
	    }finally {
            if(out!=null)out.close();
        }
	    return written;
	    
	}


	private static DicvolDialog dvdiag; //el faig estatic perque es guardi els anteriors parametres
	private static boolean writePeaksDIC(DataSerie ds, File outf, boolean overwrite){
	    if (outf.exists()&&!overwrite)return false;
	    if (outf.exists()&&overwrite)outf.delete();
	    
	    //preguntem els parametres
	    if (dvdiag==null){
	        dvdiag = new DicvolDialog(null,ds);
	        dvdiag.visible(true);
	    }else{
	        dvdiag.updateDS(ds);
	        dvdiag.visible(true);
	    }
	    
	    
	    //now we write
	    if (!dvdiag.isEverythingOK()){
	        log.warning("Error reading dicvol parameters");
	        return false;
	    }
	    
	    boolean written = true;
	    try {
	        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outf,true)));
	        
	        Iterator<String> itrComm = ds.getCommentLines().iterator();
	        int ncom = 0;
	        while (itrComm.hasNext()){
	            String cline = itrComm.next();
	            if (ncom>=2){
	                if (!cline.startsWith("!"))cline = "! "+cline;
	            }else{
	                if (!cline.startsWith("#"))cline = "# "+cline;    
	            }
	            out.println(cline);
	            ncom++;
	        }
	        out.println("# file generated with d1Dplot");
	        int xun = 2;
	        switch(ds.getxUnits()){
	            case tth:
	                xun = 2;
	                break;
	            case Q:
	                xun = 4;
	                break;
	            case dsp:
	                xun = 3;
	                break;
	            case dspInv:
	                log.warning("1/dsp² not supported by dicvol, please change xunits");
	                break;
	            default:
	                xun = 2;
	                break;
	        }
	        //N,ITYPE,JC,JT,JH,JO,JM,JTR
	        String s = String.format("%4d %2d %2d %2d %2d %2d %2d %2d",dvdiag.getNpeaks(), xun, dvdiag.isCubicInt(), dvdiag.isTetraInt(), dvdiag.isHexaInt(), dvdiag.isOrtoInt(), dvdiag.isMonoInt(), dvdiag.isTricInt());
	        s = s.replace(",", ".");
	        out.println(s);
	        //AMAX,BMAX,CMAX,VOLMIN,VOLMAX,BEMIN,BEMAX
	        s = String.format("  %.2f %.2f %.2f %.2f %.2f %.2f %.2f", dvdiag.getAmax(),dvdiag.getBmax(),dvdiag.getCmax(),dvdiag.getVmin(),dvdiag.getVmax(),dvdiag.getBetamin(),dvdiag.getBetamax());
	        s = s.replace(",", ".");
	        out.println(s);
	        //WAVE,POIMOL,DENS,DELDEN 
	        s = String.format("  %.5f %.2f %.2f %.2f", dvdiag.getWavel(), dvdiag.getMw(),dvdiag.getDensity(),dvdiag.getDensityerr());
	        s = s.replace(",", ".");
	        out.println(s);
	        //EPS,FOM,N_IMP,ZERO_S,ZERO_REF,OPTION 
	        s = String.format("  %.2f %.2f %d %d %d %d", dvdiag.getEps(),dvdiag.getMinfom(),dvdiag.getSpurious(),dvdiag.isZeroRefInt(),dvdiag.isPrevzeroInt(),dvdiag.isDic06Int());
	        s = s.replace(",", ".");
	        out.println(s);
	        
	        //ARA ELS PICS
	        for(int i=0;i<dvdiag.getNpeaks();i++){
                Plottable_point pp = ds.getPointWithCorrections(i,false); //no bkg in this case
	            s = String.format(" %12.5f %15.2f %15.2f",pp.getX(), pp.getY(), pp.getSdy());
	            s = s.replace(",", ".");
	            out.println(s);
	        }
	        
	        out.close();
	    } catch (Exception ex) {
	        if (D1Dplot_global.isDebug())ex.printStackTrace();
	        written = false;
	    }
	    return written;
	}
    
    private static double searchForWavel(String s){
        double wl = -1;
        if (FileUtils.containsIgnoreCase(s, "wave")){
            //primer provem espais, seguent element
            try{
                String[] vals = s.split("\\s+");
                for (int i=0;i<vals.length;i++){
                    if (FileUtils.containsIgnoreCase(vals[i], "wave")){
                        wl = Double.parseDouble(vals[i+1]);
                        //si arribem aqui vol dir que s'ha fet be
                        return wl;
                    }
                }
            }catch(Exception ex){
                log.debug("error parsing element after wave tag");
            }
            
            //provem amb signe igual
            try{
                String[] vals = s.split("=");
                for (int i=0;i<vals.length;i++){
                    if (FileUtils.containsIgnoreCase(vals[i], "wave")){
                        
                        String[] vals2 = vals[i+1].split("\\s+");
                        if (FileUtils.containsIgnoreCase(vals2[0], "A")){
                            wl = Double.parseDouble(vals2[0].substring(0, vals2[0].length()-1));
                        }else{
                            wl = Double.parseDouble(vals2[0]);   
                        }
                        //si arribem aqui vol dir que s'ha fet be
                        return wl;
                    }
                }
            }catch(Exception ex){
                log.debug("error parsing element after wave*= tag");
            }
        }
        
        
        return wl;
    }

	private static boolean isComment(String ln){
	    if (ln.trim().startsWith("#"))return true;
	    if (ln.trim().startsWith("!"))return true;
	    if (ln.trim().startsWith("/"))return true;
	    if (ln.trim().startsWith("$"))return true;
	    return false;
	}

}

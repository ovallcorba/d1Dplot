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

import com.vava33.BasicPlotPanel.core.Plottable_point;
import com.vava33.BasicPlotPanel.core.SerieType;
import com.vava33.cellsymm.HKLrefl;
import com.vava33.d1dplot.D1Dplot_data;
import com.vava33.d1dplot.D1Dplot_global;
import com.vava33.d1dplot.D1Dplot_main;
import com.vava33.d1dplot.DicvolDialog;
import com.vava33.d1dplot.XRDPlotPanelFrontEnd;
import com.vava33.d1dplot.data.DataPoint;
import com.vava33.d1dplot.data.DataPoint_hkl;
import com.vava33.d1dplot.data.DataSerie;
import com.vava33.d1dplot.data.DataSet;
import com.vava33.d1dplot.data.Xunits;
import com.vava33.jutils.VavaLogger;
import com.vava33.jutils.FileUtils;
import com.vava33.jutils.Options;

public final class DataFileUtils {

    private static final String className = "DataFileUtils";
    private static VavaLogger log = D1Dplot_global.getVavaLogger(className);    
    
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
    
    public static enum SupportedReadExtensions {DAT,XYE,XY,ASC,GSA,XRDML,FF,D1P,PRF,GR,REF,RAW,TXT;}
    public static enum SupportedWriteExtensions {DAT,XYE,ASC,GSA,XRDML,GR,FF,REF;}
    
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
        XRDformatInfo.put("dat", new FileFormat(new String[]{"dat","DAT"},"2 or 3 columns: 2th/int/(err) with header (.dat)"));
        XRDformatInfo.put("xye", new FileFormat(new String[]{"xye","XYE"},"3 columns: 2th/int/err (*.xye)"));
        XRDformatInfo.put("xy", new FileFormat(new String[]{"xy","XY"},"2 columns: 2th/int (*.xy)"));
        XRDformatInfo.put("asc", new FileFormat(new String[]{"asc","ASC"},"2 columns: 2th/int with no headers (*.asc)"));
        XRDformatInfo.put("gsa", new FileFormat(new String[]{"gsa","GSA"},"GSAS Standard Powder Data File (*.gsa)"));
        XRDformatInfo.put("xrdml", new FileFormat(new String[]{"xrdml","XRDML"},"Panalytical format (*.xrdml)"));
        XRDformatInfo.put("ff", new FileFormat(new String[]{"ff","FF"},"List of intensities in free format (*.ff)"));
        XRDformatInfo.put("d1p", new FileFormat(new String[]{"d1p","D1P"},"Obs,calc and difference profiles from d1Dplot (*.d1p)"));
        XRDformatInfo.put("prf", new FileFormat(new String[]{"prf","PRF"},"Obs,calc and difference profiles from fullprof (*.prf)"));
        XRDformatInfo.put("gr", new FileFormat(new String[]{"gr","GR"},"g(r) from pdfgetx3 (*.gr)"));
        XRDformatInfo.put("txt", new FileFormat(new String[]{"txt","TXT","raw","RAW"},"2 columns space or comma separated (*.txt)"));
        XRDformatInfo.put("ref", new FileFormat(new String[]{"ref","REF"},"1 or 2 columns: X/relative intensity) with X=2th or dsp (*.ref)"));
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
        filter[filter.length-1] = new FileNameExtensionFilter("All 1D-XRD supported formats", frmStrings.toArray(new String[frmStrings.size()]));
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
    public static DataSet readPatternFile(File d1file, SupportedReadExtensions format) {
        DataSet p;
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
            case REF:
                p = readREF(d1file);
                break;
            default:
                p = readUNK(d1file); //TXT
                break;

        }
        if (p==null){
            //ho tornem a intentar...
            p = readUNK(d1file);
            if (p==null) {
                log.warning("Error reading pattern "+d1file.getAbsolutePath());
                return null;               
            }
        }
        if (p.getFile() == null) {
            p.setFile(d1file);
        }
        
        if (p.getOriginalWavelength()<=0) {
            log.info("Wavelength not found in file header");
        }
        
        return p;
    }
    
    //Autodetect format from extension or ask
    public static DataSet readPatternFile(File d1file) {
        // comprovem extensio
        String ext = FileUtils.getExtension(d1file).trim();

        // this line returns the FORMAT in the ENUM or NULL
        SupportedReadExtensions format = FileUtils.searchEnum(SupportedReadExtensions.class, ext);
        
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
            case XYE:
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
            case REF:
                written = writeREF(serie,d1File,overwrite,addYbkg);
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
        String ext = FileUtils.getExtension(d1File).trim();

        //DETECCIO SERIE BKG
        if(serie.getSerieType()==SerieType.bkg){
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
        boolean written = false;
        File fout = null;
        if (D1Dplot_global.release) { //a la release no posem res del dicvol
            written = writePeaksTXT(pksDS,d1File,overwrite);
        }else {
            SupportedWritePeaksFormats[] possibilities = SupportedWritePeaksFormats
                    .values();
            SupportedWritePeaksFormats s = (SupportedWritePeaksFormats) JOptionPane
                    .showInputDialog(null, "Output format:", "Save Peaks",
                            JOptionPane.PLAIN_MESSAGE, null, possibilities,
                            possibilities[0]);
            if (s == null) {
                return null;
            }
            
            
            switch (s) {
                case DIC:
                    d1File = FileUtils.canviExtensio(d1File, "dic");
                    written = writePeaksDIC(pksDS,d1File,overwrite);
                    break;
                case TXT:
                    written = writePeaksTXT(pksDS,d1File,overwrite);
                    break;
            }
        }
        if (written) fout = d1File;
        return fout;
    }
    
    private static DataSet readXYE(File f){
        return readDAT(f);
    }
    
    private static DataSet readASC(File f){
        return readDAT(f);
    }
    
    private static DataSet readREF(File f){
        DataSet dc =  readDAT(f);
        dc.getDataSerie(0).setSerieType(SerieType.ref);
        
        //normalitzem intensitats a 100
        dc.getDataSerie(0).normalizeIntensitiesToValue(100);
        
        //mirem si hi ha un nom als comentaris #name=XXXX
        List<String> com = dc.getCommentLines();
        String name="";
        for (String s:com) {
            name = searchForName(s);
            if (!name.isEmpty()) {
                dc.getDataSerie(0).setName(name);
                break;
            }
        }
        
        
        //TODO:
        // 1) preguntar si es en t2 o d-spacing i posar-ho a x-units, ho podem detectar, normalment d-spacing decreix i tth creix
        // 2) en cas dsp demanar si es vol transformar
        try {
            if (dc.getDataSerie(0).getNPoints()>=3) {
                double diff = dc.getDataSerie(0).getCorrectedPoint(2, false).getX()-dc.getDataSerie(0).getCorrectedPoint(1, false).getX();
                if (diff<=0) { // (segur que no es tth... suposem dsp)
                    dc.getDataSerie(0).setxUnits(Xunits.dsp);
                    log.info("REF file with d-spacing values? please check X-units.");
                }else {
                    log.info("REF file with 2-theta values? please check X-units.");
                }
            }
        }catch (Exception ex) {
            log.warning("Unable to detect X-units for REF serie");
        }
        return dc;
    }
    
    //only 1 serie
    private static DataSet readUNK(File datFile ) {
    	//primer mirem si es free format
    	if (detectFreeFormat(datFile)) {
    		return readFF(datFile);
    	}
    	//es poden afegir altres comprovacions...
    	
    	//Sino mirem de detectar linies on hi hagi dos valors t2, intensitat separats per espai, coma, etc...
        boolean readed = true;
        boolean firstLine=true;
        //creem un DataSerie_Pattern
        DataSet dsP = new DataSet();
        DataSerie ds = new DataSerie(SerieType.dat,Xunits.tth,dsP);

        //FIRST CHECK ENCODING
        String enc = FileUtils.getEncodingToUse(datFile);
        Scanner sf = null;
        try {
            sf = new Scanner(datFile,enc);
            while (sf.hasNextLine()){
                String line = sf.nextLine();
                if (firstLine) {
                    firstLine=false;
                    if (!line.isEmpty()) {
                        if ((int)line.charAt(0)==65279) { //BOM character
                            line = line.substring(1);
                        }
                    }
                }
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
                if (values.length<2){
                    values = line.trim().split("\\s+");
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
//                    log.fine("error parsing sdev");
                }

                ds.addPoint(new DataPoint(t2,inten,sdev,ds));

            }
            ds.setName(datFile.getName());
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

   
    //only 1 serie
    private static DataSet readDAT(File datFile) { 
        boolean firstLine = true;
        boolean readed = true;
        
        //creem un DataSerie_Pattern
        DataSet dsP = new DataSet();
        DataSerie ds = new DataSerie(SerieType.dat,Xunits.tth,dsP);
        
        //FIRST CHECK ENCODING
        String enc = FileUtils.getEncodingToUse(datFile);
        Scanner sf = null;

        try {
            sf = new Scanner(datFile,enc);
            while (sf.hasNextLine()){
                String line = sf.nextLine();
                if (firstLine) {
                    firstLine=false;
                    if (!line.isEmpty()) {
                        if ((int)line.charAt(0)==65279) { //BOM character
                            line = line.substring(1);
                        }
                    }
                }
                if (isComment(line.trim())){
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
//                    log.fine("error parsing sdev");
                }

                ds.addPoint(new DataPoint(t2,inten,sdev,ds));

            }
            if (ds.getNPoints()<=0)return null;
            ds.setName(datFile.getName());
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
    
    
    private static DataSet readGR(File datFile) {
        boolean readed = true;
        boolean firstLine=true;
        //creem un DataSerie_Gr
        DataSet dsGr = new DataSet();
        DataSerie ds = new DataSerie(SerieType.gr,Xunits.G,dsGr);
        
        //FIRST CHECK ENCODING
        String enc = FileUtils.getEncodingToUse(datFile);
        
        boolean startData = false;
        Scanner sf = null;
        try {
            sf = new Scanner(datFile,enc);
            while (sf.hasNextLine()){
                String line = sf.nextLine();
                if (firstLine) {
                    firstLine=false;
                    if (!line.isEmpty()) {
                        if ((int)line.charAt(0)==65279) { //BOM character
                            line = line.substring(1);
                        }
                    }
                }
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
                            log.warning("Error parsing wavelength");
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
                ds.addPoint(new DataPoint(x,y,0.0,ds));
            }
            ds.setName(datFile.getName());
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
    private static DataSet readXRDML(File f){
        boolean pos = false;
        boolean startend = false; //if we have start/end or ListPositions
        boolean readed = true;
        boolean firstLine = true;
        //creem un DataSerie_Pattern
        DataSet dsP = new DataSet();
        DataSerie ds = new DataSerie(SerieType.dat,Xunits.tth,dsP);
        
        List<Double> intensities = new ArrayList<Double>();
        List<Double> t2ang = new ArrayList<Double>();;
        
        double t2i=0;
        double t2f=0;
        double step=0;
        
        //FIRST CHECK ENCODING
        String enc = FileUtils.getEncodingToUse(f);
        
        Scanner sf = null;
        try {
            sf = new Scanner(f,enc);
            while (sf.hasNextLine()){
                String line = sf.nextLine();
                if (firstLine) {
                    firstLine=false;
                    if (!line.isEmpty()) {
                        if ((int)line.charAt(0)==65279) { //BOM character
                            line = line.substring(1);
                        }
                    }
                }
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
                step = (t2f-t2i)/nrint;
                double t2c = t2i;
                while(t2c<=t2f){
                    t2ang.add(t2c);
                    t2c = t2c+step;
                }
            }
            
            //here we should have t2ang and intensities full and same size, populate dps
            int size = FastMath.min(t2ang.size(), intensities.size());
            if (size == 0)throw new Exception("no points");
            for (int i=0; i<size;i++){
                ds.addPoint(new DataPoint(t2ang.get(i),intensities.get(i),0.0f,ds));
            }
            ds.setName(f.getName());
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
    
    private static DataSet readFF(File f){
        boolean firstLine = true;
        boolean firstAbsLine = true;
        boolean readed = true;
        
        //creem un DataSerie_Pattern
        DataSet dsP = new DataSet();
        DataSerie ds = new DataSerie(SerieType.dat,Xunits.tth,dsP);
        
        List<Double> intensities = new ArrayList<Double>();
        List<Double> t2ang = new ArrayList<Double>();
        double t2i=0;
        double t2f=0;
        double step=0;
        
        //FIRST CHECK ENCODING
        String enc = FileUtils.getEncodingToUse(f);
        
        Scanner sf = null;
        try {
            sf = new Scanner(f,enc);
            while (sf.hasNextLine()){
                String line = sf.nextLine();
                if (firstAbsLine) {
                    firstAbsLine=false;
                    if (!line.isEmpty()) {
                        if ((int)line.charAt(0)==65279) { //BOM character
                            line = line.substring(1);
                        }
                    }
                }
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
            double t2c = t2i;
            while(t2c<=t2f){
                t2ang.add(t2c);
                t2c = t2c+step;
            }
            
            //here we should have t2ang and intensities full and same size, populate dps
            int size = FastMath.min(t2ang.size(), intensities.size());
            if (size == 0)throw new Exception("no points");
            for (int i=0; i<size;i++){
                ds.addPoint(new DataPoint(t2ang.get(i),intensities.get(i),0.0f,ds));
            }
            ds.setName(f.getName());
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
        boolean firstAbsLine = true;
        try {
            sf = new Scanner(f);
            while (sf.hasNextLine()){
                String line = sf.nextLine();
                if (firstAbsLine) {
                    firstAbsLine=false;
                    if (!line.isEmpty()) {
                        if ((int)line.charAt(0)==65279) { //BOM character
                            line = line.substring(1);
                        }
                    }
                }
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
    
    private static DataSet readGSA(File f){
        boolean startData = false;
        boolean esdev = false;
        double t2p = 0;
        double step = 0;
        double t2i = 0;
        boolean firstAbsLine=true;
        boolean readed = true;
        
        //creem un DataSerie_Pattern
        DataSet dsP = new DataSet();
        DataSerie ds = new DataSerie(SerieType.dat,Xunits.tth,dsP);
        
        //FIRST CHECK ENCODING
        String enc = FileUtils.getEncodingToUse(f);
        
        Scanner sf = null;
        try {
            sf = new Scanner(f,enc);
            while (sf.hasNextLine()){
                String line = sf.nextLine();
                if (firstAbsLine) {
                    firstAbsLine=false;
                    if (!line.isEmpty()) {
                        if ((int)line.charAt(0)==65279) { //BOM character
                            line = line.substring(1);
                        }
                    }
                }
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
                            ds.addPoint(new DataPoint(t2p, inten, sd,ds));
                            t2p = t2p + step;
                        }
                    }catch(Exception readex){
                        log.warning("Error reading GSA file");
                        readed=false;
                        break;
                    }
                    continue;
                }
            }
            
            ds.setName(f.getName());
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
    
    private static DataSet readD1P(File f){
        boolean readed = true;
        boolean firstAbsLine=true;
        
        //creem un DataSerie_PRF
        DataSet dsPRF = new DataSet();
        DataSerie dsObs = new DataSerie(SerieType.obs,Xunits.tth,dsPRF);
        DataSerie dsCal = new DataSerie(SerieType.cal,Xunits.tth,dsPRF);
        DataSerie dsDif = new DataSerie(SerieType.diff,Xunits.tth,dsPRF);
        
        //FIRST CHECK ENCODING
        String enc = FileUtils.getEncodingToUse(f);
        
        double wave = -1.0;
        double zero = 0.0;
        Xunits units = Xunits.tth;
        
        Scanner sf = null;
        String line = "";
        try {
            sf = new Scanner(f,enc);
            while (sf.hasNextLine()){
                line = sf.nextLine();
                if (firstAbsLine) {
                    firstAbsLine=false;
                    if (!line.isEmpty()) {
                        if ((int)line.charAt(0)==65279) { //BOM character
                            line = line.substring(1);
                        }
                    }
                }
                if (isComment(line))continue;
                
                if (FileUtils.containsIgnoreCase(line, "name")) {
                    final int iigual = line.trim().indexOf("=") + 1;
                    dsObs.setName(line.trim().substring(iigual, line.trim().length()).trim()+" (obs)");
                    dsCal.setName(line.trim().substring(iigual, line.trim().length()).trim()+" (calc)");
                    dsDif.setName(line.trim().substring(iigual, line.trim().length()).trim()+" (diff)");
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
                    dsObs.addPoint(new DataPoint(t2i,Iobs,0,Ibkg,dsObs));
                    dsCal.addPoint(new DataPoint(t2i,Ical,0,Ibkg,dsCal));
                    dsDif.addPoint(new DataPoint(t2i,Iobs-Ical,0,dsDif));
                }catch(Exception ex) {
                        //vol dir que hem llegit alguna cosa rara
                        log.warning("error reading prf intensity");
                        dsObs.addPoint(new DataPoint(t2i,0,0,0,dsObs));
                        dsCal.addPoint(new DataPoint(t2i,0,0,0,dsCal));
                        dsDif.addPoint(new DataPoint(t2i,0,0,0,dsDif));                        
                }
            }
            dsPRF.addDataSerie(dsObs);
            dsPRF.addDataSerie(dsCal);
            double[] maxminXY = dsDif.getPuntsMaxXMinXMaxYMinY();
            double maxdif = FastMath.max(FastMath.abs(maxminXY[2]), FastMath.abs(maxminXY[3]));
            dsDif.setYOffset(-1*((int)maxdif+100));
            dsPRF.addDataSerie(dsDif);
            maxminXY = dsObs.getPuntsMaxXMinXMaxYMinY();
            //HKLs  
            int nhkl=0;
            DataSerie dsHKL = new DataSerie(SerieType.hkl,units,dsPRF);
            dsHKL.setName(line.substring(3, line.length()));
            while (sf.hasNextLine()) {
                line = sf.nextLine();
                if (line.trim().startsWith("HKL")) {
                    //new hkl serie
                    //first we add the current one to the plottable
                    dsHKL.setYOffset(maxminXY[3]-maxminXY[3]*(nhkl));
                    dsPRF.addDataSerie(dsHKL);
                    //and create a new one
                    dsHKL = new DataSerie(SerieType.hkl,units,dsPRF);
                    dsHKL.setName(line.substring(3, line.length()));
                    nhkl++;
                    continue;
                }                        
                
                String values[] = line.trim().split("\\s+");
                double t2i = Double.parseDouble(values[0]);
                int h = Integer.parseInt(values[1]);
                int k = Integer.parseInt(values[2]);
                int l = Integer.parseInt(values[3]);
                HKLrefl hkl = new HKLrefl(h,k,l,wave,t2i);
                dsHKL.addPoint(new DataPoint_hkl(hkl,t2i,dsHKL));
            }
            //afegim "ultima" HKL
            dsHKL.setYOffset(maxminXY[3]-maxminXY[3]*(nhkl));
            dsPRF.addDataSerie(dsHKL);
            //and create a new one
            dsHKL = new DataSerie(SerieType.hkl,units,dsPRF);
            dsHKL.setName(line.substring(3, line.length()));

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
    
    //refem el prf considerant que hkl pot haver-hi en qualsevol línia!! (no només després del bloc de dades)
    //      1.5603    21115.          0.     -79575.          0.         12.8399      -39786    (  1  0  0)   0  1
    //podem veure quin format es segons el header (que es diferent segons el cas):
    // 2Theta   Yobs    Ycal    Yobs-Ycal   Backg   Posr    (hkl)   K
    // 2Theta   Yobs    Ycal    Yobs-Ycal   Backg   Bragg   Posr    (hkl)   K
    // o be fer-ho a saco mirant la longitud de cada linia

    private static DataSet readPRF(File f){
        boolean readed = true;
        boolean firstAbsLine=true;
        boolean startData = false;
        boolean starthkl = false;
        double previous2t = -100.0;
        int linecount = 0;
        int phases = 0;
        
        //creem un DataSerie_PRF
        DataSet dsPRF = new DataSet();
        DataSerie dsObs = new DataSerie(SerieType.obs,Xunits.tth,dsPRF);
        DataSerie dsCal = new DataSerie(SerieType.cal,Xunits.tth,dsPRF);
        DataSerie dsDif = new DataSerie(SerieType.diff,Xunits.tth,dsPRF);
        ArrayList<DataSerie> dsHKL = new ArrayList<DataSerie>();
        ArrayList<DataPoint_hkl> hkls = new ArrayList<DataPoint_hkl>();
        
        //FIRST CHECK ENCODING
        String enc = FileUtils.getEncodingToUse(f);
        
        Scanner sf = null;
        try {
            sf = new Scanner(f,enc);
            while (sf.hasNextLine()){
                String line = sf.nextLine();
                linecount = linecount +1;
                if (firstAbsLine) {
                    firstAbsLine=false;
                    if (!line.isEmpty()) {
                        if ((int)line.charAt(0)==65279) { //BOM character
                            line = line.substring(1);
                        }
                    }
                }
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
                    //Sep2019 canvi a FORMATTED ja que donava errors quan hi havia hkl tipus: -2-10 3
                    //perque fullprof ho escriu com a formatted string i3
                    
//                    String shkl[] = line.substring(ini+1, fin).trim().split("\\s+");
//                    int h = Integer.parseInt(shkl[0]);
//                    int k = Integer.parseInt(shkl[1]);
//                    int l = Integer.parseInt(shkl[2]);
                    
                    String shkl_1 = line.substring(ini+1, fin);
                    //( -2-10  3)
                    // 012345678
                    int h = Integer.parseInt(shkl_1.substring(0,3).trim());
//                    log.debug(Integer.toString(h));
                    int k = Integer.parseInt(shkl_1.substring(3,6).trim());
//                    log.debug(Integer.toString(k));
                    int l = Integer.parseInt(shkl_1.substring(6,9).trim());
//                    log.debug(Integer.toString(l));
                    
                    HKLrefl hkl = new HKLrefl(h,k,l,dsObs.getWavelength(),t2i);
                    //the y offset (more than one phase)
                    String shkl[] = line.substring(0,ini).trim().split("\\s+");
                    double yoff = Double.parseDouble(shkl[shkl.length-1].trim());
                    hkls.add(new DataPoint_hkl(t2i,yoff,0.,hkl,null)); //poso null, el parent ja es posarà a l'afegir a dataserie despres
                }else{
                    //mirem si hi ha info d'hkl també aquí
                    try {
                        double Iobs = Double.parseDouble(values[1]);
                        double Ical = Double.parseDouble(values[2]);
                        double Ibkg = Double.parseDouble(values[4]);
                        dsObs.addPoint(new DataPoint(t2i,Iobs,0,Ibkg,dsObs));
                        dsCal.addPoint(new DataPoint(t2i,Ical,0,Ibkg,dsCal));
                        dsDif.addPoint(new DataPoint(t2i,Iobs-Ical,0,dsDif));
                        
                        if (values.length>6) {                            //has hkl
                            int ini = line.indexOf("(");
                            int fin = line.indexOf(")");
                            String shkl[] = line.substring(ini+1, fin).trim().split("\\s+");
                            int h = Integer.parseInt(shkl[0]);
                            int k = Integer.parseInt(shkl[1]);
                            int l = Integer.parseInt(shkl[2]);
                            shkl = line.substring(0,ini).trim().split("\\s+");
                            double t2r = Double.parseDouble(shkl[shkl.length-2]);
                            HKLrefl hkl = new HKLrefl(h,k,l,dsObs.getWavelength(),t2r);
                            //the y offset (more than one phase)
                            double yoff = Double.parseDouble(shkl[shkl.length-1].trim());
                            hkls.add(new DataPoint_hkl(t2r,yoff,0.,hkl,null)); //poso null, el parent ja es posarà a l'afegir a dataserie despres
                        }
                    }catch(Exception ex) {
                        //vol dir que hem llegit alguna cosa rara
                        log.warning("error reading prf intensity");
                        dsObs.addPoint(new DataPoint(t2i,0,0,0,dsObs));
                        dsCal.addPoint(new DataPoint(t2i,0,0,0,dsCal));
                        dsDif.addPoint(new DataPoint(t2i,0,0,0,dsDif));                        
                    }
                    previous2t = t2i;
                }
            }
                        
            double[] maxminXY = dsDif.getPuntsMaxXMinXMaxYMinY();
            double maxdif = FastMath.max(FastMath.abs(maxminXY[2]), FastMath.abs(maxminXY[3]));
            dsDif.setYOffset(-1*((int)maxdif+100));
            maxminXY = dsObs.getPuntsMaxXMinXMaxYMinY();
            
            dsObs.setName(f.getName()+" ("+dsObs.getSerieType().toString()+")");
            dsCal.setName(f.getName()+" ("+dsCal.getSerieType().toString()+")");
            dsDif.setName(f.getName()+" ("+dsDif.getSerieType().toString()+")");
            dsPRF.addDataSerie(dsObs);
            dsPRF.addDataSerie(dsCal);
            dsPRF.addDataSerie(dsDif);
            //HKLS:
            if (phases<=1) {
                for (DataPoint_hkl hkl:hkls) {
                    hkl.setY(0);
                    dsHKL.get(0).addPoint(hkl);
                }
                dsHKL.get(0).setYOffset(maxminXY[3]-DataSerie.def_hklYOff);
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
                    dsHKL.get(i).setYOffset(maxminXY[3]-maxminXY[3]*(i)); 
                }
            }
            
            //general info
            for (DataSerie ds:dsHKL) {
                ds.setName(f.getName()+" ("+ds.getSerieType().toString()+")");
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
    public static boolean writeProject(File stateFile, boolean overwrite, XRDPlotPanelFrontEnd p, D1Dplot_data dades, boolean fullData) {
    	
        if (stateFile.exists()&&!overwrite)return false;
        if (stateFile.exists()&&overwrite)stateFile.delete();
        
        boolean written = true;
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(stateFile,true)));//        	//guardar axis info, zoom, bounds, etc...
            
            out.println(FileUtils.getCharLine('=', 80));
            out.println(FileUtils.getCenteredString("d1Dplot project File", 80));
            out.println(FileUtils.getCharLine('=', 80));
            out.println(String.format("w=%d h=%d %s", D1Dplot_global.getD1DmainFrame().getWidth(),D1Dplot_global.getD1DmainFrame().getHeight(),fullData));
            out.println(p.getVisualParametersToSave().getOptionsAsString('='));
            out.println(FileUtils.getCharLine('-', 80));
            
        	//guardar patterns/series amb tots els seus paràmetres de visualització
            int np = 0;
            for (int i=0;i<dades.getNDataSets();i++) {
                DataSet plt = dades.getDataSet(i);
                String filePath = plt.getFile().getAbsolutePath();
                out.println("PL");
                out.println(String.format("%d %s", np, filePath));
                
                int nd = 0;
        	    for(DataSerie d:plt.getDataSeries()) { //TODO cal refer. posar el tipus. etc.. o llegir de nou al carregar
                    String nam = d.getName();
                    String typ = d.getSerieType().toString();
                    String col = Integer.toString(d.getColor().getRGB());
                    String sca = FileUtils.dfX_4.format(d.getScaleY());
                    String zof = FileUtils.dfX_5.format(d.getXOffset());
                    String wav = FileUtils.dfX_5.format(d.getWavelength());
                    String xun = d.getxUnits().name();
                    String yof = FileUtils.dfX_3.format(d.getYOffset());
                    String mar = FileUtils.dfX_2.format(d.getMarkerSize());
                    String lin = FileUtils.dfX_2.format(d.getLineWidth());
                    String err = Boolean.toString(d.isShowErrBars());
                    String plo = Boolean.toString(d.isPlotThis());
                    
                    if (d.getSerieType()==SerieType.bkg) {
                        //TODO: cal guardarla tota
                    }
                    if (d.getSerieType()==SerieType.ref) {
                        //TODO: cal guardarla tota
                        
                    }

                    out.println("DS");
                    out.println(String.format("%d %s %s", nd, typ, nam)); //el nom podria tenir espais!! per aixo el deixo com a ultim
                    out.println(String.format("%s %s %s %s %s %s %s %s %s %s", col,sca,zof,wav,xun,yof,mar,lin,err,plo));
                    if (fullData) {
                        out.println(getDAT_ALBA(d,false));    
                    }

                    nd++;
        	    }
        	    np++;
                out.println(FileUtils.getCharLine('-', 80));
        	}
                        
        }catch(Exception ex) {
        	if (D1Dplot_global.isDebug())ex.printStackTrace();
        	written = false;
        }finally {
            if(out!=null)out.close();
        }
    	return written;
    }

    public static boolean readProject(File stateFile, XRDPlotPanelFrontEnd p, D1Dplot_data dades, D1Dplot_main m) {
        boolean readed = true;
        Scanner sf = null;
        try {
            sf = new Scanner(stateFile);

            // primeres linies fixes
            String line = sf.nextLine();
            line = sf.nextLine();
            line = sf.nextLine();
            
            line = sf.nextLine();
            String[] vals0 = line.trim().split("\\s+");
            int width = Integer.parseInt(vals0[0].trim().split("=")[1]);
            int height = Integer.parseInt(vals0[1].trim().split("=")[1]);
            boolean fulldata = Boolean.parseBoolean(vals0[2].trim());

            m.getMainFrame().setBounds(m.getMainFrame().getX(), m.getMainFrame().getY(), width, height); //TODO:test a veure si funciona que no crec...
//            D1Dplot_global.getD1DmainFrame().repaint(); //mirar si cal o no aixo
            
            Options opt = new Options();
            StringBuilder sb = new StringBuilder();
            line = sf.nextLine();
            while (!line.startsWith("----")) {
                sb.append(line);
                sb.append(FileUtils.lineSeparator);
                line=sf.nextLine();
            }
            opt.readOptionsFromString(sb.toString());
            
            //ara els patterns series
            DataSet currentPlottable=null;
            boolean readNext=true;
            while (sf.hasNextLine()){
                if(readNext) {
                    line = sf.nextLine();
                }else {
                    readNext=true;
                }
                if (line.trim().isEmpty())continue;
                if (line.trim().startsWith("PL")) { //new plottable
                    line = sf.nextLine();
                    String[] vals = line.trim().split("\\s+");
                    String fname = String.join(" ", Arrays.asList(vals).subList(1, vals.length));
                    if (!fulldata) {
                        currentPlottable=DataFileUtils.readPatternFile(new File(fname.trim()));
                    }else {
                        //hi ha les dades, creem un plottable buit
                        currentPlottable=new DataSet();
                    }
                    dades.addDataSet(currentPlottable, false, false);
                    continue; //seguim llegint les dataseries
                }

                if (line.trim().startsWith("DS")) { //new dataserie al currentPlottable
                    //llegim primer els "parametres"
                    line = sf.nextLine();
                    String[] vals = line.trim().split("\\s+");
                    int nDS = Integer.parseInt(vals[0]);
                    SerieType styp = SerieType.getEnum(vals[1]);
                    if (styp==null)styp=SerieType.dat;
                    String dsname = String.join(" ", Arrays.asList(vals).subList(2, vals.length));
                    //seguent linia (parametres)
                    line = sf.nextLine();
                    vals = line.trim().split("\\s+");
                    
                    //ara..
                    //    ..o be apliquem directament els "parametres" a la serie ja llegida (cas no fullData) 
                    //    ..o llegim les dades i apliquem parametres (cas fullData)
                    
                    if (fulldata) {
                        //cal llegir i afegir primer la serie abans d'aplicar els parametres
                        sb = new StringBuilder();
                        while(sf.hasNextLine()) {
                            line = sf.nextLine();
                            if (line.trim().startsWith("PL")||line.trim().startsWith("DS")||line.trim().startsWith("----")) {
                                readNext=false; //solucio una mica cutre...
                                break;
                            }
                            //la linea es part de les dades
                            sb.append(line);
                            sb.append(FileUtils.lineSeparator);
                        }
                        currentPlottable.addDataSerie(readDAT(sb.toString(),dsname).getDataSerie(0)); //TODO hem fet la prova amb read DAT pero hauria de fer-ho segons el tipus
                        
                    }
                    //ara tant sigui a partir del fitxer com a partir de llegir-lo apart tindrem la dataserie al numero que toca
                    DataSerie ds = currentPlottable.getDataSerie(nDS);
                    ds.setName(dsname.trim());
                    ds.setColor(FileUtils.getColor(Integer.parseInt(vals[0])));
                    ds.setScaleY(Float.parseFloat(vals[1]));
                    ds.setXOffset(Double.parseDouble(vals[2]));
                    ds.setWavelength(Double.parseDouble(vals[3]));
                    ds.setxUnits(Xunits.getEnum(vals[4]));
                    ds.setYOffset(Double.parseDouble(vals[5]));
                    ds.setMarkerSize(Float.parseFloat(vals[6]));
                    ds.setLineWidth(Float.parseFloat(vals[7]));
                    ds.setShowErrBars(Boolean.parseBoolean(vals[8]));
                    ds.setPlotThis(Boolean.parseBoolean(vals[9]));
                }
            }
            
            m.updateData(true,true,true);
            m.showTableTab();
            p.setVisualParametersFromSaved(opt);
                        
        }catch(Exception ex) {
            if (D1Dplot_global.isDebug())ex.printStackTrace();
            readed = false;
        }finally {
            if(sf!=null)sf.close();
        }
        
        return readed;
    }
    
    
//  #TITOL
//  name=XXXX
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
      if (d1File.exists()&&!overwrite)return null;
      if (d1File.exists()&&overwrite)d1File.delete();
      
      PrintWriter out = null;
      try {
          out = new PrintWriter(new BufferedWriter(new FileWriter(d1File,true)));
          out.println("#d1Dplot pattern matching obs/calc/hkl data");//TODO print comments?
          out.println("name="+dsOBS.getName());
          out.println(String.format("cell=%s",""));
          out.println(String.format("sg=%s",""));
          out.println(String.format("wave=%.5f",dsOBS.getWavelength()));
          out.println(String.format("zero=%.5f",dsOBS.getXOffset()));
          out.println(String.format("units=%s",dsOBS.getxUnits().getName()));
          out.println("DATA");
          for (int i=0;i<dsOBS.getNPoints();i++) {
              double t2 = dsOBS.getCorrectedPoint(i, 0, 0, 0, 1.0, false).getX();//no corrections
              double yobs = dsOBS.getCorrectedPoint(i, 0, 0, 0, 1.0, false).getY();//no corrections
              double ycal = dsCAL.getCorrectedPoint(i, 0, 0, 0, 1.0, false).getY();//no corrections
              double ybkg = dsOBS.getCorrectedPoint(i, 0, 0, 0, 1.0, false).getYbkg();//no corrections
              out.println(String.format(" %10.7e  %10.7e  %10.7e  %10.7e", t2,yobs,ycal,ybkg));
          }
          for (DataSerie ds:dsHKL) {
              out.println(String.format("HKL %s",ds.getName()));
              for (int i=0;i<ds.getNPoints();i++) {
                  out.println(String.format(" %10.7e  %s", ds.getCorrectedPoint(i, 0, 0, 0, 1.0,false).getX(),ds.getCorrectedPoint(i, 0, 0, 0, 1.0,false).getLabel()));
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

            if (FastMath.abs(ds.getXOffset())>0.01f){
                out.println("# zeroOffsetApplied="+FileUtils.dfX_3.format(ds.getXOffset()));
            }
            if (ds.getScaleY()>1.05 || ds.getScaleY()<0.95){
                out.println("# scaleFactorApplied="+FileUtils.dfX_2.format(ds.getScaleY()));
            }
            
            if (ds.getNPoints()<npoints){
                npoints = ds.getNPoints();
            }
            out.println(String.format("# %d BACKGROUND POINTS:", npoints));
            
            double minX = ds.getMinX();
            double maxX = ds.getMaxX();
            double stepX = (maxX-minX)/npoints;
            
            double currX = minX;
            int nwritten = 0;
            while(currX<=maxX){
                Plottable_point[] dps = ds.getSurroundingPoints(currX);
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
    
    public static File writeMDAT(List<DataSerie> dss, File outf, boolean overwrite, boolean addYbkg){
        if (outf.exists()&&!overwrite)return null;
        if (outf.exists()&&overwrite)outf.delete();
        
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(outf,true)));            
            //primer escribim tantes linies de comentari com noms de series (o fitxers)
            for (int ids=0;ids<dss.size();ids++) {
                out.println(String.format("# %2d %s %s", ids, dss.get(ids).getName(), dss.get(ids).getParent().getFile().getName()));
            }
            if (FastMath.abs(dss.get(0).getWavelength()-dss.get(0).getOriginalWavelength())>0.01){
                out.println("# wavelength="+FileUtils.dfX_4.format(dss.get(0).getWavelength())+" (originalWL="+FileUtils.dfX_4.format(dss.get(0).getOriginalWavelength())+")");                
            }
            if (FastMath.abs(dss.get(0).getXOffset())>0.01f){
                out.println("# zeroOffsetApplied="+FileUtils.dfX_3.format(dss.get(0).getXOffset()));
            }
            //ara les dades, 1a columna 2theta i la resta intensitats
            //cal fer rebinning de totes igualar a la primera (que es la que mana)
            DataSerie ds0 = dss.get(0);
            ArrayList<DataSerie> dsRebin = new ArrayList<DataSerie>();
            dsRebin.add(ds0);
            for (int ids=1;ids<dss.size();ids++) {
                DataSerie dsi = dss.get(ids);
                if ((PattOps.haveSameNrOfPointsDS(ds0, dsi))&&(PattOps.haveCoincidentPointsDS(dss.get(0), dsi))) {
                    dsRebin.add(dsi);
                }else {
                    dsRebin.add(PattOps.rebinDS(ds0, dsi));
                }
            }
            
            for (int ipp=0; ipp<ds0.getNPoints();ipp++){
                Plottable_point pp = ds0.getCorrectedPoint(ipp,addYbkg);    
                StringBuilder line = new StringBuilder();
                line.append(String.format(" %10.7e %10.7e",pp.getX(),pp.getY()));
                //ara la resta de dataseries
                for (int ids=1;ids<dss.size();ids++) {
                    DataSerie dsi = dss.get(ids);
//                    if ((PattOps.haveSameNrOfPointsDS(ds0, dsi))&&(PattOps.haveCoincidentPointsDS(dss.get(0), dsi))) {
//                        pp=dsi.getCorrectedPoint(ipp, addYbkg);
//                    }else {
//                        pp = PattOps.rebinDS(ds0, dsi).getCorrectedPoint(ipp, addYbkg);
//                    }
                    pp=dsi.getCorrectedPoint(ipp, addYbkg);
                    line.append(String.format(" %10.7e",pp.getY()));
                }
                out.println(line.toString());
                log.info(line.toString());
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }finally {
            if(out!=null)out.close();
        }
        return outf;
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

            if (FastMath.abs(ds.getXOffset())>0.01f){
                out.println("# zeroOffsetApplied="+FileUtils.dfX_3.format(ds.getXOffset()));
            }
            if (ds.getScaleY()>1.05 || ds.getScaleY()<0.95){
                out.println("# scaleFactorApplied="+FileUtils.dfX_2.format(ds.getScaleY()));
            }
            
            for (int i=0; i<ds.getNPoints();i++){
                Plottable_point pp = ds.getCorrectedPoint(i,addYbkg);
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
    
    private static String getDAT_ALBA(DataSerie ds, boolean addYbkg){

        StringBuilder sb = new StringBuilder();

        Iterator<String> itrComm = ds.getCommentLines().iterator();
        while (itrComm.hasNext()){
            String cline = itrComm.next();
            sb.append(cline);
            sb.append(FileUtils.lineSeparator);
        }
        //ara posem una linia comentari amb les wavelengths
        if (FastMath.abs(ds.getWavelength()-ds.getOriginalWavelength())>0.01){
            sb.append("# wavelength="+FileUtils.dfX_4.format(ds.getWavelength())+" (originalWL="+FileUtils.dfX_4.format(ds.getOriginalWavelength())+")");   
            sb.append(FileUtils.lineSeparator);
        }

        if (FastMath.abs(ds.getXOffset())>0.01f){
            sb.append("# zeroOffsetApplied="+FileUtils.dfX_3.format(ds.getXOffset()));
            sb.append(FileUtils.lineSeparator);
        }
        if (ds.getScaleY()>1.05 || ds.getScaleY()<0.95){
            sb.append("# scaleFactorApplied="+FileUtils.dfX_2.format(ds.getScaleY()));
            sb.append(FileUtils.lineSeparator);
        }

        for (int i=0; i<ds.getNPoints();i++){
            Plottable_point pp = ds.getCorrectedPoint(i,addYbkg);
            String towrite = String.format(" %10.7e  %10.7e  %10.7e",pp.getX(),pp.getY(),pp.getSdy());
            towrite = towrite.replace(",", ".");
            sb.append(towrite);
            if (i<(ds.getNPoints())-1)sb.append(FileUtils.lineSeparator);
        }
        return sb.toString();
    }
    
    private static DataSet readDAT(String datString, String serieName) { 
        boolean firstLine = true;
        boolean readed = true;
        
        //creem un DataSerie_Pattern
        DataSet dsP = new DataSet();
        DataSerie ds = new DataSerie(SerieType.dat,Xunits.tth,dsP);
        
        Scanner sf = null;
        try {
            sf = new Scanner(datString);
            while (sf.hasNextLine()){
                String line = sf.nextLine();
                if (firstLine) {
                    firstLine=false;
                    if (!line.isEmpty()) {
                        if ((int)line.charAt(0)==65279) { //BOM character
                            line = line.substring(1);
                        }
                    }
                }
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
//                    log.fine("error parsing sdev");
                }

                ds.addPoint(new DataPoint(t2,inten,sdev,ds));

            }
            if (ds.getNPoints()<=0)return null;
            ds.setName(serieName);
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
            
            for (int i=0; i<ds.getNPoints();i++){
                Plottable_point pp = ds.getCorrectedPoint(i,addYbkg);
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

            if (FastMath.abs(ds.getXOffset())>0.01f){
                comments.append(" zeroOffsetApplied="+FileUtils.dfX_3.format(ds.getXOffset()));
            }

            if (ds.getScaleY()>1.05 || ds.getScaleY()<0.95){
                comments.append("scaleFactorApplied="+FileUtils.dfX_2.format(ds.getScaleY()));
            }
            
            String towrite = String.format(" %10.7e %10.7e %10.7e %s", FileUtils.round(ds.getMinX(),5),FileUtils.round(ds.calcStep(),5),FileUtils.round(ds.getMaxX(),5),comments.toString());
            towrite = towrite.replace(",", ".");
            out.println(towrite);
            
            
            //
            double[] intensities = new double[10];
            int ii = 0;
            for (int i=0;i<ds.getNPoints();i++){
                intensities[ii] = ds.getCorrectedPoint(i,addYbkg).getY();
                ii++;
                if (ii==10 || i==ds.getNPoints()-1){
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
            out.println("# "+ds.getName()+" "+ds.getCommentLines().get(0)+" "+warn_msg);
            out.println("Instrument parameter      bl04.prm ");

            int npts = ds.getNPoints();
            int nrec = (int)((npts - (npts%5))/5.);
            if (npts%5!=0)nrec = nrec +1;
//            BANK 1   20951    4191 CONST    60.000     0.600  0.0 0.0 ESD                   
//            if (ds.getStep()<0)ds.setStep(ds.calcStep());
            String linegsa=String.format("BANK 1 %7d %7d CONST %9.3f %9.3f  0.0 0.0 ESD ",npts,nrec,ds.getCorrectedPoint(0,addYbkg).getX()*100.0,ds.calcStep()*100.0);
            out.println(linegsa);
            
            int startIndex = 0;
            int endIndex = ds.getNPoints()-1;
            
            while (startIndex <= endIndex-4){
                try{
                    String towrite = String.format("%8.1f%8.1f%8.1f%8.1f%8.1f%8.1f%8.1f%8.1f%8.1f%8.1f",ds.getCorrectedPoint(startIndex,addYbkg).getY()/top, ds.getCorrectedPoint(startIndex,addYbkg).getSdy()/top,
                            ds.getCorrectedPoint(startIndex+1,addYbkg).getY()/top, ds.getCorrectedPoint(startIndex+1,addYbkg).getSdy()/top,
                            ds.getCorrectedPoint(startIndex+2,addYbkg).getY()/top, ds.getCorrectedPoint(startIndex+2,addYbkg).getSdy()/top,
                            ds.getCorrectedPoint(startIndex+3,addYbkg).getY()/top, ds.getCorrectedPoint(startIndex+3,addYbkg).getSdy()/top,
                            ds.getCorrectedPoint(startIndex+4,addYbkg).getY()/top, ds.getCorrectedPoint(startIndex+4,addYbkg).getSdy()/top);
                    out.println(towrite);
                    startIndex+=5;
                }catch(Exception ex){
                    log.warning("Error writting gsa");
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
            for (int i=0; i<ds.getNPoints(); i++){
                Plottable_point pp = ds.getCorrectedPoint(i,addYbkg);
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
    
    private static boolean writeREF(DataSerie ds, File outf, boolean overwrite, boolean addYbkg){
        if (outf.exists()&&!overwrite)return false;
        if (outf.exists()&&overwrite)outf.delete();
        boolean written = true;
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(outf,true)));
            
            if (ds.getWavelength()>0) {
                out.println(String.format("# wavelength=%8.4f",ds.getWavelength()));
            }
            
            for (int i=0; i<ds.getNPoints(); i++){
                Plottable_point pp = ds.getCorrectedPoint(i,addYbkg);
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
                for (int i=0; i<ds.getNPoints(); i++){
                    double t2 = ds.getCorrectedPoint(i,addYbkg).getX();
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
            
            for (int i=0; i<ds.getNPoints(); i++){
                double inten = ds.getCorrectedPoint(i,addYbkg).getY();
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
	        for(int i=0;i<ds.getNPoints();i++){
	            Plottable_point pp = ds.getCorrectedPoint(i,false); //no bkg in this case
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
                Plottable_point pp = ds.getCorrectedPoint(i,false); //no bkg in this case
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
                log.debug("Error parsing element after wavelength keyword");
            }
            
            //provem amb signe igual
            try{
                String[] vals = s.split("=");
                for (int i=0;i<vals.length;i++){
                    if (FileUtils.containsIgnoreCase(vals[i], "wave")){
                        
                        String[] vals2 = vals[i+1].trim().split("\\s+");
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
                log.debug("Error parsing element after wave*= keyword");
            }
        }
        
        
        return wl;
    }

    private static String searchForName(String s){
        String name = "";
        if (FileUtils.containsIgnoreCase(s, "name")){
            //provem amb signe igual
            try{
                String[] vals = s.split("=");
                for (int i=0;i<vals.length;i++){
                    if (FileUtils.containsIgnoreCase(vals[i], "name")){
                        name = vals[i+1];
                    }
                }
            }catch(Exception ex){
                log.debug("Error parsing element after name*= keyword");
            }
        }
        return name;
    }
    
	private static boolean isComment(String ln){
	    if (ln.trim().startsWith("#"))return true;
	    if (ln.trim().startsWith("!"))return true;
	    if (ln.trim().startsWith("/"))return true;
	    if (ln.trim().startsWith("$"))return true;
	    return false;
	}

}

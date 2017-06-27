package com.vava33.d1dplot.auxi;


/**
 * D1Dplot
 * 
 * Operations with files
 *  
 * @author Oriol Vallcorba
 * Licence: GPLv3
 * 
 */

import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

//import org.apache.commons.math3.util.FastMath;








import org.apache.commons.math3.util.FastMath;

import com.vava33.d1dplot.D1Dplot_global;
import com.vava33.d1dplot.D1Dplot_main;
import com.vava33.d1dplot.Dicvol_dialog;
import com.vava33.d1dplot.auxi.DataSerie.serieType;
import com.vava33.d1dplot.auxi.DataSerie.xunits;
import com.vava33.jutils.VavaLogger;
import com.vava33.jutils.FileUtils;

public final class DataFileUtils {

    private static VavaLogger log = D1Dplot_global.getVavaLogger(DataFileUtils.class.getName());
    
    
    public static enum SupportedReadExtensions {DAT,XYE,XY,ASC,GSA,XRDML,PRF,GR,TXT;}
    public static enum SupportedWriteExtensions {DAT,ASC,GSA,XRDML,GR;}
    public static final Map<String, String> formatInfo;
    static
    {
        formatInfo = new HashMap<String, String>(); //ext, description
        formatInfo.put("dat", "2 or 3 columns file 2th/int/(err) *OR* free format (*.dat)");
        formatInfo.put("xye", "3 columns file 2th/int/err (*.xye)");
        formatInfo.put("xy", "2 columns file 2th/int (*.xy)");
        formatInfo.put("asc", "2 columns file 2th/int with no headers (*.asc)");
        formatInfo.put("gsa", "GSAS Standard Powder Data File (*.gsa)");
        formatInfo.put("xrdml", "Panalytical format (*.xrdml)");
        formatInfo.put("prf", "Obs,calc and difference profiles after fullprof refinement (*.prf)");
        formatInfo.put("gr", "g(r) from pdfgetx3 (*.gr)");
        formatInfo.put("txt", "2 columns space or comma separated (*.txt)");
    }
    
    public static enum SupportedWritePeaksFormats {DIC,TXT;}
    public static final Map<String, String> peakFormatInfo;
    static
    {
        peakFormatInfo = new HashMap<String, String>(); //ext, description
        peakFormatInfo.put("dic", "dicvol input file");
        peakFormatInfo.put("txt", "list of peaks in 3 columns format 2th/int/err");
    }
    
    public static FileNameExtensionFilter[] getExtensionFilterWrite(){
        //mirem quins formats som capaços de salvar segons ImgFileUtils
        Iterator<String> itrformats = DataFileUtils.formatInfo.keySet().iterator();
        FileNameExtensionFilter[] filter = new FileNameExtensionFilter[DataFileUtils.SupportedWriteExtensions.values().length];
        int nfiltre=0;
        while (itrformats.hasNext()){
            String frm = itrformats.next();
            //this line returns the FORMAT in the ENUM or NULL
            DataFileUtils.SupportedWriteExtensions wfrm = FileUtils.searchEnum(DataFileUtils.SupportedWriteExtensions.class, frm);
            if (wfrm!=null){
                //afegim filtre
                filter[nfiltre] = new FileNameExtensionFilter(DataFileUtils.formatInfo.get(frm), frm);
                nfiltre = nfiltre +1;
            }
        }
        return filter;
    }
    
    public static FileNameExtensionFilter[] getExtensionFilterRead(){
        //mirem quins formats som capaços de salvar segons ImgFileUtils
        Iterator<String> itrformats = DataFileUtils.formatInfo.keySet().iterator();
        FileNameExtensionFilter[] filter = new FileNameExtensionFilter[DataFileUtils.SupportedReadExtensions.values().length+1]; //+1 for all image formats
        String[] frmStrings= new String[DataFileUtils.SupportedReadExtensions.values().length];
        int nfiltre=0;
        while (itrformats.hasNext()){
            String frm = itrformats.next();
            //this line returns the FORMAT in the ENUM or NULL
            DataFileUtils.SupportedReadExtensions wfrm = FileUtils.searchEnum(DataFileUtils.SupportedReadExtensions.class, frm);
            if (wfrm!=null){
                //afegim filtre
                filter[nfiltre] = new FileNameExtensionFilter(DataFileUtils.formatInfo.get(frm), frm);
                frmStrings[nfiltre] = frm;
                nfiltre = nfiltre +1;
            }
        }
        //afegim filtre de tots els formats
        filter[nfiltre] = new FileNameExtensionFilter("All 1D-XRD supported formats", frmStrings);
        return filter;
    }
    
    public static FileNameExtensionFilter[] getExtensionFilterPeaksWrite(){
        //mirem quins formats som capaços de salvar segons ImgFileUtils
        Iterator<String> itrformats = DataFileUtils.peakFormatInfo.keySet().iterator();
        FileNameExtensionFilter[] filter = new FileNameExtensionFilter[DataFileUtils.SupportedWritePeaksFormats.values().length];
        int nfiltre=0;
        while (itrformats.hasNext()){
            String frm = itrformats.next();
            //this line returns the FORMAT in the ENUM or NULL
            DataFileUtils.SupportedWritePeaksFormats wfrm = FileUtils.searchEnum(DataFileUtils.SupportedWritePeaksFormats.class, frm);
            if (wfrm!=null){
                //afegim filtre
                filter[nfiltre] = new FileNameExtensionFilter(DataFileUtils.peakFormatInfo.get(frm), frm);
                nfiltre = nfiltre +1;
            }
        }
        return filter;
    }
    
    // OBERTURA DELS DIFERENTS FORMATS DE DADES2D
    public static boolean readPatternFile(File d1file, Pattern1D patt1D) {
        // comprovem extensio
        log.debug(d1file.toString());
        String ext = FileUtils.getExtension(d1file).trim();

        // this line returns the FORMAT in the ENUM or NULL
        SupportedReadExtensions format = FileUtils.searchEnum(
                SupportedReadExtensions.class, ext);
        if (format != null) {
            log.debug("Format=" + format.toString());
        }

        if (format == null) {
            SupportedReadExtensions[] possibilities = SupportedReadExtensions
                    .values();
            SupportedReadExtensions s = (SupportedReadExtensions) JOptionPane
                    .showInputDialog(null, "Input format:", "Read File",
                            JOptionPane.PLAIN_MESSAGE, null, possibilities,
                            possibilities[0]);
            if (s == null) {
                return false;
            }
            format = s;
        }
        boolean ok = false;
        switch (format) {
            case DAT:
                if (detectDATFreeFormat(d1file)){
                    ok = readDATFreeFormat(d1file,patt1D);
                }else{
                    ok = readDAT_ALBA(d1file,patt1D);
                }
                break;
            case XYE:
                ok = readXYE(d1file,patt1D);
                break;
            case XY:
                ok = readXYE(d1file,patt1D);
                break;
            case ASC:
                ok = readASC(d1file,patt1D);
                break;
            case GSA:
                ok = readGSA(d1file,patt1D);
                break;
            case XRDML:
                ok = readXRDML(d1file,patt1D);
                break;
            case PRF:
                ok = readPRF(d1file,patt1D);
                break;
            case GR:
                ok = readGR(d1file,patt1D);
                break;
            default:
                ok = readUNK(d1file,patt1D);
                break;

        }
        if (!ok){
            log.debug("Error reading pattern "+d1file.getAbsolutePath());
            return false;
        }
        if (patt1D.getFile() == null) {
            patt1D.setFile(d1file);
        }
        
        //SERIE CHECKS
        for (int i=0; i<patt1D.getNseries(); i++){
            DataSerie ds = patt1D.getSerie(i);
            if (ds.getTipusSerie()==DataSerie.serieType.dat){
                if (ds.getT2i()<-99)ds.setT2i(ds.getPoint(0).getX());
                if (ds.getT2f()<-99)ds.setT2f(ds.getPoint(ds.getNpoints()-1).getX());
                if (ds.getStep()<-99)ds.calcStep();
            }
        }
        return true;
    }
    
    public static File writePatternFile(File d1File, Pattern1D patt1D, int serie, boolean overwrite) {
        // comprovem extensio
        log.debug(d1File.toString());
        String ext = FileUtils.getExtension(d1File).trim();

        //DETECCIO SERIE BKG
        if(patt1D.getSerie(serie).getTipusSerie()==serieType.bkg){
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
                        log.info("error reading n points");
                        return null;
                    }
                }
                boolean written = false;
                File fout = null;
                written = writeBKG(patt1D,serie,npoints,d1File,overwrite);
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

        boolean written = false;
        File fout = null;

        switch (format) {
            case DAT:
                Object[] options = {"DAT 3 Columns: 2theta Intensity ESD","DAT Free Format"};
                int n = JOptionPane.showOptionDialog(null,
                        "Which DAT format would you like?",
                        "Select DAT format",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]);
                if(n==0){  
                    written = writeDAT_ALBA(patt1D,serie,d1File,overwrite);
                }else if(n==1){
                    written = writeDAT_FreeFormat(patt1D,serie,d1File,overwrite);
                }else{
                    log.info("no DAT format choosen");
                }
                break;
            case ASC:
                written = writeASC(patt1D,serie,d1File,overwrite);
                break;
            case GSA:
                written = writeGSA(patt1D,serie,d1File,overwrite);
                break;
            case XRDML:
                written = writeXRDML(patt1D,serie,d1File,true,overwrite);
                break;
            case GR:
                written = writeGR(patt1D,serie,d1File,overwrite);
                break;
            default:
                log.info("Unknown format to write");
                return null;
        }
        if (written) fout = d1File;
        return fout;
    }
    
    public static File writePeaksFile(File d1File, Pattern1D patt1D, int serie, boolean overwrite,D1Dplot_main m) {
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
                written = writePeaksDIC(patt1D,serie,d1File,overwrite,m);
                break;
            case TXT:
                written = writePeaksTXT(patt1D,serie,d1File,overwrite);
                break;
        }
        if (written) fout = d1File;
        return fout;
    }
    
    public static boolean writePeaksTXT(Pattern1D patt1D, int serie, File outf, boolean overwrite){
        if (outf.exists()&&!overwrite)return false;
        if (outf.exists()&&overwrite)outf.delete();
        DataSerie ds = patt1D.getSerie(serie); //Affected Serie
        boolean written = true;
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outf,true)));
            
            Iterator<String> itrComm = patt1D.getCommentLines().iterator();
            while (itrComm.hasNext()){
                String cline = itrComm.next();
                if (!cline.startsWith("#"))cline = "# "+cline;
                out.println(cline);
            }
            out.println("# file generated with d1Dplot");
            //ARA ELS PICS
            for(int i=0;i<ds.getNpeaks();i++){
                String s = String.format(" %12.5f %15.3f %15.3f",ds.getPeak(i).getX(),ds.getPeak(i).getY(),ds.getPeak(i).getSdy());
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
    
    
    static Dicvol_dialog dvdiag;
    public static boolean writePeaksDIC(Pattern1D patt1D, int serie, File outf, boolean overwrite, D1Dplot_main main){
        if (outf.exists()&&!overwrite)return false;
        if (outf.exists()&&overwrite)outf.delete();
        DataSerie ds = patt1D.getSerie(serie); //Affected Serie
        
        //preguntem els parametres
        if (dvdiag==null){
            dvdiag = new Dicvol_dialog(ds,main);
            dvdiag.setVisible(true);
        }else{
            dvdiag.updateDS(ds);
            dvdiag.setVisible(true);
        }
        
        
        //now we write
        if (!dvdiag.isEverythingOK()){
            log.info("error reading dicvol parameters");
            return false;
        }
        
        boolean written = true;
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outf,true)));
            
            Iterator<String> itrComm = patt1D.getCommentLines().iterator();
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
                    log.info("dspInv not supported by dicvol, please change xunits");
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
                s = String.format(" %12.5f %15.2f %15.2f",ds.getPeak(i).getX(), ds.getPeak(i).getY(), ds.getPeak(i).getSdy());
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
    
    private static boolean isComment(String ln){
        if (ln.trim().startsWith("#"))return true;
        if (ln.trim().startsWith("!"))return true;
        if (ln.trim().startsWith("/"))return true;
        if (ln.trim().startsWith("$"))return true;
        return false;
    }
    
    private static boolean readXYE(File f, Pattern1D patt1D){
        return readDAT_ALBA(f,patt1D);
    }
    
    private static boolean readASC(File f, Pattern1D patt1D){
        return readDAT_ALBA(f,patt1D);

    }
    
    //only 1 serie
    public static boolean readUNK(File datFile, Pattern1D patt1D) {
        boolean readed = true;
        DataSerie ds = new DataSerie();
        try {
            Scanner sf = new Scanner(datFile);
            while (sf.hasNextLine()){
                String line = sf.nextLine();
                if (isComment(line)){
                    patt1D.getCommentLines().add(line);
                    double wl = searchForWavel(line);
                    if (wl>0){
                        patt1D.setOriginal_wavelength(wl);
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
                    log.debug("error parsing sdev");
                }

                ds.addPoint(new DataPoint(t2,inten,sdev));

                if (!sf.hasNextLine()){
                    ds.setT2f(t2);
                }
            }
            ds.setSerieName(datFile.getName());
            patt1D.AddDataSerie(ds);
            sf.close();

        }catch(Exception e){
            if (D1Dplot_global.isDebug())e.printStackTrace();
            readed = false;
        }
        if (readed){
            return true;
        }else{
            return false;
        }
    }
    
    //only 1 serie
    public static boolean readDAT_ALBA(File datFile, Pattern1D patt1D) {
        boolean firstLine = true;
        boolean readed = true;
        DataSerie ds = new DataSerie();

        try {
            Scanner sf = new Scanner(datFile);
            while (sf.hasNextLine()){
                String line = sf.nextLine();
                if (isComment(line)){
                    patt1D.getCommentLines().add(line);
                    double wl = searchForWavel(line);
                    if (wl>0){
                        patt1D.setOriginal_wavelength(wl);
                        ds.setWavelength(wl);
                    }
                    continue;
                }
                if (line.trim().isEmpty()){
                    continue;
                }

                String values[] = line.trim().split("\\s+");

                double t2 = Double.parseDouble(values[0]);
                double inten = Double.parseDouble(values[1]);
                double sdev = 0.0;
                try{
                    sdev = Double.parseDouble(values[2]);
                }catch(Exception ex){
                    log.debug("error parsing sdev");
                }

                ds.addPoint(new DataPoint(t2,inten,sdev));
                if (firstLine){
                    ds.setT2i(t2);
                    firstLine = false;
                }

                if (!sf.hasNextLine()){
                    ds.setT2f(t2);
                }
            }
            ds.setSerieName(datFile.getName());
            patt1D.AddDataSerie(ds);
            sf.close();

        }catch(Exception e){
            if (D1Dplot_global.isDebug())e.printStackTrace();
            readed = false;
        }
        if (readed){
            return true;
        }else{
            return false;
        }
    }
    
    public static boolean readGR(File datFile, Pattern1D patt1D) {
        boolean firstLine = true;
        boolean readed = true;
        DataSerie ds = new DataSerie();
        boolean startData = false;
        try {
            Scanner sf = new Scanner(datFile);
            while (sf.hasNextLine()){
                String line = sf.nextLine();

                if (line.trim().startsWith("###")){
                    startData = true;
                    continue;
                }
                
                if (!startData){
                    patt1D.getCommentLines().add(line);
                    if (line.contains("wave")){
                        String values[] = line.trim().split("\\s+");
                        try{
                            patt1D.setOriginal_wavelength(Double.parseDouble(values[2]));
                            ds.setWavelength(Double.parseDouble(values[2]));
                        }catch(Exception e){
                            log.debug("error parsing wave");
                        }
                    }
                    if (line.contains("rstep")){
                        String values[] = line.trim().split("\\s+");
                        try{
                            ds.setStep(Double.parseDouble(values[2]));
                        }catch(Exception e){
                            log.debug("error parsing rstep");
                        }
                    }
                    continue;
                }
                
                if ((line.trim().startsWith("#S"))||(line.trim().startsWith("#L"))){
                    patt1D.getCommentLines().add(line);
                    continue;
                }
                
                //arribats aqui son dades
                
                String values[] = line.trim().split("\\s+");

                double x = Double.parseDouble(values[0]);
                double y = Double.parseDouble(values[1]);
                ds.addPoint(new DataPoint(x,y,0.0));
                if (firstLine){
                    ds.setT2i(x);
                    firstLine = false;
                }

                if (!sf.hasNextLine()){
                    ds.setT2f(x);
                }
            }
            ds.setTipusSerie(serieType.gr);
            ds.setxUnits(xunits.G);
            ds.setSerieName(datFile.getName());
            patt1D.AddDataSerie(ds);
            sf.close();

        }catch(Exception e){
            if (D1Dplot_global.isDebug())e.printStackTrace();
            readed = false;
        }
        if (readed){
            return true;
        }else{
            return false;
        }
    }
    
    //TODO: es podria optimitzar omplint un datapoint i afegint-lo a la serie a cada cicle
    private static boolean readXRDML(File f, Pattern1D patt1D){
        boolean pos = false;
        boolean startend = false; //if we have start/end or ListPositions
        DataSerie ds = new DataSerie();
        ArrayList<Double> intensities = new ArrayList<Double>();
        ArrayList<Double> t2ang = new ArrayList<Double>();;
        
        try{
            Scanner sf = new Scanner(f);
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
                        ds.setT2i(Float.parseFloat(temp));
                        
                        //now end position
                        line = sf.nextLine();
                        temp = line.split(">")[1];
                        temp = temp.split("<")[0];
                        ds.setT2f(Float.parseFloat(temp));
                        
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
            sf.close();

            //check if we have to generate 2thetas
            if(startend){
                int nrint = intensities.size();
                log.debug("nrint="+nrint);
                double step = (ds.getT2f()-ds.getT2i())/nrint;
                ds.setStep(step);
                double t2c = ds.getT2i();
                while(t2c<=ds.getT2f()){
                    t2ang.add(t2c);
                    t2c = t2c+step;
                }
                log.debug("nrt2ang="+t2ang.size());
            }
            
            //here we should have t2ang and intensities full and same size, populate dps
            int size = FastMath.min(t2ang.size(), intensities.size());
            if (size == 0)return false;
            for (int i=0; i<size;i++){
                ds.addPoint(new DataPoint(t2ang.get(i),intensities.get(i),0.0f));
            }
            ds.setSerieName(f.getName());
            patt1D.AddDataSerie(ds);
            
        }catch(Exception ex){
            if (D1Dplot_global.isDebug())ex.printStackTrace();
            return false;
        }
        return true;
    }
    
    private static boolean readDATFreeFormat(File f, Pattern1D patt1D){
        boolean firstLine = true;
        DataSerie ds = new DataSerie();
        ArrayList<Double> intensities = new ArrayList<Double>();
        ArrayList<Double> t2ang = new ArrayList<Double>();
        
        try{
            Scanner sf = new Scanner(f);
            while (sf.hasNextLine()){
                String line = sf.nextLine();
                if (isComment(line)){
                    patt1D.getCommentLines().add(line);
                    double wl = searchForWavel(line);
                    if (wl>0){
                        patt1D.setOriginal_wavelength(wl);
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
                        ds.setT2i(Double.parseDouble(values[0]));
                        ds.setStep(Double.parseDouble(values[1]));
                        ds.setT2f(Double.parseDouble(values[2]));
                    }catch(Exception readex){
                        if (D1Dplot_global.isDebug())readex.printStackTrace();
                        log.info("Error reading 1st line of Free Format file (t2i step t2f)");
                        sf.close();
                        return false;
                    }
                    firstLine=false;
                    continue;
                }
                //a partir d'aqu� linies d'intensitats
                for (int i=0;i<values.length;i++){
                    intensities.add(Double.parseDouble(values[i]));
                }
            }
            sf.close();
            
            //now the 2thetas
            int nrint = intensities.size();
            log.debug("nrint="+nrint);
            double t2c = ds.getT2i();
            while(t2c<=ds.getT2f()){
                t2ang.add(t2c);
                t2c = t2c+ds.getStep();
            }
            log.debug("nrt2ang="+t2ang.size());
            
            //here we should have t2ang and intensities full and same size, populate dps
            int size = FastMath.min(t2ang.size(), intensities.size());
            if (size == 0)return false;
            for (int i=0; i<size;i++){
                ds.addPoint(new DataPoint(t2ang.get(i),intensities.get(i),0.0f));
            }
            ds.setSerieName(f.getName());
            patt1D.AddDataSerie(ds);
            
        }catch(Exception ex){
            if (D1Dplot_global.isDebug())ex.printStackTrace();
            return false;
        }
        return true;
    }
    

    
    private static boolean detectDATFreeFormat(File f){
        //al free format la primera linia es 2ti step 2tf
        double t2 = 0.00f;
        double inten = 0.00f;
        double sdev = -100.00f;
        try{
            Scanner sf = new Scanner(f);
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
            sf.close();
            
            //al free format la primera linia es 2ti step 2tf, i l'step ha de ser petit pero no zero (important per els diagrames resta)
            if (inten<0)return false;
            if (FastMath.abs(inten)>0.5)return false;
            if (FastMath.abs(inten)==0.000000f)return false;
            
            if(t2>inten){
                if(sdev>inten){
                    if (sdev>t2){
                        //probablement sera freeformat
                        log.info("free format detected!");
                        return true;
                    }
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }
    
    private static boolean readGSA(File f, Pattern1D patt1D){
        boolean startData = false;
        boolean esdev = false;
        double t2p = 0;
        DataSerie ds = new DataSerie();
        
        try{
            Scanner sf = new Scanner(f);
            while (sf.hasNextLine()){
                String line = sf.nextLine();
                if (isComment(line)){
                    patt1D.getCommentLines().add(line);
                    double wl = searchForWavel(line);
                    if (wl>0){
                        patt1D.setOriginal_wavelength(wl);
                        ds.setWavelength(wl);
                    }
                    continue;
                }
                if (line.trim().startsWith("Instrument")){
                    patt1D.getCommentLines().add(line);
                    continue;
                }
                
                if (line.trim().startsWith("BANK")){
//                    Instrument parameter      bl04.prm                                              
//                    BANK 1   20951    4191 CONST    60.000     0.600  0.0 0.0 ESD   
                    String values[] = line.trim().split("\\s+");
                    ds.setT2i(Double.parseDouble(values[5])/100.0);
                    ds.setStep(Double.parseDouble(values[6])/100.0);
                    t2p = ds.getT2i();
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
                            t2p = t2p + ds.getStep();
                        }
                    }catch(Exception readex){
                        if (D1Dplot_global.isDebug())readex.printStackTrace();
                        log.info("Error GSA file");
                        sf.close();
                        return false;
                    }
                    continue;
                }
            }
            sf.close();
            
            ds.setT2f(ds.getPoint(ds.getNpoints()-1).getX());
            ds.setSerieName(f.getName());
            patt1D.AddDataSerie(ds);
            
        }catch(Exception ex){
            if (D1Dplot_global.isDebug())ex.printStackTrace();
            return false;
        }
        return true;
    }

        
    
    private static boolean readPRF(File f, Pattern1D patt1D){
        boolean startData = false;
        boolean starthkl = false;
        double previous2t = -100.0;
        int linecount = 0;
        DataSerie dsObs = new DataSerie();
        DataSerie dsCal = new DataSerie();
        DataSerie dsDif = new DataSerie();
        DataSerie dsHKL = new DataSerie();
        
        try{
            Scanner sf = new Scanner(f);
            while (sf.hasNextLine()){
                String line = sf.nextLine();
                linecount = linecount +1;
                
                if (linecount == 2){
                    String values[] = line.trim().split("\\s+");
                    dsObs.setWavelength(Double.parseDouble(values[2]));
                    dsCal.setWavelength(Double.parseDouble(values[2]));
                    dsDif.setWavelength(Double.parseDouble(values[2]));
                    dsHKL.setWavelength(Double.parseDouble(values[2]));
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
                    dsHKL.addHKLPoint(new DataHKL(h,k,l,t2i));
                }else{
                    double Iobs = Double.parseDouble(values[1]);
                    double Ical = Double.parseDouble(values[2]);
                    double Ibkg = Double.parseDouble(values[4]);
                    dsObs.addPoint(new DataPoint(t2i,Iobs,0,Ibkg));
                    dsCal.addPoint(new DataPoint(t2i,Ical,0,Ibkg));
                    dsDif.addPoint(new DataPoint(t2i,Iobs-Ical,0));
                    previous2t = t2i;
                }
                
                patt1D.setPrf(true);
            }
            sf.close();
                        
            double[] maxminXY = dsDif.getPuntsMaxXMinXMaxYMinY();
            double maxdif = FastMath.max(FastMath.abs(maxminXY[2]), FastMath.abs(maxminXY[3]));
            dsDif.setYOff(-1*((int)maxdif+100));
            maxminXY = dsObs.getPuntsMaxXMinXMaxYMinY();
            
            dsObs.setTipusSerie(DataSerie.serieType.obs);
            dsCal.setTipusSerie(DataSerie.serieType.cal);
            dsDif.setTipusSerie(DataSerie.serieType.diff);
            dsHKL.setTipusSerie(DataSerie.serieType.hkl);
            dsObs.setSerieName(f.getName()+" ("+dsObs.getTipusSerie().toString()+")");
            dsCal.setSerieName(f.getName()+" ("+dsCal.getTipusSerie().toString()+")");
            dsDif.setSerieName(f.getName()+" ("+dsDif.getTipusSerie().toString()+")");
            dsHKL.setSerieName(f.getName()+" ("+dsHKL.getTipusSerie().toString()+")");
            dsHKL.setYOff(maxminXY[3]);
            patt1D.AddDataSerie(dsObs);
            patt1D.AddDataSerie(dsCal);
            patt1D.AddDataSerie(dsDif);
            patt1D.AddDataSerie(dsHKL);
            
        }catch(Exception ex){
            if (D1Dplot_global.isDebug())ex.printStackTrace();
            return false;
        }
        return true;
        
        
        
    }
    
    public static boolean writeBKG_aprox(Pattern1D patt1D, int serie, int npoints, File outf, boolean overwrite){
        if (outf.exists()&&!overwrite)return false;
        if (outf.exists()&&overwrite)outf.delete();
        
        boolean written = true;
        DataSerie ds = patt1D.getSerie(serie); //SERIE TO WRITE
        
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outf,true)));
            //primer escribim els comentaris
            Iterator<String> itrComm = patt1D.getCommentLines().iterator();
            while (itrComm.hasNext()){
                String cline = itrComm.next();
                out.println(cline);
            }
            //ara posem una linia comentari amb les wavelengths
            if (FastMath.abs(ds.getWavelength()-patt1D.getOriginal_wavelength())>0.01){
                out.println("# wavelength="+FileUtils.dfX_4.format(ds.getWavelength())+" (originalWL="+FileUtils.dfX_4.format(patt1D.getOriginal_wavelength())+")");                
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
            
            int sepN = FastMath.round(ds.getNpoints()/npoints);
            int currSep = 0;
            //escribim el primer punt (sempre)
            String towrite = String.format(" %10.7e  %10.7e  %10.7e",ds.getPoint(0).getX(),ds.getPoint(0).getY(),ds.getPoint(0).getSdy());
            towrite = towrite.replace(",", ".");
            out.println(towrite);
            int nwritten = 1;
            for (int i=0; i<ds.getNpoints();i++){
                if (currSep<sepN){
                    currSep = currSep + 1;
                    continue;
                }
                towrite = String.format(" %10.7e  %10.7e  %10.7e",ds.getPoint(i).getX(),ds.getPoint(i).getY(),ds.getPoint(i).getSdy());
                towrite = towrite.replace(",", ".");
                out.println(towrite);
                nwritten = nwritten + 1;
                currSep = 0;
            }
            out.println(String.format("# %d BACKGROUND POINTS WRITTEN (expected %d)", nwritten,npoints));

            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            written = false;
        }
        return written;
    }
    
    //this one interpolates
    public static boolean writeBKG(Pattern1D patt1D, int serie, int npoints, File outf, boolean overwrite){
        if (outf.exists()&&!overwrite)return false;
        if (outf.exists()&&overwrite)outf.delete();
        
        boolean written = true;
        DataSerie ds = patt1D.getSerie(serie); //SERIE TO WRITE
        
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outf,true)));
            //primer escribim els comentaris
            Iterator<String> itrComm = patt1D.getCommentLines().iterator();
            while (itrComm.hasNext()){
                String cline = itrComm.next();
                out.println(cline);
            }
            //ara posem una linia comentari amb les wavelengths
            if (FastMath.abs(ds.getWavelength()-patt1D.getOriginal_wavelength())>0.01){
                out.println("# wavelength="+FileUtils.dfX_4.format(ds.getWavelength())+" (originalWL="+FileUtils.dfX_4.format(patt1D.getOriginal_wavelength())+")");                
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
            
            double minX = ds.getPoint(0).getX();
            double maxX = ds.getPoint(ds.getNpoints()-1).getX();
            double stepX = (maxX-minX)/npoints;
            
            double currX = minX;
            int nwritten = 0;
            while(currX<=maxX){
                DataPoint[] dps = ds.getSurroundingDPs(currX);
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


            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            written = false;
        }
        return written;
    }
    
    public static boolean writeDAT_ALBA(Pattern1D patt1D, int serie, File outf, boolean overwrite){
        if (outf.exists()&&!overwrite)return false;
        if (outf.exists()&&overwrite)outf.delete();
        
        boolean written = true;
        DataSerie ds = patt1D.getSerie(serie); //SERIE TO WRITE
        
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outf,true)));
            //primer escribim els comentaris
            Iterator<String> itrComm = patt1D.getCommentLines().iterator();
            while (itrComm.hasNext()){
                String cline = itrComm.next();
                out.println(cline);
            }
            //ara posem una linia comentari amb les wavelengths
            if (FastMath.abs(ds.getWavelength()-patt1D.getOriginal_wavelength())>0.01){
                out.println("# wavelength="+FileUtils.dfX_4.format(ds.getWavelength())+" (originalWL="+FileUtils.dfX_4.format(patt1D.getOriginal_wavelength())+")");                
            }

            if (FastMath.abs(ds.getZerrOff())>0.01f){
                out.println("# zeroOffsetApplied="+FileUtils.dfX_3.format(ds.getZerrOff()));
            }
            if (ds.getScale()>1.05 || ds.getScale()<0.95){
                out.println("# scaleFactorApplied="+FileUtils.dfX_2.format(ds.getScale()));
            }
            
            for (int i=0; i<ds.getNpoints();i++){
                String towrite = String.format(" %10.7e  %10.7e  %10.7e",ds.getPoint(i).getX(),ds.getPoint(i).getY(),ds.getPoint(i).getSdy());
                towrite = towrite.replace(",", ".");
                out.println(towrite);
            }
            
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            written = false;
        }
        return written;
    }
    
    public static boolean writeGR(Pattern1D patt1D, int serie, File outf, boolean overwrite){
        if (outf.exists()&&!overwrite)return false;
        if (outf.exists()&&overwrite)outf.delete();
        
        boolean written = true;
        DataSerie ds = patt1D.getSerie(serie); //SERIE TO WRITE
        
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outf,true)));
            //primer escribim els comentaris
            Iterator<String> itrComm = patt1D.getCommentLines().iterator();
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
                String towrite = String.format("%.3f %.7f",ds.getPoint(i).getX(),ds.getPoint(i).getY());
                towrite = towrite.replace(",", ".");
                out.println(towrite);
            }
            
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            written = false;
        }
        return written;
    }
    
    public static boolean writeDAT_FreeFormat(Pattern1D patt1D, int serie, File outf, boolean overwrite){
        if (outf.exists()&&!overwrite)return false;
        if (outf.exists()&&overwrite)outf.delete();
        DataSerie ds = patt1D.getSerie(serie); //SERIE TO WRITE
        boolean written = true;
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outf,true)));
            //TODO AFFEGIR MES COMENTARIS?
            StringBuilder comments = new StringBuilder();
            comments.append("#");
            if (patt1D.getCommentLines().size()>0){
                comments.append(patt1D.getCommentLines().get(0));    
            }
            
            if (FastMath.abs(ds.getWavelength()-patt1D.getOriginal_wavelength())>0.01){
                comments.append(String.format(" wavelength=%8.4f (originalWL=%8.4f",ds.getWavelength(),patt1D.getOriginal_wavelength()));
            }

            if (FastMath.abs(ds.getZerrOff())>0.01f){
                comments.append(" zeroOffsetApplied="+FileUtils.dfX_3.format(ds.getZerrOff()));
            }

            if (ds.getScale()>1.05 || ds.getScale()<0.95){
                comments.append("scaleFactorApplied="+FileUtils.dfX_2.format(ds.getScale()));
            }
            if (ds.getStep()<0)ds.setStep(ds.calcStep());
            
            String towrite = String.format(" %10.7e %10.7e %10.7e %s", FileUtils.round(ds.getT2i(),5),FileUtils.round(ds.getStep(),5),FileUtils.round(ds.getT2f(),5),comments.toString());
            towrite = towrite.replace(",", ".");
            out.println(towrite);
            
            
            //
            double[] intensities = new double[10];
            int ii = 0;
            for (int i=0;i<ds.getNpoints();i++){
                intensities[ii] = ds.getPoint(i).getY();
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
            
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            written = false;
        }
        return written;
    }
    
    public static boolean writeGSA(Pattern1D patt1D, int serie, File outf, boolean overwrite){
        if (outf.exists()&&!overwrite)return false;
        if (outf.exists()&&overwrite)outf.delete();
        DataSerie ds = patt1D.getSerie(serie); //SERIE TO WRITE
        boolean written = true;
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outf,true)));
            
            double maxY = ds.getPuntsMaxXMinXMaxYMinY()[2];
            double top = 1.;
            String warn_msg = "";
            if (maxY>1e5){
                top = (int)(maxY/1e5);
                log.info(" !!!  WARNING : Counts devided by 10 to avoid overflow ");
                warn_msg = warn_msg + " -- !!!  WARNING : Counts devided by 10 to avoid overflow ";
                top = 10.*top;
            }
            out.println("# "+ds.getSerieName()+" "+patt1D.getCommentLines().get(0)+" "+warn_msg);
            out.println("Instrument parameter      bl04.prm ");

            int npts = ds.getNpoints();
            int nrec = (int)((npts - (npts%5))/5.);
            if (npts%5!=0)nrec = nrec +1;
//            BANK 1   20951    4191 CONST    60.000     0.600  0.0 0.0 ESD                   
            if (ds.getStep()<0)ds.setStep(ds.calcStep());
            String linegsa=String.format("BANK 1 %7d %7d CONST %9.3f %9.3f  0.0 0.0 ESD ",npts,nrec,ds.getPoint(0).getX()*100.0,ds.getStep()*100.0);
            out.println(linegsa);
            
            int startIndex = 0;
            int endIndex = ds.getNpoints()-1;
            
            while (startIndex <= endIndex-4){
                try{
                    String towrite = String.format("%8.1f%8.1f%8.1f%8.1f%8.1f%8.1f%8.1f%8.1f%8.1f%8.1f",ds.getPoint(startIndex).getY()/top, ds.getPoint(startIndex).getSdy()/top,
                            ds.getPoint(startIndex+1).getY()/top, ds.getPoint(startIndex+1).getSdy()/top,
                            ds.getPoint(startIndex+2).getY()/top, ds.getPoint(startIndex+2).getSdy()/top,
                            ds.getPoint(startIndex+3).getY()/top, ds.getPoint(startIndex+3).getSdy()/top,
                            ds.getPoint(startIndex+4).getY()/top, ds.getPoint(startIndex+4).getSdy()/top);
                    out.println(towrite);
                    startIndex+=5;
                }catch(Exception ex){
                    ex.printStackTrace();
                    log.debug("error writting gsa");
                    written = false;
                    break;
                }
            }
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            written = false;
        }
        return written;
    }
    
    public static boolean writeASC(Pattern1D patt1D, int serie, File outf, boolean overwrite){
        if (outf.exists()&&!overwrite)return false;
        if (outf.exists()&&overwrite)outf.delete();
        DataSerie ds = patt1D.getSerie(serie); //SERIE TO WRITE
        boolean written = true;
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outf,true)));
            
            for (int i=0; i<ds.getNpoints(); i++){
                String s = String.format("%.6f  %.2f", ds.getPoint(i).getX(),ds.getPoint(i).getY());
                s = s.replace(",", ".");
                out.println(s);                  
            }
                        
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            written = false;
        }
        return written;
    }
    
    //option list2t is to put the positionList and avoid error in 2theta when only ini and fin are given
    public static boolean writeXRDML(Pattern1D patt1D, int serie, File outf,boolean list2T,boolean overwrite){
        if (outf.exists()&&!overwrite)return false;
        if (outf.exists()&&overwrite)outf.delete();
        DataSerie ds = patt1D.getSerie(serie); //SERIE TO WRITE
        boolean written = true;
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outf,true)));
            
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
                log.info("Warning, no wavelength given. It will not be written on the XRDML file");
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
                    double t2 = ds.getPoint(i).getX();
                    String towrite = String.format("%10.7e ", t2);  
                    towrite = towrite.replace(",", ".");
                    sb2T.append(towrite);
                }
                out.println("                <listPositions>"+sb2T.toString().trim()+"</listPositions>");                
            }else{
                out.println("                <startPosition>"+ds.getT2i()+"</startPosition>");
                out.println("                <endPosition>"+ds.getT2f()+"</endPosition>");                
            }
            out.println("                </positions>");
            out.println("                <commonCountingTime unit=\"seconds\">100.0</commonCountingTime>");
            StringBuilder sbInt = new StringBuilder();
            
            for (int i=0; i<ds.getNpoints(); i++){
                double inten = ds.getPoint(i).getY();
                int iint = (int) Math.round(inten);
                sbInt.append(Integer.toString(iint)+" ");
            }
            out.println("                 <intensities unit=\"counts\">"+sbInt.toString().trim()+"</intensities>");
            out.println("            </dataPoints>");
            out.println("        </scan>");
            out.println("    </xrdMeasurement>");
            out.println("</xrdMeasurements>");
            
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            written = false;
        }
        return written;
    }
    
    public static double getScaleFactorToFit(Dimension original, Dimension toFit) {
        double dScale = 1d;
        if (original != null && toFit != null) {
            double dScaleWidth = getScaleFactor(original.width, toFit.width);
            double dScaleHeight = getScaleFactor(original.height, toFit.height);

            dScale = Math.min(dScaleHeight, dScaleWidth);
        }
        return dScale;
    }

    public static double getScaleFactor(int iMasterSize, int iTargetSize) {
        double dScale = 1;
        if (iMasterSize > iTargetSize) {
            dScale = (double) iTargetSize / (double) iMasterSize;
        } else {
            dScale = (double) iTargetSize / (double) iMasterSize;
        }
        return dScale;
    }
    
    
    public static double searchForWavel(String s){
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

}

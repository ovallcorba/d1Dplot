package vava33.d1dplot.auxi;


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


import vava33.d1dplot.D1Dplot_global;

import com.vava33.jutils.FastMath;
import com.vava33.jutils.VavaLogger;
import com.vava33.jutils.FileUtils;

public final class DataFileUtils {

    private static VavaLogger log = D1Dplot_global.getVavaLogger(DataFileUtils.class.getName());
    
    
    public static enum SupportedReadExtensions {DAT,XYE,XY,ASC,XRDML,PRF;}
    public static enum SupportedWriteExtensions {DAT,XYE,ASC,XRDML;}
    public static final Map<String, String> formatInfo;
    static
    {
        formatInfo = new HashMap<String, String>(); //ext, description
        formatInfo.put("dat", "2 or 3 columns file 2th/int/(err) *OR* free format (*.dat)");
        formatInfo.put("xye", "3 columns file 2th/int/err (*.xye)");
        formatInfo.put("xy", "2 columns file 2th/int (*.xy)");
        formatInfo.put("asc", "2 columns file 2th/int with no headers (*.asc)");
        formatInfo.put("xrdml", "Panalytical format (*.xrdml)");
        formatInfo.put("prf", "Obs,calc and difference profiles after fullprof refinement (*.prf)");
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
    
    // OBERTURA DELS DIFERENTS FORMATS DE DADES2D
    public static Pattern1D readPatternFile(File d1file) {
        Pattern1D patt1D = null;
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
                return null;
            }
            format = s;
        }

        switch (format) {
            case DAT:
                if (detectDATFreeFormat(d1file)){
                    patt1D = readDATFreeFormat(d1file);
                }else{
                    patt1D = readDAT_ALBA(d1file);
                }
                break;
//            case DFF:
//                patt1D = readDATFreeFormat(d1file);
//                break;
            case XYE:
                patt1D = readXYE(d1file);
                break;
            case XY:
                patt1D = readXYE(d1file);
                break;
            case ASC:
                patt1D = readASC(d1file);
                break;
            case XRDML:
                patt1D = readXRDML(d1file);
                break;
            case PRF:
                patt1D = readPRF(d1file);
                break;
            default:
                break;

        }
        
        if (patt1D != null) {
            patt1D.setFile(d1file);
        }
        
        //TODO: POSAR UN CHECK que comprovi que hi hagi t2i, t2f, step, patt1d, etc... vamos, que tot estigui correcte
        
        return patt1D;
    }
    
    public static File writePatternFile(File d1File, Pattern1D patt1D, int serie, boolean overwrite) {
        // comprovem extensio
        log.debug(d1File.toString());
        String ext = FileUtils.getExtension(d1File).trim();

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
                //TODO: ask if ALBA or free format
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
            case XRDML:
                written = writeXRDML(patt1D,serie,d1File,true,overwrite);
                break;
            default:
                log.info("Unknown format to write");
                return null;
        }
        if (written) fout = d1File;
        return fout;
    }
    
    private static boolean isComment(String ln){
        if (ln.trim().startsWith("#"))return true;
        if (ln.trim().startsWith("!"))return true;
        if (ln.trim().startsWith("/"))return true;
        if (ln.trim().startsWith("$"))return true;
        return false;
    }
    
    private static Pattern1D readXYE(File f){
        return readDAT_ALBA(f);
    }
    
    private static Pattern1D readASC(File f){
        return readDAT_ALBA(f);

    }
    
    //only 1 serie
    public static Pattern1D readDAT_ALBA(File datFile) {
        Pattern1D patt1D = new Pattern1D(); //create an empty pattern1D
        boolean firstLine = true;
        boolean readed = true;
//        ArrayList<DataPoint> dps = new ArrayList<DataPoint>();
        DataSerie ds = new DataSerie();

        try {
            Scanner sf = new Scanner(datFile);
            while (sf.hasNextLine()){
                String line = sf.nextLine();
                if (isComment(line)){
                    patt1D.getCommentLines().add(line);
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
                    //ex.printStackTrace();
                }

//                patt1D.getPoints().add(new DataPoint(t2,inten,sdev));
//                dps.add(new DataPoint(t2,inten,sdev));
                ds.addPoint(new DataPoint(t2,inten,sdev));
                if (firstLine){
                    ds.setT2i(t2);
                    firstLine = false;
                }

                if (!sf.hasNextLine()){
                    ds.setT2f(t2);
                }
            }
            patt1D.AddDataSerie(ds);
            sf.close();

        }catch(Exception e){
            e.printStackTrace();
            readed = false;
        }
        if (readed){
//            patt1D.setFile(datFile); ho passo al general perque es fa amb tots
            return patt1D;
        }else{
            return null;
        }
    }
    
    //TODO: es podria optimitzar omplint un datapoint i afegint-lo a la serie a cada cicle
    private static Pattern1D readXRDML(File f){
        boolean pos = false;
        boolean startend = false; //if we have start/end or ListPositions
        Pattern1D patt1D = new Pattern1D(); //create an empty pattern1D
//        ArrayList<DataPoint> dps = new ArrayList<DataPoint>();
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
                            //this.sdev.add(0.0f);
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
            if (size == 0)return null;
            for (int i=0; i<size;i++){
                ds.addPoint(new DataPoint(t2ang.get(i),intensities.get(i),0.0f));
//                dps.add(new DataPoint(t2ang.get(i),intensities.get(i),0.0f));
            }
            patt1D.AddDataSerie(ds);
            
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
        return patt1D;
    }
    
    private static Pattern1D readDATFreeFormat(File f){
        boolean firstLine = true;
        Pattern1D patt1D = new Pattern1D(); //create an empty pattern1D
//        ArrayList<DataPoint> dps = new ArrayList<DataPoint>();
        DataSerie ds = new DataSerie();
        ArrayList<Double> intensities = new ArrayList<Double>();
        ArrayList<Double> t2ang = new ArrayList<Double>();
        
        try{
            Scanner sf = new Scanner(f);
            while (sf.hasNextLine()){
                String line = sf.nextLine();
                if (isComment(line)){
                    patt1D.getCommentLines().add(line);
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
                        readex.printStackTrace();
                        log.info("Error reading 1st line of Free Format file (t2i step t2f)");
                        sf.close();
                        return null;
                    }
                    firstLine=false;
                    continue;
                }
                //a partir d'aqu� linies d'intensitats
                for (int i=0;i<values.length;i++){
                    intensities.add(Double.parseDouble(values[i]));
                    //this.sdev.add(0.0f);
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
            if (size == 0)return null;
            for (int i=0; i<size;i++){
                ds.addPoint(new DataPoint(t2ang.get(i),intensities.get(i),0.0f));
            }
            patt1D.AddDataSerie(ds);
            
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
        return patt1D;
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
                    System.out.println("Error detecting DAT format");
                    continue;
                }
                break;
            }
            sf.close();
            
            //al free format la primera linia es 2ti step 2tf
            if(t2>inten){
                if(sdev>inten){
                    if (sdev>t2){
                        //probablement sera freeformat
                        System.out.println("free format detected!");
                        return true;
                    }
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }
    

    private static Pattern1D readPRF(File f){
        boolean startData = false;
        boolean starthkl = false;
        double previous2t = -100.0;
        int linecount = 0;
        Pattern1D patt1D = new Pattern1D(); //create an empty pattern1D
        DataSerie dsObs = new DataSerie();
//        DataSerie dsBkg = new DataSerie();
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
//                    dsBkg.setWavelength(Double.parseDouble(values[2]));
                }
                
//                if (isComment(line)){
//                    patt1D.getCommentLines().add(line);
//                    continue;
//                }
                
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
//                    double esd = FastMath.sqrt(FastMath.abs(Iobs));
                    double Ical = Double.parseDouble(values[2]);
                    double Ibkg = Double.parseDouble(values[4]);
                    dsObs.addPoint(new DataPoint(t2i,Iobs,0,Ibkg));
                    dsCal.addPoint(new DataPoint(t2i,Ical,0,Ibkg));
//                    dsBkg.addPoint(new DataPoint(t2i,Ibkg,0));
                    dsDif.addPoint(new DataPoint(t2i,Iobs-Ical,0));
                    previous2t = t2i;
                }
                
                patt1D.setPrf(true);
            }
            sf.close();
            
//            dsBkg.setPlotThis(false);
            
            double[] maxminXY = dsDif.getPuntsMaxXMinXMaxYMinY();
            double maxdif = FastMath.max(FastMath.abs(maxminXY[2]), FastMath.abs(maxminXY[3]));
//            patt1D.setDiffoffset(-1*((int)maxdif+100));
            dsDif.setYOff(-1*((int)maxdif+100));
            maxminXY = dsObs.getPuntsMaxXMinXMaxYMinY();
            
            //AIXO ESTA MALAMENT PERQUE BARREJA INTENSITAT AMB PIXELS
//            patt1D.setHkloff((int) (maxminXY[3]-patt1D.getHklticksize()+20)); //originalment era *1.3 i no pas +20
            
            dsObs.setTipusSerie(DataSerie.serieType.obs);
            dsCal.setTipusSerie(DataSerie.serieType.cal);
            dsDif.setTipusSerie(DataSerie.serieType.diff);
            dsHKL.setTipusSerie(DataSerie.serieType.hkl);
            patt1D.AddDataSerie(dsObs);
            patt1D.AddDataSerie(dsCal);
//            patt1D.AddDataSerie(dsBkg);
            patt1D.AddDataSerie(dsDif);
            patt1D.AddDataSerie(dsHKL);
            
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
        return patt1D;
        
        
        
    }
    
    public static boolean writeDAT_ALBA(Pattern1D patt1D, int serie, File outf, boolean overwrite){
        if (outf.exists()&&!overwrite)return false;
        if (outf.exists()&&overwrite)outf.delete();
        
        boolean written = true;
        DataSerie ds = patt1D.getSeries().get(serie); //SERIE TO WRITE
        
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
    
    public static boolean writeDAT_FreeFormat(Pattern1D patt1D, int serie, File outf, boolean overwrite){
        if (outf.exists()&&!overwrite)return false;
        if (outf.exists()&&overwrite)outf.delete();
        DataSerie ds = patt1D.getSeries().get(serie); //SERIE TO WRITE
        boolean written = true;
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outf,true)));
            //TODO AFFEGIR COMENTARIS
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
            
//            DecimalFormat df = new DecimalFormat("0.0000000E00");
//            df.setRoundingMode(RoundingMode.HALF_UP);
            String towrite = String.format(" %10.7e %10.7e %10.7e %s", FileUtils.round(ds.getT2i(),5),FileUtils.round(ds.getStep(),5),FileUtils.round(ds.getT2f(),5),comments.toString());
//            String towrite = String.format(" %10.7e %10.7e %10.7e %s", ds.getT2i(),ds.getStep(),ds.getT2f(),comments.toString());
//            String towrite = String.format(" %s %s %s %s", df.format(ds.getT2i()),df.format(ds.getStep()),df.format(ds.getT2f()),comments.toString());
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
//                        towr.append(String.format("%10d ", Math.round(intensities[j])));
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
    
    public static boolean writeASC(Pattern1D patt1D, int serie, File outf, boolean overwrite){
        if (outf.exists()&&!overwrite)return false;
        if (outf.exists()&&overwrite)outf.delete();
        DataSerie ds = patt1D.getSeries().get(serie); //SERIE TO WRITE
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
        DataSerie ds = patt1D.getSeries().get(serie); //SERIE TO WRITE
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
                System.out.println("Warning, no wavelength given. It will not be written on the XRDML file");
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
    
    
    
    private static double getWaveFromXRDML(File f){
        double wave = -1;
        try{
            Scanner sf = new Scanner(f);
            while (sf.hasNextLine()){
                String line = sf.nextLine();
                if (line.contains("<kAlpha1")){
                    String temp = line.split(">")[1];
                    temp = temp.split("<")[0];
                    wave = Double.parseDouble(temp);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return wave;   
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
    

}
